 /**
  * 
  */
 package com.bluespot.geom.vectors;
 
 import java.awt.Dimension;
 import java.awt.Point;
 import java.awt.geom.Point2D;
 
 import com.bluespot.geom.Axis;
 
 /**
  * A {@link Vector3} in {@code float} precision. Be aware that while this class
  * implements {@link #equals(Object)} appropriately, it may yield unexpected
  * results due to the inherent imprecision of floating-point values.
  * 
  * @author Aaron Faanes
  * 
  * @see Vector3d
  * @see Vector3i
  */
 public final class Vector3f extends AbstractVector3<Vector3f> {
 
 	public static Vector3f mutable() {
 		return mutable(0);
 	}
 
 	public static Vector3f frozen() {
 		return frozen(0);
 	}
 
 	/**
 	 * Create a mutable {@link Vector3f} using the specified value for all axes.
 	 * 
 	 * @param v
 	 *            the value used for all axes
 	 * @return a mutable {@code Vector3f}
 	 * @throw {@link IllegalArgumentException} if {@code v} is {@code NaN}
 	 */
 	public static Vector3f mutable(float v) {
 		return Vector3f.mutable(v, v, v);
 	}
 
 	/**
 	 * Create a frozen {@link Vector3f} using the specified value for all axes.
 	 * 
 	 * @param v
 	 *            the value used for all axes
 	 * @return a frozen {@code Vector3f}
 	 * @throw {@link IllegalArgumentException} if {@code v} is {@code NaN}
 	 */
 	public static Vector3f frozen(float v) {
 		return Vector3f.mutable(v, v, v);
 	}
 
 	public static Vector3f mutable(final float x, final float y, final float z) {
 		return new Vector3f(true, x, y, z);
 	}
 
 	public static Vector3f mutable(final float x, final float y) {
 		return mutable(x, y, 0);
 	}
 
 	public static Vector3f frozen(final float x, final float y, final float z) {
 		return new Vector3f(false, x, y, z);
 	}
 
 	public static Vector3f frozen(final float x, final float y) {
 		return frozen(x, y, 0);
 	}
 
 	public static Vector3f mutable(Vector3i vector) {
 		if (vector == null) {
 			throw new NullPointerException("vector must not be null");
 		}
 		return new Vector3f(true, vector.x(), vector.y(), vector.z());
 	}
 
 	public static Vector3f frozen(Vector3i vector) {
 		if (vector == null) {
 			throw new NullPointerException("vector must not be null");
 		}
 		return new Vector3f(false, vector.x(), vector.y(), vector.z());
 	}
 
 	public static Vector3f mutable(Vector3d vector) {
 		if (vector == null) {
 			throw new NullPointerException("vector must not be null");
 		}
 		return new Vector3f(true, (float) vector.x(), (float) vector.y(), (float) vector.z());
 	}
 
 	public static Vector3f frozen(Vector3d vector) {
 		if (vector == null) {
 			throw new NullPointerException("vector must not be null");
 		}
 		return new Vector3f(false, (float) vector.x(), (float) vector.y(), (float) vector.z());
 	}
 
 	public static Vector3f mutable(Point point) {
 		return Vector3f.mutable(point.x, point.y, 0);
 	}
 
 	public static Vector3f frozen(Point point) {
 		return Vector3f.frozen(point.x, point.y, 0);
 	}
 
 	public static Vector3f mutable(Dimension dimension) {
 		return Vector3f.mutable(dimension.width, dimension.height, 0);
 	}
 
 	public static Vector3f frozen(Dimension dimension) {
 		return Vector3f.frozen(dimension.width, dimension.height, 0);
 	}
 
 	public static Vector3d mutable(Point2D.Float point) {
 		return Vector3d.mutable(point.x, point.y, 0);
 	}
 
 	public static Vector3d frozen(Point2D.Float point) {
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
 	 * @return a mutable vector that lies between src and dest
 	 */
 	public static Vector3f interpolated(Vector3f src, Vector3f dest, final float offset) {
 		if (src == null) {
 			throw new NullPointerException("src must not be null");
 		}
 		if (dest == null) {
 			throw new NullPointerException("dest must not be null");
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
 
 	private static final Vector3f ORIGIN = Vector3f.frozen(0);
 
 	/**
 	 * Returns a frozen vector at the origin.
 	 * 
 	 * @return a frozen vector at the origin.
 	 */
 	public static Vector3f origin() {
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
 	public static Vector3f unit(Axis axis) {
 		return origin().copy().set(axis, 1).toFrozen();
 	}
 
 	private static final Vector3f UP = Vector3f.frozen(0, 1, 0);
 
 	/**
 	 * Returns a frozen vector that points up the y axis.
 	 * 
 	 * @return a frozen vector with components {@code (0, 1, 0)}
 	 */
 	public static Vector3f up() {
 		return UP;
 	}
 
 	private static final Vector3f FORWARD = Vector3f.frozen(0, 0, -1);
 
 	/**
 	 * Returns a frozen vector that points down the z axis.
 	 * 
 	 * @return a frozen vector with components {@code (0, 0, -1)}
 	 */
 	public static Vector3f forward() {
 		return FORWARD;
 	}
 
 	private static final Vector3f LEFT = Vector3f.frozen(-1, 0, 0);
 
 	/**
 	 * Returns a frozen vector that points down the negative x axis.
 	 * 
 	 * @return a frozen vector with components {@code (-1, 0, 0)}
 	 */
 	public static final Vector3f left() {
 		return LEFT;
 	}
 
 	private static final Vector3f RIGHT = Vector3f.frozen(1, 0, 0);
 
 	/**
 	 * Returns a frozen vector that points down the positive x axis.
 	 * 
 	 * @return a frozen vector with components {@code (1, 0, 0)}
 	 */
 	public static Vector3f right() {
 		return RIGHT;
 	}
 
	private static final Vector3f DOWN = UP.toMutable().negate().toFrozen();
 
 	/**
 	 * Returns a frozen vector that points down the negative Y axis.
 	 * 
 	 * @return a frozen vector with components {@code (0, -1, 0)}
 	 */
 	public static final Vector3f down() {
 		return DOWN;
 	}
 
 	private float z;
 	private float y;
 	private float x;
 
 	/**
 	 * Constructs a vector using the specified coordinates. There are no
 	 * restrictions on the values of these points except that none of them can
 	 * be {@code NaN}.
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
 	 *             if any coordinate is {@code NaN}
 	 */
 	private Vector3f(final boolean mutable, final float x, final float y, final float z) {
 		super(mutable);
 		if (java.lang.Float.isNaN(x)) {
 			throw new IllegalArgumentException("x is NaN");
 		}
 		if (java.lang.Float.isNaN(y)) {
 			throw new IllegalArgumentException("y is NaN");
 		}
 		if (java.lang.Float.isNaN(z)) {
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
 	public float x() {
 		return this.x;
 	}
 
 	/**
 	 * Sets the x position to the specified value.
 	 * 
 	 * @param value
 	 *            the new x value
 	 * @return the old x value
 	 */
 	public float setX(float value) {
 		if (!this.isMutable()) {
 			throw new UnsupportedOperationException("vector is not mutable");
 		}
 		if (Float.isNaN(value)) {
 			throw new IllegalArgumentException("value must not be NaN");
 		}
 		float old = this.x;
 		this.x = value;
 		return old;
 	}
 
 	/**
 	 * Add the specified x value to this vector.
 	 * 
 	 * @param offset
 	 *            the value to add
 	 * @return the old x value
 	 */
 	public float addX(float offset) {
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
 	public float subtractX(float offset) {
 		return this.setX(this.x() - offset);
 	}
 
 	/**
 	 * Multiply the specified x value of this vector.
 	 * 
 	 * @param factor
 	 *            the factor of multiplication
 	 * @return the old x value
 	 */
 	public float multiplyX(double factor) {
 		return this.setX((float) (this.x() * factor));
 	}
 
 	public float divideX(double denominator) {
 		if (Double.isNaN(denominator)) {
 			throw new IllegalArgumentException("denominator must not be NaN");
 		}
 		return this.setX((float) (this.x() / denominator));
 	}
 
 	public float moduloX(double denominator) {
 		if (Double.isNaN(denominator)) {
 			throw new IllegalArgumentException("denominator must not be NaN");
 		}
 		return this.setX((float) (this.x() % denominator));
 	}
 
 	/**
 	 * Returns the y-coordinate of this vector.
 	 * 
 	 * @return the y-coordinate of this vector
 	 */
 	public float y() {
 		return this.y;
 	}
 
 	/**
 	 * Sets the y position to the specified value.
 	 * 
 	 * @param value
 	 *            the new y value
 	 * @return the old y value
 	 */
 	public float setY(float value) {
 		if (!this.isMutable()) {
 			throw new UnsupportedOperationException("vector is not mutable");
 		}
 		if (Float.isNaN(value)) {
 			throw new IllegalArgumentException("value must not be NaN");
 		}
 		float old = this.y;
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
 	public float addY(float offset) {
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
 	 */
 	public float subtractY(float offset) {
 		return this.setY(this.y() - offset);
 	}
 
 	/**
 	 * Multiply the specified y value of this vector.
 	 * 
 	 * @param factor
 	 *            the factor of multiplication
 	 * @return the old y value
 	 */
 	public float multiplyY(double factor) {
 		return this.setY((float) (this.y() * factor));
 	}
 
 	public float divideY(double denominator) {
 		if (Double.isNaN(denominator)) {
 			throw new IllegalArgumentException("denominator must not be NaN");
 		}
 		return this.setY((float) (this.y() / denominator));
 	}
 
 	public float moduloY(double denominator) {
 		if (Double.isNaN(denominator)) {
 			throw new IllegalArgumentException("denominator must not be NaN");
 		}
 		return this.setY((float) (this.y() % denominator));
 	}
 
 	/**
 	 * Returns the z-coordinate of this vector.
 	 * 
 	 * @return the z-coordinate of this vector
 	 */
 	public float z() {
 		return this.z;
 	}
 
 	/**
 	 * Sets the z position to the specified value.
 	 * 
 	 * @param value
 	 *            the new z value
 	 * @return the old z value
 	 */
 	public float setZ(float value) {
 		if (!this.isMutable()) {
 			throw new UnsupportedOperationException("vector is not mutable");
 		}
 		if (Float.isNaN(value)) {
 			throw new IllegalArgumentException("value must not be NaN");
 		}
 		float old = this.z;
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
 	public float addZ(float offset) {
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
 	public float subtractZ(float offset) {
 		return this.setZ(this.z() - offset);
 	}
 
 	/**
 	 * Multiply the specified z value of this vector.
 	 * 
 	 * @param factor
 	 *            the factor of multiplication
 	 * @return the old z value
 	 */
 	public float multiplyZ(double factor) {
 		return this.setZ((float) (this.z() * factor));
 	}
 
 	public float divideZ(double denominator) {
 		if (Double.isNaN(denominator)) {
 			throw new IllegalArgumentException("denominator must not be NaN");
 		}
 		return this.setZ((float) (this.z() / denominator));
 	}
 
 	public float moduloZ(double denominator) {
 		if (Double.isNaN(denominator)) {
 			throw new IllegalArgumentException("denominator must not be NaN");
 		}
 		return this.setZ((float) (this.z() % denominator));
 	}
 
 	@Override
 	public Vector3f set(Vector3f vector) {
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
 	 */
 	public Vector3f set(float value) {
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
 	public Vector3f set(float x, float y) {
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
 	public Vector3f set(float x, float y, float z) {
 		if (Float.isNaN(x)) {
 			throw new IllegalArgumentException("x must not be NaN");
 		}
 		if (Float.isNaN(y)) {
 			throw new IllegalArgumentException("y must not be NaN");
 		}
 		if (Float.isNaN(z)) {
 			throw new IllegalArgumentException("z must not be NaN");
 		}
 		this.setX(x);
 		this.setY(y);
 		this.setZ(z);
 		return this;
 	}
 
 	@Override
 	public Vector3f set(Axis axis, Vector3f vector) {
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
 	public Vector3f set(Axis axis, float value) {
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
 	public Vector3f add(Vector3f vector) {
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
 	public Vector3f add(float value) {
 		this.addX(value);
 		this.addY(value);
 		this.addZ(value);
 		return this;
 	}
 
 	@Override
 	public Vector3f add(Axis axis, Vector3f vector) {
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
 	public Vector3f add(Axis axis, float value) {
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
 	public Vector3f subtract(Vector3f vector) {
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
 	public Vector3f subtract(float value) {
 		this.subtractX(value);
 		this.subtractY(value);
 		this.subtractZ(value);
 		return this;
 	}
 
 	@Override
 	public Vector3f subtract(Axis axis, Vector3f vector) {
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
 	public Vector3f subtract(Axis axis, float value) {
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
 	public Vector3f multiply(Vector3f vector) {
 		this.multiplyX(vector.x());
 		this.multiplyY(vector.y());
 		this.multiplyZ(vector.z());
 		return this;
 	}
 
 	@Override
 	public Vector3f multiply(double factor) {
 		this.multiplyX(factor);
 		this.multiplyY(factor);
 		this.multiplyZ(factor);
 		return this;
 	}
 
 	@Override
 	public Vector3f multiply(double x, double y, double z) {
 		this.multiplyX(x);
 		this.multiplyY(y);
 		this.multiplyZ(z);
 		return this;
 	}
 
 	@Override
 	public Vector3f multiply(Axis axis, Vector3f vector) {
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
 	public Vector3f multiply(Axis axis, double factor) {
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
 	public Vector3f divide(Vector3f vector) {
 		return this.divide(vector.x(), vector.y(), vector.z());
 	}
 
 	@Override
 	public Vector3f divide(double x, double y, double z) {
 		this.divideX(x);
 		this.divideY(y);
 		this.divideZ(z);
 		return this;
 	}
 
 	@Override
 	public double length() {
 		return Math.sqrt(Math.pow(this.x(), 2) + Math.pow(this.y(), 2) + Math.pow(this.z(), 2));
 	}
 
 	public float area() {
 		return this.x * this.y;
 	}
 
 	public float volume() {
 		return this.x * this.y * this.z;
 	}
 
 	@Override
 	public Vector3f normalize() {
 		float len = (float) this.length();
 		return this.set(this.x() / len,
 				this.y() / len,
 				this.z() / len);
 	}
 
 	@Override
 	public Vector3f reciprocal() {
 		this.setX(1 / this.x());
 		this.setY(1 / this.y());
 		this.setZ(1 / this.z());
 		return this;
 	}
 
 	@Override
 	public Vector3f reciprocal(Axis axis) {
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
 	public Vector3f interpolate(Vector3f dest, float offset) {
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
 	public Vector3f cross(Vector3f other) {
 		return this.set(this.y() * other.z() - other.y() * this.z(),
 				-this.x() * other.z() + other.x() * this.z(),
 				this.x() * other.y() - other.x() * this.y());
 	}
 
 	@Override
 	public Vector3f clear() {
 		return this.set(0f);
 	}
 
 	@Override
 	public Vector3f clear(Axis axis) {
 		return this.set(axis, 0f);
 	}
 
 	@Override
 	public Dimension toDimension() {
 		return new Dimension((int) x, (int) y);
 	}
 
 	@Override
 	public Point toPoint() {
 		return new Point((int) x, (int) y);
 	}
 
 	public Point2D.Float toPoint2D() {
 		return new Point2D.Float(x, y);
 	}
 
 	@Override
 	public Vector3f toMutable() {
 		return Vector3f.mutable(x, y, z);
 	}
 
 	@Override
 	public Vector3f toFrozen() {
 		if (!this.isMutable()) {
 			return this;
 		}
 		return Vector3f.frozen(x, y, z);
 	}
 
 	@Override
 	public Vector3f getThis() {
 		return this;
 	}
 
 	@Override
 	public boolean at(Vector3f vector) {
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
 		if (!(obj instanceof Vector3f)) {
 			return false;
 		}
 		final Vector3f other = (Vector3f) obj;
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
 		int result = 11;
 		result = 31 * result + (this.isMutable() ? 1 : 0);
 		result = 31 * result + java.lang.Float.floatToIntBits(this.x());
 		result = 31 * result + java.lang.Float.floatToIntBits(this.y());
 		result = 31 * result + java.lang.Float.floatToIntBits(this.z());
 		return result;
 	}
 
 	@Override
 	public String toString() {
 		return String.format("Vector3f[%s (%f, %f, %f)]", this.isMutable() ? "mutable" : "frozen", this.x(), this.y(), this.z());
 	}
 
 }
