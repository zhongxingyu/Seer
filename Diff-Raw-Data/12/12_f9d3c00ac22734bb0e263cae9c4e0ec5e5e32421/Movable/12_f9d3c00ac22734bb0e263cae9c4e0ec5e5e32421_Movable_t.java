 package fr.iutvalence.java.projet.spaceinvaders;
 
 /**
  * This class defines a movable object, that can move on a 2D grid and so being killed by others.<br/>
  * A movable object is characterized by its size and position on the grid and also its status [alive | dead]. Once a
  * movable object is touched by another one, it's killed. The bounds of the 2D grid is defined in SpaceInvaders for, 
  * and movable objects can't move over without exception.
  * 
  * @author Gallet Guyon
  */
 public class Movable extends Element
 {
 	/**
 	 * The default height of the movable on the grid.<br/>
 	 */
 	private static final int HEIGHT = 10;
 
 	/**
 	 * The default width of the movable on the grid.<br/>
 	 */
 	private static final int WIDTH = 10;
 
 	/**
 	 * Status indicating if the movable object is still alive (a dead movable can no longer move).
 	 */
 	private boolean alive;
 
 	/**
 	 * Set the direction of shoot. Negative -> down, Positive -> up, Equals to 0 means it's not a shoot.
 	 * More it's big number, more it's quick.
 	 */
 	private int direction;
 
 	// *************** Constructor *****************
 
 	/**
 	 * This constructor creates a new living <tt>Movable</tt> object taking its coordinates.<br/>
 	 * Size is set by the default couple of (10,10)
 	 * 
 	 * @param i
 	 *            the initial position, as a Coordinate object
 	 * @throws NegativeSizeException
 	 *             Throws Exception when the size coordinates are negative
 	 */
 	public Movable(Coordinates i) throws NegativeSizeException
 	{
 		super(new BoundingBox(i, new Coordinates(WIDTH, HEIGHT)));
 		this.alive = true;
 		this.direction = 0;
 	}
 
 	/**
 	 * This constructor creates a new living <tt>Movable</tt> object taking its coordinates and size as parameters.
 	 * 
 	 * @param i
 	 *            the initial position, as a Coordinate object
 	 * @param j
 	 *            the size, as a Coordinate object whose x means width and y means height.
 	 * @throws NegativeSizeException
 	 *             Throws Exception when the size coordinates are negative
 	 */
 	public Movable(Coordinates i, Coordinates j) throws NegativeSizeException
 	{
 		super(new BoundingBox(i, j));
 		this.alive = true;
 		this.direction = 0;
 	}
 	
 	/**
 	 * This constructor creates a new living <tt>Movable</tt> object taking its coordinates and size as parameters.
 	 * 
 	 * @param i
 	 *            the initial position, as a Coordinate object
 	 * @param j
 	 *            the size, as a Coordinate object whose x means width and y means height.
 	 * @param direction 
 	 * 			  the direction of the movable object (shoot). For non shoot it sets to 0. for others negative is for down shoot and positive othewise.
 	 * @throws NegativeSizeException
 	 *             Throws Exception when the size coordinates are negative
 	 */
 	public Movable(Coordinates i, Coordinates j, int direction) throws NegativeSizeException
 	{
 		super(new BoundingBox(i, j));
 		this.alive = true;
 		this.direction = direction;
 	}
 
 	// *************** Method *****************
 
 	/**
 	 * The move method changes the position of the object (the size is unchanged).<br/>
 	 * It translates the coordinates by deltas given as x and y.<br/>
 	 * 
 	 * @param dx
 	 *            new coordinate relative on x axis
 	 * @param dy
 	 *            new coordinate relative on y axis
 	 * @throws NegativeSizeException
 	 *             If position is negative.
 	 */
 	public synchronized void move(int dx, int dy) throws NegativeSizeException
 	{
 		if (this.alive)
 		{
 			this.setArea(getArea().translate(new Coordinates(dx, dy)));
 		}
 	}
 
 	/**
 	 * The move method changes the position of the object (the size is unchanged).<br/>
 	 * It translates the coordinates by deltas given as x and y.<br/>
 	 * 
 	 * @param delta
 	 *             new Coordinates
 	 * @throws NegativeSizeException
 	 *             If position is negative.
 	 */
 	public synchronized void move(Coordinates delta) throws NegativeSizeException
 	{
 		if (this.alive)
 		{
 			this.setArea(getArea().translate(delta));
 		}
 	}
 	
 	/**
 	 * Method to move element to specific coordinates on the 2D grid.
 	 * 
 	 * @param x
 	 *            the X coordinates to move the element.
 	 * @param y
 	 *            the Y coordinates to move the element.
 	 * @throws NegativeSizeException
 	 *             If position is negative.
 	 */
 	public synchronized void moveTo(int x, int y) throws NegativeSizeException
 	{
 		if (this.alive)
 		{
 			getArea().moveTo(new Coordinates(x, y));
 		}
 	}
 	
 	/**
 	 * Method to move element to specific coordinates on the 2D grid.
 	 * 
 	 * @param couple The new coordinates to move
 	 * @throws NegativeSizeException
 	 *             If position is negative.
 	 */
 	public synchronized void moveTo(Coordinates couple) throws NegativeSizeException
 	{
 		if (this.alive)
 		{
 			getArea().moveTo(couple);
 		}
 	}
 	
 	/**
 	 * Method to create new movable object who moves by itself in given direction [UP|DOWN]
 	 * @param direction The direction of the shoot (if it negative the direction is down, up otherwise).
 	 * 					 If it's equal to 0 The function return null.
 	 * @param size This defined the size of the movable object to create
 	 * @return Return new shoot (Movable) or null if it failed.
 	 * @throws NegativeSizeException If the size given is negative
 	 */
 	public synchronized Movable fire(int direction, Coordinates size) throws NegativeSizeException
 	{
 		if(direction != 0)
 		{
 			Coordinates coorShoot = null;
 			
 			if(direction < 0)
 			{
 				coorShoot = new Coordinates((this.getArea().getPosition().getX() + (this.getArea().getSize().getX() / 2)) - (size.getX() / 2)
												,(this.getArea().getPosition().getY() - size.getY()));
 			}
 			else if(direction > 0)
 			{
 				coorShoot = new Coordinates((this.getArea().getPosition().getX() + (this.getArea().getSize().getX()) / 2) - (size.getX() / 2)
												,(this.getArea().getPosition().getY() + this.getArea().getSize().getX()));
 			}
 			return new Movable(coorShoot, size, direction);
 		}
 		return null;
 	}
 
 	// *************** Getters and Setters *****************
 
 	/**
 	 * This method returns if the movable object is still alive
 	 * 
 	 * @return the life status
 	 */
 	public synchronized boolean isAlive()
 	{
 		return this.alive;
 	}
 
 	/**
 	 * This method allows to modify the alive status of the movable object
 	 * 
 	 * @param alive
 	 *            the new alive status
 	 */
 	public synchronized void setAlive(boolean alive)
 	{
 		this.alive = alive;
 	}
 	
 	
 
 	/**
 	 * Return the direction of the shoot.
 	 * @return the direction of the shoot.
 	 */
 	public int getDirection()
 	{
 		return this.direction;
 	}
 
 	/**
 	 * Set the direction of the shoot.
 	 * @param direction The direction to set
 	 */
 	public void setDirection(int direction)
 	{
 		this.direction = direction;
 	}
 
 	@Override
 	public String toString()
 	{
 		return "Movable [alive=" + this.alive + ", position=" + this.getArea().getPosition() + ", taille=" + this.getArea().getSize() + " direction=" + this.direction + "]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$//$NON-NLS-4$"
 	}
 }
