 package tasktracker.model;
 
 import tasktracker.controller.DatabaseAdapter;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.util.Log;
 import android.content.ContentValues;
 import android.content.Context;
 
 public class DatabaseModel {
 
 	private DatabaseHelper mDbHelper;
 	private Context mCtx;
 
 	/**
 	 * Database creation SQL statement.
 	 */
 
 	private static final String USERS = "CREATE TABLE users (_id INTEGER primary key autoincrement, "
 			+ "user TEXT NOT NULL, "
 			+ "email TEXT NOT NULL, "
 			+ "password TEXT NOT NULL);";
 
	private static final String VOTES = "CREATE TABLE votes (_id INTEGER primary key autoincrement, "
			+ "task_id INTEGER, " + "user TEXT NOT NULL);";
 
 	private static final String TASKS = "CREATE TABLE tasks (_id INTEGER primary key autoincrement, "
 			+ "user TEXT NOT NULL, "
 			+ "task TEXT NOT NULL, "
 			+ "date TEXT NOT NULL, "
 			+ "text TEXT NOT NULL, "
 			+ "requiresPhoto INTEGER, "
 			+ "requiresText INTEGER, "
 			+ "private INTEGER);";
 
	private static final String MEMBERS = "CREATE TABLE members(_id INTEGER primary key autoincrement, "
			+ "task_id INTEGER, " + "user TEXT NOT NULL);";
 
 	private static final String FULFILLMENTS = "CREATE TABLE fulfillments(_id INTEGER primary key autoincrement,  "
 			+ "task_id INTEGER, "
 			+ "user TEXT NOT NULL, "
 			+ "date TEXT NOT NULL, " + "text TEXT);";
 
 	private static final String PHOTOS = "CREATE TABLE photos(_id INTEGER primary key autoincrement,  "
 			+ "task_id INTEGER, "
 			+ "user TEXT NOT NULL, "
 			+ "date TEXT NOT NULL, " + "photo BLOB NOT NULL);";
 
 	private static final String NOTIFICATIONS = "CREATE TABLE notifications(_id INTEGER primary key autoincrement, "
 			+ "task_id INTEGER,"
 			+ "user TEXT NOT NULL,"
 			+ "text TEXT NOT NULL, " + "viewed INTEGER);";
 
 	public static final String[] CREATE_TABLES = new String[] {//
 	USERS, TASKS, MEMBERS, FULFILLMENTS, PHOTOS, NOTIFICATIONS, VOTES };
 
 	public static final String[] TABLE_NAMES = new String[] { //
 	"users", "tasks", "members", "fulfillments", "photos", "notifications",
 			"votes" };
 
 	private static final String DATABASE_NAME = "data";
 	private static final int DATABASE_VERSION = 2;
 
 	private static final String NAME = "dbAdapter";
 
 	private static class DatabaseHelper extends SQLiteOpenHelper {
 
 		DatabaseHelper(Context context) {
 
 			super(context, DATABASE_NAME, null, DATABASE_VERSION);
 		}
 
 		@Override
 		public void onCreate(SQLiteDatabase db) {
 
 			for (String createTable : CREATE_TABLES) {
 				db.execSQL(createTable);
 			}
 		}
 
 		@Override
 		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 
 			Log.w(NAME, "Upgrading database from version " + oldVersion
 					+ " to " + newVersion + ", which will destroy all old data");
 			for (String table : TABLE_NAMES) {
 				db.execSQL("DROP TABLE IF EXISTS " + table);
 			}
 			onCreate(db);
 		}
 	}
 
 	/**
 	 * Gets the database.
 	 * 
 	 * @return mDbHelper
 	 */
 	public DatabaseHelper getMDbHelper() {
 
 		return mDbHelper;
 	}
 
 	/**
 	 * Sets the database
 	 * 
 	 * @param mDbHelper
 	 */
 	public void setMDbHelper(DatabaseHelper mDbHelper) {
 
 		this.mDbHelper = mDbHelper;
 	}
 
 	/**
 	 * Gets the context of the activity
 	 * 
 	 * @return mCtx the context of the activity
 	 */
 	public Context getMCtx() {
 
 		return mCtx;
 	}
 
 	/**
 	 * Sets the context.
 	 * 
 	 * @param mCtx
 	 *            the context of the activity
 	 */
 	public void setMCtx(Context mCtx) {
 
 		this.mCtx = mCtx;
 	}
 
 	/**
 	 * Open the entries database. If it cannot be opened, try to create a new
 	 * instance of the database. If it cannot be created, throw an exception to
 	 * signal the failure
 	 * 
 	 * @return this (self reference, allowing this to be chained in an
 	 *         initialization call)
 	 * @throws SQLException
 	 *             if the database could be neither opened or created
 	 */
 	public DatabaseAdapter open(DatabaseAdapter databaseAdapter)
 			throws SQLException {
 
 		mDbHelper = new DatabaseHelper(mCtx);
 		databaseAdapter.setMDb(mDbHelper.getWritableDatabase());
 		return databaseAdapter;
 	}
 
 	/**
 	 * Closes the database
 	 */
 	public void close() {
 		mDbHelper.close();
 	}
 
 }
