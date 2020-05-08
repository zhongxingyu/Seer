 /* 
 Copyright (c) 2013, Haneen Abu-Khater, Alex Flyte, Kyle Greene, Vi Nguyen, Clinton Olson, and Hanrong Zhao
 All rights reserved.
 
 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:
 
 1. Redistributions of source code must retain the above copyright notice, this
 list of conditions and the following disclaimer.
 2. Redistributions in binary form must reproduce the above copyright notice,
 this list of conditions and the following disclaimer in the documentation
 and/or other materials provided with the distribution.
 
 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
 
 
 package org.leifolson.withinreach;
 
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 
 import android.annotation.TargetApi;
 import android.app.Activity;
 import android.app.DatePickerDialog;
 import android.app.DatePickerDialog.OnDateSetListener;
 import android.app.Dialog;
 import android.app.TimePickerDialog;
 import android.app.TimePickerDialog.OnTimeSetListener;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Build;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 
 import android.view.View;
 import android.widget.Button;
 import android.widget.CompoundButton;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 import android.widget.DatePicker;
 import android.widget.SeekBar;
 import android.widget.TextView;
 import android.widget.TimePicker;
 import android.widget.ToggleButton;
 
 public class MenuActivity extends Activity {
 	
 	// setting up some variables to hold menu choices
 	// may need to store this information differently
 	// or find a way to pass it back to the WithinReachActivity
 	// at a later time
 	// its possible we dont even need many of these
 	private int timeConstraint = 15;
 	private int modeCode = 7;
 	private boolean walkToggled = true;
 	private boolean bikeToggled = true;
 	private boolean transitToggled = true;
 	private final int MAX_TIME = 90;
 	private GregorianCalendar calendar;
 	private int year;
 	private int monthOfYear;
 	private int dayOfMonth;
 	private int hourOfDay;
 	private int minute;
	private final long MILLI_PER_DAY = 86400000;
 	
 	
 	// the menu has the following UI elements
 	// these will most likely be changed to fragments at a later time
 	private TextView seekTime;
 	private SeekBar timeSeekBar;
 	private ToggleButton walkToggleButton;
 	private ToggleButton bikeToggleButton;
 	private ToggleButton transitToggleButton;
 	private Button doneButton;
 	private Button helpButton;
 	private Button licenseButton;
 	private Button aboutButton;
 	private Button setDateButton;
 	private Button setTimeButton;
 
 
 	/***** ACTIVITY LIFECYCLE MANAGEMENT METHODS *****/
 	
 	// I can probably put some wrapper functions in here to abstract away
 	// all the details of setting up the listeners for UI elements
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_menu);
 		
 		// get references to menu interface elements
 		seekTime = (TextView)findViewById(R.id.time_text);
 		timeSeekBar = (SeekBar)findViewById(R.id.time_seekbar);
 		walkToggleButton = (ToggleButton)findViewById(R.id.toggle_button_walk);
 		bikeToggleButton = (ToggleButton)findViewById(R.id.toggle_button_bike);
 		transitToggleButton = (ToggleButton)findViewById(R.id.toggle_button_transit);
 		doneButton = (Button) findViewById(R.id.menu_done_button);
 		helpButton = (Button) findViewById(R.id.help_menu_button);
 	    licenseButton = (Button)findViewById(R.id.license_menu_button);
 		aboutButton = (Button)findViewById(R.id.about_menu_button);
 		setDateButton = (Button)findViewById(R.id.set_date_button);
 		setTimeButton = (Button)findViewById(R.id.set_time_button);
 		
 		calendar = (GregorianCalendar)Calendar.getInstance();
 		
 		// sets all default values for UI elements
 		setDefaults();
 		
 		// sets up listener methods for all UI buttons and other elements
 		setListeners();
 		
 	}
 	
 	
 	public void startHelpMenuActivity(){
 		Intent launchHelpMenu = new Intent(this,HelpActivity.class);
 		startActivity(launchHelpMenu);
 		
 	}
 	
 	public void returnToWithinReachActivity()
 	{
 		Intent launchWithinReach = new Intent(this, WithinReachActivity.class);
 		
 		modeCode = 0;
 		if (walkToggled)
 			modeCode += 1;
 		if (bikeToggled)
 			modeCode += 2;
 		if (transitToggled)
 			modeCode += 4;
 		
 		launchWithinReach.putExtra("timeConstraint", timeConstraint);
 		launchWithinReach.putExtra("modeCode", modeCode);
 		launchWithinReach.putExtra("year", year);
 		launchWithinReach.putExtra("month", monthOfYear);
 		launchWithinReach.putExtra("day", dayOfMonth);
 		launchWithinReach.putExtra("hour", hourOfDay);
 		launchWithinReach.putExtra("min", minute);
 		
 		startActivity(launchWithinReach);
 	}
 	
 	/***** ACTIVITY HELPER METHODS *****/
 	
 	private void setDefaults(){
 		// set options to default values
 		timeSeekBar.setMax(MAX_TIME);
 		walkToggleButton.setChecked(true);
 		bikeToggleButton.setChecked(true);
 		transitToggleButton.setChecked(true);
 		
 		timeSeekBar.setProgress(15);
 		seekTime.setText(Integer.toString(timeSeekBar.getProgress()));
 		
 		dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
 		monthOfYear = calendar.get(Calendar.MONTH);
 		year = calendar.get(Calendar.YEAR);
 		hourOfDay = calendar.get(Calendar.HOUR_OF_DAY); 
 		minute = calendar.get(Calendar.MINUTE);
 	}
 	
 
 	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
 	private void setListeners(){
 		// setting up click listener for the seekbar
 		timeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
 			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser){
 				if(fromUser){ 
 					// change textview to new time selection
 					seekTime.setText(progress + " mins");
 					
 					// store time constraint value
 					timeConstraint = progress;
 				}
 			}
 			
 			// nothing to implement here but must be present
 			@Override
 			public void onStopTrackingTouch(SeekBar seekBar) {}
 			
 			@Override
 			public void onStartTrackingTouch(SeekBar seekBar) {}
 			
 
 		});
 		
 		// setting up toggle button listeners
 		walkToggleButton.setOnCheckedChangeListener(new OnCheckedChangeListener(){
 			@Override
 			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
 				walkToggled = isChecked;
 			}
 		});
 		
 		bikeToggleButton.setOnCheckedChangeListener(new OnCheckedChangeListener(){
 			@Override
 			public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
 				bikeToggled = isChecked;
 			}
 		});
 		
 		transitToggleButton.setOnCheckedChangeListener(new OnCheckedChangeListener(){
 			@Override
 			public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
 				transitToggled = isChecked;
 			}
 		});
 		
 
 		
 		// setting up the onclick listener to handle the button click
 		// this is just a way to launch the WithinReachActivity for now
 		doneButton.setOnClickListener(new View.OnClickListener() {
 			
 			@Override
 			public void onClick(View view) {
 				// takes us back to the WithinReachActivity
 				
 				//invokeServerComMgr();
 				returnToWithinReachActivity();
 				
 			}
 		});
 		
 		
 		helpButton.setOnClickListener(new View.OnClickListener(){
 
 			@Override
 			public void onClick(View arg0) {
 				//invokes the HelpActivity menu come up when help
 				//button is clicked
 				startHelpMenuActivity();
 				
 			}
 			
 		});
 		
 		
 	    final Dialog dialog = new Dialog(this);
 	   
 		licenseButton.setOnClickListener(new View.OnClickListener() 
 		{
 			public void onClick(View v) 
 			{
 				LayoutInflater inflater = (LayoutInflater)
 					       getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 				dialog.show();
 				dialog.setTitle("License Information");
 				dialog.setCancelable(true);
 				dialog.setContentView(inflater.inflate(R.layout.license_view, null, false));
 				
 			}
 		});
 
 		
 		aboutButton.setOnClickListener(
 				new View.OnClickListener() 
 				{
 					public void onClick(View v) 
 					{
 						LayoutInflater inflater = (LayoutInflater)
 							       getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 						dialog.show();
 						dialog.setTitle("About Within Reach");
 						dialog.setCancelable(true);
 						dialog.setContentView(inflater.inflate(R.layout.about_view, null, false));
 						
 					}
 				}
 				);
 		
 
 		setDateButton.setOnClickListener(
 				new View.OnClickListener() 
 				{
 					public void onClick(View v) 
 					{
 						DatePickerDialog datePickerDialog = 
 							new DatePickerDialog(MenuActivity.this,
 									new OnDateSetListener(){
 
 								@Override
 								public void onDateSet(DatePicker view,
 										int year, int monthOfYear,
 										int dayOfMonth) {
 									MenuActivity.this.year = year;
 									MenuActivity.this.monthOfYear = monthOfYear;
 									MenuActivity.this.dayOfMonth = dayOfMonth;				
 								}
 							}, year, monthOfYear, dayOfMonth);
 						if(Build.VERSION.SDK_INT >= 11){
 							long offset = calendar.get(Calendar.ZONE_OFFSET) + 
 											calendar.get(Calendar.DST_OFFSET);
							long currTime = (calendar.getTimeInMillis() + offset);
 							long min = 18 * MILLI_PER_DAY;
 							long max = 42 * MILLI_PER_DAY;
							System.out.println("Min:" + min);
							System.out.println("Max:" + max);
 							DatePicker dp = datePickerDialog.getDatePicker();
 							dp.setMinDate(currTime - min);
 							dp.setMaxDate(currTime + max);
 						}
 						datePickerDialog.show();
 						
 					}
 				}
 				);
 		
 		
 		setTimeButton.setOnClickListener(
 				new View.OnClickListener() 
 				{
 					public void onClick(View v) 
 					{						
 						TimePickerDialog timePickerDialog = 
 							new TimePickerDialog(MenuActivity.this,
 									new OnTimeSetListener(){
 
 								@Override
 								public void onTimeSet(TimePicker view,
 										int hourOfDay, int minute) {
 									MenuActivity.this.hourOfDay = hourOfDay;
 									MenuActivity.this.minute = minute;		
 								}
 							},hourOfDay
 							 ,minute
 							 ,false);
 						
 						timePickerDialog.show();
 						
 					}
 				}
 				);
 	}
 	
 }
