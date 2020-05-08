 package matdue.raidstatus;
 
 import java.text.NumberFormat;
 import java.util.Date;
 
 import matdue.raidstatus.data.Raid;
 import matdue.raidstatus.data.RaidMember;
 import matdue.raidstatus.data.database.RaidDatabase;
 import android.app.Activity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.graphics.Bitmap;
 import android.os.Bundle;
 import android.os.Handler;
 import android.text.format.DateFormat;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.Window;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class MainActivity extends Activity {
 	
 	// Update thread; to prevent multiple updater threads running,
 	// we'll save the current thread here
 	private Thread updaterThread = null;
 	
 	private Handler handler = new Handler() {
 		@Override
 		public void handleMessage(android.os.Message msg) {
 			switch (msg.what) {
 			case ConcurrentUpdater.OK:
 				updaterThread = null;
 				setProgressBarIndeterminateVisibility(false);
 				updateView();
 				break;
 				
 			case ConcurrentUpdater.NETWORK_ERROR:
 				updaterThread = null;
 				setProgressBarIndeterminateVisibility(false);
 				String message = getResources().getString(R.string.message_dkp_network_error, msg.obj);
 				Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
 				break;
 			
 			case ConcurrentUpdater.NO_DATA:
 				updaterThread = null;
 				setProgressBarIndeterminateVisibility(false);
 				Toast.makeText(MainActivity.this, R.string.message_dkp_no_data, Toast.LENGTH_LONG).show();
 				break;
 			}
 		}
 	};
 	
 	private boolean preferencesHaveChanged = false;
 	private SharedPreferences.OnSharedPreferenceChangeListener preferencesListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
 		public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
 			preferencesHaveChanged = true;
 		}
 	};
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         
         PreferencesActivity.getApplicationPreferences(this).registerOnSharedPreferenceChangeListener(preferencesListener);
         
         requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
         setContentView(R.layout.main);
         
         Button showInfoButton = (Button) findViewById(R.id.main_raid_showinfo);
         showInfoButton.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				showRaidInfo();
 			}
 		});
         
         Button configButton = (Button) findViewById(R.id.main_button_config);
         configButton.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				startActivity(new Intent(MainActivity.this, PreferencesActivity.class));
 			}
 		});
         
         updateView();
     }
     
     @Override
     protected void onStart() {
     	super.onStart();
     	
     	if (preferencesHaveChanged) {
     		preferencesHaveChanged = false;
     		updateRaidData();
     	}
     }
     
     @Override
     protected void onResume() {
     	super.onResume();
     	
     	// Automatically update if last update is an hour or more ago
     	long lastUpdate = PreferencesActivity.getApplicationPreferences(this).getLong("lastUpdate", 0);
     	if (lastUpdate == 0 || new Date().getTime() - lastUpdate > 60*60*1000L) {
     		updateRaidData();
     	}
     }
     
     @Override
     protected void onDestroy() {
     	super.onDestroy();
     	
     	PreferencesActivity.getApplicationPreferences(this).unregisterOnSharedPreferenceChangeListener(preferencesListener);
     }
     
     private void updateView() {
     	RaidDatabase db = new RaidDatabase(this);
     	Raid nextRaid = db.loadRaid(0);
     	if (nextRaid != null) {
     		findViewById(R.id.NoDataLayout).setVisibility(View.GONE);
     		findViewById(R.id.RaidLayout).setVisibility(View.VISIBLE);
     		
     		// Raid information
     		String message = getResources().getString(R.string.main_raid_title, nextRaid.getName());
     		TextView view = (TextView) findViewById(R.id.main_raid_title);
     		view.setText(message);
     		
     		message = getResources().getString(R.string.main_raid_datetime, 
     				DateFormat.format(getResources().getString(R.string.main_raid_date_format), nextRaid.getStart()),
     				DateFormat.format(getResources().getString(R.string.main_raid_time_format), nextRaid.getStart()));
     		view = (TextView) findViewById(R.id.main_raid_datetime);
     		view.setText(message);
     		
     		String url = PreferencesActivity.getApplicationPreferences(this).getString("url", "");
     		if (!url.endsWith("/")) {
     			url = url + "/";
     		}
     		url = url + "games/WoW/events/" + nextRaid.getIcon();
     		new ImageLoaderTask(getCacheDir()) {
     			@Override
     			protected void onPostExecute(Bitmap result) {
     				if (result != null) {
     					ImageView image = (ImageView) findViewById(R.id.RaidLogo);
     		    		image.setImageBitmap(result);
     				}
     			};
     		}.execute(url);
     		
     		// Player information
     		String playerName = PreferencesActivity.getApplicationPreferences(this).getString("charname", null);
     		if (playerName != null) {
 				// Lookup player in members
 				for (RaidMember member : nextRaid.getRaidMembers()) {
 					if (playerName.equals(member.getPlayer().getName())) {
 						// Got it, display information
 						String subscription = getResources().getStringArray(R.array.subscription)[member.getSubscribed()];
 						
 						if (member.getRole() == null) {
 							message = getResources().getString(R.string.main_raid_player, 
 									playerName, subscription);
 						} else {
 							String role = "???";
 							if ("tank".equals(member.getRole())) {
 								role = getResources().getStringArray(R.array.role)[0];
 							} else if ("healer".equals(member.getRole())) {
 								role = getResources().getStringArray(R.array.role)[1];
 							} else if ("melee".equals(member.getRole())) {
 								role = getResources().getStringArray(R.array.role)[2];
 							} else if ("range".equals(member.getRole())) {
 								role = getResources().getStringArray(R.array.role)[3];
 							}
 							
 							message = getResources().getString(R.string.main_raid_player_role, 
 									playerName, subscription, role);
 						}
 						view = (TextView) findViewById(R.id.main_raid_player);
 						view.setText(message);
 						view.setVisibility(View.VISIBLE);
 						
 						// Note
 						view = (TextView) findViewById(R.id.main_raid_player_note);
 						if (member.getNote() != null && member.getNote().length() != 0) {
 							message = getResources().getString(R.string.main_raid_player_note, member.getNote());
 							view.setText(message);
 							view.setVisibility(View.VISIBLE);
 						} else {
 							view.setVisibility(View.GONE);
 						}
 						
 						// DKP
 						message = getResources().getString(R.string.main_raid_player_dkp, 
 								NumberFormat.getInstance().format(member.getPlayer().getCurrentDkp()));
 						view = (TextView) findViewById(R.id.main_raid_player_dkp);
 						view.setText(message);
 						view.setVisibility(View.VISIBLE);
 						
 						break;
 					}
 				}
     		} else {
     			view = (TextView) findViewById(R.id.main_raid_player);
     			view.setText(R.string.main_raid_no_character);
 				view.setVisibility(View.VISIBLE);
     		}
     	} else {
     		findViewById(R.id.RaidLayout).setVisibility(View.GONE);
     		findViewById(R.id.NoDataLayout).setVisibility(View.VISIBLE);
     	}
     	db.close();
     }
     
     private void showRaidInfo() {
     	startActivity(new Intent(this, RaidInfoActivity.class));
     }
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
     	getMenuInflater().inflate(R.menu.menu_main, menu);
     	return super.onCreateOptionsMenu(menu);
     }
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
     	switch (item.getItemId()) {
     	case R.id.menu_main_preferences:
     		startActivity(new Intent(this, PreferencesActivity.class));
     		return true;
     		
     	case R.id.menu_main_update:
     		updateRaidData();
     		return true;
     		
     	default:
         	return super.onOptionsItemSelected(item);
     	}
     }
     
     private void updateRaidData() {
     	if (updaterThread != null && updaterThread.getState() != Thread.State.TERMINATED) {
     		// Updating still in progress, exit silently
     		return;
     	}
     	
     	Editor editor = PreferencesActivity.getApplicationPreferences(this).edit();
     	editor.putLong("lastUpdate", new Date().getTime());
     	editor.commit();  // will trigger registered OnSharedPreferenceChangeListener
     	preferencesHaveChanged = false;
     	
 		String url = PreferencesActivity.getApplicationPreferences(this).getString("url", null);
 		if (url == null) {
 			Toast.makeText(this, R.string.message_not_configured_yet, Toast.LENGTH_SHORT).show();
 			return;
 		}
 
 		if (!url.endsWith("/")) {
 			url = url + "/";
 		}
 		url = url + "getdkp.php";
 
         setProgressBarIndeterminateVisibility(true);
         RaidDatabase db = new RaidDatabase(this);
         ConcurrentUpdater updater = new ConcurrentUpdater(url, handler, db);
         updaterThread = new Thread(updater);
         updaterThread.start();
     }
 }
