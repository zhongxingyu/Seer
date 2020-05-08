 package chalmers.TDA367.B17.model;
 
 import org.newdawn.slick.geom.*;
 
 public abstract class AbstractTank extends MovableEntity{
 
 	private String name;
 	private double health;
 	private AbstractWeapon currentWeapon;
 	private AbstractPowerUp currentPowerUp;
 	private double turnSpeed; // How many degrees the tank will turn each update
 	private Vector2f turretDirection;
 	
 	public AbstractTank(int id, Vector2f velocity, double maxSpeed, double minSpeed, double reverseSpeed) {
 		super(id, velocity, maxSpeed, minSpeed, reverseSpeed);
 		//currentWeapon = Weapons.DEFAULT_WEAPON;
 		// TODO
 	}
 	
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public double getHealth() {
 		return health;
 	}
 
 	public void setHealth(double health) {
 		this.health = health;
 	}
 
 	public AbstractWeapon getCurrentWeapon() {
 		return currentWeapon;
 	}
 
 	public void setCurrentWeapon(AbstractWeapon currentWeapon) {
 		this.currentWeapon = currentWeapon;
 	}
 
 	public AbstractPowerUp getCurrentPowerUp() {
 		return currentPowerUp;
 	}
 
 	public void setCurrentPowerUp(AbstractPowerUp currentPowerUp) {
 		this.currentPowerUp = currentPowerUp;
 	}
 
 	public double getTurnSpeed() {
 		return turnSpeed;
 	}
 
 	public void setTurnSpeed(double turretTurnSpeed) {
 		this.turnSpeed = turretTurnSpeed;
 	}
 
 	public Vector2f getTurretDirection() {
 		return turretDirection;
 	}
 	
 	public void turn(){
		setVelocity(getDirection().add(turnSpeed));
 	}
 
 	public void setTurretDirection(Vector2f turretDirection) {
 		this.turretDirection = turretDirection;
 	}
 
 }
