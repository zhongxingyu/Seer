 import java.awt.Graphics;
 import java.awt.Point;
 import java.awt.Rectangle;
 
 public interface Vehicle
 {
 	public static final Point dir[] =
 	{
 		/* {x, y} */
 		new Point(01, 00),	// East
 		new Point(-1, 00),	// West
 		new Point(00, +1),	// South
 		new Point(00, -1)	// North
 	};
 	public static final int DIR_EAST = 0;
	public static final int DIR_WSET = 1;
 	public static final int DIR_SOUTH = 2;
 	public static final int DIR_NORTH = 3;
 	
 	public String getName();
 	public void paintComponent(Graphics g);
 	public void setLocation(int x, int y);
 	public void move(int direction);
 	public Rectangle getBounds();
 }
