 
 package open.gtaskdroid.activity;
 
 import java.io.IOException;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 import java.util.Random;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.apache.http.client.ClientProtocolException;
 
 import com.google.api.client.extensions.android2.AndroidHttp;
 import com.google.api.client.googleapis.auth.oauth2.draft10.GoogleAccessProtectedResource;
 import com.google.api.client.googleapis.extensions.android2.auth.GoogleAccountManager;
 import com.google.api.client.http.HttpResponse;
 import com.google.api.client.http.HttpResponseException;
 import com.google.api.client.http.HttpTransport;
 import com.google.api.client.http.json.JsonHttpRequest;
 import com.google.api.client.http.json.JsonHttpRequestInitializer;
 import com.google.api.client.json.jackson.JacksonFactory;
 import com.google.api.services.tasks.Tasks;
 import com.google.api.services.tasks.TasksRequest;
 import com.google.api.services.tasks.model.Task;
 import com.google.api.services.tasks.model.TaskList;
 
 import open.gtaskdroid.dataaccess.EventsDbAdapter;
 import open.gtaskdroid.reminderHandler.ReminderManager;
 
 import open.Gtaskdroid.R;
 
 import android.accounts.Account;
 import android.accounts.AccountManager;
 import android.accounts.AccountManagerCallback;
 import android.accounts.AccountManagerFuture;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.DatePickerDialog;
 import android.app.Dialog;
 import android.app.ProgressDialog;
 import android.app.TimePickerDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.DatePicker;
 import android.widget.EditText;
 import android.widget.TimePicker;
 import android.widget.Toast;
 
 public class EventEditActivity extends Activity {
 
 	/**
 	* Dialog Constants
 	*/
 	private static final int DATE_PICKER_DIALOG = 0;
 	private static final int TIME_PICKER_DIALOG = 1;
 	/**
 	 * calendar constants
 	 */	
 	private static final int UPDATE_ALL_CALENDARS= 0;
 	private static final int UPDATE_EVENT_START_CALENDAR= 1;
 	private static final int UPDATE_EVENT_END_CALENDAR= 2;
 	private static final int UPDATE_REMINDER_CALENDAR= 3;
 	/** 
 	* Date Format 
 	*/
 	private static final String DATE_FORMAT = "yyyy MM dd"; 
	private static final String TIME_FORMAT = "kk:mm";		
 	public static final String DATE_TIME_FORMAT = "yyyy-MM-dd kk:mm:ss";
	//private static final String GTASK_DATE_TIME_FORMAT="yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
 	
 	
 	
 	private EditText mTitleText;
     private EditText mNoteText;
     private EditText mLocationText;
     private Button mReminderDateButton;
     private Button mReminderTimeButton;
     private Button mEventStartDateButton;
     private Button mEventStartTimeButton;
     private Button mEventEndDateButton;
     private Button mEventEndTimeButton;
     private Button mSaveButton;
     private Button mCancelButton;    
     private CheckBox mAddReminderCheckBox;
     private Long mRowId;
     private EventsDbAdapter mDbHelper;
     
     private Calendar mCalendar;
     private Calendar mEventStartCalendar;
     private Calendar mEventEndCalendar;
     private Calendar mReminderCalendar;
     
     private boolean mReminderSet; 
     private boolean eventStartDateTimeAvailable;
     private boolean eventEndDateTimeAvailable;
     private boolean eventStartDateSet;
     private boolean eventStartTimeSet;
     private boolean eventEndDateSet;
     private boolean eventEndTimeSet;
     
     private int mCalendarSwitch;									
     /** buttons get updated according to the value given here. 0 = update all the button values
      *  1 = event End dateTime
      *  2 = event end dateTime
      *  3 = reminder dateTime
      */
        
     
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         
         mDbHelper = new EventsDbAdapter(this);        
         setContentView(R.layout.event_edit);        
                 
         initContent();							//initialize buttons textFields and check boxes
         
         mRowId = savedInstanceState != null ? savedInstanceState.getLong(EventsDbAdapter.KEY_ROWID) 
                 							: null;
       
         registerButtonListenersAndSetDefaultText();
     }
 
 	private void initContent() {
 		
 		mCalendar = Calendar.getInstance();
 	    mEventStartCalendar = Calendar.getInstance();
 	    mEventEndCalendar = Calendar.getInstance();
 	    mReminderCalendar = Calendar.getInstance();
 		
 		mTitleText = (EditText) findViewById(R.id.title);
         mNoteText = (EditText) findViewById(R.id.note);
         mLocationText = (EditText) findViewById(R.id.location);
         
         mReminderDateButton = (Button) findViewById(R.id.reminder_date);
         mReminderTimeButton = (Button) findViewById(R.id.reminder_time);
         mEventStartDateButton= (Button) findViewById(R.id.event_start_date);
         mEventStartTimeButton= (Button) findViewById(R.id.event_start_time);
         mEventEndDateButton= (Button) findViewById(R.id.event_end_date);
         mEventEndTimeButton= (Button) findViewById(R.id.event_end_time);        
         mSaveButton = (Button) findViewById(R.id.save);
         mCancelButton= (Button) findViewById(R.id.cancel);
         
         mAddReminderCheckBox=(CheckBox) findViewById(R.id.add_reminder_checkbox);
        
         eventStartDateTimeAvailable=false;
         eventEndDateTimeAvailable=false;
         eventStartDateSet=false;
         eventStartTimeSet=false;
         eventEndDateSet=false;
         eventEndTimeSet=false;
         
         buildService();
         
 	}
 
 	/**
 	 * 
 	 */
 	public void buildService() {
 		
 		service = Tasks.builder(transport, new JacksonFactory())
 				.setApplicationName(Messages.getString("GtaskListActivity.4")) //$NON-NLS-1$
 				.setHttpRequestInitializer(accessProtectedResource)
 				.setJsonHttpRequestInitializer(
 						new JsonHttpRequestInitializer() {
 
 							public void initialize(JsonHttpRequest request)
 									throws IOException {
 								TasksRequest tasksRequest = (TasksRequest) request;
 								tasksRequest.setKey(API_KEY);
 							}
 						}).build();
 		accountManager = new GoogleAccountManager(this);
 		Logger.getLogger(Messages.getString("GtaskListActivity.5")).setLevel(LOGGING_LEVEL);//$NON-NLS-1$
 	}
 	
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
     	super.onCreateOptionsMenu(menu);
     	MenuInflater mi=getMenuInflater();
     	mi.inflate(R.menu.event_edit_menu, menu);
     	return true;    	
     }
 	
     @Override
     public boolean onMenuItemSelected(int featureId, MenuItem item) {
     	switch (item.getItemId()) {
 			
 		case R.id.sync_back_selected:
 			
 			syncTasks();
 			return true;
 			
 		}
     	return super.onMenuItemSelected(featureId, item);
     }
 	
 
 	
 	
 	/**
 	 *setting the value of the RowId 
 	 */
 	private void setRowIdFromIntent() {
 		if (mRowId == null) {
 			Bundle extras = getIntent().getExtras();            
 			mRowId = extras != null ? extras.getLong(EventsDbAdapter.KEY_ROWID) 
 									: null;
 			
 		}
 	}
     
 	//when the activity get paused
     @Override
     protected void onPause() {
         super.onPause();
         mDbHelper.close(); 
     }
     
     //the activity resume
     @Override
     protected void onResume() {
         super.onResume();
         mDbHelper.open(); 
     	setRowIdFromIntent();
 		populateFields();
     }
     
     
     
     @Override
     protected Dialog onCreateDialog(int id) {
     	switch(id) {
     		case DATE_PICKER_DIALOG: 
     			return showDatePicker();
     		case TIME_PICKER_DIALOG: 
     			return showTimePicker(); 
     		case DIALOG_ACCOUNTS:
     			return spawnDialogAccounts();
     		
     	}
     	return super.onCreateDialog(id);
 }
     
     
     
     //date picker dialog spawning
  	private DatePickerDialog showDatePicker() {
 		
 		
 		DatePickerDialog datePicker = new DatePickerDialog(EventEditActivity.this, new DatePickerDialog.OnDateSetListener() {
 			
 			@Override
 			public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
 				mCalendar.set(Calendar.YEAR, year);
 				mCalendar.set(Calendar.MONTH, monthOfYear);
 				mCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
 				
 				updateDateButtonText(); 
 			}
 		}, mCalendar.get(Calendar.YEAR), mCalendar.get(Calendar.MONTH), mCalendar.get(Calendar.DAY_OF_MONTH)); 
 		return datePicker; 
 	}
  	
    //time picker dialog spawning
    private TimePickerDialog showTimePicker() {
 		
     	TimePickerDialog timePicker = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
 			
 			@Override
 			public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
 				mCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
 				mCalendar.set(Calendar.MINUTE, minute); 
 				updateTimeButtonText(); 
 			}
 		}, mCalendar.get(Calendar.HOUR_OF_DAY), mCalendar.get(Calendar.MINUTE), true); 
 		
     	return timePicker; 
 	}
  	
    /**
     * button listeners 
     */
    
    
  	private void registerButtonListenersAndSetDefaultText() {
 
 		mEventStartDateButton.setOnClickListener(new View.OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				mCalendarSwitch=UPDATE_EVENT_START_CALENDAR;
 				showDialog(DATE_PICKER_DIALOG);  
 			}
 		}); 
 		
 		
 		mEventStartTimeButton.setOnClickListener(new View.OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				mCalendarSwitch=UPDATE_EVENT_START_CALENDAR;
 				showDialog(TIME_PICKER_DIALOG); 
 			}
 		}); 
 		
 		mEventEndDateButton.setOnClickListener(new View.OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				mCalendarSwitch=UPDATE_EVENT_END_CALENDAR;
 				showDialog(DATE_PICKER_DIALOG);  
 			}
 		}); 
 		
 		
 		mEventEndTimeButton.setOnClickListener(new View.OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				mCalendarSwitch=UPDATE_EVENT_END_CALENDAR;
 				showDialog(TIME_PICKER_DIALOG); 
 			}
 		});
 		
 		mReminderDateButton.setOnClickListener(new View.OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				if(mAddReminderCheckBox.isChecked()){
 					mReminderSet=true;	
 					mCalendarSwitch=UPDATE_REMINDER_CALENDAR;
 					showDialog(DATE_PICKER_DIALOG);
 				}
 			}
 		}); 
 		
 		
 		mReminderTimeButton.setOnClickListener(new View.OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				if(mAddReminderCheckBox.isChecked()){
 					mReminderSet=true;
 					mCalendarSwitch=UPDATE_REMINDER_CALENDAR;
 					showDialog(TIME_PICKER_DIALOG); 
 				}
 			}
 		});
 		
 			
 		mSaveButton.setOnClickListener(new View.OnClickListener() {
         	public void onClick(View view) {    
         		if(mTitleText.getText().toString().length()>0){
         		saveState(); 
         		setResult(RESULT_OK);        	   
         		Toast.makeText(EventEditActivity.this, getString(R.string.task_saved_message), Toast.LENGTH_SHORT).show();
         	    finish();
         		}else{
             		Toast.makeText(EventEditActivity.this, getString(R.string.fill_task_title_to_save), Toast.LENGTH_SHORT).show();
             	}
         	}
           
         });
 		
 		mCancelButton.setOnClickListener(new View.OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				finish();
 				
 			}
 		});
 		
 		/**
 		 * update specific calendar or all the calendars 
 		 */
 		  updateDateButtonText(); 						
 	      updateTimeButtonText();
 	}
    
 
  	
 	
     private void populateFields()  {
   	
     	mCalendarSwitch=UPDATE_ALL_CALENDARS;
     	/**
     	 *  Only populate the text boxes and change the calendar date
     	 *  if the row is not null from the database. 
     	 */    	
         if (mRowId != null) {
             Cursor event = mDbHelper.fetchEvent(mRowId);
             startManagingCursor(event);
             mTitleText.setText(event.getString(
             		event.getColumnIndexOrThrow(EventsDbAdapter.KEY_TITLE)));
             mNoteText.setText(event.getString(
             		event.getColumnIndexOrThrow(EventsDbAdapter.KEY_NOTE)));
             mLocationText.setText(event.getString(
             		event.getColumnIndexOrThrow(EventsDbAdapter.KEY_LOCATION)));
             
             mReminderSet = event.getInt(
             		event.getColumnIndexOrThrow(EventsDbAdapter.KEY_IS_REMINDER_SET))==1? true : false;
             mAddReminderCheckBox.setChecked(mReminderSet);
             
             // Get the date from the database and format it for our use. 
             SimpleDateFormat dateTimeFormat = new SimpleDateFormat(DATE_TIME_FORMAT);
             Date date = null;
 			try {
 				String dateString = event.getString(event.getColumnIndexOrThrow(EventsDbAdapter.KEY_EVENT_START_DATE_TIME)); 
 				if(!(" ".equalsIgnoreCase(dateString))){
 				date = dateTimeFormat.parse(dateString);
 	            mEventStartCalendar.setTime(date);
 	            eventStartDateTimeAvailable=true;
 				}
 	            dateString=event.getString(event.getColumnIndexOrThrow(EventsDbAdapter.KEY_EVENT_END_DATE_TIME));
 	            if(!(" ".equalsIgnoreCase(dateString))){
 	            date = dateTimeFormat.parse(dateString);
 	            mEventEndCalendar.setTime(date);
 	            eventEndDateTimeAvailable=true;
 	            }
 	            if (mReminderSet) {
 	            	dateString=event.getString(event.getColumnIndexOrThrow(EventsDbAdapter.KEY_REMINDER_DATE_TIME));
 	            	date = dateTimeFormat.parse(dateString);
 	            	mReminderCalendar.setTime(date);
 				}  
 			} catch (ParseException e) {
 				Log.e("ReminderEditActivity", e.getMessage(), e); 
 			} 
         } else {
         	// This is a new task - add defaults from preferences if set. 
         	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this); 
         	String defaultTitleKey = getString(R.string.pref_task_title_key); 
         	String defaultTimeKey = getString(R.string.pref_default_time_from_now_key); 
         	
         	String defaultTitle = prefs.getString(defaultTitleKey, null);
         	String defaultTime = prefs.getString(defaultTimeKey, null); 
         	
         	if(defaultTitle != null)
         		mTitleText.setText(defaultTitle); 
         	
         	if(defaultTime != null)
         		mReminderCalendar.add(Calendar.MINUTE, Integer.parseInt(defaultTime));
         		//not implemented yet. default reminder adding.
         	mReminderSet=false;
         	
         }
         
         updateDateButtonText(); 
         updateTimeButtonText();
         
         	
     }
 
 	private void updateTimeButtonText() {
 		// Set the time button text based upon the value from the database
         SimpleDateFormat timeFormat = new SimpleDateFormat(TIME_FORMAT); 
         String timeForButton = timeFormat.format(mCalendar.getTime()); 
   
         switch (mCalendarSwitch) {
         
 		case UPDATE_ALL_CALENDARS:
 			
 			if(eventStartDateTimeAvailable){
         	timeForButton = timeFormat.format(mEventStartCalendar.getTime());
         	}else {
         		timeForButton=" ";
         	}
 			mEventStartTimeButton.setText(timeForButton);
 			if(eventEndDateTimeAvailable){
         	timeForButton = timeFormat.format(mEventEndCalendar.getTime());        	
 			} else {
 				timeForButton=" ";
 			}
 			mEventEndTimeButton.setText(timeForButton);
         	if (mReminderSet) {
         		timeForButton = timeFormat.format(mReminderCalendar.getTime());
         		mReminderTimeButton.setText(timeForButton);
 			}else {
 				mReminderTimeButton.setText(R.string.date_time_not_set);
 			}			
 			break;
 			
 		case UPDATE_EVENT_START_CALENDAR:			
         	mEventStartTimeButton.setText(timeForButton);
         	mEventStartCalendar.set(Calendar.HOUR_OF_DAY, mCalendar.get(Calendar.HOUR_OF_DAY));
         	mEventStartCalendar.set(Calendar.MINUTE, mCalendar.get(Calendar.MINUTE)); 
         	eventStartTimeSet=true;
         	break;
         	
 		case UPDATE_EVENT_END_CALENDAR:			
         	mEventEndTimeButton.setText(timeForButton);
         	mEventEndCalendar.set(Calendar.HOUR_OF_DAY, mCalendar.get(Calendar.HOUR_OF_DAY));
         	mEventEndCalendar.set(Calendar.MINUTE, mCalendar.get(Calendar.MINUTE));
         	eventEndTimeSet=true;
         	break;
         	
 		case UPDATE_REMINDER_CALENDAR:
         	mReminderTimeButton.setText(timeForButton);
         	mReminderCalendar.set(Calendar.HOUR_OF_DAY, mCalendar.get(Calendar.HOUR_OF_DAY));
         	mReminderCalendar.set(Calendar.MINUTE, mCalendar.get(Calendar.MINUTE));
         	break;
         	
 		}
         
 	}
 	
 	
 
 	private void updateDateButtonText() {
 		// Set the date button text based upon the value from the database 
         SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT); 
         String dateForButton = dateFormat.format(mCalendar.getTime()); 
               
         
         switch (mCalendarSwitch) {
         
 		case UPDATE_ALL_CALENDARS:
 			if(eventStartDateTimeAvailable){
         	dateForButton = dateFormat.format(mEventStartCalendar.getTime());
 			}else {
 				dateForButton=" ";
 			}
 			mEventStartDateButton.setText(dateForButton);
 			if(eventStartDateTimeAvailable){
         	dateForButton = dateFormat.format(mEventEndCalendar.getTime());
 			} else {
 				dateForButton=" ";
 			}
         	mEventEndDateButton.setText(dateForButton);
         	if (mReminderSet) {
         		dateForButton = dateFormat.format(mReminderCalendar.getTime());
         		mReminderDateButton.setText(dateForButton);
 			}else {
 				mReminderDateButton.setText(R.string.date_time_not_set);
 			}
 			break;
 			
 		case UPDATE_EVENT_START_CALENDAR:
         	mEventStartDateButton.setText(dateForButton);
         	mEventStartCalendar.set(Calendar.YEAR, mCalendar.get(Calendar.YEAR));
         	mEventStartCalendar.set(Calendar.MONTH, mCalendar.get(Calendar.MONTH));
         	mEventStartCalendar.set(Calendar.DAY_OF_MONTH, mCalendar.get(Calendar.DAY_OF_MONTH));
         	eventStartDateSet=true;
 			break;
 			
 		case UPDATE_EVENT_END_CALENDAR:
         	mEventEndDateButton.setText(dateForButton);
         	mEventEndCalendar.set(Calendar.YEAR, mCalendar.get(Calendar.YEAR));
         	mEventEndCalendar.set(Calendar.MONTH, mCalendar.get(Calendar.MONTH));
         	mEventEndCalendar.set(Calendar.DAY_OF_MONTH, mCalendar.get(Calendar.DAY_OF_MONTH));
         	eventEndDateSet=true;
 			break;
 			
 		case UPDATE_REMINDER_CALENDAR:
         	mReminderDateButton.setText(dateForButton);
         	mReminderCalendar.set(Calendar.YEAR, mCalendar.get(Calendar.YEAR));
         	mReminderCalendar.set(Calendar.MONTH, mCalendar.get(Calendar.MONTH));
         	mReminderCalendar.set(Calendar.DAY_OF_MONTH, mCalendar.get(Calendar.DAY_OF_MONTH));
 			break;
 
 
 		}
         
 		
 	}
     
     @Override
     protected void onSaveInstanceState(Bundle outState) {
         super.onSaveInstanceState(outState);
         outState.putLong(EventsDbAdapter.KEY_ROWID, mRowId);
     }
     
 
     //save the event in database
     private void saveState() {
     	
   
     	    	
         String title = mTitleText.getText().toString();
         String description = mNoteText.getText().toString();
         String location = mLocationText.getText().toString();        
 
         SimpleDateFormat dateTimeFormat = new SimpleDateFormat(DATE_TIME_FORMAT); 
         String eventStartDateTime;
         String eventEndDateTime;
            	if(eventStartDateTimeAvailable||(eventStartDateSet&&eventStartTimeSet)){
     		eventStartDateTime = dateTimeFormat.format(mEventStartCalendar.getTime());
            	}else if(eventStartDateSet&&!eventStartTimeSet){
            		mEventStartCalendar.set(Calendar.HOUR_OF_DAY, 00);
            		mEventStartCalendar.set(Calendar.MINUTE, 00);
            		eventStartDateTime = dateTimeFormat.format(mEventStartCalendar.getTime());
            	}else if(!eventStartDateSet&&eventStartTimeSet){
            		mEventStartCalendar.set(Calendar.YEAR, mCalendar.get(Calendar.YEAR));
            		mEventStartCalendar.set(Calendar.MONTH, mCalendar.get(Calendar.MONTH));
            		mEventStartCalendar.set(Calendar.DAY_OF_MONTH, mCalendar.get(Calendar.DAY_OF_MONTH));
            		eventStartDateTime = dateTimeFormat.format(mEventStartCalendar.getTime());
            	}else{
            		eventStartDateTime=" ";
            	}
            	
            	
            	if(eventEndDateTimeAvailable||(eventEndDateSet&&eventEndTimeSet)){
            		eventEndDateTime = dateTimeFormat.format(mEventEndCalendar.getTime());
            	}else if(eventEndDateSet&&!eventEndTimeSet){
            		mEventEndCalendar.set(Calendar.HOUR_OF_DAY, 00);
            		mEventEndCalendar.set(Calendar.MINUTE, 00);
            		eventEndDateTime = dateTimeFormat.format(mEventEndCalendar.getTime());
            	}else if(!eventEndDateSet&&eventEndTimeSet){
            		mEventEndCalendar.set(Calendar.YEAR, mCalendar.get(Calendar.YEAR));
            		mEventEndCalendar.set(Calendar.MONTH, mCalendar.get(Calendar.MONTH));
            		mEventEndCalendar.set(Calendar.DAY_OF_MONTH, mCalendar.get(Calendar.DAY_OF_MONTH));
            		eventEndDateTime = dateTimeFormat.format(mEventEndCalendar.getTime());
            	}else{
            		eventEndDateTime=" ";
            	}
 
     	
     	
     	mReminderSet=mAddReminderCheckBox.isChecked();
     	
     	int isReminderChkBxSelected = mReminderSet? 1:0;
     	String reminderDateTime= dateTimeFormat.format(mReminderCalendar.getTime());
 
         if (mRowId == null) {
         	
         	long id = mDbHelper.createEvent(title, description, location,
         			eventStartDateTime, eventEndDateTime,isReminderChkBxSelected, reminderDateTime);
             if (id > 0) {
                 mRowId = id;
             }
         } else {
             mDbHelper.updateEvent(mRowId, title, description,location,
             		eventStartDateTime, eventEndDateTime,isReminderChkBxSelected, reminderDateTime);
         }
        
         //if the reminder calendar set. then add a reminder
         if (mReminderSet) {
         	new ReminderManager(this).setReminder(mRowId, mReminderCalendar);
 		} 
          
     	
 
 		
     }
     
     
     
     
     
     
     /**
      * resync ability
      */    
     private ProgressDialog myProgressDialog;
     private GoogleAccountManager accountManager;
     private static final String PREF = Messages.getString("GtaskListActivity.3"); //$NON-NLS-1$
     private String AUTH_TOKEN_TYPE = Messages.getString("GtaskListActivity.2"); //$NON-NLS-1$
 	public static final int REQUEST_AUTHENTICATE = 0;
 	private static final String TAG = Messages.getString("GtaskListActivity.0"); //$NON-NLS-1$
 	private static final Level LOGGING_LEVEL = Level.OFF;
 	private final String API_KEY = Messages.getString("GtaskListActivity.1"); //$NON-NLS-1$
 	private final HttpTransport transport = AndroidHttp.newCompatibleTransport();
 	private GoogleAccessProtectedResource accessProtectedResource = new GoogleAccessProtectedResource(
 			null);
 	private Tasks service;
 	private static final int DIALOG_ACCOUNTS = 3;	
     
 	
     private void syncTasks(){
     	if(mTitleText.getText().toString().length()>0){
     	showDialog(DIALOG_ACCOUNTS);
     	}else{
     		Toast.makeText(EventEditActivity.this, getString(R.string.atleast_fill_title), Toast.LENGTH_LONG).show();
     	}
     }
     
    
     
 	private Dialog spawnDialogAccounts() {
 		
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setTitle(R.string.select_account);
 		final Account[] accounts = accountManager.getAccounts();
 		final int size = accounts.length;
 		String[] names = new String[size];
 		for (int i = 0; i < size; i++) {
 			names[i] = accounts[i].name;
 		}
 		builder.setItems(names, new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int which) {
 				gotAccount(accounts[which]);
 				
 			}
 		});
 		return builder.create();
 	}
     
 	
 	private void gotAccount(final Account account) {
 		SharedPreferences settings = getSharedPreferences(PREF, 0);
 		SharedPreferences.Editor editor = settings.edit();
 		editor.putString(Messages.getString("GtaskListActivity.7"), account.name); //$NON-NLS-1$
 		editor.commit();
 		accountManager.manager.getAuthToken(account, AUTH_TOKEN_TYPE, true,
 				new Manager(), null);
 	}
     
 	
 	@SuppressWarnings("deprecation")
 	void onAuthToken() throws ParseException {
 		try {
 			insertTaskList();
 						
 			List<TaskList> taskLists = service.tasklists().list().execute()
 					.getItems();
 			if (taskLists != null) {
 				for (TaskList taskList : taskLists) {
 					List<Task> tasks = service.tasks().list(taskList.getId())
 							.execute().getItems();
 					if (tasks != null) {
 						
 							Task mGtask = new Task();
 							mGtask.setTitle(mTitleText.getText().toString()) ;
 							mGtask.setNotes(mNoteText.getText().toString());
 							service.tasks.insert("@default", mGtask).execute();							
 							
 						
 					} 
 				}
 			}
 
 		} catch (IOException e) {
 			Log.d(TAG, Messages.getString("GtaskListActivity.8")+e.getMessage()); //$NON-NLS-1$
 			handleException(e);
 
 			
 
 		}
 		
 	}
 	
 	void handleException(Exception e) {
 		e.printStackTrace();
 		if (e instanceof HttpResponseException) {
 			HttpResponse response = ((HttpResponseException) e).getResponse();
 			int statusCode = response.getStatusCode();
 			try {
 				response.ignore();				
 			} catch (IOException e1) {
 				e1.printStackTrace();
 				}
 
 			if (statusCode == 401) {
 				gotAccount(true);				
 				return;
 			}
 			
 		}
 		Log.e(TAG, e.getMessage(), e);
 		myProgressDialog.dismiss();
 		Toast.makeText(EventEditActivity.this, getString(R.string.no_network_connnection), Toast.LENGTH_LONG).show();
 	    
 	}
 	
 	
 	private void insertTaskList(){
 
 		try{
 		TaskList tempTaskList=new TaskList();
 		tempTaskList.setTitle(Messages.getString("GtaskListActivity.10")+new Random().nextInt(30)); //$NON-NLS-1$
 		TaskList newTaskList=service.tasklists().insert(tempTaskList).execute();
 		Log.d(TAG, Messages.getString("GtaskListActivity.11")+newTaskList.toString()); //$NON-NLS-1$
 		}catch(ClientProtocolException exp){
 			Log.d(TAG, Messages.getString("GtaskListActivity.12")+exp.getMessage()); //$NON-NLS-1$
 		}
 		catch(Exception exp){
 			Log.d(TAG, Messages.getString("GtaskListActivity.13")+exp.getMessage()); //$NON-NLS-1$
 			handleException(exp);
 		}
 	}
 	
 	private void gotAccount(boolean tokenExpired) {
 		SharedPreferences settings = getSharedPreferences(PREF, 0);
 		String accountName = settings.getString(Messages.getString("GtaskListActivity.6"), null); //$NON-NLS-1$
 		Account account = accountManager.getAccountByName(accountName);
 		if (account != null) {
 			if (tokenExpired) {
 				accountManager.invalidateAuthToken(accessProtectedResource
 						.getAccessToken());
 				accessProtectedResource.setAccessToken(null);
 			}
 			gotAccount(account);			
 			return;
 		}
 		showDialog(DIALOG_ACCOUNTS);
 		
 	}
 	
 	
 	
 	
 	private final class Manager implements AccountManagerCallback<Bundle> {
 		
 		public void run(final AccountManagerFuture<Bundle> future) {
 			 myProgressDialog = null;
 			 myProgressDialog = ProgressDialog.show(EventEditActivity.this,
                    null , "Connecting Google...", true);
 		
 
 			 new Thread() {
                 public void run() {
 			try {
 				Bundle bundle = future.getResult();
 				if (bundle.containsKey(AccountManager.KEY_INTENT)) {
 					
 					Intent intent = bundle
 							.getParcelable(AccountManager.KEY_INTENT);
 					intent.setFlags(intent.getFlags()
 							& ~Intent.FLAG_ACTIVITY_NEW_TASK);
 					startActivityForResult(intent,
 							REQUEST_AUTHENTICATE);
 				} else if (bundle
 						.containsKey(AccountManager.KEY_AUTHTOKEN)) {
 					accessProtectedResource
 							.setAccessToken(bundle
 									.getString(AccountManager.KEY_AUTHTOKEN));
 					String authToken=bundle.getString(AccountManager.KEY_AUTHTOKEN).toString();
 					Log.d(Messages.getString("GtaskListActivity.17"), Messages.getString("GtaskListActivity.18")+authToken); //$NON-NLS-1$ //$NON-NLS-2$
 					onAuthToken();							
 					EventEditActivity.this.runOnUiThread(new Runnable() {
 					    public void run() {
 					       	myProgressDialog.dismiss();
 							
 					    }
 					});
 				}
 			} catch (final Exception e) {
 				EventEditActivity.this.runOnUiThread(new Runnable() {
 				    public void run() {
 				    	
 				    	handleException(e);
 						
 				    }
 				});
 
 			}   
                 }
 			 }.start();
 		}
 		
 		}
     
 }
