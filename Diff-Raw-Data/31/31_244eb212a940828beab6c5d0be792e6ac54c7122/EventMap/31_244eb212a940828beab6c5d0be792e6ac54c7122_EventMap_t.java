 package api.festival;
 
import java.util.List;

import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapActivity;
 import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;
 
 public class EventMap extends MapActivity{
 	
 	public void onCreate(Bundle savedInstanceState) {
 	    super.onCreate(savedInstanceState);
 	    setContentView(R.layout.map);
 	    MapView mapView = (MapView) findViewById(R.id.mapview);
 	    mapView.setBuiltInZoomControls(true);
 	    
	    float longitude = (Float) getIntent().getSerializableExtra("longitude");
	    float latitude = (Float) getIntent().getSerializableExtra("latitude");
	    GeoPoint point = new GeoPoint(longitude, latitude);
	    
	    List<Overlay> mapOverlays = mapView.getOverlays();
	    Drawable drawable = this.getResources().getDrawable(R.drawable.map);
	    MapOverlay itemizedoverlay = new MapOverlay(drawable);
	    
	    OverlayItem overlayitem = new OverlayItem(point, "Hola, Mundo!", "I'm in Mexico City!");
 	    
	    itemizedoverlay.addOverlay(overlayitem);
	    mapOverlays.add(itemizedoverlay);
 	}
 	
 	protected boolean isRouteDisplayed() {
 		return false;
 	}
 }
