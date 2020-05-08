 package entity;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.HashMap;
 
 import org.lwjgl.util.vector.Vector3f;
 
 import util.math.MathHelper;
 
 public class EntityBuilder
 {
 	public static EntityBuilder defaultEntityBuilder;
 
 	public String parent;
 	public String name;
 	public String fullName;
 
 	public Integer physicsType;
 	public Integer collisionType;
 	public Boolean invisible;
 	public Boolean gravity;
 	public String model;
 	public String collisionModel;
 	public Float weight;
 	public Vector3f balancePoint = new Vector3f();
 	
 	public List<String> triggers = new ArrayList<>(), functions = new ArrayList<>();
 
 	public String classPath;
 
 	public HashMap<String, Object> customValues = new HashMap<>();
 
 	/**
 	 * @return returns an exact copy of this EntityBuilder, used for 'parents'
 	 */
 	@SuppressWarnings("unchecked")
 	public EntityBuilder clone()
 	{
 		EntityBuilder e = new EntityBuilder();
 
 		e.parent = this.parent;
 		e.name = this.name;
 		e.fullName = this.fullName;
 
 		e.physicsType = this.physicsType;
 		e.collisionType = this.collisionType;
 		e.invisible = this.invisible;
 		e.gravity = this.gravity;
 		e.model = this.model;
 		e.collisionModel = this.collisionModel;
 		e.weight = this.weight;
 		e.balancePoint = MathHelper.cloneVector(this.balancePoint);
 
 		e.classPath = this.classPath;
 
 		e.customValues = (HashMap<String, Object>) this.customValues.clone();
 
 		return e;
 	}
 
 	/**
 	 * 
 	 * @return creates and returns an instance of Entity specified in this
 	 *         EntityBuilder
 	 * @throws ClassNotFoundException
 	 * @throws InstantiationException
 	 * @throws IllegalAccessException
 	 */
 	@SuppressWarnings("unchecked")
 	public Entity createEntity() throws ClassNotFoundException, InstantiationException, IllegalAccessException
 	{
 		Entity e;
 		if (classPath != "null")
 		{
 			Class<? extends Entity> c = (Class<? extends Entity>) Class.forName(classPath);
 			e = (Entity) c.newInstance();
 		}
 		else e = new Entity();
 		e.name = this.name;
 		e.fullName = this.fullName;
 		e.parent = this.parent;
 		e.physicsType = this.physicsType;
 		e.collisionType = this.collisionType;
 		e.invisible = this.invisible;
 		e.gravity = this.gravity;
 		e.model = this.model;
 		e.collisionModel = this.collisionModel;
 		e.weight = this.weight;
 		e.balancePoint = new Vector3f(this.balancePoint);
 
 		e.customValues = (HashMap<String, Object>) this.customValues.clone();
 
 		return e;
 	}
 }
