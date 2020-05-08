 package com.smsmt.utils.calendar;
 
 import java.util.TimeZone;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.content.ContentValues;
 import android.database.Cursor;
 import android.net.Uri;
 import android.provider.CalendarContract;
 import android.provider.CalendarContract.Attendees;
 import android.provider.CalendarContract.Calendars;
 import android.provider.CalendarContract.Events;
 import android.provider.CalendarContract.Reminders;
 import android.util.Log;
 
 public class CalendarUtils {
 	private static final String MY_ACCOUNT_NAME = "SmsmtCalendar";
 	private Activity pluginActivity;
     
 	private CalendarUtils() {
 		// private constructor
 	}
 	
 	private static class CalendarUtilsHolder {
 		public static final CalendarUtils INSTANCE = new CalendarUtils();
 	}
 	
 	public static CalendarUtils getInstance(Activity activity) {
 		CalendarUtilsHolder.INSTANCE.setPluginActivity(activity);
 		return CalendarUtilsHolder.INSTANCE;
 	}
 
 	public Activity getPluginActivity() {
 		return pluginActivity;
 	}
 
 	public void setPluginActivity(Activity pluginActivity) {
 		this.pluginActivity = pluginActivity;
 	}
 	
 	
 	public void createLocalCalendar() {
 		ContentValues values = new ContentValues();
 		values.put(Calendars.ACCOUNT_NAME, MY_ACCOUNT_NAME);
 		values.put(Calendars.ACCOUNT_TYPE, CalendarContract.ACCOUNT_TYPE_LOCAL);
 		values.put(Calendars.NAME, "GrokkingAndroid Calendar");
 		values.put(Calendars.CALENDAR_DISPLAY_NAME, "GrokkingAndroid Calendar");
 		values.put(Calendars.CALENDAR_COLOR, 0xffff0000);
 		values.put(Calendars.CALENDAR_ACCESS_LEVEL, Calendars.CAL_ACCESS_OWNER);
 		values.put(Calendars.OWNER_ACCOUNT, "huypham@gmail.com");
 		values.put(Calendars.CALENDAR_TIME_ZONE, "Europe/Berlin");
 		Uri.Builder builder = CalendarContract.Calendars.CONTENT_URI
 				.buildUpon();
 		builder.appendQueryParameter(Calendars.ACCOUNT_NAME,
 				"com.grokkingandroid");
 		builder.appendQueryParameter(Calendars.ACCOUNT_TYPE,
 				CalendarContract.ACCOUNT_TYPE_LOCAL);
 		builder.appendQueryParameter(CalendarContract.CALLER_IS_SYNCADAPTER,
 				"true");
 		this.pluginActivity.getContentResolver().insert(builder.build(), values);
 	}
 
 	public Long createEvent(JSONObject arg_object) throws JSONException {
 		long calId = getCalendarId();
 		if (calId == -1) {
 		   // no calendar account; react meaningfully
 		   return -1L;
 		}
 	    
 		ContentValues values = new ContentValues();
 		values.put(Events.DTSTART, arg_object.getLong("startTimeMillis"));
 		values.put(Events.DTEND, arg_object.getLong("endTimeMillis"));
 		/*values.put(Events.RRULE, 
 		      "FREQ=DAILY;COUNT=20;BYDAY=MO,TU,WE,TH,FR;WKST=MO");*/
 		values.put(Events.TITLE, arg_object.getString("title"));
 		values.put(Events.EVENT_LOCATION, arg_object.getString("eventLocation"));
 		values.put(Events.CALENDAR_ID, calId);
 		TimeZone tz = TimeZone.getDefault();
 		Log.i("calendarUtils", "New event with timezone : " + tz.getID());
 		values.put(Events.EVENT_TIMEZONE, tz.getID());
 		values.put(Events.DESCRIPTION, 
 		      "The agenda or some description of the event");
 		// reasonable defaults exist:
 		values.put(Events.ACCESS_LEVEL, Events.ACCESS_PRIVATE);
 		values.put(Events.SELF_ATTENDEE_STATUS,
 		      Events.STATUS_CONFIRMED);
 		values.put(Events.ALL_DAY, 0);
 		values.put(Events.ORGANIZER, "some.mail@some.address.com");
 		values.put(Events.GUESTS_CAN_INVITE_OTHERS, 1);
 		values.put(Events.GUESTS_CAN_MODIFY, 1);
 		values.put(Events.AVAILABILITY, Events.AVAILABILITY_BUSY);
 		Uri uri = 
 		      this.pluginActivity.getContentResolver().
 		            insert(Events.CONTENT_URI, values);
 		long eventId = Long.valueOf(uri.getLastPathSegment());
 		Log.i("calendarUtils", "New event with event_id is created: " + eventId);
		Long reminderMins =  arg_object.getLong("remiderMins");
 		addReminder(eventId, reminderMins);
 		return eventId;
 	}
 	
 	public void addReminder(long eventId, long reminderMins) {
 		ContentValues values = new ContentValues();
 		// adding a reminder:
 		values.clear();
 		values.put(Reminders.EVENT_ID, eventId);
 		values.put(Reminders.METHOD, Reminders.METHOD_ALERT);
 		values.put(Reminders.MINUTES, reminderMins);
 		this.pluginActivity.getContentResolver().insert(Reminders.CONTENT_URI, values); 
 	}
 	
 	public void addAttendee(long eventId) {
 		ContentValues values = new ContentValues();
 		// adding an attendee:
 		values.clear();
 		values.put(Attendees.EVENT_ID, eventId);
 		values.put(Attendees.ATTENDEE_TYPE, Attendees.TYPE_REQUIRED);
 		values.put(Attendees.ATTENDEE_NAME, "Douglas Adams");
 		values.put(Attendees.ATTENDEE_EMAIL, "d.adams@zaphod-b.com");
 		this.pluginActivity.getContentResolver().insert(Attendees.CONTENT_URI, values);
 	}
 	
 	private long getCalendarId() {
 		String[] projection = new String[] { Calendars._ID };
 		/*String selection = Calendars.ACCOUNT_NAME + " = ? "
 				+ Calendars.ACCOUNT_TYPE + " = ? ";
 		// use the same values as above:
 		String[] selArgs = new String[] { MY_ACCOUNT_NAME,
 				CalendarContract.ACCOUNT_TYPE_LOCAL };*/
 		String selection = Calendars.ACCOUNT_NAME + " = ? ";
 		// use the same values as above:
 		String[] selArgs = new String[] { MY_ACCOUNT_NAME};
 		
 		Cursor cursor = this.pluginActivity.getContentResolver().query(Calendars.CONTENT_URI,
 				projection, selection, selArgs, null);
 		if (cursor.moveToFirst()) {
 			Log.i("calendarUtils", "The calendar id is: " + cursor.getLong(0));
 			return cursor.getLong(0);
 		}
 		return -1;
 	}
 	
 	/*
 	 * TODO: Find more 
 	private void getTheEvent(long eventId) {
 		String[] proj = 
 		      new String[]{
 		            Events._ID, 
 		            Events.DTSTART, 
 		            Events.DTEND, 
 		            Events.RRULE, 
 		            Events.TITLE};
 		Cursor cursor = 
 		      this.pluginActivity.getContentResolver().
 		            query(
 		               Events.CONTENT_URI, 
 		               proj, 
 		               Events._ID + " = ? ", 
 		               new String[]{Long.toString(eventId)}, 
 		               null);
 		if (cursor.moveToFirst()) {
 		   // read event data
 		}
 	}
 	
 	private void printOutAllCalendar() {
 		Log.i("calendarUtils", "printOutAllCalendar(): " + "begin at the begining");
 		String[] projection = 
 			      new String[]{
 			            Calendars._ID, 
 			            Calendars.NAME, 
 			            Calendars.ACCOUNT_NAME, 
 			            Calendars.ACCOUNT_TYPE};
 			Cursor calCursor = 
 					this.pluginActivity.getContentResolver().
 			            query(Calendars.CONTENT_URI, 
 			                  projection, 
 			                  Calendars.VISIBLE + " = 1", 
 			                  null, 
 			                  Calendars._ID + " ASC");
 			if (calCursor.moveToFirst()) {
 			   do {
 			      long id = calCursor.getLong(0);
 			      String displayName = calCursor.getString(1);
 			      Log.i("calendarUtils", "printOutAllCalendar() Calendar id: " + id + " and name: " + displayName);
 			      // ...
 			   } while (calCursor.moveToNext());
 			}
 	}
 	
 	private void createAllDayEvent() {
 		long calId = getCalendarId();
 		if (calId == -1) {
 		   // no calendar account; react meaningfully
 		   return;
 		}
 		
 		Calendar cal = new GregorianCalendar(2013, 10, 8);
 		cal.setTimeZone(TimeZone.getTimeZone("UTC"));
 		cal.set(Calendar.HOUR, 0);
 		cal.set(Calendar.MINUTE, 0);
 		cal.set(Calendar.SECOND, 0);
 		cal.set(Calendar.MILLISECOND, 0);
 		
 		long start = cal.getTimeInMillis();
 		ContentValues values = new ContentValues();
 		values.put(Events.DTSTART, start);
 		values.put(Events.DTEND, start);
 		values.put(Events.RRULE, 
 		      "FREQ=DAILY;COUNT=20;BYDAY=MO,TU,WE,TH,FR;WKST=MO");
 		values.put(Events.TITLE, "Some title");
 		values.put(Events.EVENT_LOCATION, "Mnster");
 		values.put(Events.CALENDAR_ID, calId);
 		values.put(Events.EVENT_TIMEZONE, "Europe/Berlin");
 		values.put(Events.DESCRIPTION, 
 		      "The agenda or some description of the event");
 		// reasonable defaults exist:
 		values.put(Events.ACCESS_LEVEL, Events.ACCESS_PRIVATE);
 		values.put(Events.SELF_ATTENDEE_STATUS,
 		      Events.STATUS_CONFIRMED);
 		values.put(Events.ALL_DAY, 1);
 		values.put(Events.ORGANIZER, "some.mail@some.address.com");
 		values.put(Events.GUESTS_CAN_INVITE_OTHERS, 1);
 		values.put(Events.GUESTS_CAN_MODIFY, 1);
 		values.put(Events.AVAILABILITY, Events.AVAILABILITY_BUSY);
 		Uri uri = 
 		      this.pluginActivity.getContentResolver().
 		            insert(Events.CONTENT_URI, values);
 		long eventId = new Long(uri.getLastPathSegment());
 		Log.i("calendarUtils", "New event with event_id is created: " + eventId);
 		addReminder(eventId, 5);
 	}
 	*/
 }
