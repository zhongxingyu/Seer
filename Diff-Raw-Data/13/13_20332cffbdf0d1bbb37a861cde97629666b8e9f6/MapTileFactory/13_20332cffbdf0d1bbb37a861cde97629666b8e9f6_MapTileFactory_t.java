 package com.github.joakimpersson.tda367.model.tiles.factory;
 
 import com.github.joakimpersson.tda367.model.constants.MapTileType;
 import com.github.joakimpersson.tda367.model.tiles.Tile;
 import com.github.joakimpersson.tda367.model.tiles.nonwalkable.Box;
 import com.github.joakimpersson.tda367.model.tiles.nonwalkable.Pillar;
 import com.github.joakimpersson.tda367.model.tiles.nonwalkable.Wall;
 import com.github.joakimpersson.tda367.model.tiles.walkable.Floor;
 
 /**
 * A factory class for creating different tiles that the map is built of.
  * 
  * @author joakimpersson
 * @modified adderollen
 * 
  */
 public class MapTileFactory {
 
	/**
	 * Creating a MapTile of a given type.
	 * 
	 * @param tileType
	 *            The tile type that will be created.
	 * @return The created tile.
	 */
 	public Tile createObject(MapTileType tileType) {
 
 		switch (tileType) {
 		case Box:
 			return new Box();
 		case Floor:
 			return new Floor();
 		case Pillar:
 			return new Pillar();
 		case Wall:
 			return new Wall();
 		default:
 			// should not happen
 			return null;
 		}
 	}
 
 }
