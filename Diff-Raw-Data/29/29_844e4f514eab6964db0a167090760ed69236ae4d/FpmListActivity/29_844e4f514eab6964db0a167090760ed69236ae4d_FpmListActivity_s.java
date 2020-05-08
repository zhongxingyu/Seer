 package org.braiden.fpm2;
 
 /*
  * Copyright (c) 2010 Braiden Kindt
  *
  * Permission is hereby granted, free of charge, to any person
  * obtaining a copy of this software and associated documentation
  * files (the "Software"), to deal in the Software without
  * restriction, including without limitation the rights to use,
  * copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the
  * Software is furnished to do so, subject to the following
  * conditions:
  *
  * The above copyright notice and this permission notice shall be
  * included in all copies or substantial portions of the Software.
  *
  *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
  *  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
  *  OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
  *  NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
  *  HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
  *  WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
  *  FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
  *  OTHER DEALINGS IN THE SOFTWARE.
  *
  */
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.IntentService;
 import android.app.ListActivity;
 import android.app.ProgressDialog;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.widget.Adapter;
 import android.widget.BaseAdapter;
 import android.widget.EditText;
 
 /**
  * Base class for activities which want to access FpmCrypt.
  * Takes care of ensuring the Fpm database is unlocked when
  * the view is focussed. Will notify list adapter of data
  * changes, display password prompt and dialogs.
  *  
  * @author braiden
  *
  */
 public class FpmListActivity extends ListActivity {
 	
 	private static final int FPM_PASSPHRASE_CANCEL = 0xFFFFFF00;
 	
 	private LayoutInflater layoutInflater;
 	private FpmBroadcastReceiver broadcastReceiver;
 	private AlertDialog passphraseDialog = null;
 	private ProgressDialog progressDialog = null;
 	
 	public FpmApplication getFpmApplication() {
 		return (FpmApplication) getApplication();
 	}
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		layoutInflater = LayoutInflater.from(this);
 		broadcastReceiver = new FpmBroadcastReceiver(this);
 		registerReceiver(broadcastReceiver, FpmBroadcastReceiver.createIntentFilter());
 	}
 
 	@Override
 	protected void onDestroy() {
 		super.onDestroy();
 		unregisterReceiver(broadcastReceiver);
 	}
 	
 	@Override
 	public void onWindowFocusChanged(boolean hasFocus) {
 		super.onWindowFocusChanged(hasFocus);
 		if (hasFocus && !getFpmApplication().isCryptOpen()) {
 			// initiate attempt to lock db
 			onFpmLock();
 		}
 	}
 
 	protected void onFpmError(int msg) {
 		dismissDialogs();
 		
 		if (msg != 0 && msg != R.string.exception_fpm_passphrase) {
			new AlertDialog.Builder(this)
 				.setTitle(msg)
 				.setIcon(android.R.drawable.ic_dialog_alert)
 				.setCancelable(false)
 				.setPositiveButton(R.string.passphrase_dialog_ok, new DialogInterface.OnClickListener() {
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 						dialog.dismiss();
 						onFpmLock();
 					}
				})
				.create()
				.show();
 		} else {
 			onFpmLock();
 		}
 	}
 	
 	/**
 	 * Callback when the database is unlocked an availible.
 	 * sub-classes can override this method but, should
 	 * call super.onFpmUnlock()
 	 */
 	protected void onFpmUnlock() {
 		// once datastore is unlocked
 		// clear any passphrase related dialogs
 		// and notify listView of data changes
 		dismissDialogs();
 		notifyDataSetChanged();
 	}
 	
 	/**
 	 * Callback when datadase is locked.
 	 * sub-classes who override this method should
 	 * take care to call super.onFpmLock()
 	 * if they want user to be prompted for passphrase.
 	 */
 	protected void onFpmLock() {
 		// fpm db lock event is recevied
 		notifyDataSetChanged();
 
 		synchronized (getFpmApplication()) {
 			if (getFpmApplication().getCryptState() != FpmApplication.STATE_BUSY) {
 				createPassphraseDialog().show();
 			} else {
 				createProgressDialog().show();
 			}
 		}
 		
 	}
 	
 	private void dismissDialogs() {
 		// once the password crypt is unlocked dismiss
 		// any dialogs the might exist.
 		
 		if (passphraseDialog != null) {
 			passphraseDialog.dismiss();
 		}
 		
 		if (progressDialog != null) {
 			progressDialog.dismiss();
 			progressDialog = null;
 		}
 	}
 	
 	private void notifyDataSetChanged() {
 		// notify the adapter of datachange (if supported)
 		Adapter adapter = getListAdapter();
 		if (adapter instanceof BaseAdapter) {
 			((BaseAdapter) adapter).notifyDataSetChanged();
 		}
 	}
 
 	/**
 	 * Callback once the user has entered a passphrase.
 	 * @param passphrase
 	 */
 	protected void onFpmPassphraseOk(String passphrase) {
 		createProgressDialog().show();
 		Intent serviceIntent = new Intent(this, FpmUnlockService.class);
 		serviceIntent.putExtra(FpmUnlockService.EXTRA_PASSPHRASE, passphrase);
 		startService(serviceIntent);
 	}
 		
 	/**
 	 * Callback if user clicks cacnel in passphrase prompt.
 	 */
 	protected void onFpmPassphraseCancel() {
 		setResult(FPM_PASSPHRASE_CANCEL);
 		finish();
 	}
 	
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		super.onActivityResult(requestCode, resultCode, data);
 		// if a child activity's passphrase dialog was canceled
 		// it cascaded to close entire stack. don't want to prompt
 		// again at each parent.
 		if (resultCode == FPM_PASSPHRASE_CANCEL) {
 			setResult(FPM_PASSPHRASE_CANCEL);
 			finish();
 		}
 	}
 
 	/**
 	 * Create progress dialog for "Checking Passphrase..."
 	 * @return
 	 */
 	protected Dialog createProgressDialog() {
 		if (progressDialog == null) {
 			progressDialog = new ProgressDialog(this);
 			progressDialog.setMessage(getResources().getString(R.string.checking_passphrase));
 			progressDialog.setCancelable(false);
 			progressDialog.setIndeterminate(true);
 		}
 		return progressDialog;
 	}
 	
 	/**
 	 * Create dialog for passphrase prompt.
 	 * @return
 	 */
 	protected Dialog createPassphraseDialog() {
 		if (passphraseDialog == null ) {		
 			View textEntryView = layoutInflater.inflate(R.layout.passphrase_dialog, null);
 			final EditText editText = (EditText) textEntryView.findViewById(R.id.password_edit);
 			passphraseDialog =  new AlertDialog.Builder(this)
         		.setTitle(R.string.passphrase_dialog_title)
         		.setView(textEntryView)
         		.setCancelable(true)
         		.setPositiveButton(R.string.passphrase_dialog_ok, new DialogInterface.OnClickListener() {
         			@Override
         			public void onClick(DialogInterface dialog, int which) {
         				dialog.dismiss();
         				String passphrase = editText.getText().toString();
         				editText.setText("");
         				onFpmPassphraseOk(passphrase);
         			}
         		})
         		.setNegativeButton(R.string.passphrase_dialog_cancel, new DialogInterface.OnClickListener() {
         			@Override
         			public void onClick(DialogInterface dialog, int which) {
         				dialog.dismiss();
         				onFpmPassphraseCancel();
         			}
         		})
         		.setOnCancelListener(new DialogInterface.OnCancelListener() {
 					@Override
 					public void onCancel(DialogInterface dialog) {
 						dialog.dismiss();
 						onFpmPassphraseCancel();
 					}
 				})
 				.create();
 		}
 		return passphraseDialog;
 	}
 	
 	/**
 	 * A service which unlocked the FPM data store.
 	 * This can take a while on slower devices.
 	 * Key generations takes as much as 5 seconds on
 	 * my droid 1.
 	 * 
 	 * @author braiden
 	 *
 	 */
 	public static class FpmUnlockService extends IntentService {
 		
 		public static final String EXTRA_PASSPHRASE = "passphrase";
 		
 		public FpmUnlockService() {
 			super("FpmUnlockService");
 		}
 		
 		@Override
 		protected void onHandleIntent(Intent intent) {
 			FpmApplication app = (FpmApplication) getApplication();
 			String passphrase = intent.getStringExtra(EXTRA_PASSPHRASE);
 			app.openCrypt(passphrase);
 		}		
 		
 	}
 	
 	/**
 	 * A broadcast receiver which calls our onLock, onUnlock methods
 	 * when fpm broadcasts are received. 
 	 * 
 	 * @author braiden
 	 *
 	 */
 	public static class FpmBroadcastReceiver extends BroadcastReceiver {
 
 		private FpmListActivity activity;
 		
 		public FpmBroadcastReceiver(FpmListActivity activity) {
 			this.activity = activity;
 		}
 		
 		public static IntentFilter createIntentFilter() {
 			IntentFilter filter = new IntentFilter();
 			filter.addAction(FpmApplication.ACTION_FPM_LOCK);
 			filter.addAction(FpmApplication.ACTION_FPM_UNLOCK);
 			filter.addAction(FpmApplication.ACTION_FPM_FAIL);
 			return filter;
 		}
 		
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			if (FpmApplication.ACTION_FPM_UNLOCK.equals(intent.getAction())) {
 				activity.onFpmUnlock();
 			} else if (FpmApplication.ACTION_FPM_LOCK.equals(intent.getAction())) {
 				activity.onFpmLock();
 			} else if (FpmApplication.ACTION_FPM_FAIL.equals(intent.getAction())) {
 				activity.onFpmError(intent.getIntExtra(FpmApplication.EXTRA_MSG, 0));
 			}
 		}
 		
 	}
 	
 }
