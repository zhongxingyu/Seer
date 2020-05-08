 package com.pwr.zpi;
 
 import java.util.Calendar;
 import java.util.LinkedList;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.ServiceConnection;
 import android.graphics.Color;
 import android.location.Location;
 import android.os.Bundle;
 import android.os.Handler;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.content.LocalBroadcastManager;
 import android.util.Log;
 import android.util.TypedValue;
 import android.view.KeyEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.LinearLayout;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 import com.google.android.gms.maps.CameraUpdateFactory;
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.SupportMapFragment;
 import com.google.android.gms.maps.model.CameraPosition;
 import com.google.android.gms.maps.model.LatLng;
 import com.google.android.gms.maps.model.Marker;
 import com.google.android.gms.maps.model.MarkerOptions;
 import com.google.android.gms.maps.model.Polyline;
 import com.google.android.gms.maps.model.PolylineOptions;
 import com.pwr.zpi.database.Database;
 import com.pwr.zpi.database.entity.SingleRun;
 import com.pwr.zpi.listeners.MyLocationListener;
 import com.pwr.zpi.services.MyServiceConnection;
 import com.pwr.zpi.utils.BeepPlayer;
 import com.pwr.zpi.utils.GeographicalEvaluations;
 import com.pwr.zpi.utils.Pair;
 
 public class ActivityActivity extends FragmentActivity implements
 		OnClickListener {
 
 	private GoogleMap mMap;
 
 	private Button stopButton;
 	private Button pauseButton;
 	private Button resumeButton;
 	private TextView DataTextView1;
 	private TextView DataTextView2;
 	private TextView clickedContentTextView;
 	private TextView LabelTextView1;
 	private TextView LabelTextView2;
 	private TextView clickedLabelTextView;
 	private TextView unitTextView1;
 	private TextView unitTextView2;
 	private TextView clickedUnitTextView;
 	private TextView GPSAccuracy;
 	private TextView countDownTextView;
 	private LinearLayout startStopLayout;
 	private RelativeLayout dataRelativeLayout1;
 	private RelativeLayout dataRelativeLayout2;
 	private Location mLastLocation;
 	private boolean isPaused;
 	private SingleRun singleRun;
 	private LinkedList<LinkedList<Pair<Location,Long>>> traceWithTime;
 	private Calendar calendar;
 	private PolylineOptions traceOnMap;
 	private Polyline traceOnMapObject;
 	private static final float traceThickness = 5;
 	private static final int traceColor = Color.RED;
 
 	// measured values
 	double pace;
 	double avgPace;
 	double distance;
 	Long time = 0L;
 	long startTime;
 	long pauseTime;
 	long pauseStartTime;
 
 	private int dataTextView1Content;
 	private int dataTextView2Content;
 	private int clickedField;
 	// measured values IDs
 	private static final int distanceID = 0;
 	private static final int paceID = 1;
 	private static final int avgPaceID = 2;
 	private static final int timeID = 3;
 
 	// service data
 	boolean mIsBound;
 
 	// time counting fields
 	private Handler handler;
 	private Runnable timeHandler;
 	private static final int COUNT_DOWN_TIME = 5;
 	private static final String TAG = ActivityActivity.class.getSimpleName();
 	BeepPlayer beepPlayer;
 	
 	// progress dialog lost gps
 	private ProgressDialog lostGPSDialog;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_view);
 
 		initFields();
 		addListeners();
 
 		initDisplayedData();
 		
 		prepareServiceAndStart();
 
 		startTimerAfterCountDown();
 	}
 
 	private void initFields() {
 		stopButton = (Button) findViewById(R.id.stopButton);
 		pauseButton = (Button) findViewById(R.id.pauseButton);
 		resumeButton = (Button) findViewById(R.id.resumeButton);
 		dataRelativeLayout1 = (RelativeLayout) findViewById(R.id.dataRelativeLayout1);
 		dataRelativeLayout2 = (RelativeLayout) findViewById(R.id.dataRelativeLayout2);
 		GPSAccuracy = (TextView) findViewById(R.id.TextViewGPSAccuracy);
 		countDownTextView = (TextView) findViewById(R.id.textViewCountDown);
 		startStopLayout = (LinearLayout) findViewById(R.id.startStopLinearLayout);
 		SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
 				.findFragmentById(R.id.map);
 		mMap = mapFragment.getMap();
 
 
 		traceWithTime = new LinkedList<LinkedList<Pair<Location,Long>>>();
 		pauseTime = 0;
 		traceOnMap = new PolylineOptions();
 		traceOnMap.width(traceThickness);
 		traceOnMap.color(traceColor);
 		traceOnMapObject = mMap.addPolyline(traceOnMap);
 		
 		DataTextView1 = (TextView) findViewById(R.id.dataTextView1);
 		DataTextView2 = (TextView) findViewById(R.id.dataTextView2);
 
 		LabelTextView1 = (TextView) findViewById(R.id.dataTextView1Discription);
 		LabelTextView2 = (TextView) findViewById(R.id.dataTextView2Discription);
 
 		unitTextView1 = (TextView) findViewById(R.id.dataTextView1Unit);
 		unitTextView2 = (TextView) findViewById(R.id.dataTextView2Unit);
 
 		// to change displayed info, change dataTextViewContent and start
 		// initLabelsMethod
 		dataTextView1Content = distanceID;
 		dataTextView2Content = timeID;
 		
 		//make single run object
 		singleRun = new SingleRun();
 		calendar = Calendar.getInstance(); 
 		
 		singleRun.setStartDate(calendar.getTime());
 		isPaused = false;
 		
 		beepPlayer = new BeepPlayer(this);
 		
 		moveSystemControls(mapFragment);
 	}
 	
 	private void addListeners() {
 		stopButton.setOnClickListener(this);
 		resumeButton.setOnClickListener(this);
 		pauseButton.setOnClickListener(this);
 		dataRelativeLayout1.setOnClickListener(this);
 		dataRelativeLayout2.setOnClickListener(this);
 	}
 	
 	private void initDisplayedData() {
 		GPSAccuracy.setText(getMyString(R.string.gps_accuracy) + " ?");
 
 		initLabels(DataTextView1, LabelTextView1, dataTextView1Content);
 		initLabels(DataTextView2, LabelTextView2, dataTextView2Content);
 	}
 	
 	private void prepareServiceAndStart() {
 		doBindService();
 		LocalBroadcastManager.getInstance(this).registerReceiver(
 				mMyServiceReceiver,
 				new IntentFilter(MyLocationListener.class.getSimpleName()));
 	}
 
 	@Override
 	protected void onDestroy() {
 		LocalBroadcastManager.getInstance(this).unregisterReceiver(
 				mMyServiceReceiver);
 		doUnbindService();
 		super.onDestroy();
 	}
 
 	// start of timer methods
 	private void startTimerAfterCountDown() {
 		handler = new Handler();
 		prepareTimeCountingHandler();
 		handler.post(new CounterRunnable(COUNT_DOWN_TIME));
 	}
 
 	private class CounterRunnable implements Runnable {
 
 		final int x;
 
 		public CounterRunnable(int x) {
 			this.x = x;
 		}
 
 		@Override
 		public void run() {
 			runOnUiThread(new Runnable() {
 
 				@Override
 				public void run() {
 					if (x == 0) {
 						countDownTextView.setVisibility(View.GONE);
 						startTime = System.currentTimeMillis();
 						handler.post(timeHandler);
 					} else {
 						countDownTextView.setText(x + "");
 						beepPlayer.playBeep();
 						handler.postDelayed(new CounterRunnable(x - 1), 1000);
 					}
 				}
 			});
 		}
 	}
 
 	private void prepareTimeCountingHandler() {
 		timeHandler = new Runnable() {
 
 			@Override
 			public void run() {
 				runTimerTask();
 			}
 		};
 	}
 
 	protected void runTimerTask() {
 
 		synchronized (time) {
 			time = System.currentTimeMillis() - startTime - pauseTime;
 
 			runOnUiThread(new Runnable() {
 				@Override
 				public void run() {
 					updateData(DataTextView1, dataTextView1Content);
 					updateData(DataTextView2, dataTextView2Content);
 				}
 			});
 		}
 		handler.postDelayed(timeHandler, 1000);
 	}
 
 	// end of timer methods
 
 	private void moveSystemControls(SupportMapFragment mapFragment) {
 
 		View zoomControls = mapFragment.getView().findViewById(0x1);
 
 		if (zoomControls != null
 				&& zoomControls.getLayoutParams() instanceof RelativeLayout.LayoutParams) {
 			// ZoomControl is inside of RelativeLayout
 			RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) zoomControls
 					.getLayoutParams();
 
 			// Align it to - parent top|left
 			params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
 			params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
 
 			// nie do ko�ca rozumiem t� metod�, trzeba zobaczy� czy u Ciebie
 			// jest to samo czy nie za bardzo
 			final int margin = (int) TypedValue.applyDimension(
 					TypedValue.COMPLEX_UNIT_DIP,
 					getResources().getDimension(R.dimen.zoom_buttons_margin),
 					getResources().getDisplayMetrics());
 			params.setMargins(0, 0, 0, margin);
 		}
 		View locationControls = mapFragment.getView().findViewById(0x2);
 
 		if (locationControls != null
 				&& locationControls.getLayoutParams() instanceof RelativeLayout.LayoutParams) {
 			// ZoomControl is inside of RelativeLayout
 			RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) locationControls
 					.getLayoutParams();
 
 			// Align it to - parent top|left
 			params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
 
 			// Update margins, set to 10dp
 			final int margin1 = (int) TypedValue.applyDimension(
 					TypedValue.COMPLEX_UNIT_DIP,
 					getResources().getDimension(
 							R.dimen.location_button_margin_top), getResources()
 							.getDisplayMetrics());
 			final int margin2 = (int) TypedValue.applyDimension(
 					TypedValue.COMPLEX_UNIT_DIP,
 					getResources().getDimension(
 							R.dimen.location_button_margin_right),
 					getResources().getDisplayMetrics());
 			params.setMargins(0, margin1, margin2, 0);
 		}
 	}
 
 	private void initLabels(TextView textViewInitialValue, TextView textView,
 			int meassuredValue) {
 		switch (meassuredValue) {
 		case distanceID:
 			textView.setText(R.string.distance);
 			textViewInitialValue.setText("0.000");
 			break;
 		case paceID:
 			textView.setText(R.string.pace);
 			textViewInitialValue.setText("0:00");
 			break;
 		case avgPaceID:
 			textView.setText(R.string.pace_avrage);
 			textViewInitialValue.setText("0:00");
 			break;
 		case timeID:
 			textView.setText(R.string.time);
 			textViewInitialValue.setText("00:00:00");
 			break;
 		}
 
 	}
 
 	private void updateLabels(int meassuredValue, TextView labelTextView,
 			TextView unitTextView, TextView contentTextView) {
 		switch (meassuredValue) {
 		case distanceID:
 			labelTextView.setText(R.string.distance);
 			unitTextView.setText(R.string.km);
 			break;
 		case paceID:
 			labelTextView.setText(R.string.pace);
 			unitTextView.setText(R.string.minutes_per_km);
 			break;
 		case avgPaceID:
 			labelTextView.setText(R.string.pace_avrage);
 			unitTextView.setText(R.string.minutes_per_km);
 			break;
 		case timeID:
 			labelTextView.setText(R.string.time);
 			unitTextView.setText("");
 			break;
 		}
 
 		updateData(contentTextView, meassuredValue);
 	}
 
 	@Override
 	public void onBackPressed() {
 		super.onBackPressed();
 		showAlertDialog();
 	}
 	
 	//invoke when finishing activity
 	private void saveRun()
 	{
 		//add last values 
 		singleRun.setEndDate(calendar.getTime());
 		singleRun.setRunTime(time);
 		singleRun.setDistance(distance);
 		singleRun.setTraceWithTime(traceWithTime);
 		
 		//store in DB
 		Database db = new Database(this);
 		db.insertSingleRun(singleRun);
 	}
 	private void showAlertDialog() {
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		// Add the buttons
 		builder.setTitle(R.string.dialog_message_on_stop);
 		builder.setPositiveButton(android.R.string.yes,
 				new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int id) {
 						saveRun();
 						finish();
 						overridePendingTransition(R.anim.in_up_anim,
 								R.anim.out_up_anim);
 					}
 				});
 		builder.setNegativeButton(android.R.string.no, null);
 		// Set other dialog properties
 
 		// Create the AlertDialog
 		AlertDialog dialog = builder.create();
 		dialog.show();
 
 	}
 
 	private void showLostGpsSignalDialog() {
 		lostGPSDialog = ProgressDialog.show(this, getResources().getString(R.string.dialog_message_on_lost_gpsp), null); //TODO strings
 		lostGPSDialog.setCancelable(true);
 	}
 
 	@Override
 	public void onClick(View v) {
 		if (v == stopButton) {
 			// TODO finish and save activity
 			showAlertDialog();
 		} else if (v == pauseButton) { // stop time
 			isPaused = true;
 			startStopLayout.setVisibility(View.INVISIBLE);
 			resumeButton.setVisibility(View.VISIBLE);
 			pauseStartTime = System.currentTimeMillis();
 
 			handler.removeCallbacks(timeHandler);
 		} else if (v == resumeButton) { // start time
 			isPaused = false;
 			startStopLayout.setVisibility(View.VISIBLE);
 			resumeButton.setVisibility(View.GONE);
 			pauseTime += System.currentTimeMillis() - pauseStartTime;
 			traceWithTime.add(new LinkedList<Pair<Location,Long>>());
 			handler.post(timeHandler);
 		} else if (v == dataRelativeLayout1) {
 			clickedContentTextView = DataTextView1;
 			clickedLabelTextView = LabelTextView1;
 			clickedUnitTextView = unitTextView1;
 			clickedField = 1;
 			showMeassuredValuesMenu();
 		} else if (v == dataRelativeLayout2) {
 			clickedContentTextView = DataTextView2;
 			clickedLabelTextView = LabelTextView2;
 			clickedUnitTextView = unitTextView2;
 			clickedField = 2;
 			showMeassuredValuesMenu();
 		}
 
 	}
 
 	private String getMyString(int stringId) {
 		return getResources().getString(stringId);
 	}
 
 	private void showMeassuredValuesMenu() {
 		// chcia�em zrobi� tablice w stringach, ale potem zobaczy�em, �e ju� mam
 		// te wszystkie nazwy i teraz nie wiem czy tamto zmienia� w tablic� czy
 		// nie ma sensu
 		// kolejno�� w tablicy musi odpowiada� nr ID, tzn 0 - dystans itp.
 
 		final CharSequence[] items = { getMyString(R.string.distance),
 				getMyString(R.string.pace), getMyString(R.string.pace_avrage),
 				getMyString(R.string.time) };
 
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setTitle(R.string.dialog_choose_what_to_display);
 		builder.setItems(items, new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int item) {
 				updateLabels(item, clickedLabelTextView, clickedUnitTextView,
 						clickedContentTextView);
 				if (clickedField == 1)
 					dataTextView1Content = item;
 				else
 					dataTextView2Content = item;
 			}
 		});
 		AlertDialog alert = builder.create();
 		alert.show();
 	}
 
 	//update display
 	private void updateData(TextView textBox, int meassuredValue) {
 
 		switch (meassuredValue) {
 		case distanceID:
 			textBox.setText(String.format("%.3f", distance / 1000));
 			break;
 		case paceID:
 			if (pace < 30) {
 				// convert pace to show second
 				double rest = pace - (int) pace;
 				rest = rest * 60;
 
 				String secondsZero = (rest < 10) ? "0" : "";
 
 				textBox.setText(String.format("%d:%s%.0f", (int) pace,
 						secondsZero, rest));
 			} else {
 				textBox.setText(getResources().getString(R.string.dashes));
 			}
 			break;
 		case avgPaceID:
 			if (avgPace < 30) {
 				// convert pace to show second
 				double rest = avgPace - (int) avgPace;
 				rest = rest * 60;
 
 				String secondsZero = (rest < 10) ? "0" : "";
 
 				textBox.setText(String.format("%d:%s%.0f", (int) avgPace,
 						secondsZero, rest));
 			} else {
 				textBox.setText(getResources().getString(R.string.dashes));
 			}
 			break;
 		case timeID:
 			long hours = time / 3600000;
 			long minutes = (time / 60000) - hours * 60;
 			long seconds = (time / 1000) - hours * 3600 - minutes * 60;
 			String hourZero = (hours < 10) ? "0" : "";
 			String minutesZero = (minutes < 10) ? "0" : "";
 			String secondsZero = (seconds < 10) ? "0" : "";
 
 			textBox.setText(String.format("%s%d:%s%d:%s%d", hourZero, hours,
 					minutesZero, minutes, secondsZero, seconds));
 			break;
 		}
 
 	}
 	
 	//count everything with 2 last location points
 	private void countData(Location location, Location lastLocation) {
 
 		Log.i("ActivityActivity", "countData: " + location);
 		LatLng latLng = new LatLng(location.getLatitude(),
 				location.getLongitude());
 		

 		traceOnMap.add(latLng);
 		traceOnMapObject.setPoints(traceOnMap.getPoints());
 		
 		CameraPosition cameraPosition = new CameraPosition.Builder()
 		.target(latLng)
 	    .zoom(17)                   // Sets the zoom
 	    .bearing(GeographicalEvaluations.countBearing(location, lastLocation))                // Sets the orientation of the camera to east
 	    .tilt(60)                   
 	    .build();                   // Creates a CameraPosition from the builder
 	mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
 		
 	//	mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
 
 		
 		
 		float speed = location.getSpeed();
 		GPSAccuracy.setText(String.format("%s %.2f m",
 				getString(R.string.gps_accuracy), location.getAccuracy()));
 
 		pace = (double) 1 / (speed * 60 / 1000);
 
 		distance += lastLocation.distanceTo(location);
 
 		double lastDistans = distance/1000;
 		
 		distance += lastLocation.distanceTo(location);
 		int distancetoShow = (int)(distance/1000);
 		//new km
 		if (distancetoShow-(int)lastDistans>0)
 			addMarker(location,distancetoShow);
 		
 		synchronized (time) {
 			avgPace = ((double) time / 60) / distance;
 		}
 
 
 
 	}
 	
 	private void addMarker(Location location, int distance) {
 		Marker marker = mMap.addMarker(new MarkerOptions().position(
 				new LatLng(location.getLatitude(), location.getLongitude()))
 				.title(distance + "km"));
 		marker.showInfoWindow();
 	}
 
 	
 	//this runs on every update
 	private void updateGpsInfo(Location newLocation)
 	{
 		// no pause and good gps
 		if (!isPaused
 				&& newLocation.getAccuracy() < MyLocationListener.REQUIRED_ACCURACY) {
 			// not first point after start or resume
 
 			if (lostGPSDialog != null) {
 				lostGPSDialog.dismiss();
 				lostGPSDialog = null;
 			}
 			
 			if (!traceWithTime.isEmpty() && !traceWithTime.getLast().isEmpty()) {
 
 				if (mLastLocation == null)
 					Log.e("Location_info",
 							"Shouldn't be here, mLastLocation is null");
 
 				
 				countData(newLocation, mLastLocation);
 			}
 			if (traceWithTime.isEmpty())
 				traceWithTime.add(new LinkedList<Pair<Location,Long>>());
 			updateData(DataTextView1, dataTextView1Content);
 			updateData(DataTextView2, dataTextView2Content);
 			traceWithTime.getLast().add(new Pair<Location, Long>(newLocation,calendar.getTimeInMillis()));
 		} else if (newLocation.getAccuracy() >= MyLocationListener.REQUIRED_ACCURACY) {
 			// TODO make progress dialog, waiting for gps
 			showLostGpsSignalDialog();
 		}
 		mLastLocation = newLocation;
 	}
 	
 	@Override
 	protected void onPause() {
 		beepPlayer.stopPlayer();
 		super.onPause();
 	}
 	
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 		if ((keyCode == KeyEvent.KEYCODE_BACK)) {
 			showAlertDialog();
 		}
 		return super.onKeyDown(keyCode, event);
 	}
 
 	// SERVICE METHODS
 	private ServiceConnection mConnection = new MyServiceConnection();
 
 	void doBindService() {
 
 		Log.i("Service_info", "ActivityActivity Binding");
 		Intent i = new Intent(ActivityActivity.this, MyLocationListener.class);
 		i.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
 		i.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
 		bindService(i, mConnection, Context.BIND_AUTO_CREATE);
 		mIsBound = true;
 
 	}
 
 	void doUnbindService() {
 		Log.i("Service_info", "Activity Unbinding");
 		if (mIsBound) {
 			unbindService(mConnection);
 			mIsBound = false;
 
 		}
 	}
 
 	// handler for the events launched by the service
 	private BroadcastReceiver mMyServiceReceiver = new BroadcastReceiver() {
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			Log.i("Service_info", "onReceive");
 			int messageType = intent
 					.getIntExtra(MyLocationListener.MESSAGE, -1);
 			switch (messageType) {
 			case MyLocationListener.MSG_SEND_LOCATION:
 				Log.i("Service_info", "ActivityActivity: got Location");
 
 				Location newLocation = (Location) intent
 						.getParcelableExtra("Location");
 
 				updateGpsInfo(newLocation);
 				
 				
 				break;
 			}
 		}
 	};
 }
