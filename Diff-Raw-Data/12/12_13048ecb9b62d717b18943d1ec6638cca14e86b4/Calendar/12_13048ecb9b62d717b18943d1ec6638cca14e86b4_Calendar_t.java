 package com.example.calendar;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.widget.Button;
 import android.widget.CalendarView;
 import android.widget.Toast;
 
 
 public class Calendar extends Activity {
 
 	final static int VIEW_EVENT = 1;
 	final static int EDIT_EVENT = 2;
 	private SharedPreferences sharedPref;
 	private ArrayList<CalendarEvent> event_list = new ArrayList<CalendarEvent>();
 	DbHandler db;
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         sharedPref = getPreferences(Context.MODE_PRIVATE);
         db = new DbHandler(this);
         if (event_list.isEmpty()) event_list = db.getAllEvents();
         if (event_list.isEmpty())
         {
         	//			READ FROM FILE HERE
         	db.addEvent(new CalendarEvent("5/26","blah"));
         	db.addEvent(new CalendarEvent("5/27","duh"));
         	event_list = db.getAllEvents();
         }
         setContentView(R.layout.activity_calendar);
         CalendarView calendarView = (CalendarView) findViewById(R.id.calendarView);
 		calendarView
 				.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
 					public void onSelectedDayChange(CalendarView view,
 							int year, int month, int dayOfMonth) {
 						Intent intent = new Intent(Calendar.this, NewEvent.class);
 						intent.putExtra("year", Integer.toString(year));
						intent.putExtra("month", Integer.toString(month+1));
 						intent.putExtra("day", Integer.toString(dayOfMonth));
 						startActivityForResult(intent, EDIT_EVENT);
 					}
 		});
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_calendar, menu);
         return true;
 	}
 
     @Override
     public void onActivityResult(int requestCode,int resultCode,Intent data) {
 		super.onActivityResult(requestCode, resultCode, data);
 		switch(requestCode) {
 			case EDIT_EVENT: 
 				if (resultCode == RESULT_OK) {
 					CalendarEvent event = (CalendarEvent) data.getSerializableExtra("event");
 					ArrayList<String> files = (ArrayList<String>) data.getStringArrayListExtra("files");
 					ArrayList<String> apps = (ArrayList<String>) data.getStringArrayListExtra("apps");
 					ArrayList<String> contacts = (ArrayList<String>) data.getStringArrayListExtra("contacts");
 					ArrayList<String> links = (ArrayList<String>) data.getStringArrayListExtra("links");
 					String notes = (String) data.getStringExtra("notes");
 					event_list = db.getAllEvents();
 					event = db.getEvent(event.getDate());
 					event_list.add(event);
 					Log.d("Calendar", "newly added event is id " + String.valueOf(event.getID()));
 					for(String theFile : files)
 					{
 						db.addFile(event.getID(), theFile);
 					}
 					for(String theApp : apps)
 					{
 						db.addApp(event.getID(), theApp);
 					}
 					for(String theContact : contacts)
 					{
 						db.addContact(event.getID(), theContact);
 					}
 					if(!notes.equals("")) db.addNote(event.getID(), notes);
 					Toast.makeText(getApplicationContext(), 
 							"event added", Toast.LENGTH_LONG).show();
 				} break;
 			case VIEW_EVENT: 
 				if (resultCode == RESULT_OK) {
 					CalendarEvent event = (CalendarEvent) data.getSerializableExtra("event");
 					event_list = db.getAllEvents();
 					event_list.add(db.getEvent(event.getDate()));
 					Toast.makeText(getApplicationContext(), 
 							"event added", Toast.LENGTH_LONG).show();
 						
 			}	break;
 		}
 	}
     
 	public void newEvent(View view) {
 		Intent intent = new Intent(this, NewEvent.class);
 	    startActivityForResult(intent,EDIT_EVENT);
 	}
 	
 	public void viewEvents(View view) {
 		Intent intent = new Intent(this, ViewEvents.class);
 		//intent.putExtra("events",event_list);
 		startActivity(intent);
 	}
 	
 	public void eventDetails(View view) {
 		Intent intent = new Intent(this, EventDetails.class);
 		intent.putExtra("Date","placeholder");//TODO fix date get
 		startActivity(intent);
 	}
 }
