 /**
  * 
  */
 package com.bluespot.geom.vectors;
 
 import java.awt.Dimension;
 import java.awt.Point;
 import java.awt.geom.Dimension2D;
 import java.awt.geom.Point2D;
 
 import com.bluespot.geom.Axis;
 
 /**
  * A {@link Vector3} in {@code double} precision. Be aware that while this class
  * implements {@link #equals(Object)} appropriately, it may yield unexpected
  * results due to the inherent imprecision of floating-point values.
  * 
  * @author Aaron Faanes
  * 
  * @see Vector3f
  * @see Vector3i
  */
 public class Vector3d extends AbstractVector3<Vector3d> {
 
 	public static Vector3d mutable() {
 		return mutable(0);
 	}
 
 	public static Vector3d frozen() {
 		return frozen(0);
 	}
 
 	/**
 	 * Create a mutable {@link Vector3d} using the specified value for all axes.
 	 * 
 	 * @param v
 	 *            the value used for all axes
 	 * @return a mutable {@code Vector3d}
 	 * @throw {@link IllegalArgumentException} if {@code v} is {@code NaN}
 	 */
 	public static Vector3d mutable(double v) {
 		return Vector3d.mutable(v, v, v);
 	}
 
 	/**
 	 * Create a frozen {@link Vector3d} using the specified value for all axes.
 	 * 
 	 * @param v
 	 *            the value used for all axes
 	 * @return a frozen {@code Vector3d}
 	 * @throw {@link IllegalArgumentException} if {@code v} is {@code NaN}
 	 */
 	public static Vector3d frozen(double v) {
 		return Vector3d.mutable(v, v, v);
 	}
 
 	/**
 	 * Create a mutable {@link Vector3d} using the specified values.
 	 * 
 	 * @param x
 	 *            the x component
 	 * @param y
 	 *            the y component
 	 * @param z
 	 *            the z component
 	 * @return a new mutable {@code Vector3d}
 	 * @throw {@link IllegalArgumentException} if any component is {@code NaN}
 	 */
 	public static Vector3d mutable(double x, final double y, final double z) {
 		return new Vector3d(true, x, y, z);
 	}
 
 	public static Vector3d mutable(double x, double y) {
 		return mutable(x, y, 0);
 	}
 
 	/**
 	 * Create a frozen {@link Vector3d} using the specified values.
 	 * 
 	 * @param x
 	 *            the x component
 	 * @param y
 	 *            the y component
 	 * @param z
 	 *            the z component
 	 * @return a frozen {@code Vector3d}
 	 * @throw {@link IllegalArgumentException} if any component is {@code NaN}
 	 */
 	public static Vector3d frozen(double x, final double y, final double z) {
 		return new Vector3d(false, x, y, z);
 	}
 
 	public static Vector3d frozen(double x, double y) {
 		return frozen(x, y, 0);
 	}
 
 	/**
 	 * Create a mutable {@link Vector3d} from the specified vector.
 	 * 
 	 * @param vector
 	 *            the vector that is copied
 	 * @return a mutable {@code Vector3d}
 	 * @throw {@link NullPointerException} if {@code vector} is null
 	 */
 	public static Vector3d mutable(Vector3i vector) {
 		if (vector == null) {
 			throw new NullPointerException("vector must not be null");
 		}
 		return new Vector3d(true, vector.x(), vector.y(), vector.z());
 	}
 
 	/**
 	 * Create a frozen {@link Vector3d} from the specified vector.
 	 * 
 	 * @param vector
 	 *            the vector that is copied
 	 * @return a frozen {@code Vector3d}
 	 * @throw {@link NullPointerException} if {@code vector} is null
 	 */
 	public static Vector3d frozen(Vector3i vector) {
 		if (vector == null) {
 			throw new NullPointerException("vector must not be null");
 		}
 		return new Vector3d(false, vector.x(), vector.y(), vector.z());
 	}
 
 	/**
 	 * Create a mutable {@link Vector3d} from the specified vector.
 	 * 
 	 * @param vector
 	 *            the vector that is copied
 	 * @return a mutable {@code Vector3d}
 	 * @throw {@link NullPointerException} if {@code vector} is null
 	 */
 	public static Vector3d mutable(Vector3f vector) {
 		if (vector == null) {
 			throw new NullPointerException("vector must not be null");
 		}
 		return new Vector3d(true, vector.x(), vector.y(), vector.z());
 	}
 
 	/**
 	 * Create a frozen {@link Vector3d} from the specified vector.
 	 * 
 	 * @param vector
 	 *            the vector that is copied
 	 * @return a frozen {@code Vector3d}
 	 * @throw {@link NullPointerException} if {@code vector} is null
 	 */
 	public static Vector3d frozen(Vector3f vector) {
 		if (vector == null) {
 			throw new NullPointerException("vector must not be null");
 		}
 		return new Vector3d(false, vector.x(), vector.y(), vector.z());
 	}
 
 	public static Vector3d mutable(Point point) {
 		return Vector3d.mutable(point.x, point.y, 0);
 	}
 
 	public static Vector3d frozen(Point point) {
 		return Vector3d.frozen(point.x, point.y, 0);
 	}
 
 	public static Vector3d mutable(Dimension dimension) {
 		return Vector3d.mutable(dimension.width, dimension.height, 0);
 	}
 
 	public static Vector3d frozen(Dimension dimension) {
 		return Vector3d.frozen(dimension.width, dimension.height, 0);
 	}
 
 	public static Vector3d mutable(Dimension2D dimension) {
 		return Vector3d.mutable(dimension.getWidth(), dimension.getHeight(), 0);
 	}
 
 	public static Vector3d frozen(Dimension2D dimension) {
 		return Vector3d.frozen(dimension.getWidth(), dimension.getHeight(), 0);
 	}
 
 	public static Vector3d mutable(Point2D.Double point) {
 		return Vector3d.mutable(point.x, point.y, 0);
 	}
 
 	public static Vector3d frozen(Point2D.Double point) {
 		return Vector3d.frozen(point.x, point.y, 0);
 	}
 
 	/**
 	 * Interpolates between this vector and the destination. Offsets that are
 	 * not between zero and one are handled specially:
 	 * <ul>
 	 * <li>If {@code offset <= 0}, a copy of {@code src} is returned
 	 * <li>If {@code offset >= 1}, a copy of {@code dest} is returned
 	 * </ul>
 	 * This special behavior allows clients to reliably detect when
 	 * interpolation is complete.
 	 * 
 	 * @param src
 	 *            the starting vector
 	 * @param dest
 	 *            the ending vector
 	 * @param offset
 	 *            the percentage of distance between the specified points
 	 * @return a mutable {@link Vector3d} that lies between src and dest
 	 * @throw {@link NullPointerException} if either vector is null
 	 * @throw {@link IllegalArgumentException} if {@code offset} is NaN
 	 */
 	public static Vector3d interpolated(Vector3d src, Vector3d dest, float offset) {
 		if (src == null) {
 			throw new NullPointerException("src must not be null");
 		}
 		if (dest == null) {
 			throw new NullPointerException("dest must not be null");
 		}
 		if (Float.isNaN(offset)) {
 			throw new IllegalArgumentException("offset must not be NaN");
 		}
 		if (offset <= 0f) {
 			return src.toMutable();
 		}
 		if (offset >= 1f) {
 			return dest.toMutable();
 		}
 		return mutable(src.x + (dest.x - src.x) * offset,
 				src.y + (dest.y - src.y) * offset,
 				src.z + (dest.z - src.z) * offset);
 	}
 
 	private static final Vector3d ORIGIN = Vector3d.frozen(0);
 
 	/**
 	 * Returns a frozen vector at the origin.
 	 * 
 	 * @return a frozen vector with components {@code (0, 1, 0)}
 	 */
 	public static Vector3d origin() {
 		return ORIGIN;
 	}
 
 	/**
 	 * Return a frozen vector with values of 1 at the specified axes. This is
 	 * normally used to create unit vectors, but {@code axis} values of multiple
 	 * axes are allowed.
 	 * 
 	 * @param axis
 	 *            the axes with values of 1
 	 * @return a frozen unit vector
 	 */
 	public static Vector3d unit(Axis axis) {
 		return origin().copy().set(axis, 1).toFrozen();
 	}
 
 	private static final Vector3d UP = Vector3d.frozen(0, 1, 0);
 
 	/**
 	 * Returns a frozen vector that points up the y axis.
 	 * 
 	 * @return a frozen vector with components {@code (0, 1, 0)}
 	 */
 	public static Vector3d up() {
 		return UP;
 	}
 
 	private static final Vector3d FORWARD = Vector3d.frozen(0, 0, -1);
 
 	/**
 	 * Returns a frozen vector that points down the z axis.
 	 * 
 	 * @return a frozen vector with components {@code (0, 0, -1)}
 	 */
 	public static Vector3d forward() {
 		return FORWARD;
 	}
 
 	private static final Vector3d LEFT = Vector3d.frozen(-1, 0, 0);
 
 	/**
 	 * Returns a frozen vector that points down the negative x axis.
 	 * 
 	 * @return a frozen vector with components {@code (-1, 0, 0)}
 	 */
 	public static final Vector3d left() {
 		return LEFT;
 	}
 
 	private static final Vector3d RIGHT = Vector3d.frozen(1, 0, 0);
 
 	/**
 	 * Returns a frozen vector that points down the positive x axis.
 	 * 
 	 * @return a frozen vector with components {@code (1, 0, 0)}
 	 */
 	public static Vector3d right() {
 		return RIGHT;
 	}
 
	private static final Vector3d DOWN = UP.copy().negate().toFrozen();
 
 	/**
 	 * Returns a frozen vector that points down the negative Y axis.
 	 * 
 	 * @return a frozen vector with components {@code (0, -1, 0)}
 	 */
 	public static final Vector3d down() {
 		return DOWN;
 	}
 
 	/**
 	 * The internal z component
 	 */
 	private double z;
 
 	/**
 	 * The internal y component
 	 */
 	private double y;
 
 	/**
 	 * The internal x component
 	 */
 	private double x;
 
 	/**
 	 * Constructs a vector using the specified coordinates.
 	 * 
 	 * @param mutable
 	 *            whether this vector can be directly modified
 	 * @param x
 	 *            the x-coordinate of this vector
 	 * @param y
 	 *            the y-coordinate of this vector
 	 * @param z
 	 *            the z-coordinate of this vector
 	 * @throws IllegalArgumentException
 	 *             if any component is {@code NaN}
 	 */
 	private Vector3d(final boolean mutable, final double x, final double y, final double z) {
 		super(mutable);
 		if (java.lang.Double.isNaN(x)) {
 			throw new IllegalArgumentException("x is NaN");
 		}
 		if (java.lang.Double.isNaN(y)) {
 			throw new IllegalArgumentException("y is NaN");
 		}
 		if (java.lang.Double.isNaN(z)) {
 			throw new IllegalArgumentException("z is NaN");
 		}
 		this.x = x;
 		this.y = y;
 		this.z = z;
 	}
 
 	/**
 	 * Returns the x-coordinate of this vector.
 	 * 
 	 * @return the x-coordinate of this vector
 	 */
 	public double x() {
 		return this.x;
 	}
 
 	/**
 	 * Sets the x component to the specified value.
 	 * 
 	 * @param value
 	 *            the new x value
 	 * @return the old x value
 	 * @throw UnsupportedOperationException if this vector is not mutable
 	 * @throw IllegalArgumentException if {@code value} is NaN
 	 */
 	public double setX(double value) {
 		if (!this.isMutable()) {
 			throw new UnsupportedOperationException("vector is not mutable");
 		}
 		if (Double.isNaN(value)) {
 			throw new IllegalArgumentException("value must not be NaN");
 		}
 		double old = this.x;
 		this.x = value;
 		return old;
 	}
 
 	/**
 	 * Add the specified x value to this vector.
 	 * 
 	 * @param offset
 	 *            the value to add
 	 * @return the old x value
 	 * @throw UnsupportedOperationException if this vector is not mutable
 	 * @throw IllegalArgumentException if {@code offset} is NaN
 	 */
 	public double addX(double offset) {
 		return this.setX(this.x() + offset);
 	}
 
 	/**
 	 * Subtract the specified value from this vector's X axis.
 	 * 
 	 * @param offset
 	 *            the value to subtract
 	 * @return the old value at the X axis
 	 * @see #subtractedX(int)
 	 * @throw UnsupportedOperationException if this vector is not mutable
 	 * @throw IllegalArgumentException if {@code offset} is NaN
 	 */
 	public double subtractX(double offset) {
 		return this.setX(this.x() - offset);
 	}
 
 	/**
 	 * Multiply the specified x value of this vector.
 	 * 
 	 * @param factor
 	 *            the factor of multiplication
 	 * @return the old x value
 	 * @throw IllegalArgumentException if {@code offset} is NaN
 	 * @throw UnsupportedOperationException if this vector is not mutable
 	 */
 	public double multiplyX(double factor) {
 		return this.setX(this.x() * factor);
 	}
 
 	public double divideX(double denominator) {
 		if (Double.isNaN(denominator)) {
 			throw new IllegalArgumentException("denominator must not be NaN");
 		}
 		return this.setX(this.x() / denominator);
 	}
 
 	public double moduloX(double denominator) {
 		if (Double.isNaN(denominator)) {
 			throw new IllegalArgumentException("denominator must not be NaN");
 		}
 		return this.setX(this.x() % denominator);
 	}
 
 	/**
 	 * Returns the y-coordinate of this vector.
 	 * 
 	 * @return the y-coordinate of this vector
 	 */
 	public double y() {
 		return this.y;
 	}
 
 	/**
 	 * Sets the y position to the specified value.
 	 * 
 	 * @param value
 	 *            the new y value
 	 * @return the old y value
 	 * @throw UnsupportedOperationException if the vector is not mutable
 	 * @throw IllegalArgumentException if {@code value} is NaN
 	 */
 	public double setY(double value) {
 		if (!this.isMutable()) {
 			throw new UnsupportedOperationException("vector is not mutable");
 		}
 		if (Double.isNaN(value)) {
 			throw new IllegalArgumentException("value must not be NaN");
 		}
 		double old = this.y;
 		this.y = value;
 		return old;
 	}
 
 	/**
 	 * Add the specified y value to this vector.
 	 * 
 	 * @param offset
 	 *            the value to add
 	 * @return the old y value
 	 */
 	public double addY(double offset) {
 		return this.setY(this.y() + offset);
 	}
 
 	/**
 	 * Subtract the specified value from this vector's Y axis.
 	 * 
 	 * @param offset
 	 *            the value to subtract
 	 * @return the old value at the Y axis
 	 * @see #subtractedY(int)
 	 * @throw UnsupportedOperationException if this vector is not mutable
 	 * @throw IllegalArgumentException if {@code offset} is NaN
 	 */
 	public double subtractY(double offset) {
 		return this.setY(this.y() - offset);
 	}
 
 	/**
 	 * Multiply the specified y value of this vector.
 	 * 
 	 * @param factor
 	 *            the factor of multiplication
 	 * @return the old y value
 	 */
 	public double multiplyY(double factor) {
 		return this.setY(this.y() * factor);
 	}
 
 	public double divideY(double denominator) {
 		if (Double.isNaN(denominator)) {
 			throw new IllegalArgumentException("denominator must not be NaN");
 		}
 		return this.setY(this.y() / denominator);
 	}
 
 	public double moduloY(double denominator) {
 		if (Double.isNaN(denominator)) {
 			throw new IllegalArgumentException("denominator must not be NaN");
 		}
 		return this.setY(this.y() % denominator);
 	}
 
 	/**
 	 * Returns the z-coordinate of this vector.
 	 * 
 	 * @return the z-coordinate of this vector
 	 */
 	public double z() {
 		return this.z;
 	}
 
 	/**
 	 * Sets the z position to the specified value.
 	 * 
 	 * @param value
 	 *            the new z value
 	 * @return the old z value
 	 * @throw UnsupportedOperationException if the vector is not mutable
 	 * @throw IllegalArgumentException if {@code value} is NaN
 	 */
 	public double setZ(double value) {
 		if (!this.isMutable()) {
 			throw new UnsupportedOperationException("vector is not mutable");
 		}
 		if (Double.isNaN(value)) {
 			throw new IllegalArgumentException("value must not be NaN");
 		}
 		double old = this.z;
 		this.z = value;
 		return old;
 	}
 
 	/**
 	 * Add the specified z value to this vector.
 	 * 
 	 * @param offset
 	 *            the value to add
 	 * @return the old z value
 	 */
 	public double addZ(double offset) {
 		return this.setZ(this.z() + offset);
 	}
 
 	/**
 	 * Subtract the specified value from this vector's Z axis.
 	 * 
 	 * @param offset
 	 *            the value to subtract
 	 * @return the old value at the Z axis
 	 * @see #subtractedZ(int)
 	 * @throw UnsupportedOperationException if this vector is not mutable
 	 * @throw IllegalArgumentException if {@code offset} is NaN
 	 */
 	public double subtractZ(double offset) {
 		return this.setZ(this.z() - offset);
 	}
 
 	/**
 	 * Multiply the specified z value of this vector.
 	 * 
 	 * @param factor
 	 *            the factor of multiplication
 	 * @return the old z value
 	 */
 	public double multiplyZ(double factor) {
 		return this.setZ(this.z() * factor);
 	}
 
 	public double divideZ(double denominator) {
 		if (Double.isNaN(denominator)) {
 			throw new IllegalArgumentException("denominator must not be NaN");
 		}
 		return this.setZ(this.z() / denominator);
 	}
 
 	public double moduloZ(double denominator) {
 		if (Double.isNaN(denominator)) {
 			throw new IllegalArgumentException("denominator must not be NaN");
 		}
 		return this.setZ(this.z() % denominator);
 	}
 
 	@Override
 	public Vector3d set(Vector3d vector) {
 		if (vector == null) {
 			throw new NullPointerException("vector must not be null");
 		}
 		this.setX(vector.x());
 		this.setY(vector.y());
 		this.setZ(vector.z());
 		return this;
 	}
 
 	/**
 	 * Sets all of this vector's values to the specified value.
 	 * 
 	 * @param value
 	 *            the value that will be used
 	 * @return {@code this}
 	 * @throw IllegalArgumentException if {@code value} is NaN
 	 */
 	public Vector3d set(double value) {
 		this.setX(value);
 		this.setY(value);
 		this.setZ(value);
 		return this;
 	}
 
 	/**
 	 * Sets the x and y components to the specified values.
 	 * 
 	 * @param x
 	 *            the new x value
 	 * @param y
 	 *            the new y value
 	 * @return {@code this}
 	 */
 	public Vector3d set(double x, double y) {
 		this.setX(x);
 		this.setY(y);
 		return this;
 	}
 
 	/**
 	 * Sets all of this vector's values to the specified values.
 	 * 
 	 * @param x
 	 *            the new x value
 	 * @param y
 	 *            the new y value
 	 * @param z
 	 *            the new z value
 	 * @return {@code this}
 	 * @throw IllegalArgumentException if any value is NaN. All values are
 	 *        checked before any are used.
 	 */
 	public Vector3d set(double x, double y, double z) {
 		if (Double.isNaN(x)) {
 			throw new IllegalArgumentException("x must not be NaN");
 		}
 		if (Double.isNaN(y)) {
 			throw new IllegalArgumentException("y must not be NaN");
 		}
 		if (Double.isNaN(z)) {
 			throw new IllegalArgumentException("z must not be NaN");
 		}
 		this.setX(x);
 		this.setY(y);
 		this.setZ(z);
 		return this;
 	}
 
 	@Override
 	public Vector3d set(Axis axis, Vector3d vector) {
 		if (axis == null) {
 			throw new NullPointerException("Axis must not be null");
 		}
 		if (vector == null) {
 			throw new NullPointerException("vector must not be null");
 		}
 		switch (axis) {
 		case X:
 			this.setX(vector.x());
 			return this;
 		case Y:
 			this.setY(vector.y());
 			return this;
 		case Z:
 			this.setZ(vector.z());
 			return this;
 		case XY:
 			this.setX(vector.x());
 			this.setY(vector.y());
 			return this;
 		case XZ:
 			this.setX(vector.x());
 			this.setZ(vector.z());
 			return this;
 		case YZ:
 			this.setY(vector.y());
 			this.setZ(vector.z());
 			return this;
 		}
 		throw new IllegalArgumentException("Axis is invalid");
 	}
 
 	/**
 	 * Sets values at the specified axes to the specified value.
 	 * 
 	 * @param axis
 	 *            the axes that will be modified
 	 * @param value
 	 *            the added value
 	 * @return {@code this}
 	 */
 	public Vector3d set(Axis axis, double value) {
 		if (!this.isMutable()) {
 			throw new UnsupportedOperationException("vector is not mutable");
 		}
 		if (axis == null) {
 			throw new NullPointerException("Axis must not be null");
 		}
 		switch (axis) {
 		case X:
 			this.setX(value);
 			return this;
 		case Y:
 			this.setY(value);
 			return this;
 		case Z:
 			this.setZ(value);
 			return this;
 		case XY:
 			this.setX(value);
 			this.setY(value);
 			return this;
 		case XZ:
 			this.setX(value);
 			this.setZ(value);
 			return this;
 		case YZ:
 			this.setY(value);
 			this.setZ(value);
 			return this;
 		}
 		throw new IllegalArgumentException("Axis is invalid");
 	}
 
 	@Override
 	public Vector3d add(Vector3d vector) {
 		this.addX(vector.x());
 		this.addY(vector.y());
 		this.addZ(vector.z());
 		return this;
 	}
 
 	/**
 	 * Adds the specified value to all of this vector's values.
 	 * 
 	 * @param value
 	 *            the value that will be used
 	 * @return {@code this}
 	 */
 	public Vector3d add(double value) {
 		this.addX(value);
 		this.addY(value);
 		this.addZ(value);
 		return this;
 	}
 
 	@Override
 	public Vector3d add(Axis axis, Vector3d vector) {
 		if (axis == null) {
 			throw new NullPointerException("Axis must not be null");
 		}
 		if (vector == null) {
 			throw new NullPointerException("vector must not be null");
 		}
 		switch (axis) {
 		case X:
 			this.addX(vector.x());
 			return this;
 		case Y:
 			this.addY(vector.y());
 			return this;
 		case Z:
 			this.addZ(vector.z());
 			return this;
 		case XY:
 			this.addX(vector.x());
 			this.addY(vector.y());
 			return this;
 		case XZ:
 			this.addX(vector.x());
 			this.addZ(vector.z());
 			return this;
 		case YZ:
 			this.addY(vector.y());
 			this.addZ(vector.z());
 			return this;
 		}
 		throw new IllegalArgumentException("Axis is invalid");
 	}
 
 	/**
 	 * Adds the specified value to the specified axes.
 	 * 
 	 * @param axis
 	 *            the axes that will be modified
 	 * @param value
 	 *            the added value
 	 * @return {@code this}
 	 */
 	public Vector3d add(Axis axis, double value) {
 		if (!this.isMutable()) {
 			throw new UnsupportedOperationException("vector is not mutable");
 		}
 		if (axis == null) {
 			throw new NullPointerException("Axis must not be null");
 		}
 		switch (axis) {
 		case X:
 			this.addX(value);
 			return this;
 		case Y:
 			this.addY(value);
 			return this;
 		case Z:
 			this.addZ(value);
 			return this;
 		case XY:
 			this.addX(value);
 			this.addY(value);
 			return this;
 		case XZ:
 			this.addX(value);
 			this.addZ(value);
 			return this;
 		case YZ:
 			this.addY(value);
 			this.addZ(value);
 			return this;
 		}
 		throw new IllegalArgumentException("Axis is invalid");
 	}
 
 	@Override
 	public Vector3d subtract(Vector3d vector) {
 		this.subtractX(vector.x());
 		this.subtractY(vector.y());
 		this.subtractZ(vector.z());
 		return this;
 	}
 
 	/**
 	 * Subtracts the specified value from each of this vector's values.
 	 * 
 	 * @param value
 	 *            the value that will be used
 	 * @return {@code this}
 	 */
 	public Vector3d subtract(double value) {
 		this.subtractX(value);
 		this.subtractY(value);
 		this.subtractZ(value);
 		return this;
 	}
 
 	@Override
 	public Vector3d subtract(Axis axis, Vector3d vector) {
 		if (axis == null) {
 			throw new NullPointerException("Axis must not be null");
 		}
 		if (vector == null) {
 			throw new NullPointerException("vector must not be null");
 		}
 		switch (axis) {
 		case X:
 			this.subtractX(vector.x());
 			return this;
 		case Y:
 			this.subtractY(vector.y());
 			return this;
 		case Z:
 			this.subtractZ(vector.z());
 			return this;
 		case XY:
 			this.subtractX(vector.x());
 			this.subtractY(vector.y());
 			return this;
 		case XZ:
 			this.subtractX(vector.x());
 			this.subtractZ(vector.z());
 			return this;
 		case YZ:
 			this.subtractY(vector.y());
 			this.subtractZ(vector.z());
 			return this;
 		}
 		throw new IllegalArgumentException("Axis is invalid");
 	}
 
 	/**
 	 * Subtracts the specified value from the specified axes.
 	 * 
 	 * @param axis
 	 *            the axes that will be modified
 	 * @param value
 	 *            the subtracted value
 	 * @return {@code this}
 	 */
 	public Vector3d subtract(Axis axis, double value) {
 		if (!this.isMutable()) {
 			throw new UnsupportedOperationException("vector is not mutable");
 		}
 		if (axis == null) {
 			throw new NullPointerException("Axis must not be null");
 		}
 		switch (axis) {
 		case X:
 			this.subtractX(value);
 			return this;
 		case Y:
 			this.subtractY(value);
 			return this;
 		case Z:
 			this.subtractZ(value);
 			return this;
 		case XY:
 			this.subtractX(value);
 			this.subtractY(value);
 			return this;
 		case XZ:
 			this.subtractX(value);
 			this.subtractZ(value);
 			return this;
 		case YZ:
 			this.subtractY(value);
 			this.subtractZ(value);
 			return this;
 		}
 		throw new IllegalArgumentException("Axis is invalid");
 	}
 
 	@Override
 	public Vector3d multiply(Vector3d vector) {
 		this.multiplyX(vector.x());
 		this.multiplyY(vector.y());
 		this.multiplyZ(vector.z());
 		return this;
 	}
 
 	@Override
 	public Vector3d multiply(double factor) {
 		this.multiplyX(factor);
 		this.multiplyY(factor);
 		this.multiplyZ(factor);
 		return this;
 	}
 
 	@Override
 	public Vector3d multiply(double x, double y, double z) {
 		this.multiplyX(x);
 		this.multiplyY(y);
 		this.multiplyZ(z);
 		return this;
 	}
 
 	@Override
 	public Vector3d multiply(Axis axis, Vector3d vector) {
 		if (axis == null) {
 			throw new NullPointerException("Axis must not be null");
 		}
 		if (vector == null) {
 			throw new NullPointerException("vector must not be null");
 		}
 		switch (axis) {
 		case X:
 			this.multiplyX(vector.x());
 			return this;
 		case Y:
 			this.multiplyY(vector.y());
 			return this;
 		case Z:
 			this.multiplyZ(vector.z());
 			return this;
 		case XY:
 			this.multiplyX(vector.x());
 			this.multiplyY(vector.y());
 			return this;
 		case XZ:
 			this.multiplyX(vector.x());
 			this.multiplyZ(vector.z());
 			return this;
 		case YZ:
 			this.multiplyY(vector.y());
 			this.multiplyZ(vector.z());
 			return this;
 		}
 		throw new IllegalArgumentException("Axis is invalid");
 	}
 
 	@Override
 	public Vector3d multiply(Axis axis, double factor) {
 		if (!this.isMutable()) {
 			throw new UnsupportedOperationException("vector is not mutable");
 		}
 		if (axis == null) {
 			throw new NullPointerException("Axis must not be null");
 		}
 		switch (axis) {
 		case X:
 			this.multiplyX(factor);
 			return this;
 		case Y:
 			this.multiplyY(factor);
 			return this;
 		case Z:
 			this.multiplyZ(factor);
 			return this;
 		case XY:
 			this.multiplyX(factor);
 			this.multiplyY(factor);
 			return this;
 		case XZ:
 			this.multiplyX(factor);
 			this.multiplyZ(factor);
 			return this;
 		case YZ:
 			this.multiplyY(factor);
 			this.multiplyZ(factor);
 			return this;
 		}
 		throw new IllegalArgumentException("Axis is invalid");
 	}
 
 	@Override
 	public Vector3d divide(Vector3d vector) {
 		return this.divide(vector.x(), vector.y(), vector.z());
 	}
 
 	@Override
 	public Vector3d divide(double x, double y, double z) {
 		this.divideX(x);
 		this.divideY(y);
 		this.divideZ(z);
 		return this;
 	}
 
 	@Override
 	public double length() {
 		return Math.sqrt(Math.pow(this.x(), 2) + Math.pow(this.y(), 2) + Math.pow(this.z(), 2));
 	}
 
 	public double area() {
 		return this.x * this.y;
 	}
 
 	public double volume() {
 		return this.x * this.y * this.z;
 	}
 
 	@Override
 	public Vector3d normalize() {
 		float len = (float) this.length();
 		return this.set(this.x() / len,
 				this.y() / len,
 				this.z() / len);
 	}
 
 	@Override
 	public Vector3d reciprocal() {
 		this.setX(1 / this.x());
 		this.setY(1 / this.y());
 		this.setZ(1 / this.z());
 		return this;
 	}
 
 	@Override
 	public Vector3d reciprocal(Axis axis) {
 		if (!this.isMutable()) {
 			throw new UnsupportedOperationException("vector is not mutable");
 		}
 		if (axis == null) {
 			throw new NullPointerException("Axis must not be null");
 		}
 		switch (axis) {
 		case X:
 			this.setX(1 / this.x());
 			return this;
 		case Y:
 			this.setY(1 / this.y());
 			return this;
 		case Z:
 			this.setZ(1 / this.z());
 			return this;
 		case XY:
 			this.setX(1 / this.x());
 			this.setY(1 / this.y());
 			return this;
 		case XZ:
 			this.setX(1 / this.x());
 			this.setZ(1 / this.z());
 			return this;
 		case YZ:
 			this.setY(1 / this.y());
 			this.setZ(1 / this.z());
 			return this;
 		}
 		throw new IllegalArgumentException("Axis is invalid");
 	}
 
 	@Override
 	public Vector3d interpolate(Vector3d dest, float offset) {
 		if (dest == null) {
 			throw new NullPointerException("dest must not be null");
 		}
 		if (offset >= 1f) {
 			this.set(dest);
 		} else if (offset >= 0f) {
 			this.x += (dest.x - this.x) * offset;
 			this.y += (dest.y - this.y) * offset;
 			this.z += (dest.z - this.z) * offset;
 		}
 		return this;
 	}
 
 	@Override
 	public Vector3d cross(Vector3d other) {
 		return this.set(this.y() * other.z() - other.y() * this.z(),
 				-this.x() * other.z() + other.x() * this.z(),
 				this.x() * other.y() - other.x() * this.y());
 	}
 
 	@Override
 	public Vector3d clear() {
 		return this.set(0d);
 	}
 
 	@Override
 	public Vector3d clear(Axis axis) {
 		return this.set(axis, 0d);
 	}
 
 	@Override
 	public Dimension toDimension() {
 		return new Dimension((int) x, (int) y);
 	}
 
 	@Override
 	public Point toPoint() {
 		return new Point((int) x, (int) y);
 	}
 
 	public Point2D.Double toPoint2D() {
 		return new Point2D.Double(x, y);
 	}
 
 	@Override
 	public Vector3d toMutable() {
 		return Vector3d.mutable(x, y, z);
 	}
 
 	@Override
 	public Vector3d toFrozen() {
 		if (!this.isMutable()) {
 			return this;
 		}
 		return Vector3d.frozen(x, y, z);
 	}
 
 	@Override
 	public Vector3d getThis() {
 		return this;
 	}
 
 	@Override
 	public boolean at(Vector3d vector) {
 		if (vector == null) {
 			return false;
 		}
 		return this.x() == vector.x() &&
 				this.y() == vector.y() &&
 				this.z() == vector.z();
 	}
 
 	@Override
 	public boolean equals(final Object obj) {
 		if (this == obj) {
 			return true;
 		}
 		if (!(obj instanceof Vector3d)) {
 			return false;
 		}
 		final Vector3d other = (Vector3d) obj;
 		if (this.isMutable() != other.isMutable()) {
 			return false;
 		}
 		if (this.x() != other.x()) {
 			return false;
 		}
 		if (this.y() != other.y()) {
 			return false;
 		}
 		if (this.z() != other.z()) {
 			return false;
 		}
 		return true;
 	}
 
 	@Override
 	public int hashCode() {
 		int result = 13;
 		result = 31 * result + (this.isMutable() ? 1 : 0);
 		final long xLong = java.lang.Double.doubleToLongBits(this.x());
 		final long yLong = java.lang.Double.doubleToLongBits(this.y());
 		final long zLong = java.lang.Double.doubleToLongBits(this.z());
 		result = 31 * result + (int) (xLong ^ (xLong >>> 32));
 		result = 31 * result + (int) (yLong ^ (yLong >>> 32));
 		result = 31 * result + (int) (zLong ^ (zLong >>> 32));
 		return result;
 	}
 
 	@Override
 	public String toString() {
 		return String.format("Vector3d[%s (%f, %f, %f)]", this.isMutable() ? "mutable" : "frozen", this.x(), this.y(), this.z());
 	}
 
 }
