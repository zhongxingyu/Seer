 /*******************************************************************************
  * Copyright 2012-2013 Trento RISE
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *        http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either   express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  ******************************************************************************/
 package eu.trentorise.smartcampus.dt;
 
 import android.accounts.AccountManager;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
 import android.content.res.Configuration;
 import android.content.res.Resources;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.support.v4.app.FragmentTransaction;
 import android.util.Log;
 import android.util.TypedValue;
 import android.view.Display;
 import android.view.View;
 import android.widget.Toast;
 
 import com.actionbarsherlock.app.ActionBar;
 import com.actionbarsherlock.app.SherlockFragment;
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 import com.actionbarsherlock.view.Window;
 import com.github.espiandev.showcaseview.BaseTutorialActivity;
 
 import eu.trentorise.smartcampus.ac.SCAccessProvider;
 import eu.trentorise.smartcampus.android.common.SCAsyncTask;
 import eu.trentorise.smartcampus.android.feedback.activity.FeedbackFragmentActivity;
 import eu.trentorise.smartcampus.dt.custom.AbstractAsyncTaskProcessor;
 import eu.trentorise.smartcampus.dt.custom.TabListener;
 import eu.trentorise.smartcampus.dt.custom.TutorialActivity;
 import eu.trentorise.smartcampus.dt.custom.data.DTHelper;
 import eu.trentorise.smartcampus.dt.custom.data.DTHelper.Tutorial;
 import eu.trentorise.smartcampus.dt.fragments.events.AllEventsFragment;
 import eu.trentorise.smartcampus.dt.fragments.events.EventDetailsFragment;
 import eu.trentorise.smartcampus.dt.fragments.home.HomeFragment;
 import eu.trentorise.smartcampus.dt.fragments.pois.AllPoisFragment;
 import eu.trentorise.smartcampus.dt.fragments.pois.PoiDetailsFragment;
 import eu.trentorise.smartcampus.dt.fragments.stories.AllStoriesFragment;
 import eu.trentorise.smartcampus.dt.fragments.stories.StoryDetailsFragment;
 import eu.trentorise.smartcampus.dt.model.BaseDTObject;
 import eu.trentorise.smartcampus.dt.model.EventObject;
 import eu.trentorise.smartcampus.dt.model.POIObject;
 import eu.trentorise.smartcampus.dt.model.StoryObject;
 import eu.trentorise.smartcampus.protocolcarrier.exceptions.SecurityException;
 
 public class DiscoverTrentoActivity extends FeedbackFragmentActivity {
 
 	private final static int TUTORIAL_REQUEST_CODE = 1;
 	private Tutorial lastShowed;
 	private boolean isLoading;
 
 	@Override
 	protected void onSaveInstanceState(Bundle outState) {
 		super.onSaveInstanceState(outState);
 		outState.putInt("tag", getSupportActionBar().getSelectedNavigationIndex());
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		setUpContent(savedInstanceState != null ? savedInstanceState.getInt("tag") : null);
 
 		initDataManagement(savedInstanceState);
 
 		// DEBUG PURPOSE
 		// DTHelper.getTutorialPreferences(this).edit().clear().commit();
 
 		if (DTHelper.isFirstLaunch(this)) {
 			showTourDialog();
 			DTHelper.disableFirstLaunch(this);
 		}
 	}
 
 	@Override
 	protected void onResume() {
 		if (DTHelper.getLocationHelper() != null)
 			DTHelper.getLocationHelper().start();
 		super.onResume();
 	}
 
 	@Override
 	protected void onPostResume() {
 		super.onPostResume();
 		if (DTHelper.wantTour(this))
 			showTutorial();
 	}
 
 	@Override
 	protected void onPause() {
 		if (DTHelper.getLocationHelper() != null)
 			DTHelper.getLocationHelper().stop();
 		super.onPause();
 	}
 
 	@Override
 	protected void onDestroy() {
 		DTHelper.destroy();
 		super.onDestroy();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		menu.clear();
 		MenuInflater inflater = getSupportMenuInflater();
 		inflater.inflate(R.menu.emptymenu, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		if (item.getItemId() == android.R.id.home) {
 			onBackPressed();
 			return true;
 		} else
 			return super.onOptionsItemSelected(item);
 
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		super.onActivityResult(requestCode, resultCode, data);
 	
 		if (requestCode == TUTORIAL_REQUEST_CODE) {
 			if (resultCode == RESULT_OK) {
 				String resData = data.getExtras().getString(BaseTutorialActivity.RESULT_DATA);
 				if (resData.equals(BaseTutorialActivity.OK)) {
 					DTHelper.setTutorialAsShowed(this, lastShowed);
 				}
 				if (DTHelper.wantTour(this))
 					showTutorial();
 			}
 		} else {
 			if (resultCode == RESULT_OK) {
 				String token = data.getExtras().getString(AccountManager.KEY_AUTHTOKEN);
 				if (token == null) {
 					Toast.makeText(this, R.string.app_failure_security, Toast.LENGTH_LONG).show();
 					finish();
 				} else {
 					initData(token);
 				}
 			} else if (resultCode == RESULT_CANCELED && requestCode == SCAccessProvider.SC_AUTH_ACTIVITY_REQUEST_CODE) {
 				DTHelper.endAppFailure(this, R.string.token_required);
 			}
 		}
 	}
 
 	private void initDataManagement(Bundle savedInstanceState) {
 		try {
 			DTHelper.init(getApplicationContext());
 			String token = DTHelper.getAccessProvider().getAuthToken(this, null);
 			if (token != null) {
 				initData(token);
 			}
 		} catch (Exception e) {
 			Toast.makeText(this, R.string.app_failure_init, Toast.LENGTH_LONG).show();
 			return;
 		}
 	}
 
 	private boolean initData(String token) {
 		try {
 			new SCAsyncTask<Void, Void, BaseDTObject>(this, new LoadDataProcessor(this)).execute();
 		} catch (Exception e1) {
 			Toast.makeText(this, R.string.app_failure_init, Toast.LENGTH_LONG).show();
 			return false;
 		}
 		return true;
 	}
 
 	private void setUpContent(Integer pos) {
 		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
 		setSupportProgressBarIndeterminateVisibility(false);
 
 		setContentView(R.layout.main);
 
 		ActionBar actionBar = getSupportActionBar();
 		actionBar.setDisplayShowTitleEnabled(true); // system title
 		actionBar.setDisplayShowHomeEnabled(true); // home icon bar
 		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS); // tabs bar
 
 		// Home
 		ActionBar.Tab tab = actionBar.newTab();
 		tab.setText(R.string.tab_home);
 		tab.setTabListener(new TabListener<HomeFragment>(this, "me", HomeFragment.class));
 		actionBar.addTab(tab);
 
 		// Points of interest
 		tab = actionBar.newTab();
 		tab.setText(R.string.tab_places);
 		tab.setTabListener(new TabListener<AllPoisFragment>(this, "pois", AllPoisFragment.class));
 		actionBar.addTab(tab);
 
 		// Events
 		tab = actionBar.newTab();
 		tab.setText(R.string.tab_events);
 		tab.setTabListener(new TabListener<AllEventsFragment>(this, "events", AllEventsFragment.class));
 		actionBar.addTab(tab);
 
 		// Stories
 		// ATTENZIONE se si modifica la posizione di questa tab il tutorial
 		// sballa
 		// bisogna modificare anche alla riga 475 (circa) dove si seleziona la
 		// tab delle storie
 		tab = getSupportActionBar().newTab();
 		tab.setText(R.string.tab_stories);
 		tab.setTabListener(new TabListener<AllStoriesFragment>(this, "stories", AllStoriesFragment.class));
 		actionBar.addTab(tab);
 
 		if (pos != null)
 			actionBar.selectTab(actionBar.getTabAt(pos));
 		actionBar.setHomeButtonEnabled(false);
 		actionBar.setDisplayHomeAsUpEnabled(true);
 
 	}
 
 	private class LoadDataProcessor extends AbstractAsyncTaskProcessor<Void, BaseDTObject> {
 
 		private int syncRequired = 0;
 		private SherlockFragmentActivity currentRootActivity = null;
 
 		public LoadDataProcessor(Activity activity) {
 			super(activity);
 		}
 
 		@Override
 		public BaseDTObject performAction(Void... params) throws SecurityException, Exception {
 			Long entityId = getIntent().getLongExtra(getString(R.string.view_intent_arg_entity_id), -1);
 			String type = getIntent().getStringExtra(getString(R.string.view_intent_arg_entity_type));
 
 			Exception res = null;
 
 			try {
 				syncRequired = DTHelper.syncRequired();
 			} catch (Exception e) {
 				res = e;
 			}
 
 			if (entityId > 0 && type != null) {
 				if ("event".equals(type))
 					return DTHelper.findEventByEntityId(entityId);
 				else if ("location".equals(type))
 					return DTHelper.findPOIByEntityId(entityId);
 				else if ("narrative".equals(type))
 					return DTHelper.findStoryByEntityId(entityId);
 			} else if (res != null) {
 				throw res;
 			}
 			return null;
 		}
 
 		@Override
 		public void handleResult(BaseDTObject result) {
 			if (syncRequired != DTHelper.SYNC_NOT_REQUIRED) {
 				if (syncRequired == DTHelper.SYNC_REQUIRED_FIRST_TIME) {
 					Toast.makeText(DiscoverTrentoActivity.this, R.string.initial_data_load, Toast.LENGTH_LONG).show();
 				}
 				setSupportProgressBarIndeterminateVisibility(true);
 				isLoading = true;
 				new Thread(new Runnable() {
 					@Override
 					public void run() {
 						try {
 							currentRootActivity = DTHelper.start(DiscoverTrentoActivity.this);
 						} catch (Exception e) {
 							e.printStackTrace();
 						} finally {
 							if (currentRootActivity != null) {
 								currentRootActivity.runOnUiThread(new Runnable() {
 									@Override
 									public void run() {
 										currentRootActivity.setSupportProgressBarIndeterminateVisibility(false);
 										if (DiscoverTrentoActivity.this != null)
 											DiscoverTrentoActivity.this.setSupportProgressBarIndeterminateVisibility(false);
 										else
 											Log.e("no", "woman no cry");
 										isLoading = false;
 									}
 								});
 							}
 						}
 					}
 				}).start();
 			} else {
 				setSupportProgressBarIndeterminateVisibility(false);
 
 			}
 
 			Long entityId = getIntent().getLongExtra(getString(R.string.view_intent_arg_entity_id), -1);
 			if (entityId > 0) {
 				if (result == null) {
 					Toast.makeText(DiscoverTrentoActivity.this, R.string.app_failure_obj_not_found, Toast.LENGTH_LONG).show();
 					return;
 				}
 
 				SherlockFragment fragment = null;
 				String tag = null;
 				Bundle args = new Bundle();
 				if (result instanceof POIObject) {
 					fragment = new PoiDetailsFragment();
 					args.putString(PoiDetailsFragment.ARG_POI_ID, result.getId());
 					tag = "pois";
 				} else if (result instanceof EventObject) {
 					fragment = new EventDetailsFragment();
 					args.putString(EventDetailsFragment.ARG_EVENT_ID, (result.getId()));
 					tag = "events";
 				} else if (result instanceof StoryObject) {
 					fragment = new StoryDetailsFragment();
 					args.putString(StoryDetailsFragment.ARG_STORY_ID, result.getId());
 					tag = "stories";
 					// } else if (result instanceof StoryObject) {
 					// fragment = new EventDetailsFragment();
 					// args.putSerializable(StoryDetailsFragment.ARG_STORY_OBJECT,
 					// result);
 					// tag = "stories";
 				}
 				if (fragment != null) {
 					FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
 					fragment.setArguments(args);
 
 					fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
 					fragmentTransaction.replace(android.R.id.content, fragment, tag);
 					fragmentTransaction.addToBackStack(fragment.getTag());
 					fragmentTransaction.commit();
 				}
 			}
 		}
 
 	}
 
 	@Override
 	public void onNewIntent(Intent intent) {
 		try {
 			DTHelper.getAccessProvider().getAuthToken(this, null);
 		} catch (Exception e) {
 			Toast.makeText(this, R.string.app_failure_init, Toast.LENGTH_LONG).show();
 			return;
 		}
 
 	}
 
 	private void showTourDialog() {
 		AlertDialog.Builder builder = new AlertDialog.Builder(this).setMessage(getString(R.string.dt_first_launch))
 				.setPositiveButton(getString(R.string.begin_tut), new DialogInterface.OnClickListener() {
 
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 						DTHelper.setWantTour(DiscoverTrentoActivity.this, true);
 						showTutorial();
 					}
 				}).setNeutralButton(getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
 
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 						DTHelper.setWantTour(DiscoverTrentoActivity.this, false);
 						dialog.dismiss();
 					}
 				});
 		builder.create().show();
 	}
 
 	private void showTutorial() {
		DTHelper.Tutorial t = getFirstValidTutorial();
 		int id;
 		String title = "Tip!";
 		String msg = "";
 		boolean isLast = false;
 		switch (t) {
 		case NOTIF:
 			id = R.id.menu_item_notifications;
 			title = getString(R.string.notifications_unread);
 			msg = getString(R.string.dt_notif_tut);
 			break;
 		case EVENTS:
 			id = R.id.menu_item_show_events_layers;
 			title = getString(R.string.menu_item__events_layers_text);
 			msg = getString(R.string.dt_events_tut);
 			break;
 		case PLACES:
 			id = R.id.menu_item_show_places_layers;
 			title = getString(R.string.menu_item__places_layers_text);
 			msg = getString(R.string.dt_places_tut);
 			break;
 		case STORIES:
 			id = -3;
 			title = getString(R.string.tab_stories);
 			msg = getString(R.string.dt_stories_tut);
 			isLast = true;
 			break;
 		default:
 			id = -1;
 		}
 		if (t != null) {
 			lastShowed = t;
 			displayShowcaseView(id, title, msg, isLast);
 		} else
 			DTHelper.setWantTour(this, false);
 	}
 
	private Tutorial getFirstValidTutorial() {
		Tutorial t = DTHelper.getLastTutorialNotShowed(this);
		/*if smartcampus (no notif) salta notifiche (setta a true notif*/
		ApplicationInfo ai;
		try {
			ai = getPackageManager().getApplicationInfo(
					getPackageName(), PackageManager.GET_META_DATA);
			Bundle aBundle = ai.metaData;
			if (aBundle.getBoolean("hidden-notification")&& t.equals(t.NOTIF))
			{ DTHelper.setTutorialAsShowed(this, t);
				t =DTHelper.getLastTutorialNotShowed(this);

			}
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return t;
	}
	
	
 	private void displayShowcaseView(int id, String title, String msg, boolean isLast) {
 		int[] position = new int[2];
 		int radius = 0;
 		if (id != -3) {
 			View v = findViewById(id);
 			if (v != null) {
 				v.getLocationOnScreen(position);
 				radius = v.getWidth();
 				BaseTutorialActivity.newIstance(this, position, radius, Color.WHITE, null, title, msg, isLast,
 						TUTORIAL_REQUEST_CODE, TutorialActivity.class);
 			}
 		} else {
 			Resources res = getResources();
 
 			if (res.getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
 				Display d = getWindowManager().getDefaultDisplay();
 				radius = d.getWidth() / 5;
 				position[0] = (int) (d.getWidth() - radius - TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5,
 						res.getDisplayMetrics()));
 				position[1] = (int) (getSupportActionBar().getHeight() / 2 + TypedValue.applyDimension(
 						TypedValue.COMPLEX_UNIT_DIP, 24, res.getDisplayMetrics()));
 
 			} else {
 
 				try {
 					getSupportActionBar().selectTab(getSupportActionBar().getTabAt(3));
 				} catch (IllegalStateException e) {
 					// Do nothing because there is nothing to do
 				}
 
 				View v = findViewById(R.id.menu_item_notifications);
 				if (v != null) {
 					v.getLocationOnScreen(position);
 				}
 				if (isLoading) {
 					position[0] -= TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48, res.getDisplayMetrics());
 				}
 				position[0] -= TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, res.getDisplayMetrics());
 
 				radius = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, title.length() * 12,
 						res.getDisplayMetrics());
 
 			}
 			BaseTutorialActivity.newIstance(this, position, radius, Color.WHITE, null, title, msg, isLast,
 					TUTORIAL_REQUEST_CODE, TutorialActivity.class);
 		}
 
 	}
 
 	@Override
 	public String getAppToken() {
 		return DTParamsHelper.getAppToken();
 	}
 
 	@Override
 	public String getAuthToken() {
 		return DTHelper.getAuthToken();
 	}
 
 }
