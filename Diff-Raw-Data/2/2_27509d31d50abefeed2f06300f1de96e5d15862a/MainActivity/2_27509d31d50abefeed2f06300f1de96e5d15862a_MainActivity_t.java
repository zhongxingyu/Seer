 package com.Grupp01.gymapp;
 
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import com.Grupp01.gymapp.R;
 import com.actionbarsherlock.app.SherlockActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 
 public class MainActivity extends SherlockActivity {
 	
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
     }
     //Kommentar
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getSupportMenuInflater();
     	inflater.inflate(R.menu.activity_main, menu);
     	
         return true;
     }
     
     public void workout(View view)
     {
     	Intent workout = new Intent(this, ListWorkoutActivity.class);
     	startActivity(workout);
     }
     
     public void historik(View view)
     {
     	Intent historik = new Intent(this, Historik.class);
     	startActivity(historik);
     }
     
     public void statistik(View view)
     {
     	Intent statistik = new Intent(this, Statistik.class);
     	startActivity(statistik);
     }
     
     public void exercise(View view)
     {
     	Intent exercise = new Intent(this, ListExerciseActivity.class);
     	startActivity(exercise);
     }
 }
