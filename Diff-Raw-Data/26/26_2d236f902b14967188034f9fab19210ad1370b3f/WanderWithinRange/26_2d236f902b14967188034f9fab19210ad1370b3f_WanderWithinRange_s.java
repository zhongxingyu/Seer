 package net.carmgate.morph.model.behaviors.steering;
 
 import net.carmgate.morph.model.Model;
 import net.carmgate.morph.model.behaviors.common.ActivatedMorph;
 import net.carmgate.morph.model.behaviors.common.Behavior;
 import net.carmgate.morph.model.behaviors.common.Needs;
 import net.carmgate.morph.model.common.Vect3D;
 import net.carmgate.morph.model.entities.Morph.MorphType;
 import net.carmgate.morph.model.entities.common.Entity;
 import net.carmgate.morph.ui.common.RenderUtils;
 
 import org.lwjgl.opengl.GL11;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 @Needs({ @ActivatedMorph(morphType = MorphType.SIMPLE_PROPULSOR) })
 public class WanderWithinRange extends Wander {
 
 	private static Logger LOGGER = LoggerFactory.getLogger(WanderWithinRange.class);
 
 	private final Entity target;
 	private final float range;
 	private float delta = 0;
 
 	/**
 	 * Do not use.
 	 */
 	@Deprecated
 	public WanderWithinRange() {
 		this(null, 0, 0, null, 0);
 	}
 
 	public WanderWithinRange(Entity entityToMove, float wanderFocusDistance, float wanderRadius, Entity target, float range) {
 		super(entityToMove, wanderFocusDistance, wanderRadius);
 		this.target = target;
 		this.range = range;
		delta = 0.5f;
 	}
 
 	@Override
 	public Behavior cloneForEntity(Entity entity) {
 		return new WanderWithinRange(entity, wanderFocusDistance, wanderRadius, target, range);
 	}
 
 	private Vect3D getMaxForceToTarget() {
 		Vect3D toTargetForce = new Vect3D(target.getPos()).substract(movableEntity.getPos()).normalize(movableEntity.getMaxSteeringForce());
 		return toTargetForce;
 	}
 
 	@Override
 	public void render(int glMode) {
 		super.render(glMode);
 
 		if (Model.getModel().getUiContext().isDebugMode()) {
 			GL11.glTranslatef(target.getPos().x, target.getPos().y, target.getPos().z);
 			RenderUtils.renderCircle(range, 3, new Float[] { 0f, 0f, 0f, 0f }, new Float[] { 0f, 1f, 0f, 0.5f }, new Float[] { 0f, 0f, 0f, 0f });
 			GL11.glTranslatef(-target.getPos().x, -target.getPos().y, -target.getPos().z);
 		}
 	}
 
 	@Override
 	public void run(float secondsSinceLastUpdate) {
 		if (wanderRadius == 0) {
 			movableEntity.removeBehavior(this);
 			return;
 		}
 
 		final Vect3D pos = new Vect3D(movableEntity.getPos());
 
		wanderAngle += Math.random() * 4 - 2;
 
 		// if we are out of range, change the angle to take the ship back within range
 		// the farther we are out of range, the more we pull it back within range
 		// if we are twice as far as proper range, angle is full ahead to range center
 		Vect3D offsetToTarget = new Vect3D(target.getPos()).substract(movableEntity.getPos());
 		float distanceToTarget = offsetToTarget.modulus();
 		float minDist = range * (1 - delta);
 		float maxDist = range * (1 + delta);
 		float forcedAngle = Vect3D.NORTH.angleWith(offsetToTarget);
 		if (distanceToTarget > maxDist) {
 			wanderAngle = forcedAngle;
 		} else if (distanceToTarget > minDist) {
 			// TODO il faut adapter pour que le virage soit beaucoup plus progressif
			wanderAngle = wanderAngle * (maxDist - distanceToTarget) / (maxDist - minDist)
					+ forcedAngle * (distanceToTarget - minDist) / (maxDist - minDist);
 		}
 
 		Vect3D targetDirection = new Vect3D(new Vect3D(Vect3D.NORTH).normalize(wanderFocusDistance).rotate(movableEntity.getHeading()))
 				.add(new Vect3D(Vect3D.NORTH).normalize(wanderRadius).rotate(wanderAngle));
 
 		// TODO is it right to multiply by mass ?
 		// What are we multiplying by mass ?
 		getSteeringForce().copy(targetDirection).truncate(movableEntity.getMaxSteeringForce())
 				.mult(movableEntity.getMass());
 
 	}
 }
