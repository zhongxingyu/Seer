 package fitnessapps.scavenger.components;
 
 import java.util.Random;
 
import fitnessapps.scavenger.activity.R;
import fitnessapps.scavenger.data.GlobalState;
import fitnessapps.scavenger.data.Histogram;

 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.Context;
import android.content.DialogInterface;
 import android.hardware.Camera;
 import android.os.Bundle;
 import android.util.Log;
import android.view.LayoutInflater;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 import android.view.View;
 import android.view.View.OnClickListener;
import android.view.ViewGroup;
 import android.view.Window;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 public abstract class Level extends Activity {
 
 	private AlertDialog.Builder builder = null;
 	private AlertDialog alertDialog = null;
 	private MyTimer countdownTimer = null;
 	private TextView timerView = null;
 	private Histogram histogram = null;
 	private SurfaceView preview = null;
 	private SurfaceHolder previewHolder = null;
 	private Camera mCamera = null;
 	private boolean inPreview = false;
 	private boolean cameraConfigured = false;
 	private int taskNumber = 1;
 	private int colorImgDrawable;
 
 	private static final long COUNTDOWN_INTERVAL = 1000;
 	private static final int RED = 1;
 	private static final int BLUE = 2;
 	private static final int GREEN = 3;
 	private static final int PURPLE = 4;
 	private static final int PINK = 5;
 	private static final int ORANGE = 6;
 	private static final int GREY = 7;
 	private static final int BROWN = 8;
 
 	/**
 	 * Method to start the level and register necessary listeners.
 	 */
 	public void levelStart() {
 		
 		alertTask(taskNumber);
 		//countdownTimer.start();
 	}
 
 	/**************** CAMERA METHODS ***********************************/
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		// Hide the window title.
         requestWindowFeature(Window.FEATURE_NO_TITLE);
 		setContentView(R.layout.camerapreview);
 		Button button = (Button) findViewById(R.id.buttonCustomDialog);
 		
 		button.setOnClickListener(new OnClickListener() {
 			  public void onClick(View arg0) {
 				  alertTask(1);
 			  }
 			});
 		preview = (SurfaceView) findViewById(R.id.preview);
 		previewHolder = preview.getHolder();
 		previewHolder.addCallback(surfaceCallback);
 		previewHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 		mCamera = Camera.open();
		mCamera.startPreview();
 	} 
 
 	@Override
 	public void onPause() {
 		if (inPreview) {
 			mCamera.stopPreview();
 		}
 
 		mCamera.release();
 		mCamera = null;
 		inPreview = false;
 
 		super.onPause();
 
 	}
 
 	private Camera.Size getBestPreviewSize(int width, int height,
 			Camera.Parameters parameters) {
 		Camera.Size result = null;
 
 		for (Camera.Size size : parameters.getSupportedPreviewSizes()) {
 			if (size.width <= width && size.height <= height) {
 				if (result == null) {
 					result = size;
 				} else {
 					int resultArea = result.width * result.height;
 					int newArea = size.width * size.height;
 
 					if (newArea > resultArea) {
 						result = size;
 					}
 				}
 			}
 		}
 
 		return (result);
 	}
 
 	private void initPreview(int width, int height) {
 		if (mCamera != null && previewHolder.getSurface() != null) {
 			try {
 				mCamera.setPreviewDisplay(previewHolder);
 			} catch (Throwable t) {
 				Log.e("PreviewDemo-surfaceCallback",
 						"Exception in setPreviewDisplay()", t);
 			}
 
 			if (!cameraConfigured) {
 				Camera.Parameters parameters = mCamera.getParameters();
 				Camera.Size size = getBestPreviewSize(width, height, parameters);
 
 				if (size != null) {
 					parameters.setPreviewSize(size.width, size.height);
 					mCamera.setParameters(parameters);
 					cameraConfigured = true;
 				}
 			}
 		}
 	} 
 
 	private void startPreview() {
 		if (cameraConfigured && mCamera != null) {
 			mCamera.startPreview();
 			inPreview = true;
 		}
 	} 
 
 	SurfaceHolder.Callback surfaceCallback = new SurfaceHolder.Callback() {
 		 public void surfaceCreated(SurfaceHolder holder) {
 		        // The Surface has been created, acquire the camera and tell it where
 		        // to draw.
 		    //    mCamera = Camera.open();
 		 //       try {
 		//           mCamera.setPreviewDisplay(holder);
 		//        } catch (IOException exception) {
 		//            mCamera.release();
 		//            mCamera = null;
 		            // TODO: add more exception handling logic here
 		     //   }
 		    }
 
 		    public void surfaceDestroyed(SurfaceHolder holder) {
 		        // Surface will be destroyed when we return, so stop the preview.
 		        // Because the CameraDevice object is not a shared resource, it's very
 		        // important to release it when the activity is paused.
 		    //    mCamera.stopPreview();
 		     //   mCamera.release();
 		    //    mCamera = null;
 		    }
 
 		    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
 		        // Now that the size is known, set up the camera parameters and begin
 		        // the preview.
 		       // Camera.Parameters parameters = mCamera.getParameters();
 		       // parameters.setPreviewSize(w, h);
 		        //mCamera.setParameters(parameters);
 		    	initPreview(w, h);
 		        //mCamera.startPreview();
 		    	startPreview();
 		    }
 	};
 
 	/************* END CAMERA METHODS *********************************/
 
 	public void setViews(TextView timerTextView) {
 		timerView = timerTextView;
 	}
 
 	public void initGameTimer(long numberOfMiliSec) {
 		countdownTimer = new MyTimer(numberOfMiliSec, COUNTDOWN_INTERVAL,
 				timerView);
 	}
 
 	public void alertTask(int taskNumber) {
 		String colorText = getNewColor();
 		Context mContext = this;
 		//LayoutInflater inflater = (LayoutInflater) mContext
 			//	.getSystemService(LAYOUT_INFLATER_SERVICE);
 		//View layout = inflater.inflate(R.layout.custom_dialog,
 				//(ViewGroup) findViewById(R.id.layout_root));
 
 		final Dialog dialog = new Dialog(mContext);
 		dialog.setContentView(R.layout.custom_dialog);
 		TextView text = (TextView) dialog.findViewById(R.id.text);
 		text.setText("Take a picture of something that is " + colorText);
 		ImageView image = (ImageView) dialog.findViewById(R.id.imageview);
 
 		image.setImageResource(colorImgDrawable);
 		
 		Button button = (Button) dialog.findViewById(R.id.button);
 		button.setOnClickListener(new OnClickListener() {
           
                 public void onClick(View v) {
                     dialog.dismiss();
                 }
             });
 		dialog.show();
 		/*
 		builder = new AlertDialog.Builder(mContext);
 		builder.setView(layout);
 		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialogInterface, int arg1) {
 				dialogInterface.cancel();
 			}
 		});
 		alertDialog = builder.create();
 		alertDialog.setTitle("Task " + taskNumber);
 		alertDialog.show(); */
 		
 	}
 	
 	public String getNewColor() {
 		String newColor = "";
 		int nColor = randomizeColorSelection();
 		switch (nColor) {
 			case RED:
 				newColor = "Red";
 				colorImgDrawable = R.drawable.red;
 				break;
 			case BLUE:
 				newColor = "Blue";
 				colorImgDrawable = R.drawable.red;
 				break;
 			case GREEN:
 				newColor = "Green";
 				colorImgDrawable = R.drawable.red;
 				break;
 			case PURPLE:
 				newColor = "Purple";
 				colorImgDrawable = R.drawable.red;
 				break;
 			case PINK:
 				newColor = "Pink";
 				colorImgDrawable = R.drawable.red;
 				break;
 			case GREY:
 				newColor = "Grey";
 				colorImgDrawable = R.drawable.red;
 				break;
 			case BROWN:
 				newColor = "Brown";
 				colorImgDrawable = R.drawable.red;
 				break;
 			case ORANGE:
 				newColor = "Orange";
 				colorImgDrawable = R.drawable.red;
 				break;
 		}
 		return newColor;
 	}
 	public int randomizeColorSelection() {
 		Random gen = new Random();
 		int pickedNumber = gen.nextInt() + 8;
 		return pickedNumber;
 	}
 
 	/****************** LEVELS **********************************/
 	public int getLevelNumber() {
 		return GlobalState.level_number;
 	}
 
 	public void setLevelNumber(int levelNum) {
 		GlobalState.level_number = levelNum;
 	}
 
 }
