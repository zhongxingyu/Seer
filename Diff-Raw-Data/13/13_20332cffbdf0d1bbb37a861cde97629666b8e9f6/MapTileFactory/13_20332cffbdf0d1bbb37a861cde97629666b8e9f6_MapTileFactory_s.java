 package com.github.joakimpersson.tda367.model.tiles.factory;
 
 import com.github.joakimpersson.tda367.model.constants.MapTileType;
 import com.github.joakimpersson.tda367.model.tiles.Tile;
 import com.github.joakimpersson.tda367.model.tiles.nonwalkable.Box;
 import com.github.joakimpersson.tda367.model.tiles.nonwalkable.Pillar;
 import com.github.joakimpersson.tda367.model.tiles.nonwalkable.Wall;
 import com.github.joakimpersson.tda367.model.tiles.walkable.Floor;
 
 /**
  * 
  * @author joakimpersson
 *
  */
 public class MapTileFactory {
 
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
