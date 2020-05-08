 package com.phonegap.geoquestweb;
 
 
 import org.junit.Test;
 import org.junit.runner.RunWith;
 
 import android.test.ActivityInstrumentationTestCase2;
 import android.test.AndroidTestRunner;
 
 //@RunWith(AndroidTestRunner.class)
 public class TestApp extends ActivityInstrumentationTestCase2<MainActivity> {
 
 	public TestApp() {
 		super(MainActivity.class);
 	}
 	
	@Test
 	public void testApp(){
 		assertNotNull(getActivity());
 	}
 }
