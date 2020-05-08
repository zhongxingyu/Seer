 package com.pk.addits;
 
 import java.io.File;
 
 import android.app.ActionBar;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.content.res.Configuration;
 import android.os.Bundle;
 import android.os.Environment;
 import android.os.Handler;
 import android.support.v4.app.ActionBarDrawerToggle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.view.GravityCompat;
 import android.support.v4.widget.DrawerLayout;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.animation.Animation;
 import android.view.animation.AnimationUtils;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 
 public class ActivityMain extends FragmentActivity implements AdapterView.OnItemClickListener
 {
 	private ActionBar actionBar;
 	private SharedPreferences prefs;
 	private Thread feedThread;
 	private Handler mHandler;
 	private showProgress showP;
 	
 	private DrawerLayout mDrawerLayout;
 	private ListView mDrawerList;
 	private ActionBarDrawerToggle mDrawerToggle;
 	
 	public static Feed[] NewsFeed;
 	public static String currentFragment;
 	public static boolean articleShowing;
 	
 	private CharSequence mDrawerTitle;
 	private static CharSequence mTitle;
 	private String[] mListNames;
 	private int[] mListImages;
 	
 	private ProgressBar ProgressBar;
 	private View ProgressFinished;
 	private LinearLayout Loading;
 	private TextView LoadingText;
 	private long lastUpdateCheckTime;
 	private int updateCheckInterval = 0;// 6 * 60 * 60 * 1000; // Comment out to 0 if you want to test it.
 	
 	FragmentManager fragmentManager;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		
 		actionBar = getActionBar();
 		actionBar.setDisplayHomeAsUpEnabled(true);
 		actionBar.setHomeButtonEnabled(true);
 		actionBar.setIcon(R.drawable.ic_ab_logo);
 		
 		prefs = getSharedPreferences(Data.PREFS_TAG, 0);
 		lastUpdateCheckTime = prefs.getLong(Data.PREF_TAG_LAST_UPDATE_CHECK_TIME, 0);
 		fragmentManager = getSupportFragmentManager();
 		
 		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
 		mDrawerList = (ListView) findViewById(R.id.left_drawer);
 		Loading = (LinearLayout) findViewById(R.id.loading);
 		LoadingText = (TextView) findViewById(R.id.loadingText);
 		ProgressBar = (ProgressBar) findViewById(R.id.progressBar);
 		ProgressFinished = findViewById(R.id.progressFinished);
 		
 		mTitle = mDrawerTitle = getTitle();
 		mListNames = getResources().getStringArray(R.array.drawer_items);
 		initializeNavigationDrawer();
 		//getTESTFeed();
 		
 		if (savedInstanceState == null)
 		{
 			selectItem(0);
 			
 			mHandler = new Handler();
 			initializeFeedThread();
 			checkNewFeed();
 		}
 	}
 	
 	@Override
 	protected void onPostCreate(Bundle savedInstanceState)
 	{
 		super.onPostCreate(savedInstanceState);
 		// Sync the toggle state after onRestoreInstanceState has occurred.
 		mDrawerToggle.syncState();
 	}
 	
 	@Override
 	public void onConfigurationChanged(Configuration newConfig)
 	{
 		super.onConfigurationChanged(newConfig);
 		// Pass any configuration change to the drawer toggles
 		mDrawerToggle.onConfigurationChanged(newConfig);
 	}
 	
 	@Override
 	public boolean onPrepareOptionsMenu(Menu menu)
 	{
 		if(articleShowing)
 			FragmentArticle.menuVisibility(mDrawerLayout.isDrawerOpen(mDrawerList));
 		
 		return super.onPrepareOptionsMenu(menu);
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item)
 	{
 		return mDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
 	}
 	
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event)
 	{
 		if (keyCode == KeyEvent.KEYCODE_BACK)
 		{
 			if (currentFragment.equals("Home") && articleShowing)
 			{
 				Fragment fragment = new FragmentHome();
 				mTitle = "Home";
 				actionBar.setTitle(mTitle);
 				articleShowing = false;
 				fragmentManager.beginTransaction().setCustomAnimations(R.anim.fade_into, R.anim.out_to_right).replace(R.id.content_frame, fragment).commit();
 				
 				return true;
 			}
 			else
 				finish();
 		}
 		return super.onKeyDown(keyCode, event);
 	}
 	
 	@Override
 	public void onItemClick(AdapterView<?> parent, View view, int position, long id)
 	{
 		selectItem(position);
 	}
 	
 	private void initializeNavigationDrawer()
 	{
 		// Set up custom shadow overlay
 		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
 		
 		// Set up drawer content & listener
 		mDrawerList.setAdapter(new ArrayAdapter<String>(this, R.layout.drawer_list_item, mListNames));
 		mDrawerList.setOnItemClickListener(this);
 		
 		// ActionBarDrawerToggle ties together the the proper interactions
 		// between the sliding drawer and the action bar app icon
 		mDrawerToggle = new ActionBarDrawerToggle(this, /* host Activity */
 		mDrawerLayout, /* DrawerLayout object */
 		R.drawable.ic_drawer, /* nav drawer image to replace 'Up' caret */
 		R.string.drawer_open, /* "open drawer" description for accessibility */
 		R.string.drawer_close /* "close drawer" description for accessibility */
 		)
 		{
 			public void onDrawerClosed(View view)
 			{
 				actionBar.setTitle(mTitle);
 				invalidateOptionsMenu();
 			}
 			
 			public void onDrawerOpened(View drawerView)
 			{
 				actionBar.setTitle(mDrawerTitle);
 				invalidateOptionsMenu();
 			}
 		};
 		
 		mDrawerLayout.setDrawerListener(mDrawerToggle);
 	}
 	
 	private void selectItem(int position)
 	{
 		// update the main content by replacing fragments
 		Fragment fragment = null;
 		
 		switch (position)
 		{
 			case 0:
 				fragment = new FragmentHome();
 				break;
 			case 1:
 				fragment = new FragmentCustomization();
 				break;
 			case 2:
 				fragment = new FragmentDeveloperFocus();
 				break;
 			case 3:
 				fragment = new FragmentMerchandise();
 				break;
 			case 4:
 				fragment = new FragmentReviews();
 				break;
 			case 5:
 				fragment = new FragmentTutorials();
 				break;
 			default:
 				fragment = new FragmentSample();
 				Bundle args = new Bundle();
 				args.putInt(FragmentSample.ARG_IMAGE_RES, mListImages[position]);
 				args.putInt(FragmentSample.ARG_ACTION_BG_RES, R.drawable.ab_background);
 				fragment.setArguments(args);
 				break;
 		}
 		
 		fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
 		mDrawerList.setItemChecked(position, true);
 		setTitle(mListNames[position]);
 		currentFragment = mListNames[position];
 		articleShowing = false;
 		mDrawerLayout.closeDrawer(mDrawerList);
 	}
 	
 	@Override
 	public void setTitle(CharSequence title)
 	{
 		mTitle = title;
 		actionBar.setTitle(mTitle);
 	}
 	
 	public static void callArticle(Context context, Feed article)
 	{
 		Fragment fragment = FragmentArticle.newInstance(article);
 		mTitle = article.getTitle();
 		articleShowing = true;
 		
 		FragmentManager fragmentManager = ((FragmentActivity) context).getSupportFragmentManager();
 		fragmentManager.beginTransaction().setCustomAnimations(R.anim.in_from_right, R.anim.fade_away).replace(R.id.content_frame, fragment).commit();
 	}
 	
 	public static Feed[] getFeed()
 	{
 		return NewsFeed;
 	}
 	
 	private void checkNewFeed()
 	{
 		if (lastUpdateCheckTime + updateCheckInterval < System.currentTimeMillis() && Data.isNetworkConnected(ActivityMain.this))
 		{
 			lastUpdateCheckTime = System.currentTimeMillis();
 			Editor editor = prefs.edit();
 			editor.putLong(Data.PREF_TAG_LAST_UPDATE_CHECK_TIME, lastUpdateCheckTime);
 			editor.commit();
 			
 			if (feedThread == null)
 			{
 				initializeFeedThread();
 				feedThread.start();
 			}
 			else if (!feedThread.isAlive())
 			{
 				initializeFeedThread();
 				feedThread.start();
 			}
 		}
 	}
 	
 	private void initializeFeedThread()
 	{
 		feedThread = new Thread()
 		{
 			public void run()
 			{
 				try
 				{
 					/** Checking If It Already Contains File **/
 					File sdCard = Environment.getExternalStorageDirectory();
 					File dir = new File(sdCard.getAbsolutePath() + "/Android/data/" + Data.PACKAGE_TAG);
 					dir.mkdirs();
 					File file = new File(dir, Data.FEED_TAG);
 					
 					/** Fetch Website Data **/
 					showP = new showProgress("Checking for new content...", true, true, false);
 					mHandler.post(showP);
 					
 					Data.downloadFeed();
 					boolean NewFeed = false;
 					if (file.exists())
 						NewFeed = Data.compareFeed(ActivityMain.this);
 					else
 						Data.writeFeed();
 					
 					if (NewFeed) // New Stuff Found
 					{
 						/** New Stuff Found **/
 						showP = new showProgress("Updating content...", true, false, false);
 						mHandler.post(showP);
 						
 						NewsFeed = Data.retrieveFeed(ActivityMain.this, true).clone();
 						Data.deleteTempFile();
 
 						showP = new showProgress("Everything is up to date!", true, false, true);
 						mHandler.post(showP);
 					}
 					else
 					// Nothing New
 					{
 						NewsFeed = Data.retrieveFeed(ActivityMain.this, true).clone();
 						Data.deleteTempFile();
 
 						showP = new showProgress("Everything is up to date!", true, false, true);
 						mHandler.post(showP);
 					}
 					
 					showP = new showProgress("", false, false, false);
 					mHandler.post(showP);
 					showP = new showProgress("Everything is up to date!", false, true, true);
 					mHandler.postDelayed(showP, 4000);
 				}
 				catch (Exception e)
 				{
 					Log.v("DownloadFile", "ERROR: " + e.getMessage());
 				}
 				
 				stopThread(this);
 			}
 		};
 	}
 	
 	private synchronized void stopThread(Thread theThread)
 	{
 		if (theThread != null)
 		{
 			theThread = null;
 		}
 	}
 	
 	public class showProgress implements Runnable
 	{
 		boolean Active;
 		boolean Animate;
 		boolean Finished;
 		String Progress;
 		
 		public showProgress(String p, boolean a, boolean aa, boolean f)
 		{
 			this.Active = a;
 			this.Animate = aa;
 			this.Finished = f;
 			this.Progress = p;
 		}
 		
 		@Override
 		public void run()
 		{
 			if (Progress.length() > 0)
 			{
 				LoadingText.setText(Progress);
 				
 				if (Active)
 				{
 					Loading.setVisibility(View.VISIBLE);
 					if (Animate)
 					{
 						Animation a = AnimationUtils.loadAnimation(ActivityMain.this, R.anim.slide_up);
 						Loading.startAnimation(a);
 					}
 					if(Finished)
 					{
 						ProgressBar.setVisibility(View.GONE);
 						ProgressFinished.setVisibility(View.VISIBLE);
 					}
 				}
 				else
 				{
 					Loading.setVisibility(View.GONE);
 					if (Animate)
 					{
 						Animation a = AnimationUtils.loadAnimation(ActivityMain.this, R.anim.slide_down);
 						Loading.startAnimation(a);
 					}
 					if(Finished)
 					{
 						ProgressBar.setVisibility(View.GONE);
 						ProgressFinished.setVisibility(View.VISIBLE);
 					}
 				}
 			}
 			else
 			{
				if(currentFragment.equals("Home"))
 					FragmentHome.updateState();
 			}
 		}
 	}
 	/*
 	public void getTESTFeed()
 	{
 		AQuery aq = new AQuery(ActivityMain.this);
 		boolean be = aq.ajax(Data.FEED_URL, XmlDom.class, ActivityMain.this, "saveFeed");
 	}
 	
 	public boolean saveFeed(String url, XmlDom xml, AjaxStatus status)
 	{
 		Toast.makeText(ActivityMain.this, "XML = " + xml.toString(), Toast.LENGTH_SHORT).show();
 		List<XmlDom> entries = xml.tags("item");
 		Toast.makeText(ActivityMain.this, "Entry count = " + entries.size(), Toast.LENGTH_SHORT).show();
 		
 		for (XmlDom item : entries)
 		{
 			String title = item.text("title");
 			String date = item.text("pubDate");
 			if(Data.isNewerDate(date, "Fri, 21 Jun 2013 18:00:39 +0000"))
 				return true;
 			boolean = true;
 			Toast.makeText(ActivityMain.this, title + "\n" + date + "\n" + Data.isNewerDate(date, "Fri, 21 Jun 2013 18:00:39 +0000"), Toast.LENGTH_SHORT).show();
 		}
 		return false;
 	}*/
 }
