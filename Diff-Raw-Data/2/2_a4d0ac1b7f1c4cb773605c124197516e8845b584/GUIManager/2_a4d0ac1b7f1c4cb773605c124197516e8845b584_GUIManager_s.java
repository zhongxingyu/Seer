 /*==============================================================================
             Copyright (c) 2010-2011 QUALCOMM Incorporated.
             All Rights Reserved.
             Qualcomm Confidential and Proprietary
 ==============================================================================*/
 
 package com.qualcomm.QCARSamples.ImageTargets;
 
 import android.app.Activity;
 import android.content.Context;
 import android.graphics.LightingColorFilter;
 import android.os.Handler;
 import android.os.Message;
 import android.view.View;
 import android.widget.Button;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class GUIManager extends Activity{
 
     // Custom views
     private View overlayView;
     private Button pauseButton;
     private Button unpauseButton;
     private Button startButton;
     private Button deleteButton;
     private Button storeButton;
     private Button statsButton;
     private Button upgradeButton;
     private Button creditsButton;
     private TextView currentLevel;
     private TextView currentScore;
     private TextView currentZen;
     
     // The main application context
     private Context applicationContext;
     
     // A Handler for working with the gui from other threads
     private Handler mainActivityHandler;
     
     // Flags for our Handler
     public static final int SHOW_DELETE_BUTTON = 0;
     public static final int HIDE_DELETE_BUTTON = 1;
     
     public static final int TOGGLE_PAUSE_BUTTON = 2;
     public static final int SHOW_PAUSE_BUTTON = 3;
     public static final int HIDE_PAUSE_BUTTON = 4;
     
     public static final int SHOW_UNPAUSE_BUTTON = 5;
     public static final int HIDE_UNPAUSE_BUTTON = 6;
     
     public static final int TOGGLE_STORE_BUTTON = 7;
     public static final int SHOW_STORE_BUTTON = 8;
     public static final int HIDE_STORE_BUTTON = 9;
 
     public static final int SHOW_UPGRADE_BUTTON = 10;
     public static final int HIDE_UPGRADE_BUTTON = 11;
     
     public static final int SHOW_STATS_BUTTON = 12;
     public static final int HIDE_STATS_BUTTON = 13;
     
     public static final int SHOW_CREDITS_BUTTON = 14;
     public static final int HIDE_CREDITS_BUTTON = 15;
     
     public static final int HIDE_START_BUTTON = 16;
     
     public static final int DISPLAY_INFO_TOAST = 100;
     
     // Native methods to handle button clicks
     public native void nativePause();
     public native void nativeStart();
     public native void nativeStore();
     public native void nativeLeave();
     public native void nativeUnpause();
     public native void nativeDelete();
     public native void nativeUpgrade();
     public native void nativeStats();
     public native void nativeCredits();
     
     /** Initialize the GUIManager. */
     public GUIManager(Context context)
     {
         // Load our overlay view
         // This view will add content on top of the camera / opengl view
         overlayView = View.inflate(context, R.layout.interface_overlay, null);
         
         // Store the application context
         applicationContext = context;
         
         // Create a Handler from the current thread
         // This is the only thread that can make changes to the GUI,
         // so we require a handler for other threads to make changes
         mainActivityHandler = new Handler() {
             @Override
             public void handleMessage(Message msg) {
                 switch (msg.what) {
                     case SHOW_DELETE_BUTTON:
                         if (deleteButton != null) {
                             //deleteButton.setVisibility(View.VISIBLE);
                             deleteButton.setEnabled(true);
                         }
                         break;
                     case HIDE_DELETE_BUTTON:
                         if (deleteButton != null) {
                             deleteButton.setVisibility(View.INVISIBLE);
                             deleteButton.setEnabled(false);
                         }
                         break;
                     case TOGGLE_PAUSE_BUTTON:
                         if (pauseButton != null) {
                             //pauseButton.setChecked(true);
                         }
                         break;
                     case SHOW_PAUSE_BUTTON:
                         if (pauseButton != null) {
                             //pauseButton.setVisibility(View.VISIBLE);
                        	pauseButton.setEnabled(false);
                         }
                         break;
                     case HIDE_PAUSE_BUTTON:
                         if (pauseButton != null) {
                             //pauseButton.setVisibility(View.INVISIBLE);
                         	pauseButton.setEnabled(false);
                         }
                         break;
                     case SHOW_UNPAUSE_BUTTON:
                         if (unpauseButton != null) {
                             //unpauseButton.setVisibility(View.VISIBLE);
                         	unpauseButton.setEnabled(true);
                         }
                         break;
                     case HIDE_UNPAUSE_BUTTON:
                         if (unpauseButton != null) {
                             //unpauseButton.setVisibility(View.INVISIBLE);
                         	unpauseButton.setEnabled(false);
                         }
                         break;             
                     case TOGGLE_STORE_BUTTON:
                         if (storeButton != null) {
                             //storeButton.setChecked(true);
                         }
                         break; 
                     case SHOW_STORE_BUTTON:
                         if (storeButton != null) {
                             //storeButton.setVisibility(View.VISIBLE);
                         	storeButton.setEnabled(true);
                         }
                         break;
                     case HIDE_STORE_BUTTON:
                         if (storeButton != null) {
                             //storeButton.setVisibility(View.INVISIBLE);
                         	storeButton.setEnabled(false);
                         }
                         break;
                     case SHOW_UPGRADE_BUTTON:
                         if (upgradeButton != null) {
                         	//upgradeButton.setVisibility(View.VISIBLE);
                         	upgradeButton.setEnabled(true);
                         }
                         break;
                     case HIDE_UPGRADE_BUTTON:
                         if (upgradeButton != null) {
                         	//upgradeButton.setVisibility(View.INVISIBLE);
                         	upgradeButton.setEnabled(false);
                         }
                         break;
                     case SHOW_STATS_BUTTON:
                         if (statsButton != null) {
                             //statsButton.setVisibility(View.VISIBLE);
                         }
                         break;
                     case HIDE_STATS_BUTTON:
                         if (statsButton != null) {
                             //statsButton.setVisibility(View.INVISIBLE);
                         }
                         break;
                     case SHOW_CREDITS_BUTTON:
                         if (creditsButton != null) {
                         	//creditsButton.setVisibility(View.VISIBLE);
                         	creditsButton.setEnabled(true);
                         }
                         break;
                     case HIDE_CREDITS_BUTTON:
                         if (creditsButton != null) {
                         	//creditsButton.setVisibility(View.INVISIBLE);
                         	creditsButton.setEnabled(false);
                         }
                         break;
                     case HIDE_START_BUTTON:
                         if (startButton != null) {
                             //startButton.setVisibility(View.INVISIBLE);
                             startButton.setEnabled(false);
                         }
                         break;
                     case DISPLAY_INFO_TOAST:
                         String text = (String) msg.obj;
                         int duration = Toast.LENGTH_LONG;
                         Toast toast = Toast.makeText(applicationContext, text, duration);
                         toast.show();
                         break;
                 }
             }
         };
     }
     
 
     
     
     /** Button clicks should call corresponding native functions. */
     public void initButtons()
     {
         if (overlayView == null)
             return;
 
         pauseButton = (Button) overlayView.findViewById(R.id.pause_button);
         pauseButton.getBackground().setColorFilter(new LightingColorFilter(0xFFFFFFFF, 0xFFAA0000));
         
         unpauseButton = (Button) overlayView.findViewById(R.id.unpause_button);
         unpauseButton.getBackground().setColorFilter(new LightingColorFilter(0xFFFFFFFF, 0xFFAA0000));
         unpauseButton.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                     nativeUnpause();
             }
         });
 
         storeButton = (Button) overlayView.findViewById(R.id.store_button);
         storeButton.getBackground().setColorFilter(new LightingColorFilter(0xFFFFFFFF, 0xFF00AA00));
         
         startButton = (Button) overlayView.findViewById(R.id.start_button);
         startButton.getBackground().setColorFilter(new LightingColorFilter(0xFFFFFFFF, 0xFF00AA00));
         startButton.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
               	pauseButton.setVisibility(View.VISIBLE);
             	nativeStart();
             	newLevel(String.valueOf(1)); 
             }
         });
         
         deleteButton = (Button) overlayView.findViewById(R.id.delete_button);
         deleteButton.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 nativeDelete();
             }
         });
         
         upgradeButton = (Button) overlayView.findViewById(R.id.upgrade_button);
         upgradeButton.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 nativeUpgrade();
             }
         });
         
         /*statsButton = (Button) overlayView.findViewById(R.id.stats_button);
         statsButton.getBackground().setColorFilter(new LightingColorFilter(0xFFFFFFFF, 0xFF0000AA));
         statsButton.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 nativeStats();
             }
         });*/
         
         creditsButton = (Button) overlayView.findViewById(R.id.credits_button);
         creditsButton.getBackground().setColorFilter(new LightingColorFilter(0xFFFFFFFF, 0xFFAAAA00));
         creditsButton.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 nativeCredits();
             }
         });
         
         
         pauseButton.setEnabled(false);
         unpauseButton.setEnabled(false);
         deleteButton.setEnabled(false);
         upgradeButton.setEnabled(false);
         storeButton.setEnabled(false);
         
         currentLevel = (TextView) overlayView.findViewById(R.id.current_level);
     	currentLevel.setText("Game Setup!");
         
         currentScore = (TextView) overlayView.findViewById(R.id.current_score);
     	currentScore.setText(String.valueOf(0));
     	
         currentZen = (TextView) overlayView.findViewById(R.id.current_zen);
     	currentZen.setText(String.valueOf(5));
 
     }
     
     
     /** Clear the button listeners. */
     public void deinitButtons()
     {
         if (overlayView == null)
             return;
         
         pauseButton.setOnClickListener(null);
         startButton.setOnClickListener(null);
         deleteButton.setOnClickListener(null);
         storeButton.setOnClickListener(null);
         
         pauseButton = null;
         startButton = null;
         deleteButton = null;
         storeButton = null;
     }
     
     
     /** Send a message to our gui thread handler. */
     public void sendThreadSafeGUIMessage(Message message)
     {
         mainActivityHandler.sendMessage(message);
     }
     
     public void newScore(String score)
     {
     	//SCREW YOU FINAL FOR WASTING HOURS
     	final String temp = score;
     	//extended activity. Hopefully not too much overhead?
     	runOnUiThread(new Runnable() {
     		
     	     public void run() {
     	    	 
              	currentScore.setText(temp);
 
     	    }
     	});
 
     }
     public void newZen(String zen)
     {
     	final String temp = zen;
     	//extended activity. Hopefully not too much overhead?
     	runOnUiThread(new Runnable() {
     		
     	     public void run() {
     	    	 
              	currentZen.setText(temp);
 
     	    }
     	});
     }
     
     public void newLevel(String level)
     {
     	final String temp = level;
     	//extended activity. Hopefully not too much overhead?
     	runOnUiThread(new Runnable() {
     		
     	     public void run() {
     	    	 
              	currentLevel.setText(temp);
 
     	    }
     	});
     }
     
 	public void updateEOL(String level) {
 		//updateApplicationStatus(APPSTATUS_INIT_EOL);
 		/*
     	final String temp = level;
     	//extended activity. Hopefully not too much overhead?
     	runOnUiThread(new Runnable() {
     		
     	     public void run() {
     	        //currentLevel = (TextView) mGUIManager.getOverlayView().findViewById(R.id.current_level);
              	//currentLevel.setText(temp);
     	     }
     	});*/
 	}
     
     /** Getter for the overlay view. */
     public View getOverlayView()
     {
         return overlayView;
     }
 }
