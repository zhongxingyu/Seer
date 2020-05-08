 
 package com.secondhand.model;
 
import com.secondhand.model.Vector2;
 import com.secondhand.model.powerup.PowerUp;
 
 public abstract class BlackHole extends CircleEntity {
 
 	// only a 1/5 of the masses of the eaten bodies is used in the growth
 	private static final float GROWTH_FACTOR = 0.2f;
 
 	private float maxSpeed;
 
 	private boolean canEatInedibles;
 
 	private Vector2 position;
 
 	//I put this in BlackHole, so enemy black holes will also have scores
 	// but placing it here made coding the eating logic much more convenient.
 	// but we can simply ignore the score for enemy black holes.
 	private  int score;
 
 	public BlackHole(final Vector2 position, final float radius,
 			final GameWorld level,
 			final float maxSpeed, final int startingScore) {
 		super(position, radius, true, level);
 		this.position = position;
 		this.maxSpeed = maxSpeed;
 		this.score = startingScore;
 		this.canEatInedibles = false;
 	}
 
 	public BlackHole(final Vector2 position, final float radius,
 			final GameWorld level,
 			final float maxSpeed) {
 		this(position, radius, level, maxSpeed, 0);
 	}
 
 	public float getPosX(){
 		return position.y;
 	}
 
 	public float getPosY(){
 		return position.y;
 	}
 
 	public boolean canEatInedibles() {
 		return this.canEatInedibles;
 	}
 
 	public void setCanEatInedibles(final boolean canEatInedibles) {
 		this.canEatInedibles = canEatInedibles;
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
 		if (!entity.isEdible() && !this.canEatInedibles()) {
 			return false;
 		}
 		return this.getRadius() > entity.getRadius();
 	}
 
 	protected abstract void handlePowerUp(final PowerUp powerUp);
 
 	protected void onGrow() {
 	}
 
 	protected void entityWasTooBigToEat(final Entity entity) {}
 
 
 	public void eatEntityUtil(final Entity entity) {
 		// for now we won't handle black holes eating other black holes.	
 		/*if(entity instanceof BlackHole)
 			return;*/
 
 
 		if(!this.canEat(entity)) {
 			entityWasTooBigToEat(entity);
 			return;	
 		}
 
 		this.increaseScore(entity.getScoreValue());
 
 		// increase the size of the rendered circle.
 		final float radiusInc = entity.getRadius() * GROWTH_FACTOR;
 		this.increaseSize(radiusInc);
 		onGrow();
 
 
 		entity.wasEaten();	
 	}
 
 	public void eatEntity(final Entity entity) {
 
 		if(entity instanceof PowerUp) {
 			// custom handling
 			handlePowerUp((PowerUp)entity);
 		} else if(entity instanceof BlackHole) {
 			final BlackHole otherBlackHole = (BlackHole)entity;
 
 			// instead of eating, you will be eaten!
 			if(otherBlackHole.canEat(this))
 				otherBlackHole.eatEntity(this);	
 			else
 				this.eatEntityUtil(otherBlackHole);
 		} else {
 			eatEntityUtil(entity);
 		}
 
 
 	}
 
 	@Override
 	public float getScoreWorth() {
 		return 3;
 	}	
 
 	public void setRadius(final float radius) {
		this.pcs.firePropertyChange("radius", 0, this.getRadius());
 
 	}	
 }
