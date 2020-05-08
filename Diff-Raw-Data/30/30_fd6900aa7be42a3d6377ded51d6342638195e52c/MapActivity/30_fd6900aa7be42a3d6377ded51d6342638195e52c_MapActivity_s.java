 package se.chalmers.krogkollen.map;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.Activity;
 import android.app.AlertDialog.Builder;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.graphics.Point;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.SystemClock;
 import android.view.Menu;
 import android.view.View;
 import android.view.animation.LinearInterpolator;
 import android.view.MenuItem;
 import android.widget.CheckBox;
 import android.widget.CompoundButton;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 
 import com.google.android.gms.maps.CameraUpdateFactory;
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.MapFragment;
 import com.google.android.gms.maps.Projection;
 import com.google.android.gms.maps.model.BitmapDescriptorFactory;
 import com.google.android.gms.maps.model.CameraPosition;
 import com.google.android.gms.maps.model.LatLng;
 import com.google.android.gms.maps.model.Marker;
 import com.google.android.gms.maps.model.MarkerOptions;
 
 import se.chalmers.krogkollen.R;
 import se.chalmers.krogkollen.pub.IPub;
 import se.chalmers.krogkollen.pub.PubUtilities;
 import se.chalmers.krogkollen.utils.ActivityID;
 import se.chalmers.krogkollen.utils.IObserver;
 
 /**
  * The standard implementation of IMapView.
  *
  * This is a normal map with the user marked on the map, and with a list of pubs marked on the map.
  */
 public class MapActivity extends Activity implements IMapView, IObserver{
 
 	private GoogleMap mMap;
 	private UserLocation userLocation;
 	private Marker userMarker;
 	private final int ZOOM = 15;
 	private List<Marker> pubMarkers = new ArrayList<Marker>();
 
 	//dialog stuff
 	private SharedPreferences sharedPref;
 	private boolean dontShowAgain;
 	private boolean haveShownDialog = false;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_map);
 
 		//get the map and add some markers for pubs.
 		this.mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
 		this.addPubMarkers();
 
 		mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
 			@Override
 			public boolean onMarkerClick(Marker marker) {
 				// open detailed view.
 				return false; // Keep default behavior i.e. move camera to marker.
 			}
 		});
 
 		this.sharedPref = getPreferences(Context.MODE_PRIVATE);
 
 		//Get the boolean that describes if dialogs should be shown again.
 		boolean defaultValue = getResources().getBoolean(R.bool.dont_show_again_default);
 		this.dontShowAgain = sharedPref.getBoolean(getString(R.string.dont_show_again_key), defaultValue);
 
 		// Add services for auto update of the user's location.
 		this.userLocation = UserLocation.getInstance();
 		this.userLocation.addObserver(this);
 		this.userLocation.startTrackingUser();
 
 		getActionBar().setDisplayUseLogoEnabled(false);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		super.onCreateOptionsMenu(menu);
 
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.map, menu);
 
 		return true;
 	}
 
 	/**
 	 * Adds a user marker to the map.
 	 * 
 	 * @param latLng the user location
 	 */
 	public void addUserMarker(LatLng latLng){
 		userMarker = mMap.addMarker(new MarkerOptions()
 		.position(latLng)
 		.icon(BitmapDescriptorFactory.fromResource(R.drawable.user_marker)));
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem menuItem) {
 		if (menuItem.getItemId() == R.id.refresh_info) {
 			// REFRESH MAP HERE PLS
 			return true;
 		}
 		if (menuItem.getItemId() == R.id.search) {
 			// ÖPPNA SÖKFÄLT
 			return true;
 		}
 		if (menuItem.getItemId() == R.id.open_list_view) {
 			// SKICKA INTENT - ÖPPNA LISTA
 			return true;
 		}
 		return super.onOptionsItemSelected(menuItem);
 	}
 
 	/**
 	 * Adds markers to the map for all pubs in PubUtilities.
 	 */
 	public void addPubMarkers() {
 		for (int i = 0; i < PubUtilities.getInstance().getPubList().size(); i++) {
 			addPubToMap(PubUtilities.getInstance().getPubList().get(i));
 		}
 	}
 
 	/**
 	 * Removes all pub markers and adds them again with (new) information.
 	 */
 	public void refreshPubMarkers() {
 		for(Marker pubMarker: this.pubMarkers){
 			pubMarker.remove();
 		}
 		this.pubMarkers.clear();
 		//refresh pubUtilities
 		this.addPubMarkers();
 	}
 
 	@Override
 	public void update(Status status) {
 
 		//Create most of the dialog that will be shown if either wifi or gps are disabled.
 		Builder builder = new Builder(this);
 
		//Checkbox, making it possible to chose not to show this dialog again.
 		final ArrayList<Integer> selected = new ArrayList<Integer>();
 
 		View checkBoxView = View.inflate(this, R.layout.checkbox, null);
 		CheckBox checkBox = (CheckBox)checkBoxView.findViewById(R.id.checkbox);
 		checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
 
 			@Override
 			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
 				if (isChecked) {
 					selected.add(0);
 				} else if(selected.size() != 0){
 					selected.clear();
 				}
 			}
 		});
 		checkBox.setText(this.getString(R.string.alert_dialog_dont_show_again));
 		builder.setView(checkBoxView);
 		builder.setTitle(R.string.alert_dialog_title);
 		
 		//Set listeners to the buttons in the dialog and chose appropriate consequences for clicks.
 		builder.setPositiveButton(R.string.alert_dialog_activate, new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int which) { 
 				//Save the don't show again option.
 				SharedPreferences.Editor editor = sharedPref.edit();
 				editor.putBoolean(getString(R.string.dont_show_again_key), !selected.isEmpty());
 				editor.commit();
 
 				//Send user to location settings on the phone.
 				Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
 				startActivity(intent);
 			}
 		});
 		builder.setNegativeButton(R.string.alert_dialog_cancel, new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int which) { 
 				//Save the don't show again option.
 				SharedPreferences.Editor editor = sharedPref.edit();
 				editor.putBoolean(getString(R.string.dont_show_again_key), !selected.isEmpty());
 				editor.commit();
 			}
 		});
 
 		//Update accordingly to what has happened in user location.
 		switch(status) {
 		case FIRST_LOCATION:
 			//User location has received a first location so a user marker is added and 
 			//map is centered on user.
 			addUserMarker(this.userLocation.getCurrentLatLng());
 			this.centerOnUser();
 			break;
 		case NORMAL_UPDATE:
 			//The location has been updated, move the marker accordingly.
 			this.animateMarker(this.userMarker, this.userLocation.getCurrentLatLng());
 			break;
 		case ALL_DISABLED:
 			//If we haven't shown a dialog and the user hasn't chosen don't show again,
 			//show a dialog and prompt the user to enable wifi and gps tracking.
 			if(!this.haveShownDialog && !this.dontShowAgain) {
 				builder.setMessage(R.string.alert_dialog_net_and_gps_disabled);
 				builder.show();
 				this.haveShownDialog = true;
 			}
 			break;
 		case NET_DISABLED:
 			//If we haven't shown a dialog and the user hasn't chosen don't show again,
 			//show a dialog and prompt the user to enable wifi tracking.
 			if(!this.haveShownDialog && !this.dontShowAgain) {
 				builder.setMessage(R.string.alert_dialog_net_disabled);
 				builder.show();
 				this.haveShownDialog = true;
 			}
 			break;
 		case GPS_DISABLED:
 			//If we haven't shown a dialog and the user hasn't chosen don't show again,
 			//show a dialog and prompt the user to enable gps tracking.
 			if(!this.haveShownDialog && !this.dontShowAgain) {
 				builder.setMessage(R.string.alert_dialog_gps_disabled);
 				builder.show();
 				this.haveShownDialog = true;
 			}
 			break;
 		default:
 			break;
 		}	
 	}
 
 	/**
 	 * Centers the map on the users location.
 	 */
 	public void centerOnUser() {
 		this.mMap.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(this.userLocation.getCurrentLatLng(), this.ZOOM, 0, 0)));
 	}
 
 	/**
 	 * ** Method written by Google, found on stackoverflow.com **
 	 * ** http://stackoverflow.com/questions/13728041/move-markers-in-google-map-v2-android **
 	 * Moves the user marker smoothly to a new position.
 	 * 
 	 * @param marker	the marker that will be moved
 	 * @param toPosition	the position to where the marker will be moved
 	 */
 	private void animateMarker(final Marker marker, final LatLng toPosition) {
 		final Handler handler = new Handler();
 		final long start = SystemClock.uptimeMillis();
 		Projection proj = this.mMap.getProjection();
 		Point startPoint = proj.toScreenLocation(marker.getPosition());
 		final LatLng startLatLng = proj.fromScreenLocation(startPoint);
 		final long duration = 500;
 
 		final LinearInterpolator interpolator = new LinearInterpolator();
 
 		handler.post(new Runnable() {
 			@Override
 			public void run() {
 				long elapsed = SystemClock.uptimeMillis() - start;
 				float t = interpolator.getInterpolation((float) elapsed / duration);
 				double lng = t * toPosition.longitude + (1 - t) * startLatLng.longitude;
 				double lat = t * toPosition.latitude + (1 - t) * startLatLng.latitude;
 				marker.setPosition(new LatLng(lat, lng));
 
 				if (t < 1.0) {
 					// Post again 16ms later.
 					handler.postDelayed(this, 16);
 				}
 			}
 		});
 	}
 
 	@Override
 	public void onPause() {
 		super.onPause();
 		this.userLocation.onPause();
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 		this.userLocation.onResume();
		this.userLocation.startTrackingUser();
 	}
 
 	@Override
 	public void onBackPressed() {
 		//Get what activity you came from originally.
 		int activity = this.getIntent().getIntExtra(ActivityID.ACTIVITY_ID, 0);
 		Intent intent;
 		switch(activity) {
 		case ActivityID.LIST:
 			//If you came from the list view, return there.
 			//Intent intent = new Intent(this, ListActivity.class);
 			//intent.putExtra(CallingActivity.MAP);
 			//this.startActivity(intent);
 			break;
 		default:
 			//If you came from anything else, return to home screen.
 			intent = new Intent(Intent.ACTION_MAIN);
 			intent.addCategory(Intent.CATEGORY_HOME);
 			startActivity(intent);
 			break;
 		}
 	}
 
 	@Override
 	public void navigate(Class<?> destination) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void showErrorMessage(String message) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void addPubToMap(IPub pub) {
 
 		int drawable;
 		// Determine which marker color to add.
 		switch (pub.getQueueTime()) {
 		case 1:
 			drawable = R.drawable.green_marker_bg;
 			break;
 		case 2:
 			drawable = R.drawable.yellow_marker_bg;
 			break;
 		case 3:
 			drawable = R.drawable.red_marker_bg;
 			break;
 		default:
 			drawable = R.drawable.gray_marker_bg;
 			break;
 		}
 		mMap.addMarker(MarkerFactory.createMarkerOptions(getResources(), drawable, pub.getName(), pub.getOpeningHours(),
 				new LatLng(pub.getCoordinates().latitude, pub.getCoordinates().longitude)));
 	}
 }
