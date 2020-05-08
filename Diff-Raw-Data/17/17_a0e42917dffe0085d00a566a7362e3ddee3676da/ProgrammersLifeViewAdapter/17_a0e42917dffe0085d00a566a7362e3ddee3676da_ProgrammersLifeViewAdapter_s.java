 package com.pc.programmerslife.adapters;
 
 import com.pc.programmerslife.R;
 import com.pc.programmerslife.fragments.CommicsFragment;
 import com.pc.programmerslife.fragments.FavoritesFragment;
 import com.pc.programmerslife.fragments.InformationsFragment;
 import com.pc.programmerslife.fragments.TwitterFragment;
import com.viewpagerindicator.TitleProvider;
 
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentStatePagerAdapter;
 
public class ProgrammersLifeViewAdapter extends FragmentStatePagerAdapter implements TitleProvider {
	private FragmentActivity fragmentActivity;
 	
 	public ProgrammersLifeViewAdapter(FragmentActivity fragmentActivity) {
 		super(fragmentActivity.getSupportFragmentManager());
 		
		this.fragmentActivity = fragmentActivity;
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
	public String getTitle(int position) {
		return fragmentActivity.getResources().getStringArray(R.array.titles)[position];
 	}
 }
