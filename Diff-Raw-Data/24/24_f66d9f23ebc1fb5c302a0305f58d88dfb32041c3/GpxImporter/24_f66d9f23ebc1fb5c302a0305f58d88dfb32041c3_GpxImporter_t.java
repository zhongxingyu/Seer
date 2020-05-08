 /*
  ** Licensed under the Apache License, Version 2.0 (the "License");
  ** you may not use this file except in compliance with the License.
  ** You may obtain a copy of the License at
  **
  **     http://www.apache.org/licenses/LICENSE-2.0
  **
  ** Unless required by applicable law or agreed to in writing, software
  ** distributed under the License is distributed on an "AS IS" BASIS,
  ** WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ** See the License for the specific language governing permissions and
  ** limitations under the License.
  */
 
 package com.google.code.geobeagle.io;
 
 import com.google.code.geobeagle.R;
 import com.google.code.geobeagle.io.Database.SQLiteWrapper;
 import com.google.code.geobeagle.ui.CacheListDelegate;
 import com.google.code.geobeagle.ui.ErrorDisplayer;
 
import org.xmlpull.v1.XmlPullParserException;

 import android.app.ListActivity;
 import android.app.ProgressDialog;
 import android.os.Handler;
 import android.os.Message;
 import android.widget.Toast;
 
 import java.io.FileNotFoundException;
