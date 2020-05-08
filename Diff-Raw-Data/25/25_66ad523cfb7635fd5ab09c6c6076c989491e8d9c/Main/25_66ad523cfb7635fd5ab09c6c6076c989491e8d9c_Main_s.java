 package com.alphabet7.deliveryypt.activity;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.PendingIntent;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.pm.PackageInfo;
 import android.content.pm.PackageManager;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.net.Uri;
 import android.os.Bundle;
 import android.telephony.SmsManager;
 import android.text.Html;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.Window;
 import android.webkit.WebSettings;
 import android.webkit.WebView;
 import android.webkit.WebViewClient;
 import android.widget.EditText;
 import android.widget.ImageButton;
 import android.widget.Toast;
 
 import com.alphabet7.deliveryypt.R;
 
 public class Main extends Activity {
 
 	public static final String TAG = Main.class.getCanonicalName();
 
 	public static final String ACTION_SMS_SENT = "com.example.android.apis.os.SMS_SENT_ACTION";
 
 	private static final String HOST = "deliveryypt.com";
 	private static final String URL = "http://" + HOST;
 
 	private BroadcastReceiver receiver;
 
 	private WebView wvSite;
 	private EditText etSms, etRecipient;
 	private ImageButton ibSend;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		setContentView(R.layout.activity_main);
 
 		wvSite = (WebView) findViewById(R.id.wv_deliverySite);
 		etRecipient = (EditText) findViewById(R.id.et_recipient);
 		etSms = (EditText) findViewById(R.id.et_sms);
 		ibSend = (ImageButton) findViewById(R.id.ib_send);
 
 		ibSend.setOnClickListener(btnListener);
 
 		WebSettings ws = wvSite.getSettings();
 		ws.setJavaScriptEnabled(true);
 		ws.setUserAgentString("Mobile");
 
 		wvSite.setWebViewClient(new DeliveryYptWebClient());
 		wvSite.loadUrl(URL);
 
 		receiver = new BroadcastReceiver() {
 
 			@Override
 			public void onReceive(Context context, Intent intent) {
 				ibSend.setEnabled(true);
 
 				String message = "";
 				switch (getResultCode()) {
 				case Activity.RESULT_OK:
 					message = "Pesanan terkirim";
 					etSms.setText("");
 					etRecipient.setText("");
 					break;
 				case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
 					message = "Maaf, pesan gagal terkirim";
 					break;
 				case SmsManager.RESULT_ERROR_NO_SERVICE:
 					message = "Tidak ada jaringan";
 					break;
 				case SmsManager.RESULT_ERROR_NULL_PDU:
 					message = "Maaf, pesan gagal terkirim";
 					break;
 				case SmsManager.RESULT_ERROR_RADIO_OFF:
 					message = "Maaf, pesan gagal terkirim";
 					break;
 				}
 
 				Toast.makeText(Main.this, message, Toast.LENGTH_LONG).show();
 			}
 		};
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		registerReceiver(receiver, new IntentFilter(ACTION_SMS_SENT));
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 		unregisterReceiver(receiver);
 	}
 
 	private View.OnClickListener btnListener = new View.OnClickListener() {
 
 		@Override
 		public void onClick(View v) {
 			switch (v.getId()) {
 			case R.id.ib_send:
				ibSend.setEnabled(false);
				Toast.makeText(Main.this, "Mengirim...", Toast.LENGTH_SHORT)
						.show();

				SmsManager sms = SmsManager.getDefault();

 				String recipient = etRecipient.getText().toString();
 				String message = etSms.getText().toString();
				message += "\n\nDeliveryYPT for Android\nhttp://bit.ly/deliveryYPT";
 
				sms.sendTextMessage(recipient, null, message, PendingIntent
						.getBroadcast(Main.this, 0,
								new Intent(ACTION_SMS_SENT), 0), null);
 				break;
 			}
 		}
 
 	};
 
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 		// Check if the key event was the Back button and if there's history
 		if ((keyCode == KeyEvent.KEYCODE_BACK) && wvSite.canGoBack()) {
 			wvSite.goBack();
 			return true;
 		}
 		// If it wasn't the Back key or there's no web page history, bubble up
 		// to the default
 		// system behavior (probably exit the activity)
 		return super.onKeyDown(keyCode, event);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.action_about:
 			showAboutDialog();
 			return true;
 		case R.id.action_otherApps:
 			Intent intent = new Intent(Intent.ACTION_VIEW);
 			intent.setData(Uri.parse("market://search?q=pub:Gentra+Aditya"));
 			startActivity(intent);
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	private void showAboutDialog() {
 		String appVer = "";
 
 		PackageManager manager = getPackageManager();
 		PackageInfo info;
 		try {
 			info = manager.getPackageInfo(getPackageName(), 0);
 			appVer = info.versionName;
 		} catch (NameNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setTitle("DeliveryYPT for Android " + appVer)
 				.setMessage(
 						Html.fromHtml(getResources().getString(
 								R.string.about_dialog)))
 				.setPositiveButton("OK", null);
 
 		builder.show();
 	}
 
 	private class DeliveryYptWebClient extends WebViewClient {
 		@Override
 		public boolean shouldOverrideUrlLoading(WebView view, String url) {
 			if (Uri.parse(url).getHost().equals("deliveryypt.com")) {
 				// This is my web site, so do not override; let my WebView load
 				// the page
 				return false;
 			}
 			// Otherwise, the link is not for a page on my site, so launch
 			// another Activity that handles URLs
 			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
 			startActivity(intent);
 			return true;
 		}
 	}
 
 }
