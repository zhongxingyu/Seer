 package it.helpus.helpapp;
 
 import android.annotation.SuppressLint;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.Bundle;
 import android.util.Log;
 import android.webkit.JsResult;
 import android.webkit.WebChromeClient;
 import android.webkit.WebView;
 import android.webkit.WebViewClient;
 import android.widget.Toast;
 
 import com.actionbarsherlock.app.SherlockActivity;
 
 public class WebActivity extends SherlockActivity {
 	private WebView webView;
 	private String weburl;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 		
 		if(!this._isNetworkAvailable()) {
 			AlertDialog.Builder builder = new AlertDialog.Builder(this);
 			builder.setMessage("Nessuna connessione dati attiva.")
 			       .setCancelable(false)
 			       .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
 			           public void onClick(DialogInterface dialog, int id) {
 			                WebActivity.this.finish();
 			           }
 			       }
 	        );
 			AlertDialog alert = builder.create();
 			alert.show();
 		}
 		else {
 			Bundle extras = getIntent().getExtras();
 			if(extras != null) {
 				weburl = extras.getString("weburl");
 			}
 			
 			setupWebView();
 		}
 	}
 
 	private Boolean _isNetworkAvailable() {
 		ConnectivityManager conMgr = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
 		NetworkInfo i = conMgr.getActiveNetworkInfo();
 		if (i == null)
 			return false;
 		if (!i.isConnected())
 			return false;
 		if (!i.isAvailable())
 			return false;
 		return true;
 	}
 
	@Override
    public void onBackPressed() {
        // Pop the browser back stack or exit the activity
        if (webView.canGoBack()) {
        	webView.goBack();
        }
        else {
            super.onBackPressed();
        }
    }

 	/** Sets up the WebView object and loads the URL of the page **/
 	@SuppressLint("SetJavaScriptEnabled")
 	private void setupWebView() {
 		webView = (WebView) findViewById(R.id.webview);
 		webView.getSettings().setJavaScriptEnabled(true);
 		webView.getSettings().setAllowFileAccess(true);
 		webView.setWebViewClient(new WebViewClient());
 		webView.setWebChromeClient(new MyChromeWebViewClient());
 		webView.getSettings().setGeolocationEnabled(true);
 		webView.loadUrl(weburl);
 		/** Allows JavaScript calls to access application resources **/
 		webView.addJavascriptInterface(new JavaScriptInterface(), "android");
 	}
 
 	/**
 	 * Sets up the interface for getting access to Latitude and Longitude data
 	 * from device
 	 **/
 	private class JavaScriptInterface {
 		
 	}
 
 	private class MyChromeWebViewClient extends WebChromeClient {
 
 	    @Override
 	    public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
 	        Log.d("HELPUS", message);
 
 			AlertDialog.Builder builder = new AlertDialog.Builder(WebActivity.this);
 			builder.setMessage(message)
 			       .setCancelable(true)
 			       .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
 			           public void onClick(DialogInterface dialog, int id) {
 			        	   dialog.cancel();
 			           }
 			       }
 	        );
 	        return true;
 	    }
 
 		@SuppressWarnings("unused")
 		public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
 			Toast.makeText(WebActivity.this, "Oh no! " + description,
 					Toast.LENGTH_SHORT).show();
 		}
 	}
 
 }
