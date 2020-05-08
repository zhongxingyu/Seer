 package com.example.mitflashlight;///this should be your package file name!
 
 import android.graphics.Color;
 import android.hardware.Camera;
 import android.hardware.Camera.Parameters;
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Context;
 import android.content.pm.PackageManager;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 
 public class MainActivity extends Activity {
 	
 	//***Define your Instance Variables***
 	//I've left mine here but feel free to do it your own way
 	
 	private boolean isLightOn = false;
 	private Camera camera;
 	private Button button;
 	private View v;
 	
 	
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		
 	   /* Retrieve the layout elements (buttonFlashlight and backgroundView) and cast them
 		*  into objects we can use here. 
 		*/
 		
 		button = (Button) findViewById(R.id.buttonFlashlight);
 		v = //INSERT CODE the same as above but for backgroundView and cast it into a View object v! (one line)
 
 		
 		/*
 		 * Retrieve the application's context (basically the state in which it's in) and ask it for
 		 * its PackageManager. This PackageManager will then allow us to figure out if the 
 		 * phone has a Camera. You will want to use the hasSystemFeature method of the PackageManager class
 		 * (google it) in an if-statement as I've setup for you below
 		 */
 		
 		Context context = this;		
 		PackageManager pm = context.getPackageManager();
 	
 		//if(INSERT CODE to check if device has camera){ 
 			Log.e("err", "Your device has no camera!");//this prints a message in the LogCat when we run our app!
 			return;//we escape out of here since there's no camera
 		//}
 		
 		
 		
 		//start the camera
 		camera = Camera.open();
 		final Parameters p = camera.getParameters();
 		 
 		
 		///set the clicklistener on our button
 		button.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View arg0) {
 
 				if (isLightOn) {
 					
 					
 					/*
 					 * INSERT CODE here that does the following:
 					 * 1. Prints to the LogCat the state you are about to change the light to ("Flashlight is on/off!")
 					 * 2. Turns on or off the flash (google "setFlashMode" on Parameters of the camera ('p')
 					 * 3. Starts or stops the preview (required for camera access) [e.g. camera.stopPreview(); is run when you want to switch it off]
 					 * 4. Changes the background color of our backgroundView (which we'ved named 'v' above)
 					 * 5. Adjusts the boolean value accordingly (so we can toggle it again next time)
 					 */
 
 
 
 				} else {
 
 					/*
 					 * INSERT CODE here that does the following:
 					 * 1. Prints to the LogCat the state you are about to change the light to ("Flashlight is on/off!")
 					 * 2. Turns on or off the flash (google "setFlashMode" on Parameters of the camera ('p')
 					 * 3. Starts or stops the preview (required for camera access) [e.g. camera.stopPreview(); is run when you want to switch it off]
					 * 4. Changes the background color of our backgroundView (which we'ved named 'v' above)
 					 * 5. Adjusts the boolean value accordingly (so we can toggle it again next time)
 					 */
 
 
 				}
 
 			}
 		});
 		
 		
 	}
 	
 	@Override
 	protected void onStop() {
 		super.onStop();
 		
 		/*
 		 * Here we handle the case when the app is Stopped (via exit or by the OS)
 		 * [no need for code here, just understand why we've put this here in the onStop Method of the application lifecycle]
 		 */
 		if (camera != null) {
 			camera.release();
 		}
 	}
 
 }
 
