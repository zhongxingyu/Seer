 package com.github.barcodescanner;
 
 import net.sourceforge.zbar.ImageScanner;
 import net.sourceforge.zbar.Image;
 import net.sourceforge.zbar.Symbol;
 import net.sourceforge.zbar.SymbolSet;
 import net.sourceforge.zbar.Config;
 
 import android.hardware.Camera;
 import android.hardware.Camera.AutoFocusCallback;
 import android.hardware.Camera.PreviewCallback;
 import android.hardware.Camera.Size;
 import android.os.Bundle;
 import android.os.Handler;
 import android.view.Gravity;
 import android.view.View;
 import android.view.Window;
 import android.widget.FrameLayout;
 import android.widget.LinearLayout;
 import android.widget.PopupWindow;
 import android.widget.TextView;
 import android.app.ActionBar.LayoutParams;
 import android.app.Activity;
 import android.content.Intent;
 
 public class CameraActivity extends Activity {
 
 	private BCanalyzer bcAnalyzer;
 	private Camera mCamera;
 	private CameraPreview mPreview;
 	// DrawView object to draw lines on the camera preview
 	DrawView drawLines;
 	public static final int MEDIA_TYPE_IMAGE = 1;
 	private String barcodeInfo;
     private ImageScanner scanner;
     private boolean previewing = true;
     private Handler autoFocusHandler;
     private PopupWindow popUp;
     private boolean clicked = true;
     LinearLayout layout;
     TextView tv;
     LayoutParams params;
 
     static {
 	    System.loadLibrary("iconv");
 	}
     
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		
 		System.out.println(mCamera);
 		super.onCreate(savedInstanceState);
 		requestWindowFeature(Window.FEATURE_NO_TITLE); // hides the title from the camera view
 		setContentView(R.layout.activity_camera);
 		// Create an instance of Camera
 		mCamera = getCameraInstance();
 		
 		autoFocusHandler = new Handler();
 		
 		
         scanner = new ImageScanner();
         scanner.setConfig(0, Config.X_DENSITY, 3);
         scanner.setConfig(0, Config.Y_DENSITY, 3);
         
         popUp = new PopupWindow(this);
         layout = new LinearLayout(this);
         tv = new TextView(this);
         
         params = new LayoutParams(LayoutParams.WRAP_CONTENT,
                 LayoutParams.WRAP_CONTENT);
         layout.setOrientation(LinearLayout.VERTICAL);
         tv.setText("To Scan a barcode hold it steady in the " +
         		"camera view and wait for the application to scan");
         layout.addView(tv, params);
         popUp.setContentView(layout);
 
 
 		// Create an instance of DrawView
 		drawLines = new DrawView(this);
 		System.out.println(mCamera);
 		
 		// Create our Preview view and set it as the content of our activity.
 		mPreview = new CameraPreview(this, mCamera,previewCb,autoFocusCB);
 		FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
 		preview.addView(mPreview);
 		preview.addView(drawLines);
 	}
 	
 	@Override
 	public void onPause(){
 		super.onPause();
 		releaseCamera();
 	}
 
 	/** A safe way to get an instance of the Camera object. */
 	public static Camera getCameraInstance() {
 		Camera c = null;
 		try {
 			c = Camera.open(); // attempt to get a Camera instance
 			System.out.println(c);
 		} catch (Exception e) {
 			// Camera is not available (in use or does not exist)
 		}
 		return c; // returns null if camera is unavailable
 	}
 	
     PreviewCallback previewCb = new PreviewCallback() {
         public void onPreviewFrame(byte[] data, Camera camera) {
             Camera.Parameters parameters = camera.getParameters();
             Size size = parameters.getPreviewSize();
 
             Image barcode = new Image(size.width, size.height, "Y800");
             barcode.setData(data);
 
             int result = scanner.scanImage(barcode);
             barcodeInfo = "";
             
             if (result != 0) {
                 previewing = false;
                 mCamera.setPreviewCallback(null);
                 mCamera.stopPreview();
                 
                 SymbolSet syms = scanner.getResults();
                 for (Symbol sym : syms) {
                 	barcodeInfo = sym.getData();
                 	viewProduct();
                 }
             }
         }
     };
 	
 
 	// instead of adding a listener we can add "android:onClick="methodName" to the activity_camera.xml file
 	// then we can just add the mCamera.takePicture(bla..) to that method. Saves us some code and I think that is
 	// the standard way of doing it.
 	public void helpToScan(View view) {
         if (clicked) {
             popUp.showAtLocation(layout, Gravity.BOTTOM, 10, 50);
             popUp.update(50, 50, 300, 300);
             clicked = false;
            } else {
             popUp.dismiss();
             clicked = true;
            }
 	}
 	
 	private void viewProduct(){
 		Bundle productName = new Bundle();
 		productName.putString("product",barcodeInfo);
 		Intent intent = new Intent(this, AddNewActivity.class);
 		intent.putExtras(productName);
 		startActivity(intent);
 	}
 	
     private Runnable doAutoFocus = new Runnable() {
         public void run() {
             if (previewing)
                 mCamera.autoFocus(autoFocusCB);
         }
     };
     
     AutoFocusCallback autoFocusCB = new AutoFocusCallback() {
         public void onAutoFocus(boolean success, Camera camera) {
             autoFocusHandler.postDelayed(doAutoFocus, 1000);
         }
     };
 	
 	/**
 	 * Method to release the camera 
 	 * */
 	private void releaseCamera(){
 		if(mCamera != null){
 			//Release the camera 
 			mCamera.release();
 			mCamera = null;
 		}
 	}
 }
