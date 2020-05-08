 /*-
  * Copyright 2013 Diamond Light Source Ltd.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package uk.ac.diamond.scisoft.analysis.diffraction;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.BooleanDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.Comparisons;
 import uk.ac.diamond.scisoft.analysis.dataset.DoubleDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.Maths;
 import uk.ac.diamond.scisoft.analysis.dataset.Stats;
 import uk.ac.diamond.scisoft.analysis.fitting.Fitter;
 import uk.ac.diamond.scisoft.analysis.fitting.functions.Gaussian;
 import uk.ac.diamond.scisoft.analysis.monitor.IMonitor;
 import uk.ac.diamond.scisoft.analysis.roi.IParametricROI;
 import uk.ac.diamond.scisoft.analysis.roi.LinearROI;
 import uk.ac.diamond.scisoft.analysis.roi.PointROI;
 import uk.ac.diamond.scisoft.analysis.roi.PolylineROI;
 import uk.ac.diamond.scisoft.analysis.roi.ROIProfile;
 
 public class PeakFittingEllipseFinder {
 
 	private static Logger logger = LoggerFactory.getLogger(PeakFittingEllipseFinder.class);
 	
 	/**
 	 * Find a set of points of interests near given ellipse from an image.
 	 * <p>
 	 * The ellipse is divided into sub-areas and these POIs are considered to
 	 * be the locations of maximum pixel intensities found within those sub-areas.
 	 * @param image
 	 * @param mask (can be null)
 	 * @param ellipse
 	 * @param inOut array of elliptical ROIs giving search range
 	 * @return polyline ROI
 	 */
 	public static PolylineROI findPointsOnConic(AbstractDataset image, BooleanDataset mask, IParametricROI ellipse,
 			IParametricROI[] inOut, int nPoints, IMonitor mon) {
 		
 		if (image.getRank() != 2) {
 			logger.error("Dataset must have two dimensions");
 			throw new IllegalArgumentException("Dataset must have two dimensions");
 		}
 		if (mask != null && !image.isCompatibleWith(mask)) {
 			logger.error("Mask must match image shape");
 			throw new IllegalArgumentException("Mask must match image shape");
 		}
 		
 		final int[] shape = image.getShape();
 		final int h = shape[0];
 		final int w = shape[1];
 		if (ellipse.containsPoint(-1,-1) && ellipse.containsPoint(-1,h+1) && ellipse.containsPoint(w+1,h+1) && ellipse.containsPoint(w+1,-1)) {
 			throw new IllegalArgumentException("Ellipse does not intersect image!");
 		}
 		
 		PolylineROI polyline = new PolylineROI();
 		
 		
 		List<double[]> searchRange = findSuitableSearchRange(ellipse, shape);
 		
 		if (searchRange.isEmpty()) searchRange.add(new double[]{0, Math.PI*2});
 		
 		double totalRange = 0;
 		for (double[]range : searchRange) totalRange += (range[1]-range[0]);
 		
 		int count = 1;
 		
 		while (count < 5 && polyline.getNumberOfPoints() < nPoints *0.75) {
 			
 			if (count == 1) mon.subTask("Starting peak fit...");
 			else mon.subTask("Not enough points found, continuing search...: " + count);
 			
 			for (double[] range : searchRange) {
 				
 				double step = (range[1]-range[0])/nPoints;
 				int nPointFrac = (int)((range[1]-range[0])/totalRange*nPoints);
 				
 				double offset = 1/count*step;
				offset = 0;
 				findNumberOfPointsOnEllipse(image, polyline,inOut, new double[]{offset+range[0],range[1]},nPointFrac, mon);
 			}
 			
 			count++;
 		}
 		
 		if (polyline.getNumberOfPoints() < nPoints/2) {
 			logger.debug("not find enough points");
 			return null;
 		}
 		
 		return polyline;
 	}
 	
 	private static PolylineROI findNumberOfPointsOnEllipse(AbstractDataset image, PolylineROI polyline,
 			IParametricROI[] inOut, double[] startStop, int nPoints, IMonitor mon) {
 		
 		final int[] shape = image.getShape();
 		final int h = shape[1];
 		final int w = shape[0];
 		
 		List<PointROI> roiList = new ArrayList<PointROI>();
 		List<Gaussian> gaussianList = new ArrayList<Gaussian>();
 		
 		double step = (startStop[1]-startStop[0])/nPoints;
 		
 		for (double i = startStop[0]; i < startStop[1]; i+=step) {
 			double[] beg = inOut[0].getPoint(i);
 			double[] end = inOut[1].getPoint(i);
 			
 			if (end[0] > h || end[0] < 0 || end[1] > w || end[1] < 0) continue;
 			
 			LinearROI line = new LinearROI(beg, end);
 			AbstractDataset sub = ROIProfile.line(image, line, 1)[0];
 
 			BooleanDataset badVals = Comparisons.lessThanOrEqualTo(sub, 0);
 			
 			if (Comparisons.allTrue(badVals)) continue;
 			
 			double count = (Double)badVals.sum();
 			
 			if (count > sub.getSize()*0.1) continue;
 			
 			sub = sub.setByBoolean(Double.NaN, badVals);
 			double min = sub.min(true).doubleValue();
 			sub = sub.setByBoolean(min, badVals);
 			
 			AbstractDataset xAx = AbstractDataset.arange(sub.getSize(), AbstractDataset.INT32);
 			
 			sub.isubtract(min);
 			
 			double s = (Double)Stats.median(sub.getSlice(new int[] {0}, new int[] {3}, new int[] {1}));
 			double en = (Double)Stats.median(sub.getSlice(new int[] {sub.getSize()-5}, new int[] {sub.getSize()-1}, new int[] {1}));
 			
 			double m = (s-en)/(0-sub.getSize()-1);
 			double c = s;
 			
 			AbstractDataset base = Maths.multiply(xAx, m);
 			base.iadd(c);
 			
 			sub.isubtract(base);
 			
 			if ((Double)sub.mean() < 0) continue;
 			Gaussian g = null;
 			
 			try {
 				
 				DoubleDataset xData = DoubleDataset.arange(sub.getSize());
 				int maxPos = sub.maxPos()[0];
 				g = new Gaussian(new double[]{maxPos,1,sub.getDouble(maxPos)});
 				Fitter.ApacheNelderMeadFit(new AbstractDataset[]{xData}, sub, g);
 				
 			} catch (Exception e) {
 				logger.debug(e.getMessage());
 			}
 			
 			if (g == null) continue;
 			
 			//lineAngle != i for non-ellipical
 			double lineAngle = line.getAngle();
 			
 			double r = g.getParameter(0).getValue();
			double x = r*Math.cos(lineAngle)+beg[0];
			double y = r*Math.sin(lineAngle)+beg[1];
 			roiList.add(new PointROI(x,y));
 			gaussianList.add(g);
 			
 			if (mon != null) {
 				if (mon.isCancelled()) return null;
 			}
 		}
 		
 		AbstractDataset heights = AbstractDataset.zeros(new int[] {gaussianList.size()}, AbstractDataset.FLOAT64);
 		AbstractDataset widths = AbstractDataset.zeros(new int[] {gaussianList.size()}, AbstractDataset.FLOAT64);
 		
 		for (int i = 0; i < gaussianList.size(); i++) {
 			heights.set(gaussianList.get(i).getHeight(), i);
 			widths.set(gaussianList.get(i).getFWHM(), i);
 		}
 		
 		
 		double med = (Double)Stats.median(widths);
 		double mad = (Double)Stats.median(Maths.abs(Maths.subtract(widths, med)));
 		
 		double upperw = med + mad*3;
 		double lowerw = med - mad*3;
 //		double threshold = (Double) heights.mean() - iqr > 0 ? (Double) heights.mean() - iqr : (Double) heights.mean();
 		double threshold = 0;
 		
 		for (int i = 0; i < gaussianList.size(); i++) {
 			double pw = gaussianList.get(i).getFWHM();
 //			double ph = gaussianList.get(i).getHeight();
 			if (gaussianList.get(i).getHeight() > threshold && pw > lowerw && pw < upperw) {
 				polyline.insertPoint(roiList.get(i));
 			}
 		}
 		
 		return polyline;
 	}
 	
 	private static List<double[]> findSuitableSearchRange(IParametricROI ellipse, int[] shape) {
 	
 		List<double[]> angles = new ArrayList<double[]>();
 		
 		angles.add(ellipse.getHorizontalIntersectionParameters(0));//t
 		angles.add(ellipse.getVerticalIntersectionParameters(0));//l
 		angles.add(ellipse.getHorizontalIntersectionParameters(shape[0]));//b
 		angles.add(ellipse.getVerticalIntersectionParameters(shape[1]));//r
 		
 		List<Double> all = new ArrayList<Double>();		
 		for (double[] angle : angles) {
 			if (angle != null) {
 				all.add(angle[0]);
 				all.add(angle[1]);
 			}
 		}
 		
 		List<double[]> startStop = new ArrayList<double[]>();
 		
 		if (all.isEmpty()) return startStop;
 		
 		Collections.sort(all);
 		
 		for (int i = -1; i < all.size(); i++) {
 			
 			double current = 0;
 			
 			if (i != -1) current = all.get(i);
 			
 			double next = 2*Math.PI;
 			
 			if (i+1 != all.size()) next = all.get(i+1);
 			
 			double a = (next-current)/2+current;
 			double[] point = ellipse.getPoint(a);
 			
 			if (point[0] < 0 || point[0] > shape[1] || point[1] < 0 || point[1] > shape[0]) continue;
 			
 			startStop.add(new double[] {current, next});
 		}
 		
 		return startStop;
 	}
 }
