 package com.awevation.spuzzle;
 
 import android.content.Context;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.*;
 import android.widget.Button;
 
 public class ControllerButton extends Button {
     private Runnable runmeDown;
     private Runnable runmeUp;
 
     public ControllerButton(final Context context, final AttributeSet attrs) {
 	super(context, attrs);
     }
 
     @Override
     public boolean onTouchEvent(final MotionEvent event) {
 	switch(event.getAction()) {
 	    case MotionEvent.ACTION_DOWN:
 		setPressed(true);
 		performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
 		if(runmeDown != null) {
 		    runmeDown.run();
 		}
 		break;
 	    case MotionEvent.ACTION_UP:
 		setPressed(false);
 		if(runmeUp != null) {
		    //runmeUp.run();
 		}
 		break;
 	}
 	return super.onTouchEvent(event);
     }
 
     //take in a function to run while the button is pressed down
     
     public void setRunmeDown(Runnable runme) {
 	this.runmeDown = runme;
     }
 
     //take in a function to run when.... You guessed it!
     public void setRunmeUp(Runnable runme) {
 	this.runmeUp = runme;
     }
 }
