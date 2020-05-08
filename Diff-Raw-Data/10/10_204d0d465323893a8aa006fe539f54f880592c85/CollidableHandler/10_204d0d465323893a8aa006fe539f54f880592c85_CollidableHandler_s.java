 package handlers;
 
 import java.util.ArrayList;
 
 import handleds.Collidable;
 import handleds.Handled;
 
 /**
  * This class handles multiple collidables. Any collision checks made for this 
  * object are made for all the collidables.
  *
  * @author Mikko Hilpinen.
  *         Created 18.6.2013.
  */
 public class CollidableHandler extends Handler
 {	
 	// CONSTRUCTOR	------------------------------------------------------
 	
 	/**
 	 * Creates a new empty collidablehandler
 	 *
 	 * @param autodeath Will the handler die if it runs out of handleds
 	 * @param superhandler The collidablehandler that holds the handler
 	 */
 	public CollidableHandler(boolean autodeath, CollidableHandler superhandler)
 	{
 		super(autodeath, superhandler);
 	}
 	
 	
 	// IMPLEMENTED METHODS	----------------------------------------------
 	
 	@Override
 	protected Class<?> getSupportedClass()
 	{
 		return Collidable.class;
 	}
 	
 	@Override
 	protected boolean handleObject(Handled h)
 	{
 		// All handling is done via operators
 		return false;
 	}
 	
 	
 	// OTHER METHODS	-----------------------------------------------------
 	
 	/**
 	 * Adds a new collidable to the list of collidables
 	 *
 	 * @param c The collidable to be added
 	 */
 	public void addCollidable(Collidable c)
 	{
 		addHandled(c);
 	}
 
 	/**
 	 * Returns all the collided objects the collidablehandler handles that 
 	 * collide with the given point
 	 *
 	 * @param x The x-coordinate of the collision point
 	 * @param y The y-coordinate of the collision point
 	 * @return A list of objects colliding wiht the point or null if no object 
 	 * collided with the point
 	 */
 	public ArrayList<Collidable> getCollidedObjectsAtPoint(int x, int y)
 	{
 		// Initializes the operator
 		CollisionCheckOperator checkoperator = new CollisionCheckOperator(x, y);
 		
 		// Checks collisions through all collidables
 		handleObjects(checkoperator);
 		
 		// If there wasn't collided objects, forgets the data and returns null
 		if (checkoperator.getCollidedObjects() == null)
 			return null;
 		
 		// Returns a list of collided objects (no need for cloning since 
 		// operator is not an attribute)
 		return checkoperator.getCollidedObjects();
 	}
 	
 	/**
 	 * Goes through the collidables and makes them solid
 	 */
 	public void makeCollidablesSolid()
 	{
 		// Tries to make all of the collidables solid
 		handleObjects(new MakeSolidOperator());
 	}
 
 	/**
 	 * Goes through the collidables and makes the unsolid
 	 */
 	public void makeCollidablesUnsolid()
 	{
 		// Tries to make all of the collidables solid
 		handleObjects(new MakeUnsolidOperator());
 	}
 	
 	
 	// SUBCLASSES	------------------------------------------------------
 	
 	private class MakeSolidOperator extends HandlingOperator
 	{
 		@Override
 		protected boolean handleObject(Handled h)
 		{
 			((Collidable) h).makeSolid();
 			return true;
 		}
 	}
 	
 	private class MakeUnsolidOperator extends HandlingOperator
 	{
 		@Override
 		protected boolean handleObject(Handled h)
 		{
 			((Collidable) h).makeUnsolid();
 			return true;
 		}	
 	}
 	
 	private class CollisionCheckOperator extends HandlingOperator
 	{
 		// ATTRIBUTES	-------------------------------------------------
 		
 		private ArrayList<Collidable> collided;
 		private int checkx, checky;
 		
 		
 		// CONSTRUCTOR	-------------------------------------------------
 		
 		public CollisionCheckOperator(int x, int y)
 		{
 			this.checkx = x;
 			this.checky = y;
 			this.collided = new ArrayList<Collidable>();
 		}
 		
 		
 		// IMPLEMENTED METHODS	-----------------------------------------
 		
 		@Override
 		protected boolean handleObject(Handled h)
 		{	
 			Collidable c = (Collidable) h;
 			
 			// Non-solid objects can't collide
 			if (!c.isSolid())
 				return true;
 			
 			// Checks the collision
 			if (!c.pointCollides(this.checkx, this.checky))
 				return true;
 				
 			// Adds the collided object to the list
 			this.collided.add(c);
 			
			return false;
 		}
 		
 		
 		// GETTERS & SETTERS	-----------------------------------------
 		
 		public ArrayList<Collidable> getCollidedObjects()
 		{
 			return this.collided;
 		}
 	}
 }
