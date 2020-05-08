 /*
  * Copyright (C) 2012 Michel Sébastien & Jérémy Compostella
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.oux.SmartGPSLogger;
 
 import com.google.android.maps.Overlay;
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.Projection;
 import android.graphics.Point;
 import android.graphics.Path;
 import android.graphics.Paint;
 import android.graphics.Color;
 import android.graphics.Canvas;
 import com.google.android.maps.MapView;
 import android.location.Location;
 import java.util.LinkedList;
 import java.util.ListIterator;
 import android.view.MotionEvent;
 
 public class PathOverlay extends Overlay implements LocationUpdate
 {
     private LinkedList<GeoPoint> points = new LinkedList<GeoPoint>();
 
     private MapView mapView;
     private Paint pathPaint;
     private Paint pointPaint;
     private long prevTime;
 
     public PathOverlay(LinkedList<Location> locations)
     {
         ListIterator<Location> it = locations.listIterator(0);
         while (it.hasNext()) {
             Location cur = it.next();
             points.add(new GeoPoint((int)(cur.getLatitude() * 1E6),
                                     (int)(cur.getLongitude() * 1E6)));
         }
 
         pathPaint = new Paint();
         pathPaint.setDither(true);
         pathPaint.setColor(Color.RED);
         pathPaint.setStyle(Paint.Style.STROKE);
         pathPaint.setStrokeJoin(Paint.Join.ROUND);
         pathPaint.setStrokeCap(Paint.Cap.ROUND);
         pathPaint.setStrokeWidth(2);
         pathPaint.setAntiAlias(true);
         pathPaint.setAlpha(50);
 
         pointPaint = new Paint();
         pointPaint.setColor(Color.BLUE);
         pointPaint.setStyle(Paint.Style.FILL_AND_STROKE);
         pointPaint.setStrokeWidth(10);
         pointPaint.setStrokeCap(Paint.Cap.ROUND);
         pointPaint.setAntiAlias(true);
     }
 
     public void newLocation(Location loc)
     {
         if (loc == null)
             return;
 
         GeoPoint current = new GeoPoint((int)(loc.getLatitude() * 1E6),
                                         (int)(loc.getLongitude() * 1E6));
         points.add(current);
         if (mapView != null) {
             mapView.getController().animateTo(current);
             mapView.postInvalidate();
         }
     }
 
     @Override
     public void draw(Canvas canvas, MapView mapView, boolean shadow)
     {
         super.draw(canvas, mapView, shadow);
         this.mapView = mapView;
 
         Projection projection = mapView.getProjection();
 
         if (points.size() == 0)
             return;
         if (points.size() == 1) {
             Point p = new Point();
             GeoPoint gP = points.getFirst();
             projection.toPixels(gP, p);
             canvas.drawPoint(p.x, p.y, pointPaint);
             return;
         }
 
         Path path = new Path();
         GeoPoint gP1 = points.getFirst();
         Point p1 = new Point();
         Point p2 = new Point();
         ListIterator<GeoPoint> it = points.listIterator(1);
         while (it.hasNext()) {
             GeoPoint gP2 = it.next();
 
             projection.toPixels(gP1, p1);
             projection.toPixels(gP2, p2);
 
             path.moveTo(p2.x, p2.y);
             path.lineTo(p1.x, p1.y);
 
             gP1 = gP2;
         }
 
         canvas.drawPath(path, pathPaint);
         canvas.drawPoint(p2.x, p2.y, pointPaint);
     }
 
     public boolean onTouchEvent(MotionEvent e, MapView mapView)
     {
         if (e.getPointerCount() == 1 && e.getAction() == MotionEvent.ACTION_DOWN) {
             long curTime = System.currentTimeMillis();
            if (curTime - prevTime < 1000 && points.getLast() != null)
                 mapView.getController().animateTo(points.getLast());
             prevTime = curTime;
         }
         return super.onTouchEvent(e, mapView);
     }
 }
