 package com.schedushare.android;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.ObjectOutputStream;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.List;
 import java.util.concurrent.ExecutionException;
 
 import com.facebook.FacebookActivity;
 import com.facebook.GraphUser;
 import com.facebook.Request;
 import com.facebook.Response;
 import com.facebook.SessionState;
 import com.google.inject.Inject;
 import com.google.inject.Provider;
 import com.schedushare.android.db.SchedulesDataSource;
 import com.schedushare.android.db.UserData;
 import com.schedushare.android.fragments.EditDayFragment;
 import com.schedushare.android.fragments.FacebookAuthFragment;
 import com.schedushare.android.fragments.NewScheduleDialogFragment;
 import com.schedushare.android.schedule.task.GetSchedulesTask;
 import com.schedushare.common.domain.dto.ScheduleEntity;
 import com.schedushare.common.domain.dto.ScheduleListEntity;
 import com.schedushare.common.domain.rest.RestResult;
 
 import roboguice.activity.RoboActivity;
 import roboguice.fragment.RoboDialogFragment;
 import roboguice.inject.ContentView;
 import roboguice.inject.InjectView;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentTransaction;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.Button;
 import android.widget.LinearLayout;
 import android.widget.Toast;
 
 public class MainMenuActivity extends FacebookActivity {
 	public static final String PREFS_NAME = "MAIN_SETTINGS";
 	
     //@Inject Provider<GetSchedulesTask> getGetSchedulesTaskProvider;
     
     private LinearLayout dayButtonScroller;
 
     public long activeScheduleId;
     private EditDayFragment[] dayFragments;
     private int lastViewedDay;
     private List<GraphUser> selectedUsers;
     
     private FacebookAuthFragment facebookAuthFragment;
     
     
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main_menu);
         
         if (savedInstanceState == null) {
         	
 //        	RestResult<ScheduleListEntity> getSchedulesResult;
 //			try {
 //				getSchedulesResult = getGetSchedulesTaskProvider.get().execute("test@email.com").get();
 //				
 //				SchedulesDataSource dataSource = new SchedulesDataSource(this);
 //				dataSource.open();
 //				
 //				if (getSchedulesResult.isFailure()) {
 //					Toast.makeText(this, getSchedulesResult.getError().getException(), Toast.LENGTH_LONG).show();
 //				} else {
 //					for (ScheduleEntity schedule : getSchedulesResult.getRestResult().getScheduleList()) {
 //						dataSource.createSchedule(schedule.getScheduleId(), schedule.getScheduleName(), false, 1, "whatever");
 //					}
 //				}
 //				
 //				dataSource.close();
 //			} catch (InterruptedException e) {
 //				e.printStackTrace();
 //			} catch (ExecutionException e) {
 //				e.printStackTrace();
 //			}
 			
         	startLocationManager();
 	        createTestData();
 	        initializeCurrentScheduleLayout();
 	        initializeFacebookLayout();
         }
     }
     
     @Override
     public void onResume() {
     	super.onResume();
     	
     	// Re-initialize fragments if active schedule changes.
     	SharedPreferences p = getSharedPreferences(MainMenuActivity.PREFS_NAME, 0);
     	long activeScheduleId = p.getLong(getString(R.string.settings_owner_active_schedule_id), 1);
     	if (this.activeScheduleId != activeScheduleId) {
     		this.activeScheduleId = activeScheduleId;
     		initializeFrameLayout();
     	}
     }
     
     @Override
     protected void onSessionStateChange(SessionState state, Exception exception) {
     	// user has either logged in or not ...
     	if (state.isOpened()) {
     		// make request to the /me API
     		Request request = Request.newMeRequest(
     				this.getSession(),
     				new Request.GraphUserCallback() {
     					// callback after Graph API response with user object
     					@Override
     					public void onCompleted(GraphUser user, Response response) {
     						if (user != null) {
     							SharedPreferences settings = getSharedPreferences(MainMenuActivity.PREFS_NAME, 0);
     							SharedPreferences.Editor editor = settings.edit();
     							editor.putLong(getString(R.string.settings_owner_facebook_id), Long.parseLong(user.getId()));
     							editor.putString(getString(R.string.settings_owner_facebook_name), user.getName());
     							editor.putString(getString(R.string.settings_owner_facebook_username), user.getUsername());
     							editor.commit();
     							
     							// For Hamid:
     							// Get user's Facebook ID to query back end for all their schedules.
     							// If the user does not have any schedules stored in the backend,
     							// create one for them and send it back.
     							System.out.println("MainMenu: owner fb id: " + user.getId());
     						}
     					}
     				}
     		);
     		Request.executeBatchAsync(request);
     	}
     }
 
     @Override
     public void onActivityResult(int requestCode, int resultCode, Intent data) {
         super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0) {
 	        if (resultCode == Activity.RESULT_OK) {
 	        	this.selectedUsers = ((SchedUShareApplication)getApplication())
 	                    .getSelectedUsers();
 	        	
 	        	SchedulesDataSource dataSource = new SchedulesDataSource(this);
 	        	dataSource.open();
 	        	int i = 101;
 	        	for (GraphUser u : this.selectedUsers) {
 	        		System.out.println("friend id: " + u.getId());
 	        		UserData user = dataSource.createUser(Long.parseLong(u.getId()), u.getName());
 	        		dataSource.createSchedule(i, "friend schedule", true, user.id, "whatever");
 	        		
 	        		// For Hamid:
 	        		// Call back end with each User ID to get their active schedules and time blocks.
 	        		
 	        	}
 	        	dataSource.close();
 	        	
 	        	// Start diff activiy with selected users if any.
 	        	if (this.selectedUsers.size() > 0) {
 		        	Intent intent = new Intent(this, DiffActivity.class);
 		        	Bundle b = new Bundle();
 				    b.putByteArray("selectedUsers", getByteArray(this.selectedUsers));
 				    intent.putExtras(b);
 			        startActivity(intent);
 	        	}
 	        }
         }
     }
     
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Create menu with new schedule button.
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.main_menu, menu);
 		
 		return true;
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 			case R.id.view_schedules_option:
 				// Open schedules menu.
 				Intent intent = new Intent(this, SchedulesMenuActivity.class);
 		        startActivity(intent);
 			default:
 				break;
 	  }
 	
 	  return true;
 	}
     
     // Called when user clicks bump button.
     public void startBump(View view) {
     	Intent intent = new Intent();
     	intent.setClass(this, BeamActivity.class);
     	startActivity(intent);
     }
     
     // Called when user clicks settings button.
     public void startDiff(View view) {
     	Intent intent = new Intent();
         intent.setData(FriendPickerActivity.FRIEND_PICKER);
         intent.setClass(this, FriendPickerActivity.class);
        startActivityForResult(intent, 0);
     }
     
     // Creates test data.
     private void createTestData() {
     	Calendar dateTime = Calendar.getInstance();
     	
     	SchedulesDataSource dataSource = new SchedulesDataSource(this);
         dataSource.open();
         dataSource.dropAllTables();
         
         SharedPreferences p = getSharedPreferences(MainMenuActivity.PREFS_NAME, 0);
         SharedPreferences.Editor editor = p.edit();
 		editor.putLong(getString(R.string.settings_owner_id), 1);
 		editor.putLong(getString(R.string.settings_owner_active_schedule_id), 1);
 		editor.commit();
         
         dataSource.createUser(p.getLong(getString(R.string.settings_owner_facebook_id), 1),
         		p.getString(getString(R.string.settings_owner_facebook_name), "owner"));
         dataSource.createSchedule(0, "schedule" + 0, true,
     			p.getLong(getString(R.string.settings_owner_id), 1),
     			dateTime.getTime().toString());
         for (int i = 1; i < 100; i++) {
         	dataSource.createSchedule(i, "schedule" + i, false,
         			p.getLong(getString(R.string.settings_owner_id), 1),
         			dateTime.getTime().toString());
         }
         System.out.println("MainMenu: TestData: owner fb id: " + p.getLong(getString(R.string.settings_owner_facebook_id), -1));
         
         SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss aa");
         dateTime.set(Calendar.HOUR, 5);
         dateTime.set(Calendar.MINUTE, 0);
         dateTime.set(Calendar.SECOND, 0);
         dateTime.set(Calendar.AM_PM, Calendar.AM);
         
         String startTime;
         String endTime;
 		startTime = sdf.format(dateTime.getTime());
 		dateTime.set(Calendar.HOUR, 15);
 		endTime = sdf.format(dateTime.getTime());
 		
 		System.out.println("MainMenu: start: " + startTime.toString() + 
 				" end: " + endTime.toString());
         
         dataSource.createBlockType(1, "School");
         dataSource.createBlockType(2, "Work");
         dataSource.createBlockType(3, "Social");
         dataSource.createBlockType(4, "Extra Curricular");
         dataSource.createBlockType(5, "On Bus");
         dataSource.createBlockType(6, "On Vacation");
         dataSource.createTimeBlock(1, "EECE 419", startTime.toString(), endTime.toString(), 0, 1, 1, 5.55, 5.55);
         dataSource.close();
         
 		this.activeScheduleId = p.getLong(getString(R.string.settings_owner_active_schedule_id), 1);
         System.out.println("MainMenu: activeScheduleId: " + p.getLong(getString(R.string.settings_owner_active_schedule_id), -1));
 
     }
     
     // Start location listener.
     private void startLocationManager() {
     	LocationManager locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
     	String locationProvider = LocationManager.NETWORK_PROVIDER;
     	LocationListener locationListener = new LocationListener() {
     		@Override
     		public void onLocationChanged(Location location) {}
 
 			@Override
 			public void onProviderDisabled(String arg0) {}
 
 			@Override
 			public void onProviderEnabled(String provider) {}
 
 			@Override
 			public void onStatusChanged(String provider, int status, Bundle extras) {}
     	};
     	//locationManager.requestLocationUpdates(locationProvider, 0, 0, locationListener);
     }
     
     // Creates all fragments and initializes frame layout to monday.
     private void initializeFrameLayout() {    	
     	this.dayFragments = new EditDayFragment[7];
         for (int i = 0; i < 7; i++) {
         	this.dayFragments[i] = new EditDayFragment();
         	Bundle args = new Bundle();
         	args.putInt("day", i);
         	args.putLong("scheduleId", this.activeScheduleId);
         	this.dayFragments[i].setArguments(args);
         }
         
         // Get an instance of FragmentTransaction from your Activity.
         FragmentManager fragmentManager = getSupportFragmentManager();
         FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
 
         // Put Monday in view.
         fragmentTransaction.replace(R.id.active_schedule_container, this.dayFragments[0]);
         this.lastViewedDay = 0;
         fragmentTransaction.commit();
     }
     
     private void initializeCurrentScheduleLayout() {
     	// Create all fragments.
         initializeFrameLayout();
         
         // Create scroller to switch between days of the week.
         this.dayButtonScroller = (LinearLayout)findViewById(R.id.active_schedule_day_button_scroller);
         OnClickListener dayButtonClickListener = new OnClickListener() {
         	@Override
 			public void onClick(View view) {
         		Toast.makeText(MainMenuActivity.this,
         				((Button)view).getText() + " is clicked!", Toast.LENGTH_SHORT).show();
         		
         		FragmentManager fragmentManager = getSupportFragmentManager();
         		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
         		fragmentTransaction.replace(R.id.active_schedule_container, MainMenuActivity.this.dayFragments[view.getId()]);
         		fragmentTransaction.commit();
         		
         		MainMenuActivity.this.lastViewedDay = view.getId();
 			}
         };
         
         // Create all 7 buttons.
         createDayButton(dayButtonClickListener, 0, "Mon", 150, 75);
         createDayButton(dayButtonClickListener, 1, "Tue", 150, 75);
         createDayButton(dayButtonClickListener, 2, "Wed", 150, 75);
         createDayButton(dayButtonClickListener, 3, "Thu", 150, 75);
         createDayButton(dayButtonClickListener, 4, "Fri", 150, 75);
         createDayButton(dayButtonClickListener, 5, "Sat", 150, 75);
         createDayButton(dayButtonClickListener, 6, "Sun", 150, 75);
     }
     
     private void createDayButton(OnClickListener listener, int id, String text, int width, int height) {    	
     	Button dayButton = new Button(this);
         dayButton.setId(id);
         dayButton.setOnClickListener(listener);
         dayButton.setText(text);
         dayButton.setLayoutParams(new LayoutParams(width, height));
         
         this.dayButtonScroller.addView(dayButton);
     }
     
     private void initializeFacebookLayout() {
     	FragmentManager fragmentManager = getSupportFragmentManager();
         FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
 
         // Put Facebook auth button in view.
         this.facebookAuthFragment = new FacebookAuthFragment();
         fragmentTransaction.add(R.id.facebook_auth_container, this.facebookAuthFragment);
         fragmentTransaction.commit();
     }
     
     private byte[] getByteArray(List<GraphUser> users) {
         // convert the list of GraphUsers to a list of String 
         // where each element is the JSON representation of the 
         // GraphUser so it can be stored in a Bundle
         List<String> usersAsString = new ArrayList<String>(users.size());
 
         for (GraphUser user : users) {
             usersAsString.add(user.getInnerJSONObject().toString());
         }   
         try {
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             new ObjectOutputStream(outputStream).writeObject(usersAsString);
             return outputStream.toByteArray();
         } catch (IOException e) {
         	e.printStackTrace();
         }   
         return null;
     }  
 }
