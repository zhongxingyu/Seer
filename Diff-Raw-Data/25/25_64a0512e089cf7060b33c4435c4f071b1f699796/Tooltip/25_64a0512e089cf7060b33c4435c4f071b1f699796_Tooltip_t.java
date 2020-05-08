 package com.heavyplayer.tooltip;
 
 import android.annotation.SuppressLint;
 import android.annotation.TargetApi;
 import android.app.Activity;
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Path;
 import android.graphics.PixelFormat;
 import android.graphics.Point;
 import android.graphics.Rect;
 import android.graphics.Typeface;
 import android.graphics.drawable.Drawable;
 import android.graphics.drawable.ShapeDrawable;
 import android.graphics.drawable.shapes.RoundRectShape;
 import android.os.Build;
 import android.util.TypedValue;
 import android.view.Display;
 import android.view.Gravity;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.ViewTreeObserver;
 import android.view.WindowManager;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 public class Tooltip extends ViewGroup {
     // TODO: Most of these could/should be settable.
     private static final int TEXT_SIZE_SP = 15;
     private static final int ARROW_SIDE_SIZE_DP = 10;
     private static final int ROUNDED_CORNERS_RADII_DP = 4;
     private static final int PADDING_VERTICAL_DP = 7;
     private static final int PADDING_HORIZONTAL_DP = 14;
 
     private final UpdateWindowListener UPDATE_WINDOW_LISTENER = new UpdateWindowListener();
 
     private Activity mActivity;
     private WindowManager mWindowManager;
     private WindowManager.LayoutParams mWindowLayoutParams;
     private LayoutInflater mLayoutInflater;
     private boolean mVisible;
 
     private Rect mTarget;
     private View mTargetView;
     private Integer mTargetX, mTargetY, mTargetWidth, mTargetHeight;
     private android.view.MenuItem mTargetMenuItem;
     private com.actionbarsherlock.view.MenuItem mTargetMenuItemSherlock;
 
     private Integer mGravity;
     private Point mWindowPosition = new Point();
 
     private ArrowView mArrowView;
     private BalloonView mBalloonView;
 
     private int mColor = Color.WHITE;
     private CharSequence mText;
     private int mTextColor = Color.BLACK;
 
     private OnShowListener mOnShowListener;
     private OnDismissListener mOnDismissListener;
 
     public Tooltip(Activity activity) {
         super(activity);
 
         mActivity = activity;
         mWindowManager = (WindowManager)activity.getSystemService(Activity.WINDOW_SERVICE);
         mLayoutInflater = (LayoutInflater)activity.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
     }
 
     public void show() {
         // Create and add inner views.
         mArrowView = new ArrowView(mActivity);
         addView(mArrowView, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
         mBalloonView = new BalloonView(mActivity);
         addView(mBalloonView, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
         // TODO: Configure the views here instead of having the values read from super (this).
 
         setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 dismiss();
             }
         });
 
         locateTarget(new OnTargetExtractedListener() {
             @Override
             public void onTargetExtracted(boolean immediate, boolean visible, boolean changed) {
                 calculateGravity();
                 calculateWindowPosition();
 
                 mWindowLayoutParams = new WindowManager.LayoutParams(
                         WindowManager.LayoutParams.WRAP_CONTENT,
                         WindowManager.LayoutParams.WRAP_CONTENT,
                         mWindowPosition.x,
                         mWindowPosition.y,
                         WindowManager.LayoutParams.TYPE_APPLICATION_PANEL,
                         WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                         PixelFormat.TRANSLUCENT);
                 mWindowLayoutParams.windowAnimations = R.style.TooltipAnimation;
                 mWindowLayoutParams.gravity = Gravity.LEFT | Gravity.TOP;
                 mWindowManager.addView(Tooltip.this, mWindowLayoutParams);
 
                 if(visible) {
                     mVisible = true;
                 } else {
                     setVisibility(View.GONE);
                     mVisible = false;
                 }
 
                 if(mTargetView != null) {
                     if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                         mTargetView.setHasTransientState(true);
                     mTargetView.getViewTreeObserver().addOnPreDrawListener(UPDATE_WINDOW_LISTENER);
                 }
 
                 if(mOnShowListener != null)
                     mOnShowListener.onShow(Tooltip.this);
             }
         });
     }
 
     public void dismiss() {
         if(mTargetView != null) {
             if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                 mTargetView.setHasTransientState(false);
             mTargetView.getViewTreeObserver().removeOnPreDrawListener(UPDATE_WINDOW_LISTENER);
         }
 
         mWindowManager.removeView(this);
 
         if(mOnDismissListener != null)
             mOnDismissListener.onDismiss(this);
     }
 
     private void updateWindow() {
         locateTarget(new OnTargetExtractedListener() {
             @Override
             public void onTargetExtracted(boolean immediate, boolean visible, boolean changed) {
                 if(changed) {
                     calculateWindowPosition();
 
                     mWindowLayoutParams.x = mWindowPosition.x;
                     mWindowLayoutParams.y = mWindowPosition.y;
 
                     if(mVisible && visible) {
                         mWindowManager.updateViewLayout(Tooltip.this, mWindowLayoutParams);
                     } else if(mVisible && !visible) {
                         setVisibility(View.GONE);
                         mVisible = false;
                     } else if(!mVisible && visible) {
                         setVisibility(View.VISIBLE);
                         mVisible = true;
                     }
                 }
             }
         });
     }
 
     /**
      * Set the view which is targeted by this tutorial.
      *
      * @param targetView The tutorial target
      */
     public void setTarget(View targetView) {
         mTargetView = targetView;
     }
 
     /**
      * Set the coordinates which are targeted by this tutorial.
      *
      * @param targetX the X center coordinate
      * @param targetY the Y center coordinate
      */
     public void setTarget(int targetX, int targetY) {
         mTargetX = targetX;
         mTargetY = targetY;
     }
 
     /**
      * Set the coordinates which are targeted by this tutorial.
      *
      * @param targetX the X center coordinate
      * @param targetY the Y center coordinate
      * @param targetWidth the width of the target area
      * @param targetHeight the height of the target area
      */
     public void setTarget(int targetX, int targetY, int targetWidth, int targetHeight) {
         mTargetX = targetX;
         mTargetY = targetY;
         mTargetWidth = targetWidth;
         mTargetHeight = targetHeight;
     }
 
     /**
      * Set the menu item targeted by this tutorial.
      *
      * Only visible Action Bar menu items are supported. When used with collapsed
      * menu items, or the old bottom menus, the behaviour is undefined.
      *
      * @param targetMenuItem the menu item
      */
     @TargetApi(Build.VERSION_CODES.HONEYCOMB)
     public void setTarget(android.view.MenuItem targetMenuItem) {
         View actionView = targetMenuItem.getActionView();
         if(actionView != null)
             setTarget(actionView);
         else
             mTargetMenuItem = targetMenuItem;
     }
 
     /**
      * Set the menu item targeted by this tutorial.
      *
      * Only visible Action Bar menu items are supported. When used with collapsed
      * menu items, or the old bottom menus, the behaviour is undefined.
      *
      * @param targetMenuItemSherlock the menu item
      */
     public void setTarget(com.actionbarsherlock.view.MenuItem targetMenuItemSherlock) {
         View actionView = targetMenuItemSherlock.getActionView();
         if(actionView != null)
             setTarget(actionView);
         else
             mTargetMenuItemSherlock = targetMenuItemSherlock;
     }
 
     /**
      * Set the tooltip's color.
      */
     public void setColor(int color) {
         mColor = color;
     }
 
     /**
      * Set the text that appears in the tooltip.
      */
     public void setText(CharSequence text) {
         mText = text;
     }
 
     /**
      * Set the tooltip's text color.
      */
     public void setTextColor(int textColor) {
         mTextColor = textColor;
     }
 
     /**
      * Set the listener to be invoked when the tooltip is shown.
      */
     public void setOnShowListener(OnShowListener listener) {
         mOnShowListener = listener;
     }
 
     /**
      * Set the listener to be invoked when the tooltip is dismissed.
      */
     public void setOnDismissListener(OnDismissListener listener) {
         mOnDismissListener = listener;
     }
 
     @SuppressLint("NewApi")
     private void locateTarget(final OnTargetExtractedListener onTargetExtractedListener) {
         final Rect previousTarget = mTarget;
 
         if(mTargetX != null && mTargetY != null) {
             mTarget = new Rect(mTargetX, mTargetY, mTargetX, mTargetY);
 
             if(mTargetWidth != null && mTargetHeight != null) {
                 mTarget.left = mTargetX - (int)(mTargetWidth / 2f);
                 mTarget.top = mTargetY - (int)(mTargetHeight / 2f);
                 mTarget.right = mTargetX + (int)(mTargetWidth / 2f);
                 mTarget.bottom = mTargetY + (int)(mTargetHeight / 2f);
             }
 
             if(onTargetExtractedListener != null) {
                 Point displaySize = getDisplaySize();
                 onTargetExtractedListener.onTargetExtracted(
                         true,
                         ((mTarget.right >= 0 || mTarget.left <= displaySize.x) && (mTarget.top >= 0 || mTarget.bottom <= displaySize.y)),
                         !mTarget.equals(previousTarget)
                 );
             }
         }
         else if(mTargetView != null) {
             locateTargetByView(mTargetView);
 
             if(onTargetExtractedListener != null) {
                 Rect rect = new Rect();
                 onTargetExtractedListener.onTargetExtracted(
                         true,
                         mTargetView.getLocalVisibleRect(rect) && mTargetView.isShown(),
                         !mTarget.equals(previousTarget)
                 );
             }
         }
         else if(mTargetMenuItem != null) {
             final View actionView = mLayoutInflater.inflate(R.layout.ab_placeholder_item, new LinearLayout(mActivity), false);
             if(actionView != null) {
                 mTargetMenuItem.setActionView(actionView);
                 actionView.post(new Runnable() {
                     @Override
                     public void run() {
                         locateTargetByView(actionView);
 
                         if(onTargetExtractedListener != null)
                             onTargetExtractedListener.onTargetExtracted(
                                     false,
                                     actionView.getLocalVisibleRect(new Rect()) && actionView.isShown(),
                                     !mTarget.equals(previousTarget)
                             );
 
                         mTargetMenuItem.setActionView(null);
                     }
                 });
             }
         }
         else if(mTargetMenuItemSherlock != null) {
             final View actionView = mLayoutInflater.inflate(R.layout.ab_placeholder_item, new LinearLayout(mActivity), false);
             mTargetMenuItemSherlock.setActionView(actionView);
             if(actionView != null) {
                 actionView.post(new Runnable() {
                     @Override
                     public void run() {
                         locateTargetByView(actionView);
 
                         if(onTargetExtractedListener != null)
                             onTargetExtractedListener.onTargetExtracted(
                                     false,
                                     actionView.getLocalVisibleRect(new Rect()) && actionView.isShown(),
                                     !mTarget.equals(previousTarget)
                             );
 
                         mTargetMenuItemSherlock.setActionView(null);
                     }
                 });
             }
         }
         else {
             if(onTargetExtractedListener != null)
                 onTargetExtractedListener.onTargetExtracted(true, true, previousTarget != null);
         }
     }
 
     private void calculateGravity() {
         if(mTarget == null)
             throw new IllegalStateException("The target must be set.");
 
         Point displaySize = getDisplaySize();
 
         // Multiply each space by the opposite action to uniform the scale.
         int leftSpace = mTarget.left * displaySize.y;
         int topSpace = mTarget.top * displaySize.x;
         int rightSpace = (displaySize.x - mTarget.right) * displaySize.y;
         int bottomSpace = (displaySize.y - mTarget.bottom) * displaySize.x;
 
         int mostSpacious = Math.max(leftSpace, Math.max(topSpace, Math.max(rightSpace, bottomSpace)));
 
         // Prefer TOP or BOTTOM by allowing them to be up to 30% smaller.
         if(topSpace == mostSpacious || (bottomSpace != mostSpacious && topSpace > 0.7 * mostSpacious))
             mGravity = Gravity.TOP;
         else if(bottomSpace == mostSpacious || (topSpace != mostSpacious && bottomSpace > 0.7 * mostSpacious))
             mGravity = Gravity.BOTTOM;
         else if(leftSpace == mostSpacious)
             mGravity = Gravity.LEFT;
         else if(rightSpace == mostSpacious)
             mGravity = Gravity.RIGHT;
     }
 
     private void calculateWindowPosition() {
         if(mArrowView == null || mBalloonView == null)
             throw new IllegalStateException("Both child views need to be created.");
         if(mGravity == null)
             throw new IllegalStateException("Gravity must be calculated");
 
         Point displaySize = getDisplaySize();
         int widthMeasureSpec = MeasureSpec.makeMeasureSpec(displaySize.x, MeasureSpec.AT_MOST);
         int heightMeasureSpec = MeasureSpec.makeMeasureSpec(displaySize.y, MeasureSpec.AT_MOST);
         measureChild(mArrowView, widthMeasureSpec, heightMeasureSpec);
         measureChild(mBalloonView, widthMeasureSpec, heightMeasureSpec);
 
         int arrowWidth = mArrowView.getMeasuredWidth();
         int arrowHeight = mArrowView.getMeasuredHeight();
         int balloonWidth = mBalloonView.getMeasuredWidth();
         int balloonHeight = mBalloonView.getMeasuredHeight();
 
         // Set common properties.
         switch(mGravity) {
             case Gravity.TOP:
             case Gravity.BOTTOM:
                 mWindowPosition.x = trim(
                         mTarget.centerX() - balloonWidth / 2,
                         mTarget.left - arrowWidth - getRoundedCornersRadii(),
                         mTarget.right - arrowWidth - getRoundedCornersRadii());
                 break;
 
             case Gravity.LEFT:
             case Gravity.RIGHT:
                 mWindowPosition.y = trim(mTarget.centerY() - balloonHeight / 2, 0, displaySize.y - balloonHeight);
                 break;
         }
 
         // Set individual properties.
         switch(mGravity) {
             case Gravity.TOP:
                 mWindowPosition.y = mTarget.top - balloonHeight - arrowHeight / 2;
                 break;
 
             case Gravity.BOTTOM:
                 mWindowPosition.y = mTarget.bottom - arrowHeight / 2;
                 break;
 
             case Gravity.LEFT:
                 mWindowPosition.x = mTarget.left - balloonWidth - arrowWidth / 2;
                 break;
 
             case Gravity.RIGHT:
                 mWindowPosition.x = mTarget.right - arrowWidth / 2;
                 break;
         }
     }
 
     @Override
     protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
         int arrowWidth = mArrowView.getMeasuredWidth();
         int arrowHeight = mArrowView.getMeasuredHeight();
         int balloonWidth = mBalloonView.getMeasuredWidth();
         int balloonHeight = mBalloonView.getMeasuredHeight();
 
         switch(mGravity) {
             case Gravity.TOP:
             case Gravity.BOTTOM:
                 setMeasuredDimension(balloonWidth, arrowHeight + balloonHeight);
                 break;
 
             case Gravity.LEFT:
             case Gravity.RIGHT:
                 setMeasuredDimension(arrowWidth + balloonWidth, balloonHeight);
                 break;
 
             default:
                 super.onMeasure(widthMeasureSpec, heightMeasureSpec);
                 break;
         }
     }
 
     @Override
     protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
         int width = right - left;
         int height = bottom - top;
 
         int arrowWidth = mArrowView.getMeasuredWidth();
         int arrowHeight = mArrowView.getMeasuredHeight();
         int balloonWidth = mBalloonView.getMeasuredWidth();
         int balloonHeight = mBalloonView.getMeasuredHeight();
 
         int arrowLeft = 0;
         int arrowTop = 0;
         int balloonLeft = 0;
         int balloonTop = 0;
 
         Point displaySize = getDisplaySize();
 
         // Set common properties.
         switch(mGravity) {
             case Gravity.TOP:
             case Gravity.BOTTOM:
                 arrowLeft = trim(
                         mTarget.centerX() - trim(mWindowPosition.x, 0, displaySize.x - width) - arrowWidth / 2,
                         getRoundedCornersRadii(),
                         getWidth() - getRoundedCornersRadii() - arrowWidth);
                 balloonLeft = 0;
                 break;
 
             case Gravity.LEFT:
             case Gravity.RIGHT:
                 arrowTop = trim(
                         mTarget.centerY() - trim(mWindowPosition.y, 0, displaySize.y - height) - arrowHeight / 2,
                         getRoundedCornersRadii(),
                         getHeight() - arrowHeight - getRoundedCornersRadii());
                 balloonTop = 0;
                 break;
         }
 
         // Set individual properties.
         switch(mGravity) {
             case Gravity.TOP:
                 arrowTop = balloonHeight;
                 balloonTop = 0;
                 break;
 
             case Gravity.BOTTOM:
                 arrowTop = 0;
                 balloonTop = arrowHeight;
                 break;
 
             case Gravity.LEFT:
                 arrowLeft = balloonWidth;
                 balloonTop = 0;
                 break;
 
             case Gravity.RIGHT:
                 arrowLeft = 0;
                 balloonLeft = arrowWidth;
                 break;
         }
 
         // Window Manager never lays out the content outside the screen. Account for that.
         if(mWindowPosition.x < -(width - width / 2 + arrowWidth / 2)) {
             int xDiff = mWindowPosition.x - -(width - width / 2 + arrowWidth / 2);
             arrowLeft += xDiff;
             balloonLeft += xDiff;
         }
         else if(mWindowPosition.x > displaySize.x - width / 2 + arrowWidth / 2) {
             int xDiff = mWindowPosition.x - (displaySize.x - width / 2 + arrowWidth / 2);
             arrowLeft += xDiff;
             balloonLeft += xDiff;
         }
         if(mWindowPosition.y < -(height - height / 2 + arrowHeight / 2)) {
             int yDiff = mWindowPosition.y - -(height - height / 2 + arrowHeight / 2);
             arrowTop += yDiff;
             balloonTop += yDiff;
         }
         else if(mWindowPosition.y > displaySize.y - height / 2 + arrowHeight / 2) {
             int yDiff = mWindowPosition.y - (displaySize.y - height / 2 + arrowHeight / 2);
             arrowTop += yDiff;
             balloonTop += yDiff;
         }
 
         mArrowView.layout(arrowLeft, arrowTop, arrowLeft + arrowWidth, arrowTop + arrowHeight);
         mBalloonView.layout(balloonLeft, balloonTop, balloonLeft + balloonWidth, balloonTop + balloonHeight);
     }
 
    // TODO: Cache these values.
     private int getArrowSideSize() {
        int px = dpToPx(ARROW_SIDE_SIZE_DP);
        return (px & 1) == 0 ? px : px -1; // Ensure it's even.
     }
 
     private int getRoundedCornersRadii() {
         return dpToPx(ROUNDED_CORNERS_RADII_DP);
     }
 
     private int getPaddingVertical() {
         return dpToPx(PADDING_VERTICAL_DP);
     }
 
     private int getPaddingHorizontal() {
         return dpToPx(PADDING_HORIZONTAL_DP);
     }
 
     private int dpToPx(int dpValue) {
        return (int)TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dpValue, mActivity.getResources().getDisplayMetrics());
     }
 
     @SuppressLint("NewApi")
     @SuppressWarnings("deprecation")
     private Point getDisplaySize() {
         Point displaySize = new Point();
         Display display = mWindowManager.getDefaultDisplay();
 
         if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
             display.getSize(displaySize);
         }
         else {
             displaySize.x = display.getWidth();
             displaySize.y = display.getHeight();
         }
 
         return displaySize;
     }
 
     private void locateTargetByView(View view) {
         int[] position = new int[2];
 
         view.getLocationOnScreen(position);
 
         mTarget = new Rect();
         mTarget.left = position[0];
         mTarget.top = position[1];
         mTarget.right = mTarget.left + view.getWidth();
         mTarget.bottom = mTarget.top + view.getHeight();
     }
 
     private int trim(int val, int min, int max) {
         return Math.max(min, Math.min(max, val));
     }
 
     public interface OnShowListener {
         public void onShow(Tooltip tooltip);
     }
 
     public interface OnDismissListener {
         public void onDismiss(Tooltip tooltip);
     }
 
     private class BalloonView extends TextView {
         private BalloonView(Context context) {
             super(context);
 
             setText(mText);
             setTextColor(mTextColor);
             setTextSize(TypedValue.COMPLEX_UNIT_SP, TEXT_SIZE_SP);
             setTypeface(null, Typeface.BOLD);
             setIncludeFontPadding(false);
             setBackground(new ShapeDrawable(new ColoredRoundRectShape(getRoundedCornersRadii())));
             setPadding(getPaddingHorizontal(), getPaddingVertical(), getPaddingHorizontal(), getPaddingVertical());
         }
 
         @SuppressWarnings("deprecation")
         @SuppressLint("NewApi")
         @Override
         public void setBackground(Drawable background) {
             if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                 super.setBackground(background);
             else
                 super.setBackgroundDrawable(background);
         }
 
         private class ColoredRoundRectShape extends RoundRectShape {
             private Paint mPaint;
 
             private ColoredRoundRectShape(float radii) {
                 super(new float[]{radii, radii, radii, radii, radii, radii, radii, radii}, null, null);
 
                 mPaint = new Paint();
                 mPaint.setAntiAlias(true);
                 mPaint.setColor(mColor);
             }
 
             @Override
             public void draw(Canvas canvas, Paint paint) {
                 super.draw(canvas, mPaint);
             }
         }
     }
 
     private class ArrowView extends View {
         private Paint mPaint;
         private Path mPath;
 
         public ArrowView(Context context) {
             super(context);
         }
 
         private void ensurePaint() {
             if(mPaint == null) {
                 mPaint = new Paint();
                 mPaint.setStyle(Paint.Style.FILL);
                 mPaint.setAntiAlias(true);
                 mPaint.setColor(mColor);
             }
         }
 
         private void ensurePath() {
             if(mPath == null) {
                 mPath = new Path();
                 mPath.setFillType(Path.FillType.EVEN_ODD);
 
                 int otherSideSize = getOtherSideSize();
                 mPath.moveTo(0, 0);
                 mPath.lineTo(otherSideSize / 2, getArrowSideSize());
                 mPath.lineTo(otherSideSize, 0);
 
                 mPath.close();
             }
         }
 
         private int getOtherSideSize() {
             return getArrowSideSize() * 2;
         }
 
         @Override
         protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
             switch(mGravity) {
                 case Gravity.TOP:
                 case Gravity.BOTTOM:
                     setMeasuredDimension(getOtherSideSize(), getArrowSideSize());
                     break;
 
                 case Gravity.LEFT:
                 case Gravity.RIGHT:
                     setMeasuredDimension(getArrowSideSize(), getOtherSideSize());
                     break;
 
                 default:
                     setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec));
                     break;
             }
         }
 
         @Override
         protected void onDraw(Canvas canvas) {
             ensurePaint();
             ensurePath();
 
            int count = canvas.save();
             switch(mGravity) {
                 case Gravity.BOTTOM:
                    canvas.rotate(180, getOtherSideSize() / 2, getArrowSideSize() / 2);
                     break;
 
                 case Gravity.LEFT:
                    canvas.rotate(-90, getOtherSideSize() / 2, getOtherSideSize() / 2);
                     break;
 
                 case Gravity.RIGHT:
                    canvas.rotate(90, getArrowSideSize() / 2, getArrowSideSize() / 2);
                     break;
 
                 default:
                     break;
             }
             canvas.drawPath(mPath, mPaint);
             canvas.restoreToCount(count);
         }
     }
 
     private interface OnTargetExtractedListener {
         /**
          * @param immediate true if we were able to extract the target location immediately, false if not
          * @param visible true if the target is currently visible on screen, false if not
          * @param changed true if the target changed its location, false if not
          */
         void onTargetExtracted(boolean immediate, boolean visible, boolean changed);
     }
 
     private class UpdateWindowListener implements ViewTreeObserver.OnPreDrawListener {
         @Override
         public boolean onPreDraw() {
             updateWindow();
             return true;
         }
     }
 }
