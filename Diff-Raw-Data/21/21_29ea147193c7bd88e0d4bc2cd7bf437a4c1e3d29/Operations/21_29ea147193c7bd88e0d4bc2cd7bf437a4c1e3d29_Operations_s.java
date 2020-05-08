 package com.haligali.PowerHangmanClient;
 
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.io.InputStreamReader;
 import java.io.BufferedReader;
 import java.lang.StringBuffer;
 import org.json.JSONObject;
 
 /**
  * Created with IntelliJ IDEA.
  * User:
  * Date: 10/9/13
  * Time: 11:47 PM
  * To change this template use File | Settings | File Templates.
  */
 public class Operations {
 
     // SERVER IP ADDRESS
     // do a  ifconfig -a   on your mac to find the local address of your mac on WiFi or ethernet
    private static final String ipaddr = "192.168.1.237";
 
     public static String login(String nickname, String password) {
 
         String pid = null;
         try
         {
             // TODO REFACTOR ME...
             // Should centralize this code as multiple operations will call it.
            URL url = new URL("http://" + ipaddr + "/phm/Login?nn=" + nickname + "&pw=" + password);
 
             HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
             try
             {
                 InputStreamReader in = new InputStreamReader(urlConnection.getInputStream());
                 BufferedReader br = new BufferedReader(in);
                 StringBuffer sb = new StringBuffer();
                 String line;
                 while ((line = br.readLine()) != null) {
                     sb.append(line);
                 }
                 JSONObject json = new JSONObject(sb.toString());
 
                 if (json.has("pid"))
                 {
                     pid = json.getString("pid");
                 }
             }
             finally
             {
                 urlConnection.disconnect();
             }
         }
         catch(Exception ex)
         {
            // TODO log ex

         }
 
         return pid;
     }
 }
