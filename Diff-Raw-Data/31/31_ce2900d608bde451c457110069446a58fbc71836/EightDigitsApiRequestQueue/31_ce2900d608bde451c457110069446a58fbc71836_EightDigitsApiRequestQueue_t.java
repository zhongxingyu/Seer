 package com.eightdigits.sdk;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.LinkedBlockingQueue;
 
 import org.apache.http.NameValuePair;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.ResponseHandler;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.BasicResponseHandler;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import com.eightdigits.sdk.exceptions.EightDigitsApiException;
 
 
 @SuppressWarnings("unused")
 public class EightDigitsApiRequestQueue implements Runnable {
   
   private EightDigitsClient clientInstance;
   
   public static Integer FIRST_PRIORITY = 1;
   public static Integer SECOND_PRIORITY = 2;
   public static Integer THIRD_PRIORITY = 3;
   
   private static final BlockingQueue<Map<Object, Object>> firstPriorityQueue = new LinkedBlockingQueue<Map<Object, Object>>();
   private static final BlockingQueue<Map<Object, Object>> secondPriorityQueue = new LinkedBlockingQueue<Map<Object, Object>>();
   private static final BlockingQueue<Map<Object, Object>> thirdPriorityQueue = new LinkedBlockingQueue<Map<Object, Object>>();
   
 
   public EightDigitsApiRequestQueue(EightDigitsClient clientInstance) {
     this.clientInstance = clientInstance;
   }
   
   /**
    * Creates new queue item and returns it
    * 
    * @param url
    * @param pairs
    * @return
    */
   public static Map<Object, Object> createQueueNode(String url,
       List<NameValuePair> pairs, EightDigitsResultListener callback, Integer priority) {
     Map<Object, Object> queueItem = new HashMap<Object, Object>();
     queueItem.put(Constants.URL, url);
     queueItem.put(Constants.PAIRS, pairs);
     queueItem.put(Constants.CALLBACK, callback);
     queueItem.put(Constants.PRIORITY, priority);
     return queueItem;
   }
 
   @SuppressWarnings({ "rawtypes", "unchecked" })
   public static void push(String url, List<NameValuePair> pairs, EightDigitsResultListener callback, Integer priority) {
     Map queueItem = createQueueNode(url, pairs, callback, priority);
     BlockingQueue<Map<Object, Object>> queue = null;
     
     if(priority == EightDigitsApiRequestQueue.FIRST_PRIORITY) {
       queue = firstPriorityQueue;
     }
     else if (priority == EightDigitsApiRequestQueue.SECOND_PRIORITY) {
       queue = secondPriorityQueue;
     } else {
       queue = thirdPriorityQueue;
     }
     queue.add(queueItem);
   }
   
   public void run() {
     try {
       while (true) {
         processQueue(firstPriorityQueue);
         processQueue(secondPriorityQueue);
         processQueue(thirdPriorityQueue);
         Thread.sleep(500);
       }
     } catch (InterruptedException e) {
       EightDigitsClient.logError(e.getMessage());
     }
   }
 
   @SuppressWarnings({ "unchecked", "rawtypes" })
   private void processQueue(BlockingQueue<Map<Object, Object>> queue) {
     Map queueItem = null;
     
     if (queue.size() > 0) {
       Boolean breakLoop = false;
       while (!breakLoop) {
         queueItem = queue.poll();
         
         if(queueItem == null)
           break;
         
         byte tryCount = 1;
         
         while(tryCount <= 5) {
           EightDigitsClient.log("Try count = " + new Byte(tryCount).toString());
           try {
             api(queueItem);
             break;
           } catch (EightDigitsApiException e) {
             // If token is expired we are sending new auth request
             if(e.getErrorCode() == -501) {
                 EightDigitsClient.getInstance().reAuth();
                 breakLoop = true;
             } else {
               tryCount++;
             }
             
             EightDigitsClient.logError(e.getMessage());
             
           } catch (Exception e) {
             EightDigitsClient.logError(e.getMessage());
             tryCount++;
           }
         }
       }
     }
   }
   
   @SuppressWarnings("unchecked")
   public void api(Map<Object, Object> queueItem) throws EightDigitsApiException, ClientProtocolException, IOException, UnsupportedEncodingException {
     String url = (String) queueItem.get(Constants.URL);
     EightDigitsClient.log("URL : " + url);
     
     List<NameValuePair> pairs = formatPairs((List<NameValuePair>) queueItem.get(Constants.PAIRS));
     EightDigitsResultListener callback = (EightDigitsResultListener) queueItem.get(Constants.CALLBACK);
     
       HttpClient client = new DefaultHttpClient();  
       HttpPost post = new HttpPost(url);
       post.setEntity(new UrlEncodedFormEntity(pairs));
       
       ResponseHandler<String> responseHandler = new BasicResponseHandler();
       String response = client.execute(post, responseHandler);
       
       EightDigitsClient.log("RESPONSE : " + response);
       
       if(callback != null) {
         JSONObject result = parse(response);
         callback.handleResult(result);
       }
       
       client.getConnectionManager().closeExpiredConnections();
       
     
     EightDigitsClient.log("-----------------------------------------");
   }
   
   private List<NameValuePair> formatPairs(List<NameValuePair> pairs) {
     List<NameValuePair> newPairs = new ArrayList<NameValuePair>();
     
     for(NameValuePair pair : pairs) {
       NameValuePair newPair = pair;
       
       if(pair.getName().equals(Constants.AUTH_TOKEN)) {
         newPair = new BasicNameValuePair(pair.getName(), this.clientInstance.getAuthToken());
       }
       else if (pair.getName().equals(Constants.HIT_CODE)) {
         newPair = new BasicNameValuePair(pair.getName(), this.clientInstance.getHitCode());
       }
       else if (pair.getName().equals(Constants.SESSION_CODE)) {
         newPair = new BasicNameValuePair(pair.getName(), this.clientInstance.getSessionCode());
       }
       
       EightDigitsClient.log("PARAM --> " + newPair.getName() + " : " + newPair.getValue());
       
       newPairs.add(newPair);
     }
     
     // Add SDK Version
     newPairs.add(new BasicNameValuePair(Constants.EIGHTDIGITS_SDK_VERSION, "3.0"));
     
     return newPairs;
   }
   
   public static BlockingQueue<Map<Object, Object>> getFirstPriorityQueue() {
     return firstPriorityQueue;
   }
 
   public static BlockingQueue<Map<Object, Object>> getSecondPriorityQueue() {
     return secondPriorityQueue;
   }
 
   public static BlockingQueue<Map<Object, Object>> getThirdPriorityQueue() {
     return thirdPriorityQueue;
   }
   
   private JSONObject parse(String response) throws EightDigitsApiException {
     JSONObject jsonObject = null;
     try {
 
       if (response.trim().length() == 0) {
         throw new EightDigitsApiException(-504, "API returned empty result.");
       }
 
       jsonObject = new JSONObject(response);
      int errorCode = jsonObject.getJSONObject("result").getInt("code");
      
      if(errorCode != 0) {
        if(errorCode == -1) {
          throw new EightDigitsApiException(-501, "Auth token is expired. Getting new one..");
        } else {
          String message = jsonObject.getJSONObject("result").getString("message");
          throw new EightDigitsApiException(errorCode, message);
        }
       }
     } catch (JSONException e) {
       throw new EightDigitsApiException(-502, "Problem occured at JSON parsing.");
     }
     return jsonObject;
   }
 
   private List<String> convertJsonArrayToArrayList(JSONArray jsonArray) {
     List<String> resultList = new ArrayList<String>();
 
     try {
       for (int i = 0; i < jsonArray.length(); i++) {
         resultList.add(jsonArray.get(i).toString());
       }
     } catch (JSONException e) {
       EightDigitsClient.logError(e.getMessage());
     }
     return resultList;
   }
 
 }
