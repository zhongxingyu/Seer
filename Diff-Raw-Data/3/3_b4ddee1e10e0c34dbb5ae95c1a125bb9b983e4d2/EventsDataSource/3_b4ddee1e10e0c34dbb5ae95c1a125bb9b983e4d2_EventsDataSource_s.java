 package fi.local.social.network.db;
 
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 
 public class EventsDataSource implements DataSource{
 
 	// Database fields
 	private SQLiteDatabase database;
 	private MySQLiteHelper dbHelper;
 
 	public EventsDataSource(Context context) {
 		dbHelper = new MySQLiteHelper(context);
 	}
 
 	@Override
 	public void open() throws SQLException {
 		database = dbHelper.getWritableDatabase();
 	}
 
 	@Override
 	public void close() {
 		dbHelper.close();
 	}
 
 	@Override
 	public Object createEntry(String data) {
 		
 		ContentValues values = new ContentValues();
 		
 
 		
 		int endTitle = data.indexOf(";");
 		int endUser = data.indexOf(";", endTitle+1);
 		int endStartTime = data.indexOf(";", endUser+1);
 		int endEndTime = data.indexOf(";", endStartTime+1);
 		int endDescription = data.indexOf(";", endEndTime+1);
 		
 		String title = data.substring(0, endTitle);
 		String user = data.substring(endTitle+1, endUser);
 		Long starttime = Long.getLong(data.substring(endUser+1, endStartTime ));
 		Long endtime = Long.getLong(data.substring(endStartTime+1, endEndTime ));
 		String description = (data.substring(endEndTime+1, endDescription ));
 		String profilePicURI = (data.substring(endDescription+1, data.length() ));
 		
 		
 		//TODO start and endtime check
 		starttime = System.currentTimeMillis();
 		endtime = System.currentTimeMillis();
 		if(";".equals(profilePicURI))
 			profilePicURI = "";
 		
 		
 		values.put(MySQLiteHelper.COLUMN_TIMESTAMP, System.currentTimeMillis());
 		values.put(MySQLiteHelper.COLUMN_TITLE, title);
 		values.put(MySQLiteHelper.COLUMN_USERNAME, user);
 		values.put(MySQLiteHelper.COLUMN_STARTTIME, starttime);
 		values.put(MySQLiteHelper.COLUMN_ENDTIME, endtime);
 		values.put(MySQLiteHelper.COLUMN_DESCRIPTION, description);
 		values.put(MySQLiteHelper.COLUMN_PICPROFILEURI, profilePicURI);
 		
 		long insertId = database.insert(MySQLiteHelper.TABLE_EVENTS, null,values);
 		
 		String[] allColumnNames = dbHelper.getAllColumnNames(MySQLiteHelper.TABLE_EVENTS);
 		Cursor cursor = database.query(MySQLiteHelper.TABLE_EVENTS,
 				allColumnNames, MySQLiteHelper.COLUMN_ID + " = " + insertId, null,
 				null, null, null);
 		
 		Event event = null;
 		if(cursor.moveToFirst())
 			event = cursorToEvent(cursor);
 		cursor.close();
 		return event; 
 	}
 
 	@Override
 	public List<Event> getAllEntries() {
 		List<Event> events = new ArrayList<Event>();
 
 		String[] allColumnNames = dbHelper.getAllColumnNames(MySQLiteHelper.TABLE_EVENTS);
 		
 		
 		Cursor cursor = database.query(MySQLiteHelper.TABLE_EVENTS,
 				allColumnNames, null, null, null, null, null);
 
 		cursor.moveToFirst();
 		while (!cursor.isAfterLast()) {
 			Event event = cursorToEvent(cursor);
 			events.add(event);
 			cursor.moveToNext();
 		}
 		
 		// Make sure to close the cursor
 		cursor.close();
 		return events;
 	}
 
 	private Event cursorToEvent(Cursor cursor) {
 		Event event = new EventImpl();
 		event.setID(cursor.getLong(0));
 
 		long ts = cursor.getLong(1);
 		Timestamp timestamp = new Timestamp(ts);
 		event.setTimestamp(timestamp);
 
 		
 		event.setTitle(cursor.getString(2));
 		event.setUser(cursor.getString(3));
 		
 		ts = cursor.getLong(4);
 		timestamp = new Timestamp(ts);
 		event.setStartTime(timestamp);
 		
 		ts = cursor.getLong(5);
 		timestamp = new Timestamp(ts);
 		event.setEndTime(timestamp);
 		
 		event.setDescription(cursor.getString(6));
 		
 		event.setProfilePicURI(cursor.getString(7));
 		
 
 		return event;
 	}
 	
 	public void deleteEvent(Event ev) 
 	{
 		long id = ev.getID();
 		System.out.println("User deleted with id: " + ev);
 		database.delete(MySQLiteHelper.TABLE_EVENTS, MySQLiteHelper.COLUMN_ID
 				+ " = " + id, null);
 	}
 
 
 }
