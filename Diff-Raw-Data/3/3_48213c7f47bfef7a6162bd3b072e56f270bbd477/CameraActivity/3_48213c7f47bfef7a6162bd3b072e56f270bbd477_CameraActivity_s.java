 package keendy.projects;
 
 import java.io.IOException;
 
 import android.app.Activity;
 import android.graphics.PixelFormat;
 import android.hardware.Camera;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 import android.view.View;
 import android.view.SurfaceHolder.Callback;
 import android.view.View.OnKeyListener;
 
 public class CameraActivity extends Activity implements Callback {
 
   private static final String TAG = "CAMERA ACTIVITY";
   
   //private LinearLayout mLinearLayout;
   
   private SurfaceView mSurfaceView;
   private SurfaceHolder mSurfaceHolder;
   
   private Camera mCamera;
   
   private boolean mPreviewRunning = false;
   
   @Override
   public void onCreate(Bundle savedInstanceState) {
 	super.onCreate(savedInstanceState);
 	
     getWindow().setFormat(PixelFormat.TRANSLUCENT);
     
     setContentView(R.layout.camera);
     
     mSurfaceView = (SurfaceView) findViewById(R.id.camera_surface);
     mSurfaceHolder = mSurfaceView.getHolder();
     mSurfaceHolder.addCallback(this);
     mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
     
     //mLinearLayout = (LinearLayout) findViewById(R.id.camera_layout);
     
     
     /*mLinearLayout.setOnKeyListener(new OnKeyListener() {
     
       @Override
       public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
   	    if(keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
   	      switch(keyCode) {
   	        case KeyEvent.KEYCODE_CAMERA:
   	          Log.i(TAG, "Pressed the camera button!");
   	          return true;
   	        case KeyEvent.KEYCODE_DPAD_UP:
   	          Log.i(TAG, "You pressed DPAD Up!");
   	          return true;
   	        case KeyEvent.KEYCODE_DPAD_LEFT:
   	          Log.i(TAG, "You pressed DPAD Left!");
   	          return true;
   	        case KeyEvent.KEYCODE_DPAD_RIGHT:
   	          Log.i(TAG, "You pressed DPAD Right!");
   	          return true;  	        
   	        case KeyEvent.KEYCODE_DPAD_DOWN:
   	          Log.i(TAG, "You pressed DPAD Down!");
   	          return true;
   	        case KeyEvent.KEYCODE_BACK:
   	          finish();
   	          return true;
   	        default:
   	          Log.i(TAG, "You pressed something");
   	          return true;
   	      }
   	    }
   	    if(keyEvent.getAction() == KeyEvent.ACTION_UP) {
   	      switch(keyCode) {
   	      case KeyEvent.KEYCODE_CAMERA:
   	    	Log.i(TAG, "Released the camera button!");
   	    	return true;
   	      }
   	    }
   	    return false;
       }
       
     });*/
     
     mSurfaceView.setOnKeyListener(new OnKeyListener() {
       
       @Override
       public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
   	    if(keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
   	      switch(keyCode) {
   	        case KeyEvent.KEYCODE_CAMERA:
   	          Log.i(TAG, "Pressed the camera button!");
   	          return true;
   	        case KeyEvent.KEYCODE_DPAD_UP:
   	          Log.i(TAG, "You pressed DPAD Up!");
   	          return true;
   	        case KeyEvent.KEYCODE_DPAD_LEFT:
   	          Log.i(TAG, "You pressed DPAD Left!");
   	          return true;
   	        case KeyEvent.KEYCODE_DPAD_RIGHT:
   	          Log.i(TAG, "You pressed DPAD Right!");
   	          return true;  	        
   	        case KeyEvent.KEYCODE_DPAD_DOWN:
   	          Log.i(TAG, "You pressed DPAD Down!");
   	          return true;
   	        case KeyEvent.KEYCODE_BACK:
   	          finish();
   	          return true;
   	        default:
   	          Log.i(TAG, "You pressed something");
   	          return true;
   	      }
   	    }
   	    if(keyEvent.getAction() == KeyEvent.ACTION_UP) {
   	      switch(keyCode) {
   	      case KeyEvent.KEYCODE_CAMERA:
   	    	Log.i(TAG, "Released the camera button!");
   	    	return true;
   	      }
   	    }
   	    return false;
       }
       
     });
     
     /*
     mLinearLayout.setFocusable(true);
    mLinearLayout.requestFocus();
     */
     
     mSurfaceView.setFocusable(true);
    mSurfaceView.requestFocus();
     
     Log.i(TAG, "onCreated!");
   }
 
   /** Activity is hardcoded to display in landscape, gwapo ko */
   @Override
   public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
 	
 	if(mPreviewRunning)	{
 	  mCamera.stopPreview();
 	}
 	
 	Camera.Parameters p = mCamera.getParameters();
 	p.setPreviewSize(w, h);
 	mCamera.setParameters(p);
 
 	try {
 	  mCamera.setPreviewDisplay(holder);
 	} catch (IOException e) {
 	  Log.e(TAG, "IOException lol");
 	}
 
 	mCamera.startPreview();
 	mPreviewRunning = true;
 
   }
 
   @Override
   public void surfaceCreated(SurfaceHolder holder) {
 	mCamera = Camera.open();
 	
     Log.i(TAG, "surfaceCreated!");
   }
 
   @Override
   public void surfaceDestroyed(SurfaceHolder holder) {
 	mCamera.stopPreview();
 	mPreviewRunning = false;
 	mCamera.release();
 
     Log.i(TAG, "surfaceDestroyed!");
   }
   
   Camera.ShutterCallback shutterCallback = new Camera.ShutterCallback() {
 	@Override
 	public void onShutter() {
 	  // TODO Shutter Callback
 	  Log.i(TAG, "onShutterCallback!");
 	}	
   };
   
   Camera.PictureCallback rawCallback = new Camera.PictureCallback() {
 	public void onPictureTaken(byte[] imageData, Camera c) {
 	  //TODO Handling of picture churva
 	  Log.i(TAG, "onPictureTaken! RAW");
 	}	
   };
   
   /** Picture Callback */
   Camera.PictureCallback jpegCallback = new Camera.PictureCallback() {
 	public void onPictureTaken(byte[] imageData, Camera c) {
 	  //TODO Handling of picture churva
 	  Log.i(TAG, "onPictureTaken! JPEG");
 	}	
   };
 }
