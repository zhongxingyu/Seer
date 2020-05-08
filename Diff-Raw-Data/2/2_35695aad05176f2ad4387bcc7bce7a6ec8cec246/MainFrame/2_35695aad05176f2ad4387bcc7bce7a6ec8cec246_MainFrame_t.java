 package com.jetthoughts.socksboots;
 
 import android.os.Bundle;
 import com.phonegap.*;
 
 public class MainFrame extends DroidGap
 {
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
        super.loadUrl("file:///android_asset/www/index_android.html");
     }
 }
 
