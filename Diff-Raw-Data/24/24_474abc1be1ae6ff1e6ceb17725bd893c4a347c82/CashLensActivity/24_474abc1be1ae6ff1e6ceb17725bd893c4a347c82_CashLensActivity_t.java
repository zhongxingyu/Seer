 package com.udesign.cashlens;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.graphics.Color;
 import android.hardware.Camera;
import android.hardware.Camera.Size;
 import android.os.Bundle;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 import android.view.View;
 import android.widget.Button;
 import android.widget.LinearLayout;
 import android.util.Log;
 import android.widget.Toast;
 
 public class CashLensActivity extends Activity {
 	private static String msgTitle1 = "No accounts found";
 	private static String msgMsg1 =  "Tap screen to create a new account";
 	private static String msgTitle2 = "Camera error";
 	private static String msgMsg2 =  "Please run on a device with camera";
 
 	private LinearLayout ln;
 	private Button createAccountBtn;
 	private android.hardware.Camera camera;
 	private SurfaceView cameraSurface;
 	private boolean inPreview=false;
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 
 		initInstance();
 	}
 
 	private void initInstance()
 	{
 		ln = (LinearLayout)findViewById(R.id.mainwindow);
 		ln.setBackgroundColor(Color.argb(0xf0, 0xf0, 0xf0, 0xf0));    	
 		createAccountBtn = new Button(this);
 		createAccountBtn.setText("Open Account");
 		cameraSurface = new SurfaceView(this);        
 		SurfaceHolder holder = cameraSurface.getHolder();
 		holder.addCallback(surfaceCallback);
 		holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
 
 		ln.addView(createAccountBtn);
 		ln.addView(cameraSurface);
 		createAccountBtn.setOnClickListener(new Button.OnClickListener() { public void onClick (View v){ msg(msgTitle1, msgMsg1); }});    	
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 
 		camera=Camera.open();
 	}
 
 	@Override
 	public void onPause() {
 		if (inPreview) {
 			camera.stopPreview();
 		}
 
 		camera.release();
 		camera = null;
 		inPreview = false;
 
 		super.onPause();
 	}
 
 	private void msg(String title, String msg)
 	{
 		AlertDialog alertDialog;
 		alertDialog = new AlertDialog.Builder(this).create();
 		alertDialog.setTitle(title);
 		alertDialog.setMessage(msg);
 		alertDialog.show();
 	}
 
 	private Camera.Size getBestPreviewSize(int width, int height,
 			Camera.Parameters parameters) {
 		Camera.Size result = null;
 
 		for (Camera.Size size : parameters.getSupportedPreviewSizes()) {
 			if (size.width <= width && size.height <= height) {
 				if (result == null) {
 					result = size;
 				}
 				else {
 					int resultArea = result.width*result.height;
 					int newArea = size.width*size.height;
 
 					if (newArea > resultArea) {
 						result = size;
 					}
 				}
 			}
 		}
 
 		return(result);
 	}
 
 	SurfaceHolder.Callback surfaceCallback = new SurfaceHolder.Callback() {
 		public void surfaceCreated(SurfaceHolder holder) {
 			try {
 				camera.setPreviewDisplay(holder);
 			}
 			catch (Throwable t) {
 				Log.e("PreviewDemo-surfaceCallback",
 						"Exception in setPreviewDisplay()", t);
 				Toast
 				.makeText(CashLensActivity.this, t.getMessage(), Toast.LENGTH_LONG)
 				.show();
 			}
 		}
 
 		public void surfaceChanged(SurfaceHolder holder,
 				int format, int width,
 				int height) {
 			Camera.Parameters parameters = camera.getParameters();
			if(parameters != null)
 			{
				Camera.Size size = null;
				if(parameters.getSupportedPreviewSizes() != null)
					size = getBestPreviewSize(width, height,
							parameters);
				
 				if (size != null) {
 					parameters.setPreviewSize(size.width, size.height);
				} else {
					parameters.setPreviewSize(cameraSurface.getWidth(), cameraSurface.getHeight());
 				}
				camera.setParameters(parameters);
				camera.startPreview();
				inPreview = true;

 			} else {
 				msg(msgTitle2, msgMsg2);
 			}
 		}
 
 		public void surfaceDestroyed(SurfaceHolder holder) {
 			// no-op
 		}
 	};
 }
