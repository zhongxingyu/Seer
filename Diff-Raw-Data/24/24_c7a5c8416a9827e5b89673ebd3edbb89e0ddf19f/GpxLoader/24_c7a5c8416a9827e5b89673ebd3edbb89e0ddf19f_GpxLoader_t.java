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
 import com.google.code.geobeagle.io.GpxToCache.CancelException;
 import com.google.code.geobeagle.ui.ErrorDisplayer;
 
 import org.xmlpull.v1.XmlPullParserException;
 
 import android.database.sqlite.SQLiteException;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.Reader;
 
 public class GpxLoader {
     private final CachePersisterFacade mCachePersisterFacade;
     private final ErrorDisplayer mErrorDisplayer;
     private final GpxToCache mGpxToCache;
 
     GpxLoader(GpxToCache gpxToCache, CachePersisterFacade cachePersisterFacade,
             ErrorDisplayer errorDisplayer) {
         mGpxToCache = gpxToCache;
         mCachePersisterFacade = cachePersisterFacade;
         mErrorDisplayer = errorDisplayer;
     }
 
     public void abort() {
         mGpxToCache.abort();
     }
 
     public void end() {
         mCachePersisterFacade.end();
     }
 
     /**
      * @return true if we should continue loading more files, false if we should
      *         terminate.
      */
     public boolean load(EventHelper eventHelper) {
         boolean markLoadAsComplete = false;
         boolean continueLoading = false;
         try {
             boolean alreadyLoaded = mGpxToCache.load(eventHelper);
             markLoadAsComplete = !alreadyLoaded;
             continueLoading = true;
         } catch (final SQLiteException e) {
            mErrorDisplayer.displayError(R.string.error_writing_cache, mGpxToCache.getSource()
                    + ": " + e.getMessage());
         } catch (XmlPullParserException e) {
            mErrorDisplayer.displayError(R.string.error_parsing_file, mGpxToCache.getSource()
                    + ": " + e.getMessage());
         } catch (IOException e) {
             mErrorDisplayer.displayError(R.string.error_reading_file, mGpxToCache.getSource());
         } catch (CancelException e) {
         }
         mCachePersisterFacade.close(markLoadAsComplete);
         return continueLoading;
     }
 
     public void open(String path, Reader reader) throws FileNotFoundException,
             XmlPullParserException, IOException {
         mGpxToCache.open(path, reader);
         mCachePersisterFacade.open(path);
     }
 
     public void start() {
         mCachePersisterFacade.start();
     }
 }
