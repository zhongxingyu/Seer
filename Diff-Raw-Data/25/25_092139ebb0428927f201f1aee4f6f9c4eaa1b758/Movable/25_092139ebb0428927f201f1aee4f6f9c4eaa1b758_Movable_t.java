 package fr.iutvalence.java.projet.spaceinvaders;
 
 // TODO Move XGridMax and YGridMax
 // TODO Exception move when it go over.
 /**
  * This class defines a movable object, that can move on a 2D grid and so being killed by others.<br/>
  * A movable object is characterized by its size and position on the grid and also its status [alive | dead].
  * Once a movable object is touched by another one, it's killed.
  * The bounds of the 2D grid is defined in ....(SpaceInvaders for the moment), and movable objects can't move over without exception (not now). 
  * @author Gallet Guyon
  */
 public class Movable extends Element
 { 
 	/**
 	 * Status indicating if the movable object is still alive (a dead movable can no longer move).
 	 */
 	private boolean alive;	
 	
 	/**
 	 * The default height of the movable on the grid.<br/>
 	 */
 	private static final int HEIGHT = 10;
 	
 	/**
 	 * The default width of the movable on the grid.<br/>
 	 */
 	private static final int WIDTH = 10;
 	
 	/**
 	 * This constructor creates a new living <tt>Movable</tt> object taking its coordinates.<br/>
 	 * Size is set by the default couple of (10,10)
 	 * 
 	 * @param i the initial position, as a Coordinate object  
 	 */
 	public Movable(Coordinates i)
 	{
 		super(new BoundingBox(i,new Coordinates(WIDTH,HEIGHT)));
 		this.alive = true;
 	}
 	
 	/**
 	 * This constructor creates a new living <tt>Movable</tt> object taking its coordinates and size as parameters.
 	 * 
 	 * @param i the initial position, as a Coordinate object 
 	 * @param j the size, as a Coordinate object whose x means width and y means height. 
 	 */
 	public Movable(Coordinates i, Coordinates j)
 	{
 		super(new BoundingBox(i,j));
 		this.alive = true;
 	}
 
 	/**
 	 * This method returns if the movable object is still alive
 	 * @return the life status
 	 */
 	public boolean isAlive()
 	{
 		return this.alive;
 	}
 
 	/**
 	 * This method allows to modify the alive status of the movable object
 	 * @param alive the new alive status
 	 */
 	public void setAlive(boolean alive)
 	{
 		this.alive = alive;
 	}
 
 	// FIXME take into account the fact that the destination can be out-of-bounds (exception)
 	/**
 	 * The move method changes the position of the object (the size is unchanged).<br/>
 	 * It translates the coordinates by deltas given as x and y.<br/>
 	 * 
 	 * @param dx new coordinate relative on x axis
 	 * @param dy new coordinate relative on y axis
 	 */
 	public void move(int dx, int dy)
 	{
 		if(this.alive)
 		{
 			this.setArea(
 					new BoundingBox(
 							new Coordinates(
 									this.getArea().getPosition().getX() + dx,
 									this.getArea().getPosition().getY() + dy)
 							,this.getArea().getSize()));
 		}
 	}
 
 	@Override
 	public String toString()
 	{
 		return "Movable [alive=" + this.alive + ", position=" + this.getArea().getPosition() + ", taille=" + this.getArea().getSize() + "]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$//$NON-NLS-4$
 	}
 }
