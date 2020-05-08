 package com.egeniq.widget;
 
 import android.content.Context;
 import android.support.v4.view.ViewPager;
 import android.util.AttributeSet;
 import android.view.MotionEvent;
 
 /**
  * ViewPager that can be nested inside another ViewPager and intercepts all swipe
  * events until it has reached its bounds.
  */
 public class EmbeddedViewPager extends ViewPager {
     /**
      * Scroll start.
      */
     private float _startX = 0;
    private boolean _isDragging = false;
     
     /**
      * Constructor.
      * 
      * @param context
      */
     public EmbeddedViewPager(Context context) {
         super(context);
     }
 
     /**
      * Constructor.
      * 
      * @param context
      * @param attrs
      */
     public EmbeddedViewPager(Context context, AttributeSet attrs) {
         super(context, attrs);
     }
     
     /**
      * Intercept action down event. We might not get this event in our onTouchEvent if
      * there is a click listener on the view inside the scroll view.
      */
     @Override
     public boolean onInterceptTouchEvent(MotionEvent ev) {
         // Start of (possible) scroll gesture
         if (ev.getActionMasked() == MotionEvent.ACTION_DOWN) {
             getParent().requestDisallowInterceptTouchEvent(true); // stop parent from intercepting the motion event                   
             _startX = ev.getX();
            _isDragging = false;
         }
         
         return super.onInterceptTouchEvent(ev);
     }
    
     /**
      * Handle touch event. Don't let parent intercept event if we can scroll ourselves.
      */
     public boolean onTouchEvent(MotionEvent ev) {
        if (!_isDragging && ev.getActionMasked() == MotionEvent.ACTION_MOVE) {
            if (_startX != ev.getX()) {
                int scrollX = getScrollX();
                boolean result = super.onTouchEvent(ev);
                if (scrollX != getScrollX()) {
                    _isDragging = true;
                    return result;
                } else {
                    getParent().requestDisallowInterceptTouchEvent(false); // let parent take over
                    return false;
                }
            }
         }
         
         return super.onTouchEvent(ev);
     }
 }
