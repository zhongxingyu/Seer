 package com.kii.push;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 
 import android.os.Bundle;
 import android.os.Environment;
 import android.support.v4.app.DialogFragment;
 import android.support.v4.app.FragmentActivity;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ListView;
 import android.widget.Toast;
 
 import com.kii.cloud.storage.Kii;
 import com.kii.cloud.storage.KiiACL;
 import com.kii.cloud.storage.KiiACL.FileAction;
 import com.kii.cloud.storage.KiiACLEntry;
 import com.kii.cloud.storage.KiiAnyAuthenticatedUser;
 import com.kii.cloud.storage.KiiFile;
 import com.kii.cloud.storage.KiiFileBucket;
 import com.kii.cloud.storage.KiiSubscribable;
 import com.kii.cloud.storage.KiiUser;
 import com.kii.cloud.storage.callback.KiiACLCallBack;
 import com.kii.cloud.storage.callback.KiiFileCallBack;
 import com.kii.cloud.storage.callback.KiiPushCallBack;
 
 public class FileBucketPushActivity extends FragmentActivity implements
         OnItemClickListener {
 
     private static final String FILE_BUCKET_NAME = "testFileBucket";
     private static final String FILE_PATH = "com.kii.push/test_file.txt";
     private KiiFile mFile = null;
 
     @Override
     protected void onCreate(Bundle arg0) {
         super.onCreate(arg0);
         setContentView(R.layout.filebucketpush);
         ListView lv = (ListView) findViewById(R.id.filebucket_push_list);
         lv.setOnItemClickListener(this);
     }
 
     @Override
     public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
         if (pos == 0) { // File bucket controll
             showListDialog();
         } else if (pos == 1) { // Subscribe file bucket
             KiiFileBucket bucket = Kii.fileBucket(FILE_BUCKET_NAME);
             KiiUser.getCurrentUser().pushSubscription()
                     .subscribe(bucket, new KiiPushCallBack() {
                         @Override
                         public void onSubscribeCompleted(int taskId,
                                 KiiSubscribable target, Exception e) {
                             if (e != null) {
                                 showAlertDialog(e.getMessage());
                             } else {
                                 Toast.makeText(getApplicationContext(),
                                         "Subscribe succeeded",
                                         Toast.LENGTH_LONG).show();
                             }
                         }
                     });
         }
     }
 
     private void showAlertDialog(String message) {
         DialogFragment newFragment = AlertDialogFragment
                 .newInstance(android.R.string.dialog_alert_title, message);
         newFragment.show(this.getSupportFragmentManager(),
                 AlertDialogFragment.TAG);
     }
 
     private void showListDialog() {
         ListDialogFragment frag = ListDialogFragment.newInstance(
                 R.layout.file_con_listdialog, R.string.file_bucket_controll,
                 android.R.drawable.ic_menu_edit, new OnItemClickListener(){
                     @Override
                     public void onItemClick(AdapterView<?> parent, View view,
                             int pos, long id) {
                         if (pos == 0 ) { // Create file in bucket.
                             doCreateFile();
                         } else if(pos == 1) {
                             doUpdateFileMetaData();
                         } else if(pos == 2) {
                             doUpdateFileBody();
                         } else if(pos == 3) {
                             doDeleteFile();
                         } else if(pos == 4) {
                             doMoveFileToTrash();
                         } else if(pos == 5) {
                             doUpdateFileACL(); 
                         } else if(pos == 6) {
                             doRestoreFileFromTrash();
                        } else {
                            throw new RuntimeException("Unknown Error!");
                         }
                         dismissDialogByTag(ListDialogFragment.TAG);
                     }
                 });
         frag.show(this.getSupportFragmentManager(), ListDialogFragment.TAG);
     }
 
     private void doCreateFile() {
         mFile = Kii.fileBucket(FILE_BUCKET_NAME).file();
         mFile.save(new KiiFileCallBack() {
             @Override
             public void onSaveCompleted(int token,
                     KiiFile file, Exception exception) {
                 if (exception != null) {
                     // TODO: impl progress.
                     showAlertDialog(exception.getMessage());
                 } else {
                     Toast.makeText(getApplicationContext(),
                             "Create file suceeded",
                             Toast.LENGTH_LONG).show();
                 }
             }
         });
     }
 
     private void doUpdateFileMetaData() {
         if(mFile == null) {
             Toast.makeText(getApplicationContext(),
                     "File is not created yet.",
                     Toast.LENGTH_LONG).show();
             return;
         }
         mFile.setCustomeField("custom"+System.currentTimeMillis());
         mFile.save(new KiiFileCallBack() {
             @Override
             public void onSaveCompleted(int token,
                     KiiFile file, Exception exception) {
                 if (exception != null) {
                     showAlertDialog(exception.getMessage());
                 } else {
                     Toast.makeText(getApplicationContext(),
                             "update meta-data suceeded",
                             Toast.LENGTH_LONG).show();
                 }
             }
         });
     }
 
     private void doUpdateFileBody() {
         if(mFile == null) {
             Toast.makeText(getApplicationContext(),
                     "File is not created yet.",
                     Toast.LENGTH_LONG).show();
             return;
         }
         File file = new File(Environment.getExternalStorageDirectory(),
                 FILE_PATH);
         try {
             writeFile(file, "content=" + System.currentTimeMillis());
         } catch (IOException ioe) {
             showAlertDialog(ioe.getMessage());
         }
         mFile.save(new KiiFileCallBack() {
             @Override
             public void onSaveCompleted(int token,
                     KiiFile file, Exception exception) {
                 if (exception != null) {
                     showAlertDialog(exception.getMessage());
                 } else {
                     Toast.makeText(getApplicationContext(),
                             "file body update suceeded",
                             Toast.LENGTH_LONG).show();
                 }
             }
         }, file);
     }
 
     private void doDeleteFile() {
         if(mFile == null) {
             Toast.makeText(getApplicationContext(),
                     "File is not created yet.",
                     Toast.LENGTH_LONG).show();
             return;
         }
         mFile.delete(new KiiFileCallBack() {
             @Override
             public void onDeleteCompleted(int token, Exception exception) {
                 if (exception != null) {
                     showAlertDialog(exception.getMessage());
                 } else {
                     mFile = null;
                     Toast.makeText(getApplicationContext(),
                             "file delete suceeded",
                             Toast.LENGTH_LONG).show();
                 }
             }
         });
     }
 
     private void doMoveFileToTrash() {
         if(mFile == null) {
             Toast.makeText(getApplicationContext(),
                     "File is not created yet.",
                     Toast.LENGTH_LONG).show();
             return;
         }
         mFile.moveToTrash(new KiiFileCallBack() {
             @Override
             public void onMoveTrashCompleted(int token, KiiFile file,
                     Exception exception) {
                 if (exception != null) {
                     showAlertDialog(exception.getMessage());
                 } else {
                     Toast.makeText(getApplicationContext(),
                             "file move to trash suceeded",
                             Toast.LENGTH_LONG).show();
                 }
             }
         
         });
     }
 
     private void doUpdateFileACL() {
         if (mFile == null) {
             Toast.makeText(getApplicationContext(), "File is not created yet.",
                     Toast.LENGTH_LONG).show();
             return;
         }
         KiiACL acl = mFile.acl();
         KiiACLEntry entry = new KiiACLEntry(KiiAnyAuthenticatedUser.create(),
                 FileAction.READ_EXISTING_OBJECT, true);
         acl.putACLEntry(entry);
         acl.save(new KiiACLCallBack() {
 
             @Override
             public void onSaveCompleted(int token, KiiACL acl,
                     Exception exception) {
                 if (exception != null) {
                     showAlertDialog(exception.getMessage());
                 } else {
                     Toast.makeText(getApplicationContext(),
                             "file acl change succeeded", Toast.LENGTH_LONG)
                             .show();
                 }
             }
 
         });
     }
 
     private void doRestoreFileFromTrash() {
         if(mFile == null) {
             Toast.makeText(getApplicationContext(),
                     "File is not created yet.",
                     Toast.LENGTH_LONG).show();
             return;
         }
         mFile.restoreFromTrash(new KiiFileCallBack() {
             @Override
             public void onRestoreTrashCompleted(int token, KiiFile file,
                     Exception exception) {
                 if (exception != null) {
                     showAlertDialog(exception.getMessage());
                 } else {
                     Toast.makeText(getApplicationContext(),
                             "file restore from trash suceeded",
                             Toast.LENGTH_LONG).show();
                 }
             }
         
         });
     }
 
 
     private void dismissDialogByTag(String tag) {
         DialogFragment df = (DialogFragment) FileBucketPushActivity.this
                 .getSupportFragmentManager().findFragmentByTag(
                         ListDialogFragment.TAG);
         df.dismiss();
     }
  
     private void writeFile(File file, String content)  throws IOException {
         BufferedWriter bw = null;
         try {
             if (!file.exists()) {
                 file.createNewFile();
             }
             bw = new BufferedWriter(new FileWriter(file));
             bw.write(content);
         } finally {
             if (bw != null)
                 bw.close();
         }
     }
 
 
 }
