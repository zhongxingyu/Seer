 package com.scotty.games.missionmission;
 
 import java.util.List;
 import java.util.Random;
 
 import android.content.Intent;
import android.content.res.Configuration;
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.os.CountDownTimer;
 import android.os.Vibrator;
 import android.view.View;
 import android.widget.ImageButton;
 import android.widget.TextView;
 
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapActivity;
 import com.google.android.maps.MapController;
 import com.google.android.maps.MapView;
 import com.google.android.maps.Overlay;
 import com.google.android.maps.OverlayItem;
 
 
 
 public class MissionMission extends MapActivity {    
     private MapView mapView;
     private List<Overlay> mapOverlays;
     private Drawable drawable;
     private PlayerLocationOverlay playerLocationOverlay;
     private MissionItemizedOverlay homeMarkerOverlay;
     private MissionItemizedOverlay teamMarkerOverlay;
     private MissionItemizedOverlay opponentMarkerOverlay;
     
     private CountDownTimer timer;
     private Vibrator vibrate;
     private MapController mapController;
     private WebMessenger messenger;
     private Random random;
 
     private ImageButton cameraViewButton;
     private TextView gameData;
     private TextView debugText;
     private String time = "Calculating Time...";
     private String buffer = "\n\n\n";
     private String debug = "DEBUG";
 
     private int teamNumber = 0;
     private int evidenceFound = 0;
     private float area = 0;
     private long millisLeft = 0;
     private long minutes = 20;
     private long seconds = 0;
     private Intent myIntent = null;
     private GeoPoint playerLocation = null;
     
     private static final int MILLIS_PER_MINUTE = 60000;
     private static final int CAMERA_ACTION = 0;
     
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         
         mapView = (MapView) findViewById(R.id.mapview);
         mapView.setBuiltInZoomControls(true);
         
         mapOverlays = mapView.getOverlays();
         drawable = this.getResources().getDrawable(R.drawable.arrow_blue);
         teamMarkerOverlay = new MissionItemizedOverlay(drawable, this);
         mapOverlays.add(teamMarkerOverlay);
         
         //Set up the printing for the game data.
         gameData  = (TextView) findViewById(R.id.gamedata);
         debugText = (TextView) findViewById(R.id.debugtext);
         debugText.setText(buffer + debug);
         
         //Set up the messenger to send clues to the server.
         random = new Random();
         teamNumber = random.nextInt(1000);
         messenger = new WebMessenger(29);//teamNumber);
         
         //Setup the map controller.
         mapController = mapView.getController();
         
         //Setup a player location listener.
         playerLocationOverlay = new PlayerLocationOverlay(this, mapView);
         mapOverlays.add(playerLocationOverlay);
         playerLocationOverlay.enableCompass();
         playerLocationOverlay.enableMyLocation();
         playerLocationOverlay.runOnFirstFix(new Runnable() {
             public void run() {
                 //For debugging, Cory Hall's coordinates are:
                 //Longitude = -122.257976, Latitude = 37.875344
                 playerLocation = playerLocationOverlay.getMyLocation();
                 mapController.animateTo(playerLocation);
                 mapController.setZoom(18);
             }
         });
         
         //The Camera View swap button.
         cameraViewButton = (ImageButton) findViewById(R.id.cameraViewButton);
         cameraViewButton.setOnClickListener(new MapView.OnClickListener() {
         	@Override
 			public void onClick(View v) {
 			    if (millisLeft == 0) {
 			        return;
 			    }
 			    
 			    myIntent = new Intent(
 			        mapView.getContext(), 
 			        com.scotty.games.missionmission.CameraPreview.class);
                 startActivityForResult(myIntent, CAMERA_ACTION);
 			}
         });
         
         //Set up the game timer.
         long gameLengthInMinutes = 20;
        long gameLength = gameLengthInMinutes * MILLIS_PER_MINUTE;        
         timer = new CountDownTimer(gameLength, 1000) {
             public void onTick(long millisUntilFinished) {
                 millisLeft = millisUntilFinished / 1000;
                 minutes = millisLeft / 60;
                 seconds = millisLeft % 60;
                 time = "Time Left: " + minutes + ":" + 
                     ((seconds < 10) ? "0" + seconds : seconds);
                 
                 updateGameStats();
             }
             
             public void onFinish() {
                 time = "Time's up!";
                 millisLeft = 0;
                 
                vibrate = (Vibrator) getSystemService(mapView.getContext().VIBRATOR_SERVICE);
                 long rumblePattern[] = 
                     new long[] {250, 500, 250, 500, 250, 500, 250, 500, 250, 500};
                 vibrate.vibrate(rumblePattern, -1);
                 
                 updateGameStats();
             }
         }.start();
         
         //Plot some test points.
         //plotTestPoints();
     }
     
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
     
     @Override
     protected boolean isRouteDisplayed() {
         return false;
     }
     
    //@Override
    //public Object onRetainNonConfigurationInstance() {
    //    return(null);
    //}
    
     /**
      * Called when the map becomes the focus again after going off to
      * some other game feature like the camera.
      */
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
          if (requestCode == CAMERA_ACTION) { 
              
              if (resultCode == RESULT_OK) {
                  //Acquire the player's current location, move the game 
                  //camera and set a marker there, and send a message off 
                  //to the server.
                  
                  playerLocation = playerLocationOverlay.getMyLocation();
                  if (playerLocation == null) {
                      debugText.setText(buffer + "ERROR: Couldn't locate player.");
                      return;
                  }
                  mapController.animateTo(playerLocation);
                  
                  GeoPolyPoint currentLocation = 
                     new GeoPolyPoint(
                         playerLocation.getLatitudeE6(), 
                         playerLocation.getLongitudeE6());
                  placeMarker(currentLocation);
                  messenger.sendClue(currentLocation, "Clue #" + evidenceFound);
                  
                  debugText.setText(buffer + "Claimed!");
              } else {
                  debugText.setText(buffer + "Canceled");
              }
          }
     }
     
     @Override
     protected void onResume() {
     	super.onResume();
     }
     
     private void placeMarker(GeoPolyPoint point) {
         evidenceFound++;
         OverlayItem marker = new OverlayItem(point, 
             "Clue " + evidenceFound, 
             "Longitude = " + 
             (((float)point.getLongitudeE6())/1000000.0) + 
             "\nLatitude = " + 
             (((float)point.getLatitudeE6())/1000000.0));
         
         teamMarkerOverlay.addOverlay(marker);
         area = teamMarkerOverlay.getArea() * 1000000.0f;
     }
     
     public MapView getView() {
         return mapView;
     }
     
     public void updateGameStats() {
     	gameData.setText(time + "\nClues: " + evidenceFound +
     	    " pieces\nArea: " + area + " units");
     }
     
     /**
      * Plots some test markers down in the Mission District
      * of San Francisco.
      */
     public void plotTestPoints() {
         placeMarker(new GeoPolyPoint(37761432, -122417135));
         placeMarker(new GeoPolyPoint(37761788, -122419260));
         placeMarker(new GeoPolyPoint(37758565, -122419002));
         placeMarker(new GeoPolyPoint(37759000, -122418000));
         placeMarker(new GeoPolyPoint(37760000, -122418000));
         placeMarker(new GeoPolyPoint(37760000, -122421000));
     }
 }
