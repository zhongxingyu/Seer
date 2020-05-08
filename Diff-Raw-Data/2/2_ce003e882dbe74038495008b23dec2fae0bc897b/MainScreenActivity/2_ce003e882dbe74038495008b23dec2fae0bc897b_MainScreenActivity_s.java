 package com.pwr.zpi;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashMap;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.PendingIntent;
 import android.content.ComponentName;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.IntentSender;
 import android.content.ServiceConnection;
 import android.content.SharedPreferences;
 import android.location.Location;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.IBinder;
 import android.os.RemoteException;
 import android.preference.PreferenceManager;
 import android.provider.MediaStore;
 import android.provider.Settings;
 import android.speech.tts.TextToSpeech;
 import android.support.v4.app.FragmentActivity;
 import android.util.Log;
 import android.util.Pair;
 import android.view.GestureDetector;
 import android.view.MotionEvent;
 import android.view.View;
 import android.widget.Button;
 import android.widget.ImageButton;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 
 import com.google.android.gms.common.ConnectionResult;
 import com.google.android.gms.common.GooglePlayServicesUtil;
 import com.pwr.zpi.database.Database;
 import com.pwr.zpi.database.entity.SingleRun;
 import com.pwr.zpi.database.entity.TreningPlan;
 import com.pwr.zpi.database.entity.Workout;
 import com.pwr.zpi.dialogs.DialogFactory;
 import com.pwr.zpi.dialogs.DialogsEnum;
 import com.pwr.zpi.dialogs.ErrorDialogFragment;
 import com.pwr.zpi.dialogs.MyDialog;
 import com.pwr.zpi.listeners.GestureListener;
 import com.pwr.zpi.listeners.MyGestureDetector;
 import com.pwr.zpi.mock.TreningPlans;
 import com.pwr.zpi.services.LocationService;
 import com.pwr.zpi.utils.Reminders;
 import com.pwr.zpi.utils.Time;
 import com.pwr.zpi.utils.TimeFormatter;
 import com.pwr.zpi.views.GPSSignalDisplayer;
 
 public class MainScreenActivity extends FragmentActivity implements GestureListener {
 	
 	public static final boolean REDUCED_VERSION = false;
 	public static final String NEW_PLAN_KEY = "new_plan_indicator";
 	
 	private RunListenerApi api;
 	private static final String TAG = MainScreenActivity.class.getSimpleName();
 	
 	//	private TextView GPSSignalTextView;
 	private GPSSignalDisplayer gpsDisplayer;
 	private TextView workoutNameTextView;
 	private ImageButton settingsButton;
 	private ImageButton historyButton;
 	private ImageButton planningButton;
 	private Button startButton;
 	private Button musicButton;
 	private Button treningPlansButton;
 	private RelativeLayout runSummaryRelativeLayout;
 	private TextView runSummaryDistanceTextView;
 	private TextView runSummaryTotalTimeTextView;
 	private TextView runSummaryWorkoutsCountTextView;
 	private TextView GPSSignalTextViewValue;
 	
 	long debugT1;
 	long debugT2;
 	long debugT3;
 	
 	private boolean isServiceConnected;
 	private int gpsStatus = -1;
 	private View mCurrent;
 	private Handler handler;
 	private Workout workout;
 	private GestureDetector gestureDetector;
 	private View.OnTouchListener gestureListener;
 	private TreningPlan treningPlan;
 	private HashMap<Date, Workout> datedWorkouts;
 	private boolean isPlanLoaded;
 	private Workout todayWorkout;
 	
 	public static final short GPS_NOT_ENABLED = 0;
 	public static final short NO_GPS_SIGNAL = 1;
 	public static final short GPS_WORKING = 2;
 	
 	private static final short LEFT = 0;
 	private static final short RIGHT = 1;
 	private static final short UP = 2;
 	private static final short DOWN = 3;
 	
 	public final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
 	public static final int TTS_DATA_CHECK_CODE_REQUEST = 0x1;
 	public final static int REQUEST_GOOGLE_PLAY_SERVICES = 7;
 	public static final int WORKOUT_REQUEST = 0x2;
 	
 	public boolean isConnected;
 	private Location mLastLocation;
 	// service data
 	boolean mIsBound;
 	int runNumber;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		handler = new Handler();
 		setContentView(R.layout.main_screen_activity);
 		
 		init();
 		
 		prepareGestureListener();
 		addListeners();
 		doStartService();
 		doBindService();
 		// locationListener.getmLocationClient().connect();
 		runNumber = 0;
 		isConnected = false;
 		
 	}
 	
 	private void init() {
 		isServiceConnected = false;
 		
 		//		GPSSignalTextView = (TextView) findViewById(R.id.textViewGPSSignal);
 		gpsDisplayer = (GPSSignalDisplayer) findViewById(R.id.gpsDisplayer);
 		settingsButton = (ImageButton) findViewById(R.id.buttonSettings);
 		historyButton = (ImageButton) findViewById(R.id.buttonHistory);
 		planningButton = (ImageButton) findViewById(R.id.buttonPlans);
 		startButton = (Button) findViewById(R.id.buttonStart);
 		musicButton = (Button) findViewById(R.id.buttonMusic);
 		treningPlansButton = (Button) findViewById(R.id.buttonMainScreenTreningPlans);
 		workoutNameTextView = (TextView) findViewById(R.id.textViewMainScreenWorkout);
 		runSummaryDistanceTextView = (TextView) findViewById(R.id.textViewMSDistance);
 		runSummaryTotalTimeTextView = (TextView) findViewById(R.id.textViewMSTotalTime);
 		runSummaryRelativeLayout = (RelativeLayout) findViewById(R.id.relativeLayoutMSRunSummary);
 		runSummaryWorkoutsCountTextView = (TextView) findViewById(R.id.textViewMSWorkoutsCount);
 		GPSSignalTextViewValue = (TextView) findViewById(R.id.textViewGPSIndicator);
 		// locationListener = new MyLocationListener(this);
 		
 		//		PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean(TreningPlans.TRENING_PLANS_IS_ENABLED_KEY, true).commit(); //FIXME delete
 		//		PreferenceManager.getDefaultSharedPreferences(this).edit().putLong(TreningPlans.TRENING_PLANS_ID_KEY, 0).commit(); //FIXME delete
 		//		PreferenceManager.getDefaultSharedPreferences(this).edit().putLong(TreningPlans.TRENING_PLANS_START_DATE_KEY, Calendar.getInstance().getTimeInMillis()).commit(); //FIXME delete
 		
 		validateTreningPlan();
 	}
 	
 	private void validateTreningPlan() {
 		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
 		boolean isTrenignEnabled = prefs.getBoolean(TreningPlans.TRENING_PLANS_IS_ENABLED_KEY, false);
 		
 		if (isTrenignEnabled) {
 			long treningPlanID = prefs.getLong(TreningPlans.TRENING_PLANS_ID_KEY, -1);
 			isPlanLoaded = false;
 			new LoadTreningPlan().execute(treningPlanID);
 		}
 		else {
 			Log.i("T", "NO TRENING ----------------------------------");
 			treningPlansButton.setText(R.string.none);
 			treningPlansButton.setOnClickListener(null);
 		}
 	}
 	
 	private boolean wasWorkoutAlreadyToday() {
 		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
 		long lastWorkoutForThatTrening = prefs.getLong(TreningPlans.TRENING_PLAN_LAST_WORKOUT_DATE, 0);
 		
 		Calendar cal = Calendar.getInstance();
 		Date today = Time.zeroTimeInDate(cal).getTime();
 		cal = Calendar.getInstance();
 		cal.setTimeInMillis(lastWorkoutForThatTrening);
 		cal = Time.zeroTimeInDate(cal);
 		Date lastWorkout = cal.getTime();
 		return today.equals(lastWorkout);
 	}
 	
 	private void checkSpeechSynthezator() {
 		Intent checkIntent = new Intent();
 		checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
 		startActivityForResult(checkIntent, TTS_DATA_CHECK_CODE_REQUEST);
 	}
 	
 	private void addListeners() {
 		settingsButton.setOnTouchListener(gestureListener);
 		historyButton.setOnTouchListener(gestureListener);
 		startButton.setOnTouchListener(gestureListener);
 		planningButton.setOnTouchListener(gestureListener);
 		musicButton.setOnTouchListener(gestureListener);
 		runSummaryRelativeLayout.setOnTouchListener(gestureListener);
 		//		GPSSignalTextView.setOnTouchListener(gestureListener);
 		gpsDisplayer.setOnTouchListener(gestureListener);
 		GPSSignalTextViewValue.setOnTouchListener(gestureListener);
 	}
 	
 	private void prepareGestureListener() {
 		// Gesture detection
 		gestureDetector = new GestureDetector(this, new MyGestureDetector(this, true, true, true, true));
 		gestureListener = new View.OnTouchListener() {
 			@Override
 			public boolean onTouch(View v, MotionEvent event) {
 				mCurrent = v;
 				return gestureDetector.onTouchEvent(event);
 			}
 		};
 	}
 	
 	@Override
 	protected void onNewIntent(Intent intent) {
 		super.onNewIntent(intent);
 		if (intent != null) {
 			if (intent.hasExtra(NEW_PLAN_KEY)) {
 				validateTreningPlan();
 				Log.i(TAG, "new plan ----------------------------");
 			}
 		}
 	}
 	
 	@Override
 	protected void onResume() {
 		super.onResume();
 		
 		new GetAllRunsFromDB().execute(new Void[0]);
 		if (!mIsBound) {
 			doBindService();
 		}
 		else {
 			try {
 				if (isServiceConnected) {
 					gpsStatus = api.getGPSStatus();
 					handleGPSStatusChange();
 				}
 			}
 			catch (RemoteException e) {
 				Log.w(TAG, "Failed to get gpsStatus ", e);
 			}
 		}
 		
 	}
 	
 	@Override
 	public void onWindowFocusChanged(boolean hasFocus) {
 		super.onWindowFocusChanged(hasFocus);
 		try {
 			if (isServiceConnected) {
 				gpsStatus = api.getGPSStatus();
 			}
 			
 		}
 		catch (RemoteException e) {
 			Log.w(TAG, "Failed to get gpsStatus ", e);
 		}
 		handleGPSStatusChange();
 		//	askForGpsStatus();
 	}
 	
 	@Override
 	protected void onPause() {
 		super.onPause();
 		
 		//		askForGpsStatus();
 	}
 	
 	@Override
 	public boolean onTouchEvent(MotionEvent event) {
 		return gestureListener.onTouch(mCurrent, event);
 	}
 	
 	private void startActivity(Class<? extends Activity> activity, short swipeDirection) {
 		Intent i = new Intent(MainScreenActivity.this, activity);
 		if (swipeDirection == RIGHT) { //get workout
 			startActivityForResult(i, WORKOUT_REQUEST);
 		}
 		else {
 			if (swipeDirection == DOWN) {	//start recording
 				if (workout != null) {
 					i.putExtra(Workout.TAG, workout);
 					
 				}
 				i.putExtra(ActivityActivity.RUN_NUMBER_TAG, runNumber);
 			}
 			//debugT3 = System.currentTimeMillis();
 			//Log.i("time", (debugT3 - debugT2) + "");
 			startActivity(i);
 			
 		}
 		switch (swipeDirection) {
 			case RIGHT:
 				overridePendingTransition(R.anim.in_right_anim, R.anim.out_right_anim);
 				break;
 			case LEFT:
 				overridePendingTransition(R.anim.in_left_anim, R.anim.out_left_anim);
 				break;
 			case DOWN:
 				overridePendingTransition(R.anim.in_down_anim, R.anim.out_down_anim);
 				break;
 			case UP:
 				overridePendingTransition(R.anim.in_up_anim, R.anim.out_up_anim);
 				break;
 			default:
 				break;
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
 		startActivityIfPossible(false);
 		
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
 					default:
 						break;
 				}
 				break;
 			case TTS_DATA_CHECK_CODE_REQUEST:
 				if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
 					// success, create the TTS instance
 					try {
 						api.prepareTextToSpeech();
 					}
 					catch (RemoteException e) {
 						e.printStackTrace();
 					}
 				}
 				else {
 					// missing data, install it
 					Intent installIntent = new Intent();
 					installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
 					startActivity(installIntent);
 				}
 				break;
 			case WORKOUT_REQUEST:
 				if (resultCode == RESULT_OK) {
 					workout = data.getParcelableExtra(Workout.TAG);
 					if (workout != null) {
 						workoutNameTextView.setText(workout.getName());
 					}
 				}
 				break;
 			default:
 				break;
 		}
 	}
 	
 	public void showGPSAccuracy() {
 		handler.post(new Runnable() {
 			@Override
 			public void run() {
 				try {
 					//					GPSSignalTextView.setText(String.format("%.2fm", mLastLocation.getAccuracy()));
 					gpsDisplayer.updateStrengthSignal(mLastLocation.getAccuracy());
 				}
 				catch (Throwable t) {
 					Log.e(TAG, "Error while updating the UI with tweets", t);
 				}
 			}
 		});
 	}
 	
 	@Override
 	protected void onDestroy() {
 		
 		try {
 			api.removeListener(runListener);
 			unbindService(serviceConnection);
 			
 		}
 		catch (Throwable t) {
 			// catch any issues, typical for destroy routines
 			// even if we failed to destroy something, we need to continue destroying
 			Log.w(TAG, "Failed to unbind from the service", t);
 		}
 		doStopService();
 		Log.i(TAG, "Activity destroyed");
 		super.onDestroy();
 	}
 	
 	private void startActivityIfPossible(boolean fromTreningPlan) {
 		switch (gpsStatus) {
 			case -1:
 				// Shouldn't be here;
 				Log.e("Service_info", "no gps status info");
 				break;
 			case GPS_NOT_ENABLED:
 				DialogInterface.OnClickListener positiveButtonHandler = new DialogInterface.OnClickListener() {
 					@Override
 					public void onClick(DialogInterface dialog, int id) {
 						Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
 						startActivity(intent);
 					}
 				};
 				
 				MyDialog.showAlertDialog(this, R.string.dialog_message_no_gps, R.string.empty_string,
 					android.R.string.yes, android.R.string.no, positiveButtonHandler, null);
 				
 				break;
 			case NO_GPS_SIGNAL:
 				MyDialog.showAlertDialog(this, R.string.dialog_message_low_gps_accuracy, R.string.empty_string,
 					android.R.string.ok, R.string.empty_string, null, null);
 				break;
 			default:
 				//debugT2 = System.currentTimeMillis();
 				//Log.i("time", (debugT2 - debugT1) + " srodek");
 				if (fromTreningPlan) {
 					PreferenceManager.getDefaultSharedPreferences(this).edit()
 						.putLong(TreningPlans.TRENING_PLAN_LAST_WORKOUT_DATE, Calendar.getInstance().getTimeInMillis());
 					startActivityActivityWithWorkout(todayWorkout);
 				}
 				else {
 					startActivity(ActivityActivity.class, DOWN);
 				}
 				break;
 		
 		}
 		
 	}
 	
 	private void showGoogleServicesDialog(final ConnectionResult connectionResult) {
 		/*
 		 * Google Play services can resolve some errors it detects. If the error
 		 * has a resolution, try sending an Intent to start a Google Play
 		 * services activity that can resolve error.
 		 */
 		handler.post(new Runnable() {
 			
 			@Override
 			public void run() {
 				if (connectionResult.hasResolution()) {
 					try {
 						// Start an Activity that tries to resolve the error
 						connectionResult.startResolutionForResult(MainScreenActivity.this,
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
 					Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(connectionResult.getErrorCode(),
 						MainScreenActivity.this, REQUEST_GOOGLE_PLAY_SERVICES); // tu by�a z�a liczba w
 					// dokumentacji :/
 					
 					// If Google Play services can provide an error dialog
 					if (errorDialog != null) {
 						// Create a new DialogFragment for the error dialog
 						ErrorDialogFragment errorFragment = new ErrorDialogFragment();
 						// Set the dialog in the DialogFragment
 						errorFragment.setDialog(errorDialog);
 						// Show the error dialog in the DialogFragment
 						errorFragment.show(getSupportFragmentManager(), "Location Updates");
 					}
 				}
 				
 			}
 		});
 		
 	}
 	
 	void doStartService() {
 		Log.i("Service_info", "Main Screen --> start service");
 		
 		Intent intent = new Intent(LocationService.class.getName());
 		//intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
 		//intent.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
 		startService(intent);
 		
 	}
 	
 	void doStopService() {
 		Log.i("Service_info", "Main Screen --> stop service");
 		Intent intent = new Intent(LocationService.class.getName());
 		stopService(intent);
 	}
 	
 	void doBindService() {
 		Log.i("Service_info", "Main Screen Binding");
 		Intent intent = new Intent(LocationService.class.getName());
 		//intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
 		//intent.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
 		bindService(intent, serviceConnection, 0);
 		mIsBound = true;
 	}
 	
 	void doUnbindService() {
 		Log.i("Service_info", "Main Screen Unbinding");
 		if (mIsBound) {
 			try {
 				api.removeListener(runListener);
 				unbindService(serviceConnection);
 			}
 			catch (RemoteException e) {
 				e.printStackTrace();
 			}
 			
 			mIsBound = false;
 			
 		}
 	}
 	
 	@Override
 	public void onSingleTapConfirmed(MotionEvent e) {
 		if (mCurrent != null) {
 			switch (mCurrent.getId()) {
 				case R.id.textViewGPSIndicator:
 					// if gps is not running
 					if (gpsStatus == GPS_NOT_ENABLED) {
 						Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
 						startActivity(intent);
 					}
 					break;
 				case R.id.buttonStart:
 					debugT1 = System.currentTimeMillis();
 					//					Debug.startMethodTracing("calc");
 					startActivityIfPossible(false);
 					break;
 				case R.id.buttonHistory:
 					startActivity(HistoryActivity.class, LEFT);
 					break;
 				case R.id.buttonSettings:
 					startActivity(SettingsActivity.class, UP);
 					break;
 				case R.id.buttonPlans:
 					startActivity(PlaningActivity.class, RIGHT);
 					break;
 				case R.id.buttonMusic:
 					startSystemMusicPlayer();
 					break;
 				case R.id.buttonMainScreenTreningPlans:
 					SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
 					boolean isTrenignEnabled = prefs.getBoolean(TreningPlans.TRENING_PLANS_IS_ENABLED_KEY, false);
 					if (isTrenignEnabled) {
 						showTreningPlanDialog();
 					}
 					break;
 				default:
 					break;
 			}
 		}
 	}
 	
 	private void showTreningPlanDialog() {
 		final boolean isWorkoutToday = todayWorkout != null;
 		CharSequence[] items;
 		
 		if (isWorkoutToday && !wasWorkoutAlreadyToday()) {
 			items = getResources().getStringArray(R.array.main_screen_trening_plan_dialog_with_workout);
 		}
 		else {
 			items = getResources().getStringArray(R.array.main_screen_trening_plan_dialog);
 		}
 		DialogInterface.OnClickListener itemsHandler = new DialogInterface.OnClickListener() {
 			
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				int clickedIndex = which;
				if (!isWorkoutToday) {
 					clickedIndex++; //added one to match next switch
 				}
 				switch (clickedIndex) {
 					case 0:
 						startActivityIfPossible(true);
 						break;
 					case 1:
 						if (isPlanLoaded) {
 							Intent i = new Intent(MainScreenActivity.this, PlansActivity.class);
 							i.putExtra(PlansActivity.ID_KEY, treningPlan.getID());
 							Calendar c = Calendar.getInstance();
 							long startTimeInMilis = PreferenceManager.getDefaultSharedPreferences(
 								MainScreenActivity.this).getLong(TreningPlans.TRENING_PLANS_START_DATE_KEY,
 								Calendar.getInstance().getTimeInMillis());
 							c.setTimeInMillis(startTimeInMilis);
 							i.putExtra(PlansActivity.START_DATE_KEY, startTimeInMilis);
 							startActivity(i);
 						}
 						break;
 					case 2:
 						disableTreningPlan();
 						break;
 					default:
 						break;
 				}
 			}
 		};
 		MyDialog.showAlertDialog(this, R.string.trening_plan_todays_workout_title, R.string.empty_string,
 			android.R.string.ok, R.string.empty_string, null, null, items, itemsHandler);
 	}
 	
 	private void disableTreningPlan() {
 		DialogInterface.OnClickListener positiveListener = new DialogInterface.OnClickListener() {
 			
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainScreenActivity.this);
 				prefs.edit().putBoolean(TreningPlans.TRENING_PLANS_IS_ENABLED_KEY, false).commit();
 				Reminders.cancelAllReminders(getApplicationContext());
 				validateTreningPlan();
 				treningPlansButton.setOnTouchListener(null);
 				DialogFactory.getDialogSingleButton(DialogsEnum.ConfirmationDisable, MainScreenActivity.this, null)
 					.show();
 			}
 		};
 		AlertDialog dialog = DialogFactory.getDialog(DialogsEnum.DisableThisTreningPlan, this, positiveListener, null);
 		dialog.show();
 	}
 	
 	private final ServiceConnection serviceConnection = new ServiceConnection() {
 		
 		@Override
 		public void onServiceConnected(ComponentName name, IBinder service) {
 			Log.i(TAG, "Service connection established");
 			isServiceConnected = true;
 			// that's how we get the client side of the IPC connection
 			api = RunListenerApi.Stub.asInterface(service);
 			try {
 				api.addListener(runListener);
 				gpsStatus = api.getGPSStatus();
 				handleGPSStatusChange();
 				getConnectionResult();
 			}
 			catch (RemoteException e) {
 				Log.e(TAG, "Failed to add listener", e);
 			}
 			
 			checkSpeechSynthezator();
 		}
 		
 		@Override
 		public void onServiceDisconnected(ComponentName name) {
 			Log.i(TAG, "Service connection closed");
 		}
 	};
 	
 	private final RunListener.Stub runListener = new RunListener.Stub() {
 		
 		@Override
 		public void handleLocationUpdate() throws RemoteException {
 			Location location = api.getLatestLocation();
 			
 			gpsStatus = api.getGPSStatus();
 			mLastLocation = location;
 			showGPSAccuracy();
 		}
 		
 		@Override
 		public void handleConnectionResult() throws RemoteException {
 			getConnectionResult();
 			
 		}
 		
 		@Override
 		public void handleTimeChange() throws RemoteException {}
 		
 		@Override
 		public void handleWorkoutChange(Workout workout, boolean firstTime) throws RemoteException {}
 		
 		@Override
 		public void handleCountDownChange(int countDownNumber) throws RemoteException {}
 	};
 	
 	private void getConnectionResult() {
 		Intent intent;
 		try {
 			intent = api.getConnectionResult();
 			boolean connectionFailed = intent.getBooleanExtra(LocationService.CONNECTION_FIAILED_TAG, true);
 			if (connectionFailed) {
 				int errorCode = intent.getIntExtra("status_code", -1);
 				PendingIntent pendingIntent = intent.getParcelableExtra("pending_intent");
 				showGoogleServicesDialog(new ConnectionResult(errorCode, pendingIntent));
 			}
 		}
 		catch (RemoteException e) {
 			Log.e(TAG, "Failed to get connectionResult", e);
 		}
 		
 	}
 	
 	private class GetAllRunsFromDB extends AsyncTask<Void, Void, Pair<Pair<Integer, Long>, Pair<Double, Integer>>> {
 		@Override
 		protected Pair<Pair<Integer, Long>, Pair<Double, Integer>> doInBackground(Void... params) {
 			Database db = new Database(MainScreenActivity.this);
 			ArrayList<SingleRun> runs = (ArrayList<SingleRun>) db.getAllRuns();
 			
 			long totalTime = 0;
 			double distance = 0;
 			int count;
 			if (runs != null) {
 				count = runs.size();
 				for (SingleRun run : runs) {
 					totalTime += run.getRunTime();
 					distance += run.getDistance();
 				}
 			}
 			else {
 				count = 0;
 			}
 			Pair<Pair<Integer, Long>, Pair<Double, Integer>> data = new Pair<Pair<Integer, Long>, Pair<Double, Integer>>(
 				new Pair<Integer, Long>(count, totalTime), new Pair<Double, Integer>(distance, count));
 			return data;
 		}
 		
 		@Override
 		protected void onPostExecute(Pair<Pair<Integer, Long>, Pair<Double, Integer>> data) {
 			
 			runSummaryDistanceTextView.setText(String.format("%.3fkm", data.second.first / 1000));
 			runSummaryTotalTimeTextView.setText(TimeFormatter.formatTimeHHMMSS(data.first.second));
 			runSummaryWorkoutsCountTextView.setText(data.first.first + "");
 			runNumber = data.second.second;
 		}
 		
 	}
 	
 	private class LoadTreningPlan extends AsyncTask<Long, Void, HashMap<Date, Workout>> {
 		@Override
 		protected HashMap<Date, Workout> doInBackground(Long... params) {
 			long treningPlanID = params[0];
 			HashMap<Date, Workout> plan = null;
 			//FIXME change to read from db in future version
 			treningPlan = TreningPlans.getTreningPlan(treningPlanID);
 			if (treningPlan == null) { //shouldn't be here
 				treningPlansButton.setText(R.string.none);
 			}
 			else {
 				long startDateInMilis = PreferenceManager.getDefaultSharedPreferences(MainScreenActivity.this).getLong(
 					TreningPlans.TRENING_PLANS_START_DATE_KEY, 0);
 				Calendar cal = Calendar.getInstance();
 				cal.setTimeInMillis(startDateInMilis);
 				Date startDate = cal.getTime();
 				plan = new HashMap<Date, Workout>();
 				for (Integer key : treningPlan.getWorkouts().keySet()) {
 					Workout workout = treningPlan.getWorkouts().get(key);
 					cal.setTime(startDate);
 					cal.add(Calendar.DATE, key);
 					cal = Time.zeroTimeInDate(cal);
 					Date workoutDate = cal.getTime();
 					plan.put(workoutDate, workout);
 				}
 			}
 			
 			return plan;
 		}
 		
 		@Override
 		protected void onPostExecute(HashMap<Date, Workout> result) {
 			treningPlansButton.setText(treningPlan.getName());
 			treningPlansButton.setOnTouchListener(gestureListener);
 			datedWorkouts = result;
 			isPlanLoaded = true;
 			if (!wasWorkoutAlreadyToday()) {
 				checkIsTodayPlannedWorkout();
 			}
 			Log.i(TAG, "plan read");
 		}
 		
 	}
 	
 	private void checkIsTodayPlannedWorkout() {
 		Calendar cal = Calendar.getInstance();
 		cal = Time.zeroTimeInDate(cal);
 		final Workout todayWorkout = datedWorkouts.get(cal.getTime());
 		this.todayWorkout = todayWorkout;
 		
 		if (todayWorkout != null) {
 			DialogInterface.OnClickListener startWorkoutListener = new DialogInterface.OnClickListener() {
 				
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					startActivityIfPossible(true);
 				}
 			};
 			MyDialog.showAlertDialog(this, R.string.trening_plan_todays_workout_title,
 				R.string.trening_plan_todays_workout_message, R.string.trening_plan_todays_workout_positive_button,
 				R.string.trening_plan_todays_workout_negative_button, startWorkoutListener, null);
 		}
 	}
 	
 	private void startActivityActivityWithWorkout(Workout todayWorkout) {
 		PreferenceManager.getDefaultSharedPreferences(this).edit()
 			.putLong(TreningPlans.TRENING_PLAN_LAST_WORKOUT_DATE, Calendar.getInstance().getTimeInMillis()).commit();
 		
 		Intent i = new Intent(MainScreenActivity.this, ActivityActivity.class);
 		i.putExtra(Workout.TAG, todayWorkout);
 		i.putExtra(ActivityActivity.RUN_NUMBER_TAG, runNumber);
 		startActivity(i);
 		overridePendingTransition(R.anim.in_down_anim, R.anim.out_down_anim);
 	}
 	
 	private void handleGPSStatusChange() {
 		handler.post(new Runnable() {
 			@Override
 			public void run() {
 				//				switch (gpsStatus) {
 				//					case GPS_NOT_ENABLED:
 				//						GPSStatusTextView.setText(getResources().getString(
 				//							R.string.gps_disabled));
 				//						break;
 				//					case NO_GPS_SIGNAL:
 				//						GPSStatusTextView.setText(getResources().getString(
 				//							R.string.gps_enabled));
 				//						break;
 				//					case GPS_WORKING:
 				//						GPSStatusTextView.setText(getResources().getString(
 				//							R.string.gps_enabled));
 				//						break;
 				//				}
 			}
 			
 		});
 	}
 }
