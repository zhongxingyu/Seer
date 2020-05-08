 package de.h3ndrik.openlocation;
 
 import de.h3ndrik.openlocation.util.Utils;
 import android.net.Uri;
 import android.os.Bundle;
 import android.app.Activity;
 import android.app.AlarmManager;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.Intent;
 import android.location.LocationManager;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.Window;
 import android.webkit.HttpAuthHandler;
 import android.webkit.WebChromeClient;
 import android.webkit.WebView;
 import android.webkit.WebViewClient;
 import android.webkit.GeolocationPermissions;
 import android.widget.Toast;
 
 public class OpenLocationMainActivity extends Activity {
 	private static final String DEBUG_TAG = "MainActivity"; // for logging
 															// purposes
 
 	LocationManager locationManager;
 	AlarmManager alarmManager;
 	PendingIntent pendingIntent;
 	private static WebView webview;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		getWindow().requestFeature(Window.FEATURE_PROGRESS);
 		// setContentView(R.layout.activity_open_location_main);
 
 		Utils.startReceiver(getBaseContext(), true);
 
 		// WebView
 		webview = new WebView(this);
 		Log.d("WebView", "new WebView");
 
 		webview.getSettings().setJavaScriptEnabled(true);
 		Log.d("WebView", "JavaScriptEnabled");
 
 		final Activity activity = this;
 
 		webview.setWebViewClient(new WebViewClient() {
 			public void onReceivedError(WebView view, int errorCode,
 					String description, String failingUrl) {
 				Toast.makeText(activity, "Oh no! " + description,
 						Toast.LENGTH_SHORT).show();
 			}
 
 			public void onReceivedHttpAuthRequest(WebView view,
 					HttpAuthHandler handler, String host, String realm) {
 				Log.d("WebView", "ReceivedHttpAuthRequest host: " + host
 						+ ", realm: " + realm);
 
 				// WebViewDatabase.getInstance(this).clearHttpAuthUsernamePassword();
 
 				if (Utils.getUsername(activity) == null) {
 					Toast.makeText(
 							OpenLocationMainActivity.this.getBaseContext(),
 							getResources().getString(
 									R.string.msg_usernotconfigured),
 							Toast.LENGTH_SHORT).show();
 					handler.cancel();
 					return;
 				}
 
 				// webview.setHttpAuthUsernamePassword(domain+":80",
 				// "OpenLocation", username, password);
 				Log.d("WebView", "found Username/Password");
 
 				if (!handler.useHttpAuthUsernamePassword()) {	// can't use user credentials on record (ie, we did fail trying to use them last time)
 					Toast.makeText(activity, getResources().getString(R.string.msg_wrongcredentials), Toast.LENGTH_SHORT).show();
 					handler.cancel();
 					return;
 				}
 
 				if (host.startsWith(Utils.getDomain(activity)) && realm.equals("OpenLocation")) {
 					handler.proceed(Utils.getFullUsername(activity), Utils.getPassword(activity));
 				} else {
 					Log.d(DEBUG_TAG,
 					"WebView: unknown site, canceling auth");
 					handler.cancel();
 				}
 
 			}
 
 			@Override
 			public boolean shouldOverrideUrlLoading(WebView view, String url) {
 				Log.d(DEBUG_TAG, "WebView: loading " + url);
 				if (Uri.parse(url).getHost().equals(Utils.getDomain(activity))) {
 					return false;
 				} else {
 					Intent intent = new Intent(Intent.ACTION_VIEW, Uri
 							.parse(url));
 					startActivity(intent);
 					return true;
 				}
 			}
 		});
 
 		webview.setWebChromeClient(new WebChromeClient() {
 			public void onProgressChanged(WebView view, int progress) {
 				// Activities and WebViews measure progress with different
 				// scales.
 				// The progress meter will automatically disappear when we reach
 				// 100%
 				activity.setProgress(progress * 1000);
 			}
 
 			public void onGeolocationPermissionsShowPrompt(String origin,
 					GeolocationPermissions.Callback callback) {
 				Log.d(DEBUG_TAG, "Geolocation request for " + origin);
 				if (origin.startsWith("http://" + Utils.getDomain(activity))
 						|| origin.startsWith("https://" + Utils.getDomain(activity)))
 					callback.invoke(origin, true, false);
 				else
 					callback.invoke(origin, false, false);
 			}
 		});
 
 		setContentView(webview);
 		Log.d("WebView", "loading url");
 		webview.loadUrl("http://" + Utils.getDomain(getBaseContext()) + "/");
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.activity_open_location_main, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.menu_refresh:
			//TODO: send intent to broadcast receiver
			OpenLocationMainActivity.this.webview.loadUrl("http://" + Utils.getDomain(getBaseContext()) + "/");
 			break;
 		case R.id.menu_settings:
 			Intent intent = new Intent(OpenLocationMainActivity.this,
 					OpenLocationPrefsActivity.class);
 			startActivity(intent);
 			break;
 		case R.id.menu_exit:
 			Utils.stopReceiver(getBaseContext());
 			webview.clearCache(true);
 			Toast.makeText(getBaseContext(),
 					getResources().getString(R.string.msg_updatesdisabled),
 					Toast.LENGTH_SHORT).show();
 			finish();
 			break;
 		}
 		return false;
 	}
 	
 	@Override
 	public void onBackPressed() {
		if (webview != null && webview.canGoBack())
 			webview.goBack();
 		else
 			super.onBackPressed();
 	}
 	
 	public static void clearWebviewCache(Context context) {
 		if (webview != null) {
 			// TODO: This does not work
 			webview.clearCache(true);
 			//webview.reload();
 		}
 	}
 	
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.app.Activity#onPause()
 	 */
 	@Override
 	protected void onPause() {
 		super.onPause();
 		// webview.onPause();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.app.Activity#onResume()
 	 */
 	@Override
 	protected void onResume() {
 		super.onResume();
 		// webview.onResume();
 		//TODO: send intent to broadcast receiver
 		OpenLocationMainActivity.this.webview.reload();
 		//OpenLocationMainActivity.this.webview.loadUrl("http://" + Utils.getDomain(getBaseContext()) + "/");  // does reload() work?
 	}
 }
