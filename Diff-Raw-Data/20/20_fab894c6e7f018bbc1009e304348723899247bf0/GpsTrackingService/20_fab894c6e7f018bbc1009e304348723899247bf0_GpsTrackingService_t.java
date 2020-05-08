 package com.ysaito.runningtrainer;
 
 import java.util.ArrayList;
 import java.util.Locale;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import android.app.Service;
 import android.content.Context;
 import android.content.Intent;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.IBinder;
 import android.os.Looper;
 import android.speech.tts.TextToSpeech;
 import android.util.Log;
 import android.widget.Toast;
 
 public class GpsTrackingService extends Service {
     /*
      * Communication channel between RecordingActivity and GpsTrackingService. Since both objects always
      * reside in one process and are run by the main thread, we just function calls to send GPS updates
      * the GpsTrackingService from RecordingActivity.
      */
     private static final ArrayList<RecordingActivity> mListeners = new ArrayList<RecordingActivity>();
     
     public static void registerListener(RecordingActivity listener) {
     	mListeners.add(listener);
     }
     public static void unregisterListener(RecordingActivity listener) {
     	mListeners.remove(listener);
     }
     public static boolean isListenerRegistered(RecordingActivity listener) {
     	return mListeners.contains(listener);
     }
     
     private static boolean mGpsServiceStarted = false;
     private static GpsTrackingService mSingleton = null;
     
     private static final int STOPPED = 0;
     private static final int PAUSED = 1;
     private static final int RUNNING = 2;
     private int mState = RUNNING;
 
     public static GpsTrackingService getSingleton() { return mSingleton; }
     
     public void onLapButtonPress() {
     	mUserLapStats = new LapStats();
 		mSettings = Settings.getSettings(this);  // refresh in case user has changed anything
     	speak("New lap started");
     }
     
     public void onPauseButtonPress() {
     	speak("Paused");
     	mState = PAUSED;  
     	mTotalStats.onPause();
     	if (mUserLapStats != null) mUserLapStats.onPause();
     	if (mAutoLapStats != null) mAutoLapStats.onPause();
     	updateTimer();
     }
     
     public void onResumeButtonPress() {
     	speak("Resumed");
     	mState = RUNNING;
 		mSettings = Settings.getSettings(this);  // refresh in case user has changed anything
     	mTotalStats.onResume();
     	if (mUserLapStats != null) mUserLapStats.onResume();
     	if (mAutoLapStats != null) mAutoLapStats.onResume();
     	updateTimer();
     }
     
     public static boolean isGpsServiceRunning() {
     	return mGpsServiceStarted;
     }
     
     public static void startGpsServiceIfNecessary(Context context) {
     	if (!mGpsServiceStarted) {
     		mGpsServiceStarted = true;
     		context.startService(new Intent(context, GpsTrackingService.class));
     	}
     }
     
     public static void stopGpsServiceIfNecessary(Context context) {
     	if (mGpsServiceStarted) {
     		mGpsServiceStarted = false;
     		context.stopService(new Intent(context, GpsTrackingService.class));
     	}
     }
     
     private void speakStats() {
     	// newerStats is the newer of {user,auto}LapStats.
     	LapStats newerLapStats = mUserLapStats;
     	if (mAutoLapStats != null &&
     			(mAutoLapStats == null || mAutoLapStats.getStartTimeSeconds() < mAutoLapStats.getStartTimeSeconds())) {
     		newerLapStats = mAutoLapStats;
     	}
     	if (mSettings.speakTotalDistance)
     		speak("Total distance " + Util.distanceToSpeechText(mTotalStats.getDistance(), mSettings));
     	if (mSettings.speakTotalDuration)
     		speak("Total time " + Util.durationToSpeechText(mTotalStats.getDurationSeconds()));
     	if (mSettings.speakCurrentPace)
    		speak("Current pace " + Util.paceToSpeechText((long)mTotalStats.getCurrentPace(), mSettings));
     	if (mSettings.speakAveragePace)
    		speak("Average pace " + Util.paceToSpeechText((long)mTotalStats.getPace(), mSettings));
     	if (newerLapStats != null) {
     		if (mSettings.speakLapDistance)
     			speak("Lap distance " + Util.distanceToSpeechText(newerLapStats.getDistance(), mSettings));
     		if (mSettings.speakLapDuration)
     			speak("Lap time " + Util.durationToSpeechText((long)newerLapStats.getDurationSeconds()));
     		if (mSettings.speakLapPace)
    			speak("Lap pace " + Util.paceToSpeechText((long)newerLapStats.getPace(), mSettings));
     	}
     	if (mAutoLapStats != null) {
     		if (mSettings.speakAutoLapDistance)
     			speak("Auto lap distance " + Util.distanceToSpeechText(mAutoLapStats.getDistance(), mSettings));
     		if (mSettings.speakAutoLapDuration)
     			speak("Auto lap time " + Util.durationToSpeechText((long)mAutoLapStats.getDurationSeconds()));
     		if (mSettings.speakAutoLapPace)
    			speak("Auto lap pace " + Util.paceToSpeechText((long)mAutoLapStats.getPace(), mSettings));
     	}
     }
     
     private long mLastReportedTotalDistance = 0;
     private long mLastReportedTotalDuration = 0;
     
     private void notifyListeners() {
     	boolean needSpeak = false;
     	if (mSettings.speakDistanceInterval > 0) {
     		final int interval = (int)Math.max(15, mSettings.speakDistanceInterval);
     		final long newDistance = (long)mTotalStats.getDistance();
     		if ((mLastReportedTotalDistance / interval) != (newDistance / interval)) {
     			mLastReportedTotalDistance = newDistance;
     			needSpeak = true;
     		}
     	}
     	if (mSettings.speakTimeInterval > 0) {
     		final int interval = (int)Math.max(5, mSettings.speakTimeInterval);
     		final long newDuration = (long)mTotalStats.getDurationSeconds();
     		if (mLastReportedTotalDuration / interval != newDuration / interval) {
     			mLastReportedTotalDuration = newDuration;
     			needSpeak = true;
     		}
     	}
     	if (needSpeak) speakStats();
     	for (RecordingActivity listener : mListeners) {
     		listener.onGpsUpdate(mRecord, mPath, mTotalStats, mUserLapStats, mAutoLapStats);
     	}
     }
 	
 	private static String TAG = "Gps";
 	private LocationListener mLocationListener;
 	private LocationManager mLocationManager;
 
 	private HealthGraphClient.JsonActivity mRecord;
 	private ArrayList<HealthGraphClient.JsonWGS84> mPath;
 	
 	// Stats since the beginning of the activity. */
 	private LapStats mTotalStats = null;
 	
     // Stats since the start of the last manual lap
     private LapStats mUserLapStats = null;
     
     // Stats since the start of the last auto lap, i.e., the one that renews every 1mile or 1km.
     private LapStats mAutoLapStats = null;
     
 	
 	private static int mInstanceSeq = 0;
 	private int mId;
 	
 	@Override public String toString() {
 		return "GpsService[" + mId + "]";
 	}
 	
 	@Override
 	public void onCreate() {
 		mSettings = Settings.getSettings(this);
 		mId = mInstanceSeq++;
 		Toast.makeText(this, toString() + ": Created", Toast.LENGTH_LONG).show();
 		Log.d(TAG, "onCreate");
 		
 		mPath = new ArrayList<HealthGraphClient.JsonWGS84>();
 		mRecord = new HealthGraphClient.JsonActivity();
 		mRecord.type = "Running";  // TODO: allow changing
 		mRecord.start_time = HealthGraphClient.generateStartTimeString(System.currentTimeMillis() / 1000.0);
 		mRecord.notes = "Recorded by RunningTrainer";
 		mTotalStats = new LapStats();
 		
 		// Define a listener that responds to location updates
 		mLocationListener = new LocationListener() {
 			// Ignore location updates at intervals smaller than this limit.
 			static final int MIN_RECORD_INTERVAL_MS = 1000;
 
 			long mLastReportTime = 0;
 			
 			public void onLocationChanged(Location location) {
 				// Called when a new location is found by the network location provider.
 				// makeUseOfNewLocation(location);
 				final long time = System.currentTimeMillis();
 				if (time < mLastReportTime + MIN_RECORD_INTERVAL_MS) return;
 				onGpsLocationUpdate(time, location);
 				
 				mLastReportTime = time;
 			}
 			public void onStatusChanged(String provider, int status, Bundle extras) {
 				Log.d(TAG, "Status: " + provider + ": " + status);
 			}
 			
 			public void onProviderEnabled(String provider) {
 				Log.d(TAG, "Provider Enabled: " + provider);
 			}
 			
 			public void onProviderDisabled(String provider) {
 				Log.d(TAG, "Provider Disabled: " + provider);
 			}
 		};
 		
 		// Register the listener with the Location Manager to receive location updates
 		mLocationManager = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
 
 		mTimer = null;
 		updateTimer();
         mTts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
         	public void onInit(int status) {
         		mTts.setLanguage(Locale.US);
         		speak("started");
         	}
         });
 		mSingleton = this;
 	}
 
     private TextToSpeech mTts;
 	private Timer mTimer;
 	private Settings mSettings;
 	
 	private void speak(String text) {
 		mTts.speak(text, TextToSpeech.QUEUE_ADD, null);
 	}
 
 	private void updateTimer() {
     	if (mTimer != null) {
     		mTimer.cancel();
     		mTimer.purge();
     		mTimer = null;
     	}
 		if (mState == RUNNING) {
 			mTimer = new Timer();
 			mTimer.schedule(new TimerTask() {
 				@Override public void run() {
 					Handler handler = new Handler(Looper.getMainLooper());
 					handler.post(new Runnable() {
 						public void run() { notifyListeners(); }
 					});
 				}
 			}, 0, 1000);
 		}
 	}
 	
 	public void onDestroy() {
 		mSingleton = null;
 		mState = STOPPED;
 		updateTimer();
 		Toast.makeText(this, toString() + ": Stopped", Toast.LENGTH_LONG).show();
 		Log.d(TAG, "onDestroy");
 		mLocationManager.removeUpdates(mLocationListener);
 	}
 	
 	@Override
 	public void onStart(Intent intent, int startid) {
 		Toast.makeText(this, toString() + ": Started", Toast.LENGTH_LONG).show();
 		mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mLocationListener);
 	}
 
 	private void onGpsLocationUpdate(long now, Location newLocation) {
 		if (mState == RUNNING) {
 			HealthGraphClient.JsonWGS84 wgs = new HealthGraphClient.JsonWGS84();
 			wgs.latitude = newLocation.getLatitude();
 			wgs.longitude = newLocation.getLongitude();
 			wgs.altitude = newLocation.getAltitude();
 			if (mPath.size() == 0) {
 				wgs.timestamp = 0;
 				wgs.type = "start";
 			} else {
 				wgs.timestamp = mTotalStats.getDurationSeconds();
 				wgs.type = "gps";
 			}
 			mPath.add(wgs);
 			
 			
 			mTotalStats.updatePath(mPath);
 			notifyListeners();
 		}
 	}
 
 	@Override
 	public IBinder onBind(Intent intent) {
 		return null;
 	}
 }
