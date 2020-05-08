 package org.jessies.mathdroid;
 
import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;
 
 /**
  * Displays our help text in a WebView.
  */
 public class MathdroidHelp extends Activity {
     @Override public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.help);
         ((WebView) findViewById(R.id.help_webview)).loadUrl("file:///android_asset/help.html");
     }
 }
