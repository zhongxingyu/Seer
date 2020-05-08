 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package SubwayMaker.Elements;
 
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.awt.geom.Path2D;
 import java.awt.geom.Point2D;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.ListIterator;
 import java.util.Random;
 
 /**
  *
  * @author Yichen
  */
 public class Track extends Element {
 
     public static final float DIAMETER_SCALE = 0.8f;
     public static final float TRACK_SCALE = 0.5f;
     private List<TrackPoint> trackPoints;
     private boolean complete;
     private Color trackColor;
 
     public Track() {
         complete = false;
         trackPoints = new ArrayList<>();
 
         Random random = new Random();
         trackColor = Color.getHSBColor(random.nextFloat(), (random.nextInt(1000) + 8000) / 10000f, (random.nextInt(1000) + 9000) / 10000f);
     }
 
     @Override
     public void add(Point2D pt, boolean mod) {
         pt = nearPoint(pt);
         if (trackPoints.isEmpty()) {
             trackPoints.add(new TrackPoint(pt));
         } else {
             trackPoints.add(new TrackPoint(pt, trackPoints.get(trackPoints.size() - 1), mod));
         }
     }
 
     @Override
     public void paint(Graphics2D g, int blocksize) {
         int diameter = Math.round(blocksize * DIAMETER_SCALE);
 
         Path2D track = new Path2D.Double();
         g.setColor(trackColor);
 
         ListIterator<TrackPoint> it = trackPoints.listIterator();
         if (it.hasNext()) {
             Point2D iniPt = pointScale(it.next(), blocksize);
             if (!complete) {
                g.fillOval((int) iniPt.getX() - diameter / 2, (int) iniPt.getY() - diameter / 2, diameter, diameter);
             }
             track.moveTo(iniPt.getX(), iniPt.getY());
 
             while (it.hasNext()) {
                 TrackPoint pt = it.next();
                 Point2D transPt = pointScale(pt, blocksize);
 
                 if (pt.isTurningPoint()) {
                     Point2D refPt = pointScale(pt.curveReference(), blocksize);
                     track.curveTo(refPt.getX(), refPt.getY(), refPt.getX(), refPt.getY(), transPt.getX(), transPt.getY());
                 } else {
                     track.lineTo(transPt.getX(), transPt.getY());
                 }
             }
         }
 
         g.setStroke(new BasicStroke(blocksize * TRACK_SCALE, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
         g.draw(track);
     }
 
     @Override
     public void complete() {
         complete = true;
     }
 
     private class TrackPoint extends Point2D.Double {
 
         private static final double DELTA = 0.000001;
         private boolean hort = true;
         private boolean turningPoint = false;
         private Point2D refPoint;
 
         /**
          * Create a new TrackPoint without direction or curve. <p>
          * Usually for the first point.
          * <p/>
          * @param endPoint The point for this track point.
          */
         TrackPoint(Point2D endPoint) {
             super(endPoint.getX(), endPoint.getY());
         }
 
         /**
          * Creates a new TrackPoint, automatically determining direction/curve
          * unless otherwise stated. <p>
          * To determine if there should be a curve, the function looks at the
          * previous point and evaluates if the new point is linear.
          * <p/>
          * @param endPoint  The point of this TrackPoint.
          * @param prevPoint The previous point to use as a reference.
          * @param mod       If the automatic direction/curve should be inverted.
          */
         TrackPoint(Point2D endPoint, TrackPoint prevPoint, boolean mod) {
             this(endPoint);
             turningPoint = !((Math.abs(prevPoint.getX() - x) < DELTA) || (Math.abs(prevPoint.getY() - y) < DELTA));
 
             if (turningPoint) {
                hort = !prevPoint.goingHorizontal();
 
                if (hort ^ mod) {
                     refPoint = new Point2D.Double(prevPoint.getX(), endPoint.getY());
                 } else {
                     refPoint = new Point2D.Double(endPoint.getX(), prevPoint.getY());
                 }
             } else {
                 hort = prevPoint.goingHorizontal();
                 if ((hort && (Math.abs(prevPoint.y - y) > DELTA)) || (!hort && (Math.abs(prevPoint.x - x) > DELTA))) {
                     hort = !hort;
                 }
             }
         }
 
         Point2D curveReference() {
             return refPoint;
         }
 
         boolean goingHorizontal() {
             return hort;
         }
 
         boolean isTurningPoint() {
             return turningPoint;
         }
     }
 }
