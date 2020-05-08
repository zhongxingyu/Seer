 package com.tempura.storagewidget;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.view.Menu;
 
 public class WidgetConfigure extends Activity {
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configure);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {        
         return true;
     }
 }
