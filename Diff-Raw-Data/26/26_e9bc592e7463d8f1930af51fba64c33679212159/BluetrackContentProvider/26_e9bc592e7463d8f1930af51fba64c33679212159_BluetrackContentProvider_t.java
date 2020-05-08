 //*****************************************************************************
 // This file is part of bluetrack.
 //
 // bluetrack is free software: you can redistribute it and/or modify
 // it under the terms of the GNU General Public License as published by
 // the Free Software Foundation, either version 3 of the License, or
 // (at your option) any later version.
 //
 // bluetrack is distributed in the hope that it will be useful,
 // but WITHOUT ANY WARRANTY; without even the implied warranty of
 // MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 // GNU General Public License for more details.
 //
 // You should have received a copy of the GNU General Public License
 // along with bluetrack.  If not, see <http://www.gnu.org/licenses/>.
 //*****************************************************************************
 
 package org.jonblack.bluetrack.storage;
 
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Map;
 
 import android.content.ContentProvider;
 import android.content.ContentUris;
 import android.content.ContentValues;
 import android.content.UriMatcher;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.database.sqlite.SQLiteQueryBuilder;
 import android.net.Uri;
 import android.util.Log;
 
 /**
  * ContentProvider for Bluetrack.
  * 
  * TODO: This class should be cleaned up. I'm not pleased with the hackish SQL
  *       statments. Replace with an ORM library when it really gets too much.
  */
 public class BluetrackContentProvider extends ContentProvider
 {
   private static final String TAG = "BluetrackContentProvider";
   
   public static final String AUTHORITY = "org.jonblack.bluetrack.storage.BluetrackContentProvider";
   private static final UriMatcher sUriMatcher;
   
   // URI's
   private static final int DEVICE       = 1;
   private static final int DEVICE_ID    = 2;
   private static final int DISCOVERY    = 3;
   private static final int DISCOVERY_ID = 4;
   private static final int SESSION      = 5;
   private static final int SESSION_ID   = 6;
   
   private BluetrackOpenHelper mOpenHelper;
   
   @Override
   public boolean onCreate()
   {
     mOpenHelper = new BluetrackOpenHelper(getContext());
     return true;
   }
 
   @Override
   public Cursor query(Uri uri, String[] projection, String selection,
                       String[] selectionArgs, String sortOrder)
   {
     // Warn if the projection is null, which is inefficient.
     if (projection == null)
     {
       Log.w(TAG, "Querying all columns is inefficient.");
     }
     
     SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
     
     switch (sUriMatcher.match(uri))
     {
     case DEVICE:
       qb.setTables(DeviceTable.TABLE_NAME);
       break;
     case DEVICE_ID:
       qb.setTables(DeviceTable.TABLE_NAME);
       selection = selection + DeviceTable.COL_ID + " = " + uri.getLastPathSegment();
       break;
     case DISCOVERY:
       // Shows the most recent devices discovered.
       // select * from device_discovery
       // where date_time = (select max(date_time) from device_discovery)
       // and session_id = 2;
       
       Map<String, String> columnMap = new HashMap<String, String>();
       columnMap.put("_id", "device._id");
       columnMap.put("name", "device.name");
       columnMap.put("mac_address", "device.mac_address");
      columnMap.put("rssi", "device_discovery.rssi");
       qb.setProjectionMap(columnMap);
       qb.appendWhere(DeviceDiscoveryTable.TABLE_NAME + ".date_time = (" +
                      "select max(date_time) from " +
                      DeviceDiscoveryTable.TABLE_NAME + ")");
       qb.setTables(DeviceTable.TABLE_NAME +
                    " LEFT JOIN " + DeviceDiscoveryTable.TABLE_NAME + 
                    " ON " +
                    DeviceDiscoveryTable.TABLE_NAME + ".device_id = " +
                    DeviceTable.TABLE_NAME + "." + DeviceTable.COL_ID);
       qb.setDistinct(true);
       break;
     case DISCOVERY_ID:
       qb.setTables(DeviceDiscoveryTable.TABLE_NAME);
       selection = selection + DeviceDiscoveryTable.COL_ID + " = " + uri.getLastPathSegment();
       break;
     case SESSION:
       qb.setTables(SessionTable.TABLE_NAME);
       break;
     case SESSION_ID:
       qb.setTables(SessionTable.TABLE_NAME);
       selection = selection + SessionTable.COL_ID + " = " + uri.getLastPathSegment();
       break;
     default:
       throw new IllegalArgumentException("Unknown URI: " + uri);
     }
     
     String sql = qb.buildQuery(projection, selection, null, null, sortOrder,
                                null);
     Log.v(TAG, "SQL: " + sql);
     
     Log.d(TAG, String.format("Quering '%s' where '%s' with args '%s'",
                              qb.getTables(), selection,
                              Arrays.toString(selectionArgs)));
     
     SQLiteDatabase db = mOpenHelper.getReadableDatabase();
     Cursor c = qb.query(db, projection, selection, selectionArgs, null, null,
                         sortOrder);
     c.setNotificationUri(getContext().getContentResolver(), uri);
     return c;
   }
 
   @Override
   public String getType(Uri uri)
   {
     String contentType = null;
     
     switch (sUriMatcher.match(uri))
     {
     case DEVICE:
       contentType = "vnd.android.cursor.dir/vnd.org.jonblack.provider." +
                     DeviceTable.TABLE_NAME;
       break;
     case DEVICE_ID:
       contentType = "vnd.android.cursor.item/vnd.org.jonblack.provider." +
                     DeviceTable.TABLE_NAME;
       break;
     case DISCOVERY:
       contentType = "vnd.android.cursor.dir/vnd.org.jonblack.provider." +
                     DeviceDiscoveryTable.TABLE_NAME;
       break;
     case DISCOVERY_ID:
       contentType = "vnd.android.cursor.item/vnd.org.jonblack.provider." +
                     DeviceDiscoveryTable.TABLE_NAME;
       break;
     case SESSION:
       contentType = "vnd.android.cursor.dir/vnd.org.jonblack.provider." +
                     SessionTable.TABLE_NAME;
       break;
     case SESSION_ID:
       contentType = "vnd.android.cursor.item/vnd.org.jonblack.provider." +
                     SessionTable.TABLE_NAME;
       break;
     default:
       throw new IllegalArgumentException("Unknown URI: " + uri);
     }
     
     return contentType;
   }
 
   @Override
   public Uri insert(Uri uri, ContentValues initialValues)
   {
     String table;
     Uri contentUri;
     
     // Only accept table (not row) uri's
     switch (sUriMatcher.match(uri))
     {
     case DEVICE:
       table = DeviceTable.TABLE_NAME;
       contentUri = DeviceTable.CONTENT_URI;
       break;
     case DISCOVERY:
       table = DeviceDiscoveryTable.TABLE_NAME;
       contentUri = DeviceDiscoveryTable.CONTENT_URI;
       break;
     case SESSION:
       table = SessionTable.TABLE_NAME;
       contentUri = SessionTable.CONTENT_URI;
       break;
     default:
       throw new IllegalArgumentException("Unknown URI: " + uri);
     }
     
     // Add to database
     ContentValues values;
     if (initialValues != null)
     {
       values = new ContentValues(initialValues);
     }
     else
     {
       values = new ContentValues();
     }
     
     Log.d(TAG, String.format("Inserting into '%s' values '%s'", table,
                              values.toString()));
     
     SQLiteDatabase db = mOpenHelper.getWritableDatabase();
     long rowId = db.insertOrThrow(table, null, values);
     if (rowId > 0)
     {
       Uri noteUri = ContentUris.withAppendedId(contentUri, rowId);
       getContext().getContentResolver().notifyChange(noteUri, null);
       
       // Also send notifications to anything listing devices. This is needed
       // to show updates in live tracking.
       // TODO: Is there a nicer way?
       if (table.equals(DeviceDiscoveryTable.TABLE_NAME))
       {
         getContext().getContentResolver().notifyChange(DeviceTable.CONTENT_URI,
                                                        null);
       }
       
       return noteUri;
     }
     
     throw new SQLException("Failed to insert row into " + uri);
   }
 
   @Override
   public int delete(Uri uri, String selection, String[] selectionArgs)
   {
     SQLiteDatabase db = mOpenHelper.getWritableDatabase();
     String table = null;
     
     switch (sUriMatcher.match(uri))
     {
     case DEVICE_ID:
       table = DeviceTable.TABLE_NAME;
       selection = DeviceTable.COL_ID + " = " + uri.getLastPathSegment();
       break;
     case DISCOVERY_ID:
       table = DeviceDiscoveryTable.TABLE_NAME;
       selection = DeviceDiscoveryTable.COL_ID + " = " + uri.getLastPathSegment();
       break;
     case SESSION_ID:
       table = SessionTable.TABLE_NAME;
       selection = DeviceDiscoveryTable.COL_ID + " = " + uri.getLastPathSegment();
       break;
     default:
       throw new IllegalArgumentException("Unknown URI: " + uri);
     }
     
     Log.d(TAG, String.format("Deleting from table '%s' where '%s' with args '%s'",
                              table, selection, Arrays.toString(selectionArgs)));
     
     int count = db.delete(table, selection, selectionArgs);
     getContext().getContentResolver().notifyChange(uri, null);
     
     Log.d(TAG, String.format("Deleted %d rows", count));
     
     return count;
   }
 
   @Override
   public int update(Uri uri, ContentValues values, String selection,
                     String[] selectionArgs)
   {
     SQLiteDatabase db = mOpenHelper.getWritableDatabase();
     
     int count;
     switch (sUriMatcher.match(uri))
     {
       case DEVICE_ID:
         Log.d(TAG, String.format("Updating '%s' where '%s' with values '%s'",
                                  DeviceTable.TABLE_NAME, selection,
                                  values.toString()));
         selection = DeviceTable.COL_ID + " = " + uri.getLastPathSegment();
         count = db.update(DeviceTable.TABLE_NAME, values, selection,
                           selectionArgs);
         break;
       case DISCOVERY_ID:
         Log.d(TAG, String.format("Updating '%s' where '%s' with values '%s'",
                                  DeviceDiscoveryTable.TABLE_NAME, selection,
                                  values.toString()));
         selection = DeviceDiscoveryTable.COL_ID + " = " + uri.getLastPathSegment();
         count = db.update(DeviceDiscoveryTable.TABLE_NAME, values, selection,
                           selectionArgs);
         break;
       case SESSION_ID:
         Log.d(TAG, String.format("Updating '%s' where '%s' with values '%s'",
                                  SessionTable.TABLE_NAME, selection,
                                  values.toString()));
         selection = SessionTable.COL_ID + " = " + uri.getLastPathSegment();
         count = db.update(SessionTable.TABLE_NAME, values, selection,
                           selectionArgs);
         break;
       default:
         throw new IllegalArgumentException("Unknown URI " + uri);
     }
     
     Log.d(TAG, String.format("Updated %d rows", count));
     
     getContext().getContentResolver().notifyChange(uri, null);
     return count;
   }
   
   public SQLiteOpenHelper getDatabaseHelper()
   {
     return mOpenHelper;
   }
   
   static
   {
     sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
     sUriMatcher.addURI(AUTHORITY, "device",             DEVICE);
     sUriMatcher.addURI(AUTHORITY, "device/#",           DEVICE_ID);
     sUriMatcher.addURI(AUTHORITY, "device_discovery",   DISCOVERY);
     sUriMatcher.addURI(AUTHORITY, "device_discovery/#", DISCOVERY_ID);
     sUriMatcher.addURI(AUTHORITY, "session",            SESSION);
     sUriMatcher.addURI(AUTHORITY, "session/#",          SESSION_ID);
   }
 }
