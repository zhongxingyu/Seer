 package com.jackandjason;
 
 import java.io.IOException;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.TextView;
 import android.webkit.WebView;
 import android.webkit.WebViewClient;
 import android.view.KeyEvent;
 
 public class voter extends Activity {
     /** Called when the activity is first created. */
 
 	WebView webview;
 	private class VoteViewClient extends WebViewClient {
 		@Override
 		public boolean shouldOverrideUrlLoading(WebView view, String url) {
 			view.loadUrl(url);
 			return true;
 		}
 	}    
 	@Override
 		protected void onCreate(Bundle savedInstanceState) {
 			super.onCreate(savedInstanceState);
 			webview = new WebView(this);
 			setContentView(webview);
 
			webview.loadUrl("http://141.212.237.39:8888/vote");
 			webview.getSettings().setJavaScriptEnabled(true);
 			webview.setVerticalScrollBarEnabled(false);
 			webview.setHorizontalScrollBarEnabled(false);
 			webview.setWebViewClient(new VoteViewClient());
 		}
 
 	@Override
 		public boolean onKeyDown(int keyCode, KeyEvent event) {
 			if ((keyCode == KeyEvent.KEYCODE_BACK) && webview.canGoBack()) {
 				webview.goBack();
 				return true;
 			}
 			return super.onKeyDown(keyCode, event);
 		}	
 }
