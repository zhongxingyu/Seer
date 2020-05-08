 /*
  *  This file is part of Pac Defence.
  *
  *  Pac Defence is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  Pac Defence is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with Pac Defence.  If not, see <http://www.gnu.org/licenses/>.
  *  
  *  (C) Liam Byrne, 2008.
  */
 
 package logic;
 
 import java.awt.Polygon;
 import java.awt.geom.Arc2D;
 import java.awt.geom.Line2D;
 import java.awt.geom.Point2D;
 import java.awt.geom.Rectangle2D;
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 
 public class Helper {
 
    private static final Map<Integer, DecimalFormat> formats =
          new HashMap<Integer, DecimalFormat>();
    
    private static final double twoPi = 2 * Math.PI;
    
    public static double distanceSq(Point2D p1, Point2D p2) {
       return Point2D.distanceSq(p1.getX(), p1.getY(), p2.getX(), p2.getY());
    }
    
    public static List<Point2D> getPointsOnLine(Line2D line) {
       return getPointsOnLine(line.getP1(), line.getP2());
    }
    
    public static List<Point2D> getPointsOnLine(Point2D p1, Point2D p2) {
       return getPointsOnLine(p1, p2, null);
    }
    
    public static List<Point2D> getPointsOnLine(Point2D p1, Point2D p2, Rectangle2D containedIn) {
       double dx = p2.getX() - p1.getX();
       double dy = p2.getY() - p1.getY();
       // The maximum length in either the x or y directions to divide the line
       // into points a maximum of one pixel apart in either the x or y directions
       double absDx = Math.abs(dx);
       double absDy = Math.abs(dy);
       double length = (absDx > absDy) ? absDx : absDy;
       double xStep = dx / length;
       double yStep = dy / length;
       List<Point2D> points = new ArrayList<Point2D>((int) length + 2);
       if(containedIn == null || containedIn.contains(p1)) {
          points.add((Point2D) p1.clone());
       }
       Point2D lastPoint = p1;
       for(int i = 1; i <= length; i++) {
          lastPoint = new Point2D.Double(lastPoint.getX() + xStep, lastPoint.getY() + yStep);
          if(containedIn == null || containedIn.contains(lastPoint)) {
             points.add(lastPoint);
          }
       }
       if(containedIn == null || containedIn.contains(p2)) {
          points.add((Point2D) p2.clone());
       }
       return points;
    }
    
    public static <T> List<T> makeListContaining(T... ts) {
       return new ArrayList<T>(Arrays.asList(ts));
    }
    
    public static int increaseByAtLeastOne(int currentValue, double factor) {
       int plus = currentValue + 1;
       int times = (int)(currentValue * factor);
       return plus > times ? plus : times;
    }
    
    public static void removeAll(List<?> list, List<Integer> positions) {
       // Assumes the list of Integers is sorted smallest to largest
       
       // Removes from last to first as it is faster in an array backed list
       // which is what I usually use.
       for(int i = positions.size() - 1; i >= 0; i--) {
          // Convert to int otherwise it calls list.remove(Object o)
          list.remove(positions.get(i).intValue());
       }
    }
    
    public static String format(Double d, int decimalPlaces) {
       assert decimalPlaces >= 0 : "decimalPlaces must be >= 0";
       if(!formats.containsKey(decimalPlaces)) {
          formats.put(decimalPlaces, makeFormat(decimalPlaces));
       }
       return formats.get(decimalPlaces).format(d);
    }
 
    private static DecimalFormat makeFormat(int decimalPlaces) {
       assert decimalPlaces >= 0 : "decimalPlaces must be >= 0";
       StringBuilder pattern = new StringBuilder("#0");
       if(decimalPlaces > 0) {
          pattern.append(".");
          for(int i = 0; i < decimalPlaces; i++) {
             pattern.append("0");
          }
       }
       return new DecimalFormat(pattern.toString());
    }
    
    public static List<Point2D> getPointsOnArc(Arc2D a) {
       return getPointsOnArc(a, null);
    }
    
    public static List<Point2D> getPointsOnArc(Arc2D a, Rectangle2D containedIn) {
       double radius = a.getStartPoint().distance(a.getCenterX(), a.getCenterY());
       /*double circumference = 2 * Math.PI * radius;
       double numPoints = circumference * a.getAngleExtent() / 360;
       double deltaAngle = Math.toRadians(a.getAngleExtent() / numPoints);*/
       // Left the above code in as it's easier to understand, though the two
       // lines below should be faster and do the same thing.
       double numPoints = 2 * Math.PI * radius * a.getAngleExtent() / 360;
       double deltaAngle = 1 / radius;
       double angle = Math.toRadians(a.getAngleStart() + 90);
       List<Point2D> points = new ArrayList<Point2D>((int) numPoints + 3);
       Point2D p;
       double x = a.getCenterX();
       double y = a.getCenterY();
       // Can premultiply by the radius which saves having to do it at each iteration
       double sinAngle = Math.sin(angle) * radius;
       double cosAngle = Math.cos(angle) * radius;
       double sinDeltaAngle = Math.sin(deltaAngle);
       double cosDeltaAngle = Math.cos(deltaAngle);
       for(int i = 0; i < numPoints + 1; i++) {
          //angle += deltaAngle;
          //p = new Point2D.Double(x + radius * Math.sin(angle), y + radius * Math.cos(angle));
          // These next few lines do the same as the above, just using a trig identity
          // for better performance as there are no more trig calls
          p = new Point2D.Double(x + sinAngle, y + cosAngle);
          if(containedIn == null || containedIn.contains(p)) {
             points.add(p);
          }
          // Having these after means the first point is the first point on the arc
          double newSinAngle = sinAngle * cosDeltaAngle + cosAngle * sinDeltaAngle;
          cosAngle = cosAngle * cosDeltaAngle - sinAngle * sinDeltaAngle;
          sinAngle = newSinAngle;
       }
       p = a.getEndPoint();
       if(containedIn == null || containedIn.contains(p)) {
          points.add(p);
       }
       return points;
    }
    
    public static List<Point2D> getPointsOnArc(double x, double y, double radius,
          double numPoints, double sinAngle, double cosAngle, Rectangle2D containingRect) {
       // Copied from the one above with a lot of optimisations for wave and beam towers
       // as the arcs are the same except with different radii. Gave an ~20% improvement
       // in a simple test for beam tower.
       double deltaAngle = 1 / radius;
       double sinDeltaAngle = Math.sin(deltaAngle);
       double cosDeltaAngle = Math.cos(deltaAngle);
       sinAngle *= radius;
       cosAngle *= radius;
       List<Point2D> points = new ArrayList<Point2D>((int)numPoints + 3);
       for(int i = 0; i <= numPoints + 1; i++) {
          Point2D p = new Point2D.Double(x + sinAngle, y + cosAngle);
          if(containingRect.contains(p)) {
             points.add(p);
          }
          double newSinAngle = sinAngle * cosDeltaAngle + cosAngle * sinDeltaAngle;
          cosAngle = cosAngle * cosDeltaAngle - sinAngle * sinDeltaAngle;
          sinAngle = newSinAngle;
       }
       return points;
    }
    
    public static List<Line2D> getPolygonOutline(Polygon p) {
       int[] xPoints = p.xpoints;
       int[] yPoints = p.ypoints;
      int length = xPoints.length;
       List<Line2D> outline = new ArrayList<Line2D>(length);
       outline.add(new Line2D.Double(xPoints[length - 1], yPoints[length - 1], xPoints[0],
             yPoints[0]));
       for(int i = 1; i < length; i++) {
          outline.add(new Line2D.Double(xPoints[i - 1], yPoints[i - 1], xPoints[i], yPoints[i]));
       }
       return outline;
    }
    
    public static double reduceAngle(double angle) {
       while(angle > Math.PI) {
          angle -= twoPi;
       }
       while(angle < -Math.PI) {
          angle -= twoPi;
       }
       return angle;
    }
    
    public static void main(String[] args) {
       for(int a = 0; a < 5; a++) {
          long beginTime = System.nanoTime();
          Arc2D arc = new Arc2D.Double();
          for(int i = 0; i < 200; i++) {
             arc.setArc(0.0, 0.0, i * i, i * i, 10.0, 10.0, Arc2D.OPEN);
             getPointsOnArc(arc);
          }
          System.out.println(System.nanoTime() - beginTime);
       }
    }
 }
