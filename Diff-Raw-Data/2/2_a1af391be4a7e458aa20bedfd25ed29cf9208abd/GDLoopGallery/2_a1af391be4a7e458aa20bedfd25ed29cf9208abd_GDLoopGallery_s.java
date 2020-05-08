 /*
  * Copyright (C) 2007 The Android Open Source Project
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
 
 package com.dbstar.widget;
 
 import com.dbstar.R;
 
 import android.content.Context;
 import android.content.res.TypedArray;
 import android.graphics.Rect;
 import android.os.Bundle;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.Gravity;
 import android.view.KeyEvent;
 import android.view.SoundEffectConstants;
 import android.view.View;
 import android.view.ViewConfiguration;
 import android.view.ViewGroup;
 import android.view.View.MeasureSpec;
 import android.view.accessibility.AccessibilityEvent;
 import android.view.accessibility.AccessibilityNodeInfo;
 import android.view.animation.AnimationUtils;
 import android.view.animation.GridLayoutAnimationController;
 import android.view.animation.LayoutAnimationController;
 import android.view.animation.Transformation;
 import android.widget.Scroller;
 
 /**
  * A view that shows items in a center-locked, horizontally scrolling list.
  * <p>
  * The default values for the Gallery assume you will be using
  * {@link android.R.styleable#Theme_galleryItemBackground} as the background for
  * each View given to the Gallery from the Adapter. If you are not doing this,
  * you may need to adjust some Gallery properties, such as the spacing.
  * <p>
  * Views given to the Gallery should use {@link Gallery.LayoutParams} as their
  * layout parameters type.
  * 
  * @attr ref android.R.styleable#Gallery_animationDuration
  * @attr ref android.R.styleable#Gallery_spacing
  * @attr ref android.R.styleable#Gallery_gravity
  * 
  * This widget is no longer supported. Other horizontally scrolling
  * widgets include {@link HorizontalScrollView} and {@link android.support.v4.view.ViewPager}
  * from the support library.
  */
 
 public class GDLoopGallery extends GDAbsSpinner {
 
     private static final String TAG = "GDLoopGallery";
 
     /**
      * Horizontal spacing between items.
      */
     private int mSpacing = 0;
 
     /**
      * How long the transition animation should run when a child view changes
      * position, measured in milliseconds.
      */
     private int mAnimationDuration = 400;
 
     /**
      * The alpha of items that are not selected.
      */
     private float mUnselectedAlpha;
     
     /**
      * Left most edge of a child seen so far during layout.
      */
     //private int mLeftMost;
 
     /**
      * Right most edge of a child seen so far during layout.
      */
     //private int mRightMost;
 
     private int mGravity;
 
     
     /**
      * Executes the delta scrolls from a fling or scroll movement. 
      */
     private FlingRunnable mFlingRunnable = new FlingRunnable();
 
     
     /**
      * When fling runnable runs, it resets this to false. Any method along the
      * path until the end of its run() can set this to true to abort any
      * remaining fling. For example, if we've reached either the leftmost or
      * rightmost item, we will set this to true.
      */
     private boolean mShouldStopFling;
     
     /**
      * The currently selected item's child.
      */
     private View mSelectedChild;
 
     /**
      * If true, mFirstPosition is the position of the rightmost child, and
      * the children are ordered right to left.
      */
     private boolean mIsRtl = false;
     
     public GDLoopGallery(Context context) {
         this(context, null);
     }
 
     public GDLoopGallery(Context context, AttributeSet attrs) {
         super(context, attrs);
        
         TypedArray a = context.obtainStyledAttributes(
                 attrs, R.styleable.GDGallery);
 
 //        int index = a.getInt(R.styleable.GDLoopGallery_gravity, -1);
 //        if (index >= 0) {
 //            setGravity(index);
 //        }
 
         int animationDuration =
                 a.getInt(R.styleable.GDGallery_animationDuration, -1);
         if (animationDuration > 0) {
             setAnimationDuration(animationDuration);
         }
 
         int spacing =
                 a.getDimensionPixelOffset(R.styleable.GDGallery_spacing, 0);
         setSpacing(spacing);
 
         float unselectedAlpha = a.getFloat(
                 R.styleable.GDGallery_unselectedAlpha, 0.f);
         setUnselectedAlpha(unselectedAlpha);
         
         a.recycle();
 
         // We draw the selected item last (because otherwise the item to the
         // right overlaps it)
 //        mGroupFlags |= FLAG_USE_CHILD_DRAWING_ORDER;
 //        
 //        mGroupFlags |= FLAG_SUPPORT_STATIC_TRANSFORMATIONS;
     }
 
     /**
      * Sets how long the transition animation should run when a child view
      * changes position. Only relevant if animation is turned on.
      * 
      * @param animationDurationMillis The duration of the transition, in
      *        milliseconds.
      * 
      * @attr ref android.R.styleable#Gallery_animationDuration
      */
     public void setAnimationDuration(int animationDurationMillis) {
         mAnimationDuration = animationDurationMillis;
     }
 
     /**
      * Sets the spacing between items in a Gallery
      * 
      * @param spacing The spacing in pixels between items in the Gallery
      * 
      * @attr ref android.R.styleable#Gallery_spacing
      */
     public void setSpacing(int spacing) {
         mSpacing = spacing;
     }
 
     /**
      * Sets the alpha of items that are not selected in the Gallery.
      * 
      * @param unselectedAlpha the alpha for the items that are not selected.
      * 
      * @attr ref android.R.styleable#Gallery_unselectedAlpha
      */
     public void setUnselectedAlpha(float unselectedAlpha) {
         mUnselectedAlpha = unselectedAlpha;
     }
 
     @Override
     protected boolean getChildStaticTransformation(View child, Transformation t) {
         
         t.clear();
         t.setAlpha(child == mSelectedChild ? 1.0f : mUnselectedAlpha);
         
         return true;
     }
 
     @Override
     protected int computeHorizontalScrollExtent() {
         // Only 1 item is considered to be selected
         return 1;
     }
 
     @Override
     protected int computeHorizontalScrollOffset() {
         // Current scroll position is the same as the selected position
         return mSelectedPosition;
     }
 
     @Override
     protected int computeHorizontalScrollRange() {
         // Scroll range is the same as the item count
         return getCount();
     }
 
     @Override
     protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
         return p instanceof LayoutParams;
     }
 
     @Override
     protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
         return new LayoutParams(p);
     }
 
     @Override
     public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
         return new LayoutParams(getContext(), attrs);
     }
 
     @Override
     protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
         /*
          * Gallery expects Gallery.LayoutParams.
          */
         return new GDLoopGallery.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                 ViewGroup.LayoutParams.WRAP_CONTENT);
     }
 
     @Override
     protected void onLayout(boolean changed, int l, int t, int r, int b) {
         super.onLayout(changed, l, t, r, b);
         
         /*
          * Remember that we are in layout to prevent more layout request from
          * being generated.
          */
         mInLayout = true;
         layout(0, false);
         mInLayout = false;
     }
     
     @Override
     protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
     	super.onMeasure(widthMeasureSpec, heightMeasureSpec);
     	
     	int widthMode = MeasureSpec.getMode(widthMeasureSpec);
     	
     	int width = getMeasuredWidth();
     	int height = getMeasuredHeight();
     	
     	int numCount = getCount();
     	int viewWidth = (width + mSpacing )* numCount - mSpacing;
     	
     	int parentWidth = MeasureSpec.getSize(widthMeasureSpec);
     	
     	if (widthMode == MeasureSpec.AT_MOST) {
     		viewWidth = Math.min(viewWidth, parentWidth);
     	}
     	
 //    	Log.d(TAG, "onMeasure widthxheight = " + viewWidth + "x" + height);
 
     	setMeasuredDimension(viewWidth, height);
     }
 
     @Override
     int getChildHeight(View child) {
         return child.getMeasuredHeight();
     }
 
     /**
      * @return The left of this Gallery.
      */
     private int getLeftOfGallery() {
     	return getPaddingLeft();
     }
     
     /**
      * @return The center of the given view.
      */
     private static int getLeftOfView(View view) {
         return view.getLeft();
     }
     
     /**
      * Tracks a motion scroll. In reality, this is used to do just about any
      * movement to items (touch scroll, arrow-key scroll, set an item as selected).
      * 
      * @param deltaX Change in X from the previous event.
      */
     void trackMotionScroll(boolean toLeft, int deltaX) {
 
         if (getChildCount() == 0) {
             return;
         }
 
         offsetChildrenLeftAndRight(-deltaX);
         
         detachOffScreenChildren(toLeft);
         
         if (toLeft) {
             // If moved left, there will be empty space on the right
             fillToGalleryRight();
         } else {
             // Similarly, empty space on the left
             fillToGalleryLeft();
         }
         
         // Clear unused views
         //mRecycler.clear();
 
         invalidate();
     }
 
     /**
      * Offset the horizontal location of all children of this view by the
      * specified number of pixels.
      * 
      * @param offset the number of pixels to offset
      */
     private void offsetChildrenLeftAndRight(int offset) {
         for (int i = getChildCount() - 1; i >= 0; i--) {
             getChildAt(i).offsetLeftAndRight(offset);
         }
     }
     
     /**
      * Detaches children that are off the screen (i.e.: Gallery bounds).
      * 
      * @param toLeft Whether to detach children to the left of the Gallery, or
      *            to the right.
      */
     private void detachOffScreenChildren(boolean toLeft) {
         int numChildren = getChildCount();
         int firstPosition = mFirstPosition;
         int start = 0;
         int count = 0;
 
         if (toLeft) {
             final int galleryLeft = getLeftOfGallery();
             for (int i = 0; i < numChildren; i++) {
                 int n = mIsRtl ? (numChildren - 1 - i) : i;
                 final View child = getChildAt(n);
                 if (child.getRight() > galleryLeft) {
                     break;
                 } else {
                     start = n;
                     count++;
                     mRecycler.put((firstPosition + n) % getCount(), child);
                 }
             }
             if (!mIsRtl) {
                 start = 0;
             }
         } else {
             final int galleryRight = getWidth() - getPaddingRight();
             for (int i = numChildren - 1; i >= 0; i--) {
                 int n = mIsRtl ? numChildren - 1 - i : i;
                 final View child = getChildAt(n);
                 if (child.getLeft() < galleryRight) {
                     break;
                 } else {
                     start = n;
                     count++;
                     mRecycler.put((firstPosition + n) % getCount(), child);
                 }
             }
             if (mIsRtl) {
                 start = 0;
             }
         }
 
         detachViewsFromParent(start, count);
         
         if (toLeft != mIsRtl) {
             mFirstPosition += count;
             mFirstPosition = mFirstPosition % getCount();
         }
     }
 
     private void setSelectionToLeftChild() {
         
         View selView = mSelectedChild;
         if (mSelectedChild == null) return;
         
         int galleryLeft = getLeftOfGallery();
         
         // Common case where the current selected position is correct
         if (selView.getLeft() <= galleryLeft && selView.getRight() > galleryLeft) {
             return;
         }
         
         // TODO better search
 //        int closestEdgeDistance = Integer.MAX_VALUE;
         int newSelectedChildIndex = 0;
         
         for (int i = 0; i < getChildCount(); i++) {
             
             View child = getChildAt(i);
             
             if (child.getLeft() <= galleryLeft && child.getRight() >  galleryLeft) {
                 // This child is in the center
                 newSelectedChildIndex = i;
                 break;
             }
             
 //            int childClosestEdgeDistance = Math.min(Math.abs(child.getLeft() - galleryLeft),
 //                    Math.abs(child.getRight() - galleryLeft));
 //            if (childClosestEdgeDistance < closestEdgeDistance) {
 //                closestEdgeDistance = childClosestEdgeDistance;
 //                newSelectedChildIndex = i;
 //            }
         }
         
        int newPos = mFirstPosition + newSelectedChildIndex;
         
         if (newPos != mSelectedPosition) {
             setSelectedPositionInt(newPos);
             setNextSelectedPositionInt(newPos);
             checkSelectionChanged();
         }
     }
 
     /**
      * Creates and positions all views for this Gallery.
      * <p>
      * We layout rarely, most of the time {@link #trackMotionScroll(int)} takes
      * care of repositioning, adding, and removing children.
      * 
      * @param delta Change in the selected position. +1 means the selection is
      *            moving to the right, so views are scrolling to the left. -1
      *            means the selection is moving to the left.
      */
     @Override
     void layout(int delta, boolean animate) {
 
         //mIsRtl = isLayoutRtl();
         int childrenLeft = mSpinnerPadding.left;
 
         if (mDataChanged) {
             handleDataChanged();
         }
 
         // Handle an empty gallery by removing all views.
         if (getCount() == 0) {
             resetList();
             return;
         }
 
         // Update to the new selected position.
         if (mNextSelectedPosition >= 0) {
             setSelectedPositionInt(mNextSelectedPosition);
         }
 
         // All views go in recycler while we are in layout
         if (!mDataChanged)
         {
         	// if data has changed, not recycle old views
         	recycleAllViews();
         } else {
         	mRecycler.clear();
         }
 
         // Clear out old views
         detachAllViewsFromParent();
 
         /*
          * These will be used to give initial positions to views entering the
          * gallery as we scroll
          */
 //        mRightMost = 0;
 //        mLeftMost = 0;
 
         // Make selected view and center it
         
         /*
          * mFirstPosition will be decreased as we add views to the left later
          * on. The 0 for x will be offset in a couple lines down.
          */  
         mFirstPosition = mSelectedPosition;
         View sel = makeAndAddView(mSelectedPosition, 0, 0, true);
         
         // Put the selected child in the center
         int selectedOffset = childrenLeft;
         sel.offsetLeftAndRight(selectedOffset);
 
         fillToGalleryRight();
         //fillToGalleryLeft();
         
         // Flush any cached views that did not get reused above
         //mRecycler.clear();
 
         //invalidate();
         checkSelectionChanged();
 
         mDataChanged = false;
         mNeedSync = false;
         setNextSelectedPositionInt(mSelectedPosition);
         
         updateSelectedItemMetadata();
     }
 
     private void fillToGalleryLeft() {
         if (mIsRtl) {
             fillToGalleryLeftRtl();
         } else {
             fillToGalleryLeftLtr();
         }
     }
 
     private void fillToGalleryLeftLtr() {
         int itemSpacing = mSpacing;
         int galleryLeft = getPaddingLeft();
         
         // Set state for initial iteration
         View prevIterationView = getChildAt(0);
         int curPosition;
         int curRightEdge;
         
         if (prevIterationView != null) {
             curPosition = mFirstPosition - 1;
             curRightEdge = prevIterationView.getLeft() - itemSpacing;
         } else {
             // No children available!
             curPosition = 0; 
             curRightEdge = getRight() - getLeft() - getPaddingRight();
             mShouldStopFling = true;
         }
                 
         while (curRightEdge >= galleryLeft) {
         	int index = curPosition % getCount();
         	if (index < 0) index = index + getCount();
         	
             prevIterationView = makeAndAddView(index, curPosition - mSelectedPosition,
                     curRightEdge, false);
 
             // Remember some state
             mFirstPosition = index;
             // Set state for next iteration
             curRightEdge = prevIterationView.getLeft() - itemSpacing;
             curPosition--;
         }
     }
     
     private void fillToGalleryRight() {
         if (mIsRtl) {
             fillToGalleryRightRtl();
         } else {
             fillToGalleryRightLtr();
         }
     }
 
     private void fillToGalleryRightLtr() {
         int itemSpacing = mSpacing;
         int galleryRight = getRight() - getLeft() - getPaddingRight();
         int numChildren = getChildCount();
         int numItems = getCount();
         
         // Set state for initial iteration
         View prevIterationView = getChildAt(numChildren - 1);
         int curPosition;
         int curLeftEdge;
         
         if (prevIterationView != null) {
             curPosition = mFirstPosition + numChildren;
             curLeftEdge = prevIterationView.getRight() + itemSpacing;
         } else {
             mFirstPosition = curPosition = getCount() - 1;
             curLeftEdge = getPaddingLeft();
             mShouldStopFling = true;
         }
                 
         //while (curLeftEdge < galleryRight && getChildCount() < getCount()) {
         while (curLeftEdge <= galleryRight && getChildCount() <= getCount()) {
         	
         	int index =  curPosition % numItems;
         	
             prevIterationView = makeAndAddView(index, curPosition - mSelectedPosition,
                     curLeftEdge, true);
 
             // Set state for next iteration
             curLeftEdge = prevIterationView.getRight() + itemSpacing;
             curPosition++;
         }
     }
 
     /**
      * Obtain a view, either by pulling an existing view from the recycler or by
      * getting a new one from the adapter. If we are animating, make sure there
      * is enough information in the view's layout parameters to animate from the
      * old to new positions.
      * 
      * @param position Position in the gallery for the view to obtain
      * @param offset Offset from the selected position
      * @param x X-coordinate indicating where this view should be placed. This
      *        will either be the left or right edge of the view, depending on
      *        the fromLeft parameter
      * @param fromLeft Are we positioning views based on the left edge? (i.e.,
      *        building from left to right)?
      * @return A view that has been added to the gallery
      */
     private View makeAndAddView(int position, int offset, int x, boolean fromLeft) {
 
         View child;
         if (!mDataChanged) {
             child = mRecycler.get(position);
             if (child != null) {
                 // Can reuse an existing view
                 int childLeft = child.getLeft();
                 
                 // Remember left and right edges of where views have been placed
 //                mRightMost = Math.max(mRightMost, childLeft 
 //                        + child.getMeasuredWidth());
 //                mLeftMost = Math.min(mLeftMost, childLeft);
 
                 // Position the view
                 setUpChild(child, offset, x, fromLeft);
 
                 return child;
             }
         }
 
         // Nothing found in the recycler -- ask the adapter for a view
         child = mAdapter.getView(position, null, this);
 
         // Position the view
         setUpChild(child, offset, x, fromLeft);
 
         return child;
     }
 
     /**
      * Helper for makeAndAddView to set the position of a view and fill out its
      * layout parameters.
      * 
      * @param child The view to position
      * @param offset Offset from the selected position
      * @param x X-coordinate indicating where this view should be placed. This
      *        will either be the left or right edge of the view, depending on
      *        the fromLeft parameter
      * @param fromLeft Are we positioning views based on the left edge? (i.e.,
      *        building from left to right)?
      */
     private void setUpChild(View child, int offset, int x, boolean fromLeft) {
 
         // Respect layout params that are already in the view. Otherwise
         // make some up...
         GDLoopGallery.LayoutParams lp = (GDLoopGallery.LayoutParams) child.getLayoutParams();
         if (lp == null) {
             lp = (GDLoopGallery.LayoutParams) generateDefaultLayoutParams();
         }
 
         addViewInLayout(child, fromLeft != mIsRtl ? -1 : 0, lp);
 
         child.setSelected(offset == 0);
 
         // Get measure specs
         int childHeightSpec = ViewGroup.getChildMeasureSpec(mHeightMeasureSpec,
                 mSpinnerPadding.top + mSpinnerPadding.bottom, lp.height);
         int childWidthSpec = ViewGroup.getChildMeasureSpec(mWidthMeasureSpec,
                 mSpinnerPadding.left + mSpinnerPadding.right, lp.width);
 
         // Measure child
         child.measure(childWidthSpec, childHeightSpec);
 
         int childLeft;
         int childRight;
 
         // Position vertically based on gravity setting
         int childTop = calculateTop(child, true);
         int childBottom = childTop + child.getMeasuredHeight();
 
         int width = child.getMeasuredWidth();
         if (fromLeft) {
             childLeft = x;
             childRight = childLeft + width;
         } else {
             childLeft = x - width;
             childRight = x;
         }
 
         child.layout(childLeft, childTop, childRight, childBottom);
     }
 
     /**
      * Figure out vertical placement based on mGravity
      * 
      * @param child Child to place
      * @return Where the top of the child should be
      */
     private int calculateTop(View child, boolean duringLayout) {
         int myHeight = duringLayout ? getMeasuredHeight() : getHeight();
         int childHeight = duringLayout ? child.getMeasuredHeight() : child.getHeight(); 
         
         int childTop = 0;
 
         switch (mGravity) {
         case Gravity.TOP:
             childTop = mSpinnerPadding.top;
             break;
         case Gravity.CENTER_VERTICAL:
             int availableSpace = myHeight - mSpinnerPadding.bottom
                     - mSpinnerPadding.top - childHeight;
             childTop = mSpinnerPadding.top + (availableSpace / 2);
             break;
         case Gravity.BOTTOM:
             childTop = myHeight - mSpinnerPadding.bottom - childHeight;
             break;
         }
         return childTop;
     }
 
     /**
      * Handles left, right, and clicking
      * @see android.view.View#onKeyDown
      */
     
     public boolean moveToPrev() {
     	if (movePrevious()) {
             playSoundEffect(SoundEffectConstants.NAVIGATION_LEFT);
             return true;
         }
     	
     	return false;
     }
     
     public boolean moveToNext() {
     	if (moveNext()) {
             playSoundEffect(SoundEffectConstants.NAVIGATION_RIGHT);
             return true;
         }
     	
     	return false;
     }
     
     @Override
     public boolean onKeyDown(int keyCode, KeyEvent event) {
     	Log.d(TAG, "=== Gallery onKeyDown keyCode =" + keyCode);
         switch (keyCode) {
         case KeyEvent.KEYCODE_DPAD_LEFT:
             if (movePrevious()) {
                 playSoundEffect(SoundEffectConstants.NAVIGATION_LEFT);
                 return true;
             }
             return true;
         case KeyEvent.KEYCODE_DPAD_RIGHT:
             if (moveNext()) {
                 playSoundEffect(SoundEffectConstants.NAVIGATION_RIGHT);
                 return true;	
             }
             return true;
          default:
         	 break;
         }
         
         return false;
     }
 
     @Override
     public boolean onKeyUp(int keyCode, KeyEvent event) {
 //    	Log.d(TAG, "onKeyUp keyCode " + keyCode);
     	
         switch (keyCode) {
 	        case KeyEvent.KEYCODE_DPAD_LEFT:
 	        case KeyEvent.KEYCODE_DPAD_RIGHT:
 	        	return true;
 	        default:
 	        	break;
         }
 
         return false;
     }
     
     public boolean onKeyLongPress(int keyCode, KeyEvent event) {
 //    	Log.d(TAG, "onKeyLongPress code = " + keyCode);
     	
     	return super.onKeyLongPress(keyCode, event);
     }
     
 //    public boolean onKeyMultiple(int keyCode, int repeatCount, KeyEvent event) {
 //    	Log.d(TAG, "onKeyMultiple keyCode=" + keyCode + " repeatCount=" + repeatCount);
 //
 //    	switch(keyCode) {
 //    		case KeyEvent.KEYCODE_DPAD_LEFT:
 //    			if (movePrevious()) {
 //                    playSoundEffect(SoundEffectConstants.NAVIGATION_LEFT);
 //                    return true;
 //                }
 //    			return true;
 //    		case KeyEvent.KEYCODE_DPAD_RIGHT:
 //    			if (moveNext()) {
 //                    playSoundEffect(SoundEffectConstants.NAVIGATION_RIGHT);
 //                    return true;
 //                }
 //    			return true;
 //    	}
 //    	
 //    	return false;
 //    }
     
     boolean movePrevious() {
     	if (getCount() > 1) {
 //    		Log.d(TAG, "movePrevious " + mSelectedPosition + " " + mFirstPosition);
             scrollToChild(mSelectedPosition - mFirstPosition - 1);
             return true;
         } else {
             return false;
         }
     }
 
     boolean moveNext() {
         if (getCount() > 1) {
 //        	Log.d(TAG, "moveNext " + mSelectedPosition + " " + mFirstPosition);
         	scrollToChild(mSelectedPosition - mFirstPosition + 1);
             return true;
         } else {
             return false;
         }
     }
 
     private boolean scrollToChild(int childPosition) {
         View child = getChildAt(childPosition);
         if (child != null) {
             int distance = getLeftOfGallery() - getLeftOfView(child);
             mFlingRunnable.startUsingDistance(distance);
             return true;
         } else {
         	child = getChildAt(0);
         	if (child != null) {
 	        	int distance = child.getRight() - getLeftOfGallery() + mSpacing;
 	        	mFlingRunnable.startUsingDistance(distance);
 	        	return true;
         	}
         }
         
         return false;
     }
     
     @Override
     void setSelectedPositionInt(int position) {
         super.setSelectedPositionInt(position);
 
         // Updates any metadata we keep about the selected item.
         updateSelectedItemMetadata();
     }
 
     private void updateSelectedItemMetadata() {
         
         View oldSelectedChild = mSelectedChild;
 
         View child = mSelectedChild = getChildAt(mSelectedPosition - mFirstPosition);
         if (child == null) {
             return;
         }
 
         child.setSelected(true);
 //        child.setFocusable(false);
 
 //        if (hasFocus()) {
 //            child.requestFocus();
 //        }
 
         // We unfocus the old child down here so the above hasFocus check
         // returns true
         if (oldSelectedChild != null && oldSelectedChild != child) {
 
             // Make sure its drawable state doesn't contain 'selected'
             oldSelectedChild.setSelected(false);
             
             // Make sure it is not focusable anymore, since otherwise arrow keys
             // can make this one be focused
 //            oldSelectedChild.setFocusable(false);
         }
         
     }
     
     
 
     /**
      * Responsible for fling behavior. Use {@link #startUsingVelocity(int)} to
      * initiate a fling. Each frame of the fling is handled in {@link #run()}.
      * A FlingRunnable will keep re-posting itself until the fling is done.
      */
     private class FlingRunnable implements Runnable {
         /**
          * Tracks the decay of a fling scroll
          */
         private Scroller mScroller;
 
         /**
          * X value reported by mScroller on the previous fling
          */
         private int mLastFlingX;
         
         boolean mStartFling = false;
         boolean mToLeft = false;
         int mFrameRate = 10;
         int mFrameInterval;
         long mStartTime = 0;
 
         public FlingRunnable() {
             mScroller = new Scroller(getContext());
         }
 
         private void startCommon() {
             // Remove any pending flings
             removeCallbacks(this);
         }
         
         public void startUsingVelocity(int initialVelocity) {
             if (initialVelocity == 0) return;
             
             startCommon();
 
             int initialX = initialVelocity < 0 ? Integer.MAX_VALUE : 0;
             mLastFlingX = initialX;
             mScroller.fling(initialX, 0, initialVelocity, 0,
                     0, Integer.MAX_VALUE, 0, Integer.MAX_VALUE);
             post(this);
         }
 
         public void startUsingDistance(int distance) {
             if (distance == 0) return;
 
         	if (mStartFling) return;
         	mStartFling = true;
             
             mToLeft = distance < 0 ? true : false;
 
             startCommon();
             
             mLastFlingX = 0;
 
             mFrameInterval = mAnimationDuration/mFrameRate;
             mStartTime = AnimationUtils.currentAnimationTimeMillis();
 
             mScroller.startScroll(0, 0, -distance, 0, mAnimationDuration);
             post(this);
         }
         
         public void stop(boolean scrollIntoSlots) {
             removeCallbacks(this);
             endFling(scrollIntoSlots);
         }
         
         private void endFling(boolean scrollIntoSlots) {
             /*
              * Force the scroller's status to finished (without setting its
              * position to the end)
              */
             mScroller.forceFinished(true);
             
             //if (scrollIntoSlots) scrollIntoSlots();
             
             setSelectionToLeftChild();
 
 //            onFinishedMovement();
             
             invalidate();
         }
 
         @Override
         public void run() {
 
             if (getCount() == 0) {
                 endFling(true);
                 return;
             }
 
             mShouldStopFling = false;
             
             final Scroller scroller = mScroller;
             boolean more = scroller.computeScrollOffset();
             final int x = scroller.getCurrX();
 
             // Flip sign to convert finger direction to list items direction
             // (e.g. finger moving down means list is moving towards the top)
             int delta = x - mLastFlingX;
 
             // Pretend that each frame of a fling scroll is a touch scroll
             if (delta > 0) {
 
                 // Don't fling more than 1 screen
                 delta = Math.min(getWidth() - getPaddingLeft() - getPaddingRight() - 1, delta);
             } else {
                 // Don't fling more than 1 screen
                 delta = Math.max(-(getWidth() - getPaddingRight() - getPaddingLeft() - 1), delta);
             }
 
             trackMotionScroll(mToLeft, delta);
 
             if (more && !mShouldStopFling) {
                 mLastFlingX = x;
                 //post(this);
                
                 long currentTime = AnimationUtils.currentAnimationTimeMillis();
                 int usedTime = (int)(currentTime - mStartTime);
                 int remainTime = mAnimationDuration - usedTime;
                 
                 postDelayed(this, remainTime > mFrameInterval ? mFrameInterval : remainTime);
             } else {
             	mStartFling = false;
             	mToLeft = false;
             	endFling(true);
             }
         }
         
     }
     
     /**
      * Gallery extends LayoutParams to provide a place to hold current
      * Transformation information along with previous position/transformation
      * info.
      */
     public static class LayoutParams extends ViewGroup.LayoutParams {
         public LayoutParams(Context c, AttributeSet attrs) {
             super(c, attrs);
         }
 
         public LayoutParams(int w, int h) {
             super(w, h);
         }
 
         public LayoutParams(ViewGroup.LayoutParams source) {
             super(source);
         }
     }
     
     private void fillToGalleryLeftRtl() {
         int itemSpacing = mSpacing;
         int galleryLeft = getPaddingLeft();
         int numChildren = getChildCount();
         int numItems = getCount();
 
         // Set state for initial iteration
         View prevIterationView = getChildAt(numChildren - 1);
         int curPosition;
         int curRightEdge;
 
         if (prevIterationView != null) {
             curPosition = mFirstPosition + numChildren;
             curRightEdge = prevIterationView.getLeft() - itemSpacing;
         } else {
             // No children available!
             mFirstPosition = curPosition = getCount() - 1;
             curRightEdge = getRight() - getLeft() - getPaddingRight();
             mShouldStopFling = true;
         }
 
         while (curRightEdge > galleryLeft && curPosition < numItems) {
             prevIterationView = makeAndAddView(curPosition, curPosition - mSelectedPosition,
                     curRightEdge, false);
 
             // Set state for next iteration
             curRightEdge = prevIterationView.getLeft() - itemSpacing;
             curPosition++;
         }
     }
     private void fillToGalleryRightRtl() {
         int itemSpacing = mSpacing;
         int galleryRight = getRight() - getLeft() - getPaddingRight();
 
         // Set state for initial iteration
         View prevIterationView = getChildAt(0);
         int curPosition;
         int curLeftEdge;
 
         if (prevIterationView != null) {
             curPosition = mFirstPosition -1;
             curLeftEdge = prevIterationView.getRight() + itemSpacing;
         } else {
             curPosition = 0;
             curLeftEdge = getPaddingLeft();
             mShouldStopFling = true;
         }
 
         while (curLeftEdge < galleryRight && curPosition >= 0) {
             prevIterationView = makeAndAddView(curPosition, curPosition - mSelectedPosition,
                     curLeftEdge, true);
 
             // Remember some state
             mFirstPosition = curPosition;
 
             // Set state for next iteration
             curLeftEdge = prevIterationView.getRight() + itemSpacing;
             curPosition--;
         }
     }
     
     private void dispatchPress(View child) {
         
         if (child != null) {
             child.setPressed(true);
         }
         
         setPressed(true);
     }
     
     private void dispatchUnpress() {
         
         for (int i = getChildCount() - 1; i >= 0; i--) {
             getChildAt(i).setPressed(false);
         }
         
         setPressed(false);
     }
     
     @Override
     public void dispatchSetSelected(boolean selected) {
         /*
          * We don't want to pass the selected state given from its parent to its
          * children since this widget itself has a selected state to give to its
          * children.
          */
     }
 
     @Override
     protected void dispatchSetPressed(boolean pressed) {
         
         // Show the pressed state on the selected child
         if (mSelectedChild != null) {
             mSelectedChild.setPressed(pressed);
         }
     }
     
 //    @Override
 //    public boolean dispatchKeyEvent(KeyEvent event) {
 //        // Gallery steals all key events
 //    	Log.d(TAG, "dispatchKeyEvent event.keycode=" + event.getKeyCode());
 //        return event.dispatch(this, null, null);
 //    }
     
     /**
      * Describes how the child views are aligned.
      * @param gravity
      * 
      * @attr ref android.R.styleable#Gallery_gravity
      */
     public void setGravity(int gravity)
     {
         if (mGravity != gravity) {
             mGravity = gravity;
             requestLayout();
         }
     }
 
     @Override
     protected int getChildDrawingOrder(int childCount, int i) {
         int selectedIndex = mSelectedPosition - mFirstPosition;
         
         // Just to be safe
         if (selectedIndex < 0) return i;
         
         if (i == childCount - 1) {
             // Draw the selected child last
             return selectedIndex;
         } else if (i >= selectedIndex) {
             // Move the children after the selected child earlier one
             return i + 1;
         } else {
             // Keep the children before the selected child the same
             return i;
         }
     }
 
     @Override
     protected void onFocusChanged(boolean gainFocus, int direction, Rect previouslyFocusedRect) {
         super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
         
 //        Log.d(TAG, " onFocusChanged gainFocus = " + gainFocus + " direction = " + direction);
         /*
          * The gallery shows focus by focusing the selected item. So, give
          * focus to our selected item instead. We steal keys from our
          * selected item elsewhere.
          */
         if (gainFocus && mSelectedChild != null) {
             mSelectedChild.requestFocus(direction);
             mSelectedChild.setSelected(true);
         }
 
     }
     
     /**
      * Action to scroll the node content forward.
      */
     private static final int ACTION_SCROLL_FORWARD = 0x00001000;
     /**
      * Action to scroll the node content backward.
      */
     private static final int ACTION_SCROLL_BACKWARD = 0x00002000;
 
     @Override
     public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
         super.onInitializeAccessibilityEvent(event);
         event.setClassName(GDLoopGallery.class.getName());
     }
 
     @Override
     public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
         super.onInitializeAccessibilityNodeInfo(info);
         info.setClassName(GDLoopGallery.class.getName());
         info.setScrollable(getCount() > 1);
         if (isEnabled()) {
 //        	Log.d(TAG, "onInitializeAccessibilityNodeInfo " );
             if (getCount() > 0 && mSelectedPosition < getCount() - 1) {
 //                info.addAction(ACTION_SCROLL_FORWARD);
             }
             if (isEnabled() && getCount() > 0 && mSelectedPosition > 0) {
 //                info.addAction(ACTION_SCROLL_BACKWARD);
             }
         }
     }
 
     //@Override
     public boolean performAccessibilityAction(int action, Bundle arguments) {
 //        if (super.performAccessibilityAction(action, arguments)) {
 //            return true;
 //        }
 //        switch (action) {
 //            case ACTION_SCROLL_FORWARD: {
 //                if (isEnabled() && getCount() > 0 && mSelectedPosition < getCount() - 1) {
 //                    final int currentChildIndex = mSelectedPosition - mFirstPosition;
 //                    return scrollToChild(currentChildIndex + 1);
 //                }
 //            } return false;
 //            case ACTION_SCROLL_BACKWARD: {
 //                if (isEnabled() && getCount() > 0 && mSelectedPosition > 0) {
 //                    final int currentChildIndex = mSelectedPosition - mFirstPosition;
 //                    return scrollToChild(currentChildIndex - 1);
 //                }
 //            } return false;
 //        }
         return false;
     }
 }
