 /*
  * Copyright 2013 Sergio Garcia Villalonga (yayalose@gmail.com)
  *
  * This file is part of PalmaBici.
  *
  *    PalmaBici is free software: you can redistribute it and/or modify
  *    it under the terms of the Affero GNU General Public License version 3
  *    as published by the Free Software Foundation.
  *
  *    PalmaBici is distributed in the hope that it will be useful,
  *    but WITHOUT ANY WARRANTY; without even the implied warranty of
  *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *    Affero GNU General Public License for more details
  *    (https://www.gnu.org/licenses/agpl-3.0.html).
  *    
  */
 
 package com.poguico.palmabici.map;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import org.osmdroid.ResourceProxy;
 import org.osmdroid.bonuspack.overlays.ExtendedOverlayItem;
 import org.osmdroid.bonuspack.overlays.ItemizedOverlayWithBubble;
 import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
 import org.osmdroid.tileprovider.util.CloudmadeUtil;
 import org.osmdroid.util.GeoPoint;
 import org.osmdroid.views.MapView;
 import org.osmdroid.views.overlay.OverlayItem;
 import org.osmdroid.views.overlay.PathOverlay;
 import org.osmdroid.views.overlay.ScaleBarOverlay;
 import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
 import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;
 
 import android.annotation.TargetApi;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.content.res.Resources.NotFoundException;
 import android.location.Location;
 import android.os.Build;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.util.DisplayMetrics;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 
 import com.poguico.palmabici.R;
 import com.poguico.palmabici.SynchronizableElement;
 import com.poguico.palmabici.network.synchronizer.NetworkStationAlarm;
 import com.poguico.palmabici.network.synchronizer.NetworkSynchronizer;
 import com.poguico.palmabici.synchronizers.LocationSynchronizer;
 import com.poguico.palmabici.util.BikeLane;
 import com.poguico.palmabici.util.NetworkInformation;
 import com.poguico.palmabici.util.Station;
 import com.poguico.palmabici.widgets.StationInfoWidget;
 
 public class StationMapFragment extends Fragment implements
 		SynchronizableElement, OpenStreetMapConstants {
 
     // ===========================================================
     // Fields
     // ===========================================================
 
     private SharedPreferences mPrefs;
     private MapView mMapView;
     private MyLocationNewOverlay mLocationOverlay;
 	private ScaleBarOverlay mScaleBarOverlay;
     private ResourceProxy mResourceProxy;
 	
 	private HashMap <String, ExtendedOverlayItem> mapMarkers = null;
     
     private static final String BIKE_LANE_OPTION = "show_bike_lane";
     
     private ArrayList<PathOverlay> bikeLane = null;
     private SharedPreferences conf;
 	
 	private boolean bikeLaneState;
 	
 	private LocationSynchronizer locationSynchronizer;
 	private NetworkInformation network;
 	private ItemizedOverlayWithBubble<OverlayItem> markerOverlay;
 	private NetworkSynchronizer networkSync;
     private Context context;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		conf=PreferenceManager
 				.getDefaultSharedPreferences(this.getActivity());
 		bikeLaneState = conf.getBoolean(BIKE_LANE_OPTION, true);
 		locationSynchronizer = LocationSynchronizer.getInstance(this);
 		context = this.getActivity().getApplicationContext();
 		network = NetworkInformation.getInstance(context);
 		networkSync = NetworkSynchronizer.getInstance(context);
 		networkSync.addSynchronizableElement(this);
 		
 		mPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
 		
 		final SharedPreferences.Editor edit = mPrefs.edit();
         edit.remove(PREFS_SHOWN_MARKER);
         edit.remove(PREFS_SCROLL_X);
         edit.remove(PREFS_SCROLL_Y);
         edit.remove(PREFS_ZOOM_LEVEL);
         edit.commit();
 		
 		this.setRetainInstance(true);
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		mResourceProxy = new ResourceProxyImpl(inflater.getContext().getApplicationContext());
         mMapView = new MapView(inflater.getContext(), 256, mResourceProxy);
         mMapView.setUseSafeCanvas(true);
         setHardwareAccelerationOff();
         return mMapView;
 	}
 	
 	private void drawStationMarkers(boolean force) {
 		String filename;
 		int    percentage;
 		
 		if (mapMarkers == null || force) {
 			mapMarkers = new HashMap<String, ExtendedOverlayItem>();
 			for (Station station : network.getNetwork()) {
 				percentage = (int)Math.round((station.getBusySlots()*10 / station.getSlots()));
 				
 				ExtendedOverlayItem marker = new ExtendedOverlayItem(
 						station.getName(),
 						station.getNEstacio(),
 						new GeoPoint(station.getLat(), station.getLong()),
 						this.getActivity());
 				try {
 					filename  = "marker" + percentage*10;
 					filename +=  (station.getBrokenBikes() > 0 ||
 							      station.getBrokenSlots() > 0) ?
 							    	"_alert" : "";
 					filename +=  (NetworkStationAlarm.hasAlarm(station.getNEstacio())) ?
 						    	"_alarm" : "";
 					marker.setMarker(
 						getResources().getDrawable(
 							R.drawable.class.getDeclaredField(filename).getInt(null)));
 					mapMarkers.put(station.getNEstacio(),marker);
 				} catch (NotFoundException e) {
 					e.printStackTrace();
 				} catch (IllegalAccessException e) {
 					e.printStackTrace();
 				} catch (IllegalArgumentException e) {
 					e.printStackTrace();
 				} catch (NoSuchFieldException e) {
 					e.printStackTrace();
 				}
 			}
 			
 			markerOverlay = new ItemizedOverlayWithBubble<OverlayItem>(this.getActivity(), 
 					getResources().getDrawable(R.drawable.marker0),
 					new ArrayList<OverlayItem>(mapMarkers.values()), mMapView,
 					new StationInfoWidget(mMapView, this));
 			
 			mMapView.getOverlays().add(markerOverlay);
 		}
 	}
 	
 	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
     private void setHardwareAccelerationOff() {
         // Turn off hardware acceleration here, or in manifest
         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
             mMapView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
     }
 
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
 		super.onActivityCreated(savedInstanceState);
 		float[] distance = null;
 		final DisplayMetrics dm = context.getResources().getDisplayMetrics();
 
         // only do static initialisation if needed
         if (CloudmadeUtil.getCloudmadeKey().length() == 0) {
             CloudmadeUtil.retrieveCloudmadeKey(context.getApplicationContext());
         }
 
         this.mLocationOverlay = new MyLocationNewOverlay(context, new GpsMyLocationProvider(context),
                 mMapView);
 
 		mScaleBarOverlay = new ScaleBarOverlay(context);
 		mScaleBarOverlay.setCentred(true);
 		mScaleBarOverlay.setScaleBarOffset(dm.widthPixels / 2, 10);
 
         mMapView.setBuiltInZoomControls(true);
         mMapView.setMultiTouchControls(true);
 		mMapView.getOverlays().add(this.mScaleBarOverlay);
 
         Location myLocation =
 				locationSynchronizer.getLocation();
 		
 		if (myLocation != null) {
 			distance = new float[1];
 			Location.distanceBetween(network.getCenter().getLatitude(),
 					                 network.getCenter().getLongitude(),
 									 myLocation.getLatitude(),
                                      myLocation.getLongitude(), distance);
 		}
 		
 		drawBikeLane();
 		toggleBikeLane(bikeLaneState);
 		mMapView.getController().setZoom(mPrefs.getInt(PREFS_ZOOM_LEVEL, (int)(14+dm.density)));
 		if (distance == null || distance[0] > 10000) {
 			mMapView.getController().setCenter(new GeoPoint(39574689, 2651332));
 		} else {
 			mMapView.getController().setCenter(new GeoPoint(myLocation));
 		}
         setHasOptionsMenu(true);
         mMapView.getOverlays().add(this.mLocationOverlay);
         
         mMapView.scrollTo(mPrefs.getInt(PREFS_SCROLL_X, mMapView.getScrollX()),
         				  mPrefs.getInt(PREFS_SCROLL_Y, mMapView.getScrollY()));
     }
 	
 	@Override
 	public void onStart() {
 		super.onStart();
 		updateStations();
 	}
 	
 	@Override
     public void onPause() {
         final SharedPreferences.Editor edit = mPrefs.edit();
         edit.putInt(PREFS_SCROLL_X, mMapView.getScrollX());
         edit.putInt(PREFS_SCROLL_Y, mMapView.getScrollY());
         edit.putInt(PREFS_ZOOM_LEVEL, mMapView.getZoomLevel());
         edit.putInt(PREFS_SHOWN_MARKER, markerOverlay.getBubbledItemId());
         edit.putString(PREFS_SHOWN_STATION, null);
         edit.commit();
         markerOverlay.hideBubble();
         
         super.onPause();
     }
 	
 	@Override
     public void onResume() {
 		int shownBubble;
 		String activeStation;
         super.onResume();
         
         shownBubble   = mPrefs.getInt(PREFS_SHOWN_MARKER, -1);
         activeStation = mPrefs.getString(PREFS_SHOWN_STATION, null);
         if (activeStation != null) {
         	markerOverlay.showBubbleOnItem(mapMarkers.get(activeStation), mMapView, true);
         } else if (shownBubble >= 0) {
         	markerOverlay.showBubbleOnItem(shownBubble, mMapView, true);	
         }
         
         try {
             mMapView.setTileSource(TileSourceFactory.MAPQUESTOSM);
         } catch (final IllegalArgumentException ignore) {
         }
         
         
         boolean showBikeLane = conf.getBoolean(BIKE_LANE_OPTION, true);
 		if (showBikeLane != bikeLaneState) {
 			bikeLaneState = showBikeLane;
 			toggleBikeLane(bikeLaneState);
 		}
         drawStationMarkers(false);
         this.mLocationOverlay.enableMyLocation();
     }
 
 	@Override
 	public void onDestroy() {
 		locationSynchronizer.detachSynchronizableElement(this);
 		networkSync.detachSynchronizableElement(this);
 		super.onDestroy();
 	}
 	
 	private void updateStations() {		
 		if (mapMarkers != null) {
 			mMapView.getOverlays().remove(markerOverlay);
 		}
 		drawStationMarkers(true);
 	}
 	
 	@Override
 	public void onSuccessfulNetworkSynchronization() {
 		int bubbleId = markerOverlay.getBubbledItemId();
 		markerOverlay.hideBubble();		
 		updateStations();
 		
 		if (bubbleId >= 0) {
 			markerOverlay.showBubbleOnItem(bubbleId, mMapView, true);	
 		}
 	}
 
 	@Override
 	public void onUnsuccessfulNetworkSynchronization() {
 		updateStations();
 	}
 
 	@Override
 	public void onLocationSynchronization() {
 		float[] distance;
 		Location myLocation;
 		if (markerOverlay.getBubbledItemId() < 0) {
 			myLocation =
 				locationSynchronizer.getLocation();
 			
			if (myLocation != null) {
				distance = new float[1];
				Location.distanceBetween(network.getCenter().getLatitude(),
						                 network.getCenter().getLongitude(),
										 myLocation.getLatitude(),
	                                     myLocation.getLongitude(), distance);
				
				if (distance != null && distance[0] <= 10000) {
					mMapView.getController().animateTo(new GeoPoint(myLocation));
				}
 			}
 		}
 	}
 
 	@Override
 	public FragmentActivity getSynchronizableActivity() {
 		return this.getActivity();
 	}
 
 
 	private void drawBikeLane () {
 		bikeLane = BikeLane.getOSMPaths(this.getActivity(), mMapView);
 		for (PathOverlay pathOverlay : bikeLane) {
 			mMapView.getOverlays().add(pathOverlay);
 		}
 	}
 	
 	private void toggleBikeLane(boolean visible) {
 		for(PathOverlay pathOverlay : bikeLane) {
 			pathOverlay.setEnabled(visible);
 		}
 	}
 }
