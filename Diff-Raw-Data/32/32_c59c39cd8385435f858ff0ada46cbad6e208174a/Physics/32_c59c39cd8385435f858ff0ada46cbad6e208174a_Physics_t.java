 package engine.physics;
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.concurrent.ConcurrentLinkedQueue;
 
 import javax.vecmath.Vector3f;
 
 import com.bulletphysics.collision.broadphase.AxisSweep3;
 import com.bulletphysics.collision.broadphase.BroadphaseInterface;
 import com.bulletphysics.collision.dispatch.CollisionDispatcher;
 import com.bulletphysics.collision.dispatch.CollisionObject;
 import com.bulletphysics.collision.dispatch.CollisionWorld.ClosestRayResultCallback;
 import com.bulletphysics.collision.dispatch.DefaultCollisionConfiguration;
 import com.bulletphysics.collision.dispatch.GhostPairCallback;
 
 import com.bulletphysics.dynamics.DiscreteDynamicsWorld;
 import com.bulletphysics.dynamics.DynamicsWorld;
 import com.bulletphysics.dynamics.RigidBody;
 import com.bulletphysics.dynamics.constraintsolver.ConstraintSolver;
 import com.bulletphysics.dynamics.constraintsolver.SequentialImpulseConstraintSolver;
 import com.bulletphysics.dynamics.constraintsolver.TypedConstraint;
 import com.bulletphysics.linearmath.Transform;
 
 import com.bulletphysics.collision.dispatch.GhostObject;
 
 import engine.entity.Entity;
 import engine.entity.Actor;
 import engine.entity.EntityCallbackFunctions;
 import engine.entity.QueueItem;
 import engine.entity.Entity.ObjectType;
 import engine.entity.EntityList;
 
 public class Physics extends PhysicsInterface{
 	// World Definitions
 	private DefaultCollisionConfiguration collisionConfiguration;
 	private CollisionDispatcher dispatcher;
 	private Vector3f worldAabbMin;
 	private Vector3f worldAabbMax;
 	private BroadphaseInterface overlappingPairCache;
 	private ConstraintSolver solver;
 	private DynamicsWorld dynamicsWorld;
 	private HashMap<String,TypedConstraint> constraints;
 
 	float deltaT;
 	long frames = 0;
 
 	// For holding the previous time in microseconds to calculate deltaT
 	private long prev_time;
 	
 	//Keeping track of entities that need added/removed
 	private ConcurrentLinkedQueue<QueueItem> physicsQueue;
 
 	public Physics() {
 		physicsQueue = new ConcurrentLinkedQueue<QueueItem>();
 		
 		// Default collision constructor
 		collisionConfiguration = new DefaultCollisionConfiguration();
 
 		// Creates a dispatcher thread for sending processing physics
 		// calculations
 		dispatcher = new CollisionDispatcher(collisionConfiguration);
 
 		// Min and Max collision boundaries for world (needs changing)
 		worldAabbMin = new Vector3f(-1000, -1000, -1000);
 		worldAabbMax = new Vector3f(1000, 1000, 1000);
 
 		// algorithm for finding collision proximity (there are better ones)
 		overlappingPairCache = new AxisSweep3(worldAabbMin, worldAabbMax);
 		//overlappingPairCache = new DbvtBroadphase();
 		overlappingPairCache.getOverlappingPairCache().setInternalGhostPairCallback(new GhostPairCallback());
 
 		// Type of solver to be used for solving physics (look into threading
 		// for parallel)
 		solver = new SequentialImpulseConstraintSolver();
 		
 		// Create the dynamics world and set default options
 		dynamicsWorld = new DiscreteDynamicsWorld(dispatcher, overlappingPairCache, solver, collisionConfiguration);
 
 		dynamicsWorld.getSolverInfo().numIterations = 20;
 		dynamicsWorld.setGravity(new Vector3f(0, 0, 0));
 		dynamicsWorld.getDispatchInfo().allowedCcdPenetration = 0.1f;
 
 		// Preset the previous time so deltaT isn't enormous on first run
 		prev_time = System.nanoTime();
 		
 		constraints = new HashMap<String,TypedConstraint>();
 	}
 
 	public DynamicsWorld getDynamicsWorld() {
 		return dynamicsWorld;
 	}
 
 	public void clientUpdate() {
 		// simple dynamics world doesn't handle fixed-time-stepping
 		deltaT = (System.nanoTime() - prev_time);
 		prev_time = System.nanoTime();
 		frames++;
 		// step the simulation
 		if (dynamicsWorld != null) {
 			//TODO: This should be configurable
			dynamicsWorld.stepSimulation(deltaT / 1000000000f, 1, 1/60f); 
 		}
 	}
 
 	public long getFrames() {
 		return frames;
 	}
 
 	public void resetFrames() {
 		frames = 0;
 	}
 
 	public float getDeltaT() {
 		return deltaT;
 	}
 
 	public void setGravity(Vector3f gravity) {
 		dynamicsWorld.setGravity(gravity);
 	}
 
 	public void handleGhostCollisions(EntityList entity_list) {
 		for(Entity entity : entity_list.getEntitiesAndSubEntities()){
 			if(entity.getProperty(Entity.COLLISION_OBJECT).getClass() != RigidBody.class &&
 				(Boolean)entity.getProperty(Entity.COLLIDABLE) == false) {
 				com.bulletphysics.collision.dispatch.GhostObject ghost = (GhostObject) entity.getProperty(Entity.COLLISION_OBJECT);
 				for(int i=0;i<ghost.getNumOverlappingObjects();i++){
 					entCollidedWith(entity, entity_list.getEntityByCollisionObject(ghost.getOverlappingObject(i)));
 				}
 			}
 		}
 	}
 	private void entCollidedWith(Entity source, Entity collided_with){
 		if(collided_with == null) {	
 			ArrayList<Method> methods = source.getCollisionFunctions();
 			for(Method method : methods){
 				try {
 					method.invoke(EntityCallbackFunctions.class, source, collided_with, this);
 				} catch (IllegalArgumentException e) {
 					e.printStackTrace();
 				} catch (IllegalAccessException e) {
 					e.printStackTrace();
 				} catch (InvocationTargetException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 
 	@Override
 	public Entity pickEntityWithRay(Vector3f ray_from, Vector3f ray_to, EntityList entity_list) {
 		ClosestRayResultCallback resultCallback = new ClosestRayResultCallback(ray_from, ray_to);
 		
 		dynamicsWorld.rayTest(ray_from, ray_to, resultCallback);
 		if(resultCallback.collisionObject != null){
 			for(Entity ent : entity_list){
 				if(resultCallback.collisionObject == ent.getProperty(Entity.COLLISION_OBJECT)){
 					return ent;
 				}
 			}
 		}
 		return null;
 	}
 
 	@Override
 	public void entityAdded(Entity ent) {
 		ent.addListener(this);
 		physicsQueue.add(new QueueItem(ent, QueueItem.ADD));
 	}
 
 	@Override
 	public void entityRemoved(Entity ent) {
 		ent.removeListener(this);
 		physicsQueue.add(new QueueItem(ent, QueueItem.REMOVE));
 	}
 	
 	public void parsePhysicsQueue() {
 		Object[] itemArray = (Object[]) physicsQueue.toArray();
 		for (Object obj_item : itemArray) {
 			QueueItem item = (QueueItem)obj_item;
 			CollisionObject collision_object = (CollisionObject) item.getEnt().getProperty(Entity.COLLISION_OBJECT);
 			item.getEnt().setProperty(Entity.ADDED_TO_PHYSICS, true);
 			if (QueueItem.ADD == ((QueueItem) item).getAction()) {
 				if (collision_object != null) {
 					dynamicsWorld.addCollisionObject(collision_object);
 					if(item.getEnt().getObjectType() == ObjectType.actor) {
 						//TODO: I'm not sure why this line isn't necessary; leave it until we figure that out
 						dynamicsWorld.addAction(((Actor)item.getEnt()).getActor());
 					}
 				}
 			} else if (QueueItem.REMOVE == ((QueueItem) item).getAction()) {
 				if (item.getEnt().getObjectType() == ObjectType.rigidbody){
 					dynamicsWorld.removeRigidBody((RigidBody) collision_object);
 				} else {
 					dynamicsWorld.removeCollisionObject(collision_object);
 				}
 				if(item.getEnt().getObjectType() == ObjectType.actor) {
 					dynamicsWorld.removeAction(((Actor)item.getEnt()).getActor());
 				}
 			}
 			physicsQueue.remove(item);
 		}
 	}
 
 	@Override
 	public void entityPropertyChanged(String property, Entity entity, Object old_value) {
 		switch(property){
 			case Entity.GRAVITY:
 				entityGravityChanged(entity);
 			case Entity.POSITION:
 				entityPositionChanged(entity);
 			case Entity.COLLISION_OBJECT:
 				entityCollisionChanged(entity, old_value);
 			case Entity.CONSTRAINTS:
 				entityConstraintChanged(entity);
 		}	
 	}
 
 	private void entityConstraintChanged(Entity entity) {
 		@SuppressWarnings("unchecked")
 		HashMap<String,TypedConstraint> ent_constraints = (HashMap<String,TypedConstraint>)entity.getProperty(Entity.CONSTRAINTS);
 		for(String constraint: ent_constraints.keySet()) {
 			if(constraints.containsKey(constraint) && ent_constraints.get(constraint) != null) {
 				dynamicsWorld.removeConstraint(constraints.get(constraint));
 				dynamicsWorld.addConstraint(ent_constraints.get(constraint), true);
 				constraints.put(constraint, ent_constraints.get(constraint));
 			} else if(constraints.containsKey(constraint) && ent_constraints.get(constraint) == null) {
 				dynamicsWorld.removeConstraint(constraints.get(constraint));
 				constraints.remove(constraint);
 			} else {
 				constraints.put(constraint, ent_constraints.get(constraint));
 				dynamicsWorld.addConstraint(ent_constraints.get(constraint), true);
 			}
 		}
 	}
 
 	private void entityCollisionChanged(Entity entity, Object old_value) {
 		if(old_value != null && (old_value.getClass() == CollisionObject.class ||
 			old_value.getClass() == RigidBody.class)	) {	
 			dynamicsWorld.removeCollisionObject((CollisionObject)old_value);
 			if(entity.getObjectType() == ObjectType.actor) {
 				//TODO: I'm not sure why this line isn't necessary; leave it until we figure that out
 				dynamicsWorld.removeAction(((Actor)entity).getActor());
 			}
 			dynamicsWorld.addCollisionObject((CollisionObject)entity.getProperty(Entity.COLLISION_OBJECT));
 			if(entity.getObjectType() == ObjectType.actor) {
 				//TODO: I'm not sure why this line isn't necessary; leave it until we figure that out
 				dynamicsWorld.addAction(((Actor)entity).getActor());
 			}
 		}
 	}
 
 	private void entityPositionChanged(Entity entity) {
 		//TODO: Unchecked casts
 		Vector3f pos;
 
 		pos = ((Vector3f) entity.getProperty(Entity.POSITION, false));
 		
 		CollisionObject collision_object = (CollisionObject) entity.getProperty(Entity.COLLISION_OBJECT);
 		Transform trans = collision_object.getWorldTransform(new Transform());
 		trans.setIdentity();
 		trans.origin.set(pos);
 		collision_object.setWorldTransform(trans);
 	}
 
 	private void entityGravityChanged(Entity entity) {
 		if (entity.getObjectType() == ObjectType.rigidbody){
 			//TODO:  Cast without check Dannngerous
 			CollisionObject collision_object = (CollisionObject) entity.getProperty(Entity.COLLISION_OBJECT);
 			if(collision_object != null)
 				((RigidBody) collision_object).setGravity((Vector3f) entity.getProperty(Entity.GRAVITY));
 		}else {
 			System.out.println("Method [setGravity] not supported for ghost object");
 		}
 	}
 }
