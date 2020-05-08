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
 
 package com.google.code.geobeagle.bcaching;
 
 import com.google.code.geobeagle.ErrorDisplayer;
 import com.google.code.geobeagle.R;
 import com.google.code.geobeagle.activity.cachelist.CacheListModule.ToasterSyncAborted;
 import com.google.code.geobeagle.activity.cachelist.actions.menu.Abortable;
 import com.google.code.geobeagle.bcaching.communication.BCachingException;
 import com.google.code.geobeagle.bcaching.communication.BCachingList;
 import com.google.code.geobeagle.bcaching.communication.BCachingListImporter;
 import com.google.code.geobeagle.bcaching.progress.ProgressHandler;
 import com.google.code.geobeagle.bcaching.progress.ProgressManager;
 import com.google.code.geobeagle.bcaching.progress.ProgressMessage;
 import com.google.code.geobeagle.xmlimport.GpxImporterDI.Toaster;
 import com.google.inject.Inject;
 
 import roboguice.util.RoboThread;
 
 import android.util.Log;
 
 public class ImportBCachingWorker extends RoboThread implements Abortable {
     private final ProgressHandler progressHandler;
     private final BCachingLastUpdated bcachingLastUpdated;
     private final BCachingListImporter bcachingListImporter;
     private final ErrorDisplayer errorDisplayer;
     private final ProgressManager progressManager;
     private final DetailsReaderImport detailsReaderImport;
     private final Toaster toaster;
     private boolean inProgress;
 
     @Inject
     public ImportBCachingWorker(ProgressHandler progressHandler, ProgressManager progressManager,
             BCachingLastUpdated bcachingLastUpdated, BCachingListImporter bcachingListImporter,
             ErrorDisplayer errorDisplayer, DetailsReaderImport detailsReaderImport,
             @ToasterSyncAborted Toaster toaster) {
         this.progressHandler = progressHandler;
         this.bcachingLastUpdated = bcachingLastUpdated;
         this.bcachingListImporter = bcachingListImporter;
         this.errorDisplayer = errorDisplayer;
         this.progressManager = progressManager;
         this.detailsReaderImport = detailsReaderImport;
         this.toaster = toaster;
     }
 
     @Override
     public void run() {
         inProgress = true;
         long now = System.currentTimeMillis();
         progressManager.update(progressHandler, ProgressMessage.START, 0);
         String lastUpdateTime = String.valueOf(bcachingLastUpdated.getLastUpdateTime());
 
         try {
             int totalCount = bcachingListImporter.getTotalCount(lastUpdateTime);
             if (totalCount <= 0)
                 return;
             progressManager.update(progressHandler, ProgressMessage.SET_MAX, totalCount);
            int totalCachesRead = bcachingLastUpdated.getLastRead();
             progressManager.update(progressHandler, ProgressMessage.SET_PROGRESS, totalCachesRead);
 
             BCachingList bcachingList = bcachingListImporter.getCacheList(String
                     .valueOf(totalCachesRead), lastUpdateTime.toString());
             int cachesRead;
             while ((cachesRead = bcachingList.getCachesRead()) > 0) {
                 Log.d("GeoBeagle", "cachesRead: " + cachesRead);
                 if (!detailsReaderImport.loadCacheDetails(bcachingList.getCacheIds())) {
                     Log.d("GeoBeagle", "run() ABORTING");
                     return;
                 }
                 totalCachesRead += cachesRead;
 
                 bcachingLastUpdated.putLastRead(totalCachesRead);
                 progressManager.update(progressHandler, ProgressMessage.SET_PROGRESS,
                         totalCachesRead);
                 bcachingList = bcachingListImporter.getCacheList(String.valueOf(totalCachesRead),
                         lastUpdateTime);
             }
             bcachingLastUpdated.putLastUpdateTime(now);
             bcachingLastUpdated.putLastRead(0);
             Log.d("GeoBeagle", "run() SENDING REFRESH message");
             progressManager.update(progressHandler, ProgressMessage.REFRESH, 0);
         } catch (BCachingException e) {
             progressManager.update(progressHandler, ProgressMessage.REFRESH, 0);
             errorDisplayer.displayError(R.string.problem_importing_from_bcaching, e
                     .getLocalizedMessage());
         } finally {
            /*
             * Do NOT call refresh here--we might be here as the result of
             * switching activities; a refresh will reference a stale database
             * handle and crash.
             */
             progressManager.update(progressHandler, ProgressMessage.DONE, 0);
             inProgress = false;
             Log.d("GeoBeagle", "ImportBcachingWorker ending");
         }
     }
 
     @Override
     public void abort() {
         if (!inProgress)
             return;
         Log.d("GeoBeagle", "ABORTING IMPORT");
         try {
             Log.d("GeoBeagle", "abort: JOIN STARTED");
             inProgress = false;
             join();
             toaster.showToast();
             Log.d("GeoBeagle", "abort: JOIN FINISHED");
         } catch (InterruptedException e) {
             Log.d("GeoBeagle", "Ignoring InterruptedException: " + e.getLocalizedMessage());
         }
         Log.d("GeoBeagle", "done abort IMPORT");
 
     }
 }
