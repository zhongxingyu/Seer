 package com.gromsoft.onlyroad.test;
 
 import android.test.ActivityInstrumentationTestCase2;
 import android.test.suitebuilder.annotation.SmallTest;
 
 import com.gromsoft.onlyroad.MainActivity;
 
 
 public class MainActivityTest extends ActivityInstrumentationTestCase2<MainActivity> {
 
 	MainActivity mMainActivity;
 	
 	public MainActivityTest(String name) {
 		super(MainActivity.class);
 	}
 
 	public MainActivityTest(){
		this("My First Project Test");
 	}
 	
 	protected void setUp() throws Exception {
 		
 		super.setUp();
 
 		mMainActivity = getActivity();
 	}
 
 	protected void tearDown() throws Exception {
 		super.tearDown();
 	}
 
 	@SmallTest
	public void testSomething(){
		fail("Not implemented yet");
 	}
 	
 }
