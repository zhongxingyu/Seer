 package se2.e.engine3d.j3d.animations;
 
 import java.util.logging.Logger;
 
 import javax.media.j3d.WakeupCondition;
 
 import se2.e.engine3d.j3d.DynamicBranch;
 import se2.e.simulator.runtime.petrinet.RuntimeToken;
 import animations.Animation;
 import animations.Sequence;
 
 public class RuntimeSequenceAnimation extends RuntimeAnimation<Sequence> {
 
 	private int currentAnimationIndex;
 	private Animation currentAnimation;
 
 	public RuntimeSequenceAnimation(DynamicBranch targetBranch, Sequence animation, RuntimeToken token,
 			RuntimeAnimationListener listener) {
 		super(targetBranch, animation, token, listener);
 	}
 
 	@Override
 	public WakeupCondition init() {
 		Logger.getAnonymousLogger().info("Initializing RuntimeSequenceAnimation...");
 		currentAnimationIndex = 0;
 		currentAnimation = animation.getComponents().get(0);
 		// TODO: Finish it
		//TODO: Register itself as RuntimeAnimationListener for children animations 
 		return null;
 	}
 
 	@Override
 	public WakeupCondition onUpdateAnimation() {
 		// TODO: Finish it
 		return null;
 	}
 
 	@Override
 	protected void onAnimationFinished() {
 		Logger.getAnonymousLogger().info("Finishing Sequence Animation...");
 		if (currentAnimationIndex < animation.getComponents().size() - 1) {
 			// TODO: Finish it
 		}
 
 		super.onAnimationFinished();
 
 	}
 
 }
