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
 import java.util.Arrays;
 import java.util.Comparator;
 
 public class MediaCache {
   private DownloadManager mDownloadManager;
   /// This keeps track of the song being downloaded. When this is set to NO_DOWNLOAD_IN_PROGRESS
   /// we can queue up another song. Otherwise, there is already a song being downloaded.
   private long mDownloadId;
   private Context mContext;
  /// Maximum number of songs to cache
  private static final long MAX_SONGS_CACHED = 100;
  /// We can queue up another download
  private static final long NO_DOWNLOAD_IN_PROGRESS = -1;
  /// Folder to store all of the local files
  private File mCacheDir;
  /// Folder to temporarily store files while downloading
  private File mTempDownloadDir;
  /// Used for calls to Log
  private static final String TAG = "MediaCache";
   /// Called when the download finishes. This calls our private method to actually do the work.
   BroadcastReceiver downloadCompleteReceiver = new BroadcastReceiver() {
     public void onReceive(Context context, Intent intent) {
       downloadComplete(intent);
     }
   };
 
   MediaCache (Context mCtxt) {
     mContext = mCtxt;
     mDownloadManager = (DownloadManager)mContext.getSystemService(Context.DOWNLOAD_SERVICE);
     mDownloadId = NO_DOWNLOAD_IN_PROGRESS; // Allow the system to cache another song initially
 
     // Setup the directory to store the cache on the external storage
     File externalMusicDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);
     mCacheDir = new File(externalMusicDir.getAbsolutePath() + "/ampachecache");
     if (mCacheDir.exists() == false) {
       Log.i(TAG, mCacheDir + " does not exist, creating directory.");
       mCacheDir.mkdirs();
     }
 
     // Setup the directory to store the temporary DownloadManager files
     File externalDownloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
     mTempDownloadDir = new File(externalDownloadDir.getAbsolutePath() + "/ampachetmp");
     if (mTempDownloadDir.exists() == false) {
       Log.i(TAG, mTempDownloadDir + " does not exist, creating directory.");
       mTempDownloadDir.mkdirs();
     }
 
     // When the Android download manager finishes a download
     mContext.registerReceiver(downloadCompleteReceiver,
                               new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
   }
 
   /** \brief Add a song to the local music cache.
    *  \param[in] songUid The unique ID as from Ampache.
    *  \param[in] songUrl The actual live URL to download the track from Ampache.
    */
   public void cacheSong(long songUid, String songUrl) throws Exception {
     // If the song is already cached, we are already done
     if (checkIfCached(songUid) == true) {
       return;
     }
 
     // Check to see if we already have a download running. Only cache one song at a time.
     if (mDownloadId != NO_DOWNLOAD_IN_PROGRESS) {
       Log.i(TAG, "cacheSong returning, there is already a download in progress.");
       return;
     }
 
     // If the song is not cached, then we want to cache it. First check if there is room
     if (checkIfCacheSpaceAvailable() == false) {
       Log.i(TAG, "checkIfCacheSpaceAvailable returned false. Clearing new space.");
       makeCacheSpace();
     }
 
     Log.i(TAG, "Attempting to cache song ID " + songUid);
     // Generate a new request to then add to the download manager queue.
     Request request = new Request(Uri.parse(songUrl));
     // We can keep track of the Ampache song ID in the download description
     request.setDescription(String.valueOf(songUid));
     // Set the title incase we want to view the downloads in the download manager for debugging
     request.setTitle("Amdroid caching song");
     // Normally, we don't want these downloads to appear in the UI or notifications
     request.setVisibleInDownloadsUi(false);
     // TODO: Buy a new phone that isn't stuck below API 11 :)
     //request.setNotificationVisibility(VISIBILITY_HIDDEN);
     // Set the destination to the external device in the downloads directory
     request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,
                                               "ampachetmp/" + songUid);
     // Queue up the request
     mDownloadId = mDownloadManager.enqueue(request);
     Log.i(TAG, "cacheSong queued download request mDownloadId=" + mDownloadId);
   }
 
   /** \brief Handle the song finished download.
    *
    */
   private void downloadComplete(Intent intent) {
     String action = intent.getAction();
     Log.i(TAG, "In downloadComplete method, action: " + action);
     // Check to see if the action corresponds to a completed download
     if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
       // Query for more info using the ID
       Query query = new Query();
       query.setFilterById(mDownloadId);
       Cursor cur = mDownloadManager.query(query);
 
       // Access the first row of data returned
       if (cur.moveToFirst()) {
         // Find the column which corresponds to the download status
         int statusIndex = cur.getColumnIndex(DownloadManager.COLUMN_STATUS);
         // If the download was successful try and move the file to our cache location
         if (DownloadManager.STATUS_SUCCESSFUL == cur.getInt(statusIndex)) {
           // Find the column which corresponds to the current file URI
           int uriIndex = cur.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI);
           // Retreive the temporary URI to where DownloadManager stored the file
           String downloadUri = cur.getString(uriIndex);
           File downloadFile = new File(Uri.parse(downloadUri).getPath());
 
           // Find the column which corresponds to the description we provided (Ampache song id)
           int descriptionIndex = cur.getColumnIndex(DownloadManager.COLUMN_DESCRIPTION);
           // Retreive the description
           long ampacheSongUid = cur.getLong(descriptionIndex);
 
           // Setup the destination file
           String destinationPath = cachedSongPath(ampacheSongUid);
           File destinationFile = new File(Uri.parse(destinationPath).getPath());
 
           // Move the file
           Log.i(TAG, "Moving " + downloadFile + " to " + destinationFile);
           if (downloadFile.renameTo(destinationFile)) {
             Log.i(TAG, destinationFile + " moved successfully");
           }
         }
       }
 
       // Also set the mDownloadId to indicate that no download is in progress
       mDownloadId = NO_DOWNLOAD_IN_PROGRESS;
     }
   }
 
   /** \brief Check to see if a song is already in the local music cache.
    *  \return Returns true if the song is already cached, false otherwise.
    *  \param[in] songUid The unique ID as from Ampache.
    */
   public boolean checkIfCached(long songUid) throws Exception {
     // Initially set to false. Will switch to true if we find the file.
     boolean cached = false;
     // Construct the path to check for the cached song
     File testFile = new File(cachedSongPath(songUid));
 
     Log.i(TAG, "Checking if " + testFile + " exists.");
     if (testFile.exists() == true) {
       cached = true;
       Log.i(TAG, testFile + " exists.");
     }
 
     return cached;
   }
 
   /** \brief This checks to see if the cache directory has room for another song.
    *  \return Returns a boolean indicating that the external storage does have cache
    *          space available.
    */
   private boolean checkIfCacheSpaceAvailable() {
     // Initially set to false. Will switch to true if we find available space.
     boolean spaceAvailable = false;
     // Collect the list of files currently in the cache directory
     String fileList[] = mCacheDir.list();
     Log.i(TAG, "checkIfCacheSpaceAvailable, current # of files cached: " + fileList.length);
 
     // If there is room left, return true
     if (fileList.length < MAX_SONGS_CACHED) {
       spaceAvailable = true;
     }
 
     return spaceAvailable;
   }
 
   /** \brief This deletes the oldest files from the cache directory until there is room for another
    *         song.
    */
   private void makeCacheSpace() {
     File cacheFiles[] = mCacheDir.listFiles();
 
     // Before doing anything else, return if there is room left.
     if (cacheFiles.length < MAX_SONGS_CACHED) {
       return;
     }
 
     // Sort the files by the data modified. Shamelessly borrowed from:
     // http://stackoverflow.com/questions/203030/best-way-to-list-files-in-java-sorted-by-date-modified
     Arrays.sort(cacheFiles, new Comparator<File>(){
       public int compare(File f1, File f2) {
         return Long.valueOf(f1.lastModified()).compareTo(f2.lastModified());
       }
     });
 
     // We want to remove the oldest files, which will be at the end of the array.
     for (int x = 0; x < cacheFiles.length - MAX_SONGS_CACHED; x++) {
       // For example, if the array is 30 long we want to initially delete slot 29.
       int y = cacheFiles.length - x - 1;
       Log.i(TAG, "makeCacheSpace, cacheFiles[" + y + "].lastModified=" + cacheFiles[y].lastModified());
       if (cacheFiles[y].delete()) {
         Log.i(TAG, "makeCacheSpace, successfully deleted " + cacheFiles[y].getAbsolutePath());
       } else {
         Log.i(TAG, "makeCacheSpace, failed to delete " + cacheFiles[y].getAbsolutePath());
       }
     }
   }
 
   /**
    * \return Returns a string with the path to the cached file or location
    *         where the file would be cached. In other words, this takes a
    *         song UID and converts that into a string for the file path.
    * \param[in] songUid the unique ID as from Ampache
    */
   public String cachedSongPath(long songUid) {
     String path = mCacheDir.getAbsolutePath() + "/" + songUid;
     return path;
   }
 }
 
