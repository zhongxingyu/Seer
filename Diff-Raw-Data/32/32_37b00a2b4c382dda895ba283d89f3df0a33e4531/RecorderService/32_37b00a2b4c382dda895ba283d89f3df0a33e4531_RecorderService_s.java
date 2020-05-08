 package com.bbaumgar.signalstrengthplotter;
 
 import com.bbaumgar.net.PostCallback;
 import com.bbaumgar.net.ServerAPI;
 
 import android.annotation.SuppressLint;
 import android.app.Notification;
 import android.support.v4.app.NotificationCompat;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.content.Context;
 import android.content.Intent;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.Binder;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.telephony.PhoneStateListener;
 import android.telephony.SignalStrength;
 import android.telephony.TelephonyManager;
 import android.util.Log;
 import android.widget.Toast;
 
 public class RecorderService extends Service {
 	private NotificationManager mNM;
 	private LocationListener locationListener;
 	private LocationManager locationManager;
 	private TelephonyManager teleManager;
 	private SignalListener teleListener;
 	private ConnectivityManager connManager;
 	private ServerAPI server;
 	protected int signal = -1;
 	private double lastLat, lastLon;
 	String TAG = "Recorder Service";
 	private int numberOfSends = 0;
 
 	// Unique Identification Number for the Notification.
 	// We use it on Notification start, and to cancel it.
 	private int NOTIFICATION = 200;// R.string.local_service_started;
 
 	@Override
 	public void onCreate() {
 		mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
 
 		locationListener = new LocationChangeListener();
 		locationManager = (LocationManager) this
 				.getSystemService(LOCATION_SERVICE);
 		locationManager.requestLocationUpdates(
 				LocationManager.NETWORK_PROVIDER, 50, 0, locationListener);
 
 		teleListener = new SignalListener();
 		teleManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
 		teleManager.listen(teleListener,
 				PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
 		
 		connManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
 		server = ServerAPI.getInstance();
 
 		showNotification();
 
 		// Tell the user about this for our demo.
 		Toast.makeText(this, "Started Collecting Data!", Toast.LENGTH_SHORT)
 				.show();
 	}
 
 	/**
 	 * Class for clients to access. Because we know this service always runs in
 	 * the same process as its clients, we don't need to deal with IPC.
 	 */
 	public class RecorderBinder extends Binder {
 		RecorderService getService() {
 			return RecorderService.this;
 		}
 	}
 
 	@Override
 	public int onStartCommand(Intent intent, int flags, int startId) {
 		Log.i("LocalService", "Received start id " + startId + ": " + intent);
 		locationManager.requestLocationUpdates(
 				LocationManager.NETWORK_PROVIDER, 100000, 20, locationListener);
 		teleManager.listen(teleListener,
 				PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
 
 		// We want this service to continue running until it is explicitly
 		// stopped, so return sticky.
 		return START_STICKY;
 	}
 
 	@Override
 	public void onDestroy() {
 		// Cancel the persistent notification.
 		mNM.cancel(NOTIFICATION);
 
 		// Tell the user we stopped.
 		Toast.makeText(this, "Stopped Collecting Data", Toast.LENGTH_SHORT)
 				.show();
 	}
 
 	@Override
 	public IBinder onBind(Intent intent) {
 		return mBinder;
 	}
 
 	// This is the object that receives interactions from clients. See
 	// RemoteService for a more complete example.
 	private final IBinder mBinder = new RecorderBinder();
 
 	/**
 	 * Show a notification while this service is running.
 	 */
 	private void showNotification() {
 		Toast.makeText(this, "Started Collecting Data!", Toast.LENGTH_SHORT)
 				.show();
 		 // In this sample, we'll use the same text for the ticker and the
 		 // expanded notification CharSequence text =
 		
 		 // The PendingIntent to launch our activity if the user selects this
 		 // notification 
 		 PendingIntent contentIntent =
 		 PendingIntent.getActivity(this, 0, new Intent(this,
 		 PlotActivity.class), 0);
 		
 		 Notification noti = new NotificationCompat.Builder(getBaseContext())
          .setContentTitle("Collecting Data")
          .setContentText("Readings sent: "+numberOfSends)
          .setSmallIcon(R.drawable.ic_launcher)
          .setContentIntent(contentIntent)
          .build();
 		 
 		 // Send the notification. 
 		 mNM.notify(NOTIFICATION, noti);
 		
 	}
 
 	private void logData() {
 			Log.v(TAG, String.format(
 					"Lat:\t %fLong:\t %fSignal Strength:\t %d",
 					(float) lastLat, (float) lastLon, signal));
 			NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
 			NetworkInfo mData = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
 			if (mWifi.isConnected() || mData.isConnected()) {
 				server.sendLocationData(lastLat, lastLon, signal,
 						new SendDataCallback());
 				numberOfSends++;
 				showNotification();
 			}
 	}
 
 	public class LocationChangeListener implements LocationListener {
 
 		@Override
 		public void onLocationChanged(Location location) { // <9>
 			Log.v(TAG, "Location Changed!");
 			lastLon = location.getLongitude();
 			lastLat = location.getLatitude();
 			logData();
 		}
 
 		@Override
 		public void onProviderDisabled(String arg0) {
 
 		}
 
 		@Override
 		public void onProviderEnabled(String arg0) {
 
 		}
 
 		@Override
 		public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
 
 		}
 	}
 
 	private class SignalListener extends PhoneStateListener {
 		@Override
 		public void onSignalStrengthsChanged(SignalStrength signalStrength) {
 			Log.v(TAG, "Signal Strength Changed!");
 			super.onSignalStrengthsChanged(signalStrength);
 			signal = parseSignalStrength(signalStrength);
 		}
 	}
 
 	public class SendDataCallback extends PostCallback {
 		public void onDataReceived(String data) {
 		}
 	}
 
 	public int parseSignalStrength(SignalStrength signalStrength) {
		if (signalStrength.isGsm()) {
			if (signalStrength.getGsmSignalStrength() != 99)
				Toast.makeText(this, "Your phone is not supported.",
						Toast.LENGTH_SHORT).show();
		} else {
 			final int snr = signalStrength.getEvdoSnr();
 			final int cdmaDbm = signalStrength.getCdmaDbm();
 			final int cdmaEcio = signalStrength.getCdmaEcio();
 			int levelDbm;
 			int levelEcio;
 			int level = 0;
 
 			if (snr == -1) {
 				if (cdmaDbm >= -75)
 					levelDbm = 4;
 				else if (cdmaDbm >= -85)
 					levelDbm = 3;
 				else if (cdmaDbm >= -95)
 					levelDbm = 2;
 				else if (cdmaDbm >= -100)
 					levelDbm = 1;
 				else
 					levelDbm = 0;
 
 				// Ec/Io are in dB*10
 				if (cdmaEcio >= -90)
 					levelEcio = 4;
 				else if (cdmaEcio >= -110)
 					levelEcio = 3;
 				else if (cdmaEcio >= -130)
 					levelEcio = 2;
 				else if (cdmaEcio >= -150)
 					levelEcio = 1;
 				else
 					levelEcio = 0;
 
 				level = (levelDbm < levelEcio) ? levelDbm : levelEcio;
 			} else {
 				if (snr == 7 || snr == 8)
 					level = 4;
 				else if (snr == 5 || snr == 6)
 					level = 3;
 				else if (snr == 3 || snr == 4)
 					level = 2;
 				else if (snr == 1 || snr == 2)
 					level = 1;
 			}
 			return level;
 		}
		return -1;
 	}
 
 }
