 /*
  * Copyright (C) 2013 Scott Warner
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.tortel.deploytrack;
 
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 
 import android.annotation.SuppressLint;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.support.v4.app.FragmentManager;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.Toast;
 
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.actionbarsherlock.view.MenuItem;
 import com.fourmob.datetimepicker.date.DatePickerDialog;
 import com.fourmob.datetimepicker.date.DatePickerDialog.OnDateSetListener;
 import com.larswerkman.colorpicker.ColorPicker;
 import com.larswerkman.colorpicker.ColorPicker.OnColorChangedListener;
 import com.tortel.deploytrack.data.*;
 
 /**
  * Activity for creating and editing a Deployment
  */
 public class CreateActivity extends SherlockFragmentActivity {
 	private static final String KEY_TIME_START = "start";
 	private static final String KEY_TIME_END = "end";
 	private static final String KEY_SET_START = "startset";
 	private static final String KEY_SET_END = "endset";
 	private static final String KEY_NAME = "name";
 	private static final String KEY_COLOR_COMPLETED = "completed";
 	private static final String KEY_COLOR_REMAINING = "remaining";
 	
 	private EditText nameEdit;
 	private Button startButton;
 	private Button endButton;
 	private Button saveButton;
 	
 	private SimpleDateFormat format;
 	
 	//Colors
 	private int completedColor;
 	private int remainingColor;
 	
 	//Date range
 	private Calendar start;
 	private Calendar end;
 	
 	//Flags for dates
 	private boolean startSet;
 	private boolean endSet;
 	
 	//The data to save;
 	private Deployment deployment;
 	
 	@SuppressLint("SimpleDateFormat")
 	@Override
 	public void onCreate(Bundle savedInstanceState){
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_create);
 		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
 		
 		format = new SimpleDateFormat("MMM dd, yyyy");
 		
 		nameEdit = (EditText) findViewById(R.id.name);
 		startButton = (Button) findViewById(R.id.button_start);
 		endButton = (Button) findViewById(R.id.button_end);
 		saveButton = (Button) findViewById(R.id.button_save);
 		
 		//Color pickers
 		ColorPicker completedPicker = (ColorPicker) findViewById(R.id.picker_completed);
 		ColorPicker remainingPicker = (ColorPicker) findViewById(R.id.picker_remain);
 		
 		int id = getIntent().getIntExtra("id", -1);
 		if(id >= 0){
 			//Starting in edit mode, load the old data
 			deployment = DatabaseManager.getInstance(this).getDeployment(id);
 			
 			//Set the colors
 			completedColor = deployment.getCompletedColor();
 			remainingColor = deployment.getRemainingColor();
 			
 			//Set the dates
 			start = Calendar.getInstance();
 			end = Calendar.getInstance();
 			
 			start.setTimeInMillis(deployment.getStartDate().getTime());
 			end.setTimeInMillis(deployment.getEndDate().getTime());
 			startSet = true;
 			endSet = true;
 			
 			//Set the buttons
 			startButton.setText(getResources().getString(R.string.start_date)+
 					"\n"+format.format(start.getTime()));
 			endButton.setText(getResources().getString(R.string.end_date)+
 					"\n"+format.format(end.getTime()));
 			
 			//Set the name
 			nameEdit.setText(deployment.getName());
 			
 			getSupportActionBar().setTitle(R.string.edit);
 		} else {
 			deployment = new Deployment();
 			disableButton(endButton);
 			disableButton(saveButton);
 			
 			start = Calendar.getInstance();
 			end = (Calendar) start.clone();
 			startSet = false;
 			endSet = false;
 			
 			completedColor = Color.GREEN;
 			remainingColor = Color.RED;
 			
 			getSupportActionBar().setTitle(R.string.add_new);
 		}
 		
 		//If restore from rotation
 		if(savedInstanceState != null){
 			start.setTimeInMillis(savedInstanceState.getLong(KEY_TIME_START));
 			end.setTimeInMillis(savedInstanceState.getLong(KEY_TIME_END));
 			
 			startSet = savedInstanceState.getBoolean(KEY_SET_START);
 			endSet = savedInstanceState.getBoolean(KEY_SET_END);
 			
 			nameEdit.setText(savedInstanceState.getString(KEY_NAME));
 			
 			completedColor = savedInstanceState.getInt(KEY_COLOR_COMPLETED);
 			remainingColor = savedInstanceState.getInt(KEY_COLOR_REMAINING);
 			
 			//Set the date buttons, if set
 			if(startSet){
 				startButton.setText(getResources().getString(R.string.start_date)+
 						"\n"+format.format(start.getTime()));
 				enableButton(endButton);
 			}
 			
 			if(endSet && end.after(start)){
 				endButton.setText(getResources().getString(R.string.end_date)+
 						"\n"+format.format(end.getTime()));
 				enableButton(saveButton);
 			}
 		}
 		
 		remainingPicker.setColor(remainingColor);
 		remainingPicker.setOnColorChangedListener(new RemainingColorChangeListener());
 		completedPicker.setColor(completedColor);
 		completedPicker.setOnColorChangedListener(new CompletedColorChangeListener());
 	}
 	
 	@Override
 	protected void onSaveInstanceState(Bundle outState) {
 		super.onSaveInstanceState(outState);
 		//Save everything
 		outState.putLong(KEY_TIME_START, start.getTimeInMillis());
 		outState.putLong(KEY_TIME_END, end.getTimeInMillis());
 		
 		outState.putBoolean(KEY_SET_START, startSet);
 		outState.putBoolean(KEY_SET_END, endSet);
 		
 		outState.putString(KEY_NAME, nameEdit.getText().toString());
 		
 		outState.putInt(KEY_COLOR_COMPLETED, completedColor);
 		outState.putInt(KEY_COLOR_REMAINING, remainingColor);
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch(item.getItemId()){
 		//Finish on the icon 'up' pressed
 		case android.R.id.home:
 			this.finish();
 			return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 	
 	/**
 	 * Method to both disable a button, and set
 	 * the text color to gray
 	 * @param button
 	 */
 	private void disableButton(Button button){
 		button.setEnabled(false);
 		button.setTextColor(Color.DKGRAY);
 	}
 	
 	/**
 	 * Method to both enable the button and set the
 	 * text color to white
 	 * @param button
 	 */
 	private void enableButton(Button button){
 		button.setEnabled(true);
 		button.setTextColor(Color.WHITE);
 	}
 	
 	/**
 	 * Method called when the buttons are clicked
 	 * @param v
 	 */
 	public void onClick(View v){
 		FragmentManager fm = getSupportFragmentManager();
 		
 		switch(v.getId()){
 		case R.id.button_cancel:
 			this.finish();
 			return;
 		case R.id.button_save:
 			String name = nameEdit.getText().toString().trim();
 			if("".equals(name)){
 				Toast.makeText(this, R.string.invalid_name, Toast.LENGTH_SHORT).show();
 				return;
 			}
 			//Set the values
 			deployment.setStartDate(start.getTime());
 			deployment.setEndDate(end.getTime());
 			deployment.setName(name);
 			deployment.setCompletedColor(completedColor);
 			deployment.setRemainingColor(remainingColor);
 			//Save it
 			DatabaseManager.getInstance(this).saveDeployment(deployment);
 			//End
 			finish();
 			return;
 		case R.id.button_start:
 			DatePickerDialog startPicker = new DatePickerDialog();
 			startPicker.initialize(new OnDateSetListener(){
 				public void onDateSet(DatePickerDialog dialog, int year, int month, int day){
 					start.set(year, month, day, 0, 0);
 					if(!endSet || start.before(end)){
 						startSet = true;
 						enableButton(endButton);
 						startButton.setText(getResources().getString(R.string.start_date)+
 								"\n"+format.format(start.getTime())); 
 					} else {
 						Toast.makeText(CreateActivity.this, R.string.invalid_start, Toast.LENGTH_SHORT).show();
 						disableButton(saveButton);
 					}
 				}
 			}, start.get(Calendar.YEAR), start.get(Calendar.MONTH), start.get(Calendar.DAY_OF_MONTH));
 			startPicker.show(fm, "startPicker");
 			return;
 		case R.id.button_end:
 			DatePickerDialog endPicker = new DatePickerDialog();
 			endPicker.initialize(new OnDateSetListener(){
 				public void onDateSet(DatePickerDialog dialog, int year, int month, int day){
 					end.set(year, month, day, 0, 0);
 					if(end.after(start)){
 						endSet = true;
 						enableButton(saveButton);
 						endButton.setText(getResources().getString(R.string.end_date)+
 								"\n"+format.format(end.getTime())); 
 					} else {
 						Toast.makeText(CreateActivity.this, R.string.invalid_end, Toast.LENGTH_SHORT).show();
 						disableButton(saveButton);
 					}
 				}
 			}, start.get(Calendar.YEAR), start.get(Calendar.MONTH), start.get(Calendar.DAY_OF_MONTH));
 			endPicker.show(fm, "endPicker");
 			return;
 		}
 	}
 	
 	/*
 	 * Classes to listen for color changes
 	 */
 	private class CompletedColorChangeListener implements OnColorChangedListener{
 		@Override
 		public void onColorChanged(int color) {
 			completedColor = color;
 		}
 	}
 	
 	private class RemainingColorChangeListener implements OnColorChangedListener{
 		@Override
 		public void onColorChanged(int color) {
 			remainingColor = color;
 		}
 	}
 }
