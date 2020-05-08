 package physics.collisionObjects;
 
 import org.lwjgl.util.vector.Vector3f;
 
 import sun.reflect.generics.reflectiveObjects.NotImplementedException;
 import util.math.MathHelper;
 
 /**
  * TODO: rescript
  * Mel: bitte aendere die Ellipsoid Formel bitte in: alpha²/length² + beta²/width² + gamma²/height² = 1
  * 													^X = alpha*^length + beta*^width + gamma*^height + ^middle
  * ^ bedeutet Vektor
  */
 
 /**
  * an Ellipsoid shaped Collisionbox
  * 
  * @author Marcel Mundl
  */
 public class CollisionEllipsoid extends CollisionObject
 {
 	/**
 	 * the length of the ellipsoid
 	 */
 	public Vector3f length = null;
 	/**
 	 * the width of the ellipsoid
 	 */
 	public Vector3f width = null;
 
 	/**
 	 * the height of the ellipsoid
 	 */
 	public Vector3f height = null;
 
 	/**
 	 * create a new Collisionbox based on a Vector pointing at the middle,
 	 * length, width, height and a mass
 	 * 
 	 * @param middle
 	 *            a vector to the middle
 	 * @param length
 	 *            the first radius
 	 * @param width
 	 *            the second radius
 	 * @param height
 	 *            the third radius
 	 * @param mass
 	 *            the mass used for the calculation of falling
 	 */
 	public CollisionEllipsoid(Vector3f middle, Vector3f length, Vector3f width, Vector3f height, float mass)
 	{
 		this.middle = new Vector3f(middle.getX(), middle.getY(), middle.getZ());
 		this.length = MathHelper.cloneVector(length);
 		this.width = MathHelper.cloneVector(width);
 		this.height = MathHelper.cloneVector(height);
 		this.mass = mass;
 	}
 
 	/**
 	 * create a new Collisionbox based on a Vector pointing at the middle,
 	 * length, width, height and a mass of 0
 	 * 
 	 * @param middle
 	 *            a vector to the middle
 	 * @param length
 	 *            the first radius
 	 * @param width
 	 *            the second radius
 	 * @param height
 	 *            the third radius
 	 */
 	public CollisionEllipsoid(Vector3f middle, Vector3f length, Vector3f width, Vector3f height)
 	{
 		this.middle = new Vector3f(middle.getX(), middle.getY(), middle.getZ());
 		this.length = MathHelper.cloneVector(length);
 		this.width = MathHelper.cloneVector(width);
 		this.height = MathHelper.cloneVector(height);
 	}
 
 	/**
 	 * create a new Collisionbox based on the position (with 3 coordinates),
 	 * length, width and height
 	 * 
 	 * @param x
 	 *            the position in x direction
 	 * @param y
 	 *            the position in y direction
 	 * @param z
 	 *            the position in z direction
 	 * @param length
 	 *            the first radius
 	 * @param width
 	 *            the second radius
 	 * @param height
 	 *            the third radius
 	 */
 	public CollisionEllipsoid(float x, float y, float z, Vector3f length, Vector3f width, Vector3f height)
 	{
 		this.middle = new Vector3f(x, y, z);
 		this.length = MathHelper.cloneVector(length);
 		this.width = MathHelper.cloneVector(width);
 		this.height = MathHelper.cloneVector(height);
 	}
 
 	/**
 	 * create a new Collisionbox based on the position (with 3 coordinates),
 	 * length, width, height and a mass
 	 * 
 	 * @param x
 	 *            the position in x direction
 	 * @param y
 	 *            the position in y direction
 	 * @param z
 	 *            the position in z direction
 	 * @param length
 	 *            the first radius
 	 * @param width
 	 *            the second radius
 	 * @param height
 	 *            the third radius
 	 * @param mass
 	 *            the mass used for the calculation of falling
 	 */
 	public CollisionEllipsoid(float x, float y, float z, Vector3f length, Vector3f width, Vector3f height, float mass)
 	{
 		this.middle = new Vector3f(x, y, z);
 		this.length = MathHelper.cloneVector(length);
 		this.width = MathHelper.cloneVector(width);
 		this.height = MathHelper.cloneVector(height);
 		this.mass = mass;
 	}
 
 	public  CollisionObject create(Vector3f... points) throws NotImplementedException
 	{
 		CollisionBox temp = CollisionBox.create(points);
 		CollisionEllipsoid res = new CollisionEllipsoid(temp.middle, (Vector3f) MathHelper.cloneVector(temp.depth).scale(2), (Vector3f) MathHelper.cloneVector(temp.width).scale(2), (Vector3f) MathHelper.cloneVector(temp.height).scale(2));
 		Vector3f[] ordered = points.clone();
 		float[] abstaende = new float[points.length];
 		for (int i = 0; i < points.length; i++)
 		{
 			Vector3f buffer = new Vector3f();
 			Vector3f.sub(points[i], res.middle, buffer);
 			abstaende[i] = (float) Math.sqrt(buffer.getX() * buffer.getX() + buffer.getY() * buffer.getY() + buffer.getZ() * buffer.getZ());
 		}
 		// die Punkte aufsteigend nach ihrem Abstand zum Mittelpunkt ordnen
 		for (int a = 0; a < abstaende.length; a++)
 		{
 			for (int b = a; b < abstaende.length; b++)
 			{
 				if (abstaende[b] > abstaende[b + 1])
 				{
 					float fbuffer = 0;
 					fbuffer = abstaende[b];
 					abstaende[b] = abstaende[b + 1];
 					abstaende[b + 1] = fbuffer;
 					Vector3f vbuffer = new Vector3f();
 					vbuffer = MathHelper.cloneVector(ordered[b]);
 					ordered[b] = MathHelper.cloneVector(ordered[b + 1]);
 					ordered[b + 1] = MathHelper.cloneVector(vbuffer);
 				}
 			}
 		}
 
 		// rotiere den Ellipsoid in alle Richtungen
 		for (int rotx = 1; rotx < 180; rotx++)
 		{
 			// MathHelper.rotateVector(vector, degree, rotationPlane);
 			for (int roty = 1; roty < 180; roty++)
 			{
 				for (int rotz = 1; rotz < 180; rotz++)
 				{
 					for (int a = 0; a < ordered.length; a++)
 					{
 						abstaende[a] = MathHelper.calculateDistancePointToPoint(res.middle, ordered[a]);
 						float b = ((ordered[a].getX() * ordered[a].getX()) / (res.length.length() * res.length.length())) + ((ordered[a].getY() * ordered[a].getY()) / (res.width.length() * res.width.length()))
 								+ ((ordered[a].getZ() * ordered[a].getZ()) / (res.height.length() * res.height.length()));
 						if (b == 0)
 						{
 							// punkt liegt genau auf der Oberflaeche und es muss
 							// nichts getan werden
 						}
 						else if (b < 0)
 						{
 							// punkt liegt im Ellipsoid und somit muss der
 							// Ellipsoid verkleinert werden
 						}
 						else if (b > 0)
 						{
 							// punkt liegt ausserhalb des Ellipsoiden und somit
 							// muss der Ellipsoid vergroessert werden
 						}
 					}
 				}
 			}
 		}
 		return res;
 	}
 }
