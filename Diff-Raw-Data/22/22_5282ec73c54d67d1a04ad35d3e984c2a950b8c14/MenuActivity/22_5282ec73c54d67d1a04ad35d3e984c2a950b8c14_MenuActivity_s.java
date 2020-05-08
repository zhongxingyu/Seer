 package no.kantega.android.afp;
 
 import android.app.TabActivity;
 import android.content.Intent;
 import android.content.res.Resources;
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.widget.TabHost;
 
 /**
  * This activity handles the bottom navigation panel (tabs)
  */
 public class MenuActivity extends TabActivity {
     private String uri = "drawable/menubar_0";
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         setupTabs();
     }
 
     /**
      * Configure the tab menu
      */
     private void setupTabs() {
         Resources res = getResources();
         final TabHost tabHost = getTabHost();
         TabHost.TabSpec spec;
         Intent intent;
         intent = new Intent().setClass(this, OverviewActivity.class);
         String overview = "0";
         spec = tabHost.newTabSpec(overview).setIndicator("").setContent(intent);
         tabHost.addTab(spec);
         intent = new Intent().setClass(this, TransactionsActivity.class);
         String transactions = "1";
         spec = tabHost.newTabSpec(transactions).setIndicator("").setContent(intent);
         tabHost.addTab(spec);
        intent = new Intent().setClass(this, OverviewActivity.class);
         String charts = "2";
         spec = tabHost.newTabSpec(charts).setIndicator("").setContent(intent);
         tabHost.addTab(spec);
         intent = new Intent().setClass(this, SynchronizeActivity.class);
         String synchronize = "3";
         spec = tabHost.newTabSpec(synchronize).setIndicator("").setContent(intent);
         tabHost.addTab(spec);
         tabHost.setCurrentTab(0);
         tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
             @Override
             public void onTabChanged(String tabId) {
                 setTabColors(tabHost, tabId);
             }
         });
         setTabColors(tabHost, "0");
     }
 
     private void setTabColors(TabHost tabHost, String tabId) {
         for (int i = 0; i < tabHost.getTabWidget().getChildCount(); i++) {
             int imageResource = getApplicationContext().getResources().getIdentifier(uri + i, null, getPackageName());
             Drawable image = getResources().getDrawable(imageResource);
             tabHost.getTabWidget().getChildAt(i).setBackgroundDrawable(image);
         }
         int imageResource = getApplicationContext().getResources().getIdentifier(uri + tabId + "_selected", null, getPackageName());
         Drawable image = getResources().getDrawable(imageResource);
         tabHost.getTabWidget().getChildAt(tabHost.getCurrentTab()).setBackgroundDrawable(image);
     }
 }
