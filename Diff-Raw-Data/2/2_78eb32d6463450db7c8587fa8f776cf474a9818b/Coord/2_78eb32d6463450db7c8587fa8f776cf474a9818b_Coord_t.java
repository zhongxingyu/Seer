 package balle.world;
 
 import balle.world.objects.FieldObject;
 import balle.world.objects.Pitch;
 import balle.world.objects.Point;
 
 public class Coord {
 
 	private final double x;
 	private final double y;
 
 	private final int estimatedFrames;
 
 	public Coord(double x, double y) {
 		super();
 		this.x = x;
 		this.y = y;
 		this.estimatedFrames = 0;
 	}
 
 	public Coord(double x, double y, int estimatedFrames) {
 		super();
 		this.x = x;
 		this.y = y;
 		this.estimatedFrames = estimatedFrames;
 	}
 
 	public Coord(Coord coordinate, int estimatedFrames) {
 		this(coordinate.getX(), coordinate.getY(), estimatedFrames);
 	}
 
 	public double getX() {
 		return x;
 	}
 
 	public double getY() {
 		return y;
 	}
 
 	public double abs() {
 		return Math.sqrt(sqrAbs());
 	}
 
 	public double sqrAbs() {
 		return x * x + y * y;
 	}
 
 	public double dot(Coord c) {
 		return x * c.getX() + y * c.getY();
 	}
 
 	public Coord sub(Coord c) {
 		return new Coord(x - c.getX(), y - c.getY(), estimatedFrames);
 	}
 
 	public Coord add(Coord c) {
 		return new Coord(x + c.getX(), y + c.getY(), estimatedFrames);
 	}
 
 	public Coord mult(double scalar) {
 		return new Coord(x * scalar, y * scalar, estimatedFrames);
 	}
 
 	public Coord div(double scalar) {
		return new Coord(x / scalar, y / scalar, estimatedFrames);
 	}
 
 	public double dist(Coord c) {
 		return c.sub(this).abs();
 	}
 
 	public Orientation orientation() {
 		double orientation_atan2 = Math.atan2(this.getY(), this.getX());
 		if (orientation_atan2 < 0)
 			orientation_atan2 = 2 * Math.PI + orientation_atan2;
 		return new Orientation(orientation_atan2, true);
 	}
 
 	/**
 	 * Returns whether the coordinates have been estimated or not. By convention
 	 * the coordinates from the vision are not estimated, whereas the
 	 * coordinates that are updated from velocities e.g. when the vision returns
 	 * -1 are.
 	 * 
 	 * Equivalent to checking that getEstimatedFrames() > 1
 	 * 
 	 * @return true or false depending whether the coordinates were estimated or
 	 *         not
 	 */
 	public boolean isEstimated() {
 		return estimatedFrames > 0;
 	}
 
 	/**
 	 * Get the number of frames since this object last had a known location
 	 * 
 	 * @return the number of estimated frames
 	 */
 	public int getEstimatedFrames() {
 		return estimatedFrames;
 	}
 
 	@Override
 	public boolean equals(Object other) {
 		if (other == null)
 			return false;
 		if (other == this)
 			return true;
 		if (this.getClass() != other.getClass())
 			return false;
 		Coord otherCoord = (Coord) other;
 
 		return Math.abs(otherCoord.x - this.x) < 0.00001
 				&& Math.abs(otherCoord.y - this.y) < 0.00001
 				&& otherCoord.isEstimated() == this.isEstimated();
 	}
 
 	/**
 	 * Returns true if Coordinate is reachable via a straight line from another
 	 * coordinate
 	 * 
 	 * @param fromCoordinate
 	 *            the starting coordinate
 	 * @return true, if coordinate is reachable in straight line from another
 	 */
 	public boolean isReachableInStraightLineAndNotBlocked(Coord fromCoordinate,
 			Pitch pitch) {
 		return pitch.containsCoord(this) && pitch.containsCoord(fromCoordinate);
 	}
 
 	/**
 	 * Returns true if Coordinate is reachable via a straight line from another
 	 * coordinate and is not blocked
 	 * 
 	 * @param fromCoordinate
 	 *            the starting coordinate
 	 * @param potentialObstacle
 	 *            checks what could be the potential obstacle.
 	 * @return true, if coordinate is reachable in straight line from another
 	 */
 	public boolean isReachableInStraightLineAndNotBlocked(Coord fromCoordinate,
 			Pitch pitch, FieldObject potentialObstacle) {
 		if (!this.isReachableInStraightLineAndNotBlocked(fromCoordinate, pitch))
 			return false;
 
 		Line line = new Line(new Coord(this.getX(), this.getY()), new Coord(
 				fromCoordinate.getX(), fromCoordinate.getY()));
 
 		return !potentialObstacle.intersects(line);
 	}
 
 	/**
 	 * Creates a new coord rotated counter-clockwise by an angle. around the
 	 * origin
 	 * 
 	 * @param orientation
 	 */
 	public Coord rotate(Orientation orientation) {
 		double theta, nX, nY;
 		theta = orientation.radians();
 		nX = getX() * Math.cos(theta) - getY() * Math.sin(theta);
 		nY = getX() * Math.sin(theta) + getY() * Math.cos(theta);
 		return new Coord(nX, nY);
 	}
 
 	/**
 	 * Calculates the angle from this coordinate to another from (0,0).
 	 * 
 	 * TODO write test
 	 * 
 	 * @param to
 	 *            coordinate to.
 	 * @return Angle between this and to to using (0,0) as a reference point.
 	 */
 	public Orientation angleBetween(Coord to) {
 		return new Coord(0, 0).angleBetween(this, to);
 	}
 
 	/**
 	 * Calculates the angle from one coordinate to another from this point.
 	 * 
 	 * TODO write test
 	 * 
 	 * @param from
 	 *            First coordinate.
 	 * @param to
 	 *            Second coordinate.
 	 * @return Angle between from and to from this reference point.
 	 */
 	public Orientation angleBetween(Coord from, Coord to) {
 		Coord a = from.sub(this);
 		Coord b = to.sub(this);
 
 		return b.getOrientation().sub(a.getOrientation());
 
 		/*
 		 * Orientation out;
 		 * 
 		 * Coord dFrom, dTo; dFrom = from.sub(this); dTo = to.sub(this);
 		 * 
 		 * double dotProduct, dX, dY, mulAbs, adbc; dX = (dFrom.x * dTo.x); dY =
 		 * (dFrom.y * dTo.y); mulAbs = dFrom.abs() * dTo.abs(); dotProduct = (dX
 		 * + dY) / mulAbs;
 		 * 
 		 * adbc = from.x * to.y - from.y * to.x; if (adbc >= 0) { out = new
 		 * Orientation(Math.acos(dotProduct), true); } else { out = new
 		 * Orientation(-Math.acos(dotProduct), true); }
 		 * 
 		 * return out;
 		 */
 	}
 
 	public Orientation getOrientation() {
 		return new Orientation(Math.atan2((double) getY(), (double) getX()));
 	}
 
 	public Coord opposite() {
 		return new Coord(-getX(), -getY());
 	}
 
 	public Coord getUnitCoord() {
 		return mult(1 / abs());
 	}
 
 	public Point getPoint() {
 		return new Point(this);
 	}
 
 	@Override
 	public String toString() {
 		return "(" + x + ", " + y + ")";
 	}
 }
