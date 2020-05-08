 package com.gmail.altakey.mint;
 
 import android.content.Context;
 import android.content.ContentProvider;
 import android.content.ContentValues;
 import android.net.Uri;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.database.sqlite.SQLiteStatement;
 import android.database.sqlite.SQLiteStatement;
 import android.content.UriMatcher;
 import android.content.ContentUris;
 import android.database.Cursor;
 
 import java.util.Arrays;
 import java.util.List;
 
 public class TaskContextProvider extends ContentProvider {
     public static final Uri CONTENT_URI = Uri.parse(String.format("content://%s/contexts", ProviderMap.AUTHORITY_CONTEXT));
 
     public static final String[] PROJECTION = new String[] {
         "_id", "context", "name"
     };
 
     public static final String DEFAULT_ORDER = "order by name";
     public static final String NO_ORDER = "";
     public static final String ID_FILTER = "_id=?";
     public static final String ALL_FILTER = "1=1";
     public static final String MULTIPLE_CONTEXTS_FILTER = "context in (%s)";
 
     public static final String COLUMN_ID = "_id";
     public static final String COLUMN_CONTEXT = "context";
     public static final String COLUMN_NAME = "name";
 
     public static final int COL_ID = 0;
     public static final int COL_NAME = 1;
 
     private static final String CONTEXT_QUERY = "SELECT _id,context,name FROM contexts WHERE %s %s";
 
     private static final String CONTEXT_INSERT_QUERY = "INSERT INTO contexts (context,name) VALUES (?,?)";
 
     private static final String CONTEXT_REPLACE_QUERY = "REPLACE INTO contexts (_id,context,name) VALUES (?,?,?)";
 
     private static final String CONTEXT_UPDATE_QUERY = "UPDATE contexts set context=?,name=? %s";
 
    private static final String CONTEXT_DELETE_QUERY = "DELETE contexts %s";
 
     private SQLiteOpenHelper mHelper;
 
     @Override
     public String getType(Uri uri) {
         return new ProviderMap(uri).getContentType();
     }
 
     @Override
     public boolean onCreate() {
         mHelper = new Schema.OpenHelper(getContext());
         return true;
     }
 
     @Override
     public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
         final SQLiteDatabase db = mHelper.getReadableDatabase();
 
         switch (new ProviderMap(uri).getResourceType()) {
         case ProviderMap.CONTEXTS:
             return db.rawQuery(String.format(CONTEXT_QUERY, selection == null ? ALL_FILTER : selection, sortOrder == null ? DEFAULT_ORDER : sortOrder), selectionArgs);
         case ProviderMap.CONTEXTS_ID:
             return db.rawQuery(String.format(CONTEXT_QUERY, ID_FILTER, NO_ORDER), new String[] { String.valueOf(ContentUris.parseId(uri)) });
         default:
             return null;
         }
     }
 
     @Override
     public Uri insert(Uri uri, ContentValues values) {
         final SQLiteDatabase db = mHelper.getWritableDatabase();
         final int resourceType = new ProviderMap(uri).getResourceType();
 
         if (resourceType == ProviderMap.CONTEXTS) {
             final SQLiteStatement stmt = db.compileStatement(CONTEXT_INSERT_QUERY);
             stmt.bindString(1, (String)values.get("context"));
             stmt.bindString(2, (String)values.get("name"));
             try {
                 return ContentUris.withAppendedId(uri, stmt.executeInsert());
             } finally {
                 stmt.close();
             }
         } else if (resourceType == ProviderMap.CONTEXTS_ID) {
             final SQLiteStatement stmt = db.compileStatement(CONTEXT_REPLACE_QUERY);
             stmt.bindString(1, (String)values.get("_id"));
             stmt.bindString(2, (String)values.get("context"));
             stmt.bindString(3, (String)values.get("name"));
             try {
                 stmt.executeInsert();
                 return uri;
             } finally {
                 stmt.close();
             }
         } else {
             return null;
         }
     }
 
     @Override
     public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
         final SQLiteDatabase db = mHelper.getWritableDatabase();
         final int resourceType = new ProviderMap(uri).getResourceType();
 
         if (resourceType == ProviderMap.CONTEXTS) {
             if (MULTIPLE_CONTEXTS_FILTER.equals(selection)) {
                 selection = new FilterExpander(selection, selectionArgs).expand();
             }
 
             final SQLiteStatement stmt = db.compileStatement(String.format(CONTEXT_UPDATE_QUERY, selection == null ? "" : String.format("WHERE %s", selection)));
             stmt.bindString(1, (String)values.get("context"));
             stmt.bindString(2, (String)values.get("name"));
 
             int offset = 3;
             for (final String arg: selectionArgs) {
                 stmt.bindString(offset++, arg);
             }
             try {
                 return stmt.executeUpdateDelete();
             } finally {
                 stmt.close();
             }
         } else {
             return 0;
         }
     }
 
     @Override
     public int delete(Uri uri, String selection, String[] selectionArgs) {
         final SQLiteDatabase db = mHelper.getWritableDatabase();
 
         switch (new ProviderMap(uri).getResourceType()) {
         case ProviderMap.CONTEXTS:
             if (MULTIPLE_CONTEXTS_FILTER.equals(selection)) {
                 selection = new FilterExpander(selection, selectionArgs).expand();
             }
 
             final SQLiteStatement stmt =
                 db.compileStatement(String.format(CONTEXT_DELETE_QUERY, selection == null ? "" : String.format("WHERE %s", selection)));
 
             int offset = 1;
             for (final String arg: selectionArgs) {
                 stmt.bindString(offset++, arg);
             }
             try {
                 return stmt.executeUpdateDelete();
             } finally {
                 stmt.close();
             }
         default:
             return 0;
         }
     }
 }
