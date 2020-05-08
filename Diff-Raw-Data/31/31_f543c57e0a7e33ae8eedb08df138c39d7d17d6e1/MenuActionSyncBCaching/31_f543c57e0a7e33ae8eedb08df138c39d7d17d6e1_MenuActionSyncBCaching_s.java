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
 
 package com.google.code.geobeagle.activity.cachelist.actions.menu;
 
 import com.google.code.geobeagle.R;
 import com.google.code.geobeagle.actions.MenuActionBase;
 import com.google.code.geobeagle.bcaching.ImportBCachingWorker;
 import com.google.code.geobeagle.database.DbFrontend;
import com.google.code.geobeagle.xmlimport.GpxImporterDI.Toaster;
import com.google.code.geobeagle.activity.cachelist.CacheListModule.ToasterSyncAborted;
import com.google.code.geobeagle.activity.cachelist.actions.menu.Abortable;
 import com.google.inject.Inject;
 import com.google.inject.Provider;
 
 import android.util.Log;
 
 public class MenuActionSyncBCaching extends MenuActionBase {
 
     private final Provider<ImportBCachingWorker> importBCachingWorkerProvider;
    private final Toaster toaster;
     private Abortable abortable;
     
     @Inject
     public MenuActionSyncBCaching(Provider<ImportBCachingWorker> importBCachingWorkerProvider,
            DbFrontend dbFrontend, Abortable abortable, @ToasterSyncAborted Toaster toaster) {
         super(R.string.menu_sync_bcaching);
         this.importBCachingWorkerProvider = importBCachingWorkerProvider;
         this.abortable = abortable;
        this.toaster = toaster;
         Log.d("GeoBeagleDb", "Sync: " + dbFrontend);
     }
 
     @Override
     public void act() {
         ImportBCachingWorker importBCachingWorker = importBCachingWorkerProvider.get();
         importBCachingWorker.start();
         abortable = importBCachingWorker;
     }
 
     public void abort() {
         Log.d("GeoBeagle", "GpxImport aborting");
         abortable.abort();
        toaster.showToast();
     }
 }
