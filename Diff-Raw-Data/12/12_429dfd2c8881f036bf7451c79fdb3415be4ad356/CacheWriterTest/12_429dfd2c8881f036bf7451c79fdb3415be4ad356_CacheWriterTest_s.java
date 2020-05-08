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
 
 import static org.easymock.EasyMock.expect;
 import static org.easymock.classextension.EasyMock.createMock;
 import static org.easymock.classextension.EasyMock.replay;
 import static org.easymock.classextension.EasyMock.verify;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 
 import com.google.code.geobeagle.CacheType;
 import com.google.code.geobeagle.GeocacheFactory.Source;
 import com.google.code.geobeagle.database.DatabaseDI.SQLiteWrapper;
 
 import org.junit.Test;
 
 public class CacheWriterTest {
 
     private static final String INSERT_INTO_CACHES = "INSERT INTO CACHES (Id, Description, Source, DeleteMe) ";
     private static final String INSERT_INTO_GPX = "INSERT INTO GPX (Name, ExportTime, DeleteMe) ";
 
     @Test
     public void testClearEarlierLoads() {
         DesktopSQLiteDatabase db = new DesktopSQLiteDatabase();
         db.execSQL(DatabaseTest.currentSchema()); // andpe: Error
                                                   // "table CACHES already exists"
 
         db.execSQL(INSERT_INTO_CACHES + "VALUES ('GCTHISIMPORT', 'just loaded', 'foo.gpx', 0)");
         db.execSQL(INSERT_INTO_CACHES + "VALUES ('GCCLICKEDLINK', 'from a link', '"
                 + Database.S0_INTENT + "', 0)");
         db.execSQL(INSERT_INTO_CACHES + "VALUES ('GCOLDIMPORT', 'from a gpx', 'bar.gpx', 1)");
         db.execSQL(INSERT_INTO_GPX + "VALUES ('nuke.gpx', '2009-04-30', 1)");
         db.execSQL(INSERT_INTO_GPX + "VALUES ('keep.gpx', '2009-04-30', 0)");
 
        CacheWriter cacheWriterSql = new CacheWriter(null, null);
        cacheWriterSql.clearEarlierLoads();
 
         assertEquals("GCTHISIMPORT|just loaded|||foo.gpx|1|0|0|0|0\n"
                 + "GCCLICKEDLINK|from a link|||intent|0|0|0|0|0\n", db.dumpTable("CACHES"));
         assertEquals("keep.gpx|2009-04-30|1\n", db.dumpTable("GPX"));
     }
 
     @Test
     public void testDeleteCache() {
         SQLiteWrapper sqlite = createMock(SQLiteWrapper.class);
 
         sqlite.execSQL(Database.SQL_DELETE_CACHE, "GC123");
 
         replay(sqlite);
         CacheWriter cacheWriterSql = new CacheWriter(null, null);
         cacheWriterSql.deleteCache("GC123");
         verify(sqlite);
     }
 
     @Test
     public void testInsertAndUpdate() {
         SQLiteWrapper sqlite = createMock(SQLiteWrapper.class);
         DbToGeocacheAdapter dbToGeocacheAdapter = createMock(DbToGeocacheAdapter.class);
 
         sqlite.execSQL(Database.SQL_REPLACE_CACHE, "gc123", "a cache", 122.0, 37.0, "source", 0, 0,
                 0, 0);
         expect(dbToGeocacheAdapter.sourceTypeToSourceName(Source.GPX, "source"))
                 .andReturn("source");
 
         replay(sqlite);
         replay(dbToGeocacheAdapter);
         CacheWriter cacheWriterSql = new CacheWriter(null, dbToGeocacheAdapter);
         cacheWriterSql.insertAndUpdateCache("gc123", "a cache", 122, 37, Source.GPX, "source",
                 CacheType.NULL, 0, 0, 0);
         verify(sqlite);
     }
 
     @Test
     public void testIsGpxAlreadyLoadedFalse() {
         SQLiteWrapper sqlite = createMock(SQLiteWrapper.class);
 
         expect(
                 sqlite.countResults(Database.TBL_GPX, "Name = ? AND ExportTime >= ?", "foo.gpx",
                         "04-30-2009")).andReturn(0);
 
         replay(sqlite);
         CacheWriter cacheWriterSql = new CacheWriter(null, null);
         assertFalse(cacheWriterSql.isGpxAlreadyLoaded("foo.gpx", "04-30-2009"));
         verify(sqlite);
     }
 
     @Test
     public void testIsGpxAlreadyLoadedTrue() {
         SQLiteWrapper sqlite = createMock(SQLiteWrapper.class);
 
         expect(
                 sqlite.countResults(Database.TBL_GPX, Database.SQL_MATCH_NAME_AND_EXPORTED_LATER,
                         "foo.gpx", "04-30-2009 10:30")).andReturn(1);
         sqlite.execSQL(Database.SQL_CACHES_DONT_DELETE_ME, "foo.gpx");
         sqlite.execSQL(Database.SQL_GPX_DONT_DELETE_ME, "foo.gpx");
         sqlite.execSQL(Database.SQL_REPLACE_GPX, "foo.gpx", "04-30-2009 10:30");
 
         replay(sqlite);
         CacheWriter cacheWriterSql = new CacheWriter(null, null);
         assertTrue(cacheWriterSql.isGpxAlreadyLoaded("foo.gpx", "04-30-2009 10:30"));
         cacheWriterSql.writeGpx("foo.gpx");
         verify(sqlite);
     }
 
     @Test
     public void testStartWriting() {
         SQLiteWrapper sqlite = createMock(SQLiteWrapper.class);
         sqlite.beginTransaction();
 
         replay(sqlite);
         new CacheWriter(null, null).startWriting();
         verify(sqlite);
     }
 
     @Test
     public void testStopWriting() {
         SQLiteWrapper sqlite = createMock(SQLiteWrapper.class);
         sqlite.setTransactionSuccessful();
         sqlite.endTransaction();
 
         replay(sqlite);
         new CacheWriter(null, null).stopWriting();
         verify(sqlite);
     }
 }
