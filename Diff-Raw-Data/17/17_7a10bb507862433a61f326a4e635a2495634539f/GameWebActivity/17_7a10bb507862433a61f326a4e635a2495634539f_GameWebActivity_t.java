 package net.qlun.ab1;
 
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 
 import net.qlun.ab1.js.JavascriptInterface;
 import net.qlun.ab1.util.Installation;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.webkit.ConsoleMessage;
 import android.webkit.JsResult;
 import android.webkit.WebChromeClient;
 import android.webkit.WebSettings;
 import android.webkit.WebView;
 import android.webkit.WebViewClient;
 import android.widget.Toast;
 
 public class GameWebActivity extends Activity {
 
	private static final String TAG = "ab1";

 	@Override
 	public void onCreate(Bundle icicle) {
 		super.onCreate(icicle);
 		setContentView(R.layout.localweb);
 
 		WebView webView = (WebView) findViewById(R.id.webView1);
 		webView.setScrollBarStyle(WebView.SCROLLBARS_INSIDE_OVERLAY);
 
 		WebSettings webSettings = webView.getSettings();
 		webSettings.setJavaScriptEnabled(true);
 		webSettings.setDomStorageEnabled(true);
 		webSettings.setDefaultTextEncodingName("UTF-8");
 		webSettings.setAppCacheEnabled(true);
 		webSettings.setAppCacheMaxSize(20971520); // 20M
 		webSettings.setAppCachePath(this.getExternalCacheDir().toString());
 
 		webView.setWebChromeClient(new WebChromeClient() {
 			@Override
 			public boolean onJsAlert(WebView view, String url, String message,
 					JsResult result) {
 				Log.d("ab1js", "alert - " + message);
 
 				AlertDialog.Builder builder = new AlertDialog.Builder(
 						GameWebActivity.this);
 
 				builder.setTitle(R.string.jsTitle);
 				builder.setMessage(message);
 				builder.setCancelable(true);
 
 				builder.setPositiveButton("OK",
 						new DialogInterface.OnClickListener() {
 							public void onClick(DialogInterface dialog, int id) {
 								dialog.cancel();
 							}
 						});
 
 				builder.show();
 
 				result.confirm();
 				return true;
 			}
 
 			@Override
 			public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
 				Log.v("ab1js",
 						"console - " + consoleMessage.sourceId() + ":"
 								+ consoleMessage.lineNumber() + " - "
 								+ consoleMessage.message());
 
 				super.onConsoleMessage(consoleMessage);
 
 				return true;
 			}
 
 		});
 
 		webView.setWebViewClient(new WebViewClient() {
 
 			@Override
 			public void onReceivedError(WebView view, int errorCode,
 					String description, String failingUrl) {
 				Toast.makeText(GameWebActivity.this, "Oh no! " + description,
 						Toast.LENGTH_SHORT).show();
 			}
 
 			@Override
 			public boolean shouldOverrideUrlLoading(WebView view, String url) {
 				Log.v("ab1", "check if override url: " + url);
 				return false;
 			}
 		});
 
 		webView.addJavascriptInterface(new JavascriptInterface(this, webView),
 				"Andspil");
 
 		SharedPreferences sp = PreferenceManager
 				.getDefaultSharedPreferences(this);
 
 		String indexUrl = sp.getString("indexUrl", null);
 
 		if (indexUrl != null) {
 			String accessToken = getAccessToken();
 			// webView.loadUrl(indexUrl);
 			String postData = "";
 			String id = Installation.id(this);
 			try {
 				postData = "vid=1&iid=" + URLEncoder.encode(id, "UTF-8")
 						+ "&access_token="
 						+ URLEncoder.encode(accessToken, "UTF-8");
 			} catch (UnsupportedEncodingException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			webView.postUrl(indexUrl, postData.getBytes());
 		} else {
 			webView.loadData("Error: null indexUrl", "text/plain", "utf-8");
 		}
 
 	}
 
 	private String getAccessToken() {
		String accessToken = getIntent().getStringExtra("AccessToken");
		if (accessToken == null) {
			accessToken = "";
 		}
 		return accessToken;
 	}
 
 }
