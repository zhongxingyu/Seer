 package com.vorsk.crossfitr;
 
 import android.app.Activity;
 import android.app.Dialog;
 import android.app.TimePickerDialog;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.TextView;
 import android.widget.TimePicker;
 import android.content.Context;
 /*
  * This is contains a very rudimentary implementation of the timer popup. 
  * Jason: Feel free to change any of this. All variables are just there to make it work.
  */
 public class TimerActivity extends Activity 
 {
 	 private Button mPickTime;
 	 private TextView mTimerDisplay;
 
 	 private int mSec = 0;
 	 private int mMinute = 0;
 
 	 static final int NUMBER_DIALOG_ID = 0; // Dialog variable
 	    
 	public void onCreate(Bundle savedInstanceState) 
 	{
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.timer_tab);
 		
 		mPickTime = (Button) findViewById(R.id.pickTime);
 		mTimerDisplay = (TextView) findViewById(R.id.timer_label);
 		
 		// Opens Dialog on click
/*		mPickTime.setOnClickListener(new View.OnClickListener()
 		{
 			public void onClick(View v) 
 			{
 				NumberPicker dialog = new NumberPicker(getBaseContext());
 				dialog.setRange(0,59);
 				showDialog(NUMBER_DIALOG_ID);
 	        }
 	    });
*/	
 	}
 
 	// Sets the variables after the user selects a time. Updates text field 
 /*	private TimePickerDialog.OnTimeSetListener mTimeSetListener =
 		    new TimePickerDialog.OnTimeSetListener() {
 		        public void onTimeSet(TimePicker view, int minute, int seconds) {
 		            mSec = seconds;
 		            mMinute = minute;
 		            mTimerDisplay.setText("You chose: " +  new StringBuilder().append(pad(mMinute)).append(".").append(pad(mSec)));
 		        }
 	};
 	
 	// Appends a 0 to the front of a single character 
 	private static String pad(int c) {
 	    if (c >= 10)
 	        return String.valueOf(c);
 	    else
 	        return "0" + String.valueOf(c);
 	}*/
 	
 	// Creates the Dialog
/*	@Override
 	protected Dialog onCreateDialog(int id) 
 	{
 		switch (id) 
 		{
 			case NUMBER_DIALOG_ID:
 		    return new NumberPickerDialog(this, 1, 0);
 		 }
 		 return null;
 	}
	*/
	
 }
