 /**
  * Class to handle everything associated with things that can move including characters and
  * attacks
  *
  * @author	Jamie of the Javateerz
  */
 
 import java.awt.Graphics;
 import java.awt.Rectangle;
 
 public abstract class Movable
 {
 	protected double speed;
 	
 	// placement
 	protected int x;
 	protected int y;
 	
 	// direction (0 = north, 1 = east, 2 = south, 3 = west)
 	protected int direction		= 2;
 	
 	protected Sprite sprite;
 	protected int moveCount;
 	
 	/**
 	 * Constructor
 	 * 
 	 * @param x
 	 * @param y
 	 * @param speed
 	 */
 	public Movable(int x, int y, double speed)
 	{
 		this.x		= x;
 		this.y		= y;
 		
 		this.speed	= speed;
 		
 		this.sprite	= DataStore.INSTANCE.getSprite("images/placeholder.png");
 	}
 	
 	/**
 	 * Returns the speed of the object
 	 * 
 	 * @return Speed
 	 */
 	public double getSpeed()
 	{
 		return speed;
 	}
 	
 	/**
 	 * Changes the speed of the object
 	 * 
 	 * @param change New speed
 	 */
 	public void changeSpeed(double change)
 	{
 		speed = change;
 	}
 	
 	/**
 	 * Moves the object a distance (which is multiplied by speed)
 	 * 
 	 * @param distX X component
 	 * @param distY Y component
 	 */
 	public void move(int distX, int distY)
 	{
 		// determine and change direction if necessary
		switch(direction)
		{
			case Direction.NORTH:
				if(distY > 0)	direction	= 2;
				break;
			
			case Direction.EAST:
				if(distX < 0)	direction	= 3;
				break;
				
			case Direction.SOUTH:
				if(distY < 0)	direction	= 0;
				break;
			
			case Direction.WEST:
				if(distX < 0)	direction	= 1;
				break;
		}
 		
 		x		   += (int) Math.round(distX * speed);
 		y		   += (int) Math.round(distY * speed);
 		
 		moveCount++;
 	}
 	
 	/**
 	 * Moves the object a distance (which is multiplied by speed) in the current direction
 	 * 
 	 * @param dist distance
 	 */
 	public void move(int distance)
 	{
 		int dist		= (int) Math.round(distance * speed);
 		
 		// determine and change direction if necessary
 		switch(direction)
 		{
 			case Direction.NORTH:
 				y		-= dist;
 				break;
 			
 			case Direction.EAST:
 				x		+= dist;
 				break;
 				
 			case Direction.SOUTH:
 				y		+= dist;
 				break;
 			
 			case Direction.WEST:
 				x		-= dist;
 				break;
 		}
 			
 		moveCount++;
 	}
 	
 	/**
 	 * Changes the direction that the object is facing
 	 * 
 	 * @param int direction
 	 */
 	public void setDirection(int dir)
 	{
 		direction	= dir;
 	}
 	
 	/**
 	 * Returns the direction that the object is facing
 	 * 
 	 * @return
 	 */
 	public int getDirection()
 	{
 		return direction;
 	}
 	
 	/**
 	 * Returns the number of moves made by the object
 	 * 
 	 * @return
 	 */
 	public int getMoveCount()
 	{
 		return moveCount;
 	}
 	
 	/**
 	 * Resets the move counter of the object
 	 */
 	public void resetMoveCount()
 	{
 		moveCount	= 0;
 	}
 	
 	/**
 	 * Called by main game loop, draws the object's sprite on the screen
 	 */
 	public void draw(Graphics g)
 	{
 		sprite.draw(g, x, y);
 	}
 	
 	/**
 	 * Gets the bounds of the object
 	 * 
 	 * @return Bounding box
 	 */
 	public Rectangle getBounds()
 	{
 		Rectangle rect			= new Rectangle(x, y, sprite.getWidth(), sprite.getHeight());
 		
 		return rect;
 	}
 }
 
 class Direction
 {
 	public static final int NORTH	= 0;
 	public static final int EAST	= 1;
 	public static final int SOUTH	= 2;
 	public static final int WEST	= 3;
 }
