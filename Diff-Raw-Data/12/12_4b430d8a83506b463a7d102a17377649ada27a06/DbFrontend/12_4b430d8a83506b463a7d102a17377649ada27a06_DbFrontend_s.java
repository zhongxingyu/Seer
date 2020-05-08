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
 
 import java.util.ArrayList;
 
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.util.Log;
 
 import com.google.code.geobeagle.Clock;
 import com.google.code.geobeagle.Geocache;
 import com.google.code.geobeagle.GeocacheFactory;
 import com.google.code.geobeagle.GeocacheList;
 import com.google.code.geobeagle.GeocacheListLazy;
 import com.google.code.geobeagle.GeocacheListPrecomputed;
 import com.google.code.geobeagle.database.DatabaseDI;
 import com.google.code.geobeagle.database.DatabaseDI.GeoBeagleSqliteOpenHelper;
 import com.google.code.geobeagle.database.DatabaseDI.SQLiteWrapper;
 
 /**
  * Represents the front-end to access a database. It takes
  * responsibility to open and close the actual database connection without
  * involving the clients of this class.
  */
 public class DbFrontend {
     private Context mContext;
     private GeoBeagleSqliteOpenHelper mOpenHelper;
     private boolean mIsDatabaseOpen;
     private CacheWriter mCacheWriter;
     private ISQLiteDatabase mDatabase;
     private final GeocacheFactory mGeocacheFactory;
     private final Clock mClock = new Clock();
     /** The total number of geocaches and waypoints in the database. 
      * -1 means not initialized */
     private int mTotalCacheCount = -1;
     private SourceNameTranslator mSourceNameTranslator;
 
     private SQLiteWrapper mSqliteWrapper;
 
     private static final String[] READER_COLUMNS = new String[] {
         "Latitude", "Longitude", "Id", "Description", "Source", "CacheType", "Difficulty",
         "Terrain", "Container"
     };
     
     public DbFrontend(Context context, GeocacheFactory geocacheFactory) {
         mContext = context;
         mIsDatabaseOpen = false;
         mGeocacheFactory = geocacheFactory;
         mSourceNameTranslator = new SourceNameTranslator();
     }
 
     public void openDatabase() {
         if (mIsDatabaseOpen)
             return;
         //Log.d("GeoBeagle", "DbFrontend.openDatabase()");
         mIsDatabaseOpen = true;
 
         mOpenHelper = new GeoBeagleSqliteOpenHelper(mContext);
         final SQLiteDatabase sqDb = mOpenHelper.getReadableDatabase();
         mDatabase = new DatabaseDI.SQLiteWrapper(sqDb);
         mSqliteWrapper = mOpenHelper.getWritableSqliteWrapper();
     }
 
     public void closeDatabase() {
         if (!mIsDatabaseOpen)
             return;
         //Log.d("GeoBeagle", "DbFrontend.closeDatabase()");
         mIsDatabaseOpen = false;
 
         mOpenHelper.close();
         mCacheWriter = null;
         mDatabase = null;
     }
 
     public GeocacheList loadCachesPrecomputed(String where) {
         return loadCachesPrecomputed(where, -1);
     }
 
     /** If 'where' is null, returns all caches 
      * @param maxResults if <= 0, means no limit */
     public GeocacheList loadCachesPrecomputed(String where, int maxResults) {
         openDatabase();
 
         String limit = null;
         if (maxResults > 0) {
             limit = "0, " + maxResults;
         }
         long start = mClock.getCurrentTime();
         //CacheReaderCursor cursor = mCacheReader.open(where, limit);
         Cursor cursor = mSqliteWrapper.query(Database.TBL_CACHES, READER_COLUMNS,
                 where, null, null, null, limit);
         if (!cursor.moveToFirst()) {
             cursor.close();
             return GeocacheListPrecomputed.EMPTY;
         }
 
         ArrayList<Geocache> geocaches = new ArrayList<Geocache>();
         do {
             Geocache geocache = mGeocacheFactory.fromCursor(cursor, mSourceNameTranslator);
             geocaches.add(geocache);
         } while (cursor.moveToNext());
         cursor.close();
         Log.d("GeoBeagle", "DbFrontend.loadCachesPrecomputed took " + (mClock.getCurrentTime()-start) 
                 + " ms (loaded " + geocaches.size() + " caches)");
         return new GeocacheListPrecomputed(geocaches);
     }
 
     /** If 'where' is null, returns all caches */
     public GeocacheList loadCaches(String where) {
         return loadCaches(where, -1);
     }
 
     /** If 'where' is null, returns all caches.
      * Loads the caches when first used, not from this method.
      * @param maxResults if <= 0, means no limit */
     public GeocacheList loadCaches(String where, int maxResults) {
         openDatabase();
 
         String limit = null;
         if (maxResults > 0) {
             limit = "0, " + maxResults;
         }
         long start = mClock.getCurrentTime();
         final String fields[] = { "Id" };
         Cursor cursor = mDatabase.query(Database.TBL_CACHES, fields,
                 where, null, null, null, limit);
 
         if (!cursor.moveToFirst()) {
             cursor.close();
             return GeocacheListPrecomputed.EMPTY;
         }
 
         ArrayList<Object> idList = new ArrayList<Object>();
         if (cursor != null) {
             do {
                 idList.add(cursor.getString(0));
             } while (cursor.moveToNext());
             cursor.close();
         }
         Log.d("GeoBeagle", "DbFrontend.loadCachesLazy took " + (mClock.getCurrentTime()-start) 
                 + " ms (loaded " + idList.size() + " caches)");
         return new GeocacheListLazy(this, idList);
     }
 
     /** @param sqlQuery A complete SQL query to be executed. 
      * The query must return the id's of geocaches. */
     public GeocacheList loadCachesRaw(String sqlQuery) {
         openDatabase();
 
         long start = mClock.getCurrentTime();
         Log.d("GeoBeagle", "DbFrontend.loadCachesRaw(" + sqlQuery + ")");
         Cursor cursor = mDatabase.rawQuery(sqlQuery, new String[]{} );
 
         if (!cursor.moveToFirst()) {
             cursor.close();
             return GeocacheListPrecomputed.EMPTY;
         }
 
         ArrayList<Object> idList = new ArrayList<Object>();
         if (cursor != null) {
             do {
                 idList.add(cursor.getString(0));
             } while (cursor.moveToNext());
             cursor.close();
         }
         Log.d("GeoBeagle", "DbFrontend.loadCachesRaw took " + (mClock.getCurrentTime()-start) 
                 + " ms (loaded " + idList.size() + " caches)");
         return new GeocacheListLazy(this, idList);
     }
     
     /** @return null if the cache id is not in the database */
     public Geocache loadCacheFromId(String id) {
         Geocache loadedGeocache = mGeocacheFactory.getFromId(id);
         if (loadedGeocache != null) {
             return loadedGeocache;
         }
         
         openDatabase();
 
         Cursor cursor = mSqliteWrapper.query(Database.TBL_CACHES, READER_COLUMNS,
                 "Id='"+id+"'", null, null, null, null);
         if (!cursor.moveToFirst()) {
             cursor.close();
             return null;
         }
 
         Geocache geocache = mGeocacheFactory.fromCursor(cursor, mSourceNameTranslator);
         cursor.close();
         return geocache;
     }
     
     public CacheWriter getCacheWriter() {
         if (mCacheWriter != null)
             return mCacheWriter;
         openDatabase();
         
         mCacheWriter = DatabaseDI.createCacheWriter(mDatabase, mGeocacheFactory, this);
         return mCacheWriter;
     }
 
     private int countAll() {
         if (mTotalCacheCount != -1)
             return mTotalCacheCount;
         
         openDatabase();
         
         long start = mClock.getCurrentTime();
         Cursor countCursor;
             countCursor = mDatabase.rawQuery("SELECT COUNT(*) FROM " + Database.TBL_CACHES, null);
         countCursor.moveToFirst();
         mTotalCacheCount = countCursor.getInt(0);
         countCursor.close();
         Log.d("GeoBeagle", "DbFrontend.countAll took " + (mClock.getCurrentTime()-start) + " ms (" 
                 + mTotalCacheCount + " caches)");
         return mTotalCacheCount;
     }
     
    /** If 'where' is null, returns the total number of caches */
     public int count(String where) {
        if (where == null)
             return countAll();
 
         openDatabase();
         long start = mClock.getCurrentTime();
         
         Cursor countCursor = mDatabase.rawQuery("SELECT COUNT(*) FROM " + 
                 Database.TBL_CACHES + " WHERE " + where, null);
         countCursor.moveToFirst();
         int count = countCursor.getInt(0);
         countCursor.close();
         Log.d("GeoBeagle", "DbFrontend.count took " + (mClock.getCurrentTime()-start) + " ms (" 
                 + count + " caches)");
         return count;
     }
 
     /** 'sql' must be a complete SQL query that returns a single row 
      * with the result in the first column  */
     public int countRaw(String sql) {
         openDatabase();
         long start = mClock.getCurrentTime();
         
         Cursor countCursor = mDatabase.rawQuery(sql, null);
         countCursor.moveToFirst();
         int count = countCursor.getInt(0);
         countCursor.close();
         Log.d("GeoBeagle", "DbFrontend.countRaw took " + (mClock.getCurrentTime()-start) + " ms (" 
                 + count + " caches)");
         return count;
     }
     
     public void flushTotalCount() {
         mTotalCacheCount = -1;
     }
     
     public boolean geocacheHasTag(CharSequence geocacheId, int tagId) {
         openDatabase();
         Cursor cursor = mDatabase.rawQuery("SELECT COUNT(*) FROM " + 
                 Database.TBL_CACHETAGS + " WHERE CacheId='" + geocacheId 
                 + "' AND TagId=" + tagId, null);
         if (cursor == null) {
             return false;
         }
         cursor.moveToFirst();
         int count = cursor.getInt(0);        
         cursor.close();
         Log.d("GeoBeagle", "geocacheHasTag hit count is " + count);
         return (count > 0);
     }
         
     public void addGeocacheTag(CharSequence geocacheId, int tagId) {
         openDatabase();
         Log.d("GeoBeagle", "setGeocacheTag(" + geocacheId + ", " + tagId + ")");
         mSqliteWrapper.execSQL(Database.SQL_REPLACE_CACHETAG, geocacheId, tagId);
     }
 
     public void removeGeocacheTag(CharSequence geocacheId, int tagId) {
         openDatabase();
         Log.d("GeoBeagle", "unsetGeocacheTag(" + geocacheId + ", " + tagId + ")");
         mSqliteWrapper.execSQL(Database.SQL_DELETE_CACHETAG, geocacheId, tagId);
     }
 }
