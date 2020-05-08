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
 
 import com.google.code.geobeagle.CacheTypeFactory;
 import com.google.code.geobeagle.database.CacheWriter;
 import com.google.code.geobeagle.database.ClearCachesFromSource;
 import com.google.code.geobeagle.database.ClearCachesFromSourceImpl;
 import com.google.code.geobeagle.database.GpxWriter;
 import com.google.code.geobeagle.database.TagWriter;
 import com.google.code.geobeagle.xmlimport.GpxImporterDI.MessageHandler;
 import com.google.inject.Inject;
 
 import android.os.PowerManager.WakeLock;
 
 import java.io.File;
 
 public class CachePersisterFacadeDI {
 
     public static class CachePersisterFacadeFactory {
         private final CacheTypeFactory mCacheTypeFactory;
         private final FileFactory mFileFactory;
        private final MessageHandlerInterface mMessageHandler;
         private final TagWriter mTagWriter;
         private final ClearCachesFromSource mClearCachesFromSource;
 
         @Inject
        public CachePersisterFacadeFactory(MessageHandler messageHandler,
                 CacheTypeFactory cacheTypeFactory,
                 TagWriter tagWriter,
                 ClearCachesFromSourceImpl clearCachesFromSourceImpl) {
            mMessageHandler = messageHandler;
             mFileFactory = new FileFactory();
             mCacheTypeFactory = cacheTypeFactory;
             mTagWriter = tagWriter;
             mClearCachesFromSource = clearCachesFromSourceImpl;
         }
 
         public ImportCacheActions create(CacheWriter cacheWriter, GpxWriter gpxWriter,
                 WakeLock wakeLock, GeoBeagleEnvironment geoBeagleEnvironment) {
             final CacheTagSqlWriter cacheTagSqlWriter = new CacheTagSqlWriter(cacheWriter,
                    gpxWriter, mCacheTypeFactory, mTagWriter,
                    mClearCachesFromSource);
            return new ImportCacheActions(cacheTagSqlWriter, mFileFactory, mMessageHandler,
                    wakeLock, geoBeagleEnvironment);
         }
     }
 
     public static class FileFactory {
         public File createFile(String path) {
             return new File(path);
         }
     }
 }
