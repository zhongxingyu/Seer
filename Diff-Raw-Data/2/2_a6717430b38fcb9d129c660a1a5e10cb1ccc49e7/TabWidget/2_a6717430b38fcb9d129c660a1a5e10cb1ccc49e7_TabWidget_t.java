 package org.digitalsprouts.estoffer;
 
 import android.app.TabActivity;
 import android.content.Intent;
 import android.content.res.Resources;
 import android.os.Bundle;
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
         // Initialize a TabSpec for each tab and add it to the TabHost
         intent = new Intent().setClass(this, Enumbers.class);
         spec = tabHost.newTabSpec("enumbers").setIndicator(getString(R.string.enumbers),
                 res.getDrawable(R.drawable.ic_estoffer))
                 .setContent(intent);
         tabHost.addTab(spec);
 
         // Do the same for the other tabs
         intent = new Intent().setClass(this, OtherAdditives.class);
         spec = tabHost.newTabSpec("other_additives").setIndicator(getString(R.string.other_additives),
                 res.getDrawable(R.drawable.ic_andre))
                 .setContent(intent);
         tabHost.addTab(spec);
 
 
         // Do the same for the other tabs
         String locale = getString(R.string.language_code);
         //TODO support menu option for turning display of Norwegian info on
         if (locale.equals("no")) {
            intent = new Intent().setClass(this, BeerList.class);
             spec = tabHost.newTabSpec("beer_list").setIndicator(getString(R.string.beer_list),
                     res.getDrawable(R.drawable.ic_beer_drw))
                     .setContent(intent);
             tabHost.addTab(spec);
 
             intent = new Intent().setClass(this, WineList.class);
             spec = tabHost.newTabSpec("wine_list").setIndicator(getString(R.string.wine_list),
                     res.getDrawable(R.drawable.ic_wine_drw))
                     .setContent(intent);
             tabHost.addTab(spec);
         }
 
         // Do the same for the other tabs
         intent = new Intent().setClass(this, MoreInformation.class);
         spec = tabHost.newTabSpec("more_info").setIndicator(getString(R.string.more_info),
                 res.getDrawable(R.drawable.ic_info))
                 .setContent(intent);
         tabHost.addTab(spec);
 
 
         tabHost.setCurrentTab(0);
     }
 }
