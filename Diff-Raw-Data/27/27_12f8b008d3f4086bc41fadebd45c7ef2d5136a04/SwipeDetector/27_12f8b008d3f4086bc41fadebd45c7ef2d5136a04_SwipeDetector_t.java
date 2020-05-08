 package com.group15.djhero;
 
 import android.app.Activity;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnTouchListener;
 
 public class SwipeDetector implements OnTouchListener {
  
     public static final int DIRECTION_SWIPE_LEFT = 0;
     public static final int DIRECTION_SWIPE_RIGHT = 1;
     public static final int DIRECTION_SWIPE_UP = 2;
     public static final int DIRECTION_SWIPE_DOWN = 3;
  
     private long downTime;
     private float startX, startY;
    private float MIN_SWIPE_DISTANCE = 100;
 
  
     private Activity activity;
     
     public SwipeDetector(Activity activity){
     	this.activity = activity;
     }
     
 
  
     @Override
     public boolean onTouch(View view, MotionEvent event)
     {
         // if no listener has been registered, ignore the event.
  
         if (event.getAction() == MotionEvent.ACTION_DOWN)
         {
             downTime = System.currentTimeMillis();
             startX = event.getRawX();
             startY = event.getRawY();
             return true;
         }
  
         if (event.getAction() == MotionEvent.ACTION_UP)
         {
             float endX, endY;
             endX = event.getRawX();
             endY = event.getRawY();
  
             long upTime = System.currentTimeMillis();
             long swipeTime = (upTime - downTime) / 1000;
  
             float swipeDistance = (float)Math.sqrt(Math.pow(startX - endX, 2) + Math.pow(startY - endY, 2));
             float velocity = swipeDistance / swipeTime;
  
             if(swipeDistance >= MIN_SWIPE_DISTANCE)
             {
                 float dx = Math.abs(startX - endX);
                 float dy = Math.abs(startY - endY);
               
                //See if the swipe was up/down movement
                 if(dy > dx)
                 {
                     if (startY > endY)
                     {}
                     else
                     {}
                     }
               //If the swipe was a left/right movement
                 else
                 {
                     if (startX > endX)
                     	swipeRight();
                     else
                     {
                     	swipeLeft();
                     }
                 }
             }
  
             return true;
         }
  
         return false;
     }
  
     private void swipeRight() {
 		// TODO Auto-generated method stub
    	if (activity instanceof MainScreen){
    		((MainScreen)activity).goToDJ();
    	}
     	}
 
 	private void swipeLeft() {
 		// TODO Auto-generated method stub
    	if (activity instanceof DJInterface){
		((DJInterface)activity).goToMusic();
    	}
 	}
 
 
 }
