 package com.asquera.web;
 
 import java.io.BufferedReader;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.URL;
 
 import javax.crypto.Mac;
 
 import sun.net.www.protocol.http.HttpURLConnection;
 
 import com.asquera.hmac.RequestParams;
 import com.asquera.hmac.WardenHMacSigner;
 
 public class Client {
     
     public static String sendGetRequest(String targetUrl) throws Exception {
         WardenHMacSigner signer = new WardenHMacSigner(Mac.getInstance("HmacSHA1"));
         RequestParams options = new RequestParams();
         String signedUrl = signer.signUrl(targetUrl, "TESTSECRET", options);
         
         URL url = new URL(signedUrl);
         HttpURLConnection connection = (HttpURLConnection) url.openConnection();
         connection.setRequestMethod("GET");
         
         connection.setDoInput(true);
         connection.setUseCaches(false);
         
         InputStream is = connection.getInputStream();
         BufferedReader reader = new BufferedReader(new InputStreamReader(is));
         
         String line = "";
         StringBuffer response = new StringBuffer();
         while ((line = reader.readLine()) != null) {
             response.append(line).append("\n");
         }
         
         reader.close();
         return response.toString();
     }
     
     public static void main(String[] args) {
         try {
             String url = "http://localhost:4567/test";
             String response = sendGetRequest(url);
             
             System.out.println(response);
             
         } catch (Exception e) {
            System.out.println("failed to send request");
         }
     }
 }
 
