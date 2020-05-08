 package dk.fluo.gps;
 
 import java.util.*;
 
 import android.app.Activity;
 import android.hardware.Sensor;
 import android.hardware.SensorManager;
 import android.location.*;
 import android.content.Context;
 import android.util.Log;
 
 
 /**
  * Created by hagbarth on 9/25/13.
  */
 public class GPSFixer {
 
     /**
      * Properties
      */
     private int timePeriod;
     private int dist;
     private int speed;
     private LocationManager locationManager;
     private FluoLocationListener listener;
     private Activity activity;
 
     /**
      * Constructors
      */
     public GPSFixer(int timePeriod, int dist, Activity activity){
         this.timePeriod = timePeriod;
         this.dist = dist;
         this.speed = 0;
         this.activity = activity;
 
         locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
         listener = new FluoLocationListener(activity, "time");
     }
 
     /**
      * Setters and getters
      */
     public int getTimePeriod(){
         return timePeriod;
     }
 
     public int getDist(){
         return dist;
     }
 
     public void setTimePeriod(int timePeriod){
         this.timePeriod = timePeriod;
     }
 
     public void setDist(int dist){
         this.dist = dist;
     }
 
     public void setSpeed(int value){
         speed = value;
     }
 
     /**
      * GPS related method
      */
     public ArrayList<Double> getPosition(){
         Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
 
         ArrayList<Double> coordinates = new ArrayList<Double>();
 
 
         if (location != null) {
             coordinates.add( location.getLongitude() );
             coordinates.add( location.getLatitude() );
         }
 
         return coordinates;
     }
 
     public void startTimeGPS(){
         stopGPS();
         locationManager.requestLocationUpdates(
                 LocationManager.GPS_PROVIDER,
                 timePeriod,
                 0,
                 listener
         );
     }
 
     public void startDistGPS(){
         stopGPS();
         listener.setDist(dist);
         locationManager.requestLocationUpdates(
                 LocationManager.GPS_PROVIDER,
                 0,
                 0,
                 listener
         );
     }
 
     public void startSpeedGPS(){
         stopGPS();
 
         listener.setSpeed(speed);
         listener.setDist(dist);
         locationManager.requestLocationUpdates(
                 LocationManager.GPS_PROVIDER,
                 0,
                 0,
                 listener
         );
     }
 
     public void startAccGPS(){
         stopGPS();
 
         listener.setType("accelerometer");
         listener.setDist(dist);
 
         SensorManager sensorManager = (SensorManager) activity.getSystemService(Context.SENSOR_SERVICE);
         FluoSensorEventListener sensorList = new FluoSensorEventListener();
         sensorManager.registerListener(sensorList, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
 
         locationManager.requestLocationUpdates(
                 LocationManager.GPS_PROVIDER,
                 0,
                 0,
                 listener
         );
     }
 
     public void stopGPS(){
         listener.setFirstFix(true);
         locationManager.removeUpdates(listener);
     }
 }
