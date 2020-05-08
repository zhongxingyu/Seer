 package id.sabril.testingPA;
 import android.graphics.Canvas;
 import android.graphics.Paint;
 import android.graphics.Point;
 
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapView;
 import com.google.android.maps.Overlay;
 
 public class MapCircleOverlay extends Overlay {
 
 private GeoPoint point;
 private Paint paint1, paint2;
 private float radius; //in meters
 
 public MapCircleOverlay(GeoPoint point, float radius) {
     this.point = point;
 
     paint1 = new Paint();
    paint1.setARGB(135, 0, 173, 255);
     paint1.setStrokeWidth(2);
     paint1.setStrokeCap(Paint.Cap.ROUND);
     paint1.setAntiAlias(true);
     paint1.setDither(false);
     paint1.setStyle(Paint.Style.STROKE);
 
     paint2 = new Paint();
    paint2.setARGB(60, 0, 173, 255);
 
     this.radius = radius;
 }
 
 @Override
 public void draw(Canvas canvas, MapView mapView, boolean shadow) {
 
     Point pt = mapView.getProjection().toPixels(point, null);
     float projectedRadius = mapView.getProjection().metersToEquatorPixels(radius);
 
     canvas.drawCircle(pt.x, pt.y, projectedRadius, paint2);
     canvas.drawCircle(pt.x, pt.y, projectedRadius, paint1);
 
 }
 
 }
