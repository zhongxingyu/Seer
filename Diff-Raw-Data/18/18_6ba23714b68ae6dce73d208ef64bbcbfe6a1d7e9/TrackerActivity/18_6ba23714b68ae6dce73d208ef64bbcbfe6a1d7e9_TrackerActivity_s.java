 package com.valfom.tracker;
 
import java.util.concurrent.TimeUnit;

 import android.app.ActionBar;
 import android.app.Activity;
 import android.app.Fragment;
 import android.app.FragmentManager;
 import android.app.FragmentTransaction;
 import android.app.ProgressDialog;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.SharedPreferences;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.os.PowerManager;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.SpinnerAdapter;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.valfom.tracker.TrackerMainFragment.OnButtonClickedListener;
 import com.valfom.tracker.TrackerMainFragment.OnStatusRestoredListener;
 
 public class TrackerActivity extends Activity implements
 		ActionBar.OnNavigationListener, OnButtonClickedListener, OnStatusRestoredListener {
 
 	public final static String BROADCAST_ACTION = "com.valfom.servicetest";
 
 	private static LocationManager locationManager;
 	public static ProgressDialog progressDialog;
 	
 	public static String state = "stopped";
 	public static PowerManager.WakeLock wl;
 	
 	private long lastBackPressTime = 0;
 	private Toast toastOnExit;
 	
 	final DatabaseHandler db = new DatabaseHandler(this);
 	
 	private void updateUI(Intent intent) {
 
 		long duration = intent.getLongExtra("duration", 0);
 		double distance = intent.getDoubleExtra("distance", 0);
 		float speed = intent.getFloatExtra("speed", 0);
 		float maxSpeed = intent.getFloatExtra("maxSpeed", 0);
 		float avgSpeed = intent.getFloatExtra("avgSpeed", 0);
 		double paceLast = intent.getDoubleExtra("paceLast", 0);
 		double avgPace = intent.getDoubleExtra("avgPace", 0);
 
 		if (getFragmentManager().findFragmentById(R.id.fragment_container).getTag().compareTo("Main") == 0) {
 			
 			TrackerMainFragment fragmentMain = (TrackerMainFragment) getFragmentManager()
 					.findFragmentById(R.id.fragment_container);
 			
 			if (fragmentMain != null) {
 				
 				long millis = duration;
 				int seconds = (int) (millis / 1000);
 	        	int minutes = seconds / 60;
 	        	seconds     = seconds % 60;
 	        	int hours = minutes / 60;
 	        	minutes = minutes % 60;
 	        	
 	        	TrackerSettings settings = new TrackerSettings(this);
 	        	
 	        	if (settings.getUnitId() == 0) {
 	        		
 	        		distance = (distance / 1000); // meters to kilometers
 	        		speed = (speed * 3600 / 1000);
 	        		maxSpeed = (maxSpeed * 3600 / 1000);
 	        		avgSpeed = (avgSpeed * 3600 / 1000);
 	        		
 	        	} else if (settings.getUnitId() == 1) {
 	        		
 	        		distance = (distance / 1609.344); // meters to miles
 	        		speed = (float) (speed * 2.2369);
 	        		maxSpeed = (float) (maxSpeed * 2.2369);
 	        		avgSpeed = (float) (avgSpeed * 2.2369);
 	        	}
 	
 				((TextView) fragmentMain.getView().findViewById(R.id.timeTV))
 						.setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));
 				((TextView) fragmentMain.getView().findViewById(R.id.distanceTV))
 						.setText(String.format("%.2f", distance));
 				((TextView) fragmentMain.getView().findViewById(R.id.curSpeedTV))
 						.setText(String.format("%02.0f", speed));
 				((TextView) fragmentMain.getView().findViewById(R.id.maxSpeedTV))
 						.setText(String.format("%02.0f", maxSpeed));
 				((TextView) fragmentMain.getView().findViewById(R.id.avgSpeedTV))
 					.setText(String.format("%02.0f", avgSpeed));
 				
 				((TextView) fragmentMain.getView().findViewById(R.id.paceLastTV))
 					.setText(String.format("%.2f", paceLast));
 				((TextView) fragmentMain.getView().findViewById(R.id.avgPaceTV))
 					.setText(String.format("%.2f", avgPace));
 			}
 		}
 	}
 	
 	private void startUI() {
 		
 		TrackerMainFragment fragmentMain = (TrackerMainFragment) getFragmentManager()
 				.findFragmentById(R.id.fragment_container); //********************************
 
 		if (fragmentMain != null) {
 
 			((TextView) fragmentMain.getView().findViewById(R.id.startBtn)).setVisibility(View.GONE);
 			((TextView) fragmentMain.getView().findViewById(R.id.stopBtn)).setVisibility(View.VISIBLE);
 			((TextView) fragmentMain.getView().findViewById(R.id.pauseBtn)).setVisibility(View.VISIBLE);
 		}
 	}
 	
 	private void stopUI() {
 		
 		TrackerMainFragment fragmentMain = (TrackerMainFragment) getFragmentManager()
 				.findFragmentById(R.id.fragment_container); //********************************
 
 		if (fragmentMain != null) {
 
 			((TextView) fragmentMain.getView().findViewById(R.id.startBtn)).setVisibility(View.VISIBLE);
 			((TextView) fragmentMain.getView().findViewById(R.id.stopBtn)).setVisibility(View.GONE);
 			((TextView) fragmentMain.getView().findViewById(R.id.pauseBtn)).setVisibility(View.GONE);
 			
 			((TextView) fragmentMain.getView().findViewById(R.id.timeTV)).setText(R.string.default_value_time);
 			((TextView) fragmentMain.getView().findViewById(R.id.distanceTV)).setText(R.string.default_value_distance);
 			((TextView) fragmentMain.getView().findViewById(R.id.curSpeedTV)).setText(R.string.default_value_speed);
 			((TextView) fragmentMain.getView().findViewById(R.id.avgSpeedTV)).setText(R.string.default_value_speed);
 			((TextView) fragmentMain.getView().findViewById(R.id.maxSpeedTV)).setText(R.string.default_value_speed);
 			
 			((TextView) fragmentMain.getView().findViewById(R.id.paceLastTV)).setText(R.string.default_value_distance);
 			((TextView) fragmentMain.getView().findViewById(R.id.avgPaceTV)).setText(R.string.default_value_distance);
 		}
 	}
 	
 	private void pauseUI() {
 		
 		TrackerMainFragment fragmentMain = (TrackerMainFragment) getFragmentManager()
 				.findFragmentById(R.id.fragment_container); //********************************
 
 		if (fragmentMain != null) {
 			
 			long millis = TrackerService.millis;
 			int seconds = (int) (millis / 1000);
         	int minutes = seconds / 60;
         	seconds     = seconds % 60;
         	int hours = minutes / 60;
         	minutes = minutes % 60;
         	
         	TrackerSettings settings = new TrackerSettings(this);
         	
         	double distance = TrackerService.distance;
         	float maxSpeed = TrackerService.maxSpeed;
         	float avgSpeed = TrackerService.avgSpeed;
         	double paceLast = TrackerService.paceLast;
         	double avgPace = TrackerService.avgPace;
         	
         	if (settings.getUnitId() == 0) {
         		
         		distance = (distance / 1000); // meters to kilometers
         		maxSpeed = (maxSpeed * 3600 / 1000);
         		avgSpeed = (avgSpeed * 3600 / 1000);
         		
         	} else if (settings.getUnitId() == 1) {
         		
         		distance = (distance / 1609.344); // meters to miles
         		maxSpeed = (float) (maxSpeed * 2.2369);
         		avgSpeed = (float) (avgSpeed * 2.2369);
         	}
 
 			((TextView) fragmentMain.getView().findViewById(R.id.startBtn)).setVisibility(View.GONE);
 			((TextView) fragmentMain.getView().findViewById(R.id.stopBtn)).setVisibility(View.VISIBLE);
 			((TextView) fragmentMain.getView().findViewById(R.id.pauseBtn)).setVisibility(View.VISIBLE);
 			((TextView) fragmentMain.getView().findViewById(R.id.pauseBtn)).setText(R.string.resume_btn);
 			
 			((TextView) fragmentMain.getView().findViewById(R.id.timeTV))
 				.setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));
 			((TextView) fragmentMain.getView().findViewById(R.id.distanceTV)).setText(String.format("%.2f", distance));
 			((TextView) fragmentMain.getView().findViewById(R.id.curSpeedTV)).setText(R.string.default_value_speed);
 			((TextView) fragmentMain.getView().findViewById(R.id.avgSpeedTV)).setText(String.format("%02.0f", avgSpeed));
 			((TextView) fragmentMain.getView().findViewById(R.id.maxSpeedTV)).setText(String.format("%02.0f", maxSpeed));
 			
 			((TextView) fragmentMain.getView().findViewById(R.id.paceLastTV)).setText(String.format("%.2f", paceLast));
 		((TextView) fragmentMain.getView().findViewById(R.id.avgPaceTV)).setText(String.format("%.2f", avgPace));
 		}
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 
 		final ActionBar actionBar = getActionBar();
 		actionBar.setDisplayShowTitleEnabled(false);
 		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
 		
 		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
 		wl = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "My Tag");
 		
 		SpinnerAdapter spinnerAdapter = ArrayAdapter.createFromResource(actionBar.getThemedContext(), R.array.dropdown_items,
 		        android.R.layout.simple_spinner_dropdown_item);
 		actionBar.setListNavigationCallbacks(spinnerAdapter, this);
 
 		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
 
 		IntentFilter intentFilter = new IntentFilter(BROADCAST_ACTION);
 		registerReceiver(broadcastReceiver, intentFilter);
 	}
 	
 	private void goToInfo() {
 		
 		int trackId = db.getLastId();
		Toast.makeText(this, "id_"+db.getLastId(), Toast.LENGTH_SHORT).show();
 		
 		FragmentManager fragmentManager = getFragmentManager();
 		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
 		
 		Fragment listFragment = fragmentManager.findFragmentById(R.id.fragment_container);
 		fragmentTransaction.remove(listFragment);
 
 		Fragment infoFragment = new TrackInfoFragment();
 
 		Bundle args = new Bundle();
 		args.putInt("id", trackId);
 		args.putString("type", "choise");
 		infoFragment.setArguments(args);
 
 		fragmentTransaction.add(R.id.fragment_container_info, infoFragment, "Info");
 
 		fragmentTransaction.addToBackStack(null);
 		
 		fragmentTransaction.commit();
 	}
 	
 	@Override
 	public void onBackPressed() {
 		
 		TrackInfoFragment frInfo = (TrackInfoFragment) getFragmentManager().findFragmentById(R.id.fragment_container_info);
 		
 		if ((frInfo == null)) {
 
 			if (this.lastBackPressTime < System.currentTimeMillis() - 2000) {
 				
 			    toastOnExit = Toast.makeText(this, getString(R.string.general_on_exit), Toast.LENGTH_SHORT);
 			    toastOnExit.show();
 			    this.lastBackPressTime = System.currentTimeMillis();
 			    
 			  } else {
 			    
 				  if (toastOnExit != null)
 					  toastOnExit.cancel();
 				  
 				  super.onBackPressed();
 			 }
 		} else {
 			
 			super.onBackPressed();
 		}
 	}
 
 	BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
 
 		public void onReceive(Context context, Intent intent) {
 
 			if (intent.hasExtra("destroyed")) {
 //				Toast.makeText(context, "destroyed", Toast.LENGTH_SHORT).show();
 				state = "stopped";
 				saveState();
 				
 				goToInfo();
 			} else {
 			
 //				Toast.makeText(context, "not destroyed", Toast.LENGTH_SHORT).show();
 				
 				if (state.compareTo("started") != 0) {
 					
 					state = "started";
 					saveState();
 				}
 				
 				if (!wl.isHeld())
 					enableKeepScreenOn();
 			
 				updateUI(intent);
 			}
 		}
 	};
 
 	public void startService() {
 
 		Intent intent = new Intent(this, TrackerService.class);
 		startService(intent);
 	}
 
 	public void stopService() {
 
 		Intent intent = new Intent(this, TrackerService.class);
 		stopService(intent);
 	}
 
 	@Override
 	protected void onDestroy() {
 		
 		state = "stopped";
 		saveState();
 
 		unregisterReceiver(broadcastReceiver);
 		
 		if (wl.isHeld())
 			wl.release();
 		
 		super.onDestroy();
 	}
 
 	public boolean onNavigationItemSelected(int position, long id) {
 
 		FragmentManager fm = getFragmentManager();
 		FragmentTransaction ft = fm.beginTransaction();
 		
 		TrackInfoFragment frInfo = (TrackInfoFragment) fm.findFragmentById(R.id.fragment_container_info);
 		if (frInfo != null)
 			ft.remove(frInfo);
 		
 		switch (position) {
 		
 		case 0:
 			ft.replace(R.id.fragment_container, new TrackerMainFragment(), "Main");
 			ft.commit();
 			
 			return true;
 		case 1:
 			ft.replace(R.id.fragment_container, new TrackerListFragment(), "List");
 			ft.commit();
 			
 			return true;
 		default:
 			return false;	
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 
 		getMenuInflater().inflate(R.menu.menu_tracker_activity, menu);
 
 		return true;
 	}
 
 	@Override
 	public boolean onMenuItemSelected(int featureId, MenuItem item) {
 
 		Intent settingsActivity = new Intent(TrackerActivity.this,
 				TrackerPreferenceActivity.class);
 		startActivity(settingsActivity);
 
 		return super.onMenuItemSelected(featureId, item);
 	}
 	
 	public void enableKeepScreenOn() {
 		
 		TrackerSettings settings = new TrackerSettings(this);
 		
 		if (settings.isKeepScreenOn())
 			wl.acquire();
 	}
 
 	public void disableKeepScreenOn() {
 		
 		if (wl.isHeld())
 			wl.release();
 	}
 	
 	public void onStatusRestored(String state) {
 	
 		if (state.compareTo("started") == 0)
 			startUI();
 		else if (state.compareTo("paused") == 0)
 			pauseUI();
 	}
 	
 	public void onButtonClicked(int btn) {
 
 		switch (btn) {
 
 		case TrackerMainFragment.BTN_START:
 
 			if (!locationManager
 					.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
 
 				Intent locationSettingsIntent = new Intent(
 						android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
 				startActivity(locationSettingsIntent);
 			} else {
 
 				progressDialog = ProgressDialog.show(TrackerActivity.this, "", getString(R.string.general_starting_gps));
 				progressDialog.setCancelable(true);
 				progressDialog.setCanceledOnTouchOutside(false);
 				progressDialog
 						.setOnCancelListener(new DialogInterface.OnCancelListener() {
 
 							public void onCancel(DialogInterface dialog) {
 
 								state = "stopped";
 								saveState();
 								stopUI();
 								stopService();
 								disableKeepScreenOn();
 							}
 						});
 
 				state = "started";
 				saveState();
 				startUI();
 				startService();
 				enableKeepScreenOn();
 			}
 
 			break;
 		case TrackerMainFragment.BTN_STOP:
 
 			state = "stopped";
 			saveState();
 			stopUI();
 			stopService();
 			disableKeepScreenOn();
 
 			break;
 		case TrackerMainFragment.BTN_PAUSE:
 
 			state = "paused";
 			saveState();
 			break;
 		default:
 			break;
 		}
 	}
 	
 	private void saveState() {
 		
 		SharedPreferences mySharedPreferences = getSharedPreferences("system.xml", Activity.MODE_PRIVATE);
 		SharedPreferences.Editor editor = mySharedPreferences.edit();
 		editor.putString("state", state);
 		editor.apply();
 	}
 }
