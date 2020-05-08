 package edu.ucsb.cs290.touch.to.chat;
 
 import android.app.Activity;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.util.Log;
 import edu.ucsb.cs290.touch.to.chat.KeyManagementService.KeyCachingBinder;
 import edu.ucsb.cs290.touch.to.chat.crypto.DatabaseHelper;
 
 public abstract class KeyActivity extends Activity {
 	KeyManagementService mService;
 	boolean mBound = false;
 	private String password;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		Intent intent = new Intent(this, KeyManagementService.class);
 		if (null == startService(intent)) {
 			startActivityForResult(new Intent(getApplicationContext(),
 					AuthActivity.class), 100);
 		}
 
 		bindService(intent, mConnection, Context.BIND_IMPORTANT);
 	}
 
 	@Override
 	protected void onStart() {
 		super.onStart();
 		// Bind to LocalService
 		Intent intent = new Intent(this, KeyManagementService.class);
 		bindService(intent, mConnection, Context.BIND_IMPORTANT);
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
 
 	/** Defines callbacks for service binding, passed to bindService() */
 	final KeyActivity k = this;
 	private final ServiceConnection mConnection = new ServiceConnection() {
 
 		@Override
 		public void onServiceConnected(ComponentName className, IBinder service) {
 			Log.i("kmg", "Service connected");
 			// We've bound to LocalService, cast the IBinder and get
 			// LocalService instance
 			KeyCachingBinder binder = (KeyCachingBinder) service;
 			mService = binder.getService();
 			mBound = true;
 			if (password != null) {
 				mService.getInstance().initalizeInstance(password);
				password = null;
				Log.i("kmg", "initializing db on service connected");
 				mService.startNotification();
 			} else if (!mService.getInstance().initialized()) {
 				startActivityForResult(new Intent(getApplicationContext(),
 						AuthActivity.class), 100);
				return;
 			}
			k.onServiceConnected();

 		}
 
 		@Override
 		public void onServiceDisconnected(ComponentName arg0) {
 			mBound = false;
 		}
 	};
 
 	public DatabaseHelper getInstance() {
 		return mService.getInstance();
 	}
 
 	public void onServiceConnected() {
 		// override me!
 	}
 
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		switch (requestCode) {
 		case 100:
 			if (resultCode == Activity.RESULT_CANCELED) {
 				return;
 			}
 			if (mBound) {
 				// Set password, initialize db, and generate keypair of doesn't
 				// exist.
 				Log.i("kmg", "Initializing instance on activity result");
 				String derp = data.getExtras().getString(
 						"edu.ucsb.cs290.touch.to.chat.password");
 				mService.getInstance().initalizeInstance(derp);
 				mService.startNotification();
 				k.onServiceConnected();
 			} else {
 				Log.i("kmg", "Storing password until service starts");
 				password = data.getExtras().getString(
 						"edu.ucsb.cs290.touch.to.chat.password");
 			}
 		}
 	}
 }
