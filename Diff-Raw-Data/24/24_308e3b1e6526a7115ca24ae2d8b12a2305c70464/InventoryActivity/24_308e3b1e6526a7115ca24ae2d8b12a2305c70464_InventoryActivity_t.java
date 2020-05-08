 package com.ctg.ctginventory;
 
 
 
 
 import android.app.ActionBar;
 import android.app.FragmentTransaction;
 import android.content.Intent;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.view.Gravity;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.View;
 import android.view.ViewGroup;
 
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 
 public class InventoryActivity extends FragmentActivity implements ActionBar.TabListener {
 
     private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";
     private DataSource datasource;
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         datasource = new DataSource(this);
         datasource.open();
         datasource.close();
         setContentView(R.layout.activity_inventory);
 
         // Set up the action bar.
         final ActionBar actionBar = getActionBar();
         actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
 
         // For each of the sections in the app, add a tab to the action bar.
         actionBar.addTab(actionBar.newTab().setText(R.string.title_section1).setTabListener(this));
         actionBar.addTab(actionBar.newTab().setText(R.string.title_section2).setTabListener(this));
         actionBar.addTab(actionBar.newTab().setText(R.string.title_section3).setTabListener(this));
         actionBar.addTab(actionBar.newTab().setText(R.string.title_section4).setTabListener(this));
     }
     
     
     
 
 
     @Override
     public void onRestoreInstanceState(Bundle savedInstanceState) {
         if (savedInstanceState.containsKey(STATE_SELECTED_NAVIGATION_ITEM)) {
             getActionBar().setSelectedNavigationItem(
                     savedInstanceState.getInt(STATE_SELECTED_NAVIGATION_ITEM));
         }
     }
 
     @Override
     public void onSaveInstanceState(Bundle outState) {
         outState.putInt(STATE_SELECTED_NAVIGATION_ITEM,
                 getActionBar().getSelectedNavigationIndex());
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_inventory, menu);
         return true;
     }
     
     public void onActivityResult(int requestCode, int resultCode, Intent intent) {
     	  IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
     	  if (scanResult != null) {
    	      String content = scanResult.getContents();
    	      //getAssetOrAddNew(content); //TODO
    	      //populate tab3 textview to let it display details.
     	  }
     	  // else continue with any other code you need in the method
     	  
     	}
 
     @Override
     public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
     }
 
     @Override
     public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
         // When the given tab is selected, show the tab contents in the container
         Fragment fragment = new DummySectionFragment();
         Bundle args = new Bundle();
         args.putInt(DummySectionFragment.ARG_SECTION_NUMBER, tab.getPosition() + 1);
         fragment.setArguments(args);
         getSupportFragmentManager().beginTransaction()
                 .replace(R.id.container, fragment)
                 .commit();
     }
 
     @Override
     public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
     }
     
     /**
      * A dummy fragment representing a section of the app, but that simply displays dummy text.
      */
     
     
     public static class DummySectionFragment extends Fragment {
         public DummySectionFragment() {
         }
 
         
         public static final String ARG_SECTION_NUMBER = "section_number";
         
         @Override
         public View onCreateView(LayoutInflater inflater, ViewGroup container,
                 Bundle savedInstanceState) {
         	Bundle args = getArguments();
         
             /*TextView textView = new TextView(getActivity());
             textView.setGravity(Gravity.CENTER);
             
             textView.setText(Integer.toString(args.getInt(ARG_SECTION_NUMBER)));*/
         	switch (args.getInt(ARG_SECTION_NUMBER)) {
         	case 1:
        		//list of computers + Users
         		//RelativeLayout relativeLayout = getComputersLayout();
         		break;
         	case 2:
        		//Quick Tickets View
         		
         		break;
         	case 3:
        		// Create a view from a selected item (SelectedAssetID in the view) 
         		
         		
         		break;
         	case 4:
         		IntentIntegrator integrator = new IntentIntegrator(this.getActivity());
                 integrator.initiateScan(IntentIntegrator.QR_CODE_TYPES);
                
         		break;
         	}
             
            RelativeLayout relativeLayout = new RelativeLayout(this.getActivity());//FIX this is just to prevent eclipse from complaining.
             return relativeLayout;
         }
     }
 }
