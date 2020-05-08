 package com.returnondevelopment.letters;
 
 import android.app.ActionBar;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentStatePagerAdapter;
 import android.util.Log;
 
 public class MyAdapter extends FragmentStatePagerAdapter {
 	
 	static final int ITEMS = 100;
 	static String current_page = "";
 	static int current_page_number = -1;
 
 	public MyAdapter(FragmentManager fragmentManager, String page) {
 		super(fragmentManager);
 		current_page = page;
 	}
 	
 	@Override
 	public int getCount() {
 		return ITEMS;			
 	}
 	
 	@Override
 	public Fragment getItem(int position) {
 		Log.d("getItem", Integer.toString(position));
 		
 		if(current_page_number == -1) {
 			// first load
 			current_page_number = 1;
 		} else {
 			current_page_number = position;			
 		}
 				
 		Fragment fragment = new DummySectionFragment(current_page, current_page_number);		
 		
 		//
 		// hok, can i somehow get the action bar from here?
 		//
 					
 		//args.putInt(DummySectionFragment.current_page, position + 1);
 		//fragment.setArguments(args);									
 		return fragment;
 	}
 }
 
