 package com.phonegap.taskmanager;
 
 import android.app.Activity;
 import android.os.Bundle;
 import com.phonegap.*;
 
 public class TaskManagerActivity extends DroidGap {
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
        super.loadUrl("/assets/www/index.html");
     }
 }
