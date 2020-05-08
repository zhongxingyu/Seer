 package info.eigenein.openwifi.helpers.map;
 
 import android.graphics.Canvas;
 import android.util.Log;
import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapView;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class TrackableMapView extends MapView {
     private static final String LOG_TAG = TrackableMapView.class.getCanonicalName();
 
     private int oldZoomLevel = -1;
 
    private GeoPoint oldMapCenter = null;

     private List<MapViewListener> listeners = new ArrayList<MapViewListener>();
 
    @SuppressWarnings("UnusedDeclaration")
     public TrackableMapView(android.content.Context context, android.util.AttributeSet attrs) {
         super(context, attrs);
     }
 
    @SuppressWarnings("UnusedDeclaration")
     public TrackableMapView(android.content.Context context, android.util.AttributeSet attrs, int defStyle) {
         super(context, attrs, defStyle);
     }
 
    @SuppressWarnings("UnusedDeclaration")
     public TrackableMapView(android.content.Context context, java.lang.String apiKey) {
         super(context, apiKey);
     }
 
     @Override
     public void dispatchDraw(Canvas canvas) {
         super.dispatchDraw(canvas);
         if (getZoomLevel() != oldZoomLevel) {
            Log.d(LOG_TAG + ".dispatchDraw", "zoomLevel " + oldZoomLevel + " " + getZoomLevel());
             oldZoomLevel = getZoomLevel();
             fireMovedOrZoomed();
        } else if (!getMapCenter().equals(oldMapCenter)) {
            Log.d(LOG_TAG + ".dispatchDraw", "mapCenter " + oldMapCenter + " " + getMapCenter());
            oldMapCenter = getMapCenter();
            fireMovedOrZoomed();
         }
     }
 
     public void addMovedOrZoomedObserver(MapViewListener listener) {
         listeners.add(listener);
     }
 
     private void fireMovedOrZoomed() {
         Log.d(LOG_TAG, "fireMovedOrZoomed " + listeners.size());
 
         for (MapViewListener listener : listeners) {
             synchronized (listener) {
                 listener.onMovedOrZoomed();
             }
         }
     }
 }
