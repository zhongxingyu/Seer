 package com.davidykay.lazyfood.network;
 
 import java.io.IOException;
 import java.io.OutputStream;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.ProtocolException;
 import java.net.URL;
 
 import android.os.AsyncTask;
 
 import com.davidykay.lazyfood.iface.FailureCallback;
 import com.davidykay.lazyfood.iface.SuccessCallback;
 import com.google.inject.Inject;
 
 public class NetworkHelper {
  // private static final String HOST_NAME = "10.199.142.108";
  // private static final String HOST_NAME = "www.davidykay.com";
  private static final String HOST_NAME = "192.168.43.185";
 
  //private static final String serverAddress = "10.199.142.108/api/tray/";
  private static final String SERVER_ADDRESS = HOST_NAME;
  private static final String SERVER_PATH = "/api/tray/";
 
  private static final int SERVER_PORT = 8000;
 
   @Inject
   public NetworkHelper() {
   }
 
   public void orderTrayFromAPIAsync(int trayId,
                                     SuccessCallback success,
                                     FailureCallback failure
                                    ) {
     new PostOrderTask(success, failure).execute(trayId);
   }
 
   public class PostOrderTask extends AsyncTask<Integer, Void, String> {
     private static final String TAG = "PostOrderTask";
 
     private SuccessCallback mSuccess;
     private FailureCallback mFailure;
 
     public PostOrderTask(SuccessCallback success, FailureCallback failure) {
       mSuccess = success;
       mFailure = failure;
     }
 
     @Override
     protected String doInBackground(Integer... params) {
 
       URL url;
       try {
        url = new URL("http://" + SERVER_ADDRESS + ":" + String.valueOf(SERVER_PORT) + SERVER_PATH);
       } catch (MalformedURLException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
         return null;
       }
       HttpURLConnection conn;
       try {
         conn = (HttpURLConnection) url.openConnection();
       } catch (IOException e) {
         e.printStackTrace();
         return null;
       }
 
       conn.setDoOutput(true);
       try {
         conn.setRequestMethod("POST");
       } catch (ProtocolException e1) {
         e1.printStackTrace();
         return null;
       }
 
       byte[] postData;
       postData = new byte[] {};
       conn.setRequestProperty("Content-Length", Integer.toString(postData.length));
       conn.setUseCaches(false);
 
       OutputStream out;
       String responseBody = null;
       try {
         out = conn.getOutputStream();
 
         out.write(postData);
 
         int responseCode = conn.getResponseCode();
         responseBody = conn.getResponseMessage();
         out.close();
       } catch (IOException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
       }
 
       return responseBody;
     }
 
     @Override
     protected void onPostExecute(String result) {
       mSuccess.onSuccess();
     }
 
   }
 
 }
