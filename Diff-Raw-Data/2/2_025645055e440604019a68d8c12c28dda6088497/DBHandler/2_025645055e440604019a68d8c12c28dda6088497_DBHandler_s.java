 /**
  * For handling Sqlite operations
  */
 package com.example.co2emissionalert;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.example.co2emissionalert.MapTracking.LocEntry;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.location.Location;
 
 public class DBHandler extends SQLiteOpenHelper {
 
 	private static final String DATABASE_NAME = "mytrack.sqlite";  
     private static final int DATABASE_VERSION = 1;  
     private static final String TABLE_NAME = "trackdata";  
     public static final String KEY_ID = "id";  
     public static final String KEY_LOCALTIME = "localtime";  
     public static final String KEY_MODE = "coef";
     public static final String KEY_LATITUDE = "lati";
     public static final String KEY_LONGITUDE = "longi";
     public static final String KEY_ALTITUDE = "alti";
     public static final String KEY_SPEED = "speed";
     public static final String KEY_TIME = "time";
     public static final String KEY_DTIME = "dtime"; 
     public static final String KEY_DDISTANCE = "ddistance"; 
     public static final String KEY_DCO2 = "dCO2"; 
 	
 	
 	public DBHandler(Context context) {
         super(context, DATABASE_NAME, null, DATABASE_VERSION);
     }
 	
 	/* (non-Javadoc)
 	 * @see android.database.sqlite.SQLiteOpenHelper#onCreate(android.database.sqlite.SQLiteDatabase)
 	 */
 	@Override
 	public void onCreate(SQLiteDatabase db) {
 		// TODO Auto-generated method stub
 		String sql = "CREATE TABLE " + TABLE_NAME + "(" + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
 				+ KEY_LOCALTIME + " INTEGER UNIQUE , "+  KEY_MODE +" REAL, " 
 				+ KEY_LATITUDE + " REAL, " + KEY_LONGITUDE + " REAL, " + KEY_ALTITUDE + " REAL, "
 				+ KEY_SPEED + " REAL, " + KEY_TIME + " INTEGER, "
 				+ KEY_DTIME + " INTEGER, " + KEY_DDISTANCE + " REAL, " + KEY_DCO2 + " REAL);";  
 	    db.execSQL(sql);
 	}
 
 	/* (non-Javadoc)
 	 * @see android.database.sqlite.SQLiteOpenHelper#onUpgrade(android.database.sqlite.SQLiteDatabase, int, int)
 	 */
 	@Override
 	public void onUpgrade(SQLiteDatabase db, int arg1, int arg2) {
 		// TODO Auto-generated method stub
 		String sql = "DROP TABLE IF EXISTS " + TABLE_NAME;  
         db.execSQL(sql);  
         onCreate(db);
 	}
 	
 /*	public Cursor select() {  
         SQLiteDatabase db = this.getReadableDatabase();  
         Cursor cursor = db.query(TABLE_NAME, null, null, null, null, null, null);  
         return cursor;  
     }  
 */	
 		// insert an entry with full info
 	public long addLocEntry(LocEntry l) {  
 		SQLiteDatabase db = this.getWritableDatabase();  
 		/* ContentValues */  
 		ContentValues cv = new ContentValues();  
 		cv.put(KEY_LOCALTIME, l.getLocalTime());
 		cv.put(KEY_MODE, l.getCoef()); 
 		cv.put(KEY_LATITUDE, l.getLoca().getLatitude());
 		cv.put(KEY_LONGITUDE, l.getLoca().getLongitude());
 		cv.put(KEY_ALTITUDE, l.getLoca().getAltitude());
 		cv.put(KEY_SPEED, l.getLoca().getSpeed());
 		cv.put(KEY_TIME, l.getLoca().getTime());
 		cv.put(KEY_DTIME, l.getDT());
 		cv.put(KEY_DDISTANCE, l.getDD());
 		cv.put(KEY_DCO2, l.getDC());
 		long row = db.insert(TABLE_NAME, null, cv);  
 		db.close();
 		return row;  
     }  
     
 		// delete an entry on localtime
     public void delLocEntry(LocEntry l) {  
        SQLiteDatabase db = this.getWritableDatabase();  
        String where = KEY_LOCALTIME + " = ?";  
        String[] whereValue ={ Long.toString(l.getLocalTime()) };  
        db.delete(TABLE_NAME, where, whereValue);  
        db.close();
      }  
      
     // update transport mode for single entry (current  location): m value
     public void updateLocEntry(LocEntry l) {  
        SQLiteDatabase db = this.getWritableDatabase();  
        String where = KEY_LOCALTIME + " = ?";  
        String[] whereValue = { Long.toString(l.getLocalTime()) };  
        
        ContentValues cv = new ContentValues();  
        cv.put(KEY_MODE, l.getCoef());   
        db.update(TABLE_NAME, cv, where, whereValue);  
        db.close();
      }
     
     // Getting single LocEntry object
     LocEntry getLocEntry(long t) {
         SQLiteDatabase db = this.getReadableDatabase();
  
         //Cursor cursor = db.query(TABLE_NAME, new String[] { KEY_ID, KEY_LOCALTIME, KEY_MODE, KEY_ACCURACY, KEY_LATITUDE, KEY_LONGITUDE,
         //		KEY_ALTITUDE, KEY_BEARING, KEY_SPEED, KEY_TIME, KEY_DTIME, KEY_DDISTANCE, KEY_DCO2 }, KEY_LOCALTIME + "=?",
         //        new String[] { String.valueOf(t) }, null, null, null, null);
         String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + KEY_LOCALTIME + "=?"; 
         Cursor c = db.rawQuery(query, new String[] { String.valueOf(t) });
         
         Location loc = null;
         LocEntry l = new LocEntry();
         if(c.getCount() == 1) {
             c.moveToFirst();
             if(c.getColumnCount() == 11){
                 loc = new Location(MapTracking.seedLocation);
                 loc.setLatitude(c.getDouble(4));
                 loc.setLongitude(c.getDouble(5));
                 loc.setAltitude(c.getDouble(6));
                 loc.setSpeed(c.getFloat(8));
                 loc.setTime(c.getLong(9));
                 
                 l.setLoca(loc);
                 l.setLocalTime(c.getLong(1));
                 l.setCoef(c.getFloat(2));
                 l.setDT(c.getLong(10));
                 l.setDD(c.getFloat(11));
                 l.setDC(c.getFloat(12));
             }
         }
         
         
         //LocEntry l = new LocEntry(c.getLong(1), c.getFloat(2), loc, c.getLong(10), c.getFloat(11), c.getFloat(12));
         
         if(c != null && !c.isClosed()) {
             c.close();
         }
  
         // return LocEntry
         return l;
     }
  
     // Getting All Contacts
     public List<LocEntry> getAllEntries() {
         List<LocEntry> locList = new ArrayList<LocEntry>();
         // Select All Query
         String selectQuery = "SELECT  * FROM " + TABLE_NAME;
  
         SQLiteDatabase db = this.getReadableDatabase();
         Cursor c = db.rawQuery(selectQuery, null);
  
         // looping through all rows and adding to list
         if (c.moveToFirst()) {
             do {
                 LocEntry l = new LocEntry();
                 Location loc = new Location(MapTracking.seedLocation);
                 loc.setLatitude(c.getDouble(4));
                 loc.setLongitude(c.getDouble(5));
                 loc.setAltitude(c.getDouble(6));
                 loc.setSpeed(c.getFloat(8));
                 loc.setTime(c.getLong(9));
                 
                 l.setLoca(loc);
                 l.setLocalTime(c.getLong(1));
                 l.setCoef(c.getFloat(2));
                 l.setDT(c.getLong(10));
                 l.setDD(c.getFloat(11));
                 l.setDC(c.getFloat(12));
                 //LocEntry entry = new LocEntry(c.getLong(1), c.getFloat(2), loc, c.getLong(10), c.getFloat(11), c.getFloat(12));
                 // Adding locEntry to list
                 locList.add(l);
             } while (c.moveToNext());
         }
         c.close();
         // return contact list
         return locList;
     }
     
     // Getting modes
     public String getModes() {
         String countQuery = "SELECT DISTINCT " + KEY_MODE + " FROM " + TABLE_NAME + " WHERE " + KEY_LOCALTIME + ">= ?";
         SQLiteDatabase db = this.getReadableDatabase();
         Cursor c = db.rawQuery(countQuery, new String[] { String.valueOf(MapTracking.StartTime) });
         int nMode = c.getCount();
         float mValue;
         String result = String.valueOf(nMode) + " transport mode(s):";
         if (c.moveToFirst()) {
         	do{
         		mValue = c.getFloat(0);
         		result = result + " " + MapTracking.tellWhichMode(mValue);
         	}while(c.moveToNext());
         }
         c.close();
  
         return result;
     }
     
     
     	// get time array and co2 emission rate array from database
     public ArrayPair getArrays(){
     	Number [] array1;
     	Number [] array2;
     	
     	String countQuery = "SELECT " + KEY_LOCALTIME + ", " + KEY_DTIME + ", " + KEY_DCO2 + " FROM " + TABLE_NAME + " WHERE " + KEY_LOCALTIME + ">= ?";
         SQLiteDatabase db = this.getReadableDatabase();
         Cursor c = db.rawQuery(countQuery, new String[] { String.valueOf(MapTracking.StartTime) });
         int count = c.getCount();
         array1 = new Number[count];
         array2 = new Number[count];
         
         int i = 0;
         if (c.moveToFirst()) {
         	do{
         		array1[i] = c.getLong(0);
        		array2[i] = (c.getLong(1) != 0)? c.getFloat(2) : 0;
         		i += 1;
         	}while(c.moveToNext());
         }
         c.close();
         
     	return new ArrayPair(array1, array2);
     }
     
     	// defined for returning 2 Number[] to help plot in summary view
     public static class ArrayPair{
         private Number[] array1;
         private Number[] array2;
         public ArrayPair(Number[] array1, Number[] array2)
         {
             this.array1 = array1;
             this.array2 = array2;
 
         }
         public Number[] getArray1() { return array1; }
         public Number[] getArray2() { return array2; }
     }
 }
