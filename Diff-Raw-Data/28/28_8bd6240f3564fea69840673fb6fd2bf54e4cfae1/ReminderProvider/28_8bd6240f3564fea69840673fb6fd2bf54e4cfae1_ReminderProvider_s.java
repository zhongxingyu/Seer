 package com.aragaer.reminder;
 
 import java.io.File;
 import java.sql.Date;
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.ContentProvider;
 import android.content.ContentUris;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.UriMatcher;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteException;
 import android.net.Uri;
 import android.os.Environment;
 import android.util.Log;
 
 public class ReminderProvider extends ContentProvider {
 	private static final int DATABASE_VERSION = 2;
 
 	public static final String UPDATE_ACTION = "com.aragaer.reminder.ReminderUpdate";
 
 	static final Intent update_broadcast = new Intent(UPDATE_ACTION);
 
 	public static final Uri content_uri = Uri
 			.parse("content://com.aragaer.reminder.provider/reminder");
 
 	private SQLiteDatabase db = null;
 
 	private static final UriMatcher uri_matcher = new UriMatcher(0);
 	private static final int REMINDER_CODE = 1;
 	private static final int REMINDER_WITH_ID = 2;
 
 	private static final String TAG = ReminderProvider.class.getSimpleName();
 
 	static {
 		uri_matcher.addURI("com.aragaer.reminder.provider", "reminder",	REMINDER_CODE);
 		uri_matcher.addURI("com.aragaer.reminder.provider", "reminder/#", REMINDER_WITH_ID);
 	}
 
 	void notifyChange() {
 		getContext().sendBroadcast(update_broadcast);
 	}
 
 	public int delete(Uri uri, String arg1, String[] arg2) {
 		if (!openDB())
 			return 0;
 		int result = 0;
 		switch (uri_matcher.match(uri)) {
 		case REMINDER_CODE:
 			result = db.delete("memo", arg1 == null ? "1" : arg1, arg2);
 			break;
 		case REMINDER_WITH_ID:
 			result = db.delete("memo", "_id=?", uri2selection(uri));
 			break;
 		default:
 			Log.e(TAG, "Unknown URI requested: " + uri);
 			break;
 		}
		if (result > 0)
 			notifyChange();
 		return result;
 	}
 
 	public String getType(Uri arg0) {
 		return null;
 	}
 
 	public Uri insert(Uri uri, ContentValues arg1) {
 		if (!openDB())
 			return null;
 		switch (uri_matcher.match(uri)) {
 		case REMINDER_CODE:
 			long id = db.insert("memo", null, arg1);
			if (id != -1)
 				notifyChange();
 			return ContentUris.withAppendedId(content_uri, id);
 		default:
 			Log.e(TAG, "Unknown URI requested: " + uri);
 			break;
 		}
 		return null;
 	}
 
 	public boolean onCreate() {
 		return true;
 	}
 
 	static boolean createDB(SQLiteDatabase db) {
 		Log.d(TAG, "creating DB");
 		try {
 			db.execSQL("CREATE TABLE memo (_id integer primary key autoincrement, glyph blob, comment text, date integer, color integer)");
 			return true;
 		} catch (SQLException e) {
 			Log.e(TAG, e.toString());
 			return false;
 		}
 	}
 
 	static boolean upgradeDB(SQLiteDatabase db, int old_version) {
 		switch (old_version) {
 		case 1:
 			db.execSQL("ALTER TABLE memo ADD color integer not null default 0");
 			return true;
 		default:
 			return false;
 		}
 	}
 
 	boolean openDB() {
 		if (db != null)
 			return true;
 		SharedPreferences prefs = getContext().getSharedPreferences("DB", Context.MODE_PRIVATE);
 		int current_version = prefs.getInt("DATABASE_VERSION", 0);
 
 		File sdcard = Environment.getExternalStorageDirectory();
 		File dir = new File(sdcard, "Android");
 		dir = new File(new File(dir, "data"), ReminderProvider.class
 				.getPackage().getName());
 		if (!dir.exists() && !dir.mkdirs())
 			return false;
 
 		File db_file = new File(dir, "memo.db");
 		if (db_file.exists() && current_version == 0)
 			db_file.delete();
 		else if (!db_file.exists())
 			current_version = 0;
 
 		try {
 			db = SQLiteDatabase.openOrCreateDatabase(db_file, null);
 		} catch (SQLiteException e) {
 			Log.e(TAG, e.toString());
 			return false;
 		}
 
 		boolean success = false;
 		if (current_version == DATABASE_VERSION)
 			return true;
 		if (current_version == 0)
 			success = createDB(db);
 		else
 			success = upgradeDB(db, current_version);
 
 		if (success)
 			prefs.edit().putInt("DATABASE_VERSION", DATABASE_VERSION).commit();
 		else {
 			db.close();
 			db = null;
 		}
 		return db != null;
 	}
 
 	public Cursor query(Uri uri, String[] arg1, String arg2, String[] arg3,
 			String arg4) {
 		if (!openDB())
 			return null;
 //		Log.d(TAG, "DB = " + db.toString());
 		switch (uri_matcher.match(uri)) {
 		case REMINDER_CODE:
 			return db.query("memo", arg1, arg2, arg3, null, null, arg4);
 		case REMINDER_WITH_ID:
 			return db.query("memo", arg1, "_id=?", uri2selection(uri), null, null, arg4);
 		default:
 			Log.e(TAG, "Unknown URI requested: " + uri);
 			break;
 		}
 		return null;
 	}
 
 	String[] uri2selection(Uri uri) {
 		return new String[] { Long.toString(ContentUris.parseId(uri)) };
 	}
 
 	public int update(Uri uri, ContentValues arg1, String arg2, String[] arg3) {
 		if (!openDB())
 			return 0;
 		int result = 0;
 		switch (uri_matcher.match(uri)) {
 		case REMINDER_CODE:
 			result = db.update("memo", arg1, arg2, arg3);
 			break;
 		case REMINDER_WITH_ID:
 			result = db.update("memo", arg1, "_id=?", uri2selection(uri));
 			break;
 		default:
 			Log.e(TAG, "Unknown URI requested: " + uri);
 			break;
 		}
		if (result > 0)
 			notifyChange();
 		return result;
 	}
 
 	public static ReminderItem getItem(Cursor c) {
 		return new ReminderItem(c.getLong(0), c.getBlob(1), c.getString(2),
 				new Date(c.getLong(3)), c.getInt(4));
 	}
 
 	public static List<ReminderItem> getAll(Cursor c) {
 		ArrayList<ReminderItem> result = new ArrayList<ReminderItem>();
 		while (c.moveToNext())
 			result.add(getItem(c));
 		return result;
 	}
 
 	public static List<ReminderItem> getAllSublist(Cursor c, int n) {
 		ArrayList<ReminderItem> result = new ArrayList<ReminderItem>();
 		while (c.moveToNext() && n-- > 0)
 			result.add(getItem(c));
 		return result;
 	}
 }
