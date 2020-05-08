 /*
  * A re-implementation of the classic C=64 game 'Thrust'.
  *
  * @author "Joe Kiniry (kiniry@acm.org)"
  * @module "COMP 20050, COMP 30050"
  * @creation_date "March 2007"
  * @last_updated_date "April 2008"
  * @keywords "C=64", "Thrust", "game"
  */
 
 package thrust.entities;
 
 /**
  * Entities whose position and orientation do not change.
  * @author Joe Kiniry (kiniry@acm.org)
  * @author Neil McCarthy (neil.mccarthy@ucdconnect.ie)
  * @version 28 April 2008
  */
 public class StaticEntity extends DynamicEntity {
   //@ public model boolean initialized;
   //@ public initially initialized == false;
   /**
    * Set the position and orientation of this entity.  You may only
    * call this method once ever per StaticEntity object.
    * @param the_position the immutable position.
    * @param the_orientation the immutable orientation.
    */
   //@ requires !initialized;
   //@ ensures position()[0] == the_position[0];
   //@ ensures position()[1] == the_position[1];
   //@ ensures orientation() == the_orientation;
   //@ ensures initialized;
  /** The position of this entity. */
  private double[] my_position;
  /** The orientation of this entity. */
  private double my_orientation;
  
   public void set_state(final double[] the_position,
                         final double the_orientation) {
    my_position = the_position;
    my_orientation = the_orientation;
   }
 
   //@ public invariant (* All queries are constant. *);
   //@ public constraint initialized ==> (position() == \old(position()));
   //@ public constraint initialized ==> (orientation() == \old(orientation()));
 
   /*@ public invariant (* Mass, velocity, acceleration, and momentum
     @                     are all zero. *);
     @*/
 }
