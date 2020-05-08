 package com.bluespot.geom;
 
 import java.awt.Dimension;
 import java.awt.Rectangle;
 
 /**
  * A skeletal {@link Operations} implementation.
  * 
  * @author Aaron Faanes
  */
 public abstract class AbstractOperations implements Operations {
 
 	@Override
 	public void doubleSize(final Rectangle rectangle) {
 		this.multiply(rectangle, 2.0d);
 	}
 
 	@Override
 	public void doubleSize(final Dimension dimension) {
 		this.multiply(dimension, 2.0d);
 	}
 
 	@Override
 	public void halfSize(final Dimension dimension) {
 		this.divide(dimension, 2.0d);
 	}
 
 	@Override
 	public void halfSize(final Rectangle rectangle) {
 		this.divide(rectangle, 2.0d);
 	}
 
 	@Override
 	public void divide(final Dimension dimension, final double denominator) {
 		this.divide(dimension, denominator, denominator);
 	}
 
 	@Override
 	public void divide(final Rectangle rectangle, final double denominator) {
 		this.divide(rectangle, denominator, denominator);
 	}
 
 	@Override
 	public void divide(final Rectangle rectangle, final double widthDenominator, final double heightDenominator) {
 		final Dimension dimension = rectangle.getSize();
 		this.divide(dimension, widthDenominator, heightDenominator);
 		rectangle.setSize(dimension);
 	}
 
 	@Override
 	public void divide(final Dimension dimension, final double widthDenominator, final double heightDenominator) {
 		final double width = dimension.width / widthDenominator;
 		final double height = dimension.height / heightDenominator;
 		dimension.width = this.asInteger(width);
 		dimension.height = this.asInteger(height);
 	}
 
 	@Override
 	public void multiply(final Dimension dimension, final double widthMultiplier, final double heightMultiplier) {
 		final double width = dimension.width * widthMultiplier;
 		final double height = dimension.height * heightMultiplier;
 		dimension.width = this.asInteger(width);
 		dimension.height = this.asInteger(height);
 	}
 
 	@Override
 	public void multiply(final Rectangle rectangle, final double widthMultipler, final double heightMultiplier) {
 		final Dimension dimension = rectangle.getSize();
		this.divide(dimension, widthMultipler, heightMultiplier);
 		rectangle.setSize(dimension);
 	}
 
 	@Override
 	public void multiply(final Dimension dimension, final double multiplier) {
 		this.multiply(dimension, multiplier, multiplier);
 	}
 
 	@Override
 	public void multiply(final Rectangle rectangle, final double multiplier) {
 		this.multiply(rectangle, multiplier, multiplier);
 	}
 
 	/**
 	 * Converts the specified double value to an integer.
 	 * 
 	 * @param value
 	 *            the value that is converted by this method
 	 * @return a integer representation of the specified value
 	 */
 	protected abstract int asInteger(double value);
 
 }
