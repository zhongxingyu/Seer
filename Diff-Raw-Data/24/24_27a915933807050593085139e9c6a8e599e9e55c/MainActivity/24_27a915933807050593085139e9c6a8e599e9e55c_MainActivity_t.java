 /*
  * Copyright (C) 2012 ParanoidAndroid Project
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
 
 package com.paranoid.preferences;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.res.Resources;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.preference.PreferenceManager;
import android.util.Log;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.widget.Toast;
 import static com.paranoid.preferences.scheduler.BootReceiver.UPDATE_INTENT;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
 
 public class MainActivity extends Activity{
     
    protected static final int MENU_CHECK = Menu.FIRST;
    protected static final int MENU_SETTINGS = Menu.FIRST + 1;
    protected WebFields.OtaVersion mOtaVersion;
    protected WebFields.RomMirrors mMirrors;
    protected ProgressDialog mLoadingProgress;
    protected boolean mIsNotification;
    protected boolean mServerTimeout;
    protected long mStart;
    protected Menu mOptionsMenu;
    protected Context mContext;
     
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.main);
        
        Bundle extras = getIntent().getExtras();
        mIsNotification = extras != null ? extras.getBoolean("update") : false;
        checkForUpdates();
    }
     
    private void checkForUpdates(){
        mServerTimeout = false;
        mLoadingProgress = ProgressDialog.show(mContext, null, getString(R.string.loading_info), false, false);
        final boolean mIsConnected = DownloadFiles.requestInternetConnection(mContext, true);
        new Thread(new Runnable(){
            public void run(){
                if(mIsConnected){
                    mOtaVersion = new WebFields.OtaVersion();
                    mMirrors = new WebFields.RomMirrors();
                    mStart = System.currentTimeMillis();
                    while(mOtaVersion.version == 0){
                        mOtaVersion.getWebVersion();
                        if(System.currentTimeMillis() - mStart > 15000){
                            mToastHandler.sendEmptyMessage(1);
                            mServerTimeout = true;
                        }
                        if(mOtaVersion.version != 0 || mServerTimeout)
                            break;
                    }
                    mStart = System.currentTimeMillis();
                    while(mMirrors.mirrors.isEmpty()){
                        mMirrors.getMirrorList();
                        if(System.currentTimeMillis() - mStart > 15000){
                            mToastHandler.sendEmptyMessage(1);
                            mServerTimeout = true;
                        }
                        if(!mMirrors.mirrors.isEmpty() || mServerTimeout)
                            break;
                    }
                    mLoadingProgress.dismiss();
                    if(!mServerTimeout){
                         if(mOtaVersion.version > Utils.getRomVersion()){
                            mDialogHandler.sendEmptyMessage(0);
                         }
                         else{
                             mToastHandler.sendEmptyMessage(0);
                         }
                    }
                } else
                    mLoadingProgress.dismiss();
            }
        }).start();
    }
    
    private CharSequence[] mMirrorNames(){
        String[] mTemp = new String[mMirrors.mirrors.size()];
        for(int i=0; i<mTemp.length; i++){
            if(mMirrors.mirrors.get(i)[1].equals("self_server")){
                mMirrors.mirrors.set(i, new String[]{getString(R.string.default_mirror), FetchOnlineData.HTTP_HEADER+FetchOnlineData.mDevice+"paranoid"+mOtaVersion.version+".zip"});
            }
            mTemp[i] = mMirrors.mirrors.get(i)[0];
        }
        return mTemp;
    }
 
    private Handler mToastHandler = new Handler(){
         @Override
         public void handleMessage(Message msg) {
             switch(msg.what){
                 case 0:
                     Toast.makeText(mContext, R.string.no_update_found, Toast.LENGTH_LONG).show();
                     break;
                 case 1:
                     Toast.makeText(mContext, R.string.timeout, Toast.LENGTH_LONG).show();
                     break;
             }
         }
     };
    
    private Handler mDialogHandler = new Handler(){
         @Override
         public void handleMessage(Message msg) {
             if(!mIsNotification){
                 AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                 builder.setMessage(getString(R.string.update_found_rom)+" (v"+mOtaVersion.version+")")
                     .setCancelable(false)
                     .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                         public void onClick(DialogInterface dialog, int id){
                             getMirrorsDialog();
                         }
                     })
                     .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                         public void onClick(DialogInterface dialog, int id) {
                             dialog.dismiss();
                         }
                     });
                 AlertDialog alert = builder.create();
                 alert.show();
             } else {
                 getMirrorsDialog();
                 mIsNotification = false;
             }
         }
    };
    
    public void getMirrorsDialog(){
        final CharSequence[] items = mMirrorNames();
        AlertDialog.Builder serverBuilder = new AlertDialog.Builder(mContext);
        serverBuilder.setCancelable(false);
        serverBuilder.setTitle(getString(R.string.select_mirror));
        serverBuilder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                new DownloadFiles().requestDownload(mMirrors.mirrors.get(item)[1], "paranoid"+mOtaVersion.version+".zip", mContext);
            }
        })
        .setNegativeButton(android.R.string.cancel, null);
        AlertDialog alert = serverBuilder.create();
        alert.show();
    }
    
    public void showIntervalDialog(){
        Resources r = getResources();
        final CharSequence[] items = r.getStringArray(R.array.interval_entries);
        final int[] values = r.getIntArray(R.array.interval_values);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.auto_check_interval_dialog);
        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt("interval", values[item]);
                editor.commit();
                Intent i = new Intent();
                i.setAction(UPDATE_INTENT);
                mContext.sendBroadcast(i);
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }
    
    public void showStorageDialog(){
        Resources r = getResources();
        final CharSequence[] items = r.getStringArray(R.array.storage_array);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.storage_dialog);
        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
               String storage = String.valueOf(items[item]);
               File f = new File(storage + "test_file");
               try {
                   f.createNewFile();
               } catch (IOException ex) {
                   ex.printStackTrace();
               }
               if(f.exists()){
                   f.delete();
                   SharedPreferences.Editor editor = sharedPreferences.edit();
                   editor.putString("storage", storage);
                   editor.commit();
               } else
                   Toast.makeText(mContext, getString(R.string.storage_invalid), Toast.LENGTH_LONG).show();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case MENU_CHECK:
             	checkForUpdates();
                 return true;
             case MENU_SETTINGS:
                 final CharSequence[] items = new CharSequence[]{getString(R.string.storage), getString(R.string.auto_check)};
                 AlertDialog.Builder builder = new AlertDialog.Builder(this);
                 builder.setTitle(R.string.settings);
                 builder.setItems(items, new DialogInterface.OnClickListener() {
                     public void onClick(DialogInterface dialog, int item) {
                         switch(item){
                             case 0:
                                 showStorageDialog();
                                 break;        
                             case 1:
                                 showIntervalDialog();
                                 break;
                         }
                     }
                 });
                 AlertDialog alert = builder.create();
                 alert.show();
                 return true;
         }
         return super.onOptionsItemSelected(item);
     }
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
     	 mOptionsMenu = menu;
          menu.add(Menu.NONE, MENU_CHECK, 0, R.string.auto_check)
                  .setIcon(R.drawable.ic_menu_autocheck)
                  .setEnabled(true)
                  .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
          menu.add(Menu.NONE, MENU_SETTINGS, 0, R.string.settings)
 		 .setIcon(R.drawable.ic_menu_settings)
                  .setEnabled(true)
                  .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
  	 return super.onCreateOptionsMenu(menu);
     }
     
     @Override
     public boolean onKeyDown(int keyCode, KeyEvent event){
         if ((keyCode == KeyEvent.KEYCODE_BACK)){
             android.os.Process.killProcess(android.os.Process.myPid());
         }
         return super.onKeyDown(keyCode, event);
     }
 }
