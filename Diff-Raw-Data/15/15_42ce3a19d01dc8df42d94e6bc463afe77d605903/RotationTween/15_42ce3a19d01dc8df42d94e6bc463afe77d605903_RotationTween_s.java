 package jgame.controller;
 
 import jgame.Context;
 import jgame.GObject;
 
 /**
  * A tween that allows for rotation of an object. This supports rotation of
  * greater than one revolution (e.g., from {@code 0} to {@code 720} for two
  * revolutions).
  * 
  * @author William Chargin
  * 
  */
 public class RotationTween extends TweenController {
 
 	/**
 	 * The starting rotation.
 	 */
 	private final double start;
 
 	/**
 	 * The total angular displacement over the duration over the controller.
 	 */
 	private final double delta;
 
 	/**
 	 * Creates the rotation tween with the given duration and start and end
 	 * rotation values.
 	 * <p>
 	 * The tween will use linear interpolation.
 	 * 
 	 * @param duration
 	 *            the tween duration, in frames
 	 * @param start
 	 *            the starting rotation
 	 * @param end
 	 *            the ending rotation
 	 */
 	public RotationTween(int duration, double start, double end) {
 		super(duration);
 		this.start = start;
		this.delta = start - end;
 	}
 
 	/**
 	 * Creates the rotation tween with the given duration, interpolation type,
 	 * and start and end rotation values.
 	 * 
 	 * @param duration
 	 *            the tween duration, in frames
 	 * @param interpolationType
 	 *            the interpolation type
 	 * @param start
 	 *            the starting rotation
 	 * @param end
 	 *            the ending rotation
 	 */
 	public RotationTween(int duration, Interpolation interpolationType,
 			double start, double end) {
 		super(duration, interpolationType);
 		this.start = start;
		this.delta = start - end;
 	}
 
 	@Override
 	protected void interpolate(GObject target, Context context,
 			double percentage) {
 		target.setRotation(start + delta * percentage);
 	}
 
 }
