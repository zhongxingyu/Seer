 /**
  * Wall class
  * 
  * @author	Jamie of the Javateerz
  */
 
 package org.javateerz.ParkViewProtector;
 
 import org.newdawn.slick.geom.Rectangle;
 
 public class Wall extends Movable
 {
 	public static final int NORMAL=0;
 	public static final int NARROW_H=1;
 	public static final int NARROW_V=2;
 	public static final int SMALL=3;
 	
 	private static final long serialVersionUID = 1L;
 	
 	private int width;
 	private int height;
 	private int type;
 	
 	/**
 	 * Create a new wall
 	 * 
 	 * @param x X-position
 	 * @param y Y-position
 	 * @param w Width
 	 * @param h Height
 	 */
 	public Wall(int t, int x, int y , int w, int h)
 	{
 		super(x, y, 0);
 		
 		width				= w;
 		height				= h;
 		type				= t;
 		
 		updateSprite();
 	}
 	
 	protected void updateSprite()
 	{
 		try
 		{
 			sprite				= DataStore.INSTANCE.getSprite("wall_" + type + ".png");
 		}
 		catch(Exception e)
 		{
 			sprite				= DataStore.INSTANCE.getSprite("wall_0.png");
 		}
 	}
 	
 	protected void updateSprite(int x)
 	{
 		try
 		{
 			sprite				= DataStore.INSTANCE.getSprite("wall_" + type + "x.png");
 		}
 		catch(Exception e)
 		{
 			updateSprite();
 		}
 	}
 	
 	/**
 	 * Called by main game loop, draws the object's sprite on the screen
 	 */
 	public void draw()
 	{
 		int spriteHeight	= sprite.getHeight() - 1;
 		int spriteWidth		= sprite.getWidth() - 1;
 		
 		for(int i = 0; i < width; i ++)
 		{
 			for(int j = 0; j < height; j ++)
 			{
				/*if(j+1>=height)
 					updateSprite(1);
 				else
					updateSprite();*/
 				sprite.draw((int) x + i * spriteWidth, (int) y + j * spriteHeight);
 			}
 		}
 	}
 	
 	/**
 	 * Computes the bounding box for the object
 	 * 
 	 * @return Bounding box
 	 */
 	public Rectangle getBounds()
 	{
 		Rectangle ret		= new Rectangle((int) x, (int) y,
 				width*sprite.getWidth(), height*sprite.getHeight());
 		
 		return ret;
 	}
 }
