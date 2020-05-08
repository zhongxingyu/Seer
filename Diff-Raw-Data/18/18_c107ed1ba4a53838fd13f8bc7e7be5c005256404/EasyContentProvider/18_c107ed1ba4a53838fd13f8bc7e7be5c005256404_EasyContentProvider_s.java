 package org.aaron.easycontent.provider;
 
 import android.content.*;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.net.Uri;
 import android.util.Log;
 import org.aaron.easycontent.database.DatabaseTable;
 
 /**
  * User: aprobus
  * Date: 2/16/13
  * Time: 12:28 PM
  */
 public abstract class EasyContentProvider extends ContentProvider {
 
    private static final String COLUMN_ID = "_id";
    private static final String SINGLE_ITEM_WHERE_CLAUSE = COLUMN_ID + " = ?";
 
    private DatabaseTable[] mTables;
    private String mAuthority;
    private UriMatcher mUriMatcher;
 
    private SQLiteOpenHelper mDatabaseHelper;
    private String mDatabaseName;
    private int mDatabaseVersion;
 
    public EasyContentProvider(DatabaseTable[] tables, String authority, String databaseName, int dbVersion) {
       mTables = tables;
       mAuthority = authority;
       mDatabaseName = databaseName;
       mDatabaseVersion = dbVersion;
 
       setupUris();
    }
 
    private void setupUris() {
       mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
 
       String authority = mAuthority;
 
       for (int i = 0; i < mTables.length; i += 1) {
          ContentProviderTable endPoint = mTables[i].getClass().getAnnotation(ContentProviderTable.class);
          mUriMatcher.addURI(authority, endPoint.tableName(), i * 2);
          mUriMatcher.addURI(authority, endPoint.tableName() + "/#", i * 2 + 1);
       }
    }
 
    private int getMatcherInfo(Uri uri) {
       int matchResult = mUriMatcher.match(uri);
 
       if (matchResult < 0 || matchResult / 2 >= mTables.length) {
          throw new IllegalArgumentException("Invalid uri: " + uri);
       }
 
       short tableIndex = (short)(matchResult / 2);
 
       boolean isSingleItem = true;
       if ((matchResult & 1) == 0) {
          isSingleItem = false;
       }
 
       return MatcherInfo.fromValues(tableIndex, isSingleItem);
    }
 
    private DatabaseTable findDatabaseTableForMatcherInfo(int matcherInfo) {
       return mTables[MatcherInfo.getTableIndex(matcherInfo)];
    }
 
    @Override
    public boolean onCreate() {
       mDatabaseHelper = new BaseSQLiteOpenHelper(getContext());
       return true;
    }
 
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
       int matcherInfo = getMatcherInfo(uri);
       DatabaseTable matchedTable = findDatabaseTableForMatcherInfo(matcherInfo);
       boolean isSingleItem = MatcherInfo.isSingleItem(matcherInfo);
 
       ContentProviderTable tableMeta = matchedTable.getClass().getAnnotation(ContentProviderTable.class);
       SQLiteDatabase readableDb = mDatabaseHelper.getReadableDatabase();
 
       if (isSingleItem) {
         Log.i("LOL", "Single item uri: " + uri.toString());
          long rowId = ContentUris.parseId(uri);
         return readableDb.query(tableMeta.tableName(), projection, SINGLE_ITEM_WHERE_CLAUSE, new String[]{ Long.toString(rowId) }, null, null, null);
       } else {
         Log.i("LOL", "Multiple item uri: " + uri.toString());
         return readableDb.query(tableMeta.tableName(), projection, selection, selectionArgs, null, null, sortOrder);
       }
    }
 
    @Override
    public String getType(Uri uri) {
       int matcherInfo = getMatcherInfo(uri);
       DatabaseTable matchedTable = findDatabaseTableForMatcherInfo(matcherInfo);
 
       boolean isSingleItem = MatcherInfo.isSingleItem(matcherInfo);
 
       ContentProviderTable tableMeta = matchedTable.getClass().getAnnotation(ContentProviderTable.class);
       String supportedClass = tableMeta.mappedClass().getCanonicalName();
 
       if (isSingleItem) {
          return ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd." + supportedClass;
       } else {
          return ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd." + supportedClass;
       }
    }
 
    @Override
    public Uri insert(Uri uri, ContentValues values) {
       values.remove(COLUMN_ID);
 
       int matcherInfo = getMatcherInfo(uri);
       DatabaseTable matchedTable = findDatabaseTableForMatcherInfo(matcherInfo);
 
       ContentProviderTable tableMeta = matchedTable.getClass().getAnnotation(ContentProviderTable.class);
 
       SQLiteDatabase writableDb = mDatabaseHelper.getWritableDatabase();
       long rowId = writableDb.insert(tableMeta.tableName(), null, values);
 
       Uri rowUri = ContentUris.withAppendedId(uri, rowId);
 
      getContext().getContentResolver().notifyChange(uri, null);
 
       return rowUri;
    }
 
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
       int matcherInfo = getMatcherInfo(uri);
       DatabaseTable matchedTable = findDatabaseTableForMatcherInfo(matcherInfo);
       boolean isSingleItem = MatcherInfo.isSingleItem(matcherInfo);
 
       ContentProviderTable tableMeta = matchedTable.getClass().getAnnotation(ContentProviderTable.class);
 
       SQLiteDatabase writableDb = mDatabaseHelper.getWritableDatabase();
 
       int numRowsDeleted;
       if (isSingleItem) {
          long rowId = ContentUris.parseId(uri);
          numRowsDeleted = writableDb.delete(tableMeta.tableName(), SINGLE_ITEM_WHERE_CLAUSE, new String[]{ Long.toString(rowId) });
       } else {
          numRowsDeleted = writableDb.delete(tableMeta.tableName(), selection, selectionArgs);
       }
 
       getContext().getContentResolver().notifyChange(uri, null);
 
       return numRowsDeleted;
    }
 
    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
       int matcherInfo = getMatcherInfo(uri);
       DatabaseTable matchedTable = findDatabaseTableForMatcherInfo(matcherInfo);
       boolean isSingleItem = MatcherInfo.isSingleItem(matcherInfo);
 
       ContentProviderTable tableMeta = matchedTable.getClass().getAnnotation(ContentProviderTable.class);
       SQLiteDatabase writableDb = mDatabaseHelper.getWritableDatabase();
 
       values.remove(COLUMN_ID);
       int numRowsUpdated;
       if (isSingleItem) {
          long rowId = ContentUris.parseId(uri);
          numRowsUpdated = writableDb.update(tableMeta.tableName(), values, SINGLE_ITEM_WHERE_CLAUSE, new String[]{ Long.toString(rowId) });
       } else {
          numRowsUpdated = writableDb.update(tableMeta.tableName(), values, selection, selectionArgs);
       }
 
       getContext().getContentResolver().notifyChange(uri, null);
 
       return numRowsUpdated;
    }
 
    @Override
    public void shutdown() {
       super.shutdown();
       mDatabaseHelper.close();
    }
 
    protected void onCreateDatabase(SQLiteDatabase db) {
       for (DatabaseTable table : mTables) {
          table.onCreate(db);
       }
    }
 
    protected void onUpgradeDatabase(SQLiteDatabase db, int oldVersion, int newVersion) {
       for (DatabaseTable table : mTables) {
          table.onUpgrade(db, oldVersion, newVersion);
       }
    }
 
    private class BaseSQLiteOpenHelper extends SQLiteOpenHelper {
 
       public BaseSQLiteOpenHelper(Context context) {
          super(context, mDatabaseName, null, mDatabaseVersion);
       }
 
       @Override
       public void onCreate(SQLiteDatabase db) {
          onCreateDatabase(db);
       }
 
       @Override
       public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
          onUpgradeDatabase(db, oldVersion, newVersion);
       }
    }
 
    private static class MatcherInfo {
       private static final int BITMASK_SINGLE_ITEM = 0x100;
       private static final int BITMASK_INDEX = 0xFF;
 
       public static int fromValues(short tableIndex, boolean isSingleItem) {
          int matcherInfo = tableIndex;
 
          if (isSingleItem) {
             matcherInfo = matcherInfo | BITMASK_SINGLE_ITEM;
          }
 
          return matcherInfo;
       }
 
       public static boolean isSingleItem(int matcherInfo) {
          return (matcherInfo & BITMASK_SINGLE_ITEM) > 0;
       }
 
       public static short getTableIndex(int matcherInfo) {
          return (short)(matcherInfo & BITMASK_INDEX);
       }
    }
 
 }
