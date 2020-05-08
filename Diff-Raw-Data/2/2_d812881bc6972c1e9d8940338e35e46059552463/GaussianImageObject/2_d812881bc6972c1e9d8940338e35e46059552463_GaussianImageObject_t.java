 package edu.stanford.cfuller.imageanalysistools.fitting;
 
 import edu.stanford.cfuller.imageanalysistools.parameters.ParameterDictionary;
 import edu.stanford.cfuller.imageanalysistools.image.Image;
 import edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate;
 import org.apache.commons.math.FunctionEvaluationException;
 import org.apache.commons.math.linear.ArrayRealVector;
 import org.apache.commons.math.linear.RealVector;
 import org.apache.commons.math.optimization.OptimizationException;
 
 import java.util.Vector;
 
 /**
  * Created by IntelliJ IDEA.
  * User: cfuller
  * Date: 2/11/11
  * Time: 11:55 AM
  * To change this template use File | Settings | File Templates.
  */
 public class GaussianImageObject extends ImageObject {
 
     public GaussianImageObject() {
         init();
     }
 
     public GaussianImageObject(int label, Image mask, Image parent, ParameterDictionary p) {
         init(label, mask, parent, p);
 
     }
 
     public void fitPosition(ParameterDictionary p) throws FunctionEvaluationException, OptimizationException {
 
         this.fitParametersByChannel = new Vector<RealVector>();
         this.fitR2ByChannel = new Vector<Double>();
         this.fitErrorByChannel = new Vector<Double>();
 
         final int numFitParams = 7;
 
 
         GaussianFitter3D gf = new GaussianFitter3D();
 
         //System.out.println(this.parent.getDimensionSizes().getZ());
 
         for (int channelIndex = 0; channelIndex < this.parent.getDimensionSizes().getC(); channelIndex++) {
 
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
 
             //parameter ordering: amplitude, var x-y, var z, x/y/z coords, background
 
             //amplitude: find the max value; background: find the min value
 
 
             double maxValue = 0;
 
             double minValue = Double.MAX_VALUE;
 
 
             for (ImageCoordinate ic : this.parent) {
 
                 if (parent.getValue(ic) > maxValue) maxValue = parent.getValue(ic);
                 if (parent.getValue(ic) < minValue) minValue = parent.getValue(ic);
 
             }
 
 
             fitParameters.setEntry(0, maxValue*0.9);
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
             final double limitedWidthz = 350;
 
             double sizex = limitedWidthxy / p.getDoubleValueForKey("pixelsize_nm");
             double sizez = limitedWidthz / p.getDoubleValueForKey("z_sectionsize_nm");
 
             fitParameters.setEntry(1, Math.pow(sizex, 2.0));
             fitParameters.setEntry(2, Math.pow(sizez, 2.0));
 
             //amplitude and background are in arbitrary intensity units; convert to photon counts
 
             fitParameters.setEntry(0, fitParameters.getEntry(0)*ppg);
             fitParameters.setEntry(6, fitParameters.getEntry(6)*ppg);
 
             //do the fit
 
             RealVector initialParams = fitParameters;
 
             fitParameters = gf.fit(this, fitParameters, ppg);
 //synchronized (this.getClass()) {
 //            Logger.getLogger("edu.stanford.cfuller.Colocalization3D").info("Initial guess: " + initialParams.toString());
 //
 //            Logger.getLogger("edu.stanford.cfuller.Colocalization3D").info("Fit: " + fitParameters.toString());
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
 
             this.fitR2ByChannel.add(R2);
 
             this.unboxImages();
 
             //calculate fit error
 
             double s_xy = fitParameters.getEntry(1) * Math.pow(p.getDoubleValueForKey("pixelsize_nm"), 2);
             double s_z = fitParameters.getEntry(2) * Math.pow(p.getDoubleValueForKey("z_sectionsize_nm"), 2);
 
             double error = Math.sqrt((2*s_xy + s_z)/(n_photons - 1));
 
            //System.out.println(error);
 
             this.fitErrorByChannel.add(error);
 
         }
 
         this.hadFittingError = false;
         this.nullifyImages();
     }
 
 }
