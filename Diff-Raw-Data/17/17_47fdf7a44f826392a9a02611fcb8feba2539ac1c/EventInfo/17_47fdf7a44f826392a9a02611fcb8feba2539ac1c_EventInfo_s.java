 package com.magneticbear.scala1;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.view.Menu;
 import android.webkit.WebView;
 import android.widget.TextView;
 
 public class EventInfo extends Activity {
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_event_info);
         
         // Setup debug data
         String event_title_to_display = "Event Info";
         int event_id_to_load = 1;
         
         // Navigate to event url
         WebView event_info_webview = (WebView)findViewById(R.id.webview_event_info);
         event_info_webview.getSettings().setJavaScriptEnabled(true);
         event_info_webview.loadUrl(getString(R.string.url_event_info_base) + event_id_to_load);
         
         // Set title to event title
         TextView title = (TextView)findViewById(R.id.event_info_title);
         title.setText(event_title_to_display);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_event_info, menu);
         return true;
     }
 }
