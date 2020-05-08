 package com.Grupp01.gymapp;
 
 import android.os.Bundle;
 
import com.Grupp01.gymapp.R;
 import com.actionbarsherlock.app.SherlockActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 
 public class Workout extends SherlockActivity {
 
 	@Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.workout);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
     	MenuInflater inflater = getSupportMenuInflater();
     	inflater.inflate(R.menu.workouts, menu);
         return true;
     }
 }
