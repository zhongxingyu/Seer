 package coggame;
 
 import java.awt.Graphics;
 import java.awt.Image;
 import java.util.List;
 import java.util.ArrayList;
 
 /**
 * The TiledLayer represents a grid made up of
 * tiles. Tiles can be animated and modified on
 * the fly. A tile with index 0 is not drawn.
 *
 * @author John Earnest
 **/
 public class TiledLayer extends Layer {
 
 	private final int[][] cells;
 	private final Image tiles;
 	private final int tileWidth;
 	private final int tileHeight;
 	private final int sheetWidth;
 	private final List<Integer> animatedTiles = new ArrayList<Integer>();
 
 	/**
 	* Create a new TiledLayer.
 	*
 	* @param columns the number of columns in the grid
 	* @param rows the number of rows in the grid
 	* @param tiles an Image containing a grid of equal-sized tiles
 	* @param tileWidth the width of each tile in pixels
 	* @param tileHeight the height of each tile in pixels
 	**/
 	public TiledLayer(int columns, int rows, Image tiles, int tileWidth, int tileHeight) {
 		cells = new int[columns][rows];
 		this.tiles = tiles;
 		this.tileWidth = tileWidth;
 		this.tileHeight = tileHeight;
 		sheetWidth = tiles.getWidth(null) / tileWidth;
 	}
 
 	/**
 	* Returns the width of the TiledLayer in pixels.
 	**/
 	public int getWidth()		{ return getColumns() * getCellWidth(); }
 
 	/**
 	* Returns the height of the TiledLayer in pixels.
 	**/
 	public int getHeight()		{ return getRows() * getCellHeight(); }
 
 	/**
 	* Returns the number of columns in the TiledLayer.
 	**/
 	public int getColumns()		{ return cells.length; }
 
 	/**
 	* Returns the number of rows in the TiledLayer.
 	**/
 	public int getRows()		{ return cells[0].length; }	
 	
 	/**
 	* Returns the width of a single tile in pixels.
 	**/
 	public int getCellWidth()	{ return tileWidth; }
 
 	/**
 	* Returns the height of a single tile in pixels.
 	**/
 	public int getCellHeight()	{ return tileHeight; }
 
 	/**
 	* Returns the tile index at a given position.
 	*
 	* @param col the 0-indexed column number of the cell
 	* @param row the 0-indexed row number of the cell
 	**/
 	public int getCell(int col, int row) {
 		return cells[col][row];
 	}
 
 	/**
 	* Sets the tile index at a given position.
 	*
 	* @param col the 0-indexed column number of the cell
 	* @param row the 0-indexed row number of the cell
 	* @param tile the 1-indexed tile to set (0 is transparent)
 	**/
 	public void setCell(int col, int row, int tile) {
 		cells[col][row] = tile;
 	}
 
 	/**
 	* Sets the tile index at a given position with
 	* a randomly chosen tile from a provided array.
 	*
 	* @param col the 0-indexed column number of the cell
 	* @param row the 0-indexed row number of the cell
 	* @param tiles an array of possible tiles to set
 	**/
 	public void setCell(int col, int row, int[] tiles) {
 		cells[col][row] = tiles[(int)(Math.random() * tiles.length)];
 	}
 
 	/**
 	* Fill a rectangular region of cells.
 	*
 	* @param col the 0-indexed column number of the cell
 	* @param row the 0-indexed row number of the cell
 	* @param numCols the number of columns to fill
 	* @param numRows the number of rows to fill
 	* @param tile the tile to fill the region with
 	**/
 	public void fillCells(int col, int row, int numCols, int numRows, int tile) {
 		for(int x = 0; x < numCols; x++) {
 			for(int y = 0; y < numRows; y++) {
 				cells[col + x][row + y] = tile;
 			}
 		}
 	}
 
 	/**
 	* Fill a rectangular region of cells with randomly
 	* chosen tiles from a provided array.
 	*
 	* @param col the 0-indexed column number of the cell
 	* @param row the 0-indexed row number of the cell
 	* @param numCols the number of columns to fill
 	* @param numRows the number of rows to fill
 	* @param tiles an array of tile indices to fill the region with
 	**/
 	public void fillCells(int col, int row, int numCols, int numRows, int[] tiles) {
 		for(int x = 0; x < numCols; x++) {
 			for(int y = 0; y < numRows; y++) {
 				cells[col + x][row + y] = tiles[(int)(Math.random() * tiles.length)];
 			}
 		}
 	}
 
 	/**
 	* Draw this TiledLayer.
 	*
 	* @param g the destination Graphics object
 	**/
 	public void paint(Graphics g) {
 		if (!isVisible()) { return; }
 		for(int x = 0; x < getColumns(); x++) {
 			for(int y = 0; y < getRows(); y++) {
 				
 				int tile = cells[x][y];
 				if (tile == 0) { continue; }
 				if (tile < 0) { tile = getAnimatedTile(tile); }
 
 				final int tx = ((tile - 1) % sheetWidth) * tileWidth;
 				final int ty = ((tile - 1) / sheetWidth) * tileHeight;
 				final int dx = x * tileWidth + getX();
 				final int dy = y * tileHeight + getY();
 				g.drawImage(tiles,
 								dx, dy, dx + tileWidth, dy + tileHeight,
 								tx, ty, tx + tileWidth, ty + tileHeight, null);
 			}
 		}
 	}
 
 	/**
 	* Create an animated tile.
 	* Returns the index of the new animated tile.
 	*
 	* @param staticTileIndex the static tile index the animated tile appears as
 	**/
 	public int createAnimatedTile(int staticTileIndex) {
 		animatedTiles.add(staticTileIndex);
 		return -animatedTiles.size();
 	}
 
 	/**
 	* Returns the static tile index an animated tile currently appears as
 	*
 	* @param animatedTileIndex the animated tile to examine
 	**/
 	public int getAnimatedTile(int animatedTileIndex) {
 		return animatedTiles.get(-animatedTileIndex - 1);
 	}
 
 	/**
 	* Change the appearance of an animated tile.
 	*
 	* @param animatedTileIndex the animated tile to change
 	* @param staticTileIndex the static tile the animated tile should appear as
 	**/
 	public void setAnimatedTile(int animatedTileIndex, int staticTileIndex) {
 		animatedTiles.set(-animatedTileIndex - 1, staticTileIndex);
 	}
 
 	private static boolean relEquals(int x, int y, TiledLayer grid, int testval) {
 		if (x < 0) {return false;}
 		if (y < 0) {return false;}
 		if (x >= grid.getColumns()) {return false;}
 		if (y >= grid.getRows()) {return false;}
 		return (grid.getCell(x, y) == testval);
 	}
 
 	private static final int SKY_WIDTH_MIN = 2;
 	private static final int SKY_WIDTH_MAX = 6;
 	private static final int SKY_HEIGHT_DELTA = 4;
 
 	/**
 	* Fill a provided TiledLayer with a procedurally
 	* generated skyline. Tiles are randomly selected
 	* from the lists provided.
 	*
 	* @param layer the TiledLayer to fill.
 	* @param fill tile indices for solid regions of the skyline
 	* @param left tile indices for left edges of the skyline
 	* @param right tile indices for right edges of the skyline
 	* @param top tile indices for the top edge of the skyline
 	**/
 	public static void skyline(TiledLayer layer, int[] fill, int[] left, int[] right, int[] top) {
 		layer.fillCells(0, 0, layer.getColumns(), layer.getRows(), 0);
 		
 		// fill solid skyline
 		int col = layer.getColumns() - 1;
 		int height = layer.getRows() / 2;
 		do {
 			height += (int)(Math.random() * SKY_HEIGHT_DELTA) * 2 - SKY_HEIGHT_DELTA;
 			if (height > layer.getRows() - 2) { height = (int)(Math.random() * layer.getRows()) - 2; }
 			int width = (int)(Math.random() * (SKY_WIDTH_MAX - SKY_WIDTH_MIN)) + SKY_WIDTH_MIN;
 
 			height = Math.max(height, 2);
 			width = Math.min(width, col);
 			col -= width;
 
 			for(int x = 0; x <= width; x++) {
 				for(int y = 0; y < layer.getRows() - height; y++) {
 					layer.setCell(x + col, y + height, fill);
 				}
 			}
 		} while (col > 0);
 
 		// add edge decorations
 		for(int x = layer.getColumns() - 1; x >= 0; x--) {
 			for(int y = layer.getRows() - 1; y >= 0; y--) {
 
 				if (relEquals(x, y - 1, layer, 0)) {
 					layer.setCell(x, y - 1, top);
 					break;
 				}
 				else if (relEquals(x + 1, y, layer, 0)) {
 					layer.setCell(x, y, right);
 				}
 				else if (relEquals(x - 1, y, layer, 0)) {
 					layer.setCell(x, y, left);
 				}
 			}
 		}
 	}
 
 	private static final int CLOUD_HEIGHT = 4;
 	private static final int CLOUD_DELTA = 3;
 	private static final int CLOUD_COUNT = 6;
 
 	/**
 	* Add a series of procedurally generated clouds to
 	* a TiledLayer. Tiles are randomly selected
 	* from the lists provided.
 	*
 	* @param layer the TiledLayer to fill
 	* @param leftend tiles for the leftmost end of a cloud
 	* @param rightend tiles for the rightmost end of a cloud
 	* @param left tiles for upper-left corners/clumps
 	* @param right tiles for upper-right corners/clumps
 	* @param fill tiles for the center of a cloud
 	**/
	private static void clouds(TiledLayer layer, int[] leftend, int rightend[], int[] left, int[] right, int[] fill) {
 
 		for(int z = 0; z < CLOUD_COUNT; z++) {			
 			final int maxh = (int)Math.max(2, Math.random() * CLOUD_HEIGHT);
 			int x = (int)(Math.random() * layer.getColumns());
 			int y = (int)(Math.random() * (layer.getRows() - maxh - 1)) + maxh;
 			
 			if (layer.getCell(x, y) == 0) { layer.setCell(x, y, leftend); }
 			// build up
 			for(int h = 1; h <= maxh; h++) {
 				x = (x+1) % layer.getColumns();
 				if (layer.getCell(x, y-h) == 0) { layer.setCell(x, y-h, left); }
 				layer.fillCells(x, y-h+1, 1, h, fill);
 
 				for(int b = (int)(Math.random()*CLOUD_DELTA); b > 0; b--) {
 					x = (x+1) % layer.getColumns();
 					layer.fillCells(x, y-h, 1, h+1, fill);
 				}
 			}
 			// middle stretch
 			for(int b = (int)(Math.random()*CLOUD_DELTA) + 1; b > 0; b--) {
 				x = (x+1) % layer.getColumns();
 				layer.fillCells(x, y-maxh, 1, maxh + 1, fill);
 			}
 			// build down
 			for(int h = maxh; h >= 1; h--) {
 				x = (x+1) % layer.getColumns();
 				if (layer.getCell(x, y-h) == 0) { layer.setCell(x, y-h, right); }
 				layer.fillCells(x, y-h+1, 1, h, fill);
 
 				for(int b = (int)(Math.random()*CLOUD_DELTA); b > 0; b--) {
 					x = (x+1) % layer.getColumns();
 					layer.fillCells(x, y-h+1, 1, h, fill);
 				}
 			}
 			x = (x+1) % layer.getColumns();
 			if (layer.getCell(x, y) == 0) { layer.setCell(x, y, rightend); }
 		}
 	}
 
 	/**
 	* Find a path from a starting location in a TiledLayer
 	* to a given goal location. Nonzero tiles are considered
 	* impassable, for consistency with how sprite collision
 	* deals with TiledLayers.
 	*
 	* Returns a sequence of pairs of coordinates representing
 	* relative tile positions. If no path is found, returns null.
 	*
 	* @param layer the TiledLayer to search
 	* @param xStart the x-position of the starting tile
 	* @param yStart the y-position of the starting tile
 	* @param xGoal the x-position of the goal tile
 	* @param yGoal the y-position of the goal tile
 	* @param useDiagonal should we consider diagonal movement as well as orthogonal?
 	**/
 	public static List<int[]> path(TiledLayer layer, int xStart, int yStart, int xGoal, int yGoal, boolean useDiagonal) {
 
 		List<int[]> frontier = new ArrayList<int[]>();
 		frontier.add(new int[] { xGoal, yGoal, 1 });
 
 		int[][] grid = new int[layer.getColumns()][layer.getRows()];
 		int[][] deltas = (!useDiagonal) ? new int[][] { {-1, 0}, {1, 0}, {0, -1}, {0, 1} } :
 										  new int[][] { {-1, 0}, {1, 0}, {0, -1}, {0, 1},
 														{-1,-1}, {1, 1}, {-1, 1}, {1,-1} };
 
 		while (!frontier.isEmpty()) {
 			int[] coords = frontier.remove(0);
 			int x = coords[0];
 			int y = coords[1];
 			int c = coords[2];
 			
 			if (x < 0 || x >= grid.length)		{ continue; }	// x off board
 			if (y < 0 || y >= grid[0].length)	{ continue; }	// y off board
 			if (layer.getCell(x, y) != 0)		{ continue; }	// impassable tile
 			if (grid[x][y] == 0) {								// if unvisited, fan out
 				for(int[] delta : deltas) {
 					frontier.add(new int[] { x + delta[0], y + delta[1], c + 1 });
 				}
 				grid[x][y] = c;
 			}
 			else {
 				grid[x][y] = Math.min(grid[x][y], c);
 			}
 		}
 
 		// Now every unreachable grid cell should be at 0
 		// and every other grid cell should contain their
 		// distance from the goal position.
 
 		if (grid[xStart][yStart] == 0) { return null; }
 
 		List<int[]> ret = new ArrayList<int[]>();
 		int x = xStart;
 		int y = yStart;
 		while (x != xGoal || y != yGoal) {
 			int dx = 0;
 			int dy = 0;
 			int dc = Integer.MAX_VALUE;
 			for(int[] delta : deltas) {
 				int nx = x + delta[0];
 				int ny = y + delta[1];
 				if (nx < 0 || nx >= grid.length)	{ continue; } // x off board
 				if (ny < 0 || ny >= grid[0].length)	{ continue; } // y off board
 				int value = grid[nx][ny];
 				if (value == 0 || value >= dc)		{ continue; } // unreachable / suboptimal
 				dx = delta[0];
 				dy = delta[1];
 				dc = value;
 			}
 			ret.add(new int[] {dx, dy});
 			x += dx;
 			y += dy;
 		}
 		return ret;
 	}
 
 }
