 package com.minion.timemanagement;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.app.Fragment;
 import android.app.FragmentManager;
 import android.app.FragmentTransaction;
 import android.content.Intent;
 import android.support.v4.widget.DrawerLayout;
 import android.view.Menu;
 import android.view.View;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 
 public class MainActivity extends Activity {
 	private String[] navigationList;
 	private DrawerLayout drawerLayout;
 	private ListView drawerList;
 	
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
         
        // Create a new fragment
        Fragment fragment = new HomeFragment();	
 		
		//Insert the fragment by replacing any existing fragment
 		FragmentManager fragmentManager = getFragmentManager();
		fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).addToBackStack(null).commit();
         
         navigationList = getResources().getStringArray(R.array.navigation_list);
         drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
         drawerList = (ListView) findViewById(R.id.left_drawer);
         
         //Set adapter for list view
         drawerList.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, navigationList));
         
         //Set the list's click listener
         drawerList.setOnItemClickListener(new DrawerItemClickListener());
     }
 
     private class DrawerItemClickListener implements OnItemClickListener {
     	@Override
     	public void onItemClick(AdapterView parent, View view, int position, long id) {
     		selectItem(position);
 
     	}
     }
     	//Swap fragments in the main content view
 		private void selectItem(int position) {
 			Fragment fragment;
 			FragmentManager fragmentManager = getFragmentManager();
 			
 			/*
 			 * for each case, you create a new fragment and use it to replace the old one
 			 * case numbers represent the items in the navigation drawer (starting from the top)
 			 */
 			switch (position) {
 			case 0:
 				fragment = new HomeFragment();
 				fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).addToBackStack(null).commit();
 				break;
 
 			case 1:
 				fragment = new TimerFragment();
 				fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).addToBackStack(null).commit();
 				break;
 				
 			case 2:
 				fragment = new EnterMissedTimeFragment();
 				fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).addToBackStack(null).commit();
 				break;
 			
 			case 3:
 				fragment = new CategoryFragment();
 				fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).addToBackStack(null).commit();
 				break;
 			
 			case 4:
 				fragment = new StatisticsFragment();
 				fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).addToBackStack(null).commit();
 				break;
 			}
 			
 			//Highlight the selected item, update the title, and close the drawer
 			drawerList.setItemChecked(position, true);
 			drawerLayout.closeDrawer(drawerList);
 		}
 
  
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.main, menu);
         return true;
     }
     
 }
