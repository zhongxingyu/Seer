 package com.cruzroja.android.ui.activities;
 
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.SearchManager;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.IntentSender;
 import android.content.SharedPreferences;
 import android.database.Cursor;
 import android.graphics.Color;
 import android.graphics.drawable.ColorDrawable;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.support.v4.app.DialogFragment;
 import android.support.v4.app.LoaderManager;
 import android.support.v4.content.CursorLoader;
 import android.support.v4.content.Loader;
 import android.support.v4.view.MenuItemCompat;
 import android.support.v7.app.ActionBar;
 import android.support.v7.app.ActionBarActivity;
 import android.support.v7.widget.SearchView;
 import android.text.util.Linkify;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.*;
 import com.cruzroja.android.R;
 import com.cruzroja.android.app.Location;
 import com.cruzroja.android.app.Settings;
 import com.cruzroja.android.app.loaders.DirectionsLoader;
 import com.cruzroja.android.app.utils.LocationDownloader;
 import com.cruzroja.android.app.utils.ConnectionClient;
 import com.cruzroja.android.app.utils.LocationsProvider;
 import com.cruzroja.android.database.CreuRojaContract;
 import com.google.android.gms.common.ConnectionResult;
 import com.google.android.gms.common.GooglePlayServicesClient;
 import com.google.android.gms.common.GooglePlayServicesUtil;
 import com.google.android.gms.location.LocationClient;
 import com.google.android.gms.maps.CameraUpdateFactory;
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.SupportMapFragment;
 import com.google.android.gms.maps.model.LatLng;
 import com.google.android.gms.maps.model.Marker;
 import com.google.android.gms.maps.model.Polyline;
 import com.google.android.gms.maps.model.PolylineOptions;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 public class MainActivity extends ActionBarActivity implements
         GooglePlayServicesClient.ConnectionCallbacks,
         GooglePlayServicesClient.OnConnectionFailedListener,
         CompoundButton.OnCheckedChangeListener, SearchView.OnQueryTextListener {
     private static final int ACTIVITY_LOGIN = 1;
     private static final int LOADER_SHOW_LOCATIONS = 1;
     private static final int LOADER_GET_DIRECTIONS = 2;
     private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
     private final static String POLYLINE = "polyline";
     public boolean isMarkerPanelShowing = false;
     private SharedPreferences prefs;
     private Polyline mPolyline;
     private List<LatLng> mDirections;
     private GoogleMap mGoogleMap;
     private View mMarkerPanelView;
     private LocationClient mLocationClient;
     private String mFilter;
     private Map<Marker, Location> mMarkerLocationMap = new HashMap<>();
     private LocationCard mLocationCard;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
 
         if (prefs.contains(Settings.ACCESS_TOKEN)) {
             LocationsProvider.checkExpiredLocations(getContentResolver());
             showMap();
             downloadNewData();
         } else {
             showLogin();
         }
         mLocationClient = new LocationClient(getApplicationContext(), this, this);
         if(savedInstanceState != null && savedInstanceState.containsKey(POLYLINE)){
             //Retrieve the codified polyline from it
             mDirections = new ArrayList<>();
             double[] array = savedInstanceState.getDoubleArray(POLYLINE);
             for(int i = 0; i < array.length; i++){
                 mDirections.add(new LatLng(array[i++], array[i]));
             }
         }
     }
 
     @Override
     public void onResume() {
         super.onResume();
         //The map *must* be loaded after the fragment has been shown in screen, thus we separate
         // it from the call that loads the fragment (setContentView)
         if (mMarkerPanelView != null) {
             loadMapAndMarkers();
         }
     }
 
     @Override
     public void onStart() {
         super.onStart();
         // Connect the location (play services) client.
         if (mLocationClient != null) {
             mLocationClient.connect();
         }
     }
 
     @Override
     protected void onSaveInstanceState(Bundle outState) {
         //Codify an array with the polyline points
         if(mDirections != null){
             double[] polyline = new double[mDirections.size() * 2];
             int i = 0;
             for (LatLng latLng : mDirections) {
                 polyline[i++] = latLng.latitude;
                 polyline[i++] = latLng.longitude;
             }
             outState.putDoubleArray(POLYLINE, polyline);
         }
         super.onSaveInstanceState(outState);
     }
 
     @Override
     public void onStop() {
         super.onStop();
         // Disconnecting the client invalidates it.
         if (mLocationClient != null) {
             mLocationClient.disconnect();
         }
 
     }
 
     @Override
     public void onBackPressed() {
         if (isMarkerPanelShowing) {
             showMarkerPanel();
         } else {
             super.onBackPressed();
         }
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_map, menu);
         setSearchOptions(menu);
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case R.id.menu_locate:
                 moveToCurrentPosition();
                 return true;
             case R.id.search:
                 // Nothing needed here thanks to the support library
                 return true;
             case android.R.id.home:
             case R.id.menu_show_panel:
                 showMarkerPanel();
                 return true;
             case R.id.menu_refresh:
                 downloadNewData();
                 return true;
             case R.id.menu_show_hybrid:
                 setMapType(GoogleMap.MAP_TYPE_HYBRID);
                 return true;
             case R.id.menu_show_normal:
                 setMapType(GoogleMap.MAP_TYPE_NORMAL);
                 return true;
             case R.id.menu_show_satellite:
                 setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                 return true;
             case R.id.menu_show_terrain:
                 setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                 return true;
             default:
                 return super.onOptionsItemSelected(item);
         }
     }
 
     @Override
     public void onActivityResult(int requestCode, int resultCode, Intent data) {
         super.onActivityResult(requestCode, resultCode, data);
         switch (requestCode) {
             case CONNECTION_FAILURE_RESOLUTION_REQUEST:
                 // If the result code is Activity.RESULT_OK, try to connect again
                 switch (resultCode) {
                     case RESULT_OK:
                         requestGooglePlayServicesAvailability();
                         break;
                     default:
                         break;
                 }
                 break;
             case ACTIVITY_LOGIN:
                 switch (resultCode) {
                     case RESULT_OK:
                         showMap();
                         break;
                     default:
                         finish();
                 }
                 break;
             default:
                 break;
         }
     }
 
     @Override
     public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
         //TODO: move stuff around, i dont like this shit
         SharedPreferences.Editor editor = prefs.edit();
         switch (buttonView.getId()) {
             case R.id.checkbox_adaptadas:
                 editor.putBoolean(Settings.SHOW_ADAPTADAS, isChecked);
                 break;
             case R.id.checkbox_asamblea:
                 editor.putBoolean(Settings.SHOW_ASAMBLEA, isChecked);
                 break;
             case R.id.checkbox_bravo:
                 editor.putBoolean(Settings.SHOW_BRAVO, isChecked);
                 break;
             case R.id.checkbox_cuap:
                 editor.putBoolean(Settings.SHOW_CUAP, isChecked);
                 break;
             case R.id.checkbox_hospital:
                 editor.putBoolean(Settings.SHOW_HOSPITAL, isChecked);
                 break;
             case R.id.checkbox_maritimo:
                 editor.putBoolean(Settings.SHOW_MARITIMO, isChecked);
                 break;
             case R.id.checkbox_terrestre:
                 editor.putBoolean(Settings.SHOW_TERRESTRE, isChecked);
                 break;
             case R.id.checkbox_nostrum:
                 editor.putBoolean(Settings.SHOW_NOSTRUM, isChecked);
                 break;
         }
         editor.commit();
         drawMarkers();
     }
 
     @Override
     public boolean onQueryTextChange(String newText) {
         mFilter = newText;
         //TODO shouldnt redraw.
         drawMarkers();
         return false;
     }
 
     @Override
     public boolean onQueryTextSubmit(String query) {
         mFilter = query;
         drawMarkers();
         return false;
     }
 
     @Override
     public void onConnected(Bundle args) {
     }
 
     @Override
     public void onDisconnected() {
     }
 
     @Override
     public void onConnectionFailed(ConnectionResult connectionResult) {
         /* Google Play services can resolve some errors it detects. If the error has a resolution,
         * try sending an Intent to start a Google Play services activity that can resolve error.
         */
         if (connectionResult.hasResolution()) {
             try {
                 // Start an Activity that tries to resolve the error
                 connectionResult.startResolutionForResult(this,
                         CONNECTION_FAILURE_RESOLUTION_REQUEST);
                 // Thrown if Google Play services canceled the original PendingIntent
             } catch (IntentSender.SendIntentException e) {
                 // Log the error
                 e.printStackTrace();
             }
         } else {
             Toast.makeText(getApplicationContext(), R.string.error_location_unavailable,
                     Toast.LENGTH_LONG).show();
         }
     }
 
     private void showMap() {
         requestGooglePlayServicesAvailability();
 
         setContentView(R.layout.activity_main);
 
         setActionBar();
         prepareViews();
     }
 
     private void showLogin() {
         Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
         startActivityForResult(intent, ACTIVITY_LOGIN);
     }
 
     private void setActionBar() {
         ActionBar actionBar = getSupportActionBar();
         actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#CC0000")));
         actionBar.setDisplayHomeAsUpEnabled(true);
     }
 
     private void prepareViews() {
         mMarkerPanelView = findViewById(R.id.marker_panel);
 
         if (mMarkerPanelView != null) {
             setCheckboxesVisibility();
         }
     }
 
     private void setCheckboxesVisibility() {
         List<Location.Type> availableTypes = Location.Type.getAvailableTypes(getContentResolver());
         setCheckBoxVisibility(availableTypes, R.id.checkbox_adaptadas, R.id.box_adaptadas,
                 Settings.SHOW_ADAPTADAS);
         setCheckBoxVisibility(availableTypes, R.id.checkbox_asamblea, R.id.box_asamblea,
                 Settings.SHOW_ASAMBLEA);
         setCheckBoxVisibility(availableTypes, R.id.checkbox_bravo, R.id.box_bravo,
                 Settings.SHOW_BRAVO);
         setCheckBoxVisibility(availableTypes, R.id.checkbox_cuap, R.id.box_cuap,
                 Settings.SHOW_CUAP);
         setCheckBoxVisibility(availableTypes, R.id.checkbox_hospital, R.id.box_hospital,
                 Settings.SHOW_HOSPITAL);
         setCheckBoxVisibility(availableTypes, R.id.checkbox_maritimo, R.id.box_maritimo,
                 Settings.SHOW_MARITIMO);
         setCheckBoxVisibility(availableTypes, R.id.checkbox_nostrum, R.id.box_nostrum,
                 Settings.SHOW_NOSTRUM);
         setCheckBoxVisibility(availableTypes, R.id.checkbox_social, R.id.box_social,
                 Settings.SHOW_SOCIAL);
         setCheckBoxVisibility(availableTypes, R.id.checkbox_terrestre, R.id.box_terrestre,
                 Settings.SHOW_TERRESTRE);
     }
 
     private void setCheckBoxVisibility(List<Location.Type> availableTypes, int checkBoxResId,
                                        int boxResId, String showTag) {
         CheckBox checkBox = (CheckBox) mMarkerPanelView.findViewById(checkBoxResId);
         View box = mMarkerPanelView.findViewById(boxResId);
         box.setVisibility(availableTypes.contains(Location.Type.getType(showTag)) ?
                 View.VISIBLE : View.GONE);
         checkBox.setChecked(prefs.getBoolean(showTag, true));
         checkBox.setOnCheckedChangeListener(this);
 
     }
 
     private void loadMapAndMarkers() {
         if (mGoogleMap == null) {
             mGoogleMap = ((SupportMapFragment) getSupportFragmentManager()
                     .findFragmentById(R.id.map)).getMap();
         }
         if (mGoogleMap != null) {
            mGoogleMap.clear();
             if(prefs.contains(Settings.MAP_TYPE)){
                 setMapType(prefs.getInt(Settings.MAP_TYPE, GoogleMap.MAP_TYPE_NORMAL));
             }
             mGoogleMap.setMyLocationEnabled(true);
             mGoogleMap.getUiSettings().setMyLocationButtonEnabled(false);
             if(mDirections != null){
                 drawDirections(mDirections);
             }
         }
         getSupportLoaderManager().restartLoader(LOADER_SHOW_LOCATIONS, null,
                 new LocationsLoaderHelper());
     }
 
     private void downloadNewData() {
         new Thread(new LocationDownloader(prefs.getString(Settings.ACCESS_TOKEN, ""),
                 Settings.getLastUpdateTime(getContentResolver()),
                 getContentResolver())).start();
 
     }
 
     private void showMarkerPanel() {
         if (mMarkerPanelView != null) {
             mMarkerPanelView.setVisibility((isMarkerPanelShowing) ? View.GONE : View.VISIBLE);
             isMarkerPanelShowing = !isMarkerPanelShowing;
         }
     }
 
     private void setMapType(int mapType) {
         if (mGoogleMap == null) {
             return;
         }
         mGoogleMap.setMapType(mapType);
         prefs.edit().putInt(Settings.MAP_TYPE, mGoogleMap.getMapType()).commit();
     }
 
     private void moveToCurrentPosition() {
         if (mGoogleMap != null) {
             if (areLocationServicesEnabled()) {
                 if (mLocationClient.getLastLocation() != null) {
                     mGoogleMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(
                             mLocationClient.getLastLocation().getLatitude(),
                             mLocationClient.getLastLocation().getLongitude())));
                 } else {
                     Toast.makeText(getApplicationContext(), R.string.locating, Toast.LENGTH_SHORT)
                             .show();
                 }
             } else {
                 showLocationSettings();
             }
         }
     }
 
     private void drawMarkers() {
         if (mGoogleMap == null || mMarkerLocationMap == null) {
             return;
         }
 
         for(Marker marker : mMarkerLocationMap.keySet()){
             marker.setVisible(mMarkerLocationMap.get(marker).shouldBeShown(mFilter, prefs));
         }
     }
 
     private void getDirections(Location location){
         if (areLocationServicesEnabled()) {
             if (mLocationClient.getLastLocation() != null) {
                 Bundle args = new Bundle();
                 args.putDouble(DirectionsLoader.ARG_ORIG_LAT, mLocationClient.getLastLocation()
                         .getLatitude());
                 args.putDouble(DirectionsLoader.ARG_ORIG_LONG, mLocationClient.getLastLocation()
                         .getLongitude());
                 getSupportLoaderManager().restartLoader(LOADER_GET_DIRECTIONS, args,
                         new DirectionsLoaderHelper(location));
             } else {
                 Toast.makeText(getApplicationContext(), R.string.locating, Toast.LENGTH_SHORT)
                         .show();
             }
         } else {
             showLocationSettings();
         }
     }
 
     private void drawDirections(List<LatLng> points) {
         if (mPolyline != null) {
             mPolyline.remove();
         }
         if (mGoogleMap == null || points == null) {
             return;
         }
         if (points.size() == 0) {
             Toast.makeText(getApplicationContext(), R.string.error_limit_reached,
                     Toast.LENGTH_LONG).show();
         }
 
         mDirections = points;
         mPolyline = mGoogleMap.addPolyline(new PolylineOptions().addAll(points)
                 .color(Color.parseColor("#CC0000")));
     }
 
     private void setSearchOptions(Menu menu) {
         SearchManager searchManager = (SearchManager) getApplicationContext()
                 .getSystemService(SEARCH_SERVICE);
         MenuItem searchMenuItem = menu.findItem(R.id.search);
         SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchMenuItem);
         if (searchView != null) {
             searchView.setSearchableInfo(searchManager.getSearchableInfo(this
                     .getComponentName()));
 
             searchView.setOnQueryTextListener(this);
         }
     }
 
     private boolean areLocationServicesEnabled() {
         LocationManager lm = (LocationManager) getApplicationContext()
                 .getSystemService(LOCATION_SERVICE);
         return (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
                 || lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER));
     }
 
     private boolean requestGooglePlayServicesAvailability() {
         int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(
                 getApplicationContext());
         if (ConnectionResult.SUCCESS == resultCode) {
             return true;
         } else {
             Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                     CONNECTION_FAILURE_RESOLUTION_REQUEST);
 
             // If Google Play services can provide an error dialog, show it
             if (errorDialog != null) {
                 ErrorDialogFragment errorFragment = new ErrorDialogFragment();
                 errorFragment.setDialog(errorDialog);
                 errorFragment.show(getSupportFragmentManager(), "Location Updates");
             }
         }
         return false;
     }
 
     private void showLocationSettings() {
         AlertDialog.Builder builder = new AlertDialog.Builder(this);
         builder.setTitle(R.string.location_disabled_title);
         builder.setMessage(R.string.location_disabled_message);
         builder.setPositiveButton(R.string.open_location_settings,
                 new DialogInterface.OnClickListener() {
                     public void onClick(DialogInterface dialogInterface, int i) {
                         startActivity(new Intent(
                                 android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                     }
                 });
         builder.setNegativeButton(R.string.cancel, null);
         builder.create().show();
     }
 
     // Define a DialogFragment that displays the error dialog
     private static class ErrorDialogFragment extends DialogFragment {
         // Global field to contain the error dialog
         private Dialog mDialog;
 
         public ErrorDialogFragment() {
             super();
             mDialog = null;
         }
 
         // Set the dialog to display
         public void setDialog(Dialog dialog) {
             mDialog = dialog;
         }
 
         @Override
         public Dialog onCreateDialog(Bundle savedInstanceState) {
             return mDialog;
         }
     }
 
     private void createMarkers(Cursor locations){
         List<Location> locationList = LocationsProvider.getLocationList(locations);
         for (Location location : locationList) {
             mMarkerLocationMap.put(mGoogleMap.addMarker(location.getMarker()), location);
         }
         mGoogleMap.setInfoWindowAdapter(new MarkerAdapter());
     }
 
     private class LocationsLoaderHelper implements
             LoaderManager.LoaderCallbacks<Cursor> {
         @Override
         public CursorLoader onCreateLoader(int id, Bundle args) {
             CursorLoader loader;
             switch (id) {
                 case LOADER_SHOW_LOCATIONS:
                     loader = new CursorLoader(getApplicationContext(),
                             CreuRojaContract.Locations.CONTENT_LOCATIONS, null, null, null,
                             CreuRojaContract.Locations.DEFAULT_ORDER);
                     break;
                 default:
                     loader = null;
                     break;
             }
             return loader;
         }
 
         @Override
         public void onLoadFinished(Loader<Cursor> loader, Cursor locations) {
             switch(loader.getId()){
                 case LOADER_SHOW_LOCATIONS:
                     createMarkers(locations);
                     drawMarkers();
                     break;
                 default:
                     break;
             }
 
         }
 
         @Override
         public void onLoaderReset(Loader<Cursor> loader) {
         }
     }
 
     private class DirectionsLoaderHelper implements
             LoaderManager.LoaderCallbacks<List<LatLng>> {
         private Location mLocation;
 
         public DirectionsLoaderHelper(Location location) {
             mLocation = location;
         }
 
         @Override
         public Loader<List<LatLng>> onCreateLoader(int id, Bundle args) {
             if (mLocation == null) {
                 return null;
             }
             Loader<List<LatLng>> loader;
             switch (id) {
                 case LOADER_GET_DIRECTIONS:
                     if (ConnectionClient.isConnected(getApplicationContext())) {
                         return new DirectionsLoader(getApplicationContext(), args, mLocation);
                     } else {
                         Toast.makeText(getApplicationContext(), R.string.error_no_connection,
                                 Toast.LENGTH_LONG).show();
                         return null;
                     }
 
                 default:
                     loader = null;
             }
             return loader;
         }
 
         @Override
         public void onLoadFinished(Loader<List<LatLng>> loader, List<LatLng> directions) {
             switch (loader.getId()) {
                 case LOADER_GET_DIRECTIONS:
                     drawDirections(directions);
                     break;
                 default:
                     break;
             }
         }
 
         @Override
         public void onLoaderReset(Loader<List<LatLng>> loader) {
         }
     }
 
     private class MarkerAdapter implements GoogleMap.InfoWindowAdapter {
         @Override
         public View getInfoWindow(Marker marker) {
             return null;
         }
 
         @Override
         public View getInfoContents(Marker marker) {
             mLocationCard = new LocationCard(mMarkerLocationMap.get(marker));
             return null;
         }
     }
 
     private class LocationCard {
         public final View mCard;
         public final Location mLocation;
 
         public LocationCard(Location location){
             mCard = findViewById(R.id.location_card);
             mCard.setVisibility(View.VISIBLE);
             mLocation = location;
             setUpCard();
         }
 
         private void setUpCard(){
             ((TextView) mCard.findViewById(R.id.location_card_name)).setText(mLocation.mName);
             ((TextView) mCard.findViewById(R.id.location_card_address)).setText(mLocation.mAddress);
             ((TextView) mCard.findViewById(R.id.location_card_other)).setText(mLocation.mDetails);
             ((TextView) mCard.findViewById(R.id.location_card_other)).setAutoLinkMask(Linkify.PHONE_NUMBERS);
             mCard.findViewById(R.id.location_card_get_directions).setOnClickListener(
                     new View.OnClickListener() {
                         @Override
                         public void onClick(View view) {
                             if (ConnectionClient.isConnected(MainActivity.this) &&
                                     mLocationClient != null && mLocationClient.isConnected()) {
                                 getDirections(mLocation);
                                 switchDirectionsButton();
                             }
                         }
                     });
         }
 
         private void switchDirectionsButton(){
             //TODO: implement this shit.
         }
     }
 }
