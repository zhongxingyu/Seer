 package com.gradians.collect;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.PrintWriter;
 import java.net.HttpURLConnection;
 import java.net.URL;
 
 import android.app.Activity;
 import android.os.AsyncTask;
import android.util.Log;
 
 public class ImageUploadTask extends AsyncTask<File, Void, String> implements IConstants {
     
     public ImageUploadTask(ITaskCompletedListener taskCompletedListener) {
         this.taskCompletedListener = taskCompletedListener;
     }        
 
     @Override
     protected String doInBackground(File... images) {
         
         URL url = null;
         HttpURLConnection httpUrlConnection = null;
         OutputStream ostream = null;
         PrintWriter opstream = null;        
         String boundary =  null;
         String[] params = null;
         String result = null;
         for (File image : images) {
            Log.v(TAG, image.getName());
             params = image.getName().split("\\.");
             boundary = String.valueOf(System.currentTimeMillis());
             try {
                 url = new URL(String.format(BASE_URL,
                         BANK_HOST_PORT, params[0], params[1]));
                 httpUrlConnection = (HttpURLConnection) url.openConnection();
                 httpUrlConnection.setDoOutput(true);
 
                 httpUrlConnection.setRequestMethod("POST");
                 httpUrlConnection.setRequestProperty("Connection", "Keep-Alive");
                 httpUrlConnection.setRequestProperty("Cache-Control", "no-cache");
                 httpUrlConnection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                 
                 ostream = httpUrlConnection.getOutputStream();
                 opstream = new PrintWriter(ostream); 
 
                 opstream.append("--" + boundary).append(CRLF);
                 opstream.append("Content-Disposition: form-data; name=\"image\"; filename=\"" + 
                         image.getName() + "\"").append(CRLF);
                 opstream.append("Content-Type: " + 
                         HttpURLConnection.guessContentTypeFromName(image.getName())).append(CRLF);
                 opstream.append("Content-Transfer-Encoding: binary").append(CRLF);
                 opstream.append(CRLF).flush();
                 
                 InputStream imgstream = null;
                 byte[] buffer = new byte[1024];
                 imgstream = new FileInputStream(image);
                 for (int length = 0; (length = imgstream.read(buffer)) > 0;) {
                     ostream.write(buffer, 0, length);
                 }
                 imgstream.close();
                 ostream.flush(); // Important! Output cannot be closed. Close of writer will close output as well.
                 opstream.append(CRLF).flush(); // CRLF is important! It indicates end of binary boundary.
                 
                 // End of multipart/form-data.
                 opstream.append(DASH_DASH + boundary + DASH_DASH).append(CRLF);            
                 opstream.close();
                 
                 if (httpUrlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                     image.delete();
                     result = image.getName();
                 } else {
                     result = null;
                 }
             } catch (Exception e) {
                 result = null;
             } finally {
                 if (opstream != null) opstream.close();
             }
         }
         return result;
     }
 
     @Override
     protected void onPostExecute(String result) {
         taskCompletedListener.onTaskResult(ITaskCompletedListener.UPLOAD_IMAGE_TASK_RESULT_CODE,
                 result != null ? Activity.RESULT_OK : Activity.RESULT_FIRST_USER, result);
     }
     
     private ITaskCompletedListener taskCompletedListener;
     
     private static final String CRLF = "\r\n", DASH_DASH = "--";
     private static final String BASE_URL = "http://%s/Upload/scan?type=%s&id=%s";
 }
