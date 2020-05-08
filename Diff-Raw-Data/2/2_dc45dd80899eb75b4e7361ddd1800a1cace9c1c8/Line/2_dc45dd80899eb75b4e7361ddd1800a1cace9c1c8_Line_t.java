 /* (c) Copyright by Man YUAN */
 package net.epsilony.tb.solid;
 
 import net.epsilony.tb.Factory;
 import net.epsilony.tb.analysis.Math2D;
 
 /**
  *
  * @author <a href="mailto:epsilonyuan@gmail.com">Man YUAN</a>
  */
 public class Line extends RawSegment {
 
     public Line() {
     }
 
     public Line(Node start) {
         super(start);
     }
 
     @Override
     public void bisect() {
         Line newSucc = new Line();
         newSucc.setStart(bisectionNode());
         newSucc.succ = this.succ;
         newSucc.pred = this;
         this.succ.setPred(newSucc);
         this.succ = newSucc;
     }
 
     protected Node bisectionNode() {
         Node newNode = Node.instanceByClass(start);
         newNode.setCoord(Segment2DUtils.chordMidPoint(this, null));
         return newNode;
     }
 
     @Override
     public String toString() {
         String endStr = (null == succ || null == getEnd()) ? "NULL" : getEnd().toString();
         String startStr = (null == start) ? "NULL" : start.toString();
         return String.format("Line2D(%d)[h:(%s), r:(%s)]", id, startStr, endStr);
     }
 
     public double length() {
         return Math2D.distance(getStart().getCoord(), getEnd().getCoord());
     }
 
     @Override
     public double[] values(double t, double[] results) {
         if (null == results) {
            results = new double[(diffOrder + 1) * 2];
         }
         double[] startCoord = getStart().getCoord();
         double[] endCoord = getEnd().getCoord();
         Math2D.pointOnSegment(startCoord, endCoord, t, results);
         if (diffOrder >= 1) {
             results[2] = endCoord[0] - startCoord[0];
             results[3] = endCoord[1] - startCoord[1];
         }
         return results;
     }
 
     public void fractionize(int num, Factory<? extends Node> nodeFactory) {
         if (num == 1) {
             return;
         }
         if (num < 1) {
             throw new IllegalArgumentException();
         }
         double lengthStep = length() / num;
         double[] deltaVector = Math2D.subs(getEndCoord(), getStartCoord(), null);
         deltaVector = Math2D.normalize(deltaVector, deltaVector);
         deltaVector = Math2D.scale(deltaVector, lengthStep, deltaVector);
         Segment formerSucc = getSucc();
 
         Line last = this;
         for (int i = 0; i < num - 1; i++) {
             double[] coord = Math2D.adds(getStartCoord(), 1, deltaVector, i + 1, null);
             Node nd = nodeFactory.produce();
             nd.setCoord(coord);
             Line newSucc = new Line(nd);
             newSucc.setParent(parent);
             Segment2DUtils.link(last, newSucc);
             last = newSucc;
         }
         Segment2DUtils.link(last, formerSucc);
     }
 }
