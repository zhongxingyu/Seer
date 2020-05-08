 package com.monica.campuscloudandroid;
 
 import com.monica.campuscloudandroid.component.Network;
 import com.monica.campuscloudandroid.component.Settings;
 import com.monica.campuscloudandroid.component.WebInterface;
 
 import android.os.Bundle;
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.graphics.Bitmap;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.webkit.WebSettings;
 import android.webkit.WebView;
 import android.webkit.WebViewClient;
 import android.widget.Toast;
 
 @SuppressLint("SetJavaScriptEnabled")
 public class MainActivity extends Activity {
 	final class WebClient extends WebViewClient {
 
 		@Override
 		public boolean shouldOverrideUrlLoading(WebView view, String url) {
 			view.loadUrl(url);
 			return true;
 		}
 
 		@Override
 		public void onPageFinished(WebView view, String url) {
 			super.onPageFinished(view, url);
 			_processDialog.hide();
 		}
 
 		@Override
 		public void onPageStarted(WebView view, String url, Bitmap favicon) {
 			super.onPageStarted(view, url, favicon);
 			_processDialog.show();
 		}
 
 	}
 
 
 	final String HOME_URL = "/eyeos/";
 
 	WebView _webView = null;
 	WebClient _webClient = null;
 	WebInterface _webInterface = null;
 	Settings _settings = null;
 	ProgressDialog _processDialog = null;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		initialize();
 	}
 
 	@SuppressLint({ "HandlerLeak" })
 	private void initialize() {
 		this._settings = Settings.instance(this.getApplication());
 		this._webInterface = new WebInterface(this._settings);
 		this._webClient = new WebClient();
 		this._processDialog = new ProgressDialog(this);
 		this._processDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		this._processDialog.setMessage("...");
 
 		_webView = (WebView) findViewById(R.id.webView_content);
 
 		WebSettings settings = this._webView.getSettings();
 		settings.setJavaScriptEnabled(true);
 		settings.setSaveFormData(false);
 		settings.setSavePassword(false);
 		settings.setSupportZoom(false);
 		settings.setDomStorageEnabled(true);
 
 		this._webView.setWebViewClient(this._webClient);
 		this._webView.addJavascriptInterface(this._webInterface, "interface");
 
 		// ҳ
 		navigate_remote(HOME_URL);
 	}
 
 
 	private void navigate_remote(String url) {
 		String re_addr = _settings.get("address", "");
 		if (!re_addr.startsWith("http://")) {
 			re_addr = "http://" + re_addr;
 		}
 		final String address = re_addr;
 		final String abs_url = address + url;
 		if (Network.isNetworkAvailable(this)) {
 			_webView.loadUrl(abs_url);
 		} else {
			Toast.makeText(getApplicationContext(), "", Toast.LENGTH_SHORT)
 					.show();
 		}
 
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		CharSequence cs = item.getTitle();
 		String title = cs.toString();
 		Log.d("onOptionItemSelected", title);
		if (title == "˳") {
 			this.finish();
 		}
 		return true;
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(Menu.NONE, Menu.FIRST, 1, "˳").setIcon(
 				android.R.drawable.ic_menu_close_clear_cancel);
 		return true;
 	}
 }
