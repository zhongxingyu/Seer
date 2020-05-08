 package com.secondhand.model;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 import com.badlogic.gdx.math.Vector2;
 import com.secondhand.debug.MyDebug;
 import com.secondhand.math.PolygonUtil;
 import com.secondhand.model.powerup.PowerUp;
 import com.secondhand.model.powerup.PowerUpFactory;
 import com.secondhand.model.sat.PolygonFactory;
 import com.secondhand.model.sat.World;
 import com.secondhand.util.RandomUtil;
 
 // TODO: this generation of levels seems to be a bit slow, so we probably need a level loading screen.	
 public class RandomLevelGenerator {
 
 	private final int levelNumber;
 	
 	public final Player player;
 	public final int levelWidth;
 	public final int levelHeight;
 	public final int playerMaxSize;
 	
 	public final List<Entity> entityList;
 	public final List<Enemy> enemyList;
 	
 	private final GameWorld level;
 	
 	final World world;
 	private final Random rng;
 	
 	RandomLevelGenerator(final Player player, final GameWorld level) {
 	
 		rng = new Random();
 		
 		this.levelNumber = level.getLevelNumber();
 		this.level = level;
 		
 		this.levelWidth = 1700 * levelNumber;
 		this.levelHeight = 1700 * levelNumber;
 		
 		world = new World(this.levelWidth, this.levelHeight);
 
 	
 		this.player= player;
 
 		// make sure entities are not placed on top of player
 		final World.Polygon poly = PolygonFactory.createCircle(new Vector2(player.getCenterX(),player.getCenterY()), 
 				player.getRadius());
 		world.addToWorld(poly);
 	
 		this.playerMaxSize = 40 * levelNumber;
 		
 		
 		this.entityList = new ArrayList<Entity>();
 		// to make it easier to place out the entities.
 		placeOutLevelEntities();
 		
 		this.enemyList = new ArrayList<Enemy>();
 		placeOutEnemies();
 		MyDebug.d("done placing out enemies");
 		
 		
 	}
 	
 
 	private void placeOutEnemies() {		
 		final float MAX_SIZE = 50;
 		final float MIN_SIZE = 20;
 		final int ENEMIES = 4 * this.levelNumber;
 		
 		for (int i = 0; i < ENEMIES; ++i) {
 			
 			float radius;
 			
 			 
 			radius = RandomUtil.nextFloat(rng, MIN_SIZE, MAX_SIZE);
 			
 			float x;
 			float y;
 			
 
 			while(true) {
 				x = rng.nextInt(this.levelWidth);
 				y = rng.nextInt(this.levelHeight);
 
 				final World.Polygon poly = PolygonFactory.createCircle(new Vector2(x,y), radius);
 
 
 				if(world.isUnoccupied(poly)) {
 					world.addToWorld(poly);
 					break;
 				} else {
 					//MyDebug.d("needs to reposition!");
 				}
 			}
 			
 			final Enemy enemy = new Enemy(new Vector2(x, y), radius, level);
 			entityList.add(enemy);		
			entityList.add(enemy);
 
 		}
 
 	}
 /*
 	private boolean isTooCloseToOtherEntity(final float x, final float y, final float radius) {
 		// setting high values for this constant will cause long level generation times, so be careful.
 		final float MINIMUM_DISTANCE = 60;
 		
 		for(final Entity entity: this.entityList) {
 			if(entity instanceof CircleEntity) {
 				final Circle other = (Circle)entity.getShape();
 				
 				final float dx = Math.abs(x - other.getX());
 				final float dy = Math.abs(y - other.getY());
 				
 				final float dist = (float)Math.sqrt(dx*dx + dy*dy) - radius - other.getRadius();
 				
 				if(dist < MINIMUM_DISTANCE) {
 					return true;
 				}
 			}
 		}
 		
 		return false;
 	}*/
 	
 	private void placeOutObstacles() {
 
 		final int OBSTACLES = this.levelNumber * 7;
 		
 		for (int i = 0; i < OBSTACLES; ++i) {
 			
 			final List<Vector2> edges =  PolygonUtil.getRandomPolygon();
 			
 			float x;
 			float y;
 
 			while (true) {
 
 				x = rng.nextInt(this.levelWidth);
 				y = rng.nextInt(this.levelHeight);
 				
 				final World.Polygon poly = new World.Polygon(new Vector2(x,y), edges);
 
 				if(world.isUnoccupied(poly)) {
 					world.addToWorld(poly);
 					break;
 				} else {
 					//MyDebug.d("needs to reposition!");
 				}
 			}
 
 			entityList.add(new Obstacle(new Vector2(x, y), edges , level));		
 		}
 	}
 	
 	private void placeOutPowerUps() {
 
 		final int POWER_UPS = 5 * this.levelNumber;
 		
 		for (int i = 0; i < POWER_UPS; ++i) {
 			
 			
 			float x;
 			float y;
 
 			while (true) {
 
 				x = rng.nextInt(this.levelWidth);
 				y = rng.nextInt(this.levelHeight);
 				
 				final World.Polygon poly = PolygonFactory.createRectangle(new Vector2(x,y), PowerUp.WIDTH, PowerUp.HEIGHT);
 
 				if(world.isUnoccupied(poly)) {
 					world.addToWorld(poly);
 					break;
 				} else {
 					//MyDebug.d("needs to reposition!");
 				}
 			}
 
 			entityList.add(PowerUpFactory.getRandomPowerUp(new Vector2(x,y), level, rng));		
 		}
 	}
 	
 	private void placeOutPlanets() {
 
 		final float K = 1.2f;
 		
 		final int MINIMUM_PLAYER_EATABLE = 10; //this.levelNumber * 4;
 		int numPlayerEatable = 0;
 		
 		final float MAX_SIZE = 80f * this.levelNumber * K;
 		
 		final float MIN_SIZE = player.getRadius() - 10;
 		if(MIN_SIZE < 0) {
 			MyDebug.e("planet minimum size negative");
 		}
 		
 		final int PLANETS = 10; //(int)( 25 * this.levelNumber * K);
 		
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
 			
 
 			while(true) {
 				x = rng.nextInt(this.levelWidth);
 				y = rng.nextInt(this.levelHeight);
 
 				final World.Polygon poly = PolygonFactory.createCircle(new Vector2(x,y), radius);
 
 
 				if(world.isUnoccupied(poly)) {
 					world.addToWorld(poly);
 					break;
 				} else {
 					//MyDebug.d("needs to reposition!");
 				}
 
 		
 			}
 			entityList.add(new Planet(new Vector2(x, y), radius, RandomUtil.randomEnum(rng, PlanetType.class), level));		
 
 		}
 	}
 	
 	private void placeOutLevelEntities() {
 	
 		placeOutObstacles();
 		MyDebug.d("done placing out obstacles");
 		placeOutPlanets();
 		MyDebug.d("done placing out planets");
 		
 		placeOutPowerUps();
 		MyDebug.d("done placing out power ups");
 			
 	}
 }
