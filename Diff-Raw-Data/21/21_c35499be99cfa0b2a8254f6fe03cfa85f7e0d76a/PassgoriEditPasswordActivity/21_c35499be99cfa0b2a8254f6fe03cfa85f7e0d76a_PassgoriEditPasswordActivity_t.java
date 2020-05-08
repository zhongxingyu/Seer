 /*
  * Copyright 2011 Miltiadis Allamanis
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
  
 /**
  * 
  */
 package ac.uk.cam.cl.passgroiApp;
 
 import uk.ac.cam.cl.passgori.IPasswordStore;
 import uk.ac.cam.cl.passgori.Password;
 import uk.ac.cam.cl.passgori.PasswordStoreException;
 import ac.uk.cam.cl.passgroiApp.PasswordStoreService.PasswordStorageBinder;
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.view.View;
 import android.widget.Button;
 import android.widget.TextView;
 import android.widget.Toast;
 
 /**
  * An activity permitting to edit or add new passwords.
  * 
  * @author Miltiadis Allamanis
  * 
  */
 public class PassgoriEditPasswordActivity extends Activity {
 
 	private final class CancelButtonListener implements View.OnClickListener {
 		@Override
 		public void onClick(View v) {
 			// Finish
 			finish();
 		}
 	}
 
 	private class FailureNotification implements Runnable {
 
 		private final String mMessage;
 
 		public FailureNotification(String message) {
 			mMessage = message;
 		}
 
 		@Override
 		public void run() {
 			if (mLoadingDialog != null)
 				mLoadingDialog.dismiss();
 
 			Toast.makeText(getApplicationContext(), mMessage, Toast.LENGTH_LONG);
 		}
 
 	}
 
 	private class GetPassword extends Thread {
 		@Override
 		public void run() {
 			try {
 				if (getIntent().getExtras() != null) {
 					String passId = getIntent().getExtras().getString(
 							"passwordId");
 					updateUI(passId);
 				} else {
 					UpdatePasswordDetails updater = new UpdatePasswordDetails(
 							null);
 					runOnUiThread(updater);
 				}
 
 			} catch (PasswordStoreException e) {
 				runOnUiThread(new FailureNotification(e.getMessage()));
 
 			}
 		}
 
 		/**
 		 * @param passId
 		 * @throws PasswordStoreException
 		 */
 		private void updateUI(String passId) throws PasswordStoreException {
 			Password password = mPasswordStore.retrivePassword(passId);
 
 			if (password != null) {
 				UpdatePasswordDetails updater = new UpdatePasswordDetails(
 						password);
 				runOnUiThread(updater);
 			} else {
 				// Update GUI about failure!!
 				runOnUiThread(new FailureNotification("Password Unaccessible"));
 			}
 		}
 	}
 
 	private class PasswordSaved implements Runnable {
 		@Override
 		public void run() {
 			mLoadingDialog.dismiss();
 			setResult(1);
 			finish();
 		}
 	}
 
 	private final class SaveButtonListener implements View.OnClickListener {
 
 		@Override
 		public void onClick(View v) {
 			// Save changes and go back to initial screen!
 			Password toBeSaved = new Password(mTitleText.getText().toString(),
 					mUsernameText.getText().toString(), mPasswordText.getText()
 							.toString(), mNotesText.getText().toString());
 			mLoadingDialog = ProgressDialog.show(
 					PassgoriEditPasswordActivity.this, "",
 					"Saving. Please wait...", true);
 			new SavePassword(toBeSaved).start();
 
 		}
 	}
 
 	private class SavePassword extends Thread {
 
 		private final Password mPassword;
 
 		public SavePassword(Password password) {
 			mPassword = password;
 		}
 
 		@Override
 		public void run() {
 			try {
 
         mPasswordStore.storePassword(mPassword);
         // But if we changed the title, then we have to delete the old
         // id password too!
         if (getIntent().getExtras() != null) {
           String delteId = getIntent().getExtras().getString("passwordId");
           if (!mPassword.getId().equals(delteId)) {// Only delete it if we didn't just add it.
             mPasswordStore.removePassword(delteId);
           }
         }
 				runOnUiThread(new PasswordSaved());
 			} catch (PasswordStoreException e) {
 				runOnUiThread(new FailureNotification(e.getMessage()));
 			}
 		}
 	}
 
 	private class UpdatePasswordDetails implements Runnable {
 
 		private final Password mPassword;
 
 		public UpdatePasswordDetails(Password password) {
 			mPassword = password;
 		}
 
 		@Override
 		public void run() {
 			if (mPassword != null) {
 				mTitleText.setText(mPassword.getId());
 				mUsernameText.setText(mPassword.getUsername());
 				mPasswordText.setText(mPassword.getPassword());
 				mNotesText.setText(mPassword.getNotes());
 			}
 
 			mLoadingDialog.dismiss();
 		}
 
 	}
 
 	public ProgressDialog mLoadingDialog;
 	private Button mButtonSave;
 	private Button mButtonCancel;
 	private TextView mTitleText;
 
 	private TextView mUsernameText;
 
 	private TextView mPasswordText;
 
 	private TextView mNotesText;
 
 	protected IPasswordStore mPasswordStore;
 
 	private final ServiceConnection mConnection = new ServiceConnection() {
 
		//private boolean mBound;
 
 		@Override
 		public void onServiceConnected(ComponentName className, IBinder service) {
 			PasswordStorageBinder binder = (PasswordStorageBinder) service;
 			try {
 				mPasswordStore = binder.getStore();
				//mBound = true;
 
 				// Spawn thread to get password details, if any!
 				new GetPassword().start();
 			} catch (PasswordStoreException e) {
 				new FailureNotification(e.getMessage()).run();
 			}
 
 		}
 
 		@Override
 		public void onServiceDisconnected(ComponentName arg0) {
			//mBound = false;
 		}
 	};
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.password_edit);
 
 		mButtonSave = (Button) findViewById(R.id.saveButton);
 		mButtonCancel = (Button) findViewById(R.id.cancelButton);
 
 		mButtonSave.setOnClickListener(new SaveButtonListener());
 		mButtonCancel.setOnClickListener(new CancelButtonListener());
 
 		mTitleText = (TextView) findViewById(R.id.passwordTitleEdit);
 		mUsernameText = (TextView) findViewById(R.id.usernameEdit);
 		mPasswordText = (TextView) findViewById(R.id.passwordEdit);
 		mNotesText = (TextView) findViewById(R.id.notesEdit);
 
 	}
 
 	@Override
 	public void onStop() {
 		super.onStop();
 		// Close activity to avoid storing the password if someone exits us from
 		// the home button
 		finish();
 	}
 
 	@Override
 	protected void onStart() {
 		super.onStart();
 		// Bind to PasswordStoreService
 		Intent intent = new Intent(this, PasswordStoreService.class);
 
 		if (!getApplicationContext().bindService(intent, mConnection,
 				Context.BIND_AUTO_CREATE)) {
 			// Inform GUI for failure!!
 			new FailureNotification("Unable to connect to internal server")
 					.run();
 		}
 
 		mLoadingDialog = ProgressDialog.show(PassgoriEditPasswordActivity.this,
 				"", "Loading. Please wait...", true);
 		// Once the service is binded, we will load the list
 	}
 
 }
