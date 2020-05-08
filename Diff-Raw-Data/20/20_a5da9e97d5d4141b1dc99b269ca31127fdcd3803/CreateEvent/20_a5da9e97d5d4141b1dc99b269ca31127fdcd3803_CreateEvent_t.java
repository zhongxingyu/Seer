 package com.appchallenge.android;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 import android.app.DatePickerDialog;
 import android.app.Dialog;
 import android.app.TimePickerDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.location.Location;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.support.v4.app.DialogFragment;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentStatePagerAdapter;
 import android.support.v4.app.NavUtils;
 import android.support.v4.view.PagerAdapter;
 import android.support.v4.view.ViewPager;
 import android.text.format.DateFormat;
 import android.util.Log;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.DatePicker;
 import android.widget.EditText;
 import android.widget.RadioButton;
 import android.widget.RadioGroup;
 import android.widget.RadioGroup.OnCheckedChangeListener;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.TimePicker;
 import android.widget.Toast;
 import android.widget.ViewSwitcher;
 
 import java.sql.Timestamp;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Formatter;
 import java.util.List;
 
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuItem;
 import com.google.android.gms.maps.CameraUpdate;
 import com.google.android.gms.maps.model.LatLng;
 
 import com.appchallenge.android.APICalls;
 
 /**
  * Wizard activity for creating new events.
  */
 public class CreateEvent extends SherlockFragmentActivity implements CreateEventInterface {
     /**
      * The pager widget, which handles animation and allows swiping horizontally to access previous
      * and next wizard steps.
      */
     private ViewPager mPager;
 
     /**
      * The pager adapter, which provides the pages to the view pager widget.
      */
     private PagerAdapter mPagerAdapter;
 
     // Methods for the CreateEventInterface, which provide access to wizard public members
     // for the fragment pages.
     // See http://developer.android.com/training/basics/fragments/communicating.html 
 
     
     private String name = "";
     private String description = "";
     public static Calendar cal_1;
     public static Calendar cal_2;
     public static Calendar cal_3;
     static ViewSwitcher switcher;
     private Date startDate;
     private long startTime;
     private long duration;
     private LatLng mapLocation;
     
     /**
      * The name of the event provided by the user.
      */
 	public void setName(String name) {
 		this.name = name;
 	}
 	public String getName() {
 		return this.name;
 	}
 
 	/**
 	 * The description of the event provided by the user.
 	 */
     
 	public void setDescription(String description) {
 		this.description = description;
 	}
 	public String getDescription() {
 		return this.description;
 	}
 
 	/**
 	 * The date when the event goes live.
 	 */
 	
 	public void setDate(Date date) {
 		this.startDate = date;
 	}
 	public Date getDate() {
 		return this.startDate;
 	}
 
 	public void setStartTime(long time, int hours, int minutes, String am_pm){
 		int overflow = (am_pm.equals("am")) ? 0 : 43200;
 		this.startTime = overflow + (minutes*60) + (hours*60*60) + time;
 	}
 	
 	public void setDuration(float hours, long time){
 		
 		this.duration = (long) hours*60*60 + time;
 	}
 	/**
      * Location keeping track of the wizard map camera.
      */
     
 	public void setLocation(LatLng location) {
 		this.mapLocation = location;
 	}
 	public LatLng getLocation() {
 		return this.mapLocation;
 	}
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_create_event);
         
         switcher = (ViewSwitcher) findViewById(R.id.profileSwitcher);
 
         // Instantiate a ViewPager and a PagerAdapter.
         mPager = (ViewPager)findViewById(R.id.pager);
         mPagerAdapter = new CreateEventPagerAdapter(getSupportFragmentManager());
         mPager.setAdapter(mPagerAdapter);
         mPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
             @Override
             public void onPageSelected(int position) {
                 // When changing pages, reset the action bar actions since they are dependent
                 // on which page is currently active. An alternative approach is to have each
                 // fragment expose actions itself (rather than the activity exposing actions),
                 // but for simplicity, the activity provides the actions.
                 invalidateOptionsMenu();
             }
         });
         
         
         
         if (startDate == null)
         	startDate = new Date();
         
         // Set the map location to use the location given from the EventViewer.
         if (mapLocation == null) {
             Intent receivedIntent = getIntent();
             mapLocation = new LatLng(receivedIntent.getDoubleExtra("latitude", 0),
                                      receivedIntent.getDoubleExtra("longitude", 0));
         }
         
         
         
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         super.onCreateOptionsMenu(menu);
         getSupportMenuInflater().inflate(R.menu.activity_create_event, menu);
 
         menu.findItem(R.id.action_back).setEnabled(mPager.getCurrentItem() > 0);
 
         // Add either a "next" or "submit" button to the action bar.
         int action = (mPager.getCurrentItem() == mPagerAdapter.getCount() - 1)
                      ? R.string.action_finish : R.string.action_next;
         MenuItem item = menu.add(Menu.NONE, R.id.action_next, Menu.NONE, action);
         item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
     	Context context = getApplicationContext();
     	int duration = Toast.LENGTH_SHORT;
     	Toast toast = Toast.makeText(context, "test", duration);
     	
         if (item.getItemId() == android.R.id.home) {
 			// See http://developer.android.com/design/patterns/navigation.html for more.
 			NavUtils.navigateUpTo(this, new Intent(this, EventViewer.class));
         	return true;
         	
 		} else if (item.getItemId() == R.id.action_back) {
 			// Go to the previous step in the wizard. If there is no previous step,
 			// setCurrentItem will do nothing.
 			mPager.setCurrentItem(mPager.getCurrentItem() - 1);
 			return true;
 
 		} else if (item.getItemId() == R.id.action_next && !item.getTitle().toString().equals("Submit")) {
 			// Advance to the next step in the wizard. If there is no next step, setCurrentItem
 			// will do nothing.
 			if(mPager.getCurrentItem() == 0){
 				setName(((EditText) findViewById(R.id.event_name)).getText().toString());
 				setDescription(((EditText) findViewById(R.id.event_description)).getText().toString());
 			}
 			
 			if(mPager.getCurrentItem() == 1){
 				
 				String minutes = ((EditText) findViewById(R.id.event_minutes)).getText().toString();
 				String hours = ((EditText) findViewById(R.id.event_hours)).getText().toString();
				String event_time = ((EditText) findViewById(R.id.event_enter_hours)).getText().toString();
 				String am_pm = ((Spinner) findViewById(R.id.spinner1)).getSelectedItem().toString();
				
				//Checking if null values left
				if (minutes.equals("")) { minutes = "0"; }
				if (hours.equals("")) { hours = "0"; }
				if (event_time.equals("")) { event_time = "0"; }
				
				//Checking for valid inputs
 				if (Integer.parseInt(hours) > 12 || Integer.parseInt(minutes) > 60) {
 					toast = Toast.makeText(context, "Invalid Start Time", duration);
 					toast.show();
 					return true;
 				}
 				if (Float.parseFloat(event_time) > 24){
					toast = Toast.makeText(context, "Invalid Duration. Enter Number Less Than 24", duration);
 					toast.show();
 					return true;
 				}
				
				
 				setStartTime(getSeconds(((Spinner) findViewById(R.id.spinner1)).getSelectedItemPosition()), 
 						Integer.parseInt(hours), Integer.parseInt(minutes), am_pm);
 				setDuration(Float.parseFloat(event_time), this.startTime);
 			}
			
 			mPager.setCurrentItem(mPager.getCurrentItem() + 1);
 			return true;
 			
 		  //using item.getItemId() == R.id.action_finish doesn't work because the button is under a different action
 		} else if (item.getItemId() == R.id.action_next && item.getTitle().toString().equals("Submit")) {
 			// This is the code that extracts the data from the wizard
 			// It will then make a JSONObject and Post it returning a toast about success or failure
 
 			long time = new java.util.Date().getTime();
 			if (this.startDate != null)
 				time = this.startDate.getTime();
 			
 			Event newEvent = new Event(name, description, startTime, this.duration, /*type,*/ mapLocation);
 			
 			JSONObject object = new JSONObject();
 			try {
                 object.put("title", name);
                 object.put("description", description);
                 object.put("star_date", startTime);
                 object.put("end_date", this.duration);
                 //object.put("type", type);
                 object.put("latitude", mapLocation.latitude);
                 object.put("longitude", mapLocation.longitude);
                 String object_2 = object.toString();
                 toast = Toast.makeText(context, object_2, duration);
 				
 				//This toast verifies that the data is being passed correctly
 				//Remove after done debugging
 				toast.show();
 			} catch (JSONException e) {
 			   	Log.e(CreateEvent.class.toString(), "Error creating JSON Object in Create Event");
 			    e.printStackTrace();
 			}
 			//boolean returnedEvent = APICalls.createEvent(newEvent);
 			//toast = Toast.makeText(context, ((Boolean)returnedEvent).toString(), duration);
 			//toast.show();
 			
 			// TODO Add the actual Call to the database
 	
 			// TODO This return is not enough. We will need to wait until the API call
 			// finishes before closing this activity. A progress spinner will probably
 			// be the cleanest way to show progress.
 			return true;
 		}
 
         return super.onOptionsItemSelected(item);
     }
 
 	/**
      * A pager adapter that represents the wizard pages sequentially.
      */
     public class CreateEventPagerAdapter extends FragmentStatePagerAdapter {
     	// Instances of each wizard page, in order.
     	public Fragment[] wizardPages = new Fragment[] {
             new CreateEventPage1EventName(),
             new CreateEventPage2EventTime(),
             new CreateEventPage3EventLocation()
     	};
     	
         public CreateEventPagerAdapter(FragmentManager fragmentManager) {
             super(fragmentManager);
         }
 
         @Override
         public Fragment getItem(int position) {
             return wizardPages[position];
         }
 
         @Override
         public int getCount() {
             return wizardPages.length;
         }
     }
     
     
     private long getSeconds(int date){
     	Calendar calen = (date == 0) ? cal_1: (date == 1) ? cal_2 : cal_3;
     	long time = calen.getTimeInMillis();
     	return (time / 1000);
     	
     }
     
 }
