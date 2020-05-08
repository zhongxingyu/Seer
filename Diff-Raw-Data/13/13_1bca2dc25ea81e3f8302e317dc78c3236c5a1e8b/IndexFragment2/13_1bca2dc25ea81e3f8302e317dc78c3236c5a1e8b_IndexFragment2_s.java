 package com.mimee.ui;
 
 import android.content.Context;
import android.content.res.Configuration;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
 import android.support.v4.view.ViewPager;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 
 import com.astuetz.viewpager.extensions.PagerSlidingTabStrip;
 import com.mimee.R;
 
 
 public class IndexFragment2 extends Fragment {
 	
 	private final static String KEY_SAVED_STATE = "tab";
 	private PagerSlidingTabStrip mTabs;
 	private ViewPager mPager;
 	private TabAdapter mAdapter;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		// TODO Auto-generated method stub
 		super.onCreate(savedInstanceState);
 	}
 	
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		super.onCreateView(inflater, container, savedInstanceState);
 		View root = inflater.inflate(R.layout.fragment_index2, container, false);
 		
 		mTabs = (PagerSlidingTabStrip) root.findViewById(R.id.tabs);
 		mPager = (ViewPager) root.findViewById(R.id.pager);
 		mAdapter = new TabAdapter(this.getActivity());
 		
 		mPager.setAdapter(mAdapter);
 		mTabs.setViewPager(mPager);
 		
 		if(savedInstanceState != null) 
 			mTabs.onRestoreInstanceState(savedInstanceState);
 		
 		return root;
 	}
 	
 	@Override
 	public void onSaveInstanceState(Bundle outState) {
 		super.onSaveInstanceState(outState);
 		outState = (Bundle) mTabs.onSaveInstanceState();
 	}
 	
	public static class TabAdapter extends FragmentPagerAdapter {
 		
 		private final static String[] TITLES = {"My Invited", "Top Heat", "Look Around", "City"};
 		private Class<?>[] fragments = new Class<?>[] {
 				ActivityListFragment.class, 
 				StubFragment.class, 
 				StubFragment.class, 
 				StubFragment.class
 		};
 		private Context mContext;
 
 		public TabAdapter(FragmentActivity activity) {
 			super(activity.getSupportFragmentManager());
 			mContext = activity;
 		}
 
 		@Override
 		public Fragment getItem(int position) {
 			return Fragment.instantiate(mContext, fragments[position].getName(), 
 					createBundle(TITLES[position]));
 		}
 
 		@Override
 		public int getCount() {
 			return TITLES.length;
 		}
 		
 		@Override
 		public CharSequence getPageTitle(int position) {
 			return TITLES[position];
 		}
 		
 		private Bundle createBundle(String name) {
 			Bundle args = new Bundle();
 			args.putString("name", name);
 			return args;
 		}
 	}
 }
