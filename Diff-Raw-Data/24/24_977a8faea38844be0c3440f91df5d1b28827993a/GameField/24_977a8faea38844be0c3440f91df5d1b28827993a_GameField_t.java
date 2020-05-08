 package com.github.joakimpersson.tda367.core;
 
 /**
 * @date 2012-03-26
 * @author Viktor Anderling
  * 
  * An abstract class representing the gamefield with a matrix of tiles.
 * 
  */
 public abstract class GameField {
 	private Tile[][] map;
	
	
	/**
	 * Resets the field to it's initial state.
	 */
 	public abstract void resetField();
 
	/**
	 * A method for setting a specific position of the gamefield to a specific tile.
	 * 
	 * @param tile	The tile that will be set.
	 * @param pos	The position where the tile will be set.
	 * @throws IndexOutBoundsException if the position has negative parameters or is
	 * bigger than the size of the matrix.
	 */
 	public void setTile(Tile tile, Position pos) {
 		int x = pos.getX();
 		int y = pos.getY();
 		if(x < 0 || y < 0 || x > map.length || y < map[0].length) {
 			throw new IndexOutOfBoundsException();
 		}
 		map[x][y] = tile;
 	}
 
 }
