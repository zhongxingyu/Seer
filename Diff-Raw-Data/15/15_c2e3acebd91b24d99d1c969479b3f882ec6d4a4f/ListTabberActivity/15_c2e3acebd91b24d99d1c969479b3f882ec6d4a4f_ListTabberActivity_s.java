 package com.example.foundit;
 
 import android.app.ActionBar;
 import android.app.TabActivity;
 import android.content.Intent;
 import android.graphics.Color;
 import android.graphics.drawable.ColorDrawable;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.widget.TabHost;
 import android.widget.TabHost.TabSpec;
 
 @SuppressWarnings("deprecation")
 public class ListTabberActivity extends TabActivity {
 	 private static final String FOUND_SPEC = "Found";
 	 private static final String LOST_SPEC = "Lost";
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_list_tabber);
 		ActionBar actionBar = getActionBar();
 		actionBar.setIcon(R.drawable.foundit_final_small);
 		actionBar.setTitle("");
 		actionBar.setBackgroundDrawable(new ColorDrawable(Color.rgb(117, 150, 194)));
		TabHost tabHost = getTabHost();
 		
 		TabSpec foundSpec = tabHost.newTabSpec(FOUND_SPEC);
 	     // Tab Icon
 	     foundSpec.setIndicator(FOUND_SPEC, getResources().getDrawable(R.drawable.ic_launcher));
 	    
 		 Intent foundIntent = new Intent(this, AllListActivity.class);
 	        // Tab Content
 	     foundSpec.setContent(foundIntent);
 	     
 	     
 	     TabSpec lostSpec = tabHost.newTabSpec(LOST_SPEC);
 	     // Tab Icon
 	     lostSpec.setIndicator(LOST_SPEC, getResources().getDrawable(R.drawable.ic_launcher));
 	    
 		 Intent lostIntent = new Intent(this, LostListActivity.class);
 	        // Tab Content
 	     lostSpec.setContent(lostIntent);
 	    // tabHost.getTabWidget().setStripEnabled(true);
 	    // tabHost.getTabWidget().setLeftStripDrawable(R.drawable.blah);
 	     //tabHost.getTabWidget().setRightStripDrawable(R.drawable.blah);
 	   //  tabHost.getTabWidget().setRightStripDrawable(R.drawable.tab_unselected_pressed_example);
 	     tabHost.addTab(foundSpec);
 	     //tabHost.getTabWidget().getChildAt(1).setBackgroundColor(Color.rgb(228, 228, 228));
 	     tabHost.addTab(lostSpec);
 	     //tabHost.getTabWidget().getChildAt(tabHost.getCurrentTab()).setBackgroundColor(R.drawable.blah);
 	     //tabHost.
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_list_tabber, menu);
 		return true;
 	}
 	public boolean onMenuItemSelected(int featureId, MenuItem item) {
	    Intent refresh = new Intent(this, ListTabberActivity.class);
 	    startActivity(refresh);
 	    finish();
 	    return true;
 
 	}
 
 }
