 package com.example.setapp.test;
 
import com.android.uiautomator.core.*;
import com.android.uiautomator.testrunner.UiAutomatorTestCase;
 
 public class IntegTest extends UiAutomatorTestCase {
	
	public IntegTest() {
		super();
	}
 
	public void testInitialization() throws UiObjectNotFoundException {
 		
 		// Simulate a press on the HOME button
 		getUiDevice().pressHome();
 		
 		// Navigate to the apps screen
 		UiObject appsButton = new UiObject(new UiSelector().description("Apps"));
 		
 		// Simulate a click on the apps button
 		appsButton.clickAndWaitForNewWindow();
 		
 		// Find the Set app and simulate launch
 		UiScrollable appView = new UiScrollable(new UiSelector().scrollable(true));
 		appView.setAsHorizontalList();	// Swipe horizontally 
 		UiObject setApp = appView.getChildByText(new UiSelector().className(android.widget.TextView.class.getName()), "Set App");
 		setApp.clickAndWaitForNewWindow();
 		
 		// Validate the package name
 		UiObject valid = new UiObject(new UiSelector().packageName("com.example.setapp"));
 		assertTrue("Unable to detect Set App.", valid.exists());
 	}
 

 }
