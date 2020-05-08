 package com.example.tensioncamapp_project.test;
 
 import android.hardware.Camera;
 import android.hardware.Camera.Parameters;
 import android.test.ActivityInstrumentationTestCase2;
 import android.test.TouchUtils;
 
 import com.example.tensioncamapp_project.CameraActivity;
 
 public class CameraActivityTest extends ActivityInstrumentationTestCase2<CameraActivity> {
 
 	private static CameraActivity cam;
 	
 	public CameraActivityTest () {
 		super(CameraActivity.class);
 	}
 
 	protected void setUp() throws Exception {
 		super.setUp();
 
 		setActivityInitialTouchMode(false);
 
 		cam = getActivity();
 	}
 	
	/**These tests requires a real device */
	
 	public void testCaptureButton() {
		TouchUtils.clickView(this, cam.findViewById(com.example.tensioncamapp_project.R.id.button_capture_symbol));
 		
	}
 	
 	//Checks that the method getCameraInstance returns an object of the class Camera
 	public void testGetCameraInstance() {
         assertTrue(cam.getCameraInstance().getClass() == Camera.class);
  
 	}
 	
 	
 }
