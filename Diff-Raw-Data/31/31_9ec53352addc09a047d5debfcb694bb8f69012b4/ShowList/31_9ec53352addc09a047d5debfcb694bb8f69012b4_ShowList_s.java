 package com.teamholder.shopping;
 
 import android.app.Activity;
 import android.os.Bundle;
import android.util.Log;
 import android.webkit.WebView;
 
 public class ShowList extends Activity {
     @Override
     public void onCreate(Bundle savedInstanceState) {
    	Log.i("ShowList", "creating ShowList activity");
         super.onCreate(savedInstanceState);
        if (savedInstanceState.containsKey(DisplayLists.LIST_URL)) {
        	Log.i("ShowList", "found list url key");
        } else {
        	Log.i("ShowList", "no key found");
        }
        String targetUrl = savedInstanceState.getString(DisplayLists.LIST_URL);
         WebView webview = new WebView(this);
         setContentView(webview);
         webview.loadUrl(targetUrl);
     }
 }
