 package com.geoloqi.ui;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.net.Uri;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.WindowManager;
 import android.webkit.WebChromeClient;
 import android.webkit.WebView;
 import android.webkit.WebViewClient;
 import android.widget.ProgressBar;
 import android.widget.Toast;
 
 import com.geoloqi.interfaces.GeoloqiConstants;
 import com.geoloqi.interfaces.LoggingConstants;
 import com.geoloqi.interfaces.RPCException;
 import com.geoloqi.mapattack.R;
 import com.geoloqi.rpc.AccountMonitor;
 import com.geoloqi.rpc.MapAttackClient;
 import com.geoloqi.services.GPSTrackingService;
 import com.geoloqi.services.IOSocketService;
 import com.google.zxing.integration.android.IntentIntegrator;
 import com.google.zxing.integration.android.IntentResult;
 
 public class MapAttackActivity extends Activity implements GeoloqiConstants {
 	public static final String TAG = "MapAttackActivity";
 	public static final String PARAM_GAME_ID = "game_id";
 	public static final int SCAN_QR_CODE = 0;
 	private static final int DIALOG_QRCODE_SUCCESS = 0;
 	private static final int DIALOG_QRCODE_CANCEL = 1;
 	private static final String QRTAG = "QR_CODE_TAG";
 	public static final String PARAM_USER_ID = "user_id";
 
 	private String mGameId;
 	private String mGameUrl;
 	private WebView mWebView;
 	private Intent mPushNotificationIntent;
 	private String mQrCodeReturn = "";
 	private Intent mGPSIntent;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 		final Bundle extras = getIntent().getExtras();
 		if (extras != null) {
 			mGameId = getIntent().getExtras().getString(PARAM_GAME_ID);
 			Log.i("BBB", "mGameId is " + mGameId);
 		}
 
 		// Keep the screen lit while this Activity is visible
 		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
 
 		// Build game
 		mGameUrl = String.format(GAME_URL_BASE + mGameId);
 		Log.i("AAA", "Game id is: " + mGameId);
 		mWebView = (WebView) findViewById(R.id.webView);
 		mPushNotificationIntent = new Intent(this, IOSocketService.class);
 		mPushNotificationIntent.putExtra(GameListActivity.PARAM_GAME_ID,
 				mGameId);
 
 		mGPSIntent = new Intent(this, GPSTrackingService.class);
 
 		// Prepare the web view
 		mWebView.clearCache(false);
 		mWebView.setVerticalScrollBarEnabled(false);
 		mWebView.setHorizontalScrollBarEnabled(false);
 
 		mWebView.getSettings().setJavaScriptEnabled(true);
 		mWebView.setWebViewClient(mWebViewClient);
 		mWebView.setWebChromeClient(new WebChromeClient());
 
 		// Show the loading indicator
 		setLoading(true);
 	}
 
 	@Override
 	public void onStart() {
 		super.onStart();
 
 		final MapAttackClient client = MapAttackClient
 				.getApplicationClient(this);
 		// Check for a valid account token
 		if (!client.hasToken()) {
 			// Kick user out to the sign in activity
 			Log.i("AAA", "you dont have a token!");
 			Intent intent = new Intent(this, SignInActivity.class);
 			intent.putExtra(MapAttackActivity.PARAM_GAME_ID, mGameId);
 			startActivity(intent);
 			finish();
 		} else {
 			try {
 				// Stop any previously started services and broadcast receivers
 				unregisterReceiver(mPushReceiver);
 				stopService(mPushNotificationIntent);
 				stopService(mGPSIntent);
 			} catch (IllegalArgumentException e) {
 				Log.w(TAG, "Trying to unregister an inactive push receiver.");
 			}
 
 			try {
 				Log.i("AAA", "starting the MapAttackActivity");
 				// Join the game
 				client.joinGame(mGameId);
 
 				Log.d(LoggingConstants.RECORDING_TAG, "Joined game " + mGameId);
 
 				String userID = AccountMonitor.getUserID(this);
 				mPushNotificationIntent.putExtra(PARAM_USER_ID, userID);
 
 				Log.i("AAA", "joined the game");
 
 				// Start our services
 				registerReceiver(mPushReceiver, new IntentFilter("PUSH"));
 				startService(mPushNotificationIntent);
 				startService(mGPSIntent);
 
 				// Load the game into the WebView
 				String webUrl = String.format("%s?id=%s", mGameUrl, userID);
 				Log.i("AAA", webUrl);
 				mWebView.loadUrl(webUrl);
 				Log.i("AAA", "web view loaded");
 
 			} catch (RPCException e) {
 				Log.e(TAG, "Got an RPCException when trying to join the game!",
 						e);
 				Toast.makeText(this, R.string.error_join_game,
 						Toast.LENGTH_LONG).show();
 				finish();
 			}
 		}
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 		unregisterReceiver(mPushReceiver);
 		stopService(mPushNotificationIntent);
 		stopService(mGPSIntent);
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		registerReceiver(mPushReceiver, new IntentFilter("PUSH"));
 		startService(mPushNotificationIntent);
 		startService(mGPSIntent);
 	}
 
 	@Override
 	public void onStop() {
 		super.onStop();
 		try {
 			unregisterReceiver(mPushReceiver);
 			stopService(mPushNotificationIntent);
 			stopService(mGPSIntent);
 		} catch (IllegalArgumentException e) {
 			Log.w(TAG, "Trying to unregister an inactive push receiver.");
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.game_menu, menu);
 		return super.onCreateOptionsMenu(menu);
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.share:
 			Intent shareIntent = new Intent(Intent.ACTION_SEND);
 			shareIntent.setType("text/plain");
 			shareIntent.putExtra(Intent.EXTRA_TEXT,
 					String.format("Map Attack! %s #mapattack", mGameUrl));
 			startActivity(Intent.createChooser(shareIntent, "Share this map: "));
 			return true;
 		case R.id.quit:
 			finish();
 			return true;
 		case R.id.qrscan:
 			Intent intent = new Intent("com.google.zxing.client.android.SCAN");
			intent.putExtra("SCAN_MODE", "PRODUCT_MODE");
 			intent.putExtra("SCAN_FORMATS", "QR_CODE");
 
			intent.putExtra("SCAN_WIDTH", 800);
			intent.putExtra("SCAN_HEIGHT", 200);
			intent.putExtra("RESULT_DISPLAY_DURATION_MS", 3000L);
			intent.putExtra("PROMPT_MESSAGE", "Custom prompt to scan a product");
 
 			startActivityForResult(intent, IntentIntegrator.REQUEST_CODE);
 			return true;
 		}
 		return false;
 	}
 
 	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
 		IntentResult scanResult = IntentIntegrator.parseActivityResult(
 				requestCode, resultCode, intent);
 		if (scanResult != null) {
 			if (requestCode == IntentIntegrator.REQUEST_CODE) {
 				if (resultCode == RESULT_OK) {
 					mQrCodeReturn = intent.getStringExtra("SCAN_RESULT");
 					// showDialog(DIALOG_QRCODE_SUCCESS);
 					Uri uri = Uri.parse(mQrCodeReturn);
 					startActivity(new Intent(Intent.ACTION_VIEW, uri));
 				} else if (resultCode == RESULT_CANCELED) {
 					showDialog(DIALOG_QRCODE_CANCEL);
 				}
 			}
 		} else {
 			// else continue with any other code you need in the method
 			Log.i(QRTAG, "scan result was null");
 		}
 	}
 
 	@Override
 	protected Dialog onCreateDialog(int id) {
 		Dialog dialog = null;
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		switch (id) {
 		case DIALOG_QRCODE_SUCCESS:
 			builder.setMessage("The QR Code says: " + mQrCodeReturn)
 					.setPositiveButton("OK",
 							new DialogInterface.OnClickListener() {
 								public void onClick(DialogInterface dialog,
 										int id) {
 									;
 								}
 							});
 
 			dialog = builder.create();
 			break;
 		case DIALOG_QRCODE_CANCEL:
 			builder.setMessage(
 					"You cancelled the QR Code scan. Your results"
 							+ "cannot be saved. Please try again.")
 					.setPositiveButton("OK",
 							new DialogInterface.OnClickListener() {
 								public void onClick(DialogInterface dialog,
 										int id) {
 									;
 								}
 							});
 		}
 		return dialog;
 	}
 
 	/** Show or hide the loading indicator. */
 	private void setLoading(boolean loading) {
 		ProgressBar spinner = (ProgressBar) findViewById(R.id.loading);
 
 		if (loading) {
 			spinner.setVisibility(View.VISIBLE);
 			mWebView.setVisibility(View.GONE);
 		} else {
 			spinner.setVisibility(View.GONE);
 			mWebView.setVisibility(View.VISIBLE);
 		}
 	}
 
 	/** A reference to the WebViewClient that hosts the MapAttack game. */
 	private WebViewClient mWebViewClient = new WebViewClient() {
 		@Override
 		public boolean shouldOverrideUrlLoading(WebView view, String url) {
 			view.loadUrl(url);
 			return true;
 		}
 
 		@Override
 		public void onPageFinished(WebView view, String url) {
 			super.onPageFinished(view, url);
 
 			// Make WebView visible and hide loading indicator
 			setLoading(false);
 		}
 	};
 
 	/** The broadcast receiver used to push game data to the server. */
 	private BroadcastReceiver mPushReceiver = new BroadcastReceiver() {
 		@Override
 		public void onReceive(Context ctxt, Intent intent) {
 			Log.i("Testing IO", "Received JSON: "
 					+ intent.getExtras().getString("json"));
 
 			mWebView.loadUrl(String.format("javascript:handleSocketData(%s)",
 					intent.getExtras().getString("json")));
 
 		}
 	};
 }
