 package com.bustiblelemons.tasque.main;
 
 import static com.bustiblelemons.tasque.utilities.Values.TAG;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 
 import android.app.ActivityManager;
 import android.app.ActivityManager.RunningServiceInfo;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.res.Configuration;
import android.content.res.Resources;
 import android.database.sqlite.SQLiteException;
 import android.net.ConnectivityManager;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentStatePagerAdapter;
 import android.support.v4.app.FragmentTransaction;
 import android.support.v4.view.ViewPager;
 import android.support.v4.view.ViewPager.OnPageChangeListener;
 import android.text.InputType;
import android.util.DisplayMetrics;
 import android.util.Log;
 import android.util.Pair;
 import android.util.SparseArray;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.WindowManager;
 import android.widget.EditText;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 import android.widget.TextView.OnEditorActionListener;
 
 import com.actionbarsherlock.app.ActionBar;
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.bustiblelemons.tasque.R;
 import com.bustiblelemons.tasque.database.Database;
 import com.bustiblelemons.tasque.frontend.Alarms;
 import com.bustiblelemons.tasque.main.CategoriesFragment.CategoriesFragmentListener;
 import com.bustiblelemons.tasque.main.CompletedTasksFragment.CompletedTasksListener;
 import com.bustiblelemons.tasque.main.NotesFragment.NotesFragmentListener;
 import com.bustiblelemons.tasque.main.TasqueGroupFragment.TasqueGroupFragmentListener;
 import com.bustiblelemons.tasque.rtm.RTMBackend;
 import com.bustiblelemons.tasque.rtm.RTMConnectivityReceiver;
 import com.bustiblelemons.tasque.rtm.RTMSyncService;
 import com.bustiblelemons.tasque.rtm.RTMSyncService.OnRTMRefresh;
 import com.bustiblelemons.tasque.rtm.SynchronizingFragment;
 import com.bustiblelemons.tasque.settings.SettingsUtil;
 import com.bustiblelemons.tasque.utilities.Connection;
 import com.bustiblelemons.tasque.utilities.Utility;
 import com.bustiblelemons.tasque.utilities.Values.FragmentArguments;
 
 public class Tasque extends SherlockFragmentActivity implements OnPageChangeListener, OnEditorActionListener,
 		NotesFragmentListener, CategoriesFragmentListener, TasqueGroupFragmentListener, OnRTMRefresh,
 		RightSideFragmentPocketListener, CompletedTasksListener {
 
 	private NotesFragment notesFragment;
 	private CategoriesFragment categoriesFragment;
 	private CompletedTasksFragment completedTasksFragment;
 	private FragmentManager fmanager;
 	private SynchronizingFragment synchronizingFragment;
 
 	protected static ArrayList<Pair<Integer, String>> categories;
 	private Context context;
 	private MyPagerAdapter pagerAdapter;
 	private ViewPager pager;
 	private ActionBar abar;
 
 	private RTMConnectivityReceiver connectivityReceiver;
 	private IntentFilter connectivityFilter;
 	private SynchronizationReceiver synchronizationReceiver;
 	private IntentFilter synchronizationFilter;
 	private static EditText customInputField;
 	private static RelativeLayout customAbarInputView;
 	private Intent service;
 
 	private class SynchronizationReceiver extends BroadcastReceiver {
 
 		@SuppressWarnings("unchecked")
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			int syncStatus = intent.getIntExtra(RTMSyncService.SYNC_STATUS, -1);
 			switch (syncStatus) {
 			case RTMSyncService.SYNC_BEGIN:
 				Log.d(TAG, "\t\t\t\tSynchronization began");
 				onShowSynchronizationFragment();
 				break;
 			case RTMSyncService.SYNC_CATEGORIES_CHANGED:
 				Log.d(TAG, "\t\t\t\tRefreshing the adapter");
 				refreshPagerAdapter();
 				break;
 			case RTMSyncService.SYNC_CATEGORIES_UPLOADED:
 				break;
 			case RTMSyncService.SYNC_DONE:
 				HashSet<String> syncedListsIDs = (HashSet<String>) intent
 						.getSerializableExtra(RTMSyncService.SYNCHRONIZED_LISTS);
 				for (String id : syncedListsIDs) {
 					refreshCategory(id);
 				}
 				refreshCompletedFragment();
 				if (notesFragment != null) {
 					if (notesFragment.isVisible()) {
 						notesFragment.refreshData();
 					}
 				}
 				Log.d(TAG, "\t\t\t\tSynchronization done");
 				break;
 			case RTMSyncService.SYNC_DONE_EMPTY:
 				Log.d(TAG, "\t\t\t\tEMPTY");
 				break;
 			case RTMSyncService.SYNC_POSTPONED:
 				Log.d(TAG, "\t\t\t\tSynchronization postponed");
 				break;
 			case RTMSyncService.SERVICE_STOPPED:
 				Log.d(TAG, "Service stoppped");
 				onDetachSynchronizationFragment();
 				break;
 			}
 		}
 	}
 
 	@Override
 	protected void onCreate(Bundle arg0) {
 		super.onCreate(arg0);
 		Log.d(TAG, "Tasque.class onCreate");
 		context = getApplicationContext();
 		fmanager = getSupportFragmentManager();
 		setContentView(R.layout.activity_tasque);
 		pager = (ViewPager) findViewById(R.id.tabs_view_pager);
 		pager.setOnPageChangeListener(this);
 		abar = getSupportActionBar();
 		try {
 			categories = Database.getCategoriesList(context);
 		} catch (SQLiteException e) {
 			this.finish();
 		}
 		this.setActionBarForInput();
 		completedTasksFragment = new CompletedTasksFragment();
 		if (RTMBackend.useRTM(context)) {
 			connectivityReceiver = new RTMConnectivityReceiver();
 			connectivityFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
 			registerReceiver(connectivityReceiver, connectivityFilter);
 			synchronizationReceiver = new SynchronizationReceiver();
 			synchronizationFilter = new IntentFilter(RTMSyncService.INTENT_FILTER);
 			registerReceiver(synchronizationReceiver, synchronizationFilter);
 			if (Connection.isUp(context)) {
 				startRTMRefreshService(context, false);
 				String defaultID = RTMBackend.getDefaultListId(context);
 				if (defaultID.length() > 0) {
 					SettingsUtil.setDefaultCategoryId(context, Integer.valueOf(defaultID));
 				}
 			}
 		}
 	}
 
 	private void load() {
 		if (SettingsUtil.isFirstRun(context)) {
 			Log.d(TAG, "First Run. Setting categories and preventing further first runs");
 			SettingsUtil.setDefaultValues(context, true);
 			SettingsUtil.setSelectedCategoriesToAll(context);
 			SettingsUtil.firstRunDone(context);
 		}
 		abar.show();
 		categories = Database.getCategoriesList(context);
 		pagerAdapter = new MyPagerAdapter(getSupportFragmentManager());
 		pager.setAdapter(pagerAdapter);
 	}
 
 	@Override
 	protected void onStart() {
 		super.onStart();
 		Log.d(TAG, "Tasque:onStart()");
 		if (SettingsUtil.hideKeyboard(context)) {
 			getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
 		}
 		if (SettingsUtil.useLightActionBarInput(context)) {
 			customInputField.setTextColor(context.getResources().getColor(R.color.abs__background_holo_light));
 		}
 		if (SettingsUtil.autoCap(context)) {
 			customInputField.setRawInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);
 		}
 		this.load();
 		Utility.applyFontSize(customInputField);
 		int defCat = SettingsUtil.getDefaultCategoryId(context);
 		if (defCat > 0) {
 			this.goToDefaultList(defCat);
 		}
 		onRemoveRightSideFragment();
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		try {
 			registerReceiver(synchronizationReceiver, synchronizationFilter);
 			registerReceiver(connectivityReceiver, connectivityFilter);
 		} catch (NullPointerException e) {
 			Log.d(TAG, "One of the receivers were null.");
 		}
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 		try {
 			if (connectivityReceiver != null) {
 				unregisterReceiver(connectivityReceiver);
 				unregisterReceiver(synchronizationReceiver);
 			}
 		} catch (IllegalArgumentException e) {
 			Log.d(TAG, "Receiver already unregistered");
 		} catch (NullPointerException e) {
 			Log.d(TAG, "One of the receivers were null");
 		}
 	}
 
 	@Override
 	protected void onDestroy() {
 		super.onDestroy();
 		if (SettingsUtil.getExportOnExit(context)) {
 			Utility.pushDatabase(context);
 		}
 		if (pager != null)
 			Utility.hideKeyboard(context, pager.getWindowToken());
 
 		if (RTMBackend.useRTM(context)) {
 			if (SettingsUtil.useRTMUpdateService(context)) {
 				Alarms.setUp(context);
 			} else {
 				Log.d(TAG, "Not setting up the alarms");
 			}
 		}
 	}
 
 	@Override
 	public void onConfigurationChanged(Configuration newConfig) {
 		super.onConfigurationChanged(newConfig);
 		Log.d(TAG, "OnConfigurationChanged");
 		if (completedTasksFragment != null) {
 			if (completedTasksFragment.isVisible()) {
 				completedTasksFragment.setUpHasMenu();
 			}
 		} else if (categoriesFragment != null) {
 			if (categoriesFragment.isVisible()) {
 				categoriesFragment.setActionBar();
 			}
 		} else if (notesFragment != null) {
 			if (notesFragment.isVisible()) {
 				notesFragment.setActionBar();
 			}
 		} else {
 			setActionBarForInput();
 		}
 	}
 
 	@Override
 	public void onShowNotesFragment(String listId, String taskID, String taskName) {
 		if (notesFragment != null) {
 			if (notesFragment.isVisible()) {
 				notesFragment.saveData();
 			}
 		}
 		onAttachRightSideFragment(false);
 		notesFragment = new NotesFragment();
 		FragmentTransaction transaction = fmanager.beginTransaction();
 		Bundle args = new Bundle();
 		args.putString(FragmentArguments.ID, taskID);
 		args.putString(FragmentArguments.LIST_ID, listId);
 		args.putString(FragmentArguments.TASK_NAME, taskName);
 		notesFragment.setArguments(args);
 		transaction.replace(R.id.fragment_pocket, notesFragment, NotesFragment.FRAGMENT_TAG);
 		transaction.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right, R.anim.slide_in_left,
 				R.anim.slide_out_right);
 		transaction.addToBackStack(null);
 		transaction.commit();
 	}
 
 	@Override
 	public void onShowCategoriesFragment() {
 		onAttachRightSideFragment(true);
 		if (categoriesFragment == null) {
 			categoriesFragment = new CategoriesFragment();
 		}
 		if (categoriesFragment.isInLayout()) {
 			fmanager.beginTransaction().show(categoriesFragment).commit();
 			return;
 		}
 		FragmentTransaction transaction = fmanager.beginTransaction();
 		transaction.replace(R.id.fragment_pocket, categoriesFragment, CategoriesFragment.FRAGMENT_TAG);
 		transaction.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right, R.anim.slide_in_left,
 				R.anim.slide_out_right);
 		transaction.addToBackStack(null);
 		transaction.commit();
 	}
 
 	@Override
 	public void onShowCompletedTasksFragment(String categoryId) {
 		onAttachRightSideFragment(false);
 		FragmentTransaction transaction = fmanager.beginTransaction();
 		if (completedTasksFragment.isVisible()) {
 			onRemoveRightSideFragment();
 			return;
 		}
 		completedTasksFragment = new CompletedTasksFragment();
 		Bundle args = new Bundle();
 		args.putString(FragmentArguments.ID, categoryId);
 		completedTasksFragment.setArguments(args);
 
 		transaction.replace(R.id.fragment_pocket, completedTasksFragment, CompletedTasksFragment.FRAGMENT_TAG);
 		transaction.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right, R.anim.slide_in_left,
 				R.anim.slide_out_right);
 		transaction.addToBackStack(null);
 		transaction.commit();
 	}
 
 	public void onShowSynchronizationFragment() {
 		synchronizingFragment = new SynchronizingFragment();
 		FragmentTransaction tr = fmanager.beginTransaction();
 		tr.add(R.id.synchronization_fragment_pocket, synchronizingFragment, SynchronizingFragment.FRAGMENT_TAG);
 		tr.setCustomAnimations(R.anim.slide_in_up, R.anim.slide_out_down, R.anim.slide_in_up, R.anim.slide_out_down);
 		tr.commit();
 	}
 
 	public void onDetachSynchronizationFragment() {
 		if (synchronizingFragment != null) {
 			FragmentTransaction transaction = fmanager.beginTransaction();
 			transaction.remove(synchronizingFragment);
 			transaction.commit();
 		}
 	}
 
 	@Override
 	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
 		if (event != null) {
 			Log.d(TAG, "onEditorAction");
 			if (categoriesFragment != null) {
 				if (categoriesFragment.isVisible()) {
 					return categoriesFragment.onEditorAction(v, actionId, event);
 				}
 			}
 			if (completedTasksFragment.isVisible()) {
				Resources res = context.getResources();
				if (res.getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
					if (res.getDisplayMetrics().densityDpi == DisplayMetrics.DENSITY_HIGH) {
						return pagerAdapter.getFragment(pager.getCurrentItem()).onEditorAction(v, actionId, event);
					}
				} else {
					return completedTasksFragment.onEditorAction(v, actionId, event);
				}
 			}
 			return pagerAdapter.getFragment(pager.getCurrentItem()).onEditorAction(v, actionId, event);
 		}
 		return false;
 	}
 
 	@Override
 	public void setDefaultCategory(int categoryID) {
 		SettingsUtil.setDefaultCategoryId(context, categoryID);
 		if (RTMBackend.useRTM(context)) {
 			RTMBackend.setDefaultListId(context, String.valueOf(categoryID));
 		}
 	}
 
 	private void goToDefaultList(int categoryId) {
 		for (Pair<Integer, String> p : categories) {
 			if (categoryId == p.first) {
 				pager.setCurrentItem(categories.indexOf(p));
 				customInputField.setHint(p.second);
 				return;
 			}
 		}
 	}
 
 	@Override
 	public void setActionBarForInput() {
 		customAbarInputView = (RelativeLayout) LayoutInflater.from(context).inflate(R.layout.actionbar_input, null);
 		customInputField = (EditText) customAbarInputView.findViewById(R.id.actionbar_input);
 		customInputField.setOnEditorActionListener(this);
 		customInputField.setHint(categories.get(pager.getCurrentItem()).second);
 		Utility.applyFontSize(customInputField);
 		abar.setDisplayShowCustomEnabled(true);
 		abar.setDisplayShowTitleEnabled(false);
 		abar.setTitle(R.string.app_name);
 		abar.setCustomView(customAbarInputView);
 	}
 
 	public static ViewGroup getActionBarView() {
 		return Tasque.customAbarInputView;
 	}
 
 	static TextView getActionBarInput() {
 		return Tasque.customInputField;
 	}
 
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 		if (event != null) {
 			if (keyCode == KeyEvent.KEYCODE_BACK) {
 				if (notesFragment != null) {
 					if (notesFragment.isVisible()) {
 						return notesFragment.onKeyDown(keyCode, event) ? true : super.onKeyDown(keyCode, event);
 					}
 				}
 				if (completedTasksFragment.isVisible()) {
 					return completedTasksFragment.onKeyDown(keyCode, event) ? true : super.onKeyDown(keyCode, event);
 				}
 				if (categoriesFragment != null) {
 					if (categoriesFragment.isVisible()) {
 						return categoriesFragment.onKeyCode(keyCode, event) ? true : super.onKeyDown(keyCode, event);
 					}
 				}
 				TasqueGroupFragment f = (TasqueGroupFragment) pagerAdapter.getFragment(pager.getCurrentItem());
 				if (f.isVisible()) {
 					return f.onKeyDown(keyCode, event) ? true : super.onKeyDown(keyCode, event);
 				}
 				return super.onKeyDown(keyCode, event);
 			}
 			return super.onKeyDown(keyCode, event);
 		} else {
 			return super.onKeyDown(keyCode, event);
 		}
 	}
 
 	public static class MyPagerAdapter extends FragmentStatePagerAdapter {
 		private SparseArray<TasqueGroupFragment> fragments;
 
 		public MyPagerAdapter(FragmentManager fm) {
 			super(fm);
 			fragments = new SparseArray<TasqueGroupFragment>();
 		}
 
 		@Override
 		public int getCount() {
 			return Tasque.categories.size();
 		}
 
 		protected TasqueGroupFragment getFragment(int key) {
 			return fragments.get(key);
 		}
 
 		@Override
 		public Fragment getItem(int position) {
 			Log.d(TAG, "getItem(" + position + ")");
 			fragments.put(position, TasqueGroupFragment.newInstance(Tasque.categories.get(position)));
 			return getFragment(position);
 
 		}
 
 		@Override
 		public void destroyItem(ViewGroup container, int position, Object object) {
 			super.destroyItem(container, position, object);
 			fragments.remove(position);
 		}
 	}
 
 	@Override
 	public void onPageScrolled(int arg0, float arg1, int arg2) {
 	}
 
 	@Override
 	public void onPageScrollStateChanged(int arg0) {
 	}
 
 	@Override
 	public void onPageSelected(int arg0) {
 		Tasque.getActionBarInput().setHint(categories.get(arg0).second);
 		if (completedTasksFragment.isVisible()) {
 			completedTasksFragment.loadData(categories.get(arg0).first);
 		}
 	}
 
 	@Override
 	public void onNotesFragmentHidden(boolean taskNameChanged) {
 		this.setActionBarForInput();
 		if (taskNameChanged) {
 			((TasqueGroupFragment) pagerAdapter.getFragment(pager.getCurrentItem())).refreshData();
 		}
 	}
 
 	private void refreshPagerAdapter() {
 		categories = Database.getCategoriesList(context);
 		for (int i = 0; i < pagerAdapter.getCount(); i++) {
 			this.refreshCategory(i);
 		}
 		pager.setCurrentItem(0);
 		this.setActionBarForInput();
 		this.goToDefaultList(SettingsUtil.getDefaultCategoryId(context));
 		if (categoriesFragment != null) {
 			if (categoriesFragment.isVisible()) {
 				categoriesFragment.refreshCategories();
 			}
 		}
 	}
 
 	@Override
 	public void onShowInAllCategoriesChanged() {
 		TasqueGroupFragment f = pagerAdapter.getFragment(0);
 		if (f != null) {
 			if (f.isVisible()) {
 				f.refreshData();
 			}
 		}
 	}
 
 	@Override
 	public void onTaskMarkedActive() {
 		TasqueGroupFragment f = pagerAdapter.getFragment(pager.getCurrentItem());
 		if (f != null) {
 			if (f.isVisible()) {
 				f.refreshData();
 			}
 		}
 	}
 
 	@Override
 	public void onRefreshAllCategories() {
 		this.refreshPagerAdapter();
 	}
 
 	@Override
 	public boolean onRefreshCategory() {
 		return this.refreshCategory(pager.getCurrentItem());
 	}
 
 	@Override
 	public boolean onRefreshCategory(int positionInAdapter) {
 		return this.refreshCategory(positionInAdapter);
 	}
 
 	private boolean refreshCategory(String categoryId) {
 		for (Pair<Integer, String> p : categories) {
 			if (p.first.equals(Integer.valueOf(categoryId))) {
 				return refreshCategory(categories.indexOf(p));
 			}
 		}
 		return false;
 	}
 
 	@Override
 	public boolean onRefreshCategory(String categoryId) {
 		return this.refreshCategory(categoryId);
 	}
 
 	private boolean refreshCategory(int position) {
 		try {
 			Log.d(TAG, "Refreshing category at " + position + "\n" + categories.get(position).second);
 			TasqueGroupFragment f = (TasqueGroupFragment) pagerAdapter.instantiateItem(pager, position);
 			f.refreshData();
 			this.setActionBarForInput();
 			refreshCompletedFragment();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return true;
 	}
 
 	private boolean isRTMUpdateSericeRunning(Context context) {
 		ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
 		for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
 			if (RTMSyncService.class.getName().equals(service.service.getClassName())) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	@Override
 	public void startRTMRefreshService(Context context, boolean force) {
 		if (isRTMUpdateSericeRunning(context)) {
 			Log.d(TAG, "Service is alread running not starting a new one");
 		} else {
 			service = new Intent(this, RTMSyncService.class);
 			service.putExtra(RTMSyncService.FORCE_SYNC, force);
 			context.startService(service);
 		}
 	}
 
 	private void refreshCompletedFragment() {
 		if (completedTasksFragment != null) {
 			if (completedTasksFragment.isVisible()) {
 				completedTasksFragment.refreshData();
 			}
 		}
 	}
 
 	@Override
 	public void onRemoveRightSideFragment() {
 		if (completedTasksFragment != null) {
 			if (completedTasksFragment.isVisible()) {
 				completedTasksFragment.stopDeleting();
 			}
 		}
 		fmanager.popBackStack();
 		findViewById(R.id.fragment_pocket).setVisibility(View.GONE);
 		if (pager.getVisibility() == View.GONE) {
 			pager.setVisibility(View.VISIBLE);
 		}
 		setActionBarForInput();
 		Tasque.getActionBarInput().setHint(categories.get(pager.getCurrentItem()).second);
 	}
 
 	@Override
 	public void onAttachRightSideFragment(boolean rightSideFullScreen) {
 		findViewById(R.id.fragment_pocket).setVisibility(View.VISIBLE);
 		if (rightSideFullScreen) {
 			pager.setVisibility(View.GONE);
 		}
 	}
 
 	@Override
 	public void onStartDeletingCompletedTasks() {
 		if (completedTasksFragment != null) {
 			if (completedTasksFragment.isVisible()) {
 				completedTasksFragment.startDeleting();
 			}
 		}
 	}
 
 	@Override
 	public void onStopDeletingCompletedTasks() {
 		if (completedTasksFragment != null) {
 			if (completedTasksFragment.isVisible()) {
 				completedTasksFragment.stopDeleting();
 			}
 		}
 	}
 
 	@Override
 	public void onDeleteItems() {
 		if (completedTasksFragment != null) {
 			if (completedTasksFragment.isVisible()) {
 				completedTasksFragment.deleteSelected();
 			}
 		}
 	}
 
 	@Override
 	public boolean isTasqueListVisible() {
 		return pagerAdapter.getFragment(pager.getCurrentItem()).isVisible();
 	}
 }
