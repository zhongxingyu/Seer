 package edu.cmu.hcii.peer.page;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.Context;
 import android.graphics.Canvas;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.MotionEvent;
 import android.view.ViewGroup;
 import android.widget.ScrollView;
 
 public class StepPageScrollView extends ScrollView{
 	private String TAG = "StepPageScrollView";
 	
 	private int viewWidth;
 	private int viewHeight;
 	private ViewGroup stepPage;
 	private List<Integer> scrollIndex = null;
 	private int current_scrollIndex = 0;
 	
 	private static float SCROLL_DISTANCE = 0.7f; // % of screen that is scrolled normally
 	
 	/**
 	 * 
 	 * @param context
 	 * @param stepPageViewGroup
 	 */
 	public StepPageScrollView(Context context, ViewGroup stepPageViewGroup) {
 		super(context);
 		this.addView(stepPageViewGroup);
 		this.stepPage = stepPageViewGroup;
 		setScrollbarFadingEnabled(false);
 		this.setPadding(0, 0, 20, 0);
 	}
 
 	
 	
 	/**
 	 * @param context
 	 * @param attrs
 	 */
 	public StepPageScrollView(Context context, AttributeSet attrs) {
 		super(context, attrs);
 		// TODO Auto-generated constructor stub
 	}
 	
 	
 	
 	/**
 	 * 
 	 */
     @Override
     protected void onSizeChanged(int xNew, int yNew, int xOld, int yOld){
             super.onSizeChanged(xNew, yNew, xOld, yOld);
             viewWidth = xNew;
             viewHeight = yNew;
             
            //Log.v(TAG,"w,h: "+viewWidth+", "+viewHeight);
     }
     
    
     
     /**
      * 
      */
     @Override
     protected void onDraw(Canvas canvas) {
         super.onDraw(canvas);
         //Log.v(TAG,"onDraw - scrollY "+       this.getScrollY() + ", " + stepPage.getHeight());
 /*
         for (int i = 0; i< stepPage.getChildCount(); i++){
         	Rect r = new Rect();
         	View stepItem = stepPage.getChildAt(i);
         	
         	stepItem.getGlobalVisibleRect(r);
             //Log.v(TAG,"onDraw - child:"+i+"  Rect -"+" top:"+stepItem.getTop()+" bot:"+stepItem.getBottom() + " height:"+stepItem.getHeight());
 
             
         	
         	//this.getChildVisibleRect(stepPage.getChildAt(i), r, null);
             //Log.v(TAG,"onDraw - child:"+i+"  Rect -"+" top:"+r.top+" bot:"+r.bottom);
       	
         }
      */
         if (scrollIndex==null){
         	scrollIndex = getScrollIndex();
         	
         }
     }
     
     
     
     /**
      * gets scroll indexes
      * @return 
      */
 	private List<Integer> getScrollIndex(){
 		List<Integer> list = new ArrayList<Integer>();
 		int currentScroll = 0;
 		int currentChild = 0;
 		list.add(0);
 		
 		while (currentScroll < stepPage.getHeight() - viewHeight){			
 			// iterate through all children that are currently viewable
 				
 			while (stepPage.getChildAt(currentChild).getTop() < currentScroll + viewHeight ||
 				   stepPage.getChildAt(currentChild).getBottom() > currentScroll){
 					// if the entire height of the current child is currently visible
 					if (stepPage.getChildAt(currentChild).getBottom() <= currentScroll + viewHeight){ 	
 						// then everything is fine and move on to next child visible
 						currentChild++;		
 						if (currentChild > stepPage.getChildCount() - 1)
 							break;
 						
 					}// if entire height of this child is not currently visible	
 					else if (stepPage.getChildAt(currentChild).getBottom() > currentScroll + viewHeight){																				
 						
 						// if we are not already past the top of current obscured view
 						if (stepPage.getChildAt(currentChild).getTop() > currentScroll){
 							// then we scroll to the top of current obscured view
 							currentScroll = stepPage.getChildAt(currentChild).getTop();
 							list.add(Math.min(currentScroll, stepPage.getHeight() - viewHeight));
 							
 						}else{ //if we already past the top of current obscured view (aka the current view is larger than the viewHeight)
 							currentScroll += viewHeight * SCROLL_DISTANCE;
 							list.add(Math.min(currentScroll, stepPage.getHeight() - viewHeight));
 							
 						}
 					}
 						
 			
 			}
 					
 		}
 		
 		return list;
 		
 	}
 	
 	
 	
 	@Override
 	public boolean onTouchEvent(MotionEvent e){
 		if (e.getAction() == MotionEvent.ACTION_DOWN && e.getY() > viewHeight/2){
 			scrollDown();
 		}else if (e.getAction() == MotionEvent.ACTION_DOWN && e.getY() < viewHeight/2){
 			scrollUp();
 		}
 		
 		//smoothScrollTo(0, 200);
 		return false;
 		
 	}
 	
 	public StepPage getStepPage(){
 		return (StepPage) stepPage;
 	}
 	
 	/**
 	 * Scrolls up
 	 */
 	public void scrollUp(){
 		current_scrollIndex = Math.max(0, current_scrollIndex-1);
 		smoothScrollTo(0, scrollIndex.get(current_scrollIndex));
 	}
 	
 	
 	
 	/**
 	 * Scrolls down 
 	 */
 	public void scrollDown(){
 		current_scrollIndex = Math.min(scrollIndex.size() - 1, current_scrollIndex+1);
 		smoothScrollTo(0, scrollIndex.get(current_scrollIndex));
 	}
 }
