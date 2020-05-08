 package com.patmahoneyJR.csu.vikings;
 
import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;
 
public class MobileEmail extends Activity {

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.webview);
        
        WebView webview = (WebView) findViewById(R.id.webview);
        webview.loadUrl("file:///android_asset/mobile_email.html");
	}
	
 }
