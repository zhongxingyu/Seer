 package org.gnuton.newshub.tasks;
 
 import android.os.AsyncTask;
 import android.util.Log;
 
 import org.gnuton.newshub.utils.Notifications;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.util.zip.GZIPInputStream;
 
 /**
  * mListener must implement OnRequestCompleted.
  */
 public class DownloadWebTask extends AsyncTask<String, Void, byte[]>{
     private static final String TAG = "DOWNLOAD_WEB_TASK";
 
     private static OnRequestCompletedListener mListener;
     public DownloadWebTask(Object o) {
         if (o instanceof OnRequestCompletedListener) {
             mListener = (OnRequestCompletedListener) o;
         } else {
             throw new ClassCastException(o.toString() + " must implement DownloadWebTask.OnRequestCompletedListener");
         }
     }
 
     @Override
     protected byte[] doInBackground(String... urls) {
         try {
             return downloadUrl(urls[0]);
         } catch (IOException e) {
             e.printStackTrace();
             return null;
         }
     }
 
     @Override
     protected void onPostExecute(byte[] s) {
         mListener.onRequestCompleted(s);
     }
 
     static public byte[] downloadUrl(String url) throws IOException {
         InputStream is = null;
         Log.d(TAG, "Downloading: " + url);
         try {
             URL u = new URL(url);
             HttpURLConnection conn = (HttpURLConnection) u.openConnection();
             conn.setInstanceFollowRedirects(true);
 
             conn.setReadTimeout(10 * 1000);
             conn.setConnectTimeout(10 * 1000);
             conn.setRequestMethod("GET");
             conn.setDoInput(true);
             //conn.connect();
 
             int status = conn.getResponseCode();
             if (status == HttpURLConnection.HTTP_MOVED_TEMP || status == HttpURLConnection.HTTP_MOVED_PERM || status == 307 || status == HttpURLConnection.HTTP_SEE_OTHER)
             {
                 String redUrl = conn.getHeaderField("Location");
                 String cookies = conn.getHeaderField("Set-Cookie");
 
                 Log.d(TAG, "Redirect URL:>" + redUrl + "<");
                 conn = (HttpURLConnection) new URL(redUrl).openConnection();
                 conn.setRequestProperty("Cookie", cookies);
            } else if (status == -1 ) {
                if (conn.getURL().equals(url))
                    throw new IOException("Unable to read data from server.");
                else
                    return downloadUrl(conn.getURL().toString());
             }
 
             Log.d(TAG, "Status: " + status);
             is = conn.getInputStream();
 
             if ("gzip".equals(conn.getContentEncoding())) {
                 Log.d(TAG,"GZIP input stream");
                 is = new GZIPInputStream(is);
             }
            byte[] data =getBytesFromInputStream(is);
            String s = new String(data);
            return data;
         } catch(Exception e) {
             Notifications.showMsg(e.getMessage());
             return null;
         }
         finally {
             if (is != null)
                 is.close();
         }
     }
 
 
     //FIXME this could return an encoded string! USe XMLPullParser with the first part
     //of the data, then get the encoding and and concatenate the data to make the string
     private static byte[] getBytesFromInputStream(InputStream is) throws IOException {
         ByteArrayOutputStream buffer = new ByteArrayOutputStream();
         int nRead;
         byte[] data = new byte[16384];
 
         while ((nRead = is.read(data, 0, data.length)) != -1) {
             buffer.write(data, 0, nRead);
         }
 
         buffer.flush();
         return buffer.toByteArray();
     }
 
     public interface OnRequestCompletedListener {
         public void onRequestCompleted(final byte[] buffer);
     }
 }
