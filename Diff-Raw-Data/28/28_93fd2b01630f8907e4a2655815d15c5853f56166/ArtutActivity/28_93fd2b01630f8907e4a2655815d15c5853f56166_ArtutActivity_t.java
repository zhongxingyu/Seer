 package com.mamlambo.artut;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.OutputStream;
 
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.app.AlarmManager;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.PendingIntent;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.res.Configuration;
 import android.graphics.Bitmap;
 import android.graphics.Bitmap.Config;
 import android.graphics.Canvas;
 import android.graphics.Matrix;
 import android.hardware.Camera;
 import android.hardware.Camera.AutoFocusCallback;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Environment;
 import android.os.Handler;
 import android.os.Message;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.FrameLayout;
 import android.widget.ImageView;
 
 import com.mamlambo.globals.Global;
 
 @SuppressLint("NewApi")
 public class ArtutActivity extends Activity {
 	private static final String TAG = "ArtutActivity";
 	private static final int DIALOG_YES_NO_MESSAGE = 1;
 	private static final int DIALOG_SINGLE_CHOICE = 2;
 	private static final String ACTION_RETRY_GPS_INIT = "retry_gps_init";
 
 	private int slopeMeasureType = 0;
 	private OverlayView arContent;
 	private ArDisplayView arDisplay;
 	private LocationManager mLocationManager;
 	private AlarmManager mAlertManager;
 	private Location mCurrentLocation = null;
 
 	public static ArtutActivity Instance;
 	private BroadcastReceiver mReceiver = new BroadcastReceiver() {
 
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			if (ACTION_RETRY_GPS_INIT.equals(intent.getAction())) {
 				initGPS();
 			}
 		}
 	};
 
 	
 	private void saveScreenShot() {
 		File dir = new File(Global.ARTUTIMAGE_CAPTURE_PATH);
 		dir.mkdirs();
 		long timeMillis = System.currentTimeMillis();
 		Bitmap screen;
 		OutputStream fout = null;
 		String filename;
 		filename = String.format("/SlopeView_" + String.valueOf(timeMillis)
 				+ ".jpg");
 		File imageFile = new File(dir + filename);
 		View screenview = (View) findViewById(android.R.id.content);
 		screenview.setDrawingCacheEnabled(true);
 		screen = Bitmap.createBitmap(screenview.getDrawingCache());
 		screenview.setDrawingCacheEnabled(false);
 		byte[] data = arDisplay.lastPreviewData;
 		int[] rgbIm = decodeYUV420SP(data, arDisplay.mSize.width,
 				arDisplay.mSize.height);
 		Bitmap preview = Bitmap.createBitmap(rgbIm, arDisplay.mSize.width,
 				arDisplay.mSize.height, Config.ARGB_8888);
 		Bitmap result = Bitmap.createBitmap(preview.getWidth(),
 				preview.getHeight(), Config.ARGB_8888);
 		int previewHeight = preview.getHeight();
 		int screenHeight = screen.getHeight();
 		Canvas c = new Canvas(result);
 		c.drawBitmap(preview, new Matrix(), null);
 		if (previewHeight - screenHeight > 0) {
 			c.drawBitmap(screen, 0, (previewHeight - screenHeight) / 2,
 					null);
 		} else {
 			c.drawBitmap(screen, new Matrix(),
 					null);
 		}
 		try {
 			fout = new FileOutputStream(imageFile);
 			result.compress(Bitmap.CompressFormat.JPEG, 100, fout);
 			fout.flush();
 			fout.close();
 			sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED,
 					Uri.parse("file://"
 							+ Environment.getExternalStorageDirectory())));
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	private LocationListener mLocationListener = new LocationListener() {
 
 		public void onStatusChanged(String provider, int status, Bundle extras) {
 		}
 
 		public void onProviderEnabled(String provider) {
 		}
 
 		public void onProviderDisabled(String provider) {
 		}
 
 		public void onLocationChanged(Location location) {
 			if (location != null) {
 				mCurrentLocation = location;
 				arContent.setLocation(mCurrentLocation);
 			}
 		}
 	};
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 		Instance = this;
 		FrameLayout arViewPane = (FrameLayout) findViewById(R.id.ar_view_pane);
 		arDisplay = new ArDisplayView(getApplicationContext(), this);
 		arViewPane.addView(arDisplay);
 		arContent = new OverlayView(getApplicationContext());
 		arViewPane.addView(arContent);
 		mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
 		mAlertManager = (AlarmManager) getSystemService(ALARM_SERVICE);
 		if (!initGPS()) {
 			mAlertManager.set(AlarmManager.RTC_WAKEUP,
 					System.currentTimeMillis(), prepareIntent());
 		}
 		/* Display a radio button group */
 		Button slopeButton = (Button) findViewById(R.id.slope);
 		slopeButton.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				showDialog(DIALOG_SINGLE_CHOICE);
 			}
 		});
 
 		/* Get ScreenShot */
 		Button cameraButton = (Button) findViewById(R.id.camera);
 		cameraButton.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
				saveScreenShot();
 			}
 		});
 
 		/* Show Agree Dialog */
 		Handler handler = new Handler() {
 			public void handleMessage(Message msg) {
 				showDialog(DIALOG_YES_NO_MESSAGE);
 			}
 		};
 
 		Global.isShowVersionText = true;
 		handler.sendEmptyMessageDelayed(0, 1000);
 
 		ArtutActivity.this.sendBroadcast(new Intent(
 				Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://"
 						+ Global.ARTUTIMAGE_CAPTURE_PATH)));
 	}
 
 	private PendingIntent prepareIntent() {
 		Intent i = new Intent(ACTION_RETRY_GPS_INIT);
 		PendingIntent pi = PendingIntent.getBroadcast(this, 0, i,
 				PendingIntent.FLAG_UPDATE_CURRENT);
 		return pi;
 	}
 
 	private boolean initGPS() {
 		if (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
 			mLocationManager.requestLocationUpdates(
 					LocationManager.GPS_PROVIDER, 5 * 1000, 10,
 					mLocationListener);
 			return true;
 		} else if (mLocationManager
 				.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
 			mLocationManager.requestLocationUpdates(
 					LocationManager.NETWORK_PROVIDER, 5 * 1000, 10,
 					mLocationListener);
 			return true;
 		}
 		return false;
 	}
 
 	public int[] decodeYUV420SP(byte[] yuvData, int dataWidth, int dataHeight) {
 		final int frameSize = dataWidth * dataHeight;
 		int[] rgb = new int[frameSize];
 		for (int j = 0, yp = 0; j < dataHeight; j++) {
 			int uvp = frameSize + (j >> 1) * dataWidth, u = 0, v = 0;
 			for (int i = 0; i < dataWidth; i++, yp++) {
 				int y = (0xff & ((int) yuvData[yp])) - 16;
 				if (y < 0) {
 					y = 0;
 				}
 				if ((i & 1) == 0) {
 					v = (0xff & yuvData[uvp++]) - 128;
 					u = (0xff & yuvData[uvp++]) - 128;
 				}
 
 				int y1192 = 1192 * y;
 				int r = (y1192 + 1634 * v);
 				int g = (y1192 - 833 * v - 400 * u);
 				int b = (y1192 + 2066 * u);
 
 				if (r < 0) {
 					r = 0;
 				} else if (r > 262143) {
 					r = 262143;
 				}
 				if (g < 0) {
 					g = 0;
 				} else if (g > 262143) {
 					g = 262143;
 				}
 				if (b < 0) {
 					b = 0;
 				} else if (b > 262143) {
 					b = 262143;
 				}
 
 				rgb[yp] = 0xff000000 | ((r << 6) & 0xff0000)
 						| ((g >> 2) & 0xff00) | ((b >> 10) & 0xff);
 			}
 		}
 		return rgb;
 	}
 
 	@Override
 	protected void onPause() {
 		arContent.onPause();
 		mLocationManager.removeUpdates(mLocationListener);
 		unregisterReceiver(mReceiver);
 		super.onPause();
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		arContent.onResume();
 		registerReceiver(mReceiver, new IntentFilter(ACTION_RETRY_GPS_INIT));
 	}
 
 	@Override
 	protected Dialog onCreateDialog(int id) {
 		switch (id) {
 		case DIALOG_YES_NO_MESSAGE:
 			final ImageView iv = (ImageView) findViewById(R.id.imageview);
 			iv.setVisibility(View.VISIBLE);
 			return new AlertDialog.Builder(ArtutActivity.this)
 					.setIconAttribute(R.drawable.svicon)
 					.setTitle(R.string.alert_dialog_agreetitle)
 					.setMessage(R.string.alert_dialog_agreemsg)
 					.setPositiveButton(R.string.alert_dialog_agree,
 							new DialogInterface.OnClickListener() {
 								public void onClick(DialogInterface dialog,
 										int whichButton) {
 									iv.setVisibility(View.INVISIBLE);
 									Global.isShowVersionText = false;
 								}
 							})
 					.setNegativeButton(R.string.alert_dialog_disagree,
 							new DialogInterface.OnClickListener() {
 								public void onClick(DialogInterface dialog,
 										int whichButton) {
 									iv.setVisibility(View.INVISIBLE);
 									finish();
 								}
 							}).create();
 		case DIALOG_SINGLE_CHOICE:
 			return new AlertDialog.Builder(ArtutActivity.this)
 					.setIcon(R.drawable.svicon)
 					// .setIconAttribute(android.R.attr.alertDialogIcon)
 					.setTitle(R.string.slope_line_option_title)
 					.setSingleChoiceItems(R.array.slope_line_array, 0,
 							new DialogInterface.OnClickListener() {
 								public void onClick(DialogInterface dialog,
 										int whichButton) {
 									slopeMeasureType = whichButton;
 								}
 							})
 					.setPositiveButton(R.string.alert_dialog_ok,
 							new DialogInterface.OnClickListener() {
 								public void onClick(DialogInterface dialog,
 										int whichButton) {
 									Global.slopeMeasureType = slopeMeasureType;
 								}
 							})
 					.setNegativeButton(R.string.alert_dialog_cancel,
 							new DialogInterface.OnClickListener() {
 								public void onClick(DialogInterface dialog,
 										int whichButton) {
 								}
 							}).create();
 		}
 		return null;
 	}
 
 	@Override
 	public void onConfigurationChanged(Configuration newConfig) {
 		// TODO Auto-generated method stub
 		super.onConfigurationChanged(newConfig);
 	}
 	
 	
 }
