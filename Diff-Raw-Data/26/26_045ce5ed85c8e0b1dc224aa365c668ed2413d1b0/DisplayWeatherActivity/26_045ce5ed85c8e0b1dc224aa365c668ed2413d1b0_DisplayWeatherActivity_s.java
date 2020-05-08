 package com.threedigital.weatherme;
 
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 public class DisplayWeatherActivity extends Activity {
     public ImageButton bikeButton;
     public TextView textView;
     public ImageView imageView;
     public TextView textView2;
     public MyData data;
 
 
     public UserLocationManager mMyPersonalLocationManager;
     public ForecastAPIRequestObject myWeatherDataPopulator;
     public PopulateDataTask myFriendPopulateDataTask;
 
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
         goGetWeatherData();
 
 
 
         ImageButton bikeButton = (ImageButton) findViewById(R.id.bikeButton);
         bikeButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 Intent intent = new Intent(getApplicationContext(), BikeActivity.class);
                 intent.putExtra("temperature", String.valueOf(data.getmCurrentTemp()));
                 intent.putExtra("precipProbability",String.valueOf(data.getmPercipitation()));
                 startActivity(intent);
                 finish();
 
 
 
             }
         });
         //dont let them click until i have data.
 //        bikeButton.setEnabled(false);
 
 
 
 
 
         }
 
     private void goGetWeatherData() {
         myFriendPopulateDataTask = new PopulateDataTask(this);
 
 
 
     }
 
     public void receiveWeatherData(MyData data) {
 
         TextView textView = (TextView) findViewById(R.id.TempView);
         textView.setText(String.valueOf(data.getmCurrentTemp()));
         TextView textView2 = (TextView) findViewById(R.id.textView2);
         textView2.setText(String.valueOf(data.getmTodaysStatus()));
 
 
 
 
 
         Log.e("Look at my data", data.getmTodaysStatus());
 //        bikeButton.setEnabled(true);
     }
 
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.main, menu);
         return true;
     }
 
 
 
 
 
 
 }
 
 
