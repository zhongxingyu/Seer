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
 
 import engine.Engine;
 import engine.render.Model;
 import engine.render.Shader;
 
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Set;
 
 import javax.vecmath.Vector3f;
 
 import com.bulletphysics.collision.dispatch.CollisionFlags;
 import com.bulletphysics.collision.dispatch.CollisionObject;
 import com.bulletphysics.collision.dispatch.PairCachingGhostObject;
 import com.bulletphysics.collision.shapes.CollisionShape;
 import com.bulletphysics.dynamics.RigidBody;
 import com.bulletphysics.dynamics.RigidBodyConstructionInfo;
 import com.bulletphysics.linearmath.DefaultMotionState;
 import com.bulletphysics.linearmath.Transform;
 
 public class Entity{
 	// Properties
 	//protected CollisionObject collision_object;
 	private HashMap<String, Object> data;
 	private ArrayList<EntityListener> listeners;
 	private Shader shader;
 
 	private ArrayList<Method> collision_functions = new ArrayList<Method>(); 
 	
 	public static enum ObjectType {
 		ghost, rigidbody, actor
 	};
 
 	protected ObjectType object_type;
 
 	/* Properties the engine uses a lot */
 	public static String NAME = "name";
 	public static final String COLLIDABLE = "collidable";
 	public static final String TIME_TO_LIVE = "TTL";
 	public static final String SHOULD_DRAW = "should_draw";
 	public static final String COLLISION_OBJECT = "collision_object";
 	public static final String POSITION = "position";
 	
 	// Required keys
 	public static String[] reqKeys = { NAME, COLLIDABLE, TIME_TO_LIVE, SHOULD_DRAW };
 
 	// Keep track of number of entities for naming purposes
 	private static int num_entities = 0;
 
 	// For making entity groups (complex bodies)
 	private EntityList subEntities;
 
 	/* Constructors */
 	public Entity(float mass, boolean collide, Model model, Shader shader) {
 		initialSetup(mass, collide, model, shader);
 	}
 
 	public Entity(String name, float mass, boolean collide, Model model, Shader shader) {
 		initialSetup(name, mass, collide, model, shader);
 	}
 	
 	public Entity(Entity ent) {
 		String new_name = (String)ent.getProperty(Entity.NAME);
		CollisionObject collision_object = (CollisionObject) this.getProperty(Entity.COLLISION_OBJECT);
 		float mass = ((RigidBody)collision_object).getInvMass();
 		boolean collide = (boolean)ent.getProperty(Entity.COLLIDABLE);
 		Shader shader = (Shader)ent.getProperty("shader");		
 				
 		//TODO: Error checking for model cast
 		initialSetup(new_name, mass, collide, (Model)ent.getProperty("model"), shader);
 	}
 	
 	// Sets the initial name of the body in the list
 	// Also sets some default options to the ent
 	private void initialSetup(float mass, boolean c, Model model, Shader shader) {
 		initialSetup("ent" + String.valueOf(num_entities), mass, c, model, shader);
 	}
 
 	private void initialSetup(String name, float mass, boolean c, Model model, Shader shader) {
 		
 		listeners = new ArrayList<EntityListener>();
 
 		num_entities++;
 		data = new HashMap<String, Object>();
 		data.put(NAME, name);
 		data.put(COLLIDABLE, c);
 		data.put(TIME_TO_LIVE, 0);
 		data.put(SHOULD_DRAW, true);
 		//TODO: Generate this based on model instead
 		//this.model = model;
 		setProperty("model",model);
 				
 		CollisionShape shape = model.getCollisionShape();
 		if(c){
 			createRigidBody(mass, shape);
 			object_type = ObjectType.rigidbody;
 		}else{
 			createGhostBody(mass, shape);
 			object_type = ObjectType.ghost;
 		}
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
 
 		CollisionObject collision_object = new RigidBody(cInfo);
 		// This is extremely important; if you forget this
 		// then nothing will rotate
 		Transform identity = new Transform();
 		identity.setIdentity();
 		((RigidBody) collision_object).setWorldTransform(identity);
 		((RigidBody) collision_object).setMassProps(mass, localInertia);
 		((RigidBody) collision_object).updateInertiaTensor();
 		
 		this.setProperty(Entity.COLLISION_OBJECT,collision_object);
 
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
 		ghost.setCollisionFlags(CollisionFlags.NO_CONTACT_RESPONSE);
 		// This is extremely important; if you forget this
 		// then nothing will rotate
 		// ghost.setMassProps(mass, localInertia);
 		// ghost.updateInertiaTensor();
 		Transform identity = new Transform();
 		identity.setIdentity();
 		
 		CollisionObject collision_object = new CollisionObject();
 		collision_object = ghost;
 		collision_object.setWorldTransform(identity);
 		this.setProperty(Entity.COLLISION_OBJECT, collision_object);
 	}
 
 	/*
 	 * End of Constructors
 	 * 
 	 * /* MUTATORS
 	 */
 	public void setProperty(String key, Object val) {
 		data.put(key, val);
 		for(EntityListener listener : listeners){
 			listener.entityPropertyChanged(key, this);
 		}
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
 
 	/* ACCESSORS */
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
		System.out.println(key + "||" + data.containsKey(key));
 		if(key != null  && data.containsKey(key))
 			return data.get(key);
 		else
 			return null;
 	}
 
 	public Set<String> getKeySet() {
 		return data.keySet();
 	}
 
 	/* MISC */
 	public void drawFixedPipe() {
 		Boolean should_draw = (Boolean)getProperty(SHOULD_DRAW);
 		if (should_draw) {
 			//TODO: Error checking for model cast
 			Model model = (Model)getProperty("model");
 			if(model != null) {
 				model.drawFixedPipe(this);
 			}
 		}
 	}
 	
 	
 	public void drawProgrammablePipe() {
 		Boolean should_draw = (Boolean)getProperty(SHOULD_DRAW);
 		if (should_draw) {
 			//TODO: Error checking for model cast
 			Model model = (Model)getProperty("model");
 			if(model != null) {
 				if(shader == null)
 					model.drawProgrammablePipe(this);
 				else
 					model.drawProgrammablePipe(this,shader);
 			}
 		}
 	}
 
 	public void setCollisionFlags(int kinematic_object) {
 		CollisionObject collision_object = (CollisionObject) this.getProperty(Entity.COLLISION_OBJECT);
 		collision_object.setCollisionFlags(kinematic_object);
 	}
 
 	public void applyImpulse(Vector3f impulse, Vector3f position) {
 		CollisionObject collision_object = (CollisionObject) this.getProperty(Entity.COLLISION_OBJECT);
 		if (object_type == ObjectType.rigidbody){ 
 			((RigidBody) collision_object).applyImpulse(impulse, position);
 		} else {
 			System.out.println("Method [applyImpulse] not supported for ghost object");
 		}
 	}
 	
 	public void applyTorqueImpulse(Vector3f impulse) {
 		CollisionObject collision_object = (CollisionObject) this.getProperty(Entity.COLLISION_OBJECT);
 		if (object_type == ObjectType.rigidbody){ 
 			((RigidBody) collision_object).applyTorqueImpulse(impulse);
 		} else {
 			System.out.println("Method [applyTorqueImpulse] not supported for ghost object");
 		}
 	}
 	
 	public void clearForces() {
 		CollisionObject collision_object = (CollisionObject) this.getProperty(Entity.COLLISION_OBJECT);
 		if (object_type == ObjectType.rigidbody){ 
 			((RigidBody) collision_object).clearForces();
 		} else {
 			System.out.println("Method [clearForces] not supported for ghost object");
 		}
 	}
 
 	public void setMotionState(DefaultMotionState defaultMotionState) {
 		CollisionObject collision_object = (CollisionObject) this.getProperty(Entity.COLLISION_OBJECT);
 		if (object_type == ObjectType.rigidbody){ 
 			((RigidBody) collision_object).setMotionState(defaultMotionState);
 		} else { 
 			System.out.println("Method [setActivation] not supported for ghost object");
 		}
 	}
 
 	public ObjectType getObjectType() {
 		return object_type;
 	}
 
 	public void activate() {
 		CollisionObject collision_object = (CollisionObject) this.getProperty(Entity.COLLISION_OBJECT);
 		collision_object.activate();
 	}
 
 	public void setCollisionShape(CollisionShape createCollisionShape) {
 		// Sets the new collision shape
 		CollisionObject collision_object = (CollisionObject) this.getProperty(Entity.COLLISION_OBJECT);
 		Vector3f scalevec = collision_object.getCollisionShape().getLocalScaling(new Vector3f());
 		collision_object.setCollisionShape(createCollisionShape);
 		collision_object.getCollisionShape().setLocalScaling(scalevec);
 
 		// This is to correct for the fact that the center of mass
 		// is not the same as the origin of the model
 		// we have to do this here because the offset is calculated
 		// by the model which doesn't get associated until now
 		// TODO: This has not been tested at all
 		// Someone should see if this actually corrects for the
 		// offset problem
 		//Transform offset = new Transform();
 		//offset.origin.set(model.getCenter());
 		Transform position = new Transform();
 		position.origin.set((Vector3f)this.getProperty(Entity.POSITION));
 		//this.setMotionState(new DefaultMotionState(position, offset));
 	}
 
 	public void setAngularFactor(float factor, Vector3f velocity) {
 		CollisionObject collision_object = (CollisionObject) this.getProperty(Entity.COLLISION_OBJECT);
 		if (object_type == ObjectType.rigidbody) {
 			((RigidBody) collision_object).setAngularFactor(factor);
 			((RigidBody) collision_object).setAngularVelocity(velocity);
 		} else {
 			System.out.println("Method [setAngularFactor] not supported for ghost object");
 		}
 	}
 
 	public void setDamping(float linear_damping, float angular_damping) {
 		CollisionObject collision_object = (CollisionObject) this.getProperty(Entity.COLLISION_OBJECT);
 		if (object_type == ObjectType.rigidbody) {
 			// ((RigidBody)
 			// collision_object).setInterpolationLinearVelocity(velocity);
 			((RigidBody) collision_object).setDamping(linear_damping,
 				angular_damping);
 			// ((RigidBody) collision_object).applyDamping(0);
 		} else {
 			System.out.println("Method [setVelocity] not supported for ghost object");
 		}
 	}
 
 	public void setAngularIdentity() {
 		CollisionObject collision_object = (CollisionObject) this.getProperty(Entity.COLLISION_OBJECT);
 		if (object_type == ObjectType.rigidbody) {
 			// ((RigidBody)
 			// collision_object).setInterpolationLinearVelocity(velocity);
 			DefaultMotionState motionState = new DefaultMotionState();
 			Transform t = new Transform();
 			t.setIdentity();
 			motionState.setWorldTransform(t);
 			((RigidBody) collision_object).setMotionState(motionState);
 			// ((RigidBody) collision_object).applyDamping(0);
 		} else {
 			System.out.println("Method [setVelocity] not supported for ghost object");
 		}
 	}
 	
 	public void addCollisionFunctions(String... names){
 		for(String method_name : names){
 			try {
 				collision_functions.add(
 					EntityCallbackFunctions.class.getMethod(
 							method_name, 
 							Entity.class, 
 							Entity.class, 
 							Engine.class
 					)
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
 
 	
 	public ArrayList<Method> getCollisionFunctions() {
 		return collision_functions;
 	}
 
 	public void getTransformation(float[] body_matrix) {
 		CollisionObject collision_object = (CollisionObject) this.getProperty(Entity.COLLISION_OBJECT);
 		Transform transform_matrix = new Transform();
 		transform_matrix = collision_object.getWorldTransform(new Transform());
 		transform_matrix.getOpenGLMatrix(body_matrix);
 	}
 	
 	public void addListener(EntityListener listener){
 		listeners.add(listener);
 	}
 	public void removeListener(EntityListener listener){
 		listeners.remove(listener);
 	}
 }
