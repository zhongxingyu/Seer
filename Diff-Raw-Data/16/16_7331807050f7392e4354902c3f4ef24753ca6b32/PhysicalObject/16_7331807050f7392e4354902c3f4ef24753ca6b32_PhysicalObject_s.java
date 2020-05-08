 package tesseract.objects;
 
 import java.util.List;
 
 import javax.media.j3d.GeometryArray;
 import javax.media.j3d.Node;
 import javax.media.j3d.Shape3D;
 import javax.media.j3d.TransformGroup;
 import javax.vecmath.Quat4f;
 import javax.vecmath.Vector3f;
 
 import alden.CollidableObject;
 
 import com.sun.j3d.utils.geometry.Primitive;
 
 /**
  * This class is the parent of all objects in the world.
  * 
  * Note: The constructor of a child class must add its shape
  *  to the transform group for it to be visible.
  * 
  * @author Jesse Morgan
  */
 public class PhysicalObject extends CollidableObject {
 	protected boolean collidable;
 	
 	protected boolean mySelected;
 	
 	public PhysicalObject(final Vector3f thePosition, final float mass) {
 		super(mass);
 		this.position.set(thePosition);
 		collidable = true;
 		mySelected = false;
 	}
 
 	public List<PhysicalObject> spawnChildren(final float duration) {
 		return null;
 	}
 	
 	public void addForce(final Vector3f force) {
 		this.forceAccumulator.add(force);
 	}
 	
 	public void updateState(final float duration) {
 		if (!mySelected) { 
 			super.updateState(duration);
 			this.updateTransformGroup();
 		}
 	}
 	
 	public void setShape(final Node node) {
 		TG.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
 		TG.setCapability(TransformGroup.ENABLE_PICK_REPORTING);
 		
 		node.setUserData(this);
 		
 		node.setCapability(javax.media.j3d.Node.ALLOW_BOUNDS_READ);
 		node.setCapability(javax.media.j3d.Node.ALLOW_LOCAL_TO_VWORLD_READ);
 
 		if (node instanceof Primitive) {
 			Primitive prim = (Primitive) node;
 			int index = 0;
 			Shape3D shape;
 			while ((shape = prim.getShape(index++)) != null) {
 				shape.setCapability(Shape3D.ALLOW_GEOMETRY_READ);
 				shape.setCapability(Node.ALLOW_LOCAL_TO_VWORLD_READ);
 				shape.setCapability(Node.ALLOW_LOCAL_TO_VWORLD_READ);
 
 				/*for (int i = 0; i < shape.numGeometries(); i++) {
 					shape.getGeometry(i).setCapability(
 							GeometryArray.ALLOW_COUNT_READ);
 					shape.getGeometry(i).setCapability(
 							GeometryArray.ALLOW_COORDINATE_READ);
 				}*/
 
 			}
 		}
 
 		super.setShape(node);
 	}
 
 	public void resolveCollisions(final PhysicalObject other) {
 		if (collidable && other.collidable) {
 			super.resolveCollisions(other);
 		}
 	}
 	
 	/**
 	 * TODO: Test code to mess with the orientation of an object.
 	 */
 	public void setRotation() {
 		this.orientation.x = (float) (Math.PI / 4);
 		this.orientation.w = 1;
 		//this.angularVelocity.x = 0.5f;
 	}
 	
 	/**
 	 * @return The position of the object.
 	 */
 	public Vector3f getPosition() {
 		return position;
 	}
 	
 	/**
 	 * @return The orientation of the object.
 	 */
 	public Quat4f getOrientation() {
 		return this.orientation;
 	}
 
 	/**
 	 * When set to true, the object will ignore all updateState calls.
 	 * 
 	 * @param b true to ignore updateState. False to heed.
 	 */
 	public void selected(final boolean b) {
 		mySelected = b;
 	}
 	
 	/**
 	 * Update the transform group after changing the position.
 	 */
 	public void updateTranformGroup() {
 		super.updateTransformGroup();
 	}
 
 	public Vector3f getVelocity() {
 		return this.velocity;
 	}
 	
 	public Vector3f getCenterOfMass() {
 		return this.centerOfMass;
 	}
 
 	public boolean isCollidable() {
 		return collidable;
 	}
 	
 	public boolean isNodeNull() {
 		return this.node == null;
 	}
 }
