 package main.game.entities.controls;
 
 import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.math.Matrix3f;
 import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
 import com.jme3.scene.Spatial;
 
 public class ReaperControl extends SpacecraftControl {
 
 	public ReaperControl(Spatial spatial, CollisionShape shape, float mass) {
 		super(spatial, shape, mass);
 		// TODO Auto-generated constructor stub
 	}
 	public ReaperControl(Spatial spatial, float mass) {
 		super(spatial, mass);
 		// TODO Auto-generated constructor stub
 	}
 
 	private static final float SENSITIVITY_X = 3;
 	private static final float SENSITIVITY_Y = 3;
 	private float velocity = 2;
 	private float currentspeed = 0;
 
 	@Override
 	public void leftRotation() {
 		System.out.println("haha");
 		Quaternion oldOne=new Quaternion();
 		getPhysicsRotation(oldOne);
 		Quaternion toRotate=new Quaternion(0, 0, 1, SENSITIVITY_X);
		setPhysicsRotation(oldOne.mult(toRotate););
 	}
 
 	@Override
 	public void rightRotation() {
 		Quaternion oldOne=new Quaternion();
 		getPhysicsRotation(oldOne);
 		Quaternion toRotate=new Quaternion(0, 0, 1, -SENSITIVITY_X);
		setPhysicsRotation(oldOne.mult(toRotate););
 	}
 
 	@Override
 	public void upRotation() {
 		Quaternion oldOne=new Quaternion();
 		getPhysicsRotation(oldOne);
 		Quaternion toRotate=new Quaternion(0, 1, 0, SENSITIVITY_Y);
		setPhysicsRotation(oldOne.mult(toRotate););
 	}
 
 	@Override
 	public void downRotation() {
 		System.out.println("haha");
 		Quaternion oldOne=new Quaternion();
 		getPhysicsRotation(oldOne);
 		Quaternion toRotate=new Quaternion(0, 1, 0, -SENSITIVITY_Y);
		setPhysicsRotation(oldOne.mult(toRotate););
 	}
 
 	@Override
 	public void accelerate() {
 		System.out.println("accelerate");
 		currentspeed+=velocity;
 		System.out.println(currentspeed);
 		setLinearVelocity(spatial.getLocalRotation().getRotationColumn(0).mult(+currentspeed));
 
 	}
 
 	@Override
 	public void decelerate() {
 		System.out.println("decelerate");
 		currentspeed-=velocity;
 		setLinearVelocity(spatial.getLocalRotation().getRotationColumn(0).mult(-currentspeed));
 
 	}
 
 	@Override
 	public void land() {
 
 	}
 
 	@Override
 	public void lift() {
 
 	}
 
 	@Override
 	public void primaryShoot() {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void secondShoot() {
 		// TODO Auto-generated method stub
 
 	}
 	
 	@Override
 	public void update(float tpf){
 		super.update(tpf);		
 	}
 
 }
