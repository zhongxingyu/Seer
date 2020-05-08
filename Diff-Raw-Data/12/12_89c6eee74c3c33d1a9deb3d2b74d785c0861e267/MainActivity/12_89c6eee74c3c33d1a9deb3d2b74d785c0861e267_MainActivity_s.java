 package com.timetravellingtreasurechest.app;
 
 import android.media.AudioManager;
 import android.os.Bundle;
 import android.os.Environment;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.view.Display;
 import android.view.Gravity;
 import android.view.Menu;
 import android.view.Surface;
 import android.view.View;
 import android.view.Window;
 import android.view.WindowManager;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.FrameLayout;
 import android.widget.Toast;
 import android.graphics.ImageFormat;
 import android.graphics.Rect;
 import android.hardware.Camera;
 import android.hardware.Camera.PictureCallback;
 import android.hardware.Camera.Size;
 
 import com.googlecode.javacv.cpp.opencv_core.CvMat;
 import com.googlecode.javacv.cpp.opencv_highgui;
 import com.timetravellingtreasurechest.app.R;
 import com.timetravellingtreasurechest.camera.Preview;
 import com.timetravellingtreasurechest.report.ReportData;
 import com.timetravellingtreasurechest.services.*;
 
 public class MainActivity extends Activity {
 
 	public static String REPORT = "ReportData";
 	// Dependencies
 	public static Preview cameraSurfaceView;
 	public static ReportData latestReport;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		ServiceServer.setAndroidContext(getApplicationContext());
 		ServiceServer.setFacialFeatureService(new AndroidFacialFeatureService(
 				this.getApplicationContext()));
 		ServiceServer
 				.setReportGeneratorService(new AndroidReportGeneratorService());
 
 		setContentView(R.layout.activity_main);
 		addLegacyOverflowButton(this.getWindow());
 
 		// Setup the FrameLayout with the Camera Preview Screen
 		cameraSurfaceView = new Preview(this);
 		FrameLayout preview = (FrameLayout) this
 				.findViewById(R.id.PreviewFrame);
 		if (preview == null) {
 			System.out.println("PreviewFrame not found!");
 		} else {
 			System.out.println("PreviewFrame found");
 			preview.addView(cameraSurfaceView);
 		}
 
 		Button takeAPicture = (Button) this.findViewById(R.id.TakePhoto);
 		takeAPicture.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				// generateAndViewReport(getPictureFromFile("face"));
 				Camera camera = cameraSurfaceView.getCamera();
 				AudioManager mgr = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
 				mgr.setStreamMute(AudioManager.STREAM_SYSTEM, true);
 				camera.takePicture(null, null, new HandlePictureStorage());
 			}
 		});
 
 	}
 
 	private class HandlePictureStorage implements PictureCallback {
 
 		@Override
 		public void onPictureTaken(byte[] picture, Camera camera) {
 			cameraSurfaceView.stopPreview();
 			Size size = camera.getParameters().getPictureSize();
 			CvMat cvPicture = ImageConverter
 					.getCvMatFromRawImage(picture, new Rect(0, 0, size.width,
 							size.height), camera.getParameters()
 							.getPictureFormat() == ImageFormat.NV21);
 			
 			Display display = ((WindowManager)ServiceServer.getAndroidContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
 						
 	        if(display.getRotation() == Surface.ROTATION_0)
 	        	cvPicture = AndroidFacialFeatureService.cvRotateStep(cvPicture, 3);
 //	        else if (display.getRotation() == Surface.ROTATION_90)
 //	        	cvPicture = AndroidFacialFeatureService.cvRotateStep(cvPicture, 4);
 	        else if (display.getRotation() == Surface.ROTATION_180)
 	        	cvPicture = AndroidFacialFeatureService.cvRotateStep(cvPicture, 1);
 	        else if (display.getRotation() == Surface.ROTATION_270)
 	        	cvPicture = AndroidFacialFeatureService.cvRotateStep(cvPicture, 2);
 	       
 			
 			generateAndViewReport(cvPicture);
 			cameraSurfaceView.startPreview();
 		}
 	}
 
 	private void generateAndViewReport(CvMat cvPicture) {
 		Intent myIntent = new Intent();
 		myIntent.setClassName(MainActivity.this,
 				"com.timetravellingtreasurechest.gui.ManageReportActivity");
 
 		ReportData report = ServiceServer.getReportGeneratorService()
 				.getReport(
 						ServiceServer.getFacialFeatureService().getFeatures(
 								cvPicture), cvPicture);
 		
 		if (report.reportSucessful() == false) {
 			Toast toast = Toast.makeText(ServiceServer.getAndroidContext(), "No face detected", Toast.LENGTH_LONG);
 			toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
 			toast.show();
 			cameraSurfaceView.startPreview();
 			return;
 		}
 		
 		//myIntent.putExtra(MainActivity.REPORT, report);
 		if(latestReport != null && latestReport.getOriginalImage() != null)
 			latestReport.getOriginalImage().deallocate();
 		latestReport = report;
 		System.out.println("Starting new activity");
 		startActivity(myIntent);
 		System.out.println("Activity returned");
 	}
 	
 	public static void addLegacyOverflowButton(Window window) {
 		if (window.peekDecorView() == null) {
 			throw new RuntimeException("Must call addLegacyOverflowButton() after setContentView()");
 		}
 		try {
 			window.addFlags(WindowManager.LayoutParams.class.getField("FLAG_NEEDS_MENU_KEY").getInt(null));
 		}
 		catch (NoSuchFieldException e) {
 			// Ignore since this field won't exist in most versions of Android
 		} catch (IllegalAccessException e) {
 			System.out.println("Could not access FLAG_NEEDS_MENU_KEY in addLegacyOverflowButton()");
 	}
 }
 
 	@SuppressWarnings("unused")
 	private CvMat getPictureFromFile(String name) /* throws IOException */{
 		return (CvMat) opencv_highgui.cvLoadImageM(Environment
 				.getExternalStorageDirectory().getPath() + name + ".jpg");
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
     	menu.add("Report History").setIntent(new Intent().setClassName(ServiceServer.getAndroidContext(), "com.timetravellingtreasurechest.gui.ReportHistoryActivity"));
     	getMenuInflater().inflate(R.menu.report_history, menu);
         return true;
 	}
 
 }
