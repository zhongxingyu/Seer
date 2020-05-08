 package ie.ucd.asteroid;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Locale;
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.util.Log;
 
 public class DBAdapter {
 
 	// Column names
 	public static final String KEY_ROWID = "_id";
 	public static final String KEY_OBJECTNAME = "objectName";
 	public static final String KEY_APPROACHDATE = "approachDate";
 	public static final String KEY_APPROACHDATEUNIXTIME = "approachDateUnixTime";
 	public static final String KEY_APPROACHDISTANCE = "approachDistance";
 	public static final String KEY_APPROACHTIME = "approachTime";
 	public static final String KEY_MINDIAMETER = "minDiameter";
 	public static final String KEY_MAXDIAMETER = "maxDiameter";
 	public static final String KEY_ABSOLUTEMAGNITUDE = "absoluteMagnitude";
 	public static final String KEY_RELATIVEVELOCITY = "relativeVelocity";
 	
 	// Database details
 	private static final String DATABASE_TAG = "Asteroid_DBAdapter";
 	private static final String DATABASE_NAME = "Asteroid";
 	private static final String DATABASE_TABLE = "tblAsteroids";
 	private static final int DATABASE_VERSION = 4;
 		
 	// Create database string
 	private static final String DATABASE_CREATE = "create table " + DATABASE_TABLE +
 			" (" + KEY_ROWID +" integer primary key autoincrement, " +
 			"" + KEY_OBJECTNAME + " text not null, " +
 			"" + KEY_APPROACHDATE + " text not null, " +
 			"" + KEY_APPROACHDATEUNIXTIME + " integer not null, " +
 			"" + KEY_APPROACHDISTANCE + " real not null, " +
 			"" + KEY_APPROACHTIME + " text not null, " +
 			"" + KEY_MINDIAMETER + " real not null, " +
 			"" + KEY_MAXDIAMETER + " real not null, " +
 			"" + KEY_ABSOLUTEMAGNITUDE + " real not null, " +
 			"" + KEY_RELATIVEVELOCITY + " real not null);";
 	
 	// DBAdapter properties
 	private final Context context;
 	private DatabaseHelper DBHelper;
 	private SQLiteDatabase db;
 	
 	public DBAdapter(Context ctx)
 	{
 		this.context = ctx;
 		DBHelper = new DatabaseHelper(context);
 	}
 	
 	
 	/** onCreate and onUpgrade methods **/
 	private static class DatabaseHelper extends SQLiteOpenHelper
 	{
 		DatabaseHelper(Context context)
 		{
 			super(context, DATABASE_NAME, null, DATABASE_VERSION);
 		}
 		
 		@Override
 		public void onCreate(SQLiteDatabase db)
 		{
 			Log.w(DATABASE_TAG, "Creating " + DATABASE_NAME + ".");
 			try { 
 				db.execSQL(DATABASE_CREATE);
 			} catch (SQLException e) {
 				Log.w(DATABASE_TAG, "onCreate FAILED. Error: " + e);
 			}
 		}
 		
 		@Override
 		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
 		{
 			Log.w(DATABASE_TAG, "Upgrading " + DATABASE_NAME + " from version " + oldVersion + " to " + newVersion + ".");
 			db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
 			onCreate(db);
 		}
 	}
 	
 	
 	/** Database usage methods **/
     // Opens database
 	public DBAdapter openDB() throws SQLException
 	{
         Log.w(DATABASE_TAG, "Opening " + DATABASE_NAME + " for writing.");
 		db = DBHelper.getWritableDatabase();
 		return this;
 	}
 	
 	// Closes database
 	public void closeDB()
 	{
         Log.w(DATABASE_TAG, "Closing " + DATABASE_NAME + ".");
 		DBHelper.close();
 	}
 	
 	
 	/** Access methods **/	
 	// Get asteroid's rowID
 	public int getAsteroidID(Cursor c)
 	{
 		Log.w(DATABASE_TAG, "Running getAsteroidID on asteroid with cursor.");
 		return c.getInt(c.getColumnIndex(KEY_ROWID));
 	}
 	
 	
 	// Get object name using rowID
 	public String getName(int rowID)
 	{
 		Log.w(DATABASE_TAG, "Running getName on asteroid with rowID: " + rowID + ".");
 		try {
 			Cursor cursorAsteroid = db.query(true, DATABASE_TABLE, new String[] {KEY_ROWID, KEY_OBJECTNAME}, KEY_ROWID + "=" + rowID, null, null, null, null, null);
 			if ( cursorAsteroid != null ) {
 				cursorAsteroid.moveToFirst();
 			}
 			Log.w(DATABASE_TAG, "getName SUCCESSFUL on asteroid with rowID: " + rowID + ".");
 			String objectName = cursorAsteroid.getString(cursorAsteroid.getColumnIndex(KEY_OBJECTNAME));
 			cursorAsteroid.close();
 			return objectName;
 		} catch (SQLException e) {
 			Log.w(DATABASE_TAG, "getName FAILED on asteroid with rowID: " + rowID + ". Error: " + e);
 			return "Error: getName, check Log.";
 		}
 	}
 	// Get object name using Cursor
 	public String getName(Cursor c)
 	{
 		Log.w(DATABASE_TAG, "Running getName on asteroid with cursor.");
 		return c.getString(c.getColumnIndex(KEY_OBJECTNAME));
 	}
 	
 	
 	// Get approach date using rowID
 	public String getApproachDate(int rowID)
 	{
 		Log.w(DATABASE_TAG, "Running getApproachDate on asteroid with rowID: " + rowID + ".");
 		try {
 			Cursor cursorAsteroid = db.query(true, DATABASE_TABLE, new String[] {KEY_APPROACHDATE}, KEY_ROWID + "=" + rowID, null, null, null, null, null);
 			if ( cursorAsteroid != null ) {
 				cursorAsteroid.moveToFirst();
 			}
 			Log.w(DATABASE_TAG, "getApproachDate SUCCESSFUL on asteroid with rowID: " + rowID + ".");
 			String approachDate = cursorAsteroid.getString(cursorAsteroid.getColumnIndex(KEY_APPROACHDATE));
 			cursorAsteroid.close();
 			return approachDate;
 		} catch (SQLException e) {
 			Log.w(DATABASE_TAG, "getApproachDate FAILED on asteroid with rowID: " + rowID + ". Error: " + e);
 			return "Error: getApproachDate, check Log";
 		}
 	}
 	// Get approach date using Cursor
 	public String getApproachDate(Cursor c)
 	{
 		Log.w(DATABASE_TAG, "Running getApproachDate on asteroid with cursor.");
 		return c.getString(c.getColumnIndex(KEY_APPROACHDATE));
 	}
 	
 	
 	// Get approach date for display using rowID (Returns date based on user's locale)
 	public String getApproachDateDisplay(int rowID)
 	{
 		Log.w(DATABASE_TAG, "Running getApproachDateDisplay on asteroid with rowID: " + rowID + ".");
 		try {
 			Cursor cursorAsteroid = db.query(true, DATABASE_TABLE, new String[] {KEY_APPROACHDATE}, KEY_ROWID + "=" + rowID, null, null, null, null, null);
 			if ( cursorAsteroid != null ) {
 				cursorAsteroid.moveToFirst();
 			}
 			Log.w(DATABASE_TAG, "getApproachDateDisplay SUCCESSFUL on asteroid with rowID: " + rowID + ".");
 			String approachDate = cursorAsteroid.getString(cursorAsteroid.getColumnIndex(KEY_APPROACHDATE));
 			cursorAsteroid.close();
 			
 			Locale locale = Locale.getDefault();
 			Date date;
 			
 			try {
 				date = new SimpleDateFormat("dd-MM-yyyy").parse(approachDate);
 				String displayDate = new SimpleDateFormat("dd MMM yyyy", locale).format(date);
 				Log.w(DATABASE_TAG, "Display date: " + displayDate + ". Locale: " + locale);
 				return displayDate;
 			} catch (ParseException e) {
 				e.printStackTrace();
 				return "Error";
 			}
 			
 		} catch (SQLException e) {
 			Log.w(DATABASE_TAG, "getApproachDate FAILED on asteroid with rowID: " + rowID + ". Error: " + e);
 			return "Error: getApproachDateDisplay, check Log";
 		}
 	}
 	// Get approach date for display using cursor (Returns date based on user's locale)
 	public String getApproachDateDisplay(Cursor c)
 	{
 		Log.w(DATABASE_TAG, "Running getApproachDateDisplay on asteroid with cursor.");
 		String approachDate = c.getString(c.getColumnIndex(KEY_APPROACHDATE));
 		Locale locale = Locale.getDefault();
 		Date date;
 		
 		try {
 			date = new SimpleDateFormat("dd-MM-yyyy").parse(approachDate);
 			String displayDate = new SimpleDateFormat("dd MMM yyyy", locale).format(date);
 			Log.w(DATABASE_TAG, "Display date: " + displayDate + ". Locale: " + locale);
 			return displayDate;
 		} catch (ParseException e) {
 			e.printStackTrace();
 			return "Error";
 		}
 	}
 	
 	
 	// Get approach distance using rowID
 	public double getApproachDistance(int rowID)
 	{
 		Log.w(DATABASE_TAG, "Running getApproachDistance on asteroid with rowID: " + rowID + ".");
 		try {
 			Cursor cursorAsteroid = db.query(true, DATABASE_TABLE, new String[] {KEY_APPROACHDISTANCE}, KEY_ROWID + "=" + rowID, null, null, null, null, null);
 			if ( cursorAsteroid != null ) {
 				cursorAsteroid.moveToFirst();
 			}
 			Log.w(DATABASE_TAG, "getApproachDistance SUCCESSFUL on asteroid with rowID: " + rowID + ".");
 			double approachDistance = cursorAsteroid.getDouble(cursorAsteroid.getColumnIndex(KEY_APPROACHDISTANCE));
 			cursorAsteroid.close();
 			return approachDistance;
 		} catch (SQLException e) {
 			Log.w(DATABASE_TAG, "getApproachDistance FAILED on asteroid with rowID: " + rowID + ". Error: " + e);
 			return Double.NaN;
 		}
 	}
 	// Get approach distance using Cursor
 	public double getApproachDistance(Cursor c)
 	{
 		Log.w(DATABASE_TAG, "Running getApproachDistance on asteroid with cursor.");
 		return c.getDouble(c.getColumnIndex(KEY_APPROACHDISTANCE));
 	}
 	
 	
 	// Get approach time using rowID
 	public String getTime(int rowID)
 	{
 		Log.w(DATABASE_TAG, "Running getTime on asteroid with rowID: " + rowID + ".");
 		try {
 			Cursor cursorAsteroid = db.query(true, DATABASE_TABLE, new String[] {KEY_APPROACHTIME}, KEY_ROWID + "=" + rowID, null, null, null, null, null);
 			if ( cursorAsteroid != null ) {
 				cursorAsteroid.moveToFirst();
 			}
 			Log.w(DATABASE_TAG, "getTime SUCCESSFUL on asteroid with rowID: " + rowID + ".");
 			String time = cursorAsteroid.getString(cursorAsteroid.getColumnIndex(KEY_APPROACHTIME));
 			cursorAsteroid.close();
 			return time;
 		} catch (SQLException e) {
 			Log.w(DATABASE_TAG, "getTime FAILED on asteroid with rowID: " + rowID + ". Error: " + e);
 			return "Error: getTime, check Log.";
 		}
 	}
 	// Get approach time using Cursor
 	public String getTime(Cursor c)
 	{
 		Log.w(DATABASE_TAG, "Running getTime on asteroid with cursor.");
 		return c.getString(c.getColumnIndex(KEY_APPROACHTIME));
 	}
 	
 	
 	// Get minimum diameter using rowID
 	public double getMinDiameter(int rowID)
 	{
 		Log.w(DATABASE_TAG, "Running getMinDiameter on asteroid with rowID: " + rowID + ".");
 		try {
 			Cursor cursorAsteroid = db.query(true, DATABASE_TABLE, new String[] {KEY_MINDIAMETER}, KEY_ROWID + "=" + rowID, null, null, null, null, null);
 			if ( cursorAsteroid != null ) {
 				cursorAsteroid.moveToFirst();
 			}
 			Log.w(DATABASE_TAG, "getMinDiameter SUCCESSFUL on asteroid with rowID: " + rowID + ".");
 			double minDiameter = cursorAsteroid.getDouble(cursorAsteroid.getColumnIndex(KEY_MINDIAMETER));
 			cursorAsteroid.close();
 			return minDiameter;
 		} catch (SQLException e) {
 			Log.w(DATABASE_TAG, "getMinDiameter FAILED on asteroid with rowID: " + rowID + ". Error: " + e);
 			return Double.NaN;
 		}
 	}
 	// Get minimum diameter using Cursor
 	public double getMinDiameter(Cursor c)
 	{
 		Log.w(DATABASE_TAG, "Running getMinDiameter on asteroid with cursor.");
 		return c.getDouble(c.getColumnIndex(KEY_MINDIAMETER));
 	}
 	
 	
 	// Get maximum diameter using rowID
 	public double getMaxDiameter(int rowID)
 	{
 		Log.w(DATABASE_TAG, "Running getMaxDiameter on asteroid with rowID: " + rowID + ".");
 		try {
 			Cursor cursorAsteroid = db.query(true, DATABASE_TABLE, new String[] {KEY_MAXDIAMETER}, KEY_ROWID + "=" + rowID, null, null, null, null, null);
 			if ( cursorAsteroid != null ) {
 				cursorAsteroid.moveToFirst();
 			}
 			Log.w(DATABASE_TAG, "getMaxDiameter SUCCESSFUL on asteroid with rowID: " + rowID + ".");
 			double maxDiameter = cursorAsteroid.getDouble(cursorAsteroid.getColumnIndex(KEY_MAXDIAMETER));
 			cursorAsteroid.close();
 			return maxDiameter;
 		} catch (SQLException e) {
 			Log.w(DATABASE_TAG, "getMaxDiameter FAILED on asteroid with rowID: " + rowID + ". Error: " + e);
 			return Double.NaN;
 		}
 	}
 	// Get maximum diameter using Cursor
 	public double getMaxDiameter(Cursor c)
 	{
 		Log.w(DATABASE_TAG, "Running getMaxDiameter on asteroid with cursor.");
 		return c.getDouble(c.getColumnIndex(KEY_MAXDIAMETER));
 	}
 	
 	
 	// Get absolute magnitude using rowID
 	public double getAbsoluteMagnitude(int rowID)
 	{
 		Log.w(DATABASE_TAG, "Running getAbsoluteMagnitude on asteroid with rowID: " + rowID + ".");
 		try {
 			Cursor cursorAsteroid = db.query(true, DATABASE_TABLE, new String[] {KEY_ABSOLUTEMAGNITUDE}, KEY_ROWID + "=" + rowID, null, null, null, null, null);
 			if ( cursorAsteroid != null ) {
 				cursorAsteroid.moveToFirst();
 			}
 			Log.w(DATABASE_TAG, "getAbsoluteMagnitude SUCCESSFUL on asteroid with rowID: " + rowID + ".");
 			double absoluteMagnitude = cursorAsteroid.getDouble(cursorAsteroid.getColumnIndex(KEY_ABSOLUTEMAGNITUDE));
 			cursorAsteroid.close();
 			return absoluteMagnitude;
 		} catch (SQLException e) {
 			Log.w(DATABASE_TAG, "getAbsoluteMagnitude FAILED on asteroid with rowID: " + rowID + ". Error: " + e);
 			return Double.NaN;
 		}
 	}
 	// Get absolute magnitude using Cursor
 	public double getAbsoluteMagnitude(Cursor c)
 	{
 		Log.w(DATABASE_TAG, "Running getAbsoluteMagnitude on asteroid with cursor.");
 		return c.getDouble(c.getColumnIndex(KEY_ABSOLUTEMAGNITUDE));
 	}
 	
 	
 	// Get relative velocity using rowID
 	public double getRelativeVelocity(int rowID)
 	{
 		Log.w(DATABASE_TAG, "Running getRelativeVelocity on asteroid with rowID: " + rowID + ".");
 		try {
 			Cursor cursorAsteroid = db.query(true, DATABASE_TABLE, new String[] {KEY_RELATIVEVELOCITY}, KEY_ROWID + "=" + rowID, null, null, null, null, null);
 			if ( cursorAsteroid != null ) {
 				cursorAsteroid.moveToFirst();
 			}
 			Log.w(DATABASE_TAG, "getRelativeVelocity SUCCESSFUL on asteroid with rowID: " + rowID + ".");
 			double relativeVelocity = cursorAsteroid.getDouble(cursorAsteroid.getColumnIndex(KEY_RELATIVEVELOCITY));
 			cursorAsteroid.close();
 			return relativeVelocity;
 		} catch (SQLException e) {
 			Log.w(DATABASE_TAG, "getRelativeVelocity FAILED on asteroid with rowID: " + rowID + ". Error: " + e);
 			return Double.NaN;
 		}
 	}
 	// Get relative velocity using Cursor
 	public double getRelativeVelocity(Cursor c)
 	{
 		Log.w(DATABASE_TAG, "Running getRelativeVelocity on asteroid with cursor.");
 		return c.getDouble(c.getColumnIndex(KEY_RELATIVEVELOCITY));
 	}
 	
 	
 	// Retrieve all asteroids in database
 	public Cursor getAllAsteroids()
 	{
 		Log.w(DATABASE_TAG, "Running getAllAsteroids.");
 		return db.query(DATABASE_TABLE, new String[] {KEY_ROWID, KEY_OBJECTNAME, KEY_APPROACHDATE, KEY_APPROACHDATEUNIXTIME, KEY_APPROACHDISTANCE, KEY_MINDIAMETER, KEY_MAXDIAMETER, KEY_ABSOLUTEMAGNITUDE, KEY_RELATIVEVELOCITY}, null, null, null, null, null);
 	}
 	
 		
 	// Returns number of database entries as an int
 	// NOT TESTED YET
 	public int countAsteroids()
 	{
 		int numberOfAsteroids=0;
 			
 		if ( isDatabaseEmpty() == false ) {
 			Cursor Query = db.rawQuery("SELECT COUNT(*) FROM " + DATABASE_TABLE, null);
 			numberOfAsteroids=Query.getCount();
 			Query.close();
 			return numberOfAsteroids;
 		} else {
 			return 0;
 		}
 	}
 	
 	
 	
 	// Insert asteroid into database
 	public long insertAsteroid(String name, String approachDate, int approachDateUnixTime, double approachDistance, String approachTime, double minDiameter, double maxDiameter, double absoluteMagnitude, double relativeVelocity)
 	{
 		Log.w(DATABASE_TAG, "Inserting asteroid: " + name + " into database.");
 		ContentValues incomingValues = new ContentValues();
 	
 		incomingValues.put(KEY_OBJECTNAME, name);
 		incomingValues.put(KEY_APPROACHDATE, approachDate);
 		incomingValues.put(KEY_APPROACHDATEUNIXTIME, approachDateUnixTime);
 		incomingValues.put(KEY_APPROACHDISTANCE, approachDistance);
 		incomingValues.put(KEY_APPROACHTIME, approachTime);
 		incomingValues.put(KEY_MINDIAMETER, minDiameter);
 		incomingValues.put(KEY_MAXDIAMETER, maxDiameter);
 		incomingValues.put(KEY_ABSOLUTEMAGNITUDE, absoluteMagnitude);
 		incomingValues.put(KEY_RELATIVEVELOCITY, relativeVelocity);
 			
 		return db.insert(DATABASE_TABLE, null, incomingValues);
 	}
 	
 
 	// Retrieve asteroids for the next seven days
 	public Cursor getUpcomingAsteroids()
 	{
 		int UPCOMING_TIME_WINDOW = 604800;
 		Log.w(DATABASE_TAG, "Running getUpcomingAsteroids");
 		
 		// Get today's date
 		Calendar c = Calendar.getInstance();
 		int year = Calendar.getInstance().get(Calendar.YEAR);
 		int month = Calendar.getInstance().get(Calendar.MONTH);
 		int day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
 		c.set(year, month, day, 0, 0, 0);
 		
 		int unixTimeCurrent = (int) (c.getTimeInMillis() / 1000L);
 		int unixTimeUpcoming = unixTimeCurrent + UPCOMING_TIME_WINDOW;
 			
 		String str_unixTimeCurrent = String.valueOf(unixTimeCurrent);
 		String str_unixTimeUpcoming = String.valueOf(unixTimeUpcoming);
 			
 		return db.query(DATABASE_TABLE, new String[] {KEY_ROWID, KEY_OBJECTNAME, KEY_APPROACHDATE, KEY_APPROACHDATEUNIXTIME, KEY_APPROACHDISTANCE, KEY_MINDIAMETER, KEY_MAXDIAMETER, KEY_ABSOLUTEMAGNITUDE, KEY_RELATIVEVELOCITY}, KEY_APPROACHDATEUNIXTIME + " between ? and ?", new String[] {str_unixTimeCurrent, str_unixTimeUpcoming}, null, null, null, null);
 	}
 	
 	
 	// Delete asteroid from database using rowID
 	public boolean deleteAsteroid(int rowID)
 	{
 		Log.w(DATABASE_TAG, "Deleting asteroid with rowID " + rowID + " from database.");
 		return db.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowID, null) > 0;
 	}
 	
 	
 	// Check if asteroid with name s is in the database
 	public boolean isInDatabase(String s)
 	{
 		Log.w(DATABASE_TAG, "Checking if asteroid with name " + s + " exists in database.");
 		Cursor Query = db.query(true, DATABASE_TABLE, new String[] {KEY_ROWID}, KEY_OBJECTNAME + "=" + "?", new String[]{s}, null, null, null, null);
 		if ( Query.moveToFirst() ) {
 			Log.w(DATABASE_TAG, s + " exists in database.");
 			Query.close();
 			return true;
 		}
 		else {
 			Log.w(DATABASE_TAG, s + " doesn't exist in database.");
 			Query.close();
 			return false;
 		}
 	}
 	
 	
 	// Check if database has any entries
 	public boolean isDatabaseEmpty()
 	{
 		Log.w(DATABASE_TAG, "Checking if database is empty.");
 		Cursor Query = db.rawQuery("SELECT COUNT(*) FROM " + DATABASE_TABLE, null);
 		if (Query != null) {
 			Query.moveToFirst();
 		    if (Query.getInt(0) == 0) {
 		    	Log.w(DATABASE_TAG, "Database is empty.");
 		    	Query.close();
 		    	return true;
 		    } else {
 		    	Log.w(DATABASE_TAG, "Database is not empty");
 		    	Query.close();
 				return false;
 		    }
 		} else {
 			Log.w(DATABASE_TAG, "ERROR: Database cursor was null.");
 			return false;
 		}
 	}
 	
 	
 	// Check last entry's approach date to signify if an update is needed, also removes old entries
 	public boolean requireUpdate()
 	{
 		final int TIME_WINDOW = 2419200; // time difference in seconds to signify update (four weeks)
 		final int DEAD_ASTEROID = 86400; // time difference in seconds to remove an old asteroid (one day)
 		
 		Log.w(DATABASE_TAG, "Checking if last entry's approach date is within two weeks");
 		Cursor Query = db.rawQuery("SELECT " + KEY_ROWID + ", " + KEY_APPROACHDATEUNIXTIME + " from " + DATABASE_TABLE, null);
 		int rowID = 0;
 		int approachDateUnixTime = 0;
 		
 		// Get phone's timestamp
 		int unixTimeCurrent = (int) (System.currentTimeMillis() / 1000L);
 		
 		// Get rowID and approach timestamp of last entry in database (cycles through each entry)
 		if (Query.moveToFirst()){
 			do{
 				rowID = Query.getInt(Query.getColumnIndex(KEY_ROWID));
 				approachDateUnixTime = Query.getInt(Query.getColumnIndex(KEY_APPROACHDATEUNIXTIME));
 				
 				// Remove entries that are more than DEAD_ASTEROID old
 				if ( unixTimeCurrent >= approachDateUnixTime  ) {
 					int diff = unixTimeCurrent - approachDateUnixTime;
					if ( diff >= DEAD_ASTEROID ) {
 						// delete this asteroid row
 						boolean deleteRow = db.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowID, null) > 0;
 						if ( deleteRow == true ) {
 							Log.w(DATABASE_TAG, "Deleting old asteroid with rowID: " + rowID);
 						}
 					}
 				}
 				
 			} while (Query.moveToNext());
 			Log.w(DATABASE_TAG, "Last entry has rowID: " + rowID);
 			Log.w(DATABASE_TAG, "Approach date for last row: " + approachDateUnixTime);
 		}
 		
 		Log.w(DATABASE_TAG, "Current timestamp: " + unixTimeCurrent);
 		Log.w(DATABASE_TAG, "Asteroid timestamp: " + approachDateUnixTime);
 		
 		// If current time is greater than last asteroid's approach date then signify update required
 		if ( unixTimeCurrent >= approachDateUnixTime ) {
 			Log.w(DATABASE_TAG, "Current time is greater than or equal to asteroid time, update required");
 			Query.close();
 			return true;
 		} else {
 			// Otherwise calculate the difference
 			int diff = approachDateUnixTime - unixTimeCurrent;
 			// If the difference is within TIME_WINDOW then signify update required
 			if ( diff <= TIME_WINDOW ) {
 				Log.w(DATABASE_TAG, "Difference is within time window: " + diff + ". Update required.");
 				Query.close();
 				return true;
 			// else no update required
 			} else {
 				Log.w(DATABASE_TAG, "No update required");
 				Query.close();
 				return false;
 			}
 		}
 	}
 
 }
