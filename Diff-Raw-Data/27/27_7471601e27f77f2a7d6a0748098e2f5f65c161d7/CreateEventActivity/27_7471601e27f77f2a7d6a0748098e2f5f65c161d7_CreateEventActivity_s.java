 package il.ac.huji.app4beer;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 
 import com.parse.ParseUser;
 
 import il.ac.huji.app4beer.DAL.Contact;
 import il.ac.huji.app4beer.DAL.DAL;
 import il.ac.huji.app4beer.DAL.Event;
 import il.ac.huji.app4beer.DAL.ParseProxy;
 import il.ac.huji.app4beer.DAL.PushEvent;
 import android.os.Bundle;
 import android.app.Activity;
 import android.app.DatePickerDialog;
 import android.app.DatePickerDialog.OnDateSetListener;
 import android.app.TimePickerDialog;
 import android.app.TimePickerDialog.OnTimeSetListener;
 import android.content.Intent;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.DatePicker;
 import android.widget.EditText;
 import android.widget.ImageButton;
 import android.widget.TimePicker;
 
 public class CreateEventActivity extends Activity {
 
 	private static final int ChooseParticipants=42;
 
 	private ArrayList<Integer> _contacts;
 	private ArrayList<Integer> _groups;
 	private EditText _eventDate;
 	private EditText _eventTime;
 	private Boolean _dateSet;
 	private Boolean _timeSet;
 	private Calendar _calendar;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_create_event);
 		
 		createDateTime();
 		initCreateEventButton();	 
 		initAddParticipantsButton();
 
 	}
 
 	private void initCreateEventButton() {
 		ImageButton createEventButton = (ImageButton)findViewById(R.id.sendBtn);
 		createEventButton.setOnClickListener(new OnClickListener() {
             public void onClick(View v) {
             	EditText eventName = (EditText)findViewById(R.id.eventName);
             	EditText eventDescription = (EditText)findViewById(R.id.eventDescription);
             	EditText eventLocation= (EditText)findViewById(R.id.eventLocation);
             	Calendar c = Calendar.getInstance();
     			if(eventName.getText().length() == 0 || 
     				eventDescription.getText().length() == 0 ||
     				!_dateSet || 
     				!_timeSet ||
     				_calendar.before(Calendar.getInstance())) {
   			      return;
     			}
     			try {
     				Event event = new Event(eventName.getText().toString(),
 							eventDescription.getText().toString(), eventLocation.getText().toString(),
 							_calendar.getTime(), _contacts, _groups);
     				Contact owner = DAL.Instance().readContact(ParseUser.getCurrentUser().getUsername());
     				owner.set_source(Contact.Source.OWNER);
     				owner.set_attending(Contact.Attending.YES);
     				event.set_owner(owner);
     				PushEvent pEvent = new PushEvent(event);
     				pEvent.persist();
     				pEvent.push();    				
 		    		setResult(RESULT_OK);
 		    		finish();
     			} catch (Exception e) {
     				e.printStackTrace();
     			}
             }
         });
 		
 	}
 
 	private void createDateTime() {
 		_calendar = Calendar.getInstance();
 		_dateSet = false;
 		_timeSet = false;
 		
 		_eventDate = (EditText)findViewById(R.id.eventDate);
 		_eventTime = (EditText)findViewById(R.id.eventTime);
 		
 		final OnDateSetListener odsl=new OnDateSetListener()
         {
 			@Override
 			public void onDateSet(DatePicker arg0, int year, int month, int dayOfMonth) {
				_eventDate.setText(String.format("%02d / %02d / %04d", dayOfMonth, month, year));
 				_dateSet = true;
 				_calendar.set(year, month, dayOfMonth);
 			}
         };
 
         _eventDate.setFocusable(false);
         _eventDate.setOnClickListener(new OnClickListener() {
         	@Override
         	public void onClick(View arg0) {
         		DatePickerDialog datePickDiag=new DatePickerDialog(CreateEventActivity.this,odsl,_calendar.get(Calendar.YEAR),_calendar.get(Calendar.MONTH),_calendar.get(Calendar.DAY_OF_MONTH));
         		datePickDiag.show();
         	}
         });
 
 		final OnTimeSetListener otsl=new OnTimeSetListener()
         {
 			@Override
 			public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
 				_eventTime.setText(String.format("%02d : %02d", hourOfDay, minute));
 				_timeSet = true;
 				_calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
 				_calendar.set(Calendar.MINUTE, minute);
 			}
         };
 
         _eventTime.setFocusable(false);
         _eventTime.setOnClickListener(new OnClickListener() {
         	@Override
         	public void onClick(View arg0) {
         		TimePickerDialog timePickDiag=new TimePickerDialog(CreateEventActivity.this,otsl,_calendar.get(Calendar.HOUR_OF_DAY),_calendar.get(Calendar.MINUTE)+1,true);
         	    timePickDiag.show();
         	}
         });
 
 	}
 
 	private void initAddParticipantsButton() {
 		Button button = 
         		(Button)findViewById(R.id.eventAddParticipantsBtn);
 		button.setOnClickListener(new OnClickListener() {
             public void onClick(View v) {
     	        Intent myIntent = new Intent(CreateEventActivity.this, ChooseParticipantsActivity.class);
     	        myIntent.putIntegerArrayListExtra("contacts", _contacts);
     	        myIntent.putIntegerArrayListExtra("groups", _groups);
   	        startActivityForResult(myIntent, ChooseParticipants);
             }
         });
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.create_event, menu);
 		return true;
 	}
 	
     @Override
 	protected void onActivityResult(int reqCode, int resCode, Intent data) {
 		  switch (reqCode) {
 		  case ChooseParticipants:
 			  if (resCode==RESULT_OK) {
 				  _contacts = data.getIntegerArrayListExtra("contacts");
 				  _groups = data.getIntegerArrayListExtra("groups");
 				  
 			  }
 			  break;
 		  }
 	}
 
 
 }
