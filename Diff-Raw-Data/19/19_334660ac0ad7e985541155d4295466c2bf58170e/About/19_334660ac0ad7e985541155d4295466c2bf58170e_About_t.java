 package com.magneticbear.scala1;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.view.Menu;
import android.webkit.WebView;
 
 public class About extends Activity {
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_about);
        
        // Navigate to about url
        WebView playground_webview = (WebView)findViewById(R.id.webview_about);
        playground_webview.getSettings().setJavaScriptEnabled(true);
        playground_webview.loadUrl(getString(R.string.url_about));
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_about, menu);
         return true;
     }
 }
