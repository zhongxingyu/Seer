 package chalmers.TDA367.B17.model;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.Sound;
 import org.newdawn.slick.geom.*;
 
 import chalmers.TDA367.B17.controller.GameController;
 
 public abstract class AbstractTank extends MovableEntity {
 
 	private String name;
 	private double health;
 	private AbstractPowerUp currentPowerUp;
 	private float turnSpeed; // How many degrees the tank will turn each update
 	protected AbstractTurret turret;
 	protected float turretOffset;
 	private List<AbstractProjectile> projectiles;
 	public boolean fire = true;
 	protected int timeSinceLastShot;
 	public int lastDelta;
	public double lastDir = 270;
 	private Player player;
 	
 	private Sound debugWallHit = null;
 	
 	public AbstractTank(Vector2f velocity, float maxSpeed, float minSpeed, Player player) {
 		super(velocity, maxSpeed, minSpeed);
 		turnSpeed = 0.15f;
 		projectiles = new ArrayList<AbstractProjectile>();
 		currentPowerUp = null;
 		spriteID = "turret";
 		renderLayer = GameController.RenderLayer.SECOND;
 
 		this.player = player;
 		
 		try {
 			debugWallHit = new Sound("data/wall.wav");
         } catch (SlickException e) {
 	        // TODO Auto-generated catch block
 	        e.printStackTrace();
         }
 	}
 	
 	public double getRotation(){
 		return direction.getTheta() + 90;
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
 		if(currentPowerUp != null){
 			if(this.currentPowerUp != null){
 				this.currentPowerUp.deactivate();
 			}
 			this.currentPowerUp = currentPowerUp;
 		}else if(currentPowerUp == null){
 			this.currentPowerUp = null;
 		}
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
 		getTurret().destroy();
 		this.turret = turret;
 	}
 
 	public void turnLeft(int delta){
 		lastDir = getDirection().getTheta();
 		setDirection(getDirection().add(-turnSpeed * delta * (Math.abs(getSpeed())*0.2f + 0.7)));
 	}
 	
 	public void turnRight(int delta){
 		lastDir = getDirection().getTheta();
 		setDirection(getDirection().add(turnSpeed * delta * (Math.abs(getSpeed())*0.2f + 0.7)));
 	}
 
 	public float getTurretOffset() {
 	    return turretOffset;
     }
 	
 	public List<AbstractProjectile> getProjectiles(){
 		return projectiles;
 	}
 	
 	public void addProjectile(AbstractProjectile projectile){
 		projectiles.add(projectile);
 	}
 	
 	public void update(int delta){
 		lastDelta = delta;
 		super.update(delta);
 		timeSinceLastShot -= delta;
 	}
 	
 	public void fireWeapon(int delta){
 		timeSinceLastShot -= delta;
 		if(timeSinceLastShot <= 0 && fire){
 			turret.fireWeapon(delta, this);
 			timeSinceLastShot = turret.getFireRate();
 		}
 	}
 
 	@Override
 	public void didCollideWith(Entity entity){
 		if(entity instanceof MapBounds || entity instanceof AbstractTank || entity instanceof AbstractObstacle){
 			/*if(entity instanceof AbstractTank){
 				if(Math.abs(((AbstractTank)entity).getSpeed()) > Math.abs(getSpeed())){
 					setSpeed(-getSpeed());
 				}else if(Math.abs(((AbstractTank)entity).getSpeed()) < Math.abs(getSpeed())){
 					((AbstractTank)entity).setSpeed(-((AbstractTank)entity).getSpeed());
 				}else{
 					((AbstractTank)entity).setSpeed(-((AbstractTank)entity).getSpeed());
 					setSpeed(-getSpeed());
 				}
 			}*/
 			if(lastPos != getPosition()){
 				setPosition(lastPos);
 				setSpeed(-getSpeed());
 			}
 			if(lastDir != getDirection().getTheta()){
 				setDirection(new Vector2f(lastDir));
 			}
 		}
 	}
 	
 	public void recieveDamage(AbstractProjectile ap){
 		setHealth(getHealth() - ap.getDamage());
 		if(getHealth() <= 0){
 			tankDeath();
 		}
 	}
 	
 	public void tankDeath(){
 		if(getCurrentPowerUp() != null)
 			getCurrentPowerUp().deactivate();
 		this.destroy();
 		player.tankDeath();
 	}
 	
 	@Override
 	public void destroy(){
 		super.destroy();
 		fire = false;
 		active = false;
 		getTurret().destroy();
 	}
 }
 
 
 
