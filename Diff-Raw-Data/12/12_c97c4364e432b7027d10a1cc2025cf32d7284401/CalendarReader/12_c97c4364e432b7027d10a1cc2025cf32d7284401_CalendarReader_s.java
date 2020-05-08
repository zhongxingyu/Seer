 package com.dutymator;
 
 import android.content.ContentResolver;
 import android.content.ContentUris;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteException;
 import android.net.Uri;
 import android.os.Build;
 import android.text.format.DateUtils;
 
 import java.util.*;
 
 /**
  * @author Zsolt Takacs <zsolt@takacs.cc>
  */
 public class CalendarReader {
     public Map<String, String> getCalendarIds(Context context) {
         ContentResolver contentResolver = context.getContentResolver();
 
        String calendarUrl = Integer.parseInt(Build.VERSION.SDK) >= Build.VERSION_CODES.FROYO ?
            "content://com.android.calendar" : "content://calendar";
 
         final Cursor cursor = contentResolver.query(Uri.parse(calendarUrl + "/calendars"),
                 (new String[] { "_id", "displayName" }), null, null, null);
 
         HashMap<String, String> calendars = new HashMap<String, String>();
 
         if (cursor != null) {
             while (cursor.moveToNext()) {
 
                 final String _id = cursor.getString(0);
                 final String displayName = cursor.getString(1);
 
                 calendars.put(_id, displayName);
             }
         }
 
         return calendars;
     }
 
     public ArrayList<Event> getEventsFromCalendar(Context context, int calendarId, Date allDayFrom, Date allDayTo)
         throws IntermittentException {
         ContentResolver contentResolver = context.getContentResolver();
 
        Uri.Builder builder = Uri.parse("content://com.android.calendar/instances/when").buildUpon();
         long now = new Date().getTime();
         ContentUris.appendId(builder, now - DateUtils.DAY_IN_MILLIS * 2);
         ContentUris.appendId(builder, now + DateUtils.WEEK_IN_MILLIS);
 
         try {
             Cursor eventCursor = contentResolver.query(builder.build(),
                 new String[] { "_id", "title", "begin", "end", "allDay", "eventTimezone"},
                 "Calendars._id = " + calendarId, null, "begin ASC");
 
             ArrayList<Event> events = new ArrayList<Event>();
 
             if (eventCursor != null) {
                 while (eventCursor.moveToNext()) {
                     Event event = new Event();
                     event._id = eventCursor.getString(0);
                     event.title = eventCursor.getString(1);
                     event.begin = new Date(eventCursor.getLong(2));
                     event.end = new Date(eventCursor.getLong(3));
                     event.allDay = eventCursor.getInt(4) != 0;
                     event.beginTimestamp = eventCursor.getLong(2);
                     event.endTimestamp = eventCursor.getLong(3);
 
                     if (!event.title.equals("busy")) {
                         processEvent(allDayFrom, allDayTo, event);
 
                         events.add(event);
                     }
                 }
 
                 Collections.sort(events);
             }
             return events;
         } catch (SQLiteException e) {
             throw new IntermittentException(e);
         }
     }
 
     public String[] getCalendarEntries(Context context) {
         Map<String, String> calendars = getCalendarIds(context);
 
         String[] entries = new String[calendars.size()];
 
         calendars.values().toArray(entries);
 
         return entries;
     }
 
     public String[] getCalendarValues(Context context) {
         Map<String, String> calendars = getCalendarIds(context);
 
         String[] entries = new String[calendars.size()];
 
         calendars.keySet().toArray(entries);
 
         return entries;
     }
 
     public Event getActiveEventFromCalendar(Context context, int calendarId, Date allDayFrom, Date allDayTo)
         throws IntermittentException
     {
         ArrayList<Event> events = getEventsFromCalendar(context, calendarId, allDayFrom, allDayTo);
 
         Event activeEvent = null;
 
         for (Event event : events) {
             if (event.isActive()) {
                 activeEvent = event;
             }
         }
 
         return activeEvent;
     }
 
     private void processEvent(Date allDayFrom, Date allDayTo, Event event) {
         if (event.allDay) {
             event.begin = new Date(
                 event.begin.getYear(), event.begin.getMonth(), event.begin.getDate(), allDayFrom.getHours(),
                 allDayFrom.getMinutes()
             );
 
             if (event.end.getHours() > event.begin.getHours()) {
                 Calendar day = new GregorianCalendar(event.end.getYear(), event.end.getMonth(), event.end.getDate());
                 day.add(Calendar.DAY_OF_MONTH, -1);
 
                 event.end = new Date(
                     day.get(Calendar.YEAR), day.get(Calendar.MONTH), day.get(Calendar.DAY_OF_MONTH),
                     event.end.getHours(), event.end.getMinutes()
                 );
             } else {
                 event.end = new Date(
                     event.end.getYear(), event.end.getMonth(), event.end.getDate(), allDayTo.getHours(),
                     allDayTo.getMinutes()
                 );
             }
         }
     }
 }
