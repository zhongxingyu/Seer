 package ch.hsr.hsrlunch;
 
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 
 import net.simonvt.widget.MenuDrawer;
 import net.simonvt.widget.MenuDrawerManager;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
 import android.os.Build;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.support.v4.view.ViewPager;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 import ch.hsr.hsrlunch.controller.BadgeUpdater;
 import ch.hsr.hsrlunch.controller.PersistenceFactory;
 import ch.hsr.hsrlunch.controller.WeekDataSource;
 import ch.hsr.hsrlunch.model.Badge;
 import ch.hsr.hsrlunch.model.Offer;
 import ch.hsr.hsrlunch.model.Week;
 import ch.hsr.hsrlunch.model.WorkDay;
 import ch.hsr.hsrlunch.ui.CustomMenuView;
 import ch.hsr.hsrlunch.ui.SettingsActivity;
 import ch.hsr.hsrlunch.util.DBOpenHelper;
 import ch.hsr.hsrlunch.util.MenuViewAdapter;
 import ch.hsr.hsrlunch.util.OnBadgeResultListener;
 import ch.hsr.hsrlunch.util.TabPageAdapter;
 
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuItem;
 import com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener;
 import com.actionbarsherlock.widget.ShareActionProvider;
 import com.viewpagerindicator.TabPageIndicator;
 
 
 public class MainActivity extends SherlockFragmentActivity implements
 		OnSharedPreferenceChangeListener, OnBadgeResultListener {
 	private static final int SHOW_PREFERENCES = 1;
 	private static final long WEEK_IN_MILLISECONDS = 7 * 24 * 60 * 60 * 1000;
 
 	public static boolean dataAvailable = true;
 	public static WorkDay selectedDay;
 	public static Offer selectedOffer;
 	public static String[] offertitles;
 
 	private ViewPager mViewPager;
 	private MenuDrawerManager mMenuDrawer;
 	private TabPageAdapter mTabPageAdapter;
 	private ShareActionProvider provider;
 	private MenuViewAdapter mvAdapter;
 	private LinearLayout badgeLayout;
 	private CustomMenuView menuView;
 	private Week week;
 
 	// Instanciate DB (onCreate) and create PersistenceFactory = Fill all
 	// Objects from DB
 	private DBOpenHelper dbHelper;
 	private PersistenceFactory persistenceFactory;
 
 	// Attributes for Preferences in SettingActivity
 	private boolean showBadgeInfo = false;
 	private int favouriteMenu;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		onCreatePersistence();
 
 		offertitles = getResources().getStringArray(R.array.menu_title_entries);
 
 		if (Build.VERSION.SDK_INT >= 14) {
 			PreferenceManager.setDefaultValues(this, R.xml.userpreference,false);
 		} else {
 			PreferenceManager.setDefaultValues(this,R.xml.userpreference_oldver, false);
 		}
 		updatePreferences();
 
 		onCreateMenuDrawer();
 		mMenuDrawer.setContentView(R.layout.activity_main);
 		mMenuDrawer.setMenuView(menuView);
 
 		getSupportActionBar().setHomeButtonEnabled(true);
 
 		// sp�ter furtschmeissen
 		init();
 
 		onCreateViewPager();
 
 		badgeLayout = (LinearLayout) findViewById(R.id.badge);
 		updateBadgeView();
 
 	}
 
 	private void onCreatePersistence() {
 		dbHelper = new DBOpenHelper(this);
 		persistenceFactory = new PersistenceFactory(dbHelper);
 		week = persistenceFactory.getWeek();
 	}
 
 	private void updatePreferences() {
 	
 		Context context = getApplicationContext();
 		SharedPreferences prefs = PreferenceManager
 				.getDefaultSharedPreferences(context);
 	
 		showBadgeInfo = prefs.getBoolean(SettingsActivity.PREF_BADGE, false);
 	
 		String temp = prefs.getString(SettingsActivity.PREF_FAV_MENU,
 				offertitles[0]);
 	
 		// update index von favourite menu
 		for (int i = 0; i <= offertitles.length; i++) {
 			if (temp.equals(offertitles[i])) {
 				favouriteMenu = i;
 				return;
 			}
 		}
 	}
 
 	private void onCreateMenuDrawer() {
 		menuView = new CustomMenuView(this);
 		mMenuDrawer = new MenuDrawerManager(this, MenuDrawer.MENU_DRAG_CONTENT);
 		mvAdapter = new MenuViewAdapter(this, mMenuDrawer);
 		menuView.setAdapter(mvAdapter);
 		menuView.setOnScrollChangedListener(new CustomMenuView.OnScrollChangedListener() {
 			@Override
 			public void onScrollChanged() {
 				mMenuDrawer.getMenuDrawer().invalidate();
 			}
 		});
 		menuView.setOnItemClickListener(new OnItemClickListener() {
 			@Override
 			public void onItemClick(AdapterView<?> parent, View view,
 					int position, long id) {
 				mvAdapter.setActiveEntry(position);
 				mMenuDrawer.setActiveView(view, position); // falls vorig Zeit^^
 				mMenuDrawer.closeMenu();
 				if (position >= 1 && position <= 6) {
 					setSelectedDay(position - 1);
 				} else {
 					// starte Settings-Activity
 					Intent i = new Intent(getApplicationContext(),
 							SettingsActivity.class);
 					startActivityForResult(i, SHOW_PREFERENCES);
 				}
 			}
 		});
 	}
 
 	/*
 	 * statisches Füllen der Daten solange zugriff auf DB noch nicht
 	 * implementiert ist
 	 */
 	private void init() {
 	
 //		badge = new Badge(999.99, new Date().getTime());
 		
 	
 		GregorianCalendar cal = new GregorianCalendar();
 		/*
 		 * Samstags und Sonntags stehen keine Informationen bereit
 		 */
 		if (cal.get(Calendar.DAY_OF_WEEK) == 1 /* sonntag */
 				|| cal.get(Calendar.DAY_OF_WEEK) == 7 /* samstag */) {
 			dataAvailable = false;
 			setSelectedDay(1);
 		} else {
 			dataAvailable = true;
 			setSelectedDay((cal.get(Calendar.DAY_OF_WEEK) + 5) % 7);
 		}
 	}
 
 	private void onCreateViewPager() {
 		mTabPageAdapter = new TabPageAdapter(getSupportFragmentManager());
 		mViewPager = (ViewPager) findViewById(R.id.viewpager);
 		mViewPager.setAdapter(mTabPageAdapter);
 		mViewPager.setCurrentItem(favouriteMenu, true);
 
 		TabPageIndicator indicator = (TabPageIndicator) findViewById(R.id.indicator);
 		indicator.setViewPager(mViewPager);
 		indicator.setCurrentItem(favouriteMenu);
 
 		// Listener für "pageChange Event"
 		ViewPager.SimpleOnPageChangeListener pageChangeListener = new ViewPager.SimpleOnPageChangeListener() {
 			@Override
 			public void onPageSelected(int position) {
 				super.onPageSelected(position);
 				selectedOffer = selectedDay.getOfferList().get(position);
 				provider.setShareIntent(getDefaultShareIntent());
 			}
 		};
 		indicator.setOnPageChangeListener(pageChangeListener);
 	}
 
 	private void updateBadgeView() {
 		if (showBadgeInfo) {
 			badgeLayout.setVisibility(View.VISIBLE);
 			//hole Informationen aus der DB:
 			onBadgeUpdate();
 			//initiate Update
 			BadgeUpdater service = new BadgeUpdater();
 			service.setBackend(persistenceFactory);
			service.setContext(this);
 			service.setListener(this);
 			service.execute();
 		} else {
 			badgeLayout.setVisibility(View.GONE);
 		}
 	}
 
 	private void updateViewPager() {
 		if (mViewPager != null)
 			mViewPager.setCurrentItem(favouriteMenu, true);
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(
 			com.actionbarsherlock.view.MenuItem item) {
 		switch (item.getItemId()) {
 		case android.R.id.home: // this is the app icon of the actionbar
 			mMenuDrawer.toggleMenu();
 			break;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 	/*
 	 * setzen des ausgewählten Tages(selectedDay) des viewPagers
 	 * 
 	 * @param: int position 0-4, 0 = Montag, ..., 4=Freitag
 	 */
 	private void setSelectedDay(int position) {
 		selectedDay = week.getDayList().get(position);
 		selectedOffer = selectedDay.getOfferList().get(favouriteMenu);
 		if (mTabPageAdapter != null) {
 			mTabPageAdapter.notifyDataSetChanged();
 			updateViewPager();
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getSupportMenuInflater().inflate(R.menu.actionbar_menu, menu);
 
 		MenuItem item = menu.findItem(R.id.menu_share);
 		provider = (ShareActionProvider) item.getActionProvider();
 		provider.setShareHistoryFileName(ShareActionProvider.DEFAULT_SHARE_HISTORY_FILE_NAME);
 		provider.setShareIntent(getDefaultShareIntent());
 
 		MenuItem refresh = menu.findItem(R.id.menu_refresh);
 		refresh.setOnMenuItemClickListener(new OnMenuItemClickListener() {
 
 			@Override
 			public boolean onMenuItemClick(MenuItem item) {
 				Toast.makeText(getApplicationContext(), "Update",
 						Toast.LENGTH_SHORT).show();
 				persistenceFactory.updateAllOffers();
 				if (mTabPageAdapter != null) {
 					mTabPageAdapter.notifyDataSetChanged();
 				}
 				return false;
 			}
 		});
 
 		MenuItem settings = menu.findItem(R.id.menu_settings);
 		settings.setOnMenuItemClickListener(new OnMenuItemClickListener() {
 
 			@Override
 			public boolean onMenuItemClick(MenuItem item) {
 				Intent i = new Intent(getApplicationContext(),
 						SettingsActivity.class);
 				startActivityForResult(i, SHOW_PREFERENCES);
 				return true;
 			}
 		});
 
 		return true;
 	}
 
 	private static Intent getDefaultShareIntent() {
 		Intent intent = new Intent(Intent.ACTION_SEND);
 		intent.setType("text/plain");
 		intent.putExtra(android.content.Intent.EXTRA_SUBJECT,
 				"HSR Menu @ " + selectedDay.getDate() + "-"
 						+ offertitles[selectedOffer.getOfferType()]);
 		intent.putExtra(android.content.Intent.EXTRA_TEXT,
 				selectedOffer.getOfferAndPrice());
 		return intent;
 	}
 
 	public boolean DBUpdateNeeded() {
 		// TODO: check if 7 days difference + check if sunday is past
 		long dbage = new WeekDataSource(dbHelper).getWeekLastUpdate();
 		long actday = new Date().getTime();
 		long difference = 3 * 24 * 60 * 60 * 1000; // Weil der 1.1.1970 ein
 													// Donnerstag war
 
 		if (actday - ((actday + difference) % WEEK_IN_MILLISECONDS) > dbage)
 			return true; // dbage ist aus letzer Woche
 		return false; // dbage ist neuer als der letzte Montag
 
 	}
 
 	@Override
 	public void onActivityResult(int requestCode, int resultCode, Intent data) {
 		super.onActivityResult(requestCode, resultCode, data);
 		if (requestCode == SHOW_PREFERENCES) {
 			updatePreferences();
 			// Badge Information updaten und wenn nötig anzeigen
 			updateBadgeView();
 			updateViewPager();
 		}
 	}
 
 	@Override
 	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
 			String key) {
 		updatePreferences();
 		// Badge Information updaten und wenn nötig anzeigen
 		updateBadgeView();
 		updateViewPager();
 	}
 
 	@Override
 	public void onBadgeUpdate() {
 		Badge badge = persistenceFactory.getBadge();
 		TextView badgeAmount = (TextView) findViewById(R.id.amount);
 		badgeAmount.setText(badge.getAmount() + " CHF");
 		TextView badgeLastUpdate = (TextView) findViewById(R.id.lastUpdate);
 		badgeLastUpdate.setText(badge.getLastUpdateString());
 	}
 
 }
