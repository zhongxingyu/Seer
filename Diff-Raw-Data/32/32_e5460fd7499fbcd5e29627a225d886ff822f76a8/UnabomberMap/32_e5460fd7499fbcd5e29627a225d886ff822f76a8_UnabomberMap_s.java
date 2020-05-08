 package ui;
 
 import unabomber.client.R;
 import android.content.Context;
 import android.os.Bundle;
 import android.telephony.TelephonyManager;
 
 import com.google.android.maps.MapActivity;
 import com.google.android.maps.MapView;
 
 import engine.GameEngine;
 
 public class UnabomberMap extends MapActivity {
     private MapView mapView;
 	private PlayerLocationOverlay playerLocationOverlay;
 
 	@Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         
         setUpMap();
         showPlayerLocation();
         authenticatePlayer();
     }
 
	protected void authenticatePlayer() {
		TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		new GameEngine(telephonyManager.getDeviceId()).authenticate();
 	}
 
 	private void showPlayerLocation() {
 		playerLocationOverlay = new PlayerLocationOverlay(this, mapView);
         playerLocationOverlay.enableMyLocation();
         mapView.getOverlays().add(playerLocationOverlay);
 	}
 
 	private void setUpMap() {
 		mapView = (MapView)findViewById(R.id.mapview);
         mapView.setBuiltInZoomControls(true);
 	}
 
 	@Override
 	protected boolean isRouteDisplayed() {
 		return false;
 	}
 }
