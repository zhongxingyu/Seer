 package com.matze5800.paupdater;
 
 import android.app.Fragment;
 import android.app.FragmentManager;
 import android.support.v13.app.FragmentPagerAdapter;
 
 
 
 import com.matze5800.paupdater.fragments.ChangelogFragment;
 import com.matze5800.paupdater.fragments.SettingsFragment;
 import com.matze5800.paupdater.fragments.UpdateFragment;
 
 public class AppSectionsPagerAdapter extends FragmentPagerAdapter {
 	
	private final String[] titles = { "Update", "Changlog", "Preferences" };
 
 	public AppSectionsPagerAdapter(FragmentManager fm) {
 		super(fm);
 	}
 
 	@Override
 	public Fragment getItem(int i) {
 		switch (i) {
 		case 0:
 			return new UpdateFragment();
 			
 		case 1:
 			return new ChangelogFragment();
 			
 		case 2:
 			return new SettingsFragment();
 
 		default:
 //			Dummy
 			return new UpdateFragment();
 		}
 	}
 
 	@Override
 	public int getCount() {
 		return 3;
 	}
 
 	@Override
 	public CharSequence getPageTitle(int position) {
 		return titles[position];
 	}
 }
