 package io.hgeg.cloudboard;
 
 import android.app.Activity;
 import android.content.ClipboardManager;
 import android.os.AsyncTask;
 import android.widget.Toast;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.util.EntityUtils;
 
 import java.io.OutputStreamWriter;
 import java.net.HttpURLConnection;
 import java.security.MessageDigest;
 
 public class Push extends AsyncTask<Void, Void, String> {
 
     private boolean exception;
     private Activity context;
     private ClipboardManager clipboard;
 
     protected String doInBackground(Void... none) {
         this.context = MainActivity.model;
         this.clipboard = MainActivity.clipboard;
         this.exception = false;
 
         HttpURLConnection connection;
         OutputStreamWriter request = null;
 
         long timestamp = System.currentTimeMillis()/1000;
        String data = MainActivity.getClipText();
         String raw_sig = timestamp+"&"+md5("sokoban")+"&"+"UZepT6F8abA80DK1ilCz";
         String parameters = "{\"user\":\"Hgeg\"," +
                             " \"timestamp\":"+ timestamp + "," +
                             " \"key\":\"H4vlwkm8tvO8\"," +
                             " \"signature\":\"" + md5(raw_sig) + "\"," +
                             " \"data\":" + "\"" + data + "\"," +
                             " \"uniqueID\":\"cboard-android-0097-162078DM\"}"; // TODO: create actual unique id
         int status = -1;
         try
         {
 
             HttpClient c = new DefaultHttpClient();
             HttpPost p = new HttpPost("http://hgeg.io/cloudboard/set/");
 
             p.setEntity(new StringEntity(parameters));
 
             HttpResponse response = c.execute(p);
             return status+"\n" + parameters + "\n" + EntityUtils.toString(response.getEntity());
 
         } catch(Exception e) {
             this.exception = true;
             return status+"\n"+"r"+"\nparams:\n" + parameters;
         }
 
     }
 
     public static String md5(String message) {
         String digest = null;
         try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(message.getBytes("UTF-8"));
             //converting byte array to Hexadecimal
             StringBuilder sb = new StringBuilder(2*hash.length);
             for(byte b : hash){ sb.append(String.format("%02x", b&0xff)); }
             digest = sb.toString();
         } catch (Exception e) {}
         return digest;
     }
 
     protected void onPostExecute(String data) {
         if(!this.exception) {
         }else {
             Toast.makeText(this.context, "  Error!", Toast.LENGTH_LONG).show();
         }
 
     }
 }
