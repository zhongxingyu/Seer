 package com.returnondevelopment.letters;
 
 import android.app.ActionBar;
 import android.app.ActionBar.OnNavigationListener;
 import android.app.FragmentTransaction;
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.view.ViewPager;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.webkit.WebView;
 import android.widget.ArrayAdapter;
 
 public class Letters extends FragmentActivity implements ActionBar.TabListener, OnNavigationListener {
 	
 	MyAdapter mAdapter;
 	ViewPager mPager;
 	int selected_id = 0;
 	String current_page = "";
 	int current_page_number = 1;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_letters);
 
 		mAdapter = new MyAdapter(getSupportFragmentManager(), current_page);		
		
 		mPager = (ViewPager)findViewById(R.id.pager);
 		mPager.setAdapter(mAdapter);
 				
		
		
 		// Set up the action bar.
 		final ActionBar actionBar = getActionBar();
 		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);    
 	    	    
 	    ArrayAdapter<CharSequence> dropDownAdapter = ArrayAdapter.createFromResource(this, R.array.action_list, android.R.layout.simple_list_item_1);
 	    
 		actionBar.setListNavigationCallbacks(dropDownAdapter, 
 				new OnNavigationListener() {
 
 					@Override
 					public boolean onNavigationItemSelected(int spinner_position, long arg1) {
 												
 						
 						// RECREATE
 						// THE 
 						// FRAGMENT
 
 					    switch (spinner_position) {
 				        case 0:	        	
 				        	current_page = "";
 //				        	dummyWebView.loadUrl("http://www.letterstocrushes.com/mobile/page/1");	        		  
 				        	break;
 				        case 1:
 				        	current_page = "more/";					        	
 //				        	dummyWebView.loadUrl("http://www.letterstocrushes.com/mobile/more/page/1");	        		        	
 				        	break;
 				        case 2:
 				        	current_page = "bookmarks";					        	
 //				        	dummyWebView.loadUrl("http://www.letterstocrushes.com/mobile/bookmarks");  	
 //				        	break;
 				        case 3:
 				        	current_page = "search";
 //				        	dummyWebView.loadUrl("http://www.letterstocrushes.com/mobile/search");  	
 //				        	break;
 //				        case 4:
 //				        	// switch to the send screen...
 				    }
 //				    
					    						
 						mAdapter = new MyAdapter(getSupportFragmentManager(), current_page);		
 						mPager = (ViewPager)findViewById(R.id.pager);
 						mPager.setAdapter(mAdapter);
 						
						mPager.setCurrentItem(1);
 																																
					    Log.d("CurrentPage", current_page + " - " + Integer.toString(spinner_position) + " - /" + mAdapter.getCount());
 
 					    return true;
 												
 					}
 			
 				
 			});
 			    
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.letters, menu);		
 		return true;
 	}
 
 	@Override
 	public void onTabSelected(ActionBar.Tab tab,
 			FragmentTransaction fragmentTransaction) {
 		// When the given tab is selected, switch to the corresponding page in
 		// the ViewPager.
 		mPager.setCurrentItem(tab.getPosition());
 	}
 
 	@Override
 	public void onTabUnselected(ActionBar.Tab tab,
 			FragmentTransaction fragmentTransaction) {
 	}
 
 	@Override
 	public void onTabReselected(ActionBar.Tab tab,
 			FragmentTransaction fragmentTransaction) {
 	}
 
 	
 	@Override
 	public boolean onPrepareOptionsMenu (Menu menu) {
 		super.onPrepareOptionsMenu(menu);
 	
 		// could change menu items here if i desired, 
 		// but since i can't change the icon, not going
 		// to go this route
 		return true;
 	}
 
 	@Override
 	public boolean onNavigationItemSelected(int arg0, long arg1) {
 		return false;
 	}
 		
 }
