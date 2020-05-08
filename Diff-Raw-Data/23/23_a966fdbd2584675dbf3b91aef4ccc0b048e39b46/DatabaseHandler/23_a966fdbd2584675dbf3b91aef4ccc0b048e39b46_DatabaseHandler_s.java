 package com.plingnote;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.util.Log;
 
 public class DatabaseHandler {
 	/* Name of database file */
 	private static final String DB_NAME = "notedb";
 
 	/* Table */
 	private static final String TABLE_NOTE = "Note";
 
 	/* Columns */
 	private static final String KEY_ROWID = "_id";
 	private static final String KEY_TEXT = "Text";
 	private static final String KEY_TITLE = "Title";
 	private static final String KEY_LONGITUDE = "Longitude";
 	private static final String KEY_LATITUDE = "Latitude";
 
 	/* SQL statement to create Note table */
 	private static final String CREATE_TABLE_NOTE =
 			"create table " + TABLE_NOTE + " (" + KEY_ROWID + " integer primary key autoincrement, "
					+ KEY_TITLE + " String not null, " + KEY_TEXT + " String not null, " 
 					+ KEY_LONGITUDE +" Double not null, "+ KEY_LATITUDE +" Double not null);";
 
 	private Context context;
 	private DBHelper dbHelp;
 	private SQLiteDatabase db;
 	private static DatabaseHandler instance = null;
	
 	/**
 	 * 
 	 * @param con the context
 	 * @return the singleton instance
 	 */
 	public static DatabaseHandler getInstance(Context con){
 		if(instance == null)
 			instance = new DatabaseHandler(con);
 		return instance;
 	}
	
 	private DatabaseHandler(Context con){
 		this.context = con;
 		this.dbHelp = new DBHelper(this.context);
 	}
 
 	private static class DBHelper extends SQLiteOpenHelper{
 		DBHelper(Context con){
 			super(con, DB_NAME, null, 1);
 		}
 
 		@Override
 		public void onCreate(SQLiteDatabase db) {
 			try {
 				db.execSQL(CREATE_TABLE_NOTE);;
 			} catch (SQLException e) {
 				Log.e("SQLException", "while creating database");
 			}
 		}
 
 		@Override
 		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 			//TODO left empty for now
 		}
 	}
 
 	/**
 	 * 
 	 * @param title title of the note to insert
 	 * @param text text of the note to insert
 	 * @return row id or -1 if an error occurred
 	 */
 	public long insertNote(String title, String text, Location l){
 		this.open();
 		ContentValues cv = new ContentValues();
 		cv.put(KEY_TITLE, title);
 		cv.put(KEY_TEXT, text);
 		cv.put(KEY_LONGITUDE, l.getLongitude());
 		cv.put(KEY_LATITUDE, l.getLatitude());
 		long tmp = this.db.insert(TABLE_NOTE, null, cv);
 		this.close();
 		return tmp;
 	}
 
 	/**
 	 * 
 	 * @param  rowId row id of the note to delete
 	 * @return true if the whereclause is passed in, false otherwise
 	 */
 	public boolean deleteNote(long rowId){
 		this.open();
 		boolean b = this.db.delete(TABLE_NOTE, KEY_ROWID + "=" + rowId, null) > 0;
 		this.close();
 		return b;
 	}
 
 	/**
 	 * 
 	 * @return all data in the note table represented as a list of Note objects
 	 */
 	public List<Note> getNoteList(){
 		this.open();
 		Cursor c = this.getAllNotes();
 		List<Note> l = new ArrayList<Note>();
 		if(c.moveToFirst()){
 			do{
 				Integer rowId = Integer.parseInt(c.getString(0));
 				String title = c.getString(1);
 				String text = c.getString(2);
 				Double longitude = Double.parseDouble(c.getString(3));
 				Double latitude = Double.parseDouble(c.getString(4));
 				l.add(new Note(rowId, title, text, new Location(longitude, latitude)));
 			}while(c.moveToNext());
 		}
 		this.close();
 		return l;
 	}
 
 	private Cursor getAllNotes(){
		return this.db.query(TABLE_NOTE, new String[]{ KEY_ROWID, KEY_TITLE, KEY_TEXT, KEY_LONGITUDE, KEY_LATITUDE }, 
				null, null, null, null, null);
 	}
 
 	/**
 	 * 
	 * @param rowId	 row id of the note to update
 	 * @param title the title to update to
 	 * @param text the text to update to
 	 * @return true if database was updated, false otherwise
 	 */
 	public boolean updateNote(long rowId, String title, String text, Location l){
 		this.open();
 		ContentValues cv = new ContentValues();
 		cv.put(KEY_TITLE, title);
 		cv.put(KEY_TEXT, text);
 		cv.put(KEY_LONGITUDE, l.getLongitude());
 		cv.put(KEY_LONGITUDE, l.getLatitude());
 		boolean b = this.db.update(TABLE_NOTE, cv, KEY_ROWID + "=" + rowId, null) > 0;
 		this.close();
 		return b;
 	}
 
 	private DatabaseHandler open() throws SQLException{
 		this.db = this.dbHelp.getWritableDatabase();
 		return this;
 	}
 
 	private void close(){
 		this.db.close();
 	}
 }
