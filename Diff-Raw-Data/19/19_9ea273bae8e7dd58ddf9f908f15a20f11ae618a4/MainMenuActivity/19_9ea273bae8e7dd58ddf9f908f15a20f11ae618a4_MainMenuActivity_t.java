 package com.schedushare.android;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.ObjectOutputStream;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.List;
 import java.util.concurrent.ExecutionException;
 
 import org.springframework.web.client.RestTemplate;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentTransaction;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.Button;
 import android.widget.LinearLayout;
 import android.widget.Toast;
 
 import com.facebook.FacebookActivity;
 import com.facebook.Request;
 import com.facebook.Response;
 import com.facebook.SessionState;
 import com.facebook.model.GraphUser;
 import com.schedushare.android.db.ScheduleData;
 import com.schedushare.android.db.SchedulesDataSource;
 import com.schedushare.android.db.TimeBlockData;
 import com.schedushare.android.db.UserData;
 import com.schedushare.android.fragments.EditDayFragment;
 import com.schedushare.android.fragments.FacebookAuthFragment;
 import com.schedushare.android.guice.GuiceConstants;
 import com.schedushare.android.schedule.task.CreateScheduleTask;
 import com.schedushare.android.schedule.task.GetActiveScheduleTask;
 import com.schedushare.android.user.task.CreateUserTask;
 import com.schedushare.android.util.ResourceUriBuilder;
 import com.schedushare.common.domain.dto.ScheduleEntity;
 import com.schedushare.common.domain.dto.TimeBlockEntity;
 import com.schedushare.common.domain.dto.UserEntity;
 import com.schedushare.common.domain.rest.RestResult;
 import com.schedushare.common.domain.rest.RestResultHandler;
 import com.schedushare.common.util.JSONUtil;
 
 public class MainMenuActivity extends FacebookActivity {
 	private static final String FIRST_SCHEDULE = "First Schedule";
 
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
         	this.lastViewedDay = 0;
         	
         	startLocationManager();
         	
         	// For debug only.
 	        //createTestData();
         	
         	// Extract active user from database.
         	setActiveScheduleId();
         	
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
     							
 //    							// Get user's Facebook ID to query back end for all their schedules.
 //    							// If the user does not have any schedules stored in the back end,
 //    							// create one for them and send it back.
 //    							GetSchedulesTask getSchedulesTask = new GetSchedulesTask(new RestTemplate(),
 //    									new ResourceUriBuilder(GuiceConstants.HOST, GuiceConstants.HOST_PORT),
 //    									GuiceConstants.USER_SCHEDULE_RESOURCE, new RestResultHandler(new JSONUtil()));
 //    							try {
 //									RestResult<ScheduleListEntity> restResult = getSchedulesTask.execute(user.getId())
 //													.get();
 //									if (restResult.isFailure()) {
 //										// Handle error
 //										SchedushareExceptionEntity error = restResult.getError();
 //										
 //										Toast.makeText(MainMenuActivity.this, "Server authentication failed",
 //												Toast.LENGTH_LONG).show();
 //									} else {
 //										ScheduleListEntity scheduleListEntity = restResult.getRestResult();
 //										// Render schedules from schedule list provided.
 //									}
 //								} catch (InterruptedException e) {
 //									e.printStackTrace();
 //								} catch (ExecutionException e) {
 //									e.printStackTrace();
 //								}
 //    							System.out.println("MainMenu: owner fb id: " + user.getId());
     							
     							
     							// Create user entry if he/she does not exist on the back end.
     							// Also create the user's first schedule. Call it "First Schedule",
     							// and send it back to update local copy.
     							CreateUserTask createUserTask = new CreateUserTask(new RestTemplate(),
     									new ResourceUriBuilder(GuiceConstants.HOST, GuiceConstants.HOST_PORT), 
     									GuiceConstants.USER_RESOURCE,new RestResultHandler(new JSONUtil()));
     							
     							try {
     								String userFBID = Long.toString(settings.getLong(getString(R.string.settings_owner_facebook_id), -1));
 									RestResult<UserEntity> restResult = createUserTask.execute(new UserEntity(userFBID, "",
 											settings.getString(getString(R.string.settings_owner_facebook_name), "User"),"")).get();
 									
 									if (restResult.isFailure()) {
 										GetActiveScheduleTask getActiveScheduleTask = new GetActiveScheduleTask(new RestTemplate(),
 												new ResourceUriBuilder(GuiceConstants.HOST, GuiceConstants.HOST_PORT),
 												GuiceConstants.ACTIVE_SCHEDULE_RESOURCE,
 												new RestResultHandler(new JSONUtil()));
 										RestResult<ScheduleEntity> getActiveScheduleResult = getActiveScheduleTask.execute(userFBID).get();
 										
 										if (getActiveScheduleResult.isFailure()) {
 											//Do something due to some back end error.
 										} else {
 											ScheduleEntity activeScheduleEntity = getActiveScheduleResult.getRestResult();
 											
 											if (activeScheduleEntity == null) {
 											} else {
 												// Update the local db copy of the active schedule.
 												SchedulesDataSource dataSource = new SchedulesDataSource(MainMenuActivity.this);
 				    					    	dataSource.open();
 				    					    	ScheduleData schedule = dataSource.getActiveScheduleFromOwnerId(
 				    					    			settings.getLong(getString(R.string.settings_owner_id), -1));
 				    					    	schedule.sid = activeScheduleEntity.getScheduleId();
 				    					    	schedule.name = activeScheduleEntity.getScheduleName();
 				    					    	dataSource.updateSchedule(schedule);
 				    					        dataSource.close();
 				    					        
 				    					        // Update the user preferences.
 				    					        editor.putLong(getString(R.string.settings_owner_active_schedule_sid), schedule.sid);
 				    					        editor.commit();
 											}
 										}
 									} else {
 										UserEntity createdUser = restResult.getRestResult();
 										//No active schedule for user. Create a schedule.
 										CreateScheduleTask createScheduleTask = new CreateScheduleTask(new RestTemplate(), 
 												new ResourceUriBuilder(GuiceConstants.HOST, GuiceConstants.HOST_PORT), 
 												GuiceConstants.SCHEDULE_RESOURCE, 
 												new RestResultHandler(new JSONUtil()));
 										RestResult<ScheduleEntity> createScheduleEntityResult = 
 												createScheduleTask.execute(new ScheduleEntity(0, FIRST_SCHEDULE, Boolean.TRUE, userFBID, null)).get();
 										
 										if (createScheduleEntityResult.isFailure()) {
 											//Do something with error on the backend.
 										} else {
 											ScheduleEntity createdScheduleEntity = createScheduleEntityResult.getRestResult();		
 											
 											// Update the local db copy of the active schedule.
 											SchedulesDataSource dataSource = new SchedulesDataSource(MainMenuActivity.this);
 			    					    	dataSource.open();
 			    					    	ScheduleData schedule = dataSource.getActiveScheduleFromOwnerId(settings.getLong(getString(R.string.settings_owner_id), -1));
 			    					    	schedule.sid = createdScheduleEntity.getScheduleId();
 			    					    	dataSource.updateSchedule(schedule);
 			    					        dataSource.close();
 			    					        
 			    					        // Update the user preferences.
 			    					        editor.putLong(getString(R.string.settings_owner_active_schedule_sid), schedule.sid);
 			    					        editor.commit();
 										}
 										
 									}
 								} catch (InterruptedException e) {
 									e.printStackTrace();
 								} catch (ExecutionException e) {
 									e.printStackTrace();
 								}
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
         if (requestCode == FriendPickerActivity.REQUEST_CODE) {
 	        if (resultCode == Activity.RESULT_OK) {
 	        	this.selectedUsers = ((SchedUShareApplication)getApplication())
 	                    .getSelectedUsers();
 	        	
 	        	SchedulesDataSource dataSource = new SchedulesDataSource(this);
 	        	Collection<ScheduleEntity> userSchedules = new ArrayList<ScheduleEntity>();
 	        	ArrayList<GraphUser> selectedUsers = new ArrayList<GraphUser>();
 	        	
 	        	dataSource.open();
 	        	
 	        	for (GraphUser u : this.selectedUsers) {
 	        		System.out.println("MainMenu: friend id: " + u.getId());
 	        		
 	        		// Create user in local db if not already created.
	        		dataSource.createUser(Long.parseLong(u.getId()), u.getName());
 	        		
 	        		// Call back end with each User ID to get their active schedules and time blocks.
 	        		GetActiveScheduleTask getActiveScheduleTask = new GetActiveScheduleTask(new RestTemplate(),
 	        				new ResourceUriBuilder(GuiceConstants.HOST, GuiceConstants.HOST_PORT), 
 	        				GuiceConstants.ACTIVE_SCHEDULE_RESOURCE, new RestResultHandler(new JSONUtil()));
 	        		try {
 						RestResult<ScheduleEntity> restResult = getActiveScheduleTask.execute(u.getId()).get();
 						
 						if (restResult.isFailure()) {
 							// Output error.
 							Toast.makeText(this, "Diff failed.", Toast.LENGTH_LONG).show();
 						} else {
 							if (restResult.getRestResult() == null) {
 							} else {
 								// Add to collection of active schedules.
 								userSchedules.add(restResult.getRestResult());
 								
 								// Update selected users with only users who have schedules in the back end.
 								selectedUsers.add(u);
 							}
 						}
 					} catch (InterruptedException e) {
 						e.printStackTrace();
 					} catch (ExecutionException e) {
 						e.printStackTrace();
 					}
 	        	}
 	        	
 	        	// Create all the friend schedules if they don't exist already.
 	        	for (ScheduleEntity s : userSchedules) {
	        		System.out.println("MainMenu: Add friend schedule.");
	        		System.out.println("MainMenu: scheduleId: " + s.getScheduleId());
	        		System.out.println("MainMenu: scheduleName: " + s.getScheduleName());
	        		System.out.println("MainMenu: getUserId: " + s.getUserId());
	        		System.out.println("MainMenu: getLastModified: " + s.getLastModified());
	        		
	        		UserData friend = dataSource.getUserFromSid(Long.parseLong(s.getUserId()));
	        		
	        		ScheduleData schedule = dataSource.createSchedule(s.getScheduleId(), s.getScheduleName(), 
	        				true, friend.id, s.getLastModified());
 	        		
 	        		// Time block missing two attributes. Name and Block Type ID.
 	        		for (TimeBlockEntity t : s.getTimeBlocks()) {
 	        			dataSource.createTimeBlock(t.getTimeBlockId(), "Block", t.getStartTime(), 
 	        					t.getEndTime(), TimeBlockData.getDayIntFromString(t.getDay()),
	        					1, schedule.id, t.getLongitude(), t.getLatitude());
 	        		}
 	        	}
 	        	dataSource.close();
 	        	
 	        	// Update the selected users to only those who have schedules in the back end.
 	        	this.selectedUsers = selectedUsers;
 	        	
 	        	// Start diff activiy with selected users if any.
 	        	if (this.selectedUsers.size() > 0) {
 		        	Intent intent = new Intent(this, DiffActivity.class);
 		        	Bundle b = new Bundle();
 				    b.putByteArray("selectedUsers", getByteArray(this.selectedUsers));
 				    intent.putExtras(b);
 			        startActivity(intent);
 	        	} else {
 	        		Toast.makeText(this, "None of your friends have an active schedule to diff!",
 	        				Toast.LENGTH_LONG).show();
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
 		        break;
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
         startActivityForResult(intent, FriendPickerActivity.REQUEST_CODE);
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
         fragmentTransaction.replace(R.id.active_schedule_container, this.dayFragments[this.lastViewedDay]);
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
     
     private void setActiveScheduleId() {
     	SchedulesDataSource dataSource = new SchedulesDataSource(this);
     	dataSource.open();
     	UserData owner = dataSource.checkOwnerCreated();   	
     	this.activeScheduleId = dataSource.getActiveScheduleFromOwnerId(owner.id).id;
         dataSource.close();
         
         // Check whether preferences have been created.
         SharedPreferences p = getSharedPreferences(MainMenuActivity.PREFS_NAME, 0);
         if (p.getLong(getString(R.string.settings_owner_id), -1) == -1) {
 	        SharedPreferences.Editor editor = p.edit();
 			editor.putLong(getString(R.string.settings_owner_id), owner.id);
 			editor.putLong(getString(R.string.settings_owner_active_schedule_id), this.activeScheduleId);
 			editor.commit();
         }
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
     
     // Creates test data for debug.
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
 }
