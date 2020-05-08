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
 import android.content.DialogInterface;
 import android.content.pm.ActivityInfo;
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
 import android.widget.Toast;
 
 import android.widget.ToggleButton;
 
 import com.qualcomm.QCAR.QCAR;
 import com.qualcomm.QCARSamples.ImageTargets.GUIManager;
 
 /** The main activity for the ImageTargets sample. */
 public class ImageTargets extends Activity {
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
 
     private static final int DIALOG_STORE = 100;
     private static final int DIALOG_EOL = 101;
     private static final int DIALOG_STORE_CONT = 102;
 	
 	// Name of the native dynamic libraries to load:
 	private static final String NATIVE_LIB_SAMPLE = "ImageTargets";
 	private static final String NATIVE_LIB_QCAR = "QCAR";
     private int currentLevel;
 	// Our OpenGL view:
 	private QCARSampleGLView mGlView;
 
 	// The view to display the sample splash screen:
 	//private ImageView mSplashScreenView;
 
 	// The minimum time the splash screen should be visible:
 	//private static final long MIN_SPLASH_SCREEN_TIME = 2000;
 
 	// The time when the splash screen has become visible:
 	//long mSplashScreenStartTime = 0;
 
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
 	//private int mSplashScreenImageResource = 0;
 
 	/** Static initializer block to load native libraries on start-up. */
 	static {
 		loadLibrary(NATIVE_LIB_QCAR);
 		loadLibrary(NATIVE_LIB_SAMPLE);
 	}
 
 	public static native void nativeBuy(int cost);
 	public static native void nativeNext();
 	
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
             final CharSequence[] items = {"Castle: 1 ZP", "Igloo: 2 ZP", "Terran Bunker: 3 ZP", "Protoss Cannon: 4 ZP", "Protoss Cannon: 5 ZP", "Protoss Cannon: 6 ZP"};
         	AlertDialog.Builder builder = new AlertDialog.Builder(this);
             builder.setTitle("Welcome to the Store! Buy:")
             .setItems(items, new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int item) {
                     nativeBuy((int)(item+1));
                    	int duration = Toast.LENGTH_LONG;
                     Toast toast = Toast.makeText(getApplicationContext(), "Spent " + (item+1), duration);
                     toast.show();
                 	showDialog(DIALOG_STORE_CONT);
                 }
             });
             dialog = builder.create();
             break;
             
         case DIALOG_EOL:
             final CharSequence[] items2 = {"Next Level", "Enter Store", "Quit Game"};
         	AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
             builder2.setTitle("COMPLETE LEVEL " + currentLevel)
             .setItems(items2, new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int item) {
                     
                 	
                 	if (item == 0) {
                 		//TODO: Test if need to run on UIThread
                     	mGUIManager.nativeLeave();
                 	}
                     else if (item == 1) {
                     	runOnUiThread(new Runnable() {
                     		
                       	     public void run() {
                              	showDialog(DIALOG_STORE);
                       	     }
                       	});
 
                     }
                     else if (item == 2) {
                     	//end game
                     	updateApplicationStatus(APPSTATUS_CAMERA_STOPPED);
                     }
                 }
             });
             dialog = builder2.create();
             break;
             
         case DIALOG_STORE_CONT:
             final CharSequence[] items3 = {"Continue Buying", "Return to Game"};
         	AlertDialog.Builder builder3 = new AlertDialog.Builder(this);
             builder3.setTitle("Continue Buying?")
             .setItems(items3, new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int item) {
                  	if (item == 0) {
                     	runOnUiThread(new Runnable() {
                     		
                      	     public void run() {
                             	showDialog(DIALOG_STORE);
                      	     }
                      	});
                 	}
                     else if (item == 1) {
                      	mGUIManager.nativeLeave();
                     }
                 }
             });
             dialog = builder3.create();
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
 		
 		// Set the splash screen image to display during initialization:
 		//mSplashScreenImageResource = R.drawable.splash_screen_image_targets;
 
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
 		mTextures.add(Texture.loadTextureFromApk("tower_bake.png",
 				getAssets()));
 		mTextures.add(Texture.loadTextureFromApk("horse_cow.png",
 				getAssets()));
 		mTextures.add(Texture.loadTextureFromApk("arrow_bake.png",
 				getAssets()));
 		//mTextures.add(Texture.loadTextureFromApk("dwight_as_jim.gif",
 		//		getAssets()));
 		mTextures.add(Texture.loadTextureFromApk("igloo.png",
 				getAssets()));
 		mTextures.add(Texture.loadTextureFromApk("snowball.png",
 				getAssets()));
 		//mTextures.add(Texture.loadTextureFromApk("arrow_bake.png",
 		//		getAssets()));
 		//mTextures.add(Texture.loadTextureFromApk("zombie1.png",
 		//		getAssets()));
 		
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
             mGUIManager.initButtons();
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
 	        		updateApplicationStatus(APPSTATUS_INIT_QCAR);
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
 	        		
 	        	}
 	        });
 	        
 	      //New Game Button
 	        Button CreditsButton = (Button)findViewById(R.id.credits_button);
 	        CreditsButton.setOnClickListener(new OnClickListener() {
 	        	
 	        	public void onClick(View v) {
 	        		setContentView(R.layout.credits);
 	        	}
 	        });
 			
 			break;
 			/*
 		case APPSTATUS_INIT_EOL:
 			
 	    	runOnUiThread(new Runnable() {
 	    		
 	    	     public void run() {
 	    	    	 
 	    	    	 setContentView(R.layout.levelend);
 	    				
 	    				//Next Level Button
 	    				Button EOLLevelButton = (Button)findViewById(R.id.eol_level_button);
 	    				EOLLevelButton.setOnClickListener(new OnClickListener() {
 	    		        	
 	    		        	public void onClick(View v) {
 	    		        		//TODO: Close the screen and get back to the game
 	    		        		//updateApplicationStatus(APPSTATUS_CAMERA_RUNNING);
 	    		        		//next level
 	    		        	}
 	    		        });
 	    				
 	    		      //Enter Store Button
 	    		        Button EOLStoreButton = (Button)findViewById(R.id.eol_store_button);
 	    		        EOLStoreButton.setOnClickListener(new OnClickListener() {
 	    		        	
 	    		        	public void onClick(View v) {
 	    		        		//TODO: Store here
 	    		        		setContentView(R.layout.game_rules);
 	    		        	}
 	    		        });
 	    		        
 	    		      //Quit Game Button
 	    		        Button EOLQuitButton = (Button)findViewById(R.id.eol_quit_button);
 	    		        EOLQuitButton.setOnClickListener(new OnClickListener() {
 	    		        	
 	    		        	public void onClick(View v) {
 	    		        		//TODO: Quit here
 	    		        		setContentView(R.layout.game_rules);	
 	    		        	}
 	    		        });
 	    				
 	    	     }
 	    	});
 			
 			
 			break;
 			*/
 		
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
 
 			// The elapsed time since the splash screen was visible:
 			/*long splashScreenTime = System.currentTimeMillis()
 					- mSplashScreenStartTime;
 			long newSplashScreenTime = 0;
 			if (splashScreenTime < MIN_SPLASH_SCREEN_TIME) {
 				newSplashScreenTime = MIN_SPLASH_SCREEN_TIME - splashScreenTime;
 			}
 			*/
 
 			// Request a callback function after a given timeout to dismiss
 			// the splash screen:
 			Handler handler = new Handler();
 			handler.postDelayed(new Runnable() {
 				public void run() {
 					// Hide the splash screen
 					//mSplashScreenView.setVisibility(View.INVISIBLE);
 
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
                     //TODO: There can only be one on click listener... but I can't create the showDialog from the GUI Manager, not the activity
                     Button storeButton;
                     storeButton = (Button) mGUIManager.getOverlayView().findViewById(R.id.store_button);
                     storeButton.setOnClickListener(new View.OnClickListener() {
                         public void onClick(View v) {
                             mGUIManager.nativeStore();
                             showDialog(DIALOG_STORE);
                         }
                     });
                     
 					// Start the camera:
 					updateApplicationStatus(APPSTATUS_CAMERA_RUNNING);
 				}
 			}, 0); //newSplashScreenTime);
 
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
 
 		//TODO: Can this be completely removed?
 		/*
 		// Create and add the splash screen view
 		mSplashScreenView = new ImageView(this);
 		mSplashScreenView.setImageResource(mSplashScreenImageResource);
 		addContentView(mSplashScreenView, new LayoutParams(
 		LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
 
 		mSplashScreenStartTime = System.currentTimeMillis();
 		*/
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
 
 	
 	//TODO: let's hope this works
 	public void updateEOL(String level) {
 		currentLevel = Integer.parseInt(level);
 
 
 		//updateApplicationStatus(APPSTATUS_CAMERA_STOPPED);
 		//Changing from "xml" style EOL menu to "store" style menu
 		/*updateApplicationStatus(APPSTATUS_INIT_EOL);
 		
     	final String temp = level;
         currentLevel = (TextView) mGUIManager.getOverlayView().findViewById(R.id.current_level);
     	*///extended activity. Hopefully not too much overhead?
     	runOnUiThread(new Runnable() {
     		
     	     public void run() {
              	mGUIManager.nativeStore();
     	     	showDialog(DIALOG_EOL);
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
 	
 	
 }
