 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package org.transketch.core.network.corridor;
 
 import java.awt.Shape;
 import java.awt.geom.Line2D;
 import java.awt.geom.Point2D;
 
 /**
  *
  * @author demory
  */
 public class SegmentComponent extends CorridorComponent {
 
   private Line2D segment_;
 
   public SegmentComponent(Point2D from, Point2D to) {
     segment_ = new Line2D.Double(from, to);
   }
   
   public Type getType() {
     return Type.SEGMENT;
   }
 
   public Line2D getSegment() {
     return segment_;
   }
 
   public Shape getShape() {
     return getSegment();
   }
 
   public Point2D getFrom() {
     return segment_.getP1();
   }
 
   public Point2D getTo() {
     return segment_.getP2();
   }
 
   public Point2D intersect(CorridorComponent comp) {
     throw new UnsupportedOperationException("Not supported yet.");
   }
 
   public Shape truncateTo(CorridorComponent comp) {
     if(comp.getType() == Type.SEGMENT) {
       Point2D isect = ComponentIntersector.segmentSegmentIntersection(this, (SegmentComponent) comp);
       if(isect != null) return new Line2D.Double(segment_.getP1(), isect);
     }
     return getSegment();
   }
 
   public Shape truncateFrom(CorridorComponent comp) {
     if(comp.getType() == Type.SEGMENT) {
       Point2D isect = ComponentIntersector.segmentSegmentIntersection(this, (SegmentComponent) comp);
       if(isect != null) return new Line2D.Double(isect, segment_.getP2());
     }
     return getSegment();
   }
 
 }
