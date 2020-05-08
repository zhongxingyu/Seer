 package org.quuux.eyecandy;
 
 import android.content.Context;
 import android.database.Cursor;
 import android.net.Uri;
 
 import java.io.File;
 
 import org.json.JSONObject;
 
 import java.security.MessageDigest;
 
 public class Image {
 
     public enum Source {
         REDDIT
     };
     
     public enum Status {
         NOT_FETCHED,
         FETCHED,
         FAILED
     };
 
     protected Source source;
     protected String url, sourceUrl, title;
     protected Status status;
     protected int timesShown;
 
     protected Image(Source source, String url, String sourceUrl, String title, Status status, int timesShown) {
         this.source = source;
         this.url = url;
         this.sourceUrl = sourceUrl;
         this.title = title;
         this.status = status;
         this.timesShown = timesShown;
     }
 
     public static Image fromReddit(JSONObject json) {
         String url = json.optString("url");
 
         // FIXME hacked
         if (!url.endsWith(".jpg") && !url.endsWith(".jpeg")) {
             return null;
         }
 
         String sourceUrl = json.optString("permalink");
         if (!sourceUrl.startsWith("http://reddit.com")) {
             sourceUrl = "http://reddit.com" + sourceUrl;
         }
         
         String title = json.optString("title");
         
         return new Image(Source.REDDIT, url, sourceUrl, title, Status.NOT_FETCHED, 0);
     }
 
     public static Image from(Cursor cursor) {
         Source source = Source.valueOf(Utils.getString(cursor, "source"));
         String url = Utils.getString(cursor, "url");
         String sourceUrl = Utils.getString(cursor, "source_url");
         String title = Utils.getString(cursor, "title");
         Status status = Status.valueOf(Utils.getString(cursor, "status"));
         int timesShown = Utils.getInt(cursor, "times_shown");
 
         return new Image(source, url, sourceUrl, title, status, timesShown);
     }
 
     public String toString() {
         return "Image(" + url + ")";
     }
     
     public Source getSource() {
         return source;
     }
 
     public String getUrl() {
         return url;
     }
     
     public String getSourceUrl() {
         return sourceUrl;
     }
 
     public String getTitle() {
         return title;
     }
 
     public Status getStatus() {
         return status;
     }
 
     public int getTimesShown() {
         return timesShown;
     }
 
     // FIXME needs to be hash of url
     public String getCachedImagePath(Context context) {
         try {
             MessageDigest message_digest = MessageDigest.getInstance("MD5");
             message_digest.update(url.getBytes());

            byte digest[] = message_digest.digest();
            StringBuffer buf = new StringBuffer();
            for (int i=0; i<digest.length; i++)
                buf.append(Integer.toHexString(0xFF & digest[i]));

            return context.getExternalCacheDir().getPath() + "/" + buf.toString() + ".jpg";
         } catch(Exception e) {
             return null;
         }
     }
 
     public Uri getCachedImageUri(Context context) {
         return Uri.parse("file://" + getCachedImagePath(context));
     }
     public boolean isCached(Context context) {
         return new File(getCachedImagePath(context)).exists();
     }    
 }
 
