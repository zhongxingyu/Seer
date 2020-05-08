 package com.nijine.encoderring;
 
 import android.app.TabActivity;
 import android.content.Intent;
 import android.content.res.Resources;
 import android.os.Bundle;
 import android.view.KeyEvent;
 import android.widget.TabHost;
 
 public class TabWidget extends TabActivity {
 	public void onCreate(Bundle savedInstanceState) {
 	    super.onCreate(savedInstanceState);
 	    setContentView(R.layout.main);
 
 	    Resources res = getResources(); // Resource object to get Drawables
 	    TabHost tabHost = getTabHost();  // The activity TabHost
 	    TabHost.TabSpec spec;  // Resusable TabSpec for each tab
 	    Intent intent;  // Reusable Intent for each tab
 
 	    // Create an Intent to launch an Activity for the tab (to be reused)
 	    intent = new Intent().setClass(this, EncoderRing.class);
 	    spec = tabHost.newTabSpec("EncoderRing").setIndicator("EncoderRing",
 	                      res.getDrawable(R.drawable.ic_tab_encoder))
 	                  .setContent(intent);
 	    tabHost.addTab(spec);
 	    
 	    intent = new Intent().setClass(this, PasswordList.class);
 	    spec = tabHost.newTabSpec("PasswordList").setIndicator("PasswordList",
 	                      res.getDrawable(R.drawable.ic_tab_encoder))
 	                  .setContent(intent);
 	    tabHost.addTab(spec);
 
	    tabHost.setCurrentTab(0);
 	}
 }
