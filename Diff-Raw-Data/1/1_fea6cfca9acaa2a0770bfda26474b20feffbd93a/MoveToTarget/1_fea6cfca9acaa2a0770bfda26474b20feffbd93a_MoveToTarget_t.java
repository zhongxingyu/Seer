 package vooga.towerdefense.action.movement;
 
 import vooga.towerdefense.action.TargetedAction;
 import vooga.towerdefense.attributes.Attribute;
 import util.Location;
 import util.Vector;
 
 
 /**
  * Action to move from one location to another location, or to a target. If
  * followTarget is true, projectile follows target, else it moves in straight line
  * in target's direction.
  * 
  * @author Xu Rui
  * 
  */
 public class MoveToTarget extends TargetedAction {
 
     private Vector myHeading;
     private Location myCenter;
     private Location myDestination;
 
     private Attribute mySpeed;
     private boolean isTargetTracking;
     private boolean headingSet;
 
     public MoveToTarget (Location start, Location destination,
                          Attribute movespeed, boolean followTarget) {
         super();
         mySpeed = movespeed;
         myCenter = start;
         myDestination = destination;
         isTargetTracking = followTarget;
         headingSet = false;
     }
     
     public void update(double elapsedTime) {
         if (isEnabled()) {
             executeAction(elapsedTime);
         }
         else {
             updateFollowUpActions(elapsedTime);
         }
     }
 
     @Override
     public void executeAction (double elapsedTime) {    
         setFollowTarget(isTargetTracking);
         Vector v = new Vector(myHeading.getDirection(), mySpeed.getValue());
         v.scale(elapsedTime / 1000);
         myCenter.translate(v);
         if (myCenter.distance(myDestination) < myHeading.getMagnitude()) {
             isTargetTracking = false;
             setEnabled(false);
             myCenter.setLocation(myDestination);
         }
         else {
             myCenter.translate(myHeading);
         }
     }
 
     /**
      * Set whether projectile follows path of target, or shoots off in initial
      * target direction.
      * 
      * @param follow
      */
     public void setFollowTarget (boolean follow) {
         if (headingSet && !follow) {
             return;
         }
         else {
             myHeading =
                     new Vector(Vector.angleBetween(myDestination, myCenter), mySpeed.getValue());
             headingSet = true;
         }
     }
 }
