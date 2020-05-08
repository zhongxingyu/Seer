 package se2.e.engine3d.runtime.annimations;
 
 import se2.e.engine3d.runtime.AnimatedObject;
 import se2.e.engine3d.runtime.Animation;
import se2.e.utilities.PathInterpolator;
 import se2.e.utilities.Where;
 
 
 public class MoveAnimation implements Animation {
 	private Animation parent;
	private PathInterpolator path;
 	private double speed;
 	private Where where = null;
 
 	/**
 	 * Creates a new move animation.
 	 * 
 	 * @param object
 	 * @param path
 	 * @param speed
 	 */
	public MoveAnimation(PathInterpolator path, double speed) {
 		this.speed = speed;
 		this.path = path;
 		where = path.start();
 		}
 	
 	@Override
 	public boolean recalculate() {
 		double distance = speed*(parent.getStartTick() - getTarget().getTick());
 		where = path.findPosition(distance, where);
 		getTarget().moveTo(where);
 		return path.getLength() >= distance;
 		}
 
 	@Override
 	public AnimatedObject getTarget() { return parent.getTarget(); }
 
 	@Override
 	public long getStartTick() { return parent.getStartTick(); }
 
 	@Override
 	public void setParent(Animation parent) {
 		this.parent = parent;
 		}
 	
 	}
