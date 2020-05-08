 package phillykeyspots.frpapp;
 
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentPagerAdapter;
 import android.support.v4.view.ViewPager;
 
 public class DashboardActivity extends FragmentActivity {
 	
 	private FinderFragment finder = new FinderFragment();
 	private EventsFragment events = new EventsFragment();
 	private ResourcesFragment resources = new ResourcesFragment();
 	private JomlFragment joml = new JomlFragment();
 	SectionsPagerAdapter mSectionsPagerAdapter;
 	ViewPager mViewPager;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_dashboard);
 		
 		mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
 
 		mViewPager = (ViewPager) findViewById(R.id.fragment_container);
 		mViewPager.setAdapter(mSectionsPagerAdapter);
 		
 		switch(Integer.parseInt(getIntent().getExtras().get("ID").toString())){
 		case R.id.b_finder:
 			mViewPager.setCurrentItem(0);
 			break;
 		case R.id.b_events:
 			mViewPager.setCurrentItem(1);
 			break;
 		case R.id.b_resources:
 			mViewPager.setCurrentItem(2);
 			break;
 		case R.id.b_joml:
 			mViewPager.setCurrentItem(3);
 			break;
 		}
 	}
 	public class SectionsPagerAdapter extends FragmentPagerAdapter {
 
 		public SectionsPagerAdapter(FragmentManager fm) {
 			super(fm);
 		}
 
 		@Override
 		public Fragment getItem(int position) {
 			switch(position){
 			case(0):
 				return finder;
 			case(1):
 				return events;
 			case(2):
 				return resources;
 			case(3):
 				return joml;
 			}
 			return null;
 		}
 
 		@Override
 		public int getCount() {
 			return 4;
 		}
 
 		@Override
 		public CharSequence getPageTitle(int position) {
 			switch (position) {
 			case 0:
 				return getString(R.string.title_activity_search_for_keyspot).toUpperCase();
 			case 1:
 				return getString(R.string.title_activity_find_events).toUpperCase();
 			case 2:
 				return getString(R.string.title_activity_find_resources).toUpperCase();
 			case 3:
 				return getString(R.string.title_activity_join_our_mailing_list).toUpperCase();
 			}
 			return null;
 		}
 	}
 
 }
