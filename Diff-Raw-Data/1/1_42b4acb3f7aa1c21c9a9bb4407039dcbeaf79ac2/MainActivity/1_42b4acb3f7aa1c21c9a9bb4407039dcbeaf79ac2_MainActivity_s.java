 package com.evacipated.pesterdroid;
 
 import java.util.ArrayList;
 
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.content.SharedPreferences;
 import android.content.pm.ActivityInfo;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.preference.PreferenceManager;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentPagerAdapter;
 import android.support.v4.app.FragmentTransaction;
 import android.support.v4.view.ViewPager;
 import android.widget.ArrayAdapter;
 
 import com.actionbarsherlock.app.ActionBar;
 import com.actionbarsherlock.app.ActionBar.Tab;
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuItem;
 import com.devspark.collapsiblesearchmenu.CollapsibleMenuUtils;
 import com.evacipated.pesterdroid.irc.IRCService;
 import com.evacipated.pesterdroid.irc.IRCService.IRCBinder;
 
 public class MainActivity extends SherlockFragmentActivity implements ServiceConnection {
 	private Menu mainmenu;
 	private ViewPager mViewPager;
 	private MyTabListener mTabsAdapter;
 	private IRCService service;
 	private IRCBinder mBinder;
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         
         
         mViewPager = new ViewPager(this);
         mViewPager.setId(R.id.pager);
         
         //setContentView(R.layout.activity_main);
         setContentView(mViewPager);
         
         // Create ActionBar
         final ActionBar actionbar = getSupportActionBar();
         actionbar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
         
         // Create mood list
         ArrayAdapter<CharSequence> aa = ArrayAdapter.createFromResource(this, R.array.mood_list, R.layout.mood_dropdown_item);
         aa.setDropDownViewResource(R.layout.mood_dropdown_item);
         actionbar.setListNavigationCallbacks(aa, null);
         
         mTabsAdapter = new MyTabListener(this, mViewPager);
         
         // Create temporary tabs
         Tab tab = actionbar.newTab()
         		.setText("#General_Chat")
         		.setIcon(R.drawable.chummy);
         mTabsAdapter.addTab(tab, ConversationFragment.class, null);
         
         tab = actionbar.newTab()
         		.setText("canLover");
         mTabsAdapter.addTab(tab, ConversationFragment.class, null);
     }
     
     @Override
     public void onResume() {
     	super.onResume();
     	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
         if (prefs.getBoolean("orientation_lock", false)) {
         	if (prefs.getString("orientation_value", "Portrait").equals("Portrait")) {
         		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
         	} else {
         		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
         	}
         } else {
         	setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
         }
         
         // Start and connect to service
         Intent intent = new Intent(this, IRCService.class);
         intent.setAction(IRCService.ACTION_FOREGROUND);
         startService(intent);
         bindService(intent, this, 0);
     }
     
     @Override
     public void onPause() {
     	super.onPause();
     	
     	if (mBinder != null && mBinder.getService() != null) {
     		//mBinder.getService().checkService
     	}
     
     	unbindService(this);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getSupportMenuInflater().inflate(R.menu.activity_main, menu);
         
         CollapsibleMenuUtils.addSearchMenuItem(menu, false, null);
         
         mainmenu = menu;
         
         return true;
     }
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
     	Intent intent;
         switch (item.getItemId()) {
         	case R.id.menu_settings:
         		intent = new Intent(this, SettingsActivity.class);
         		startActivity(intent);
         		return true;
         	
         	case R.id.menu_help:
         		intent = new Intent(this, AboutActivity.class);
         		startActivity(intent);
         		return true;
         	
         	case R.id.menu_nickserv:
         		intent = new Intent(this, IRCService.class);
                 intent.setAction(IRCService.ACTION_CONNECT);
                 startService(intent);
         	
         	case R.id.mood1:
         	case R.id.mood2:
         	case R.id.mood3:
         	case R.id.mood4:
         	case R.id.mood5:
         	case R.id.mood6:
         	case R.id.mood7:
         	case R.id.mood8:
         	case R.id.mood9:
         	case R.id.mood10:
         	case R.id.mood11:
         	case R.id.mood12:
         	case R.id.mood13:
         	case R.id.mood14:
         	case R.id.mood15:
         	case R.id.mood16:
         	case R.id.mood17:
         	case R.id.mood18:
         	case R.id.mood19:
         	case R.id.mood20:
         	case R.id.mood21:
         	case R.id.mood22:
         	case R.id.mood23:
         		MenuItem moodmenuitem = mainmenu.findItem(R.id.menu_mood);
         		moodmenuitem.setIcon(item.getIcon());
         		break;
         }
         return super.onOptionsItemSelected(item);
     }
     
     @Override
     public void onDestroy() {
     	super.onDestroy();
     	
     	if (mBinder != null && mBinder.getService() != null) {
     		mBinder.getService().stopSelf();
     	}
     }
 
     public static class MyTabListener extends FragmentPagerAdapter
     				implements ActionBar.TabListener, ViewPager.OnPageChangeListener {
 		private final Context mContext;
         private final ActionBar mActionBar;
         private final ViewPager mViewPager;
         private final ArrayList<TabInfo> mTabs = new ArrayList<TabInfo>();
 		
 		static final class TabInfo
 		{
 			private final Class<?> clss;
             private final Bundle args;
 
             TabInfo(Class<?> _class, Bundle _args)
             {
             	clss = _class;
             	args = _args;
             }
 		}
 
 		public MyTabListener(SherlockFragmentActivity activity, ViewPager pager) {
 			super(activity.getSupportFragmentManager());
 			mContext = activity;
 			mActionBar = activity.getSupportActionBar();
 			mViewPager = pager;
 			mViewPager.setAdapter(this);
 			mViewPager.setOnPageChangeListener(this);
 		}
 		
 		public void addTab(ActionBar.Tab tab, Class<?> clz, Bundle args) {
 			TabInfo info = new TabInfo(clz, args);
 			tab.setTag(info);
 			tab.setTabListener(this);
 			mTabs.add(info);
 			mActionBar.addTab(tab);
 			notifyDataSetChanged();
 		}
 		
 		@Override
 		public int getCount() {
 			return mTabs.size();
 		}
 		
 		@Override
 		public Fragment getItem(int position) {
 			TabInfo info = mTabs.get(position);
 			return Fragment.instantiate(mContext, info.clss.getName(), info.args);
 		}
 		
 		public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
 			
 		}
 		
 		public void onPageSelected(int position) {
 			mActionBar.setSelectedNavigationItem(position);
 		}
 		
 		public void onPageScrollStateChanged(int state) {
 			
 		}
 
 		public void onTabSelected(Tab tab, FragmentTransaction ft) {
 			Object tag = tab.getTag();
 			for (int i=0; i<mTabs.size(); ++i) {
 				if (mTabs.get(i) == tag) {
 					mViewPager.setCurrentItem(i);
 				}
 			}
 		}
 
 		public void onTabUnselected(Tab tab, FragmentTransaction ft) {
 
 		}
 
 		public void onTabReselected(Tab tab, FragmentTransaction ft) {
 		}
     }
     
 	@Override
 	public void onServiceConnected(ComponentName name, IBinder service) {
 		mBinder = (IRCBinder) service;
 	}
 
 	@Override
 	public void onServiceDisconnected(ComponentName name) {
 		mBinder = null;
 	}
 }
