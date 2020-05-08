 /*
  * Copyright (C) 2011 Hemanth Meenakshisundaram
  * 
  * This file is part of OnTimeBirthdayPost.
  * Contact the developers at tsaucestudio@gmail.com
 
  * OnTimeBirthdayPost is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
 
  * OnTimeBirthdayPost is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
 
  * You should have received a copy of the GNU General Public License
  * along with OnTimeBirthdayPost.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.TomatoSauceStudio.OnTimeBirthdayPost;
 
 import java.io.IOException;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.List;
 import java.util.StringTokenizer;
 import java.util.GregorianCalendar;
 import java.util.Locale;
 import java.util.TimeZone;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.json.JSONArray;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.ListActivity;
 import android.app.ProgressDialog;
 import android.content.ActivityNotFoundException;
 import android.content.Context;
 import android.content.Intent;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.view.ContextMenu;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.ViewGroup;
 import android.view.Window;
 import android.widget.CursorAdapter;
 import android.widget.ListView;
 import android.widget.SimpleCursorAdapter;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 import android.widget.TextView;
 import android.content.SharedPreferences;
 import android.content.pm.ActivityInfo;
 import android.content.res.Configuration;
 import android.location.Address;
 import android.location.Geocoder;
 //import android.util.Log;
 
 
 import com.facebook.android.*;
 import com.facebook.android.Facebook.*;
 
 public class OnTimeBirthdayPost extends ListActivity {
     // Define some constants.
     private static final int HIGHLIGHT_COLOR=0xff440044;
     private static final double LONGITUDE_TOLERANCE=140.0;
     private static final int DAYS_PAST=1;
     private static final int DAYS_AHEAD=2;
     private static final int SECS_IN_HOUR=3600;
     
     /**
      * Important: Add your Facebook app secret key here or the app won't be able
      * to talk to FB. I have removed mine when posting code since it is tied to my account.
      */
     private static final String APP_SECRET = "";
 
     Facebook facebook;
     private SharedPreferences mPrefs;
     private BirthdaysDbAdapter mDbHelper;
     private Geocoder geocoder;
     private TextView titleText;
     private ProgressDialog pdialog = null;
     private boolean connectionError = false;
     private boolean authRetry = false;
     long lastRequestTime = 0;
     private AsyncFacebookRunner mAsyncRunner;
     /**
      * The two date formats we shall use, one for display and
      * one for storage/comparison in the SQLite table,
      * SQLite seems to have no date datatype, so we simply store/compare strings.
      */
 	DateFormat df = new SimpleDateFormat("MMM dd, EEEE");
 	DateFormat rawDF = new SimpleDateFormat("MMdd");
 
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         /**
          * Request custom title-bar so we can display our own messages.
          */
         requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
         setContentView(R.layout.main);
         getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.titlebar);
         titleText = (TextView) findViewById(R.id.titlet);
         /**
          * Fix our orientation, the list looks best in Portrait and this way we
          * don't have to deal with orientation changes.
          */
         setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
         
         /**
          * We will use this progress dialog throughout to display busy messages.
          */
         pdialog = new ProgressDialog(this);
         pdialog.setIndeterminate(true);
         /**
          * Init Facebook objects.
          */
         facebook  = new Facebook(APP_SECRET);
         mAsyncRunner = new AsyncFacebookRunner(facebook);
         /**
          * Init DB.
          */
         mDbHelper = new BirthdaysDbAdapter(this);
         mDbHelper.open();
         /**
          * Init the geocoder that will help us map locations to longitude and thus approximate timezone.
          */
         geocoder = new Geocoder(this, Locale.getDefault());
         registerForContextMenu(getListView());
         /**
          * Get existing access_token if any and skip authorization if possible.
          */
         mPrefs = getPreferences(MODE_PRIVATE);
         String access_token = mPrefs.getString("access_token", null);
         long expires = mPrefs.getLong("access_expires", 0);
         if(access_token != null) {
             facebook.setAccessToken(access_token);
         }
         if(expires != 0) {
             facebook.setAccessExpires(expires);
         }
  
         /**
          * Request FB auth again only if current session is invalid, else proceed to
          * request info from FB.
          */
         if (!facebook.isSessionValid())
         {
         	//Log.d("OnTimeBirthdayPost","Facebook session not valid. Redoing auth.");
             fbAuthWrapper();
         } else {
         	//Log.d("OnTimeBirthdayPost","Facebook session valid. Proceeding to requests");
         	makeFBRequests();
         }
     }
     
     @Override
     public void onPause() {
     	super.onPause();
         /**
          * Just dismiss any dialogs and set to NULL so async tasks don't try to use it.
          */
     	if (pdialog != null) {
     	    pdialog.dismiss();
     	}
     	pdialog = null;
     	//Log.d("OnTimeBirthdayPost","Pausing");
     }
     
     @Override
     public void onRestart() {
     	super.onRestart();
         /**
          * If it has been an hour since last start, fetch FB data again.
          */
     	long currentTime = (new GregorianCalendar()).getTime().getTime();
     	if (currentTime - lastRequestTime > SECS_IN_HOUR * 1000) {
     		makeFBRequests();
     	}
     }
     @Override
     public void onDestroy() {
     	super.onDestroy();
         /**
          * Close Db to avoid leaks.
          */
     	mDbHelper.close();
     	//Log.d("OnTimeBirthdayPost","Stopping");
     }
     
     @Override
     public void onConfigurationChanged(Configuration newConfig) {
         super.onConfigurationChanged(newConfig);
         /**
          * Ignore orientation / keyboard changes for now, if we don't
          * override here, the activity will be recreated. We don't want that.
          */
     }
 
     /**
      * Just a wrapper for the Fb auth call.
      */
     private void fbAuthWrapper() {
     	facebook.authorize(this, new String[] {"user_about_me", "friends_about_me",
                 "friends_birthday", "friends_location"}, new MyFBDialogListener());
     }
     
     public class MyFBDialogListener implements DialogListener {
 		@Override
 		public void onComplete(Bundle values) {
             /**
              * When auth completes, cache token so we can avoid re-auth for future runs
              * when possible; then make FB information requests.
              */
 			SharedPreferences.Editor editor = mPrefs.edit();
             editor.putString("access_token", facebook.getAccessToken());
             editor.putLong("access_expires", facebook.getAccessExpires());
             editor.commit();
 			makeFBRequests();
 		}
 
 		@Override
 		public void onFacebookError(FacebookError e) {
 			fbErrorHandle("Facebook connection or authorization error!");
 		}
 
 		@Override
 		public void onError(DialogError e) {
 			connectionError = true;
 			fbErrorHandle("Facebook connection or authorization error!");
 		}
 
 		@Override
 		public void onCancel() {
 			fbErrorHandle("Facebook authorization cancelled!");
 		}
     }
     
     private void makeFBRequests() {
         /**
          * Store request time, request name of user and name, birthday, location of friends.
          */
     	lastRequestTime = (new GregorianCalendar()).getTime().getTime();
     	Bundle fbParams1 = new Bundle();
     	fbParams1.putString("fields","name");
     	mAsyncRunner.request("me", fbParams1, new AboutRequestListener());
     	Bundle fbParams2 = new Bundle();
         fbParams2.putString("fields","name,birthday,location");
         mAsyncRunner.request("me/friends", fbParams2, new FriendsRequestListener());
     }
 
     private int fillData() {
         /**
          * Fetch recent and upcoming birthdays from DB: We use comparison of the
          * MMdd date strings as the basis.
          */
         Calendar cal = new GregorianCalendar();
         cal.add(Calendar.DATE, -DAYS_PAST);
         String startDate = rawDF.format(cal.getTime());
         //Log.d("OnTimeBirthdayPost","Start Date Req: " + startDate);
         cal.add(Calendar.DATE, DAYS_PAST+DAYS_AHEAD);
         String endDate = rawDF.format(cal.getTime());
         //Log.d("OnTimeBirthdayPost","End Date Req: " + endDate);
         Cursor birthdaysCursor = null;
         /**
          * If database is empty or we can't access it for some reason, return err;
          * if our query returns no results return 0, else return success (1).
          */
         try {
         	if (mDbHelper.getRowCount() <= 0) {
         		return -1;
         	}
             birthdaysCursor = mDbHelper.fetchBirthdays(startDate,endDate);
         } catch (Exception e) {
         	return -1;
         }
         if (birthdaysCursor.getCount() == 0)
         {
         	return 0;
         }
         startManagingCursor(birthdaysCursor);
 
         /**
          * Create an array to specify the fields we want to display in the list.
          */
         String[] from = new String[]{BirthdaysDbAdapter.KEY_NAME,
                                      BirthdaysDbAdapter.KEY_BIRTHDAY_FORMATTED,
                                      BirthdaysDbAdapter.KEY_LOCATION};
 
         /**
          * And an array of the fields we want to bind those fields to (in this case just text1)
          */
         int[] to = new int[]{R.id.fname, R.id.fbday, R.id.floc};
 
         /**
          * Now create a simple cursor adapter and set it to display
          */
         ColorfulSimpleCursorAdapter birthdays = 
             new ColorfulSimpleCursorAdapter(this, R.layout.birthdays_row, birthdaysCursor, from, to);
         setListAdapter(birthdays);
         return 1;
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
     	MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.options_menu, menu);
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         /**
          * Handle item selection: Launch the Help or About screens as necessary.
          */
     	Intent i;
         switch (item.getItemId()) {
         case R.id.help:
         	i = new Intent(this, obpHelp.class);
             startActivity(i);
             return true;
         case R.id.about:
         	i = new Intent(this, obpAbout.class);
             startActivity(i);
             return true;
         default:
             return super.onOptionsItemSelected(item);
         }
     }
     
     @Override
     protected void onListItemClick(ListView l, View v, int position, long id) {
         super.onListItemClick(l, v, position, id);
         /**
          * On item click, just launch browser to go to friend's wall;
          * can't get the native Fb app intent to work all the time, so disabling that for now.
          */
         String uid = ((CursorAdapter) l.getAdapter()).getCursor().getString(4);
         String uri = "http://facebook.com/" + uid; //"facebook://facebook.com/wall?user=" + uid;
         Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
         try {
             startActivity(intent);
         } catch(ActivityNotFoundException ae) {
         	//TODO
         }
     }
 
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
         super.onActivityResult(requestCode, resultCode, intent);
         facebook.authorizeCallback(requestCode, resultCode, intent);
     }
     
     private void doLocalDbProcess() {
         /**
          * Start the busy progress dialog.
          */
     	if (pdialog != null) {
     	    pdialog.setMessage("Loading birthdays database. Please wait...");
     	}
         /**
          * Fetch data, show error or info messages based on result.
          */
     	int res = fillData();
         if (res > 0)
         {
         	titleText.setText("Birthday Reminders! Click & Post");
         } else if (res < 0) {
         	titleText.setText("Network or Facebook Error! Try again later");
         }
         else {
         	titleText.setText("No birthdays to show today!");
         }
         try {
         	if (pdialog != null) {
                 pdialog.dismiss();
         	}
         } catch (Exception e) {
         	//Ignore. This is just caused by orientation changes.
         }
         
     }
 
     /**
      * Handle FB or network error and attempt to load from local database instead.
      */
     private void fbErrorHandle(String err)
     {
     	titleText.setText(err);
     	titleText.setText("Reading local database...");
     	if (pdialog != null) {
     	    pdialog.setMessage("Loading birthdays database. Please wait...");
     	    pdialog.show();
     	}
     	doLocalDbProcess();
     }
     
     public class ColorfulSimpleCursorAdapter extends SimpleCursorAdapter {
 
     	Calendar calC = new GregorianCalendar();
     	String today = "";
     	String yesterday = "";
     	String tomorrow = "";
     	int hour_of_day = 0;
     	double myLongitudeApprox = 0.0;
     	
 		public ColorfulSimpleCursorAdapter(Context context, int layout,
 				Cursor c, String[] from, int[] to) {
 			super(context, layout, c, from, to);
             /**
              * Get the MMM dd representation of yesterday, today & tomorrow
              * so we can use it for comparison: clumsy but works.
              */
 			calC.add(Calendar.DATE, -1);
 	    	yesterday = df.format(calC.getTime());
 	    	calC.add(Calendar.DATE, 2);
 	    	tomorrow = df.format(calC.getTime());
 	    	calC.add(Calendar.DATE, -1);
 	    	df.format(calC.getTime());
 	    	today = df.format(calC.getTime());
 	    	hour_of_day = calC.get(Calendar.HOUR_OF_DAY);
 	    	/**
              * Calculate user's approximate longitude based on our timezone,
              * used later to determine difference in timezones wiuth friends.
              */
 	    	int offsetFromUTCInMillis = TimeZone.getDefault().getOffset(calC.getTimeInMillis());
 	    	myLongitudeApprox = (double)offsetFromUTCInMillis/(4.0*60.0*1000.0);
 	    	//Log.d("OnTimeBirthdayPost","Yesterday: " + yesterday + " Today: " + today + " Tomorrow: " + tomorrow);
 		}
 		
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent) {
 		    View resultView = super.getView(position, convertView, parent);
 		    Cursor c = super.getCursor();
 
 		    /**
              * Geocode the location from this cursor record;
 		     * use the longitude to determine timezone and 
 		     * highlight all the birthdays being celebrated at this point in time
              * across the world. The calculations are approximate to avoid looking up an
              * actual geo-coordinate to timezone conversion table. We highlight birthdays
              * on the safe side (140 longitude diff instead of 180) to account for the
              * above approximation.
              */
 		    
 		    String flocation = c.getString(3);
 		    String fBdayf = c.getString(2);
         	double flongt = 0;
         	boolean locationFailed = true;
         	if (!flocation.equals("Location Hidden") && !connectionError)
         	{
 	        	try {
 	        		List<Address> addrList = geocoder.getFromLocationName(flocation, 1);
 	        		if (!addrList.isEmpty())
 	        		{
 					    flongt = addrList.get(0).getLongitude();
 					    double longdiff = flongt - myLongitudeApprox;
 					    //Log.d("OnTimeBirthdayPost","Longdiff: " + String.valueOf(longdiff) + " bday: " + fBdayf);
 					    if ((yesterday.equals(fBdayf) && (longdiff < -LONGITUDE_TOLERANCE) &&
                              (hour_of_day < 12)) ||
 						    (today.equals(fBdayf) &&
 						     !((longdiff > LONGITUDE_TOLERANCE) && (hour_of_day > 12)) &&
 						     !((longdiff < -LONGITUDE_TOLERANCE) && (hour_of_day < 12))) ||
 						    (tomorrow.equals(fBdayf) && (longdiff > LONGITUDE_TOLERANCE) &&
                              (hour_of_day > 12))) {
					    	//Log.d("OnTimeBirthdayPost","Bday: " + fBdayf);
 					    	resultView.setBackgroundColor(HIGHLIGHT_COLOR);	
 					    }   	
 					    locationFailed = false;
 	        		}
 				} catch (IOException e) {
 					//Log.w("OnTimeBirthdayPost","Geocoding failed");
 				}
         	}
  
             /**
              * If location to timezone inference failed, highlight the birthday if
              * and only if it is today.
              */
        	if (locationFailed && today.equals(fBdayf)) 
        	{
              	 resultView.setBackgroundColor(HIGHLIGHT_COLOR);
         	}
         	
 		    return resultView;
 		}
 
     }
     public class AboutRequestListener extends BaseRequestListener {
 
         public void onComplete(final String response, final Object state) {
             try {
                 /**
                  * Process the FB response to self info request here: executed in background thread.
                  */
                 //Log.d("OnTimeBirthdayPost", "About Response: " + response.toString());
                 JSONObject json = Util.parseJson(response);
                 JSONObject errResp = json.optJSONObject("error");
                 if (errResp != null) {
                 	final String errType = errResp.getString("type");
                 	if (errType.equals("OAuthException") && !authRetry) {
                 		//Log.d("OnTimeBirthdayPost","Invalid FB token. Redoing auth");
                 		authRetry = true;
                 		fbAuthWrapper();
                 	}
                 	else
                 	{
                         OnTimeBirthdayPost.this.runOnUiThread(new Runnable() {
                             public void run() {
 			                    fbErrorHandle("Facebook authorization error!");
                             }
                         });
                 	}
                 	return;
                 }
                 final String name = json.getString("name");
                 /**
                  * Post the processed result back to the UI thread;
                  * in this case just say hello to user with name.
                  */
                 OnTimeBirthdayPost.this.runOnUiThread(new Runnable() {
                     public void run() {
                     	titleText.setText("Hello there, " + name + "!");
                     	if (pdialog != null) {
                     	    pdialog.setMessage("Loading birthdays database. Please wait...");
                     	    pdialog.show();
                     	}
                     }
                 });
             } catch (JSONException e) {
                 //Log.w("OnTimeBirthdayPost", "JSON Error in response");
             } catch (FacebookError e) {
                 //Log.w("OnTimeBirthdayPost", "Facebook Error: " + e.getMessage());
             }
         }
     }
     public class FriendsRequestListener extends BaseRequestListener {
 
         public void onComplete(final String response, final Object state) {
             /**
              * Process the FB response to friend info request here, again BG thread.
              */
             try {
                 /**
                  * If there is an auth error, retry auth once.
                  */
                 JSONObject json = Util.parseJson(response);
                 JSONObject errResp = json.optJSONObject("error");
                 if (errResp != null) {
                 	final String errType = errResp.getString("type");
                 	if (errType.equals("OAuthException") && !authRetry) {
                 		//Log.d("OnTimeBirthdayPost","Invalid FB token. Redoing auth");
                 		authRetry = true;
                 		fbAuthWrapper();
                 	}
                 	else
                 	{
                         OnTimeBirthdayPost.this.runOnUiThread(new Runnable() {
                             public void run() {
 			                    fbErrorHandle("Facebook authorization error!");
                             }
                         });
                 	}
                 	return;
                 }
                 /**
                  * Get the name, birthday and location data and insert it into our local DB.
                  */
                 JSONArray friends = json.optJSONArray("data");
                 mPrefs = getPreferences(MODE_PRIVATE);
                 long friendsProcessed = mPrefs.getLong("num_friends_processed", 0);
                 //Log.d("OnTimeBirthdayPost","Friends in db: " +
                 //       String.valueOf(friendsProcessed) + " and friends in FB: "
                 //       + String.valueOf(friends.length()));
                 if ((friends != null) && (friends.length() > friendsProcessed)) {
                 	Calendar calD = new GregorianCalendar();
                     OnTimeBirthdayPost.this.runOnUiThread(new Runnable() {
                         public void run() {
                         	if (pdialog != null) {
                         	    pdialog.setMessage("Getting data from Facebook. This may take a while...");
                         	}
                         }
                     });
                     for (int j = 0; j < friends.length(); j++) {
                         JSONObject friend = friends.getJSONObject(j);
                         
                         final String id = friend.getString("id");
                         final String fname = friend.getString("name");
                         String locName = "Location Hidden";
                         String bday = "";
                         try {
                             bday = friend.getString("birthday");
                         } catch (JSONException e) {
                         	continue;
                         }
                         try {
                             JSONObject loc = friend.optJSONObject("location");
                     	    try {
                                 locName = loc.getString("name");
                     	    } catch (JSONException e) {
                     	    }
                         } catch (Exception ef) {
                         } finally {
                         }
 
                         // Extract only month & date from birthday.
                         StringTokenizer st = new StringTokenizer(bday, "/");
                         String bdayf = "";
                         String bday_formatted = "";
                         while(st.countTokens() > 1) {
                             String month = st.nextToken();
                         	calD.set(Calendar.MONTH, Integer.parseInt(month)-1);
                             String date = st.nextToken();
                             calD.set(Calendar.DATE, Integer.parseInt(date));
                             bday_formatted = df.format(calD.getTime());
                             bdayf = month + date;
                         }
                         
                         // Add friend record to database.
                         mDbHelper.createFriendRecord(fname, bdayf, bday_formatted, locName, id);
                     }
                     SharedPreferences.Editor editor = mPrefs.edit();
                     editor.putLong("num_friends_processed", friends.length());
                     editor.commit();
                 }
             } catch (JSONException e) {
                 //Log.w("OnTimeBirthdayPost", "JSON Error in response");
             } catch (FacebookError e) {
                 //Log.w("OnTimeBirthdayPost", "Facebook Error: " + e.getMessage());
             }
             /**
              * Finally process Db data for display.
              */
         	OnTimeBirthdayPost.this.runOnUiThread(new Runnable() {
                 public void run() {
                 	doLocalDbProcess();
                 }
         	});
         }
     }
 }
