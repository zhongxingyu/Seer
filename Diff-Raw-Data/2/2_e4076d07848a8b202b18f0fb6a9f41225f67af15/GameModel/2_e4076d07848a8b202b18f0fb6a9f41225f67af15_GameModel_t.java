 package se.chalmers.kangaroo.model;
 
 import java.awt.*;
 import se.chalmers.kangaroo.constants.*;
 
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
 	 * Will check if the game is running.
 	 */
 	private boolean isRunning = false;
 	
 	private GameMap gameMap;
 	
 	private Tile tile;
 	
 	
 	/**
 	 * A method to start the game.
 	 */
 //	public void start() {
 //		isRunning = true;
 //		while (isRunning) {
 //			this.update();
 //		}
 //	}
 
 	/**
 	 * Will make it able to pause the game
 	 */
 //	public void pause() {
 //		if (isRunning = true) {
 //			isRunning = false;
 //		} else {
 //			this.start();
 //		}
 //	}
 
 	/**
 	 * A method to stop the game, and thereby quit it.
 	 */
 //	public void stop() {
 //		isRunning = false;
 //	}
 
 	/**
 	 * A method to update the game.
 	 */
 	public void update() {
 		// TODO implement update
 	}
 
 	/**
 	 * Checks if a polygon collides with a tile
 	 */
 	private void checkCollition() {
 		if(kangaroo.getPolygon().getBounds2D().intersects(creature.getPolygon().getBounds2D())) {
 			if(kangaroo.getVerticalSpeed() > 0 && creature.isKillable()){
 				creature.remove();
 			} else {
 				deathCount++;
 				restartLevel();
 			}
 		}
		if(tile.isCollidable() &&  kangaroo.getPolygon().getBounds2D().intersects(tile.getPolygon().getBounds2D())) {
 			
 		}
 	}
 	
 	
 	
 	/**
 	 * Restarts the level.
 	 * Will be used when the kangaroo dies.
 	 */
 	private void restartLevel(){
 		//TODO implement restartLevel
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
