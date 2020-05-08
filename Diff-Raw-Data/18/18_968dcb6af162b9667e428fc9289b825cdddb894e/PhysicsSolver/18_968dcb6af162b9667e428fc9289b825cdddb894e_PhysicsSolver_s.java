 package game;
 
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Set;
 
 public class PhysicsSolver {
 
 	private List<DynamicObject> dynamicObjects;
 	private List<StaticObject> staticObjects;
 
 	public PhysicsSolver() {
 		dynamicObjects = new LinkedList<DynamicObject>();
 		staticObjects = new LinkedList<StaticObject>();
 	}
 
 	public void addObject(PhysicsObject object) {
 		if (object instanceof DynamicObject) {
 			dynamicObjects.add((DynamicObject) object);
 		} else if (object instanceof StaticObject) {
 			staticObjects.add((StaticObject) object);
 		}
 	}
 
 	public void removeObject(PhysicsObject object) {
 		if (object instanceof DynamicObject) {
 			dynamicObjects.remove((DynamicObject) object);
 		} else if (object instanceof StaticObject) {
 			staticObjects.remove((StaticObject) object);
 		}
 	}
 	
 	/*
 	 * This needs to be fixed, somehow run through multiple steps
 	 * and add the lines to the drawing.  Maybe add some kind of
 	 * line class?
 	 */
 	public void showMissileDirection(Lander l, float timeStep) {
 		Vector position = l.getPosition();
 		for(int i=0; i<1000; i++) {
 			Vector totalForce = new Vector();
 			for (StaticObject b : staticObjects) {
 				totalForce = totalForce.add(gravitationalForce(position, b));
 			}
 			
 			/*Vector dragForce = velocity.multiply(-DRAG_COEFFICIENT);
 			Vector acceleration = new Vector(force.x / getMass(), force.y / getMass());
 
 			velocity = velocity.add(acceleration.multiply(timeStep));
 			position = position.add(velocity.multiply(timeStep));
 			*/
 		}
 	}
 
 	public void simulateStep(float timeStep) {
 		Set<Collission> collissions = new HashSet<Collission>();
 
 		for (DynamicObject object : dynamicObjects) {
			// sum force on them
			Vector totalForce = sumVectors(object);
 
 			object.simulateStep(totalForce, timeStep);
 		}
 
 		for (DynamicObject a : dynamicObjects) {
 			checkCollisions(collissions, a, dynamicObjects);
 			checkCollisions(collissions, a, staticObjects);
 		}
 
 		for (Collission collission : collissions) {
 			collission.a.collidedWith(collission.b);
 			collission.b.collidedWith(collission.a);
 		}
 	}
 	
	private Vector sumVectors(DynamicObject object) {
 		Vector totalForce = new Vector();
 
 		for (DynamicObject b : dynamicObjects) {
 			if (object == b) {
 				continue;
 			}
 
			totalForce.add(gravitationalForce(object, b));
 		}
 
 		for (StaticObject b : staticObjects) {
 			totalForce = totalForce.add(gravitationalForce(object, b));
 		}
 		return totalForce;
 	}
 
 	private void checkCollisions(Set<Collission> collissions, DynamicObject object, List<? extends PhysicsObject> objects) {
 		for (PhysicsObject b : objects) {
 			if (object == b) {
 				continue;
 			}
 
 			if (Vector.distanceBetween(object.getPosition(), b.getPosition()) <= (object.getBoundingRadius() + b.getBoundingRadius())) {
 				collissions.add(new Collission(object, b));
 			}
 		}
 	}
 
 	private class Collission {
 		public PhysicsObject a, b;
 
 		public Collission(PhysicsObject a, PhysicsObject b) {
 			this.a = a;
 			this.b = b;
 		}
 
 		@Override
 		public boolean equals(Object other) {
 			if (this == other) {
 				return true;
 			}
 
 			if (other instanceof Collission) {
 				Collission o = (Collission) other;
 
 				if ((a == o.a && b == o.b) || (b == o.a && a == o.b)) {
 					return true;
 				}
 			}
 
 			return false;
 		}
 
 		@Override
 		public int hashCode() {
 			return a.hashCode() ^ b.hashCode();
 		}
 	}
 
 	// calculates the gravitational force of source on target
 	private Vector gravitationalForce(PhysicsObject target, PhysicsObject source) {
 		Vector direction = source.getPosition().subtract(target.getPosition());
 		float distance = direction.magnitude();
 
 		return direction.multiply(G * target.getMass() * source.getMass() / (distance * distance * distance));
 	}
 	
 	// Overload previous function for use of easy function
 	private Vector gravitationalForce(Vector place, PhysicsObject source) {
 		Vector direction = source.getPosition().subtract(place);
 		float distance = direction.magnitude();
 		
 		return direction.multiply(G * Missile.MASS * source.getMass() / (distance * distance * distance));
 	}
 
 	public static final float G = 2E7f;
 }
