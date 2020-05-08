 package net.sourcewalker.garanbot.api;
 
 import java.io.IOException;
 import java.io.InputStream;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpStatus;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.util.EncodingUtils;
 
 import android.util.Base64;
 
 public class GaranboClient {
 
     private final String username;
     private final String password;
     private final DefaultHttpClient client;
     private final UserService userService;
 
     public GaranboClient(String username, String password) {
         this.username = username;
         this.password = password;
 
         client = new DefaultHttpClient();
 
         userService = new UserService();
     }
 
     /**
      * Returns true, if the service is available.
      * 
      * @return True, if service is available.
      */
     public boolean ping() {
         try {
             HttpResponse response = get("/ping");
             if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                 return true;
             }
         } catch (IOException e) {
         }
         return false;
     }
 
     public UserService user() {
         return userService;
     }
 
     protected HttpResponse get(String path) throws IOException {
         HttpGet request = new HttpGet(ApiConstants.BASE + path);
         prepareRequest(request);
         return client.execute(request);
     }
 
     private void prepareRequest(HttpGet request) {
         request.addHeader("Content-type", "application/json");
         request.addHeader("Accept", "application/json");
         request.addHeader("open-api", ApiConstants.KEY);
         request.addHeader("Authorization", getAuthHeader());
     }
 
     private String getAuthHeader() {
        byte[] input = String.format("%s:%s", username, password).getBytes();
        return "Basic " + Base64.encodeToString(input, Base64.NO_WRAP);
     }
 
     public String readEntity(HttpResponse response) {
         StringBuilder sb = new StringBuilder();
         try {
             InputStream stream = response.getEntity().getContent();
             byte[] buffer = new byte[1024];
             int read;
             do {
                 read = stream.read(buffer);
                 if (read != -1) {
                    if (read == buffer.length) {
                        sb.append(EncodingUtils.getString(buffer, "utf-8"));
                    } else {
                        sb.append(EncodingUtils.getString(buffer, 0, read,
                                "utf-8"));
                    }
                 }
             } while (read != -1);
         } catch (IOException e) {
             sb.append("ERROR: " + e.getMessage());
         }
         return sb.toString();
     }
 
 }
