 package tests;
 
 import java.awt.Graphics2D;
 
 import handlers.DrawableHandler;
 import handlers.MouseListenerHandler;
 import helpAndEnums.DepthConstants;
 import drawnobjects.DrawnObject;
 import listeners.AdvancedMouseListener;
 
 /**
  * This class draws a rectangle around the mouse position to check where the 
  * game thinks the mouse is
  *
  * @author Mikko Hilpinen.
  *         Created 30.11.2013.
  */
 public class GraphicalMousePositionTest extends DrawnObject implements AdvancedMouseListener
 {
 	// CONSTRUCTOR	-----------------------------------------------------
 	
 	/**
 	 * Creates a new test object
 	 *
 	 * @param drawer The drawer that will draw the object
 	 * @param mousehandler The mousehandler that will inform the object about 
 	 * mouse status
 	 */
 	public GraphicalMousePositionTest(DrawableHandler drawer, 
 			MouseListenerHandler mousehandler)
 	{
 		super(0, 0, DepthConstants.NORMAL, drawer);
 		
 		// Adds teh object to the handler(s)
 		if (mousehandler != null)
 			mousehandler.addMouseListener(this);
 	}
 	
 	
 	// IMPLEMENTED METHODS	---------------------------------------------
 
 	@Override
 	public boolean isActive()
 	{
 		return true;
 	}
 
 	@Override
 	public void activate()
 	{
 		// Unnecessary in this context
 	}
 
 	@Override
 	public void inactivate()
 	{
 		// Unnecessary in this context
 	}
 
 	@Override
 	public boolean isDead()
 	{
 		return false;
 	}
 
 	@Override
 	public void kill()
 	{
 		// Unnecessary in this context
 	}
 
 	@Override
 	public void onLeftDown(int mouseX, int mouseY)
 	{
 		// Prints the difference between the given values
		System.out.println("Mouse position difference: " + (getX() - mouseX) + 
				", " + (getY() - mouseY));
 	}
 
 	@Override
 	public void onRightDown(int mouseX, int mouseY)
 	{
 		// Unnecessary in this context
 	}
 
 	@Override
 	public void onLeftPressed(int mouseX, int mouseY)
 	{
		// Unnecessary in this context
 	}
 
 	@Override
 	public void onRightPressed(int mouseX, int mouseY)
 	{
 		// Unnecessary in this context
 	}
 
 	@Override
 	public void onLeftReleased(int mouseX, int mouseY)
 	{
 		// Unnecessary in this context
 	}
 
 	@Override
 	public void onRightReleased(int mouseX, int mouseY)
 	{
 		// Unnecessary in this context
 	}
 
 	@Override
 	public boolean listensPosition(int x, int y)
 	{
 		return true;
 	}
 
 	@Override
 	public boolean listensMouseEnterExit()
 	{
 		return false;
 	}
 
 	@Override
 	public void onMouseEnter(int mouseX, int mouseY)
 	{
 		// Unnecessary in this context
 	}
 
 	@Override
 	public void onMouseOver(int mouseX, int mouseY)
 	{
 		// Unnecessary in this context
 	}
 
 	@Override
 	public void onMouseExit(int mouseX, int mouseY)
 	{
 		// Unnecessary in this context
 	}
 
 	@Override
 	public void onMouseMove(int mouseX, int mouseY)
 	{
 		// Changes the object's position
 		setPosition(mouseX, mouseY);
 	}
 
 	@Override
 	public int getOriginX()
 	{
 		return 25;
 	}
 
 	@Override
 	public int getOriginY()
 	{
 		return 25;
 	}
 
 	@Override
 	public void drawSelfBasic(Graphics2D g2d)
 	{
 		// Draws a rectangle
 		g2d.drawRect(0, 0, 50, 50);
 	}
 }
