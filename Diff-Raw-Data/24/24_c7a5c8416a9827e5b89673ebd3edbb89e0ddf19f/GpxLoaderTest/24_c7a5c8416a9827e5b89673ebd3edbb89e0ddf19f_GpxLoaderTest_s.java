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
 
 package com.google.code.geobeagle.io;
 
 import static org.easymock.EasyMock.expect;
 import static org.easymock.EasyMock.expectLastCall;
 import static org.easymock.classextension.EasyMock.createMock;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 
 import com.google.code.geobeagle.R;
 import com.google.code.geobeagle.io.GpxToCache.CancelException;
 import com.google.code.geobeagle.ui.ErrorDisplayer;
 
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.powermock.api.easymock.PowerMock;
 import org.powermock.modules.junit4.PowerMockRunner;
 import org.xmlpull.v1.XmlPullParserException;
 
 import android.database.sqlite.SQLiteException;
 
 import java.io.IOException;
 import java.io.Reader;
 import java.text.ParseException;
 
 @RunWith(PowerMockRunner.class)
 public class GpxLoaderTest {
 
     private <T> void loadRaiseAndDisplayCustomMessage(Class<T> exceptionClass, int errorResource)
             throws XmlPullParserException, IOException, ParseException, CancelException {
         CachePersisterFacade cachePersisterFacade = PowerMock
                 .createMock(CachePersisterFacade.class);
         GpxToCache gpxToCache = PowerMock.createMock(GpxToCache.class);
         Throwable e = (Throwable)PowerMock.createMock(exceptionClass);
         ErrorDisplayer errorDisplayer = PowerMock.createMock(ErrorDisplayer.class);
         EventHelper eventHelper = PowerMock.createMock(EventHelper.class);
 
         expect(gpxToCache.load(eventHelper)).andStubReturn(false);
         expectLastCall().andThrow(e);
         expect(gpxToCache.getSource()).andReturn("foo.gpx");
         expect(e.fillInStackTrace()).andReturn(e);
         errorDisplayer.displayError(errorResource, "foo.gpx");
         cachePersisterFacade.close(false);
 
         PowerMock.replayAll();
         assertFalse(new GpxLoader(gpxToCache, cachePersisterFacade, errorDisplayer)
                 .load(eventHelper));
         PowerMock.verifyAll();
     }
 
     private <T> void loadRaiseAndDisplayExceptionMessage(int errorMessage, Class<T> exceptionClass)
             throws XmlPullParserException, IOException, ParseException, CancelException {
         CachePersisterFacade cachePersisterFacade = createMock(CachePersisterFacade.class);
         GpxToCache gpxToCache = PowerMock.createMock(GpxToCache.class);
         Throwable e = (Throwable)PowerMock.createMock(exceptionClass);
         ErrorDisplayer errorDisplayer = PowerMock.createMock(ErrorDisplayer.class);
         EventHelper eventHelper = PowerMock.createMock(EventHelper.class);
 
         gpxToCache.load(eventHelper);
         expectLastCall().andThrow(e);
         expect(e.getMessage()).andReturn("a problem of some sort");
         expect(e.fillInStackTrace()).andReturn(e);
        errorDisplayer.displayError(errorMessage, "a problem of some sort");
         cachePersisterFacade.close(false);
 
         PowerMock.replayAll();
         assertFalse(new GpxLoader(gpxToCache, cachePersisterFacade, errorDisplayer)
                 .load(eventHelper));
         PowerMock.verifyAll();
     }
 
     private <T> void loadRaiseAndDisplayNothing(Class<T> exceptionClass)
             throws XmlPullParserException, IOException, ParseException, CancelException {
         CachePersisterFacade cachePersisterFacade = PowerMock
                 .createMock(CachePersisterFacade.class);
         GpxToCache gpxToCache = PowerMock.createMock(GpxToCache.class);
         Throwable e = (Throwable)PowerMock.createMock(exceptionClass);
         ErrorDisplayer errorDisplayer = PowerMock.createMock(ErrorDisplayer.class);
         EventHelper eventHelper = PowerMock.createMock(EventHelper.class);
 
         expect(gpxToCache.load(eventHelper)).andStubReturn(false);
         expectLastCall().andThrow(e);
         expect(e.fillInStackTrace()).andReturn(e);
         cachePersisterFacade.close(false);
 
         PowerMock.replayAll();
         assertFalse(new GpxLoader(gpxToCache, cachePersisterFacade, errorDisplayer)
                 .load(eventHelper));
         PowerMock.verifyAll();
     }
 
     @Test
     public void testAbortLoad() throws XmlPullParserException, IOException {
         GpxToCache gpxToCache = PowerMock.createMock(GpxToCache.class);
 
         gpxToCache.abort();
 
         PowerMock.replayAll();
         new GpxLoader(gpxToCache, null, null).abort();
         PowerMock.verifyAll();
     }
 
     @Test
     public void testEnd() {
         CachePersisterFacade cachePersisterFacade = PowerMock
                 .createMock(CachePersisterFacade.class);
 
         cachePersisterFacade.end();
 
         PowerMock.replayAll();
         new GpxLoader(null, cachePersisterFacade, null).end();
         PowerMock.verifyAll();
     }
 
     @Test
     public void testLoad() throws XmlPullParserException, IOException, ParseException,
             CancelException {
         CachePersisterFacade cachePersisterFacade = PowerMock
                 .createMock(CachePersisterFacade.class);
         GpxToCache gpxToCache = PowerMock.createMock(GpxToCache.class);
         EventHelper eventHelper = PowerMock.createMock(EventHelper.class);
 
         expect(gpxToCache.load(eventHelper)).andReturn(false);
         cachePersisterFacade.close(true);
 
         PowerMock.replayAll();
         assertTrue(new GpxLoader(gpxToCache, cachePersisterFacade, null).load(eventHelper));
         PowerMock.verifyAll();
     }
 
     @Test
     public void testLoadAlreadyLoaded() throws XmlPullParserException, IOException, ParseException,
             CancelException {
         CachePersisterFacade cachePersisterFacade = PowerMock
                 .createMock(CachePersisterFacade.class);
         GpxToCache gpxToCache = PowerMock.createMock(GpxToCache.class);
         EventHelper eventHelper = PowerMock.createMock(EventHelper.class);
 
         expect(gpxToCache.load(eventHelper)).andReturn(true);
         cachePersisterFacade.close(false);
 
         PowerMock.replayAll();
         assertTrue(new GpxLoader(gpxToCache, cachePersisterFacade, null).load(eventHelper));
         PowerMock.verifyAll();
     }
 
     @Test
     public void testLoadCancelException() throws XmlPullParserException, IOException,
             ParseException, CancelException {
         loadRaiseAndDisplayNothing(CancelException.class);
     }
 
     @Test
     public void testLoadIOException() throws XmlPullParserException, IOException, ParseException,
             CancelException {
         loadRaiseAndDisplayCustomMessage(IOException.class, R.string.error_reading_file);
     }
 
     @Test
     public void testLoadPullParserException() throws XmlPullParserException, IOException,
             ParseException, CancelException {
         loadRaiseAndDisplayExceptionMessage(R.string.error_parsing_file,
                 XmlPullParserException.class);
     }
 
     @Test
     public void testLoadSqliteException() throws XmlPullParserException, IOException,
             ParseException, CancelException {
         loadRaiseAndDisplayExceptionMessage(R.string.error_writing_cache, SQLiteException.class);
     }
 
     @Test
     public void testOpen() throws XmlPullParserException, IOException {
         CachePersisterFacade cachePersisterFacade = PowerMock
                 .createMock(CachePersisterFacade.class);
         GpxToCache gpxToCache = PowerMock.createMock(GpxToCache.class);
         Reader reader = PowerMock.createMock(Reader.class);
 
         gpxToCache.open("/sdcard/foo.gpx", reader);
         cachePersisterFacade.open("/sdcard/foo.gpx");
 
         PowerMock.replayAll();
         new GpxLoader(gpxToCache, cachePersisterFacade, null).open("/sdcard/foo.gpx", reader);
         PowerMock.verifyAll();
     }
 
     @Test
     public void testStart() {
         CachePersisterFacade cachePersisterFacade = PowerMock
                 .createMock(CachePersisterFacade.class);
 
         cachePersisterFacade.start();
 
         PowerMock.replayAll();
         new GpxLoader(null, cachePersisterFacade, null).start();
         PowerMock.verifyAll();
     }
 }
