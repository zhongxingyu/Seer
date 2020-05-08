 package org.scoutant.tf.overlay;
 
 import java.util.ConcurrentModificationException;
 
 import org.scoutant.tf.model.LatLng;
 import org.scoutant.tf.model.Model;
 import org.scoutant.tf.model.Network;
 import org.scoutant.tf.model.Polyline;
 import org.scoutant.tf.model.Road;
 import org.scoutant.tf.util.ColorUtil;
 import org.scoutant.tf.util.MapUtils;
 
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Paint.Cap;
 import android.graphics.Paint.Style;
 import android.graphics.Point;
 import android.graphics.Rect;
 import android.util.Log;
 
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapView;
 import com.google.android.maps.Overlay;
 import com.google.android.maps.Projection;
 
 public class TrafficOverlay extends Overlay {
 	
 	public static final int green = Color.rgb( 51, 187, 0);
 	private static final String tag = "overlay";
 
 	private Paint paint = new Paint();
 
 	public TrafficOverlay() {
 		paint.setStyle(Style.STROKE);
 		paint.setStrokeWidth(4.5f);
 		paint.setColor( Color.MAGENTA);
 		paint.setAntiAlias(true);
 		paint.setStrokeCap(Cap.ROUND);
 	}
 
 	private void drawTraffic(Canvas canvas, MapView map) {
 		for (Network network : Model.model().country.networks() ) {
 			drawNetwork(canvas, map, network);
 		}
 	}
 	private void drawNetwork(Canvas canvas, MapView map, Network network) {
 		try {
 			for(Road road : network.roads()) {
 				try {
 					drawPolyline(canvas, map, road.polyline);
 				} catch (ConcurrentModificationException e) {
 					Log.e(tag, "************************ Concurent modification exception : skipping road : " + road.name );
 				}
 			}
 		} catch (ConcurrentModificationException e) {
 			Log.e(tag, "************************ Concurent modification exception : skipping the full draw." );
 		}
 	}
 	
 	private void drawPolyline(Canvas canvas, MapView map, Polyline polyline ) {
 		if (polyline==null || polyline.size()==0) return;
 		Projection pj = map.getProjection();
 		Rect viewRect = MapUtils.toRectx2(map);
 		Point last = null;
		Point lastNormal=null;
		int q=0;
 		int color=Color.GRAY;
 		for (LatLng f : polyline.points()) {
 			boolean visible = viewRect.contains(f.getLongitudeE6(), f.getLatitudeE6());
 			if (visible) {
 				Point p = new Point();
 				pj.toPixels(f, p);
				if (last==null) {
					lastNormal = new Point(p);
					last = p;
 				} else {
					Point o = MapUtils.offset(lastNormal, p, 5);
					// if null, hence too close, we just omit the point 
 					if (o!=null) {
						lastNormal = new Point(p);
 						p.offset(o.x, o.y);
						// TODO ok to extrapolate unknow gray color? NO on too long distance!!!
//						if (f.color != 0 && f.color != -9407614 && f.color != -1) {
 						if (f.color != 0 && f.color != -1) {
 							color= ColorUtil.color(f.color);
 							if (color == Color.BLACK) {
 								Log.d(tag, "Color pb for point : " + p + ". color is " + f.color);
 							}
 						}
 						paint.setColor( color);
						q++;
						canvas.drawLine(last.x, last.y, p.x, p.y, paint);
 						last = p;
 					}
 				}
 			}
 		}
 	}
 	
 	@Override
 	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
 		if (shadow) return;
 		drawTraffic(canvas, mapView);
 		super.draw(canvas, mapView, shadow);
 	}
 	
 	@Override
 	public boolean onTap(GeoPoint p, MapView mapView) {
 		mapView.getController().zoomIn();
 		return super.onTap(p, mapView);
 	}
 }
