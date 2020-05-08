 package edu.umn.pinkpanthers.beerfinder.activities;
 
 import java.util.ArrayList;
 import java.util.List;
 import android.content.Intent;
 import android.graphics.drawable.Drawable;
 import android.location.Location;
 import android.os.Bundle;
 import android.view.View;
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.ItemizedOverlay;
 import com.google.android.maps.MapActivity;
 import com.google.android.maps.MapController;
 import com.google.android.maps.MapView;
 import com.google.android.maps.OverlayItem;
 import edu.umn.pinkpanthers.beerfinder.R;
 import edu.umn.pinkpanthers.beerfinder.data.Venue;
 import edu.umn.pinkpanthers.beerfinder.network.BeerFinderWebService;
 import edu.umn.pinkpanthers.beerfinder.network.LocationResolver;
 
 public class MapResultsActivity extends MapActivity {
 
     private MapView mapView;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.map_results_screen);
         mapView = (MapView) findViewById(R.id.map_results_view);
         mapView.setBuiltInZoomControls(true);
         mapView.setSatellite(false);
 
         Location defaultLocation = LocationResolver.getDefaultLocation();
         GeoPoint defaultPoint = LocationResolver.makeGeoPoint(defaultLocation);
         MapController mc = mapView.getController();
         mc.setCenter(defaultPoint);
         mc.setZoom(14);
 
         createOverlays();
     }
 
     public void homeClicked(View view) {
         finish();
     }
 
     public void listResultsClicked(View view) {
         Intent listResultsIntent = new Intent(this, ListResultsActivity.class);
         startActivity(listResultsIntent);
     }
 
     @Override
     protected boolean isRouteDisplayed() {
         return false;
     }
 
     private void createOverlays() {
         // create the itemized overlay
         Drawable drawable = this.getResources().getDrawable(android.R.drawable.btn_star_big_on);
         VenueItemizedOverlay venueOverlay = new VenueItemizedOverlay(drawable);
         mapView.getOverlays().add(venueOverlay);
 
         // TODO make this into a query call, instead of just getting a
         // searchable list of venues
         List<Venue> venues = BeerFinderWebService.getInstance().getSearchableVenueList();
 
         // Add an overlay for each Venue
         for (Venue venue : venues) {
             GeoPoint venuePoint = venue.getLocation();
             OverlayItem venueItem = new OverlayItem(venuePoint, venue.getName(), venue.getAddress());
             venueOverlay.addOverlay(venueItem);
         }
     }
 
     private class VenueItemizedOverlay extends ItemizedOverlay<OverlayItem> {
 
         private ArrayList<OverlayItem> overlays = new ArrayList<OverlayItem>();
 
         public VenueItemizedOverlay(Drawable drawable) {
             super(boundCenterBottom(drawable));
         }
 
         @Override
         protected OverlayItem createItem(int i) {
             return overlays.get(i);
         }
 
         @Override
         public int size() {
             return overlays.size();
         }
 
         public void addOverlay(OverlayItem overlay) {
             overlays.add(overlay);
             populate();
         }
     }
 
 }
