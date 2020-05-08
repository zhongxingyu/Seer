 package com.playphone.sdk.example;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 import com.playphone.multinet.MNDirect;
 import com.playphone.multinet.providers.MNVItemsProvider.GameVItemInfo;
 
 public class LeaderboardHome extends CustomTitleActivity {
	
	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.leaderboard_list);
 		
 		// set the breadcrumbs text
 		TextView txtBreadCrumbs = (TextView) findViewById(R.id.txtBreadCrumbs);
 		txtBreadCrumbs.setText("Home > Leaderboards");
 				
 		
 		LinearLayout layout = (LinearLayout) findViewById(R.id.layoutLeaderboards);
 		
 		
 		List<String> leaderboards = new ArrayList<String>();
 		leaderboards.add("Default");
 		leaderboards.add("Simple");
 		leaderboards.add("Advanced");
 		
 		// for each item create a button and add it to the layout
 		for(String leaderboardName : leaderboards)
 		{
 			final String name = leaderboardName;
 			
 			Button btnLeaderboard = new Button(layout.getContext());
 			btnLeaderboard.setText(leaderboardName);
 			btnLeaderboard.setOnClickListener(new OnClickListener() {
 				
 				@Override
 				public void onClick(View v) {
 					//generate the intent for the virtual item details and pass in the item id
 					Intent intent = new Intent(LeaderboardHome.this,LeaderboardsDetailsActivity.class);
 					intent.putExtra("leaderboardID", LeaderboardHome.getLeaderboardIdForName(name));
 					startActivity(intent);
 				}
 			});
 			layout.addView(btnLeaderboard);
 		}
 		
 		
 		
 	}
 	
 	static int getLeaderboardIdForName(String name){
 		if(name.equalsIgnoreCase("default")) return 0;
 		if(name.equalsIgnoreCase("simple")) return 1;
 		if(name.equalsIgnoreCase("advanced")) return 2;
 		return -1;
 	}
 
 
 }
