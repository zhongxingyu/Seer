 /*
  *  This program is a widget library base on Android platform
  *  Copyright (C) 2012  Xu Zhengyang
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  *
  *******************************************************************
  * This class first version is base on HorizontalListView.java v1.5
  * by Paul Soucy.
  * Notice for this file:
  *******************************************************************
  *
  * The MIT License
  * Copyright (c) 2011 Paul Soucy (paul@dev-smart.com)
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  *
  */
 
 package xzy.android.awidgets.widget;
 
 import android.content.Context;
 import android.content.res.TypedArray;
 import android.database.DataSetObserver;
 import android.graphics.Canvas;
 import android.graphics.Rect;
 import android.util.AttributeSet;
 import android.view.GestureDetector;
 import android.view.GestureDetector.OnGestureListener;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.ViewParent;
 import android.widget.AdapterView;
 import android.widget.HeaderViewListAdapter;
 import android.widget.ListAdapter;
 import android.widget.ListView;
 import android.widget.ListView.FixedViewInfo;
 import android.widget.Scroller;
 
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.Queue;
 
 /**
  * @author zhengyang.xu
  * @version 0.9.1
  * @since Mar 27, 2012 1:11:55 PM
  * @project AWidgets
  */
 public class AHorizontalListView extends AdapterView<ListAdapter> {
 
     public boolean mAlwaysOverrideTouch = true;
 
     protected ListAdapter mAdapter;
 
     private int mLeftViewIndex = -1;
 
     private int mRightViewIndex = 0;
 
     protected int mCurrentX;
 
     protected int mNextX;
 
     private int mMaxX = Integer.MAX_VALUE;
 
     private int mDisplayOffset = 0;
 
     private ArrayList<ListView.FixedViewInfo> mHeaderViewInfos = new ArrayList<ListView.FixedViewInfo>();
 
     private ArrayList<ListView.FixedViewInfo> mFooterViewInfos = new ArrayList<ListView.FixedViewInfo>();
 
     protected Scroller mScroller;
 
     private GestureDetector mGesture;
 
     private Queue<View>[] mRemovedViewQueue;// = new LinkedList<View>()[];
 
     private OnItemSelectedListener mOnItemSelected;
 
     private OnItemClickListener mOnItemClicked;
 
     private OnItemLongClickListener mOnItemLongClicked;
 
     private OnScrollEndListener mOnScrollEndListener;
 
     private boolean mDataChanged = false;
 
     private boolean isInLayout = false;
 
     private boolean mStartScroll = false;
 
     private boolean TOUCH_MODE = false;
 
     private boolean SCROLL_MODE = false;
 
     public AHorizontalListView(Context context) {
         super(context);
         this.setWillNotDraw(false);
         setAlwaysDrawnWithCacheEnabled(false);
         initView();
         // this.setHorizontalFadingEdgeEnabled(true);
         this.setHorizontalScrollBarEnabled(true);
 
         TypedArray a = context.obtainStyledAttributes(xzy.android.awidgets.R.styleable.hlistview);
 
         initializeScrollbars(a);
         a.recycle();
     }
 
     public AHorizontalListView(Context context, AttributeSet attrs) {
         super(context, attrs);
         this.setWillNotDraw(false);
         setAlwaysDrawnWithCacheEnabled(false);
         initView();
         // this.setHorizontalFadingEdgeEnabled(true);
         this.setHorizontalScrollBarEnabled(true);
 
         // this.setScrollbarFadingEnabled(true);
 
         TypedArray a = context.obtainStyledAttributes(xzy.android.awidgets.R.styleable.hlistview);
 
         initializeScrollbars(a);
         a.recycle();
     }
 
     private synchronized void initView() {
         mLeftViewIndex = -1;
         mRightViewIndex = 0;
         mDisplayOffset = 0;
         mCurrentX = 0;
         mNextX = 0;
         mMaxX = Integer.MAX_VALUE;
         mScroller = new Scroller(getContext());
         mGesture = new GestureDetector(getContext(), mOnGesture);
     }
 
     @Override
     public void setOnItemSelectedListener(AdapterView.OnItemSelectedListener listener) {
         mOnItemSelected = listener;
     }
 
     @Override
     public void setOnItemClickListener(AdapterView.OnItemClickListener listener) {
         mOnItemClicked = listener;
     }
 
     @Override
     public void setOnItemLongClickListener(AdapterView.OnItemLongClickListener listener) {
         mOnItemLongClicked = listener;
     }
 
     private DataSetObserver mDataObserver = new DataSetObserver() {
 
         @Override
         public void onChanged() {
             synchronized (AHorizontalListView.this) {
                 mDataChanged = true;
             }
             invalidate();
             if (!isInLayout) {
                 requestLayout();
             }
         }
 
         @Override
         public void onInvalidated() {
             reset();
             invalidate();
             if (!isInLayout) {
                 requestLayout();
             }
         }
 
     };
 
     @Override
     protected void onDraw(Canvas canvas) {
         super.onDraw(canvas);
     }
 
     @Override
     protected void dispatchDraw(Canvas canvas) {
         super.dispatchDraw(canvas);
     }
 
     @Override
     public ListAdapter getAdapter() {
         return mAdapter;
     }
 
     @Override
     public View getSelectedView() {
         // TODO: implement
         return null;
     }
 
     @Override
     public void setAdapter(ListAdapter adapter) {
 
         if (mAdapter != null) {
             mAdapter.unregisterDataSetObserver(mDataObserver);
         }
 
         if (mRemovedViewQueue != null) {
             for (int i = mRemovedViewQueue.length - 1; i >= 0; i--) {
                 mRemovedViewQueue[i].clear();
             }
         }
 
         if (mHeaderViewInfos.size() > 0 || mFooterViewInfos.size() > 0) {
             mAdapter = new HeaderViewListAdapter(mHeaderViewInfos, mFooterViewInfos, adapter);
         } else {
             mAdapter = adapter;
         }
         int viewTypeCount = mAdapter.getViewTypeCount();
         Queue<View>[] removedViewQueue = new LinkedList[viewTypeCount + 1];
         for (int i = viewTypeCount; i >= 0; i--) {
             removedViewQueue[i] = new LinkedList<View>();
         }
         mRemovedViewQueue = removedViewQueue;
 
         mAdapter.registerDataSetObserver(mDataObserver);
         reset();
     }
 
     private synchronized void reset() {
         // need test
         // mFooterViewInfos.clear();
         // mHeaderViewInfos.clear();
         initView();
         removeAllViewsInLayout();
         if (!isInLayout) {
             requestLayout();
         }
     }
 
     @Override
     public void setSelection(int position) {
 
         if (position < 0 || position >= mAdapter.getCount()) {
             return;
         }
 
         int rightEdge = 0;
         View child;
         for (int i = 0; i < position - 1; i++) {
             int itemType = mAdapter.getItemViewType(mRightViewIndex);
             if (itemType < 0) {
                 itemType = mAdapter.getViewTypeCount();
             }
             child = mAdapter.getView(i, mRemovedViewQueue[itemType].poll(), this);
             if (child != null) {
                 ViewGroup.LayoutParams params = child.getLayoutParams();
                 if (params == null) {
                     params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                             ViewGroup.LayoutParams.FILL_PARENT);
                 }
                 if (params.width > 0) {
                     child.measure(MeasureSpec.makeMeasureSpec(getWidth(), MeasureSpec.EXACTLY),
                             MeasureSpec.makeMeasureSpec(getHeight(), MeasureSpec.AT_MOST));
                 } else {
                     child.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
                             MeasureSpec.makeMeasureSpec(getHeight(), MeasureSpec.AT_MOST));
                 }
                 rightEdge += child.getMeasuredWidth();
                 child = null;
                 if (rightEdge > mMaxX) {
                     rightEdge = mMaxX;
                     break;
                 }
             }
         }
         mScroller.setFinalX(rightEdge);
         if (!isInLayout) {
             requestLayout();
         }
     }
 
     public void setSelectionFromLeft(int position, int x) {
 
         if (position < 0 || position >= mAdapter.getCount()) {
             return;
         }
 
         int rightEdge = 0;
         View child;
         for (int i = 0; i < position - 1; i++) {
             int itemType = mAdapter.getItemViewType(mRightViewIndex);
             if (itemType < 0) {
                 itemType = mAdapter.getViewTypeCount();
             }
             child = mAdapter.getView(i, mRemovedViewQueue[itemType].poll(), this);
             if (child != null) {
                 ViewGroup.LayoutParams params = child.getLayoutParams();
                 if (params == null) {
                     params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                             ViewGroup.LayoutParams.FILL_PARENT);
                 }
                 if (params.width > 0) {
                     child.measure(MeasureSpec.makeMeasureSpec(getWidth(), MeasureSpec.EXACTLY),
                             MeasureSpec.makeMeasureSpec(getHeight(), MeasureSpec.AT_MOST));
                 } else {
                     child.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
                             MeasureSpec.makeMeasureSpec(getHeight(), MeasureSpec.AT_MOST));
                 }
                 rightEdge += child.getMeasuredWidth();
                 child = null;
                 if (rightEdge > mMaxX) {
                     rightEdge = mMaxX;
                     break;
                 }
             }
         }
         rightEdge += x;
         mScroller.setFinalX(rightEdge);
         if (!isInLayout) {
             requestLayout();
         }
     }
 
     public void smoothScrollToPosition(int position) {
 
         if (position < 0 || position >= mAdapter.getCount()) {
             return;
         }
 
         int rightEdge = 0;
         View child;
         for (int i = 0; i < position - 1; i++) {
             int itemType = mAdapter.getItemViewType(mRightViewIndex);
             if (itemType < 0) {
                 itemType = mAdapter.getViewTypeCount();
             }
             child = mAdapter.getView(i, mRemovedViewQueue[itemType].poll(), this);
             if (child != null) {
                 ViewGroup.LayoutParams params = child.getLayoutParams();
                 if (params == null) {
                     params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                             ViewGroup.LayoutParams.FILL_PARENT);
                 }
                 if (params.width > 0) {
                     child.measure(MeasureSpec.makeMeasureSpec(getWidth(), MeasureSpec.EXACTLY),
                             MeasureSpec.makeMeasureSpec(getHeight(), MeasureSpec.AT_MOST));
                 } else {
                     child.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
                             MeasureSpec.makeMeasureSpec(getHeight(), MeasureSpec.AT_MOST));
                 }
                 rightEdge += child.getMeasuredWidth();
                 child = null;
                 if (rightEdge > mMaxX) {
                     rightEdge = mMaxX;
                     break;
                 }
             }
         }
         this.scrollTo(rightEdge);
     }
 
     private void addAndMeasureChild(final View child, int viewPos) {
 
         ViewGroup.LayoutParams params = child.getLayoutParams();
         if (params == null) {
             params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                     ViewGroup.LayoutParams.FILL_PARENT);
         }
 
         addViewInLayout(child, viewPos, params, true);
         if (params.width > 0) {
             child.measure(MeasureSpec.makeMeasureSpec(getWidth(), MeasureSpec.EXACTLY),
                     MeasureSpec.makeMeasureSpec(getHeight(), MeasureSpec.AT_MOST));
         } else {
             child.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
                     MeasureSpec.makeMeasureSpec(getHeight(), MeasureSpec.AT_MOST));
         }
     }
 
     @Override
     protected int computeHorizontalScrollExtent() {
         // TODO
         if (mAdapter == null || mAdapter.getCount() == 0) {
             return 0;
         }
         return (int) (((float) (mRightViewIndex - mLeftViewIndex)) / mAdapter.getCount() * (float) getWidth());
         /*
          * final int count = getChildCount(); if (count > 0) { int extent =
          * count * 100; View view = getChildAt(0); final int left =
          * view.getLeft(); int width = view.getWidth(); if (width > 0) { extent
          * += (left * 100) / width; } view = getChildAt(count - 1); final int
          * right = view.getRight(); width = view.getWidth(); if (width > 0) {
          * extent -= ((right - getWidth()) * 100) / width; } return extent; }
          * return 0;
          */
     }
 
     @Override
     protected int computeHorizontalScrollOffset() {
         // TODO
         if (mAdapter == null || mAdapter.getCount() == 0) {
             return 0;
         }
         return mCurrentX + getWidth() * mLeftViewIndex / mAdapter.getCount();
         /*
          * final int firstPosition = mLeftViewIndex; final int childCount =
          * getChildCount(); if (firstPosition >= 0 && childCount > 0) { final
          * View view = getChildAt(0); final int left = view.getLeft(); int width
          * = view.getWidth(); if (width > 0) { return Math.max(firstPosition *
          * 100 - (left * 100) / width + (int) ((float) mNextX / getWidth() *
          * mAdapter.getCount() * 100), 0); } }
          *//*
             * else { int index; final int count = mAdapter.getCount(); if
             * (firstPosition == 0) { index = 0; } else if (firstPosition +
             * childCount == count) { index = count; } else { index =
             * firstPosition + childCount / 2; } return (int) (firstPosition +
             * childCount * (index / (float) count)); }
             */
         // return 0;
     }
 
     @Override
     protected int computeHorizontalScrollRange() {
         // TODO
        if (mAdapter == null ||mAdapter.getCount() == 0) {
             return 0;
         }
         View child = getChildAt(0);
         if (child != null) {
             // int childWidth = getChildAt(getChildCount() - 1).getWidth();
             // return childWidth * mAdapter.getCount();
             int childWidth = getChildAt(0).getWidth();
             return childWidth * mAdapter.getCount();
         }
         // int result;
         // result = Math.max(mAdapter.getCount() * 100, 0);
         // if (mNextX != 0) {
         // // Compensate for overscroll
         // result += Math.abs((int) ((float) mNextX / getWidth() *
         // mAdapter.getCount() * 100));
         // }
         // return result;
         return 0;
     }
 
     public void setTargetXAfterNotifyDataSetChanged(int targetX) {
         mCurrentX = targetX;
     }
 
     @Override
     protected synchronized void onLayout(boolean changed, int left, int top, int right, int bottom) {
         super.onLayout(changed, left, top, right, bottom);
         isInLayout = true;
 
         if (mAdapter == null) {
             return;
         }
 
        if (changed) {
             int oldCurrentX = mCurrentX;
             initView();
             removeAllViewsInLayout();
             mNextX = oldCurrentX;
         }
 
        if (mDataChanged) {
             int oldCurrentX = mCurrentX;
             initView();
             removeAllViewsInLayout();
             mNextX = oldCurrentX;
             mDataChanged = false;
        }
 
         if (mScroller.computeScrollOffset()) {
             int scrollx = mScroller.getCurrX();
             mNextX = scrollx;
             awakenScrollBars();
         }
 
         if (mNextX <= 0) {
             mNextX = 0;
             awakenScrollBars();
             mScroller.forceFinished(true);
         }
 
         if (mNextX >= mMaxX) {
             mNextX = mMaxX - 1;
             mScroller.forceFinished(true);
             awakenScrollBars();
             mOnScrollEndListener.onScrollEnd();
         }
 
         int dx = mCurrentX - mNextX;
         removeNonVisibleItems(dx);
 
         fillList(dx);
         positionItems(dx);
 
         mCurrentX = mNextX;
 
         isInLayout = false;
         if (!mScroller.isFinished()) {
             awakenScrollBars();
             postDelayed(new Runnable() {
 
                 @Override
                 public void run() {
                     if (!isInLayout) {
                         requestLayout();
                     }
                 }
             }, 20);
 
         } else {
             postDelayed(new Runnable() {
 
                 @Override
                 public void run() {
                     if (!TOUCH_MODE && mOnScrollEndListener != null) {
                         mOnScrollEndListener.onScrollFinish();
                         // mScroller.setFinalX(mCurrentX);
                     } else {
                         android.util.Log.i("xzy", "not in touch mode");
                     }
                 }
             }, 20);
         }
//        super.onLayout(changed, left, top, right, bottom);
     }
 
     private void fillList(final int dx) {
         int edge = 0;
         View child = getChildAt(getChildCount() - 1);
         if (child != null) {
             edge = child.getRight();
         }
         fillListRight(edge, dx);
 
         edge = 0;
         child = getChildAt(0);
         if (child != null) {
             edge = child.getLeft();
         }
         fillListLeft(edge, dx);
 
     }
 
     private void fillListRight(int rightEdge, final int dx) {
         while (rightEdge + dx <= getWidth() && mRightViewIndex < mAdapter.getCount()) {
             int itemType = mAdapter.getItemViewType(mRightViewIndex);
             View child;
             if (itemType < 0) {
                 itemType = mAdapter.getViewTypeCount();
             }
             child = mAdapter.getView(mRightViewIndex, mRemovedViewQueue[itemType].poll(), this);
             addAndMeasureChild(child, -1);
             rightEdge += child.getMeasuredWidth();
             if (mRightViewIndex == mAdapter.getCount() - 1) {
                 mMaxX = mCurrentX + rightEdge - getWidth();
             }
 
             if (mMaxX < 0) {
                 mMaxX = 0;
             }
             mRightViewIndex++;
 
             // remove some nonVisible views
             if (child != null && rightEdge + dx <= 0) {
                 itemType = mAdapter.getItemViewType(mLeftViewIndex + 1);
 
                 mDisplayOffset += child.getMeasuredWidth();
                 mRemovedViewQueue[itemType].offer(child);
                 removeViewInLayout(child);
                 mLeftViewIndex++;
                 child = getChildAt(0);
 
             }
         }
     }
 
     private void fillListLeft(int leftEdge, final int dx) {
         while (leftEdge + dx > 0 && mLeftViewIndex >= 0) {
             int itemType = mAdapter.getItemViewType(mLeftViewIndex);
             if (itemType < 0) {
                 itemType = mAdapter.getViewTypeCount();
             }
             View child = mAdapter.getView(mLeftViewIndex, mRemovedViewQueue[itemType].poll(), this);
             addAndMeasureChild(child, 0);
             leftEdge -= child.getMeasuredWidth();
             mLeftViewIndex--;
             mDisplayOffset -= child.getMeasuredWidth();
         }
     }
 
     private void removeNonVisibleItems(final int dx) {
         View child = getChildAt(0);
 
         if (dx + getWidth() > 0) {
             while (child != null && child.getRight() + dx <= 0) {
                 int itemType = mAdapter.getItemViewType(mLeftViewIndex + 1);
                 if (itemType < 0) {
                     itemType = mAdapter.getViewTypeCount();
                 }
 
                 mDisplayOffset += child.getMeasuredWidth();
                 mRemovedViewQueue[itemType].offer(child);
                 removeViewInLayout(child);
                 mLeftViewIndex++;
                 child = getChildAt(0);
 
             }
         }
 
         child = getChildAt(getChildCount() - 1);
         while (child != null && child.getLeft() + dx >= getWidth()) {
             int itemType = mAdapter.getItemViewType(mRightViewIndex - 1);
             if (itemType < 0) {
                 itemType = mAdapter.getViewTypeCount();
             }
             mRemovedViewQueue[itemType].offer(child);
             removeViewInLayout(child);
             mRightViewIndex--;
             child = getChildAt(getChildCount() - 1);
         }
     }
 
     private void positionItems(final int dx) {
         if (getChildCount() > 0) {
             mDisplayOffset += dx;
             int left = mDisplayOffset;
             for (int i = 0; i < getChildCount(); i++) {
                 View child = getChildAt(i);
                 int childWidth = child.getMeasuredWidth();
                 child.layout(left, 0, left + childWidth, child.getMeasuredHeight());
                 left += childWidth;
             }
         }
     }
 
     public synchronized void scrollTo(int x) {
         mScroller.startScroll(mNextX, 0, x - mNextX, 0);
         if (!isInLayout) {
             requestLayout();
         }
     }
 
     @Override
     public boolean dispatchTouchEvent(MotionEvent ev) {
 
         boolean handled = super.dispatchTouchEvent(ev);
         handled |= mGesture.onTouchEvent(ev);
         switch (ev.getAction()) {
             case MotionEvent.ACTION_DOWN:
                 TOUCH_MODE = true;
                 SCROLL_MODE = false;
                 final ViewParent parent = AHorizontalListView.this.getParent();
                 if (parent != null) {
                     parent.requestDisallowInterceptTouchEvent(true);
                 }
                 break;
             case MotionEvent.ACTION_MOVE:
                 break;
             case MotionEvent.ACTION_CANCEL:
             case MotionEvent.ACTION_UP:
                 if (SCROLL_MODE) {
                     mOnScrollEndListener.onScrollFinish();
                 }
                 SCROLL_MODE = false;
                 TOUCH_MODE = false;
                 break;
             default:
                 break;
         }
         return handled;
     }
 
     protected boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
 
         synchronized (AHorizontalListView.this) {
             mScroller.fling(mNextX, 0, (int) -velocityX, 0, 0, mMaxX, 0, 0);
         }
         if (!isInLayout) {
             requestLayout();
         }
         return true;
     }
 
     protected boolean onDown(MotionEvent e) {
         mScroller.forceFinished(true);
         awakenScrollBars();
         return true;
     }
 
     private OnGestureListener mOnGesture = new GestureDetector.SimpleOnGestureListener() {
 
         @Override
         public boolean onDown(MotionEvent e) {
             SCROLL_MODE = false;
             return AHorizontalListView.this.onDown(e);
         }
 
         @Override
         public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
             SCROLL_MODE = false;
             return AHorizontalListView.this.onFling(e1, e2, velocityX, velocityY);
         }
 
         @Override
         public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
             synchronized (AHorizontalListView.this) {
                 SCROLL_MODE = true;
                 if (!mStartScroll) {
                     mStartScroll = true;
                     return true;
                 }
                 awakenScrollBars();
                 AHorizontalListView.this.invalidate();
                 mNextX += (int) distanceX;
                 // mScroller.setFinalX(mNextX);
 
                 /*
                  * final ViewParent parent =
                  * AHorizontalListView.this.getParent(); if (parent != null) {
                  * parent.requestDisallowInterceptTouchEvent(true); }
                  */
             }
             if (!isInLayout) {
                 requestLayout();
             }
             return true;
         }
 
         @Override
         public boolean onSingleTapConfirmed(MotionEvent e) {
             SCROLL_MODE = false;
             for (int i = 0; i < getChildCount(); i++) {
                 View child = getChildAt(i);
                 if (isEventWithinView(e, child)) {
                     if (mOnItemClicked != null) {
                         mOnItemClicked.onItemClick(AHorizontalListView.this, child, mLeftViewIndex
                                 + 1 + i, mAdapter.getItemId(mLeftViewIndex + 1 + i));
                     }
                     if (mOnItemSelected != null) {
                         mOnItemSelected.onItemSelected(AHorizontalListView.this, child,
                                 mLeftViewIndex + 1 + i, mAdapter.getItemId(mLeftViewIndex + 1 + i));
                     }
                     break;
                 }
 
             }
             return true;
         }
 
         @Override
         public void onLongPress(MotionEvent e) {
             SCROLL_MODE = false;
             int childCount = getChildCount();
             for (int i = 0; i < childCount; i++) {
                 View child = getChildAt(i);
                 if (isEventWithinView(e, child)) {
                     if (mOnItemLongClicked != null) {
                         mOnItemLongClicked.onItemLongClick(AHorizontalListView.this, child,
                                 mLeftViewIndex + 1 + i, mAdapter.getItemId(mLeftViewIndex + 1 + i));
                     }
                     break;
                 }
             }
         }
 
         private boolean isEventWithinView(MotionEvent e, View child) {
             Rect viewRect = new Rect();
             int[] childPosition = new int[2];
             child.getLocationOnScreen(childPosition);
             int left = childPosition[0];
             int right = left + child.getWidth();
             int top = childPosition[1];
             int bottom = top + child.getHeight();
             viewRect.set(left, top, right, bottom);
             return viewRect.contains((int) e.getRawX(), (int) e.getRawY());
         }
     };
 
     @Override
     public void draw(Canvas canvas) {
         super.draw(canvas);
     }
 
     public void addFooterView(View v) {
         // TODO
         addFooterView(v, null, true);
     }
 
     public void addFooterView(View v, Object data, boolean isSelectable) {
 
         if (mAdapter != null && !(mAdapter instanceof HeaderViewListAdapter)) {
             throw new IllegalStateException(
                     "Cannot add footer view to HorizontalList -- setAdapter has already been called.");
         }
 
         ListView listview = new ListView(this.getContext());
         FixedViewInfo info = listview.new FixedViewInfo();
         info.view = v;
         info.data = data;
         info.isSelectable = isSelectable;
         mFooterViewInfos.add(info);
 
         if (mAdapter != null && mDataObserver != null) {
             mDataObserver.onChanged();
         }
     }
 
     public int getFooterViewCount() {
         return mFooterViewInfos.size();
     }
 
     public boolean removeFooterView(View v) {
         if (mFooterViewInfos.size() > 0) {
             boolean result = false;
             if (mAdapter != null && ((HeaderViewListAdapter) mAdapter).removeFooter(v)) {
                 removeFixedViewInfo(v, mFooterViewInfos);
                 if (mDataObserver != null) {
                     mDataObserver.onChanged();
                 }
                 result = true;
             }
             return result;
         }
         return false;
     }
 
     private void removeFixedViewInfo(View v, ArrayList<FixedViewInfo> where) {
         int len = where.size();
         for (int i = 0; i < len; ++i) {
             FixedViewInfo info = where.get(i);
             if (info.view == v) {
                 where.remove(i);
                 break;
             }
         }
     }
 
     public void addHeaderView(View v, Object data, boolean isSelectable) {
         // TODO
         if (mAdapter != null && !(mAdapter instanceof HeaderViewListAdapter)) {
             throw new IllegalStateException(
                     "Cannot add header view to HorizontalList -- setAdapter has already been called.");
         }
 
         ListView listView = new ListView(this.getContext());
         FixedViewInfo info = listView.new FixedViewInfo();
         info.view = v;
         info.data = data;
         info.isSelectable = isSelectable;
         mHeaderViewInfos.add(info);
 
         if (mAdapter != null && mDataObserver != null) {
             mDataObserver.onChanged();
         }
     }
 
     public void addHeaderView(View v) {
         addHeaderView(v, null, true);
     }
 
     public int getHeaderViewsCount() {
         return mHeaderViewInfos.size();
     }
 
     public boolean removeHeaderView(View v) {
         if (mHeaderViewInfos.size() > 0) {
             boolean result = false;
             if (mAdapter != null && ((HeaderViewListAdapter) mAdapter).removeHeader(v)) {
                 if (mDataObserver != null) {
                     mDataObserver.onChanged();
                 }
                 result = true;
             }
             removeFixedViewInfo(v, mHeaderViewInfos);
             return result;
         }
         return false;
     }
 
     public int getCurrentX() {
         // return mScroller.getCurrX();
         if (mNextX == mMaxX - 1) {
             return mMaxX;
         }
         return mNextX;
     }
 
     public int getMaxX() {
         return mMaxX;
     }
 
     public void setOnScrollEndListener(OnScrollEndListener l) {
         if (l != null) {
             mOnScrollEndListener = l;
         }
     }
 
     public interface OnScrollEndListener {
         void onScrollEnd();
 
         void onScrollFinish();
     }
 
     @Override
     public int getFirstVisiblePosition() {
         return mLeftViewIndex + 1;
     }
 
     @Override
     public int getLastVisiblePosition() {
         return mRightViewIndex;
     }
 
     @Override
     public boolean onInterceptTouchEvent(MotionEvent ev) {
 
         synchronized (AHorizontalListView.this) {
             // if (TOUCH_MODE && mStartScroll && !isInLayout) { // &&
             // SCROLL_MODE)
             // {
             if (SCROLL_MODE) {
                 mStartScroll = false; // TODO TEST
                 return true;
             } else {
                 return false;
             }
         }
     }
 }
