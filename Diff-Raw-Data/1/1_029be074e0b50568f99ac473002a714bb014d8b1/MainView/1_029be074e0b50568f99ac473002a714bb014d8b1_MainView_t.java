 package com.example.quickcal;
 
 import java.util.Calendar;
 
 import android.os.Bundle;
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.util.Log;
 import android.view.Menu;
 import android.webkit.JavascriptInterface;
 import android.webkit.WebSettings;
 import android.webkit.WebView;
 import android.webkit.WebSettings.ZoomDensity;
 import android.widget.Toast;
 
 public class MainView extends Activity {
 
     @SuppressLint("SetJavaScriptEnabled") @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_web_view);
         
        WebView view = (WebView) findViewById(R.id.webview);
        view.addJavascriptInterface(new WebAppInterface(this), "Native");
        WebSettings webSettings = view.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setTextZoom(150);
        view.loadUrl("file:///android_asset/index.html");
        
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.web_view, menu);
         return true;
     }
  
     public class WebAppInterface {
         Context mContext;
 
         /** Instantiate the interface and set the context */
         WebAppInterface(Context c) {
             mContext = c;
         }
 
         /** Show a toast from the web page */
         @JavascriptInterface
         public void showToast(String toast) {
             Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
         }
         
         @JavascriptInterface
         public void handleReturnFromWebPage(String eventName, String eventLocation, String eventDescription, String allDay) {
         	Intent calendarIntent = new Intent(Intent.ACTION_EDIT);
         	calendarIntent.setType("vnd.android.cursor.item/event");
         	calendarIntent.putExtra("title", eventName);
         	calendarIntent.putExtra("eventLocation",eventLocation);
         	calendarIntent.putExtra("description",eventDescription);
         	Boolean allDayBool = false;
         	if (allDay.matches("Yes"))
         	{
         		allDayBool = true;
         	}
         	calendarIntent.putExtra("allDay", allDayBool);
         	startActivity(calendarIntent);  	
         }
         
     }
 }
