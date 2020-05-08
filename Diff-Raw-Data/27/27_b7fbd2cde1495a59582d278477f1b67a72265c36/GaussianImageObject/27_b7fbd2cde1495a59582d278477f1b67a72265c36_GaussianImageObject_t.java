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
 
 import edu.stanford.cfuller.imageanalysistools.parameters.ParameterDictionary;
 import edu.stanford.cfuller.imageanalysistools.frontend.LoggingUtilities;
 import edu.stanford.cfuller.imageanalysistools.image.Image;
 import edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate;
 
 import org.apache.commons.math.ConvergenceException;
 import org.apache.commons.math.FunctionEvaluationException;
 import org.apache.commons.math.analysis.UnivariateRealFunction;
 import org.apache.commons.math.analysis.integration.LegendreGaussIntegrator;
 import org.apache.commons.math.linear.ArrayRealVector;
 import org.apache.commons.math.linear.RealVector;
 import org.apache.commons.math.optimization.OptimizationException;
 
 import java.util.Vector;
 
 /**
  * An ImageObject that fits to a three-dimensional gaussian.
  * 
  * @author Colin J. Fuller
  */
 public class GaussianImageObject extends ImageObject {
 
 	static final long serialVersionUID =2L;
 		
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
         this.nPhotonsByChannel = new Vector<Double>();
 
         GaussianFitter3D gf = new GaussianFitter3D();
 
         //System.out.println(this.parent.getDimensionSizes().getZ());
 
         int numChannels = 0;
 
         if (p.hasKey("num_wavelengths")) {
             numChannels = p.getIntValueForKey("num_wavelengths");
         } else {
             numChannels = this.parent.getDimensionSizes().get(ImageCoordinate.C);
         }
 
         //for (int channelIndex = 0; channelIndex < this.parent.getDimensionSizes().getC(); channelIndex++) {
         for (int channelIndex = 0; channelIndex < numChannels; channelIndex++) {
 
             RealVector fitParameters = new ArrayRealVector(7, 0.0);
 
             double ppg = p.getDoubleValueForKey("photons_per_greylevel");
 
             this.parentBoxMin.set(ImageCoordinate.C,channelIndex);
             this.parentBoxMax.set(ImageCoordinate.C,channelIndex + 1);
 
             this.boxImages();
 
             java.util.Vector<Double> x = new java.util.Vector<Double>();
             java.util.Vector<Double> y = new java.util.Vector<Double>();
             java.util.Vector<Double> z = new java.util.Vector<Double>();
             java.util.Vector<Double> f = new java.util.Vector<Double>();
 
 
             for (ImageCoordinate ic : this.parent) {
                 x.add((double) ic.get(ImageCoordinate.X));
                 y.add((double) ic.get(ImageCoordinate.Y));
                 z.add((double) ic.get(ImageCoordinate.Z));
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
 
             fitParameters.setEntry(3, xCentroid);
             fitParameters.setEntry(4, yCentroid);
             fitParameters.setEntry(5, zCentroid);
 
             //variances
 
             final double limitedWidthxy = 200;
             final double limitedWidthz = 500;
 
             double sizex = limitedWidthxy / p.getDoubleValueForKey("pixelsize_nm");
             double sizez = limitedWidthz / p.getDoubleValueForKey("z_sectionsize_nm");
 
             fitParameters.setEntry(1, sizex/2);
             fitParameters.setEntry(2, sizez/2);
 
             //amplitude and background are in arbitrary intensity units; convert to photon counts
 
             fitParameters.setEntry(0, fitParameters.getEntry(0)*ppg);
             fitParameters.setEntry(6, fitParameters.getEntry(6)*ppg);
             
             //System.out.println("guess: " + fitParameters);
             
             //do the fit
 
             fitParameters = gf.fit(this, fitParameters, ppg);
             
             //System.out.println("fit: " + fitParameters);
 
 
 
             fitParametersByChannel.add(fitParameters);
             
 //            System.out.println(fitParameters);
 
             //calculate R2
 
             double residualSumSquared = 0;
             double mean = 0;
             double variance = 0;
             double R2 = 0;
 
             double n_photons = 0;
 
             for (int i =0; i < this.xValues.length; i++) {
 
                 residualSumSquared += Math.pow(GaussianFitter3D.fitResidual(functionValues[i], xValues[i], yValues[i], zValues[i], fitParameters), 2);
 
                 mean += functionValues[i];
 
                 n_photons += functionValues[i] - fitParameters.getEntry(6);
 
             }
 
             mean /= functionValues.length;
 
             for (int i =0; i < this.xValues.length; i++) {
                 variance += Math.pow(functionValues[i] - mean, 2);
             }
 
             R2 = 1 - (residualSumSquared/variance);
 
             this.fitR2ByChannel.add(R2);
 
             this.unboxImages();
 
             //calculate fit error
 
             double s_xy = fitParameters.getEntry(1)*fitParameters.getEntry(1) * Math.pow(p.getDoubleValueForKey("pixelsize_nm"), 2);
             double s_z = fitParameters.getEntry(2)*fitParameters.getEntry(2) * Math.pow(p.getDoubleValueForKey("z_sectionsize_nm"), 2);
 
             //s_z = 0; //remove!!
             
             double error = (2*s_xy + s_z)/(n_photons-1);// + 4*Math.sqrt(Math.PI) * Math.pow(2*s_xy,1.5)*Math.pow(fitParameters.getEntry(6),2)/(p.getDoubleValueForKey("pixelsize_nm")*n_photons*n_photons);
  
             double b = fitParameters.getEntry(6);
             double a = p.getDoubleValueForKey("pixelsize_nm");
             double alpha=  p.getDoubleValueForKey("z_sectionsize_nm");
             double sa_x = s_xy + Math.pow(a,2)/12;
             double sa_z = s_z + Math.pow(alpha, 2)/12;
             double error_x = sa_x/n_photons * (16.0/9.0 + 8*Math.PI*sa_x*b*b/(n_photons*Math.pow(p.getDoubleValueForKey("pixelsize_nm"), 2)));
             double error_z = sa_z/n_photons * (16.0/9.0 + 8*Math.PI*sa_z*b*b/(n_photons*Math.pow(p.getDoubleValueForKey("z_sectionsize_nm"), 2)));
             
             double A = 1.0/(2*Math.sqrt(2)*Math.pow(Math.PI,1.5) * Math.sqrt(sa_z)*sa_x);
             
             ErrIntFunc eif = new ErrIntFunc();
             
             eif.setParams(b, n_photons, A, sa_z, sa_x, a, alpha);
             
             LegendreGaussIntegrator lgi = new LegendreGaussIntegrator(5, 1000);
             
             //integrate over 10*width of PSF in z 
             
             double size = 10*Math.sqrt(sa_z);
             
             double intpart = 0;
             try {
            	
            	if (b < 0) throw new FunctionEvaluationException(b, "negative background!"); // a negative value for b seems to cause the integration to hang, preventing the program from progressing
            	
             	intpart = lgi.integrate(eif, -size, size);
             	
             	double fullIntPart = intpart + Math.pow(2*Math.PI, 1.5)*sa_x*A/Math.sqrt(sa_z);
             	
             	error_x = Math.sqrt(2/(n_photons*sa_z/(2*sa_z + sa_x)*fullIntPart));
             	error_z = Math.sqrt(2/(n_photons*sa_x/(2*sa_z + sa_x)*fullIntPart));
             	
             } catch (FunctionEvaluationException e) {
             	LoggingUtilities.getLogger().severe("Integration error: " + e.getMessage());
             	error_x = -1;
             	error_z = -1;
             } catch (ConvergenceException e) {
             	LoggingUtilities.getLogger().severe("Integration error: " + e.getMessage());
             	error_x = -1;
             	error_z = -1;
             }
             
 
            if (error_x > 0 && error_z > 0) {
             
            	error = Math.sqrt(2*error_x*error_x + error_z*error_z);
            	
            } else {
            	error = Double.NaN;
            }
             
             this.fitErrorByChannel.add(error);
             
             this.positionsByChannel.add(fitParameters.getSubVector(3, 3));
             
             this.nPhotonsByChannel.add(n_photons);
             
         }
 
         this.hadFittingError = false;
         this.nullifyImages();
     }
     
     protected class DI1Func implements UnivariateRealFunction {
     	
     	private double z;
     	private double b;
     	private double n;
     	private double A;
     	private double sa_z;
     	private double a;
     	private double alpha;
     	
     	public double value(double t) {
     		
     		double tau = b/(n*a*a*alpha*A*Math.exp(-z*z/(2*sa_z)));
     		
     		return (-1.0*t*Math.log(t)/(t+tau));
     	}
     	
     	public void setZ(double z) {
     		this.z = z;
     	}
     	
     	public void setParams(double b, double n, double A, double sa_z, double a, double alpha) {
     		this.b = b; this.n = n; this.A = A; this.sa_z = sa_z; this.a = a; this.alpha = alpha;
     	}
     	
     }
     
     protected class ErrIntFunc implements UnivariateRealFunction {
     	private double b;
     	private double n;
     	private double A;
     	private double sa_z;
     	private double sa_x;
     	private double a;
     	private double alpha;
     	
     	private LegendreGaussIntegrator lgi;
     	private DI1Func di1;
     	
     	public ErrIntFunc() {
     		this.lgi = new LegendreGaussIntegrator(5,1000);
     		this.di1 = new DI1Func();
     	}
     	
     	public void setParams(double b, double n, double A, double sa_z, double sa_x, double a, double alpha) {
     		this.b = b; this.n = n; this.A = A; this.sa_z = sa_z; this.sa_x = sa_x; this.a = a; this.alpha = alpha;
     		this.di1.setParams(b,n,A,sa_z,a,alpha);
     	}
     	
     	public double value(double z) throws FunctionEvaluationException {
     		
     		this.di1.setZ(z);
     		
     		double I1 = 0;
     		
     		try {
     			I1 = lgi.integrate(di1,0,1);
     		} catch (ConvergenceException e) {
     			throw new FunctionEvaluationException(e, z);
     		}
     		
     		double part1 = 4*Math.PI*A*Math.exp(-z*z/(2*sa_z))*I1;
     		
     		double part2 = 2*Math.PI*sa_x*b/(sa_z*sa_z*n*a*a*alpha)*z*z*Math.log(1/(1+n*A*a*a*alpha*Math.exp(-z*z/(2*sa_z))/b));
     		
     		return part1+part2;
 
     	}
     	
     	
     	
     	
     }
 
 }
