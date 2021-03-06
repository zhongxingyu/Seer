 package com.sunlightlabs.android.congress;
 
 import android.app.TabActivity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Window;
 import android.widget.TabHost;
 
 import com.sunlightlabs.android.congress.utils.Utils;
 
 public class FloorUpdateTabs extends TabActivity {
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		setContentView(R.layout.tabs_plain);
 
 		setupControls();
 		setupTabs();
 	}
 
 	private void setupControls() {
 		Utils.setTitle(this, R.string.floor_updates_title, R.drawable.floor);
 	}
 
 	private void setupTabs() {
 		TabHost tabHost = getTabHost();
 
 		Utils.addTab(this, tabHost, "floor_updates_house", floorIntent("house"), R.string.tab_house);
 		Utils.addTab(this, tabHost, "floor_updates_senate", floorIntent("senate"), R.string.tab_senate);

		tabHost.setCurrentTab(0);
 	}
 
 	private Intent floorIntent(String chamber) {
 		return new Intent(this, FloorUpdateList.class)
 			.putExtra("chamber", chamber);
 	}
 }
