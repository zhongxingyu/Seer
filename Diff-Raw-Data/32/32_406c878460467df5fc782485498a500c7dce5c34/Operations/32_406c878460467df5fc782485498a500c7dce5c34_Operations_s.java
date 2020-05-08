 package com.bluespot.geom;
 
 import java.awt.Dimension;
 import java.awt.Rectangle;
 
 /**
  * Defines a set of geometric operations. Implementators typically vary by the
  * precision and rounding rules the implements.
  * 
  * @author Aaron Faanes
  * @see AbstractOperations
  */
 public interface Operations {
 
     /**
      * Doubles the specified {@link Dimension}.
      * 
      * @param dimension
      *            the {@code Dimension} that will be halved as a result of this
      *            operation
      * @see #multiply(Dimension, double)
      */
     public void doubleSize(Dimension dimension);
 
     /**
      * Doubles the specified {@link Rectangle}.
      * 
     * @param dimension
      *            the {@code Rectangle} that will be halved as a result of this
      *            operation
      * @see #multiply(Rectangle, double)
      */
     public void doubleSize(Rectangle rectangle);
 
     /**
      * Halves the specified {@link Dimension}.
      * 
      * @param dimension
      *            the {@code Dimension} that will be halved as a result of this
      *            operation
      * @see #divide(Dimension, double)
      */
     public void halfSize(Dimension dimension);
 
     /**
      * Halves the specified {@link Rectangle}.
      * 
     * @param dimension
      *            the {@code Rectangle} that will be halved as a result of this
      *            operation
      * @see #divide(Rectangle, double)
      */
     public void halfSize(Rectangle rectangle);
 
     /**
      * Multiply the specified {@link Dimension} by the specified multiplier. The
      * {@code Dimension} object's width and height will be multiplied by the
      * specified multiplier.
      * <p>
      * For the complementary function, see {@link #divide(Dimension, double)}.
      * 
      * @param dimension
      *            the {@code Dimension} to scale. It will be modified by this
      *            operation.
      * @param multiplier
      *            the scale
      */
     public void multiply(Dimension dimension, double multiplier);
 
     /**
      * Multiply the specified {@link Dimension} by the specified multiplier. The
      * {@code Dimension} object's width and height will be multiplied by their
      * respective multipliers.
      * <p>
      * For the complementary function, see
      * {@link #divide(Dimension, double, double)}.
      * 
      * @param dimension
      *            the {@code Dimension} that will be modified by this operation
      * @param widthMultiplier
      *            The width's multiplier
      * @param heightMultiplier
      *            The height's multiplier
      */
     public void multiply(Dimension dimension, double widthMultiplier, double heightMultiplier);
 
     /**
      * Multiply the dimensions of the specified {@link Rectangle} by the
      * specified multiplier. The {@code Rectangle} will remain aligned to its
      * original top-left corner.
      * <p>
      * For the complementary function, see {@link #divide(Rectangle, double)}.
      * 
      * @param rectangle
      *            the target {@code Rectangle} that will be modified by this
      *            operation
      * @param multiplier
      *            the amount that the {@code Rectangle} will be scaled by
      */
     public void multiply(Rectangle rectangle, double multiplier);
 
     /**
      * Multiply the dimensions of the specified {@link Rectangle} by the
      * specified multiplier. The {@code Rectangle} will remain aligned to its
      * original top-left corner.
      * <p>
      * For the complementary function, see {@link #divide(Rectangle, double)}.
      * 
      * @param rectangle
      *            the target {@code Rectangle} that will be modified by this
      *            operation
     * @param widthDenominator
     *            a non-zero value that acts as the denominator for width in
     *            this division operation
     * @param heightDenominator
     *            a non-zero value that acts as the denominator for height in
      *            this division operation
      */
     public void multiply(Rectangle rectangle, double widthMultipler, double heightMultiplier);
 
     /**
      * Divides the specified {@link Dimension} by the specified denominator. The
      * {@code Dimension} object's width and height will be divided by the
      * specified denominator.
      * 
      * @param dimension
      *            the {@code Dimension} that will be modified by this operation
      * @param denominator
      *            a non-zero value that acts as the denominator for both width
      *            and height in this division operation
      * @throws IllegalArgumentException
      *             if {@code denominator} is zero
      */
     public void divide(Dimension dimension, double denominator);
 
     /**
      * Divides the specified {@link Dimension} by the specified denominators.
      * The {@code Dimension} object's width and height will be divided by their
      * respective denominators.
      * <p>
      * For the complementary function, see
      * {@link #multiply(Dimension, double, double)}.
      * 
      * @param dimension
      *            the {@code Dimension} that will be modified by this operation
      * @param widthDenominator
      *            a non-zero value that acts as the denominator for width in
      *            this division operation
      * @param heightDenominator
      *            a non-zero value that acts as the denominator for height in
      *            this division operation
      * @throws IllegalArgumentException
      *             if either denominator is zero
      * @see #multiply(Dimension, double, double)
      */
     public void divide(Dimension dimension, double widthDenominator, double heightDenominator);
 
     /**
      * Divides the dimensions of the specified {@link Rectangle} by the
      * specified denominator. The {@code Rectangle} will remain aligned to its
      * original top-left corner.
      * <p>
      * For the complementary function, see {@link #multiply(Rectangle, double)}.
      * 
      * @param rectangle
      *            the target {@code Rectangle} that will be modified by this
      *            operation
      * @param denominator
      *            a non-zero value that acts as the denominator for both width
      *            and height in this division operation
      */
     public void divide(Rectangle rectangle, double denominator);
 
     /**
      * Divides the dimensions of the specified {@link Rectangle} by the
      * specified denominators. The {@code Rectangle} will remain aligned to its
      * original top-left corner.
      * <p>
      * For the complementary function, see
      * {@link #multiply(Rectangle, double, double)}.
      * 
      * @param rectangle
      *            the target {@code Rectangle} that will be modified by this
      *            operation
      * @param widthDenominator
      *            a non-zero value that acts as the denominator for width in
      *            this division operation
      * @param heightDenominator
      *            a non-zero value that acts as the denominator for height in
      *            this division operation
      */
     public void divide(Rectangle rectangle, double widthDenominator, double heightDenominator);
 
 }
