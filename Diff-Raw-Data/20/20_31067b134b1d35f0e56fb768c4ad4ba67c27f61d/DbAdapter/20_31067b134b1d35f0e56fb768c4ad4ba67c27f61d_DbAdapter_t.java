 package edu.vanderbilt.vuphone.android.storage;
 
 import java.util.Calendar;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.database.sqlite.SQLiteDatabase.CursorFactory;
 import android.util.Log;
 
 
 /**
  * Sql DB interface for the rest of the application
  * 
  * @author austin
  *
  */
 public class DbAdapter {
 
 	/** Used for logging */
 	//private static final String pre = "DBAdapter: ";
 
 	/** Used for database updates */
	private static final int DB_VERSION = 7;
 
 	/** The filename where the database is stored */
 	private static final String DB_NAME = "dining.db";
 
 	/** The main table name */
 	protected static final String RESTAURANT_TABLE = "restaurants";
 
 	/** The index column */
 	protected static final String COLUMN_ID = "_id";
 
 	/** The other column names */
 	protected static final String COLUMN_NAME 			= "name";
 	protected static final String COLUMN_HOUR_SUN 		= "hourS";
 	protected static final String COLUMN_HOUR_MON 		= "hourM";
 	protected static final String COLUMN_HOUR_TUE 		= "hourT";
 	protected static final String COLUMN_HOUR_WED 		= "hourW";
 	protected static final String COLUMN_HOUR_THU 		= "hourTh";
 	protected static final String COLUMN_HOUR_FRI 		= "hourF";
 	protected static final String COLUMN_HOUR_SAT 		= "hourSa";
 	protected static final String COLUMN_MENU 			= "menu";
 	protected static final String COLUMN_DESCRIPTION 	= "description";
 	protected static final String COLUMN_TYPE 			= "type";
 	protected static final String COLUMN_ICON 			= "icon";
 	protected static final String COLUMN_LATITUDE 		= "latitude";
 	protected static final String COLUMN_LONGITUDE 		= "longitude";
 	protected static final String COLUMN_BOOLEANS		= "bools";
 	protected static final String COLUMN_PHONE_NUMBER 	= "phoneNumber";
 	protected static final String COLUMN_URL 			= "url";
 	
 	/** Handle to the database instance */
 	private SQLiteDatabase _database;
 
 	/** Used to help open and update the database */
 	DBOpenHelper _openHelper;
 
 	private static class DBOpenHelper extends SQLiteOpenHelper {
 
 		/** Used for logging */
 		private static final String pre = "DBOpenHelper: ";
 
 
 		/** Used to create database */
 		private static final String RESTAURANT_CREATE = "CREATE TABLE IF NOT EXISTS "
 				+ RESTAURANT_TABLE		+ " ("
 				+ COLUMN_ID				+ " INTEGER PRIMARY KEY AUTOINCREMENT,"
 				+ COLUMN_NAME 			+ " TEXT NOT NULL,"
 				+ COLUMN_HOUR_SUN 		+ " INTEGER NOT NULL,"
 				+ COLUMN_HOUR_MON 		+ " INTEGER NOT NULL,"
 				+ COLUMN_HOUR_TUE 		+ " INTEGER NOT NULL,"
 				+ COLUMN_HOUR_WED 		+ " INTEGER NOT NULL,"
 				+ COLUMN_HOUR_THU 		+ " INTEGER NOT NULL,"
 				+ COLUMN_HOUR_FRI 		+ " INTEGER NOT NULL,"
 				+ COLUMN_HOUR_SAT 		+ " INTEGER NOT NULL,"
 				+ COLUMN_BOOLEANS 		+ " INTEGER NOT NULL,"
 				+ COLUMN_TYPE 			+ " TEXT NOT NULL,"
 				+ COLUMN_LATITUDE 		+ " INTEGER NOT NULL,"
 				+ COLUMN_LONGITUDE 		+ " INTEGER NOT NULL,"
 				+ COLUMN_ICON 			+ " INTEGER NOT NULL,"
 				+ COLUMN_DESCRIPTION 	+ " TEXT,"
 				+ COLUMN_PHONE_NUMBER 	+ " TEXT,"
 				+ COLUMN_URL 			+ " TEXT,"
 				+ COLUMN_MENU 			+ " BLOB);";
 
 		// create table restaurants (_id integer primary key autoincrement,
 		// name text not null, latitude real not null, longitude real not null,
 		// description text not null, favorite integer not null, hour blob not null);
 
 		/**
 		 * @see android.database.sqlite.SQLiteOpenHelper#SQLiteOpenHelper(Context,
 		 *      String, CursorFactory, int)
 		 */
 		protected DBOpenHelper(Context context) {
 			super(context, DB_NAME, null, DB_VERSION);
 		}
 
 		/**
 		 * @see android.database.sqlite.SQLiteOpenHelper#onCreate(android.database.sqlite.SQLiteDatabase)
 		 */
 		@Override
 		public void onCreate(SQLiteDatabase db) {
 			Log.i("i", pre + "Creating a new DB");
 			db.execSQL(RESTAURANT_CREATE); 
 		}
 
 		/**
 		 * @see android.database.sqlite.SQLiteOpenHelper#onUpgrade(android.database.sqlite.SQLiteDatabase,
 		 *      int, int)
 		 */
 		@Override
 		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 			// Log the version upgrade.
 			Log.w("Warning", pre + "Upgrading from version " + oldVersion + " to "
 					+ newVersion + ", which will destroy all old data");
 
 			db.execSQL("DROP TABLE IF EXISTS " + RESTAURANT_TABLE);
 			onCreate(db);
 		}
 	}
 
 	
 	
 	protected DbAdapter(Context context) {
 		_openHelper = new DBOpenHelper(context);
 	}
 
 	/** Used to close the database when done */
 	protected void close() {
 		_openHelper.close();
 	}
 
 	/**
 	 * Create a new restaurant using the restaurant provided. 
 	 * If the restaurant is successfully created
 	 * return the new rowId for that restaurant, otherwise return a -1 to
 	 * indicate failure.
 	 * 
 	 * @param name
 	 *            the name of the restaurant
 	 * @param latitude
 	 *            the latitude of the restaurant location
 	 * @param longitude
 	 *            the longitude of the restaurant location
 	 * @param description
 	 *            the restaurant description
 	 * @param favorite
 	 *            true indicates this restaurant is a favorite
 	 * @param hours
 	 *            the RestaurantHours object that represents the hours this
 	 *            Restaurant is open
 	 * 
 	 * @return rowId or -1 if failed
 	 */
 	protected long createRestaurant(Restaurant r) {
 		ContentValues initialValues = new ContentValues(6);
 		initialValues.put(COLUMN_NAME 			, r.getName());
 		initialValues.put(COLUMN_HOUR_SUN 		, r.getHours().flatten(Calendar.SUNDAY));
 		initialValues.put(COLUMN_HOUR_MON 		, r.getHours().flatten(Calendar.MONDAY));
 		initialValues.put(COLUMN_HOUR_TUE 		, r.getHours().flatten(Calendar.TUESDAY)); 
 		initialValues.put(COLUMN_HOUR_WED 		, r.getHours().flatten(Calendar.WEDNESDAY)); 
 		initialValues.put(COLUMN_HOUR_THU 		, r.getHours().flatten(Calendar.THURSDAY)); 
 		initialValues.put(COLUMN_HOUR_FRI 		, r.getHours().flatten(Calendar.FRIDAY)); 
 		initialValues.put(COLUMN_HOUR_SAT 		, r.getHours().flatten(Calendar.SATURDAY));
 		initialValues.put(COLUMN_DESCRIPTION 	, r.getDescription());
 		initialValues.put(COLUMN_TYPE 			, r.getType());
 		initialValues.put(COLUMN_ICON 			, r.getIcon());
 		initialValues.put(COLUMN_LATITUDE 		, r.getLat());
 		initialValues.put(COLUMN_LONGITUDE 		, r.getLon());
 		initialValues.put(COLUMN_BOOLEANS 		, booleansEncode(new boolean [] 
 		         {r.favorite(), r.mealPlanAccepted(), r.mealMoneyAccepted(), r.offCampus()}));
 		initialValues.put(COLUMN_PHONE_NUMBER 	, r.getPhoneNumber());
 		initialValues.put(COLUMN_URL 			, r.getUrl());
 		
 		/*		
 		// TODO remove comments when menu functional
 		if (r.getMenu() != null) {
 			XStream xstream = new XStream(new DomDriver());
 			String menuf = xstream.toXML(r.getMenu());
 			initialValues.put(COLUMN_MENU, menuf);
 		}
 		*/
 		return _database.insert(RESTAURANT_TABLE, null, initialValues);
 	}
 
 	/**
 	 * Delete the restaurant with the given rowId
 	 * 
 	 * @param rowId
 	 *            id of restaurant to delete
 	 * @return true if deleted, false otherwise
 	 */
 	protected boolean deleteRestaurant(long rowId) {
 		return _database.delete(RESTAURANT_TABLE, COLUMN_ID + "=" + rowId, null) > 0;
 	}
 	
 	/** Deletes the entire contents of the database
 	 * @return true if successful
 	 */
 	protected boolean deleteAllRestaurants() {
 		return _database.delete(RESTAURANT_TABLE, null, null) > 0;
 	}
 
 	
     /**
      * Return a Cursor over the list of all restaurants in the database
      * 
      * @param columns
      * 			An array of column names required to be traversable by the 
      * 			returned Cursor
      * @return This cursor allows you to reference these columns. 
      */
     protected Cursor getCursor(String [] columns) {
     	return _database.query(RESTAURANT_TABLE, columns, null, null, null, null, null);
     }
     
     
     /**
      * @param columns
      * 			An array of column names required to be traversable by the 
      * 			returned Cursor
      * @param rowId
      * 			the rowID of the restaurant to be traversed
      * @return A cursor to traverse over Restaurant with rowID.
      */
     protected Cursor getCursor(String [] columns, long rowId) {
     	return _database.query(true, RESTAURANT_TABLE, columns, COLUMN_ID + "=" + rowId, null, null, null, null, null);
     }
 
 
 	/**
 	 * Update the restaurant using the details provided. The restaurant to be
 	 * updated is specified using the rowId, and it is altered to use the name,
 	 * latitude, longitude, description, favorite values passed in
 	 * 
 	 * @param rowId
 	 *            id of note to update
 	 * @param updated
 	 * 			the updated version of restaurant with row id rowId
 	 * @return true if the restaurant was successfully updated, false otherwise
 	 * 
 	 * TODO add the other fields that might be updated as they are implemented
 	 */
 	protected boolean updateRestaurant(long rowId, Restaurant updated) {
 
 		ContentValues updateParams = new ContentValues(16);
 		updateParams.put(COLUMN_NAME 			, updated.getName());                                     
 		updateParams.put(COLUMN_HOUR_SUN 		, updated.getHours().flatten(Calendar.SUNDAY));           
 		updateParams.put(COLUMN_HOUR_MON 		, updated.getHours().flatten(Calendar.MONDAY));           
 		updateParams.put(COLUMN_HOUR_TUE 		, updated.getHours().flatten(Calendar.TUESDAY));          
 		updateParams.put(COLUMN_HOUR_WED 		, updated.getHours().flatten(Calendar.WEDNESDAY));        
 		updateParams.put(COLUMN_HOUR_THU 		, updated.getHours().flatten(Calendar.THURSDAY));         
 		updateParams.put(COLUMN_HOUR_FRI 		, updated.getHours().flatten(Calendar.FRIDAY));           
 		updateParams.put(COLUMN_HOUR_SAT 		, updated.getHours().flatten(Calendar.SATURDAY));         
 		updateParams.put(COLUMN_DESCRIPTION 	, updated.getDescription());                              
 		updateParams.put(COLUMN_TYPE 			, updated.getType());                                     
 		updateParams.put(COLUMN_ICON 			, updated.getIcon());                                     
 		updateParams.put(COLUMN_LATITUDE 		, updated.getLat());                                      
 		updateParams.put(COLUMN_LONGITUDE 		, updated.getLon());                                      
 		updateParams.put(COLUMN_BOOLEANS 		, booleansEncode(new boolean []
 		    {updated.favorite(), updated.mealPlanAccepted(), updated.mealMoneyAccepted(), updated.mealPlanAccepted(), updated.offCampus()}));                          
 		updateParams.put(COLUMN_PHONE_NUMBER 	, updated.getPhoneNumber());                              
 		updateParams.put(COLUMN_URL 			, updated.getUrl());                                      
 	
 		/*
 		if (updated.getMenu() != null) {
 			XStream xstream = new XStream(new DomDriver());
 			String menuf = xstream.toXML(updated.getMenu());
 			updateParams.put(COLUMN_MENU, menuf);
 		}
 		*/
 		
 		return _database.update(RESTAURANT_TABLE, updateParams,
 				COLUMN_ID + "=" + rowId, null) > 0;
 	}
 	
 	/** 
 	 * Updates a row with the given contentValue pairs
 	 * @param rowId 
 	 * 	value of the column COLUMN_ID for the restaurant
 	 * @param newVals
 	 * 	the value map
 	 * @return true if successful
 	 * 	
 	 */
 	protected boolean updateColumns(long rowId, ContentValues newVals) {
 		return _database.update(RESTAURANT_TABLE, newVals, 
 				COLUMN_ID + "=" + rowId, null) > 0;
 	}
 	
 	/**
 	 * Updates a single column of a single row with the given value
 	 * @param rowId
 	 * 	value of the column COLUMN_ID for the restaurant
 	 * @param column
 	 * 	id of the column to modify
 	 * @param value
 	 * 	new value for the column
 	 * @return true if successful
 	 */
 	protected boolean updateColumn(long rowId, String column, int value) {
 		ContentValues args = new ContentValues(1);
 		args.put(column, value);
 		return _database.update(RESTAURANT_TABLE, args, 
 				COLUMN_ID + "=" + rowId, null) > 0;
 	}
 	
 	/**
 	 * Updates a single column of a single row with the given value
 	 * @param rowId
 	 * 	value of the column COLUMN_ID for the restaurant
 	 * @param column
 	 * 	id of the column to modify
 	 * @param value
 	 * 	new value for the column
 	 * @return true if successful
 	 */
 	protected boolean updateColumn(long rowId, String column, long value) {
 		ContentValues args = new ContentValues(1);
 		args.put(column, value);
 		return _database.update(RESTAURANT_TABLE, args, 
 				COLUMN_ID + "=" + rowId, null) > 0;
 	}
 	
 	/**
 	 * Updates a single column of a single row with the given value
 	 * @param rowId
 	 * 	value of the column COLUMN_ID for the restaurant
 	 * @param column
 	 * 	id of the column to modify
 	 * @param value
 	 * 	new value for the column
 	 * @return true if successful
 	 */
 	protected boolean updateColumn(long rowId, String column, boolean value) {
 		ContentValues args = new ContentValues(1);
 		args.put(column, value);
 		return _database.update(RESTAURANT_TABLE, args, 
 				COLUMN_ID + "=" + rowId, null) > 0;
 	}
 	
 	/**
 	 * Updates a single column of a single row with the given value
 	 * @param rowId
 	 * 	value of the column COLUMN_ID for the restaurant
 	 * @param column
 	 * 	id of the column to modify
 	 * @param value
 	 * 	new value for the column
 	 * @return true if successful
 	 */
 	protected boolean updateColumn(long rowId, String column, String value) {
 		ContentValues args = new ContentValues(1);
 		args.put(column, value);
 		return _database.update(RESTAURANT_TABLE, args, 
 				COLUMN_ID + "=" + rowId, null) > 0;
 	}
 
 	/** Used to open a readable database */
 	protected DbAdapter openReadable() throws SQLException {
 		_database = _openHelper.getReadableDatabase();
 		return this;
 	}
 
 	/** Used to open a writable database */
 	protected DbAdapter openWritable() throws SQLException {
 		_database = _openHelper.getWritableDatabase();
 		return this;
 	}
 	
 	
 	
 	/** 
 	 * Encodes the given array of booleans into an integer
 	 * Decode with boolenasDecode()
 	 * @param in
 	 * 	boolean array of length less or equal to 27
 	 * @return the encoded integer
 	 */
 	protected static int booleansEncode(boolean []in) {
 		int out = in.length;
 		for (int i = 0; i < in.length; i++)
 			out += ((in[i]?1:0)<<(i + 5));
 		return out;
 	}
 	
 	/**
 	 * Decodes a previously encoded boolean array encoded using
 	 * booleansEncode()
 	 * @param in
 	 * 	the integer to decode
 	 * @param num
 	 * 	the number of booleans 
 	 * @return
 	 */
 	protected static boolean [] booleansDecode(int in) {
 		boolean [] out = new boolean[in & 0x1f];
 		in = in >> 5;
 		for (int i = 0; i < out.length; i++) {
 			out[i]=((in&1)==1);
 			in = in >> 1;
 		}
 		return out;
 	}
 	
 	/**
 	 * Generates RestaurantHours from database hours xml data
 	 * @param xmlFromDatabase
 	 * 		contents of a database COLUMN_HOUR
 	 * @return
 	 *		equivalent RestaurantHours object
 	 */
 	/*
 	protected static RestaurantMenu getMenuFromXml(String xmlFromDatabase) {
 		if (xmlFromDatabase == null){
 			Log.i("DBAdapter", "getMenuFromXml null pointer");
 			return null;
 		}
 		
 		XStream xs = new XStream(new DomDriver());
 		return (RestaurantMenu) xs.fromXML(xmlFromDatabase);
 		
 	}
 	*/
 	
 //
 //	/**
 //	 * not needed by DBWrapper
 //	 * Get a list of all Restaurant IDs in the database
 //	 * 
 //	 * @return A List of Longs, where each Long is the ID of one of the
 //	 *         restaurants in the database. Use the fetchRestaurant() call to
 //	 *         get the Restaurant object
 //	 * 
 //	 * @see DBAdapter.fetchRestaurant(long rowId)
 //	 */
 //	protected List<Long> fetchAllRestaurantIDs() {
 //		Cursor c = database_.query(RESTAURANT_TABLE,
 //				new String[] { COLUMN_ID }, null, null, null, null, null);
 //
 //		ArrayList<Long> restaurantIds = new ArrayList<Long>();
 //
 //		// If there are no restaurants, return empty list
 //		if (c.moveToFirst() == false)
 //			return restaurantIds;
 //
 //		do {
 //			restaurantIds.add(c.getLong(c.getColumnIndex(COLUMN_ID)));
 //		} while (c.moveToNext());
 //		
 //		c.close();
 //
 //		return restaurantIds;
 //	}
 //
 //	/**
 //	 * not needed by DBWrapper
 //	 * Return a Restaurant object for the given restaurant id.
 //	 * 
 //	 * @param rowId
 //	 *            id of restaurant to retrieve
 //	 * @return Restaurant that that ID represents in the database
 //	 * 
 //	 * @throws SQLException
 //	 *             if restaurant could not be found/retrieved
 //	 * 
 //	 * @TODO - Create a RestaurantNotFound exception, and throw that instead
 //	 */
 //	protected Restaurant fetchRestaurant(long rowId) throws SQLException {
 //		Cursor c = database_.query(true, RESTAURANT_TABLE, new String[] {
 //				COLUMN_ID, COLUMN_NAME, COLUMN_LATITUDE, COLUMN_LONGITUDE,
 //				COLUMN_DESCRIPTION, COLUMN_FAVORITE, COLUMN_HOUR }, COLUMN_ID + "=" + rowId,
 //				null, null, null, null, null);
 //		
 //		if (c.moveToFirst() == false) 
 //			throw new SQLException("Restaurant was not found");
 //			
 //		String xml = c.getString(c.getColumnIndex(COLUMN_HOUR));
 //		RestaurantHours hours = getRestaurantHoursFromXml(xml);
 //
 //		String name = c.getString(c.getColumnIndex(COLUMN_NAME));
 //		int latitude = c.getInt(c.getColumnIndex(COLUMN_LATITUDE));
 //		int longitude = c.getInt(c.getColumnIndex(COLUMN_LONGITUDE));
 //		boolean fav = (c.getInt(c.getColumnIndex(COLUMN_FAVORITE)) == 1);
 //		
 //		c.close();
 //		
 //		// need to implement the rest of the elements of this constructor
 //		return new Restaurant(name, hours, fav, latitude, longitude, null, null, null, 0x0, true, false, null, null);
 //	}
 }
