 package com.androidhuman.ctsprepare.automator;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 
 import org.json.JSONException;
 
 import android.os.Build;
import android.widget.LinearLayout;
 import android.widget.ListView;
 
 import com.android.uiautomator.core.Configurator;
 import com.android.uiautomator.core.UiDevice;
 import com.android.uiautomator.core.UiObject;
 import com.android.uiautomator.core.UiObjectNotFoundException;
 import com.android.uiautomator.core.UiScrollable;
 import com.android.uiautomator.core.UiSelector;
 import com.android.uiautomator.testrunner.UiAutomatorTestCase;
 
 public class PreCTSAutomate extends UiAutomatorTestCase{
 	
 	private static final int MAX_RETRY = 5;
 
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
 		if(Build.VERSION.SDK_INT >= 18){
 			Configurator config = Configurator.getInstance();
 			config.setWaitForSelectorTimeout(2000); // Set UI wait timeout for 2 seconds
 		}
 		int numRetry = 0;
 		
 		File file = new File("/data/local/tmp/wifi.info");
 		
 		BufferedReader reader = null;
 		WifiAp apData = null;
 		try{
 			reader = new BufferedReader(new FileReader(file));
 			String line = reader.readLine();
 			apData = WifiAp.fromJson(line);
 		}catch(IOException e){
 			fail(e.getMessage());
 		}catch(JSONException e){
 			fail(e.getMessage());
 		}
 		
 		UiObject wifiSwitch = new UiObject(new UiSelector().className("android.widget.Switch"));
 		if(!wifiSwitch.isChecked()){
 			wifiSwitch.clickAndWaitForNewWindow();
 		}
 		
 		// For tablets
 		UiScrollable wifiList = null;
 		
 		wifiList = new UiScrollable(new UiSelector().className(ListView.class).textContains("Network connections"));
 		// Double column mode
 		wifiList = new UiScrollable(new UiSelector().className(ListView.class).instance(1));
 		
 		if(!wifiList.exists()){
 			// Single column mode
 			wifiList = new UiScrollable(new UiSelector().className(ListView.class));
 		}
				
		System.out.println("Wifi AP count : "+wifiList.getChildCount());
 		
 		UiObject ap = null;
 		
 		while(true){
 			try{
 				// Select AP
 				System.out.println("Trying to find AP : "+apData.apName);
 				ap = wifiList.getChildByText(
 						new UiSelector().className("android.widget.LinearLayout"), apData.apName);
 				break;
 			}catch(UiObjectNotFoundException e){
 				if(numRetry>MAX_RETRY){
 					fail("Didn't find AP. Cancel Wi-Fi setting.");
 				}
 				UiDevice.getInstance().waitForWindowUpdate("com.android.settings", 1500);
 			}
 			skipAdditionalDialogsForWifi();
 			numRetry++;
 		}
 		ap.clickAndWaitForNewWindow();
 		
 		numRetry = 0;
 		while(true){
 			try{
 				UiObject editText = new UiObject(new UiSelector().className("android.widget.EditText"));
 				editText.setText(apData.password); // Enter password
 				break;
 			}catch(UiObjectNotFoundException e){	
 				if(numRetry>MAX_RETRY){
 					fail("Could not find authentication dialog.");
 				}
 			}
 			skipAdditionalDialogs();
 			numRetry++;
 		}
 		
 		UiObject connectButton = new UiObject(new UiSelector().text("Connect"));
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
 		if(Build.VERSION.SDK_INT >= 18){
 			Configurator config = Configurator.getInstance();
 			config.setWaitForSelectorTimeout(2000); // Set UI wait timeout for 2 seconds
 		}
 		
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
 		UiObject existingButton = new UiObject(new UiSelector().text("Existing"));
 		existingButton.clickAndWaitForNewWindow();
 		
 		int count = 0;
 		while(true){
 			try{
 				// Find 'Email' field
 				UiObject email = new UiObject(new UiSelector().textContains("Email"));
 				email.click();
 				email.setText(account.email);
 				
 				// Find 'Password' Field
 				UiObject password = new UiObject(new UiSelector().className(android.widget.EditText.class).instance(1));
 				password.click();
 				password.setText(account.password);
 				
 				break;
 			}catch(UiObjectNotFoundException e){
 				count++;
 				if(count>3){
 					fail("Could not find Google account auth screen.");
 				}
 				System.out.println("Dialog found. trying to dismiss...");
 				
 			}
 			
 			skipAdditionalDialogs();
 		}
 		
 		UiObject nextBtn = new UiObject(new UiSelector().description("Next"));
 		nextBtn.clickAndWaitForNewWindow();
 		
 		// Find terms of service 'OK' button
 		UiObject tosOk = new UiObject(new UiSelector().text("OK"));
 		tosOk.clickAndWaitForNewWindow();
 		
 		// Skip Google+ signup
 		try{
 			UiObject skipGp = new UiObject(new UiSelector().text("Not now"));
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
 			UiObject skipEnt = new UiObject(new UiSelector().text("Not now"));
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
 	
 	public void testSetInternetAsDefault() throws UiObjectNotFoundException {
 		if(Build.VERSION.SDK_INT >= 18){
 			Configurator config = Configurator.getInstance();
 			config.setWaitForSelectorTimeout(2000); // Set UI wait timeout for 2 seconds
 		}
 		
 		// Get installed app list
 		UiScrollable deviceAdminList = 
 				new UiScrollable(new UiSelector().className("android.widget.GridView"));
 		
 		// First device admin receiver
 		UiObject internet = 
 				deviceAdminList.getChildByText(
 						new UiSelector().className("android.widget.LinearLayout"), 
 						"Internet");
 		
 		// select internet
 		internet.click();
 		
 		UiObject alwaysBtn = new UiObject(new UiSelector().text("Always"));
 		alwaysBtn.click();
 		
 		UiObject okBtn = new UiObject(new UiSelector().text("OK"));
 		okBtn.click();
 		
 		// Done!
 		UiDevice.getInstance().pressHome();
 		
 	}
 	
 	private void skipAdditionalDialogs(){
 		UiObject dismissDlgBtn = null;
 		
 		try{
 			// If input method dialog popup showed up
 			dismissDlgBtn = new UiObject(new UiSelector().text("OK"));
 			dismissDlgBtn.clickAndWaitForNewWindow(1000);
 		}catch(UiObjectNotFoundException e){}
 		
 		try{
 			// If swype instruction dialog popup showed up
 			dismissDlgBtn = new UiObject(new UiSelector().text("Dismiss"));
 			dismissDlgBtn.clickAndWaitForNewWindow(1000);
 		}catch(UiObjectNotFoundException e){}
 	
 		try{
 			// Wi-Fi calling
 			dismissDlgBtn = new UiObject(new UiSelector().text("Skip"));
 			dismissDlgBtn.clickAndWaitForNewWindow(1000);
 		}catch(UiObjectNotFoundException e){}
 	}
 	
 	private void skipAdditionalDialogsForWifi(){
 		UiObject dismissDlgBtn = null;
 		
 		try{
 			// Wi-Fi calling
 			dismissDlgBtn = new UiObject(new UiSelector().text("Skip"));
 			dismissDlgBtn.clickAndWaitForNewWindow(1000);
 		}catch(UiObjectNotFoundException e){}
 		
 		try{
 			// If input method dialog popup showed up
 			dismissDlgBtn = new UiObject(new UiSelector().text("OK"));
 			dismissDlgBtn.clickAndWaitForNewWindow(1000);
 		}catch(UiObjectNotFoundException e){}
 		
 		try{
 			// If swype instruction dialog popup showed up
 			dismissDlgBtn = new UiObject(new UiSelector().text("Dismiss"));
 			dismissDlgBtn.clickAndWaitForNewWindow(1000);
 		}catch(UiObjectNotFoundException e){}
 	
 		
 	}
 	
 }
