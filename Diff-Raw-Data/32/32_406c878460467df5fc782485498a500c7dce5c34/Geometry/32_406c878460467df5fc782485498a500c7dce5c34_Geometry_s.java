 package com.bluespot.geom;
 
 import java.awt.Dimension;
 import java.awt.Insets;
 import java.awt.Point;
 import java.awt.Rectangle;
 
 /**
  * Collection of useful methods relating to geometry.
  * 
  * @author Aaron Faanes
  * 
  */
 public final class Geometry {
 
     private Geometry() {
         throw new AssertionError("This class cannot be instantiated");
     }
 
     public static final Operations Floor = new AbstractOperations() {
 
         @Override
         protected int asInteger(final double value) {
             return (int) Math.floor(value);
         }
 
     };
 
     public static final Operations Round = new AbstractOperations() {
 
         @Override
         protected int asInteger(final double value) {
             return (int) Math.round(value);
         }
     };
 
     public static final Operations Ceil = new AbstractOperations() {
 
         @Override
         protected int asInteger(final double value) {
             return (int) Math.ceil(value);
         }
     };
 
     /**
      * Vertically and horizontally centers the given {@link Rectangle} over the
      * given point.
      * 
      * @param rectangle
      *            the {@code Rectangle} that will be aligned
      * @param center
      *            the {@code Point} that the {@code Rectangle} will be centered
      *            on
      */
     public static void alignCenter(final Rectangle rectangle, final Point center) {
         final Dimension halfDim = rectangle.getSize();
         Geometry.Round.halfSize(halfDim);
         rectangle.setLocation(new Point(center.x - halfDim.width, center.y - halfDim.height));
     }
 
     /**
      * Gets the bounds of the given {@link Insets}.
      * <p>
      * Note that this method allows negative inset values, but its behavior is
      * undefined.
      * 
      * @param insets
      *            the {@code Insets} that are used in this operation
      * @return The bounds, equal to {@code (left + right, top + bottom)}
      */
     public static Dimension bounds(final Insets insets) {
         return new Dimension(insets.left + insets.right, insets.top + insets.bottom);
     }
 
     /**
      * Returns a {@link Dimension} that is equivalent to the distance between
      * the given {@link Point} objects.
      * <p>
      * Note that the signs of the dimension's values are dependent on the order
      * of the points provided; if {@code destination} is lower than {@code
      * destination} on either axis, its value in the returned {@code Dimension}
      * will be negative.
      * 
      * @param source
      *            the starting {@code Point}
      * @param destination
      *            the end {@code Point}
      * @return The {@code Dimension} that is equivalent to the distance between
      *         the specified {@code Point} objects.
      */
     public static Dimension bounds(final Point source, final Point destination) {
         return new Dimension(destination.x - source.x, destination.y - source.y);
     }
 
     /**
      * Check whether {@code candidate} is between {@code a} and {@code b}. The
      * order of the bounds does not matter.
      * 
      * @param a
      *            the first numeric bound
      * @param b
      *            the second numeric bound
      * @param candidate
      *            the number to test
      * @return {@code true} if the candidate is between (but not equal to)
      *         {@code a} and {@code b}.
      * @see com.bluespot.geom.Geometry#containsInclusive
      */
     public static boolean containsExclusive(final double a, final double b, final double candidate) {
         final double lowerBound = Math.min(a, b);
         final double upperBound = Math.max(a, b);
         return lowerBound < candidate && candidate < upperBound;
     }
 
     /**
      * Check whether {@code candidate} is between, or equal to, {@code a} and
      * {@code b}. The order of the bounds does not matter.
      * 
      * @param a
      *            the first numeric bound
      * @param b
      *            the second numeric bound
      * @param candidate
      *            the number to test
      * @return {@code true} if the candidate is between (or equal to) {@code a}
      *         and {@code b}.
      * @see com.bluespot.geom.Geometry#containsExclusive
      */
     public static boolean containsInclusive(final double a, final double b, final double candidate) {
         final double lowerBound = Math.min(a, b);
         final double upperBound = Math.max(a, b);
         return lowerBound <= candidate && candidate <= upperBound;
     }
 
     /**
      * Flips the specified {@link Dimension} around both axes. For example, a
      * dimension of {@code (1, 1)} will become {@code (-1, -1)}.
      * 
      * @param dimension
      *            the {@code Dimension} that will be flipped as a result of this
      *            operation
      */
     public static void flip(final Dimension dimension) {
         dimension.width *= -1;
         dimension.height *= -1;
     }
 
     /**
      * Flips the specified {@link Dimension} over the X-axis. For example, a
      * dimension of {@code (1, 1)} will become {@code (1, -1)}.
      * 
      * @param dimension
      *            the {@code Dimension} that will be flipped as a result of this
      *            operation
      */
     public static void flipOverX(final Dimension dimension) {
         dimension.height *= -1;
     }
 
     /**
      * Flips the specified {@link Dimension} over the Y-axis. For example, a
      * dimension of {@code (1, 1)} will become {@code (-1, 1)}.
      * 
      * @param dimension
      *            the {@code Dimension} that will be flipped as a result of this
      *            operation
      */
     public static void flipOverY(final Dimension dimension) {
         dimension.width *= -1;
     }
 
     /**
      * Returns a {@link Point} that is equal to the center of the specified
      * {@link Rectangle} object.
      * 
      * @param rectangle
      *            the {@code Rectangle} used in this operation
      * @return the center of the specified {@code Rectangle}
      */
     public static Point getCenter(final Rectangle rectangle) {
         return new Point((int) rectangle.getCenterX(), (int) rectangle.getCenterY());
     }
 
     /**
      * Returns a square {@link Dimension} which has an hypotenuse equal to the
      * specified one. That is, the returned {@code Dimension} will have a width
      * and height whose hypotenuse is equal to the specified {@code double}
      * value.
      * 
      * @param hypotenuse
      *            the hypotenuse of the created {@code Dimension}
      * @return a {@Dimension} as described above
      */
     public static Dimension getDimensionFromHypotenuse(final double hypotenuse) {
         final int side = (int) Math.sqrt(Math.pow(hypotenuse, 2) / 2);
         return new Dimension(side, side);
 
     }
 
     /**
      * Makes the specified {@link Dimension} a square by using the greater of
      * its two axis-values. In other words, it will grow the {@code Dimension}.
      * <p>
      * This method will never cause the {@code Rectangle} to grow in size or
      * take up regions that it previously did not occupy. This means that signs
      * are preserved; the {@code Rectangle} object's dimensions will never
      * "flip" due to opposing signs changing the resulting size.
      * 
      * @param dimension
      *            The {@code Dimension} to grow. It will be modified by this
      *            operation
      * @see Geometry#trimToSquare(java.awt.Dimension)
      */
     public static void growToSquare(final Dimension dimension) {
         final int largestAxis = Math.abs(Geometry.maxAxis(dimension));
         // We use signum to preserve the sign of the original dimension.
         final int width = largestAxis * (int) Math.signum(dimension.width);
         final int height = largestAxis * (int) Math.signum(dimension.height);
         dimension.setSize(width, height);
     }
 
     /**
      * Grows the specified {@link Rectangle} such that it becomes a square.
      * 
      * @param rectangle
      *            the {@code Rectangle} to grow. It will be modified by this
      *            operation
      * @see Geometry#growToSquare(Dimension)
      */
     public static void growToSquare(final Rectangle rectangle) {
         final Dimension dimension = rectangle.getSize();
         Geometry.growToSquare(dimension);
         rectangle.setSize(dimension);
     }
 
     /**
      * Creates an {@link Insets} object with values of the given magnitude on
      * all sides.
      * 
      * @param magnitude
      *            the value that the created {@code Insets} object will use for
      *            every side
      * @return the created {@code Insets} object
      */
     public static Insets insets(final int magnitude) {
         return new Insets(magnitude, magnitude, magnitude, magnitude);
     }
 
     /**
      * Creates an {@link Insets} object with values of the given height and
      * width magnitudes for its sides.
      * 
      * @param widthMagnitude
      *            the value of the left and right {@code Insets} values
      * @param heightMagnitude
      *            the value of the top and bottom {@code Insets} values
      * @return the created {@code Insets} object
      */
     public static Insets insets(final int widthMagnitude, final int heightMagnitude) {
         return new Insets(heightMagnitude, widthMagnitude, heightMagnitude, widthMagnitude);
     }
 
     /**
      * Creates a new {@link Point} that is interpolated between the given source
      * and destination {@code Point} objects. The exact location of the returned
      * point is determined by the value of {@code interpolation}. This method
      * may be used to extrapolate points as well.
      * 
      * @param source
      *            the source, or starting, {@code Point}. It is not modified by
      *            this method.
      * @param destination
      *            the final, or end, {@code Point}. It is not modified by this
      *            method.
      * @param interpolation
      *            the percent that the created point should use to interpolate.
      *            For example:
      *            <ul>
      *            <li>{@code 0.0} will make the created point equal to {@code
      *            source}
      *            <li>{@code 1.0} will make the created point equal to {@code
      *            destination}
      *            <li>{@code 2.0} will make the created point be on the line
      *            made by the two specified points, but twice as far away from
      *            {@code source} as {@code destination} currently is.
      * @return a new, interpolated {@code Point}
      */
     public static Point interpolate(final Point source, final Point destination, final double interpolation) {
         final Dimension bounds = Geometry.bounds(source, destination);
         final int interpolatedX = (int) (source.x + (bounds.width * interpolation));
         final int interpolatedY = (int) (source.y + (bounds.height * interpolation));
         return new Point(interpolatedX, interpolatedY);
     }
 
     /**
      * Returns the larger of the two axes' value of the specified
      * {@link Dimension}. Notice that this method defines "larger" as larger in
      * magnitude, rather than numerical value. For example, {@code -2} is larger
      * than {@code 1} according to this method.
      * 
      * @param dimension
      *            the {@code Dimension} to use in this operation
      * @return the larger axis's value
      */
     public static int maxAxis(final Dimension dimension) {
         return Math.abs(dimension.width) > Math.abs(dimension.height) ? dimension.width : dimension.height;
     }
 
     /**
      * Returns the smaller of the two axes' value of the specified
      * {@link Dimension}. Notice that this method defines "smaller" as smaller
      * in magnitude, rather than numerical value. For example, {@code 1} is
      * smaller than {@code -2} according to this method.
      * 
      * @param dimension
      *            the {@code Dimension} to use in this operation
      * @return the smaller axis's value
      */
     public static int minAxis(final Dimension dimension) {
         return Math.abs(dimension.width) < Math.abs(dimension.height) ? dimension.width : dimension.height;
     }
 
     /**
      * Subtracts the specified {@link Insets} from the specified
      * {@link Dimension}.
      * <p>
      * If the {@code Insets} specified are greater than the dimensions of the
      * {@code Dimension}, the {@code Dimension} will have a width and height of
      * zero.
      * 
      * @param dimension
      *            the {@code Dimension } that will be trimmed during this
      *            operation. Negative values for width and height are supported.
      * @param insets
      *            the {@code Insets} to trim with. Negative values for the
      *            {@code Insets} are allowed, but will grow the {@code
      *            Dimension} instead of trimming it.
      */
     public static void subtractInsets(final Dimension dimension, final Insets insets) {
         final Dimension insetDim = Geometry.bounds(insets);
         if (insetDim.width >= Math.abs(dimension.width) || insetDim.height >= Math.abs(dimension.height)) {
             dimension.setSize(0, 0);
             return;
         }
         // Use signum here to guarantee that our rect doesn't grow if it has a
         // negative dimension.
         dimension.width -= Math.signum(dimension.width) * insetDim.width;
         dimension.height -= Math.signum(dimension.height) * insetDim.height;
     }
 
     /**
      * Subtracts the specified {@link Insets} from the specified
      * {@link Rectangle}. The {@code Rectangle } will be centered using its
      * original center.
      * <p>
      * If the {@code Insets} specified are greater than the dimensions of the
      * {@code Rectangle}, the {@code Rectangle} will have a width and height of
      * zero.
      * 
      * @param rectangle
      *            the {@code Rectangle } that will be trimmed during this
      *            operation. Negative values for width and height are supported.
      * @param insets
      *            the {@code Insets} to trim with. Negative values for the
      *            {@code Insets} are allowed, but will grow the {@code
      *            Dimension} instead of trimming it.
      */
     public static void subtractInsets(final Rectangle rectangle, final Insets insets) {
         final Dimension insetDim = Geometry.bounds(insets);
         if (insetDim.width >= Math.abs(rectangle.width) || insetDim.height >= Math.abs(rectangle.height)) {
             rectangle.setLocation(Geometry.getCenter(rectangle));
             rectangle.setSize(0, 0);
             return;
         }
         rectangle.x += Math.signum(rectangle.width) * insets.left;
         rectangle.y += Math.signum(rectangle.height) * insets.top;
 
         // Use signum here to guarantee that our rect doesn't grow if it has a
         // negative dimension.
         rectangle.width -= Math.signum(rectangle.width) * insetDim.getWidth();
         rectangle.height -= Math.signum(rectangle.height) * insetDim.getHeight();
     }
 
     /**
      * Makes the specified {@link Dimension} a square by using the lesser of its
      * two axis-values. In other words, it will 'trim' the {@code Dimension}.
      * <p>
      * This method will never cause the {@code Rectangle} to grow in size or
      * take up regions that it previously did not occupy. This means that signs
      * are preserved; the {@code Rectangle} object's dimensions will never
      * "flip" due to opposing signs changing the resulting size.
      * 
      * @param dimension
      *            the {@code Dimension} to trim. It will be modified by this
      *            operation
      * @see Geometry#growToSquare(java.awt.Dimension)
      */
     public static void trimToSquare(final Dimension dimension) {
         final int smallestAxis = Math.abs(Geometry.minAxis(dimension));
         // We use signum to preserve the sign of the original dimension.
         final int width = smallestAxis * (int) Math.signum(dimension.width);
         final int height = smallestAxis * (int) Math.signum(dimension.height);
         dimension.setSize(width, height);
     }
 
     /**
      * Trims the specified {@link Rectangle} such that it becomes a square.
      * <p>
      * The {@code Rectangle} object's location is never affected by this method.
      * 
      * @param rectangle
      *            the {@code Rectangle} to trim. It will be modified by this
      *            operation
      * @see Geometry#trimToSquare(Dimension)
      * @see Geometry#alignCenter(Rectangle, Point)
      */
     public static void trimToSquare(final Rectangle rectangle) {
         final Dimension dimension = rectangle.getSize();
         Geometry.trimToSquare(dimension);
         rectangle.setSize(dimension);
     }
 
 }
