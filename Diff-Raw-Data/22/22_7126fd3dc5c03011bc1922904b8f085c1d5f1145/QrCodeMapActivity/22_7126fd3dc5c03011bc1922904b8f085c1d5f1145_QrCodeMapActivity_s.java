 package at.qraz.qrcodelocalizer.activity;
 
 import java.util.List;
 
 import android.app.ActionBar;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.location.Location;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import at.qraz.qrcodelocalizer.CodeLocation;
 import at.qraz.qrcodelocalizer.R;
 import at.qraz.qrcodelocalizer.Settings;
 import at.qraz.qrcodelocalizer.WebServiceClient;
 import at.qraz.qrcodelocalizer.view.MapViewEx;
 import at.qraz.qrcodelocalizer.view.QRCodeOverlay;
 import at.qraz.qrcodelocalizer.view.MapAreaChangedListener;
 
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapActivity;
 import com.google.android.maps.MapController;
 import com.google.android.maps.MyLocationOverlay;
 import com.google.android.maps.Overlay;
 import com.google.android.maps.Projection;
 
 public class QrCodeMapActivity extends MapActivity {
 
     private final int DIALOG_SHOW_MAPTYPE_SELECTION = 1;
 
     private MyLocationOverlay _myLocationOverlay;
     private MapViewEx _mapView;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         setContentView(R.layout.qrcodemap);
 
         Settings.initialize(this);
 
         _mapView = (MapViewEx) findViewById(R.id.largeMapView);
         _mapView.setBuiltInZoomControls(true);
 
         _mapView.setMapAreaChangedListener(new MapAreaChangedListener() {
 
             @Override
             public void mapAreaChanged(int newZoomLevel) {
                 System.out.println("zoom changed");
                 loadQrCodesForChangedZoomLevel(newZoomLevel);
             }
         });
 
         LocationManager manager = (LocationManager) getSystemService(LOCATION_SERVICE);
         Location l = manager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
 
        MapController controller = _mapView.getController();
        controller.setZoom(MapViewEx.ZOOM_LEVEL_LARGE);
        controller.setCenter(new GeoPoint((int) (l.getLatitude() * 1E6), (int) (l.getLongitude() * 1E6)));

         _myLocationOverlay = new MyLocationOverlay(this, _mapView);
         List<Overlay> overlays = _mapView.getOverlays();
         overlays.add(_myLocationOverlay);
 
         ActionBar bar = getActionBar();
         bar.setHomeButtonEnabled(true);
         bar.setDisplayHomeAsUpEnabled(true);
     }
 
     @Override
     protected void onPause() {
         _myLocationOverlay.disableCompass();
         _myLocationOverlay.disableMyLocation();
 
         super.onPause();
     }
 
     @Override
     protected void onResume() {
         _myLocationOverlay.enableCompass();
         _myLocationOverlay.enableMyLocation();
 
         super.onResume();
     }
 
     @Override
     protected boolean isRouteDisplayed() {
         return false;
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater f = getMenuInflater();
         f.inflate(R.menu.location_preview_menu, menu);
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case R.id.toggleMapType:
                 showDialog(DIALOG_SHOW_MAPTYPE_SELECTION);
                 break;
 
             case android.R.id.home:
                 // app icon in action bar clicked; go home
                 Intent intent = new Intent(this, Main.class);
                 intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                 startActivity(intent);
                 break;
         }
 
         return true;
     }
 
     @Override
     protected Dialog onCreateDialog(int id) {
 
         switch (id) {
             case 1:
                 return createMapTypeSelection();
 
             default:
                 return null;
         }
     }
 
     private Dialog createMapTypeSelection() {
         final String[] items = { getString(R.string.map), getString(R.string.satellite) };
 
         int selectedItem = _mapView.isSatellite() ? 1 : 0;
 
         AlertDialog.Builder builder = new AlertDialog.Builder(this);
         builder.setTitle(getString(R.string.mapTypeSelection));
         builder.setSingleChoiceItems(items, selectedItem, new DialogInterface.OnClickListener() {
 
             public void onClick(DialogInterface dialog, int item) {
 
                 _mapView.setSatellite(item == 1);
                 dismissDialog(DIALOG_SHOW_MAPTYPE_SELECTION);
             }
         });
 
         return builder.create();
     }
 
     protected void loadQrCodesForChangedZoomLevel(int newZoomLevel) {
         System.out.println("zoomLevel: " + newZoomLevel);
 
         try {
             Projection projection = _mapView.getProjection();
             GeoPoint topLeft = projection.fromPixels(0, 0);
             GeoPoint topRight = projection.fromPixels(_mapView.getWidth(), 0);
             GeoPoint bottomLeft = projection.fromPixels(0, _mapView.getHeight());
             GeoPoint bottomRight = projection.fromPixels(_mapView.getWidth(), _mapView.getHeight());
 
             WebServiceClient wsClient = new WebServiceClient();
             List<CodeLocation> locations = wsClient.getCodeLocationsInArea(convertToDouble(topLeft.getLatitudeE6()), convertToDouble(topLeft.getLongitudeE6()), convertToDouble(topRight.getLatitudeE6()), convertToDouble(topRight.getLongitudeE6()), convertToDouble(bottomLeft.getLatitudeE6()), convertToDouble(bottomLeft.getLongitudeE6()), convertToDouble(bottomRight.getLatitudeE6()), convertToDouble(bottomRight.getLongitudeE6()));
            
             System.out.println("Count: " + locations.size());
            
            for(CodeLocation l : locations)
                 _mapView.getOverlays().add(new QRCodeOverlay(this, l));
         }
         catch (Exception ex) {
             ex.printStackTrace();
         }
     }
 
     private double convertToDouble(int value) {
         return (double) value / 1E6;
     }
 }
