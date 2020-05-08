 	
 package com.secondhand.model;
 
 import org.anddev.andengine.extension.physics.box2d.PhysicsWorld;
 import org.anddev.andengine.extension.physics.box2d.util.constants.PhysicsConstants;
 
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.physics.box2d.CircleShape;
 import com.secondhand.model.powerup.PowerUp;
 import com.secondhand.opengl.Circle;
import com.secondhand.physics.PhysicsDestroyer;
 
 public abstract class BlackHole extends CircleEntity {
 
 	// only a 1/5 of the masses of the eaten bodies is used in the growth
 	private static final float GROWTH_FACTOR = 0.2f;
 	
 	private float maxSpeed;
 	
 	private  int score;
 
 
 	public BlackHole(final Vector2 position, final float radius,
 			final PhysicsWorld physicsWorld, final boolean updateRotation,
 			final float maxSpeed) {
 		// TODO load texture instead of creating Circle
 		super(new Circle(position.x, position.y, radius), true, physicsWorld,
 				updateRotation, FixtureDefs.BLACK_HOLE_FIXTURE_DEF);
 		this.maxSpeed = maxSpeed;
 		this.score = 0;
 		
 	}
 	
 	public int getScore() {
 		return score;
 	}
 
 	public void increaseScore(final int increase) {
 		score += increase;
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
 		return this.getRadius() > entity.getRadius();
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
 
 	protected abstract void handlePowerUp(final PowerUp powerUp);
 	
 	protected void onGrow() {
 		
 	}
 	
 	protected void entityWasTooBigToEat(final Entity entity) {}
 	
 	public void eatEntity(final Entity entity) {
 		
 		if(entity instanceof PowerUp) {
 			// custom handling
 			handlePowerUp((PowerUp)entity);
 		} else if(entity instanceof BlackHole) {
 				final BlackHole otherBlackHole = (BlackHole)entity;
 				
 				// instead of eating, you will be eaten!
 				if(otherBlackHole.canEat(entity))
 					otherBlackHole.eatEntity(this);	
 		} else {
 			
 			// for now we won't handle black holes eating other black holes.	
 			if(entity instanceof BlackHole)
 				return;
 			
 			if(!this.canEat(entity)) {
 				entityWasTooBigToEat(entity);
 				return;	
 			}
 			
 				
 			this.increaseScore(entity.getScoreValue());
 
 			// increase the size of the rendered circle.
 			final float radiusInc = entity.getRadius() * GROWTH_FACTOR;
 			this.increaseSize(radiusInc);
 			onGrow();
 
 			// now we must also increase the size of the circle physics body
 			final CircleShape shape = (CircleShape) getBody().getFixtureList()
 					.get(0).getShape();
 			shape.setRadius(this.getRadius()
 					/ PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT);
 
 			
 			entity.wasEaten();	
 		}
 
 
 	}
 
 }
