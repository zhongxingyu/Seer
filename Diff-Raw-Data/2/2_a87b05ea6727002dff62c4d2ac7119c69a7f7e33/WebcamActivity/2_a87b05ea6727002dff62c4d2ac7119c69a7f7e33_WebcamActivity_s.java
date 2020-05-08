 package com.thomasbiddle.puppywood;
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.Spinner;
 import android.widget.Toast;
 
 public class WebcamActivity extends Activity {
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.webcam);
     }
     
     public void startBeechmontCamera(View v) {
     	Spinner sp = (Spinner) findViewById(R.id.beechmontWebcamSpinner);
     	String currentWebcam = sp.getSelectedItem().toString();
     	Toast t = Toast.makeText(getApplicationContext(), currentWebcam, Toast.LENGTH_SHORT);
     	t.show();
     	Intent intent = new Intent();
     	// Can't have just an uninstantiated class, setting it to base to begin will re-set it
     	// base on the selection below.
     	Class cameraClass = com.thomasbiddle.puppywood.cameras.CameraBase.class;
 
     	// WTF Java - You didn't implement switch conditionals by string until JDK7?!
     	// Using If/ElseIf for backwards compatibility.
     	if (currentWebcam.equalsIgnoreCase("Camera 1")) {
     		cameraClass = com.thomasbiddle.puppywood.cameras.BeechmontCamera1.class;
     	}
     	else if(currentWebcam.equalsIgnoreCase("Camera 2")) {
     		cameraClass = com.thomasbiddle.puppywood.cameras.BeechmontCamera2.class;
     	}
 		else if(currentWebcam.equalsIgnoreCase("Camera 3")) {
 			cameraClass = com.thomasbiddle.puppywood.cameras.BeechmontCamera3.class;
 		}
 		else if(currentWebcam.equalsIgnoreCase("Camera 4")) {
 			cameraClass = com.thomasbiddle.puppywood.cameras.BeechmontCamera4.class;
 		}
     	intent.setClass(this, cameraClass);
     	startActivity(intent);	
     }
     
     public void goToCamera(View v) {
     	Spinner sp = (Spinner) findViewById(R.id.Spinner_Webcam);
     	String currentWebcam = sp.getSelectedItem().toString();
     	Toast t = Toast.makeText(getApplicationContext(), currentWebcam, Toast.LENGTH_SHORT);
     	t.show();
     	Intent intent = new Intent();
     	// Can't have just an uninstantiated class, setting it to base to begin will re-set it
     	// base on the selection below.
     	Class cameraClass = com.thomasbiddle.puppywood.cameras.CameraBase.class;
 
     	// WTF Java - You didn't implement switch conditionals by string until JDK7?!
     	// Using If/ElseIf for backwards compatibility.
     	if (currentWebcam.equalsIgnoreCase("Quad Camera 1")) {
     		cameraClass = com.thomasbiddle.puppywood.cameras.CameraQuad1.class;
     	}
     	else if(currentWebcam.equalsIgnoreCase("Quad Camera 2")) {
     		cameraClass = com.thomasbiddle.puppywood.cameras.CameraQuad2.class;
     	}
 		else if(currentWebcam.equalsIgnoreCase("Camera 1")) {
 			cameraClass = com.thomasbiddle.puppywood.cameras.Camera1.class;
 		}
 		else if(currentWebcam.equalsIgnoreCase("Camera 2")) {
 			cameraClass = com.thomasbiddle.puppywood.cameras.Camera2.class;
 		}
 		else if(currentWebcam.equalsIgnoreCase("Camera 3")) {
 			cameraClass = com.thomasbiddle.puppywood.cameras.Camera3.class;
 		}
 		else if(currentWebcam.equalsIgnoreCase("Camera 4")) {
 			cameraClass = com.thomasbiddle.puppywood.cameras.Camera4.class;
 		}
 		else if(currentWebcam.equalsIgnoreCase("Camera 5")) {
 			cameraClass = com.thomasbiddle.puppywood.cameras.Camera5.class;
 		}
 		else if(currentWebcam.equalsIgnoreCase("Camera 6")) {
			cameraClass = com.thomasbiddle.puppywood.cameras.Camera1.class;
 		}
 		else if(currentWebcam.equalsIgnoreCase("Camera 7")) {
 			cameraClass = com.thomasbiddle.puppywood.cameras.Camera7.class;
 		}
 		else if(currentWebcam.equalsIgnoreCase("Camera 8")) {
 			cameraClass = com.thomasbiddle.puppywood.cameras.Camera8.class;
 		}
     	intent.setClass(this, cameraClass);
     	startActivity(intent);
     } 
    
 }
