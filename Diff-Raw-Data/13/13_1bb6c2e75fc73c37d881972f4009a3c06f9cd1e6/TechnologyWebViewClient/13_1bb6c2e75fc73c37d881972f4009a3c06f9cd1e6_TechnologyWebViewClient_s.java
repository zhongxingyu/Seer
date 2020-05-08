 package com.parworks.mars.view.technology;
 
 import android.webkit.WebView;
 import android.webkit.WebViewClient;
 
 public class TechnologyWebViewClient extends WebViewClient {
 	
 	@Override
 	public boolean shouldOverrideUrlLoading(WebView view, String url) {
		view.loadUrl(url);
 		return false;
 	}	
 }
