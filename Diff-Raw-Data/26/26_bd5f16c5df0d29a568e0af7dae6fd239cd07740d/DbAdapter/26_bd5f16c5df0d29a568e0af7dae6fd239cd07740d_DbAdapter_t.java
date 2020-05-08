 package clock.db;
 
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 import android.util.Log;
 
 public class DbAdapter 
 {
 	// Database fields
 	private SQLiteDatabase database;
 	private Connection connection;
 	private String[] allColumns = { Connection.COLUMN_ID,
 									Connection.COLUMN_DATE,
 									Connection.COLUMN_LOCATION,
 									Connection.COLUMN_DETAILS,
 									Connection.COLUMN_ALARM};
 	
 	public DbAdapter(Context context) 
 	{
 		try {
 			connection = new Connection(context);
 		} 
 		catch (IOException e){
 			Log.d("DBAdapter", "Error in constractor");
 		}
 	}
 
 	public void open() throws SQLException 
 	{
 		database = connection.openDataBase();
 	}
 
 	public void close() 
 	{
 		database.close();
 		connection.close();
 	}
 	
 	public boolean isAddressTableEmpty()
 	{
 		boolean isEmpty = true;
 		
 		this.open();
 		
 		//TODO: what is the best way to do this?
 		
 		this.close();
 		
 		return isEmpty;
 	}
 
 	public Event createEvent(Event event) 
 	{
 		ContentValues values = new ContentValues();
 		String dateSqlFormat = Event.getSqlRepresent(event); 
 		values.put(Connection.COLUMN_DATE, dateSqlFormat);
 		values.put(Connection.COLUMN_LOCATION, event.getLocation());
 		values.put(Connection.COLUMN_DETAILS, event.getDetails());
 		values.put(Connection.COLUMN_ALARM, event.getWithAlarmStatus() == false? 0 : 1);
 		long insertId = database.insert(Connection.TABLE_EVENTS, null, values);
 		Cursor cursor = database.query(Connection.TABLE_EVENTS, allColumns, Connection.COLUMN_ID + " = " + insertId, null,
 										null, null, null);
 		
 		cursor.moveToFirst();
 		Event newEvent = cursorToEvent(cursor);
 		cursor.close();
 		return newEvent;
 	}
 
 	public void deleteEvent(Event event) 
 	{
 		long id = event.getId();
 		System.out.println("Comment deleted with id: " + id);
 		database.delete(Connection.TABLE_EVENTS, Connection.COLUMN_ID
 				+ " = " + id, null);
 	}
 	
 	public HashMap<Integer, List<Event>> getEventsMapForMonth(int month, int year)
 	{
 		//TODO: quickly replace 31 to the const MAX_DAYS_IN_MONTH
 		HashMap<Integer, List<Event>> eventsMap = new HashMap<Integer, List<Event>>(31);
 		String yearMonth = String.valueOf(year) + "-";
 		yearMonth += month < 10 ? "0" + month : month;
 		Cursor cursor = database.rawQuery("SELECT * FROM " + Connection.TABLE_EVENTS + " WHERE " +
 				"date <= '" + yearMonth + "-31 23-59-59'" + " AND " +
 				"date >= '" + yearMonth + "-00 00-00-00'", null); 
 				//database.query(Connection.TABLE_EVENTS, allColumns, Connection.COLUMN_MONTH + "=" + month, 
 				//null, null, null, null);
 	
 		cursor.moveToFirst();
 		while (!cursor.isAfterLast()) 
 		{
 			Event event = cursorToEvent(cursor);
 			List<Event> dayList = eventsMap.get(event.getDay());
 			if (dayList == null) // first event for day
 			{
 				dayList = new ArrayList<Event>();
 				eventsMap.put(new Integer(event.getDay()), dayList);
 			}
 			dayList.add(event);
 			cursor.moveToNext();
 		}
 		
 		return eventsMap;
 	}
 	
 	public List<Event> getAllEvents() 
 	{
 		List<Event> events = new ArrayList<Event>();
 
 		Cursor cursor = database.query(Connection.TABLE_EVENTS,
 				allColumns, null, null, null, null, null);
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
 		
 	/**
 	 * get the next event from the database.
 	 * returns null if no upcoming events.
 	 */
 	public Event getNextEvent()
 	{
 		Event retEvent = null;
 		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd' 'HH-mm-ss");
         String currentTime = sdf.format(new Date());
 		Cursor cursor = database.rawQuery("SELECT * FROM " + Connection.TABLE_EVENTS + " WHERE " +
 				"date > '" + currentTime + "' order by date limit 1", null);
 		Log.d("Next SQL", "SELECT * FROM " + Connection.TABLE_EVENTS + " WHERE " +
 				"date > '" + currentTime + "' order by date limit 1");
 		cursor.moveToFirst();
 		if (!cursor.isAfterLast())
 		{
 			cursor.moveToFirst();
 			retEvent = cursorToEvent(cursor);
 		}
 		if (retEvent != null)
 		{
 			Log.d("NEXT EVENT", retEvent.toString());
 		}
 		else
 		{
 			Log.d("NEXT EVENT", " NO EVENT");
 		}
 		
 		return retEvent;
 	}
 	
 	private Event cursorToEvent(Cursor cursor) 
 	{
 		Event event = Event.createNewInstance();
 		event.setId(cursor.getLong(0));
 		event.setDateFromSql(cursor.getString(1));
 		event.setLocation(cursor.getString(2));
 		event.setDetails(cursor.getString(3));
 		event.setWithAlarmStatus(cursor.getInt(4) == 1);
 		return event;
 	}
 
 	public String[] getStreetSugg(String constrain) 
 	{
 		String[] sugg = new String[0];
 		if (constrain.length() > 2)
 		{
			Cursor cursor = database.query(Connection.TABLE_ADDRESS, new String[] {Connection.COLUMN_STREET}, Connection.COLUMN_STREET + " LIKE ? limit 3",
					new String[] {constrain + "%"}, null, null, null);
//			Cursor cursor = database.rawQuery("SELECT DISTINCT " + Connection.COLUMN_STREET + 
//					" FROM " + Connection.TABLE_ADDRESS + " where " + Connection.COLUMN_STREET + " LIKE '" + constrain + "%' limit 3", null); 
 			cursor.moveToFirst();
 			sugg = new String[cursor.getCount()];
 			int i = 0;
 			while (!cursor.isAfterLast()) 
 			{
 				sugg[i] = cursor.getString(0);
 				cursor.moveToNext();
 				i++;
 			}
 		}
 		
 		return sugg;
 		
 	}
 }
