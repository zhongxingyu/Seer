 package com.example.navidration;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.hardware.SensorEventListener;
 import android.hardware.SensorManager;
 import android.location.Location;
 import android.location.LocationManager;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.preference.PreferenceManager;
 import android.view.View;
 import android.widget.Button;
 import android.widget.TextView;
 import android.widget.ToggleButton;
 
 import org.json.JSONObject;
 
 import java.io.BufferedReader;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.text.DecimalFormat;
 import java.util.LinkedList;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.concurrent.TimeUnit;
 
 
 /**
  * Created by Rachel Fang on 6/2/13.
  */
 public class tracking extends Activity implements SensorEventListener {
     private SensorManager mSensorManager;
     private Sensor mAccelerometer;
     private Sensor mGravity;
     private Sensor mMagnetic;
 
     float[] gravity = null;
     float[] magnet = null;
     float[] inR = new float[16];
     float[] outR = new float[16];
     float[] output = new float[4];
     float[] linAcceleration = {0,0,0,0};
     private float localMin = 0;
     private float localMax = 0;
     private float localMean = 0;
     private float threshold = 0;
     private int step = 0;
 
     private static int DEFAULT_WALKING_MAX_STEPS = 200;
     private static int DEFAULT_IDLE = 0;
     private static int DEFAULT_JOGGING_MAX_STEPS = 300;
     private LinkedList<Long> step_timestamps = new LinkedList<Long>();
 
     private static int IDLE_MODE = 90009;
     private static int WALKING_MODE = 10001;
     private static int JOGGING_MODE = 20002;
     private static int RUNNING_MODE = 30003;
     private int CURRENT_MODE = IDLE_MODE;
     private long prev_timestamp = -1;
     private long start_timestamp = -1;
     public int dehydrationLevel;	// min: 1, max: 5
     Dehydration mDehydration;
    private long lastTimeDrankWater = -1;
 
     int weatherTemperature = 0;
     int weatherHumidty = 0;
     DecimalFormat dForm = new DecimalFormat("#.###");
     boolean weatherDataRetrieved;
     Timer mTimer;
     SharedPreferences mSharedPreferences;
 
     boolean isTracking = false;
 
 
     TextView tvHydrate, tvTime;
 
     class Dehydration
     {
 
         // private variables
         private int intensity;	// from profile, [1: easy, 2: moderate, 3: hard]
         private int time;		// from timer, in minutes
         private int weight; 	// from profile, in lbs
         private double vwl;		// volume of water lost
         private double mwl;		// mass of water lost
         private double bwl;		// percent body weight lost
 
         // public variables
         public int temperature;
         public int relativeHumidity;
         public double heatIndex;
         public int heatCategory;
         public double water;
 
 
 
 
         // private methods
         private double calcHeatIndex (int t, int r)
         {
             double hi = 16.923 + (1.85212 * Math.pow(10,-1) * t) + (5.37941 * r) - (1.00254 * Math.pow(10,-1) * t * r)
                     + (9.41695 * Math.pow(10,-3) * Math.pow(t,2)) + (7.28898 * Math.pow (10,-3) * Math.pow(r,2)) + (3.45372 * Math.pow(10,-4) * Math.pow(t,2) * r)
                     - (8.14971 * Math.pow(10,-4) * t * Math.pow(r,2)) + (1.02102 * Math.pow(10,-5) * Math.pow(t,2) * Math.pow(r,2)) - (3.8646 * Math.pow(10,-5) * Math.pow(t,3))
                     + (2.91583 * Math.pow(10,-5) * Math.pow(r,3)) + (1.42721 * Math.pow(10,-6) * Math.pow(t,3) * r) + (1.97483 * Math.pow(10,-7) * t * Math.pow(r,3))
                     - (2.18429 * Math.pow (10,-8) * Math.pow(t,3) * Math.pow(r,2)) + (8.43296 * Math.pow(10,-10) * Math.pow(t,2) * Math.pow(r,3)) - (4.81975 * Math.pow(10,-11) * Math.pow(t,3) * Math.pow (r,3));
             return hi;
         }
 
         private int calcHeatCategory (double hi)
         {
             if (hi < 80)
             {
                 if (intensity == WALKING_MODE)
                     water = 0.5;
                 else if (intensity == JOGGING_MODE)
                     water = 0.75;
                 else
                     water = 0.75;
                 return 1;	// normal
             }
             else if (hi < 91)
             {
                 if (intensity == 1)
                     water = 0.5;
                 else if (intensity == 2)
                     water = 0.75;
                 else
                     water = 1;
                 return 2;	// caution
             }
             else if (hi < 104)
             {
                 if (intensity == 1)
                     water = 0.75;
                 else if (intensity == 2)
                     water = 0.75;
                 else
                     water = 1;
                 return 3;	// extreme caution
             }
             else if (hi < 125)
             {
                 if (intensity == 1)
                     water = 0.75;
                 else if (intensity == 2)
                     water = 0.75;
                 else
                     water = 1;
                 return 4;	// danger
             }
             else
             {
                 if (intensity == 1)
                     water = 1;
                 else if (intensity == 2)
                     water = 1;
                 else
                     water = 1;
                 return 5;	// extreme danger
             }
         }
 
         private double calcBodyWeightLoss ()
         {
             vwl = time / 60 * water;
             mwl = vwl * 946.4 * 0.001;
             return mwl / weight * 2.203;
         }
 
         // constructor
         public Dehydration ()
         {
 
             temperature = weatherTemperature;
             relativeHumidity = weatherHumidty;
             heatIndex = calcHeatIndex (temperature, relativeHumidity);
             heatCategory = calcHeatCategory (heatIndex);
             if (CURRENT_MODE == WALKING_MODE)
                 intensity = new Integer(mSharedPreferences.getString("prefWalkingValue", "1"));
             else if (CURRENT_MODE == JOGGING_MODE)
                 intensity = new Integer(mSharedPreferences.getString("prefJoggingValue", "2"));
             else
                 intensity = new Integer(mSharedPreferences.getString("prefRunningValue", "3"));
             weight = new Integer(mSharedPreferences.getString("setWeight", "100"));
 
         }
 
         public void updateHydrationLevel()
         {
            time = currentTime()-((int) lastTimeDrankWater/1000);
             bwl = calcBodyWeightLoss ();
            System.out.println("Body weight loss: "+bwl);
             // 5 levels of dehydration
             if (bwl <= 2)
                 dehydrationLevel = 1;
             else if (bwl <= 4)
                 dehydrationLevel = 2;
             else if (bwl <= 6)
                 dehydrationLevel = 3;
             else if (bwl <= 8)
                 dehydrationLevel = 4;
             else
                 dehydrationLevel = 5;
            System.out.println("Dehydration Level: "+dehydrationLevel);
         }
     }
 
 
     private class WeatherAsyncTask extends AsyncTask<Void, Integer, Void> {
 
         @Override
         protected void onPreExecute() {
             // update the UI immediately after the task is executed
             super.onPreExecute();
             weatherDataRetrieved = false;
 
         }
 
         @Override
         protected void onProgressUpdate(Integer... values) {
             super.onProgressUpdate(values);
         }
 
         @Override
         protected void onPostExecute(Void result) {
             super.onPostExecute(result);
             weatherDataRetrieved = true;
         }
 
         @Override
         protected Void doInBackground(Void... params) {
             LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
             Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
             if (location == null)
                 location = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
             double longitude = Double.valueOf(dForm.format(location.getLongitude()));
             double latitude = Double.valueOf(dForm.format(location.getLatitude()));
             URL url = null;
             String data= "";
             BufferedReader in = null;
             try
             {
                 String l = "";
                 String output = "";
                 String api_url = "http://api.worldweatheronline.com/free/v1/weather.ashx?key=324eefpxmgz3ww7c6tga4svd&num_of_days=1&q="+latitude+","+longitude+"&format=json";
                 url = new URL(api_url);
                 HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                 if(urlConnection!=null){
                     urlConnection.setDoInput(true);
                 }
 
                 InputStream is = urlConnection.getInputStream();
                 InputStreamReader isr = new InputStreamReader(is);
                 in = new BufferedReader(isr);
                 while ((l = in.readLine()) !=null){
 
                     JSONObject weather_json = new JSONObject(l);
 
                     String tempF = weather_json.getJSONObject("data").getJSONArray("current_condition").getJSONObject(0).getString("temp_F");
                     String humidity = weather_json.getJSONObject("data").getJSONArray("current_condition").getJSONObject(0).getString("humidity");
                     weatherTemperature = new  Integer(tempF);
                     weatherHumidty = new Integer(humidity);
                 }
                 weatherDataRetrieved = true;
                 in.close();
             }
             catch (Exception ex)
             {
                 ex.printStackTrace();
             }
             finally{
                 if (in != null){
                     try{
                         in.close();
                     }catch (Exception e){
                         e.printStackTrace();
                     }
                 }
             }
             return null;
         }
     }
 
 
     final Handler h = new Handler(new Handler.Callback() {
 
         @Override
         public boolean handleMessage(Message msg) {
             long millis = System.currentTimeMillis() - start_timestamp;
             int seconds = (int) (millis / 1000);
             int minutes = seconds / 60;
             seconds     = seconds % 60;
 
             tvTime.setText(String.format("%d:%02d", minutes, seconds));
             return false;
         }
     });
 
     class timeTask extends TimerTask {
 
         @Override
         public void run() {
             h.sendEmptyMessage(0);
         }
     };
 
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_tracking);
         mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
         mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
         mGravity= mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
         mMagnetic = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
         mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
         //get layout
         WeatherAsyncTask wTask = new WeatherAsyncTask();
 
         wTask.execute();
         while (!weatherDataRetrieved)
         {
             try
             {
                 wTask.get(1000, TimeUnit.MILLISECONDS);
             }
             catch (Exception ex)
             {
 
             }
         }
 
         mDehydration = new Dehydration();
 
         tvHydrate = (TextView)findViewById(R.id.tvHydrate);
         tvTime = (TextView) findViewById(R.id.tvTime);
     }
 
 
     protected int currentTime()
     {
         return (int) (System.currentTimeMillis()-start_timestamp)/1000;
     }
 
 
 
     public void tracking_clicked(View view) {
         // Is the toggle on?
         boolean on = ((ToggleButton) view).isChecked();
 
         if (on) {
             //RUNNING
             isTracking = true;
             start_timestamp = System.currentTimeMillis();
            lastTimeDrankWater = start_timestamp;
             mTimer = new Timer();
             mTimer.schedule(new timeTask(), 0,500);
         } else {
             //NOT RUNNING
             isTracking = false;
             mTimer.cancel();
             mTimer.purge();
 
         }
     }
 
    public void input_drink(View view){
        lastTimeDrankWater = System.currentTimeMillis();
    }


 
     @Override
     public final void onSensorChanged(SensorEvent event)
     {
 
         if (event.sensor.getType() == Sensor.TYPE_GRAVITY) {
             gravity = event.values;
         }
         if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
             magnet = event.values;
         }
         if (isTracking){
             long currentTimestamp = System.currentTimeMillis();
 
             if (gravity != null && magnet != null && event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION){
                 processAccelerationData(event.values, currentTimestamp);
             }
         }
     }
 
     @Override
     public void onAccuracyChanged(Sensor sensor, int i) {
 
     }
 
     protected void processAccelerationData(float[] data, long currentTimeStamp)
     {
         linAcceleration[0]= data[0];
         linAcceleration[1] = data[1];
         linAcceleration[2] = data[2];
         SensorManager.getRotationMatrix(inR, null, gravity, magnet);
         android.opengl.Matrix.invertM(outR, 0, inR, 0);
         if (0.9 < outR[6] && outR[6] < 1.1 ) {
             return;
         }
         float[] result = new float[4];
         android.opengl.Matrix.multiplyMV(result, 0, outR, 0, linAcceleration, 0);
         float xyAcceleration = android.util.FloatMath.sqrt(result[0]*result[0] + result[1]*result[1]);
         boolean stepCounted = false;
         if (xyAcceleration > 1.3) {
             if (localMax < result[2]) {
                 localMax = result[2];
             }
             if (localMin > result[2] && result[2] < localMax) {
                 localMin = result[2];
             }
             localMean = (localMax + localMin) / 2;
             if (localMin <= localMean && localMean <= localMax) {
                 threshold++;
                 localMax = 0;
                 localMin = 0;
 
             }
             if (threshold > 50) {
                 threshold = 0;
                 step++;
                 stepCounted = true;
                 step_timestamps.add(currentTimeStamp);
 
             }
         }
 
         if (prev_timestamp != -1 && currentTimeStamp-prev_timestamp > 1000)
         {
             long iterator = step_timestamps.getFirst();
             while (currentTimeStamp-60000>iterator)
             {
                 step_timestamps.removeFirst();
                 iterator = step_timestamps.getFirst();
             }
 
             int num_steps_last_minute = step_timestamps.size();
 
 
 
             if (num_steps_last_minute == DEFAULT_IDLE)
                 CURRENT_MODE = IDLE_MODE;
             else if (num_steps_last_minute > DEFAULT_IDLE && num_steps_last_minute <= DEFAULT_WALKING_MAX_STEPS)
                 CURRENT_MODE = WALKING_MODE;
             else if (num_steps_last_minute > DEFAULT_WALKING_MAX_STEPS && num_steps_last_minute <= DEFAULT_JOGGING_MAX_STEPS)
                 CURRENT_MODE = JOGGING_MODE;
             else if (num_steps_last_minute > DEFAULT_JOGGING_MAX_STEPS)
                 CURRENT_MODE = RUNNING_MODE;
 
 
 
         }
         if (stepCounted)
         {
             prev_timestamp = currentTimeStamp;
             mDehydration.updateHydrationLevel();
 
            String dehydrationMessage = "Low risk";
             if (dehydrationLevel <= 2)
                 dehydrationMessage = "Low risk";
             else if (dehydrationLevel == 3)
                 dehydrationMessage = "Moderate risk";
            else if (dehydrationLevel >= 4)
                 dehydrationMessage = "High risk";
 
             tvHydrate.setText("Current Hydration Level: "+dehydrationMessage);
         }
 
 
     }
 
 
 
     @Override
     protected void onResume()
     {
         super.onResume();
         mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);
         mSensorManager.registerListener(this, mGravity, SensorManager.SENSOR_DELAY_FASTEST);
         mSensorManager.registerListener(this, mMagnetic, SensorManager.SENSOR_DELAY_FASTEST);
 
     }
 
     @Override
     protected void onPause() {
         super.onPause();
         mSensorManager.unregisterListener(this);
     }
 }
