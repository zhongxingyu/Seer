 /**
 	This file is part of Personal Trainer.
 
     Personal Trainer is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     any later version.
 
     Personal Trainer is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with Personal Trainer.  If not, see <http://www.gnu.org/licenses/>.
  */
 package se.team05.activity;
 
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 import java.util.List;
 
 import se.team05.R;
 import se.team05.content.Result;
 import se.team05.content.Route;
 import se.team05.content.Track;
 import se.team05.data.DatabaseHandler;
 import se.team05.dialog.EditCheckPointDialog;
 import se.team05.dialog.SaveRouteDialog;
 import se.team05.listener.MapLocationListener;
 import se.team05.listener.MapOnGestureListener;
 import se.team05.overlay.CheckPoint;
 import se.team05.overlay.CheckPointOverlay;
 import se.team05.overlay.RouteOverlay;
 import se.team05.service.MediaService;
 import se.team05.view.EditRouteMapView;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.drawable.Drawable;
 import android.location.Criteria;
 import android.location.Location;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.PowerManager;
 import android.os.PowerManager.WakeLock;
 import android.support.v4.app.NavUtils;
 import android.util.Log;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.Button;
 import android.widget.TextView;
 
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapActivity;
 import com.google.android.maps.MyLocationOverlay;
 import com.google.android.maps.Overlay;
 
 /**
  * This activity Presents at map to the user. It also tracks the users movement
  * and will paint the geoPointList as the user moves. This is accomplished by
  * using Google's map API.
  * 
  * @author Markus Schutzer, Patrik Thitusson, Daniel Kvist
  * 
  */
 public class RouteActivity extends MapActivity implements View.OnClickListener, EditCheckPointDialog.Callbacks,
 		SaveRouteDialog.Callbacks, CheckPointOverlay.Callbacks, MapOnGestureListener.Callbacks,
 		MapLocationListener.Callbacks
 {
 
 	private static final String TAG = "Personal trainer";
 	private ArrayList<GeoPoint> geoPointList = new ArrayList<GeoPoint>();
 	private LocationManager locationManager;
 	private String providerName;
 	private EditRouteMapView mapView;
 	private boolean started = false;
 	private MyLocationOverlay myLocationOverlay;
 	private String userSpeed = "0";
 	private String userDistance = "0";
 	private Location lastLocation;
 	private float totalDistance = 0;
 	private String lengthPresentation = DISTANCE_UNIT_METRES;
 	private String userDistanceRun = userDistance + lengthPresentation;
 	private CheckPointOverlay checkPointOverlay;
 	private EditCheckPointDialog checkPointDialog;
 	private Handler handler;
 	private Runnable runnable;
 	private int timePassed = 0;
 	private String nameOfExistingRoute;
 
 	private static String DISTANCE_UNIT_KILOMETRE = "Km";
 	private static String DISTANCE_UNIT_METRES = " metres";
 	private static float DISTANCE_THRESHOLD_EU = 1000;
 	private ArrayList<Track> selectedTracks = new ArrayList<Track>();
 	private DatabaseHandler databaseHandler;
 	private CheckPoint currentCheckPoint;
 	private Result routeResults;
 	private boolean newRoute;
 	private List<Overlay> overlays;
 	private Button stopAndSaveButton;
 	private Button startRunButton;
 	private Button startExistingRunButton;
 	private Button stopExistingRunButton;
 	private Route route;
 	private WakeLock wakeLock;
 	private TextView speedView;
 	private TextView distanceView;
 	private Intent serviceIntent;;
 
 	/**
 	 * Will present a map to the user and will also display a dot representing
 	 * the user's location. Also contains three buttons of which one
 	 * (startRunButton) will start the recording of the user's movement and will
 	 * paint the track accordingly on the map. The button stopAndSaveButton will
 	 * finish the run and save it. As of now it is recorded in the memory but
 	 * later it will have database functionality. The button addCheckPointButton
 	 * will place a checkpoint at the user's current location.
 	 */
 	@Override
 	public void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_route);
 		getActionBar().setDisplayHomeAsUpEnabled(true);
 		databaseHandler = new DatabaseHandler(this);
 		serviceIntent = new Intent(this, MediaService.class);
 		route = new Route("New route", "This is a new route");
 		newRoute = true;
 		setupMapAndLocation();
 
 		long rid = getIntent().getLongExtra(Route.EXTRA_ID, -1);
 		if (rid != -1)
 		{
 			newRoute = false;
 			initRoute(rid);
 			setTitle("Saved Route: " + nameOfExistingRoute);
 			addSavedCheckPoints(rid);
 		}
 		setupButtons();
 		mapView.postInvalidate();
 	}
 
 	/**
 	 * Sets up the map view and the location
 	 */
 	private void setupMapAndLocation()
 	{
 		distanceView = (TextView) findViewById(R.id.show_distance_textview);
 		speedView = (TextView) findViewById(R.id.show_speed_textview);
 		mapView = (EditRouteMapView) findViewById(R.id.mapview);
 		mapView.setBuiltInZoomControls(true);
 		mapView.setOnGestureListener(new MapOnGestureListener(this));
 
 		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
 		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, new MapLocationListener(this));
 
 		Criteria criteria = new Criteria();
 		criteria.setAccuracy(Criteria.ACCURACY_FINE);
 		criteria.setCostAllowed(false);
 
 		providerName = locationManager.getBestProvider(criteria, true);
 
 		if (providerName != null)
 		{
 			Log.d(TAG, "No provider: " + providerName);
 		}
 
 		overlays = mapView.getOverlays();
 		Drawable drawable = getResources().getDrawable(R.drawable.ic_launcher);
 
 		RouteOverlay routeOverlay = new RouteOverlay(geoPointList, 78, true);
 		myLocationOverlay = new MyLocationOverlay(this, mapView);
 		checkPointOverlay = new CheckPointOverlay(drawable, this);
 
 		overlays.add(routeOverlay);
 		overlays.add(myLocationOverlay);
 		overlays.add(checkPointOverlay);
 	}
 
 	/**
 	 * Adds saved checkpoints to the route that has been drawn if the user is
 	 * using an previously saved map.
 	 * 
 	 * @param rid
 	 */
 	private void addSavedCheckPoints(long rid)
 	{
 		ArrayList<Track> trackList;
 		ArrayList<CheckPoint> checkPointList = databaseHandler.getCheckPoints(rid);
 		for (CheckPoint checkPoint : checkPointList)
 		{
 			trackList = databaseHandler.getTracks(checkPoint.getId());
 			checkPoint.addTracks(trackList);
 		}
 		checkPointOverlay.setCheckPoints(checkPointList);
 	}
 
 	/**
 	 * Sets up the buttons in the view.
 	 */
 	private void setupButtons()
 	{
 		Button addCheckPointButton = (Button) findViewById(R.id.add_checkpoint);
 		Button showResultButton = (Button) findViewById(R.id.show_result_button);
 
 		stopAndSaveButton = (Button) findViewById(R.id.stop_and_save_button);
 		startRunButton = (Button) findViewById(R.id.start_run_button);
 		startExistingRunButton = (Button) findViewById(R.id.start_existing_run_button);
 		stopExistingRunButton = (Button) findViewById(R.id.stop_existing_run_button);
 
 		if (newRoute)
 		{
 			stopAndSaveButton.setOnClickListener(this);
 			startRunButton.setOnClickListener(this);
 			addCheckPointButton.setOnClickListener(this);
 		}
 		else
 		{
 			startExistingRunButton.setOnClickListener(this);
 			startExistingRunButton.setVisibility(View.VISIBLE);
 			stopExistingRunButton.setOnClickListener(this);
 
 			showResultButton.setOnClickListener(this);
 			showResultButton.setVisibility(View.VISIBLE);
 
 			stopAndSaveButton.setVisibility(View.GONE);
 			startRunButton.setVisibility(View.GONE);
 			addCheckPointButton.setVisibility(View.GONE);
 		}
 	}
 
 	/**
 	 * Gets route information from the database and draws an overlay on the map
 	 * view if the user is using a previously saved map.
 	 * 
 	 * @param id
 	 *            the route id
 	 */
 	private void initRoute(long id)
 	{
 		ArrayList<GeoPoint> geoPoints = databaseHandler.getGeoPoints(id);
 		route = databaseHandler.getRoute(id);
 		route.setGeoPoints(geoPoints);
 
 		ArrayList<CheckPoint> checkPoints = databaseHandler.getCheckPoints(id);
 		route.setCheckPoints(checkPoints);
 
 		for (CheckPoint checkPoint : checkPoints)
 		{
 			checkPoint.addTracks(databaseHandler.getTracks(checkPoint.getId()));
 		}
 
 		RouteOverlay routeOverlay = new RouteOverlay(geoPoints, 23, true);
 		overlays.add(routeOverlay);
 		nameOfExistingRoute = route.getName();
 	}
 
 	/**
 	 * This is called when the user has selected a media from the media
 	 * selection activity. A list of tracks is then passed back as a result
 	 * which this method then saves into the database.
 	 */
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data)
 	{
 		super.onActivityResult(requestCode, resultCode, data);
 		if (requestCode == MediaSelectorActivity.REQUEST_MEDIA && resultCode == RESULT_OK)
 		{
 			selectedTracks = data.getParcelableArrayListExtra(MediaSelectorActivity.EXTRA_SELECTED_ITEMS);
 			currentCheckPoint.addTracks(selectedTracks);
 		}
 	}
 
 	/**
 	 * Unused method, must implement this because of MapActivity inheritance.
 	 * Might be implemented in a later stage.
 	 */
 	@Override
 	protected boolean isRouteDisplayed()
 	{
 		return false;
 	}
 
 	/**
 	 * This will be called when user changes location. It will create a new
 	 * Geopoint consisting of longitude and latitude represented by integers and
 	 * put it in a list (geoPointList).
 	 * 
 	 * @param location
 	 *            the new location of the user
 	 */
 	@Override
 	public void updateLocation(Location location)
 	{
 		GeoPoint geoPoint;
 		GeoPoint currentGeoPoint;
 		if (started)
 		{
 			currentGeoPoint = new GeoPoint((int) (location.getLatitude() * 1E6), (int) (location.getLongitude() * 1E6));
 			geoPointList.add(currentGeoPoint);
 			userSpeed = (3.6 * location.getSpeed()) + DISTANCE_UNIT_KILOMETRE + "/h";
 
 			if (lastLocation != null)
 			{
 				totalDistance += lastLocation.distanceTo(location);
 				if (totalDistance >= DISTANCE_THRESHOLD_EU)
 				{
 					lengthPresentation = DISTANCE_UNIT_KILOMETRE;
 					userDistance = new DecimalFormat("#.##").format(totalDistance / 1000);
 				}
 				else
 				{
 					userDistance = "" + (int) totalDistance;
 				}
 				userDistanceRun = userDistance + lengthPresentation;
 			}
 
 			if (!newRoute)
 			{
 				for (CheckPoint checkPoint : route.getCheckPoints())
 				{
 					geoPoint = checkPoint.getPoint();
 					if (MapLocationListener.getDistance(currentGeoPoint, geoPoint) <= checkPoint.getRadius())
 					{
 						if (checkPoint != currentCheckPoint)
 						{
 							stopService(serviceIntent);
 							serviceIntent.putExtra(MediaService.DATA_PLAYLIST, checkPoint.getTracks());
 							serviceIntent.setAction(MediaService.ACTION_PLAY);
 							try
 							{
 								startService(serviceIntent);
 							}
 							catch (Exception e)
 							{
 								Log.e(TAG, "Could not start media service: " + e.getMessage());
 							}
 							currentCheckPoint = checkPoint;
 						}
 
 						break;
 					}
 				}
 			}
 
 			lastLocation = location;
 			speedView.setText(userSpeed);
 			distanceView.setText(userDistanceRun);
 			mapView.postInvalidate();
 		}
 	}
 
 	/**
 	 * When our activity resumes, we want to register for location updates.
 	 */
 	protected void onResume()
 	{
 		super.onResume();
 		myLocationOverlay.enableMyLocation();
 	}
 
 	/**
 	 * When our activity pauses, we want to remove listening for location
 	 * updates
 	 */
 	protected void onPause()
 	{
 		super.onPause();
 		myLocationOverlay.disableMyLocation();
 	}
 
 	/**
 	 * Button listener for this activity. Will activate the desired outcome of
 	 * any of the three buttons. In the case of Start Run the button will
 	 * disappear and will be replaced by a "Stop Run"-button, start run till
 	 * also start the timer and the recording of the user's locations and start
 	 * drawing his or hers route on the map. If the user presses the Stop
 	 * Run-button the recording will stop and the user will be prompted to
 	 * either save or discard this run. This will also stop the timer. The add
 	 * checkpoint will place a checkpoint at the users current location similar
 	 * to the single tap implementation.
 	 * 
 	 * @param v
 	 *            the button being pressed.
 	 */
 	@Override
 	public void onClick(View v)
 	{
 		switch (v.getId())
 		{
 			case R.id.start_run_button:
 				acquireWakeLock();
 				started = true;
 				startRunButton.setVisibility(View.GONE);
 				stopAndSaveButton.setVisibility(View.VISIBLE);
 				startTimer();
 				break;
 			case R.id.stop_and_save_button:
 				handler.removeCallbacks(runnable);
 				routeResults = new Result(-1, -1, timePassed, (int) totalDistance, 0);
 				SaveRouteDialog saveRouteDialog = new SaveRouteDialog(this, this, routeResults);
 				saveRouteDialog.show();
 				releaseWakeLock();
 				break;
 			case R.id.add_checkpoint:
 				if (myLocationOverlay.isMyLocationEnabled())
 				{
 					GeoPoint geoPoint = myLocationOverlay.getMyLocation();
 					if (geoPoint != null)
 					{
 						createCheckPoint(geoPoint);
 					}
 				}
 				break;
 			case R.id.start_existing_run_button:
 				acquireWakeLock();
 				started = true;
 				startExistingRunButton.setVisibility(View.GONE);
 				stopExistingRunButton.setVisibility(View.VISIBLE);
 				timePassed = 0;
 				startTimer();
 				break;
 			case R.id.show_result_button:
 				break;
 			case R.id.stop_existing_run_button:
 				handler.removeCallbacks(runnable);
 				routeResults = new Result(route.getId(), (int) System.currentTimeMillis() / 1000, timePassed,
 						(int) totalDistance, 0);
 				databaseHandler.saveResult(routeResults);
 				stopExistingRunButton.setVisibility(View.GONE);
 				startExistingRunButton.setVisibility(View.VISIBLE);
 				stopService(serviceIntent);
 				releaseWakeLock();
 				break;
 		}
 	}
 
 	/**
 	 * Starts the timer that is used to let the user know for how long they have
 	 * been using the route.
 	 */
 	private void startTimer()
 	{
 		runnable = new Runnable()
 		{
 			@Override
 			public void run()
 			{
 				timerTick();
 				handler.postDelayed(this, 1000);
 			}
 		};
 		handler = new Handler();
 		handler.postDelayed(runnable, 0);
 	}
 
 	/**
 	 * Method that gets called to update the UI with how much time that has
 	 * passed and presents this to the user. Will use field timePassed to
 	 * determine time while not alteringthe timePassed variable if we want to
 	 * pass that value to the database.
 	 */
 	private void timerTick()
 	{
 		int seconds = timePassed % 60;
 		int minutes = timePassed / 60;
 		TextView timeView = (TextView) findViewById(R.id.show_time_textview);
 		String result = String.format(" %02d:%02d", minutes, seconds);
 		timeView.setText(result);
 		timePassed++;
 	}
 
 	/**
 	 * This method deletes the checkpoint with all its tracks from the database.
 	 * It also calls on the checkpoint overlay to delete its current selected
 	 * checkpoint from the view. We then need to invalidate the map view for it
 	 * to update.
 	 */
 	@Override
 	public void onDeleteCheckPoint(long checkPointId)
 	{
 		databaseHandler.deleteCheckPoint(checkPointId);
 		databaseHandler.deleteTracksByCid(checkPointId);
 		checkPointOverlay.deleteCheckPoint();
 		mapView.postInvalidate();
 	}
 
 	/**
 	 * Callback from the checkpoint dialog that tells the activity that the user
 	 * has pressed the save button and thus the checkpoint with all its data
 	 * should now be saved. This method also saves the "tracks" that are related
 	 * to the checkpoint.
 	 */
 	@Override
 	public void onSaveCheckPoint(CheckPoint checkPoint)
 	{
 		long cid = checkPoint.getId();
 		if (cid > 0)
 		{
 			databaseHandler.updateCheckPoint(checkPoint);
 			databaseHandler.deleteTracksByCid(cid);
 		}
 		else
 		{
 			cid = databaseHandler.saveCheckPoint(checkPoint);
 			checkPoint.setId(cid);
 		}
 		for (Track track : selectedTracks)
 		{
 			databaseHandler.saveTrack(cid, track);
 		}
 		selectedTracks.clear();
 	}
 
 	/**
 	 * When a checkpoint is tapped this method calls the showCheckPointDialog
 	 * method with MODE_EDIT which marks it as a edit dialog. This method also
 	 * sets the current checkpoint to the last tapped.
 	 */
 	@Override
 	public void onCheckPointTap(CheckPoint checkPoint)
 	{
 		currentCheckPoint = checkPoint;
 		showCheckPointDialog(checkPoint, EditCheckPointDialog.MODE_EDIT);
 	}
 
 	/**
 	 * Initiates a new checkpoint dialog
 	 * 
 	 * @param checkPoint
 	 * @param mode
 	 */
 	private void showCheckPointDialog(CheckPoint checkPoint, int mode)
 	{
 		checkPointDialog = new EditCheckPointDialog(this, checkPoint, mode);
 		checkPointDialog.show();
 	}
 
 	/**
 	 * Creates a checkpoint with the geopoint and adds it to the checkpoint
 	 * overlay, it also calls showCheckPointDialog with the MODE_ADD which marks
 	 * it as a add dialog. This method also saves the newly created checkpoint
 	 * as the current checkpoint for future reference.
 	 * 
 	 * @param geoPoint
 	 */
 	private void createCheckPoint(GeoPoint geoPoint)
 	{
 		CheckPoint checkPoint = new CheckPoint(geoPoint);
 		currentCheckPoint = checkPoint;
 		checkPointOverlay.addCheckPoint(checkPoint);
 		showCheckPointDialog(checkPoint, EditCheckPointDialog.MODE_ADD);
 	}
 
 	/**
 	 * The onTap method zooms in on double tap and creates a geopoint on single
 	 * tap which it sends to createCheckPoint
 	 */
 	@Override
 	public void onTap(int x, int y, int eventType)
 	{
 		switch (eventType)
 		{
 			case MapOnGestureListener.EVENT_DOUBLE_TAP:
 				mapView.getController().zoomInFixing(x, y);
 				break;
 			case MapOnGestureListener.EVENT_SINGLE_TAP:
 				if (checkPointDialog == null || !checkPointDialog.isShowing())
 				{
 					GeoPoint geoPoint = mapView.getProjection().fromPixels(x, y);
 					createCheckPoint(geoPoint);
 				}
 				break;
 		}
 	}
 
 	/**
 	 * This method is called when an item in the action bar (options menu) has
 	 * been pressed. Currently this only takes the user to the parent activity
 	 * (main activity).
 	 */
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item)
 	{
 		switch (item.getItemId())
 		{
 			case android.R.id.home:
 				NavUtils.navigateUpFromSameTask(this);
 				return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 	/**
 	 * This method is called from the save geoPointList dialog when the user has
 	 * pressed the "save" button. It creates a new geoPointList with the
 	 * information given in the dialog and then saves it to the database. After
 	 * that, the user is taken back to the main activity.
 	 */
 	@Override
 	public void onSaveRoute(String name, String description, boolean saveResult)
 	{
 		Route route = new Route(name, description);
 		route.setId(databaseHandler.saveRoute(route));
 		if (saveResult)
 		{
 			routeResults.setRouteId(route.getId());
 			routeResults.setTimestamp((int) System.currentTimeMillis() / 1000);
 			databaseHandler.saveResult(routeResults);
 		}
 		databaseHandler.saveGeoPoints(route.getId(), geoPointList);
 		databaseHandler.updateCheckPointRid(route.getId());
 		launchMainActivity();
 	}
 
 	/**
 	 * Called from the save geoPointList dialog when a geoPointList has been
 	 * dismissed. This method just launches the main activity.
 	 */
 	@Override
 	public void onDismissRoute()
 	{
 		launchMainActivity();
 	}
 
 	/**
 	 * Called by the system when the activity is shut down completely. Releases
 	 * the wake lock.
 	 */
 	@Override
 	public void onDestroy()
 	{
 		releaseWakeLock();
 		super.onDestroy();
 	}
 
 	/**
 	 * Private helper method to launch the main activity.
 	 */
 	private void launchMainActivity()
 	{
 		Intent intent = new Intent(this, MainActivity.class);
 		this.startActivity(intent);
 	}
 
 	/**
 	 * Acquires the wake lock from the system if it is available and not already
 	 * held.
 	 */
 	private void acquireWakeLock()
 	{
 		try
 		{
 			PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
 			if (wakeLock == null)
 			{
 				wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
 			}
 			if (!wakeLock.isHeld())
 			{
 				wakeLock.acquire();
 			}
 		}
 		catch (RuntimeException e)
 		{
 			Log.e(TAG, "Could not acquire wakelock: ", e);
 		}
 	}
 
 	/**
 	 * Releases the wake lock if available and held
 	 */
 	private void releaseWakeLock()
 	{
 		if (wakeLock != null && wakeLock.isHeld())
 		{
 			wakeLock.release();
 			wakeLock = null;
 		}
 	}
 }
