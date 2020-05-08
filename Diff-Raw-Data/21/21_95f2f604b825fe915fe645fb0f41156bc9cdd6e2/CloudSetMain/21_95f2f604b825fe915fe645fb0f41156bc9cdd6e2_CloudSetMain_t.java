 /*
  * CloudSet - Android devices settings synchronization
  * Copyright (C) 2013 Bryan Emmanuel
  *
  * This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  *
  *  Bryan Emmanuel piusvelte@gmail.com
  */
 package com.piusvelte.cloudset.android;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Locale;
 
 import com.google.android.gms.common.GooglePlayServicesUtil;
 import com.piusvelte.cloudset.gwt.server.deviceendpoint.model.SimpleDevice;
 
 import android.app.ActionBar;
 import android.app.AlertDialog;
 import android.app.FragmentTransaction;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.DialogInterface.OnClickListener;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentPagerAdapter;
 import android.support.v4.app.LoaderManager;
 import android.support.v4.content.Loader;
 import android.support.v4.view.ViewPager;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.widget.Toast;
 
 public class CloudSetMain extends FragmentActivity implements
 		ActionBar.TabListener, AccountsFragment.AccountsListener,
 		DevicesListener, LoaderManager.LoaderCallbacks<List<SimpleDevice>> {
 
 	private static final String TAG = "CloudSetMain";
 
 	SectionsPagerAdapter sectionsPagerAdapter;
 
 	ViewPager viewPager;
 
 	public static final String ACTION_GCM_REGISTERED = "com.piusvelte.cloudset.android.action.GCM_REGISTERED";
 	public static final String ACTION_GCM_UNREGISTERED = "com.piusvelte.cloudset.android.action.GCM_UNREGISTERED";
 	public static final String ACTION_GCM_ERROR = "com.piusvelte.cloudset.android.action.GCM_ERROR";
 
 	public static final String EXTRA_DEVICE_REGISTRATION = "com.piusvelte.cloudset.android.extra.DEVICE_REGISTRATION";
 
 	public static final String ARGUMENT_ISSUBSCRIPTIONS = "issubscriptions";
 
 	private static final int FRAGMENT_ACCOUNT = 0;
 	private static final int FRAGMENT_SUBSCRIPTIONS = 1;
 	private static final int FRAGMENT_SUBSCRIBERS = 2;
 
 	private String account;
 	private String registrationId;
 	private List<SimpleDevice> devices = new ArrayList<SimpleDevice>();
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		// not called on screen rotate
 		GooglePlayServicesUtil
 				.getOpenSourceSoftwareLicenseInfo(getApplicationContext());
 		setContentView(R.layout.activity_main);
 		final ActionBar actionBar = getActionBar();
 		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
 		sectionsPagerAdapter = new SectionsPagerAdapter(
 				getSupportFragmentManager());
 		viewPager = (ViewPager) findViewById(R.id.pager);
 		viewPager.setAdapter(sectionsPagerAdapter);
 		viewPager
 				.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
 					@Override
 					public void onPageSelected(int position) {
 						actionBar.setSelectedNavigationItem(position);
 					}
 				});
 
 		for (int i = 0; i < sectionsPagerAdapter.getCount(); i++) {
 			actionBar.addTab(actionBar.newTab()
 					.setText(sectionsPagerAdapter.getPageTitle(i))
 					.setTabListener(this));
 		}
 
 		SharedPreferences sp = getSharedPreferences(
 				getString(R.string.app_name), MODE_PRIVATE);
 		account = sp.getString(getString(R.string.preference_account_name),
 				null);
 		registrationId = sp.getString(
 				getString(R.string.preference_gcm_registration), null);
 		setCurrentTab();
 		LoaderManager loaderManager = getSupportLoaderManager();
 		loaderManager.initLoader(0, null, this);
 
		if (savedInstanceState != null
				&& savedInstanceState.containsKey(EXTRA_LOADERS_COUNT)) {
			loadersCount = savedInstanceState.getInt(EXTRA_LOADERS_COUNT);
 		}
 
 		for (int i = 1; i < loadersCount; i++) {
 			loaderManager.initLoader(i, null, this);
 		}
 	}
 
 	private int loadersCount = 1;
 	private static final String EXTRA_LOADERS_COUNT = "loaders_count";
 	private static final String EXTRA_DEREGISTER_ID = "deregister_id";
 
 	@Override
 	public void onSaveInstanceState(Bundle state) {
 		super.onSaveInstanceState(state);
 		state.putInt(EXTRA_LOADERS_COUNT, loadersCount);
 	}
 
 	@Override
 	protected void onNewIntent(Intent intent) {
 		super.onNewIntent(intent);
 		handleGCMIntent(intent);
 	}
 
 	private void setCurrentTab() {
 		if (hasRegistration()) {
 			viewPager.setCurrentItem(FRAGMENT_SUBSCRIPTIONS);
 		} else {
 			viewPager.setCurrentItem(FRAGMENT_ACCOUNT);
 		}
 	}
 
 	private void handleGCMIntent(Intent intent) {
 		if (intent != null) {
 			String action = intent.getAction();
 
 			if (action != null) {
 				if (action.equals(ACTION_GCM_ERROR)) {
 					Toast.makeText(getApplicationContext(),
 							"Error occurred during device registration",
 							Toast.LENGTH_SHORT).show();
 					account = null;
 					registrationId = null;
 					getSharedPreferences(getString(R.string.app_name),
 							MODE_PRIVATE)
 							.edit()
 							.putString(
 									getString(R.string.preference_account_name),
 									account)
 							.putString(
 									getString(R.string.preference_gcm_registration),
 									registrationId).commit();
 					setCurrentTab();
 				} else if (action.equals(ACTION_GCM_REGISTERED)
 						&& intent.hasExtra(EXTRA_DEVICE_REGISTRATION)) {
 					registrationId = intent
 							.getStringExtra(EXTRA_DEVICE_REGISTRATION);
 					getSharedPreferences(getString(R.string.app_name),
 							MODE_PRIVATE)
 							.edit()
 							.putString(
 									getString(R.string.preference_gcm_registration),
 									registrationId).commit();
 					setCurrentTab();
					loadDevices();
 				} else if (action.equals(ACTION_GCM_UNREGISTERED)
 						&& intent.hasExtra(EXTRA_DEVICE_REGISTRATION)) {
 					account = null;
 					registrationId = null;
 					getSharedPreferences(getString(R.string.app_name),
 							MODE_PRIVATE)
 							.edit()
 							.putString(
 									getString(R.string.preference_account_name),
 									account)
 							.putString(
 									getString(R.string.preference_gcm_registration),
 									registrationId).commit();
 					setCurrentTab();
 				}
 			}
 		}
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 
 	private String getFragmentTag(int position) {
 		return "android:switcher:" + R.id.pager + ":" + position;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		int itemId = item.getItemId();
 
 		if (itemId == R.id.action_refresh) {
 			loadDevices();
 			return true;
 		} else if (item.getItemId() == R.id.action_about) {
 			(new AlertDialog.Builder(this))
 					.setTitle(R.string.about_title)
 					.setMessage(R.string.about_message)
 					.setPositiveButton(android.R.string.ok,
 							new OnClickListener() {
 
 								@Override
 								public void onClick(DialogInterface arg0,
 										int arg1) {
 									arg0.cancel();
 								}
 
 							}).setCancelable(true).show();
 			return true;
 		}
 
 		return super.onOptionsItemSelected(item);
 	}
 
 	private void updateDevicesFragments() {
 		if (viewPager.getCurrentItem() == FRAGMENT_SUBSCRIPTIONS) {
 			DevicesListListener listener = (DevicesListListener) getSupportFragmentManager()
 					.findFragmentByTag(getFragmentTag(FRAGMENT_SUBSCRIPTIONS));
 
 			if (listener != null) {
 				listener.onDevicesLoaded(devices);
 			}
 		} else if (viewPager.getCurrentItem() == FRAGMENT_SUBSCRIBERS) {
 			DevicesListListener listener = (DevicesListListener) getSupportFragmentManager()
 					.findFragmentByTag(getFragmentTag(FRAGMENT_SUBSCRIBERS));
 
 			if (listener != null) {
 				listener.onDevicesLoaded(devices);
 			}
 		}
 	}
 
 	@Override
 	public void onTabSelected(ActionBar.Tab tab,
 			FragmentTransaction fragmentTransaction) {
 		viewPager.setCurrentItem(tab.getPosition());
 		updateDevicesFragments();
 	}
 
 	@Override
 	public void onTabUnselected(ActionBar.Tab tab,
 			FragmentTransaction fragmentTransaction) {
 	}
 
 	@Override
 	public void onTabReselected(ActionBar.Tab tab,
 			FragmentTransaction fragmentTransaction) {
 	}
 
 	public void loadDevices() {
 		if (hasRegistration()) {
			Loader<List<SimpleDevice>> loader = getSupportLoaderManager().initLoader(0, null, this);

			if (loader != null) {
				loader.forceLoad();
			}
 		} else {
 			// set to new empty List for the adapter
 			devices = new ArrayList<SimpleDevice>();
 			updateDevicesFragments();
 		}
 	}
 
 	public class SectionsPagerAdapter extends FragmentPagerAdapter {
 
 		public SectionsPagerAdapter(FragmentManager fm) {
 			super(fm);
 		}
 
 		@Override
 		public Fragment getItem(int position) {
 			if (position == FRAGMENT_ACCOUNT) {
 				return new AccountsFragment();
 			} else if (position == FRAGMENT_SUBSCRIPTIONS) {
 				Fragment fragment = new DevicesFragment();
 				Bundle b = new Bundle();
 				b.putBoolean(ARGUMENT_ISSUBSCRIPTIONS, true);
 				fragment.setArguments(b);
 				return fragment;
 			} else if (position == FRAGMENT_SUBSCRIBERS) {
 				Fragment fragment = new DevicesFragment();
 				Bundle b = new Bundle();
 				b.putBoolean(ARGUMENT_ISSUBSCRIPTIONS, false);
 				fragment.setArguments(b);
 				return fragment;
 			}
 
 			return null;
 		}
 
 		@Override
 		public int getCount() {
 			return 3;
 		}
 
 		@Override
 		public CharSequence getPageTitle(int position) {
 			Locale l = Locale.getDefault();
 
 			switch (position) {
 			case FRAGMENT_ACCOUNT:
 				return getString(R.string.title_accounts).toUpperCase(l);
 			case FRAGMENT_SUBSCRIPTIONS:
 				return getString(R.string.title_subscriptions).toUpperCase(l);
 			case FRAGMENT_SUBSCRIBERS:
 				return getString(R.string.title_subscribers).toUpperCase(l);
 			}
 
 			return null;
 		}
 	}
 
 	@Override
 	public String getAccount() {
 		return account;
 	}
 
 	@Override
 	public void setAccount(String account) {
 		this.account = account;
 		// store the account
 		getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE)
 				.edit()
 				.putString(getString(R.string.preference_account_name), account)
 				.commit();
 		// register with GCM, this is an asynchronous operation
 		GCMIntentService.register(getApplicationContext());
 		// move to show "loading devices"
 		viewPager.setCurrentItem(FRAGMENT_SUBSCRIPTIONS);
 	}
 
 	@Override
 	public boolean hasRegistration() {
 		return account != null && registrationId != null;
 	}
 
 	@Override
 	public String getRegistration() {
 		return registrationId;
 	}
 
 	@Override
 	public String getDeviceId(int which) {
 		if (devices != null && which < devices.size()) {
 			return devices.get(which).getId();
 		}
 
 		return null;
 	}
 
 	@Override
 	public Loader<List<SimpleDevice>> onCreateLoader(int arg0, Bundle arg1) {
 		if (arg0 > 0) {
 			if (arg1 != null && arg1.containsKey(EXTRA_DEREGISTER_ID)) {
 				return new DevicesLoader(this, account, registrationId,
 						devices, arg1.getString(EXTRA_DEREGISTER_ID));
 			} else {
 				return null;
 			}
 		} else if (registrationId != null) {
 			return new DevicesLoader(this, account, registrationId);
 		} else {
 			return null;
 		}
 	}
 
 	@Override
 	public void onLoadFinished(Loader<List<SimpleDevice>> arg0,
 			List<SimpleDevice> arg1) {
 		if (loadersCount > 1) {
 			getLoaderManager().destroyLoader(--loadersCount);
 		}

 		devices = arg1;
 		updateDevicesFragments();
 	}
 
 	@Override
 	public void onLoaderReset(Loader<List<SimpleDevice>> arg0) {
		//NO-OP
 	}
 
 	@Override
 	public List<SimpleDevice> getDevices() {
 		return devices;
 	}
 
 	@Override
 	public void deregisterDevice(String id) {
 		if (id != null) {
 			Bundle extras = new Bundle();
 			extras.putString(EXTRA_DEREGISTER_ID, id);
 			getSupportLoaderManager().initLoader(loadersCount++, extras, this);
 		}
 	}
 
 	@Override
 	public void confirmDeregistration(String id) {
 		new ConfirmDialog().setDeviceId(id).show(getSupportFragmentManager(),
 				"confirm:deregister");
 	}
 
 }
