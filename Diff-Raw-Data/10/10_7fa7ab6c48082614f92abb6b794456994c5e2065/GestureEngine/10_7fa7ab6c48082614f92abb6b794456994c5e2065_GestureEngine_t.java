 package com.example.musicplayer;
 
 import android.media.AudioManager;
 import android.util.Log;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnTouchListener;
 import android.widget.Toast;
 
 /**
  * This is one of the main classes of the application.
  * This class processes the touch gestures made by the user on the phone.
  * This class simultaneously processes following touch gestures,
  * 1. Two Finger Double Tap
  * 2. Two Finger Left Swipe
  * 3. Two Finger Right Swipe
  * 4. Two Finger Scroll Up
  * 5. Two Finger Scroll Down
  * This class doesn't use any api or libraries available for android and have been written from scratch by me.
  * @author rajatagrawal
  *
  */
 public class GestureEngine {
 		
 	
 	// initial x,y of the touch gesture
 	float initialX;
     float initialY;
     
     // the final x,y of the touch gesture when the user lifts his fingers from the screen of the phone
     float finalX;
     float finalY;
     
     // the x,y value of finger coordinates of the previous frame
     float previousX;
     float previousY;
     
     //boolean variable that track if the user is swiping or scrolling
     Boolean swiping,scrolling;
     
     // boolean variable that track whether the user is swiping right or swiping left when the user is swiping
     Boolean swipingRight;
     Boolean swipingLeft;
     
     //boolean variable that tracks whether the user is scrolling up or scrolling down when the user is scrolling 
     Boolean scrollingUp;
     
     // the time stamp when the user starting making a touch gesture on the screen of the phone
     long initialTime;
     
     // the threshold number of pixels per second that a user should swipe in order to a swipe gesture
     float swipePixels;
     boolean cancelEvent;
     
     //time variables that keep track of the time stamps when the user tapped for the first time on the touch screen to do a double tap.
     long tap1Up,tap1Down;
     
     // time variables that keep track of the time stamps when the user tapped for the second time on the touch screen to do a double tap
     long tap2Up,tap2Down;
     
     //threshold value for the user to do a double tap
     long tapTimeLimit;
     
     // counter variables that keep track of false swipe movements read by the touch screen of the phone
     int reverseRightCounter;
     int reverseLeftCounter;
     
     //threshold number of false swipes to be considered before considering that gesture to be an actual swipe in the respective direction
     int reverseSwipeLimit;
     
     //string that stores the swiping direction in the previous frame
     String previousDirection;
     
     // counter variables that keep track of false horizontal and vertical movements detected by the touch screen.
     int horizontalMovementCounter,verticalMovementCounter;
     
     // previous and the current direction in the X-Y plane
     String ss_previousDirection,ss_currentDirection;
     
     // threshold value for number of false positives in X-Y plane before considering the false positives as the right motion.
     int ss_movementLimit;
     
     // the on touch listener which processes all the touch events
     OnTouchListener onTouchListener;
     
     // reference to the parent activity to call the respective functions on detecting a gesture.
     MainActivity parentActivity;
     
     
     public GestureEngine(MainActivity parentActivity)
     {
     	
     	// initialize all the values for different parameters for gesture detection.
     	initialX = initialY = finalX = finalY = 0;
         previousX = previousY = 0;
         swiping = scrolling = false;
         swipePixels = (float) 0.15;
         swipingRight = false;
         swipingLeft = false;
         cancelEvent = false;
         tap1Up = -1;
         tap1Down = -1;
         tap2Up = -1;
         tap2Down = -1;
         tapTimeLimit = (long) 1000;
         reverseRightCounter = 0;
         reverseLeftCounter = 0;
         reverseSwipeLimit = 4;
         previousDirection = null;
         horizontalMovementCounter = verticalMovementCounter = 0;
         ss_previousDirection = ss_currentDirection = null;
         ss_movementLimit = 3;
         
         initializeOnTouchListener();
         
         //set the parent activity
         this.parentActivity = parentActivity;
     }
     
     /**
      * this is the function that processes all the touch event received by the touch screen of the phone.
      */
     public void initializeOnTouchListener()
     {
     	onTouchListener = new View.OnTouchListener() {
     		@Override
 			public boolean onTouch(View v, MotionEvent event) {
 				// TODO Auto-generated method stub
 				
     			//when the user touches the first finger on the touch screen
 				if (event.getPointerCount() == 1)
 				{
 					if (event.getActionMasked() == MotionEvent.ACTION_MOVE);
 						//Log.d("GestureEngine","One Finger is moving");
 					
 					//when the user touches the touch screen
 					else if (event.getActionMasked() == MotionEvent.ACTION_DOWN)
 						Log.d("GestureEngine","First finger touched the screen");
 					
 					//when the user lifts the last finger from the touch screen
 					else if (event.getActionMasked() == MotionEvent.ACTION_UP)
 					{
 						Log.d("GestureEngine","The gesture has been finished");
 						return false;
 					}
 					return true;
 				}
 				
 				// when the user touches the second finger on the touch screen after already touching the first finger on the touch
 				// screen
 				else if (event.getPointerCount() == 2)
 				{
 					// on moving both the fingers on the touch screen
 					if (event.getActionMasked() == MotionEvent.ACTION_MOVE)
 					{
 						Log.d("GestureEngine","In action move and swiping left = " + swipingLeft + " and swiping Right is " + swipingRight);
 						
 						/*
 						if the double tap is being in the process of recognition.
 						On touching the touch screen with two fingers keeping them without moving still has some very small move movement
 						that needs to be handled
 						*/
 						if (tap1Down !=-1)
 						{
 							Log.d("GestureEngine","The difference in x is " + Math.abs(event.getX() - previousX) + " and difference in y is " + Math.abs(event.getY() - previousY));
 							
 							// if the movement is greater than a threshold, cancel the double tap gesture
 							if (Math.abs(event.getX() - previousX) > 5 || Math.abs(event.getY() - previousY) > 3)
 							{
 								tap1Down = -1;
 								tap1Up = -1;
 								tap2Up = -1;
 								tap2Down = -1;
 								previousX = event.getX();
 								previousY = event.getY();
 								return true;
 								
 							}
 							// else keep processing the double tap gesture
 							else
 							{
 								previousX = event.getX();
 								previousY = event.getY();
 							}
 							Log.d("GestureEngine","in tapping");
 							
 							//measure the time after the user puts two fingers on the touch screen to do the first tap
 							if ((System.currentTimeMillis() - tap1Down) < tapTimeLimit)
 								return true;
 							
 							/*
 							if the user hasn't still lifted his fingers up from the touch screen within a certain time threshold, cancel
 							the gesture
 							*/
 							else if (tap1Up == -1)
 							{
 								tap1Down = -1;
 								tap1Up = -1;
 								tap2Up = -1;
 								tap2Down = -1;
 							}
 							// if the user has lifted his two fingers after doing the first tap
 							else if (tap1Up !=-1)
 							{
 								// if the user hasn't put two fingers again for doing the second tap
 								if (tap2Down == -1)
 								{
 									//measure the time until when the two fingers for doing the second tap touch the touch screen
 									if ((System.currentTimeMillis() - tap1Up) < (tapTimeLimit/3))
 										return true;
 									// if the user doesn't touch two fingers again for doing the second tap, cancel the gesture
 									else
 									{
 										tap1Up = -1;
 										tap1Down = -1;
 										tap2Up = -1;
 										tap2Down = -1;
 									}
 										
 								}
 								// if the user touches the touch screen again for doing the second tap
 								else if (tap2Down !=-1)
 								{
 									//measure the time until when the user touches the touch screen for the doing the second tap
 									if ((System.currentTimeMillis() - tap2Down) < tapTimeLimit)
 										return true;
 									
 									// if the user doesn't lift the two fingers after doing the second tap, cancel the gesture
 									else
 									{
 										tap1Up = -1;
 										tap1Down = -1;
 										tap2Up = -1;
 										tap2Down = -1;
 									}
 								}
 							}
 						}
 						//Log.d("GestureEngine","Coming in moving");
 						
 						// if the user is moving her fingers on the screen in horizontal direction
 						if (Math.abs(event.getX() - previousX)>= Math.abs(event.getY() - previousY))
 						{
 							
 							/* if current horizontal movement direction is null, this is for first call to this function
 							   when touch events are processed
 							*/
 							if (ss_currentDirection == null)
 								ss_currentDirection = "horizontal";
 							
 							/* if previous horizontal movement direction is null, this is for first call to this function
 							   when touch events are processed
 							*/
 							if (ss_previousDirection == null)
 								ss_previousDirection = "horizontal";
 							
 							// if there was a false vertical movement detection in the previous frame, reset the value of the 
 							// counter that keeps a track of the number of false vertical movements done while doing actual horizontal
 							// movements
 							if (ss_previousDirection.equals("vertical"))
 								verticalMovementCounter = 0;
 							
 							// set the value of previous direction to horizontal for next frame processing
 							ss_previousDirection = "horizontal";
 							
 							/* if by chance this is a false movement detection.
 							that means we were actually in the process of processing movement in vertical direction
 							*/
 							if (ss_currentDirection == "vertical")
 							{
 								// increase the counter that keeps track of the number of false movements detected in horizontal direction
 								horizontalMovementCounter++;
 								/*
 								 * if the number of horizontal false movements detected is less than a threshold,
 								 * keep processing the touch events as vertical movements and ignore this horizontal movement
 								*/
 								if (horizontalMovementCounter<=ss_movementLimit)
 								{
 									previousX = event.getX();
 									previousY = event.getY();
 									return true;
 								}
 								/*
 								 * else make the current movement direction as horizontal
 								 * initialize the variables that keep track of false movements to zero
 								 * Also consider this new horizontal movement as a new horizontal movement
 								 * Hence also initialize all the movement tracking variables
 								 */
 								else
 								{
 									ss_currentDirection = "horizontal";
 									horizontalMovementCounter = 0;
 									verticalMovementCounter = 0;
 									initialX = previousX = event.getX();
 									initialY = previousY = event.getY();
 									initialTime = System.currentTimeMillis();
 								}
 							}
 							// if the current movement direction is already horizontal
 							else if (ss_currentDirection == "horizontal")
 							{
 								
 								// if is a horizontal movement in the "right" direction
 								if (event.getX() >= previousX)
 								{
 									Log.d("GestureEngine","SWIPING RIGHT and previous direction was " + previousDirection);
 									swipingRight = true;
 									
 									// initialize directions for first time event processing
 									if (previousDirection == null)
 										previousDirection = "right";
 									
 									/*
 									 *  if there was false left movement detected in the previous frame, reset the variable
 									 *  that keeps track of the false movements in the left direction.
 									 */
 									if (previousDirection.equals("left"))
 										reverseLeftCounter = 0;
 									
 									// setting the previous direction to 'right' for processing the next frame
 									previousDirection = "right";
 									
 									/*
 									 * May be this movement in the 'right' direction is a false movement. We were earlier moving in
 									 * left direction
 									 */
 									if (swipingLeft == true)
 									{
 										/*increase the value of the variable that keeps track of the
 										 *  false movements in the 'right' direction
 										 */
 										reverseRightCounter++;
 										
 										/*
 										 * if the number of false movements in the 'right' direction is within a threshold,
 										 * keep processing the touch events
 										 */
 										if (reverseRightCounter <= reverseSwipeLimit)
 										{
 											swipingRight = false;
 											swipingLeft = true;
 											previousX = event.getX();
 											previousY = event.getY();
 											return true;
 										}
 										/*
 										 * if the number of false positives in the 'right' direction exceed the threshold,
 										 * make the current movement direction as the 'right direction'
 										 * Consider this movement in the right direction as a new movement. Hence initialize
 										 * all the variables that keep track of the swipe gesture
 										 */
 										else if (reverseRightCounter > reverseSwipeLimit)
 										{
 											swipingRight = true;
 											swipingLeft = false;
 											reverseRightCounter = 0;
 											reverseLeftCounter = 0;
 											initialX = event.getX();
 											initialY = event.getY();
 											previousX = event.getX();
 											previousY = event.getY();
 											initialTime = System.currentTimeMillis();
 											return true;
 										}
 									}
 									/*
 									 * If we were previously processing movement in 'right' direction itself,
 									 * keep processing the touch events.
 									 */
 									if (swipingRight == true)
 									{
 										previousX = event.getX();
 										previousY = event.getY();
 										swipingRight = true;
 										swipingLeft = false;
 										return true;
 									}
 								}
 								
 								// if there is horizontal movement in the 'left' direction
 								else if (event.getX() < previousX)
 								{
 									Log.d("GestureEngine","SWIPING LEFT and previous direction was " + previousDirection);
 									swipingLeft = true;
 									
 									// initialize directions for first time event processing
 									if (previousDirection == null)
 										previousDirection = "left";
 									
 									/*
 									 *  if there was false right movement detected in the previous frame, reset the variable
 									 *  that keeps track of the false movements in the right direction.
 									 */
 									if (previousDirection.equals("right"))
 										reverseRightCounter = 0;
 									
 									// setting the previous direction to 'left' for processing the next frame
 									previousDirection = "left";
 									
 									/*
 									 * May be this movement in the 'left' direction is a false movement. We were earlier moving in
 									 * right direction
 									 */
 									if (swipingRight == true)
 									{
 										/*increase the value of the variable that keeps track of the
 										 *  false movements in the 'left' direction
 										 */
 										reverseLeftCounter++;
 										
 										/*
 										 * if the number of false movements in the 'left' direction is within a threshold,
 										 * keep processing the touch events
 										 */
 										if (reverseLeftCounter <= reverseSwipeLimit)
 										{
 											swipingRight = true;
 											swipingLeft = false;
 											previousX = event.getX();
 											previousY = event.getY();
 											return true;
 										}
 										/*
 										 * if the number of false positives in the 'left' direction exceed the threshold,
 										 * make the current movement direction as the 'left' direction
 										 * Consider this movement in the left direction as a new movement. Hence initialize
 										 * all the variables that keep track of the swipe gesture
 										 */
 										else if (reverseLeftCounter>reverseSwipeLimit)
 										{
 											swipingLeft = true;
 											swipingRight = false;
 											reverseRightCounter = 0;
 											reverseLeftCounter = 0;
 											initialX = event.getX();
 											initialY = event.getY();
 											previousX = event.getX();
 											previousY = event.getY();
 											initialTime = System.currentTimeMillis();
 											return true;
 										
 										}
 									}
 									/*
 									 * If we were previously processing movement in 'left' direction itself,
 									 * keep processing the touch events.
 									 */
 									else if (swipingLeft == true)
 									{
 										previousX = event.getX();
 										previousY = event.getY();
 										swipingLeft = true;
 										swipingRight = false;
 										return true;
 									}
 								}
 							}
 							
 						}
 						
 						// if there is movement in the vertical direction
 						else if (Math.abs(event.getX() - previousX) < Math.abs(event.getY() - previousY))
 						{
 							// initialize the current direction for first time processing of the touch events
 							if (ss_currentDirection == null)
 								ss_currentDirection = "vertical";
 							
 							// initialize the previous direction for first time processing of the touch events
 							if (ss_previousDirection == null)
 								ss_previousDirection = "vertical";
 							
 							/* if there was a false movement detection in the horizontal direction, in the previous frame,
 							 * reset the variable that keeps track of false movement in the horizontal direction.
 							 */
 							if (ss_previousDirection.equals("horizontal"))
 								horizontalMovementCounter = 0;
 							
 							// set the previous direction to 'vertical' for processing touch gestures in the next frame
 							ss_previousDirection = "vertical";
 							
 							// May be this movement in the vertical direction is a false movement
 							if (ss_currentDirection.equals("horizontal"))
 							{
 								// increase the counter that keeps track of the false movements in the vertical direction
 								verticalMovementCounter++;
 								
 								/* if the number of false vertical movements is within the threshold limit,
 								 * keep processing the touch events
 								 */
 								if (verticalMovementCounter <=ss_movementLimit)
 								{
 									previousX = event.getX();
 									previousY = event.getY();
 									verticalMovementCounter++;
 									return true;
 								}
 								/*
 								 * If the number of false movements in the vertical direction exceed the threshold,
 								 * then make the current direction of movement as vertical.
 								 * This motion in vertical direction is treated as a fresh movement. Hence all the variables
 								 * that keep track of the touch gesture processing are initialized.
 								 */
 								else
 								{
 									ss_currentDirection = "vertical";
 									horizontalMovementCounter = 0;
 									verticalMovementCounter = 0;
 									initialX = previousX = event.getX();
 									initialY = previousY = event.getY();
 									initialTime = System.currentTimeMillis();
 								}
 									
 							}
 							
 							//If we were already moving in vertical direction
 							else if (ss_currentDirection.equals("vertical"))
 							{
 								// if we are moving down
 								if (event.getY()>= previousY)
 								{
 									//SCROLLING GESTURE DETECTED
 									Log.d("GestureEngine","GESTURE : Scrolling Down");
 									
 									// incrementally decrease the volume of the song player
 									int currentProgress = parentActivity.volumeControl.getProgress();
 									parentActivity.volumeControl.setProgress(currentProgress-1);
 									double volumeToSet = ((parentActivity.volumeControl.getProgress())/(float)parentActivity.seekBarMax) *parentActivity.maxVolume;
 									parentActivity.audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,(int) volumeToSet,AudioManager.FLAG_VIBRATE);
 									
 									//audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,AudioManager.ADJUST_LOWER,AudioManager.FLAG_VIBRATE);
 									//setInitialProgressBar();
 								}
 								
 								// if we are moving in the up direction
 								else if (event.getY() < previousY)
 								{
 									//GESTURE SCROLLING DETECTED
 									Log.d("GestureEngine","GESTURE : Scrolling Up");
 									
 									//incrementally increase the volume of the song player
 									int currentProgress = parentActivity.volumeControl.getProgress();
 									parentActivity.volumeControl.setProgress(currentProgress+1);
 									double volumeToSet = ((parentActivity.volumeControl.getProgress())/(float)parentActivity.seekBarMax) *parentActivity.maxVolume;
 									parentActivity.audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,(int) volumeToSet,AudioManager.FLAG_VIBRATE);
 								}
 								//keep processing the touch gestures for the next frame
 								previousX = event.getX();
 								previousY = event.getY();
 								return true;
 							}
 							
 						}
 						return true;
 					}
 					
 					// if the user lifts the second finger from the touch screen
 					else if (event.getActionMasked() == MotionEvent.ACTION_POINTER_UP)
 					{
 						Log.d("GestureEngine","Second Finger lifted up from the screen");
 						
 						//reset all the variables that keep track of the touch gesture movement
 						reverseRightCounter = 0;
 						reverseLeftCounter = 0;
 						previousDirection = null;
 						horizontalMovementCounter = 0;
 						verticalMovementCounter = 0;
 						ss_previousDirection = null;
 						
 						
 						// if we were double tapping and had already made an attempt to the second tap
 						if (tap1Down !=-1 && tap2Down == -1)
 						{
 							Log.d("GestureEngine","The time difference is " + (System.currentTimeMillis() - tap1Down));
 							
 							/*measure the time of the lift of the two fingers for up movement 
 							 * while making the second tap in the double tap movement
 							 */
 							if ((System.currentTimeMillis() - tap1Down) < tapTimeLimit)
 								tap1Up = System.currentTimeMillis();
 							
 							/*
 							 * If we don't lift the two fingers within time threshold while doing the second tap,
 							 * cancel the tap gesture
 							 */
 							else
 							{
 								tap1Up = -1;
 								tap1Down = -1;
 								tap2Up = -1;
 								tap2Down = -1;
 							}
 							
 						}
 						else if (tap1Down !=-1 && tap2Down !=-1)
 						{
 							/* if the two fingers are lifted in time after putting down 
 							 * the two fingers for the second tap while doing the double tap
 							 */
 							if ((System.currentTimeMillis() - tap2Down) < tapTimeLimit)
 							{
 								//GESTURE DOUBLE TAP DETECTED
 								Log.d("GestureEngine","GESTURE : Double Tap Detected");
 								
 								//Play/Pause the song player
 								parentActivity.pauseThePlayer();
 								tap1Up = -1;
 								tap1Down = -1;
 								tap2Up = -1;
 								tap2Down = -1;
 								return true;
 							}
 							/*
 							 * We didn't lift the two fingers in time after doing second tap, hence cancel the tap gesture
 							 */
 							else
 							{
 								tap1Up = -1;
 								tap1Down = -1;
 								tap2Up = -1;
 								tap2Down = -1;
 							}
 								
 						}
 						/*
 						 * If we had been moving in the horizontal direction
 						 */
 						if (ss_currentDirection!= null && ss_currentDirection.equals("horizontal"))
 						{
 							Log.d("GestureEngine","Finished swiping with fastness is " + Math.abs(event.getX() - initialX)/(float)(System.currentTimeMillis() - initialTime) + " and threshold is " + swipePixels);
 							//Calcuate the fastness of the movements of the fingers while doing the swipe gesture
 							// if the swipe gesture is fast enough
 							if (Math.abs(event.getX() - initialX)/(float)(System.currentTimeMillis() - initialTime) > swipePixels)
 							{
 								
 								// if we moved in the 'right' direction
 								if (event.getX() >= initialX)
 								{
 									//GESTURE : RIGHT SWIPE DETECTED
 									Log.d("GestureEngine","GESTURE Swiped Right");
 									
 									//play the previous song
 									parentActivity.playPreviousSong();
 								}
 								// we moved in the 'left' direction
 								else
 								{
 									//GESTURE : LEFT SWIPE DETECTED
 									Log.d("GestureEngine","GESTURE Swiped Left");
 									
 									// play the next song
 									parentActivity.playNextSong();
 								}
 							}
 							return true;
 						}
 						return true;
 						
 					}
 					
 					//second finger touched the touch screen
 					else if (event.getActionMasked() == MotionEvent.ACTION_POINTER_DOWN)
 					{
 						Log.d("GestureEngine","Second finger touched the screennnn");
 						
 						// If we hadn't been tapping, start double tap gesture recognition
 						if (tap1Down == -1)
 							tap1Down = System.currentTimeMillis();
 						
 						/* If we have touched our fingers again within time on the touch screen for doing the second tap to do double tapping
 						 *  do the processing for detecting the second tap of the double tap gesture
 						 */
 						else if (tap2Down == -1 && (System.currentTimeMillis() - tap1Up) < (tapTimeLimit/3))
 							tap2Down = System.currentTimeMillis();
 						
 						/* if we didn't touch our two fingers again in time for doing the second tap of the double tap gesture,
 						 * cancel the double tap gesture
 						 */
 						else
 						{
 							tap1Up = -1;
 							tap2Up = -1;
 							tap1Down = -1;
 							tap2Down = -1;
 						}
 						
 						// initialize the variables that keep process the touch gesture movements
 						initialX = event.getX();
 						initialY = event.getY();
 						previousX = event.getX();
 						previousY = event.getY();
 						swiping = false;
 						scrolling = false;
 						swipingLeft = false;
 						swipingRight = false;
 						initialTime = System.currentTimeMillis();
 						
 						reverseRightCounter = 0;
 						reverseLeftCounter = 0;
 						previousDirection = null;
 						
 						ss_currentDirection = null;
 						ss_previousDirection = null;
 						horizontalMovementCounter = 0;
 						verticalMovementCounter = 0;
 						return true;
 					}
 					
 				}
 				return true;
 			}
 		
 			
 		};
     }
 }
