 /* ***** BEGIN LICENSE BLOCK *****
  * 
  * Copyright (c) 2011 Colin J. Fuller
  * 
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  * 
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  * 
  * ***** END LICENSE BLOCK ***** */
 
 package edu.stanford.cfuller.imageanalysistools.fitting;
 
 
 import org.apache.commons.math.analysis.MultivariateRealFunction;
 import org.apache.commons.math.linear.ArrayRealVector;
 import org.apache.commons.math.linear.RealVector;import java.lang.Math;
 
 /**
  * Performs a three-dimensional gaussian fit to an object in an image using a maximum likelihood method assuming Poisson
  * distributed pixel intensities (once converted to units of photons).
  * 
  * @author Colin J. Fuller
  *
  */
 public class GaussianFitter3D {
 	
     static RealVector logFactorialLookups;
 
     static int maxLogFactorialPrecalculated;
 
     static {
     	    	
         synchronized (GaussianFitter3D.class) {
 
             logFactorialLookups = new ArrayRealVector(65536, 0.0);
             maxLogFactorialPrecalculated = -1;
 
         }
     }
 
 
     /**
      * Fits a 3D Gaussian to a supplied object, starting from an initial guess of the parameters of that Gaussian.
      *
      * The Gaussian is contrained to be symmetric in the x and y dimensions (that is, it will have equal variance in both dimensions).
      *
      * @param toFit         The {@link ImageObject} to be fit to a Gaussian.
      * @param initialGuess  The initial guess at the parameters of the Gaussian.  These must be supplied in the order: amplitude, x-y stddev, z stddev, x position, y position, z position, background.  Positions should be supplied in absolute coordinates from the original image, not relative to the box around the object being fit.
      * @param ppg           The number of photons corresponding to one greylevel in the original image.
      * @return              The best fit Gaussian parameters, in the same order as the initial guess had to be supplied.
      */
     public RealVector fit(ImageObject toFit, RealVector initialGuess, double ppg) {
 
         //parameter ordering: amplitude, stddev x-y, stddev z, x/y/z coords, background
 
         //System.out.println("for object " + toFit.getLabel() + " in image: " + toFit.getImageID() + " initial guess is: " + initialGuess.toString());
 
 
         double tol = 1.0e-6;
         NelderMeadMinimizer nmm = new NelderMeadMinimizer(tol);
         
         RealVector result = nmm.optimize(new MLObjectiveFunction(toFit, ppg), initialGuess);
        
         result = nmm.optimize(new MLObjectiveFunction(toFit, ppg), result);
        //System.out.println("for object " + toFit.getLabel() + " in image: " + toFit.getImageID() + " result is: " + result.toString());
 
         return result;
 
     }
 
     private static double gaussian(double x, double y, double z, double[] parameters) {
 
         double xmx0 = x-parameters[3];
         double ymy0 = y-parameters[4];
         double zmz0 = z-parameters[5];
         double vxy = parameters[1];
         double vz = parameters[2];
         double A = parameters[0];
         double b = parameters[6];
 
 
 
         return ( A * Math.exp(-(xmx0*xmx0 + ymy0*ymy0)/(2*vxy*vxy) - zmz0*zmz0/(2*vz*vz)) + b);
 
     }
 
     private static double poissonProb(double x, double y, double z, double[] parameters, double n) {
     	
         double mean = gaussian(x,y,z,parameters);
 
         return (n*Math.log(mean) - mean - logFactorial((int) Math.round(n)+1));
             
     }
 
     private static double logLikelihood(ImageObject toFit, double[] parameters, double ppg) {
         double logLikelihood = 0;
 
         double[] x = toFit.getxValues();
         double[] y = toFit.getyValues();
         double[] z = toFit.getzValues();
         double[] f = toFit.getFunctionValues();
          
         for (int i = 0; i < x.length; i++) {
 
             logLikelihood -= poissonProb(x[i], y[i], z[i], parameters, f[i]);
 
         }
 
         return logLikelihood;
     }
     
     private class MLObjectiveFunction implements ObjectiveFunction, MultivariateRealFunction {
 
 
         private ImageObject toFit;
         private double ppg;
         public MLObjectiveFunction(ImageObject toFit, double ppg) {
             this.toFit = toFit;
             this.ppg = ppg;
         }
 
         public double evaluate(RealVector parameters) {
 
             return value(parameters.getData());
 
         }
 
         public double value(double[] parameters) {
             return logLikelihood(this.toFit, parameters, this.ppg);
         }
 
     }
 
 
     /**
      * Calculates the fit residual between a set of parameters and a value at a supplied point.
      * @param value         The observed value at the point.
      * @param x             The x-coordinate of the point (in absolute original image coordinates)
      * @param y             The y-coordinate of the point (in absolute original image coordinates)
      * @param z             The z-coordinate of the point (in absolute original image coordinates.
      * @param parameters    The gaussian parameters in the same order as would be required for or returned from {@link #fit}
      * @return              The fit residual at the specified point.
      */
     public static double fitResidual(double value, double x, double y, double z, RealVector parameters) {
 
         return value - gaussian(x,y,z,parameters.getData());
     }
 
     private static double logFactorial(int n) {
 
         if (n < 0) return 0;
 
         if (n <= maxLogFactorialPrecalculated) {
             return logFactorialLookups.getEntry(n);
         }
 
         synchronized (GaussianFitter3D.class) {
 
             if (n > maxLogFactorialPrecalculated) {
 
                 if (n >= logFactorialLookups.getDimension()) {
 
                     int sizeDiff = n+1 - logFactorialLookups.getDimension();
 
                     RealVector toAppend = new ArrayRealVector(sizeDiff, 0.0);
 
                     RealVector newLookups = logFactorialLookups.append(toAppend);
 
                     logFactorialLookups = newLookups;
 
                 }
 
                 for (int i = maxLogFactorialPrecalculated + 1; i <= n; i++) {
 
                     if (i == 0) {logFactorialLookups.setEntry(i, 0);} else {
                         logFactorialLookups.setEntry(i, logFactorialLookups.getEntry(i-1) + Math.log(i));
                     }
                 }
 
                 maxLogFactorialPrecalculated = n;
 
 
             }
 
             
 
 
         }
 
         return logFactorialLookups.getEntry(n);
 
     }
 
 
 }
