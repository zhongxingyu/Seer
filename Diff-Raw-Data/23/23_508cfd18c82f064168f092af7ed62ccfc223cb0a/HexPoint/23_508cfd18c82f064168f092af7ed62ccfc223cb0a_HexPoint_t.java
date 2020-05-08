 //----------------------------------------------------------------------------
// $Id$ 
 //----------------------------------------------------------------------------
 
 package hexgui.hex;
 
 import java.lang.Exception;
 import java.lang.NumberFormatException;
 import java.awt.Dimension;
 
 //----------------------------------------------------------------------------
 
 /** A cell on a Hex board. 
     In addition to each playable cell, HexPoints are created for each edge of 
     the board and for some special cases like swap moves, resignations, and
     forfeitures. 
 */
 public final class HexPoint
 {
     /**  Exception. */
     public static class InvalidHexPointException
 	extends Exception
     {
 	public InvalidHexPointException(String message)
 	{
 	    super("Invalid point: " + message);
 	}
     }
 
     public static final HexPoint NORTH;
     public static final HexPoint SOUTH;
     public static final HexPoint WEST;
     public static final HexPoint EAST;
     public static final HexPoint SWAP_SIDES;
     public static final HexPoint SWAP_PIECES;
     public static final HexPoint RESIGN;
     public static final HexPoint FORFEIT;
 
     public static final int MAXSIZE = 25;
 
     static 
     {
 	createPoints(MAXSIZE, MAXSIZE);
 	NORTH       = new HexPoint(   0, -100, "north");
 	SOUTH       = new HexPoint(   0,  100, "south");
 	WEST        = new HexPoint(-100,    0, "west");
 	EAST        = new HexPoint( 100,    0, "east");
 	SWAP_SIDES  = new HexPoint( 200,  200, "swap-sides"); 
 	SWAP_PIECES = new HexPoint( 300,  300, "swap-pieces");
 	RESIGN      = new HexPoint( 400,  400, "resign");
 	FORFEIT     = new HexPoint( 500,  500, "forfeit");
     }
 
     /** Returns the point with the given coordinates.
 	Note that it is not possible to obtain points for board edges and
 	special moves with this method.  Use the <code>get(String)</code> method
 	for these types of points.
 	@param x x-coordinate of point
 	@param y y-coordinate of point
 	@return point with coordinates (x,y). 
     */
     public static HexPoint get(int x, int y)
     {
 	assert(x >= 0);
 	assert(y >= 0);
 	assert(x < MAXSIZE);
 	assert(y < MAXSIZE);
 	return s_points[x][y];
     }
     
     /** Returns the point with the given string represention.
 	Valid special moves include: "north", "south", "east", "west" 
 	"swap-sides", "swap-pieces", "resign", and "forfeit". 
 	@param name The name of the point to return
 	@return the point or <code>null</code> if <code>name</code> is invalid.
     */
     public static HexPoint get(String name) 
     {
 	if (name.equals("north")) return NORTH;
 	else if (name.equals("south")) return SOUTH;
 	else if (name.equals("west")) return WEST;
 	else if (name.equals("east")) return EAST;
 	else if (name.equals("swap-sides")) return SWAP_SIDES;
 	else if (name.equals("swap-pieces")) return SWAP_PIECES;
 	else if (name.equals("resign")) return RESIGN;
 	else if (name.equals("forfeit")) return FORFEIT;
 	
 	for (int x=0; x<MAXSIZE; x++) 
 	    for (int y=0; y<MAXSIZE; y++) 
 		if (name.equals(s_points[x][y].toString()))
 		    return s_points[x][y];
 
 	assert(false);
 	return null;
     }
 
     /** Returns the string representation of the point. */
     public String toString()
     {
 	return m_string;
     }
 
     private static void createPoints(int width, int height)
     {
 	s_points = new HexPoint[width][height];
 	for (int x=0; x<width; x++) {
 	    for (int y=0; y<height; y++) {
 		String name = "" + (char)('a' + x) + (y+1);
 		s_points[x][y] = new HexPoint(x, y, name);
 	    }
 	}
     }
  
     private static HexPoint s_points[][];
 
     private HexPoint(int x, int y, String name)
     {
 	this.x = x;
 	this.y = y;
 	m_string = name;
     }
 
     public int x, y;
     private String m_string;
 }
 
 //----------------------------------------------------------------------------
