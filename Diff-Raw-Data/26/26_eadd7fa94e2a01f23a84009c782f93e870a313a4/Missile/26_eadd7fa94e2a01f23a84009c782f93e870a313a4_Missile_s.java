 package common.world;
 
import com.jme.math.FastMath;
 import com.jme.math.Vector3f;
import com.jme.scene.shape.Cylinder;
 
 public class Missile extends MobileObject {
 
 	public Missile(String name, Vector3f pos, Vector3f dir) {
 		super(name);
		final Cylinder shape = new Cylinder("MissileCylinder", 15, 10, 0.1f, 0.5f );
		
		shape.rotateUpTo(dir);
		
		//TODO: Det här är konstigt. (som om man inte ser det)
 		
 		attachChild(shape);
 		movement.set(dir);
 		position.set(pos);
 		resetGeometrics();
 		setLastUpdate(System.currentTimeMillis()); //TODO: this should be missile firing time
 	}
 	
 	public Missile(String name, Ship s) {
		this(name, s.position, s.orientation.getRotationColumn(1).multLocal(-5));
 	}
 
 }
