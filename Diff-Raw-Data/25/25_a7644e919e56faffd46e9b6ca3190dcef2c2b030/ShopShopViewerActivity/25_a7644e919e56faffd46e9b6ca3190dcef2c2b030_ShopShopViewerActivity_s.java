 /**
  * ShopShopViewer
  * Copyright (C) 2011 Remko Plantenga
  * 
  * This file is part of ShopShopViewer.
  *
  * ShopShopViewer is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * ShopShopViewer is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with ShopShopViewer. If not, see <http://www.gnu.org/licenses/>.
  */
 package de.remk0.shopshopviewer;
 
 import java.io.File;
 import java.io.FilenameFilter;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.ListActivity;
 import android.app.ProgressDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 
 import com.dropbox.client2.DropboxAPI;
 import com.dropbox.client2.android.AndroidAuthSession;
 
 import de.remk0.shopshopviewer.ShopShopViewerApplication.AppState;
 import de.remk0.shopshopviewer.io.DropboxFileAccess;
 import de.remk0.shopshopviewer.io.ExternalFilesDirFileAccess;
 import de.remk0.shopshopviewer.task.SynchronizeTask;
 
 /**
  * The main activity that shows a list of files and allows the user to
  * synchronize with Dropbox.
  * 
  * @author Remko Plantenga
  * 
  */
 public class ShopShopViewerActivity extends ListActivity {
     private static final int DIALOG_DROPBOX_FAILED = 1;
     private static final int DIALOG_STORAGE_ERROR = 2;
     private static final int DIALOG_PROGRESS_SYNC = 3;
     private static final int REQUEST_CODE = 1;
     private static final String REVISIONS_STORE = "REV_STORE";
 
     private ShopShopViewerApplication application;
     private DropboxAPI<AndroidAuthSession> mDBApi;
     private ArrayAdapter<String> listAdapter;
     private ProgressDialog progressDialog;
     private String hash = null;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         application = (ShopShopViewerApplication) getApplicationContext();
         application.setAppState(AppState.STARTED);
 
         if (!application.isExternalStorageAvailable()) {
             showDialog(DIALOG_STORAGE_ERROR);
         } else {
             getFiles();
         }
     }
 
     private void getFiles() {
         File appFolder = getExternalFilesDir(null);
         String[] files = appFolder.list(new FilenameFilter() {
 
             @Override
             public boolean accept(File dir, String filename) {
                 if (filename
                         .endsWith(ShopShopViewerApplication.SHOPSHOP_EXTENSION)) {
                     return true;
                 }
                 return false;
             }
         });
 
         listAdapter = new ArrayAdapter<String>(this, R.layout.filelist, files) {
             @Override
             public String getItem(int position) {
                 return super.getItem(position).split("\\.")[0];
             }
         };
         setListAdapter(listAdapter);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.listview_menu, menu);
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
         case R.id.synchronize:
             startDropboxSynchronize();
         default:
             return super.onOptionsItemSelected(item);
         }
     }
 
     private void startDropboxSynchronize() {
         application.setAppState(AppState.SYNCHRONIZE);
         startActivityForResult(new Intent(this, DropboxAuthActivity.class),
                 REQUEST_CODE);
     }
 
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         super.onActivityResult(requestCode, resultCode, data);
 
         if (requestCode == REQUEST_CODE) {
             if (resultCode == RESULT_OK) {
                 mDBApi = application.getDropboxAPI();
             } else {
                 showDialog(DIALOG_DROPBOX_FAILED);
             }
         }
     }
 
     @Override
     protected void onResume() {
         super.onResume();
 
         if (mDBApi != null
                 && application.getAppState() == AppState.AUTH_SUCCESS) {
             showDialog(DIALOG_PROGRESS_SYNC);
             new MyDropboxSynchronizeTask().execute();
         }
 
         application.setAppState(AppState.WAITING);
     }
 
     private class MyDropboxSynchronizeTask extends SynchronizeTask {
 
         @Override
         protected void onPreExecute() {
             setRemoteFileAccess(new DropboxFileAccess(mDBApi));
             SharedPreferences prefs = getSharedPreferences(REVISIONS_STORE,
                     MODE_PRIVATE);
             setRevisionsStore(prefs.getAll());
             setRevisionsEditor(prefs.edit());
             setFileAccess(new ExternalFilesDirFileAccess(
                     ShopShopViewerActivity.this));
             setHash(hash);
         }
 
         @Override
         protected void onProgressUpdate(Integer... values) {
             float count = values[0];
             float total = values[1];
             Log.d(ShopShopViewerApplication.APP_NAME, "Progress updated to "
                     + count + " " + total + " " + +(count / total * 100f));
             progressDialog.setProgress((int) (count / total * 100));
         }
 
         @Override
         protected void onPostExecute(Boolean result) {
             dismissDialog(DIALOG_PROGRESS_SYNC);
             if (result) {
                 hash = getHash();
                 getFiles();
             }
         }
 
     }
 
     @Override
     protected void onListItemClick(ListView l, View v, int position, long id) {
         super.onListItemClick(l, v, position, id);
 
         String e = (String) this.getListAdapter().getItem(position);
         Intent intent = new Intent(this, DisplayFileActivity.class);
        intent.putExtra(this.getPackageName() + ".fileName", e);
         startActivity(intent);
     }
 
     @Override
     protected Dialog onCreateDialog(int id) {
         AlertDialog.Builder builder = new AlertDialog.Builder(this);
         switch (id) {
         case DIALOG_DROPBOX_FAILED:
             builder.setMessage(
                     "Connection to Dropbox failed, please check your internet connection.")
                     .setCancelable(false)
                     .setPositiveButton("OK",
                             new DialogInterface.OnClickListener() {
 
                                 @Override
                                 public void onClick(DialogInterface dialog,
                                         int which) {
 
                                 }
                             });
             return builder.create();
         case DIALOG_STORAGE_ERROR:
             builder.setMessage("External storage is not available.")
                     .setCancelable(false)
                     .setPositiveButton("OK",
                             new DialogInterface.OnClickListener() {
 
                                 @Override
                                 public void onClick(DialogInterface dialog,
                                         int which) {
                                     finish();
                                 }
                             });
             return builder.create();
         case DIALOG_PROGRESS_SYNC:
             progressDialog = new ProgressDialog(ShopShopViewerActivity.this);
             progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
             progressDialog.setMessage("Synchronizing...");
             return progressDialog;
         default:
             return null;
         }
     }
 
     @Override
     protected void onPrepareDialog(int id, Dialog dialog) {
         switch (id) {
         case DIALOG_PROGRESS_SYNC:
             progressDialog.setProgress(0);
         }
     }
 
 }
