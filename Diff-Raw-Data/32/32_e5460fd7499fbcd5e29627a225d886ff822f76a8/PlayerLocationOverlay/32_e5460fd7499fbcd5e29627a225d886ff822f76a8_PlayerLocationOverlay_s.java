 package ui;
 
import android.content.Context;
 import android.location.Location;
 
 import com.google.android.maps.MapView;
 import com.google.android.maps.MyLocationOverlay;
 
 import engine.GameEngine;
 
 public class PlayerLocationOverlay extends MyLocationOverlay {
 	private GameEngine engine;
 
	public PlayerLocationOverlay(Context context, MapView mapView) {
 		super(context, mapView);
		engine = new GameEngine("DEVICE--ID");
 	}
 
 	@Override
 	public synchronized void onLocationChanged(Location location) {
 		super.onLocationChanged(location);
 		engine.sendLocation(location);
 	}
 }
