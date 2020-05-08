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
 
 package uk.ac.diamond.scisoft.analysis.diffraction;
 
 import java.util.Iterator;
 import java.util.LinkedHashSet;
 import java.util.Set;
 
 import javax.vecmath.Vector3d;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.diamond.scisoft.analysis.roi.EllipticalROI;
 import uk.ac.diamond.scisoft.analysis.roi.IROI;
 
 /**
  * Utility class to hold methods that calculate or use d-spacings
  */
 public class DSpacing {
 
 	@SuppressWarnings("unused")
 	private static Logger logger = LoggerFactory.getLogger(DSpacing.class);
 
 	/**
 	 * Calculate d-spacings from given positions of Bragg diffraction spots
 	 * @param detector
 	 * @param diffExp
 	 * @param pos
 	 *            An array of x,y positions of spots on the detector in pixels. There must be an even number of values
 	 * @return array of inter-spot distances (d spacing) in angstroms
 	 */
 	public static double[] dSpacingsFromPixelCoords(DetectorProperties detector, DiffractionCrystalEnvironment diffExp,
 			int... pos) {
 		double[] dpos = new double[pos.length];
 		for (int i = 0; i < pos.length; i++)
 			dpos[i] = pos[i];
 		return dSpacingsFromPixelCoords(detector, diffExp, dpos);
 	}
 
 	static class Pair {
 		final double x;
 		final double y;
 
 		public Pair(double x, double y) {
 			this.x = x;
 			this.y = y;
 		}
 
 		@Override
 		public boolean equals(Object obj) {
 			if (obj instanceof Pair) {
 				Pair other = (Pair) obj;
 				if (this == other)
 					return true;
 
 				return Double.doubleToRawLongBits(x) == Double.doubleToRawLongBits(other.x) &&
 						Double.doubleToRawLongBits(y) == Double.doubleToRawLongBits(other.y);
 			}
 
 			return false;
 		}
 
 		@Override
 		public int hashCode() {
 			return (int) (Double.doubleToRawLongBits(x) * 17 + Double.doubleToRawLongBits(y));
 		}
 
 		@Override
 		public String toString() {
 			return String.format("(%f, %f)", x, y);
 		}
 	}
 
 	/**
 	 * Calculate d-spacings from given positions of Bragg diffraction spots
 	 * @param detector
 	 * @param diffExp
 	 * @param pos
 	 *            An array of x,y positions of spots on the detector in pixels. There must be an even number of values
 	 * @return array of inter-spot distances (d spacing) in angstroms
 	 */
 	public static double[] dSpacingsFromPixelCoords(DetectorProperties detector, DiffractionCrystalEnvironment diffExp,
 			double... pos) {
 		if (pos.length % 2 != 0) {
 			throw new IllegalArgumentException("The number of values must be even");
 		}
 
 		// unique-fy coords
 		Set<Pair> coords = new LinkedHashSet<Pair>();
 		for (int i = 0; i < pos.length; i += 2) {
 			Pair p = new Pair(pos[i], pos[i+1]);
 			coords.add(p);
 		}
 
 		Vector3d q = new Vector3d();
 		QSpace qspace = new QSpace(detector, diffExp);
 		double[] spacings = new double[coords.size()-1];
 		Iterator<Pair> it = coords.iterator();
 		Pair p2 = it.next();
 		int i = 0;
 		while (it.hasNext()) {
 			Pair p1 = p2;
 			p2 = it.next();
 			q.sub(qspace.qFromPixelPosition(p1.x, p1.y), qspace.qFromPixelPosition(p2.x, p2.y));
 			spacings[i++] = 2 * Math.PI / q.length();
 		}
 		return spacings;
 	}
 
 	/**
 	 * Calculate radius of circle assuming the detector is normal to the beam vector
 	 * 
 	 * @param detector
 	 * @param difExp
 	 * @param dSpacing
 	 * @return radius of circle in PIXELS
 	 */
 	public static double radiusFromDSpacing(DetectorProperties detector, DiffractionCrystalEnvironment difExp,
 			double dSpacing) {
 		double theta = 2 * Math.asin(difExp.getWavelength() / (2 * dSpacing));
 		Vector3d radiusVector = new Vector3d(0, Math.sin(theta), Math.cos(theta));
 		Vector3d beam = new Vector3d(detector.getBeamVector());
 		Vector3d normal = detector.getNormal();
 		
 		// scale vectors
 		radiusVector.scale(detector.getOrigin().dot(normal) / radiusVector.dot(normal));
 		beam.scale(detector.getOrigin().dot(normal) / beam.dot(normal));
 
 		radiusVector.sub(beam);
 		return radiusVector.length() / detector.getVPxSize();
 	}
 
 	/**
 	 * Calculate a conic section
 	 * @param detector
 	 * @param diffExp
 	 * @param dSpacing (in Angstroms)
 	 * @return roi
 	 */
 	public static IROI conicFromDSpacing(DetectorProperties detector, DiffractionCrystalEnvironment diffExp,
 			double dSpacing) {
 		return conicFromDSpacing(detector, diffExp.getWavelength(), dSpacing);
 	}
 
 	/**
 	 * Calculate a conic section
 	 * @param detector
 	 * @param wavelength (in same units as d-spacing)
 	 * @param dSpacing (in same units as wavelength)
 	 * @return roi
 	 */
 	public static IROI conicFromDSpacing(DetectorProperties detector, double wavelength,
 			double dSpacing) {
 		double alpha = 2 * Math.asin(wavelength / (2 * dSpacing));
 		return conicFromAngle(detector, alpha);
 	}
 
 	/**
 	 * Calculate a conic section
 	 * @param detector
 	 * @param alpha semi-angle (in radians)
 	 * @return roi
 	 */
 	public static IROI conicFromAngle(DetectorProperties detector, double alpha) {
 		final Vector3d normal = detector.getNormal();
 
 		Vector3d major = new Vector3d();
 		Vector3d minor = new Vector3d();
 		minor.cross(normal, detector.getBeamVector());
 		double se = minor.length();
 		double ce = Math.sqrt(1. - se*se);
 		if (se == 0) {
 			major.set(-1, 0, 0);
 		} else {
 			minor.normalize();
 			major.cross(minor, normal);
 		}
 
 		Vector3d intersect = null;
 		double r = 0;
 		try {
 			intersect = detector.getBeamCentrePosition();
 			r = intersect.length();
 		} catch (IllegalStateException e) {
 			// parabolic case TODO
 			throw new UnsupportedOperationException("Cannot handle parabolic case yet");
 		}
 
 		// TODO test for intersection behind sample (aka hyperbolic case)
 
 		double sa = Math.sin(alpha);
 		double ca = Math.cos(alpha);
 
 		Vector3d row = detector.getPixelRow();
		Vector3d col = detector.getPixelColumn();
		double angle = -Math.atan2(major.dot(col), major.dot(row));
 		if (se != 0) {
 			double x = r*se*sa*sa/(ca*ca - se*se);
 			major.scale(x/major.length());
 			intersect.add(major);
 		}
 		Vector3d centre = new Vector3d();
 		detector.pixelCoords(intersect, centre);
 
 		r /= detector.getVPxSize();
 		EllipticalROI eroi;
 		if (se != 0) {
 			double denom = ca*ca - se*se; // if alpha = 90 - eta it's the parabolic case TODO
 			double a = r*ce*sa*ca/denom;
 			double b = r*ce*sa/Math.sqrt(denom);
 			eroi = new EllipticalROI(a, b, angle, centre.x, centre.y);
 		} else {
 			double a = r*sa/ca;
 			eroi = new EllipticalROI(a, centre.x, centre.y);
 			eroi.setAngle(angle);
 		}
 
 		return eroi;
 	}
 }
