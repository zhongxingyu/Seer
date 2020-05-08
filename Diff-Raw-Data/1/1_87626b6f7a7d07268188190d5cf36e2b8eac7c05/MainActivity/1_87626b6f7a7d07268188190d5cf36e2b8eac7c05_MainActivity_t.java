 package com.example.skidrow;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.view.Menu;
 
 public class MainActivity extends Activity {
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
        this.setContentView(R.layout.homescreen);
         
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_main, menu);
         return true;
     }
 }
