 package com.gromsoft.onlyroad.test;
 
import android.app.Instrumentation;
 import android.test.ActivityInstrumentationTestCase2;
 import android.test.suitebuilder.annotation.SmallTest;
 
import com.actionbarsherlock.app.ActionBar;
 import com.gromsoft.onlyroad.MainActivity;
 
 
 public class MainActivityTest extends ActivityInstrumentationTestCase2<MainActivity> {
 
 	MainActivity mMainActivity;
	Instrumentation mInstrumentation; 
	com.actionbarsherlock.app.ActionBar mActionBar;
 	
 	public MainActivityTest(String name) {
 		super(MainActivity.class);
 	}
 
 	public MainActivityTest(){
		this("Only Road MainActivity Tests");
 	}
 	
 	protected void setUp() throws Exception {
 		
 		super.setUp();
 
		setActivityInitialTouchMode(false);
 		mMainActivity = getActivity();
		mInstrumentation = getInstrumentation();
		mActionBar = mMainActivity.getSupportActionBar();
 	}
 
 	protected void tearDown() throws Exception {
 		super.tearDown();
 	}
 
 	@SmallTest
	public void testActionBarNavigationMode(){
		assertEquals("    ", ActionBar.NAVIGATION_MODE_TABS, mActionBar.getNavigationMode());
 	}
 	
 }
