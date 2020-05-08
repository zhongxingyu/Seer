 package de.jensnistler.trackmap.helper;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.InputStream;
 import java.net.HttpURLConnection;
 import java.net.URL;
 
 import de.jensnistler.trackmap.R;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnCancelListener;
 import android.os.AsyncTask;
 import android.widget.Toast;
 
 public class MapDownloader extends AsyncTask<MapModel, Integer, Integer> {
     private boolean mRunning = true;
     private Activity mContext;
     private MapAdapter mAdapter;
     private MapDataSource mDataSource;
     private ProgressDialog mDialog;
 
     public MapDownloader(Activity context, MapAdapter adapter, MapDataSource dataSource) {
         mContext = context;
         mAdapter = adapter;
         mDataSource = dataSource;
         mDialog = new ProgressDialog(mContext);
     }
 
     protected void onPreExecute() {
         mAdapter.setNotifyOnChange(false);
 
         // keep screen on while downloading
         mContext.getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
 
         mDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
         mDialog.setMessage(mContext.getResources().getString(R.string.downloading));
         mDialog.setCancelable(true);
         mDialog.setOnCancelListener(new OnCancelListener() {
             public void onCancel(DialogInterface dialog) {
                 mRunning = false;
             }
         });
         mDialog.setIndeterminate(false);
         mDialog.setMax(0);
         mDialog.setProgress(0);
         mDialog.show();
     }
 
     protected Integer doInBackground(MapModel... maps) {
         Integer count = 0;
 
         for (MapModel map: maps) {
             try {
                 URL url = new URL(map.getUrl());
                 HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                 urlConnection.setRequestMethod("GET");
                 urlConnection.setDoOutput(true);
                 urlConnection.connect();
 
                 File cacheDir = mContext.getExternalCacheDir();
                 if (null == cacheDir || !cacheDir.canWrite()) {
                     publishProgress(0, 0);
                     continue;
                 }
                 File cacheFile = new File(cacheDir, map.getKey().replace("/", "_") + ".map");
 
                 FileOutputStream fileOutput = new FileOutputStream(cacheFile);
                 InputStream inputStream = urlConnection.getInputStream();
 
                 int totalSize = urlConnection.getContentLength();
                 int downloadedSize = 0;
 
                 //create a buffer...
                 byte[] buffer = new byte[1024];
                 int bufferLength = 0;
 
                 while (true == mRunning && (bufferLength = inputStream.read(buffer)) > 0) {
                     fileOutput.write(buffer, 0, bufferLength);
                     downloadedSize += bufferLength;
 
                     publishProgress(totalSize, downloadedSize);
                 }
 
                 //close the output stream when done
                 urlConnection.disconnect();
                 fileOutput.close();
 
                 // file was not downloaded completely
                 if (downloadedSize < totalSize) {
                     cacheFile.delete();
                     continue;
                 }
 
                 // update map dataset
                 mAdapter.remove(map);
                 map.setUpdated(map.getDate());
                 mDataSource.saveMap(map);
                 mAdapter.add(map);
                 count++;
             }
             catch (Exception e) {
                 e.printStackTrace();
             }
         }
 
         return count; 
     }
 
     protected void onProgressUpdate(Integer... progress) {
         if (0 == progress[0]) {
             Toast.makeText(mContext, R.string.cannotWriteToCache, Toast.LENGTH_LONG).show();
         }
 
         mDialog.setMax(progress[0]);
         mDialog.setProgress(progress[1]);
     }
 
     protected void onPostExecute(Integer count) {
        mDialog.dismiss();
 
         // finish keep screen on while downloading
         mContext.getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
 
         mAdapter.setNotifyOnChange(true);
         if (count > 0) {
             mAdapter.notifyDataSetChanged();
         }
         else {
             Toast.makeText(mContext, R.string.downloadFailed, Toast.LENGTH_LONG).show();
         }
     }
 }
