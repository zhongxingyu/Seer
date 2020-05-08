 package edu.vu.augmented.reality;
 
 import java.util.List;
 import java.util.Timer;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.pm.PackageManager;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.ImageFormat;
 import android.graphics.Typeface;
 import android.hardware.Camera;
 import android.hardware.Camera.PictureCallback;
 import android.os.Bundle;
 import android.os.Environment;
 import android.util.Log;
 import android.view.Gravity;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.FrameLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.googlecode.tesseract.android.TessBaseAPI;
 
 public class CameraActivity extends Activity {
 
     private static String LOGTAG = "augmented-reality";
     private Camera mCamera;
     private CameraPreview mPreview;
 
     private Timer cameraTimer;
 
     private TessBaseAPI tess;
     private CardParser parser;
     private DatabaseHandler databaseHandler;
 
     private Button img_cap;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
     }
 
     @Override
     public void onPause() {
         super.onPause();
 
         tess.end();
         
         if (cameraTimer != null) {
             cameraTimer.cancel();
             cameraTimer.purge();
         }
 
         if (mCamera != null) {
             mCamera.release();
         }
     }
     
    public void takePicture(View v){
         mCamera.autoFocus(new Camera.AutoFocusCallback() {
             
             @Override
             public void onAutoFocus(boolean success, Camera camera) {
                 mCamera.takePicture(null, null, mPicture);    
             }
         });
         mCamera.startPreview();
     }
 
     @Override
     public void onResume() {
         super.onResume();
         if (checkCameraHardware(this)) {
             setContentView(R.layout.activity_camera);
            
             // Create an instance of Camera
             mCamera = getCameraInstance();
             if (mCamera == null) {
                 Toast.makeText(getApplicationContext(), "Camera Unavailable",
                         Toast.LENGTH_SHORT).show();
                 showErrorView();
                 return;
             }
             
             //set camera parameters
             setCameraParams();
 
             // Create our Preview view and set it as the content of our
             // activity.
             mPreview = new CameraPreview(this, mCamera);
             FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
             preview.addView(mPreview, 0);
 
             // Make a Tesseract object and parser
             tess = new TessBaseAPI();
             if (!tess.init(getExternalFilesDir(Environment.MEDIA_MOUNTED).getAbsolutePath(), "eng")) {
                 Log.d(LOGTAG, "Tesseract not initialized successfully");
             }
             else {
                 Log.d(LOGTAG, "Tesseract initialized");
             }
             parser = new CardParser();
             
             // Now make connection to database
             databaseHandler = new DatabaseHandler(this);
 
         } else {
             showErrorView();
         }
     }
     
     private void setCameraParams() {
         
         Camera.Parameters cp = mCamera.getParameters();
         
         List<Camera.Size> sizes = cp.getSupportedPreviewSizes();
         int viewWidth = sizes.get(sizes.size() - 1).width;
         int viewHeight = sizes.get(sizes.size() - 1).height;
         cp.setPreviewSize(viewWidth, viewHeight);
         cp.setPictureSize(viewWidth, viewHeight);
         
         cp.setFocusMode(Camera.Parameters.FOCUS_MODE_MACRO);
         cp.setPictureFormat(ImageFormat.JPEG);
         cp.setJpegQuality(100);
         mCamera.setParameters(cp);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_main, menu);
         return true;
     }
 
     private boolean checkCameraHardware(Context context) {
         if (context.getPackageManager().hasSystemFeature(
                 PackageManager.FEATURE_CAMERA)) {
             return true;
         } else {
             return false;
         }
     }
 
     private void showErrorView() {
         TextView errorView;
         errorView = new TextView(getApplicationContext());
         errorView.setBackgroundResource(android.R.drawable.dark_header);
         errorView.setGravity(Gravity.CENTER_VERTICAL);
         errorView.setFocusable(false);
         errorView.setClickable(false);
         errorView.setLongClickable(false);
         errorView.setOnClickListener(new View.OnClickListener() {
 
             @Override
             public void onClick(View v) {
             }
         });
         errorView.setTextSize((float) 14.0);
         errorView.setTypeface(Typeface.DEFAULT_BOLD);
         errorView
                 .setText("Sorry, your device either does not have a camera or its camera is not compatible with this application.");
         setContentView(errorView);
     }
 
     /** A safe way to get an instance of the Camera object. */
     public static Camera getCameraInstance() {
         Camera c = null;
         try {
             c = Camera.open(); // attempt to get a Camera instance
         } catch (Exception e) {
             // Camera is not available (in use or does not exist)
         }
         return c; // returns null if camera is unavailable
     }
 
     private PictureCallback mPicture = new PictureCallback() {
 
         @Override
         public void onPictureTaken(byte[] data, Camera camera) {
 
             long startTime = System.currentTimeMillis();
             
             BitmapFactory.Options options = new BitmapFactory.Options();
             options.inPreferredConfig = Bitmap.Config.ARGB_8888;
             options.inSampleSize = 2;
 
             Bitmap myBitmap = BitmapFactory.decodeByteArray(data, 0, data.length, options);
 
             tess.clear();
             tess.setImage(myBitmap);
             String textOnCard = tess.getUTF8Text();
             parser.setText(textOnCard);
             String textEmail = parser.getEmail();
             String textPhone = parser.getPhone();
             String textWeb = parser.getURL();
             String textName = parser.getPersonName();
             
             Toast.makeText(getApplicationContext(), "Email: " + textEmail +
              "\nPhone: " + textPhone + "\nWeb: " + textWeb + "\nName: " + textName,
              Toast.LENGTH_SHORT).show();
             Log.d(LOGTAG, textOnCard);
             Log.d(LOGTAG, "Email: " + textEmail + "\nPhone: " + textPhone
                     + "\nWeb: " + textWeb);
             
             databaseHandler.addContact(new Contact(textName, textPhone, textEmail, textWeb));
             
             // Tracking execution time
             long endTime = System.currentTimeMillis();
             Toast timeDisplay = Toast.makeText(getApplicationContext(), "Execution time(ms): " + Long.toString(endTime - startTime), Toast.LENGTH_SHORT);
             timeDisplay.setGravity(Gravity.BOTTOM, 0, 0);
             timeDisplay.show();
             
             mCamera.startPreview(); //Don't delete this line!!! If you do, the preview doesn't restart and the user will have to exit the activity
         }
 
     };
 }
