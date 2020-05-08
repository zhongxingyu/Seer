 package com.denolk.locationtrack;
 
 import java.util.Timer;
 import java.util.TimerTask;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.impl.client.DefaultHttpClient;
 
 import android.content.Context;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.net.wifi.WifiInfo;
 import android.net.wifi.WifiManager;
 import android.os.Bundle;
 import android.util.Log;
 
 public class LocationWatcherSingleton implements LocationListener {
 
 	private enum UPLOAD_MODE {
 		USE_IMMEDIATE(1), USE_TIMER(2);
 
 		private int type;
 
 		UPLOAD_MODE(int value) {
 			this.type = value;
 		}
 
 		public int getValue() {
 			return type;
 		}
 	}
 
	private int _uploadMode = UPLOAD_MODE.USE_TIMER.getValue();
	private int TIMER_INTERVAL = 5000;
	private String _url = "http://10.0.0.14:1188/alive";
 	private String _macAddress;
 	private Location _location;
 	private Timer _locationUploader;
 	private Context _context;
 	private LocationManager _locationManager;
 	private static LocationWatcherSingleton _instance;
 
 	public static LocationWatcherSingleton getInstance(Context context) {
 		if (_instance == null) {
 			_instance = new LocationWatcherSingleton(context);
 		}
 		return _instance;
 	}
 
 	private LocationWatcherSingleton() {
 	}
 
 	public void startListening() {
 		initializeLocationManager();
 
 		if (_uploadMode == UPLOAD_MODE.USE_TIMER.getValue()) {
 			initializeTimer();
 		}
 		Log.i("LocationWatcher", "started listening location changes");
 	}
 
 	private LocationWatcherSingleton(Context context) {
 		this._context = context;
 
 		WifiManager wifiManager = (WifiManager) this._context
 				.getSystemService(Context.WIFI_SERVICE);
 		WifiInfo wInfo = wifiManager.getConnectionInfo();
 		this._macAddress = wInfo.getMacAddress();
 
 	}
 
 	private void initializeLocationManager() {
 		if (this._locationManager == null) {
 			this._locationManager = (LocationManager) this._context
 					.getSystemService(Context.LOCATION_SERVICE);
 			Log.i("LocationWatcher", "location manager initialized");
 		}
 		this._locationManager.requestLocationUpdates(
 				LocationManager.GPS_PROVIDER, 1000, 1, this);
 	}
 
 	private void initializeTimer() {
 		if (this._locationUploader == null) {
 			this._locationUploader = new Timer();
 		}
 		this._locationUploader.scheduleAtFixedRate(new TimerTask() {
 			@Override
 			public void run() {
 				_instance.postData(_location);
 			}
 		}, 1, TIMER_INTERVAL);
 		Log.i("LocationWatcher", "location uploader timer initialized");
 	}
 
 	private void postData(Location location) {
		if(location==null){
 			Log.i("LocationWatcher", "location is null, will not upload.");
 			return;
 		}
		
 		String data = String.format(
 				"{\"device\":\"%s\",\"lat\":\"%s\",\"long\":\"%s\"}",
 				this._macAddress, location.getLatitude(),
 				location.getLongitude());
		Log.i("LocationWatcher", "uploading location "+ data);
 		try {
 			HttpClient client = new DefaultHttpClient();
 			HttpPost post = new HttpPost(this._url);
 			post.setEntity(new StringEntity(data));
 			post.setHeader("content-type", "application/json");
 			HttpResponse response = client.execute(post);
			Log.i("LocationWatcher", "uploaded "+ response.toString());
 		} catch (Exception ex) {
 			Log.e("LocationWatcher", ex.toString());
 		}
 	}
 
 	@Override
 	public void onLocationChanged(Location location) {
 		this._location = location;
 		Log.i("LocationWatcher", "location update");
 		if (this._uploadMode == UPLOAD_MODE.USE_IMMEDIATE.getValue()) {
 			this.postData(this._location);
 		}
 	}
 
 	@Override
 	public void onProviderDisabled(String provider) {
 
 	}
 
 	@Override
 	public void onProviderEnabled(String provider) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void onStatusChanged(String provider, int status, Bundle extras) {
 		// TODO Auto-generated method stub
 
 	}
 
 }
