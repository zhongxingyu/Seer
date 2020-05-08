 package com.parent.management.jsonclient;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.util.UUID;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.ProtocolVersion;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.params.BasicHttpParams;
 import org.apache.http.params.HttpConnectionParams;
 import org.apache.http.params.HttpParams;
 import org.apache.http.params.HttpProtocolParams;
 import org.apache.http.protocol.HTTP;
 import org.apache.http.util.EntityUtils;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.util.Log;
 
 import com.parent.management.ManagementApplication;
 
 public class JSONHttpClient {
     
     private static final String TAG = ManagementApplication.getApplicationTag()
             + "." + JSONHttpClient.class.getSimpleName();
     
     /*
      * HttpClient to issue the HTTP/POST request
      */
     private HttpClient httpClient;
     /*
      * Service URI
      */
     private String serviceUri;
     
     private String encoding = HTTP.UTF_8;
     
     private int soTimeout = 0, connectionTimeout = 0;
     
     // HTTP 1.0
     private static final ProtocolVersion PROTOCOL_VERSION = new ProtocolVersion("HTTP", 1, 0);
     
     private static final int MS_SUCCESS = 0;
     
     public JSONHttpClient(HttpClient cleint, String uri) {
         httpClient = cleint;
         serviceUri = uri;
     }
     
     public JSONHttpClient(String uri)
     {
         this(new DefaultHttpClient(), uri);
     }
     
     public void setEncoding(String encoding){
         this.encoding = encoding;
     }
     
     public void delEncoding(){
         this.encoding = "";
     }
     
     /**
      * Set the socket operation timeout
      * @param soTimeout timeout in milliseconds
      */
     public void setSoTimeout(int soTimeout)
     {
         this.soTimeout = soTimeout;
     }
 
     /**
      * Set the connection timeout
      * @param connectionTimeout timeout in milliseconds
      */
     public void setConnectionTimeout(int connectionTimeout)
     {
         this.connectionTimeout = connectionTimeout;
     }
 
     private JSONObject doJSONRequest(JSONObject jsonRequest) throws JSONClientException
     {
         // Create HTTP/POST request with a JSON entity containing the request
         HttpPost request = new HttpPost(serviceUri);
         HttpParams params = new BasicHttpParams();
         HttpConnectionParams.setConnectionTimeout(params, this.connectionTimeout);
         HttpConnectionParams.setSoTimeout(params, this.soTimeout);
         HttpProtocolParams.setVersion(params, PROTOCOL_VERSION);
         request.setParams(params);
 
         Log.d(TAG, "JSON Request: " + jsonRequest.toString());
         
         HttpEntity entity;
         
         try {
             if (encoding.length() > 0) {
                 entity = new JSONEntity(jsonRequest, encoding);
             } else {
                 entity = new JSONEntity(jsonRequest);
             }
         } catch (UnsupportedEncodingException e) {
             throw new JSONClientException("Unsupported encoding", e);
         }
         request.setEntity(entity);
         
         try {
             // Execute the request and try to decode the JSON Response
             long t = System.currentTimeMillis();
             HttpResponse response = httpClient.execute(request);
             
             t = System.currentTimeMillis() - t;
             String responseString = EntityUtils.toString(response.getEntity());
             
             responseString = responseString.trim();
             
             Log.d(TAG, "Response: " + responseString);
             
             return new JSONObject(responseString);
         }
         // Underlying errors are wrapped into a JSONRPCException instance
         catch (ClientProtocolException e) {
             throw new JSONClientException("HTTP error", e);
         } catch (IOException e) {
             throw new JSONClientException("IO error", e);
         } catch (JSONException e) {
             throw new JSONClientException("Invalid JSON response", e);
         }
     }
     
     protected static JSONArray getJSONArray(Object[] array){
         JSONArray arr = new JSONArray();
         for (Object item : array) {
             if(item.getClass().isArray()){
                 arr.put(getJSONArray((Object[])item));
             }
             else {
                 arr.put(item);
             }
         }
         return arr;
     }
     
     public JSONArray doUpload(JSONArray payload) throws JSONClientException
     {
         //Create the json request object
         JSONObject jsonRequest = new JSONObject();
         try {
             jsonRequest.put(JSONParams.PROTOCOL_VERSION, "1.0");
             jsonRequest.put(JSONParams.MESSAGE_CLASS, JSONParams.MC_BASIC);
             jsonRequest.put(JSONParams.MESSAGE_TYPE, JSONParams.MT_BASIC_DATA_UPLOAD_REQ);
             long id = System.currentTimeMillis();
             jsonRequest.put(JSONParams.REQUEST_SEQUENCE, id);
             jsonRequest.put(JSONParams.DEVICE_IMEI, getDevideId());
             jsonRequest.put(JSONParams.PAYLOAD, payload);
             
             JSONObject result = doJSONRequest(jsonRequest);
             
             if (result.getInt(JSONParams.MESSAGE_TYPE) == JSONParams.MT_BASIC_DATA_UPLOAD_RESP && 
                 result.getLong(JSONParams.REQUEST_SEQUENCE) == id) {
                 return result.getJSONObject(JSONParams.PAYLOAD).getJSONArray(JSONParams.RESPONSE_FAILED);
             } else {
                 throw new JSONClientException("Invalid JSON response");
             }
         } catch (JSONException e1) {
             throw new JSONClientException("Invalid JSON request", e1);
         }
     }
     
     public boolean doRegistion(String account, String code) throws JSONClientException
     {
         try {
         	//Create the json request object
             JSONObject jsonRequest = new JSONObject();
             
             jsonRequest.put(JSONParams.PROTOCOL_VERSION, "1.0");
             jsonRequest.put(JSONParams.MESSAGE_CLASS, JSONParams.MC_BASIC);
             jsonRequest.put(JSONParams.MESSAGE_TYPE, JSONParams.MT_BASIC_REG_REQ);
             long id = System.currentTimeMillis();
             jsonRequest.put(JSONParams.REQUEST_SEQUENCE, id);
             jsonRequest.put(JSONParams.DEVICE_IMEI, getDevideId());
             
             JSONObject jsonParams = new JSONObject();
             jsonParams.put(JSONParams.MANAGER_ACCOUNT, account);
             jsonParams.put(JSONParams.VERIFY_CODE, code);
             jsonParams.put(JSONParams.OS_TYPE, "Android");
             jsonParams.put(JSONParams.MODEL, android.os.Build.MODEL);
             jsonParams.put(JSONParams.OS_VERSION, android.os.Build.VERSION.RELEASE);
             jsonParams.put(JSONParams.DEVICE_IMSI, ManagementApplication.getIMSI());
             jsonRequest.put(JSONParams.PAYLOAD, jsonParams);
             
             JSONObject result = doJSONRequest(jsonRequest);
             
             if (result.getInt(JSONParams.MESSAGE_TYPE) == JSONParams.MT_BASIC_REG_RESP && 
                 result.getLong(JSONParams.REQUEST_SEQUENCE) == id &&
                 result.getJSONObject(JSONParams.PAYLOAD).getInt(JSONParams.RESPONSE_STATUS_CODE) == MS_SUCCESS) {
                 return true;
             }
             return false;
         } catch (JSONException e1) {
             throw new JSONClientException("Invalid JSON response", e1);
         }
         
     }
     
     public JSONObject doConfiguration() throws JSONClientException 
     {
         //Create the json request object
         JSONObject jsonRequest = new JSONObject();
         
         try {
             jsonRequest.put(JSONParams.PROTOCOL_VERSION, "1.0");
             jsonRequest.put(JSONParams.MESSAGE_CLASS, JSONParams.MC_CONFIG);
             jsonRequest.put(JSONParams.MESSAGE_TYPE, JSONParams.MT_CONFIG_GET_INTERVAL_REQ);
             long id = System.currentTimeMillis();
             jsonRequest.put(JSONParams.REQUEST_SEQUENCE, id);
             jsonRequest.put(JSONParams.DEVICE_IMEI, getDevideId());
             
             JSONObject result = doJSONRequest(jsonRequest);
             
             if (result.getInt(JSONParams.MESSAGE_TYPE) == JSONParams.MT_CONFIG_GET_INTERVAL_RESP && 
                 result.getLong(JSONParams.REQUEST_SEQUENCE) == id &&
                 result.getJSONObject(JSONParams.PAYLOAD).getInt(JSONParams.RESPONSE_STATUS_CODE) == MS_SUCCESS) {
                 return result.getJSONObject(JSONParams.PAYLOAD);
             }
             
             throw new JSONClientException("Invalid JSON response");
         } catch (JSONException e1) {
             throw new JSONClientException("Invalid JSON request", e1);
         }
         
     }
     
     private String getDevideId() {
         String uid;
        String imei = ManagementApplication.getIMEI();
        if (imei == null || Integer.valueOf(imei) == 0) {
             if (ManagementApplication.getConfiguration().getUUid() != null) {
                 uid = ManagementApplication.getConfiguration().getUUid();
             } else {
                 uid = UUID.randomUUID().toString();
                 ManagementApplication.getConfiguration().setUUid(uid);
             }
         } else {
             uid = ManagementApplication.getIMEI();
         }
         
         return uid;
     }
     
 }
