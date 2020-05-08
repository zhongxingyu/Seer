 package edu.vu.augmented.reality;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.pm.PackageManager;
 import android.graphics.Typeface;
 import android.hardware.Camera;
 import android.os.Bundle;
 import android.view.Gravity;
 import android.view.Menu;
 import android.view.View;
 import android.widget.FrameLayout;
 import android.widget.TextView;
 
 public class MainActivity extends Activity {
 
     private Camera mCamera;
     private CameraPreview mPreview;
     
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         if (checkCameraHardware(this)){
 	        setContentView(R.layout.activity_main);
 	        
 	        // Create an instance of Camera
 	        mCamera = getCameraInstance();
	
 	        // Create our Preview view and set it as the content of our activity.
 	        mPreview = new CameraPreview(this, mCamera);
 	        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
 	        preview.addView(mPreview,0);
         } else {
 	        TextView errorView;
 	        errorView = new TextView(getApplicationContext());
 	        errorView.setBackgroundResource(android.R.drawable.dark_header);
 	        errorView.setGravity(Gravity.CENTER_VERTICAL);
 	        errorView.setFocusable(false);
 	        errorView.setClickable(false);//TODO figure out why the partition is clickable...
 	        errorView.setLongClickable(false);
 	        errorView.setOnClickListener(new View.OnClickListener() {
 	            
 	            @Override
 	            public void onClick(View v) {//Temporary so the app doesn't crash when partitions are clicked
 	            }
 	        });
 	        errorView.setTextSize((float) 14.0);
 	        errorView.setTypeface(Typeface.DEFAULT_BOLD);
 	        errorView.setText("Sorry, your device either does not have a camera or its camera is not compatible with this application.");
 	        setContentView(errorView);
         }
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_main, menu);
         return true;
     }
     
     private boolean checkCameraHardware(Context context) {
         if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
             return true;
         } else {
             return false;
         }
     }
     
     /** A safe way to get an instance of the Camera object. */
     public static Camera getCameraInstance(){
         Camera c = null;
         try {
             c = Camera.open(); // attempt to get a Camera instance
         }
         catch (Exception e){
             // Camera is not available (in use or does not exist)
         }
         return c; // returns null if camera is unavailable
     }
 }
