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
 import com.google.code.geobeagle.cachedetails.WriterWrapper;
 import com.google.inject.Inject;
 import com.google.inject.assistedinject.Assisted;
 
 import org.apache.http.HttpException;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.os.Handler;
 import android.os.Message;
 import android.util.Log;
 
 import java.io.BufferedReader;
 import java.io.DataInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.nio.charset.Charset;
 import java.util.Hashtable;
 
 public class ImportBCachingWorker extends Thread {
     public static interface ImportBCachingWorkerFactory {
         ImportBCachingWorker create(Handler handler);
     }
 
     static class DetailsReader {
         private final BCachingCommunication bcachingCommunication;
 
         DetailsReader(BCachingCommunication bcachingCommunication) {
             this.bcachingCommunication = bcachingCommunication;
         }
 
         void getCacheDetails(String csvIds, int updatedCaches) throws IOException, HttpException {
             Log.d("GeoBeagle", "Getting details: " + updatedCaches);
             Hashtable<String, String> params = new Hashtable<String, String>();
             params.put("a", "detail");
             params.put("desc", "html");
             params.put("ids", csvIds);
             params.put("tbs", "0");
             params.put("wpts", "1");
             params.put("logs", "1");
             params.put("fmt", "gpx");
             params.put("app", "GeoBeagle");
         
             Log.d("GeoBeagle", "Downloading cache details");
             DataInputStream dis = new DataInputStream(bcachingCommunication.sendRequest(params));
             WriterWrapper writerWrapper = new WriterWrapper();
             writerWrapper.open("/sdcard/download/bcaching" + String.valueOf(updatedCaches) + ".gpx");
             String line;
             while ((line = dis.readLine()) != null) {
                 writerWrapper.write(line);
             }
             writerWrapper.close();
         }
         
     }
     private final Handler handler;
     private final BCachingLastUpdated bcachingLastUpdated;
     private final BCachingListFactory bcachingListFactory;
     private final ErrorDisplayer errorDisplayer;
     private final ProgressManager progressManager;
     private final DetailsReader detailsReader;
 
     @Inject
     public ImportBCachingWorker(@Assisted Handler handler, ProgressManager progressManager,
             BCachingLastUpdated bcachingLastUpdated, BCachingListFactory bcachingListFactory,
             ErrorDisplayer errorDisplayer, DetailsReader detailsReader) {
         this.handler = handler;
         this.bcachingLastUpdated = bcachingLastUpdated;
         this.bcachingListFactory = bcachingListFactory;
         this.errorDisplayer = errorDisplayer;
         this.progressManager = progressManager;
         this.detailsReader = detailsReader;
     }
 
     static JSONObject readResponse(InputStream in) throws IOException, JSONException {
         BufferedReader rd = new BufferedReader(new InputStreamReader(in, Charset.forName("UTF-8")),
                 8192);
 
         StringBuilder result = new StringBuilder();
         String line;
         while ((line = rd.readLine()) != null) {
             result.append(line);
             result.append('\n');
         }
         String string = result.toString();
         Log.d("GeoBeagle", "readResponse: " + string);
         return new JSONObject(string);
     }
 
     static class BCachingList {
         private final JSONObject cacheList;
 
         BCachingList(JSONObject json) {
             this.cacheList = json;
         }
 
         int getCachesRead() throws JSONException {
             return cacheList.getJSONArray("data").length();
         }
 
         int getTotalCount() throws JSONException {
             return cacheList.getInt("totalCount");
         }
 
         String getCacheIds() throws JSONException {
             JSONArray summary = cacheList.getJSONArray("data");
             Log.d("GeoBeagle", summary.toString());
 
             StringBuilder csvIds = new StringBuilder();
             int count = summary.length();
             for (int i = 0; i < count; i++) {
                 JSONObject cacheObject = summary.getJSONObject(i);
                 int id = cacheObject.getInt("id");
                 if (csvIds.length() > 0) {
                     csvIds.append(',');
                 }
                 csvIds.append(String.valueOf(id));
             }
             return csvIds.toString();
         }
     }
 
     static class BCachingListFactory {
         private final Hashtable<String, String> params;
         private final BCachingCommunication bCachingCommunication;
 
         @Inject
         BCachingListFactory(Hashtable<String, String> params,
                 BCachingCommunication bCachingCommunication) {
             this.params = params;
             this.bCachingCommunication = bCachingCommunication;
         }
 
         int getTotalCount(String lastUpdate) throws IOException, JSONException, HttpException {
             params.remove("first");
             params.put("maxcount", "0");
             params.put("since", lastUpdate);
             return new BCachingList(readResponse(bCachingCommunication.sendRequest(params)))
                     .getTotalCount();
         }
 
         BCachingList getCacheList(int startAt, int count, String lastUpdate) throws IOException,
                 JSONException, HttpException {
             params.put("first", Integer.toString(startAt));
             params.put("maxcount", Integer.toString(count));
             params.put("since", lastUpdate);
             return new BCachingList(readResponse(bCachingCommunication.sendRequest(params)));
         }
     }
 
     static class ProgressManager {
         void update(Handler handler, ProgressMessage progressMessage, int arg) {
             Message.obtain(handler, progressMessage.ordinal(), arg, 0).sendToTarget();
         }
     }
 
     @Override
     public void run() {
         long now = System.currentTimeMillis();
        try {
            progressManager.update(handler, ProgressMessage.START, 0);
            String lastUpdateTime = bcachingLastUpdated.getLastUpdateTime();
 
             int totalCount = bcachingListFactory.getTotalCount(lastUpdateTime);
             if (totalCount <= 0)
                 return;
             progressManager.update(handler, ProgressMessage.SET_MAX, totalCount);
             Log.d("GeoBeagle", "totalCount = " + totalCount);
 
             int updatedCaches = 0;
             BCachingList bcachingList = bcachingListFactory.getCacheList(updatedCaches, 50, String
                     .valueOf(now));
             while (bcachingList.getCachesRead() > 0) {
                 detailsReader.getCacheDetails(bcachingList.getCacheIds(), updatedCaches);
                 updatedCaches += bcachingList.getCachesRead();
                progressManager.update(handler, ProgressMessage.SET_PROGRESS, updatedCaches);
                 bcachingList = bcachingListFactory.getCacheList(updatedCaches, 50, String
                         .valueOf(now));
             }
         } catch (IOException e) {
             errorDisplayer.displayError(R.string.problem_importing_from_bcaching, e
                     .getLocalizedMessage());
             Log.d("GeoBeagle", "Exception: " + e);
         } catch (JSONException e) {
             errorDisplayer.displayError(R.string.problem_importing_from_bcaching, e
                     .getLocalizedMessage());
             Log.d("GeoBeagle", "Exception: " + e);
         } catch (HttpException e) {
             errorDisplayer.displayError(R.string.problem_importing_from_bcaching, e
                     .getLocalizedMessage());
             Log.d("GeoBeagle", "Exception: " + e);
         } finally {
             progressManager.update(handler, ProgressMessage.DONE, 0);
             Log.d("GeoBeagle", "Setting bcaching_lastupdate to " + now);
             bcachingLastUpdated.putLastUpdateTime(now);
         }
     }
 
 }
