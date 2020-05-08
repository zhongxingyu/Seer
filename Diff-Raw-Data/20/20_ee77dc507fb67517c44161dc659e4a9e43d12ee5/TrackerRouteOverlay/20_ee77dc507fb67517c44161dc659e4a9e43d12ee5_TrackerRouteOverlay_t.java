 package com.valfom.tracker;
 
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Path;
 import android.graphics.Point;
 
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapView;
 import com.google.android.maps.Overlay;
 import com.google.android.maps.Projection;
 
 public class TrackerRouteOverlay extends Overlay {
 	
     private Integer trackId = null;
     
 	private Paint paint = new Paint();
 	private Point fromPoint = new Point();
 	private Point toPoint = new Point();
 	private Path path = new Path();
     
     private TrackerPoint trackerPointPrev = null;
     private TrackerPoint trackerPointCur = null;
     
     private GeoPoint fromGeoPoint = null;
     private GeoPoint toGeoPoint = null;
     
     private TrackerRoute route = null;
     
     private int accuracy = 3;
 	
     public TrackerRouteOverlay() {
 		
     	initPaint();
 	}
     
 	public TrackerRouteOverlay(int trackId) {
 		
 		this.trackId = trackId;
 		
 		initPaint();
 	}
 	
 	private void initPaint() {
 		
 		paint.setDither(true);
 		paint.setColor(Color.RED);
 		paint.setStyle(Paint.Style.STROKE);
 		paint.setStrokeJoin(Paint.Join.ROUND);
 		paint.setStrokeCap(Paint.Cap.ROUND);
 //		paint.setAlpha(100);
 		paint.setStrokeWidth(5);
 		paint.setAntiAlias(true);
 	}
 
 	@Override
 	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
 		
 		TrackerDB db = new TrackerDB(mapView.getContext());
 		
 		if (trackId == null) route = db.getRoute();
 		else if (trackId == TrackerStorage.trackId) route = TrackerStorage.route;
 		else {
 				
 			TrackerStorage.trackId = trackId;
 			TrackerStorage.route = route = db.getRoute(trackId);
 		}
 		
 		int zoomLvl = mapView.getZoomLevel();
 		
 		if (zoomLvl == 19) accuracy = 2;
 		else if (zoomLvl >= 20) accuracy = 1;
 		
 		Projection projection = mapView.getProjection();
 	    
 		int count = route.getCount();
 		
 		if (count >= 2) {
 			
 			trackerPointPrev = route.getPoint(0);
 			fromGeoPoint = new GeoPoint(trackerPointPrev.getLatitude(), trackerPointPrev.getLongtitude());
 			
 			projection.toPixels(fromGeoPoint, fromPoint);
 		}
 			
 		for (int i = accuracy; i < count; i += accuracy) {
 	    	
 	    	trackerPointCur = route.getPoint(i);
 	    	toGeoPoint = new GeoPoint(trackerPointCur.getLatitude(), trackerPointCur.getLongtitude());
 	    	
 	    	projection.toPixels(toGeoPoint, toPoint);
 	    	
 	    	if ((toPoint.x > 0) && (toPoint.y > 0) && (toPoint.x < canvas.getWidth()) && (toPoint.y < canvas.getHeight())) {
 	    	
 	    		projection.toPixels(fromGeoPoint, fromPoint);
 	    		
 	    		path.moveTo(fromPoint.x, fromPoint.y);
 	            path.lineTo(toPoint.x, toPoint.y);
 	            
 	    	} else if ((fromPoint.x > 0) && (fromPoint.y > 0) && (fromPoint.x < canvas.getWidth()) && (fromPoint.y < canvas.getHeight())) {
 	    		
 	    		projection.toPixels(fromGeoPoint, fromPoint);
 	    		
 	    		path.moveTo(fromPoint.x, fromPoint.y);
 	            path.lineTo(toPoint.x, toPoint.y);
 	    	}
 	    	
 	    	fromGeoPoint = toGeoPoint;
 	    }
 		
 		canvas.drawPath(path, paint);
 		
 		path.reset();
 	    
 	    super.draw(canvas, mapView, shadow);
 	}
 
 	@Override
 	public boolean onTap(GeoPoint geoPoint, MapView mapView) {
 		
 		return true;
 	}
}   
