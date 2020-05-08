 package jgame.controller;
 
 import jgame.Context;
 import jgame.GObject;
 
 /**
  * A tween that allows for modifying the alpha of an object.
  * 
  * @author William Chargin
  * 
  */
 public class AlphaTween extends TweenController {
 
 	/**
	 * The starting alpha.
 	 */
 	private final double start;
 
 	/**
	 * The total alpha displacement over the duration over the controller.
 	 */
 	private final double delta;
 
 	/**
 	 * Creates the alpha tween with the given duration and start and end alpha
 	 * values.
 	 * <p>
 	 * The tween will use linear interpolation.
 	 * 
 	 * @param duration
 	 *            the tween duration, in frames
 	 * @param start
 	 *            the starting alpha
 	 * @param end
 	 *            the ending alpha
 	 */
 	public AlphaTween(int duration, double start, double end) {
 		super(duration);
 		this.start = start;
 		this.delta = end - start;
 	}
 
 	/**
	 * Creates the alpha tween with the given duration, interpolation type, and
	 * start and end alpha values.
 	 * 
 	 * @param duration
 	 *            the tween duration, in frames
 	 * @param interpolationType
 	 *            the interpolation type
 	 * @param start
 	 *            the starting alpha
 	 * @param end
 	 *            the ending alpha
 	 */
 	public AlphaTween(int duration, Interpolation interpolationType,
 			double start, double end) {
 		super(duration, interpolationType);
 		this.start = start;
 		this.delta = end - start;
 	}
 
 	@Override
 	protected void interpolate(GObject target, Context context,
 			double percentage) {
 		target.setAlpha(Math.min(1, Math.max(0, start + delta * percentage)));
 	}
 
 }
