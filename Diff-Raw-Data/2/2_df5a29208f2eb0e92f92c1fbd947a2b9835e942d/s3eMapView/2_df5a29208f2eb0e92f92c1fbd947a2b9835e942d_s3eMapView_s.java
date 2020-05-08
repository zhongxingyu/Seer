 /*
 Part of the java implementation of the s3eMapView extension.
 This class is used via JNI from the native code in s3eMapView_platform.cpp
 
This file contains the "s3eMapView" class, which is responsbile for:
 1. Checking that the Google Maps service is available on this device
 2. Instantiating an "s3eMapActivity" that will hold the actual map
 3. Using LocalActivityManager (as m_mapActivityHost) to host the s3eMapActivity
    within the Marmalade app's Activity 
    (Note: LocalActivityManager is deprecated but is the only way to get this
    to work with older android OS versions in Marmalade since the Marmalade
    LoaderActivity is not a FragmentAcivity. The new 
    "Fragment"/"FragmentManager"/"MapFragment" API trio requires Android API
    level 12 or higher.)
 4. Passing messages/commands between the Marmalade app and s3eMapActivity
 
 Code is copyright (c) 2013 Get to Know Society.
 Licensed under the zlib license - see LICENSE file.
 
 */
 
 import com.ideaworks3d.marmalade.LoaderAPI;
 import com.ideaworks3d.marmalade.LoaderActivity;
 import android.app.AlertDialog;
 import android.app.LocalActivityManager;
 import android.content.Context;
 import android.content.Intent;
 import android.util.Log;
 import android.view.View;
 import android.widget.Button;
 import android.widget.FrameLayout;
 import com.google.android.gms.common.ConnectionResult;
 import com.google.android.gms.common.GooglePlayServicesUtil;
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.CameraUpdate;
 import com.google.android.gms.maps.CameraUpdateFactory;
 import com.google.android.gms.maps.model.LatLng;
 import com.google.android.gms.maps.model.LatLngBounds;
 import com.google.android.gms.maps.model.Marker;
 import com.google.android.gms.maps.model.MarkerOptions;
 import java.util.HashMap;
 
 import com.bradenmacdonald.s3eMapActivity;
 
 class s3eMapView implements GoogleMap.OnInfoWindowClickListener {
 	public s3eMapView() {
 		// Check if the Google Services APK is available - required for using Maps
 		int gsApkStatus = GooglePlayServicesUtil.isGooglePlayServicesAvailable((Context)LoaderActivity.m_Activity);
 		if (gsApkStatus != ConnectionResult.SUCCESS) {
 			// We'd love to show the nice Google Play error dialog as coded below,
 			// but it requires resources that are hard to integrate into a Marmalade
 			// project. (Marmalade doesn't let us specify a '--extra-packges' flag for aapt)
 			/*if (GooglePlayServicesUtil.isUserRecoverableError(gsApkStatus)) {
 				// User needs to update the Google Play APK on their device.
 				// Show Google's default dialog for doing so:
 				GooglePlayServicesUtil.getErrorDialog(gsApkStatus, LoaderActivity.m_Activity, 999).show();
 			} else {*/
 			AlertDialog.Builder builder = new AlertDialog.Builder((Context)LoaderActivity.m_Activity);
 			builder.setMessage("Unable to create a Google Map. Please make sure the Google Play Store app is installed on your device and up to date.");
 			builder.setCancelable(true);
 			builder.setPositiveButton("OK", null);
 			builder.create().show();
 			throw new RuntimeException("Google Play Services not available on this device.");
 		}
 		
 		m_mapLayout = new FrameLayout(LoaderActivity.m_Activity);
 		m_mapLayout.setId(MAP_LAYOUT_ID);
 		m_mapLayout.setPadding(50, 50, 50, 50);
 
 		m_mapActivityHost = new LocalActivityManager(LoaderActivity.m_Activity, false);
 		m_mapActivityHost.dispatchCreate(null);
 		Intent intent = new Intent((Context)LoaderActivity.m_Activity, s3eMapActivity.class);
 		android.view.Window mapWindow = m_mapActivityHost.startActivity(MAP_ACTIVITY_ID, intent);
 		m_mapLayout.addView(mapWindow.getDecorView());
 		LoaderActivity.m_Activity.m_FrameLayout.addView(m_mapLayout);
 		m_mapActivity = (s3eMapActivity)m_mapActivityHost.getActivity(MAP_ACTIVITY_ID);
 		m_mapActivityHost.dispatchResume();
 		// At this point, if no exception has been thrown, we can 
 		// access and manipulate the GoogleMap object
 		// m_mapActivity.getMap()
 		m_mapActivity.getMap().setOnInfoWindowClickListener(this);
 		markerInfoMap = new HashMap<Marker, MarkerInfo>();
 	}
 	public void Destroy() {
 		LoaderActivity.m_Activity.m_FrameLayout.removeView(m_mapLayout);
 		m_mapActivityHost.destroyActivity(MAP_ACTIVITY_ID, true);
 	}
 	public void SetScreenRect(int x, int y, int w, int h) {
 		m_mapLayout.setPadding(x, y, 0, 0);
 		m_mapLayout.setLayoutParams(new FrameLayout.LayoutParams(w+x,h+y, android.view.Gravity.FILL)); // (width,height,gravity); n.b. layout size includes the padding
 	}
 	public void SetVisible(boolean visible) {
 		m_mapActivity.setVisibility(visible);
 	}
 	public void SetType(int typeCode) {
 		int type = 
 			typeCode == 0 ? GoogleMap.MAP_TYPE_NORMAL : // S3E_MAPVIEW_TYPE_STANDARD - see .s4e file
 			typeCode == 1 ? GoogleMap.MAP_TYPE_SATELLITE : // S3E_MAPVIEW_TYPE_SATELLITE
 			/* else: */ GoogleMap.MAP_TYPE_HYBRID; // S3E_MAPVIEW_TYPE_HYBRID
 		m_mapActivity.getMap().setMapType(type);
 	}
 	public void SetShowUserLocation(boolean show) {
 		m_mapActivity.getMap().setMyLocationEnabled(show);
 	}
 	public void GoTo(double lat, double lng, double latDelta, double lngDelta, boolean animate) {
 		LatLng southwest = new LatLng(lat - latDelta, lng - lngDelta);
 		LatLng northeast = new LatLng(lat + latDelta, lng + lngDelta);
 		LatLngBounds bounds = new LatLngBounds(southwest, northeast);
 		CameraUpdate dest = CameraUpdateFactory.newLatLngBounds(bounds, 0);
 		m_mapActivity.GoTo(dest, animate);
 	}
 	public int AddPOI(double lat, double lng, String title, String subtitle, boolean clickable, int customData) {
 		Marker m = m_mapActivity.getMap().addMarker(
 			new MarkerOptions()
 			.position(new LatLng(lat, lng))
 			.title(clickable ? title + " \u2192" : title) // show an arrow as part of the title if it's clickable
 			.snippet(subtitle)
 		);
 		MarkerInfo info = new MarkerInfo(markerIdGenerator++, clickable, customData);
 		markerInfoMap.put(m, info);
 		return info.id;
 	}
 	public void RemovePOI(int markerId) {
 		for (java.util.Map.Entry<Marker, MarkerInfo> entry : markerInfoMap.entrySet()) {
 			if ((entry.getValue()).id == markerId) {
 				Marker m = entry.getKey();
 				m.remove();
 				break;
 			}
 		}
 	}
 	public void onInfoWindowClick (Marker marker) { // Called when user clicks on a POI's callout
 		MarkerInfo info = markerInfoMap.get(marker);
 		if (info.clickable)
 			notifyAppOfPOIClick(info.customData);
 	}
 	public static String GetLicensingString() {
 		return GooglePlayServicesUtil.getOpenSourceSoftwareLicenseInfo((Context)LoaderActivity.m_Activity);
 	}
 	public native void notifyAppOfPOIClick(int n); // implemented in s3eMapView_platform.cpp
 	private s3eMapActivity m_mapActivity;
 	private FrameLayout m_mapLayout;
 	private LocalActivityManager m_mapActivityHost;
 	private class MarkerInfo {
 		// Custom class to hold meta-data associated with Markers
 		int id;
 		boolean clickable;
 		int customData;
 		MarkerInfo(int _id, boolean _clickable, int _customData) { id=_id; clickable=_clickable; customData=_customData; }
 	}
 	private static int markerIdGenerator=500; // Used to generate unique integer IDs for each marker
 	private HashMap<Marker, MarkerInfo> markerInfoMap;
 	private static final int MAP_LAYOUT_ID = 10101010;
 	private static final String MAP_ACTIVITY_ID = "s3eMapActivity";
 	private static final String TAG = "s3eMapView";
 }
