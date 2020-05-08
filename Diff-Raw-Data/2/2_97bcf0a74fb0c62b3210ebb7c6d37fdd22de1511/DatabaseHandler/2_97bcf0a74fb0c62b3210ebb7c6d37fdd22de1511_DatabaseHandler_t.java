 package edu.umbc.teamawesome.assignment2;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.util.Log;
 
 public class DatabaseHandler extends SQLiteOpenHelper {
 	/* http://www.androidhive.info/2011/11/android-sqlite-database-tutorial/ */
 	
    private static final int DATABASE_VERSION = 2;
     private static final String DATABASE_NAME = "assignment2";
     private static final String TABLE_PINFO = "pinfo";
     
     private static final String KEY_ID = "id";
     private static final String KEY_TIME =  "time";	
     private static final String KEY_LONGITUDE = "longitude";
     private static final String KEY_LATITUDE = "latitude";	
     private static final String KEY_ACCEL_X = "accel_x";
     private static final String KEY_ACCEL_Y = "accel_y";
     private static final String KEY_ACCEL_Z = "accel_z";	
     private static final String KEY_ORIENT_X = "orient_x";
     private static final String KEY_ORIENT_Y = "orient_y";
     private static final String KEY_ORIENT_Z = "orient_z";	
     private static final String KEY_LX = "lx";
     private static final String KEY_PROX = "prox";
     private static final String KEY_ACTIVITY = "activity";
     
     public DatabaseHandler(Context context) {
     	super(context, DATABASE_NAME, null, DATABASE_VERSION);
     }
 
 	@Override
 	public void onCreate(SQLiteDatabase db) {
 		String CREATE_PINFO_TABLE = "CREATE TABLE " + TABLE_PINFO + " ("
 				+ KEY_ID + " INTEGER PRIMARY KEY,"
 				+ KEY_TIME + " INTEGER,"
 				+ KEY_LONGITUDE + " REAL,"
 				+ KEY_LATITUDE + " REAL,"
 				+ KEY_ACCEL_X + " REAL,"
 				+ KEY_ACCEL_Y + " REAL,"
 				+ KEY_ACCEL_Z + " REAL,"
 				+ KEY_ORIENT_X + " REAL,"
 				+ KEY_ORIENT_Y + " REAL,"
 				+ KEY_ORIENT_Z + " REAL,"
 				+ KEY_LX + " REAL,"
 				+ KEY_PROX + " REAL,"
 				+ KEY_ACTIVITY + " TEXT" + ")";
 		db.execSQL(CREATE_PINFO_TABLE);
 	}
 
 	@Override
 	public void onUpgrade(SQLiteDatabase db, int oldVer, int newVer) {
 		// will simply delete the table ... this is fine
 		db.execSQL("DROP TABLE IF EXISTS " + TABLE_PINFO);
 		onCreate(db);
 	}
 	
 	public void addEntry(PinInformation pinfo) {
 		SQLiteDatabase db = this.getWritableDatabase();
 		
 		ContentValues values = new ContentValues();
 	    values.put(KEY_TIME, pinfo.getTime());	
 	    values.put(KEY_LONGITUDE, pinfo.getLongitude());
 	    values.put(KEY_LATITUDE, pinfo.getLatitude());
 	    values.put(KEY_ACCEL_X, pinfo.getAccel_x());
 	    values.put(KEY_ACCEL_Y, pinfo.getAccel_y());
 	    values.put(KEY_ACCEL_Z, pinfo.getAccel_z());
 	    values.put(KEY_ORIENT_X, pinfo.getOrient_x());
 	    values.put(KEY_ORIENT_Y, pinfo.getOrient_y());
 	    values.put(KEY_ORIENT_Z, pinfo.getOrient_z());
 	    values.put(KEY_LX, pinfo.getLx());
 	    values.put(KEY_PROX, pinfo.getProx());
 	    values.put(KEY_ACTIVITY, pinfo.getActivity());
 	    
 	    db.insert(TABLE_PINFO, null, values);
 	    db.close();
 	    
 	    Log.d("Datbase", "Pin added at " + pinfo.getTime());
 	}
 	
 	public PinInformation getPin(int id) {
 		SQLiteDatabase db = this.getReadableDatabase();
 		
 		Cursor cursor = db.query(TABLE_PINFO, new String[] {
 				KEY_ID, KEY_TIME, KEY_LONGITUDE, KEY_LATITUDE, KEY_ACCEL_X, KEY_ACCEL_Y,
 				KEY_ACCEL_Z, KEY_ORIENT_X, KEY_ORIENT_Y, KEY_ORIENT_Z, KEY_LX, KEY_PROX, KEY_ACTIVITY
 			}, KEY_ID + "=?", new String[] { String.valueOf(id) }, null, null, null, null);
 		if (cursor != null)
 			cursor.moveToFirst();
 		return getPinFromCursor(cursor);
 	}
 	
 	public List<PinInformation> getAllPins() {
 		List<PinInformation> pins = new ArrayList<PinInformation>();
 		String select = "SELECT * FROM " + TABLE_PINFO;
 		SQLiteDatabase db = this.getWritableDatabase();
 		Cursor cursor = db.rawQuery(select, null);
 		
 		if (cursor.moveToFirst()) {
 			do {
 				pins.add(getPinFromCursor(cursor));
 			} while (cursor.moveToNext());
 		}
 		
 		return pins;
 	}
 	
 	public PinInformation getPinFromCursor(Cursor cursor) {		
 		PinInformation pinfo = new PinInformation();
 		pinfo.setId(cursor.getInt(0));
 		pinfo.setTime(cursor.getLong(1));
 		pinfo.setLongitude(cursor.getFloat(2));
 		pinfo.setLatitude(cursor.getFloat(3));
 		pinfo.setAccel_x(cursor.getFloat(4));
 		pinfo.setAccel_y(cursor.getFloat(5));
 		pinfo.setAccel_z(cursor.getFloat(6));
 		pinfo.setOrient_x(cursor.getFloat(7));
 		pinfo.setOrient_y(cursor.getFloat(8));
 		pinfo.setOrient_z(cursor.getFloat(9));
 		pinfo.setLx(cursor.getFloat(10));
 		pinfo.setLx(cursor.getFloat(11));
 		pinfo.setActivity(cursor.getString(12));
 		return pinfo;		
 	}
 	
 	public int getPinCount() {
 		String count = "SELECT * FROM " + TABLE_PINFO;
 		SQLiteDatabase db = this.getReadableDatabase();
 		Cursor cursor = db.rawQuery(count, null);		
 		return cursor.getCount();
 	}
 	
 	public void deletePin(PinInformation pin) {
 		SQLiteDatabase db = this.getWritableDatabase();
 		db.delete(TABLE_PINFO, KEY_ID + " = ?", new String[] { String.valueOf(pin.getId()) } );
 		db.close();
 	}
 	
 	public void updatePin(PinInformation pinfo) {
 		SQLiteDatabase db = this.getWritableDatabase();
 		
 		ContentValues values = new ContentValues();
 	    values.put(KEY_TIME, pinfo.getTime());	
 	    values.put(KEY_LONGITUDE, pinfo.getLongitude());
 	    values.put(KEY_LATITUDE, pinfo.getLatitude());
 	    values.put(KEY_ACCEL_X, pinfo.getAccel_x());
 	    values.put(KEY_ACCEL_Y, pinfo.getAccel_y());
 	    values.put(KEY_ACCEL_Z, pinfo.getAccel_z());
 	    values.put(KEY_ORIENT_X, pinfo.getOrient_x());
 	    values.put(KEY_ORIENT_Y, pinfo.getOrient_y());
 	    values.put(KEY_ORIENT_Z, pinfo.getOrient_z());
 	    values.put(KEY_LX, pinfo.getLx());
 	    values.put(KEY_PROX, pinfo.getProx());
 	    values.put(KEY_ACTIVITY, pinfo.getActivity());
 	    
 	    db.update(TABLE_PINFO, values, KEY_ID + " = ?",
 	            new String[] { String.valueOf(pinfo.getId()) });
 	}
 	
 	public void clearAll() {
 		SQLiteDatabase db = this.getWritableDatabase();
 		// will simply delete the table ... this is fine
 		db.execSQL("DROP TABLE IF EXISTS " + TABLE_PINFO);
 		onCreate(db);
 	}
 
 }
