 package com.jackandjason;
 
 import java.io.IOException;
 
import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceListener;

 import android.app.Activity;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.TextView;
 import android.webkit.WebView;
 import android.webkit.WebViewClient;
 import android.view.KeyEvent;
import javax.jmdns.ServiceInfo;
 
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
 
			webview.loadUrl("http://127.0.0.1:8888/");
 			webview.getSettings().setJavaScriptEnabled(true);
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
