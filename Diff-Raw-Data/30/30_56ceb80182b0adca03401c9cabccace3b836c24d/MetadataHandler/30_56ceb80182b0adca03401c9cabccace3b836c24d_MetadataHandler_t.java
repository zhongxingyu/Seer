 package org.xmms2.server;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.text.TextUtils;
 
 import java.io.File;
 import java.io.UnsupportedEncodingException;
 import java.net.URLDecoder;
 import java.util.HashSet;
 import java.util.Set;
 
 /**
  * @author Eclipser
  */
 public class MetadataHandler
 {
     private final CoverArtSource cache;
 
     private String url;
     private String title;
     private String artist;
     private String pictureFront;
 
     public MetadataHandler(Context context)
     {
         cache = new CoverArtSource(context, 8 * 1024 * 1024);
     }
 
     private Set<MetadataListener> metadataListeners = new HashSet<MetadataListener>();
 
     private void setCurrentlyPlayingInfo(String url, String artist, String title, String pictureFront)
     {
         try {
             this.url = new File(URLDecoder.decode(url, "UTF-8")).getName();
         } catch (UnsupportedEncodingException e) {
             this.url = url;
         }
         this.title = title;
         this.artist = artist;
         this.pictureFront = pictureFront;
 
         for (MetadataListener metadataListener : metadataListeners) {
             metadataListener.metadataChanged(this);
         }
     }
 
     public String getTitle()
     {
         return !TextUtils.isEmpty(title) ? title : url;
     }
 
     public String getArtist()
     {
         return artist != null ? artist : "";
     }
 
     public String getTicker()
     {
         if (artist == null && title == null) {
             return url;
         } else if (artist == null) {
             return title;
         } else if (title == null) {
             return artist;
         }
         return String.format("%s - %s", artist, title);
     }
 
     public Bitmap getCoverArt()
     {
         if (pictureFront == null) {
             return null;
         }
         Bitmap bitmap = cache.get(pictureFront);
         if (bitmap == null) {
             return null;
         }
 
         return bitmap.copy(bitmap.getConfig(), false);
     }
 
    public String getCoverArtId()
    {
        return pictureFront;
    }

     public void registerMetadataListener(MetadataListener listener)
     {
         metadataListeners.add(listener);
     }
 
     public void unregisterMetadataListener(MetadataListener listener)
     {
         metadataListeners.remove(listener);
     }
 }
