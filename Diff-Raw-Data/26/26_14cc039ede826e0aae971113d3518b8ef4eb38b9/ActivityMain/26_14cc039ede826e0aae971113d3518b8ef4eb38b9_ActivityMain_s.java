 package com.pk.addits;
 
 import java.io.File;
 import java.util.List;
 
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
 import android.widget.Toast;
 
 import com.androidquery.AQuery;
 import com.androidquery.callback.AjaxStatus;
 import com.androidquery.util.XmlDom;
 
 public class ActivityMain extends FragmentActivity implements AdapterView.OnItemClickListener
 {
 	private ActionBar actionBar;
 	private SharedPreferences prefs;
 	private Thread feedThread;
 	private Thread emptyFeedThread;
 	private Handler mHandler;
 	private showProgress showP;
 	private showProgress2 showP2;
 	private boolean emptyFeed;
 	
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
 	private boolean newFound;
 	private boolean fragmentLoaded;
 	private boolean newChecked;
 	
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
 		emptyFeed = false;
 		fragmentLoaded = false;
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
 		newFound = false;
 		newChecked = false;
 		
 		if (savedInstanceState == null)
 		{
 			mHandler = new Handler();
 			File sdCard = Environment.getExternalStorageDirectory();
 			File dir = new File(sdCard.getAbsolutePath() + "/Android/data/" + Data.PACKAGE_TAG);
 			dir.mkdirs();
 			File file = new File(dir, Data.FEED_TAG);
 			emptyFeed = !file.exists();
 			
 			if (file.exists())
 			{
 				selectItem(225);
 				
 				initializeFeedThread();
 				checkNewFeed();
 			}
 			else
 			{
 				selectItem(225);
 				initializeEmptyFeedThread();
 				emptyFeedThread.start();
 			}
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
 		if (articleShowing)
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
 				fragmentManager.beginTransaction().setCustomAnimations(R.anim.plus_page_in_left, R.anim.plus_page_out_left).replace(R.id.content_frame, fragment).commit();
 				
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
 		if (emptyFeed)
 		{
 			Toast.makeText(ActivityMain.this, "Wait for everything to finish loading first!", Toast.LENGTH_LONG).show();
 			mDrawerLayout.closeDrawers();
 		}
 		else
 		{
 			selectItem(position);
 		}
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
 			case 225:
 				fragment = new FragmentLoading();
 				currentFragment = "Loading";
 				break;
 			default:
 				fragment = new FragmentSample();
 				Bundle args = new Bundle();
 				args.putInt(FragmentSample.ARG_IMAGE_RES, mListImages[position]);
 				args.putInt(FragmentSample.ARG_ACTION_BG_RES, R.drawable.ab_background);
 				fragment.setArguments(args);
 				break;
 		}
 		
 		articleShowing = false;
 		fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
 		if (position != 225)
 		{
 			mDrawerList.setItemChecked(position, true);
 			setTitle(mListNames[position]);
 			currentFragment = mListNames[position];
 			mDrawerLayout.closeDrawer(mDrawerList);
 		}
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
 		fragmentManager.beginTransaction().setCustomAnimations(R.anim.plus_page_in_right, R.anim.plus_page_out_right).replace(R.id.content_frame, fragment).commit();
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
 	
 	public void downloadFeed(String url, XmlDom xml, AjaxStatus status)
 	{
 		List<XmlDom> entries = xml.tags("item");
 		NewsFeed = new Feed[entries.size()];
 		int count = 0;
 		
 		for (XmlDom item : entries)
 		{
 			String Title = item.text("title");
 			String des = android.text.Html.fromHtml(item.text("description")).toString();
 			String Description = des.substring(3, des.length());
 			String Content = item.text("content:encoded");
 			String CommentFeed = item.text("wfw:commentRss");
 			String Author = item.text("dc:creator");
 			String Date = Data.parseDate(ActivityMain.this, item.text("pubDate"));
 			String Category = item.text("category");
 			String Image = Data.pullLinks(item.text("description"));
 			String URL = item.text("link");
 			
 			NewsFeed[count] = new Feed(count, Title, Description, Content, CommentFeed, Author, Date, Category, Image, URL, false, false);
 			count++;
 		}
 		return;
 	}
 	
 	public void checkNew(String url, XmlDom xml, AjaxStatus status)
 	{
 		List<XmlDom> entries = xml.tags("item");
 		
 		for (XmlDom item : entries)
 		{
 			String title = item.text("title");
 			String date = item.text("pubDate");
 			if (Data.isNewerDate(NewsFeed[0].getDate(), date))
 			{
 				newFound = true;
 				newChecked = true;
 				Toast.makeText(ActivityMain.this, "New Found!" + date + "\n " + title, Toast.LENGTH_SHORT).show();
 				return;
 			}
 		}
 		newChecked = true;
 		return;
 	}
 	
 	public static void overwriteFeedXML()
 	{
 		Data.overwriteFeedXML(NewsFeed);
 	}
 	
 	private void initializeEmptyFeedThread()
 	{
 		emptyFeedThread = new Thread()
 		{
 			public void run()
 			{
 				try
 				{
 					/** Download Content **/
 					showP2 = new showProgress2("Downloading content...");
 					mHandler.post(showP2);
 					
 					AQuery aq = new AQuery(ActivityMain.this);
 					aq.ajax(Data.FEED_URL, XmlDom.class, ActivityMain.this, "downloadFeed");
 					
 					Data.overwriteFeedXML(NewsFeed);
 					
 					showP2 = new showProgress2("Everything is up to date!");
 					mHandler.post(showP2);
 					
 					showP2 = new showProgress2("");
 					mHandler.postDelayed(showP2, 5000);
 					
 				}
 				catch (Exception e)
 				{
 					Log.v("DownloadFile", "ERROR: " + e.getMessage());
 				}
 				
 				stopThread(this);
 			}
 		};
 	}
 	
 	private void initializeFeedThread()
 	{
 		feedThread = new Thread()
 		{
 			public void run()
 			{
 				try
 				{
 					Log.v("Loading Feed!", "");
 					showP = new showProgress("Loading feed...", true, true, false);
 					mHandler.post(showP);
 					
 					NewsFeed = Data.retrieveFeed().clone();
 					Log.v("Feed Loaded! ", "" + NewsFeed.length);
 					
 					showHome showH = new showHome();
 					mHandler.post(showH);
 					/** Fetch Website Data **/
 					
 					showP = new showProgress("Checking for new content...", true, false, false);
 					mHandler.post(showP);
 					
 					AQuery aq = new AQuery(ActivityMain.this);
 					aq.ajax(Data.FEED_URL, XmlDom.class, ActivityMain.this, "checkNew");
 					
 					while(true)
 					{
 						if(newChecked)
 							break;
 					}
 					
 					if(newFound)
 					{
 						showP = new showProgress("Updating content...", true, false, false);
 						mHandler.post(showP);
 						
 						// TODO Make sure read/favorite params don't get overwritten
 						aq.ajax(Data.FEED_URL, XmlDom.class, ActivityMain.this, "downloadFeed");
 						Data.overwriteFeedXML(NewsFeed);
 						Log.v("Happy Face", " New stuff found!");
 					}
 					else
 						Log.v("Sad Face", " No new found...");
 					
 					showP = new showProgress("Everything is up to date!", true, false, true);
 					mHandler.post(showP);
 					
 					while (true)
 					{
 						if (fragmentLoaded)
 						{
 							showP = new showProgress("", false, false, false);
 							mHandler.post(showP);
 							break;
 						}
 					}
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
 						Animation a = AnimationUtils.loadAnimation(ActivityMain.this, R.anim.loading_slide_up);
 						Loading.startAnimation(a);
 					}
 					if (Finished)
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
 						Animation a = AnimationUtils.loadAnimation(ActivityMain.this, R.anim.loading_slide_down);
 						Loading.startAnimation(a);
 					}
 					if (Finished)
 					{
 						ProgressBar.setVisibility(View.GONE);
 						ProgressFinished.setVisibility(View.VISIBLE);
 					}
 				}
 			}
 			else
 			{
 				if (currentFragment.equals("Home") && !articleShowing)
 					FragmentHome.updateState();
 			}
 		}
 	}
 	
 	public class showProgress2 implements Runnable
 	{
 		String Progress;
 		
 		public showProgress2(String p)
 		{
 			this.Progress = p;
 		}
 		
 		@Override
 		public void run()
 		{
 			if (Progress.length() > 0)
 			{
 				FragmentLoading.setLoadingText(Progress);
 			}
 			else
 			{
 				emptyFeed = false;
 				selectItem(0);
 			}
 		}
 	}
 	
 	public class showHome implements Runnable
 	{
 		@Override
 		public void run()
 		{
 			selectItem(0);
 			fragmentLoaded = true;
 		}
 	}
 	
 	public void getTESTFeed()
 	{
 		AQuery aq = new AQuery(ActivityMain.this);
 		aq.ajax(Data.FEED_URL, XmlDom.class, ActivityMain.this, "saveFeed");
 	}
 	
 	public void saveFeed(String url, XmlDom xml, AjaxStatus status)
 	{
 		Toast.makeText(ActivityMain.this, "XML = " + xml.toString(), Toast.LENGTH_SHORT).show();
 		List<XmlDom> entries = xml.tags("item");
 		Toast.makeText(ActivityMain.this, "Entry count = " + entries.size(), Toast.LENGTH_SHORT).show();
 		
 		for (XmlDom item : entries)
 		{
 			String title = item.text("title");
 			String date = item.text("pubDate");
 			if (Data.isNewerDate(date, "Fri, 21 Jun 2013 18:00:39 +0000"))
 				return;
 			Toast.makeText(ActivityMain.this, title + "\n" + date + "\n" + Data.isNewerDate(date, "Fri, 21 Jun 2013 18:00:39 +0000"), Toast.LENGTH_SHORT).show();
 		}
 		return;
 	}
 }
