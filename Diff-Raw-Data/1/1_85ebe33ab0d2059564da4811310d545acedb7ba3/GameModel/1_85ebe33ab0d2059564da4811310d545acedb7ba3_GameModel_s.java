 package se.chalmers.kangaroo.model;
 
 import java.awt.geom.Rectangle2D;
 
 import se.chalmers.kangaroo.constants.Constants;
 import se.chalmers.kangaroo.model.creatures.Creature;
 import se.chalmers.kangaroo.model.kangaroo.Item;
 import se.chalmers.kangaroo.model.kangaroo.Kangaroo;
 import se.chalmers.kangaroo.model.utils.Position;
 import se.chalmers.kangaroo.utils.GameTimer;
 import se.chalmers.kangaroo.utils.Sound2;
 
 /**
  * A class to represent the model of a platform game.
  * 
  * @author arvidk
  * @modifiedby simonal
  * 
  */
 public class GameModel{
 
 	/*
 	 * The kangaroo that the player controlls.
 	 */
 	private Kangaroo kangaroo;
 	/*
 	 * The amount of times the player have died.
 	 */
 	private int deathCount;
 	/*
 	 * Will keep track of how long the player have played.
 	 */
 	private GameTimer timer;
 	/*
 	 * The gameMap. Makes it able to check collition
 	 */
 	private GameMap gameMap;
 	/*
 	 * Avariable to keep track of the kangaroos old position
 	 */
 	private Position oldPos;
 
 	private int currentLevel;
 
 	private boolean levelFinished;
 
 	private boolean gameFinished;
 	
 	private Sound2 s;
 
 	public GameModel() {
 		super();
 		levelFinished = false;
 		gameFinished = false;
 		currentLevel = 0;
 		gameMap = new GameMap("resources/maps/level" + currentLevel + ".tmx");
 		kangaroo = new Kangaroo(new Position(10, 186));
 		
 		s = Sound2.getInstance();
 	}
 
 	/**
 	 * Starts the model. Will start the timer.
 	 */
 	public void start() {
 		timer = new GameTimer();
 		timer.start();
 	}
 
 	/**
 	 * A method to update the game.
 	 */
 	public void update() {
 		oldPos = kangaroo.getPosition();
 		kangaroo.move();
 		updateCreatures();
 		checkCollition();
 		if (gameMap.getTileWidth() - kangaroo.getPosition().getX()
 				/ Constants.TILE_SIZE < 3)
 			changeLevel();
 	}
 
 	/* Private method for updating all the creatures. */
 	private void updateCreatures() {
 		for (int i = 0; i < gameMap.getCreatureSize(); i++) {
 			Creature c = gameMap.getCreatureAt(i);
 			Rectangle2D cRect = c.getPolygon().getBounds2D();
 			if (!(gameMap.getTile((int) (cRect.getMinX() / 32),
 					(int) (cRect.getMinY() / 32) + 1).isCollidable())
 					|| !(gameMap.getTile((int) (cRect.getMaxX() / 32),
 							(int) (cRect.getMinY() / 32) + 1).isCollidable())
 					|| (gameMap.getTile((int) (cRect.getMinX() / 32),
 							(int) (cRect.getMinY() / 32))).isCollidable()
 					|| gameMap.getTile((int) (cRect.getMaxX() / 32),
 							(int) (cRect.getMinY() / 32)).isCollidable()) {
 				c.changeDirection();
 			}
 			c.updateCreature();
 
 		}
 
 	}
 
 	/*
 	 * Checks if a polygon collides with a tile or a creature.
 	 */
 	private void checkCollition() {
 		try {
 			itemCollition();
 			tileCollition();
 			iObjectCollition();
 			creatureCollition();
 			checkFalling();
 
 		} catch (ArrayIndexOutOfBoundsException e) {
 			s.playSfx("death");
 			restartLevel();
 		}
 	}
 
 	/* Check collition with interactive objects and calls for their action */
 	private void iObjectCollition() {
 		int x = kangaroo.getPosition().getX() / Constants.TILE_SIZE;
 		int y = kangaroo.getPosition().getY() / Constants.TILE_SIZE;
 		for (int i = x; i < x + 2; i++)
 			for (int j = y; j < y + 3; j++)
 				if (gameMap.getIObjectAt(i, j) != null)
 					gameMap.getIObjectAt(i, j).onCollision();
 	}
 
 	/*
 	 * Uses the polygon of the kangaroo and looks if it intersects with a
 	 * creature. If so, the kangaroo will either kill the creature or die.
 	 */
 	private void creatureCollition() {
 		for (int i = 0; i < gameMap.getCreatureSize(); i++) {
 			Creature creature = gameMap.getCreatureAt(i);
 			if (kangaroo.getPolygon().getBounds2D()
 					.intersects(creature.getPolygon().getBounds2D())) {
 				if (creature.isKillable() && kangaroo.getVerticalSpeed() > 0) {
 					s.playSfx("creaturedeath");
 					gameMap.killCreature(creature);
 					kangaroo.setVerticalSpeed(-6.5f);
 				} else {
 					s.playSfx("death");
 					restartLevel();
 				}
 
 			}
 		}
 	}
 
 	/*
 	 * If the kangaroo collides with a tile, the kangaroo shall be moved to its
 	 * old position so it looks like it stops at the tile. If the kangaroo hits
 	 * the ground its vertical speed shall be resetted.
 	 */
 	private void tileCollition() {
 		int oldX = oldPos.getX() / Constants.TILE_SIZE;
 		int oldY = oldPos.getY() / Constants.TILE_SIZE;
 		int x = kangaroo.getPosition().getX() / Constants.TILE_SIZE;
 		int y = kangaroo.getPosition().getY() / Constants.TILE_SIZE;
 		for (int i = x; i < x + 2; i++) {
 			for (int j = y; j < y + 3; j++) {
 				Tile tile = gameMap.getTile(i, j);
 
 				if (tile.isCollidable()) {
 					int tileMaxY = (int) tile.getPolygon().getBounds2D()
 							.getMaxY();
 					int tileMinY = (int) tile.getPolygon().getBounds2D()
 							.getMinY();
 					int kangMinY = (int) kangaroo.getPolygon().getBounds2D()
 							.getMinY();
 					if (x != oldX && Math.abs(tileMaxY - kangMinY) > 2) {
 						kangaroo.setPosition(new Position(oldPos.getX(),
 								kangMinY));
 
 					} else if (oldX == x
 							&& kangMinY < tileMaxY
 							&& kangaroo.getVerticalSpeed() < 0
 							&& kangMinY > tileMinY
 							&& (kangaroo.getPolygon().getBounds2D().getMaxX() > tile
 									.getPolygon().getBounds2D().getMinX() == kangaroo
 									.getPolygon().getBounds2D().getMinX() < tile
 									.getPolygon().getBounds2D().getMaxX())) {
 						kangaroo.setPosition(new Position(kangaroo
 								.getPosition().getX(), tileMaxY - 1));
 						kangaroo.setVerticalSpeed(0f);
 						kangaroo.setFalling(true);
 					}
 					if (oldY != y
 							&& tileMaxY > kangaroo.getPolygon().getBounds2D()
 									.getMaxY()
 							&& kangaroo.getVerticalSpeed() > kangaroo
 									.getPolygon().getBounds2D().getMaxY()
 									- tileMinY
 							&& (kangaroo.getPolygon().getBounds2D().getMaxX() > tile
 									.getPolygon().getBounds2D().getMinX() == kangaroo
 									.getPolygon().getBounds2D().getMinX() < tile
 									.getPolygon().getBounds2D().getMaxX())) {
 						kangaroo.setPosition(new Position(kangaroo
 								.getPosition().getX(), tileMinY - 66));
 						kangaroo.setVerticalSpeed(0f);
 						kangaroo.setFalling(false);
 					}
 
 				}
 			}
 		}
 	}
 
 	/*
 	 * Checks collition with the items and calls them if they collide with the
 	 * kangaroo
 	 */
 	private void itemCollition() {
 		int x = kangaroo.getPosition().getX() / Constants.TILE_SIZE;
 		int y = kangaroo.getPosition().getY() / Constants.TILE_SIZE;
 		for (int i = x; i < x + 2; i++)
 			for (int j = y; j < y + 3; j++) {
 				Item item = gameMap.getItemAt(i, j);
 				if (item != null)
 					kangaroo.setItem(item);
 			}
 	}
 
 	/*
 	 * Checks if there is something collideable under the kangaroo or if it
 	 * shuold start falling
 	 */
 	private void checkFalling() {
 		Rectangle2D kangBounds = kangaroo.getPolygon().getBounds2D();
 		if ((!gameMap.getTile((int) (kangBounds.getMaxX() / 32),
 				(int) (kangBounds.getMaxY() / 32) + 1).isCollidable())
 				&& !gameMap.getTile((int) (kangBounds.getMinX() / 32),
 						(int) (kangBounds.getMaxY() / 32) + 1).isCollidable()) {
 			kangaroo.setFalling(true);
 		}
 
 	}
 
 	/**
 	 * Restarts the level. Should be used when the kangaroo dies or wants to
 	 * respawn.
 	 */
 	public void restartLevel() {
 		deathCount++;
 		kangaroo.reset();
 		gameMap.resetItems();
 		gameMap.resetCreatures();
 		kangaroo.setPosition(new Position(10, 186));
 	}
 
 	/* When one level is finished this method should be invoked. */
 	private void changeLevel() {
 		// setHighScore(currentLevel, time);
 		levelFinished = true;
 		currentLevel++;
 		if (currentLevel == Constants.NUMBER_OF_LEVELS)
 			gameFinished = true;
 		// end of tmp
 	}
 
 	/**
 	 * Will return true when the current level has been completed.
 	 * 
 	 * @return whether the level is finished or not
 	 */
 	public boolean isLevelFinished() {
 		return levelFinished;
 	}
 
 	/**
 	 * Will return true when all the levels has been completed and the game is
 	 * over.
 	 * 
 	 * @return whether the game is finished or not
 	 */
 	public boolean isGameFinished() {
 		return gameFinished;
 	}
 	/**
 	 * Starts the next level.
 	 */
 	public void nextLevel() {
 		levelFinished = false;
 		gameMap = new GameMap("resources/maps/level" + currentLevel + ".tmx");
 		restartLevel();
 		deathCount = 0;
 		timer = new GameTimer();
 		timer.start();
 	}
 
 	/**
 	 * A getter for the current deathcount.
 	 * 
 	 * @return the amount of times the player has died.
 	 */
 	public int getDeathCount() {
 		return deathCount;
 	}
 
 	/**
 	 * A getter for the kangaroo.
 	 * 
 	 * @return the kangaroo
 	 */
 	public Kangaroo getKangaroo() {
 		return kangaroo;
 	}
 
 	/**
 	 * A getter for the gamemap
 	 * 
 	 * @return the gameMap
 	 */
 	public GameMap getGameMap() {
 		return gameMap;
 	}
 
 	/**
 	 * A getter for the time.
 	 * 
 	 * @return the time that has elapsed for the player.
 	 */
 	public double getTime() {
 		return timer.getElapsedTime();
 	}
 
 }
