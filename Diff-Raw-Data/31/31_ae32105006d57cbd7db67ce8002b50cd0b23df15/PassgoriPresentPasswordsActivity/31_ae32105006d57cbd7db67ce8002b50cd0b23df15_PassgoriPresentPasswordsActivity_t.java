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
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.TextView;
 import android.widget.Toast;
 
 /**
  * An activity presenting the a password entity.
  * 
  * @author Miltiadis Allamanis
  * 
  */
 public class PassgoriPresentPasswordsActivity extends Activity {
 
 	public class DeletePasswordThread extends Thread {
 
 		private final String mPasswordId;
 
 		public DeletePasswordThread(final String passwordId) {
 			mPasswordId = passwordId;
 		}
 
 		@Override
 		public void run() {
 			try {
 				if (mPasswordStore.removePassword(mPasswordId)) {
 					runOnUiThread(new PasswordDeleted());
 					// go back!
 				} else {
 					// update GUI
					runOnUiThread(new FailureNotification(
							"Password unaccessbile"));
 				}
 			} catch (PasswordStoreException e) {
				runOnUiThread(new FailureNotification(e.getMessage()));

 			}
 		}
 	}
 
 	public class PasswordDeleted implements Runnable {
 
 		@Override
 		public void run() {
 			mLoadingDialog.dismiss();
 			setResult(1); // on finish ask previous activity to refresh
 			finish();
 			Toast.makeText(getApplicationContext(), "Password Deleted",
 					Toast.LENGTH_LONG);
 		}
 
 	}
 
	private class FailureNotification implements Runnable {

		private final String mMessage;

		public FailureNotification(String message) {
			mMessage = message;
		}

		@Override
		public void run() {
			Toast.makeText(getApplicationContext(), mMessage, Toast.LENGTH_LONG);
		}

	}

 	private class GetPassword extends Thread {
 		@Override
 		public void run() {
 			try {
 				Password password = mPasswordStore.retrivePassword(getIntent()
 						.getExtras().getString("passwordId"));
 
 				if (password != null) {
 					UpdatePasswordDetails updater = new UpdatePasswordDetails(
 							password);
 					runOnUiThread(updater);
 				} else {
 					// Update GUI about failure!!
					runOnUiThread(new FailureNotification(
							"Password Unaccessible"));
 				}
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
 			mUsernameField.setText(mPassword.getUsername());
 			mPasswordField.setText(mPassword.getPassword());
 			mNotesField.setText(mPassword.getNotes());
 
 			mUsernameField.setVisibility(View.VISIBLE);
 			mPasswordField.setVisibility(View.VISIBLE);
 			mNotesField.setVisibility(View.VISIBLE);
 
 			mLoadingDialog.dismiss();
 		}
 
 	}
 
 	/**
 	 * The TextView corresponding to the password's title.
 	 */
 	private TextView mPasswordTitle;
 
 	/**
 	 * The TextView corresponding to the paswords's username.
 	 */
 	private TextView mUsernameField;
 
 	/**
 	 * The TextView corresponding to the password's password.
 	 */
 	private TextView mPasswordField;
 
 	/**
 	 * The TextView corresponding to the password's notes.
 	 */
 	private TextView mNotesField;
 
 	/**
 	 * Progress Dialog to indicate progress.
 	 */
 	private ProgressDialog mLoadingDialog;
 
 	/**
 	 * The password store.
 	 */
 	private IPasswordStore mPasswordStore;
 
 	/** Defines callbacks for service binding, passed to bindService() */
 	private final ServiceConnection mConnection = new ServiceConnection() {
 
 		private boolean mBound;
 
 		@Override
 		public void onServiceConnected(ComponentName className, IBinder service) {
 			PasswordStorageBinder binder = (PasswordStorageBinder) service;
 			mPasswordStore = binder.getStore();
 			mBound = true;
 
 			// Spawn thread to get password
 			new GetPassword().start();
 
 		}
 
 		@Override
 		public void onServiceDisconnected(ComponentName arg0) {
 			mBound = false;
 		}
 	};
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.password_present);
 
 		// Assign content
 		mPasswordTitle = (TextView) findViewById(R.id.passwordTitle);
 		mUsernameField = (TextView) findViewById(R.id.usernameField);
 		mPasswordField = (TextView) findViewById(R.id.passwordField);
 		mNotesField = (TextView) findViewById(R.id.notesField);
 
 		mLoadingDialog = ProgressDialog.show(this, "",
 				"Loading. Please wait...", true);
 
 		mPasswordTitle.setText(getIntent().getExtras().getString("passwordId"));
 		mUsernameField.setVisibility(View.INVISIBLE);
 		mPasswordField.setVisibility(View.INVISIBLE);
 		mNotesField.setVisibility(View.INVISIBLE);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.password_present_menu, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		// Handle item selection
 		switch (item.getItemId()) {
 		case R.id.editPasswordOption:
 			// TODO
 			return true;
 		case R.id.deletePasswordOption:
 			mLoadingDialog = ProgressDialog.show(this, "",
 					"Deleting. Please wait...", true);
 			new DeletePasswordThread(getIntent().getExtras().getString(
 					"passwordId")).start();
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
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
 		}
 
 		// Once the service is binded, we will load the list
 	}
 
 }
