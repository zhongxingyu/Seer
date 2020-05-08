 package it.giacomos.android.osmer.widgets.mapview;
 
 import it.giacomos.android.osmer.preferences.Settings;
 import android.graphics.Canvas;
 import android.graphics.Paint;
 import android.graphics.Point;
 import android.graphics.Rect;
 import android.graphics.RectF;
 import android.location.Location;
 import android.os.Bundle;
 import android.os.Parcelable;
 import android.util.Log;
 import android.view.MotionEvent;
 
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapView;
 import com.google.android.maps.MyLocationOverlay;
 import com.google.android.maps.Overlay;
 
 /**
  * This class manages distance measuring between two points: mCenterPoint and mTappedPoint.
  * 
  * States:
  * - the measure becomes active when the user taps on a point of the map (a circle becomes visible);
  * - by default, the measured distance is taken from the MyLocation overlay and the tapped point;
  * - after the user taps, it is possible to move around the center of the circle
  * - tapping outside a sensible area deactivates the measure;
  * - tapping inside the sensible area makes the circle grow/shrink
  * - tapping on a small area around the arrow, the arrow point can be moved
  * 
  * @author giacomo
  *
  */
 public class CircleOverlay extends Overlay {
 
 	CircleOverlay()
 	{
 		mCurrentGeoPoint = null;
 		mJustMoved = false;
 		mMovingCenter = false;
 		mCustomCenter = false;
 		mIsActive = false;
 	}
 
 	public boolean onTap(GeoPoint gp,
 			MapView mapView)
 	{
 		if(!mIsActive) /* enter distance mode */
 		{
 			mCurrentGeoPoint = gp;
 			mIsActive = true;
 		}
 		else if(!mJustMoved) /* exit distance mode */
 		{
 			mIsActive = false;
 			mMovingCenter = false;
 			mCurrentGeoPoint = null;
 			mCustomCenter = false;
 		}
 		return true;
 	}
 
 	@Override
 	public boolean draw(Canvas canvas, MapView mapView, boolean shadow, long when)
 	{
 		super.draw(canvas, mapView, shadow);
 
 		if(mCurrentGeoPoint == null)
 			return false;
 		/* 
 		 * get center point and current (tapped) point
 		 */
 		GeoPoint centerGeoPoint = null;
 		Point centerPoint = null;
 
 		Point currentPoint = mapView.getProjection().toPixels(mCurrentGeoPoint, null);
 
 		if(mCustomCenter) /* a custom center has been specified */
 		{
 			centerGeoPoint = mCenterGeoPoint; /* take it from the internal variable */
 			centerPoint = mapView.getProjection().toPixels(centerGeoPoint, null);
 		}
 		else 
 		{
 			/* get the center every time we draw */
 			centerGeoPoint = getCenterGeoPoint(mapView);
 			centerPoint = getCenterPoint(mapView);	
 		}
 		if(centerGeoPoint != null)
 		{
 			float dist [] = new float[1];
 			/* calculate distance */
 			Location.distanceBetween(centerGeoPoint.getLatitudeE6()/1E6,
 					centerGeoPoint.getLongitudeE6()/1E6, 
 					mCurrentGeoPoint.getLatitudeE6()/1E6,
 					mCurrentGeoPoint.getLongitudeE6()/1E6, dist);
 			/* from meters to km */
 			dist[0] /= 1000.0f;
 
 			/* arrow ;-) */
 			float radius = (float) Math.sqrt(Math.pow(centerPoint.x - currentPoint.x, 2) +
 					Math.pow(centerPoint.y - currentPoint.y, 2));
 
 			double alphaArrow = Math.PI / 48;
 			double alpha;
 
 			if(currentPoint.x < centerPoint.x)
 				alpha =  Math.asin(-(centerPoint.y - currentPoint.y)/radius) + Math.PI;
 			else
 				alpha =  Math.asin((centerPoint.y - currentPoint.y)/radius);
 
 			Paint p = new Paint();
 			if(mJustMoved && mapView.isSatellite())
 				p.setARGB(255, 255, 233, 233);
 			else if(mJustMoved)
 				p.setARGB(185, 26, 120, 6);
 			else if(mapView.isSatellite())
 				p.setARGB(255, 255, 255, 255);
 			else
 				p.setARGB(155, 0, 113, 188);
 
 			float yArr1Coord = (float) (centerPoint.y -  0.9f * radius* Math.sin(alpha - alphaArrow));
 			float xArr1Coord = (float) (centerPoint.x + 0.9f * radius * Math.cos(alpha - alphaArrow));
 
 			if(mCustomCenter)
 				canvas.drawCircle(centerPoint.x, centerPoint.y, 6, p);
 
 			if(mMovingCenter)
 			{
 				canvas.drawLine(0, centerPoint.y, mapView.getWidth(), centerPoint.y, p);
 				canvas.drawLine(centerPoint.x, 0, centerPoint.x, mapView.getHeight(), p);
 			}
 			canvas.drawLine(centerPoint.x, centerPoint.y, currentPoint.x, currentPoint.y, p);
 
 			canvas.drawLine((float)currentPoint.x, (float)currentPoint.y, 
 					xArr1Coord,
 					yArr1Coord, p);
 
 			canvas.drawLine((float)currentPoint.x, (float)currentPoint.y, 
 					(float) (centerPoint.x +  0.9f * radius * Math.cos(alpha + alphaArrow)),
 					(float) (centerPoint.y -  0.9f * radius * Math.sin(alpha + alphaArrow)), p);
 
 			/* draw the circle */
 			if(mapView.isSatellite())
 				p.setAlpha(40);
 			else
 				p.setAlpha(20);
 			p.setStyle(Paint.Style.FILL_AND_STROKE);
 			canvas.drawCircle((float)centerPoint.x, (float) centerPoint.y, radius, p);
 
 			p.setARGB(130, 255, 255, 255);
 			Rect txtBounds = new Rect();
 			String text = String.format("%.1fkm", dist[0]);
 			p.getTextBounds(text, 0, text.length(), txtBounds);
 
 			txtBounds.set((int)xArr1Coord + 3,  (int)yArr1Coord - txtBounds.height(), 
 					(int)xArr1Coord + 3 + txtBounds.width(),
 					(int) yArr1Coord);
 
 			RectF rectF = new RectF(txtBounds);
 			rectF.bottom += 3;
 			rectF.right += 3;
 			canvas.drawRoundRect(rectF, 5.0f, 5.0f, p);
 
 			p.setARGB(255, 0, 0, 246);
 			canvas.drawText(String.format("%.1fkm", dist[0]), txtBounds.left, txtBounds.bottom, p);
 		}
 
 		return false;
 	}
 
 
 	public boolean onTouchEvent(MotionEvent e, MapView mapView)
 	{
 		/* touching myLocation? */
 		if(e.getAction() == MotionEvent.ACTION_DOWN && !mJustMoved)
 		{
 			Settings settings = new Settings(mapView.getContext());
 			Point centerPoint = null;
 			mLastX = e.getX();
 			mLastY = e.getY();
 			if(!mCustomCenter) /* update center geo point from map */
 				mCenterGeoPoint = getCenterGeoPoint(mapView); /* the most-recently-set user location. */
 
 			if(mCenterGeoPoint != null)
 				centerPoint = mapView.getProjection().toPixels(mCenterGeoPoint, null);
 
 			if(centerPoint != null)
 			{
 				/* if we touch near the center, then set mMovingCenter to true: next moves will move the
 				 * center and the tapped point, i.e. the circle will be translated.
 				 */
 				if(mCurrentGeoPoint != null && 
 						Math.abs(e.getX() - centerPoint.x) < mCenterSensibilityDelta && 
 						Math.abs(e.getY() - centerPoint.y) < mCenterSensibilityDelta)
 				{
 					mMovingCenter = true;
 					mCustomCenter = true;
 					
 					/* disable move location center hint forever */
 					settings.setMapMoveLocationToMeasureHintEnabled(false);
 				}
 				else if(mCurrentGeoPoint == null)
 				{			
 					mMovingCenter = false;
 				}
 			}
 			/* disable map move hint forever */
 			settings.setMapMoveToMeasureHintEnabled(false);
 		}
 		else if(e.getAction() == MotionEvent.ACTION_UP && mJustMoved)
 		{
 			mJustMoved = false;
 			mMovingCenter = false;
 			/* do not do anything else */
 			return true;
 		}
 		else if(e.getAction() == MotionEvent.ACTION_MOVE) /* screen touched */
 		{			
 			mJustMoved = false;
 			if(mCurrentGeoPoint != null && mCenterGeoPoint != null)
 			{
 				float x = e.getX();
 				float y = e.getY();
 				float deltaX, deltaY;
 				deltaX = x - mLastX;
 				deltaY = y - mLastY;
 
 				/* center point in screen coordinates */
 				Point centerPoint = mapView.getProjection().toPixels(mCenterGeoPoint, null);
 				/* current point in screen coordinates */
 				Point currentPoint = mapView.getProjection().toPixels(mCurrentGeoPoint, null);
 				
 				if(mMovingCenter) /* translate center and tapped point */
 				{			
 					currentPoint.x += Math.round(deltaX);
 					currentPoint.y += Math.round(deltaY);			
 					centerPoint.x += Math.round(deltaX);
 					centerPoint.y += Math.round(deltaY);	
 					mJustMoved = true;
 				}
 				else
 				{
 					//Log.e("center current ", "center " + centerPoint + " current " + currentPoint);
 					/* calculate the square of the radiuses interval, augmented and diminished */
 					float squareRadius = (float) (Math.pow(centerPoint.x - currentPoint.x, 2) + Math.pow(centerPoint.y - currentPoint.y, 2));
 					float squareRadiusPlus = squareRadius + 0.6f * squareRadius;
 					float squareRadiusMinus = 0; //squareRadius - squareRadius / 2;
 
 					float circleSquareR = (float) Math.pow(x - centerPoint.x, 2) + (float) Math.pow(y - centerPoint.y, 2);
 
 					if(squareRadiusMinus <= circleSquareR && circleSquareR <= squareRadiusPlus)
 					{
 						/* if we touch near the arrow, then we can change orientation */
 						int sensibleDelta = (int) Math.round(Math.sqrt(squareRadius)/3);
 						//sensibleDelta = 40;
 						if(x < currentPoint.x + sensibleDelta && x > currentPoint.x - sensibleDelta 
 								&& y < currentPoint.y + sensibleDelta && y > currentPoint.y - sensibleDelta)
 						{
 							/* move the arrow according to the movement of the finger */
 							currentPoint.x += Math.round(deltaX);
 							currentPoint.y += Math.round(deltaY);
 						}
 						else /*  the radius grows or shrinks according to the gesture movement */
 						{
 							float delta;
 							float radius;
 							double alpha;
 							float oldRadius =  (float) Math.sqrt(squareRadius);
 							if(currentPoint.x < centerPoint.x) /* terzo o quarto quadrante [PI/2; 3/2PI ] */
 								alpha = (Math.asin(-(currentPoint.y - centerPoint.y)/ oldRadius) + Math.PI);
 							else
 								alpha =  Math.asin((currentPoint.y - centerPoint.y)/ oldRadius);
 
 
 							double squareLastTouchRadius = Math.pow(centerPoint.x - mLastX, 2) + Math.pow(centerPoint.y - mLastY, 2);
 							double squareNewTouchRadius = Math.pow(centerPoint.x - x, 2) + Math.pow(centerPoint.y - y, 2);
 
 							if(squareLastTouchRadius > squareNewTouchRadius) /* moved towards inside: diminish radius */
 							{
 								delta = (float) (Math.sqrt(squareLastTouchRadius) - Math.sqrt(squareNewTouchRadius));
 								radius = oldRadius - delta;
 							}
 							else
 							{
 								delta = (float) (Math.sqrt(squareNewTouchRadius) - Math.sqrt(squareLastTouchRadius));
 								radius = oldRadius + delta;
 							}
 							double newX =  (centerPoint.x + radius * Math.cos(alpha));
 							double newY =  (centerPoint.y + radius * Math.sin(alpha));
 
 							currentPoint.x = (int)Math.round(newX);
 							currentPoint.y = (int)Math.round(newY);
 						}
 						mJustMoved = true;
 					}
 				}
 				if(mJustMoved)
 				{
 					mLastX = x;
 					mLastY = y;
 
 					/* save center and current in geographical coordinates */
 					mCurrentGeoPoint = mapView.getProjection().fromPixels(currentPoint.x, currentPoint.y);
 					mCenterGeoPoint = mapView.getProjection().fromPixels(centerPoint.x, centerPoint.y);
 					return true;
 				}
 
 			}	
 		}
 		return false;
 	}
 
 	private GeoPoint getCenterGeoPoint(MapView mapView)
 	{
 		MyLocationOverlay myLocOv = ((OMapView)mapView).getMyLocationOverlay();
 		return myLocOv.getMyLocation(); /* the most-recently-set user location. */
 	}
 
 	private Point getCenterPoint(MapView mapView)
 	{
 		GeoPoint geoPoint = getCenterGeoPoint(mapView);
 		if(geoPoint != null)
 			return  mapView.getProjection().toPixels(geoPoint, null);
 		return null;
 	}
 
 	public Parcelable saveState(Bundle bundle)
 	{
 		bundle.putBoolean("customCenter", mCustomCenter);
		if(mCenterGeoPoint != null)
		{
			bundle.putInt("centerGeoPointLat", mCenterGeoPoint.getLatitudeE6());
			bundle.putInt("centerGeoPointLong", mCenterGeoPoint.getLongitudeE6());
		}
		if(mCurrentGeoPoint != null)
		{
			bundle.putInt("currentGeoPointLat", mCurrentGeoPoint.getLatitudeE6());
			bundle.putInt("currentGeoPointLong", mCurrentGeoPoint.getLongitudeE6());
		}
 		return bundle;
 	}
 
 	public void restoreState (Parcelable state)
 	{
 		Bundle b = (Bundle) state;
 		if(b.containsKey("customCenter"))
 			mCustomCenter = b.getBoolean("customCenter");
 		if(b.containsKey("centerGeoPointLat") && b.containsKey("centerGeoPointLong"))
 			mCenterGeoPoint = new GeoPoint(b.getInt("centerGeoPointLat"), b.getInt("centerGeoPointLong"));
 		if(b.containsKey("currentGeoPointLat") && b.containsKey("currentGeoPointLong"))
 			mCurrentGeoPoint = new GeoPoint(b.getInt("currentGeoPointLat"), b.getInt("currentGeoPointLong"));		
 	}
 	
 	private GeoPoint mCenterGeoPoint, mCurrentGeoPoint;
 	private boolean mJustMoved;
 	private boolean mMovingCenter;
 	private boolean mCustomCenter;
 	private boolean mIsActive;
 	private float mLastX, mLastY;
 	private final int mCenterSensibilityDelta = 40; /* pixel */
 }
 
 
