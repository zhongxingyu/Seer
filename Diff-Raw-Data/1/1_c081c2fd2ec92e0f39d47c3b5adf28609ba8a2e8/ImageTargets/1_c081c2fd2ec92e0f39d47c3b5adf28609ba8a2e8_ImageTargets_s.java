 /*==============================================================================
             Copyright (c) 2010-2011 QUALCOMM Incorporated.
             All Rights Reserved.
             Qualcomm Confidential and Proprietary
             
 @file 
     ImageTargets.java
 
 @brief
     Sample for ImageTargets
 
 ==============================================================================*/
 
 package com.qualcomm.QCARSamples.ImageTargets;
 
 import java.util.Vector;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.pm.ActivityInfo;
 import android.media.MediaPlayer;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Handler;
 import android.util.DisplayMetrics;
 import android.view.GestureDetector;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.SubMenu;
 import android.view.View;
 import android.view.WindowManager;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.Button;
 import android.widget.RadioButton;
 import android.widget.SeekBar;
 import android.widget.TextView;
 
 import android.media.AudioManager;
 
 
 import com.qualcomm.QCAR.QCAR;
 import com.qualcomm.QCARSamples.ImageTargets.GUIManager;
 
 /** The main activity for the ImageTargets sample. */
 public class ImageTargets extends Activity {
 	
 	AudioManager audioManager;
 	
 	MediaPlayer arrow_sound;
 	MediaPlayer cannon_sound;
 	MediaPlayer snowball_sound;
 	MediaPlayer level_sound;
 	MediaPlayer button_sound;
 	
 	// Application status constants:
 	private static final int APPSTATUS_UNINITED = -1;
 	private static final int APPSTATUS_INIT_APP = 0;
 	private static final int APPSTATUS_INIT_QCAR = 1;
 	private static final int APPSTATUS_INIT_APP_AR = 2;
 	private static final int APPSTATUS_INIT_TRACKER = 3;
 	private static final int APPSTATUS_INITED = 4;
 	private static final int APPSTATUS_CAMERA_STOPPED = 5;
 	private static final int APPSTATUS_CAMERA_RUNNING = 6;
 	private static final int APPSTATUS_INIT_MENU = 7;
 	private static final int APPSTATUS_INIT_EOL = 8;
 	private static final int APPSTATUS_GAMEOVER = 9;
 
     private static final int DIALOG_GAMEOVER = 98;
 	private static final int DIALOG_PAUSE = 99;
     private static final int DIALOG_STORE = 100;
     private static final int DIALOG_EOL = 101;
     private static final int DIALOG_STORE_CONT = 102;
     private static final int DIALOG_STORE_CASTLE = 103;
     private static final int DIALOG_STORE_IGLOO = 104;
     private static final int DIALOG_STORE_CANNON = 105;
     private static final int DIALOG_STORE_FAIL = 106;
 	
 	// Name of the native dynamic libraries to load:
 	private static final String NATIVE_LIB_SAMPLE = "ImageTargets";
 	private static final String NATIVE_LIB_QCAR = "QCAR";
 	
 	
     Boolean stuffInited = false;
     
     private int currentLevel;
     Boolean PauseState = false;
     Boolean EOLState = false;
     
 	int level = 0;
 	int difficulty = 0;
 	int lives = 0;
     
 	// Our OpenGL view:
 	private QCARSampleGLView mGlView;
 	
 	AlertDialog.Builder levelBuilder = null;
 
 	// Our renderer:
 	private ImageTargetsRenderer mRenderer;
 
     private GUIManager mGUIManager;
 	
 	// Display size of the device
 	private int mScreenWidth = 0;
 	private int mScreenHeight = 0;
 
 	// The current application status
 	private int mAppStatus = APPSTATUS_UNINITED;
 
 	// The async tasks to initialize the QCAR SDK
 	private InitQCARTask mInitQCARTask;
 	private LoadTrackerTask mLoadTrackerTask;
 
 	// QCAR initialization flags
 	private int mQCARFlags = 0;
 
 	// The textures we will use for rendering:
 	private Vector<Texture> mTextures;
 
 	/** Static initializer block to load native libraries on start-up. */
 	static {
 		loadLibrary(NATIVE_LIB_QCAR);
 		loadLibrary(NATIVE_LIB_SAMPLE);
 	}
 
 	public static native int nativeBuy(int cost);
 	public static native int nativeUpgrade();
 	public static native void nativeNext();
 	public static native void nativeDelete();
 	public static native void nativeSettings(int level, int difficulty, int lives);
 	public static native void nativeGameOver();
 	
     /** Native function to store Java environment information for callbacks. */
     public native void initNativeCallback();
 	
 	/** An async task to initialize QCAR asynchronously. */
 	private class InitQCARTask extends AsyncTask<Void, Integer, Boolean> {
 		// Initialize with invalid value
 		private int mProgressValue = -1;
 
 		protected Boolean doInBackground(Void... params) {
 			QCAR.setInitParameters(ImageTargets.this, mQCARFlags);
 
 			do {
 				// QCAR.init() blocks until an initialization step is complete,
 				// then it proceeds to the next step and reports progress in
 				// percents (0 ... 100%)
 				// If QCAR.init() returns -1, it indicates an error.
 				// Initialization is done when progress has reached 100%.
 				mProgressValue = QCAR.init();
 
 				// Publish the progress value:
 				publishProgress(mProgressValue);
 
 				// We check whether the task has been canceled in the meantime
 				// (by calling AsyncTask.cancel(true))
 				// and bail out if it has, thus stopping this thread.
 				// This is necessary as the AsyncTask will run to completion
 				// regardless of the status of the component that started is.
 			} while (!isCancelled() && mProgressValue >= 0
 					&& mProgressValue < 100);
 
 			return (mProgressValue > 0);
 		}
 
 		protected void onProgressUpdate(Integer... values) {
 			// Do something with the progress value "values[0]", e.g. update
 			// splash screen, progress bar, etc.
 		}
 
 		protected void onPostExecute(Boolean result) {
 			// Done initializing QCAR, proceed to next application
 			// initialization status:
 			if (result) {
 				DebugLog.LOGD("InitQCARTask::onPostExecute: QCAR initialization"
 						+ " successful");
 
 				updateApplicationStatus(APPSTATUS_INIT_APP_AR);
 			} else {
 				// Create dialog box for display error:
 				AlertDialog dialogError = new AlertDialog.Builder(
 						ImageTargets.this).create();
 				dialogError.setButton("Close",
 						new DialogInterface.OnClickListener() {
 							public void onClick(DialogInterface dialog,
 									int which) {
 								// Exiting application
 								System.exit(1);
 							}
 						});
 
 				String logMessage;
 
 				// NOTE: Check if initialization failed because the device is
 				// not supported. At this point the user should be informed
 				// with a message.
 				if (mProgressValue == QCAR.INIT_DEVICE_NOT_SUPPORTED) {
 					logMessage = "Failed to initialize QCAR because this "
 							+ "device is not supported.";
 				} else if (mProgressValue == QCAR.INIT_CANNOT_DOWNLOAD_DEVICE_SETTINGS) {
 					logMessage = "Network connection required to initialize camera "
 							+ "settings. Please check your connection and restart "
 							+ "the application. If you are still experiencing "
 							+ "problems, then your device may not be currently "
 							+ "supported.";
 				} else {
 					logMessage = "Failed to initialize QCAR.";
 				}
 
 				// Log error:
 				DebugLog.LOGE("InitQCARTask::onPostExecute: " + logMessage
 						+ " Exiting.");
 
 				// Show dialog box with error message:
 				dialogError.setMessage(logMessage);
 				dialogError.show();
 			}
 		}
 	}
 
 	/** An async task to load the tracker data asynchronously. */
 	private class LoadTrackerTask extends AsyncTask<Void, Integer, Boolean> {
 		protected Boolean doInBackground(Void... params) {
 			// Initialize with invalid value
 			int progressValue = -1;
 
 			do {
 				progressValue = QCAR.load();
 				publishProgress(progressValue);
 
 			} while (!isCancelled() && progressValue >= 0
 					&& progressValue < 100);
 
 			return (progressValue > 0);
 		}
 
 		protected void onProgressUpdate(Integer... values) {
 			// Do something with the progress value "values[0]", e.g. update
 			// splash screen, progress bar, etc.
 		}
 
 		protected void onPostExecute(Boolean result) {
 			DebugLog.LOGD("LoadTrackerTask::onPostExecute: execution "
 					+ (result ? "successful" : "failed"));
 
 			// Done loading the tracker, update application status:
 			updateApplicationStatus(APPSTATUS_INITED);
 		}
 	}
 	
     public Dialog onCreateDialog(int id) {
     	AlertDialog dialog = null;
         switch (id) {
         case DIALOG_STORE:
             final CharSequence[] items = {"Castle: 7 ZP", "Igloo: 10 ZP", "Cannon: 15 ZP", "Castle2: 14 ZP", "Igloo2: 20 ZP", "Cannon2: 30 ZP"};
         	AlertDialog.Builder builder = new AlertDialog.Builder(this);
             builder.setTitle("Welcome to the Store! Buy:")
             .setItems(items, new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int item) {
                     int success = nativeBuy((int)item);
                     if (success == -1) {
                     	showDialog(DIALOG_STORE_FAIL);     	
                     }
                     else if (item == 0 || item == 3) {
                     	showDialog(DIALOG_STORE_CASTLE);
                     }
                     else if (item == 1 || item == 4) {
                     	showDialog(DIALOG_STORE_IGLOO);
                     }
                     else if (item == 2 || item == 5) {
                     	showDialog(DIALOG_STORE_CANNON);
                     }
                     else {
                     	showDialog(DIALOG_STORE_CONT);
                     }
                 }
             });
             dialog = builder.create();
             break;
             
         case DIALOG_EOL:
             final CharSequence[] items2 = {"Next Level", "Buy/Upgrade Towers", "Quit Game"};
         	AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
         	levelBuilder = builder2;
             builder2.setTitle("LEVEL COMPLETE")
             .setItems(items2, new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int item) {
                     
                 	
                 	if (item == 0) {
                     	mGUIManager.nativeLeave();
                      	nativeNext();
                      	mGUIManager.newLevel(String.valueOf(currentLevel+1));
                      	EOLState = false;
                      	PauseState = false;
                 	}
                     else if (item == 1) {
                     	mRenderer.hidePauseButton();
                     	mRenderer.showUnpauseButton();
                     	
                     }
                     else if (item == 2) {
                     	//end game
                     	updateApplicationStatus(APPSTATUS_INIT_APP);
                     }
                 }
             });
             dialog = builder2.create();
             break;
             
         case DIALOG_PAUSE:
             final CharSequence[] itemsp = {"Return to Game", "Pause to Manage Towers", "Quit Game"};
         	AlertDialog.Builder builderp = new AlertDialog.Builder(this);
             builderp.setTitle("GAME PAUSED")
             .setItems(itemsp, new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int item) {
                     
                 	
                 	if (item == 0) {
  
                     	mGUIManager.nativeLeave();              
                     	
                 	}
                     else if (item == 1) {
                     	mRenderer.hidePauseButton();
                     	mRenderer.showUnpauseButton();
                     }
                     else if (item == 2) {
                     	//end game
                        	nativeGameOver();  
                     }
                 }
             });
             dialog = builderp.create();
             break;
             
         case DIALOG_STORE_CONT:
             final CharSequence[] items3 = {"Return to Game"};
         	AlertDialog.Builder builder3 = new AlertDialog.Builder(this);
             builder3.setTitle("Bought another building! ")
             .setItems(items3, new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int item) {
                 	if (PauseState != true) {
                 		mGUIManager.nativeLeave();
                 	}
                 }
             });
             dialog = builder3.create();
             break;
             
         case DIALOG_GAMEOVER:
             final CharSequence[] itemsg = {"Click here to continue"};
         	AlertDialog.Builder builderg = new AlertDialog.Builder(this);
             TextView currentScore = (TextView) findViewById(R.id.current_score);
             builderg.setTitle("GAME OVER! Final Score: " + currentScore.getText())
             //builderg.setTitle("GAME OVER\n\nFINAL SCORE: ")
             .setItems(itemsg, new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int item) {
                 	
                 	if (item == 0) {
                 		stopCamera();
                 		updateApplicationStatus(APPSTATUS_GAMEOVER);
                 	}
                 }
             });
             dialog = builderg.create();
             break;
             
         case DIALOG_STORE_CASTLE:
             final CharSequence[] items4 = {"Return to Game"};
         	AlertDialog.Builder builder4 = new AlertDialog.Builder(this);
             builder4.setTitle("Bought a Castle!")
             .setItems(items4, new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int item) {
                 	if (PauseState != true) {
                 		mGUIManager.nativeLeave();
                 	}
                 }
             });
             dialog = builder4.create();
             break;
             
         case DIALOG_STORE_IGLOO:
             final CharSequence[] items5 = {"Return to Game"};
         	AlertDialog.Builder builder5 = new AlertDialog.Builder(this);
             builder5.setTitle("Bought an Igloo!")
             .setItems(items5, new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int item) {
                 	if (PauseState != true) {
                 		mGUIManager.nativeLeave();
                 	}
                 }
             });
             dialog = builder5.create();
             break;
             
         case DIALOG_STORE_CANNON:
             final CharSequence[] items6 = {"Return to Game"};
         	AlertDialog.Builder builder6 = new AlertDialog.Builder(this);
             builder6.setTitle("Bought a Cannon!")
             .setItems(items6, new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int item) {
                 	if (PauseState != true) {
                 		mGUIManager.nativeLeave();
                 	}
                 }
             });
             dialog = builder6.create();
             break;
         case DIALOG_STORE_FAIL:
             final CharSequence[] items7 = {"Return to Game"};
         	AlertDialog.Builder builder7 = new AlertDialog.Builder(this);
             builder7.setTitle("Not enough ZP!")
             .setItems(items7, new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int item) {
                 	if (PauseState != true) {
                 		mGUIManager.nativeLeave();
                 	}
                 }
             });
             dialog = builder7.create();
             break;
         }
         return dialog;
         
     }
 	
 	/**
 	 * Called when the activity first starts or the user navigates back to an
 	 * activity.
 	 */
     private GestureDetector mGestureDetector;
     
 	protected void onCreate(Bundle savedInstanceState) {
 		DebugLog.LOGD("ImageTargets::onCreate");
 		super.onCreate(savedInstanceState);
 		
 		// Load any sample specific textures:
 		mTextures = new Vector<Texture>();
 		loadTextures();
 
 		// Query the QCAR initialization flags:
 		mQCARFlags = getInitializationFlags();
 
 		// Update the application status to start initializing application
 		updateApplicationStatus(APPSTATUS_INIT_APP);
 		mGestureDetector = new GestureDetector(this, new TapGestureListener());
 	}
 
 	/**
 	 * We want to load specific textures from the APK, which we will later use
 	 * for rendering.
 	 */
 	private void loadTextures() {
 		mTextures.add(Texture.loadTextureFromApk("tower.png",
 				getAssets()));//0
 		mTextures.add(Texture.loadTextureFromApk("cow.png",
 				getAssets()));//1
 		mTextures.add(Texture.loadTextureFromApk("arrow_bake.png",
 				getAssets()));//2
 		mTextures.add(Texture.loadTextureFromApk("igloo.png",
 				getAssets()));//3
 		mTextures.add(Texture.loadTextureFromApk("snowball.png",
 				getAssets()));//4
 		mTextures.add(Texture.loadTextureFromApk("zombie1.png",
 				getAssets()));//5
 		mTextures.add(Texture.loadTextureFromApk("tank2.png",
 				getAssets()));//6
 		mTextures.add(Texture.loadTextureFromApk("spaceship2.png",
 				getAssets()));//7
 		mTextures.add(Texture.loadTextureFromApk("grid_sample1_shrunk.jpg",
 				getAssets()));//8
 		mTextures.add(Texture.loadTextureFromApk("cannon.jpg",
 				getAssets()));//9
 		mTextures.add(Texture.loadTextureFromApk("hp_bar_10.png",
 				getAssets()));//10
 		mTextures.add(Texture.loadTextureFromApk("hp_bar_full.png",
 				getAssets()));//11
 		mTextures.add(Texture.loadTextureFromApk("hp_bar_90.png",
 				getAssets()));//12
 		mTextures.add(Texture.loadTextureFromApk("hp_bar_80.png",
 				getAssets()));//13
 		mTextures.add(Texture.loadTextureFromApk("hp_bar_70.png",
 				getAssets()));//14
 		mTextures.add(Texture.loadTextureFromApk("hp_bar_60.png",
 				getAssets()));//15
 		mTextures.add(Texture.loadTextureFromApk("hp_bar_50.png",
 				getAssets()));//16
 		mTextures.add(Texture.loadTextureFromApk("hp_bar_40.png",
 				getAssets()));//17
 		mTextures.add(Texture.loadTextureFromApk("hp_bar_30.png",
 				getAssets()));//18
 		mTextures.add(Texture.loadTextureFromApk("hp_bar_20.png",
 				getAssets()));//19
 		mTextures.add(Texture.loadTextureFromApk("hp_bar_10.png",
 				getAssets()));//20
 		mTextures.add(Texture.loadTextureFromApk("tower_up1.png",
 				getAssets()));//21
 		mTextures.add(Texture.loadTextureFromApk("tower_up2.png",
 				getAssets()));//22
 		mTextures.add(Texture.loadTextureFromApk("tower_up3.png",
 				getAssets()));//23
 		mTextures.add(Texture.loadTextureFromApk("igloo_up1.png",
 				getAssets()));//24
 		mTextures.add(Texture.loadTextureFromApk("igloo_up2.png",
 				getAssets()));//25
 		mTextures.add(Texture.loadTextureFromApk("igloo_up3.png",
 				getAssets()));//26
 		mTextures.add(Texture.loadTextureFromApk("cannon_up1.jpg",
 				getAssets()));//27
 		mTextures.add(Texture.loadTextureFromApk("cannon_up2.jpg",
 				getAssets()));//28
 		mTextures.add(Texture.loadTextureFromApk("cannon_up3.jpg",
 				getAssets()));//29
 		mTextures.add(Texture.loadTextureFromApk("green_glow.png",
 				getAssets()));//30
 		mTextures.add(Texture.loadTextureFromApk("blue_glow.png",
 				getAssets()));//31
 		mTextures.add(Texture.loadTextureFromApk("level_path_1_shrunk.png",
 				getAssets()));//32
 	}
 
 	/** Configure QCAR with the desired version of OpenGL ES. */
 	private int getInitializationFlags() {
 		int flags = 0;
 
 		// Query the native code:
 		if (getOpenGlEsVersionNative() == 1) {
 			flags = QCAR.GL_11;
 		} else {
 			flags = QCAR.GL_20;
 		}
 
 		return flags;
 	}
 
 	/**
 	 * native method for querying the OpenGL ES version. Returns 1 for OpenGl ES
 	 * 1.1, returns 2 for OpenGl ES 2.0.
 	 */
 	public native int getOpenGlEsVersionNative();
 
 	/** Native sample initialization. */
 	public native void onQCARInitializedNative();
 
 	/** Native methods for starting and stoping the camera. */
 	private native void startCamera();
 
 	private native void stopCamera();
 	
 	/** Called when the activity will start interacting with the user. */
 	protected void onResume() {
 		DebugLog.LOGD("ImageTargets::onResume");
 		super.onResume();
 
 		// QCAR-specific resume operation
 		QCAR.onResume();
 
 		// We may start the camera only if the QCAR SDK has already been
 		// initialized
 		if (mAppStatus == APPSTATUS_CAMERA_STOPPED)
 			updateApplicationStatus(APPSTATUS_CAMERA_RUNNING);
 
 		// Resume the GL view:
 		if (mGlView != null) {
 			mGlView.setVisibility(View.VISIBLE);
 			mGlView.onResume();
 		}
         if (mGUIManager != null)
         {
             //mGUIManager.initButtons();
         }
 	}
 
 	/** Called when the system is about to start resuming a previous activity. */
 	protected void onPause() {
 		DebugLog.LOGD("ImageTargets::onPause");
 		super.onPause();
 
 		if (mGlView != null) {
 			mGlView.setVisibility(View.INVISIBLE);
 			mGlView.onPause();
 		}
 
 		// QCAR-specific pause operation
 		QCAR.onPause();
 
 		if (mAppStatus == APPSTATUS_CAMERA_RUNNING) {
 			updateApplicationStatus(APPSTATUS_CAMERA_STOPPED);
 		}
 	}
 
 	/** Native function to deinitialize the application. */
 	private native void deinitApplicationNative();
 
 	/** The final call you receive before your activity is destroyed. */
 	protected void onDestroy() {
 		DebugLog.LOGD("ImageTargets::onDestroy");
 		super.onDestroy();
 
 		// Cancel potentially running tasks
 		if (mInitQCARTask != null
 				&& mInitQCARTask.getStatus() != InitQCARTask.Status.FINISHED) {
 			mInitQCARTask.cancel(true);
 			mInitQCARTask = null;
 		}
 
 		if (mLoadTrackerTask != null
 				&& mLoadTrackerTask.getStatus() != LoadTrackerTask.Status.FINISHED) {
 			mLoadTrackerTask.cancel(true);
 			mLoadTrackerTask = null;
 		}
 
 		// Do application deinitialization in native code
 		deinitApplicationNative();
 
 		// Unload texture
 		mTextures.clear();
 		mTextures = null;
 
 		// Deinitialize QCAR SDK
 		QCAR.deinit();
 
 		System.gc();
 	}
 
 	
 	
 	//back button will go back to main menu
 	@Override
 	public void onBackPressed() {
 		if (mAppStatus == APPSTATUS_INIT_MENU)
 			updateApplicationStatus(APPSTATUS_INIT_APP);
 	}
 	
 	/**
 	 * NOTE: this method is synchronized because of a potential concurrent
 	 * access by ImageTargets::onResume() and InitQCARTask::onPostExecute().
 	 */
 	
 
 	
 	private synchronized void updateApplicationStatus(int appStatus) {
 		
 		arrow_sound = MediaPlayer.create(this,R.raw.arrow);
 		snowball_sound = MediaPlayer.create(this,R.raw.snowball);
 		cannon_sound = MediaPlayer.create(this,R.raw.cannon);
 		level_sound = MediaPlayer.create(this,R.raw.harp);
 		button_sound = MediaPlayer.create(this,R.raw.button);
 		
 		// Exit if there is no change in status
 		if (mAppStatus == appStatus)
 			return;
 
 		// Store new status value
 		mAppStatus = appStatus;
 
 		// Execute application state-specific actions
 		switch (mAppStatus) {
 		case APPSTATUS_INIT_APP:
 			// Initialize application elements that do not rely on QCAR
 			// initialization
 			initApplication();
 		    initNativeCallback();
 			//Show main menu
 	        updateApplicationStatus(APPSTATUS_INIT_MENU);
 			
 			break;
 			
 		case APPSTATUS_INIT_MENU:
 			
 			setContentView(R.layout.main);
 			
 			//New Game Button
 			Button StartGameButton = (Button)findViewById(R.id.new_game_button);
 	        StartGameButton.setOnClickListener(new OnClickListener() {
 	        	
 	        	public void onClick(View v) {
 	        		setContentView(R.layout.level_select);
 	        		Button StartGameButton2 = (Button)findViewById(R.id.start_game_button);
 	        		
     				
     				StartGameButton2.setOnClickListener(new OnClickListener() {
 	        			public void onClick(View v) {
 	        				RadioButton level1_button = (RadioButton) findViewById(R.id.radioButton_level1);
 	        				RadioButton level2_button = (RadioButton) findViewById(R.id.radioButton_level2);
 	        				RadioButton level3_button = (RadioButton) findViewById(R.id.radioButton_level3);
 	        				RadioButton difficulty1_button = (RadioButton) findViewById(R.id.radioButton_difficulty1);
 	        				RadioButton difficulty2_button = (RadioButton) findViewById(R.id.radioButton_difficulty2);
 	        				RadioButton difficulty3_button = (RadioButton) findViewById(R.id.radioButton_difficulty3);
 	        				RadioButton lives1_button = (RadioButton) findViewById(R.id.radioButton_lives1);
 	        				RadioButton lives2_button = (RadioButton) findViewById(R.id.radioButton_lives2);
 	        				RadioButton lives3_button = (RadioButton) findViewById(R.id.radioButton_lives3);
 	        				if (level1_button.isChecked()) {
 	        					level = 1;
 	        				}
 	        				else if (level2_button.isChecked()) {
 	        					level = 2;
 	        				}
 	        				else if (level3_button.isChecked()) {
 	        					level = 3;
 	        				}
 	        				
 	        				if (difficulty1_button.isChecked()) {
 	        					difficulty = 1;
 	        				}
 	        				else if (difficulty2_button.isChecked()) {
 	        					difficulty = 2;
 	        				}
 	        				else if (difficulty3_button.isChecked()) {
 	        					difficulty = 3;
 	        				}
 	        				
 	        				if (lives1_button.isChecked()) {
 	        					lives = 20;
 	        				}
 	        				else if (lives2_button.isChecked()) {
 	        					lives = 10;
 	        				}
 	        				else if (lives3_button.isChecked()) {
 	        					lives = 5;
 	        				}
 	        				
 	        				nativeSettings(level, difficulty, lives);
 	        				updateApplicationStatus(APPSTATUS_INIT_QCAR);
 	        			}
 	        		});
 	        	}
 	        });
 			
 	      //Game Rules Button
 	        Button GameRulesButton = (Button)findViewById(R.id.game_rules_button);
 	        GameRulesButton.setOnClickListener(new OnClickListener() {
 	        	
 	        	public void onClick(View v) {
 	        		setContentView(R.layout.game_rules);
 	        	}
 	        });
 	        
 	      //Settings Button
 	        Button SettingsButton = (Button)findViewById(R.id.settings_button);
 	        SettingsButton.setOnClickListener(new OnClickListener() {
 	        	
 	        	public void onClick(View v) {
 	        		setContentView(R.layout.settings);
 	        		/*audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
 		            int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
 		            int curVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
 	        		SeekBar volControl = (SeekBar)findViewById(R.id.volbar);
 	        		
 	        		volControl.setMax(maxVolume);
 	        		volControl.setProgress(curVolume);
 	        		volControl.setOnSeenBarChangeListener(this);*/
 	        		
 	        		audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
 	        	    int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
 	        	    int curVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
 	        	    SeekBar volControl = (SeekBar)findViewById(R.id.volbar);
 	        	    volControl.setMax(maxVolume);
 	        	    volControl.setProgress(curVolume);
 	        	    volControl.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
 	        	        @Override
 	        	        public void onStopTrackingTouch(SeekBar arg0) {
 	        	        }
 
 	        	        @Override
 	        	        public void onStartTrackingTouch(SeekBar arg0) {
 	        	        }
 
 	        	        @Override
 	        	        public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
 	        	            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, arg1, 0);
 	        	        }
 	        	    });
 	        		
 	        		
 	        	}
 	        });
 	        
 	      //Credits Button
 	        Button CreditsButton = (Button)findViewById(R.id.credits_button);
 	        CreditsButton.setOnClickListener(new OnClickListener() {
 	        	
 	        	public void onClick(View v) {
 	        		setContentView(R.layout.credits);
 	        	}
 	        });
 			
 			break;
 		
 		
 			
 		case APPSTATUS_INIT_QCAR:
 			// Initialize QCAR SDK asynchronously to avoid blocking the
 			// main (UI) thread.
 			// This task instance must be created and invoked on the UI
 			// thread and it can be executed only once!
 			try {
 				mInitQCARTask = new InitQCARTask();
 				mInitQCARTask.execute();
 			} catch (Exception e) {
 				DebugLog.LOGE("Initializing QCAR SDK failed");
 			}
 			break;
 
 		case APPSTATUS_INIT_APP_AR:
 			// Initialize Augmented Reality-specific application elements
 			// that may rely on the fact that the QCAR SDK has been
 			// already initialized
 			initApplicationAR();
 
 			// Proceed to next application initialization status
 			updateApplicationStatus(APPSTATUS_INIT_TRACKER);
 			break;
 
 		case APPSTATUS_INIT_TRACKER:
 			// Load the tracking data set
 			//
 			// This task instance must be created and invoked on the UI
 			// thread and it can be executed only once!
 			try {
 				mLoadTrackerTask = new LoadTrackerTask();
 				mLoadTrackerTask.execute();
 			} catch (Exception e) {
 				DebugLog.LOGE("Loading tracking data set failed");
 			}
 			break;
 
 		case APPSTATUS_INITED:
 			// Hint to the virtual machine that it would be a good time to
 			// run the garbage collector.
 			//
 			// NOTE: This is only a hint. There is no guarantee that the
 			// garbage collector will actually be run.
 			System.gc();
 
 			// Native post initialization:
 			onQCARInitializedNative();
 			// Request a callback function after a given timeout to dismiss
 			// the splash screen:
 			Handler handler = new Handler();
 			handler.postDelayed(new Runnable() {
 				public void run() {
 					// Activate the renderer
 					mRenderer.mIsActive = true;
 
 						// Now add the GL surface view. It is important
 						// that the OpenGL ES surface view gets added
 						// BEFORE the camera is started and video
 						// background is configured.
 					
 					setContentView(mGlView, new LayoutParams(
 							LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
 					
                     addContentView(mGUIManager.getOverlayView(), new LayoutParams(
                             LayoutParams.FILL_PARENT,
                             LayoutParams.FILL_PARENT));
 					
                     mGUIManager.initButtons();
 
                     mGUIManager.newLives(String.valueOf(lives));
                     
                     Button storeButton;
                     storeButton = (Button) mGUIManager.getOverlayView().findViewById(R.id.store_button);
                     storeButton.setOnClickListener(new View.OnClickListener() {
                         public void onClick(View v) {
                             mGUIManager.nativeStore();
                             showDialog(DIALOG_STORE);
                         }
                     });
                     
                     Button pauseButton;
                     pauseButton = (Button) mGUIManager.getOverlayView().findViewById(R.id.pause_button);
                     pauseButton.setOnClickListener(new View.OnClickListener() {
                         public void onClick(View v) {
                         	PauseState = true;
                         	mGUIManager.nativePause();
                         	showDialog(DIALOG_PAUSE);   
                         }
                     });
                     
                     Button upgradeButton;
                     upgradeButton = (Button) mGUIManager.getOverlayView().findViewById(R.id.upgrade_button);
                     upgradeButton.setOnClickListener(new View.OnClickListener() {
                         public void onClick(View v) {
                             nativeUpgrade();
                         }
                     });
                     
                     Button deleteButton;
                     deleteButton = (Button) mGUIManager.getOverlayView().findViewById(R.id.delete_button);
                     deleteButton.setOnClickListener(new View.OnClickListener() {
                         public void onClick(View v) {
                             nativeDelete();
                         }
                     });
                     
                     Button unpauseButton;
                     unpauseButton = (Button) mGUIManager.getOverlayView().findViewById(R.id.unpause_button);
                     unpauseButton.setOnClickListener(new View.OnClickListener() {
                         public void onClick(View v) {
 
                         	PauseState = false;
                         	mRenderer.showPauseButton();
                         	mRenderer.hideUnpauseButton();
                         	mGUIManager.nativeUnpause();
                         	
                         	if (EOLState == true) {
                             	EOLState = false;
                              	mGUIManager.newLevel(String.valueOf(currentLevel+1));
                             	nativeNext();
                         	}
                         }
                     });
 					// Start the camera:
         			stuffInited = true;
 					updateApplicationStatus(APPSTATUS_CAMERA_RUNNING);
 					
 				}
 			}, 0);
 			break;
 
 		case APPSTATUS_GAMEOVER:
 			
 			setContentView(R.layout.endgame);
 			break;
 			
 		case APPSTATUS_CAMERA_STOPPED:
 			// Call the native function to stop the camera
 			stopCamera();
 			break;
 
 		case APPSTATUS_CAMERA_RUNNING:
 			// Call the native function to start the camera
 			startCamera();
 			break;
 
 		default:
 			throw new RuntimeException("Invalid application state");
 		}
 	}
 
 	/** Tells native code whether we are in portait or landscape mode */
 	private native void setActivityPortraitMode(boolean isPortrait);
 
 	/** Initialize application GUI elements that are not related to AR. */
 	private void initApplication() {
 		// Set the screen orientation
 		//
 		// NOTE: It is recommended to set this because of the following reasons:
 		//
 		// 1.) Before Android 2.2 there is no reliable way to query the
 		// absolute screen orientation from an activity, therefore using
 		// an undefined orientation is not recommended. Screen
 		// orientation matching orientation sensor measurements is also
 		// not recommended as every screen orientation change triggers
 		// deinitialization / (re)initialization steps in internal QCAR
 		// SDK components resulting in unnecessary overhead during
 		// application run-time.
 		//
 		// 2.) Android camera drivers seem to always deliver landscape images
 		// thus QCAR SDK components (e.g. camera capturing) need to know
 		// when we are in portrait mode. Before Android 2.2 there is no
 		// standard, device-independent way to let the camera driver know
 		// that we are in portrait mode as each device seems to require a
 		// different combination of settings to rotate camera preview
 		// frames images to match portrait mode views. Because of this,
 		// we suggest that the activity using the QCAR SDK be locked
 		// to landscape mode if you plan to support Android 2.1 devices
 		// as well. Froyo is fine with both orientations.
 		int screenOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
 
 		// Apply screen orientation
 		setRequestedOrientation(screenOrientation);
 
 		// Pass on screen orientation info to native code
 		setActivityPortraitMode(screenOrientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
 
 		// Query display dimensions
 		DisplayMetrics metrics = new DisplayMetrics();
 		getWindowManager().getDefaultDisplay().getMetrics(metrics);
 		mScreenWidth = metrics.widthPixels;
 		mScreenHeight = metrics.heightPixels;
 
 		// As long as this window is visible to the user, keep the device's
 		// screen turned on and bright.
 		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
 				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
 
 	}
 
 	/** Native function to initialize the application. */
 	private native void initApplicationNative(int width, int height);
 
 	/** Initializes AR application components. */
 	private void initApplicationAR() {
 		// Do application initialization in native code (e.g. registering
 		// callbacks, etc.)
 		initApplicationNative(mScreenWidth, mScreenHeight);
 
 		// Create OpenGL ES view:
 		int depthSize = 16;
 		int stencilSize = 0;
 		boolean translucent = QCAR.requiresAlpha();
 
 		mGlView = new QCARSampleGLView(this);
 		mGlView.init(mQCARFlags, translucent, depthSize, stencilSize);
 
 		mRenderer = new ImageTargetsRenderer();
 		mGlView.setRenderer(mRenderer);
 
         mGUIManager = new GUIManager(getApplicationContext());
         mRenderer.setGUIManager(mGUIManager);
 		
 	}
 
 	/**
 	 * Invoked the first time when the options menu is displayed to give the
 	 * Activity a chance to populate its Menu with menu items.
 	 */
 	public boolean onCreateOptionsMenu(Menu menu) {
 		super.onCreateOptionsMenu(menu);
 
 		menu.add("Toggle flash");
 		menu.add("Autofocus");
 		menu.add("Nothing");
 		SubMenu focusModes = menu.addSubMenu("Focus Modes");
 		focusModes.add("Auto Focus").setCheckable(true);
 		focusModes.add("Fixed Focus").setCheckable(true);
 		focusModes.add("Infinity").setCheckable(true);
 		focusModes.add("Macro Mode").setCheckable(true);
 
 		return true;
 	}
 
 	/** Invoked when the user selects an item from the Menu */
 	public boolean onOptionsItemSelected(MenuItem item) {
 		if (item.getTitle().equals("Toggle flash")) {
 			mFlash = !mFlash;
 			boolean result = toggleFlash(mFlash);
 			DebugLog.LOGI("Toggle flash " + (mFlash ? "ON" : "OFF") + " "
 					+ (result ? "WORKED" : "FAILED") + "!!");
 		} else if (item.getTitle().equals("Autofocus")) {
 			boolean result = autofocus();
 			DebugLog.LOGI("Autofocus requested"
 					+ (result ? " successfully."
 							: ".  Not supported in current mode or on this device."));
 		} else {
 			int arg = -1;
 			if (item.getTitle().equals("Auto Focus"))
 				arg = 0;
 			if (item.getTitle().equals("Fixed Focus"))
 				arg = 1;
 			if (item.getTitle().equals("Infinity"))
 				arg = 2;
 			if (item.getTitle().equals("Macro Mode"))
 				arg = 3;
 
 			if (arg != -1) {
 				item.setChecked(true);
 				if (checked != null)
 					checked.setChecked(false);
 				checked = item;
 
 				boolean result = setFocusMode(arg);
 
 				DebugLog.LOGI("Requested Focus mode "
 						+ item.getTitle()
 						+ (result ? " successfully."
 								: ".  Not supported on this device."));
 			}
 		}
 
 		return true;
 	}
 
 	private MenuItem checked;
 	private boolean mFlash = false;
 
 	private native boolean toggleFlash(boolean flash);
 
 	private native boolean autofocus();
 
 	private native boolean setFocusMode(int mode);
 
 	public void updateEOL(String level) {
 		PauseState = true;
 		EOLState = true;
 		currentLevel = Integer.parseInt(level);
 		
 		mGUIManager.newLevel("END "+level);
 
     	runOnUiThread(new Runnable() {
     		
     	     public void run() {
 
              	mGUIManager.nativeStore();
     	     	showDialog(DIALOG_EOL);
     	     }
     	});
 		
 	}
 	
 	public void updateGameOver() {
 		PauseState = true;
 		EOLState = true;
 
     	runOnUiThread(new Runnable() {
     		
     	     public void run() {
     	     	showDialog(DIALOG_GAMEOVER);
     	     }
     	});
 		
 	}
 
 	/** Returns the number of registered textures. */
 	public int getTextureCount() {
 		return mTextures.size();
 	}
 
 	/** Returns the texture object at the specified index. */
 	public Texture getTexture(int i) {
 		return mTextures.elementAt(i);
 	}
 
 	/** A helper for loading native libraries stored in "libs/armeabi*". */
 	public static boolean loadLibrary(String nLibName) {
 		try {
 			System.loadLibrary(nLibName);
 			DebugLog.LOGI("Native library lib" + nLibName + ".so loaded");
 			return true;
 		} catch (UnsatisfiedLinkError ulee) {
 			DebugLog.LOGE("The library lib" + nLibName
 					+ ".so could not be loaded");
 		} catch (SecurityException se) {
 			DebugLog.LOGE("The library lib" + nLibName
 					+ ".so was not allowed to be loaded");
 		}
 
 		return false;
 	}
 	
 
 
     @Override
     public boolean onTouchEvent(MotionEvent event)
     {
         if (mGestureDetector.onTouchEvent(event))
             return true;
         else
             return false;
     }
     
     private native boolean nativeTapEvent(float tapX, float tapY);
     
     class TapGestureListener extends GestureDetector.SimpleOnGestureListener
     {
         @Override
         public boolean onSingleTapUp(MotionEvent ev)
         {
             nativeTapEvent(ev.getX(), ev.getY());
             return true;
         }
     }
 	
     public void call_arrow_sound()
     {
         //arrow_sound.start();
     }
     
     public void call_snowball_sound()
     {
         //snowball_sound.start();
     }
     public void call_cannon_sound()
     {
         //cannon_sound.start();
     }
     public void call_level_sound()
     {
         //level_sound.start();
     }
     public void call_button_sound()
     {
         //button_sound.start();
     }
 	
 }
