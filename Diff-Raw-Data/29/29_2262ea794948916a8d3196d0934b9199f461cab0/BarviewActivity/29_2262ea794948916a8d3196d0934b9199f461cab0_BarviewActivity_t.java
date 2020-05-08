 package com.barview;
 
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.Toast;
 import android.widget.AdapterView.OnItemClickListener;
 
 import com.barview.constants.BarviewConstants;
 import com.barview.mobile.BarviewMobileUser;
 import com.barview.mobile.BarviewMobileUtility;
 import com.barview.utilities.FacebookUtility;
 import com.facebook.android.DialogError;
 import com.facebook.android.Facebook;
 import com.facebook.android.FacebookError;
 import com.facebook.android.Facebook.DialogListener;
 
 public class BarviewActivity extends Activity {
 	
 	private final String ACTION_BARLOOKUP 	= "Look up bars";
 	private final String ACTION_CURRLOC		= "Current location";
 	private final String ACTION_FAVORITES	= "Favorite bars";
 	private final String ACTION_DEALS		= "Deals/Events";
 	private final String ACTION_LOGIN		= "Log in";
 	private final String ACTION_LOGOUT		= "Log out";
 	
 	Facebook fb;		// Singleton, no harm if it's already there.
 	SharedPreferences settings;
 	
 	ListView bvActionsListView;
 	ArrayAdapter<String> adapter;
 	
 	BarviewActivity bvClass = this;
 	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         
         // Grab a reference to the view and set up a click listener
         bvActionsListView = (ListView) findViewById(R.id.list);
         bvActionsListView.setOnItemClickListener(new OnItemClickListener() {
 
 			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 				String action = adapter.getItem(position);
 				Intent intent = null;
 				
 				/*
 				 * Determine which Intent to invoke...
 				 */
 				if(action.equals(ACTION_BARLOOKUP)) {
 					intent = new Intent(bvClass, MapLookupActivity.class);
 				}
 				else if(action.equals(ACTION_CURRLOC)) {
 					intent = new Intent(bvClass, CurrentLocationActivity.class);
 				}
 				else if(action.equals(ACTION_FAVORITES)) {
 					intent = new Intent(bvClass, FavoritesActivity.class);
 				}
 				else if(action.equals(ACTION_LOGIN) || action.equals(ACTION_LOGOUT)) {
 					intent = new Intent(bvClass, FacebookActivity.class);
 				}
 				else if(action.equals(ACTION_DEALS)) {
 					intent = new Intent(bvClass, DealsActivity.class);
 				}
 				
 		    	if(intent != null)
 		    		startActivity(intent);
 			}
         	
         });
         
     }
     
     /**
      * We use this callback to set up a currently-logged-in user if they have closed the application
      * and re-opened it.  At the end we also establish the list items that display.
      * 
      * For Facebook we need to (possibly) re-instantiate the object and populate it with the access token
      * and expires variables that were saved into Preferences.  Then, if the session happens to be invalid
      * then we re-authorize and save the new access token to Preferences.
      * 
      * For Barview we just have to fetch the BarviewUser object (possibly instantiating in the process) 
      * and populate it with all the data stored in Preferences, including the token.
      */
     public void onResume() {
     	super.onResume();
     	
     	/*
          * Determine if a user is already logged in.
          */
     	fb = FacebookUtility.getFacebook();
     	settings = getSharedPreferences(BarviewConstants.PREFS_NAME, Activity.MODE_PRIVATE);
     	
         String type = settings.getString(BarviewConstants.LOGIN_TYPE, "");
         
         // If facebook, just make an attribute call to refresh attributes.
         if(type.equals(BarviewConstants.LOGIN_TYPE_FACEBOOK)) {
         	
         	// Set the access token
         	String accessToken = settings.getString(FacebookUtility.FB_ACCESS_TOKEN, null);
         	long expires = settings.getLong(FacebookUtility.FB_EXPIRES, 0);
         	if(accessToken != null)
         		fb.setAccessToken(accessToken);
         	if(expires != 0)
         		fb.setAccessExpires(expires);
         	
         	
         	String fbId = FacebookUtility.getAttribute(FacebookUtility.FB_ID);
         	Log.i(BarviewActivity.class.getName(), "Refreshed attributes for Facebook user " + fbId);
         }
         // If Barview, populate the barview user object with preferences values.
         else if(type.equals(BarviewConstants.LOGIN_TYPE_BARVIEW)) {
         	BarviewMobileUser user = BarviewMobileUtility.getUser();
     		
             user.setFirstName(settings.getString(BarviewConstants.BARVIEW_FIRST_NAME, null));
             user.setLastName(settings.getString(BarviewConstants.BARVIEW_LAST_NAME, null));
             user.setUserId(settings.getString(BarviewConstants.BARVIEW_EMAIL, null));
             user.setDob(settings.getString(BarviewConstants.BARVIEW_DOB, null));
             user.setCity(settings.getString(BarviewConstants.BARVIEW_CITY, null));
             user.setState(settings.getString(BarviewConstants.BARVIEW_STATE, null));
             user.setToken(settings.getString(BarviewConstants.BARVIEW_TOKEN, null));
         }
         
         setUpList();
     }
     
     // Called every time user clicks on a menu item 
     @Override 
     public boolean onOptionsItemSelected(MenuItem item) {
     	Toast toast = Toast.makeText(this, item.getItemId() + "", Toast.LENGTH_LONG);
 		toast.show();
     	return true;
     }
     
     /**
      * Determine which options for activities will be available to the user.  Logged-in
      * users (through Facebook or Barview) will be able to view their favorite bars.  They
      * will also have a "Log out" action while someone who is not logged in will see "Log in".
      */
     private void setUpList() {
     	ArrayList<String> list = new ArrayList<String>();
     	list.add(ACTION_BARLOOKUP);
     	list.add(ACTION_CURRLOC);
     	
     	
     	if(BarviewMobileUtility.isLoggedIn() || FacebookUtility.isLoggedIn()) {
     		list.add(ACTION_FAVORITES);
     		list.add(ACTION_DEALS);
     		list.add(ACTION_LOGOUT);
     	}
     	else
     		list.add(ACTION_LOGIN);
     	
     	adapter = new ArrayAdapter<String>(this, R.layout.row, list);
     	bvActionsListView.setAdapter(adapter);
     }
 }
