 package org.omships.omships;
  
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentPagerAdapter;
 import android.support.v4.view.ViewPager;
 import android.util.SparseArray;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
  
 public class MainActivity extends FragmentActivity {
 	
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
         ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
         viewPager.setAdapter(new SampleFragmentPagerAdapter(getSupportFragmentManager()));
     }
  
     static public class SampleFragmentPagerAdapter extends FragmentPagerAdapter {
     	static final String ARG_PAGE = "ARG_PAGE";
     	static final String[] tabNames =
     		{"Map", "News/PrayerRequests", "Media","Webcams","Port Schedule"};
     	FragmentManager fm;
     	
         public SampleFragmentPagerAdapter(FragmentManager fm) {
 			super(fm);
 			this.fm=fm;
 		}
 
 		static final int PAGE_COUNT = 5;
         private SparseArray<Fragment> frags =
         		new SparseArray<Fragment>(PAGE_COUNT);        
  
         @Override
         public int getCount() {
             return PAGE_COUNT;
         }
  
         @Override
         public Fragment getItem(int position) {
         	if(frags.get(position)==null)
         		frags.put(position, create(position));
         	return frags.get(position);
         }
         
         @Override
         public CharSequence getPageTitle(int position) {
         	return tabNames[position];
         }
         
        public Fragment create(int page) {
             Bundle args = new Bundle();
             args.putInt(ARG_PAGE, page);
             Fragment fragment;
             switch(page){
            case 0:
             	fragment = new ShipMapFragment();
            	break;
             case 1:
             	fragment = new NewsFeedFragment();
             	break;
             case 2:
             	fragment = new MediaFeedFragment();
             	break;
             case 3:
             	fragment = new CamListFragment();
             	break;
             default:
             	fragment = new PageFragment();
             }//end switch
             fragment.setArguments(args);
             return fragment;
         }//end create
     }//end SampleFragmentPagerAdapter
  
     public static class PageFragment extends Fragment {
   
         @Override
         public void onCreate(Bundle savedInstanceState) {
             super.onCreate(savedInstanceState);
         }
  
         @Override
         public View onCreateView(LayoutInflater inflater, ViewGroup container,
                 Bundle savedInstanceState) {
             return inflater.inflate(R.layout.fragment_page, container, false);
         }
     }
 }
