 package org.fruct.oss.tourme;
 
 import android.annotation.TargetApi;
 import android.app.ActionBar;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Build;
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.view.ViewPager.LayoutParams;
 import android.view.GestureDetector;
 import android.view.GestureDetector.SimpleOnGestureListener;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.View;
import android.view.ViewGroup;
 import android.view.animation.AnimationUtils;
 import android.widget.ArrayAdapter;
 import android.widget.ImageSwitcher;
 import android.widget.ImageView;
 import android.widget.Toast;
 import android.widget.ViewSwitcher.ViewFactory;
 
 public class MainActivity extends FragmentActivity implements
 		ActionBar.OnNavigationListener, ViewFactory {
 
 	/**
 	 * The serialization (saved instance state) Bundle key representing the
 	 * current dropdown position.
 	 */
 	private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";
 	
 	ImageSwitcher slideshow;
 	private int slideshowIndex = 0; // ImageSwitcher current image Id from this array:
 	// FIXME: you should always have array with drawable's Ids
 	int[] images = {R.drawable.ic_action_menu_star, R.drawable.ic_launcher, R.drawable.ic_action_menu_map}; // FIXME: add images dynamically
 	
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 
 		// Set up the action bar to show a dropdown list.
 		final ActionBar actionBar = getActionBar();
 		actionBar.setDisplayShowTitleEnabled(false);
 		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
 
 		// Set up the dropdown list navigation in the action bar.
 		actionBar.setListNavigationCallbacks(
 		// Specify a у SpinnerAdapter to populate the dropdown list.
 				new ArrayAdapter<String>(getActionBarThemedContextCompat(),
 						android.R.layout.simple_list_item_1,
 						android.R.id.text1, new String[] {
 								getString(R.string.actionbar_main),
 								getString(R.string.actionbar_map),
 								getString(R.string.actionbar_nearby),
 								getString(R.string.actionbar_favour),
 								getString(R.string.actionbar_log) }), this);
 	
 		
 		// TODO: just for tests, remove this
 		// An example to open ArticleActivity for an article
 		// Use http:// for web source or file:// for local file
 		//Intent myInt = new Intent(this, ArticleActivity.class);
 		//myInt.putExtra(ConstantsAndTools.ARTICLE_ID, "http://wikipedia.org");
 		//startActivity(myInt);
 		
 		// Slideshow		
 		slideshow = (ImageSwitcher) findViewById(R.id.ImageSwitcher01);
 		slideshow.setFactory(this);
         slideshow.setInAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_in));
         slideshow.setOutAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_out));
         slideshow.setImageDrawable(getResources().getDrawable(images[slideshowIndex]));
         
         // Change image by timeout
         slideshow.postDelayed(new Runnable() {
             //int i = 0;
             public void run() {
             	if (slideshowIndex == images.length-1)
             		slideshowIndex = 0;            	
             	else
             		slideshowIndex++;
             	
             	slideshow.setImageDrawable(getResources().getDrawable(images[slideshowIndex]));
             	slideshow.postDelayed(this, 10000); // Slide after 10 secs
             }
         }, 10000); // Start first slide after 10 secs
 
         
         // Set onFling listener (fling\swipe from right to left and vice versa)
         final GestureDetector gdt = new GestureDetector(new GestureListener());
         slideshow.setOnTouchListener(new View.OnTouchListener() {
 			@Override
 			public boolean onTouch(View v, MotionEvent event) {
 				gdt.onTouchEvent(event);
 				return true;
 			}			
 		});
 	}
 
     private class GestureListener extends SimpleOnGestureListener {
     	private static final int SWIPE_MIN_DISTANCE = 120;
         private static final int SWIPE_THRESHOLD_VELOCITY = 200;
     	
     	@Override
         public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
             if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
             	// Swipe right to left
             	slideshowIndex = slideshowIndex < 2 ? ++slideshowIndex : slideshowIndex;
             	//slideshow.setImageDrawable(getResources().getDrawable(getResources().getIdentifier(images[slideshowIndex], null, getPackageName())));
             	slideshow.setImageDrawable(getResources().getDrawable(images[slideshowIndex]));
             	return false; 
             }  else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                 // Swipe left to right
             	slideshowIndex = slideshowIndex > 0 ? --slideshowIndex : slideshowIndex;
             	slideshow.setImageDrawable(getResources().getDrawable(images[slideshowIndex]));
             	return false;
             }
 
            // Swiping top-bottom
            /*if(e1.getY() - e2.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
                 // Swipe from bottom to top
            	// Hide buttons
            	ViewGroup buttons = (ViewGroup) getLayoutInflater().inflate(R.id.menu_list_layout, null);
            	buttons.setVisibility(View.VISIBLE);            	
             	return false;
             }  else if (e2.getY() - e1.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
                 // Swipe from top to bottom
            	// Show buttons
            	ViewGroup buttons = (ViewGroup) getLayoutInflater().inflate(R.id.menu_list_layout, null);
            	buttons.setVisibility(View.GONE);
             	return false;
            }*/
             return false;
         }
     }
 
 	/**
 	 * Backward-compatible version of {@link ActionBar#getThemedContext()} that
 	 * simply returns the {@link android.app.Activity} if
 	 * <code>getThemedContext</code> is unavailable.
 	 */
 	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
 	private Context getActionBarThemedContextCompat() {
 		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
 			return getActionBar().getThemedContext();
 		} else {
 			return this;
 		}
 	}
 
 	@Override
 	public void onRestoreInstanceState(Bundle savedInstanceState) {
 		// Restore the previously serialized current dropdown position.
 		if (savedInstanceState.containsKey(STATE_SELECTED_NAVIGATION_ITEM)) {
 			getActionBar().setSelectedNavigationItem(
 					savedInstanceState.getInt(STATE_SELECTED_NAVIGATION_ITEM));
 		}
 	}
 
 	@Override
 	public void onSaveInstanceState(Bundle outState) {
 		// Serialize the current dropdown position.
 		outState.putInt(STATE_SELECTED_NAVIGATION_ITEM, getActionBar()
 				.getSelectedNavigationIndex());
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_main, menu);
 		
 		return true;
 	}
 
 	@Override
 	public boolean onNavigationItemSelected(int position, long id) {
 		Intent intent = null;
     	
 		// I know, it's kinda bicycle, but I dunno how to do better
 		switch(position) {
 			// Goto Main
 			case(0):
 				//intent = new Intent (this, MainActivity.class);
 				break;
 			// Goto Map
 			case(1):
 				intent = new Intent (this, MapActivity.class);
 				break;
 			// Goto Nearby
 			case(2):
 				intent = new Intent (this, NearbyActivity.class);
 				break;
 			// Goto Favourites
 			case(3):
 				intent = new Intent (this, FavourActivity.class);
 				break;
 			// Goto Travel Log
 			case(4):
 				intent = new Intent (this, TravellogActivity.class);
 				break;
 			default:
 				Toast.makeText(getApplicationContext(), "Test", Toast.LENGTH_LONG).show();
 				break;
 		}
 		    	
     	if (intent != null)
     		startActivity(intent);    
 		
 		return true;
 	}
 	
 	public boolean onOptionsItemSelected(MenuItem item) {
 		Intent intent = null;
 		
 		switch(item.getItemId()) {
 			case(R.id.menu_map):
 				intent = new Intent(getApplicationContext(), MapActivity.class);
 				break;
 			case(R.id.menu_favourites):
 				intent = new Intent(getApplicationContext(), FavourActivity.class);
 				break;
 			case(R.id.menu_settings):
 				//intent = new Intent(getApplicationContext(), SettingsActivity.class); // TODO
 				break;
 			case(R.id.menu_onoff_online_mode):
 				// Turn on\off online mode (save to shared preferences)
 				SharedPreferences settings = getSharedPreferences(ConstantsAndTools.SHARED_PREFERENCES, 0);
 				SharedPreferences.Editor editor = settings.edit();
 				
 				// Check for current state and update
 				if (settings.getBoolean("ONLINE_MODE", false) == true)		
 					editor.putBoolean(ConstantsAndTools.ONLINE_MODE, false);
 				else
 					editor.putBoolean(ConstantsAndTools.ONLINE_MODE, true);
 				
 				editor.commit();
 				break;
 			default:
 				break;
 		}
 		
 		if (intent != null)
 			startActivity(intent);
 		
 		return true;
 		
 	}
 
 	@Override
 	public View makeView() {
 		// TODO Auto-generated method stub
 		ImageView iView = new ImageView(this);
         iView.setScaleType(ImageView.ScaleType.FIT_CENTER);
         iView.setLayoutParams(new 
                 ImageSwitcher.LayoutParams(
                         LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT));
         iView.setBackgroundColor(0xFF000000);
         return iView;
 	}
 
 }
