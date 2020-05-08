 package se.chalmers.project14.enterBuilding;
 
 import se.chalmers.project14.main.R;
 import android.util.Log;
 import android.view.GestureDetector.*;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ViewFlipper;
 
 public class SwipeHandler extends SimpleOnGestureListener {
 	private static final int SWIPE_MIN_DISTANCE = 120;
 	private static final int SWIPE_MAX_OFF_PATH = 250;
 	private static final int SWIPE_THRESHOLD_VELOCITY = 200;
 	ViewFlipper flipper;
 
 	public SwipeHandler(ViewFlipper f) {
 		super();
 		flipper = f;
 
 	}
 
 	@Override
 	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
 		System.out.println("" + e1 + e2);
 		
 			if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
 				return false;
 			// right to left swipe
 			if(e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
 				
 				flipper.showNext();
 
 			}  else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
 			
 				flipper.showNext();
 			}
		 
 		return false;
 	}
 	@Override
 	public boolean onDown(MotionEvent e) {
 	    return true;        
 	}
 
 
 
 }
