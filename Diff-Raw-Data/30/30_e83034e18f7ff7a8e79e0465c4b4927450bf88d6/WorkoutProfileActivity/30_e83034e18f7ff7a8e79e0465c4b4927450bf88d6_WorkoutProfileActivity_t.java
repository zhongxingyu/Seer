 package com.vorsk.crossfitr;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.content.Intent;
 import android.view.View;
 import android.view.View.OnClickListener;
 
public class WorkoutProfileActivity extends Activity implements OnClickListener 
 {
 	public void onCreate(Bundle savedInstanceState) 
 	{
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.workout_profile);
		
        // begin workout button
        View beginButton = findViewById(R.id.button_begin_workout);
        beginButton.setOnClickListener(this);
	}
	
	public void onClick(View v) 
	{
		switch (v.getId()) 
		{
			case R.id.button_begin_workout:
				Intent i = new Intent(this, TimerTabWidget.class);
				startActivity(i);
				break;
		}
 	}
 }
