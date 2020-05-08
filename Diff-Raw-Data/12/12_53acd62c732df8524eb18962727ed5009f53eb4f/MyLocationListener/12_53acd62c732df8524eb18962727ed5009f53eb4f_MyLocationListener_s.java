 package com.pwr.zpi.listeners;
 
 import java.util.ArrayList;
 
 import android.app.PendingIntent;
 import android.app.Service;
 import android.content.Intent;
 import android.location.GpsStatus;
 import android.location.Location;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.IBinder;
 import android.os.Message;
 import android.os.Messenger;
 import android.os.SystemClock;
 import android.support.v4.content.LocalBroadcastManager;
 import android.util.Log;
 
 import com.google.android.gms.common.ConnectionResult;
 import com.google.android.gms.common.GooglePlayServicesClient;
 import com.google.android.gms.location.LocationClient;
 import com.google.android.gms.location.LocationListener;
 import com.google.android.gms.location.LocationRequest;
 import com.google.android.gms.maps.GoogleMap;
 import com.pwr.zpi.MainScreenActivity;
 
 public class MyLocationListener extends Service implements LocationListener,
 GooglePlayServicesClient.ConnectionCallbacks,
 GooglePlayServicesClient.OnConnectionFailedListener,
 android.location.GpsStatus.Listener {
 	
 	// private ActivityActivity activityActivity;
 	// private MainScreenActivity mainScreenActivity;
 	
 	private LocationClient mLocationClient;
 	private LocationRequest mLocationRequest;
 	private static final long LOCATION_UPDATE_FREQUENCY = 1000;
 	private static final long MAX_UPDATE_TIME = 5000;
 	public static final int REQUIRED_ACCURACY = 300; // in meters //TODO change
 	private GoogleMap mMap;
 	private boolean isStarted;
 	private boolean isPaused;
 	private boolean isGpsFix;
 	private boolean isConnected;
 	private Location mLastRecordedLocation;
 	private long mLastRecordedLocationTime;
 	// private Location mLastLocation;
 	
 	// Service data
 	ArrayList<Messenger> mClients = new ArrayList<Messenger>();
 	public static final int MSG_REGISTER_CLIENT = 1;
 	public static final int MSG_UNREGISTER_CLIENT = 2;
 	public static final int MSG_SEND_LOCATION = 3;
 	public static final int MSG_ASK_FOR_GPS = 4;
 	public static final int MSG_SHOW_GOOGLE_SERVICES_DIALOG = 5;
 	public static final String MESSAGE = "Message";
 	public static final int MESSAGE_FROM_MAIN_SCREEN = 1;
 	public static final int MESSAGE_FROM_ACTIVITY = 2;
 	final Messenger mMessenger = new Messenger(new IncomingHandler());
 	
 	private int checkGPS() {
 		
 		LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
 		service.addGpsStatusListener(this);
 		short gpsStatus = 0;
 		
 		boolean enabled = service
 			.isProviderEnabled(LocationManager.GPS_PROVIDER);
 		
 		if (!enabled) {
 			gpsStatus = MainScreenActivity.GPS_NOT_ENABLED;
 		}
 		else if (isConnected
 			&& (mLastRecordedLocation == null || mLastRecordedLocation
 			.getAccuracy() > MyLocationListener.REQUIRED_ACCURACY)) {
 			gpsStatus = MainScreenActivity.NO_GPS_SIGNAL;
 		}
 		else {
 			gpsStatus = MainScreenActivity.GPS_WORKING;
 		}
 		
 		return gpsStatus;
 	}
 	
 	// LOCATION METHODS FOR THE LISTENERS
 	@Override
 	public void onLocationChanged(Location location) {
 		
 		sendLocationToClients(location);
 		mLastRecordedLocationTime = SystemClock.elapsedRealtime();
 		mLastRecordedLocation = location;
 	}
 	
 	@Override
 	public void onConnected(Bundle bundle) {
 		Log.i("Location_info", "onConnected");
 		mLocationClient.requestLocationUpdates(mLocationRequest, this);
 		isConnected = true;
 	}
 	
 	@Override
 	public void onDisconnected() {
 		Log.i("Location_info", "onDisconnected");
 		isConnected = false;
 	}
 	
 	@Override
 	public void onConnectionFailed(ConnectionResult connectionResult) {
 		Log.i("Location_info", "onConnectionFailed");
 		
 		Intent intent = new Intent(MyLocationListener.class.getSimpleName());
 		int statusCode = connectionResult.getErrorCode();
 		PendingIntent pendingIntent = connectionResult.getResolution();
 		// Add data
 		intent.putExtra(MESSAGE, MSG_SHOW_GOOGLE_SERVICES_DIALOG);
 		intent.putExtra("pending_intent", connectionResult.getResolution());
 		intent.putExtra("status_code", statusCode);
 		LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
 		
 	}
 	
 	@Override
 	public void onGpsStatusChanged(int event) {
 		
 		switch (event) {
 			case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
 				if (mLastRecordedLocation != null) {
 					isGpsFix = (SystemClock.elapsedRealtime() - mLastRecordedLocationTime) < MAX_UPDATE_TIME;
 				}
 				
 				break;
 			case GpsStatus.GPS_EVENT_FIRST_FIX:
 				isGpsFix = true;
 				break;
 		}
 		
 	}
 	
 	// SERVICE METHODS
 	
 	@Override
 	public IBinder onBind(Intent intent) {
 		Log.i("Service_info", "onBind");
 		return mMessenger.getBinder();
 	}
 	
 	/**
 	 * Handler of incoming messages from clients.
 	 */
 	class IncomingHandler extends Handler {
 		@Override
 		public void handleMessage(Message msg) {
 			switch (msg.what) {
 				case MSG_REGISTER_CLIENT:
 					mClients.add(msg.replyTo);
 					break;
 				case MSG_UNREGISTER_CLIENT:
 					mClients.remove(msg.replyTo);
 					break;
 				case MyLocationListener.MSG_ASK_FOR_GPS:
 					Log.i("Service_info", "Service: asked for gps signal");
 					int gpsStatus = checkGPS();
 					
 					Intent intent = new Intent(
 						MyLocationListener.class.getSimpleName());
 					// Add data
 					intent.putExtra(MESSAGE, MSG_ASK_FOR_GPS);
 					intent.putExtra("gpsStatus", gpsStatus);
 					LocalBroadcastManager.getInstance(MyLocationListener.this)
 					.sendBroadcast(intent);
 					break;
 				default:
 					super.handleMessage(msg);
 			}
 		}
 	}
 	
 	private void sendLocationToClients(Location location) {
 		Log.i("Service_info", "Service is sending location");
 		
 		Intent intent = new Intent(MyLocationListener.class.getSimpleName());
 		// Add data
 		intent.putExtra(MESSAGE, MSG_SEND_LOCATION);
 		intent.putExtra("Location", location);
 		LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
 		
 	}
 	
 	@Override
 	public void onCreate() {
 		Log.i("Service_info", "Service created");
 		mLocationClient = new LocationClient(getApplicationContext(), this,
 			this);
 		mLocationRequest = LocationRequest.create();
 		mLocationRequest.setInterval(LOCATION_UPDATE_FREQUENCY);
 		mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
 		isStarted = false;
 		isPaused = false;
 		isGpsFix = false;
 		isConnected = false;
 		mLocationClient.connect();
 		super.onCreate();
 	}
 	
 	@Override
 	public boolean onUnbind(Intent intent) {
 		Log.i("Service_info", "Service Unbind");
 		return super.onUnbind(intent);
 	}
 	
 	@Override
 	public void onDestroy() {
 		Log.i("Service_info", "Service Destroyed");
 		
 		if (mLocationClient.isConnected()) {
 			mLocationClient.removeLocationUpdates(this);
 		}
 		
 		mLocationClient.disconnect();
 		super.onDestroy();
 	}
 	
 	@Override
 	public int onStartCommand(Intent intent, int flags, int startId) {
 		return Service.START_STICKY;
 	}
 	
 }
