 /* (c) Copyright by Man YUAN */
 package net.epsilony.tb.implicit;
 
 import java.awt.Shape;
 import java.awt.geom.Ellipse2D;
 import net.epsilony.tb.solid.ArcSegment2D;
 import net.epsilony.tb.analysis.ArrvarFunction;
 import net.epsilony.tb.analysis.Math2D;
 import static java.lang.Math.PI;
 import static java.lang.Math.sin;
 import static java.lang.Math.cos;
 import net.epsilony.tb.analysis.DifferentiableFunction;
 import net.epsilony.tb.solid.Node;
 import net.epsilony.tb.solid.Segment2DUtils;
 
 /**
  *
  * @author <a href="mailto:epsilonyuan@gmail.com">Man YUAN</a>
  */
 public class CircleLevelSet implements ArrvarFunction, DifferentiableFunction<double[], double[]> {
 
     int diffOrder = 0;
     double radius;
     double centerX, centerY;
     boolean concrete = true;
 
     public CircleLevelSet(double centerX, double centerY, double radius) {
         this.radius = radius;
         this.centerX = centerX;
         this.centerY = centerY;
     }
 
     public CircleLevelSet() {
     }
 
     @Override
     public double value(double[] vec) {
         double result = radius * radius - Math2D.distanceSquare(vec[0], vec[1], centerX, centerY);
         if (!concrete) {
             result = -result;
         }
         return result;
     }
 
     public double getRadius() {
         return radius;
     }
 
     public void setRadius(double radius) {
         this.radius = radius;
     }
 
     public double getCenterX() {
         return centerX;
     }
 
     public void setCenterX(double centerX) {
         this.centerX = centerX;
     }
 
     public double getCenterY() {
         return centerY;
     }
 
     public void setCenterY(double centerY) {
         this.centerY = centerY;
     }
 
     public Shape genProfile() {
         return new Ellipse2D.Double(centerX - radius, centerY - radius, radius * 2, radius * 2);
     }
 
     public boolean isConcrete() {
         return concrete;
     }
 
     public void setConcrete(boolean concrete) {
         this.concrete = concrete;
     }
 
     @Override
     public double[] value(double[] input, double[] output) {
         if (null == output) {
            output = new double[2 * diffOrder + 1];
         }
         double v = value(input);
         output[0] = v;
         if (diffOrder >= 1) {
             double x = input[0];
             double y = input[1];
             if (concrete) {
                output[1] = -2 * (x - centerX);
                output[2] = -2 * (y - centerY);
             } else {
                output[1] = 2 * (x - centerX);
                output[2] = 2 * (y - centerY);
             }
         }
         return output;
     }
 
     public ArcSegment2D toArcs(double arcLengthSup) {
         double perimeter = 2 * PI * radius;
         int arcNum = (int) Math.ceil(perimeter / 4 / arcLengthSup) * 4;
         if (arcNum < 4) {
             arcNum = 4;
         }
         double deltaAmpAngle = 2 * PI / arcNum;
         if (!concrete) {
             deltaAmpAngle = -deltaAmpAngle;
         }
         ArcSegment2D arc = new ArcSegment2D();
         ArcSegment2D headArc = arc;
         int i = 0;
         while (i < arcNum) {
             double ampAngle = deltaAmpAngle * i;
             double arcX = radius * cos(ampAngle) + centerX;
             double arcY = radius * sin(ampAngle) + centerY;
             arc.setStart(new Node(arcX, arcY));
             arc.setRadius(radius);
             arc.setCenterOnChordLeft(concrete);
             ArcSegment2D succArc;
             if (i != arcNum - 1) {
                 succArc = new ArcSegment2D();
             } else {
                 succArc = headArc;
             }
             Segment2DUtils.link(arc, succArc);
             arc = succArc;
             i++;
         }
         return headArc;
     }
 
     @Override
     public int getInputDimension() {
         return 2;
     }
 
     @Override
     public int getOutputDimension() {
         return 1;
     }
 
     @Override
     public int getDiffOrder() {
         return diffOrder;
     }
 
     @Override
     public void setDiffOrder(int diffOrder) {
         if (diffOrder < 0 || diffOrder > 1) {
             throw new IllegalArgumentException("only support 0 or 1, not " + diffOrder);
         }
         this.diffOrder = diffOrder;
     }
 }
