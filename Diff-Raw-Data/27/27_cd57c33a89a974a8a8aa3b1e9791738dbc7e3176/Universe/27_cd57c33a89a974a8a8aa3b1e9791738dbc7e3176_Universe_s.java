 package com.secondhand.model;
 
 import org.anddev.andengine.engine.Engine;
 
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.physics.box2d.Contact;
 import com.secondhand.debug.MyDebug;
 import com.secondhand.model.powerup.PowerUp;
 import com.secondhand.physics.PhysicsDestroyer;
 import com.secondhand.resource.Sounds;
 
 /**
  * Singelton class for describing the universe.
  */
 public final class Universe {
 	private Level currentLevel;
 
 	private PhysicsDestroyer physicsDestroyer;
 
 	private Engine engine;
 
 	// flag for when the game is over
 	// check by update by gamePlayScene
 	private boolean gameOver;
 
 	private static Universe instance;
 
 	private Universe() {
 	}
 
 	public void initialize(final Engine engine) {
 		this.engine = engine;
 		nextLevel();
 		physicsDestroyer = PhysicsDestroyer.getInstance();
 		physicsDestroyer.initialize(engine, currentLevel.getPhysicsWorld());
 		gameOver = false;
 	}
 
 	public static Universe getInstance() {
 		if (instance == null) {
 			instance = new Universe();
 		}
 		return instance;
 	}
 
 	private void handlePlanetBlackHoleCollision(final Entity entityA,
 			final Entity entityB) {
 
 		BlackHole blackHole;
 		Planet planet;
 		if (entityA instanceof BlackHole) {
 			blackHole = (BlackHole) entityA;
 			planet = (Planet) entityB;
 		} else {
 			planet = (Planet) entityA;
 			blackHole = (BlackHole) entityB;
 		}
 
 		if (blackHole.canEat(planet)) {
 			if (blackHole instanceof Player) {
 				Sounds.getInstance().growSound.play();
 			}
 			blackHole.eatEntity(planet);
 		}/* else {
 			if (blackHole instanceof Player) {
 				gameOver = true;
 			} else {
 				PhysicsDestroyer.getInstance().destroy(blackHole.getShape(),
 						true);
 			}
 		}*/
 
 	}
 
 	private void handlePowerUpPlayerCollision(final Entity entityA,
 			final Entity entityB) {
 
 		Sounds.getInstance().powerUpSound.play();
 
 		PowerUp powerUp;
 		if (entityA instanceof PowerUp) {
 			powerUp = (PowerUp) entityA;
 		} else {
 			powerUp = (PowerUp) entityB;
 		}
 
 		powerUp.getShape().detachSelf();
 		MyDebug.d("Now the powerup should dissappear");
 
 		physicsDestroyer.destroy(powerUp.getShape(), true);
 
 
 		currentLevel.getPlayer().addPowerUp(powerUp);
 
 	}
 
 	private void handleBlackHoleCollision(final Entity entityA, final Entity entityB) {
 		// TODO Auto-generated method stub
 		final BlackHole blackHole1 = (BlackHole) entityA;
 		final BlackHole blackHole2 = (BlackHole) entityB;
 
 		if (blackHole1.canEat(blackHole2)) {
 
 			if (blackHole1 instanceof Player) {
 				Sounds.getInstance().growSound.play();
 			} else {
 				gameOver = true;
 			}
 
 			blackHole1.eatEntity(blackHole2);
 
 		} else if (blackHole2.canEat(blackHole1)) {
 
 			if (blackHole2 instanceof Player) {
 				Sounds.getInstance().growSound.play();
 			} else {
 				gameOver = true;
 			}
 
 			blackHole2.eatEntity(blackHole1);
 		}
 	}
 
	// TODO Now there is alot of duplicated code in this method.
	// Perhaps we could extract that code into a new method.
 	public void checkCollision(final Contact contact) {
 		// if one or both is null, then we are dealing with a collision
 		// involving one or
 		// two non-entities
 		// (ie, a black hole collides with the wall),
 		// and we are not interested in handling such a collision
 		if (contact.getFixtureA().getBody().getUserData() == null
 				|| contact.getFixtureB().getBody().getUserData() == null) {
 			return;
 		}
 
 		// now we know both the bodies are entities.
 		final Entity entityA = (Entity) contact.getFixtureA().getBody()
 				.getUserData();
 		final Entity entityB = (Entity) contact.getFixtureB().getBody()
 				.getUserData();
 
 		if (entityA instanceof BlackHole && entityB instanceof Planet
 				|| entityB instanceof BlackHole && entityA instanceof Planet) {
 			handlePlanetBlackHoleCollision(entityA, entityB);
 		} else if (entityA instanceof Player && entityB instanceof PowerUp
 				|| entityB instanceof Player && entityA instanceof PowerUp) {
 			handlePowerUpPlayerCollision(entityA, entityB);
 		} else if (entityA instanceof Player && entityB instanceof Obstacle
 				|| entityB instanceof Player && entityA instanceof Obstacle) {
 			Sounds.getInstance().obstacleCollisionSound.play();
 		}/*
 		 * else if (entityA instanceof BlackHole && entityB instanceof
 		 * BlackHole) { handleBlackHoleCollision(entityA, entityB); }
 		 */
 
 	}
 
 	public boolean isGameOver() {
 		return gameOver;
 	}
 
 	// perhaps not needed if we only set gameover
 	private void gameOver() {
 		gameOver = true;
 		// gameOver flag that gameplayScene checks each update
 		// other gameOver stuff in this method
 
 	}
 
 	public Level getLevel() {
 		return currentLevel;
 	}
 
 	// TODO how to decide what to have on each successive level?
 
 	// If after gameOver a new game should start, this method
 	// could be called. Also the case where the player starts
 	// a new game when there already is one saved could be
 	// handled here. In both cases there already is a Universe
 	// so everything having to do with new level could be handled here.
 
 	public void nextLevel() {
 		if (currentLevel == null) {
 			currentLevel = new Level(2);
 		} else if (!gameOver) {
 			currentLevel = new Level(currentLevel.getLevelNumber());
 		} else {
 			gameOver = false;
 			currentLevel = new Level();
 		}
 	}
 
 	public void update(final Vector2 v) {
 
 		if (currentLevel.checkPlayerBigEnough()) {
 
 			nextLevel();
 
 		} else {
 
 			currentLevel.sendTouchInput(v);
 		}
 	}
 }
