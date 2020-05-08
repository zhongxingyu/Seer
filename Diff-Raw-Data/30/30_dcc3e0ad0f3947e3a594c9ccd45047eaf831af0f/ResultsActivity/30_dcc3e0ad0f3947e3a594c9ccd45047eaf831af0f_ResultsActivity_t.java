 package org.routy;
 
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.routy.fragment.OneButtonDialog;
 import org.routy.mapview.MapRoute;
 import org.routy.mapview.MapRoute.RouteListener;
 import org.routy.mapview.MapRouteOverlay;
 import org.routy.mapview.RoutyItemizedOverlay;
 import org.routy.model.Route;
import org.routy.model.RouteOptimizePreference;
 import org.routy.view.ResultsSegmentView;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.graphics.Color;
 import android.graphics.drawable.Drawable;
 import android.location.Address;
 import android.media.AudioManager;
 import android.media.SoundPool;
 import android.net.Uri;
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
 import android.util.Log;
 import android.view.Menu;
import android.view.View;
 import android.view.ViewGroup;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapActivity;
 import com.google.android.maps.MapController;
 import com.google.android.maps.MapView;
 import com.google.android.maps.Overlay;
 import com.google.android.maps.OverlayItem;
 
 //public class ResultsActivity extends FragmentActivity {
 public class ResultsActivity extends MapActivity {
 	
 	private static final int INSTRUCTIONS_DIALOG = 1;
 	Context mContext;
 	
 	// MapView stuff
 	private MapView					mapView;
 	private MapController			mapController;
 	private ArrayList<GeoPoint> 	geoPoints;
 	private List<Overlay> 			mapOverlays;
 	
 	private SharedPreferences resultsActivityPrefs;
 	
 	// The Route sent by DestinationActivity
 	Route route;
 
 	private LinearLayout resultsLayout;
 
 	private final String TAG = "ResultsActivity";
 
 	private SoundPool sounds;
 	private int click;
	private RouteOptimizePreference routeOptimizePreference;
 	AudioManager audioManager;
 	float volume;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 //		context  = this;
 		mContext = this;
 
 		audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
 		volume = (float) audioManager
 				.getStreamVolume(AudioManager.STREAM_SYSTEM);
 
 		sounds = new SoundPool(3, AudioManager.STREAM_MUSIC, 0);
 		click = sounds.load(this, R.raw.routyclick, 1);
 		setContentView(R.layout.activity_results);
 
 		// Get the layout containing the list of destination
 		resultsLayout = (LinearLayout) findViewById(R.id.linearlayout_results);
 
 		Bundle extras = getIntent().getExtras();
 		if (extras != null) {
 			int distance = (Integer) extras.get("distance");
 			ArrayList<Address> addresses = (ArrayList<Address>) extras.get("addresses");
 			Log.v(TAG, "Results: " + addresses.size() + " addresses");
 			route = new Route(addresses, distance);
			routeOptimizePreference = (RouteOptimizePreference) extras.get("optimize_for");
 		}
 		
 		initMapView();
 		buildResultsView();
 		
 		resultsActivityPrefs = getSharedPreferences("results_prefs", MODE_PRIVATE);
 		// TODO: for testing purposes. Remove before prod.
 		showNoobDialog();
 		// First-time user dialog cookie
 		boolean noobCookie = resultsActivityPrefs.getBoolean("noob_cookie", false);
 		if (!noobCookie){
 			showNoobDialog();
 			userAintANoob();
 		}
 	}
 	
 
 	/**
 	 * Displays an {@link AlertDialog} with one button that dismisses the dialog. Dialog displays helpful first-time info.
 	 * 
 	 * @param message
 	 */
 	@SuppressWarnings("deprecation")
 	private void showNoobDialog() {
 		// Yes, this is deprecated, but there's a conflict with RA.java extending FragmentActivity and MapActivity
 		showDialog(INSTRUCTIONS_DIALOG);
 	}
 	
 	/**
 	 *  If the user sees the first-time instruction dialog, they won't see it again next time.
 	 */
 	private void userAintANoob() {
 		SharedPreferences.Editor ed = resultsActivityPrefs.edit();
 		ed.putBoolean("noob_cookie", true);
 		ed.commit();	
 	}
 	
 	
 	@Override
 	protected Dialog onCreateDialog(int id) {
 		switch(id){
 			case INSTRUCTIONS_DIALOG:
 				AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		        builder.setTitle(R.string.results_noob_title);
 		        builder.setMessage(R.string.results_noob_instructions);
 				builder.setPositiveButton(android.R.string.ok, 
 						new DialogInterface.OnClickListener() {
 							public void onClick(DialogInterface dialog, int which) {
 		                      return;
 		                } });
 		        return builder.create();
 			}
 		return null;
 	}
 	
 	
 	void initMapView() {		
 		mapView = (MapView) findViewById(R.id.mapview_results);
 		mapView.setBuiltInZoomControls(true);		// Don't let the user do anything to the map, and don't display zoom buttons
 		mapController 		= mapView.getController();
 		mapOverlays 		= mapView.getOverlays();
 		Drawable drawable 	= this.getResources().getDrawable(R.drawable.pin1);
 		geoPoints 			= new ArrayList<GeoPoint>();
 	}
 	
 	
 	private void zoomToOverlays(List<GeoPoint> geoPoints){
 		//http://stackoverflow.com/questions/5241487/android-mapview-setting-zoom-automatically-until-all-itemizedoverlays-are-visi
 		
 		int minLat = Integer.MAX_VALUE;
 		int maxLat = Integer.MIN_VALUE;
 		int minLon = Integer.MAX_VALUE;
 		int maxLon = Integer.MIN_VALUE;
 
 		for (GeoPoint item : geoPoints) 
 		{ 
 
 		      int lat = item.getLatitudeE6();
 		      int lon = item.getLongitudeE6();
 
 		      maxLat = Math.max(lat, maxLat);
 		      minLat = Math.min(lat, minLat);
 		      maxLon = Math.max(lon, maxLon);
 		      minLon = Math.min(lon, minLon);
 		 }
 
 		double fitFactor = 2.3;
 		mapController.setCenter(new GeoPoint( (maxLat + minLat)/2, (maxLon + minLon)/2 ));
 //		mapController.setZoom(17);
 		mapController.zoomToSpan((int) (Math.abs(maxLat - minLat) * fitFactor), (int)(Math.abs(maxLon - minLon) * fitFactor));
 		mapController.animateTo(new GeoPoint( (maxLat + minLat)/2, (maxLon + minLon)/2 )); 
 	}
 	
 
 	// Dynamically build the results screen by building a ResultsRowView, which inflates view_result_segment
 	private void buildResultsView() {
 		int addressesSize = route.getAddresses().size();
 
 		Address address = null;
 		// TODO: do we need lastAddress?
 		boolean isLastAddress = false;
 		ResultsSegmentView v;
 
 		for (int addressIndex = 0; addressIndex < addressesSize; addressIndex++) {
 			address = route.getAddresses().get(addressIndex);
 			// special case if it's the last segment
 			if (addressIndex == addressesSize - 1) {
 				isLastAddress = true;
 			}
 			
 			// Put point on MapView
 			//http://stackoverflow.com/questions/3577866/android-geopoint-with-lat-long-values
 			GeoPoint geopoint = new GeoPoint((int) (address.getLatitude() * 1E6), (int) (address.getLongitude() * 1E6));
 			geoPoints.add(geopoint);
 			OverlayItem overlayitem = new OverlayItem(geopoint, address.getFeatureName(), "Location #" + addressIndex);
 			RoutyItemizedOverlay itemizedOverlay = new RoutyItemizedOverlay(Util.getItemizedPin(addressIndex, mContext));
 			itemizedOverlay.addOverlay(overlayitem);
 			
 //			// Draw route
 //			if (!isLastAddress){
 //				Address nextAddress		= route.getAddresses().get(addressIndex + 1);
 //				GeoPoint nextGeopoint 	= new GeoPoint((int) (nextAddress.getLatitude() * 1E6), (int) (nextAddress.getLongitude() * 1E6));
 //				drawPath(geopoint, nextGeopoint);
 //			}
 
 			
 			v = new ResultsSegmentView(mContext, address, addressIndex, isLastAddress) {
 
 				@Override
 				public void onSegmentClicked(int id, boolean isLastAddress) {
 					if (!isLastAddress){
 						volume = (float) audioManager.getStreamVolume(AudioManager.STREAM_SYSTEM);
 						volume = volume / audioManager.getStreamMaxVolume(AudioManager.STREAM_SYSTEM);
 						sounds.play(click, volume, volume, 1, 0, 1);
 							
 						// Get start and end Addresses from route[] - the index is the id in ResultsSegmentView
 						Address startAddress	= route.getAddresses().get(id);
 						Address endAddress 		= route.getAddresses().get(id + 1);
 	
 						// Build call to Google Maps native app
 						double startAddressLat = startAddress.getLatitude();
 						double startAddressLong = startAddress.getLongitude();
 						double endAddressLat = endAddress.getLatitude();
 						double endAddressLong = endAddress.getLongitude();
 
 						// Button segment GMaps call
 						String mapsCall = "http://maps.google.com/maps?saddr="
 								+ startAddressLat + "," + startAddressLong
 								+ "&daddr=" + endAddressLat + ","
 								+ endAddressLong;
 						Log.d(TAG, "maps segment call URI: " + mapsCall);
 	
 						// Open Google Maps App on the device
 						Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(mapsCall));
 						startActivity(intent);
 					}
 				}
 			};
 
 			resultsLayout.addView(v, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
 			mapOverlays.add(itemizedOverlay);
 		}
 		
 		zoomToOverlays(geoPoints);
 
 		TextView text_total_distance = (TextView) findViewById(R.id.textview_total_distance);
		TextView text_total_duration = (TextView) findViewById(R.id.textview_total_duration);
		
		if (routeOptimizePreference.equals(RouteOptimizePreference.PREFER_DISTANCE)) {
			text_total_duration.setVisibility(View.INVISIBLE);
			String truncatedDistanceInMiles = convertMetersToMiles(route.getTotalDistance());
			text_total_distance.setText(getString(R.string.total_distance) + truncatedDistanceInMiles + " miles");
		} else if (routeOptimizePreference.equals(RouteOptimizePreference.PREFER_DURATION)) {
			text_total_distance.setVisibility(View.INVISIBLE);
			String durationInMinutes = convertSecondsToMinutes(route.getTotalDistance());
			text_total_duration.setText(getString(R.string.total_duration) + durationInMinutes + " minutes");
		}
		
		
 	}
 
 	private String convertMetersToMiles(int distanceInMeters) {
 		final double MILE_RATIO 	= 0.000621371;
 		double distanceInMiles 		= distanceInMeters * MILE_RATIO; 
 		return new DecimalFormat("#.##").format(distanceInMiles);
 	}
	
	private String convertSecondsToMinutes(int durationInSeconds) {
		final double RATIO = 60;
		double durationInMinutes = durationInSeconds / RATIO;
		return Long.valueOf(Math.round(durationInMinutes)).toString();
	}
 
 	
 	private void drawPath(GeoPoint startPoint,GeoPoint endPoint){		
 	    MapRoute oRoute = new MapRoute(startPoint,endPoint);
 	    oRoute.getPoints(new RouteListener(){
 	    	
 	        @Override
 	        public void onDetermined(ArrayList<GeoPoint> geoPoints){
 	            GeoPoint oPointA = null;
 	            GeoPoint oPointB = null;
 
 //	            mapView.getOverlays().clear();
 
 	            for(int i=1; i<geoPoints.size()-1; i++){
 	                oPointA = geoPoints.get(i-1);
 	                oPointB = geoPoints.get(i);
 	                mapOverlays.add(new MapRouteOverlay(oPointA,oPointB,2,Color.RED));
 	            }
 //	            mapOverlays.add(new MapRoutePinOverlay(geoPoints.get(0),dPin));
 //	            mapOverlays.add(new MapRoutePinOverlay(geoPoints.get(geoPoints.size()-1),dPin));
 
 	            mapView.invalidate();
 	        }
 	        
 	        @Override
 	        public void onError(){
 	        }           
 	        
 	    });
 	}
 	
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.activity_results, menu);
 		return true;
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		sounds = new SoundPool(3, AudioManager.STREAM_MUSIC, 0);
 		click = sounds.load(this, R.raw.routyclick, 1);
 	}
 
 	@Override
 	public void onPause() {
 		super.onPause();
 		if (sounds != null) {
 			sounds.release();
 			sounds = null;
 		}
 	}
 
 	@Override
 	protected boolean isRouteDisplayed() {
 		return false;
 	}
 }
