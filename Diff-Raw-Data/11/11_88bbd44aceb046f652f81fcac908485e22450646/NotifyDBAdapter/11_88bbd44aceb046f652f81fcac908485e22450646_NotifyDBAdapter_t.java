 package com.bitty.notifyme;
 
 import java.util.ArrayList;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteException;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.database.sqlite.SQLiteDatabase.CursorFactory;
 import android.util.Log;
 
 import com.bitty.utils.Serializer;
 
 public class NotifyDBAdapter
 {
 	private static final String TAG = "NotifyMeDBAdapter";
 
 	private static final int DATABASE_VERSION = 4;
 	private static final String DATABASE_NAME = "notifyMe.db";
 	private static final String DATABASE_TABLE = "notifyItems";
 
 	public static final String KEY_ID = "_id";
 	public static final String KEY_NOTIFY_DAY = "day";
 	public static final String KEY_NOTIFY_SUBWAYS = "subways";
 	public static final String KEY_NOTIFY_HOUR = "hour";
 	public static final String KEY_NOTIFY_MINUTES = "minutes";
 
 	private SQLiteDatabase db;
 	private final Context context;
 	private notifyDBOpenHelper dbHelper;
 
 	public NotifyDBAdapter(Context _context)
 	{
 		this.context = _context;
 		dbHelper = new notifyDBOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION);
 		Log.w(TAG, "NotifyMeDBAdapter contructor");
 	}
 
 	public void close()
 	{
 		db.close();
 	}
 
 	public void open() throws SQLiteException
 	{
 		try
 		{
 			db = dbHelper.getWritableDatabase();
 		} catch (SQLiteException ex)
 		{
 			db = dbHelper.getReadableDatabase();
 		}
 	}
 
 	/*
 	 * Adding a new notification in DB
 	 */
 	public long insertNotification(NotifyMeItem _notify)
 	{
 		// Create a new row of values to insert
 		ContentValues newTaskValues = new ContentValues();
 
 		// Assign values for each row
 		newTaskValues.put(KEY_NOTIFY_SUBWAYS, Serializer.serializeArray(_notify.getSubways(), context));
 		newTaskValues.put(KEY_NOTIFY_DAY, _notify.getDay());
 		newTaskValues.put(KEY_NOTIFY_HOUR, _notify.getHour());
 		newTaskValues.put(KEY_NOTIFY_MINUTES, _notify.getMinutes());
 		
 		// Insert the row
 		return db.insert(DATABASE_TABLE, null, newTaskValues);
 	}
 
 	@SuppressWarnings("unchecked")
 	public NotifyMeItem getNotification(long _rowIndex) throws SQLException
 	{
 		Cursor cursor = db.query(true, DATABASE_TABLE, new String[] { KEY_ID, KEY_NOTIFY_SUBWAYS, KEY_NOTIFY_DAY,
 				KEY_NOTIFY_HOUR, KEY_NOTIFY_MINUTES }, KEY_ID + "=" + _rowIndex, null, null, null, null, null);
 		
 		if ((cursor.getCount() == 0) || !cursor.moveToFirst())
 			throw new SQLException("No to do item found for row: " + _rowIndex);
 
 		String serialString = cursor.getString(cursor.getColumnIndex(KEY_NOTIFY_SUBWAYS));
 		int day = cursor.getInt(cursor.getColumnIndex(KEY_NOTIFY_DAY));
 		int hour = cursor.getInt(cursor.getColumnIndex(KEY_NOTIFY_HOUR));
 		int minutes = cursor.getInt(cursor.getColumnIndex(KEY_NOTIFY_MINUTES));
 		long db_ID = cursor.getLong(cursor.getColumnIndex(KEY_ID));
 
 		ArrayList<String> subwaysArray = Serializer.deSerializeArray(serialString, context);
 		NotifyMeItem result = new NotifyMeItem(subwaysArray, day, hour, minutes, db_ID);
 		
 		cursor.close();
 		return result;
 	}
 
 	// Remove a notification based on its index
 	public boolean removeNotification(long _rowIndex)
 	{
 		//Log.w(TAG, Long.toString(_rowIndex));
 		return db.delete(DATABASE_TABLE, KEY_ID + "=" + _rowIndex, null) > 0;
 	}
 
 	//update edited notification
 	public boolean updateNotification(NotifyMeItem _notify)
 	{
 		ContentValues newValues = new ContentValues();
 		newValues.put(KEY_NOTIFY_SUBWAYS, Serializer.serializeArray(_notify.getSubways(), context));
 		newValues.put(KEY_NOTIFY_DAY, _notify.getDay());
 		newValues.put(KEY_NOTIFY_HOUR, _notify.getHour());
 		newValues.put(KEY_NOTIFY_MINUTES, _notify.getMinutes());
 		
 		return db.update(DATABASE_TABLE, newValues, KEY_ID + "=" + _notify.getDB_ID(), null) > 0;
 	}
 
	/*
	 * Get all values
	 */
 	public Cursor getAllNotifications()
 	{
 		return db.query(DATABASE_TABLE, new String[] { KEY_ID, KEY_NOTIFY_DAY, KEY_NOTIFY_HOUR, KEY_NOTIFY_MINUTES , KEY_NOTIFY_SUBWAYS },
 				null, null, null, null, null);
 	}
 
 	/*
 	 * Get notification count for a particular day
 	 */
 	public int getDayCount(int _dayInt)
 	{
 		final String SQL_STATEMENT = "SELECT " + KEY_NOTIFY_DAY + " FROM " + DATABASE_TABLE + " WHERE "
 				+ KEY_NOTIFY_DAY + "=" + _dayInt;
 		Cursor cursor = db.rawQuery(SQL_STATEMENT, null);
 
 		int dayCount = cursor.getCount();
 		cursor.close();
 		return dayCount;
 	}
 
 	/*
 	 * Get ArrayList of notification items for a particular day
 	 */
 	public ArrayList<NotifyMeItem> getNotifyItemsByDay(int _dayInt)
 	{
 		final String SQL_STATEMENT = "SELECT " + KEY_ID + " FROM " + DATABASE_TABLE + " WHERE " + KEY_NOTIFY_DAY + "="
 				+ _dayInt;
 		Cursor cursor = db.rawQuery(SQL_STATEMENT, null);
 
 		
 		ArrayList<NotifyMeItem> notifyMeItems = new ArrayList<NotifyMeItem>();
 		if (cursor.moveToFirst())
 		{
 			do{
 				notifyMeItems.add(getNotification(cursor.getLong(cursor.getColumnIndex(KEY_ID))));
 			}while(cursor.moveToNext());
 		}
 		
 		cursor.close();
 		return notifyMeItems;
 	}
 
 	// ///////////////////////////////////////////////////
 	// SQLite Helper Class
 	// ///////////////////////////////////////////////////
 
 	private static class notifyDBOpenHelper extends SQLiteOpenHelper
 	{
 		private static final String TAG = "notifyDBOpenHelper";
 
 		public notifyDBOpenHelper(Context context, String name, CursorFactory factory, int version)
 		{
 			super(context, name, factory, version);
 			// Log.w(TAG, "DB Contructor");
 		}
 
 		// SQL Statement to create a new database
 		private static final String DATABASE_CREATE = "create table " + DATABASE_TABLE + " (" + KEY_ID
 				+ " integer primary key autoincrement, " + KEY_NOTIFY_DAY + " int, " + KEY_NOTIFY_SUBWAYS
 				+ " text not null, " + KEY_NOTIFY_HOUR + " int, " + KEY_NOTIFY_MINUTES + " int);";
 
 		@Override
 		public void onCreate(SQLiteDatabase _db)
 		{
 			// Log.w(TAG, "Create DB");
 			_db.execSQL(DATABASE_CREATE);
 		}
 
 		@Override
 		public void onUpgrade(SQLiteDatabase _db, int _oldVersion, int _newVersion)
 		{
 			Log.w("TaskDBAdapter", "Upgrading from version " + _oldVersion + " to " + _newVersion
 					+ ", which will destroy all old data");
 
 			// Drop the old table.
 			_db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
 			// Create a new one
 			onCreate(_db);
 		}
 	}
 }
