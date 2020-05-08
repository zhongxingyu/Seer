 package com.plingnote;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.util.Log;
 
 public class DatabaseHandler {
 	// Name of database file
 	private static final String DB_NAME = "notedb";
 
 	// Table
 	private static final String TABLE_NOTE = "Note";
 
 	// Columns
 	private static final String ID = "docid"; //created automatically
 	private static final String KEY_TEXT = "Text";
 	private static final String KEY_TITLE = "Title";
 	private static final String KEY_LONGITUDE = "Longitude";
 	private static final String KEY_LATITUDE = "Latitude";
 	private static final String KEY_IMAGEPATH = "ImagePath";
 	private static final String KEY_ALARM = "Alarm";
 	private static final String KEY_DATE = "Date";
 
 	// SQL statement to create Note table using fts3
 	private static final String CREATE_FTS_TABLE = "create virtual table " + TABLE_NOTE + " using fts3("
 			+ KEY_TITLE + " String, " + KEY_TEXT + " String, " 
 			+ KEY_LONGITUDE +" Double not null, "+ KEY_LATITUDE +" Double not null, " 
 			+ KEY_IMAGEPATH + " String, " + KEY_ALARM + " String, " + KEY_DATE + " String);";
 
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
 				db.execSQL(CREATE_FTS_TABLE);
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
 	 * @return id or -1 if an error occurred
 	 */
 	public long insertNote(String title, String text, Location l, String path, String alarm){
 		if(l == null)
 			l = new Location(0.0, 0.0);
 		this.open();
 		ContentValues cv = new ContentValues();
 		cv.put(KEY_TITLE, title);
 		cv.put(KEY_TEXT, text);
 		cv.put(KEY_LONGITUDE, l.getLongitude());
 		cv.put(KEY_LATITUDE, l.getLatitude());
 		cv.put(KEY_IMAGEPATH, path);
 		cv.put(KEY_ALARM, alarm);
 		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); 
 		Date date = new Date();
 		cv.put(KEY_DATE, dateFormat.format(date));
 		long tmp = this.db.insert(TABLE_NOTE, null, cv);
 		this.close();
 		return tmp;
 	}
 
 	/**
 	 * 
 	 * @param  id id of the Note to delete
 	 * @return true if the whereclause is passed in, false otherwise
 	 */
 	public boolean deleteNote(int id){
 		this.open();
 		boolean b = this.db.delete(TABLE_NOTE, ID + "=" + id, null) > 0;
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
 		List<Note> l = this.createNoteList(c);
 		this.close();
 		return l;
 	}
 
 	private Cursor getAllNotes(){
 		return this.db.query(TABLE_NOTE, new String[]{ ID, KEY_TITLE, KEY_TEXT,
 				KEY_LONGITUDE, KEY_LATITUDE, KEY_IMAGEPATH, KEY_ALARM, KEY_DATE },
 				null, null,null, null, null);
 	}
 
 	/**
 	 * 
 	 * @param id id of the note to update
 	 * @param title the title to update to
 	 * @param text the text to update to
 	 * @return true if database was updated, false otherwise
 	 */
 	public boolean updateNote(int id, String title, String text, Location l, String path, String alarm){
 		if(l == null)
 			l = new Location(0.0, 0.0);
 		this.open();
 		ContentValues cv = new ContentValues();
 		cv.put(KEY_TITLE, title);
 		cv.put(KEY_TEXT, text);
 		cv.put(KEY_LONGITUDE, l.getLongitude());
		cv.put(KEY_LATITUDE, l.getLatitude());
 		cv.put(KEY_IMAGEPATH, path);
 		cv.put(KEY_ALARM, alarm);
 		boolean b = this.db.update(TABLE_NOTE, cv, ID + "=" + id, null) > 0;
 		this.close();
 		return b;
 	}
 	
 	/**
 	 * 
 	 * @param id id of the note to update
 	 * @param title the title to update to
 	 * @return true if database was updated, false otherwise 
 	 */
 	public boolean updateTitle(int id, String title){
 		this.open();
 		ContentValues cv = new ContentValues();
 		cv.put(KEY_TITLE, title);
 		boolean b = this.db.update(TABLE_NOTE, cv, ID + "=" + id, null) > 0;
 		this.close();
 		return b;
 	}
 	
 	/**
 	 * 
 	 * @param id id of the note to update
 	 * @param text the text to update to
 	 * @return true if database was updated, false otherwise 
 	 */
 	public boolean updateText(int id, String text){
 		this.open();
 		ContentValues cv = new ContentValues();
 		cv.put(KEY_TEXT, text);
 		boolean b = this.db.update(TABLE_NOTE, cv, ID + "=" + id, null) > 0;
 		this.close();
 		return b;
 	}
 	
 	/**
 	 * 
 	 * @param id id of the note to update
 	 * @param l the Location object with the longitude and latitude to update to
 	 * @return true if database was updated, false otherwise 
 	 */
 	public boolean updateLocation(int id, Location l){
 		this.open();
 		ContentValues cv = new ContentValues();
 		cv.put(KEY_LONGITUDE, l.getLongitude());
 		cv.put(KEY_LATITUDE, l.getLatitude());
 		boolean b = this.db.update(TABLE_NOTE, cv, ID + "=" + id, null) > 0;
 		this.close();
 		return b;
 	}
 	
 	/**
 	 * 
 	 * @param id id of the note to update
 	 * @param path the image path to update to
 	 * @return true if database was updated, false otherwise 
 	 */
 	public boolean updateImagePath(int id, String path){
 		this.open();
 		ContentValues cv = new ContentValues();
 		cv.put(KEY_IMAGEPATH, path);
 		boolean b = this.db.update(TABLE_NOTE, cv, ID + "=" + id, null) > 0;
 		this.close();
 		return b;
 	}
 	
 	/**
 	 * 
 	 * @param id id of the note to update
 	 * @param alarm the alarm date to update to
 	 * @return true if database was updated, false otherwise 
 	 */
 	public boolean updateAlarm(int id, String alarm){
 		this.open();
 		ContentValues cv = new ContentValues();
 		cv.put(KEY_ALARM, alarm);
 		boolean b = this.db.update(TABLE_NOTE, cv, ID + "=" + id, null) > 0;
 		this.close();
 		return b;
 	}
 
 	/**
 	 * 
 	 * @param id id of the row to retrieve data from
 	 * @return a Note object containting all data from the selected row
 	 */
 	public Note getNote(int id){
 		this.open();
 		Cursor c = this.findNoteById(id);
 		c.move(1);
 		String title = c.getString(1);
 		String text = c.getString(2);
 		Double longitude = Double.parseDouble(c.getString(3));
 		Double latitude = Double.parseDouble(c.getString(4));
 		String imagePath = c.getString(5);
 		String alarm = c.getString(6);
 		String date = c.getString(7);
 		Note n = new Note(id, title, text, new Location(longitude, latitude), imagePath, alarm, date);
 		this.close();
 		return n;
 	}
 
 	private Cursor findNoteById(int id){
 		return this.db.rawQuery("select " + ID + ", * from " 
 				+ TABLE_NOTE + " where " + ID + "='" + id + "'", null);
 	}
 
 	/**
 	 * 
 	 * @return id of the latest inserted Note
 	 */
 	public int getLastId(){
 		this.open();
 		Cursor c = this.db.rawQuery("select " + ID + " from " 
 				+ TABLE_NOTE + " order by " + ID + " desc limit 1", null);
 		c.move(1);
 		int id = Integer.parseInt(c.getString(0));
 		this.close();
 		return id;
 	}
 
 	/**
 	 * 
 	 * @param s the string to search the database with
 	 * @return a list of Note objects with at least one field matching the search
 	 */
 	public List<Note> search(String s){
 		this.open();
 		Cursor c = this.db.rawQuery("select " + ID + ", * from " 
 				+ TABLE_NOTE + " where " + TABLE_NOTE + " match '*" + s + "*'", null);
 		List<Note> l = this.createNoteList(c);
 		this.close();
 		return l;
 	}
 
 	private List<Note> createNoteList(Cursor c){
 		List<Note> l = new ArrayList<Note>();
 		if(c.moveToFirst()){
 			do{
 				int id = Integer.parseInt(c.getString(0));
 				String title = c.getString(1);
 				String text = c.getString(2);
 				Double longitude = Double.parseDouble(c.getString(3));
 				Double latitude = Double.parseDouble(c.getString(4));
 				String imagePath = c.getString(5);
 				String alarm = c.getString(6);
 				String date = c.getString(7);
 				l.add(new Note(id, title, text, new Location(longitude, latitude), imagePath, alarm, date));
 			}while(c.moveToNext());
 		}
 		return l;
 	}
 
 	private DatabaseHandler open() throws SQLException{
 		this.db = this.dbHelp.getWritableDatabase();
 		return this;
 	}
 
 	private void close(){
 		this.db.close();
 	}
 }
