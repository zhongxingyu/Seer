 package com.magneticbear.scala1;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.view.Menu;
import android.webkit.WebView;
 
 public class Playground extends Activity {
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_playground);
        
        // Navigate to playground url
        WebView playground_webview = (WebView)findViewById(R.id.webview_playground);
        playground_webview.getSettings().setJavaScriptEnabled(true);
        playground_webview.loadUrl(getString(R.string.url_playground));
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_playground, menu);
         return true;
     }
 }
