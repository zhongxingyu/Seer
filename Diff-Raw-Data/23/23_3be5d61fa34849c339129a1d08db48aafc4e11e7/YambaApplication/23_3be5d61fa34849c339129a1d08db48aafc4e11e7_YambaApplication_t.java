 /* $Id: $
    Copyright 2013, G. Blake Meike
 
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
 
        http://www.apache.org/licenses/LICENSE-2.0
 
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
 */
 package com.marakana.android.yamba;
 
import java.util.List;

 import android.app.Application;
 import android.util.Log;
 
 import com.marakana.android.yamba.clientlib.YambaClient;
import com.marakana.android.yamba.clientlib.YambaClient.Status;
import com.marakana.android.yamba.clientlib.YambaClientException;
 
 
 /**
  *
  * @version $Revision: $
  * @author <a href="mailto:blake.meike@gmail.com">G. Blake Meike</a>
  */
 public class YambaApplication extends Application {
     private static final String TAG = "APP";
    private static final int MAX_POSTS = 50;
 
     public class SafeYambaClient {
         private final YambaClient yClient;
 
         public SafeYambaClient(String usr, String pwd, String url) {
             yClient = new YambaClient(usr, pwd, url);
         }
 
        public synchronized void post(String status) throws YambaClientException {
            yClient.postStatus(status);
        }

        public synchronized List<Status> poll() throws YambaClientException {
            return yClient.getTimeline(MAX_POSTS);
         }
     }
 
 
     private SafeYambaClient client;
 
     @Override
     public void onCreate() {
         if (BuildConfig.DEBUG) { Log.d(TAG, "Yamba is up!"); }
         super.onCreate();
     }
 
     public synchronized SafeYambaClient getClient() {
         if (null == client) {
             client = new SafeYambaClient(
                 "student",
                 "password",
                 "http://yamba.marakana.com/api");
         }
         return client;
     }
 }
