 package org.jessies.mathdroid;
 
import android.app.*;
import android.os.*;
import android.view.*;
import android.webkit.*;
 
 /**
  * Displays our help text in a WebView.
  */
 public class MathdroidHelp extends Activity {
     @Override public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
         setContentView(R.layout.help);
         ((WebView) findViewById(R.id.help_webview)).loadUrl("file:///android_asset/help.html");
     }
 }
