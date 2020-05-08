 package com.TweetGet.Activites;
 
 import android.content.res.Configuration;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentTransaction;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.SearchView;
 
 import com.TweetGet.R;
 import com.TweetGet.Adapters.TweetListAdapter;
 import com.TweetGet.Fragments.MainFeedFragment;
 import com.TweetGet.Fragments.TimelineFragment;
 import com.TweetGet.Managers.DrawerManager;
 
 public class MainActivity extends FragmentActivity {
 
 	public static TweetListAdapter mTweetListAdapter;
 	private DrawerManager mDrawerManager;
 
 	private static final class FragmentTags {
 		public static final String HASHTAG_FRAGMENT = "HASHTAG";
 		public static final String USER_FRAGMENT = "USER";
 	}
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 
 		mDrawerManager = new DrawerManager(this, getSupportFragmentManager());
 
 	}
 
 	public boolean onOptionsItemSelected(MenuItem item) {
 
 		if (mDrawerManager.onOptionsItemSelected(item)) {
 			return true;
 		}
 
 		switch (item.getItemId()) {
 		case R.id.action_settings:
 			PreferenceActivity.newInstance(this);
 			break;
 
 		}
 		return true;
 	}
 
 	public void onCreateContextMenu(ContextMenu menu, View v,
 			ContextMenuInfo menuInfo) {
 		super.onCreateContextMenu(menu, v, menuInfo);
 		MenuInflater inflate = getMenuInflater();
 		inflate.inflate(R.menu.context, menu);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.main, menu);
 		SearchView searchView = (SearchView) menu.findItem(R.id.search)
 				.getActionView();
 
 		setSearchListener(searchView);
 
 		return true;
 	}
 
 	private void setSearchListener(SearchView searchView) {
 
 		searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
 			@Override
 			public boolean onQueryTextChange(String newText) {
 				return true;
 			}
 
 			@Override
 			public boolean onQueryTextSubmit(String query) {
 
 				Bundle data = new Bundle();
				data.putString("query", query.trim());
 				Fragment frag = mDrawerManager.getSelectedFragment();
 				String tag = "";
 
 				if (frag instanceof MainFeedFragment) {
 					frag = new MainFeedFragment();
 					tag = FragmentTags.HASHTAG_FRAGMENT;
 
 				} else if (frag instanceof TimelineFragment) {
 					frag = new TimelineFragment();
 					tag = FragmentTags.USER_FRAGMENT;
 
 				}
 				if (frag != null) {
 					frag.setArguments(data);
					FragmentManager fm = getSupportFragmentManager();
 					FragmentTransaction ft = fm.beginTransaction();
 					ft.add(R.id.content_frame, frag, tag);
 					Fragment fragment = fm.findFragmentByTag(tag);
 					ft.remove(fragment);
					ft.addToBackStack(tag);
 					ft.commit();
 
 				}
 				return true;
 			}
 		});
 	}
 
 	@Override
 	public void setTitle(CharSequence title) {
 		getActionBar().setTitle(title);
 	}
 
 	@Override
 	protected void onPostCreate(Bundle savedInstanceState) {
 		super.onPostCreate(savedInstanceState);
 		mDrawerManager.getActionBarDrawerToggle().syncState();
 	}
 
 	@Override
 	public void onConfigurationChanged(Configuration newConfig) {
 		super.onConfigurationChanged(newConfig);
 		mDrawerManager.getActionBarDrawerToggle().onConfigurationChanged(
 				newConfig);
 	}
 
 }
