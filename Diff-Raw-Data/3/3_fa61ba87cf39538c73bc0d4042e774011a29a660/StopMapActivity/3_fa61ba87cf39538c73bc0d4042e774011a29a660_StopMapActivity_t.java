 /*
  * Copyright 2010, 2011 Colin Paton - cozzarp@googlemail.com
  * This file is part of rEdBus.
  *
  *  rEdBus is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  rEdBus is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with rEdBus.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.redbus.ui.stopmap;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.redbus.R;
 import org.redbus.geocode.GeocodingHelper;
 import org.redbus.geocode.IGeocodingResponseListener;
 import org.redbus.settings.SettingsHelper;
 import org.redbus.stopdb.ServiceBitmap;
 import org.redbus.stopdb.StopDbHelper;
 import org.redbus.ui.BusyDialog;
 import org.redbus.ui.Common;
 import org.redbus.ui.arrivaltime.ArrivalTimeActivity;
 
 
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.DialogInterface.OnCancelListener;
 import android.location.Address;
 import android.location.Location;
 import android.net.Uri;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.widget.EditText;
 import android.widget.Toast;
 
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapActivity;
 import com.google.android.maps.MapController;
 import com.google.android.maps.MapView;
 import com.google.android.maps.MyLocationOverlay;
 
 public class StopMapActivity extends MapActivity implements IGeocodingResponseListener, OnCancelListener  {
 
 	private MapView mapView;
 	private MapController mapController;
 	private MyLocationOverlay myLocationOverlay;
 	private StopMapOverlay stopOverlay;
 	private ServiceBitmap serviceFilter = new ServiceBitmap();
 
 	private BusyDialog busyDialog = null;
 	private int expectedRequestId = -1;
 	
 	private final int StopTapRadiusMetres = 50;
 
 	
 	
 	
 	
 	public static void showActivity(Context context) {
 		Intent i = new Intent(context, StopMapActivity.class);
 		context.startActivity(i);
 	}
 	
 	public static void showActivity(Context context, 
 			int lat,
 			int lng) {
 		Intent i = new Intent(context, StopMapActivity.class);
 		i.putExtra("Lat", lat);
 		i.putExtra("Lng", lng);
 		context.startActivity(i);
 	}
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) { 
 		super.onCreate(savedInstanceState); 
 		setContentView(R.layout.stop_map);
 		busyDialog = new BusyDialog(this);
 
 		mapView = (MapView) findViewById(R.id.mapview);
 		mapView.setBuiltInZoomControls(true);
 
 		mapController = mapView.getController();
 		mapController.setZoom(17);
 
 		// Make map update automatically as user moves around
 		myLocationOverlay = new ReallyMyLocationOverlay(this, mapView);
 		mapView.getOverlays().add(myLocationOverlay);
 
 		// Check to see if we've been passed data
 		int lat = getIntent().getIntExtra("Lat", -1);
 		int lng = getIntent().getIntExtra("Lng", -1);
 		
 		// Not been passed a location, so use GPS and default to centre
 		if (lat == -1 && lng == -1) {
 			// Default map to centre of Edinburgh
 			lat = 55946052;
 			lng = -3188879;
 			
 			updateMyLocationStatus(true);
 		} else {
 			updateMyLocationStatus(false);
 		}
 
 		stopOverlay = new StopMapOverlay(this);
 		mapView.getOverlays().add(stopOverlay);
 		mapController.setCenter(new GeoPoint(lat, lng));
 	}
 	
 	public void invalidate()
 	{
 		this.stopOverlay.invalidate();
 		this.mapView.invalidate();
 	}
 	
 	public ServiceBitmap getServiceFilter() {
 		return serviceFilter;
 	}
 
 	@Override
 	protected boolean isRouteDisplayed() {
 		return false;
 	}
 
 	@Override
 	public void onPause() {
 		updateMyLocationStatus(false);
 		super.onPause();
 	}
 
 	@Override
 	public void onResume() {
 		updateMyLocationStatus(true);
 		super.onResume();
 	}	
 	
 	@Override
 	protected void onDestroy() {
 		if (busyDialog != null)
 			busyDialog.dismiss();
 		busyDialog = null;
 		super.onDestroy();
 	}
 
 	public void onCancel(DialogInterface dialog) {
 		expectedRequestId = -1;
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.stopmap_menu, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onPrepareOptionsMenu(Menu menu) {
 		if (mapView.isSatellite())
 			menu.findItem(R.id.stopmap_menu_satellite_or_map).setTitle("Map View");
 		else
 			menu.findItem(R.id.stopmap_menu_satellite_or_map).setTitle("Satellite View");		
 		
 		if (serviceFilter.areAllSet)
 			menu.findItem(R.id.stopmap_menu_showall).setEnabled(false);
 		else
 			menu.findItem(R.id.stopmap_menu_showall).setEnabled(true);
 		
 		return super.onPrepareOptionsMenu(menu);
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.stopmap_menu_search:
 			doSearchForLocation();
 			return true;
 
 		case R.id.stopmap_menu_showall:
 			doShowAllServices();
 			return true;
 
 		case R.id.stopmap_menu_filterservices:
 			doFilterServices();
 			return true;
 
 		case R.id.stopmap_menu_satellite_or_map:
 			doSetMapType();
 			return true;
 			
 		case R.id.stopmap_menu_mylocation:
 			doSetMyLocation();
 			return true;
 		}
 		
 		return false;
 	}
 
 	public boolean onStopMapTap(GeoPoint point, MapView mapView)
 	{
 		StopDbHelper pt = StopDbHelper.Load(this);
 		final int nearestStopNodeIdx = pt.findNearest(point.getLatitudeE6(), point.getLongitudeE6());
 		final int stopCode = pt.lookupStopCodeByStopNodeIdx(nearestStopNodeIdx);
 		final double stopLat = pt.lat[nearestStopNodeIdx] / 1E6;
 		final double stopLon = pt.lon[nearestStopNodeIdx] / 1E6;
 
 		// Yuk - there must be a better way to convert GeoPoint->Point than this?			
 		Location touchLoc = new Location("");
 		touchLoc.setLatitude(point.getLatitudeE6() / 1E6);
 		touchLoc.setLongitude(point.getLongitudeE6() / 1E6);
 
 		Location stopLoc = new Location("");
 		stopLoc.setLatitude(stopLat);
 		stopLoc.setLongitude(stopLon);
 
 		if (touchLoc.distanceTo(stopLoc) >= StopTapRadiusMetres)
 			return false;
 		
 		new StopMapPopup(this, stopCode);
 		return true;
 	}
 	
 	public boolean onStopMapTouchEvent(MotionEvent e, MapView mapView) {
 		// disable my location if user drags the map
 		if (e.getAction() == MotionEvent.ACTION_MOVE)
 			updateMyLocationStatus(false);			
 		return false;
 	}
 
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	private void doSearchForLocation() {
 		final EditText input = new EditText(this);
 		new AlertDialog.Builder(this)
 			.setTitle("Enter a location or postcode")
 			.setView(input)
 			.setPositiveButton(android.R.string.ok,
 					new DialogInterface.OnClickListener() {
 						public void onClick(DialogInterface dialog, int whichButton) {
 							busyDialog.show(StopMapActivity.this, "Finding location...");
 							StopMapActivity.this.expectedRequestId = GeocodingHelper.geocode(StopMapActivity.this, input.getText().toString(), StopMapActivity.this);
 						}
 					})
 			.setNegativeButton(android.R.string.cancel, null)
 			.show();
 	}
 	
 	private void doShowAllServices() {
 		serviceFilter.setAll();
 		StopMapActivity.this.invalidate();
 	}
 	
 	private void doFilterServices() {
 		final EditText input = new EditText(this);
 
 		new AlertDialog.Builder(this)
 			.setTitle("Enter services separated by spaces")
 			.setView(input)
 			.setPositiveButton(android.R.string.ok, 
 				new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int which) {
 						ServiceBitmap filter = new ServiceBitmap().clearAll();
 						StopDbHelper pt = StopDbHelper.Load(StopMapActivity.this);
 						for(String serviceStr: input.getText().toString().split("[ ]+")) {
 							if (pt.serviceNameToServiceBit.containsKey(serviceStr.toUpperCase()))
 								filter.setBit(pt.serviceNameToServiceBit.get(serviceStr.toUpperCase()));
 						}
 						
 						updateServiceFilter(filter);
 					}
 				})
 			.setNegativeButton(android.R.string.cancel, null)
 			.show();
 	}
 	
 	public void doFilterServices(int stopCode) {
 		StopDbHelper pt = StopDbHelper.Load(this);		
 		int nodeIdx = pt.lookupStopNodeIdxByStopCode(stopCode);
 		if (nodeIdx == -1)
 			return;
 		
 		updateServiceFilter(pt.lookupServiceBitmapByStopNodeIdx(nodeIdx));
 	}
 	
 	private void doSetMapType() {
 		mapView.setSatellite(!mapView.isSatellite());
 	}
 	
 	private void doSetMyLocation() {
 		updateMyLocationStatus(true);
 	}
 	
 	public void doStreetView(int stopCode) {
 		StopDbHelper pt = StopDbHelper.Load(this);		
 		int nodeIdx = pt.lookupStopNodeIdxByStopCode(stopCode);
 		if (nodeIdx == -1)
 			return;
 		double stopLat = pt.lat[nodeIdx] / 1E6;
 		double stopLon = pt.lon[nodeIdx] / 1E6;
 
         startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("google.streetview:cbll=" + stopLat + "," + stopLon + "&cbp=1,180,,0,2.0")));		
 	}
 	
 	public void doShowArrivalTimes(int stopCode) {
 		ArrivalTimeActivity.showActivity(StopMapActivity.this, stopCode);
 	}
 	
 	public void doAddBookmark(int stopCode) {
 		StopDbHelper pt = StopDbHelper.Load(this);		
 		int nodeIdx = pt.lookupStopNodeIdxByStopCode(stopCode);
 		if (nodeIdx == -1)
 			return;
 		String stopName = pt.lookupStopNameByStopNodeIdx(nodeIdx);
 
 		Common.doAddBookmark(this, stopCode, stopName);
 	}
 	
 	private void updateServiceFilter(ServiceBitmap filter) {
 		serviceFilter.setTo(filter);
 		mapController.setZoom(12);
 		StopMapActivity.this.invalidate();		
 	}
 	
 	private void updateMyLocationStatus(boolean status) {
 		if (status) {
 			myLocationOverlay.enableMyLocation();
			Toast.makeText(this, "Finding your location...", Toast.LENGTH_SHORT).show();
 		} else {
 			myLocationOverlay.disableMyLocation();
 		}
 	}	
 
 	
 	
 	
 	public void onAsyncGeocodeResponseError(int requestId, String message) {
 		if (requestId != expectedRequestId)
 			return;
 		
 		if (busyDialog != null)
 			busyDialog.dismiss();
 		
 		new AlertDialog.Builder(this).setTitle("Error").
 			setMessage("Unable to find location: " + message).
 			setPositiveButton(android.R.string.ok, null).
 			show();
 	}
 
 	public void onAsyncGeocodeResponseSucccess(int requestId, List<Address> addresses_) {
 		if (requestId != expectedRequestId)
 			return;
 		
 		if (busyDialog != null)
 			busyDialog.dismiss();
 		
 		if (addresses_.size() == 1) {
 			Address address = addresses_.get(0);
 			GeoPoint gp = new GeoPoint((int) (address.getLatitude() * 1E6), (int) (address.getLongitude() * 1E6));
 			mapController.animateTo(gp);
 			return;
 		}
 		
 		final List<Address> addresses = addresses_;
 		ArrayList<String> addressNames = new ArrayList<String>();
 		for(Address a: addresses) {
 			StringBuilder strb = new StringBuilder();
 			for(int i =0; i< a.getMaxAddressLineIndex(); i++) {
 				if (i > 0)
 					strb.append(", ");
 				strb.append(a.getAddressLine(i));
 			}
 			addressNames.add(strb.toString());
 		}
 
 		new AlertDialog.Builder(this)
   	       .setSingleChoiceItems(addressNames.toArray(new String[addressNames.size()]), -1, new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog, int which) {
 					if (which < 0)
 						return;
 					
 					Address address = addresses.get(which);
 					GeoPoint gp = new GeoPoint((int) (address.getLatitude() * 1E6), (int) (address.getLongitude() * 1E6));
 					mapController.animateTo(gp);
 					dialog.dismiss();
 				}
   	       })
   	       .show();
 	}
 }
