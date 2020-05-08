 package chalmers.TDA367.B17.model;
 
 import java.util.ArrayList;
 
 import org.newdawn.slick.geom.Vector2f;
 
 import chalmers.TDA367.B17.controller.GameController;
 import chalmers.TDA367.B17.event.GameEvent;
 import chalmers.TDA367.B17.event.GameEvent.EventType;
 import chalmers.TDA367.B17.powerups.Shield;
 import chalmers.TDA367.B17.weapons.FlamethrowerProjectile;
 
 
 public abstract class AbstractTank extends MovableEntity {
 	private String name;
 	private double health;
 	private AbstractPowerUp currentPowerUp;
 	private float turnSpeed; // How many degrees the tank will turn each update
 	protected AbstractTurret turret;
 	protected float turretOffset;
 	protected int timeSinceLastShot;
 	private boolean fire;
 	private double lastDirection;
 	public static final double MAX_HEALTH = 100;
 	public static final double MAX_SHIELD_HEALTH = 50;
 	private Shield shield;
 	private ArrayList<AbstractProjectile> projectiles;
 	private Player player;
 	private double maxShieldHealth;
 	
 	public static final String TANK_DEATH_EVENT = "TANK_DEATH_EVENT";
 	
 	/**
 	 * Create a new AbstractTank.
 	 * @param velocity The velocity of this tank
 	 * @param maxSpeed The maximum movement speed of this tank
 	 * @param minSpeed The minimum movement speed of this tank
 	 * @param player The owning player of this tank
 	 */
 	public AbstractTank(int id, Vector2f direction, float maxSpeed, float minSpeed, Player player) {
 		super(id, direction, maxSpeed, minSpeed);
 		this.player = player;
 		turnSpeed = 0.15f;
 		currentPowerUp = null;
 		spriteID = "turret";
 		renderLayer = RenderLayer.SECOND;
 		fire = true;
 		projectiles = new ArrayList<AbstractProjectile>();
 		maxShieldHealth = MAX_SHIELD_HEALTH;
 	}
 	
 	/**
 	 * Get the rotation of this tank.
 	 * @return The rotation of this tank
 	 */
 	public double getRotation(){
 		return direction.getTheta() + 90;
 	}
 	
 	/**
 	 * Get the name of this tank.
 	 * @return The name
 	 */
 	public String getName() {
 		return name;
 	}
 
 	/**
 	 * Set the name of this tank.
 	 * @param name The new name
 	 */
 	public void setName(String name) {
 		this.name = name;
 	}
 	
 	/**
 	 * Return the max health of the tank.
 	 * @return max_health
 	 */
 	public double getMaxHealth(){
 		return MAX_HEALTH;
 	}
 
 	/**
 	 * Get the current health of this tank.
 	 * @return The health of this tank
 	 */
 	public double getHealth() {
 		return health;
 	}
 
 	/**
 	 * Set the health of this tank.
 	 * @param health The new health
 	 */
 	public void setHealth(double health) {
 		this.health = health;
 		if(this.health <= 0){
 			tankDeath();
 		}
 	}
 
 	/**
 	 * Get the power up that is currently active for this tank.
 	 * @return The current power up
 	 */
 	public AbstractPowerUp getCurrentPowerUp() {
 		return currentPowerUp;
 	}
 
 	/**
 	 * Set the current power up of this tank.
 	 * @param currentPowerUp The new power up
 	 */
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
 
 	/**
 	 * Get the turn speed of this tank.
 	 * @return The turn speed
 	 */
 	public float getTurnSpeed() {
 		return turnSpeed;
 	}
 
 	/**
 	 * Set the turn speed of this tank.
 	 * @param turnSpeed The new turn speed
 	 */
 	public void setTurnSpeed(float turnSpeed) {
 		this.turnSpeed = turnSpeed;
 	}
 	
 	/**
 	 * Get the turret of this tank.
 	 * @return The turret
 	 */
 	public AbstractTurret getTurret() {
 		return turret;
 	}
 
 	/**
 	 * Set the turret of this tank.
 	 * @param turret The new turret
 	 */
 	public void setTurret(AbstractTurret turret) {
 		if(getTurret() != null)
 			getTurret().destroy();
 		this.turret = turret;
 	}
 
 	/**
 	 * Turn the tank left, adjusting the direction it's facing.
 	 * @param delta The time that has passed since the last update in milliseconds
 	 */
 	public void turnLeft(int delta){
 		lastDirection = getDirection().getTheta();
 		setDirection(getDirection().add(-turnSpeed * delta * (Math.abs(getSpeed())*0.2f + 0.7)));
 	}
 	
 	/**
 	 * Turn the tank right, adjusting the direction it's facing.
 	 * @param delta The time that has passed since the last update in milliseconds
 	 */
 	public void turnRight(int delta){
 		lastDirection = getDirection().getTheta();
 		setDirection(getDirection().add(turnSpeed * delta * (Math.abs(getSpeed())*0.2f + 0.7)));
 	}
 	
 	/**
 	 * Get the offset between the tanks position and the turret position.
 	 * @return turretOffset
 	 */
 	public float getTurretOffset() {
 	    return turretOffset;
     }
 	
 	public void update(int delta){
 		super.update(delta);
 
 		timeSinceLastShot -= delta;
 		
 		// moved here from AbstractTurret
 		double tankRotation = getRotation() - 90;
 		float newTurX = (float) (getPosition().x + getTurretOffset() * Math.cos(Math.toRadians(tankRotation + 180)));
 		float newTurY = (float) (getPosition().y - getTurretOffset() * Math.sin(Math.toRadians(tankRotation)));
 		turret.setPosition(new Vector2f(newTurX, newTurY));
 	}
 	
 	/**
 	 * Fire the tanks turret if the turrets has cooled down.
 	 * @param delta
 	 */
 	public void fireWeapon(int delta){
 		if(timeSinceLastShot <= 0 && fire){
 			turret.fireWeapon(delta, this);
 			timeSinceLastShot = turret.getFireRate();
 		}
 	}
 
 	@Override
 	public void didCollideWith(Entity entity){
 		if(entity instanceof MapBounds || entity instanceof AbstractTank || entity instanceof AbstractObstacle){
 			if(!lastPos.equals(getPosition())){
 				setPosition(lastPos);
				setSpeed(-(getSpeed()*0.5f));
 				double tankRotation = getRotation() - 90;
 				float newTurX = (float) (getPosition().x + getTurretOffset() * Math.cos(Math.toRadians(tankRotation + 180)));
 				float newTurY = (float) (getPosition().y - getTurretOffset() * Math.sin(Math.toRadians(tankRotation)));
 				turret.setPosition(new Vector2f(newTurX, newTurY));
 			}
 			if(lastDirection != getDirection().getTheta()){
 				setDirection(new Vector2f(lastDirection));
 			}
 		}
 	}
 	
 	/**
 	 * The tank takes damage and take appropriate action is taken.
 	 * @param projectile
 	 */
 	public void recieveDamage(AbstractProjectile projectile){
 		if(!(projectile instanceof FlamethrowerProjectile))
 			GameController.getInstance().getWorld().handleEvent(new GameEvent(EventType.SOUND, this, "TANK_HIT_EVENT"));
 		setHealth(getHealth() - projectile.getDamage());
 	}
 	
 	/**
 	 * The tank is eliminated and appropriate action is taken.
 	 */
 	public void tankDeath(){
 		GameController.getInstance().getWorld().handleEvent(new GameEvent(EventType.SOUND, this, TANK_DEATH_EVENT));
 		GameController.getInstance().getWorld().handleEvent(new GameEvent(EventType.ANIM, this, TANK_DEATH_EVENT));
 		if(getCurrentPowerUp() != null)
 			getCurrentPowerUp().deactivate();
 		this.destroy();
 		player.tankDeath();
 	}
 	
 	@Override
 	public void destroy(){
 		super.destroy();
 		if(getCurrentPowerUp() != null)
 			getCurrentPowerUp().deactivate();
 		fire = false;
 		active = false;
 		getTurret().destroy();
 	}
 
 	/**
 	 * Get the last direction of the tank.
 	 * @return lastDirection
 	 */
 	public double getLastDir() {
 		return lastDirection;
 	}
 
 	/**
 	 * Set the last direction of the tank.
 	 * @param lastDirection
 	 */
 	public void setLastDir(double lastDirection) {
 		this.lastDirection = lastDirection;
 	}
 
 	/**
 	 * Set the turret offset of the tank.
 	 * @param turretOffset
 	 */
 	public void setTurretOffset(float turretOffset) {
 		this.turretOffset = turretOffset;
 	}
 	
 	/**
 	 * Get the max shield health of the tank.
 	 * @return
 	 */
 	public double getMaxShieldHealth(){
 		return maxShieldHealth;
 	}
 	
 	/**
 	 * Get the tanks shield.
 	 * @return shield
 	 */
 	public Shield getShield(){
 		return shield;
 	}
 	
 	/**
 	 * Set the tanks shield.
 	 * @param shield
 	 */
 	public void setShield(Shield shield){
 		this.shield = shield;
 	}
 
 	/**
 	 * Add a projectile to the tank's list of projectiles.
 	 * @param projectile
 	 */
 	public void addProjectile(AbstractProjectile projectile) {
 		projectiles.add(projectile);   
     }
 
 	/**
 	 * Return the list of the tank's projectiles.
 	 * @return projectiles
 	 */
 	public ArrayList<AbstractProjectile> getProjectiles() {
 		return projectiles;
     }
 }
 
 
 
