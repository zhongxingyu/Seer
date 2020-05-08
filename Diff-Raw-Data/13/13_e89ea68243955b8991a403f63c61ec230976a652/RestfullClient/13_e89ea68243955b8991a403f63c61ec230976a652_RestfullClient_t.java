 package com.abril.traker.satelite;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.URI;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.methods.HttpPut;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
 import org.json.JSONObject;
 
 public class RestfullClient {
 	 
     private static StringBuilder convertStreamToString(InputStream is) throws IOException{
  
         BufferedReader reader = new BufferedReader(new InputStreamReader(is,"ISO-8859-1"));
         StringBuilder sb = new StringBuilder();
  
         String line = null;
         try {
             while ((line = reader.readLine()) != null) {
  
                 sb.append(line + "\n");
             }
         } catch (IOException e) {
             throw e;
         } finally {
             try {
                 is.close();
             } catch (IOException e) {
                 throw e;
             }
         }
         return sb;
     }
  
     public  static StringBuilder connect(URI url)throws IOException,ClientProtocolException{
     	return connect(url.toString());    
     }
     
     public  static StringBuilder connect(String url)throws IOException,ClientProtocolException{
  
         HttpClient httpclient = new DefaultHttpClient();
         StringBuilder result = null;
  
         HttpGet httpget = new HttpGet(url);
         HttpResponse response;
  
         try {
  
             response = httpclient.execute(httpget);
             HttpEntity entity = response.getEntity();
  
             if (entity != null) {
                 InputStream instream = entity.getContent();
                 result= convertStreamToString(instream);
                 instream.close();
             }
             httpclient.getConnectionManager().shutdown();
         } catch (ClientProtocolException e) {
             // TODO Auto-generated catch block
             throw e;
         } catch (IOException e) {
             // TODO Auto-generated catch block
             throw e;
         }
         return result;
     }
     public  static void sendData(URI url,JSONObject data)throws IOException,ClientProtocolException{
     	sendData(url.toString(), data);
     }
     
     public  static void sendData(String url,JSONObject data)throws IOException,ClientProtocolException{
  
         HttpClient httpclient = new DefaultHttpClient();
         HttpPost httpPost = new HttpPost(url);
        httpPost.addHeader("Content-type","application/json");
        
         HttpResponse response;
  
         try {
  
        	StringEntity entity = new StringEntity(data.toString());            
            entity.setContentType("application/json;charset=UTF-8");//text/plain;charset=UTF-8
            entity.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE,"application/json;charset=UTF-8"));
             httpPost.setEntity(entity);
            
             response = httpclient.execute(httpPost);
             HttpEntity input = response.getEntity();
             if (input != null) {
                 // A Simple JSON Response Read
                 InputStream instream = input.getContent();
                 StringBuilder result= convertStreamToString(instream);
                 instream.close();
             }
             httpclient.getConnectionManager().shutdown();
         } catch (ClientProtocolException e) {
             // TODO Auto-generated catch block
             throw e;
         } catch (IOException e) {
             // TODO Auto-generated catch block
             throw e;
         }
     }
  
 }
