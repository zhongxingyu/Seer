 package edu.upenn.cis350;
 
 import java.util.Calendar;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.DatePickerDialog;
 import android.app.Dialog;
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
 import android.widget.RadioGroup;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class CreateNewEvent extends Activity {
 
 	
 	// fields for dateDisplay popup (START DATE FIELDS)
 	private TextView mDateDisplay;
     private Button mPickDate;
     private int mYear;
     private int mMonth;
     private int mDay;
 	// fields for dateDisplay popup (END DATE FIELDS)
     private TextView mDateDisplay2;
     private Button mPickDate2;
     private int mYear2;
     private int mMonth2;
     private int mDay2;
     private CharSequence[] affils;
     private boolean[] affilsChecked;
     private CharSequence[] systems;
     private boolean[] systemsChecked;
     
     private String uname;
 
     //dialog constants
     static final int START_DATE_DIALOG_ID = 0;
     static final int END_DATE_DIALOG_ID = 1;
 	static final int PICK_AFFILS_DIALOG_ID = 2;
 	private static final int PICK_SYS_DIALOG_ID = 3;
 
     
     // the callback received when the user "sets" the date in the dialog (START DATE)
     private DatePickerDialog.OnDateSetListener mDateSetListener =
             new DatePickerDialog.OnDateSetListener() {
 
                 public void onDateSet(DatePicker view, int year, 
                                       int monthOfYear, int dayOfMonth) {
                     mYear = year;
                     mMonth = monthOfYear;
                     mDay = dayOfMonth;
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
                    updateDisplay();
                }
            };
 	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.eventform);
         
         Bundle extras = this.getIntent().getExtras();
         if(extras != null){
            	uname = extras.getString("user");
         }
         
         // capture our View elements
         mDateDisplay = (TextView) findViewById(R.id.startDateDisplay);
         mPickDate = (Button) findViewById(R.id.pickStartDate);
 
         // get the current date
         final Calendar c = Calendar.getInstance();
         mYear = c.get(Calendar.YEAR);
         mMonth = c.get(Calendar.MONTH);
         mDay = c.get(Calendar.DAY_OF_MONTH);
         
         mDateDisplay2 = (TextView) findViewById(R.id.endDateDisplay);
         mPickDate2 = (Button) findViewById(R.id.pickEndDate);
 
         mYear2 = mYear;
         mMonth2 = mMonth;
         mDay2 = mDay;
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
     	EventPOJO event = new EventPOJO();
     	Intent i = new Intent(this, ShowEvent.class);
     	EditText temp = (EditText)findViewById(R.id.eventTitle);
     	event.setEventTitle(temp.getText().toString());		// EVENT
     	temp = (EditText)findViewById(R.id.eventDesc);
     	event.setEventDesc(temp.getText().toString());		// EVENT
     	temp = (EditText)findViewById(R.id.eventActions);
     	event.setEventActions(temp.getText().toString());	// EVENT
     	TextView temp2 = (TextView)findViewById(R.id.startDateDisplay);
     	event.setStart(temp2.getText().toString());			// EVENT
     	temp2 = (TextView)findViewById(R.id.endDateDisplay);
     	event.setEnd(temp2.getText().toString());			// EVENT
     	if(affils != null){
     		for(int x = 0; x < affils.length; x++){				// EVENT
     			if(affilsChecked[x])
     				event.addToAffils(affils[x].toString());
     		}
     	}
     	if(systems != null){
     		for(int x = 0; x < systems.length; x++){
     			if(systemsChecked[x])
     				event.addToSystems(systems[x].toString());
     		}
     	}														// EVENT
     	Spinner spin1 = (Spinner)findViewById(R.id.personSpinner1);
     	event.setContact1(spin1.getSelectedItem().toString());	// EVENT
     	spin1 = (Spinner)findViewById(R.id.personSpinner2);
     	event.setContact2(spin1.getSelectedItem().toString());	// EVENT
     	if(((RadioButton)findViewById(R.id.radioRed)).isChecked()){
     		event.setSeverity(Color.RED);						// EVENT
     	}
     	else if(((RadioButton)findViewById(R.id.radioYellow)).isChecked()){
     		event.setSeverity(Color.YELLOW);					// EVENT
     	}
     	else if(((RadioButton)findViewById(R.id.radioGreen)).isChecked()){
     		event.setSeverity(Color.GREEN);						// EVENT
     	}
     	else {
    		Toast.makeText(this, "Select an severity code.", Toast.LENGTH_SHORT).show();
    		return;
     	}
     	
     	if(((RadioButton)findViewById(R.id.radioEmergency)).isChecked()){
     		event.setType("Emergency");
     	}
     	else if(((RadioButton)findViewById(R.id.radioScheduled)).isChecked()){
     		event.setType("Scheduled");					// EVENT
    	} else  {
    		Toast.makeText(this, "Select an event type.", Toast.LENGTH_SHORT).show();
    		return;
     	}
     	i.putExtra("eventPOJO", event);
     	i.putExtra("user", uname);
     	insertEvent(event);
     	startActivity(i);
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
     
     public void showPickSysDialog(View view){
     	showDialog(PICK_SYS_DIALOG_ID);
     }
     
     // updates the date in the TextView
     private void updateDisplay() {
         mDateDisplay.setText(
             new StringBuilder()
                     // Month is 0 based so add 1
                     .append(mMonth + 1).append("-")
                     .append(mDay).append("-")
                     .append(mYear).append(" "));
         mDateDisplay2.setText(
                 new StringBuilder()
                         // Month is 0 based so add 1
                         .append(mMonth2 + 1).append("-")
                         .append(mDay2).append("-")
                         .append(mYear2).append(" "));
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
     
     public void insertEvent(EventPOJO event){
 
 		// First we have to open our DbHelper class by creating a new object of that
 		AndroidOpenDbHelper androidOpenDbHelperObj = new AndroidOpenDbHelper(this);
 
 		// Then we need to get a writable SQLite database, because we are going to insert some values
 		// SQLiteDatabase has methods to create, delete, execute SQL commands, and perform other common database management tasks.
 		SQLiteDatabase sqliteDatabase = androidOpenDbHelperObj.getWritableDatabase();
 		androidOpenDbHelperObj.createEventsTable(sqliteDatabase);
 
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
 		Toast.makeText(this, "Values inserted column ID is :" + affectedColumnId, Toast.LENGTH_SHORT).show();
 
 	}
     
 }
