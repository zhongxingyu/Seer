 package edu.upenn.cis350;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import com.parse.FindCallback;
 import com.parse.GetCallback;
 import com.parse.Parse;
 import com.parse.ParseException;
 import com.parse.ParseObject;
 import com.parse.ParsePush;
 import com.parse.ParseQuery;
 import com.parse.ParseUser;
 import com.parse.PushService;
 import com.parse.SaveCallback;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.DatePickerDialog;
 import android.app.Dialog;
 import android.app.ProgressDialog;
 import android.app.TimePickerDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
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
 
 public class EditEvent extends Activity {
 
 	private ParseObject event;
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
     private Map<String, String> userIdMap = new HashMap<String, String>();
     private Map<String, String> fullNameMap = new HashMap<String, String>();
 
     private Date date1;
     private Date date2;
     
     private ProgressDialog dialog;
     
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
 			date1 = new Date(year - 1900, monthOfYear, dayOfMonth);                    
 			showDialog(START_TIME_DIALOG_ID);
 			//updateDisplay();
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
 			date2 = new Date(year - 1900, monthOfYear, dayOfMonth);
 			showDialog(END_TIME_DIALOG_ID);
 			//updateDisplay();
 		}
 	};
 	// the callback received when the user "sets" the time in the dialog (start time)      
 	private TimePickerDialog.OnTimeSetListener mTimeSetListener =
 			new TimePickerDialog.OnTimeSetListener() {
 		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
 			mHour = hourOfDay;
 			mMinute = minute;
 			date1.setHours(hourOfDay);
 			date1.setMinutes(minute);
 			updateDisplay();
 		}
 	};
 	// the callback received when the user "sets" the time in the dialog (end time)              	
 	private TimePickerDialog.OnTimeSetListener mTimeSetListener2 =
 			new TimePickerDialog.OnTimeSetListener() {
 		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
 			mHour2 = hourOfDay;
 			mMinute2 = minute;
 			date2.setHours(hourOfDay);
 			date2.setMinutes(minute);
 			updateDisplay();
 		}
 	};        
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		Parse.initialize(this, "FWyFNrvpkliSb7nBNugCNttN5HWpcbfaOWEutejH", "SZoWtHw28U44nJy8uKtV2oAQ8suuCZnFLklFSk46");
         setContentView(R.layout.event_form);
         Bundle extras = this.getIntent().getExtras();
         if(extras != null){
         	//event = (EventPOJO)extras.get("eventPOJO");
            	//uname = extras.getString("user");
         	ParseQuery query = new ParseQuery("Event");
         	
         	final Toast toast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
 			dialog = ProgressDialog.show(this, "", 
                     "Loading. Please wait...", true);
 			query.getInBackground(extras.getString("eventKey"), new GetCallback() {
 
 				@Override
 				public void done(ParseObject event1, ParseException e) {
 					if (event1 == null) {
 						toast.setText(e.getMessage());
 						toast.show();
 					} else {
 						event = event1;
 						EditText temp = (EditText)findViewById(R.id.eventTitle);
 			        	temp.setText(event.getString("title"));
 			        	temp = (EditText)findViewById(R.id.eventDesc);
 			        	temp.setText(event.getString("description"));
 			        	//temp = (TextView)findViewById(R.id.eventActionsText);
 			        	//temp.setText(event.getString("actionItems" + "\n"));
 			        	SimpleDateFormat formatter = new SimpleDateFormat();
 			        	TextView temp2 = (TextView)findViewById(R.id.startDateDisplay);
 			        	date1 = new Date(event.getLong("startDate"));
 			        	temp2.setText(formatter.format(date1));
 			        	temp2 = (TextView)findViewById(R.id.endDateDisplay);
 			        	date2 = new Date(event.getLong("endDate"));
 			        	temp2.setText(formatter.format(date2));
 			        	populateSpinners();
 			        	/*
 =======
 						temp.setText(event.getString("title"));
 						temp = (EditText)findViewById(R.id.eventDesc);
 						temp.setText(event.getString("description"));
 						//temp = (TextView)findViewById(R.id.eventActionsText);
 						//temp.setText(event.getString("actionItems" + "\n"));
 						TextView temp2 = (TextView)findViewById(R.id.startDateDisplay);
 						date1 = new Date(event.getLong("startDate"));
 						temp2.setText(date1.toString());
 						temp2 = (TextView)findViewById(R.id.endDateDisplay);
 						date2 = new Date(event.getLong("endDate"));
 						temp2.setText(date2.toString());
 						populateSpinners();
 						/*
 >>>>>>> 8c77665cd1807a15a8d0a0454be5c9913bc5f175
 			        	temp = (TextView)findViewById(R.id.affilsText);
 
 			        	List<String> affilList = event.getList("affils");
 			        	StringBuilder affilText = new StringBuilder();
 			        	if(affilList != null){
 			        		for(String s : affilList){
 			        			affilText.append(s + "\t");
 			        		}
 			        		temp.setText(affilText.toString());
 			        	}
 			        	temp = (TextView)findViewById(R.id.systemsText);
 
 			        	List<String> systemList = event.getList("systems");
 			        	StringBuilder systemText = new StringBuilder();
 			        	if(systemList != null){
 			        		for(String s : systemList){
 			        			systemText.append(s + "\t");
 			        		}
 			        		temp.setText(systemText.toString());
 			        	}
 						 */	
 						final RadioButton radioRed = (RadioButton) findViewById(R.id.radioRed);
 						radioRed.setBackgroundColor(Color.RED);
 						final RadioButton radioYellow = (RadioButton) findViewById(R.id.radioYellow);
 						radioYellow.setBackgroundColor(Color.YELLOW);
 						final RadioButton radioGreen = (RadioButton) findViewById(R.id.radioGreen);
 						radioGreen.setBackgroundColor(Color.GREEN);
 						int col = event.getInt("severity");
 						RadioButton tempRad;
 						//green
 						if(col == -16711936){
 							radioGreen.setChecked(true);
 						}
 						//yellow
 						else if(col == -256){
 							radioYellow.setChecked(true);
 						}
 						//red
 						else if(col == -65536){
 							radioRed.setChecked(true);
 						}
 
 						String type = event.getString("type");
 						if(type.equals("Scheduled")){
 							tempRad = (RadioButton)findViewById(R.id.radioScheduled);
 							tempRad.setChecked(true);
 						}
 						else if(type.equals("Emergency")){
 							tempRad = (RadioButton)findViewById(R.id.radioEmergency);
 							tempRad.setChecked(true);
 						}
 					}
 				}
 
 			});
 
 
 
 		}
 
 		mDateDisplay = (TextView) findViewById(R.id.startDateDisplay);
 		mPickDate = (Button) findViewById(R.id.pickStartDate);
 		mDateDisplay2 = (TextView) findViewById(R.id.endDateDisplay);
 		mPickDate2 = (Button) findViewById(R.id.pickEndDate);
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
 	}
 
 	private void populateSpinners() {
 
 		final Spinner spinner = (Spinner) findViewById(R.id.personSpinner1);
 		final ArrayAdapter <CharSequence> adapter =
 				new ArrayAdapter <CharSequence> (this, android.R.layout.simple_spinner_item );
 		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 
 		final Spinner spinner2 = (Spinner) findViewById(R.id.personSpinner2);
 		final ArrayAdapter <CharSequence> adapter2 =
 				new ArrayAdapter <CharSequence> (this, android.R.layout.simple_spinner_item );
 		adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 
 		ParseQuery query = new ParseQuery("_User");
 		query.orderByAscending("lname");
 
 		final Toast toast = Toast.makeText(this, "", Toast.LENGTH_SHORT); 
 
 		query.findInBackground(new FindCallback() {
 
 			@Override
 			public void done(List<ParseObject> contactList, ParseException e) {
 				if (e == null) {
 					int pos1 = 0;
 					int pos2 = 0;
 					boolean found1 = false;
 					boolean found2 = false;
 					for(ParseObject obj : contactList){
 						// typesafe??
 						String formattedName = obj.getString("lname") + ", " + obj.getString("fname");
 						adapter.add(formattedName);
 						adapter2.add(formattedName);
 						userIdMap.put(formattedName, obj.getObjectId());
 						fullNameMap.put(formattedName, obj.getString("fullName"));
 						if(fullNameMap.get(formattedName).equals(event.getString("contact1")))
 							found1 = true;
 						if(!found1)
 							pos1++;
 
 						if(fullNameMap.get(formattedName).equals(event.getString("contact2")))
 							found2 = true;
 						if(!found2)
 							pos2++;
 					}
 			        spinner.setAdapter(adapter);
 			        spinner2.setAdapter(adapter2);
 			        spinner.setSelection(pos1);
 			        spinner2.setSelection(pos2);
 			        dialog.cancel();
 				} else {
 					toast.setText("Error: " + e.getMessage());
 					toast.show();
 					return;
 				}
 
 			}
 
 		});
 
 	}
 
 	public void onCreateEventSubmit(View view){
 
 		EditText temp = (EditText)findViewById(R.id.eventTitle);
 		event.put("title", temp.getText().toString());
 		temp = (EditText)findViewById(R.id.eventDesc);
 		event.put("description", temp.getText().toString());		// EVENT
 		//temp = (EditText)findViewById(R.id.eventActions);
 		//event.put("actionItems", temp.getText().toString());	// EVENT
 		TextView temp2 = (TextView)findViewById(R.id.startDateDisplay);
 		event.put("startDate", date1.getTime());
 		temp2 = (TextView)findViewById(R.id.endDateDisplay);
 		event.put("endDate", date2.getTime());
 
 		//TODO: Affils + Systems
 		/*
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
 		 */
 
 		//TODO: User linking
 		Spinner spin1 = (Spinner)findViewById(R.id.personSpinner1);
 		String contact1 = spin1.getSelectedItem().toString();
 		event.put("contact1", fullNameMap.get(contact1));
 		event.put("contact1ID", userIdMap.get(contact1));
 		spin1 = (Spinner)findViewById(R.id.personSpinner2);
 		String contact2 = spin1.getSelectedItem().toString();
 		event.put("contact2", fullNameMap.get(contact2));	// EVENT
 		event.put("contact2ID", userIdMap.get(contact2));
 
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
 
 		final Toast failure = Toast.makeText(this, "Could not save event. Try again.", Toast.LENGTH_SHORT);
 
 		event.saveInBackground(new SaveCallback() {
 			@Override
 			public void done(ParseException e) {
 				if (e == null) {
 					success.show();
 					PushUtils.createEventChangedPush(getApplicationContext(), event.getObjectId(), event);
 					
 					ParseUser user = ParseUser.getCurrentUser();
 					String currentId = user.getObjectId();
 					
 					String userId1 = event.getString("contact1ID");
 					String userId2 = event.getString("contact2ID");
 
 					Context context = getApplicationContext();
 					if (!currentId.equals(userId1))
 						PushUtils.lazySubscribeContact(context, event, userId1);
 					if (!currentId.equals(userId2) && !userId1.equals(userId2))
 						PushUtils.lazySubscribeContact(context, event, userId2);
 					
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
 		if(date1 != null)
			mDateDisplay.setText(date1.toString());
 		if(date2 != null)
			mDateDisplay2.setText(date2.toString());
 		/*
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
 		 */
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
 
 }
