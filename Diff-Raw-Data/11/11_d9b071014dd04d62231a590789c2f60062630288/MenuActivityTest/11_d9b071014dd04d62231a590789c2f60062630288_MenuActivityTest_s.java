 /*
  * Copyright [2012] [Simon Fransson]
 
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
 
        http://www.apache.org/licenses/LICENSE-2.0
 
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License. 
  */
 package com.chalmers.schmaps.test;
 
import java.lang.reflect.Field;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 
 import com.chalmers.schmaps.CheckBusActivity;
 import com.chalmers.schmaps.CheckInActivity;
 import com.chalmers.schmaps.MenuActivity;
 import com.chalmers.schmaps.R;
 import com.jayway.android.robotium.solo.Solo;
 
import android.R.menu;
 import android.content.Context;
 import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
 import android.test.ActivityInstrumentationTestCase2;
 import android.test.TouchUtils;
 import android.view.KeyEvent;
 import android.widget.Button;
 
 /**
  * Test case to see that all the menu buttons in MenuActivity starts the
  * correct intent when user touches a button.
  * @author Froll
  *
  */
 public class MenuActivityTest extends ActivityInstrumentationTestCase2<MenuActivity> {
 	private Button searchHall, groupRoom,atmButton,microwaveButton,findRestaurantsButton;
 	private MenuActivity menuActivity;
 	private Solo solo;	
	private WifiManager wifiManager;
 	private ConnectivityManager iConnectivityManager;
 	public MenuActivityTest() {
 		super(MenuActivity.class);
 	}
 	
 	/**
 	 * Setup method for instantiating the variables.
 	 */
 	@Override
 	protected void setUp() throws Exception {
 		super.setUp();
 		setActivityInitialTouchMode(false);
 		solo = new Solo(getInstrumentation(), getActivity());
 		this.menuActivity = super.getActivity();
 		searchHall = (Button) menuActivity.findViewById(R.id.searchHallButton);
 		groupRoom = (Button) menuActivity.findViewById(R.id.groupRoomButton);
 		atmButton = (Button) menuActivity.findViewById(R.id.atmButton);
 		microwaveButton = (Button) menuActivity.findViewById(R.id.microwaveButton);
 		findRestaurantsButton = (Button) menuActivity.findViewById(R.id.findRestaurantsButton);
 		iConnectivityManager = (ConnectivityManager)menuActivity.getSystemService(Context.CONNECTIVITY_SERVICE);
 	}
 	
 	public void testPreConditions(){
 		super.assertNotNull(searchHall);
 		super.assertNotNull(groupRoom);
 		super.assertNotNull(atmButton);
 		super.assertNotNull(microwaveButton);
 		super.assertNotNull(findRestaurantsButton);
 	}
 	
 	/**
 	 * Tests the "Search hall" button to start the correct activity by comparing the intent.
 	 * Tests below works in the same manner but for different buttons and different intents.
 	 */
 	public void testSearchHallButton(){
 		TouchUtils.clickView(this, this.searchHall);
 		super.getInstrumentation().waitForIdleSync();
 		assertEquals("android.intent.action.GOOGLEMAPSEARCHLOCATION", menuActivity.getActivityString());
 		this.sendKeys(KeyEvent.KEYCODE_BACK);
 	}
 	/**
 	 *	Tests the "Group Room" button to start the correct activity by comparing the intent.
 	 */
 	public void testGroupRoomButton(){
 		TouchUtils.clickView(this, this.groupRoom);
 		super.getInstrumentation().waitForIdleSync();
		assertEquals("GroupRoomButton", menuActivity.getActivityString());
 		this.sendKeys(KeyEvent.KEYCODE_BACK);
 	}
 	
 	/**
 	 *	Tests the "Atm" button to start the correct activity by comparing the intent.
 	 */
 	public void testAtmButton(){
 		TouchUtils.clickView(this, this.atmButton);
 		super.getInstrumentation().waitForIdleSync();
 		assertEquals("android.intent.action.CAMPUSMENUACTIVITY", menuActivity.getActivityString());
 		this.sendKeys(KeyEvent.KEYCODE_BACK);
 	}
 	
 	/**
 	 *	Tests the "Microwave" button to start the correct activity by comparing the intent.
 	 */
 	public void testMicrowaveButton(){
 		TouchUtils.clickView(this, this.microwaveButton);
 		super.getInstrumentation().waitForIdleSync();
 		assertEquals("android.intent.action.CAMPUSMENUACTIVITY", menuActivity.getActivityString());
 		this.sendKeys(KeyEvent.KEYCODE_BACK);
 	}
 	
 	/**
 	 *	Tests the "Find restaurants" button to start the correct activity by comparing the intent.
 	 */
 	public void testFindRestaurantsButton(){
 		TouchUtils.clickView(this, this.findRestaurantsButton);
 		super.getInstrumentation().waitForIdleSync();
 		assertEquals("android.intent.action.CAMPUSMENUACTIVITY", menuActivity.getActivityString());
 		this.sendKeys(KeyEvent.KEYCODE_BACK);
 	}
 	
 	/**
 	 *	Tests the "Check in" button to start the correct activity by using solo to assert
 	 *	that the current activity is the correct one after pressing the button. Uses reflection
 	 *	to disable the internet connection to be able to test that the Toast-window appears
 	 *	and not starting the Check in activity.
 	 */
 	public void testCheckInButton(){
 		//Wait for mobile data to be enabled again.
 		while(!isOnline());
 		solo.clickOnButton("Check In");
 		super.getInstrumentation().waitForIdleSync();
 		solo.assertCurrentActivity("Wrong class", CheckInActivity.class);
 		super.getInstrumentation().waitForIdleSync();
 		solo.goBack();
 		//Toggle the internet off for affirming the Toast coming forth.
 		toggleInternetConnection(menuActivity, false);
 		super.getInstrumentation().waitForIdleSync();
 		solo.clickOnButton("Check In");
 		super.getInstrumentation().waitForIdleSync();
 		//solo checks if the Toast appeared or not, validating that it did not start
 		//the activity if there was no internet connection.
 		assertTrue(solo.waitForText("Internet connection needed for this option"));
 		assertTrue(solo.searchText("Internet connection needed for this option"));
 		toggleInternetConnection(menuActivity, true);
 	}
 
 	/**
 	 *	Tests the "Check bus" button to start the correct activity by using solo to assert
 	 *	that the current activity is the correct one after pressing the button. Uses reflection
 	 *	to disable the internet connection to be able to test that the Toast-window appears
 	 *	and not starting the Check in activity.
 	 */
 	public void testCheckBusButton(){
 		solo.clickOnButton("Check Buses");
 		super.getInstrumentation().waitForIdleSync();
 		solo.assertCurrentActivity("Wrong class", CheckBusActivity.class);
 		solo.goBack();
 		super.getInstrumentation().waitForIdleSync();
 		//Toggle the internet off for affirming the Toast coming forth.
 		toggleInternetConnection(menuActivity, false);
 		super.getInstrumentation().waitForIdleSync();
 		solo.clickOnButton("Check Buses");
 		//Use solo to assert the text of the Toast that occurs at loss of internet.
 		assertTrue(solo.waitForText("Internet connection needed for this option"));
 		assertTrue(solo.searchText("Internet connection needed for this option"));
 		toggleInternetConnection(menuActivity, true);
 	}
 
 	/**
 	 * Method to toggle both wifi and mobile data on/off. Uses reflection to access the
 	 * toggling for mobile data since it's hidden from the developer API, but still accessible
 	 * through reflection.
 	 * @param context - the test activity to use the function of toggling internet access.
 	 * @param toggle - turn on/off.
 	 */
 	public void toggleInternetConnection(Context context, boolean toggle){
 		//Assert the ConnectivityManager to be able to access mobile data on/off toggling, reflection is used
 		//since it's a hidden method from the API.
 			Method toggleDataEnabled;
 			try {
 				toggleDataEnabled = ConnectivityManager.class.getDeclaredMethod("setMobileDataEnabled", boolean.class);
 				toggleDataEnabled.setAccessible(true);
 				toggleDataEnabled.invoke(iConnectivityManager, toggle); 
 			} catch (NoSuchMethodException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (IllegalArgumentException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (IllegalAccessException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (InvocationTargetException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 	}
 	/**
 	 * Simple method for checking if the device has internet connection or not
 	 * @return boolean - true or false depending on connection.
 	 */
 	public boolean isOnline() {
 	    return iConnectivityManager.getActiveNetworkInfo() != null && 
 	       iConnectivityManager.getActiveNetworkInfo().isConnectedOrConnecting();
 	}
 
 	public void tearDown() throws Exception{
 		super.tearDown();
 	}
 }
