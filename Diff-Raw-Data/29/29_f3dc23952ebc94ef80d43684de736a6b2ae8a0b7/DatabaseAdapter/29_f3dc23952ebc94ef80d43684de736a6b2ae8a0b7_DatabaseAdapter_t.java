 package no.ctryti.dagensatuio;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.database.sqlite.SQLiteQueryBuilder;
 import android.net.Uri;
 import android.util.Log;
 
 public class DatabaseAdapter {
 
 	private static final String DATABASE_NAME = "dagensatuio.db";
 	private static final String TAG = "DatabaseAdapter";
	
 	/* Database column names */
 	public static final String KEY_PLACE = "place";
 	public static final String KEY_DAY = "day";
 	public static final String KEY_TYPE = "type";
 	public static final String KEY_DESC = "desc";
 	public static final String KEY_DISH = "dish";
 	public static final String KEY_GLUTEN = "has_gluten";
 	public static final String KEY_LAKTOSE = "has_laktose";
 	public static final String KEY_ROWID = "_id";
 
	private DatabaseHelper mDbHelper;
	private SQLiteDatabase mDb;
	private Context mCtx;

	
 	private static class DatabaseHelper extends SQLiteOpenHelper {
 
 		private static final String TAG = "DatabaseAdapter$DatabaseHelper";
 		private static final int DATABASE_VERSION = 4;
 
 		/* Create statements */
 		private static final String CREATE_TABLE_PLACES =
 			"create table " + KEY_PLACE + " (\n" +
 			"\t_id integer primary key autoincrement, \n" +
 			"\t"+KEY_PLACE + " text not null unique\n" +
 			");";
 		private static final String CREATE_TABLE_DAY =
 			"create table " + KEY_DAY + "(\n" +
 			"\t_id integer primary key autoincrement, \n" +
 			"\t"+KEY_DAY + " text not null unique\n" +
 			");";
 		private static final String CREATE_TABLE_TYPE =
 			"create table " + KEY_TYPE + "(\n" +
 			"\t_id integer primary key autoincrement, \n" +
 			"\t"+KEY_TYPE + " text not null unique\n" +
 			");";
 		private static final String CREATE_TABLE_DISH =
 			"create table " + KEY_DISH + "(\n" +
 			"\t_id integer primary key autoincrement, \n" +
 			"\t"+KEY_DESC   + " text not null, \n" +
 			"\t"+KEY_PLACE  + " integer not null, \n" +
 			"\t"+KEY_DAY    + " integer not null, \n" +
 			"\t"+KEY_TYPE   + " integer not null, \n" +
 			"\t"+KEY_GLUTEN + " integer not null, \n" +
 			"\t"+KEY_LAKTOSE+ " integer not null, \n" +
 			"\tforeign key (" + KEY_PLACE + ") REFERENCES " + KEY_PLACE + ", \n" + //"(_id)," +
 			"\tforeign key (" + KEY_DAY   + ") REFERENCES " + KEY_DAY   + ", \n" + //"(_id)," +
 			"\tforeign key (" + KEY_TYPE  + ") REFERENCES " + KEY_TYPE  + ", \n" + //"(_id)," +
 			"\tunique (" + KEY_DESC + ", " + KEY_PLACE + ", " + KEY_DAY + ", \n" + KEY_TYPE + ")\n" +
 			");";
 
 
 		public DatabaseHelper(Context context) {
 			super(context, DATABASE_NAME, null, DATABASE_VERSION);
 		}
 
 		@Override
 		public void onCreate(SQLiteDatabase db) {
 			Log.w(TAG, "Creating database (version: " + DATABASE_VERSION + ")");
 			try {
 				db.execSQL(CREATE_TABLE_PLACES);
 				db.execSQL(CREATE_TABLE_DAY);
 				db.execSQL(CREATE_TABLE_TYPE);
 				db.execSQL(CREATE_TABLE_DISH);
 			} catch(Exception e) {
 				Log.e(TAG, "Something happend!:: "+e.getMessage());
 			}
 		}
 
 		@Override
 		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 			Log.w(TAG, "Upgrading database from version " + oldVersion + "to "
 					+ newVersion + ", which will destroy all old data");
 			dropStatements(db);
 			onCreate(db);
 		}
 
 		private void dropStatements(SQLiteDatabase db) {
 			try {
 				db.execSQL("DROP TABLE IF EXISTS " + KEY_DISH);
 				db.execSQL("DROP TABLE IF EXISTS " + KEY_PLACE);
 				db.execSQL("DROP TABLE IF EXISTS " + KEY_TYPE);
 				db.execSQL("DROP TABLE IF EXISTS " + KEY_DAY);
 			} catch(Exception e) {
 				Log.e(TAG, e.getMessage());
 			}
 		}
 
 		private void reCreateDatabase(SQLiteDatabase db) {
 			Log.i(TAG, "Re-creating the database (version " + DATABASE_VERSION + ")");
 			dropStatements(db);
 			onCreate(db);
 		}
 	}
 
 	public DatabaseAdapter(Context ctx) {
 		mCtx = ctx;
 	}
 
 	public DatabaseAdapter open() throws SQLException {
 		mDbHelper = new DatabaseHelper(mCtx);
 		mDb = mDbHelper.getWritableDatabase();
 		return this;
 	}
 
 	public void close() {
 		mDbHelper.close();
 	}
 
 	public void reCreateDatabase() {
 		mDbHelper.reCreateDatabase(mDb);
 	}	
 
 	public Cursor fetchAll() {
 		return mDb.query(KEY_PLACE, new String[] {KEY_ROWID, KEY_PLACE}, null, null, null, null, null);
 	}
 
 	public long insert(ContentValues values) {
 		boolean valid = true;
 		if(!values.containsKey(KEY_DAY))
 			valid = false;
 		if(!values.containsKey(KEY_DESC))
 			valid = false;
 		if(!values.containsKey(KEY_PLACE))
 			valid = false;
 		if(!values.containsKey(KEY_TYPE))
 			valid = false;
 		if(!values.containsKey(KEY_GLUTEN))
 			valid = false;
 		if(!values.containsKey(KEY_LAKTOSE))
 			valid = false;
 		if(!valid)
 			throw new IllegalArgumentException();
 
 		ContentValues cv = new ContentValues();
 		long placeId, typeId, dayId;
 
 		cv.put(KEY_PLACE, values.getAsString(KEY_PLACE));
 		placeId = insertMinorValue(KEY_PLACE, cv);
 
 		cv.clear();
 		cv.put(KEY_TYPE, values.getAsString(KEY_TYPE));
 		typeId = insertMinorValue(KEY_TYPE, cv);
 
 		cv.clear();
 		cv.put(KEY_DAY, values.getAsString(KEY_DAY));
 		dayId = insertMinorValue(KEY_DAY, cv);
 
 		cv.clear();
 		cv.put(KEY_PLACE, placeId);
 		cv.put(KEY_DAY, dayId);
 		cv.put(KEY_TYPE, typeId);
 		cv.put(KEY_DESC, values.getAsString(KEY_DESC));
 		cv.put(KEY_GLUTEN, values.getAsInteger(KEY_GLUTEN));
 		cv.put(KEY_LAKTOSE, values.getAsInteger(KEY_LAKTOSE));
 		//if(values.getAsString(KEY_PLACE).equals("Frederikke kafé") && values.getAsString(KEY_TYPE).equals("Suppe"))
 		//	Log.i(TAG, values.getAsString(KEY_DESC));
 		
 		long rowId = mDb.insert(KEY_DISH, null, cv);
 		return rowId;
 	}
 
 	private long insertMinorValue(String key, ContentValues v) {
 		long rowId;
 		Cursor c = mDb.query(true, key, new String[] {KEY_ROWID, key }, key + "='"+v.getAsString(key)+"'", null, null, null, null, null);
 
 		if(c != null && c.getCount() > 0) {
 			c.moveToFirst();
 		} else {
 			c.close();
 			return mDb.insert(key, null, v);
 		}
 		rowId = c.getInt(c.getColumnIndex(KEY_ROWID));
 		c.close();
 		return rowId;
 	}
 
 }
