 package edu.upenn.cis350;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 
 import com.parse.Parse;
 import com.parse.ParseException;
 import com.parse.ParseObject;
 import com.parse.SaveCallback;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.DatePickerDialog;
 import android.app.Dialog;
 import android.app.TimePickerDialog;
 import android.content.ContentValues;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.database.sqlite.SQLiteDatabase;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.DatePicker;
 import android.widget.EditText;
 import android.widget.RadioButton;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.TimePicker;
 import android.widget.Toast;
 
 /* This activity displays the form for creating a new event.
  * Once the event is submitted, it is written to the DB and the 
  * user is shown the ShowEvent view for this Event
  */
 public class CreateNewEvent extends Activity {
 
 	
 	// fields for dateDisplay popup (START DATE FIELDS)
 	private TextView mDateDisplay;
     private Button mPickDate;
     private int mYear;
     private int mMonth;
     private int mDay;
     private int mHour;
     private int mMinute;
 	// fields for dateDisplay popup (END DATE FIELDS)
     private TextView mDateDisplay2;
     private Button mPickDate2;
     private int mYear2;
     private int mMonth2;
     private int mDay2;
     private int mHour2;
     private int mMinute2;
     private CharSequence[] affils;
     private boolean[] affilsChecked;
     private CharSequence[] systems;
     private boolean[] systemsChecked;
     
     //dialog constants
     static final int START_DATE_DIALOG_ID = 0;
     static final int END_DATE_DIALOG_ID = 1;
 	static final int PICK_AFFILS_DIALOG_ID = 2;
 	private static final int PICK_SYS_DIALOG_ID = 3;
 	static final int START_TIME_DIALOG_ID = 4;
 	static final int END_TIME_DIALOG_ID = 5;
 
     
     // the callback received when the user "sets" the date in the dialog (START DATE)
     private DatePickerDialog.OnDateSetListener mDateSetListener =
             new DatePickerDialog.OnDateSetListener() {
 
                 public void onDateSet(DatePicker view, int year, 
                                       int monthOfYear, int dayOfMonth) {
                     mYear = year;
                     mMonth = monthOfYear;
                     mDay = dayOfMonth;
                     showDialog(START_TIME_DIALOG_ID);
                     updateDisplay();
                 }
             };
      // the callback received when the user "sets" the date in the dialog (END DATE)
      private DatePickerDialog.OnDateSetListener mDateSetListener2 =
            new DatePickerDialog.OnDateSetListener() {
 
                public void onDateSet(DatePicker view, int year, 
                                      int monthOfYear, int dayOfMonth) {
                    mYear2 = year;
                    mMonth2 = monthOfYear;
                    mDay2 = dayOfMonth;
                    showDialog(END_TIME_DIALOG_ID);
                    updateDisplay();
                }
            };
      // the callback received when the user "sets" the time in the dialog (start time)      
      private TimePickerDialog.OnTimeSetListener mTimeSetListener =
     	 	new TimePickerDialog.OnTimeSetListener() {
     	 		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
         	        mHour = hourOfDay;
         	        mMinute = minute;
         	        updateDisplay();
         	    }
         	};
      // the callback received when the user "sets" the time in the dialog (end time)              	
      private TimePickerDialog.OnTimeSetListener mTimeSetListener2 =
     	 	new TimePickerDialog.OnTimeSetListener() {
          		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            	        mHour2 = hourOfDay;
            	        mMinute2 = minute;
            	        updateDisplay();
            	    }
            	};           	
 	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 		Parse.initialize(this, "FWyFNrvpkliSb7nBNugCNttN5HWpcbfaOWEutejH", "SZoWtHw28U44nJy8uKtV2oAQ8suuCZnFLklFSk46");
         setContentView(R.layout.eventform);
         
         // capture our View elements
         mDateDisplay = (TextView) findViewById(R.id.startDateDisplay);
         mPickDate = (Button) findViewById(R.id.pickStartDate);
 
         // get the current date
         final Calendar c = Calendar.getInstance();
         mYear = c.get(Calendar.YEAR);
         mMonth = c.get(Calendar.MONTH);
         mDay = c.get(Calendar.DAY_OF_MONTH);
         mHour = c.get(Calendar.HOUR_OF_DAY);
         mMinute = c.get(Calendar.MINUTE);
         
         mDateDisplay2 = (TextView) findViewById(R.id.endDateDisplay);
         mPickDate2 = (Button) findViewById(R.id.pickEndDate);
 
         mYear2 = mYear;
         mMonth2 = mMonth;
         mDay2 = mDay;
         mHour2 = mHour;
         mMinute2 = mMinute;
         // display the current date (this method is below)
         updateDisplay();
         
         //populate spinner
         populateSpinners();
         
         //set up severity buttons
         //TODO closen: add listeners
         final RadioButton radioRed = (RadioButton) findViewById(R.id.radioRed);
         radioRed.setBackgroundColor(Color.RED);
         final RadioButton radioYellow = (RadioButton) findViewById(R.id.radioYellow);
         radioYellow.setBackgroundColor(Color.YELLOW);
         final RadioButton radioGreen = (RadioButton) findViewById(R.id.radioGreen);
         radioGreen.setBackgroundColor(Color.GREEN);
 
     }
     
     // helper method to populate spinners with dummy info
     private void populateSpinners() {
         Spinner spinner = (Spinner) findViewById(R.id.personSpinner1);
         ArrayAdapter <CharSequence> adapter =
         	  new ArrayAdapter <CharSequence> (this, android.R.layout.simple_spinner_item );
         adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
         for(int i = 1; i <= 10; i++){
         	adapter.add("Person " + i);
         }
         spinner.setAdapter(adapter);
         
         Spinner spinner2 = (Spinner) findViewById(R.id.personSpinner2);
         ArrayAdapter <CharSequence> adapter2 =
         	  new ArrayAdapter <CharSequence> (this, android.R.layout.simple_spinner_item );
         adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
         for(int i = 1; i <= 10; i++){
         	adapter2.add("Person " + i);
         }
         spinner2.setAdapter(adapter2);
 	}
 
 	// onClick function of submit button
     public void onCreateEventSubmit(View view){
     	//TODO closen: Phase out intent stuff - use eventPOJO for everything
     	//Intent i = new Intent(this, WhartonComputingCommunicationsActivity.class);
     	
     	final ParseObject event = new ParseObject("Event");
     	EditText temp = (EditText)findViewById(R.id.eventTitle);
     	event.put("title", temp.getText().toString());
     	temp = (EditText)findViewById(R.id.eventDesc);
     	event.put("description", temp.getText().toString());		// EVENT
     	temp = (EditText)findViewById(R.id.eventActions);
     	event.put("actionItems", temp.getText().toString());	// EVENT
     	TextView temp2 = (TextView)findViewById(R.id.startDateDisplay);
     	event.put("startDate", temp2.getText().toString());			// EVENT
     	temp2 = (TextView)findViewById(R.id.endDateDisplay);
     	event.put("endDate", temp2.getText().toString());			// EVENT
 
     	//TODO: Affils + Systems
     	List<String> affiliations = new ArrayList<String>();
     	if(affils != null){
     		for(int x = 0; x < affils.length; x++){				// EVENT
     			if(affilsChecked[x])
     				affiliations.add(affils[x].toString());
     		}
     		event.put("affils", affiliations);
     	}
     	List<String> sys = new ArrayList<String>();
     	if(systems != null){
     		for(int x = 0; x < systems.length; x++){
     			if(systemsChecked[x])
     				sys.add(systems[x].toString());
     		}
     		event.put("systems", sys);
     	}
 
     	//TODO: User linking
 //    	Spinner spin1 = (Spinner)findViewById(R.id.personSpinner1);
 //    	event.setContact1(spin1.getSelectedItem().toString());	// EVENT
 //    	spin1 = (Spinner)findViewById(R.id.personSpinner2);
 //    	event.setContact2(spin1.getSelectedItem().toString());	// EVENT
 
     	if(((RadioButton)findViewById(R.id.radioRed)).isChecked()){
     		event.put("severity", Color.RED);
     	}
     	else if(((RadioButton)findViewById(R.id.radioYellow)).isChecked()){
     		event.put("severity", Color.YELLOW);
     	}
     	else if(((RadioButton)findViewById(R.id.radioGreen)).isChecked()){
     		event.put("severity", Color.GREEN);
     	}
     	else {
     		Toast.makeText(this, "Select an severity code.", Toast.LENGTH_SHORT).show();
     		return;
     	}
     	
     	if(((RadioButton)findViewById(R.id.radioEmergency)).isChecked()){
     		event.put("type", "Emergency");
     	}
     	else if(((RadioButton)findViewById(R.id.radioScheduled)).isChecked()){
     		event.put("type", "Scheduled");
     	} else  {
     		Toast.makeText(this, "Select an event type.", Toast.LENGTH_SHORT).show();
     		return;
     	}
     	
     	final Toast success = Toast.makeText(this, "Event saved.", Toast.LENGTH_SHORT);
     	final Intent i = new Intent(this, ShowEvent.class);
		i.putExtra("eventKey", event.getObjectId());
 
     	final Toast failure = Toast.makeText(this, "Could not save event. Try again.", Toast.LENGTH_SHORT);
 
     	event.saveInBackground(new SaveCallback() {
 			@Override
 			public void done(ParseException e) {
 				if (e == null) {
 					success.show();
 					i.putExtra("eventKey", event.getObjectId());
 			    	startActivity(i);	
 				} else {
 					failure.setText(e.getMessage());
 					failure.show();
 				}
 			}
     	});
     }
     
     // onClick function of pickStartDateButton
     public void showStartDateDialog(View view){
     	showDialog(START_DATE_DIALOG_ID);
     }
     
     // onClick function of pickEndDateButton
     public void showEndDateDialog(View view){
     	showDialog(END_DATE_DIALOG_ID);
     }
     
     // onClick function of pickAffils button
     public void showPickAffilsDialog(View view){
        	showDialog(PICK_AFFILS_DIALOG_ID);
     }
     // onClick function of pickSys button
     public void showPickSysDialog(View view){
     	showDialog(PICK_SYS_DIALOG_ID);
     }
     
     public void showStartTimeDialog(View view){
     	showDialog(START_TIME_DIALOG_ID);
     }
     
     public void showEndTimeDialog(View view){
     	showDialog(END_TIME_DIALOG_ID);
     }
     
     // updates the date in the TextView
     private void updateDisplay() {
         mDateDisplay.setText(
             new StringBuilder()
                     // Month is 0 based so add 1
                     .append(mMonth + 1).append("-")
                     .append(mDay).append("-")
                     .append(mYear).append(" ")
                     .append(pad(mHour)).append(":")
                     .append(pad(mMinute)).append(" "));
         mDateDisplay2.setText(
                 new StringBuilder()
                         // Month is 0 based so add 1
                         .append(mMonth2 + 1).append("-")
                         .append(mDay2).append("-")
                         .append(mYear2).append(" ")
                         .append(pad(mHour2)).append(":")
                         .append(pad(mMinute2)).append(" "));
     }
     
     private static String pad(int c) {
         if (c >= 10)
             return String.valueOf(c);
         else
             return "0" + String.valueOf(c);
     }
 
     // creates dialogs
     @Override
     protected Dialog onCreateDialog(int id) {
         switch (id) {
         case START_DATE_DIALOG_ID:
             return new DatePickerDialog(this,
                         mDateSetListener,
                         mYear, mMonth, mDay);
         case END_DATE_DIALOG_ID:
         		return new DatePickerDialog(this,
         					mDateSetListener2,
         					mYear2, mMonth2, mDay2);
         case START_TIME_DIALOG_ID:
         		return new TimePickerDialog(this,
                     mTimeSetListener, mHour, mMinute, false);
         case END_TIME_DIALOG_ID:
     		return new TimePickerDialog(this,
                 mTimeSetListener2, mHour2, mMinute2, false);
         case PICK_AFFILS_DIALOG_ID:
         	final CharSequence[] items = {"Group 1", "Group 2", "Group 3"};
         	affils = items;
         	affilsChecked = new boolean[items.length];
 
         	AlertDialog.Builder builder = new AlertDialog.Builder(this);
         	builder.setTitle("Pick Affiliations");
         	builder.setMultiChoiceItems(items, null, new DialogInterface.OnMultiChoiceClickListener() {
         	    public void onClick(DialogInterface dialog, int item, boolean isChecked) {
         	        Toast.makeText(getApplicationContext(), items[item], Toast.LENGTH_SHORT).show();
         	        affilsChecked[item] = isChecked;
         	    }
         	});
         	builder.setPositiveButton("Finished", new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int id) {
                 	dialog.dismiss();
                }
            });
         	AlertDialog alert = builder.create();
         	return alert;
         case PICK_SYS_DIALOG_ID:
         	final CharSequence[] items2 = {"System 1", "System 2", "System 3"};
         	systems = items2;
         	systemsChecked = new boolean[items2.length];
 
         	AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
         	builder2.setTitle("Pick Affected Systems");
         	builder2.setMultiChoiceItems(items2, null, new DialogInterface.OnMultiChoiceClickListener() {
         	    public void onClick(DialogInterface dialog, int item, boolean isChecked) {
         	        Toast.makeText(getApplicationContext(), items2[item], Toast.LENGTH_SHORT).show();
         	        systemsChecked[item] = isChecked;
         	    }
         	});
         	builder2.setPositiveButton("Finished", new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int id) {
                 	dialog.dismiss();
                }
            });
         	AlertDialog alert2 = builder2.create();
         	return alert2;
         }
         return null;
     }
     
     // inserts the event into the SQLite DB
     // TODO: Change to MySQL DB
     public void insertEvent(EventPOJO event){
 
 		// First we have to open our DbHelper class by creating a new object of that
 		AndroidOpenDbHelper androidOpenDbHelperObj = new AndroidOpenDbHelper(this);
 
 		// Then we need to get a writable SQLite database, because we are going to insert some values
 		// SQLiteDatabase has methods to create, delete, execute SQL commands, and perform other common database management tasks.
 		SQLiteDatabase sqliteDatabase = androidOpenDbHelperObj.getWritableDatabase();
 		androidOpenDbHelperObj.createEventsTable(sqliteDatabase);
 		androidOpenDbHelperObj.createMessagesTable(sqliteDatabase);
 		
 		// ContentValues class is used to store a set of values that the ContentResolver can process.
 		ContentValues contentValues = new ContentValues();
 
 		// Get values from the POJO class and passing them to the ContentValues class
 		contentValues.put(AndroidOpenDbHelper.COLUMN_NAME_EVENT_TITLE, event.getEventTitle());
 		contentValues.put(AndroidOpenDbHelper.COLUMN_NAME_EVENT_DESC, event.getEventDesc());
 		contentValues.put(AndroidOpenDbHelper.COLUMN_NAME_EVENT_ACTIONS, event.getEventActions());
 		contentValues.put(AndroidOpenDbHelper.COLUMN_NAME_EVENT_START, event.getStart());
 		contentValues.put(AndroidOpenDbHelper.COLUMN_NAME_EVENT_END, event.getEnd());
 		StringBuffer temp = new StringBuffer();
 		for(String s : event.getAffils()){
 			temp.append(s + "\t");
 		}
 		contentValues.put(AndroidOpenDbHelper.COLUMN_NAME_EVENT_AFFILS, temp.toString());
 		temp = new StringBuffer();
 		for(String s : event.getSystems()){
 			temp.append(s + "\t");
 		}
 		contentValues.put(AndroidOpenDbHelper.COLUMN_NAME_EVENT_SYSTEMS, temp.toString());
 		contentValues.put(AndroidOpenDbHelper.COLUMN_NAME_EVENT_CONTACT1, event.getContact1());
 		contentValues.put(AndroidOpenDbHelper.COLUMN_NAME_EVENT_CONTACT2, event.getContact2());
 		contentValues.put(AndroidOpenDbHelper.COLUMN_NAME_EVENT_SEVERITY, event.getSeverity());
 		contentValues.put(AndroidOpenDbHelper.COLUMN_NAME_EVENT_TYPE, event.getType());
 
 
 
 
 		// Now we can insert the data in to relevant table
 		// I am going pass the id value, which is going to change because of our insert method, to a long variable to show in Toast
 		long affectedColumnId = sqliteDatabase.insert(AndroidOpenDbHelper.TABLE_NAME_EVENTS, null, contentValues);
 
 		// It is a good practice to close the database connections after you have done with it
 		//sqliteDatabase.delete(AndroidOpenDbHelper.TABLE_NAME_EVENTS, null, null);
 		sqliteDatabase.close();
 
 		// I am not going to do the retrieve part in this post. So this is just a notification for satisfaction 
 //		Toast.makeText(this, "Values inserted column ID is :" + affectedColumnId, Toast.LENGTH_SHORT).show();
 		Toast.makeText(this, "Event created.", Toast.LENGTH_SHORT).show();
 
 	}
     
 }
