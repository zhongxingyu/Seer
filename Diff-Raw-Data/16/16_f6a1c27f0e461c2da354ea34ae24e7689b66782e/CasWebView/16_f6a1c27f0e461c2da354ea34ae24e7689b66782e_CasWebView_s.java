 package fr.utc.assos.payutc;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.webkit.CookieManager;
 import android.webkit.CookieSyncManager;
 import android.webkit.WebSettings;
 import android.webkit.WebView;
 import android.webkit.WebViewClient;
 
 public class CasWebView extends Activity {
 	WebView webview;
 	Pattern pattern = Pattern.compile("ticket=([^&]+)");
 	Boolean finish = false;
 	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.caswebview);
         Bundle b = getIntent().getExtras();
         String casurl = b.getString("casurl");
         CookieSyncManager.createInstance(this);  
         CookieManager cookieManager = CookieManager.getInstance();  
         cookieManager.removeSessionCookie();
         webview = (WebView) findViewById(R.id.webview);
         webview.setWebViewClient(new HelloWebViewClient());
         WebSettings settings = webview.getSettings();
         settings.setJavaScriptEnabled(true);
         settings.setSaveFormData(false);
         settings.setSavePassword(false);
        webview.loadUrl(casurl+"?service="+PaulineActivity.CAS_SERVICE);
     }
     
     protected void returnTicket(String ticket) {
		Intent intent = new Intent();
		intent.putExtra("ticket", ticket);
        setResult(RESULT_OK, intent);
 		finish();
     }
     
     private class HelloWebViewClient extends WebViewClient {
         @Override
         public boolean shouldOverrideUrlLoading(WebView view, String url) {
         	if (!finish) {
 	        	Log.i("url", url);
 	        	Matcher matcher = pattern.matcher(url);
 	            if (matcher.find()) {
 	                finish = true;
 	                returnTicket(matcher.group(1));
 	            }
 	            view.loadUrl(url);
         	}
             return true;
         }
     }
 }