import java.io.IOException;
 
 public class GpxImporter {
 
     // Too hard to test this class due to final methods in base.
     public static class ImportThread extends Thread {
 
         static ImportThread create(CacheListDelegate cacheListDelegate,
                 ProgressDialogWrapper progressDialog, GpxLoader gpxLoader,
                 ErrorDisplayer errorDisplayer) {
             final MessageHandler messageHandler = new MessageHandler(cacheListDelegate,
                     progressDialog);
             return new ImportThread(messageHandler, gpxLoader, errorDisplayer);
         }
 
         private final ErrorDisplayer mErrorDisplayer;
         private final GpxLoader mGpxLoader;
         private final MessageHandler mMessageHandler;
 
         public ImportThread(MessageHandler messageHandler, GpxLoader gpxLoader,
                 ErrorDisplayer errorDisplayer) {
             mMessageHandler = messageHandler;
             mGpxLoader = gpxLoader;
             mErrorDisplayer = errorDisplayer;
         }
 
         public void run() {
             try {
                 mGpxLoader.load(mMessageHandler);
                 mMessageHandler.sendEmptyMessage(MessageHandler.MSG_DONE);
             } catch (Exception e) {
                 mErrorDisplayer.displayErrorAndStack(e);
             }
         }
     }
 
     public static class ImportThreadWrapper {
         private Thread mImportThread;
 
         public boolean isAlive() {
             if (mImportThread != null)
                 return mImportThread.isAlive();
             return false;
         }
 
         public void join() throws InterruptedException {
             if (mImportThread != null)
                 mImportThread.join();
         }
 
         public void open(CacheListDelegate cacheListDelegate, ProgressDialogWrapper progressDialog,
                 GpxLoader gpxLoader, ErrorDisplayer mErrorDisplayer) {
             mImportThread = ImportThread.create(cacheListDelegate, progressDialog, gpxLoader,
                     mErrorDisplayer);
         }
 
         public void start() {
             if (mImportThread != null)
                 mImportThread.start();
         }
     }
 
     // Too hard to test this class due to final methods in base.
     public static class MessageHandler extends Handler {
         static final int MSG_DONE = 1;
         static final int MSG_PROGRESS = 0;
         private final CacheListDelegate mCacheListDelegate;
         private final ProgressDialogWrapper mProgressDialog;
         private String mStatus;
 
         public MessageHandler(CacheListDelegate cacheListDelegate,
                 ProgressDialogWrapper progressDialog) {
             mCacheListDelegate = cacheListDelegate;
             mProgressDialog = progressDialog;
         }
 
         public void handleMessage(Message msg) {
             switch (msg.what) {
                 case MessageHandler.MSG_PROGRESS:
                     mProgressDialog.setMessage(mStatus);
                     break;
                 case MessageHandler.MSG_DONE:
                     mProgressDialog.dismiss();
                     mCacheListDelegate.onResume();
                     break;
                 default:
                     break;
             }
         }
 
         public void update(String status) {
             mStatus = status;
             sendEmptyMessage(MessageHandler.MSG_PROGRESS);
         }
     }
 
     public static class ProgressDialogWrapper {
         private ProgressDialog mProgressDialog;
 
         public void dismiss() {
             if (mProgressDialog != null)
                 mProgressDialog.dismiss();
         }
 
         public void setMessage(CharSequence message) {
             mProgressDialog.setMessage(message);
         }
 
         public void show(ListActivity listActivity, String title, String msg) {
             mProgressDialog = ProgressDialog.show(listActivity, title, msg);
         }
     }
 
     public static GpxImporter create(Database database, ErrorDisplayer errorDisplayer,
             ListActivity listActivity) {
         final SQLiteWrapper sqliteWrapper = new SQLiteWrapper();
         final ProgressDialogWrapper progressDialogWrapper = new ProgressDialogWrapper();
         final GpxLoader gpxLoader = GpxLoader.create(errorDisplayer, database, sqliteWrapper);
         final ImportThreadWrapper importThreadWrapper = new ImportThreadWrapper();
         return new GpxImporter(gpxLoader, database, errorDisplayer, listActivity,
                 progressDialogWrapper, sqliteWrapper, importThreadWrapper);
     }
 
     private final Database mDatabase;
     private final ErrorDisplayer mErrorDisplayer;
     private final GpxLoader mGpxLoader;
     private final ImportThreadWrapper mImportThreadWrapper;
     private final ListActivity mListActivity;
     private final ProgressDialogWrapper mProgressDialog;
     private final SQLiteWrapper mSqliteWrapper;
 
     public GpxImporter(GpxLoader gpxLoader, Database database, ErrorDisplayer errorDisplayer,
             ListActivity listActivity, ProgressDialogWrapper progressDialog,
             SQLiteWrapper sqliteWrapper, ImportThreadWrapper importThreadWrapper) {
         mDatabase = database;
         mErrorDisplayer = errorDisplayer;
         mListActivity = listActivity;
         mProgressDialog = progressDialog;
         mGpxLoader = gpxLoader;
         mSqliteWrapper = sqliteWrapper;
         mImportThreadWrapper = importThreadWrapper;
     }
 
     public void abort() throws InterruptedException {
         mProgressDialog.dismiss();
         mGpxLoader.abortLoad();
         if (mImportThreadWrapper.isAlive()) {
             mImportThreadWrapper.join();
             mSqliteWrapper.close();
             Toast mToast = Toast.makeText(mListActivity, R.string.import_canceled,
                     Toast.LENGTH_SHORT);
             mToast.show();
         }
     }
 
    public void importGpxs(CacheListDelegate cacheListDelegate) {
         try {
             mSqliteWrapper.openReadableDatabase(mDatabase);
             mGpxLoader.open();
             mProgressDialog.show(mListActivity, "Importing Caches", "Please wait...");
             mImportThreadWrapper.open(cacheListDelegate, mProgressDialog, mGpxLoader,
                     mErrorDisplayer);
             mImportThreadWrapper.start();
         } catch (final FileNotFoundException e) {
             mErrorDisplayer.displayError("Unable to open file '" + e.getMessage()
                     + "'.  Please ensure that the cache import file exists "
                     + "and that the sdcard is readable from your phone.");
        } catch (XmlPullParserException e) {
            mErrorDisplayer.displayError(R.string.error_parsing_file, GpxLoader.GPX_PATH);
        } catch (IOException e) {
            mErrorDisplayer.displayError(R.string.error_reading_file, GpxLoader.GPX_PATH);
         }
     }
 }
