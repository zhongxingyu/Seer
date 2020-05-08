 package com.Grupp01.gymapp;
 
 import android.os.Bundle;
 
 import com.actionbarsherlock.app.SherlockActivity;
 import com.actionbarsherlock.view.*;
 
 public class Historik extends SherlockActivity {
 
 	@Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.historik);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
     	MenuInflater inflater = getSupportMenuInflater();
     	inflater.inflate(R.menu.historik, menu);
         return true;
     }
 }
