 /*
  * Copyright (C) 2012 Pixmob (http://github.com/pixmob)
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
 package org.pixmob.freemobile.netstat.ui;
 
 import static org.pixmob.freemobile.netstat.Constants.TAG;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.TimeZone;
 
 import org.pixmob.freemobile.netstat.R;
 import org.pixmob.freemobile.netstat.content.NetstatContract.Events;
 import org.pixmob.freemobile.netstat.util.IOUtils;
 
 import android.content.Context;
 import android.database.Cursor;
 import android.os.AsyncTask;
 import android.os.Environment;
 import android.support.v4.app.DialogFragment;
 import android.support.v4.app.FragmentManager;
 import android.util.Log;
 import android.widget.Toast;
 
 /**
  * Export database to a file on the external storage.
  * @author Pixmob
  */
 class ExportTask extends AsyncTask<Void, Integer, Boolean> {
     private static final String DIALOG_TAG = "export";
     private static final String LINE_SEP = "\r\n";
     private static final String COL_SEP = ";";
     private static final String DATE_FORMAT = "dd/MM/yyyy HH:mm:ss";
     private final Context context;
     private FragmentManager fragmentManager;
     
     public ExportTask(final Context context,
             final FragmentManager fragmentManager) {
         this.context = context;
         this.fragmentManager = fragmentManager;
     }
     
     public void setFragmentManager(FragmentManager fragmentManager) {
         this.fragmentManager = fragmentManager;
     }
     
     @Override
     protected Boolean doInBackground(Void... params) {
         try {
             export();
             Log.i(TAG, "Export done");
         } catch (IOException e) {
             Log.e(TAG, "Failed to export database", e);
             return false;
         }
         return true;
     }
     
     @Override
     protected void onProgressUpdate(Integer... values) {
         final ExportDialogFragment f = (ExportDialogFragment) fragmentManager
                 .findFragmentByTag(DIALOG_TAG);
         if (f != null) {
             final int current = values[0];
             final int total = values[1];
             f.update(current, total);
         }
     }
     
     @Override
     protected void onPreExecute() {
         if (!Environment.getExternalStorageState().equals(
             Environment.MEDIA_MOUNTED)) {
             Log.w(TAG, "External storage is not available");
             Toast.makeText(context,
                 context.getString(R.string.external_storage_not_available),
                 Toast.LENGTH_SHORT).show();
            return;
         }
        
        new ExportDialogFragment().show(fragmentManager, DIALOG_TAG);
     }
     
     @Override
     protected void onPostExecute(Boolean result) {
         dismissDialog();
         
         if (result) {
             Toast.makeText(context, R.string.export_done, Toast.LENGTH_SHORT)
                     .show();
         } else {
             Toast.makeText(context, R.string.export_error, Toast.LENGTH_SHORT)
                     .show();
         }
     }
     
     @Override
     protected void onCancelled(Boolean result) {
         dismissDialog();
     }
     
     private void dismissDialog() {
         final DialogFragment f = (DialogFragment) fragmentManager
                 .findFragmentByTag(DIALOG_TAG);
         if (f != null) {
             f.dismiss();
         }
         
         // Clear reference to avoid memory leaks.
         fragmentManager = null;
     }
     
     private void export() throws IOException {
         final File outputFile = new File(
                 Environment.getExternalStorageDirectory(),
                 "freemobilenetstat.csv");
         Log.i(TAG, "Exporting database to " + outputFile.getPath());
         
         final DateFormat dateFormatter = new SimpleDateFormat(DATE_FORMAT);
         dateFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));
         
         final BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
                 new FileOutputStream(outputFile), "UTF-8"));
         
         final Cursor c = context.getContentResolver().query(
             Events.CONTENT_URI,
             new String[] { Events.TIMESTAMP, Events.MOBILE_OPERATOR,
                     Events.MOBILE_CONNECTED, Events.WIFI_CONNECTED,
                     Events.BATTERY_LEVEL, Events.SCREEN_ON }, null, null, null);
         
         try {
             final int rowCount = c.getCount();
             int currentRow = 0;
             
             final StringBuilder buf = new StringBuilder(1024);
             buf.append("#Timestamp").append(COL_SEP).append("Mobile Operator")
                     .append(COL_SEP).append("Mobile Connected").append(COL_SEP)
                     .append("Wi-Fi Connected").append(COL_SEP)
                     .append("Screen On").append(COL_SEP).append("Battery")
                     .append(LINE_SEP);
             out.write(buf.toString());
             
             while (c.moveToNext()) {
                 final long t = c.getLong(0);
                 final String mobOp = c.isNull(1) ? "" : c.getString(1);
                 final int mobConn = c.getInt(2) == 1 ? 1 : 0;
                 final int wifiOn = c.getInt(3) == 1 ? 1 : 0;
                 final int bat = c.getInt(4);
                 final int screenOn = c.getInt(5) == 1 ? 1 : 0;
                 
                 buf.delete(0, buf.length());
                 buf.append(dateFormatter.format(t)).append(COL_SEP)
                         .append(mobOp).append(COL_SEP).append(mobConn)
                         .append(COL_SEP).append(wifiOn).append(COL_SEP)
                         .append(screenOn).append(COL_SEP).append(bat)
                         .append(LINE_SEP);
                 out.write(buf.toString());
                 
                 publishProgress(++currentRow, rowCount);
             }
         } finally {
             IOUtils.close(out);
             c.close();
         }
     }
 }
