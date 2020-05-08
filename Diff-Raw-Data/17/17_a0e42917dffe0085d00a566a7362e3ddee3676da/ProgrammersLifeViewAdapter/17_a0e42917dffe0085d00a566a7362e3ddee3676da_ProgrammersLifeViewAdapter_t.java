 package com.pc.programmerslife.adapters;
 
 import com.pc.programmerslife.R;
 import com.pc.programmerslife.fragments.CommicsFragment;
 import com.pc.programmerslife.fragments.FavoritesFragment;
 import com.pc.programmerslife.fragments.InformationsFragment;
 import com.pc.programmerslife.fragments.TwitterFragment;
 
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentStatePagerAdapter;
 
public class ProgrammersLifeViewAdapter extends FragmentStatePagerAdapter {
	private String[] titles;
 	
 	public ProgrammersLifeViewAdapter(FragmentActivity fragmentActivity) {
 		super(fragmentActivity.getSupportFragmentManager());
 		
		titles = fragmentActivity.getResources().getStringArray(R.array.titles);
 	}
 
 	@Override
 	public Fragment getItem(int position) {
 		if (position == 0)
 			return new CommicsFragment();
 		else if (position == 1)
 			return new FavoritesFragment();
 		else if (position == 2)
 			return new TwitterFragment();
 		else if (position == 3)
 			return new InformationsFragment();
 		return null;
 	}
 
 	@Override
 	public int getCount() {
 		return 4;
 	}
	
 	@Override
	public CharSequence getPageTitle(int position) {
		return titles[position];
 	}
 }
