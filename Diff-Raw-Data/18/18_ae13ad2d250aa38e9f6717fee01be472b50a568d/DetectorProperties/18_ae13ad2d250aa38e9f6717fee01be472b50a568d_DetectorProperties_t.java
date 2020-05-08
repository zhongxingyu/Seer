 /*
  * Copyright 2011 Diamond Light Source Ltd.
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
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.List;
 
 import javax.vecmath.Matrix3d;
 import javax.vecmath.Vector3d;
 
 import uk.ac.diamond.scisoft.analysis.diffraction.DetectorPropertyEvent.EventType;
 
 /**
  * This class will contain the information describing the properties of an area detector that are relevant to diffraction
  * calculations. The Diamond reference frame is defined so its origin is at the intersection of the beam and the sample.
  * [This is a volume but I guess it's the centre of this volume.]
  * <p>
  * The laboratory reference frame is oriented so that the z-axis is along the beam direction (or as close to that as
  * possible so it forms a orthogonal basis with its other two axis), its y-axis is anti-parallel to local direction of
  * gravity, and its x-axis is horizontal. The area detector has a frame that describes its orientation relative to the
  * laboratory reference frame. In an idealised case, the detector frame coincides with the laboratory but has its origin
  * situated at the top-left corner of the top-leftmost pixel of the corrected image that recorded by the detector. The
  * image is presented as if seen from the beam source's perspective and image coordinates start off with (0,0) in the
  * top-left corner of the image and end at (width-1,height-1) in the bottom-right corner pixel. Thus the image rows and
  * columns are anti-parallel to the area detector frame's x and y axes and its z-axis is anti-parallel to the area
  * detector's outward (to the sample) normal.
  */
 public class DetectorProperties implements Serializable {
 	/**
 	 * Update this when there are any serious changes to API
 	 */
 	static final long serialVersionUID = 3928760686423603813L; 
 
 	private Vector3d origin; // top left corner of detector's (0,0) pixel
 	private Vector3d beamVector; // unit vector in beam direction
 	private Vector3d normal; // unit vector perpendicular to detector surface
 	private int px; // width in pixels
 	private int py; // height in pixels
 	private double hPxSize; // horizontal pixel size (in mm)
 	private double vPxSize; // vertical pixel size (in mm)
 	private Matrix3d orientation; // transformation from reference frame to detector frame
 	private Matrix3d invOrientation; // its inverse
 	private Matrix3d ta;
 	private Matrix3d tb;
 	// TODO move controller away from model?
 	private boolean fire = true;
 	private HashSet<IDetectorPropertyListener> detectorPropListeners; 
 	
 	/**
 	 * Null constructor
 	 */
 	public DetectorProperties() {
 		ta = new Matrix3d();
 		tb = new Matrix3d();
 		normal = new Vector3d(0, 0, -1);
 	}
 	
 	/**
 	 * This assumes beam is along z-axis
 	 * 
 	 * @param origin
 	 *            The local origin of the detector plane relative to the reference frame. This origin indicates the top
 	 *            left corner of the detector's (0,0) pixel. Distances in mm
 	 * @param heightInPixels
 	 *            Detector height in pixels
 	 * @param widthInPixels
 	 *            Detector width in pixels
 	 * @param pixelHeightInMM
 	 *            pixel height in mm
 	 * @param pixelWidthInMM
 	 *            pixel width in mm
 	 * @param orientation
 	 *            matrix describing the orientation of the detector relative to the reference frame. This matrix's
 	 *            columns describes the direction of decreasing image rows, the direction of decreasing image columns
 	 *            and the detector plane normal.
 	 */
 	public DetectorProperties(Vector3d origin, final int heightInPixels, final int widthInPixels, final double pixelHeightInMM,
 			final double pixelWidthInMM, Matrix3d orientation) {
 		this(origin, new Vector3d(0, 0, 1), heightInPixels, widthInPixels, pixelHeightInMM, pixelWidthInMM, orientation);
 	}
 
 	/**
 	 * This assumes beam is along z-axis
 	 * 
 	 * @param origin
 	 *            The local origin of the detector plane relative to the reference frame. This origin indicates the top
 	 *            left corner of the detector's (0,0) pixel. Distances in mm
 	 * @param heightInPixels
 	 *            Detector height in pixels
 	 * @param widthInPixels
 	 *            Detector width in pixels
 	 * @param pixelHeightInMM
 	 *            pixel height in mm
 	 * @param pixelWidthInMM
 	 *            pixel width in mm
 	 * @param detectorRotationX value describing the orientation of the detector relative to the reference frame
 	 * @param detectorRotationY value describing the orientation of the detector relative to the reference frame
 	 * @param detectorRotationZ value describing the orientation of the detector relative to the reference frame
 	 */
 	public DetectorProperties(Vector3d origin, final int heightInPixels, final int widthInPixels, final double pixelHeightInMM,
 			final double pixelWidthInMM, final double detectorRotationX, final double detectorRotationY, final double detectorRotationZ) {
 		this();
 		Matrix3d rotX = new Matrix3d();
 		rotX.rotX(detectorRotationX);
 		Matrix3d rotY = new Matrix3d();
 		rotY.rotY(detectorRotationY);
 		Matrix3d rotZ = new Matrix3d();
 		rotZ.rotZ(detectorRotationZ);
 
 		Matrix3d euler = new Matrix3d();
 		euler.mul(rotX, rotY);
 		euler.mul(rotZ);
 		
 		this.origin = origin;
 		this.beamVector = new Vector3d(0, 0, 1);
 		this.beamVector.normalize();
 		px = widthInPixels;
 		py = heightInPixels;
 		vPxSize = pixelHeightInMM;
 		hPxSize = pixelWidthInMM;
 		
 		this.orientation = euler;
 		if (this.orientation == null) {
 			this.orientation = new Matrix3d();
 			this.orientation.setIdentity();
 		}
 		calcNormalAndInverse();
 	}
 
 	
 	/**
 	 * @param origin
 	 *            The local origin of the detector plane relative to the reference frame. This origin indicates the top
 	 *            left corner of the detector's (0,0) pixel. Distances in mm
 	 * @param beamVector
 	 *            A unit vector describing the beam position.
 	 * @param heightInPixels
 	 *            Detector height in pixels
 	 * @param widthInPixels
 	 *            Detector width in pixels
 	 * @param pixelHeightInMM
 	 *            pixel height in mm
 	 * @param pixelWidthInMM
 	 *            pixel width in mm
 	 * @param orientation
 	 *            matrix describing the orientation of the detector relative to the reference frame. This matrix's
 	 *            columns describes the direction of decreasing image rows, the direction of decreasing image columns
 	 *            and the detector plane normal.
 	 */
 	public DetectorProperties(Vector3d origin, Vector3d beamVector, final int heightInPixels, final int widthInPixels, final double pixelHeightInMM,
 			final double pixelWidthInMM, Matrix3d orientation) {
 		this();
 		this.origin = origin;
 		this.beamVector = beamVector;
 		this.beamVector.normalize();
 		px = widthInPixels;
 		py = heightInPixels;
 		vPxSize = pixelHeightInMM;
 		hPxSize = pixelWidthInMM;
 		
 		this.orientation = orientation;
 		if (this.orientation == null) {
 			this.orientation = new Matrix3d();
 			this.orientation.setIdentity();
 		}
 		calcNormalAndInverse();
 	}
 
 	/**
 	 * @param detprop
 	 *            the DetectorProperties to copy
 	 */
 	protected DetectorProperties(DetectorProperties detprop) {
 		this();
 		origin = new Vector3d(detprop.origin);
 		beamVector = new Vector3d(detprop.beamVector);
 		beamVector.normalize();
 		px = detprop.getPx();
 		py = detprop.getPy();
 		vPxSize = detprop.getVPxSize();
 		hPxSize = detprop.getHPxSize();
 		
 		if (detprop.orientation == null) {
 			orientation = new Matrix3d();
 			orientation.setIdentity();
 		} else {
 			orientation = new Matrix3d(detprop.orientation);
 		}
 		calcNormalAndInverse();
 	}
 
 	/**
 	 * Produce a Detector properties object populated with sensible default values given image shape.
 	 * It produces a detector normal to the beam and centred on the beam with square pixels of 0.1024mm and set 200mm
 	 * from the sample.
 	 * 
 	 * @param shape image shape
 	 */
 	public static DetectorProperties getDefaultDetectorProperties(int[] shape) {
 		int heightInPixels = shape[0];
 		int widthInPixels = shape[1];
 		
 		// Set a few default values
 		double pixelSizeX = 0.1024;
 		double pixelSizeY = 0.1024;
 		double distance = 200.00;
 
 		// Create the detector origin vector based on the above
 		Vector3d dOrigin = new Vector3d((widthInPixels - widthInPixels/2d) * pixelSizeX, (heightInPixels - heightInPixels/2d) * pixelSizeY, distance);
 
 		return new DetectorProperties(dOrigin, heightInPixels, widthInPixels, pixelSizeX, pixelSizeY, null);
 	}
 
 	@Override
 	public  DetectorProperties clone() {
 		return new DetectorProperties(this);
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + ((beamVector == null) ? 0 : beamVector.hashCode());
 		long temp;
 		temp = Double.doubleToLongBits(hPxSize);
 		result = prime * result + (int) (temp ^ (temp >>> 32));
 		result = prime * result + ((orientation == null) ? 0 : orientation.hashCode());
 		result = prime * result + ((origin == null) ? 0 : origin.hashCode());
 		result = prime * result + px;
 		result = prime * result + py;
 		temp = Double.doubleToLongBits(vPxSize);
 		result = prime * result + (int) (temp ^ (temp >>> 32));
 		return result;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		DetectorProperties other = (DetectorProperties) obj;
 		if (beamVector == null) {
 			if (other.beamVector != null)
 				return false;
 		} else if (!beamVector.equals(other.beamVector))
 			return false;
 		if (Double.doubleToLongBits(hPxSize) != Double.doubleToLongBits(other.hPxSize))
 			return false;
 		if (orientation == null) {
 			if (other.orientation != null)
 				return false;
 		} else if (!orientation.equals(other.orientation))
 			return false;
 		if (origin == null) {
 			if (other.origin != null)
 				return false;
 		} else if (!origin.equals(other.origin))
 			return false;
 		if (px != other.px)
 			return false;
 		if (py != other.py)
 			return false;
 		if (Double.doubleToLongBits(vPxSize) != Double.doubleToLongBits(other.vPxSize))
 			return false;
 		return true;
 	}
 
 	
 
 	private void calcNormalAndInverse() {
 		// calculate the vector from the origin of the detector that is perpendicular to the plane of the detector.
 		normal.set(0, 0, -1);
 
 		orientation.transform(normal);
 
 		if (invOrientation == null)
 			invOrientation = new Matrix3d(orientation);
 		else
 			invOrientation.set(orientation);
 
 		invOrientation.transpose(); // assume it's orthogonal
 		// invOrientation.invert();
 	}
 
 	/**
 	 * @return a vector describing the row-wise component of a detector pixel in space.
 	 * I.e. the horizontal (in an image) edge of a pixel
 	 */
 	public Vector3d getPixelRow() {
 		Vector3d horVec = new Vector3d(-hPxSize, 0, 0);
 
 		orientation.transform(horVec);
 		return horVec;
 	}
 
 	/**
 	 * @return a vector describing the column-wise component of a detector pixel in space
 	 * I.e. the vertical (in an image) edge of a pixel
 	 */
 	public Vector3d getPixelColumn() {
 		Vector3d vertVec = new Vector3d(0, -vPxSize, 0);
 
 		orientation.transform(vertVec);
 		return vertVec;
 	}
 
 	/**
 	 * @return origin of the detector (top-left corner of (0,0) pixel)
 	 */
 	public Vector3d getOrigin() {
 		return origin;
 	}
 
 	/**
 	 * @param origin
 	 *            of the detector (top-left corner of (0,0) pixel)
 	 */
 	public void setOrigin(Vector3d origin) {
 		this.origin = origin;
 		// Tell listeners
 		fireDetectorPropertyListeners(new DetectorPropertyEvent(this, EventType.ORIGIN));
 	}
 
 	/**
 	 * Get distance from sample to beam centre.
 	 * @return distance can be infinity if direct beam does not intersect detector.
 	 */
 	public double getBeamCentreDistance() {
 		try {
 			return getBeamCentrePosition().length();
 		} catch (IllegalStateException e) {
 			return Double.POSITIVE_INFINITY;
 		}
 	}
 
 	/**
 	 * Set distance from sample to beam centre.
 	 * <p>
 	 * Can throw an exception if direct beam does not intersect detector.
 	 * @param distance
 	 */
 	public void setBeamCentreDistance(double distance) {
 		distance -= getBeamCentrePosition().length();
 		Vector3d b = new Vector3d(beamVector);
 		b.scale(distance);
 		origin.add(b);
 		fireDetectorPropertyListeners(new DetectorPropertyEvent(this, EventType.ORIGIN));
 	}
 
 	/**
 	 * Get distance from sample to detector
 	 * @return distance from sample to closest point on detector 
 	 */
 	public double getDetectorDistance() {
 		return -normal.dot(origin);
 	}
 
 	/**
 	 * Set distance from sample to detector
 	 * @param distance
 	 */
 	public void setDetectorDistance(double distance) {
 		Vector3d b = new Vector3d(normal);
 		b.scale(getDetectorDistance()-distance);
 		origin.add(b);
 		fireDetectorPropertyListeners(new DetectorPropertyEvent(this, EventType.ORIGIN));
 	}
 
 	/**
 	 * @return number of pixels in the x direction
 	 */
 	public int getPx() {
 		return px;
 	}
 
 	/**
 	 * @param px
 	 *            number of pixels in the x direction
 	 */
 	public void setPx(final int px) {
 		this.px = px;
 	}
 
 	/**
 	 * @return number of pixels in the y direction
 	 */
 	public int getPy() {
 		return py;
 	}
 
 	/**
 	 * @param py
 	 *            number of pixels in the y direction
 	 */
 	public void setPy(final int py) {
 		this.py = py;
 	}
 
 	/**
 	 * @return vertical size of pixels in mm
 	 */
 	public double getVPxSize() {
 		return vPxSize;
 	}
 
 	/**
 	 * @param pxSize
 	 *            vertical size of pixels in mm
 	 */
 	public void setVPxSize(final double pxSize) {
 		vPxSize = pxSize;
 		fireDetectorPropertyListeners(new DetectorPropertyEvent(this, EventType.VPXSIZE));
 	}
 
 	/**
 	 * @return horizontal pixel size in mm
 	 */
 	public double getHPxSize() {
 		return hPxSize;
 	}
 
 	/**
 	 * @param pxSize
 	 *            horizontal pixel size in mm
 	 */
 	public void setHPxSize(final double pxSize) {
 		hPxSize = pxSize;
 		fireDetectorPropertyListeners(new DetectorPropertyEvent(this, EventType.HPXSIZE));
 	}
 
 	/**
 	 * @return size of detector in mm
 	 */
 	public double getDetectorSizeV() {
 		return vPxSize * py;
 	}
 
 	/**
 	 * @return size of detector in mm
 	 */
 	public double getDetectorSizeH() {
 		return hPxSize * px;
 	}
 
 	/**
 	 * @return detector normal
 	 */
 	public Vector3d getNormal() {
 		return normal;
 	}
 
 	/**
 	 * @param orientation
 	 *            matrix describing the orientation of the detector relative to the origin of the detector that
 	 *            describes the position of the detector relative to the crystal in space.
 	 */
 	public void setOrientation(Matrix3d orientation) {
 		this.orientation = orientation;
 		calcNormalAndInverse();
 	}
 
 	/**
 	 * Set detector orientation using a set of (proper) Euler angles in ZXZ order
 	 * 
 	 * @param alpha first angle about global z
 	 * @param beta second angle about local x
 	 * @param gamma third angle about local z
 	 */
 	public void setOrientationEulerZXZ(final double alpha, final double beta, final double gamma) {
 		if (orientation == null)
 			orientation = new Matrix3d();
 		ta.rotZ(alpha);
 		tb.rotX(beta);
 		tb.mul(ta);
 		orientation.rotZ(gamma);
 		orientation.mul(tb);
 		calcNormalAndInverse();
 	}
 
 	/**
 	 * Set detector orientation using a set of (proper) Euler angles in ZYZ order
 	 * 
 	 * @param alpha first angle about global z
 	 * @param beta second angle about local y
 	 * @param gamma third angle about local z
 	 */
 	public void setOrientationEulerZYZ(final double alpha, final double beta, final double gamma) {
 		if (orientation == null)
 			orientation = new Matrix3d();
 		ta.rotZ(alpha);
 		tb.rotY(beta);
 		tb.mul(ta);
 		orientation.rotZ(gamma);
 		orientation.mul(tb);
 		calcNormalAndInverse();
 	}
 
 	/**
 	 * Set detector normal (from face out to sample) using a set of yaw, pitch and roll angles in degrees.
 	 * <p>
 	 * Note, this re-orients the detector about the beam centre and therefore alters the detector origin.
 	 * 
 	 * @param yaw rotate about vertical axis (positive is to the right, east or clockwise looking down)
 	 * @param pitch rotate about horizontal axis (positive is upwards)
 	 * @param roll rotate about normal (positive is clockwise looking along normal)
 	 */
 	public void setNormalAnglesInDegrees(final double yaw, final double pitch, final double roll) {
 		Vector3d c = getBeamCentrePosition();
 		Vector3d d = new Vector3d();
		d.sub(origin, c);
 		invOrientation.transform(d);  // relative beam centre in image frame
 
 		if (orientation == null)
 			orientation = new Matrix3d();
 		ta.rotY(Math.toRadians(-yaw));
 		tb.rotX(Math.toRadians(pitch));
 		tb.mul(ta);
 		orientation.rotZ(Math.toRadians(-roll));
 		orientation.mul(tb);
 		calcNormalAndInverse();
 
 		// set origin back from beam centre
 		orientation.transform(d);
 		c.add(d);
 		origin = c;
 
 		fireDetectorPropertyListeners(new DetectorPropertyEvent(this, EventType.NORMAL));
 	}
 
 	/**
 	 * Get detector normal (from face out to sample) as a set of yaw, pitch and roll angles in degrees.
 	 * Note, in the gimbal lock case (when pitch is +/- 90 degrees), roll is returned as zero.
 	 * @return yaw, pitch and roll as defined in {@link #setNormalAnglesInDegrees(double, double, double)}
 	 */
 	public double[] getNormalAnglesInDegrees() {
 		if (orientation == null)
 			return new double[3];
 
 		double sp = orientation.getM21();
 		double cp = Math.sqrt(1 - sp * sp);
 		double yaw;
 		double roll;
 		if (cp == 0) {
 			// gimbal lock case
 			yaw  = -Math.atan2(orientation.getM10(), orientation.getM00());
 			roll = 0;
 		} else {
 			yaw  = Math.atan2(orientation.getM20(), orientation.getM22());
 			roll = Math.atan2(orientation.getM01(), orientation.getM11());
 		}
 
 		double pitch = Math.asin(sp);
 		return new double[] {Math.toDegrees(yaw), Math.toDegrees(pitch), Math.toDegrees(roll)};
 	}
 
 	/**
 	 * @return orientation matrix describing the orientation of the detector relative to the origin of the detector that
 	 *         describes the position of the detector relative to the crystal in space.
 	 */
 	public Matrix3d getOrientation() {
 		return orientation;
 	}
 
 	/**
 	 * @param beamVector
 	 *            The beam vector to set.
 	 */
 	public void setBeamVector(Vector3d beamVector) {
 		this.beamVector = beamVector;
 		beamVector.normalize();
 	}
 
 	/**
 	 * @return Returns the beam unit vector.
 	 */
 	public Vector3d getBeamVector() {
 		return beamVector;
 	}
 
 	/**
 	 * from image coordinates, work out position of pixel's top-left corner
 	 */
 	public void pixelPosition(final double x, final double y, Vector3d p) {
 		p.set(-hPxSize * x, -vPxSize * y, 0);
 		orientation.transform(p);
 		p.add(origin);
 	}
 
 	/**
 	 * from image coordinates, work out position of pixel's top-left corner
 	 */
 	public void pixelPosition(final int x, final int y, Vector3d p) {
 		pixelPosition((double) x, (double) y, p);
 	}
 
 	/**
 	 * @return position vector of pixel's top-left corner
 	 */
 	public Vector3d pixelPosition(final double x, final double y) {
 		Vector3d pos = new Vector3d();
 		pixelPosition(x, y, pos);
 		return pos;
 	}
 
 	/**
 	 * @return position vector of pixel's top-left corner
 	 */
 	public Vector3d pixelPosition(final int x, final int y) {
 		return pixelPosition((double) x, (double) y);
 	}
 
 	/**
 	 * from position on detector, work out pixel coordinates
 	 * 
 	 * @param p
 	 *            position vector
 	 * @param t
 	 *            output vector (x and y components are pixel coordinates)
 	 */
 	public void pixelCoords(final Vector3d p, Vector3d t) {
 		t.set(p);
 		t.sub(origin);
 		invOrientation.transform(t);
 		t.x /= -hPxSize;
 		t.y /= -vPxSize;
 	}
 
 	/**
 	 * from position on detector, work out pixel coordinates
 	 * 
 	 * @param p
 	 *            position vector
 	 * @param t
 	 *            output vector (x and y components are pixel coordinates)
 	 * @param coords
 	 *            double pixel coordinates
 	 */
 	public void pixelCoords(final Vector3d p, Vector3d t, double[] coords) {
 		pixelCoords(p, t);
 		coords[0] = t.x;
 		coords[1] = t.y;
 	}
 
 	/**
 	 * from position on detector, work out pixel coordinates
 	 * 
 	 * @param p
 	 *            position vector
 	 * @param t
 	 *            output vector (x and y components are pixel coordinates)
 	 * @param coords
 	 *            integer pixel coordinates
 	 */
 	public void pixelCoords(final Vector3d p, Vector3d t, int[] coords) {
 		pixelCoords(p, t);
 		coords[0] = (int) Math.floor(t.x);
 		coords[1] = (int) Math.floor(t.y);
 	}
 
 	/**
 	 * from position on detector, work out pixel coordinates
 	 * 
 	 * @param p
 	 *            position vector
 	 * @param coords
 	 *            double pixel coordinates
 	 */
 	public void pixelCoords(final Vector3d p, double[] coords) {
 		Vector3d t = new Vector3d();
 		pixelCoords(p, t, coords);
 	}
 
 	/**
 	 * from position on detector, work out pixel coordinates
 	 * 
 	 * @param p
 	 *            position vector
 	 * @param coords
 	 *            integer pixel coordinates
 	 */
 	public void pixelCoords(final Vector3d p, int[] coords) {
 		Vector3d t = new Vector3d();
 		pixelCoords(p, t, coords);
 	}
 
 	/**
 	 * from position on detector, work out pixel coordinates
 	 * 
 	 * @param p
 	 *            position vector
 	 * @return integer array of pixel coordinates
 	 */
 	public int[] pixelCoords(final Vector3d p) {
 		int[] coords = new int[2];
 		pixelCoords(p, coords);
 		return coords;
 	}
 
 	/**
 	 * from position on detector, work out pixel coordinates
 	 * 
 	 * @param p
 	 *            position vector
 	 * @return integer array of pixel coordinates
 	 */
 	public double[] pixelPreciseCoords(final Vector3d p) {
 		double[] coords = new double[2];
 		pixelCoords(p, coords);
 		return coords;
 	}
 
 	/**
 	 * @return scattering angle (two-theta) associated with pixel
 	 */
 	public double pixelScatteringAngle(final double x, final double y) {
 		Vector3d p = pixelPosition(x, y);
 		p.normalize();
 		return Math.acos(p.dot(beamVector));
 	}
 
 	/**
 	 * @return scattering angle (two-theta) associated with pixel
 	 */
 	public double pixelScatteringAngle(final int x, final int y) {
 		return pixelScatteringAngle((double) x, (double) y);
 	}
 
 	/**
 	 * Get beam centre position.
 	 * <p>
 	 * Can throw an illegal state exception when there is no intersection
 	 * @return position of intersection of direct beam with detector
 	 */
 	public Vector3d getBeamCentrePosition() {
 		try {
 			return intersect(beamVector);
 		} catch (IllegalArgumentException e) {
 			throw new IllegalStateException("No intersection when beam is parallel to detector");
 		}
 	}
 
 	/**
 	 * @param v vector
 	 * @return point of intersection of vector with detector
 	 */
 	public Vector3d intersect(final Vector3d v) {
 		Vector3d pos = new Vector3d();
 		intersect(v, pos);
 		return pos;
 	}
 
 	/**
 	 * Calculate point of intersection of vector with detector
 	 * 
 	 * @param v
 	 *            vector (does not have to be a unit vector)
 	 * @param p
 	 *            position vector of intersection
 	 */
 	public void intersect(final Vector3d v, Vector3d p) {
 		p.set(v);
 		double t = normal.dot(v);
 		if (t == 0) {
 			throw new IllegalArgumentException("No intersection possible as vector is parallel to detector");
 		}
 		t = normal.dot(origin) / t;
 		p.scale(t);
 	}
 
 	private Vector3d[] cornerPositions() {
 		Vector3d[] corners = new Vector3d[4];
 		corners[0] = new Vector3d(origin);
 		corners[1] = pixelPosition(px, 0);
 		corners[2] = pixelPosition(0, py);
 		corners[3] = pixelPosition(px, py);
 		return corners;
 	}
 
 	/**
 	 * Can throw illegal state exception
 	 * @return The pixel position on the edge of the detector which is closest to the beam centre
 	 */
 	private int[] pixelClosestToBeamCentre() {
 		if (!inImage(getBeamCentrePosition()))
 			throw new IllegalStateException("The beam does not intersect the detector");
 		double[] beamCentre = pixelPreciseCoords(getBeamCentrePosition());
 		double shortest = Double.MAX_VALUE;
 		int[] closestCoords = new int[2];
 		double d = distBetweenPix(beamCentre[0], beamCentre[1], beamCentre[0], 0); 
 		if (d < shortest) {
 			closestCoords[0] = (int) beamCentre[0];
 			closestCoords[1] = 0;
 			shortest = d;
 		}
 		d = distBetweenPix(beamCentre[0], beamCentre[1], 0, beamCentre[1]); 
 		if (d < shortest) {
 			closestCoords[0] = 0;
 			closestCoords[1] = (int) beamCentre[1];
 			shortest = d;
 		}
 		d = distBetweenPix(beamCentre[0], beamCentre[1], beamCentre[0] - px, py);
 		if (d < shortest) {
 			closestCoords[0] = (int) (px - beamCentre[0]);
 			closestCoords[1] = py;
 			shortest = d;
 		}
 		d = distBetweenPix(beamCentre[0], beamCentre[1], px, beamCentre[1] - py);
 		if (d < shortest) {
 			closestCoords[0] = px;
 			closestCoords[1] = (int) (py - beamCentre[1]);
 			shortest = d;
 		}
 		return closestCoords;
 	}
 
 	/**
 	 * @return pixel coordinates of the beam centre (where beam intersects detector)
 	 */
 	public double[] getBeamCentreCoords() {
 		final Vector3d cen = new Vector3d();
 		pixelCoords(getBeamCentrePosition(), cen);
 		return new double[] { cen.x, cen.y };
 	}
 
 	/**
 	 * Set beam centre (where beam intersects detector)
 	 * @param coords in image
 	 */
 	public void setBeamCentreCoords(double[] coords) {
 		Vector3d oc = getBeamCentrePosition(); // old beam centre
 		oc.sub(pixelPosition(coords[0], coords[1]));
 		origin.add(oc); // shift origin accordingly
 		// Tell listeners
 		fireDetectorPropertyListeners(new DetectorPropertyEvent(this, EventType.BEAM_CENTRE));
 	}
 
 	public void addDetectorPropertyListener(IDetectorPropertyListener l) {
 		if (detectorPropListeners==null) 
 			detectorPropListeners = new HashSet<IDetectorPropertyListener>(5);
 		detectorPropListeners.add(l);
 	}
 
 	/**
 	 * Call from dispose of part listening to listen to detector properties changing
 	 * @param l
 	 */
 	public void removeDetectorPropertyListener(IDetectorPropertyListener l) {
 		if (detectorPropListeners==null)  return;
 		detectorPropListeners.remove(l);
 	}
 
 	protected void fireDetectorPropertyListeners(DetectorPropertyEvent evt) {
 		if (detectorPropListeners==null || !fire) 
 			return;
 		for (IDetectorPropertyListener l : detectorPropListeners) {
 			l.detectorPropertiesChanged(evt);
 		}
 	}
 
 	private static double distBetweenPix(double p1x, double p1y, double p2x, double p2y) {
 		return Math.hypot(p2x - p1x, p2y - p1y);
 	}
 
 //	public Vector3d vectorToClosestPoint() {
 //		int[] closestCoords = pixelClosestToBeamCentre();
 //		Vector3d beamToClosestPoint = new Vector3d();
 //		beamToClosestPoint.sub(pixelPosition(closestCoords[0], closestCoords[1]), getBeamPosition());
 //		return beamToClosestPoint;
 //	}
 
 	/**
 	 * Can throw illegal state exception if beam does not intersect detector
 	 * @return distance to closest edge from beam centre in pixels
 	 */
 	public int distToClosestEdgeInPx() {
 		int[] closestCoords = pixelClosestToBeamCentre();
 		double[] beamCoords = pixelPreciseCoords(getBeamCentrePosition());
 		return (int) distBetweenPix(closestCoords[0], closestCoords[1], beamCoords[0], beamCoords[1]);
 	}
 
 	/**
 	 * @return longest vector from beam centre to farthest corner
 	 */
 	public Vector3d getLongestVector() {
 		Vector3d[] corners = cornerPositions();
 		Vector3d longVec = new Vector3d();
 		double length = -Double.MAX_VALUE;
 		for (int i = 0; i < 4; i++) {
 			Vector3d tempVec = new Vector3d();
 			tempVec.sub(corners[i], getBeamCentrePosition());
 			double vecLength = tempVec.length();
 			if (vecLength > length) {
 				longVec = tempVec;
 				length = longVec.length();
 			}
 		}
 		return longVec;
 	}
 
 	/**
 	 * @return maximum scattering angle (two-theta) that detector can see
 	 */
 	public double getMaxScatteringAngle() {
 		Vector3d[] corners = cornerPositions();
 		List<Double> dots = new ArrayList<Double>();
 
 		for (int i = 0; i < 4; i++) {
 			corners[i].normalize();
 			dots.add(corners[i].dot(beamVector));
 		}
 		Collections.sort(dots);
 		return Math.acos(dots.get(0)); // use smallest cos(two-theta)
 	}
 
 	/**
 	 * @param x
 	 * @param y
 	 * @return true if given pixel coordinate is within bounds
 	 */
 	public boolean inImage(final double x, final double y) {
 		return x >= 0 && y < px && y >= 0 && y < py;
 	}
 
 	/**
 	 * @param coords
 	 * @return true if given pixel coordinate is within bounds
 	 */
 	public boolean inImage(double[] coords) {
 		return inImage(coords[0], coords[1]);
 	}
 
 	/**
 	 * @param coords
 	 * @return true if given pixel coordinate is within bounds
 	 */
 	public boolean inImage(int[] coords) {
 		return inImage(coords[0], coords[1]);
 	}
 
 	/**
 	 * @param p
 	 * @return true if given pixel position vector is within bounds
 	 */
 	public boolean inImage(final Vector3d p) {
 		return inImage(pixelPreciseCoords(p));
 	}
 
 	/**
 	 * 
 	 * @param original
 	 */
 	public void restore(DetectorProperties original) {
 		fire = false;
 		setOrigin(new Vector3d(original.getOrigin()));
 		setBeamVector(new Vector3d(original.getBeamVector()));
 		setPx(original.getPx());
 		setPy(original.getPy());
 		setVPxSize(original.getVPxSize());
 		setHPxSize(original.getHPxSize());
 		fire = true;
 		setOrientation(new Matrix3d(original.getOrientation()));
 	}

	@Override
	public String toString() {
		return "DP: o = " + origin + ", n = " + normal;
	}
 }
