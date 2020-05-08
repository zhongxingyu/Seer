 package org.kset.android;
 
 import org.kset.android.fragments.AboutKsetFragment;
 import org.kset.android.fragments.NewsListFragment;
 import org.kset.android.fragments.VideoStreamFragment;
 import org.kset.android.fragments.NewsListFragment.NewsListListener;
 import org.kset.android.models.Source.Category.Feed.Article;
 
 import android.content.Intent;
 import android.content.res.Resources;
 import android.net.Uri;
 import android.os.Bundle;
 import android.support.v4.app.ActionBar;
 import android.support.v4.app.ActionBar.Tab;
 import android.support.v4.app.ActionBar.TabListener;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentTransaction;
 import android.support.v4.app.ListFragment;
 import android.support.v4.view.Window;
 
 public class MainActivity extends FragmentActivity implements TabListener,
 		NewsListListener {
 	private static final String KEY_LAST_TAB = "org.kset.android.last_tab";
 
 	private ActionBar mActionBar;
 	private Resources mResources;
 
 	private Fragment[] mFragments = new Fragment[] { new NewsListFragment(),
 			new VideoStreamFragment(), new AboutKsetFragment() };
 
 	private int[] mTabLabels = new int[] { R.string.tab_news,
 			R.string.tab_stream, R.string.tab_about_kset };
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		requestWindowFeature(Window.FEATURE_ACTION_BAR);
 		mResources = getResources();
 
 		mActionBar = getSupportActionBar();
 		mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
 
 		for (int i : mTabLabels) {
 			mActionBar.addTab(mActionBar.newTab()
 					.setText(mResources.getString(i)).setTabListener(this));
 		}
 		setContentView(R.layout.main);
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 		((NewsListFragment) mFragments[0]).registerListener(this);
 	}
 
 	@Override
 	public void onPause() {
 		((NewsListFragment) mFragments[0]).unregisterListener(this);
 		super.onPause();
 	}
 
 	@Override
 	public void onSaveInstanceState(Bundle outState) {
 		outState.putInt(KEY_LAST_TAB, mActionBar.getSelectedTab().getPosition());
 		super.onSaveInstanceState(outState);
 	}
 
 	@Override
 	public void onRestoreInstanceState(Bundle savedInstanceState){
 		super.onRestoreInstanceState(savedInstanceState);
 		mActionBar.setSelectedNavigationItem(savedInstanceState.getInt(KEY_LAST_TAB));
 	}
 	
 	@Override
 	public void onTabReselected(Tab tab, FragmentTransaction ft) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void onTabSelected(Tab tab, FragmentTransaction ft) {
 		ft = getSupportFragmentManager().beginTransaction();
		ft.replace(R.id.main_root, mFragments[tab.getPosition()]);
 		ft.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
 		ft.commit();
 	}
 
 	@Override
 	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void onArticleSelected(ListFragment which, Article a) {
 		/*
 		 * For now, we want to open the Article in a web browser
 		 */
 		startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse(a
 				.getLocation().toString())));
 	}
 }
