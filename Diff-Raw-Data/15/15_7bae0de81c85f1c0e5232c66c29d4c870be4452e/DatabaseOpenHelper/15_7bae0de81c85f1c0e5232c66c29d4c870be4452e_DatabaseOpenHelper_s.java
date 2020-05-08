 /*
  * Copyright 2013 Friederike Wild, created 02.07.2013
  */
 package de.devmob.nomnom.data;
 
 import java.util.Arrays;
 import java.util.HashSet;
 
 import android.content.Context;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.provider.BaseColumns;
 
 /**
  * Database open helper to connect the ContentProvider to the database
  * 
  * @author Friederike Wild
  */
 public class DatabaseOpenHelper extends SQLiteOpenHelper
 {
     private static final String  DATABASE_NAME    = "nomnom_storage.db";
     private static final int     DATABASE_VERSION = 1;
 
     /** The table name to use */
     public static final String   TABLE_NAME       = "nomnoms";
 
     /** The id of the database entry */
     public static final String   COLUMN_ID        = BaseColumns._ID;
     /** The name of the place */
     public static final String   COLUMN_NAME      = "name";
     /** The google id, helpful if one wants to do a follow-up call to show details */
     public static final String   COLUMN_GID       = "googleid";
     /** The rating */
     public static final String   COLUMN_RATING    = "rating";
     /** The latitude of the position */
    public static final String   COLUMN_LAT       = "lat";
     /** The longitude of the position */
    public static final String   COLUMN_LNG       = "lng";
     
     /** List of all available columns for a projection of our database */
     public static final String[] PROJECTION_ALL   = 
        { COLUMN_ID, COLUMN_NAME, COLUMN_GID, COLUMN_RATING, COLUMN_LAT, COLUMN_LNG };
 
     /** Select all entries */
     public static final String   SQL_SELECT_ALL   = "SELECT * FROM " + TABLE_NAME;
     /** Create table */
     public static final String   SQL_CREATE       = "CREATE TABLE " + TABLE_NAME + " ("
                                                      + COLUMN_ID + " integer primary key autoincrement,"
                                                      + COLUMN_NAME + " text" + " not null, "
                                                      + COLUMN_GID + " text" + " not null, "
                                                     + COLUMN_RATING + " text" + " not null "
                                                     + COLUMN_LAT + " real" + " not null "
                                                     + COLUMN_LNG + " real" + " not null "
                                                      + ")";
     /** Drop the table */
     public static final String   SQL_DROP         = "DROP TABLE IF EXISTS " + TABLE_NAME;
 
     /** The default sort order to use for the table */
     public static final String DEFAULT_SORT_ORDER = COLUMN_ID + "DESC";
 
     /**
      * Simple constructor. 
      * No additional parameters cause all information is stored as constants in here.
      * @param context The activity context to create the database helper in.
      */
     public DatabaseOpenHelper(Context context)
     {
         super(context, DATABASE_NAME, null, DATABASE_VERSION);
     }
 
     /* (non-Javadoc)
      * @see android.database.sqlite.SQLiteOpenHelper#onCreate(android.database.sqlite.SQLiteDatabase)
      */
     @Override
     public void onCreate(SQLiteDatabase db)
     {
         db.execSQL(SQL_CREATE);
     }
 
     /* (non-Javadoc)
      * @see android.database.sqlite.SQLiteOpenHelper#onUpgrade(android.database.sqlite.SQLiteDatabase, int, int)
      */
     @Override
     public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
     {
         // Drop existing one
         db.execSQL(SQL_DROP);
         // Create in latest version
         onCreate(db);
     }
 
     /**
      * Method to check the availability of a given set of projection columns.
      * @param projection
      */
     protected static void checkColumns(String[] projection)
     {
         if (projection != null)
         {
             HashSet<String> requestedColumns = new HashSet<String>(Arrays.asList(projection));
             HashSet<String> availableColumns = new HashSet<String>(Arrays.asList(PROJECTION_ALL));
             // Check if requested columns are available
             if (!availableColumns.containsAll(requestedColumns))
             {
                 throw new IllegalArgumentException("Unknown columns in projection");
             }
         }
     }
 }
