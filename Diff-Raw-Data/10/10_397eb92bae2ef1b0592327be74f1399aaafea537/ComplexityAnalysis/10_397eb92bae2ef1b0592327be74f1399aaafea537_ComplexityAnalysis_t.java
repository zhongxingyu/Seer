 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package fi.lolcatz.profiler;
 
 import java.io.File;
 import java.io.IOException;
 import java.lang.Math.*;
 import java.util.Random;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.jfree.chart.ChartUtilities;
 
 public class ComplexityAnalysis {
 
     private ComplexityAnalysis() {
     }
     
     /*
      * add asserts for linear etc methods
      * e.g. assertLinear ... if (!isLinear()) throw new AssertionError  (<-- same exception that junit uses)
      */
 
     /**
      * Calculates the linearity of the parameter output
      *
      * @return True if the output is linear, false if the output is not linear
      */
     public static boolean isLinear(Output<?> output) {
         if (output.getSize().size() < 2) {
             return false;
         }
         Integer x0 = output.getSize().get(0);
         Long y0 = output.getTime().get(0);
         Integer xn = output.getSize().get(output.getSize().size() - 1);
         Long yn = output.getTime().get(output.getTime().size() - 1);
         double a = Math.abs(y0 - yn) / Math.abs(x0 - xn);
         double b = y0 - a * x0;
         for (int i = 0; i < output.getTime().size(); i++) {
             double time = output.getTime().get(i);
             double function = a * output.getSize().get(i) + b;
            boolean linearity = (time * 1.002 >= function && time - (time * 0.002) <= function) ? true : false;
             if (!linearity) {
                 return false;
             }
         }
         return true;
     }
 
     /**
      * Calculates whether the parameter output is O(n*n)
      *
      * @return True if output is squared, false if not
      */
     public static boolean isQuadric(Output<?> out) {
         if (out.getSize().size() < 2) {
             return false;
         }
         Integer x1 = out.getSize().get(0);
         Integer x2 = out.getSize().get(1);
         Integer x3 = out.getSize().get(2);
 
         long y1 = out.getTime().get(0);
         long y2 = out.getTime().get(1);
         long y3 = out.getTime().get(2);
 
         double denom = (x1 - x2) * (x1 - x3) * (x2 - x3);
         double a = (x3 * (y2 - y1) + x2 * (y1 - y3) + x1 * (y3 - y2)) / denom;
         double b = (x3 * x3 * (y1 - y2) + x2 * x2 * (y3 - y1) + x1 * x1 * (y2 - y3)) / denom;
         double c = (x2 * x3 * (x2 - x3) * y1 + x3 * x1 * (x3 - x1) * y2 + x1 * x2 * (x1 - x2) * y3) / denom;
 
         for (int i = 0; i < out.getTime().size(); i++) {
             double time = out.getTime().get(i);
             double function = (a * out.getSize().get(i) * out.getSize().get(i)) + (b * out.getSize().get(i)) + c;
             boolean exponential = (time * 1.002 >= function && time - (time * 0.002) <= function) ? true : false;
             if (!exponential) {
                 return false;
             }
         }
 
         return true;
     }
 
     /**
      * Calculates whether the class variable output is O(NlogN)
      *
      * @return True if output is O(NlogN), false if not
      */
     public static boolean isNlogN(Output<?> out) {
         if (out.getSize().size() < 2) {
             return false;
         }
         Integer x1 = out.getSize().get(0);
         Integer x2 = out.getSize().get(1);
         Integer x3 = out.getSize().get(2);
 
         long y1 = out.getTime().get(0);
         long y2 = out.getTime().get(1);
         long y3 = out.getTime().get(2);
 
         double denom = (x1 - x2) * (x1 - x3) * (x2 - x3);
         double a = (x3 * (y2 - y1) + x2 * (y1 - y3) + x1 * (y3 - y2)) / denom;
         double b = (x3 * (Math.log(x3) / Math.log(2)) * (y1 - y2) + x2 * (Math.log(x2) / Math.log(2)) * (y3 - y1) + x1 * (Math.log(x1) / Math.log(2)) * (y2 - y3)) / denom;
         double c = (x2 * x3 * (x2 - x3) * y1 + x3 * x1 * (x3 - x1) * y2 + x1 * x2 * (x1 - x2) * y3) / denom;
 
         for (int i = 0; i < out.getTime().size(); i++) {
             double time = out.getTime().get(i);
             double function = (a * out.getSize().get(i) * (Math.log(out.getSize().get(i)) / Math.log(2))) + (b * out.getSize().get(i)) + c;
             System.out.println("function: " + a + "*" + out.getSize().get(i) * (Math.log(out.getSize().get(i)) / Math.log(2)) + "+" + b + "*" + out.getSize().get(i) + "+" + c + " = " + function);
             boolean nlogn = (time * 1.004 >= function && time - (time * 0.004) <= function) ? true : false;
             if (!nlogn) {
                 return false;
             }
         }
 
         return true;
     }
 
     /**
      * Draws a runtime chart of the outputs size and time lists and saves the chart to a new .png file.
      *
      * @param actual The tested output
      * @param projected The example output
      */
     public static void drawGraph(Output<?> actual, Output<?> projected) {
         //generate example outputs for one param
         //linear, quadric ,nlogn
         Graph g = new Graph("Test", actual, projected);
         Random r = new Random();
         File f = new File("Graphs" + r.nextInt());
         try {
             ChartUtilities.saveChartAsPNG(f, g.getChart(), 500, 270);
         } catch (IOException ex) {
             Logger.getLogger(ComplexityAnalysis.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 }
