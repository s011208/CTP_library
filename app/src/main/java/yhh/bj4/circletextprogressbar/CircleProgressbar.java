package yhh.bj4.circletextprogressbar;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;

/**
 * Created by yenhsunhuang on 2016/8/11.
 */
public class CircleProgressbar extends View {
    private static final String TAG = "CircleProgressbar";
    private static final boolean DEBUG = true;

    public static final int CLIP_DIRECTION_LEFT_RIGHT = 0;
    public static final int CLIP_DIRECTION_TOP_BOTTOM = 1;
    public static final int CLIP_DIRECTION_RIGHT_LEFT = 2;
    public static final int CLIP_DIRECTION_BOTTOM_TOP = 3;
    private static final int TOTAL_CLIP_DIRECTION_TYPE = 4;

    public static final int ANIMATOR_DURATION_FAST = 800;
    public static final int ANIMATOR_DURATION_NORMAL = 1500;
    public static final int ANIMATOR_DURATION_SLOW = 3000;

    private boolean mShowText = false;
    private boolean mIntermediate = true;
    private boolean mFixedAnimationDirection = false;

    private float mAnimatorValue = 0;

    private Paint mProgressbarPaint;

    private final int[] mDefaultColorList = new int[]{Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW};

    private int[] mCustomColorList;

    private int[] mColorList;

    private final ValueAnimator mDrawAnimator = ValueAnimator.ofFloat(0, mDefaultColorList.length);

    private float mRadius = 200;

    private int mPreviousAnimatorValue = -1;
    private int mClipDirection = CLIP_DIRECTION_LEFT_RIGHT;
    private int mAnimatorDuration = ANIMATOR_DURATION_NORMAL;
    private int mDefaultCircleProgressbarSize;

