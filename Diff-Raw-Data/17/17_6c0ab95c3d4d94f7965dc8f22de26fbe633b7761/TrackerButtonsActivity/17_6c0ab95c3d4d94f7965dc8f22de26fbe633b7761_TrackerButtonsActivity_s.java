 package com.valfom.sports.tracker;
 
 import android.content.Context;
 import android.content.Intent;
 import android.hardware.Sensor;
 import android.hardware.SensorManager;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.ImageButton;
 
 import com.actionbarsherlock.app.SherlockActivity;
 import com.actionbarsherlock.view.Window;
 
 public class TrackerButtonsActivity extends SherlockActivity implements OnClickListener {
 
 	private SensorManager mSensorManager;
 	private TrackerShakeEventListener mShakeEventListener;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 
 		super.onCreate(savedInstanceState);
 		
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		
 		Intent intent = getIntent();
 		
 		int tabId = intent.getIntExtra("tabId", 1);
 		boolean started = intent.getBooleanExtra("started", false);
 		
 		if (tabId == 0) {
 			
 			setContentView(R.layout.dialog_map_buttons);
 			
 			ImageButton btnMapSatellite = (ImageButton) findViewById(R.id.ivMapSatelliteDialog);
 			
 			btnMapSatellite.setImageResource(TrackerMapFragment.mapView.isSatellite() ? R.drawable.ic_map : R.drawable.ic_map);
 			btnMapSatellite.setContentDescription(getString(TrackerMapFragment.mapView.isSatellite() ? R.string.btn_map : R.string.btn_satellite));
 			
 			btnMapSatellite.setOnClickListener(this);
 			
 			boolean isMapLocked = intent.getBooleanExtra("isMapLocked", true);
 			
 			ImageButton btnLockUnlock = (ImageButton) findViewById(R.id.ivLockUnlockDialog);
 			
 			btnLockUnlock.setImageResource(isMapLocked ? R.drawable.ic_map_locked : R.drawable.ic_map_unlocked);
 			btnLockUnlock.setContentDescription(getString(isMapLocked ? R.string.btn_unlock : R.string.btn_lock));
 			
 			btnLockUnlock.setOnClickListener(this);
 			
 			ImageButton btnFindMyLocation = (ImageButton) findViewById(R.id.ivMyLocationDialog);
 			
 			btnFindMyLocation.setOnClickListener(this);
 			
 			if (started) {
 			
 				ImageButton btnAddMarker = (ImageButton) findViewById(R.id.ivAddMarkerDialog);
 			
 				btnAddMarker.setOnClickListener(this);
 				btnAddMarker.setVisibility(View.VISIBLE);
 			}
 		
 		} else if (tabId == 1) {
 			
 			setContentView(R.layout.dialog_main_buttons);
 			
 			Button btnStart = (Button) findViewById(R.id.ivStartDialog);
 			
 			btnStart.setOnClickListener(this);
 			
 			if (started) {
 
 				btnStart.setVisibility(View.GONE);
 				
 				Button btnStop = (Button) findViewById(R.id.ivStopDialog);
 				
 				btnStop.setOnClickListener(this);
 				btnStop.setVisibility(View.VISIBLE);
 			
 				Button btnPause = (Button) findViewById(R.id.ivPauseDialog);
 			
 				btnPause.setOnClickListener(this);
 				btnPause.setVisibility(View.VISIBLE);
 			}
 			
		} else if (tabId == 3) {
 			
 			setContentView(R.layout.dialog_info_buttons);
 			
 			Button btnSave = (Button) findViewById(R.id.ivSaveDialog);
 			
 			btnSave.setOnClickListener(this);
 			
 			Button btnDelete = (Button) findViewById(R.id.ivDeleteDialog);
 				
 			btnDelete.setOnClickListener(this);
 		}
 		
 		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
 		mShakeEventListener = new TrackerShakeEventListener();
 		
 		mShakeEventListener.setOnShakeListener(new TrackerShakeEventListener.OnShakeListener() {
 
 			public void onShake() {
 				
 				finish();
 			}
 		});
 	}
 	
 	@Override
 	public void onClick(View v) {
 
 		Intent result = new Intent();
 		
 		switch(v.getId()) {
 		
 			case R.id.ivMapSatelliteDialog:
 				result.putExtra("btnId", R.id.ivMapSatelliteDialog);
 				break;
 				
 			case R.id.ivLockUnlockDialog:
 				result.putExtra("btnId", R.id.ivLockUnlockDialog);
 				break;
 				
 			case R.id.ivMyLocationDialog:
 				result.putExtra("btnId", R.id.ivMyLocationDialog);
 				break;
 				
 			case R.id.ivAddMarkerDialog:
 				result.putExtra("btnId", R.id.ivAddMarkerDialog);
 				break;
 				
 			case R.id.ivStartDialog:
 				result.putExtra("btnId", R.id.ivStartDialog);
 				break;
 				
 			case R.id.ivStopDialog:
 				result.putExtra("btnId", R.id.ivStopDialog);
 				break;
 			
 			case R.id.ivPauseDialog:
 				result.putExtra("btnId", R.id.ivPauseDialog);
 				break;
 				
 			case R.id.ivSaveDialog:
 				result.putExtra("btnId", R.id.ivSaveDialog);
 				break;
 			
 			case R.id.ivDeleteDialog:
 				result.putExtra("btnId", R.id.ivDeleteDialog);
 				break;
 				
 			default:
 				break;
 		}
 		
 		setResult(RESULT_OK, result);
 		finish();
 	}
 	
 	@Override
 	protected void onPause() {
 		
 		mSensorManager.unregisterListener(mShakeEventListener);
 		
 		super.onPause();
 	}
 
 	@Override
 	protected void onResume() {
 		
 		super.onResume();
 		
 		mSensorManager.registerListener(mShakeEventListener,
 				mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
 				SensorManager.SENSOR_DELAY_UI);
 	}
 }
