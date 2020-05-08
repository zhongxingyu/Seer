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
 
 import com.google.code.geobeagle.ErrorDisplayer;
 import com.google.code.geobeagle.R;
 import com.google.code.geobeagle.cachedetails.FileDataVersionChecker.FileDataVersionWriter;
 import com.google.code.geobeagle.xmlimport.EventHelperDI.EventHelperFactory;
 import com.google.code.geobeagle.xmlimport.GpxImporterDI.MessageHandler;
 import com.google.code.geobeagle.xmlimport.gpx.GpxAndZipFiles;
 import com.google.code.geobeagle.xmlimport.gpx.IGpxReader;
 import com.google.code.geobeagle.xmlimport.gpx.GpxAndZipFiles.GpxFilesAndZipFilesIter;
 
 import org.xmlpull.v1.XmlPullParserException;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 
 public class ImportThreadDelegate {
     public static class ImportThreadHelper {
         private final ErrorDisplayer mErrorDisplayer;
         private final EventHandlers mEventHandlers;
         private final EventHelperFactory mEventHelperFactory;
         private final GpxLoader mGpxLoader;
         private boolean mHasFiles;
         private final MessageHandler mMessageHandler;
 
         public ImportThreadHelper(GpxLoader gpxLoader, MessageHandler messageHandler,
                 EventHelperFactory eventHelperFactory, EventHandlers eventHandlers,
                 ErrorDisplayer errorDisplayer) {
             mErrorDisplayer = errorDisplayer;
             mGpxLoader = gpxLoader;
             mMessageHandler = messageHandler;
             mEventHelperFactory = eventHelperFactory;
             mEventHandlers = eventHandlers;
             mHasFiles = false;
         }
 
         public void cleanup() {
             mMessageHandler.loadComplete();
         }
 
         public void end() {
             mGpxLoader.end();
             if (!mHasFiles)
                 mErrorDisplayer.displayError(R.string.error_no_gpx_files);
         }
 
         public boolean processFile(IGpxReader gpxReader) throws XmlPullParserException, IOException {
             String filename = gpxReader.getFilename();
 
             mHasFiles = true;
             mGpxLoader.open(filename, gpxReader.open());
             int len = filename.length();
             String extension = filename.substring(Math.max(0, len - 3), len).toLowerCase();
             return mGpxLoader.load(mEventHelperFactory.create(mEventHandlers.get(extension)));
         }
 
         public void start() {
             OldCacheFilesCleaner.clean(CacheDetailsLoader.DETAILS_DIR, mMessageHandler);
             mGpxLoader.start();
         }
     }
 
     private final ErrorDisplayer mErrorDisplayer;
     private final GpxAndZipFiles mGpxAndZipFiles;
     private final ImportThreadHelper mImportThreadHelper;
     private final FileDataVersionWriter mFileDataVersionWriter;
 
     public ImportThreadDelegate(GpxAndZipFiles gpxAndZipFiles,
             ImportThreadHelper importThreadHelper, ErrorDisplayer errorDisplayer,
             FileDataVersionWriter fileDataVersionWriter) {
         mGpxAndZipFiles = gpxAndZipFiles;
         mImportThreadHelper = importThreadHelper;
         mErrorDisplayer = errorDisplayer;
         mFileDataVersionWriter = fileDataVersionWriter;
     }
 
     public void run() {
         try {
             tryRun();
         } catch (final FileNotFoundException e) {
             mErrorDisplayer.displayError(R.string.error_opening_file, e.getMessage());
         } catch (IOException e) {
             mErrorDisplayer.displayError(R.string.error_reading_file, e.getMessage());
         } catch (XmlPullParserException e) {
             mErrorDisplayer.displayError(R.string.error_parsing_file, e.getMessage());
         } finally {
             mImportThreadHelper.cleanup();
         }
     }
 
     protected void tryRun() throws IOException, XmlPullParserException {
         GpxFilesAndZipFilesIter gpxFilesAndZipFilesIter = mGpxAndZipFiles.iterator();
         if (gpxFilesAndZipFilesIter == null) {
             mErrorDisplayer.displayError(R.string.error_cant_read_sd);
             return;
         }
 
         mImportThreadHelper.start();
         while (gpxFilesAndZipFilesIter.hasNext()) {
             if (!mImportThreadHelper.processFile(gpxFilesAndZipFilesIter.next()))
                 return;
         }
         mFileDataVersionWriter.writeVersion();
         mImportThreadHelper.end();
     }
 }
