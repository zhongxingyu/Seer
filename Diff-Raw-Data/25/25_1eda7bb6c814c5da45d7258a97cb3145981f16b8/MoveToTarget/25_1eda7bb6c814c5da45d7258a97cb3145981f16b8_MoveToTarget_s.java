 package vooga.towerdefense.action.actionlist;
 
 import vooga.towerdefense.action.Action;
 import vooga.towerdefense.action.TargetedAction;
 import vooga.towerdefense.attributes.Attribute;
 import vooga.towerdefense.util.Location;
 import vooga.towerdefense.util.Vector;
 
 
 /**
  * Action to move from one location to another. If location is undefined, move to target.
  * 
  * @author Matthew Roy
  * @author Xu Rui
  *
  */
 public class MoveToTarget extends TargetedAction{
 	
 	private Vector myHeading;
 	private Location myCenter;
 	private Location myDestination;
 	private Attribute mySpeed;
 
 	public MoveToTarget(Location start, Location destination, Attribute movespeed){
 		mySpeed = movespeed;
 		myCenter = start;
 		myDestination = destination;
 	}
 
 	@Override
 	public void executeAction(double elapsedTime) {
 		if (myDestination == null){
 			myDestination = getTargets().get(0).getCenter();
 		}
 		myHeading = new Vector(Vector.angleBetween(myCenter, myDestination), mySpeed.getValue());
         Vector v = new Vector(myHeading.getDirection(), mySpeed.getValue());
         v.scale(elapsedTime / 1000);
         myCenter.translate(v);
 		if (myCenter.distance(myDestination) < myHeading.getMagnitude()) {
 			setEnabled(false);
 			myCenter.setLocation(myDestination);
 		}
 		else {
 			myCenter.translate(myHeading);
 		}
 	}
 	
 
 }
