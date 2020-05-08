 /*
  * Copyright (C) 2008 Google Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.example.ass2note.notepad;
 
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.DatePickerDialog;
 import android.app.Dialog;
 import android.app.TimePickerDialog;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.Button;
 import android.widget.DatePicker;
 import android.widget.EditText;
 import android.widget.ImageButton;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 import android.widget.TimePicker;
 import android.widget.Toast;
 import android.widget.ToggleButton;
 
 import com.example.ass2note.R;
 import com.example.ass2note.location.ConnectionService;
 import com.example.ass2note.location.GoogleMapsActivity;
 
 public class NoteEdit extends Activity {
 	private static final int MAPSINTENT_ID = 1;
 	private EditText mTitleText;
 	private EditText mBodyText;
 	private String lati = "lat";
 	private String longi = "long";
 	private long time = 0;
 	private String positionReminder = "false", snippet="";
 	private Long mRowId;
 	private NotesDbAdapter mDbHelper;
 	private IntentFilter intentFilter;
 	private boolean gpsEnabled=false, networkEnabled=false;
 	long da =0;
 	
 
 	Calendar myCalendar = Calendar.getInstance();
 
 	DatePickerDialog.OnDateSetListener d = new DatePickerDialog.OnDateSetListener() {
 		public void onDateSet(DatePicker view, int year, int monthOfYear,
 				int dayOfMonth) {
 			myCalendar.set(Calendar.YEAR, year);
 			myCalendar.set(Calendar.MONTH, monthOfYear);
 			myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
 
 			new TimePickerDialog(NoteEdit.this, t,
 					myCalendar.get(Calendar.HOUR_OF_DAY),
 					myCalendar.get(Calendar.MINUTE), true).show();
 		}
 	};
 
 	TimePickerDialog.OnTimeSetListener t = new TimePickerDialog.OnTimeSetListener() {
 		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
 
 			myCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
 			myCalendar.set(Calendar.MINUTE, minute);
 			Date dat = myCalendar.getTime();
 			SimpleDateFormat ss = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 
 			da = myCalendar.getTimeInMillis();
 
 			updateTime(da);
 
 		}
 
 	};
 
 	private void updateTime(long da) {
 		// SimpleDateFormat ss = new SimpleDateFormat("YYYY-MM-DD HH:mm:SS");
 		SimpleDateFormat dateFormat = new SimpleDateFormat(
 				"dd-MM-yyyy HH:mm:ss");
 
 	//	mydateview.setText(dateFormat.format(da));
 		saveTime(da);
 
 	}
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		mDbHelper = new NotesDbAdapter(this);
 		mDbHelper.open();
 		setContentView(R.layout.note_edit);
 		setTitle(R.string.edit_note);
 
 		ToggleButton positionToggle = (ToggleButton) findViewById(R.id.showAlarmNoteInfo);
 		positionToggle.setSaveEnabled(false);
 		
 		intentFilter = new IntentFilter("com.example.ass2note.notepad.NoteEdit.connectionReceiver");
 		
 		da = myCalendar.getTimeInMillis();
 		mTitleText = (EditText) findViewById(R.id.title);
 		mBodyText = (EditText) findViewById(R.id.body);
 
 
 		mRowId = (savedInstanceState == null) ? null : (Long) 
 				savedInstanceState.getSerializable(NotesDbAdapter.KEY_ROWID);
 		if (mRowId == null) {
 			Bundle extras = getIntent().getExtras();
 			mRowId = extras != null ? extras.getLong(NotesDbAdapter.KEY_ROWID)
 					: null;
 		}
 
 		populateFields();
 		
 		// Set onClickListeners on buttons.
 		initiateButtons();
 		
 		isAlarmSet();
 	}
 	
 	@Override
 	protected void onStart() {
 		super.onStart();
 		RelativeLayout focuslayout = (RelativeLayout) findViewById(R.id.RequestFocusLayout);
 		focuslayout.requestFocus();
 	}
 
 	// TO SELF MAKE TEXT FIELDS EDDITABLE SO WHEN CLICKED DATE AND TIME
 	// INPUT SHOULD BE TRIGGERED
 	// TOOO BE DELETED
 	// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
 	// or chanced
 	private void initiateButtons(){
 		Button confirmButton = (Button) findViewById(R.id.confirm);
 		Button alarmButton = (Button) findViewById(R.id.newNoteAlarm);
 		
 		
 		confirmButton.setOnClickListener(new View.OnClickListener() {
 			// ON click CONFIRM
 			public void onClick(View view) {
 				setResult(RESULT_OK);
 				finish();
 			}
 		});
 		
 		alarmButton.setOnClickListener(new View.OnClickListener() {
 			// ON click ALARM
 			public void onClick(View v) {
 				
 				final Dialog dialog = new Dialog(NoteEdit.this);
 			    // Set the dialog title
 				dialog.setTitle(R.string.alarm);
 				//dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
 				dialog.setContentView(R.layout.alert_dialog);
 				dialog.show();
 			    
 
 				ImageButton positionButton = (ImageButton) dialog.findViewById(R.id.positionButton);
 				ImageButton timeButton = (ImageButton) dialog.findViewById(R.id.timeButton);
 				timeButton.setOnClickListener(new View.OnClickListener() {
 					public void onClick(View v) {
 						new DatePickerDialog(NoteEdit.this, d, myCalendar
 								.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
 								myCalendar.get(Calendar.DAY_OF_MONTH)).show();
 						
 						dialog.dismiss();
 					}
 				});
 				
 				positionButton.setOnClickListener(new View.OnClickListener() {
 					public void onClick(View v) {
 						startGoogleMaps(v);
 						dialog.dismiss();
 					}
 				});
 			}
 		});
 	}
 	
	
 
 	// Gets the values from the database
 	private void populateFields() {
 		if (mRowId != null) {
 
 			Cursor note = mDbHelper.fetchNote(mRowId);
 			startManagingCursor(note);
 			mTitleText.setText(note.getString(note
 					.getColumnIndexOrThrow(NotesDbAdapter.KEY_TITLE)));
 			mBodyText.setText(note.getString(note
 					.getColumnIndexOrThrow(NotesDbAdapter.KEY_BODY)));
 			positionReminder = note.getString(note.getColumnIndexOrThrow(NotesDbAdapter.KEY_POSITION_REMINDER));
 
 			// Toast.makeText(this, note.getString(
 			// note.getColumnIndexOrThrow(NotesDbAdapter.KEY_TEST)) ,
 			// Toast.LENGTH_SHORT).show();
 			time = note.getLong(note.getColumnIndexOrThrow(NotesDbAdapter.KEY_TIME));
 
 			// View the Date set in the format .....
 			SimpleDateFormat dateFormat = new SimpleDateFormat(
 					"dd-MM-yyyy HH:mm");
 //			mydateview.setText(dateFormat.format(temp));
 
 			// Fetch the position values from the database:
 			lati = note.getString(note
 					.getColumnIndexOrThrow(NotesDbAdapter.KEY_LATI));
 			longi = note.getString(note
 					.getColumnIndexOrThrow(NotesDbAdapter.KEY_LONG));
 			snippet = note.getString(note.getColumnIndexOrThrow(NotesDbAdapter.KEY_SNIPPET));
 		}
 	}
 
 	@Override
 	protected void onSaveInstanceState(Bundle outState) {
 		super.onSaveInstanceState(outState);
 		saveState();
 		outState.putSerializable(NotesDbAdapter.KEY_ROWID, mRowId);
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 		unregisterReceiver(connectionReceiver);
 		saveState();
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		registerReceiver(connectionReceiver, intentFilter);
 		populateFields();
 	}
 
 	private void saveTime(long time) {
 		if (mRowId == null) {} 
 		else mDbHelper.updateTime(mRowId, time);
 	}
 
 	// Saves the values to the database
 	private void saveState() {
 		String title = mTitleText.getText().toString();
 		String body = mBodyText.getText().toString();
 
 		String latitude = lati;
 		String longitude = longi;
 
 		if (mRowId == null) {
 			long id = mDbHelper.createNote(title, body,
 					da , longitude, latitude,
 					positionReminder, snippet);
 			if (id > 0) {
 				mRowId = id;
 			}
 		} else {
 			mDbHelper.updateNote(mRowId, title, body, longitude, latitude,
 					positionReminder, snippet);
 
 		}
 	}
 
 	/**
 	 * Function. Receives information from GoogleMapsActivity when the activity
 	 * finishes. Both latitude and longitude from the preferred position from 
 	 * the user is available here.
 	 */
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		super.onActivityResult(requestCode, resultCode, data);
 		Log.i("NoteEdit", "onActivityResult");
 		
 		// The result came from GoogleMapsActivity:
 		if (requestCode == MAPSINTENT_ID)
 			switch (resultCode) {
 			// A new location was selected:
 			case Activity.RESULT_OK: {
 				// Fetch the new data:
 				lati = data.getStringExtra("latitude");
 				longi = data.getStringExtra("longitude");
 				snippet = data.getStringExtra("snippet");
 				positionReminder = "true";
 				
 				// Save the data in the database.
 				saveState();
 				isAlarmSet();
 				break;
 			}
 			// Unexpected occurrence happened or no new location was selected: 
 			case Activity.RESULT_CANCELED: {
 				break;
 			}
 			} // end switch
 	} // end onActivityResult
 	
 	
 	
 	/**
 	 * Method for starting GoogleMapsActivity. The latitude and longitude 
 	 * values in the database for this note is sent to the activity, and the
 	 * activity is started with startActivityForResult.
 	 * @param view
 	 */
 	public void startGoogleMaps(View view) {
 		/*Intent i = new Intent(NoteEdit.this, GoogleMapsActivity.class);
 		i.putExtra("LATITUDE", lati);
 		i.putExtra("LONGITUDE", longi);
 		startActivityForResult(i, MAPSINTENT_ID);
 	*/
 		checkConnection();
 		}
 	
 	private void checkConnection(){
 		Intent intent = new Intent(NoteEdit.this, ConnectionService.class);
 		intent.putExtra("fromActivity", "NoteEdit");
 		startService(intent);
 	}
 
 	// TODO: Tell the user how this function works:
 	private BroadcastReceiver connectionReceiver = new BroadcastReceiver(){
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			gpsEnabled = intent.getBooleanExtra("gpsEnabled", false);
 			networkEnabled = intent.getBooleanExtra("networkEnabled", false);
 				
 			if(networkEnabled){
 				Intent i = new Intent(NoteEdit.this, GoogleMapsActivity.class);
 				i.putExtra("LATITUDE", lati);
 				i.putExtra("LONGITUDE", longi);
 				startActivityForResult(i, MAPSINTENT_ID);
 			}else
 				alertToast(R.string.network_alert+"");
 			
 		}	
 	};
 	
 	private void alertToast(String message){
 		Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
 	}
 	
 	/*private void alertGPSConnection(){
 		AlertDialog.Builder altDialog = new AlertDialog.Builder(this);
 		altDialog.setMessage("Please start your GPS and try again. The GPS " +
 				"needs to be ON all the time for this function to work.");
 		altDialog.setNeutralButton("OK",
 				new DialogInterface.OnClickListener() {
 					// @Override
 					public void onClick(DialogInterface dialog, int which) {
 			//			enableLocationSettings();
 					}
 				});
 		altDialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int id) {
                 // User cancelled the dialog
             }
         });
 		altDialog.show();
 	}*/
 	
 	/*private void enableLocationSettings() {
 		Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
 		startActivity(settingsIntent);
 	}*/
 	
 	public void showAlarmInfo(View view){
     	View v = (View)findViewById(R.id.alarmInfo);
 	    boolean on = ((ToggleButton) view).isChecked();
 	    if(on) v.setVisibility(View.VISIBLE);
 	    else v.setVisibility(View.GONE);
 	}
 	
 	public void changePositionStatus(View view){
 		boolean on = ((ToggleButton) view).isChecked();
 		
 		// If the toggle was set to "on":
 		if(on) { 
 			mDbHelper.updatePositionNotification(mRowId, "true");
 			
 			// If the note was previously initiated with latitude and longitude:
 			if(!longi.contains("long")) {
 				setPositionInfo(true, snippet);
 				positionReminder = "true";
 			}
 			
 			// If the note does not have valid latitude and longitude stored:
 			else{
 				System.out.println("not valid lati");
 				setPositionInfo(false, "");
 				
 				
 				 // Use the Builder class for convenient dialog construction
 		        AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		        builder.setMessage("Would you like to be reminded by position?")
 		               .setPositiveButton("Start Google Maps!", new DialogInterface.OnClickListener() {
 		                   public void onClick(DialogInterface dialog, int id) {
 		                       // FIRE ZE MISSILES!
 		                   }
 		               })
 		               .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
 		                   public void onClick(DialogInterface dialog, int id) {
 		                       // User cancelled the dialog
 		                   }
 		               });
 		        // Create the AlertDialog object and return it
 		        builder.show();
 			}
 		}
 		// If the toggle was set to off:
 		else {
 			mDbHelper.updatePositionNotification(mRowId, "false");
 			positionReminder = "false";
 			setPositionInfo(false, snippet);
 		}
 	}
 	
 	// TODO: Finish this
 	public void changeTimeStatus(View view){
 		//boolean on = ((ToggleButton) view).isChecked();
 	}
 	
 	// TODO: add time check here
 	private void isAlarmSet(){
 		ToggleButton showAlarmInfo = (ToggleButton)findViewById(R.id.showAlarmNoteInfo);
 		
 		if(!longi.contains("long")){
 			showAlarmInfo.setVisibility(View.VISIBLE);
 			
 			if(positionReminder.contains("true")) setPositionInfo(true, snippet);
 			else setPositionInfo(false, snippet);
 		}
 	}
 	
 	private void setPositionInfo(boolean checked, String text){
 		ToggleButton alarmPosition = (ToggleButton)findViewById(R.id.toggleAlarmPosition);
 		TextView alarmPositionInfo = (TextView)findViewById(R.id.alarmPositionInfo);
 		
 		alarmPosition.setChecked(checked);
 		alarmPositionInfo.setText(text);
 	}
 }
