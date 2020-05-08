 package com.flipptor.orbgame;
 
 
 /**
  * A class containing the status for an arbitrary entity.
  * 
  * @author Viktor Anderling
  *
  */
 public class Status {
 	
 	/** The entity's current health. */
 	private float health = 1;
 	
 	/** The max health for this entity. */
 	private int maxHealth = 1;
 	
 	/** The amount of health that regenerates per second. */
 	private float healthRegen = 0;
 	
 	/**	If the entity currently should regenerate health. */
 	private boolean regenerateHealth = false;
 	
 	/** The time of the last health regeneration */
 	private long lastRegTime;
 	
 	/** If the entity can take damage or not. */
 	private boolean isInvincible = false;
 	
 	/** If the entity has taken lethal damage. */
 	private boolean isDead = false;
 	
 	/** The level of this entity's firing ability. */
 	private int firingLevel = 0;
 	
 	/** The level of this entity's dashing ability. */
 	private int dashingLevel = 0;
 	
 	/** The number of consecutive dashes allowed for this entity. */
 	private int consecutiveDashes = 1;
 	
 	
 	public Status() {}
 	
 	/**
 	 * Updates the status, by for example regenerating the health for the
 	 * amount that has passed. Doesn't regenerate a dead entity.
 	 */
 	public void update() {
 		if(regenerateHealth && !isDead) {
 			this.health += healthRegen*1000*(
 					System.currentTimeMillis()-lastRegTime);
 		}
 	}
 	
 	/**
 	 * Sets the entity's current health to a fixed value. May be set higher 
 	 * than max health. Also overrides invincibility. Does not inflict lethal
 	 * damage if set to zero.
 	 * 
 	 * @param Health The health to be set to.
 	 */
 	public void setHealth(float health) {
 		this.health = health;
 	}
 	
 	/**
 	 * Increases the entity's health a given amount. If the health where to 
 	 * exceed max health, health will be set to max health.
 	 * 
 	 * @param health The amount to be healed.
 	 */
 	public void heal(float health) {
 		this.health += health;
 		if(health > maxHealth) {
 			health = maxHealth;
 		}
 	}
 	
 	/**
 	 * Inflicts an amount of damage to the entity. If the entity is invincible, 
 	 * no damage will be inflicted.
 	 * 
 	 * @param damage The amount of damage to be inflicted.
 	 */
 	public void inflictDamage(float damage) {
 		if(!isInvincible) {
 			health -= damage;
 		}
 		if(health <= 0) {
 			isDead = true;
 		}
 	}
 	
 	/**
 	 * Sets the health regeneration speed of the entity. You probably want this
 	 * to be very small.
 	 * 
 	 * @param regenSpeed The amount of health that will regenerate per update.
 	 */
 	public void setHealthRegen(float regenSpeed) {
 		this.healthRegen = regenSpeed;
 	}
 	
 	/**
 	 * Sets whether this entity should regenerate on update or not.
 	 * 
 	 * @param regenerate Set to true if you want it to regenerate,
 	 * false otherwise.
 	 */
 	public void setRegenerate(boolean regenerate) {
 		this.regenerateHealth = regenerate;
 	}
 	
 	/**
 	 * If this entity can't take lethal damage.
 	 * 
 	 * @return True if invincible, false otherwise.
 	 */
 	public boolean isInvincible() {
 		return this.isInvincible;
 	}
 	
 	/**
 	 * Sets invincibility if this entity. An invincible entity cannot take
 	 * lethal damage.
 	 * 
 	 * @param invincible True if you want to set invincible, false otherwise.
 	 */
 	public void setInvincible(boolean invincible) {
 		this.isInvincible = invincible;
 	}
 	
 	/**
 	 * Whether this entity is dead or not.
 	 * 
 	 * @return True if dead, false otherwise.
 	 */
 	public boolean isDead() {
 		return this.isDead;
 	}
 	
 	/**
 	 * Kills the entity. This doesn't count as lethal damage so death will not
 	 * be prevented by invincibility.
 	 */
 	public void kill() {
 		this.isDead = true;
 	}
 	
 	/**
 	 * @return The firing level of this entity.
 	 */
 	public int getFiringLevel() {
 		return this.firingLevel;
 	}
 	
 	/**
 	 * Sets the current firing level to the given level.
 	 * 
 	 * @param firingLevel The new firing level.
 	 */
 	public void setFiringLevel(int firingLevel) {
 		this.firingLevel = firingLevel;
 	}
 	
 	/**
 	 * @return The dashing level of this entity.
 	 */
 	public int getDashingLevel() {
 		return this.dashingLevel;
 	}
 	
 	/**
 	 * Sets the current dashing level to the given level.
 	 * 
	 * @param dashingLevel The new firing level.
 	 */
 	public void setDashingLevel(int dashingLevel) {
 		this.dashingLevel = dashingLevel;
 	}
 	
 	/**
 	 * @return The number of consecutive dashes allowed for this entity.
 	 */
 	public int getConsecutiveDashes() {
 		return this.consecutiveDashes;
 	}
 	
 	/**
 	 * Sets the allowed number of consecutive dashes to the given amount.
 	 * 
 	 * @param nOfDashes The number of consecutive dashes allowed.
 	 */
 	public void setConsecutiveDashes(int nOfDashes) {
 		this.consecutiveDashes = nOfDashes;
 	}
 }
