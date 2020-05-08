 /**
  * Copyright (C) 2010, Hoccer GmbH Berlin, Germany <www.hoccer.com>
  *
  * These coded instructions, statements, and computer programs contain
 * proprietary information of Hoccer GmbH Berlin, and are copy protected
  * by law. They may be used, modified and redistributed under the terms
  * of GNU General Public License referenced below. 
  *    
  * Alternative licensing without the obligations of the GPL is
  * available upon request.
  * 
  * GPL v3 Licensing:
  * 
  * This file is part of the "Linccer Android-API".
  * 
  * Linccer Android-API is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * Linccer Android-API is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with Linccer Android-API. If not, see <http://www.gnu.org/licenses/>.
  */
 package com.hoccer.api.android;
 
 import java.io.IOException;
 
 import android.app.Service;
 import android.content.Intent;
 import android.os.IBinder;
 
 import com.hoccer.api.ClientConfig;
 import com.hoccer.api.FileCache;
 import com.hoccer.data.StreamableContent;
 import com.hoccer.http.HttpResponseHandler;
 
 public class FileCacheService extends Service {
 
     private FileCache mFileCache;
 
     public void init(ClientConfig config) {
         if (mFileCache == null) {
             mFileCache = new FileCache(config);
         }
     }
 
     protected void stopWhenAllLoadsFinished() {
         if (mFileCache.getOngoingRequests().size() == 0) {
             stopSelf();
         }
     }
 
     public void fetch(String uri, StreamableContent sink, HttpResponseHandler responseHandler) {
         mFileCache.asyncFetch(uri, sink, responseHandler);
     }
 
     public String store(StreamableContent source, int secondsUntilExipred,
             HttpResponseHandler responseHandler) throws IOException {
         String uri = mFileCache.asyncStore(source, secondsUntilExipred, responseHandler);
         return uri;
     }
 
     public void cancel(String uri) {
         mFileCache.cancel(uri);
     }
 
     @Override
     public IBinder onBind(Intent intent) {
         return null;
     }
 }
