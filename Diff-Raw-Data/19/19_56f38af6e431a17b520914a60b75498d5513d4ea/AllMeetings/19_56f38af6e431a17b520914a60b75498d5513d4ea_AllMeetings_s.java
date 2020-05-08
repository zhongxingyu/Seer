 package com.uiproject.meetingplanner;
 
 import android.app.TabActivity;
 import android.content.Intent;
 import android.content.res.Resources;
 import android.os.Bundle;
 import android.widget.TabHost;
 
 public class AllMeetings extends TabActivity{
 
 	public void onCreate(Bundle savedInstanceState) {
 	    super.onCreate(savedInstanceState);
 	    
 	    Resources res = getResources(); // Resource object to get Drawables
 	    TabHost tabHost = getTabHost();  // The activity TabHost
 	    TabHost.TabSpec spec;  // Resusable TabSpec for each tab
 	    Intent intent;  // Reusable Intent for each tab
 
 	    // Create an Intent to launch an Activity for the tab (to be reused)
 	    intent = new Intent().setClass(this, MeetingListPending.class);
 
 	    // Initialize a TabSpec for each tab and add it to the TabHost
 	    spec = tabHost.newTabSpec("pending").setIndicator("Pending")
         			  .setContent(intent);
 	    tabHost.addTab(spec);
 	    
 	    intent = new Intent().setClass(this, MeetingListAccepted.class);
 	    spec = tabHost.newTabSpec("accepted").setIndicator("Accepted")
 	                  .setContent(intent);
 	    tabHost.addTab(spec);
 	    
 	    intent = new Intent().setClass(this, MeetingListDeclined.class);
 	    spec = tabHost.newTabSpec("declined").setIndicator("Declined")
         			  .setContent(intent);
 	    tabHost.addTab(spec);
 
<<<<<<< HEAD
 	    tabHost.setCurrentTab(3);
=======
	    tabHost.setCurrentTab(1);
>>>>>>> 332ee120e22c165763c88916ec871a326d2c9c69
 	}
 }
