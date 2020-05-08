 package se2.e.engine3d.j3d.animations;
 
 import java.util.Enumeration;
 import java.util.logging.Logger;
 
 import javax.media.j3d.Behavior;
 import javax.media.j3d.BoundingSphere;
 import javax.media.j3d.WakeupCondition;
 import javax.vecmath.Point3d;
 
 import se2.e.engine3d.j3d.DynamicBranch;
 import se2.e.simulator.runtime.petrinet.RuntimeToken;
 
 /**
  * The Class RuntimeAnimation.
  * 
  * @param <T> the generic type
  * @author cosmin
  */
 public abstract class RuntimeAnimation<T> extends Behavior {
 
 	/** The target branch. */
 	protected DynamicBranch targetBranch;
 
 	/** The animation. */
 	protected T animation;
 
 	/** The associated runtime token. */
 	protected RuntimeToken token;
 
 	/** The listener. */
 	protected RuntimeAnimationListener animationListener;
 
 	/** Whether it is attached to root. */
 	private boolean attachedToRoot = false;
 
 	/**
 	 * Inits the runtime animation. Usually, this is called in the initialization method of the Behavior associated with
 	 * the animation.
 	 * 
 	 * @return the wakeup condition defining when it should first update the animation.
 	 * {@link RuntimeAnimation#onUpdateAnimation()} will be performed, to update the animation and to set up next wakeup
 	 * conditions.
 	 */
 	public abstract WakeupCondition init();
 
 	/**
 	 * Method called when the behavior set up in the initialization or in a previous call to this method is triggered.
 	 * Should update the Java3D visualization and return a {@link WakeupCondition} stating when it should be triggered
 	 * again.
 	 * <p>
 	 * Also this method must make the verification for finishing of animation and, if needed, call the
 	 * 
 	 * @return the wakeup condition defining when it should be called again.
 	 * {@link RuntimeAnimation#onAnimationFinished()} method.
 	 * </p>
 	 */
 	public abstract WakeupCondition onUpdateAnimation();
 
 	/**
 	 * Method called when this animation is finished. This method detaches the branch associated with the Runtime
 	 * Animation from its parent and notifies the listener.
 	 * <p>
 	 * NOTE - It is highly recommended to override this method in the implementing classes, eventually calling this
 	 * method (super.onAnimationFinished().
 	 * </p>
 	 * 
 	 * @author cosmin
 	 */
 	protected void onAnimationFinished() {
		// Don't detach any more, as the detach will be done when token is destroyed
		// this.targetBranch.getBranchGroup().detach();
 		animationListener.animationFinished(token);
 	}
 
 	/**
 	 * Instantiates a new runtime animation.
 	 * 
 	 * @param targetBranch the target branch
 	 * @param animation the animation
 	 * @param token the token
 	 * @param animationListener the animation listener
 	 * @author cosmin
 	 */
 	public RuntimeAnimation(DynamicBranch targetBranch, T animation, RuntimeToken token,
 			RuntimeAnimationListener animationListener) {
 		super();
 		this.targetBranch = targetBranch;
 		this.token = token;
 		this.animation = animation;
 		this.animationListener = animationListener;
 		this.attachedToRoot = false;
 
 		// Connect the behavior to the branch group
		this.targetBranch.getBranchGroup().detach();
		if (targetBranch.getBehaviorNode() != null)
			this.getTargetBranch().getBranchGroup().removeChild(this.targetBranch.getBehaviorNode());
 		this.targetBranch.setBehaviorNode(this);
 		this.targetBranch.getBranchGroup().addChild(this);
 	}
 
 	/**
 	 * Gets the target branch.
 	 * 
 	 * @return the target branch
 	 */
 	public DynamicBranch getTargetBranch() {
 		return targetBranch;
 	}
 
 	/**
 	 * Gets the animation.
 	 * 
 	 * @return the animation
 	 */
 	public Object getAnimation() {
 		return animation;
 	}
 
 	/**
 	 * Gets the associated runtime token.
 	 * 
 	 * @return the token
 	 */
 	public RuntimeToken getToken() {
 		return token;
 	}
 
 	/**
 	 * Checks if is attached to root.
 	 * 
 	 * @return true, if is attached to root
 	 */
 	public boolean isAttachedToRoot() {
 		return attachedToRoot;
 	}
 
 	/**
 	 * Sets the attached to root.
 	 * 
 	 * @param attachedToRoot the new attached to root
 	 */
 	public void setAttachedToRoot(boolean attachedToRoot) {
 		this.attachedToRoot = attachedToRoot;
 	}
 
 	/**
 	 * Initialize.
 	 */
 	@Override
 	/* @author cosmin. */
 	public void initialize() {
 		// Initialize the behavior and prepare first wake-up criterion
 		Logger.getAnonymousLogger().info(
 				"Initializing behavior corresponding to animation " + this.getClass().getSimpleName());
 		WakeupCondition criteria = init();
 		if (criteria == null)
 			return;
 		wakeupOn(criteria);
 		BoundingSphere bounds = new BoundingSphere(new Point3d(0.0, 0.0, 0.0), 1000.0);
 		this.setSchedulingBounds(bounds);
 	}
 
 	/**
 	 * Process stimulus.
 	 * 
 	 * @param inCriteria the in criteria
 	 */
 	@Override
 	public void processStimulus(@SuppressWarnings("rawtypes") Enumeration inCriteria) {
 		WakeupCondition criteria = onUpdateAnimation();
 		if (criteria != null)
 			wakeupOn(criteria);
 	}
 }
