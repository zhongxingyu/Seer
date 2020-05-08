 package com.sonyericsson.extras.liveware.extension.util.view;
 
 import com.sonyericsson.extras.liveware.aef.control.Control;
 import com.sonyericsson.extras.liveware.extension.util.control.ControlExtension;
 import com.sonyericsson.extras.liveware.extension.util.control.ControlTouchEvent;
 import com.sonyericsson.extras.liveware.sdk.R;
 
 import android.content.Context;
 import android.content.res.Resources;
 import android.graphics.Bitmap;
 import android.graphics.Color;
 import android.graphics.Bitmap.Config;
 import android.graphics.PorterDuff.Mode;
 import android.graphics.Canvas;
 import android.view.LayoutInflater;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.MeasureSpec;
 import android.view.ViewGroup;
 import android.view.ViewGroup.LayoutParams;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class ControlExtensionViewGroup extends ControlExtension {
     private static final int DEVICE_SMART_WATCH = 0;
     private static final int DEVICE_HEADSET = 1;
 
     private int mDevice;
 
     private int mWidth;
     private int mHeight;
 
     private List<View> mChildren;
     private int mScrollX;
     private int mScrollY;
     private long mDownStartTime;
     final private Bitmap mBitmap;
     final private Canvas mCanvas;
     private int mPaddingLeft;
     private int mPaddingRight;
     private int mPaddingTop;
     private int mPaddingBottom;
 
     /**
      * Simple constructor to use when creating a view from code.
      *
      * @param context The Context the view is running in, through which it can
      *        access the current theme, resources, etc.
      */
     public ControlExtensionViewGroup(final Context context, final int device,
             final String hostAppPackageName) {
         super(context, hostAppPackageName);
 
        if (device != DEVICE_HEADSET && device != DEVICE_SMART_WATCH) {
             throw new IllegalArgumentException("Invalid device");
         }
 
         mDevice = device;
 
         mWidth = getSupportedControlWidth();
         mHeight = getSupportedControlHeight();
         mChildren = new ArrayList<View>();
         mBitmap = Bitmap.createBitmap(mWidth, mHeight, Config.RGB_565);
         mCanvas = new Canvas(mBitmap);
     }
 
 
     /**
      * Ask all of the children of this view to measure themselves, taking into
      * account both the MeasureSpec requirements for this view and its padding.
      * We skip children that are in the GONE state The heavy lifting is done in
      * getChildMeasureSpec.
      *
      * @param widthMeasureSpec The width requirements for this view
      * @param heightMeasureSpec The height requirements for this view
      */
     protected void measureChildren(int widthMeasureSpec, int heightMeasureSpec) {
         for (View child : mChildren) {
             if (child.getVisibility() != View.GONE) {
                 measureChild(child, widthMeasureSpec, heightMeasureSpec);
             }
         }
     }
 
     /**
      * Ask one of the children of this view to measure itself, taking into
      * account both the MeasureSpec requirements for this view and its padding.
      * The heavy lifting is done in getChildMeasureSpec.
      *
      * @param child The child to measure
      * @param parentWidthMeasureSpec The width requirements for this view
      * @param parentHeightMeasureSpec The height requirements for this view
      */
     protected void measureChild(View child, int parentWidthMeasureSpec,
             int parentHeightMeasureSpec) {
         final LayoutParams lp = child.getLayoutParams();
 
         final int childWidthMeasureSpec = getChildMeasureSpec(parentWidthMeasureSpec,
                 mPaddingLeft + mPaddingRight, lp.width);
         final int childHeightMeasureSpec = getChildMeasureSpec(parentHeightMeasureSpec,
                 mPaddingTop + mPaddingBottom, lp.height);
 
         child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
     }
 
 
     /**
      * Does the hard part of measureChildren: figuring out the MeasureSpec to
      * pass to a particular child. This method figures out the right MeasureSpec
      * for one dimension (height or width) of one child view.
      *
      * The goal is to combine information from our MeasureSpec with the
      * LayoutParams of the child to get the best possible results. For example,
      * if the this view knows its size (because its MeasureSpec has a mode of
      * EXACTLY), and the child has indicated in its LayoutParams that it wants
      * to be the same size as the parent, the parent should ask the child to
      * layout given an exact size.
      *
      * @param spec The requirements for this view
      * @param padding The padding of this view for the current dimension and
      *        margins, if applicable
      * @param childDimension How big the child wants to be in the current
      *        dimension
      * @return a MeasureSpec integer for the child
      */
     public static int getChildMeasureSpec(int spec, int padding, int childDimension) {
         int specMode = MeasureSpec.getMode(spec);
         int specSize = MeasureSpec.getSize(spec);
 
         int size = Math.max(0, specSize - padding);
 
         int resultSize = 0;
         int resultMode = 0;
 
         switch (specMode) {
         // Parent has imposed an exact size on us
         case MeasureSpec.EXACTLY:
             if (childDimension >= 0) {
                 resultSize = childDimension;
                 resultMode = MeasureSpec.EXACTLY;
             } else if (childDimension == LayoutParams.FILL_PARENT) {
                 // Child wants to be our size. So be it.
                 resultSize = size;
                 resultMode = MeasureSpec.EXACTLY;
             } else if (childDimension == LayoutParams.WRAP_CONTENT) {
                 // Child wants to determine its own size. It can't be
                 // bigger than us.
                 resultSize = size;
                 resultMode = MeasureSpec.AT_MOST;
             }
             break;
 
         // Parent has imposed a maximum size on us
         case MeasureSpec.AT_MOST:
             if (childDimension >= 0) {
                 // Child wants a specific size... so be it
                 resultSize = childDimension;
                 resultMode = MeasureSpec.EXACTLY;
             } else if (childDimension == LayoutParams.FILL_PARENT) {
                 // Child wants to be our size, but our size is not fixed.
                 // Constrain child to not be bigger than us.
                 resultSize = size;
                 resultMode = MeasureSpec.AT_MOST;
             } else if (childDimension == LayoutParams.WRAP_CONTENT) {
                 // Child wants to determine its own size. It can't be
                 // bigger than us.
                 resultSize = size;
                 resultMode = MeasureSpec.AT_MOST;
             }
             break;
 
         // Parent asked to see how big we want to be
         case MeasureSpec.UNSPECIFIED:
             if (childDimension >= 0) {
                 // Child wants a specific size... let him have it
                 resultSize = childDimension;
                 resultMode = MeasureSpec.EXACTLY;
             } else if (childDimension == LayoutParams.FILL_PARENT) {
                 // Child wants to be our size... find out how big it should
                 // be
                 resultSize = 0;
                 resultMode = MeasureSpec.UNSPECIFIED;
             } else if (childDimension == LayoutParams.WRAP_CONTENT) {
                 // Child wants to determine its own size.... find out how
                 // big it should be
                 resultSize = 0;
                 resultMode = MeasureSpec.UNSPECIFIED;
             }
             break;
         }
         return MeasureSpec.makeMeasureSpec(resultSize, resultMode);
     }
 
     public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
         measureChildren(widthMeasureSpec, heightMeasureSpec);
     }
 
     public void addView(View v) {
         ViewGroup.LayoutParams params = v.getLayoutParams();
 
         if (null == params) {
             params = new ViewGroup.LayoutParams(mWidth, mHeight);
             v.setLayoutParams(params);
         }
 
         mChildren.add(v);
         onMeasure(mWidth, mHeight);
         onLayout(true, 0, 0, mWidth, mHeight);
         invalidate();
     }
 
     public View addView(int resourceId) {
         final LayoutInflater inflater = LayoutInflater.from(mContext);
         View v = inflater.inflate(resourceId, null);
         addView(v);
         return v;
     }
 
     public void setContentView(int resourceId) {
         addView(resourceId);
     }
 
     public void setContentView(View view) {
         addView(view);
     }
 
     public View getChildAt(int index) {
         return mChildren.get(index);
     }
 
     public View findViewById(int resourceId) {
         View view = null;
         for(View v : mChildren) {
             view = v.findViewById(resourceId);
             if(null != view) {
                 break;
             }
         }
         return view;
     }
 
     public Context getContext() {
         return mContext;
     }
 
     public int getChildCount() {
         return mChildren.size();
     }
 
     /**
      * Get supported control width.
      *
      * @param context The context.
      * @return the width.
      */
     public int getSupportedControlWidth() {
         Resources r = mContext.getResources();
 
         switch (mDevice) {
             case DEVICE_HEADSET:
                 return r.getDimensionPixelSize(R.dimen.headset_pro_control_width);
 
             case DEVICE_SMART_WATCH:
                 return r.getDimensionPixelSize(R.dimen.smart_watch_control_width);
 
             default:
                 throw new IllegalArgumentException("Bad device");
         }
     }
 
     /**
      * Get supported control height.
      *
      * @param context The context.
      * @return the height.
      */
     public int getSupportedControlHeight() {
         Resources r = mContext.getResources();
 
         switch (mDevice) {
             case DEVICE_HEADSET:
                 return r.getDimensionPixelSize(R.dimen.headset_pro_control_height);
 
             case DEVICE_SMART_WATCH:
                 return r.getDimensionPixelSize(R.dimen.smart_watch_control_height);
 
             default:
                 throw new IllegalArgumentException("Bad device");
         }
     }
 
     @Override
     public void onDestroy() {
         mBitmap.recycle();
     };
 
     @Override
     public void onStart() {
         invalidate();
     }
 
     @Override
     public void onStop() {
     }
 
     @Override
     public void onResume() {
         invalidate();
     }
 
     public void scrollTo(int x, int y) {
         mScrollX = x;
         mScrollY = y;
     }
 
     public int getScrollX() {
         return mScrollX;
     }
 
     public int getScrollY() {
         return mScrollY;
     }
 
     protected void onLayout(final boolean changed, final int l, final int t, final int r,
             final int b) {
         final int count = getChildCount();
 
         for (int i = 0; i < count; i++) {
             final View child = getChildAt(i);
             if (child.getVisibility() != View.GONE) {
                 child.layout(l, r, t, b);
             }
         }
     }
 
     public void removeAllViews() {
         mChildren.clear();
     }
 
     public boolean onInterceptTouchEvent(final ControlTouchEvent ev) {
         return false;
     }
 
     @Override
     public void onTouch(ControlTouchEvent event) {
         if(!onInterceptTouchEvent(event)) {
             super.onTouch(event);
             if(event.getAction() == Control.Intents.TOUCH_ACTION_PRESS ) {
                 mDownStartTime = event.getTimeStamp();
             }
 
             int action = 0;
             switch(event.getAction()) {
                 case Control.Intents.TOUCH_ACTION_PRESS:
                     action = MotionEvent.ACTION_DOWN;
                     break;
                 case Control.Intents.TOUCH_ACTION_RELEASE:
                     action = MotionEvent.ACTION_UP;
                     break;
             }
 
 
             MotionEvent me = MotionEvent.obtain(
                 event.getTimeStamp() - mDownStartTime,
                 event.getTimeStamp(),
                 action,
                 event.getX() + mScrollX,
                 event.getY() + mScrollY,
                 0
             );
             onTouchEvent(me);
         }
     }
 
     public boolean onTouchEvent(MotionEvent ev) {
         for(View v : mChildren) {
             v.dispatchTouchEvent(ev);
         }
         return false;
     }
 
     /**
      * Move the scrolled position of your view. This will cause a call to
      * {@link #onScrollChanged(int, int, int, int)} and the view will be
      * invalidated.
      * @param x the amount of pixels to scroll by horizontally
      * @param y the amount of pixels to scroll by vertically
      */
     public void scrollBy(int x, int y) {
         scrollTo(mScrollX + x, mScrollY + y);
     }
 
     public int getWidth() {
         return mWidth;
     }
 
     public int getHeight() {
         return mHeight;
     }
 
     public void computeScroll() {
 
     }
 
     public void postInvalidate() {
         invalidate();
     }
 
     public void invalidate() {
         mCanvas.drawColor(Color.TRANSPARENT, Mode.CLEAR);
         for(View v : mChildren) {
             mCanvas.save();
             mCanvas.translate(v.getLeft() - mScrollX, v.getTop() - mScrollY);
             v.draw(mCanvas);
             mCanvas.restore();
         }
         showBitmap(mBitmap);
     }
 }
