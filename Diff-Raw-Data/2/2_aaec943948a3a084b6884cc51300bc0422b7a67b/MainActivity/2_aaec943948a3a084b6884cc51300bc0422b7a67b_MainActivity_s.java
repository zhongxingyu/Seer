 package au.id.teda.broadband.usage.ui;
 
 import com.actionbarsherlock.app.ActionBar;
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuItem;
 
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.res.Configuration;
 import android.os.Bundle;
 import android.util.DisplayMetrics;
 import android.util.Log;
 import android.view.Display;
 import android.view.LayoutInflater;
 import android.view.animation.Animation;
 import android.view.animation.AnimationUtils;
 import android.widget.ImageView;
 import android.widget.Toast;
 import au.id.teda.broadband.usage.R;
 import au.id.teda.broadband.usage.authenticator.AccountAuthenticator;
 import au.id.teda.broadband.usage.authenticator.AuthenticatorActivity;
 import au.id.teda.broadband.usage.helper.ConnectivityHelper;
 
 public class MainActivity extends SherlockFragmentActivity {
 	
 	public static final String DEBUG_TAG = "bbusage";
 	
     /** Refresh icon reference object **/
     private static MenuItem mRefreshMenuItem;
     
     private static boolean refreshing;
     
     private static final String STATE_REFRESHING = "refresh";
 	
 	private AccountAuthenticator mAccountAuthenticator;
 	
 	private SyncReceiver mSyncReceiver;
     private IntentFilter filter;
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
        	setContentView(R.layout.activity_main);
         
         // Set up the action bar.
         final ActionBar mActionBar = getSupportActionBar();
 
         String BROADCAST = getString(R.string.sync_broadcast_action);
         filter = new IntentFilter(BROADCAST);
         mSyncReceiver = new SyncReceiver();
         
         if( savedInstanceState != null ) {
         	refreshing = savedInstanceState.getBoolean(STATE_REFRESHING);
         	savedInstanceState.clear();
         }
         
        logScreenSpecs();
         
     }
     
     @Override
     protected void onSaveInstanceState(Bundle outState) {
         outState.putBoolean(STATE_REFRESHING, refreshing);
         super.onSaveInstanceState(outState);
     }
     
     @Override
     protected void onPause() {
         // TODO Auto-generated method stub
         super.onPause();
         unregisterReceiver(mSyncReceiver);
      }
     
     protected void onResume(){
     	super.onResume();
     	
     	registerReceiver(mSyncReceiver, filter);
         
         // Check to see if account has been authenticated
         mAccountAuthenticator = new AccountAuthenticator(this);
         if(!mAccountAuthenticator.isAccountAuthenticated()){
         	Intent authenticator = new Intent(this, AuthenticatorActivity.class);
     		startActivity(authenticator);
         }
 
     }
     
     private void noConnectivityToast(){
     	Toast toast = Toast.makeText(this, "No connectivity", Toast.LENGTH_LONG);
 		toast.show();
     }
     
     // Create options menu
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getSupportMenuInflater().inflate(R.menu.options_menu, menu);
         // Set object reference for refresh item
         mRefreshMenuItem = menu.findItem(R.id.menu_refresh);
         if (refreshing){
         	startAnimateRefreshIcon();
         }
         return true;
     }
     
     // Handle options menu clicks
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
         case R.id.menu_settings:
                 Intent settingsActivityIntent = new Intent(getBaseContext(), SettingsActivity.class);
                 startActivity(settingsActivityIntent);
                 return true;
         case R.id.menu_refresh:
         	
         		ConnectivityHelper mNetwork = new ConnectivityHelper(this);
         		if(mNetwork.isConnected()){
         			mNetwork.requestSync();
         		} else {
         			noConnectivityToast();
         		}
         	
         		return true;
         default:
                 return super.onOptionsItemSelected(item);
         }
     }
     
     /**
      * Start the animation of the refresh icon in the action bar
      */
 	public void startAnimateRefreshIcon() {
 		if (mRefreshMenuItem != null){
 			// Attach a rotating ImageView to the refresh item as an ActionView
 			LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 			ImageView iv = (ImageView) inflater.inflate(R.layout.refresh_action_view, null);
 	
 			// Set animation
 			Animation rotation = AnimationUtils.loadAnimation(this, R.anim.clockwise_refresh);
 			rotation.setRepeatCount(Animation.INFINITE);
 			iv.startAnimation(rotation);
 			iv.setDrawingCacheEnabled(true);
 			iv.buildDrawingCache();
 	
 			// Start animation of image view
 			mRefreshMenuItem.setActionView(iv);
 			
 			refreshing = true;
 		}
 	}
 	
 	/**
 	 * Start stop animation of the refresh icon in the action bar
 	 */
 	public void stopAnimateRefreshIcon() {
 		 // Stop refresh icon animation
 		 if (mRefreshMenuItem != null && mRefreshMenuItem.getActionView() != null){
 			 mRefreshMenuItem.getActionView().clearAnimation();
 			 mRefreshMenuItem.setActionView(null);
 			 
 		 	refreshing = false;
 		 }
 	}
 	
 	public class SyncReceiver extends BroadcastReceiver {
 		
         @Override
         public void onReceive(Context context, Intent i) {
             
             String MESSAGE = getString(R.string.sync_broadcast_message);
             String SYNC_START = getString(R.string.sync_broadcast_start);
             String SYNC_COMPLETE = getString(R.string.sync_broadcast_complete);
             
             String msg = i.getStringExtra(MESSAGE);
             if (msg.equals(SYNC_START)){
         		startAnimateRefreshIcon();
             } else if (msg.equals(SYNC_COMPLETE)){
         		stopAnimateRefreshIcon();
             }
         }
          
     }
 	
 	public int getScreenOrientation() {
 	    Display getOrient = getWindowManager().getDefaultDisplay();
 	    int orientation = Configuration.ORIENTATION_UNDEFINED;
 	    if(getOrient.getWidth()==getOrient.getHeight()){
 	        orientation = Configuration.ORIENTATION_SQUARE;
 	    } else{ 
 	        if(getOrient.getWidth() < getOrient.getHeight()){
 	            orientation = Configuration.ORIENTATION_PORTRAIT;
 	        }else { 
 	             orientation = Configuration.ORIENTATION_LANDSCAPE;
 	        }
 	    }
 	    return orientation;
 	}
 	
 	public boolean isScreenPortrait(){
 		int screen = getScreenOrientation();
 		
 		if (screen == Configuration.ORIENTATION_PORTRAIT){
 			return true;
 		} else {
 			return false;
 		}
 	}
 	
 	private void logScreenSpecs(){
 		Display display = getWindowManager().getDefaultDisplay();
 	    DisplayMetrics outMetrics = new DisplayMetrics ();
 	    display.getMetrics(outMetrics);
 
 	    float density  = getResources().getDisplayMetrics().density;
 	    float dpHeight = outMetrics.heightPixels / density;
 	    float dpWidth  = outMetrics.widthPixels / density;
 	    
 	    Log.d(DEBUG_TAG, "Density:" + density + " | dpHeight:" + dpHeight + " | dpWidth:" + dpWidth);
 	}
 
 
 }
