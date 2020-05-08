 package com.example.maptracker;
 
 
 
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Set;
 
 import util.Database;
 import android.annotation.SuppressLint;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.location.Criteria;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.net.Uri;
 import android.os.Bundle;
 import android.provider.MediaStore;
 import android.support.v4.app.FragmentActivity;
 import android.text.format.Time;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.CompoundButton;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 import android.widget.FrameLayout;
 import android.widget.SlidingDrawer;
 import android.widget.SlidingDrawer.OnDrawerCloseListener;
 import android.widget.SlidingDrawer.OnDrawerOpenListener;
 import android.widget.ImageButton;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.ToggleButton;
 
 import com.google.android.gms.common.GooglePlayServicesUtil;
 import com.google.android.gms.maps.CameraUpdateFactory;
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
 import com.google.android.gms.maps.SupportMapFragment;
 import com.google.android.gms.maps.model.LatLng;
 import com.google.android.gms.maps.model.Marker;
 import com.google.android.gms.maps.model.MarkerOptions;
 import com.google.android.gms.maps.model.Polyline;
 import com.google.android.gms.maps.model.PolylineOptions;
 
 import dataWrappers.DBMarker;
 import dataWrappers.DBRoute;
 import dataWrappers.GPS;
 
 
 
 public class MainActivity extends FragmentActivity{
 
 	private static final String TAG="MT: ";
 	private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
 	private static final int CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE = 200;
 
 	public LinkedList<DBMarker> markerList = new LinkedList<DBMarker>();
 	public LinkedList<GPS> gpsList = new LinkedList<GPS>();
 	public HashMap<DBMarker, Marker> markerHashTable = new HashMap<DBMarker, Marker>();
 	public long gpsTimeFreq = 5000;
 	public int gpsMaxDistanceFreq = 26;
 	public DBRoute thisRoute;
 	Database database = new Database(this);
 	public LocationManager locationManager;
 	public String provider;
 	public LocationListener locListener;
 	PolylineOptions lineOptions = null;
 	public GoogleMap map;
 	Polyline oldPolyline = null;
 	LatLng lastPosition;
 	Marker startMarker;
 	Marker endMarker;
 	FrameLayout markerDetails;
 	TextView getComment;
 	Button export;
 	Button edit;
 	Button menu;
 	Button handle, drawerMenu, drawerComment, drawerCamera, drawerMarker, drawerAudio, drawerVideo;
 	SlidingDrawer sliding;
 	ToggleButton trackingButton;
 	Uri imageFilePath;
 	boolean first = true, markerOpen = true, markerClose = true, editMarker = true, tutExport = true;
 	boolean tutorial = true;
 
 
 	//---------------------------------------------------------------
 	DBMarker theMarker;
 	TextView markerTitleText;
 	TextView markerDateText;
 	TextView commentText;
 	Button deletePhoto;
 	Button deleteVideo;
 	Button deleteAudio;
 	ImageButton trashMarker;
 	ImageButton closeMarker;
 	ImageButton pictureButton;
 	ImageButton videoButton;
 	ImageButton audioButton;
 
 
 
 	///---------------------------------------------------------------
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 
 		firstTimeTutorial();
 
 
 		getComment  = new TextView(this);
 		//Create the map Fragment
 		GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext());
 		map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
 
 		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
 		Criteria criteria = new Criteria();
 		provider = locationManager.getBestProvider(criteria, false);
 		provider = "network";
 		System.out.println("PRINT THI:");
 		System.out.println(provider +  " is being used.");
 		markerDetails = (FrameLayout)findViewById(R.id.markerDetailsFrame);
 		markerDetails.setVisibility(FrameLayout.GONE);
 
 		initializeMarkerDetailsButtons();
 		initializeMarkerDetailsText();
 		setMarkerDetailListeners();
 		map.setOnMarkerClickListener(new OnMarkerClickListener(){
 			//Fill in code here for what to do when Google Maps Marker is clicked
 			//Check which DBMarker is connected with argument Marker and go from there
 			public boolean onMarkerClick(Marker arg0) {
 				System.out.println("MARKER CLICK CALLED");
 				if(arg0.equals(startMarker) || arg0.equals(endMarker))
 					return false;
 
 				Set<DBMarker> keyList = markerHashTable.keySet();
 				DBMarker editedMarker = null;
 
 				for(DBMarker marker : keyList)
 				{
 					if(markerHashTable.get(marker).equals(arg0))
 					{
 
 						System.out.println("HASH WORKED");
 						passMarker(marker);
 						break;
 					}
 				}
 
 				return true;
 			}
 		});
 
 		locListener = new LocationListener(){
 			public void onLocationChanged(Location location)
 			{
 				System.out.println(location.getAccuracy());
 				if(location.getAccuracy() < gpsMaxDistanceFreq)
 				{
 					LatLng thisLocation = new LatLng(location.getLatitude(), location.getLongitude());
 
 					if(lastPosition == null)
 					{
 						startMarker = map.addMarker(new MarkerOptions()
 						.position(thisLocation)
 						.title("Starting marker"));
 
 						//Create a new GPS object
 						GPS gpsObject = new GPS();
 						gpsObject.latitude = location.getLatitude();
 						gpsObject.longitude = location.getLongitude();
 						gpsObject.time = location.getTime();
 						gpsList.add(gpsObject);
 
 						lineOptions = new PolylineOptions();
 						lineOptions.add(thisLocation);
 					}
 
 					else
 					{
 						//Create a new GPS object
 						GPS gpsObject = new GPS();
 						gpsObject.latitude = location.getLatitude();
 						gpsObject.longitude = location.getLongitude();
 						gpsObject.time = location.getTime();
 						gpsList.add(gpsObject);
 
 						if (lineOptions != null && oldPolyline == null)
 						{
 							lineOptions.add(thisLocation);
 							oldPolyline = map.addPolyline(lineOptions);
 						}
 						else
 						{
 							lineOptions.add(thisLocation);
 							oldPolyline.remove();
 							oldPolyline = map.addPolyline(lineOptions);
 						}
 					}
 
 					lastPosition = thisLocation;
 
 				}
 
 				System.out.println("Pass at Loc: " + location.getLatitude() + ", " + location.getLongitude());
 
 			}
 
 			public void onProviderDisabled(String provider) {}
 			public void onProviderEnabled(String provider) {}
 			public void onStatusChanged(String provider, int status, Bundle extras) {}
 		};
 
 		database.open();
 
 		Location location = getLocation();
 		LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
 		map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
 		//        long startTime = System.currentTimeMillis();
 		//        
 		//        for(int i = 0; i < 5; i++)
 		//        {
 		//        	//Create a new GPS object
 		//			GPS gpsObject = new GPS();
 		//			gpsObject.latitude = Math.random();
 		//			gpsObject.longitude = Math.random();
 		//			gpsObject.time = System.currentTimeMillis();
 		//			gpsList.add(gpsObject);
 		//        }
 		//       
 		//		database.addNewRoute(gpsList, markerList, startTime, System.currentTimeMillis(), "", "Test route", "Mikes House");
 		//
 		//       
 		//       List<DBRoute> routeList = database.getAllRoutes();
 		//       System.out.println(routeList.size() + " Routes");
 		//       for(DBRoute route : routeList)
 		//       {
 		//    	   System.out.println("Route: " + route.routeID);
 		//    	   System.out.println("GPS Points: " + route.countDataPoints);
 		//    	   System.out.println("Location Name: " + route.location);
 		//    	   System.out.println("Route Name: " + route.routeName);
 		//    	   System.out.println("Start Time: " + route.timeStart);
 		//    	   System.out.println("End Time: " + route.timeEnd);
 		//    	   
 		//    	   List<GPS> gpsPoints = database.getGPSData(route.routeID);
 		//    	   for(GPS gps : gpsPoints)
 		//    	   {
 		//    		   System.out.println("GPS: " + gps.latitude + ", " + gps.longitude + " at " + gps.time);
 		//    	   }
 		//       }
 
 		trackingButton = (ToggleButton)findViewById(R.id.togglebutton);
 		trackingButton.setOnCheckedChangeListener(new OnCheckedChangeListener(){
 
 			@Override
 			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
 				if(arg1){
 					startTracking();
 					System.out.println("TRACKING WAS TURNED ON");
 				}else{
 					System.out.println("TRACKING WAS TURNED OFF");
 					stopTracking("Default Route Name","What a route!","FranceFreancefrancfrance");
 				}
 
 			}
 
 		});
 		handle = (Button) findViewById(R.id.handle);
 		sliding = (SlidingDrawer) findViewById(R.id.slidingDrawer1);
 		sliding.setOnDrawerOpenListener(new OnDrawerOpenListener() {
 			@Override
 			public void onDrawerOpened() {
 				handle.setBackgroundResource(R.drawable.arrowclose);
 				menuOpenTutorial();
 			}
 		});
 		sliding.setOnDrawerCloseListener(new OnDrawerCloseListener() {
 			@Override
 			public void onDrawerClosed() {
 				handle.setBackgroundResource(R.drawable.arrowopen);
 			}
 		});
 
 		drawerMenu = (Button) findViewById(R.id.drawerButtonMenu);
 		drawerMenu.setOnClickListener(new OnClickListener(){
 			public void onClick(View view) {
 				Intent i = new Intent(view.getContext(), MenuActivity.class);
 				i.putExtra("tutorial", tutorial);
 				startActivity(i);
 				Object response = i.getExtras().get("newFreq");
 				if(response != null){
 					gpsTimeFreq = (Integer)response;
 				}
 			}
 		});
 
 		drawerCamera = (Button) findViewById(R.id.drawerButtonCamera);
 		drawerCamera.setOnClickListener(new OnClickListener() {
 			public void onClick(View view) {
 				cameraButtonClicked();
 			}
 		});
 
 		drawerVideo = (Button) findViewById(R.id.drawerButtonVideo);
 		drawerVideo.setOnClickListener(new OnClickListener() {
 			public void onClick(View view) {
 				videoButtonClicked();
 			}
 		});
 
 		drawerMarker = (Button) findViewById(R.id.drawerButtonMarker);
 		drawerMarker.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 
 				//Log create marker
 				Log.w(TAG, "Create Marker, Time: " + System.currentTimeMillis());
 
 				DBMarker marker = new DBMarker();
 				createMarker(marker);
 
 			}
 		});
 
 		drawerComment = (Button) findViewById(R.id.drawerButtonComment);
 		drawerComment.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View arg0) {
 
 				// custom dialog
 				final Dialog dialog = new Dialog(arg0.getContext());
 				dialog.setContentView(R.layout.alert_dialog);
 				dialog.setTitle("Comment!");
 
 				// set the custom dialog components - text, image and button
 				TextView text = (TextView) dialog.findViewById(R.id.text);
 				text.setText("a");
 				text.setText("Type comment below:");
 
 				Button dialogButton = (Button) dialog.findViewById(R.id.dialogButtonConfirm);
 				// if button is clicked, close the custom dialog
 				dialogButton.setOnClickListener(new OnClickListener() {
 					@Override
 					public void onClick(View v) {
 
 						//Log create comment marker
 						Log.w(TAG, "Create Comment Marker, Time: " + System.currentTimeMillis());
 
 						DBMarker marker = new DBMarker();
 						marker.text = ((TextView) dialog.findViewById(R.id.editText1)).getText().toString();
 						createMarker(marker);
 						dialog.dismiss();
 					}
 				});
 
 				dialog.show();
 			}
 		});
 
 
 	}
 
 	protected void menuOpenTutorial() {
 		if (markerOpen) {
 		AlertDialog.Builder tut=new AlertDialog.Builder(MainActivity.this);
 		tut.setMessage("This is the Marker Panel. To start tracing your route" +
 				" switch the tracking on or add a marker, to automatically turn on tracking.\n" +
 				"Click on one of these icons to add a text comment," +
 				" picture, video or audio recording to this location.");
 
 		tut.setNeutralButton("Continue", new DialogInterface.OnClickListener() {
 
 			@Override
 			public void onClick(DialogInterface dialog, int which) {}
 		});
 		markerOpen = false;
 		tut.show();
 		}
 	}
 
 	protected void menuCloseTutorial() {
 		if (markerClose) {
 		AlertDialog.Builder tut=new AlertDialog.Builder(MainActivity.this);
 		tut.setMessage("Data has been successfully added to the location. " +
 				"Click on the marker to edit the data or add new data.");
 
 		tut.setNeutralButton("Continue", new DialogInterface.OnClickListener() {
 
 			@Override
 			public void onClick(DialogInterface dialog, int which) {}
 		});
 		markerClose = false;
 		tut.show();
 		}
 	}
 
 	/**
 	 * 
 	 */
 
 	private void startTutorial() {
 		AlertDialog.Builder tut=new AlertDialog.Builder(MainActivity.this);
 		tut.setMessage("Click the arrow at the bottom of the page to mark your first location," +
 				" export data, or change settings.");
 
 		tut.setNeutralButton("Continue", new DialogInterface.OnClickListener() {
 
 			@Override
 			public void onClick(DialogInterface dialog, int which) {}
 		});
 
 		tut.show();
 	}
 
 	private void firstTimeTutorial() {
 		if (first) {
 			AlertDialog.Builder firstTut=new AlertDialog.Builder(MainActivity.this);
 			firstTut.setMessage("Welcome! We recognize that this is your first time using Field Tracker, would you like a guided tutorial?");
 
 			firstTut.setPositiveButton("YES",new DialogInterface.OnClickListener() {
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					tutorial = true;
 					first = false;
 					startTutorial();
 				}
 			});
 
 			firstTut.setNegativeButton("NO",new DialogInterface.OnClickListener() {
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					tutorial = false;
 					first = false;
 					Toast.makeText(getApplicationContext(), "The tutorial is switched off now. You can start the tutorial at anytime by going to the settings menu.", Toast.LENGTH_LONG).show();
 				}
 			});
 
 			firstTut.show();
 		}
 
 	}
 
 	public void videoButtonClicked() {
 		Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
 
 		// start the image capture Intent
 		startActivityForResult(intent, CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE);
 
 	}
 
 	public void cameraButtonClicked() {
 		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);  
 
 		//ContentValues values = new ContentValues();  
 		ContentValues values = new ContentValues(3);  
 		values.put(MediaStore.Images.Media.DISPLAY_NAME, "testing");  
 		values.put(MediaStore.Images.Media.DESCRIPTION, "this is description");  
 		values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");  
 		imageFilePath = MainActivity.this.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);  
 		intent.putExtra(MediaStore.EXTRA_OUTPUT, imageFilePath); 
 
 		// start the image capture Intent
 		startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
 
 	}
 
 	//Creates a marker on the map based on the current location and links it with the current marker
 	public void createMarker(DBMarker marker)
 	{				
 		sliding.close();
 		//Call get location function
 		LatLng currentLocation = lastPosition;
 
 		//If location is not null, create a marker at the location and draw a line from that location to lat:0 long:0
 		if (currentLocation != null)
 		{
 			
 			//Get current location and create a LatLng object
 			Marker locationMarker = map.addMarker(new MarkerOptions().position(currentLocation));
 
 			//Create a new GPS object to attach to the marker
 			GPS gpsObject = new GPS();
 			gpsObject.latitude = currentLocation.latitude;
 			gpsObject.longitude = currentLocation.longitude;
 			gpsObject.time = System.currentTimeMillis();
 			marker.timeStamp =gpsObject.time;
 			marker.gps = gpsObject;
 
 			//Add the marker to the list of markers and HashTable
 			markerList.add(marker);
 			markerHashTable.put(marker, locationMarker);
 			menuCloseTutorial();
 		}
 		else
 		{
 			//Create alert when location fails
 			AlertDialog.Builder builder = new AlertDialog.Builder(this);
 			builder.setMessage("Location not found.");
 			builder.setTitle("Error");
 			builder.setNeutralButton("Close", null);
 			AlertDialog dialog = builder.create();
 			dialog.show();
 		}
 	}
 
 	//Removes the marker from the map
 	public void deleteMarker(DBMarker marker)
 	{
 		//Log delete marker
 		Log.w(TAG, "Delete Marker, Time: " + System.currentTimeMillis());
 
 		Marker locationMarker = markerHashTable.get(marker);
 		markerHashTable.remove(marker);
 		locationMarker.remove();
 	}
 
 	//Get location every 3 seconds and save a GPS object to the GPS list.
 	//Add a line to the new location
 	public void startTracking()
 	{
 		//Log start route
 		Log.w(TAG, "Start Tracking Route, Time: " + System.currentTimeMillis());
 
 		thisRoute = new DBRoute();
 		thisRoute.timeStart = System.currentTimeMillis();
 		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, gpsTimeFreq, 0, locListener);
 		Toast.makeText(this, "Starting Route", Toast.LENGTH_LONG).show();
 	}
 
 	//Stops tracking and saves the route data to the database
 	public void stopTracking(String routeName, String notes, String location)
 	{
 		//Log stop route
 		Log.w(TAG, "Stop Tracking Route, Time: " + System.currentTimeMillis());
 
 		if(lastPosition== null)return;
 		endMarker = map.addMarker(new MarkerOptions()
 		.position(new LatLng(lastPosition.latitude, lastPosition.longitude))
 		.title("Ending marker"));
 		locationManager.removeUpdates(locListener);
 
 		thisRoute.timeEnd = System.currentTimeMillis();
 		thisRoute.routeName = routeName;
 		thisRoute.notes = notes;
 		thisRoute.location = location;
 
 		database.addNewRoute(gpsList, markerList, thisRoute.timeStart, thisRoute.timeEnd, thisRoute.notes, thisRoute.routeName, thisRoute.location);
 		Toast.makeText(this, "Ending Route", Toast.LENGTH_LONG).show();
 	}
 
 	//Draws a line on the map for a given linked list of GPS objects
 	public void drawPath(LinkedList<GPS> gpsList)
 	{
 
 		PolylineOptions lineOptions = new PolylineOptions();
 		for(int i = 0; i < gpsList.size(); i++)
 		{
 			GPS currentLocation = gpsList.get(i);
 			LatLng thisLocation = new LatLng(currentLocation.latitude, currentLocation.longitude);
 			lineOptions.add(thisLocation);	
 		}
 
 		map.addPolyline(lineOptions);
 	}
 
 
 	//Get location object
 	public Location getLocation()
 	{        
 		Location locationGPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
 		Location locationNet = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
 
 		long GPSLocationTime = 0;
 		if (locationGPS != null)
 			GPSLocationTime = locationGPS.getTime();
 
 		long NetLocationTime = 0;
 		if (locationNet != null)
 			NetLocationTime = locationNet.getTime();
 
 		if ( 0 < GPSLocationTime - NetLocationTime )
 			return locationGPS;
 		else
 			return locationNet;
 	}
 
 
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
 			if (resultCode == RESULT_OK) {
 
 				DBMarker marker = new DBMarker();
 				if(imageFilePath != null){
 					System.out.println(imageFilePath.toString());
 					buttonVisibility();
 					marker.pictureLink = imageFilePath.toString();
 				}else{
 					marker.pictureLink = "failed";
 				}
 
 				//Log create picture marker
 				Log.w(TAG, "Create Picture Marker, Time: " + System.currentTimeMillis());
 
 				createMarker(marker);
 
 			} else if (resultCode == RESULT_CANCELED) {
 				// User cancelled the image capture
 			} else {
 				// Image capture failed, advise user
 			}
 		}
 
 		if (requestCode == CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE) {
 			if (resultCode == RESULT_OK) {
 				// Video captured and saved to fileUri specified in the Intent
 				saveDataInMarker(data,CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE);
 				Toast.makeText(this, "Video saved to:\n" +
 						data.getData(), Toast.LENGTH_LONG).show();
 			} else if (resultCode == RESULT_CANCELED) {
 				// User cancelled the video capture
 			} else {
 				// Video capture failed, advise user
 			}
 		}
 	}
 
 	private void saveDataInMarker(Intent data, int code) {
 
 		DBMarker newMarker = new DBMarker();
 		newMarker.timeStamp = System.currentTimeMillis();
 
 		if (code == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE){
 			newMarker.pictureLink = data.getData().toString();
 		}
 		else if (code == CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE){
 			newMarker.videoLink = data.getData().toString();
 		}
 
 		createMarker(newMarker);
 
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_main, menu);
 
 		return true;
 	}
 
 	private void initializeMarkerDetailsButtons() {
 		deleteVideo = (Button) findViewById(R.id.deleteVideo);
 		//ivPicture = (ImageView) findViewById(R.id.ivPicture); // ------debug purpose
 		deletePhoto = (Button) findViewById(R.id.deletePicture);
 		deleteAudio = (Button) findViewById(R.id.deleteAudio);
 		trashMarker = (ImageButton) findViewById(R.id.trashButton);
 		closeMarker = (ImageButton) findViewById(R.id.closeButton);
 		pictureButton = (ImageButton) findViewById(R.id.pictureButton);
 		videoButton = (ImageButton) findViewById(R.id.videoButton);
 		audioButton = (ImageButton) findViewById(R.id.audioButton);
 	}
 
 	public void passMarker(DBMarker marker){
 		System.out.println("In pass marker function");
 		sliding.close();
 		//Log edit marker
 		Log.w(TAG, "Edit Marker, Time: " + System.currentTimeMillis());
 
 		theMarker = marker;
 		String title = marker.name == null? "Marker": marker.name;
 		markerTitleText.setText(title);
 
 		Date d = new Date(marker.timeStamp);
 		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
 		String time = sdf.format(d);
 		markerDateText.setText(time);
 
 		commentText.setText(theMarker.hasText() ? theMarker.text:"Add a comment...");
 		markerDetails.setVisibility(FrameLayout.VISIBLE);
 
 		editMarkerTutorial();
 		//markerDetails.getParent()
 	}
 
 	private void editMarkerTutorial() {
 		if (editMarker) {
 		AlertDialog.Builder tut=new AlertDialog.Builder(MainActivity.this);
 		tut.setMessage("You can edit your data here. Click icon to add a picture, " +
 				"video or audio. Click on the text field to add a comment. " +
 				"Click on the Trash icon to erase data.");
 
 		tut.setNeutralButton("Continue", new DialogInterface.OnClickListener() {
 
 			@Override
 			public void onClick(DialogInterface dialog, int which) {}
 		});
 		editMarker = false;
 		tut.show();
 		}
 	}
 
 	public void setMarkerDetailPanelVisibile(){
 
 		markerDetails.setVisibility(FrameLayout.VISIBLE);
 	}
 
 	private void initializeMarkerDetailsText() {
 		markerTitleText = (TextView) findViewById(R.id.markerTitle);
 		markerTitleText.setOnClickListener(new OnClickListener(){
 
 			@Override
 			public void onClick(View arg0) {
 				// custom dialog
 				final Dialog dialog = new Dialog(arg0.getContext());
 				dialog.setContentView(R.layout.alert_dialog);
 				dialog.setTitle("Set Title");
 
 				Button dialogButton = (Button) dialog.findViewById(R.id.dialogButtonConfirm);
 				// if button is clicked, close the custom dialog
 				dialogButton.setOnClickListener(new OnClickListener() {
 					@Override
 					public void onClick(View v) {
 						theMarker.name = ((TextView) dialog.findViewById(R.id.editText1)).getText().toString();
 						markerTitleText.setText(theMarker.name);
 						dialog.dismiss();
 					}
 				});
 
 				dialog.show();
 
 			}
 
 
 		});
 		markerDateText = (TextView) findViewById(R.id.markerDate);
 		commentText = (TextView) findViewById(R.id.comment);
		commentText.setText("Comment...");
 	}
 
 	private void setMarkerDetailListeners() {
 		setTrashListener();
 		setCloseListener();
 		setPictureListener();
 		setVideoListener();
 		//setAudioListener();
 		setDeleteVideoListener();
 		setDeletePhotoListener();
 		setDeleteAudioListener();
 	}
 
 
 
 
 	private void setCloseListener() {
 		// Action for Close button
 		closeMarker.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				if(theMarker != null)
 					theMarker.text = commentText.getText().toString();
 				markerDetails.setVisibility(FrameLayout.GONE);
 			}
 		});
 	}
 
 	private void setTrashListener() {
 		// Action for Trash button
 		trashMarker.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				if(theMarker!= null){
 					//TODO Add prompt to confirm deletion of marker
 					//TODO If confirmed, markerDetials.setVisibility(FrameLayout.GONE);
 					deleteMarker(theMarker);
 					markerDetails.setVisibility(FrameLayout.VISIBLE);
 				}
 			}
 		});
 	}
 
 	// private void setTrashListener() {
 	//  // Action for Trash button
 	//  trashMarker.setOnClickListener(new OnClickListener() {
 	//
 	//
 	//   @Override
 	//   public void onClick(View v) {
 	//    Intent takePhoto = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
 	//
 	//    // After photo is taken, pass it to the result method
 	//    startActivityForResult(takePhoto, 0);
 	//   }
 	//  });
 	// }
 
 	private void setDeleteAudioListener() {
 		// Action for Media button when no data
 		deleteAudio.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				theMarker.audioLink = null;
 			}
 		});
 	}
 
 	private void setDeletePhotoListener() {
 		// Action for Media button when no data
 		deletePhoto.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				theMarker.pictureLink = null;
 			}
 		});
 	}
 
 	private void setDeleteVideoListener() {
 		// Action for Media button when no data
 		deleteVideo.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				theMarker.videoLink = null;
 			}
 		});
 	}
 
 	private void setAudioListener() {
 		// Action for Media button when no data
 		audioButton.setOnClickListener(new OnClickListener() {
 
 
 			@Override
 			public void onClick(View v) {
 				Intent takePhoto = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
 
 				// After photo is taken, pass it to the result method
 				startActivityForResult(takePhoto, 0);
 			}
 		});
 
 	}
 
 	private void setVideoListener() {
 		// Action for Media button when no data
 		videoButton.setOnClickListener(new OnClickListener() {
 			public void onClick(View view) {
 				videoButtonClicked();
 			}
 		});
 	}
 
 	private void setPictureListener() {
 		// Action for Media button when no data
 		pictureButton.setOnClickListener(new OnClickListener() {
 			public void onClick(View view) {
 				cameraButtonClicked();
 			}
 		});
 	}
 	
 	
 	//TODO ADD THIS EVERYWHERE
 	 @SuppressLint("NewApi")
 	private void buttonVisibility(){
 		  
 		  deletePhoto.setVisibility(theMarker.hasPic()? 0:1);
 		  deleteVideo.setVisibility(theMarker.hasVid()? 0:1);
 		  deleteAudio.setVisibility(theMarker.hasAudio()? 0:1);
 		  pictureButton.setAlpha(theMarker.hasPic()? (float)1:(float)0.5);
 		  videoButton.setAlpha(theMarker.hasVid()? (float)1:(float)0.5);
 		  audioButton.setAlpha(theMarker.hasAudio()? (float)1:(float)0.5);
 	}
 
 
 }
