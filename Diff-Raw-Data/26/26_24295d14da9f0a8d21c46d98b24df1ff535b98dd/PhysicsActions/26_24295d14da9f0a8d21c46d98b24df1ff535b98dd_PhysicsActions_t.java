 package edu.vu.vuse.cs278.g3.engine;
 
 import edu.vu.vuse.cs278.g3.model.BusObject;
 import edu.vu.vuse.cs278.g3.model.ObjectManager;
 import edu.vu.vuse.cs278.g3.model.PhysicsObject;
 import edu.vu.vuse.cs278.g3.model.RelationshipManager;
 import edu.vu.vuse.cs278.g3.model.RelationshipTypes;
 
 /**
  * Class that contains inner classes that implement the Runnable interface.
  * All inner classes should implement the Runnable interface and be designed as 
  * one specific action for the PhysicsEngine work queue.
  * @author Matthew Shea
  *
  */
 public class PhysicsActions {
 
 
 	public static class accelerationFrame implements Runnable {
 		@Override
 		public void run() {
 			accelerateBus();
 			moveBus();
 			accelerateObject();
 			updateObjectPosition();
 			PhysicsEngine.getInstance().updateState();
 			loadNextFrame();
 		}
 		
 	}
 	
 	public static class runningFrame implements Runnable {
 
 		@Override
 		public void run() {
 			moveBus();
 			updateObjectPosition();
 			PhysicsEngine.getInstance().updateState();
 			loadNextFrame();
 		}
 		
 	}
 	
 	public static class decelerationFrame implements Runnable {
 
 		@Override
 		public void run() {
 			decelerateBus();
 			moveBus();
 			accelerateObject();
 			updateObjectPosition();
 			PhysicsEngine.getInstance().updateState();
 			loadNextFrame();
 			
 		}
 		
 	}
 	private static void loadNextFrame() {
 		switch(PhysicsEngine.getInstance().getState()) {
 		
 		case PhysicsEngine.ACCELERATION_PHASE:
 			PhysicsEngine.getInstance().addtoQueue(new accelerationFrame());
 			break;
 		case PhysicsEngine.RUNNING_PHASE:
 			PhysicsEngine.getInstance().addtoQueue(new runningFrame());
 			break;
 		case PhysicsEngine.STOPPING_PHASE:
 			PhysicsEngine.getInstance().addtoQueue(new decelerationFrame());
 			break;
 		
 		}
 	}
 
 
 	/**
 	 * The bus itself should never move on the screen. 
 	 * We will simulate movement by adjusting the position of the background.
 	 * @author Matthew Shea
 	 *
 	 */
 	private static void moveBus() {
 		((BusObject)ObjectManager.getInstance().getObject("bus")).moveForward(10);
 	}
 	
 	
 	/**
 	 * Calculates the new speed for the bus based on the current speed and the acceleration.
 	 * @author Matthew Shea
 	 *
 	 */
 	private static void accelerateBus() {
 
 		// Retrieve Bus Object
 		BusObject busObject = (BusObject) ObjectManager.getInstance().getObject("bus");
 		
 		// Update speed of the bus
 		busObject.setSpeed(busObject.getSpeed() + busObject.getAcceleration() /* 1 Frame */);
 		System.out.println(busObject.getSpeed());
 		// Commit changes
 		busObject.commit();
 	
 		
 	}
 	
 	/**
 	 * Calculates the new speed for the bus based on the current speed and the deceleration.
 	 * @author Matthew Shea
 	 *
 	 */
 	private static void decelerateBus() {
 
 		// Retrieve Bus Object
 		BusObject busObject = (BusObject) ObjectManager.getInstance().getObject("bus");
 		
 		// Update speed of the bus
 		busObject.setSpeed(busObject.getSpeed() - busObject.getDeceleration() /* 1 Frame */);
 		
 		System.out.println(busObject.getSpeed());
 		// Commit changes
 		busObject.commit();
 	
 		
 	}
 	
 	/**
 	 * Accelerates the object based on the calculated friction and speed of the object
 	 * @author Matthew Shea
 	 *
 	 */
 	private static void accelerateObject () {
 
 			// Retrieve Object
 			PhysicsObject object = ObjectManager.getInstance().getObject("object");
 			BusObject bus = (BusObject) ObjectManager.getInstance().getObject("bus");
 			
 			// Update speed of object (relative to bus)
 			double opposingFrictionalForce = PhysicsFormulas.frictionalForce(null, object.getMass(), 0.125);
 			double modifiedAcceleration = object.getAcceleration();// - opposingFrictionalForce/object.getMass();
 			object.setSpeed(object.getSpeed() + modifiedAcceleration);
 			System.out.println(object.getSpeed());
 			
 			// Commit changes
 			object.commit();
 		
 	}
 		
 	/**
 	 * Updates the object's position on the netlogo display
 	 * @author Matthew Shea
 	 *
 	 */
 	public static void updateObjectPosition() {
 
 		PhysicsObject object = ObjectManager.getInstance().getObject("object");
 		BusObject bus = (BusObject) ObjectManager.getInstance().getObject("bus");
 		RelationshipTypes relationship = RelationshipManager.getInstance().getRelationship("object", "bus");
 		double upperXBound = 0, lowerXBound = 0;
 		switch(relationship) {
 			
 		case INSIDE_UNRESTRAINED:
 			// set bounds to inside of bus
 			upperXBound = 50;
 			lowerXBound = -100;
 			break;
 		case INSIDE_RESTRAINED:
 			// should not move since object is restrained
 			upperXBound = object.getXCoord();
 			lowerXBound = object.getXCoord();			
 			break;
 		case ABOVE_UNRESTRAINED:
 			// set bounds to edges of screen
			upperXBound = 735;
 			lowerXBound = -175;
 			break;
 		case BEHIND_ATTACHED:
 			// set bounds to back of bus and some x distance of "rope" behind bus
 			upperXBound = -130;
			lowerXBound = -150;
 			break;
 		case FRONT:
 			// set bounds to front of bus and edge of screen
 			upperXBound = 735;
 			lowerXBound = 140;
 			break;
 		}
 		double newObjectLocation = object.getXCoord() + object.getSpeed() - bus.getSpeed();
 		if (newObjectLocation <= lowerXBound)
 		{
			if(relationship == RelationshipTypes.ABOVE_UNRESTRAINED)
			{
				//makes ball fall off of bus
				//do gravity things
			}
 			object.setXCoord(lowerXBound);
 			object.setAcceleration(bus.getAcceleration());
 			object.setSpeed(bus.getSpeed());
 		}
 		else if (newObjectLocation >= upperXBound)
 		{
 			object.setXCoord(upperXBound);
 			object.setAcceleration(- bus.getDeceleration());
 			object.setSpeed(bus.getSpeed());
 		}
 		else
 		{
 			object.setXCoord(newObjectLocation);
 		}
 		object.commit();
 		
 		
 	}
 }
