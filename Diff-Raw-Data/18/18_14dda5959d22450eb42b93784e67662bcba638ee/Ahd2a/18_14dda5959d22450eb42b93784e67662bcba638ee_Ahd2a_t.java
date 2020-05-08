 package com.onemobs.ahd2a;
 
 import android.os.Bundle;
 import com.phonegap.*;
import android.webkit.WebSettings.RenderPriority;
 
 public class Ahd2a extends DroidGap {
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         super.setIntegerProperty("splashscreen", R.drawable.splash);
         
         String baseUrl = "file:///android_asset/www/";
         super.loadUrl(baseUrl+"init.html",300);
        
        this.appView.setBackgroundColor(0x000000);
        this.appView.setHorizontalScrollBarEnabled(false);
        this.appView.setHorizontalScrollbarOverlay(false);
        this.appView.setVerticalScrollBarEnabled(false);
        this.appView.setVerticalScrollbarOverlay(false);
        
        this.appView.getSettings().setRenderPriority( RenderPriority.HIGH );
        this.appView.getSettings().setBuiltInZoomControls(false); 
        this.appView.getSettings().setSupportZoom(false); 
        this.appView.getSettings().setUseWideViewPort(true);
        this.appView.getSettings().setLoadWithOverviewMode(true);

        
        
        
     }
 }
