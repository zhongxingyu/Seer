 package edu.mines.csci498.ybakos.lunchlist;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.webkit.WebView;
 
 public class HelpActivity extends Activity {
 
 	private WebView browser;
 	
 	@Override
 	public void onCreate(Bundle state) {
 		super.onCreate(state);
 		setContentView(R.layout.help);
 		browser = (WebView) findViewById(R.id.webkit);
		browser.loadUrl("file:///android_asset/help.html");
 	}
 	
 }
