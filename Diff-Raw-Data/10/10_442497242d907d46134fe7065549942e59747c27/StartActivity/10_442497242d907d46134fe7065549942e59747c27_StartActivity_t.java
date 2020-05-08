 package ru.entropysw.sporttracker;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.provider.Settings;
 import android.view.View;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.CompoundButton;
 import android.widget.Toast;
 
 public class StartActivity extends Activity {
     Button btnGetLocation;
     CheckBox cbDoMonitoring;
 
     GPSTracker gps;
 
     /**
      * Called when the activity is first created.
      */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         GPSTracker.get().setContext(StartActivity.this);
 
         btnGetLocation = (Button) findViewById(R.id.getLocationButton);
         btnGetLocation.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 showLocation();
             }
         });
 
         cbDoMonitoring = (CheckBox) findViewById(R.id.doMonitoringCheckbox);
         cbDoMonitoring.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
             @Override
             public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                 GPSTracker.get().setDoMonitoring(b);
             }
         });
     }
 
     private void showLocation() {
         GPSTracker gps = GPSTracker.get();
         if(gps.canGetLocation()) {
            gps.getLocation();
             gps.showLocationNotificator();
         } else {
             gps.showSettingsAlert();
         }
     }
 }
