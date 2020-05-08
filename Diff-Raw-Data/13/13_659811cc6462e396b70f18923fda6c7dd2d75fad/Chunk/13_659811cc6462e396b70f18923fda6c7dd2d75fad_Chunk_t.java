 package complexion.server;
 
 /**
  * @author head
  * A small part of a larger map.
  */
 public class Chunk
 {
 	/// The global x,y,z location of the chunk
 	private int global_x,global_y,global_z;
 	/// The tiles inside this chunk
 	private Tile[][] contents; 
 	/// the size of the chunk width (tiles)
 	private int chunk_size_width = 50;
 	/// the size of the chunk height (tiles)
 	private int chunk_size_height = 50;
 
 	/// if the chunk is active
 	private boolean active = false;
 	/**
 	 * @return the chunks X location on the zlevel
 	 */
 	public int getGlobalX()
 	{
 		return global_x;
 	}
 	/**
 	 * @return the chunks Y location on the zlevel
 	 */
 	public int getGlobalY()
 	{
 		return global_y;
 	}
 	/**
 	 * The chunks Z location
 	 * @return the z
 	 */
 	public int getGlobalZ()
 	{
 		return global_z;
 	}
 	/**
 	 * @return the chunk_size_x
 	 */
 	/**
 	 * TODO: Remove? replace with a ref to to its zlevel instead?
 	 * @return the chunk Wdith in tiles
 	 */
 	public int getWidth()
 	{
 		return chunk_size_width;
 	}
 	/**
 	 * TODO: Remove? replace with a ref to to its zlevel instead?
 	 * @return the chunk height in tiles
 	 */
 	public int getHeight()
 	{
 		return chunk_size_height;
 	}
 	/**
 	 * if the chunk is active and should be processed
 	 * @return return true if it active and returns false if not
 	 */
 	public boolean isActive()
 	{
 		return active;
 	}
 	/**
 	 * unloads the chunk to a  cache file
 	 * @return true if successful false if it failed.
 	 */
 	public boolean unloadChunk()
 	{
 		return false;
 	}
 	
 	/**
 	 * Loads the chunk from its cache file
 	 * @return true if successful false if it failed.
 	 */
 	public boolean loadChunk()
 	{
 		return false;
 	}
 	/**
 	 * Get the tile at the specified tile-position(NOT pixel-position)
 	 * Returns null if Tile is past map boundaries.
 	 */
	public Tile getTile(int pos_x,int pos_y)
 	{
		int x = pos_x / getWidth(); // ????
		int y = pos_y / getHeight(); // MATHS IS GEWD
 		if(x >= 50 || y >= 50)
 			return null;
 		Tile T = contents[x][y];
 		return T;
 		
 	}
 	/**
 	 * Processes all tiles in the chunk. Calls Tile.Tick that properly should process its contents.
 	 */
 	public void Tick()
 	{
 		for(int x = 0;x<getWidth();x++)
 		{
 			for(int y = 0;x<getHeight();y++)
 			{
 				Tile T = contents[x][y];
 				T.Tick();
 			}
 		}
 	}
 }
