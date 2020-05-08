 package de.knittig.spike.hudson.client;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.HttpMethodBase;
 import org.apache.commons.httpclient.HttpStatus;
 import org.apache.commons.httpclient.methods.PostMethod;
 import org.apache.commons.httpclient.methods.StringRequestEntity;
 
 
 public class HudsonClientImpl implements HudsonClient {
 
     private final String hudsonUrl;
     private final HttpClient httpClient;
 
     public HudsonClientImpl(String hudsonUrl) {
         this.hudsonUrl = hudsonUrl;
         this.httpClient = new HttpClient();
     }
 
     public void createJob(String name, String xmlConfiguration) {
         PostMethod createItemRequest = new PostMethod(hudsonUrl + "/createItem");
        createItemRequest.setParameter("name", name);
         try {
             createItemRequest.setRequestEntity(new StringRequestEntity(xmlConfiguration, "text/xml", "UTF-8"));
        } catch (UnsupportedEncodingException e) {
             throw new RuntimeException(e);
         }
        createItemRequest.setRequestHeader("Content-type","text/xml; charset=UTF-8");
 
         executeMethod(createItemRequest);
 
         if (createItemRequest.getStatusCode() == HttpStatus.SC_OK) {
             System.out.println("Created job!");
         } else {
             System.err.println("Failed to job!");
         }
     }
 
     private void executeMethod(HttpMethodBase method) {
         try {
             httpClient.executeMethod(method);
         } catch (IOException e) {
             throw new RuntimeException(e);
         } finally {
             method.releaseConnection();
         }
     }
 
 }
