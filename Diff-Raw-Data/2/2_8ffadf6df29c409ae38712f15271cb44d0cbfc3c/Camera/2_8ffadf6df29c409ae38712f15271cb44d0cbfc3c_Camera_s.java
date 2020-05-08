 package entity;
 
 import javax.vecmath.Vector3f;
 
 import com.bulletphysics.collision.shapes.CollisionShape;
 import com.bulletphysics.dynamics.RigidBodyConstructionInfo;
 import com.bulletphysics.linearmath.MotionState;
 
 public class Camera extends Entity{
 	public static final String CAMERA_NAME = "camera";
 	private static final String FOCUS_JOINT = "focus"; 
 	
 	private Entity focus;
 	
 	private EntityList parentList;
 	
 	/* Constructors */
 	public Camera(RigidBodyConstructionInfo r, boolean collide, EntityList pList) {
 		super(r, collide);
 		cameraInit(pList);
 	}
	public Camera(float f, MotionState m, CollisionShape c, boolean collide, EntityList objectList, EntityList pList ) {
 		super(f,m,c, collide);
 		cameraInit(pList);
 	}
 	public Camera(float f, MotionState m, CollisionShape c, Vector3f v, boolean collide, EntityList pList ) {
 		super(f,m,c,v, collide);
 		cameraInit(pList);
 	}
 	public Camera(String _name, RigidBodyConstructionInfo r, boolean collide, EntityList pList) {
 		super(r, collide);
 		cameraInit(pList);
 	}
 	public Camera(String _name,float f, MotionState m, CollisionShape c, boolean collide, EntityList pList ) {
 		super(f,m,c, collide);
 		cameraInit(pList);
 	}
 	public Camera(String _name,float f, MotionState m, CollisionShape c, Vector3f v, boolean collide, EntityList pList ) {
 		super(f,m,c,v, collide);
 		cameraInit(pList);
 	}
 	private void cameraInit(EntityList pList){
 		setProperty(Entity.NAME, "camera");
 		parentList = pList;
 	}
 	
 	public void focusOn(Entity newFocus){
 		focus = newFocus;
 		parentList.removeJoint(FOCUS_JOINT);
 		parentList.addBallJoint(FOCUS_JOINT, 
 				newFocus, new Vector3f(0.0f,0.0f,0.0f),
 				this, new Vector3f(0.0f, 0.0f, -15.0f)
 		);
 	}
 	
 	public Vector3f getFocusPosition(){
 		Vector3f temp = new Vector3f();
 		if(focus != null){
 			focus.getCenterOfMassPosition(temp);
 		}else{
 			temp.set(0, 0, 0);
 		}
 		return temp;
 	}
 	
 	
 }
