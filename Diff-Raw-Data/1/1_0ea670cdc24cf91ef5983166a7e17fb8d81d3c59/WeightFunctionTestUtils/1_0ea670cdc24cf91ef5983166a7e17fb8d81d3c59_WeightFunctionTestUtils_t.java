 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package net.epsilony.simpmeshfree.model2d.test;
 
 import gnu.trove.list.array.TDoubleArrayList;
 import static java.lang.Math.PI;
 import static java.lang.Math.ceil;
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 import net.epsilony.simpmeshfree.model.*;
 import net.epsilony.simpmeshfree.sfun2d.MLS;
 import net.epsilony.simpmeshfree.utils.BasesFunction;
 import net.epsilony.simpmeshfree.utils.Complete2DPolynomialBases;
 import net.epsilony.utils.geom.Coordinate;
 import net.epsilony.utils.geom.Node;
 import net.epsilony.utils.spfun.DistanceSquareFunctions;
 import net.epsilony.utils.spfun.ShapeFunction;
 import net.epsilony.utils.spfun.radialbasis.WeightFunction;
 import net.epsilony.utils.spfun.radialbasis.WeightFunctionCore;
 import net.epsilony.utils.spfun.radialbasis.WeightFunctions;
 
 /**
  *
  * @author epsilon
  */
 public class WeightFunctionTestUtils {
 
     public static List<Node> genNodes(double xMin, double yMin, double xMax, double yMax, double ndDistOrNum, boolean isDist) {
         return genNodes(xMin, yMin, xMax, yMax, ndDistOrNum, isDist, null);
     }
 
     public static List<Node> genNodes(double xMin, double yMin, double xMax, double yMax, double ndDistOrNum, boolean isDist, List<double[]> xys) {
         int numX, numY;
         double stepX, stepY;
         if (isDist) {
             numX = (int) ceil((xMax - xMin) / ndDistOrNum) + 1;
             numY = (int) ceil((yMax - yMin) / ndDistOrNum) + 1;
 
         } else {
             double w=xMax-xMin,h=yMax-yMin;
             numX = (int) ceil(Math.sqrt(ndDistOrNum*w/h));
             numY = (int) ceil(Math.sqrt(ndDistOrNum*h/w));
         }
         stepX = (xMax - xMin) / (numX - 1);
         stepY = (yMax - yMin) / (numY - 1);
         double[] xs = new double[numX];
         double[] ys = new double[numY];
         LinkedList<Node> result = new LinkedList<>();
         for (int i = 0; i < numX; i++) {
             double x = xMin + stepX * i;
             if (i == 0) {
                 x = xMin;
             } else if (i == numX - 1) {
                 x = xMax;
             }
             xs[i] = x;
             for (int j = 0; j < numY; j++) {
                 double y = yMin + stepY * j;
                 if (j == 0) {
                     y = yMin;
                 } else if (j == numY - 1) {
                     y = yMax;
                 }
                 result.add(new Node(x, y));
                 if (i == 0) {
                     ys[j] = y;
                 }
             }
         }
         if (null != xys) {
             xys.clear();
             xys.add(xs);
             xys.add(ys);
         }
         return result;
 
     }
 
     public static ShapeFunction genShapeFunction(int baseOrder, WeightFunctionCore coreFun) {
         BasesFunction baseFun = Complete2DPolynomialBases.complete2DPolynomialBase(baseOrder);
         WeightFunction weightFunction = WeightFunctions.weightFunction(coreFun);
         return new MLS(weightFunction, baseFun);
     }
 
     public interface ValueFun {
 
         double[] value(Coordinate c);
     }
 
     public static double[] value(TDoubleArrayList[] shapeFunVals, List<Node> nds, ValueFun fun) {
         int i = 0;
         double[] result = new double[3];
         for (Node nd : nds) {
             double[] funVs = fun.value(nd);
             for (int j = 0; j < 3; j++) {
                 result[j] += shapeFunVals[j].getQuick(i) * funVs[0];
             }
             i++;
         }
         return result;
     }
 
     public static void evalCase(Double rad,ShapeFunction shapeFun, ValueFun expFun, List<Node> nds, List<? extends Coordinate> centers, List<double[]> actResults) {
         shapeFun.setDiffOrder(1);
         ArrayList<Node> resNodes = new ArrayList<>(50);
         TDoubleArrayList rads=new TDoubleArrayList(1);
         rads.add(rad);
         actResults.clear();
         SupportDomainCritierion simpCriterion = SupportDomainUtils.simpCriterion(rad, nds);
        simpCriterion.setDiffOrder(shapeFun.getDiffOrder());
         TDoubleArrayList[] distSqs = DistanceSquareFunctions.initDistSqsContainer(2, 1);
         for (Coordinate center : centers) {
             simpCriterion.getSupports(center, null, resNodes, distSqs);
             TDoubleArrayList[] shapeFunVals = shapeFun.values(center, resNodes,null,rads,null);
             double[] actResult=value(shapeFunVals,resNodes,expFun);
             actResults.add(actResult);
         }
     }
 
     public static ValueFun genExpFun(String choice) {
         String ch = choice.toLowerCase();
         switch (ch) {
             case "c":
             case "cubic":
                 return new ValueFun() {
 
                     final double[] result = new double[3];
 
                     @Override
                     public double[] value(Coordinate c) {
                         double x = c.x, y = c.y;
                         result[0] = cubicTestFun(x, y);
                         result[1] = cubicXTestFun(x, y);
                         result[2] = cubicYTestFun(x, y);
                         return result;
                     }
                 };
             case "s":
             case "sin":
                 return new ValueFun() {
 
                     final double[] result = new double[3];
 
                     @Override
                     public double[] value(Coordinate c) {
                         double x = c.x, y = c.y;
                         result[0] = sinTestFun(x, y);
                         result[1] = sinXTestFun(x, y);
                         result[2] = sinYTestFun(x, y);
                         return result;
                     }
                 };
             default:
                 throw new IllegalArgumentException("Choice must come from \"c\", \"cubic\", \"s\" and \"sin\"");
         }
     }
 
     public static double cubicTestFun(double x, double y) {
         return 3.5 + 4.2 * x + 6.3 * y + 2.2 * x * x + 1.7 * x * y + 3.9 * y * y + 7.2 * x * x * x + 0.7 * x * x * y + 0.2 * x * y * y + y * y * y;
     }
 
     public static double cubicXTestFun(double x, double y) {
         return 4.2 + 4.4 * x + 1.7 * y + 21.6 * x * x + 1.4 * x * y + 0.2 * y * y;
     }
 
     public static double cubicYTestFun(double x, double y) {
         return 6.3 + 1.7 * x + 7.8 * y + 0.7 * x * x + 0.4 * x * y + 3 * y * y;
     }
 
     public static double sinTestFun(double x, double y) {
         return Math.sin(x / (2 * PI)) * Math.sin(y / (2 * PI));
     }
 
     public static double sinXTestFun(double x, double y) {
         return Math.cos(x / (2 * PI)) * Math.sin(y / (2 * PI)) / (2 * PI);
     }
 
     public static double sinYTestFun(double x, double y) {
         return Math.sin(x / (2 * PI)) * Math.cos(y / (2 * PI)) / (2 * PI);
     }
 }
