 package com.secondhand.model;
 
 import java.util.List;
 
 import com.secondhand.debug.MyDebug;
 import com.secondhand.model.powerup.PowerUp;
 
 public class Enemy extends BlackHole {
 
 	private static float enemyMaxSpeed = 10;
 	private static final float MAX_SIZE = 40;
 	private static final float MIN_SIZE = 20;
 
 	private float huntingArea;
 
 	@Override
 	public void createType() {
 		huntingArea = getHuntingArea();
 	}
 	
 	public Enemy(final Vector2 vector, final float radius, final GameWorld level) {
 		super(vector, radius, level, enemyMaxSpeed);
 		
 		//physics.setLinearDamping(1.2f);
 		
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
 
 	@Override
 	public void setMaxSpeed(final float maxSpeed) {
 		enemyMaxSpeed = maxSpeed;
 	}
 
 	// player has highest chase-priority
 	public void moveEnemy(final Entity player, final List<Entity> entityList) {
 		if (isCloseToEntity(player) && canEat(player)) {
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
 
 			if (e.getClass() != PowerUp.class && isCloseToEntity(e)
 					&& canEat(e)) {
 				entity = getSmaller(entity, e);
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
 				// MyDebug.d("Enemy: applyMovement towards " +
 				// entity.getClass());
 				// closeToDanger();
 				move(new Vector2(getCenterX() - entity.getCenterX(),
 						getCenterY() - entity.getCenterY()));
 
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
 	private void closeToDanger() {
 
 	}
 
 	private void stopMovement() {
 		//TODO better to make a method in physics for this
 		pcs.firePropertyChange("stopMovement", 0, 0);
 		//this.getBody().setLinearVelocity(new Vector2());
 		//this.getBody().setAngularVelocity(0);
 
 	}
 
 	public void retreat(final Entity danger) {
 		MyDebug.d("Enemy: Retreat");

 	}
 	
 	@Override
 	protected void handlePowerUp(final PowerUp powerUp) {
 		// enemies can't eat powerups :(
 	}
 
 	@Override
 	protected void wasEaten() {
 		this.gameWorld.getEntityManager().removeEnemyFromList(this);
 		super.wasEaten();
 	}
 }
