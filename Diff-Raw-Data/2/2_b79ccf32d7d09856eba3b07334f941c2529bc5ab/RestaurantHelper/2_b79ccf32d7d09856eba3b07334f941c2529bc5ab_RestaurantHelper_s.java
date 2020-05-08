 package csci498.tonnguye.lunchlist;
 
 import android.content.Context;
 import android.content.ContentValues;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.database.sqlite.SQLiteDatabase;
 
 class RestaurantHelper extends SQLiteOpenHelper {
 	private static final String DATABASE_NAME = "lunchlist.db";
 	private static final int SCHEMA_VERSION = 3;
 	private static final String CREATE_TABLE = "CREATE TABLE restaurants (_id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, address TEXT, type TEXT, notes TEXT, feed TEXT, lat REAL, lon REAL);";
 	private static final String ALTER_TABLE_FEED = "ALTER TABLE restaurants ADD COLUMN feed TEXT";
 	private static final String ALTER_TABLE_LAT = "ALTER TABLE restaurants ADD COLUMN lat REAL";
 	private static final String ALTER_TABLE_LON = "ALTER TABLE restaurants ADD COLUMN lon REAL";
	private static final String SELECT_ORDER_BY = "SELECT _id, name, address, type, notes, feed FROM restaurants ORDER BY ";
 	private static final String SELECT_WHERE = "SELECT _id, name, address, type, notes, feed, lat, lon, FROM restaurants WHERE _ID=?";
 	
 	public RestaurantHelper(Context context) {
 		super(context, DATABASE_NAME, null, SCHEMA_VERSION);
 	}
 	
 	@Override
 	public void onCreate(SQLiteDatabase db) {
 		db.execSQL(CREATE_TABLE);
 	}
 	
 	@Override
 	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 		if (oldVersion < 2) {
 			db.execSQL(ALTER_TABLE_FEED);
 		}
 		
 		if (oldVersion < 3) {
 			db.execSQL(ALTER_TABLE_LAT);
 			db.execSQL(ALTER_TABLE_LON);
 		}
 	}
 	
 	public Cursor getAll(String orderBy) {
 		return(getReadableDatabase().rawQuery(SELECT_ORDER_BY + orderBy, null));
 	}
 
 	public Cursor getById(String id) {
 		String[] args = {id};
 		return(getReadableDatabase().rawQuery(SELECT_WHERE, args));
 	}
 	
 	public void updateLocation(String id, double lat, double lon) {
 		ContentValues cv = new ContentValues();
 		String[] args = {id};
 		
 		cv.put("lat", lat);
 		cv.put("lon", lon);
 		
 		getWritableDatabase().update("restaurants", cv, "_ID=?", args);
 	}
 	
 	public void insert(String name, String address, String type, String notes, String feed) {
 		ContentValues cv=new ContentValues();
 		
 		cv.put("name", name);
 		cv.put("address", address);
 		cv.put("type", type);
 		cv.put("notes", notes);
 		cv.put("feed", feed);
 		
 		getWritableDatabase().insert("restaurants", "name", cv);
 	}
 	
 	public void update(String id, String name, String address, String type, String notes, String feed) {
 		ContentValues cv=new ContentValues();
 		String[] args={id};
 		
 		cv.put("name", name);
 		cv.put("address", address);
 		cv.put("type", type);
 		cv.put("notes", notes);
 		cv.put("feed", feed);
 		
 		getWritableDatabase().update("restaurants", cv, "_ID=?", args);
 	}
 	
 	public String getFeed(Cursor c) {
 		return(c.getString(5));
 	}
 	public String getName(Cursor c) {
 		return(c.getString(1));
 	}
 	public String getAddress(Cursor c) {
 		return(c.getString(2));
 	}
 	public String getType(Cursor c) {
 		return(c.getString(3));
 	}
 	public String getNotes(Cursor c) {
 		return(c.getString(4));
 	}
 	public double getLatitude(Cursor c) {
 		return c.getDouble(6);
 	}
 	public double getLongitude(Cursor c) {
 		return c.getDouble(7);
 	}
 }
