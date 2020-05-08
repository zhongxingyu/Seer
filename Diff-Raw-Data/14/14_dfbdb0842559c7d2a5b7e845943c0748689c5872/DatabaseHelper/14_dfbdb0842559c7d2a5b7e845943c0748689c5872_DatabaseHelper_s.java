 package it.unipi.db;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.util.Log;
 import it.unipi.models.Forecast;
 import java.util.*;
 
 public class DatabaseHelper extends SQLiteOpenHelper {
 
 	private static final String DATABASE_NAME = "forescastdb";
 	private static final String FORECAST_TABLE = "forecasts";
 	private static final String FORECAST_ID = "id";
 	private static final String FORECAST_DATETIME = "datetime";
 	private static final String FORECAST_TEMPERATURE = "temperature";
 	private static final String FORECAST_DESCRIPTION = "description";
 
 	private static final int DATABASE_VERSION = 3;
 	private static final String FORECAST_TABLE_CREATE =
 				"CREATE TABLE " + FORECAST_TABLE + " (" +
         FORECAST_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
 				FORECAST_DATETIME + " INTEGER UNIQUE, " +
         FORECAST_TEMPERATURE + " INTEGER, " +
 				FORECAST_DESCRIPTION + " TEXT);";
 
 
     public DatabaseHelper( Context context ) {
         super( context, DATABASE_NAME, null, DATABASE_VERSION );
     }
 
     @Override
     public void onCreate(SQLiteDatabase db) {
         db.execSQL( FORECAST_TABLE_CREATE );
         Log.d( DATABASE_NAME, "Created!" );
 	}
 	
     public void resetDatabase() {
         Log.d( DATABASE_NAME, "Deleting all data!" );
         SQLiteDatabase db = getWritableDatabase();
         db.delete( FORECAST_TABLE, null, null );
     }
 
     @Override
     public void onUpgrade( SQLiteDatabase db, int oldVersion, int newVersion ) {
         Log.w( DATABASE_NAME, "Upgrading database from version " + oldVersion + " to "
                + newVersion + ", which will destroy all old data" );
         db.execSQL( "DROP TABLE IF EXISTS " + FORECAST_TABLE + ";" );
         db.execSQL( FORECAST_TABLE_CREATE );
     }
 
     public long insertRow( Forecast f ) {
         long rowId = 0;
         ContentValues val = new ContentValues();
         SQLiteDatabase db = getWritableDatabase();
 
         if ( f == null )
             throw new IllegalArgumentException("insertRow() No null args here!");
 
         val.put( FORECAST_DATETIME, f.getTimestamp() );
         val.put( FORECAST_TEMPERATURE, f.getTemperature() );
         val.put( FORECAST_DESCRIPTION, f.getDescription() );
 
         rowId = db.insertWithOnConflict( FORECAST_TABLE, null, val, SQLiteDatabase.CONFLICT_REPLACE);
 
         if ( rowId <= 0 )
             Log.e( DATABASE_NAME, "Row not created: " + f.getDatetimeString() );
 
         db.close();
         return rowId;
     }
 
     public List<Forecast> getForecasts( Date from ) {
         SQLiteDatabase db = getReadableDatabase();
 
         String[] PROJECTION_D = { FORECAST_DATETIME, FORECAST_DESCRIPTION, FORECAST_TEMPERATURE };
         String SELECTION_W = FORECAST_DATETIME + " >= " + from.getTime();
         String DEFAULT_SORT_ORDER = FORECAST_DATETIME + " ASC";
 
         Cursor c = db.query( FORECAST_TABLE, PROJECTION_D, SELECTION_W,
                              null, null, null,  DEFAULT_SORT_ORDER);
         Log.d(DATABASE_NAME, "Selection gave " + c.getCount() + " rows");
 
         List<Forecast> aForecasts = new ArrayList<Forecast>();
         if (!c.moveToFirst()) {
             c.close();
             return null;
         }
         do {
            Forecast f = new Forecast( c.getInt(c.getColumnIndexOrThrow(FORECAST_DATETIME)),
                                        c.getInt(c.getColumnIndexOrThrow(FORECAST_TEMPERATURE)),
                                        c.getString(c.getColumnIndexOrThrow(FORECAST_DESCRIPTION)) );
             aForecasts.add( f );
         } while( c.moveToNext() );
         c.close();
         db.close();
 
         return aForecasts;
     }
 }
