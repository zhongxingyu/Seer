 package com.github.takuji31.appbase.widget;
 
 import android.annotation.SuppressLint;
 import android.content.Context;
 import android.os.Build;
 import android.os.Handler;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.View;
 import android.view.ViewTreeObserver;
 import android.view.ViewTreeObserver.OnGlobalLayoutListener;
 import android.widget.AbsListView;
 import android.widget.Adapter;
 import android.widget.ListView;
 
 public class PagingListView extends ListView implements
 		AbsListView.OnScrollListener {
 
 	private static final int POST_DELAY_TIME = 50;
 
 	public static interface OnPagingListener {
 		public void onScrollStart(int page);
 
 		public void onScrollFinish(int page);
 
 		public void onNextListLoad(int page);
 	}
 
	private int mPage;
	private int mScrollDuration = 400;
 	private boolean mFlinged;
 	private OnPagingListener mListener;
 	private OnGlobalLayoutListener mLayoutListener = new OnGlobalLayoutListener() {
 		@Override
 		public void onGlobalLayout() {
 			if (mViewHeight == 0) {
 				postDelayed(new Runnable() {
 					
 					@Override
 					public void run() {
 						mViewHeight = getHeight();
 						invalidateViews();
 					}
 				}, POST_DELAY_TIME);
 			}
 		}
 	};
 	
 	private Handler mHandler = new Handler();
 	
 	int mViewHeight;
 
 	public PagingListView(Context context) {
 		super(context);
 		init();
 	}
 
 	public PagingListView(Context context, AttributeSet attrs) {
 		super(context, attrs);
 		init();
 	}
 
 	public PagingListView(Context context, AttributeSet attrs, int defStyle) {
 		super(context, attrs, defStyle);
 		init();
 	}
 
 	public void setOnPagingListener(OnPagingListener listener) {
 		mListener = listener;
 	}
 
 	public void setPage(final int page) {
 		mHandler.postDelayed(new Runnable() {
 			
 			@Override
 			public void run() {
 				Log.d(VIEW_LOG_TAG, "set page" + page);
 				int distance = getPositionByPage(page) - getCurrentTop();
 				smoothScrollBy(distance, mScrollDuration * (Math.abs(page - getFirstVisiblePosition()) + 1));
 			}
 		}, POST_DELAY_TIME);
 	}
 
 	private int getPositionByPage(int page) {
 		return getHeight() * page;
 	}
 	
 	int getPage() {
 		return Math.round((getCurrentTop() * 1.0f) / (getHeight() * 1.0f));
 	}
 
 	private void init() {
 		setOnScrollListener(this);
 		ViewTreeObserver observer = getViewTreeObserver();
 		observer.addOnGlobalLayoutListener(mLayoutListener);
 	}
 	
 	@SuppressLint("NewApi")
 	@SuppressWarnings("deprecation")
 	public void addOnGlobalLayoutListener(OnGlobalLayoutListener listener) {
 		ViewTreeObserver observer = getViewTreeObserver();
 		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
 			observer.removeOnGlobalLayoutListener(mLayoutListener);
 		} else {
 			observer.removeGlobalOnLayoutListener(mLayoutListener);
 		}
 		observer.addOnGlobalLayoutListener(listener);
 		observer.addOnGlobalLayoutListener(mLayoutListener);
 	}
 
 	int startY;
 	int getCurrentTop() {
 		int pos = getFirstVisiblePosition();
 		int top = 0;
 		View firstView = getChildAt(0);
 		if (firstView != null) {
 			top = firstView.getTop();
 		}
 		return getHeight() * pos - top;
 		
 	}
 	@Override
 	public void onScrollStateChanged(AbsListView view, int scrollState) {
 		switch (scrollState) {
 
 		case OnScrollListener.SCROLL_STATE_IDLE:
 			if (mFlinged) {
 				mFlinged = false;
 				if (mListener != null) {
					mListener.onScrollFinish(mPage);
 				}
 			} else {
 				int page = getPage();
 				setPage(page);
 
 				if (mListener != null) {
					mListener.onScrollFinish(mPage);
 				}
 			}
 
 			break;
 			
 		case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
 			startY = getCurrentTop();
 			
 			break;
 
 		case OnScrollListener.SCROLL_STATE_FLING:
 			mFlinged = true;
 			int currentY = getCurrentTop();
 			
 			int page = getFirstVisiblePosition();
 			Adapter adapter = getAdapter();
 			int count = adapter != null ? adapter.getCount() : 0;
 			if (currentY > startY && page + 1 < count) {
 				setPage(page + 1);
			} else if (currentY < startY && page > 0) {
 				setPage(page);
 			} else {
				setPage(page);
 			}
 
 			if (mListener != null) {
				mListener.onScrollStart(mPage);
 			}
 
 			break;
 		}
 	}
 
 	@Override
 	public void onScroll(AbsListView view, int firstVisibleItem,
 			int visibleItemCount, int totalItemCount) {
 		if (mListener != null) {
			mListener.onNextListLoad(mPage);
 		}
 	}
 }
