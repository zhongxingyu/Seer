 package com.nickotter.randomname.test;
 
 import com.jayway.android.robotium.solo.Solo;
 import com.nickotter.randomname.MainActivity;
 
 import android.test.ActivityInstrumentationTestCase2;
 
 public class MainActivityTest extends
 		ActivityInstrumentationTestCase2<MainActivity> {
 
 	private Solo solo;
 
 	public MainActivityTest() {
 		super(MainActivity.class);
 
 	}
 
 	protected void setUp() throws Exception {
 		super.setUp();
 		solo = new Solo(getInstrumentation(), getActivity());
 	}
 	
 	public void testOpenSideDrawer() {
 		
 		solo.assertCurrentActivity("The App should launch", MainActivity.class);
 		
 		solo.clickOnImage(0);
		assertFalse(getActivity().mNav.isClosed());
 		
 	}
 	
 	public void testChangeActionBarTab() {
 		
 		solo.assertCurrentActivity("The App should launch", MainActivity.class);
 		
 	}
 
 
 }
