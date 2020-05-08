 package com.threedigital.weatherme;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.widget.TextView;
 
 /**
  * Created by 3digital on 9/9/13.
  */
 public class BikeActivity extends Activity {
     public PopulateDataTask myFriendPopulateDataTask;
     public TextView textView6;
     public TextView textView5;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_weather);
 
         Bundle extras = getIntent().getExtras();
         TextView textView6 = (TextView) findViewById(R.id.textView6);
         TextView textView5 = (TextView) findViewById(R.id.textView5);
         if (extras != null) {
             textView6.setText(extras.getString("temperature"));
             textView5.setText(extras.getString("precipProbability"));
 
     }
 
 
 
 }
 }
 
 
 
 
 
 
 
 
 
 
 
 
 
 
