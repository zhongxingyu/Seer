 package com.quanleimu.widget;
 
 import android.content.Context;
 import android.graphics.Canvas;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.MeasureSpec;
 import android.view.animation.Animation;
 import android.view.animation.LinearInterpolator;
 import android.view.animation.RotateAnimation;
 import android.widget.ScrollView;
 
 public class PullableScrollView extends ScrollView
 {
 	/* member class not found */
 	private enum PULL_STATE {
 		PULL_STATE_IDLE,
 		
 		PULL_STATE_PULLDOWN2PREV,
 		PULL_STATE_RELEASE2PREV,
 		PULL_STATE_LOADING_PREV,
 		
 		PULL_STATE_PULLUP2NEXT,
 		PULL_STATE_RELEASE2NEXT,
 		PULL_STATE_LOADING_NEXT
 	};
 
 	/* member class not found */
 	public interface PullNotifier {
 		
 		public View getHeaderView();
 		public View getFooterView();
 		public View getContentView();
 
 		public void startAnnimation(Animation animation, boolean isHeader);
 		public void stopAnimation();
 
 
 		public void beginLoadingNextView();
 		public void beginLoadingPrevView();
 		
 		public boolean hasPrev();
 		public boolean hasNext();
 	};
 
 	private static int FLIP_DISTANCE_PIXEL = 30;
 	private static int MAX_PULL_DISTANCE_PIXEL = 200;
 	private RotateAnimation mFlipAnimation;
 	private float mLastBottomY;
 	private float mLastMotionY;
 	private float mLastTopY;
 	private PullNotifier mPullNotifier;
 	private RotateAnimation mReverseFlipAnimation;
 	private PULL_STATE mState;
 	private int mViewHeight;
 	private int mResponsePart;
 	private boolean mHasReseted = false;
 	private boolean mDuringTouch = false;
 
 	public PullableScrollView(Context context)
 	{
 		super(context);
 		
 		mPullNotifier = null;
 		mViewHeight = 0;
 		mResponsePart = 0;
 		mState = PULL_STATE.PULL_STATE_IDLE;
 		
 		init();
 	}
 
 	public PullableScrollView(Context context, AttributeSet attributeset)
 	{
 		super(context, attributeset);
 		
 		mPullNotifier = null;
 		mViewHeight = 0;
 		mResponsePart = 0;
 		mState = PULL_STATE.PULL_STATE_IDLE;
 		
 		init();
 	}
 
 	public PullableScrollView(Context context, AttributeSet attributeset, int i)
 	{
 		super(context, attributeset, i);
 		
 		mPullNotifier = null;
 		mViewHeight = 0;
 		mResponsePart = 0;
 		mState = PULL_STATE.PULL_STATE_IDLE;
 		
 		init();
 	}
 
 	private void applyHeaderPadding(MotionEvent motionevent)
 	{
 		if (mPullNotifier == null)
 			return;
 		
 		if(mPullNotifier.hasPrev()){
 			int topPadding = (int)((motionevent.getY() - mLastMotionY) + mLastTopY);
 			if (topPadding < 0)
 				topPadding = 0;
 			else if (topPadding > MAX_PULL_DISTANCE_PIXEL)
 				topPadding = MAX_PULL_DISTANCE_PIXEL;
 			setHeaderTopPadding(topPadding);
 		}
 		
 		if(mPullNotifier.hasNext()){
 			int bottomPadding = (int)(((float)mViewHeight - mLastBottomY - motionevent.getY()) + mLastMotionY);
 			if (bottomPadding < 0)
 				bottomPadding = 0;
 			else if (bottomPadding > MAX_PULL_DISTANCE_PIXEL)
 				bottomPadding = MAX_PULL_DISTANCE_PIXEL;
 	
 			setFooterBottomPadding(bottomPadding);
 		}
 	}
 
 	private void updateTopAndBottom()
 	{
 		mLastTopY = getChildAt(0).getTop();
 		mLastBottomY = getChildAt(0).getBottom();
 		float scrollY = getScrollY();
 		
 		if(null != mPullNotifier){
 			View view = mPullNotifier.getHeaderView();
 			if (view != null)
 				mLastTopY = view.getTop();
 			View view1 = mPullNotifier.getFooterView();
 			if (view1 != null)
 				mLastBottomY = view1.getBottom();
 		}
 		
 		mLastTopY = mLastTopY - scrollY;
 		mLastBottomY = mLastBottomY - scrollY;
 	}
 
 	/*public void draw(Canvas canvas)
 	{
 		if (!adjustContentPadding()){
 			super.draw(canvas);
 		}
 	}*/
 
 	
 	//view status: <0:totally visible 0:partly visible >0:totally invisible
 	protected int getFooterStatus()
 	{
 		int status = 1;
 		
 		if(null != mPullNotifier && mPullNotifier.hasNext()){
 			View view = mPullNotifier.getFooterView();
 			if (view != null)
 			{
 				int footerTop = view.getTop() - getScrollY();
 				int footerHeight = view.getHeight() - view.getPaddingBottom();
 				
 				status = footerTop  - mViewHeight + 1;
 				int bottomStatus = status + footerHeight;
 				
 	
 				if(status < 0 && bottomStatus + FLIP_DISTANCE_PIXEL > 0)
 					status = 0;
 				
 				//Log.d("PullableScrollView", "footerTop = "+footerTop+", footHeight = "+footerHeight+"frameHeight = "+mViewHeight+"status ="+status+"bottomStatus"+bottomStatus);
 			}
 		}
 		
 		return status;
 	}
 
 	protected int getHeaderStatus()
 	{
 		int status = 1;
 		
 		if(null != mPullNotifier && mPullNotifier.hasPrev()){
 			View view = mPullNotifier.getHeaderView();
 			if (view != null)
 			{
 				status = getScrollY() - (view.getBottom() - view.getPaddingTop()) + 1;
 				int statusTop = getScrollY() - view.getTop();
 				
 				if(status < 0 && (statusTop > 0 || status + FLIP_DISTANCE_PIXEL > 0))
 					status = 0;
 				
 				//Log.d("PullableScrollView", "status ="+status+"statusTop"+statusTop);
 			}
 		}
 		
 		return status;
 	}
 
 	void init()
 	{
 		mViewHeight = getHeight();
 		mFlipAnimation = new RotateAnimation(0.0F, -180F, 1, 0.5F, 1, 0.5F);
 		mFlipAnimation.setInterpolator(new LinearInterpolator());
 		mFlipAnimation.setDuration(250L);
 		mFlipAnimation.setFillAfter(true);
 		mReverseFlipAnimation = new RotateAnimation(-180F, 0.0F, 1, 0.5F, 1, 0.5F);
 		mReverseFlipAnimation.setInterpolator(new LinearInterpolator());
 		mReverseFlipAnimation.setDuration(250L);
 		mReverseFlipAnimation.setFillAfter(true);
 		
 		setSmoothScrollingEnabled(true);
 	}
 
 /*	protected void onDraw(Canvas canvas)
 	{
 		if (!adjustContentPadding()){
 			super.onDraw(canvas);
 		}
 	}
 */
 	public void onNewViewLoaded(boolean bPrev)
 	{		
 		if(bPrev)
 			scrollToContentHeader(true);
 		else
 			scrollToContentFooter();
 		
 		mState = PULL_STATE.PULL_STATE_IDLE;
 	}
 	
 	@Override
 	protected void onMeasure(int measureWidthSpec, int measureHeightSpec){
 
 		mViewHeight = MeasureSpec.getSize(measureHeightSpec);
 		adjustContentPadding();
 		super.onMeasure(measureWidthSpec, measureHeightSpec);
 	}
 	
 	
 	@Override 
 	protected void onLayout (boolean changed, int left, int top, int right, int bottom) {
 
 		super.onLayout(changed, left, top, right, bottom);
 	}
 
 	public boolean onTouchEvent(MotionEvent motionevent)
 	{
 		motionevent.getAction();
 		
 		switch(motionevent.getAction()){
 			case MotionEvent.ACTION_DOWN:
 				mDuringTouch = true;
 				mLastMotionY = motionevent.getY();
 				updateTopAndBottom();
 				break;
 	
 			case MotionEvent.ACTION_MOVE:
 				applyHeaderPadding(motionevent);
 				
 				if(mState != PULL_STATE.PULL_STATE_LOADING_NEXT && mState != PULL_STATE.PULL_STATE_LOADING_PREV){
 					//Log.d("PullableScrollView", "it is in onTouchEvent");
 					
 					if(getHeaderStatus() <= 0)
 						mResponsePart = -1;
 					else if(getFooterStatus() <= 0)
 						mResponsePart = 1;
 					else
 						mResponsePart = 0;
 					
 					if (mResponsePart == -1)
 					{
 						if (mState == PULL_STATE.PULL_STATE_IDLE)
 							mState = PULL_STATE.PULL_STATE_PULLDOWN2PREV;
 						else
 						if (mState == PULL_STATE.PULL_STATE_PULLDOWN2PREV)
 						{
 							if(getHeaderStatus() < 0){
 								if (mPullNotifier != null)
 									mPullNotifier.startAnnimation(mFlipAnimation, true);
 								mState = PULL_STATE.PULL_STATE_RELEASE2PREV;
 							}
 						} else
 						if (mState == PULL_STATE.PULL_STATE_RELEASE2PREV)
 						{
 							if (getHeaderStatus() == 0)
 							{
 								if (mPullNotifier != null)
 									mPullNotifier.startAnnimation(mReverseFlipAnimation, true);
 								mState = PULL_STATE.PULL_STATE_PULLDOWN2PREV;
 							}
 						} else
 						{
 							mState = PULL_STATE.PULL_STATE_IDLE;
 							if (mPullNotifier != null)
 								mPullNotifier.stopAnimation();
 						}
 					} else if (mResponsePart == 1)
 					{
 						if (mState == PULL_STATE.PULL_STATE_IDLE)
 							mState = PULL_STATE.PULL_STATE_PULLUP2NEXT;
 						else
 						if (mState == PULL_STATE.PULL_STATE_PULLUP2NEXT)
 						{
 							if(getFooterStatus() < 0){
 								if (mPullNotifier != null)
 									mPullNotifier.startAnnimation(mFlipAnimation, false);
 								mState = PULL_STATE.PULL_STATE_RELEASE2NEXT;
 							}
 						} else if (mState == PULL_STATE.PULL_STATE_RELEASE2NEXT)
 						{
 							if (getFooterStatus() == 0)
 							{
 								if (mPullNotifier != null)
 									mPullNotifier.startAnnimation(mReverseFlipAnimation, false);
 								mState = PULL_STATE.PULL_STATE_PULLUP2NEXT;
 							}
 						} else
 						{
 							mState = PULL_STATE.PULL_STATE_IDLE;
 							if (mPullNotifier != null)
 								mPullNotifier.stopAnimation();
 						}
 					}/* else
 					{
 						mState = PULL_STATE.PULL_STATE_IDLE;
 						if (mPullNotifier != null)
 							mPullNotifier.stopAnimation();
 					}*/
 				}
 				break;
 				
 			//case MotionEvent.ACTION_CANCEL:
 			case MotionEvent.ACTION_UP:
 				if (mPullNotifier != null)
 				{
 					setHeaderTopPadding(0);
 					setFooterBottomPadding(0);
 					mPullNotifier.stopAnimation();
 					if (mState == PULL_STATE.PULL_STATE_RELEASE2NEXT){
 						mPullNotifier.beginLoadingNextView();
 						mState = PULL_STATE.PULL_STATE_LOADING_NEXT;
 					}
 					else if (mState == PULL_STATE.PULL_STATE_RELEASE2PREV){
 						mPullNotifier.beginLoadingPrevView();
 						mState = PULL_STATE.PULL_STATE_LOADING_PREV;
 					}
 					else{
 				        if(mState == PULL_STATE.PULL_STATE_PULLDOWN2PREV){
 				        	scrollToContentHeader(true);
 				        	mState = PULL_STATE.PULL_STATE_IDLE;
 				        }else if(mState == PULL_STATE.PULL_STATE_PULLUP2NEXT){
 				        	scrollToContentFooter();
 				        	mState = PULL_STATE.PULL_STATE_IDLE;
 				        } 
 					}
 				}
 				
 				mDuringTouch = false;
 				
 				//Log.d("PullableScrollView", "ACTION_UP event is handled");
 
 				break;
 			}
 
 		////Log.d("PullableScrollView", "event "+ motionevent.getAction()+" handled!!");
 		
 		return super.onTouchEvent(motionevent);
 	}
 
 	protected void scrollToContentFooter() {
 		//mAvoidEndlessScrollAPICall = true;
 		
 		int footerTop = mPullNotifier.getFooterView().getVisibility() == View.VISIBLE ? mPullNotifier.getFooterView().getTop() : mPullNotifier.getContentView().getBottom();
 		scrollTo(0, footerTop - mViewHeight);	
 		
 		//Log.d("PullableScrollView", "scrollToContentFooter() called!!!");
 	}
 
 	protected void scrollToContentHeader(boolean bSmoothScroll) {
 		//mAvoidEndlessScrollAPICall = true;
 		
 		int headerBottom = mPullNotifier.getHeaderView().getBottom();
 		if(bSmoothScroll)
 			scrollTo(0, headerBottom);	
 		else
 			this.smoothScrollTo(0, headerBottom);
 		
 		//Log.d("PullableScrollView", "scrollToContentHeader() called!!!");
 	}	
 
 	
 	@Override
 	public void computeScroll() {
 		super.computeScroll();
 		
 		if(!mDuringTouch && null != mPullNotifier){
			int maxY =  mPullNotifier.getFooterView().getTop()-mViewHeight;
			if(mPullNotifier.hasNext() && mState != PULL_STATE.PULL_STATE_LOADING_NEXT  && getScrollY() > maxY){
 				Log.d("PullableScrollView", "force scroll back-up, scrollY="+getScrollY()+", parentHeight="+mViewHeight+", footerTop="+mPullNotifier.getFooterView().getTop()+"maxY="+maxY);
 				scrollTo(0, maxY);
 			}
			else if(mPullNotifier.hasPrev() && mState != PULL_STATE.PULL_STATE_LOADING_PREV && getScrollY() < mPullNotifier.getHeaderView().getBottom()){
 				scrollTo(0, mPullNotifier.getHeaderView().getBottom());
 			}
 		}
 	}
 //	private boolean mAvoidEndlessScrollAPICall = false;
 //	private boolean mInFling = false;
 //	 @Override
 //	 public void onScrollChanged(int x, int y, int oldX, int oldY){
 //		 //Log.d("PullableScrollView", "duringTouch="+mDuringTouch+", scrollChanged("+x+", "+y+", "+oldX+", "+oldY+");");
 //		 
 //
 //				if(y > mPullNotifier.getFooterView().getTop()-mViewHeight || y < mPullNotifier.getHeaderView().getBottom()){
 //					
 //					int i = 0;
 //					int ii = i+i;
 //				}
 //					
 //
 //		 ////Log.d("PullableScrollView", "it is in onScrollChange");
 //		 
 ////		 if(	!mAvoidEndlessScrollAPICall 
 ////				 && (	mState == PULL_STATE.PULL_STATE_IDLE 
 ////				 		|| (	!mDuringTouch 
 ////				 				&& mState != PULL_STATE.PULL_STATE_LOADING_NEXT 
 ////				 				&& mState != PULL_STATE.PULL_STATE_LOADING_PREV) )){
 ////			 			 
 ////			 if(getHeaderStatus() <= 0){
 ////				 scrollToContentHeader(true);				 
 ////			 }else if(getFooterStatus() <= 0){
 ////				 scrollToContentFooter();
 ////			 }
 ////			 
 ////			 mState = PULL_STATE.PULL_STATE_IDLE;
 ////		 }
 ////		 
 ////		 mAvoidEndlessScrollAPICall = false;
 //	 }
 
 //	@Override
 //	protected void onAnimationStart(){
 //		super.onAnimationStart();
 //	}
 //	
 //	@Override
 //	protected void onAnimationEnd(){
 //		super.onAnimationEnd();
 //	}
 	
 	@Override
 	public void scrollTo(int i, int j)
 	{	
 		if(!mDuringTouch && null != mPullNotifier){
			int maxY =  mPullNotifier.getFooterView().getTop()-mViewHeight;
			if(mPullNotifier.hasNext() && mState != PULL_STATE.PULL_STATE_LOADING_NEXT && getScrollY() > maxY){
 				Log.d("PullableScrollView", "scroll_y modified to maxY, scrollY="+getScrollY()+", parentHeight="+mViewHeight+", footerTop="+mPullNotifier.getFooterView().getTop()+", contentBottom="+mPullNotifier.getContentView().getBottom()+", wholeHeight="+(getBottom()-getTop())+", maxY="+maxY);
 				j = maxY;
 			}
			else if(mPullNotifier.hasPrev() && mState != PULL_STATE.PULL_STATE_LOADING_PREV && getScrollY() < mPullNotifier.getHeaderView().getBottom()){
 				j = mPullNotifier.getHeaderView().getBottom();
 			}
 		}
 		
 		Log.d("PullableScrollView", "scrollTo("+i+","+j+") when mDuringTouch="+mDuringTouch+",headerBottom="+mPullNotifier.getHeaderView().getBottom()+",footerTop="+(mPullNotifier.getFooterView().getTop()-mViewHeight));
 		
 		super.scrollTo(i, j);
 	}
 	 
 	 @Override
 	 public void fling(int velocityY){
 		 ////Log.d("PullableScrollView", "fling("+velocityY+") called!!");
 		 super.fling(velocityY);
 	 }
 
 	protected void setFooterBottomPadding(int i)
 	{
 		View view = mPullNotifier.getFooterView();
 		if (view != null)
 			view.setPadding(view.getPaddingLeft(), view.getPaddingTop(), view.getPaddingRight(), i);
 	}
 
 	protected void setHeaderTopPadding(int i)
 	{
 		View view = mPullNotifier.getHeaderView();
 		if (view != null)
 			view.setPadding(view.getPaddingLeft(), i, view.getPaddingRight(), view.getPaddingBottom());
 	}
 
 	public void setPullNotifier(PullNotifier pullnotifier)
 	{
 		mPullNotifier = pullnotifier;
 	}
 
 	private void adjustContentPadding(){
     	if(!mHasReseted )
     	{   
     		if(null != mPullNotifier){
 		        View viewContent = mPullNotifier.getContentView();
 		        int contentHeight = viewContent.getMeasuredHeight();
 		        if(mViewHeight > 0 && contentHeight > 0){
 		        	if(contentHeight < mViewHeight){
 		        		viewContent.setPadding(viewContent.getPaddingLeft(), viewContent.getPaddingTop(), viewContent.getPaddingRight(), mViewHeight - contentHeight + 10);
 		        	}
 		        	scrollToContentHeader(false);
 		        	mHasReseted = true;
 		        }
     		}
     	}
 	}
 }
