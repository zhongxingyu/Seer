 package com.sound.ampache;
 
 import android.app.DownloadManager;
 import android.app.DownloadManager.Query;
 import android.app.DownloadManager.Request;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.app.Activity;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Environment;
 import android.view.View;
 import android.widget.ImageView;
 import android.util.Log;
 import java.io.File;
 
 public class MediaCache
 {
   private DownloadManager dm;
   private long enqueue;
   private Context mContext;
   private static final long maxCacheSize = 100*1024*1024; /// Maximum amount of data to cache
   private File cacheDir; /// Folder to store all of the local files
   private static final String TAG = "MediaCache"; /// Used for calls to Log
 
   MediaCache (Context mCtxt)
   {
     mContext = mCtxt;
     dm = (DownloadManager)mContext.getSystemService(Context.DOWNLOAD_SERVICE);
 
     // Setup the directory to store the cache on the external storage
     File externalMusicDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);
    cacheDir = new File(externalMusicDir.getAbsolutePath() + "ampacheCache");
     if (cacheDir.exists() == false)
     {
       Log.i(TAG, cacheDir + " does not exist, creating directory.");
       cacheDir.mkdirs();
     }
   }
 
   /** \brief Add a song to the local music cache.
    *  \param[in] songUid The unique ID as from Ampache.
    */
   public void cache_file(long songUid) throws Exception
   {
     // If the song is already cached, we are already done
     if (check_if_cached(songUid))
     {
       return;
     }
   }
 
   /** \brief Check to see if a song is already in the local music cache.
    *  \return Returns true if the song is already cached, false otherwise.
    *  \param[in] songUid The unique ID as from Ampache.
    */
   private boolean check_if_cached(long songUid) throws Exception
   {
     // Initially set to false. Will switch to true if we find the file.
     boolean cached = false;
 
     return cached;
   }
 }
 
