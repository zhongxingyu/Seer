 package jgame.controller;
 
 import jgame.Context;
 import jgame.GObject;
 
 /**
  * A tween that allows for scaling of an object. This only supports uniform
  * scale.
  * 
  * @author William Chargin
  * 
  */
 public class ScaleTween extends TweenController {
 
 	/**
 	 * The starting scale.
 	 */
 	private final double start;
 
 	/**
 	 * The total scaling displacement over the duration over the controller.
 	 */
 	private final double delta;
 
 	/**
 	 * Creates the scaling tween with the given duration and start and end scale
 	 * values.
 	 * <p>
 	 * The tween will use linear interpolation.
 	 * 
 	 * @param duration
 	 *            the tween duration, in frames
 	 * @param start
 	 *            the starting scale
 	 * @param end
 	 *            the ending scale
 	 */
 	public ScaleTween(int duration, double start, double end) {
 		super(duration);
 		this.start = start;
		this.delta = end - start;
 	}
 
 	/**
 	 * Creates the scaling tween with the given duration, interpolation type,
 	 * and start and end scale values.
 	 * 
 	 * @param duration
 	 *            the tween duration, in frames
 	 * @param interpolationType
 	 *            the interpolation type
 	 * @param start
 	 *            the starting scale
 	 * @param end
 	 *            the ending scale
 	 */
 	public ScaleTween(int duration, Interpolation interpolationType,
 			double start, double end) {
 		super(duration, interpolationType);
 		this.start = start;
		this.delta = end - start;
 	}
 
 	@Override
 	protected void interpolate(GObject target, Context context,
 			double percentage) {
 		target.setScale(start + delta * percentage);
 	}
 
 }
