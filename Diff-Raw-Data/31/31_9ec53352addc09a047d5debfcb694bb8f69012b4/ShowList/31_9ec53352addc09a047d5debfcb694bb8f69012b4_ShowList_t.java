 package com.teamholder.shopping;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.webkit.WebView;
 
 public class ShowList extends Activity {
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         WebView webview = new WebView(this);
         setContentView(webview);
        String targetUrl = getIntent().getStringExtra(DisplayLists.LIST_URL);
         webview.loadUrl(targetUrl);
     }
 }
