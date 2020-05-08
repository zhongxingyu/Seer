 package net.hope.mobile;
 
 import android.app.Activity;
 import android.app.SearchManager;
 import android.content.Context;
 import android.content.Intent;
 import android.net.ConnectivityManager;
 import android.net.Uri;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.webkit.WebView;
 import android.webkit.WebViewClient;
 import android.widget.Button;
 import android.widget.Toast;
 
 public class TheNextHOPE extends Activity {
 	private WebView webview;
 	private Button scheduleButton;
 	private Button favoritesButton;
 	private Button noticeButton;
 
     private static final String LOG_TAG = "TheNextHOPE";
 	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         
         // initialize the buttons
         scheduleButton = (Button) findViewById(R.id.scheduleButton);
         scheduleButton.setOnClickListener(new View.OnClickListener() {
         	public void onClick(View v) {
         		webview.loadUrl("file:///android_asset/www/schedule.html");
         		//Toast.makeText(getBaseContext(), "Loading Schedule", Toast.LENGTH_SHORT).show();
         	}
         });
         favoritesButton = (Button) findViewById(R.id.favoritesButton);
         favoritesButton.setOnClickListener(new View.OnClickListener() {
         	public void onClick(View v) {
         		webview.loadUrl("file:///android_asset/www/favorites.html");
         		//Toast.makeText(getBaseContext(), "Loading Favorites", Toast.LENGTH_SHORT).show();
         	}
         });
         noticeButton = (Button) findViewById(R.id.noticeButton);
         noticeButton.setOnClickListener(new View.OnClickListener() {
         	public void onClick(View v) {
         		webview.loadUrl("file:///android_asset/www/notice.html");
         	}
         });
         
         // initialize the web view
         JSInterface jsInterface = new JSInterface(getBaseContext());
         webview = (WebView) findViewById(R.id.webview);
         webview.getSettings().setAllowFileAccess(true);
         webview.getSettings().setJavaScriptEnabled(true);
         webview.setFocusable(true);
         webview.setFocusableInTouchMode(false);
         webview.addJavascriptInterface(jsInterface, "JSInterface");
         webview.setWebViewClient(new WebViewClient() {  
         	@Override
         	public boolean shouldOverrideUrlLoading(WebView view, String url) {
         		view.loadUrl(url);
         		return true;
         	}
         	public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
         		Toast.makeText(getBaseContext(), "Oh no! "+description, Toast.LENGTH_SHORT).show();
         	}
         });
 
         onNewIntent(getIntent());
     }
 
     @Override
     protected void onNewIntent(Intent intent) {
         StringBuilder url = new StringBuilder("file:///android_asset/www/schedule.html");
 
         if (intent.getAction().equals(Intent.ACTION_SEARCH)) {
             String query=intent.getStringExtra(SearchManager.QUERY);
             if (query != null) {
                 url.append("?q=");
                 url.append(Uri.encode(query.trim()));
             }
         }
 
         Log.d(LOG_TAG, url.toString());
         webview.loadUrl(url.toString());
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         super.onCreateOptionsMenu(menu);
 
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.main_menu, menu);
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch(item.getItemId()) {
         case R.id.menu_search:
             onSearchRequested();
             return true;
         }
         return super.onOptionsItemSelected(item);
     }
 
     public boolean isOnline() {
     	ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
     	return cm.getActiveNetworkInfo().isConnected();
     }
 }
