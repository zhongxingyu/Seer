 package se.chalmers.dat255.sleepfighter.service;
 
 import java.io.IOException;
 
 import se.chalmers.dat255.sleepfighter.SFApplication;
 import se.chalmers.dat255.sleepfighter.speech.WeatherDataFetcher;
 import se.chalmers.dat255.sleepfighter.utils.debug.Debug;
 
 import com.google.android.gms.common.ConnectionResult;
 import com.google.android.gms.common.GooglePlayServicesClient;
 import com.google.android.gms.location.LocationClient;
 import com.google.android.gms.location.LocationListener;
 import com.google.android.gms.location.LocationRequest;
 
 import android.app.Service;
 import android.content.Intent;
 import android.location.Location;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.IBinder;
 
 /*
  * Retrieves the current location, then fetches the weather of that location, and then shuts down. 
  */
 public class LocationService extends Service implements GooglePlayServicesClient.ConnectionCallbacks, GooglePlayServicesClient.OnConnectionFailedListener, LocationListener
 {
 	private LocationClient locationClient;
 
 	@Override
 	public void onCreate() {
 		super.onCreate();
 		locationClient = new LocationClient(this, this, this);
 		Debug.d("LocationService oncreate");
 	}
 
 	@Override
 	public int onStartCommand(Intent intent, int flags, int startId) {
 		Debug.d("LocationService startcommand");
 		
 		locationClient.connect();
 		return Service.START_STICKY;
 	}
 
 	@Override
 	public void onDestroy(){
 		super.onDestroy();
 
 		if(locationClient != null && locationClient.isConnected()) {
 			locationClient.removeLocationUpdates(this);
 			locationClient.disconnect();
 		}
 	}
 
 	
 	@Override
 	public void onConnected(Bundle arg0) {
 		
 		Debug.d("LocationService onconnected");
 
 		Long time = 60*1000L;
 
 		LocationRequest locationRequest = LocationRequest.create();
 		locationRequest.setPriority(LocationRequest.PRIORITY_LOW_POWER);
 		locationRequest.setInterval(time);
 		locationRequest.setFastestInterval(time);
 
 		locationClient.requestLocationUpdates(locationRequest, this);
 	}
 
 	@Override
 	public void onLocationChanged(Location location) {
 		Debug.d("LocationService onLocationChanged");
 		this.fetchWeatherData(location);	
 		this.stopSelf();
 	}
 	
 
 	/*
 	 * Called by Location Services if the attempt to Location Services fails.
 	 */
 	@Override
 	public void onConnectionFailed(ConnectionResult connectionResult) {
 		/*
 		 * Google Play services can resolve some errors it detects. If the error
 		 * has a resolution, try sending an Intent to start a Google Play
 		 * services activity that can resolve error.
 		 */
 		if (connectionResult.hasResolution()) {
 
 			Debug.d("Connection failed we are dead.");
 
 		} else {
 			/*
 			 * If no resolution is available, display a dialog to the user with
 			 * the error.
 			 */
 			
 			
 			Debug.d("Connection failed with no resolution. Error code:" + connectionResult.getErrorCode());
 
 		}
 	}
 
 	/*
 	 * Called by Location Services if the connection to the location client
 	 * drops because of an error.
 	 */
 	@Override
 	public void onDisconnected() {
 		// Display the connection status
 		Debug.d("Disconnected. Please re-connect.");
 	}
 
 
 	@Override
 	public IBinder onBind(Intent arg0) {
 		Debug.d("LocationService onBind");
 		return null;
 	}
 	
	private void fetchWeatherData(Location location) {
 		Debug.d("about to execute WeatherDataTask");
 
 		new WeatherDataTask().execute(location.getLatitude(),
 				location.getLongitude());
 	}
 	
	private static class WeatherDataTask extends AsyncTask<Double, Void, WeatherDataFetcher> {
 
 		protected void onPostExecute(WeatherDataFetcher weather) {
 			Debug.d("done loading url");
 			
 			SFApplication app = SFApplication.get();
 			if(weather == null) {
 				app.setWeather(null);
 			} else {
 				app.setWeather(weather.getSummary());
 			}
 		}
 
 		@Override
 		protected WeatherDataFetcher doInBackground(Double... params) {
 			Debug.d("now executing weather data task");
 
 			try {
 				return new WeatherDataFetcher(params[0], params[1]);
 			} catch (IOException e) {
 				// If we couldn't connect we'll have to do without the weather.
 				return null;		
 			}
 		}
 	}
 }
