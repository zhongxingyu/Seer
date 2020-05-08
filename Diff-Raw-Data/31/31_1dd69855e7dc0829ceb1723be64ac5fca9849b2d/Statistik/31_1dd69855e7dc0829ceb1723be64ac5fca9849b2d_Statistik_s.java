 package com.Grupp01.gymapp;
 
 import android.os.Bundle;
 
 import com.actionbarsherlock.app.SherlockActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 
 public class Statistik extends SherlockActivity {
 
 	@Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.statistik);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
     	MenuInflater inflater = getSupportMenuInflater();
     	inflater.inflate(R.menu.statistik, menu);
         return true;
     }
 }
