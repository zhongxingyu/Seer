 /*
  * Copyright (c) 2011 Hiroshi Okada (http://toycode.com/hiroshi/)
  * 
  * This software is provided 'as-is', without any express or implied
  * warranty. In no event will the authors be held liable for any damages
  * arising from the use of this software.
  * 
  * Permission is granted to anyone to use this software for any purpose,
  * including commercial applications, and to alter it and redistribute it
  * freely, subject to the following restrictions:
  * 
  *   1.The origin of this software must not be misrepresented; you must
  *     not claim that you wrote the original software. If you use this
  *     software in a product, an acknowledgment in the product
  *     documentation would be appreciated but is not required.
  *   
  *   2.Altered source versions must be plainly marked as such, and must
  *     not be misrepresented as being the original software.
  *   
  *   3.This notice may not be removed or altered from any source
  *     distribution.
  */
 
 package com.toycode.pwmemo;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.database.sqlite.SQLiteDatabase;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.EditText;
 
 import java.io.ByteArrayOutputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.nio.ByteBuffer;
 import java.nio.channels.Channels;
 import java.nio.channels.ReadableByteChannel;
 
 /**
  * Class that handles when open a "*.pwmemo" file by implicit intents.
  * 
  * @author Hiroshi Okada
  */
 public class ImplicitIntentImportActivity extends Activity implements OnClickListener {
 
     private EditText mPasswordEdittext;
     enum ReadMethod { MERGE, INSERT };
     private ReadMethod mReadMethod = ReadMethod.MERGE;
     Uri mUri = null;
     private App mApp;
     ProgressDialog mProgressDialog;
     
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         mApp = App.GetApp(this);
         mProgressDialog = null;
         setContentView(R.layout.implicit_intent_import);
         findViewById(R.id.import_button).setOnClickListener(this);
         findViewById(R.id.cancel_button).setOnClickListener(this);
         mPasswordEdittext = (EditText)findViewById(R.id.import_password_edittext);
         String action = getIntent().getAction();
         if (Intent.ACTION_VIEW.equals(action)) {
             mUri = getIntent().getData();
             if (mUri == null) {
                 finish();
             }
         } else if(Intent.ACTION_SEND.equals(action)) {
             mUri = getIntent().getParcelableExtra(Intent.EXTRA_STREAM);
             if (mUri == null) {
                 finish();
             }
         } else {
             // never come
             finish();
         }
     }
 
     @Override
     protected void onResume() {
         super.onResume();
         if (!PasswordManager.getInstance(this).isMainPasswordExist()) {
             Intent i = new Intent(this, DeclarMasterPasswordActivity.class);
             startActivityForResult(i, Const.REQUEST_TYPE.NEW);
         }
     }
     
     @Override
     protected void onStop() {
         super.onStop();
         if (mProgressDialog != null) {
             mProgressDialog.dismiss();
             mProgressDialog = null;
         }
     }
 
     @Override
     public void onClick(View v) {
         switch (v.getId()) {
             case R.id.import_button:
                 if (App.isEmptyTextView(mPasswordEdittext)) {
                     mApp.toastMessage(R.string.please_set_import_password);
                     return;
                 } else {
                     try{
                         final InputStream inputStream = getContentResolver().openInputStream(mUri);
                         if (PasswordManager.getInstance(this).isMainPasswordDecrypted()) {
                             new ReadFileTask().execute(inputStream);
                         } else {
                             (new MasterPasswordInput(this) {
                                 public void onTureMasterPassword() {
                                     new ReadFileTask().execute(inputStream);
                                 }
                             }).Ask();
                         }
                     } catch (FileNotFoundException e) {
                         mApp.toastMessage(R.string.file_not_found);
                     }
                 }
                 break;
             case R.id.cancel_button:
                 finish();
                 break;
         }
     }
 
     private class ReadFileTask extends AsyncTask<InputStream, Void, Boolean> {
         int mErrorMessageId = 0;
         private final int READ_SIZE = 1024;
         
         @Override
         protected void onPreExecute() {
             super.onPreExecute();
             mProgressDialog = ProgressDialog.show(ImplicitIntentImportActivity.this, "", "Reading...");
         }
 
         @Override
         protected Boolean doInBackground(InputStream... inputStreams) {
             SQLiteDatabase db = (new PwMemoDbOpenHelper(ImplicitIntentImportActivity.this)).getReadableDatabase();
             if (db == null) {
                 throw new RuntimeException("db==null");
             }
 
             byte[] mainPasswod = PasswordManager.getInstance(ImplicitIntentImportActivity.this).getDecryptedMainPassword();
             if (mainPasswod == null) {
                 throw new RuntimeException("mainPasswod==null");
             }
             String json = "";
             try {
                 byte[] password = mPasswordEdittext.getText().toString().getBytes();
                 InputStream is = inputStreams[0];
                 ReadableByteChannel ch = Channels.newChannel(is);
                 ByteArrayOutputStream cryptBytesStream = new ByteArrayOutputStream();
                 byte [] bufferArray = new byte[READ_SIZE];
                 ByteBuffer buffer = ByteBuffer.wrap(bufferArray);
                 buffer.rewind();
                 int readSize = 0;
                 try {
                     while( (readSize = ch.read(buffer)) >= 0) {
                         if (readSize > 0) {
                             cryptBytesStream.write(bufferArray, 0, readSize);
                             buffer.rewind();
                         }
                     }
                 } catch (IOException e) {
                     ;
                 }
                 
                 byte[] cryptBytes = cryptBytesStream.toByteArray();
                 ch.close();
                 byte[] bytesData = OpenSSLAES128CBCCrypt.INSTANCE.decrypt(password, cryptBytes);
                 if (bytesData == null) {
                     mErrorMessageId = R.string.password_does_not_match;
                     return false;
                 }
                 json = new String(bytesData);
             } catch (FileNotFoundException e) {
                 mErrorMessageId = R.string.file_not_found;
                 return false;
             } catch (IOException e) {
                 mErrorMessageId = R.string.file_read_error;
                 return false;
             } catch (CryptException e) {
                 mErrorMessageId = e.GetMsgId();
                 return false;
             }
 
             DbRw dbrw = new DbRw(db, mainPasswod);
             switch (mReadMethod) {
                 case MERGE:
                     dbrw.insertRecords(json);
                     break;
                 case INSERT:
                     dbrw.mergeRecords(json);
                     break;
                 default:
                     break;
             }
             dbrw.cleanup();
             return true;
         }
 
         @Override
         protected void onPostExecute(Boolean result) {
             if (mProgressDialog != null){
                 mProgressDialog.dismiss();
                 mProgressDialog = null;
             }
             if (result == true) {
                 mApp.toastMessage(R.string.import_ok);
             } else {
                 mApp.toastMessage(mErrorMessageId);
             }
             super.onPostExecute(result);
         }
      }
 }
