 package com.vorsk.crossfitr.models;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.util.Log;
 
 import java.io.*;
 import java.util.Calendar;
 
 import com.vorsk.crossfitr.R;
 
 /**
  * Abstract Base DAO for other Models to extend.
  * 
  * Each relation should have its own DAO. TECHNICALLY nothing in this class
  * is abstract, so you could instantiate it to connect to any table. But that
  * would involve a lot of type-checking on the user end, and probably create
  * inconsistent DB access routines. So don't do it.
  * 
  * @author Vivek
  * @since 1.0
  */
 public abstract class SQLiteDAO
 {
 	//// Constants
 	
 	// Global column names
 	public static final String COL_ID    = "_id";
 	public static final String COL_NAME  = "name";
 	public static final String COL_DESC  = "description";
 	public static final String COL_CDATE = "date_created";
 	public static final String COL_MDATE = "date_modified";
 	
 	// Pre-populated Type IDs
 		// Workout types
 	public static final int TYPE_NONE = 0;
 	public static final int TYPE_WOD = 1;
 	public static final int TYPE_GIRL = 2;
 	public static final int TYPE_HERO = 3;
 	public static final int TYPE_CUSTOM = 4;
 	
 	// Achievement types
 	public static final int TYPE_ALL = 1;
 	public static final int TYPE_MISC = 5;
 	
 		// Score types
 	public static final int SCORE_NONE   = 0;
 	public static final int SCORE_TIME   = 1;
 	public static final int SCORE_REPS   = 2;
 	public static final int SCORE_WEIGHT = 3;
 	
 		// Score value
 	public static final int NOT_SCORED = -1;
 	
 	// Abstract - defined by arguments to the ctor
 	protected final String DB_TABLE;
 	
 	// DB Properties
 	private static final String DB_NAME = "CrossFitr";
	private static final int DB_VERSION = 5;
 	
 	
 	/**
 	 * DB connection object, subclassed for the specific DB params we need
 	 * 
 	 * Though, not entirely sure regarding the necessity of this AND its
 	 * wrapper class. Just referenced a google "common practices" doc to
 	 * make this helper.
 	 */
 	private static class DatabaseHelper extends SQLiteOpenHelper
 	{
 		private Context context;
 		
 		public DatabaseHelper(Context context)
 		{
 			super(context, DB_NAME, null, DB_VERSION);
 			this.context = context;
 		}
 
 		@Override
 		public void onCreate(SQLiteDatabase db)
 		{
 			Log.v("DB", "DB onCreate BEGIN");
 			
 			// Fetch the creation scripts
 			InputStream sqlfile = context.getResources().openRawResource(
 					R.raw.db_create);
 			InputStream insfile = context.getResources().openRawResource(
 					R.raw.db_create_inserts);
 			byte[] reader;
 			String sqltext;
 			String[] statements;
 			
 			try {
 				// Read in the creation script
 				reader = new byte[sqlfile.available()];
 				while (sqlfile.read(reader) != -1){}
 				sqltext = new String(reader);
 				statements = sqltext.split("--###--");
 				
 				// Create all tables
 				Log.v("DB", "Creating database...");
 				for (int ii=0; ii<statements.length; ii++) {
 					db.execSQL(statements[ii]);
 				}
 				Log.v("DB", "Done creating database");
 				
 				// Read in script with the data for prepopulation
 				reader = new byte[insfile.available()];
 				while (insfile.read(reader) != -1) {}
 				sqltext = new String(reader);
 				statements = sqltext.split("--###--");
 				
 				// Prepopulate the db
 				Log.v("DB", "Inserting data...");
 				for (int ii=0; ii<statements.length; ii++) {
 					db.execSQL(statements[ii]);
 				}
 				Log.v("DB", "Done inserting data");
 			} catch (SQLException e) {
 				// TODO: this
 				Log.e("DB", "Error occurred during creation");
 			} catch (IOException e) {
 				// TODO: this
 				Log.e("DB", "Error reading DB creation files");
 			}
 			
 			Log.v("DB", "DB onCreate END");
 		}
 
 		@Override
 		public void onUpgrade(SQLiteDatabase db, int over, int nver)
 		{
 			InputStream sqlfile = context.getResources().openRawResource(
 					R.raw.db_delete);
 			byte[] reader;
 			String sqltext;
 			String[] statements;
 			
 			try {
 				// Read in the deletion script
 				reader = new byte[sqlfile.available()];
 				while (sqlfile.read(reader) != -1){}
 				sqltext = new String(reader);
 				statements = sqltext.split("--###--");
 				
 				// Delete the db
 				Log.v("DB", "Deleting data...");
 				for (int ii=0; ii<statements.length; ii++) {
 					db.execSQL(statements[ii]);
 				}
 			} catch (SQLException e) {
 				// TODO: this
 				Log.e("DB", "Error occurred during creation");
 			} catch (IOException e) {
 				// TODO: this
 				Log.e("DB", "Error reading DB creation files");
 			}
 			
 			onCreate(db);
 		}
 		
 		@Override
 		public void onOpen(SQLiteDatabase db)
 		{
 			super.onOpen(db);
 			if (!db.isReadOnly()) {
 				// This enables foreign keys (Android 2.2+ only)
 				db.execSQL("PRAGMA foreign_keys=ON;");
 			}
 		}
 		
 	} // END DatabaseHelper
 	
 	
 	private DatabaseHelper DBHelper;
 	protected static SQLiteDatabase db;
 
 	/*** Constructors ***/
 
 	public SQLiteDAO(String table, Context ctx)
 	{
 		DB_TABLE = table;
 		DBHelper = new DatabaseHelper(ctx);
 	}
 
 	/*** Private ***/
 	
 	private String getWhereClause(String[] cols)
 	{
 		String sql = "";
 		if (cols == null)
 			return sql;
 		
 		// Build the WHERE clause (append each col-val)
 		if (cols.length > 0) {
 			sql += " WHERE ";
 		}
 		for (int ii = 0; ii < cols.length; ii++) {
 			if (ii != 0)
 				sql += ", ";
 			sql += cols[ii] + " = ?";
 		}
 		return sql;
 	}
 
 	/*** Protected ***/
 
 	protected long insert(ContentValues cv)
 	{
 		Long time = Calendar.getInstance().getTimeInMillis();
 
 		// Automated input for global columns
 		cv.put(COL_ID, (Integer)null); // Always let this autoincrement
 		cv.put(COL_MDATE, time);
 		cv.put(COL_CDATE, time);
 
 		return db.insert(DB_TABLE, null, cv);
 	}
 
 	protected int update(ContentValues cv, String where)
 	{
 		if (where == null)
 			return -1; // GTFO. You are not updating everything.
 		
 		cv.remove(COL_ID);
 		cv.remove(COL_CDATE);
 		cv.remove(COL_MDATE);
 		cv.put(COL_MDATE, Calendar.getInstance().getTimeInMillis());
 
 		return db.update(DB_TABLE, cv, where, null);
 	}
 
 	// TODO: Should just make a deleteByID and disable this...
 	protected int delete(String where)
 	{
 		if (where == null)
 			return -1; // ... Really? GTFO x 99999
 
 		return db.delete(DB_TABLE, where, null);
 	}
 	
 	protected Cursor select(String[] cols, String[] vals)
 			throws SQLException
 	{
 		return select(cols, vals, null, -1);
 	}
 
 	protected Cursor select(String[] cols, String[] vals, String order, int limit)
 			throws SQLException
 	{
 		String sql = "SELECT * FROM " + DB_TABLE;
 		
 		sql += getWhereClause(cols);
 		if (order != null) sql += " ORDER BY " + order;
 		if (limit > 0) sql += " LIMIT " + limit;
 		
 		return db.rawQuery(sql, vals);
 	}
 	
 	protected int selectCount(String[] cols, String[] vals) throws SQLException
 	{
 		String sql = "SELECT COUNT(*) as count FROM " + DB_TABLE;
 		sql += getWhereClause(cols);
 		Cursor cr = db.rawQuery(sql, vals);
 		
 		if (cr == null) {
 			return -1;
 		}
 		if (!cr.moveToFirst()) {
 			cr.close();
 			return -1;
 		}
 		
 		int ind = cr.getColumnIndexOrThrow("count");
 		int result = cr.getInt(ind);
 		cr.close();
 		return result;
 	}
 
 	protected Cursor selectByID(long id) throws SQLException
 	{
 		return db.rawQuery(
 			"SELECT * FROM " + DB_TABLE + " WHERE " + COL_ID + " = " + id,
 			null);
 	}
 	
 	protected String selectNameByID(String table, long id) throws SQLException
 	{
 		Cursor cr = db.rawQuery(
 			"SELECT * FROM " + table + " WHERE " + COL_ID + "=?",
 			new String[] { String.valueOf(id) });
		if (cr == null) {
			return null;
		}
		if (cr.getCount() < 1) {
			cr.close();
 			return null;
 		}
 		
 		int col = cr.getColumnIndexOrThrow(COL_NAME);
 		cr.moveToFirst();
		String result = cr.getString(col);
		cr.close();
		return result;
 	}
 	
 	protected long selectIDByName(String table, String name) throws SQLException
 	{
 		Cursor cr = db.rawQuery(
 			"SELECT * FROM " + table + " WHERE " + COL_NAME + "=?",
 			new String[] { name });
		if (cr == null) {
			return -1;
		}
		if (cr.getCount() < 1) {
 			cr.close();
 			return -1;
 		}
 		
 		int col = cr.getColumnIndexOrThrow(COL_ID);
 		cr.moveToFirst();
 		long out = cr.getLong(col);
 		cr.close();
 		return out;
 	}
 	
 	protected void fetchBaseData(Cursor cr, SQLiteRow row,
 			int ind_id, int ind_dm, int ind_dc)
 	{
 		row._id = cr.getLong(ind_id);
 		// Gets as milliseconds
 		row.date_modified = cr.getLong(ind_dm);
 		row.date_created = cr.getLong(ind_dc);
 	}
 
 	/*** Public ***/
 
 	/**
 	 * This must be called prior to any DB access methods to open a connection
 	 * 
 	 * @throws SQLException
 	 */
 	public void open() throws SQLException
 	{
 		if (db == null) {
 			db = DBHelper.getWritableDatabase();
 		}
 	}
 
 	/**
 	 * Closes the DB connection if it is open
 	 * 
 	 * This should be called after all DB transactions are completed. Not
 	 * calling this can cause problems with hanging cursors if mulitple
 	 * threads are used.
 	 */
 	public void close()
 	{
 		db.close();
 		DBHelper.close();
 		db = null;
 	}
 
 }
