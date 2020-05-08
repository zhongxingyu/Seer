 package com.unict.geophoto;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Locale;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Matrix;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.media.ExifInterface;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Environment;
 import android.preference.PreferenceManager;
 import android.provider.MediaStore;
 import android.provider.Settings;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 import android.util.Base64;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewTreeObserver;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 
 public class MainActivity extends FragmentActivity implements LocationListener,
 		SendFragment.SendCallbacks {
 
 	private static final int ACTION_TAKE_PHOTO = 1;
 	private static final int ACTION_ENABLE_LOCATION_SOURCE = 2;
 	private static final String JPEG_FILE_PREFIX = "IMG_";
 	private static final String JPEG_FILE_SUFFIX = ".jpg";
 	private File imagePath;
 	private double latitude;
 	private double longitude;
 	private String date = "";
 	private ImageButton buttonPhoto;
 	private ImageButton buttonLocation;
 	private ImageButton buttonSend;
 	private MenuItem menuItemClearAll;
 	private ImageView imageView;
 	private TextView textLocation;
 	private TextView textDate;
 	private ProgressBar progressBar;
 	private LocationManager locationManager;
 	private String provider;
 	private boolean locationSearching = false;
 	private boolean locationEstablished = false;
 	private boolean sending = false;
 	private SendFragment sendFragment;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		buttonPhoto = (ImageButton) findViewById(R.id.buttonPhoto);
 		buttonLocation = (ImageButton) findViewById(R.id.buttonLocation);
 		buttonSend = (ImageButton) findViewById(R.id.buttonSend);
 		imageView = (ImageView) findViewById(R.id.imageViewPhoto);
 		textLocation = (TextView) findViewById(R.id.textLocation);
 		textDate = (TextView) findViewById(R.id.textDate);
 		progressBar = (ProgressBar) findViewById(R.id.progressBar);
 		locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
 		provider = LocationManager.GPS_PROVIDER;
 		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
 		FragmentManager fm = getSupportFragmentManager();
 		sendFragment = (SendFragment) fm.findFragmentByTag("task");
 		if (sendFragment == null) {
 			sendFragment = new SendFragment();
 			fm.beginTransaction().add(sendFragment, "task").commit();
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		menuItemClearAll = menu.findItem(R.id.action_clear_all);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.action_settings:
 			Intent settingsActivity = new Intent(getBaseContext(),
 					SettingsActivity.class);
 			startActivity(settingsActivity);
 			return true;
 		case R.id.action_clear_all:
 			this.imagePath = null;
 			this.locationEstablished = false;
 			this.date = "";
 			repaintImage();
 			repaintLocation();
 			repaintDate();
 			updateGUI();
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode,
 			Intent intent) {
 		switch (requestCode) {
 		case ACTION_TAKE_PHOTO: {
 			if (resultCode == Activity.RESULT_OK) {
 				repaintImage();
 			} else {
 				imagePath.delete();
 				imagePath = null;
 			}
 			break;
 		}
 		case ACTION_ENABLE_LOCATION_SOURCE: {
 			Log.d("GeoPhoto", "Return from location source settings");
 			retrieveLocation();
 			break;
 		}
 		}
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		repaintImage();
 		repaintLocation();
 		repaintDate();
 		updateGUI();
 	}
 
 	@Override
 	protected void onSaveInstanceState(Bundle outState) {
 		super.onSaveInstanceState(outState);
 		outState.putDouble("latitude", this.latitude);
 		outState.putDouble("longitude", this.longitude);
 		outState.putBoolean("location_searching", this.locationSearching);
 		outState.putBoolean("location_enstablished", this.locationEstablished);
 		outState.putBoolean("sending", this.sending);
 		outState.putString("date", this.date);
 		if (this.imagePath != null) {
 			outState.putString("path", this.imagePath.getAbsolutePath());
 		} else {
 			outState.putString("path", null);
 		}
 	}
 
 	@Override
 	protected void onRestoreInstanceState(Bundle savedInstanceState) {
 		super.onRestoreInstanceState(savedInstanceState);
 		this.latitude = savedInstanceState.getDouble("latitude");
 		this.longitude = savedInstanceState.getDouble("longitude");
 		this.locationSearching = savedInstanceState
 				.getBoolean("location_searching");
 		this.locationEstablished = savedInstanceState
 				.getBoolean("location_enstablished");
 		this.sending = savedInstanceState.getBoolean("sending");
 		this.date = savedInstanceState.getString("date");
 		String path = savedInstanceState.getString("path");
 		if (path != null) {
 			this.imagePath = new File(path);
 		} else {
 			this.imagePath = null;
 		}
 	}
 
 	/*
 	 * User interface callbacks
 	 */
 
 	/**
 	 * Called when the user clicks the photo button
 	 */
 	public void takePhoto(View view) {
 		try {
 			imagePath = createImageFile();
 			Intent takePictureIntent = new Intent(
 					MediaStore.ACTION_IMAGE_CAPTURE);
 			takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
 					Uri.fromFile(imagePath));
 			startActivityForResult(takePictureIntent, ACTION_TAKE_PHOTO);
 		} catch (Exception e) {
 			e.printStackTrace();
 			imagePath = null;
 		}
 	}
 
 	/**
 	 * Called when the user clicks the location button
 	 */
 	public void takeLocation(View view) {
 		retrieveLocation();
 	}
 
 	/**
 	 * Called when the user clicks the send button
 	 */
 	public void sendData(View view) {
 		if (!this.sending) {
 			ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
 			NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
 			if (networkInfo != null && networkInfo.isConnected()) {
 				this.sending = true;
 				updateGUI();
 				SharedPreferences sharedPrefs = PreferenceManager
 						.getDefaultSharedPreferences(this);
 				String serverUrl = sharedPrefs
 						.getString("server_url_preference",
 								getString(R.string.server_url));
 				sendFragment.start(serverUrl, getXML());
 			} else {
 				Log.d("GeoPhoto", "No network connection available.");
 				showNetworkAlert();
 			}
 		}
 	}
 
 	/*
 	 * Internal methods to take the photo
 	 */
 	private File createImageFile() throws IOException {
 		// Create an image file name
 		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
 				Locale.getDefault()).format(new Date());
 		String imageFileName = JPEG_FILE_PREFIX + timeStamp + "_";
 		File image = File.createTempFile(imageFileName, JPEG_FILE_SUFFIX,
 				getAlbumDir());
 		return image;
 	}
 
 	private File getAlbumDir() {
 		File albumDir = new File(
 				Environment
 						.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
 				getString(R.string.album_name));
 		if (albumDir != null) {
 			if (!albumDir.mkdirs()) {
 				if (!albumDir.exists()) {
 					Log.d("GeoPhoto", "failed to create directory");
 					return null;
 				}
 			}
 		}
 		return albumDir;
 	}
 
 	/*
 	 * Internal methods to take location
 	 */
 	private void retrieveLocation() {
 		if (!this.locationSearching) {
 			boolean enabled = locationManager.isProviderEnabled(provider);
 			if (!enabled) {
 				showGPSAlert();
 			} else {
 				Log.d("GeoPhoto", "Location Enabled");
 				this.locationSearching = true;
 				this.locationEstablished = false;
 				repaintLocation();
 				updateGUI();
 				locationManager.requestSingleUpdate(provider, this,
 						getMainLooper());
 			}
 		}
 	}
 
 	private void showGPSAlert() {
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setTitle(getString(R.string.dialog_gps_title))
 				.setMessage(getString(R.string.dialog_gps_message))
 				.setPositiveButton(getString(android.R.string.ok),
 						new DialogInterface.OnClickListener() {
 							public void onClick(DialogInterface dialog, int id) {
 								Intent intent = new Intent(
 										Settings.ACTION_LOCATION_SOURCE_SETTINGS);
 								startActivityForResult(intent,
 										ACTION_ENABLE_LOCATION_SOURCE);
 							}
 						})
 				.setNegativeButton(getString(android.R.string.cancel),
 						new DialogInterface.OnClickListener() {
 							public void onClick(DialogInterface dialog, int id) {
 								// User cancelled the dialog
 							}
 						});
 		// show the created AlertDialog
 		builder.show();
 	}
 
 	/*
 	 * Internal methods and classes to send the data over the internet
 	 */
 	private String getXML() {
 		String imageBase64 = getImageBase64();
		String xml = "<?xml version=\"1.0\"?>\n<photo>\n";
		xml += "<image>" + imageBase64 + "</image>\n";
		xml += "<date>" + this.date + "</date>\n";
		xml += "<location>\n<latitude>" + this.latitude
 				+ "</latitude>\n<longitude>" + this.longitude
				+ "</longitude>\n</location>\n";
		xml += "</photo>";
		return xml;
 	}
 
 	private String getImageBase64() {
 		byte[] byteArray = new byte[(int) imagePath.length()];
 		try {
 			FileInputStream fileInputStream = new FileInputStream(imagePath);
 			fileInputStream.read(byteArray);
 			fileInputStream.close();
 		} catch (IOException e) {
 			Log.e("GeoPhoto", "Error in image read", e);
 		}
 		return Base64.encodeToString(byteArray, Base64.DEFAULT);
 	}
 
 	private void showNetworkAlert() {
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setTitle(getString(R.string.dialog_network_title))
 				.setMessage(getString(R.string.dialog_network_message))
 				.setPositiveButton(getString(android.R.string.ok),
 						new DialogInterface.OnClickListener() {
 							public void onClick(DialogInterface dialog, int id) {
 							}
 						});
 		builder.show();
 	}
 
 	/*
 	 * Repaint methods
 	 */
 	private void repaintImage() {
 		if (imagePath == null) {
 			imageView.setImageDrawable(null);
 		} else {
 			// retrieve the rotation
 			final int rotation = getImageRotation();
 			// retrieve sizes
 			BitmapFactory.Options opts = new BitmapFactory.Options();
 			opts.inJustDecodeBounds = true;
 			BitmapFactory.decodeFile(imagePath.getAbsolutePath(), opts);
 			final int height = getHeightRotated(rotation, opts.outHeight,
 					opts.outWidth);
 			final int width = getWidthRotated(rotation, opts.outHeight,
 					opts.outWidth);
 			// Create the observer otherwise imageView hasn't the right sizes
 			ViewTreeObserver vto = imageView.getViewTreeObserver();
 			vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
 				public boolean onPreDraw() {
 					int reqHeight = imageView.getMeasuredHeight();
 					int reqWidth = imageView.getMeasuredWidth();
 					// calculate sample ratios
 					int inSampleSize = 1;
 					if (height > reqHeight || width > reqWidth) {
 						// Calculate ratios of height and width to requested
 						// height and width
 						final int heightRatio = Math.round((float) height
 								/ (float) reqHeight);
 						final int widthRatio = Math.round((float) width
 								/ (float) reqWidth);
 						// Choose the smallest ratio as inSampleSize value, this
 						// will guarantee a final image with both dimensions
 						// larger than or equal to the requested height and
 						// width.
 						inSampleSize = heightRatio < widthRatio ? heightRatio
 								: widthRatio;
 					}
 					// load and rotate photo
 					Matrix matrix = new Matrix();
 					matrix.setRotate(rotation);
 					BitmapFactory.Options opts2 = new BitmapFactory.Options();
 					opts2.inJustDecodeBounds = false;
 					opts2.inSampleSize = inSampleSize;
 					Bitmap b = BitmapFactory.decodeFile(
 							imagePath.getAbsolutePath(), opts2);
 					b = Bitmap.createBitmap(b, 0, 0, b.getWidth(),
 							b.getHeight(), matrix, true);
 					// show photo
 					imageView.setImageBitmap(b);
 					// remove this listener
 					imageView.getViewTreeObserver().removeOnPreDrawListener(
 							this);
 					return true;
 				}
 			});
 		}
 	}
 
 	private int getImageRotation() {
 		int orientation = 1;
 		try {
 			ExifInterface exif = new ExifInterface(imagePath.getAbsolutePath());
 			orientation = exif
 					.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
 		} catch (IOException e) {
 			Log.e("GeoPhoto", "Exif Read error", e);
 		}
 		int rotationDegree = 90;
 		switch (orientation) {
 		case ExifInterface.ORIENTATION_ROTATE_90:
 			return rotationDegree;
 		case ExifInterface.ORIENTATION_ROTATE_180:
 			return rotationDegree * 2;
 		case ExifInterface.ORIENTATION_ROTATE_270:
 			return -rotationDegree;
 		default:
 			return 0;
 		}
 	}
 
 	private int getHeightRotated(int rotation, int height, int width) {
 		switch (rotation) {
 		case 90:
 		case 270:
 			return width;
 		case 0:
 		case 180:
 		default:
 			return height;
 		}
 	}
 
 	private int getWidthRotated(int rotation, int height, int width) {
 		switch (rotation) {
 		case 90:
 		case 270:
 			return height;
 		case 0:
 		case 180:
 		default:
 			return width;
 		}
 	}
 
 	private void repaintLocation() {
 		if (this.locationSearching) {
 			textLocation.setText(getString(R.string.text_location) + " "
 					+ getString(R.string.text_searching));
 		} else if (this.locationEstablished) {
 			textLocation.setText(getString(R.string.text_location) + " Lat: "
 					+ this.latitude + ", Long: " + this.longitude);
 		} else {
 			textLocation.setText(getString(R.string.text_location));
 		}
 	}
 
 	private void repaintDate() {
 		textDate.setText(getString(R.string.text_date) + " " + this.date);
 	}
 
 	private void updateGUI() {
 		// update buttons
 		if (this.sending) {
 			this.buttonPhoto.setEnabled(false);
 			this.buttonLocation.setEnabled(false);
 			this.buttonSend.setEnabled(false);
 		} else {
 			// buttonPhoto
 			if (!this.locationSearching) {
 				this.buttonPhoto.setEnabled(true);
 			} else {
 				this.buttonPhoto.setEnabled(false);
 			}
 			// buttonLocation
 			if (this.imagePath != null) {
 				this.buttonLocation.setEnabled(true);
 			} else {
 				this.buttonLocation.setEnabled(false);
 			}
 			// buttonSend
 			if (this.imagePath != null && this.locationEstablished) {
 				this.buttonSend.setEnabled(true);
 			} else {
 				this.buttonSend.setEnabled(false);
 			}
 		}
 		// update progressBar
 		if (this.sending || this.locationSearching) {
 			this.progressBar.setVisibility(ProgressBar.VISIBLE);
 			if (this.menuItemClearAll != null) {
 				this.menuItemClearAll.setEnabled(false);
 			}
 		} else {
 			this.progressBar.setVisibility(ProgressBar.INVISIBLE);
 			if (this.menuItemClearAll != null) {
 				this.menuItemClearAll.setEnabled(true);
 			}
 		}
 	}
 
 	/*
 	 * LocationListener methods
 	 */
 	@Override
 	public void onLocationChanged(Location location) {
 		Log.d("GeoPhoto", "Location retrieved");
 		this.latitude = location.getLatitude();
 		this.longitude = location.getLongitude();
 		this.locationSearching = false;
 		this.locationEstablished = true;
 		repaintLocation();
 		updateGUI();
 		this.date = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss",
 				Locale.getDefault()).format(new Date());
 		repaintDate();
 	}
 
 	@Override
 	public void onProviderDisabled(String arg0) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void onProviderEnabled(String arg0) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
 		// TODO Auto-generated method stub
 
 	}
 
 	/*
 	 * SendFragment.SendCallbacks methods
 	 */
 	@Override
 	public void onPostExecute() {
 		this.sending = false;
 		updateGUI();
 	}
 }
