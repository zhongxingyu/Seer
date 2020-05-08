 package com.vorsk.crossfitr;
 
 import android.app.TabActivity;
 import android.content.Intent;
 import android.content.res.Resources;
 import android.os.Bundle;
 import android.widget.TabHost;
 
 public class TimeTabWidget extends TabActivity{ // Resource object to get Drawables
 	long id;
 	TabHost tabhost;
 	
 	public void onCreate(Bundle savedInstanceState) {
 	    super.onCreate(savedInstanceState);
 	    setContentView(R.layout.time_tab_widget);
 	    
 	    Resources res = getResources(); // Resource object to get Drawables
 	    tabhost = getTabHost(); // The activity TabHost
 	    TabHost.TabSpec spec;  // Resusable TabSpec for each tab
 	    Intent intent;
 
 	  	//get the id passed from previous activity (workout lists)
 	  	id = getIntent().getLongExtra("workout_id", -1);
 	  	//if ID is invalid, go back to home screen
 	  	if(id < 0)
 	  	{
 	  		getParent().setResult(RESULT_CANCELED);
 	  		finish();
 	  	}
 	  	
 	  	
 	    // Create an Intent to launch an Activity for the tab (to be reused)
 	    intent = new Intent().setClass(this, TimerActivity.class);
 	    intent.putExtra("ID", id);
 
 	    // Initialize a TabSpec for each tab and add it to the TabHost
 	    spec = tabhost.newTabSpec("timer").setIndicator("Timer",
 	                      res.getDrawable(R.drawable.timer_tab_icons))
 	                  .setContent(intent);
 	    tabhost.addTab(spec);
 	    tabhost.setEnabled(false);
 
 
 	    // Do the same for the other tabs
 	    intent = new Intent().setClass(this, StopwatchActivity.class);
 	    intent.putExtra("ID", id);
 	    spec = tabhost.newTabSpec("stopwatch").setIndicator("Stopwatch",
	                      res.getDrawable(R.drawable.tab_stopwatch))
 	                  .setContent(intent);
 	    tabhost.addTab(spec);
 
 	    intent = new Intent().setClass(this, TabataActivity.class);
 	    intent.putExtra("ID", id);
 	    spec = tabhost.newTabSpec("tabata").setIndicator("Tabata",
	                      res.getDrawable(R.drawable.tab_tabata))
 	                  .setContent(intent);
 	    tabhost.addTab(spec);
 
 	    tabhost.setCurrentTab(1);
 	}
 }
 
 	
