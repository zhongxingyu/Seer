 package com.plingnote;
 
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
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.widget.SearchView;
 
 public class ActivityMain extends FragmentActivity{

 	private ScrollableViewPager viewPager;
 	private TabsAdapter tabsAdapter;
 	private SearchView searchView;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 	
 		this.viewPager = new ScrollableViewPager(this);
 		this.viewPager.setId(R.id.viewPager);
 		setContentView(viewPager);
 		final ActionBar actionBar = getActionBar();
 		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
 		actionBar.setDisplayOptions(0, ActionBar.DISPLAY_SHOW_TITLE);
 
 		tabsAdapter = new TabsAdapter(this, viewPager);
 		tabsAdapter.addTab(actionBar.newTab().setIcon(android.R.drawable.ic_menu_mapmode), FragmentMapView.class, null);
 		tabsAdapter.addTab(actionBar.newTab().setIcon(android.R.drawable.ic_menu_sort_by_size), FragmentListView.class, null);
 		if (savedInstanceState != null) {
 			actionBar.setSelectedNavigationItem(savedInstanceState.getInt("tab", 0));
 		}
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
             default:
                 return false;
         }
     }
 	
 	@Override
 	public boolean onSearchRequested() {
		
 		return super.onSearchRequested();
 	}
	
 	@Override
 	protected void onSaveInstanceState(Bundle outState) {
 		super.onSaveInstanceState(outState);
 		outState.putInt("tab", getActionBar().getSelectedNavigationIndex());
 	}
 	
 	/**
 	 * Menu item add new note is pressed.
 	 * @param newNote
 	 */
 	public void addNewNote(MenuItem newNote){
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
