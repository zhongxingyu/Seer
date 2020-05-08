 package physiks.engine;
 
 import java.util.List;
 
 import physiks.audio.AudioPlayer;
 import physiks.collision.SeparatingAxisTest;
 import physiks.engine.misc.PhysHelper;
 import physiks.engine.misc.SpatialData;
 import physiks.entities.PolyBody;
 import physiks.entities.RigidBody;
 import physiks.forces.*;
 import physiks.geometry.Vector2D;
 import physiks.main.PhysiKsSim;
 import physiks.quadtree.QuadTree;
 
 public class PhysicsEngine {
	private int frameNumber = 0;
 	private List<RigidBody> entities;
 	private QuadTree quadTree;
 	public static final float coefficientOfRestitution = 0.7f;
 	
 	public PhysicsEngine(List<RigidBody> entities) {
 		this.entities = entities;
		quadTree = new QuadTree(-PhysiKsSim.WIDTH, -PhysiKsSim.HEIGHT, PhysiKsSim.WIDTH*3, PhysiKsSim.HEIGHT*3, 1000);
 	}
 	
 	public void update(int delta) {
 		delta = Math.min(16, delta);
 		delta = 16; // This is to ensure determinism when doing testing
 		
 		float deltaInSeconds = (float)delta/1000;
 		
 		quadTree.clear();
 		for (RigidBody entity : entities) {
 			quadTree.add(entity);
 		}
 		
 		for (RigidBody entity : entities) {
 			performTimeStep(entity, deltaInSeconds);
 		}
 		
 //		String logOutput = frameNumber++ + " " + entities.get(0).getPosition() + " " + entities.get(0).getVelocity() + " " + entities.get(0).getAcceleration();
 //		System.out.println(logOutput);
 	}
 	
 	private void performTimeStep(RigidBody body, float delta) {
 		PhysHelper.zeroOutMicroVelocities(body, 0.01f);
 		SpatialData prevSpatialData = new SpatialData(body);
 		
 		advanceBody(body, delta);
 		
 		List<RigidBody> collisionCandidates = quadTree.getIntersectionCandidates(body);
 		for (RigidBody target : collisionCandidates) {
 			if (collidesWidth(body, target)) {
 				resolveCollision(body, target, prevSpatialData);
 				AudioPlayer.getInstance().playWav("bounce");
 			}
 		}
 	}
 	
 	private void advanceBody(RigidBody body, float delta) {
 		Vector2D position = body.getPosition();
 		Vector2D velocity = body.getVelocity();
 		Vector2D acceleration = body.getAcceleration();
 		
 		body.setPosition(position.add(velocity.mult(delta)));
 		body.setVelocity(velocity.add(acceleration.mult(delta)));
 		
 		body.clearForces();
 		if (body.getMass() != Float.POSITIVE_INFINITY) {
 			body.addForce(new Gravity(body));
 		}
 
 		Vector2D netForce = body.calculateNetForce();
 		Vector2D newAcceleration = netForce.div(body.getMass());
 		body.setAcceleration(newAcceleration);
 	}
 	
 	private boolean collidesWidth(RigidBody a, RigidBody b) {
 		PolyBody body = (PolyBody)a;
 		PolyBody target = (PolyBody)b;
 		
 		if (body.getId() == target.getId()) return false;
 		if (body.getMass() == Float.POSITIVE_INFINITY) return false;
 		
 		Vector2D separatingAxis = SeparatingAxisTest.getSeparatingAxis(body, target);
 
 		return (separatingAxis == null);
 	}
 	
 	private void resolveCollision(RigidBody a, RigidBody b, SpatialData prevSpatialData) {
 		PolyBody body = (PolyBody)a;
 		PolyBody target = (PolyBody)b;
 		
 //		Vector2D collisionNormal = calculateCollisionNormal(body, target, prevSpatialData);
 //		Vector2D separatingVector = calculateSeparatingVector(body, target, collisionNormal);
 		
 		// testing
 		Vector2D separatingVector = PhysHelper.calculateMinimumSeparatingVector(body, target);
 		Vector2D collisionNormal = separatingVector.normalize();
 		
 		body.setPosition(body.getPosition().add(separatingVector));
 		
 		float impulseMagnitude = PhysHelper.calculateImpulseMagnitude(body, target, collisionNormal);
 		Vector2D impulseVector = collisionNormal.mult(impulseMagnitude);
 		
 		body.setVelocity(body.getVelocity().add(impulseVector.div(body.getMass())));
 		target.setVelocity(target.getVelocity().sub(impulseVector.div(target.getMass())));
 	}
 	
 	private Vector2D calculateCollisionNormal(RigidBody a, RigidBody b, SpatialData prevSpatialData) {
 		PolyBody body1 = (PolyBody)a;
 		PolyBody body2 = (PolyBody)b;
 		
 		SpatialData currentSpatialData = new SpatialData(a);
 		
 		// Load previous position to find the separating axis
 		body1.setPosition(prevSpatialData.getPosition());
 		
 		Vector2D separatingAxis = SeparatingAxisTest.getSeparatingAxis(body1, body2);
 		if (separatingAxis == null) {
 //			Vector2D minSeparatingVector = PhysHelper.getMinimumSeparatingVector(body1, body2);
 //			Vector2D safePosition = body1.getPosition().add(minSeparatingVector);
 //			
 //			prevSpatialData.setPosition(safePosition);
 //			body1.setPosition(safePosition);
 //			separatingAxis = SeparatingAxisTest.getSeparatingAxis(body1, body2);
 			
 			System.out.println("WTF? no separating axis after rewinding it?");
 			System.exit(1);
 		}
 		
 		// TODO: Make sure the collision normal is actually the normal of the closest edge.
 		Vector2D collisionNormal = separatingAxis.perpendicular();
 		collisionNormal = collisionNormal.pointAlongWith(body1.getVelocity().mult(-1)).normalize();
 		
 		// Reload current position
 		body1.setPosition(currentSpatialData.getPosition());
 		
 		return collisionNormal;
 	}
 	
 	/*
 	 * Calculates the separating vector which resolves the overlapping of a onto; b.
 	 * The result takes into account the velocities of the two bodies
 	 * 
 	 */
 
 	// TODO: the separating vector should rewind based on the velocity, then play more
 	// physics in the axis that doesn't collide.
 	private Vector2D calculateSeparatingVector(RigidBody body1, RigidBody body2, Vector2D collisionNormal) {
 		
 		float separatingMagnitude = PhysHelper.overlapAlongAxis(body1, body2, collisionNormal);
 		Vector2D separatingVector = collisionNormal.mult(separatingMagnitude);
 
 		return separatingVector;
 	}
 }
