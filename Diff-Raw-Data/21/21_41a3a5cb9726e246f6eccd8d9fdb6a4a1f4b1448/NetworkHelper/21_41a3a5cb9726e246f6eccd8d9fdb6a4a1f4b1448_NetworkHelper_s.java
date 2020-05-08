 /*
    Copyright 2012 Mikhail Chabanov
 
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
 
        http://www.apache.org/licenses/LICENSE-2.0
 
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
  */
 package mc.lib.network;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.MalformedURLException;
 import java.net.Socket;
 import java.net.URL;
 import java.net.URLConnection;
 
 import mc.lib.stream.StreamHelper;
 
 import org.json.JSONObject;
 import org.w3c.dom.Document;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.drawable.Drawable;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.util.Log;
 
 public class NetworkHelper
 {
    private static final String LOGTAG = NetworkHelper.class.getCanonicalName();
     public static final String DEFAULT_ENCODING = "UTF8";
 
     private static String TEST_SERVER1 = "google.com";
     private static String TEST_SERVER2 = "baidu.com";
     private static String TEST_SERVER3 = "bing.com";
 
     public static boolean hasConnection(Context context)
     {
         ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
         NetworkInfo ni = cm.getActiveNetworkInfo();
         if(ni != null && ni.isAvailable() && ni.isConnected())
         {
            if(testConnection(TEST_SERVER1) || testConnection(TEST_SERVER2) || testConnection(TEST_SERVER3))
            {
                Log.i(LOGTAG, "Internet connection detected");
                return true;
            }
         }
 
         Log.i(LOGTAG, "No internet connection detected");
         return false;
     }
 
     private static boolean testConnection(String url)
     {
         Socket s = null;
         boolean res = false;
         try
         {
             s = new Socket(url, 80);
             if(s.isBound() && s.isConnected())
             {
                 res = true;
             }
         }
         catch(IOException e)
         {
             Log.e(LOGTAG, "Error on testConnection to " + url, e);
         }
         finally
         {
             if(s != null)
                 StreamHelper.close(s);
         }
         return res;
     }
 
     public static void saveToFile(String url, File file)
     {
         InputStream is = null;
         OutputStream os = null;
         try
         {
             URL u = new URL(url);
             URLConnection c = u.openConnection();
             is = c.getInputStream();
             os = new FileOutputStream(file);
             Log.i(LOGTAG, "Reading timeout:" + c.getReadTimeout());
             StreamHelper.copyNetworkStream(is, os, c.getReadTimeout());
         }
         catch(MalformedURLException e)
         {
             Log.e(LOGTAG, "Wrong url format", e);
         }
         catch(IOException e)
         {
             Log.e(LOGTAG, "Can not open stream", e);
         }
         finally
         {
             StreamHelper.close(os);
             StreamHelper.close(is);
         }
     }
 
     public static String getAsText(String url)
     {
         return getAsText(url, DEFAULT_ENCODING);
     }
 
     public static String getAsText(String url, String encoding)
     {
         InputStream is = null;
         try
         {
             URL urlo = new URL(url);
             URLConnection c = urlo.openConnection();
             is = c.getInputStream();
             Log.i(LOGTAG, "Reading timeout:" + c.getReadTimeout());
             return StreamHelper.readNetworkStream(is, encoding, c.getReadTimeout());
         }
         catch(MalformedURLException e)
         {
             Log.e(LOGTAG, "Wrong url format", e);
         }
         catch(IOException e)
         {
             Log.e(LOGTAG, "Can not open stream", e);
         }
         finally
         {
             StreamHelper.close(is);
         }
         return null;
     }
 
     public static Bitmap getBitmap(String url)
     {
 
         return null;
     }
 
     public static Drawable getDrawable(String url)
     {
 
         return null;
     }
 
     public static Document getXml(String url)
     {
 
         return null;
     }
 
     public static JSONObject getJson(String url)
     {
 
         return null;
     }
 }
