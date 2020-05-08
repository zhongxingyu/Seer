 package id.sabril.testingPA;
 
 import java.util.List;
 
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapView;
 import com.google.android.maps.OverlayItem;
 
 import android.content.Context;
 import android.graphics.drawable.Drawable;
 
 public class LokasiPuskesmas {
 	public Context mContext;
 	public GeoPoint point;
 	public OverlayItem overlayItem;
 	mapPuskesmasOverlay mapPuskesmas;
 	Context myContext;
 	MapView map;
 	List<Puskesmas> puskesmas;
 	public LokasiPuskesmas(Context context){
 		this.mContext = context;
 	}
 	
 	public void setPosisiPuskesmas(MapView mapView,String category,Double lat,Double lon){
 		this.map = mapView;
 		DatabaseHandler db = new DatabaseHandler(mContext);
 		if(category.length() == 0){
 			puskesmas = db.getAllPuskesmas("");
 		}
 		else{
 			puskesmas = db.getAllPuskesmas(category);
 		}
 			Drawable marker = mContext.getResources().getDrawable(R.drawable.marker_puskesmas);
 			int markerWidth = marker.getIntrinsicWidth();
 	        int markerHeight = marker.getIntrinsicHeight();
 	        marker.setBounds(0, markerHeight, markerWidth, 0);
 			mapPuskesmas = new mapPuskesmasOverlay(marker, mapView);
 			mapView.getOverlays().add(mapPuskesmas);
 			for (Puskesmas cn : puskesmas){
 	        	if(lat != null && lon != null){
 	        		String coordinates[] = {cn.getLatitude(),cn.getLongnitude()};
 		        	double latitude = Double.parseDouble(coordinates[0]);
 		        	double longnitude = Double.parseDouble(coordinates[1]);
 		        	point = new GeoPoint(
 		        			(int) (latitude * 1E6),
 		        			(int) (longnitude * 1E6)
 		        			);
 	        		if((latitude >= (lat - 0.02) && latitude <= (lat + 0.02)) && (longnitude >= (lon - 0.02) && longnitude <= (lon + 0.02))){
 		        		overlayItem = new OverlayItem(point, cn.getNamaPuskesmas(), cn.getAlamat());
		        		mapPuskesmas.addItem(overlayItem);  
 		        	}
 	        		else{
 	        			
 	        		}
 	        	}
 	        	 /*if((latitude >= -6.975199 && latitude <= -6.955199) && (longnitude >= 107.627908 && longnitude <= 107.647908)){
 	        		overlayItem = new OverlayItem(point, cn.getNamaPuskesmas(), cn.getAlamat());
 		        	mapPuskesmas.addItem(overlayItem); 
 	        	} */
 	        	else{
 	        		String coordinates[] = {cn.getLatitude(),cn.getLongnitude()};
 		        	double latitude = Double.parseDouble(coordinates[0]);
 		        	double longnitude = Double.parseDouble(coordinates[1]);
 		        	point = new GeoPoint(
 		        			(int) (latitude * 1E6),
 		        			(int) (longnitude * 1E6)
 		        			);
 	        		overlayItem = new OverlayItem(point, cn.getNamaPuskesmas(), cn.getAlamat()); 
 	        		mapPuskesmas.addItem(overlayItem); 
 	        	}
 			}
 			
 	}
 	
 	public void removePuskesmas(MapView mapView) {
 		mapPuskesmas.remove();
 	}
 	
 }
