 package stetson.CTF.Game;
 
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Point;
 import android.graphics.Rect;
 
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapView;
 import com.google.android.maps.Overlay;
 
 public class Boundaries extends Overlay {
 	
 	private GeoPoint redTopLeftBoundary;
 	private GeoPoint redBottomRightBoundary;
 	private GeoPoint blueTopLeftBoundary;
 	private GeoPoint blueBottomRightBoundary;
 
 	/**
 	 * Sets the bounds of the red zone.
 	 * @param topLeft
 	 * @param bottomRight
 	 */
 	public void setRedBounds(GeoPoint topLeft, GeoPoint bottomRight) {
 		redTopLeftBoundary = topLeft;
 		redBottomRightBoundary = bottomRight;
 	}
 	
 	/**
 	 * Sets the bounds of the blue zone.
 	 * @param topLeft
 	 * @param bottomRight
 	 */
 	public void setBlueBounds(GeoPoint topLeft, GeoPoint bottomRight) {
 		blueTopLeftBoundary = topLeft;
 		blueBottomRightBoundary = bottomRight;
 	}
 	
 	/**
 	 * Draws the red and blue boundaries on the map canvas.
 	 */
 	public void draw(android.graphics.Canvas canvas, MapView mapView, boolean shadow) { 
 		
 		// Draw Red Rectangle
 		Paint redPaint = new Paint();
 		redPaint.setColor(Color.RED);
 		redPaint.setAlpha(40);
 		Point redTopLeft = new Point(); 
 		Point redBottomRight = new Point();
 		mapView.getProjection().toPixels(redTopLeftBoundary,redTopLeft);
 		mapView.getProjection().toPixels(redBottomRightBoundary,redBottomRight);
 		canvas.drawRect(new Rect(redTopLeft.x,redTopLeft.y,redBottomRight.x,redBottomRight.y), redPaint);
 		
 		// Draw Blue Rectangle
 		Paint bluePaint = new Paint();
 		bluePaint.setColor(Color.BLUE);
 		bluePaint.setAlpha(40);
 		Point blueTopLeft = new Point(); 
 		Point blueBottomRight = new Point();
 		mapView.getProjection().toPixels(blueTopLeftBoundary,blueTopLeft);
 		mapView.getProjection().toPixels(blueBottomRightBoundary,blueBottomRight);
 		canvas.drawRect(new Rect(blueTopLeft.x,blueTopLeft.y,blueBottomRight.x,blueBottomRight.y), bluePaint);
 	} 
 	
	public GeoPoint getTopLeft() {
		return redTopLeftBoundary;
	}
	
	public GeoPoint getBottomRight() {
		return blueBottomRightBoundary;
	}
 }
