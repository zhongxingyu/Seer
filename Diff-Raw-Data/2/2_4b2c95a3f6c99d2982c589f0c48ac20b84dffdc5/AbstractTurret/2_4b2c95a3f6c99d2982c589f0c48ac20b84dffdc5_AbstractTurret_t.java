 package chalmers.TDA367.B17.model;
 
 import chalmers.TDA367.B17.controller.TanskController;
 import org.newdawn.slick.geom.Point;
 import org.newdawn.slick.geom.Shape;
 import org.newdawn.slick.geom.Transform;
 import org.newdawn.slick.geom.Vector2f;
 
 public abstract class AbstractTurret extends Entity {
 	protected float angle;
 	protected Vector2f turretCenter;
 	protected float turretLength;
 	protected int fireRate;
 	protected String projectileType;
 	protected AbstractTank tank;
 
 	/**
 	 * Create a new AbstractTurret.
 	 */
 	public AbstractTurret(AbstractTank tank) {
 		super();
 		angle = 0;
 		spriteID = "turret";
 		this.tank = tank;
 		//Shape nonRotatedTankShape = tank.getShape().transform(Transform.createRotateTransform(-(float)Math.toRadians(getTank().getRotation())), );
 		setShape(new Point(tank.getPosition().x, tank.getPosition().y+tank.getTurretOffset()));
 	}
 	
 	public AbstractTank getTank(){
 		return tank;
 	}
 
 	/**
 	 * Get the fire rate.
 	 * @return the delay between shots in milliseconds
 	 */
 	public int getFireRate(){
 		return fireRate;
 	}
 
 	@Override
 	public Vector2f getSpritePosition(){
 		return new Vector2f(getPosition().x - turretCenter.x, getPosition().y - turretCenter.y);
 	}
 	
 	@Override
 	public double getRotation() {
 	    return angle;
     }
 
 	/**
 	 * Set the rotation angle of the turret.
 	 * @param angle the new angle
 	 */
 	public void setRotation(float angle) {
 		this.angle = angle % 360.0f;
     }
 
 	/**
 	 * Get the turret center position represented by a vector.
 	 * @return a new vector with the coordinates of the center of the turret
 	 */
 	public Vector2f getTurretCenter(){
 		return new Vector2f(turretCenter);
 	}
 
 	/**
 	 * Get the position of the nozzle of the turret.
 	 * @return the position of the nozzle
 	 */
 	public Vector2f getTurretNozzle(){
 		double turretRotation = getRotation(); 
 		float nozzleX = (float) (getPosition().x - turretLength * Math.sin(Math.toRadians(turretRotation + 0)));
 		float nozzleY = (float) (getPosition().y + turretLength * Math.cos(Math.toRadians(turretRotation )));
 		
 		return new Vector2f(nozzleX, nozzleY);
 	}
 	
 	public void setFireRate(int fireRate) {
 		this.fireRate = fireRate;
 	}
 
 	/**
 	 * Shoots projectiles.
 	 * @param delta time in milliseconds since last game update
 	 * @param tank the tank (owner of this turret) shooting
 	 */
 	public abstract void fireWeapon(int delta, AbstractTank tank);
 
 	/**
 	 * Defines which type of projectiles to create.
 	 * @return a new projectile
 	 */
 	public abstract AbstractProjectile createProjectile();
 
 	/**
 	 * Creates and spawns a new projectile.
 	 * @return the new projectile
 	 */
 	public AbstractProjectile spawnNewProjectile(){
 		AbstractProjectile projectile = createProjectile();
 		projectile.setDirection(new Vector2f(getRotation() + 90));
 		projectile.setPosition(getTurretNozzle());
 		return projectile;
 	}
 
 	@Override
 	public void update(int delta){
 		java.awt.Point mouseCoordinates = TanskController.getInstance().getMouseCoordinates();
 
 		float rotation = (float) Math.toDegrees(Math.atan2(this.getPosition().x - mouseCoordinates.x + 0, this.getPosition().y - mouseCoordinates.y + 0)* -1)+180;
 		this.setRotation(rotation);
 
		double tankRotation = tank.getRotation() - 90;
 		float newTurX = (float) (tank.getPosition().x + tank.getTurretOffset() * Math.cos(Math.toRadians(tankRotation + 180)));
 		float newTurY = (float) (tank.getPosition().y - tank.getTurretOffset() * Math.sin(Math.toRadians(tankRotation)));
 
 		this.setPosition(new Vector2f(newTurX, newTurY));
 	}
 }