    {
        mDrawAnimator.setDuration(mAnimatorDuration);
        mDrawAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                mAnimatorValue = (float) valueAnimator.getAnimatedValue();
                invalidate();
            }
        });
        mDrawAnimator.setRepeatMode(ValueAnimator.RESTART);
        mDrawAnimator.setRepeatCount(ValueAnimator.INFINITE);
        mDrawAnimator.setInterpolator(new LinearInterpolator());

        mDrawAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                if (DEBUG) Log.d(TAG, "anim start");
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                if (DEBUG) Log.d(TAG, "anim end");
            }

            @Override
            public void onAnimationCancel(Animator animator) {
                if (DEBUG) Log.d(TAG, "anim cancel");
            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
    }

    public CircleProgressbar(Context context) {
        this(context, null);
    }

    public CircleProgressbar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircleProgressbar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CircleProgressbar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        initAttrs(context, attrs);
        mProgressbarPaint = new Paint();
        mProgressbarPaint.setAntiAlias(true);
        mProgressbarPaint.setStyle(Paint.Style.FILL);
        mDefaultCircleProgressbarSize = context.getResources().getDimensionPixelSize(R.dimen.circle_progressbar_default_size);
        if (DEBUG)
            setKeepScreenOn(true);
    }

    private void initAttrs(Context context, AttributeSet attrs) {
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.CircleProgressbar,
                0, 0);
        try {
            mShowText = a.getBoolean(R.styleable.CircleProgressbar_showText, false);
            mIntermediate = a.getBoolean(R.styleable.CircleProgressbar_intermediate, true);
            mFixedAnimationDirection = a.getBoolean(R.styleable.CircleProgressbar_fixAnimationDirection, false);
            mAnimatorDuration = a.getInt(R.styleable.CircleProgressbar_animatorDuration, ANIMATOR_DURATION_NORMAL);
            setAnimatorDuration(mAnimatorDuration);
        } finally {
            a.recycle();
        }

        if (mIntermediate) {
            mDrawAnimator.start();
        }

        if (DEBUG) {
            Log.d(TAG, "mShowText: " + mShowText);
            Log.d(TAG, "mIntermediate: " + mIntermediate);
            Log.d(TAG, "mFixedAnimationDirection: " + mFixedAnimationDirection);
            Log.d(TAG, "mAnimatorDuration: " + mAnimatorDuration);
        }
    }

    public boolean isFixedAnimationDirection() {
        return mFixedAnimationDirection;
    }

    public void setFixAnimationDirection(boolean fixed) {
        mFixedAnimationDirection = fixed;
    }

    public boolean isShowText() {
        return mShowText;
    }

    public void setShowText(boolean showText) {
        mShowText = showText;
        invalidate();
        requestLayout();
    }

    public boolean isIntermediate() {
        return mIntermediate;
    }

    public void setIntermediate(boolean intermediate) {
        mIntermediate = intermediate;
        if (mIntermediate) {
            if (mDrawAnimator.isStarted()) return;
            mDrawAnimator.start();
        } else {
            mDrawAnimator.cancel();
        }
    }

    public void setCustomColorList(int[] list) {
        mCustomColorList = list;
    }

    public int getAnimatorDuration() {
        return mAnimatorDuration;
    }

    public void setAnimatorDuration(int duration) {
        mAnimatorDuration = duration;
        mDrawAnimator.setDuration(mAnimatorDuration);
    }

    // Activity life cycle

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (mIntermediate && !mDrawAnimator.isRunning()) {
            mDrawAnimator.start();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mDrawAnimator.isStarted()) mDrawAnimator.cancel();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int desiredWidth = mDefaultCircleProgressbarSize;
        int desiredHeight = mDefaultCircleProgressbarSize;

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int width;
        int height;

        if (widthMode == MeasureSpec.EXACTLY) {
            width = widthSize;
        } else if (widthMode == MeasureSpec.AT_MOST) {
            width = Math.min(desiredWidth, widthSize);
        } else {
            width = desiredWidth;
        }

        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize;
        } else if (heightMode == MeasureSpec.AT_MOST) {
            height = Math.min(desiredHeight, heightSize);
        } else {
            height = desiredHeight;
        }
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mRadius = Math.min((w - (getPaddingLeft() + getPaddingRight())) / 2,
                ((h - (getPaddingTop() + getPaddingBottom()))) / 2);
        if (DEBUG) Log.d(TAG, "onSizeChanged, mRadius: " + mRadius);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mColorList = (mCustomColorList == null || mCustomColorList.length == 0) ? mDefaultColorList : mCustomColorList;
        final int intValue = (int) mAnimatorValue % mColorList.length;
        final float floatValue = mAnimatorValue - intValue;
        mProgressbarPaint.setColor(mColorList[intValue]);
        canvas.drawCircle(mRadius, mRadius, mRadius, mProgressbarPaint);
        mProgressbarPaint.setColor(mColorList[(intValue - 1 + mColorList.length) % mColorList.length]);
        if (!mFixedAnimationDirection) {
            if (mPreviousAnimatorValue != intValue) {
                mClipDirection = (mClipDirection + 1) % TOTAL_CLIP_DIRECTION_TYPE;
                if (mClipDirection >= TOTAL_CLIP_DIRECTION_TYPE) mClipDirection = 0;
            }
        }

        canvas.save();
        switch (mClipDirection) {
            case CLIP_DIRECTION_BOTTOM_TOP:
                canvas.clipRect(0, 0, mRadius * 2, mRadius * 2 * (1 - floatValue));
                break;
            case CLIP_DIRECTION_LEFT_RIGHT:
                canvas.clipRect(mRadius * 2 * floatValue, 0, mRadius * 2, mRadius * 2);
                break;
            case CLIP_DIRECTION_RIGHT_LEFT:
                canvas.clipRect(0, 0, mRadius * 2 * (1 - floatValue), mRadius * 2);
                break;
            case CLIP_DIRECTION_TOP_BOTTOM:
            default:
                canvas.clipRect(0, mRadius * 2 * floatValue, mRadius * 2, mRadius * 2);
                break;
        }

        canvas.drawCircle(mRadius, mRadius, mRadius, mProgressbarPaint);
        canvas.restore();
        mPreviousAnimatorValue = intValue;
    }
}
