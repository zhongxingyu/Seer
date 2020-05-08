 package com.example.jsmap;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.view.Menu;
 import android.webkit.WebChromeClient;
 import android.webkit.WebView;
 import android.webkit.WebViewClient;
 import android.widget.Button;
 import android.widget.Toast;
 
 public class MainActivity extends Activity {
 	WebView webview;
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         //webview = (WebView) findViewById(R.id.webView);
         webview = new WebView(this);
         webview.getSettings().setJavaScriptEnabled(true);
         //webview.setWebChromeClient(new WebChromeClient());
         final Activity act = this;
         webview.setWebViewClient(new WebViewClient(){
             public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                 Toast.makeText(act, description, Toast.LENGTH_SHORT).show();
             }
         });
        webview.loadUrl("file:///android_asset/Index.html");
         setContentView(webview);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_main, menu);
         return true;
     }
 }
