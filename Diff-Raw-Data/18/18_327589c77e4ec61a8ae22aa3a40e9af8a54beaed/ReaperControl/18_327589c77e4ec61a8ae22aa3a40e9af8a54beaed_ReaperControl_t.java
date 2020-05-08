 package main.game.entities.controls;
 
 import main.game.physics.HitManager;
 
 import com.jme3.bullet.collision.shapes.CollisionShape;
 import com.jme3.collision.Collidable;
 import com.jme3.collision.CollisionResults;
 import com.jme3.collision.UnsupportedCollisionException;
 import com.jme3.math.FastMath;
 import com.jme3.math.Quaternion;
 import com.jme3.scene.Spatial;
 
 public class ReaperControl extends SpacecraftControl{
 
 	public ReaperControl(Spatial spatial, CollisionShape shape, float mass) {
 		super(spatial, shape, mass);
 		// TODO Auto-generated constructor stub
 	}
 	public ReaperControl(Spatial spatial, float mass) {
 		super(spatial, mass);
 		// TODO Auto-generated constructor stub
 	}
 
 	// see http://www.esparacing.com/sport_pilot/how%20to%20control%20an%20aircraft.htm
 	private static final float SENSITIVITY_X = 500f; // aka "pitch"
	private static final float SENSITIVITY_Z = 500f; // aka "roll"
	private static final float SENSITIVITY_y = 10000f; // aka "yaw"; very low, we want to increase difficulty ;-)
 	private float acceleration = 5;
 	private float currentspeed = 0;
 	private boolean accelerating = false;
 	private boolean decelerating = false;
 	private boolean yawRight = false;
 	private boolean yawLeft = false;
 	private boolean isExploding = false;
 	private float explosionRadius = 10;
 	private float explosionStrength = 5;
 	private HitManager hitManager;
 	
 
 	@Override
 	public void leftRotation(float value, float tpf) {
		System.out.println(value);
		doRotation(new float[]{0f,0f,-value*tpf*SENSITIVITY_Z});
 	}
 
 	@Override
 	public void rightRotation(float value, float tpf) {
		doRotation(new float[]{0f,0f,value*tpf*SENSITIVITY_Z});
 	}
 
 	@Override
 	public void upRotation(float value, float tpf) {
		doRotation(new float[]{-value*SENSITIVITY_X*tpf,0f,0f});
 	}
 
 	@Override
 	public void downRotation(float value, float tpf) {
		doRotation(new float[]{value*SENSITIVITY_X*tpf,0f,0f});
 	}
 
 	@Override
 	public void yawRight() {
 		yawRight = !yawRight;
 	}
 
 	@Override
 	public void yawLeft() {
 		yawLeft = !yawLeft;		
 	}
 
 	@Override
 	public void accelerate() {
 		accelerating=!accelerating;
 	}
 
 	@Override
 	public void decelerate() {
 		decelerating=!decelerating;
 	}
 
 	@Override
 	public void land() {
 
 	}
 
 	@Override
 	public void lift() {
 
 	}
 
 	@Override
 	public void primaryShoot() {
 
 	}
 
 	@Override
 	public void secondShoot() {
 
 	}
 	
 	@Override
 	public Spatial getSpatial() {
 		return spatial;
 	}
 
 	@Override
 	public void update(float tpf){  // tpf = 1/fps  in seconds
 
 		if (accelerating && (currentspeed <= 30)) {
 			currentspeed += acceleration * tpf; // 5 m/s�
 			System.out.println("accelerating " + currentspeed);
 		} else {
 			if (decelerating && (currentspeed >= -30)) {
 				currentspeed -= acceleration * tpf; // 5 m/s�
 				System.out.println("decelerating " + currentspeed);
 			} else {
 				if (currentspeed > 0)
 					currentspeed -= 1.5 * tpf;
 				else if (currentspeed < 0)
 					currentspeed += 1.5 * tpf;
 
 			}
 		}
 
 		if(yawRight){
 			doRotation(new float[]{0f,-1f*tpf,0f});
 		}
 
 		if(yawLeft){
 			doRotation(new float[]{0f,1f*tpf,0f});
 		}
 
 		setLinearVelocity(spatial.getLocalRotation().getRotationColumn(2).mult(+currentspeed));
 		super.update(tpf);
 	}
 
 	private void doRotation(float[] angles){
 		Quaternion oldOne = getPhysicsRotation();
 		Quaternion toRotate=new Quaternion(angles);
 		setPhysicsRotation(oldOne.mult(toRotate));
 	}
 	private void doRotation(float x, float y, float z, float w){
 		Quaternion oldOne = getPhysicsRotation();
 		Quaternion toRotate=new Quaternion(x, y, z, w);
 		setPhysicsRotation(oldOne.mult(toRotate));
 	}
 	
 	
 	@Override
 	public float getExplosionStrength() {
 		return explosionStrength;
 	}
 	
 	@Override
 	public float getExplosionRadius() {
 		return explosionRadius;
 	}
 	
 	@Override
 	public boolean isTriggered() {
 		return isExploding;
 	}
 	
 	@Override
 	public void setTriggered(boolean bvalue) {
 		isExploding = bvalue;		
 	}
 	
 
 
 }
