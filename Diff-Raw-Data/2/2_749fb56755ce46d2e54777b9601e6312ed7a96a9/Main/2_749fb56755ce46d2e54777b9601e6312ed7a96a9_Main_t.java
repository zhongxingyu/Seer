 package no.whirlwin.nerdfeud;
 
 import org.apache.cordova.DroidGap;
 
 import android.app.Activity;
 import android.os.Bundle;
 
 /**
  * 
  * @author whirlwin
  *
  */
 public class Main extends DroidGap {
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
        super.loadUrl("file:///android_asset/www/index.html");
     }
 }
