 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package no.hials.muldvarp.utility;
 
 import android.util.Log;
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 import java.io.*;
 import java.net.URI;
 import java.util.Date;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import no.hials.muldvarp.MuldvarpService;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 
 /**
  *
  * @author kristoffer
  */
 public class DownloadUtilities {
 
     public static InputStream getJSONData(String url, String header) {
         DefaultHttpClient httpClient = new DefaultHttpClient();
         URI uri;
         InputStream data = null;
         try {
             uri = new URI(url);
             HttpGet method = new HttpGet(uri);
             
             if((!header.equals("")) && (header != null))
                 method.setHeader("Authorization", header);
             
             HttpResponse response = httpClient.execute(method);
             data = response.getEntity().getContent();
         } catch (Exception ex) {
             Logger.getLogger(MuldvarpService.class.getName()).log(Level.SEVERE, null, ex);
         }
         return data;
     }
 
     public static Gson buildGson() {
         GsonBuilder builder = new GsonBuilder();
 
         // Register an adapter to manage the date types as long values 
         builder.registerTypeAdapter(Date.class, new DateAdapter());
 
         return builder.create();
     }
 
     public static void cacheThis(Reader json, File f) {
         try {
             BufferedWriter writer = new BufferedWriter(new FileWriter(f));
             char[] buffer = new char[8192];
             int length;
             while ((length = json.read(buffer)) != -1) {
                 writer.write(buffer, 0, length);
             }
             writer.flush();
             writer.close();
             json.close();
         } catch (IOException ex) {
             Log.e("DownloadUtilities", "Failed to cache " + f.getName(), ex);
         }
     }
 }
