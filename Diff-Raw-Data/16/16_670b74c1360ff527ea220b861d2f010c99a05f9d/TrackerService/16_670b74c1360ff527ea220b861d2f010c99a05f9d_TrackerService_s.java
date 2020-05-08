 package org.financetool.financetooltracker;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.concurrent.LinkedBlockingDeque;
 
 import android.app.ActivityManager;
 import android.app.Service;
 import android.app.ActivityManager.RunningServiceInfo;
 import android.content.Context;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Binder;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.util.Log;
 
 public class TrackerService extends Service implements LocationListener {
 	// Track locations every 15 seconds and that are at least 50m apart.
 	// Upload or save those locations in batches of 20 locations (i.e. 5 minutes)
 	
 	private static final long MIN_TIME_BTW_LOCS = 15 * 1000;
 	private static final float MIN_DIST_BTW_LOCS = 50.0f;	
	private static final int SAVE_BATCH_SIZE = 20;			
 	
 	public static String TAG = MainActivity.TAG; 
 	
 	private LocationManager lm;	
 	private DBUtil db;
 	private ServerUtil server;
 	private PreferenceUtil prefs;
 	
 	private Location lastLocation;
 	private LinkedBlockingDeque<Location> locationsToSave;
 
 	public static void bindAndStartIfUnstarted(Context c, ServiceConnection sc) {	
 		c.bindService(start(c), sc, 0);
 	}
 	
 	public static Intent start(Context c) {
 		Intent intent = new Intent(c, TrackerService.class);
 		if (!isRunning(c)) {
 			c.startService(intent);
 		}
 		return intent;
 	}
 	
 	public static boolean isRunning(Context c) {
 		String className = TrackerService.class.getCanonicalName();
 		ActivityManager manager 
 			= (ActivityManager) c.getSystemService(ACTIVITY_SERVICE);	    
 		for (RunningServiceInfo info : manager.getRunningServices(Integer.MAX_VALUE)) {
 	        if (className.equals(info.service.getClassName())) {
 	            return true;
 	        }
 	    }
 	    return false;
 	}
 
 	@Override
 	public int onStartCommand(Intent intent, int flags, int startId) {
 	    return START_STICKY;
 	}
 	
 	@Override
 	public void onCreate() {
 		super.onCreate();
 		
 		prefs = new PreferenceUtil(getApplicationContext());	
 		db = new DBUtil(getApplicationContext());
 		server = new ServerUtil(getApplicationContext());
 		
 		locationsToSave = new LinkedBlockingDeque<Location>();		
 		
 		lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
 		updateTrackingFromPrefs();
         
 		new SaveAndUploadThread().start();
 	}
 	
 	@Override
 	public void onDestroy() {
 		super.onDestroy();
 		sendThreadDoneSignal();
 		lm.removeUpdates(this);
 	}
 	
 	private void sendThreadDoneSignal() {
		locationsToSave.offer(null);
 	}
 	
 	private boolean shouldRecordLocation(Location l) {
 		return
			lastLocation == null 
 			|| !lastLocation.getProvider().equals(l.getProvider())
 			|| (
 				System.currentTimeMillis() - lastLocation.getTime() 
 					>= MIN_TIME_BTW_LOCS
 				&& lastLocation.distanceTo(l) >=  MIN_DIST_BTW_LOCS
 			);
 	}
 	
 	public void onLocationChanged(Location loc) {
 		if (shouldRecordLocation(loc)) {
 			lastLocation = loc;
 			locationsToSave.offer(loc);
		}		
 	}
 	public void onProviderDisabled(String provider) {
 	}
 	public void onProviderEnabled(String provider) {
 	}
 	public void onStatusChanged(String provider, int status, Bundle extras) {
 	}
 	
 	private void requestLocations(String provider, long minTime) {
 		lm.requestLocationUpdates(provider, minTime, 
 				MIN_DIST_BTW_LOCS, this);
 	}
 	
 	private void updateTrackingFromPrefs() {
 		lm.removeUpdates(this);
 		if (prefs.shouldTrackLocation()) {
 			requestLocations(LocationManager.NETWORK_PROVIDER, 
 					MIN_TIME_BTW_LOCS);
 			if (prefs.shouldUseGPS()) {
 				requestLocations(LocationManager.GPS_PROVIDER, 0);
 			} else {
 				requestLocations(LocationManager.PASSIVE_PROVIDER, 0);
 			}
 		}				
 	}
 			
 	private final IBinder binder = new LocalBinder();
 	
 	public class LocalBinder extends Binder {
 		public void uploadNow() {	
 		}
 		public void updateTrackingFromPrefs() {
 			TrackerService.this.updateTrackingFromPrefs();
 		}
     }
 	
 	@Override
 	public IBinder onBind(Intent intent) {
 		return binder;
 	}
 	
 	private class SaveAndUploadThread extends Thread {		
 		ArrayList<Location> newLocs = new ArrayList<Location>();
 		private boolean doneSignalReceived = false;
 		
 		
 		@Override
 		public void run() {
 			while (!doneSignalReceived) {
 				waitFullBatchOrDone();
 				uploadOrSaveNewLocs();											
 			}		
 			db.closeDB();
 		}
 		
 		private void waitFullBatchOrDone() {			
 			do {				
 				try {
 					Location loc = locationsToSave.take();
					if (loc == null) {
 						doneSignalReceived = true;
 						break;
 					} else {
 						newLocs.add(loc);
 					}		
 				} catch (InterruptedException e) {
 					Log.e(TAG, e.toString(), e);
 				}
 			} while (newLocs.size() < SAVE_BATCH_SIZE);
 		}		
 
 		private void uploadOrSaveNewLocs() {
 			if (!server.isNetworkAvailable()) {
 				saveNewLocsToDB();
 			} else {				
 				Collection<Location> toUpload = db.getSavedLocations();
 				boolean hadSavedLocations = toUpload.size() > 0;
 				toUpload.addAll(newLocs);
 				if (server.uploadLocations(toUpload)) {
 					newLocs.clear();
 					if (hadSavedLocations) {
 						db.clearSavedLocations();
 					}
 				} else {
 					saveNewLocsToDB();
 				}
 			}
 		}
 		
 		private void saveNewLocsToDB() {
 			for (Location l : newLocs) {
 				db.saveLocation(l);
 			}
 			newLocs.clear();
 		}
 	}
 }
