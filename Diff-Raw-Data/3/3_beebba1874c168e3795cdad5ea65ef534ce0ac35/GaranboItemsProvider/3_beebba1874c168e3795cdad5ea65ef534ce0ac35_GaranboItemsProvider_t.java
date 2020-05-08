 package net.sourcewalker.garanbot.data;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.util.HashMap;
 
 import net.sourcewalker.garanbot.api.Item;
 import android.content.ContentProvider;
 import android.content.ContentUris;
 import android.content.ContentValues;
 import android.content.UriMatcher;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteQueryBuilder;
 import android.net.Uri;
 import android.os.ParcelFileDescriptor;
 import android.util.Log;
 
 /**
  * This content provider communicates with the local database, which stores the
  * garanbo items.
  * 
  * @author pieronczyk
  */
 public class GaranboItemsProvider extends ContentProvider {
 
     private static final Uri CONTENT_URI_BASE = Uri.parse("content://"
             + GaranbotDBMetaData.AUTHORITY);
     public static final Uri CONTENT_URI_ITEMS = Uri.withAppendedPath(
             CONTENT_URI_BASE, "items");
     public static final Uri CONTENT_URI_IMAGES = Uri.withAppendedPath(
             CONTENT_URI_BASE, "images");
 
     private static UriMatcher matcher;
     private GaranbotDBHelper dbHelper;
     private static final String TAG = "GaranbotProvider";
     private static final HashMap<String, String> projectionMap;
     private static final int MATCH_LIST = 1;
     private static final int MATCH_ITEM = 2;
     private static final int MATCH_IMAGE = 3;
 
     static {
         matcher = new UriMatcher(UriMatcher.NO_MATCH);
         matcher.addURI(GaranbotDBMetaData.AUTHORITY, "items", MATCH_LIST);
         matcher.addURI(GaranbotDBMetaData.AUTHORITY, "items/#", MATCH_ITEM);
         matcher.addURI(GaranbotDBMetaData.AUTHORITY, "images/#", MATCH_IMAGE);
 
         projectionMap = new HashMap<String, String>();
         for (String col : GaranbotDBMetaData.DEFAULT_PROJECTION) {
             projectionMap.put(col, col);
         }
         projectionMap.put(GaranbotDBMetaData.IMAGE_URI, "'"
                + CONTENT_URI_IMAGES + "/' || " + GaranbotDBMetaData.SERVER_ID
                + " as image");
     }
 
     @Override
     public int delete(Uri uri, String arg1, String[] arg2) {
         Log.d(TAG, "delete");
         SQLiteDatabase db = dbHelper.getWritableDatabase();
         int result;
         switch (matcher.match(uri)) {
         case MATCH_LIST:
             result = db.delete(GaranbotDBMetaData.TABLE_NAME, null, null);
             break;
         case MATCH_ITEM:
             result = db.delete(GaranbotDBMetaData.TABLE_NAME,
                     GaranbotDBMetaData._ID + " == ?",
                     new String[] { uri.getLastPathSegment() });
             break;
         default:
             throw new IllegalArgumentException("Unknown URI: " + uri);
         }
         getContext().getContentResolver().notifyChange(uri, null);
         return result;
     }
 
     @Override
     public String getType(Uri arg0) {
         // TODO Auto-generated method stub
         return null;
     }
 
     @Override
     public Uri insert(Uri uri, ContentValues values) {
         Log.d(TAG, "insert");
         switch (matcher.match(uri)) {
         case MATCH_LIST:
             if (values.containsKey(GaranbotDBMetaData._ID)) {
                 long valuesId = values.getAsLong(GaranbotDBMetaData._ID);
                 if (valuesId == Item.UNKNOWN_ID) {
                     values.remove(GaranbotDBMetaData._ID);
                 }
             }
             SQLiteDatabase db = dbHelper.getWritableDatabase();
             long id = db.insert(GaranbotDBMetaData.TABLE_NAME,
                     GaranbotDBMetaData.NAME, values);
             Uri numberUri = ContentUris.withAppendedId(CONTENT_URI_ITEMS, id);
             getContext().getContentResolver().notifyChange(numberUri, null);
             return numberUri;
         default:
             throw new IllegalArgumentException("Unknown URI: " + uri);
         }
     }
 
     @Override
     public boolean onCreate() {
         dbHelper = new GaranbotDBHelper(getContext());
         return true;
     }
 
     /**
      * Queries the database for garanbo items and returns a cursor object which
      * points to the requested data.
      */
     @Override
     public Cursor query(Uri uri, String[] projection, String selection,
             String[] selectionArgs, String sortOrder) {
         Log.d(TAG, "query");
         // Build basic sql query
         SQLiteQueryBuilder query = new SQLiteQueryBuilder();
         query.setTables(GaranbotDBMetaData.TABLE_NAME);
         query.setProjectionMap(projectionMap);
         if (sortOrder == null) {
             sortOrder = GaranbotDBMetaData.DEFAULT_SORT_ORDER;
         }
         SQLiteDatabase db = dbHelper.getReadableDatabase();
         Cursor c;
         switch (matcher.match(uri)) {
         case MATCH_LIST:
             break;
         case MATCH_ITEM:
             query.appendWhere(GaranbotDBMetaData._ID + " == "
                     + ContentUris.parseId(uri));
             break;
         default:
             throw new IllegalArgumentException("Unknown URI: " + uri);
         }
         c = query.query(db, projection, selection, selectionArgs, null, null,
                 sortOrder);
         c.setNotificationUri(getContext().getContentResolver(), uri);
         return c;
     }
 
     @Override
     public int update(Uri uri, ContentValues values, String arg2, String[] arg3) {
         Log.d(TAG, "update");
         switch (matcher.match(uri)) {
         case MATCH_ITEM:
             SQLiteDatabase db = dbHelper.getWritableDatabase();
             int result = db.update(GaranbotDBMetaData.TABLE_NAME, values,
                     GaranbotDBMetaData._ID + " == ?",
                     new String[] { uri.getLastPathSegment() });
             getContext().getContentResolver().notifyChange(uri, null);
             return result;
         default:
             throw new IllegalArgumentException("Unknown URI: " + uri);
         }
     }
 
     /*
      * (non-Javadoc)
      * @see android.content.ContentProvider#openFile(android.net.Uri,
      * java.lang.String)
      */
     @Override
     public ParcelFileDescriptor openFile(Uri uri, String mode)
             throws FileNotFoundException {
         switch (matcher.match(uri)) {
         case MATCH_IMAGE:
             long itemId = ContentUris.parseId(uri);
             File imageFile;
             if (itemId == Item.UNKNOWN_ID) {
                 imageFile = ImageCache.getDefaultImageFile(getContext());
             } else {
                 imageFile = ImageCache.getFile(getContext(), itemId);
                 if (!imageFile.exists()) {
                     ImageDownloadService.downloadImage(getContext(), itemId);
                     imageFile = ImageCache.getDefaultImageFile(getContext());
                 }
             }
             return ParcelFileDescriptor.open(imageFile,
                     ParcelFileDescriptor.MODE_READ_ONLY);
         default:
             throw new IllegalArgumentException("Unknown URI: " + uri);
         }
     }
 }
