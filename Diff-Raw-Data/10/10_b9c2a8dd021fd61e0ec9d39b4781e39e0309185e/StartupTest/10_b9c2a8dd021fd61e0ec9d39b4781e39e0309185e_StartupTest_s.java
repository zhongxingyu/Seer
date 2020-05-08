 package com.chalmers.schmaps.test;
 
 import com.chalmers.schmaps.Startup;
 import android.content.Intent;
 import android.test.ActivityInstrumentationTestCase2;
 
 public class StartupTest extends ActivityInstrumentationTestCase2<Startup> {
 
 	private Startup startupActivity;
 	private String stringIntentActivity;
 	private Intent startupIntent;
 
 	public StartupTest() {
 		super(Startup.class);
 	}
 	@Override
 	protected void setUp() throws Exception {
 		super.setUp();
 		setActivityInitialTouchMode(false);
 		this.startupActivity = super.getActivity();
 		this.startupIntent = startupActivity.getIntentForMenuActivity();
		this.stringIntentActivity = startupIntent.getAction();	
 		}
 	
 	@Override
 	protected void tearDown() throws Exception {
 		super.tearDown();
 	}
 	public void testForStartingMenuActivity(){
 		assertEquals("android.intent.action.MENUACTIVITY", stringIntentActivity);
 	}
 }
