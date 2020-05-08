 package com.example.calendar;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 
 public class DbHandler extends SQLiteOpenHelper {
 
 	// All Static variables
 	// Database Version
 	private static final int DATABASE_VERSION = 3;
 	
 	// Database Name
 	private static final String DATABASE_NAME = "HPL";
 	
 	// Table Name(s)
 	private static final String EVENT_TABLE = "Events";
 	
 	private static final String KEY_ID = "Id";
 	private static final String KEY_DATE = "Date";
 	private static final String KEY_NAME = "Name";
 	private static final String KEY_LOC = "Location";
 
 	// This is the commandline to be used in the lower call of execSQL(). This
 	// is where the schema for the db is determined
 
 	// Proposed alternative would be just having event_id as the key and and
 	// event object as the second column
 
 	public DbHandler(Context context) {
 		super(context, DATABASE_NAME, null, DATABASE_VERSION);
 	}
 
 	@Override
 	public void onCreate(SQLiteDatabase db) {
 		String DICTIONARY_TABLE_CREATE = "CREATE TABLE "
 				+ EVENT_TABLE + " (" + KEY_ID + " INTEGER PRIMARY KEY, " + KEY_DATE + " TEXT, " + KEY_NAME + " TEXT, "
 				+ KEY_LOC + " TEXT);";
 		db.execSQL(DICTIONARY_TABLE_CREATE);
 	}
 
 	@Override
 	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 		db.execSQL("DROP TABLE IF EXISTS " + EVENT_TABLE);
 
 		onCreate(db);
 	}
 
 	/**
 	 * All CRUD(Create, Read, Update, Delete) Operations
 	 */
 
 	// Adding new Event
 	public void addEvent(CalendarEvent event) {
 		SQLiteDatabase db = getWritableDatabase();
 		
 		ContentValues cv = new ContentValues();
 		cv.put(KEY_DATE, event.getDate());
 		cv.put(KEY_NAME, event.getName());
 		cv.put(KEY_LOC, event.getLocation());
 		
 		// Inserting Row
 		db.insert(EVENT_TABLE, null, cv);
 		db.close(); // Closing database connection
 	}
 
 	// Getting single Event
 	CalendarEvent getEvent(String eventDate) {
 		SQLiteDatabase db = this.getReadableDatabase();
 
 		Cursor cur = db.query(EVENT_TABLE, new String[] { KEY_ID, KEY_DATE,
 				KEY_NAME, KEY_LOC }, KEY_DATE + "=?",
 				new String[] { eventDate }, null, null, null, null);
 
 		//db.close(); // Closing database connection
 		if (cur != null)
 			cur.moveToFirst();
 
 		CalendarEvent event = new CalendarEvent(cur.getInt(0),
 				cur.getString(1), cur.getString(2), cur.getString(3));
 		// return event
 		return event;
 	}
 	
 	CalendarEvent getEvent(int id) {
 		SQLiteDatabase db = this.getReadableDatabase();
 
		Cursor cur = db.query(EVENT_TABLE, new String[] { KEY_DATE,
 				KEY_NAME, KEY_LOC }, KEY_ID + "=?",
 				new String[] { String.valueOf(id) }, null, null, null, null);
 
 		db.close(); // Closing database connection
 		if (cur != null)
 			cur.moveToFirst();
 
 		CalendarEvent event = new CalendarEvent(cur.getInt(0),
 				cur.getString(1), cur.getString(2), cur.getString(3));
 		// return event
 		return event;
 	}
 	
 	// Getting All Contacts
 	public ArrayList<CalendarEvent> getAllEvents() {
 		ArrayList<CalendarEvent> eventList = new ArrayList<CalendarEvent>();
 		
 		// Select All Query
 		String selectQuery = "SELECT  * FROM " + EVENT_TABLE;
 
 		SQLiteDatabase db = this.getWritableDatabase();
 		Cursor cursor = db.rawQuery(selectQuery, null);
 
 		// looping through all rows and adding to list
 		if (cursor.moveToFirst()) {
 			do {
 				CalendarEvent e = new CalendarEvent();
 				e.setDate(cursor.getString(0));
 				e.setName(cursor.getString(1));
 				e.setLocation(cursor.getString(2));
 				// Adding contact to list
 				eventList.add(e);
 			} while (cursor.moveToNext());
 		}
 
 		// return contact list
 		return eventList;
 	}
 
 	// Updating single Event
 	public int updateEvent(CalendarEvent event) {
 		SQLiteDatabase db = this.getWritableDatabase();
 
 		ContentValues values = new ContentValues();
 		values.put(KEY_ID, event.getID());
 		values.put(KEY_NAME, event.getName());
 		values.put(KEY_DATE, event.getDate());
 		values.put(KEY_LOC, event.getLocation());
 
 		// updating row
 		return db.update(EVENT_TABLE, values, KEY_ID + " = ?",
 				new String[] {  String.valueOf(event.getID()) });
 	}
 
 	// Deleting single event
 	public void deleteEvent(CalendarEvent event) {
 		SQLiteDatabase db = this.getWritableDatabase();
 		db.delete(EVENT_TABLE, KEY_ID + " = ?",
 				new String[] { String.valueOf(event.getID()) });
 		db.close();
 	}
 
 
 	// Getting contacts Count
 	public int getEventsCount() {
 		String countQuery = "SELECT  * FROM " + EVENT_TABLE;
 		SQLiteDatabase db = this.getReadableDatabase();
 		Cursor cursor = db.rawQuery(countQuery, null);
 		cursor.close();
 		db.close();
 		// return count
 		return cursor.getCount();
 	}
 
 }
