 package com.ffvl.geoloc;
 
 import android.app.Activity;
 import org.apache.cordova.DroidGap;
 
 import com.strumsoft.websocket.phonegap.WebSocketFactory;
 
 import android.os.Bundle;
 
 public class GeolocActivity extends DroidGap {
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         super.loadUrl("file:///android_asset/www/index.html");
         
        this.appView.addJavascriptInterface(new WebSocketFactory(this), "WebSocketFactory");
     }
 }
