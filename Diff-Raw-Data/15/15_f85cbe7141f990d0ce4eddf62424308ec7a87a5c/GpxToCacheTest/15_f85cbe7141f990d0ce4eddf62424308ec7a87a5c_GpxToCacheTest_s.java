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
import static org.junit.Assert.*;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 
 import com.google.code.geobeagle.io.GpxToCache.Aborter;
 import com.google.code.geobeagle.io.GpxToCache.CancelException;
 import com.google.code.geobeagle.io.GpxToCacheDI.XmlPullParserWrapper;
 
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.powermock.api.easymock.PowerMock;
 import org.powermock.core.classloader.annotations.PrepareForTest;
 import org.powermock.modules.junit4.PowerMockRunner;
 import org.xmlpull.v1.XmlPullParser;
 import org.xmlpull.v1.XmlPullParserException;
 
 import java.io.IOException;
 import java.io.Reader;
 import java.text.ParseException;
 
 @RunWith(PowerMockRunner.class)
 @PrepareForTest( {
     GpxToCache.class
 })
 public class GpxToCacheTest {
 
     @Test
     public void testGetSource() {
         XmlPullParserWrapper xmlPullParserWrapper = PowerMock
                 .createMock(XmlPullParserWrapper.class);
 
         expect(xmlPullParserWrapper.getSource()).andReturn("/my/path");
 
         PowerMock.replayAll();
         GpxToCache gpxToCache = new GpxToCache(xmlPullParserWrapper, new Aborter());
         assertEquals("/my/path", gpxToCache.getSource());
         PowerMock.verifyAll();
     }
 
     @Test
     public void testLoadAbort() throws XmlPullParserException, IOException, ParseException {
         XmlPullParserWrapper xmlPullParser = PowerMock.createMock(XmlPullParserWrapper.class);
         EventHelper eventHelper = PowerMock.createMock(EventHelper.class);
 
         expect(xmlPullParser.getEventType()).andReturn(XmlPullParser.START_DOCUMENT);
 
         PowerMock.replayAll();
         GpxToCache gpxToCache = new GpxToCache(xmlPullParser, new Aborter());
         gpxToCache.abort();
         try {
             gpxToCache.load(eventHelper);
             assertFalse("expected to throw cancel exception", false);
         } catch (CancelException e) {
         }
         PowerMock.verifyAll();
     }
 
     @Test
     public void testLoadNone() throws XmlPullParserException, IOException, ParseException,
             CancelException {
         XmlPullParserWrapper xmlPullParser = PowerMock.createMock(XmlPullParserWrapper.class);
         EventHelper eventHelper = PowerMock.createMock(EventHelper.class);
 
         expect(xmlPullParser.getEventType()).andReturn(XmlPullParser.END_DOCUMENT);
         expect(eventHelper.handleEvent(XmlPullParser.END_DOCUMENT)).andReturn(true);
 
         PowerMock.replayAll();
         GpxToCache gpxToCache = new GpxToCache(xmlPullParser, new Aborter());
         assertEquals(false, gpxToCache.load(eventHelper));
         PowerMock.verifyAll();
     }
 
     @Test
     public void testLoadOne() throws XmlPullParserException, IOException, ParseException,
             CancelException {
         XmlPullParserWrapper xmlPullParser = PowerMock.createMock(XmlPullParserWrapper.class);
         EventHelper eventHelper = PowerMock.createMock(EventHelper.class);
 
         expect(xmlPullParser.getEventType()).andReturn(XmlPullParser.START_DOCUMENT);
         expect(eventHelper.handleEvent(XmlPullParser.START_DOCUMENT)).andReturn(true);
         expect(xmlPullParser.next()).andReturn(XmlPullParser.END_DOCUMENT);
         expect(eventHelper.handleEvent(XmlPullParser.END_DOCUMENT)).andReturn(true);
 
         PowerMock.replayAll();
         GpxToCache gpxToCache = new GpxToCache(xmlPullParser, new Aborter());
         assertEquals(false, gpxToCache.load(eventHelper));
         PowerMock.verifyAll();
     }
 
     @Test
     public void testAborterReset() {
         final Aborter aborter = new Aborter();
         assertFalse(aborter.isAborted());
         aborter.abort();
         assertTrue(aborter.isAborted());
         aborter.reset();
         assertFalse(aborter.isAborted());
     }
 
     @Test
     public void testLoadSkipThisFile() throws XmlPullParserException, IOException, ParseException,
             CancelException {
         XmlPullParserWrapper xmlPullParser = PowerMock.createMock(XmlPullParserWrapper.class);
         EventHelper eventHelper = PowerMock.createMock(EventHelper.class);
 
         expect(xmlPullParser.getEventType()).andReturn(XmlPullParser.START_DOCUMENT);
         expect(eventHelper.handleEvent(XmlPullParser.START_DOCUMENT)).andReturn(false);
 
         PowerMock.replayAll();
         GpxToCache gpxToCache = new GpxToCache(xmlPullParser, new Aborter());
         assertEquals(true, gpxToCache.load(eventHelper));
         PowerMock.verifyAll();
     }
 
     @Test
     public void testLoadTwo() throws XmlPullParserException, IOException, ParseException,
             CancelException {
         XmlPullParserWrapper xmlPullParser = PowerMock.createMock(XmlPullParserWrapper.class);
         EventHelper eventHelper = PowerMock.createMock(EventHelper.class);
 
         expect(xmlPullParser.getEventType()).andReturn(XmlPullParser.START_DOCUMENT);
         expect(eventHelper.handleEvent(XmlPullParser.START_DOCUMENT)).andReturn(true);
 
         expect(xmlPullParser.next()).andReturn(XmlPullParser.START_TAG);
         expect(eventHelper.handleEvent(XmlPullParser.START_TAG)).andReturn(true);
 
         expect(xmlPullParser.next()).andReturn(XmlPullParser.START_TAG);
         expect(eventHelper.handleEvent(XmlPullParser.START_TAG)).andReturn(true);
         expect(xmlPullParser.next()).andReturn(XmlPullParser.END_DOCUMENT);
         expect(eventHelper.handleEvent(XmlPullParser.END_DOCUMENT)).andReturn(true);
 
         PowerMock.replayAll();
         assertEquals(false, new GpxToCache(xmlPullParser, new Aborter()).load(eventHelper));
         PowerMock.verifyAll();
     }
 
     @Test
     public void testOpen() throws Exception {
         XmlPullParserWrapper xmlPullParserWrapper = PowerMock
                 .createMock(XmlPullParserWrapper.class);
         Reader reader = PowerMock.createMock(Reader.class);
 
         xmlPullParserWrapper.open("/my/path", reader);
 
         PowerMock.replayAll();
         GpxToCache gpxToCache = new GpxToCache(xmlPullParserWrapper, new Aborter());
         gpxToCache.open("/my/path", reader);
         PowerMock.verifyAll();
     }
 }
