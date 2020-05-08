 /* ***** BEGIN LICENSE BLOCK *****
  * Version: MPL 1.1/GPL 2.0/LGPL 2.1
  *
  * The contents of this file are subject to the Mozilla Public License Version
  * 1.1 (the "License"); you may not use this file except in compliance with
  * the License. You may obtain a copy of the License at
  * http://www.mozilla.org/MPL/
  *
  * Software distributed under the License is distributed on an "AS IS" basis,
  * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
  * for the specific language governing rights and limitations under the
  * License.
  *
  * The Original Code is Colin J. Fuller's code.
  *
  * The Initial Developer of the Original Code is
  * Colin J. Fuller.
  * Portions created by the Initial Developer are Copyright (C) 2011
  * the Initial Developer. All Rights Reserved.
  *
  * Contributor(s): Colin J. Fuller
  *
  * Alternatively, the contents of this file may be used under the terms of
  * either the GNU General Public License Version 2 or later (the "GPL"), or
  * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
  * in which case the provisions of the GPL or the LGPL are applicable instead
  * of those above. If you wish to allow use of your version of this file only
  * under the terms of either the GPL or the LGPL, and not to allow others to
  * use your version of this file under the terms of the MPL, indicate your
  * decision by deleting the provisions above and replace them with the notice
  * and other provisions required by the GPL or the LGPL. If you do not delete
  * the provisions above, a recipient may use your version of this file under
  * the terms of any one of the MPL, the GPL or the LGPL.
  *
  * ***** END LICENSE BLOCK ***** */
 
 package edu.stanford.cfuller.imageanalysistools.fitting;
 
 import edu.stanford.cfuller.imageanalysistools.frontend.LoggingUtilities;
 import edu.stanford.cfuller.imageanalysistools.parameters.ParameterDictionary;
 import edu.stanford.cfuller.imageanalysistools.image.Image;
 import edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate;
 import org.apache.commons.math.FunctionEvaluationException;
 import org.apache.commons.math.linear.ArrayRealVector;
 import org.apache.commons.math.linear.RealVector;
 import org.apache.commons.math.optimization.OptimizationException;
 
 import java.util.Vector;
 
 /**
  * An ImageObject that fits to a three-dimensional gaussian.
  */
 public class GaussianImageObject extends ImageObject {
 
     /**
      * Creates an empty GaussianImageObject.
      */
     public GaussianImageObject() {
         init();
     }
 
     /**
      * Creates a GaussianImageObject from the specified masked region in an Image.
      * @param label     The greylevel of the object in the Image mask.
      * @param mask      The mask of objects in the Image, with a unique greylevel assigned to each object.
      * @param parent    The Image that the object occurs in and that is masked by mask.
      * @param p         The parameters associated with this analysis.
      */
     public GaussianImageObject(int label, Image mask, Image parent, ParameterDictionary p) {
         init(label, mask, parent, p);
 
     }
 
 
     /**
      * Fits this object to a 3-dimensional gaussian, and estimates error and goodness of fit.
      * @param p     The parameters for the current analysis.
      * @throws FunctionEvaluationException if there is an error evaluating the gaussian function or the likelihood function used to fit the gaussian.
      * @throws OptimizationException        if the optimizer used to compute the fit raises an exception.
      */
     public void fitPosition(ParameterDictionary p) throws FunctionEvaluationException, OptimizationException {
 
         if (this.sizeInPixels == 0) {
             this.nullifyImages();
             return;
         }
 
         this.fitParametersByChannel = new Vector<RealVector>();
         this.fitR2ByChannel = new Vector<Double>();
         this.fitErrorByChannel = new Vector<Double>();
 
         final int numFitParams = 7;
 
 
         GaussianFitter3D gf = new GaussianFitter3D();
 
         //System.out.println(this.parent.getDimensionSizes().getZ());
 
         int numChannels = 0;
 
         if (p.hasKey("num_wavelengths")) {
             numChannels = p.getIntValueForKey("num_wavelengths");
         } else {
             numChannels = this.parent.getDimensionSizes().getC();
         }
 
         //for (int channelIndex = 0; channelIndex < this.parent.getDimensionSizes().getC(); channelIndex++) {
         for (int channelIndex = 0; channelIndex < numChannels; channelIndex++) {
 
             RealVector fitParameters = new ArrayRealVector(7, 0.0);
 
             double ppg = p.getDoubleValueForKey("photons_per_greylevel");
 
             this.parentBoxMin.setC(channelIndex);
             this.parentBoxMax.setC(channelIndex + 1);
 
             this.boxImages();
 
             java.util.Vector<Double> x = new java.util.Vector<Double>();
             java.util.Vector<Double> y = new java.util.Vector<Double>();
             java.util.Vector<Double> z = new java.util.Vector<Double>();
             java.util.Vector<Double> f = new java.util.Vector<Double>();
 
 
             for (ImageCoordinate ic : this.parent) {
                 x.add((double) ic.getX());
                 y.add((double) ic.getY());
                 z.add((double) ic.getZ());
                 f.add(parent.getValue(ic));
             }
 
             xValues = new double[x.size()];
             yValues = new double[y.size()];
             zValues = new double[z.size()];
             functionValues = new double[f.size()];
 
             double xCentroid = 0;
             double yCentroid = 0;
             double zCentroid = 0;
             double totalCounts = 0;
 
 
             
 
             for (int i = 0; i < x.size(); i++) {
                 xValues[i] = x.get(i);
                 yValues[i] = y.get(i);
                 zValues[i] = z.get(i);
                 functionValues[i] = f.get(i)*ppg;
                 xCentroid += xValues[i] * functionValues[i];
                 yCentroid += yValues[i] * functionValues[i];
                 zCentroid += zValues[i] * functionValues[i];
                 totalCounts += functionValues[i];
             }
 
 
             xCentroid /= totalCounts;
             yCentroid /= totalCounts;
             zCentroid /= totalCounts;
 
             //z sometimes seems to be a bit off... trying (20110415) to go back to max value pixel at x,y centroid
 
             int xRound = (int) Math.round(xCentroid);
             int yRound = (int) Math.round(yCentroid);
 
             double maxVal = 0;
             int maxInd = 0;
 
             double minZ = Double.MAX_VALUE;
             double maxZ = 0;
 
             for (int i =0; i < x.size(); i++) {
 
                 if (zValues[i] < minZ) minZ = zValues[i];
                 if (zValues[i] > maxZ) maxZ = zValues[i];
 
                 if (xValues[i] == xRound && yValues[i] == yRound) {
                     if (functionValues[i] > maxVal) {
                         maxVal = functionValues[i];
                         maxInd = (int) zValues[i];
                     }
                 }
             }
 
             //System.out.println("object " + this.label + "  max: " + maxZ + "  min: " + minZ);
 
             zCentroid = maxInd;
 
 
             //parameter ordering: amplitude, var x-y, var z, x/y/z coords, background
 
             //amplitude: find the max value; background: find the min value
 
 
             double maxValue = 0;
 
             double minValue = Double.MAX_VALUE;
 
 
             for (ImageCoordinate ic : this.parent) {
 
                 if (parent.getValue(ic) > maxValue) maxValue = parent.getValue(ic);
                 if (parent.getValue(ic) < minValue) minValue = parent.getValue(ic);
 
             }
 
 
             fitParameters.setEntry(0, (maxValue-minValue)*0.95);
             fitParameters.setEntry(6, minValue+0.05*(maxValue - minValue));
 
             //positions
 
             //fitParameters.setEntry(3, this.centroidInMask.getX());
             //fitParameters.setEntry(4, this.centroidInMask.getY());
             //fitParameters.setEntry(5, this.maxIntensityZCoordByChannel[channelIndex]);
 
             fitParameters.setEntry(3, xCentroid);
             fitParameters.setEntry(4, yCentroid);
             fitParameters.setEntry(5, zCentroid);
 
             //variances
 
             int xBoxSize = this.parentBoxMax.getX() - this.parentBoxMin.getX();
             int zBoxSize = this.parentBoxMax.getZ() - this.parentBoxMin.getZ();
 
             final double limitedWidthxy = 200;
             final double limitedWidthz = 500;
 
             double sizex = limitedWidthxy / p.getDoubleValueForKey("pixelsize_nm");
             double sizez = limitedWidthz / p.getDoubleValueForKey("z_sectionsize_nm");
 
             fitParameters.setEntry(1, Math.pow(sizex, 2.0)/2);
             fitParameters.setEntry(2, Math.pow(sizez, 2.0)/2);
 
             //amplitude and background are in arbitrary intensity units; convert to photon counts
 
             fitParameters.setEntry(0, fitParameters.getEntry(0)*ppg);
             fitParameters.setEntry(6, fitParameters.getEntry(6)*ppg);
 
             //do the fit
 
             RealVector initialParams = fitParameters;
 
             fitParameters = gf.fit(this, fitParameters, ppg);
 //synchronized (this.getClass()) {
             //LoggingUtilities.getLogger().info("Initial guess for object # " + this.label + ": " + initialParams.toString());
 //
             //LoggingUtilities.getLogger().info("Fit for object # " + this.label + ": " + fitParameters.toString());
 //            }
 
             fitParametersByChannel.add(fitParameters);
 
             //calculate R2
 
             double residualSumSquared = 0;
             double mean = 0;
             double variance = 0;
             double R2 = 0;
 
             double n_photons = 0;
 
             for (int i =0; i < this.xValues.length; i++) {
 
                 residualSumSquared += Math.pow(gf.fitResidual(functionValues[i], xValues[i], yValues[i], zValues[i], fitParameters), 2);
 
                 mean += functionValues[i];
 
                 n_photons += functionValues[i] - fitParameters.getEntry(6);
 
             }
 
             //double n_photons = mean;
 
             mean /= functionValues.length;
 
             for (int i =0; i < this.xValues.length; i++) {
                 variance += Math.pow(functionValues[i] - mean, 2);
             }
 
             //variance /= functionValues.length;
 
             //R2 = 1 - (residualSumSquared/(variance * functionValues.length));
 
             R2 = 1 - (residualSumSquared/variance);
 
            //LoggingUtilities.getLogger().info("R^2 for object # " + this.label + ": " +R2);


             this.fitR2ByChannel.add(R2);
 
             this.unboxImages();
 
             //calculate fit error
 
             double s_xy = fitParameters.getEntry(1) * Math.pow(p.getDoubleValueForKey("pixelsize_nm"), 2);
             double s_z = fitParameters.getEntry(2) * Math.pow(p.getDoubleValueForKey("z_sectionsize_nm"), 2);
 
             double error = Math.sqrt(2*(2*s_xy + s_z)/(n_photons - 1));
 
             //System.out.println(error);
  
             this.fitErrorByChannel.add(error);
 
         }
 
         this.hadFittingError = false;
         this.nullifyImages();
     }
 
 }
