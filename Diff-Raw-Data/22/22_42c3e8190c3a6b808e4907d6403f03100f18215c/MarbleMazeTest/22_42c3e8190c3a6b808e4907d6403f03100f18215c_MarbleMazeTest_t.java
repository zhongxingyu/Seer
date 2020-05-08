 package com.thoughtworks.mm.test;
 
 import com.jayway.android.robotium.solo.Solo;
 
 import android.test.ActivityInstrumentationTestCase2;
 import com.thoughtworks.mm.MarbleMazeActivity;
 
 public class MarbleMazeTest extends
		ActivityInstrumentationTestCase2 {
 
 	private Solo solo;
 
     public MarbleMazeTest() {
         super("com.thoughtworks.mm", MarbleMazeActivity.class);
     }
 
 	@Override
	public void setUp() throws Exception {
 		super.setUp();
 		solo = new Solo(getInstrumentation(), getActivity());
 	}
 
 
    public void testSample() throws Exception {
    	solo.sendKey(Solo.MENU);
        assertTrue(solo.searchText("Start Method Tracing"));
     }
 
 	@Override
	public void tearDown() throws Exception {
 		try {
 			solo.finalize();
 		} catch (Throwable e) {
 			e.printStackTrace();
 		}
		//getActivity().finish();
 		super.tearDown();
 	}
 
 }
