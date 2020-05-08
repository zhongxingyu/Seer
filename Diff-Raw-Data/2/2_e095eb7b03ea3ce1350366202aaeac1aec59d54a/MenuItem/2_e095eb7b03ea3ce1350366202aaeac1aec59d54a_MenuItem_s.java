 /**
  * Game menu item
  * 
  * @author	James Schwinabart
  */
 
 import java.awt.*;
 import javax.swing.*;
 
 public class MenuItem extends JLabel
 {
 	private String label;
 	
 	/**
 	 * Create a new menu item with the specified label
 	 * 
 	 * @param name
 	 */
 	public MenuItem(String label)
 	{
 		this.label			= label;
 	}
 	
 	/**
 	 * Get the bounds of the menu item
 	 * 
 	 * @param g Graphics context
 	 * @return Bounding box
 	 */
 	public Rectangle getBounds(Graphics g)
 	{
 		int width			= g.getFontMetrics().stringWidth(label);
 		int height			= g.getFontMetrics().getHeight();
 		
 		return new Rectangle(width, height);
 	}
 	
 	/**
 	 * Draw the menu item on the graphics context
 	 *  
 	 * @param g Graphics context
 	 * @param x X position
 	 * @param y Y position
 	 */
 	public void draw(Graphics g, int x, int y)
 	{
 		// center the string
 		int width			= g.getFontMetrics().stringWidth(label);
 		x				   -= width / 2;
 		
		g.drawString(name, x, y);
 	}
 }
