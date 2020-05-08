 package rsmg.model.object.unit;
 
 import java.util.Collection;
 
 import rsmg.io.CharacterProgress;
 import rsmg.model.Constants;
 import rsmg.model.ObjectName;
 import rsmg.model.object.InteractiveObject;
 import rsmg.model.object.bullet.Bullet;
 import rsmg.model.object.item.Item;
 
 /**
  * Class for representing the playable Character
  * @author Johan Gronvall
  * @author Daniel Jonsson
  */
 
 public class PCharacter extends LivingObject {
 	
 	/**
 	 * If the character is airborne (i.e. in the air).
 	 */
 	private boolean airborne;
 	
 	/**
 	 * The weapon that the character is currently equipped with.
 	 */
 	private IWeapon currentWeapon;
 	
 	/**
 	 * Reference to the level's bullet list.
 	 */
 	private Collection<Bullet> bulletList;
 	
 	/**
 	 * Keeps track of when the character last attacked. Used to have a cooldown
 	 * on the attack.
 	 */
 	private long lastAttacktime = 0;
 	
 	/**
 	 * If the character can use the dash move.
 	 */
 	private boolean canDash;
 	
 	/**
 	 * How far the character has traveled with the dash move.
 	 */
 	private double distanceDashed = 0;
 	
 	/**
 	 * If the character is dashing.
 	 */
 	private boolean isDashing = false;
 	
 	/**
 	 * Create a character that the player controls.
 	 * @param x The X coordinate.
 	 * @param y The Y coordinate.
 	 * @param bulletList Reference to the level's bullet list where the character's bullets should be stored.
 	 */
 	public PCharacter(double x, double y, Collection<Bullet> bulletList) {
 		super(x, y, Constants.CHARACTERWIDTH, Constants.CHARACTERHEIGHT, Constants.CHARACTERHEALTH, ObjectName.CHARACTER);
 		this.bulletList = bulletList;	
 		currentWeapon = new RocketLauncher(this, bulletList);
 		canDash = CharacterProgress.dashUnlocked();
 	}
 	
 	@Override
 	public void collide(InteractiveObject obj) {
 		if (obj instanceof Enemy) {
 			this.damage(((Enemy) obj).getTouchDamage());
 		}
 		if (obj instanceof Item) {
 			if(obj.getName().equals(ObjectName.LASER_PISTOL))
 				currentWeapon = new LaserPistol(this, bulletList);
 			else if(obj.getName().equals(ObjectName.HEALTH_PACK))
 				addHealth();
 		}
 	}
 	
 	@Override
 	public void applyGravity(double delta) {
 		// Apply gravity to the character if he is in the air, in other words,
 		// not standing on the ground.
 		if (airborne)
 			super.applyGravity(delta);
 	}
 
 	@Override
 	public void move(double delta) {
 		if (isDashing)
 			dash(delta);
 		
 		super.move(delta);
 	}
 	
 	/**
 	 * Returns a reference to the weapon that the character is currently wielding.
 	 * @return A reference to the weapon that the character is currently wielding.
 	 */
 	public IWeapon getWeapon() {
 		return currentWeapon;
 	}
 	
 	/**
 	 * Update whether the character is in the air or standing on the ground.
 	 * @param airborne If the character is in the air.
 	 */
 	public void setAirborne(boolean airborne) {
 		this.airborne = airborne;
 	}
 	
 	/**
 	 * Is the character airborne, i.e. in the air.
 	 * @return If the character is in the air.
 	 */
 	public boolean isAirborne() {
 		return airborne;
 	}
 	
 	/**
 	 * Adds a velocity upwards to the character, sending him up in the air.
 	 */
 	public void jump() {
 		// Only jump if the character is standing on the ground.
 		if (!airborne)
 			this.addVelocity(0, -Constants.JUMPSTRENGTH);
 	}
 	
 	/**
 	 * Changes the character's velocity, moving him westwards in next loop.
 	 */
 	public void moveLeft() {
 		this.setVelocityX(-Constants.CHARACTERSPEED);
 	}
 	
 	/**
 	 * Changes the character's velocity, moving him eastwards in next loop.
 	 */
 	public void moveRight() {
 		this.setVelocityX(Constants.CHARACTERSPEED);
 	}
 
 	/**
 	 * Called when the user releases the jump key.
 	 */
 	public void jumpReleased() {
 		if (getVelocityY() < Constants.RELEASED_JUMP_VELOCITY)
 			setVelocityY(Constants.RELEASED_JUMP_VELOCITY);
 	}
 
 	/**
 	 * Make the character attack (i.e. shoot with his gun.
 	 */
 	public void attack() {
 		if (lastAttacktime + currentWeapon.getCooldown() < System.currentTimeMillis()) {
 			currentWeapon.shoot();
 			lastAttacktime = System.currentTimeMillis(); 
 		}
 	}
 
 	/**
 	 * Dash with the character.
 	 */
 	public void dash() {
		isDashing = true;
 	}
 
 	/**
 	 * Make the character perform the "dash" move.
 	 */
 	private void dash(double delta) {
 		distanceDashed += Constants.DASHSPEED * delta;
 
 		if (this.isFacingRight())
 			this.setVelocityX(Constants.DASHSPEED);
 		else
 			this.setVelocityX(-Constants.DASHSPEED);
 		
 		if (distanceDashed > Constants.DASHLENGTH) {
 			isDashing = false;
 			distanceDashed = 0;
 		}
 	}
 	
 	/**
 	 * If the character is dashing.
 	 * @return If the character is dashing.
 	 */
 	public boolean isDashing() {
 		return isDashing;
 	}

	/**
	 * If the player can dash.
	 * @return
	 */
	public boolean canDash() {
		return canDash;
	}
 }
