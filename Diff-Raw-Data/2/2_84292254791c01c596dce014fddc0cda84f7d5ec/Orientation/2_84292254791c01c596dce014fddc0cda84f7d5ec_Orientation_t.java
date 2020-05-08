 package balle.world;
 
 public class Orientation {
 
 	private final double angleInRadians;
 
 	public static final Orientation rightAngle = new Orientation(90, false);
 
 	/**
 	 * Initialises orientation. The angle must be set to radians if useRadians
 	 * is true else the angle should be set to radians
 	 * 
 	 * @param angle
 	 * @param useRadians
 	 */
 	public Orientation(double angle, boolean useRadians) {
 
 		if (useRadians) {
 			angle = angle % (2 * Math.PI);
 			if (angle < 0)
 				angle += (2 * Math.PI);
 			angleInRadians = angle;
 		} else {
 			angle = angle % 360;
 			if (angle < 0)
 				angle += 360;
 			angleInRadians = (angle * Math.PI) / 180;
 		}
 
 	}
 
 	/**
 	 * Initialises orientation
 	 * 
 	 * @param angle
 	 *            in radians
 	 */
 	public Orientation(double angle) {
 		this(angle, true);
 	}
 
 	/**
 	 * Return the angle in radians
 	 * 
 	 * @return radians
 	 */
 	public double radians() {
 		return angleInRadians;
 	}
 
 	public double atan2styleradians() {
 		if (radians() <= Math.PI)
 			return radians();
 		else
 			return -1 * (2 * Math.PI - radians());
 
 	}
 
 	public double atan2styledegrees() {
 		if (radians() <= Math.PI)
 			return degrees();
 		else
 			return -1 * (360 - degrees());
 
 	}
 
 	/**
 	 * Return the angle in degrees
 	 * 
 	 * @return angle in degrees
 	 */
 	public double degrees() {
 		return (angleInRadians * 180) / Math.PI;
 	}
 
 	/**
 	 * Version of this angle that is between 0 and 180, returns the opposite for
 	 * reflex angles.
 	 * 
 	 * @return
 	 */
 	public Orientation abs() {
 		if (this.atan2styleradians() < 0)
 			return getOpposite();
 		else
 			return this;
 	}
 
 	@Override
 	public boolean equals(Object other) {
 		if (other == null)
 			return false;
 		if (other == this)
 			return true;
 		if (this.getClass() != other.getClass())
 			return false;
 		Orientation otherOrientation = (Orientation) other;
 		return (otherOrientation.radians() - this.radians() < 0.000001);
 	}
 
 	public Orientation sub(Orientation targetOrientation) {
 		return new Orientation(this.radians() - targetOrientation.radians(),
 				true);
 	}
 
 	public Orientation add(Orientation targetOrientation) {
 		return new Orientation(this.angleInRadians
 				+ targetOrientation.angleInRadians, true);
 	}
 
 
 	public boolean isFacingLeft(double epsilon) {
 		return (degrees() > 90 + epsilon) && (degrees() < 270 - epsilon);
 	}
 
 	public boolean isFacingRight(double epsilon) {
        return (degrees() < 90 - epsilon) || (degrees() > 270 + epsilon);
 	}
 
 	public Orientation getOpposite() {
 		return new Orientation(radians() - Math.PI, true);
 	}
 
 	public Coord getUnitCoord() {
 		return (new Coord(1, 0)).rotate(this);
 	}
 
 	public double angleToatan2Radians(Orientation to) {
 		return to.sub(this).atan2styleradians();
 	}
 
 	@Override
 	public String toString() {
 		return degrees() + "deg";
 	}
 }
