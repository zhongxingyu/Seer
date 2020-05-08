 package com.urhola.cheddar.connection;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.HttpURLConnection;
 import java.net.URL;
 
 
 /**
  *
  * @author janikoskela
  */
 final public class Connection {
     
     private final static int TIME_OUT_LENGTH = 2000;
     private final static int HTTP_STATUS_OK = 200;
 
     public static String sendRequest(String url) throws IOException {
        HttpURLConnection urlConnection = null;
	String out = null;
        try {
            URL u = new URL(url);
            urlConnection = (HttpURLConnection) u.openConnection();
            urlConnection.setReadTimeout(TIME_OUT_LENGTH);
            if (urlConnection.getResponseCode() != HTTP_STATUS_OK)
                return null;
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            out = Connection.readStream(in);
        } catch(IOException e) {
              throw new IOException();
        } 
        finally {
            urlConnection.disconnect();
        }
         return out;
     }
     
     private static String readStream(InputStream in) throws IOException {
         String line;
         BufferedReader r = new BufferedReader(new InputStreamReader(in));
         StringBuilder sb = new StringBuilder();
         while((line = r.readLine()) != null) 
                 sb.append(line);
         return sb.toString();
     }
 }
