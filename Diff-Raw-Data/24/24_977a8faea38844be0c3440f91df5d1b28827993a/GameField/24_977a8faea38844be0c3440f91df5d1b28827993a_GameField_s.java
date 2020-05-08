 package com.github.joakimpersson.tda367.core;
 
 /**
 * 
 * @author Viktor
  * 
  * An abstract class representing the gamefield with a matrix of tiles.
  */
 public abstract class GameField {
 	private Tile[][] map;

	public abstract void createWorld();

 	public abstract void resetField();
 
 	public void setTile(Tile tile, Position pos) {
 		int x = pos.getX();
 		int y = pos.getY();
 		if(x < 0 || y < 0 || x > map.length || y < map[0].length) {
 			throw new IndexOutOfBoundsException();
 		}
 		map[x][y] = tile;
 	}
 
 }
