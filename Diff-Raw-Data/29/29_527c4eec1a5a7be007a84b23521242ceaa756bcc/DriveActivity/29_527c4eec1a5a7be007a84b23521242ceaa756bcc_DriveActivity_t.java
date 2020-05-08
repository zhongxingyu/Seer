 package com.jamiewannenburg.loghours;
 
 import java.io.IOException;
 
 import android.accounts.AccountManager;
 import android.app.Activity;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.Toast;
 
 import com.google.api.client.extensions.android.http.AndroidHttp;
 import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
 import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
 import com.google.api.client.http.FileContent;
 import com.google.api.client.json.gson.GsonFactory;
 import com.google.api.services.drive.Drive;
 import com.google.api.services.drive.DriveScopes;
 import com.google.api.services.drive.model.File;
 import com.jamiewannenburg.loghours.R;
 
 public class DriveActivity extends Activity {
   static final int REQUEST_ACCOUNT_PICKER = 1;
   static final int REQUEST_AUTHORIZATION = 2;
 
   private static Uri fileUri;
   private static Drive service;
   private GoogleAccountCredential credential;
 
   @Override
   public void onCreate(Bundle savedInstanceState) {
     super.onCreate(savedInstanceState);
     setContentView(R.layout.activity_drive);
     
     credential = GoogleAccountCredential.usingOAuth2(this, DriveScopes.DRIVE);
     startActivityForResult(credential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
     
   }
 
   @Override
   protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
     switch (requestCode) {
     case REQUEST_ACCOUNT_PICKER:
       if (resultCode == RESULT_OK && data != null && data.getExtras() != null) {
         String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
         if (accountName != null) {
           credential.setSelectedAccountName(accountName);
           service = getDriveService(credential);
           
           Intent get_intent = getIntent();
   		  fileUri = Uri.fromFile(new java.io.File(get_intent.getStringExtra(MainActivity.FILE)));
   		  saveFileToDrive();
   		  
         }
       }
       break;
     case REQUEST_AUTHORIZATION:
       if (resultCode == Activity.RESULT_OK) {
         saveFileToDrive();
       } else {
         startActivityForResult(credential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
       }
       break;
     
     }
   }
 
   public void backToMain(View view){
 	Intent intent = new Intent(this, MainActivity.class);
 	startActivity(intent);
   }
 
   private void saveFileToDrive() {
     Thread t = new Thread(new Runnable() {
       @Override
       public void run() {
         try {
           // File's binary content
           java.io.File fileContent = new java.io.File(fileUri.getPath());
           FileContent mediaContent = new FileContent("text/csv", fileContent);
 
           // File's metadata.
           File body = new File();
           body.setTitle(fileContent.getName());
           body.setMimeType("text/csv");
           
           
           File file = service.files().insert(body, mediaContent).setConvert(true).execute();
           if (file != null) {
             showToast("file uploaded: " + file.getTitle());
 
           }
         } catch (UserRecoverableAuthIOException e) {
        	startActivityForResult(e.getIntent(), REQUEST_AUTHORIZATION);
        	
         } catch (IOException e) {
        	showToast("file could not be found");
        	e.printStackTrace();
        } catch (NullPointerException e) {
        	showToast("Sruggeling to connect");
        	e.printStackTrace();
         }
       }
     });
     t.start();
   }
 
   private Drive getDriveService(GoogleAccountCredential credential) {
     return new Drive.Builder(AndroidHttp.newCompatibleTransport(), new GsonFactory(), credential)
         .build();
   }
 
   public void showToast(final String toast) {
     runOnUiThread(new Runnable() {
       @Override
       public void run() {
         Toast.makeText(getApplicationContext(), toast, Toast.LENGTH_SHORT).show();
       }
     });
   }
 }
