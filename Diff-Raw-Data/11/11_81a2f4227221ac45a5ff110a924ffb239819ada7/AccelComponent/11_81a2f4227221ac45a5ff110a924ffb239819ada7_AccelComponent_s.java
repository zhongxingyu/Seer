 package atl.space.entities;
 
 import org.lwjgl.util.vector.Vector3f;
 
 public class AccelComponent extends Component { //don't use this, use restricted version
 	public Vector3f accel;
 	public AccelComponent(){
 		accel = new Vector3f(0, 0, 0);
 	}
 	public AccelComponent(Vector3f a){
 		accel = a;
 	}
 	public void update(int delta) {
 		MovementComponent mc = (MovementComponent)owner.getComponent("movement");
 		Vector3f.add(mc.speed, accel, mc.speed);
 	}
 
 	public Component getStepped(int delta) {
 		AccelComponent ac = new AccelComponent(accel);
 		ac.update(delta);
 		return ac;
 	}
 
 }
