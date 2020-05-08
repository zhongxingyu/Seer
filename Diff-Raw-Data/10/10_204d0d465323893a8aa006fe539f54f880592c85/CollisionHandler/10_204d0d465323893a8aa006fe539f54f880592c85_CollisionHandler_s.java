 package handlers;
 
 import java.awt.geom.Point2D;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import listeners.CollisionListener;
 import handleds.Actor;
 import handleds.Collidable;
 import handleds.Handled;
 
 /**
  * A handler that checks collisions between multiple collisionlisteners and 
  * Collidables
  *
  * @author Mikko Hilpinen.
  *         Created 18.6.2013.
  * @warning There might be some inaccuracies if CollidableHandlers are added 
  * to the collidables
  */
 public class CollisionHandler extends LogicalHandler implements Actor
 {
 	// ATTRIBUTES	-----------------------------------------------------
 	
 	private CollidableHandler collidablehandler;
 	
 	
 	// CONSTRUCTOR	-----------------------------------------------------
 	
 	/**
 	 * Creates a new empty collisionhandler
 	 *
 	 * @param autodeath Will the handler die when it runs out of listeners
 	 * @param superhandler Which actorhandler will inform the handler about 
 	 * the act-event (Optional)
 	 */
 	public CollisionHandler(boolean autodeath, ActorHandler superhandler)
 	{
 		super(autodeath, superhandler);
 		
 		// Initializes attributes
 		this.collidablehandler = new CollidableHandler(false, null);
 	}
 	
 	
 	// IMPLEMENTED METHODS	--------------------------------------------
 
 	@Override
 	public void act(double steps)
 	{
 		// Handles the objects normally = checks collisions
 		handleObjects();
 	}
 	
 	@Override
 	protected Class<?> getSupportedClass()
 	{
 		return CollisionListener.class;
 	}
 	
 	@Override
 	protected boolean handleObject(Handled h)
 	{
 		CollisionListener listener = (CollisionListener) h;
 		
 		// Inactive listeners are not counted
 		if (!listener.isActive())
 			return true;
 		
 		Point2D.Double[] colpoints = listener.getCollisionPoints();
 		HashMap<Collidable, ArrayList<Point2D.Double>> collidedpoints = 
 				new HashMap<Collidable, ArrayList<Point2D.Double>>();
 		
 		// Goes throug each point and checks collided objects
 		for (Point2D.Double colpoint : colpoints)
 		{
 			ArrayList<Collidable> collided = 
 					this.collidablehandler.getCollidedObjectsAtPoint((int) 
 					colpoint.getX(), (int) colpoint.getY());
 			// If no collisions were detected, moves on
 			if (collided == null)
 				continue;
 			
 			// If collisions were detected, adds them to the map
 			for (Collidable c : collided)
 			{
				//TODO: Still informs about collisions with self?
				
 				// Objects can't collide with themselves
 				if (c.equals(listener))
 					continue;
 				
 				if (!collidedpoints.containsKey(c))
 					collidedpoints.put(c, new ArrayList<Point2D.Double>());
 				
 				collidedpoints.get(c).add(colpoint);
 			}
 		}
 		
 		// Informs the listener about each object it collided with
 		for (Collidable c: collidedpoints.keySet())
 			listener.onCollision(collidedpoints.get(c), c);
 		
 		return true;
 	}
 	
 	@Override
 	public void kill()
 	{
 		// In addition to the normal killing, kills the collidablehandler as well
 		getCollidableHandler().kill();
 		super.kill();
 	}
 	
 	
 	// GETTERS & SETTERS	----------------------------------------------
 	
 	/**
 	 * @return The collidablehandler that handles the collisionhandler's 
 	 * collision checking
 	 */
 	public CollidableHandler getCollidableHandler()
 	{
 		return this.collidablehandler;
 	}
 	
 	
 	// OTHER METHODS	--------------------------------------------------
 	
 	/**
 	 * Adds a new collisionlistener to the checked listeners
 	 *
 	 * @param c The new collisionlistener
 	 */
 	public void addCollisionListener(CollisionListener c)
 	{
 		super.addHandled(c);
 	}
 	
 	/**
 	 * Adds a new collidable to the list of checkked collidables
 	 *
 	 * @param c The collidable to be added
 	 */
 	public void addCollidable(Collidable c)
 	{
 		this.collidablehandler.addCollidable(c);
 	}
 }
