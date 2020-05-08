 package com.thoughtworks.imeeting.tasks;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.TimeZone;
 
 import android.content.Context;
 import android.os.AsyncTask;
 
 import com.google.api.client.googleapis.json.GoogleJsonResponseException;
 import com.google.api.client.util.DateTime;
 import com.google.api.services.calendar.Calendar;
 import com.google.api.services.calendar.model.Event;
 import com.google.api.services.calendar.model.EventAttendee;
 import com.google.api.services.calendar.model.EventDateTime;
 import com.thoughtworks.imeeting.BaseActivity;
 import com.thoughtworks.imeeting.Keys;
 
 public class CreateEventTask extends AsyncTask<Object, Integer, Event> {
 	private Context context;
 	private String calendarId;
 	private Calendar service;
 	private boolean unauthorised;
 	
 	public CreateEventTask(Calendar service, Context context) {
 		 this( service, context, "primary");
 	}
 	
 	public CreateEventTask(Calendar service, Context context, String calendarId) {
 		this.service = service;
 		this.context = context;
 		this.calendarId = calendarId;
 	}
 	
 	@Override
     protected Event doInBackground(Object... params) {
 		Event createdEvent = null;
 		try {
 			Event event = new Event();
			
 			
 			ArrayList<EventAttendee> attendees = new ArrayList<EventAttendee>();
 			attendees.add(new EventAttendee().setEmail(calendarId).setResource(true));
 			Date startDate = (Date) params[2];
 			Date endDate = (Date) params[3];
 			DateTime start = new DateTime(startDate, TimeZone.getTimeZone("UTC"));
 			DateTime end = new DateTime(endDate, TimeZone.getTimeZone("UTC"));
 			
 			event.setAttendees(attendees);
 			event.setSummary((String) params[0]);
 			event.setLocation((String) params[1]);
 			event.setStart(new EventDateTime().setDateTime(start));			
 			event.setEnd(new EventDateTime().setDateTime(end));
 
 			createdEvent = service.events().insert(Keys.SELF_CALENDAR_ID, event).execute();
 			return createdEvent;
 
 		} catch(GoogleJsonResponseException e){
 			e.printStackTrace();
 			if(e.getStatusCode() == 401) unauthorised = true;						  
 			
 		} catch (IOException e) {
 			e.printStackTrace();
 		} 
 		return null;
     }
 
     protected void onPostExecute(Event event) {
     	if(unauthorised) ((BaseActivity)context).invalidateToken();
     	else ((BaseActivity)context).onMeetingCreated(event);
     }
 }
