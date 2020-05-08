 package com.android.nitelights;
 
 import android.app.ActionBar;
 import android.content.Intent;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentStatePagerAdapter;
 import android.support.v4.view.ViewPager;
 import android.view.LayoutInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.TextView;
 
 
 public class CollectionDemoActivity extends FragmentActivity {
 
 	DemoCollectionPagerAdapter mDemocollectionPagerAdapter;
 	
 	ViewPager mViewPager;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_collection_demo);
 		
 		mDemocollectionPagerAdapter = new DemoCollectionPagerAdapter(getSupportFragmentManager());
 		
 		final ActionBar actionBar = getActionBar();
 		
 		//show the home button
 		actionBar.setDisplayHomeAsUpEnabled(true);
 		
 		//set up the ViewPager, attaching the adapter.
 		mViewPager = (ViewPager) findViewById(R.id.pager);
 		mViewPager.setAdapter(mDemocollectionPagerAdapter);
 	}
 	
 	//This hook is called whenever an item in your opetions menu is selected
	public boolean onOptionsItemSelected(MenuItem item) {
 		
 		switch(item.getItemId()){
 		case android.R.id.home:
				Intent upIntent = new Intent(this, MainActivity.class);
				startActivity(upIntent);
				break;
 		}
		return true;
 	}
 	
 	/**
 	 * returns a fragment representing an object in the collection
 	 * @author Lenovo
 	 *
 	 */
 	public static class DemoCollectionPagerAdapter extends FragmentStatePagerAdapter {
 
 		public DemoCollectionPagerAdapter(FragmentManager fm) {
 			super(fm);
 		}
 		
 		public int getCount(){
 			return 100;
 		}
 		
 		public CharSequence getPageTitle(int position){
 			return "OBJECT " + (position + 1);
 		}
 
 		@Override
 		public Fragment getItem(int i) {
 			Fragment fragment = new DemoObjectFragment();
 			Bundle args = new Bundle();
 			args.putInt(DemoObjectFragment.ARG_OBJECT, i+1);
 			fragment.setArguments(args);
 			return fragment;
 		}
 	}
 	
 	/**
 	 * a dummy fragment representing a section of the app, but that simply displays dummy text
 	 */
 	public static class DemoObjectFragment extends Fragment {
 		public static final String ARG_OBJECT = "object";
 		
 		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
 			View rootView = inflater.inflate(R.layout.fragment_collection_object, container, false);
 			Bundle args = getArguments();
 			((TextView) rootView.findViewById(android.R.id.text1)).setText(
 					Integer.toString(args.getInt(ARG_OBJECT)));
 			return rootView;
 		}
 	}
 }
