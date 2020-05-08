 package atl.space.components;
 
 import java.util.List;
 
 import org.lwjgl.util.vector.Vector3f;
 
 import atl.space.entities.Entity;
 
 public class MovementComponent extends Component {
 	public Vector3f speed;
 	
 	public MovementComponent(){
 		this(new Vector3f(0, 0, 0));
 	}
 	public MovementComponent(Vector3f s){
 		super("movement");
 		speed = s;
 	}
 	public MovementComponent(MovementComponent mc){
 		this(new Vector3f(mc.speed));
 	}
 	
 	public Component clone(){
 		return new MovementComponent(this);
 	}
 	
 	public void update(int delta, List<Entity> entities) {
 		Vector3f.add(owner.position, speed, owner.position);
 	}
 
 	public Component getStepped(int delta, List<Entity> entities) {
 		MovementComponent cs = new MovementComponent(speed);
 		cs.update(delta, entities);
 		return cs;
 	}
 	
 	public Vector3f getPrevPosition(){
 		Vector3f temp = new Vector3f(owner.position);
		Vector3f.add(temp, (Vector3f)speed.negate(), temp);
 		return temp;
 	}
 	public Vector3f getNextPosition(){
 		Vector3f temp = new Vector3f(owner.position);
 		Vector3f.add(temp, speed, temp);
 		return temp;
 	}
 
 }
