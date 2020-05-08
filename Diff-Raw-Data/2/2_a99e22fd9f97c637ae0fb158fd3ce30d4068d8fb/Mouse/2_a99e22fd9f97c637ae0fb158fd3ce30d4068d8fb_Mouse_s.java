 package org.ice.io;
 
 import java.awt.GraphicsDevice;
 
 /**
  * User-friendly mouse class. Uses MouseDriver and only exposes methods needed by engine.
  * 
  * @author home
  *
  */
 public class Mouse {
 	
 	public static enum MouseClick { NONE, LEFT, RIGHT, WHEEL };
 
 	private MouseDriver mouseDriver;
 	
 	public Mouse( GraphicsDevice graphicsDevice )
 	{
 		this.mouseDriver = new MouseDriver();
 		graphicsDevice.getFullScreenWindow().addMouseListener( mouseDriver );
 		graphicsDevice.getFullScreenWindow().addMouseMotionListener( mouseDriver );
 	}
 	
 	public MouseClick getMouseButton()
 	{
		final int button = mouseDriver.getMouseButton();
 		
 	    switch(button) 
 		{
 	    	case 0:
 	    		return MouseClick.NONE;
 	    	case 1:	    	
 	    		return MouseClick.LEFT;
 	    	case 2:
 	    		return MouseClick.WHEEL;
 	    	case 3:
 	    		return MouseClick.RIGHT;
 		}
 	    return MouseClick.NONE;
 	}
 	
 	public int getMouseX()
 	{
 		return mouseDriver.getMouseX();
 	}
 	
 	public int getMouseY()
 	{
 		return mouseDriver.getMouseY();
 	}
 }
