 package com.github.mobileartisans.bawall.domain;
 
 import android.util.Base64;
 
 import java.io.ByteArrayOutputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.HttpURLConnection;
 import java.net.URL;
 
 public class SimpleHttpClient {
     private final String username;
     private final String password;
     private final String url;
 
     public SimpleHttpClient(String username, String password, String url) {
         this.username = username;
         this.password = password;
         this.url = url;
     }
 
     protected String get() {
         try {
             URL url = new URL(this.url);
             HttpURLConnection con = (HttpURLConnection) url.openConnection();
             con.addRequestProperty("Authorization", "basic " + new String(Base64.encode((username + ":" + password).getBytes(), Base64.NO_WRAP)));
             return getResponseAsString(con);
        } catch (HttpClientException e) {
            throw e;
         } catch (Exception e) {
             throw new HttpClientException(HttpClientException.ClientError.UNKNOWN);
         }
     }
 
     protected String post(String body) {
         return requestWithBody(body, "POST");
     }
 
     public String put(String body) {
         return requestWithBody(body, "PUT");
     }
 
     private String requestWithBody(String body, String method) {
         try {
             URL url = new URL(this.url);
             HttpURLConnection con = (HttpURLConnection) url.openConnection();
             con.setRequestMethod(method);
             con.addRequestProperty("Authorization", "basic " + new String(Base64.encode((username + ":" + password).getBytes(), Base64.NO_WRAP)));
             con.setRequestProperty("Content-Length", Integer.toString(body.getBytes().length));
             con.setUseCaches(false);
             con.setDoInput(true);
             con.setDoOutput(true);
             DataOutputStream wr = new DataOutputStream(con.getOutputStream());
             wr.writeBytes(body);
             wr.flush();
             wr.close();
             return getResponseAsString(con);
        } catch (HttpClientException e) {
            throw e;
         } catch (Exception e) {
             throw new HttpClientException(HttpClientException.ClientError.UNKNOWN);
         }
     }
 
     private String getResponseAsString(HttpURLConnection con) throws IOException {
         int responseCode = con.getResponseCode();
         if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
             throw new HttpClientException(HttpClientException.ClientError.NOT_FOUND);
         }
         if (responseCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
             throw new HttpClientException(HttpClientException.ClientError.UNAUTHORIZED);
         }
         if (responseCode > 300) {
             throw new HttpClientException(HttpClientException.ClientError.UNKNOWN);
         }
         InputStream inputStream = con.getInputStream();
         ByteArrayOutputStream content = new ByteArrayOutputStream();
 
         int readBytes = 0;
         byte[] sBuffer = new byte[1000];
         while ((readBytes = inputStream.read(sBuffer)) != -1) {
             content.write(sBuffer, 0, readBytes);
         }
         return new String(content.toByteArray());
     }
 }
