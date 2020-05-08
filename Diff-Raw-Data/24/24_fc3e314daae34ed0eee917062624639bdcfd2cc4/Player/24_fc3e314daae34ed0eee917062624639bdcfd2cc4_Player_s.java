 package mta13438;
 
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
	
 	@Override
 	public String toString() {
 		return "Player [health=" + getHealth() + ", speed=" + super.getSpeed() + ", orientation=" + super.getOrientation() + "]";
 	}
 	
 }
