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
 
 import java.util.Arrays;
 
 /**
  * An elliptical region of interest
  */
 public class EllipticalROI extends ROIBase {
 	private double[] saxis; // semi-axes
 	private double ang; // angles in radians
 
 	/**
 	 * No argument constructor need for serialization
 	 */
 	public EllipticalROI() {
 		this(1, 1, 0, 0, 0);
 	}
 
 	/**
 	 * Create a circular ROI
 	 * @param croi
 	 */
 	public EllipticalROI(CircularROI croi) {
 		this(croi.getRadius(), croi.getRadius(), 0, croi.getPointX()	, croi.getPointY());
 	}
 
 	/**
 	 * Create a circular ROI
 	 * @param radius
 	 * @param ptx centre point x value
 	 * @param pty centre point y value
 	 */
 	public EllipticalROI(double radius, double ptx, double pty) {
 		this(radius, radius, 0, ptx, pty);
 	}
 
 	/**
 	 * Create an elliptical ROI
 	 * @param major semi-axis
 	 * @param minor semi-axis
 	 * @param angle major axis angle
 	 * @param ptx centre point x value
 	 * @param pty centre point y value
 	 */
 	public EllipticalROI(double major, double minor, double angle, double ptx, double pty) {
 		spt = new double[] { ptx, pty };
 		saxis = new double[] { major, minor };
 		ang = angle;
 		checkAngle();
 	}
 
 	@Override
 	public void downsample(double subFactor) {
 		super.downsample(subFactor);
 		saxis[0] /= subFactor;
 		saxis[1] /= subFactor;
 	}
 
 	@Override
 	public EllipticalROI copy() {
 		EllipticalROI croi = new EllipticalROI(saxis[0], saxis[1], ang, spt[0], spt[1]);
 		croi.setPlot(plot);
 		return croi;
 	}
 
 	/**
 	 * @return Returns reference to the semi-axes
 	 */
 	public double[] getSemiAxes() {
 		return saxis;
 	}
 
 	/**
 	 * @param index (should be 0 or 1 for major or minor axis)
 	 * @return Returns the semi-axis value
 	 */
 	public double getSemiAxis(int index) {
 		if (index < 0 || index > 1)
 			throw new IllegalArgumentException("Index should be 0 or 1");
 		return saxis[index];
 	}
 
 	/**
 	 * Set semi-axis values
 	 * @param semiaxis
 	 */
 	public void setSemiAxes(double[] semiaxis) {
 		if (saxis.length < 2)
 			throw new IllegalArgumentException("Need at least two semi-axis values");
 		saxis[0] = semiaxis[0];
 		saxis[1] = semiaxis[1];
 	}
 
 	/**
 	 * Set semi-axis value
 	 * @param index (should be 0 or 1 for major or minor axis)
 	 * @param semiaxis
 	 */
 	public void setSemiAxis(int index, double semiaxis) {
 		if (index < 0 || index > 1)
 			throw new IllegalArgumentException("Index should be 0 or 1");
 		saxis[index] = semiaxis;
 	}
 
 	/**
 	 * @return Returns the angle in degrees
 	 */
 	public double getAngleDegrees() {
 		return Math.toDegrees(ang);
 	}
 
 	/**
 	 * @param angle The angle in degrees to set
 	 */
 	public void setAngleDegrees(double angle) {
 		ang = Math.toRadians(angle);
 		checkAngle();
 	}
 
 	private final static double TWOPI = 2.0 * Math.PI;
 	/**
 	 * Make sure angle lie in permitted ranges:
 	 *  0 <= ang <= 2*pi
 	 */
 	private void checkAngle() {
 		while (ang < 0) {
 			ang += TWOPI;
 		}
 		while (ang > TWOPI) {
 			ang -= TWOPI;
 		}
 	}
 
 	/**
 	 * @return Returns the angle
 	 */
 	public double getAngle() {
 		return ang;
 	}
 
 	/**
 	 * @param angle The major axis angle to set
 	 */
 	public void setAngle(double angle) {
 		ang = angle;
 		checkAngle();
 	}
 
 	/**
 	 * @return true if ellipse is circular (i.e. its axes have the same length)
 	 */
 	public boolean isCircular() {
 		return saxis[0] == saxis[1];
 	}
 
 	/**
 	 * Get point on ellipse at given angle
 	 * @param angle in radians
 	 * @return point 
 	 */
 	public double[] getPoint(double angle) {
 		double ca = Math.cos(ang);
 		double sa = Math.sin(ang);
 		double cb = Math.cos(angle);
 		double sb = Math.sin(angle);
 
 		return new double[] { spt[0] + saxis[0]*ca*cb - saxis[1]*sa*sb, 
 				spt[1] + saxis[0]*sa*cb + saxis[1]*ca*sb};
 	}
 
 	/**
 	 * Get point on ellipse at given angle
 	 * @param angle in degrees
 	 * @return point 
 	 */
 	public double[] getPointDegrees(double angle) {
 		return getPoint(Math.toRadians(angle));
 	}
 
 	/**
 	 * Get distance from centre to point on ellipse at given angle
 	 * @param angle in radians
 	 * @return distance
 	 */
 	public double getDistance(double angle) {
 		double[] p = getPoint(angle);
		return Math.hypot(p[0] - spt[0], p[1] - spt[1]);
 	}
 
 	@Override
 	public String toString() {
 		if (isCircular()) {
 			return String.format("Centre %s Radius %g", Arrays.toString(spt), saxis[0]);
 		}
 		return String.format("Centre %s Semi-axes %s Angle %g", Arrays.toString(spt), Arrays.toString(saxis), getAngleDegrees());
 	}
 }
