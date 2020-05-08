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
     public static final HexPoint INVALID;
 
     public static final int MAX_WIDTH  = 16;
     public static final int MAX_HEIGHT = 15;
     public static final int MAX_POINTS = 256;
 
     private static HexPoint s_points[];
 
     static 
     {
 	s_points = new HexPoint[MAX_POINTS];
 
 	for (int x=0; x<MAX_WIDTH; x++) {
 	    for (int y=0; y<MAX_HEIGHT; y++) {
 		String name = "" + (char)('a' + y) + (x+1);
 		s_points[y*MAX_WIDTH+ x] = new HexPoint(x, y, name);
 	    }
 	}
         
         /** NOTE: This must be the same as in wolve, or
          *  the vc display methods in GuiBoard will break. 
          */
 
 	NORTH       = s_points[240] = new HexPoint(240, "north");
 	EAST        = s_points[241] = new HexPoint(241, "east");
 	SOUTH       = s_points[242] = new HexPoint(242, "south");
 	WEST        = s_points[243] = new HexPoint(243, "west");
 
 	SWAP_PIECES = s_points[244] = new HexPoint(244, "swap-pieces");
 	SWAP_SIDES  = s_points[245] = new HexPoint(245, "swap-sides"); 
 	RESIGN      = s_points[246] = new HexPoint(246, "resign");
 	FORFEIT     = s_points[247] = new HexPoint(247, "forfeit");
 
                       s_points[248] = new HexPoint(248, "--");
                       s_points[249] = new HexPoint(249, "--");
                       s_points[250] = new HexPoint(250, "--");
                       s_points[251] = new HexPoint(251, "--");
                       s_points[252] = new HexPoint(252, "--");
                       s_points[253] = new HexPoint(253, "--");
                       s_points[254] = new HexPoint(254, "--");
 
         INVALID     = s_points[255] = new HexPoint(255, "invalid");
     }
 
     /** Returns the point with the given index.
 
 	@param i index of the point. 
 	@return point with index i.
     */
     public static HexPoint get(int i)
     {
 	assert(i >= 0);
 	assert(i < MAX_POINTS);
 	return s_points[i];
     }
 
 
     /** Returns the point with the given coordinates.  Note that it is
 	not possible to obtain points for board edges and special
 	moves with this method.  Use the <code>get(String)</code>
 	method for these types of points.
 
 	@param x x-coordinate of point
 	@param y y-coordinate of point
 	@return point with coordinates (x,y). 
     */
     public static HexPoint get(int x, int y)
     {
 	assert(x >= 0);
 	assert(y >= 0);
 	assert(x < MAX_WIDTH);
 	assert(y < MAX_HEIGHT);
 	return s_points[y*MAX_WIDTH + x];
     }
     
     /** Returns the point with the given string represention.
 	Valid special moves include: "north", "south", "east", "west" 
 	"swap-sides", "swap-pieces", "resign", and "forfeit". 
 	@param name The name of the point to return
 	@return the point or <code>null</code> if <code>name</code> is invalid.
     */
     public static HexPoint get(String name) 
     {
        if (name.equalsIgnoreCase("swap"))
            return SWAP_PIECES;

         for (int x=0; x<MAX_POINTS; x++) 
             if (name.equalsIgnoreCase(s_points[x].toString()))
                 return s_points[x];
         assert(false);
 	return null;
     }
 
     /** Returns the string representation of the point. */
     public String toString()
     {
 	return m_string;
     }
 
     private HexPoint(int p, String name)
     {
         this.x = p & (MAX_WIDTH-1);
         this.y = p / MAX_WIDTH;
         m_string = name;
     }
 
     private HexPoint(int x, int y, String name)
     {
 	this.x = x;
 	this.y = y;
 	m_string = name;
     }
 
     public final int x, y;
     private final String m_string;
 }
 
 //----------------------------------------------------------------------------
