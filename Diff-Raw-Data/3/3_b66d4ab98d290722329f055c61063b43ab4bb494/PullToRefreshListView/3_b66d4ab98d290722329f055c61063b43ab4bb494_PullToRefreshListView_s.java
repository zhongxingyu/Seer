 package com.quanleimu.widget;
 
 
 import android.content.Context;
 import android.content.res.TypedArray;
 import android.graphics.Canvas;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.MotionEvent;
 import android.view.VelocityTracker;
 import android.view.View;
 import android.view.ViewConfiguration;
 import android.view.ViewGroup;
 import android.view.animation.LinearInterpolator;
 import android.view.animation.RotateAnimation;
 import android.widget.AbsListView;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.ListAdapter;
 import android.widget.ListView;
 import android.widget.ProgressBar;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 import android.widget.AbsListView.OnScrollListener;
 
 import com.quanleimu.activity.R;
 
 public class PullToRefreshListView extends ListView implements OnScrollListener {
 
     private static final int TAP_TO_REFRESH = 1;
     private static final int PULL_TO_REFRESH = 2;
     private static final int RELEASE_TO_REFRESH = 3;
     private static final int REFRESHING = 4;
  
     private static final int SCROLLDOWN_TO_GETMORE = 5;
     private static final int GETTING_MORE = 6;
     
     private static final int DAY_MS = 24*60*60*1000;
     private static final int HOUR_MS = 60*60*1000;
     private static final int MINUTE_MS = 60*1000;
     private static final String TAG = "PullToRefreshListView";
 
     private OnRefreshListener mOnRefreshListener;
     private OnGetmoreListener mGetMoreListener;
     /**
      * Listener that will receive notifications every time the list scrolls.
      */
     private OnScrollListener mOnScrollListener;
     private LayoutInflater mInflater;
     
     private RelativeLayout mRefreshView;
     private TextView mRefreshViewText;
     private ImageView mRefreshViewImage;
     private ProgressBar mRefreshViewProgress;
     private TextView mRefreshViewLastUpdated;
     
     private LinearLayout mGapHeaderView = null;
     
     private RelativeLayout mGetmoreView = null;
     //private TextView mGetmoreViewText;
 
     private int mCurrentScrollState;
     private int mRefreshState;
     private int mGetMoreState = SCROLLDOWN_TO_GETMORE;
 
     private RotateAnimation mFlipAnimation;
     private RotateAnimation mReverseFlipAnimation;
 
     private int mRefreshViewHeight;
     private int mRefreshOriginalTopPadding;
     private int mLastMotionY;
     private int mDownY;
 
     private boolean mBounceHack;
     private boolean mTouchDown = false;
     private boolean mHasMore = true;
     
     private boolean mGetMoreAllowPolicy=true;
     private boolean mAllowGetMore = true;
     private boolean mEnableHeader = true;
     
     private boolean mNeedBlankGapHeader = false;
     
     private long mLastUpdateTimeMs;
     
     private VelocityTracker mVelocityTracker;
 	private int mMaximumVelocity;
     private static final int SNAP_VELOCITY = 1000;
 
     public PullToRefreshListView(Context context) {
         super(context);
         init(context);
     }
 
     public PullToRefreshListView(Context context, AttributeSet attrs) {
         super(context, attrs);
         
         TypedArray styledAttrs = context.obtainStyledAttributes(attrs,
 				R.styleable.PullToRefreshListView);
         mGetMoreAllowPolicy = styledAttrs.getBoolean(R.styleable.PullToRefreshListView_getmore, true);
         
         mNeedBlankGapHeader = styledAttrs.getBoolean(R.styleable.PullToRefreshListView_need_gap_header, false);
         
         init(context);
     }
 
     public PullToRefreshListView(Context context, AttributeSet attrs, int defStyle) {
         super(context, attrs, defStyle);
  
         TypedArray styledAttrs = context.obtainStyledAttributes(attrs,
 				R.styleable.PullToRefreshListView);
         mGetMoreAllowPolicy = styledAttrs.getBoolean(R.styleable.PullToRefreshListView_getmore, true);
         
         mNeedBlankGapHeader = styledAttrs.getBoolean(R.styleable.PullToRefreshListView_need_gap_header, false);
         
         init(context);
     }
 
     private void init(Context context) {
         // Load all of the animations we need in code rather than through XML
         mFlipAnimation = new RotateAnimation(0, -180,
                 RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                 RotateAnimation.RELATIVE_TO_SELF, 0.5f);
         mFlipAnimation.setInterpolator(new LinearInterpolator());
         mFlipAnimation.setDuration(250);
         mFlipAnimation.setFillAfter(true);
         mReverseFlipAnimation = new RotateAnimation(-180, 0,
                 RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                 RotateAnimation.RELATIVE_TO_SELF, 0.5f);
         mReverseFlipAnimation.setInterpolator(new LinearInterpolator());
         mReverseFlipAnimation.setDuration(250);
         mReverseFlipAnimation.setFillAfter(true);
 
         mInflater = (LayoutInflater) context.getSystemService(
                 Context.LAYOUT_INFLATER_SERVICE);
         
         mRefreshView = (RelativeLayout)mInflater.inflate(R.layout.pull_to_refresh_header, this, false);
         mRefreshViewText =
             (TextView) mRefreshView.findViewById(R.id.pull_to_refresh_text);
         mRefreshViewImage =
             (ImageView) mRefreshView.findViewById(R.id.pull_to_refresh_image);
         mRefreshViewProgress =
             (ProgressBar) mRefreshView.findViewById(R.id.pull_to_refresh_progress);
         mRefreshViewLastUpdated =
             (TextView) mRefreshView.findViewById(R.id.pull_to_refresh_updated_at);
 
         mRefreshViewImage.setMinimumHeight(50);
         mRefreshView.setOnClickListener(new OnClickRefreshListener());
         mRefreshOriginalTopPadding = mRefreshView.getPaddingTop();
 
         mRefreshState = TAP_TO_REFRESH;
 
         addHeaderView(mRefreshView);
         
         if(mNeedBlankGapHeader){
         	mGapHeaderView=(LinearLayout)mInflater.inflate(R.layout.pull_to_refresh_gap_header, this, false);
         	addHeaderView(mGapHeaderView);
         }
         
         mAllowGetMore = true;
         if(mAllowGetMore){
 	        mGetmoreView = (RelativeLayout)mInflater.inflate(R.layout.pull_to_refresh_footer, this, false);
 	        //mGetmoreViewText = (TextView)mGetmoreView.findViewById(R.id.pulldown_to_getmore);
 	        addFooterView(mGetmoreView, null, true);
         }
 
         super.setOnScrollListener(this);
 
         measureView(mRefreshView);
         mRefreshViewHeight = mRefreshView.getMeasuredHeight();
         
         mLastUpdateTimeMs = System.currentTimeMillis();
         
         resetHeader();
         
         //for velocity_tracker
 		final ViewConfiguration configuration = ViewConfiguration
 				.get(getContext());
 		configuration.getScaledTouchSlop();
 		mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
     }
 
     private void updateFooter(boolean hasMore){
     	if(mAllowGetMore){
 	    	mHasMore = hasMore;
 	    	if(mHasMore){
 	    		((TextView)mGetmoreView.findViewById(R.id.pulldown_to_getmore)).setText(R.string.scrolldown_to_getmore);
 	    	}else{
 	    		((TextView)mGetmoreView.findViewById(R.id.pulldown_to_getmore)).setText(R.string.scrolldown_to_getmore_nomore);
 	    	}
     	}
     }
     
     @Override
     protected void onAttachedToWindow() {
     	super.onAttachedToWindow();
     	
         if(mGetMoreState == GETTING_MORE){
         	if(null != getAdapter() && mAllowGetMore && getAdapter().getCount() == getLastVisiblePosition()){
         		onGetMore();
         	}else{
         		mGetMoreState = SCROLLDOWN_TO_GETMORE;
         	}
         }
         else if(mRefreshState ==  REFRESHING){
         	if(0 == getFirstVisiblePosition())
         		onRefresh();
         	else
         		mRefreshState = PULL_TO_REFRESH;
         }
         
         resetHeader();
         updateFooter(mHasMore);
         
         invalidate();
     }
 
 	@Override
 	protected void onDraw(Canvas canvas){
 		mAllowGetMore = mGetMoreAllowPolicy&judgeListFull();
 		if(!mAllowGetMore && mGetmoreView.getVisibility() != View.GONE){
 			mGetmoreView.setVisibility(View.GONE);
 			((TextView)mGetmoreView.findViewById(R.id.pulldown_to_getmore)).setVisibility(View.GONE);
 		}else if(mAllowGetMore && mGetmoreView.getVisibility() != View.VISIBLE){
 			mGetmoreView.setVisibility(View.VISIBLE);
 			((TextView)mGetmoreView.findViewById(R.id.pulldown_to_getmore)).setVisibility(View.VISIBLE);			
 		}
 		
 		super.onDraw(canvas);
     }
     
 	public void fireRefresh(){
 		checkLastUpdateTime();
 		prepareForRefresh();
 		super.setSelection(0);
 		
 		if(null != mOnRefreshListener){
 			mOnRefreshListener.onRefresh();
 		}
 	}
 	
 	
     public void setPullToRefreshEnabled(boolean enable){
     	if(mEnableHeader != enable){
     		if(enable){
     			mRefreshView.setVisibility(View.VISIBLE);
     		    mRefreshViewText.setVisibility(View.VISIBLE);
     		    
     		    //mRefreshViewImage.setVisibility(View.VISIBLE);
     		    
     		    if(mRefreshState == REFRESHING)
     		    	mRefreshViewProgress.setVisibility(View.VISIBLE);
     		    
     		    if(mRefreshState == PULL_TO_REFRESH && mRefreshState == RELEASE_TO_REFRESH)
     		    	mRefreshViewLastUpdated.setVisibility(View.VISIBLE);
     		}else{
     			mRefreshView.setVisibility(View.GONE);
     		    mRefreshViewText.setVisibility(View.GONE);
     		    mRefreshViewImage.setVisibility(View.GONE);
     		    mRefreshViewProgress.setVisibility(View.GONE);
     		    mRefreshViewLastUpdated.setVisibility(View.GONE);
     		}
     		
     		mEnableHeader = enable;
     	}
     }
     
 //    @Override
 //    protected void handleDataChanged() {
 //    	if( this.getParent()  instanceof View){
 //    		View parentView = (View)getParent();
 //    		
 //    		int heightParent = parentView.getHeight();
 //    		int heightList = 0;
 //    		if(getCount() > 0){
 //    			heightList = getCount() * .getHeight();
 //    		}
 //    		if(heightParent < heightList){
 //    		}
 //    	}
 //    	super.handleDataChanged();
 //    }
     
     @Override
     public void setAdapter(ListAdapter adapter) {
         super.setAdapter(adapter);
 
         setSelectionFromTop(1, 0);
     }
     
 
     @Override 
     public void setSelection(int selection){
     	if(mNeedBlankGapHeader){
     		super.setSelection(selection-getHeaderViewsCount() + 1);
     	}else{
     		super.setSelection(selection-getHeaderViewsCount());
     	}
     }
     
     public void setSelectionFromHeader(int selection){
     	setSelectionFromTop(selection, getHeaderViewsCount());
     }
 
     /**
      * Set the listener that will receive notifications every time the list
      * scrolls.
      * 
      * @param l The scroll listener. 
      */
     @Override
     public void setOnScrollListener(AbsListView.OnScrollListener l) {
         mOnScrollListener = l;
     }
 
     /**
      * Register a callback to be invoked when this list should be refreshed.
      * 
      * @param onRefreshListener The callback to run.
      */
     public void setOnRefreshListener(OnRefreshListener onRefreshListener) {
         mOnRefreshListener = onRefreshListener;
     }
     
     public void setOnGetMoreListener(OnGetmoreListener listener){
     	mGetMoreListener = listener; 
     }
     
     public void setOnListHeightMeasurer(OnListHeightMeasurer measurer){
     }
 
     /**
      * Set a text to represent when the list was last updated. 
      * @param lastUpdated Last updated at.
      */
     public void checkLastUpdateTime() {    
     	long time_diff = System.currentTimeMillis() - mLastUpdateTimeMs;
     	long nDays = time_diff / DAY_MS;
     	time_diff %= DAY_MS;
     	long nHours = time_diff / HOUR_MS;
     	time_diff %= HOUR_MS;
     	long nMinutes = time_diff / MINUTE_MS;
     	time_diff %= MINUTE_MS;
     	String strLastUpdate = "最后更新于:";
     	if(nDays > 0){
     		strLastUpdate += nDays + "天";
     	}
     	if(nHours > 0){
     		strLastUpdate += nHours + "小时";
     	}
     	
    		strLastUpdate += nMinutes + "分钟";
     	
 //   		strLastUpdate += nSeconds + "秒";
    		
     	strLastUpdate += "前";
     	
         mRefreshViewLastUpdated.setText(strLastUpdate);       
     }
 
     @Override
     public boolean onTouchEvent(MotionEvent event) {
     	
 		if (mVelocityTracker == null) {
 			mVelocityTracker = VelocityTracker.obtain();
 		}
 		mVelocityTracker.addMovement(event);
     	
         final int y = (int) event.getY();
         mBounceHack = false;
 
         switch (event.getAction()) {
             case MotionEvent.ACTION_UP:
                 if (!isVerticalScrollBarEnabled()) {
                     setVerticalScrollBarEnabled(true);
                 }
                 
                 if (getFirstVisiblePosition() < getHeaderViewsCount() && mEnableHeader && mRefreshState != REFRESHING) {
                     if (mRefreshState == RELEASE_TO_REFRESH){
                         // Initiate the refresh
                         mRefreshState = REFRESHING;
                         prepareForRefresh();
                         onRefresh();
                     } 
                     else /*if (mRefreshView.getBottom() < mRefreshViewHeight
                             || mRefreshView.getTop() <= 0)*/ {
                         // Abort refresh and scroll down below the refresh view
                     	mRefreshState=TAP_TO_REFRESH;
                         resetHeader();
                         setSelection(0);
                     }
                 }
                 else if(mAllowGetMore && this.getLastVisiblePosition() == this.getCount() - 1 && mGetMoreState != GETTING_MORE){
 //                	int bottom1 = mGetmoreView.getBottom();
 //                	int bottom2 = this.getBottom();
                     if ((mGetmoreView.getTop() + mGetmoreView.getHeight() *2 / 5 < this.getBottom())) {
                         // Initiate the refresh
                     	mGetMoreState = GETTING_MORE;                       
                         onGetMore();
                     }
                 }
                 mTouchDown = false;               
                 
                 break;
             case MotionEvent.ACTION_DOWN:
                 mLastMotionY = y;
                 mTouchDown = true;
                 mDownY = y;
                 break;
             case MotionEvent.ACTION_MOVE:
             	
             	if( mRefreshState != REFRESHING
             		&&(mCurrentScrollState == SCROLL_STATE_IDLE || !judgeListFull())){
             		if(y - mDownY > 10){
             			pullToRefresh(getFirstVisiblePosition());
             		}else{
             			mRefreshState = TAP_TO_REFRESH;
             			resetHeader();
             		}
             	}
 
             	applyHeaderPadding(event);
                 mTouchDown = true;
                 break;
             case MotionEvent.ACTION_CANCEL:
             	break;
         }
         
         return super.onTouchEvent(event);
     }
     
     private boolean judgeListFull(){
 //    	if(getParent() instanceof View){
 //    		View parentView = (View)getParent();
 //    		
 //    		int heightParent = parentView.getHeight();
 //    		
 //    		if(null != mMeasurer){
 //    			int heightList = mMeasurer.onMeasureListHeight();
 //    			if(heightList > heightParent)
 //    				return true;
 //    		}
 //    	}
 //    	
     	int visibleCount = getChildCount();
     	int allCount = 0;
     	if(null != getAdapter())
     		allCount = getAdapter().getCount() - getHeaderViewsCount() - getFooterViewsCount();
     	
     	boolean bRet = (visibleCount < allCount);
     	return bRet;
     }
 
     private void applyHeaderPadding(MotionEvent ev) {
         // getHistorySize has been available since API 1
     	
     	//Log.d("PullToRefreshListView", "scrollY: "+getScrollY()+", refresh state: "+mRefreshState);
     	
         if ( getScrollY() == 0 ) {
         	
             int pointerCount = ev.getHistorySize();
 
             if(pointerCount > 0){
             	for (int p = 0; p < pointerCount; p++) {
                     if (isVerticalFadingEdgeEnabled()) {
                         setVerticalScrollBarEnabled(false);
                     }
 
                     int historicalY = (int) ev.getHistoricalY(p);
 
                     // Calculate the padding to apply, we divide by 1.7 to
                     // simulate a more resistant effect during pull.
                     int topPadding = (int) (((historicalY - mLastMotionY)
                             - mRefreshViewHeight) / 1.7);
 
                     //Log.d("PullToRefreshListView", "calculated top padding: "+topPadding);
                     
                     if(topPadding >= 0)
                     	mRefreshView.setPadding(
                             mRefreshView.getPaddingLeft(),
                             topPadding,
                             mRefreshView.getPaddingRight(),
                             mRefreshView.getPaddingBottom());
             	}
             }else{
                 int topPadding = (int) (((ev.getY() - mLastMotionY)
                         - mRefreshViewHeight) / 1.7);
 
                // Log.d("PullToRefreshListView", "calculated top padding: "+topPadding);
                 
                 if(topPadding >= 0)
                 	mRefreshView.setPadding(
                         mRefreshView.getPaddingLeft(),
                         topPadding,
                         mRefreshView.getPaddingRight(),
                         mRefreshView.getPaddingBottom());
             }
         }
     }
 
     /**
      * Sets the header padding back to original size.
      */
     private void resetHeaderPadding() {
     	
     	int header_top_gap = 0;
     	if(mNeedBlankGapHeader){
     		header_top_gap = mGapHeaderView.getMeasuredHeight() + mGapHeaderView.getPaddingBottom() + mGapHeaderView.getPaddingTop();
     	}
     	
         mRefreshView.setPadding(
                 mRefreshView.getPaddingLeft(),
                 mRefreshOriginalTopPadding + header_top_gap,
                 mRefreshView.getPaddingRight(),
                 mRefreshView.getPaddingBottom());
     }
 
     /**
      * Resets the header to the original state.
      */
     private void resetHeader() {
         if (mRefreshState != TAP_TO_REFRESH) {
             mRefreshState = TAP_TO_REFRESH;
 
             openHeaderView();  
             
             resetHeaderPadding();
             
           // Clear the full rotation animation
           mRefreshViewImage.clearAnimation();
             
         }else
         {
 			mRefreshView.setVisibility(View.GONE);
 		    mRefreshViewText.setVisibility(View.GONE);
 		    mRefreshViewImage.setVisibility(View.GONE);
 		    mRefreshViewProgress.setVisibility(View.GONE);
 		    mRefreshViewLastUpdated.setVisibility(View.GONE);
 		    
 		    mRefreshView.setPadding(0, 0, 0, 0);
         }
     }
 
 	protected void openHeaderView() {
 		mRefreshView.setVisibility(View.VISIBLE);
 		
 		mRefreshViewText.setVisibility(View.VISIBLE);
         
 		if(mRefreshState == PULL_TO_REFRESH || mRefreshState == RELEASE_TO_REFRESH){
 			if(mRefreshViewImage.getVisibility() != View.VISIBLE)
 				mRefreshViewImage.setVisibility(View.VISIBLE);
 			if(mRefreshViewProgress.getVisibility() == View.VISIBLE)
 				mRefreshViewProgress.setVisibility(View.GONE);
 		}else if(mRefreshState == REFRESHING){
 			if(mRefreshViewProgress.getVisibility() != View.VISIBLE)
 				mRefreshViewProgress.setVisibility(View.VISIBLE);
 			if(mRefreshViewImage.getVisibility() == View.VISIBLE)
 				mRefreshViewImage.setVisibility(View.GONE);
 		}
 		
 		mRefreshViewLastUpdated.setVisibility(View.VISIBLE);	
 		
         // Set refresh view text to the pull label
         mRefreshViewText.setText(R.string.pull_to_refresh_tap_label);
         // Replace refresh drawable with arrow drawable
         mRefreshViewImage.setImageResource(R.drawable.ic_pulltorefresh_arrow);
 	}
 
     private void measureView(View child) {
         ViewGroup.LayoutParams p = child.getLayoutParams();
         if (p == null) {
             p = new ViewGroup.LayoutParams(
                     ViewGroup.LayoutParams.FILL_PARENT,
                     ViewGroup.LayoutParams.WRAP_CONTENT);
         }
 
         int childWidthSpec = ViewGroup.getChildMeasureSpec(0,
                 0 + 0, p.width);
         int lpHeight = p.height;
         int childHeightSpec;
         if (lpHeight > 0) {
             childHeightSpec = MeasureSpec.makeMeasureSpec(lpHeight, MeasureSpec.EXACTLY);
         } else {
             childHeightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
         }
         child.measure(childWidthSpec, childHeightSpec);
     }
 
     @Override
     public void onScroll(AbsListView view, int firstVisibleItem,
             int visibleItemCount, int totalItemCount) {
         // When the refresh view is completely visible, change the text to say
         // "Release to refresh..." and flip the arrow drawable.
         if (mEnableHeader 
         		&& mCurrentScrollState == SCROLL_STATE_TOUCH_SCROLL
                 && mRefreshState != REFRESHING && this.judgeListFull()) {
             pullToRefresh(firstVisibleItem);
         }/* else if (mCurrentScrollState == SCROLL_STATE_FLING
                 && firstVisibleItem == 0
                 && mRefreshState != REFRESHING) {
             setSelection(1);
             mBounceHack = true;
         } else if (mBounceHack && mCurrentScrollState == SCROLL_STATE_FLING) {
             setSelection(1);
         }*/else if(mCurrentScrollState == SCROLL_STATE_FLING){
         	
             if (mAllowGetMore
             		&& getLastVisiblePosition() == getCount() - 1 
             		&& mGetMoreState != GETTING_MORE 
             		&&  mGetmoreView.getBottom() <= this.getBottom()) {
                 // Initiate the refresh
             	mGetMoreState = GETTING_MORE;                       
                 onGetMore();
             }            
             else if (mEnableHeader && firstVisibleItem == 0) {
             	mVelocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
             	int velocityX = (int) mVelocityTracker.getXVelocity();
             	
             	//Log.d("on fling: ", "top y = " + mRefreshView.getTop());
             	
             	if(mRefreshState != REFRESHING 
             			&& velocityX > SNAP_VELOCITY
             			&& mRefreshView.getTop() >= 0){
 	                mRefreshState = REFRESHING;
 	                openHeaderView();
 	                checkLastUpdateTime();
 	                prepareForRefresh();
 	                onRefresh();
             	}
             	else if(mRefreshView.getTop() < 0){
                     mRefreshViewText.setText(R.string.pull_to_refresh_pull_label);
 //                    if (mRefreshState != TAP_TO_REFRESH) {
 //                        mRefreshViewImage.clearAnimation();
 //                        mRefreshViewImage.startAnimation(mReverseFlipAnimation);
 //                    }
                     mRefreshState = PULL_TO_REFRESH;
             	}
             	
             	mBounceHack = true;
             }
         }
 
         if (mOnScrollListener != null) {
             mOnScrollListener.onScroll(view, firstVisibleItem,
                     visibleItemCount, totalItemCount);
         }
     }
 
 	protected void pullToRefresh(int firstVisibleItem) {
 		if (firstVisibleItem < getFooterViewsCount()) {
 			
 			//Log.d("PullToRefreshListView", "first visible item: "+firstVisibleItem+", top: "+mRefreshView.getTop()+", mRefreshView.bottom: "+mRefreshView.getBottom()+", height: "+mRefreshView.getHeight()+", paddingTop: "+mRefreshView.getPaddingTop());
 			
 //		    if (mRefreshView.getBottom() >= mRefreshView.getHeight()) {
 			if (mRefreshView.getPaddingTop() > 20) {
 		    	if(mRefreshState != RELEASE_TO_REFRESH){
 		    		mRefreshState = RELEASE_TO_REFRESH;
 		    		openHeaderView();
 		            mRefreshViewText.setText(R.string.pull_to_refresh_release_label);
 		            mRefreshViewImage.clearAnimation();
 		            mRefreshViewImage.startAnimation(mFlipAnimation);
 		            checkLastUpdateTime();	                    
 		    	}
 		    }
 		    else if (mRefreshState != PULL_TO_REFRESH) {
 		    	boolean startAnimation = (mRefreshState != TAP_TO_REFRESH);
 		    	mRefreshState = PULL_TO_REFRESH;
 		    	openHeaderView();
 		        mRefreshViewText.setText(R.string.pull_to_refresh_pull_label);
 		        if (startAnimation) {
 		            mRefreshViewImage.clearAnimation();
 		            mRefreshViewImage.startAnimation(mReverseFlipAnimation);
 		        }
 		        checkLastUpdateTime();
 		    }
 		} else{
 		    mRefreshViewImage.setVisibility(View.GONE);
 		    resetHeader();
 		    
 //                if(firstVisibleItem+visibleItemCount==totalItemCount
 //                		&& mGetmoreView.getBottom() < this.getBottom() + 5){
 //                	mRefreshState = SCROLLDOWN_TO_GETMORE;
 //                }
 		}
 	}
 
     @Override
     public void onScrollStateChanged(AbsListView view, int scrollState) {
         mCurrentScrollState = scrollState;
 
         if (mCurrentScrollState == SCROLL_STATE_IDLE) {            
         	//Log.d("on fling: ", "onScrollStateChanged to SCROLL_STATE_IDLE, mRefreshState="+mRefreshState);
         	
             if(mBounceHack && !mTouchDown &&mRefreshState == PULL_TO_REFRESH){
                 //resetHeader();
                setSelection(0);
                 mBounceHack = false;
             }           
         }
 
         if (mOnScrollListener != null) {
             mOnScrollListener.onScrollStateChanged(view, scrollState);
         }
     }
 
     public void prepareForRefresh() {
         mRefreshView.setVisibility(View.VISIBLE);
         mRefreshViewText.setVisibility(View.VISIBLE);
         mRefreshViewImage.setVisibility(View.GONE);
         mRefreshViewLastUpdated.setVisibility(View.VISIBLE);
         
         resetHeaderPadding();
 
         // We need this hack, otherwise it will keep the previous drawable.
         mRefreshViewImage.setImageDrawable(null);
         mRefreshViewProgress.setVisibility(View.VISIBLE);
 
         // Set refresh view text to the refreshing label
         mRefreshViewText.setText(R.string.pull_to_refresh_refreshing_label);
 
         mRefreshState = REFRESHING;
     }
 
     public void onRefresh() {
         //Log.d(TAG, "onRefresh");
 
         if (mOnRefreshListener != null) {
             mOnRefreshListener.onRefresh();
         }
     }
     
     public void onGetMore(){
         //Log.d(TAG, "onGetMore");
 
         if (mHasMore && mGetMoreListener != null) {
         	mGetMoreListener.onGetMore();
         }else{
         	mGetMoreState = SCROLLDOWN_TO_GETMORE;  
         }
     }
 
     /**
      * Resets the list to a normal state after a refresh.
      * @param lastUpdated Last updated at.
      */
 //    public void onRefreshComplete(CharSequence lastUpdated) {
 //        setLastUpdated(lastUpdated);
 //        onRefreshComplete();
 //    }
 
     public void onFail(){
         if(mGetMoreState == GETTING_MORE){
         	mGetMoreState = SCROLLDOWN_TO_GETMORE;
         }
         
         if(mRefreshState ==  REFRESHING){
         	
             mRefreshState = TAP_TO_REFRESH;
             
             resetHeader();
         }
         
         invalidate();
     }
     
     /**
      * Resets the list to a normal state after a refresh.
      */
     public void onRefreshComplete() {        
         //Log.d(TAG, "onRefreshComplete");
         
         mLastUpdateTimeMs = System.currentTimeMillis();
 
         mRefreshState = TAP_TO_REFRESH;
         
         resetHeader();
 
         // If refresh view is visible when loading completes, scroll down to
         // the next item.
         if (mRefreshView.getBottom() > 0) {
             invalidateViews();
             if(mRefreshView.getTop() >= 0)
             setSelection(0);
         }
         
         updateFooter(true);        
     }
     
 	public enum E_GETMORE{
 		E_GETMORE_OK,
 		E_GETMORE_NO_MORE
 	};
     public void onGetMoreCompleted(E_GETMORE status){
     	boolean hasMore = true;
     	switch(status){
     	case E_GETMORE_OK:	    	
 	    	invalidateViews();
 	    	break;
     	case E_GETMORE_NO_MORE:
     		hasMore = false;
     		break;
     	}
     	
     	mGetMoreState = SCROLLDOWN_TO_GETMORE;
     	
     	updateFooter(hasMore);
     }
 
     /**
      * Invoked when the refresh view is clicked on. This is mainly used when
      * there's only a few items in the list and it's not possible to drag the
      * list.
      */
     private class OnClickRefreshListener implements OnClickListener {
 
         @Override
         public void onClick(View v) {
             if (mRefreshState != REFRESHING) {
                 prepareForRefresh();
                 onRefresh();
             }
         }
 
     }
 
     /**
      * Interface definition for a callback to be invoked when list should be
      * refreshed.
      */
     public interface OnRefreshListener {
         /**
          * Called when the list should be refreshed.
          * <p>
          * A call to {@link PullToRefreshListView #onRefreshComplete()} is
          * expected to indicate that the refresh has completed.
          */
         public void onRefresh();
     }
     
     
     public interface OnGetmoreListener{
     	public void onGetMore();
     }
     
     public interface OnListHeightMeasurer{
     	public int onMeasureListHeight();
     }
 }
