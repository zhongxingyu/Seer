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
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.List;
 import java.util.TreeSet;
 
 import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.BooleanDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.BooleanIterator;
 import uk.ac.diamond.scisoft.analysis.dataset.Comparisons;
 import uk.ac.diamond.scisoft.analysis.dataset.DatasetUtils;
 import uk.ac.diamond.scisoft.analysis.dataset.DoubleDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.Stats;
 import uk.ac.diamond.scisoft.analysis.fitting.Generic1DFitter;
 import uk.ac.diamond.scisoft.analysis.fitting.IConicSectionFitFunction;
 import uk.ac.diamond.scisoft.analysis.fitting.IConicSectionFitter;
 import uk.ac.diamond.scisoft.analysis.fitting.functions.IdentifiedPeak;
 import uk.ac.diamond.scisoft.analysis.roi.CircularROI;
 import uk.ac.diamond.scisoft.analysis.roi.EllipticalFitROI;
 import uk.ac.diamond.scisoft.analysis.roi.EllipticalROI;
 import uk.ac.diamond.scisoft.analysis.roi.PointROI;
 import uk.ac.diamond.scisoft.analysis.roi.PolylineROI;
 import uk.ac.diamond.scisoft.analysis.roi.ROIProfile;
 import uk.ac.diamond.scisoft.analysis.roi.RectangularROI;
 
 /**
  * Utilities to fit powder rings
  */
 public class PowderRingsUtils {
 	private static Logger logger = LoggerFactory.getLogger(PowderRingsUtils.class);
 
 //	/**
 //	 * Given two (fitted) rings, which have parallel major axes, calculate detector distance
 //	 * and beam angle relative to detector normal if angles are known
 //	 * @param innerAxis     length on major axis of inner ring
 //	 * @param firstGap      gap between outer and inner ring
 //	 * @param secondGap     gap between inner and outer ring
 //	 * @param innerTwoTheta scattering angle for inner ring
 //	 * @param outerTwoTheta scattering angle for outer ring
 //	 */
 //	public static DetectorProperties calculateDetectorProperties(double innerAxis, double firstGap, double secondGap, double innerTwoTheta, double outerTwoTheta) {
 //
 //		DetectorProperties prop = new DetectorProperties();
 //		return prop;
 //	}
 
 	/*
 	 * Given circle in image
 	 *  - generate radii/spokes so they are spaced a few pixels apart on circle
 	 *  - find peak centres on spokes near circle if within image
 	 *  - fit ellipse to those centres
 	 *  - repeat for larger ellipses by scanning along longest spoke
 	 */
 
 	private static final double ARC_LENGTH = 8;
 	private static final double RADIAL_DELTA = 8;
 	private static final double RADIAL_MIN = 5*RADIAL_DELTA;
 	private static final int MAX_POINTS = 200;
 
 	private static final int PEAK_SMOOTHING = 3;
 	private static final double MAX_FWHM_FACTOR = 2;
 	private static final double RING_SEPARATION = 4;
 
 	public static PolylineROI findPOIsNearCircle(AbstractDataset image, BooleanDataset mask, CircularROI circle) {
 		return findPOIsNearCircle(image, mask, circle, ARC_LENGTH, RADIAL_DELTA, MAX_POINTS);
 	}
 
 	public static PolylineROI findPOIsNearCircle(AbstractDataset image, BooleanDataset mask, CircularROI circle,
 			double arcLength, double radialDelta, int maxPoints) {
 		return findPOIsNearEllipse(image, mask, new EllipticalROI(circle), arcLength, radialDelta, maxPoints);
 	}
 
 	public static PolylineROI findPOIsNearEllipse(AbstractDataset image, BooleanDataset mask, EllipticalROI ellipse) {
 		return findPOIsNearEllipse(image, mask, ellipse, ARC_LENGTH, RADIAL_DELTA, MAX_POINTS);
 	}
 
 	/**
 	 * Find a set of points of interests near given ellipse from an image.
 	 * <p>
 	 * The ellipse is divided into sub-areas and these POIs are considered to
 	 * be the locations of maximum pixel intensities found within those sub-areas.
 	 * @param image
 	 * @param mask (can be null)
 	 * @param ellipse
 	 * @param arcLength step size along arc in pixels
 	 * @param radialDelta +/- value to define area to search
 	 * @param maxPoints maximum number of points to return ( 
 	 * @return polyline ROI
 	 */
 	public static PolylineROI findPOIsNearEllipse(AbstractDataset image, BooleanDataset mask, EllipticalROI ellipse,
 			double arcLength, double radialDelta, int maxPoints) {
 		if (image.getRank() != 2) {
 			logger.error("Dataset must have two dimensions");
 			throw new IllegalArgumentException("Dataset must have two dimensions");
 		}
 		if (mask != null && !image.isCompatibleWith(mask)) {
 			logger.error("Mask must match image shape");
 			throw new IllegalArgumentException("Mask must match image shape");
 		}
 
 		final double aj = ellipse.getSemiAxis(0);
 		final double an = ellipse.getSemiAxis(1);
 		if (an < arcLength) {
 			logger.error("Ellipse/circle is too small");
 			throw new IllegalArgumentException("Ellipse/circle is too small");
 		}
 
 		final double xc = ellipse.getPointX();
 		final double yc = ellipse.getPointY();
 		final double ang = ellipse.getAngle();
 		final double ca = Math.cos(ang);
 		final double sa = Math.sin(ang);
 		final int[] shape = image.getShape();
 		final int h = shape[0];
 		final int w = shape[1];
 
 		final double pdelta = arcLength / aj; // change in angle
 		double rdelta = radialDelta; // semi-width of annulus of interest
 		if (rdelta < 1) {
 			logger.warn("Radial delta was set too low: setting to 1");
 			rdelta = 1;
 		}
 		final double rsj = aj - rdelta;
 		final double rej = aj + rdelta;
 		final double rsn = an - rdelta;
 		final double ren = an + rdelta;
 
 		final int imax = (int) Math.ceil(Math.PI * 2. / pdelta);
 
 		logger.debug("Major semi-axis = [{}, {}]; {}", new Object[] { rsj, rej, imax });
 		final int[] start = new int[2];
 		final int[] stop = new int[2];
 		final int[] step = new int[] { 1, 1 };
 		HashSet<PointROI> pointSet = new HashSet<PointROI>();
 		for (int i = 0; i < imax; i++) {
 			double p = i * pdelta;
 			double cp = Math.cos(p);
 			double sp = Math.sin(p);
 			AbstractDataset sub;
 			final int[] beg = new int[] { (int) (yc + rsj * sa * cp + rsn * ca * sp),
 					(int) (xc + rsj * ca * cp - rsn * sa * sp) };
 			final int[] end = new int[] { (int) (yc + rej * sa * cp + ren * ca * sp),
 					(int) (xc + rej * ca * cp - ren * sa * sp) };
 			start[0] = Math.max(0, Math.min(beg[0], end[0]));
 			stop[0] = Math.min(h, Math.max(beg[0], end[0]));
 			if (start[0] == stop[0]) {
 				if (stop[0] == h) {
 					start[0]--;
 				} else {
 					stop[0]++;
 				}
 			} else if (start[0] > stop[0] || start[0] >= h) {
 				continue;
 			}
 			start[1] = Math.max(0, Math.min(beg[1], end[1]));
 			stop[1] = Math.min(w, Math.max(beg[1], end[1]));
 			if (start[1] == stop[1]) {
 				if (stop[1] == w) {
 					start[1]--;
 				} else {
 					stop[1]++;
 				}
 			} else if (start[1] > stop[1] || start[1] >= w) {
 				continue;
 			}
 			sub = image.getSlice(start, stop, step);
 
 			int[] pos = sub.maxPos();
 			if (mask != null) {
 				pos[0] += start[0];
 				pos[1] += start[1];
 
				if (mask.get(pos)) {
 					AbstractDataset sorted = DatasetUtils.sort(sub.flatten(), null);
 					int l = sorted.getSize() - 1;
 					do {
 						double x = sorted.getElementDoubleAbs(l);
 						pos = sub.getNDPosition(DatasetUtils.findIndexEqualTo(sub, x));
 						pos[0] += start[0];
 						pos[1] += start[1];
 					} while (!mask.get(pos) && --l >= 0);
 					if (l < 0) {
 						logger.warn("Could not find unmasked value for slice!");
 					} else {
 						pointSet.add(new PointROI(pos[1], pos[0]));
 					}
 				}
 			} else {
 //			System.err.printf("Slice: %s, %s has max at %s\n", Arrays.toString(start), Arrays.toString(stop), Arrays.toString(pos));
 				pointSet.add(new PointROI(pos[1]+start[1], pos[0]+start[0]));
 			}
 		}
 
 		// analyse pixel values
 		int n = pointSet.size();
 		double[] values = new double[n];
 		int i = 0;
 		for (PointROI p : pointSet) {
 			int[] pos = p.getIntPoint();
 			values[i++] = image.getDouble(pos[1], pos[0]);
 		}
 
 		DoubleDataset pixels = new DoubleDataset(values);
 		System.err.println(pixels);
 
 		// threshold with population stats from maxima
 		logger.debug("Stats: {} {} {} {}", new Object[] {pixels.min(), pixels.mean(), pixels.max(),
 				Arrays.toString(Stats.quantile(pixels, 0.25, 0.5, 0.75))});
 
 		double threshold;
 		if (n > maxPoints) {
 			threshold = Stats.quantile(pixels, 1 - maxPoints/(double) n);
 			logger.debug("Threshold: {} setting for highest {}", threshold, maxPoints);
 		} else {
 			threshold = (Double) pixels.mean() - 2 * (Double) Stats.iqr(pixels);
 			logger.debug("Threshold: {} setting by mean - 2IQR", threshold);
 		}
 
 		PolylineROI polyline = new PolylineROI();
 		if (threshold > (Double) pixels.min()) {
 			for (PointROI p : pointSet) {
 				int[] pos = p.getIntPoint();
 				double v = image.getDouble(pos[1], pos[0]);
 				if (v >= threshold) {
 //					System.err.printf("Adding %f %s\n", v, Arrays.toString(pos));
 					polyline.insertPoint(p);
 //				} else {
 //					System.err.println("Rejecting " + p + " = " + v);
 				}
 			}
 		} else {
 			for (PointROI p : pointSet) {
 				polyline.insertPoint(p);
 			}
 		}
 		logger.debug("Used {} of {} pixels", polyline.getNumberOfPoints(), pointSet.size());
 
 		return polyline;
 	}
 
 	public static EllipticalFitROI fitAndTrimOutliers(PolylineROI points, boolean circleOnly) {
 		return fitAndTrimOutliers(points, RADIAL_DELTA, circleOnly);
 	}
 
 	/**
 	 * Fit an ellipse to given points, trim points if they fall outside given distance
 	 * and re-fit
 	 * @param points
 	 * @param trimDelta trim distance
 	 * @param circleOnly if true, then fit a circle
 	 * @return fitted ellipse
 	 */
 	public static EllipticalFitROI fitAndTrimOutliers(PolylineROI points, double trimDelta, boolean circleOnly) {
 		try {
 			EllipticalFitROI efroi = new EllipticalFitROI(points, circleOnly);
 
 			PolylineROI cpts = points;
 			int n = cpts.getNumberOfPoints();
 			IConicSectionFitter f = efroi.getFitter();
 			IConicSectionFitFunction fn = f.getFitFunction(null, null);
 
 			AbstractDataset d = fn.calcDistanceSquared(f.getParameters());
 
 			// find outliers
 			double h = trimDelta * trimDelta;
 			double ds = d.max().doubleValue();
 			logger.debug("Range: [0, {}] cf [{}, {}, {}]", new Object[] { h, d.min(), d.mean(), d.max() });
 			if (ds < h)
 				return efroi;
 
 			BooleanDataset b = Comparisons.lessThanOrEqualTo(d, h);
 			BooleanIterator it = d.getBooleanIterator(b, true);
 			PolylineROI npts = new PolylineROI();
 			while (it.hasNext()) {
 				npts.insertPoint(cpts.getPoint(it.index));
 			}
 			int m = npts.getNumberOfPoints();
 			if (m < n) {
 				logger.debug("Found some outliers: {}/{}", n - m, n);
 				efroi.setPoints(npts);
 			}
 
 			return efroi;
 		} catch (Exception e) {
 			logger.error("Problem with trimming: {}", e);
 			throw new IllegalArgumentException("Problem!: ", e);
 		}
 	}
 
 	/**
 	 * Find other ellipses from given ellipse and image.
 	 * <p>
 	 * This is done by looking at the box profile along spokes from the
 	 * given centre and finding peaks. Then the distance out to those peaks is used
 	 * to search for more POIs and so more ellipses
 	 * @param image
 	 * @param mask (can be null)
 	 * @param roi initial ellipse
 	 * @return list of ellipses
 	 */
 	public static List<EllipticalROI> findOtherEllipses(AbstractDataset image, BooleanDataset mask, EllipticalROI roi) {
 		return findOtherEllipses(image, mask, roi, RADIAL_MIN, RADIAL_DELTA, ARC_LENGTH, RADIAL_DELTA, MAX_POINTS);
 	}
 
 	/**
 	 * Find other ellipses from given ellipse and image.
 	 * <p>
 	 * This is done by looking at the box profile along spokes from the
 	 * given centre and finding peaks. Then the distance out to those peaks is used
 	 * to search for more POIs and so more ellipses
 	 * @param image
 	 * @param mask (can be null)
 	 * @param roi initial ellipse
 	 * @param radialMin
 	 * @param radialDelta
 	 * @param arcLength
 	 * @param trimDelta
 	 * @param maxPoints
 	 * @return list of ellipses
 	 */
 	public static List<EllipticalROI> findOtherEllipses(AbstractDataset image, BooleanDataset mask, EllipticalROI roi,
 			double radialMin, double radialDelta, double arcLength, double trimDelta, int maxPoints) {
 		if (image.getRank() != 2) {
 			logger.error("Dataset must have two dimensions");
 			throw new IllegalArgumentException("Dataset must have two dimensions");
 		}
 		if (mask != null && !image.isCompatibleWith(mask)) {
 			logger.error("Mask must match image shape");
 			throw new IllegalArgumentException("Mask must match image shape");
 		}
 
 		// explore all corners
 		final int[] shape = image.getShape();
 		final int h = shape[0];
 		final int w = shape[1];
 		double[] ec = roi.getPoint();
 		TreeSet<Double> majors = new TreeSet<Double>();
 
 		findMajorAxes(majors, image, mask, roi, radialMin, radialDelta, ec, 0 - ec[0], 0 - ec[1]); // TL
 		findMajorAxes(majors, image, mask, roi, radialMin, radialDelta, ec, w - ec[0], 0 - ec[1]); // TR
 		findMajorAxes(majors, image, mask, roi, radialMin, radialDelta, ec, w - ec[0], h - ec[1]); // BR
 		findMajorAxes(majors, image, mask, roi, radialMin, radialDelta, ec, 0 - ec[0], h - ec[1]); // BL
 
 		// and finally find POIs
 		List<EllipticalROI> ells = new ArrayList<EllipticalROI>();
 		double major = roi.getSemiAxis(0);
 		double aspect = roi.getSemiAxis(0)/roi.getSemiAxis(1);
 		double last = Double.NEGATIVE_INFINITY;
 		for (double a : majors) {
 			System.err.println("Current " + a + ", last " + last);
 			if (Math.abs(a - last) < RING_SEPARATION) { // omit close rings
 				last = a;
 				System.err.println("Dropped as too close");
 				continue;
 			}
 			if (Math.abs(a - major) < RING_SEPARATION) {
 				last = major;
 				System.err.println("Add original");
 				ells.add(roi);
 			} else {
 				EllipticalROI er = new EllipticalROI(a, a/aspect, roi.getAngle(), ec[0], ec[1]);
 				try {
 					PolylineROI polyline = findPOIsNearEllipse(image, mask, er, arcLength, radialDelta, maxPoints);
 					if (polyline.getNumberOfPoints() > 2) {
 						er = fitAndTrimOutliers(polyline, trimDelta, roi.isCircular());
 						if (Math.abs(er.getSemiAxis(0) - last) < RING_SEPARATION) { // omit close rings
 							last = a;
 							System.err.println("Dropped as fit is too close");
 							continue;
 						}
 						double[] c = er.getPointRef();
 						if (Math.hypot(c[0] - ec[0], c[1] - ec[1]) > radialDelta) {
 							last = a; // omit fits with far-off centres
 							System.err.println("Dropped as centre is far-off");
 							continue;
 						}
 						last = er.getSemiAxis(0);
 						ells.add(er);
 					} else {
 						logger.warn("Could not find enough points at {}", er);
 					}
 				} catch (IllegalArgumentException e) {
 					logger.debug("Problem with {}", er, e);
 					last = a;
 				}
 			}
 		}
 
 		return ells;
 	}
 
 	/**
 	 * Find major axes by looking along thick line given by relative coordinates to centre for
 	 * maximum intensity values
 	 * @param axes
 	 * @param image
 	 * @param mask
 	 * @param roi
 	 * @param radialMin
 	 * @param radialDelta
 	 * @param centre
 	 * @param dx
 	 * @param dy
 	 */
 	private static void findMajorAxes(TreeSet<Double> axes, AbstractDataset image, AbstractDataset mask, EllipticalROI roi, double radialMin, double radialDelta, double[] centre, double dx, double dy) {
 		RectangularROI rroi = new RectangularROI();
 		rroi.setPoint(centre);
 		rroi.setAngle(Math.atan2(dy, dx));
 		rroi.setLengths(Math.hypot(dx, dy) - radialMin, radialDelta);
 		rroi.translate(0, -0.5); //rroi.getPoint(0, -0.5));
 		rroi.setClippingCompensation(true);
 		AbstractDataset profile = ROIProfile.maxInBox(image, mask, rroi)[0];
 		List<IdentifiedPeak> peaks = Generic1DFitter.findPeaks(AbstractDataset.arange(profile.getSize(), AbstractDataset.INT), profile, PEAK_SMOOTHING);
 
 		DescriptiveStatistics stats = new DescriptiveStatistics();
 		for (IdentifiedPeak p : peaks) {
 			if (p.getPos() < radialMin) {
 				continue;
 			}
 			stats.addValue(p.getArea());
 			System.err.printf("P %f A %f W %f H %f\n", p.getPos(), p.getArea(), p.getFWHM(), p.getHeight());
 		}
 		
 		double area = stats.getMean() + 0.5*(stats.getPercentile(75) - stats.getPercentile(25));
 		logger.debug("Area: {}", stats);
 		logger.debug("Minimum threshold: {}", area);
 
 		double majorFactor = roi.getSemiAxis(0)/roi.getDistance(rroi.getAngle());
 		double maxFWHM = MAX_FWHM_FACTOR*radialDelta;
 		for (IdentifiedPeak p : peaks) {
 			double l = p.getPos();
 			if (l < radialMin) {
 				continue;
 			}
 			System.err.println(p);
 			// filter on area and FWHM
 			if (p.getFWHM() > maxFWHM) {
 				continue;
 			}
 			if (p.getArea() < area) {
 				break;
 			}
 			axes.add(l*majorFactor);
 		}
 	}
 }
