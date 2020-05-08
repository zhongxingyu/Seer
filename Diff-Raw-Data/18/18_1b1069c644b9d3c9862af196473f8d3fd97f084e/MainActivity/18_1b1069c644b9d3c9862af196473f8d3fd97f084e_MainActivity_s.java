 package com.laskowski.simplegpstracker;
 
 import java.util.Calendar;
 import java.util.List;
 
 import android.annotation.SuppressLint;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.location.Location;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.IBinder;
 import android.os.Message;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.Button;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 
import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.SupportMapFragment;
 import com.google.android.gms.maps.model.LatLng;
 import com.laskowski.simplegpstracker.db.DBUtils;
 import com.laskowski.simplegpstracker.fragments.EnableGpsDialogFragment;
 import com.laskowski.simplegpstracker.service.GpsTrackService;
 import com.laskowski.simplegpstracker.task.CreatePolylineTask;
 import com.laskowski.simplegpstracker.util.SpeedDistanceTuple;
 
 public class MainActivity extends BaseActivity {
 
	public static final String TAG = "com.laskowski.simplegpstracker.FragmentActivity";
 
 	private static final long UI_UPDATE_INTERVAL = 100;
 
 	// UI handler codes.
 	private static final int UPDATE_TIME = 1;
 	private static final int UPDATE_TOTAL_DIST = 2;
 	private static final int UPDATE_AVG_SPEED = 3;
 	private static final int UPDATE_CUR_SPEED = 4;
 
 	private boolean mIsStart = true;
 	private boolean mIsBound = false;
 
 	private TextView mCurSpeed;
 
 	private Handler mHandler;
 
 	// the background service runnning the gps interaction
 	private GpsTrackService mBoundService;
 
 	private Button mStartStopButton;
 	private Long mStartTime = 0L;
 	private int mTotalTime = 0;
 
 	private Handler mTimeHandler = new Handler();
 
 	private TextView mTotalDistance;
 
 	private Integer mTotalDistanceInMeters = 0;
 
 	private static final int HISTORY_ID = Menu.FIRST;
 
 	/**
 	 * task for updating the elapsed time on UI
 	 */
 	private Runnable mUpdateTimeTask = new Runnable() {
 		public void run() {
 			final long start = mStartTime;
 			long millis = System.currentTimeMillis() - start;
 			int seconds = (int) (millis / 1000);
 
 			mTotalTime = seconds;
 
 			int minutes = seconds / 60;
 			seconds = seconds % 60;
 
 			if (seconds < 10) {
 				mTimeElapsed.setText("" + minutes + ":0" + seconds);
 			} else {
 				mTimeElapsed.setText("" + minutes + ":" + seconds);
 			}
 
 			// rest of UI fields - update only each second time
 			if (millis % 2 == 0) {
 				if (mIsBound && mBoundService != null) {
 					SpeedDistanceTuple tuple = mBoundService.getUIData();
 					if (tuple != null) {
 						Message.obtain(mHandler, UPDATE_TOTAL_DIST,
 								tuple.getDistance()).sendToTarget();
 						Message.obtain(mHandler, UPDATE_CUR_SPEED,
 								tuple.getSpeed()).sendToTarget();
 					}
 				} else {
 					Log.w(TAG,
 							"service was not bound, unable to update travelled distance");
 				}
 			}
 
 			mTimeHandler.postDelayed(this, UI_UPDATE_INTERVAL);
 		}
 	};
 
 	@SuppressLint("HandlerLeak")
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		setContentView(R.layout.activity_main);
 
 		mTimeElapsed = (TextView) findViewById(R.id.time);
 		mTotalDistance = (TextView) findViewById(R.id.totaldist);
 		mAvgSpeed = (TextView) findViewById(R.id.avgspeed);
 		mFragmentLayout = (LinearLayout) findViewById(R.id.fragment_layout);
 

 		mCurSpeed = (TextView) findViewById(R.id.curspeed);
 
 		mStartStopButton = (Button) findViewById(R.id.start_stop_button);
 
 		// Handler for updating text fields on the UI like the lat/long and
 		// address.
 		mHandler = new Handler() {
 			public void handleMessage(Message msg) {
 				switch (msg.what) {
 				case UPDATE_TIME:
 					mTimeElapsed.setText((String) msg.obj);
 					break;
 				case UPDATE_TOTAL_DIST:
 					mTotalDistance.setText((String) msg.obj);
 					break;
 				case UPDATE_CUR_SPEED:
 					mCurSpeed.setText((String) msg.obj);
 					break;
 				case UPDATE_AVG_SPEED:
 					mAvgSpeed.setText((String) msg.obj);
 					break;
 				default:
 
 				}
 
 			}
 		};
 
 		mMapFragment = (SupportMapFragment) getSupportFragmentManager()
 				.findFragmentById(R.id.map);
 		mMapFragment.setRetainInstance(true);
 
 	}
 
 	@Override
 	protected void onDestroy() {
 		super.onDestroy();
 		doUnbindService();
 	}
 
 	@Override
 	protected void onPostCreate(Bundle savedInstanceState) {
 		super.onPostCreate(savedInstanceState);
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 
 		// setUp();
 	}
 
 	@Override
 	public void onSaveInstanceState(Bundle state) {
 		super.onSaveInstanceState(state);
 		state.putInt("mTotalDistanceInMeters", mTotalDistanceInMeters);
 		state.putCharSequence("mStartStopButton", mStartStopButton.getText());
 		state.putCharSequence("mTimeElapsed", mTimeElapsed.getText());
 
 	}
 
 	@Override
 	public void onRestoreInstanceState(Bundle state) {
 		super.onRestoreInstanceState(state);
 		mTotalDistanceInMeters = state.getInt("mTotalDistanceInMeters");
 		mStartStopButton.setText(state.getCharSequence("mStartStopButton"));
 		mTimeElapsed.setText(state.getCharSequence("mTimeElapsed"));
 
 	}
 
 	@Override
 	protected void onStart() {
 		super.onStart();
 
 		LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
 
 		final boolean gpsEnabled = locationManager
 				.isProviderEnabled(LocationManager.GPS_PROVIDER);
 
 		if (!gpsEnabled) {
 			// Build an alert dialog here that requests that the user enable the
 			// location services, then when the user clicks the "OK"
 			// button, call enableLocationSettings()
 			new EnableGpsDialogFragment().show(getSupportFragmentManager(),
 					"enableGpsDialog");
 		}
 	}
 
 	@Override
 	protected void onStop() {
 		super.onStop();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		super.onCreateOptionsMenu(menu);
 		menu.add(Menu.NONE, HISTORY_ID, Menu.NONE, R.string.menu_history);
 		return true;
 	}
 
 	@Override
 	public boolean onMenuItemSelected(int featureId, MenuItem item) {
 		switch (item.getItemId()) {
 		case HISTORY_ID:
 			Intent i = new Intent(this, TripListActivity.class);
 			startActivityForResult(i, HISTORY_ID);
 			return true;
 		}
 		return super.onMenuItemSelected(featureId, item);
 	}
 
 	private void setUpMapIfNeeded() {
 		if (mIsBound) {
 			List<Location> locations = mBoundService.getLocations();
 			if (mMap == null) {
 
 				mMap = ((SupportMapFragment) getSupportFragmentManager()
 						.findFragmentById(R.id.map)).getMap();
 				if (mMap != null && locations.size() > 1) {
 					new CreatePolylineTask(this).execute(mBoundService
 							.getmLatLngs().toArray(
 									new LatLng[mBoundService.getmLatLngs()
 											.size()]));
 				}
 			} else if (locations.size() > 1) {
 				new CreatePolylineTask(this)
 						.execute(mBoundService.getmLatLngs().toArray(
 								new LatLng[mBoundService.getmLatLngs().size()]));
 			}
 		}
 
 	}
 
 	public void startGps(View v) {
 
 		if (!mIsStart) {
 
 			mTimeHandler.removeCallbacks(mUpdateTimeTask);
 
 			mStartStopButton.setText(R.string.start_label);
 			setUpMapIfNeeded();
 			if (mIsBound) {
 				// update total average speed
 				Message.obtain(mHandler, UPDATE_AVG_SPEED,
 						mBoundService.getAverageSpeed(mTotalTime))
 						.sendToTarget();
 				DBUtils db = DBUtils.getInstance(this);
 				db.createTripEntry("Trip from "
 						+ Calendar.getInstance().getTime().toString(),
 						mTimeElapsed.getText().toString(),
 						mBoundService.getAverageSpeed(mTotalTime),
 						mTotalDistance.getText().toString(),
 						mBoundService.getLocations());
 			}
 			doUnbindService();
 			stopService(new Intent(this, GpsTrackService.class));
 
 		} else {
 
 			cleanAppState();
 
 			mStartStopButton.setText(R.string.stop_label);
 			doBindService();
 			startService(new Intent(this, GpsTrackService.class));
 
 			if (mStartTime == 0L) {
 				mStartTime = System.currentTimeMillis();
 				mTimeHandler.removeCallbacks(mUpdateTimeTask);
 				mTimeHandler.postDelayed(mUpdateTimeTask, UI_UPDATE_INTERVAL);
 			}
 
 		}
 		mIsStart = !mIsStart;
 	}
 
 	private void cleanAppState() {
 		if (mFragmentLayout.getVisibility() == View.VISIBLE) {
 			mFragmentLayout.setVisibility(View.INVISIBLE);
 		}
 		mStartTime = 0L;
 		mAvgSpeed.setText("0");
 		mTotalDistance.setText("0");
 		mTotalDistanceInMeters = 0;
 	}
 
 	void doBindService() {
 		bindService(new Intent(this, GpsTrackService.class), mConnection,
 				Context.BIND_AUTO_CREATE);
 		mIsBound = true;
 	}
 
 	void doUnbindService() {
 		if (mIsBound) {
 			// Detach our existing connection.
 			unbindService(mConnection);
 			mIsBound = false;
 		}
 	}
 
 	private ServiceConnection mConnection = new ServiceConnection() {
 		public void onServiceConnected(ComponentName className, IBinder service) {
 			mBoundService = ((GpsTrackService.LocalBinder) service)
 					.getService();
 
 		}
 
 		public void onServiceDisconnected(ComponentName className) {
 			mBoundService = null;
 			Toast.makeText(MainActivity.this, "some nasty message",
 					Toast.LENGTH_SHORT).show();
 		}
 	};
 
 	public boolean ismIsBound() {
 		return mIsBound;
 	}
 
 	public void setmIsBound(boolean mIsBound) {
 		this.mIsBound = mIsBound;
 	}
 
 	public GpsTrackService getmBoundService() {
 		return mBoundService;
 	}
 
 	public void setmBoundService(GpsTrackService mBoundService) {
 		this.mBoundService = mBoundService;
 	}
 

 }
