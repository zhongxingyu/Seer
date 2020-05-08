 package org.sas04225.cameraapp;
 
 import java.io.IOException;
 
import android.R;
import android.os.Bundle;
 import android.app.Activity;
import android.view.Menu;
import android.hardware.Camera;
 import android.graphics.PixelFormat;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 import android.view.View;
 import android.widget.Button;
 
 
 public class MainActivity extends Activity implements SurfaceHolder.Callback{
 	Camera camera;
 	SurfaceView surfaceview;
 	SurfaceHolder surfaceholder;
 	boolean preview = false;
 	
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_list_item);
 		Button buttonStartCameraPreview = (Button)findViewById(R.id.button1);
 	       Button buttonStopCameraPreview = (Button)findViewById(R.id.button2);
 	       getWindow().setFormat(PixelFormat.UNKNOWN);
 	       surfaceview = (SurfaceView)findViewById(R.id.surfaceview);
 	       surfaceholder = surfaceview.getHolder();
 	       surfaceholder.addCallback(this);
 	       surfaceholder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
 	      buttonStartCameraPreview.setOnClickListener(new Button.OnClickListener(){
 	    	  
 
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				if (!preview){
 					camera= Camera.open();
 					if (camera != null){
 						try{
 							camera.setPreviewDisplay(surfaceholder);
 						       camera.startPreview();
 						       preview = true;
 						}catch (IOException e){
 							e.printStackTrace();
 							
 						}
 					}
 				}
 			}
 	    	  
 	      });
 	      buttonStopCameraPreview.setOnClickListener(new Button.OnClickListener(){
 
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				if(camera != null && preview){
 				     camera.stopPreview();
 				     camera.release();
 				     camera = null;
 				     preview=false;
 				
 			}
 	    	  
 	      }});
 	}
 
 	
 	//public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 //		getMenuInflater().inflate(R.menu.activity_main);
 	//	return true;
 	//}
 
 	@Override
 	public void surfaceChanged(SurfaceHolder holder, int format, int width,
 			int height) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void surfaceCreated(SurfaceHolder holder) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void surfaceDestroyed(SurfaceHolder holder) {
 		// TODO Auto-generated method stub
 		
 	}
 
 }
