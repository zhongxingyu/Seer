 /*
  * Copyright 2013 The Android Open Source Project
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.nurun.activemtl.ui;
 
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.Intent;
 import android.content.res.Configuration;
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 
 import com.nurun.activemtl.R;
 import com.nurun.activemtl.model.EventType;
 import com.nurun.activemtl.ui.fragment.HomeFragment;
 import com.nurun.activemtl.util.NavigationUtil;
 
 public class HomeActivity extends FragmentActivity {
 	private ActiveMtlDrawerLayout mDrawerLayout;
 	private CharSequence mTitle;
 
 	public static Intent newIntent(Context context) {
 		Intent intent = new Intent(context, HomeActivity.class);
 		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 		return intent;
 	}
 
 	public static PendingIntent newPendingIntent(Context context, int mId) {
 		return PendingIntent.getActivity(context, 165446846, new Intent(
 				context, HomeActivity.class), mId);
 	}
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.home_activity);
 
 		mTitle = getTitle();
 		mDrawerLayout = (ActiveMtlDrawerLayout) findViewById(R.id.drawer_layout);
 		mDrawerLayout.init(this, savedInstanceState == null);
 
 		// enable ActionBar app icon to behave as action to toggle nav drawer
 		getActionBar().setDisplayHomeAsUpEnabled(true);
 		getActionBar().setHomeButtonEnabled(true);
		getSupportFragmentManager().beginTransaction()
				.replace(R.id.content_frame, HomeFragment.newFragment())
				.commit();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.main, menu);
 		return super.onCreateOptionsMenu(menu);
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode,
 			Intent intent) {
 		super.onActivityResult(requestCode, resultCode, intent);
 		if (requestCode == NavigationUtil.requestLoginCode && resultCode == 200) {
 			NavigationUtil.goToProfile(this, getSupportFragmentManager());
 		}
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		// The action bar home/up action should open or close the drawer.
 		// ActionBarDrawerToggle will take care of this.
 		if (mDrawerLayout.onOptionsItemSelected(item)) {
 			return true;
 		}
 		// Handle action buttons
 		switch (item.getItemId()) {
 		case R.id.action_suggest:
			if (getSupportFragmentManager().findFragmentByTag(EventType.Alert.name()) != null) {
 				startActivity(SuggestionActivity.newIntent(this,
 						EventType.Alert));
 			} else if (getSupportFragmentManager().findFragmentByTag(
 					EventType.Challenge.name()) != null) {
 				startActivity(SuggestionActivity.newIntent(this,
 						EventType.Challenge));
 			} else if (getSupportFragmentManager().findFragmentByTag(
 					EventType.Idea.name()) != null) {
 				startActivity(SuggestionActivity
 						.newIntent(this, EventType.Idea));
 			} else {
 				startActivity(SuggestionActivity.newIntent(this));
 			}
 			return true;
 		case R.id.action_settings:
 			startActivity(PreferencesActivity.newIntent(this));
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	@Override
 	public void setTitle(CharSequence title) {
 		mTitle = title;
 		getActionBar().setTitle(mTitle);
 	}
 
 	@Override
 	protected void onPostCreate(Bundle savedInstanceState) {
 		super.onPostCreate(savedInstanceState);
 		mDrawerLayout.syncState();
 	}
 
 	@Override
 	public void onConfigurationChanged(Configuration newConfig) {
 		super.onConfigurationChanged(newConfig);
 		mDrawerLayout.onToggleConfigurationChanged(newConfig);
 	}
 
 }
