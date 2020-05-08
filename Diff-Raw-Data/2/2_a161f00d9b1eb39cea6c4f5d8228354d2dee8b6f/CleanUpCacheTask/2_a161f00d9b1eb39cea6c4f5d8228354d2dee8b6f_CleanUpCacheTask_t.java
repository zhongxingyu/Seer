 package de.raptor2101.GalDroid.WebGallery.Tasks;
 
 import java.io.File;
 import java.lang.ref.WeakReference;
 import java.util.List;
 
 import de.raptor2101.GalDroid.Config.GalDroidPreference;
 import de.raptor2101.GalDroid.WebGallery.ImageCache;
 import android.os.AsyncTask;
 
 public class CleanUpCacheTask extends AsyncTask<Void, Long, Void> {
   private WeakReference<CacheTaskListener> mListener;
   private List<String> mCachedObjects;
   private File mCacheDir;
   private long mCleanUpSize;
 
   public CleanUpCacheTask(ImageCache cache, CacheTaskListener listener) {
     mListener = new WeakReference<CacheTaskListener>(listener);
     mCachedObjects = GalDroidPreference.getCacheOjectsOrderedByAccessTime();
 
     mCacheDir = cache.getCacheDir();
     long maxCacheSize = cache.getMaxCacheSize();
     long cleanUpToSize = maxCacheSize - (maxCacheSize / 3);
     long currentCacheSize = GalDroidPreference.getCacheSpaceNeeded();
     mCleanUpSize = currentCacheSize - cleanUpToSize;
   }
 
   @Override
   protected void onPreExecute() {
     CacheTaskListener listener = mListener.get();
     if (listener != null) {
       listener.onCacheOperationStart(100);
     }
   }
 
   @Override
   protected Void doInBackground(Void... params) {
     long currentCleanUp = 0;
     if (mCleanUpSize > 0) {
      for (int i = 0; currentCleanUp < mCleanUpSize && i < mCachedObjects.size(); i++) {
         String hash = mCachedObjects.get(i);
         File cacheFile = new File(mCacheDir, hash);
         if (cacheFile.exists()) {
           currentCleanUp += cacheFile.length();
           cacheFile.delete();
         }
         GalDroidPreference.deleteCacheObject(hash);
 
         publishProgress(currentCleanUp);
       }
     }
     return null;
   }
 
   @Override
   protected void onProgressUpdate(Long... values) {
     CacheTaskListener listener = mListener.get();
     if (listener != null) {
       long calculatedValue = values[0] * 100 / mCleanUpSize;
       listener.onCacheOperationProgress((int) calculatedValue);
     }
   }
 
   @Override
   protected void onPostExecute(Void result) {
     CacheTaskListener listener = mListener.get();
     if (listener != null) {
       listener.onCacheOperationDone();
     }
   }
 
 }
