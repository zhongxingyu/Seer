 package com.example.pinchpanmap;
 
 import android.graphics.Point;
 import android.os.Handler;
 import android.util.FloatMath;
 import android.util.Log;
 import android.view.MotionEvent;
 
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapView;
 import com.google.android.maps.Overlay;
 
 public class PinchPanOverlay extends Overlay {
 
	private Point   mOneFingerPanStart;

     private Point   mTwoFingerPanFirstPoint;
     private Point   mTwoFingerPanSecondPoint;

     private Point   mLastPinchCenter;

     private float   mCurrentDistanceSum = 0;

     private Handler mHandler            = new Handler();
 
     @Override
     public boolean onTouchEvent(MotionEvent event, final MapView mapView) {
         int action = event.getAction();
 
         if (event.getPointerCount() == 1) {
             switch (action)
             {
             case MotionEvent.ACTION_DOWN:
             	mOneFingerPanStart = new Point((int)event.getX(), (int)event.getY());
                 break;
             case MotionEvent.ACTION_MOVE:
                 pan(mapView, mOneFingerPanStart, new Point((int)event.getX(), (int)event.getY()));
                 break;
             case MotionEvent.ACTION_UP:
                 break;
             default:
                 break;
             }
         }
 
         if (event.getPointerCount() == 2) {
             int pointerId1 = event.getPointerId(0);
             int pointerId2 = event.getPointerId(1);
 
             switch (action) {
             case MotionEvent.ACTION_POINTER_2_DOWN:
                 mTwoFingerPanFirstPoint = new Point((int)event.getX(pointerId1), (int)event.getY(pointerId1));
                 mTwoFingerPanSecondPoint = new Point((int)event.getX(pointerId2), (int)event.getY(pointerId2));
                 mLastPinchCenter = getCenterPoint(mTwoFingerPanFirstPoint, mTwoFingerPanSecondPoint);
                 break;
             case MotionEvent.ACTION_MOVE:
             	float previousFingerSpan = computeEuclideanDistanceBetween(mTwoFingerPanFirstPoint, mTwoFingerPanSecondPoint);
             	
             	final Point previousFirstPoint = new Point(mTwoFingerPanFirstPoint);
             	final Point previousSecondPoint = new Point(mTwoFingerPanSecondPoint);
             	
             	mTwoFingerPanFirstPoint = new Point((int)event.getX(pointerId1), (int)event.getY(pointerId1));
             	mTwoFingerPanSecondPoint = new Point((int)event.getX(pointerId2), (int)event.getY(pointerId2));
 
                 final Point currentCenter = getCenterPoint(mTwoFingerPanFirstPoint, mTwoFingerPanSecondPoint);
                 final float currentFingerSpan = computeEuclideanDistanceBetween(mTwoFingerPanFirstPoint, mTwoFingerPanSecondPoint);
                 final float mostRecentDistanceChange = currentFingerSpan - previousFingerSpan;
 
                 mCurrentDistanceSum = mCurrentDistanceSum + mostRecentDistanceChange;
 
                 int targetZoomLevel = mapView.getZoomLevel();
 
                 final int ZOOM_THRESHOLD = 30;
                 if (mCurrentDistanceSum > ZOOM_THRESHOLD) {
                     targetZoomLevel++;
                     mCurrentDistanceSum = mCurrentDistanceSum % ZOOM_THRESHOLD;
                 } else if (mCurrentDistanceSum < -ZOOM_THRESHOLD) {
                     targetZoomLevel--;
                     mCurrentDistanceSum = mCurrentDistanceSum % ZOOM_THRESHOLD;
                 }
 
                 Log.d("Zoom", "Zooming from " + mapView.getZoomLevel() + " to " + targetZoomLevel);
 
                 if (targetZoomLevel != mapView.getZoomLevel()) {
                     long delay = 0;
                     int zoomLevel = mapView.getZoomLevel();
                     while (zoomLevel != targetZoomLevel) {
                         if (zoomLevel < targetZoomLevel) {
                             mHandler.postDelayed(new Runnable() {
                                 @Override
                                 public void run() {
                                     mapView.getController().zoomInFixing((int)mLastPinchCenter.x, (int)mLastPinchCenter.y);
                                 }
                             }, delay);
                             zoomLevel++;
                         } else {
                             mHandler.postDelayed(new Runnable() {
                                 @Override
                                 public void run() {
                                     mapView.getController().zoomOutFixing((int)mLastPinchCenter.x, (int)mLastPinchCenter.y);
                                 }
                             }, delay);
                             zoomLevel--;
                         }
 
                         delay += 350;
                     }
 
                 } else {
                     if (computeEuclideanDistanceBetween(previousFirstPoint, mTwoFingerPanFirstPoint) > 4 &&
                         computeEuclideanDistanceBetween(previousSecondPoint, mTwoFingerPanSecondPoint) > 4) {
                         pan(mapView, mOneFingerPanStart, new Point((int)event.getX(), (int)event.getY()));
                     }
                 }
 
                 mLastPinchCenter = currentCenter;
 
                 break;
             case MotionEvent.ACTION_UP:
                 break;
             default:
                 break;
             }
         }
 
         return true;
     }
 
    private boolean pan(MapView mapView, Point startPoint, Point newPoint) {
         if (mOneFingerPanStart.x >= 0 && mOneFingerPanStart.y >= 0) {
             GeoPoint mapCenter = mapView.getMapCenter();
             GeoPoint panToCenter = new GeoPoint(
                     (int) (mapCenter.getLatitudeE6() + (newPoint.y - startPoint.y) * 0.001 * mapView.getLatitudeSpan()),
                     (int) (mapCenter.getLongitudeE6() - (newPoint.x - startPoint.x) * 0.001 * mapView.getLongitudeSpan()));
             mapView.getController().setCenter(panToCenter);
         }
         mOneFingerPanStart = new Point(newPoint.x, newPoint.y);
        return true;
     }
 
     private float computeEuclideanDistanceBetween(Point first, Point second) {
        return computeEuclideanDistanceBetween(first.x, first.y, second.x, second.y);
    }

    private float computeEuclideanDistanceBetween(float x1, float y1, float x2, float y2) {
        return FloatMath.sqrt((float) (Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2)));
    }

    private Point getCenterPoint(float x1, float y1, float x2, float y2) {
        return new Point((int)((x1 + x2) / 2), (int)((y1 + y2) / 2));
     }
     
     private Point getCenterPoint(Point p1, Point p2) {
         return new Point((int)((p1.x + p2.x) / 2), (int)((p1.y + p2.y) / 2));
     }
 }
