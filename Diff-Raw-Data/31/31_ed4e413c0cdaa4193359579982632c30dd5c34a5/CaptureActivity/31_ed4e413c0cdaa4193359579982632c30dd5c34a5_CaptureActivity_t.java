 
 package com.lab117.exchangecam;
 import com.lab117.exchangecam.BeepManager;
 import com.lab117.exchangecam.HelpActivity;
 import com.lab117.exchangecam.OcrResult;
 import com.lab117.exchangecam.PreferencesActivity;
 import com.lab117.exchangecam.R;
 import com.lab117.exchangecam.camera.CameraManager;
 import com.lab117.exchangecam.currency.CurrencyHelper;
 import com.lab117.exchangecam.currency.QueryConversionRateAysncTask;
 
 //import com.lab117.exchangecam.camera.ShutterButton;
 //import com.lab117.exchangecam.language.LanguageCodeHelper;
 //import com.lab117.exchangecam.language.TranslateAsyncTask;
 import com.google.ads.Ad;
 import com.google.ads.AdListener;
 import com.google.ads.AdRequest;
 import com.google.ads.AdView;
 import com.googlecode.tesseract.android.TessBaseAPI;
 
 
 import android.os.StrictMode;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.DialogFragment;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
 import android.content.pm.PackageInfo;
 import android.content.pm.PackageManager;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Color;
 import android.graphics.Point;
 import android.graphics.Rect;
 import android.graphics.Typeface;
 import android.opengl.Visibility;
 import android.os.Bundle;
 import android.os.Environment;
 import android.os.Handler;
 import android.preference.PreferenceActivity;
 import android.preference.PreferenceManager;
 import android.text.ClipboardManager;
 import android.text.SpannableStringBuilder;
 import android.text.style.CharacterStyle;
 import android.text.style.ForegroundColorSpan;
 import android.util.Log;
 import android.util.TypedValue;
 import android.view.ContextMenu;
 import android.view.ContextThemeWrapper;
 import android.view.Display;
 import android.view.Gravity;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.Window;
 import android.view.WindowManager;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.FrameLayout;
 import android.widget.LinearLayout;
 import android.widget.ImageView;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.app.Fragment;
 import android.app.FragmentManager;
 import android.app.FragmentTransaction;
 import android.R.anim;
 import android.R.animator;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Date;
 
 public final class CaptureActivity extends Activity implements SurfaceHolder.Callback, AdListener {
 
 	/*
 	 * UPPER AND LOWER THRESHOLDS FOR WORD CONFIDNCE DISPLAY OF RECOGNIZED WORDS
 	 */
 
	static final int UPPER_WORD_CONFIDENCE_THRESHOLD = 65;
 	static final int LOWER_WORD_CONFIDENCE_THRESHOLD = 5;
 
 	static final long EXCHANGE_RATE_TIMESTAMP = 0;
 	static final long EXCHANGE_RATE_REFRESH_MINS = 20;
 
 
 	private static final String TAG = CaptureActivity.class.getSimpleName();
 
 	// Note: These constants will be overridden by any default values defined in preferences.xml.
 
 	public static final String DEFAULT_SOURCE_CURRENCY = "USD";
 	public static final String DEFAULT_TARGET_CURRENCY = "EUR";
 	public static final String DEFAULT_EXCHANGE_RATE = "0";
 	public static final Boolean DEFAULT_AUTO_EXCHANGE_RATE_PREFERENCE = true;
 
 	/** The default OCR engine to use. */
 	public static final String DEFAULT_OCR_ENGINE_MODE = "Tesseract";
 
 	/** The default page segmentation mode to use. */
 	public static final String DEFAULT_PAGE_SEGMENTATION_MODE = "Auto";
 
 	/** Whether to use autofocus by default. */
 	public static final boolean DEFAULT_TOGGLE_AUTO_FOCUS = true;
 
 	/** Whether to beep by default when the shutter button is pressed. */
 	public static final boolean DEFAULT_TOGGLE_BEEP = true;
 
 	/** Whether to initially show a looping, real-time OCR display. */
 	public static final boolean DEFAULT_TOGGLE_CONTINUOUS = true;
 
 	/** Whether to initially reverse the image returned by the camera. */
 	public static final boolean DEFAULT_TOGGLE_REVERSED_IMAGE = false;
 
 	public static final String DEFAULT_LANGUAGE_CODE = "eng";
 
 	/** Flag to display the real-time recognition results at the top of the scanning screen. */
 	private static final boolean CONTINUOUS_DISPLAY_RECOGNIZED_TEXT = true;
 
 	/** Flag to display recognition-related statistics on the scanning screen. */
 	private static final boolean CONTINUOUS_DISPLAY_METADATA = true;
 
 	/** Resource to use for data file downloads. */
 	static final String DOWNLOAD_BASE = "http://tesseract-ocr.googlecode.com/files/";
 
 	/** Download filename for orientation and script detection (OSD) data. */
 	static final String OSD_FILENAME = "tesseract-ocr-3.01.osd.tar";
 
 	/** Destination filename for orientation and script detection (OSD) data. */
 	static final String OSD_FILENAME_BASE = "osd.traineddata";
 
 	/** Minimum mean confidence score necessary to not reject single-shot OCR result. Currently unused. */
 	static final int MINIMUM_MEAN_CONFIDENCE = 0; // 0 means don't reject any scored results
 
 
 	// Context menu
 	private static final int SETTINGS_ID = Menu.FIRST;
 	private static final int ABOUT_ID = Menu.FIRST + 1;
 
 	private CameraManager cameraManager;
 	private CaptureActivityHandler handler;
 	private ViewfinderView viewfinderView;
 	private SurfaceView surfaceView;
 	private SurfaceHolder surfaceHolder;
 	private View dashboardContainer;
 	private AdView adView;
 	private OcrResult lastResult;
 	private String currentPrice;
 	private boolean hasSurface;
 	private BeepManager beepManager;
 	private TessBaseAPI baseApi; // Java interface for the Tesseract OCR engine
 	final private String sourceLanguageCodeOcr = "eng"; // ISO 639-3 language code
 	final private String sourceLanguageReadable = "English"; // Language name, for example, "English"
 	private int pageSegmentationMode = TessBaseAPI.PSM_AUTO;
 	private int ocrEngineMode = TessBaseAPI.OEM_TESSERACT_ONLY;
 	private String characterBlacklist;
 	private String characterWhitelist;
 	private boolean isContinuousModeActive; // Whether we are doing OCR in continuous mode
 	private SharedPreferences prefs;
 	private ProgressDialog dialog; // for initOcr - language download & unzip
 	private ProgressDialog indeterminateDialog; // also for initOcr - init OCR engine
 	private boolean isEngineReady;
 	private boolean isPaused;
 	private static boolean isFirstLaunch; // True if this is the first time the app is being run
 
 	private String sourceCurrencyCode;
 	private String targetCurrencyCode;
 	private String conversionRate;
 	private Boolean autoExchangeRate;
 	private Boolean dashboardOpen;
 
 
 	Handler getHandler() {
 		return handler;
 	}
 
 	TessBaseAPI getBaseApi() {
 		return baseApi;
 	}
 
 	CameraManager getCameraManager() {
 		return cameraManager;
 	}
 
 	@Override
 	public void onCreate(Bundle icicle) {
 //		//Enable StrictMode for debugging Unintentional network and disk accesses on the main thread.
 //		StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
 //		.detectDiskReads()
 //		.detectDiskWrites()
 //		.detectNetwork()   // or .detectAll() for all detectable problems
 //		.penaltyLog()
 //		.build());
 //		StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
 //		.detectLeakedSqlLiteObjects()
 //		.detectLeakedClosableObjects()
 //		.penaltyLog()
 //		.penaltyDeath()
 //		.build());
 
 		super.onCreate(icicle);
 
 		checkFirstLaunch();
 
 		if (isFirstLaunch) {
 			setDefaultPreferences();
 		}
 
 		Window window = getWindow();
 		window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
 		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
 		setContentView(R.layout.capture);
 		viewfinderView = (ViewfinderView) findViewById(R.id.viewfinder_view);
 		dashboardContainer = (FrameLayout) findViewById(R.id.dashboard_container);
 		adView = (AdView) findViewById(R.id.adView);
 		
 		adView.setAdListener(this);
 
 		//		debugStatusViews = findViewById(R.id.debug_status_views);
 
 		//		statusViewBottom = (TextView) findViewById(R.id.status_view_bottom);
 		//		registerForContextMenu(statusViewBottom);
 		//		statusViewTop = (TextView) findViewById(R.id.status_view_top);
 		//		registerForContextMenu(statusViewTop);
 		//
 		//		statusViewTopRight = (TextView) findViewById(R.id.status_view_top_right);
 		//		registerForContextMenu(statusViewTopRight);
 
 		handler = null;
 		lastResult = null;
 		currentPrice = null;
 		hasSurface = false;
 		beepManager = new BeepManager(this);
 		dashboardOpen = false;
 
 		// Camera shutter button
 		//    if (DISPLAY_SHUTTER_BUTTON) {
 		//      shutterButton = (ShutterButton) findViewById(R.id.shutter_button);
 		//      shutterButton.setOnShutterButtonListener(this);
 		//    }
 		//   
 		//    translationView = (TextView) findViewById(R.id.translation_text_view);
 		//    registerForContextMenu(translationView);
 
 
 		cameraManager = new CameraManager(getApplication());
 		viewfinderView.setCameraManager(cameraManager);
 
 		openDashboardFragment();
 //		adjustAdLocation();
 		// Set listener to change the size of the viewfinder rectangle.
 		viewfinderView.setOnTouchListener(new View.OnTouchListener() {
 			int lastX = -1;
 			int lastY = -1;
 
 			@Override
 			public boolean onTouch(View v, MotionEvent event) {
 				switch (event.getAction()) {
 				case MotionEvent.ACTION_DOWN:
 					lastX = -1;
 					lastY = -1;
 					return true;
 				case MotionEvent.ACTION_MOVE:
 					int currentX = (int) event.getX();
 					int currentY = (int) event.getY();
 
 					try {
 						Rect rect = cameraManager.getFramingRect();
 
 						final int BUFFER = 50;
 						final int BIG_BUFFER = 60;
 						if (lastX >= 0) {
 							// Adjust the size of the viewfinder rectangle. Check if the touch event occurs in the corner areas first, because the regions overlap.
 							if (((currentX >= rect.left - BIG_BUFFER && currentX <= rect.left + BIG_BUFFER) || (lastX >= rect.left - BIG_BUFFER && lastX <= rect.left + BIG_BUFFER))
 									&& ((currentY <= rect.top + BIG_BUFFER && currentY >= rect.top - BIG_BUFFER) || (lastY <= rect.top + BIG_BUFFER && lastY >= rect.top - BIG_BUFFER))) {
 								// Top left corner: adjust both top and left sides
 								cameraManager.adjustFramingRect( 2 * (lastX - currentX), 2 *  (lastY - currentY));
 								viewfinderView.removeResultText();
 							} else if (((currentX >= rect.right - BIG_BUFFER && currentX <= rect.right + BIG_BUFFER) || (lastX >= rect.right - BIG_BUFFER && lastX <= rect.right + BIG_BUFFER)) 
 									&& ((currentY <= rect.top + BIG_BUFFER && currentY >= rect.top - BIG_BUFFER) || (lastY <= rect.top + BIG_BUFFER && lastY >= rect.top - BIG_BUFFER))) {
 								// Top right corner: adjust both top and right sides
 								cameraManager.adjustFramingRect( 2 * (currentX - lastX), 2 * (lastY - currentY));
 								viewfinderView.removeResultText();
 							} else if (((currentX >= rect.left - BIG_BUFFER && currentX <= rect.left + BIG_BUFFER) || (lastX >= rect.left - BIG_BUFFER && lastX <= rect.left + BIG_BUFFER))
 									&& ((currentY <= rect.bottom + BIG_BUFFER && currentY >= rect.bottom - BIG_BUFFER) || (lastY <= rect.bottom + BIG_BUFFER && lastY >= rect.bottom - BIG_BUFFER))) {
 								// Bottom left corner: adjust both bottom and left sides
 								cameraManager.adjustFramingRect(2 * (lastX - currentX), 2 * (currentY - lastY));
 								viewfinderView.removeResultText();
 							} else if (((currentX >= rect.right - BIG_BUFFER && currentX <= rect.right + BIG_BUFFER) || (lastX >= rect.right - BIG_BUFFER && lastX <= rect.right + BIG_BUFFER)) 
 									&& ((currentY <= rect.bottom + BIG_BUFFER && currentY >= rect.bottom - BIG_BUFFER) || (lastY <= rect.bottom + BIG_BUFFER && lastY >= rect.bottom - BIG_BUFFER))) {
 								// Bottom right corner: adjust both bottom and right sides
 								cameraManager.adjustFramingRect(2 * (currentX - lastX), 2 * (currentY - lastY));
 								viewfinderView.removeResultText();
 							} else if (((currentX >= rect.left - BUFFER && currentX <= rect.left + BUFFER) || (lastX >= rect.left - BUFFER && lastX <= rect.left + BUFFER))
 									&& ((currentY <= rect.bottom && currentY >= rect.top) || (lastY <= rect.bottom && lastY >= rect.top))) {
 								// Adjusting left side: event falls within BUFFER pixels of left side, and between top and bottom side limits
 								cameraManager.adjustFramingRect(2 * (lastX - currentX), 0);
 								viewfinderView.removeResultText();
 							} else if (((currentX >= rect.right - BUFFER && currentX <= rect.right + BUFFER) || (lastX >= rect.right - BUFFER && lastX <= rect.right + BUFFER))
 									&& ((currentY <= rect.bottom && currentY >= rect.top) || (lastY <= rect.bottom && lastY >= rect.top))) {
 								// Adjusting right side: event falls within BUFFER pixels of right side, and between top and bottom side limits
 								cameraManager.adjustFramingRect(2 * (currentX - lastX), 0);
 								viewfinderView.removeResultText();
 							} else if (((currentY <= rect.top + BUFFER && currentY >= rect.top - BUFFER) || (lastY <= rect.top + BUFFER && lastY >= rect.top - BUFFER))
 									&& ((currentX <= rect.right && currentX >= rect.left) || (lastX <= rect.right && lastX >= rect.left))) {
 								// Adjusting top side: event falls within BUFFER pixels of top side, and between left and right side limits
 								cameraManager.adjustFramingRect(0, 2 * (lastY - currentY));
 								viewfinderView.removeResultText();
 							} else if (((currentY <= rect.bottom + BUFFER && currentY >= rect.bottom - BUFFER) || (lastY <= rect.bottom + BUFFER && lastY >= rect.bottom - BUFFER))
 									&& ((currentX <= rect.right && currentX >= rect.left) || (lastX <= rect.right && lastX >= rect.left))) {
 								// Adjusting bottom side: event falls within BUFFER pixels of bottom side, and between left and right side limits
 								cameraManager.adjustFramingRect(0, 2 * (currentY - lastY));
 								viewfinderView.removeResultText();
 							}     
 						}
 					} catch (NullPointerException e) {
 						Log.e(TAG, "Framing rect not available", e);
 					}
 
 					v.invalidate();
 					lastX = currentX;
 					lastY = currentY;
 					return true;
 				case MotionEvent.ACTION_UP:
 					lastX = -1;
 					lastY = -1;
 					return true;
 				}
 				return false;
 			}
 		});
 
 		isEngineReady = false;
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();   
 		resetStatusView();
 		Log.d(TAG, "onResume()");
 
 		String previousSourceLanguageCodeOcr = sourceLanguageCodeOcr;
 		int previousOcrEngineMode = ocrEngineMode;
 		openDashboardFragment();
 //		adjustAdLocation();
 
 		retrievePreferences();
 
 		// Set up the camera preview surface.
 		surfaceView = (SurfaceView) findViewById(R.id.preview_view);
 		surfaceHolder = surfaceView.getHolder();
 		if (!hasSurface) {
 			surfaceHolder.addCallback(this);
 			surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
 		}
 
 		// Comment out the following block to test non-OCR functions without an SD card
 
 		// Do OCR engine initialization, if necessary
 		boolean doNewInit = (baseApi == null) || !sourceLanguageCodeOcr.equals(previousSourceLanguageCodeOcr) || 
 				ocrEngineMode != previousOcrEngineMode;
 		if (doNewInit) {      
 			// Initialize the OCR engine
 			File storageDirectory = getStorageDirectory();
 			if (storageDirectory != null) {
 				initOcrEngine(storageDirectory, sourceLanguageCodeOcr, sourceLanguageReadable);
 			}
 		} else {
 			// We already have the engine initialized, so just start the camera.
 			resumeOCR();
 		}
 		//TODO: Check if the conversion rate is out of date.
 		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
 		autoExchangeRate = prefs.getBoolean(PreferencesActivity.KEY_AUTO_EXCHANGE_RATE_PREFERENCE, CaptureActivity.DEFAULT_AUTO_EXCHANGE_RATE_PREFERENCE);
 		if (autoExchangeRate && exchangeRateIsOld()) {
 			initQueryConversionRate();
 		}
 	}
 
 	/** 
 	 * Method to start or restart recognition after the OCR engine has been initialized,
 	 * or after the app regains focus. Sets state related settings and OCR engine parameters,
 	 * and requests camera initialization.
 	 */
 	void resumeOCR() {
 		Log.d(TAG, "resumeOCR()");
 
 		// This method is called when Tesseract has already been successfully initialized, so set 
 		// isEngineReady = true here.
 		isEngineReady = true;
 
 		isPaused = false;
 
 		if (handler != null) {
 			handler.resetState();
 		}
 		if (baseApi != null) {
 			baseApi.setPageSegMode(pageSegmentationMode);
 			baseApi.setVariable(TessBaseAPI.VAR_CHAR_BLACKLIST, characterBlacklist);
 			baseApi.setVariable(TessBaseAPI.VAR_CHAR_WHITELIST, characterWhitelist);
 		}
 
 		if (hasSurface) {
 			// The activity was paused but not stopped, so the surface still exists. Therefore
 			// surfaceCreated() won't be called, so init the camera here.
 			initCamera(surfaceHolder);
 		}
 	}
 
 	/** Called to resume recognition after translation in continuous mode. */
 	@SuppressWarnings("unused")
 	void resumeContinuousDecoding() {
 		isPaused = false;
 		resetStatusView();
 		setStatusViewForContinuous();
 		DecodeHandler.resetDecodeState();
 		handler.resetState();
 		//    if (shutterButton != null && DISPLAY_SHUTTER_BUTTON) {
 		//      shutterButton.setVisibility(View.VISIBLE);
 		//    }
 	}
 
 	@Override
 	public void surfaceCreated(SurfaceHolder holder) {
 		Log.d(TAG, "surfaceCreated()");
 
 		if (holder == null) {
 			Log.e(TAG, "surfaceCreated gave us a null surface");
 		}
 
 		// Only initialize the camera if the OCR engine is ready to go.
 		if (!hasSurface && isEngineReady) {
 			Log.d(TAG, "surfaceCreated(): calling initCamera()...");
 			initCamera(holder);
 		}
 		hasSurface = true;
 	}
 
 	/** Initializes the camera and starts the handler to begin previewing. */
 	private void initCamera(SurfaceHolder surfaceHolder) {
 		Log.d(TAG, "initCamera()");
 		if (surfaceHolder == null) {
 			throw new IllegalStateException("No SurfaceHolder provided");
 		}
 		try {
 
 			// Open and initialize the camera
 			cameraManager.openDriver(surfaceHolder);
 
 			// Creating the handler starts the preview, which can also throw a RuntimeException.
 			handler = new CaptureActivityHandler(this, cameraManager, isContinuousModeActive);
 
 		} catch (IOException ioe) {
 			showErrorMessage("Uh-oh!", "Could not connect to the camera. Please try restarting the device", true);
 		} catch (RuntimeException e) {
 			// Barcode Scanner has seen crashes in the wild of this variety:
 			// java.?lang.?RuntimeException: Fail to connect to camera service
 			showErrorMessage("Uh-oh!", "Could not connect to the camera. Please try restarting the device", true);
 		}   
 	}
 
 	@Override
 	protected void onPause() {
 		Long time = System.currentTimeMillis();
 		Log.d(TAG, "onPause()");
 		if (handler != null) {
 			handler.quitSynchronously();
 		}
 		Log.d(TAG, "onPauseTimer, handler.quitSynchronously(): " + (System.currentTimeMillis()-time));
 
 		time = System.currentTimeMillis();
 
 		// Stop using the camera, to avoid conflicting with other camera-based apps
 		cameraManager.closeDriver();
 		Log.d(TAG, "onPauseTimer, cameraManager.closeDriver(): " + (System.currentTimeMillis()-time));
 
 		time = System.currentTimeMillis();
 
 		if (!hasSurface) {
 			SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
 			SurfaceHolder surfaceHolder = surfaceView.getHolder();
 			surfaceHolder.removeCallback(this);
 		}
 		Log.d(TAG, "onPauseTimer, surfaceHolder.removeCallback(this): " + (System.currentTimeMillis()-time));
 			
 		super.onPause();
 	}
 
 	void stopHandler() {
 		if (handler != null) {
 			handler.stop();
 		}
 	}
 
 	@Override
 	protected void onDestroy() {
 		if (baseApi != null) {
 			baseApi.end();
 		}
 	    if (adView != null) {
 	        // Destroy the AdView.
 	        adView.destroy();
 	      }
 		
 		super.onDestroy();
 
 		FragmentManager fm = getFragmentManager();
 		DashboardFragment dashboardFragment = (DashboardFragment) fm.findFragmentByTag("DASHBOARD_FRAGMENT");
 		Log.d(TAG, "onPause after remove, dashboardFragment == null: " + (dashboardFragment == null));
 		if (dashboardFragment != null) {
 			Log.d(TAG, "removing fragment");
 			FragmentTransaction ft = fm.beginTransaction();
 			ft.remove(dashboardFragment);
 			ft.commit();
 		}
 		Log.d(TAG, "onPause before remove, dashboardFragment == null: " + (dashboardFragment == null));		
 	}
 
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 
 		/*
 		if (keyCode == KeyEvent.KEYCODE_BACK) {
 			FragmentManager fm = getFragmentManager();
 			DashboardFragment dashboardFragment = (DashboardFragment) fm.findFragmentByTag("DASHBOARD_FRAGMENT");
 			if (dashboardFragment != null) {
 				Log.d(TAG, "Remove DashBoardFragment");
 				dashboardContainer.setVisibility(View.GONE);
 				FragmentTransaction ft = fm.beginTransaction();
 				ft.remove(dashboardFragment);
 				ft.commit();
 				getCameraManager().setDashboardOpen(false);
 				getCameraManager().adjustFramingRect(0, 0);
 				return true;
 			}
 		}
 		 */
 		//		else 
 		if (keyCode == KeyEvent.KEYCODE_FOCUS) {      
 			// Only perform autofocus if user is not holding down the button.
 			if (event.getRepeatCount() == 0) {
 				cameraManager.requestAutoFocus(500L);
 			}
 			return true;
 		}
 		/*
 		else if(keyCode == KeyEvent.KEYCODE_MENU) {
 			FragmentManager fm = getFragmentManager();
 			DashboardFragment dashboardFragment = (DashboardFragment) fm.findFragmentByTag("DASHBOARD_FRAGMENT");
 			if (dashboardFragment == null) {
 		 *
 		 * TODO: Definately not the right place to do this, 
 		 * Try and use the CameraConfigurationManager. 
 		 *
 				Display display = getWindowManager().getDefaultDisplay();
 				Point size = new Point();
 				display.getSize(size);
 				int width = size.x;
 				int height = size.y;
 				dashboardContainer.setVisibility(View.VISIBLE);
 
 				dashboardContainer.setLayoutParams(new FrameLayout.LayoutParams(width/3, height, Gravity.RIGHT));
 				FragmentTransaction ft = fm.beginTransaction();
 				dashboardFragment = new DashboardFragment();
 
 				ft.replace(R.id.dashboard_container, dashboardFragment, "DASHBOARD_FRAGMENT");
 				ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
 //				ft.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
 				//ft.addToBackStack(null);
 				ft.commit();
 				getCameraManager().setDashboardOpen(true);
 				getCameraManager().adjustFramingRect(0, 0);
 				return true;
 			}
 			else {
 				Log.d(TAG, "Remove DashBoardFragment");
 				dashboardContainer.setVisibility(View.GONE);
 				FragmentTransaction ft = fm.beginTransaction();
 				ft.remove(dashboardFragment);
 				ft.commit();
 				getCameraManager().setDashboardOpen(false);
 				getCameraManager().adjustFramingRect(0, 0);
 				return true;
 			}
 		}
 		 */
 		return super.onKeyDown(keyCode, event);
 	}
 
 	//	@Override
 	//	public boolean onCreateOptionsMenu(Menu menu) {
 	//		//    MenuInflater inflater = getMenuInflater();
 	//		//    inflater.inflate(R.menu.options_menu, menu);
 	//		super.onCreateOptionsMenu(menu);
 	//		menu.add(0, SETTINGS_ID, 0, "Settings").setIcon(android.R.drawable.ic_menu_preferences);
 	//		menu.add(0, ABOUT_ID, 0, "About").setIcon(android.R.drawable.ic_menu_info_details);
 	//		return true;
 	//	}
 	//
 	//	@Override
 	//	public boolean onOptionsItemSelected(MenuItem item) {
 	//		Intent intent;
 	//		switch (item.getItemId()) {
 	//		case SETTINGS_ID: {
 	//			intent = new Intent().setClass(this, PreferencesActivity.class);
 	//			startActivity(intent);
 	//			break;
 	//		}
 	//		case ABOUT_ID: {
 	//			intent = new Intent(this, HelpActivity.class);
 	//			intent.putExtra(HelpActivity.REQUESTED_PAGE_KEY, HelpActivity.ABOUT_PAGE);
 	//			startActivity(intent);
 	//			break;
 	//		}
 	//		}
 	//		return super.onOptionsItemSelected(item);
 	//	}
 
 	public void surfaceDestroyed(SurfaceHolder holder) {
 		Log.d(TAG, "surfaceDestroyed()");
 		hasSurface = false;
 	}
 
 	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
 	}
 
 	/** Finds the proper location on the SD card where we can save files. */
 	private File getStorageDirectory() {
 		//Log.d(TAG, "getStorageDirectory(): API level is " + Integer.valueOf(android.os.Build.VERSION.SDK_INT));
 
 		String state = null;
 		try {
 			state = Environment.getExternalStorageState();
 		} catch (RuntimeException e) {
 			Log.e(TAG, "Is the SD card visible?", e);
 			showErrorMessage("Error", "Required external storage (such as an SD card) is unavailable.", true);
 		}
 
 		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
 
 			// We can read and write the media
 			//    	if (Integer.valueOf(android.os.Build.VERSION.SDK_INT) > 7) {
 			// For Android 2.2 and above
 
 			try {
 				return getExternalFilesDir(Environment.MEDIA_MOUNTED);
 			} catch (NullPointerException e) {
 				// We get an error here if the SD card is visible, but full
 				Log.e(TAG, "External storage is unavailable");
 				showErrorMessage("Error", "Required external storage (such as an SD card) is full or unavailable.", true);
 			}
 
 			//        } else {
 			//          // For Android 2.1 and below, explicitly give the path as, for example,
 			//          // "/mnt/sdcard/Android/data/com.lab117.exchangecam/files/"
 			//          return new File(Environment.getExternalStorageDirectory().toString() + File.separator + 
 			//                  "Android" + File.separator + "data" + File.separator + getPackageName() + 
 			//                  File.separator + "files" + File.separator);
 			//        }
 
 		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
 			// We can only read the media
 			Log.e(TAG, "External storage is read-only");
 			showErrorMessage("Error", "Required external storage (such as an SD card) is unavailable for data storage.", true);
 		} else {
 			// Something else is wrong. It may be one of many other states, but all we need
 			// to know is we can neither read nor write
 			Log.e(TAG, "External storage is unavailable");
 			showErrorMessage("Error", "Required external storage (such as an SD card) is unavailable or corrupted.", true);
 		}
 		return null;
 	}
 
 	//TODO: Comment
 	private void initQueryConversionRate() {
 
 		// Set up the dialog box 
 		if (dialog != null) {
 			dialog.dismiss();
 		}
 		Context c = new ContextThemeWrapper(this, R.style.indeterminateProgressDialog);
 		dialog = new ProgressDialog(c);
 		autoExchangeRate = prefs.getBoolean(PreferencesActivity.KEY_AUTO_EXCHANGE_RATE_PREFERENCE, CaptureActivity.DEFAULT_AUTO_EXCHANGE_RATE_PREFERENCE);
 
 		Log.d(TAG, "initQueryConversionRate(), autoExchangeRate == " + (autoExchangeRate));
 		if (autoExchangeRate) {
 			Log.d(TAG, "Querying Exchange Rate");
 			new QueryConversionRateAysncTask(this, dialog, sourceCurrencyCode, targetCurrencyCode).execute();
 		}
 	}
 
 
 	/**
 	 * Requests initialization of the OCR engine with the given parameters.
 	 * 
 	 * @param storageRoot Path to location of the tessdata directory to use
 	 * @param languageCode Three-letter ISO 639-3 language code for OCR 
 	 * @param languageName Name of the language for OCR, for example, "English"
 	 */
 	private void initOcrEngine(File storageRoot, String languageCode, String languageName) {    
 		//TODO: Manually override languageCode and languageName.
 		isEngineReady = false;
 
 		// Set up the dialog box for the thermometer-style download progress indicator
 		if (dialog != null) {
 			dialog.dismiss();
 		}
 		dialog = new ProgressDialog(this);
 
 		// Display the name of the OCR engine we're initializing in the indeterminate progress dialog box
 		indeterminateDialog = new ProgressDialog(this);
 		indeterminateDialog.setTitle("Please wait");
 
 		if (handler != null) {
 			handler.quitSynchronously();     
 		}
 
 		// Start AsyncTask to install language data and init OCR
 		baseApi = new TessBaseAPI();
 		new OcrInitAsyncTask(this, baseApi, indeterminateDialog, ocrEngineMode)
 		.execute(storageRoot.toString());
 	}
 
 
 	/**
 	 * Displays information relating to the results of a successful real-time OCR request.
 	 * 
 	 * @param ocrResult Object representing successful OCR results
 	 */
 	void handleOcrContinuousDecode(OcrResult ocrResult) {
 
 		lastResult = ocrResult;
 		String[] words = ocrResult.getText().replace("\n", " ").split(" ");
 		int[] wordConfidences = ocrResult.getWordConfidences();
 
 		//Iterate words in result
 		//get word with highest confidence
 
 		if (wordConfidences.length > 0) {
 			int maxConfidenceIndex = 0;
 			int maxConfidence = wordConfidences[0];
 
 			try {
 
 				for (int i = 0; i < wordConfidences.length; i++) {
 					if (wordConfidences[i] > maxConfidence) {
 						maxConfidence = wordConfidences[i];
 						maxConfidenceIndex = i;
 					}
 				}
 				Log.d(TAG, "maxConfidence: " + maxConfidence);
 
 				if (!words[maxConfidenceIndex].equals("") &&
 						CurrencyHelper.isPrice(this, words[maxConfidenceIndex])) {
 
 					if (currentPrice != null && words[maxConfidenceIndex].equals(currentPrice)) {
 						if (maxConfidence < LOWER_WORD_CONFIDENCE_THRESHOLD) {
 							currentPrice = null;
 							viewfinderView.removeResultText();
 							FragmentManager fm = getFragmentManager();
 							DashboardFragment dashboardFragment = (DashboardFragment) fm.findFragmentByTag("DASHBOARD_FRAGMENT");
 							dashboardFragment.removeResultText();
 						}
 						else {
 							OcrResultText ocrResultText = new OcrResultText(
 									ocrResult.getText(), 
 									ocrResult.getWordConfidences(),
 									ocrResult.getMeanConfidence(),
 									ocrResult.getBitmapDimensions(),
 									ocrResult.getRegionBoundingBoxes(),
 									ocrResult.getTextlineBoundingBoxes(),
 									ocrResult.getStripBoundingBoxes(),
 									ocrResult.getWordBoundingBoxes(),
 									ocrResult.getCharacterBoundingBoxes());
 							dispatchResultToView(ocrResultText, maxConfidenceIndex);
 						}
 					} else { //either currentPrice is null or we have a new price.
 						if (maxConfidence > UPPER_WORD_CONFIDENCE_THRESHOLD) {
 							currentPrice = words[maxConfidenceIndex];
 							OcrResultText ocrResultText = new OcrResultText(
 									ocrResult.getText(), 
 									ocrResult.getWordConfidences(),
 									ocrResult.getMeanConfidence(),
 									ocrResult.getBitmapDimensions(),
 									ocrResult.getRegionBoundingBoxes(),
 									ocrResult.getTextlineBoundingBoxes(),
 									ocrResult.getStripBoundingBoxes(),
 									ocrResult.getWordBoundingBoxes(),
 									ocrResult.getCharacterBoundingBoxes());
 							dispatchResultToView(ocrResultText, maxConfidenceIndex);
 						}
 					}
 				}
 
 			} catch (ArrayIndexOutOfBoundsException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	void dispatchResultToView(OcrResultText ocrResultText, int priceIndex) {
 		FragmentManager fm = getFragmentManager();
 		DashboardFragment dashboardFragment = (DashboardFragment) fm.findFragmentByTag("DASHBOARD_FRAGMENT");
 
 		if (dashboardFragment != null) {
 			dashboardFragment.updateTextViews(ocrResultText, priceIndex);	
 		}
 		viewfinderView.addResultText(ocrResultText, priceIndex);
 	}
 
 
 	/**
 	 * Version of handleOcrContinuousDecode for failed OCR requests. Displays a failure message.
 	 * 
 	 * @param obj Metadata for the failed OCR request.
 	 */
 	void handleOcrContinuousDecode(OcrResultFailure obj) {
 		lastResult = null;
 		viewfinderView.removeResultText();
 		FragmentManager fm = getFragmentManager();
 		DashboardFragment dashboardFragment = (DashboardFragment) fm.findFragmentByTag("DASHBOARD_FRAGMENT");
 
 		if (dashboardFragment != null) {
 			dashboardFragment.removeResultText();	
 		}
 
 
 	}
 
 	/**
 	 * Resets view elements.
 	 */
 	private void resetStatusView() {
 		lastResult = null;
 		viewfinderView.removeResultText();
 
 		FragmentManager fm = getFragmentManager();
 		DashboardFragment dashboardFragment = (DashboardFragment) fm.findFragmentByTag("DASHBOARD_FRAGMENT");
 
 		if (dashboardFragment != null) {
 			dashboardFragment.removeResultText();	
 		}
 
 	}
 
 	/** Displays a pop-up message showing the name of the current OCR source language. */
 	public void showUpdatedExcahngeRate() {   
 		Toast toast = Toast.makeText(this, "Exchange Rate Updated", Toast.LENGTH_LONG);
 		//				+ sourceLanguageReadable, Toast.LENGTH_LONG);
 		toast.setGravity(Gravity.TOP, 0, 0);
 		toast.show();
 	}
 
 	/**
 	 * Displays an initial message to the user while waiting for the first OCR request to be
 	 * completed after starting realtime OCR.
 	 */
 	void setStatusViewForContinuous() {
 		viewfinderView.removeResultText();
 		if (CONTINUOUS_DISPLAY_METADATA) {
 			//			statusViewBottom.setText("OCR: " + sourceLanguageReadable + " - waiting for OCR...");
 		}
 	}
 
 	/** Request the viewfinder to be invalidated. */
 	void drawViewfinder() {
 		viewfinderView.drawViewfinder();
 	}
 
 	static boolean getFirstLaunch() {
 		return isFirstLaunch;
 	}
 
 	/**
 	 * We want the help screen to be shown automatically the first time a new version of the app is
 	 * run. The easiest way to do this is to check android:versionCode from the manifest, and compare
 	 * it to a value stored as a preference.
 	 */
 	private boolean checkFirstLaunch() {
 		try {
 			PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), 0);
 			int currentVersion = info.versionCode;
 			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
 			int lastVersion = prefs.getInt(PreferencesActivity.KEY_HELP_VERSION_SHOWN, 0);
 			if (lastVersion == 0) {
 				isFirstLaunch = true;
 			} else {
 				isFirstLaunch = false;
 			}
 			if (currentVersion > lastVersion) {
 
 				// Record the last version for which we last displayed the What's New (Help) page
 				prefs.edit().putInt(PreferencesActivity.KEY_HELP_VERSION_SHOWN, currentVersion).commit();
 				Intent intent = new Intent(this, HelpActivity.class);
 				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
 
 				//				// Show the default page on a clean install, and the what's new page on an upgrade.
 				//				String page = lastVersion == 0 ? HelpActivity.DEFAULT_PAGE : HelpActivity.WHATS_NEW_PAGE;
 				//				intent.putExtra(HelpActivity.REQUESTED_PAGE_KEY, page);
 				//				startActivity(intent);
 				//				return true;
 			}
 		} catch (PackageManager.NameNotFoundException e) {
 			Log.w(TAG, e);
 		}
 		return false;
 	}
 
 	/**
 	 * Gets values from shared preferences and sets the corresponding data members in this activity.
 	 */
 	private void retrievePreferences() {
 
 		prefs = PreferenceManager.getDefaultSharedPreferences(this);
 
 		// Retrieve from preferences, and set in this Activity, the language preferences
 		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
 
 		sourceCurrencyCode = prefs.getString(PreferencesActivity.KEY_SOURCE_CURRENCY_PREFERENCE, CaptureActivity.DEFAULT_SOURCE_CURRENCY);
 		targetCurrencyCode = prefs.getString(PreferencesActivity.KEY_TARGET_CURRENCY_PREFERENCE, CaptureActivity.DEFAULT_TARGET_CURRENCY);
 		conversionRate = prefs.getString(PreferencesActivity.KEY_EXCHANGE_RATE_PREFERENCE, CaptureActivity.DEFAULT_EXCHANGE_RATE);
 
 		isContinuousModeActive = true;
 		pageSegmentationMode = TessBaseAPI.PSM_AUTO_OSD;
 		ocrEngineMode = TessBaseAPI.OEM_TESSERACT_ONLY;
 		characterBlacklist = OcrCharacterHelper.getBlacklist(prefs, "eng");
 		characterWhitelist = OcrCharacterHelper.getWhitelist(prefs, "eng");
 
 
 		prefs.registerOnSharedPreferenceChangeListener(listener);
 
 		beepManager.updatePrefs();
 	}
 
 	/**
 	 * TODO:Why are the default preferences being set programatically instead of in the xml?
 	 * Sets default values for preferences. To be called the first time this app is run.
 	 */
 	private void setDefaultPreferences() {
 		prefs = PreferenceManager.getDefaultSharedPreferences(this);
 
 		//currency exchange
 		SharedPreferences.Editor editor = prefs.edit();
 		editor.putString(PreferencesActivity.KEY_SOURCE_CURRENCY_PREFERENCE, CaptureActivity.DEFAULT_SOURCE_CURRENCY).commit();
 		editor.putString(PreferencesActivity.KEY_TARGET_CURRENCY_PREFERENCE, CaptureActivity.DEFAULT_TARGET_CURRENCY).commit();
 		editor.putString(PreferencesActivity.KEY_EXCHANGE_RATE_PREFERENCE, CaptureActivity.DEFAULT_EXCHANGE_RATE).commit();
 		editor.putBoolean(PreferencesActivity.KEY_AUTO_EXCHANGE_RATE_PREFERENCE, CaptureActivity.DEFAULT_AUTO_EXCHANGE_RATE_PREFERENCE).commit();
 		//TODO: This commit is unnecessary
 		editor.commit();
 	}
 
 	void displayProgressDialog() {
 		// Set up the indeterminate progress dialog box
 		indeterminateDialog = new ProgressDialog(this);
 		indeterminateDialog.setTitle("Please wait");        
 		String ocrEngineModeName = DEFAULT_OCR_ENGINE_MODE;
 		if (ocrEngineModeName.equals("Both")) {
 			indeterminateDialog.setMessage("Performing OCR using Cube and Tesseract...");
 		} else {
 			indeterminateDialog.setMessage("Performing OCR using " + ocrEngineModeName + "...");
 		}
 		indeterminateDialog.setCancelable(false);
 		indeterminateDialog.show();
 	}
 
 	ProgressDialog getProgressDialog() {
 		return indeterminateDialog;
 	}
 
 	/**
 	 * Displays an error message dialog box to the user on the UI thread.
 	 * 
 	 * @param title The title for the dialog box
 	 * @param message The error message to be displayed
 	 */
 	public void showErrorMessage(String title, String message, Boolean quit) {
 		if(quit) {
 			Context c = new ContextThemeWrapper(this, R.style.indeterminateProgressDialog);
 			new AlertDialog.Builder(c)
 			.setTitle(title)
 			.setMessage(message)
 			.setOnCancelListener(new FinishListener(this))
 			.setPositiveButton( "Done", new FinishListener(this))
 			.show();
 		} else {
 			Context c = new ContextThemeWrapper(this, R.style.exchangeErrorDialog);
 			new AlertDialog.Builder(c)
 			.setTitle(title)
 			.setMessage(message)
 			.show();
 		}
 	}
 
 	public void showExchangeErrorDialog() {
 		DialogFragment dialog = new ExchangeErrorDialogFragment();
 		dialog.show(getFragmentManager(), "ExchangeErrorDialogFragment");
 	}
 
 	public void openDashboardFragment() {
 		//Open Dashboard
 		Log.d(TAG, "openDashboardFragment()");
 		FragmentManager fm = getFragmentManager();
 		DashboardFragment dashboardFragment = (DashboardFragment) fm.findFragmentByTag("DASHBOARD_FRAGMENT");
 		Log.d(TAG, "dashboardFragment == null: " + (dashboardFragment == null));
 		if (dashboardFragment != null) {
 			Log.d(TAG, "removing fragment");
 			FragmentTransaction ft = fm.beginTransaction();
 			ft.remove(dashboardFragment);
 			ft.commit();
 		}
 
 
 		Display display = getWindowManager().getDefaultDisplay();
 		Point size = new Point();
 		display.getSize(size);
 		//			dashboardContainer.setLayoutParams(new FrameLayout.LayoutParams(size.x/3, size.y, Gravity.RIGHT));
 		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(size.x/3, size.y);
 		params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
 		dashboardContainer.setLayoutParams(params);
 
 		ImageView borderline = (ImageView)findViewById(R.id.border_view);
 		params = new RelativeLayout.LayoutParams(40, size.y);
 		params.addRule(RelativeLayout.LEFT_OF, dashboardContainer.getId());
 		borderline.setLayoutParams(params);
 
 
 		//			dashboardContainer.setLayoutParams(new RelativeLayout.LayoutParams(size.x/3, size.y));
 
 		//			borderline.setLayoutParams(new new FrameLayout.LayoutParams())
 
 		FragmentTransaction ft = fm.beginTransaction();
 		dashboardFragment = new DashboardFragment();
 
 		ft.replace(R.id.dashboard_container, dashboardFragment, "DASHBOARD_FRAGMENT");
 		//			ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
 		//			ft.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
 		//			ft.addToBackStack(null);
 		ft.commit();
 		//		getCameraManager().adjustFramingRect(0, 0);
 
 	}
 
 	private boolean exchangeRateIsOld() {
 		//Do not update if rate is fresh.
 		long millis = prefs.getLong(PreferencesActivity.KEY_EXCHANGE_RATE_TIMESTAMP, EXCHANGE_RATE_TIMESTAMP);
 		long diff = (new Date()).getTime() - millis;
 		Date diffDate = new Date(diff);
 		Log.d(TAG, "Freshness: " + diffDate.getMinutes());
 		return (diffDate.getMinutes() > EXCHANGE_RATE_REFRESH_MINS);
 	}
 
 	private OnSharedPreferenceChangeListener listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
 		public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
 			Log.d(TAG, "onSharedPreferenceChanged() " + key);
 			autoExchangeRate = prefs.getBoolean(PreferencesActivity.KEY_AUTO_EXCHANGE_RATE_PREFERENCE, CaptureActivity.DEFAULT_AUTO_EXCHANGE_RATE_PREFERENCE);
 			if (key.equals(PreferencesActivity.KEY_SOURCE_CURRENCY_PREFERENCE)) {
 				sourceCurrencyCode = prefs.getString(PreferencesActivity.KEY_SOURCE_CURRENCY_PREFERENCE, CaptureActivity.DEFAULT_SOURCE_CURRENCY);
 				if (autoExchangeRate) {
 					initQueryConversionRate();
 				}
 			} else if (key.equals(PreferencesActivity.KEY_TARGET_CURRENCY_PREFERENCE)) {
 				targetCurrencyCode = prefs.getString(PreferencesActivity.KEY_TARGET_CURRENCY_PREFERENCE, CaptureActivity.DEFAULT_TARGET_CURRENCY);
 				if (autoExchangeRate) {
 					initQueryConversionRate();
 				}
 			} else if (key.equals(PreferencesActivity.KEY_AUTO_EXCHANGE_RATE_PREFERENCE)) {
 				if (autoExchangeRate) {
 					initQueryConversionRate();
 				}
 			}
 
 		}
 	};
 	
 	private void adjustAdLocation() {
 		if (adView != null) {
 			Display display = getWindowManager().getDefaultDisplay();
 			Point size = new Point();
 			display.getSize(size);
 			FrameLayout.LayoutParams adParams = new FrameLayout.LayoutParams(2*size.x/3, FrameLayout.LayoutParams.WRAP_CONTENT);
 			adParams.gravity = Gravity.BOTTOM;
 			adView.setLayoutParams(adParams);
 			
 			Log.w(TAG, "adjustAdLocation, size.x: " + size.x + ", adView.getWidth():" + adView.getWidth() );
 //			adView.setX(());
 //			adView.setX(size.x/2 - adView.getWidth());
 		}
 	}
 	  /** Called when an ad is clicked and about to return to the application. */
 	  @Override
 	  public void onDismissScreen(Ad ad) {
 	    Log.d(TAG, "onDismissScreen"); 
 //	    Toast.makeText(this, "onDismissScreen", Toast.LENGTH_SHORT).show();
 	  }
 
 	  /** Called when an ad was not received. */
 	  @Override
 	  public void onFailedToReceiveAd(Ad ad, AdRequest.ErrorCode error) {
 	    String message = "onFailedToReceiveAd (" + error + ")";
 	    Log.d(TAG, message);
 //	    Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
 	  }
 
 	  /**
 	   * Called when an ad is clicked and going to start a new Activity that will
 	   * leave the application (e.g. breaking out to the Browser or Maps
 	   * application).
 	   */
 	  @Override
 	  public void onLeaveApplication(Ad ad) {
 	    Log.d(TAG, "onLeaveApplication");
 //	    Toast.makeText(this, "onLeaveApplication", Toast.LENGTH_SHORT).show();
 	  }
 
 	  /**
 	   * Called when an Activity is created in front of the app (e.g. an
 	   * interstitial is shown, or an ad is clicked and launches a new Activity).
 	   */
 	  @Override
 	  public void onPresentScreen(Ad ad) {
 	    Log.d(TAG, "onPresentScreen");
 //	    Toast.makeText(this, "onPresentScreen", Toast.LENGTH_SHORT).show();
 	  }
 
 	  /** Called when an ad is received. */
 	  @Override
 	  public void onReceiveAd(Ad ad) {
 	    Log.w(TAG, "onReceiveAd");
 	    adjustAdLocation();
 //	    Toast.makeText(this, "onReceiveAd", Toast.LENGTH_SHORT).show();
 	  }
 	  
 	  @Override
 	  public void onWindowFocusChanged(boolean hasFocus) {
 	     super.onWindowFocusChanged(hasFocus);
 //	     adjustAdLocation();
 	  }
 	
 }
