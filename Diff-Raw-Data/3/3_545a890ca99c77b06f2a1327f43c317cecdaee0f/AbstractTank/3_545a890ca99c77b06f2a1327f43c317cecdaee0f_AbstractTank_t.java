 package chalmers.TDA367.B17.model;
 
 import java.awt.Point;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.newdawn.slick.geom.*;
 
 import chalmers.TDA367.B17.controller.TanskController;
 
 public abstract class AbstractTank extends MovableEntity {
 
 	private String name;
 	private double health;
 	private AbstractPowerUp currentPowerUp;
 	private float turnSpeed; // How many degrees the tank will turn each update
 	protected AbstractTurret turret;
 	protected float turretOffset;
 	private List<AbstractProjectile> projectiles;
 	public boolean fire = true;
 	protected int timeSinceLastShot = 0;
 	
 	public AbstractTank(Vector2f velocity, float maxSpeed, float minSpeed) {
 		super(velocity, maxSpeed, minSpeed);
 		turnSpeed = 3f;
 		projectiles = new ArrayList<AbstractProjectile>();
 	}
 	/**
 	 * Get the position of the tank's image.
 	 * @return The position of the tank's image.
 	 */
 	public Vector2f getImagePosition(){
 		return new Vector2f(position.x - size.x/2, position.y - size.y/2);
 	}
 	
 	public double getRotation(){
 		return direction.getTheta();
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
 
 	public AbstractPowerUp getCurrentPowerUp() {
 		return currentPowerUp;
 	}
 
 	public void setCurrentPowerUp(AbstractPowerUp currentPowerUp) {
 		this.currentPowerUp = currentPowerUp;
 	}
 
 	public float getTurnSpeed() {
 		return turnSpeed;
 	}
 
 	public void setTurnSpeed(float turnSpeed) {
 		this.turnSpeed = turnSpeed;
 	}
 	
 	public AbstractTurret getTurret() {
 		return turret;
 	}
 
 	public void setTurret(AbstractTurret turret) {
 		this.turret = turret;
 	}
 
 	public void turnLeft(int delta){
 		setDirection(getDirection().add(-turnSpeed * delta/60 * (Math.abs(getSpeed())*0.2f + 0.7)));
 	}
 	
 	public void turnRight(int delta){
 		setDirection(getDirection().add(turnSpeed * delta/60 * (Math.abs(getSpeed())*0.2f + 0.7)));
 	}
 
 	public float getTurretOffset() {
 	    return turretOffset;
     }
 	
 	public List<AbstractProjectile> getProjectiles(){
 		return projectiles;
 	}
 	
 	public void addProj(AbstractProjectile ap){
 		projectiles.add(ap);
 	}
 	
 	public void update(int delta){
 		super.update(delta);
 		updateTurret();
		timeSinceLastShot -= delta;
 	}
 
 	private void updateTurret() {
 		Point mouseCoords = TanskController.getInstance().getMouseCoordinates();
 		
 		float rotation = (float) Math.toDegrees(Math.atan2(turret.getPosition().x - mouseCoords.x + 0, turret.getPosition().y - mouseCoords.y + 0)* -1)+180;
         turret.setRotation(rotation);  
         
         double tankRotation = getRotation(); 
         float newTurX = (float) (position.x + turretOffset * Math.cos(Math.toRadians(tankRotation + 180)));
         float newTurY = (float) (position.y - turretOffset * Math.sin(Math.toRadians(tankRotation)));
       	
 		turret.setPosition(new Vector2f(newTurX, newTurY));
     }
 	
 	public void fireWeapon(int delta){
 		if(timeSinceLastShot < 0 && fire){
 			turret.fireWeapon(delta, this);
 			timeSinceLastShot = turret.getFireRate();
 		}
 	}
 }
