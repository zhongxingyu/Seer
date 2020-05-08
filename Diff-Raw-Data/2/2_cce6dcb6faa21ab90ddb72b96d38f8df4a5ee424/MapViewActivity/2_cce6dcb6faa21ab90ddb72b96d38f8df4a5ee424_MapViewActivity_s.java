 package com.aboveware.abovetracker;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import android.content.Context;
 import android.content.Intent;
 import android.database.Cursor;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Paint;
 import android.graphics.Path;
 import android.graphics.Point;
 import android.location.Location;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.provider.Settings.Secure;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.Toast;
 
 import com.aboveware.abovetracker.TrackService.Mode;
 import com.aboveware.abovetracker.TrackService.TrackServiceListener;
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapActivity;
 import com.google.android.maps.MapController;
 import com.google.android.maps.MapView;
 import com.google.android.maps.Overlay;
 import com.google.android.maps.Projection;
 
 public class MapViewActivity extends MapActivity implements TrackServiceListener {
 
   public final static String COMMAND = "COMMAND";
   static View start;
   static View stop;
   static View pause;
   static View resume;
 
   public void ClickHandler(View v) {
     command(v.getId());
   }
 
   private void command(int id) {
     switch (id) {
     case R.id.buttonPause:
       Dashboard.trackServiceConnection.pause();
       break;
     case R.id.buttonResume:
       Dashboard.trackServiceConnection.resume();
       break;
     case R.id.buttonStart:
       start();
       break;
     case R.id.buttonStop:
       Dashboard.trackServiceConnection.stop();
       break;
     }
     Dashboard.updateButtons(this);
   }
 
   private void start() {
     if (!Secure.isLocationProviderEnabled(getContentResolver(), LocationManager.GPS_PROVIDER)) {
       Dashboard.ask4Gps(this);
     } else {
       ask4Name();
     }
   }
 
   private void ask4Name() {
     final Context context = this;
     final PromptDialog dlg = new PromptDialog(this, R.string.enter_name_title, R.string.enter_name,
         String.format("%1$s %2$tc", getString(R.string.track), new Date())) {
       @Override
       public boolean onOkClicked(String input) {
         if (input.length() > 0) {
           Dashboard.trackServiceConnection.start(input);
           Dashboard.updateButtons(context);
         }
         return true;
       }
     };
     dlg.show();
   }
 
   public void ModeChange(Mode mode) {
     Dashboard.updateButtons(this);
   }
 
   @Override
   public void onCreate(Bundle savedInstanceState) {
     super.onCreate(savedInstanceState);
     setContentView(R.layout.map);
     start = findViewById(R.id.buttonStart);
     stop = findViewById(R.id.buttonStop);
     pause = findViewById(R.id.buttonPause);
     resume = findViewById(R.id.buttonResume);
 
     Dashboard.trackServiceConnection = new TrackServiceConnection(this, getIntent(), this);
     new SmsLogger(this).execute((Void[]) null);
     mapView = (MapView) findViewById(R.id.mapView);
     // enable Street view by default
     mapView.setStreetView(true);
     // enable to show Satellite view
     // mapView.setSatellite(true);
     // enable to show Traffic on map
     // mapView.setTraffic(true);
     mapView.setBuiltInZoomControls(true);
 
     mapController = mapView.getController();
     mapController.setZoom(16);
 
     // Show'em where we're
     LocationChange(TrackService.getLastKnownLocation(this));
   }
 
   /*
    * (non-Javadoc)
    * 
    * @see android.app.Activity#onCreateContextMenu(android.view.ContextMenu,
    * android.view.View, android.view.ContextMenu.ContextMenuInfo)
    */
   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
     getMenuInflater().inflate(R.menu.main, menu);
     return true;
   }
 
   /*
    * (non-Javadoc)
    * 
    * @see android.app.Activity#onPrepareOptionsMenu(android.view.Menu)
    */
   @Override
   public boolean onPrepareOptionsMenu(Menu menu) {
     for (int index = 0; index < menu.size(); ++index) {
       if (menu.getItem(index).getItemId() == R.id.archive || menu.getItem(index).getItemId() == R.id.settings)
         menu.getItem(index).setEnabled(Mode.STOPPED == Dashboard.trackServiceConnection.getMode());
     }
     return super.onPrepareOptionsMenu(menu);
   }
 
   @Override
   protected void onDestroy() {
     super.onDestroy();
     Dashboard.trackServiceConnection.Unbind();
   }
 
   /*
    * (non-Javadoc)
    * 
    * @see android.app.Activity#onContextItemSelected(android.view.MenuItem)
    */
   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
     switch (item.getItemId()) {
     case R.id.settings:
       startActivity(new Intent(this, Preferences.class));
       break;
     case R.id.log:
       startActivity(new Intent(this, SmsErrorActivity.class));
       break;
     case R.id.archive:
       startActivity(new Intent(this, TrackListActivity.class));
       break;
     }
     return super.onOptionsItemSelected(item);
   }
 
   @Override
   public void onNewIntent(Intent newIntent) {
     super.onNewIntent(newIntent);
     resume(newIntent);
   }
 
   @Override
   public void onResume() {
     super.onResume();
   }
 
   private void resume(Intent intent) {
     ArrayList<String> dates = intent.getStringArrayListExtra(TrackListActivity.TRACK_LIST_VIEW);
     if (null != dates) {
       viewTrack(dates);
     } else {
       int id = intent.getIntExtra(MapViewActivity.COMMAND, -1);
       if (-1 != id) {
         command(id);
       }
     }
     Dashboard.updateButtons(this);
   }
 
   private void viewTrack(ArrayList<String> dates) {
     View buttons = findViewById(R.id.buttons);
     if (null != buttons) buttons.setVisibility(View.GONE);
     TrackDbAdapter dbHelper = new TrackDbAdapter(this);
     dbHelper.open();
     for (String date : dates) {
       Cursor cursor = dbHelper.selectTrackRecords(date);
       if (null != cursor) {
 
         PolylineOverlay mapOverlay = new PolylineOverlay();
 
         int latIndex = cursor.getColumnIndex(TrackDbAdapter.LAT);
         int lonIndex = cursor.getColumnIndex(TrackDbAdapter.LON);
         mapController.setZoom(16);
         cursor.moveToFirst();
         while (!cursor.isAfterLast()) {
           GeoPoint point = new GeoPoint((int) (cursor.getDouble(latIndex) * 1E6),
               (int) (cursor.getDouble(lonIndex) * 1E6));
           mapOverlay.addPoint(point);
           if (!cursor.moveToNext()) {
             mapController.animateTo(point);
           }
         }
         cursor.close();
 
         List<Overlay> listOfOverlays = mapView.getOverlays();
         listOfOverlays.clear();
         listOfOverlays.add(mapOverlay);
 
         mapView.invalidate();
       }
     }
     dbHelper.close();
   }
 
   @Override
   protected boolean isRouteDisplayed() {
     // TODO Auto-generated method stub
     return false;
   }
 
   public class PolylineOverlay extends Overlay {
     private List<GeoPoint> polyline; // Contains set of points to be connected.
     private Paint pathPaint = null; // Paint tool that is used to draw on the
                                     // map canvas.
 
     public PolylineOverlay() {
       super();
       polyline = new ArrayList<GeoPoint>();
       pathPaint = new Paint();
       pathPaint.setAntiAlias(true);
     }
 
     public void addPoint(GeoPoint point) {
       polyline.add(point);
     }
 
     /**
      * Draws the poly line route on the map the this overlay belongs to.
      */
     @Override
     public void draw(Canvas canvas, MapView mView, boolean shadow) {
       super.draw(canvas, mView, shadow);
 
       // Reset our paint.
       pathPaint.setStrokeWidth(4);
       pathPaint.setARGB(100, 113, 105, 252);
       pathPaint.setStyle(Paint.Style.STROKE);
 
       Projection projection = mView.getProjection();
       Path routePath = new Path();
 
       // Add each point to the routePath.
       boolean moveTo = true;
       for (GeoPoint inPoint : polyline) {
         Point outPoint = projection.toPixels(inPoint, null);
         if (moveTo)
           routePath.moveTo(outPoint.x, outPoint.y);
         else
           routePath.lineTo(outPoint.x, outPoint.y);
         moveTo = false;
       }
       canvas.drawPath(routePath, pathPaint);
     }
   }
 
   class MapOverlay extends Overlay {
     private List<GeoPoint> points = new ArrayList<GeoPoint>();
 
     @Override
     public boolean draw(Canvas canvas, MapView mapView, boolean shadow, long when) {
       super.draw(canvas, mapView, shadow);
      Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.red);
 
       for (GeoPoint point : points) {
         // convert point to pixels
         Point screenPts = new Point();
         mapView.getProjection().toPixels(point, screenPts);
 
         // add marker
         canvas.drawBitmap(bmp, screenPts.x, screenPts.y - 24, null); // 24
         // is
         // the
         // height of
         // image
       }
       return true;
     }
 
     public void addPoint(GeoPoint point) {
       points.add(point);
     }
   }
 
   private MapController mapController;
   private MapView mapView;
 
   public void LocationChange(Location location) {
     LocationChange(location.getLatitude(), location.getLongitude());
   }
 
   private void LocationChange(double latitude, double longitude) {
     GeoPoint point = TrackService.geoPoint(latitude, longitude);
     mapController.animateTo(point);
     mapController.setZoom(16);
 
     // add marker
     MapOverlay mapOverlay = new MapOverlay();
     mapOverlay.addPoint(point);
     List<Overlay> listOfOverlays = mapView.getOverlays();
     listOfOverlays.clear();
     listOfOverlays.add(mapOverlay);
 
     String address = TrackService.ConvertPointToLocation(getBaseContext(), point);
     Toast.makeText(getBaseContext(), address, Toast.LENGTH_SHORT).show();
 
     mapView.invalidate();
   }
 
   public void Bound(Intent intent) {
     resume(intent);
   }
 }
