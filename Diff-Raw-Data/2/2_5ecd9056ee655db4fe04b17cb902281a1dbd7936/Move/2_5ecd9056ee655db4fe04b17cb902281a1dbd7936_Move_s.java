 //----------------------------------------------------------------------------
// $Id: 
 //----------------------------------------------------------------------------
 
 package hexgui.hex;
 
 //----------------------------------------------------------------------------
 
 /** Move.
     Contains a HexPoint and a HexColor.  To construct a swap move use the
     appropriate HexPoint. 
 */
 public final class Move
 {
     /** Constructs a move with <code>null</code> move and color. */
     public Move()
     {
 	m_point = null;
     	m_color = null;
     }
 
     /** Constructs a move with the given point and color.
 	@param p location of move
 	@param c black or white.
     */
     public Move(HexPoint p, HexColor c)
     {
 	m_point = p;
 	m_color = c;
     }
 
     /** Returns the point of this move. 
 	@return HexPoint of the location.
     */
     public HexPoint getPoint()
     {
 	return m_point;
     }
 
     /** Returns the color of the move.
 	@return HexColor of the move (WHITE or BLACK).
     */
     public HexColor getColor()
     {
 	return m_color;
     }
 
     /** Returns a string representation of the move.
 	@return String of the form "[color, point]".
     */
     public String toString()
     {
 	return toString(m_point, m_color);
     }
 
     private static String toString(HexPoint p, HexColor c)
     {
 	if (p == null || c == null) 
 	    return "[null]";
 
 	return "[" + c.toString() + ", " + p.toString() + "]";
     }
 
     private HexPoint m_point;
     private HexColor m_color;
 }
 
 //----------------------------------------------------------------------------
