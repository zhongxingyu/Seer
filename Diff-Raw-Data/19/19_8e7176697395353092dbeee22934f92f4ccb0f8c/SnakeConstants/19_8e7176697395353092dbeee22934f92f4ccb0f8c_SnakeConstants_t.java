 /*******************************************************************************
  * Copyright (c) 2013 See AUTHORS file.
  * 
  * This file is part of SleepFighter.
  * 
  * SleepFighter is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * SleepFighter is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  * See the GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with SleepFighter. If not, see <http://www.gnu.org/licenses/>.
  ******************************************************************************/
 
 package se.chalmers.dat255.sleepfighter.challenge.rotosnake;
 
 import se.chalmers.dat255.sleepfighter.utils.geometry.Dimension;
 import android.graphics.Color;
 
 /**
  * Configuration constants for the game Snake. Original author Centril, modified
  * by Laszlo for Sleepfighter.
  */
 public enum SnakeConstants {
 	; // Safe Singleton pattern, prevent instantiation.
 
 	/*
 	 * -------------------------------- Constants.
 	 * --------------------------------
 	 */
 	/** The size of games, that is how many tiles there is in each axis. */
 	private static final Dimension SIZE = new Dimension(20, 10);
 
 	/** The size of each tile in game. */
 	private static final int TILE_SIZE = 1;
 
 	/** The number of milliseconds (ms) between game updates. */
	private static final int UPDATE_INTERVAL = 300;
 
 	/** The initial length of snake. */
 	private static final int INITIAL_SNAKE_LENGTH = 3;
 
 	/** The color of food. */
 	private static final int FOOD_COLOR = Color.BLUE;
 
 	/** The color of the snake. */
 	private static final int SNAKE_COLOR = Color.RED;
 
 	/** The color of the snakes head. */
 	private static final int SNAKE_HEAD_COLOR = Color.GREEN;
 
 	/** Points needed to win. */
 	private static final int VICTORY_CONDITION = 3;
 
 	/*
 	 * -------------------------------- Getters.
 	 * --------------------------------
 	 */
 	/** @return The size of games, that is how many tiles there is in each axis. */
 	public static Dimension getGameSize() {
 		return SIZE;
 	}
 
 	/** @return The size of each tile in game. */
 	public static int getTileSize() {
 		// Dimension is a mutable class, copy to prevent mutation.
 		return TILE_SIZE;
 	}
 
 	/** @return The number of milliseconds (ms) between game updates. */
 	public static int getUpdateSpeed() {
 		return UPDATE_INTERVAL;
 	}
 
 	/**
 	 * @return The color of food.
 	 */
 	public static int getFoodColor() {
 		return FOOD_COLOR;
 	}
 
 	/**
 	 * The color of the snake.
 	 * 
 	 * @return The color of the snake.
 	 */
 	public static int getSnakeColor() {
 		return SNAKE_COLOR;
 	}
 
 	/**
 	 * The color of the snakes head.
 	 * 
 	 * @return The color of the snakes head.
 	 */
 	public static int getSnakeHeadColor() {
 		return SNAKE_HEAD_COLOR;
 	}
 
 	/**
 	 * Returns the initial length of snake.
 	 * 
 	 * @return The initial length of snake.
 	 */
 	public static int getInitialSnakeLength() {
 		return INITIAL_SNAKE_LENGTH;
 	}
 
 	/**
 	 * Returns the number of points needed to win.
 	 * 
 	 * @return The number of points needed to win.
 	 */
 	public static int getVictoryCondition() {
 		return VICTORY_CONDITION;
 	}
 }
