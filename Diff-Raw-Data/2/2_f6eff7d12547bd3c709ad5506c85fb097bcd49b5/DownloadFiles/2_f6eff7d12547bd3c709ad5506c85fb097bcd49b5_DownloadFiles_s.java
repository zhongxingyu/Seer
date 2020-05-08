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
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.AsyncTask;
 import android.os.Environment;
 import android.widget.Toast;
 import java.io.*;
 import java.net.URL;
 import java.net.URLConnection;
 
 public class DownloadFiles extends AsyncTask<String, Integer, Boolean>{
         
         private static Context mContext;
         private static String mFileName;
         private static ProgressDialog mProgressDialog;
         public static boolean mIsSuccess = false;
         public static int mFileLength;
         
         @Override
         protected Boolean doInBackground(String... sUrl) {
             int count;
             try {
                 URL url = new URL(sUrl[0]);
                 URLConnection connection = url.openConnection();
                 connection.connect();
                 mFileLength = connection.getContentLength();
                 InputStream input = new BufferedInputStream(url.openStream());
                 OutputStream output = new FileOutputStream(Environment.getExternalStorageDirectory() + File.separator + mFileName);
                 byte data[] = new byte[1024];
                 long total = 0;
                 while ((count = input.read(data)) != -1) {
                     total += count;
                     publishProgress((int)(total*100/mFileLength));
                     output.write(data, 0, count);
                 }
                 output.flush();
                 output.close();
                 input.close();
                 mIsSuccess = true;
                 return true;
             } catch (Exception e) {
                 e.printStackTrace();
                 mIsSuccess = false;
                 return false;
             }
         }
         
         @Override
         protected  void onProgressUpdate(Integer... args){
             mProgressDialog.setProgress(args[0]);
         }
         
         @Override
         protected void onPostExecute(Boolean result) {
             mProgressDialog.dismiss();
             if(mIsSuccess)
                 showRebootDialog();
             else
                 showWrongDownloadDialog();
         }
         
     public void requestDownload(String url, String filename, Context context){
         mProgressDialog = new ProgressDialog(context);
         mProgressDialog.setMessage(context.getString(R.string.downloading));
         mProgressDialog.setIndeterminate(false);
         mProgressDialog.setCancelable(false);
         mProgressDialog.setMax(100);
         mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
         mProgressDialog.show();
         mContext = context;
         mFileName = filename;
         this.execute(url);
     }
     
     public static void showRebootDialog(){
         AlertDialog.Builder rebootAlert = new AlertDialog.Builder(mContext);
         rebootAlert.setMessage(R.string.rom_downloaded)
         .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int id) {
                     AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                     builder.setMessage(mContext.getString(R.string.reboot_alert)+"\n"+Environment.getExternalStorageDirectory().getPath() + File.separator + mFileName)
                         .setCancelable(false)
                         .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                             public void onClick(DialogInterface dialog, int id) {
                                 RunCommands.execute(new String[]{
                                     "busybox echo 'install_zip(\"/sdcard/"+mFileName+"\");' > /cache/recovery/extendedcommand",
                                     "busybox echo 'install_zip(\"/emmc/"+mFileName+"\");' >> /cache/recovery/extendedcommand"}, mContext); 
                             }
                         });
                     AlertDialog alert = builder.create();
                     alert.show();
          }})
         .setNegativeButton(android.R.string.no, null);
         rebootAlert.show();
     }
 
     private static void showWrongDownloadDialog(){
         AlertDialog.Builder wrongDownload = new AlertDialog.Builder(mContext);
         wrongDownload.setMessage(R.string.wrong_download)
         .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int id) {
                 dialog.dismiss();
             }
         });
         wrongDownload.show();
     }
     
     public static boolean requestInternetConnection(Context context){
         ConnectivityManager conMgr =  (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
         NetworkInfo i = conMgr.getActiveNetworkInfo();
         if (i == null || !i.isConnected() || !i.isAvailable()){
            Toast.makeText(activity, R.string.no_internet, Toast.LENGTH_LONG).show();
             return false;
         }
         return true;
     }
 }
