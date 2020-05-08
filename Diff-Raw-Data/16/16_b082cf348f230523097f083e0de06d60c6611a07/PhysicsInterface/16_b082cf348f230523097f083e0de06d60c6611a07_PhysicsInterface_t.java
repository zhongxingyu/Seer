 package nl.sest.gamejam.physics;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 import java.util.HashMap;
 
 import nl.sest.gamejam.controller.GameInputController;
 import nl.sest.gamejam.model.Physical;
 import nl.sest.gamejam.model.event.listener.CreatePhysicalListener;
 import nl.sest.gamejam.model.event.listener.DeletePhysicalListener;
 import nl.sest.gamejam.model.impl.Blockade;
 import nl.sest.gamejam.model.impl.Bob;
 import nl.sest.gamejam.model.impl.Edge;
 import nl.sest.gamejam.model.impl.Model;
 import nl.sest.gamejam.model.impl.Obstacle;
 import nl.sest.gamejam.model.impl.PointOfInterest;
 import nl.sest.gamejam.model.impl.Valuable;
 import nl.sest.gamejam.model.player.PlayerAttractor;
 import nl.sest.gamejam.model.player.PlayerRepulsor;
 
 import org.jbox2d.callbacks.ContactImpulse;
 import org.jbox2d.callbacks.ContactListener;
 import org.jbox2d.collision.Manifold;
 import org.jbox2d.collision.shapes.CircleShape;
 import org.jbox2d.collision.shapes.PolygonShape;
 import org.jbox2d.collision.shapes.Shape;
 import org.jbox2d.common.Vec2;
 import org.jbox2d.dynamics.Body;
 import org.jbox2d.dynamics.BodyDef;
 import org.jbox2d.dynamics.BodyType;
 import org.jbox2d.dynamics.Fixture;
 import org.jbox2d.dynamics.FixtureDef;
 import org.jbox2d.dynamics.World;
 import org.jbox2d.dynamics.contacts.Contact;
 import org.jbox2d.dynamics.joints.DistanceJoint;
 import org.jbox2d.dynamics.joints.DistanceJointDef;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class PhysicsInterface implements CreatePhysicalListener, DeletePhysicalListener, ContactListener {
 
 	private static final Logger LOGGER = LoggerFactory.getLogger(GameInputController.class);
 	
 	private World world;
 	private HashMap<Physical, Body> objects = new HashMap<Physical, Body>();
 	private Model model;
 	
 	private ArrayList<PhysicsCollisionListener> physicsCollisionListeners = new ArrayList<PhysicsCollisionListener>();
 	
     public int targetFPS = 120;  
     public int timeStep = (1000 / targetFPS);  
     
 	public PhysicsInterface(Model model) {
 		// Set model
 		this.model = model;
 		
 		// Add listeners
 		model.registerCreatePhysicalEventListener(this);
 		model.registerDeletePhysicalEventListener(this);
 		
 	    // Init world
 		Vec2 gravity = new Vec2(0.0f, 0.0f);
 	    boolean doSleep = true;
 	    
 	    world = new World(gravity, doSleep);
 	    world.setContactListener(this);
 	}
 	
 	public World getWorld() {
 		return world;
 	}
 	
 	public void update() {
 		// Update Box2D world
         world.step(timeStep, 8, 3);
         
         // Sync Box2D objects with the Physical objects
         for (Physical physical : objects.keySet()) {
         	Body body = objects.get(physical);
         	
         	body.setLinearDamping(0.05f);
         	
         	// If the physical is a Bob, apply force using all POIs
         	if (physical instanceof Bob) {
         		applyPOIForces(body);
         		applyPlayerAttractorForces(body);
         		applyPlayerRepulsorForces(body);
         	}
         	
         	// If the Physical is dynamic, update the Physical properties using the Body properties
         	if (physical.isDynamic()) {
         		updatePhysical(physical, body);
         	}
         	// Else if the Physical is static, update the Body properties using the Physical properties
         	else {
         		updateBody(body, physical);
         	}
         }
 	}
 	
 	public void addObject(Physical physical) {
 		// Create Bodydef using Physical properties
 		float x = physical.getX();
 		float y = physical.getY();
 		float angle = physical.getAngle();
 
 		BodyDef bodyDef = new BodyDef();
 	    bodyDef.angle = angle;
 		bodyDef.position.set(x,y);
 
 		if (physical.isDynamic()) {
 			bodyDef.type = BodyType.DYNAMIC;
 		}
 		else {
 			bodyDef.type = BodyType.STATIC;
 		}
 
 		// Attach Bodydef to world and save body to hashmap
 		Body body = world.createBody(bodyDef);
 		body.setUserData(physical);
 		objects.put(physical, body);
 
 		// Create shape using Physical properties
 		Shape shape;
 		if (physical instanceof Edge) {
 			Edge edge = (Edge) physical;
 			
 		    PolygonShape pShape = new PolygonShape();
 		    pShape.setAsBox(edge.getWidth(), edge.getWidth());
 		    shape = pShape;
 		}
 		else {
 			float radius = physical.getRadius();
 			
 			CircleShape cShape = new CircleShape();
 			cShape.m_radius = radius;
 			shape = cShape;
 		}
 
 	    // Create FixtureDef (use predefined parameters for now)
 	    FixtureDef fixtureDef = new FixtureDef();
 	    fixtureDef.shape = shape;
 	    fixtureDef.density = 1;
 	    fixtureDef.friction = 100f;
 	    fixtureDef.restitution = 0.3f;
 		
 	    // Attach FixtureDef to body
 	    body.createFixture(fixtureDef);
 	    
 	    // Join it with another body (for Blockade)
 	    if (physical instanceof Blockade) {
 	    	Blockade blockade = (Blockade) physical;
 	    	Blockade previous = blockade.getPrevious();
 	    	Body previousBody = objects.get(previous);
 			
 	    	// Middle or last object in list
	    	if (previous != null) {
 	    		DistanceJointDef djd = new DistanceJointDef();
 	    		djd.bodyA = previousBody;
 				djd.bodyB = body;
				djd.length = 1f;
 				djd.frequencyHz = 0;
 				djd.dampingRatio = 0;
 				DistanceJoint dj = (DistanceJoint) world.createJoint(djd);
 	    	}
 	    }
 	}
 	
 	public void deleteObject(Physical physical) {
 		// Get Body attached to Physical object
 		Body body = objects.get(physical);
 		
 		// Remove Body from world and Physical from hashmap
 		world.destroyBody(body);
 		objects.remove(physical);
 	}
 	
     private void updatePhysical(Physical physical, Body body) {
     	physical.setX(body.getPosition().x);
     	physical.setY(body.getPosition().y);
     	physical.setAngle(body.getAngle());
     }
     
     private void updateBody(Body body, Physical physical) {
 		float x = physical.getX();
 		float y = physical.getY();
 		float angle = physical.getAngle();
 		body.setTransform(new Vec2(x, y), angle);
     }
     
     private void applyPOIForces(Body body) {
     	List<PointOfInterest> pois = model.getPointsOfInterest();
     	
     	// Get number of active POIs (interest > 0)
     	int numActivePOIs = 0;
     	for(PointOfInterest poi : pois) {
     		if(poi.getInterest() > 0)
     			numActivePOIs++;
     	}
     	
     	for(PointOfInterest poi : pois) {
     		if(poi.getInterest() > 0) {
 				Vec2 poiVec = new Vec2(poi.getX(), poi.getY());
 	    		Vec2 bobVec = body.getWorldCenter();
 	    		float interest = poi.getInterest()/10f;
 	    		float dist = (float)Math.max(0.0001, poiVec.sub(bobVec).length());
 				Vec2 force = computeForceVector(bobVec, poiVec, (interest)/dist);
 				force.mulLocal(1f/numActivePOIs);
 				body.applyForce(force, body.getWorldCenter());
     		}
 		}
     }
     
     private void applyPlayerAttractorForces(Body body) {
     	Collection<PlayerAttractor> pas = model.getPlayerAttractors();
     	
     	for(PlayerAttractor pa : pas) {
     		Vec2 paVec = new Vec2(pa.getX(), pa.getY());
     		Vec2 bobVec = body.getWorldCenter();
 			Vec2 force = computeForceVector(bobVec, paVec, 0.01f);
 			body.applyForce(force, body.getWorldCenter());
     	}
     }
     
     private void applyPlayerRepulsorForces(Body body) {
     	Collection<PlayerRepulsor> prs = model.getPlayerRepulsors();
     	
     	for(PlayerRepulsor pr : prs) {
     		Vec2 paVec = new Vec2(pr.getX(), pr.getY());
     		Vec2 bobVec = body.getWorldCenter();
 			Vec2 force = computeForceVector(paVec, bobVec, 0.01f);
 			body.applyForce(force, body.getWorldCenter());
     	} 
     }
     
     private Vec2 computeForceVector(Vec2 point1, Vec2 point2, float force) {
     	float distX = point2.x - point1.x;
     	float distY = point2.y - point1.y;
     	float totalDist = (float) Math.sqrt(Math.pow(distX, 2.0) + Math.pow(distY, 2.0));
     	
     	// Compute ratio to multiply vector by
     	float factor = force / totalDist;
     	return new Vec2(distX*factor, distY*factor);
     }
     
     @SuppressWarnings("unused")
 	private Vec2 calculateAttract(Vec2 poiPos, Vec2 bobPos, float bobMass) {
         float forceStrength = 100; 
         
         Vec2 force = poiPos.sub(bobPos);
         float distance = force.length();
         force.normalize();
 
         float strength = (forceStrength * 1 * bobMass) / (distance * distance);
         force.mulLocal(strength);
         return force;
      }
     
     public void fireObstacleCollisionEvent(Bob bob, Obstacle obstacle) {
     	for (PhysicsCollisionListener physicsCollisionListener : physicsCollisionListeners) {
     		physicsCollisionListener.obstacleCollisionEvent(bob, obstacle);
     	}
     }
     
     public void fireValuableCollisionEvent(Bob bob, Valuable valuable) {
     	for (PhysicsCollisionListener physicsCollisionListener : physicsCollisionListeners) {
     		physicsCollisionListener.valuableCollisionEvent(bob, valuable);
     	}
     }
     
     public void registerPhysicsCollisionListener(PhysicsCollisionListener physicsCollisionListener) {
     	physicsCollisionListeners.add(physicsCollisionListener);
     }
     
     public void unregisterPhysicsCollisionListener(PhysicsCollisionListener physicsCollisionListener) {
     	physicsCollisionListeners.remove(physicsCollisionListener);
     }
 
 	@Override
 	public void fireDeletePhysical(Physical physical) {
 		deleteObject(physical);
 	}
 
 	@Override
 	public void fireCreatePhysical(Physical physical) {
 		addObject(physical);
 	}
 
 	@Override
 	public void beginContact(Contact contact) {		
 		Fixture fixtureA = contact.getFixtureA();
 		Fixture fixtureB = contact.getFixtureB();
 		
 		Body bodyA = fixtureA.getBody();
 		Body bodyB = fixtureB.getBody();
 		
 		Physical physicalA = (Physical) bodyA.getUserData();
 		Physical physicalB = (Physical) bodyB.getUserData();
 		
 		// See if a Bob collided with an Obstacle and which is which
 		if (physicalA instanceof Bob && physicalB instanceof Obstacle) {
 			Bob bob = (Bob) physicalA;
 			Obstacle obstacle = (Obstacle) physicalB;
 			fireObstacleCollisionEvent(bob, obstacle);
 		} 
 		else if (physicalB instanceof Bob && physicalA instanceof Obstacle) {
 			Bob bob = (Bob) physicalB;
 			Obstacle obstacle = (Obstacle) physicalA;
 			fireObstacleCollisionEvent(bob, obstacle);
 		} else if (physicalA instanceof Bob && physicalB instanceof Valuable) {
 			Bob bob = (Bob) physicalA;
 			Valuable valuable = (Valuable) physicalB;
 			fireValuableCollisionEvent(bob, valuable);
 		} else if (physicalB instanceof Bob && physicalA instanceof Valuable) {
 			Bob bob = (Bob) physicalB;
 			Valuable valuable = (Valuable) physicalA;
 			fireValuableCollisionEvent(bob, valuable);
 		}
 	}
 
 	@Override
 	public void endContact(Contact contact) {
 	}
 
 	@Override
 	public void preSolve(Contact contact, Manifold oldManifold) {
 	}
 
 	@Override
 	public void postSolve(Contact contact, ContactImpulse impulse) {
 	}
 }
