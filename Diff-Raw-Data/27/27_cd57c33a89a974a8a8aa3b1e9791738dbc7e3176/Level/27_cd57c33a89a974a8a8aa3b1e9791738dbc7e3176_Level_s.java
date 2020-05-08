 package com.secondhand.model;
 
 import java.util.List;
 
 import org.anddev.andengine.entity.primitive.Rectangle;
 import org.anddev.andengine.entity.shape.Shape;
 import org.anddev.andengine.extension.physics.box2d.PhysicsFactory;
 import org.anddev.andengine.extension.physics.box2d.PhysicsWorld;
 
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
 import com.badlogic.gdx.physics.box2d.FixtureDef;
 
 public class Level {
 
 	private List<Entity> entityList;
 	private List<Enemy> enemyList;
 	private int playerMaxSize;
 	private PhysicsWorld physicsWorld;
 
 	private Player player;
 
 	private int levelWidth;
 	private int levelHeight;
 
 	private int levelNumber;
 
 	public Level() {
 		this(1);
 	}
 	
 	public Level(final int levelNumber) {
		this.levelNumber = levelNumber;
 		prepareLevel();
 	}
 
 	public void prepareLevel() {
 
 		this.physicsWorld  = new PhysicsWorld(new Vector2(), true);
 		
 		RandomLevelGenerator randomLevelGenerator = new RandomLevelGenerator(this.levelNumber, this.physicsWorld);
 		
 		this.player = randomLevelGenerator.player;
 		this.playerMaxSize = randomLevelGenerator.playerMaxSize;
 		this.levelWidth = randomLevelGenerator.levelWidth;
 		this.levelHeight = randomLevelGenerator.levelHeight;
 		
 		this.entityList = randomLevelGenerator.entityList;
 		this.enemyList = randomLevelGenerator.enemyList;
 		
 		setupWorldBounds();
 	}
 	
 	
 	public int getPlayerMaxSize() {
 		return playerMaxSize;
 	}
 
 	public int getLevelNumber() {
 		return levelNumber;
 	}
 
 	public List<Entity> getEntityList() {
 		return entityList;
 	}
 
 	public PhysicsWorld getPhysicsWorld() {
 		return physicsWorld;
 	}
 
 	public final void setupWorldBounds() {
 
 		final Shape[] worldBounds = new Shape[4];
 
 		// put some invisible, static rectangles that keep the player within the
 		// world bounds:
 		// we do not do this using registerEntity, because these bodies are
 		// static.
 
 		worldBounds[0] = new Rectangle(0, levelHeight - 2, levelWidth, 2);
 		worldBounds[1] = new Rectangle(0, 0, levelWidth, 2);
 		worldBounds[2] = new Rectangle(0, 0, 2, levelHeight);
 		worldBounds[3] = new Rectangle(levelWidth - 2, 0, 2, levelHeight);
 		final FixtureDef wallFixtureDef = PhysicsFactory.createFixtureDef(0,
 				0.5f, 0.5f);
 		PhysicsFactory.createBoxBody(this.physicsWorld, worldBounds[0],
 				BodyType.StaticBody, wallFixtureDef);
 		PhysicsFactory.createBoxBody(this.physicsWorld, worldBounds[1],
 				BodyType.StaticBody, wallFixtureDef);
 		PhysicsFactory.createBoxBody(this.physicsWorld, worldBounds[2],
 				BodyType.StaticBody, wallFixtureDef);
 		PhysicsFactory.createBoxBody(this.physicsWorld, worldBounds[3],
 				BodyType.StaticBody, wallFixtureDef);
 
 	}
 
 	public Player getPlayer() {
 		return player;
 	}
 
 	public int getLevelWidth() {
 		return levelWidth;
 	}
 
 	public int getLevelHeight() {
 		return levelHeight;
 	}
 
 	private void moveEnemies() {
 		// enemies are in both lists because we want them
 		// for easy access and for the posibility of attacking
		// each other. it would be preferable to change it later
 		// if we can come up with a better way
 		
 		for (Enemy enemy : enemyList) {
 			enemy.moveEnemy(player, entityList);
 		}
 	}
 	
 	public void onManagedUpdate(final float pSecondsElapsed){ 
 		moveEnemies();
 		
 		this.player.moveToNeededPositionIfNecessary();
 		
 		// move player if necessary. 
 	}
 	
 
 	public void sendTouchInput(final Vector2 v) {
 		this.player.reachToTouch(v);
 	}
 
 	public boolean checkPlayerBigEnough() {
 		return player.getRadius() >= playerMaxSize;
 
 	}
 }
