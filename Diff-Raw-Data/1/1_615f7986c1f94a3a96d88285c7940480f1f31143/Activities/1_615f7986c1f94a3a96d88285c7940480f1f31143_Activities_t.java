 // Copyright (C) 2011 Woelfware
 
 package com.woelfware.blumote;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.os.CountDownTimer;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.ImageView;
 import android.widget.TextView;
 import android.widget.Toast;
 /**
  * This class handles activities which are created to 
  * allow combining devices on the interface - like "watch a DVD"
  * and also allows for initialization sequences to be sent in a sequence
  * The data is persisted in the SharedPreferences object 
  * @author keusej
  *
  */
 public class Activities {
 	protected static boolean timerReady = true;
 	private final String TAG = "Activities";
 
 	private MainInterface mainint = null;
 	private BluMote blumote = null;
 	private Pod pod = null;
 	ImageArrayAdapter mActivitiesArrayAdapter = null;
 	private String workingActivity = null;
 	
 	// prefix to put before a new activity to prefs file to find it easier
 	// when searching through keys
 	static final String ACTIVITY_PREFIX = "(A)_";
 	static final String ACTIVITY_PREFIX_SPACE = "(A) ";
 	
 	// SUFFIX for initialization items
 	private static final String INIT = "INIT";
 	// SUFFIX for power off codes to store
 	private static final String OFF = "_OFF";
 	// SUFFIX for image associated with activity
 	private static final String IMAGEID = "IMAGE#";
 	// SUFFIX for Button Configuration associated with activity
 	private static final String BTNCONFIG = "BTNCONFIG";
 	
 	// these member variables will deal with executing all the 
 	// initialization steps
 	private int initItemsIndex = 0;	
 	private ArrayList<String[]> initItems = null;
 	
 	// these member variables will deal with sending the power off codes for an activity
 	private ButtonData[] powerOffData = null;
 	private int powerOffDataIndex = 0;
 	
 	/**
 	 * 
 	 * @param blumote main BluMote object to reference
 	 * @param mainint MainInterface object to reference
 	 */
 	public Activities(BluMote blumote, MainInterface mainint, Pod pod) {
 		this.mainint = mainint;
 		this.blumote = blumote;
 		this.pod = pod;
 		
 		// Initialize array adapter
 		mActivitiesArrayAdapter = new ImageArrayAdapter(blumote, R.layout.activities_item);
 	}
 	
 	/**
 	 * Get the activities from prefs file as an ArrayList<ImageActivityItem>
 	 * @param suppressPrefix true if we want to remove the activity prefix ACTIVITY_PREFIX, 
 	 * false if we don't want to suppress when adding to the ArrayAdapter
 	 * @param prefs
 	 * @return
 	 */
 	static ArrayList<ImageActivityItem> getImageActivities(boolean suppressPrefix, SharedPreferences prefs) {
 		Map<String,?> values = prefs.getAll();
 		ArrayList<ImageActivityItem> items = new ArrayList<ImageActivityItem>();
 		
 		// iterate through these values
 		for (String item : values.keySet()) {
 			// check if prefix is an activity and this is not an initialization item
 			if (item.startsWith(ACTIVITY_PREFIX)) {
 				// make sure it is not a misc button item				
 				boolean foundIt = false;
 				for (int i=0; i< MainInterface.NUM_MISC_BTNS; i++) {
 					if (item.endsWith(MainInterface.BTN_MISC + Integer.toString(i))) {
 						foundIt = true;
 						break;
 					}
 				}
 				if (foundIt) {
 					continue;
 				}
 				
 				// if we got here we have a valid item
 				
 				// see if there is an image associated with it
 				int imageIndex = prefs.getInt(formatActivityImageIdSuffix(item), 0);
 				
 				if (suppressPrefix == true) {					
 					// remove the prefix
 					item = item.replace(ACTIVITY_PREFIX, "");
 				}			
 				
 				// add it to arraylist
 				items.add(new ImageActivityItem(CreateActivity.getImageId(imageIndex), item)); 
 			}
 		}
 		return items;
 	}
 	
 	/**
 	 * updates the arrayadapter parameter with all the activities from the
 	 * prefs file.  
 	 * @param adapter the ArrayAdapter that we want to add activities to
 	 * @param prefs the SharedPreferences object that we used to store activities in
 	 * 
 	 */
 	static void populateActivities(ArrayAdapter<String> adapter, SharedPreferences prefs) {
 		ArrayList<ImageActivityItem> items = getImageActivities(false, prefs);
 		for (int i=0; i < items.size(); i++) {
 			adapter.add(items.get(i).title.replace("(A)_","(A) ")); // just add title to adapter
 		}
 	}
 	
 	/**
 	 * updates the arrayadapter parameter with all the activities from the
 	 * prefs file.  
 	 * @param adapter the ArrayAdapter that we want to add activities to
 	 * @param prefs the SharedPreferences object that we used to store activities in
 	 * 
 	 */
 	static void populateImageActivities(ArrayAdapter<ImageActivityItem> adapter, SharedPreferences prefs) {
 		adapter.clear(); // clear before re-populating
 		ArrayList<ImageActivityItem> items = getImageActivities(true, prefs);
 		for (int i=0; i < items.size(); i++) {
 			adapter.add(items.get(i)); 
 		}
 	}
 	
 	/**
 	 * sets the working activity.  The Activities class depends on this being set prior to usage
 	 * of any member functions.
 	 * @param key The name of the activity that we want to work with
 	 */
 	public void setWorkingActivity(String key) 	{
 		workingActivity = addActivityPrefix(key);
 	}
 	
 	/**
 	 * Receive the working activity that we set with setWorkingActivity()
 	 * @return The working activity is returned
 	 */
 	public String getWorkingActivity() {
 		return workingActivity;
 	}
 	
 	 
 	/**
 	 * this function will make sure the activity prefix is 
 	 * attached to the key passed in, this format is necessary for
 	 * saving activity data except activity-INIT data.
 	 * @param key the name of the activity that we want to operate on
 	 * @return the processed key
 	 *  
 	 */
 	private static String addActivityPrefix(String key) {
 		// prepend prefix if it doesn't already exist
 		if (key.startsWith(ACTIVITY_PREFIX)) {
 			return key;
 		} else if (key.startsWith(ACTIVITY_PREFIX_SPACE)) {
 			return key = key.replace(ACTIVITY_PREFIX_SPACE, ACTIVITY_PREFIX);
 		}
 		else {
 			return ACTIVITY_PREFIX + key;
 		}
 	}
 	
 	/**
 	 * this function will make sure the activity prefix is 
 	 * removed from the key passed in, this format is necessary for
 	 * saving some activity data that needs to be hidden from the user.
 	 * @param key the name of the activity that we want to operate on
 	 * @return the processed key
 	 *  
 	 */
 	@SuppressWarnings("unused")
 	private static String removeActivityPrefix(String key) {
 		// remove prefix if it doesn't already exist
 		if (key.startsWith(ACTIVITY_PREFIX)) {
 			return key.replace(ACTIVITY_PREFIX, "");
 		} else if (key.startsWith(ACTIVITY_PREFIX_SPACE)) {
 			return key = key.replace(ACTIVITY_PREFIX_SPACE, "");
 		}
 		else {
 			return key;
 		}
 	}
 
 	/**
 	 * This function will make sure the activity prefix is 
 	 * removed from the key passed in and add the INIT suffix
 	 * @param key the name of the activity we want to operate on
 	 * @return the processed activity name that we passed in
 	 */
 	private static String formatActivityInitSuffix(String key) {
 		if (key.startsWith(ACTIVITY_PREFIX)) {
 			// remove the prefix
 			key = key.replace(ACTIVITY_PREFIX, "");
 		} else if (key.startsWith(ACTIVITY_PREFIX_SPACE)) {
 			key = key.replace(ACTIVITY_PREFIX_SPACE, ACTIVITY_PREFIX);
 		}
 		if (key.endsWith(INIT)) {
 			return key;
 		} else {
 			return key + INIT;
 		}
 	}
 	
 	/**
 	 * This function will make sure the activity prefix is 
 	 * removed from the key passed in, and add the OFF suffix
 	 * @param key the name of the activity we want to operate on
 	 * @return the processed activity name that we passed in
 	 */
 	private static String formatActivityOffSuffix(String key) {
 		if (key.startsWith(ACTIVITY_PREFIX)) {
 			// remove the prefix
 			key = key.replace(ACTIVITY_PREFIX, "");
 		} else if (key.startsWith(ACTIVITY_PREFIX_SPACE)) {
 			key = key.replace(ACTIVITY_PREFIX_SPACE, "");
 		}
 		if (key.endsWith(OFF)) {
 			return key;
 		} else {
 			return key + OFF;
 		}
 	}
 		
 	private static String formatActivityImageIdSuffix(String key) {
 		if (key.startsWith(ACTIVITY_PREFIX)) {
 			// remove the prefix
 			key = key.replace(ACTIVITY_PREFIX, "");
 		} else if (key.startsWith(ACTIVITY_PREFIX_SPACE)) {
 			key = key.replace(ACTIVITY_PREFIX_SPACE, ACTIVITY_PREFIX);
 		}
 		if (key.endsWith(IMAGEID)) {
 			return key;
 		} else {
 			return key + IMAGEID;
 		}
 	}
 	
 	private static String formatActivityBtnCnfgSuffix(String key) {
 		if (key.startsWith(ACTIVITY_PREFIX)) {
 			// remove the prefix
 			key = key.replace(ACTIVITY_PREFIX, "");
 		} else if (key.startsWith(ACTIVITY_PREFIX_SPACE)) {
 			key = key.replace(ACTIVITY_PREFIX_SPACE, "");
 		}
 		if (key.endsWith(BTNCONFIG)) {
 			return key;
 		} else {
 			return key + BTNCONFIG;
 		}
 		
 	}
 	
 	/**
 	 * delete an activity from the arraylist on the interface.  This function is usually
 	 * invoked on a context menu on the arraylist that displays all the activities to the user.
 	 * 
 	 * @param position the position in the arraylist to delete
 	 */
 	public void deleteActivity(int position) {
 		ImageActivityItem name = mActivitiesArrayAdapter.getItem(position);
 		mActivitiesArrayAdapter.remove(name);
 		
 		String toDelete;		
 		String activityName = addActivityPrefix(name.title);
 		
 		// remove the ID associated with the device
 		blumote.lookup.deleteLookupId(activityName);
 		
 		MainInterface.deleteMiscButtons(activityName, blumote.prefs);
 		
 		Editor mEditor = blumote.prefs.edit();
 		mEditor.remove(activityName); 	
 
 		toDelete = formatActivityInitSuffix(activityName);
 		mEditor.remove(toDelete);
 		
 		toDelete = formatActivityImageIdSuffix(activityName);
 		mEditor.remove(toDelete);
 		
 		// activity off codes
 		toDelete = formatActivityOffSuffix(activityName);
 		mEditor.remove(toDelete);
 		
 		toDelete = formatActivityBtnCnfgSuffix(activityName);
 		mEditor.remove(toDelete);
 		
 		mEditor.commit();
 		
 		// refresh drop-down
 		mainint.populateDropDown();
 	}
 	
 	/**
 	 * add a new activity to the arraylist on the interface
 	 * @param activityName the name of the activity to add to the arraylist
 	 * @param image the resource ID of the image to use in the list
 	 * @param buttonConfig the button configuration setting
 	 */
 	public void addActivity(String activityName, int image, String buttonConfig) {
 		// create object to hold string and image
 		ImageActivityItem item = new ImageActivityItem(CreateActivity.getImageId(image), activityName);
 		
 		// add to arraylist
 		mActivitiesArrayAdapter.add(item);
 
 		activityName = addActivityPrefix(activityName);
 		
 		// assign new ID
 		blumote.lookup.addLookupId(activityName);
 		
 		Editor mEditor = blumote.prefs.edit();
 		mEditor.putString(activityName, null); // key, value
 		mEditor.putInt(formatActivityImageIdSuffix(activityName), image);
 		mEditor.putString(formatActivityBtnCnfgSuffix(activityName), buttonConfig); 
 		mEditor.commit();	
 		
 		// set workingActivity to this
 		setWorkingActivity(activityName);
 
 		mainint.populateDropDown(); // always refresh dropdown when adding an activity
 		mainint.setDropDown(activityName); // always set active dropdown item to new activity
 		
 		// set program state
 		mainint.setInterfaceState(MainInterface.INTERFACE_STATES.ACTIVITY_INIT);
 				
 		//TODO - launch help window if first time creating activity		
 		
 		// Tell user that they are in this mode
 		Toast.makeText(blumote, "Now in activity startup setup process mode", Toast.LENGTH_SHORT).show();
 	}
 	
 	/**
 	 * rename an activity on the interface, pass in new name and position in arraylist.
 	 * This function will update the preferences file as well as update the interface arraylist.
 	 * @param newName the new name
 	 * @param position the position of the old name in the arraylist
 	 */
 	public void renameActivity(String newName, int position) {  
 		ImageActivityItem oldItem = mActivitiesArrayAdapter.getItem(position);
 		String oldName = oldItem.title;
 		// update arraylist with new name (before adding prefix to it)
 		mActivitiesArrayAdapter.remove(oldItem);
 		mActivitiesArrayAdapter.add(new ImageActivityItem(oldItem.icon, newName));
 				
 		newName = addActivityPrefix(newName);
 		oldName = addActivityPrefix(oldName);
 		
 		// update lookup ID
 		blumote.lookup.updateLookupName(newName, oldName);
 		
 		Editor mEditor = blumote.prefs.edit();
 		
 		// store the data from old name
 		String activity = blumote.prefs.getString(oldName, null);				
 		
 		mEditor.remove(oldName); // remove old one
 		mEditor.putString(newName, activity); // add new name with old data
 
 		// rename image ID tag
 		String oldImageTag = formatActivityImageIdSuffix(oldName);
 		String newImageTag = formatActivityImageIdSuffix(newName);
 		int oldImage = blumote.prefs.getInt(oldImageTag, R.drawable.tv);
 		mEditor.remove(oldImageTag);
 		mEditor.putInt(newImageTag, oldImage);
 		
 		// rename all MISC button keys stored
 		MainInterface.renameMiscButtons(oldName, newName, blumote.prefs);
 		
 		// rename all OFF data keys stored
 		String newOFF = formatActivityOffSuffix(newName);
 		String oldOFF = formatActivityOffSuffix(oldName);
 		String offData = blumote.prefs.getString(oldOFF, null);
 		if (offData != null) {
 			mEditor.remove(oldOFF);
 			mEditor.putString(oldOFF, newOFF);
 		}
 		
 		// Now rename INIT data keys
 		String oldINIT = formatActivityInitSuffix(oldName);
 		String newINIT = formatActivityInitSuffix(newName);
 		String activityInit = blumote.prefs.getString(oldINIT, null);
 		mEditor.remove(oldINIT); // remove old one
 		mEditor.putString(newINIT, activityInit); // add new name with old data		
 		
 		// rename all Button Config data keys
 		String oldCfg = formatActivityBtnCnfgSuffix(oldName);
 		String newCfg = formatActivityBtnCnfgSuffix(newName);
 		String cfgValue = blumote.prefs.getString(oldCfg, null);
 		mEditor.remove(oldCfg);
 		mEditor.putString(newCfg, cfgValue);
 		
 		mEditor.commit();
 		
 		mainint.populateDropDown(); // always refresh dropdown when renaming an activity
 	}		
 	
 	/**
 	 * add a new initialization sequence to an existing activity....
 	 * 	
 	 * @param activityName the name of the activity we want to add init sequence to
 	 * @param init the list of Strings that we want to add to the initialization int the following two formats:
 	 * "Delay X" : delay of X milli-seconds
 	 * "DeviceID button" : 'device' represents one of the known devices in the database and should be the ID as 
 	 * returned by InterfaceLookup, 'button' represents the button ID on the device's interface
 	 * @param prefs The shared preferences object that the data is persisted in 
 	 * @param lookup the InterfaceLookup object to use to convert the device/activity names to IDs
 	 */
 	static void addActivityInitSequence(String activityName, List<String[]> init, SharedPreferences prefs, 
 			InterfaceLookup lookup) {
 		if (activityName != null) {
 			Editor mEditor = prefs.edit();
 			
 			// convert List to a compacted csv string
 			StringBuilder initItems = new StringBuilder();
 			String[] curItem;
 			ArrayList<String> powerOffItems = new ArrayList<String>();
 			for (Iterator<String[]> initStep = init.iterator(); initStep.hasNext();) {
 				curItem = initStep.next();
 				
 				//TODO - need to make init items into hashmap....
 				//check if the item is a power button, if so then need to add it to the activity power off button				
 				if (curItem[0].length() != 0) { // check for valid entry
 					// second item is the button name (if not a delay)
 					if(!curItem[0].equals("DELAY")) {
 						// parse the element [0] to a proper device/activity ID if it is not a delay item
 						curItem[0] = lookup.getID(curItem[0]);
 						
 						// check to see if this is a power_on button and if so add it to the arraylist
 						if (curItem[1].equals("power_on_btn")) { 
 							powerOffItems.add(curItem[0]); // save just the device ID
 						}
 					}
 					// add this item to the initItems stringbuilder
 					initItems.append(curItem[0] + "+" + curItem[1] + ","); 
 				}
 			}
 			
 			// add the collected power off buttons to the interface
 			savePowerOffButton(activityName, powerOffItems, prefs);
 			
 			// add INIT suffix before storing initItems to prefs file
 			activityName = formatActivityInitSuffix(activityName);
 			
 			mEditor.putString(activityName, initItems.toString()); 
 
 			mEditor.commit();
 		}
 	}
 	
 	/**
 	 * add a new initialization sequence to an existing activity....
 	 * 	
 	 * @param activityName the name of the activity we want to add init sequence to
 	 * @param init the list of Strings that we want to add to the initialization int the following two formats:
 	 * "Delay X" : delay of X milli-seconds
 	 * "Device button" : 'device' represents one of the known devices in the database,
 	 * 'button' represents the button ID on the device's interface
 	 */
 	void addActivityInitSequence(String activityName, List<String[]> init) {
 		addActivityInitSequence(activityName,init, blumote.prefs, blumote.lookup);
 	}
 	
 	/**
 	 * add a new initialization sequence to an existing activity....the activity to operate on
 	 * is assumed to be set by setWorkingActivity()
 	 * 	
 	 * @param init the list of Strings that we want to add to the initialization int the following two formats:
 	 * "Delay X" : delay of X milli-seconds
 	 * "Device button" : 'device' represents one of the known devices in the database,
 	 * 'button' represents the button ID on the device's interface
 	 */
 	void addActivityInitSequence(List<String[]> init) {
 		addActivityInitSequence(workingActivity,init, blumote.prefs, blumote.lookup);
 	}
 	
 	/**
 	 * Retrieve the initialization sequence to be performed by the activity
 	 * @param activityName the name of activity to retrieve init sequence of
 	 * @param prefs the Shared preferences object that has the init data
 	 * @return String[] with the elements in order (first to last)
 	 */
 	static ArrayList<String[]> getActivityInitSequence(String activityName, SharedPreferences prefs) {
 		activityName = formatActivityInitSuffix(activityName);
 		String initSequence = prefs.getString(activityName, null);
 		String[] initItems;
 		ArrayList<String[]> parsedInitItems = new ArrayList<String[]>();
 		
 		InterfaceLookup lookup = new InterfaceLookup(prefs);
 		
 		if (initSequence==null) {
 			return null;
 		}
 		else {
 			initItems = initSequence.split(",");
 			// now split the individual entries by the "+" token
 			for (int i=0; i< initItems.length; i++) {				
 				try {
 					String[] item = initItems[i].split("\\+");
 					String name = lookup.getName(item[0]);
 					// attempt to convert a lookup-id to a real text name
 					if ( name != null) {
 						parsedInitItems.add(new String[]{name, item[1]});
 					} else { // otherwise just add it
 						parsedInitItems.add(item);
 					}
 				} catch (Exception e) {
 					// ignore invalid tokens
 				}
 			}
 			
 			return parsedInitItems;
 		}
 	}
 	
 	/**
 	 * Retrieve the initialization sequence to be performed by the activity.
 	 * This function assumes setWorkingActivity() was called and that the default blumote prefs file is used
 	 * @return String[] with the elements in order (first to last)
 	 */
 	private ArrayList<String[]> getActivityInitSequence() {
 		return getActivityInitSequence(workingActivity, blumote.prefs);
 	}
 	
 	/**
 	 * this function will extract the init sequence and then execute it.
 	 * note that while this is running it will pop-up a 'working' dialog
 	 * that will stay up until the init sequence completes.
 	 * @param activityName The name of the activity to execute init sequence of
 	 */
 	void startActivityInitSequence(String activityName) {
 		activityName = formatActivityInitSuffix(activityName);
 		// call getActivityInitSequence(activityName) to get the list of items to execute
 		initItems = getActivityInitSequence();
 		initItemsIndex = 0; // reset index
 		
 		// show progress dialog
 		blumote.showDialog(BluMote.DIALOG_INIT_PROGRESS);
 		
 		nextActivityInitSequence();
 	}
 
 	/**
 	 * this function will extract the init sequence and then execute it.
 	 * note that while this is running it will pop-up a 'working' dialog
 	 * that will stay up until the init sequence completes.
 	 * The activity to use is assumed to be set by setWorkingActivity() previously.
 	 */
 	void startActivityInitSequence() {
 		startActivityInitSequence(workingActivity);
 	}
 	
 	/**
 	 * this should be called after startActivityInitSequence has completed
 	 * this function will execute the next item in the Init sequence, if
 	 * no more items are available it will dismiss the progress dialog
 	 */
 	void nextActivityInitSequence() {
 		String[] item;
 		while (initItems != null && initItemsIndex < initItems.size()) {
 			// use initItemIndex to deal with getting through all the items
 			// if run into a delay item then need to spawn CountDownTimer and then CountDownTimer
 			// will call this method after it finishes...			
 			item = initItems.get(initItemsIndex);
 			initItemsIndex++;
 			if (item[0].matches("")) {
 				// log error and continue to next item
 				Log.e(TAG, "initialization item was null - malformed item");
 			}
 			// check if item is null and is a DELAY xx item
 			else if (item[0].startsWith("DELAY")) {
 				// extract value after the space
 				String delay = (item[1]);
 				try {
 					int delayTime = Integer.parseInt(delay);
 					//need to start a wait timer for this period of time
 					new CountDownTimer(delayTime, delayTime) {
 						public void onTick(long millisUntilFinished) {
 							// no need to use this function
 						}
 
 						public void onFinish() {
 							// called when timer expired
 							nextActivityInitSequence(); // continue on the quest to finish the initItems
 						}
 					}.start();					
 					break; // exit while loop while we are waiting for Delay to finish
 				} catch (Exception e) {
 					// failed, skip and go to next item in this case.
 					Log.e(TAG, "Failed to execute an initialization delay!");
 				}
 			}			
 			// else if the item is a button in format "Device Button"
 			else {
 				// extract value after the space
 				String buttonID = (item[1]); 
 				byte[] toSend = null;
 
 				String buttonSource = item[0];
 				// if buttonSource is null that means the device/activity was deleted, so just skip over this item
 				if (buttonSource != null) {
 					// need to determine if this is an activity (A)_ 
 					if ( buttonSource.startsWith(Activities.ACTIVITY_PREFIX)) {
 						// then need to extract lookup to real device button association..
 						try {
 							// Returns DeviceButton created from activity button and activity name
 							DeviceButton realDevice = new DeviceButton(buttonSource, buttonID);
 							toSend = blumote.device_data.getButton(realDevice.getDevice(), realDevice.getButton());
 						} catch (Exception e) {
 							// failed so don't send anything
 						}
 
 					}				
 					else {
 						// 	otherwise we can just use getButton if it is a regular device
 						try {
 							toSend = blumote.device_data.getButton(buttonSource, buttonID);
 						} catch (Exception e) {
 							// failed so don't send anything
 						}
 					}
 					// execute button code
 					if (toSend != null) {					
 						pod.sendButtonCode(toSend);
 					}
 				}
 			}				
 		} // end while	
 		// check if we are done processing, if so dismiss the progress dialog
 		if (initItems == null || initItemsIndex >= initItems.size()) {
 			blumote.dismissDialog(BluMote.DIALOG_INIT_PROGRESS);
 		}
 	} // end nextActivityInitSequence
 	
 
 	/**
 	 * sends the power off codes with a proper delay between sends to prevent buffer overflow on pod
 	 * @param powerOff
 	 */
 	public void sendPowerOffData(ButtonData[] powerOff) {		
 		powerOffData = powerOff;
 		powerOffDataIndex = 0;
 		
 		nextPowerOffData();
 	}
 	
 	public void nextPowerOffData() {
 		if (powerOffDataIndex < powerOffData.length) {
 			pod.sendButtonCode(powerOffData[powerOffDataIndex].getButtonData());
 			powerOffDataIndex++;
 
 			if (powerOffDataIndex < powerOffData.length) {
 				//need to start a wait timer for this period of time
 				new CountDownTimer(BluMote.DELAY_TIME, BluMote.DELAY_TIME) {
 					public void onTick(long millisUntilFinished) {
 						// no need to use this function
 					}
 
 					public void onFinish() {
 						// called when timer expired			
 						nextPowerOffData(); // continue on the quest to finish
 					}
 				}.start();					
 			}
 
 		} // end while	
 	}
 	
 	/**
 	 * add a new button association for an existing activity
 	 * Note: when a new keybinding is added it is 'appended' to the existing bindings
 	 * @param activityname the key of the activity to associate this init sequence with
 	 * @param btnID the button id of the activity screen button that we want to bind
 	 * @param device is an existing device name which is in the database
 	 * @param deviceBtn is the ID for an interface button of the device that we want bound
 	 */
 	void addActivityKeyBinding(String activityName, String btnID, String device, String deviceBtn) {
 		if (activityName != null) {
 			activityName = addActivityPrefix(activityName);
 
 			// convert device to the ID associated with that device
 			device = blumote.lookup.getID(device);
 			// check if device is not null, if it is then skip this item
 			if (device != null) {
 				String record = blumote.prefs.getString(activityName, null);
 
 				// formatting of record is : btnID deviceID deviceBtn, etc
 				if (record != null) {
 					// supress leading comma if null record (empty)
 					record = record + ",";
 					
 					// TODO check if association already exists, if so edit it
 					if (record.startsWith(btnID) || record.contains(","+btnID)) {
 						removeActivityKeyBinding(btnID);
 						record = blumote.prefs.getString(activityName, null);
 					}
 				} else {
 					record = "";
 				}
 
 				// append the new data to the existing record
 				record = record + btnID + " " + device + " " + deviceBtn;
 				// save new record into NV memory
 				Editor mEditor = blumote.prefs.edit();
 				mEditor.putString(activityName, record);
 				mEditor.commit();
 
 				// update buttons on interface
 				mainint.fetchButtons();
 			}
 		}
 	}
 	
 	/**
 	 * add a new button association for an existing activity
 	 * Note: when a new keybinding is added it is 'appended' to the existing bindings
 	 * This function assumes setWorkingActivity() was previously called
 	 * @param btnID the button id of the activity screen button that we want to bind
 	 * @param device is an existing device name which is in the database
 	 * @param deviceBtn is the ID for an interface button of the device that we want bound
 	 */
 	void addActivityKeyBinding(String btnID, String device, String deviceBtn) {
 		addActivityKeyBinding(workingActivity, btnID, device, deviceBtn);
 	}
 	
 	/** 
 	 * Removes a key binding from an activity button to a device button.
 	 * @param activityname the activity name we want to work with
 	 * @param buttonID the button id of the activity button to remove binding of
 	 */
 	void removeActivityKeyBinding(String activityName, String buttonID) {
 		activityName = addActivityPrefix(activityName);
 		
 		String record = blumote.prefs.getString(activityName, null);
 		
 		if (record != null) {
 			// split up the items by commas
 			String[] entries = record.split(",");
 			
 			// after we remove the button we will have data of one less item
 			String[] newEntries = new String[entries.length - 1];
 			int newEntriesIndex = 0;
 			
 			// formatting of record is : btnID deviceID deviceBtn, etc
 			String[] buttonMap = new String[3];
 			try {
 				for (int i= 0; i< entries.length; i++) {
 					buttonMap = entries[i].split(" ");
 					if (buttonMap[2].equals(buttonID)) {					
 						continue; // skip this item
 					} else {
 						newEntries[newEntriesIndex] = entries[i];
 						newEntriesIndex++;
 					}
 				}
 			} catch (ArrayIndexOutOfBoundsException e) {
 				// if we get this then we tried to put too many things into newEntries[]
 				// which probably means we did not find the item to delete.
 				// in this case we are just going to not edit the record so we will 
 				// define newEntries as equivalent to entries[]
 				newEntries = entries;
 			}
 				
 			// now that we have our newEntries[] we need to convert it into a flattened csv string
 			// convert List to a compacted csv string
 			StringBuilder newRecord = new StringBuilder();
 			for (int i= 0 ; i < newEntries.length; i++) {
 				newRecord.append(newEntries[i]+",");
 			}
 			
 			// save new record into NV memory
 			Editor mEditor = blumote.prefs.edit();
 			mEditor.putString(activityName, newRecord.toString());
 			mEditor.commit();			
 		} // end if						
 	}			
 	
 	/** 
 	 * Removes a key binding from an activity button to a device button.
 	 * This function assumes setWorkingActivity() was called previously.
 	 * @param buttonID the button id of the activity button to remove binding of
 	 */
 	void removeActivityKeyBinding(String binding) {
 		removeActivityKeyBinding(workingActivity, binding);
 	}
 	
 	/**
 	 * Returns the activity buttons in a ButtonData[] structure
 	 * @param activityName the activity we want to return data from
 	 * @return The full array of button associations
 	 */
 	ButtonData[] getActivityButtons(String activityName) {
 		activityName = addActivityPrefix(activityName);
 		
 		// need to loop through the activity 'record' that defines the button mappings
 		// and then add each item in the proper columns of the ButtonData[] object
 		String record = blumote.prefs.getString(activityName, null);
 		ButtonData[] deviceButtons = null;
 		if (record != null) {
 			// split up the items by commas		
 			String[] entries = record.split(",");
 
 			deviceButtons = new ButtonData[entries.length];
 			ActivityButton activityButton;
 
 			for (int index = 0; index < entries.length; index++) {
 				activityButton = new ActivityButton(activityName, entries[index]);
 				try {
 					// try to insert data from database if it exists
 					deviceButtons[index] = new ButtonData(
 							0, activityButton.getActivityButton(), 
 							blumote.device_data.getButton(activityButton.getDeviceName(), 
 							activityButton.getDeviceButton()));
 				} catch (Exception e) {
 					// if the call the getButton() failed then lets just create a button with null for data
 					deviceButtons[index] = new ButtonData(0, activityButton.getActivityButton(),null);
 				}
 				
 			}
 		}
 		return deviceButtons;
 	}	
 	
 	/**
 	 * Returns the activity buttons in a ButtonData[] structure
 	 * This function assumes setWorkingActivity() was previously called.
 	 * @return The full array of button associations
 	 */
 	ButtonData[] getActivityButtons() {
 		return getActivityButtons(workingActivity);
 	}
 
 	/**
 	 * This function sets the working activity but uses the position in the 
 	 * interface arraylist to do so.
 	 * @param position Position in the array list
 	 */
 	void setWorkingActivity(int position) {
 		// uses ListView index to set the working activity
 		ImageActivityItem name = mActivitiesArrayAdapter.getItem(position);
 		setWorkingActivity(name.title);
 	}
 	
 	/**
 	 * Checks if the button ID is valid for an initialization sequence of an activity
 	 * @param buttonId the resource ID of the interface button
 	 * @return true if it is valid, false if not valid
 	 */
 	static boolean isValidActivityButton(int buttonId) {		 
 		switch (buttonId) {
 		case R.id.power_off_btn:
 			return false;
 		default:
 			return true;
 		}
 	}
 	
 	/**
 	 * Takes a list of devices that have had their power buttons pushed when creating the init list for an activity
 	 * and stores them for the power off button of an activity.
 	 * @param activityName the name of the activity
 	 * @param powerOnDevices a list of power buttons that were pushed during init sequence recording, note 
 	 * this data should be the device-ID not the device-name.
 	 * @param prefs the preferences file
 	 */
 	static void savePowerOffButton(String activityName, ArrayList<String> powerOnDevices, SharedPreferences prefs) {
 		// convert the items to comma separated values
 		StringBuilder builder = new StringBuilder();
 		
 		Iterator<String> iterator = powerOnDevices.iterator();
 		
 		while (iterator.hasNext()) {
 			builder.append(iterator.next()+",");
 		}
 		
 		Editor mEditor = prefs.edit();
 		activityName = formatActivityOffSuffix(activityName);		
 		mEditor.putString(activityName, builder.toString()); 			
 		mEditor.commit();
 	}
 	
 	/**
 	 * Takes a list of devices that have had their power buttons pushed when creating the init list for an activity
 	 * and stores them for the power off button of an activity.
 	 * @param powerOnDevices a list of power buttons that were pushed during init sequence recording
 	 */
 	void savePowerOffButton(ArrayList<String> powerOnDevices) {
 		savePowerOffButton(workingActivity, powerOnDevices, blumote.prefs);
 	}	
 	
 	/**
 	 * This function returns the power off data associated with an activities 'power off' button.
 	 * This is automatically generated when setting up an activities initialization sequence and the user 
 	 * pushes a power on command for a device.
 	 * @param activityName the name of the activity we want to get the power off codes for
 	 * @return the power off codes as an array of ButtonData objects
 	 */
 	ButtonData[] getPowerOffButtonData(String activityName) {
 		// pull power off / toggle codes from prefs file
 		// populate blumote's field with this data
 		// BluMote needs to look for the power off button push and execute this data
 		// this function loads 'null' if no data is stored
 		try {
 			activityName = formatActivityOffSuffix(activityName);
 			String powerOffCodes = blumote.prefs.getString(activityName, null);
 			// powerOffCodes is csv
 			String[] devices = powerOffCodes.split(",");
 						
 			byte[] buttonData;
 			if (devices != null) {				
 				ButtonData[] returnData = new ButtonData[devices.length];
 				// each token is a device name that we should issue the power command for			
 				for (int i=0; i < devices.length; i++) {					
 					try {
 						// convert each devices element back to the actual name from the id
 						devices[i] = blumote.lookup.getName(devices[i]);
 				 		
 						// try to insert data from database if it exists
 						buttonData = blumote.device_data.getButton(devices[i], mainint.button_map.get(R.id.power_on_btn));
 						returnData[i] = new ButtonData( R.id.power_on_btn, 
 								mainint.button_map.get(R.id.power_on_btn),buttonData);
 					} catch (Exception e) {
 						// if the call the getButtion() failed then lets just create a button with null for data
 						returnData[i] = new ButtonData(0, mainint.button_map.get(R.id.power_on_btn),null);
 					}
 				}
 				
 				return returnData;
 			} else { // no devices in the list, so return null
 				return null;
 			}
 		} catch (Exception e) {
 			// just exit function and set blumote power-off data to null
 			return null;
 		}
 	}
 	
 	/**
 	 * Get the button config from the prefs file based on the activity name passed in.
 	 * @param activityName
 	 * @return
 	 */
 	public String getButtonConfig(String activityName) {
 		// format activityName for retrieving the button config from prefs file
 		activityName = formatActivityBtnCnfgSuffix(activityName);
 		return blumote.prefs.getString(activityName, null);		
 	}
 	
 	/**
 	 * This is a helper class to encapsulate all the parameters of a
 	 * activity button which is associated with a real device button.
 	 * @author keusej
 	 */
 	private class ActivityButton {
 		private String deviceName = null;
 		private String activityName = null;
 		private String activityButton = null;
 		private String deviceButton = null;
 		
 		/**
 		 * Constructor for the class
 		 * @param activityName the name of the activity
 		 * @param record The data that is extracted from the SharedPreferences for a particular activity
 		 */
 		ActivityButton(String activityName, String record) {
 			try {
 				String[] items = record.split(" ");
 				this.activityName = activityName;
 				this.activityButton = items[0];
 				
 				// device name is going to be converted from the ID which is stored
 				// in the prefs file to the actual name
 				this.deviceName = blumote.lookup.getName(items[1]);
 				
 				this.deviceButton = items[2];
 			} catch(Exception e) {
 				// .split() failed, so leave values as null
 			}
 		}
 		
 		String getActivityButton() {
 			return activityButton;
 		}
 		
 		String getDeviceButton() {
 			return deviceButton;
 		}
 		
 		String getDeviceName() {
 			return deviceName;
 		}
 		
 		@SuppressWarnings("unused")
 		String getActivityName() {
 			return activityName;
 		}	
 	}
 	
 	/**
 	 * Helper class to encapsulate the data associated with a real device button
 	 */
 	private class DeviceButton {
 		private String deviceName;
 		private String deviceButton;
 		
 		/**
 		 * Takes an activity and activity button and converts to a device and button.
 		 * 
 		 * @param activityName the name of the activity
 		 * @param activityButton the button name on the activity interface
 		 */
 		DeviceButton(String activityName, String activityButton) {
 			String record = null;
 			Map<String,?> values = blumote.prefs.getAll();
 			
 			// iterate through these values
 			for (String item : values.keySet()) {
 				// check if prefix is an activity
 				if (item.startsWith(ACTIVITY_PREFIX)) {									
 					// need to see if this is the activityName we are seeking
 					if (item.equals(activityName)) {
 						record = (String)values.get(item);
 						break; // get out of for loop
 					}
 				}
 			}
 			
 			if (record != null) {
 				// convert activity buttons record to appropriate device buttons
 				// split up the items by commas		
 				String[] entries = record.split(",");
 
 				ActivityButton activityBtnItem;
 				for (int index = 0; index < entries.length; index++) {
 					activityBtnItem = new ActivityButton(activityName, entries[index]);
 
 					// check if this activityBtnItem matches the activityButton we are interested in
 					if (activityBtnItem.getActivityButton().equals(activityButton)) {
 						this.deviceButton = activityBtnItem.getDeviceButton();
 						this.deviceName = activityBtnItem.getDeviceName();
 						break;
 					}
 
 				}
 			} // end if
 			else {
 				this.deviceButton = null;
 				this.deviceName = null;
 			}
 		}
 		
 		String getDevice() {
 			return deviceName;
 		}
 		
 		String getButton() {
 			return deviceButton;
 		}
 	}
 
 	/**
 	 * Custom array adapter to allow for an image to be put into the 
 	 * list along with a text title.  
 	 * @author keusej
 	 *
 	 */
 	public class ImageArrayAdapter extends ArrayAdapter<ImageActivityItem> {
 		Context context; 
 	    int layoutResourceId;    
 	    	    
 		public ImageArrayAdapter(Context context, int layoutResourceId) {
 			super(context, layoutResourceId);
 	        this.layoutResourceId = layoutResourceId;
 	        this.context = context;     
 		}
 
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent) {
 			View row = convertView;
 	        ImageTextHolder holder = null;
 
 	        if(row == null)
 	        {
 	            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
 	            row = inflater.inflate(layoutResourceId, parent, false);
 	            
 	            holder = new ImageTextHolder();
 	            holder.imgIcon = (ImageView)row.findViewById(R.id.activity_image);
 	            holder.txtTitle = (TextView)row.findViewById(R.id.activity_label);
 	            
 	            row.setTag(holder);
 	        }
 	        else
 	        {
 	            holder = (ImageTextHolder)row.getTag();
 	        }
 	        
 	        ImageActivityItem weather = getItem(position);
 	        holder.txtTitle.setText(weather.title);
 	        holder.imgIcon.setImageResource(weather.icon);
 	        
 	        return row;
 		}
 						
 		class ImageTextHolder
 	    {
 	        ImageView imgIcon;
 	        TextView txtTitle;
 	    }
 	}
 	
 	static class ImageActivityItem {
 	    public int icon;
 	    public String title;
 	    public ImageActivityItem(){
 	        super();
 	    }
 	    
 	    public ImageActivityItem(int icon, String title) {
 	        super();
 	        this.icon = icon;
 	        this.title = title;
 	    }
 	}	
 }
