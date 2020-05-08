 package be.iminds.mix.streamstore;
 
 import android.content.Context;
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.hardware.SensorEventListener;
 import android.hardware.SensorManager;
 import android.location.Address;
 import android.location.Geocoder;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.util.Log;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Locale;
 
 
 /**
  * Created by matthias on 30/05/13. gathers sensor data
  */
 public class SensorData {
     private static final int TWO_MINUTES = 1000 * 60 * 2;
     String sensorData;
     Location myLocation;
     LocationManager locationManager;
     List<LocationListener> locationListeners;
     Geocoder geoCoder;
     Address address1;
     float[] linear_acceleration;
     float light;
     // this can be called from javascript
 //    @JavascriptInterface -> not anymore since we use the user agent
     public String toString(){
         String[] address = {"", "", "", "", ""};
         if(address1!=null){
             if(address1.getAddressLine(0) != null) address[0] = address1.getAddressLine(0);
             if(address1.getPostalCode() != null) address[1] = address1.getPostalCode();
             if(address1.getSubLocality() != null) address[2] = address1.getSubLocality();
             if(address1.getLocality() != null) address[3] = address1.getLocality();
             if(address1.getCountryName() != null) address[4] = address1.getCountryName();
         }
         String accelString = "{\"x\":"+linear_acceleration[0]+",\"y\":"+linear_acceleration[1]+",\"z\":"+linear_acceleration[2]+"}";
         if(myLocation != null){
             sensorData =
                     "\"location\":{\"provider\":\""+myLocation.getProvider()+"\","+
                                 "\"time\":" + myLocation.getTime()+","+
                                 "\"latitude\":" + myLocation.getLatitude()+","+
                                 "\"longitude\":" +myLocation.getLongitude()+","+
                                 "\"hasAltitude\":" + myLocation.hasAltitude()+","+
                                 "\"altitude\":" + myLocation.getAltitude()+","+
                                 "\"hasSpeed\":" + myLocation.hasSpeed()+","+
                                 "\"speed\":" + myLocation.getSpeed()+","+
                                 "\"hasBearing\":" + myLocation.hasBearing()+","+
                                 "\"bearing\":" + myLocation.getBearing()+ ","+
                                 "\"hasAccuracy\":"+ myLocation.hasAccuracy()+","+
                                 "\"accuracy\":"+ myLocation.getAccuracy()+ "," +
                                 "\"street\":\"" + address[0] + "\"," +
                                 "\"postalcode\":\"" + address[1] + "\"," +
                                 "\"sublocality\":\"" + address[2] + "\"," +
                                 "\"locality\":\"" + address[3] + "\"," +
                                 "\"country\":\""+ address[4] + "\"" +
                             "},";
         }
         else{
             sensorData = "";
         }
         sensorData +=
                 "\"acceleration\":"+ accelString+ ","+
                 "\"light\":"+ light;
         return sensorData;
     }
 
     public SensorData(Context ctx){
 //        locale nederlands; resultaten in nl
         Locale locale = new Locale("nl", "BE");
         geoCoder = new Geocoder(ctx, locale);
         linear_acceleration = new float[3];
         final float[] gravity = new float[3];
         locationManager = (LocationManager) ctx.getSystemService(Context.LOCATION_SERVICE);
         locationListeners = new ArrayList<LocationListener>();
         SensorManager sensorManager = (SensorManager) ctx.getSystemService(Context.SENSOR_SERVICE);
         Sensor acSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
         Sensor lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
         SensorEventListener lightListener = new SensorEventListener() {
             @Override
             public void onSensorChanged(SensorEvent event) {
                 light = event.values[0];
             }
 
             @Override
             public void onAccuracyChanged(Sensor sensor, int i) {
 
             }
         };
 
         SensorEventListener acListener = new SensorEventListener() {
             @Override
             public void onSensorChanged(SensorEvent event) {
                 // In this example, alpha is calculated as t / (t + dT),
                 // where t is the low-pass filter's time-constant and
                 // dT is the event delivery rate.
 
                 final float alpha = 0.8f;
 
                 // Isolate the force of gravity with the low-pass filter.
                 gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
                 gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
                 gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];
 
                 // Remove the gravity contribution with the high-pass filter.
                 linear_acceleration[0] = event.values[0] - gravity[0];
                 linear_acceleration[1] = event.values[1] - gravity[1];
                 linear_acceleration[2] = event.values[2] - gravity[2];
             }
 
             @Override
             public void onAccuracyChanged(Sensor sensor, int i) {
 
             }
         };
 //        delay in MICROseconds
         sensorManager.registerListener(acListener, acSensor, 500000);
         sensorManager.registerListener(lightListener, lightSensor, 5000000);
 
 
 
 // Register the listener with the Location Manager to receive location updates
 //        om de 300000 ms of 5 minuten checken
 //        Log.d("LOCATION", String.valueOf(locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)));
 //        NOT HERE SINCE ONRESUME IS EXECUTED EVERY TIME!!!!
 //        if(locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10000, 0, locationListeners.get(0));
 //        if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 0, locationListeners.get(1));
 //        network more logical for last-known
         myLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
 //        LAST ADDRESS
         address1 = convertLocationToAddress(myLocation);
 
     }
 
     protected Address convertLocationToAddress(Location location){
        List<Address> addresses = null;
         try{
             addresses = geoCoder.getFromLocation(location.getLatitude(), location.getLongitude(),1);
         }catch(Exception e){
             e.printStackTrace();
             return null;
         }
         if(addresses!=null && !addresses.isEmpty()){
             return addresses.get(0);
         }
         return null;
     }
     /** Determines whether one Location reading is better than the current Location fix
      * @param location  The new Location that you want to evaluate
      * @param currentBestLocation  The current Location fix, to which you want to compare the new one
      */
 //    2 minutes maar laten staan, aangezien we zowel gps als netwerk gebruiken
     protected boolean isBetterLocation(Location location, Location currentBestLocation) {
         if (currentBestLocation == null) {
             // A new location is always better than no location
             return true;
         }
 
         // Check whether the new location fix is newer or older
         long timeDelta = location.getTime() - currentBestLocation.getTime();
         boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
         boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
         boolean isNewer = timeDelta > 0;
 
         // If it's been more than two minutes since the current location, use the new location
         // because the user has likely moved
         if (isSignificantlyNewer) {
             return true;
             // If the new location is more than two minutes older, it must be worse
         } else if (isSignificantlyOlder) {
             return false;
         }
 
         // Check whether the new location fix is more or less accurate
         int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
         boolean isLessAccurate = accuracyDelta > 0;
         boolean isMoreAccurate = accuracyDelta < 0;
         boolean isSignificantlyLessAccurate = accuracyDelta > 200;
 
         // Check if the old and new location are from the same provider
         boolean isFromSameProvider = isSameProvider(location.getProvider(),
                 currentBestLocation.getProvider());
 
         // Determine location quality using a combination of timeliness and accuracy
         if (isMoreAccurate) {
             return true;
         } else if (isNewer && !isLessAccurate) {
             return true;
         } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
             return true;
         }
         return false;
     }
 
     /** Checks whether two providers are the same */
     private boolean isSameProvider(String provider1, String provider2) {
         if (provider1 == null) {
             return provider2 == null;
         }
         return provider1.equals(provider2);
     }
 
     public void stop(){
         // Remove the listener you previously added
         for(int i = 0; i < locationListeners.size(); i++){
             locationManager.removeUpdates(locationListeners.get(i));
             Log.d("STREAMSTORE", "removed listeners");
         }
         locationListeners.clear();
     }
 
     public void resume(){
         int nrListeners = 0;
         if(locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) nrListeners++;
        if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) nrListeners++;
         // Define a listener that responds to location updates
 //        separate for GPS and network
         for(int i = 0; i < nrListeners; i++){
             Log.d("STREAMSTORE", "creating new listener");
             LocationListener locListener = new LocationListener() {
                 @Override
                 public void onLocationChanged(Location location) {
                     // Called when a new location is found by the location provider.
                     Log.d("STREAMSTORE", location.getProvider());
                     if(isBetterLocation(location, myLocation)){
                         myLocation = location;
                         address1 = convertLocationToAddress(myLocation);
                     }
                 }
                 @Override
                 public void onStatusChanged(String provider, int status, Bundle extras) {}
                 @Override
                 public void onProviderEnabled(String provider) {}
                 @Override
                 public void onProviderDisabled(String provider) {}
             };
             locationListeners.add(locListener);
         }
         if(locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10000, 0, locationListeners.get(0));
        if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 0, locationListeners.get(1));
     }
 }
