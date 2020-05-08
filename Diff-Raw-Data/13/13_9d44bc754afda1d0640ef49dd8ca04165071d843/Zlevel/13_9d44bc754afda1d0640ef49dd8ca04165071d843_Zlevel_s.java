 package complexion.server;
 
 /**
  * @author head
  * Hold's the map for a zlevel.
  * 
  * This is basically one "slice" of the map that holds all the tiles that are on the same
  * "height". 
  * 
  * Thus far, it's unclear whether we will support seeing tiles on a "below" z-level,
  * or whether different z-levels will be completely separate maps.
  */
 public class Zlevel {
 	/// Fixed sized array containing chunks each being 50x50(tiles) 
 	private Chunk[][] map;
 	private int level_width = 50;
 	private int level_height = 50;
 
 	/**
 	 * Processes all chunks on the zlevel that are active.
 	 */
 	public void Tick()
 	{
 		for(int x=0;x<level_width;x++)
 		{
 			for(int y =0;y<level_height;y++)
 			{
 				Chunk cur_chunk = map[x][y];
 				if(cur_chunk.isActive()) // if it's not in hibernation tick it.
 					cur_chunk.Tick();
 			}
 		}
 	}
 	/**
 	 * Get the tile at the specified tile-position(NOT pixel-position)
 	 * Returns null if Tile is past map boundaries. or the chunk is not present.
 	 */
 	Tile getTile(int pos_x,int pos_y)
 	{
 		Chunk chunk = map[pos_x / level_width][pos_y / level_height];
 		if(chunk == null)
 			return null;
 		return chunk.getTile(pos_x, pos_y);
 	}
 }
