 package in.uncod.android.droidbooru;
 
 import in.uncod.android.droidbooru.backend.Backend;
 import in.uncod.android.droidbooru.net.FilesUploadedCallback;
 import in.uncod.android.util.threading.TaskWithResultListener.OnTaskResultListener;
 
 import java.io.File;
 import java.net.MalformedURLException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.http.HttpHost;
 
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Handler;
 import android.util.Log;
 import android.view.Window;
 
 public class UploadFilesActivity extends DroidBooruAccountActivity {
    private static final String TAG = "ReceiveContentActivity";

    /**
     * Request code to get a single file
     */
    private static final int REQ_CODE_GET_FILE = 0;
 
     private Handler mUiHandler;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
 
         setContentView(R.layout.activity_upload_files);
 
         mUiHandler = new Handler();
     }
 
     protected void onAccountLoaded() {
         setProgressBarIndeterminateVisibility(true);
 
         Intent intent = getIntent();
         if (Intent.ACTION_SEND.equals(intent.getAction())) {
             // Started via Share request
             Log.d(TAG, "Received single file upload request");
 
             Uri shareUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
             if (shareUri == null) {
                 // No direct file URI; see if there's a URI in the text extra
                 shareUri = Uri.parse(intent.getStringExtra(Intent.EXTRA_TEXT));
             }
 
             Log.d(TAG, "Shared URI is " + shareUri);
 
             if (shareUri.getScheme().equals(HttpHost.DEFAULT_SCHEME_NAME)) {
                 // URI is HTTP link to file; download it first
                 try {
                     Backend.getInstance(this).downloadTempFileFromHttp(shareUri,
                             new OnTaskResultListener<List<File>>() {
                                 public void onTaskResult(List<File> result) {
                                     if (result.size() > 0) {
                                         uploadSingleFile(Uri.fromFile(result.get(0)));
                                     }
                                     else {
                                         setResult(RESULT_CANCELED);
                                         finish();
                                     }
                                 }
                             });
                 }
                 catch (MalformedURLException e) {
                     e.printStackTrace();
 
                     setResult(RESULT_CANCELED);
                     finish();
                 }
             }
             else {
                 // Uploading a single file
                 uploadSingleFile(shareUri);
             }
         }
         else if (Intent.ACTION_SEND_MULTIPLE.equals(intent.getAction())) {
             // Started via Share request
             Log.d(TAG, "Received multiple file upload request");
 
             // Uploading multiple files
             ArrayList<Uri> fileUris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
             File[] files = new File[fileUris.size()];
 
             int i = 0;
             for (Uri uri : fileUris) {
                 files[i] = Backend.getInstance(this).createTempFileForUri(uri, getContentResolver());
 
                 i++;
             }
 
             uploadMultipleFiles(files);
         }
     }
 
     private void uploadMultipleFiles(File[] files) {
         Backend.getInstance(this).uploadFiles(files, mAccount.name,
                 Backend.getInstance(this).getDefaultTags(), mUiHandler, new FilesUploadedCallback() {
                     public void onFilesUploaded(boolean error) {
                         finishUpload(error);
                     }
                 });
     }
 
     private void uploadSingleFile(Uri fileUri) {
         Backend.getInstance(this).uploadFiles(
                 new File[] { Backend.getInstance(this).createTempFileForUri(fileUri, getContentResolver()) },
                 mAccount.name, Backend.getInstance(this).getDefaultTags(), mUiHandler,
                 new FilesUploadedCallback() {
                     public void onFilesUploaded(boolean error) {
                         finishUpload(error);
                     }
                 });
     }
 
     private void finishUpload(boolean error) {
         setProgressBarIndeterminateVisibility(false);
 
         if (error) {
             setResult(RESULT_CANCELED);
         }
         else {
             setResult(RESULT_OK);
         }
 
         finish();
     }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQ_CODE_GET_FILE) {
            // Received data back from gallery; exit
            setResult(resultCode, data);
            finish();
        }
        else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
 }
