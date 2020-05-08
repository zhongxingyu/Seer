 package com.amd.myhomework;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ImageButton;
 
 public class MainActivity extends Activity {
 	
 	private ImageButton calendarButton;
 	private ImageButton classesButton;
 	private enum SwitchActivity {Calendar, Homework, Classes, Settings, Account, About};
 	
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.six_icon_screen);
 		
 		calendarButton = (ImageButton) findViewById(R.id.calendarButton);
 		calendarButton.setOnClickListener(new OnClickListener() {
 
 			@Override
			public void onClick(View v) {
 				switchActivity(SwitchActivity.Calendar);
 			}
 			
 		});
 		
 		classesButton = (ImageButton) findViewById(R.id.classesButton);
 		classesButton.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				switchActivity(SwitchActivity.Classes);
 			}
 			
 		});
 		
 	}
 	
 	private void switchActivity(SwitchActivity type){
 		Intent activityChange;
 		switch (type){
 		case Calendar:
 			activityChange = new Intent(MainActivity.this, CalendarActivity.class);
 			this.startActivity(activityChange);
 			break;
 		case Classes:
 			activityChange = new Intent(MainActivity.this, classesActivity.class);
 			this.startActivity(activityChange);
 			break;
 		}
 	}
 }
