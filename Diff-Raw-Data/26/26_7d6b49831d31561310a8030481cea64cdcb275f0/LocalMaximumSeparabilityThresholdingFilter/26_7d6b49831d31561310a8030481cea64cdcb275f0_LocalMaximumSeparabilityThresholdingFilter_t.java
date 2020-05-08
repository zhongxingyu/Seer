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
 
 package edu.stanford.cfuller.imageanalysistools.filter;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 
 import edu.stanford.cfuller.imageanalysistools.fitting.NelderMeadMinimizer;
 import edu.stanford.cfuller.imageanalysistools.fitting.ObjectiveFunction;
 import edu.stanford.cfuller.imageanalysistools.image.Image;
 import edu.stanford.cfuller.imageanalysistools.image.Histogram;
 import edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate;
 import org.apache.commons.math.linear.ArrayRealVector;
 import org.apache.commons.math.linear.RealVector;
 /**
  * A Filter that thresholds an Image using a modified Otsu's method.  (Otsu, 1979, DOI: 10.1109/TSMC.1979.4310076).  This attempts to fit the
  * eta vs. greylevel histogram to a double gaussian, and takes the higher value for the threshold.
  * <p>
  * This filter does not use a reference Image.
  * <p>
  * The argument to the apply method should be the Image to be thresholded.  After filtering, this Image will have its original values
  * anywhere where it is above the threshold, and zero values overwritten anywhere where the original values were below the threshold.
  *
  * @author Colin J. Fuller
  *
  *
  */
 public class LocalMaximumSeparabilityThresholdingFilter extends Filter {
 
 	final int defaultIncrement = 1;
 	final boolean defaultAdaptive = false;
 
 
     /**
      * Applies the filter to an Image.
      * @param im    The Image to be thresholded; this will be overwritten by the thresholded Image.
      */
 	@Override
 	public void apply(Image im) {
 		apply_ext(im, defaultAdaptive, defaultIncrement);
 	}
 
     /**
      * Applies the filter to an Image, optionally turning on adaptive determination of the increment size used to find the threshold.
      * Turning on adaptive determination of the increment will generally make the threshold slightly less optimal, but can sometimes speed up the filtering, especially
      * for images with a large dynamic range.
      * @param im    The Image to be thresholded; this will be overwritten by the thresholded Image.
      * @param adaptiveincrement     true to turn on adaptive determination of the threshold increment; false to turn it off and use the default value
      */
 	public void apply_ext(Image im, boolean adaptiveincrement) {
 		apply_ext(im, adaptiveincrement, defaultIncrement);
 	}
 
     /**
      * Applies the filter to an Image, optionally turning on adaptive determination of the increment size used to find the threshold, and specifying a size for the threshold determination increment.
      * Turning on adaptive determination of the increment will generally make the threshold slightly less optimal, but can sometimes speed up the filtering, especially
      * for images with a large dynamic range.
      * <p>
      * The increment size specified (in greylevels) will be used to determine the threshold only if adaptive determination is off; otherwise this parameter will be ignored.
      * @param im    The Image to be thresholded; this will be overwritten by the thresholded Image.
      * @param adaptiveincrement     true to turn on adaptive determination of the threshold increment; false to turn it off and use the default value
      * @param increment             the increment size (in greylevels) to use for determining the threshold; must be positive.
      */
 	public void apply_ext(Image im, boolean adaptiveincrement, int increment) {
 		
 		Histogram h = new Histogram(im);
 		
 		int thresholdValue = 0;
 		
 		final int numSteps = 1000;
 		
 		double best_eta = 0;
 		int best_index = Integer.MAX_VALUE;
 		
 		int nonzerocounts = h.getTotalCounts() - h.getCounts(0);
 		
 		double meannonzero = h.getMeanNonzero();
 				
 		ArrayRealVector omega_v = new ArrayRealVector(h.getMaxValue());
 		ArrayRealVector mu_v = new ArrayRealVector(h.getMaxValue());
 		ArrayRealVector eta_v = new ArrayRealVector(h.getMaxValue());
 		
 		int c = 0;
 		
 		if (adaptiveincrement) {
 			increment = (int) ((h.getMaxValue() - h.getMinValue() + 1)*1.0/numSteps);
 			if (increment < 1) increment = 1;
 		}
 		
 		
 		for (int k= h.getMinValue(); k < h.getMaxValue() + 1; k+= increment) {
 			
 			if (k==0) continue;
 			
 			omega_v.setEntry(c, h.getCumulativeCounts(k) * 1.0/nonzerocounts);
 			
 			if (c == 0) {
 				mu_v.setEntry(c, k*omega_v.getEntry(c));
 			} else {
 				
 				mu_v.setEntry(c, mu_v.getEntry(c-1) + k*h.getCounts(k) *1.0/nonzerocounts);
 				for (int i = k-increment + 1; i < k; i++) {
 					mu_v.setEntry(c, mu_v.getEntry(c)+h.getCounts(i)*i*1.0/nonzerocounts);
 				}
 								
 			}
 			
 			double omega = omega_v.getEntry(c);
 			double mu = mu_v.getEntry(c);
 			
 			if (omega > 1e-8 && 1-omega > 1e-8) {
 				
 				double eta = omega*(1-omega) * Math.pow((meannonzero - mu)/(1-omega) - mu/omega, 2);
 				
 				eta_v.setEntry(c, eta);
 				
 				if (eta >= best_eta) {
 					best_eta = eta;
 					best_index = k;
 				}
 				
 			
 			} else {
 				eta_v.setEntry(c, 0);
 			}
 			
 		
 			c++;
 			
 		}
 		
 		c = 1;
 		
 		ArrayList<Integer> maxima = new ArrayList<Integer>();
 		Map<Integer, Integer> k_by_c = new HashMap<Integer, Integer>();
 		Map<Integer, Integer> c_by_k = new HashMap<Integer, Integer>();
 		
 		for (int k= h.getMinValue()+1; k < h.getMaxValue(); k+= increment) {
 			
 			//detect if this is a local maximum
 			
 			k_by_c.put(c, k);
 			c_by_k.put(k, c);
 			
 			int lastEntryNotEqual = c-1;
 			int nextEntryNotEqual = c+1;
 			
 			while(lastEntryNotEqual > 0 && eta_v.getEntry(lastEntryNotEqual) == eta_v.getEntry(c)) {--lastEntryNotEqual;}
 			while(nextEntryNotEqual < (eta_v.getDimension()-1) && eta_v.getEntry(nextEntryNotEqual) == eta_v.getEntry(c)) {++nextEntryNotEqual;}
 
 			
 			if (eta_v.getEntry(c) > eta_v.getEntry(lastEntryNotEqual) && eta_v.getEntry(c) > eta_v.getEntry(nextEntryNotEqual)) {
 										
 				if (eta_v.getEntry(c) > 0.5*best_eta) { //require that we're close to the best
 				
 					maxima.add(k);
 					
 				}
 				
 			}
 			
 			c++;
 			
 		}
 				
 		//now that we have maxima, try doing a gaussian fit to find the positions.  If there's only one, we need to guess at a second
 		
 		RealVector parameters = new ArrayRealVector(6, 0.0);
 
 		int position0 = 0;
 		int position1 = h.getMaxValue();
 		
 		if (maxima.size() > 1) {
 			
 			position0 = c_by_k.get(maxima.get(0));
 			position1 = c_by_k.get(maxima.get(maxima.size() - 1));
 			
 		} else {
 			
 			position0 = c_by_k.get(maxima.get(0));
 			position1 = (eta_v.getDimension() - position0)/4 + position0;
 
 		}
 		
 		double s = (position1 - position0)/4.0;
 		
 		parameters.setEntry(0, eta_v.getEntry(position0));//*Math.sqrt(2*Math.PI)*s);
 		parameters.setEntry(1, position0);
 		parameters.setEntry(2, s);
 		parameters.setEntry(3, eta_v.getEntry(position1));//*Math.sqrt(2*Math.PI)*s);
 		parameters.setEntry(4, position1);
 		parameters.setEntry(5, s);
 		
 		DoubleGaussianObjectiveFunction dgof = new DoubleGaussianObjectiveFunction();
 		
 		dgof.setEta(eta_v);
 		
 		NelderMeadMinimizer nmm = new NelderMeadMinimizer();
 		
 		RealVector result = nmm.optimize(dgof, parameters);
 		
 		best_index = (int) result.getEntry(4);
 		
 		if (k_by_c.containsKey(best_index)) {
 			best_index = k_by_c.get(best_index);
 		} else {
 			//fall back to the best local maximum if the fitting seems to have found an invalid value.
 			best_index = maxima.get(maxima.size() -1);
 		
 		}	
 		thresholdValue = best_index;
 		
 		if (thresholdValue == Integer.MAX_VALUE) {
 			thresholdValue = 0;
 		}
 		for (ImageCoordinate coord : im) {
 			if (im.getValue(coord) < thresholdValue) im.setValue(coord, 0);
 		}
 		
 	}
 	
 	protected class DoubleGaussianObjectiveFunction implements ObjectiveFunction {
 		
 		//format of the point = A0, mean0, stddev0, A1, mean1, stddev1
 
 		RealVector etaValues;
 		
 		public void setEta(RealVector eta) {
 			this.etaValues = eta;
 		}
 		
 		
 		protected double doubleGaussProb(double x, RealVector parameters) {
 			double A0 = parameters.getEntry(0);
 			double A1 = parameters.getEntry(3);
 			double mean0 = parameters.getEntry(1);
 			double mean1 = parameters.getEntry(4);
 			double s0 = parameters.getEntry(2);
 			double s1 = parameters.getEntry(5);
 			
 			return (A0*Math.exp(-Math.pow(x-mean0, 2)/(2*s0*s0)) + A1*Math.exp(-Math.pow(x-mean1,2)/(2*s1*s1)));
 			
 		}
 		
 		/**
 		 * Calculates the sse between the gaussians specified by the parameters and the data.
 		 */
 		public double evaluate(RealVector point) {
 			
 			double sse = 0;
 			
 			for (int i =0; i < this.etaValues.getDimension(); i++) {
 				
 				double err = this.etaValues.getEntry(i) - doubleGaussProb(i, point);
 				
 				sse += err*err;
 				
 			}
 			
 			return sse;
 			
 		}
 		
 		
 	}
 	
 
 }
