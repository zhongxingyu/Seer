 package com.github.barcodescanner.camera;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.github.barcodescanner.R;
 import com.github.barcodescanner.activities.AddNewActivity;
 import com.github.barcodescanner.activities.BarcodeViewActivity;
 import com.github.barcodescanner.activities.ProductActivity;
 import com.github.barcodescanner.core.BCGenerator;
 import com.github.barcodescanner.core.BCLocator;
 import com.github.barcodescanner.core.DatabaseHelper;
 import com.github.barcodescanner.core.DatabaseHelperFactory;
 import com.github.barcodescanner.core.Product;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.hardware.Camera;
 import android.hardware.Camera.AutoFocusCallback;
 import android.hardware.Camera.PictureCallback;
 import android.os.Bundle;
 import android.os.Handler;
 import android.util.Log;
 import android.view.View;
 import android.view.Window;
 import android.view.WindowManager;
 import android.widget.FrameLayout;
 import android.widget.ImageView;
 
 public class CameraActivity extends Activity {
 	private static final String TAG = "CameraActivity";
 
 	private Preview mPreview;
 	private boolean mPreviewRunning;
 	private Camera mCamera;
 	private int numberOfCameras;
 	private boolean isOwner;
 
 	private Handler autoFocusHandler;
 
 	private DatabaseHelper database;
 
 	private BCLocator bcLocator;
 	private BCGenerator bcGenerator;
 
 	// Load zbar library
 	static {
 		// System.loadLibrary("iconv");
 	}
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		// Hide the window title.
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
 		setContentView(R.layout.activity_camera);
 		isOwner = getIntent().getExtras().getBoolean("isOwner");
 
 		// Set layout
 
 		// Create a RelativeLayout container that will hold a SurfaceView,
 		// and set it as the content of our activity.
 		// setupPreview();
 
 		// Configure the ZBar scanner
 		// setupScanner();
 
 		// Setup autofocus handler
 		// setupAutoFocus();
 
 		// Setup the database
 		setupDatabase();
 
 		// barcode analyzer
 		bcLocator = new BCLocator();
 
 		bcGenerator = new BCGenerator();
 
 	}
 
 	private void setupPreview() {
 		// setContentView(R.layout.activity_camera);
 		// mPreview = new Preview(this);
 
 		// setContentView(mPreview);
 
 		// setContentView(draw);
 		// mPreviewRunning = true;
 		// System.out.println(previewCallback);
 		// mPreview.setPreviewCallback(previewCallback);
 		// System.out.println(previewCallback);
 	}
 
 	/*
 	 * private void setupAutoFocus() { autoFocusHandler = new Handler();
 	 * mPreview.setAutoFocusCallback(autoFocusCallback); }
 	 */
 
 	private void setupDatabase() {
 		DatabaseHelperFactory.init(this);
 		database = DatabaseHelperFactory.getInstance();
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 
 		// Open the default i.e. the first rear facing camera.
 		mCamera = getCameraInstance();
 		System.err.println("mCamera is: " + mCamera);
 		mPreview = new Preview(this, mCamera);
 		mPreviewRunning = true;
 		autoFocusHandler = new Handler();
 		mPreview.setAutoFocusCallback(autoFocusCallback);
 		// gets the FrameLayout camera_preview in activity_camera.xml
 		FrameLayout frameLayout = (FrameLayout) findViewById(R.id.camera_preview);
 		// adds the mPreview view to that FrameLayout
 		frameLayout.addView(mPreview);
 	}
 
 	private static Camera getCameraInstance() {
 		Camera camera = null;
 
 		try {
 			camera = Camera.open();
 		} catch (Exception e) {
 			Log.e(TAG, "Exception when opening the camera", e);
 		}
 		return camera;
 	}
 
 	@Override
 	protected void onPause() {
 		// Because the Camera object is a shared resource, it's very
 		// important to release it when the activity is paused.
 		releaseCamera();
 
 		FrameLayout frameLayout = (FrameLayout) findViewById(R.id.camera_preview);
 		// adds the mPreview view to that FrameLayout
 		frameLayout.removeView(mPreview);
 
 		super.onPause();
 	}
 
 	private void releaseCamera() {
 		if (mCamera != null) {
 			mPreviewRunning = false;
 			mCamera.stopPreview();
 			mCamera.setPreviewCallback(null);
 			// mPreview.setCamera(null);
 			mCamera.release();
 			mCamera = null;
 		}
 	}
 
 	private PictureCallback pictureCallback = new PictureCallback() {
 		public void onPictureTaken(byte[] data, Camera camera) {
 			String tempBarcode = null;
			List<List<Integer>> tempList = new ArrayList<List<Integer>>();
 
 			/*
 			 * s Size previewSize = camera.getParameters().getPreviewSize();
 			 * YuvImage yuvimage=new YuvImage(data, ImageFormat.NV21,
 			 * previewSize.width, previewSize.height, null);
 			 * ByteArrayOutputStream baos = new ByteArrayOutputStream();
 			 * yuvimage.compressToJpeg(new Rect(0, 0, previewSize.width,
 			 * previewSize.height), 80, baos); byte[] jdata =
 			 * baos.toByteArray();
 			 */
 			bcLocator.setData(data);
 			boolean foundBarcode = bcLocator.foundBarcode();
 			
 			if (foundBarcode) {
 				tempList = bcGenerator.generate(bcLocator.getSegment());
 				tempBarcode = bcGenerator.normalize(tempList);
 				
 				System.err.println("bcGenerator: " + tempBarcode);
 
 				List<Product> products = database.getProducts();
 				Product foundProduct = null;
 				if (products != null) {
 					for (Product p : products) {
 						boolean isSame = bcGenerator.compare(p.getBarcode(),
 								tempBarcode, 10);
 						if (isSame) {
 							foundProduct = p;
 							break;
 						}
 					}
 				}
 				//startBarcodeViewActivity();
 				checkBarcode(foundProduct, tempBarcode);
 			}
 
 			/*
 			 * Integer[] line = bcAnalyzer.getMostPlausible();
 			 * imageV.setImageBitmap(bcAnalyzer.getBitmap());
 			 * setContentView(imageV);
 			 */
 			/*
 			 * if(line[0] != null){
 			 * 
 			 * draw.setLineArray(line); setContentView(draw); }
 			 */
 			/*
 			 * Camera.Parameters parameters = camera.getParameters(); Size size
 			 * = parameters.getPreviewSize();
 			 * 
 			 * Image barcode = new Image(size.width, size.height, "Y800");
 			 * barcode.setData(data);
 			 * 
 			 * int result = scanner.scanImage(barcode);
 			 * 
 			 * if (result != 0) { SymbolSet syms = scanner.getResults(); for
 			 * (Symbol sym : syms) { barcodeData = sym.getData();
 			 * checkBarcode(); } }
 			 */
 		}
 	};
 
 	public void startBarcodeViewActivity() {
 		Intent intent = new Intent(this, BarcodeViewActivity.class);
 		intent.putExtra("barcodeBitmap", bcLocator.getSegmentedBitmap());
 		startActivity(intent);
 	}
 
 	public void takePicture(View view) {
 		System.err.println("mCamera is: " + mCamera);
 		//mCamera.autoFocus(autoFocusCallback);
 		mCamera.takePicture(null, null, pictureCallback);
 	}
 
 	private void checkBarcode(Product matchingProduct, String barcode) {
 		// Initialize the bundle that we will send to the productActivity
 		Bundle productBundle = new Bundle();
 		Intent productIntent;
 
 		// Check if the database contained a matching product
 		if (matchingProduct != null) {
 			// Get product name
 			String productName = matchingProduct.getName();
 
 			// Get product price
 			int productPrice = matchingProduct.getPrice();
 
 			// Put product name in bundle
 			productBundle.putString("productName", productName);
 
 			// Put product price in bundle
 			productBundle.putInt("productPrice", productPrice);
 
 			// Set ProductActivity as the intent
 			productIntent = new Intent(this, ProductActivity.class);
 		} else if(isOwner) {
 			// Put new product ID in bundle
 			productBundle.putString("productID", barcode);
 
 			// Set AddNewActivity as the intent
 			productIntent = new Intent(this, AddNewActivity.class);
 		}else{
 			// Put new product ID in bundle
 			productBundle.putString("productID", barcode);
 			
 			// Set NoProductActivity as the intent
 			productIntent = new Intent(this, AddNewActivity.class);
 		}
 
 		// Add the bundle to the intent, and start the requested activity
 		productIntent.putExtras(productBundle);
 		startActivity(productIntent);
 	}
 
 	private Runnable doAutoFocus = new Runnable() {
 		public void run() {
 			if (mPreviewRunning)
 				mCamera.autoFocus(autoFocusCallback);
 		}
 	};
 
 	private AutoFocusCallback autoFocusCallback = new AutoFocusCallback() {
 		public void onAutoFocus(boolean success, Camera camera) {
 			autoFocusHandler.postDelayed(doAutoFocus, 5000);
 		}
 	};
 }
