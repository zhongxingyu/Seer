 /* (c) Copyright by Man YUAN */
 package net.epsilony.tb.quadrature;
 
 import java.util.Iterator;
 import net.epsilony.tb.analysis.ArrvarFunction;
 import net.epsilony.tb.analysis.Math2D;
 import net.epsilony.tb.solid.Node;
 import net.epsilony.tb.solid.winged.Triangle;
 
 /**
  *
  * @author <a href="mailto:epsilonyuan@gmail.com">Man YUAN</a>
  */
 public class SymmetricTriangleQuadrature implements Iterable<QuadraturePoint> {
 
     Triangle<? extends Node> triangle;
     int degree = 2;
 
     public int getNumberOfQuadraturePoints() {
         return SymmetricTriangleQuadratureUtils.numPointsByAlgebraicAccuracy(degree);
     }
 
     public void setTriangle(Triangle<? extends Node> triangle) {
         this.triangle = triangle;
     }
 
     public Triangle getTriangle() {
         return triangle;
     }
 
     public void setDegree(int degree) {
         this.degree = degree;
     }
 
     public int getDegree() {
         return degree;
     }
 
     public QuadraturePoint getQuadraturePoint(int index) {
        QuadraturePoint result = new QuadraturePoint();
         SymmetricTriangleQuadratureUtils.cartesianCoordinate(triangle, degree, index, result.coord);
         result.weight = Math2D.triangleArea(triangle) * SymmetricTriangleQuadratureUtils.getWeight(degree, index);
         return result;
     }
 
     public double quadrate(ArrvarFunction fun) {
         double result = 0;
         for (QuadraturePoint qp : this) {
             result += qp.weight * fun.value(qp.coord);
         }
         return result;
     }
 
     @Override
     public Iterator<QuadraturePoint> iterator() {
 
         return new Iterator<QuadraturePoint>() {
             int index = 0;
 
             @Override
             public boolean hasNext() {
                 return index < getNumberOfQuadraturePoints();
             }
 
             @Override
             public QuadraturePoint next() {
                 return getQuadraturePoint(index++);
             }
 
             @Override
             public void remove() {
                 throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
             }
         };
     }
 }
