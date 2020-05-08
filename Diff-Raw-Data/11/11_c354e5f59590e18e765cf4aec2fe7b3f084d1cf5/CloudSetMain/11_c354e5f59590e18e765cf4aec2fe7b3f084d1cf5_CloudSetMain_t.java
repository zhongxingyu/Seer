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
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.net.URLDecoder;
 import java.util.ArrayList;
 import java.util.Locale;
 
 import com.google.android.gms.common.GooglePlayServicesUtil;
 import com.google.api.client.extensions.android.http.AndroidHttp;
 import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
 import com.google.api.client.json.jackson.JacksonFactory;
 import com.piusvelte.cloudset.gwt.server.subscriberendpoint.Subscriberendpoint;
 import com.piusvelte.cloudset.gwt.server.subscriberendpoint.model.Subscriber;
 
 import android.app.ActionBar;
 import android.app.AlertDialog;
 import android.app.FragmentTransaction;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.DialogInterface.OnClickListener;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentPagerAdapter;
 import android.support.v4.view.ViewPager;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.widget.ArrayAdapter;
 import android.widget.Toast;
 
 public class CloudSetMain extends FragmentActivity implements
 ActionBar.TabListener, AccountsFragment.AccountsListener, DevicesFragment.DevicesListener {
 
 	private static final String TAG = "CloudSetMain";
 
 	SectionsPagerAdapter sectionsPagerAdapter;
 
 	ViewPager viewPager;
 
 	public static final String ACTION_GCM_REGISTERED = "com.piusvelte.cloudset.android.action.GCM_REGISTERED";
 	public static final String ACTION_GCM_UNREGISTERED = "com.piusvelte.cloudset.android.action.GCM_UNREGISTERED";
 	public static final String ACTION_GCM_ERROR = "com.piusvelte.cloudset.android.action.GCM_ERROR";
 
 	public static final String ARGUMENT_ISSUBSCRIPTIONS = "issubscriptions";
 
 	private static final int FRAGMENT_ACCOUNT = 0;
 	private static final int FRAGMENT_SUBSCRIPTIONS = 1;
 	private static final int FRAGMENT_SUBSCRIBERS = 2;
 
 	private String account;
 	private String registrationId;
 	private GoogleAccountCredential credential;
 	private ArrayList<Subscriber> devices = new ArrayList<Subscriber>();
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		GooglePlayServicesUtil.getOpenSourceSoftwareLicenseInfo(getApplicationContext());
 
 		setContentView(R.layout.activity_main);
 
 		final ActionBar actionBar = getActionBar();
 		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
 
 		sectionsPagerAdapter = new SectionsPagerAdapter(
 				getSupportFragmentManager());
 
 		viewPager = (ViewPager) findViewById(R.id.pager);
 		viewPager.setAdapter(sectionsPagerAdapter);
 
 		viewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
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
 
 	}
 
 	@Override
 	protected void onNewIntent(Intent intent) {
 		super.onNewIntent(intent);
 		setIntent(intent);
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 		Intent intent = getIntent();
 		if (intent != null) {
 			String action = intent.getAction();
 			if (action != null) {
 				if (action.equals(ACTION_GCM_ERROR)) {
 					Toast.makeText(getApplicationContext(), "Error occurred during device registration", Toast.LENGTH_SHORT).show();
 				} else if (action.equals(ACTION_GCM_REGISTERED)) {
 				} else if (action.equals(ACTION_GCM_UNREGISTERED)) {	
 				}
 			}
 		}
 
 		// check if the account is setup
 		SharedPreferences sp = getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE);
 		if ((sp.getString(getString(R.string.preference_account_name), null) != null)
 				&& (sp.getString(getString(R.string.preference_gcm_registration), null) != null)) {
 			account = sp.getString(getString(R.string.preference_account_name), null);
 			registrationId = sp.getString(getString(R.string.preference_gcm_registration), null);
 		}
 
 		loadDevices();
 
 		if (!hasRegistration()) {
 			viewPager.setCurrentItem(FRAGMENT_ACCOUNT);
 		} else {
 			viewPager.setCurrentItem(FRAGMENT_SUBSCRIPTIONS);
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		if (item.getItemId() == R.id.action_about) {
 			(new AlertDialog.Builder(this))
 			.setTitle(R.string.about_title)
 			.setMessage(R.string.about_message)
 			.setPositiveButton(android.R.string.ok, new OnClickListener() {
 
 				@Override
 				public void onClick(DialogInterface arg0, int arg1) {
 					arg0.cancel();
 				}
 
 			})
 			.setCancelable(true)
 			.show();
 			return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 	@Override
 	public void onTabSelected(ActionBar.Tab tab,
 			FragmentTransaction fragmentTransaction) {
 		viewPager.setCurrentItem(tab.getPosition());
 
 		if ((viewPager.getCurrentItem() == FRAGMENT_SUBSCRIPTIONS) && (subscriptionsFragment != null)) {
 			subscriptionsFragment.reloadAdapter();
 		} else if ((viewPager.getCurrentItem() == FRAGMENT_SUBSCRIBERS) && (subscribersFragment != null)) {
 			subscribersFragment.reloadAdapter();
 		}
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
 
 			if (credential == null) {
 				credential = GoogleAccountCredential.usingAudience(getApplicationContext(), "server:client_id:" + getString(R.string.client_id));
 				credential.setSelectedAccountName(account);
 			}
 
 			(new AsyncTask<String, Void, Void>() {
 
 				@Override
 				protected Void doInBackground(String... params) {
 
 					Subscriberendpoint.Builder endpointBuilder = new Subscriberendpoint.Builder(
 							AndroidHttp.newCompatibleTransport(),
 							new JacksonFactory(),
 							credential);
 					Subscriberendpoint endpoint = CloudEndpointUtils.updateBuilder(endpointBuilder).build();
 
 					try {
 						devices.clear();
 						devices.addAll(endpoint.subscriberEndpoint().subscribers(registrationId).execute().getItems());
 					} catch (IOException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 
 					return null;
 				}
 
 				@Override
 				protected void onPostExecute(Void result) {
					if ((viewPager.getCurrentItem() == FRAGMENT_SUBSCRIPTIONS) && (subscriptionsFragment != null)) {
 						subscriptionsFragment.reloadAdapter();
					} else if ((viewPager.getCurrentItem() == FRAGMENT_SUBSCRIBERS) && (subscribersFragment != null)) {
 						subscribersFragment.reloadAdapter();
 					}
 				}
 
 			}).execute();
 
 		} else {
 			devices.clear();
			if ((viewPager.getCurrentItem() == FRAGMENT_SUBSCRIPTIONS) && (subscriptionsFragment != null)) {
 				subscriptionsFragment.reloadAdapter();
			} else if ((viewPager.getCurrentItem() == FRAGMENT_SUBSCRIBERS) && (subscribersFragment != null)) {
 				subscribersFragment.reloadAdapter();
 			}
 		}
 	}
 
 	private DevicesFragment subscriptionsFragment;
 	private DevicesFragment subscribersFragment;
 
 	public class SectionsPagerAdapter extends FragmentPagerAdapter {
 
 		public SectionsPagerAdapter(FragmentManager fm) {
 			super(fm);
 		}
 
 		@Override
 		public Fragment getItem(int position) {
 			if (position == FRAGMENT_ACCOUNT) {
 				return new AccountsFragment();
 			} else if (position == FRAGMENT_SUBSCRIPTIONS) {
 				subscriptionsFragment = new DevicesFragment();
 				Bundle b = new Bundle();
 				b.putBoolean(ARGUMENT_ISSUBSCRIPTIONS, true);
 				subscriptionsFragment.setArguments(b);
 				return subscriptionsFragment;
 			} else if (position == FRAGMENT_SUBSCRIBERS) {
 				subscribersFragment = new DevicesFragment();
 				Bundle b = new Bundle();
 				b.putBoolean(ARGUMENT_ISSUBSCRIPTIONS, false);
 				subscribersFragment.setArguments(b);
 				return subscribersFragment;
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
 
 	}
 
 	@Override
 	public boolean hasRegistration() {
 		return (account != null) && (registrationId != null);
 	}
 
 	@Override
 	public String getRegistration() {
 		return registrationId;
 	}
 
 	@Override
 	public String getDeviceId(int which) {
 		if ((devices != null) && (which < devices.size())) {
 			return devices.get(which).getId();
 		}
 		return null;
 	}
 
 	@Override
 	public void loadDeviceModels(ArrayAdapter<String> adapter) {
 		if (devices != null) {
 			for (Subscriber device : devices) {
 				try {
 					adapter.add(URLDecoder.decode(device.getModel(), "UTF-8"));
 				} catch (UnsupportedEncodingException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 
 }
