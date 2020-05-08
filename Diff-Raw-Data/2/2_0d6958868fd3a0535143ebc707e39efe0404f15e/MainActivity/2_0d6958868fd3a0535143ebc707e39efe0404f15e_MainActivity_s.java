 package com.isawabird;
 
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Typeface;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Parcelable;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.isawabird.db.DBHandler;
 import com.isawabird.parse.ParseConsts;
 import com.isawabird.parse.ParseUtils;
 import com.isawabird.parse.extra.SyncUtils;
 import com.isawabird.utilities.PostUndoAction;
 import com.isawabird.utilities.UndoBarController;
 import com.isawabird.utilities.UndoBarController.UndoListener;
 import com.parse.Parse;
 import com.parse.ParseAnalytics;
 import com.parse.ParseInstallation;
 import com.parse.PushService;
 
 public class MainActivity extends Activity {
 
 	TextView mBirdCountText;
 	TextView mTotalBirdCountText;
 	TextView currentListName;
 	TextView currentLocation;
 	TextView total_sightings_title;
 	Button btn_myLists;
 	Button btn_more;
 	Button btn_loginLogout;
 	Button btn_settings;
 	Button btn_help;
 	LinearLayout mLayoutSettings;
 	Button mSawBirdButton;
 	Typeface openSansLight;
 	Typeface openSansBold;
 	Typeface openSansBoldItalic;
 	Typeface tangerine;
 	ImageView helpOverlay;
 
 	private long birdCount = 0;
 	private long totalBirdCount = 0;
 	private long undoSightingId;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		openSansLight = Typeface.createFromAsset(getAssets(), "fonts/OpenSans-Light.ttf");
 		openSansBold = Typeface.createFromAsset(getAssets(), "fonts/OpenSans-Bold.ttf");
 		openSansBoldItalic = Typeface.createFromAsset(getAssets(), "fonts/OpenSans-BoldItalic.ttf");
 		tangerine = Typeface.createFromAsset(getAssets(), "fonts/Tangerine_Bold.ttf");
 
 		try {
 			// hide action bar
 			getActionBar().hide();
 			Utils.prefs = getSharedPreferences(Consts.PREF, Context.MODE_PRIVATE);
 			Parse.initialize(this, ParseConsts.APP_ID, ParseConsts.CLIENT_KEY);
 
 			PushService.setDefaultPushCallback(this, MainActivity.class);
 			ParseInstallation.getCurrentInstallation().saveInBackground();
 			ParseAnalytics.trackAppOpened(getIntent());
 			if (Utils.isFirstTime()) {
 				login();
 				// exit this activity
 				finish();
 			} else {
 
 				setContentView(R.layout.activity_main);
 				mSawBirdButton = (Button) findViewById(R.id.btn_isawabird);
 				mBirdCountText = (TextView) findViewById(R.id.textView_birdcount);
 				currentListName = (TextView) findViewById(R.id.textView_currentList);
 				total_sightings_title = (TextView) findViewById(R.id.textView_total_text);
 				mTotalBirdCountText = (TextView) findViewById(R.id.textView_total);
 				btn_more = (Button) findViewById(R.id.btn_more);
 				mLayoutSettings = (LinearLayout) findViewById(R.id.layout_settings);
 				btn_myLists = (Button) findViewById(R.id.btn_myLists);
 				btn_loginLogout = (Button) findViewById(R.id.btn_loginOrOut);
 				btn_settings = (Button) findViewById(R.id.btn_settings);
 				btn_help = (Button) findViewById(R.id.btn_help);
 				helpOverlay = (ImageView) findViewById(R.id.help_overlay);
 
 				mSawBirdButton.setTypeface(tangerine);
 				currentListName.setTypeface(openSansLight);
 				mBirdCountText.setTypeface(openSansLight);
 				total_sightings_title.setTypeface(openSansLight);
 				mTotalBirdCountText.setTypeface(openSansLight);
 				btn_more.setTypeface(openSansLight);
 				btn_loginLogout.setTypeface(openSansLight);
 				btn_settings.setTypeface(openSansLight);
 				btn_help.setTypeface(openSansLight);
 
 				new UpdateBirdCountAsyncTask().execute();
 				
 				// move heavy work to asynctask
 				new InitChecklistAsyncTask(getApplicationContext()).execute();
 				
 				/* Get FGPS location */
 				GPSLocation g = new GPSLocation();
 				g.getLocation(getApplicationContext());
 
 				/* Try to sync if needed */ 
 				SyncUtils.createSyncAccount(getApplicationContext());
 				SyncUtils.triggerRefresh();
 				
 				showHelpOverlay();
 
 				mBirdCountText.setOnClickListener(new OnClickListener() {
 
 					@Override
 					public void onClick(View v) {
 						Bundle b = new Bundle();
 						b.putString("listName", Utils.getCurrentListName());
 
 						Intent mySightingIntent = new Intent(getApplicationContext(), SightingsActivity.class);
 						mySightingIntent.putExtras(b);
 						startActivity(mySightingIntent);
 					}
 				});
 
 				currentListName.setOnClickListener(new OnClickListener() {
 
 					@Override
 					public void onClick(View v) {
 						Bundle b = new Bundle();
 						b.putString("listName", Utils.getCurrentListName());
 
 						Intent mySightingIntent = new Intent(getApplicationContext(), SightingsActivity.class);
 						mySightingIntent.putExtras(b);
 						startActivity(mySightingIntent);
 					}
 				});
 
 				total_sightings_title.setOnClickListener(new OnClickListener() {
 
 					@Override
 					public void onClick(View v) {
 						startActivity(new Intent(getApplicationContext(), BirdListActivity.class));
 					}
 				});
 
 				mTotalBirdCountText.setOnClickListener(new OnClickListener() {
 
 					@Override
 					public void onClick(View v) {
 						startActivity(new Intent(getApplicationContext(), BirdListActivity.class));
 					}
 				});
 
 				mSawBirdButton.setOnClickListener(new OnClickListener() {
 					public void onClick(View v) {
 						Intent searchIntent = new Intent(getApplicationContext(), SearchActivity.class);
 						startActivityForResult(searchIntent, 7);
 					}
 				});
 
 				btn_myLists.setOnClickListener(new OnClickListener() {
 					public void onClick(View v) {
 						startActivity(new Intent(getApplicationContext(), BirdListActivity.class));
 					}
 				});
 
 				btn_more.setOnClickListener(new OnClickListener() {
 					public void onClick(View v) {
 						if (mLayoutSettings.getVisibility() == View.GONE) {
 							mLayoutSettings.setVisibility(View.VISIBLE);
 						} else {
 							mLayoutSettings.setVisibility(View.GONE);
 						}
 					}
 				});
 
 				btn_help.setOnClickListener(new OnClickListener() {
 
 					@Override
 					public void onClick(View v) {
 						mLayoutSettings.setVisibility(View.GONE);
 						btn_more.setWidth(88);
 						helpOverlay.setVisibility(View.VISIBLE);
 						helpOverlay.setOnClickListener(new OnClickListener() {
 							@Override
 							public void onClick(View arg0) {
 								helpOverlay.setVisibility(View.INVISIBLE);
 							}
 						});
 					}
 				});
 
 				btn_loginLogout.setOnClickListener(new OnClickListener() {
 					public void onClick(View v) {
 						mLayoutSettings.setVisibility(View.GONE);
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
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		if(mLayoutSettings.getVisibility() != View.GONE) {
 			mLayoutSettings.setVisibility(View.GONE);
 		}
 		new UpdateBirdCountAsyncTask().execute();
 	}
 
 	public void showBirdList(View view) {
 		startActivity(new Intent(getApplicationContext(), BirdListActivity.class));
 	}
 	
 	public void showSettings(View view){
 		startActivity(new Intent(getApplicationContext(), DeveloperSettings.class));
 	}
 
 	private void showHelpOverlay() {
 		if (Utils.isFirstTime()) {
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
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		super.onActivityResult(requestCode, resultCode, data);
 		// beware: usage of magic numbers 7 and 14
 		if (requestCode == 7 && resultCode == 14) {
 			Bundle extras = data.getExtras();
 			final String speciesName = extras.getString(Consts.SPECIES_NAME);
 			new AddSightingAsyncTask().execute(speciesName);
 		}else{
 			  
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
 
 	private void login() {
 		Intent loginIntent = new Intent(getApplicationContext(), LoginActivity.class);
 		startActivity(loginIntent);
 	}
 
 	private void logout() {
 		// TODO implement
 	}
 
 	private class UpdateBirdCountAsyncTask extends AsyncTask<Void, Void, Long> {
 
 		protected Long doInBackground(Void... params) {
 
 			DBHandler dh = DBHandler.getInstance(getApplicationContext());
 			// TODO: not happy with static access to Utils class in DBHandler
 			birdCount = dh.getBirdCountForCurrentList();
 			totalBirdCount = dh.getTotalSpeciesCount();
 			return -1L;
 		}
 
 		protected void onPostExecute(Long param) {
 			Log.e(Consts.TAG, "Count: " + birdCount + ", total: " + totalBirdCount);
 			mBirdCountText.setText(String.valueOf(birdCount));
 			mTotalBirdCountText.setText(String.valueOf(totalBirdCount));
			if (Utils.getCurrentListName().isEmpty()) {
 				currentListName.setText(Utils.getCurrentListName());
 			}
 		}
 	}
 
 	private class AddSightingAsyncTask extends AsyncTask<String, String, Boolean> {
 		private String speciesName = "" ; 
 		protected Boolean doInBackground(String... params) {
 			DBHandler dh = DBHandler.getInstance(getApplicationContext());
 			try {
 				if (Utils.getCurrentListID() == -1) {
 					// create one based on todays date
 					BirdList list = new BirdList(new SimpleDateFormat("dd MMM yyyy").format(new Date()));
 					if (dh.addBirdList(list, true) == -1) {
 						return false;
 					}
 				}
 				speciesName = params[0]; 
 				undoSightingId = dh.addSightingToCurrentList(params[0]);
 				
 			} catch (ISawABirdException e) {
 				Log.e(Consts.TAG, e.getMessage());
 				if (e.getErrorCode() == ISawABirdException.ERR_SIGHTING_ALREADY_EXISTS) {
 					publishProgress("Species already exists");
 				}
 				return false;
 			}
 			return true;
 		}
 
 		@Override
 		protected void onProgressUpdate(String... values) {
 			Toast.makeText(getApplicationContext(), values[0], Toast.LENGTH_SHORT).show();
 		}
 
 		@Override
 		protected void onPostExecute(Boolean result) {
 			if (result) {
 				PostUndoAction action = new PostUndoAction() {
 					@Override
 					public void action() {
 						undoSightingId = -1;
 					}
 				};
 
 				UndoBarController.show(MainActivity.this, speciesName + " added successfully to list", new UndoListener() {
 					@Override
 					public void onUndo(Parcelable token) {
 						new DeleteSightingAsyncTask().execute();
 					}
 				}, action);
 				SyncUtils.triggerRefresh();
 				new UpdateBirdCountAsyncTask().execute();
 			}
 		}
 	}
 
 	private class DeleteSightingAsyncTask extends AsyncTask<Void, String, Boolean> {
 
 		protected Boolean doInBackground(Void... params) {
 			if (undoSightingId == -1)
 				return false;
 
 			DBHandler dh = DBHandler.getInstance(getApplicationContext());
 			dh.deleteSighting(undoSightingId);
 			return true;
 		}
 
 		@Override
 		protected void onProgressUpdate(String... values) {
 			Toast.makeText(getApplicationContext(), values[0], Toast.LENGTH_SHORT).show();
 		}
 
 		@Override
 		protected void onPostExecute(Boolean result) {
 			if (result) {
 				SyncUtils.triggerRefresh();
 				new UpdateBirdCountAsyncTask().execute();
 			}
 		}
 	}
 }
