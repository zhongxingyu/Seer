 package com.typeiisoft.lct;
 
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentStatePagerAdapter;
 import android.util.Log;
 
 /**
  * This class handles the pager routines to deal with the tabs associated with 
  * the Lunar Club observing program.
  * 
  * @author Michael Reuter
  */
 public class LunarClubPagerAdapter extends FragmentStatePagerAdapter {
 	/** Logging identifier. */
 	private final static String TAG = LunarClubPagerAdapter.class.getName();
 	/** Number of tabs for the display. */
 	private final static int NUM_TABS = 3;
 	/** The set of titles for the Lunar Club tabs */
	private String[] tabTitles = {"Naked Eye", "Binocular", "Telescope"};
 	
 	/**
 	 * Class constructor.
 	 * @param fm : The handle for the FragmentManager.
 	 */
 	public LunarClubPagerAdapter(FragmentManager fm) {
 		super(fm);
 	}
 
 	/**
 	 * This function retrieves a fragment at a given index.
 	 * @param position : The index for a fragment.
 	 * @return : The fragment at the given index.
 	 */
 	@Override
 	public Fragment getItem(int position) {
 		Log.i(TAG, "Creating tab at position " + String.valueOf(position));
 		return LunarClubFeaturesFragment.newInstance(this.tabTitles[position]);
 	}
 
 	/**
 	 * This function to get the number of total number of tabs.
 	 * @return : The number of tabs in the pager.
 	 */
 	@Override
 	public int getCount() {
 		return NUM_TABS;
 	}
 
 	/**
 	 * The function to put a title on the current page.
 	 * @param position : The index for the title.
 	 * @return : The title for the page.
 	 */
     @Override
     public CharSequence getPageTitle(int position) {
         return this.tabTitles[position];
     }
 }
