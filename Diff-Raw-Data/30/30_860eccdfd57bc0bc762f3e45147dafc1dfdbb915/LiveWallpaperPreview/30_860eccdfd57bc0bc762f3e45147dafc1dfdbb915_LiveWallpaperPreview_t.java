 /*
  * Copyright (C) 2009 The Android Open Source Project
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.android.wallpaper.livepicker;
 
 import android.app.Activity;
 import android.app.WallpaperManager;
 import android.app.WallpaperInfo;
 import android.service.wallpaper.IWallpaperConnection;
 import android.service.wallpaper.IWallpaperService;
 import android.service.wallpaper.IWallpaperEngine;
 import android.service.wallpaper.WallpaperSettingsActivity;
 import android.content.ServiceConnection;
 import android.content.Intent;
 import android.content.Context;
 import android.content.ComponentName;
 import android.os.RemoteException;
 import android.os.IBinder;
 import android.os.ParcelFileDescriptor;
 import android.os.Bundle;
 import android.view.View;
 import android.view.WindowManager;
 import android.util.Log;
 
 public class LiveWallpaperPreview extends Activity {
     static final String EXTRA_LIVE_WALLPAPER_INTENT = "android.live_wallpaper.intent";
     static final String EXTRA_LIVE_WALLPAPER_SETTINGS = "android.live_wallpaper.settings";
     static final String EXTRA_LIVE_WALLPAPER_PACKAGE = "android.live_wallpaper.package";
 
     private static final String LOG_TAG = "LiveWallpaperPreview";
 
     private WallpaperManager mWallpaperManager;
     private WallpaperConnection mWallpaperConnection;
 
     private String mSettings;
     private String mPackageName;
     private Intent mWallpaperIntent;
    private View mView;
 
     static void showPreview(Activity activity, int code, Intent intent, WallpaperInfo info) {
         Intent preview = new Intent(activity, LiveWallpaperPreview.class);
         preview.putExtra(EXTRA_LIVE_WALLPAPER_INTENT, intent);
         preview.putExtra(EXTRA_LIVE_WALLPAPER_SETTINGS, info.getSettingsActivity());
         preview.putExtra(EXTRA_LIVE_WALLPAPER_PACKAGE, info.getPackageName());
         activity.startActivityForResult(preview, code);
     }
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         Bundle extras = getIntent().getExtras();
         mWallpaperIntent = (Intent) extras.get(EXTRA_LIVE_WALLPAPER_INTENT);
         if (mWallpaperIntent == null) {
             setResult(RESULT_CANCELED);
             finish();
         }
 
         setContentView(R.layout.live_wallpaper_preview);
        mView = findViewById(R.id.configure);
 
         mSettings = extras.getString(EXTRA_LIVE_WALLPAPER_SETTINGS);
         mPackageName = extras.getString(EXTRA_LIVE_WALLPAPER_PACKAGE);
         if (mSettings == null) {
            mView.setVisibility(View.GONE);
         }
 
         mWallpaperManager = WallpaperManager.getInstance(this);
 
        mWallpaperConnection = new WallpaperConnection(mWallpaperIntent);
     }
 
     public void setLiveWallpaper(View v) {
         try {
             mWallpaperManager.getIWallpaperManager().setWallpaperComponent(
                     mWallpaperIntent.getComponent());
             mWallpaperManager.setWallpaperOffsets(v.getRootView().getWindowToken(), 0.5f, 0.0f);
             setResult(RESULT_OK);
         } catch (RemoteException e) {
             // do nothing
         } catch (RuntimeException e) {
             Log.w(LOG_TAG, "Failure setting wallpaper", e);
         }
         finish();
     }
 
     @SuppressWarnings({"UnusedDeclaration"})
     public void configureLiveWallpaper(View v) {
         Intent intent = new Intent();
         intent.setComponent(new ComponentName(mPackageName, mSettings));
         intent.putExtra(WallpaperSettingsActivity.EXTRA_PREVIEW_MODE, true);
         startActivity(intent);
     }
 
     @Override
     public void onResume() {
         super.onResume();
         if (mWallpaperConnection != null && mWallpaperConnection.mEngine != null) {
             try {
                 mWallpaperConnection.mEngine.setVisibility(true);
             } catch (RemoteException e) {
                 // Ignore
             }
         }
     }
     
     @Override
     public void onPause() {
         super.onPause();
         if (mWallpaperConnection != null && mWallpaperConnection.mEngine != null) {
             try {
                 mWallpaperConnection.mEngine.setVisibility(false);
             } catch (RemoteException e) {
                 // Ignore
             }
         }
     }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!mWallpaperConnection.connect()) {
            mWallpaperConnection = null;
        }
    }

     @Override
     public void onDetachedFromWindow() {
         super.onDetachedFromWindow();
         if (mWallpaperConnection != null) {
             mWallpaperConnection.disconnect();
         }
         mWallpaperConnection = null;
     }
 
     class WallpaperConnection extends IWallpaperConnection.Stub implements ServiceConnection {
         final Intent mIntent;
         IWallpaperService mService;
         IWallpaperEngine mEngine;
         boolean mConnected;
 
         WallpaperConnection(Intent intent) {
             mIntent = intent;
         }
 
         public boolean connect() {
             synchronized (this) {
                 if (!bindService(mIntent, this, Context.BIND_AUTO_CREATE)) {
                     return false;
                 }
                 
                 mConnected = true;
                 return true;
             }
         }
         
         public void disconnect() {
             synchronized (this) {
                 mConnected = false;
                 if (mEngine != null) {
                     try {
                         mEngine.destroy();
                     } catch (RemoteException e) {
                         // Ignore
                     }
                     mEngine = null;
                 }
                 unbindService(this);
                 mService = null;
             }
         }
         
         public void onServiceConnected(ComponentName name, IBinder service) {
             if (mWallpaperConnection == this) {
                 mService = IWallpaperService.Stub.asInterface(service);
                 try {
                    final View view = mView;
                    final View root = view.getRootView();
                     mService.attach(this, view.getWindowToken(),
                             WindowManager.LayoutParams.TYPE_APPLICATION_MEDIA,
                            true, root.getWidth(), view.getHeight());
                 } catch (RemoteException e) {
                     Log.w(LOG_TAG, "Failed attaching wallpaper; clearing", e);
                 }
             }
         }
 
         public void onServiceDisconnected(ComponentName name) {
             mService = null;
             mEngine = null;
             if (mWallpaperConnection == this) {
                 Log.w(LOG_TAG, "Wallpaper service gone: " + name);
             }
         }
         
         public void attachEngine(IWallpaperEngine engine) {
             synchronized (this) {
                 if (mConnected) {
                     mEngine = engine;
                     try {
                         engine.setVisibility(true);
                     } catch (RemoteException e) {
                         // Ignore
                     }
                 } else {
                     try {
                         engine.destroy();
                     } catch (RemoteException e) {
                         // Ignore
                     }
                 }
             }
         }
         
         public ParcelFileDescriptor setWallpaper(String name) {
             return null;
         }
     }
 }
