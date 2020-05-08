 package com.runninghusky.spacetracker;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.PendingIntent;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.pm.ActivityInfo;
 import android.graphics.PixelFormat;
 import android.hardware.Camera;
 import android.hardware.Camera.Size;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.os.Environment;
 import android.os.Handler;
 import android.os.Message;
 import android.provider.Settings;
 import android.telephony.SmsManager;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 import android.view.View;
 import android.view.Window;
 import android.view.WindowManager;
 
 public class CameraLoggerActivity extends Activity implements
 		SurfaceHolder.Callback, Runnable {
 	private Camera mCamera;
 	private SurfaceView mSurfaceView;
 	private SurfaceHolder mSurfaceHolder;
 	boolean mPreviewRunning = false;
 	static final int FOTO_MODE = 0;
 	private Context ctx = this;
 	private DataHelper dh;
 	public Flight f = new Flight();
 	public long flightId, smsInterval, picInterval, smsLastSent;
 	public LocationManager fullLocManager;
 	public LocationListener fullLocListener;
 	public FlightData fd = new FlightData();
 	public Integer i = 0;
 	public float distance, brightness;
 	int defTimeOut = 0;
 	private static final int DELAY = 999999999;
 	private static String TAG = "spacetrack";
 	protected static final int REFRESH = 0;
 	boolean firstRun = true;
 	boolean shouldTakePics = true;
 	public Location oldLoc;
 
 	// private ProgressDialog pd;
 
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
 			if (brightness != 0) {
 				WindowManager.LayoutParams layout = getWindow().getAttributes();
 				layout.screenBrightness = brightness;
 				getWindow().setAttributes(layout);
 			}
 			quitLogger();
 		}
 		return super.onKeyDown(keyCode, event);
 	}
 
 	@Override
 	public void onAttachedToWindow() {
 		// disables the home button so logging will continue
 		this.getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD);
 		super.onAttachedToWindow();
 	}
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
 		getWindow().setFormat(PixelFormat.TRANSLUCENT);
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		brightness = 0;
 
 		setContentView(R.layout.cameralogger);
 		defTimeOut = Settings.System.getInt(getContentResolver(),
 				Settings.System.SCREEN_OFF_TIMEOUT, DELAY);
 		Settings.System.putInt(getContentResolver(),
 				Settings.System.SCREEN_OFF_TIMEOUT, DELAY);
 
 		setupStart();
 	}
 
 	@Override
 	protected void onDestroy() {
 		super.onDestroy();
 		Settings.System.putInt(getContentResolver(),
 				Settings.System.SCREEN_OFF_TIMEOUT, defTimeOut);
 
 		if (fullLocManager != null) {
 			fullLocManager.removeUpdates(fullLocListener);
 			fullLocManager = null;
 		}
 
 	}
 
 	public void setupStart() {
 		mSurfaceView = (SurfaceView) findViewById(R.id.SurfaceViewPicture);
 		mSurfaceHolder = mSurfaceView.getHolder();
 		mSurfaceHolder.addCallback(this);
 		mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
 
 		mSurfaceView.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				if (firstRun) {
 					doStuff();
 					HlprUtil.toast("Logging has started...", ctx, true);
 					firstRun = false;
 					WindowManager.LayoutParams layout = getWindow()
 							.getAttributes();
 					brightness = layout.screenBrightness;
 					layout.screenBrightness = .1F;
 					getWindow().setAttributes(layout);
 
 				}
 			}
 		});
 
 		Bundle b = getIntent().getExtras();
 		flightId = b.getLong("flightId", 0);
 
 		this.dh = new DataHelper(this);
 		f = this.dh.selectFlightHistoryById(flightId);
 		this.dh.close();
 
 		smsInterval = Long.valueOf(f.getSmsDuration()) * 1000;
 		picInterval = Long.valueOf(f.getPicDuration()) * 1000;
 
 		fullLocManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
 		fullLocListener = new FullLocationListener();
 		fullLocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,
 				0, fullLocListener);
 
 		HlprUtil.toast("Tap the screen to start logging...", ctx, true);
 	}
 
 	private void doStuff() {
 		Thread thread = new Thread(this);
 		thread.start();
 	}
 
 	private void takePicture() {
 		System.out.println("takePicture()");
 		try {
 			mCamera.autoFocus(onFocus);
 		} catch (RuntimeException e) {
 			e.printStackTrace();
 			mCamera.takePicture(null, null, jpegCallback);
 		}
 	}
 
 	Camera.AutoFocusCallback onFocus = new Camera.AutoFocusCallback() {
 		public void onAutoFocus(boolean success, Camera camera) {
 			camera.takePicture(null, null, jpegCallback);
 		}
 	};
 
 	public void surfaceCreated(SurfaceHolder holder) {
 		Log.e("surafaceCreated", "surfaceCreated");
 		mCamera = Camera.open();
 
 	}
 
 	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
 		Log.e("surfaceChanged", "surfaceChanged");
 
 		if (mPreviewRunning) {
 			mCamera.stopPreview();
 		}
 
 		Camera.Parameters p = mCamera.getParameters();
 		List<Size> sizes = p.getSupportedPictureSizes();
 		Size s = sizes.get(0);
 		for (Size size : sizes) {
			if (size.width < s.width) {
 				s = size;
 			}
 		}
 		p.setPictureSize(s.width, s.height);
 
 		List<Size> previewSizes = p.getSupportedPreviewSizes();
 		p.setPreviewSize(previewSizes.get(0).width, previewSizes.get(0).height);
 
 		List<String> focusModes = p.getSupportedFocusModes();
 		for (String str : focusModes) {
 			if (str.equalsIgnoreCase("infinity")) {
 				p.setFocusMode(str);
 			}
 		}
 
 		try {
 			mCamera.setParameters(p);
 		} catch (Exception e) {
 			HlprUtil
 					.toast(
 							"Ahh SNAP! Sense sucks so your camera didn't set the parameters...  should still work though  "
 									+ String.valueOf(e), ctx, false);
 		}
 		try {
 			mCamera.setPreviewDisplay(holder);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		mCamera.startPreview();
 		mPreviewRunning = true;
 	}
 
 	public void surfaceDestroyed(SurfaceHolder holder) {
 		Log.e("surfaceDestroyed", "surfaceDestroyed");
 		mCamera.stopPreview();
 		mPreviewRunning = false;
 		mCamera.release();
 	}
 
 	/** Handles data for jpeg picture */
 	Camera.PictureCallback jpegCallback = new Camera.PictureCallback() {
 		public void onPictureTaken(byte[] data, Camera camera) { // getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
 			// WindowManager.LayoutParams.FLAG_FULLSCREEN);
 
 			FileOutputStream outStream = null;
 			Intent mIntent = new Intent();
 			if (data != null) {
 				try {
 					File root = Environment.getExternalStorageDirectory();
 					File f = new File(root + "/DCIM/SpaceTracker/");
 					f.mkdirs();
 					if (root.canWrite()) {
 						outStream = new FileOutputStream(root
 								+ "/DCIM/SpaceTracker/"
 								+ System.currentTimeMillis() + ".jpg");
 						outStream.write(data);
 						outStream.close();
 						Log.d(TAG, "onPictureTaken - wrote bytes: "
 								+ data.length);
 						HlprUtil.toast("picture saved...", ctx, true);
 					}
 				} catch (FileNotFoundException e) {
 					e.printStackTrace();
 				} catch (IOException e) {
 					e.printStackTrace();
 				} finally {
 					mCamera.startPreview();
 					setResult(FOTO_MODE, mIntent);
 				}
 			}
 			Log.d(TAG, "onPictureTaken - jpeg");
 		}
 	};
 
 	protected void onResume() {
 		Log.e("resume", "onResume");
 		super.onResume();
 	}
 
 	public void sendSMS(String pn, String m) {
 		final String phoneNumber = pn;
 		final String message = m;
 		String SENT = "SMS_SENT";
 
 		PendingIntent sentPI = PendingIntent.getBroadcast(ctx, 0, new Intent(
 				SENT), 0);
 
 		// ---when the SMS has been sent---
 		registerReceiver(new BroadcastReceiver() {
 			@Override
 			public void onReceive(Context arg0, Intent arg1) {
 				String msg = "";
 				switch (getResultCode()) {
 				case Activity.RESULT_OK:
 					msg = "SMS sent";
 					break;
 				case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
 					msg = "Generic failure";
 					break;
 				case SmsManager.RESULT_ERROR_NO_SERVICE:
 					msg = "No service";
 					break;
 				case SmsManager.RESULT_ERROR_NULL_PDU:
 					msg = "Null PDU";
 					break;
 				case SmsManager.RESULT_ERROR_RADIO_OFF:
 					msg = "Radio off";
 					break;
 				}
 				HlprUtil.toast(msg, ctx, true);
 				Calendar c = Calendar.getInstance();
 				dh = new DataHelper(ctx);
 				dh.insertSms(flightId, phoneNumber, message, c
 						.getTimeInMillis(), msg);
 				dh.close();
 
 			}
 		}, new IntentFilter(SENT));
 
 		SmsManager sms = SmsManager.getDefault();
 		sms.sendTextMessage(phoneNumber, "", message, sentPI, null);
 
 	}
 
 	public class FullLocationListener implements LocationListener {
 		@Override
 		public void onLocationChanged(Location loc) {
 			try {
 				if (i == 0) {
 					smsLastSent = System.currentTimeMillis();
 					oldLoc = loc;
 					dh = new DataHelper(ctx);
 					distance = dh.getLastDistance(flightId);
 					dh.close();
 					i++;
 				}
 
 				distance += loc.distanceTo(oldLoc);
 
 				if (i == 0) {
 					smsLastSent = System.currentTimeMillis();
 					i++;
 				}
 				dh = new DataHelper(ctx);
 				dh.insertDetails(flightId, (float) loc.getLongitude(),
 						(float) loc.getLatitude(), (float) loc.getAltitude(),
 						loc.getSpeed(), loc.getTime(), loc.getAccuracy(), loc
 								.getBearing(), loc.getProvider(), distance);
 				dh.close();
 
 			} catch (Exception e) {
 				HlprUtil.toast("Error saving data... " + String.valueOf(e),
 						ctx, true);
 			}
 
 			oldLoc = loc;
 			if (f.getSendSms()
 					&& (smsInterval < (System.currentTimeMillis() - smsLastSent))) {
 				try {
 
 					smsLastSent = System.currentTimeMillis();
 
 					Date date = new Date();
 					SimpleDateFormat sdf = new SimpleDateFormat(
 							"HH:mm:ss MM/dd/yyyy");
 					String strCal = sdf.format(date);
 
 					sendSMS(
 							f.getSmsNumber(true),
 							"http://maps.google.com/maps?q="
 									+ loc.getLatitude()
 									+ ","
 									+ loc.getLongitude()
 									+ "  Altitude: "
 									+ String
 											.valueOf(HlprUtil
 													.roundTwoDecimals(loc
 															.getAltitude() * 3.2808399))
 									+ " feet, Traveled: "
 									+ String
 											.valueOf(HlprUtil
 													.roundTwoDecimals(distance * 0.000621371192))
 									+ " miles, Current Speed: "
 									+ String
 											.valueOf(HlprUtil
 													.roundTwoDecimals(loc
 															.getSpeed() * 2.23693629))
 									+ " at " + strCal);
 
 				} catch (Exception e) {
 					HlprUtil.toast("SMS error... " + String.valueOf(e), ctx,
 							true);
 				}
 			}
 		}
 
 		@Override
 		public void onProviderDisabled(String provider) {
 		}
 
 		@Override
 		public void onProviderEnabled(String provider) {
 		}
 
 		@Override
 		public void onStatusChanged(String provider, int status, Bundle extras) {
 		}
 	}
 
 	public void goBackIntent() {
 		Intent myIntent = new Intent(CameraLoggerActivity.this,
 				SpaceTrackerActivity.class);
 		CameraLoggerActivity.this.startActivity(myIntent);
 		finish();
 	}
 
 	private void quitLogger() {
 		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int which) {
 				switch (which) {
 				case DialogInterface.BUTTON_POSITIVE:
 					handler.sendEmptyMessage(0);
 					break;
 				case DialogInterface.BUTTON_NEGATIVE:
 					if (brightness != 0) {
 						WindowManager.LayoutParams layout = getWindow()
 								.getAttributes();
 						layout.screenBrightness = .1F;
 						getWindow().setAttributes(layout);
 					}
 					break;
 				}
 			}
 		};
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setMessage("Are you sure you want to stop logging?")
 				.setPositiveButton("Of Course...", dialogClickListener)
 				.setNegativeButton("No Way!", dialogClickListener).show();
 
 	}
 
 	@Override
 	public void run() {
 		while (shouldTakePics) {
 			try {
 				Thread.sleep(picInterval);
 				if (shouldTakePics) {
 					try {
 						takePicture();
 					} catch (Exception e) {
 						HlprUtil.toast("Error taking picture... "
 								+ String.valueOf(e), ctx, true);
 					}
 				} else {
 					return;
 				}
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	private Handler handler = new Handler() {
 		@Override
 		public void handleMessage(Message msg) {
 			shouldTakePics = false;
 			Intent myIntent = new Intent(CameraLoggerActivity.this,
 					SpaceTrackerActivity.class);
 			CameraLoggerActivity.this.startActivity(myIntent);
 			finish();
 		}
 	};
 }
