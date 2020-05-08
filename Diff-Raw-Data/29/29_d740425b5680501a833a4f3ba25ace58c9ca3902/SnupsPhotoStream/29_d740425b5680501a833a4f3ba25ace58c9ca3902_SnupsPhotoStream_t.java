 package com.mmt.snups;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.net.Uri;
 import android.os.Bundle;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.Window;
 import android.webkit.WebChromeClient;
 import android.webkit.WebView;
 import android.webkit.WebViewClient;
  
 public class SnupsPhotoStream extends Activity
 {
 	private SharedPreferences mPreferences;
     final Activity activity = this;
  
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
         this.getWindow().requestFeature(Window.FEATURE_PROGRESS);
         setContentView(R.layout.snups_photo_stream);
         WebView webView = (WebView) findViewById(R.id.webView);
         webView.getSettings().setJavaScriptEnabled(true);
  
         webView.setWebChromeClient(new WebChromeClient() {
             public void onProgressChanged(WebView view, int progress)
             {
                 activity.setProgress(progress * 100);
  
                 if(progress == 100)
                     activity.setTitle(R.string.app_name);
             }
         });
  
         webView.setWebViewClient(new WebViewClient() {
         	@Override
         	public boolean shouldOverrideUrlLoading(WebView view, String url) {               
         		if (Uri.parse(url).getHost().equals("franzonrails.multimediatechnology.at")) {
                     // This is my web site, so do not override; let my WebView load the page
                     return false;
                 }
         		view.loadUrl(url);
                 return true;
             }
         	
         	@Override
             public void onReceivedError(WebView view, int errorCode, String description, String failingUrl)
             {
                 // Handle the error
             }
             
             
  
 
 
         });
         mPreferences = getSharedPreferences("CurrentUser", MODE_PRIVATE);
         
         webView.loadUrl("http://franzonrails.multimediatechnology.at/mobilehome?auth_token=" + mPreferences.getString("Token", ""));
     }
 
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.menu, menu);
         return true;
     }
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         // Handle item selection
         switch (item.getItemId()) {
         case R.id.tutorial:
       	  TutorialDialog tutorialDialog = new TutorialDialog(this);
         	  tutorialDialog.show();
             return true;
         case R.id.quit:
       	  finish();
             return true;
         default:
             return super.onOptionsItemSelected(item);
         }
     }
     @Override 
     public boolean onKeyDown(int keyCode, KeyEvent event) { 
         if (keyCode == KeyEvent.KEYCODE_BACK) { 
 	            Intent intent=new Intent(getApplicationContext(),SnupsMenu.class);
 	            startActivity(intent);
 	            finish();
                 return true; 
         } 
         return super.onKeyDown(keyCode, event); 
     }  
 }
