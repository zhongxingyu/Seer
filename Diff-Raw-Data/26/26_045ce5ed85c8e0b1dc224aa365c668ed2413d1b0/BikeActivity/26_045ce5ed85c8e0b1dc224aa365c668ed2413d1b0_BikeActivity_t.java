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
    public TextView textView7;
    public TextView textView8;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_weather);
 
         Bundle extras = getIntent().getExtras();
         TextView textView6 = (TextView) findViewById(R.id.textView6);
         TextView textView5 = (TextView) findViewById(R.id.textView5);
        TextView textView7 = (TextView) findViewById(R.id.textView7);
        TextView textView8 = (TextView) findViewById(R.id.textView8);
         if (extras != null) {
             textView6.setText(extras.getString("temperature"));
             textView5.setText(extras.getString("precipProbability"));
            textView7.setText(extras.getString("windSpeed"));
            textView8.setText(extras.getString("summary"));

 
     }
 
 
 
 }
 }
 
 
 
 
 
 
 
 
 
 
 
 
 
 
