 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package org.transketch.core.network.corridor;
 
 import java.awt.geom.Point2D;
 
 /**
  *
  * @author demory
  */
 public class ComponentIntersector {
 
   public static Point2D segmentSegmentIntersection(SegmentComponent seg1, SegmentComponent seg2) {
 
     double x1 = seg1.getFrom().getX(), y1 = seg1.getFrom().getY();
     double x2 = seg1.getTo().getX(), y2 = seg1.getTo().getY();
     double x3 = seg2.getFrom().getX(), y3 = seg2.getFrom().getY();
     double x4 = seg2.getTo().getX(), y4 = seg2.getTo().getY();
 
     double denom  = (y4-y3) * (x2-x1) - (x4-x3) * (y2-y1);
     double ua = ((x4-x3) * (y1-y3) - (y4-y3) * (x1-x3)) / denom;
     double ub = ((x2-x1) * (y1-y3) - (y2-y1) * (x1-x3)) / denom;
 
    if (ua < 0 || ua > 1 || ub < 0 || ub > 1) return null;

     return new Point2D.Double(x1 + ua * (x2 - x1),  y1 + ua * (y2 - y1));
   }
 
   public static Point2D arcSegmentIntersection(ArcComponent arc, SegmentComponent seg) {
     return null;
   }
 
   public static Point2D ArcArcIntersection(ArcComponent arc1, ArcComponent arc2) {
     return null;
   }
 
 }
