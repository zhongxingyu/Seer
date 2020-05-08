 package cens.ucla.edu.budburst;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import cens.ucla.edu.budburst.database.OneTimeDBHelper;
 import cens.ucla.edu.budburst.database.StaticDBHelper;
 import cens.ucla.edu.budburst.database.SyncDBHelper;
 import cens.ucla.edu.budburst.floracaching.FloraCacheEasyLevel;
 import cens.ucla.edu.budburst.floracaching.FloraCacheMain;
 import cens.ucla.edu.budburst.helper.HelperBackgroundService;
 import cens.ucla.edu.budburst.helper.HelperFunctionCalls;
 import cens.ucla.edu.budburst.helper.HelperSettings;
 import cens.ucla.edu.budburst.helper.HelperSharedPreference;
 import cens.ucla.edu.budburst.helper.HelperValues;
 import cens.ucla.edu.budburst.lists.ListMain;
 import cens.ucla.edu.budburst.lists.ListUserDefinedSpecies;
 import cens.ucla.edu.budburst.mapview.MapViewMain;
 import cens.ucla.edu.budburst.myplants.PBBPlantList;
 import cens.ucla.edu.budburst.onetime.OneTimeMainPage;
 import cens.ucla.edu.budburst.utils.QuickCapture;
 import cens.ucla.edu.budburst.weeklyplant.WeeklyPlant;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.ActivityNotFoundException;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Handler;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.Button;
 import android.widget.ImageButton;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class PBBMainPage extends Activity {
 	
 	private ImageButton myPlantBtn = null;
 	private Button oneTimeBtn = null;
 	private Button myResultBtn = null;
 	private Button mapBtn = null;
 	private Button newsBtn = null;
 	private Button weeklyBtn = null;
 	private Button floraBtn = null;
 	private TextView mUserInfo = null;
 	private SharedPreferences pref;
 	private String mUsername;
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 	    super.onCreate(savedInstanceState);
 	    setContentView(R.layout.mainpage);
 	    
 	    pref = getSharedPreferences("userinfo",0);
 	    
 	    mUsername = pref.getString("Username", "");
 	    if(mUsername.equals("test10")){
 	    	mUsername = "Preview";
 	    }
 	    SharedPreferences.Editor edit = pref.edit();	
 	    edit.putBoolean("new", false);
 		edit.putString("visited","false");
 		edit.commit();
 		
 		LinearLayout ll = (LinearLayout) findViewById(R.id.header);
 		ll.setOnClickListener(new View.OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				Intent intent = new Intent(PBBMainPage.this, firstActivity.class);
 				finish();
 				startActivity(intent);
 			}
 		});
 		
 		
 		
 		// check sync
 		int synced = checkSync();
 	    
 	    // if the app has not been synced.
 	    if(synced == SyncDBHelper.SYNCED_NO) {
 	    	LinearLayout sync_layout = (LinearLayout)findViewById(R.id.my_plant_sync);
 	    	LinearLayout unsync_layout = (LinearLayout)findViewById(R.id.my_plant_unsync);
 	    	
 	    	sync_layout.setVisibility(View.GONE);
 	    	unsync_layout.setVisibility(View.VISIBLE);
 	    	
 	    	ImageButton unsync_Btn = (ImageButton) findViewById(R.id.unsync_my_plant);
 	    	unsync_Btn.setOnClickListener(new View.OnClickListener() {
 				
 				@Override
 				public void onClick(View v) {
 					// TODO Auto-generated method stub
 					Intent intent = new Intent(PBBMainPage.this, PBBPlantList.class);
 					startActivity(intent);
 				}
 			});
 	    }
 	    
 	    // myPlant button
 	    myPlantBtn = (ImageButton)findViewById(R.id.my_plant);
 	    myPlantBtn.setOnClickListener(new View.OnClickListener(){
 
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				Intent intent = new Intent(PBBMainPage.this, PBBPlantList.class);
 				startActivity(intent);
 			}
 	    });
 	    
 	    // local list button
 	    myResultBtn = (Button) findViewById(R.id.myresults);
 	    myResultBtn.setOnClickListener(new View.OnClickListener(){
 
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				//Toast.makeText(MainPage.this, getString(R.string.Alert_comingSoon), Toast.LENGTH_SHORT).show();
 				startActivity(new Intent(PBBMainPage.this, ListMain.class));
 			}
 	    });
 	    
 	    // plant maps button
 	    mapBtn = (Button)findViewById(R.id.map);
 	    mapBtn.setOnClickListener(new View.OnClickListener(){
 
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				//Toast.makeText(MainPage.this, getString(R.string.Alert_comingSoon), Toast.LENGTH_SHORT).show();
 				Intent intent = new Intent(PBBMainPage.this, MapViewMain.class);
 				intent.putExtra("type", 100);
 				startActivity(intent);
 			}
 	    	
 	    });
 	    
 	    // plant news button
 	    newsBtn = (Button)findViewById(R.id.news);
 	    newsBtn.setOnClickListener(new View.OnClickListener(){
 
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				Toast.makeText(PBBMainPage.this, getString(R.string.Alert_comingSoon), Toast.LENGTH_SHORT).show();
 			}
 	    	
 	    });
 	    
 	    // weekly plant button
 	    weeklyBtn = (Button)findViewById(R.id.weekly);
 	    weeklyBtn.setOnClickListener(new View.OnClickListener(){
 
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				//Toast.makeText(MainPage.this, getString(R.string.Alert_comingSoon), Toast.LENGTH_SHORT).show();
 				Intent intent = new Intent(PBBMainPage.this, WeeklyPlant.class);
 				startActivity(intent);
 
 			}
 	    	
 	    });
 	    
 	    // floracaching button
 	    floraBtn = (Button)findViewById(R.id.flora);
 	    floraBtn.setOnClickListener(new View.OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				// Toast.makeText(PBBMainPage.this, getString(R.string.Alert_comingSoon), Toast.LENGTH_SHORT).show();
 				
 				
 				HelperSharedPreference hPref = new HelperSharedPreference(PBBMainPage.this);
 				if(hPref.getPreferenceBoolean("floracache")) {
 					Intent intent = new Intent(PBBMainPage.this, FloraCacheMain.class);
 					startActivity(intent);
 				}
 				else {
 					Toast.makeText(PBBMainPage.this, getString(R.string.go_to_settings_page), Toast.LENGTH_SHORT).show();
 				}
 				
 			}
 		});
 	    
 	    mUserInfo = (TextView) findViewById(R.id.user_info);
 	    mUserInfo.setText("Hi, " + mUsername);
 	    
 	    // TODO Auto-generated method stub
 	}
 	
     // or when user press back button
 	// when you hold the button for 3 sec, the app will be exited
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 		if(keyCode == event.KEYCODE_BACK) {
 			boolean flag = false;
 			if(event.getRepeatCount() == 3) {
 
 				/*
 				 * Stop the service if it is still working
 				 */
 				Intent service = new Intent(PBBMainPage.this, HelperBackgroundService.class);
 			    stopService(service);
 			    
 				finish();
 				return true;
 			}
 			else if(event.getRepeatCount() == 0 && flag == false){
 				Toast.makeText(PBBMainPage.this, getString(R.string.Alert_holdBackExit), Toast.LENGTH_SHORT).show();
 				flag = true;
 			}
 		}
 		
 		return false;
 	}
 	
 	public int checkSync() {
 		SyncDBHelper syncDB = new SyncDBHelper(PBBMainPage.this);
 		OneTimeDBHelper onetime = new OneTimeDBHelper(PBBMainPage.this);
 		
 		SQLiteDatabase ot = onetime.getReadableDatabase();
 		SQLiteDatabase sync = syncDB.getReadableDatabase();
 		
 		int synced = SyncDBHelper.SYNCED_YES;
 		
 		Cursor syncCheck = ot.rawQuery("SELECT synced FROM oneTimeObservation", null);
 		while(syncCheck.moveToNext()) {
 			if(syncCheck.getInt(0) == SyncDBHelper.SYNCED_NO) {
 				synced = SyncDBHelper.SYNCED_NO;
 			}
 		}
 		syncCheck.close();
 		
 		syncCheck = ot.rawQuery("SELECT synced FROM oneTimePlant", null);
 		while(syncCheck.moveToNext()) {
 			if(syncCheck.getInt(0) == SyncDBHelper.SYNCED_NO) {
 				synced = SyncDBHelper.SYNCED_NO;
 			}
 		}
 		syncCheck.close();
 	
 		// check if there is any unsynced data from my_observation and onetimeob tables.
 		syncCheck = sync.rawQuery("SELECT synced FROM my_observation", null);
 		while(syncCheck.moveToNext()) {
 			if(syncCheck.getInt(0) == SyncDBHelper.SYNCED_NO) {
 				synced = SyncDBHelper.SYNCED_NO;
 			}
 		}
 		syncCheck.close();
 		
 		sync.close();
 		ot.close();
 		
 		return synced;
 	}
 	
 	/*
 	 * Menu option(non-Javadoc)
 	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
 	 */
 	public boolean onCreateOptionsMenu(Menu menu){
 		super.onCreateOptionsMenu(menu);
 		
 		menu.add(0, 1, 0, getString(R.string.Menu_help)).setIcon(android.R.drawable.ic_menu_help);
 		menu.add(0, 2, 0, getString(R.string.Menu_sync)).setIcon(android.R.drawable.ic_menu_rotate);
 		menu.add(0, 3, 0, getString(R.string.Menu_about)).setIcon(android.R.drawable.ic_menu_info_details);
 		//menu.add(0, 4, 0, getString(R.string.Menu_logout)).setIcon(android.R.drawable.ic_menu_close_clear_cancel);
 		menu.add(0, 4, 0, getString(R.string.Menu_settings)).setIcon(android.R.drawable.ic_menu_preferences);
 			
 		return true;
 	}
 	
 	/*
 	 * Menu option selection handling(non-Javadoc)
 	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
 	 */
 	public boolean onOptionsItemSelected(MenuItem item){
 
 		Intent intent;
 		switch(item.getItemId()){
 			case 1:
 				intent = new Intent(PBBMainPage.this, PBBHelpPage.class);
 				intent.putExtra("from", HelperValues.FROM_MAIN_PAGE);
 				startActivity(intent);
 				return true;
 			case 2:
 				intent = new Intent(PBBMainPage.this, PBBSync.class);
 				intent.putExtra("sync_instantly", true);
 				intent.putExtra("from", HelperValues.FROM_MAIN_PAGE);
 				startActivity(intent);
 				finish();
 				return true;				
 			case 3:
 				intent = new Intent(PBBMainPage.this, PBBHelpPage.class);
 				intent.putExtra("from", HelperValues.FROM_ABOUT);
 				startActivity(intent);
 				return true;
 			case 4:
 				intent = new Intent(PBBMainPage.this, HelperSettings.class);
 				intent.putExtra("from", HelperValues.FROM_MAIN_PAGE);
 				startActivity(intent);
 				finish();
 				return true;
 		}
 		return false;
 	}
 }
 
