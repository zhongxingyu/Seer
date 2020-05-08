 package hu.miracle.workers;
 
 import java.awt.Point;
 import java.util.List;
 
 public class AntEater extends Creature {
 
 	// Members
 	private boolean visible; // Jelen van-e
 	private int hunger;
 	private int wait;
 
 	// Constructor
	public AntEater(Scene scene) {
		super(scene);
 	}
 
 	// Protected methods
 	protected void routeAndMove() {
 		System.out.println(getClass().getCanonicalName() + ".routeAndMove()");
 
 		for (Ant ant : scene.getAnts()) {
 			if(pointInRange(ant.getPosition()) == true) {
 				ant.terminate();
 			}
 		}
 	}
 
 	// Public interface
 	public boolean isVisible() {
 		System.out.println(getClass().getCanonicalName() + ".isVisible()");
 		return visible;
 	}
 
 	public void handleTick() {
 		System.out.println(getClass().getCanonicalName() + ".handleTick()");
 
 		if (visible) {
 			routeAndMove();
 		}
 	}
 
 	@Override
 	public boolean pointInRange(Point point) {
 
 		if (Point.distance(this.position.x, this.position.y, point.x, point.y) < this.radius) {
 			return true;
 		} else {
 			return false;
 		}
 
 	}
 }
