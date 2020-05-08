 package com.carlgo11.preventip.pastebin;
 
 import java.io.BufferedReader;
 import java.io.DataOutputStream;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.UnsupportedEncodingException;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.net.URLEncoder;
 
 public class Pastebin {
 
     static public String token = "b558dbec597603c726d31633634f294b";
     static public String devkey = "9e7c871d87d0e51a0ee185b4c55ab173";
     static public String pasteURL = "http://www.pastebin.com/api/api_post.php";
 
     public Pastebin()
     {
     }
 
     static public String checkResponse(String response)
     {
        if (response.substring(0, 15).equals("Bad API request")) {
             return response.substring(17);
         }
         return "";
     }
 
     static public String makePaste(String code, String name, String format)
             throws UnsupportedEncodingException
     {
         String content = URLEncoder.encode(code, "UTF-8");
         String title = URLEncoder.encode(name, "UTF-8");
         String data = "api_option=paste&api_user_key=" + Pastebin.token
                 + "&api_paste_private=0&api_paste_name=" + title
                 + "&api_paste_expire_date=1M&api_paste_format=" + format
                 + "&api_dev_key=" + Pastebin.devkey + "&api_paste_code=" + content;
         String response = Pastebin.page(Pastebin.pasteURL, data);
         String check = Pastebin.checkResponse(response);
         if (!check.equals("")) {
             return check;
         }
         return response;
     }
 
     static public String page(String uri, String urlParameters)
     {
         URL url;
         HttpURLConnection connection = null;
         try {
             // Create connection
             url = new URL(uri);
             connection = (HttpURLConnection) url.openConnection();
             connection.setRequestMethod("POST");
             connection.setRequestProperty("Content-Type",
                     "application/x-www-form-urlencoded");
 
             connection.setRequestProperty("Content-Length",
                     "" + Integer.toString(urlParameters.getBytes().length));
             connection.setRequestProperty("Content-Language", "en-US");
 
             connection.setUseCaches(false);
             connection.setDoInput(true);
             connection.setDoOutput(true);
 
             // Send request
             DataOutputStream wr = new DataOutputStream(
                     connection.getOutputStream());
             wr.writeBytes(urlParameters);
             wr.flush();
             wr.close();
 
             // Get Response
             InputStream is = connection.getInputStream();
             BufferedReader rd = new BufferedReader(new InputStreamReader(is));
             String line;
             StringBuffer response = new StringBuffer();
             while ((line = rd.readLine()) != null) {
                 response.append(line);
             }
             rd.close();
             return response.toString();
 
         } catch (Exception e) {
             e.printStackTrace();
             return null;
 
         } finally {
 
             if (connection != null) {
                 connection.disconnect();
             }
         }
     }
 }
