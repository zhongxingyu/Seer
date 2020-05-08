 /**
  *
  */
 package ca.eandb.jmist.math;
 
 import java.io.Serializable;
 
 
 /**
  * A sphere in two dimensional space (the set of points at most a
  * constant distance from a fixed point).
  * @author Brad Kimmel
  */
 public final class Sphere implements Serializable {
 
 	/**
 	 * Initializes the center and radius of the sphere.
 	 * @param center The center of the sphere.
 	 * @param radius The radius of the sphere (must be non-negative).
 	 */
 	public Sphere(Point3 center, double radius) {
 		assert(radius >= 0.0);
 
 		this.center = center;
 		this.radius = radius;
 	}
 
 	/**
 	 * Gets the center of the sphere.
 	 * @return The center of the sphere.
 	 */
 	public Point3 center() {
 		return center;
 	}
 
 	/**
 	 * Gets the radius of the sphere.
 	 * @return The radius of the sphere.
 	 */
 	public double radius() {
 		return radius;
 	}
 
 	/**
 	 * Determines if this sphere is empty.
 	 * @return A value indicating if this sphere is empty.
 	 */
 	public boolean isEmpty() {
 		return Double.isNaN(radius);
 	}
 
 	/**
 	 * Determines if this sphere contains the specified point.
 	 * @param p The point to check for containment of.
 	 * @return A value indicating if p is within this sphere.
 	 */
 	public boolean contains(Point3 p) {
 		return center.distanceTo(p) <= radius;
 	}
 
 	/**
 	 * Computes the volume of the sphere.
 	 * @return The volume of the sphere.
 	 */
 	public double volume() {
 		return (4.0 / 3.0) * Math.PI * radius * radius * radius;
 	}
 
 	/**
 	 * Computes the diameter of the sphere.
 	 * @return The diameter of the sphere.
 	 */
 	public double diameter() {
 		return 2.0 * radius;
 	}
 
 	/**
 	 * Computes the surface area of the sphere.
 	 * @return The surface area of the sphere.
 	 */
 	public double surfaceArea() {
 		return 4.0 * Math.PI * radius * radius;
 	}
 
 	/**
 	 * Expands this sphere outwards by the specified amount.
 	 * @param amount The amount to expand the sphere by.
 	 * @return The expanded sphere.
 	 */
 	public Sphere expand(double amount) {
 		return getInstance(center, radius + amount);
 	}
 
 	/**
 	 * Expands this sphere outwards to encompass the specified point.
 	 * Guarantees that {@code this.contains(p)} after this method is called.
 	 * @param p The point to include in this sphere.
 	 * @return The expanded sphere.
 	 */
 	public Sphere expandTo(Point3 p) {
 		if (isEmpty()) {
 			return new Sphere(p, 0.0);
 		} else {
 			double newRadius = center.distanceTo(p);
 
 			if (newRadius < radius) {
 				return this;
 			} else {
 				return new Sphere(center, newRadius);
 			}
 		}
 	}
 
 	/**
 	 * Determines whether the specified ray intersects with
 	 * this sphere.  Equivalent to {@code !this.intersect(ray).isEmpty()}.
 	 * @param ray The ray to test for an intersection with.
 	 * @return A value indicating whether the ray intersects
 	 * 		this sphere.
 	 * @see {@link #intersect(Ray3)}, {@link Interval#isEmpty()}.
 	 */
 	public boolean intersects(Ray3 ray) {
 
 		//
 		// Algorithm from:
 		//
 		// A.S. Glassner, Ed.,
 		// "An Introduction to Ray Tracing",
 		// Morgan Kaufmann Publishers, Inc., San Francisco, CA, 2002
 		// Section 2.2
 		//
 
 		if (isEmpty()) {
 			return false;
 		}
 
 		double		r2 = radius * radius;
 		Vector3		oc = ray.origin().vectorTo(center);
 		double		L2oc = oc.dot(oc);
 
 		if (L2oc < r2) return true;
 
 		double		tca = oc.dot(ray.direction());
 
 		if (tca < 0.0) return false;
 
 		return ray.pointAt(tca).squaredDistanceTo(center) < r2;
 
 	}
 
 	/**
 	 * Computes the intersection of this sphere with the given
 	 * ray.
 	 * @param ray The ray to compute the intersection of this
 	 * 		sphere with.
 	 * @return The interval in which the ray passes through
 	 * 		the sphere (i.e., this.contains(ray.pointAt(x)) if and
 	 * 		only if this.intersect(ray).contains(x)).
 	 * @see {@link #contains(Point3)}, {@link Ray3#pointAt(double)},
 	 * 		{@link Interval#contains(double)}.
 	 */
 	public Interval intersect(Ray3 ray) {
 
 		//
 		// Algorithm from:
 		//
 		// A.S. Glassner, Ed.,
 		// "An Introduction to Ray Tracing",
 		// Morgan Kaufmann Publishers, Inc., San Francisco, CA, 2002
 		// Section 2.2
 		//
 
 		// Check for an empty box.
 		if (isEmpty()) {
 			return Interval.EMPTY;
 		}
 
 		// Check if the ray starts from within the box.
 		double		r2 = radius * radius;
 		Vector3		oc = ray.origin().vectorTo(center);
 		double		L2oc = oc.dot(oc);
 		boolean		startInside = (L2oc < r2);
 
 		// distance along ray to point on ray closest to center of sphere (equation (A10)).
 		double		tca = oc.dot(ray.direction());
 
 		// if the ray starts outside the sphere and points away from the center of the
		// sphwere, then the ray does not hit the sphere.
 		if (!startInside && tca < 0.0) {
 			return Interval.EMPTY;
 		}
 
 		// compute half chord distance squared (equation (A13)).
 		double		t2hc = r2 - L2oc + (tca * tca);
 
 		if (t2hc < 0.0) {
 			return Interval.EMPTY;
 		}
 
 		double		thc = Math.sqrt(t2hc);
 
 		// compute interval (equation (A14)).
 		return new Interval(tca - thc, tca + thc);
 
 	}
 
 	/**
 	 * Determines whether the specified point is near the
 	 * boundary of the sphere.
 	 * @param p The point to consider.
 	 * @return A value indicating whether the specified point
 	 * 		is near the boundary of the sphere.
 	 */
 	public boolean nearBoundary(Point3 p) {
 		return MathUtil.equal(center.squaredDistanceTo(p) / radius, radius);
 	}
 
 	/**
 	 * Determines whether the specified point is near the
 	 * boundary of the sphere, within a specified tolerance.
 	 * @param p The point to consider.
 	 * @param epsilon The tolerance.
 	 * @return A value indicating whether the specified point
 	 * 		is near the boundary of the sphere.
 	 */
 	public boolean nearBoundary(Point3 p, double epsilon) {
 		return MathUtil.equal(center.squaredDistanceTo(p) / radius, radius, epsilon);
 	}
 
 	/**
 	 * Computes the normal at the specified point, assuming p
 	 * is on the surface of the sphere.  This method is guaranteed
 	 * to return a unit vector.
 	 * @param p The point at which to compute the normal.
 	 * @return The normal at the specified point.
 	 */
 	public Vector3 normalAt(Point3 p) {
 		return center.vectorTo(p).unit();
 	}
 
 	/**
 	 * Computes the smallest <code>Sphere</code> containing each of the given
 	 * points.
 	 * @param points The collection of points for which to compute the smallest
 	 * 		<code>Sphere</code> containing those points.
 	 * @return The smallest <code>Sphere</code> containing the given points.
 	 */
 	public static Sphere smallestContaining(Iterable<Point3> points) {
 
 		Point3 center = Point3.centroid(points);
 		double radius = 0.0;
 
 		for (Point3 p : points) {
 			double distance = center.distanceTo(p);
 			if (distance > radius) {
 				radius = distance;
 			}
 		}
 
 		return new Sphere(center, radius);
 
 	}
 
 	/**
 	 * Gets the smallest <code>Box3</code> that contains this
 	 * <code>Sphere</code>.
 	 * @return The smallest <code>Box3</code> that contains this
 	 * 		<code>Sphere</code>.
 	 */
 	public Box3 boundingBox() {
 		return new Box3(
 				center.x() - radius,
 				center.y() - radius,
 				center.z() - radius,
 				center.x() + radius,
 				center.y() + radius,
 				center.z() + radius
 		);
 	}
 	
 	/**
 	 * Gets the <code>Matrix4</code> representation of this <code>Sphere</code>.
 	 * The <code>Matrix4</code> returned, <code><b>A</b></code>, will satisfy:
 	 * 
 	 * <ul>
 	 * 		<li>
 	 * 			<code><b>x<sup>t</sup>Ax</b> &lt; 0</code> whenever
 	 *			<code><b>x</b> = (x y z 1)<sup>t</sup></code> lies inside this
 	 *			<code>Sphere</code>,
 	 *		<li>
 	 * 			<code><b>x<sup>t</sup>Ax</b> &gt; 0</code> whenever
 	 *			<code><b>x</b></code> lies outside this <code>Sphere</code>,
 	 *			and
 	 *		<li>
 	 * 			<code><b>x<sup>t</sup>Ax</b> = 0</code> whenever
 	 *			<code><b>x</b></code> lies on the surface of this
 	 *			<code>Sphere</code>.
 	 * </ul>	
 	 * @return The <code>Matrix4</code> representation of this
 	 * 		<code>Sphere</code>.
 	 */
 	public Matrix4 getMatrixRepresentation() {
 		double Tx = -center.x();
 		double Ty = -center.y();
 		double Tz = -center.z();
 		Matrix4 T = new Matrix4(
 				1.0, 0.0, 0.0, Tx,
 				0.0, 1.0, 0.0, Ty,
 				0.0, 0.0, 1.0, Tz,
 				0.0, 0.0, 0.0, 1.0);
 		Matrix4 Tt = T.transposed();
 		
 		double r2i = 1.0 / (radius * radius);
 		Matrix4 S = new Matrix4(
 				r2i, 0.0, 0.0, 0.0,
 				0.0, r2i, 0.0, 0.0,
 				0.0, 0.0, r2i, 0.0,
 				0.0, 0.0, 0.0, -1.0);
 		
 		return Tt.times(S).times(T);
 	}
 
 	/**
 	 * Default constructor.
 	 */
 	private Sphere() {
 		center = Point3.ORIGIN;
 		radius = Double.NaN;
 	}
 
 	/**
 	 * Gets an instance of a sphere.
 	 * @param center The center of the sphere.
 	 * @param radius The radius of the sphere.
 	 * @return A new sphere if 0 <= radius < infinity,
 	 *         Sphere.UNIVERSE if radius == infinity,
 	 *         Sphere.EMPTY if radius < 0.
 	 */
 	private static final Sphere getInstance(Point3 center, double radius) {
 		if (radius < 0.0) {
 			return Sphere.EMPTY;
 		} else if (Double.isInfinite(radius)) {
 			return Sphere.UNIVERSE;
 		} else {
 			return new Sphere(center, radius);
 		}
 	}
 
 	/**
 	 * A sphere containing all points.
 	 */
 	public static final Sphere UNIVERSE = new Sphere(Point3.ORIGIN, Double.POSITIVE_INFINITY);
 
 	/**
 	 * The unit sphere (the sphere centered at the origin with a radius of 1.0).
 	 */
 	public static final Sphere UNIT = new Sphere(Point3.ORIGIN, 1.0);
 
 	/**
 	 * An empty sphere.
 	 */
 	public static final Sphere EMPTY = new Sphere();
 
 	/** The center of the sphere. */
 	private final Point3 center;
 
 	/** The radius of the sphere. */
 	private final double radius;
 
 	/**
 	 * Serialization version ID.
 	 */
 	private static final long serialVersionUID = -7300424778393465269L;
 
 }
