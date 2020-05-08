 package se.chalmers.project14.main;
 
 /*
  * Copyright (c) 2012 Henrik Andersson, Anton Palmqvist, Tomas Selldn and Marcus Tyrn
  * See the file license.txt for copying permission.
  */
 
 import java.security.acl.LastOwnerException;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import org.xml.sax.Parser;
 
 import se.chalmers.project14.database.DatabaseHandler;
 import se.chalmers.project14.model.Door;
 import utils.CoordinateParser;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.AlertDialog.Builder;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Path;
 import android.graphics.Point;
 import android.graphics.PorterDuff.Mode;
 import android.graphics.drawable.Drawable;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.os.Looper;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.MotionEvent;
 import android.widget.Toast;
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.ItemizedOverlay;
 import com.google.android.maps.MapView;
 import com.google.android.maps.MyLocationOverlay;
 import com.google.android.maps.Overlay;
 import com.google.android.maps.OverlayItem;
 import com.google.android.maps.MyLocationOverlay;
 import com.google.android.maps.Projection;
 
 /**
  * 
  *Class containing and managing the overlays containing information such as location, 
  *destination and buildings. Also handling all touchrelated actions of the mapview.
  *These touchevents are managing and updating the overlays, for example changing the 
  *route if the destination is changed.
  * 
  * @version
  * 
  *          0.2 21 Oktober 2012
  * @author
  * 
  *         Anton Palmqvist and Henrik Andersson
  */
 
 public class OverlayHolder extends Overlay implements LocationListener{
 	//private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();
 	private Context context;
 	private long touchStart;
 	private float touchX, touchY;
 	private long touchTimeDown;
 	private MapView mapView;
 	private GeoPoint myGeoPoint, destGeoPoint, focusedGeoPoint;
 	private MarkerOverlay sourceOverlay, destOverlay;
 	private CoordinateParser coordinateParser = CoordinateParser.getInstance();
 	private MyLocationOverlay myLocationOverlay;
 	private boolean isClassroomChosen=false;
 	private LocationManager locManager;
 	private Projection projection;
 	private boolean useGpsData = true;
 	private Timer touchTimer;
 	private boolean holding;
 	private DatabaseHandler db;
 	private List<Door> doors;
 	private List<Door> editDoors;
 	private List<Door> maskinDoors;
 	private List<Door> haDoors;
 	private List<Door> hbDoors;
 	private List<Door> hcDoors;
 	private String chosenBuildingName;
 	private int [] chosenBuildingCoordinates;
 
 	/**
 	 * Creates an instance of the OverlayHolder.
 	 * @param context
 	 * @param mapView
 	 * @param intent
 	 */
 	public OverlayHolder(Context context, MapView mapView, Intent intent) {
 		super();
 		this.context = context;
 		this.mapView=mapView;
 		projection = mapView.getProjection();
 		db = new DatabaseHandler(context);
 		doors =  db.getAllDoorsAndBuildings();
 		editDoors = new ArrayList<Door>();
 		maskinDoors = new ArrayList<Door>();
 		haDoors = new ArrayList<Door>();
 		hbDoors = new ArrayList<Door>();
 		hcDoors = new ArrayList<Door>();
 
 		//Checks if a specific classroom has been chosen
 		if (intent.getStringExtra(ChooseLocationActivity.CTHBUILDING.toString()) != null) {
 			isClassroomChosen = true;
 			drawChosenEntrances(intent);
 		}
 
 		else{
 			drawAllEntrances(intent);
 		}
 
 		/* Using the LocationManager class to obtain GPS-location */
 		locManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
 		startGpsUpdates();
 
 
 		/*
 		 * Using the MyLocationOverlay-class to add users current position to
 		 * map-view
 		 */
 		myLocationOverlay = new MyLocationOverlay(context,
 				mapView);
 		mapView.getOverlays().add(myLocationOverlay);
 		myLocationOverlay.enableCompass(); // Adding a compass to the map
 
 		// Creates a position-marker avatar
 		Drawable avatar = mapView.getResources().getDrawable(R.drawable.anton);
 		sourceOverlay = new MarkerOverlay(avatar, mapView);
 
 		// Creates a destination flag overlay
 		Drawable destFlag = mapView.getResources().getDrawable(R.drawable.destination_flag);
 		destOverlay = new MarkerOverlay(destFlag, mapView);
 
 		//Adds the created overlays		
 		mapView.getOverlays().add(sourceOverlay);
 		mapView.getOverlays().add(destOverlay);
 	}
 
 	/**
 	 * Containg all touchrelated events. The time of the press is calculated and determining 
 	 * if it is a longpress or a tap making an action accordingly.
 	 * @param event event to get data such as time and position of the press
 	 * @param m MapView
 	 */
 	@Override
 	public boolean onTouchEvent(MotionEvent event, MapView m) {
 		// When user touches the screen
 		holding=true;
 		if (event.getAction() == MotionEvent.ACTION_DOWN) {
 			touchX = event.getX(); //The position of the finger
 			touchY = event.getY();
 			touchTimeDown = event.getEventTime(); //The time of the down press
 			/*
 			 * Creating a timer scheduling a delayed startup after 600 ms pressed on the view
 			 */
 			touchTimer = new Timer();
 			touchTimer.schedule(new TimerTask(){ 
 				public void run(){
 					if(holding){
 						((Activity) context).runOnUiThread(new Runnable() {//Needed to run in UI-thread
 							public void run(){
 								launchMapFunctions();
 								cancel();
 							}
 						});
 					}
 				}}, 600);
 		}
 		//When moving or not moving the finger on the screen, in the middle of pressing and releasing
 		else if (event.getAction() == MotionEvent.ACTION_MOVE) {
 			if(!isSameFocus(event.getX(), event.getY())){
 				holding=false; //if the finger is moved the action is no longer consider a hold
 			}
 		}
 		// When screen is released
 		else if (event.getAction() == MotionEvent.ACTION_UP) {
 			touchTimer.cancel();
 			holding=false;
 			if(event.getEventTime()-touchTimeDown<=200){//Checking that press is below 200 ms, aka a tap
 				boolean isDoorFound = false;
 				while (!isDoorFound){
 					if(isClassroomChosen){
 						isDoorFound = launchDoorFunctions(chosenBuildingName, chosenBuildingCoordinates);
 					}
 					else{
 					isDoorFound = launchDoorFunctions(haDoors);
 					isDoorFound = launchDoorFunctions(hbDoors);
 					isDoorFound = launchDoorFunctions(hcDoors);
 					isDoorFound = launchDoorFunctions(editDoors);
 					isDoorFound = launchDoorFunctions(maskinDoors);
 					}
 					
 					isDoorFound = true;
 				}
 			}
 			return true;
 		}
 		return false;
 	}
 	/**
 	 * Method that launches a dialog for the door that is clicked on.
 	 */
 	private boolean launchDoorFunctions(List<Door> doors){
 		String buildingName = doors.get(0).getBuilding();
 		int [] doorCoordinates = coordinateParser.parseCoordinatesFromDoors(doors);
 		return launchDoorFunctions(buildingName, doorCoordinates);
 	}
 	
 	/**
 	 * Method that launches a dialog for the door that is clicked on.
 	 */
 	private boolean launchDoorFunctions(String building, int [] coordinates){
 		for(int i=0; i<coordinates.length; i+=2 ){
 			GeoPoint doorGeoPoint = new GeoPoint(coordinates[i], coordinates[i+1]);
 			Point doorPoint = new Point();
 			projection.toPixels(doorGeoPoint, doorPoint);//converting the doors GeoPoints to Points
 			if(isSameFocus(doorPoint.x, doorPoint.y)){
 				AlertDialog.Builder buildingOptions = new AlertDialog.Builder(context);
 				buildingOptions.setTitle("Building options");
 				buildingOptions.setMessage("Entrance to " + building);
 				buildingOptions.setNegativeButton("Go back to map", new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int which) {
 						// do nothing and go back to mapview
 					}
 				});
 				buildingOptions.setNeutralButton("Enter Building", new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int which) {
 						//The Indoorview of the building of the chosen door is opened
 						Intent intent = new Intent(context, se.chalmers.project14.enterBuilding.FloorViewer.class);
 						context.startActivity(intent);
 					}
 				});
 				buildingOptions.setPositiveButton("Set destination", new DialogInterface.OnClickListener(){
 					public void onClick(DialogInterface dialog, int which) {						
 						//updating the destination-GeoPoint
 						destGeoPoint = mapView.getProjection().fromPixels((int)touchX, (int)touchY);
 						//Adding a destination marker
 						OverlayItem destinationItem = new OverlayItem(destGeoPoint, "Destinationmarker", "This is the chosen destination");
 						destOverlay.setMarker(destinationItem);
 						mapView.invalidate();
 					}
 				});
 				buildingOptions.show();
 				return true;
 			}
 
 		}
 		return false;
 	}
 	
 	/**
 	 * Checking if the finger has been kept in the same position as on down press.
 	 * @param touchStopX The x-wise position focused on down press
 	 * @param touchStopY The y-wise position focused on down press
 	 * @return True if still at same position, given the tolerance of +/-20 pixels
 	 */
 	private boolean isSameFocus(float touchStopX, float touchStopY){
 		if(touchX <= touchStopX+20 && touchX >= touchStopX-20 
 				&& touchY <= touchStopY+20 && touchY >= touchStopY-20){
 			return true;
 		}
 		else{
 			return false;
 		}
 	}
 	/**
 	 * Method that launches a dialog for the position being clicked on.
 	 */
 	private void launchMapFunctions(){
 		focusedGeoPoint = mapView.getProjection().fromPixels((int)touchX, (int)touchY);
 		AlertDialog.Builder options = new AlertDialog.Builder(context);
 		options.setTitle("Options");
 		options.setMessage("Coordinates:\nLatitude: " + focusedGeoPoint.getLatitudeE6()/1E6 + "\nLongitude: " 
 				+ focusedGeoPoint.getLongitudeE6()/1E6 + "\n\nWhat do you want to do?");
 		options.setNegativeButton("Set destination", new DialogInterface.OnClickListener(){
 			public void onClick(DialogInterface dialog, int which) {						
 				//updating the destination-GeoPoint
 				destGeoPoint = mapView.getProjection().fromPixels((int)touchX, (int)touchY);
 				//Adding a destination marker
 				OverlayItem destinationItem = new OverlayItem(destGeoPoint, "Destinationmarker", "This is the chosen destination");
 				destOverlay.setMarker(destinationItem);
 				mapView.invalidate();
 			}
 		});
 		options.setNeutralButton("Set location", new DialogInterface.OnClickListener(){
 			public void onClick(DialogInterface dialog, int which) {
 				if(!useGpsData){
 					//myGeoPoint set from coordinates on focus
 					myGeoPoint = mapView.getProjection().fromPixels((int)touchX, (int)touchY);
 					//Adding a location marker at the manually set position
 					OverlayItem sourceItem = new OverlayItem(myGeoPoint, "Locationmarker", "This is the recent location");
 					sourceOverlay.setMarker(sourceItem);
 					mapView.invalidate();
 				}
 				else{
 					Toast.makeText(context, "Turn of GPS-location to set manual position", Toast.LENGTH_SHORT).show();
 				}
 			}
 		});
 		options.setPositiveButton("Back to Map", new DialogInterface.OnClickListener(){
 			public void onClick(DialogInterface dialog, int which) {
 				Toast.makeText(context, "Back to map", Toast.LENGTH_SHORT).show();
 			}
 		});
 		options.show();
 	}
 	/**
 	 * Method for getting the destination overlay
 	 * @return destination overlay
 	 */
 	public MarkerOverlay getDestOverlay(){
 		return destOverlay;
 	}
 
 	//TODO Henke fixar Javadoc
 	private Drawable setBuildingIcon(String s){
 		if(s.equals("EDIT-huset")){
 			return mapView.getResources().getDrawable(R.drawable.edit);
 		}
 		else if (s.equals("Maskinhuset")){
 			return mapView.getResources().getDrawable(R.drawable.m);
 		}
 		else if (s.equals("HA")){
 			return mapView.getResources().getDrawable(R.drawable.ha);
 		}
 		else if (s.equals("HB")){
 
 			return mapView.getResources().getDrawable(R.drawable.hb);
 		}
 		else if (s.equals("HC")){
 			return mapView.getResources().getDrawable(R.drawable.hc);
 		}
 		return null ;
 	}
 
 	/**
 	 * Method invoked if the location is updated by the GPS.
 	 * @param location the updated location obtained by the GPS
 	 */
 	public void onLocationChanged(Location location) {
 
 		if(useGpsData){ //if GPS-data is used the location is set automatically
 			//TODO Make the the location-toast optional by a choice in settings
 			String text = "Min nuvarande position r: \nLatitud: " + location.getLatitude() + 
 					"\nLongitud: " + location.getLongitude();		
 			Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
 
 			//Obtaining the latitude and longitude
 			int lat = (int) (location.getLatitude() * 1E6);
 			int lng = (int) (location.getLongitude() * 1E6);
 			//myGeoPoint is being set by the collected longitude and latitude
 			myGeoPoint = new GeoPoint(lat, lng);
 			//Adding a location marker at the obtained geoPoint
 			OverlayItem sourceItem = new OverlayItem(myGeoPoint, "Locationmarker", "This is the recent location");
 			sourceOverlay.setMarker(sourceItem);
 			mapView.invalidate();
 		}
 		else{ //if GPS-data is not used the location is set manually
 			Toast.makeText(context, "Manually set location", Toast.LENGTH_SHORT).show();
 		}
 	}
 	
 	public void onProviderDisabled(String provider) {
 		Toast.makeText(context, "GPS Disabled", Toast.LENGTH_SHORT).show();
 	}
 
 	public void onProviderEnabled(String provider) {
 		Toast.makeText(context, "GPS Enabled", Toast.LENGTH_SHORT).show();
 	}
 
 	public void onStatusChanged(String provider, int status, Bundle extras) {
 	}
 	
 	//TODO Henke lgger till javadoc
 	private void drawChosenEntrances (Intent intent) {
 		// Retrieves info about the chosen classroom from the database
 		String cthLectureRoom = intent
 				.getStringExtra(ChooseLocationActivity.CTHLECTURE_ROOM);
 		chosenBuildingName = intent
 				.getStringExtra(ChooseLocationActivity.CTHBUILDING);
 		chosenBuildingCoordinates = coordinateParser.parseCoordinatesFromString(intent
 				.getStringExtra(ChooseLocationActivity.CTHDOOR_COORDINATES));
 		/*for the moment, never used varible
 		int [] cthBuildingCoordinates = coordinateParser.parseCoordinates(intent
 		.getStringExtra(ChooseLocationActivity.CTHBUILDING_COORDINATES));*/
 
 		int cthBuildingFloor = Integer.parseInt(intent
 				.getStringExtra(ChooseLocationActivity.CTHBUILDING_FLOOR));
 
 		// Creates clickable map overlays for the chosen classrooms closest entrances
 		Drawable buildingIcon = setBuildingIcon(chosenBuildingName);
 		BuildingOverlay buildingOverlay = new BuildingOverlay(buildingIcon, context);
 		for (int i=0; i<chosenBuildingCoordinates.length;i +=2 ){
 			GeoPoint entranceGeoPoint = new GeoPoint(chosenBuildingCoordinates[i], chosenBuildingCoordinates[i+1]);
 			OverlayItem entranceOverlayItem = new OverlayItem(entranceGeoPoint,
 					"Entrance" + " " + chosenBuildingName, "Classrooms close to this entrance:");
 			buildingOverlay.addOverlay(entranceOverlayItem);
 			mapView.getOverlays().add(buildingOverlay);
 		}
 	}
 	private void drawAllEntrances(Intent intent){
 
 		// Splits the list of doors into a list of each building
 		for(int i=0; i<doors.size();i++){
 			if(doors.get(i).getBuilding().equals("EDIT-huset")){
 				editDoors.add(doors.get(i));
 			}
 			else if (doors.get(i).getBuilding().equals("Maskinhuset")){
 				maskinDoors.add(doors.get(i));
 			}
 			else if (doors.get(i).getBuilding().equals("HA")){
 				haDoors.add(doors.get(i));
 			}
 			else if (doors.get(i).getBuilding().equals("HB")){
 				hbDoors.add(doors.get(i));
 			}
 			else if (doors.get(i).getBuilding().equals("HC")){
 				hcDoors.add(doors.get(i));
 			}
 		}
 		//Adds the overlay into the mapview				
 
 		mapView.getOverlays().add(generateBuildingOverlay(maskinDoors));
 		mapView.getOverlays().add(generateBuildingOverlay(haDoors));
 		mapView.getOverlays().add(generateBuildingOverlay(hbDoors));
 		mapView.getOverlays().add(generateBuildingOverlay(hcDoors));
 		mapView.getOverlays().add(generateBuildingOverlay(editDoors));
 	}
 
 	//TODO Henke fixar javadoc
 	private BuildingOverlay generateBuildingOverlay(List<Door> doors){
 
 		int [] doorCoordinates = coordinateParser.parseCoordinatesFromDoors(doors);
 
 
 		// Creates clickable map overlays for the chosen classrooms closest entrances
 		Drawable buildingIcon = setBuildingIcon(doors.get(0).getBuilding());
 		BuildingOverlay buildingOverlay = new BuildingOverlay(buildingIcon, context);
 
 		for(int i = 0; i<doorCoordinates.length;i=i+2){
 			GeoPoint entranceGeoPoint = new GeoPoint(doorCoordinates[i], doorCoordinates[i+1]);
 			OverlayItem entranceOverlayItem = new OverlayItem(entranceGeoPoint,
 					"Entrance" + " " + (doors.get(0).getBuilding()), "Classrooms close to this entrance:");
 			buildingOverlay.addOverlay(entranceOverlayItem);
 
 		}
 		System.out.println(buildingOverlay.size());
 		return buildingOverlay;
 	}
 	
 	/**
 	 * Method drawing the line between the location and destination.
 	 */
 	public void draw(Canvas canvas, MapView mapview, boolean shadow){
 		super.draw(canvas, mapView, shadow);
 
 		//Customizing the paint-brush
 		Paint mPaint = new Paint();
 		mPaint.setDither(true);
 		mPaint.setColor(Color.RED);
 		mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
 		mPaint.setStrokeJoin(Paint.Join.ROUND);
 		mPaint.setStrokeCap(Paint.Cap.ROUND);
 		mPaint.setStrokeWidth(4);
 
 		Point myPoint = new Point();//Creating points to draw between
 		Point destPoint = new Point();
 		Path path1 = new Path();//Creating the path to draw
 		canvas.save();//Saving the canvas at an empty state
 		/*
 		 * If location and destination exists a line should be drawn.
 		 */
 		if(myGeoPoint!=null && destGeoPoint!=null){ 
 			projection.toPixels(myGeoPoint, myPoint);//converting GeoPoints to Points
 			projection.toPixels(destGeoPoint, destPoint);
 			path1.moveTo(myPoint.x, myPoint.y);//Moving to myPoint (my location)
 			path1.lineTo(destPoint.x,destPoint.y);//Path to destPoint (my destination)
 			canvas.drawPath(path1, mPaint);//Drawing the path
 		}
 		/*
 		 * //Called when no line shold be drawn or when a line should be removed
 		 */
 		else{
 			canvas.restore();//Retrieving the canvas from its empty state that was saved earlier
 		}
 	}
 	/**
 	 * Method to toggle the use of GPS-data on and off.
 	 */
 	public void toggleUseGpsData(){
 		useGpsData = !useGpsData;
 		String useGps = "The use of GPS-data is turned: ";
 		if(useGpsData){
 			Toast.makeText(context, useGps + "ON", Toast.LENGTH_SHORT).show();
 			startGpsUpdates();
 		}
 		else{
 			Toast.makeText(context, useGps + "OFF", Toast.LENGTH_SHORT).show();
 			stopGpsUpdates();
 		}
 	}
 	
 	/**
 	 * Method that starts the receiving of updates of from the GPS.
 	 */
 	public void startGpsUpdates(){
 		locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0,
 				this);
 	}
 	
 	/**
 	 * Method that removes the receiving of updates of from the GPS.
 	 */
 	public void stopGpsUpdates(){
 		locManager.removeUpdates(this);
 	}
 
 	/**
 	 * Method that removes the destination-marker from destOverlay and resets the destGeoPoint
 	 */
 	public void resetDestination() {
 		destOverlay.removeDestinationMarker();
 		destGeoPoint=null;		
 	}
 }
