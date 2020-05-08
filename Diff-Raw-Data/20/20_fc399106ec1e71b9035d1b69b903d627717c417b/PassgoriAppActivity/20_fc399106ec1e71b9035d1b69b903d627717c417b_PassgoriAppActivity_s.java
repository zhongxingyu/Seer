 package ac.uk.cam.cl.passgroiApp;
 
 import java.util.List;
 
 import uk.ac.cam.cl.passgori.IPasswordStore;
 import uk.ac.cam.cl.passgori.PasswordStoreException;
 import ac.uk.cam.cl.passgroiApp.PasswordStoreService.PasswordStorageBinder;
 import android.app.Activity;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.ArrayAdapter;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 
 /**
  * The main password listing activity.
  * 
  * @author Miltiadis Allamanis
  * 
  */
 public class PassgoriAppActivity extends Activity {
 
 	/**
 	 * A Runnable for updating the GUI on failure.
 	 * 
 	 */
 	private class InformFailureRunnable implements Runnable {
 
 		private final String mFailureMessage;
 
 		/**
 		 * The constructor.
 		 * 
 		 * @param message
 		 *            the message to be presented to the user
 		 */
 		public InformFailureRunnable(String message) {
 			mFailureMessage = message;
 		}
 
 		@Override
 		public void run() {
 			mWaitingLinearLayout.removeAllViews();
 			mWaitingText.setText(mFailureMessage);
 			mWaitingLinearLayout.addView(mWaitingText);
 			// TODO: Ideally add a failure sign!
 		}
 
 	}
 
 	/**
 	 * A Thread responsible for updating the list
 	 * 
 	 */
 	private class UpdateList extends Thread {
 		@Override
 		public void run() {
 
 			List<String> passwordList;
 			try {
 				passwordList = getPasswordList();
 				if (passwordList != null) {
 					// Update GUI
 					final ArrayAdapter<String> adapter = new ArrayAdapter<String>(
 							PassgoriAppActivity.this,
 							R.layout.password_list_item, passwordList);
 
 					runOnUiThread(new UpdateListRunnable(adapter));
 				} else {
 					runOnUiThread(new InformFailureRunnable(
 							getString(R.string.noPasswordsError)));
 				}
 
 			} catch (final PasswordStoreException e) {
 				runOnUiThread(new InformFailureRunnable(e.getMessage()));
 			}
 
 		}
 
 		/**
 		 * Retrieve Password List
 		 * 
 		 * @return
 		 * @throws PasswordStoreException
 		 */
 		private List<String> getPasswordList() throws PasswordStoreException {
 			mPasswordStore.authorize("l", "b"); // TODO: Change...
 			return mPasswordStore.getAllStoredPasswordIds();
 
 		}
 
 	}
 
 	/**
 	 * Runnable to update the list on the GUI
 	 * 
 	 */
 	private class UpdateListRunnable implements Runnable {
 		/**
 		 * The password List
 		 */
 		final ArrayAdapter<String> mPasswordAdapter;
 
 		public UpdateListRunnable(final ArrayAdapter<String> passwordAdapter) {
 			mPasswordAdapter = passwordAdapter;
 		}
 
 		@Override
 		public void run() {
 			ListView passwordListView = new ListView(PassgoriAppActivity.this);
 			passwordListView.setAdapter(mPasswordAdapter);
 			// Remove waiting
 			mWaitingLinearLayout.removeAllViews();
 			mWaitingLinearLayout.addView(passwordListView);
 		}
 
 	}
 
 	/**
 	 * The password store.
 	 */
 	private IPasswordStore mPasswordStore;
 
 	/**
 	 * Boolean indicating if activity is bound on service.
 	 */
 	boolean mBound = false;
 
 	/** Defines callbacks for service binding, passed to bindService() */
 	private final ServiceConnection mConnection = new ServiceConnection() {
 
 		@Override
 		public void onServiceConnected(ComponentName className, IBinder service) {
 			PasswordStorageBinder binder = (PasswordStorageBinder) service;
 			mPasswordStore = binder.getStore();
 			mBound = true;
 
 			// Load List!
 			new UpdateList().start();
 		}
 
 		@Override
 		public void onServiceDisconnected(ComponentName arg0) {
 			mBound = false;
 		}
 	};
 
 	private LinearLayout mWaitingLinearLayout;
 
 	private TextView mWaitingText;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.password_list);
 
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.password_list_menu, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		// Handle item selection
 		switch (item.getItemId()) {
 		case R.id.passwordListRefresh:
 			mWaitingLinearLayout.removeAllViews();
 			mWaitingText.setText(R.string.refreshing);
 			final LayoutParams params = new LayoutParams(
 					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
 
 			final ProgressBar pb = new ProgressBar(this);
 			pb.setLayoutParams(params);
 			mWaitingLinearLayout.addView(pb);
 			mWaitingLinearLayout.addView(mWaitingText);
 			new UpdateList().start();
 			return true;
 		case R.id.passgoriConfigure:
 			// TODO
 			return true;
 		case R.id.passwordListAdd:
 			// TODO
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	@Override
	protected void onDestroy() {
		super.onDestroy();
		// Unbind from the service
		if (mBound) {
			unbindService(mConnection);
			mBound = false;
		}
	}

	@Override
 	protected void onStart() {
 		super.onStart();
 
 		mWaitingLinearLayout = (LinearLayout) this
 				.findViewById(R.id.loadingView);
 		mWaitingText = (TextView) this.findViewById(R.id.listLoadingText);
 
 		// Bind to PasswordStoreService
 		Intent intent = new Intent(this, PasswordStoreService.class);
 
 		if (!getApplicationContext().bindService(intent, mConnection,
 				Context.BIND_AUTO_CREATE)) {
 			mWaitingLinearLayout.removeAllViews();
 			mWaitingText.setText(R.string.serviceBindError);
 			mWaitingLinearLayout.addView(mWaitingText);
 		}
 
 		// Once the service is binded, we will load the list
 	}
 
	@Override
	protected void onStop() {
		super.onStop();
		// Unbind from the service
		if (mBound) {
			unbindService(mConnection);
			mBound = false;
		}
	}
 }
