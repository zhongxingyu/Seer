 /**
  * This file is part of Plingnote.
 <<<<<<< HEAD
  * Copyright (C) 2012 Barnabas sapan
 =======
  * Copyright (C) 2012 Barnabas Sapan
 >>>>>>> base_gui
  * 
  * Plingnote is free software: you can redistribute it and/or modify it under
  * the terms of the GNU General Public License as published by the Free Software
  * Foundation, either version 3 of the License, or any later version.
  * 
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  * 
  * You should have received a copy of the GNU General Public License along with
  * this program. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.plingnote.main;
 
 import java.util.ArrayList;
 
 import android.app.ActionBar;
 import android.app.ActionBar.Tab;
 import android.app.SearchManager;
 import android.app.SearchManager.OnDismissListener;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentPagerAdapter;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.widget.SearchView;
 
 import com.plingnote.R;
 import com.plingnote.gridview.FragmentImageGridView;
 import com.plingnote.listview.FragmentListView;
 import com.plingnote.map.FragmentMapView;
 import com.plingnote.preferences.ActivityAppPreference;
 import com.plingnote.service.LocationService;
 
 public class ActivityMain extends FragmentActivity{
 	private boolean isSearching = false;
 	private ScrollableViewPager viewPager;
 	private TabsAdapter tabsAdapter;
 	private SearchView searchView;
 		
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
		Intent service = new Intent(this, LocationService.class);
		startService(service);
 		this.viewPager = new ScrollableViewPager(this);
 		this.viewPager.setId(R.id.viewPager);
 		setContentView(viewPager);
 		
 		
 		final ActionBar actionBar = getActionBar();
 		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
 		actionBar.setDisplayOptions(0, ActionBar.DISPLAY_SHOW_TITLE);
 
 		tabsAdapter = new TabsAdapter(this, viewPager);
 		tabsAdapter.addTab(actionBar.newTab().setIcon(android.R.drawable.ic_menu_mapmode), FragmentMapView.class, null);
 		tabsAdapter.addTab(actionBar.newTab().setIcon(android.R.drawable.ic_menu_sort_by_size), FragmentListView.class, null);
 		tabsAdapter.addTab(actionBar.newTab().setIcon(android.R.drawable.ic_menu_gallery), FragmentImageGridView.class, null);
 		
 		if (savedInstanceState != null) {
 			actionBar.setSelectedNavigationItem(savedInstanceState.getInt("tab", 0));
 			if(savedInstanceState.getBoolean("isSearching")) {
 				this.onSearchRequested();
 			}
 		}
 		
 		//TODO Removed for now until better implementation
 		//this.startService(new Intent(this, LocationService.class));
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.activity_main, menu);
 		final Menu m = menu;
 		SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
 		searchView = (SearchView) menu.findItem(R.id.search).getActionView();
 		searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
 		searchView.setIconifiedByDefault(true);
 		searchManager.setOnDismissListener(new OnDismissListener() {
 			public void onDismiss() {
 				searchView.setIconified(true);
 				m.findItem(R.id.search).collapseActionView();
 				isSearching = false;
 			}
 		});
 		
 		return true;
 	}
 	
 	@Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
         	/*If there is no room to display the search widget it will
         	 * be placed in the overflow menu. To display the widget
         	 * once pressed we have to manually call it from here.
         	 */
             case R.id.search:
                 onSearchRequested();
                 return true;
      
             case R.id.menu_settings: 
  			   startActivity(new Intent(this, ActivityAppPreference.class));
 
             default:
                 return false;
         }
     }
 	
 	@Override
 	public boolean onSearchRequested() {
 		this.isSearching = true;
 		return super.onSearchRequested();
 	}
 		
 	@Override
 	protected void onSaveInstanceState(Bundle outState) {
 		super.onSaveInstanceState(outState);
 		outState.putInt("tab", getActionBar().getSelectedNavigationIndex());
 		outState.putBoolean("isSearching", isSearching);
 	}
 	
 	/**
 	 * Menu item add new note is pressed.
 	 */
 	public void addNewNote(MenuItem item){
 		Intent intent = new Intent(this, ActivityNote.class);
 		startActivity(intent);
 	}
 
 	public static class TabsAdapter extends FragmentPagerAdapter implements ActionBar.TabListener, ScrollableViewPager.OnPageChangeListener {
 		private final Context context;
 		private final ActionBar actionBar;
 		private final ScrollableViewPager viewPager;
 		private final ArrayList<TabInfo> tabs = new ArrayList<TabInfo>();
 
 		static final class TabInfo {
 			private final Class<?> clss;
 			private final Bundle args;
 
 			TabInfo(Class<?> clss, Bundle args) {
 				this.clss = clss;
 				this.args = args;
 			}
 		}
 
 		public TabsAdapter(FragmentActivity activity, ScrollableViewPager pager) {
 			super(activity.getSupportFragmentManager());
 			this.context = activity;
 			this.actionBar = activity.getActionBar();
 			this.viewPager = pager;
 			this.viewPager.setAdapter(this);
 			this.viewPager.setOnPageChangeListener(this);
 		}
 
 		public void addTab(ActionBar.Tab tab, Class<?> clss, Bundle args) {
 			TabInfo info = new TabInfo(clss, args);
 			tab.setTag(info);
 			tab.setTabListener(this);
 			this.tabs.add(info);
 			this.actionBar.addTab(tab);
 			notifyDataSetChanged();
 		}
 
 		public void onPageScrollStateChanged(int state) { 
 		}
 
 		public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) { }
 
 		public void onPageSelected(int position) {
 			this.actionBar.setSelectedNavigationItem(position);
 		}
 
 		public void onTabReselected(Tab tab, android.app.FragmentTransaction ft) { }
 
 		public void onTabSelected(Tab tab, android.app.FragmentTransaction ft) {
 			Object tag = tab.getTag();
 			for (int i = 0 ; i < this.tabs.size(); i++) {
 				if (this.tabs.get(i) == tag) {
 					this.viewPager.setCurrentItem(i);
 				}
 			}
 		}
 
 		public void onTabUnselected(Tab tab, android.app.FragmentTransaction ft) { }
 
 		@Override
 		public Fragment getItem(int position) {
 			TabInfo info = this.tabs.get(position);
 			return Fragment.instantiate(this.context, info.clss.getName(), info.args);
 		}
 
 		@Override
 		public int getCount() {
 			return this.tabs.size();
 		}
 		
 		
 	}
 }
 
