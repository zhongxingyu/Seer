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
 
 import static org.easymock.EasyMock.eq;
 import static org.easymock.EasyMock.expect;
 import static org.easymock.EasyMock.isNull;
 import static org.junit.Assert.assertEquals;
 
 import com.google.code.geobeagle.CacheType;
 import com.google.code.geobeagle.Geocache;
 import com.google.code.geobeagle.GeocacheFactory;
 import com.google.code.geobeagle.GeocacheFactory.Source;
 import com.google.code.geobeagle.database.DatabaseDI.CacheReaderCursorFactory;
 import com.google.code.geobeagle.database.DatabaseDI.SQLiteWrapper;
import com.google.inject.Provider;
 
 import org.easymock.EasyMock;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.powermock.api.easymock.PowerMock;
 import org.powermock.modules.junit4.PowerMockRunner;
 
 import android.database.Cursor;
 
 @RunWith(PowerMockRunner.class)
 public class CacheReaderTest {
 
     private void expectQuery(SQLiteWrapper sqliteWrapper, Cursor cursor, String where) {
         expect(
                 sqliteWrapper.query(eq("CACHES"), eq(CacheReader.READER_COLUMNS), eq(where),
                         (String)isNull(), (String)isNull(), (String)isNull(), (String)isNull()))
                 .andReturn(cursor);
     }
 
     @Test
     public void testCursorClose() {
         Cursor cursor = PowerMock.createMock(Cursor.class);
 
         cursor.close();
 
         PowerMock.replayAll();
         new CacheReaderCursor(cursor, null, null).close();
         PowerMock.verifyAll();
     }
 
     @Test
     public void testCursorGetCache() {
         Cursor cursor = PowerMock.createMock(Cursor.class);
         GeocacheFactory geocacheFactory = PowerMock.createMock(GeocacheFactory.class);
         Geocache geocache = PowerMock.createMock(Geocache.class);
         DbToGeocacheAdapter dbToGeocacheAdapter = PowerMock.createMock(DbToGeocacheAdapter.class);
 
         expect(cursor.getDouble(0)).andReturn(122.0);
         expect(cursor.getDouble(1)).andReturn(37.0);
         expect(cursor.getString(2)).andReturn("GC123");
         expect(cursor.getString(3)).andReturn("name");
         expect(cursor.getString(4)).andReturn("cupertino");
         expect(cursor.getString(5)).andReturn("0");
         expect(cursor.getString(6)).andReturn("0");
         expect(cursor.getString(7)).andReturn("0");
         expect(cursor.getString(8)).andReturn("0");
         expect(geocacheFactory.cacheTypeFromInt(0)).andReturn(CacheType.TRADITIONAL);
 
         expect(dbToGeocacheAdapter.sourceNameToSourceType("cupertino")).andReturn(Source.GPX);
         expect(
                 geocacheFactory.create("GC123", "name", 122.0, 37.0, Source.GPX, "cupertino",
                         CacheType.TRADITIONAL, 0, 0, 0)).andReturn(geocache);
 
         PowerMock.replayAll();
         assertEquals(geocache, new CacheReaderCursor(cursor, geocacheFactory, dbToGeocacheAdapter)
                 .getCache());
         PowerMock.verifyAll();
     }
 
     @Test
     public void testGetCount() {
         Cursor cursor = PowerMock.createMock(Cursor.class);
         EasyMock.expect(cursor.getCount()).andReturn(27);
 
         PowerMock.replayAll();
         assertEquals(27, new CacheReaderCursor(cursor, null, null).count());
         PowerMock.verifyAll();
 
     }
 
     @Test
     public void testCursorMoveToNext() {
         Cursor cursor = PowerMock.createMock(Cursor.class);
 
         expect(cursor.moveToNext()).andReturn(true);
 
         PowerMock.replayAll();
         new CacheReaderCursor(cursor, null, null).moveToNext();
         PowerMock.verifyAll();
     }
 
    @SuppressWarnings("unchecked")
     @Test
     public void testGetTotalCount() {
         SQLiteWrapper sqliteWrapper = PowerMock.createMock(SQLiteWrapper.class);
         Cursor cursor = PowerMock.createMock(Cursor.class);
        Provider<ISQLiteDatabase> databaseProvider = PowerMock.createMock(Provider.class);
 
        expect(databaseProvider.get()).andReturn(sqliteWrapper);
         expect(sqliteWrapper.rawQuery("SELECT COUNT(*) FROM " + Database.TBL_CACHES, null))
                 .andReturn(cursor);
         expect(cursor.moveToFirst()).andReturn(true);
         expect(cursor.getInt(0)).andReturn(812);
         cursor.close();
 
         PowerMock.replayAll();
        assertEquals(812, new CacheReader(databaseProvider, null).getTotalCount());
         PowerMock.verifyAll();
     }
 
    @SuppressWarnings("unchecked")
     @Test
     public void testOpen() {
         WhereFactory whereFactoryNearestCaches = PowerMock
                 .createMock(WhereFactoryNearestCaches.class);
         SQLiteWrapper sqliteWrapper = PowerMock.createMock(SQLiteWrapper.class);
         Cursor cursor = PowerMock.createMock(Cursor.class);
         DatabaseDI.CacheReaderCursorFactory cacheReaderCursorFactory = PowerMock
                 .createMock(CacheReaderCursorFactory.class);
         CacheReaderCursor cacheReaderCursor = PowerMock.createMock(CacheReaderCursor.class);
        Provider<ISQLiteDatabase> databaseProvider = PowerMock.createMock(Provider.class);
 
        expect(databaseProvider.get()).andReturn(sqliteWrapper);
         String where = "Latitude > something AND Longitude < somethingelse";
         expect(whereFactoryNearestCaches.getWhere(sqliteWrapper, 122, 37)).andReturn(where);
         expectQuery(sqliteWrapper, cursor, where);
         expect(cursor.moveToFirst()).andReturn(true);
         expect(cacheReaderCursorFactory.create(cursor)).andReturn(cacheReaderCursor);
 
         PowerMock.replayAll();
        assertEquals(cacheReaderCursor, new CacheReader(databaseProvider, cacheReaderCursorFactory)
                .open(122, 37, whereFactoryNearestCaches, null));
         PowerMock.verifyAll();
     }
 
    @SuppressWarnings("unchecked")
     @Test
     public void testOpenEmpty() {
         SQLiteWrapper sqliteWrapper = PowerMock.createMock(SQLiteWrapper.class);
         Cursor cursor = PowerMock.createMock(Cursor.class);
         WhereFactory whereFactoryNearestCaches = PowerMock
                 .createMock(WhereFactoryNearestCaches.class);
        Provider<ISQLiteDatabase> databaseProvider = PowerMock.createMock(Provider.class);
 
        expect(databaseProvider.get()).andReturn(sqliteWrapper);
         expect(whereFactoryNearestCaches.getWhere(sqliteWrapper, 0, 0)).andReturn("a=b");
         expectQuery(sqliteWrapper, cursor, "a=b");
         expect(cursor.moveToFirst()).andReturn(false);
         cursor.close();
 
         PowerMock.replayAll();
        new CacheReader(databaseProvider, null).open(0, 0, whereFactoryNearestCaches, null);
         PowerMock.verifyAll();
     }
 }
