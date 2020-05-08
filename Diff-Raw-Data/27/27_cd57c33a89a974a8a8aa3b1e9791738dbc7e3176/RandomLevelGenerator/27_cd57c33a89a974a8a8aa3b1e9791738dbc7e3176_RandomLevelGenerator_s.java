 package com.secondhand.model;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 import org.anddev.andengine.extension.physics.box2d.PhysicsWorld;
 
 import com.badlogic.gdx.math.Vector2;
 import com.secondhand.debug.MyDebug;
 import com.secondhand.math.PolygonUtil;
 import com.secondhand.model.powerup.RandomTeleport;
 import com.secondhand.model.powerup.Shield;
 import com.secondhand.physics.PhysicsAreaChecker;
 import com.secondhand.resource.PlanetType;
 import com.secondhand.util.RandomUtil;
 
 public class RandomLevelGenerator {
 
 	private final PhysicsWorld physicsWorld;
 	private final int levelNumber;
 	
 	public final Player player;
 	public final int levelWidth;
 	public final int levelHeight;
 	public final int playerMaxSize;
 	
 	
 	public final List<Entity> entityList;
 	public final List<Enemy> enemyList;
 	
 	
 	RandomLevelGenerator(final int levelNumber, final PhysicsWorld physicsWorld) {
 		this.physicsWorld = physicsWorld;
 		this.levelNumber = levelNumber;
 		
 		this.player = new Player(new Vector2(50, 50), 40, physicsWorld, 20);
 		
 		this.levelWidth = 2000 * levelNumber;
 		this.levelHeight = 2000 * levelNumber;
 	
 		this.playerMaxSize = 800 * levelNumber;
 		
 		this.entityList = new ArrayList<Entity>();
 		placeOutLevelEntities();
 		
 		this.enemyList = new ArrayList<Enemy>();
 		placeOutEnemies();
 	}
 	
 
 	private void placeOutEnemies() {
 		enemyList.add(new Enemy(new Vector2(800, 800), 50, physicsWorld, 10));
 
 		for (Enemy enemy : enemyList) {
 			entityList.add(enemy);
 		}
 
 	}
 
 	
 	// TODO could we create some more space between the planets?
	// so the don't group together at start
 	private void placeOutLevelEntities() {
 	
 		final float K = 1.2f;
 		
 		final int MINIMUM_PLAYER_EATABLE = 10;
 		int numPlayerEatable = 0;
 		
 		final float MAX_SIZE = 80f * this.levelNumber * K;
 		
 		final float MIN_SIZE = player.getRadius() - 10;
 		if(MIN_SIZE < 0) {
 			MyDebug.e("planet minimum size negative");
 		}
 		
 		final int PLANETS =(int)( 20 * this.levelNumber * K);
 		
 		// make sure they don't get too close to the edges.
 		final int HEIGHT = (int)(this.levelHeight - MAX_SIZE - 50);
 		final int WIDTH = (int)(this.levelWidth - MAX_SIZE - 50);
 		
 		final Random rng = new Random();
 
 		for (int i = 0; i < PLANETS; ++i) {
 			
 			float radius;
 			
 			// first ensure we place out some player eatable ones.
 			if(numPlayerEatable <= MINIMUM_PLAYER_EATABLE) {
 				
 				while(true) {
 					radius = RandomUtil.nextFloat(rng, MIN_SIZE, player.getRadius());
 					if(radius < player.getRadius())
 						break;
 				}
 				numPlayerEatable++;
 				
 			} else {
 				radius = RandomUtil.nextFloat(rng, MIN_SIZE, MAX_SIZE);
 			}
 			
 			float x;
 			float y;
 
 			while (true) {
 
 				x = rng.nextInt(WIDTH);
 				y = rng.nextInt(HEIGHT);
 
 				if (PhysicsAreaChecker.isRectangleAreaUnoccupied(new Vector2(x,
 						y), radius, radius, physicsWorld))
 					break;
 			}
 
 			entityList.add(new Planet(new Vector2(x, y), radius, RandomUtil
 					.randomEnum(rng, PlanetType.class), physicsWorld));
 		}
 		
 		entityList.add(new Obstacle(new Vector2(200, 200), PolygonUtil.getRandomPolygon() , physicsWorld));		
 		
 		entityList.add(new RandomTeleport(new Vector2(100, 500), physicsWorld));
 
 		entityList.add(new Shield(new Vector2(20, 500), physicsWorld));
 
 		entityList.add(new Shield(new Vector2(20, 700), physicsWorld));
 
 	}
 }
