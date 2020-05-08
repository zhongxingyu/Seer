 package com.networksinmocean.timecard;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.HashMap;
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.app.DatePickerDialog;
 import android.app.Dialog;
 import android.app.DialogFragment;
 import android.app.TimePickerDialog;
 import android.os.Bundle;
 import android.text.format.DateFormat;
 import android.view.View;
 import android.view.WindowManager;
 import android.widget.DatePicker;
 import android.widget.EditText;
 import android.widget.Spinner;
 import android.widget.TimePicker;
 
 public class Add extends Activity {
         
     static EditText dateIn;
     static EditText timeIn;
     static EditText dateOut;
     static EditText timeOut;
     Spinner activity;
     
     DBTools dbTools = new DBTools(this);
     
     public void onCreate(Bundle savedInstanceState){
     	
     	// This hides the top status bar since it is not necessary if the app is running.
         getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
         		WindowManager.LayoutParams.FLAG_FULLSCREEN);
         		    
     	super.onCreate(savedInstanceState);
     	setContentView(R.layout.activity_add_entry);
     	
     	dateIn = (EditText) findViewById(R.id.dateIn);
     	timeIn = (EditText) findViewById(R.id.timeIn);
     	dateOut = (EditText) findViewById(R.id.dateOut);
     	timeOut = (EditText) findViewById(R.id.timeOut);
     	activity = (Spinner) findViewById(R.id.activity);
     	
     }
     
     /* I really hate this entire section, but I don't know Java well enough to 
        figure out how to use each of these once and pass the EditText id in the
        onClick call... FIXME */
     public static class DatePickerFragmentIn extends DialogFragment
 							implements DatePickerDialog.OnDateSetListener {
 
 		@SuppressLint("SimpleDateFormat")
 		@Override
 		public Dialog onCreateDialog(Bundle savedInstanceState) {
 			
 			int year , month, day;
 			// Use the current date as the default values for the picker
 			String dateInText = dateIn.getText().toString();
 			if (dateInText.matches("")) {
 				
 				final Calendar c = Calendar.getInstance();
 				year = c.get(Calendar.YEAR);
 		        month = c.get(Calendar.MONTH);
 		        day = c.get(Calendar.DAY_OF_MONTH);
 		        
 			} else {
 				
 				final Calendar c = Calendar.getInstance();
 				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
 				try {
 					c.setTime(sdf.parse(dateInText));
 				} catch (ParseException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 				
 				year = c.get(Calendar.YEAR);
 		        month = c.get(Calendar.MONTH);
 		        day = c.get(Calendar.DAY_OF_MONTH);
 
 			}
 			
 			// Create a new instance of DatePickerDialog and return it
 			return new DatePickerDialog(getActivity(), this, year, month, day);
 	        
 		}
 		
 		public void onDateSet(DatePicker view, int year, int month, int day) {
 			
 			dateIn.setText(String.valueOf(year) + "-" +   String.valueOf(month + 1 ) + "-" + String.valueOf(day));
 			
 		}
 		
 	}
     
     public static class TimePickerFragmentIn extends DialogFragment
 							implements TimePickerDialog.OnTimeSetListener {
 
 		@SuppressLint("SimpleDateFormat")
 		public Dialog onCreateDialog(Bundle savedInstanceState) {
 			
 			int hour, minute;
 			// Use the current date as the default values for the picker
 			String timeInText = timeIn.getText().toString();
 			if (timeInText.matches("")) {
 				
 				final Calendar c = Calendar.getInstance();
 				hour = c.get(Calendar.HOUR_OF_DAY);
 				minute = 0;
 		        
 			} else {
 				
 				final Calendar c = Calendar.getInstance();
 				SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
 				try {
 					c.setTime(sdf.parse(timeInText));
 				} catch (ParseException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 				
 				hour = c.get(Calendar.HOUR_OF_DAY);
 		        minute = c.get(Calendar.MINUTE);
 
 			}
 		
 			// Create a new instance of TimePickerDialog and return it
 			return new TimePickerDialog(getActivity(), this, hour, minute,
 			DateFormat.is24HourFormat(getActivity()));
 			
 		}
 		
 		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
 
 			timeIn.setText(String.valueOf(hourOfDay) + ":" + pad(minute));
 			
 		}
 	
 	}
     
     @SuppressLint("SimpleDateFormat")
 	public static class DatePickerFragmentOut extends DialogFragment
 	implements DatePickerDialog.OnDateSetListener {
 
 		@Override
 		public Dialog onCreateDialog(Bundle savedInstanceState) {
 		
 			int year , month, day;
 			// Use the current date as the default values for the picker
 			String dateOutText = dateOut.getText().toString();
 			if (dateOutText.matches("")) {
 				
 				final Calendar c = Calendar.getInstance();
 				year = c.get(Calendar.YEAR);
 		        month = c.get(Calendar.MONTH);
 		        day = c.get(Calendar.DAY_OF_MONTH);
 		        
 			} else {
 				
 				final Calendar c = Calendar.getInstance();
 				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
 				try {
 					c.setTime(sdf.parse(dateOutText));
 				} catch (ParseException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 				
 				year = c.get(Calendar.YEAR);
 		        month = c.get(Calendar.MONTH);
 		        day = c.get(Calendar.DAY_OF_MONTH);
 		        
 			}
 		
 		// Create a new instance of DatePickerDialog and return it
 		return new DatePickerDialog(getActivity(), this, year, month, day);
 		
 		}
 		
 		public void onDateSet(DatePicker view, int year, int month, int day) {
 		
 		dateOut.setText(String.valueOf(year) + "-" +   String.valueOf(month + 1 ) + "-" + String.valueOf(day));
 		
 		}
 		
 		}
 		
 		public static class TimePickerFragmentOut extends DialogFragment
 			implements TimePickerDialog.OnTimeSetListener {
 		
 		@SuppressLint("SimpleDateFormat")
 		public Dialog onCreateDialog(Bundle savedInstanceState) {
 		
 			int hour, minute;
 			// Use the current date as the default values for the picker
 			String timeOutText = timeOut.getText().toString();
 			if (timeOutText.matches("")) {
 				
 				final Calendar c = Calendar.getInstance();
 				hour = c.get(Calendar.HOUR_OF_DAY);
 				minute = 0;
 		        
 			} else {
 				
 				final Calendar c = Calendar.getInstance();
 				SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
 				try {
 					c.setTime(sdf.parse(timeOutText));
 				} catch (ParseException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 				
 				hour = c.get(Calendar.HOUR_OF_DAY);
 		        minute = c.get(Calendar.MINUTE);
 
 			}
 		
 		// Create a new instance of TimePickerDialog and return it
 		return new TimePickerDialog(getActivity(), this, hour, minute,
 		DateFormat.is24HourFormat(getActivity()));
 		
 		}
 		
 		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
 		
 		timeOut.setText(String.valueOf(hourOfDay) + ":" + pad(minute));
 		
 		}
 		
 	}
     
     private static String pad(int c) {
 		if (c >= 10)
 		   return String.valueOf(c);
 		else
 		   return "0" + String.valueOf(c);
 	}
 
     public void showDatePickerDialogIn(View v) {
         DialogFragment newFragment = new DatePickerFragmentIn();
         newFragment.show(getFragmentManager(), "datePicker");
     }
     
     public void showTimePickerDialogIn(View v) {
         DialogFragment newFragment = new TimePickerFragmentIn();
         newFragment.show(getFragmentManager(), "timePicker");
     }
     
     public void showDatePickerDialogOut(View v) {
         DialogFragment newFragment = new DatePickerFragmentOut();
         newFragment.show(getFragmentManager(), "datePicker");
     }
     
     public void showTimePickerDialogOut(View v) {
         DialogFragment newFragment = new TimePickerFragmentOut();
         newFragment.show(getFragmentManager(), "timePicker");
     }
     
     public void saveEntry(View view){
     	
     	HashMap<String, String> queryValuesMap = new HashMap<String, String>();
     	
     	String dateTimeIn = dateIn.getText().toString() + " " + timeIn.getText().toString();
     	String dateTimeOut = dateOut.getText().toString() + " " + timeOut.getText().toString();
     	
     	queryValuesMap.put("dateTimeIn", dateTimeIn);
     	queryValuesMap.put("dateTimeOut", dateTimeOut);
     	queryValuesMap.put("activity", activity.getSelectedItem().toString());
     	
     	dbTools.insertPunch(queryValuesMap);
     	
    	finish();
     	
     }
 	
 }
