 /*
  * This class is what holds any object in the world.
  * 	The user can add/remove custom properties (though a few are 
  * 	unremovable for internal engine reasons)
  * 
  *	//TODO: Maybe come up with actions like rotate, etc that a user might want to access
  *			Programmatically, and use skynet code to queue them up and play them back?
  *			Would function for animations and such too?
  */
 package engine.entity;
 
 import engine.render.Model;
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Set;
 
 import javax.vecmath.Vector3f;
 
 import com.bulletphysics.collision.dispatch.CollisionObject;
 import com.bulletphysics.collision.dispatch.PairCachingGhostObject;
 import com.bulletphysics.collision.shapes.CollisionShape;
 import com.bulletphysics.dynamics.RigidBody;
 import com.bulletphysics.dynamics.RigidBodyConstructionInfo;
 import com.bulletphysics.linearmath.DefaultMotionState;
 import com.bulletphysics.linearmath.Transform;
 
 
 
 public class Entity {
 	
 	
 	// Properties
 	protected CollisionObject collision_object;
 	private HashMap<String, Object> data;
 	private Model model;
 	private boolean shouldDraw = true;
 
 	ArrayList<Method> collision_functions = new ArrayList<Method>(); 
 	
 	public static enum ObjectType {
 		ghost, rigidbody
 	};
 
 	private ObjectType object_type;
 
 	/* Properties the engine uses a lot */
 	public static final String NAME = "name";
 	public static final String COLLIDABLE = "collidable";
 	public static final String TIME_TO_LIVE = "TTL";
 
 	// Required keys
 	private String[] reqKeys = { NAME, COLLIDABLE, TIME_TO_LIVE };
 
 	// Keep track of number of entities for naming purposes
 	private static int num_entities = 0;
 
 	// For making entity groups (complex bodies)
 	private EntityList subEntities;
 
 	/* Constructors */
 	public Entity(float mass, CollisionShape shape, boolean collide) {
		if (collide) {
			createRigidBody(mass, shape);
			object_type = ObjectType.rigidbody;
		} else {
			createGhostBody(mass, shape);
			object_type = ObjectType.ghost;
		}
		initialSetup(collide);
 	}
 
 	public Entity(String name, float mass, CollisionShape shape, boolean collide) {
 		if (collide) {
 			createRigidBody(mass, shape);
 			object_type = ObjectType.rigidbody;
 		} else {
 			createGhostBody(mass, shape);
 			object_type = ObjectType.ghost;
 		}
		initialSetup(name, collide);
 	}
 
 	/* Initializing segments */
 	// Creates the initial settings for a rigidbody
 	// This function is what we use to make things rotate over multiple axes
 	private void createRigidBody(float mass, CollisionShape shape) {
 		// rigid body is dynamic if and only if mass is non zero,
 		// otherwise static
 		boolean isDynamic = (mass != 0f);
 
 		Vector3f localInertia = new Vector3f(0f, 0f, 0f);
 		if (isDynamic) {
 			shape.calculateLocalInertia(mass, localInertia);
 		}
 
 		DefaultMotionState motion_state = new DefaultMotionState(
 			new Transform());
 		RigidBodyConstructionInfo cInfo = new RigidBodyConstructionInfo(mass,
 			motion_state, shape, localInertia);
 
 		collision_object = new RigidBody(cInfo);
 
 		// This is extremely important; if you forget this
 		// then nothing will rotate
 		((RigidBody) collision_object).setMassProps(mass, localInertia);
 		((RigidBody) collision_object).updateInertiaTensor();
 	}
 
 	protected void createGhostBody(float mass, CollisionShape shape) {
 		// rigid body is dynamic if and only if mass is non zero,
 		// otherwise static
 		PairCachingGhostObject ghost = new PairCachingGhostObject();
 		boolean isDynamic = (mass != 0f);
 
 		Vector3f localInertia = new Vector3f(0f, 0f, 0f);
 		if (isDynamic) {
 			shape.calculateLocalInertia(mass, localInertia);
 		}
 
 		ghost.setCollisionShape(shape);
 		// This is extremely important; if you forget this
 		// then nothing will rotate
 		// ghost.setMassProps(mass, localInertia);
 		// ghost.updateInertiaTensor();
 
 		collision_object = ghost;
 	}
 
 	// Sets the initial name of the body in the list
 	// Also sets some default options to the ent
	private void initialSetup(boolean c) {
		initialSetup("ent" + String.valueOf(num_entities), c);
 	}
 
	private void initialSetup(String name, boolean c) {
 		num_entities++;
 		data = new HashMap<String, Object>();
 		data.put("name", name);
 		data.put("collidable", c);
 		data.put("TTL", 0);
 	}
 
 	/*
 	 * End of Constructors
 	 * 
 	 * /* MUTATORS
 	 */
 	public void setPosition(Object p) {
 		/*
 		 * There's no straight-forward way to move a RigidBody to some location
 		 * So that's what this class does. It takes an Object because of skynet
 		 * code TODO: Remove skynet code
 		 */
 		try {
 			Vector3f pos = ((Vector3f) p);
 			Transform trans = collision_object
 				.getWorldTransform(new Transform());
 			trans.origin.set(pos);
 			collision_object.setWorldTransform(trans);
 
 		} catch (Exception e) {
 			System.out
 				.print(p.toString()
 					+ "<< Possible Incorrect data type for position, must be Vector3f\n");
 			e.printStackTrace();
 		}
 	}
 
 	public void setProperty(String key, Object val) {
 		data.put(key, val);
 	}
 
 	public void removeProperty(String key) {
 		// Protect our required keys. Don't delete those, oh no!
 		boolean req = false;
 		for (int i = 0; i < reqKeys.length; i++) {
 			if (reqKeys[i].equals(key)) req = true;
 		}
 		if (!req) {
 			data.remove(key);
 		}
 	}
 
 	public void setModel(Model model) {
 		this.model = model;
 	}
 
 	public void setShouldDraw(boolean shouldDraw) {
 		this.shouldDraw = shouldDraw;
 	}
 
 	/* ACCESSORS */
 	public Vector3f getPosition() {
 		Transform out = new Transform();
 		out = collision_object.getWorldTransform(new Transform());
 		return out.origin;
 
 	}
 
 	public EntityList getSubEntities() {
 		return subEntities;
 	}
 
 	public Set<String> getKeys() {
 		return data.keySet();
 	}
 
 	public boolean keyExists(String prop_name) {
 		for (int i = 0; i < data.keySet().size(); i++) {
 			// TODO: Probably a better way to loop through the keys, but I'm
 			// lazy
 			if ((data.keySet().toArray())[i].equals(prop_name)) return true;
 		}
 		return false;
 	}
 
 	public Object getProperty(String key) {
 		return data.get(key);
 	}
 
 	public Model getModel() {
 		return model;
 	}
 
 	public Set<String> getKeySet() {
 		return data.keySet();
 	}
 
 	public boolean shouldDraw() {
 		return shouldDraw;
 	}
 
 	/* MISC */
 	public void draw() {
 		// Retrieve the current motionstate to get the transform
 		// versus the world
 		if (shouldDraw && this.getObjectType() == ObjectType.rigidbody) {
 			if(model != null)
 				model.draw(collision_object);
 		} else {
 			// System.out.println("Method [draw] not supported for ghost object");
 		}
 	}
 
 	public void setCollisionFlags(int kinematic_object) {
 		collision_object.setCollisionFlags(kinematic_object);
 	}
 
 	public void setGravity(Vector3f gravity) {
 		if (object_type == ObjectType.rigidbody) ((RigidBody) collision_object)
 			.setGravity(gravity);
 		else System.out
 			.println("Method [setGravity] not supported for ghost object");
 	}
 
 	public void applyImpulse(Vector3f impulse, Vector3f position) {
 		if (object_type == ObjectType.rigidbody) ((RigidBody) collision_object)
 			.applyImpulse(impulse, position);
 		else System.out
 			.println("Method [applyImpulse] not supported for ghost object");
 	}
 
 	public CollisionObject getCollisionObject() {
 		return collision_object;
 	}
 
 	public void setMotionState(DefaultMotionState defaultMotionState) {
 		if (object_type == ObjectType.rigidbody) ((RigidBody) collision_object)
 			.setMotionState(defaultMotionState);
 		else System.out
 			.println("Method [setActivation] not supported for ghost object");
 	}
 
 	public ObjectType getObjectType() {
 		return object_type;
 	}
 
 	public void activate() {
 		collision_object.activate();
 	}
 
 	public void setCollisionShape(CollisionShape createCollisionShape) {
 		// Sets the new collision shape
 		collision_object.setCollisionShape(createCollisionShape);
 
 		// This is to correct for the fact that the center of mass
 		// is not the same as the origin of the model
 		// we have to do this here because the offset is calculated
 		// by the model which doesn't get associated until now
 		// TODO: This has not been tested at all
 		// Someone should see if this actually corrects for the
 		// offset problem
 		Transform offset = new Transform();
 		offset.origin.set(model.getCenter());
 		Transform position = new Transform();
 		position.origin.set(this.getPosition());
 		this.setMotionState(new DefaultMotionState(position, offset));
 	}
 
 	public void setAngularFactor(float factor, Vector3f velocity) {
 		if (object_type == ObjectType.rigidbody) {
 			((RigidBody) collision_object).setAngularFactor(factor);
 			((RigidBody) collision_object).setAngularVelocity(velocity);
 		} else System.out
 			.println("Method [setAngularFactor] not supported for ghost object");
 	}
 
 	public void setDamping(float linear_damping, float angular_damping) {
 		if (object_type == ObjectType.rigidbody) {
 			// ((RigidBody)
 			// collision_object).setInterpolationLinearVelocity(velocity);
 			((RigidBody) collision_object).setDamping(linear_damping,
 				angular_damping);
 			// ((RigidBody) collision_object).applyDamping(0);
 		} else System.out
 			.println("Method [setVelocity] not supported for ghost object");
 	}
 
 	public void setAngularIdentity() {
 		if (object_type == ObjectType.rigidbody) {
 			// ((RigidBody)
 			// collision_object).setInterpolationLinearVelocity(velocity);
 			DefaultMotionState motionState = new DefaultMotionState();
 			Transform t = new Transform();
 			t.setIdentity();
 			motionState.setWorldTransform(t);
 			((RigidBody) collision_object).setMotionState(motionState);
 			// ((RigidBody) collision_object).applyDamping(0);
 		} else System.out
 			.println("Method [setVelocity] not supported for ghost object");
 	}
 	
 	public void collidedWith(Entity collided_with){
 		for(Method method : collision_functions){
 			try {
 				method.invoke(EntityCallbackFunctions.class, this, collided_with);
 			} catch (IllegalArgumentException e) {
 				e.printStackTrace();
 			} catch (IllegalAccessException e) {
 				e.printStackTrace();
 			} catch (InvocationTargetException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	public void addCollisionFunctions(String... names){
 		for(String method_name : names){
 			try {
 				collision_functions.add(
 					EntityCallbackFunctions.class.getMethod(method_name, Entity.class, Entity.class)
 				);
 			} catch (SecurityException e) {
 				e.printStackTrace();
 			} catch (NoSuchMethodException e) {
 				e.printStackTrace();
 			}
 			
 		}
 	}
 	
 	public void removeCollisionFunctions(String... names){
 		for(String method_name : names){
 			boolean found = false;
 			for(Method method : collision_functions){
 				if(method.getName().equals(method_name)){
 					collision_functions.remove(method);
 					found = true;
 				}
 			}
 			if(found){ break; }
 		}
 	}
 }
