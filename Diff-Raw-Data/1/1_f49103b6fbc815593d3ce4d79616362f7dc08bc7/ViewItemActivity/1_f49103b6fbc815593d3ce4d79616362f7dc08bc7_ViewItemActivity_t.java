 package com.example.myrssreader;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.ProtocolException;
 import java.net.URL;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Locale;
 
 import javax.xml.xpath.XPath;
 import javax.xml.xpath.XPathConstants;
 import javax.xml.xpath.XPathExpressionException;
 import javax.xml.xpath.XPathFactory;
 
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.InputSource;
 
 import android.app.Activity;
 import android.app.DownloadManager;
 import android.content.Context;
 import android.content.Intent;
 import android.database.Cursor;
 import android.database.DataSetObserver;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentStatePagerAdapter;
 import android.support.v4.view.MenuItemCompat;
 import android.support.v4.view.PagerAdapter;
 import android.support.v4.view.ViewPager;
 import android.support.v4.view.ViewPager.OnPageChangeListener;
 import android.support.v7.app.ActionBar;
 import android.support.v7.app.ActionBar.OnNavigationListener;
 import android.support.v7.app.ActionBarActivity;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.View.OnClickListener;
 import android.webkit.WebView;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.SpinnerAdapter;
 import android.widget.Toast;
 
 public class ViewItemActivity extends ActionBarActivity {
 	private RSSDatabaseAdapter adapter;
 	private String source_id;
 	private ViewPager mPager;
 	private PagerAdapter mPagerAdapter;
 	private Cursor mCurrentCursor;
 	private SpinnerAdapter mSpinnerAdapter;
 	private OnNavigationListener mNavigationListener;
 	private boolean showAll = false;
 	static final int ALL_ITEMS = 0;
 	static final int UNREADED_ITEMS = 1;
 
 
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.view_item_activity);
 		adapter = new RSSDatabaseAdapter(this);
 		source_id = getIntent().getStringExtra("source_id");
 		mPager = (ViewPager) findViewById(R.id.pager);
 		mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
 		mPager.setAdapter(mPagerAdapter);
 		ActionBar actionBar = getSupportActionBar();
 		actionBar.setDisplayHomeAsUpEnabled(true);
 		mPager.setOnPageChangeListener(new OnPageChangeListener() {
 
 			@Override
 			public void onPageSelected(int position) {			
 				if (!showAll && mCurrentCursor.moveToPosition(position-1) && !(mCurrentCursor.isBeforeFirst())) {
 					adapter.setItemAsReaded(mCurrentCursor.getString(mCurrentCursor.getColumnIndex(RSSDatabaseAdapter.KEY_ITEM_ID)));
 				}
 			}
 
 			@Override
 			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
 			}
 
 			@Override
 			public void onPageScrollStateChanged(int state) {				
 			}
 		});
 		mSpinnerAdapter =  ArrayAdapter.createFromResource(this, R.array.action_list_read, R.layout.dropdown_item);
 		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
 		mNavigationListener = new OnNavigationListener() {
 
 			@Override
 			public boolean onNavigationItemSelected(int position, long itemId) {
 				String[] list = getResources().getStringArray(R.array.action_list_read);
 				if (list[position].equals(getResources().getString(R.string.action_list_read_unreaded))) {
 					showAll = false;
 					updatePager();
 					return true;
 				} else {
 					if (list[position].equals(getResources().getString(R.string.action_list_read_all))) {
 						showAll = true;
 						updatePager();
 						return true;
 					}
 				}
 				return false;
 			}
 		};
 		actionBar.setListNavigationCallbacks(mSpinnerAdapter, mNavigationListener);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.view_item_actions, menu);
 		return super.onCreateOptionsMenu(menu);
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		// Handle presses on the action bar items
 		switch (item.getItemId()) {
 		case R.id.action_update:
 			updateFeeds();
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 	}
 
 	private void updatePager() {
 		updateCursor();
 		mPagerAdapter.notifyDataSetChanged();
 		mPager.setCurrentItem(0);
 	}
 
 	private void updateCursor(){
 		if (showAll)
 			mCurrentCursor = adapter.fetchAllItems(source_id);
 		else
 			mCurrentCursor = adapter.fetchUnreadedItems(source_id);
 		mCurrentCursor.moveToFirst();
 	}
 
 	@Override
 	protected void onStart() {
 		super.onStart();
 		adapter.open();
 		updatePager();
 	}
 
 	@Override
 	protected void onStop() {
 		super.onStop();
 		adapter.close();
 	}
 
 	private void updateFeeds() {
 		ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
 		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
 		if (networkInfo != null && networkInfo.isConnected()) {
 			new UpdateFeeds().execute(source_id);
 		} else {
 			Toast toast = Toast.makeText(this, R.string.no_internet_connection, 1);
 			toast.show();
 		}
 	}
 
 	private class UpdateFeeds extends AsyncTask<String, Integer, Boolean> {
 		private RSSDatabaseAdapter adapter;
 		@Override
 		protected Boolean doInBackground(String... params) {
 			adapter = new RSSDatabaseAdapter(ViewItemActivity.this);
 			adapter.open();
 			String url = adapter.getURLbyID(params[0]);
 			InputStream stream;
 			try {
 				stream = downloadRSS(url);
 				parseRSS(stream);
 			} 
 			//TODO: handle exception
 			catch (IOException e) {
 			}
 			catch (XPathExpressionException e) {
 			}
 			catch (IllegalAccessError e) {		
 			}
 			adapter.close();
 			return null;
 		}
 
 		@Override
 		protected void onPostExecute(Boolean result) {
 			Toast toast = Toast.makeText(ViewItemActivity.this, R.string.update_completed, 1);
 			toast.show();
 		}
 
 		private void parseRSS(InputStream stream) throws XPathExpressionException {
 			XPath xPath = XPathFactory.newInstance().newXPath();
 			NodeList items = (NodeList) xPath.evaluate("/rss/channel/item", new InputSource(stream), XPathConstants.NODESET);
 			SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z", Locale.US);
 			for (Integer i=0; i<items.getLength();i++) {
 				Node node = items.item(i).cloneNode(true);
 				String title = xPath.evaluate("./title", node);
 				String link = xPath.evaluate("./link", node);
 				String pubDate = xPath.evaluate("./pubDate", node);
 				String description = xPath.evaluate("./description", node);
 				Date date;
 				try {
 					date = dateFormat.parse(pubDate);
 				} catch (ParseException e) {
 					date = new Date();
 					e.printStackTrace();
 				}
 				pubDate = String.valueOf(date.getTime() / 1000L);
 				adapter.addNewItem(link, title, 
 						pubDate, description, source_id);
 			}
 
 		}
 
 		private InputStream downloadRSS(String myurl) throws IOException, MalformedURLException, ProtocolException, IllegalAccessError {
 			InputStream is = null;
 
 			URL url = new URL(myurl);
 			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
 			conn.setReadTimeout(10000 /* milliseconds */);
 			conn.setConnectTimeout(15000 /* milliseconds */);
 			conn.setRequestMethod("GET");
 			conn.setDoInput(true);
 			conn.connect();
 			is = conn.getInputStream();
 
 			return is;
 		}
 	}
 
 	private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
 		public ScreenSlidePagerAdapter(FragmentManager fm) {
 			super(fm);
 		}
 
 		@Override
 		public Fragment getItem(int position) {
 			ViewItemFragment fragment = new ViewItemFragment();
 			Bundle args = new Bundle();
 			if  (mCurrentCursor != null) {
 				mCurrentCursor.moveToPosition(position);
 				args.putString("title", mCurrentCursor.getString(mCurrentCursor.getColumnIndex(RSSDatabaseAdapter.KEY_ITEM_TITLE)));
 				args.putString("link", mCurrentCursor.getString(mCurrentCursor.getColumnIndex(RSSDatabaseAdapter.KEY_ITEM_LINK)));
 				args.putString("pubDate", mCurrentCursor.getString(mCurrentCursor.getColumnIndex(RSSDatabaseAdapter.KEY_ITEM_PUBDATE)));
 				args.putString("description", mCurrentCursor.getString(mCurrentCursor.getColumnIndex(RSSDatabaseAdapter.KEY_ITEM_DESCRIPTION)));
 				fragment.setArguments(args);
 			}
 			return fragment;
 		}
 
 		@Override
 		public int getItemPosition(Object object) {
 			return POSITION_NONE;
 		}
 
 		@Override
 		public int getCount() {
 			if  (mCurrentCursor != null)
 				return mCurrentCursor.getCount();
 			return 0;
 		}
 	}
 }
