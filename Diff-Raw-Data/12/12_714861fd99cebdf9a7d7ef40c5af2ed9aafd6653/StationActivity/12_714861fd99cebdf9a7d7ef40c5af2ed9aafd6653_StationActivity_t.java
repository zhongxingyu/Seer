 /**
  * *******************************************************************************************************************
  * <p/>
  * Copyright (C) 7/28/12 by Manuel Palacio
  * <p/>
  * **********************************************************************************************************************
  * <p/>
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
  * the License. You may obtain a copy of the License at
  * <p/>
  * http://www.apache.org/licenses/LICENSE-2.0
  * <p/>
  * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
  * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
  * specific language governing permissions and limitations under the License.
  * <p/>
  * **********************************************************************************************************************
  */
 package net.palacesoft.cngstation.client;
 
 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.location.Address;
 import android.location.Geocoder;
 import android.location.Location;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.*;
 import com.bugsense.trace.BugSenseHandler;
 import com.google.android.maps.*;
 import net.palacesoft.cngstation.R;
 import net.palacesoft.cngstation.client.loader.CityLoader;
 import net.palacesoft.cngstation.client.loader.CountryLoader;
 import net.palacesoft.cngstation.client.loader.StationLoader;
 import net.palacesoft.cngstation.client.mapoverlay.StationBalloonOverlay;
 import net.palacesoft.cngstation.client.mapoverlay.StationOverlayItem;
 import org.springframework.util.StringUtils;
 
 import java.io.IOException;
 import java.util.*;
 
 import static android.R.layout.simple_spinner_dropdown_item;
 import static android.R.layout.simple_spinner_item;
 import static org.springframework.util.StringUtils.hasText;
 
 public class StationActivity extends MapActivity {
 
     private MapView mapView;
     private MapController mapController;
     private StationBalloonOverlay stationOverlay;
     private Location currentLocation;
     private MyLocationOverlay myLocationOverlay;
     private Spinner countries, cities;
     private Address currentLocationAddress;
 
     private static final String COUNTRIES_URL = "http://fuelstationservice.appspot.com/countries";
     private static final String CITY_URL_v2 = "http://fuelstationservice.appspot.com/stations2/city/";
     private static final String CITIES_URL = "http://fuelstationservice.appspot.com/cities/country/";
 
     @Override
     protected boolean isRouteDisplayed() {
         return true;
     }
 
     public Location getCurrentLocation() {
         return currentLocation;
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         super.onCreateOptionsMenu(menu);
         MenuInflater menuInflater = getMenuInflater();
         menuInflater.inflate(R.menu.main_menu, menu);
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case R.id.refresh:
                 try {
                     Address locationAddress = lookupAddressFromLocation(currentLocation);
                     new StationLoader(this, locationAddress, getZoomLevel(), CITY_URL_v2).execute();
                 } catch (AddressEmptyException e) {
                     showInfoMessage("Could not determine your current location");
                 } catch (IOException e) {
                     showInfoMessage("Network error. Try refreshing.");
                 }
 
                 break;
 
             case R.id.search:
                 LinearLayout linearLayout = (LinearLayout) findViewById(R.id.searchLayout);
                 if (linearLayout.getVisibility() == View.VISIBLE) {
                     linearLayout.setVisibility(View.INVISIBLE);
                 } else {
                     linearLayout.setVisibility(View.VISIBLE);
                     if (countries == null) {
                         initSearchForm();
                         loadAvailableCountriesList();
                     }
                 }
                 break;
 
             case R.id.settings:
                 startActivity(new Intent(this, Preferences.class));
                 break;
 
             case R.id.cheapest:
                 try {
                     stationOverlay.popupCheapest();
                 } catch (Exception e) {
                     logError(e, "Problem showing the cheapest station");
                 }
                 break;
 
             case R.id.closest:
                 try {
                     stationOverlay.popupClosest();
                 } catch (Exception e) {
                     logError(e, "Problem showing the closest station");
                 }
                 break;
         }
 
         return false;
     }
 
     private void logError(Exception e, String text) {
         Map<String, String> extraData = new HashMap<String, String>();
         if (currentLocationAddress != null) {
             extraData.put("location", currentLocationAddress.getLocality());
         }
         BugSenseHandler.log(text, extraData, e);
         showInfoMessage(text);
     }
 
     @Override
     protected void onPause() {
         super.onPause();
         myLocationOverlay.disableMyLocation();
     }
 
     @Override
     protected void onResume() {
         super.onResume();
         myLocationOverlay.enableMyLocation();
     }
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         setContentView(R.layout.main);
 
         BugSenseHandler.setup(this, "4812ac69");
 
         initMapView();
 
         initMyLocationOverlay();
 
         addStationOverlay();
 
         startTrackingMyLocation(getZoomLevel());
     }
 
     private Integer getZoomLevel() {
         int distance = Preferences.getDistance(getApplicationContext());
         if (distance > 40) {
             return 9;
         }
 
         return 10;
     }
 
     private void initSearchForm() {
         countries = (Spinner) findViewById(R.id.countries);
         countries.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
             @Override
             public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {
 
                 String country = adapterView.getItemAtPosition(pos).toString();
                 new CityLoader(StationActivity.this, country).execute(CITIES_URL);
             }
 
             @Override
             public void onNothingSelected(AdapterView<?> adapterView) {
             }
         });
         cities = (Spinner) findViewById(R.id.cities);
 
 
         Button btnSubmit = (Button) findViewById(R.id.btnSubmit);
         btnSubmit.setOnClickListener(new View.OnClickListener() {
 
             @Override
             public void onClick(View v) {
                Object selection = cities.getSelectedItem();
 
                if (selection != null && hasText(selection.toString())) {
                    String city = selection.toString();
                     try {
                         Address address = lookupAddressFromLocationName(city, countries.getSelectedItem().toString());
                         new StationLoader(StationActivity.this, address, getZoomLevel(), CITY_URL_v2).execute();
                     } catch (Exception e) {
                         showInfoMessage("Problem finding CNG stations for chosen location");
                     }
                 }
             }
         });
     }
 
     private Address lookupAddressFromLocationName(String city, String country) throws IOException, AddressEmptyException {
         Geocoder geocoder = new Geocoder(this, Locale.getDefault());
         List<Address> addresses = Collections.emptyList();
 
         if (StringUtils.hasText(city)) {
             addresses = geocoder.getFromLocationName(city + " " + country, 1);
         }
 
 
         return extractAddress(addresses);
     }
 
 
     private void loadAvailableCountriesList() {
         new CountryLoader(this).execute(COUNTRIES_URL);
     }
 
     private Address lookupAddressFromLocation(Location location) throws IOException, AddressEmptyException {
         Geocoder geocoder = new Geocoder(this, Locale.getDefault());
         List<Address> addresses = Collections.emptyList();
 
         if (location != null) {
             addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
         }
 
 
         return extractAddress(addresses);
     }
 
 
     private Address extractAddress(List<Address> addresses) throws AddressEmptyException {
         if (addresses.isEmpty()) {
             throw new AddressEmptyException("Could not determine location address");
         }
 
         return addresses.get(0);
     }
 
 
     public void showInfoMessage(String text) {
         Toast.makeText(this, text, Toast.LENGTH_LONG).show();
     }
 
     public void addStationOverlay() {
         if (stationOverlay == null) {
             stationOverlay = new StationBalloonOverlay(this.getResources().getDrawable(R.drawable.pin_02), this, mapView);
             stationOverlay.setShowDisclosure(true);
             stationOverlay.setSnapToCenter(true);
         }
         if (!mapView.getOverlays().contains(stationOverlay)) {
             mapView.getOverlays().add(stationOverlay);
         }
     }
 
 
     private void initMyLocationOverlay() {
         myLocationOverlay = new MyLocationOverlay(this, mapView) {
             @Override
             public synchronized void onLocationChanged(Location location) {
                 currentLocation = location;
                 super.onLocationChanged(location);
             }
         };
 
         myLocationOverlay.enableMyLocation();
         mapView.getOverlays().add(myLocationOverlay);
     }
 
     private void startTrackingMyLocation(final int zoomLevel) {
 
         final ProgressDialog progressDialog = createProgressDialog("Trying to find your location...");
         progressDialog.show();
         myLocationOverlay.runOnFirstFix(new Runnable() {
             @Override
             public void run() {
                 currentLocation = myLocationOverlay.getLastFix();
                 runOnUiThread(new Runnable() {
                     public void run() {
                         progressDialog.dismiss();
                         if (currentLocation != null) {
 
                             try {
                                 currentLocationAddress = lookupAddressFromLocation(currentLocation);
                                 new StationLoader(StationActivity.this, currentLocationAddress, zoomLevel, CITY_URL_v2).execute();
                             } catch (AddressEmptyException e) {
                                 BugSenseHandler.log("Could not determine the user's location ", e);
                                 showInfoMessage("Could not determine your location. Refresh option might help");
                             } catch (IOException e) {
                                 //ignore
                             }
                         }
                     }
                 });
             }
         });
     }
 
     private void initMapView() {
         mapView = (MapView) findViewById(R.id.mapview);
         mapView.setBuiltInZoomControls(true);
         mapController = mapView.getController();
     }
 
     public ProgressDialog createProgressDialog(String message) {
         ProgressDialog progressDialog = new ProgressDialog(this);
         progressDialog.setMessage(message);
         progressDialog.setCancelable(true);
         return progressDialog;
     }
 
     public Spinner getCities() {
         return cities;
     }
 
     public void setCountries(List<String> countriesList) {
         if (!countriesList.isEmpty()) {
             ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                     simple_spinner_item, countriesList);
             dataAdapter.setDropDownViewResource(simple_spinner_dropdown_item);
             countries.setAdapter(dataAdapter);
 
             if (currentLocationAddress != null) {
                 String country = currentLocationAddress.getCountryName();
                 int countryIndex = countriesList.indexOf(country);
                 if (countryIndex > -1) {
                     countries.setSelection(countryIndex);
                 }
             }
         } else {
             showInfoMessage("Could not load country list");
         }
     }
 
     public void showStations(List<StationOverlayItem> overlayItems, GeoPoint geoPoint, int zoomLevel, String location) {
         stationOverlay.clear();
         stationOverlay.addOverlayItems(overlayItems);
         mapController.animateTo(geoPoint);
         mapController.setZoom(zoomLevel);
         int distance = Preferences.getDistance(this);
         if (overlayItems.size() > 1) {
             showInfoMessage("Showing " + overlayItems.size() + " results within " + distance + " km from " + location);
         } else {
             showInfoMessage("Showing " + overlayItems.size() + " result within " + distance + " km from " + location);
         }
     }
 }
