 package com.github.joakimpersson.tda367.model.map;
 
 import com.github.joakimpersson.tda367.model.positions.Position;
 import com.github.joakimpersson.tda367.model.tiles.Tile;
 
 /**
  * 
  * @author joakimpersson
  * 
  */
 public interface IGameMap {
 	/**
 	 * Get the matrix representation of the current gamemap made of tiles
 	 * 
 	 * @return A matrix representation of the gamemap
 	 */
 	Tile[][] getMap();
 
 	/**
 	 * Replace a tile at the supplied position with a new tile
 	 * 
 	 * @param tile
 	 *            The new tile
 	 * @param pos
 	 *            The position to change tile on
 	 */
 	void setTile(Tile tile, Position pos);
 
 	/**
 	 * Get the tile located at the supplied position
 	 * 
 	 * @param pos
 	 *            The position of the tile
 	 * @return The tile at the supplied position
 	 */
 	Tile getTile(Position pos);
 
 	/**
 	 * Reset the map to its original state
 	 */
 	void reset();
 
 }
