 package game;
 
 /**
  * @author Hung <hnl5010@psu.edu>
  * @author Endrit <eqa5029@psu.edu>
  * @author max <maxdeliso@gmail.com>
  *
  */
 
 import java.awt.Canvas;
 import java.awt.Cursor;
 import java.awt.Dimension;
 import java.awt.Point;
 import java.awt.Toolkit;
 import java.awt.image.BufferedImage;
 
 public class GameCanvas extends Canvas {
 	private static final long serialVersionUID = -8443065392838310022L;
	public static final Dimension defaultDimension = new Dimension(800,494);  
 	
 	/**
 	 * The game canvas constructor
 	 */
 	public GameCanvas()
 	{
 		setPreferredSize( defaultDimension );
 	}
 	
 	/**
 	 * A function to hide the cursor
 	 */
 	public void hideCursor()
 	{
 		Game.log("hiding cursor");
 		
 		/* Transparent 16 x 16 pixel cursor image. */
 		BufferedImage cursorImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
 
 		/* Create a new blank cursor. */
 		Cursor blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(
 		    cursorImg, new Point(0, 0), "blank cursor");
 
 		/* Set the blank cursor to the JFrame. */
 		setCursor(blankCursor);
 	}
 	
 	/**
 	 * A function to show the cursor
 	 */
 	public void showCursor()
 	{
 		Game.log("showing cursor");
 		setCursor( Cursor.getDefaultCursor() );
 	}
 	
 	public Dimension getDimension()
 	{
 		return getSize();
 	}
 }
