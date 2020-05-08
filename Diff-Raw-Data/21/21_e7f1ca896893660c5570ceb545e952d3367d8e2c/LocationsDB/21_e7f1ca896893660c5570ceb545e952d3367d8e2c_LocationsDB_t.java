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
 
 import com.prosthetics.LocationsDBHelper;
 import com.prosthetics.LocationWrapper;
 
 import org.json.JSONObject;
 import org.json.JSONArray;
 
 /**
  * Helper class for accessing the locations db. It can be used to add a new 
  * record, check whether a specified record already exists, count number of 
  * records, or fetch records from the database.
  * @author jbanford, based on prior work by
  * @author Jakub Konka
  * @version 1.0
  * @see LocationsDBHelper
  */
 public class LocationsDB
 {
   /** Tag for Log statements in this class. */
   public final static String TAG = "LocationsDB";
   private static final boolean D = true;
 
   /**
    * Base directory holding the public contents of the app on the 
    * external memory card. The absolute path looks like:
    * /path_to_external_memory/BASE_DIR/
    */
   private final static String BASE_DIR = "ProstheticsMonitoringAppProject";
   /** The name of the interactions db file. */
   private final static String DB_NAME = "locations.sqlite3";
 
   private LocationsDBHelper dbHelper;
 
   /**
    * Constructs an object of type {@code LocationsDB}.
    * @param context The {@code Context} in which the {@code LocationsDB} object
    * was created.
    */
   public LocationsDB(Context context) 
   {
     Log.i(TAG, "Creating a handler for locations db.");
     String dbPath = LocationsDB.getDBPath() + "/" + DB_NAME;
     dbHelper = new LocationsDBHelper(context, dbPath);
   }
   
   /**
    * Adds a location to the location db.
    * @param location {@code LocationWrapper} object to be added
    */
  public synchronized void addLocation(LocationWrapper location)
   {
     // Add interaction to the db
     if(D) Log.i(TAG, "Adding record to locations table");
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
     } 
     catch (SQLException e)
     {
       Log.i(TAG, "Could not insert data into locations table: " + e);
     } 
     finally
     {
       Log.i(TAG, "Closing db...");
       if (db != null)
         db.close();
     }
   }
 
   /**
    * Returns the absolute path to the location db.
    * @return Absolute path to the location db.
    */
   private static String getDBPath()
   {
     if(D) Log.i(TAG, "getDBPath called.");
     String path = Environment.getExternalStorageDirectory().getPath() + "/" + BASE_DIR;
     if(D) Log.i(TAG, "Checking if " + path + " exists");
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
 
   public JSONArray getLatestLocations(int latestId)
   {
     Log.i(TAG, "Getting all locations");
     JSONArray locations = new JSONArray();
     SQLiteDatabase db = null;
     try
     {
       db = dbHelper.getReadableDatabase();
       Cursor c = db.query(
                            LocationsDBHelper.TABLE,
                            null,
                            LocationsDBHelper.ID + " > " + latestId,
                            null,
                            null,
                            null,
                            LocationsDBHelper.ID + " ASC"
                          );
       Log.i(TAG, "Fetched samples " + c.getCount() + " rows");
       while (c.moveToNext())
       {
         int id = c.getInt(0);
         String t = c.getString(1);
         String prov = c.getString(2);
         float lat = c.getFloat(3);
         float lon = c.getFloat(4);
         float acc = c.getFloat(5);
         JSONObject data = new JSONObject();
         try
         {
           data.put(LocationsDBHelper.ID, id);
           data.put(LocationsDBHelper.TIMESTAMP, t);
           data.put(LocationsDBHelper.PROVIDER, prov);
           data.put(LocationsDBHelper.LATITUDE, lat);
           data.put(LocationsDBHelper.LONGITUDE, lon);
           data.put(LocationsDBHelper.ACCURACY, acc);
           JSONObject location = new JSONObject();
           location.put("location", data);
           locations.put(location);
         }
         catch (org.json.JSONException e){} // Fix;Me: handle this exception
       }
      
       return locations;
       }
       finally 
       {
         if (db != null)
         db.close();
       }
   }
 }
