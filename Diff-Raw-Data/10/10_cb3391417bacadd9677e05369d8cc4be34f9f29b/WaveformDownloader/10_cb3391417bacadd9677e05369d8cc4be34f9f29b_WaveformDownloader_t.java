 package com.github.kaeppler.sawtoothtapestry.waveform;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.os.Handler;
 import android.os.Message;
 import android.util.Log;
 
 import com.github.ignition.support.cache.ImageCache;
 import com.github.ignition.support.images.remote.RemoteImageLoaderHandler;
 import com.github.ignition.support.images.remote.RemoteImageLoaderJob;
 import com.github.kaeppler.sawtoothtapestry.R;
 import com.github.kaeppler.sawtoothtapestry.R.id;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class WaveformDownloader extends Thread {
 
     private static final String TAG = WaveformDownloader.class.getSimpleName();
     private static final int CONN_TIMEOUT = 20000;
 
     private String url;
     private Handler handler;
 
     public WaveformDownloader(String url, Handler handler) {
         this.url = url.replaceFirst("https://[^.]+\\.", "https://wis.");
         this.handler = handler;
     }
 
     @Override
     public void run() {
 
         HttpURLConnection conn = null;
         try {
             Log.d(TAG, "Fetching waveform data from graphite: " + url);
 
             conn = (HttpURLConnection) new URL(url).openConnection();
             conn.setConnectTimeout(CONN_TIMEOUT);
             conn.setReadTimeout(CONN_TIMEOUT);
             conn.setUseCaches(true);
 
             BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
             StringBuilder sb = new StringBuilder();
             String line = br.readLine();
             while (line != null) {
                 sb.append(line);
                 line = br.readLine();
             }
             br.close();
 
             JSONObject data = new JSONObject(sb.toString());
 
             JSONArray jsonArray = data.getJSONArray("samples");
             int[] samples = new int[jsonArray.length()];
             for (int i = 0; i < jsonArray.length(); i++) {
                 samples[i] = jsonArray.getInt(i);
             }
             WaveformData waveformData = new WaveformData(data.getInt("width"), data.getInt("height"), samples);
             Message message = handler.obtainMessage(R.id.message_waveform_downloaded, waveformData);
             handler.sendMessage(message);
 
 
         } catch (IOException e) {
             e.printStackTrace();
         } catch (JSONException e) {
             e.printStackTrace();
         } finally {
             if (conn != null) {
                 conn.disconnect();
             }
         }
     }
 }
