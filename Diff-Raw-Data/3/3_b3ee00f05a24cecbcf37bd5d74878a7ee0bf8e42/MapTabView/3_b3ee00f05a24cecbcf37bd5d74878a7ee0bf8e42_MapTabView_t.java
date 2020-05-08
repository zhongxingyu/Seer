 package ch.ulrichard.flightpred;
 
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeMap;
 
 import com.google.android.maps.MapActivity;
 import com.google.android.maps.MapController;
 import com.google.android.maps.MapView;
 import com.google.android.maps.MyLocationOverlay;
 import com.google.android.maps.Overlay;
 import com.google.android.maps.OverlayItem;
 import com.google.android.maps.GeoPoint;
 
 
 import android.graphics.Paint;
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.widget.TextView;
 
 public class MapTabView extends MapActivity {
 
 	@Override
 	protected void onCreate(Bundle icicle) {
 		super.onCreate(icicle);
 		setContentView(R.layout.maptabview);
 		GeoPoint mapcenter = new GeoPoint(46362093, 9036255);
 		
 		MapView mapv = (MapView) findViewById(R.id.mapview);
 		mapv.setBuiltInZoomControls(true);
 		
 		final MyLocationOverlay myLocation = new MyLocationOverlay(this, mapv);
 		mapv.getOverlays().add(myLocation);
 		myLocation.enableMyLocation();
 		myLocation.runOnFirstFix(new Runnable() {
 		    public void run() {
 		    	GeoPoint mapcent = myLocation.getMyLocation();
 		    }
 		});
 		
 		MapController mc = mapv.getController();
 		mc.animateTo(mapcenter);
 		mc.setZoom(8);
 		
 		loadPredData();
 	 }
 	
 	@Override
 	protected boolean isRouteDisplayed() {
 		return false;
 	}
 	
 	private void loadPredData() {
 		try {
//			XmlHandler xmlh = XmlHandler.inst("");
			JsonHandler xmlh = JsonHandler.inst("");
 	    	xmlh.load();
 	    	TreeMap<String, TreeMap<Date, Float>> preddata = xmlh.getPredData();
 	    	MapView mapv = (MapView)findViewById(R.id.mapview);
 	    	List<Overlay> mapOverlays = mapv.getOverlays();
 	    	
 	    	TreeMap<Date, Paint> preddates = new TreeMap<Date, Paint>();
 	    	Paint pntRed = new Paint();
 	    	int alpha = 100;
 			pntRed.setARGB(alpha, 255, 0, 0);
 			pntRed.setStrokeWidth(3);
 			Paint pntGreen = new Paint();
 			pntGreen.setARGB(alpha, 0, 255, 0);
 			pntGreen.setStrokeWidth(3);
 			Paint pntBlue = new Paint();
 			pntBlue.setARGB(alpha, 0, 0, 255);
 			pntBlue.setStrokeWidth(3);
 			Date day = new Date();
 			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
 			String todaysdtr = sdf.format(day);
 			day = sdf.parse(todaysdtr);
 			preddates.put(day, pntRed);
 			Calendar cal = Calendar.getInstance();
 			cal.setTime(day);
 			cal.add(Calendar.DATE, 1);
 			preddates.put(cal.getTime(), pntGreen);
 			cal.add(Calendar.DATE, 1);
 			preddates.put(cal.getTime(), pntBlue);
 			
 			Drawable sigma = this.getResources().getDrawable(R.drawable.sigma);
 			MapCircleOverlay itemizedoverlay = new MapCircleOverlay(sigma);
 			
 			for(Map.Entry<String, TreeMap<Date, Float>> ent : preddata.entrySet()) {
 				String sitename = ent.getKey();
 				Set<Date> days = ent.getValue().keySet();
 				GeoPoint location = xmlh.getLocation(sitename);
 				
 				OverlayItem overlayitem = new OverlayItem(location, sitename, sitename);
 				itemizedoverlay.addOverlay(overlayitem);
 				mapOverlays.add(itemizedoverlay);
 				
 				for(Map.Entry<Date, Paint> ent2 : preddates.entrySet()) {
 					day = ent2.getKey();
 					if(days.contains(day)) {
 						CircleOverlay circov = new CircleOverlay(location, ent.getValue().get(day), ent2.getValue());
 						mapOverlays.add(circov);
 					}
 				}
 			}
 		} catch(Exception e) {
 			throw new RuntimeException(e);		
 		}
 	}
 
 }
