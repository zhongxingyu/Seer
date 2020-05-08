 package com.vorsk.crossfitr;
 
 import com.vorsk.crossfitr.models.WorkoutModel;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 
 
 public class CrossFitrActivity extends Activity implements OnClickListener
 {
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) 
     {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         
         // workout button
         View workoutButton = findViewById(R.id.main_button_workout);
         workoutButton.setOnClickListener(this);
         // calendar button
         View calendarButton = findViewById(R.id.main_button_calendar);
         calendarButton.setOnClickListener(this);
         // profile button
         View profileButton = findViewById(R.id.main_button_profile);
         profileButton.setOnClickListener(this);
     }
 
 
 	public void onClick(View v) 
 	{
 		switch (v.getId()) {
 		case R.id.main_button_workout:
 			Intent i = new Intent(this, WorkoutsActivity.class);
 			startActivity(i);
 			break;
 
 		case R.id.main_button_calendar:
 			break;
 
 		case R.id.main_button_profile:
 			Intent p = new Intent(this, UserProfileActivity.class);
 			startActivity(p);
 			break;
 		}
 	}
 }
