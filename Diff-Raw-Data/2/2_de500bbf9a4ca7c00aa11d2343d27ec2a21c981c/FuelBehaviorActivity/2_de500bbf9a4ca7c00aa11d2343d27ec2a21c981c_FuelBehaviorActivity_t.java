 package edu.mines.zapcraft.FuelBehavior;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.PendingIntent;
 import android.content.BroadcastReceiver;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.os.Bundle;
 import android.os.ParcelFileDescriptor;
 import android.os.PowerManager;
 import android.os.PowerManager.WakeLock;
 import android.util.Log;
 import android.view.View;
 import android.widget.Button;
 import android.widget.TextView;
 import android_serialport_api.SerialPort;
 
 import com.android.future.usb.UsbAccessory;
 import com.android.future.usb.UsbManager;
 
 import java.io.File;
 import java.io.FileDescriptor;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 
 import java.util.Calendar;
 
 import net.sf.marineapi.nmea.io.SentenceReader;
 
 import org.mapsforge.android.maps.MapContext;
 import org.mapsforge.android.maps.MapController;
 import org.mapsforge.android.maps.MapScaleBar;
 import org.mapsforge.android.maps.MapView;
 import org.mapsforge.android.maps.overlay.ArrayItemizedOverlay;
 import org.mapsforge.android.maps.overlay.ItemizedOverlay;
 import org.mapsforge.android.maps.overlay.OverlayItem;
 import org.mapsforge.core.GeoPoint;
 
 public class FuelBehaviorActivity extends Activity implements MapContext, Updatable {
 	private static final String TAG = FuelBehaviorActivity.class.getSimpleName();
 
 	private static final String ACTION_USB_PERMISSION = "edu.mines.zapcraft.FuelBehavior.action.USB_PERMISSION";
 
 	private UsbManager mUsbManager;
 	private PendingIntent mPermissionIntent;
 	private boolean mPermissionRequestPending;
 
 	private UsbAccessory mAccessory;
 	private ParcelFileDescriptor mAccessoryFileDescriptor;
 	private InputStream mAccessoryInputStream;
 	private OutputStream mAccessoryOutputStream;
 
 	private SerialPort mSerialPort;
 	private SentenceReader mSentenceReader;
 	private InputStream mGPSInputStream;
 
 	private AdkReader mAdkReader;
 
 	private DataHandler mDataHandler;
 
 	private WakeLock mWakeLock;
 
 	private MapController mMapController;
 	private MapView mMapView;
 
 	private ArrayItemizedOverlay mItemizedOverlay;
 	private OverlayItem mOverlayItem;
 
 	private DbAdapter mDbAdapter;
 
 	private DataLogger mDataLogger;
 
 	private PeriodicUpdater mUpdater;
 
 	private boolean mControlsVisible = false;
 
 	private int mLastMapViewId;
 
 	private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			String action = intent.getAction();
 			if (ACTION_USB_PERMISSION.equals(action)) {
 				synchronized (this) {
 					UsbAccessory accessory = UsbManager.getAccessory(intent);
 					if (intent.getBooleanExtra(
 							UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
 						openAccessory(accessory);
 					} else {
 						Log.d(TAG, "permission denied for accessory "
 								+ accessory);
 					}
 					mPermissionRequestPending = false;
 				}
 			} else if (UsbManager.ACTION_USB_ACCESSORY_DETACHED.equals(action)) {
 				UsbAccessory accessory = UsbManager.getAccessory(intent);
 				if (accessory != null && accessory.equals(mAccessory)) {
 					closeAccessory();
 				}
 			}
 		}
 	};
 
 	/** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
         mWakeLock = powerManager.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "AMV");
 
         // setup receivers for USB notifications
         mUsbManager = UsbManager.getInstance(this);
 		mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(
 				ACTION_USB_PERMISSION), 0);
 		IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
 		filter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
 		registerReceiver(mUsbReceiver, filter);
 
 		mDbAdapter = new DbAdapter(this);
         mDbAdapter.open();
 
 		mDataHandler = new DataHandler();
 		mUpdater = new PeriodicUpdater(500, this);
 
 		mDataLogger = new DataLogger(mDataHandler, mDbAdapter);
 
 		ContentValues values = new ContentValues();
 		values.put("start_time", Calendar.getInstance().getTimeInMillis());
 		mDataLogger.setDriveId(mDbAdapter.createDrive(values));
 
 		//showControls();// should be hideControls, but I need to test the interface
 		hideControls();
     }
 
     @Override
 	public void onResume() {
 		super.onResume();
 
 		if (!mWakeLock.isHeld()) {
 			mWakeLock.acquire();
 		}
 
 		if (mAccessoryInputStream == null || mAccessoryOutputStream == null) {
 			UsbAccessory[] accessories = mUsbManager.getAccessoryList();
 			UsbAccessory accessory = (accessories == null ? null : accessories[0]);
 			if (accessory != null) {
 				if (mUsbManager.hasPermission(accessory)) {
 					openAccessory(accessory);
 				} else {
 					synchronized (mUsbReceiver) {
 						if (!mPermissionRequestPending) {
 							mUsbManager.requestPermission(accessory,
 									mPermissionIntent);
 							mPermissionRequestPending = true;
 						}
 					}
 				}
 			} else {
 				Log.d(TAG, "mAccessory is null");
 			}
 		}
 
 		startGPS();
 
 		if (mControlsVisible) {
 			mUpdater.start();
 		}
 
 		if (mMapView != null) {
 			mMapView.onResume();
 		}
 	}
 
     @Override
 	public void onPause() {
 		super.onPause();
 
 		if (mWakeLock.isHeld()) {
 			mWakeLock.release();
 		}
 
 		mUpdater.stop();
 
 		closeAccessory();
 		stopGPS();
 
 		if (mMapView != null) {
 			mMapView.onPause();
 		}
 	}
 
     @Override
 	public void onDestroy() {
     	super.onDestroy();
 
 		unregisterReceiver(mUsbReceiver);
 
 		mDbAdapter.close();
 
 		if (mMapView != null) {
 			mMapView.onDestroy();
 		}
 	}
 
 	private void openAccessory(UsbAccessory accessory) {
 		mAccessoryFileDescriptor = mUsbManager.openAccessory(accessory);
 		if (mAccessoryFileDescriptor != null) {
 			mAccessory = accessory;
 			FileDescriptor fd = mAccessoryFileDescriptor.getFileDescriptor();
 			mAccessoryInputStream = new FileInputStream(fd);
 			mAccessoryOutputStream = new FileOutputStream(fd);
 
 			mAdkReader = new AdkReader(mAccessoryInputStream);
 			mAdkReader.setListener(mDataHandler);
 			mAdkReader.start();
 			Log.d(TAG, "accessory opened");
 
 			showControls();
 
 			mDataLogger.start();
 		} else {
 			Log.d(TAG, "accessory open fail");
 		}
 	}
 
 	private void closeAccessory() {
		Log.d(TAG, "accessory closed");

 		hideControls();
 
 		mDataLogger.stop();
 
 		try {
 			if (mAdkReader != null) {
 				mAdkReader.stop();
 			}
 
 			if (mAccessoryFileDescriptor != null) {
 				mAccessoryFileDescriptor.close();
 			}
 		} catch (IOException e) {
 		} finally {
 			mAccessoryFileDescriptor = null;
 			mAccessoryInputStream = null;
 			mAccessoryOutputStream = null;
 			mAccessory = null;
 			mAdkReader = null;
 		}
 	}
 
 	private void startGPS() {
 		try {
 			mSerialPort = new SerialPort(new File("/dev/ttyUSB0"), 4800, 0);
 			mGPSInputStream = mSerialPort.getInputStream();
 			mSentenceReader = new SentenceReader(mGPSInputStream);
             mSentenceReader.addSentenceListener(mDataHandler);
             mSentenceReader.start();
 		} catch (SecurityException e) {
 			DisplayError(R.string.error_gps_security);
 		} catch (IOException e) {
 			DisplayError(R.string.error_gps_ioexception);
 		}
 	}
 
 	private void stopGPS() {
 		try {
 			if (mSentenceReader != null) {
 				mSentenceReader.stop();
 			}
 
 			if (mSerialPort != null) {
 				mSerialPort.close();
 			}
 		} finally {
 			mSentenceReader = null;
 			mSerialPort = null;
 			mGPSInputStream = null;
 		}
 	}
 
 	public void hideControls() {
 		mControlsVisible = false;
 		mUpdater.stop();
 
 		if (mMapView != null) {
 			mMapView.onDestroy();
 		}
 
 		mMapView = null;
 
 		setContentView(R.layout.no_device);
 	}
 
 	public void showControls() {
 		setContentView(R.layout.main);
 
 		mMapView.setClickable(true);
 		mMapView.setBuiltInZoomControls(true);
 		mMapView.setMapFile(new File("/sdcard/frontrange.map"));
 		MapScaleBar mapScaleBar = mMapView.getMapScaleBar();
 		mapScaleBar.setImperialUnits(true);
 		mapScaleBar.setShowMapScaleBar(true);
 
 		mMapController = mMapView.getController();
 
 		mItemizedOverlay = new ArrayItemizedOverlay(null);
 		mOverlayItem = new OverlayItem();
 		mOverlayItem.setMarker(ItemizedOverlay.boundCenter(getResources().getDrawable(R.drawable.my_location)));
 		mItemizedOverlay.addItem(this.mOverlayItem);
 		mMapView.getOverlays().add(this.mItemizedOverlay);
 
 		Button button1 = (Button) findViewById(R.id.button1);
 		button1.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View view) {
 				updatePosition();
 			}
 		});
 
 		Button button2 = (Button) findViewById(R.id.start_log_button);
 		button2.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View view) {
 				mDataLogger.start();
 			}
 		});
 
 		mControlsVisible = true;
 		mUpdater.start();
 	}
 
 	public void update() {
 		updatePosition();
 		setGauge(mDataHandler.getRpm());
 	}
 
 	public void sendCommand(byte[] command) {
 		if (mAccessoryOutputStream != null) {
 			try {
 				mAccessoryOutputStream.write(command);
 			} catch (IOException e) {
 				Log.e(TAG, "write failed", e);
 			}
 		}
 	}
 
 	public void handleStringMessage(String message) {
 		TextView textView = (TextView) findViewById(R.id.output_text);
 		textView.append(message + "\n");
 	}
 
 	public void setGauge(float value) {
 		Gauge gauge = (Gauge) findViewById(R.id.gauge1);
 		gauge.setHandValue(value);
 	}
 
 	public void updatePosition() {
 		//handleStringMessage("Lat: " + mDataHandler.getLatitude() + " Lon: " + mDataHandler.getLongitude() + " Alt: " + mDataHandler.getAltitude() + " m");
 		//handleStringMessage("Speed: " + mDataHandler.getGpsSpeed() + " Course: " + mDataHandler.getCourse());
 
 		GeoPoint point = new GeoPoint(mDataHandler.getLatitude(), mDataHandler.getLongitude());
 		mOverlayItem.setPoint(point);
 		mItemizedOverlay.requestRedraw();
 		mMapController.setCenter(point);
 	}
 
 	private void DisplayError(int resourceId) {
 		AlertDialog.Builder b = new AlertDialog.Builder(this);
 		b.setTitle("Error");
 		b.setMessage(resourceId);
 		b.show();
 	}
 
 	/**
      * Returns a unique MapView ID on each call.
      *
      * @return the new MapView ID.
      */
 	@Override
     public int getMapViewId() {
 		return ++mLastMapViewId;
     }
 
 	@Override
 	public void registerMapView(MapView mapView) {
 		mMapView = mapView;
 	}
 }
