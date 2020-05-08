 package com.schedushare.android;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.HashMap;
 import java.util.List;
 
 import com.schedushare.android.db.BlockTypeData;
 import com.schedushare.android.db.SchedulesDataSource;
 import com.schedushare.android.db.TimeBlockData;
 
 import roboguice.activity.RoboActivity;
 import roboguice.inject.ContentView;
 import roboguice.inject.InjectView;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.location.Criteria;
 import android.location.Location;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.Spinner;
 
 @ContentView(R.layout.activity_edit_time_block)
 public class EditTimeBlockActivity extends RoboActivity {
 	private Calendar startTime;
 	private SimpleDateFormat timeFormat;
 	private ArrayList<TimeBlockData> timeBlocks = null;
     private HashMap<Long, BlockTypeData> blockTypes = null;
     private TimeBlockData newTimeBlock;
     
     public static final int REQUEST_CODE = 420;
     
     @InjectView(R.id.edit_time_block_name_input) private EditText nameEditView;
     @InjectView(R.id.edit_time_block_type_spinner) private Spinner typeSpinner;
     @InjectView(R.id.edit_time_block_location_button) private Button locationButton;
     @InjectView(R.id.edit_time_block_start_time_spinner) private Spinner startTimeSpinner;
     @InjectView(R.id.edit_time_block_end_time_spinner) private Spinner endTimeSpinner;
     @InjectView(R.id.edit_time_block_confirm_button) private Button confirmButton;
     @InjectView(R.id.edit_time_block_cancel_button) private Button cancelButton;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         
         this.nameEditView = (EditText)findViewById(R.id.edit_time_block_name_input);
         this.typeSpinner = (Spinner)findViewById(R.id.edit_time_block_type_spinner);
         this.locationButton = (Button)findViewById(R.id.edit_time_block_location_button);
         this.startTimeSpinner = (Spinner)findViewById(R.id.edit_time_block_start_time_spinner);
         this.endTimeSpinner = (Spinner)findViewById(R.id.edit_time_block_end_time_spinner);
         this.confirmButton = (Button)findViewById(R.id.edit_time_block_confirm_button);
         this.cancelButton = (Button)findViewById(R.id.edit_time_block_cancel_button);
         
         if (savedInstanceState == null) {
 	        this.timeFormat = new SimpleDateFormat("hh:mm:ss aa");
 	        this.newTimeBlock = new TimeBlockData();
 	        
 	        // Get bundle passed from parent activity.
 	        Bundle extras = getIntent().getExtras();
 	        if (extras != null) {
 	        	// Set start and end times to selected time block by default.
 	        	this.newTimeBlock.startTime = extras.getString("startTime");
 	        	this.newTimeBlock.endTime = extras.getString("startTime");
 	        	this.newTimeBlock.scheduleId = extras.getLong("scheduleId");
 	        	this.newTimeBlock.day = extras.getInt("day");
 	        	
 	        	// Set the default geolocation to the last known location.
 	        	LocationManager locationManager = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
 	        	try {
 	        		String locationProvider = LocationManager.NETWORK_PROVIDER;
 	        		locationManager.getBestProvider(new Criteria(), false);
 	        		Location location = locationManager.getLastKnownLocation(locationProvider);
 	        		if (location != null) {
 	        			this.newTimeBlock.longitude = location.getLongitude();
 	        			this.newTimeBlock.latitude = location.getLatitude();
 	        		}
 	        	} catch (Exception e) {
 	        		e.printStackTrace();
 	        	}
 	        	
 	        	this.startTime = Calendar.getInstance();
 	        	try {
 					this.startTime.setTime(this.timeFormat.parse(extras.getString("startTime")));
 				} catch (ParseException e) {
 					e.printStackTrace();
 				}
 	        	
 	            //System.out.println("scheduleId: " + this.newTimeBlock.scheduleId +
 	            //		" startTime: " + this.timeFormat.format(this.startTime.getTime()) +
 	            //		" day: " + this.newTimeBlock.day);
 	            
 	            // Get all time blocks as per schedule and day.
 	            // Get all time block types.
 	            SchedulesDataSource dataSource = new SchedulesDataSource(this);
 	            dataSource.open();
 	            this.timeBlocks = dataSource.getScheduleDayTimeBlocks(this.newTimeBlock.scheduleId, this.newTimeBlock.day);
 	            this.blockTypes = dataSource.getAllBlockTypes();
 	            dataSource.close();
 	            
 	            // Check whether time block belongs to any existing time block.
 	            // Look through all time blocks and find first time blocks in which the
 	            // current time block belongs to.
 	            try {	    			
 	    			Calendar startTime = Calendar.getInstance();
 	    	        Calendar endTime = Calendar.getInstance();
 
 	    	        for (TimeBlockData b : this.timeBlocks) {
 	    	        	// Create calendar objects from the time block's start and end times.
 	    	        	startTime.setTime(this.timeFormat.parse(b.startTime));
 	    	        	endTime.setTime(this.timeFormat.parse(b.endTime));
 	    	        	
 	    	        	// Check for whether current time block falls into the start and end times.
 	    	        	if ((this.startTime.get(Calendar.HOUR_OF_DAY) >= startTime.get(Calendar.HOUR_OF_DAY)) &&
 	    	        		(this.startTime.get(Calendar.HOUR_OF_DAY) < endTime.get(Calendar.HOUR_OF_DAY))) {
 	    	        		this.newTimeBlock.id = b.id;
 	    	        		this.newTimeBlock.sid = b.sid;
 	    	        		this.newTimeBlock.name = b.name;
 	    	        		this.newTimeBlock.blockTypeId = b.blockTypeId;
 	    	        		this.newTimeBlock.startTime = b.startTime;
 	    	        		this.newTimeBlock.endTime = b.endTime;
 	    	        		this.newTimeBlock.longitude = b.longitude;
 	    	        		this.newTimeBlock.latitude = b.latitude;
 	    	        		
 	    	        		break;
 	    	        	}
 	    	        }
 	    		} catch (ParseException e) {
 	    			e.printStackTrace();
 	    		}
 	            
 	            // Initialize the name.
 	            if (this.newTimeBlock.name != null) {
 	            	this.nameEditView.setText(this.newTimeBlock.name);
 	            }
 	            
 	            // Extract block type names from hash for use in the adapter.
 	            List<BlockTypeData> blockTypeList = new ArrayList<BlockTypeData>(this.blockTypes.values());
 	            List<String> blockTypeNames = new ArrayList<String>();
 	            int defaultBlockTypeSpinnerPosition = 0;
 	            int i = 0;
 	            for (BlockTypeData b : blockTypeList) {
 	            	blockTypeNames.add(b.name);
 	            	
 	            	// Default block type shown in spinner.
 	            	if (b.name.equals(this.blockTypes.get(this.newTimeBlock.blockTypeId)))
 	            		defaultBlockTypeSpinnerPosition = i;
 	            	i++;
 	            }
 	            
 	            // Create adapter with block types and add to spinner.
 	            ArrayAdapter<String> typeAdapter = 
 	            		new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, blockTypeNames);
 	            typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 	            this.typeSpinner.setAdapter(typeAdapter);
 	            this.typeSpinner.setSelection(defaultBlockTypeSpinnerPosition);
 	            
 	            // Get default positions start and end time spinners.
 	            List<String> startEndTimes = new ArrayList<String>(Arrays.asList(EditScheduleActivity.TIME_DATA));
 	            int defaultStartTimeSpinnerPosition = 0;
 	            int defaultEndTimeSpinnerPosition = 0;
 	            Calendar theTime = Calendar.getInstance();
 	            Calendar selectedStartTime = Calendar.getInstance();
 	            Calendar selectedEndTime = Calendar.getInstance();
 	            i = 0;
 	            for (String time : startEndTimes) {
 	            	try {
 						theTime.setTime(this.timeFormat.parse(time));
 						selectedStartTime.setTime(this.timeFormat.parse(this.newTimeBlock.startTime));
 						selectedEndTime.setTime(this.timeFormat.parse(this.newTimeBlock.endTime));
 						String t = this.timeFormat.format(theTime.getTime());
 						
 						if (t.equals(this.timeFormat.format(selectedStartTime.getTime())))
 							defaultStartTimeSpinnerPosition = i;
 						
 						if (t.equals(this.timeFormat.format(selectedEndTime.getTime())))
 							defaultEndTimeSpinnerPosition = i;
 					} catch (ParseException e1) {
 						e1.printStackTrace();
 					}
 	            	
 	            	i++;
 	            }
 	            
 	            // Create adapters with start and end time spinners.
 	            ArrayAdapter<String> startTimeAdapter = 
 	            		new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, startEndTimes);
 	            ArrayAdapter<String> endTimeAdapter = 
 	            		new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, startEndTimes);
 	            typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 	            this.startTimeSpinner.setAdapter(startTimeAdapter);
 	            this.endTimeSpinner.setAdapter(endTimeAdapter);
 	            this.startTimeSpinner.setSelection(defaultStartTimeSpinnerPosition);
 	            this.endTimeSpinner.setSelection(defaultEndTimeSpinnerPosition);
 	        }
         }
     }
     
     @Override
     public void onActivityResult(int requestCode, int resultCode, Intent data) {
     	super.onActivityResult(requestCode, resultCode, data);
     	
     	// Called on map return.
     	if (requestCode == SelectLocationActivity.REQUEST_CODE) {
     		if (resultCode != Activity.RESULT_CANCELED) {
     			Bundle b = data.getExtras();
     			this.newTimeBlock.latitude = (b.getInt("latitude") + 0.0f) * 1e-6;
     			this.newTimeBlock.longitude = (b.getInt("longitude") + 0.0f) * 1e-6;
     			
     			System.out.println("select map result:" + 
     					" latitude: " + this.newTimeBlock.latitude +
     					" longitude: " + this.newTimeBlock.longitude);
     		}
     	}
     		
     }
     
     public void confirmEdit(View view) {
     	// Open database connection for changes.
     	SchedulesDataSource dataSource = new SchedulesDataSource(this);
     	dataSource.open();
     	
     	// Get name.
     	this.newTimeBlock.name = this.nameEditView.getText().toString();
     	
     	// Get block type id.
     	String blockTypeName = (String)this.typeSpinner.getSelectedItem();
     	this.newTimeBlock.blockTypeId = dataSource.getBlockTypeId(blockTypeName);
     	    	
     	// Convert all times to calendar format for comparison.
     	Calendar startTime = Calendar.getInstance();
     	Calendar endTime = Calendar.getInstance();
     	Calendar newBlockStartTime = Calendar.getInstance();
     	Calendar newBlockEndTime = Calendar.getInstance();
     	
     	try {
     		newBlockStartTime.setTime(this.timeFormat.parse((String)this.startTimeSpinner.getSelectedItem()));
 			newBlockEndTime.setTime(this.timeFormat.parse((String)this.endTimeSpinner.getSelectedItem()));
     		
     		for (TimeBlockData timeBlock : this.timeBlocks) {
         		// Create calendar objects from the time block's start and end times.        		
             	startTime.setTime(this.timeFormat.parse(timeBlock.startTime));
 				endTime.setTime(this.timeFormat.parse(timeBlock.endTime));
 				
 				// Check for whether current time block falls into the start and end times.
 				// Case where time block has to be deleted to make room for new time block.
 	        	if ((startTime.getTime().getTime() >= newBlockStartTime.getTime().getTime()) &&
 	        		(endTime.getTime().getTime() <= newBlockEndTime.getTime().getTime())) {
 	        		dataSource.deleteTimeBlock(timeBlock);
 	        	
 	        	// Case where start time of the block needs to be moved to make room for new block.
 	        	} else if ((startTime.getTime().getTime() >= newBlockStartTime.getTime().getTime()) &&
 	        			(startTime.getTime().getTime() < newBlockEndTime.getTime().getTime())) {
 	        		timeBlock.startTime = this.timeFormat.format(newBlockEndTime.getTime());
 	        		
 	        		System.out.println("EditTimeBlock: new start time: " + timeBlock.startTime);
 	        		dataSource.updateTimeBlock(timeBlock);
 	        		
 	        	// Case where the end time of the block needs to be moved to make room for new block.
 	        	} else if ((endTime.getTime().getTime() >= newBlockStartTime.getTime().getTime()) &&
 		        		(endTime.getTime().getTime() < newBlockEndTime.getTime().getTime())) {
 	        		timeBlock.endTime = this.timeFormat.format(newBlockStartTime.getTime());
 	        		
 	        		System.out.println("EditTimeBlock: new end time: " + timeBlock.endTime);
 	        		dataSource.updateTimeBlock(timeBlock);
 	        	}    	
     		}
     		
 	    	this.newTimeBlock.startTime = this.timeFormat.format(newBlockStartTime.getTime());
 	    	this.newTimeBlock.endTime = this.timeFormat.format(newBlockEndTime.getTime());
 	    	
 	    	System.out.println("EditTimeBlock: new block:");
 	    	System.out.println("sid: " + this.newTimeBlock.sid);
 	    	System.out.println("name: " + this.newTimeBlock.name);
 	    	System.out.println("start time: " + this.newTimeBlock.startTime);
 	    	System.out.println("end time: " + this.newTimeBlock.endTime);
 	    	System.out.println("day: " + this.newTimeBlock.day);
 	    	System.out.println("block type id: " + this.newTimeBlock.blockTypeId);
 	    	System.out.println("schedule id: " + this.newTimeBlock.scheduleId);
 	    	System.out.println("latitude: " + this.newTimeBlock.latitude);
 	    	System.out.println("longitude: " + this.newTimeBlock.longitude);
 	    	
 	    	// Delete existing if exists.
 	    	if (dataSource.isTimeBlockExists(this.newTimeBlock.id))
 	    		dataSource.deleteTimeBlock(this.newTimeBlock);
 	    	
 	    	// Create time block with all information.
 	    	dataSource.createTimeBlock(this.newTimeBlock.sid, this.newTimeBlock.name,
 	    			this.newTimeBlock.startTime, this.newTimeBlock.endTime, this.newTimeBlock.day,
 	    			this.newTimeBlock.blockTypeId, this.newTimeBlock.scheduleId,
 	    			this.newTimeBlock.longitude, this.newTimeBlock.latitude);
 		} catch (ParseException e) {
 			e.printStackTrace();
 		}    
     	
     	dataSource.close();
     	
     	Intent i = new Intent();
     	setResult(Activity.RESULT_OK, i);
     	finish();
     }
     
     public void cancelEdit(View view) {
     	finish();
     }
     
     public void selectLocation(View view) {
     	Intent intent = new Intent(this, SelectLocationActivity.class);
         startActivityForResult(intent, SelectLocationActivity.REQUEST_CODE);
     }    
 }
