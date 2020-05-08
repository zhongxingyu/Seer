 package com.main.passthedoodle;
 
 import android.os.Bundle;
 import android.content.Intent;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentPagerAdapter;
 import android.support.v4.view.ViewPager;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.TextView;
 
 public class MainActivity extends FragmentActivity {
     
 	ViewPager mViewPager;
 	MainPagerAdapter mMainPagerAdapter;
 	
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
         mMainPagerAdapter = new MainPagerAdapter(getSupportFragmentManager());
         mViewPager = (ViewPager) findViewById(R.id.main_pager);
         mViewPager.setAdapter(mMainPagerAdapter);
         mViewPager.setCurrentItem(1); // start in the center tab
     }
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.main, menu);
         return true;
     }
     
     public class MainPagerAdapter extends FragmentPagerAdapter {
     	public MainPagerAdapter(FragmentManager fm) {
             super(fm);
         }
     	@Override
         public Fragment getItem(int i) {
             switch (i) {
                 case 0:
                     return new CreateFragment();
                 case 1:
                 	return new HomeFragment();
                 case 2:
                 	return new BrowseFragment();
                 default:
                     return new CreateFragment();
             }
         }
         @Override
         public int getCount() {
             return 3;
         }
         @Override
         public CharSequence getPageTitle(int position) {
         	if (position == 0)
         		return "Create";
         	else if (position == 1)
         		return "Home";
         	else if (position == 2)
         		return "Browse";
         	else
         		return "gtfo";
         }
     }
 
     public static class HomeFragment extends Fragment {
     	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
     		View rootView = inflater.inflate(R.layout.fragment_home, container, false);
     		
     		rootView.findViewById(R.id.button_login)
             			.setOnClickListener(new View.OnClickListener() {
             				@Override
 				    		public void onClick(View view) {
 				                Intent intent = new Intent(getActivity(), LoginActivity.class);
 				                startActivity(intent);
 				            }
             			});
     		rootView.findViewById(R.id.button_register)
 						.setOnClickListener(new View.OnClickListener() {
 							@Override
 				    		public void onClick(View view) {
 				                Intent intent = new Intent(getActivity(), RegisterActivity.class);
 				                startActivity(intent);
 				            }
 						});
     		rootView.findViewById(R.id.button_guest)
 						.setOnClickListener(new View.OnClickListener() {
 							@Override
 				    		public void onClick(View view) {
 				                Intent intent = new Intent(getActivity(), RegisterActivity.class);
 				                startActivity(intent);
 				            }
 						});
     		return rootView;
     	}
     }
     
     public static class CreateFragment extends Fragment {
     	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
     		View rootView = inflater.inflate(R.layout.fragment_create, container, false);
     		
     		rootView.findViewById(R.id.button_paint)
 						.setOnClickListener(new View.OnClickListener() {
 							@Override
 				    		public void onClick(View view) {
 				                Intent intent = new Intent(getActivity(), DrawingActivity.class);
 				                startActivity(intent);
 				            }
 						});
 			rootView.findViewById(R.id.button_guess)
 						.setOnClickListener(new View.OnClickListener() {
 							@Override
 				    		public void onClick(View view) {
 				                Intent intent = new Intent(getActivity(), TextActivity.class);
 				                startActivity(intent);
 				            }
 						});
 			rootView.findViewById(R.id.button_local_play)
 						.setOnClickListener(new View.OnClickListener() {
 							@Override
 				    		public void onClick(View view) {
 				                Intent intent = new Intent(getActivity(), DrawingActivity.class);
 				                intent.putExtra("isLocal", true);
 				                startActivity(intent);
 				            }
 						});
 			return rootView;
     	}
     }
     
     public static class BrowseFragment extends Fragment {
     	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
     		View rootView = inflater.inflate(R.layout.fragment_browse, container, false);
     		((TextView) rootView.findViewById(R.id.textview_placeholder)).setText("COOL SHIET HERE");
     		return rootView;
     	}
     }
<<<<<<< HEAD
     
     public void goGame(View view) {
         Intent intent = new Intent(this, GameActivity.class);
         startActivity(intent);
     }
=======
}
>>>>>>> ba29e793c85995bc8a560ee39ee612913b58f376

 
