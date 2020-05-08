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
 	private String THIS = "DataProvider";
 
 	// db settings
 	private static final int DATABASE_VERSION = 2;
 	private static final String DATABASE_NAME = "wsnproject.db";
 
 	/*
 	 * if table name has changed, make sure to have cleanly uninstalled the app 
 	 * before the next installation, remaining DB artefarkts may break the
 	 * program, if not
 	 */
 
 	// db col names
 	private static final String KEY_ID = "id";
 	private static final String KEY_TIMESTAMP = "timestamp";
 // TODO decide on case basis, which values are necessary, and 
 //	in case shift to derived classes 
 	private static final String KEY_VALUE = "value";
 
 	// db handler as priv class
 	protected class DbHelper extends SQLiteOpenHelper {
 		private long initialMillis;
 		private String _tablename="";
 
 		DbHelper( Context context, String tablename ){
 			super( context, DATABASE_NAME, null, DATABASE_VERSION);
 			this._tablename = tablename;
 		}
 		
 		@Override
 		public void onCreate(SQLiteDatabase db) {
 			Log.d(MainActivity.TAG, THIS + "::DbHelper::onCreate()" );
 			
 			if( 0 == this._tablename.length() ){
 				Log.e(MainActivity.TAG, THIS + "DbHelper::onCreate() - no tablename provided");
 				throw new RuntimeException();
 			}
 
 			// set up new table, if not existed before
 			String sql = 
					"CREATE TABLE IF NOT EXISTS "
 					+ this._tablename
 					+ "(" + KEY_ID   + " INTEGER PRIMARY KEY "
 					+ "," + KEY_TIMESTAMP + " LONG "
 					+ "," + KEY_VALUE + " INTEGER "
					+ ")";
 			
 			db.execSQL( sql );
 			
 			// IMPORTANT: no db.close() here!
 		}
 
 		@Override
 		public void onUpgrade(SQLiteDatabase db, int old_version, int new_version) {
 			Log.w( MainActivity.TAG, "upgrade db from version " + old_version 
 					+ " to "                      + new_version 
 					+ ", which will destroy all old data");
 			db.execSQL( "DROP TABLE IF EXISTS " + this._tablename);
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
 //			Log.d(MainActivity.TAG, THIS + "::DbHelper::addMeasurement");
 			// writing Measurement obj
 			SQLiteDatabase db = this.getWritableDatabase();
 			ContentValues vals = new ContentValues();
 			
 			// c.getTimestamp()
 			long ts = c.getTimestamp();
 			if( 0 == this.getMeasurementsCount() ){
 				this.initialMillis = ts;
 			}
 			ts -= this.initialMillis;
 			
 			vals.put( KEY_TIMESTAMP, ts );
 			vals.put( KEY_VALUE, c.getValue() );
 			db.insert( this._tablename, null, vals);
 			db.close();
 		}
 
 		public DataElement getMeasurement( int id ){
 			// reading Measurement obj
 			SQLiteDatabase db = this.getReadableDatabase();
 			
 			// get cursor on query
 			Cursor cursor = db.query( this._tablename
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
 
 
 		public List<DataElement> getAllMeasurements(String tablename){
 			// return a list of all measurements ( = db entries )
 //			SQLiteDatabase db = this.getReadableDatabase();
 // FIXME: why writable??? why readable does not work?
 			SQLiteDatabase db = this.getWritableDatabase();
 
 			List<DataElement> measurementlist = new ArrayList<DataElement>();
 			String sql = "SELECT * FROM " + tablename;
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
 			String sql = "SELECT * FROM " + this._tablename;
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
 			
 			return db.update( this._tablename
 					, vals
 					, KEY_ID + "=?"
 					, new String[]{ String.valueOf( c.getID() ) }
 			);
 		}
 
 		public void deleteMeasurement( int id ){
 			// delete a measurement
 			SQLiteDatabase db = this.getWritableDatabase();
 	
 			db.delete( this._tablename
 					, KEY_ID + "=?"
 					, new String[]{ String.valueOf( id ) }
 			);
 			db.close();
 		}
 		
 		public void deleteTable( String table ){
 			SQLiteDatabase db = this.getWritableDatabase();
 			db.delete( table, null, null);
 			db.close();
 		}
 	};
 	
 	// db instance
 	protected DbHelper db;
 };
 
