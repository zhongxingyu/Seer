 package edu.rosehulman.androidmovingmap;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeMap;
 
 import org.osmdroid.tileprovider.MapTile;
 import org.osmdroid.tileprovider.tilesource.XYTileSource;
 import org.osmdroid.util.GeoPoint;
 import org.osmdroid.views.overlay.MyLocationOverlay;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.DialogFragment;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.location.LocationProvider;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.preference.PreferenceManager;
 import android.provider.Settings;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.Toast;
 import edu.rosehulman.maps.OSMMapView;
 import edu.rosehulman.overlays.AMMItemizedOverlay;
 import edu.rosehulman.overlays.IItemizedOverlay;
 import edu.rosehulman.overlays.OverlayIconRegistry;
 import edu.rosehulman.server.POI;
 import edu.rosehulman.server.Server;
 
 public class MainActivity extends Activity implements OnClickListener,
 		Serializable {
 
 	private SharedPreferences prefs;
 
 	private OSMMapView mMapView;
 	private LocationManager locationManager;
 
 	LocationProvider provider;
 	private MyLocationOverlay deviceOverlay;
 
 	private ImageView mCompass;
 	private ImageView mFindMe;
 	private boolean mNorthUp;
 
 	private XYTileSource tileSource;
 
 
 	private String mapSourcePrefix = "initial source prefix";
 
 	private ArrayList<String> mapSourceNames = new ArrayList<String>(
 			Arrays.asList("map1", "map2"));
 	private ArrayList<Integer> mapMaxZoom = new ArrayList<Integer>(
 			Arrays.asList(16, 4));
 	private int mapSourceIndex = 0;
 
 	private int UID_to_track = -1;
 
 	private Handler invalidateDisplay = new Handler() {
 		@Override
 		public void handleMessage(Message msg) {
 			updatePOIandScreen();
 		}
 	};
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 
 		prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
 		
 		int refreshGPStime = 1000;
 		int refreshGPSdistance = 20;
 
 		locationManager = (LocationManager) this
 				.getSystemService(Context.LOCATION_SERVICE);
 		provider = locationManager.getProvider(LocationManager.GPS_PROVIDER);
 		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
 				refreshGPStime, refreshGPSdistance, listener);
 
 		mMapView = (OSMMapView) findViewById(R.id.map_view);
 		mMapView.setClickable(true);
 		mMapView.setMultiTouchControls(true);
 		mMapView.setBuiltInZoomControls(true);
 
 		deviceOverlay = new MyLocationOverlay(this, mMapView);
 		deviceOverlay.enableFollowLocation();
 		deviceOverlay.enableMyLocation();
 
 		// Comment back in to use MAPNIK server data
 		// mMapView.setTileSource(TileSourceFactory.MAPNIK);
 
 		String server_name = this.prefs.getString("KEY_SERVER", "bad preferences"); 
 		Log.d("server_name", server_name);
 
 		mapSourcePrefix = "http://" + server_name;
 
 		tileSource = new XYTileSource("local" + mapSourceIndex, null, 0, 
 				mapMaxZoom.get(mapSourceIndex),
 				256, ".png", mapSourcePrefix + "/" + mapSourceNames.get(mapSourceIndex) + "/");
 
 		mMapView.setTileSource(tileSource);
 
 		mMapView.getController().setZoom(13);
 
 		mCompass = (ImageView) findViewById(R.id.compass);
 		mCompass.setOnClickListener(this);
 
 		mFindMe = (ImageView) findViewById(R.id.find_me);
 		mFindMe.setOnClickListener(this);
 
 		Server.getInstance().updatePOIHandler = invalidateDisplay;
 		Server.getInstance().setServerAddress(server_name);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.activity_main, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		int itemId = item.getItemId();
 		if (itemId == R.id.add_poi_type) {
 			Toast.makeText(this, "I can totally add POI types",
 					Toast.LENGTH_SHORT).show();
 			AddPOITypeDialogFragment.newInstance(
 					mMapView.getAMMOverlayManager(), this).show(
 					getFragmentManager(), "lol");
 			return true;
 		} else if (itemId == R.id.menu_settings) {
 			Intent intent = new Intent(this, Preferences.class);
 			startActivity(intent);
 			return true;
 		} else if (itemId == R.id.menu_cycle_map_type) {
 			mapSourceIndex = (mapSourceIndex + 1) % mapSourceNames.size();
 			tileSource = new XYTileSource("local" + mapSourceIndex, null, 0,
 					mapMaxZoom.get(mapSourceIndex), 256, ".png",
 					mapSourcePrefix + mapSourceNames.get(mapSourceIndex));
 			Log.d("menu cycle",
 					"pathBase: "
 							+ tileSource.getTileURLString(new MapTile(0, 0, 0)));
 			mMapView.setTileSource(tileSource);
 			mMapView.invalidate();
 			return true;
 		} else if (itemId == R.id.menu_track_point) {
 			UID_to_track += 1;
 			if (UID_to_track == Server.getInstance().POIelements.size()) {
 				UID_to_track = -1;
 			}
 			return true;
 		} else if (itemId == R.id.menu_start_stop_sync) {
 			Log.d("sync", "status: " + Server.getInstance().startedPOISync());
 			if (Server.getInstance().startedPOISync()) {
 				Server.getInstance().stopPOISync();
 			} else {
 				Server.getInstance().startPOISync();
 			}
 			return true;
 		} else if (itemId == R.id.menu_push_server) {
 			Log.d("POI", "attempting to sync onto server");
 			for (IItemizedOverlay type : mMapView.getAMMOverlayManager().getOverlays()) {
 				Iterator<POI> iter = type.getOverlays().iterator();
 				while (iter.hasNext()) {
 					POI tempPoint = iter.next();
 					if (tempPoint.getUID() < 0) {
 						try {
 							Server.getInstance().sendMessage(
 									"addPoint:" + tempPoint.toJSONString() + "\n");
 							iter.remove();
 						} catch (Exception e) {
 							Log.d("POI", "failed to send POI");
 						}
 					}
 				}
 				for (int UIDToRemove : Server.POIUIDToDelete)
 				{
 					POI tempPoint = new POI(UIDToRemove, "", 0.0, 0.0, "", new TreeMap<String,String>());
 					try {
 						Server.getInstance().sendMessage("removePoint:" + tempPoint.toJSONString() + "\n");
 					} catch (Exception e) {
 						Log.d("POI", "failed to remove UID: " + UIDToRemove);
 					}
 				}
 			}
 			return true;
 		} else if (itemId == R.id.choose_poi_to_display) {
 			new ChooseDisplayedPOIFragment().show(getFragmentManager(),
 					"show displayed poi dialog");
 			return true;
 		} else {
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	private class ChooseDisplayedPOIFragment extends DialogFragment {
 
 		@Override
 		public Dialog onCreateDialog(Bundle savedInstanceState) {
 			ListView listView = new ListView(getBaseContext());
 			final List<IItemizedOverlay>  overlays = mMapView.getAMMOverlayManager().getOverlays();
 			final OverlayListAdapter adapter = new OverlayListAdapter(
 					MainActivity.this.getBaseContext(), OverlayIconRegistry
 							.getInstance().getRegisteredOverlays(),
 					ListView.CHOICE_MODE_MULTIPLE, overlays);
 			listView.setAdapter(adapter);
 			listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
 			listView.setItemsCanFocus(false);
 			for(int i = 0; i < listView.getCount(); i++){
 				AMMItemizedOverlay overlay = (AMMItemizedOverlay)((IItemizedOverlay) overlays.get(i));
 				listView.setItemChecked(i, overlay.isActive());
 			}
 			listView.setOnItemClickListener(new OnItemClickListener() {
 
 				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 					OverlayTypeView item = (OverlayTypeView) adapter.getView(position, view, parent);
 					item.toggle();
 					AMMItemizedOverlay mur = (AMMItemizedOverlay)((IItemizedOverlay) overlays.get(position));
 					mur.setActive(item.isChecked());
 					
 				}
 			});
 
 			return new AlertDialog.Builder(getActivity())
 					.setTitle(R.string.displayed_poi_title).setView(listView)
 					.setCancelable(false)
 					.create();
 		}
 		@Override
 		public void onDismiss(DialogInterface dialog) {
 			super.onDismiss(dialog);
 			mMapView.setOverylays();
 			mMapView.invalidate();
 		}
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		if (requestCode == AddPOIActivity.NEW_POI_REQUEST) {
 			if (resultCode == RESULT_OK) {
 				GeoPoint geopoint = (GeoPoint) data
 						.getSerializableExtra(AddPOIActivity.KEY_GEOPOINT);
 				String name = data.getStringExtra(AddPOIActivity.KEY_POI_NAME);
 				String type = data.getStringExtra(AddPOIActivity.KEY_POI_TYPE);
 				String descr = data
 						.getStringExtra(AddPOIActivity.KEY_POI_DESCR);
 
 				double latitude = geopoint.getLatitudeE6() / 1000000.0;
 				double longitude = geopoint.getLongitudeE6() / 1000000.0;
 
 				Map<String, String> attribs = new HashMap<String, String>();
 				attribs.put("description", descr);
 
 				POI poi = new POI(-2, name, latitude, longitude, type, attribs);
 				mMapView.getAMMOverlayManager().addOverlay(poi);
 			}
 		}
 		super.onActivityResult(requestCode, resultCode, data);
 	}
 
 	@Override
 	protected void onStart() {
 		super.onStart();
 
 		LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
 		final boolean gpsEnabled = locationManager
 				.isProviderEnabled(LocationManager.GPS_PROVIDER);
 
 		if (!gpsEnabled) {
 			new EnableGpsDialogFragment().show(getFragmentManager(),
 					"enableGpsDialog");
 		}
 	}
 
 	private class EnableGpsDialogFragment extends DialogFragment {
 
 		@Override
 		public Dialog onCreateDialog(Bundle savedInstanceState) {
 			return new AlertDialog.Builder(getActivity())
 					.setTitle(R.string.enable_gps)
 					.setMessage(R.string.enable_gps_dialog)
 					.setPositiveButton(R.string.enable_gps,
 							new DialogInterface.OnClickListener() {
 								// @Override
 								public void onClick(DialogInterface dialog,
 										int which) {
 									enableLocationSettings();
 								}
 							}).setNegativeButton(R.string.server_gps, null)
 					.create();
 		}
 	}
 
 	@Override
 	protected void onStop() {
 		super.onStop();
 		locationManager.removeUpdates(listener);
 		Server.getInstance().stopServer();
 	}
 
 	private void enableLocationSettings() {
 		Intent settingsIntent = new Intent(
 				Settings.ACTION_LOCATION_SOURCE_SETTINGS);
 		startActivity(settingsIntent);
 	}
 
 	private final LocationListener listener = new LocationListener() {
 
 		public void onStatusChanged(String provider, int status, Bundle extras) {
 			// TODO Auto-generated method stub
 
 		}
 
 		public void onProviderEnabled(String provider) {
 			// TODO Auto-generated method stub
 
 		}
 
 		public void onProviderDisabled(String provider) {
 			// TODO Auto-generated method stub
 
 		}
 
 		public void onLocationChanged(Location location) {
 			CharSequence loc = "Lat: " + location.getLatitude() + "\n Lon: "
 					+ location.getLongitude();
 			Log.d("AMM", "loc: " + loc);
 			// Toast.makeText(MainActivity.this, loc, Toast.LENGTH_LONG).show();
 
 		}
 	};
 
 	public void onClick(View v) {
 		if (v.getId() == R.id.compass) {
 			mNorthUp = !mNorthUp;
 			String s;
 			if (mNorthUp) {
 				s = getString(R.string.north_up);
 			} else {
 				s = getString(R.string.heading_up);
 			}
 			Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
 		} else if (v.getId() == R.id.find_me) {
 			// Toast.makeText(this, "centering on device location",
 			// Toast.LENGTH_SHORT).show();
 			mMapView.getController().setCenter(deviceOverlay.getMyLocation());
 		}
 
 	}
 
 	private void updatePOIandScreen() {
 		for (IItemizedOverlay type : mMapView.getAMMOverlayManager()
 				.getOverlays()) {
 			Iterator<POI> iter = type.getOverlays().iterator();
 			while (iter.hasNext()) {
 				POI testPoint = iter.next();
 				if (testPoint.getUID() >= 0) {
 					iter.remove();
 				}
 			}
 
 			for (POI point : Server.getInstance().POIelements.values()) {
 				if (point.getType().equalsIgnoreCase(type.getName())) {
 					type.addOverlay(point);
 				}
 				if (point.getUID() == UID_to_track) {
 					mMapView.getController().setCenter(point.getGeoPoint());
 				}
 			}
 		}
 		Log.d("status", "over");
 
 		this.mMapView.invalidate();
 	}
 
 	public void setUIDtoTrack(int UID) {
 		UID_to_track = UID;
 		Log.d("AMM", "tracking UID: " + UID_to_track);
 	}
 
 	public LocationProvider getLocationProvider() {
 		return provider;
 	}
 
 	@Override
 	public SharedPreferences getSharedPreferences(String name, int mode) {
 		// TODO Auto-generated method stub
 		return super.getSharedPreferences(name, mode);
 	}
 }
