 package environment;
 
 import java.awt.Graphics;
 import java.awt.Rectangle;
 import java.util.ArrayList;
 import java.util.List;
 
 public class Tiles {
 	private RoadTile[][] tiles = new RoadTile[2000][2000];
 	private int centerX, centerY;
 
 	public Tiles() {
 		centerX = tiles.length / 2;
		centerY = tiles[0].length/2;
 		tiles[centerX][centerY] = new RoadTile(true, true, true, true, this, 0,
 				0);
 	}
 
 	public RoadTile createNew(int x, int y) {
 		boolean north;
 		boolean east;
 		boolean south;
 		boolean west;
 
 		x += centerX;
 		y += centerY;
 		// calculates east edge
 		if (x + 1 < tiles.length) {
 			if (tiles[x + 1][y] != null
 					&& tiles[x + 1][y].hasWall(Direction.WEST)) {
 				east = false;
 			} else if (tiles[x + 1][y] != null) {
 				east = true;
 			} else {
 				if (Math.random() < 0.5) {
 					east = true;
 				} else {
 					east = false;
 				}
 			}
 		} else {
 			east = false;
 		}
 		// calculates west edge
 		if (x - 1 >= 0) {
 			if (tiles[x - 1][y] != null
 					&& tiles[x - 1][y].hasWall(Direction.EAST)) {
 				west = false;
 			} else if (tiles[x - 1][y] != null) {
 				west = true;
 			} else {
 				if (Math.random() < 0.5) {
 					west = true;
 				} else {
 					west = false;
 				}
 			}
 		} else {
 			west = false;
 		}
 		// calculates north edge
 		if (y - 1 >= 0) {
 			if (tiles[x][y - 1] != null
 					&& tiles[x][y - 1].hasWall(Direction.SOUTH)) {
 				north = false;
 			} else if (tiles[x][y - 1] != null) {
 				north = true;
 			} else {
 				if (Math.random() < 0.5) {
 					north = true;
 				} else {
 					north = false;
 				}
 			}
 		} else {
 			north = false;
 		}
 		// calculate south edge
 		if (y + 1 < tiles.length) {
 			if (tiles[x][y + 1] != null
 					&& tiles[x][y + 1].hasWall(Direction.NORTH)) {
 				south = false;
 			} else if (tiles[x][y + 1] != null) {
 				south = true;
 			} else {
 				if (Math.random() < 0.5) {
 					south = true;
 				} else {
 					south = false;
 				}
 			}
 		} else {
 			south = false;
 		}
 		RoadTile tile = new RoadTile(north, east, south, west, this, x, y);
 		if (north) {
 			tile.addNeighbour(Direction.NORTH, tiles[x][y - 1]);
 			if (tiles[x][y - 1] != null) {
 				tiles[x][y - 1].addNeighbour(Direction.SOUTH, tile);
 			}
 		}
 		if (east) {
 			tile.addNeighbour(Direction.EAST, tiles[x + 1][y]);
 			if (tiles[x + 1][y] != null) {
 				tiles[x + 1][y].addNeighbour(Direction.WEST, tile);
 			}
 		}
 		if (west) {
 			tile.addNeighbour(Direction.WEST, tiles[x - 1][y]);
 			if (tiles[x - 1][y] != null) {
 				tiles[x - 1][y].addNeighbour(Direction.EAST, tile);
 			}
 		}
 		if (south) {
 			tile.addNeighbour(Direction.SOUTH, tiles[x][y + 1]);
 			if (tiles[x][y + 1] != null) {
 				tiles[x][y + 1].addNeighbour(Direction.NORTH, tile);
 			}
 		}
 		return tile;
 	}
 
 	public void draw(Graphics g, int x, int y, int screenWidth, int screenHeight) {
 		int xDiff = x % 320;
 		int yDiff = y & 320;
 		x = (int) Math.round(x / 320);
 		y = (int) Math.round(y / 320);
 		if (tiles[x][y] != null) {
 			tiles[x][y].draw(g, screenWidth / 2 + xDiff, screenHeight / 2
 					+ yDiff, screenWidth, screenHeight);
 		}
 	}
 
 	public List<Rectangle> getBoundingBoxes(int x, int y, int screenWidth,
 			int screenHeight) {
 		int xDiff = x % 320;
 		int yDiff = y & 320;
 		x = (int) Math.round(x / 320);
 		y = (int) Math.round(y / 320);
 		RoadTile road = tiles[x][y];
 		List<Rectangle> bounds = new ArrayList<Rectangle>();
 		bounds.add(new Rectangle(screenWidth / 2 + xDiff - 160, screenHeight
 				/ 2 + yDiff - 160, 128, 128));
 		bounds.add(new Rectangle(screenWidth / 2 + xDiff - 160, screenHeight
 				/ 2 + yDiff + 32, 128, 128));
 		bounds.add(new Rectangle(screenWidth / 2 + xDiff + 32, screenHeight / 2
 				+ yDiff - 160, 128, 128));
 		bounds.add(new Rectangle(screenWidth / 2 + xDiff + 32, screenHeight / 2
 				+ yDiff + 32, 128, 128));
 		if (road.hasWall(Direction.NORTH)) {
 			bounds.add(new Rectangle(screenWidth / 2 + xDiff - 160,
 					screenHeight / 2 + yDiff - 40, 128, 128));
 		}
 		if (road.hasWall(Direction.EAST)) {
 			bounds.add(new Rectangle(screenWidth / 2 + xDiff - 40, screenHeight
 					/ 2 + yDiff - 160, 128, 128));
 		}
 		if (road.hasWall(Direction.SOUTH)) {
 			bounds.add(new Rectangle(screenWidth / 2 + xDiff + 32, screenHeight
 					/ 2 + yDiff - 40, 128, 128));
 		}
 		if (road.hasWall(Direction.WEST)) {
 			bounds.add(new Rectangle(screenWidth / 2 + xDiff - 40, screenHeight
 					/ 2 + yDiff + 32, 128, 128));
 		}
 
 		return bounds;
 	}
 }
