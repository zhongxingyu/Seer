 package complexion.server;
 
 public class Tile extends Atom {
 	// TODO: Add a public contents variable
 	//		 (Not sure yet which type we're going to use
 	//		  where BYOND uses lists)
 	
 	///The x/y location of the tile on the map.
 	private int x, y;
 
 	/**
 	 * @return The x location of the tile.
 	 */
 	public int getX()
 	{
 		return x;
 	}
 	/**
 	 * @return The y location of the tile.
 	 */
 	public int getY()
 	{
 		return y;
 	}
 	/**
 	 * This is here so that it doesn't inherit this from Atom.
 	 * Don't call this.
 	 */
 	public void setLoc(Atom new_loc)
 	{
 		return;
 	}
 	/**
 	 * Changed from Atom so that if loc does get set somehow then it still returns null.
 	 * If it's not set then it would do the same thing, but be faster this way.
 	 * @return null
 	 */
 	public Atom getLoc()
 	{
 		return null;
 	}
 	/**
 	 * Gets the tile object at the bottom of whatever location it is on, which since this is
 	 * the tile, is this.
 	 * @return this
 	 */
 	public Atom getTile()
 	{
 		return this;
 	}
 }
