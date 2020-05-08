 package com.music.fmv.core;
 
 import android.content.Context;
 import com.music.fmv.R;
 import com.music.fmv.models.PlayableSong;
 import com.nostra13.universalimageloader.core.DisplayImageOptions;
 import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
 
 /**
  * User: vitaliylebedinskiy
  * Date: 7/12/13
  * Time: 5:46 PM
  */
 public final class Core {
     public static final PlayableSong TEST_SONG = new PlayableSong();
    static {
        TEST_SONG.setUrl("http://f4.pleer.com/0357fcbeb86a579a7424c8ebcc07b43d90b2a2350a9ea4a4dd371ac6ec79d21f815ed5722175ea811323ce8837/ef58d44f50.mp3");
    }
 
     private static Core instance;
     private final Context app;
 
     private NotifyManager mNotificationManager;
     private CacheManager mCacheManager ;
     private SettingsManager mSettingsManager;
     private DisplayImageOptions notCachedOptions;
 
     public static Core getInstance(Context app) {
         if (instance == null){
             synchronized (Core.class){
                 if(instance == null){
                     instance = new Core(app);
                 }
             }
         }
         return instance;
     }
 
     private Core(Context app){
         if (app == null) throw new IllegalArgumentException("Context cannot be null");
         this.app = app;
         mNotificationManager = new NotifyManager(this);
         mCacheManager = new CacheManager(this);
         mSettingsManager = new SettingsManager(this);
     }
 
     public NotifyManager getNotificationManager() {
         return mNotificationManager;
     }
 
     public CacheManager getCacheManager() {
         return mCacheManager;
     }
 
     public SettingsManager getSettingsManager(){
         return mSettingsManager;
     }
 
     public DisplayImageOptions getNotCachedOptions(){
         if (notCachedOptions == null){
             notCachedOptions = new DisplayImageOptions.Builder()
                     .showStubImage(R.drawable.default_artist_medium)
                     .cacheInMemory(true)
                     .displayer(new FadeInBitmapDisplayer(200))
                     .showImageForEmptyUri(R.drawable.default_artist_medium)
                     .cacheOnDisc(false)
                     .build();
         }
         return notCachedOptions;
     }
 
     public Context getContext() {
         return app;
     }
 
     public void finish(){
 
     }
 }
