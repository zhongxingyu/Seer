 package com.medievaltech;
 
 import android.content.*;
 import android.graphics.*;
 import android.util.AttributeSet;
 
 import com.medievaltech.utils.*;
 
 class ShipDesignerView extends GameView {
 
 	/** Used to figure out elapsed time between frames */
     private long mLastUpdated;
     
     /** Variables for the counter */
     private int frameSamplesCollected = 0;
     private int frameSampleTime = 0;
     private int fps = 0;
 	
     private Paint textPaint;
     
     public ShipDesignerView(Context context, AttributeSet attrs) {
         super(context, attrs);
         
         textPaint = new Paint();
         textPaint.setARGB(255,255,255,255);
         textPaint.setTextSize(32);
     }
     
     public void runFrame(Canvas c) {
    	updatePhysics();
 		doDraw(c);
 	}
     
     protected void updatePhysics() {
 		long now = System.currentTimeMillis();
 		long timeElapsed = now - mLastUpdated;
 		mLastUpdated = now;
 		
 		//Time difference between now and last time we were here
 		frameSampleTime += timeElapsed;
 		frameSamplesCollected++;
 		
 		//After 10 frames
 		if (frameSamplesCollected == 10) {
 			
 			//Update the fps variable
 			fps = (int) (10000 / frameSampleTime);
 			
 			//Reset the sampletime + frames collected
 			frameSampleTime = 0;
 			frameSamplesCollected = 0;
 		}
 	}
 	
 	protected void doDraw(Canvas canvas) {
		canvas.drawColor(Color.BLACK);
 	    
		// Draw fps
		canvas.drawText(fps + " fps", getWidth() / 2, getHeight() / 2, textPaint);
 	    
 	    //Draw screen dimensions
 	    canvas.drawText(getWidth()+"x"+getHeight(), getWidth() / 2, getHeight() / 2 + 50, textPaint);
 	}
 }
