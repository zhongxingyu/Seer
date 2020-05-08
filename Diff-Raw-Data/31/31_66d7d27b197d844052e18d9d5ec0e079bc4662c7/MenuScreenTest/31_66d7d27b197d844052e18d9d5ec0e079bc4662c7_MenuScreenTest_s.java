 package edu.chl.asciicam.activity.test;
 
 //Copyright 2012 Robin Braaf, Ossian Madisson, Martin Thrnesson, Fredrik Hansson and Jonas strm.
 //
 //This file is part of Asciicam.
 //
 //Asciicam is free software: you can redistribute it and/or modify
 //it under the terms of the GNU General Public License as published by
 //the Free Software Foundation, either version 3 of the License, or
 //(at your option) any later version.
 //
 //Asciicam is distributed in the hope that it will be useful,
 //but WITHOUT ANY WARRANTY; without even the implied warranty of
 //MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 //GNU General Public License for more details.
 //
 //You should have received a copy of the GNU General Public License
 //along with Asciicam.  If not, see <http://www.gnu.org/licenses/>.
 
 import android.app.Activity;
 import android.app.Instrumentation.ActivityMonitor;
 import android.test.ActivityInstrumentationTestCase2;
 import android.widget.Button;
import edu.chl.asciicam.activity.MenuScreen;
 import edu.chl.asciicam.activity.CameraScreen;
 import edu.chl.asciicam.activity.PreviewScreen;
 
 public class MenuScreenTest extends ActivityInstrumentationTestCase2<MenuScreen> {
 
 	private Activity mActivity;
 		
 	public MenuScreenTest() {
 		super("edu.chl.asciicam.activity", MenuScreen.class);
 	}
 	
 	@Override
 	protected void setUp() throws Exception{
 		super.setUp();
 		
 		//Disabling the touch function to make the activity listen to the test class
 		setActivityInitialTouchMode(false);
 		mActivity = getActivity();		
 	}
 	
 	public void testTakePicButton(){
 		
 		//Adding a monitor to CameraScreen
 		ActivityMonitor activityMonitor = getInstrumentation().addMonitor(CameraScreen.class.getName(), null, false);
 		
 		//Initiating the take_Pic_B to test if it works correctly
 		mActivity.runOnUiThread(new Runnable() {
 			Button button = ((MenuScreen) mActivity).getTakePicButton();
 			public void run(){
 				//Starting CameraScreen activity
 				button.performClick();				
 			}
 		});
 		
 		//Get context from CameraScreen to determine if test succeeded
 		CameraScreen cameraScreen = (CameraScreen) getInstrumentation().waitForMonitor(activityMonitor);
 		
 		//Determining that cameraScreen != null
 		assertNotNull(cameraScreen);
 		cameraScreen.finish();
 	}
 	
 	public void testLoadPicButton(){
 		
 		//Adding a monitor to PreviewScreen
 		ActivityMonitor activityMonitor = getInstrumentation().addMonitor(PreviewScreen.class.getName(), null, false);
 		
 		mActivity.runOnUiThread(new Runnable() {
 			Button button = ((MenuScreen) mActivity).getLoadPicButton();
 			public void run(){
 				//Starting activity
 				button.performClick();
 				
 			}		
 		});
 		
 		//Get context from CameraScreen to determine if test succeeded
 				PreviewScreen previewScreen = (PreviewScreen) getInstrumentation().waitForMonitor(activityMonitor);
 				
 				//Determining that cameraScreen != null
 				assertNotNull(previewScreen);
 				previewScreen.finish();		
 	}
 	
 	public void testOptionButton(){
		// TODO Activity OptionScreen
 	}
 }
