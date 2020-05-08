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
 
import com.google.code.geobeagle.R;
 import com.google.code.geobeagle.io.GpxLoader.Cache;
 import com.google.code.geobeagle.ui.ErrorDisplayer;
 
 import org.xmlpull.v1.XmlPullParserException;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.Iterator;
 
 public class GpxCaches implements Iterable<Cache> {

     public class CacheIterator implements Iterator<Cache> {
         private Cache mCache;
 
         // TODO: hasNext() has a side effect, and next() does not, which is
         // backwards.
         public boolean hasNext() {
             try {
                 mCache = mGpxToCache.load();
                 return mCache != null;
             } catch (XmlPullParserException e) {
                mErrorDisplayer.displayError(R.string.error_parsing_file, mGpxToCache.getSource());
                 return false;
             } catch (IOException e) {
                mErrorDisplayer.displayError(R.string.error_reading_file, mGpxToCache.getSource());
                return false;
            } catch (Exception e) {
                 mErrorDisplayer.displayErrorAndStack(e);
                 return false;
             }
         }
 
         public Cache next() {
             return mCache;
         }
 
         public void remove() {
             throw new UnsupportedOperationException();
         }
     }
 
     public static GpxCaches create(ErrorDisplayer errorDisplayer) {
         final GpxToCache gpxToCache = GpxToCache.create();
         return new GpxCaches(gpxToCache, errorDisplayer);
     }
 
     private final ErrorDisplayer mErrorDisplayer;
     private final GpxToCache mGpxToCache;
     private String mSource;
 
     public GpxCaches(GpxToCache gpxToCache, ErrorDisplayer errorDisplayer) {
         mGpxToCache = gpxToCache;
         mErrorDisplayer = errorDisplayer;
     }
 
     public void open(String path) throws FileNotFoundException, XmlPullParserException {
         mSource = path;
         mGpxToCache.open(path);
     }
 
     public String getSource() {
         return mSource;
     }
 
     public Iterator<Cache> iterator() {
         return new CacheIterator();
     }
 }
