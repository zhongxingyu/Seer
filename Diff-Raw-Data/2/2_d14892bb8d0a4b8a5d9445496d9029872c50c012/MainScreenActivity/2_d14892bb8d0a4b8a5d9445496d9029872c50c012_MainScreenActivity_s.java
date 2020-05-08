 package com.pwr.zpi;
 
 import android.app.Activity;
 import android.app.Dialog;
 import android.app.PendingIntent;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.IntentSender;
 import android.location.Location;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.os.Message;
 import android.os.RemoteException;
 import android.provider.MediaStore;
 import android.provider.Settings;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.content.LocalBroadcastManager;
 import android.util.Log;
 import android.view.GestureDetector;
 import android.view.MotionEvent;
 import android.view.View;
 import android.widget.Button;
 import android.widget.TextView;
 
 import com.google.android.gms.common.ConnectionResult;
 import com.google.android.gms.common.GooglePlayServicesUtil;
 import com.pwr.zpi.dialogs.ErrorDialogFragment;
 import com.pwr.zpi.dialogs.MyDialog;
 import com.pwr.zpi.listeners.GestureListener;
 import com.pwr.zpi.listeners.MyGestureDetector;
 import com.pwr.zpi.listeners.MyLocationListener;
 import com.pwr.zpi.services.MyServiceConnection;
 import com.pwr.zpi.views.VerticalTextView;
 
 public class MainScreenActivity extends FragmentActivity implements GestureListener {
 	
 	private TextView GPSStatusTextView;
 	private TextView GPSSignalTextView;
 	private Button settingsButton;
 	private VerticalTextView historyButton;
 	private VerticalTextView planningButton;
 	private Button startButton;
 	private Button musicButton;
 	private LocationManager service;
 	private int gpsStatus = -1;
 	private View mCurrent;
 	// TODO potem zmieni�
 	// public static MyLocationListener locationListener;
 	
 	private GestureDetector gestureDetector;
 	private View.OnTouchListener gestureListener;
 	
 	public static final short GPS_NOT_ENABLED = 0;
 	public static final short NO_GPS_SIGNAL = 1;
 	public static final short GPS_WORKING = 2;
 	
 	private static final short LEFT = 0;
 	private static final short RIGHT = 1;
 	private static final short UP = 2;
 	private static final short DOWN = 3;
 	
 	public final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
 	public final static int REQUEST_GOOGLE_PLAY_SERVICES = 7;
 	
 	public boolean isConnected;
 	private Location mLastLocation;
 	// service data
 	boolean mIsBound;
 	
 	// final Messenger mMessenger = new Messenger(new IncomingHandler());
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main_screen_activity);
 		
 		GPSStatusTextView = (TextView) findViewById(R.id.textViewGPSIndicator);
 		GPSSignalTextView = (TextView) findViewById(R.id.GPSSignalTextView);
 		settingsButton = (Button) findViewById(R.id.buttonSettings);
 		historyButton = (VerticalTextView) findViewById(R.id.buttonHistory);
 		planningButton = (VerticalTextView) findViewById(R.id.buttonPlans);
 		startButton = (Button) findViewById(R.id.buttonStart);
 		musicButton = (Button) findViewById(R.id.buttonMusic);
 		
 		// locationListener = new MyLocationListener(this);
 		
 		prepareGestureListener();
 		addListeners();
 		
 		// locationListener.getmLocationClient().connect();
 		
 		isConnected = false;
 		doStartService();
 		doBindService();
 		
 		LocalBroadcastManager.getInstance(this).registerReceiver(
 			mMyServiceReceiver,
 			new IntentFilter(MyLocationListener.class.getSimpleName()));
 		
 	}
 	
 	private void addListeners() {
 		GPSStatusTextView.setOnTouchListener(gestureListener);
 		settingsButton.setOnTouchListener(gestureListener);
 		historyButton.setOnTouchListener(gestureListener);
 		startButton.setOnTouchListener(gestureListener);
 		planningButton.setOnTouchListener(gestureListener);
 		musicButton.setOnTouchListener(gestureListener);
 	}
 	
 	private void prepareGestureListener() {
 		// Gesture detection
 		gestureDetector = new GestureDetector(this, new MyGestureDetector(this,
 			true, true, true, true));
 		gestureListener = new View.OnTouchListener() {
 			@Override
 			public boolean onTouch(View v, MotionEvent event) {
 				mCurrent = v;
 				return gestureDetector.onTouchEvent(event);
 			}
 		};
 	}
 	
 	@Override
 	protected void onResume() {
 		super.onResume();
 		
 		askForGpsStatus();
 	}
 	
 	@Override
 	public void onWindowFocusChanged(boolean hasFocus) {
 		super.onWindowFocusChanged(hasFocus);
 		askForGpsStatus();
 	}
 	
 	@Override
 	protected void onPause() {
 		super.onPause();
 		
 		askForGpsStatus();
 	}
 	
 	@Override
 	public boolean onTouchEvent(MotionEvent event) {
		return gestureListener.onTouch(null, event);
 	}
 	
 	private void startActivity(Class<? extends Activity> activity,
 		short swipeDirection) {
 		Intent i = new Intent(MainScreenActivity.this, activity);
 		
 		startActivity(i);
 		if (swipeDirection == RIGHT) {
 			overridePendingTransition(R.anim.in_right_anim,
 				R.anim.out_right_anim);
 		}
 		else if (swipeDirection == LEFT) {
 			overridePendingTransition(R.anim.in_left_anim, R.anim.out_left_anim);
 		}
 		else if (swipeDirection == DOWN) {
 			overridePendingTransition(R.anim.in_down_anim, R.anim.out_down_anim);
 		}
 		else if (swipeDirection == UP) {
 			overridePendingTransition(R.anim.in_up_anim, R.anim.out_up_anim);
 		}
 	}
 	
 	@Override
 	public void onLeftToRightSwipe() {
 		startActivity(PlaningActivity.class, RIGHT);
 		
 	}
 	
 	@Override
 	public void onRightToLeftSwipe() {
 		startActivity(HistoryActivity.class, LEFT);
 		
 	}
 	
 	@Override
 	public void onUpToDownSwipe() {
 		startActivityIfPossible();
 		
 	}
 	
 	@Override
 	public void onDownToUpSwipe() {
 		startActivity(SettingsActivity.class, UP);
 		
 	}
 	
 	private void startSystemMusicPlayer() {
 		Intent i;
 		i = new Intent(MediaStore.INTENT_ACTION_MUSIC_PLAYER);
 		startActivity(i);
 	}
 	
 	// TODO wywolac
 	private boolean servicesConnected() {
 		// Check that Google Play services is available
 		int resultCode = GooglePlayServicesUtil
 			.isGooglePlayServicesAvailable(this);
 		// If Google Play services is available
 		if (ConnectionResult.SUCCESS == resultCode) {
 			// In debug mode, log the status
 			Log.d("Location Updates", "Google Play services is available.");
 			// Continue
 			return true;
 			// Google Play services was not available for some reason
 		}
 		else {
 			// Get the error dialog from Google Play services
 			Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(
 				resultCode, this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
 			
 			// If Google Play services can provide an error dialog
 			if (errorDialog != null) {
 				// Create a new DialogFragment for the error dialog
 				ErrorDialogFragment errorFragment = new ErrorDialogFragment();
 				// Set the dialog in the DialogFragment
 				errorFragment.setDialog(errorDialog);
 				// Show the error dialog in the DialogFragment
 				errorFragment.show(getSupportFragmentManager(),
 					"Location Updates");
 			}
 			return false;
 		}
 	}
 	
 	/*
 	 * Handle results returned to the FragmentActivity by Google Play services
 	 */
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		// Decide what to do based on the original request code
 		switch (requestCode) {
 			case CONNECTION_FAILURE_RESOLUTION_REQUEST:
 				/*
 				 * If the result code is Activity.RESULT_OK, try to connect again
 				 */
 				switch (resultCode) {
 					case Activity.RESULT_OK:
 						/*
 						 * Try the request again
 						 */
 						break;
 				}
 		}
 	}
 	
 	public void showGPSAccuracy(double accuracy) {
 		GPSSignalTextView.setText(String.format("%s: %.2fm", getResources()
 			.getString(R.string.gps_accuracy), accuracy));
 	}
 	
 	@Override
 	protected void onStop() {
 		// LocalBroadcastManager.getInstance(this).unregisterReceiver(mMyServiceReceiver);
 		super.onStop();
 	}
 	
 	@Override
 	protected void onStart() {
 		
 		super.onStart();
 	}
 	
 	@Override
 	protected void onDestroy() {
 		doUnbindService();
 		doStopService();
 		LocalBroadcastManager.getInstance(this).unregisterReceiver(
 			mMyServiceReceiver);
 		super.onDestroy();
 	}
 	
 	private void startActivityIfPossible() {
 		if (gpsStatus == -1) {
 			// Shouldn't be here;
 			Log.e("Service_info", "no gps status info");
 		}
 		else if (gpsStatus == GPS_NOT_ENABLED) {
 			MyDialog dialog = new MyDialog();
 			DialogInterface.OnClickListener positiveButtonHandler = new DialogInterface.OnClickListener() {
 				@Override
 				public void onClick(DialogInterface dialog, int id) {
 					Intent intent = new Intent(
 						android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
 					startActivity(intent);
 				}
 			};
 			
 			dialog.showAlertDialog(this, R.string.dialog_message_no_gps,
 				R.string.empty_string, android.R.string.yes,
 				android.R.string.no, positiveButtonHandler, null);
 			
 		}
 		else if (gpsStatus == NO_GPS_SIGNAL) {
 			MyDialog dialog = new MyDialog();
 			DialogInterface.OnClickListener positiveButtonHandler = new DialogInterface.OnClickListener() {
 				@Override
 				public void onClick(DialogInterface dialog, int id) {
 					
 				}
 			};
 			dialog.showAlertDialog(this,
 				R.string.dialog_message_low_gps_accuracy,
 				R.string.empty_string, android.R.string.ok,
 				R.string.empty_string, positiveButtonHandler, null);
 			
 		}
 		else {
 			startActivity(ActivityActivity.class, DOWN);
 		}
 	}
 	
 	private void showGoogleServicesDialog(ConnectionResult connectionResult) {
 		/*
 		 * Google Play services can resolve some errors it detects. If the error
 		 * has a resolution, try sending an Intent to start a Google Play
 		 * services activity that can resolve error.
 		 */
 		
 		if (connectionResult.hasResolution()) {
 			try {
 				// Start an Activity that tries to resolve the error
 				connectionResult.startResolutionForResult(this,
 					CONNECTION_FAILURE_RESOLUTION_REQUEST);
 				/*
 				 * Thrown if Google Play services canceled the original
 				 * PendingIntent
 				 */
 			}
 			catch (IntentSender.SendIntentException e) {
 				// Log the error
 				e.printStackTrace();
 			}
 		}
 		else {
 			/*
 			 * If no resolution is available, display a dialog to the user with
 			 * the error.
 			 */
 			// Get the error dialog from Google Play services
 			Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(
 				connectionResult.getErrorCode(), this,
 				REQUEST_GOOGLE_PLAY_SERVICES); // tu by�a z�a liczba w
 												// dokumentacji :/
 			
 			// If Google Play services can provide an error dialog
 			if (errorDialog != null) {
 				// Create a new DialogFragment for the error dialog
 				ErrorDialogFragment errorFragment = new ErrorDialogFragment();
 				// Set the dialog in the DialogFragment
 				errorFragment.setDialog(errorDialog);
 				// Show the error dialog in the DialogFragment
 				errorFragment.show(getSupportFragmentManager(),
 					"Location Updates");
 			}
 		}
 		
 	}
 	
 	// SERVICE METHODS
 	private void askForGpsStatus() {
 		try {
 			Message msg = Message.obtain(null,
 				MyLocationListener.MSG_ASK_FOR_GPS);
 			if (mConnection.getmService() != null) {
 				mConnection.getmService().send(msg);
 			}
 		}
 		catch (RemoteException e) {
 			
 		}
 		
 	}
 	
 	private final MyServiceConnection mConnection = new MyServiceConnection();
 	
 	void doStartService() {
 		Log.i("Service_info", "Main Screen --> start service");
 		Intent intent = new Intent(MainScreenActivity.this,
 			MyLocationListener.class);
 		intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
 		intent.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
 		startService(intent);
 	}
 	
 	void doStopService() {
 		Log.i("Service_info", "Main Screen --> stop service");
 		Intent intent = new Intent(MainScreenActivity.this,
 			MyLocationListener.class);
 		stopService(intent);
 	}
 	
 	void doBindService() {
 		Log.i("Service_info", "Main Screen Binding");
 		Intent i = new Intent(MainScreenActivity.this, MyLocationListener.class);
 		i.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
 		i.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
 		bindService(i, mConnection, Context.BIND_AUTO_CREATE);
 		mIsBound = true;
 	}
 	
 	void doUnbindService() {
 		Log.i("Service_info", "Main Screen Unbinding");
 		if (mIsBound) {
 			// Detach our existing connection.
 			unbindService(mConnection);
 			mIsBound = false;
 			
 		}
 	}
 	
 	// handler for the events launched by the service
 	private final BroadcastReceiver mMyServiceReceiver = new BroadcastReceiver() {
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			Log.i("Service_info", "onReceive");
 			
 			// Extract data included in the Intent
 			int message_type = intent.getIntExtra(MyLocationListener.MESSAGE,
 				-1);
 			
 			switch (message_type) {
 				case MyLocationListener.MSG_SEND_LOCATION:
 					Log.i("Service_info", "got Location");
 					isConnected = true;
 					mLastLocation = (Location) intent
 						.getParcelableExtra("Location");
 					showGPSAccuracy(mLastLocation.getAccuracy());
 					askForGpsStatus();
 					
 					break;
 				case MyLocationListener.MSG_ASK_FOR_GPS:
 					gpsStatus = intent.getIntExtra("gpsStatus", GPS_NOT_ENABLED);
 					Log.i("Service_info", "gotGpsStatus: " + gpsStatus);
 					switch (gpsStatus) {
 						case GPS_NOT_ENABLED:
 							GPSStatusTextView.setText(getResources().getString(
 								R.string.gps_disabled));
 							break;
 						case NO_GPS_SIGNAL:
 							GPSStatusTextView.setText(getResources().getString(
 								R.string.gps_enabled));
 							break;
 						case GPS_WORKING:
 							GPSStatusTextView.setText(getResources().getString(
 								R.string.gps_enabled));
 							break;
 					}
 					break;
 				case MyLocationListener.MSG_SHOW_GOOGLE_SERVICES_DIALOG:
 					Log.i("Service_info", "show Google services dialog");
 					int errorCode = intent.getIntExtra("status_code", -1);
 					PendingIntent pendingIntent = intent
 						.getParcelableExtra("pending_intent");
 					showGoogleServicesDialog(new ConnectionResult(errorCode,
 						pendingIntent));
 					break;
 			}
 		}
 	};
 	
 	@Override
 	public void onSingleTapConfirmed(MotionEvent e) {
 		View v = mCurrent;
 		if (v == GPSStatusTextView) {
 			// if gps is not running
 			if (gpsStatus == GPS_NOT_ENABLED) {
 				Intent intent = new Intent(
 					Settings.ACTION_LOCATION_SOURCE_SETTINGS);
 				startActivity(intent);
 			}
 		}
 		else if (v == startButton) {
 			startActivityIfPossible();
 			
 		}
 		else if (v == historyButton) {
 			startActivity(HistoryActivity.class, LEFT);
 			
 		}
 		else if (v == settingsButton) {
 			startActivity(SettingsActivity.class, UP);
 			
 		}
 		else if (v == planningButton) {
 			startActivity(PlaningActivity.class, RIGHT);
 		}
 		else if (v == musicButton) {
 			startSystemMusicPlayer();
 		}
 	}
 	
 }
