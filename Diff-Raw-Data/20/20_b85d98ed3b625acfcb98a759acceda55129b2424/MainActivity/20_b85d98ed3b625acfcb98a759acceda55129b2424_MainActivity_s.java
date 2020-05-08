 package com.kristyandkyle.walkabout.ui;
 
 import com.actionbarsherlock.app.ActionBar;
 import com.actionbarsherlock.app.ActionBar.Tab;
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 
 import android.content.Intent;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentTransaction;
import android.util.Log;
 import android.widget.Toast;
 
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 import com.kristyandkyle.walkabout.R;
 import com.kristyandkyle.walkabout.ui.home.FeedFragment;
 import com.kristyandkyle.walkabout.ui.home.HistoryFragment;
 import com.kristyandkyle.walkabout.ui.paths.PathsActivity;
 
 public class MainActivity extends SherlockFragmentActivity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 				
 		ActionBar actionBar = getSupportActionBar();
 		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
 				
 		Tab tab = actionBar.newTab()
 					.setText("Feed")
 					.setTabListener(new TabListener<FeedFragment>(this, "feed", FeedFragment.class));
 		
 		actionBar.addTab(tab);
 		
 		tab = actionBar.newTab()
 				.setText("History")
 				.setTabListener(new TabListener<HistoryFragment>(this, "history", HistoryFragment.class));
 		
 		actionBar.addTab(tab);
 		
 		tab = actionBar.newTab()
 				.setText("Leaders")
				.setTabListener(new TabListener<FeedFragment>(this, "leaders", FeedFragment.class));
 		
 		actionBar.addTab(tab);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getSupportMenuInflater();
 		inflater.inflate(R.menu.home, menu);
 		
 		return super.onCreateOptionsMenu(menu);
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 			case android.R.id.home:
 				Toast.makeText(this, "Tapped home", Toast.LENGTH_SHORT).show();
 				break;
 				
 			case R.id.content_new:
 				Intent intent = new Intent(getApplicationContext(), PathsActivity.class);
 				startActivity(intent);
 				break;
 	
 			default:
 				break;
 		}
 	
 		return super.onOptionsItemSelected(item);
 	}
 
 	public static class TabListener<T extends Fragment> implements ActionBar.TabListener {
 
 		private Fragment mFragment;
 		private final FragmentActivity mActivity;
 		private final String mTag;
 		private final Class<T> mClass;
 		
 		public TabListener(FragmentActivity activity, String tag, Class<T> clz) {
 			mActivity = activity;
 			mTag = tag;
 			mClass = clz;
 		}
 		
 		@Override
 		public void onTabSelected(Tab tab, FragmentTransaction ft) {
 			// check if the fragment is already initialized			
 			mFragment = mActivity.getSupportFragmentManager().findFragmentByTag(mTag);
 			
 			if (mFragment == null) {
 				mFragment = Fragment.instantiate(mActivity, mClass.getName());
 				ft.add(android.R.id.content, mFragment, mTag);
 			} else {
 				// if it exists, simply attach it in order to show it
 				ft.attach(mFragment);
 			}
 		}
 
 		@Override
 		public void onTabUnselected(Tab tab, FragmentTransaction ft) {
 			if (mFragment != null) {
 				// Detach the fragment, because another one is being attached
 				ft.detach(mFragment);
 			}
 		}
 
 		@Override
 		public void onTabReselected(Tab tab, FragmentTransaction ft) {
 			// User selected the already selected tab. Usually do nothing			
 		}
 		
 	}
 
 }
