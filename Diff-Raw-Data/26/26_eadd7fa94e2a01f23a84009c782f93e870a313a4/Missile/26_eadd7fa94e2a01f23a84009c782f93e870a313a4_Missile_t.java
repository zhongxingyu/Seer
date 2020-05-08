 package common.world;
 
 import com.jme.math.Vector3f;
import com.jme.scene.shape.Sphere;
 
 public class Missile extends MobileObject {
 
	private static final long serialVersionUID = 1L;

 	public Missile(String name, Vector3f pos, Vector3f dir) {
 		super(name);
		final Sphere shape = new Sphere("Missile", 10, 10, 0.1f );		
 		
 		attachChild(shape);
		//orientation.lookAt(dir, Vector3f.ZERO);
 		movement.set(dir);
 		position.set(pos);
 		resetGeometrics();
 		setLastUpdate(System.currentTimeMillis()); //TODO: this should be missile firing time
 	}
 	
 	public Missile(String name, Ship s) {
		this(name, s.position, s.orientation.getRotationColumn(2).multLocal(-5));
 	}
 
 }
