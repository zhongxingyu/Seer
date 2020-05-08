 package com.snot.whereareyou.database;
 
 import android.content.ContentProvider;
 import android.content.ContentResolver;
 import android.content.ContentValues;
 import android.content.UriMatcher;
 import android.database.Cursor;
 import android.net.Uri;
 import android.net.Uri.Builder;
 import android.util.Log;
 import android.widget.Toast;
 
 /**
  * Created by snot on 6/3/13.
  */
 public class Provider extends ContentProvider {
 
     private static final String TAG = "Provider";
 
     private static final String AUTHORITY = "com.snot.whereareyou.database.provider";
 
 	public static final Uri URI_HISTORYS = (new Uri.Builder())
 						.scheme(ContentResolver.SCHEME_CONTENT)
 						.authority(AUTHORITY)
						.appendPath("/history")
 						.build();
 
 
     private static final int HISTORY = 1;
     private static final int HISTORYS = 2;
 
     private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
 
     static
     {
         sURIMatcher.addURI(AUTHORITY, "history", HISTORYS);
         sURIMatcher.addURI(AUTHORITY, "history/#", HISTORY);
     }
 
 
     public Provider() {
     }
 
     @Override
     public String getType(Uri uri) {
         // TODO: Implement this to handle requests for the MIME type of the data
         // at the given URI.
         throw new UnsupportedOperationException("Not yet implemented");
     }
 
 
     @Override
     public boolean onCreate() {
         // TODO: Implement this to initialize your content provider on startup.
         return true;
     }
 
     @Override
     public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
         Log.v(TAG, "URI: " + uri);
         Cursor result = null;
         int match = sURIMatcher.match(uri);
         switch(match)
         {
             case HISTORYS:
                 result = DatabaseHandler
                     .getInstance(getContext())
                     .getReadableDatabase()
                     .query(History.TABLE_NAME, History.FIELDS, selection, selectionArgs, null, null, sortOrder);
                 result.setNotificationUri(getContext().getContentResolver(), URI_HISTORYS);
                 break;
             case HISTORY:
                 final long eid = Long.parseLong(uri.getLastPathSegment());
                 result = DatabaseHandler
                     .getInstance(getContext())
                     .getReadableDatabase()
                     .query(History.TABLE_NAME, History.FIELDS,
                             History.COL_ID + " IS ?",
                             new String[] { String.valueOf(eid) }, null, null, sortOrder);
                 result.setNotificationUri(getContext().getContentResolver(), URI_HISTORYS);
                 break;
             default:
                 throw new UnsupportedOperationException("Unmatched(" + match + ") URI: " + uri.toString());
         }
         return result;
     }
 
     @Override
     public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
         // TODO: Implement this to handle requests to update one or more rows.
         throw new UnsupportedOperationException("Not yet implemented");
     }
 
     @Override
     public Uri insert(Uri uri, ContentValues values) {
         // TODO: Implement this to handle requests to insert a new row.
         throw new UnsupportedOperationException("Not yet implemented");
     }
 
     @Override
     public int delete(Uri uri, String selection, String[] selectionArgs) {
         // Implement this to handle requests to delete one or more rows.
          throw new UnsupportedOperationException("Not yet implemented");
 /*
         int result = -1;
         if (uri.toString().startsWith(HISTORY_BASE)) {
             final long id = Long.parseLong(uri.getLastPathSegment());
             result = DatabaseHandler
                     .getInstance(getContext())
                     .getWritableDatabase()
                     .delete(History.TABLE_NAME,
                             History.COL_ID + " IS ?",
                             new String[] { String.valueOf(id) });
             result.setNotificationUri(getContext().getContentResolver(), URI_HISTORYS);
         }
         return result;
 */
     }
 }
 
