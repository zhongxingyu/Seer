 package put.medicallocator.ui.overlay;
 
 import java.util.List;
 
 import junit.framework.Assert;
 import put.medicallocator.io.model.Facility;
 import put.medicallocator.ui.overlay.utils.FacilityLookupStrategy;
 import put.medicallocator.ui.overlay.utils.FacilityTapListener;
 import put.medicallocator.utils.MyLog;
 import android.graphics.Canvas;
 import android.graphics.Point;
 import android.graphics.drawable.Drawable;
 
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapView;
 import com.google.android.maps.Overlay;
 import com.google.android.maps.Projection;
 
 public class FacilitiesOverlay extends Overlay {
 
     private static final String TAG = FacilitiesOverlay.class.getSimpleName();
     
     private final List<Facility> source;
     private final Drawable drawable;
     private final FacilityTapListener tapListener;
     private final FacilityLookupStrategy lookupStrategy;
 
     FacilitiesOverlay(List<Facility> source, Drawable drawable,
             FacilityLookupStrategy lookupStrategy, FacilityTapListener listener) {
         this.source = source;
         this.drawable = drawable;
         this.tapListener = listener;
         this.lookupStrategy = lookupStrategy;
 
         if (MyLog.ASSERT_ENABLED) {
             Assert.assertNotNull(lookupStrategy);
         }
     }
 
     @Override
     public void draw(Canvas canvas, MapView mapView, boolean shadow) {
         if (shadow) {
             // To speed-up drawing things, we just omit this phase.
             return;
         }
 
         final Projection projection = mapView.getProjection();
         final Point point = new Point();
 
         for (Facility facility : source) {
             projection.toPixels(facility.getGeoPoint(), point);
            drawable.setBounds(point.x, point.y,
                    point.x + drawable.getIntrinsicWidth(), point.y + drawable.getIntrinsicHeight());
             drawable.draw(canvas);
         }
     }
 
     @Override
     public boolean onTap(GeoPoint p, MapView mapView) {
         final long start = System.currentTimeMillis();
         
         final Facility facility = lookupStrategy.findNearestFacility(p, mapView.getProjection());
         
         final long end = System.currentTimeMillis();
         MyLog.d(TAG, "Finding nearest facility took " + (end - start) + "ms");
 
         if (facility != null) {
             if (tapListener != null) {
                 tapListener.onFacilityTap(facility);
             }
         }
         return false;
     }
 }
