 package com.comp1008.happygui;
 
 import android.webkit.WebView;
 import android.webkit.WebViewClient;
 
 public class MyWebViewClient extends WebViewClient {
 	private MainActivity activity;
 	private boolean loadedEditor = false;
 	
 	public MyWebViewClient(MainActivity activity) {
 		this.activity = activity;
 	}
 	
 	@Override
 	public void onPageFinished(WebView view, String url) {
 		super.onPageFinished(view, url);
 		if(!loadedEditor) {
 			loadedEditor = true;
 			activity.setContentView(view);
 		}
 	}
 }
