 package clock.sched;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 import java.util.Timer;
 import java.util.TimerTask;
 
 
 import clock.db.DbAdapter;
 import clock.db.Event;
 import clock.db.Event.eComparison;
 import clock.exceptions.CantGetLocationException;
 import clock.exceptions.IllegalAddressException;
 import clock.exceptions.InternetDisconnectedException;
 import clock.exceptions.OutOfTimeException;
 
 import clock.sched.R;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.res.ColorStateList;
 import android.graphics.Color;
 import android.graphics.drawable.Drawable;
 import android.location.Address;
 import android.os.Bundle;
 import android.os.Looper;
 import android.text.format.Time;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnFocusChangeListener;
 import android.view.View.OnKeyListener;
 import android.widget.ArrayAdapter;
 import android.widget.AutoCompleteTextView;
 import android.widget.Button;
 import android.widget.CompoundButton;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 import android.widget.DatePicker;
 import android.widget.EditText;
 import android.widget.TimePicker;
 import android.widget.Toast;
 import android.widget.ToggleButton;
 
 public class EventView extends Activity implements OnClickListener, OnKeyListener, OnCheckedChangeListener, OnFocusChangeListener 
 {	
 	private static final String ADDRESS_GUIDE_TEXT = ", ' , ";
 	protected Button set_date_btn;
 	protected Button set_time_btn;
 	protected Button add_event_btn;
 	protected DatePicker date_picker; 
 	protected TimePicker time_picker;
 	protected AutoCompleteTextView location_text;
 	protected EditText details_text;
 	protected Event event;
 	protected ToggleButton alarm_on_off;	
 	protected AlarmsManager alarmManager;
 	protected DbAdapter dbAdapter;
 	protected boolean alarmOnOffStatus;
 //	protected Timer autoCompleteTimer;
 //	protected Context context;
 	
    /** Called when the activity is first created. */
    @Override
    	public void onCreate(Bundle savedInstanceState) 
    	{
 	   	super.onCreate(savedInstanceState);
 	   	setContentView(R.layout.day_events);
 
 	   	set_date_btn = (Button)this.findViewById(R.id.setDatePickerBtn);
 	   	set_time_btn = (Button)this.findViewById(R.id.setTimePickerBtn);
 	   	add_event_btn = (Button)this.findViewById(R.id.add_event_btn);
 	   	alarm_on_off = (ToggleButton)this.findViewById(R.id.alarm_on_off);
 	   	date_picker = (DatePicker) this.findViewById(R.id.datePicker);
 	   	time_picker = (TimePicker) this.findViewById(R.id.timePicker);
 	   	time_picker.setIs24HourView(true);
 	   	location_text = (AutoCompleteTextView) this.findViewById(R.id.locationText);
 	   	location_text.setDropDownBackgroundResource(R.drawable.bg);
 //	   	location_text.setOnKeyListener(this);
 	   	location_text.setFocusable(true);
 	   	location_text.setOnFocusChangeListener(this);
 	   	details_text = (EditText) this.findViewById(R.id.detailsText);
 	   	dbAdapter= new DbAdapter(this);
 	   	add_event_btn.setOnClickListener(this);
 	   	set_date_btn.setOnClickListener(this);
 	   	set_time_btn.setOnClickListener(this);
 	   	alarm_on_off.setOnCheckedChangeListener(this);
 	   	alarmOnOffStatus = false;
 	   	alarmManager = new AlarmsManager(this, dbAdapter);
 //	   	autoCompleteTimer = new Timer();
 	   	
    	}
    
 
    @Override
    protected void onStart()
    {
 	   	super.onStart();
 	   	Bundle b = getIntent().getExtras();
 	   	if(b.containsKey("selectedDate")) // new event.
 	   	{
 	   		setForNewEvent(b.get("selectedDate").toString());
 	   	}
 	   	else // edit event
 	   	{
 	   		String eventStr = b.get("editEvent").toString();
 	   		event = Event.CreateFromString(eventStr);
 	   	}
 	   	
 //	   	Log.d("UI SET TO:", event.toString());
    		setPageFields();
    }
    
    private void setForNewEvent(String newEventInitDate) 
    {
   		event = Event.createNewInstance();
   		// there was a day selected at the calander.
 	   	if (event != null)
 	   	{
 	   		//Setting date in new event
 		   	String[] date = newEventInitDate.split("-");
 			event.setDay(Integer.parseInt(date[0]));
 			event.setMonth(Integer.parseInt(date[1]));
 			event.setYear(Integer.parseInt(date[2])); 
 			
 			//Setting time in new event
 			Calendar cal = Calendar.getInstance();
			event.setHour(cal.get(Calendar.HOUR));
 			event.setMin(cal.get(Calendar.MINUTE));
 	   	}
    }
 
    private void setPageFields() 
    {   
 	   date_picker.updateDate(event.getYear(), event.getMonth() - 1, event.getDay());
 	   time_picker.setCurrentHour(event.getHour());
 	   time_picker.setCurrentMinute(event.getMin());
 	   date_picker.setVisibility(View.INVISIBLE);
 	   location_text.setText(event.getLocation());
 	   details_text.setText(event.getDetails());
 	   set_time_btn.setEnabled(false);
 	   alarm_on_off.setChecked(event.getWithAlarmStatus());
    }
    
    @Override
    public void onClick(View v)
    {
 	   if (v == add_event_btn)
 	   {
 		   //TODO: check if it's a legal event - if this event overlapped by duration time with other events!!!
 		   // yes ! so we can show a message that's explains and keep user on this page.
 		   event.setPropFromViews(date_picker, time_picker, location_text, details_text, alarmOnOffStatus);
 		   // if the event is before or overlapped the current time, no need to trouble the alarm manager about that.
 		   try 
 		   {
 			   alarmManager.newEvent(event);
 			   returnResult();
 		   } 
 		   catch (IllegalAddressException iae)
 		   {
 			   Toast.makeText(this, "Unknown address",Toast.LENGTH_LONG).show();
 		   }
 		   catch (InternetDisconnectedException ide)
 		   {
 			   Toast.makeText(this, "Internet disconnected",Toast.LENGTH_LONG).show();
 		   }
 		   catch (CantGetLocationException cgle)
 		   {
 			   Toast.makeText(this, "Can't get device location",Toast.LENGTH_LONG).show();
 		   }
 		   catch (OutOfTimeException e) {
 			   Toast.makeText(this, "You Dont have time To get there!",Toast.LENGTH_LONG).show();
 		   }
 		   catch (Exception e) 
 		   {
 			   Toast.makeText(this, "Unknown error",Toast.LENGTH_LONG).show();
 		   }	
 	   }
 	   if(v == set_date_btn || v == set_time_btn)
 	   {
 		   boolean dateVisibility = v == set_date_btn;
 		   setDateTimeBtns(dateVisibility, !dateVisibility);		   
 	   }
 	   
 	}
    
    	private void setDateTimeBtns(boolean dateVisibility, boolean timeVisibility)
    	{
    			if (dateVisibility == true)
    			{
    			   	date_picker.setVisibility(View.VISIBLE);
    			   	time_picker.setVisibility(View.INVISIBLE);   				
    			}
    			else
    			{
    			   	date_picker.setVisibility(View.INVISIBLE);
    			   	time_picker.setVisibility(View.VISIBLE);   				
    			}
    			
 		   	set_date_btn.setEnabled(!dateVisibility);
 		   	set_time_btn.setEnabled(!timeVisibility);   		
 	}
    	
 	private void returnResult() 
 	{
 	   Intent i = this.getIntent();
 	   i.putExtra("newEvent", event.encodeToString());
 	   setResult(RESULT_OK, i);
 	   //Close activity
 	   finish();		
 	}
 	
 //	 private void setAutoCompleteTimer() 
 //	 {
 //		 try
 //		 {
 //			 context = this;
 //			 autoCompleteTimer = new Timer();
 //			 autoCompleteTimer.schedule(new TimerTask() {
 //				
 //				@Override
 //				public void run() {
 //					Log.d("EVENT", "Timer has start running");
 //					String text = location_text.getText().toString();
 //					ArrayList<String> sugg = GoogleAdapter.getSuggestions(text);
 //					if (!sugg.isEmpty())
 //					{
 //						Looper.prepare();
 //						ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, sugg);
 //						location_text.setAdapter(adapter);
 //						location_text.showDropDown();
 //					}
 //					
 //				}
 //			}, 1000);
 //		 }
 //		 catch (Exception e) {
 //			Log.e("EVENT", "Can't create auto complete thread - " + e.getMessage());
 //		}
 //	  }
 
 	@Override
 	public boolean onKey(View v, int keyCode, KeyEvent eventCode) 
 	{
 		String text = location_text.getText().toString();
 //		if(text.length() > 2 && eventCode.getAction() == KeyEvent.ACTION_UP)
 //		{
 //			autoCompleteTimer.cancel();		//Stop previous timer
 //			setAutoCompleteTimer();			//Set new timer with new text
 //		}
 		
 		return false;
 	}
 
 	@Override
 	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
 		if (buttonView == alarm_on_off)
 			alarmOnOffStatus = isChecked;
 	}
 
 	@Override
 	public void onFocusChange(View v, boolean hasFocus) {
 		if (v == location_text)
 		{
 			if (hasFocus && location_text.getText().toString().equals(ADDRESS_GUIDE_TEXT))
 			{
 				location_text.setText("");
 				location_text.setTextColor(Color.BLACK);
 			}
 			else if (!hasFocus && location_text.getText().toString().equals(""))
 			{
 				location_text.setTextColor(Color.LTGRAY);
 				location_text.setText(ADDRESS_GUIDE_TEXT);
 			}
 		}
 		
 	}
 	   
 }
