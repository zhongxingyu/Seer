 /*
  *	Copyright (c) 2013 Andrew Fontaine, James Finlay, Jesse Tucker, Jacob Viau, and
  * 	Evan DeGraff
  *
  * 	Permission is hereby granted, free of charge, to any person obtaining a copy of
  * 	this software and associated documentation files (the "Software"), to deal in
  * 	the Software without restriction, including without limitation the rights to
  * 	use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
  * 	the Software, and to permit persons to whom the Software is furnished to do so,
  * 	subject to the following conditions:
  *
  * 	The above copyright notice and this permission notice shall be included in all
  * 	copies or substantial portions of the Software.
  *
  * 	THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * 	IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
  * 	FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
  * 	COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
  * 	IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
  * 	CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
  */
 
 package ca.cmput301f13t03.adventure_datetime.view;
 
 import java.util.Collection;
 
 import ca.cmput301f13t03.adventure_datetime.R;
 import ca.cmput301f13t03.adventure_datetime.model.Story;
 import ca.cmput301f13t03.adventure_datetime.model.Interfaces.IStoryListListener;
 import ca.cmput301f13t03.adventure_datetime.serviceLocator.Locator;
 import android.app.ActionBar;
 import android.app.FragmentTransaction;
 import android.app.ActionBar.Tab;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentPagerAdapter;
 import android.support.v4.view.ViewPager;
 import android.util.Log;
 
 /**
  * View accessed via MainView > ~Browse button~
  * 
  * Contains three fragments/tabs where user can see list of cached stories, 
  * authored stories, and online stories.
  * 
  * @author James Finlay
  *
  */
 public class BrowseView extends FragmentActivity implements IStoryListListener {
 	private static final String TAG = "BrowseView";
 
 	private ViewPager _viewPager;
 	private ViewPagerAdapter _adapter;
 	
 	@Override
 	public void OnCurrentStoryListChange(Collection<Story> newStories) {
 		_adapter.setLocalStories(newStories);	
 	}
 	
 	// TODO::JF Listen for Server stories
 	
 	@Override
 	public void onResume() {
 		Locator.getPresenter().Subscribe(this);
 		super.onResume();
 	}
 	@Override
 	public void onPause() {
 		Locator.getPresenter().Unsubscribe(this);
 		super.onPause();
 	}
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.viewpager);
 		
 		/* Set up View Pager */
 		_adapter = new ViewPagerAdapter(getSupportFragmentManager());
 		_viewPager = (ViewPager) findViewById(R.id.pager);
 		_viewPager.setAdapter(_adapter);
 
 		/* Set up Tabs */
 		final ActionBar actionBar = getActionBar();
 		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
 		ActionBar.TabListener tabListener = new ActionBar.TabListener() {
 			@Override
 			public void onTabReselected(Tab tab, FragmentTransaction ft) {}
 
 			@Override
 			public void onTabSelected(Tab tab, FragmentTransaction ft) {
 				_viewPager.setCurrentItem(tab.getPosition());
 			}
 			@Override
 			public void onTabUnselected(Tab tab, FragmentTransaction ft) {}
 		};
 
 		actionBar.addTab(actionBar.newTab()
 				.setText("Saved")
 				.setTabListener(tabListener));
 		actionBar.addTab(actionBar.newTab()
 				.setText("My Stories")
 				.setTabListener(tabListener));
 		actionBar.addTab(actionBar.newTab()
 				.setText("Online")
 				.setTabListener(tabListener));
 
 		/* Change tabs when View Pager swiped */
 		_viewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
 			@Override
 			public void onPageSelected(int position) {
 				getActionBar().setSelectedNavigationItem(position);
 			}
 		});
 
 	}
 
 
 	public class ViewPagerAdapter extends FragmentPagerAdapter {
 		
 		private BrowseFragment cached, authored, online;
 		
 		public ViewPagerAdapter(FragmentManager fm) {
 			super(fm);
 			
 			cached = new BrowseFragment();
 			authored = new BrowseFragment();
 			online = new BrowseFragment();
 		}
 		
 		public void setLocalStories(Collection<Story> stories) {
 			cached.setStories(stories);
 			authored.setStories(stories);
 		}
 		public void setOnlineStories(Collection<Story> stories) {
 			online.setStories(stories);
 		}
 
 		@Override
 		public Fragment getItem(int i) {
 			switch(i) {
 			case 0: return cached;
 			case 1: return authored;
 			case 2: return online;
 			default: return null;
 			}
 		}
 
 		@Override
 		public int getCount() {
 			return 3;
 		}
 
 		@Override
 		public CharSequence getPageTitle(int position) {
 			switch (position) {
 			case 0: return "Saved";
 			case 1: return "My Stories";
 			case 2: return "Online";
 			default: return "It be a Pirate!";
 			}
 		}
 	}
 
 
 }
