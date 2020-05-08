 package com.group2.finger_occ_demo;
 
import android.util.Log;
 import android.view.MotionEvent;
 
 class States
 {
 	enum FingerStates
 	{
 		IDLE,			// No user input
 		FINGERDOWN,		// Has a finger been detected
 		SWIPE,			// User is moving
 		RESIZE,			// User is resizing / moving
 		HOVER,			// User is selecting an object
 		SELECT
 	}
 	
 	public static final int SWIPE_DISTANCE =   10;	// How many pixels do we have to move to initiate a move
 	public static final int HOVER_TIMEOUT  = 1000;	// How many milliseconds before detecting hover
 	public static final int SELECT_TIMEOUT = 2000;	// How long without moving before item should be selected
 	public static final int SELECT_MOVE    =   10;  // How many pixels does it take to reset the select timeout
 	
 	// Used by other functions
 	static Point2D screen_offset = new Point2D(0,0);
 	static double zoom = 0;
 	
 	// Used only in inputMachine
 	static FingerStates fs = FingerStates.IDLE; 
 	static double oldDistance = 0;
 	static long oldTimestamp  = 0;
 	static long stateStart    = 0;	// How long have we been in the current mode
 	static Point2D old;				// Old position
 	static FingerStates inputMachine(Point2D one, Point2D two)
 	{
 		double distance;
 		long timestamp = System.currentTimeMillis();
 		
 		// If at any time the user removes both fingers, jump back to waiting
 		if (one == null)
 			fs = FingerStates.IDLE;
 		
 		switch (fs)
 		{
 			case IDLE:
 				// First finger down
 				if (one != null)
 				{
 					stateStart = System.currentTimeMillis();
 					fs = FingerStates.FINGERDOWN;
 				}
 				
 				break;
 			
 			case FINGERDOWN:
 				distance = one.distance(old);
 			
 				// User moved their finger, so they want to swipe the screen
 				if (distance > SWIPE_DISTANCE)
 				{
 					// Move to swipe mode
 					fs = FingerStates.SWIPE;
 					
 					// Track how far the finger moved
 					screen_offset.translate(old.x - one.x, old.y - one.y);
 					break;
 				}
 				
 				// Found a second finger, so they want to resize
 				if (two != null)
 				{
 					fs = FingerStates.RESIZE;
 					oldDistance = 0;
 					break;
 				}
 					
 				// Finger was left on the screen without moving, so the user wants
 				// to hover/select something
 				if (System.currentTimeMillis() > stateStart + HOVER_TIMEOUT)
 				{
 					stateStart = System.currentTimeMillis();
 					fs = FingerStates.HOVER;
 				}
 				
 				break;
 				
 			// Two fingers, expecting resizing but can still move the screen
 			case RESIZE:
 				// Revert back to swipe if we lost the second finger
 				if (two == null)
 				{
 					fs = FingerStates.SWIPE;
 					break;
 				}
 					
 				// We have a decent sample
 				if (oldDistance != 0)
 				{
 					// Offset our zoom by the distance between the two fingers
 					zoom += oldDistance - one.distance(two);					
 				}
 				
 				oldDistance = one.distance(two);
 				
 			// One finger. Just moving
 			case SWIPE:
 				// Upgrade to resize if a second finger was found
 				if (two != null)
 				{
 					fs = FingerStates.RESIZE;
 					oldDistance = 0;
 				}
 			
 				// Track how far the finger moved and apply it to our canvas offset
 				screen_offset.translate(old.x - one.x, old.y - one.y);
 			
 				break;
 				
 			case HOVER:
 				// If user moved, consider the hover state reset
 				if (old.distance(one) > SELECT_MOVE)
 					stateStart = System.currentTimeMillis();
 				
 				// If the user hovers without moving, consider something selected
 				if (System.currentTimeMillis() > stateStart + SELECT_TIMEOUT)
 					fs = FingerStates.SELECT;
 				
 				break;
 				
 			case SELECT:
 				// We have nothing to do in here but wait for fingers to be removed
 				break;
 		}
 		
 		//Log.w("State", fs.toString() + " start:" + stateStart + " time:" + System.currentTimeMillis());
 		//Log.w("Values", "Zoom: " + Double.toString(zoom) + " Offset: " + screen_offset.toString());
 		
 		oldTimestamp = timestamp;
 		old = one;
 		return fs;
 	}
 	
 	static FingerStates massageEvent(MotionEvent e)
 	{
 		Point2D one = new Point2D(e.getX(0), e.getY(0));
 		Point2D two = null;
 		
 		if (e.getPointerCount() > 1)
 		{
 			two = new Point2D(e.getX(1), e.getY(1));
 		}
 		
     	if ((e.getActionMasked() & (MotionEvent.ACTION_POINTER_1_UP)) == 0)
     		one = null;
 		
 		return inputMachine(one, two);
 	}
 }
