 
 package com.google.code.geobeagle.io;
 
 import static org.easymock.EasyMock.expect;
 import static org.easymock.classextension.EasyMock.createMock;
 import static org.easymock.classextension.EasyMock.replay;
 import static org.easymock.classextension.EasyMock.verify;
 
 import com.google.code.geobeagle.io.CacheDetailsWriter.CacheDetailsWriterFactory;
 import com.google.code.geobeagle.io.CachePersisterFacade.Cache;
 import com.google.code.geobeagle.io.Database.CacheWriter;
 import com.google.code.geobeagle.io.di.GpxImporterDI;
 import com.google.code.geobeagle.io.di.HtmlWriterFactory;
 import com.google.code.geobeagle.io.di.CachePersisterFacadeDI.FileFactory;
 import com.google.code.geobeagle.io.di.GpxImporterDI.MessageHandler;
 import com.google.code.geobeagle.io.di.GpxToCacheDI.XmlPullParserWrapper;
 
 import android.os.PowerManager.WakeLock;
 
 import java.io.File;
 import java.io.IOException;
 
 import junit.framework.TestCase;
 
 public class CachePersisterFacadeTest extends TestCase {
 
     public void testCache() {
         Cache cache = new Cache("id", "name", 37, -122);
         assertEquals("id", cache.mId);
         assertEquals("name", cache.mName);
         assertEquals(37.0, cache.mLatitude);
         assertEquals(-122.0, cache.mLongitude);
     }
 
     public void testCacheEmptyConstructor() {
         Cache cache = new Cache();
         assertEquals("", cache.mId);
         assertEquals("", cache.mName);
     }
 
     public void testClose() {
         CacheWriter cacheWriter = createMock(CacheWriter.class);
         cacheWriter.stopWriting();
 
         replay(cacheWriter);
         new CachePersisterFacade(cacheWriter, null, null, null, null, null, null, null).close();
         verify(cacheWriter);
     }
 
     public void testEndTag() throws IOException {
         CacheDetailsWriter cacheDetailsWriter = createMock(CacheDetailsWriter.class);
         cacheDetailsWriter.writeEndTag();
         CacheWriter cacheWriter = createMock(CacheWriter.class);
 
         cacheWriter.insertAndUpdateCache("GC1234", "blinkermania", 37, -122);
 
         replay(cacheWriter);
         replay(cacheDetailsWriter);
         Cache cache = new Cache();
         cache.mId = "GC1234";
         cache.mName = "blinkermania";
         cache.mLatitude = 37;
         cache.mLongitude = -122;
         cache.mSymbol = "Geocache";
         CachePersisterFacade cachePersisterFacade = new CachePersisterFacade(cacheWriter, null,
                 null, cacheDetailsWriter, null, null, cache, null);
         cachePersisterFacade.endTag();
         verify(cacheDetailsWriter);
         verify(cacheWriter);
     }
 
     public void testEndTagNotCache() throws IOException {
         GpxEventHandler gpxEventHandler = new GpxEventHandler(null);
         gpxEventHandler.endTag("/gpx/wptNOT!");
     }
 
     public void testGroundspeakName() throws IOException {
         Cache cache = createMock(Cache.class);
         new CachePersisterFacade(null, null, null, null, null, null, cache, null)
                 .groundspeakName("GC12");
         assertEquals("GC12", cache.mName);
     }
 
     public void testHint() throws IOException {
         CacheDetailsWriter cacheDetailsWriter = createMock(CacheDetailsWriter.class);
 
         cacheDetailsWriter.writeHint("a hint");
 
         replay(cacheDetailsWriter);
         new CachePersisterFacade(null, null, null, cacheDetailsWriter, null, null, null, null)
                 .hint("a hint");
         verify(cacheDetailsWriter);
     }
 
     public void testLine() throws IOException {
         CacheDetailsWriter cacheDetailsWriter = createMock(CacheDetailsWriter.class);
         cacheDetailsWriter.writeLine("some data");
 
         replay(cacheDetailsWriter);
         new CachePersisterFacade(null, null, null, cacheDetailsWriter, null, null, null, null)
                 .line("some data");
         verify(cacheDetailsWriter);
     }
 
     public void testLogDate() throws IOException {
         CacheDetailsWriter cacheDetailsWriter = createMock(CacheDetailsWriter.class);
         Cache cache = new Cache();
         cacheDetailsWriter.writeLogDate("04/30/99");
 
         replay(cacheDetailsWriter);
         new CachePersisterFacade(null, null, null, cacheDetailsWriter, null, null, cache, null)
                 .logDate("04/30/99");
         verify(cacheDetailsWriter);
     }
 
     public void testStart() {
         FileFactory fileFactory = createMock(FileFactory.class);
         File file = createMock(File.class);
        CacheWriter cacheWriter = createMock(CacheWriter.class);
 
         expect(fileFactory.createFile(CachePersisterFacade.GEOBEAGLE_DIR)).andReturn(file);
         expect(file.mkdirs()).andReturn(true);
        cacheWriter.clearAllImportedCaches();
 
         replay(fileFactory);
         replay(file);
        CachePersisterFacade cachePersisterFacade = new CachePersisterFacade(cacheWriter,
                fileFactory, null, null, null, null, null, null);
         cachePersisterFacade.start();
         verify(fileFactory);
         verify(file);
     }
 
     public void testSymbol() throws IOException {
         CacheDetailsWriter cacheDetailsWriter = createMock(CacheDetailsWriter.class);
 
         replay(cacheDetailsWriter);
         Cache cache = new Cache();
         new CachePersisterFacade(null, null, null, cacheDetailsWriter, null, null, cache, null)
                 .symbol("Geocache Found");
         assertEquals("Geocache Found", cache.mSymbol);
         verify(cacheDetailsWriter);
     }
 
     public void testWpt() throws IOException {
         XmlPullParserWrapper xmlPullParser = createMock(XmlPullParserWrapper.class);
         Cache cache = new Cache();
         expect(xmlPullParser.getAttributeValue(null, "lat")).andReturn("37");
         expect(xmlPullParser.getAttributeValue(null, "lon")).andReturn("122");
 
         replay(xmlPullParser);
         new CachePersisterFacade(null, null, null, null, null, null, cache, null)
                 .wpt(xmlPullParser);
         verify(xmlPullParser);
     }
 
     public void testWptName() throws IOException {
         HtmlWriterFactory htmlWriterFactory = createMock(HtmlWriterFactory.class);
         HtmlWriter htmlWriter = createMock(HtmlWriter.class);
         CacheDetailsWriterFactory cacheDetailsWriterFactory = createMock(CacheDetailsWriterFactory.class);
         CacheDetailsWriter cacheDetailsWriter = createMock(CacheDetailsWriter.class);
         CacheWriter cacheWriter = createMock(CacheWriter.class);
         GpxImporterDI.MessageHandler messageHandler = createMock(MessageHandler.class);
         WakeLock wakeLock = createMock(WakeLock.class);
 
         Cache cache = new Cache();
         cache.mLatitude = 122;
         cache.mLongitude = 37;
         cache.mName = "a little cache";
//        cacheWriter.clearCaches("foo.gpx");
         cacheWriter.startWriting();
         expect(htmlWriterFactory.create(CachePersisterFacade.GEOBEAGLE_DIR + "/GC123.html"))
                 .andReturn(htmlWriter);
         expect(cacheDetailsWriterFactory.create(htmlWriter)).andReturn(cacheDetailsWriter);
         cacheDetailsWriter.writeWptName("GC123", 122, 37);
         messageHandler.workerSendUpdate("1: foo.gpx - GC123 - a little cache");
 
         replay(htmlWriterFactory);
         replay(htmlWriter);
         replay(cacheDetailsWriterFactory);
         replay(cacheDetailsWriter);
         replay(messageHandler);
         replay(cacheWriter);
         CachePersisterFacade cachePersisterFacade = new CachePersisterFacade(cacheWriter, null,
                 cacheDetailsWriterFactory, cacheDetailsWriter, htmlWriterFactory, messageHandler,
                 cache, wakeLock);
         cachePersisterFacade.open("foo.gpx");
         cachePersisterFacade.wptName("GC123");
         wakeLock.acquire(CachePersisterFacade.WAKELOCK_DURATION);
         verify(htmlWriterFactory);
         verify(htmlWriter);
         verify(cacheDetailsWriterFactory);
         verify(cacheDetailsWriter);
         verify(messageHandler);
         verify(cacheWriter);
     }
 }
