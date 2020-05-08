 package gis.cityreports.android;
 
 import gis.cityreports.data.ConfigSetting;
 import gis.cityreports.data.ReportInfo;
 import gis.cityreports.utils.Utils;
 
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.StringReader;
 import java.text.SimpleDateFormat;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.List;
 import java.util.concurrent.Executors;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.ScheduledFuture;
 import java.util.concurrent.TimeUnit;
 
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 
 import org.apache.commons.lang.StringUtils;
 
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.methods.PostMethod;
 import org.apache.commons.httpclient.methods.multipart.ByteArrayPartSource;
 import org.apache.commons.httpclient.methods.multipart.FilePart;
 import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
 import org.apache.commons.httpclient.methods.multipart.Part;
 import org.apache.commons.httpclient.methods.multipart.PartBase;
 import org.apache.commons.httpclient.methods.multipart.StringPart;
 
 
 import org.apache.http.client.ResponseHandler;
 import org.apache.http.entity.mime.content.InputStreamBody;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 import org.xml.sax.XMLReader;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.ProgressDialog;
 import android.content.ActivityNotFoundException;
 import android.content.ContentResolver;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.res.Configuration;
 import android.database.Cursor;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Matrix;
 import android.graphics.Bitmap.CompressFormat;
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.hardware.SensorEventListener;
 import android.hardware.SensorManager;
 import android.location.Criteria;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.media.ExifInterface;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.net.Uri;
 import android.os.Build;
 import android.os.Bundle;
 import android.os.Debug;
 import android.os.Environment;
 import android.os.Handler;
 import android.os.Message;
 import android.provider.MediaStore;
 import android.provider.MediaStore.Images;
 import android.provider.Settings.Secure;
 import android.text.method.LinkMovementMethod;
 import android.text.util.Linkify;
 import android.util.Log;
 import android.view.Display;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.WindowManager;
 import android.view.View.OnClickListener;
 import android.view.View.OnTouchListener;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.AdapterView.OnItemSelectedListener;
 
 
 
 /**
  * @author Roy Cham
  * 
  * Copyright 2010 City of Portland. All rights reserved.
  *
  */
 public class Report extends Activity {
 
 	private static final int DIALOG_QUIT_REPORT_ID = 1;
 	private static final int DIALOG_GPS_ERROR_ID = 2;
 	private static final int DIALOG_GPS_PROGRESS_ID = 3;
 	private static final int DIALOG_CONTACT_INFO = 4;
 
 	private static final int CROSSHAIR_SELECT = 1;
 	private static final int MAP_SELECT = 2;
 	private static final int TAKE_PICTURE = 3;
 	private static final int INTERNAL_MEDIA_SELECT = 4;
 	private static final int NETWORK_SETTING = 5;
 	private static final int GPS_FINDER = 6;
 
 	private static final String GET_BEST_PROVIDER = "GET_BEST_PROVIDER";
 
 	public static final String REQUEST_CATEGORIES = "REQUEST_CATEGORIES";
 	public static final String REQUEST_SETTINGS = "REQUEST_SETTINGS";
 	public static final String REQUEST_BLACKLIST = "REQUEST_BLACKLIST";
 
 	public static final String SUBMIT_REPORT = "SUBMIT_REPORT";
 	public static final String POST_REPORT = "POST_REPORT";
 
 	public static final String CROSS_HAIR_LATITUDE = "CROSS_HAIR_LATITUDE";
 	public static final String CROSS_HAIR_LONGITUDE = "CROSS_HAIR_LONGITUDE";
 	public static final String DEVICE_ID = "device_id";
 
 	public static final boolean USING_EMULATOR = false;
 
 	public static final String android_category_refresh = "android_category_refresh";
 	public static final String android_gps_sample_count = "android_gps_sample_count";
 	public static final String android_timeout = "android_timeout";
 	public static final String disclaimer_frequency = "disclaimer_frequency";
 	public static final String disclaimer_text = "disclaimer_text";
 	public static final String gps_accuracy_threshold = "gps_accuracy_threshold";
 	public static final String gps_sample_count = "gps_sample_count";
 	public static final String gps_sample_interval = "gps_sample_interval";
 	public static final String help_page = "android_help_page";
 	public static final String item_id = "item_id";
 	public static final String max_retry_attempts = "max_retry_attempts";
 	public static final String photo_compression = "photo_compression";
 	public static final String photo_max_pixels = "photo_max_pixels";
 	public static final String sample_size_extra = "sample_size_extra";
 	public static final String sample_size_large = "sample_size_large";
 	public static final String sample_size_medium = "sample_size_medium";
 	public static final String sample_size_small = "sample_size_small";
 	public static final String status = "status";
 
 	private static final String CLASSTAG = Report.class.getSimpleName();
 	private static final String DATE_FORMAT_NOW = "EEE, d MMM yyyy HH:mm:ss Z";
 
 	private static ArrayAdapter<CategoryDetails> adapter;
 	private static ConfigSetting configSetting;
 	private static ReportInfo reportInfo;
 
 	private static long categoryTimestamp = 0;
 	private static long settingTimestamp = 0;
 	
 	public static SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
 		
 	private boolean activeNetworkState = true;
 	private boolean categoryRefreshRunning = false;
 
 	private double currentLatitude = 0;
 	private double currentLongitude = 0;
 	private double gpsLatitude;
 	private double gpsLongitude;
 	
 	private int retryAttemptsForCategory;
 	private int retryAttemptsForBlacklist;
 
 	private long currentMills;
 	private long milliDate;
 	private long timestampContact = 0;
 
 	private Button buttonSubmit;
 	private Button buttonLocationChoices;
 
 	private EditText mEditText;
 
 	private List<CategoryDetails> posts;
 
 	private LocationManager locationManagerGPS;
 
 	private ProgressDialog progressDialog;
 
 	private Spinner s;
 
 	private String errorFields = "";
 	private String imageStoreFilePath;
 	private String imageName;
 	private String gpsButtonModifyText;
 	private String gpsErrorMsgDialog;
 
 	private TextView imgPreviewTv;
 
 	private Uri tempPhotoUri;
 
 	private Utils utils = new Utils();
 
 	
 	
 	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
 
 	Runnable categoryRefresh = new Runnable() {
 		public void run() {
 
 			currentMills = System.currentTimeMillis();
 
 			if (categoryTimestamp != 0) {
 				
 				if ((currentMills - categoryTimestamp) >= configSetting.getAndroidCategoryRefresh()) {
 					
 					if (!categoryRefreshRunning) {
 						categoryRefreshRunning = true;
 						categoryTimestamp = currentMills;
 						
 						reportPostHandler.post(mCategoryInfoRefresh);
 						//runOnUiThread(mContactInfo);
 					}
 				}
 			} else {
 				categoryTimestamp = currentMills;
 			}
 		}
 	};
 
 	final ScheduledFuture<?> categoryInfoHandle = scheduler.scheduleAtFixedRate(categoryRefresh, 0, 60, TimeUnit.SECONDS);
 	
 	
 	final Handler reportPostHandler = new Handler();
 
 	SettingHandler parserSettingHandler;
 
 	/************************************************
 	 * Runnable thread for errors found before submitting report
 	 *************************************************/
 	
 	Runnable mCategoryInfoRefresh = new Runnable() {
 		public void run() {
 			try {
 				setDefaultCategory(getString(R.string.btnSpinnerRetrieving));
 				performRequest(null, null, REQUEST_CATEGORIES);	
 			} catch (Exception e) {
 				Log.e("ERROR", "error " + e.toString());
 			} finally {
 				errorFields = "";
 			}
 		}
 	};
 	
 
 	/************************************************
 	 * Runnable thread for errors found before submitting report
 	 *************************************************/
 	Runnable mReportResults = new Runnable() {
 		public void run() {
 			try {
 				disclaimerPopup("Incomplete Report\n\nThe current report requires the following before submittal:\n\n"
 						+ errorFields);
 			} catch (Exception e) {
 				Log.e("ERROR", "error " + e.toString());
 			} finally {
 				errorFields = "";
 			}
 		}
 	};
 
 	/************************************************
 	 * Runnable thread for successful report submittal
 	 * 
 	 *************************************************/
 	Runnable mReportSuccessful = new Runnable() {
 		public void run() {
 			try {
 				createNewReport();
 				disclaimerPopup(getString(R.string.reportThankYou));
 
 			} catch (Exception e) {
 				Log.e("ERROR", "error " + e.toString());
 			} finally {
 				errorFields = "";
 			}
 		}
 	};
 
 	/************************************************
 	 * Runnable thread for failed report submittal
 	 * 
 	 *************************************************/
 	Runnable mReportFailure = new Runnable() {
 		public void run() {
 			try {
 				disclaimerPopup(getString(R.string.reportError));
 			} catch (Exception e) {
 				Log.e("ERROR", "error " + e.toString());
 			} finally {
 				errorFields = "";
 			}
 		}
 	};
 
 	/************************************************
 	 * Handler for retrieval of categories returned from the
 	 * categoryResponseHandler
 	 *************************************************/
 	final Handler categoryHandler = new Handler() {
 
 		@Override
 		public void handleMessage(final Message msg) {
 
 			String bundleResult = msg.getData().getString("RESPONSE");
 			// Log.d("BUNDLE_RESULT", bundleResult);
 
 			if (!bundleResult.contains("ERROR_FOUND")) {
 
 				try {
 					SAXParserFactory spf = SAXParserFactory.newInstance();
 					SAXParser sp = spf.newSAXParser();
 					XMLReader xr = sp.getXMLReader();
 
 					CategoryHandler parserCategoryHandler = new CategoryHandler();
 
 					xr.setContentHandler(parserCategoryHandler);
 					xr.parse(new InputSource(new StringReader(bundleResult)));
 
 					posts = parserCategoryHandler.getPosts();
 
 					Collections.sort(posts, new CaseInsensitiveComparator());
 
 					adapter = null;
 					adapter = new ArrayAdapter<CategoryDetails>(getApplicationContext(),R.layout.my_simple_spinner_dropdown_item);
 					adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 
 					CategoryDetails defaultSelection = new CategoryDetails();
 					defaultSelection.setCategory_id("");
 					defaultSelection.setIphone_input_alias(getString(R.string.btnReportType));
 					adapter.add(defaultSelection);
 
 					int categoryPosition = 0;
 
 					// Log.d("past category id", reportInfo.getCategoryId());
 
 					if (posts != null) {
 						for (CategoryDetails p : posts) {
 							adapter.add(p);
 
 							if (p.getCategory_id().compareToIgnoreCase(reportInfo.getCategoryId()) == 0) {
 								categoryPosition = adapter.getPosition(p);
 							}
 
 						}
 						s.setAdapter(adapter);
 						s.setSelection(categoryPosition);
 						s.setPrompt(getString(R.string.btnSpinnerDefault));
 					} else {
 						setDefaultCategory(getString(R.string.btnSpinnerError));
 					}
 
 				} catch (IOException ioe) {
 					Log.e("IOException", ioe.toString());
 				} catch (ParserConfigurationException e) {
 					// TODO Auto-generated catch block
 					Log.e("ParserConfigurationException", e.toString());
 				} catch (SAXException e) {
 					// TODO Auto-generated catch block
 					Log.e("SAXException", e.toString());
 				} catch (Exception e) {
 					Log.e("ERROR", e.toString());
 				} finally {
 					
 				}
 
 			} else {
 				retryAttemptsForCategory++;
 
 				if (retryAttemptsForCategory >= configSetting.getMaxRetryAttempts()) {
 					setDefaultCategory(getString(R.string.btnSpinnerError));
 					retryAttemptsForCategory = 0;
 				} else {
 					performRequest(null, null, REQUEST_CATEGORIES);
 				}
 
 			}
 			categoryRefreshRunning = false;
 		}
 	};
 
 	private ResponseHandler<String> categoryResponseHandler = HTTPRequestHelper.getResponseHandlerInstance(categoryHandler);
 
 	/************************************************
 	 * Handler for retrieval of preferred settings returned from the
 	 * responseSettingHandler
 	 *************************************************/
 
 	final Handler preferredSettingHandler = new Handler() {
 
 		@Override
 		public void handleMessage(final Message msg) {
 
 			String bundleResult = msg.getData().getString("RESPONSE");
 			// Log.d("BUNDLE_RESULT", bundleResult);
 
 			if (!bundleResult.contains("ERROR_FOUND")) {
 
 				try {
 					SAXParserFactory spf2 = SAXParserFactory.newInstance();
 					SAXParser sp2 = spf2.newSAXParser();
 					XMLReader xr2 = sp2.getXMLReader();
 
 					xr2.setContentHandler(parserSettingHandler);
 					xr2.parse(new InputSource(new StringReader(bundleResult)));
 
 				} catch (IOException ioe) {
 					Log.e("IOException", ioe.toString());
 				} catch (ParserConfigurationException e) {
 					// TODO Auto-generated catch block
 					Log.e("ParserConfigurationException", e.toString());
 				} catch (SAXException e) {
 					// TODO Auto-generated catch block
 					Log.e("SAXException", e.toString());
 				} catch (Exception e) {
 					Log.e("ERROR", e.toString());
 				} finally {
 					
 				}
 				
 				if (ApplicationState.showDisclaimer()) {
 					disclaimerPopup(configSetting.getDisclaimerText());
 					ApplicationState.disclaimerStatus(false);
 				}
 
 			} else {
 
 				disclaimerPopup(configSetting.getDisclaimerText());
 
 			}
 		}
 	};
 
 	private ResponseHandler<String> responseSettingHandler = HTTPRequestHelper.getResponseHandlerInstance(preferredSettingHandler);
 
 	/************************************************
 	 * Handler for retrieval of blacklist returned from the
 	 * responseBlackListHandler
 	 *************************************************/
 
 	final Handler blackListHandler = new Handler() {
 
 		@Override
 		public void handleMessage(final Message msg) {
 
 			String bundleResult = msg.getData().getString("RESPONSE");
 			// Log.d("BUNDLE_RESULT", bundleResult);
 
 			if (!bundleResult.contains("ERROR_FOUND")) {
 
 				try {
 					SAXParserFactory spf = SAXParserFactory.newInstance();
 					SAXParser sp = spf.newSAXParser();
 					XMLReader xr = sp.getXMLReader();
 
 					BlackListHandler parserBlackListHandler = new BlackListHandler();
 
 					xr.setContentHandler(parserBlackListHandler);
 					xr.parse(new InputSource(new StringReader(bundleResult)));
 
 					if (parserBlackListHandler.isBlackListed()) {
 						buttonSubmit.setBackgroundResource(R.drawable.denied_button);
 						buttonSubmit.setClickable(false);
 					}
 
 				} catch (IOException ioe) {
 					Log.e("IOException", ioe.toString());
 				} catch (ParserConfigurationException e) {
 					// TODO Auto-generated catch block
 					Log.e("ParserConfigurationException", e.toString());
 				} catch (SAXException e) {
 					// TODO Auto-generated catch block
 					Log.e("SAXException", e.toString());
 				} catch (Exception e) {
 					Log.e("ERROR", e.toString());
 				} finally {
 					
 				}
 
 			} else {
 				retryAttemptsForBlacklist++;
 
 				if (retryAttemptsForBlacklist >= configSetting.getMaxRetryAttempts()) {
 					retryAttemptsForBlacklist = 0;
 				} else {
 					performRequest(null, null, REQUEST_BLACKLIST);
 				}
 
 			}
 		}
 	};
 
 	private ResponseHandler<String> responseBlackListHandler = HTTPRequestHelper.getResponseHandlerInstance(blackListHandler);
 
 
 	private void startGPSService() {
 		LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
 
 		if (manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
 
 			locationManagerGPS = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
 			
 			
 		    Criteria criteria = new Criteria();
 		    criteria.setAccuracy(Criteria.ACCURACY_COARSE);
 		    criteria.setAltitudeRequired(false);
 		    criteria.setBearingRequired(false);
 		    criteria.setSpeedRequired(false);
 		    criteria.setCostAllowed(true);
 		    criteria.setPowerRequirement(Criteria.POWER_HIGH);
 		    
 		    String provider = locationManagerGPS.getBestProvider(criteria, true);
 		    
 		    locationManagerGPS.requestLocationUpdates(provider, 0, 0, locationListenerGPS);
 		    		    
 		    //locationManagerGPS.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListenerGPS);
 		    
 		} else {
 			currentLatitude = 0;
 			currentLongitude = 0;
 			// Notify users and show settings if they want to enable GPS
 		}
 		
 	}
 
 	private void stopGPSService() {
 		if (locationManagerGPS != null) {
 			locationManagerGPS.removeUpdates(locationListenerGPS);
 			locationManagerGPS = null;
 		}
 		
 		//ApplicationState.stopListening();
 	}
 
 
 	private LocationListener locationListenerGPS = new LocationListener() {
 		public void onLocationChanged(Location location) {
 			updateWithNewLocation(location);
 		}
 
 		public void onProviderDisabled(String provider) {
 			updateWithNewLocation(null);
 		}
 
 		public void onProviderEnabled(String provider) {
 		}
 
 		public void onStatusChanged(String provider, int status, Bundle extras) {
 		}
 	};
 
 	protected void updateWithNewLocation(Location location) {
 
 		String locationDetail = "";
 		locationDetail += sdf.format(location.getTime()) + "\n";
 		locationDetail += "\tLatitude:  " + location.getLatitude() + "\n";
 		locationDetail += "\tLongitude: " + location.getLongitude() + "\n";
 		locationDetail += "\tAccuracy:  " + location.getAccuracy() + "\n";
 		locationDetail += "GPS\n";
 
 		currentLatitude = location.getLatitude();
 		currentLongitude = location.getLongitude();
 		
 		//Log.d("PDX GPS", locationDetail);
 				
 
 	}
 
 	private void setDefaultCategory(String message) {
 		if (s != null) {
 			CategoryDetails defaultSelection = new CategoryDetails();
 			defaultSelection.setCategory_id("");
 			defaultSelection.setIphone_input_alias(message);
 
 			adapter = new ArrayAdapter<CategoryDetails>(this, R.layout.my_simple_spinner_dropdown_item);
 
 			adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
 			adapter.add(defaultSelection);
 
 			s.setAdapter(adapter);
 			s.setOnItemSelectedListener(new MyOnItemSelectedListener());
 		}
 	}
 	
 	
 	private SensorEventListener listener = new SensorEventListener() {
 		public volatile float direction = (float) 0;
 		
 		@Override
 		public void onAccuracyChanged(Sensor sensor, int accuracy) {
 			// TODO Auto-generated method stub
 			
 		}
 
 		@Override
 		public void onSensorChanged(SensorEvent event) {
 			// TODO Auto-generated method stub
 			float vals[] = event.values;   
 		    direction = vals[0];
 		    Log.d("DIRECTION", "sensorChanged (" + Float.toString(direction) + ")");
 		}
 	};
 	
 	public static SensorManager sensorMan;
 		
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
 		int width = display.getWidth();
 		int height = display.getHeight();

		setContentView(R.layout.main_480);
 
 		configSetting = ((ApplicationState) getApplication()).getConfigSetting();
 		reportInfo = ((ApplicationState) getApplication()).getReportInfoState();
 
 
 		ApplicationState.setupDefaults();
 
 		//
 		
 //		sensorMan = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
 //		sensorMan.registerListener(listener, sensorMan.getDefaultSensor(SensorManager.SENSOR_ORIENTATION), SensorManager.SENSOR_DELAY_FASTEST);
 		//sensorMan.unregisterListener(listener);
 		
 		//
 		String android_id = null;
 
 		boolean validDevice = false;
 				
 		try {
 			android_id = Secure.getString(getContentResolver(),	android.provider.Settings.Secure.ANDROID_ID);
 
 			if (!USING_EMULATOR) {
 
 				if (StringUtils.isNotBlank(android_id)) {
 					StringBuffer deviceInfo = new StringBuffer();
 					deviceInfo.append(android_id);
 					// deviceInfo.append("_");
 					// deviceInfo.append(Build.BRAND);
 					deviceInfo.append("_");
 					deviceInfo.append(Build.MANUFACTURER);
 					deviceInfo.append("_");
 					deviceInfo.append(Build.MODEL);
 					// deviceInfo.append("_");
 					// deviceInfo.append(Build.DISPLAY);
 					// Log.d("ANDROID_ID", deviceInfo.toString());
 					reportInfo.setDeviceId(deviceInfo.toString());
 					validDevice = true;
 				}
 			} else {
 				android_id = getString(R.string.android_emulator);
 				reportInfo.setDeviceId(android_id);
 				validDevice = true;
 			}
 
 		} catch (Exception ex) {
 			android_id = "";
 			validDevice = false;
 		}
 
 		if (StringUtils.isBlank(android_id)) {
 
 			AlertDialog.Builder builder = new AlertDialog.Builder(this);
 			builder.setMessage(getString(R.string.unknownDevice))
 					.setCancelable(false).setPositiveButton("Close",
 							new DialogInterface.OnClickListener() {
 								public void onClick(DialogInterface dialog,	int id) {
 									Report.this.finish();
 								}
 							});
 
 			AlertDialog alert = builder.create();
 			alert.show();
 		}
 
 		parserSettingHandler = new SettingHandler(configSetting);
 
 		if (validDevice) {
 
 			// Let's NOT do this on the main thread and prevent any ANR due to network connectivity delays or response times
 			final HashMap<String, String> postParams = new HashMap<String, String>();
 
 			final HTTPRequestHelper helper = new HTTPRequestHelper(responseSettingHandler);
 
 			postParams.put(getString(R.string.param1), getString(R.string.pval));
 			postParams.put(getString(R.string.param2), reportInfo.getDeviceId());
 
 			new Thread() {
 
 				@Override
 				public void run() {
 					helper.performPost(
 									getString(R.string.postRequestSettings_PROD),
 									"",
 									"",
 									Integer.parseInt(getString(R.string.httpTimeOutMilliseconds)),
 									Integer.parseInt(getString(R.string.socketTimeoutMilliseconds)),
 									null, postParams);
 						}
 			}.start();
 			
 			performRequest(null, null, REQUEST_BLACKLIST);
 			// performRequest(null, null, REQUEST_SETTINGS);
 		}
 		
 		imgPreviewTv = (TextView) findViewById(R.id.imgPreviewTextView);
 
 		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
 
 		mEditText = (EditText) findViewById(R.id.editTxtComments);
 
 		mEditText.setText(reportInfo.getDescription());
 
 		mEditText.setOnTouchListener(new OnTouchListener() {
 			@Override
 			public boolean onTouch(View v, MotionEvent event) {
				Intent i = new Intent(Report.this, Comments.class);
				startActivity(i);
 				return true;
 			}
 		});
 
 		Button btnCancel = (Button) findViewById(R.id.btnCancelMain);
 		btnCancel.setOnClickListener(mCancelReport);
 
 		s = (Spinner) findViewById(R.id.spinner);
 
 		if (validDevice) {
 			setDefaultCategory(getString(R.string.btnSpinnerRetrieving));
 		}
 
 		gpsButtonModifyText = getString(R.string.gpsFoundButtonDialog);
 		gpsErrorMsgDialog = getString(R.string.gpsErrorMsgDialog);
 
 		buttonSubmit = (Button) findViewById(R.id.btnSubmitMain);
 		buttonSubmit.setOnClickListener(mSubmitReport);
 
 		buttonLocationChoices = (Button) findViewById(R.id.btnLocationChoices);
 		buttonLocationChoices.setOnClickListener(mLocationListener);
 
 		if (reportInfo.isValidGPSLocation())
 			buttonLocationChoices.setText(gpsButtonModifyText);
 
 		Button buttonSelectInternalImg = (Button) findViewById(R.id.btnSelectInternalImage);
 		buttonSelectInternalImg.setOnClickListener(mPhotoSelectionListener);
 
 		ImageView mTakePhoto = (ImageView) findViewById(R.id.imgPreview);
 		mTakePhoto.setOnClickListener(mViewPhoto);
 
 
 		if (validDevice) {
 			performRequest(null, null, REQUEST_CATEGORIES);
 		}
 		// Load failed report settings
 		if (StringUtils.isNotBlank(reportInfo.getImageFullUriPath())) {
 
 			if (StringUtils.contains(reportInfo.getImageFullUriPath(), "file://")) {
 				Uri tempUri = Uri.parse(reportInfo.getImageFullUriPath());
 				setExternalPhoto(tempUri, reportInfo.getImageAbsolutePath(), reportInfo.getImageName());
 			} else {
 				Uri savedUri = Uri.parse(reportInfo.getImageFullUriPath());
 				resizeImageForBitmap(savedUri);
 			}
 		}
 
 		mTakePhoto.requestFocus();
 
 	}
 
 	public void hideKeyBoard(EditText et) {
 		InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
 		imm.hideSoftInputFromWindow(et.getWindowToken(),
 				InputMethodManager.RESULT_HIDDEN);
 	}
 
 	private OnClickListener mSubmitReport = new OnClickListener() {
 		public void onClick(View v) {
 						
 			long currentMills = System.currentTimeMillis();
 			long diff = currentMills - timestampContact;
 						
 			if (!reportInfo.hasContact())
 			{				
 				if ( (timestampContact == 0) || (diff >= 86400000) ) {
 					contactInfoDialog();
 					timestampContact = currentMills;
 					return;
 				}
 			}
 			
 			performRequest(null, null, POST_REPORT);
 			
 		}
 	};
 
 	private OnClickListener mCancelReport = new OnClickListener() {
 		public void onClick(View v) {
 			showDialog(DIALOG_QUIT_REPORT_ID);
 		}
 	};
 
 	private OnClickListener mViewPhoto = new OnClickListener() {
 		public void onClick(View v) {
 
 			if (reportInfo.getImageUri() != null) {
 
 				Intent intent = new Intent(android.content.Intent.ACTION_VIEW);
 				intent.setDataAndType(reportInfo.getImageUri(), "image/jpeg");
 				startActivity(intent);
 
 			}
 		}
 	};
 	
 	
 	private OnClickListener mLocationListener = new OnClickListener() {
 		public void onClick(View v) {
 
 			try {
 				Intent i = new Intent(Report.this, LocationFinder.class);
 				
 				if (reportInfo.getLatitude() != 0 && reportInfo.getLongitude() != 0) {
 					i.putExtra(CROSS_HAIR_LATITUDE, reportInfo.getLatitude());
 					i.putExtra(CROSS_HAIR_LONGITUDE, reportInfo.getLongitude());
 				} else {
 					/*
 					if(currentLatitude != 0 && currentLongitude != 0 ) {
 						i.putExtra(CROSS_HAIR_LATITUDE, currentLatitude);
 						i.putExtra(CROSS_HAIR_LONGITUDE, currentLongitude);
 					}
 					*/
 				}
 			
 				startActivityForResult(i, CROSSHAIR_SELECT);
 				
 			} catch (ActivityNotFoundException aex) {
 				Log.e("ActivityNotFoundException", aex.toString());
 			} catch (Exception ex) {
 				Log.e("Exception", ex.toString());
 			}
 
 		}
 
 	};
 
 
 	private OnClickListener mPhotoSelectionListener = new OnClickListener() {
 		public void onClick(View v) {
 
 			final Dialog d = new Dialog(Report.this);
 			d.setTitle("Photo Options");
 			d.setCancelable(true);
 			d.setContentView(R.layout.photo_dialog_view);
 
 			Button btnTakePhoto = (Button) d.findViewById(R.id.btnTakePhoto);
 
 			btnTakePhoto.setOnClickListener(new OnClickListener() {
 				@Override
 				public void onClick(View v) {
 					getThumbailPicture();
 					d.dismiss();
 				}
 			});
 
 			Button btnChooseFromLib = (Button) d
 					.findViewById(R.id.btnChooseFromLib);
 
 			btnChooseFromLib.setOnClickListener(new OnClickListener() {
 				@Override
 				public void onClick(View v) {
 					selectExternalStorageImage();
 					d.dismiss();
 				}
 			});
 
 			Button btnNoPhoto = (Button) d.findViewById(R.id.btnNoPhoto);
 			btnNoPhoto.setOnClickListener(new OnClickListener() {
 				@Override
 				public void onClick(View v) {
 					imgPreviewTv.setVisibility(android.view.View.VISIBLE);
 					ImageView mTakePhoto = (ImageView) findViewById(R.id.imgPreview);
 					mTakePhoto.setImageBitmap(null);
 					reportInfo.clearImageSetting();
 					d.dismiss();
 				}
 			});
 
 			Button btnPhotoCancel = (Button) d.findViewById(R.id.btnPhotoCancel);
 			btnPhotoCancel.setOnClickListener(new OnClickListener() {
 				@Override
 				public void onClick(View v) {
 					d.dismiss();
 				}
 			});
 
 			d.show();
 
 		}
 
 	};
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.app.Activity#onActivityResult(int, int,
 	 * android.content.Intent) Callback to LoctionFinder where any saved
 	 * crosshair location is returned
 	 */
 
 	private void storeGPSInfo(double latitude, double longitude) {
 		if (latitude != 0 && longitude != 0) {
 			gpsLatitude = latitude;
 			gpsLongitude = longitude;
 
 			reportInfo.setLatitude(gpsLatitude);
 			reportInfo.setLongitude(gpsLongitude);
 
 			reportInfo.setValidGPSLocation(true);
 
 			buttonLocationChoices.setText(gpsButtonModifyText);
 
 		}
 	}
 
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		switch (requestCode) {
 		case GPS_FINDER:
 			if (resultCode == RESULT_OK) {
 				double gpsLatitude = data.getDoubleExtra("GPS_FINDER_LATITUDE",	0);
 				double gpsLongitude = data.getDoubleExtra("GPS_FINDER_LONGITUDE", 0);
 			
 				boolean isValidGPS = data.getBooleanExtra("IS_VALID_GPS", false);
 
 				if (isValidGPS) {
 
 					boolean result = utils.locationIsInside(gpsLatitude, gpsLongitude);
 					if (!result) {
 						reportInfo.setValidGPSLocation(false);
 						reportInfo.setLatitude(0);
 						reportInfo.setLongitude(0);
 						
 						disclaimerPopup(getString(R.string.invalidLocation));
 						
 						return;
 					} else {
 						storeGPSInfo(gpsLatitude, gpsLongitude);
 					}
 
 				} else {
 					buttonLocationChoices.setText(getString(R.string.btnReportLocation));
 //					showDialog(DIALOG_GPS_ERROR_ID);
 				}
 				
 				Intent i = new Intent(Report.this, LocationFinder.class);
 				i.putExtra(CROSS_HAIR_LATITUDE, gpsLatitude);
 				i.putExtra(CROSS_HAIR_LONGITUDE, gpsLongitude);
 
 				try {
 					startActivityForResult(i, CROSSHAIR_SELECT);
 				} catch (ActivityNotFoundException aex) {
 					Log.e("ActivityNotFoundException", aex.toString());
 				} catch (Exception ex) {
 					Log.e("Exception", ex.toString());
 				}
 
 				break;
 			}
 		case CROSSHAIR_SELECT:
 			if (resultCode == RESULT_OK) {
 				double crossHairLatitude = data.getDoubleExtra(CROSS_HAIR_LATITUDE, 0);
 				double crossHairLongitude = data.getDoubleExtra(CROSS_HAIR_LONGITUDE, 0);
 
 				storeGPSInfo(crossHairLatitude, crossHairLongitude);
 				break;
 			}
 		case MAP_SELECT:
 			if (resultCode == RESULT_OK) {
 				String name = data.getStringExtra("mapCoordinates");
 				break;
 			}
 		case INTERNAL_MEDIA_SELECT:
 			if (resultCode == Activity.RESULT_OK) {
 				libraryIntentData(data);
 				break;
 			}
 		case TAKE_PICTURE:
 			if (resultCode == Activity.RESULT_OK) {
 				resizeImageIntentData(data);
 				break;
 			}
 		case NETWORK_SETTING:
 			if (resultCode == Activity.RESULT_OK) {
 				break;
 			}
 		}
 
 	}
 
 	private void getThumbailPicture() {
 
 		System.gc();
 
 		// Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
 		Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
 		// Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
 
 		milliDate = System.currentTimeMillis();
 
 		String state = Environment.getExternalStorageState();
 
 		boolean directoryExists = false;
 
 		if (Environment.MEDIA_MOUNTED.equals(state)) {
 
 			File directory = new File(Environment.getExternalStorageDirectory()
 					+ "/Android/data/gis.cityreports.android/files/pictures");
 
 			if (!directory.exists()) {
 				try {
 					if (directory.mkdirs()) {
 						directoryExists = true;
 					} else {
 						// Toast.makeText(getApplicationContext(), "directory NOT make...", Toast.LENGTH_LONG).show();
 					}
 				} catch (Exception ex) {
 					// Log.e("ERROR FOUND!", ex.toString());
 				}
 			} else {
 				directoryExists = true;
 			}
 
 			if (directoryExists) {
 
 				imageName = "pic-" + System.currentTimeMillis() + ".jpg";
 
 				imageStoreFilePath = Environment.getExternalStorageDirectory()
 						+ "/Android/data/gis.cityreports.android/files/pictures"
 						+ File.separator + imageName;
 
 				tempPhotoUri = Uri.fromFile(new File(imageStoreFilePath));
 				// Log.d("photoUri", tempPhotoUri.toString());
 
 				intent.putExtra(MediaStore.EXTRA_OUTPUT, tempPhotoUri);
 			}
 
 		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
 			// We can only read the media
 		} else {
 			// Something else is wrong. It may be one of many other states, but
 			// all we need
 			// to know is we can neither read nor write
 		}
 
 		startActivityForResult(intent, TAKE_PICTURE);
 	}
 
 	public String getRealPathFromURI(Uri contentUri)
 			throws IllegalArgumentException, FileNotFoundException {
 		// can post image
 		String[] proj = { MediaStore.Images.Media.DATA };
 
 		// int imageResource = getResources().getIdentifier(contentUri.to, null,
 		// getPackageName());
 		// Drawable image = getResources().getDrawable(imageResource);
 
 		Cursor cursor = managedQuery(contentUri, proj, // Which columns to
 														// return
 				null, // WHERE clause; which rows to return (all rows)
 				null, // WHERE clause selection arguments (none)
 				null); // Order-by clause (ascending by name)
 
 		int column_index = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
 		// int column_index =
 		// cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
 
 		if (cursor != null && cursor.getCount() > 0) {
 			if (column_index != -1) {
 				cursor.moveToFirst();
 
 				String value = null;
 				try {
 					value = cursor.getString(column_index);
 				} catch (Exception ex) {}
 				return value;
 			}
 		}
 
 		return null;
 	}
 
 	private void libraryIntentData(Intent data) {
 
 		if (data != null) {
 			Uri internalImageUri = data.getData();
 
 			resizeImageForBitmap(internalImageUri);
 
 		}
 	}
 
 	private void resizeImageIntentData(Intent data) {
 
 		if (data != null) {
 
 			Bitmap bmp = null;
 
 			if (data.hasExtra("data")) {
 				// Log.d("Found intent data", "here");
 				bmp = (Bitmap) data.getExtras().get("data");
 			}
 
 			// Bundle extras = data.getExtras();
 			// Bitmap bmp = (Bitmap)extras.get("data") ;
 
 			// String result2 = data.toUri(0);
 
 			// **********************************************************************************************
 			// If we have a bitmap return, then according to the google
 			// documentation, we did not send any
 			// "EXTRA_OUTPUT" extended data with the camera intent.
 			// As I find out, this is not true with all device manufacturers
 			// **********************************************************************************************
 			if (bmp != null) {
 
 				Uri internalImageUri = data.getData();
 
 				// *******************************************************************
 				// There shouldn't be a need to check for this, but there seems
 				// to be a
 				// issue with the camera intent not working as documented
 				// *******************************************************************
 				if (internalImageUri == null && tempPhotoUri != null)
 					setExternalPhoto(tempPhotoUri, imageStoreFilePath, imageName);
 				else
 					resizeImageForBitmap(internalImageUri);
 				// *******************************************************************
 
 			} else {
 				setExternalPhoto(tempPhotoUri, imageStoreFilePath, imageName);
 			}
 
 			bmp.recycle();
 			bmp = null;
 
 		} else {
 			// TODO Do something with the full image stored in outputFileUri
 			// Check to see if data is empty because it got saved to the
 			// internal storage
 			// or if got saved to the SD card location specificed in the Intent
 			// parameter
 
 			if (tempPhotoUri != null
 					&& StringUtils.isNotBlank(imageStoreFilePath)
 					&& StringUtils.isNotBlank(imageName))
 				setExternalPhoto(tempPhotoUri, imageStoreFilePath, imageName);
 			else
 				resizeImageForBitmap(findImageUri());
 		}
 
 	}
 
 	private void setExternalPhoto(Uri tempUri, String tempImagePath,
 			String tempImageName) {
 
 		InputStream photoStream = null;
 		Bitmap photoBitmap = null;
 
 		try {
 			int photoSizeThreshold = Integer.parseInt(getString(R.string.photoSizeThreshold));
 
 			byte[] imageByteArray = null;
 			File myfile = new File(tempImagePath);
 
 			if (myfile.exists()) {
 				imageByteArray = getImageBytes(myfile);
 				int imgSize = imageByteArray.length;
 
 				photoStream = getContentResolver().openInputStream(tempUri);
 
 				BitmapFactory.Options options = new BitmapFactory.Options();
 
 				if (imgSize > 0) {
 					imgSize = imgSize / 1000;
 
 					if (imgSize <= 150) {
 						// needToResize = false;
 						options.inSampleSize = 4;
 					} else if (imgSize <= 500) {
 						options.inSampleSize = 4;
 					} else if (imgSize <= photoSizeThreshold) {
 						options.inSampleSize = 8;
 					} else {
 						options.inSampleSize = 10;
 					}
 
 					photoBitmap = BitmapFactory.decodeStream(photoStream, null, options);
 					int width = photoBitmap.getWidth();
 					int height = photoBitmap.getHeight();
 
 					Matrix mat = new Matrix();
 
 					ExifInterface exif = new ExifInterface(tempUri.getPath());
 					String tagOrientation = exif.getAttribute(ExifInterface.TAG_ORIENTATION);
 
 					if (StringUtils.isNotBlank(tagOrientation) && Integer.parseInt(tagOrientation) >= 6)
 						mat.postRotate(90);
 
 					reportInfo.setImageUri(tempUri);
 					reportInfo.setImageAbsolutePath(tempImagePath);
 					reportInfo.setImageFullUriPath(tempUri.toString());
 					reportInfo.setImageName(tempImageName);
 
 					imgPreviewTv.setVisibility(android.view.View.INVISIBLE);
 
 					ImageView mTakePhoto = (ImageView) findViewById(R.id.imgPreview);
 					mTakePhoto.setImageBitmap(Bitmap.createBitmap(photoBitmap, 0, 0, width, height, mat, true));
 
 					photoStream.close();
 					photoStream = null;
 
 				}
 			}
 
 		} catch (Exception e) {
 			
 			// Log.e("ERROR", e.toString());
 			//e.printStackTrace();
 			
 			//Notify user to manually select the photo.
 			View customDialogView = View.inflate(this, R.layout.custom_dialog, null); 
 	        TextView customTextView = (TextView) customDialogView.findViewById(R.id.customDialogText); 
 	      	
 	      	customTextView.setText("Oops, we were unable to create an image preview. Please reselect your photo by clicking the button below."); 
 	      	customTextView.setMovementMethod(LinkMovementMethod.getInstance()); 
 			
 			AlertDialog.Builder builder = new AlertDialog.Builder(this);
 			builder.setView(customDialogView)
 					.setCancelable(false)
 					.setPositiveButton("Reselect", new DialogInterface.OnClickListener() {
 			           public void onClick(DialogInterface dialog, int id) {
 			        	   selectExternalStorageImage();
 			        	   dialog.dismiss();
 			           }
 			       })
 			       .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
 			           public void onClick(DialogInterface dialog, int id) {
 			                dialog.cancel();
 			           }
 			       });
 			AlertDialog alert = builder.create();
 			alert.show();
 			
 		} finally {
 
 			if (photoStream != null) {
 
 				try {
 					photoStream.close();
 				} catch (IOException e) {
 					// Log.e("photoStream ERROR", e.toString());
 					e.printStackTrace();
 				}
 				photoStream = null;
 			}
 
 			tempPhotoUri = null;
 			imageStoreFilePath = "";
 			imageName = "";
 
 			if (photoBitmap != null) {
 				// photoBitmap.recycle();
 				// photoBitmap = null;
 			}
 
 			System.gc();
 		}
 	}
 
 	/*
 	 * TODO return the URI instead of boolean and have the main thread check the
 	 * image size but for now keep it this way for testing
 	 */
 	private Uri findImageUri() {
 
 		Uri contenturi = null;
 
 		try {
 			// Environment.getDataDirectory()
 			contenturi = Images.Media.getContentUri("phoneStorage");
 
 			if (contenturi != null) {
 
 				String[] projection = new String[] {
 						MediaStore.Images.Media._ID,
 						MediaStore.Images.Media.DISPLAY_NAME,
 						MediaStore.Images.Media.DATE_ADDED,
 						MediaStore.Images.Media.MIME_TYPE,
 						MediaStore.Images.Media.SIZE };
 
 				// Log.d("milliDate", Long.toString(milliDate));
 				milliDate = milliDate / 1000;
 
 				String value = "";
 				int imgSize = 0;
 
 				Cursor imgCursor = managedQuery(contenturi, projection,
 						MediaStore.Images.Media.DATE_ADDED + " > " + milliDate,
 						null, null);
 
 				if (imgCursor != null && imgCursor.getCount() > 0) {
 					imgCursor.moveToFirst();
 
 					while (imgCursor.isAfterLast() == false) {
 						value = imgCursor.getString(0);
 						imgSize = imgCursor.getInt(4);
 						imgCursor.moveToNext();
 					}
 				}
 
 				Uri test = Uri.parse(contenturi.toString() + File.separator	+ value);
 
 				contenturi = test;
 
 			}
 
 		} catch (Exception ex) {
 			Log.e("ERROR", ex.toString());
 		} finally {
 
 		}
 
 		return contenturi;
 
 	}
 
 	private void resizeImageForBitmap(Uri internalImageUri) {
 
 		boolean needToResize = false;
 		Uri imageUri = internalImageUri;
 
 		String realPathImage = null;
 		Bitmap photoBitmap = null;
 		InputStream photoStream = null;
 
 		try {
 			if (imageUri != null) {
 
 				int photoSizeThreshold = Integer.parseInt(getString(R.string.photoSizeThreshold));
 				long size = 0;
 				String imgName = "";
 				ExifInterface exif = null;
 
 				String tagOrientation;
 				String tagWidth;
 				String tagHeight;
 				String tagMake;
 				String tagModel;
 				String tagLatitude;
 				String tagLatitudeRef;
 				String tagLongitude;
 				String tagLongRef;
 				String tagDatetime;
 
 				Matrix mat = new Matrix();
 
 				String testpath = imageUri.getPath();
 				String lasttpath = imageUri.getLastPathSegment();
 
 				BitmapFactory.Options options = new BitmapFactory.Options();
 
 				if (StringUtils.contains(imageUri.toString(), "file://")) {
 					// bypass check
 					realPathImage = imageUri.toString();
 
 					// Need to extract file name
 					imgName = lasttpath;
 
 					byte[] imageByteArray = null;
 					File myfile = new File(testpath);
 
 					imageByteArray = getImageBytes(myfile);
 					size = imageByteArray.length;
 					
 					exif = new ExifInterface(testpath);
 					tagOrientation = exif.getAttribute(ExifInterface.TAG_ORIENTATION);
 
 					if (StringUtils.isNotBlank(tagOrientation) && Integer.parseInt(tagOrientation) >= 6)
 						mat.postRotate(90);
 
 				} else {
 					realPathImage = getRealPathFromURI(imageUri);
 
 					if (realPathImage != null)
 						exif = new ExifInterface(realPathImage);
 
 					ContentResolver contentResolver = getContentResolver();
 					Cursor c = contentResolver
 							.query(imageUri, new String[] {
 									Images.ImageColumns.SIZE,
 									Images.ImageColumns.DISPLAY_NAME,
 									Images.ImageColumns.ORIENTATION }, null,
 									null, null);
 
 					String imgOrientation = "0";
 
 
 					while (c.moveToNext()) {
 						size = c.getLong(0);
 						imgName = c.getString(1);
 						imgOrientation = c.getString(2);
 					}
 					c.close();
 
 					if (StringUtils.isNotBlank(imgOrientation))
 						mat.postRotate(Integer.parseInt(imgOrientation));
 
 				}
 
 				if (realPathImage != null) {
 					tagOrientation = exif.getAttribute(ExifInterface.TAG_ORIENTATION);
 					tagWidth = exif.getAttribute(ExifInterface.TAG_IMAGE_WIDTH);
 					tagHeight = exif.getAttribute(ExifInterface.TAG_IMAGE_LENGTH);
 
 					// Modified 9-18-2010
 					if (StringUtils.isNotBlank(tagWidth) && StringUtils.isNumeric(tagWidth)) {
 						int imgWidth = Integer.parseInt(tagWidth.trim());
 						if (imgWidth > 0) {
 							reportInfo.setImageWidth(imgWidth);
 						}
 					}
 
 					if (StringUtils.isNotBlank(tagHeight) && StringUtils.isNumeric(tagHeight)) {
 						int imgHeight = Integer.parseInt(tagHeight.trim());
 						if (imgHeight > 0) {
 							reportInfo.setImageHeight(imgHeight);
 						}
 					}
 
 					tagMake = exif.getAttribute(ExifInterface.TAG_MAKE);
 					tagModel = exif.getAttribute(ExifInterface.TAG_MODEL);
 					tagLatitude = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE);
 					tagLatitudeRef = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF);
 					tagLongitude = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);
 					tagLongRef = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF);
 					tagDatetime = exif.getAttribute(ExifInterface.TAG_DATETIME);
 				}
 
 				if (realPathImage != null) {
 
 					if (size > 0) {
 						size = size / 1000;
 
 						if (size <= 150) {
 							needToResize = false;
 							options.inSampleSize = 4;
 						} else if (size <= 500) {
 							options.inSampleSize = 4;
 						} else if (size <= photoSizeThreshold) {
 							options.inSampleSize = 8;
 						} else {
 							options.inSampleSize = 10;
 						}
 
 					}
 
 					photoStream = getContentResolver().openInputStream(imageUri);
 
 					photoBitmap = BitmapFactory.decodeStream(photoStream, null,	options);
 					int width = photoBitmap.getWidth();
 					int height = photoBitmap.getHeight();
 
 					Bitmap bMapRotate = Bitmap.createBitmap(photoBitmap, 0, 0, width, height, mat, true);
 
 					reportInfo.setImageUri(internalImageUri);
 					reportInfo.setImageAbsolutePath(realPathImage);
 					reportInfo.setImageFullUriPath(internalImageUri.toString());
 					reportInfo.setImageName(imgName);
 
 					// photoBitmap.recycle();
 					// System.gc();
 
 					imgPreviewTv.setVisibility(android.view.View.INVISIBLE);
 
 					ImageView mTakePhoto = (ImageView) findViewById(R.id.imgPreview);
 					mTakePhoto.setImageBitmap(bMapRotate);
 
 					photoStream.close();
 					photoStream = null;
 
 				} else {
 					reportInfo.clearImageSetting();
 				}
 
 			}
 
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			Log.e("FileNotFoundException", e.toString());
 			reportInfo.clearImageSetting();
 		} catch (IllegalArgumentException ile) {
 			// TODO Auto-generated catch block
 			ile.printStackTrace();
 			Log.e("FileNotFoundException", ile.toString());
 			reportInfo.clearImageSetting();
 		} catch (Exception e) {
 			reportInfo.clearImageSetting();
 			Log.e("Exception", e.toString());
 		} finally {
 
 			if (photoStream != null) {
 
 				try {
 					photoStream.close();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 				photoStream = null;
 			}
 
 			imageUri = null;
 
 			if (photoBitmap != null) {
 				// photoBitmap.recycle();
 				// photoBitmap = null;
 			}
 
 			System.gc();
 
 		}
 	}
 
 	private void selectExternalStorageImage() {
 		// Intent intent = new Intent(Intent.ACTION_GET_CONTENT, null);
 
 		// Modified 8-18-2010
 		// Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
 		Intent intent = new Intent();
 
 		intent.setType("image/*");
 
 		// NEWLY ADDED PARAMETERS
 		intent.setAction(Intent.ACTION_GET_CONTENT);
 		intent.addCategory(Intent.CATEGORY_OPENABLE);
 
 		startActivityForResult(intent, INTERNAL_MEDIA_SELECT);
 	}
 
 	private void setEditTextField() {
 		if (mEditText.getText() != null) {
 			reportInfo.setDescription(mEditText.getText().toString().trim());
 		}
 	}
 
 	@Override
 	protected void onSaveInstanceState(Bundle outState) {
 		setEditTextField();
 		stopGPSService();
 
 		ApplicationState.savePreferences();
 
 		super.onSaveInstanceState(outState);
 	}
 
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 		if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.ECLAIR
 				&& keyCode == KeyEvent.KEYCODE_BACK
 				&& event.getRepeatCount() == 0) {
 			// Take care of calling this method on earlier versions of
 			// the platform where it doesn't exist.
 			onBackPressed();
 		}
 
 		setEditTextField();
 
 		return super.onKeyDown(keyCode, event);
 	}
 
 	@Override
 	public void onBackPressed() {
 		// This will be called either automatically for you on 2.0
 		// or later, or by the code above on earlier versions of the
 		// platform.
 		// Toast.makeText(getApplicationContext(), "Back button clicked",
 		// Toast.LENGTH_SHORT).show();
 		showDialog(DIALOG_QUIT_REPORT_ID);
 		return;
 	}
 
 	@Override
 	public boolean onSearchRequested() {
 		return false;
 	}
 
 	@Override
 	public void onConfigurationChanged(Configuration newConfig) {
 		super.onConfigurationChanged(newConfig);
 
 		if (newConfig.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_NO) {
 			Intent i = new Intent(Report.this, Comments.class);
 			startActivity(i);
 
 		} else if (newConfig.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_YES) {
 		}
 	}
 
 	@Override
 	protected void onDestroy() {
 
 		ImageView mTakePhoto = (ImageView) findViewById(R.id.imgPreview);
 		mTakePhoto.setImageBitmap(null);
 
 		// make sure the gps location provider is stopped
 		stopGPSService();
 
 		categoryResponseHandler = null;
 		responseSettingHandler = null;
 		responseBlackListHandler = null;
 		mReportFailure = null;
 		mReportSuccessful = null;
 		mReportResults = null;
 
 		setContentView(R.layout.blank);
 
 		System.gc();
 
 		super.onDestroy();
 
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 		
 		settingTimestamp = System.currentTimeMillis();
 		stopServices();
 	}
 
 	@Override
 	protected void onStop() {
 		super.onStop();
 		stopServices();
 	}
 
 	@Override
 	protected void onResume() {
 
 		startGPSService();
 		
 		long currentTime = System.currentTimeMillis();
 				
 		if (settingTimestamp > 0 && ((currentTime - settingTimestamp) >= 1800000)) 
 		{
 			final HashMap<String, String> postParams = new HashMap<String, String>();
 			
 			final HTTPRequestHelper helper = new HTTPRequestHelper(responseSettingHandler);
 	
 			postParams.put(getString(R.string.param1), getString(R.string.pval));
 			postParams.put(getString(R.string.param2), reportInfo.getDeviceId());
 			
 			new Thread() {
 
 				@Override
 				public void run() {
 					helper.performPost(
 									getString(R.string.postRequestSettings_PROD),
 									"",
 									"",
 									Integer.parseInt(getString(R.string.httpTimeOutMilliseconds)),
 									Integer.parseInt(getString(R.string.socketTimeoutMilliseconds)),
 									null, postParams);
 						}
 			}.start();
 			
 			
 		} else if (settingTimestamp != 0) {
 			settingTimestamp = System.currentTimeMillis();
 		}
 
 		double heap = (Debug.getNativeHeapAllocatedSize() / 1024) / 1024;
 		// Toast.makeText(this, "Heap size is " + Double.toString(heap) + " mb",
 		// Toast.LENGTH_SHORT).show();
 
 		mEditText.setText(ApplicationState.getComments());
 
 		File myfile = null;
 		if (StringUtils.contains(reportInfo.getImageAbsolutePath(), "file://")) {
 			Uri tempUri = Uri.parse(reportInfo.getImageFullUriPath());
 			if (tempUri != null) {
 				myfile = new File(tempUri.getPath());
 			}
 		} else {
 			String currentPhotoPath = reportInfo.getImageAbsolutePath();
 			if (currentPhotoPath != null) {
 				myfile = new File(currentPhotoPath);
 			}
 		}
 
 		if (!myfile.exists()) {
 			imgPreviewTv.setVisibility(android.view.View.VISIBLE);
 			reportInfo.setImageUri(null);
 
 			ImageView mTakePhoto = (ImageView) findViewById(R.id.imgPreview);
 			mTakePhoto.setImageBitmap(null);
 		}
 
 		networkVerify(Report.this);
 
 		super.onResume();
 	}
 
 	/* 
 	 * Verify Network Connectivity 
 	 */
 	public void networkVerify(final Activity mActivity) {
 		if (!isNetworkAvailable(mActivity)) {
 			activeNetworkState = false;
 
 			View customDialogView = View.inflate(this, R.layout.custom_dialog, null);
 			TextView customTextView = (TextView) customDialogView.findViewById(R.id.customDialogText);
 			customTextView.setMinWidth(300);
 
 			customTextView.setText("No Network Available");
 			customTextView.setMovementMethod(LinkMovementMethod.getInstance());
 
 			Linkify.addLinks(customTextView, Linkify.WEB_URLS | Linkify.PHONE_NUMBERS | Linkify.EMAIL_ADDRESSES);
 
 			AlertDialog.Builder builder = new AlertDialog.Builder(this);
 			builder.setView(customDialogView).setCancelable(false)
 					.setPositiveButton("Ok",
 							new DialogInterface.OnClickListener() {
 								public void onClick(DialogInterface dialog,
 										int id) {
 									dialog.dismiss();
 								}
 							}).setNeutralButton("Settings",
 							new DialogInterface.OnClickListener() {
 								public void onClick(DialogInterface dialog,
 										int id) {
 									dialog.dismiss();
 									Intent i = new Intent(
 											android.provider.Settings.ACTION_WIRELESS_SETTINGS);
 									startActivity(i);
 
 								}
 							}).setNegativeButton("Exit",
 							new DialogInterface.OnClickListener() {
 								public void onClick(DialogInterface dialog,
 										int id) {
 									mActivity.finish();
 								}
 							});
 
 			AlertDialog alert = builder.create();
 			alert.show();
 
 		} else {
 			if (!activeNetworkState) {
 				performRequest(null, null, REQUEST_CATEGORIES);
 			}
 		}
 	}
 
 	public static boolean isNetworkAvailable(Activity mActivity) {
 		Context context = mActivity.getApplicationContext();
 		ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
 
 		if (connectivity == null) {
 			return false;
 		} else {
 			NetworkInfo[] info = connectivity.getAllNetworkInfo();
 			if (info != null) {
 				for (int i = 0; i < info.length; i++) {
 					if (info[i].getState() == NetworkInfo.State.CONNECTED) {
 						return true;
 					}
 				}
 			}
 		}
 		return false;
 	}
 
 	private void stopServices() {
 
 		try {
 			stopGPSService();
 
 		} catch (Exception e) {
 			// ignore error
 		}
 
 	}
 
 	private void createNewReport() {
 		
 		startGPSService();
 		
 		currentLatitude = 0;
 		currentLongitude = 0;
 		
 		tempPhotoUri = null;
 		imageStoreFilePath = "";
 		imageName = "";
 		imgPreviewTv.setVisibility(android.view.View.VISIBLE);
 		reportInfo.clear();
 
 		ImageView mTakePhoto = (ImageView) findViewById(R.id.imgPreview);
 		mTakePhoto.setImageBitmap(null);
 		mEditText.setText("");
 
 		buttonLocationChoices.setText(getString(R.string.btnReportLocation));
 
 		if (s != null)
 			s.setSelection(0);
 	}
 
 	public boolean onPrepareOptionsMenu(Menu menu) {
 
 		setEditTextField();
 		return true;
 
 	}
 
 	/* Creates the menu items */
 	public boolean onCreateOptionsMenu(Menu menu) {
 
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.layout.report_menu, menu);
 		return true;
 	}
 
 	/* Handles item selections */
 
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.NewReport:
 			newReportPopup(getString(R.string.newReportMsg));
 			return true;
 		case R.id.MyReport:
 			Intent i = new Intent(Report.this, MyReports.class);
 			startActivity(i);
 			return true;
 		case R.id.Close:
 			showDialog(DIALOG_QUIT_REPORT_ID);
 			return true;
 		case R.id.Help:
 			Intent browserIntent = new Intent("android.intent.action.VIEW", Uri.parse(configSetting.getHelpPage()));
 			startActivity(browserIntent);
 			return true;
 		case R.id.Setting:
 			Intent contactIntent = new Intent(Report.this, ContactInfo.class);
 			startActivity(contactIntent);
 			return true;
 		}
 
 		return false;
 	}
 
 	protected Dialog onCreateDialog(int id) {
 		Dialog dialog = null;
 		switch (id) {
 		case DIALOG_QUIT_REPORT_ID:
 			dialog = quit();
 			break;
 		case DIALOG_GPS_ERROR_ID:
 			dialog = gpsError();
 			break;
 		default:
 			dialog = null;
 		}
 		return dialog;
 	}
 
 	private AlertDialog quit() {
 
 		View customDialogView = View.inflate(this, R.layout.custom_dialog, null);
 		TextView customTextView = (TextView) customDialogView.findViewById(R.id.customDialogText);
 
 		customTextView.setText(getString(R.string.exitApplicationMsg));
 		customTextView.setMovementMethod(LinkMovementMethod.getInstance());
 
 		Linkify.addLinks(customTextView, Linkify.WEB_URLS | Linkify.PHONE_NUMBERS | Linkify.EMAIL_ADDRESSES);
 
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setView(customDialogView)
 
 		// builder.setMessage(Html.fromHtml(""))
 				.setCancelable(false).setPositiveButton("Yes",
 						new DialogInterface.OnClickListener() {
 							public void onClick(DialogInterface dialog, int id) {
 								stopServices();
 
 								ApplicationState.disclaimerStatus(true);
 								ApplicationState.savePreferences();
 
 								android.os.Process.killProcess(android.os.Process.myPid());
 
 							}
 						}).setNegativeButton("No",
 						new DialogInterface.OnClickListener() {
 							public void onClick(DialogInterface dialog, int id) {
 								dialog.cancel();
 							}
 						});
 
 		AlertDialog alert = builder.create();
 
 		return alert;
 	}
 
 	private void contactInfoDialog() {
 
 		View customDialogView = View.inflate(Report.this, R.layout.custom_dialog, null);
 		TextView customTextView = (TextView) customDialogView.findViewById(R.id.customDialogText);
 
 		customTextView.setText(getString(R.string.contactInfoMsg));
 		customTextView.setMovementMethod(LinkMovementMethod.getInstance());
 
 		Linkify.addLinks(customTextView, Linkify.WEB_URLS | Linkify.PHONE_NUMBERS | Linkify.EMAIL_ADDRESSES);
 
 		AlertDialog.Builder builder = new AlertDialog.Builder(Report.this);
 		builder.setView(customDialogView)
 
 		.setCancelable(false).setPositiveButton("Yes",
 				new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int id) {
 						Intent i = new Intent(Report.this, ContactInfo.class);
 						startActivity(i);
 						dialog.dismiss();
 					}
 				}).setNegativeButton("No",
 				new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int id) {
 						dialog.cancel();
 						performRequest(null, null, POST_REPORT);
 					}
 				});
 
 		AlertDialog alert = builder.create();
 
 		alert.show();
 	}
 
 	private AlertDialog gpsError() {
 		reportInfo.setValidGPSLocation(false);
 		reportInfo.setLatitude(0);
 		reportInfo.setLongitude(0);
 
 		View customDialogView = View.inflate(this, R.layout.custom_dialog, null);
 		TextView customTextView = (TextView) customDialogView.findViewById(R.id.customDialogText);
 
 		customTextView.setText(gpsErrorMsgDialog);
 		customTextView.setMovementMethod(LinkMovementMethod.getInstance());
 
 		Linkify.addLinks(customTextView, Linkify.WEB_URLS | Linkify.PHONE_NUMBERS | Linkify.EMAIL_ADDRESSES);
 
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setView(customDialogView)
 
 		.setCancelable(true).setPositiveButton("OK",
 				new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int id) {
 						Intent i = new Intent(Report.this, LocationFinder.class);
 						i.putExtra(CROSS_HAIR_LATITUDE, reportInfo
 								.getLatitude());
 						i.putExtra(CROSS_HAIR_LONGITUDE, reportInfo
 								.getLongitude());
 
 						try {
 							startActivityForResult(i, CROSSHAIR_SELECT);
 						} catch (ActivityNotFoundException aex) {
 							Log.e("ActivityNotFoundException", aex.toString());
 						} catch (Exception ex) {
 							Log.e("Exception", ex.toString());
 						}
 
 						dialog.dismiss();
 					}
 				});
 
 		AlertDialog alert = builder.create();
 
 		return alert;
 
 	}
 
 	private void disclaimerPopup(String disclaimer) {
 		if (StringUtils.isNotBlank(disclaimer)) {
 
 			View customDialogView = View.inflate(this, R.layout.custom_dialog, null);
 			TextView customTextView = (TextView) customDialogView.findViewById(R.id.customDialogText);
 
 			customTextView.setText(disclaimer);
 			customTextView.setMovementMethod(LinkMovementMethod.getInstance());
 			Linkify.addLinks(customTextView, Linkify.WEB_URLS | Linkify.PHONE_NUMBERS | Linkify.EMAIL_ADDRESSES);
 
 			AlertDialog.Builder builder = new AlertDialog.Builder(this);
 			// setLinks(disclaimer);
 			builder.setView(customDialogView).setCancelable(false)
 					.setPositiveButton("OK",
 							new DialogInterface.OnClickListener() {
 								public void onClick(DialogInterface dialog,
 										int id) {
 
 									dialog.dismiss();
 								}
 							});
 
 			AlertDialog alert = builder.create();
 			alert.show();
 		}
 	}
 
 	private void newReportPopup(String msg) {
 		if (StringUtils.isNotBlank(msg)) {
 
 			View customDialogView = View.inflate(this, R.layout.custom_dialog, null);
 			TextView customTextView = (TextView) customDialogView.findViewById(R.id.customDialogText);
 
 			customTextView.setText(msg);
 			customTextView.setMovementMethod(LinkMovementMethod.getInstance());
 			Linkify.addLinks(customTextView, Linkify.WEB_URLS | Linkify.PHONE_NUMBERS | Linkify.EMAIL_ADDRESSES);
 
 			AlertDialog.Builder builder = new AlertDialog.Builder(this);
 			// setLinks(disclaimer);
 			builder.setView(customDialogView).setCancelable(false)
 					.setPositiveButton("Yes",
 							new DialogInterface.OnClickListener() {
 								public void onClick(DialogInterface dialog,	int id) {
 									createNewReport();
 									dialog.dismiss();
 								}
 
 							}).setNegativeButton("No",
 							new DialogInterface.OnClickListener() {
 								public void onClick(DialogInterface dialog, int id) {
 									dialog.dismiss();
 								}
 							});
 
 			AlertDialog alert = builder.create();
 			alert.show();
 
 		}
 	}
 
 	private void performRequest(final String user, final String pass, final String requestType) {
 		String dialogText = "";
 
 		if (requestType == REQUEST_SETTINGS) {
 			if (progressDialog == null) {
 				dialogText = getString(R.string.btnSpinnerRetrieving);
 				progressDialog = ProgressDialog.show(this, null, dialogText);
 			}
 		} else if (requestType == POST_REPORT) {
 			dialogText = getString(R.string.submitReportMsg);
 
 			boolean isValidReport = true;
 			boolean foundValidCategory = false;
 
 			String category = null;
 			category = reportInfo.getCategoryId();
 
 			if (StringUtils.isBlank(category)) {
 				errorFields += "Report Type\n";
 				isValidReport = false;
 			} else {
 				// Check all required fields are populated correctly.
 				if (posts != null) {
 					for (CategoryDetails p : posts) {
 						if (p.getCategory_id().compareToIgnoreCase(reportInfo.getCategoryId()) == 0) {
 							foundValidCategory = true;
 
 							if (p.getIphone_address_input_required()
 									.compareToIgnoreCase("1") == 0) {
 								if (reportInfo.getLatitude() == 0 || reportInfo.getLongitude() == 0) {
 									errorFields += "Location\n";
 									isValidReport = false;
 								}
 							}
 
 							if (p.getIphone_binary_input_required().compareToIgnoreCase("1") == 0) {
 								if (StringUtils.isBlank(reportInfo.getImageAbsolutePath())) {
 									errorFields += "Photo\n";
 									isValidReport = false;
 								} else {
 									File myfile = new File(reportInfo.getImageAbsolutePath());
 
 									if (!myfile.exists()) {
 										errorFields += "Missing Photo\n";
 										isValidReport = false;
 									}
 									myfile = null;
 								}
 							}
 
 
 							if (p.getIphone_text_input_required().compareToIgnoreCase("1") == 0) {
 								if (StringUtils.isBlank(reportInfo.getDescription())) {
 									errorFields += "Comments\n";
 									isValidReport = false;
 								} else if (reportInfo.getDescription().compareToIgnoreCase(getString(R.string.default_text)) == 0) {
 									errorFields += "Comments\n";
 									isValidReport = false;
 								}
 							}
 							break;
 						}
 					}
 
 					if (!foundValidCategory) {
 						errorFields += "Unable to find matching category\n";
 						isValidReport = false;
 					}
 
 				} else {
 					errorFields += "Missing internal categories\n";
 					isValidReport = false;
 				}
 			}
 
 			if (!isValidReport) {
 				reportPostHandler.post(mReportResults);
 				return;
 			} else {
 				progressDialog = ProgressDialog.show(this, null, dialogText);
 			}
 		}
 
 		new Thread() {
 
 			@Override
 			public void run() {
 
 				String requestUrl = null;
 				HashMap<String, String> postParams = new HashMap<String, String>();
 
 				if (requestType == REQUEST_CATEGORIES) {
 					HTTPRequestHelper helper = new HTTPRequestHelper(categoryResponseHandler);
 
 					postParams.put(getString(R.string.param1), getString(R.string.pval));
 					postParams.put(getString(R.string.param2), reportInfo.getDeviceId()); 
 					postParams.put(getString(R.string.param3), Double.toString(reportInfo.getLatitude()));
 					postParams.put(getString(R.string.param4), Double.toString(reportInfo.getLongitude()));
 
 					helper.performPost(
 									getString(R.string.postRequestInfo_PROD),
 									user,
 									pass,
 									Integer.parseInt(getString(R.string.httpTimeOutMilliseconds)),
 									Integer.parseInt(getString(R.string.socketTimeoutMilliseconds)),
 									null, postParams);
 
 				} else if (requestType == REQUEST_BLACKLIST) {
 					HTTPRequestHelper helper = new HTTPRequestHelper(responseBlackListHandler);
 
 					postParams.put(getString(R.string.param1), getString(R.string.pval));
 					postParams.put(getString(R.string.param2), reportInfo.getDeviceId()); 
 
 					helper.performPost(
 									getString(R.string.postRequestBlackList_PROD),
 									user,
 									pass,
 									Integer.parseInt(getString(R.string.httpTimeOutMilliseconds)),
 									Integer.parseInt(getString(R.string.socketTimeoutMilliseconds)),
 									null, postParams);
 
 				} else if (requestType == REQUEST_SETTINGS) {
 
 					HTTPRequestHelper helper = new HTTPRequestHelper(responseSettingHandler);
 
 					postParams.put(getString(R.string.param1), getString(R.string.pval));
 					postParams.put(getString(R.string.param2), reportInfo.getDeviceId());
 
 					helper.performPost(
 									getString(R.string.postRequestSettings_PROD),
 									user,
 									pass,
 									Integer.parseInt(getString(R.string.httpTimeOutMilliseconds)),
 									Integer.parseInt(getString(R.string.socketTimeoutMilliseconds)),
 									null, postParams);
 
 				} else if (requestType == POST_REPORT) {
 					boolean isError = false;
 
 					PartBase picture = null;
 					boolean imageAdded = false;
 					
 					try {
 						CategoryDetails category = null;
 
 						if (posts != null) {
 							for (CategoryDetails currentCategory : posts) {
 								if (currentCategory.getCategory_id().compareToIgnoreCase(reportInfo.getCategoryId()) == 0) {
 									category = currentCategory;
 									break;
 								}
 							}
 						} else {
 							return;
 						}
 
 						//HttpClient httpClient = new DefaultHttpClient();
 						// httpClient.getHttpConnectionManager().getParams().setConnectionTimeout(90000);
 
 						//HttpContext localContext = new BasicHttpContext();
 
 						requestUrl = getString(R.string.postReportUrl_PROD);
 						
 						//HttpPost httpPost = new HttpPost(requestUrl);
 						//MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
 
 						byte[] imageByteArray = null;
 						
 						try {
 							
 							if (reportInfo.getImageUri() != null && StringUtils.isNotBlank(reportInfo.getImageAbsolutePath())) {
 								
 								File tempfile = null;
 
 								if (StringUtils.contains(reportInfo.getImageUri().toString(), "file://")) {
 									tempfile = new File(reportInfo.getImageUri().getPath());
 								} else {
 									tempfile = new File(reportInfo.getImageAbsolutePath());
 								}
 
 								imageByteArray = getImageBytes(tempfile);
 								long length = imageByteArray.length;
 
 								InputStream photoStream = getContentResolver().openInputStream(reportInfo.getImageUri());
 								BitmapFactory.Options options = new BitmapFactory.Options();
 
 								// default sample size
 								//options.inSampleSize = 2;
 																
 								options.inSampleSize = configSetting.getSampleSizeSmall();
 
 								int imgQuality = 70;
 
 								if (length > 0) {
 									length = length / 1000;
 
 									if (length > 3000) {
 										options.inSampleSize = configSetting.getSampleSizeExtra();
 									} else if (length >= 2000) {
 										options.inSampleSize = configSetting.getSampleSizeLarge();
 									} else if (length >= 1000) {
 										options.inSampleSize = configSetting.getSampleSizeMedium();
 									} else if (length > 200) {
 										options.inSampleSize = configSetting.getSampleSizeSmall();
 									}
 
 									if (reportInfo.getImageWidth() > 1000 || reportInfo.getImageHeight() > 1000) {
 										options.inSampleSize = configSetting.getSampleSizeMedium();
 										imgQuality = 90;
 									} else if (reportInfo.getImageWidth() > 800 || reportInfo.getImageHeight() > 800) {
 										options.inSampleSize = configSetting.getSampleSizeSmall();
 										imgQuality = 90;
 									}
 
 								}
 
 								Bitmap photoBitmap = BitmapFactory.decodeStream(photoStream, null, options);
 								 
 								try {
 							        
 							        ExifInterface exif = new ExifInterface(tempfile.getAbsolutePath());
 									String tagOrientation = exif.getAttribute(ExifInterface.TAG_ORIENTATION);
 
 									if (StringUtils.isNotBlank(tagOrientation) && Integer.parseInt(tagOrientation) >= 6) {
 										Matrix matrix = new Matrix();
 								        matrix.postRotate(90);
 
 								        photoBitmap = Bitmap.createBitmap( photoBitmap, 0, 0, photoBitmap.getWidth(), photoBitmap.getHeight(), matrix, true);
 									}
 
 								} catch (Exception ex) {
 									Log.e("ROTATE ERROR", ex.toString());
 								}
 						        
 								//Bitmap photoBitmap = BitmapFactory.decodeStream(photoStream, null, options);
 								ByteArrayOutputStream baos = new ByteArrayOutputStream();
 								photoBitmap.compress(CompressFormat.JPEG, imgQuality, baos);
 
 								
 								imageByteArray = baos.toByteArray();
 								photoBitmap.recycle();
 								photoBitmap = null;
 
 								baos.close();
 
 								//long newSize = imageByteArray.length / 1000;
 								
 								/*
 								entity.addPart( category.getIphone_binary_input_id(), 
 										new FileBody((( File ) tempfile ), "image/jpg" ));
 								*/
 								
 			/**********************************************************************************************
 			 * Issue using HttpClient 4.0.3 where the mime-type for image file is not received correctly 
 			 * on the backend.  Seems to always show up as text/plain or blank.  Able to receive binary file,
 			 * but mime-type is incorrect.
 			 * 
 			 * If there's a fix or workaround, I'm open to reusing the latest and greatest HttpClient library.  
 			 * Until then will be reverting back to the legacy Commons HttpClient 3.1 library.
 			 **********************************************************************************************/
 								/*
 								entity.addPart(category.getIphone_binary_input_id(),
 										new InputStreamKnownSizeBody(
 												new ByteArrayInputStream(
 														imageByteArray), 
 														imageByteArray.length, 
 														"image/jpg", 
 														reportInfo.getImageName()));
 								*/
 								
 								
 								picture = new FilePart(category.getIphone_binary_input_id(), 
 										new ByteArrayPartSource(reportInfo.getImageName(), imageByteArray));
 
 								picture.setContentType("image/jpeg");
 								picture.setCharSet(null);
 								
 								imageAdded = true;
 								
 								// }
 							} 
 
 						} catch (Exception e) {
 							isError = true;
 							Log.e("FileOutputStream", e.toString());
 						}
 
 						if (category != null && !isError) {
 
 							String tempText = reportInfo.getDescription();
 
 							if (StringUtils.isBlank(tempText)) {
 								reportInfo.setDescription(mEditText.getText().toString().trim());
 							}
 							
 							if (!imageAdded) {
 								//Add placeholder
 								picture = new StringPart("EMPTY_PLACEHOLDER_FOR_NO_IMAGE", "");
 							}
 							
 							Part[] parts = { 
 									new StringPart(getString(R.string.param1),getString(R.string.pval)),
 									new StringPart(getString(R.string.param2), reportInfo.getDeviceId()),
 									new StringPart("category_id", reportInfo.getCategoryId()),
 									new StringPart("track_id", category.getInstance_id()),
 									new StringPart(category.getIphone_address_input_id(), reportInfo.getAddressDescription()),
 									new StringPart(category.getIphone_address_input_id() + "_lat", Double.toString(reportInfo.getLatitude())),
 									new StringPart(category.getIphone_address_input_id() + "_long", Double.toString(reportInfo.getLongitude())),
 									new StringPart(category.getIphone_text_input_id(), reportInfo.getDescription()),
 									new StringPart("device_manufacturer",Build.MANUFACTURER),
 									new StringPart("device_model", Build.MODEL),
 									new StringPart("device_os_name", "Android"),
 									new StringPart("device_os_version", Build.VERSION.RELEASE + " Build " + Build.VERSION.INCREMENTAL + " Version " + Integer.toString(Build.VERSION.SDK_INT)),
 									picture};
 							
 							
 							/*
 							Hashtable<String, String> postData = new Hashtable<String, String>();
 
 							postData.put(getString(R.string.param1),getString(R.string.pval));
 							postData.put(getString(R.string.param2), reportInfo.getDeviceId());
 
 							postData.put("category_id", reportInfo.getCategoryId());
 							postData.put("track_id", category.getInstance_id());
 
 							postData.put(category.getIphone_address_input_id(),	reportInfo.getAddressDescription());
 							postData.put(category.getIphone_address_input_id() + "_lat", Double.toString(reportInfo.getLatitude()));
 							postData.put(category.getIphone_address_input_id() + "_long", Double.toString(reportInfo.getLongitude()));
 							postData.put(category.getIphone_text_input_id(), reportInfo.getDescription());
 
 							// Optional information
 							try {
 								postData.put("device_manufacturer",	Build.MANUFACTURER);
 								postData.put("device_model", Build.MODEL);
 
 								//postData.put("device_os_name", "Android " + Build.VERSION.RELEASE + " Build " + Build.VERSION.INCREMENTAL);
 								//postData.put("device_os_version", Integer.toString(Build.VERSION.SDK_INT));
 
 								postData.put("device_os_name", "Android Version " + Integer.toString(Build.VERSION.SDK_INT) );
 								postData.put("device_os_version", Build.VERSION.RELEASE + " Build " + Build.VERSION.INCREMENTAL);
 
 							
 							} catch (Exception ex) {
 								// do nothing
 							}
 
 							Iterator postDataIterator = postData.entrySet().iterator();
 
 							while (postDataIterator.hasNext()) {
 								Map.Entry entry = (Map.Entry) postDataIterator.next();
 
 								StringBody stringBody = new StringBody(entry.getValue().toString());
 								entity.addPart(entry.getKey().toString(), stringBody);
 							}
 									
 							httpPost.setEntity(entity);
 
 							HttpResponse response = httpClient.execute(httpPost);
 							
 							*/
 							
 							
 							//isError = true;
 							isError = sendReport(requestUrl, parts);
 
 							if (progressDialog.isShowing())
 								progressDialog.dismiss();
 
 							/*
 							if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
 
 								String str = EntityUtils.toString(response.getEntity());
 
 								Log.d("HTTP RESPONSE", str);
 								isError = parseXMLResult(str, SUBMIT_REPORT);
 								// createNewReport();
 							} else {
 
 								// Looks to be a failed report
 								// reportPostHandler.post(mReportFailure);
 
 								ApplicationState.savePreferences();
 								// savePreferences();
 
 								isError = true;
 								// disclaimerPopup("Error sending report.\nPlease try again later.");
 							}
 							
 							httpClient.getConnectionManager().shutdown();
 							*/
 						}
 
 					} catch (Exception e) {
 						isError = true;
 						Log.e("HTTP RESPONSE ERROR", e.toString());
 					} finally {
 
 						if (progressDialog.isShowing())
 							progressDialog.dismiss();
 
 						if (isError) {
 							reportPostHandler.post(mReportFailure);
 						}
 					}
 
 				}
 
 			}
 		}.start();
 
 	}
 
 	public static byte[] getImageBytes(File filename) throws IOException {
 		int offset = 0, num = 0;
 
 		InputStream inputstr = new FileInputStream(filename);
 		byte[] bytes = new byte[(int) filename.length()];
 
 		while (offset < bytes.length
 				&& (num = inputstr.read(bytes, offset, bytes.length - offset)) >= 0) {
 			offset += num;
 		}
 
 		if (offset < bytes.length) {
 			throw new IOException("Error reading filename "	+ filename.getName());
 		}
 
 		inputstr.close();
 		return bytes;
 	}
 
 	class InputStreamKnownSizeBody extends InputStreamBody {
 		private int length;
 
 		public InputStreamKnownSizeBody(final InputStream in, final int len,
 				final String mimeType, final String filename) {
 			super(in, mimeType, filename);
 			length = len;
 		}
 
 		@Override
 		public long getContentLength() {
 			return length;
 		}
 	}
 	
 	
 	private boolean sendReport(String url, Part[] parts) {
 
 		String httpResponse = null;
 		PostMethod postRequest = new PostMethod(url);
 
 		int httpStatusCode = 500;
 		try {
 
 			HttpClient client = new HttpClient();
 			client.getHttpConnectionManager().getParams().setConnectionTimeout(configSetting.getAndroidTimeout());
 
 			postRequest.setRequestEntity(new MultipartRequestEntity(parts, postRequest.getParams()));
 			client.executeMethod(postRequest);
 			httpStatusCode = postRequest.getStatusCode();
 			httpResponse = postRequest.getResponseBodyAsString();
 			postRequest.releaseConnection();
 			postRequest = null;
 
 		} catch (Exception ex) {
 			Log.e("ERROR POST", ex.getMessage());
 			return true;
 		} finally {
 			if (postRequest != null)
 				postRequest.releaseConnection();
 		}
 
 		if (httpStatusCode == 200 && StringUtils.isNotBlank(httpResponse)) {
 			return parseXMLResult(httpResponse, SUBMIT_REPORT);
 		} else {
 			ApplicationState.savePreferences();
 			return true;
 		}
 		
 	}
     
     
 	/**
 	 * Parse XML result into data objects.
 	 * 
 	 * @param xmlString
 	 * @return
 	 */
 	private boolean parseXMLResult(String xmlString, String requestType) {
 		StringBuilder result = new StringBuilder();
 		boolean errorFlag = false;
 
 		try {
 
 			if (requestType.compareToIgnoreCase(SUBMIT_REPORT) == 0) {
 				// ResultHandler handler = new ResultHandler();
 
 				SAXParserFactory spf = SAXParserFactory.newInstance();
 				SAXParser sp = spf.newSAXParser();
 
 				XMLReader xr = sp.getXMLReader();
 
 				ResultHandler parserResultHandler = new ResultHandler();
 
 				xr.setContentHandler(parserResultHandler);
 				xr.parse(new InputSource(new StringReader(xmlString)));
 
 				if (StringUtils.isNotBlank(parserResultHandler.getItemId())) {
 					reportPostHandler.post(mReportSuccessful);
 				} else {
 					// reportPostHandler.post(mReportFailure);
 					errorFlag = true;
 				}
 
 			}
 
 		} catch (Exception e) {
 			Log.e(Constants.LOGTAG, " " + Report.CLASSTAG + " ERROR - "
 					+ e.toString());
 			errorFlag = true;
 		}
 
 		if (errorFlag)
 			setDefaultCategory(getString(R.string.btnSpinnerError));
 
 		return errorFlag;
 		// return result.toString();
 	}
 
 	public class MyOnItemSelectedListener implements OnItemSelectedListener {
 
 		private boolean initialSetup = false;
 
 		public MyOnItemSelectedListener() {
 			initialSetup = true;
 		}
 
 		public void onItemSelected(AdapterView<?> parent, View view, int pos,
 				long id) {
 
 			CategoryDetails d = adapter.getItem(pos);
 
 
 			if (!initialSetup) {
 				if (reportInfo.getCategoryId().compareToIgnoreCase(d.getCategory_id()) != 0) {
 					disclaimerPopup(d.getIphone_message());
 					reportInfo.setCategoryId(d.getCategory_id());
 				}
 			} else {
 				initialSetup = false;
 			}
 
 		}
 
 		public void onNothingSelected(AdapterView<?> parent) {
 			// Do nothing.
 		}
 	}
 
 	class CaseInsensitiveComparator implements Comparator {
 		public int compare(Object o1, Object o2) {
 			String s1 = o1.toString().toUpperCase();
 			String s2 = o2.toString().toUpperCase();
 			return s1.compareTo(s2);
 		}
 	}
 
 }
