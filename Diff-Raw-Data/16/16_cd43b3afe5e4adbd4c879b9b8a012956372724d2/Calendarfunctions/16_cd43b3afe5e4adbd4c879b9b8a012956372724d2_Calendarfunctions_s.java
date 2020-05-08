 package com.example.opnedatatestapp;
 
 import android.app.Activity;
 import android.content.ContentValues;
 import android.database.Cursor;
 import android.net.Uri;
 import android.provider.CalendarContract;
 import android.provider.CalendarContract.*;
 
 public class Calendarfunctions extends Activity {
 
 	public static void main(String[] args) {
 
 
 	}
 
 	public void metode() {
 		String[] projection = 
 				new String[]{
 				Calendars._ID, 
 				Calendars.NAME, 
 				Calendars.ACCOUNT_NAME, 
 				Calendars.ACCOUNT_TYPE};
 		Cursor calCursor = 
 				getContentResolver().
 				query(Calendars.CONTENT_URI, 
 						projection, 
 						Calendars.VISIBLE + " = 1", 
 						null, 
 						Calendars._ID + " ASC");
 		if (calCursor.moveToFirst()) {
 			do {
 				long id = calCursor.getLong(0);
 				String displayName = calCursor.getString(1);
 				// ...
 			} while (calCursor.moveToNext());
 		}
 
 	}
 
 
 
 	public void metode2() {
 		ContentValues values = new ContentValues();
 		values.put(
 				Calendars.ACCOUNT_NAME,
				MY_ACCOUNT_NAME);
 		values.put(
 				Calendars.ACCOUNT_TYPE, 
 				CalendarContract.ACCOUNT_TYPE_LOCAL);
 		values.put(
 				Calendars.NAME, 
 				"GrokkingAndroid Calendar");
 		values.put(
 				Calendars.CALENDAR_DISPLAY_NAME, 
 				"GrokkingAndroid Calendar");
 		values.put(
 				Calendars.CALENDAR_COLOR, 
 				0xffff0000);
 		values.put(
 				Calendars.CALENDAR_ACCESS_LEVEL, 
 				Calendars.CAL_ACCESS_OWNER);
 		values.put(
 				Calendars.OWNER_ACCOUNT, 
 				"some.account@googlemail.com");
 		values.put(
 				Calendars.CALENDAR_TIME_ZONE, 
 				"Europe/Berlin");
 		Uri.Builder builder = 
 				CalendarContract.Calendars.CONTENT_URI.buildUpon(); 
 		builder.appendQueryParameter(
 				Calendars.ACCOUNT_NAME, 
 				"com.grokkingandroid");
 		builder.appendQueryParameter(
 				Calendars.ACCOUNT_TYPE, 
 				CalendarContract.ACCOUNT_TYPE_LOCAL);
 		builder.appendQueryParameter(
 				CalendarContract.CALLER_IS_SYNCADAPTER, 
 				"true");
 		Uri uri = 
 				getContentResolver().insert(builder.build(), values);
 	}
 }
