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
 
 package com.google.code.geobeagle.xmlimport;
 
 import com.google.code.geobeagle.GeocacheFactory.Source;
 
 import java.io.IOException;
 
 public class EventHandlerGpx implements EventHandler {
    
     static final String XPATH_CACHE = "/gpx/wpt/groundspeak:cache";
     static final String XPATH_GEOCACHELOGDATE = "/gpx/wpt/geocache/logs/log/time";
     static final String XPATH_GPXNAME = "/gpx/name";
     static final String XPATH_WPTTIME = "/gpx/wpt/time";
     static final String XPATH_LOGTEXT = "/gpx/wpt/groundspeak:cache/groundspeak:logs/groundspeak:log/groundspeak:text";
     
     static final String[] XPATH_PLAINLINES = {
             "/gpx/wpt/cmt",
             "/gpx/wpt/groundspeak:cache/groundspeak:logs/groundspeak:log/groundspeak:finder",
             /* here are the geocaching.com.au entries */
             "/gpx/wpt/geocache/owner", "/gpx/wpt/geocache/type", "/gpx/wpt/geocache/summary",
             "/gpx/wpt/geocache/description", "/gpx/wpt/geocache/logs/log/geocacher",
             "/gpx/wpt/geocache/logs/log/type", "/gpx/wpt/geocache/logs/log/text"
 
     };
     static final String XPATH_SYM = "/gpx/wpt/sym";
     static final String XPATH_WPT = "/gpx/wpt";
     static final String XPATH_WPTDESC = "/gpx/wpt/desc";
     static final String XPATH_WPTNAME = "/gpx/wpt/name";
     
     private boolean mLogEncrypted;
 
     @Override
     public void endTag(String name, String previousFullPath,
             ICachePersisterFacade cachePersisterFacade) throws IOException {
         if (previousFullPath.equals(XPATH_WPT)) {
             cachePersisterFacade.endCache(Source.GPX);
         }
     }
 
     @Override
     public void startTag(String name, String fullPath, XmlPullParserWrapper xmlPullParser,
             ICachePersisterFacade cachePersisterFacade) {
         if (fullPath.equals(XPATH_WPT)) {
             cachePersisterFacade.startCache();
             cachePersisterFacade.wpt(xmlPullParser.getAttributeValue(null, "lat"), xmlPullParser
                     .getAttributeValue(null, "lon"));
         } else if (fullPath.equals(XPATH_CACHE)) {
             cachePersisterFacade.available(xmlPullParser.getAttributeValue(null, "available"));
             cachePersisterFacade.archived(xmlPullParser.getAttributeValue(null, "archived"));
         } else if (fullPath.equals(XPATH_LOGTEXT)) {
             mLogEncrypted = "true".equalsIgnoreCase(xmlPullParser
                     .getAttributeValue(null, "encoded"));
         }
     }
 
     @Override
     public boolean text(String fullPath, String text, XmlPullParserWrapper xmlPullParser,
             ICachePersisterFacade cachePersisterFacade) throws IOException {
         String trimmedText = text.trim();
         GpxPath gpxPath = GpxPath.fromString(fullPath);
         if (gpxPath != null) {
             gpxPath.text(trimmedText, cachePersisterFacade);
             return true;
         }
         if (fullPath.equals(XPATH_LOGTEXT)) {
             cachePersisterFacade.logText(trimmedText, mLogEncrypted);
         } else if (fullPath.equals(XPATH_WPTTIME)) {
             cachePersisterFacade.wptTime(trimmedText);
         }
         
         for (String writeLineMatch : XPATH_PLAINLINES) {
             if (fullPath.equals(writeLineMatch)) {
                 cachePersisterFacade.line(trimmedText);
                 return true;
             }
         }
         return true;
     }
     
     @Override
     public void open(String filename) throws IOException {
     }
 }
