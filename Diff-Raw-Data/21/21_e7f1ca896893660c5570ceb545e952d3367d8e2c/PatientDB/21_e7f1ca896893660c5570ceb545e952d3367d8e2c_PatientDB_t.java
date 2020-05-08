 package com.prosthetics;
 
 import java.io.IOException;
 import java.io.File;
 import java.io.StringWriter;
 import java.io.Writer;
 import java.io.PrintWriter;
 
 import android.util.Log;
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.SQLException;
 import android.os.Environment;
 
 import com.prosthetics.PatientDBHelper;
 import com.prosthetics.AccelerometerWrapper;
 import com.prosthetics.TemperatureWrapper;
 
 import org.json.JSONObject;
 import org.json.JSONArray;
 
 /**
  * Helper class for accessing the accelerometer db. It can be used to add a new 
  * record, check whether a specified record already exists, count number of 
  * records, or fetch records from the database.
  * @author aumar11 and jbanford, based on prior work by
  * @author Jakub Konka
  * @version 1.0
  * @see PatientDBHelper
  */
 public class PatientDB
 {
   /** Tag for Log statements in this class. */
   public final static String TAG = "PatientDB";
   private static final boolean D = true;
   /**
    * Base directory holding the public contents of the app on the 
    * external memory card. The absolute path looks like:
    * /path_to_external_memory/BASE_DIR/
    */
   private final static String BASE_DIR = "ProstheticsMonitoringAppProject";
   /** The name of the db. */
   private final static String DB_NAME = "patient.sqlite3";
   private PatientDBHelper dbHelper;
 
   /**
    * Constructs an object of type {@code PatientDB}.
    * @param context The {@code Context} in which the {@code PatientDB} object
    * was created.
    */
   public PatientDB(Context context) 
   {
     if (D) Log.i(TAG, "Creating a handler for accelerometer db.");
     String dbPath = PatientDB.getDBPath() + "/" + DB_NAME;
     dbHelper = new PatientDBHelper(context, dbPath);
   }
   
   /**
    * Adds an accelerometer sample to the patient db.
    * @param acceleromter {@code AccelerometerWrapper} object to be added
    */
  public synchronized void addAccelerometerSample(AccelerometerWrapper accelerometer)
   {
     // Add interaction to the db
     if (D) Log.i(TAG, "Adding record to accelerometer table");
 
     SQLiteDatabase db = null;
     try
     {
       db = dbHelper.getWritableDatabase();
       ContentValues values = new ContentValues();
       values.put(PatientDBHelper.TIMESTAMP, accelerometer.getTimestamp());
       values.put(PatientDBHelper.X_AXIS, accelerometer.getX());
       values.put(PatientDBHelper.Y_AXIS, accelerometer.getY());
       values.put(PatientDBHelper.Z_AXIS, accelerometer.getZ());
       db.insertOrThrow(PatientDBHelper.ACC_TABLE, PatientDBHelper.TIMESTAMP, values);
       if (D) Log.i(TAG, "Value has been inserted");
 
     } 
     catch (SQLException e)
     {
       Log.i(TAG, "Could not insert data into accelerometer table: " + e);
     } 
     finally
     {
       if (D) Log.i(TAG, "Closing db...");
       if (db != null)
         db.close();
     }
   }
 
   /**
    * Adds the temperature that was measured to the database.
    * @param temperature {@code TemperatureWrapper} object to be added
    */
  public synchronized void addTemperatureSample(TemperatureWrapper temperature)
   {
     // Add interaction to the db
     if (D) Log.i(TAG, "Adding record to temperature table");
 
     SQLiteDatabase db = null;
     try
     {
       db = dbHelper.getWritableDatabase();
       ContentValues values = new ContentValues();
       values.put(PatientDBHelper.TIMESTAMP, temperature.getTimestamp());
       values.put(PatientDBHelper.VALUE1, temperature.getValue1());
       values.put(PatientDBHelper.VALUE2, temperature.getValue2());
       db.insertOrThrow(PatientDBHelper.TEM_TABLE, PatientDBHelper.TIMESTAMP, values);
       if (D) Log.i(TAG, "Value has been inserted");
     } 
     catch (SQLException e)
     {
       Log.i(TAG, "Could not insert data into temperature table: " + e);
     } 
     finally
     {
       if (db != null)
         db.close();
     }
   }
 
     /**
    * Adds a location to the location db.
    * @param location {@code LocationWrapper} object to be added
    */
  public synchronized void addLocation(LocationWrapper location)
   {
     // Add interaction to the db
     Log.i(TAG, "Adding record to locations table");
     SQLiteDatabase db = null;
     try
     {
       db = dbHelper.getWritableDatabase();
       ContentValues values = new ContentValues();
       values.put(LocationsDBHelper.TIMESTAMP, location.getTimestamp());
       values.put(LocationsDBHelper.PROVIDER, location.getProvider());
       values.put(LocationsDBHelper.LATITUDE, location.getLatitude());
       values.put(LocationsDBHelper.LONGITUDE, location.getLongitude());
       values.put(LocationsDBHelper.ACCURACY, location.getAccuracy());
       db.insertOrThrow(LocationsDBHelper.TABLE, LocationsDBHelper.TIMESTAMP, values);      
       Log.i(TAG, "Location has been inserted");
     } 
     catch (SQLException e)
     {
       Log.i(TAG, "Could not insert data into locations table: " + e);
     } 
     finally
     {
       if (D) Log.i(TAG, "Closing db...");
       if (db != null)
         db.close();
     }
   }
 
   /**
    * Returns the absolute path to the accelerometer db.
    * @return Absolute path to the accelerometer db.
    */
   private static String getDBPath()
   {
     if (D) Log.i(TAG, "getDBPath called.");
     String path = Environment.getExternalStorageDirectory().getPath() + "/" + BASE_DIR;
     if (D) Log.i(TAG, "Checking if " + path + " exists");
     File dbDir = new File(path);
     if (!dbDir.isDirectory())
     {
       try
       {
         Log.i(TAG, "Trying to create " + path);
         dbDir.mkdirs();
       } 
       catch (Exception e)
       {
         final Writer result = new StringWriter();
         final PrintWriter printWriter = new PrintWriter(result);
         e.printStackTrace(printWriter);
         Log.i(TAG, result.toString());
       }
     }
     return path;
   }
 }
