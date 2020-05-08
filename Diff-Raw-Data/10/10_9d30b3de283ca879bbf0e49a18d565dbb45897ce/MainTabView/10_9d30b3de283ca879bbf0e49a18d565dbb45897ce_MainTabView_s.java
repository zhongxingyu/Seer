 package com.android.iliConnect;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Vector;
 import android.content.Context;
 import android.content.Intent;
 import android.content.pm.PackageManager;
 import android.os.Bundle;
 import android.os.Handler;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.view.ViewPager;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.TabHost;
 import android.widget.TabHost.TabContentFactory;
 import android.widget.Toast;
 
 import com.android.iliConnect.PagerAdapter;
 import com.android.iliConnect.Exceptions.NetworkException;
 
 /*
  * The <code>TabsViewPagerFragmentActivity</code> class implements the Fragment activity that maintains a TabHost using a ViewPager. 
  */
 public class MainTabView extends FragmentActivity implements TabHost.OnTabChangeListener, ViewPager.OnPageChangeListener {
 
 	public static MainTabView instance;
 	private TabHost mTabHost;
 	private ViewPager mViewPager;
 	private HashMap<String, TabInfo> mapTabInfo = new HashMap<String, MainTabView.TabInfo>();
 	private PagerAdapter mPagerAdapter;
 	private boolean doubleBackToExitPressedOnce = false;
 
 	/*
 	 *	 
 	 * Maintains extrinsic info of a tab's construct
 	 */
 	private class TabInfo {
 		private String tag;
 		private Class<?> clss;
 		private Bundle args;
 		private Fragment fragment;
 
 		TabInfo(String tag, Class<?> clazz, Bundle args) {
 			this.tag = tag;
 			this.clss = clazz;
 			this.args = args;
 		}
 
 	}
 
 	/*
 	 * A simple factory that returns dummy views to the Tabhost	
 	 */
 	class TabFactory implements TabContentFactory {
 
 		private final Context mContext;
 
 		/*
 		 * @param context
 		 */
 		public TabFactory(Context context) {
 			mContext = context;
 		}
 
 		/*
 		 * @see android.widget.TabHost.TabContentFactory#createTabContent(java.lang.String)
 		 */
 		public View createTabContent(String tag) {
 			View v = new View(mContext);
 			v.setMinimumWidth(0);
 			v.setMinimumHeight(0);
 			return v;
 		}
 
 	}
 
 	public static MainTabView getInstance() {
 		return instance;
 	}
 
 	/*
 	 * @see android.support.v4.app.FragmentActivity#onCreate(android.os.Bundle)
 	 */
 	private Bundle savedInstanceState;
 
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		this.savedInstanceState = savedInstanceState;
 		// Inflate the layout
 		setContentView(R.layout.tabviewcontainer);
 		MainActivity.currentActivity = this;
 		instance = this;
 		// Initialise the TabHost
 		this.initialiseTabHost(savedInstanceState);
 		if (savedInstanceState != null) {
 			mTabHost.setCurrentTabByTag(savedInstanceState.getString("Schreibtisch")); // set the tab as per the saved state
 		}
 		// Intialise ViewPager
 		this.intialiseViewPager();
 	}
 
 	/*
 	 * @see android.support.v4.app.FragmentActivity#onSaveInstanceState(android.os.Bundle)
 	 */
 	protected void onSaveInstanceState(Bundle outState) {
 		outState.putString("tab", mTabHost.getCurrentTabTag()); // save the tab selected
 		super.onSaveInstanceState(outState);
 	}
 
 	/*
 	 * Initialise ViewPager
 	 */
 	private void intialiseViewPager() {
 
 		List<Fragment> fragments = new Vector<Fragment>();
 		fragments.add(Fragment.instantiate(this, Suche.class.getName()));
 		if (hasBackCam()==true){
 		fragments.add(Fragment.instantiate(this, QR.class.getName()));
 		}
 		fragments.add(Fragment.instantiate(this, Uebersicht.class.getName()));
 		fragments.add(Fragment.instantiate(this, Schreibtisch.class.getName()));
 		fragments.add(Fragment.instantiate(this, Termine.class.getName()));
 
 		this.mPagerAdapter = new PagerAdapter(super.getSupportFragmentManager(), fragments);
 
 		//
 		this.mViewPager = (ViewPager) super.findViewById(R.id.viewpager);
 		this.mViewPager.setAdapter(this.mPagerAdapter);
 
 		this.mViewPager.setCurrentItem(2); // set default site "�bersicht"
 
 		this.mViewPager.setOnPageChangeListener(this);
 	}
 
 	/*
 	 * Initialise the Tab Host
 	 */
 	private void initialiseTabHost(Bundle args) {
 		mTabHost = (TabHost) findViewById(android.R.id.tabhost);
 		mTabHost.setup();
 		TabInfo tabInfo = null;
 		// to insert Picture: setIndicator(name, getResources().getDrawable(R.drawable.qr_code_defaul)) ....
 		MainTabView.AddTab(this, this.mTabHost, this.mTabHost.newTabSpec("Tab1").setIndicator("Suche"), (tabInfo = new TabInfo("Tab1", Suche.class, args)));
 		this.mapTabInfo.put(tabInfo.tag, tabInfo);
 		if (hasBackCam()==true){
 		MainTabView.AddTab(this, this.mTabHost, this.mTabHost.newTabSpec("Tab2").setIndicator("QR"), (tabInfo = new TabInfo("Tab2", QR.class, args)));
 		this.mapTabInfo.put(tabInfo.tag, tabInfo);
 		}
 		MainTabView.AddTab(this, this.mTabHost, this.mTabHost.newTabSpec("Tab3").setIndicator("Übersicht"), (tabInfo = new TabInfo("Tab3", Uebersicht.class, args)));
 		this.mapTabInfo.put(tabInfo.tag, tabInfo);
 		MainTabView.AddTab(this, this.mTabHost, this.mTabHost.newTabSpec("Tab4").setIndicator("Schreibtisch"), (tabInfo = new TabInfo("Tab4", Schreibtisch.class, args)));
 		this.mapTabInfo.put(tabInfo.tag, tabInfo);
 		MainTabView.AddTab(this, this.mTabHost, this.mTabHost.newTabSpec("Tab5").setIndicator("Termine"), (tabInfo = new TabInfo("Tab5", Termine.class, args)));
 		this.mapTabInfo.put(tabInfo.tag, tabInfo);
 
 		mTabHost.setCurrentTab(2);// set default tab "�bersicht"
 		for (int i = 0; i < mTabHost.getTabWidget().getChildCount(); i++) {
 			// mTabHost.getTabWidget().getChildAt(i).setPadding(10,10,10,10);
 			mTabHost.getTabWidget().getChildAt(i).getLayoutParams().height = 60;
 
 		}
 		mTabHost.setOnTabChangedListener(this);
 	}
 
 	/*
 	 * Add Tab content to the Tabhost	
 	 */
 	private static void AddTab(MainTabView activity, TabHost tabHost, TabHost.TabSpec tabSpec, TabInfo tabInfo) {
 		// Attach a Tab view factory to the spec
 		tabSpec.setContent(activity.new TabFactory(activity));
 		tabHost.addTab(tabSpec);
 	}
 
 	/*
 	 * @see android.widget.TabHost.OnTabChangeListener#onTabChanged(java.lang.String)
 	 */
 	public void onTabChanged(String tag) {
 
 		int pos = this.mTabHost.getCurrentTab();
 		this.mViewPager.setCurrentItem(pos);
 
 		instance = this;
 
 	}
 
 	public void update() {
 		for (int i = 0; i < mPagerAdapter.getCount(); i++)
 			if (((Fragment) this.mPagerAdapter.getItem(i)).isVisible())
 				((Redrawable) this.mPagerAdapter.getItem(i)).refreshViews();
 	}
 
 	/* 
 	 * @see android.support.v4.view.ViewPager.OnPageChangeListener#onPageScrolled(int, float, int)
 	 */
 	public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
 		// TODO Auto-generated method stub
 
 	}
 
 	/* 
 	 * @see android.support.v4.view.ViewPager.OnPageChangeListener#onPageSelected(int)
 	 */
 	public void onPageSelected(int position) {
 		// TODO Auto-generated method stub
 
 		this.mTabHost.setCurrentTab(position);
 	}
 
 	/* 
 	 * @see android.support.v4.view.ViewPager.OnPageChangeListener#onPageScrollStateChanged(int)
 	 */
 	public void onPageScrollStateChanged(int state) {
 		// TODO Auto-generated method stub
 
 	}
 
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.menu_layout, menu);
 		return true;
 	}
 
 	public boolean onOptionsItemSelected(MenuItem item) {
 		// respond to menu item selection
 		switch (item.getItemId()) {
 		case R.id.settings:
 			Intent settingsActivity = new Intent(instance, Einstellungen.class);
 			startActivity(settingsActivity);
 			return true;
 		case R.id.Logout:
 			// <<<<<<< HEAD
 			MainActivity.instance.logout();
 			MainActivity.instance.localDataProvider.deleteAuthentication();
 
 			// Anmeldebildschirm anzeigen
 			// Intent mainActivity = new Intent(instance, MainActivity.class);
 			// startActivity(mainActivity);
 
 			return true;
 			// =======
 			/*
 			// TODO: Login-Ansicht aufrufen, autLogin auf false setzen und Daten aus XML entfernen
 			LocalDataProvider prov = new LocalDataProvider();
 			
 			prov.deleteAuthentication();
 			
 			// Anmeldebildschirm anzeigen
 			Intent mainActivity = new Intent(instance, MainActivity.class);
 			startActivity(mainActivity);
 
 			//finish();
 			>>>>>>> branch 'master' of https://github.com/Develman/IliConnect.git*/
 		case R.id.refresh:
 			try {
 				MainActivity.instance.sync(instance, true);
 			} catch (NetworkException e) {
 				// MainActivity.instance.showToast(e.getMessage());
 				MessageBuilder.exception_message(MainTabView.instance, e.getMessage());
 				e.printStackTrace();
 			}
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	// Methode um View wechseln zu können
 	public void changeViewTo(int i) {
 		mTabHost.setCurrentTab(i);
 		this.mViewPager.setCurrentItem(i);
 
 	}
 
	@Override
	protected void onResume() {
	    super.onResume();
	    // .... other stuff in my onResume ....
	    this.doubleBackToExitPressedOnce = false;
	}
 	 private boolean hasBackCam() {
 		  PackageManager pm = MainTabView.instance.getPackageManager();
 		  return pm.hasSystemFeature(PackageManager.FEATURE_CAMERA);
 		 }
 	@Override
 	public void onBackPressed() {
 		// Immer auf Überischt zurückgehen
 		mTabHost.setCurrentTab(2);
 		this.mViewPager.setCurrentItem(2);
 		if (doubleBackToExitPressedOnce) {
 	        super.onBackPressed();	  
 	        MainActivity.instance.finish();
 	        finish();
 	        return;
 	    }
 	    this.doubleBackToExitPressedOnce = true;
 	    Toast.makeText(this, "Erneut drücken, um das Programm zu beenden.", Toast.LENGTH_SHORT).show();
 	    //reset der DoubleBackToExitPressedOnce nach 2s
 	    new Handler().postDelayed(new Runnable() {
 
             public void run() {
              doubleBackToExitPressedOnce=false;
             }
         }, 2000);
 		// super.onBackPressed();
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		MainActivity.currentActivity = this;
 
 	};
 
 	@Override
 	protected void onRestart() {
 		MainActivity.currentActivity = this;
 		update();
 
 		super.onRestart();
 	}
 
 }
