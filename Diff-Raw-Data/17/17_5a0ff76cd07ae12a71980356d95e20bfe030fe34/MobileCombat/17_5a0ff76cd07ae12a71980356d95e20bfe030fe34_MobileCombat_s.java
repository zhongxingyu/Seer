 package com.pinokia.mobilecombat;
 
import android.app.Activity;
 import android.os.Bundle;
 import org.apache.cordova.*;
 
 public class MobileCombat extends DroidGap
 {
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
         super.loadUrl("file:///android_asset/www/index.html");
     }
 }
 
