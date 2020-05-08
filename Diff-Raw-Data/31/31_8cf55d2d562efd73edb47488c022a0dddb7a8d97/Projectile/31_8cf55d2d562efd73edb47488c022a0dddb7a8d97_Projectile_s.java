 package bullets;
 
 import ships.Player;
 import ships.Ship;
 import application_states.PlayerStats;
 import base.GameUtils;
 import base.Runner;
 import base.Updatable;
 import base.events.AddUpdatableEvent;
 
 import com.jme3.bullet.collision.shapes.CollisionShape;
 import com.jme3.bullet.control.GhostControl;
 import com.jme3.math.Vector3f;
 import com.jme3.scene.Spatial;
 
 public abstract class Projectile extends GhostControl implements Updatable {
 	
 	public Gun gun;
 	
 	public boolean valid = true;
 	public Spatial spatial;
 	
 	public Vector3f start, velocity;
 	
 	/**
 	 * Creates and launches new projectile
 	 * 
 	 * @param fired		source ship
 	 * @param source	source location
 	 * @param target	target location
 	 */
 	public Projectile(Gun gun){
 		super();
 		
 		this.gun = gun;
 		
 		CollisionShape c = createNewCollisionShape();
 		c.setScale(new Vector3f(gun.scale,gun.scale,gun.scale));
 		setCollisionShape(c);
 		
 		spatial = createNewSpatial();
 		spatial = spatial.scale(gun.scale);
 		
 		start = gun.fired.getNode().getWorldTranslation();
 		Vector3f shipDir = gun.fired.getNode().getLocalRotation().getRotationColumn(2).normalize();
 		Vector3f translation = shipDir.mult(gun.translation);
 		
 		spatial.setLocalTranslation(start.add(translation));
 		
 		velocity = shipDir.add(GameUtils.randomVector(gun.error)).mult(gun.speed);
 		
 		spatial.rotate(gun.fired.getPhysicsRotation());
 		
 		Runner.getGameState().registerPhysics(this);
 		Runner.getGameState().getRunnableProcessor().addRunnable(new AddUpdatableEvent(this));
 		Runner.getGameState().getRootNode().attachChild(spatial);
 		spatial.addControl(this);
 		
 		if(gun.fired instanceof Player)
 			PlayerStats.shots++;
 	}
 	
 	/**
 	 * Executes a collision between a Projectile and a Ship.
 	 * <p>
 	 * By default, the ship is dealt damage as defined in gun and no explosion is required.
 	 * 
 	 * @param ship Ship hit
 	 * @return whether an explosion is required
 	 */
 	public boolean onCollision(Ship ship){
		//System.out.println(ship.toString() + " is hit by a " + gun.bulletClass + " fired by " + gun.fired + ".");
		ship.life -= gun.damage;
		if(ship instanceof Player){
			PlayerStats.damageTaken += gun.damage;
			PlayerStats.kills += ship.isValid()? 1: 0;
		}
 		if(gun.fired instanceof Player){
			PlayerStats.damageDealt += gun.damage;
 			PlayerStats.hits ++;
 		}
 		return false;
 	}
 	
 	/**
 	 * Generates a spatial for a new projectile
 	 * 
 	 * @return	an instance of a spatial representing the bullet
 	 */
 	public abstract Spatial createNewSpatial();
 	
 	/**
 	 * Generates a collision shape for a new projectile
 	 * 
 	 * @return an instance of a CollisionShape representing the bullet
 	 */
 	public abstract CollisionShape createNewCollisionShape();
 	
 	/**
 	 * Override to add reactive behavior to projectile
 	 */
 	@Override
 	public void updateObject(float tpf){
 		spatial.move(velocity.mult(tpf));
 	}
 	
 	/** 
 	 * Override to add end condition
 	 */
 	public boolean isValid(){
 		if(start.distance(this.getPhysicsLocation()) > gun.maxRange)
 			valid = false;
 		return valid;
 	}
 	
 	/**
 	 * Override to add end behavior
 	 */
 	public void clearModel(){
 		spatial.removeFromParent();
 	}
 }
