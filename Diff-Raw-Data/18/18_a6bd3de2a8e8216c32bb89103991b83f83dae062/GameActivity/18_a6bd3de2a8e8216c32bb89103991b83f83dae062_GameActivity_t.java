 package fi.hiit.serenghetto.activity;
 
 import android.util.Log;
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.MenuItem;
 import android.view.Menu;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.Context;
 import android.content.BroadcastReceiver;
 import android.location.Location;
 import android.database.Cursor;
 
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapView;
 import com.google.android.maps.MapController;
 import com.google.android.maps.MapActivity;
 import com.google.android.maps.Overlay;
 
 import fi.hiit.serenghetto.R;
 import fi.hiit.serenghetto.SerenghettoApplication;
 import fi.hiit.serenghetto.constants.IntentConstants;
 import fi.hiit.serenghetto.dto.Barcode;
 import fi.hiit.serenghetto.map.MapCircleOverlay;
 
 
 public class GameActivity extends MapActivity
 {
     private SerenghettoApplication app;
     private MapView mapGame;
     private MapController mapController;
     private BestLocationEstimateReceiver receiver;
     private IntentFilter filter;
     private boolean bCentered;
 
 
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.game);
 
         this.app = (SerenghettoApplication) getApplication();
 
         bCentered = false;
 
         Intent i = getIntent();
         String id = i.getStringExtra("id");
 
         mapGame = (MapView) findViewById(R.id.mapGame);
         if (mapGame != null) {
             mapGame.setBuiltInZoomControls(true);
             mapController = mapGame.getController();
 
             if (id != null) {
                 Barcode barcode = this.app.getBarcodeData().getBarcodeById(id);
                 if (barcode != null) {
                     centerLocation(barcode.getLatitude(), barcode.getLongitude());
                     bCentered = true;
                 }
             }
             
             initOverlays(id);
         }
         else  {
             //[FIXME]
             Log.d(SerenghettoApplication.TAG, "NO MAP REFERENCE");
         }
     
         // Create the barcodes updated receiver
         receiver = new BestLocationEstimateReceiver();
         filter = new IntentFilter(IntentConstants.NEW_BEST_LOCATION_ESTIMATE_INTENT);
     }
 
     protected void initOverlays(String curId) {
         // get users' barcodes
         Cursor barcodeCursor = app.getBarcodeData().getBarcodesByUser(app.getUserId());
         Barcode curBarcode = null;
 
         if (barcodeCursor != null) {
             Log.d(SerenghettoApplication.TAG, "start cursor walk..");
             while (barcodeCursor.moveToNext()) {
                 Barcode barcode = new Barcode(barcodeCursor);
                 if (curId != null && barcode.getId().equals(curId)) {
                     curBarcode = barcode;
                     continue;
                 }
                 else if (barcode.hasLocation()) {
                     mapGame.getOverlays().add(new MapCircleOverlay(barcode, SerenghettoApplication.MAP_OVERLAY_ALPHA, 0x00, 0x00, 0xFF));
                 }
             }
             if (curBarcode != null && curBarcode.hasLocation()) {
                mapGame.getOverlays().add(new MapCircleOverlay(curBarcode, 0xAA, 0x00, 0xFF, 0x00));
             }
             barcodeCursor.close();
             Log.d(SerenghettoApplication.TAG, "end cursor walk..");
         }
     }
 
     @Override
     protected void onPause() {
         super.onPause();
         Log.d(SerenghettoApplication.TAG, "GameActivity.onPause");
 
         // UNregister the receiver
         if (receiver != null) {
             unregisterReceiver(receiver); 
         }
     }
     
     @Override
     protected void onResume() {
         super.onResume();
         Log.d(SerenghettoApplication.TAG, "GameActivity.onResume");
 
         // Register the receiver
         if (receiver != null && filter != null) {
             registerReceiver(receiver, filter, null, null);
         }
     }
 
     @Override
     protected void onStart() {
         super.onStart();
         Log.d(SerenghettoApplication.TAG, "GameActivity.onStart");
     }
 
     @Override
     protected void onRestart() {
         super.onRestart();
         Log.d(SerenghettoApplication.TAG, "GameActivity.onRestart");
     }
 
     @Override
     protected void onStop() {
         super.onStop();
         Log.d(SerenghettoApplication.TAG, "GameActivity.onStop");
     }
 
     @Override
     protected void onDestroy() {
         super.onDestroy();
         Log.d(SerenghettoApplication.TAG, "GameActivity.onDestroy");
     }
 
     @Override
     protected boolean isRouteDisplayed() {
         return false;
     }
     
     private void onLocation(Location location) {
         if (!bCentered) {
             centerLocation(location);
             bCentered = true;
         }
     }
 
     private void centerLocation(GeoPoint p) {
         if (p != null) {
             if (mapController != null) {
                 mapController.animateTo(p);
             }
         }
     }
     private void centerLocation(double latitude, double longitude) {
         GeoPoint p = new GeoPoint((int)(latitude*1E6), (int)(longitude*1E6));
         centerLocation(p);
     }
     private void centerLocation(Location location) {
         if (location != null) {
             GeoPoint p = new GeoPoint((int)(location.getLatitude()*1E6), (int)(location.getLongitude()*1E6));
             centerLocation(p);
         }
     }
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
     	return ActivityUtil.onCreateOptionsMenu(this, menu);
     }
     
     @Override
 	public boolean onOptionsItemSelected(MenuItem item) {
     	return ActivityUtil.onOptionsItemSelected(this, item);
 	}
 
     // Receiver to wake up when BarcodesService gets new best location estimate
     class BestLocationEstimateReceiver extends BroadcastReceiver
     {
         @Override
         public void onReceive(Context context, Intent intent) {
             Location location = GameActivity.this.app.getBestLocationEstimate();
             GameActivity.this.onLocation(location);
         }
     }
 }
 
