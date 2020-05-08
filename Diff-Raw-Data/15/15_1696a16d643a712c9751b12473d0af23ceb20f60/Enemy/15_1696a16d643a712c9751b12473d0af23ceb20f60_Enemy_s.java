 package com.secondhand.model.entity;
 
 import java.util.List;
 
 import com.secondhand.model.physics.Vector2;
 
 public class Enemy extends BlackHole {
 
 	private static final float ENEMY_MAX_SPEED = 2;
 	private static final float MAX_SIZE = 40;
 	private static final float MIN_SIZE = 20;
 
 	private float huntingArea;
 	
 	private float maxSpeed;
 
 	@Override
 	public void onPhysicsAssigned() {
 		huntingArea = getHuntingArea();
 	}
 
 	public Enemy(final Vector2 vector, final float radius, final GameWorld level) {
 		super(vector, radius, level);
 		huntingArea = getHuntingArea();
 		this.maxSpeed = ENEMY_MAX_SPEED;
 	}
 
 	public float getHuntingArea() {
 		return getRadius() * getRadius() * (float) Math.PI * 40;
 	}
 
 	public static float getMaxSize() {
 		return MAX_SIZE;
 	}
 
 	public static float getMinSize() {
 		return MIN_SIZE;
 	}
 
 	public void setMaxSpeed(final float maxSpeed) {
 		this.maxSpeed = maxSpeed;
 	}
 	
 	public float getMaxSpeed() {
 		return maxSpeed;
 	}
 
 	// player has highest chase-priority
 	public void moveEnemy(final Entity player, final List<Entity> entityList) {
 		if (isCloseToEntity(player) && canEat(player)) {
 			// MyDebug.d("enemy" + getRadius()+ " " + player.getRadius());
 			dangerCheck(player);
 		} else {
 			dangerCheck(getHighesPriority(entityList));
 		}
 
 	}
 
 	// checks if enemy is close enough to start chasing
 	// the entity
 	private boolean isCloseToEntity(final Entity entity) {
 
 		final float dx = entity.getCenterX() - this.getCenterX();
 
 		final float dy = entity.getCenterY() - this.getCenterY();
 
 		return dx * dx + dy * dy <= huntingArea;
 
 	}
 
 	// chase after smallest entity first
 	// could create many get-something and make different
 	// enemies behave different ( getClosest... )
 	private Entity getHighesPriority(final List<Entity> entityList) {
 		Entity entity = null;
 		for (final Entity e : entityList) {
 			if (!(e instanceof IPowerUp)) {
 				if (isCloseToEntity(e) && canEat(e)) {
 					entity = getSmaller(entity, e);
 				}
 			}
 		}
 		return entity;
 	}
 
 	private Entity getSmaller(final Entity entity, final Entity e) {
 		if (entity == null || e.getRadius() < entity.getRadius()) {
 			return e;
 		} else {
 			return entity;
 		}
 	}
 
 	private void dangerCheck(final Entity entity) {
 		// TODO change the null-check to something nicer
 		if (entity != null) {
 
 			if (this.gameWorld.getPhysics().isStraightLine(entity, this)) {
 				// closeToDanger();
 				physics.applyImpulse(
 						new Vector2(entity.getCenterX() - getCenterX(), entity
 								.getCenterY() - getCenterY()).mul(0.002f),
 						getMaxSpeed());
 			}
 			if (huntingArea != getHuntingArea()) {
 				huntingArea = getHuntingArea();
 			}
 		} else {
 			huntingArea *= 2;
 		}
 	}
 
 	// checks if there is something uneatable close by
 	// in movement direction
 	/*private void closeToDanger() {
 
 	}*/
 /*
 	private void stopMovement() {
 		physics.stopMovment();
 	}*/
 
 	public void retreat(final Entity danger) {
 	}
 
 	@Override
 	protected void handlePowerUp(final IPowerUp powerUp) {
 		// enemies can't eat powerups :(
 	}
 
 	@Override
 	protected void wasEaten() {
 		this.gameWorld.getEntityManager().removeEnemyFromList(this);
 		super.wasEaten();
 	}
 }
