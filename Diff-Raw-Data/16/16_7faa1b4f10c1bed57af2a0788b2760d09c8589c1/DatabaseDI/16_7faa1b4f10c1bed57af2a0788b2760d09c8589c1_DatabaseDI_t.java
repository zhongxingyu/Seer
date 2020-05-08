 /*
  ** Licensed under the Apache License, Version 2.0 (the "License");
  ** you may not use this file except in compliance with the License.
  ** You may obtain a copy of the License at
  **
  **     http://www.apache.org/licenses/LICENSE-2.0
  **
  ** Unless required by applicable law or agreed to in writing, software
  ** distributed under the License is distributed on an "AS IS" BASIS,
  ** WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ** See the License for the specific language governing permissions and
  ** limitations under the License.
  */
 
 package com.google.code.geobeagle.database;
 
 import com.google.code.geobeagle.GeocacheFactory;
import com.google.code.geobeagle.activity.cachelist.SearchWhereFactory;
 import com.google.code.geobeagle.database.WhereFactoryNearestCaches.BoundingBox;
 import com.google.code.geobeagle.database.WhereFactoryNearestCaches.Search;
 import com.google.code.geobeagle.database.WhereFactoryNearestCaches.SearchDown;
 import com.google.code.geobeagle.database.WhereFactoryNearestCaches.SearchUp;
 import com.google.code.geobeagle.database.WhereFactoryNearestCaches.WhereStringFactory;
 import com.google.code.geobeagle.preferences.PreferencesUpgrader;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.util.Log;
 
 import java.util.Arrays;
 
 public class DatabaseDI {
 
     public static class CacheReaderCursorFactory {
         public CacheReaderCursor create(Cursor cursor) {
             final GeocacheFactory geocacheFactory = new GeocacheFactory();
             final DbToGeocacheAdapter dbToGeocacheAdapter = new DbToGeocacheAdapter();
             return new CacheReaderCursor(cursor, geocacheFactory, dbToGeocacheAdapter);
         }
     }
 
     static class GeoBeagleSqliteOpenHelper extends SQLiteOpenHelper {
         private final OpenHelperDelegate mOpenHelperDelegate;
         private final PreferencesUpgrader mPreferencesUpgrader;
 
         GeoBeagleSqliteOpenHelper(Context context, PreferencesUpgrader preferencesUpgrader) {
             super(context, Database.DATABASE_NAME, null, Database.DATABASE_VERSION);
             mOpenHelperDelegate = new OpenHelperDelegate();
             mPreferencesUpgrader = preferencesUpgrader;
         }
 
         SQLiteWrapper getWritableSqliteWrapper() {
             return new SQLiteWrapper(this.getWritableDatabase());
         }
 
         @Override
         public void onCreate(SQLiteDatabase db) {
             final SQLiteWrapper sqliteWrapper = new SQLiteWrapper(db);
             mOpenHelperDelegate.onCreate(sqliteWrapper);
         }
 
         @Override
         public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
             final SQLiteWrapper sqliteWrapper = new SQLiteWrapper(db);
             mOpenHelperDelegate.onUpgrade(sqliteWrapper, oldVersion);
             mPreferencesUpgrader.upgrade(oldVersion);
         }
     }
 
     public static class SQLiteWrapper implements ISQLiteDatabase {
         private final SQLiteDatabase mSQLiteDatabase;
 
         SQLiteWrapper(SQLiteDatabase writableDatabase) {
             mSQLiteDatabase = writableDatabase;
         }
 
         @Override
         public void beginTransaction() {
             mSQLiteDatabase.beginTransaction();
         }
 
         @Override
         public int countResults(String table, String selection, String... selectionArgs) {
 
             Cursor cursor = mSQLiteDatabase.query(table, null, selection, selectionArgs, null,
                     null, null, null);
             int count = cursor.getCount();
             Log.d("GeoBeagle", "SQL count results: " + selection + ", "
                     + Arrays.toString(selectionArgs) + ": " + count);
             cursor.close();
             return count;
         }
 
         @Override
         public void endTransaction() {
             mSQLiteDatabase.endTransaction();
         }
 
         public void execSQL(String sql) {
             Log.d("GeoBeagle", this + " :SQL: " + sql);
             mSQLiteDatabase.execSQL(sql);
         }
 
         @Override
         public void execSQL(String sql, Object... bindArgs) {
             Log.d("GeoBeagle", this + " :SQL: " + sql + ", " + Arrays.toString(bindArgs));
             mSQLiteDatabase.execSQL(sql, bindArgs);
         }
 
         @Override
         public Cursor query(String table, String[] columns, String selection, String groupBy,
                 String having, String orderBy, String limit, String... selectionArgs) {
             final Cursor query = mSQLiteDatabase.query(table, columns, selection, selectionArgs,
                     groupBy, orderBy, having, limit);
             // Log.d("GeoBeagle", "limit: " + limit + ", count: " +
             // query.getCount() + ", query: "
             // + selection);
             Log.d("GeoBeagle", "limit: " + limit + ", query: " + selection);
             return query;
         }
 
         @Override
         public Cursor query(String table,
                 String[] columns,
                 String selection,
                 String selectionArgs[],
                 String groupBy,
                 String having,
                 String orderBy,
                 String limit) {
             final Cursor query = mSQLiteDatabase.query(table, columns, selection, selectionArgs,
                     groupBy, orderBy, having, limit);
             Log.d("GeoBeagle", "limit: " + limit + ", query: " + selection);
             return query;
         }
 
         @Override
         public Cursor rawQuery(String sql, String[] selectionArgs) {
             return mSQLiteDatabase.rawQuery(sql, selectionArgs);
         }
 
         @Override
         public void setTransactionSuccessful() {
             mSQLiteDatabase.setTransactionSuccessful();
         }
 
         @Override
         public void close() {
             Log.d("GeoBeagle", "----------closing sqlite------");
             mSQLiteDatabase.close();
         }
 
         @Override
         public boolean isOpen() {
             return mSQLiteDatabase.isOpen();
         }
 
         @Override
         public void insert(String table, String[] columns, Object[] bindArgs) {
             // Assumes len(bindArgs) > 0.
             StringBuilder columnsAsString = new StringBuilder();
             for (String column : columns) {
                 columnsAsString.append(", ");
                 columnsAsString.append(column);
             }
             mSQLiteDatabase.execSQL("REPLACE INTO " + table + " (" + columnsAsString.substring(2)
                     + ") VALUES (?, ?)", bindArgs);
         }
 
         @Override
         public boolean hasValue(String table, String[] columns, String[] selectionArgs) {
             StringBuilder where = new StringBuilder();
             where.append(columns[0] + "=?");
             for (int ix = 1; ix < columns.length; ix++) {
                 where.append(" AND " + columns[ix] + "=?");
             }
 
             Cursor c = mSQLiteDatabase.query(table, new String[] {
                 columns[0]
             }, where.toString(), selectionArgs, null, null, null);
             boolean hasValues = c.moveToFirst();
             c.close();
             return hasValues;
         }
 
         @Override
         public void delete(String table, String whereClause, String whereArg) {
             mSQLiteDatabase.delete(table, whereClause + "=?", new String[] {
                 whereArg
             });
         }
 
         @Override
         public void update(String table,
                 ContentValues values,
                 String whereClause,
                 String[] whereArgs) {
             Log.d("GeoBeagle", "updating: " + table + ", " + values + ", " + whereClause + ", "
                     + whereArgs);
             mSQLiteDatabase.update(table, values, whereClause, whereArgs);
         }
     }
 
     static public class SearchFactory {
         public Search createSearch(double latitude,
                 double longitude,
                 float min,
                 float max,
                 ISQLiteDatabase sqliteWrapper) {
             WhereStringFactory whereStringFactory = new WhereStringFactory();
             BoundingBox boundingBox = new BoundingBox(latitude, longitude, sqliteWrapper,
                     whereStringFactory);
             SearchDown searchDown = new SearchDown(boundingBox, min);
             SearchUp searchUp = new SearchUp(boundingBox, max);
             return new WhereFactoryNearestCaches.Search(boundingBox, searchDown, searchUp);
         }
 
     }
 
 }
