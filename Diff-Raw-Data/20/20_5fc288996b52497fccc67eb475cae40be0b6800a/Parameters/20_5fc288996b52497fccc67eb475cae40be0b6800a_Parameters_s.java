 package com.github.joakimpersson.tda367.model.constants;
 
 import java.awt.Dimension;
 
 /**
  * 
  * @author joakimpersson
  * 
  */
 public enum Parameters {
 	INSTANCE;
 
 	/**
 	 * Get the time it takes for a bomb to exploded after it has been placed on
 	 * the gamefiled
 	 * 
 	 * @return The time it takes for a bomb to explode in milliseconds
 	 */
 	public int getBombDetonationTime() {
 		return 1000;
 	}
 
 	/**
 	 * Get the starting health for a player
 	 * 
 	 * @return A players init health
 	 */
 	public int getInitHealth() {
 		return 3;
 	}
 
 	/**
 	 * Get the number of bombs the player starts with every game
 	 * 
 	 * @return The number of bombs a player starts with
 	 */
 	public int getStartingBombs() {
 		return 1;
 	}
 
 	/**
 	 * Get the starting speed for a player every match
 	 * 
 	 * @return A players starting speed
 	 */
 	public int getInitSpeed() {
 		return 1;
 	}
 
 	/**
 	 * Get the starting bombrange value for a players bomb every match
 	 * 
 	 * @return Bombs starting range
 	 */
 	public int getInitBombRange() {
 		return 2;
 	}
 
 	/**
 	 * Get the starting power values for a players bomb every match
 	 * 
 	 * @return Bombs init power
 	 */
 	public int getInitBombPower() {
 		return 1;
 	}
 
 	public int getInitBombType() {
 		return 0;
 	}
 
 	/**
 	 * Get the probability for when a box is destroyed by fire that a
 	 * powerupitem will appear
 	 * 
 	 * @return The probability that a powerupitem will appear
 	 */
 	public double getPowerUpProbabilityBox() {
 		return 0.4;
 	}
 
 	/**
 	 * Get the probability for when a pillar is destroyed by fire that a
 	 * powerupitem will appear
 	 * 
 	 * @return The probability that a powerupitem will appear
 	 */
 	public double getPowerUpProbabilityPillar() {
 		return 0.15;
 	}
 
 	/**
 	 * Get the time in milliseconds that the fire will remain on the gamefield
 	 * after the bomb has exploded
 	 * 
 	 * @return The time the fire will remain on the gamefield in milliseconds
 	 */
 	public int getFireDuration() {
 		return 1500;
 	}
 
 	/**
 	 * This method returns the size of one step inside a tile, preferably
 	 * smaller than 0.2. The step size affects how fast the game must be
 	 * updated.
 	 */
	public double getPlayerStepSize() {
 		return 0.105;
 	}
 
 	/**
 	 * Get the GameMap Dimension
 	 * 
 	 * @return A dimension representing the size of the gamemap
 	 */
 	public Dimension getMapSize() {
 		return new Dimension(15, 13);
 	}
 
 	/**
 	 * Get the initial volume for the game's sound-effects.
 	 * 
 	 * @return The initial volume for the game's sound-effects.
 	 */
 	public float getInitSFXVolume() {
 		return 1;
 	}
 
 	/**
 	 * Get the initial volume for the game's background-music.
 	 * 
 	 * @return The initial volume for the game's background-music.
 	 */
 	public float getInitBGMVolume() {
 		return 1;
 	}
 
 	/**
 	 * Get the number of maximum entries in the highscorelist
 	 * 
 	 * @return Maximum number of entries in the highscorelist
 	 */
 	public int getHighscoreMaxSize() {
 		return 20;
 	}
 
 }
