 package com.secondhand.model;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 import com.badlogic.gdx.math.Vector2;
 import com.secondhand.debug.MyDebug;
 import com.secondhand.model.powerup.PowerUp;
 import com.secondhand.model.powerup.PowerUpFactory;
 import com.secondhand.model.resource.PlanetType;
 import com.secondhand.model.sat.PolygonFactory;
 import com.secondhand.model.sat.World;
 import com.secondhand.model.util.PolygonUtil;
 import com.secondhand.model.util.RandomUtil;
 
 // TODO: this generation of levels seems to be a bit slow, so we probably need a level loading screen.	
 public class RandomLevelGenerator {
 
 	private final int levelNumber;
 
 	public final Player player;
 	public final int levelWidth;
 	public final int levelHeight;
 	public final int playerMaxSize;
 
 	private float xAxis;
 	private float yAxis;
 	public final List<Entity> entityList;
 	public final List<Enemy> enemyList;
 
 	private final GameWorld level;
 
 	final World world;
 	private final Random rng;
 
 	RandomLevelGenerator(final Player player, final GameWorld level) {
 
 		rng = new Random();
 
 		this.levelNumber = level.getLevelNumber()-1;
 		this.level = level;
 
 		this.levelWidth = level.getLevelWidth();
 		this.levelHeight = level.getLevelHeight();
 
 		world = new World(this.levelWidth, this.levelHeight);
 
 		this.player = player;
 
 		// make sure entities are not placed on top of player
 		final World.Polygon poly = PolygonFactory.createCircle(new Vector2(
 				player.getCenterX(), player.getCenterY()), player.getRadius());
 		world.addToWorld(poly);
 
 		this.playerMaxSize = player.getMaxSize();
 		this.enemyList = new ArrayList<Enemy>();
 		this.entityList = new ArrayList<Entity>();
 		// to make it easier to place out the entities.
 		placeOutLevelEntities();
 
 	}
 
 	private void placeOutEnemies() {
 		
		Enemy enemy = new Enemy(new Vector2(200, 200), 50, level);
 		enemy.setMaxSpeed(8+(this.levelNumber-1)*2);
 		entityList.add(enemy);
 		enemyList.add(enemy);
 
 		
 
 		final int ENEMIES;
 	//	Enemy enemy;
 		//Instead of making to many enemies we will make them go faster.
 		if (levelNumber < 4) {
 			ENEMIES = 2 * (this.levelNumber);
 		} else {
 			ENEMIES = 8;
 		}
 
 		for (int i = 0; i < ENEMIES; ++i) {
 
 			float radius;
 
 			radius = RandomUtil.nextFloat(rng, Enemy.getMinSize(),
 					Enemy.getMaxSize());
 
 			while (true) {
 
 				xAxis = rng.nextInt(this.levelWidth);
 				yAxis = rng.nextInt(this.levelHeight);
 
 				final World.Polygon poly = PolygonFactory.createCircle(
 						new Vector2(xAxis, yAxis), radius);
 
 				// addToWorld add the polygon if it is unoccupied otherwise it
 				// returns false
 				if (world.addToWorld(poly)) {
 					break;
 				}
 
 			}
 
 			enemy = new Enemy(new float[]{xAxis, yAxis}, radius, level);
 			enemy.setMaxSpeed(8+(this.levelNumber-1)*2);
 			entityList.add(enemy);
 			enemyList.add(enemy);
 
 		}
 
 	}
 
 	/*
 	 * private boolean isTooCloseToOtherEntity(final float xAxis, final float y,
 	 * final float radius) { // setting high values for this constant will cause
 	 * long level generation times, so be careful. final float MINIMUM_DISTANCE
 	 * = 60;
 	 * 
 	 * for(final Entity entity: this.entityList) { if(entity instanceof
 	 * CircleEntity) { final Circle other = (Circle)entity.getShape();
 	 * 
 	 * final float dx = Math.abs(x - other.getX()); final float dy = Math.abs(y
 	 * - other.getY());
 	 * 
 	 * final float dist = (float)Math.sqrt(dx*dx + dy*dy) - radius -
 	 * other.getRadius();
 	 * 
 	 * if(dist < MINIMUM_DISTANCE) { return true; } } }
 	 * 
 	 * return false; }
 	 */
 
 	private void placeOutObstacles() {
 
 		final int OBSTACLES = this.levelNumber * 5;
 
 		for (int i = 0; i < OBSTACLES; ++i) {
 
 			final List<Vector2> edges = PolygonUtil.getRandomPolygon();
 
 			while (true) {
 
 				xAxis = rng.nextInt(this.levelWidth);
 				yAxis = rng.nextInt(this.levelHeight);
 
 				final World.Polygon poly = new World.Polygon(new Vector2(xAxis,
 						yAxis), edges);
 
 				if (world.addToWorld(poly)) {
 					break;
 				}
 			}
 
 			entityList
 			.add(new Obstacle(new Vector2(xAxis, yAxis), edges, level));
 		}
 	}
 
 	private void placeOutPowerUps() {
 		final int POWER_UPS = 5 * this.levelNumber;
 
 		for (int i = 0; i < POWER_UPS; ++i) {
 
 			while (true) {
 
 				xAxis = rng.nextInt(this.levelWidth);
 				yAxis = rng.nextInt(this.levelHeight);
 
 				final World.Polygon poly = PolygonFactory.createRectangle(
 						new Vector2(xAxis, yAxis), PowerUp.WIDTH,
 						PowerUp.HEIGHT);
 
 				if (world.addToWorld(poly)) {
 					break;
 				}
 			}
 
 			entityList.add(PowerUpFactory.getRandomPowerUp(new Vector2(xAxis,
 					yAxis), level, rng));
 		}
 	}
 
 	private void placeOutPlanets() {
 		MyDebug.d("The level is = " + levelNumber);
 		final float K = 1.2f;
 		final int MINIMUM_PLAYER_EATABLE;
 		if (this.levelNumber < 10) {
 			MINIMUM_PLAYER_EATABLE = 50 - (this.levelNumber) * 3;
 		} else {
 			MINIMUM_PLAYER_EATABLE = 10;
 		}
 		MyDebug.d(" eatable = " + MINIMUM_PLAYER_EATABLE);
 		int numPlayerEatable = 0;
 
 		final float MAX_SIZE = player.getRadius()*4f;
 
 		final float MIN_SIZE = player.getRadius() - 20;
 
 		final int PLANETS = 50; // (int)( 25 * this.levelNumber * K);
 		float radius;
 
 		MyDebug.d("There are " + entityList.size() + " entitys out there before adding planets");
 		//Start with the smaller planets.
 		for (int i = 0; i < MINIMUM_PLAYER_EATABLE; ++i) {
 
 			while (true) {
 				radius = RandomUtil.nextFloat(rng, MIN_SIZE,
 						player.getRadius());
 
 				xAxis = rng.nextInt(this.levelWidth);
 				yAxis = rng.nextInt(this.levelHeight);
 
 				final World.Polygon poly = PolygonFactory.createCircle(
 						new Vector2(xAxis, yAxis), radius);
 
 				if (world.addToWorld(poly)&& radius<player.getRadius()) {
 					numPlayerEatable++;
 					break;
 				}
 			}
 			entityList.add(new Planet(new Vector2(xAxis, yAxis), radius,
 					RandomUtil.randomEnum(rng, PlanetType.class), level));
 		}
 		MyDebug.d("there are " + entityList.size() + " entitys out there after adding eatable planets");
 		for (int i = MINIMUM_PLAYER_EATABLE ; i<PLANETS ; i++ ) {
 			while (true) {
 				radius = RandomUtil.nextFloat(rng, player.getRadius(),
 						MAX_SIZE);
 
 				xAxis = rng.nextInt(this.levelWidth);
 				yAxis = rng.nextInt(this.levelHeight);
 
 				final World.Polygon poly = PolygonFactory.createCircle(
 						new Vector2(xAxis, yAxis), radius);
 
 				if (world.addToWorld(poly)) {
 					break;
 				}
 
 			}
 			entityList.add(new Planet(new Vector2(xAxis, yAxis), radius,
 					RandomUtil.randomEnum(rng, PlanetType.class), level));
 		}
 	}
 
 	// private int check
 	private void placeOutLevelEntities() {
 
 		placeOutObstacles();
 		MyDebug.d("done placing out obstacles");
 		placeOutPlanets();
 		MyDebug.d("done placing out planets");
 		placeOutPowerUps();
 		MyDebug.d("done placing out power ups");
 		placeOutEnemies();
 		MyDebug.d("done placing out enemies");
 
 	}
 }
