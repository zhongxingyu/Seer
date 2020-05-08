 package com.secondhand.model;
 
 import org.anddev.andengine.extension.physics.box2d.PhysicsWorld;
 import org.anddev.andengine.extension.physics.box2d.util.constants.PhysicsConstants;
 
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.physics.box2d.CircleShape;
 import com.secondhand.opengl.Circle;
 import com.secondhand.physics.PhysicsDestroyer;
 
 public abstract class BlackHole extends CircleEntity {
 
 	// only a 1/5 of the masses of the eaten bodies is used in the growth
 	private static final float GROWTH_FACTOR = 0.2f;
 	
 	private float maxSpeed;
 	
 
 	public BlackHole(final Vector2 position, final float radius,
 			final PhysicsWorld physicsWorld, final boolean updateRotation,
 			final float maxSpeed) {
 		// TODO load texture instead of creating Circle
 		super(new Circle(position.x, position.y, radius), true, physicsWorld,
 				updateRotation);
 		this.maxSpeed = maxSpeed;
 	}
 
 	public float getMaxSpeed() {
 		return maxSpeed;
 	}
 	
 	public void setMaxSpeed(final float maxSpeed) {
 		this.maxSpeed = maxSpeed;
 	}
 
 	private void increaseSize(final float increase) {
 		setRadius(getRadius() + increase);
 	}
 
 	/**
 	 * If sizes are equal then false is returned.
 	 */
 	public boolean canEat(final Entity entity) {
 		if (!entity.isEdible()) {
 			return false;
 		}
 		return this.getArea() > entity.getArea();
 	}
 	
 	// moves in the specified direction. If max speed is reached, then no movement is performed.
 	public void move(final Vector2 direction) {
 		final Vector2 testVector = new Vector2(this.getBody()
 				.getLinearVelocity());
 		if (testVector.add(direction).len() > this.getMaxSpeed()){
 			// Check if new velocity doesn't exceed maxSpeed!
 			return;
 		}
 
 		this.getBody().applyLinearImpulse(direction,
 				this.getBody().getWorldCenter());	
 	}
 
 	public void eatEntity(final Entity entity) {
 
 		// Detach the shape from AndEngine-rendering
 		entity.getShape().detachSelf();
 
 		// if you eat an planet you get 10 points, else you get 20.
 		// TODO: Maybe we should have different scores for different size
 		// this we will do in entity class, every entity has a score.
 		if (this instanceof Player) {
 			if (entity instanceof Planet)
 				Universe.getInstance().getLevel().getPlayer().increaseScore(10);
 			else
 				Universe.getInstance().getLevel().getPlayer().increaseScore(20);
 		}
 
 		// remove the eaten entity from the physics world:
 		PhysicsDestroyer.getInstance().destroy(entity.getShape(), true);
 
 		// TODO: we should use general entities instead, but for debugging
 		// purposes we'll do this cast.
 		// we'll fix it later.
 		final Planet planet = (Planet) entity;
 
 		// increase the size of the rendered circle.
 		this.increaseSize(planet.getRadius() * GROWTH_FACTOR);
 
 		// now the must also increase the size of the circle physics body
 
 		final CircleShape shape = (CircleShape) getBody().getFixtureList()
 				.get(0).getShape();
 
 		shape.setRadius(getRadius()
 				/ PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT);
 
 		final Vector2 currentPosition = shape.getPosition();
 		final float inc = (planet.getRadius())
 				/ PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT;
 		final Vector2 newPosition = currentPosition.add(inc, inc);
 
 		shape.setPosition(newPosition);
 
 		// this.getBody().setTransform(newPosition, 0);
 
 		/*
 		 * Universe.getInstance().getPhysicsDestroyer().destroy(this.getShape(),
 		 * true);
 		 * 
 		 * final Body newBody = super.createNewCircleBody(this.circle,
 		 * this.physicsWorld);
 		 * 
 		 * // TODO: this won't remove the update handler. Fix that as well.
 		 * super.registerBody(newBody);
 		 */
 
 	}
 }
