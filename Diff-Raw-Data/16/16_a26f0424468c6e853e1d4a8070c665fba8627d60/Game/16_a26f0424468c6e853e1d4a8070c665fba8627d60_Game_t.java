 package com.avona.games.towerdefence;
 
 import java.io.Serializable;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Random;
 
 import com.avona.games.towerdefence.enemy.Enemy;
 import com.avona.games.towerdefence.enemyEventListeners.EnemyDeathGivesMoney;
 import com.avona.games.towerdefence.enemyEventListeners.EnemyDeathUpdatesGameStats;
 import com.avona.games.towerdefence.level.Level;
 import com.avona.games.towerdefence.level._010_Hello_World;
 import com.avona.games.towerdefence.level._020_About_Colors;
 import com.avona.games.towerdefence.particle.Particle;
 import com.avona.games.towerdefence.tower.Tower;
 
 public class Game implements Serializable {
 	private static final long serialVersionUID = 1L;
 
 	public List<Enemy> enemies = new LinkedList<Enemy>();
 	public List<Tower> towers = new LinkedList<Tower>();
 	public List<Particle> particles = new LinkedList<Particle>();
 
 	public TimeTrack gameTime = new TimeTrack();
 	public TimedCodeManager timedCodeManager = new TimedCodeManager();
 
 	public EventListener eventListener;
 
 	public Level level;
 
 	public int killed = 0;
 	public int lives;
 
 	public void looseLife() {
 		if (lives <= 0) {
 			return;
 		}
 		--lives;
 		if (lives == 0) {
 			gameOver();
 		}
 	}
 
 	public void gameOver() {
 		// FIXME add some game over logic here...
 		Util.log("you should die now...");
 	}
 
 	public int money;
 
 	/**
 	 * Which type of tower to build - if any.
 	 */
 	public Tower selectedBuildTower = null;
 
 	public boolean draggingTower = false;
 
 	public void loadLevel(Level l) {
 		this.level = l;
 
 		lives = level.getStartLives();
 		money = level.getStartMoney();
 		selectedBuildTower = level.buildableTowers[0];
 	}
 
 	/**
 	 * Currently selected, existing tower. We will typically show the properties
 	 * of that tower.
 	 */
 	public LocationObject selectedObject = null;
 
 	private EnemyDeathGivesMoney enemyDeathGivesMoney = new EnemyDeathGivesMoney(
 			this);
 	private EnemyDeathUpdatesGameStats enemyDeathUpdatesGameStats = new EnemyDeathUpdatesGameStats(
 			this);
 
 	public Game(EventListener eventListener) {
 		this.eventListener = eventListener;
 
 		/*
 		 * TODO We need some sort of fixed list of levels and a current level -> next level approach.
 		 * TODO Levels need to be completed / finished / done at some point, so that the next level can be selected.
 		 */
 		Level[] levels = new Level[] { new _010_Hello_World(this),
 				new _020_About_Colors(this) };
 		loadLevel(levels[0]);
 	}
 
 	public void startWave() {
 		level.waveTracker.startNextWave();
 	}
 
 	public boolean canBuildTowerAt(V2 location) {
		return selectedBuildTower != null
				&& money >= selectedBuildTower.price
				&& getTowerWithinRadius(location, selectedBuildTower.radius) == null;
 	}
 
 	public void addTowerAt(V2 location) {
 		Tower newTower = selectedBuildTower.copy();
 		newTower.location = new V2(location);
 		money -= newTower.price;
 		towers.add(newTower);
 		eventListener.onBuildTower(newTower);
 	}
 
 	static Random rand = new Random();
 
 	public void onEnemySpawned(Enemy e) {
 		e.eventListeners.add(enemyDeathGivesMoney);
 		e.eventListeners.add(enemyDeathUpdatesGameStats);
 		enemies.add(e);
 	}
 
 	public Tower getTowerWithinRadius(V2 location, float range) {
 		return (Tower) getLocationWithinRadius(towers, location, range);
 	}
 
 	public Enemy getEnemyWithinRadius(V2 location, float range) {
 		return (Enemy) getLocationWithinRadius(enemies, location, range);
 	}
 
 	@SuppressWarnings("unchecked")
 	public static Object getLocationWithinRadius(final List objects,
 			final V2 location, final float range) {
 		final List<LocationObject> locationObjects = (List<LocationObject>) objects;
 		for (final LocationObject lo : locationObjects) {
 			if (lo.collidesWith(location, range))
 				return lo;
 		}
 		return null;
 	}
 
 	public void pause() {
 		gameTime.stopClock();
 	}
 
 	public void unpause() {
 		gameTime.startClock();
 	}
 
 	public boolean isPaused() {
 		return !gameTime.isRunning();
 	}
 
 	public void updateWorld(final float dt) {
 		if (isPaused())
 			return;
 
 		gameTime.updateTick(dt);
 		timedCodeManager.update(gameTime.tick);
 
 		/**
 		 * Step all objects first. This will cause them to move.
 		 */
 		for (Enemy e : enemies) {
 			e.step(dt);
 		}
 		for (Tower t : towers) {
 			t.step(dt);
 		}
 
 		Iterator<Particle> piter = particles.iterator();
 		while (piter.hasNext()) {
 			final Particle p = piter.next();
 
 			// Advance particle.
 			p.step(dt);
 			if (p.isDead()) {
 				piter.remove();
 				continue;
 			}
 
 			// Collide particle with enemies.
 			p.collideWithEnemies(enemies, dt);
 			if (p.isDead()) {
 				piter.remove();
 				continue;
 			}
 		}
 
 		/**
 		 * Look for each tower, if it's ready to shoot and if an enemy ready to
 		 * shoot. If so, create a new particle. The tower is free to create any
 		 * particle in its shootTowards() method.
 		 */
 
 		for (Tower t : towers) {
 			Enemy enemy = t.findSuitableEnemy(enemies);
 			if (enemy != null) { // policy found some enemy
 				final Particle p = t.shootTowards(enemy);
 				if (p != null) {
 					particles.add(p);
 				}
 			}
 		}
 
 		/**
 		 * Handle enemy damage / deaths.
 		 */
 		Iterator<Enemy> eiter = enemies.iterator();
 		while (eiter.hasNext()) {
 			final Enemy e = eiter.next();
 			if (e.isDead()) {
 				eiter.remove();
 				continue;
 			}
 
 			final V2 w = level.waypoints[e.waypointId];
 			if (Collision.movingCircleCollidedWithCircle(e.location,
 					e.velocity, e.radius, w, V2.ZERO, 1, dt)) {
 				e.setWPID(e.waypointId + 1);
 				if (e.escaped) {
 					eiter.remove();
 					continue;
 				}
 			}
 		}
 	}
 }
