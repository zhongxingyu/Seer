 package com.praszapps.owetracker.ui.activity;
 
 import java.lang.reflect.Field;
 
 import android.content.Intent;
 import android.content.pm.ActivityInfo;
 import android.os.Bundle;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentTransaction;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewConfiguration;
 
 import com.praszapps.owetracker.R;
 import com.praszapps.owetracker.ui.fragment.DueFragment;
 import com.praszapps.owetracker.ui.fragment.OweboardFragment;
 import com.praszapps.owetracker.ui.fragment.OweboardFragment.OnFriendNameClickListener;
 import com.praszapps.owetracker.util.Constants;
 
 
 /**
  * 
  * This activity is the container of the fragments that will display
  * the friends and dues that the app shows
  * @author Prasannajeet Pani
  * @version 1.0
  *
  */
 public class MainActivity extends RootActivity implements OnFriendNameClickListener{
 
 	public static boolean isSinglePane;
 	private DueFragment dueFrag = null;	
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		// Set the activity container
 		setContentView(R.layout.activity_main);
 		View v = findViewById(R.id.fragment_container);
 
 		// ActionBar overflow will be visible even for devices with physical menu buttons
 		try {
 			ViewConfiguration config = ViewConfiguration.get(this);
 			Field menuKeyField = ViewConfiguration.class
 					.getDeclaredField("sHasPermanentMenuKey");
 			if (menuKeyField != null) {
 				menuKeyField.setAccessible(true);
 				menuKeyField.setBoolean(config, false);
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		
 		if(v == null) {
 			//Running on tablet
 			isSinglePane = false;
 			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			if(dueFrag == null) {
				dueFrag = (DueFragment)getSupportFragmentManager().findFragmentById(R.id.due_frag);
			}
			
 		} else {
 			//Its running on phone
 			isSinglePane = true;
 			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
 			if (savedInstanceState == null) {
 				FragmentManager fm = getSupportFragmentManager();
 				FragmentTransaction ft = fm.beginTransaction();
 				OweboardFragment f1 = new OweboardFragment();
 				ft.add(R.id.fragment_container, f1);
 				ft.commit();
 			}
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		super.onCreateOptionsMenu(menu);
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.activity_menu, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		if(item.getItemId() == R.id.item_about) {
 			// Go to About app screen
 			startActivity(new Intent(this, AboutActivity.class));
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 	@Override
 	public void OnFriendNameClick(String friendId, String currency) {
 		
 		if(isSinglePane == true){
 			/*
 			 * The second fragment not yet loaded. 
 			 * Load FriendDueFragment by FragmentTransaction, and pass 
 			 * data from current fragment to second fragment via bundle.
 			 */
 			FragmentManager fm = getSupportFragmentManager();
 			FragmentTransaction ft = fm.beginTransaction();
 			DueFragment fragTwo = new DueFragment();
 			Bundle args = new Bundle();
 			args.putString(Constants.BUNDLE_EXTRA_FRIENDID, friendId);
 			args.putString(Constants.BUNDLE_EXTRA_CURRENCY, currency);
 			fragTwo.setArguments(args);
 			ft.addToBackStack(null);
 			ft.replace(R.id.fragment_container, fragTwo);
 			ft.commit();
 			
 		} else {
 			/*
 			 * Activity have two fragments. Pass data between fragments
 			 * via reference to fragment
 			 */
 			//get reference to FriendDueFragment
 			getDueFrag().showDetails(friendId, currency);
 		}
 	
 	}
 
 	/**
 	 * @return the dueFrag
 	 */
 	public DueFragment getDueFrag() {
 		return dueFrag;
 	}
 		
 }
