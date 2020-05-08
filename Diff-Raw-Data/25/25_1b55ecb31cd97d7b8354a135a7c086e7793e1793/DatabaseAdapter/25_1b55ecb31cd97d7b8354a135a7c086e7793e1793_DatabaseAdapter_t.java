 package com.asa.sms2honeycomb;
 
 import java.util.ArrayList;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteDatabase.CursorFactory;
 import android.database.sqlite.SQLiteException;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.util.Log;
 
 public class DatabaseAdapter {
 	// TAG for debugging
 	private final String TAG = "DatabaseHandler";
 
 	// Basic Database information
 	private static final String DATABASE_NAME = "texttotab.db";
 	private static final String MESSAGE_TABLE = "messageTable";
 	private static final String THREAD_TABLE = "threadTable";
 	private static final int DATABASE_VERSION = 1;
 
 	// Used in all of the tables
 	public static final String KEY_ID = "_id";
 
 	// The index (key) column name for use in where clauses.
 	// Column names for SMS table:
 	public static final String KEY_ADDRESS = "_address";
 	public static final String KEY_TIME = "_time";
 	public static final String KEY_BODY = "_body";
 	public static final String KEY_READ_S = "_reads";
 	public static final String KEY_SMSID = "_smsid";
 	public static final String KEY_SUBJECT = "_subject";
 	public static final String KEY_THREADID_S = "_threadids";
 	public static final String KEY_TYPE = "_type";
 	public static final String KEY_USERNAME_S = "_usernames";
 
 	// The index (key) column name for use in where clauses.
 	// Column names for THREAD table:
 	public static final String KEY_HAS_ATTACHMENT = "_hasattachment";
 	public static final String KEY_MESSAGE_COUNT = "_messagecount";
 	public static final String KEY_READ_T = "_readt";
 	public static final String KEY_THREADID_T = "_threadidt";
 	public static final String KEY_USERNAME_T = "_usernamet";
 
 	// Used to identify the columns in other classes.
 	// SMS table
 	public static final int ID_COLUMN_S = 0;
 	public static final int TIME_COLUMN = 1;
 	public static final int ADDRESS_COLUMN = 2;
 	public static final int BODY_COLUMN = 3;
 	public static final int READ_S_COLUMN = 4;
 	public static final int SMSID_COLUMN = 5;
 	public static final int SUBJECT_COLUMN = 6;
 	public static final int THREADID_S_COLUMN = 7;
 	public static final int TYPE_COLUMN = 8;
 	public static final int USERNAME_S_COLUMN = 9;
 	// THREAD table
 	public static final int ID_COLUMN_T = 0;
 	public static final int HAS_ATTACHMENT = 1;
 	public static final int MESSAGE_COUNT = 2;
 	public static final int READ_T = 3;
 	public static final int THREADID_T = 4;
 	public static final int USERNAME_T = 5;
 
 	// SQL Statement to create a new database.
 	private static final String DATABASE_CREATE = "create table "
 			+ MESSAGE_TABLE + " (" + KEY_ID
 			+ " integer primary key autoincrement, " + KEY_TIME + " date, "
 			+ KEY_ADDRESS + " string, " + KEY_BODY + " String, " + KEY_READ_S
 			+ " int, " + KEY_SMSID + " int, " + KEY_SUBJECT + " String, "
 			+ KEY_THREADID_S + " int, " + KEY_TYPE
 			+ " int, "
 			+ KEY_USERNAME_S
 			+ " String);"
 			// creating a new table
 			+ "create table " + THREAD_TABLE + " (" + KEY_ID
 			+ " integer primary key autoincrement, " + HAS_ATTACHMENT
 			+ " int, " + MESSAGE_COUNT + " int, " + READ_T + " int, "
 			+ THREADID_T + " int, " + KEY_USERNAME_T + " String);";
 
 	// Variables to hold the database instance
 	private SQLiteDatabase db;
 	// Context of the appliction using the database
 	private final Context context;
 	// Database open/upgrade helper
 	private myDbHelper dbHelper;
 
 	public DatabaseAdapter(Context _context) {
 		Log.d(TAG, "DatabaseHandler constructor is working");
 		context = _context;
 		dbHelper = new myDbHelper(context, DATABASE_NAME, null,
 				DATABASE_VERSION);
 	}
 
 	public void open() throws SQLiteException {
 		try {
 			db = dbHelper.getWritableDatabase();
 		} catch (SQLiteException ex) {
 			db = dbHelper.getReadableDatabase();
 		}
 	}
 
 	public void close() {
 		Log.d(TAG, "Closing: " + DATABASE_NAME);
 		db.close();
 	}
 
 	// Insert a new MessageItem into Database
 	public long insertMessageItem(MessageItem item) {
 		ContentValues newMessageValues = new ContentValues();
		newMessageValues.put(KEY_ADDRESS, item.getMessageAddress());
		newMessageValues.put(KEY_TIME, item.getMessageTime());
		newMessageValues.put(KEY_BODY, item.getMessageBody());
		newMessageValues.put(KEY_READ_S, item.getMessageRead());
		newMessageValues.put(KEY_SMSID, item.getMessageSmsId());
		newMessageValues.put(KEY_SUBJECT, item.getMessageSubject());
		newMessageValues.put(KEY_THREADID_S, item.getMessageThreadId());
		newMessageValues.put(KEY_TYPE, item.getMessageType());
		newMessageValues.put(KEY_USERNAME_S, item.getMessageUsername());
 		// Inserts the new row into the database
 		Log.d(TAG, "Values: " + " have been put in to: " + DATABASE_NAME);
 		return db.insert(MESSAGE_TABLE, null, newMessageValues);
 	}
 
 	public long insertThreadItem(ThreadItem item) {
 		ContentValues newThreadValues = new ContentValues();
 		newThreadValues.put(KEY_HAS_ATTACHMENT, item.getHasAttachment());
 		newThreadValues.put(KEY_MESSAGE_COUNT, item.getMessageCount());
 		newThreadValues.put(KEY_READ_T, item.getRead());
 		newThreadValues.put(KEY_USERNAME_T, item.getUsername());
 		Log.d(TAG, "Values: " + " have been put in to: ");
 		return db.insert(THREAD_TABLE, null, newThreadValues);
 	}
 
 	/*
 	 * Returns the cursor for the given table MESSAGE_TABLE or THREAD_TABLE if
 	 * not those returns null
 	 */
 	public Cursor getAllEntries(String table) {
 		Log.d(TAG, "Getting all entries from: " + table);
 		if (table == MESSAGE_TABLE) {
 			return db.query(table, new String[] { KEY_ID, KEY_ADDRESS,
 					KEY_TIME, KEY_BODY, KEY_READ_S, KEY_SMSID, KEY_SUBJECT,
 					KEY_THREADID_S, KEY_TYPE, KEY_USERNAME_S }, null, null,
 					null, null, null);
 		}
 		if (table == THREAD_TABLE) {
 			return db.query(table, new String[] { KEY_ID, KEY_HAS_ATTACHMENT,
 					KEY_BODY, KEY_MESSAGE_COUNT, KEY_READ_T, KEY_THREADID_T,
 					KEY_USERNAME_T }, null, null, null, null, null);
 		}
 		Log.e(TAG, table + " is not vaild");
 		return null;
 	}
 
 	/*
 	 * Removes entry in a table using the row index tables: THREAD_TABLE and
 	 * MESSAGE_TABLE
 	 */
 	public boolean removeEntry(long _rowIndex, String table) {
 		Log.d(TAG, "Removed entry: " + _rowIndex);
 		return db.delete(table, KEY_ID + "=" + _rowIndex, null) > 0;
 	}
 
 	// TODO delete or make work
 	public boolean updateEntry(long _rowIndex, MessageItem _messageItem,
 			ThreadItem _threadItem) {
 		// TODO: Create a new ContentValues based on the new object
 		// and use it to update a row e database.
 		return true;
 	}
 
 	// TODO update
 	/**
 	 * Querys the Database for the number given getting both the TO and the
 	 * FROM.
 	 * 
 	 * @param String
 	 *            number
 	 * 
 	 * @return ArrayList<String> list
 	 */
 	public ArrayList<String> getMessageArrayList(String number) {
 		ArrayList<String> list = new ArrayList<String>();
 		// Only get values from the to and from number given and sort by time
 		// oldest at
 		// the end of the list
 		Cursor cursor = db.query(true, MESSAGE_TABLE, new String[] { KEY_ID,
 				KEY_ADDRESS, KEY_TIME, KEY_BODY, KEY_READ_S, KEY_SMSID,
 				KEY_SUBJECT, KEY_THREADID_S, KEY_TYPE, KEY_USERNAME_S },
 				KEY_ADDRESS + "=" + number, null, null, null, KEY_TIME, null);
 		if ((cursor.getCount() == 0) || !cursor.moveToFirst()) {
 			Log.e(TAG, "No messages for number: " + number);
 			list.add("No messages for: " + number + " Start a conversation.");
 		}
 		if (cursor.moveToFirst()) {
 			do {
 				String time = cursor.getString(TIME_COLUMN);
 				String address = cursor.getString(ADDRESS_COLUMN);
 				String body = cursor.getString(BODY_COLUMN);
 				// add the message parts together
 				String message = "Received: " + time + "\n" + "Address: "
 						+ address + "\n" + "Message : " + body + "\n";
 				list.add(message);
 			} while (cursor.moveToNext());
 			{
 				cursor.close();
 			}
 		}
 		return list;
 	}
 
 	// TODO update
 	public ArrayList<String> getConversationList() {
 		ArrayList<String> list = new ArrayList<String>();
 		// TODO get the cursor to query the db and get the number of entries for
 		// both the to and from that are from the same person MAKE IT WORK
 		// TODO bug counts get number's conversation then adds that name entries
 		// public Cursor query (boolean distinct, String table, String[]
 		// columns, String selection, String[] selectionArgs, String groupBy,
 		// String having, String orderBy, String limit)
 		Cursor cursor = db.query(true, THREAD_TABLE, new String[] { KEY_ID,
 				KEY_HAS_ATTACHMENT, KEY_BODY, KEY_MESSAGE_COUNT, KEY_READ_T,
 				KEY_THREADID_T, KEY_USERNAME_T }, null, null, null, null, null,
 				null);
 
 		if ((cursor.getCount() == 0) || !cursor.moveToFirst()) {
 			Log.d(TAG, "No conversations.");
 			list.add("Start a new conversation");
 			list.add("The conversation list is not working yet bitch. I do not know how the threads are going to work.");
 		}
 
 		if (cursor.moveToFirst()) {
 			// This will be used to start the new conversations within the
 			// ConversationFragment
 			do {
 				String threadId = cursor.getString(THREADID_T);
 				String messageCount = cursor.getString(MESSAGE_COUNT);
 				Log.d(TAG, "Conversations from threadId: " + threadId
 						+ " with the count of " + messageCount
 						+ " have been found");
 				// TODO relate the thread id with the messages and get the
 				// address and relate that to the contacts
 				String conversationItem = " " + threadId + " (" + messageCount
 						+ ")";
 				list.add(conversationItem);
 			} while (cursor.moveToNext());
 			{
 				cursor.close();
 			}
 		}
 
 		return list;
 	}
 
 	// TODO make class work
 	private static class myDbHelper extends SQLiteOpenHelper {
 		private final String TAG = "myDbHelper";
 
 		public myDbHelper(Context context, String name, CursorFactory factory,
 				int version) {
 			super(context, name, factory, version);
 			// TODO Auto-generated constructor stub
 		}
 
 		// Called when no database exists in disk and the helper class needs
 		// to create a new one.
 		@Override
 		public void onCreate(SQLiteDatabase _db) {
 			Log.d(TAG, "Created: " + DATABASE_CREATE);
 			_db.execSQL(DATABASE_CREATE);
 		}
 
 		/*
 		 * Called when there is a database wersion mismatch meaning that the
 		 * version of the database on disk needs to be upgraded to the current
 		 * version.
 		 */
 		@Override
 		public void onUpgrade(SQLiteDatabase _db, int _oldVersion,
 				int _newVersion) {
 			// Log the version upgrade.
 			Log.w("TaskDBAdapter", "Upgrade from version " + _oldVersion
 					+ " to " + _newVersion
 					+ ", which will destroy all old data");
 
 			/*
 			 * Upgrade the existing database to conform to the new version.
 			 * Multiple previous version can be handled by comparing _oldVersion
 			 * and _newVersion values.
 			 */
 
 			// The simplest case is to drop the old table and create a new one.
 			_db.execSQL("DROP TABLE IF EXISTS " + MESSAGE_TABLE);
 			_db.execSQL("DROP TABLE IF EXISTS " + THREAD_TABLE);
 			// Create a new one.
 			onCreate(_db);
 		}
 	}
 }
