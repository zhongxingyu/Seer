 /*   
  * Copyright 2013 karsten
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.muckebox.android.net;
 
 import java.io.IOException;
 
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
 import org.muckebox.android.utils.Preferences;
 
 import android.os.AsyncTask;
 
 public class PreannounceTask extends AsyncTask<Integer, Void, Void> {
     @Override
     protected Void doInBackground(Integer... trackIds) {
         for (int i = 0; i < trackIds.length; ++i)
         {
             int trackId = trackIds[i];
             
             try {
                 String url = ApiHelper.getApiUrl(
                     "hint",
                     Integer.toString(trackId),
                     new String[] { "format", "quality" },
                     new String[] {
                         Preferences.getTranscodingType(),
                         Preferences.getTranscodingQuality()
                         });
                 
                HttpClient httpClient = new DefaultHttpClient();
                 HttpPost httpPost = new HttpPost(url);
                 
                 httpClient.execute(httpPost);
                
                httpPost.abort();
             } catch (IOException e) {
             }
         }
         
         return null;
     }
 }
