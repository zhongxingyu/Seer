 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.rhomobile.rhoconnect.restclient;
 
 import java.io.BufferedInputStream;
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.Map;
 
 /**
  *
  * @author gporemba
  */
 public class RestClient implements RestCall {
 
     protected URL destinationUrl;
     
     protected enum REQUEST_TYPE  { GET, POST, PUT, DELETE }
     
     public RestClient(String url) throws MalformedURLException {
         destinationUrl = new URL(url);
     }
     
     public RestClient(URL url) throws MalformedURLException {
         destinationUrl = url;
     }
     
     @Override
     public RestResponse delete() {
         return call(REQUEST_TYPE.DELETE, null, null);
     }
 
     @Override
     public RestResponse get(Map<String, String> params) {
         return call(REQUEST_TYPE.GET, null, params);
     }
 
     @Override
     public RestResponse post(InputStream content, Map<String, String> params) {
         return call(REQUEST_TYPE.POST, content, params);
     }
 
     @Override
     public RestResponse put(InputStream content, Map<String, String> params) {
         return call(REQUEST_TYPE.POST, content, params);
     }
     
     
     protected RestResponse call(REQUEST_TYPE requestType, InputStream content, Map params) {
         
         OutputStream os;
         RestResponse res = null;
         HttpURLConnection connection;
         Map.Entry<String, String> entry;
         
         try {
            
             connection = (HttpURLConnection) destinationUrl.openConnection();
             
             
             connection.setInstanceFollowRedirects(false); 
             connection.setRequestMethod(requestType.toString());
             
             for (Object param:params.entrySet()) {
                 entry = (Map.Entry<String, String>) param;
                 connection.addRequestProperty(entry.getKey(), entry.getValue());
             }

            connection.setDoOutput(true); 
             
            int value = content.read();
            os = connection.getOutputStream();    
            
            while (value > -1) {
                os.write(value);
                 value = content.read();
            }
            
            os.flush();
             
             res = new RestResponseImpl(connection); 
  
             connection.disconnect(); 
             
         } catch(Exception e) { 
              e.printStackTrace();
         }
         
         return res;
     }
     
     /*protected void pipeData(InputStream is, OutputStream os) {
         int data =0;
         
         try {
             while (data > -1) {
                 data = is.read();
                 os.write(data);
             }
             
         } catch (IOException ioe) {
             ioe.printStackTrace();
         }
     }*/
 
     @Override
     public RestResponse put(String content, Map<String, String> params) {
         InputStream bais = new ByteArrayInputStream(content.getBytes());
         
         return call(REQUEST_TYPE.PUT, bais, params);
     }
 
     @Override
     public RestResponse post(String content, Map<String, String> params) {
         InputStream bais = new ByteArrayInputStream(content.getBytes());
         
         return call(REQUEST_TYPE.POST, bais, params);
     }
     
 }
