 package com.example.wsn03;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.util.Log;
 
 /**
  * 
  * @author Lothar Rubusch
  *
  */
 public class DataProvider{
 	private static String THIS = "DataProvider - ";
 	
 	// internal settings
 	private static Context _context;
 
 	// db settings
 	private static final int DATABASE_VERSION = 2;
 	private static final String DATABASE_NAME = "measurementmanager.db";
 // TODO improve solution?
 //	private static final String TABLE_BATTERYCAPACITY = "batterycapacity";
 	private static final String TABLE_BATTERYCAPACITY = "measurements";
 	private static final String TABLE_WIFICAPACITY = "wificapacity";
 	private static final String TABLE_3GCAPACITY = "3gcapacity";
 
 	// db col names
 	private static final String KEY_ID = "id";
 	private static final String KEY_TIMESTAMP = "timestamp";
 	private static final String KEY_VALUE = "value";
 
 	// db handler as priv class
 	public class DbHelper extends SQLiteOpenHelper {
 		DbHelper( Context context ){
 			super( context, DATABASE_NAME, null, DATABASE_VERSION);
 		}
 		
 		@Override
 		public void onCreate(SQLiteDatabase db) {
 			Log.d(MainActivity.TAG, "DataProvider::DbHelper::onCreate()" );
 			
 			// set up new table
 			String sql = 
 					"CREATE TABLE "
 					+ TABLE_BATTERYCAPACITY
 					+ "(" + KEY_ID   + " INTEGER PRIMARY KEY "
 					+ "," + KEY_TIMESTAMP + " LONG "
 					+ "," + KEY_VALUE + " INTEGER "
 					+ ");";
 			
 			db.execSQL( sql );
			
			// IMPORTANT: no db.close() here!
 		}
 
 		@Override
 		public void onUpgrade(SQLiteDatabase db, int old_version, int new_version) {
 			Log.w( MainActivity.TAG, "upgrade db from version " + old_version 
 					+ " to "                      + new_version 
 					+ ", which will destroy all old data");
 			db.execSQL( "DROP TABLE IF EXISTS " + TABLE_BATTERYCAPACITY);
 			onCreate( db );
 		}
 		
 		/*
 		 * CRUD funcs
 		 * C-reate
 		 * R-ead
 		 * U-update
 		 * D-elete
 		 */
 		public void addMeasurement( DataElement c ){
 			Log.d(MainActivity.TAG, "DataProvider::DbHelper::addMeasurement");
 			// writing Measurement obj
 			SQLiteDatabase db = this.getWritableDatabase();
 			ContentValues vals = new ContentValues();
 //			Log.d(MainActivity.TAG, "...::c.getTimestamp() " + c.getTimestamp()); // XXX
 			vals.put( KEY_TIMESTAMP, c.getTimestamp() );
 			vals.put( KEY_VALUE, c.getValue() );
 			db.insert( TABLE_BATTERYCAPACITY, null, vals);
 			db.close();
 		}
 
 		public DataElement getMeasurement( int id ){
 			// reading Measurement obj
 			SQLiteDatabase db = this.getReadableDatabase();
 			
 			// get cursor on query
 			Cursor cursor = db.query( TABLE_BATTERYCAPACITY
 					, new String[]{ KEY_ID, KEY_TIMESTAMP, KEY_VALUE }
 					, KEY_ID + "=?"
 					, new String[]{ String.valueOf(id) }
 					, null
 					, null
 					, null
 					, null );
 			if( null != cursor ){
 				cursor.moveToFirst();
 			}
 
 			// get result set
 			return new DataElement( Integer.parseInt( cursor.getString( 0 ) )
 					, Long.parseLong( cursor.getString( 1 ))
 					, Integer.parseInt( cursor.getString( 2 )) );
 		}	
 
 
 		public List<DataElement> getAllMeasurements(){
 			// return a list of all measurements ( = db entries )
 //			SQLiteDatabase db = this.getReadableDatabase();
 // FIXME: why writable??? why readable does not work?
 			SQLiteDatabase db = this.getWritableDatabase();
 
 			List<DataElement> measurementlist = new ArrayList<DataElement>();
 			String sql = "SELECT * FROM " + TABLE_BATTERYCAPACITY;
 			Cursor cursor = db.rawQuery( sql, null);
 
 			// iterate over returned list elems
 			if( cursor.moveToFirst() ){
 				do{
 					DataElement measurement = new DataElement();
 					measurement.setID( Integer.parseInt( cursor.getString(0) ) );
 					measurement.setTimestamp( Long.parseLong( cursor.getString(1)) );
 					measurement.setValue( Integer.parseInt( cursor.getString(2)) );
 					
 					measurementlist.add( measurement );
 				}while( cursor.moveToNext());
 			}
 			cursor.close();
 			
 			return measurementlist;
 		}
 
 		public int getMeasurementsCount(){
 			// total number of tables in db
 			SQLiteDatabase db = this.getReadableDatabase();
 
 			String sql = "SELECT * FROM " + TABLE_BATTERYCAPACITY;
 			Cursor cursor = db.rawQuery( sql, null );
 			int count = cursor.getCount(); 
 			cursor.close();
 
 			return count;
 		}
 		
 		public int updateMeasurement( DataElement c ){
 			// update db
 			SQLiteDatabase db = this.getWritableDatabase();
 
 			ContentValues vals = new ContentValues();
 			vals.put( KEY_TIMESTAMP, c.getTimestamp() );
 			vals.put( KEY_VALUE, c.getValue() );
 			
 			return db.update( TABLE_BATTERYCAPACITY
 					, vals
 					, KEY_ID + "=?"
 					, new String[]{ String.valueOf( c.getID() ) }
 			);
 		}
 
 		public void deleteMeasurement( int id ){
 			// delete a measurement
 			SQLiteDatabase db = this.getWritableDatabase();
 	
 			db.delete( TABLE_BATTERYCAPACITY
 					, KEY_ID + "=?"
 					, new String[]{ String.valueOf( id ) }
 			);
 			db.close();
 		}
 		
 		public void deleteTable( String table ){
 			SQLiteDatabase db = this.getWritableDatabase();
 //			db.execSQL("DELETE FROM " + table + ";");
 //			db.close();
 			
 //			db = this.getWritableDatabase();
 			db.delete( table, null, null);
 			db.close();
 		}
 		
 		public void deleteDatabase(){
 			getContext().deleteDatabase(DATABASE_NAME);
 		}
 	};
 
 	// db instance
 	private DbHelper db;
 	
 	// internal getter / setter
 	private Context getContext(){
 		return DataProvider.get_context();
 	}
 
 	/*
 	 * constructors
 	 */
 	
 	public DataProvider( Context c ){
 		Log.d(MainActivity.TAG, THIS + "DataProvider");
 		DataProvider.set_context(c);
 	}
 
 	// ContentProvider like functions
 	public boolean onCreate() {
 		// clean up first
 		db = new DbHelper( getContext() );
 		return true;
 	}
 
 	/*
 	 * ACCESSABILITY
 	 */
 
 // TODO keep it like this, or use derrived classes? functionality in common?
 	public void batterySave( Integer val ){
 		db.addMeasurement( new DataElement( System.currentTimeMillis(), val ));
 	}
 	
 	public List<DataElement> batteryData(){
 		return db.getAllMeasurements();
 	}
 	
 	public void batteryReset(){
 		db.deleteTable( TABLE_BATTERYCAPACITY );
 	}
 	
 	public void batteryDeleteId( int id ){
 		db.deleteMeasurement(id);
 	}
 	
 	public int count(){
 		return db.getMeasurementsCount();
 	}
 
 	/*
 	 * PUBLIC getter / setter, static and "provider"
 	 */
 	public static Context get_context() {
 		return _context;
 	}
 
 	public static void set_context(Context _context) {
 		DataProvider._context = _context;
 	}
 };
