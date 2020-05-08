 package com.hoccer.api.android;
 
 import java.io.IOException;
 import java.util.HashMap;
 
 import android.app.Service;
 import android.content.Intent;
 import android.os.IBinder;
 
 import com.hoccer.api.ClientConfig;
 import com.hoccer.data.StreamableContent;
 import com.hoccer.http.AsyncHttpGet;
 import com.hoccer.http.AsyncHttpPut;
 import com.hoccer.http.AsyncHttpRequest;
 import com.hoccer.http.HttpResponseHandler;
 
 public class FileCacheService extends Service {
 
     private final HashMap<String, AsyncHttpRequest> mOngoingRequests = new HashMap<String, AsyncHttpRequest>();
 
     protected void stopWhenAllLoadsFinished() {
 
     }
 
     public void fetch(String url, StreamableContent sink, HttpResponseHandler responseHandler) {
         AsyncHttpGet fetchRequest = new AsyncHttpGet(url);
         fetchRequest.registerResponseHandler(responseHandler);
         fetchRequest.setStreamableContent(sink);
         fetchRequest.start();
         mOngoingRequests.put(url, fetchRequest);
     }
 
     public String store(StreamableContent source, int secondsUntilExipred,
             HttpResponseHandler responseHandler) throws IOException {
         String url = ClientConfig.getFileCacheBaseUri() + "/" + source.getFilename()
                + "/?expires_in=" + secondsUntilExipred;
 
         AsyncHttpPut storeRequest = new AsyncHttpPut(url);
         storeRequest.registerResponseHandler(responseHandler);
         storeRequest.setBody(source);
         storeRequest.start();
         mOngoingRequests.put(url, storeRequest);
 
         return url;
     }
 
     @Override
     public IBinder onBind(Intent intent) {
         return null;
     }
 }
