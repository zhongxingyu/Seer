 package com.example.tensioncamapp_project.test;
 
 import android.hardware.Camera;
 import android.hardware.Camera.Parameters;
 import android.test.ActivityInstrumentationTestCase2;
 import android.test.TouchUtils;
 
 import com.example.tensioncamapp_project.CameraActivity;
 import com.example.tensioncamapp_project.FileHandler;
 
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
 	
 	/**Checks that a picture is being saved once the capture button is pressed*/
 	public void testCaptureButton() {
 		
 		FileHandler.deleteFromExternalStorage(); //wipes the directory
 		TouchUtils.clickView(this, cam.findViewById(com.example.tensioncamapp_project.R.id.button_capture_symbol)); //captures a picture
 		//cam.delay(); change visibility from private to protected in CameraActivity
 		assertTrue(!FileHandler.pathToString().isEmpty()); //checks that there is a file stores in the directory
 	
	}	
 		
 	//Checks that the method getCameraInstance returns an object of the class Camera
 	public void testGetCameraInstance() {
         assertTrue(cam.getCameraInstance().getClass() == Camera.class);
  
 	}
 	
 	
 }
