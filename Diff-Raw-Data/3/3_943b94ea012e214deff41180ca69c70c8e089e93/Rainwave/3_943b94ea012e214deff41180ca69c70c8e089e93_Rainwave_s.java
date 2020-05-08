 package cc.rainwave.android;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import cc.rainwave.android.api.types.RainwaveException;
 import cc.rainwave.android.api.types.Song;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.content.res.Resources;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.widget.Toast;
 
 public class Rainwave {
     public static void showError(Context ctx, RainwaveException e) {
         showError(ctx, 1, e.getMessage());
         if(e.getCause() != null) {
             Log.e("Rainwave", "Cause", e.getCause());
         }
     }
 
     public static void showError(Context ctx, int resId) {
         Resources r = ctx.getResources();
         showError(ctx, 1, r.getString(resId));
     }
 
     public static void showError(Context ctx, int code, String msg) {
         Message m = ERROR_QUEUE.obtainMessage(code, ctx);
         Bundle data = m.getData();
         data.putString("text", msg);
         m.sendToTarget();
     }
 
     public static void reorderSongs(Song songs[], int from, int to) {
         Song s = songs[from];
         if(to < from) {
             for(int i = from; i > to; i--) {
                 songs[i] = songs[i-1];
             }
         }
         else {
             for(int i = from; i < to; i++) {
                 songs[i] = songs[i+1];
             }
         }
         songs[to] = s;
     }
 
     /**
      * Makes a comma-delimited string out of an array of songs
      * delineating the value of Song.requestq_id.
      * @param requests
      * @return CSV string
      */
     public static String makeRequestQueueString(Song requests[]) {
         if(requests == null || requests.length == 0) return "";
         if(requests.length == 1) return String.valueOf(requests[0].getId());
 
         StringBuilder sb = new StringBuilder();
         sb.append(requests[0].getId());
 
         for(int i = 1; i < requests.length; i++) {
             sb.append(",");
             sb.append(requests[i].getId());
         }
 
         return sb.toString();
     }
 
     /**
      * Parse a Rainwave Uri.
      * 
      * The general format is rw://[userid]:[key]@[hostname]/[stationId] though currently
      * only user ID's and keys are used.
      * 
      * @param uri the uri to parse
      * @param ctx if not null, show a Toast message saying why
      * @return a 2-item array containing User ID and key, or null if the parse failed
      */
     public static String[] parseUrl(final Uri uri, final Context ctx) {
         if(!Rainwave.SCHEME.equals(uri.getScheme())) {
             if(ctx != null) {
                 showError(ctx, R.string.msg_invalidUrl);
             }
         }
         else {
             final String userInfo = uri.getUserInfo();
 
             if(userInfo != null) {
                 return userInfo.split("[:]", 2);
             }
             else if(ctx != null) {
                 showError(ctx, R.string.msg_noUserInfo);
             }
         }
         return null;
     }
 
     public static String getTimeTemplate(Context ctx, long time) {
         long d = time / 86400, h = time / 3600, m = time / 60;
         String template;
         Resources r = ctx.getResources();
         long n;
         if(d > 0) {
             n = d;
             template = r.getString(R.string.template_days);
         }
         else if(h > 0) {
             n = h;
             template = r.getString(R.string.template_hours);
         }
         else if(m > 0) {
             n = m;
             template = r.getString(R.string.template_minutes);
         }
         else {
             n = time;
             template = r.getString(R.string.template_seconds);
         }
         return String.format(template, n);
     }
 
     private static final Handler ERROR_QUEUE = new Handler() {
         public void handleMessage(Message msg) {
             Bundle data = msg.getData();
             Context ctx = (Context) msg.obj;
             String text = data.getString("text");
             Toast.makeText(ctx, text, Toast.LENGTH_LONG).show();
         }
     };
 
     public static final int
         USERID_MAX = 10,
         KEY_MAX = 10;
 
     /** Bundle constants */
     public static final String
         HANDLED_URI = "handled-uri",
         SCHEDULE = "schedule",
         ART = "art";
 
     public static final String
         RAINWAVE_URL = "http://rainwave.cc/api4",
         SCHEME = "rw";
 
     public static final URL    DEFAULT_URL;
 
     static {
         URL tmp;
         try {
             tmp = new URL(RAINWAVE_URL);
         } catch (MalformedURLException e) {
            Log.e("Rainwave", "Rainwave URL is malformed!");
            tmp = null;
         }
         DEFAULT_URL = tmp;
     }
 }
