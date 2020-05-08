 /**
  * *******************************************************************************************************************
  * <p/>
  * Copyright (C) 7/30/12 by Manuel Palacio
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
 package net.palacesoft.cngstation.client.loader;
 
 import android.app.ProgressDialog;
 import android.location.Address;
 import android.os.AsyncTask;
 import android.util.Log;
 import com.google.android.maps.GeoPoint;
 import net.palacesoft.cngstation.client.AddressEmptyException;
 import net.palacesoft.cngstation.client.Preferences;
 import net.palacesoft.cngstation.client.StationActivity;
 import net.palacesoft.cngstation.client.StationDTO;
 import net.palacesoft.cngstation.client.mapoverlay.StationOverlayItem;
import org.springframework.web.client.HttpServerErrorException;
 import org.springframework.web.client.RestClientException;
 import org.springframework.web.client.RestTemplate;
 
 import java.util.ArrayList;
 import java.util.List;
 
 
 public class StationLoader extends AsyncTask<Void, Void, List<StationOverlayItem>> {
     private ProgressDialog progressDialog;
     private Address address;
     private StationActivity stationActivity;
     private int zoomLevel;
     private RestTemplate restTemplate = new RestTemplate();
     private String url;
    private String errorMessage;
 
 
     public StationLoader(StationActivity stationActivity, Address address, int zoomLevel, String url) throws AddressEmptyException {
         this.stationActivity = stationActivity;
         this.zoomLevel = zoomLevel;
         this.url = url;
         if (address == null) {
             throw new AddressEmptyException("Cannot load stations without a location");
         }
         this.address = address;
     }
 
 
     @Override
     protected void onPreExecute() {
         stationActivity.addStationOverlay();
         progressDialog = stationActivity.createProgressDialog("Loading CNG map...");
         progressDialog.show();
     }
 
 
     @Override
     protected List<StationOverlayItem> doInBackground(Void... urls) {
         String queryURL = url + address.getLocality() + "?latitude=" + address.getLatitude() + "&longitude="
                                     + address.getLongitude() + "&distance=" + Preferences.getDistance(stationActivity);
         return fetchStations(queryURL);
     }
 
     private List<StationOverlayItem> fetchStations(String queryURL) {
         List<StationOverlayItem> stationOverlayItems = new ArrayList<StationOverlayItem>();
         StationDTO[] stations = new StationDTO[0];
         try {
             stations = restTemplate.getForObject(queryURL, StationDTO[].class);
         } catch (RestClientException e) {
            if(e instanceof HttpServerErrorException){
                errorMessage = "Service is currently down";
            }
         }
         for (StationDTO stationDTO : stations) {
             StationOverlayItem overlayItem = createOverlayItem(stationDTO);
             stationOverlayItems.add(overlayItem);
         }
         return stationOverlayItems;
     }
 
     private StationOverlayItem createOverlayItem(StationDTO stationDTO) {
         GeoPoint geoPoint = new GeoPoint((int) (stationDTO.getLatitude() * 1E6), (int) (stationDTO.getLongitude() * 1E6));
         String stationText = stationDTO.getStreet();
         return new StationOverlayItem(geoPoint, stationText, "Price: " + stationDTO.getPrice(), stationDTO);
     }
 
     @Override
     protected void onPostExecute(List<StationOverlayItem> overlayItems) {
         progressDialog.dismiss();
 
         if (!overlayItems.isEmpty()) {
             GeoPoint geoPointToZoomTo;
             geoPointToZoomTo = new GeoPoint((int) (address.getLatitude() * 1E6), (int) (address.getLongitude() * 1E6));
             stationActivity.showStations(overlayItems, geoPointToZoomTo, zoomLevel, address.getLocality());
         } else {
            if (errorMessage == null) {
                stationActivity.showInfoMessage("Could not find CNG stations for location: " + address.getLocality());
            } else {
                stationActivity.showInfoMessage(errorMessage);
            }
         }
     }
 }
