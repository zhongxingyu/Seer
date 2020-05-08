 package com.secondhand.view.physics;
 
 import org.anddev.andengine.extension.physics.box2d.PhysicsWorld;
 
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.physics.box2d.Contact;
 import com.secondhand.model.CollisionResolver;
 import com.secondhand.model.Enemy;
 import com.secondhand.model.Entity;
 import com.secondhand.model.GameWorld;
 import com.secondhand.model.IPhysicsWorld;
 
 public class MyPhysicsWorld implements IPhysicsWorld {
 	private PhysicsWorld physicsWorld;
 	// TODO only enemy needs the util class
 	private final PhysicsEnemyUtil enemyUtil;
 	private CollisionResolver collisionResolver;
 	private final PhysicsWorldBounds bounds;
 	private GameWorld gameWorld;
 
	public MyPhysicsWorld(final PhysicsWorld physicsWorld){
 
 		// TODO remove physicsWorld from constructor
 		// and put worldBoundries somewhere else
 		this.physicsWorld = new PhysicsWorld(new Vector2(), true);
 		bounds = new PhysicsWorldBounds(physicsWorld);
 		enemyUtil = new PhysicsEnemyUtil(physicsWorld);
 		this.physicsWorld.setVelocityIterations(16);
 		this.physicsWorld.setPositionIterations(16);
 
 	}
 
 	public PhysicsWorld getPhysicsWorld() {
 		return this.physicsWorld;
 	}
 
 	public void setPhysicsWorld(final PhysicsWorld physicsWorld) {
 		this.physicsWorld = physicsWorld;
 	}
 
 	// put some invisible, static rectangles that keep the player within the
 	// world bounds:
 	// we do not do this using registerEntity, because these bodies are
 	// static.
 	@Override
 	public void setWorldBounds(final int levelWidth, final int levelHeight) {
 		bounds.setWorldBounds(levelWidth, levelHeight);
 	}
 
 	@Override
 	public void removeWorldBounds() {
 		bounds.removeBounds();
 	}
 
 	
 	@Override
 	public void checkCollision(final Contact contact) {
 		collisionResolver.checkCollision(contact.getFixtureA().getBody()
 				.getUserData(), contact.getFixtureB().getBody().getUserData());
 	}
 
 	@Override
 	public boolean isStraightLine(final Entity entity, final Enemy enemy) {
 		return enemyUtil.straightLine(entity, enemy);
 	}
 
 	public boolean isAreaUnOccupied(final float x, final float y, final float r) {
 		return PhysicsAreaChecker.isRectangleAreaUnoccupied(new Vector2(x - r,
 				y + r), new Vector2(x + r, y - r), physicsWorld);
 	}
 
 	@Override
 	public void setContactListener() {
 		physicsWorld.setContactListener(new CollisionContactListener(this));
 
 	}
 	@Override
 	public void setGameWorld(final GameWorld gameWorld) {
 		this.gameWorld = gameWorld;
 		this.collisionResolver = new CollisionResolver(gameWorld);
 
 	}
 
 
 }
