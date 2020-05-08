 /*
 *	StatsUtil.java
  *  	==============
  *
  *  This file is a part of a program which serves as a utility for data analysis
  *  of experimental data
  *
  * Copyright (C) 2013-2014  Magdalen Berns <m.berns@sms.ed.ac.uk>
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 import java.io.PrintWriter;
 
 public class StatsUtil{
 
     public static double mean(double[] data){
 
         double sum = 0.0;
         for(int i=0;i<data.length;i++){
 
             sum += data[i];
         }
         return sum / (double) data.length;
     }
     public static double covariance(double xVariance, double yVariance, double[][] data){
 
         double covariance=0.0;
         //works out the difference of least squares fit
         for (int i = 0; i < data.length; i++) {
             covariance += (data[i][0] - xVariance) * (data[i][1] - yVariance);
         }
         return covariance;
     }
     public static double covariance(double xVariance, double yVariance, double[] x, double[] y){
 
         double covariance=0.0;
         //works out the difference of least squares fit
         for (int i = 0; i < x.length; i++) {
             covariance += (x[i] - xVariance) * (y[i] - yVariance);
         }
         return covariance;
     }
 
     /**
      * variance
      *          Works out the difference of least squares fit
      * @data
      *          The data being analysed
      * @mean
      *          The mean of the data
      *
      * @return
      *          The sum of all the variances
      *
      */
     public static double variance(double[] data, double mean){
 
         double variance=0.0;
         for (int i = 0; i < data.length; i++){
             variance += Math.pow((data[i] - mean),2);
         }
         return variance/data.length;
     }
 
     /**
      * standardDeviation
      *                   Works out the standard deviation of least squares fit
      * @variance
      *                   The variance of the data being analysed
      * @length
      *                   The integer length of the data array
      *
      * @return
      *          The the standard deviation of least squares fit as a double
      *
      */
     public static double standardDeviation(double variance, int length){
         double stdDev= 0.0;
         if(length > 0){
             stdDev = Math.sqrt(variance / length);
         }
         return stdDev;
     }
     /**
      * gradient
      *                   Works out the standard deviation of least squares fit
      * @covariance
      *                   The covariance of the data being analysed
      * @xVariance
      *                   The integer length of the data array
      *
      * @return
      *          The the standard deviation of least squares fit as a double
      *
      */
     public static double gradient(double covariance, double xVariance){
 
         return covariance / xVariance;
     }
     /**
      * yIntercept
      *                   Works out the offset of the data (i.e the constant value by which y is offset)
      * @xMean
      *                   The mean value of the x coordinate of the data being analysed
      * @yMean
      *                   The mean value of the y coordinate of the data being analysed
      * @gradient
      *                  The gradient
      *
      * @return
      *          The the standard deviation of least squares fit as a double
      *
      */
     public static double yIntercept(double xMean, double yMean, double gradient){
         return yMean - gradient * xMean;
     }
 
     /**
     * 
     *                   Works out the offset of the data (i.e the constant value by which y is offset)
     * @xMean
     *                   The mean value of the x coordinate of the data being analysed
     * @yMean
     *                   The mean value of the y coordinate of the data being analysed
      * @gradient
      *                  The gradient
      *
      * @return
     *          The the standard deviation of least squares fit as a double
      *
      */
     public static double[] fit(double[][] data, double gradient, double offset){
         double[] fit=new double[data.length];
         for(int i=0; i<data.length; i++)
             fit[i] = gradient*data[i][0] + offset;
         return fit;
     }
     public static double[] fit(double[] x, double gradient, double offset){
         double[] fit=new double[x.length];
         for(int i=0; i<x.length; i++)
             fit[i] = gradient*x[i] + offset;
         return fit;
     }
     // Residual Sum of Squares.
     public static double rss(double[][] data, double[] fit){
 
         double rss = 0.0; //standard error in mean i.e. residual sum of squares
         for (int i = 0; i < data.length; i++)
             rss += (fit[i] - data[i][1]) * (fit[i] - data[i][1]);
         return rss;
     }
     // Residual Sum of Squares.
     public static double rss(double[] y, double[] fit){
 
         double rss = 0.0; //standard error in mean i.e. residual sum of squares
         for (int i = 0; i < y.length; i++)
             rss += (fit[i] - y[i]) * (fit[i] - y[i]);
         return rss;
     }
     //Regression sum of squares.
     public static double ssr(double[] fit, double yMean){
 
         double ssr = 0.0; // regression sum of squares
         for (int i = 0; i < fit.length; i++){
             ssr += (fit[i] - yMean) * (fit[i] - yMean);
         }
         return ssr;
     }
     public static double[] residuals(double[] y, double[] fit){
 
         double[] residuals=new double[y.length];
         for (int i = 0; i < y.length; i++){
             residuals[i] = y[i] - fit[i];
         }
         return residuals;
     }
     //Returns the linear corrilation coefficient
     public static double linearCC(double ssr, double yVariance){
         return ssr/yVariance;
     }
     //Assumes that data has only 2 degrees of freedom.
     //gives the error in the offset
     public static double errorOffset(double n,double xVar, double xMean, double rss) {
         double degreesFreedom=n-2;
         double sigma = rss / degreesFreedom;
         double svar = sigma / xVar;
         return Math.sqrt(sigma/n + xMean*xMean*svar);
     }
     //Gives the error in the gradient
     public static double errorGradient(double xVariance, double rss, int n){
 
         double degreesFreedom=n-2;
         double stdVar = rss / degreesFreedom;
         return Math.sqrt(stdVar/ xVariance);
     }
     public static double errorFit(double stdFit){
 
         return Math.sqrt(stdFit);
     }
 }
