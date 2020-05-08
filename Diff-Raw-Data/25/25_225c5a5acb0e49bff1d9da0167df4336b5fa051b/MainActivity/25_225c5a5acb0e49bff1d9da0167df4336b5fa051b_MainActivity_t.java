 package com.isawabird;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.graphics.Typeface;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Parcelable;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.isawabird.db.DBHandler;
 import com.isawabird.parse.ParseConsts;
 import com.isawabird.parse.ParseUtils;
 import com.isawabird.parse.extra.SyncUtils;
 import com.isawabird.utilities.UndoBarController;
 import com.isawabird.utilities.UndoBarController.UndoListener;
 import com.parse.Parse;
 import com.parse.ParseInstallation;
 import com.parse.PushService;
 
 public class MainActivity extends Activity {
 
 	static MainActivity act = null;
 
 	TextView numberSpecies;
 	TextView currentListName;
 	TextView currentLocation;
 	TextView total_sightings_title;
 	TextView total_sightings;
 	Button btn_myLists;
 	Button btn_more;
 	Button btn_loginLogout;
 	Button btn_settings;
 	Button mSawBirdButton;
 	Typeface openSansLight;
 	Typeface openSansBold;
 	Typeface openSansBoldItalic;
 	Typeface tangerine;
 	ImageView helpOverlay;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		act = this;
 
 		openSansLight = Typeface.createFromAsset(getAssets(), "fonts/OpenSans-Light.ttf");
 		openSansBold = Typeface.createFromAsset(getAssets(), "fonts/OpenSans-Bold.ttf");
 		openSansBoldItalic = Typeface.createFromAsset(getAssets(), "fonts/OpenSans-BoldItalic.ttf");
 		tangerine = Typeface.createFromAsset(getAssets(), "fonts/Tangerine_Bold.ttf");
 
 		try {
 			// TODO: hide action bar before switching to login screen
 			Utils.prefs = getSharedPreferences(Consts.PREF, Context.MODE_PRIVATE);
 			Parse.initialize(this, ParseConsts.APP_ID, ParseConsts.CLIENT_KEY);
 			ParseUtils.updateCurrentLocation();
 
 			PushService.setDefaultPushCallback(this, MainActivity.class);
 			ParseInstallation.getCurrentInstallation().saveInBackground();
 			// ParseAnalytics.trackAppOpened(getIntent());
 			if (Utils.isFirstTime()) {
 				login();
 				// exit this activity
 				finish();
 			} else {
 
 				setContentView(R.layout.activity_main);
 				mSawBirdButton = (Button) findViewById(R.id.btn_isawabird);
 				numberSpecies = (TextView) findViewById(R.id.text_mode);
 				currentListName = (TextView) findViewById(R.id.textView_currentList);
 				total_sightings_title = (TextView) findViewById(R.id.textView_total_text);
 				total_sightings = (TextView) findViewById(R.id.textView_total);
 				btn_myLists = (Button) findViewById(R.id.btn_myLists);
 				btn_more = (Button) findViewById(R.id.btn_more);
 				btn_loginLogout = (Button) findViewById(R.id.btn_loginOrOut);
 				btn_settings = (Button) findViewById(R.id.btn_settings);
 				helpOverlay = (ImageView) findViewById(R.id.help_overlay);
 
 				mSawBirdButton.setTypeface(tangerine);
 				currentListName.setTypeface(openSansBold);
 				numberSpecies.setTypeface(openSansBold);
 				total_sightings_title.setTypeface(openSansBold);
 				total_sightings.setTypeface(openSansLight);
 				btn_myLists.setTypeface(openSansLight);
 				btn_more.setTypeface(openSansLight);
 				btn_loginLogout.setTypeface(openSansLight);
 				btn_settings.setTypeface(openSansLight);
 
 				// move heavy work to asynctask
 				new InitAsyncTask().execute();
 
 				ParseUtils.updateCurrentLocation();
 
 				/* Set up the sync service */
 				SyncUtils.createSyncAccount(this);
 				SyncUtils.triggerRefresh();
 				ParseInstallation.getCurrentInstallation().saveInBackground();
 
 				showHelpOverlay();
 				updateBirdsCount();
 
 				mSawBirdButton.setOnClickListener(new OnClickListener() {
 					public void onClick(View v) {
 						Intent searchIntent = new Intent(getApplicationContext(), SearchActivity.class);
 						startActivityForResult(searchIntent, 7);
 					}
 				});
 
 				btn_myLists.setOnClickListener(new OnClickListener() {
 					public void onClick(View v) {
 						startActivity(new Intent(getApplicationContext(), MyListActivity.class));
 					}
 				});
 
 				btn_more.setOnClickListener(new OnClickListener() {
 					public void onClick(View v) {
 						if (btn_more.getWidth() == btn_settings.getWidth()) {
 							btn_settings.setVisibility(View.INVISIBLE);
 							btn_loginLogout.setVisibility(View.INVISIBLE);
 							btn_more.setWidth(88);
 						} else {
 							btn_settings.setVisibility(View.VISIBLE);
 							btn_loginLogout.setVisibility(View.VISIBLE);
 							btn_more.setWidth(btn_settings.getWidth());
 						}
 					}
 				});
 
 				btn_loginLogout.setOnClickListener(new OnClickListener() {
 					public void onClick(View v) {
 						btn_settings.setVisibility(View.INVISIBLE);
 						btn_loginLogout.setVisibility(View.INVISIBLE);
 						btn_more.setWidth(88);
 						startActivity(new Intent(getApplicationContext(), LoginActivity.class));
 					}
 				});
 
 				Log.i(Consts.TAG, "current List ID: " + Utils.getCurrentListID());
 				Log.i(Consts.TAG, "current List Name: " + Utils.getCurrentListName());
 				Log.i(Consts.TAG, "current Username: " + ParseUtils.getCurrentUsername());
 			}
 		} catch (Exception ex) {
 			ex.printStackTrace();
 		}
 	}
 
 	private void showHelpOverlay() {
 		SharedPreferences prefs = this.getSharedPreferences("com.isawabird", Context.MODE_PRIVATE);
 		if (prefs.getBoolean("IsFirstRun", true)) {
 			prefs.edit().putBoolean("IsFirstRun", false).commit();
 			helpOverlay.setOnClickListener(new OnClickListener() {
 
 				@Override
 				public void onClick(View arg0) {
 					helpOverlay.setVisibility(View.INVISIBLE);
 				}
 			});
 		} else {
 			helpOverlay.setVisibility(View.INVISIBLE);
 		}
 	}
 
 	private void updateBirdsCount() {
 		DBHandler mydbh = DBHandler.getInstance(MainActivity.getContext());
 		numberSpecies.setText(Long.toString(mydbh.getBirdCountForCurrentList()));
 		total_sightings.setText(Long.toString(mydbh.getTotalSpeciesCount()));
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		super.onActivityResult(requestCode, resultCode, data);
 		// beware: usage of magic numbers 7 and 14
 		if (requestCode == 7) {
 			if (resultCode == 14) {
 				Bundle extras = data.getExtras();
 				final String speciesName = extras.getString(Consts.SPECIES_NAME);
 
 				final DBHandler dh = DBHandler.getInstance(MainActivity.getContext());
 				try {
 					dh.addSightingToCurrentList(speciesName);
 					UndoBarController.show(MainActivity.this, speciesName + " added successfully to list", new UndoListener() {
 
 						@Override
 						public void onUndo(Parcelable token) {
 							dh.deleteSightingFromCurrentList(speciesName);
 							SyncUtils.triggerRefresh();
 							updateBirdsCount();
 						}
 					});
 					SyncUtils.triggerRefresh();
 				} catch (ISawABirdException ex) {
 					// TODO Change to use strings.xml
 					if (ex.getErrorCode() == ISawABirdException.ERR_SIGHTING_ALREADY_EXISTS) {
 						Toast.makeText(SearchActivity.getContext(), "Species already exists", Toast.LENGTH_SHORT).show();
 					}
 				}
 
 				updateBirdsCount();
 
 			}
 		}
 	}
 
 	long lastPress;
 
 	public void onBackPressed() {
 		long currentTime = System.currentTimeMillis();
 		if (currentTime - lastPress > 5000) {
 			Toast.makeText(getBaseContext(), "Press Back again to exit.", Toast.LENGTH_SHORT).show();
 			lastPress = currentTime;
 		} else {
 			finish();
 		}
 	}
 
 	public static Context getContext() {
 		return act.getApplicationContext();
 	}
 
 	private void login() {
 		Intent loginIntent = new Intent(getApplicationContext(), LoginActivity.class);
 		startActivity(loginIntent);
 	}
 
 	private void logout() {
 		// TODO implement
 	}
 
 	/*
 	 * public static ConnectivityManager getConnectivityManager(){ return
 	 * (ConnectivityManager)act.getSystemService(CONNECTIVITY_SERVICE); }
 	 */
 
 	private class InitAsyncTask extends AsyncTask<Void, Void, Long> {
 
 		protected Long doInBackground(Void... params) {
 
 			/* Initialize the checklists */
 			Log.i(Consts.TAG, "Starting checklist init...");
 			try {
 				Utils.initializeChecklist(getApplicationContext(), Utils.getChecklistName());
 				SyncUtils.createSyncAccount(MainActivity.getContext());
 				SyncUtils.triggerRefresh();
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 			Log.i(Consts.TAG, "Checklist init complete");
 
 			// TODO: remove below line after dev
 			// put all the dump tables and testing in data loader
 			// new
 			// DataLoader(getApplicationContext()).load(this.getDatabasePath(DBConsts.DATABASE_NAME).getAbsolutePath());
 			// new DataLoader(getApplicationContext())
 			// .srihariTestFunction(getApplicationContext()
 			// .getDatabasePath(DBConsts.DATABASE_NAME)
 			// .getAbsolutePath());
 			//
 			DBHandler dh = DBHandler.getInstance(getApplicationContext());
 			// TODO: not happy with static access to Utils class in DBHandler
 			return dh.getBirdCountForCurrentList();
 		}
 
 		protected void onPostExecute(Long param) {
 			numberSpecies.setText(String.valueOf(param));
 			if (Utils.getCurrentListName() != "")
 				currentListName.setText(Utils.getCurrentListName());
 		}
 	}
 }
