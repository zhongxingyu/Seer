 package com.tw.techradar.activity;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.webkit.WebView;
 import com.tw.techradar.R;
 
 public class WebActivity extends Activity {
     private WebView webView;
     public void onCreate(Bundle savedInstanceState) {
 
         super.onCreate(savedInstanceState);
 
         setContentView(R.layout.activity_web);
         webView = (WebView) findViewById(R.id.web_view);
         Bundle extras = getIntent().getExtras();
         String action = (String) extras.get("Action");
         if(action.equals("Introduction")){
             goToIntroduction(null);
         };
         if(action.equals("References")){
             goToReferences(null);
         };
         if(action.equals("About")){
             goToAbout(null);
         };
 
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.menu_navigation, menu);
         return true;
     }
 
     public void goToIntroduction(MenuItem menuItem){
         webView.loadUrl("file:///android_asset/html/introduction.html");
     }
 
     public void goToAbout(MenuItem menuItem){
         webView.loadUrl("file:///android_asset/html/about.html");
     }
 
     public void goToRadar(MenuItem menuItem){
        Intent intent = new Intent(this, CurrentRadar.class);
        startActivity(intent);
     }
 
     public void goToReferences(MenuItem menuItem){
         webView.loadUrl("file:///android_asset/html/radar_references.html");
     }
 }
