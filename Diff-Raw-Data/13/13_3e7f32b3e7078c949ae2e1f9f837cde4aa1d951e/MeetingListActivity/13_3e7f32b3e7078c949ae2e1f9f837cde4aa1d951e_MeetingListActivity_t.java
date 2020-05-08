 package com.thoughtworks.imeeting;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ListView;
 import android.widget.NumberPicker;
 import android.widget.RadioGroup;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.google.api.services.calendar.model.Event;
 import com.thoughtworks.imeeting.models.MeetingEvent;
 import com.thoughtworks.imeeting.tasks.CreateEventTask;
 import com.thoughtworks.imeeting.tasks.FetchEventListTask;
 
 public class MeetingListActivity extends BaseActivity{
 	private String calendarId;
 	private String roomName;
 	private Long[] duration = new Long[2];
 	private AlertDialog quickBookDialog;
 	private List <Long> freeSlots;
 	private static final long MIN_15 = 900000L;
 	private static final long MIN_30 = 1800000L;
 	private static final long MIN_60 = 3600000L;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 				
 		Intent intent = getIntent();
 		calendarId = intent.getStringExtra(Keys.CALENDAR_ID);
 		roomName = intent.getStringExtra(Keys.ROOM_NAME);
 		
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.meeting_list);
 		freeSlots = new ArrayList<Long>();
     }
 	
 	@Override
 	protected void onCalendarServiceReady(){
 		getProgressDialog().setMessage("Fetching Events ...");
 		getProgressDialog().show();
 		new FetchEventListTask(service, this, calendarId).execute();
 	}
 	
 	@Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // use an inflater to populate the ActionBar with items
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.main, menu);
         return true;
     }
 	
 	
 	@Override
   	public boolean onOptionsItemSelected(MenuItem item){
     	// same as using a normal menu
     	switch(item.getItemId()) {
 	    	case R.id.action_settings:
 	    		Toast.makeText(getApplicationContext(), "Open Settings", Keys.TOAST_SHORT).show();
 	    		Intent intent = new Intent(this, SettingsActivity.class);
 	    		startActivity(intent);
 	    		break;
     	}
   		return true;
   	}
 	
 	@Override 
 	public void onMeetingCreated(Event event) {
 		super.onMeetingCreated(event);
 		onCalendarServiceReady();
 	}
 	
 	public void onEventListFetched(List<Event> events) {
 		getProgressDialog().hide();
 		populateEventListView(events);
 		if(!calendarId.equalsIgnoreCase(Keys.SELF_CALENDAR_ID))
 			presentQuickBookMenu(events);
 	}
 
 	private void presentQuickBookMenu(List<Event> events) {
 		long eventStart = new Date().getTime() + 172800000L;
 		if(events.size()>0){
 			eventStart = events.get(0).getStart().getDateTime().getValue();
 		}
 		
 		long now = new Date().getTime();
 		long timeInterval = (eventStart - now)/(60000);
 		Log.v(Keys.TAG, "Time Interval: "+timeInterval);
 		Log.v(Keys.TAG, "Event Start: "+eventStart);
 		Log.v(Keys.TAG, "Now Start: "+now);
 		if(timeInterval > 15) {
 			ArrayList<String> menuList = new ArrayList<String>();
 			if(timeInterval >= 60) {
 				menuList.add("30 Minutes");
 				menuList.add("1 Hour");
 				duration[0] = 1800000L;
 				duration[1] = 3600000L;
 			} else if(timeInterval > 45 ) {
 				menuList.add("30 Minutes");
 				menuList.add(timeInterval + " Minutes");
 				duration[0] = 1800000L;
 				duration[1] = timeInterval*60000;
 			} else {
 				menuList.add(timeInterval + " Minutes");
 				duration[0] = timeInterval*60000;
 			}
 			
 			quickBookDialog = new AlertDialog.Builder(MeetingListActivity.this)
 				.setTitle("Quick Book")
 				.setItems(menuList.toArray(new String[0]),new DialogInterface.OnClickListener() {
 					 public void onClick(DialogInterface dialog, int item) {
 						 dialog.dismiss();
 						 Date startTime = new Date(new Date().getTime() + 120000);
 						  switch (item) {
 						  case 0:
 							  createEvent(startTime, duration[0] - 120000);
 							  Toast.makeText(MeetingListActivity.this, "Quick Book "+roomName+" for "+duration[0]/60000+" min", Keys.TOAST_SHORT).show();
 						  break;
 						  case 1:
 							  createEvent(startTime, duration[1] - 120000);
 							  Toast.makeText(MeetingListActivity.this, "Quick Book "+roomName+" for "+duration[1]/60000+" min", Keys.TOAST_SHORT).show();
 						  break;
 						  }
 					 }
 				}).create();
 			quickBookDialog.show();
 		}
 		
 	}
 	
 	private void showNewMeetingDialog(Long startTime, Long endTime){
 		List<String> availableSlots = populateSlots(startTime, endTime);
 		if(availableSlots.size() == 0) {
 			Toast.makeText(MeetingListActivity.this, "No slot here", Toast.LENGTH_SHORT);
 			return;
 		}
 	    AlertDialog.Builder b = new AlertDialog.Builder(this);
 	    b.setTitle("Book "+roomName);
 	    View bookRoom = getLayoutInflater().inflate(R.layout.room_book, null);
 	    SharedPreferences prefs = getApplicationContext().getSharedPreferences(Keys.PREFERENCE_NAME, Context.MODE_PRIVATE);
 	    String defaultTitle = prefs.getString(Keys.DEFAULT_EVENT_NAME_KEY, null);
 	    final NumberPicker slotSelector = (NumberPicker) bookRoom.findViewById(R.id.slotPicker);
 	    final TextView meetingTitle = (TextView) bookRoom.findViewById(R.id.meeting_title);
 	    final RadioGroup durationSelect = (RadioGroup) bookRoom.findViewById(R.id.radioGroup1);
 	    meetingTitle.setText(defaultTitle, TextView.BufferType.NORMAL);
 	    slotSelector.setMinValue(0);
 	    slotSelector.setMaxValue(availableSlots.size()-1);
 	    slotSelector.setDisplayedValues(availableSlots.toArray(new String[]{}));
 	    
 	    b.setView(bookRoom);
 	    b.setPositiveButton("BOOK", new DialogInterface.OnClickListener()
 	    {
 	        @Override
 	        public void onClick(DialogInterface dialog, int whichButton)
 	        {
 	        	Long startTime = freeSlots.get(slotSelector.getValue());
 	        	String title = meetingTitle.getText().toString();
 	        	Long duration = durationSelect.getCheckedRadioButtonId() == R.id.radio0 ? MIN_30 : MIN_60;
 	        	dialog.dismiss();
	        	createEvent(title, new Date(startTime), duration);
 	        }
 	    });
 	    b.setNegativeButton("CANCEL", null);
 	    b.create().show();
 	}
 	
 	@SuppressWarnings("deprecation")
 	private List<String> populateSlots(long startTime, long endTime) {
 		freeSlots.clear();
 		long start = startTime/MIN_15*MIN_15;
 		String day;
 		int today = new Date().getDay();
 		long interval = (endTime - startTime)/MIN_30;
 		List<String> list = new ArrayList<String>();
 		SimpleDateFormat sdf = new SimpleDateFormat("h:mm a");
 		while(interval > 0) {
 			freeSlots.add(start);
 			Date date = new Date(start);
 			 day = date.getDay() > today ? "Tomorrow" : "Today"; 
 			list.add(day +", "+sdf.format(date));
 			start += MIN_30;
 			interval --;
 		}
 		return list;
 	}
 
 	private void populateEventListView(final List<Event> events) {
 		final ListView listview = (ListView) findViewById(R.id.listView1);
 		final MeetingListAdapter meetingListAdapter = new MeetingListAdapter(events, this);
 		listview.setAdapter(meetingListAdapter);
 		listview.setClickable(true);
 		listview.setOnItemClickListener(new OnItemClickListener() {
 			@Override
 			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
 				MeetingEvent event = meetingListAdapter.getList().get(arg2);
 				if(event.isEmptySlot())
 					showNewMeetingDialog(event.getStartTime().getValue(), event.getEndTime().getValue());
 			}
 		});
 	}
 	
 	private void createEvent(Date startTime, Long duration) {
 		SharedPreferences prefs = getApplicationContext().getSharedPreferences(Keys.PREFERENCE_NAME, Context.MODE_PRIVATE);
 		String name = prefs.getString(Keys.DEFAULT_EVENT_NAME_KEY, getResources().getString(R.string.default_event_name));
 		createEvent(name, startTime, duration);
 	}
 	
 	private void createEvent(String title, Date startTime, Long duration) {
 		getProgressDialog().setMessage("Booking "+roomName+"...");
 		getProgressDialog().show();
		Date endTime = new Date(startTime.getTime() + duration);
 		new CreateEventTask(service, MeetingListActivity.this, calendarId).execute(title, roomName, startTime, endTime);
 	}
 	
 }
