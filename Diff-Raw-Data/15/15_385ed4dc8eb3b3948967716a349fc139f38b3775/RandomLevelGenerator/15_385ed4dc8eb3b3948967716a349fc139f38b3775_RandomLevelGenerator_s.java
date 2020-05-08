 package com.secondhand.model.randomlevelgenerator;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 import com.secondhand.model.entity.Enemy;
 import com.secondhand.model.entity.Entity;
 import com.secondhand.model.entity.GameWorld;
 import com.secondhand.model.entity.IGameWorld;
 import com.secondhand.model.entity.Obstacle;
 import com.secondhand.model.entity.Planet;
 import com.secondhand.model.entity.Player;
 import com.secondhand.model.physics.Vector2;
 import com.secondhand.model.powerup.PowerUp;
 import com.secondhand.model.powerup.PowerUpFactory;
 import com.secondhand.model.randomlevelgenerator.sat.Circle;
 import com.secondhand.model.randomlevelgenerator.sat.Polygon;
 import com.secondhand.model.randomlevelgenerator.sat.PolygonFactory;
 import com.secondhand.model.randomlevelgenerator.sat.World;
 import com.secondhand.model.resource.PlanetType;
 
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
 
 	private final IGameWorld level;
 
 	private final World world;
 	private final Random rng;
 	private static final float PLAYER_RADIUS = 30.0f;
 
 
 	private final PowerUpFactory powerUpFactory;
 	
 	public RandomLevelGenerator(final Player player, final IGameWorld level) {
 		rng = new Random();
 		this.levelNumber = level.getLevelNumber();
 		this.level = level;
 		this.levelWidth = 2000 * this.levelNumber;
 		this.levelHeight = 2000 * this.levelNumber;
 		world = new World(this.levelWidth, this.levelHeight);
 		this.player = player;
 		// make sure entities are not placed on top of player
 
 		powerUpFactory = new PowerUpFactory();
 		
 		final Circle circle = new Circle(new Vector2(
 				player.getInitialPosition().x, player.getInitialPosition().y),
 				40);
 		world.addToWorld(circle);
 
 		this.playerMaxSize = GameWorld.PLAYER_STARTING_SIZE
 				* (this.levelNumber + 1);
 
 		this.enemyList = new ArrayList<Enemy>();
 		this.entityList = new ArrayList<Entity>();
 		// to make it easier to place out the entities.
 		placeOutLevelEntities();
 	}
 
 	private void placeOutEnemies(final int ENEMIES, final int EATABLE_ENEMIES) {
 		Enemy enemy;
 
 		for (int i = 0; i < EATABLE_ENEMIES; ++i) {
 
 			float radius;
 			while (true) {
 				
 				radius = RandomUtil
 						.nextFloat(rng, Enemy.getMinSize(), this.PLAYER_RADIUS);
 
 				xAxis = rng.nextInt(this.levelWidth);
 				yAxis = rng.nextInt(this.levelHeight);
 
 				final Circle circle = new Circle(new Vector2(xAxis, yAxis),
 						radius);
 
 				if (world.addToWorld(circle)
 						&& radius < 40 * PLAYER_RADIUS) {
 					break;
 				}
 			}
 			enemy = new Enemy(new Vector2(xAxis, yAxis), radius);
 			enemy.setMaxSpeed(this.levelNumber*2);
 			entityList.add(enemy);
 			enemyList.add(enemy);
 		}
 		for (int i = EATABLE_ENEMIES; i < ENEMIES; ++i) {
 
 			float radius;
 			radius = RandomUtil.nextFloat(rng, Enemy.getMinSize(),
 					Enemy.getMaxSize());
 
 			while (true) {
 				xAxis = rng.nextInt(this.levelWidth);
 				yAxis = rng.nextInt(this.levelHeight);
 				final Circle circle = new Circle(new Vector2(xAxis, yAxis),
 						radius);
 
 				// addToWorld add the polygon if it is unoccupied otherwise it
 				// returns false
 				if (world.addToWorld(circle)) {
 					break;
 				}
 
 			}
 			enemy = new Enemy(new Vector2(xAxis, yAxis), radius);
 			enemy.setMaxSpeed(2 + (this.levelNumber - 1) * 2);
 			entityList.add(enemy);
 			enemyList.add(enemy);
 
 		}
 
 	}
 
 	private void placeOutObstacles(final int OBSTACLES) {
 
 		for (int i = 0; i < OBSTACLES; ++i) {
 
 			final List<Vector2> edges = PolygonUtil.getRandomPolygon();
 
 			while (true) {
 
 				xAxis = rng.nextInt(this.levelWidth);
 				yAxis = rng.nextInt(this.levelHeight);
 
 				final Polygon poly = new Polygon(new Vector2(xAxis, yAxis),
 						edges);
 
 				if (world.addToWorld(poly)) {
 					break;
 				}
 			}
 
 			entityList
 			.add(new Obstacle(new Vector2(xAxis, yAxis), edges));
 		}
 	}
 
 	private void placeOutPowerUps(final int number) {
 		for (int i = 0; i < number; ++i) {
 
 			while (true) {
 
 				xAxis = rng.nextInt(this.levelWidth);
 				yAxis = rng.nextInt(this.levelHeight);
 
 				final Polygon poly = PolygonFactory.createRectangle(
 						new Vector2(xAxis, yAxis), PowerUp.WIDTH,
 						PowerUp.HEIGHT);
 
 				if (world.addToWorld(poly)) {
 					break;
 				}
 			}
 
 			entityList.add(powerUpFactory.getRandomPowerUp(new Vector2(xAxis,
 					yAxis), level, rng,level.getPlayer()));
 		}
 	}
 
 	private void placeOutPlanets(final int NUMBER_EATABLE, final int NUMBER,
 			final float MIN_SIZE, final float MAX_SIZE) {
 		final float K = 1.2f;
 
 		final int PLANETS = (int) (25 * this.levelNumber * K);
 		float radius;
 
 		int planetCounter = 0;
 
 		// Start with the smaller planets.
 		for (int i = 0; i < NUMBER_EATABLE; ++i, planetCounter++) {
 
 			while (true) {
 				radius = RandomUtil
 						.nextFloat(rng, MIN_SIZE, PLAYER_RADIUS);
 
 				xAxis = rng.nextInt(this.levelWidth);
 				yAxis = rng.nextInt(this.levelHeight);
 
 				final Circle circle = new Circle(new Vector2(xAxis, yAxis),
 						radius);
 
 				if (world.addToWorld(circle)
 						&& radius < 40 * PLAYER_RADIUS) {
 					break;
 				}
 			}
 			entityList.add(new Planet(new Vector2(xAxis, yAxis), radius,
 					RandomUtil.randomEnum(rng, PlanetType.class)));
 		}
 		for (int i = NUMBER_EATABLE; i < PLANETS; i++, planetCounter++) {
 
 			while (true) {
 				radius = RandomUtil.nextFloat(rng, MIN_SIZE, MAX_SIZE);
 
 				xAxis = rng.nextInt(this.levelWidth);
 				yAxis = rng.nextInt(this.levelHeight);
 
 				final Circle circle = new Circle(new Vector2(xAxis, yAxis),
 						radius);
 
 				if (world.addToWorld(circle)) {
 					break;
 				}
 
 			}
 			entityList.add(new Planet(new Vector2(xAxis, yAxis), radius,
 					RandomUtil.randomEnum(rng, PlanetType.class)));
 		}
 	}
 
 	private void placeOutLevelEntities() {
 
 		placeOutObstacles(newObstacles());
 
 		final int[] numPlanets= newPlanets();
 		placeOutPlanets(numPlanets[0], numPlanets[1], (PLAYER_RADIUS / 1.1f),
 				PLAYER_RADIUS * 10);
 
 		placeOutPowerUps(10);
 
		final int[] numEnemies= newEnemies();
 		placeOutEnemies(numEnemies[0],numEnemies[1]);
 
 	}
 
 	// checks witch level and placing out the Obstacles after difficulty.
 	private int newObstacles() {
 
 		if (levelNumber == 1) {
 			return 0;
 		} else if (levelNumber == 2){
 			return 5;
 		} else {
 			return 10;
 		}
 	}
 
 	// checks witch level and placing out the Planets after difficulty.
 	private int[] newPlanets() {
 		int[] list = new int[2];
 		if (levelNumber == 1) {
 			list[0]=30;
 			list[1]=40;
 		} else if (levelNumber == 2) {
 			list[0]=24;
 			list[1]=34;
 		} else if (levelNumber == 3){
 			list[0]=20;
 			list[1]=30;
 		} else {
 			list[0]=5;
 			list[1]=20;
 		}
 		return list;
 	}
 
 	// checks witch level and placing out the Planets after difficulty.
 	private int[] newEnemies() {
 		final int[] ENEMIES = new int[2];
 		if (levelNumber < 4) {
 			ENEMIES[0] = 2 + 6 * (this.levelNumber);
 			ENEMIES[1] = 5 * this.levelNumber;
 		} else {
 			ENEMIES[0] = 60;
 			ENEMIES[1] = 55;
 		}
 		return ENEMIES;
 	}
 }
