 package com.android.csci498.lunchlist;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.view.Menu;
 
 public class LunchList extends Activity {
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lunch_list);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_lunch_list, menu);
         return true;
     }
 }
