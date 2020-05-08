 package com.example.getconnected.activities;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.example.getconnected.R;
 import com.example.getconnected.gps.GPSLocator;
 
 
 import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
 import org.osmdroid.util.GeoPoint;
 import org.osmdroid.views.MapController;
 import org.osmdroid.views.MapView;
 import org.osmdroid.views.overlay.ItemizedIconOverlay;
 import org.osmdroid.views.overlay.MyLocationOverlay;
 import org.osmdroid.views.overlay.OverlayItem;
 
 import android.app.AlertDialog;
 import android.content.Context;
 import android.os.Bundle;
 import android.view.Menu;
 public class MapActivity extends BaseActivity {
 
 	private MapController mapController;
     private MapView mapView;
     protected GPSLocator locator;
     private MyLocationOverlay myLocationoverlay;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_map);
         initLayout(R.string.title_activity_map, true, true, true, false);
         
         mapView = (MapView) findViewById(R.id.mapview);
         mapView.setTileSource(TileSourceFactory.MAPNIK);
         mapView.setBuiltInZoomControls(true);
         mapController = mapView.getController();
         mapController.setZoom(15);
         locator = new GPSLocator(getApplicationContext());
         GeoPoint point2 = new GeoPoint(locator.getLatitude(), locator.getLongitude());
         mapController.setCenter(point2);
        /*
         myLocationoverlay = new MyLocationOverlay(this, mapView);
         myLocationoverlay.disableCompass();
         myLocationoverlay.disableFollowLocation();
         myLocationoverlay.setDrawAccuracyEnabled(true);
         myLocationoverlay.runOnFirstFix(new Runnable() {
         public void run() {
                 mapController.animateTo(myLocationoverlay
                         .getMyLocation());
             }
         });
         
         myLocationoverlay.enableMyLocation();
        */
         ArrayList<OverlayItem> overlayItemArray = new ArrayList<OverlayItem>();
        OverlayItem olItem = new OverlayItem("Here", "SampleDescription", point2);
         overlayItemArray.add(olItem);
         MyOwnItemizedOverlay overlay = new MyOwnItemizedOverlay(this, overlayItemArray);
         mapView.getOverlays().add(overlay);
        //mapView.getOverlays().add(myLocationoverlay);
     	
     }
     
     @Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.map, menu);
 		return true;
 	}
     
     public class MyOwnItemizedOverlay extends ItemizedIconOverlay<OverlayItem> {
         protected Context mContext;
 
         public MyOwnItemizedOverlay(final Context context, final List<OverlayItem> aList) {
              
         	super(context, aList, new OnItemGestureListener<OverlayItem>() {
                     @Override public boolean onItemSingleTapUp(final int index, final OverlayItem item) {
                             return false;
                     }
                     @Override public boolean onItemLongPress(final int index, final OverlayItem item) {
                             return false;
                     }
                   } );
            // TODO Auto-generated constructor stub
              mContext = context;
         }
/*
         @Override 
         protected boolean onSingleTapUpHelper(final int index, final OverlayItem item, final MapView mapView) {
            //Toast.makeText(mContext, "Item " + index + " has been tapped!", Toast.LENGTH_SHORT).show();
             AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
             dialog.setTitle(item.getTitle());
             dialog.setMessage(item.getSnippet());
             dialog.show();
             return true;
         }
        */
     }
 }
