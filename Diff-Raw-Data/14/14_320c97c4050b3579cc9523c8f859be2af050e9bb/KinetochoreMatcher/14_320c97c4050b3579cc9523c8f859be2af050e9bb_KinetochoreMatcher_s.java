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
 
 package edu.stanford.cfuller.imageanalysistools.matching;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
import org.apache.commons.math.FunctionEvaluationException;
 import org.apache.commons.math.linear.Array2DRowRealMatrix;
 import org.apache.commons.math.linear.ArrayRealVector;
 import org.apache.commons.math.linear.RealMatrix;
 import org.apache.commons.math.linear.RealVector;
 import org.apache.commons.math.optimization.OptimizationException;
 
 import edu.stanford.cfuller.imageanalysistools.filter.Filter;
 import edu.stanford.cfuller.imageanalysistools.filter.KernelFilterND;
 import edu.stanford.cfuller.imageanalysistools.filter.Label3DFilter;
 import edu.stanford.cfuller.imageanalysistools.filter.LaplacianFilterND;
 import edu.stanford.cfuller.imageanalysistools.filter.MaximumSeparabilityThresholdingFilter;
 import edu.stanford.cfuller.imageanalysistools.filter.RelabelFilter;
 import edu.stanford.cfuller.imageanalysistools.filter.SizeAbsoluteFilter;
 import edu.stanford.cfuller.imageanalysistools.filter.VariableSizeMeanFilter;
 import edu.stanford.cfuller.imageanalysistools.filter.ZeroPointFilter;
 import edu.stanford.cfuller.imageanalysistools.fitting.CentroidImageObject;
 import edu.stanford.cfuller.imageanalysistools.fitting.ImageObject;
 import edu.stanford.cfuller.imageanalysistools.image.Histogram;
 import edu.stanford.cfuller.imageanalysistools.image.Image;
 import edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate;
 import edu.stanford.cfuller.imageanalysistools.image.ReadOnlyImage;
 import edu.stanford.cfuller.imageanalysistools.image.io.PromptingImageReader;
 import edu.stanford.cfuller.imageanalysistools.parameters.ParameterDictionary;
 
 /**
  * Matches sister kinetochores at metaphase using a marker (like CREST or anti-
  * CENP-B) that stains between the paired kinetochores.
  * 
  * @author Colin J. Fuller
  *
  */
 public class KinetochoreMatcher {
 
 	ParameterDictionary parameters;
 
 	public void setParameters(ParameterDictionary p) {
 
 		this.parameters = p;
 	}
 	
 	public Map<Integer, RealVector> calculateDirections(Image reference, Image pairMarker) {
 		
 		reference.writeToFile("/Users/cfuller/Desktop/reference.ome.tif");
 		pairMarker.writeToFile("/Users/cfuller/Desktop/pair.ome.tif");
 		
 		Map<Integer, Set<Integer> > refToPairMarkerMatching = new HashMap<Integer, Set<Integer> >();
 		Map<Integer, Set<Integer> > pairToRefMarkerMatching = new HashMap<Integer, Set<Integer> >();
 
 		reference.clearBoxOfInterest();
 		
 		for (ImageCoordinate ic : reference) {
 			
 			int refValue = (int) reference.getValue(ic);
 			int pairValue = (int) pairMarker.getValue(ic);
 			
 			if (refValue == 0 || pairValue == 0) continue;
 			
 			if (! refToPairMarkerMatching.containsKey(refValue)) {
 				refToPairMarkerMatching.put(refValue, new HashSet<Integer>());
 			}
 			
 			if (! pairToRefMarkerMatching.containsKey(pairValue)) {
 				pairToRefMarkerMatching.put(pairValue, new HashSet<Integer>());
 			}
 			
 			refToPairMarkerMatching.get(refValue).add(pairValue);
 			pairToRefMarkerMatching.get(pairValue).add(refValue);
 			
 		}
 		
 		Image ones = new Image(reference.getDimensionSizes(), 1.0);
 		
 		Map<Integer, RealVector> refCentroids = new HashMap<Integer, RealVector>();
 		
 		for (Integer i : refToPairMarkerMatching.keySet()) {
 		
 			ImageObject ref = new CentroidImageObject(i, new ReadOnlyImage(reference), new ReadOnlyImage(ones), this.parameters);
 			
 			try {
 				ref.fitPosition(parameters);
 			} catch (OptimizationException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
			} catch (FunctionEvaluationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
 			}
 			
 			refCentroids.put(i, ref.getPositionForChannel(0));
 			
 		}
 		
 		Map<Integer, RealVector> pairChannelCentroids = new HashMap<Integer, RealVector>();
 		
 		Map<Integer, Integer> pairChannelCounts = new HashMap<Integer, Integer>();
 			
 		
 		for (ImageCoordinate ic : reference) {
 			
 			
 			int pairValue = (int) pairMarker.getValue(ic);
 			if (pairValue == 0 || !pairToRefMarkerMatching.containsKey(pairValue)) continue;
 
 			for (Integer refValue : pairToRefMarkerMatching.get(pairValue)) {
 			
 				if (! pairChannelCentroids.containsKey(refValue)) {
 					pairChannelCentroids.put(refValue, new ArrayRealVector(3, 0.0));
 					pairChannelCounts.put(refValue, 0);
 				}
 				
 				pairChannelCounts.put(refValue, pairChannelCounts.get(refValue) + 1);
 				RealVector centroid = pairChannelCentroids.get(refValue);
 				centroid.setEntry(0, centroid.getEntry(0) + ic.get(ImageCoordinate.X));
 				centroid.setEntry(1, centroid.getEntry(1) + ic.get(ImageCoordinate.Y));
 				centroid.setEntry(2, centroid.getEntry(2) + ic.get(ImageCoordinate.Z));
 
 			}
 			
 		}
 		
 		for (Integer i : pairChannelCounts.keySet()) {
 			
 			RealVector centroid = pairChannelCentroids.get(i);
 			centroid.mapDivideToSelf(pairChannelCounts.get(i));
 			if (refCentroids.get(i) != null) {
 				pairChannelCentroids.put(i, centroid.subtract(refCentroids.get(i)));
 				//System.out.printf("Using %d: %s\n", i, pairChannelCentroids.get(i).toString());
 
 			} else {
 				//System.out.printf("Skipping %d because of null reference centroid.\n", i);
 			}
 			
 		}
 		
 		return pairChannelCentroids;
 		
 	}
 
 	public void makePairs(Image reference, Image pairMarker) {
 		
 		PromptingImageReader pir = new PromptingImageReader();
 
 		Image mask = null;
 		Image pairMask = null;
 
 		try {
 			mask = pir.promptingRead();
 			pairMask = pir.promptingRead();
 		} catch (java.io.IOException e) {
 			e.printStackTrace();
 		}
 		
 		Histogram h = new Histogram(mask);
 /*
 		KernelFilterND kf = new KernelFilterND();
 		
 		double[] d = {0.1, 0.2, 0.4, 0.2, 0.1};
 				
 		kf.addDimensionWithKernel(edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate.X, d);
 		kf.addDimensionWithKernel(edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate.Y, d);
 		kf.addDimensionWithKernel(edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate.Z, d);
 		java.util.Vector<Filter> filters = new java.util.Vector<Filter>();
 		
 		Image pairMask = new Image(pairMarker);
 		
 		filters.add(kf);
         
         LaplacianFilterND lf = new LaplacianFilterND();
         
         filters.add(lf);
         
         filters.add(new ZeroPointFilter());
         
         filters.add(new MaximumSeparabilityThresholdingFilter());
         filters.add(new Label3DFilter());
         filters.add(new SizeAbsoluteFilter());
         filters.add(new RelabelFilter()); 
         
         this.parameters.setValueForKey("min_size", "10");
         this.parameters.setValueForKey("max_size", "1000000");
         
         for (Filter f : filters) {
             f.setParameters(this.parameters);
             f.apply(pairMask);
         }
         
 */
 		
 		Map<Integer, RealVector> directions = calculateDirections(mask, pairMask);
 		
 
 		ArrayList<ImageObject> imageObjects = new ArrayList<ImageObject>();
 
 		for (int i = 1; i <= h.getMaxValue(); i++) {
 
 			ImageObject o = new CentroidImageObject(i, new ReadOnlyImage(mask), new ReadOnlyImage(reference), this.parameters);
 			try {
 				o.fitPosition(this.parameters);
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 
 			imageObjects.add(o);
 
 			//System.out.printf("%d: %s\n", o.getLabel(), o.getPositionForChannel(0).toString());
 		}
 
 		RealMatrix distanceMatrix = new Array2DRowRealMatrix(h.getMaxValue(), h.getMaxValue());
 		RealMatrix weightMatrix = new Array2DRowRealMatrix(h.getMaxValue(), h.getMaxValue());
 
 		
 //		KernelFilterND kf = new KernelFilterND();
 //		
 //		double[] d = {0.1, 0.2, 0.4, 0.2, 0.1};
 //				
 //		kf.addDimensionWithKernel(edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate.X, d);
 //		kf.addDimensionWithKernel(edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate.Y, d);
 //		kf.addDimensionWithKernel(edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate.Z, d);
 //		
 //		Image matchingChannel = new Image(pairMarker);
 //		
 //		kf.apply(matchingChannel);
 //		
 //		LaplacianFilterND lf = new LaplacianFilterND();
 //		
 //		lf.apply(matchingChannel);
 //		
 //		double min = Double.MAX_VALUE;
 //		
 //		for (ImageCoordinate ic : matchingChannel) {
 //			if (matchingChannel.getValue(ic) < min) {
 //				min = matchingChannel.getValue(ic);
 //			}
 //		}
 //		
 //		for (ImageCoordinate ic : matchingChannel) {
 //			matchingChannel.setValue(ic, matchingChannel.getValue(ic) -min);
 //		}
 //		
 //		matchingChannel.writeToFile("/Users/cfuller/Desktop/LF_ch2.ome.tif");
 //		
 //		VariableSizeMeanFilter vsmf = new VariableSizeMeanFilter();
 //		
 //		vsmf.setBoxSize(1);
 //		
 //		vsmf.apply(pairMarker);
 //		
 //		pairMarker.writeToFile("/Users/cfuller/Desktop/vsmf_ch2.ome.tif");
 //		
 //		
 //				
 //		kf.apply(pairMarker);
 //								
 //		pairMarker.writeToFile("/Users/cfuller/Desktop/blurred.ome.tif");
 //		
 //		lf.apply(pairMarker);
 //		
 //		min = Double.MAX_VALUE;
 //		
 //		for (ImageCoordinate ic : pairMarker) {
 //			if (pairMarker.getValue(ic) < min) {
 //				min = pairMarker.getValue(ic);
 //			}
 //		}
 //		
 //		for (ImageCoordinate ic : matchingChannel) {
 //			pairMarker.setValue(ic, pairMarker.getValue(ic) -min);
 //		}
 //		
 //		pairMarker.writeToFile("/Users/cfuller/Desktop/vsmf_lf_ch2.ome.tif");
 //		
 //		kf.apply(pairMarker);
 //		
 //		pairMarker.writeToFile("/Users/cfuller/Desktop/vsmf_lf_ch2_blur.ome.tif");
 
 		
 		for (int i = 0; i < h.getMaxValue(); i++) {
 			for (int j = 0; j < h.getMaxValue(); j++) {
 				distanceMatrix.setEntry(i,j, imageObjects.get(i).getPositionForChannel(0).getDistance(imageObjects.get(j).getPositionForChannel(0)));
 
 				
 				
 				RealVector ijVec = imageObjects.get(j).getPositionForChannel(0).subtract(imageObjects.get(i).getPositionForChannel(0));
 				
 				RealVector iDirVec = directions.get(i+1);
 				RealVector jDirVec = directions.get(j+1);
 				
 				
 				double signed_cos_product = -1;
 
 				if ((!(ijVec == null)) && (!(iDirVec==null)) && (!(jDirVec==null))) {
 					
 					//System.out.printf("%d-%d: ij: %s, idir: %s, jdir: %s\n", i, j, ijVec.toString(), iDirVec.toString(), jDirVec.toString());
 
 					double cos_angle_ij = ijVec.dotProduct(iDirVec)/(ijVec.getNorm()*iDirVec.getNorm());
 					double cos_angle_ji = ijVec.mapMultiply(-1.0).dotProduct(jDirVec)/(ijVec.getNorm()*jDirVec.getNorm());
 					
 					signed_cos_product = Math.abs(cos_angle_ij * cos_angle_ji);
 					if (cos_angle_ij < 0 || cos_angle_ji < 0) {signed_cos_product*=-1;}
 
 				}
 				
 				
 				
 				if (i == j) {
 					weightMatrix.setEntry(i,j,0);
 				} else {
 					weightMatrix.setEntry(i,j,signed_cos_product*getWeight(imageObjects.get(i).getPositionForChannel(0), imageObjects.get(j).getPositionForChannel(0), pairMask, mask, i+1, j+1));
 				}
 
 				//System.out.printf("%d --> %d: %f\n", i+1, j+1, weightMatrix.getEntry(i,j));
 			}
 		}
 
 		double cutoff = 40;
 
 		Map<Integer, Integer> pairs = new HashMap<Integer, Integer>();
 		
 		for (int i = 0; i < h.getMaxValue(); i++) {
 			System.out.print((i+1) + "--> " );
 			double maxValue = 0;
 			int maxIndex = -1;
 			for (int j = 0; j < h.getMaxValue(); j++) {
 				if (distanceMatrix.getEntry(i,j) < cutoff) {
 					double twoSidedWeight = weightMatrix.getEntry(i,j) + weightMatrix.getEntry(j,i);
 					
 					System.out.printf("%d (%1.2f), ", (j+1), weightMatrix.getEntry(i,j));
 					if (twoSidedWeight > maxValue) {
 						maxValue = twoSidedWeight;
 						maxIndex = j;
 					}
 				}
 			}
 			
 			pairs.put(i, maxIndex);
 
 			System.out.println("");
 		}
 		
 		for (Integer i : pairs.keySet()) {
 			//System.out.println((i+1) + " " + (pairs.get(i)+1));
 			//if (true || pairs.get(i)>=0 && pairs.get(pairs.get(i)) == i) {
 			if (directions.get(i+1) != null) {
 			//System.out.println(imageObjects.get(i).getPositionForChannel(0) + " " + imageObjects.get(pairs.get(i)).getPositionForChannel(0));
 				System.out.println(imageObjects.get(i).getPositionForChannel(0) + " " + imageObjects.get(i).getPositionForChannel(0).add(directions.get(i+1)));
 			}
 		}
 	}
 
 
 	double getWeight(RealVector pos0, RealVector pos1, Image weights, Image mask, int label0, int label1) {
 
 		ImageCoordinate boxLower = ImageCoordinate.createCoordXYZCT(0,0,0,0,0);
 		ImageCoordinate boxUpper = ImageCoordinate.createCoordXYZCT(1,1,1,1,1);
 
 		int x0 = (int) Math.round(pos0.getEntry(0));
 
 		int x1 = (int) Math.round(pos1.getEntry(0));
 
 		int y0 = (int) Math.round(pos0.getEntry(1));
 
 		int y1 = (int) Math.round(pos1.getEntry(1));
 
 		int z0 = (int) Math.round(pos0.getEntry(2));
 
 		int z1 = (int) Math.round(pos1.getEntry(2));
 
 		boxLower.set(ImageCoordinate.X, x0 < x1 ? x0 : x1);
 		boxLower.set(ImageCoordinate.Y, y0 < y1 ? y0 : y1);
 		boxLower.set(ImageCoordinate.Z, z0 < z1 ? z0 : z1);
 
 		boxUpper.set(ImageCoordinate.X, x0 > x1 ? x0+1 : x1+1);
 		boxUpper.set(ImageCoordinate.Y, y0 > y1 ? y0+1 : y1+1);
 		boxUpper.set(ImageCoordinate.Z, z0 > z1 ? z0+1 : z1+1);
 
 		weights.setBoxOfInterest(boxLower, boxUpper);
 
 		RealVector unitVector = pos1.subtract(pos0);
 		unitVector.unitize();
 
 		double weight = 0;
 		int count = 0;
 
 		//try stepping on x; later, choose the best coordinate
 
 		int startx = x0 < x1 ? x0 : x1;
 		int finishx = x0 > x1 ? x0 : x1;
 
 		final int cutoff = 2;
 
 
 		for (int x = (int) Math.floor(startx); x <= (int) Math.ceil(finishx); x+=2) {
 
 			//calculate y, z at current x
 
 			double mult = (x - startx)/unitVector.getEntry(0);
 
 			RealVector currPos =  pos0.add(unitVector.mapMultiply(mult));
 
 			boxLower.set(ImageCoordinate.X, x - 1);
 			boxUpper.set(ImageCoordinate.X, x + 2);
 
 			boxLower.set(ImageCoordinate.Y, (int) Math.floor(currPos.getEntry(1)-cutoff));
 			boxUpper.set(ImageCoordinate.Y, (int) Math.ceil(currPos.getEntry(1) + cutoff));
 
 			boxLower.set(ImageCoordinate.Z, (int) Math.floor(currPos.getEntry(2)-cutoff));
 			boxUpper.set(ImageCoordinate.Z, (int) Math.ceil(currPos.getEntry(2) + cutoff));
 
 			weights.setBoxOfInterest(boxLower, boxUpper);
 
 			RealVector posOffset = currPos.subtract(pos0);
 
 			for (ImageCoordinate ic : weights) {
 
 				int value = (int) mask.getValue(ic);
 
 				if (value != 0) continue; // don't count if we're still in the centromere or in a different one
 
 				currPos.setEntry(0, ic.get(ImageCoordinate.X));
 				currPos.setEntry(1, ic.get(ImageCoordinate.Y));
 				currPos.setEntry(2, ic.get(ImageCoordinate.Z));
 
 
 				double dot = unitVector.dotProduct(posOffset);
 
 				RealVector perpendicular = posOffset.subtract(unitVector.mapMultiply(dot));
 
 				double distToPixel = perpendicular.getNorm();
 
 
 				if (distToPixel < cutoff) {
 					weight += weights.getValue(ic);
 					count++;
 				}
 
 
 			}
 
 			weights.clearBoxOfInterest();
 
 
 		}
 
 
 
 
 		boxLower.recycle();
 		boxUpper.recycle();
 
 		if (count > 0) {
 			weight/=count;
 		}
 
 		return weight;
 
 
 	}
 }
