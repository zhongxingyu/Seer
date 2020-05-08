 /**
  *
  */
 package org.jmist.packages;
 
 import org.jmist.toolkit.Point2;
 import org.jmist.toolkit.Point3;
 import org.jmist.toolkit.Ray3;
 import org.jmist.toolkit.Vector3;
 
 /**
  * A camera that captures light at a single point.
  * This is equivalent to the limit as the aperature
  * width and shutter speed approach zero for a
  * normal camera.  A pinhole camera has an infinite
  * depth of field (i.e., no depth of field effects
  * are observed).
  * @author bkimmel
  */
 public final class PinholeLens extends TransformableLens {
 
 	/**
 	 * Initializes the pinhole camera from the specified
 	 * field of view and aspect ratio.
 	 * @param fieldOfView The field of view in the horizontal
 	 * 		direction (in radians).  This value must be in
 	 * 		(0, PI).
 	 * @param aspectRatio The ratio between the width and
 	 * 		height of the image.  This value must be positive.
 	 */
 	public PinholeLens(double fieldOfView, double aspectRatio) {
 
 		// Compute the width and height of the virtual
 		// image plane from the provided field of view
 		// and aspect ratio.  The image plane is assumed
 		// to be one unit away from the origin.
 		width = 2.0 * Math.tan(0.5 * fieldOfView);
 		height = width / aspectRatio;
 
 	}
 
 	/* (non-Javadoc)
 	 * @see org.jmist.packages.TransformableLens#viewRayAt(org.jmist.toolkit.Point2)
 	 */
 	@Override
 	protected Ray3 viewRayAt(Point2 p) {
 
 		return new Ray3(
 			Point3.ORIGIN,
 			Vector3.unit(
 				width * (p.x() - 0.5),
				height * (0.5 - p.y()),
 				-1.0
 			)
 		);
 
 	}
 
 	/** The width of the virtual image plane. */
 	private final double width;
 
 	/** The height of the virtual image plane. */
 	private final double height;
 
 }
