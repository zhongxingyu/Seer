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
 
 import com.google.code.geobeagle.io.CacheDetailsWriter.CacheDetailsWriterFactory;
 import com.google.code.geobeagle.io.LoadGpx.Cache;
 
 import org.xmlpull.v1.XmlPullParser;
 
 import java.io.IOException;
 
 public class GpxEventHandler {
     private final Cache mCache;
     private CacheDetailsWriter mCacheDetailsWriter;
     private final CacheDetailsWriterFactory mCacheDetailsWriterFactory;
     private final String mWriteLineMatches[] = {
             "/gpx/wpt/desc", "/gpx/wpt/groundspeak:cache/groundspeak:type",
             "/gpx/wpt/groundspeak:cache/groundspeak:container",
             "/gpx/wpt/groundspeak:cache/groundspeak:short_description",
             "/gpx/wpt/groundspeak:cache/groundspeak:long_description",
             "/gpx/wpt/groundspeak:cache/groundspeak:logs/groundspeak:log/groundspeak:type",
             "/gpx/wpt/groundspeak:cache/groundspeak:logs/groundspeak:log/groundspeak:finder",
             "/gpx/wpt/groundspeak:cache/groundspeak:logs/groundspeak:log/groundspeak:text"
     };
 
     public GpxEventHandler(CacheDetailsWriterFactory cacheDetailsWriterFactory, Cache cache,
             CacheDetailsWriter cacheDetailsWriter) {
         mCacheDetailsWriterFactory = cacheDetailsWriterFactory;
         mCacheDetailsWriter = cacheDetailsWriter;
         mCache = cache;
     }
 
     public Cache endTag(String previousFullPath) throws IOException {
         if (previousFullPath.equals("/gpx/wpt")) {
             mCacheDetailsWriter.writeFooter();
             mCacheDetailsWriter.close();
             return mCache;
         }
         return null;
     }
 
     public void startTag(String mFullPath, XmlPullParser mXmlPullParser) {
         if (mFullPath.equals("/gpx/wpt")) {
             final String lat = mXmlPullParser.getAttributeValue(null, "lat");
             final String lon = mXmlPullParser.getAttributeValue(null, "lon");
             mCache.mLatitude = Double.parseDouble(lat);
             mCache.mLongitude = Double.parseDouble(lon);
         }
     }
 
     public void text(String mFullPath, String text) throws IOException {
         if (mFullPath.equals("/gpx/wpt/name")) {
             mCacheDetailsWriter = mCacheDetailsWriterFactory.create(GpxToCache.GEOBEAGLE_DIR + "/"
                     + text + ".html");
             mCacheDetailsWriter.writeHeader();
             mCacheDetailsWriter.write(text);
             mCacheDetailsWriter.write(mCache.mLatitude + ", " + mCache.mLongitude);
             mCache.mId = text;
        } else if (mFullPath.equals("/gpx/wpt/groundspeak:cache/groundspeak:name")) {
            mCache.mName = text;
        } else if (mFullPath
                .equals("/gpx/wpt/groundspeak:cache/groundspeak:logs/groundspeak:log/groundspeak:date")) {
            mCacheDetailsWriter.writeSeparator();
            mCacheDetailsWriter.write(text);
        } else {
            for (String writeLineMatch : mWriteLineMatches) {
                if (mFullPath.equals(writeLineMatch)) {
                    mCacheDetailsWriter.write(text);
                    return;
                }
            }
         }
     }
 }
