 package com.RoboMobo;
 
 import android.util.Log;
import org.apache.http.util.ByteArrayBuffer;
import org.json.JSONException;
 import org.json.JSONObject;
 
 import java.io.BufferedInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 
 /**
  * Created with IntelliJ IDEA.
  * User: loredan
  * Date: 03.08.13
  * Time: 10:05
  * To change this template use File | Settings | File Templates.
  */
 public class Networking
 {
     public static HttpURLConnection connection;
     public static URL url;
     public static InputStream in;
     public static OutputStream out;
 
     public static void init(String _url)
     {
         try
         {
             url = new URL(_url);
             connection = (HttpURLConnection) url.openConnection();
         }
         catch (MalformedURLException e)
         {
             Log.wtf("URL", e.getMessage());
         }
         catch (IOException e)
         {
             Log.wtf("IO", e.getMessage());
         }
     }
 
     public static JSONObject get(JSONObject send)
     {
        JSONObject recieved;
         try
         {
             InputStream is = connection.getInputStream();
             OutputStream os = connection.getOutputStream();
             os.write(send.toString().getBytes());
             while (is.available()==0);
            byte[] raw = new byte[is.available()];
            is.read(raw);
            recieved = new JSONObject(new String(raw));
            is.close();
            os.close();
            return recieved;
 
         } catch (IOException e)
         {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (JSONException e)
        {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
         return null;
     }
 }
