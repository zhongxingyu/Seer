 package se2.e.engine3d.runtime;
 
 import se2.e.engine3d.runtime.annimations.MoveAnimation;
import se2.e.utilities.Path;
 
 public class AnimationBase implements Animation {
 	private AnimatedObject target;
 	private Animation root;
 	private long startTick;
 	
 	public AnimationBase(AnimatedObject target, Animation root) {
 		this.target = target;
 		this.startTick = target.getTick();
 		this.root = root;
 		this.root.setParent(this);
 		}
 	
 	/**
 	 * Create a new move animation.
 	 * 
 	 * This is an example of a factory method, that might be useful.
 	 * 
 	 * @param target the targeted animation object
 	 * @param path the path
 	 * @param speed speed of the move
 	 * @return the animation base of the the move animation.
 	 */
	public static Animation createMove(AnimatedObject target, Path path, double speed) {
 		Animation root = new MoveAnimation(path, speed);
 		return new AnimationBase(target, root);
 		}
 	
 	public AnimatedObject getTarget() {
 		return target;
 		}
 
 	@Override
 	public long getStartTick() {
 		return startTick;
 		}
 	
 	@Override
 	public boolean recalculate() {
 		if (root == null) return true;
 		return root.recalculate();
 		}
 
 	/**
 	 * Ignored while this is the parent
 	 */
 	@Override
 	public void setParent(Animation parent) { }
 	
 	}
