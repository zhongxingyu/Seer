 package se.chalmers.kangaroo.model;
 
 import se.chalmers.kangaroo.constants.Constants;
 
 /**
  * A class to represent the model of a platform game.
  * 
  * @author arvidk
  * 
  */
 public class GameModel {
 
 	/*
 	 * The kangaroo that the player controlls.
 	 */
 	private Kangaroo kangaroo;
 
 	/*
 	 * A variable to track collition with other creatures.
 	 */
 	private Creature creature;
 	/*
 	 * The current level that the player are playing.
 	 */
 	private GameMap map;
 	/*
 	 * The amount of times the player have died.
 	 */
 	private int deathCount;
 	/*
 	 * Will keep track of how long the player have played.
 	 */
 	private int time;
 	/*
 	 * Will check if the game is running. Not sure if we need it.
 	 */
 	private boolean isRunning = false;
 	/*
 	 * The gameMap. Makes it able to check collition
 	 */
 	private GameMap gameMap;
 	/*
 	 * Avariable to keep track of the kangaroos old position
 	 */
 	private Position oldPos;
 
 	/**
 	 * A method to start the game.
 	 */
 	// public void start() {
 	// isRunning = true;
 	// while (isRunning) {
 	// this.update();
 	// }
 	// }
 
 	/**
 	 * Will make it able to pause the game
 	 */
 	// public void pause() {
 	// if (isRunning = true) {
 	// isRunning = false;
 	// } else {
 	// this.start();
 	// }
 	// }
 
 	/**
 	 * A method to stop the game, and thereby quit it.
 	 */
 	// public void stop() {
 	// isRunning = false;
 	// }
 
 	public GameModel() {
 		gameMap = new GameMap("../maps/testmap.tmx");
 		kangaroo = new Kangaroo(new Position(10, 186));
 	}
 
 	/**
 	 * A method to update the game.
 	 */
 	public void update() {
 		oldPos = kangaroo.getPosition();
 		kangaroo.move();
 		checkCollition();
 	}
 
 	/**
 	 * Checks if a polygon collides with a tile or a creature.
 	 */
 	private void checkCollition() {
 		creatureCollition();
 		kangaroo.setFalling(tileCollition());
 	}
 
 	/**
 	 * Uses the polygon of the kangaroo and looks if it intersects with a
 	 * creature. If so, the kangaroo will either kill the creature or die.
 	 */
 	private void creatureCollition() {
 		int nbrOfCreatures = gameMap.getCreatureSize();
 		if (nbrOfCreatures != 0) {
 			for (int i = 0; i < nbrOfCreatures; i++) {
 				if (kangaroo.getPolygon().getBounds2D()
 						.intersects(creature.getPolygon().getBounds2D())) {
 					if (creature.isKillable()
 							&& kangaroo.getVerticalSpeed() > 0) {
 						creature.remove();
 					} else {
 						deathCount++;
 						restartLevel();
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * If the kangaroo collides with a tile, the kangaroo shall be moved to its
 	 * old position so it looks like it stops at the tile. If the kangaroo hits
 	 * the ground its vertical speed shall be resetted.
 	 */
 	private Boolean tileCollition() {
 		Position p = kangaroo.getPosition();
 		int x = p.getX() / Constants.TILE_SIZE;
 		int y = p.getY() / Constants.TILE_SIZE;
 
 		for (int i = 0; i < 2; i++) {
 			for (int j = 0; j < 3; j++) {
 				try{
					Tile tile = gameMap.getTile((x + i), (y + i));
 					System.out.println(kangaroo.getVerticalSpeed());
 					if (tile.isCollidable()
 							&& (kangaroo.getPolygon().getBounds2D()
 									.intersects(tile.getPolygon().getBounds2D()))) {
 						kangaroo.setVerticalSpeed(0f);
						Position pos = new Position(oldPos.getX(), p.getY());
						kangaroo.setPosition(pos);
 						
 						//Collides with ground
 						if (!(oldPos.getX() < (x + i) * Constants.TILE_SIZE == p
 								.getX() > (x + i) * Constants.TILE_SIZE)) {
 							kangaroo.setVerticalSpeed(0f);
 							Position pos2 = new Position(p.getX(), oldPos.getY());
 							kangaroo.setPosition(pos2);
 							return false;
 						}
 					}
 				} catch(ArrayIndexOutOfBoundsException e){
 					if(e.toString().endsWith("-1")){
 						kangaroo.setPosition(oldPos);
 					} else {
 						deathCount++;
 						restartLevel();
 					}
 				}
 			}	
 		}
 		return true;
 	}
 
 	/**
 	 * Restarts the level. Will be used when the kangaroo dies.
 	 */
 	private void restartLevel() {
 		// TODO implement restartLevel
 	}
 
 	/**
 	 * 
 	 * @return the amount of times the player has died.
 	 */
 	public int getDeathCount() {
 		return deathCount;
 	}
 
 	/**
 	 * 
 	 * @return the kangaroo
 	 */
 	public Kangaroo getKangaroo() {
 		return kangaroo;
 	}
 
 	/**
 	 * 
 	 * @return the gameMap
 	 */
 	public GameMap getGameMap() {
 		return gameMap;
 	}
 
 	/**
 	 * 
 	 * @return the time that has elapsed for the player.
 	 */
 	public int getTime() {
 		return time;
 	}
 
 }
