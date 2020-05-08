 package com.github.tommywalsh.map;
 
 import java.util.TreeSet;
 import java.util.TreeMap;
 import java.util.SortedMap;
 import java.util.Vector;
 import java.util.Scanner;
 import java.io.File;
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.Overlay;
 import com.google.android.maps.MapView;
 import android.graphics.Canvas;
 import android.graphics.Paint;
 import android.graphics.Point;
 import android.graphics.Path;
 import android.graphics.Color;
 import android.os.Handler;
 
 
 // This class handles drawing of a path on a map.
 // The path is stored as KML file, and displayed as a semi-translucent overlay 
 // over the map.
 public class RouteOverlay extends Overlay
 {
 
     public RouteOverlay() {
     }
 
     public void loadDataSet(String filename) {
 	startIOProcess(filename);	
     }
 
     // Called periodically by the Android system when it wants us to redraw the path
     public void draw(Canvas canvas, MapView mv, boolean shadow) {
 	super.draw(canvas, mv, shadow);
 
 
 	if (m_loaded) {
 
 	    // This is the normal case: all of our data is already loaded
 	    // So, we need to draw the path to screen
 
 	    // Draws are called twice, once for the "shadow" and once for the foreground
 	    // Since we draw only one thing, we only need to draw during one of these
 	    // Pick shadow, in case we ever add anything later that we want to appear
 	    // on top of the path.
 	    if (shadow) {
 		
 		// Do we need to calculate a new path?  Only if we've moved since the last call
 		// Note, it might be more efficient to give a little "slop" to this, so that 
 		// a small amount of motion is allowed before recalulating the whole path.  In that
 		// case we'd simply translate() the path based on the number of pixels moved since last
 		// time.  But, we can't allow moves that are "too big" since the map projection change
 		// does not imply necessarily a pixel-for-pixel translation over larger distances.
 		GeoPoint ctr = mv.getMapCenter();
 		
 		if (m_path == null || !ctr.equals(m_lastCenter))  {
 		    // We need to calculate a new path
 		    m_path = new Path();
 		    
 		    // Figure screen span
 		    int latSpan = mv.getLatitudeSpan();
 		    int lat1 = ctr.getLatitudeE6() - latSpan/2;
 		    int lat2 = lat1 + latSpan;
 		    int lngSpan = mv.getLongitudeSpan();
 		    int lng1 = ctr.getLongitudeE6() - lngSpan/2;
 		    int lng2 = lng1 + lngSpan;
 		    
 		    // cache center for next time
 		    m_lastCenter = ctr;
 
 
 		    // Each tip may have one or more legs.  Try to draw them all
 		    for (TripLeg leg : m_legs) {
 			
 			// We can be more efficient here by short-circuiting and not considering
 			// legs that aren't even partially on-screen
 
 			// Iterate over each point in the leg, and figure out if we need to add it
 			// to the drawn path.  The analogy here is a pen.  
 			boolean drewLastPoint = false;
 			for (int i = 0; i < leg.size(); i++) {
 			    GeoPoint gp = leg.elementAt(i);
 			    int lat = gp.getLatitudeE6();
 			    int lng = gp.getLongitudeE6();
 			    Point thisPoint = new Point();
 			    Point otherPoint = new Point();
 			    
 			    
 			    if (lat >= lat1 && lat <= lat2 && lng >= lng1 && lng <= lng2) {
 				// this point is in view.  We will need to do some drawing
 				mv.getProjection().toPixels(gp, thisPoint);
 				if (drewLastPoint) {
 				    // Last point was also on screen... 
 				    // The pen is on the paper.  Draw to the current point.
 				    m_path.lineTo(thisPoint.x, thisPoint.y); 
 				} else if (i != 0) {
 				    // The last point was off-screen.  Pick up the pen and put it there,
 				    // then draw to the current point.
 				    mv.getProjection().toPixels(leg.elementAt(i-1), otherPoint);
 				    m_path.moveTo(otherPoint.x, otherPoint.y);
 				    m_path.lineTo(thisPoint.x, thisPoint.y); 
 				} else {
 				    // This is the first point in the leg.
 				    // Pick up the pen and position it at this point.
 				    m_path.moveTo(thisPoint.x, thisPoint.y);
 				}
 				drewLastPoint = true;
 			    } else {
 				// This point is not on screen...
 				if (drewLastPoint) {
 				    // ... but the last point was.  Draw to this off-screen point to finish the path
 				    mv.getProjection().toPixels(gp, thisPoint);
 				    m_path.lineTo(thisPoint.x, thisPoint.y);
 				}
 				drewLastPoint = false;
 			    }
 			}
 		    }
 		}
 	    }
 
 
 
 	    // Now, we've got a fully-calculated path.  Put it on the screen (if it's not empty)
 	    if (!m_path.isEmpty()) {
 		Paint paint = new Paint();
 		paint.setDither(true);
 		paint.setARGB(126,0,126,255);
 		paint.setStyle(Paint.Style.STROKE);
 		paint.setStrokeJoin(Paint.Join.ROUND);
 		paint.setStrokeCap(Paint.Cap.ROUND);
 		paint.setStrokeWidth(5);		
 		canvas.drawPath(m_path, paint);
 	    }
 
 	} else {
 	    // We get here if the data hasn't been loaded.  Just show a message.
 
 	    Paint paint = new Paint();
 	    if (shadow) {
 		// Draw a black rectangular background...
 		paint.setARGB(255,0,0,0);
 		paint.setStyle(Paint.Style.FILL);
 		canvas.drawRect(0,0,300,40, paint);		
 	    } else {
 		// With white text in front.
 		paint.setARGB(255,255,255,255);
 		paint.setStyle(Paint.Style.STROKE);
 		canvas.drawText("Loading Route Data...", 10, 20, paint);
 	    }
 
 	    // And cache the map so we can force a redraw when we get the data loaded.
 	    m_mapView = mv;
 	}
     }
 
 
 
 
     GeoPoint m_lastCenter;    // Cache the last center point so we don't bother redrawing the path unless it changes
     Path m_path;              // Also cache the path for potential reuse next draw cycle
 
     
 
 
     // We do our IO operations in another thread.  It can take in a while to read in a whole bike trip's
     // worth of position data.  Don't hang the UI thread during this time! 
 
// If we draw before we load our data, cache the mapview so we can force it to redraw    
    MapView m_mapView = null;        
 
    // ONLY to be used on the UI thread!
    // "true" because we are trivially loaded at startup
    boolean m_loaded = true;         

    // m_legs is not safe to access unless m_loaded is true
    Vector<TripLeg> m_legs = new Vector<TripLeg>();   
 
     final Handler m_handler = new Handler();
     final Runnable m_IODoneUpdater = new Runnable() {
 	    // This will be called when IO operations are done on the other thread.
 	    // This runs on the UI thread, so it is safe to change m_loaded
 	    // Also, force a redraw of the map, so we can draw the newly-gotten route
 	    public void run() {
 		m_loaded = true;   // done loading
 		if (m_mapView != null) {
 		    // force redraw and stop caching map view
 		    m_mapView.invalidate();
 		    m_mapView = null;
 		}
 	    }
 	};
 
     
     private void startIOProcess(final String filename) {
 
 	// clear our caches so we don't draw an old path while we wait
 	// for the data
 	m_loaded = false;
 	m_legs = null;
 	m_path = null;
 	m_lastCenter = null;
 
 	Thread t = new Thread() {
 		public void run() {
 
 		    // This runs on its own thread.
 		    // It's okay to take our sweet time loading data here.
 		    m_legs = KMLParser.parse(filename);
 
 		    // Notify the UI thread that we're done (this will cause m_IODoneUpdater.run to run on the UI thread)
 		    m_handler.post(m_IODoneUpdater);
 		}
 	    };
 	t.start();
     }
 }
