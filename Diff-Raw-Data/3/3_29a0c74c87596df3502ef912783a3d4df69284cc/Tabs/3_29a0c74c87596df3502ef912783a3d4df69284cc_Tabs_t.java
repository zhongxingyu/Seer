 package org.oxygen.settings;
 
 import android.app.TabActivity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.widget.TabHost;
 
 public class Tabs extends TabActivity {
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         final TabHost tabHost = getTabHost();
 
         tabHost.addTab(tabHost.newTabSpec("tab1")
                 .setIndicator("Main")
                 .setContent(new Intent(this, MainTab.class)
                         .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)));
 
         tabHost.addTab(tabHost.newTabSpec("tab2")
                 .setIndicator("Power Widget")
                 .setContent(new Intent(this, PowerWidgetTab.class)
                         .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)));
 
         tabHost.addTab(tabHost.newTabSpec("tab3")
                 .setIndicator("Updater")
                .setContent(new Intent(this, UpdateTab.class)));
     }
 
 }
