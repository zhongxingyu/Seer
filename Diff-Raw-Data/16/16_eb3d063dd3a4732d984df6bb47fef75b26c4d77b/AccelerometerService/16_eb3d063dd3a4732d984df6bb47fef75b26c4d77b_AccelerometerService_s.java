 package com.islamsharabash.clock_of_you;
 
 import android.app.Service;
 import android.content.Intent;
 import android.hardware.SensorManager;
 
 import org.openintents.sensorsimulator.hardware.Sensor;
 import org.openintents.sensorsimulator.hardware.SensorEvent;
 import org.openintents.sensorsimulator.hardware.SensorEventListener;
 import org.openintents.sensorsimulator.hardware.SensorManagerSimulator;
 import android.os.IBinder;
 import android.util.Log;
 
 public class AccelerometerService extends Service implements SensorEventListener {
   /* Time in milliseconds to read the accelerometer
    * if you want to set this to 0, instead just comment out the sleep code
    */
   private long READING_THROTTLE = 1000;
   private  SensorManagerSimulator mSensorManager;
   private Sensor mAccelerometer;
   private MovementOpenHelper dbHelper; 
 
   @Override
   public IBinder onBind(Intent intent) {
     return null;
   }
 
   @Override
   public void onCreate() {
     super.onCreate();
     mSensorManager = SensorManagerSimulator.getSystemService(this, SENSOR_SERVICE);
    mSensorManager.connectSimulator();
     
     mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
     mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
 
     dbHelper = new MovementOpenHelper(this);
   }
 
   @Override
   public int onStartCommand(Intent intent, int flags, int startId) {
     Log.d("AccelerometerService", "Received start id " + startId + ": " + intent);
     return START_STICKY;
   }
 
   @Override
   public void onDestroy() {
     dbHelper.close();
   }
 
   @Override
   public void onStart(Intent intent, int startid) {
     //code to execute when the service is starting up
   }
 
   @Override
   public void onAccuracyChanged(Sensor sensor, int accuracy) {
     // TODO Auto-generated method stub
 
   }
 
   @Override
   public void onSensorChanged(SensorEvent event) {
     recordMovement(event);
 
     try {
       Thread.sleep(READING_THROTTLE );
     } catch (InterruptedException e) {
       e.printStackTrace();
     }
   }
 
   /* recordMovement filters for movements and only records activity, this
    * implies other periods do not have activity
    */
   private void recordMovement(SensorEvent event) {
     dbHelper.insertMovement(System.currentTimeMillis(), magnitude(event));
   }
 
   /* magnitude of the acceleration minus gravity 
    */ 
   private float magnitude(SensorEvent event) {
     float magnitude = (float)Math.sqrt(
       event.values[0] * event.values[0] +
       event.values[1] * event.values[1] +
       event.values[2] * event.values[2]);
 
     return Math.abs(magnitude - SensorManager.GRAVITY_EARTH);
   }
 }
