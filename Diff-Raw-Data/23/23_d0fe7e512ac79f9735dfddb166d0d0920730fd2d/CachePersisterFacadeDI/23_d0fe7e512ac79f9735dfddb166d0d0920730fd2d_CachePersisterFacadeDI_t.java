 
 package com.google.code.geobeagle.io;
 
 import com.google.code.geobeagle.io.DatabaseDI.SQLiteWrapper;
 import com.google.code.geobeagle.io.GpxImporterDI.MessageHandler;
 
 import android.app.Activity;
 import android.content.Context;
 import android.os.PowerManager;
 import android.os.PowerManager.WakeLock; /*
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
  *//*
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
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.Writer;
 
 public class CachePersisterFacadeDI {
 
     public static class FileFactory {
         public File createFile(String path) {
             return new File(path);
         }
     }
 
     public static class WriterWrapper {
         Writer mWriter;
 
         public void close() throws IOException {
             mWriter.close();
         }
 
         public void open(String path) throws IOException {
             mWriter = new BufferedWriter(new FileWriter(path), 4000);
         }
 
         public void write(String str) throws IOException {
            try {
                mWriter.write(str);
            } catch (IOException e) {
                throw new IOException("Error writing line '" + str + "'");
            }
         }
     }
 
     public static CachePersisterFacade create(Activity activity, MessageHandler messageHandler,
             Database database, SQLiteWrapper sqliteWrapper) {
         final CacheWriter cacheWriter = DatabaseDI.createCacheWriter(sqliteWrapper);
         final CacheTagWriter cacheTagWriter = new CacheTagWriter(cacheWriter);
         final FileFactory fileFactory = new FileFactory();
         final WriterWrapper writerWrapper = new WriterWrapper();
         final HtmlWriter htmlWriter = new HtmlWriter(writerWrapper);
         final CacheDetailsWriter cacheDetailsWriter = new CacheDetailsWriter(htmlWriter);
         final PowerManager powerManager = (PowerManager)activity
                 .getSystemService(Context.POWER_SERVICE);
         final WakeLock wakeLock = powerManager.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK,
                 "Importing");
 
         return new CachePersisterFacade(cacheTagWriter, fileFactory, cacheDetailsWriter,
                 messageHandler, wakeLock);
     }
 
 }
