 package se.chalmers.project14.main;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.graphics.drawable.Drawable;
 import android.view.MotionEvent;
 import android.widget.Toast;
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.ItemizedOverlay;
 import com.google.android.maps.MapView;
 import com.google.android.maps.Overlay;
 import com.google.android.maps.OverlayItem;
 
 public class TouchOverlay extends Overlay {
 	//private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();
 	private Context context;
 	private long touchStart, touchStop;
 	private float touchStartX = 1, touchStartY = 2, touchStopX = 3,
 			touchStopY = 4;
 	private MapView mapView;
 	private GeoPoint geoPoint;
 	private DestinationMarkerOverlay destOverlay;
 
 	public TouchOverlay(Context context, MapView mapView) {
 		super();// Fixing so that the flag is pointed
 		// to the lower left corner
 		this.context = context;
 		this.mapView=mapView;
 	}
 
 	@SuppressWarnings("deprecation")
 	@Override
 	public boolean onTouchEvent(MotionEvent event, MapView m) {
 		// when user touches the screen
 		if (event.getAction() == MotionEvent.ACTION_DOWN) {
 			touchStart = event.getEventTime();
 			touchStartX = event.getX();
 			touchStartY = event.getY();
 		}// when screen is released
 		else if (event.getAction() == MotionEvent.ACTION_UP) {
 			touchStop = event.getEventTime();
 			touchStopX = event.getX();
 			touchStopY = event.getY();
 
 			/*
 			 * Checking holdtime to be above 1000 ms and at he same position
 			 */
 			if (touchStop - touchStart > 1000 && touchStartX <= touchStopX+20 &&touchStartX >= touchStopX-20 
 					&& touchStartY <= touchStopY+20 && touchStartY >= touchStopY-20) {
 				geoPoint = mapView.getProjection().fromPixels((int)touchStopX, (int)touchStopY);
 				AlertDialog options = new AlertDialog.Builder(context).create();
 				options.setTitle("Options");
 				options.setMessage("Coordinates:\nLatitude: " + geoPoint.getLatitudeE6()/1E6 + "\nLongitude: " 
 						+ geoPoint.getLongitudeE6()/1E6 + "\n\nWhat do you want to do?");
 				options.setButton("Set destination", new DialogInterface.OnClickListener(){
 					public void onClick(DialogInterface dialog, int which) {						
 						//TODO Add Possibility to add a destination marker
 						Drawable destFlag = mapView.getResources().getDrawable(R.drawable.destination_flag);
 						destOverlay = new DestinationMarkerOverlay(destFlag);
 						OverlayItem overlayitem = new OverlayItem((GeoPoint)geoPoint, "Hola, Mundo!", "I'm in Mexico City!");
 				        destOverlay.addOverlay(overlayitem);
 				        List<Overlay> destinationOverlays = mapView.getOverlays();
 				        destinationOverlays.add(destOverlay);
 						//OverlayItem overlayitem = new OverlayItem(new
 						//GeoPoint((int)touchStopX*1E6, (int)touchStopY*1E6), "Hola, Mundo!",
 						//"I'm in Mexico City!");
 					}
 				});
 				options.setButton2("Back to Map", new DialogInterface.OnClickListener(){
 					public void onClick(DialogInterface dialog, int which) {
 						Toast.makeText(context, "Test2", Toast.LENGTH_SHORT).show();
 					}
 				});
 				options.show();
 				return true;
 			}
 		}
 		return false;
 	}
 	DestinationMarkerOverlay getDestOverlay(){
 		return destOverlay;
 	}
 }
