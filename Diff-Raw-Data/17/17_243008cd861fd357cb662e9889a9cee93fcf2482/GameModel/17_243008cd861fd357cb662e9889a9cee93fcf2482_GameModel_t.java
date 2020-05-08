 package se.chalmers.kangaroo.model;
 
 import java.awt.geom.Rectangle2D;
 
 import se.chalmers.kangaroo.constants.Constants;
 
 /**
  * A class to represent the model of a platform game.
  * 
  * @author arvidk
  * @modifiedby simonal
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
 	
 	private int jGlobal;
 
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
 		gameMap = new GameMap("../maps/betamap.tmx");
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
		//creatureCollition();
 		tileCollition();
 		changeFalling();
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
 	private void tileCollition() {
 		int oldX = oldPos.getX()/Constants.TILE_SIZE;
 		int oldY = oldPos.getY()/Constants.TILE_SIZE;
 		int x = kangaroo.getPosition().getX()/Constants.TILE_SIZE;
 		int y = kangaroo.getPosition().getY()/Constants.TILE_SIZE;
 		for(int i=x; i<x+2; i++) {
 			for(int j=y; j<y+3; j++) {
 				Tile tile = gameMap.getTile(i,j);
 				
 				if(tile.isCollidable()){
 					int tileMaxY = (int)tile.getPolygon().getBounds2D().getMaxY();
 					int tileMinY = (int)tile.getPolygon().getBounds2D().getMinY();
 					int kangMinY = (int)kangaroo.getPolygon().getBounds2D().getMinY();
 					int kangMaxY = (int)kangaroo.getPolygon().getBounds2D().getMaxY();
 					if(oldX != x && Math.abs(tileMaxY - kangMinY) > 5){
 						kangaroo.setPosition(new Position(oldPos.getX(), kangaroo.getPosition().getY()));
 					}else if(kangMinY < tileMaxY && kangaroo.getVerticalSpeed() < 0 && kangMinY > tileMinY){
 						kangaroo.setPosition(new Position(kangaroo.getPosition().getX(), tileMaxY));
 						kangaroo.setVerticalSpeed(0f);
 						kangaroo.setFalling(true);
 					}
 					if(oldY != y && tileMaxY > kangaroo.getPolygon().getBounds2D().getMaxY()&& kangaroo.getVerticalSpeed() > kangaroo.getPolygon().getBounds2D().getMaxY() - tileMinY){
 						kangaroo.setPosition(new Position(kangaroo.getPosition().getX(), tileMinY-66));
 						kangaroo.setVerticalSpeed(0f);
 						kangaroo.setFalling(false);
 					}
 		
 				}
 			}
 		}
 	}
 	
 	private void changeFalling() {
 		Rectangle2D kangBounds = kangaroo.getPolygon().getBounds2D();
 		if(!gameMap.getTile((int)(kangBounds.getMaxX()/32),(int)(kangBounds.getMaxY()/32)+1).isCollidable()) {
 			kangaroo.setFalling(true);
 		}
 		
 		
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
