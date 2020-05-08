 package com.github.norwae.whatiread;
 
 import java.util.Collection;
 import java.util.HashSet;
 
 import com.github.norwae.whatiread.data.BookInfo;
 import com.github.norwae.whatiread.data.ISBN13;
 import com.google.zxing.integration.android.IntentIntegrator;
 import com.google.zxing.integration.android.IntentResult;
 
 import android.app.ActionBar;
 import android.app.ActionBar.Tab;
 import android.app.ActionBar.TabListener;
 import android.app.AlertDialog;
 import android.app.FragmentTransaction;
 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentPagerAdapter;
 import android.support.v4.view.ViewPager;
 import android.support.v4.view.ViewPager.OnPageChangeListener;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.MenuItem.OnMenuItemClickListener;
 
 public class MainActivity extends FragmentActivity {
 
 	private static class PageAdapter extends FragmentPagerAdapter {
 		public PageAdapter(FragmentManager manager) {
 			super(manager);
 		}
 
 		@Override
 		public Fragment getItem(int item) {
 			switch (item) {
 			case 0: return new AddOrCheckFragment();
 			case 1: return new BrowseFragment();
 			}
 			return null;
 		}
 
 		@Override
 		public int getCount() {
 			return 2;
 		}
 
 	}
 
 	private Collection<AsyncTask<?, ?, ?>> backgroundTasks = new HashSet<AsyncTask<?, ?, ?>>();
 	private ViewPager viewPager;
 	private PageAdapter pageAdapter;
 	private ActionBar actionBar;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		setContentView(R.layout.activity_main);
 		
 		viewPager = (ViewPager) findViewById(R.id.pager);
 		pageAdapter = new PageAdapter(getSupportFragmentManager());
 		actionBar = getActionBar();
 	
 		viewPager.setAdapter(pageAdapter);
 		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
 		actionBar.setHomeButtonEnabled(false);
 
 		TabListener tabListener = new TabListener() {
 			
 			@Override
 			public void onTabUnselected(Tab tab, FragmentTransaction ft) {}
 			
 			@Override
 			public void onTabSelected(Tab tab, FragmentTransaction ft) {
				viewPager.setCurrentItem(tab.getPosition());
 			}
 			
 			@Override
 			public void onTabReselected(Tab tab, FragmentTransaction ft) {}
 		};
 		
 		OnPageChangeListener pageChangeListener = new OnPageChangeListener() {
 			
 			@Override
 			public void onPageSelected(int position) {
 				actionBar.setSelectedNavigationItem(position);
 			}
 			
 			@Override
 			public void onPageScrolled(int arg0, float arg1, int arg2) {}
 			
 			@Override
 			public void onPageScrollStateChanged(int arg0) {}
 		}; 
 
 		String[] titles = { getString(R.string.fragment_title_register),
 				getString(R.string.fragment_title_browse) };
 				
 		for (String name : titles) {
 			actionBar.addTab(actionBar.newTab().setText(name).setTabListener(tabListener));
 		}
 		
 		viewPager.setOnPageChangeListener(pageChangeListener);
 	}
 
 	@Override
 	protected void onDestroy() {
 		for (AsyncTask<?, ?, ?> temp : backgroundTasks) {
 			temp.cancel(false);
 		}
 
 		super.onDestroy();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.main, menu);
 
 		menu.add(R.string.action_about).setOnMenuItemClickListener(
 				new OnMenuItemClickListener() {
 
 					@Override
 					public boolean onMenuItemClick(MenuItem item) {
 						new AlertDialog.Builder(MainActivity.this)
 								.setMessage(R.string.alpha)
 								.setNeutralButton(R.string.action_ok, null)
 								.show();
 
 						return true;
 					}
 				});
 
 		return true;
 	}
 
 
 	@Override
 	public void onActivityResult(int requestCode, int resultCode, Intent data) {
 		IntentResult scanResult = IntentIntegrator.parseActivityResult(
 				requestCode, resultCode, data);
 		if (scanResult != null) {
 			String code = scanResult.getContents();
 			ISBN13 isbn = ISBN13.parse(code);
 
 			if (isbn != null) {
 				lookupISBN(isbn);
 			}
 		}
 	}
 	
 
 
 	void lookupISBN(ISBN13 isbn) {
 		final ProgressDialog progressDialog = ProgressDialog.show(this,
 				getString(R.string.progress_pleaseWait),
 				getString(R.string.progress_initial));
 
 		AsyncCallbackReceiver<BookInfo, String> tempCallback = new AsyncCallbackReceiver<BookInfo, String>() {
 
 			@Override
 			public void onAsyncComplete(BookInfo anObject) {
 				if (progressDialog.isShowing()) {
 					progressDialog.dismiss();
 				}
 
 				if (anObject != null) {
 					displayBookInfo(anObject, true);
 				}
 			}
 
 			@Override
 			public void onProgressReport(String... someProgress) {
 				progressDialog.setMessage(someProgress[0]);
 			}
 		};
 		BookISBNLookup lookup = new BookISBNLookup(tempCallback, this);
 
 		lookup.execute(isbn);
 	}
 	
 
 	void displayBookInfo(BookInfo anObject, boolean warnForExisting) {
 		Intent tempIntent = new Intent(this, DisplayBookActivity.class);
 		tempIntent.putExtra(DisplayBookActivity.BOOK_INFO_VARIABLE, anObject);
 		tempIntent.putExtra(DisplayBookActivity.WARN_FOR_READ_BOOKS_VARIABLE,
 				warnForExisting);
 		startActivity(tempIntent);
 	}
 
 }
