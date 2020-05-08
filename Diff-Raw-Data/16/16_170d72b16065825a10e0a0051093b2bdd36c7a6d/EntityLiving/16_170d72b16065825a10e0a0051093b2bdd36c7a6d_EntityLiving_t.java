 package entities;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import enums.EnumColor;
 
 import blocks.Block;
 
 import statuseffects.StatusEffect;
 import statuseffects.StatusEffectAbsorb;
 import utils.MathHelper;
 import world.World;
 
 /**
  * <code>EntityLiving</code> extends <code>Entity</code> and implements Serializable (as an extension of Entity)
  * <br>
  * It provides most of the features required for an Entity
  * that is alive. The list of methods for EntityLiving is extensive. It includes methods to: 
  * <br><br>
  * <li>Apply gravity: {@link #applyGravity(World)}
  * <li>Check for nearby blocks: {@link #blockInBounds(World, int, int, int, int, Block)}
  * <li>Check for vertexes inside the Entity: {@link #inBounds(double, double)}, or {@link #inBounds(double, double, double, double)}
  * <li>Check if the entity has health left: {@link #isDead()}
  * <li>Damage the entity: {@link #damageEntity(World, int, boolean)} 
  * <li>Handle a jump: {@link #hasJumped()}
  * <li>Handle movement: {@link #moveEntityDown(World)}, {@link #moveEntityUp(World)}, {@link #moveEntityLeft(World)}, {@link #moveEntityRight(World)}
  * <li>Determine movement: {@link #isWalkingSafe(World, boolean)}, {@link #isJumpNeeded(World, boolean, boolean)}, {@link #isJumpPossibleAndNeeded(World, boolean, boolean)}
  * <li>Launch projectiles: {@link #launchProjectile(World, EntityProjectile)}
  * <li>Heal the entity: {@link #healEntity(World, int)}
  * <li>Implement custom death behaviour: {@link #onDeath()}
  * <br><br>
  * More advanced features can be added by further extending the class. All methods in EntityLiving
  * are designed to scale with the entity's size, health, etc.
  * <br>
  * @author      Alec Sobeck
  * @author      Matthew Robertson
  * @version     1.0
  * @since       1.0
  */
 public class EntityLiving extends Entity
 {
 	private static final long serialVersionUID = 1L;
 	/** The flat damage reduction provided by 1 point of defense */
 	public final static double DEFENSE_REDUCTION_FLAT = 0.375F;
 	/** The percent of damage reduction provided by 1 point of defense (from 0-1F, where 1F is 100%)*/
 	public final static double DEFENSE_REDUCTION_PERCENT = 0.25F / 100F;
 	public boolean isFireImmune;
 	public double attackSpeedModifier;
 	public double knockbackModifier;
 	public double meleeDamageModifier;
 	public double rangeDamageModifier;
 	public double magicDamageModifier;
 	public double allDamageModifier;	
 	public List<StatusEffect> statusEffects;	
 	/** (chance to crit / 100) */
 	public double criticalStrikeChance; 
 	/** (Chance to dodge / 100) */
 	public double dodgeChance;
 	public boolean isImmuneToCrits;
 	public boolean isImmuneToFallDamage;
 	public boolean isImmuneToFireDamage;
 	public int invincibilityTicks;	
 	public int maxHealth;
 	public int maxMana;
 	public double mana;
 	public double defense;
 	public double health;
 	public List<StatusEffectAbsorb> absorbs;
 	
 	public String jumpSound;
 	public String deathSound;
 	public String hitSound;
 	
 	/**
 	 * Overrides Entity's constructor, and constructs a new EntityLiving. Initializes combat and life
 	 * related variables in addition to the variables from Entity.
 	 */
 	public EntityLiving()
 	{
 		super();
 		defense = 0;
 		health = 100;
 		maxHealth = 100;
 		mana = 0;
 		maxMana = 0;
 		setUpwardJumpHeight(42); //7 blocks
 		upwardJumpCounter = 0;
 		setMovementSpeedModifier(1.0f);
 		setBaseSpeed(1.0f);
 		distanceFallen = 0;
 		setMaxHeightFallenSafely(72);
 		jumpSpeed = 6;
 		isJumping = false;
 		canJumpAgain = true;
 		isImmuneToFallDamage = false;
 		isImmuneToFireDamage = false;
 		dodgeChance = 0.05f;
 		attackSpeedModifier = 1.0f;
 		knockbackModifier = 1;
 		meleeDamageModifier = 1;
 		rangeDamageModifier = 1;
 		magicDamageModifier = 1;
 		allDamageModifier = 1;		
 		isImmuneToCrits = false;
 		criticalStrikeChance = 0.05f;
 		statusEffects = new ArrayList<StatusEffect>();
 		absorbs = new ArrayList<StatusEffectAbsorb>();		
 		x = 0;
 		y = 0;	
 		setStunned(false);
 		jumpSound = "Player Jump";
 		deathSound = "Player Death";		
 		hitSound = "Generic Hit";
 	}
 	
 	/**
 	 * Creates a deep copy of this EntityLiving. Calls Entity's copy constructor as well.
 	 * @param entity the entityliving to copy
 	 */
 	public EntityLiving(EntityLiving entity)
 	{
 		super(entity);
 
 		this.isFireImmune = entity.isFireImmune;
 		this.attackSpeedModifier = entity.attackSpeedModifier;
 		this.knockbackModifier = entity.knockbackModifier;
 		this.meleeDamageModifier = entity.meleeDamageModifier;
 		this.rangeDamageModifier = entity.rangeDamageModifier;
 		this.magicDamageModifier = entity.magicDamageModifier;
 		this.allDamageModifier = entity.allDamageModifier;			
 		this.statusEffects = new ArrayList<StatusEffect>();		
 		for(int i = 0; i < entity.statusEffects.size(); i++)
 		{
 			this.statusEffects.add(new StatusEffect(entity.statusEffects.get(i)));
 		}		
 		this.absorbs = new ArrayList<StatusEffectAbsorb>();		
 		this.criticalStrikeChance = entity.criticalStrikeChance; 
 		this.dodgeChance = entity.dodgeChance;
 		this.isImmuneToCrits = entity.isImmuneToCrits;
 		this.isImmuneToFallDamage = entity.isImmuneToFallDamage;
 		this.isImmuneToFireDamage = entity.isImmuneToFireDamage;
 		this.invincibilityTicks = entity.invincibilityTicks;		
 		this.maxHealth = entity.maxHealth;
 		this.maxMana = entity.maxMana;
 		this.mana = entity.mana;
 		this.defense = entity.defense;
 		this.health = entity.health;		
 		
 		this.deathSound = entity.deathSound;
 		this.jumpSound = entity.jumpSound;
 		this.hitSound = entity.hitSound;
 	}
 		
 	/**
 	 * Takes the damage done after armour, and tries to remove any absorbs using that damage. Returns the amount of
 	 * damage done to the entity after absorbs are exhausted. <b>Consumes absorbs in the process.</b>
 	 * @param damageAfterArmor the damage taken by the entity after applying defense and armour benefits
 	 * @return the damage taken by the entity after absorbs.
 	 */
 	protected double dealDamageToAbsorbs(double damageAfterArmor)
 	{		
 		double newValue = damageAfterArmor;
 		for(int i = 0; i < absorbs.size(); i++)
 		{
 			if(damageAfterArmor == 0)
 			{
 				break;
 			}
 			newValue = absorbs.get(i).absorbDamage(newValue);
 		}		
 		return newValue;
 	}
 	
 	/**
 	 * Damages the entity for the specified amount
 	 * @param d the amount damage
 	 * @param isCrit was the hit critical? (2x damage)
 	 * @param isDodgeable whether or not the hit can be dodged
 	 * @param showWorldText true if the damage should be shown as world text, otherwise false
 	 */
 	public void damageEntity(World world, int d, boolean isCrit, boolean isDodgeable, boolean showWorldText)
 	{
 		if(invincibilityTicks <= 0) //Check if the entity can be damaged
 		{	
 			double dodgeRoll = Math.random();
 			//Check if the damage was dodged
 			if(isDodgeable && dodgeRoll < dodgeChance || dodgeChance >= 1.0f) //Is it a dodge
 			{
 				//Render world text for the dodge if applicable
 				if(showWorldText)
 				{
 					world.addTemporaryText("Dodge", (int)x - 2, (int)y - 3, 20, EnumColor.GREEN); //add temporary text to be rendered for the damage done
 				}
 			}
 			else if(!isCrit) //Is it Normal Damage
 			{
 				//Double the damage done if it was a critical hit
 				if(isCrit && !isImmuneToCrits)
 				{
 					d *= 2;
 				}
 				//Set the entity immune to damage for 250ms
 				invincibilityTicks = 5; 
 				//Determine the damage after armour, with a floor of 1 damage and then apply absorbs
 				double damageAfterArmor = MathHelper.floorOne(
 						(d * (1F - DEFENSE_REDUCTION_PERCENT * defense)) - (defense * DEFENSE_REDUCTION_FLAT)					
 						);
 				damageAfterArmor = dealDamageToAbsorbs(damageAfterArmor);
 				health -= damageAfterArmor; 
 				world.soundEngine.playSoundEffect(hitSound);
 				//Show world text if applicable
 				if(showWorldText)
 				{
 					String message = (damageAfterArmor == 0) ? "Absorb" : ""+(int)damageAfterArmor;
					world.addTemporaryText(message, (int)x - 2, (int)y - 3, 20, (damageAfterArmor == 0) ? EnumColor.WHITE : EnumColor.RED); 
 				}
 			}		
 		}
 		else //the entity can't be damaged, so reduce their invincibility
 		{
 			invincibilityTicks--;
 		}
 		
 		if(isDead()) //is the entity has died, perform the appropriate action
 		{
 			onDeath(world);
 		}		
 	}
 	
 	/**
 	 * Heals the entity for the specified amount
 	 * @param h the amount healed
 	 * @param showWorldText whether or not to display world text
 	 * @return true if the entity was healed, otherwise false
 	 */
 	public boolean heal(World world, int h, boolean showWorldText)
 	{
 		if(health < maxHealth)
 		{
 			health += h; 
 			
 			if(showWorldText)
 			{
 				//add temperary text to be rendered, for the healing done
 				world.addTemporaryText(""+(int)h, (int)x - 2, (int)y - 3, 20, EnumColor.GREEN); 
 			}
 			if(health > maxHealth) //if health exceeds the maximum, set it to the maximum
 			{
 				health = maxHealth;
 			}	
 			return true;
 		}
 		return false;
 	}
 	
 	/**
 	 * Determines if the EntityLiving has died
 	 * @return whether the entity is dead or not (true = dead)
 	 */
 	public boolean isDead()
 	{
 		return health <= 0;
 	}	
 		
 	/**
 	 * Handles special death events. Generally this should be done by extending EntityLiving and overriding this method
 	 */
 	public void onDeath(World world)
 	{		
 		world.soundEngine.playSoundEffect(deathSound);
 	}	
 			
 	/**
 	 * Applies the periodic bonuses of StatusEffects and then checks if they're expired. If an effect is expired, then
 	 * the StatusEffect is removed from the EntityLiving and no longer has an effect.
 	 * @param world the world that this entity is currently associated with
 	 */
 	public void checkAndUpdateStatusEffects(World world)
 	{
 		for(int i = 0; i < statusEffects.size(); i++)
 		{
 			statusEffects.get(i).applyPeriodicBonus(world, this);
 			if(statusEffects.get(i).isExpired())
 			{
 				statusEffects.get(i).removeInitialEffect(this);
 				if(statusEffects.get(i) instanceof StatusEffectAbsorb)
 				{
 					removeAbsorb((StatusEffectAbsorb)statusEffects.get(i));
 				}
 				statusEffects.remove(i);
 			}
 		}
 	}
 	
 	/**
 	 * Removes an absorb effect from this EntityLiving.
 	 * @param absorb the absorb effect to remove
 	 */
 	private void removeAbsorb(StatusEffectAbsorb absorb)
 	{
 		for(int i = 0; i < absorbs.size(); i++)
 		{
 			if(absorb.getID() == absorbs.get(i).getID())
 			{
 				absorbs.remove(i);
 			}
 		}
 	}
 	
 	/**
 	 * Registers an absorb effect, so that it will take effect and reduce damage taken.
 	 * @param absorb the absorb status effect to register
 	 */
 	protected void registerAbsorb(StatusEffectAbsorb absorb)
 	{
 		absorbs.add(absorb);
 	}
 	
 	/**
 	 * Registers a status effect to this entity, whether it is positive or negative.
 	 * An effect may fail to register if a more powerful, non-stacking effect is already active.
 	 * @param effect the effect to register
 	 * @return whether or not the effect was successfully registered
 	 */
 	public boolean registerStatusEffect(StatusEffect effect)
 	{
 		if(effect instanceof StatusEffectAbsorb)
 		{
 			registerAbsorb((StatusEffectAbsorb)effect);
 		}
 		
 		for(int i = 0; i < statusEffects.size(); i++)
 		{
 			if(statusEffects.get(i).toString().equals(effect.toString()) && !effect.stacksIndependantly)
 			{
 				if(statusEffects.get(i).tier <= effect.tier)
 				{
 					if(statusEffects.get(i).tier == effect.tier && statusEffects.get(i).ticksLeft > effect.ticksLeft)
 					{
 						return false;
 					}
 					else
 					{
 						if(!statusEffects.get(i).reapplicationSkipsRemovalEffect)
 						{
 							statusEffects.get(i).removeInitialEffect(this);
 						}
 						statusEffects.remove(i);						
 						effect.applyInitialEffect(this);
 						statusEffects.add(effect);
 						return true;
 					}
 				}
 				else
 				{
 					return false;
 				}
 			}
 		}
 		
 		effect.applyInitialEffect(this);
 		statusEffects.add(effect);
 		return true;
 	}
 
 	/**
 	 * Applies gravity or a jump upward, depending on if the entity is jumping. In addition, applies fall damage 
 	 * if applicable.
 	 * @param world the world the entity is currently in
 	 */
 	public void applyGravity(World world) 
 	{
 		if(isOnGround(world)) //Is the entity on the ground? If so they can jump again
 		{
 			if((isJumping || !canJumpAgain) && 
 					isAffectedByGravity && 
 					!isImmuneToFallDamage && 
 					distanceFallen > getMaxHeightFallenSafely()) 
 			{
 				//calculate the fall damage
 				double fallDamage = MathHelper.getFallDamage(jumpSpeed, 
 						world.g, 
 						ticksFallen); 
 				if(fallDamage > 0)
 				{
 					damageEntity(world, (int)fallDamage, ((Math.random() < 0.1f) ? true : false), false, true); //damage the entity
 				}
 			}
 			
 			ticksFallen = 0;
 			canJumpAgain = true;
 			distanceFallen = 0;
 		}	
 				
 		if(isJumping) //If the entity is jumping upwards, move them up
 		{
 			moveEntityUp(world, jumpSpeed * getMovementSpeedModifier());			
 		}
 		else if(!isOnGround(world) && isAffectedByGravity) //otherwise, if the entity is in the air, make them fall
 		{
 			moveEntityDown(world, MathHelper.getFallSpeed(jumpSpeed, world.g, ticksFallen));
 			ticksFallen++;
 		}	
 	}
 
 	/**
 	 * Restores mana upto maximum mana. 
 	 * @param m the amount of mana to restore
 	 * @param showWorldText whether or not to show world text
 	 * @return true if mana was restored, otherwise false
 	 */
 	public boolean restoreMana(World world, int m, boolean showWorldText)
 	{
 		if(mana < maxMana)
 		{
 			mana += m; 
 			if(showWorldText)
 			{
 				world.addTemporaryText(""+(int)m, (int)x - 2, (int)y - 3, 20, EnumColor.BLUE);
 			}
 			//if mana exceeds the maximum, set it to the maximum
 			if(mana > maxMana)
 			{
 				mana = maxMana;
 			}
 			return true;
 		}
 		return false;
 	}
 	
 	/**
 	 * Sees if it's possible to jump. If so then the jump will be performed
 	 */
 	public void tryToJumpAgain(World world, EntityPlayer player)
 	{
 		if(canJumpAgain)// If the entity can jump, let them
 		{
 			isJumping = true;
 			upwardJumpCounter = 0;
 			canJumpAgain = false;
 			
 			if (this != player){	
 				double distance = MathHelper.distanceBetweenTwoPoints(this.x, this.y, player.x, player.y) / (12);
 				double percentageVolume = 0;
 				if (distance > 80){
 					percentageVolume = 0;
 				}
 				if (percentageVolume < 0.001){
 					percentageVolume = 0;
 				}
 				//System.out.println("Derp!" + percentageVolume + " Distance: " + distance);
 				//world.soundEngine.playSoundEffect(jumpSound, percentageVolume);
 			}
 			else {
 				world.soundEngine.playSoundEffect(jumpSound);
 			}
 		}	
 	}
 	
 	/**
 	 * Gets whether or not the entity is immune to damage. An entity is immune to damage if invincibilityTicks > 0.
 	 * @return true if the entity is immune to damage, otherwise false
 	 */
 	public boolean isImmuneToDamage()
 	{
 		return invincibilityTicks > 0;
 	}	
 }
