 package com.basicsetup.db;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.database.sqlite.SQLiteStatement;
 import android.util.Log;
 
 /**
  * DBHelper v2.0 helper class for database CRUD operation on sqlite database
  * 
  * @author Rishi K
  * 
  */
 public class DbHelper {
 	public String databaseName;
 	private int databaseVersion;
 	private List<DbModel> models;
 	private Context context;
 	private SQLiteDatabase db;
 	private SQLiteStatement insertStmt;
 	private static OpenHelper openHelper;
 	private String databasePath = null;
 
 	private static DbHelper dbHelper = null;
 
 	private DbHelper(Context context,String dbPath,String dbName,int dbVersion,List<DbModel>models) {
 		this.context = context;
 		this.databasePath=dbPath;
 		this.databaseName=dbName;
 		this.databaseVersion=dbVersion;
		this.models = models;
 		
 		openHelper = new OpenHelper(this.context);
 		openHelper.close();
 		if (db != null && db.isOpen()) {
 			db.close();
 			openHelper.close();
 		}
 		if (databasePath == null) {
 			db = openHelper.getWritableDatabase();
 		} else {
 			db = SQLiteDatabase.openDatabase(databasePath + databaseName,
 					null, SQLiteDatabase.OPEN_READWRITE);
 		}
 		// Enable foreign key constraints
 		db.execSQL("PRAGMA foreign_keys=ON;");
 
 	}
 
 	public static DbHelper getInstance(Context context,IDbConfiguration dbConfiguration){
 		if ((!isDatabaseExists(	context,
 								dbConfiguration.getDatabasePath(),
 								dbConfiguration.getDatabaseName())) || dbHelper == null) {
 			instanciateDatabase(context, dbConfiguration);
 		}
 		return dbHelper;
 	}
 	
 	public static DbHelper instanciateDatabase(Context context,
 			IDbConfiguration dbConfiguration) {
 		if ((!isDatabaseExists(context, dbConfiguration.getDatabasePath(), dbConfiguration.getDatabaseName())) || dbHelper == null) {
 			
 			dbHelper = null;
 			dbHelper = new DbHelper(context, dbConfiguration.getDatabasePath(),
 					dbConfiguration.getDatabaseName(), dbConfiguration.getDatabaseVersion(),
 					dbConfiguration.getModels());
  		}  
 
 		return dbHelper;
 	}
 	
 	private static boolean isDatabaseExists(Context context,String databasePath,String databsaseName){
 		
		File f = new File((databasePath == null || context.getDatabasePath(databsaseName)
															.getAbsolutePath()
															.equalsIgnoreCase(databasePath)) ? context.getDatabasePath(databsaseName)
																										.getAbsolutePath()
				: (databasePath + File.separator + databsaseName));
 
 		boolean bool = f.exists();
 		f = null;
 		return bool;
 	}
 	
 	/*
 	 * public DbHelper(Context mContext,List<DbModel> models) { this.context =
 	 * mContext; this.models = models; openHelper = new
 	 * OpenHelper(this.context); openHelper.close(); if (db != null &&
 	 * db.isOpen()) { db.close(); openHelper.close(); } db =
 	 * openHelper.getWritableDatabase();
 	 * 
 	 * }
 	 */
 
 	public void close() {
 		if (db != null) {
 			db.close();
 		}
 	}
 
 	public SQLiteDatabase getSQLiteDatabase() {
 		return db;
 	}
 
 	/**
 	 * Method to insert values to database
 	 * 
 	 * @param query
 	 *            = string query as per JDBC prepared statement
 	 * @param values
 	 *            = values substituted for ? in query specified
 	 * @return returns true if insert is successful else returns false
 	 */
 	public boolean insert(String query, String[] values) {
 		this.insertStmt = this.db.compileStatement(query);
 		for (int i = 0; i < values.length; i++) {
 			this.insertStmt.bindString(i + 1, values[i]);
 		}
 		return this.insertStmt.executeInsert() > 0 ? true : false;
 	}
 
 	/**
 	 * @author ritesh
 	 * 
 	 *         Method to update values to database
 	 * 
 	 * @param table
 	 *            = name of table
 	 * @param columns
 	 *            = String[] for columns
 	 * @param values
 	 *            = values substituted for ? in query specified
 	 * @param whereClause
 	 *            = WHERE condition
 	 * @param whereArgs
 	 *            = arguments if where parameter is in prepared statement format
 	 * 
 	 * @return returns true if update is successful else returns false
 	 */
 
 	public boolean update(String table, String columns[], String[] values,
 			String whereClause, String whereArgs[]) {
 
 		int size = columns.length;
 		ContentValues cv = new ContentValues(size);
 		for (int i = 0; i < size; i++) {
 			cv.put(columns[i], values[i]);
 		}
 
 		return this.db.update(table, cv, whereClause, whereArgs) > 0 ? true
 				: false;
 
 	}
 
 	/**
 	 * @author ritesh
 	 * 
 	 *         Method to delete row(s) from table
 	 * 
 	 * @param table
 	 *            = name of table
 	 * @param whereClause
 	 *            = WHERE condition
 	 * @param whereArgs
 	 *            = arguments if where parameter is in prepared statement format
 	 * 
 	 * @return returns true if delete is successful else returns false
 	 */
 
 	public boolean delete(String table, String whereClause, String whereArgs[]) {
 
 		return this.db.delete(table, whereClause, whereArgs) > 0 ? true : false;
 	}
 
 	/**
 	 * Clears complete database
 	 */
 	public void delete() {
 		for (DbModel query : models) {
 			this.db.delete(query.getTableName(), null, null);
 		}
 	}
 
 	public List<Object[]> parseCursorToList(Cursor cursor) {
 		List<Object[]> list = new ArrayList<Object[]>();
 		String columns[] = cursor.getColumnNames();
 		if (cursor.moveToFirst()) {
 			do {
 				int i = 0;
 				Object row[] = new Object[columns.length];
 				for (String column : columns) {
 					row[i] = cursor.getString(cursor.getColumnIndex(column));
 					Log.d("size", String.valueOf(row[i]));
 					i++;
 				}
 				list.add(row);
 			} while (cursor.moveToNext());
 		}
 		cursor.close();
 		return list;
 	}
 
 	/**
 	 * Selects all columns from table
 	 * 
 	 * @param table
 	 *            = name of table from which records need to be specified
 	 * @return returns List of Object[], each element in List represent row of
 	 *         table in Object[] form
 	 */
 	public List<Object[]> select(String table) {
 		Cursor cursor = this.db.query(table, new String[] { "*" }, null, null,
 				null, null, null);
 		List<Object[]> list = parseCursorToList(cursor);
 		if (cursor != null && !cursor.isClosed()) {
 			cursor.close();
 		}
 		return list;
 	}
 
 	/**
 	 * Selects records from table
 	 * 
 	 * @param table
 	 *            = name of table
 	 * @param columns
 	 *            = String[] for columns
 	 * @param where
 	 *            = WHERE condition
 	 * @param whereargs
 	 *            = arguments if where parameter is in prepared statement format
 	 * @param groupby
 	 *            = GROUP BY column(s)
 	 * @param having
 	 *            = HAVING condition
 	 * @param orderby
 	 *            = ORDER BY column witrh asc, desc specification
 	 * @return
 	 */
 	public List<Object[]> select(String table, String columns[], String where,
 			String whereargs[], String groupby, String having, String orderby) {
 
 		Cursor cursor = this.db.query(table, columns, where, whereargs,
 				groupby, having, orderby);
 		List<Object[]> list = parseCursorToList(cursor);
 		if (cursor != null && !cursor.isClosed()) {
 			cursor.close();
 		}
 		return list;
 	}
 
 	public int getMaxID(String tableName) {
 		String query = "SELECT MAX(_id) FROM " + tableName;
 		Cursor cursor = db.rawQuery(query, null);
 
 		int id = 0;
 		if (cursor.moveToFirst()) {
 			do {
 				id = cursor.getInt(0);
 			} while (cursor.moveToNext());
 		}
 		cursor.close();
 		return id;
 	}
 
 	public int getCount(String tableName) {
 		String query = "SELECT count(*) FROM " + tableName;
 		Cursor cursor = db.rawQuery(query, null);
 
 		int id = 0;
 		if (cursor.moveToFirst()) {
 			do {
 				id = cursor.getInt(0);
 			} while (cursor.moveToNext());
 		}
 		cursor.close();
 		return id;
 	}
 
 	public boolean isNotEmpty(String table) {
 		Cursor c = null;
 		try {
 			c = db.rawQuery("select * from " + table, null);
 			return c.moveToFirst();
 		} catch (Exception e) {
 			return false;
 		} finally {
 			if (c != null) {
 				c.close();
 			}
 		}
 
 	}
 
 	public List<DbModel> getModels() {
 		return models;
 	}
 
 	public void setModels(List<DbModel> models) {
 		this.models = models;
 	}
 
 	private class OpenHelper extends SQLiteOpenHelper {
 
 		private static final String TAG = "OpenHelper";
 		private String sql;
 
 		OpenHelper(Context context) {
 			super(context, databaseName, null, databaseVersion);
 		}
 
 		@Override
 		public void onCreate(SQLiteDatabase db) {
 			if (models != null) {
 
 				Log.d(TAG, "Table List " + models.size());
 
 				for (DbModel query : models) {
 					db.execSQL(query.getCreateStatement());
 					Log.d(TAG, "created table " + query.getTableName());
 				}
 
 			}
 		}
 
 		@Override
 		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 			Log.d("upgrading database", databaseName);
 			if (models != null) {
 				for (DbModel query : models) {
 					db.execSQL("DROP TABLE IF EXISTS " + query.getTableName());
 				}
 			}
 			onCreate(db);
 		}
 	}
 
 }
