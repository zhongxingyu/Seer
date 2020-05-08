 /*-
  * Copyright 2012 Diamond Light Source Ltd.
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
 
 package uk.ac.diamond.scisoft.analysis.roi;
 
 import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
 import uk.ac.diamond.scisoft.analysis.fitting.CircleFitter;
 import uk.ac.diamond.scisoft.analysis.fitting.EllipseFitter;
 import uk.ac.diamond.scisoft.analysis.fitting.IConicSectionFitter;
 
 /**
  * An elliptical region of interest which fits the points in a polygonal region of interest
  */
 public class EllipticalFitROI extends EllipticalROI {
 
 	private PolylineROI proi;
 	private boolean circleOnly;
	private IConicSectionFitter fitter;
 	private double residual;
 
 	private EllipticalFitROI(double major, double minor, double angle, double ptx, double pty) {
 		super(major, minor, angle, ptx, pty);
 		residual = 0;
 	}
 
 	public EllipticalFitROI(PolylineROI points) {
 		this(points, false);
 	}
 
 	public EllipticalFitROI(PolylineROI points, boolean fitCircle) {
 		super(1, 0, 0);
 		circleOnly = fitCircle;
 		setPoints(points);
 	}
 
 	@Override
 	public void downsample(double subFactor) {
 		super.downsample(subFactor);
 		proi.downsample(subFactor);
 	}
 
 	@Override
 	public EllipticalFitROI copy() {
 		EllipticalFitROI croi = new EllipticalFitROI(getSemiAxis(0), getSemiAxis(1), getAngle(), getPointX(), getPointY());
 		croi.proi = proi.copy();
 		croi.setPlot(plot);
 		return croi;
 	}
 
 	/**
 	 * Fit an ellipse to given polyline
 	 * @param polyline
 	 * @return fitter
 	 */
 	public static IConicSectionFitter fit(PolylineROI polyline, final boolean fitCircle) {
 		AbstractDataset[] xy = polyline.makeCoordinateDatasets();
 		if (fitCircle) {
 			CircleFitter f = new CircleFitter();
 			f.geometricFit(xy[0], xy[1], null);
 			return f;
 		}
 
 		IConicSectionFitter f = new EllipseFitter();
 		f.geometricFit(xy[0], xy[1], null);
 		return f;
 	}
 
 	
 	/**
 	 * Set points which are then used to fit ellipse
 	 * @param points
 	 */
 	public void setPoints(PolylineROI points) {
 		proi = points;
 		int n = points.getNumberOfPoints();
 		if (fitter == null) {
 			fitter = fit(points, n < 5 || circleOnly);
 		} else {
 			AbstractDataset[] xy = points.makeCoordinateDatasets();
 			fitter.geometricFit(xy[0], xy[1], fitter.getParameters());
 		}
 		final double[] p = fitter.getParameters();
 		residual = fitter.getRMS();
 
 		if (p.length < 5) {
 			setSemiAxis(0, p[0]);
 			setSemiAxis(1, p[0]);
 			setAngle(0);
 			setPoint(p[1], p[2]);
 		} else {
 			setSemiAxis(0, p[0]);
 			setSemiAxis(1, p[1]);
 			setAngle(p[2]);
 			setPoint(p[3], p[4]);
 		}
 	}
 
 	/**
 	 * @return root mean squared of residuals
 	 */
 	public double getRMS() {
 		return residual;
 	}
 
 	/**
 	 * @return fitter used
 	 */
 	public IConicSectionFitter getFitter() {
 		return fitter;
 	}
 	
 	/**
 	 * @return points in polygon for fitting
 	 */
 	public PolylineROI getPoints() {
 		return proi;
 	}
 
 }
