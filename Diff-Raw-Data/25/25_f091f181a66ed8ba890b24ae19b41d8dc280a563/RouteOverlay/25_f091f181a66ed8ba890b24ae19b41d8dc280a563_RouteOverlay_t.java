 package put.medicallocator.ui.overlay;
 
 import android.graphics.*;
 import com.google.android.maps.*;
 import put.medicallocator.io.route.model.RouteSpec;
 import put.medicallocator.utils.ArrayUtils;
 import put.medicallocator.utils.MyLog;
 
 import java.util.ArrayList;
 
 /**
  * {@link Overlay} implementation for drawing route purpose on the {@link MapView}.
  * Uses {@link Path} to draw smoothly the route.
  *
  * <p>Path is built only when necessary (for the first time
  * and when zoom changes). Since the application is GPU accelerated,
  * Path is being drawn on the {@link Bitmap} first (which is not accelerated).
  * GPU acceleration couldn't be used for path drawing, because in that mode path is being drawn as a texture
  * and devices have some limitations about the texture size (f.e. 2048x2048). Because of the fact that path
  * may be simply much larger than this, it is drawn using the CPU on the Bitmap {@link Canvas}.</p>
  *
  * <p>Bitmap is of the {@link MapView} size. To draw proper part of the Path on the bitmap, there are some translations
  * on-going calculated based on the initial position of some points from the Route.
  * This is also used to skip redrawing path every time {@link MapView} is moved.</p>
  */
 public class RouteOverlay extends Overlay {
 
     private static final String TAG = RouteOverlay.class.getSimpleName();
 
     /** Defines the width of the path inner part. */
     private static final int INNER_LINE_WIDTH = 4;
 
     /** Used to define path inner part look. */
     private final Paint innerPaint;
 
     /** Used to define path outer part (stroking) look. */
     private final Paint outerPaint;
 
     /** Collects all the points creating the actual route. */
     private final ArrayList<GeoPoint> points;
 
     /** Cached path instance used to draw the route. */
     private final Path path = new Path();
 
     /** {@link Point} instance used to either build path or to calculate translations. */
     private final Point point = new Point();
 
     /** {@link Bitmap} where the path is being drawn. */
     private final Bitmap bitmap;
 
     /** {@link Canvas} to facilitate drawing on the {@link Bitmap} (using CPU). */
     private final Canvas bitmapCanvas;
 
     /** Last span of the longitude. Used to detect whether zoom changed. */
     private int lastLongitudeSpan = Integer.MIN_VALUE;
 
     /** Reference {@link Point} used to detect offset between the initial route position and its current position. */
     private Point firstDrawnReferencePoint = new Point();
 
     /** {@link Point} storing the offset itself. */
     private Point offset = new Point();
 
     /**
      * Constructor. Creates internal structures and caches everything what possible to avoid creating any objects
      * in drawing itself.
      */
     public RouteOverlay(RouteSpec route, MapView mv) {
         this.points = new ArrayList<GeoPoint>();
         this.innerPaint = new Paint();
         this.outerPaint = new Paint();
         this.bitmap = Bitmap.createBitmap(mv.getWidth(), mv.getHeight(), Bitmap.Config.ARGB_8888);
         this.bitmapCanvas = new Canvas(bitmap);
 
         if (ArrayUtils.isNotEmpty(route.getPoints())) {
             for (int i = 0; i < route.getPoints().length; i++) {
                 points.add(new GeoPoint(
                         (int) (route.getPoints()[i].getLatitude() * 1E6),
                         (int) (route.getPoints()[i].getLongitude() * 1E6)));
             }
             int moveToLat = (points.get(0).getLatitudeE6() + (points.get(
                     points.size() - 1).getLatitudeE6() - points.get(0)
                     .getLatitudeE6()) / 2);
             int moveToLong = (points.get(0).getLongitudeE6() + (points.get(
                     points.size() - 1).getLongitudeE6() - points.get(0)
                     .getLongitudeE6()) / 2);
             GeoPoint moveTo = new GeoPoint(moveToLat, moveToLong);
 
            final MapController mapController = mv.getController();
             mapController.animateTo(moveTo);
 
             innerPaint.setColor(0x28D7FF);
             innerPaint.setStyle(Paint.Style.STROKE);
             innerPaint.setStrokeWidth(INNER_LINE_WIDTH);
             innerPaint.setAntiAlias(false);
             innerPaint.setAlpha(0x80);
 
             outerPaint.setColor(0x1E9AFF);
             outerPaint.setStyle(Paint.Style.STROKE);
             outerPaint.setStrokeWidth(INNER_LINE_WIDTH * 2);
             outerPaint.setAntiAlias(true);
             outerPaint.setAlpha(0xAB);
         }
 
         MyLog.d(TAG, "Initializion done");
     }
 
     @Override
     public void draw(Canvas canvas, MapView mapView, boolean shadow) {
         super.draw(canvas, mapView, shadow);
 
         if (shadow) {
             return;
         }
 
         buildPath(mapView);
 
         bitmapCanvas.save();
         bitmapCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
         bitmapCanvas.translate(offset.x, offset.y);
         bitmapCanvas.drawPath(path, outerPaint);
         bitmapCanvas.drawPath(path, innerPaint);
         bitmapCanvas.restore();
 
         canvas.drawBitmap(bitmap, 0f, 0f, null);
     }
 
     /**
      * Builds the route {@link Path}, if required (if zoom changed or if path is built for the first time).
      * Otherwise, calculates the offset to be used to draw previously created {@link Path}.
      */
     public Path buildPath(MapView mapView) {
         final Projection projection = mapView.getProjection();
         if (points.size() > 1) {
             projection.toPixels(points.get(0), point);
             final int currentLongitudeSpan = projection.fromPixels(0, mapView.getHeight() / 2).getLongitudeE6() - projection.fromPixels(mapView.getWidth(), mapView.getHeight() / 2).getLongitudeE6();
             if (currentLongitudeSpan == lastLongitudeSpan) {
                 // For some reason, offsetting the path doesn't work on the HTC One X, 4.1.1. It's GPU accelerated,
                 // but Nexus 7 (also accelerated) works just fine. For this reason, Canvas is translated.
                 // path.offset(point.x - lastDrawnReferencePoint.x, point.y - lastDrawnReferencePoint.y);
                 offset.set(point.x - firstDrawnReferencePoint.x, point.y - firstDrawnReferencePoint.y);
             } else {
                 offset.set(0, 0);
                 path.rewind();
                 path.moveTo(point.x, point.y);
                 firstDrawnReferencePoint.set(point.x, point.y);
 
                 for (int i = 1; i < points.size(); i++) {
                     projection.toPixels(points.get(i), point);
                     path.lineTo(point.x, point.y);
                 }
             }
             lastLongitudeSpan = currentLongitudeSpan;
         }
 
         return path;
     }
 
 }
