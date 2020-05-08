 package yuuki.world;
 
 /**
  * Holds a series of Tile instances at a specific set of coordinates. Every
  * TileGrid is rectangular.
  */
 public class TileGrid {
 	
 	/**
 	 * The height of this TileGrid, in number of Tile instances.
 	 */
 	private int height;
 	
 	/**
 	 * The Tile instances in this TileGrid.
 	 */
 	private Tile[][] tiles;
 	
 	/**
 	 * The width of this TileGrid, in number of Tile instances.
 	 */
 	private int width;
 	
 	/**
 	 * Creates a new TileGrid and initializes new Tiles with the given names.
 	 * 
 	 * @param w The width of the new TileGrid.
 	 * @param h The height of the new TileGrid.
 	 * @param names The names of the Tile instances.
 	 */
 	public TileGrid(int w, int h, String[] names) {
 		this.width = w;
 		this.height = h;
 		tiles = new Tile[width][height];
 		for (int i = 0; i < width; i++) {
 			for (int j = 0; j < height; j++) {
 				tiles[i][j] = new Tile(names[(i*height) + j], true);
 			}
 		}
 	}
 	
 	/**
 	 * Creates a new TileGrid from an existing Tile array.
 	 * 
 	 * @param w The width of the new TileGrid.
 	 * @param h The height of the new TileGrid.
 	 * @param tiles The existing Tile array to create the TileGrid from.
 	 */
 	public TileGrid(int w, int h, Tile[] tiles) {
 		this.width = w;
 		this.height = h;
		this.tiles = new Tile[w][h];
 		for (int i = 0; i < width; i++) {
 			for (int j = 0; j < height; j++) {
 				this.tiles[i][j] = tiles[(i*height) + j];
 			}
 		}
 	}
 	
 	/**
 	 * Creates a new TileGrid an initializes its Tile array from an existing
 	 * Tile array. Each Tile reference in the existing array is copied; the
 	 * array itself is not copied.
 	 * 
 	 * @param tiles The existing tiles to use for this TileGrid's array.
 	 */
 	public TileGrid(Tile[][] tiles) {
 		this.width = tiles.length;
 		this.height = tiles[0].length;
 		this.tiles = new Tile[width][height];
 		for (int i = 0; i < width; i++) {
 			for (int j = 0; j < height; j++) {
 				this.tiles[i][j] = tiles[i][j];
 			}
 		}
 	}
 	
 	/**
 	 * Creates a new TileGrid without setting any properties.
 	 */
 	private TileGrid() {}
 	
 	/**
 	 * Gets the height of this TileGrid.
 	 * 
 	 * @return The height.
 	 */
 	public int getHeight() {
 		return height;
 	}
 	
 	/**
 	 * Gets a TileGrid that is a sub-grid of this TileGrid. The returned
 	 * TileGrid will only contain valid coordinates specified by the given
 	 * dimensions.
 	 * 
 	 * @param x The x-coordinate of the sub-grid to get.
 	 * @param y The y-coordinate of the sub-grid to get.
 	 * @param w The width of the sub-grid to get.
 	 * @param h The height of the sub-grid to get.
 	 * 
 	 * @return A TileGrid that has the same references to Tile instances as
 	 * this one does, with the specified dimensions.
 	 */
 	public TileGrid getSubGrid(int x, int y, int w, int h) {
 		int subWidth = Math.min(width - x, w);
 		int subHeight = Math.min(height - y, h);
 		Tile[][] subTiles = new Tile[subWidth][subHeight];
 		for (int i = 0; i < subWidth; i++) {
 			for (int j = 0; j < subHeight; j++) {
 				subTiles[i][j] = tiles[x + i][y + j];
 			}
 		}
 		TileGrid subGrid = new TileGrid();
 		subGrid.tiles = subTiles;
 		subGrid.width = subTiles.length;
 		subGrid.height = subTiles[0].length;
 		return subGrid;
 	}
 	
 	/**
 	 * Gets the width of this TileGrid.
 	 * 
 	 * @return The width.
 	 */
 	public int getWidth() {
 		return width;
 	}
 	
 	/**
 	 * Gets a reference to the Tile instance at the specific point.
 	 * 
 	 * @param x The x-coordinate of the Tile to get.
 	 * @param y The y-coordinate of the Tile to get.
 	 * 
 	 * @return A reference to the Tile at the given position.
 	 */
 	public Tile tileAt(int x, int y) {
 		return tiles[x][y];
 	}
 	
 }
