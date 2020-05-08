 package net.redlinesoft.app.yannifanclub;
 
 import android.content.Intent;
 import android.os.Bundle;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentTransaction;
 import android.support.v4.view.ViewPager;
 import android.widget.LinearLayout;
 
 import com.actionbarsherlock.app.ActionBar;
 import com.actionbarsherlock.app.ActionBar.Tab;
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 import com.actionbarsherlock.view.Window;
 import com.actionbarsherlock.widget.ShareActionProvider;
 import net.redlinesoft.app.yannifanclub.R;
 import com.google.ads.AdRequest;
 import com.google.ads.AdSize;
 import com.google.ads.AdView;
 
 public class MainActivity extends SherlockFragmentActivity {
 
 	ActionBar mActionBar;
 	ViewPager mPager;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
 		setContentView(R.layout.activity_main);
 		setProgressBarIndeterminateVisibility(false);
 
 		// Create the adView
 		AdView adView = new AdView(this, AdSize.BANNER, "a15116b3fb5c1da");
 		// Lookup your LinearLayout assuming it’s been given
 		// the attribute android:id="@+id/mainLayout"
 		LinearLayout layout = (LinearLayout) findViewById(R.id.mainLayout);
 		// Add the adView to it
 		layout.addView(adView);
 		// Initiate a generic request to load it with an ad
 		adView.loadAd(new AdRequest());
 		//adView.loadAd(new AdRequest().addTestDevice("EEEC201218AC425593883C4F37DAA5C9"));
 
 		mActionBar = getSupportActionBar();
 		mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
 		mPager = (ViewPager) findViewById(R.id.pager);
 		FragmentManager fm = getSupportFragmentManager();
 
 		ViewPager.SimpleOnPageChangeListener pageChangeListener = new ViewPager.SimpleOnPageChangeListener() {
 			@Override
 			public void onPageSelected(int position) {
 				super.onPageSelected(position);
 				mActionBar.setSelectedNavigationItem(position);
 			}
 		};
 
 		mPager.setOnPageChangeListener(pageChangeListener);
 		MyFragmentPagerAdapter fragmentPagerAdapter = new MyFragmentPagerAdapter(
 				fm);
 		mPager.setAdapter(fragmentPagerAdapter);
 		mActionBar.setDisplayShowTitleEnabled(true);
 		ActionBar.TabListener tabListener = new ActionBar.TabListener() {
 
 			@Override
 			public void onTabSelected(Tab tab, FragmentTransaction ft) {
 				mPager.setCurrentItem(tab.getPosition());
 			}
 
 			@Override
 			public void onTabUnselected(Tab tab, FragmentTransaction ft) {
 				// TODO Auto-generated method stub
 			}
 
 			@Override
 			public void onTabReselected(Tab tab, FragmentTransaction ft) {
 				// TODO Auto-generated method stub
 			}
 		};
 
 		Tab tab = mActionBar.newTab().setText("Video")
 				.setTabListener(tabListener);
 		mActionBar.addTab(tab);
 		tab = mActionBar.newTab().setText("Concert")
 				.setTabListener(tabListener);
 		mActionBar.addTab(tab);
 		tab = mActionBar.newTab().setText("Twitter")
 				.setTabListener(tabListener);
 		mActionBar.addTab(tab);
 
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu) {
 		// TODO Auto-generated method stub
 		MenuInflater inflater = getSupportMenuInflater();
 		inflater.inflate(R.menu.main, menu);
 		MenuItem actionItem = menu
 				.findItem(R.id.menu_item_share_action_provider_action_bar);
 		ShareActionProvider actionProvider = (ShareActionProvider) actionItem
 				.getActionProvider();
 		actionProvider.setShareIntent(createShareIntent());
 		return true;
 	}
 
 	private Intent createShareIntent() {
 		// TODO Auto-generated method stub
 		Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
		shareIntent.setType("text/*");
 		shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
 				getString(R.string.text_share_subject));
 		shareIntent.putExtra(android.content.Intent.EXTRA_TEXT,
 				getString(R.string.text_share_body)
 						+ getApplicationContext().getPackageName());
 		return shareIntent;
 	}
 
 }
