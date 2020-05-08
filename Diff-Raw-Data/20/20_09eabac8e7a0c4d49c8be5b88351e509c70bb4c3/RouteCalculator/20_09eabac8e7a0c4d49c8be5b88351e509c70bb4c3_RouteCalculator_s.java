 /*
  * Copyright 2011 mapsforge.org
  *
  *	This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.muxe.advancedtouristmap.routing;
 
 import java.io.File;
 
 import org.mapsforge.android.maps.GeoPoint;
 import org.mapsforge.core.Edge;
 import org.mapsforge.core.GeoCoordinate;
 import org.mapsforge.core.Router;
 import org.mapsforge.core.Vertex;
 import org.muxe.advancedtouristmap.BaseActivity;
 import org.muxe.advancedtouristmap.LocationPicker;
 import org.muxe.advancedtouristmap.PositionInfo;
 import org.muxe.advancedtouristmap.R;
 import org.muxe.advancedtouristmap.Search;
 import org.muxe.advancedtouristmap.Utility;
 import org.muxe.advancedtouristmap.sourcefiles.RoutingFile;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.location.LocationProvider;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageButton;
 import android.widget.RelativeLayout;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class RouteCalculator extends BaseActivity {
 	static final String TAG = RouteCalculator.class.getSimpleName();
 
 	protected static final int INTENT_SEARCH = 0;
 	protected static final int INTENT_MAP = 1;
 
 	protected static final int START_FIELD = 0;
 	protected static final int DEST_FIELD = 1;
 
 	protected static final int DIALOG_CHOOSE_INPUT = 0;
 
	private static final int TWO_MINUTES = 1000 * 60 * 2;
 	private static final String SAVE_START_LAT = "saved_start_lat";
 	private static final String SAVE_START_LON = "saved_start_lon";
 	private static final String SAVE_DEST_LAT = "saved_dest_lat";
 	private static final String SAVE_DEST_LON = "saved_dest_lon";
 
 	GeoPoint startPoint;
 	GeoPoint destPoint;
 
 	ImageButton chooseStartButton;
 	ImageButton chooseDestButton;
 	private Button calcRouteButton;
 
 	private EditText startEditText;
 	private EditText destEditText;
 	
 	private RelativeLayout refiningPositionRow;
 	private TextView refiningPositionText;
 	
 	int viewToSet;
 
 	ProgressDialog progressDialog;
 
 	private LocationManager locationManager;
 	private LocationListener locationListener;
 	Location currentBestLocation;
 
 	Spinner routingFileSpinner;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		Log.d("lifecycle", "routeCalculator onCreate");
 		this.advancedMapViewer.setViewWithHelp(this, R.layout.activity_calculate_route);
 
 		Intent startingIntent = getIntent();
 		if (startingIntent.hasExtra("lat") && startingIntent.hasExtra("lon")) {
 			this.destPoint = new GeoPoint(startingIntent.getDoubleExtra("lat", 0.0),
 					startingIntent.getDoubleExtra("lon", 0.0));
 		}
 
 		this.startEditText = (EditText) findViewById(R.id.calculate_route_edittext_start);
 		this.destEditText = (EditText) findViewById(R.id.calculate_route_edittext_dest);
 
 		this.chooseStartButton = (ImageButton) findViewById(R.id.calculate_route_button_choose_start);
 		this.chooseDestButton = (ImageButton) findViewById(R.id.calculate_route_button_choose_dest);
 		this.calcRouteButton = (Button) findViewById(R.id.calculate_route_button_calculate);
 
 		this.routingFileSpinner = (Spinner) findViewById(R.id.calculate_route_spinner_routing_file);
 		
 		this.refiningPositionRow = (RelativeLayout) findViewById(R.id.routing_loading_position);
 		this.refiningPositionText = (TextView) findViewById(R.id.routing_refining_position_text);
 
 		OnClickListener startDestChooserListener = new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				if (v.getId() == RouteCalculator.this.chooseStartButton.getId()) {
 					RouteCalculator.this.viewToSet = RouteCalculator.START_FIELD;
 				} else if (v.getId() == RouteCalculator.this.chooseDestButton.getId()) {
 					RouteCalculator.this.viewToSet = RouteCalculator.DEST_FIELD;
 				}
 				// open a dialog to select method to chose start/dest
 				showDialog(DIALOG_CHOOSE_INPUT);
 			}
 		};
 
 		this.chooseStartButton.setOnClickListener(startDestChooserListener);
 		this.chooseDestButton.setOnClickListener(startDestChooserListener);
 
 		this.calcRouteButton.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				// TODO: if no routing file selected?
 				// still nullpointer exception on no routing files
 				RoutingFile rf = (RoutingFile) RouteCalculator.this.routingFileSpinner
 						.getSelectedItem();
 				if (RouteCalculator.this.startPoint == null) {
 					Toast.makeText(RouteCalculator.this,
 							getString(R.string.routing_no_start_selected), Toast.LENGTH_LONG)
 							.show();
 					return;
 				}
 				if (RouteCalculator.this.destPoint == null) {
 					Toast.makeText(RouteCalculator.this,
 							getString(R.string.routing_no_destination_selected),
 							Toast.LENGTH_LONG).show();
 					return;
 				}
 				new CalculateRouteAsync().execute(rf);
 			}
 		});
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		if (requestCode == INTENT_SEARCH) {
 			if (resultCode == RESULT_OK) {
 				if (data != null && data.hasExtra("lon") && data.hasExtra("lat")) {
 					double lon = data.getDoubleExtra("lon", 0.0);
 					double lat = data.getDoubleExtra("lat", 0.0);
 					GeoPoint point = new GeoPoint(lat, lon);
 					if (this.viewToSet == RouteCalculator.START_FIELD) {
 						this.startPoint = point;
 					} else {
 						this.destPoint = point;
 					}
 				}
 			}
 		} else if (requestCode == INTENT_MAP) {
 			// TODO: not DRY yet
 			// TODO: find nearest vertex first?
 			if (resultCode == RESULT_OK) {
 				if (data != null && data.hasExtra("LONGITUDE") && data.hasExtra("LATITUDE")) {
 					double lon = data.getDoubleExtra("LONGITUDE", 0.0);
 					double lat = data.getDoubleExtra("LATITUDE", 0.0);
 					GeoPoint point = new GeoPoint(lat, lon);
 					if (this.viewToSet == RouteCalculator.START_FIELD) {
 						this.startPoint = point;
 					} else {
 						this.destPoint = point;
 					}
 				}
 			}
 		}
 	}
 
 	@Override
 	protected Dialog onCreateDialog(int dialogId) {
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		if (dialogId == DIALOG_CHOOSE_INPUT) {
 			final String[] items = getResources().getStringArray(
 					R.array.routing_point_picker_values);
 			final String[] items_keys = getResources().getStringArray(
 					R.array.routing_point_picker_keys);
 			builder.setTitle(R.string.dialog_title_find_location);
 			builder.setItems(items, new DialogInterface.OnClickListener() {
 				@Override
 				public void onClick(DialogInterface dialog, int item) {
 					if (items_keys[item].equals("ADDRESS")) {
 						if (RouteCalculator.this.advancedMapViewer.getCurrentMapBundle()
 								.isSearchable()) {
 							startActivityForResult(new Intent(RouteCalculator.this,
 									Search.class), INTENT_SEARCH);
 						} else {
 							// TODO:
 							Toast.makeText(RouteCalculator.this,
 									getString(R.string.addressfile_not_avaiable),
 									Toast.LENGTH_LONG).show();
 						}
 					} else if (items_keys[item].equals("POSITION")) {
 						startPositionSearch();
 					} else if (items_keys[item].equals("MAP")) {
 						startActivityForResult(new Intent(RouteCalculator.this,
 								LocationPicker.class), INTENT_MAP);
 						// new Intent(RouteCalculator.this, AdvancedMapViewer.class)
 						// .putExtra("mode", "LOCATION_PICKER"), INTENT_MAP);
 					}
 				}
 			});
 			return builder.create();
 		}
 		return null;
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		Log.d("lifecycle", "routeCalculator onResume");
 
 		if (this.locationManager == null) {
 			this.locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
 		}
 
 		if (this.startPoint != null) {
 			changeStartStop(START_FIELD, this.startPoint);
 			// this.startEditText.setText(this.startPoint.getLatitude() + " "
 			// + this.startPoint.getLongitude());
 		}
 		if (this.destPoint != null) {
 			changeStartStop(DEST_FIELD, this.destPoint);
 			// this.destEditText.setText(this.destPoint.getLatitude() + " "
 			// + this.destPoint.getLongitude());
 		}
 
 		RoutingFile[] routingFiles = this.advancedMapViewer.getCurrentMapBundle()
 				.getRoutingFilesArray();
 
 		ArrayAdapter<RoutingFile> adapter = new ArrayAdapter<RoutingFile>(this,
 				android.R.layout.simple_spinner_item, routingFiles);
 		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		this.routingFileSpinner.setAdapter(adapter);
 	}
 
 	void startPositionSearch() {
 		// TODO: exit strategy (timer and/or when signal stabilized)
 		// check if already running, if so, stop first
 		if (this.locationListener != null) {
 			stopPositionSearch();
 		}
 		
 		//display animation
 		this.refiningPositionRow.setVisibility(View.VISIBLE);
 
 		// get cached locations first
 		Location currentLocation;
 		for (String provider : this.locationManager.getProviders(true)) {
 			currentLocation = this.locationManager.getLastKnownLocation(provider);
 			if (currentLocation != null) {
 				if (Utility.isBetterLocation(currentLocation, this.currentBestLocation)) {
 					this.currentBestLocation = currentLocation;
 					changeStartStop(RouteCalculator.this.viewToSet, new GeoPoint(
 							currentLocation.getLatitude(), currentLocation.getLongitude()));
 					Log.d(TAG, "got better cached location from: " + provider + " ("
 							+ currentLocation.getAccuracy() + ")");
 					RouteCalculator.this.refiningPositionText.setText(getString(R.string.refining_position) + " (" + currentLocation.getAccuracy() + " m)");
 				} else {
 					Log.d(TAG,
 							"dismissed location from: " + provider + " ("
 									+ currentLocation.getAccuracy() + ")");
 				}
 			}
 		}
 
 		this.locationListener = new LocationListener() {
 			@Override
 			public void onLocationChanged(Location location) {
 				// GeoPoint point = new GeoPoint(location.getLatitude(),
 				// location.getLongitude());
 				if (Utility.isBetterLocation(location, RouteCalculator.this.currentBestLocation)) {
 					RouteCalculator.this.currentBestLocation = location;
 					Log.d(TAG, "better location from: " + location.getProvider() + " ("
 							+ location.getAccuracy() + ")");
 					changeStartStop(RouteCalculator.this.viewToSet,
 							new GeoPoint(location.getLatitude(), location.getLongitude()));
 					//show accuracy in loading message
 					RouteCalculator.this.refiningPositionText.setText(getString(R.string.refining_position) + " (" + location.getAccuracy() + " m)");
 				} else {
 					Log.d(TAG, "dismissed location from: " + location.getProvider() + " ("
 							+ location.getAccuracy() + ")");
 				}
 			}
 
 			@Override
 			public void onProviderDisabled(String provider) {
 
 			}
 
 			@Override
 			public void onProviderEnabled(String provider) {
 				// do nothing
 			}
 
 			@Override
 			public void onStatusChanged(String provider, int status, Bundle extras) {
 				if (status == LocationProvider.AVAILABLE) {
 
 				} else if (status == LocationProvider.OUT_OF_SERVICE) {
 
 				} else {
 					// must be TEMPORARILY_UNAVAILABLE
 				}
 			}
 		};
 
 		this.locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0,
 				this.locationListener);
 		this.locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0,
 				this.locationListener);
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 		Log.d("lifecycle", "routeCalculator onPause");
 		stopPositionSearch();
 	}
 
 	private void stopPositionSearch() {
 		if (this.locationListener != null) {
 			this.locationManager.removeUpdates(this.locationListener);
 			this.locationListener = null;
 			
 			this.refiningPositionRow.setVisibility(View.GONE);
 		}
 	}
 
 	void changeStartStop(int field, GeoPoint point) {
 		String infoText = point.getLatitude() + " " + point.getLongitude();
 		if (this.advancedMapViewer.getCurrentMapBundle().isRoutable()) {
 			//TODO: may halt on slow phones
 			Router router = this.advancedMapViewer.getRouter();
 			if (router != null) {
 				Vertex nearestVertex = router.getNearestVertex(new GeoCoordinate(point
 						.getLatitude(), point.getLongitude()));
 				if (nearestVertex != null) {
 					infoText = PositionInfo.edgesToStringInfo(nearestVertex.getOutboundEdges());
 					if (infoText.equals("")) {
 						infoText = getString(R.string.positioninfo_unknown_road);
 					}
 				}
 			}
 		}
 
 		if (field == RouteCalculator.START_FIELD) {
 			this.startPoint = point;
 			this.startEditText.setText(infoText);
 		} else if (field == RouteCalculator.DEST_FIELD) {
 			this.destPoint = point;
 			this.destEditText.setText(infoText);
 		}
 	}
 
 	private class CalculateRouteAsync extends AsyncTask<RoutingFile, Void, Route> {
 
 		public CalculateRouteAsync() {
 			super();
 		}
 
 		@Override
 		protected void onPreExecute() {
 			RouteCalculator.this.progressDialog = ProgressDialog.show(RouteCalculator.this, "",
 					getString(R.string.loading_message), true);
 		}
 
 		@Override
 		protected Route doInBackground(RoutingFile... routingFiles) {
 			// calculate
 			RoutingFile rf = null;
 			try {
 				rf = routingFiles[0];
 			} catch (ArrayIndexOutOfBoundsException e) {
 				return null;
 			}
 
 			String path = RouteCalculator.this.advancedMapViewer.getBaseBundlePath()
 					+ File.separator + rf.getRelativePath();
 			Router router = RouteCalculator.this.advancedMapViewer.getRouter(path);
 			if (router == null) {
 				return null;
 			}
 			Vertex start = router.getNearestVertex(new GeoCoordinate(
 					RouteCalculator.this.startPoint.getLatitude(),
 					RouteCalculator.this.startPoint.getLongitude()));
 			Vertex dest = router.getNearestVertex(new GeoCoordinate(
 					RouteCalculator.this.destPoint.getLatitude(),
 					RouteCalculator.this.destPoint.getLongitude()));
 
 			if (start == null || dest == null) {
 				return null;
 			}
 
 			Edge[] edges = router.getShortestPath(start.getId(), dest.getId());
 
 			// try {
 			// Thread.sleep(10000);
 			// } catch (InterruptedException e) {
 			//
 			// }
 
 			if (edges.length > 0) {
 				Route route = new Route(edges);
 				Log.d(TAG, "done");
 				Log.d(TAG, "length: " + edges.length);
 				return route;
 			}
 			return null;
 		}
 
 		@Override
 		protected void onPostExecute(Route route) {
 			// remove progress bar
 			RouteCalculator.this.progressDialog.dismiss();
 			// show route
 			if (route != null) {
 				RouteCalculator.this.advancedMapViewer.currentRoute = route;
 				startActivity(new Intent(RouteCalculator.this, RouteList.class));
 			} else {
 				Log.d(TAG, "No Route Found");
 				Toast.makeText(RouteCalculator.this,
 						getString(R.string.routing_no_route_found), Toast.LENGTH_LONG).show();
 			}
 		}
 	}
 
 	@Override
 	protected void onRestoreInstanceState(Bundle savedInstanceState) {
 		super.onRestoreInstanceState(savedInstanceState);
 		double startLat = savedInstanceState.getDouble(SAVE_START_LAT);
 		double startLon = savedInstanceState.getDouble(SAVE_START_LON);
 		double destLat = savedInstanceState.getDouble(SAVE_DEST_LAT);
 		double destLon = savedInstanceState.getDouble(SAVE_DEST_LON);
 		if (startLat != 0.0 && startLon != 0.0) {
 			this.startPoint = new GeoPoint(startLat, startLon);
 		}
 		if (destLat != 0.0 && destLon != 0.0) {
 			this.destPoint = new GeoPoint(destLat, destLon);
 		}
 	}
 
 	@Override
 	protected void onSaveInstanceState(Bundle outState) {
 		if (this.startPoint != null) {
 			outState.putDouble(SAVE_START_LAT, this.startPoint.getLatitude());
 			outState.putDouble(SAVE_START_LON, this.startPoint.getLongitude());
 		}
 		if (this.destPoint != null) {
 			outState.putDouble(SAVE_DEST_LAT, this.destPoint.getLatitude());
 			outState.putDouble(SAVE_DEST_LON, this.destPoint.getLongitude());
 		}
 		super.onSaveInstanceState(outState);
 	}
 }
