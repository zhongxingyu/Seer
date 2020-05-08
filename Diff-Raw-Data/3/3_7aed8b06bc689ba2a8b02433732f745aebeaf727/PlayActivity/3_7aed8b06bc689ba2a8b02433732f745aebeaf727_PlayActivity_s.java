 package com.jessescott.sonicity;
 
 /* IMPORTS */
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.text.DecimalFormat;
import java.text.Format;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.ServiceConnection;
import android.view.View;
 import android.widget.TextView;
 import android.telephony.PhoneStateListener;
 import android.telephony.TelephonyManager;
 import android.util.Log;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.IBinder;
 
 import org.puredata.android.io.AudioParameters;
 import org.puredata.android.service.PdService;
 import org.puredata.android.utils.PdUiDispatcher;
 import org.puredata.core.PdBase;
 import org.puredata.core.utils.IoUtils;
 
 //import processing.core.*;
 
 
 /* PLAYACTIVITY CLASS */
 public class PlayActivity extends Activity {
 	
 	// GLOBALS
 	private static final String TAG = "SoniCity";
 	private static final int REFRESH_RATE = 5000;
 	
 	public Runnable runnable;
 	SimpleDateFormat format;
 	Date date;
 	
 	private String dirName;
 	Boolean isSDPresent = false;
 	Date fileDate;
 	BufferedWriter writer;
 	
 	private PdUiDispatcher dispatcher;
 	private PdService pdService = null;
 	
 	LocationManager locationManager;
 	MyLocationListener locationListener;
 	Handler handler;
 	
 	TextView latitude, longitude, altitude, speed, accuracy;
 	TextView ActualLatitude, ActualLongitude, ActualAltitude, ActualSpeed, ActualAccuracy;
 	
 	float currentLatitude  	= 0;
 	float currentLongitude 	= 0;
 	float currentAltitude 	= 0;
 	float currentAccuracy  	= 0;
 	float currentSpeed 		= 0;
 	float currentBearing 	= 0;
 	String currentProvider 	= "";
 	
 	/* LIBPD */
 	
 	// Service
 	private final ServiceConnection pdConnection = new ServiceConnection() {
 		@Override
 		public void onServiceConnected(ComponentName name, IBinder service) {
 				pdService = ((PdService.PdBinder)service).getService();
 				try {
 					Log.e(TAG, "Starting Pd Service ");
 					initPd();
 					loadPatch();
 				}
 				catch(IOException e) {
 					Log.e(TAG, e.toString());
 					finish();
 				}
 		}
 
 		@Override
 		public void onServiceDisconnected(ComponentName name) {
 			// this method will never be called
 		}
 	};
 	
 	// Initialize Audio
 	private void  initPd() throws IOException {
 		Log.e(TAG, "Initializing PD ");
 		
 		// Audio Settings
 		AudioParameters.init(this);
 		Log.v(TAG, "Does This Device Support Low Latency? " + AudioParameters.supportsLowLatency());
 		int sampleRate = AudioParameters.suggestSampleRate();
 		pdService.initAudio(sampleRate, 0, 2, 10.0f);
 		start();
 		
 		// Dispatcher
 		dispatcher = new PdUiDispatcher();
 		PdBase.setReceiver(dispatcher);
 		
 	}
 	
 	// Start Audio
 	private void start() {
 		Log.e(TAG, "Starting PD ");
 		if (!pdService.isRunning()) {
 			Log.e(TAG, "Starting Audio ");
 			Intent intent = new Intent(PlayActivity.this, PlayActivity.class);
 			pdService.startAudio(intent, R.drawable.icon, "SoniCity", "Return to SoniCity.");
 		}
 	}
 	
 	// Load Patch
 	private void loadPatch() throws IOException {
 		Log.e(TAG, "Loading PD Patch");
 		File dir = getFilesDir();
 		IoUtils.extractZipResource(getResources().openRawResource(R.raw.sonicity), dir, true);
 		File patchFile = new File(dir, "sonicity.pd");
 		PdBase.openPatch(patchFile.getAbsolutePath());
 	}
 	
 	// Kill PD
 	private void cleanupPd() {
 		try {
 			unbindService(pdConnection);
 		} catch (IllegalArgumentException e) {
 			// already unbound
 			pdService = null;
 		}
 	}
 	
 	// Send Data
 	private void sendLatToPd(float n) {
 		n  = Math.abs(n);
 		// Hour
 		int hour = parseHour(n);
 		PdBase.sendFloat("LATh", hour);
 		// Minute
 		int minute = parseMinute(n);
 		PdBase.sendFloat("LATm", minute);
 		// Second
 		int second = parseSecond(n);
 		PdBase.sendFloat("LATs", second);
 	}
 	
 	private void sendLonToPd(float n) {
 		n  = Math.abs(n);
 		// Hour
 		int hour = parseHour(n);
 		PdBase.sendFloat("LONh", hour);
 		// Minute
 		int minute = parseMinute(n);
 		PdBase.sendFloat("LONm", minute);
 		// Second
 		int second = parseSecond(n);
 		PdBase.sendFloat("LONs", second);
 	}
 
 	private void sendAltToPd(float n) {
 		PdBase.sendFloat("ALT", n);
 	}
 	
 	private void sendSpdToPd(float n) {
 		PdBase.sendFloat("SPD", n);
 	}
 	
 	private void sendAccToPd(float n) {
 		PdBase.sendFloat("ACC", n);
 	}
 	
 	// Parse Data
 	private int parseHour(float val) {
 		int hour = (int)val;
 		hour = Math.abs(hour);
 		//Log.v(TAG, "The Hour is " + hour);
 		return hour;
 	}
 	
 	private int parseMinute(float val) {
 		int hour = (int)val;
 		hour = Math.abs(hour);
 		float rem = val - hour;
 		DecimalFormat df = new DecimalFormat("##.######");
 		String min = df.format(rem);
 		String mm = "0";
 		if(min.length() >= 4) {
 			mm = min.substring(2, 4);
 		}
 		int minute = Integer.parseInt(mm);
 		//Log.v(TAG, "The Minute is " + minute);
 		return minute;
 	}
 	
 	private int parseSecond(float val) {
 		int hour = (int)val;
 		hour = Math.abs(hour);
 		float rem = val - hour;
 		DecimalFormat df = new DecimalFormat("##.######");
 		String min = df.format(rem);
 		String mm = "0";
 		if(min.length() >= 4) {
 			mm = min.substring(4, min.length());
 		}
 		int second = Integer.parseInt(mm);
 		//Log.v(TAG, "The Second is " + second);
 		return second;
 	}
 	
 	/* PHONE */
 	
 	private void initSystemServices() {
 		TelephonyManager telephonyManager =
 				(TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
 		telephonyManager.listen(new PhoneStateListener() {
 			@Override
 			public void onCallStateChanged(int state, String incomingNumber) {
 				if (pdService == null) return;
 				if (state == TelephonyManager.CALL_STATE_IDLE) {
 					start(); } else {
 						pdService.stopAudio(); }
 			}
 		}, PhoneStateListener.LISTEN_CALL_STATE);
 	}
 	
 	/* UI */
 
 	private void initGUI() {
 		setContentView(R.layout.play_layout);
 		
 		// TextViews
 		latitude  = (TextView) findViewById(R.id.Latitude);
 		longitude = (TextView) findViewById(R.id.Longitude);
 		ActualLatitude  = (TextView) findViewById(R.id.ActualLat);
 		ActualLongitude = (TextView) findViewById(R.id.ActualLon);
 		ActualAltitude = (TextView) findViewById(R.id.ActualAlt);	
 		ActualSpeed = (TextView) findViewById(R.id.ActualSpd);
 		ActualAccuracy = (TextView) findViewById(R.id.ActualAcc);
 		
 	}
 	
 	@SuppressLint("SimpleDateFormat")
 	public Date getTime() {
 		format = new SimpleDateFormat("HH:MM:SS");
 		date = new Date();
 		return date;
 	}
 	
 	
 	/* LIFECYCLE */
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		if(savedInstanceState != null) {
 			Log.v(TAG, " - There's An Existing State - ");
 		}
 		else {
 			Log.v(TAG, " - Brand New State - ");
 		}
 		Log.v(TAG, " - Starting The Play Screen - ");
 		
 		// UI
 		initGUI();
 		
 		// GPS
 		locationListener = new MyLocationListener(this);
 		
 		// Telephone Services
 		initSystemServices();
 		
 		// PD Service
 		bindService(new Intent(this, PdService.class), pdConnection, BIND_AUTO_CREATE);
 		
 		// Directory
 		isSDPresent = android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
 		if(isSDPresent) {
 			Log.v(TAG, " - SD Card Online - ");
 			try{
 				dirName = "//sdcard//SonicCity";
 				File newFile = new File(dirName);
 				newFile.mkdirs();
 			}
 			catch(Exception e) {
 				Log.e(TAG, "Couldnt Access SD Card");
 				e.printStackTrace();
 			}
 		}
 		else {
 			Log.v(TAG, " - No SD Card Available - ");
 		}
 		
 		// Writer
 		try {
 			SimpleDateFormat hhmmss = new SimpleDateFormat("HH_MM_SS");
 			fileDate = getTime();
 			StringBuilder fileName = new StringBuilder(hhmmss.format(getTime()));
 			Log.v(TAG, "The time is " + fileName);
 			writer = new BufferedWriter(new FileWriter("//sdcard//SonicCity//" + fileName + ".txt", true));
 			writer.flush();
 			//writer.close();
 		}
 		catch(Exception e) {
 			Log.e(TAG, "Couldnt Create File");
 			e.printStackTrace();
 		}
 		
 		// Runnable
 		handler = new Handler();
 		runnable = new Runnable() {
 			public void run() {
 				//Log.v(TAG, "run");
 				handler.postDelayed(this, REFRESH_RATE);
 				
 				// Get Data
 				Log.v(TAG, "runnable getting data");
 				float lat = Float.parseFloat(locationListener.getCurrentLatitude());
 				float lon = Float.parseFloat(locationListener.getCurrentLongitude());
 				float alt = Float.parseFloat(locationListener.getCurrentAltitude());
 				float spd = Float.parseFloat(locationListener.getCurrentSpeed());
 				float acc = Float.parseFloat(locationListener.getCurrentAccuracy());
 				
 				// Send Data
 				Log.v(TAG, "runnable sending data");
 				sendLatToPd(lat);
 				sendLonToPd(lon);
 				sendAltToPd(alt);
 				sendSpdToPd(spd);
 				sendAccToPd(acc);
 				
 				// Update Text
 				Log.v(TAG, "runnable updating text");
 				ActualLatitude.setText(locationListener.getCurrentLatitude());
 				ActualLongitude.setText(locationListener.getCurrentLongitude());
 				ActualAltitude.setText(locationListener.getCurrentAltitude());
 				ActualSpeed.setText(locationListener.getCurrentSpeed());
 				ActualAccuracy.setText(locationListener.getCurrentAccuracy());
 				
 				// Write Data
 				try {
 					writer.write(locationListener.getCurrentLatitude());
 					writer.write("\t");
 					writer.write(locationListener.getCurrentLongitude());
 					writer.write("\t");
 					writer.write(locationListener.getCurrentAltitude());
 					writer.write("\t");
 					writer.write(locationListener.getCurrentSpeed());
 					writer.write("\t");
 					writer.write(locationListener.getCurrentAccuracy());
 					writer.write("\t");
 					writer.write("\n");
 					writer.flush();
 				}
 				catch(Exception e) {
 					Log.e(TAG, "Couldnt Write To File");
 					e.printStackTrace();
 				}
 				
 				Log.v(TAG, "runnable running at " + getTime());
 			}
 		};
 		handler.postDelayed(runnable, REFRESH_RATE);
 		
 	}
 	
 	@Override
 	public void onSaveInstanceState(Bundle savedInstanceState) {
 		Log.v(TAG, " - Saving The State - ");
 	    // Save data
 
 	    
 	    // Always call the superclass so it can save the view hierarchy state
 	    super.onSaveInstanceState(savedInstanceState);
 	}
 	
 	@Override
 	protected void onPause() {
 		super.onPause();
 		Log.v(TAG, " - Exiting From The Play Screen - ");
 
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		Log.v(TAG, " - Entering The Play Screen - ");
 		
 		// GPS
 		locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
 		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, REFRESH_RATE, 5, locationListener);
 		locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
 	}
 	
 	@Override
 	public void onDestroy() {
 		super.onDestroy();
 		Log.v(TAG, " - Destroying Play Activity - ");
 		
 		// Kill Runnable
 		handler.removeCallbacks(runnable);
 		
 		// Close Writer
 		try {
 			writer.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 		// Kill Pd
 		cleanupPd();
 		
 		// Stop GPS
 		locationManager.removeUpdates(locationListener);
 		locationManager = null;
 
 	}
 
 } /*  */
