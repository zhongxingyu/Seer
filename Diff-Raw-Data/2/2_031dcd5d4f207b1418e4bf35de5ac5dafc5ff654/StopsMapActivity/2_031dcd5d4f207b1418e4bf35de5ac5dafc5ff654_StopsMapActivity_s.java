 /* vim: set ts=4 sw=4 et: */
 
 package org.gitorious.scrapfilbleu.android;
 
 import java.util.ArrayList;
 
 import android.util.Log;
 
 import android.os.Bundle;
 
 import android.location.Location;
 
 import android.widget.Toast;
 
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnCancelListener;
 
 import android.graphics.drawable.Drawable;
 
 import org.osmdroid.util.GeoPoint;
 import org.osmdroid.util.BoundingBoxE6;
 import org.osmdroid.views.overlay.ItemizedIconOverlay;
 import org.osmdroid.views.overlay.ItemizedOverlay;
 import org.osmdroid.views.overlay.OverlayItem;
 import org.osmdroid.DefaultResourceProxyImpl;
 import org.osmdroid.ResourceProxy;
 
 import org.osmdroid.events.MapListener;
 import org.osmdroid.events.ZoomEvent;
 import org.osmdroid.events.ScrollEvent;
 
 public class StopsMapActivity extends MapViewActivity
 {
     private String[] stopsNames;
     private double[] latitudes;
     private double[] longitudes;
     private boolean dep;
     private boolean arr;
     private Location whereAmI;
     private BoundingBoxE6 bbox;
     private Drawable stopsMarker;
     private Drawable posMarker;
     private boolean saveBBOX;
 
     private ItemizedOverlay<OverlayItem> stopsOverlay;
     private ItemizedOverlay<OverlayItem> myLocationOverlay;
     private ResourceProxy mResourceProxy;
 
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
         double bboxNorth = 0, bboxEast = 0, bboxSouth = 0, bboxWest = 0;
         super.onCreate(savedInstanceState);
 
         this.mResourceProxy = new DefaultResourceProxyImpl(getApplicationContext());
         this.stopsMarker = this.mResourceProxy.getDrawable(ResourceProxy.bitmap.marker_default);
         this.posMarker = this.mResourceProxy.getDrawable(ResourceProxy.bitmap.center);
         ArrayList<OverlayItem> items = new ArrayList<OverlayItem>();
         ArrayList<OverlayItem> pos = new ArrayList<OverlayItem>();
         this.saveBBOX = true;
 
         Bundle extras = getIntent().getExtras();
         if (extras != null) {
             this.stopsNames = extras.getStringArray("stopsNames");
             this.latitudes = extras.getDoubleArray("latitudes");
             this.longitudes = extras.getDoubleArray("longitudes");
             this.whereAmI = (Location)extras.get("location");
             this.dep = extras.getBoolean("selectDeparture");
             this.arr = extras.getBoolean("selectArrival");
 
             if (this.dep && this.arr) {
                 this.selectStopsItems = new CharSequence[] { getString(R.string.sens_departure), getString(R.string.sens_arrival) };
             } else {
                 if (this.dep) {
                     this.selectStopsItems = new CharSequence[] { getString(R.string.sens_departure) };
                 }
                 if (this.arr) {
                     this.selectStopsItems = new CharSequence[] { getString(R.string.sens_arrival) };
                 }
             }
         }
 
         if (this.stopsNames != null && this.latitudes != null && this.longitudes != null) {
             if (this.stopsNames.length == this.latitudes.length && this.latitudes.length == this.longitudes.length && this.longitudes.length > 0) {
                 Log.e("BusTours:StopsMaps", "Got some points ...");
                 int i;
                 double minNorth = 180, minEast = 180, maxSouth = -180, maxWest = -180;
                 for (i = 0; i < this.stopsNames.length; i++) {
                     Log.e("BusTours:StopsMaps", "Stops @ (" + String.valueOf(this.latitudes[i]) + ", " + String.valueOf(this.longitudes[i]) + ") == " + this.stopsNames[i]);
                     items.add(new OverlayItem(this.stopsNames[i], this.stopsNames[i], new GeoPoint((int)(this.latitudes[i]*1e6), (int)(this.longitudes[i]*1e6))));
                     if (this.latitudes[i] < minNorth) {
                         minNorth = this.latitudes[i];
                     }
                     if (this.latitudes[i] > maxSouth) {
                         maxSouth = this.latitudes[i];
                     }
                     if (this.longitudes[i] < minEast) {
                         minEast = this.longitudes[i];
                     }
                     if (this.longitudes[i] > maxWest) {
                         maxWest = this.longitudes[i];
                     }
                 }
                 Log.e("BusTours:StopsMaps", "minEast=" + String.valueOf(minEast));
                 Log.e("BusTours:StopsMaps", "maxWest=" + String.valueOf(maxWest));
                 Log.e("BusTours:StopsMaps", "minNorth=" + String.valueOf(minNorth));
                 Log.e("BusTours:StopsMaps", "maxSouth=" + String.valueOf(maxSouth));
                 bboxNorth = minNorth;
                 bboxEast = minEast;
                 bboxSouth = maxSouth;
                 bboxWest = maxWest;
             }
         }
 
         /* OnTapListener for the Markers, shows a simple Toast. */
         this.stopsOverlay = new ItemizedIconOverlay<OverlayItem>(items,
             this.stopsMarker,
             new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
                 @Override
                 public boolean onItemSingleTapUp(final int index,
                         final OverlayItem item) {
                     displayStopInfos(item);
                     return true; // We 'handled' this event.
                 }
                 @Override
                 public boolean onItemLongPress(final int index,
                         final OverlayItem item) {
                     selectStop(item);
                     return false;
                 }
             }, mResourceProxy);
 
         pos.add(new OverlayItem("Your location", "Your location", new GeoPoint((int)(this.whereAmI.getLatitude()*1e6), (int)(this.whereAmI.getLongitude()*1e6))));
         this.myLocationOverlay = new ItemizedIconOverlay<OverlayItem>(
             pos,
             this.posMarker,
             new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
                 @Override
                 public boolean onItemSingleTapUp(final int index,
                         final OverlayItem item) {
                     Toast.makeText(
                             StopsMapActivity.this,
                             item.mTitle, Toast.LENGTH_LONG).show();
                     return true; // We 'handled' this event.
                 }
                 @Override
                 public boolean onItemLongPress(final int index,
                         final OverlayItem item) {
                     Toast.makeText(
                             StopsMapActivity.this,
                             item.mTitle ,Toast.LENGTH_LONG).show();
                     return false;
                 }
             }, mResourceProxy);
 
         this.getOsmMap().getOverlays().add(this.stopsOverlay);
         this.getOsmMap().getOverlays().add(this.myLocationOverlay);
         this.getOsmMap().setMapListener(new MapListener() {
             public boolean onScroll(ScrollEvent event) {
                 Log.e("BusTours:StopsMap", "ScrollEvent");
                 if (saveBBOX) {
                     bbox = getOsmMap().getBoundingBox();
                 }
                 return true;
             }
             public boolean onZoom(ZoomEvent event) {
                 Log.e("BusTours:StopsMap", "ZoomEvent");
                 if (saveBBOX) {
                     bbox = getOsmMap().getBoundingBox();
                 }
                 return true;
             }
         });
         this.bbox = new BoundingBoxE6(bboxNorth, bboxEast, bboxSouth, bboxWest);
     }
 
     @Override
     public void onWindowFocusChanged(boolean hasFocus)
     {
         if (hasFocus) {
             this.saveBBOX = false;
             Log.e("BusTours:StopsMap", "Got focus, zooming to " + this.bbox);
             Log.e("BusTours:StopsMap", "Targeting &lat=" + (this.bbox.getCenter().getLatitudeE6()/1E6) + "&lon=" + (this.bbox.getCenter().getLongitudeE6()/1E6) + "&zoom=15");
             this.getOsmMap().getController().setCenter(this.bbox.getCenter());
             this.getOsmMap().getController().zoomToSpan(this.bbox);
             this.saveBBOX = true;
         }
     }
 
     public void displayStopInfos(OverlayItem item)
     {
         AlertDialog.Builder dialog = new AlertDialog.Builder(StopsMapActivity.this);
         dialog.setTitle(getString(R.string.stop_info));
         dialog.setMessage(getString(R.string.stop_info_msg) + " " + item.mTitle);
         dialog.setPositiveButton(
             getString(R.string.okay),
             new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int id) {
                     dialog.cancel();
                 }
             }
         );
 
         AlertDialog d = dialog.create();
         d.show();
     }
 
     public void selectStop(OverlayItem item)
     {
         AlertDialog.Builder dialog = new AlertDialog.Builder(StopsMapActivity.this);
         dialog.setTitle(getString(R.string.select_stop));
         // dialog.setMessage(getString(R.string.select_stop_msg) + " " + item.mTitle);
         dialog.setSingleChoiceItems(
             selectStopsItems, -1,
             new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int id) {
                     Log.e("BusTours:StopsMap", "checked: " + selectStopsItems[id]);
                     dialog.dismiss();
                 }
             }
         );
 
         AlertDialog d = dialog.create();
         d.show();
     }
 }
