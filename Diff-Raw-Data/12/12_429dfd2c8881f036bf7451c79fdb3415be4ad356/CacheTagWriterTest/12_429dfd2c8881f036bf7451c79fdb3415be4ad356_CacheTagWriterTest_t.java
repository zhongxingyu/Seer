 
 package com.google.code.geobeagle.xmlimport;
 
 import static org.easymock.EasyMock.expect;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 
 import com.google.code.geobeagle.CacheType;
 import com.google.code.geobeagle.CacheTypeFactory;
 import com.google.code.geobeagle.GeocacheFactory.Source;
 import com.google.code.geobeagle.database.CacheWriter;
 import com.google.code.geobeagle.database.ClearCachesFromSource;
 import com.google.code.geobeagle.database.Tag;
 import com.google.code.geobeagle.database.TagWriterImpl;
 import com.google.code.geobeagle.database.TagWriterNull;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.powermock.api.easymock.PowerMock;
 import org.powermock.modules.junit4.PowerMockRunner;
 
 @RunWith(PowerMockRunner.class)
 public class CacheTagWriterTest {
     private CacheWriter cacheWriter;
     private TagWriterNull tagWriterNull;
     private TagWriterImpl tagWriterImpl;
     private CacheTypeFactory cacheTypeFactory;
     private CacheTagSqlWriter cacheTagSqlWriter;
     private ClearCachesFromSource clearCachesFromSource;
 
     @Before
     public void setUp() {
         cacheWriter = PowerMock.createMock(CacheWriter.class);
         tagWriterNull = PowerMock.createMock(TagWriterNull.class);
         tagWriterImpl = PowerMock.createMock(TagWriterImpl.class);
         cacheTypeFactory = PowerMock.createMock(CacheTypeFactory.class);
         clearCachesFromSource = PowerMock.createMock(ClearCachesFromSource.class);
         cacheTagSqlWriter = new CacheTagSqlWriter(cacheWriter, cacheTypeFactory, tagWriterImpl,
                 tagWriterNull, clearCachesFromSource);
     }
 
     @Test
     public void testClear() {
         cacheWriter.insertAndUpdateCache(null, null, 0, 0, Source.GPX, null, CacheType.NULL, 0, 0,
                 0);
         tagWriterNull.add(null, Tag.FOUND);
 
         PowerMock.replayAll();
         cacheTagSqlWriter.clear();
         cacheTagSqlWriter.write(Source.GPX);
         PowerMock.verifyAll();
     }
 
     @Test
     public void testEnd() {
        clearCachesFromSource.clearEarlierLoads();
 
         PowerMock.replayAll();
         cacheTagSqlWriter.end();
         PowerMock.verifyAll();
     }
 
     @Test
     public void testSymbol() {
         cacheWriter.insertAndUpdateCache(null, null, 0, 0, Source.GPX, null, null, 0, 0, 0);
         tagWriterImpl.add(null, Tag.FOUND);
 
         PowerMock.replayAll();
         cacheTagSqlWriter.symbol("Geocache Found");
         cacheTagSqlWriter.write(Source.GPX);
         PowerMock.verifyAll();
     }
 
     @Test
     public void testGpxTimeDontLoad() {
         expect(cacheWriter.isGpxAlreadyLoaded("foo.gpx", "2008-04-15 16:10:30")).andReturn(true);
 
         PowerMock.replayAll();
         cacheTagSqlWriter.gpxName("foo.gpx");
         assertFalse(cacheTagSqlWriter.gpxTime("2008-04-15T16:10:30"));
         PowerMock.verifyAll();
     }
 
     @Test
     public void testGpxTimeLoad() {
         expect(cacheWriter.isGpxAlreadyLoaded("foo.gpx", "2008-04-15 16:10:30")).andReturn(false);
         clearCachesFromSource.clearCaches("foo.gpx");
 
         PowerMock.replayAll();
         cacheTagSqlWriter.gpxName("foo.gpx");
         assertTrue(cacheTagSqlWriter.gpxTime("2008-04-15T16:10:30"));
         PowerMock.verifyAll();
     }
 
     @Test
     public void testIsoTimeToSql() {
         assertEquals("2008-04-15 16:10:30", cacheTagSqlWriter
                 .isoTimeToSql("2008-04-15T16:10:30.7369220-08:00"));
     }
 
     @Test
     public void testStartWriting() {
         cacheWriter.startWriting();
 
         PowerMock.replayAll();
         cacheTagSqlWriter.startWriting();
         PowerMock.verifyAll();
     }
 
     @Test
     public void testStopWritingFailure() {
         cacheWriter.stopWriting();
 
         PowerMock.replayAll();
         cacheTagSqlWriter.stopWriting(false);
         PowerMock.verifyAll();
     }
 
     @Test
     public void testStopWritingSuccess() {
         cacheWriter.stopWriting();
         expect(cacheWriter.isGpxAlreadyLoaded("foo.gpx", "2008-04-15 16:10:30")).andReturn(true);
         cacheWriter.writeGpx("foo.gpx");
 
         PowerMock.replayAll();
         cacheTagSqlWriter.gpxName("foo.gpx");
         cacheTagSqlWriter.gpxTime("2008-04-15T16:10:30.7369220-08:00");
         cacheTagSqlWriter.stopWriting(true);
         PowerMock.verifyAll();
     }
 
     @Test
     public void testWrite() {
         cacheWriter.insertAndUpdateCache("GC123", "my cache", 122, 37, Source.GPX, "foo.gpx",
                 CacheType.TRADITIONAL, 6, 5, 1);
         expect(cacheTypeFactory.container("Micro")).andReturn(1);
         expect(cacheTypeFactory.stars("2.5")).andReturn(5);
         expect(cacheTypeFactory.stars("3")).andReturn(6);
         expect(cacheTypeFactory.fromTag("Traditional Cache")).andReturn(CacheType.TRADITIONAL);
         tagWriterNull.add("GC123", Tag.FOUND);
 
         PowerMock.replayAll();
         cacheTagSqlWriter.id("GC123");
         cacheTagSqlWriter.cacheName("my cache");
         cacheTagSqlWriter.latitudeLongitude("122", "37");
         cacheTagSqlWriter.container("Micro");
         cacheTagSqlWriter.terrain("2.5");
         cacheTagSqlWriter.difficulty("3");
 
         cacheTagSqlWriter.gpxName("foo.gpx");
         cacheTagSqlWriter.cacheType("Traditional Cache");
         cacheTagSqlWriter.write(Source.GPX);
         PowerMock.verifyAll();
     }
 }
