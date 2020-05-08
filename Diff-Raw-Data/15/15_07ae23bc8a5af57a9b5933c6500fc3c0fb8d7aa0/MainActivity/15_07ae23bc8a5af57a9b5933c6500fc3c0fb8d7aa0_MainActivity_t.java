 package com.socaldevs.timelapse.glass;
 
 import java.io.DataInputStream;
 import java.io.File;
 import java.io.IOException;
 import java.text.DecimalFormat;
 
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.DefaultHttpClient;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.pm.PackageManager;
 import android.hardware.Camera;
 import android.hardware.Camera.PictureCallback;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.PowerManager;
 import android.os.PowerManager.WakeLock;
 import android.provider.Settings.Secure;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 import android.widget.FrameLayout;
 
 public class MainActivity extends Activity {
 
 	private static final int DELAY = 5000; // 5 seconds
 
 	private static String TAG = "test";
 
 	private Camera mCamera;
 	private CameraPreview mPreview;
 
 	private WakeLock wakeLock = null;
 
 	private File dir = new File("/sdcard/timelapse");
 	private DecimalFormat formatter = new DecimalFormat("00000");
 	private static int picNum = 0;
 
 	private int eventId = -1;
 
 	private PictureCallback mPicture = new PictureCallback() {
 
 		@Override
 		public void onPictureTaken(byte[] data, Camera camera) {
 			if (!running)
 				return;
 			Log.i("status", "picture taken");
 			Log.i("length", "" + data.length);
 
 			// File out = new File(dir, "lapse_1_img" + formatter.format(picNum)
 			// + ".jpg");
 			// FileOutputStream fos;
 			// try {
 			// fos = new FileOutputStream(out);
 			// fos.write(data);
 			// } catch (IOException e) {
 			// e.printStackTrace();
 			// }
 
 			if (eventId == -1)
 				return;
 
 			Uploader uploader = new Uploader(MainActivity.this, eventId, picNum);
 			uploader.execute(data);
 
 			picNum++;
 		}
 	};
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 
 		handler = new Handler();
 
 		dir.mkdirs();
 
 		// Log.i("autofocus support",
 		// String.valueOf(getPackageManager().hasSystemFeature("android.hardware.camera.autofocus")));
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 		// Create an instance of Camera
 		mCamera = getCameraInstance();
 
 		// Create our Preview view and set it as the content of our activity.
 		mPreview = new CameraPreview(this);
 		FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
 		preview.addView(mPreview);
 
 		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
 		wakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK,
 				"Glass Timelapse");
 	}
 
 	@Override
 	public void onPause() {
 		super.onPause();
 		mCamera.stopPreview();
 		mCamera.unlock();
 		mCamera.release();
 		mCamera = null;
 	}
 
 	/** Check if this device has a camera */
 	private boolean checkCameraHardware(Context context) {
 		if (context.getPackageManager().hasSystemFeature(
 				PackageManager.FEATURE_CAMERA)) {
 			// this device has a camera
 			return true;
 		} else {
 			// no camera on this device
 			return false;
 		}
 	}
 
 	/** A safe way to get an instance of the Camera object. */
 	public static Camera getCameraInstance() {
 		Camera c = null;
 		try {
 			c = Camera.open(); // attempt to get a Camera instance
 		} catch (Exception e) {
 			// Camera is not available (in use or does not exist)
 			e.printStackTrace();
 		}
 		return c; // returns null if camera is unavailable
 	}
 
 	private Handler handler = null;
 	private boolean running = false;
 
 	private void start() {
 		wakeLock.acquire();
 		Log.i("status", "acquired wakelock");
 		running = true;
 
 		new AsyncTask<Void, Void, Integer>() {
 
 			@Override
 			protected Integer doInBackground(Void... params) {
 				int event = -1;
 				String id = Secure.getString(
 						MainActivity.this.getContentResolver(),
 						Secure.ANDROID_ID);
 
 				try {
 					HttpClient cli = new DefaultHttpClient();
 					HttpPost post = new HttpPost(Constants.EVENT_URL
 							+ "?mode=new&glassId=" + id);
 					Log.i("event url", Constants.EVENT_URL
							+ "?mode=new&glassId=" + id);
 					DataInputStream dis = new DataInputStream(cli.execute(post)
 							.getEntity().getContent());
 					event = dis.readInt();
 					Log.i("event id", "" + event);
 				} catch (Exception ex) {
 					ex.printStackTrace();
 				}
 
 				return event;
 			}
 
 			@Override
 			protected void onPostExecute(Integer result) {
 				MainActivity.this.eventId = result;
 
 				handler.postDelayed(new Runnable() {
 
 					public void run() {
 						// do something
 						takePicture();
 
 						if (running)
 							handler.postDelayed(this, DELAY);
 					}
 				}, 1000);
 			}
 		}.execute();
 	}
 
 	private void stop() {
 		running = false;
 
 		String id = Secure.getString(MainActivity.this.getContentResolver(),
 				Secure.ANDROID_ID);
 
 		HttpClient cli = new DefaultHttpClient();
 		HttpPost post = new HttpPost(Constants.EVENT_URL + "?mode=end&glassId="
				+ id + "&eventId=" + eventId);
 		try {
 			cli.execute(post).getEntity().consumeContent();
 		} catch (ClientProtocolException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
		try {
			wakeLock.release();
			Log.i("status", "released wakelock");
		} catch (Exception ex) {
			Log.e("wakelock", "error releasing wakelock, proceeding normally");
		}
 	}
 
 	private void takePicture() {
 		if (mCamera == null) {
 			Log.e("cam status", "camera is null");
 			return;
 		}
 		mCamera.takePicture(null, null, mPicture);
 		Log.i("status", "called takePicture");
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		// Handle item selection
 		switch (item.getItemId()) {
 		case R.id.action_start:
 			start();
 			return true;
 		case R.id.action_stop:
 			stop();
 			return true;
 		case R.id.action_take_picture:
 			takePicture();
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	/**
 	 * Handle the tap event from the touchpad.
 	 */
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 		switch (keyCode) {
 		// On Tap, we want to open the menus
 		case KeyEvent.KEYCODE_DPAD_CENTER:
 		case KeyEvent.KEYCODE_ENTER:
 			openOptionsMenu();
 			return true;
 		default:
 			return super.onKeyDown(keyCode, event);
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 
 	/** A basic Camera preview class */
 	public class CameraPreview extends SurfaceView implements
 			SurfaceHolder.Callback {
 		private SurfaceHolder mHolder;
 
 		public CameraPreview(Context context) {
 			super(context);
 
 			// Install a SurfaceHolder.Callback so we get notified when the
 			// underlying surface is created and destroyed.
 			mHolder = getHolder();
 			mHolder.addCallback(this);
 			// deprecated setting, but required on Android versions prior to 3.0
 			mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
 		}
 
 		public void surfaceCreated(SurfaceHolder holder) {
 			// The Surface has been created, now tell the camera where to draw
 			// the preview.
 			try {
 				mCamera.setPreviewDisplay(holder);
 				mCamera.startPreview();
 			} catch (IOException e) {
 				Log.d(TAG, "Error setting camera preview: " + e.getMessage());
 			}
 		}
 
 		public void surfaceDestroyed(SurfaceHolder holder) {
 			// empty. Take care of releasing the Camera preview in your
 			// activity.
 		}
 
 		public void surfaceChanged(SurfaceHolder holder, int format, int w,
 				int h) {
 			// If your preview can change or rotate, take care of those events
 			// here.
 			// Make sure to stop the preview before resizing or reformatting it.
 
 			if (mHolder.getSurface() == null) {
 				// preview surface does not exist
 				return;
 			}
 
 			// stop preview before making changes
 			try {
 				mCamera.stopPreview();
 			} catch (Exception e) {
 				// ignore: tried to stop a non-existent preview
 			}
 
 			// set preview size and make any resize, rotate or
 			// reformatting changes here
 
 			// start preview with new settings
 			try {
 				mCamera.setPreviewDisplay(mHolder);
 				mCamera.startPreview();
 
 			} catch (Exception e) {
 				Log.d(TAG, "Error starting camera preview: " + e.getMessage());
 			}
 		}
 	}
 
 }
