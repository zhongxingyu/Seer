 package org.osgcc.osgcc5.soapydroid.physics;
 
 import org.osgcc.osgcc5.soapydroid.things.CollidableThing;
 
 public interface CollisionHandler {
 	
 	/**
 	 * Method to handle collisions. Should change the velocity attributes of 
 	 * thing1 and thing2.
 	 * 
 	 * @param thing1, the object moved by the player
 	 * @param thing2, objects falling from sky
 	 * @param onTop 1 = thing1 on top, 2 = thing2 on top, 0 = neither on top (strictly horizontal collision)
 	 * @param onLeft 1 = thing1 on left, 2 = thing2 on left, 0 = neither on left (strictly vertical collision)
 	 */
	public void collision(CollidableThing thing1, CollidableThing thing2,
			int onTop, int onLeft);
 	
 	
 }
