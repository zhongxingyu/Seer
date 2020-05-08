 package de.hszigr.mobileapps.questionnaire.client.rest;
 
 import java.io.IOException;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPut;
 import org.apache.http.client.methods.HttpUriRequest;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.util.EntityUtils;
 
 public class RestClient {
 
     private final HttpClient client = new DefaultHttpClient();
 
     public String get(final String url) throws IOException {
         HttpUriRequest request = new HttpGet(url);
 
         HttpResponse response = client.execute(request);
         HttpEntity entity = response.getEntity();
 
         return EntityUtils.toString(entity);

     }
 
     public String put(final String url, final String data) {
         try {
             HttpPut request = new HttpPut(url);
 
             StringEntity stringEntity = new StringEntity(data, "UTF-8");
             stringEntity.setContentType("text/xml");
 
             request.setEntity(stringEntity);
 
             HttpResponse response = client.execute(request);
             HttpEntity entity = response.getEntity();
 
             return EntityUtils.toString(entity);
         } catch (IOException e) {
             return e.toString();
         }
     }
 }
