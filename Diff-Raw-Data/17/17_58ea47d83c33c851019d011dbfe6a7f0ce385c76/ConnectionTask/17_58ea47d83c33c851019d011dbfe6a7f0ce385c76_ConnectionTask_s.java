 
 package com.huskysoft.eduki.data;
 
 import android.content.Context;
 import android.content.Intent;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.AsyncTask;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.HttpURLConnection;
 import java.net.URL;
 
 import com.huskysoft.eduki.NoConnectivityActivity;
 import com.huskysoft.eduki.TaskComplete;
 
 /**
  * @author Cody Thomas Class ConnectionTask will make basic GET requests to a
  *         provided url, passing the data to a callback represented by a
  *         TaskComplete object.
  */
 public class ConnectionTask extends AsyncTask<String, Void, String> {
 
     /**
      * The callback to pass the result to when the request is complete
      */
     private TaskComplete callback;
     private int id;
 
     /**
      * Initializes a connectionTask object
      * 
      * @param callback The object to pass the result to.
      */
     public ConnectionTask(TaskComplete callback, int id) {
         this.callback = callback;
         this.id = id;
     }
 
     @Override
     protected String doInBackground(String... urls) {
         HttpURLConnection conn = null;
         try {
             URL url;
             url = new URL(urls[0]);
             conn = (HttpURLConnection) url.openConnection();
             conn.setRequestMethod("GET");
             conn.connect();
             BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
             StringBuilder sb = new StringBuilder();
             String line = rd.readLine();
             while (line != null) {
                 sb.append(line + '\n');
                 line = rd.readLine();
             }
             return sb.toString();
         } catch (IOException e) {
             try {
                 int respCode = conn.getResponseCode();
                 return "Error" + respCode;
             } catch(Exception ex) {
                 return null;
             }
         } catch (Exception e) {
             e.printStackTrace();
             return null;
         }
     }
 
     @Override
     protected void onPostExecute(String response) {
         callback.taskComplete(response, id);
     }
     
     /**
      * Check internet connectivity 
      * 
      * @param activity activity that is currently running
      * @return true if there is internet connectivity, false otherwise
      */
     public static boolean isOnline(Context activity) {
         ConnectivityManager cm = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
         NetworkInfo netInfo = cm.getActiveNetworkInfo();
         return netInfo != null && netInfo.isConnectedOrConnecting();
     }   
     
     public static void startNoConnectivityActivity(Context activity) {
         Intent intent = new Intent(activity, NoConnectivityActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
         activity.startActivity(intent);
     }
 }
