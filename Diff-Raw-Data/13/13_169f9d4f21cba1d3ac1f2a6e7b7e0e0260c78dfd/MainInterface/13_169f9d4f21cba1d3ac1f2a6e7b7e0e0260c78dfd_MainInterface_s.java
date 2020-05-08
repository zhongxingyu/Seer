 // Copyright (C) 2011 Woelfware
 
 package com.woelfware.blumote;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.graphics.Color;
 import android.graphics.PorterDuffColorFilter;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.animation.Animation;
 import android.view.animation.AnimationUtils;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.Spinner;
 import android.widget.Toast;
 import android.widget.ViewFlipper;
 
 import com.woelfware.blumote.screens.Blank;
 import com.woelfware.blumote.screens.Main;
 import com.woelfware.blumote.screens.RokuActivity;
 import com.woelfware.blumote.screens.RokuDevice;
 
 /**
  * This class encapsulates several of the functions associated with dealing 
  * with the interface and buttons/dropdowns/lists
  * Understands the ButtonCreator interface
  * @author keusej
  *
  */
 public class MainInterface {
 	
 	// this keeps track of what state the interface is in
 	// this is useful for how to setup the options menus
 	// and what actions buttons should take when pressed
 	public enum INTERFACE_STATES {
 		MAIN, // default program state, when we are in a device mode
 		ACTIVITY, // when we are in a normal activity mode
 		ACTIVITY_EDIT, // when editing an activity (associating buttons/etc)
 		ACTIVITY_INIT, // when setting up the initialization of an activity
 		LEARN, // when learning a new button code
 	    RENAME_STATE // renaming misc button
 	}
 	
 	// current interface State of program
 	INTERFACE_STATES INTERFACE_STATE = INTERFACE_STATES.MAIN;
 	
 	// define the types of interfaces available
 	public enum TYPE {
 		DEVICE, ACTIVITY
 	}
 	
 	public int NUM_SCREENS = 3; // number of screens in the active layout
 	private String ACTIVE_BTN_CNFG = null;
 	private TYPE ACTIVE_BTN_TYPE = null;
 	
 	// all the device button layouts
 	public enum DEVICE_LAYOUTS {
 		// "name of interface", layout id, number of screens in layout, button class
 		BLANK("blank", R.layout.blank_interface, 2, Blank.class),
 		MAIN("main", R.layout.main_interface, 3, Main.class), 
 		ROKU("roku", R.layout.rokud_interface, 2, RokuDevice.class); // need to change to RokuDevice.class
 		private final String field;
 		private final int layout;
 		private final int screens;
 		private Class<? extends ButtonCreator> type;
 		DEVICE_LAYOUTS(String field, int layout, int screens, Class<? extends ButtonCreator> type) {
 			this.field = field;
 			this.layout = layout;
 			this.screens = screens;
 			this.type = type;
 		}
 		public String getValue() {
 			return field;
 		}
 		public int getLayout() {
 			return layout;
 		}
 		public int getScreens() {
 			return screens;
 		}
 		public ButtonCreator getInstance() {
 			try {
 				return type.newInstance();
 			} catch (InstantiationException e) {
 				e.printStackTrace();
 				return null;
 			} catch (IllegalAccessException e) {
 				e.printStackTrace();
 				return null;
 			}
 		}
 	}
 		
 	// all the activity button layouts - in case they ever differ from devices
 	public enum ACTIVITY_LAYOUTS {
 		// "name of interface", layout id, number of screens in layout, button class
 		MAIN("main", R.layout.main_interface, 3, Main.class), 
 		ROKU("roku", R.layout.rokua_interface, 2, RokuActivity.class);
 		private final String field;
 		private final int layout;
 		private final int screens;
 		private Class<? extends ButtonCreator> type;
 		ACTIVITY_LAYOUTS(String field, int layout, int screens, Class<? extends ButtonCreator> type) {
 			this.field = field;
 			this.layout = layout;
 			this.screens = screens;
 			this.type = type;
 		}
 		public String getValue() {
 			return field;
 		}
 		public int getLayout() {
 			return layout;
 		}
 		public int getScreens() {
 			return screens;
 		}
 		public ButtonCreator getInstance() {
 			try {
 				return type.newInstance();
 			} catch (InstantiationException e) {
 				e.printStackTrace();
 				return null;
 			} catch (IllegalAccessException e) {
 				e.printStackTrace();
 				return null;
 			}
 		}
 	}
 	
 	/*********************************
 	 * Common screen elements
 	 *********************************/
 	View toplevel; // the top-level linearlayout/relativelayout for the interface
 	// pager is for keeping track of what page we are on
 	ImageView pager;
 	// helps change interface pages
 	ViewFlipper flip;	
 	// viewflipper animations
 	Animation slide_right_anim;
 	Animation slide_left_anim;
 	Animation slide_right_out_anim;
 	Animation slide_left_out_anim;
 	
 	private Spinner device_spinner;
 	
 	// keep track of what the active page of buttons is
 	enum Pages {
 		MAIN, NUMBERS, ACTIVITIES
 	}
 
 	Pages page = Pages.ACTIVITIES;
 	
 	// Special Button, must be on EVERY activity, so declared here
 	// instead of in the specific button class definition
 	private  ImageButton power_off_btn = null;
 	// special button, if button is present we need to record button
 	// presses when in activity training mode, so that we can use
 	// power_off_btn to turn all the devices off
 	private  ImageButton power_on_btn = null;
 	
 	/*************************
 	 * ACTIVITIES SCREEN
 	 *************************/
 	private Button add_activity_btn;
     ListView activitiesListView; 
 	 
 	static final String BTN_MISC = "btn_misc";
 
     // this has to match the number of misc buttons in the interface
     // we use this for the renaming of misc buttons logic
     static final int NUM_MISC_BTNS = 8;
    
     // array adapter for the drop-down spinner
 	private ArrayAdapter<String> spinnerAdapter;   
     
     private  BluMote blumote;
     
     HashMap<Integer,String> button_map = null;
     
     private Activities activities;
     
     /**
      * public constructor
      * @param d the BluMote object to work with
      */
 	public MainInterface(BluMote d) {
 		blumote = d;
 	}
 	
 	// prefix to put before a new activity to prefs file to find it easier
 	// when searching through keys
 	static final String ACTIVITY_PREFIX = "(A)_";
 	static final String ACTIVITY_PREFIX_SPACE = "(A) ";
 	
 	/**
 	 * Must be called before any initialize functions are called so that we have a handle
 	 * to the activities framework object.
 	 * @param a
 	 */
 	public void setActivities(Activities a) {
 		activities = a;
 	}
 	
 	/**
 	 * This function will setup the layout based on the passed in button config name
 	 * @param buttonConfig the name of the button config, must be one of the MainInterface
 	 * deviceLayouts[] or activityLayouts[]
 	 * @param type the type of interface which is a member of the local enum TYPE
 	 */
 	public void initializeInterface(String buttonConfig, TYPE type) {			
 		if (buttonConfig == null) {
 			// then set a blank screen....
 			buttonConfig = "blank";
 			type = TYPE.DEVICE;
 		} 
 
 		// only execute the body of this function if the interface type
 		// has changed from before.  This is a performance improvement over always reloading.
 		if (ACTIVE_BTN_CNFG == null || ACTIVE_BTN_TYPE == null ||  
 				!(ACTIVE_BTN_CNFG.equals(buttonConfig)) ||	ACTIVE_BTN_TYPE != type) { 
 			ACTIVE_BTN_CNFG = buttonConfig.toString(); // create copy and save
 			ACTIVE_BTN_TYPE = type; // record last type used
 
 			button_map = new HashMap<Integer,String>();	
 
 			ButtonCreator instance = null;
 
 			if (type == TYPE.ACTIVITY) {
 				for (ACTIVITY_LAYOUTS i : ACTIVITY_LAYOUTS.values()) {
 					if (buttonConfig.equals(i.getValue())) {
 						blumote.setContentView(i.getLayout());
 						NUM_SCREENS = i.getScreens();
 						instance = i.getInstance();
 						break;
 					}			
 				}
 			} else {
 				for (DEVICE_LAYOUTS i : DEVICE_LAYOUTS.values()) {
 					if (buttonConfig.equals(i.getValue())) {
 						blumote.setContentView(i.getLayout());
 						NUM_SCREENS = i.getScreens();
 						instance = i.getInstance();
 						break;
 					}			
 				}
 			}
 			// reset page to far left screen
 			page = Pages.ACTIVITIES;
 
 			// initialize interface items that are always present in a layout
 			// namely, toplevel, pager and flipper and spinner
 			toplevel = blumote.findViewById(R.id.toplevel);
 			pager = (ImageView)blumote.findViewById(R.id.pager);
 			flip=(ViewFlipper)blumote.findViewById(R.id.flipper); // flips between our screens
 			flip.setOnTouchListener(blumote.gestureListener);
 			slide_right_anim = AnimationUtils.loadAnimation(blumote, R.anim.slide_right);
 			slide_left_anim = AnimationUtils.loadAnimation(blumote, R.anim.slide_left);
 			slide_left_out_anim = AnimationUtils.loadAnimation(blumote, R.anim.slide_left_out);
 			slide_right_out_anim = AnimationUtils.loadAnimation(blumote, R.anim.slide_right_out);
 
 			/*************************************
 			 * ACTIVITIES SCREEN
 			 * ************************************/
 			// Find and set up the ListView
 			activitiesListView = (ListView) blumote.findViewById(R.id.activities_list);
 			activitiesListView.setAdapter(activities.mActivitiesArrayAdapter);
 			activitiesListView.setOnItemClickListener(blumote);
 			activitiesListView.setOnTouchListener(blumote.gestureListener);
 			blumote.registerForContextMenu(blumote.findViewById(R.id.activities_list));
 
 			// setup activity add button
 			add_activity_btn = (Button) blumote.findViewById(R.id.add_activity_btn);
 			add_activity_btn.setOnClickListener(new OnClickListener() {
 				public void onClick(View v) {
 					// first clear the arraylist that keeps track if initialization entries
 					blumote.activityInit.clear();
 					// Launch the function to ask for a name for device
 					Intent i = new Intent(blumote, CreateActivity.class);
 					blumote.startActivityForResult(i, BluMote.ACTIVITY_ADD);
 				}
 			});
 
 			// populate activities arraylist with initial items
 			// need to pass in the arrayadapter we want to populate
 			Activities.populateImageActivities(activities.mActivitiesArrayAdapter, blumote.prefs); 
 
 			/*************************************
 			 * SPINNER SETUP
 			 * ************************************/
 			device_spinner = (Spinner) blumote.findViewById(R.id.device_spinner);
 			spinnerAdapter = new ArrayAdapter<String>(blumote, R.layout.spinner_entry);
 			spinnerAdapter.setDropDownViewResource(R.layout.spinner_entry);
 			device_spinner.setAdapter(spinnerAdapter);
 			device_spinner.setOnItemSelectedListener(blumote);	
 
 			// need to call this, since if we install a new interface then the
 			// drop-down gets blown away and needs re-creating....
 			// the setDropDown() should set "lastDevice" appropriately so this works
 			restoreSpinner();
 
 			initializeButtons(instance); // initialize all buttons on interface
 
 			moveRight(); // start out one screen to the right (main)
 		}
 
 	}
 	
 	void refreshInterfaceBackground() {		
 		// set background based on mode of operation
 		try {
 			switch (INTERFACE_STATE) {
 			case MAIN:				
 				toplevel.setBackgroundResource(R.drawable.background_gry_scaled);
 				setButtonBackground(false);
 				break;
 
 			case ACTIVITY:
 				toplevel.setBackgroundResource(R.drawable.background_gry_scaled);				
 				setButtonBackground(false);
 				break;
 
 			case ACTIVITY_EDIT:
 				toplevel.setBackgroundResource(R.drawable.background_bl_scaled);
 				setButtonBackground(false);
 				break;
 
 			case ACTIVITY_INIT:				
 				toplevel.setBackgroundResource(R.drawable.background_bl_scaled);
 				setButtonBackground(false);
 				break;
 
 			case LEARN:
 				toplevel.setBackgroundResource(R.drawable.background_gr_scaled);
 				//TODO set button background colors for buttons with no button code
 				setButtonBackground(true);
 				break;
 
 			case RENAME_STATE:
 				toplevel.setBackgroundResource(R.drawable.background_gry_scaled);
 				setButtonBackground(false);
 				break;
 			}
 		} catch (Exception e) {}
 	}
 	
 	void setInterfaceState(INTERFACE_STATES state) {
 		INTERFACE_STATE = state;
 		
 		refreshInterfaceBackground();
 	}
 	
 	TYPE getInterfaceType() {
 		return ACTIVE_BTN_TYPE;
 	}
 	
 	/**
 	 * Sets a specific button resource ID to a colorized or regular state
 	 * @param butonID resource ID
 	 * @param setColor true if button to be colorized, false if not
 	 */
 	void setButtonBackground(boolean setColor, int buttonID) {
 		try {
 			View v = blumote.findViewById(buttonID);
 			
 			if (setColor == true) {			 
 				// mutate() to avoid changing all of the same drawable
 				v.getBackground().mutate().setColorFilter(
 					0xFFFF0000, android.graphics.PorterDuff.Mode.MULTIPLY);
 			} else {
 				v.getBackground().mutate().clearColorFilter();
 			}
 		} catch (Exception e) {
 			// a failure here is not critical
 		}		
 	}
 	
 	/**
 	 * Sets button background to RED if true and default if false
 	 * will iterate through all buttons on the interface
 	 * @param setColor
 	 */
 	void setButtonBackground(boolean setColor) {		
 		//TODO		
 		ButtonCreator interfaceInstance = null;
 		
 		if (ACTIVE_BTN_TYPE == TYPE.ACTIVITY) {
 			for (ACTIVITY_LAYOUTS i : ACTIVITY_LAYOUTS.values()) {
 				if (ACTIVE_BTN_CNFG.equals(i.getValue())) {
 					interfaceInstance = i.getInstance();
 					break;
 				}			
 			}
 		} else {
 			for (DEVICE_LAYOUTS i : DEVICE_LAYOUTS.values()) {
 				if (ACTIVE_BTN_CNFG.equals(i.getValue())) {
 					interfaceInstance = i.getInstance();
 					break;
 				}			
 			}
 		}
 
 		// iterate through buttons, those with no IR code attached get set background
 		// color if method param is set to true
 		//TODO
 		ButtonParameters[] buttons = interfaceInstance.getButtons(blumote);		 
 		byte[] btnData = null;
 		if (buttons != null) {
 			for (int i=0; i<buttons.length; i++) {
 				View v = buttons[i].getView();				
 				if (setColor == true) {
 					btnData = getButtonCode(buttons[i].getID());
 					if (btnData == null) { 
 						// mutate() to avoid changing all of the same drawable
 						v.getBackground().mutate().setColorFilter(
 							0xFFFF0000, android.graphics.PorterDuff.Mode.MULTIPLY);
 					} else {
 						v.getBackground().mutate().clearColorFilter();
 					}
 					
 				} else { // if false then clear color filter									
 					v.getBackground().clearColorFilter();
 					//v.setBackgroundColor(android.graphics.Color.TRANSPARENT);
 				}								
 			}		
 		}
 	}
 	
 	/**
 	 * Pass in button resource ID and the byte[] IR code is returned otherwise returns null.
 	 * @param buttonID
 	 * @return
 	 */
 	byte[] getButtonCode(int buttonID) {		
 		if (blumote.buttons != null && blumote.buttons.length > 0) {
 			String buttonName = button_map.get(buttonID);
 			if (buttonName != null) {
 				for (int i=0; i < blumote.buttons.length; i++) {
 					if ( buttonName.equals(blumote.buttons[i].getButtonName()) ) {
 						// then extract the button data out to be sent, check if data is non-null
 						byte[] code = blumote.buttons[i].getButtonData();
 						if (code != null) {
 							return code;
 						}
 					}				 
 				}
 			}
 		}
 		
 		return null; // presumably the button code is not present
 	}
 	
 	/**
 	 * This method will initialize the button elements of the interface
 	 */
 	public void initializeButtons(ButtonCreator type) {
 		ButtonParameters[] buttons = type.getButtons(blumote);
 		button_map.clear(); // start out clear
 		 
 		if (buttons != null) {
 			for (int i=0; i<buttons.length; i++) {
 				View v = buttons[i].getView();
 				v.setOnTouchListener(blumote.gestureListener);
 				v.setOnClickListener(blumote);
 				button_map.put(buttons[i].getID(), buttons[i].getName());
 			}		
 		}
 		// save a reference to power_on_btn and power_off_btn since they are used elsewhere in 
 		// this class.  Null return values are fine and acceptable if these buttons are not present.
 		power_off_btn = type.getPowerOffBtn();
 		power_on_btn = type.getPowerOnBtn();
 	}
 	
 	/**
 	 * move screen to the left	
 	 */
 	void moveLeft() {
 		BluMote.BUTTON_LOOPING = false;
 		
 		// setup flipper animations
 		flip.setInAnimation(slide_right_anim); // -100 -> 0
 		flip.setOutAnimation(slide_left_out_anim); // 0 -> 100
 		
 		switch (page) {
 		case MAIN:
 			flip.showPrevious();
 			page = Pages.ACTIVITIES;
 			// set pager to left
 			if (NUM_SCREENS == 3) {
 				pager.setImageDrawable(blumote.getResources().getDrawable(R.drawable.left_circle));
 			} else if (NUM_SCREENS == 2) {
 				pager.setImageDrawable(blumote.getResources().getDrawable(R.drawable.left_circle_two));
 			}
 			return;
 
 		case ACTIVITIES:
 			return;
 
 		case NUMBERS:
 			// NUMBERS can only be achieved if we have at least 3 screens,
 			// so no need for custom logic for 2 screens
 			flip.showPrevious();
 			page = Pages.MAIN;
 			// set pager to center
 			pager.setImageDrawable(blumote.getResources().getDrawable(R.drawable.middle_circle));
 			return;
 		}
 	}
 	
 
 	/**
 	 * move screen to the right
 	 */
 	void moveRight() {
 		BluMote.BUTTON_LOOPING = false;
 		
 		// setup flipper animations
 		flip.setInAnimation(slide_left_anim); // 100 -> 0
 		flip.setOutAnimation(slide_right_out_anim); // 0 -> -100
 		//mainScreen.flip.setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_right));
 		
 		switch (page) {
 		case MAIN:
 			if (NUM_SCREENS == 3) {
 				flip.showNext();
 				page = Pages.NUMBERS;
 				// set pager to right
 				pager.setImageDrawable(blumote.getResources().getDrawable(R.drawable.right_circle));
 			}
 			return;
 
 		case ACTIVITIES:
 			flip.showNext();
 			page = Pages.MAIN;
 			// set pager to middle
 			if (NUM_SCREENS == 3) {			
 				pager.setImageDrawable(blumote.getResources().getDrawable(R.drawable.middle_circle));
 			} else if (NUM_SCREENS == 2) {
 				pager.setImageDrawable(blumote.getResources().getDrawable(R.drawable.right_circle_two));
 			}
 			return;
 
 		case NUMBERS:
 			return;
 		}
 	}
 	
 	
 	/**
 	 * called first time program initializes, just determines what the last used
 	 * device was and then sets the drop-down to that item
 	 */
 	void restoreSpinner() {
 		populateDropDown();
 		// set spinner to default from last session if possible		
 		String prefs_table = blumote.prefs.getString("lastDevice", null);
 		if (prefs_table != null) {
 			prefs_table = prefs_table.replaceAll("_", " ");		
 			for (int i = 0; i < device_spinner.getCount(); i++) {
 				if (prefs_table.equals( (String)device_spinner.getItemAtPosition(i) ) ) {
 					device_spinner.setSelection(i);
 					break;
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Checks if the button is one for navigation (move left or right)
 	 * THIS IS CURRENTLY DEPRACATED SINCE NAVIGATION BUTTONS WERE REMOVED FROM INTERFACE
 	 * @param buttonID the button we want to check
 	 * @return true if it is a navigation button, false if not
 	 */
 	boolean isNavigationButton(int buttonID) {
 		String payload = button_map.get(buttonID);
 		if (payload != null) {
 			try {
 				// see if we have a navigation page move command....
 				if (payload == "move_left_btn") {
 					return true;
 				}
 				// check if the navigation move_right was pushed
 				// this only works when we are in main screen
 				if (payload == "move_right_btn") {
 					return true;
 				}
 			} catch (Exception e) {
 				// do nothing, this is fine
 				return false;
 			}
 		}
 		return false;
 	}
 	
 	/**
 	 * Get the button hashmap 
 	 * @return the button_map hashmap of all buttons on the interface
 	 */
 	public HashMap<Integer,String> getButtonMap() {
 		return button_map;
 	}		
 	
 	/**
 	 * sets up the drop-down list, pulls rows from DB to populate
 	 */
 	void populateDropDown() {
 		spinnerAdapter.clear(); // clear before adding
 		String[] devices = blumote.device_data.getDevices();
 		if (devices != null) {			
 			for (int i= 0 ; i< devices.length; i++) {
 				spinnerAdapter.add(devices[i]);
 			}
 
 			//put activities into drop-down
 			Activities.populateActivities(spinnerAdapter, blumote.prefs);
 		}
 	}
 	
 	/**
 	 * Turns the visibility of the drop-down selection on or off.
 	 * @param visible true shows the drop-down
 	 */
 	void setDropDownVis(boolean visible) {
 		// toggles the drop-down visibility
 		if (visible) {
 			device_spinner.setVisibility(View.VISIBLE);
 		}
 		else {
 			device_spinner.setVisibility(View.INVISIBLE);
 		}
 	}
 	
 	/**
 	 * sets dropdown to the item indicated by string parameter
 	 * @param s the item that we want to set the drop-down to
 	 */
 	void setDropDown(String s) {
 		s = s.replace(ACTIVITY_PREFIX, ACTIVITY_PREFIX_SPACE);
 		if (blumote.LOCK_LAST_DEVICE != true) {
 			// save 'lastdevice' for next time						
 			Editor mEditor = blumote.prefs.edit();
 			mEditor.putString("lastDevice", s);
 			mEditor.commit();
 		}
 		
 		for (int i = 0; i < device_spinner.getCount(); i++) {
 			if (s.equals((String)device_spinner.getItemAtPosition(i))) {
 				device_spinner.setSelection(i);
 				break;
 			}
 		}								
 		
 		// update buttons
 		fetchButtons();				
 		
 		refreshInterfaceBackground();
 	}
 	
 	/**
 	 * The currently selected drop down item is returned
 	 * @return the currently selected drop down
 	 */
 	String getCurrentDropDown() {
 		return (String)device_spinner.getSelectedItem();		
 	}			
 	
 	/**
 	 * This function updates the data associated with buttons on the interface, needs 
 	 * to be called anytime that data is changed.
 	 */
 	void fetchButtons() {
 		// first update the cur_table from spinner
 		if (device_spinner.getCount() > 0) {
 			Object spinnerItem;
 			try {
 				spinnerItem = getCurrentDropDown();
 			} catch (Exception e) {
 				spinnerItem = null;				
 				setSpinnerErrorState();
 			}
 			if (spinnerItem != null) {
 				// set cur_table				
 				String spinner_selected = spinnerItem.toString();
 				blumote.cur_device = spinner_selected;
 				
 				String buttonConfig;
 				if (spinner_selected.startsWith(ACTIVITY_PREFIX_SPACE)) {
 					// update interface if needed
 					buttonConfig = activities.getButtonConfig(spinner_selected);
 					initializeInterface(buttonConfig, MainInterface.TYPE.ACTIVITY);
 					
 					// show the "power off" button, hide the power on button					
 					if (power_off_btn != null) {
 						power_off_btn.setVisibility(View.VISIBLE);
 					}
 					if (power_on_btn != null) {
 						power_on_btn.setVisibility(View.INVISIBLE);
 					}
 				
 					blumote.activityPowerOffData = activities.getPowerOffButtonData(spinner_selected);										
 				}
 				else {
 					// update interface if needed
 					buttonConfig = blumote.device_data.getButtonConfig(spinner_selected);
 					initializeInterface(buttonConfig, MainInterface.TYPE.DEVICE);
 					
 					// hide the power off button, show the power on button
 					if (power_on_btn != null) {
 						power_on_btn.setVisibility(View.VISIBLE);
 					}
 					if (power_off_btn != null) {
 						power_off_btn.setVisibility(View.INVISIBLE);
 					}
 				}
 				
 				// Fetch actual button codes, do this for "activity" or "main" interface states
 				// if we are in ACTIVITY or MAIN modes then toggle between them when
 				// changing devices in the drop-down
 				if (INTERFACE_STATE == INTERFACE_STATES.ACTIVITY ||
 						INTERFACE_STATE == INTERFACE_STATES.MAIN) {
 					// check if activity or a device
 					if (spinner_selected.startsWith(ACTIVITY_PREFIX_SPACE)) {
 						setInterfaceState(INTERFACE_STATES.ACTIVITY);
 						activities.setWorkingActivity(spinner_selected);
 						blumote.buttons = activities.getActivityButtons(spinner_selected);								
 					}
 					else { // must be a device
 						setInterfaceState(INTERFACE_STATES.MAIN);					
 						blumote.buttons = blumote.device_data.getButtons(blumote.cur_device);					
 					}
 				}
 				// refresh the text on the misc buttons
 				refreshMiscBtns(spinner_selected); 
 			} else {
 				setSpinnerErrorState();								
 			}
 		} else {
 			setSpinnerErrorState();
 		}				
 	}			
 	
 	/** 
 	 * This function renames all the misc buttons associated with the old name to the new name
 	 * @param oldName the original name the keys would have been stored with
 	 * @param newName the new name we want to use	
 	 * @param prefs the SharedPreferences object that the misc button data is stored in
 	 */
 	static void renameMiscButtons(String oldName, String newName, SharedPreferences prefs) {
 		//iterate through all oldName items, if we find the item, then copy the data and 
 		// delete the old and insert with new name
 		Editor mEditor = prefs.edit();
 		String miscButton;
 		for (int i=0; i< NUM_MISC_BTNS; i++) {
 			miscButton = prefs.getString(
 					oldName + MainInterface.BTN_MISC+Integer.toString(i), null);
 			if (miscButton != null) {
 				mEditor.remove(oldName + MainInterface.BTN_MISC+Integer.toString(i));
 				mEditor.putString(newName + MainInterface.BTN_MISC+Integer.toString(i), miscButton);
 			}
 		}
 		mEditor.commit();
 	}
 	
 	/** 
 	 * This function removes all the misc buttons associated with the name
 	 * @param name the name of the activity
 	 * @param newName the new name we want to use	
 	 * @param prefs the SharedPreferences object that the misc button data is stored in
 	 */
 	static void deleteMiscButtons(String name, SharedPreferences prefs) {
 		//iterate through all oldName items, if we find the item, then copy the data and 
 		// delete the old and insert with new name
 		Editor mEditor = prefs.edit();
 		String miscButton;
 		for (int i=0; i< NUM_MISC_BTNS; i++) {
 			miscButton = prefs.getString(
 					name + MainInterface.BTN_MISC+Integer.toString(i), null);
 			if (miscButton != null) {
 				mEditor.remove(name + MainInterface.BTN_MISC+Integer.toString(i));
 			}
 		}
 		mEditor.commit();
 	}
 	
 	/**
 	 * sets spinner to a default error condition
 	 */
 	void setSpinnerErrorState() {
 		blumote.buttons = null;
 		setInterfaceState(INTERFACE_STATES.MAIN);
 		
 		// setup interface to be 'blank'
 		initializeInterface(DEVICE_LAYOUTS.BLANK.getValue(), MainInterface.TYPE.DEVICE);								
 	}
 	
 	/**
 	 * Updates the text associated with a Misc button on the interface
 	 * @param return_string is the new name of the Misc button
 	 * @param misc_button The misc_button name to be operated on
 	 */
 	void renameMisc(String return_string, String misc_button) {
 		// this function should rename the misc button
 		// work with the preferences file for this
 		// after update preferences then refresh the misc buttons
 		Editor mEditor = blumote.prefs.edit();
 		
 		if (getCurrentDropDown() != null) {
 			// the drop down will help create the key
 			misc_button = getCurrentDropDown() + misc_button;
 			mEditor.putString(misc_button, return_string); // key, value
 			mEditor.commit();
 			refreshMiscBtns(getCurrentDropDown()); // update interface with new label
 		} else {
 			// tell user he must have an active device/activity selected in the dropdown
 			Toast.makeText(blumote, "Can't rename a button with no active device or activity!",
 					Toast.LENGTH_SHORT).show();
 		}
 		
 		
 	}	
 	
 	/**
 	 * This function retrieves the underlying android resource ID associated 
 	 * with a View based on the name of the item as defined in the interface.
 	 * @param name the name of the View
 	 * @return the integer id of that View
 	 */
 	int getResourceFromString(String name) {
 		return blumote.getResources().getIdentifier(name,"id",blumote.getPackageName());		
 	}
 	
 	/**
 	 * Refresh the misc buttons on the interface, pulls in any modified text labels that may have been set
 	 */
 	void refreshMiscBtns(String dropDown) {
 		// for refreshing the misc buttons from the preferences file
 		// call this when the program first is launched and after renaming any of them
 				
 		if (dropDown != null) {
 			String miscButton;
 			for (int i=1; i<= NUM_MISC_BTNS; i++) {
 				miscButton = blumote.prefs.getString(
 						dropDown + MainInterface.BTN_MISC+Integer.toString(i), null);
 				if (miscButton != null) {
 					// update btn on interface with text value from prefs file
 					try {
 						Button btn = (Button)blumote.findViewById(
 								getResourceFromString(MainInterface.BTN_MISC+Integer.toString(i)));					
 						btn.setText(miscButton);
 					} catch (Exception e) {
 						// oops something didn't work, oh well
 					}
 				} else {
 					// if prefs getString() returns null then lets restore the default misc button
 					// text - need this so when changing drop-down it restores default text
 					try {
 						Button btn = (Button)blumote.findViewById(
 								getResourceFromString(MainInterface.BTN_MISC+Integer.toString(i)));					
 						btn.setText("Misc "+Integer.toString(i));
 					} catch (Exception e) {
 						// oops something didn't work, oh well
 					}
 				}
 					//button_map.get(misc_btn)
 			}
 		}				
 	}	
 
 	/**
 	 * Returns an array that contains the devices that are currently setup
 	 * Note that the return value excludes activities, only devices are returned
 	 * @return the CharSequence[] representing all the devices currently known
 	 */
 	CharSequence[] getDropDownDevices() {
 		ArrayList<String> list = new ArrayList<String>();
 		if (device_spinner.getCount() > 0) {
 			for (int i=0; i < device_spinner.getCount(); i++) {
 				String s = (String)device_spinner.getItemAtPosition(i);
 				if (!s.startsWith(ACTIVITY_PREFIX_SPACE)) { 
 					// if not an activity then add to the stringbuilder
 					list.add(s);
 				}
 			}
 		}
 		CharSequence[] returnData = list.toArray(new CharSequence[list.size()]);
 		
 		return returnData;
 	}		
 }
 
