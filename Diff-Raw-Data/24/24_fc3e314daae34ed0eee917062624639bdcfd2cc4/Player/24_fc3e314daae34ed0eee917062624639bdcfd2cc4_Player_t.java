 package mta13438;
 
import org.lwjgl.openal.AL10;

 public class Player extends Entity {
 	
 	private int health;
 	
 	public Player() {
 		super();
 		setHealth(0);
 	}
 	public Player(Point pos, float speed, float orientation, int health) {
 		super(pos, speed, orientation);
 		setHealth(health);
 	}	
 	
 	public int getHealth() {
 		return health;
 	}
 	public void setHealth(int health) {
 		this.health = health;
 	}
	public void setListener(){ // Since only one listener will/can exist at a time, this function 
							   // initializes listener values, using the position values of the object.
					
			//Uses getters from the entity class
			AL10.alListener3f(AL10.AL_POSITION,   getPos().getX(), getPos().getY(), getPos().getZ());
			AL10.alListenerf(AL10.AL_VELOCITY,    getSpeed());
			AL10.alListenerf(AL10.AL_ORIENTATION, getOrientation());
			//Haven't been tested yet
	}
	public void kill(){
		//Death sequence and reposition
		//Play death sound
	}
 	@Override
 	public String toString() {
 		return "Player [health=" + getHealth() + ", speed=" + super.getSpeed() + ", orientation=" + super.getOrientation() + "]";
 	}
 	
 }
