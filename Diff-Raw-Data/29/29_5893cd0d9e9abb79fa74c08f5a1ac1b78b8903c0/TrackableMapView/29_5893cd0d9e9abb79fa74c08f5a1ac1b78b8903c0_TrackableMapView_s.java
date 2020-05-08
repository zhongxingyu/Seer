 package info.eigenein.openwifi.helpers.map;
 
 import android.graphics.Canvas;
 import android.util.Log;
import android.view.MotionEvent;
 import com.google.android.maps.MapView;
 
 import java.util.ArrayList;
 import java.util.List;
import java.util.Observer;
 
 public class TrackableMapView extends MapView {
     private static final String LOG_TAG = TrackableMapView.class.getCanonicalName();
 
     private int oldZoomLevel = -1;
 
     private List<MapViewListener> listeners = new ArrayList<MapViewListener>();
 
     public TrackableMapView(android.content.Context context, android.util.AttributeSet attrs) {
         super(context, attrs);
     }
 
     public TrackableMapView(android.content.Context context, android.util.AttributeSet attrs, int defStyle) {
         super(context, attrs, defStyle);
     }
 
     public TrackableMapView(android.content.Context context, java.lang.String apiKey) {
         super(context, apiKey);
     }
 
     @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_UP) {
            fireMovedOrZoomed();
        }
        return super.onTouchEvent(ev);
    }

    @Override
     public void dispatchDraw(Canvas canvas) {
         super.dispatchDraw(canvas);
         if (getZoomLevel() != oldZoomLevel) {
             oldZoomLevel = getZoomLevel();
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
