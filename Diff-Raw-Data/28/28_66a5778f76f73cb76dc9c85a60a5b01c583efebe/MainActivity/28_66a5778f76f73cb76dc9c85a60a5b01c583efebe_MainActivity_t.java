 package com.example.helloiccm;
 
 import android.os.Bundle;
 import android.view.Menu;
 import org.apache.cordova.*;
import android.view.WindowManager;
 
 public class MainActivity extends DroidGap {
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);

        // Make app full screen (no status bar)
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN | 
        WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        
         super.loadUrl("file:///android_asset/www/index.html");
        
        // We shouldn't have to do this!
        this.appView.setInitialScale(100);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_main, menu);
         return true;
    }  
}
