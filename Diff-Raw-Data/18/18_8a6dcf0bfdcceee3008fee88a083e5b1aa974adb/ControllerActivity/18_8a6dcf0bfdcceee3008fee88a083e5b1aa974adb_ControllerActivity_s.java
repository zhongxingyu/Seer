 package com.scribdroid.android;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
 import android.content.res.Resources;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Paint.Style;
 import android.graphics.RectF;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.view.MotionEvent;
 import android.widget.RelativeLayout;
 
 public class ControllerActivity extends Activity {
     // Debugging
     private static final String TAG = "ControllerActivity";
     private static final boolean D = true;
 	
     private MyApp appState; 
 	private SharedPreferences settings;
 	private Resources res;
 	
 	// Must be instance variable to avoid garbage collection!
 	private OnSharedPreferenceChangeListener listener;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         
 	    setContentView(R.layout.controller);
 	    final RelativeLayout mLayout = (RelativeLayout) findViewById(R.id.controller_layout);
 
 	    appState = ((MyApp)getApplicationContext());	    
 	    settings = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
 	    res = getResources();
 	    
 	    // Enclosures to swap between as user changes preferences
 	    final Enclosure s = new SimpleEnclosure(this);
 	    final Enclosure c = new ComplexEnclosure(this);
 	    
 	    // Set the initial controller mode
 	    if (settings.getString(res.getString(R.string.controller_mode_pref), res.getString(R.string.complex)).equals(res.getString(R.string.simple))){
	    	mLayout.addView(s);  
 	    }
 	    else {
 	    	mLayout.addView(c);  
 	    }
 	    
 	    // Listener that will change controller settings as user changes preferences
		listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
			  public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
				  
 				if (key.equals(res.getString(R.string.controller_mode_pref))){
 					  String mode = settings.getString(res.getString(R.string.controller_mode_pref), res.getString(R.string.complex));
					  
					  if (mode.equals(res.getString(R.string.simple))) {
 						  mLayout.removeView(c);
 						  mLayout.addView(s);
 						  Log.i(TAG, "Changed to Simple Controller");
					  } else if (mode.equals(res.getString(R.string.complex))) {
 						  mLayout.removeView(s);
 						  mLayout.addView(c);
 						  Log.i(TAG, "Changed to Complex Controller");
 					  }	  
                 }
 			  }
 			};
 	    settings.registerOnSharedPreferenceChangeListener(listener);
     }
   
     @Override
     protected void onStart() {
       super.onStart();  
     }    
     
     @Override
     protected void onResume() {
       super.onResume();
     }
 
     @Override
     protected void onStop() {
       super.onStop();
     }
     
     private class ComplexEnclosure extends Enclosure {
     	private float threshold;
     	
     	public ComplexEnclosure(Context context) {
 			super(context);
 			threshold = 0.1f;
 		}
     	
     	 @Override
     	 protected void onDraw(Canvas canvas) {
     	     super.onDraw(canvas);
              
     	     // Draw Enclosure
     	     this.getPaint().setStyle(Style.STROKE);
     	     canvas.drawCircle(this.getX(), this.getY(), this.getR(), this.getPaint());
     	    
     	     // Draw line from center to where user touches
     	     if (this.getTouchX() == 0 && this.getTouchY() == 0) {
     	    	 canvas.drawLine(0, 0, 0, 0, this.getPaint());
     	     } else {
     	    	 canvas.drawLine(this.getX(), this.getY(), this.getTouchX(), this.getTouchY(), this.getPaint()); 
     	     }
     	     
     	     // Draw center point
     	     this.getPaint().setStyle(Style.FILL_AND_STROKE);
     	     canvas.drawCircle(this.getX(), this.getY(), 5, this.getPaint());
     	 }
     	 
     	 /**
     	  * Function that determines if a given users touch is in the enclosure
     	  * @param tx - the x-coordinate to test enclosure of
     	  * @param ty - the y-coordinate to test enclosure of
     	  * @return true if tx and ty are in the current enclosure
     	  */
     	 public boolean inEnclosure(float tx, float ty) {
     		 if ( Math.pow(tx - this.getX(), 2) + Math.pow(ty - this.getY(), 2) < Math.pow(this.getR(), 2) ) {
     			 return true;
     		 }
     		 return false; 
     	 }
 	    
 	    @Override
 	    public boolean onTouchEvent(MotionEvent me) {
 	        float[] values = new float[] {0,0};
 	        
 	        int action = me.getAction();
 	        float currentX = me.getX();
 	        float currentY = me.getY();
 	        
 	        
 	        // Make sure robot moves only if user touches in enclosure
 	        if (this.inEnclosure(currentX, currentY)) {
 	        	
 		        if (appState.getScribbler().isConnected() && action == MotionEvent.ACTION_DOWN) {        	
 		        	if (D) Log.d(TAG, "ACTION_DOWN: X = " + currentX + "Y = " + currentY);
 		        	
 		        	this.setTouchX(currentX);
 		        	this.setTouchY(currentY);
 		        	this.invalidate();
 		        	
 		            values = this.calculateValues(currentX, currentY);
 		            if (D) Log.d(TAG, "Moving... Trans = " + values[0] + " Rot = " + values[1]);
 		            appState.getScribbler().move(values[0], values[1]);
 		        	
 		        }
 	        }
 	        
 	        // Stop the robot once user lifts finger
 	        if (appState.getScribbler().isConnected() && action == MotionEvent.ACTION_UP) {
 	            if (D) Log.d(TAG, "ACTION_UP: X = " + currentX + " Y = " + currentY);
 	            
 	        	this.setTouchX(this.getX());
 	        	this.setTouchY(this.getY());
 	            this.invalidate();
 	            
 	            appState.getScribbler().move(0, 0);
 				
 	        }
 	        return true;
 	      }
 
 	    /**
 	     * Given a users touch, determine how to move the robot
 	     * @param xPos - the x position of user's touch
 	     * @param yPos - the y position of user's touch
 	     * @return - float[] containing translate value and rotate value
 	     */
 		public float[] calculateValues(float xPos, float yPos) {
 			float trans = (this.getY() - yPos) / this.getY();
 	    	float rot = (this.getX() - xPos) / this.getX();
 	    	
 	    	// Calculate translate value
 	    	if (Math.abs(trans) < threshold) trans = 0.0f;
 			if (trans < 0.0f) {
 				trans += threshold;
 			} else if (trans > 0.0f) {
 				trans -= threshold;
 			}
 			
 			// Calculate rotate value
 	    	if (Math.abs(rot) < threshold)	rot = 0.0f;
 			if (rot < 0.0f) {
 				rot += threshold;
 			} else if (rot > 0.0f) {
 				rot -= threshold;
 			}
 			
 			// Fix to make the left and right turns correct
 			if (trans > 0) rot *= -1;
 	    		    	
 	    	return new float[] {trans, rot};
 		}
     }
     
     private class SimpleEnclosure extends Enclosure {
     	// RectF's that will contain the respective arrows
         private RectF topR;
         private RectF bottomR;
         private RectF leftR;
         private RectF rightR;
         
         
     	public SimpleEnclosure(Context context) {
 			super(context);
 		}
     	
     	 @Override
     	 protected void onDraw(Canvas canvas) {
     	    super.onDraw(canvas);
             this.getPaint().setStyle(Style.FILL_AND_STROKE);
      
             // define containing rectf's
 			topR = new RectF(getX() - 50,
                     getY() - getR(),
                     getX() + 50,
                     getY());
 			bottomR = new RectF(getX() - 50,
                     getY(),
                     getX() + 50,
                     getY() + getR());
 			leftR = new RectF(getX() - getR(),
 					getY() - 50,
 					getX(),
 					getY() + 50);
 			rightR = new RectF(getX(),
 					getY() - 50,
 					getX() + getR(),
 					getY() + 50);
 			
 			// Get Bitmap Sources
             Bitmap topArrowBitmap = BitmapFactory.decodeResource(getResources(),
                     R.drawable.top_arrow);
 
             Bitmap bottomArrowBitmap = BitmapFactory.decodeResource(getResources(),
                     R.drawable.bottom_arrow);
             
             Bitmap leftArrowBitmap = BitmapFactory.decodeResource(getResources(),
                     R.drawable.left_arrow);
 
             Bitmap rightArrowBitmap = BitmapFactory.decodeResource(getResources(),
                     R.drawable.right_arrow);
             
             // Draw arrows in appropriate RectF
             canvas.drawBitmap(topArrowBitmap, null, topR, getPaint());
             canvas.drawBitmap(bottomArrowBitmap, null, bottomR, getPaint());
             canvas.drawBitmap(leftArrowBitmap, null, leftR, getPaint());
             canvas.drawBitmap(rightArrowBitmap, null, rightR, getPaint());
             
             // Draw containing circle
             this.getPaint().setStyle(Style.STROKE);
     	    canvas.drawCircle(this.getX(), this.getY(), this.getR(), this.getPaint());
    	 		
     	 }
     	 
 	    @Override
 	    public boolean onTouchEvent(MotionEvent me) {
 	        int action = me.getAction();
 	        float currentX = me.getX();
 	        float currentY = me.getY();
 	               
 	        // Movement controls
 	        if (appState.getScribbler().isConnected() && action == MotionEvent.ACTION_DOWN) {
 		        if (topR.contains(currentX, currentY)) {
 		        	if (D) Log.d(TAG, "Moving Forward...");
 		        	appState.getScribbler().forward(.5);
 		        } else if (bottomR.contains(currentX, currentY)){
 		        	if (D) Log.d(TAG, "Moving Backward...");
 		        	appState.getScribbler().backward(.5);
 		        } else if (leftR.contains(currentX, currentY)) {
 		        	if (D) Log.d(TAG, "Turning Left...");
 		        	appState.getScribbler().turnLeft(.5);
 		        } else if (rightR.contains(currentX, currentY)) {
 		        	if (D) Log.d(TAG, "Turning Right...");
 		        	appState.getScribbler().turnRight(.5);
 		        } 
 	        }
 	        
 	        // Stop the robot when user moves finger up
 	        if (appState.getScribbler().isConnected() && action == MotionEvent.ACTION_UP) {
 	        	if (D) Log.d(TAG, "Stopping Robot");
 	            appState.getScribbler().move(0, 0);	
 	        }
 	        return true;
 	    }
     }    
 }
