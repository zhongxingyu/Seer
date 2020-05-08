 package com.androidhuman.ctsprepare.automator;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 
 import org.json.JSONException;
 
 import com.android.uiautomator.core.UiDevice;
 import com.android.uiautomator.core.UiObject;
 import com.android.uiautomator.core.UiObjectNotFoundException;
 import com.android.uiautomator.core.UiScrollable;
 import com.android.uiautomator.core.UiSelector;
 import com.android.uiautomator.testrunner.UiAutomatorTestCase;
 
 public class PreCTSAutomate extends UiAutomatorTestCase{
 
 	public void testActivateDeviceAdmin() throws UiObjectNotFoundException {
 		// Get installed device admin listview
 		UiScrollable deviceAdminList = 
 				new UiScrollable(new UiSelector().className("android.widget.ListView"));
 		
 		// First device admin receiver
 		UiObject receiver1 = 
 				deviceAdminList.getChildByText(
 						new UiSelector().className("android.widget.LinearLayout"), 
 						"android.deviceadmin.cts.CtsDeviceAdminReceiver");
 		UiObject checkbox1 = receiver1.getChild(new UiSelector().className("android.widget.CheckBox"));
 		// If not checked
 		if(!checkbox1.isChecked()){
 			receiver1.click();
 			(new UiObject(new UiSelector().text("Activate"))).clickAndWaitForNewWindow();
 		}
 		
 		// Second device admin receiver
 		UiObject receiver2 = 
 				deviceAdminList.getChildByText(
 						new UiSelector().className("android.widget.LinearLayout"), 
 						"android.deviceadmin.cts.CtsDeviceAdminReceiver2");
 		UiObject checkbox2 = receiver2.getChild(new UiSelector().className("android.widget.CheckBox"));
 		// If not checked
 		if(!checkbox2.isChecked()){
 			receiver2.click();
 			(new UiObject(new UiSelector().text("Activate"))).clickAndWaitForNewWindow();
 		}
 		
 		UiDevice.getInstance().pressHome();
 	}
 	
 	public void testActivateWifi() throws UiObjectNotFoundException {
 		UiObject wifiSwitch = new UiObject(new UiSelector().className("android.widget.Switch"));
 		if(!wifiSwitch.isChecked()){
 			wifiSwitch.clickAndWaitForNewWindow();
 		}
 		
 		UiScrollable wifiList = new UiScrollable(new UiSelector().className("android.widget.ListView"));
 		UiObject ap = null;
 		while(true){
 			try{
 				// Select AP
 				ap = wifiList.getChildByText(new UiSelector().className("android.widget.LinearLayout"), "AndroidNet");
 				break;
 			}catch(UiObjectNotFoundException e){
 				UiDevice.getInstance().waitForWindowUpdate("com.android.settings", 1500);
 			}
 		}
 		ap.clickAndWaitForNewWindow();
 		
 		// If input method dialog popup showed up
 		try{
 			UiObject okButton = new UiObject(new UiSelector().textMatches("OK"));
 			okButton.clickAndWaitForNewWindow(1000);
 		}catch(UiObjectNotFoundException e){
 			// No dialog showed up
 		}
 		
 		// If swype instruction dialog popup showed up
 		try{
 			UiObject okButton = new UiObject(new UiSelector().textMatches("Dismiss"));
 			okButton.clickAndWaitForNewWindow(1000);
 		}catch(UiObjectNotFoundException e){
 			// No dialog showed up
 		}
 		
 		UiObject editText = new UiObject(new UiSelector().className("android.widget.EditText"));
 		editText.setText("welcomegsm!"); // Enter password
 		
 		UiObject connectButton = new UiObject(new UiSelector().textMatches("Connect"));
 		connectButton.clickAndWaitForNewWindow();
 		
 		UiDevice.getInstance().pressHome();
 	}
 	
 	public void testSetTimeout() throws UiObjectNotFoundException{
 		UiScrollable settingsList = new UiScrollable(new UiSelector().className("android.widget.ListView"));
 		UiObject timeout = settingsList.getChildByText(new UiSelector().className("android.widget.LinearLayout"), "Screen timeout");
 		// Select display timeout
 		timeout.clickAndWaitForNewWindow();
 		
 		// Select '10 minutes' for timeout
 		UiObject timeoutItem = new UiObject(new UiSelector().textContains("10 min"));
 		timeoutItem.clickAndWaitForNewWindow();
 		
 		UiDevice.getInstance().pressHome();
 	}
 	
 	public void testAddGoogleAccount() throws UiObjectNotFoundException{
 		File file = new File("/data/local/tmp/account.info");
 		
 		BufferedReader reader = null;
 		GoogleAccount account = null;
 		try{
 			reader = new BufferedReader(new FileReader(file));
 			String line = reader.readLine();
 			account = GoogleAccount.fromJson(line);
 		}catch(IOException e){
 			fail(e.getMessage());
 		}catch(JSONException e){
 			fail(e.getMessage());
 		}
 		
 		UiScrollable accountTypeList = new UiScrollable(new UiSelector().className("android.widget.ListView"));
 		UiObject google = accountTypeList.getChildByText(new UiSelector().className("android.widget.LinearLayout"), "Google");
 		
 		// Select 'Google'
 		google.clickAndWaitForNewWindow();
 		
 		// Find 'Existing' button
 		UiObject existingButton = new UiObject(new UiSelector().textMatches("Existing"));
 		existingButton.clickAndWaitForNewWindow();
 		
 		// Press 'OK' button if additional dialog(Samsung keyboard) appeared
 		try{
			System.out.println("Trying to dismiss Samsung keyboard popup...");
			UiObject okBtn = new UiObject(new UiSelector().textMatches("OK"));
			okBtn.clickAndWaitForNewWindow(1000);
 		}catch(UiObjectNotFoundException e){
 			System.out.println("Additional dialog not appeared");
 		}
 		
		// Press 'Dismiss' button if additional dialog(Keyboard tips) appeared
		try{
			System.out.println("Trying to dismiss Samsung keyboard tips...");
			UiObject dismissBtn = new UiObject(new UiSelector().textMatches("Dismiss"));
			dismissBtn.clickAndWaitForNewWindow(1000);
		}catch(UiObjectNotFoundException e){
			System.out.println("Additional dialog not appeared.");
		}
		
 		// Find 'Email' field
 		UiObject email = new UiObject(new UiSelector().textContains("Email"));
 		email.click();
 		email.setText(account.email);
 		
 		// Find 'Password' Field
 		UiObject password = new UiObject(new UiSelector().className(android.widget.EditText.class).instance(1));
 		password.click();
 		password.setText(account.password);
 		
 		UiObject nextBtn = new UiObject(new UiSelector().description("Next"));
 		nextBtn.clickAndWaitForNewWindow();
 		
 		// Find terms of service 'OK' button
 		UiObject tosOk = new UiObject(new UiSelector().textMatches("OK"));
 		tosOk.clickAndWaitForNewWindow();
 		
 		// Skip Google+ signup
 		try{
 			UiObject skipGp = new UiObject(new UiSelector().textMatches("Not now"));
 			skipGp.clickAndWaitForNewWindow(1500);
 		}catch(UiObjectNotFoundException e){
 			System.out.println("Google+ signup has already done");
 		}
 		
 		// Proceed to next (Google Services)
 		try{
 			UiObject skipGs = new UiObject(new UiSelector().description("Next"));
 			skipGs.clickAndWaitForNewWindow(1500);
 		}catch(UiObjectNotFoundException e){
 			System.out.println("Google services screen not found");
 		}
 		
 		// Skip Entertainment
 		try{
 			UiObject skipEnt = new UiObject(new UiSelector().textMatches("Not now"));
 			skipEnt.clickAndWaitForNewWindow(1500);
 		}catch(UiObjectNotFoundException e){
 			System.out.println("Payment setup has already done");
 		}
 		
 		// Proceed final step (Account sign-in successful)
 		try{
 			UiObject finalNext = new UiObject(new UiSelector().description("Next"));
 			finalNext.clickAndWaitForNewWindow(1500);
 		}catch(UiObjectNotFoundException e){
 			System.out.println("Account setup confirmation screen not found");
 		}
 		
 		// Finished!
 		UiDevice.getInstance().pressHome();
 	}
 }
