 package com.alexkorovyansky.carwashfor.services;
 
 import android.app.Service;
 import android.appwidget.AppWidgetManager;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.location.Location;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.util.Log;
 import android.widget.Toast;
 
import com.alexkorovyansky.carwashfor.BuildConfig;
 import com.alexkorovyansky.carwashfor.ForecasterAppWidgetProvider;
 import com.alexkorovyansky.carwashfor.R;
 import com.alexkorovyansky.carwashfor.data.ForecastStorage;
 import com.google.android.gms.common.ConnectionResult;
 import com.google.android.gms.common.GooglePlayServicesClient;
 import com.google.android.gms.location.LocationClient;
 import com.google.android.gms.location.LocationListener;
 import com.google.android.gms.location.LocationRequest;
 
 import java.util.Arrays;
 import java.util.Date;
 
 import retrofit.Callback;
 import retrofit.RestAdapter;
 import retrofit.RetrofitError;
 import retrofit.client.Response;
 
 /**
  * @author Alex Korovyansky (korovyansk@gmail.com)
  */
 public class ForecastIntentService extends Service {
 
     public static final String TAG = ForecastIntentService.class.getSimpleName();
 
     private RestAdapter mForecastRestAdapter;
     private ForecastRestService mForecastRestService;
     private ForecastStorage mForecastStorage;
     private LocationClient mLocationClient;
 
     @Override
     public void onCreate() {
         super.onCreate();
         Log.v(TAG, "onCreate");
         mForecastRestAdapter = new RestAdapter.Builder()
                 .setServer("http://api.openweathermap.org")
                .setDebug(BuildConfig.DEBUG)
                 .build();
         mForecastRestService = mForecastRestAdapter.create(ForecastRestService.class);
 
         mForecastStorage = ForecastStorage.load(this);
 
         mLocationClient = new LocationClient(this, new GooglePlayServicesClient.ConnectionCallbacks() {
             @Override
             public void onConnected(Bundle bundle) {
                 Log.v(TAG, "onConnected");
                 doStuff();
             }
 
             @Override
             public void onDisconnected() {
                 Log.v(TAG, "onDisconnected");
             }
         }, new GooglePlayServicesClient.OnConnectionFailedListener() {
             @Override
             public void onConnectionFailed(ConnectionResult connectionResult) {
                 Log.v(TAG, "onConnectionFailed");
                 showToast(R.string.error_google_play_services);
                 stopSelf();
 ;            }
         });
 
         mLocationClient.connect();
 
         mForecastStorage.inProgress = true;
         mForecastStorage.save();
         updateAllWidgets();
     }
 
     @Override
     public IBinder onBind(Intent intent) {
         return null;
     }
 
     @Override
     public void onDestroy() {
         super.onDestroy();
         Log.v(TAG, "onDestroy");
         if (mLocationClient.isConnected()) {
             mLocationClient.disconnect();
         }
         mForecastStorage.inProgress = false;
         mForecastStorage.save();
         updateAllWidgets();
     }
 
     private void doStuff(){
         final Location lastKnownLocation = mLocationClient.getLastLocation();
         final long now = new Date().getTime();
         final long lastKnownLocationTime = lastKnownLocation == null ? -1 : lastKnownLocation.getTime();
         final long oneHourMs = 1000 * 60 * 60;
         if (now - lastKnownLocationTime < oneHourMs) {
             Log.v(TAG, "doStuff --> location isFresh " + lastKnownLocation);
             getForecastForLocation(lastKnownLocation);
         } else {
             if (isLocationSourcesAvailable()) {
                 Log.v(TAG, "doStuff --> will ask location");
                 final LocationRequest request = LocationRequest.create()
                         .setNumUpdates(1);
                 final LocationListener listener = new LocationListener() {
                     @Override
                     public void onLocationChanged(Location location) {
                         Log.v(TAG, "doStuff --> got location");
                         getForecastForLocation(location);
                     }
                 };
                 mLocationClient.requestLocationUpdates(request, listener);
             } else{
                 Log.v(TAG, "doStuff --> location sources are unavailable");
                 showToast(R.string.error_location);
                 stopSelf();
             }
         }
     }
 
     private boolean isLocationSourcesAvailable(){
         final LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
         return isLocationProviderAvailable(locationManager, LocationManager.GPS_PROVIDER)
                 || isLocationProviderAvailable(locationManager, LocationManager.NETWORK_PROVIDER)
                 || isLocationProviderAvailable(locationManager, LocationManager.PASSIVE_PROVIDER);
     }
 
     private boolean isLocationProviderAvailable(LocationManager locationManager, String provider){
         try{
             if (locationManager.isProviderEnabled(provider)) {
                 return true;
             }
         } catch (Exception e){
             //workaround for Android Emulator
         }
         return false;
     }
     private void getForecastForLocation(Location location){
         mForecastRestService.getForecast(
                 location.getLatitude(), location.getLongitude(),
 
                 new Callback<ForecastRestService.ForecastResponse>(){
                     @Override
                     public void success(ForecastRestService.ForecastResponse response, Response rawResponse) {
                         if (response.code == 200) {
                            Log.v(TAG, "getForecastForLocation -> response is ok");
                             mForecastStorage.forecastValue = calculateForecast(response);
                             mForecastStorage.timestamp = new Date().getTime();
                             mForecastStorage.save();
                        } else {
                            Log.v(TAG, "getForecastForLocation -> bad response code == " + response.code);
                         }
                         stopSelf();
                     }
 
                     @Override
                     public void failure(RetrofitError retrofitError) {
                        Log.v(TAG, "getForecastForLocation -> failure " + retrofitError);
                         showToast(R.string.error_network);
                         stopSelf();
                     }
                 });
     }
     private float calculateForecast(ForecastRestService.ForecastResponse response){
         final float[] rainsArray = ForecastAlgorithms.calculateRainsArray(response);
         Log.v(TAG, "calculateForecast --> rainsArray = " + Arrays.toString(rainsArray));
         final float forecast = ForecastAlgorithms.makeForecast(rainsArray);
         Log.v(TAG, "calculateForecast --> forecast = " + forecast);
         return forecast;
     }
 
     private void updateAllWidgets() {
         final AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getApplicationContext());
         int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this, ForecasterAppWidgetProvider.class));
 
         ForecasterAppWidgetProvider.updateAllWidgets(this, appWidgetManager, appWidgetIds);
     }
 
     private void showToast(int messageId){
         final String message = this.getResources().getString(messageId);
         Log.w(TAG, message);
         Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
     }
 }
