 package com.flow5.template;
 
 import android.os.Bundle;
 import com.phonegap.*;
 
 public class F5templateActivity extends DroidGap {
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
        super.loadUrl("http://10.0.1.5:8008/generate?app=jitc&native=true&inline=false&debug=true");
     }
 }
