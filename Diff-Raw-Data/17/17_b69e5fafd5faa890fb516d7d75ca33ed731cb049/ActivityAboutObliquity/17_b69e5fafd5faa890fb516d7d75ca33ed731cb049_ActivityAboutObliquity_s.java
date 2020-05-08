 package com.xdev.obliquity;
 
 import android.os.Bundle;
 import android.view.Window;
 
import com.WazaBe.HoloEverywhere.R;
 import com.google.android.apps.analytics.easytracking.TrackedActivity;
 
 public class ActivityAboutObliquity extends TrackedActivity{
 
 	 @Override
 	    public void onCreate(Bundle savedInstanceState) {
 	        super.onCreate(savedInstanceState);
 	        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
 	        setContentView(R.layout.ac_aboutobliquity);
 	    }
 	 
 }
