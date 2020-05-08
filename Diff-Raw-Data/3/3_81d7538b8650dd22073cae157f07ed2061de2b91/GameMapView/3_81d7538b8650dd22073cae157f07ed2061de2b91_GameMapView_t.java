 package com.ghostrun.activity;
 
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.util.List;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.drawable.Drawable;
 import android.location.Criteria;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.os.Handler;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.Button;
 
 import com.ghostrun.R;
 import com.ghostrun.controllers.GameLoop;
 import com.ghostrun.driving.NodeFactory;
 import com.ghostrun.overlays.MazeOverlay;
 import com.ghostrun.overlays.PlayerOverlay;
 import com.ghostrun.overlays.RobotsItemizedOverlay;
 import com.google.android.maps.MapActivity;
 import com.google.android.maps.MapView;
 import com.google.android.maps.MyLocationOverlay;
 import com.google.android.maps.Overlay;
 
 /** Map Activity for showing the status of a game in progress.
  */
 public class GameMapView extends MapActivity {
     MapView mapView;
     MyLocationOverlay locationOverlay;
     MazeOverlay mazeOverlay;
     Drawable defaultMarker;
     
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.mapview);
 
         mapView = (MapView) findViewById(R.id.mapview);
         mapView.setBuiltInZoomControls(true);
 
         List<Overlay> mapOverlays = mapView.getOverlays();
         mapOverlays.clear();
         
         GameLoop gameLoop = new GameLoop();
 
         // Add player overlay
         locationOverlay = new PlayerOverlay(this, mapView,
                 gameLoop.getPlayer());
         registerLocationUpdates(locationOverlay);
         mapOverlays.add(locationOverlay);
 
         // Add robot overlay
         Drawable robotIcon = this.getResources().getDrawable(
                 R.drawable.androidmarker);
         RobotsItemizedOverlay robotOverlay = new RobotsItemizedOverlay(
                     robotIcon, gameLoop.getRobots());
         mapOverlays.add(robotOverlay);
         
         // Start game loop
         Handler handler = new Handler();
         gameLoop.setRobotOverlay(robotOverlay);
         handler.post(gameLoop);
         
         // Stop the current activity and return to the previous view.
         Button logobutton=(Button)findViewById(R.id.mapview_paclogo);
         logobutton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 finish();
             }
         });
         
         this.defaultMarker = this.getResources().getDrawable(R.drawable.blue);
         this.mazeOverlay = null;
     }
 
     @Override
     public void onPause() {
         super.onPause();
         locationOverlay.disableMyLocation();
     }
 
     @Override
     public void onResume() {
         super.onResume();
         locationOverlay.enableMyLocation();
     }
 
     @Override
     protected boolean isRouteDisplayed() { return false; }
     
     ///////////////////////////////////////////////////////////////////
     //                     private methods
     ///////////////////////////////////////////////////////////////////
 
     private void registerLocationUpdates(LocationListener listener) {
         LocationManager locationManager = (LocationManager) getSystemService(
                 Activity.LOCATION_SERVICE);
         Criteria criteria = new Criteria();
         criteria.setAccuracy(Criteria.ACCURACY_FINE);
         criteria.setAltitudeRequired(true);
         String bestLocationProvider = locationManager.getBestProvider(
                 criteria, false);
         if (bestLocationProvider == null
                 || !locationManager.isProviderEnabled(bestLocationProvider)) {
             android.util.Log.d("registerLocationUpdates",
                     "Provider not available or not enabled");
             return;
         }
         locationManager.requestLocationUpdates(bestLocationProvider, 0, 0, listener);
     }
     
     // Menu will hold "Sound" button and "Map Selection" button.
     public boolean onCreateOptionsMenu(Menu menu) {
         super.onCreateOptionsMenu(menu);
         menu.add("Select Map");
         menu.add("Sound is on");
         menu.getItem(0).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
             @Override
             public boolean onMenuItemClick(MenuItem item) {
                 Intent i= new Intent(GameMapView.this, FileBrowserView.class);
                 startActivityForResult(i, 0);
                 return true;
             }
         });
         return true;
     }
     
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
     	super.onActivityResult(requestCode, resultCode, data);
    	if (data == null) {
    	    return;
    	}
     	String filename = data.getStringExtra("filename");
 		try {
 			FileReader input = new FileReader(filename);
 			BufferedReader bufRead = new BufferedReader(input);
 			String json = bufRead.readLine();
 			
 			System.out.println(json);
 			
 			NodeFactory factory = new NodeFactory();
 			NodeFactory.NodesAndRoutes nodesAndRoutes = factory.fromMap(json);
 			
 			System.out.println(nodesAndRoutes.nodes.size());
 			System.out.println(nodesAndRoutes.routesMap.size());
 			
 			if (mazeOverlay != null)
 				this.mapView.getOverlays().remove(mazeOverlay);
 			
 			mazeOverlay = new MazeOverlay(defaultMarker, nodesAndRoutes);
 			this.mapView.getOverlays().add(mazeOverlay);			
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} 
     	
     }
 }
