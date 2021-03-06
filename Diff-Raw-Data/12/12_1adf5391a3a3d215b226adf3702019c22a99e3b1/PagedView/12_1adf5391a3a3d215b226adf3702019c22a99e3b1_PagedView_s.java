 /*
  * Copyright (C) 2010 The Android Open Source Project
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.android.launcher2;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import android.content.Context;
 import android.content.res.TypedArray;
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.graphics.Rect;
 import android.os.Parcel;
 import android.os.Parcelable;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.ActionMode;
 import android.view.MotionEvent;
 import android.view.VelocityTracker;
 import android.view.View;
 import android.view.ViewConfiguration;
 import android.view.ViewGroup;
 import android.view.ViewParent;
 import android.view.animation.Animation;
 import android.view.animation.Animation.AnimationListener;
 import android.view.animation.AnimationUtils;
 import android.widget.Checkable;
 import android.widget.LinearLayout;
 import android.widget.Scroller;
 
 import com.android.launcher.R;
 
 /**
  * An abstraction of the original Workspace which supports browsing through a
  * sequential list of "pages"
  */
 public abstract class PagedView extends ViewGroup {
     private static final String TAG = "PagedView";
     protected static final int INVALID_PAGE = -1;
 
     // the min drag distance for a fling to register, to prevent random page shifts
     private static final int MIN_LENGTH_FOR_FLING = 50;
 
     private static final int PAGE_SNAP_ANIMATION_DURATION = 1000;
     protected static final float NANOTIME_DIV = 1000000000.0f;
 
     // the velocity at which a fling gesture will cause us to snap to the next page
     protected int mSnapVelocity = 500;
 
     protected float mSmoothingTime;
     protected float mTouchX;
 
     protected boolean mFirstLayout = true;
 
     protected int mCurrentPage;
     protected int mNextPage = INVALID_PAGE;
     protected Scroller mScroller;
     private VelocityTracker mVelocityTracker;
 
     private float mDownMotionX;
     private float mLastMotionX;
     private float mLastMotionY;
     private int mLastScreenCenter = -1;
 
     protected final static int TOUCH_STATE_REST = 0;
     protected final static int TOUCH_STATE_SCROLLING = 1;
     protected final static int TOUCH_STATE_PREV_PAGE = 2;
     protected final static int TOUCH_STATE_NEXT_PAGE = 3;
     protected final static float ALPHA_QUANTIZE_LEVEL = 0.0001f;
 
     protected int mTouchState = TOUCH_STATE_REST;
 
     protected OnLongClickListener mLongClickListener;
 
     private boolean mAllowLongPress = true;
 
     private int mTouchSlop;
     private int mPagingTouchSlop;
     private int mMaximumVelocity;
     protected int mPageSpacing;
     protected int mPageLayoutPaddingTop;
     protected int mPageLayoutPaddingBottom;
     protected int mPageLayoutPaddingLeft;
     protected int mPageLayoutPaddingRight;
     protected int mCellCountX;
     protected int mCellCountY;
 
     protected static final int INVALID_POINTER = -1;
 
     protected int mActivePointerId = INVALID_POINTER;
 
     private PageSwitchListener mPageSwitchListener;
 
     private ArrayList<Boolean> mDirtyPageContent;
     private boolean mDirtyPageAlpha;
 
     // choice modes
     protected static final int CHOICE_MODE_NONE = 0;
     protected static final int CHOICE_MODE_SINGLE = 1;
     // Multiple selection mode is not supported by all Launcher actions atm
     protected static final int CHOICE_MODE_MULTIPLE = 2;
 
     protected int mChoiceMode;
     private ActionMode mActionMode;
 
     protected PagedViewIconCache mPageViewIconCache;
 
     // If true, syncPages and syncPageItems will be called to refresh pages
     protected boolean mContentIsRefreshable = true;
 
     // If true, modify alpha of neighboring pages as user scrolls left/right
     protected boolean mFadeInAdjacentScreens = true;
 
     // It true, use a different slop parameter (pagingTouchSlop = 2 * touchSlop) for deciding
     // to switch to a new page
     protected boolean mUsePagingTouchSlop = true;
 
     // If true, the subclass should directly update mScrollX itself in its computeScroll method
     // (SmoothPagedView does this)
     protected boolean mDeferScrollUpdate = false;
 
     protected boolean mIsPageMoving = false;
 
     /**
      * Simple cache mechanism for PagedViewIcon outlines.
      */
     class PagedViewIconCache {
         private final HashMap<Object, Bitmap> iconOutlineCache = new HashMap<Object, Bitmap>();
 
         public void clear() {
             iconOutlineCache.clear();
         }
         public void addOutline(Object key, Bitmap b) {
             iconOutlineCache.put(key, b);
         }
         public void removeOutline(Object key) {
             if (iconOutlineCache.containsKey(key)) {
                 iconOutlineCache.remove(key);
             }
         }
         public Bitmap getOutline(Object key) {
             return iconOutlineCache.get(key);
         }
     }
 
     public interface PageSwitchListener {
         void onPageSwitch(View newPage, int newPageIndex);
     }
 
     public PagedView(Context context) {
         this(context, null);
     }
 
     public PagedView(Context context, AttributeSet attrs) {
         this(context, attrs, 0);
     }
 
     public PagedView(Context context, AttributeSet attrs, int defStyle) {
         super(context, attrs, defStyle);
         mChoiceMode = CHOICE_MODE_NONE;
 
         TypedArray a = context.obtainStyledAttributes(attrs,
                 R.styleable.PagedView, defStyle, 0);
         mPageSpacing = a.getDimensionPixelSize(R.styleable.PagedView_pageSpacing, 0);
         mPageLayoutPaddingTop = a.getDimensionPixelSize(
                 R.styleable.PagedView_pageLayoutPaddingTop, 10);
         mPageLayoutPaddingBottom = a.getDimensionPixelSize(
                 R.styleable.PagedView_pageLayoutPaddingBottom, 10);
         mPageLayoutPaddingLeft = a.getDimensionPixelSize(
                 R.styleable.PagedView_pageLayoutPaddingLeft, 10);
         mPageLayoutPaddingRight = a.getDimensionPixelSize(
                 R.styleable.PagedView_pageLayoutPaddingRight, 10);
         a.recycle();
 
         setHapticFeedbackEnabled(false);
         init();
     }
 
     /**
      * Initializes various states for this workspace.
      */
     protected void init() {
         mDirtyPageContent = new ArrayList<Boolean>();
         mDirtyPageContent.ensureCapacity(32);
         mPageViewIconCache = new PagedViewIconCache();
         mScroller = new Scroller(getContext());
         mCurrentPage = 0;
 
         final ViewConfiguration configuration = ViewConfiguration.get(getContext());
         mTouchSlop = configuration.getScaledTouchSlop();
         mPagingTouchSlop = configuration.getScaledPagingTouchSlop();
         mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
     }
 
     public void setPageSwitchListener(PageSwitchListener pageSwitchListener) {
         mPageSwitchListener = pageSwitchListener;
         if (mPageSwitchListener != null) {
             mPageSwitchListener.onPageSwitch(getPageAt(mCurrentPage), mCurrentPage);
         }
     }
 
     /**
      * Returns the index of the currently displayed page.
      *
      * @return The index of the currently displayed page.
      */
     int getCurrentPage() {
         return mCurrentPage;
     }
 
     int getPageCount() {
         return getChildCount();
     }
 
     View getPageAt(int index) {
         return getChildAt(index);
     }
 
     int getScrollWidth() {
         return getWidth();
     }
 
     /**
      * Sets the current page.
      */
     void setCurrentPage(int currentPage) {
         if (!mScroller.isFinished()) mScroller.abortAnimation();
         if (getChildCount() == 0) return;
 
         mCurrentPage = Math.max(0, Math.min(currentPage, getPageCount() - 1));
         int newX = getChildOffset(mCurrentPage) - getRelativeChildOffset(mCurrentPage);
         scrollTo(newX, 0);
         mScroller.setFinalX(newX);
 
         invalidate();
         notifyPageSwitchListener();
     }
 
     protected void notifyPageSwitchListener() {
         if (mPageSwitchListener != null) {
             mPageSwitchListener.onPageSwitch(getPageAt(mCurrentPage), mCurrentPage);
         }
     }
 
     private void pageBeginMoving() {
         mIsPageMoving = true;
         onPageBeginMoving();
     }
 
     private void pageEndMoving() {
         onPageEndMoving();
         mIsPageMoving = false;
     }
 
     // a method that subclasses can override to add behavior
     protected void onPageBeginMoving() {
     }
 
     // a method that subclasses can override to add behavior
     protected void onPageEndMoving() {
     }
 
     /**
      * Registers the specified listener on each page contained in this workspace.
      *
      * @param l The listener used to respond to long clicks.
      */
     @Override
     public void setOnLongClickListener(OnLongClickListener l) {
         mLongClickListener = l;
         final int count = getPageCount();
         for (int i = 0; i < count; i++) {
             getPageAt(i).setOnLongClickListener(l);
         }
     }
 
     @Override
     public void scrollTo(int x, int y) {
         super.scrollTo(x, y);
         mTouchX = x;
         mSmoothingTime = System.nanoTime() / NANOTIME_DIV;
     }
 
     // we moved this functionality to a helper function so SmoothPagedView can reuse it
     protected boolean computeScrollHelper() {
         if (mScroller.computeScrollOffset()) {
             mDirtyPageAlpha = true;
             scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
             invalidate();
             return true;
         } else if (mNextPage != INVALID_PAGE) {
             mDirtyPageAlpha = true;
             mCurrentPage = Math.max(0, Math.min(mNextPage, getPageCount() - 1));
             mNextPage = INVALID_PAGE;
             notifyPageSwitchListener();
             pageEndMoving();
             return true;
         }
         return false;
     }
 
     @Override
     public void computeScroll() {
         computeScrollHelper();
     }
 
     @Override
     protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
         final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
         final int widthSize = MeasureSpec.getSize(widthMeasureSpec);
         if (widthMode != MeasureSpec.EXACTLY) {
             throw new IllegalStateException("Workspace can only be used in EXACTLY mode.");
         }
 
         final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
         final int heightSize = MeasureSpec.getSize(heightMeasureSpec);
         if (heightMode != MeasureSpec.EXACTLY) {
             throw new IllegalStateException("Workspace can only be used in EXACTLY mode.");
         }
 
         // The children are given the same width and height as the workspace
         // unless they were set to WRAP_CONTENT
         final int childCount = getChildCount();
         for (int i = 0; i < childCount; i++) {
             // disallowing padding in paged view (just pass 0)
             final View child = getChildAt(i);
             final LayoutParams lp = (LayoutParams) child.getLayoutParams();
 
             int childWidthMode;
             if (lp.width == LayoutParams.WRAP_CONTENT) {
                 childWidthMode = MeasureSpec.AT_MOST;
             } else {
                 childWidthMode = MeasureSpec.EXACTLY;
             }
 
             int childHeightMode;
             if (lp.height == LayoutParams.WRAP_CONTENT) {
                 childHeightMode = MeasureSpec.AT_MOST;
             } else {
                 childHeightMode = MeasureSpec.EXACTLY;
             }
 
             final int childWidthMeasureSpec =
                 MeasureSpec.makeMeasureSpec(widthSize, childWidthMode);
             final int childHeightMeasureSpec =
                 MeasureSpec.makeMeasureSpec(heightSize, childHeightMode);
 
             child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
         }
 
         setMeasuredDimension(widthSize, heightSize);
     }
 
     @Override
     protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
         if (mFirstLayout && mCurrentPage >= 0 && mCurrentPage < getChildCount()) {
             setHorizontalScrollBarEnabled(false);
             int newX = getChildOffset(mCurrentPage) - getRelativeChildOffset(mCurrentPage);
             scrollTo(newX, 0);
             mScroller.setFinalX(newX);
             setHorizontalScrollBarEnabled(true);
             mFirstLayout = false;
         }
 
         final int childCount = getChildCount();
         int childLeft = 0;
         if (childCount > 0) {
             childLeft = getRelativeChildOffset(0);
         }
 
         for (int i = 0; i < childCount; i++) {
             final View child = getChildAt(i);
             if (child.getVisibility() != View.GONE) {
                 final int childWidth = child.getMeasuredWidth();
                 final int childHeight = (getMeasuredHeight() - child.getMeasuredHeight()) / 2;
                 child.layout(childLeft, childHeight,
                         childLeft + childWidth, childHeight + child.getMeasuredHeight());
                 childLeft += childWidth + mPageSpacing;
             }
         }
     }
 
     protected void updateAdjacentPagesAlpha() {
         if (mFadeInAdjacentScreens) {
             if (mDirtyPageAlpha || (mTouchState == TOUCH_STATE_SCROLLING) || !mScroller.isFinished()) {
                 int halfScreenSize = getMeasuredWidth() / 2;
                 int screenCenter = mScrollX + halfScreenSize;
                 final int childCount = getChildCount();
                 for (int i = 0; i < childCount; ++i) {
                     View layout = (View) getChildAt(i);
                     int childWidth = layout.getMeasuredWidth();
                     int halfChildWidth = (childWidth / 2);
                     int childCenter = getChildOffset(i) + halfChildWidth;
 
                     // On the first layout, we may not have a width nor a proper offset, so for now
                     // we should just assume full page width (and calculate the offset according to
                     // that).
                     if (childWidth <= 0) {
                         childWidth = getMeasuredWidth();
                         childCenter = (i * childWidth) + (childWidth / 2);
                     }
 
                     int d = halfChildWidth;
                     int distanceFromScreenCenter = childCenter - screenCenter;
                     if (distanceFromScreenCenter > 0) {
                         if (i > 0) {
                             d += getChildAt(i - 1).getMeasuredWidth() / 2;
                         }
                     } else {
                         if (i < childCount - 1) {
                             d += getChildAt(i + 1).getMeasuredWidth() / 2;
                         }
                     }
                     d += mPageSpacing;
 
                     // Preventing potential divide-by-zero
                     d = Math.max(1, d);
 
                     float dimAlpha = (float) (Math.abs(distanceFromScreenCenter)) / d;
                     dimAlpha = Math.max(0.0f, Math.min(1.0f, (dimAlpha * dimAlpha)));
                     float alpha = 1.0f - dimAlpha;
 
                     if (alpha < ALPHA_QUANTIZE_LEVEL) {
                         alpha = 0.0f;
                     } else if (alpha > 1.0f - ALPHA_QUANTIZE_LEVEL) {
                         alpha = 1.0f;
                     }
 
                     if (Float.compare(alpha, layout.getAlpha()) != 0) {
                         layout.setAlpha(alpha);
                     }
                 }
                 mDirtyPageAlpha = false;
             }
         }
     }
 
     protected void screenScrolled(int screenCenter) {
     }
 
     @Override
     protected void dispatchDraw(Canvas canvas) {
         int halfScreenSize = getMeasuredWidth() / 2;
         int screenCenter = mScrollX + halfScreenSize;
 
         if (screenCenter != mLastScreenCenter) {
             screenScrolled(screenCenter);
             updateAdjacentPagesAlpha();
             mLastScreenCenter = screenCenter;
         }
 
         // Find out which screens are visible; as an optimization we only call draw on them
         // As an optimization, this code assumes that all pages have the same width as the 0th
         // page.
         final int pageCount = getChildCount();
         if (pageCount > 0) {
             final int pageWidth = getChildAt(0).getMeasuredWidth();
             final int screenWidth = getMeasuredWidth();
             int x = getRelativeChildOffset(0) + pageWidth;
             int leftScreen = 0;
             int rightScreen = 0;
             while (x <= mScrollX) {
                 leftScreen++;
                 x += pageWidth + mPageSpacing;
                 // replace above line with this if you don't assume all pages have same width as 0th
                 // page:
                 // x += getChildAt(leftScreen).getMeasuredWidth();
             }
             rightScreen = leftScreen;
             while (x < mScrollX + screenWidth) {
                 rightScreen++;
                 x += pageWidth + mPageSpacing;
                 // replace above line with this if you don't assume all pages have same width as 0th
                 // page:
                 //if (rightScreen < pageCount) {
                 //    x += getChildAt(rightScreen).getMeasuredWidth();
                 //}
             }
             rightScreen = Math.min(getChildCount() - 1, rightScreen);
 
             final long drawingTime = getDrawingTime();
             for (int i = leftScreen; i <= rightScreen; i++) {
                 drawChild(canvas, getChildAt(i), drawingTime);
             }
         }
     }
 
     @Override
     public boolean requestChildRectangleOnScreen(View child, Rect rectangle, boolean immediate) {
         int page = indexOfChild(child);
         if (page != mCurrentPage || !mScroller.isFinished()) {
             snapToPage(page);
             return true;
         }
         return false;
     }
 
     @Override
     protected boolean onRequestFocusInDescendants(int direction, Rect previouslyFocusedRect) {
         int focusablePage;
         if (mNextPage != INVALID_PAGE) {
             focusablePage = mNextPage;
         } else {
             focusablePage = mCurrentPage;
         }
         View v = getPageAt(focusablePage);
         if (v != null) {
             v.requestFocus(direction, previouslyFocusedRect);
         }
         return false;
     }
 
     @Override
     public boolean dispatchUnhandledMove(View focused, int direction) {
         if (direction == View.FOCUS_LEFT) {
             if (getCurrentPage() > 0) {
                 snapToPage(getCurrentPage() - 1);
                 return true;
             }
         } else if (direction == View.FOCUS_RIGHT) {
             if (getCurrentPage() < getPageCount() - 1) {
                 snapToPage(getCurrentPage() + 1);
                 return true;
             }
         }
         return super.dispatchUnhandledMove(focused, direction);
     }
 
     @Override
     public void addFocusables(ArrayList<View> views, int direction, int focusableMode) {
         if (mCurrentPage >= 0 && mCurrentPage < getPageCount()) {
             getPageAt(mCurrentPage).addFocusables(views, direction);
         }
         if (direction == View.FOCUS_LEFT) {
             if (mCurrentPage > 0) {
                 getPageAt(mCurrentPage - 1).addFocusables(views, direction);
             }
         } else if (direction == View.FOCUS_RIGHT){
             if (mCurrentPage < getPageCount() - 1) {
                 getPageAt(mCurrentPage + 1).addFocusables(views, direction);
             }
         }
     }
 
     /**
      * If one of our descendant views decides that it could be focused now, only
      * pass that along if it's on the current page.
      *
      * This happens when live folders requery, and if they're off page, they
      * end up calling requestFocus, which pulls it on page.
      */
     @Override
     public void focusableViewAvailable(View focused) {
         View current = getPageAt(mCurrentPage);
         View v = focused;
         while (true) {
             if (v == current) {
                 super.focusableViewAvailable(focused);
                 return;
             }
             if (v == this) {
                 return;
             }
             ViewParent parent = v.getParent();
             if (parent instanceof View) {
                 v = (View)v.getParent();
             } else {
                 return;
             }
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {
         if (disallowIntercept) {
             // We need to make sure to cancel our long press if
             // a scrollable widget takes over touch events
             final View currentPage = getChildAt(mCurrentPage);
             currentPage.cancelLongPress();
         }
         super.requestDisallowInterceptTouchEvent(disallowIntercept);
     }
 
     @Override
     public boolean onInterceptTouchEvent(MotionEvent ev) {
         /*
          * This method JUST determines whether we want to intercept the motion.
          * If we return true, onTouchEvent will be called and we do the actual
          * scrolling there.
          */
 
         /*
          * Shortcut the most recurring case: the user is in the dragging
          * state and he is moving his finger.  We want to intercept this
          * motion.
          */
         final int action = ev.getAction();
         if ((action == MotionEvent.ACTION_MOVE) &&
                 (mTouchState == TOUCH_STATE_SCROLLING)) {
             return true;
         }
 
         switch (action & MotionEvent.ACTION_MASK) {
             case MotionEvent.ACTION_MOVE: {
                 /*
                  * mIsBeingDragged == false, otherwise the shortcut would have caught it. Check
                  * whether the user has moved far enough from his original down touch.
                  */
                 if (mActivePointerId != INVALID_POINTER) {
                     determineScrollingStart(ev);
                     break;
                 }
                 // if mActivePointerId is INVALID_POINTER, then we must have missed an ACTION_DOWN
                 // event. in that case, treat the first occurence of a move event as a ACTION_DOWN
                 // i.e. fall through to the next case (don't break)
                 // (We sometimes miss ACTION_DOWN events in Workspace because it ignores all events
                 // while it's small- this was causing a crash before we checked for INVALID_POINTER)
             }
 
             case MotionEvent.ACTION_DOWN: {
                 final float x = ev.getX();
                 final float y = ev.getY();
                 // Remember location of down touch
                 mDownMotionX = x;
                 mLastMotionX = x;
                 mLastMotionY = y;
                 mActivePointerId = ev.getPointerId(0);
                 mAllowLongPress = true;
 
                 /*
                  * If being flinged and user touches the screen, initiate drag;
                  * otherwise don't.  mScroller.isFinished should be false when
                  * being flinged.
                  */
                 final int xDist = (mScroller.getFinalX() - mScroller.getCurrX());
                 final boolean finishedScrolling = (mScroller.isFinished() || xDist < mTouchSlop);
                 if (finishedScrolling) {
                     mTouchState = TOUCH_STATE_REST;
                     mScroller.abortAnimation();
                 } else {
                     mTouchState = TOUCH_STATE_SCROLLING;
                 }
 
                 // check if this can be the beginning of a tap on the side of the pages
                 // to scroll the current page
                 if ((mTouchState != TOUCH_STATE_PREV_PAGE) && !handlePagingClicks() &&
                         (mTouchState != TOUCH_STATE_NEXT_PAGE)) {
                     if (getChildCount() > 0) {
                         int width = getMeasuredWidth();
                         int offset = getRelativeChildOffset(mCurrentPage);
                         if (x < offset - mPageSpacing) {
                             mTouchState = TOUCH_STATE_PREV_PAGE;
                         } else if (x > (width - offset + mPageSpacing)) {
                             mTouchState = TOUCH_STATE_NEXT_PAGE;
                         }
                     }
                 }
                 break;
             }
 
             case MotionEvent.ACTION_CANCEL:
             case MotionEvent.ACTION_UP:
                 mTouchState = TOUCH_STATE_REST;
                 mAllowLongPress = false;
                 mActivePointerId = INVALID_POINTER;
                 break;
 
             case MotionEvent.ACTION_POINTER_UP:
                 onSecondaryPointerUp(ev);
                 break;
         }
 
         /*
          * The only time we want to intercept motion events is if we are in the
          * drag mode.
          */
         return mTouchState != TOUCH_STATE_REST;
     }
 
     protected void animateClickFeedback(View v, final Runnable r) {
         // animate the view slightly to show click feedback running some logic after it is "pressed"
         Animation anim = AnimationUtils.loadAnimation(getContext(), 
                 R.anim.paged_view_click_feedback);
         anim.setAnimationListener(new AnimationListener() {
             @Override
             public void onAnimationStart(Animation animation) {}
             @Override
             public void onAnimationRepeat(Animation animation) {
                 r.run();
             }
             @Override
             public void onAnimationEnd(Animation animation) {}
         });
         v.startAnimation(anim);
     }
 
     /*
      * Determines if we should change the touch state to start scrolling after the
      * user moves their touch point too far.
      */
    private void determineScrollingStart(MotionEvent ev) {
         /*
          * Locally do absolute value. mLastMotionX is set to the y value
          * of the down event.
          */
         final int pointerIndex = ev.findPointerIndex(mActivePointerId);
         final float x = ev.getX(pointerIndex);
         final float y = ev.getY(pointerIndex);
         final int xDiff = (int) Math.abs(x - mLastMotionX);
         final int yDiff = (int) Math.abs(y - mLastMotionY);
 
         final int touchSlop = mTouchSlop;
         boolean xPaged = xDiff > mPagingTouchSlop;
         boolean xMoved = xDiff > touchSlop;
         boolean yMoved = yDiff > touchSlop;
 
         if (xMoved || yMoved) {
             if (mUsePagingTouchSlop ? xPaged : xMoved) {
                 // Scroll if the user moved far enough along the X axis
                 mTouchState = TOUCH_STATE_SCROLLING;
                 mLastMotionX = x;
                 mTouchX = mScrollX;
                 mSmoothingTime = System.nanoTime() / NANOTIME_DIV;
                 pageBeginMoving();
             }
             // Either way, cancel any pending longpress
             if (mAllowLongPress) {
                 mAllowLongPress = false;
                 // Try canceling the long press. It could also have been scheduled
                 // by a distant descendant, so use the mAllowLongPress flag to block
                 // everything
                 final View currentPage = getPageAt(mCurrentPage);
                 currentPage.cancelLongPress();
             }
         }
     }
 
     protected boolean handlePagingClicks() {
         return false;
     }
 
     @Override
     public boolean onTouchEvent(MotionEvent ev) {
         acquireVelocityTrackerAndAddMovement(ev);
 
         final int action = ev.getAction();
 
         switch (action & MotionEvent.ACTION_MASK) {
         case MotionEvent.ACTION_DOWN:
             /*
              * If being flinged and user touches, stop the fling. isFinished
              * will be false if being flinged.
              */
             if (!mScroller.isFinished()) {
                 mScroller.abortAnimation();
             }
 
             // Remember where the motion event started
             mDownMotionX = mLastMotionX = ev.getX();
             mActivePointerId = ev.getPointerId(0);
             if (mTouchState == TOUCH_STATE_SCROLLING) {
                 pageBeginMoving();
             }
             break;
 
         case MotionEvent.ACTION_MOVE:
             if (mTouchState == TOUCH_STATE_SCROLLING) {
                 // Scroll to follow the motion event
                 final int pointerIndex = ev.findPointerIndex(mActivePointerId);
                 final float x = ev.getX(pointerIndex);
                 final int deltaX = (int) (mLastMotionX - x);
                 mLastMotionX = x;
 
                 int sx = getScrollX();
                 if (deltaX < 0) {
                     if (sx > 0) {
                         mTouchX += Math.max(-mTouchX, deltaX);
                         mSmoothingTime = System.nanoTime() / NANOTIME_DIV;
                         if (!mDeferScrollUpdate) {
                             scrollBy(Math.max(-sx, deltaX), 0);
                         } else {
                             // This will trigger a call to computeScroll() on next drawChild() call
                             invalidate();
                         }
                     }
                 } else if (deltaX > 0) {
                     final int lastChildIndex = getChildCount() - 1;
                     final int availableToScroll = getChildOffset(lastChildIndex) -
                         getRelativeChildOffset(lastChildIndex) - sx;
                     if (availableToScroll > 0) {
                         mTouchX += Math.min(availableToScroll, deltaX);
                         mSmoothingTime = System.nanoTime() / NANOTIME_DIV;
                         if (!mDeferScrollUpdate) {
                             scrollBy(Math.min(availableToScroll, deltaX), 0);
                         } else {
                             // This will trigger a call to computeScroll() on next drawChild() call
                             invalidate();
                         }
                     }
                 } else {
                     awakenScrollBars();
                 }
             } else {
                 determineScrollingStart(ev);
             }
             break;
 
         case MotionEvent.ACTION_UP:
             if (mTouchState == TOUCH_STATE_SCROLLING) {
                 final int activePointerId = mActivePointerId;
                 final int pointerIndex = ev.findPointerIndex(activePointerId);
                 final float x = ev.getX(pointerIndex);
                 final VelocityTracker velocityTracker = mVelocityTracker;
                 velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
                 int velocityX = (int) velocityTracker.getXVelocity(activePointerId);
                 boolean isfling = Math.abs(mDownMotionX - x) > MIN_LENGTH_FOR_FLING;
 
                 final int snapVelocity = mSnapVelocity;
                 if (isfling && velocityX > snapVelocity && mCurrentPage > 0) {
                     snapToPageWithVelocity(mCurrentPage - 1, velocityX);
                 } else if (isfling && velocityX < -snapVelocity &&
                         mCurrentPage < getChildCount() - 1) {
                     snapToPageWithVelocity(mCurrentPage + 1, velocityX);
                 } else {
                     snapToDestination();
                 }
             } else if (mTouchState == TOUCH_STATE_PREV_PAGE && !handlePagingClicks()) {
                 // at this point we have not moved beyond the touch slop
                 // (otherwise mTouchState would be TOUCH_STATE_SCROLLING), so
                 // we can just page
                 int nextPage = Math.max(0, mCurrentPage - 1);
                 if (nextPage != mCurrentPage) {
                     snapToPage(nextPage);
                 } else {
                     snapToDestination();
                 }
             } else if (mTouchState == TOUCH_STATE_NEXT_PAGE && !handlePagingClicks()) {
                 // at this point we have not moved beyond the touch slop
                 // (otherwise mTouchState would be TOUCH_STATE_SCROLLING), so
                 // we can just page
                 int nextPage = Math.min(getChildCount() - 1, mCurrentPage + 1);
                 if (nextPage != mCurrentPage) {
                     snapToPage(nextPage);
                 } else {
                     snapToDestination();
                 }
             }
             mTouchState = TOUCH_STATE_REST;
             mActivePointerId = INVALID_POINTER;
             releaseVelocityTracker();
             break;
 
         case MotionEvent.ACTION_CANCEL:
             if (mTouchState == TOUCH_STATE_SCROLLING) {
                 snapToDestination();
             }
             mTouchState = TOUCH_STATE_REST;
             mActivePointerId = INVALID_POINTER;
             releaseVelocityTracker();
             break;
 
         case MotionEvent.ACTION_POINTER_UP:
             onSecondaryPointerUp(ev);
             break;
         }
 
         return true;
     }
 
     private void acquireVelocityTrackerAndAddMovement(MotionEvent ev) {
         if (mVelocityTracker == null) {
             mVelocityTracker = VelocityTracker.obtain();
         }
         mVelocityTracker.addMovement(ev);
     }
 
     private void releaseVelocityTracker() {
         if (mVelocityTracker != null) {
             mVelocityTracker.recycle();
             mVelocityTracker = null;
         }
     }
 
     private void onSecondaryPointerUp(MotionEvent ev) {
         final int pointerIndex = (ev.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >>
                 MotionEvent.ACTION_POINTER_INDEX_SHIFT;
         final int pointerId = ev.getPointerId(pointerIndex);
         if (pointerId == mActivePointerId) {
             // This was our active pointer going up. Choose a new
             // active pointer and adjust accordingly.
             // TODO: Make this decision more intelligent.
             final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
             mLastMotionX = mDownMotionX = ev.getX(newPointerIndex);
             mLastMotionY = ev.getY(newPointerIndex);
             mActivePointerId = ev.getPointerId(newPointerIndex);
             if (mVelocityTracker != null) {
                 mVelocityTracker.clear();
             }
         }
     }
 
     @Override
     public void requestChildFocus(View child, View focused) {
         super.requestChildFocus(child, focused);
         int page = indexOfChild(child);
         if (page >= 0 && !isInTouchMode()) {
             snapToPage(page);
         }
     }
 
     protected int getChildIndexForRelativeOffset(int relativeOffset) {
         final int childCount = getChildCount();
         int left;
         int right;
         for (int i = 0; i < childCount; ++i) {
             left = getRelativeChildOffset(i);
             right = (left + getChildAt(i).getMeasuredWidth());
             if (left <= relativeOffset && relativeOffset <= right) {
                 return i;
             }
         }
         return -1;
     }
 
     protected int getRelativeChildOffset(int index) {
         return (getMeasuredWidth() - getChildAt(index).getMeasuredWidth()) / 2;
     }
 
     protected int getChildOffset(int index) {
         if (getChildCount() == 0)
             return 0;
 
         int offset = getRelativeChildOffset(0);
         for (int i = 0; i < index; ++i) {
             offset += getChildAt(i).getMeasuredWidth() + mPageSpacing;
         }
         return offset;
     }
 
     int getPageNearestToCenterOfScreen() {
         int minDistanceFromScreenCenter = getMeasuredWidth();
         int minDistanceFromScreenCenterIndex = -1;
         int screenCenter = mScrollX + (getMeasuredWidth() / 2);
         final int childCount = getChildCount();
         for (int i = 0; i < childCount; ++i) {
             View layout = (View) getChildAt(i);
             int childWidth = layout.getMeasuredWidth();
             int halfChildWidth = (childWidth / 2);
             int childCenter = getChildOffset(i) + halfChildWidth;
             int distanceFromScreenCenter = Math.abs(childCenter - screenCenter);
             if (distanceFromScreenCenter < minDistanceFromScreenCenter) {
                 minDistanceFromScreenCenter = distanceFromScreenCenter;
                 minDistanceFromScreenCenterIndex = i;
             }
         }
         return minDistanceFromScreenCenterIndex;
     }
 
     protected void snapToDestination() {
         snapToPage(getPageNearestToCenterOfScreen(), PAGE_SNAP_ANIMATION_DURATION);
     }
 
     protected void snapToPageWithVelocity(int whichPage, int velocity) {
         // We ignore velocity in this implementation, but children (e.g. SmoothPagedView)
         // can use it
         snapToPage(whichPage);
     }
 
     protected void snapToPage(int whichPage) {
         snapToPage(whichPage, PAGE_SNAP_ANIMATION_DURATION);
     }
 
     protected void snapToPage(int whichPage, int duration) {
         whichPage = Math.max(0, Math.min(whichPage, getPageCount() - 1));
 
         int newX = getChildOffset(whichPage) - getRelativeChildOffset(whichPage);
         final int sX = getScrollX();
         final int delta = newX - sX;
         snapToPage(whichPage, delta, duration);
     }
 
     protected void snapToPage(int whichPage, int delta, int duration) {
         mNextPage = whichPage;
 
         View focusedChild = getFocusedChild();
         if (focusedChild != null && whichPage != mCurrentPage &&
                 focusedChild == getChildAt(mCurrentPage)) {
             focusedChild.clearFocus();
         }
 
         pageBeginMoving();
         awakenScrollBars(duration);
         if (duration == 0) {
             duration = Math.abs(delta);
         }
 
         if (!mScroller.isFinished()) mScroller.abortAnimation();
         mScroller.startScroll(getScrollX(), 0, delta, 0, duration);
 
         // only load some associated pages
         loadAssociatedPages(mNextPage);
         notifyPageSwitchListener();
         invalidate();
     }
 
     @Override
     protected Parcelable onSaveInstanceState() {
         final SavedState state = new SavedState(super.onSaveInstanceState());
         state.currentPage = mCurrentPage;
         return state;
     }
 
     @Override
     protected void onRestoreInstanceState(Parcelable state) {
         SavedState savedState = (SavedState) state;
         super.onRestoreInstanceState(savedState.getSuperState());
         if (savedState.currentPage != -1) {
             mCurrentPage = savedState.currentPage;
         }
     }
 
     public void scrollLeft() {
         if (mScroller.isFinished()) {
             if (mCurrentPage > 0) snapToPage(mCurrentPage - 1);
         } else {
             if (mNextPage > 0) snapToPage(mNextPage - 1);
         }
     }
 
     public void scrollRight() {
         if (mScroller.isFinished()) {
             if (mCurrentPage < getChildCount() -1) snapToPage(mCurrentPage + 1);
         } else {
             if (mNextPage < getChildCount() -1) snapToPage(mNextPage + 1);
         }
     }
 
     public int getPageForView(View v) {
         int result = -1;
         if (v != null) {
             ViewParent vp = v.getParent();
             int count = getChildCount();
             for (int i = 0; i < count; i++) {
                 if (vp == getChildAt(i)) {
                     return i;
                 }
             }
         }
         return result;
     }
 
     /**
      * @return True is long presses are still allowed for the current touch
      */
     public boolean allowLongPress() {
         return mAllowLongPress;
     }
 
     /**
      * Set true to allow long-press events to be triggered, usually checked by
      * {@link Launcher} to accept or block dpad-initiated long-presses.
      */
     public void setAllowLongPress(boolean allowLongPress) {
         mAllowLongPress = allowLongPress;
     }
 
     public static class SavedState extends BaseSavedState {
         int currentPage = -1;
 
         SavedState(Parcelable superState) {
             super(superState);
         }
 
         private SavedState(Parcel in) {
             super(in);
             currentPage = in.readInt();
         }
 
         @Override
         public void writeToParcel(Parcel out, int flags) {
             super.writeToParcel(out, flags);
             out.writeInt(currentPage);
         }
 
         public static final Parcelable.Creator<SavedState> CREATOR =
                 new Parcelable.Creator<SavedState>() {
             public SavedState createFromParcel(Parcel in) {
                 return new SavedState(in);
             }
 
             public SavedState[] newArray(int size) {
                 return new SavedState[size];
             }
         };
     }
 
     public void loadAssociatedPages(int page) {
         if (mContentIsRefreshable) {
             final int count = getChildCount();
             if (page < count) {
                 int lowerPageBound = getAssociatedLowerPageBound(page);
                 int upperPageBound = getAssociatedUpperPageBound(page);
                 for (int i = 0; i < count; ++i) {
                     final ViewGroup layout = (ViewGroup) getChildAt(i);
                     final int childCount = layout.getChildCount();
                     if (lowerPageBound <= i && i <= upperPageBound) {
                         if (mDirtyPageContent.get(i)) {
                             syncPageItems(i);
                             mDirtyPageContent.set(i, false);
                         }
                     } else {
                         if (childCount > 0) {
                             layout.removeAllViews();
                         }
                         mDirtyPageContent.set(i, true);
                     }
                 }
             }
         }
     }
 
     protected int getAssociatedLowerPageBound(int page) {
         return Math.max(0, page - 1);
     }
     protected int getAssociatedUpperPageBound(int page) {
         final int count = getChildCount();
         return Math.min(page + 1, count - 1);
     }
 
     protected void startChoiceMode(int mode, ActionMode.Callback callback) {
         if (isChoiceMode(CHOICE_MODE_NONE)) {
             mChoiceMode = mode;
             mActionMode = startActionMode(callback);
         }
     }
 
     public void endChoiceMode() {
         if (!isChoiceMode(CHOICE_MODE_NONE)) {
             mChoiceMode = CHOICE_MODE_NONE;
             resetCheckedGrandchildren();
             if (mActionMode != null) mActionMode.finish();
             mActionMode = null;
         }
     }
 
     protected boolean isChoiceMode(int mode) {
         return mChoiceMode == mode;
     }
 
     protected ArrayList<Checkable> getCheckedGrandchildren() {
         ArrayList<Checkable> checked = new ArrayList<Checkable>();
         final int childCount = getChildCount();
         for (int i = 0; i < childCount; ++i) {
             final ViewGroup layout = (ViewGroup) getChildAt(i);
             final int grandChildCount = layout.getChildCount();
             for (int j = 0; j < grandChildCount; ++j) {
                 final View v = layout.getChildAt(j);
                 if (v instanceof Checkable && ((Checkable) v).isChecked()) {
                     checked.add((Checkable) v);
                 }
             }
         }
         return checked;
     }
 
     /**
      * If in CHOICE_MODE_SINGLE and an item is checked, returns that item.
      * Otherwise, returns null.
      */
     protected Checkable getSingleCheckedGrandchild() {
         if (mChoiceMode == CHOICE_MODE_SINGLE) {
             final int childCount = getChildCount();
             for (int i = 0; i < childCount; ++i) {
                 final ViewGroup layout = (ViewGroup) getChildAt(i);
                 final int grandChildCount = layout.getChildCount();
                 for (int j = 0; j < grandChildCount; ++j) {
                     final View v = layout.getChildAt(j);
                     if (v instanceof Checkable && ((Checkable) v).isChecked()) {
                         return (Checkable) v;
                     }
                 }
             }
         }
         return null;
     }
 
     public Object getChosenItem() {
         View checkedView = (View) getSingleCheckedGrandchild();
         if (checkedView != null) {
             return checkedView.getTag();
         }
         return null;
     }
 
     protected void resetCheckedGrandchildren() {
         // loop through children, and set all of their children to _not_ be checked
         final ArrayList<Checkable> checked = getCheckedGrandchildren();
         for (int i = 0; i < checked.size(); ++i) {
             final Checkable c = checked.get(i);
             c.setChecked(false);
         }
     }
 
     /**
      * This method is called ONLY to synchronize the number of pages that the paged view has.
      * To actually fill the pages with information, implement syncPageItems() below.  It is
      * guaranteed that syncPageItems() will be called for a particular page before it is shown,
      * and therefore, individual page items do not need to be updated in this method.
      */
     public abstract void syncPages();
 
     /**
      * This method is called to synchronize the items that are on a particular page.  If views on
      * the page can be reused, then they should be updated within this method.
      */
     public abstract void syncPageItems(int page);
 
     public void invalidatePageData() {
         if (mContentIsRefreshable) {
             // Update all the pages
             syncPages();
 
             // Mark each of the pages as dirty
             final int count = getChildCount();
             mDirtyPageContent.clear();
             for (int i = 0; i < count; ++i) {
                 mDirtyPageContent.add(true);
             }
 
             // Load any pages that are necessary for the current window of views
             loadAssociatedPages(mCurrentPage);
             mDirtyPageAlpha = true;
             updateAdjacentPagesAlpha();
             requestLayout();
         }
     }
 }
