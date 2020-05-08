 package se.chalmers.h_sektionen;
 
 import se.chalmers.h_sektionen.adapters.MenuArrayAdapter;
 import se.chalmers.h_sektionen.utils.MenuItems;
 import se.chalmers.h_sektionen.utils.MenuModel;
 import android.annotation.TargetApi;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Color;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.Build;
 import android.os.Bundle;
 import android.support.v4.widget.DrawerLayout;
 import android.support.v7.app.ActionBar;
 import android.support.v7.app.ActionBarActivity;
 import android.view.Gravity;
 import android.view.LayoutInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.animation.AnimationUtils;
 import android.widget.AdapterView;
 import android.widget.FrameLayout;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.TextView;
 
 import com.parse.Parse;
 import com.parse.ParseAnalytics;
 import com.parse.ParseInstallation;
 import com.parse.PushService;
 
 /**
  * The base activity which every activity extends to get the same
  * action bar and menu drawer.
  */
 public class BaseActivity extends ActionBarActivity {
 
     private static DrawerLayout mDrawerLayout;
     private static ListView mDrawerList;
     private static FrameLayout frameLayout;
     private static int currentView;
     
     /**
      * Creates the view for the main activity, the action bar
      * and the menu drawer.
      */
     @Override
     protected void onCreate(Bundle savedInstance) {
     	super.onCreate(savedInstance);
     	
     	setContentView(R.layout.activity_main);        
         setupActionBar();
         
         MenuModel.LoadModel(this);
         
         mDrawerList = (ListView) findViewById(R.id.left_drawer);
         String[] ids = new String[MenuModel.Items.size()];
         for (int i= 0; i < ids.length; i++){
 
             ids[i] = Integer.toString(i+1);
         }
         MenuArrayAdapter adapter = new MenuArrayAdapter(this,R.layout.row_menu, ids);
        
         mDrawerList.setAdapter(adapter);
         mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
         mDrawerList.setCacheColorHint(Color.BLACK);
         frameLayout = (FrameLayout) findViewById(R.id.content_frame);
         
         // Set the list's click listener
         mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
 		
 		//Parse.com Add your Parse API keys
 		Parse.initialize(this, "f4nb9heDlUu0uBmPiOJlYCXxlNnHftMkoBRkurLN", 
 				"y7raMOFCv6mkDLm953GFBuRI6P3XAzDYtvbgzmm4");
 		
 		//Parse.com inform the Parse Push Service that it is ready for notifications.
 		PushService.setDefaultPushCallback(this, MainActivity.class);
 		ParseInstallation.getCurrentInstallation().saveInBackground();
 		
 		//Parse.com track statistics around application opens
 		ParseAnalytics.trackAppOpened(getIntent());
     }
     
     /**
      * Sets up a listener for the drawer menu.
      */
     private class DrawerItemClickListener implements ListView.OnItemClickListener {
         /**
          * Checks which icon was pressed in the drawer menu.
          */
     	@Override
         public void onItemClick(AdapterView parent, View view, int position, long id) {
         	mDrawerLayout.closeDrawers();
         	if(currentView != position){
         		switch (position) {
 		        	case MenuItems.NEWS:
 		        		startActivityByClass(MainActivity.class);
 		        		break;
 		        	case MenuItems.LUNCH:
 		        		startActivityByClass(LunchActivity.class);
 		        		break;
 		        	case MenuItems.PUB:
 		        		startActivityByClass(PubActivity.class);
 		        		break;
 		        	case MenuItems.INFO:
 		        		startActivityByClass(InfoActivity.class);
 		        		break;
 		        	case MenuItems.EVENTS:
 		        		startActivityByClass(EventsActivity.class);
 		        		break;
 		        	case MenuItems.SUGGEST:
 		        		startActivityByClass(SuggestActivity.class);
 		        		break;
 		        	case MenuItems.FAULTREPORT:
 		        		startActivityByClass(FaultreportActivity.class);
 		        		break;
 		        	case MenuItems.ABOUT:
 		        		startActivityByClass(AboutActivity.class);
 		        		break;
 		        	default:
 		        		return;
 		        }
         	}
         }
     }
     
     /**
      * Help method to start an activity.
      * @param Which activity to start.
      */
     private void startActivityByClass(Class<? extends BaseActivity> c) { 
     	Intent i =  new Intent(this, c);
     	i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
     	i.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
     	startActivity(i);
     }
     
     /**
      * Sets up the custom action bar.
      */
     private void setupActionBar() {
     	ActionBar ab = getSupportActionBar();
     	ab.setDisplayShowCustomEnabled(true);
     	ab.setDisplayShowTitleEnabled(false);
     	ab.setIcon(R.drawable.ic_action_overflow);
     	
     	LayoutInflater inflator = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
     	View v = inflator.inflate(R.layout.action_bar_title, null);
     	
     	ab.setCustomView(v);
     	
     	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
     		enableHomeButton();
     	} else {
     		ab.setHomeButtonEnabled(true);
     	}
     }
     
     /**
      * Enables the home button if the user has an newer device.
      */
     @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
     private void enableHomeButton() {
     	getActionBar().setHomeButtonEnabled(true);
     }
 	
     /**
      * Enables the home icon to open the sliding drawer menu.
      */
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		
 		 switch(item.getItemId()) {		 	
 		 	case android.R.id.home:
 		 		toggleMenu();
 		 		return true;
 		 	default:
 		 		return super.onOptionsItemSelected(item);
 		 }
 	}
 	
 	/**
 	 * Toggles the sliding drawer menu to open or close.
 	 */
 	private void toggleMenu() {
 		if(mDrawerLayout.isDrawerOpen(Gravity.LEFT))
 			mDrawerLayout.closeDrawer(Gravity.LEFT);
 		else
 			mDrawerLayout.openDrawer(Gravity.LEFT);
 	}
 	
 	/**
 	 * @return The frame layout.
 	 */
 	protected FrameLayout getFrameLayout() {
 		return frameLayout;
 	}
 	
 	/**
 	 * Sets the current activity view.
 	 * @param The current view active.
 	 */
 	static protected void setCurrentView(int currentView) {
 		BaseActivity.currentView = currentView;
 	}
 	
 	/**
 	 * Runs a loading animation image.
 	 */
 	protected void runLoadAnimation() {
 		getFrameLayout().removeAllViews();
 		getFrameLayout().addView(getLayoutInflater().inflate(R.layout.view_loading, null));
 		
 		((ImageView)findViewById(R.id.load_image)).startAnimation(AnimationUtils.loadAnimation(this, R.anim.rotate_centre));
 	}
 	
 	/**
 	 * Runs a transparent loading animation image.
 	 */
 	protected void runTransparentLoadAnimation() {
 		getFrameLayout().addView(getLayoutInflater().inflate(R.layout.view_loading, null));
 		
 		((ImageView)findViewById(R.id.load_image)).startAnimation(AnimationUtils.loadAnimation(this, R.anim.rotate_centre));
 	}
 	
 	/**
 	 * Stops the loading animation image.
 	 */
 	protected void stopAnimation(){
 		ImageView img = ((ImageView)findViewById(R.id.load_image));
 		TextView txt = (TextView) findViewById(R.id.load_label);
 		
 		View view = getLayoutInflater().inflate(R.layout.view_loading, null);
 		getFrameLayout().removeView(view);
 		
 		img.clearAnimation();
 		img.setVisibility(View.GONE);
 		txt.setVisibility(View.GONE);
 	}
 	
 	/**
 	 * Starts an error view with a supplied message.
 	 * @param What error message to be displayed.
 	 */
 	protected void setErrorView(String message) {
 		View errorView = getLayoutInflater().inflate(R.layout.view_error, null);
 		TextView errorTextView = (TextView) errorView.findViewById(R.id.error_text_view);
 		errorTextView.setText(message);
 		
 		getFrameLayout().removeAllViews();
 		getFrameLayout().addView(errorView);
 	}
 	
 	/**
 	 * Returns to the main activity if in another activity
 	 * otherwise returns the user to the home screen of their device.
 	 */
 	@Override
 	public void onBackPressed() {
 		if(currentView != MenuItems.NEWS)
 			startActivityByClass(MainActivity.class);
 		else{
 		    Intent i = new Intent(Intent.ACTION_MAIN);
 		    i.addCategory(Intent.CATEGORY_HOME);
 		    startActivity(i);
 		}
		//super.onBackPressed();
 	}
 	
 	/**
 	 * Checks if device is connected to Internet.
 	 * @return True if connected, otherwise false
 	 */
 	protected boolean connectedToInternet(){
 		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
 		NetworkInfo ni = cm.getActiveNetworkInfo();
 		
 		return (ni != null && ni.isConnected()) ? true : false;
 	}
 
 }
