 package com.kinnack.nthings.activity;
 
 import java.util.List;
 
 import android.content.Context;
 import android.graphics.Color;
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.hardware.SensorEventListener;
 import android.hardware.SensorManager;
 import android.os.Bundle;
 import android.util.Log;
 import android.widget.TextView;
 
 import com.kinnack.nthings.R;
 
 public class ProximityCounterActivity extends CounterActivity implements SensorEventListener {
     int current = 0;
    private boolean screenLoading = true;
     @Override
     public void onCreate(Bundle savedInstanceState_) {
         super.onCreate(savedInstanceState_);
         registerSensors();
     }
    
    @Override
    protected void onResume() {
        super.onResume();
        registerSensors();
        screenLoading = true;
    }
    
   
     @Override
    protected void onPause() {
        super.onPause();
         unRegisterSensors();
     }
    
     @Override
     protected int getLayout() {
         return R.layout.proximity_count;
     }
     
     @Override
     protected void count()
     {
         TextView count = (TextView) findViewById(R.id.CurrentCount);
         current++;
         count.setText("" + current);
         super.count();
     }
     private boolean registerSensors() {
         SensorManager sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
         List<Sensor> sensors = sm.getSensorList(Sensor.TYPE_PROXIMITY);
         for (Sensor s : sensors) {
             sm.registerListener(this, s, SensorManager.SENSOR_DELAY_NORMAL);
         }
         return sensors.size() > 0;
     }
     private void unRegisterSensors() {
         SensorManager sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
         sm.unregisterListener(this);
     }
     @Override
     public void onAccuracyChanged(Sensor arg0, int arg1) {
         //Don't care
     }
 
     @Override
     public void onSensorChanged(SensorEvent event_) {
         if (event_.sensor.getType() == Sensor.TYPE_PROXIMITY) {
            if (screenLoading) { screenLoading = false; return; }
             float value = event_.values[0];
             float max = event_.sensor.getMaximumRange();
             boolean prox_is_far = value >= max;
             Log.d("dgmt!proximityCounterActivity.onSensorChanged","sensed far?"+prox_is_far+" value="+value+", max="+max);
             //Beep on the way down so user knows they're close enough to trigger the prox sensor.
             //Count on the way up to ensure they do the final pushup.
             TextView currentCount = (TextView) findViewById(R.id.CurrentCount);
             if (prox_is_far) {
                 count();
                 currentCount.setTextColor(Color.WHITE);
             } else {
                 currentCount.setTextColor(Color.RED);
                 soundAlert.progressBeep();
             }
         }
     }
 
 }
