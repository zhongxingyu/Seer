 package edu.dartmouth.mhb.MenuFragments;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.Context;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
 import android.support.v4.view.PagerAdapter;
 import android.support.v4.view.ViewPager;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import edu.dartmouth.mhb.Hymn;
 import edu.dartmouth.mhb.R;
 import edu.dartmouth.mhb.SlidePageFragment;
 
 public class MenuHymnsFragment extends Fragment {
 	private ArrayList<Hymn> hymns;
 	private ViewPager mPager;
 	private PagerAdapter mPagerAdapter;
 	
 	public static Fragment newInstance(Context context) {
 		MenuHymnsFragment f = new MenuHymnsFragment();
 		return f;
 	}
 
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		ViewGroup root = (ViewGroup) inflater.inflate(
 				R.layout.fragment_menu_hymns, null);
 
 		hymns = getArguments().getParcelableArrayList("hymn_list");
 		
 		 List<Fragment> fragments = getFragments();
 		 // Instantiate a ViewPager and a PagerAdapter.
 		 mPager = (ViewPager) root.findViewById(R.id.pager);
 		 mPagerAdapter = new SlidePageAdapter(getFragmentManager(),
 		 fragments);
 		 mPager.setAdapter(mPagerAdapter);
 		
 		return root;
 	}
 	
 	
 	private List<Fragment> getFragments() {
 		List<Fragment> fList = new ArrayList<Fragment>();
 		int no_hymns = hymns.size();
 		for (int i = 0; i < no_hymns; i++) {
 			fList.add(SlidePageFragment.newInstance(hymns.get(i)));
 
 		}
 		return fList;
 	}
 	
	private class SlidePageAdapter extends FragmentStatePagerAdapter {
 		private List<Fragment> fragments;
 
 		public SlidePageAdapter(FragmentManager fm, List<Fragment> fragments) {
 			super(fm);
 			this.fragments = fragments;
 		}
 
 		@Override
 		public Fragment getItem(int position) {
 			return this.fragments.get(position);
 		}
 
 		@Override
 		public int getCount() {
 			return this.fragments.size();
 		}
 	}
 }
