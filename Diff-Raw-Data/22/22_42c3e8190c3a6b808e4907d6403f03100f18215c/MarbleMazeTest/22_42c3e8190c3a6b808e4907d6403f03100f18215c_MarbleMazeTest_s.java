 package com.thoughtworks.mm.test;
 
 import com.jayway.android.robotium.solo.Solo;
 
 import android.test.ActivityInstrumentationTestCase2;
 import com.thoughtworks.mm.MarbleMazeActivity;
 
 public class MarbleMazeTest extends
		ActivityInstrumentationTestCase2<MarbleMazeActivity> {
 
 	private Solo solo;
 
	public MarbleMazeTest(Class<MarbleMazeActivity> activityClass) {
		super("com.thoughtworks.mm", activityClass);
	}

     public MarbleMazeTest() {
         super("com.thoughtworks.mm", MarbleMazeActivity.class);
     }
 
 	@Override
	protected void setUp() throws Exception {
 		super.setUp();
 		solo = new Solo(getInstrumentation(), getActivity());
 	}
 
 
    public void testSample() {
        assertTrue(true);
     }
 
 	@Override
	protected void tearDown() throws Exception {
 		try {
 			solo.finalize();
 		} catch (Throwable e) {
 			e.printStackTrace();
 		}
		getActivity().finish();
 		super.tearDown();
 	}
 
 }
