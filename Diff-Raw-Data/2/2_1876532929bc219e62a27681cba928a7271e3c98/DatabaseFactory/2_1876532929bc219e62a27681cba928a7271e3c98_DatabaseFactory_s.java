 /*
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
 
 package com.google.code.geobeagle.io;
 
 import com.google.code.geobeagle.ui.ErrorDisplayer;
 
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteException;
 import android.database.sqlite.SQLiteDatabase.CursorFactory;
 
 public class DatabaseFactory {
 
     public class CacheReader {
         private Cursor mCursor;
         private final SQLiteDatabase mDb;
 
         public CacheReader(SQLiteDatabase sqlite) {
             mDb = sqlite;
         }
 
         public void close() {
            mDb.close();
         }
 
         public CharSequence getCache() {
             return mCursor.getString(1);
         }
 
         public boolean moveToNext() {
             return mCursor.moveToNext();
         }
 
         public boolean open() {
             try {
                 mCursor = mSqliteWrapper.query(mDb, "CACHES", null, null, null, null, null, null);
             } catch (SQLiteException e) {
                 e.printStackTrace();
                 return false;
             }
             final boolean result = mCursor.moveToFirst();
             if (!result)
                 mCursor.close();
             return result;
         }
     }
 
     public static class CacheWriter {
         private final SQLiteDatabase mSqlite;
 
         public CacheWriter(SQLiteDatabase sqlite) {
             mSqlite = sqlite;
         }
 
         public boolean write(String cache) {
             mSqlite.execSQL("INSERT INTO CACHES (Description) VALUES (\"" + cache + "\")");
             return true;
         }
 
     }
 
     public static class SQLiteWrapper {
         public SQLiteDatabase openDatabase(String path, CursorFactory factory, int flags) {
             return SQLiteDatabase.openDatabase(path, factory, flags);
         }
 
         public SQLiteDatabase openOrCreateDatabase(String path, CursorFactory factory) {
             return SQLiteDatabase.openOrCreateDatabase(path, factory);
         }
 
         public Cursor query(SQLiteDatabase db, String table, String[] columns, String selection,
                 String[] selectionArgs, String groupBy, String having, String orderBy) {
             return db.query(table, columns, selection, selectionArgs, groupBy, orderBy, having);
         }
     }
 
     public static final String CREATE_CACHE_TABLE = "CREATE TABLE IF NOT EXISTS CACHES (Id INTEGER PRIMARY KEY AUTOINCREMENT, Description VARCHAR)";
     public static final String DATABASE_FILE = "/sdcard/GeoBeagle.sqlite";
 
     private final SQLiteWrapper mSqliteWrapper;
 
     public DatabaseFactory(SQLiteWrapper sqliteWrapper) {
         mSqliteWrapper = sqliteWrapper;
     }
 
     public CacheReader createCacheReader(SQLiteDatabase sqlite) {
         return new CacheReader(sqlite);
     }
 
     public CacheWriter createCacheWriter(SQLiteDatabase sqlite) {
         return new CacheWriter(sqlite);
     }
 
     public SQLiteDatabase openCacheDatabase(ErrorDisplayer errorDisplayer) {
         try {
             return mSqliteWrapper.openDatabase(DATABASE_FILE, null, SQLiteDatabase.OPEN_READONLY);
         } catch (final SQLiteException e) {
             errorDisplayer.displayError("Error opening database " + DATABASE_FILE + ": "
                     + e.getMessage() + ".");
         }
         return null;
     }
 
     public SQLiteDatabase openOrCreateCacheDatabase(ErrorDisplayer errorDisplayer) {
         try {
             SQLiteDatabase sqlite = mSqliteWrapper.openOrCreateDatabase(DATABASE_FILE, null);
             sqlite.execSQL(CREATE_CACHE_TABLE);
             return sqlite;
         } catch (final SQLiteException e) {
             errorDisplayer.displayError("Error opening or creating database " + DATABASE_FILE + ": "
                     + e.getMessage() + ".");
 
         }
         return null;
     }
 }
