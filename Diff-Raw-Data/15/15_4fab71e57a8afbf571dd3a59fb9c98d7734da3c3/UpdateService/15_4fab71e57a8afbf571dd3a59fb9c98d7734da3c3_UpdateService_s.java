 package com.boh.flatmate;
 
 import java.text.DecimalFormat;
 
 import com.boh.flatmate.FlatMate.ConnectionExchanger;
 import com.boh.flatmate.FlatMate.FlatDataExchanger;
 import com.boh.flatmate.MapFragment.getNearShops;
 
 import android.app.NotificationManager;
 import android.app.Service;
 import android.content.Context;
 import android.content.Intent;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.location.LocationProvider;
 import android.os.Binder;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.widget.Toast;
 
 public class UpdateService extends Service {
 
 	private final DecimalFormat sevenSigDigits = new DecimalFormat("0.#######");
 
 	private LocationManager lm;
 	private LocationListener locationListener;
 
	//private static long minTimeMillis = 600000; //10Minutes
	private static long minTimeMillis = 1000; //10 Seconds
 	private static long minDistanceMeters = 100;
 	private static float minAccuracyMeters = 1000;
 
 	private int lastStatus = 0;
 	private static boolean showingDebugToast = false;
 
 	/** Called when the activity is first created. */
 	private void startLoggerService() {
 
 		// ---use the LocationManager class to obtain GPS locations---
 		lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
 
 		locationListener = new MyLocationListener();
 		lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, minTimeMillis, minDistanceMeters, locationListener);
 		/*lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 
 				minTimeMillis, 
 				minDistanceMeters,
 				locationListener);
 				
 			*/
 	}
 
 	private void shutdownLoggerService() {
 		lm.removeUpdates(locationListener);
 	}
 
 	public class MyLocationListener implements LocationListener {
 		public void onLocationChanged(Location loc) {
 			if (loc != null) {
 				boolean pointIsRecorded = false;
 				System.out.println(loc.getAccuracy());
 					if (loc.hasAccuracy() && loc.getAccuracy() <= minAccuracyMeters) {
 						pointIsRecorded = true;
 
 						try{
 						FlatDataExchanger.flatData.getCurrentUser().setGeocode_lat((float)loc.getLatitude());
 						FlatDataExchanger.flatData.getCurrentUser().setGeocode_long((float)loc.getLongitude());
 						System.out.println(FlatDataExchanger.flatData.getCurrentUser().getGeocode_lat());
 						System.out.println(FlatDataExchanger.flatData.getCurrentUser().getGeocode_long());
 						
 						ConnectionExchanger.connection.updateUser(FlatDataExchanger.flatData.getCurrentUser());
 						}catch(Exception e){}
 						
 					}
 				if (pointIsRecorded) {
 					if (showingDebugToast) Toast.makeText(
 							getBaseContext(),
 							"Location stored: \nLat: " + sevenSigDigits.format(loc.getLatitude())
 							+ " \nLon: " + sevenSigDigits.format(loc.getLongitude())
 							+ " \nAlt: " + (loc.hasAltitude() ? loc.getAltitude()+"m":"?")
 							+ " \nAcc: " + (loc.hasAccuracy() ? loc.getAccuracy()+"m":"?"),
 							Toast.LENGTH_SHORT).show();
 				} else {
 					if (showingDebugToast) Toast.makeText(
 							getBaseContext(),
 							"Location not accurate enough: \nLat: " + sevenSigDigits.format(loc.getLatitude())
 							+ " \nLon: " + sevenSigDigits.format(loc.getLongitude())
 							+ " \nAlt: " + (loc.hasAltitude() ? loc.getAltitude()+"m":"?")
 							+ " \nAcc: " + (loc.hasAccuracy() ? loc.getAccuracy()+"m":"?"),
 							Toast.LENGTH_SHORT).show();
 				}
 			}
 		}
 
 		public void onProviderDisabled(String provider) {
 			if (showingDebugToast) Toast.makeText(getBaseContext(), "onProviderDisabled: " + provider,
 					Toast.LENGTH_SHORT).show();
 
 		}
 
 		public void onProviderEnabled(String provider) {
 			if (showingDebugToast) Toast.makeText(getBaseContext(), "onProviderEnabled: " + provider,
 					Toast.LENGTH_SHORT).show();
 
 		}
 
 		public void onStatusChanged(String provider, int status, Bundle extras) {
 			String showStatus = null;
 			if (status == LocationProvider.AVAILABLE)
 				showStatus = "Available";
 			if (status == LocationProvider.TEMPORARILY_UNAVAILABLE)
 				showStatus = "Temporarily Unavailable";
 			if (status == LocationProvider.OUT_OF_SERVICE)
 				showStatus = "Out of Service";
 			if (status != lastStatus && showingDebugToast) {
 				Toast.makeText(getBaseContext(),
 						"new status: " + showStatus,
 						Toast.LENGTH_SHORT).show();
 			}
 			lastStatus = status;
 		}
 
 	}
 
 	// Below is the service framework methods
 
 	private NotificationManager mNM;
 
 	@Override
 	public void onCreate() {
 		super.onCreate();
 		mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
 
 		startLoggerService();
 
 		// Display a notification about us starting. We put an icon in the
 		// status bar.
 		//showNotification();
 		System.out.println("UpdateService onCreate");
 	}
 
 	@Override
 	public void onDestroy() {
 		super.onDestroy();
 
 		shutdownLoggerService();
 
 		// Cancel the persistent notification.
 		mNM.cancel("LocationService has stopped",0);
 
 		// Tell the user we stopped.
 		Toast.makeText(this, "LocationService had stopped",
 				Toast.LENGTH_SHORT).show();
 	}
 
 	/**
 	 * Show a notification while this service is running.
 	 */
 	private void showNotification() {
 		Toast.makeText(this, "LocationService had started",
 				Toast.LENGTH_SHORT).show();
 	}
 
 	// This is the object that receives interactions from clients. See
 	// RemoteService for a more complete example.
 	private final IBinder mBinder = new LocalBinder();
 
 	@Override
 	public IBinder onBind(Intent intent) {
 		return mBinder;
 	}
 
 	public static void setMinTimeMillis(long _minTimeMillis) {
 		minTimeMillis = _minTimeMillis;
 	}
 
 	public static long getMinTimeMillis() {
 		return minTimeMillis;
 	}
 
 	public static void setMinDistanceMeters(long _minDistanceMeters) {
 		minDistanceMeters = _minDistanceMeters;
 	}
 
 	public static long getMinDistanceMeters() {
 		return minDistanceMeters;
 	}
 
 	public static float getMinAccuracyMeters() {
 		return minAccuracyMeters;
 	}
 
 	public static void setMinAccuracyMeters(float minAccuracyMeters) {
 		UpdateService.minAccuracyMeters = minAccuracyMeters;
 	}
 
 	public static void setShowingDebugToast(boolean showingDebugToast) {
 		UpdateService.showingDebugToast = showingDebugToast;
 	}
 
 	public static boolean isShowingDebugToast() {
 		return showingDebugToast;
 	}
 
 	/**
 	 * Class for clients to access. Because we know this service always runs in
 	 * the same process as its clients, we don't need to deal with IPC.
 	 */
 	public class LocalBinder extends Binder {
 		UpdateService getService() {
 			return UpdateService.this;
 		}
 	}
 
 }
