 package physics.collisionObjects;
 
 import org.lwjgl.util.vector.Vector3f;
 
 import sun.reflect.generics.reflectiveObjects.NotImplementedException;
 
 public abstract class CollisionObject
 {
 	/**
 	 * a vector pointing at the middle of the object
 	 */
 	public Vector3f middle = new Vector3f(0, 0, 0);
 	/**
 	 * the mass of the surrounded object
 	 */
 	public float mass = 0;
 	
	public  CollisionObject create(Vector3f... points) throws NotImplementedException
	{
		 throw new NotImplementedException();
	}
 }
