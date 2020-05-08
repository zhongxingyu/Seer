 package com.example.test;
 import junit.framework.Assert;
 import com.example.*;
 import com.example.R;
 
 import junit.framework.TestCase;
 import android.app.Activity;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.net.wifi.WifiManager;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.Button;
 import android.widget.CompoundButton;
 import android.widget.TextView;
 import android.widget.ToggleButton;
 import android.test.ActivityInstrumentationTestCase2;
 import android.test.ViewAsserts;
 import android.test.suitebuilder.annotation.SmallTest;
 
 public class WifiLocatorTest extends ActivityInstrumentationTestCase2<WifiLocator> {
//	WifiLocator wifiLocator = getActivity();
 	TextView WifiState;
 	ToggleButton OnOff;
 	TextView textStatus;
 	Button Scan;
 	public WifiLocatorTest(Class<WifiLocator> name) {
 		super(name);
 	}
 
 	public WifiLocatorTest(){
 		super("com.example.WifiLocator", WifiLocator.class);
 	}
 	protected void setUp() throws Exception {
 		super.setUp();
		WifiLocator wifiLocator = getActivity();
 		OnOff = (ToggleButton) wifiLocator.findViewById(R.id.onoff);
 		WifiState = (TextView)wifiLocator.findViewById(R.id.wifistate);
 	    Scan = (Button)wifiLocator.findViewById(R.id.scan);
  		textStatus = (TextView) wifiLocator.findViewById(R.id.textStatus);
 	}
 	

	public void testViewsCreateTest(){
 		assertNotNull(getActivity());
 		assertNotNull(OnOff);
 		assertNotNull(WifiState);
 		assertNotNull(Scan);
 		assertNotNull(textStatus);
 	}
 	
	public void testNameTest0(){
		
	}
	
	public void testNameTest1(){
		
	}
	
	public void testNameTest2(){
		
	}
	
	public void testNameTest3(){
		
	}
	
	public void testOnCreateTest(){
 		assertTrue("Test On/Off Button when starting up", OnOff.getText().toString().equals(WifiManager.WIFI_STATE_CHANGED_ACTION));
 		assertTrue("Test Wifi State when starting up", WifiState.getText().toString().equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
 	}
 	
 }
