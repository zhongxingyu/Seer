 package com.aragaer.reminder;
 
 import java.io.File;
 import java.sql.Date;
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.ContentProvider;
 import android.content.ContentUris;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.content.UriMatcher;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteDatabase.CursorFactory;
 import android.database.sqlite.SQLiteException;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.net.Uri;
 import android.os.Environment;
 import android.text.TextUtils;
 import android.util.Log;
 import android.widget.Toast;
 
 public class ReminderProvider extends ContentProvider {
	private static final int DATABASE_VERSION = 1;
 	private SQLiteDatabase db = null;
 	private final ArrayList<Long> ordered_ids = new ArrayList<Long>();
 
 	public static final Uri content_uri = Uri
 			.parse("content://com.aragaer.reminder.provider/reminder");
 	public static final Uri reorder_uri = Uri
 			.parse("content://com.aragaer.reminder.provider/reorder");
 
 	public static final String REORDER_FROM = "from";
 	public static final String REORDER_TO = "to";
 
 	private static final UriMatcher uri_matcher = new UriMatcher(0);
 	private static final int REMINDER_CODE = 1;
 	private static final int REMINDER_WITH_ID = 2;
 	private static final int REORDER_CODE = 3;
 
 	private static final String TAG = ReminderProvider.class.getSimpleName();
 	private static final String PREF_ORDER = "com.aragaer.reminder.order";
 
 	static {
 		uri_matcher.addURI("com.aragaer.reminder.provider", "reorder",	REORDER_CODE);
 		uri_matcher.addURI("com.aragaer.reminder.provider", "reminder",	REMINDER_CODE);
 		uri_matcher.addURI("com.aragaer.reminder.provider", "reminder/#", REMINDER_WITH_ID);
 	}
 
 	public int delete(Uri uri, String arg1, String[] arg2) {
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
 		if (result > 0) {
 			Toast.makeText(getContext(), R.string.toast_deleted, Toast.LENGTH_LONG).show();
 			getContext().getContentResolver().notifyChange(content_uri, null);
 		}
 		return result;
 	}
 
 	public String getType(Uri arg0) {
 		return null;
 	}
 
 	public Uri insert(Uri uri, ContentValues arg1) {
 		switch (uri_matcher.match(uri)) {
 		case REORDER_CODE:
 			final int from = arg1.getAsInteger(REORDER_FROM);
 			final int to = arg1.getAsInteger(REORDER_TO);
 			Long moved = ordered_ids.remove(from);
 			ordered_ids.add(to, moved);
 			save_reorder();
 			getContext().getContentResolver().notifyChange(content_uri, null);
 			break;
 		case REMINDER_CODE:
 			long id = db.insert("memo", null, arg1);
 			if (id != -1) {
 				Toast.makeText(getContext(), R.string.toast_created, Toast.LENGTH_LONG).show();
 				getContext().getContentResolver().notifyChange(content_uri, null);
 			}
 			return ContentUris.withAppendedId(content_uri, id);
 		default:
 			Log.e(TAG, "Unknown URI requested: " + uri);
 			break;
 		}
 		return null;
 	}
 
 	final void save_reorder() {
 		getContext().getSharedPreferences("DB", Context.MODE_PRIVATE)
 				.edit()
 				.putString(PREF_ORDER, TextUtils.join(",", ordered_ids))
 				.commit();
 	}
 
 	public boolean onCreate() {
 		db = new ReminderSQLHelper(getContext(), "MEMO", null, DATABASE_VERSION).getWritableDatabase();
 		if (db == null || db.isReadOnly())
 			return false;
 
 		Log.d(TAG, "Created. Checking if we need to move old data");
 
 		SharedPreferences prefs = getContext().getSharedPreferences("DB", Context.MODE_PRIVATE);
 		int current_version = prefs.getInt("DATABASE_VERSION", 0);
 		if (current_version > 0 && moveOldData(db))
 			prefs.edit().putInt("DATABASE_VERSION", 0).commit();
 
 		for (final String id : TextUtils.split(prefs.getString(PREF_ORDER, ""), ","))
 			ordered_ids.add(Long.valueOf(id));
 
 		return true;
 	}
 
 	boolean moveOldData(SQLiteDatabase db) {
 		Log.d(TAG, "Converting old database");
 		File sdcard = Environment.getExternalStorageDirectory();
 		File dir = new File(sdcard, "Android");
 		dir = new File(new File(dir, "data"), ReminderProvider.class
 				.getPackage().getName());
 		if (!dir.exists())
 			return true;
 
 		File db_file = new File(dir, "memo.db");
 
 		if (db_file.exists()) {
 			try {
 				db.execSQL("attach ? as sd", new String[] {db_file.getAbsolutePath()} );
 			} catch (SQLiteException e) {
 				Log.e(TAG, "Failed to attach old DB: "+e);
 				return false;
 			}
 
 			try {
 				db.beginTransaction();
 				db.execSQL("insert into memo select * from sd.memo");
 				db.delete("sd.memo", null, null);
 				db.setTransactionSuccessful();
 			} catch (SQLiteException e) {
 				Log.e(TAG, "Failed to move old DB: "+e);
 				if (db.inTransaction())
 					db.endTransaction();
 				return false;
 			} finally {
 				db.endTransaction();
 			}
 
 			Log.d(TAG, "Data moved");
 
 			try {
 				db.execSQL("detach sd");
 			} catch (SQLiteException e) {
 				Log.w(TAG, "Failed to detach database");
 				return false;
 			}
 
 			db_file.delete();
 		}
 
 		dir.delete();
 
 		return true;
 	}
 
 	public Cursor query(Uri uri, String[] cols, String selection,
 			String[] sel_args, String group_by) {
 		switch (uri_matcher.match(uri)) {
 		case REMINDER_CODE:
 			Cursor result = db.query("memo", cols, selection, sel_args, null, null, group_by);
 			return selection == null && group_by == null
 					&& (cols == null || cols[0].equals("_id"))
 				? reorder(result)
 				: result;
 		case REMINDER_WITH_ID:
 			return db.query("memo", cols, "_id=?", uri2selection(uri), null, null, group_by);
 		default:
 			Log.e(TAG, "Unknown URI requested: " + uri);
 			break;
 		}
 		return null;
 	}
 
 	// Use this only if no selection condition, no groupby
 	// and first column is _id
 	private synchronized Cursor reorder(final Cursor c) {
 		if (c == null) {
 			ordered_ids.clear();
 			save_reorder();
 			return null;
 		}
 		final ArrayList<Long> ids = new ArrayList<Long>(c.getCount());
 		if (c.moveToFirst())
 			do {
 				ids.add(c.getLong(0));
 			} while (c.moveToNext());
 		ordered_ids.retainAll(ids); // remove all lost entries
 		ids.removeAll(ordered_ids); // check all known entries
 		ordered_ids.addAll(ids); // add all new entries to the end
 		save_reorder();
 		return new ReorderedCursor(c).setOrder(ordered_ids);
 	}
 
 	String[] uri2selection(Uri uri) {
 		return new String[] { Long.toString(ContentUris.parseId(uri)) };
 	}
 
 	public int update(Uri uri, ContentValues arg1, String arg2, String[] arg3) {
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
 		if (result > 0) {
 			Toast.makeText(getContext(), R.string.toast_saved, Toast.LENGTH_LONG).show();
 			getContext().getContentResolver().notifyChange(uri, null);
 		}
 		return result;
 	}
 
 	public static ReminderItem getItem(Cursor c, ReminderItem reuse) {
 		if (reuse == null)
 			reuse = new ReminderItem(c.getLong(0), c.getBlob(1), c.getString(2),
 				new Date(c.getLong(3)), c.getInt(4));
 		else
 			reuse.setTo(c.getLong(0), c.getBlob(1), c.getString(2),
 				new Date(c.getLong(3)), c.getInt(4));
 		return reuse;
 	}
 
 	public static ReminderItem getItem(Cursor c) {
 		return getItem(c, null);
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
 
 	class ReminderSQLHelper extends SQLiteOpenHelper {
 		public ReminderSQLHelper(Context context, String name,
 				CursorFactory factory, int version) {
 			super(context, name, factory, version);
 		}
 
 		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) { }
 		
 		public void onCreate(SQLiteDatabase db) {
 			Log.d(TAG, "creating DB");
 			try {
 				db.execSQL("CREATE TABLE memo (_id integer primary key autoincrement, glyph blob, comment text, date integer, color integer)");
 			} catch (SQLException e) {
 				Log.e(TAG, e.toString());
 			}
 		}
 	};
 
 	public static void reorder(final Context c, final int from, final int to) {
 		final ContentValues row = new ContentValues(2);
 		row.put(REORDER_FROM, from);
 		row.put(REORDER_TO, to);
 		c.getContentResolver().insert(reorder_uri, row);
 	}
 }
