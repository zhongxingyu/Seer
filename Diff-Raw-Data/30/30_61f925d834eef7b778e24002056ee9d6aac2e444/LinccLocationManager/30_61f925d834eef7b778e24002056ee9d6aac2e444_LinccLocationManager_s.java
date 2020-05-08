 /**
  * Copyright (C) 2010, Hoccer GmbH Berlin, Germany <www.hoccer.com>
  *
  * These coded instructions, statements, and computer programs contain
 * proprietary information of Linccer GmbH Berlin, and are copy protected
  * by law. They may be used, modified and redistributed under the terms
  * of GNU General Public License referenced below. 
  *    
  * Alternative licensing without the obligations of the GPL is
  * available upon request.
  * 
  * GPL v3 Licensing:
  * 
  * This file is part of the "Linccer Android-API".
  * 
  * Linccer Android-API is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * Linccer Android-API is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with Linccer Android-API. If not, see <http://www.gnu.org/licenses/>.
  */
 package com.hoccer.api.android;
 
 import java.io.IOException;
 import java.util.List;
 
 import android.content.Context;
 import android.location.Address;
 import android.location.Geocoder;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.net.wifi.WifiManager;
 import android.os.Bundle;
 
 import com.hoccer.api.UpdateException;
 
 public class LinccLocationManager implements LocationListener {
 
     private static final String   UNKNOWN_LOCATION_TEXT = "You can not hoc without a location";
 
     private final LocationManager mLocationManager;
     private final WifiManager     mWifiManager;
 
     private final Context         mContext;
 
     private final AsyncLinccer    mLinccer;
 
     public LinccLocationManager(Context pContext, AsyncLinccer linccer) {
         mContext = pContext;
 
         mLinccer = linccer;
 
         mLocationManager = (LocationManager) pContext.getSystemService(Context.LOCATION_SERVICE);
         mWifiManager = (WifiManager) pContext.getSystemService(Context.WIFI_SERVICE);
     }
 
     public Context getContext() {
         return mContext;
     }
 
     public void refreshLocation() throws UpdateException {
         mLinccer.onWifiScanResults(mWifiManager.getScanResults());
         mLinccer.onNetworkChanged(mLocationManager
                 .getLastKnownLocation(LocationManager.NETWORK_PROVIDER));
         mLinccer.onGpsChanged(mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER));
     }
 
     public void deactivate() {
         mLocationManager.removeUpdates(this);
     }
 
     public void activate() {
         mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, this);
         mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 1, this);
     }
 
     @Override
     public void onLocationChanged(Location location) {
         // Logger.v(LOG_TAG, location);
     }
 
     @Override
     public void onProviderDisabled(String provider) {
     }
 
     @Override
     public void onProviderEnabled(String provider) {
     }
 
     @Override
     public void onStatusChanged(String provider, int status, Bundle extras) {
     }
 
     public Address getAddress(Location location) throws IOException {
         if (location == null) {
             return new Address(null);
         }
 
         Geocoder gc = new Geocoder(mContext);
 
         Address address = null;
         List<Address> addresses = gc.getFromLocation(location.getLatitude(), location
                 .getLongitude(), 1);
         if (addresses.size() > 0) {
             address = addresses.get(0);
         }
         return address;
     }
 
     public String getDisplayableAddress(Location location) {
 
         try {
             Address address = getAddress(location);
 
             String addressLine = null;
             String info = " (~" + location.getAccuracy() + "m)";
             if (location.getAccuracy() < 500) {
                 addressLine = address.getAddressLine(0);
             } else {
                 addressLine = address.getAddressLine(1);
             }
 
             addressLine = trimAddress(addressLine);
 
             return addressLine + info;
 
         } catch (Exception e) {
             return UNKNOWN_LOCATION_TEXT + " ~" + location.getAccuracy() + "m";
         }
     }
 
     private String trimAddress(String pAddressLine) {
         if (pAddressLine.length() < 27)
             return pAddressLine;
 
         String newAddress = pAddressLine.substring(0, 18) + "..."
                 + pAddressLine.substring(pAddressLine.length() - 5);
 
         return newAddress;
     }
 }
