 package com.ibc.library;
 
import android.app.AlertDialog;
 import android.content.ContentResolver;
 import android.content.ContentValues;
 import android.content.Context;
import android.content.DialogInterface;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Build;
 import android.text.format.DateUtils;
 
 import com.ibc.iBCApplication;
 
 public class CalendarProvider {
 
 	private static CalendarProvider _instance;
 	private static Context _context;
 	private static String CALENDER_URI = "content://calendar/calendars";
 
 	public static CalendarProvider sharedInstance() {
 		if (_instance == null) {
 			_instance = new CalendarProvider();
 			_context = iBCApplication.sharedInstance();
 			if (Integer.parseInt(Build.VERSION.SDK) < 14) {
 				CALENDER_URI = "content://com.android.calendar/calendars";
 			}
 		}
 
 		return _instance;
 	}
 
 	public boolean hasCalendar() {
 		String[] projection = { "_id", "displayName" };
 		String selection = null;//"selected = 1";
 		String[] selectionArgs = null;
 		String sortOrder = null;
 		Cursor cursor = _context.getContentResolver().query( Uri.parse(CALENDER_URI), projection, selection, selectionArgs, sortOrder);
 		if (cursor != null && cursor.moveToFirst()) {
 			int nameColumn = cursor.getColumnIndex("name");
 			int idColumn = cursor.getColumnIndex("_id");
 			String[] calNames = new String[cursor.getCount()];
 			int[] calIds = new int[cursor.getCount()];
 			for (int i = 0; i < calNames.length; i++) {
 				calIds[i] = cursor.getInt(0);
 				calNames[i] = cursor.getString(1);
 				System.out.println(calNames[i]);
 
 				cursor.moveToNext();
 			}
 			cursor.close();
 			if (calIds.length > 0) {
 				// WE'RE SAFE HERE TO DO ANY FURTHER WORK
 			}
 		}
 		if (cursor != null) {
 			return (cursor.getCount() != 0);
 		} else {
 			return false;
 		}
 	}
 	
 	public void insertEvent(int cal_id) {
 
 		// set the content value
 		ContentValues cv = new ContentValues();
 
 		// make sure you add it to the right calendar
 		cv.put("calendar_id", cal_id);
 
 		// set the title of the event
 		cv.put("title", "demon's birthday");
 
 		// set the description of the event
 		cv.put("description", "Time to celebrate demon's birthday.");
 
 		// set the event's physical location
 		cv.put("eventLocation", "Nucam, Barcelona");
 
 		// set the start and end time
 		// note: you're going to need to convert the desired date into milliseconds
 		cv.put("dtstart", System.currentTimeMillis());
 		cv.put("dtend", System.currentTimeMillis() + DateUtils.DAY_IN_MILLIS);
 
 		// let the calendar know whether this event goes on all day or not
 		// true = 1, false = 0
 		cv.put("allDay", 1);
 
 		// let the calendar know whether an alarm should go off for this event
 		cv.put("hasAlarm", 1);
 
 		// once desired fields are set, insert it into the table
 		_context.getContentResolver().insert(Uri.parse("content://calendar/events"), cv);
 	}
 	
 	/**
 	 * Adds the event to a calendar. It lets the user choose the calendar
 	 * @param ctx Context ( Please use the application context )
 	 * @param title title of the event
 	 * @param dtstart Start time: The value is the number of milliseconds since Jan. 1, 1970, midnight GMT.
 	 * @param dtend End time: The value is the number of milliseconds since Jan. 1, 1970, midnight GMT.
 	 */
 	public static void addToCalendar(Context ctx, final String title,
 			final long dtstart, final long dtend) {
 		final ContentResolver cr = ctx.getContentResolver();
 		Cursor cursor;
 		if (Integer.parseInt(Build.VERSION.SDK) == 8)
 			cursor = cr.query(
 					Uri.parse("content://com.android.calendar/calendars"),
 					new String[] { "_id", "displayname" }, null, null, null);
 		else
 			cursor = cr.query(Uri.parse("content://com.android.calendar/calendars"),
 					new String[] { "_id", "displayname" }, null, null, null);
 		if (cursor != null) {
 			if (cursor.moveToFirst()) {
 				final String[] calNames = new String[cursor.getCount()];
 				final int[] calIds = new int[cursor.getCount()];
 				for (int i = 0; i < calNames.length; i++) {
 					calIds[i] = cursor.getInt(0);
 					calNames[i] = cursor.getString(1);
 					cursor.moveToNext();
 				}
 				//
 				ContentValues cv = new ContentValues();
 				cv.put("calendar_id", calIds[0]);
 				cv.put("title", title);
 				cv.put("dtstart", dtstart);
 				cv.put("hasAlarm", 1);
 				cv.put("dtend", dtend);
 
 				Uri newEvent;
 				if (Integer.parseInt(Build.VERSION.SDK) == 8)
 					newEvent = cr.insert(
 							Uri.parse("content://com.android.calendar/events"),
 							cv);
 				else
 					newEvent = cr.insert(
 							Uri.parse("content://com.android.calendar/events"),
 							cv);
 
 				if (newEvent != null) {
 					long id = Long.parseLong(newEvent
 							.getLastPathSegment());
 					ContentValues values = new ContentValues();
 					values.put("event_id", id);
 					values.put("method", 1);
 					values.put("minutes", 15); // 15 minuti
 					if (Integer.parseInt(Build.VERSION.SDK) == 8)
 						cr.insert(
 								Uri.parse("content://com.android.calendar/reminders"),
 								values);
 					else
 						cr.insert(
 								Uri.parse("content://com.android.calendar/reminders"),
 								values);
 
 				}
 				/*
 				AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
 				builder.setSingleChoiceItems(calNames, -1,
 						new DialogInterface.OnClickListener() {
 
 							@Override
 							public void onClick(DialogInterface dialog,
 									int which) {
 								ContentValues cv = new ContentValues();
 								cv.put("calendar_id", calIds[which]);
 								cv.put("title", title);
 								cv.put("dtstart", dtstart);
 								cv.put("hasAlarm", 1);
 								cv.put("dtend", dtend);
 
 								Uri newEvent;
 								if (Integer.parseInt(Build.VERSION.SDK) == 8)
 									newEvent = cr.insert(
 											Uri.parse("content://com.android.calendar/events"),
 											cv);
 								else
 									newEvent = cr.insert(
 											Uri.parse("content://com.android.calendar/events"),
 											cv);
 
 								if (newEvent != null) {
 									long id = Long.parseLong(newEvent
 											.getLastPathSegment());
 									ContentValues values = new ContentValues();
 									values.put("event_id", id);
 									values.put("method", 1);
 									values.put("minutes", 15); // 15 minuti
 									if (Integer.parseInt(Build.VERSION.SDK) == 8)
 										cr.insert(
 												Uri.parse("content://com.android.calendar/reminders"),
 												values);
 									else
 										cr.insert(
 												Uri.parse("content://com.android.calendar/reminders"),
 												values);
 
 								}
 								dialog.cancel();
 							}
 
 						});
 
 				builder.create().show();
 				*/
 			}
 			cursor.close();
 		}
 	}
 }
