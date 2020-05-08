 package com.mmt.snups;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
import android.util.Log;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.webkit.WebView;
 import android.webkit.WebViewClient;
 
public class SnupsPhotoStream extends Activity {	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.snups_photo_stream);
  
         WebView web = (WebView) findViewById(R.id.webView);
        web.getSettings().setJavaScriptEnabled(false); 
         web.getSettings().setJavaScriptCanOpenWindowsAutomatically(false);
         web.getSettings().setPluginsEnabled(false);
         web.getSettings().setSupportMultipleWindows(false);
         web.getSettings().setSupportZoom(false);
         web.setVerticalScrollBarEnabled(false);
         web.setHorizontalScrollBarEnabled(false);
  
         //Our application's main page will be loaded
        web.loadUrl("http://franzonrails.multimediatechnology.at/mobile/home");
  
         web.setWebViewClient(new WebViewClient() {
             @Override public boolean shouldOverrideUrlLoading(WebView view, String url) {
            	Log.v("URL: ", url);
                 return false;
             }
        }); 
        
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
