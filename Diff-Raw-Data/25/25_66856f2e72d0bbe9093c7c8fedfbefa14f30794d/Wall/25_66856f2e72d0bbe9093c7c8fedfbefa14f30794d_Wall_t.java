 package com.github.joakimpersson.tda367.model.tiles.nonwalkable;
 
 import com.github.joakimpersson.tda367.model.tiles.Tile;
 
 /**
  * 
  * @author joakimpersson
  * 
  */
 public class Wall implements Tile {
 
	private final String image;
 
 	public Wall() {
 		this.image = "wall";
 	}
 
 	@Override
 	public boolean isWalkable() {
 		return false;
 	}
 
 	@Override
 	public String getTileType() {
		return image;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 
 		if (this == obj) {
 			return true;
 		}
 		return obj != null && getClass() == obj.getClass();
 
 	}
 
 	@Override
 	public int hashCode() {
 		return image.hashCode() * 13;
 	}
 
 }
