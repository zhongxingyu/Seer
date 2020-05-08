 package net.geekzy.mobile.brewclock.data;
 
 import android.app.Activity;
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.DatabaseUtils;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.provider.BaseColumns;
 
 public class TeaData extends SQLiteOpenHelper {
 	private static final String DATABASE_NAME = "teas.db";
 	private static final int DATABASE_VERSION = 1;
 	public static final String TABLE_NAME = "teas";
 	public static final String _ID = BaseColumns._ID;
 	public static final String NAME = "name";
 	public static final String BREW_TIME = "brew_time";
 
 	public TeaData(Context context) {
 		super(context, DATABASE_NAME, null, DATABASE_VERSION);
 	}
 
 	public void insert(String name, int brewTime) {
 		SQLiteDatabase db = getWritableDatabase();
 
 		ContentValues values = new ContentValues();
 		values.put(NAME, name);
 		values.put(BREW_TIME, brewTime);
 
 		db.insertOrThrow(TABLE_NAME, null, values);
 	}
 
	//@SuppressWarnings("deprecation")
 	public Cursor all(Activity activity) {
		String[] from = {_ID, NAME, BREW_TIME};
 		String order = _ID;
 
 		SQLiteDatabase db = getReadableDatabase();
		Cursor cursor = db.query(TABLE_NAME, from, null, null, null, null, order);
		//activity.startManagingCursor(cursor);
 
 		return cursor;
 	}
 
 	public long count() {
 		SQLiteDatabase db = getReadableDatabase();
 		return DatabaseUtils.queryNumEntries(db, TABLE_NAME);
 	}
 
 	public boolean delete(int id) {
 		SQLiteDatabase db = getWritableDatabase();
		db.delete(TABLE_NAME, _ID+"=?", new String[]{String.valueOf(id)});
 		return true;
 	}
 
 	@Override
 	public void onCreate(SQLiteDatabase db) {
 		StringBuilder buff = new StringBuilder();
 		buff.append("CREATE TABLE ").append(TABLE_NAME).append("(");
 		buff.append(_ID).append(" INTEGER PRIMARY KEY AUTOINCREMENT, ");
 		buff.append(NAME).append(" TEXT NOT NULL, ");
 		buff.append(BREW_TIME).append(" INTEGER");
 		buff.append(");");
 
 		String sql = buff.toString();
 		db.execSQL(sql);
 	}
 
 	@Override
 	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 		db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
 		onCreate(db);
 	}
 
 }
