 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package net.epsilony.tsmf.util;
 
 import net.epsilony.tsmf.cons_law.ConstitutiveLaw;
 import net.epsilony.tsmf.cons_law.IsoElastic2D;
 
 /**
  *
  * @author epsilon
  */
 public class TimoshenkoAnalyticalBeam2D {
 
     double width, height;
     double E, nu;
     double P;
     double I;
 
     public TimoshenkoAnalyticalBeam2D(double width, double height, double E, double nu, double P) {
         this.width = width;
         this.height = height;
         this.E = E;
         this.nu = nu;
         this.P = P;
         I = Math.pow(height, 3) / 12;
     }
 
     public double getWidth() {
         return width;
     }
 
     public double getHeight() {
         return height;
     }
 
     public double getE() {
         return E;
     }
 
     public double getNu() {
         return nu;
     }
 
     public double getP() {
         return P;
     }
 
     public double[] displacement(double x, double y, int partDiffOrder, double[] results) {
         int resDim = 0;
         switch (partDiffOrder) {
             case 0:
                 resDim = 2;
                 break;
             case 1:
                 resDim = 6;
                 break;
             default:
                 throw new IllegalArgumentException("partDiffOrder should be 0 or 1, others are not supported");
         }
         if (null == results) {
             results = new double[resDim];
         } else {
             if (results.length < resDim) {
                 throw new IllegalArgumentException("When partDiffOrder is " + partDiffOrder + ", the results.lenght should >= " + resDim + ". Try to give a longer results all just give a null reference.");
             }
         }
         double D = height, L = width;
         double u = -P * y / (6 * E * I) * ((6 * L - 3 * x) * x + (2 + nu) * (y * y - D * D / 4));
         double v = P / (6 * E * I) * (3 * nu * y * y * (L - x) + (4 + 5 * nu) * D * D * x / 4 + (3 * L - x) * x * x);
         results[0] = u;
         results[1] = v;
         if (partDiffOrder > 0) {
             double u_x = -P * y * (L - x) / E / I;
             double u_y = P / E / I * (-(nu + 2) * y * y / 2 + (nu + 2) * D * D / 24 - L * x + x * x / 2);
             double v_x = P / E / I * (-nu * y * y / 2 + (4 + 5 * nu) * D * D / 24 + L * x - x * x / 2);
             double v_y = P / E / I * nu * y * (L - x);
             results[2] = u_x;
             results[3] = u_y;
             results[4] = v_x;
             results[5] = v_y;
         }
         return results;
     }
 
     public double[] strain(double x, double y, double results[]) {
         if (null == results) {
             results = new double[3];
         }
         double L = width;
         double D = height;
         double xx = -P * y * (L - x) / E / I;
         double yy = P / E / I * nu * y * (L - x);
         double xy = P / E / I * (1 + nu) * (D * D / 4 - y * y);
         results[0] = xx;
         results[1] = yy;
         results[2] = xy;
         return results;
     }
 
     public double[] stress(double x, double y, double results[]) {
         if (null == results) {
             results = new double[3];
         }
         double L = width;
         double D = height;
         double sxx = -P * (L - x) * y / I;
         double syy = 0;
         double sxy = P / (2 * I) * (D * D / 4.0 - y * y);
         results[0] = sxx;
         results[1] = syy;
         results[2] = sxy;
         return results;
     }
 
     public class NeumannFunction implements GenericFunction<double[], double[]> {
 
         @Override
         public double[] value(double[] input, double[] output) {
            return displacement(input[0], input[1], 0, input);
         }
     }
 
     public class DirichletFunction implements GenericFunction<double[], double[]> {
 
         @Override
         public double[] value(double[] input, double[] output) {
             double[] strVal = stress(input[0], input[1], null);
             if (output == null) {
                 output = new double[2];
             }
             output[0] = strVal[0];
             output[1] = strVal[2];
             return output;
         }
     }
 
     public class DirichletMarker implements GenericFunction<double[], boolean[]> {
 
         @Override
         public boolean[] value(double[] input, boolean[] output) {
             if (output == null) {
                 output = new boolean[2];
             }
             output[0] = true;
             output[1] = true;
             return output;
         }
     }
 
     public ConstitutiveLaw constitutiveLaw() {
         return IsoElastic2D.planeStressMatrix(E, nu);
     }
 }
