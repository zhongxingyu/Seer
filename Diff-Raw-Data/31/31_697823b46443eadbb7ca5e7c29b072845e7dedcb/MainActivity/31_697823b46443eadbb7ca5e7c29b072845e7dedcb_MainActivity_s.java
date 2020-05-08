 package com.hekai.camera;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.pm.PackageManager;
 import android.hardware.Camera;
 import android.hardware.Camera.PreviewCallback;
 import android.hardware.Camera.Size;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.Button;
 import android.widget.FrameLayout;
 import com.hekai.camera.R;
 
 public class MainActivity extends Activity implements OnClickListener,PreviewCallback{
 	
 	private static final String TAG="MainActivity";
 
 	private Camera mCamera;
     private CameraPreview mPreview;
     
     private FrameLayout preview;
     
     private LayoutInflater mLayoutInflater;
     private View mOverlayView;
     private HistogramView histogramView;
     
     private boolean mIsOverlayShow=false;
     private boolean mIsFirstInit=true;
     
     private Button mCaptureButton,mSwitchButton;
     
     private int mCameraIndex;
 	
     private byte[] buffer;
     
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		
 		mCameraIndex=0;//use back camera default. 
 		
         // Create our Preview view and set it as the content of our activity.
         mPreview = new CameraPreview(this);
         preview = (FrameLayout) findViewById(R.id.camera_preview);
         preview.addView(mPreview);
         
         mCaptureButton=(Button)findViewById(R.id.button_capture);
         mCaptureButton.setOnClickListener(this);
         mSwitchButton=(Button)findViewById(R.id.switch_camera);
         mSwitchButton.setOnClickListener(this);
 		
         mLayoutInflater=LayoutInflater.from(this);
 		mOverlayView=mLayoutInflater.inflate(R.layout.overlay, null);
 	}
 	
 	@Override
 	protected void onResume() {
 		Log.d(TAG, "onResume");
 		super.onResume();
 		
 		startCamera();
 	}
 	
 	@Override
 	protected void onPause() {
 		Log.d(TAG, "onPause");
 		super.onPause();
 		
 		stopCamera();
 		
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 	
 	/** Check if this device has a camera */
 	private boolean checkCameraHardware(Context context) {
 	    if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
 	        // this device has a camera
 	        return true;
 	    } else {
 	        // no camera on this device
 	        return false;
 	    }
 	}
 	
 	/** A safe way to get an instance of the Camera object. */
 	public static Camera getCameraInstance(int index){
 	    Camera c = null;
 	    try {
 	        c = Camera.open(index); // attempt to get a Camera instance
 	    }
 	    catch (Exception e){
 	        // Camera is not available (in use or does not exist)
 	    }
 	    return c; // returns null if camera is unavailable
 	}
 
 	@Override
 	public void onClick(View v) {
 		if(v.equals(mCaptureButton)){
 			showOverlay();
 		}else if(v.equals(mSwitchButton)){
 			switchCamera();
 		}
 	}
 
 	private void startCamera(){
 		preview.removeView(mPreview);
 		mCamera = getCameraInstance(mCameraIndex);
 		mPreview.onResume(mCamera);
 		preview.postDelayed(new Runnable() {
 			@Override
 			public void run() {
				buffer=new byte[640*480*2];
				mCamera.addCallbackBuffer(buffer);
				mCamera.setPreviewCallbackWithBuffer(MainActivity.this);
 			}
 		},1000);
 		preview.addView(mPreview);
 	}
 	
 	private void stopCamera(){
 		mCamera.setPreviewCallback(null);
 		mPreview.onPause();
 	}
 	
 	private void showOverlay(){
 		Log.d(TAG, "showOverlay() mIsOverlayShow="+mIsOverlayShow+",mIsFirstInit="+mIsFirstInit);
 		
 		if(!mIsOverlayShow){
 			if(mIsFirstInit){
 				addContentView(mOverlayView, new LayoutParams(
 						LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
 				
 				mIsFirstInit=false;
 			}else{
 				mOverlayView.setVisibility(View.VISIBLE);
 			}
 			histogramView=new HistogramView(this);
 			histogramView.updatePosition(preview.getWidth()/2, preview.getHeight()/2);
 			preview.addView(histogramView);
 		}else{
 			mOverlayView.setVisibility(View.GONE);
 			preview.removeView(histogramView);
 			histogramView=null;
 		}
 		mIsOverlayShow=!mIsOverlayShow;
 	}
 	
 	private void switchCamera(){
 		int cameraNum=Camera.getNumberOfCameras();
 		if(cameraNum<=1)
 			return;
 		
 		stopCamera();
 		
 		mCameraIndex=(mCameraIndex+1)%2;
 		
 		startCamera();
 	}
 
 	@Override
 	public void onPreviewFrame(byte[] data, Camera camera) {
 		Size size=mCamera.getParameters().getPreviewSize();
 		int imgFormat = mCamera.getParameters().getPreviewFormat();
 //		Log.d(TAG,"data.length = "+data.length+" , width = "+size.width+" , height = "
 //				+size.height+" , format = "+imgFormat);
 		if(histogramView!=null){
 			histogramView.updateData(data, size.width, size.height, imgFormat);
 			histogramView.invalidate();
 		}
 		
 		mCamera.addCallbackBuffer(buffer);
 //		YuvImage image = new YuvImage(data, imgFormat, size.width, size.height, null);
 	}
 }
