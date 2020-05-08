 package com.twt.xtreme;
 
 import android.app.Activity;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.webkit.WebView;
 import android.webkit.WebViewClient;
 
 public class TrackViewActivity extends Activity {
 
 	private static final String TAG = "TrackViewActivity";
 	private SharedPreferences pref;
 	private String track_url;
 	private WebView webview;
 	private String sample_url;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
		sample_url = getText(R.string.tracking_view_url).toString() + "/48";
 		pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
 		track_url = getText(R.string.tracking_view_url).toString() + Util.getSharedPrefStr(getApplicationContext(), "rental_id");
 		webview = new WebView(this);
 		webview.setVerticalFadingEdgeEnabled(true);
 		webview.setVerticalScrollbarOverlay(true);
 		setContentView(webview);
 	}
 
 	@Override
 	protected void onDestroy() {
 		// TODO Auto-generated method stub
 		super.onDestroy();
 	}
 
 	@Override
 	protected void onStart() {
 		super.onStart();
 		webview.getSettings().setJavaScriptEnabled(true);
 		webview.setWebViewClient(new WebViewClient() {
 			public boolean shouldOverrideUrlLoading(WebView view, String url) {
 				webview.loadUrl(url);
 				return true;
 			}
 			
 		});
 		webview.loadUrl(track_url);
 	}
 
 
     @Override
 	public void onBackPressed() {
     	if (webview.getUrl() != null && !webview.getUrl().equals(track_url)) {
     		webview.loadUrl(track_url);
     	} else {
     		Log.d(TAG, "at the report url, closing activity.");
     		super.onBackPressed();
     	}
 	}
 
 	@Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.report_menu, menu);
         return true;
     }
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
         case R.id.menu_close:
         	this.finish();
         	return true;
         	
         case R.id.menu_report_list:
         	webview.loadUrl(sample_url);
         	break;
         }
 
         return false;
     }
 }
