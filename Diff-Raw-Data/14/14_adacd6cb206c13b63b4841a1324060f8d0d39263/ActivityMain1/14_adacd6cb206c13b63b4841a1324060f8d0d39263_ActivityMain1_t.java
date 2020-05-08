 package com.tzachsolomon.spendingtracker;
 
 import static com.tzachsolomon.spendingtracker.ClassCommonUtilities.DebugActivityMain;
 import static com.tzachsolomon.spendingtracker.ClassCommonUtilities.PREF_KEY_DEBUG_ACTIVITY_MAIN;
 import static com.tzachsolomon.spendingtracker.ClassCommonUtilities.PREF_KEY_DEBUG_DB;
 import static com.tzachsolomon.spendingtracker.ClassCommonUtilities.PREF_KEY_DEBUG_FRAGMENT_GENERAL;
 import static com.tzachsolomon.spendingtracker.ClassCommonUtilities.PREF_KEY_DEBUG_FRAGMENT_REMINDER_LOCATION;
 import static com.tzachsolomon.spendingtracker.ClassCommonUtilities.PREF_KEY_DEBUG_FRAGMENT_REMINDER_TIME;
 import static com.tzachsolomon.spendingtracker.ClassCommonUtilities.PREF_KEY_DEBUG_SERVICE_REMINDER_TIME;
 import static com.tzachsolomon.spendingtracker.ClassCommonUtilities.TYPE_REMINDER_TIME_EVERYDAY;
 import static com.tzachsolomon.spendingtracker.ClassCommonUtilities.TYPE_REMINDER_TIME_MONTHLY;
 import static com.tzachsolomon.spendingtracker.ClassCommonUtilities.TYPE_REMINDER_TIME_WEEKLY;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 
 import android.app.AlarmManager;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.app.ProgressDialog;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.SharedPreferences;
 import android.location.Location;
 import android.os.Bundle;
 import android.os.SystemClock;
 import android.preference.PreferenceManager;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentPagerAdapter;
 import android.support.v4.app.FragmentTransaction;
 import android.support.v4.view.ViewPager;
 import android.widget.Toast;
 
 import com.actionbarsherlock.app.ActionBar;
 import com.actionbarsherlock.app.ActionBar.Tab;
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 import com.tzachsolomon.spendingtracker.ClassDbEngine.DatabaseImportExportListener;
 import com.tzachsolomon.spendingtracker.FragmentAdminDb.AdminDbListener;
 import com.tzachsolomon.spendingtracker.FragmentDialogCategoriesManager.CategoriesManagerListener;
 import com.tzachsolomon.spendingtracker.FragmentDialogCategoryAdd.AddCategoryListener;
 import com.tzachsolomon.spendingtracker.FragmentDialogEditSpentEntry.UpdateSpentEntryListener;
 import com.tzachsolomon.spendingtracker.FragmentDialogRemindersTimeManage.ReminderTimeListener;
 import com.tzachsolomon.spendingtracker.FragmentDialogSpentNotification.FragmentDialogSpentListener;
 import com.tzachsolomon.spendingtracker.FragmentEntries.SpentEntryListener;
 import com.tzachsolomon.spendingtracker.FragmentGeneral.ButtonAddEntrySpentListener;
 import com.tzachsolomon.spendingtracker.FragmentGeneral.ButtonCategoriesEditListener;
 import com.tzachsolomon.spendingtracker.FragmentRemindersTime.TimeReminderListener;
 
 public class ActivityMain1 extends SherlockFragmentActivity implements
 		ButtonAddEntrySpentListener, ButtonCategoriesEditListener,
 		TimeReminderListener, SpentEntryListener, AddCategoryListener,
 		CategoriesManagerListener, UpdateSpentEntryListener,
 		ReminderTimeListener, FragmentDialogSpentListener, AdminDbListener,
 		DatabaseImportExportListener {
 
 	private final static String XMLFILE = "spendingTracker.xml";
 	private ViewPager mViewPager;
 	private TabsAdapter mTabsAdapter;
 	private ClassDbEngine mSpendingTrackerDbEngine;
 	private SharedPreferences mSharedPreferences;
 	private FragmentGeneral mFragemtGeneral;
 	private FragmentEntries mFragmentEntries;
 	private FragmentDialogCategoriesManager mFragmentCategoriesManager;
 	private FragmentDialogRemindersTimeManage mFragmentDialogRemindersTimeManage;
 	private PendingIntent mTimeAlarmSender;
 	private NotificationManager mNotificationManager;
 	private BroadcastReceiver m_BroadcastReceiverLocationUpdate;
 	private FragmentRemindersLocation mFragmentRemindersLocation;
 	private PendingIntent m_LocationAlarmSender;
 	private ArrayList<Location> mLocations;
 	private ProgressDialog mProgressDialog;
 
 	// TODO: delete entry with dialog
 	// 
 	// TODO: admin reminders
 	// TODO: default TAB
 	// TODO: first time setup
 	// TODO: share spending
 	// TODO: add option to place tab on bottom instead of up
 	// TODO: add colors to categories
 	// TODO: add graphs
 	// TODO: auto export database (auto backup)
 	// TODO: auto import in case re installing the app
 	// TODO: add learning mode
 	// TODO: scrolling of spent entries
 
 	@Override
 	protected void onCreate(Bundle arg0) {
 		//
 		super.onCreate(arg0);
 		mViewPager = new ViewPager(this);
 		mViewPager.setId(R.id.pager);
 
 		setContentView(mViewPager);
 
 		initializeVariables();
 
 		mTimeAlarmSender = PendingIntent
 				.getService(ActivityMain1.this, 0, new Intent(
 						ActivityMain1.this, SpendingTrackerTimeService.class),
 						0);
 
 		m_LocationAlarmSender = PendingIntent.getService(ActivityMain1.this, 0,
 				new Intent(ActivityMain1.this,
 						SpendingTrackerLocationService.class), 0);
 
 		updatePreferences();
 
 		initializeActionBar();
 	}
 
 	public void updatePreferences() {
 		ClassCommonUtilities.DEBUG_DB = mSharedPreferences.getBoolean(
 				PREF_KEY_DEBUG_DB, false);
 		ClassCommonUtilities.DEBUG_FRAGMENT_GENERAL = mSharedPreferences
 				.getBoolean(PREF_KEY_DEBUG_FRAGMENT_GENERAL, false);
 		ClassCommonUtilities.DEBUG_FRAGMENT_REMINDER_LOCATION = mSharedPreferences
 				.getBoolean(PREF_KEY_DEBUG_FRAGMENT_REMINDER_LOCATION, false);
 		ClassCommonUtilities.DEBUG_FRAGMENT_REMINDER_TIME = mSharedPreferences
 				.getBoolean(PREF_KEY_DEBUG_FRAGMENT_REMINDER_TIME, false);
 		ClassCommonUtilities.DEBUG_SERVICE_REMINDER_TIME = mSharedPreferences
 				.getBoolean(PREF_KEY_DEBUG_SERVICE_REMINDER_TIME, false);
 		ClassCommonUtilities.DEBUG_ACTIVITY_MAIN = mSharedPreferences
 				.getBoolean(PREF_KEY_DEBUG_ACTIVITY_MAIN, false);
 	}
 
 	private void initializeVariables() {
 		//
 		mSpendingTrackerDbEngine = new ClassDbEngine(this);
 		mSpendingTrackerDbEngine.setDatabaseImportExportListener(this);
 		mSharedPreferences = PreferenceManager
 				.getDefaultSharedPreferences(getBaseContext());
 
 		mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
 
 	}
 
 	private void initializeActionBar() {
 		//
 
 		ActionBar actionBar = getSupportActionBar();
 		actionBar.setDisplayShowHomeEnabled(false);
 		actionBar.setDisplayShowTitleEnabled(false);
 		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
 		actionBar.setDisplayHomeAsUpEnabled(true);
 		actionBar.setDisplayShowHomeEnabled(true);
 		actionBar.setDisplayShowTitleEnabled(true);
 
 		actionBar.removeAllTabs();
 
 		mTabsAdapter = new TabsAdapter(this, mViewPager);
 
 		mTabsAdapter.addTab(actionBar.newTab().setText("General"),
 				FragmentGeneral.class, null);
 		mTabsAdapter.addTab(actionBar.newTab().setText("Entries"),
 				FragmentEntries.class, null);
 		if (mSharedPreferences.getBoolean("checkBoxPrefReminderTime", true)) {
 			mTabsAdapter.addTab(actionBar.newTab().setText("Time Reminder"),
 					FragmentRemindersTime.class, null);
 		}
 
 		if (mSharedPreferences
 				.getBoolean("checkBoxPrefReminderLocation", false)) {
 			mTabsAdapter.addTab(
 					actionBar.newTab().setText("Location Reminder"),
 					FragmentRemindersLocation.class, null);
 		}
 		mTabsAdapter.addTab(actionBar.newTab().setText("Admin"),
 				FragmentAdminDb.class, null);
 
 	}
 
 	public static class TabsAdapter extends FragmentPagerAdapter implements
 			ActionBar.TabListener, ViewPager.OnPageChangeListener {
 		private final Context mContext;
 		private final ActionBar mActionBar;
 		private final ViewPager mViewPager;
 		private final ArrayList<TabInfo> mTabs = new ArrayList<TabInfo>();
 
 		static final class TabInfo {
 			private final Class<?> clss;
 			private final Bundle args;
 
 			TabInfo(Class<?> _class, Bundle _args) {
 				clss = _class;
 				args = _args;
 			}
 		}
 
 		public TabsAdapter(SherlockFragmentActivity activity, ViewPager pager) {
 			super(activity.getSupportFragmentManager());
 			mContext = activity;
 			mActionBar = activity.getSupportActionBar();
 			mViewPager = pager;
 			mViewPager.setAdapter(this);
 			mViewPager.setOnPageChangeListener(this);
 
 		}
 
 		public void addTab(ActionBar.Tab tab, Class<?> clss, Bundle args) {
 			TabInfo info = new TabInfo(clss, args);
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
 			return Fragment.instantiate(mContext, info.clss.getName(),
 					info.args);
 		}
 
 		public void onPageScrolled(int position, float positionOffset,
 				int positionOffsetPixels) {
 		}
 
 		public void onPageSelected(int position) {
 			mActionBar.setSelectedNavigationItem(position);
 		}
 
 		public void onPageScrollStateChanged(int state) {
 		}
 
 		public void onTabSelected(Tab tab, FragmentTransaction ft) {
 			Object tag = tab.getTag();
 			for (int i = 0; i < mTabs.size(); i++) {
 				if (mTabs.get(i) == tag) {
 					mViewPager.setCurrentItem(i);
 				}
 			}
 		}
 
 		public void setTabPage(int i) {
 			mViewPager.setCurrentItem(i);
 
 		}
 
 		public void onTabUnselected(Tab tab, FragmentTransaction ft) {
 
 		}
 
 		public void onTabReselected(Tab tab, FragmentTransaction ft) {
 		}
 
 	}
 
 	public void onButtonCategoriesEditClicked() {
 		//
 		FragmentDialogCategoriesManager fragmentCategoriesManager = new FragmentDialogCategoriesManager();
 
 		fragmentCategoriesManager.show(getSupportFragmentManager(),
 				"FragmentCategoriesManager");
 
 	}
 
 	@Override
 	public boolean onMenuItemSelected(int featureId, MenuItem item) {
 		//
 		switch (item.getItemId()) {
 
 		case android.R.id.home:
 			mTabsAdapter.setTabPage(0);
 			break;
 		case R.id.menuExit:
 			finish();
 			break;
 		case R.id.menuPrefernces:
 			Intent intent = new Intent(ActivityMain1.this,
 					ActivityPreferences.class);
 
 			startActivityForResult(intent,
 					ClassCommonUtilities.REQUEST_CODE_ACTIVITY_PREFERENCES);
 
 			break;
 		default:
 			break;
 		}
 		return true;
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		//
 		MenuInflater inflater = getSupportMenuInflater();
 		inflater.inflate(R.menu.mainmenu, menu);
 		return true;
 	}
 
 	public void onAddTimeReminderClicked(ArrayList<ClassTypeReminderTime> values) {
 		//
 		String amount = mFragemtGeneral.getAmount();
 		String category = mFragemtGeneral.getCategory();
 		StringBuilder sb = new StringBuilder();
 
 		if (amount.length() > 0) {
 
 			if (!values.isEmpty()) {
 
 				for (ClassTypeReminderTime reminderTime : values) {
 					mSpendingTrackerDbEngine.insertNewTimeReminder(
 							reminderTime.getmType(), reminderTime.getmHour(),
 							reminderTime.getmMinute(), reminderTime.getmDay(),
 							amount, category);
 					sb.append(reminderTime.toToastMessage());
 					sb.append("\n");
 
 				}
 				sb.append("Category: " + category);
 				sb.append("Amount: " + amount);
 				Toast.makeText(ActivityMain1.this, sb.toString(),
 						Toast.LENGTH_LONG).show();
 				sb.setLength(0);
 			}
 		} else {
 			Toast.makeText(ActivityMain1.this, "Please fill in amount",
 					Toast.LENGTH_LONG).show();
 			mTabsAdapter.setTabPage(0);
 		}
 
 	}
 
 	public void onButtonAddEntrySpentClicked(Bundle values) {
 		//
 		String amount = values.getString("amount");
 		String category = values.getString("category");
 		String comments = values.getString("comments");
 		boolean validEntry = true;
 
 		if (amount.isEmpty()) {
 			Toast.makeText(ActivityMain1.this, "Please fill in the amount",
 					Toast.LENGTH_LONG).show();
 			validEntry = false;
 		}
 
 		if (category.isEmpty()) {
 			Toast.makeText(ActivityMain1.this, "Please fill in the category",
 					Toast.LENGTH_LONG).show();
 			validEntry = false;
 		}
 
 		// if ( comments.isEmpty() ){
 		// Toast.makeText(ActivityMain1.this, "Please fill in the comments",
 		// Toast.LENGTH_LONG).show();
 		// validEntry = false;
 		// }
 
 		if (validEntry) {
 			mSpendingTrackerDbEngine.insertNewSpending(amount, category,
 					comments, null);
			// showing message to user that entry was added
			if (mSharedPreferences.getBoolean("cbShowEntryAdded", true)) {
				Toast.makeText(ActivityMain1.this,
						getString(R.string.toastMessageEntryAdded),
						Toast.LENGTH_SHORT).show();
			}
 		}
 
		
 
 		if (mFragemtGeneral != null) {
 			mFragemtGeneral.updateSpentDayWeekMonth();
 
 		}
 
 		if (mFragmentEntries != null) {
 			mFragmentEntries.updateListViewAdapter();
 		}
 
 	}
 
 	public void setFragmentGeneralRef(String tag) {
 		//
 		mFragemtGeneral = (FragmentGeneral) getSupportFragmentManager()
 				.findFragmentByTag(tag);
 
 	}
 
 	public void setFragmentEntriesRef(String tag) {
 		//
 		mFragmentEntries = (FragmentEntries) getSupportFragmentManager()
 				.findFragmentByTag(tag);
 
 	}
 
 	public void setFragmentReminderTimeManagerRef(String tag) {
 		//
 		mFragmentDialogRemindersTimeManage = (FragmentDialogRemindersTimeManage) getSupportFragmentManager()
 				.findFragmentByTag(tag);
 
 	}
 
 	public void onSpentEntryDeleted(String rowId) {
 		//
 		mSpendingTrackerDbEngine.deleteSpentEntryByRowId(rowId);
 		mFragmentEntries.updateListViewAdapter();
 		mFragemtGeneral.updateSpentDayWeekMonth();
 
 	}
 
 	public void onSpentEntryEditRequest(Bundle values) {
 		//
 		FragmentDialogEditSpentEntry fragment = new FragmentDialogEditSpentEntry();
 		fragment.setArguments(values);
 		fragment.show(getSupportFragmentManager(),
 				"FragmentDialogEditSpentEntry");
 
 	}
 
 	public void onAddCategorySaveClicked(String categoryName) {
 		//
 		mSpendingTrackerDbEngine.insertNewCategory(categoryName);
 		Toast.makeText(ActivityMain1.this, categoryName + " category added",
 				Toast.LENGTH_LONG).show();
 		mFragemtGeneral.initSpinnerCategories();
 		mFragmentCategoriesManager.initCategories();
 	}
 
 	public void setFragmentCategoriesManagerRef(String tag) {
 		//
 		mFragmentCategoriesManager = (FragmentDialogCategoriesManager) getSupportFragmentManager()
 				.findFragmentByTag(tag);
 	}
 
 	public void setFragmentReminderLocationRef(String tag) {
 		//
 		mFragmentRemindersLocation = (FragmentRemindersLocation) getSupportFragmentManager()
 				.findFragmentByTag(tag);
 		while (!mLocations.isEmpty()) {
 			mFragmentRemindersLocation.updateLocationTextViews(mLocations
 					.get(0));
 			mLocations.remove(0);
 		}
 
 	}
 
 	public void onDeleteCategoryClicked(String categoryName) {
 		//
 		mSpendingTrackerDbEngine.deleteCategory(categoryName);
 		Toast.makeText(ActivityMain1.this,
 				"Category " + categoryName + " deleted!", Toast.LENGTH_LONG)
 				.show();
 		mFragemtGeneral.initSpinnerCategories();
 		mFragmentCategoriesManager.initCategories();
 
 	}
 
 	private void checkServiceStatus() {
 		//
 		try {
 			boolean checkBoxPreferencsReminderService = mSharedPreferences
 					.getBoolean("checkBoxPreferencsReminderService", true);
 
 			// checking service always should be on and the service is not
 			// running
 			cancelTimeAlarmManager();
 
 			if (checkBoxPreferencsReminderService) {
 
 				startTimeAlarmManager();
 			}
 		} catch (Exception e) {
 			DebugActivityMain(e.toString());
 		}
 
 	}
 
 	@Override
 	protected void onResume() {
 		//
 		super.onResume();
 		checkServiceStatus();
 		checkPendingReminder();
 		startLocationService();
 	}
 
 	/**
 	 * Function will start the location service and cancel the location alarm
 	 * manager in order prevent from the alarm manager to stop the location
 	 * service
 	 * 
 	 */
 	private void startLocationService() {
 		//
 
 		boolean checkBoxPreferencesLocationService = mSharedPreferences
 				.getBoolean("checkBoxPrefReminderLocation", false);
 
 		cancelLocationAlarmManager();
 
 		// checking the location service option is enabled
 		if (checkBoxPreferencesLocationService) {
 			// the location service is enabled, thus disabling the alarm
 			// location manager
 			mLocations = new ArrayList<Location>();
 			Intent service = new Intent(this,
 					SpendingTrackerLocationService.class);
 
 			startService(service);
 
 		}
 		IntentFilter iFilter = new IntentFilter(
 				SpendingTrackerLocationService.ACTION_FILTER);
 
 		m_BroadcastReceiverLocationUpdate = new BroadcastReceiver() {
 
 			@Override
 			public void onReceive(Context context, Intent intent) {
 				//
 				Location location = (Location) intent.getExtras().get(
 						"location");
 
 				Toast.makeText(ActivityMain1.this, "Location updated",
 						Toast.LENGTH_SHORT).show();
 
 				if (mFragmentRemindersLocation != null) {
 					mFragmentRemindersLocation
 							.updateLocationTextViews(location);
 				} else {
 					mLocations.add(location);
 				}
 
 			}
 		};
 
 		registerReceiver(m_BroadcastReceiverLocationUpdate, iFilter);
 
 	}
 
 	private void cancelLocationAlarmManager() {
 		//
 		AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
 		am.cancel(m_LocationAlarmSender);
 	}
 
 	/**
 	 * Function will start the location service and cancel the location alarm
 	 * manager in order prevent from the alarm manager to stop the location
 	 * service
 	 * 
 	 */
 	private void stopLocationService() {
 		//
 
 		boolean checkBoxPreferencesLocationService = mSharedPreferences
 				.getBoolean("checkBoxPrefReminderLocation", false);
 
 		Intent service = new Intent(this, SpendingTrackerLocationService.class);
 
 		stopService(service);
 
 		// checking the location service option is enabled
 		if (checkBoxPreferencesLocationService) {
 			// the location service is enabled, thus disabling the alarm
 			// location manager
 			startLocationAlarmManager();
 
 		}
 
 	}
 
 	private void startLocationAlarmManager() {
 		//
 		Calendar firstTime = Calendar.getInstance();
 		firstTime.setTimeInMillis(SystemClock.elapsedRealtime());
 
 		// Schedule the alarm!
 		AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
 		am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
 				firstTime.getTimeInMillis(), 60000, m_LocationAlarmSender);
 
 	}
 
 	@Override
 	protected void onPause() {
 		//
 		super.onPause();
 		unregisterReceiver(m_BroadcastReceiverLocationUpdate);
 		stopLocationService();
 	}
 
 	private void checkPendingReminder() {
 		// Function checks if there is a pending reminder
 		// Currently only support one reminder
 
 		Bundle extras = getIntent().getExtras();
 
 		int flag = getIntent().getFlags()
 				& Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY;
 
 		if (extras != null && flag == 0) {
 
 			getIntent().getExtras().clear();
 
 			DebugActivityMain("Found extras, checking if starting from reminder");
 			boolean isReminder = false;
 			if (extras.containsKey(TYPE_REMINDER_TIME_EVERYDAY)) {
 				DebugActivityMain("Starting from everyday reminder");
 				getIntent().removeExtra(TYPE_REMINDER_TIME_EVERYDAY);
 
 				isReminder = true;
 
 			} else if (extras.containsKey(TYPE_REMINDER_TIME_WEEKLY)) {
 				DebugActivityMain("Starting from weekly reminder");
 				getIntent().removeExtra(TYPE_REMINDER_TIME_WEEKLY);
 				isReminder = true;
 			} else if (extras.containsKey(TYPE_REMINDER_TIME_MONTHLY)) {
 				DebugActivityMain("Starting from monthly reminder");
 				getIntent().removeExtra(TYPE_REMINDER_TIME_MONTHLY);
 				isReminder = true;
 
 				// TODO: this might be wrong!!!!
 			} else if (extras.containsKey(ClassDbEngine.KEY_REMINDER_TYPE)) {
 				DebugActivityMain("Starting from location reminder");
 				isReminder = true;
 				String rowId = extras.getString(ClassDbEngine.KEY_REMINDER_ID);
 				mSpendingTrackerDbEngine.changeLocationReminderToPressed(rowId);
 			}
 
 			if (isReminder) {
 
 				// Cancel notification
 				int notificationId = extras.getInt("notificationId");
 
 				mNotificationManager.cancel(notificationId);
 				mNotificationManager.cancel(12021982);
 
 				FragmentDialogSpentNotification fragmentDialogSpentNotification = new FragmentDialogSpentNotification();
 				Bundle args = new Bundle();
 				fragmentDialogSpentNotification.setArguments(extras);
 				fragmentDialogSpentNotification.show(
 						getSupportFragmentManager(),
 						"fragmentDialogSpentNotification");
 
 			}
 
 		}
 
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		//
 		super.onActivityResult(requestCode, resultCode, data);
 		switch (requestCode) {
 		case ClassCommonUtilities.REQUEST_CODE_ACTIVITY_PREFERENCES:
 			updatePreferences();
 			initializeActionBar();
 
 			break;
 		}
 	}
 
 	private void cancelTimeAlarmManager() {
 		//
 		AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
 		am.cancel(mTimeAlarmSender);
 	}
 
 	private void startTimeAlarmManager() {
 		//
 		Calendar firstTime = Calendar.getInstance();
 		firstTime.setTimeInMillis(System.currentTimeMillis());
 
 		int secondsToAdd = 60 - firstTime.get(Calendar.SECOND);
 
 		DebugActivityMain("Starting Time service using alarm manager in "
 				+ secondsToAdd + " Seconds");
 
 		firstTime.setTimeInMillis(SystemClock.elapsedRealtime());
 		firstTime.add(Calendar.SECOND, secondsToAdd);
 
 		// Schedule the alarm!
 		AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
 		am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
 				firstTime.getTimeInMillis(), 60000, mTimeAlarmSender);
 
 	}
 
 	public void onUpdateSpentEntryClicked(Bundle values) {
 		//
 		mSpendingTrackerDbEngine.updateSpentByRowId(values.getString("id"),
 				values.getString("amount"), values.getString("date") + "T"
 						+ values.getString("time"),
 				values.getString("category"));
 		Toast.makeText(ActivityMain1.this,
 				"Entry " + values.getString("id") + " updated!",
 				Toast.LENGTH_LONG).show();
 		mFragmentEntries.updateListViewAdapter();
 
 	}
 
 	public void onDeleteReminderTimeClicked(String rowId) {
 		//
 		mSpendingTrackerDbEngine.deleteReminderTimeById(rowId);
 		mFragmentDialogRemindersTimeManage.initRemindersTimeListView();
 
 	}
 
 	public void onManagerReminderTimeClicked() {
 		//
 		FragmentDialogRemindersTimeManage fragmentDialogRemindersTimeManage = new FragmentDialogRemindersTimeManage();
 
 		fragmentDialogRemindersTimeManage.show(getSupportFragmentManager(),
 				"FragmentDialogRemindersTimeManage");
 
 	}
 
 	public void onSpentClicked(Bundle values) {
 		//
 
 		onButtonAddEntrySpentClicked(values);
 		if (values.getBoolean("autoClose")) {
 			this.finish();
 		}
 
 	}
 
 	public void onSpentNoClicked(boolean autoClose) {
 		//
 		if (autoClose) {
 			this.finish();
 		}
 
 	}
 
 	public void onDatabaseExportClicked() {
 		//
 		mProgressDialog = new ProgressDialog(ActivityMain1.this);
 		mProgressDialog.setCancelable(false);
 		mProgressDialog.setTitle("Database export to file " + XMLFILE);
 		mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
 		mProgressDialog.setMessage("Exporting database ...");
 		mProgressDialog.show();
 
 		mSpendingTrackerDbEngine.exportToXMLFile(XMLFILE);
 	}
 
 	public void onDatabaseImportClicked() {
 		//
 		mProgressDialog = new ProgressDialog(ActivityMain1.this);
 		mProgressDialog.setCancelable(false);
 		mProgressDialog.setTitle("Database import from file" + XMLFILE);
 		mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
 		mProgressDialog.setMessage("Importing database ...");
 		mProgressDialog.show();
 
 		mSpendingTrackerDbEngine.importFromXMLFile(XMLFILE);
 
 	}
 
 	public void onImportFinished(String result) {
 		//
 		mProgressDialog.dismiss();
 		Toast.makeText(ActivityMain1.this, result, Toast.LENGTH_LONG).show();
 	}
 
 	public void onExportFinished(String result) {
 		//
 		mProgressDialog.dismiss();
 		Toast.makeText(ActivityMain1.this, result, Toast.LENGTH_LONG).show();
 	}
 
 	public void onDatabaseImportExportUpdateMessage(String result) {
 		//
 		mProgressDialog.setMessage(result);
 
 	}
 
 }
