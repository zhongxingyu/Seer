 package com.sl.mimc;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.accounts.Account;
 import android.accounts.AccountManager;
 import android.accounts.OnAccountsUpdateListener;
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.provider.Settings;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnTouchListener;
 import android.widget.TextView;
 
 public class MainActivity extends Activity implements OnTouchListener, DialogInterface.OnCancelListener, OnAccountsUpdateListener {
 
	final static int version = 11;
 	
 	final String accountType = "com.sl.mimc.account";
 	final String[] authority = {"com.sl.mimc.content"};
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 
 		findViewById(R.id.latest).setOnTouchListener(this);
 		findViewById(R.id.contact).setOnTouchListener(this);
 		findViewById(R.id.archive).setOnTouchListener(this);
 		findViewById(R.id.categories).setOnTouchListener(this);
 		
 		if (getIntent().hasExtra("latest")) {
 			Bundle extras = getIntent().getExtras();
 		
 			boolean launchLatest = extras.getBoolean("latest");
 			if (launchLatest) {
 				Intent intent = new Intent(this, EntryActivity.class);
 				intent.putExtra("latest", true);
 				startActivity(intent);
 			}
 		}
 	}
 	
 	public void onDestroy() {
 		super.onDestroy();
 		
 		try {
 			AccountManager accountManager = AccountManager.get(this);
 			accountManager.removeOnAccountsUpdatedListener(this);
 		} catch (IllegalStateException e) {
 			//not added, never mind
 		}
 	}
 
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.layout.mainmenu, menu);
 		return true;
 	}
 
 	public boolean onOptionsItemSelected(MenuItem item) {
 		// Handle item selection
 		switch (item.getItemId()) {
 		case R.id.update:
 		{
 			update();
 			return true;
 		}
 		case R.id.sync:
 		{
 			AccountManager accountManager = AccountManager.get(this);
 			Account[] accounts = accountManager.getAccountsByType(accountType);
 
 			if (accounts.length == 0) {
 				
 				try {
 					accountManager.addOnAccountsUpdatedListener(this, null, true);
 				}
 				catch (IllegalStateException e) {
 					//already added, do nothing
 				}
 				
 				Intent intent = new Intent(Settings.ACTION_ADD_ACCOUNT);
 				intent.putExtra(Settings.EXTRA_AUTHORITIES, authority);
 				startActivity(intent);
 			} else {
 				showSyncSettings();
 			}
 			
 			return true;
 		}
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 	
 	public void onAccountsUpdated(Account[] accounts){
 		
 		for (int i = 0; i < accounts.length; ++i) {
 			if (accountType.compareTo(accounts[i].type) == 0) {
 				AccountManager accountManager = AccountManager.get(this);
 				accountManager.removeOnAccountsUpdatedListener(this);
 				
 				showSyncSettings();
 				return;
 			}
 		}
 	}
 	
 	private void showSyncSettings() {
 		
 		Intent intent = new Intent(Settings.ACTION_SYNC_SETTINGS);
 		intent.putExtra(Settings.EXTRA_AUTHORITIES, authority);
 		startActivity(intent);
 	}
 
 	public void onClick(View v) {
 		final TextView view = (TextView) v;
 		if (view != null) {
 			String text = (String) view.getText();
 			Intent intent = null;
 			if (text == getText(R.string.latest)) {
 
 				intent = new Intent(this, EntryActivity.class);
 				intent.putExtra("latest", true);
 
 			} else if (text == getText(R.string.categories)) {
 
 				intent = new Intent(this, CategoriesActivity.class);
 
 			} else if (text == getText(R.string.archive)) {
 
 				intent = new Intent(this, ArchiveActivity.class);
 
 			} else if (text == getText(R.string.contact)) {
 
 				intent = new Intent(android.content.Intent.ACTION_SEND);
 				intent.setType("plain/text");
 				intent.putExtra(android.content.Intent.EXTRA_EMAIL,
 						new String[] { "sleuthold@beuth-hochschule.de" });
 				intent.putExtra(android.content.Intent.EXTRA_SUBJECT,
 						getText(R.string.appContact));
 
 				intent = Intent.createChooser(intent,
 						getText(R.string.sendMail));
 			}
 			startActivity(intent);
 		}
 	}
 
 	private int getActiveImage(String text) {
 		if (text == getText(R.string.latest)) {
 			return R.drawable.latest_active;
 		} else if (text == getText(R.string.categories)) {
 			return R.drawable.categories_active;
 		} else if (text == getText(R.string.archive)) {
 			return R.drawable.archive_active;
 		} else if (text == getText(R.string.contact)) {
 			return R.drawable.contact_active;
 		}
 		return 0;
 	}
 
 	public boolean onTouch(View v, MotionEvent event) {
 
 		if (!(v instanceof TextView)) {
 			return false;
 		}
 
 		final TextView view = (TextView) v;
 
 		String text = (String) view.getText();
 		int image = getActiveImage(text);
 
 		switch (event.getAction()) {
 		case MotionEvent.ACTION_DOWN:
 			view.setCompoundDrawablesWithIntrinsicBounds(0, image, 0, 0);
 			return false;
 		case MotionEvent.ACTION_MOVE:
 			int[] location = new int[2];
 			view.getLocationOnScreen(location);
 			int px = (int)event.getRawX();
 			int py = (int)event.getRawY();
 			if (location[1] > py ||
 					location[0] > px ||
 					location[1] + view.getHeight() < py ||
 					location[0] + view.getWidth() < px) {
 				view.setCompoundDrawablesWithIntrinsicBounds(0, image - 1, 0, 0);
 			}
 			return false;
 		case MotionEvent.ACTION_CANCEL:
 		case MotionEvent.ACTION_UP:
 			view.setCompoundDrawablesWithIntrinsicBounds(0, image - 1, 0, 0);
 			return false;
 		}
 		return false;
 	}
 
 
 	final Handler handler = new Handler() {
 		public void handleMessage(Message msg) {
 
 			if (progressDialog.isShowing()) {
 				progressDialog.dismiss();
 
 				if (versionrequest == -1) {
 					ErrorNotification.noConnection(MainActivity.this);
 				} else if (versionrequest == -3) {
 					ErrorNotification.updateFailure(MainActivity.this);
 				} else if (versionrequest == -4) {
 					ErrorNotification.noupdate(MainActivity.this);
 				}
 			}
 		}
 	};
 	private void update() {
 
 		versionrequest = -1;
 		
 		progressDialog = ProgressDialog.show(this,
 				getText(R.string.progressTitle), getText(R.string.checkUpdate), true, true, this);
 
 		requestThread = new Thread(new Runnable() {
 			public void run() {
 
 				NBAPIResponse nbapi = new NBAPIResponse();
 				String response = nbapi.getText("https://cgi.beuth-hochschule.de/~sleuthold/nb_api/nb_api.cgi?q=version");
 
 				JSONArray json = null;
 				try {
 					if (response != null)
 						json = new JSONArray(response);
 				} catch (JSONException e) {
 					e.printStackTrace();
 				}
 
 				if (json != null) {
 					try {
 							
 						JSONObject obj = json.getJSONObject(0);
 						versionrequest = obj.getInt("version");
 						
 					} catch (JSONException e) {
 						e.printStackTrace();
 					}
 				}
 				
 				if (versionrequest > version)
 				{
 					Intent downloadIntent = new Intent(Intent.ACTION_VIEW, Uri.parse( "http://public.beuth-hochschule.de/%7Esleuthold/files/android/MiMC.apk"));
 					startActivity(downloadIntent);
 				} else if (versionrequest != -1) {
 					versionrequest = -4;
 				}
 
 				handler.sendEmptyMessage(0);
 			}
 		});
 		requestThread.start();
 
 	}
 
 	public void onCancel(DialogInterface dialog) {
 		if (requestThread != null &&
 				requestThread.isAlive())
 			requestThread.interrupt();
 		versionrequest = -2;
 		handler.sendEmptyMessage(0);
 	}
 	
 	private int versionrequest;
 	private Thread requestThread = null;
 	private ProgressDialog progressDialog;
 }
