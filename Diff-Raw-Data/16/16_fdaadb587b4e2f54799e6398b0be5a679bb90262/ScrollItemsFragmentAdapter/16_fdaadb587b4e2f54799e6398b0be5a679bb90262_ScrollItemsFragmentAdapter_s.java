 package com.skybot.adapters;
 
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentPagerAdapter;
 
 import com.skybot.activities.ScrollItemsFragment;
 
 /**
  * Fulfilling Fragment with Chart objects to have a scroll control
  * 
  * @author gor, armenabrahamyan
  * 
  */
 public class ScrollItemsFragmentAdapter extends FragmentPagerAdapter {
 
 	final int PAGE_COUNT = 4;
 	/*public ArrayList<HashMap<String, String>> data;*/
 
 	/** Constructor of the class */
 	public ScrollItemsFragmentAdapter(FragmentManager fm) {
 		super(fm);
 	}
 
 	/** This method will be invoked when a page is requested to create */
 	@Override
 	public Fragment getItem(int arg0) {
 		
 		ScrollItemsFragment myFragment = new ScrollItemsFragment();
 		
 		//myFragment.data = data;
 		Bundle data = new Bundle();
 		data.putInt("current_page", arg0 + 1);
 		myFragment.setArguments(data);
 		return myFragment;
 	}
 
 	/** Returns the number of pages */
 	@Override
 	public int getCount() {
 		return PAGE_COUNT;
 	}
 }
