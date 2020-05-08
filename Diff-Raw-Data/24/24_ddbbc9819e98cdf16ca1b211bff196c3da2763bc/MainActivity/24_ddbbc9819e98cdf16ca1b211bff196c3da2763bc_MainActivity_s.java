 package com.cruzroja.creuroja;
 
 import java.util.ArrayList;
 
 import android.annotation.SuppressLint;
 import android.annotation.TargetApi;
 import android.app.ActionBar;
 import android.app.ActionBar.OnNavigationListener;
 import android.content.SharedPreferences;
 import android.graphics.Color;
 import android.graphics.drawable.ColorDrawable;
 import android.net.ConnectivityManager;
 import android.os.Build;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.LoaderManager.LoaderCallbacks;
 import android.support.v4.content.Loader;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.CheckBox;
 import android.widget.CompoundButton;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 import android.widget.SpinnerAdapter;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.google.android.gms.maps.CameraUpdateFactory;
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
 import com.google.android.gms.maps.SupportMapFragment;
 import com.google.android.gms.maps.model.BitmapDescriptorFactory;
 import com.google.android.gms.maps.model.LatLng;
 import com.google.android.gms.maps.model.Marker;
 import com.google.android.gms.maps.model.MarkerOptions;
 
 public class MainActivity extends FragmentActivity implements
 		LoaderCallbacks<ArrayList<Location>>, OnCheckedChangeListener {
 	public static final int LOADER_CONNECTION = 1;
 	public static final String LOCATIONS = "Locations";
 	protected static final int MAP_STYLE_NORMAL = 0;
 	protected static final int MAP_STYLE_HYBRID = 1;
 	protected static final int MAP_STYLE_TERRAIN = 2;
 	protected static final int MAP_STYLE_SATELLITE = 3;
 
 	private static final String SHOW_ASAMBLEA = "showAsamblea";
 	private static final String SHOW_BRAVO = "showBravo";
 	private static final String SHOW_CUAP = "showCuap";
 	private static final String SHOW_EMBARCACION = "showEmbarcacion";
 	private static final String SHOW_HOSPITAL = "showHospital";
 	private static final String SHOW_PREVENTIVO = "showPreventivo";
 	private static final String MAP_STYLE = "mapStyle";
 	private static final String IS_FIRST_RUN = "isFirstRun";
 
 	GoogleMap mGoogleMap;
 	ArrayList<Location> mLocationsList;
 
 	CheckBox mAsambleaCheckBox;
 	CheckBox mBravoCheckBox;
 	CheckBox mCuapCheckBox;
 	CheckBox mEmbarcacionCheckBox;
 	CheckBox mHospitalCheckBox;
 	CheckBox mPreventivoCheckBox;
 
 	View mMarkerPanel;
 
 	boolean isMarkerPanelShowing;
 
 	SharedPreferences prefs;
 
 	@SuppressLint("NewApi")
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		prefs = PreferenceManager.getDefaultSharedPreferences(this);
 		if (prefs.getBoolean(IS_FIRST_RUN, true)) {
 			makeFirstRun();
 		}
 
 		setContentView(R.layout.activity_main);
 
 		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
 			setActionBar();
 		}
 		setMap();
 
 		if (savedInstanceState != null) {
 			if (mLocationsList == null) {
 				mLocationsList = new ArrayList<Location>();
 				for (int i = 0; i < savedInstanceState.getInt(LOCATIONS, 0); i++) {
 					mLocationsList.add((Location) savedInstanceState
 							.getSerializable(Integer.toString(i)));
 				}
 				drawMarkers();
 			}
 		} else {
 
 			mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(
 					41.3958, 2.1739), 12));
			if (isConnected()) {
				// If the device is connected to the internet, start the
				// download
				downloadData();
			} else {
				// TODO: Here comes what to do without a valid connection, such
				// as showing old markers or whatever
			}
 		}
 
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_main, menu);
 		return true;
 	}
 
 	@Override
 	protected void onSaveInstanceState(Bundle outState) {
 		super.onSaveInstanceState(outState);
 		if (mLocationsList == null) {
 			return;
 		}
 		outState.putInt(LOCATIONS, mLocationsList.size());
 		for (int i = 0; i < mLocationsList.size(); i++) {
 			outState.putSerializable(Integer.toString(i), mLocationsList.get(i));
 		}
 	}
 
 	@Override
 	public void onBackPressed() {
 		if (isMarkerPanelShowing) {
 			mMarkerPanel.setVisibility(View.GONE);
 			isMarkerPanelShowing = false;
 		} else {
 			super.onBackPressed();
 		}
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.menu_locate:
 			moveToLocation();
 			return true;
 		case android.R.id.home:
 		case R.id.menu_show_panel:
 			showMarkerPanel();
 			return true;
 		case R.id.menu_refresh:
 			downloadData();
 			return true;
 		case R.id.menu_show_hybrid:
 			return setMapStyle(MAP_STYLE_HYBRID);
 		case R.id.menu_show_normal:
 			return setMapStyle(MAP_STYLE_NORMAL);
 		case R.id.menu_show_satellite:
 			return setMapStyle(MAP_STYLE_SATELLITE);
 		case R.id.menu_show_terrain:
 			return setMapStyle(MAP_STYLE_TERRAIN);
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	private void moveToLocation() {
 		if (mGoogleMap != null) {
 			if (mGoogleMap.getMyLocation() != null) {
 				mGoogleMap.animateCamera(CameraUpdateFactory
 						.newLatLng(new LatLng(mGoogleMap.getMyLocation()
 								.getLatitude(), mGoogleMap.getMyLocation()
 								.getLongitude())));
 			} else {
 				Toast.makeText(this, R.string.locating, Toast.LENGTH_SHORT)
 						.show();
 			}
 		}
 	}
 
 	private void downloadData() {
		getSupportLoaderManager().restartLoader(LOADER_CONNECTION, null, this);
 	}
 
 	private void makeFirstRun() {
 		prefs.edit().putBoolean(IS_FIRST_RUN, false).commit();
 	}
 
 	private void showMarkerPanel() {
 		if (isMarkerPanelShowing) {
 			mMarkerPanel.setVisibility(View.GONE);
 			isMarkerPanelShowing = false;
 		} else {
 			prepareMarkerPanel();
 			isMarkerPanelShowing = true;
 			mMarkerPanel.setVisibility(View.VISIBLE);
 		}
 
 	}
 
 	private void prepareMarkerPanel() {
 		if (mMarkerPanel == null) {
 			mMarkerPanel = findViewById(R.id.marker_panel);
 			mAsambleaCheckBox = (CheckBox) findViewById(R.id.checkbox_asamblea);
 			mBravoCheckBox = (CheckBox) findViewById(R.id.checkbox_bravo);
 			mCuapCheckBox = (CheckBox) findViewById(R.id.checkbox_cuap);
 			mEmbarcacionCheckBox = (CheckBox) findViewById(R.id.checkbox_embarcacion);
 			mHospitalCheckBox = (CheckBox) findViewById(R.id.checkbox_hospital);
 			mPreventivoCheckBox = (CheckBox) findViewById(R.id.checkbox_preventivo);
 
 			mAsambleaCheckBox.setChecked(prefs.getBoolean(SHOW_ASAMBLEA, true));
 			mBravoCheckBox.setChecked(prefs.getBoolean(SHOW_BRAVO, true));
 			mCuapCheckBox.setChecked(prefs.getBoolean(SHOW_CUAP, true));
 			mEmbarcacionCheckBox.setChecked(prefs.getBoolean(SHOW_EMBARCACION,
 					true));
 			mHospitalCheckBox.setChecked(prefs.getBoolean(SHOW_HOSPITAL, true));
 			mPreventivoCheckBox.setChecked(prefs.getBoolean(SHOW_PREVENTIVO,
 					true));
 
 			mAsambleaCheckBox.setOnCheckedChangeListener(this);
 			mBravoCheckBox.setOnCheckedChangeListener(this);
 			mCuapCheckBox.setOnCheckedChangeListener(this);
 			mEmbarcacionCheckBox.setOnCheckedChangeListener(this);
 			mHospitalCheckBox.setOnCheckedChangeListener(this);
 			mPreventivoCheckBox.setOnCheckedChangeListener(this);
 		}
 	}
 
 	private boolean isConnected() {
 		ConnectivityManager manager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
 		return manager.getActiveNetworkInfo().isConnected();
 	}
 
 	private void setMap() {
 		if (mGoogleMap == null) {
 			mGoogleMap = ((SupportMapFragment) getSupportFragmentManager()
 					.findFragmentById(R.id.map)).getMap();
 		}
 		setMapStyle(prefs.getInt(MAP_STYLE, MAP_STYLE_NORMAL));
 		mGoogleMap.setMyLocationEnabled(true);
 		mGoogleMap.getUiSettings().setZoomControlsEnabled(false);
 		mGoogleMap.getUiSettings().setMyLocationButtonEnabled(false);
 		mGoogleMap.getUiSettings().setAllGesturesEnabled(true);
 	}
 
 	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
 	private void setActionBar() {
 		ActionBar actionBar = getActionBar();
 		actionBar.setBackgroundDrawable(new ColorDrawable(Color
 				.parseColor("#CC0000")));
 		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
 		actionBar.setDisplayHomeAsUpEnabled(true);
 		int myPosition = prefs.getInt(MAP_STYLE, MAP_STYLE_NORMAL);
 		actionBar.setListNavigationCallbacks(getMapStyleAdapter(),
 				getMapStyleListener());
 		actionBar.setSelectedNavigationItem(myPosition);
 	}
 
 	private SpinnerAdapter getMapStyleAdapter() {
 		return new ArrayAdapter<String>(this,
 				android.R.layout.simple_spinner_dropdown_item,
 				android.R.id.text1, getResources().getStringArray(
 						R.array.map_styles));
 	}
 
 	private OnNavigationListener getMapStyleListener() {
 		return new OnNavigationListener() {
 			@Override
 			public boolean onNavigationItemSelected(int itemPosition,
 					long itemId) {
 				return setMapStyle(itemPosition);
 			}
 		};
 	}
 
 	private boolean setMapStyle(int mapStyle) {
 		if (mGoogleMap == null) {
 			return false;
 		}
 
 		switch (mapStyle) {
 		case MAP_STYLE_NORMAL:
 			mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
 			break;
 		case MAP_STYLE_HYBRID:
 			mGoogleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
 			break;
 		case MAP_STYLE_SATELLITE:
 			mGoogleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
 			break;
 		case MAP_STYLE_TERRAIN:
 			mGoogleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
 			break;
 		default:
 			return false;
 		}
 		prefs.edit().putInt(MAP_STYLE, mapStyle).commit();
 		return true;
 	}
 
 	private void drawMarkers() {
 		// TODO add markers to the map.
 		if (mGoogleMap == null || mLocationsList == null) {
 			return;
 		}
 
 		mGoogleMap.clear();
 		for (int i = 0; i < mLocationsList.size(); i++) {
 			if (!shouldShowMarker(mLocationsList.get(i))) {
 				continue;
 			}
 
 			MarkerOptions marker = new MarkerOptions().position(mLocationsList
 					.get(i).getPosition());
 			if (mLocationsList.get(i).mIcono != 0) {
 				marker.icon(BitmapDescriptorFactory.fromResource(mLocationsList
 						.get(i).mIcono));
 			}
 			if (mLocationsList.get(i).mContenido.mNombre != null) {
 				marker.title(mLocationsList.get(i).mContenido.mNombre);
 			}
 			if (mLocationsList.get(i).mContenido.mSnippet != null) {
 				marker.snippet(mLocationsList.get(i).mContenido.mSnippet);
 			}
 
 			mGoogleMap.addMarker(marker);
 		}
 		mGoogleMap.setInfoWindowAdapter(new MarkerAdapter());
 	}
 
 	private boolean shouldShowMarker(Location location) {
 		switch (location.mIcono) {
 		case R.drawable.asamblea:
 			return prefs.getBoolean(SHOW_ASAMBLEA, true);
 		case R.drawable.bravo:
 			return prefs.getBoolean(SHOW_BRAVO, true);
 		case R.drawable.cuap:
 			return prefs.getBoolean(SHOW_CUAP, true);
 		case R.drawable.embarcacion:
 			return prefs.getBoolean(SHOW_EMBARCACION, true);
 		case R.drawable.hospital:
 			return prefs.getBoolean(SHOW_HOSPITAL, true);
 		case R.drawable.preventivo:
 			return prefs.getBoolean(SHOW_PREVENTIVO, true);
 		default:
 			return true;
 		}
 	}
 
 	@Override
 	public Loader<ArrayList<Location>> onCreateLoader(int id, Bundle args) {
 		Loader<ArrayList<Location>> loader = null;
 		switch (id) {
 		case LOADER_CONNECTION:
 			loader = new ConnectionLoader(this, args);
 			break;
 		}
 		return loader;
 	}
 
 	@Override
 	public void onLoadFinished(Loader<ArrayList<Location>> loader,
 			ArrayList<Location> locations) {
 		switch (loader.getId()) {
 		case LOADER_CONNECTION:
 			if (locations == null) {
 				return;
 			}
 			mLocationsList = locations;
 			drawMarkers();
 			break;
 		}
 	}
 
 	@Override
 	public void onLoaderReset(Loader<ArrayList<Location>> loader) {
 	}
 
 	private class MarkerAdapter implements InfoWindowAdapter {
 		@Override
 		public View getInfoWindow(Marker marker) {
 			return null;
 		}
 
 		@Override
 		public View getInfoContents(Marker marker) {
 			View v = getLayoutInflater().inflate(R.layout.map_marker, null);
 
 			((TextView) v.findViewById(R.id.location)).setText(marker
 					.getTitle());
 
 			if (marker.getSnippet() != null) {
 				String address = marker.getSnippet().substring(0,
 						marker.getSnippet().indexOf(Location.MARKER_NEW_LINE));
 				String other = marker.getSnippet().substring(
 						marker.getSnippet().indexOf(Location.MARKER_NEW_LINE)
 								+ Location.MARKER_NEW_LINE.length(),
 						marker.getSnippet().length());
 				((TextView) v.findViewById(R.id.address)).setText(address);
 				((TextView) v.findViewById(R.id.other_information))
 						.setText(other);
 			}
 
 			return v;
 		}
 	}
 
 	@Override
 	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
 		SharedPreferences.Editor editor = prefs.edit();
 		switch (buttonView.getId()) {
 		case R.id.checkbox_asamblea:
 			editor.putBoolean(SHOW_ASAMBLEA, isChecked);
 			break;
 		case R.id.checkbox_bravo:
 			editor.putBoolean(SHOW_BRAVO, isChecked);
 			break;
 		case R.id.checkbox_cuap:
 			editor.putBoolean(SHOW_CUAP, isChecked);
 			break;
 		case R.id.checkbox_embarcacion:
 			editor.putBoolean(SHOW_EMBARCACION, isChecked);
 			break;
 		case R.id.checkbox_hospital:
 			editor.putBoolean(SHOW_HOSPITAL, isChecked);
 			break;
 		case R.id.checkbox_preventivo:
 			editor.putBoolean(SHOW_PREVENTIVO, isChecked);
 			break;
 		}
 		editor.commit();
 		drawMarkers();
 	}
 }
