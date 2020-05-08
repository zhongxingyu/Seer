 package cge.zeppelin;
 
 import de.bht.jvr.core.GroupNode;
 import de.bht.jvr.core.Transform;
 import de.bht.jvr.math.Vector3;
 
 /**
  * An entity that responds to keyboard input and implements a very simple
  * interactive model. The flyer can shoot bullets into the viewing direction.
  * The total number of bullets is fixed. Bullet entities are reused.
  */
 public class Flyer extends Entity {
 
     private static final float STARTGAS = 25;
 	private static final float STARTLOAD = 15;
 	
 	float acceleration 		= 0.5f; 	// m/s
     float rotAcceleration	= 0.05f; 	// rad/s
     
     float yRotVelocity 		= 0;
     float xRotVelocity 		= 0;
     float velocity 			= 0; 		// m/s
     
     float gravity 			= -10;
     float gas				=  STARTGAS;
     float load				=  STARTLOAD;
     
 	private float friction = 0.01f;
 	private float roll = 0;
 	private Zeppelin zeppelin;
 	private Terrain terrain;
     
     /**
      * Create a new flyer and attach it to an existing scene node. Needs a
      * reference to the world for access to input and scene state.
      */
     Flyer(GroupNode n, Vector3 start, Terrain terrain) {
         node = n;
         this.terrain = terrain;
 		
         translation = Transform.translate(start);
 		rotation 	= Transform.rotate(new Vector3(0, 1, 0), 0);
 		zeppelin 	= new Zeppelin(node); 
 		     
         manipulate(0.01f);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see Entity#manipulate(float, World)
 	 */
 	@Override
 	void manipulate(float dt) {    
 		// Yaw Angle
 		rotation = Transform.rotate(new Vector3(0, 1, 0), yRotVelocity*dt).mul(rotation);
 
 		// Limit pitch to 45 deg
 		float pitch = (float) Math.asin(xform.extractRotation().getMatrix().get(1, 2));
 		if (pitch<-0.5){
 			xRotVelocity = (float) Math.min(xRotVelocity, 0);
 		}
 		if (pitch>0.7){
 			xRotVelocity = Math.max(xRotVelocity, 0);
 		}
 		
 		// Pitch angle 
 		rotation = rotation.mul(Transform.rotate(new Vector3(1, 0, 0), xRotVelocity * dt));
 		
 		// Roll angle
 		rotation = rotation.mul(Transform.rotate(new Vector3(0, 0, 1), -roll ));
 		roll  	 = -yRotVelocity*velocity/80f;
 		rotation = rotation.mul(Transform.rotate(new Vector3(0, 0, 1), roll));
 
 		translation = 
 			translation.mul(
 					Transform.translate(rotation.getMatrix().mulDir(new Vector3(0, 0, velocity * dt))));
 
 		// Gravity, Gas and Balance
 		float overAllGravity = gravity+gas-load;
 		
 		Vector3 position = translation.extractTranslation().getMatrix().translation();
		/* +300 wegen Translate des Gelndes */
		if (position.y()<=terrain.getElevation(position.x()+300,position.z()+300)+3){
 			//TODO wenn das Terrain steigt, sollte man wieder hochkommen
 			overAllGravity = Math.max(0, overAllGravity);
 		}
 		
 		translation = translation.mul(Transform.translate(0,overAllGravity,0));
 
 		//TODO Alle Velocities in einen Vektor
 		// Friction
 		velocity 	*= 1-(friction*Math.abs(velocity));
 		velocity 	= Math.abs(velocity) < 0.01 ? 0 : velocity;
 
 		yRotVelocity *= 1-(friction);//*Math.abs(yRotVelocity));
 		yRotVelocity = Math.abs(yRotVelocity) < 0.01 ? 0 : yRotVelocity;
 
 		xRotVelocity *= 1-(friction);//*Math.abs(xRotVelocity));
 		xRotVelocity = Math.abs(xRotVelocity) < 0.01 ? 0 : xRotVelocity;
         update();
     }
 
 	
 	public void update() {
 		xform = translation.mul(rotation);
 		node.setTransform(xform);
 		
 		zeppelin.updateState(gas,load);
 		terrain.postPosition(translation.extractTranslation().getMatrix().translation());

 	}
 
 	public void accelerate(float direction) {
 		velocity -= direction * acceleration;
 	}
 
 	public void turn(float direction) {
 		yRotVelocity += direction * rotAcceleration * Math.min(Math.abs(velocity),1);
 	}
 
 	public void pitch(float direction) {
 		xRotVelocity += direction * rotAcceleration * Math.min(Math.abs(velocity),1);
 	}
 
 	public void balast(int i) {
 		load -= 0.001 * i;
 		load =  Math.max(0, load);
 	}
 
 	public void gaz(int i) {
 		gas	-= 0.001 * i;      
 		gas =  Math.max(0, gas);
 	}
 	
 	public void reset(){
 		gas  = STARTGAS;
 		load = STARTLOAD;
 		// Back to start
     	translation = Transform.translate(new Vector3(3, 10, 0));
     	rotation = Transform.rotate(new Vector3(0, 1, 0), 0);
     	velocity = xRotVelocity = yRotVelocity = 0;
     
    	update();    
 	}
 
 }
