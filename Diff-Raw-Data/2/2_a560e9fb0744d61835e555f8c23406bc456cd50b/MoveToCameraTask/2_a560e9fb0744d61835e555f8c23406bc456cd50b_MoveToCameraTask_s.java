 package net.kaoriya.android.shphotofolderhelper;
 
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.os.AsyncTask;
 import android.util.Log;
 import android.widget.Toast;
 
 public class MoveToCameraTask
     extends AsyncTask<Void, Void, Integer>
     implements ISHPhotoFolderHelper
 {
 
     private final Context context;
     private ProgressDialog progressDialog = null;
 
     public MoveToCameraTask(Context context) {
         this.context = context;
     }
 
     @Override
     protected Integer doInBackground(Void... params) {
         int count = 0;
         try {
             PhotoFolderHelper helper = new PhotoFolderHelper(this.context);
             count = helper.moveToCamera(true);
         } catch (Exception e) {
             Log.e(TAG, "#moveToCamera failed", e);
             count = -1;
         }
         return count;
     }
 
     @Override
     protected void onPreExecute() {
         super.onPreExecute();
 
         // プログレスダイアログを表示.
         ProgressDialog progress = new ProgressDialog(this.context);
         progress.setTitle("Processing");
         progress.setIndeterminate(true);
         progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
         progress.setCancelable(false);
         progress.show();
         this.progressDialog = progress;
     }
 
     @Override
     protected void onPostExecute(Integer result) {
         super.onPostExecute(result);
 
         if (this.progressDialog != null) {
             this.progressDialog.dismiss();
         }
 
         final Toast toast;
         if (result > 0) {
             toast = Toast.makeText(this.context,
                    "Process " + result + "images",
                     Toast.LENGTH_SHORT);
         } else if (result == 0) {
             toast = Toast.makeText(this.context,
                     "No images processed",
                     Toast.LENGTH_SHORT);
         } else {
             toast = Toast.makeText(this.context,
                     "Got error",
                     Toast.LENGTH_SHORT);
         }
         toast.show();
     }
 }
