 /**
  * 
  */
 package com.mobileobservinglog.tests;
 
 import java.lang.reflect.Field;
 
 import android.app.Instrumentation;
 import android.test.SingleLaunchActivityTestCase;
 import android.util.Log;
 import android.view.Window;
 import android.view.WindowManager;
 import android.view.WindowManager.LayoutParams;
 
 import com.mobileobservinglog.ObservingLogActivity;
import com.mobileobservinglog.strategies.CustomizeBrightness;
 
 /**
  * @author Joe Rowley
  *
  */
 public class CustomizeBrightnessTest extends SingleLaunchActivityTestCase<ObservingLogActivity>{
 	
 	//Class Under Test
	CustomizeBrightness mCut = null;
 	ObservingLogActivity mAut = null;
 	Instrumentation mInstrumentation = null;
 	
 	public CustomizeBrightnessTest(){
 		super("com.mobileobservinglog", ObservingLogActivity.class);
 	}
 
 	/**
 	 * @throws java.lang.Exception
 	 */
 	@Override
 	public void setUp() throws Exception {
 		super.setUp();
 		mAut = getActivity();
		mCut = new CustomizeBrightness(mAut, mAut);
 		mInstrumentation = getInstrumentation();
 	}
 
 	/**
 	 * @throws java.lang.Exception
 	 */
 	@Override
 	public void tearDown() throws Exception {
 		super.tearDown();
 		mCut = null;
 	}
 	
 	//Test setDimButton
 	public void testsetDimButton() throws Throwable
 	{
 		runTestOnUiThread(new Runnable()
 		{
 			public void run()
 			{
 				mCut.setDimButtons(0.5f);
 				
 				Window window = mAut.getWindow();
 			    LayoutParams layoutParams = window.getAttributes();
 			    try {
 			        Field buttonBrightness = layoutParams.getClass().getField("buttonBrightness");
 			        float brightnessValue = buttonBrightness.getFloat(layoutParams);
 			        assertEquals("Button brightness was not correct", 0.5f, brightnessValue);
 			    } catch (Exception e) {
 			        e.printStackTrace();
 			    }
 			}
 		});
 	}
 	
 	/**
 	 * Test method for {@link com.com.mobileobservinglog.mainityBase#setBacklight()}.
 	 * 
 	 * This test will set the night-mode backlight and then check to see that it's set right.
 	 * @throws Throwable 
 	 */
 	public void testSetBacklight() throws Throwable
 	{
 		runTestOnUiThread(new Runnable()
 		{
 			public void run()
 			{
 				mCut.setBacklight(true);
 				
 				WindowManager.LayoutParams WMLP = mAut.getWindow().getAttributes();
 				Log.d("JoeTest", "Full Brightness is " + WMLP.screenBrightness);
 				assertEquals("Full Brightness was not right", -1.0F, WMLP.screenBrightness);
 				
 				mCut.setBacklight(false);
 				
 				WMLP = mAut.getWindow().getAttributes();
 				Log.d("JoeTest", "night mode Brightness is " + WMLP.screenBrightness);
 				assertTrue("Night Mode Brightness was not less than full", WMLP.screenBrightness > 0.0F);
 			}
 		});
 	}
 }
