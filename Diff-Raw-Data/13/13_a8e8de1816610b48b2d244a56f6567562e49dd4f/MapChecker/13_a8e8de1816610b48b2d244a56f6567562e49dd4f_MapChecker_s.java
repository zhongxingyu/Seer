 package com.xidstudios.pixelarena.pathfinding;
 
 import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
 
 public class MapChecker {
 
 	public static Cell[][] cells;
 
 	public boolean[][] passable;
 
 	public int[][] passCost;
 
	public MapChecker(Cell[][] cell) {
 		passable = new boolean[cells.length][cells[0].length];
 		passCost = new int[passable.length][passable[0].length];
 
 		checkPassable();
 	}
 
 	private void checkPassable() {
 
 		for (int x = 0; x < passable.length; x++) {
 			for (int y = 0; y < passable[x].length; y++) {
 
 				passCost[x][y] = 1;
 				// if (condition)? (choice one) :which means or (choice two)
 				passable[y][x] = cells[y][x].getTile()
 						.getProperties().containsKey("blocked") == true ? false
 						: true;
 
 			}
 
 		}
 	}
 }
