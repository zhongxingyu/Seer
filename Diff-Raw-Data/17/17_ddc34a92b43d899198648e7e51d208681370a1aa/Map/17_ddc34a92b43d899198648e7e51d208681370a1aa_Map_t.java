 package edu.gatech.cs2340.data;
 
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Random;
 import java.util.logging.Logger;
 
 /**
  * 
  * @author Stephen Conway Function group: Model: Data manager Created for: M6
  *         10/7/13 Assigned to: Maddy Modifications:
  * 
  * 
  * 
  *         Purpose: Hold Tiles. Return tiles as needed.
  * 
  */
 
 // 5 x 9 grid
 public class Map implements MapResponsibilities {
 
 	// Can actually handle a map of any size
 	/**
 	 * #M6 Main constructor. Takes in 44 Tiles to hold.
 	 * 
 	 * @param tiles
 	 */
 	// the current title coordinates
 	private int currX;
 	private int currY;
 	private Logger logger;
 
 	private Tile[][] tiles;
 
 	public Map(Tile[][] tiles) {
 		this.tiles = tiles;
 		this.currX = 0;
 		this.currY = 0;
 
 		// for error logging
 		logger = Logger.getGlobal();
 	}
 
 	// increment the current tile coordinates and return the next tile
 	// TODO deal with last tile
 	@Override
 	public Tile getNextTile() {
 		currX++;
 		currY++;
 		return getTileAt(currX, currY);
 
 	}
 
 	// gets the tile at the given indices. logs error, catches exception, and
 	// returns null if tile
 	// does not exist.
 	@Override
 	public Tile getTileAt(int r, int c) {
 		Tile t = null;
 		try {
 			t = tiles[r][c];
 
 		} catch (ArrayIndexOutOfBoundsException ex) {
 			logger.log(
 					null,
 					"Goofy goober, don't try and access a tile that doesn't exist",
 					ex);
 			return null;
 
 		}
 		return t;
 
 	}
 
 	// NOTE: Has to work with MapSprite which has different global coordinates
 	// than the tiles
 	// onverts global coordinates to approximate map coordinates.
 	// will return null if tile does not exist
 	@Override
 	public Tile getTileAt(double x, double y) {
 		return getTileAt((int) (x / tiles.length), (int) (y / tiles[0].length));
 	}
 
 	// get next tile by row
 	// mod ndx by # of tiles in a row
 	// will return null if tile does not exist
 	@Override
 	public Tile getTileNumber(int ndx) {
 		// to get col, mod ndx by # of tiles in a row (# of columns)
 		int col = ndx / tiles.length;
 		int row = (ndx - col) / tiles[0].length;
 
 		return getTileAt(row, col);
 	}
 
 	// gets the next unknown tile, setting pointers to point to it
 	// will return null if all tiles are owned
 	@Override
 	public Tile getNextUnownedTile() {
 		Tile t = null;
 		do {
 			t = getTileAt(currX, currY);
 			if (t != null && t.getOwner() == null) {
 				return t;
 			}
			if (currY < tiles[0].length) {
				currY++;
			} else {
				currX++;
				currY = 0;
			}
 		} while (t != null);
 		logger.log(null, "All tiles are owned");
 		return null;
 	}
 
 	// creates a list of unowned tiles and returns a random element from this
 	// list
 	// sets pointers to point to the random unowned tile
 	@Override
 	public Tile getRandomUnownedTile() {
 		Collection<Tile> unownedTiles = new HashSet<Tile>();
 		int oldCurrX = currX;
 		int oldCurrY = currY;
 		Tile t = null;
 		do {
 			t = getTileAt(currX, currY);
 			if (t != null && t.getOwner() == null) {
 				unownedTiles.add(t);
 			}
			if (currY < tiles[0].length) {
				currY++;
			} else {
				currX++;
				currY = 0;
			}
 		} while (t != null);
 
 		if (!unownedTiles.isEmpty()) {
 			Random gen = new Random();
 			Tile[] unowned = (Tile[]) unownedTiles.toArray();
 			t = unowned[gen.nextInt(unownedTiles.size())];
 			setCurrentTile(t);
 			return t;
 		} else {
 			// log and reset pointer
 			logger.log(null, "All tiles are owned");
 			currX = oldCurrX;
 			currY = oldCurrY;
 			return null;
 		}
 	}
 
 	// helper method to get x y coordinates of tile
 	// and set currX and currY
 	private void setCurrentTile(Tile t) {
 		outerloop: for (int i = 0; i < tiles.length; i++) {
 			for (int j = 0; j < tiles[0].length; j++) {
 				if (tiles[i][j].compareTo(t)) {
 					currX = i;
 					currY = j;
 					break outerloop;
 				}
 			}
 		}
 
 	}
 
 	// resets ownership of next tile
 	// sets current tile pointers to next tile
 	@Override
 	public void resetNextUnownedTile() {
 		Tile t = getNextUnownedTile();
 		t.setOwner(null);
 	}
 
 }
