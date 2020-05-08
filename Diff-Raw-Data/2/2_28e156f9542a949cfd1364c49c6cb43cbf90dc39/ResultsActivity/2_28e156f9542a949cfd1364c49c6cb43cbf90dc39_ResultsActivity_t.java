 package com.HuskySoft.metrobike.ui;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.util.DisplayMetrics;
 import android.util.Log;
 import android.view.Display;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.LinearLayout;
 import android.widget.LinearLayout.LayoutParams;
 import android.widget.Toast;
 
 import com.HuskySoft.metrobike.R;
 import com.HuskySoft.metrobike.backend.Leg;
 import com.HuskySoft.metrobike.backend.Location;
 import com.HuskySoft.metrobike.backend.Route;
 import com.HuskySoft.metrobike.backend.Step;
 import com.HuskySoft.metrobike.backend.TravelMode;
 import com.HuskySoft.metrobike.ui.utility.MapSetting;
 import com.google.android.gms.maps.CameraUpdateFactory;
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.MapFragment;
 import com.google.android.gms.maps.model.BitmapDescriptorFactory;
 import com.google.android.gms.maps.model.CircleOptions;
 import com.google.android.gms.maps.model.LatLng;
 import com.google.android.gms.maps.model.MarkerOptions;
 import com.google.android.gms.maps.model.PolylineOptions;
 
 /**
  * This class shows the result and display it onto the screen.
  * @author mengwan, Sam Wilson, Shuo Wang
  * 
  */
 public class ResultsActivity extends Activity {
 
     /**
      * The tag of this activity.
      */
     private static final String TAG = "ResultActivity";
     
     /**
      * duration of the animated camera in the map.
      */
     private static final int ANIMATED_CAMERA_DURATION_IN_MILLISECOND = 3000;
 
     /**
      * GoogleMap object stored here to be modified.
      */
     private GoogleMap mMap;
 
     /**
      * Results from the search.
      */
     private ArrayList<Route> routes;
 
     /**
      * Current route that should be displayed on the map.
      */
     private int currRoute = -1;
     
     /**
      * List of all the buttons used for selecting different routes.
      */
     private ArrayList<Button> buttons;
 
     /**
      * Highlight color of the button text.
      */
     private static final int BUTTON_TEXT_COLOR_HIGHLIGTH = -16711936;
     
     /**
      * Toast object in this page.
      */
     private Toast currToast;
     
     /**
      * Current Device's screen height.
      */
     private float dpHeight;
     
     /**
      * Current Device's screen width.
      */
     private float dpWidth;
     
     /**
      * Bitmap object used for drawing icon of the marker.
      */
     private Bitmap bitmap;
     
     /**
      * Map circle radius.
      */
     private static final int MAP_CIRCLE_RADIUS = 4;
     
     /**
      * Stroke width in the map.
      */
     private static final int MAP_STROKE_WIDTH = 3;
     
     /**
      * Transparent rate of the poly-line.
      */
     private static final int POLYLINE_TRANSPARENT = 200;
     
     /**
      * poly-line color integer.
      */
     private static final int POLYLINE_COLOR = 255;
     
     /**
      * Thick poly-line width.
      */
     private static final float POLYLINE_THICK = 12f;
     
     /**
      * Thin poly-line width.
      */
     private static final float POLYLINE_THIN = 8f;
     
     /**
      * The margin number.
      */
     private static final int MARGIN = 3;
     
     /**
      * The setShadowLayer.
      */
     private static final float LAYER = 0.6f;
 
     /**
      * {@inheritDoc}
      * 
      * @see android.app.Activity#onCreate(android.os.Bundle)
      */
     @Override
     protected final void onCreate(final Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         
         Display display = getWindowManager().getDefaultDisplay();
         DisplayMetrics outMetrics = new DisplayMetrics();
         display.getMetrics(outMetrics);
 
         float density  = getResources().getDisplayMetrics().density;
         dpHeight = outMetrics.heightPixels / density;
         dpWidth = outMetrics.widthPixels / density;
         // get the solution from the search activity
         @SuppressWarnings("unchecked")
         List<Route> recievedRoutes = (ArrayList<Route>) getIntent().getSerializableExtra(
                 "List of Routes");
 
         // set the default route to be the first route of the solution
 
         setContentView(R.layout.activity_results);
         if (recievedRoutes != null) {
             routes = (ArrayList<Route>) recievedRoutes;
             currRoute = (Integer) getIntent().getSerializableExtra("Current Route Index");
             addRouteButtons();
             mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
         }
         Log.v(TAG, "Done on create");
     }
 
     /**
      * Show the menu bar when the setting button is clicked.
      * 
      * @param menu
      *            The options menu in which you place your items.
      * @return true if the menu to be displayed.
      * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
      */
     @Override
     public final boolean onCreateOptionsMenu(final Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.activity_menu, menu);
         return true;
     }
 
     /**
      * this method will be called when user click buttons in the setting menu.
      * 
      * @param item
      *            the menu item that user will click
      * @return true if user select an item
      */
     @Override
     public final boolean onOptionsItemSelected(final MenuItem item) {
         switch (item.getItemId()) {
         case R.id.action_search:
             // user click the search button, start the search activity
             Intent searchIntent = new Intent(this, SearchActivity.class);
             searchIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
             startActivity(searchIntent);
             return true;
         case R.id.action_settings:
             // user click the setting button, start the settings activity
             Intent settingsIntent = new Intent(this, SettingsActivity.class);
             startActivity(settingsIntent);
             return true;
         default:
             return super.onOptionsItemSelected(item);
         }
     }
 
     /**
      * Go to the Navigate Page.
      * 
      * @param view
      *            : the view of the button onClick function of the go to
      *            Navigate button
      */
     public final void goToNavigate(final View view) {
         // Do something in response to button
         Intent intent = new Intent(this, NavigateActivity.class);
         intent.putExtra("List of Routes", (Serializable) routes);
         intent.putExtra("Current Route Index", currRoute);
         startActivity(intent);
     }
 
     /**
      * Direct to search page.
      * 
      * @param view
      *            the view of the button onClick function of the return to
      *            search page button
      */
     public final void goToSearchPage(final View view) {
         // Do something in response to button
         Intent intent = new Intent(this, SearchActivity.class);
         intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
         startActivity(intent);
     }
 
     /**
      * Direct to detail page.
      * 
      * @param view
      *            : the view of the button onClick function of the go to details
      *            button
      */
     public final void goToDetail(final View view) {
         // Do something in response to button
         Intent intent = new Intent(this, DetailsActivity.class);
         intent.putExtra("List of Routes", (Serializable) routes);
         intent.putExtra("Current Route Index", currRoute);
         startActivity(intent);
     }
 
     /**
      * add the selecting route buttons to the scroll bar on left.
      */
     private void addRouteButtons() {
         buttons = new ArrayList<Button>();
         String white = "#000000";
         LinearLayout main = (LinearLayout) findViewById(R.id.linearLayoutForRouteSelection);
         for (int i = 0; i < routes.size(); i++) {
             Button selectRouteBtn = new Button(this);         
             selectRouteBtn.setBackgroundResource(R.drawable.custom_btn_shakespeare);
             selectRouteBtn.setText(ResultsActivity.this.getResources().
                     getString(R.string.route_button) + (i + 1));
             LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT,
                     LayoutParams.WRAP_CONTENT);
             params.setMargins(MARGIN, MARGIN, MARGIN, MARGIN);
             selectRouteBtn.setLayoutParams(params);                  
             selectRouteBtn.setPadding(0, 0, 0, 0);
             selectRouteBtn.setShadowLayer(LAYER, 1, 1, Color.parseColor(white));
             selectRouteBtn.setOnClickListener(new MyOnClickListener(i));
             main.addView(selectRouteBtn);
             buttons.add(selectRouteBtn);
         }
     }
 
     /**
      * Implemented onClickListener in order to passed in parameter.
      * 
      * @author mengwan
      * 
      */
     private class MyOnClickListener implements OnClickListener {
         /**
          * stores the routeNumber gets passed in.
          */
         private int routeNumber;
 
         /**
          * Create a MyOnClickListener object.
          * 
          * @param routeSelectionNumber
          *            Route number that is passed in.
          */
         public MyOnClickListener(final int routeSelectionNumber) {
             this.routeNumber = routeSelectionNumber;
         }
 
         @Override
         public void onClick(final View v) {
             currRoute = routeNumber;
             drawRoute();
         }
     };
 
     /**
      * draw the current route on the map.
      */
     private void drawRoute() {
         
         if (currRoute >= 0 && currRoute < routes.size()) {
             //clear the map drawing first
             mMap.clear();
             
             //disable the buttons before the drawing taking place
             for (Button button : buttons) {
                 button.setTextColor(Color.WHITE);
                 button.setEnabled(false);
             }
             
             buttons.get(currRoute).setTextColor(BUTTON_TEXT_COLOR_HIGHLIGTH);            
             
             drawRoutesAndMarkers();
             
             if (currToast != null) {
                 currToast.cancel();
             }
             String prettyDuration = 
                     com.HuskySoft.metrobike.ui.utility.Utility.secondsToHumanReadableDuration(
                             routes.get(currRoute).getDurationInSeconds());
             currToast = Toast.makeText(getApplicationContext(), 
                     ResultsActivity.this.getResources().getString(R.string.route_length)
                    + " " + routes.get(currRoute).getDistanceInMeters() 
                     + ResultsActivity.this.getResources().getString(R.string.meters_duration)
                     + " " + prettyDuration, Toast.LENGTH_LONG);
             currToast.show();
             
             //set the camera to focus on the route
             mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                     com.HuskySoft.metrobike.ui.utility.Utility.
                     getCameraCenter(routes.get(currRoute)), 
                     com.HuskySoft.metrobike.ui.utility.Utility.
                     getCameraZoomLevel(routes.get(currRoute), dpHeight, dpWidth)),
                     ANIMATED_CAMERA_DURATION_IN_MILLISECOND, null);   
             
             //enable the buttons after the drawing is done
             for (Button button : buttons) {
                 button.setEnabled(true);
             }
         }
         Log.v(TAG, "Done drawing the route");
     }
     
     /**
      * draw the routes and markers on the map.
      */
     private void drawRoutesAndMarkers() {
       //get the source and destination
         List<Leg> legs = routes.get(currRoute).getLegList();
         Location start = legs.get(0).getStartLocation();
         Location end = legs.get(legs.size() - 1).getStepList()
                 .get(legs.get(legs.size() - 1).getStepList().size() - 1).getEndLocation();
 
         //draw Markers for starting and ending points
         mMap.addMarker(new MarkerOptions()
                 .position(com.HuskySoft.metrobike.ui.utility.Utility.convertLocation(start))
                 .title(ResultsActivity.this.getResources().getString(R.string.start_here))
                 .icon(BitmapDescriptorFactory.fromResource(R.drawable.starting)));
 
         mMap.addMarker(new MarkerOptions()
                 .position(com.HuskySoft.metrobike.ui.utility.Utility.
                         convertLocation(end)).title(
                                 ResultsActivity.this.getResources().getString(R.string.end_here))
                 .icon(BitmapDescriptorFactory.fromResource(R.drawable.ending)));
         
         mMap.addCircle(new CircleOptions()
             .center(com.HuskySoft.metrobike.ui.utility.Utility.convertLocation(start))
             .radius(MAP_CIRCLE_RADIUS)
             .strokeColor(Color.BLACK).strokeWidth(MAP_STROKE_WIDTH)
             .fillColor(Color.WHITE).zIndex(2));
 
         //draw Poly-line on the map
         drawPolyLine(legs);
         Log.v(TAG, "Done drawing the legs");
     }
 
     /**
      * Draw Poly lines on the map.
      * 
      * @param legs
      *            each step on the route.
      */
     private void drawPolyLine(final List<Leg> legs) {
         for (Leg l : legs) {
             for (Step s : l.getStepList()) {
                 PolylineOptions polylineOptions = new PolylineOptions();
                 for (LatLng ll : com.HuskySoft.metrobike.ui.utility.Utility.
                         convertLocationList(s.getPolyLinePoints())) {
                     polylineOptions = polylineOptions.add(ll);
                 }
                 
                 if (s.getTravelMode() == TravelMode.TRANSIT) {
                     boolean getIcon = true;
                     Thread markerThread = new Thread(new MarkerThread(s));
                     markerThread.start();                    
                     try {
                         markerThread.join();
                     } catch (InterruptedException e) {
                         getIcon = false;
                     }
                     MarkerOptions mo = new MarkerOptions()
                     .position(com.HuskySoft.metrobike.ui.utility.Utility
                             .convertLocation(s.getStartLocation()))
                     .title(s.getTransitDetails().getVehicleType() 
                             + " " + s.getTransitDetails().getLineShortName())
                             .snippet(ResultsActivity.this.getResources().
                                     getString(R.string.departure_at) 
                                     + s.getTransitDetails().getDepartureTime());
                     if (getIcon && bitmap != null) {
                         mo = mo.icon(BitmapDescriptorFactory.fromBitmap(bitmap));
                     }
                     mMap.addMarker(mo);
                     mMap.addPolyline(polylineOptions.
                             color(Color.argb(POLYLINE_TRANSPARENT, POLYLINE_COLOR, 0, 0))
                             .width(POLYLINE_THICK));
                 } else {
                     mMap.addPolyline(polylineOptions.
                             color(Color.argb(POLYLINE_TRANSPARENT, 0, POLYLINE_COLOR, 0))
                             .width(POLYLINE_THIN).zIndex(1));
                 }
                 
                 mMap.addCircle(new CircleOptions()
                 .center(com.HuskySoft.metrobike.ui.utility.Utility.
                         convertLocation(s.getEndLocation()))
                 .radius(MAP_CIRCLE_RADIUS)
                 .strokeColor(Color.BLACK).strokeWidth(MAP_STROKE_WIDTH)
                 .fillColor(Color.WHITE).zIndex(2));
             }
         }
     }
     
     /**
      * Update the map setting.
      * 
      * @see android.app.Activity#onResume()
      */
     @Override
     protected final void onResume() {
         super.onResume();
         MapSetting.updateStatus(mMap);
         drawRoute();
         Log.v(TAG, "Done on resume");
     }
     
     /**
      * An inner class that generates a request for drawing the icon of the marker
      * for a URL connection.
      * 
      * @author mengwan
      */
     private class MarkerThread implements Runnable {
         /**
          * Step object to be drew on the map.
          */
         private Step toDraw;
         
         /**
          * Constructor of the thread.
          * @param s : Step object to be drew.
          */
         public MarkerThread(final Step s) {
             this.toDraw = s;
         }
         
         /**
          * {@inheritDoc}
          */
         @Override
         public void run() {
             bitmap = com.HuskySoft.metrobike.ui.utility.Utility.
                     getBitmapFromURL(toDraw.getTransitDetails().getVehicleIconURL());
         }
     }
 }
