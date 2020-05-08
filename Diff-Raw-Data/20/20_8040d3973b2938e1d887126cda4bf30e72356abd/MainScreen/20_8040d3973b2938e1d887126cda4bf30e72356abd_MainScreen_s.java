 package com.allplayers.android;
 
 import android.app.TabActivity;
 import android.content.Intent;
 import android.content.res.Resources;
 import android.os.Bundle;
 import android.widget.TabHost;
 
 public class MainScreen extends TabActivity
 {
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.inapplayout);
 		
 		Resources res = getResources(); // Resource object to get Drawables, this will be little icons for each one
 		TabHost tabHost = getTabHost();  // The activity TabHost
 		TabHost.TabSpec spec;  // Reusable TabSpec for each tab
 		Intent intent;  // Reusable Intent for each tab
 		
 		// Create an Intent to launch an Activity for the tab (to be reused)
 		intent = new Intent().setClass(this, GroupsActivity.class); //set as GroupsActivity.class, this will be changed to
 		//whatever we end up calling that particular class
 		
 		// Initialize a TabSpec for each tab and add it to the TabHost
		spec = tabHost.newTabSpec("groups").setIndicator("Groups", res.getDrawable(R.drawable.ic_tab_groups)).setContent(intent);
 		tabHost.addTab(spec);
 		
 		// Do the same for the other tabs
		/*intent = new Intent().setClass(this, MailActivity.class);
		spec = tabHost.newTabSpec("mail").setIndicator("Mail")
				//,res.getDrawable(R.drawable.ic_tab_mail))
				.setContent(intent);
 		tabHost.addTab(spec);
 		
		intent = new Intent().setClass(this, AccountActivity.class);
 		spec = tabHost.newTabSpec("account").setIndicator("Account")
 				//,res.getDrawable(R.drawable.ic_tab_account))
 				.setContent(intent);
		tabHost.addTab(spec);
 		
		tabHost.setCurrentTab(1);*/
 	}
 }
