 package nl.sest.gamejam.physics;
 
 import java.util.List;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import nl.sest.gamejam.model.Physical;
 import nl.sest.gamejam.model.event.listener.CreatePhysicalListener;
 import nl.sest.gamejam.model.event.listener.DeletePhysicalListener;
 import nl.sest.gamejam.model.impl.Bob;
 import nl.sest.gamejam.model.impl.Model;
 import nl.sest.gamejam.model.impl.PointOfInterest;
 
 import org.jbox2d.collision.shapes.CircleShape;
 import org.jbox2d.common.Vec2;
 import org.jbox2d.dynamics.Body;
 import org.jbox2d.dynamics.BodyDef;
 import org.jbox2d.dynamics.BodyType;
 import org.jbox2d.dynamics.FixtureDef;
 import org.jbox2d.dynamics.World;
 
 public class PhysicsInterface implements CreatePhysicalListener, DeletePhysicalListener {
 
 	private World world;
 	private HashMap<Physical, Body> objects = new HashMap<Physical, Body>();
 	private Model model;
 	
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
         	
         	// If the physical is a Bob, apply force using all POIs
         	if (physical instanceof Bob) {
         		applyForces(body);
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
 		float radius = physical.getRadius();
 		
 	    CircleShape circleShape = new CircleShape();
 	    circleShape.m_radius = radius;
 
 	    // Create FixtureDef (use predefined parameters for now)
 	    FixtureDef fixtureDef = new FixtureDef();
 	    fixtureDef.shape = circleShape;
 	    fixtureDef.density = 1;
 	    fixtureDef.friction = 0.01f;
 	    fixtureDef.restitution = 0.3f;
 		
 	    // Attach FixtureDef to body
 	    body.createFixture(fixtureDef);
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
     
     private void applyForces(Body body) {
     	List<PointOfInterest> pois = model.getPointsOfInterest();
     	
     	for(PointOfInterest poi : pois) {
			Vec2 bobVec = body.getPosition();
 			Vec2 poiVec = new Vec2(poi.getX(), poi.getY());
			Vec2 force = calculateAttract(poiVec, bobVec);
 			body.applyForce(force, body.getWorldCenter());
 		}
     }
     
    private Vec2 calculateAttract(Vec2 poi, Vec2 bob) {
    	Body poiBody = objects.get(poi);
    	Body bobBody = objects.get(bob);
    	
         float forceStrength = 100; 
        Vec2 poiPos = poiBody.getWorldCenter();    
        Vec2 bobPos = bobBody.getWorldCenter();
         
         Vec2 force = poiPos.sub(bobPos);
         float distance = force.length();
         force.normalize();
 
        float strength = (forceStrength * 1 * bobBody.m_mass) / (distance * distance);
         force.mulLocal(strength);
         return force;
      }
 
 	@Override
 	public void fireDeletePhysical(Physical physical) {
 		deleteObject(physical);
 	}
 
 	@Override
 	public void fireCreatePhysical(Physical physical) {
 		addObject(physical);
 	}
 }
