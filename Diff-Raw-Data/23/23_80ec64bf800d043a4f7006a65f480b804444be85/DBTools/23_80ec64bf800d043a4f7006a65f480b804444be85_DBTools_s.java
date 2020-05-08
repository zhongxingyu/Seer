 package ie.sortons.events.ucd;
 
 
 import java.util.Date;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Locale;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.util.Log;
 
 public class DBTools extends SQLiteOpenHelper {
 	
 	public DBTools(Context app_context) {
 		super(app_context, "modulelist.db", null, 1);
 		
 	}
 
 	@Override
 	public void onCreate(SQLiteDatabase db) {
 		String q = "CREATE TABLE events ( id INTEGER PRIMARY KEY AUTOINCREMENT," + 
                    "eventId INTEGER, name TEXT, location TEXT," + 
 				   "startTimeDate TEXT, startTime TEXT, latitude REAL," +
 				   "longitude REAL )";
 		db.execSQL(q);
 		q = "CREATE TABLE sourcePages ( id INTEGER PRIMARY KEY AUTOINCREMENT," + 
 				"eventId INTEGER, pageId INTEGER, name TEXT," + 
 				"pageUrl TEXT )";
 		db.execSQL(q);		
 	}
 
 	@Override
 	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 		String q = "DROP TABLE IF EXISTS events";
 		db.execSQL(q);
 		q = "DROP TABLE IF EXISTS sourcePages";
 		db.execSQL(q);
 		onCreate(db);
 	}
 	
 	public void insertEvents(List<HashMap<String, String>> values) {
 		SQLiteDatabase db = this.getWritableDatabase();
 		for (int i = 0; i < values.size(); i++) {
 			ContentValues vals = new ContentValues();
 			vals.put("eventId", values.get(i).get("eventId"));
 			vals.put("name", values.get(i).get("name"));
 			vals.put("location", values.get(i).get("location"));
 			vals.put("startTimeDate", values.get(i).get("startTimeDate"));
 			vals.put("startTime", values.get(i).get("startTime"));
 			vals.put("latitude", values.get(i).get("latitude"));
 			vals.put("longitude", values.get(i).get("longitude"));
 			Log.i("insertEvents", "added event");
 			db.insert("events", null, vals);
 		}
 		db.close();
 	}
 	
 	public void insertSourcePages(List<HashMap<String, String>> values) {
 		SQLiteDatabase db = this.getWritableDatabase();
 		for (int i = 0; i < values.size(); i++) {
 			ContentValues vals = new ContentValues();
 			vals.put("eventId", values.get(i).get("eventId"));
 			vals.put("pageId", values.get(i).get("pageId"));
 			vals.put("name", values.get(i).get("name"));
 			vals.put("pageUrl", values.get(i).get("pageUrl"));
 			
 			db.insert("sourcePages", null, vals);
 		}
 		db.close();
 	}
 	//should be called by deleteAllEventsBeforeDate
 	public void deleteEvents(List<String> eventId) {
 		SQLiteDatabase db = this.getWritableDatabase();
 		for(int i = 0; i < eventId.size(); i++) {
 			String q = "DELETE FROM events WHERE eventId='" + eventId.get(i) + "'";
 			db.execSQL(q);
 			q = "DELETE FROM sourcePages WHERE eventId='" + eventId.get(i) + "'";
 			db.execSQL(q);
 		}
 		db.close();
 	}
 	//pass in Date and all events before date are deleted
 	public void deleteAllEventsBeforeDate(Date before) {
 		ArrayList<HashMap<String, String>> events = getEvents();
 		ArrayList<String> toDelete = new ArrayList<String>();
 		for (int i = 0; i < events.size(); i++) {
 			try {
 				Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.ENGLISH).parse(events.get(i).get("startTimeDate"));
 				if (date.before(before)) {
 					toDelete.add(events.get(i).get("eventId"));
 				}
 			} catch (ParseException e) {
 				; //doNothing
 			}
 		}
 		deleteEvents(toDelete);
 	}
 	
 	public ArrayList<HashMap<String, String>> getEvents() {
 		ArrayList<HashMap<String, String>> eventList = new ArrayList<HashMap<String, String>>();
 		String q = "SELECT * FROM events";
 		SQLiteDatabase db = this.getWritableDatabase();
 		Cursor cursor = db.rawQuery(q, null);
 		
 		if (cursor.moveToFirst()) {
 			
 			do{
				HashMap<String, String> module = new HashMap<String, String>();
				module.put("id", cursor.getString(0));
				module.put("eventId", cursor.getString(1));
				module.put("name", cursor.getString(2));
				module.put("location", cursor.getString(3));
				module.put("startTimeDate", cursor.getString(4));
				module.put("startTime", cursor.getString(5));
				module.put("latitude", cursor.getString(6));
				module.put("longitude", cursor.getString(7));
 				
 				
				eventList.add(module);
 			} while(cursor.moveToNext());
 		}
		db.close();
 		return eventList;
 	}
 	
 	public HashMap<String, String> getEventInfo(String id) {
 		HashMap<String, String> event = new HashMap<String, String>();
 		SQLiteDatabase db = this.getReadableDatabase();
 		String q = "SELECT * FROM modules WHERE moduleId='" + id + "'";
 		Cursor cursor = db.rawQuery(q, null);
 		if (cursor.moveToFirst()) {
 			event.put("id", cursor.getString(0));
 			event.put("eventId", cursor.getString(1));
 			event.put("name", cursor.getString(2));
 			event.put("location", cursor.getString(3));
 			event.put("startTimeDate", cursor.getString(4));
 			event.put("startTime", cursor.getString(5));
 			event.put("latitude", cursor.getString(6));
 			event.put("longitude", cursor.getString(7));
 		}
 		db.close();
 		return event;
 	}
 	
 	public List<HashMap<String,String>> getSourcePagesForEvent(String eventId) {
 		ArrayList<HashMap<String, String>> pageList = new ArrayList<HashMap<String, String>>();
 		SQLiteDatabase db = this.getReadableDatabase();
 		String q = "SELECT * FROM sourcePages WHERE eventId='" + eventId + "'";
 		Cursor cursor = db.rawQuery(q, null);
 		if (cursor.moveToFirst()) {
 			HashMap<String, String> page  = new HashMap<String,String>();
 			do {
 				page.put("id", cursor.getString(0));
 				page.put("eventId", cursor.getString(1));
 				page.put("name", cursor.getString(2));
 				page.put("location", cursor.getString(3));
 				page.put("startTimeDate", cursor.getString(4));
 				page.put("startTime", cursor.getString(5));
 				page.put("latitude", cursor.getString(6));
 				page.put("longitude", cursor.getString(7));
 				pageList.add(page);
 			}	while (cursor.moveToNext());
 		}
 		db.close();
 		return pageList;
 	}
 }
