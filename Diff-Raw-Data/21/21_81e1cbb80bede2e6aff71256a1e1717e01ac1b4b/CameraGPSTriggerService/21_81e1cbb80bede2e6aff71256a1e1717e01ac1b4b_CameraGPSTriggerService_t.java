 package com.lspr.services;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.List;
 import java.util.Locale;
 
 import android.app.Service;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.hardware.Camera;
 import android.hardware.Camera.PictureCallback;
 import android.location.Address;
 import android.location.Geocoder;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.media.AudioManager;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.util.Log;
 import android.view.SurfaceView;
 import android.widget.Toast;
 
 import com.lspr.constants.LSPRConstants;
 import com.lspr.modules.Mail;
 
 public class CameraGPSTriggerService extends Service {
 
 	private static final String TAG = "Service";
 	private SharedPreferences prefs;
 	private static LocationManager mlocManager;
 	private String mlat;
 	private String mlong;
 	private StringBuilder mAddress;
 	Geocoder geocoder;
 	public static boolean stopGps = false;
	public static boolean stopThread = false;
 
 	public CameraGPSTriggerService() {
 		super();
 		prefs = null;
 	}
 
 	/** A safe way to get an instance of the Camera object. */
 	public static Camera getCameraInstance() {
 		Camera c = null;
 		/*
 		 * try { // 1 as the parameter is for the front camera, 0 is for back
 		 * camera c = Camera.open(1); // attempt to get a Camera instance }
 		 * catch (Exception e) { // Camera is not available (in use or does not
 		 * exist) }
 		 */
 		int cameraCount = 0;
 		boolean hasFront = false, hasBack = false;
 		Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
 		cameraCount = Camera.getNumberOfCameras();
 		for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
 			Camera.getCameraInfo(camIdx, cameraInfo);
 			if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
 				// phone has a front facing camera available
 				hasFront = true;
 
 			} else if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
 				// phone has a back facing camera available
 				hasBack = true;
 			} else {
 				// unable to determine camera information
 			}
 		}
 		// priority to use front facing camera if available
 		// if not, use back camera
 		if (hasFront) {
 			c = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
 		} else if (hasBack) {
 			c = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
 		} else {
 			// return null if camera isn't available
 			c = null;
 		}
 
 		return c;
 	}
 
 	private void releaseCamera() {
 		if (mCamera != null) {
 			mCamera.release(); // release the camera for other applications
 			mCamera = null;
 		}
 	}
 
 	public static final int MEDIA_TYPE_IMAGE = 1;
 	public static final int MEDIA_TYPE_VIDEO = 2;
 	private Camera mCamera;
 
 	// picture call back start
 	PictureCallback mPicture = new PictureCallback() {
 
 		public void onPictureTaken(byte[] data, Camera camera) {
 
 			// Get app preference
 			prefs = getApplicationContext().getSharedPreferences(
 					LSPRConstants.PREF_NAME, 0);
 
 			// create unique file name
 			String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
 					.format(new Date());
 
 			final String FILENAME = "IMG_" + timeStamp + ".jpg";
 
 			prefs.edit().putString(LSPRConstants.PREF_FILE_NAME, FILENAME)
 					.commit();
 
 			try {
 
 				FileOutputStream fos = openFileOutput(FILENAME,
 						Context.MODE_PRIVATE);
 				fos.write(data);
 				fos.close();
 
 				// Grab GPS location
 				// ----------------------------------------------------------------------
 				int duration = prefs.getInt(
 						LSPRConstants.PREF_SEND_EMAIL_DURATION, 900000);
 
 				mlocManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
 				mlocManager.requestLocationUpdates(
						LocationManager.NETWORK_PROVIDER, 10000, 0,
 						networklocationListener);
 
 			} catch (FileNotFoundException e) {
 				// Log.d(TAG, "File not found: " + e.getMessage());
 			} catch (IOException e) {
 				// Log.d(TAG, "Error accessing file: " + e.getMessage());
 			}
 		}
 	};
 
 	private final LocationListener networklocationListener = new LocationListener() {
 		public void onLocationChanged(Location location) {
 			// Called when a new location is found by the network location
 			// provider.
 			double lat = location.getLatitude();
 			double lng = location.getLongitude();
 			mlat = "" + lat;
 			mlong = "" + lng;
			stopThread = false;
 
 			List<Address> addresses;
 			try {
 				addresses = geocoder.getFromLocation(lat, lng, 1);
 				// mAddress = addresses.get(0).toString();
 				if (addresses != null) {
 					Address returnedAddress = addresses.get(0);
 					StringBuilder strReturnedAddress = new StringBuilder(
 							"Address:\n");
 					for (int i = 0; i < returnedAddress
 							.getMaxAddressLineIndex(); i++) {
 						strReturnedAddress.append(
 								returnedAddress.getAddressLine(i)).append("\n");
 					}
 					mAddress = strReturnedAddress;
 				}
 			} catch (IOException e1) {
 				// TODO Auto-generated catch block
 				e1.printStackTrace();
 			}
 
 			// Emails out picture and location
 			// ----------------------------------------------------------------------
 			// Email module grabs from SD, attach and sends email
 			final String emailText = prefs.getString(LSPRConstants.PREF_EMAIL,
 					"email@domain.com");
 			final String emailPassword = prefs.getString(
 					LSPRConstants.PREF_EMAIL_PASS, "email_password");
 
 			// Get app preference
 			prefs = getApplicationContext().getSharedPreferences(
 					LSPRConstants.PREF_NAME, 0);
 
 			// send mail
 			new Thread(new Runnable() {
 				public void run() {
					if (!stopGps && !stopThread) {
						stopThread = true;
 						sendMail(emailText, emailPassword, mlat, mlong,
 								mAddress);
						
 					}
 				}
 
 				private void sendMail(String email, String pass, String mlat,
 						String mlong, StringBuilder mAddress) {
 
 					final Mail m = new Mail();
 					String usertext = email;
 					String emailPass = pass;
 					String[] toArr = { usertext };
 					m.setPass(emailPass);
 					m.setUser(usertext);
 					m.setTo(toArr);
 					m.setFrom(usertext);
 					m.setSubject("Stolen Phone Recovery Log");
 					m.setBody("GPS Coordinates: " + "\nLat: " + mlat
 							+ "\nLong: " + mlong + "\nAddress: " + mAddress);
 					try {
 
 						// get path to picture
 						String path_to_picture = prefs.getString(
 								LSPRConstants.PREF_PATH_NAME, "file_path")
 								+ File.separator
 								+ prefs.getString(LSPRConstants.PREF_FILE_NAME,
 										"file_name");
 
 						// attach picture
 						m.addAttachment(path_to_picture);
 
 						// send email
 						if (m.send()) {
 							Log.e(TAG, "Email sent successful.");
 						} else {
 							Log.e(TAG, "Email was not sent.");
 						}
 					} catch (Exception e) {
 						Log.e(TAG, "Problem sending email.");
 					}
 				}
 			}).start();
 
 			if (stopGps) {
 				mlocManager.removeUpdates(networklocationListener);
 			}
 
 			stopSelf();
 
 		}
 
 		public void onStatusChanged(String provider, int status, Bundle extras) {
 		}
 
 		public void onProviderEnabled(String provider) {
 		}
 
 		public void onProviderDisabled(String provider) {
 		}
 	};
 
 	@Override
 	public IBinder onBind(Intent intent) {
 		return null;
 	}
 
 	@Override
 	public void onCreate() {
 		geocoder = new Geocoder(this, Locale.ENGLISH);
 
 	}
 
 	@Override
 	public void onDestroy() {
 		releaseCamera();
 	}
 
 	@Override
 	public void onStart(Intent intent, int startid) {
 
 		// Capture picture
 		// ----------------------------------------------------------------------
 
 		// Create an instance of Camera
 		mCamera = getCameraInstance();
 		SurfaceView view = new SurfaceView(this.getApplicationContext());
 		try {
 			mCamera.setPreviewDisplay(view.getHolder());
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		mCamera.startPreview();
 		// change ringer to silent to disable shutter sound
 		AudioManager am = (AudioManager) this.getApplicationContext()
 				.getSystemService(Context.AUDIO_SERVICE);
 		am.setRingerMode(AudioManager.RINGER_MODE_SILENT);
 		// rotate to correct position
 		Camera.Parameters params = mCamera.getParameters();
 		params.setRotation(90);
 		mCamera.setParameters(params);
 		// take picture
 		mCamera.takePicture(null, null, mPicture);
 		// return to normal sound settings
 	}
 }
