 package com.appspot.manup.signature;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.util.Log;
 import android.widget.Toast;
 
 public class DataHelper {
 
 	private static final String DATABASE_NAME = "manup_signatures.db";
 	private static final int DATABASE_VERSION = 1;
 	private static final String TABLE_NAME = "tbl_sigs";
 	static final String ID = "student_id", FILEPATH = "file_path", UPLOADED = "uploaded";
 
 	private Context context;
 	private SQLiteDatabase db;
 
 	private OpenHelper openHelper;
 
 	public DataHelper(Context context) {
 		this.context = context;
 		openHelper = new OpenHelper(this.context);
 	}
 
 	public SQLiteDatabase getDb() {
 		return this.db;
 	}
 
 	public void insert(long student_id, String filepath, boolean uploaded) {
 		this.db = openHelper.getWritableDatabase();
 		ContentValues cv = makeCV(student_id, filepath, uploaded);
 		this.db.beginTransaction();
 		try{
 			this.db.insert(TABLE_NAME, null, cv);
 			this.db.setTransactionSuccessful();
 			Toast.makeText(this.context, "Write successful", Toast.LENGTH_SHORT).show();
 		}finally{ this.db.endTransaction();}
 		this.db.close();
 	}
 	
 	private ContentValues makeCV(long student_id, String filepath, boolean uploaded){
 		ContentValues cv = new ContentValues();
 		cv.put(ID, student_id);
 		cv.put(FILEPATH, filepath);
 		cv.put(UPLOADED, (uploaded) ? 1 : 0);
 		return cv;
 	}
 	
 	public void update(int id, long student_id, String filepath, boolean uploaded) {
 		this.db = openHelper.getWritableDatabase();
 		ContentValues cv = makeCV(student_id, filepath, uploaded);
 		this.db.beginTransaction();
 		try{
 			this.db.update(TABLE_NAME, cv, "id=?", new String[]{""+id});
 			this.db.setTransactionSuccessful();
 			Toast.makeText(this.context, "Update successful", Toast.LENGTH_SHORT).show();
 		}finally{ this.db.endTransaction();}
 		this.db.close();
 	}
 
 	public void deleteAll() {
 		this.db = openHelper.getWritableDatabase();
 		this.db.delete(TABLE_NAME, null, null);
 		this.db.close();
 	}
 
 	public Map<String, Object> selectAll() {
 		this.db = openHelper.getReadableDatabase();
 		Map<String, Object> map = new HashMap<String, Object>();
 		Cursor cursor = this.db.query(TABLE_NAME, new String[] { ID, FILEPATH, UPLOADED }, null,
 				null, null, null, null);
 		if (cursor.moveToFirst()) {
 			do {
 				map.put(ID, cursor.getLong(0));
 				map.put(FILEPATH, cursor.getString(1));
 				map.put(UPLOADED, cursor.getInt(2));
 			} while (cursor.moveToNext());
 		}
 		if (cursor != null && !cursor.isClosed()) {
 			cursor.close();
 		}
 		this.db.close();
 		return map;
 	}
 
 	public Map<String, Object> selectAllNotUploaded() {
 		this.db = openHelper.getReadableDatabase();
 		Map<String, Object> map = new HashMap<String, Object>();
 		Cursor cursor = this.db.query(TABLE_NAME, new String[] { ID, FILEPATH, UPLOADED }, UPLOADED + "=?",
 				new String[] {"" + 0}, null, null, null);
 		if (cursor.moveToFirst()) {
 			do {
 				map.put(ID, cursor.getLong(0));
 				map.put(FILEPATH, cursor.getString(1));
 				map.put(UPLOADED, cursor.getInt(2));
 			} while (cursor.moveToNext());
 		}
 		if (cursor != null && !cursor.isClosed()) {
 			cursor.close();
 		}
 		this.db.close();
 		return map;
 	}
 	private static class OpenHelper extends SQLiteOpenHelper {
 
 		OpenHelper(Context context) {
 			super(context, DATABASE_NAME, null, DATABASE_VERSION);
 		}
 		
		private static final String createTbl = "CREATE TABLE" +
 				TABLE_NAME + " (id INTEGER PRIMARY KEY AUTOINCREMENT, " 
 				+ ID + " NUMERIC, " + FILEPATH + " TEXT, " + UPLOADED
 				+ " INTEGER)";
 
 		@Override
 		public void onCreate(SQLiteDatabase db) {
 			db.execSQL(createTbl);
 		}
 
 		@Override
 		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 			Log.w("DB Upgrade",
 					"Upgrading database, this will drop tables and recreate.");
 			db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
 			onCreate(db);
 		}
 	}
 }
