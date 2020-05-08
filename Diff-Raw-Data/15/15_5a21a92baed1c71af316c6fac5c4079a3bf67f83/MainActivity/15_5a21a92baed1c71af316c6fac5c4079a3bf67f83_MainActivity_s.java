 package com.kennyproductions.rtxapp;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.View;
 
 public class MainActivity extends Activity {
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
     }
 
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.main, menu);
         return true;
     }
     
     /** Called when the user clicks the News Button */
     public void openNews(View view) {
     	Intent intent = new Intent(this, ShowNewsActivity.class);
     }
     
     /** Called when the user clicks the schedule button */
     public void openSchedule(View view) {
     	Intent intent = new Intent(this, ShowScheduleActivity.class);
     }
     
     /** Called when the user clicks the map button */
     public void openMap(View view) {
     	Intent intent = new Intent(this, ShowMapActivity.class);
     }
     
     /** Called when the user clicks the extras button */
     public void openExtras(View view) {
     	Intent intent = new Intent(this, ShowExtrasActivity.class);
     }
     
 }
