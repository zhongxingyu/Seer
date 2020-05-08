 package com.kii.demo.cloudstorage.api;
 
 import java.io.File;
 import java.util.List;
 
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 
 import com.kii.cloud.storage.KiiClient;
 import com.kii.cloud.storage.KiiFile;
 import com.kii.cloud.storage.callback.KiiFileCallBack;
 import com.kii.cloud.storage.callback.KiiFileProgress;
 import com.kii.demo.cloudstorage.activities.ShowInfo;
 import com.kii.demo.cloudstorage.file.TrashFilesListActivity;
 import com.kii.demo.cloudstorage.file.WorkingFilesListActivity;
 
 public class KiiFileOperation {
     private static final String FILE_CONTAINER = "apidemo";
 
     static {
        KiiClient.initialize(AppInfo.APP_ID, AppInfo.APP_KEY, AppInfo.BAST_URL);
     }
 
     public static List<KiiFile> mWorkingFiles;
     public static List<KiiFile> mTrashFiles;
 
     public static int asyncListWorkingFiles(
             final WorkingFilesListActivity activity) {
         int token = KiiFile.listWorkingFiles(new KiiFileCallBack() {
 
             @Override
             public void onListWorkingCompleted(int mToken, boolean success,
                     List<KiiFile> files, Exception exception) {
                 ShowInfo.closeProgressDialog();
                 if (success) {
                     mWorkingFiles = files;
                     activity.rebuildTitle(getWorkingFileTitles());
                 } else {
                     ShowInfo.showException(activity, exception);
                 }
             }
 
         }, FILE_CONTAINER);
 
         return token;
     }
 
     public static int asyncListTrashFiles(final TrashFilesListActivity activity) {
         int token = KiiFile.listTrashedFiles(new KiiFileCallBack() {
 
             @Override
             public void onListTrashCompleted(int mToken, boolean success,
                     List<KiiFile> files, Exception exception) {
                 ShowInfo.closeProgressDialog();
                 if (success) {
                     mTrashFiles = files;
                     activity.rebuildTitle(getTrashFileTitles());
                 } else {
                     ShowInfo.showException(activity, exception);
                 }
             }
 
         }, FILE_CONTAINER);
 
         return token;
     }
 
     public static int asyncDeleteWorkingFile(
             final WorkingFilesListActivity activity, final int position) {
         KiiFile file = mWorkingFiles.get(position);
         int token = file.delete(new KiiFileCallBack() {
 
             @Override
             public void onDeleteCompleted(int token, boolean success,
                     Exception exception) {
                 ShowInfo.closeProgressDialog();
                 if (success) {
                     mWorkingFiles.remove(position);
                     activity.rebuildTitle(getTrashFileTitles());
                 } else {
                     ShowInfo.showException(activity, exception);
                 }
 
             }
 
         });
 
         return token;
     }
 
     public static int asyncDeleteTrashFile(
             final TrashFilesListActivity activity, final int position) {
         KiiFile file = mTrashFiles.get(position);
         int token = file.delete(new KiiFileCallBack() {
 
             @Override
             public void onDeleteCompleted(int token, boolean success,
                     Exception exception) {
                 ShowInfo.closeProgressDialog();
                 if (success) {
                     mTrashFiles.remove(position);
                     activity.rebuildTitle(getWorkingFileTitles());
                 } else {
                     ShowInfo.showException(activity, exception);
                 }
 
             }
 
         });
 
         return token;
     }
 
     public static int asyncUploadFile(final WorkingFilesListActivity activity,
             String filename) {
         KiiFile f = new KiiFile(new File(filename), FILE_CONTAINER);
         int token = f.upload(new KiiFileCallBack() {
             @Override
             public void onUploadCompleted(int token, boolean success,
                     KiiFile file, Exception exception) {
                 ShowInfo.closeProgressDialog();
                 if (success) {
                     mWorkingFiles.add(0, file);
                     activity.rebuildTitle(getWorkingFileTitles());
                 } else {
                     ShowInfo.showException(activity, exception);
                 }
             }
 
             @Override
             public void onProgressUpdate(int token, KiiFileProgress progress) {
                 switch(progress.getStatus()){
                     case KiiFileProgress.STATUS_START_UPDATE_META:
                         ShowInfo.updateProgressText("Start to upload metadata...");
                         break;
                     case KiiFileProgress.STATUS_START_UPDATE_BODY:
                         ShowInfo.updateProgressText("Start to upload file body...");
                         break;
                     case KiiFileProgress.STATUS_UPDATING_BODY:
                         long totalsize = progress.getTotalSize();
                         long currentsize = progress.getCurrentSize();
                         String msg = "Uploading file body :" + currentsize +"/" + totalsize;
                         ShowInfo.updateProgressText(msg);
                         break;
                 }
             }
             
             
 
         });
 
         return token;
     }
 
     public static int asyncMoveToTrash(final WorkingFilesListActivity activity,
             final int position) {
         KiiFile f = mWorkingFiles.get(position);
         int token = f.moveToTrash(new KiiFileCallBack() {
 
             @Override
             public void onMoveTrashCompleted(int token, boolean success,
                     KiiFile file, Exception exception) {
                 ShowInfo.closeProgressDialog();
                 if (success) {
                     mWorkingFiles.remove(position);
                     activity.rebuildTitle(getWorkingFileTitles());
                     mTrashFiles.add(0, file);
                 } else {
                     ShowInfo.showException(activity, exception);
                 }
             }
 
         });
 
         return token;
     }
 
     public static int asyncRestoreFile(final TrashFilesListActivity activity,
             final int position) {
         KiiFile f = mTrashFiles.get(position);
         int token = f.restoreFromTrash(new KiiFileCallBack() {
 
             @Override
             public void onRestoreTrashCompleted(int token, boolean success,
                     KiiFile file, Exception exception) {
                 ShowInfo.closeProgressDialog();
                 if (success) {
                     mTrashFiles.remove(position);
                     activity.rebuildTitle(getTrashFileTitles());
                     mWorkingFiles.add(0, file);
                 } else {
                     ShowInfo.showException(activity, exception);
                 }
             }
 
         });
 
         return token;
     }
 
     public static int asyncDownloadFile(
             final WorkingFilesListActivity activity, final int position,
             final String filename) {
         KiiFile f = mWorkingFiles.get(position);
         int token = f.downloadFileBody(new KiiFileCallBack() {
 
             @Override
             public void onDownloadBodyCompleted(int token, boolean success,
                     Exception exception) {
                 ShowInfo.closeProgressDialog();
                 if (success) {
 
                     AlertDialog dialog = new AlertDialog.Builder(activity)
                             .setTitle(
                                     "The file have been downloaded successful!")
                             .setPositiveButton("OK",
                                     new DialogInterface.OnClickListener() {
                                         public void onClick(
                                                 DialogInterface dialog,
                                                 int whichButton) {
 
                                             /* User clicked OK so do some stuff */
                                         }
                                     }).create();
 
                     dialog.show();
                 } else {
                     ShowInfo.showException(activity, exception);
                 }
             }
             
             @Override
             public void onProgressUpdate(int token, KiiFileProgress progress) {
                 switch(progress.getStatus()){
                     case KiiFileProgress.STATUS_START_DOWNLOAD_BODY:
                         ShowInfo.updateProgressText("Start to download file...");
                         break;
                     case KiiFileProgress.STATUS_DOWNLOADING_BODY:
                         long totalsize = progress.getTotalSize();
                         long currentsize = progress.getCurrentSize();
                         String msg = "Downloading file body :" + currentsize +"/" + totalsize;
                         ShowInfo.updateProgressText(msg);
                         break;
                 }
             }
 
         }, filename);
 
         return token;
     }
 
     public static int asyncUpdateFileCustomField(
             final WorkingFilesListActivity activity, final int position,
             final String custom) {
         KiiFile f = mWorkingFiles.get(position);
         f.setCustomeField(custom);
         int token = f.updateMetaData(new KiiFileCallBack() {
 
             @Override
             public void onUpdateCompleted(int token, boolean success,
                     KiiFile file, Exception exception) {
                 ShowInfo.closeProgressDialog();
                 if (success) {
                     mWorkingFiles.set(position, file);
                     activity.rebuildTitle(getWorkingFileTitles());
                 } else {
                     ShowInfo.showException(activity, exception);
                 }
             }
 
         });
         return token;
     }
 
     public static int asyncUpdateFile(final WorkingFilesListActivity activity,
             final int position, String filename) {
         KiiFile f = mWorkingFiles.get(position);
         File update = new File(filename);
         int token = f.update(new KiiFileCallBack(){
             @Override
             public void onUpdateCompleted(int token, boolean success,
                     KiiFile file, Exception exception) {
                 ShowInfo.closeProgressDialog();
                 if (success) {
                     mWorkingFiles.set(position, file);
                     activity.rebuildTitle(getWorkingFileTitles());
                 } else {
                     ShowInfo.showException(activity, exception);
                 }
             }
             
             @Override
             public void onProgressUpdate(int token, KiiFileProgress progress) {
                 switch(progress.getStatus()){
                     case KiiFileProgress.STATUS_START_UPDATE_META:
                         ShowInfo.updateProgressText("Start to update metadata...");
                         break;
                     case KiiFileProgress.STATUS_START_UPDATE_BODY:
                         ShowInfo.updateProgressText("Start to update file body...");
                         break;
                     case KiiFileProgress.STATUS_UPDATING_BODY:
                         long totalsize = progress.getTotalSize();
                         long currentsize = progress.getCurrentSize();
                         String msg = "Updating file body :" + currentsize +"/" + totalsize;
                         ShowInfo.updateProgressText(msg);
                         break;
                 }
             }
         }, update);
         return token;
     }
 
     public static String[] getWorkingFileTitles() {
         if (mWorkingFiles == null) {
             return null;
         }
         int length = mWorkingFiles.size();
         String[] res = new String[length];
         for (int i = 0; i < length; i++) {
             res[i] = mWorkingFiles.get(i).getTitle();
         }
 
         return res;
     }
 
     public static String[] getTrashFileTitles() {
         if (mTrashFiles == null) {
             return null;
         }
         int length = mTrashFiles.size();
         String[] res = new String[length];
         for (int i = 0; i < length; i++) {
             res[i] = mTrashFiles.get(i).getTitle();
         }
 
         return res;
     }
 
 }
