 package com.darkprograms.gaio.network;
 
 import com.darkprograms.gaio.device.DeviceManager;
 import com.darkprograms.gaio.util.Constants;
 
 import java.io.*;
 import java.net.URL;
 import java.net.URLConnection;
 
 /**
  * Created with IntelliJ IDEA.
  * User: theshadow
  * Date: 12/29/12
  * Time: 3:28 PM
  * To change this template use File | Settings | File Templates.
  */
 public class NetworkUtil {
 
     private static NetworkUtil instance;
 
     public static NetworkUtil getInstance() {
         if (instance == null) {
             instance = new NetworkUtil();
         }
         return instance;
     }
 
     private NetworkUtil() {
 
     }
 
     private int length = -1;
     private int downloaded;
 
     public String getJSON(String jsonFile) {
         try {
             URLConnection urlConnection = new URL(Constants.GAIO_URL_BASE + jsonFile).openConnection();
             BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
 
             String line;
             String json = "";
 
             while ((line = bufferedReader.readLine()) != null) {
                 json += line;
             }
 
             bufferedReader.close();
             return json;
 
         } catch (Exception ex) {
             ex.printStackTrace();
         }
         return null;
     }
 
     public void downloadFile(String filename) {
         try {
            setLength(-1);
             URLConnection urlConnection = new URL(Constants.GAIO_URL_BASE + filename).openConnection();
             InputStream inputStream = urlConnection.getInputStream();
             FileOutputStream fileOutputStream = new FileOutputStream(new File(DeviceManager.getInstance().getTmpDir() + "/gaio/" + filename));
             setLength(urlConnection.getContentLength());
             setDownloaded(0);
 
             byte[] buffer = new byte[512];
 
             int read;
 
             while ((read = inputStream.read(buffer, 0, 512)) != -1) {
                 fileOutputStream.write(buffer, 0, read);
                 setDownloaded(getDownloaded() + read);
             }
 
             inputStream.close();
             fileOutputStream.close();
 
         } catch (Exception ex) {
             ex.printStackTrace();
         }
     }
 
     private void setLength(int length) {
         this.length = length;
     }
 
     public synchronized int getLength() {
         return length;
     }
 
     private void setDownloaded(int downloaded) {
         this.downloaded = downloaded;
     }
 
     public synchronized int getDownloaded() {
         return downloaded;
     }
 
     public int getPercentage(int current, int total) {
         double divided = (double) current / (double) total;
 
         return (int) (divided * 100);
     }
 
 }
