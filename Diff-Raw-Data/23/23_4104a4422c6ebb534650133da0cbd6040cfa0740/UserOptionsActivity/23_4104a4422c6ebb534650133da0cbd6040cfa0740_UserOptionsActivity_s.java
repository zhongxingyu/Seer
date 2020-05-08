 package com.thesis.emostatus;
 
 import android.app.ActionBar;
 
 import android.content.Intent;
 import android.os.Bundle;
 import android.support.v4.app.FragmentTabHost;
 import android.support.v4.app.FragmentActivity;
 import android.util.Log;
 
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 
 import android.widget.TabHost;
 
 
 import persistance.EmoStatus;
 
 public class UserOptionsActivity extends FragmentActivity {
     private FragmentTabHost mTabHost;
     private String user;
     protected Menu menu;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         setContentView(R.layout.activity_user_options);
         mTabHost = (FragmentTabHost)findViewById(android.R.id.tabhost);
         mTabHost.setup(this, getSupportFragmentManager(), R.id.realtabcontent);
 
         mTabHost.addTab(mTabHost.newTabSpec("history").setIndicator("Historial"),HistoryUserActivity.class,savedInstanceState);
         mTabHost.addTab(mTabHost.newTabSpec("monitoring").setIndicator("Monitoreo"),MonitorUserActivity.class,savedInstanceState);
         mTabHost.addTab(mTabHost.newTabSpec("alerts").setIndicator("Alertas"),AlertsUserActivity.class,savedInstanceState);
 
         mTabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
             @Override
             public void onTabChanged(String s) {
                 Log.i("tab",s);
                 if(s.equals("history")){
                     menu.findItem(R.id.calendar).setVisible(true);
                 }
                 else{
                     menu.findItem(R.id.calendar).setVisible(false);
                 }
             }
         });
 
         EmoStatus app = (EmoStatus)getApplicationContext();
         user = app.getActualUserMonitorized();
 
         ActionBar actionBar = getActionBar();
         actionBar.setTitle(user);
         actionBar.setHomeButtonEnabled(true);
         actionBar.setDisplayHomeAsUpEnabled(true);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.more_options, menu);
         this.menu = menu;
         return super.onCreateOptionsMenu(menu);
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem menuItem)
     {
         switch (menuItem.getItemId()){
             case android.R.id.home:
                 startActivity(new Intent(this,UserListActivity.class));
                 break;
             case R.id.info:
                 Intent info = new Intent(this,InformationActivity.class);
                 startActivity(info);
                 break;
             case R.id.help:
                 Intent help = new Intent(this,HelpActivity.class);
                 startActivity(help);
                 break;
             case R.id.logout:
                 Intent login = new Intent(this,MainActivity.class);
                 startActivity(login);
                 finish();
                 break;
             case R.id.exit:
                 finish();
                 break;
             case R.id.calendar:
                 ((HistoryUserActivity)getSupportFragmentManager().findFragmentByTag("history")).showDateOptionsDialog();
                 break;
         }
         return super.onOptionsItemSelected(menuItem);
     }
 
     @Override
     public void onBackPressed() {
         startActivity(new Intent(this,UserListActivity.class));
         super.onBackPressed();
     }
 }
