 package com.comp1008.happygui;
 
import android.util.Log;
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
			Log.d("MyWebViewClient", "Finished loading page: " + url);
 			loadedEditor = true;
 			activity.setContentView(view);
 		}
 	}
 }
