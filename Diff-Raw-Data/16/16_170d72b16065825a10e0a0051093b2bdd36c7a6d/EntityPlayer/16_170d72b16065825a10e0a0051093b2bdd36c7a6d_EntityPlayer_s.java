 package entities;
 import items.Item;
 import items.ItemAmmo;
 import items.ItemArmor;
 import items.ItemMagic;
 import items.ItemRanged;
 import items.ItemTool;
 import items.ItemToolAxe;
 import items.ItemToolHammer;
 import items.ItemToolPickaxe;
 
 import java.util.Dictionary;
 import java.util.Enumeration;
 import java.util.Hashtable;
 import java.util.Random;
 import java.util.Vector;
 
 import org.lwjgl.input.Mouse;
 
 import render.Render;
 import setbonus.SetBonus;
 import setbonus.SetBonusContainer;
 import setbonus.SetBonusFactory;
 import utils.Cooldown;
 import utils.CraftingManager;
 import utils.GemSocket;
 import utils.InventoryPlayer;
 import utils.ItemStack;
 import utils.MathHelper;
 import utils.Recipe;
 import utils.Vector2F;
 import world.World;
 import auras.Aura;
 import auras.AuraContainer;
 import auras.AuraTracker;
 import blocks.Block;
 import blocks.BlockLight;
 import client.TerraeRasa;
 import enums.EnumColor;
 import enums.EnumPlayerDifficulty;
 import enums.EnumToolMaterial;
 
 /**
  * <code>EntityPlayer</code> extends <code>EntityLiving</code> and implements Serializable
  * <br><br>
  * EntityPlayer implements all the features needed for the user's character. 
  * <br><br>
  * There are several methods of significance in EntityPlayer, serving different purposes. 
  * The first is {@link #onWorldTick(World)}. This method should be called every single world tick,
  * and this is how the player gets updated regularly. This should apply things like regen, gravity,
  * and update cooldowns or invincibility (things based on world ticks). onWorldTick(World) can also
  * do things like check nearby blocks, to see if recipes need updated. Anything that should be 
  * done regularly, but not every frame, essentially.
  * <br><br>
  * A second major component of <code>EntityPlayer</code> is recipe updating. The Recipe[] used are based off 
  * of what craftManager decides are possible for the inventory object passed to it. 
  * <code>EntityPlayer</code> provides a method to check for nearby blocks, and update recipes as required 
  * {@link #checkForNearbyBlocks(World)}, based on the result for craftingManager. 
  * <br><br>
  * The third major component of <code>EntityPlayer</code> is armour management. This is done almost 
  * automatically through a single method - {@link #onArmorChange()}. This method recalculates the armor
  * bonuses of the armour, as well as the set bonuses. It then proceeds to cancel the previous ones. 
  * <b>NOTE: This method does dangerous things, and more than likely is going to terribly ruin any attempt 
  * at adding a +defense potion, or -defense debuff. It WILL have to be recoded, with little doubt.</b>
  * <br><br>
  * The final major component of <code>EntityPlayer</code> is block breaking, through the method 
  * {@link #breakBlock(World, int, int, Item)}. This method is what handles mining, and any 
  * gradual block breaking done by the player. A block break is automatically send to the 'world map' upon
  * completion of the mining.
  * @author      Alec Sobeck
  * @author      Matthew Robertson
  * @version     1.0 
  * @since       1.0
  */
 public class EntityPlayer extends EntityLiving
 {
 	private static final long serialVersionUID = 2L;
 	private final static int HEALTH_FROM_STAMINA = 10;
 	private final static int MANA_FROM_INTELLECT = 10;
 	private static final double leftSwingBound = MathHelper.degreeToRadian(-120);
 	private static final double rightSwingBound = MathHelper.degreeToRadian(40);
 	public int viewedChestX;
 	public int viewedChestY;
 	public boolean isViewingChest;	
 	public int strength;
 	public int dexterity;
 	public int intellect;
 	public int stamina;
 	public int baseMaxHealth;
 	public int baseMaxMana;	
 	public double respawnXPos;
 	public double respawnYPos;	
 	
 	private boolean isSwingingRight;
 	private boolean hasSwungTool;
 	private double rotateAngle;
 	
 	public int selectedRecipe;
 	public int selectedSlot;
 	public boolean isFacingRight;
 	public boolean isInventoryOpen;	
 	public InventoryPlayer inventory;
 	
 	private boolean armorChanged;
 	
 	private int ticksSinceLastCast = 99999;
 	private int ticksInCombat;
 	private int ticksOfHealthRegen;
 	private boolean isInCombat;
 	protected boolean isMining;
 	private boolean isReloaded;
 	private int ticksreq;
 	private int sx;
 	private int sy;		
 	private CraftingManager craftingManager;
 	private Recipe[] allPossibleRecipes;
 	private final String playerName;
 	private boolean inventoryChanged;
 
 	private final EnumPlayerDifficulty difficulty;
 	
 	private final int MAXIMUM_BASE_MANA;
 	private final int MAXIMUM_BASE_HEALTH;
 	private final int MAX_BLOCK_PLACE_DISTANCE;
 	private Hashtable<String, Cooldown> cooldowns;
 	public final static int MAX_SPECIAL_ENERGY = 100;
 	public double specialEnergy;
 	private Dictionary<String, Boolean> nearBlock;
 	private Dictionary<String, Recipe[]> possibleRecipesByBlock;
 	private SetBonusContainer currentBonuses; 
 	private AuraTracker auraTracker;
 	
 	/** A flag indicating if the player has been forever defeated. If they have, they will not be saved to disk.*/
 	public boolean defeated;
 	
 	/**
 	 * Constructs a new instance of EntityPlayer with default settings, inventory (includes 3 basic tools),
 	 * and the specified name/difficulty. The default reset position is set to (50, 0), but this
 	 * value is essentially worthless, and should be reset.
 	 * @param name the player's name
 	 * @param difficulty the difficulty setting the player has selected (EnumDifficulty)
 	 */
 	public EntityPlayer(String name, EnumPlayerDifficulty difficulty)
 	{
 		super();		
 		stamina = 0;
 		specialEnergy = 100;
 		isJumping = false;
 		isMining = false;
 		setMovementSpeedModifier(1.0f);		
 		setBaseSpeed(5.0f);
 		width = 12;
 		height = 18;
 		setMaxHeightFallenSafely(78);
 		blockHeight = 3;
 		blockWidth = 2;
 		setRespawnPosition(50, 0);		
 		setUpwardJumpHeight(48);
 		upwardJumpCounter = 0;
 		jumpSpeed = 5;
 		canJumpAgain = true;
 		inventory = new InventoryPlayer();
 		playerName = name;
 		rotateAngle = -120.0f;		
 		isInCombat = false;
 		ticksInCombat = 0;
 		ticksSinceLastCast = 0;
 		health = 100;
 		maxHealth = 100;
 		baseMaxHealth = 100;
 		baseMaxMana = 0;
 		maxMana = 0;
 		this.difficulty = difficulty;
 		invincibilityTicks = 10;
 		selectedSlot = 0;
 		MAX_BLOCK_PLACE_DISTANCE = 42;
 		MAXIMUM_BASE_HEALTH = 400;
 		MAXIMUM_BASE_MANA = 200;
 		viewedChestX = 0;
 		viewedChestY = 0;
 		defeated = false;
 		isImmuneToCrits = false;
 		craftingManager = new CraftingManager();
 		inventoryChanged = true;	
 		selectedRecipe = 0;
 		knockbackModifier = 1;
 		meleeDamageModifier = 1;
 		rangeDamageModifier = 1;
 		magicDamageModifier = 1;
 		allDamageModifier = 1;
 		isReloaded = false;
 		cooldowns = new Hashtable<String, Cooldown>();
 		nearBlock = new Hashtable<String, Boolean>();
 		for(Block block : CraftingManager.getCraftingBlocks())
 		{
 			nearBlock.put(block.getName(), false);
 		}
 		nearBlock.put(Block.none.getName(), true);
 		possibleRecipesByBlock = new Hashtable<String, Recipe[]>();
 		for(Block block : CraftingManager.getCraftingBlocks())
 		{
 			possibleRecipesByBlock.put(block.getName(), new Recipe[] { });
 		}
 		auraTracker = new AuraTracker();
 		
 		jumpSound = "Player Jump";
 		deathSound = "Player Death";
 	}
 	
 	public EntityPlayer(EntityPlayer entity)
 	{
 		throw new RuntimeException("Support Not Implemented - Was this action actually required?");
 	}
 	
 	/**
 	 * Fixes issues from loading a player from disk. This generally relates to applying newer recipes and content, so that the
 	 * player can actually view and use them.
 	 */
 	public void reconstructPlayerFromFile()
 	{	
 		isReloaded = true;
 		rotateAngle = -120.0f;			
 		invincibilityTicks = 10;
 		selectedSlot = 0;
 		viewedChestX = 0;
 		viewedChestY = 0;
 		isViewingChest = false;
 		isMining = false;
 		inventory.initializeInventoryTotals(isReloaded);
 		craftingManager = new CraftingManager();
 		inventoryChanged = true;	
 		selectedRecipe = 0;
 		nearBlock = new Hashtable<String, Boolean>();
 		for(Block block : CraftingManager.getCraftingBlocks())
 		{
 			nearBlock.put(block.getName(), false);
 		}
 		nearBlock.put(Block.none.getName(), true);
 		possibleRecipesByBlock = new Hashtable<String, Recipe[]>();
 		for(Block block : CraftingManager.getCraftingBlocks())
 		{
 			possibleRecipesByBlock.put(block.getName(), new Recipe[] { });
 		}
 		auraTracker = new AuraTracker();
 	}
 	
 	/**
 	 * Updates the player, should only be called each world tick.
 	 */
 	public void onWorldTick(World world)
 	{		
 		invincibilityTicks = (invincibilityTicks > 0) ? --invincibilityTicks : 0;
 		ticksSinceLastCast++;
 		if(armorChanged)
 		{
 			refreshSetBonuses();
 			armorChanged = false;
 		}		
 		checkForCombatStatus();
 		checkAndUpdateStatusEffects(world);
 		applyGravity(world); //Apply Gravity to the player (and jumping)
 		applyHealthRegen(world);
 		applyManaRegen();
 		applySpecialRegen();
 		checkForNearbyBlocks(world);	
 		verifyChestRange();
 		updateCooldowns();
 		auraTracker.update(world, this);
 	}
 	
 	/**
 	 * Updates time in combat, and if the time has exceeded 120 ticks (6 seconds) since the last combat action, combat status is removed.
 	 */
 	private void checkForCombatStatus()
 	{
 		ticksInCombat++;
 		
 		if(ticksInCombat > 120)
 		{
 			ticksInCombat = 0;
 			isInCombat = false;
 		}
 	}
 	
 	/**
 	 * Checks if the player is still near their selected chest (should there be one selected). If the player is not near that
 	 * chest, it is cleared and no longer rendered.
 	 */
 	private void verifyChestRange() 
 	{
 		if(isViewingChest)
 		{
 			if(MathHelper.distanceBetweenTwoPoints(viewedChestX * 6 + 6, viewedChestY * 6 + 6, x + (blockWidth / 2), y + (blockHeight / 2)) >= 48)
 			{
 				clearViewedChest();
 			}			
 		}
 	}
 
 	/**
 	 * Gets the player's name, assigned upon player creation
 	 * @return the player's name
 	 */
 	public final String getName()
 	{
 		return playerName;
 	}
 	
 	/**
 	 * Get every recipe the player is able to craft, based on what they're standing by and what's in 
 	 * their inventory
 	 * @return the allPossibleRecipes[], indicating possible craftable recipes
 	 */
 	public final Recipe[] getAllPossibleRecipes()
 	{
 		return allPossibleRecipes;
 	}
 	
 	public void setIsMining(boolean b){
 		isMining = b;
 	}
 	
 	/**
 	 * Sets the respawn position of the player (this may be broken between world sizes)
 	 * @param x the x position in ortho units
 	 * @param y the y position in ortho units
 	 */
 	public void setRespawnPosition(int x, int y) 
 	{
 		respawnXPos = x;		
 		if(x % 6 != 0)
 		{
 			x = (int)(x / 6);
 		}		
 		respawnYPos = y;
 	}
 	
 	/**
 	 * Inflicts damage that does not trigger combat. This damage will not interrupt things like health regeneration 
 	 * which is dependent on whether or not combat is active. This damage cannot be critical, or dodged, can be 
 	 * affected by the players armor, but is unaffected by invincibility (including triggering it).
 	 * @param damage the damage inflicted
 	 * @param penetratesArmor whether or not the damage penetrates the player's armor points (defense)
 	 */
 	public void damageNonCombat(World world, int damage, boolean penetratesArmor, boolean renderDamage)
 	{
 		if(invincibilityTicks <= 0)
 		{
 			if(!penetratesArmor)
 			{
 				double damageAfterArmor = MathHelper.floorOne(
 						(damage * (1F - DEFENSE_REDUCTION_PERCENT * defense)) - (defense * DEFENSE_REDUCTION_FLAT)									
 						);
 				damageAfterArmor = dealDamageToAbsorbs(damageAfterArmor);
 				health -= damageAfterArmor;
 				auraTracker.onDamageTaken(world, this);
 				if(renderDamage)
 				{
 					String message = (damageAfterArmor == 0) ? "Absorb" : ""+(int)damageAfterArmor;
 					world.addTemporaryText(message, (int)x - 2, (int)y - 3, 20, (damageAfterArmor == 0) ? EnumColor.WHITE : EnumColor.RED);
 				}
 			}
 			else
 			{
 				health -= dealDamageToAbsorbs(damage);
 				auraTracker.onDamageTaken(world, this);
 				if(renderDamage)
 				{
 					world.addTemporaryText(""+(int)damage, (int)x - 2, (int)y - 3, 20, EnumColor.RED);
 				}
 			}
 			
 			if(isDead()) //check if the player has died
 			{
 				onDeath(world);
 				world.clearEntityList();
 			}	
 		}
 	}
 	
 	/**
 	 * Applies gravity or a jump upward, depending on if the entity is jumping
 	 */
 	public void applyGravity(World world) 
 	{
 		if(isOnGround(world)) //Is the entity on the ground? If so they can jump again
 		{
 			//Check for fall damage
 			if((isJumping || !canJumpAgain) && 
 				isAffectedByGravity && 
 				!isImmuneToFallDamage && 
 				distanceFallen > getMaxHeightFallenSafely()) 
 			{
 				//calculate the fall damage
 				double fallDamage = MathHelper.getFallDamage(jumpSpeed, world.g, ticksFallen); 
 				if(fallDamage > 0)
 				{
 					//damage the player with non-combat defense affected damage
 					damageNonCombat(world, (int)fallDamage, true, true); 
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
 	 * Overrides the entity damage taken to account for different invincibility and death
 	 * @param d damage inflicted against the player
 	 * @param isCrit was the hit critical? (2x damage)
 	 * @param isDodgeable whether or not this damage can be dodged
 	 * @param showWorldText true if the damage should be shown as world text, otherwise false
 	 */
 	public void damageEntity(World world, int d, boolean isCrit, boolean isDodgeable, boolean showWorldText)
 	{
 		if(invincibilityTicks <= 0) //If it's possible to take damage
 		{
 			double dodgeRoll = Math.random();
 			//Check if the damage was dodged
 			if(isDodgeable && dodgeRoll < dodgeChance || dodgeChance >= 1.0f) 
 			{
 				//Render world text for the dodge if applicable
 				if(showWorldText)
 				{
 					world.addTemporaryText("Dodge", (int)x - 2, (int)y - 3, 20, EnumColor.GREEN); 
 				}
 			}
 			else 
 			{
 				//Double the damage done if it was a critical hit
 				if(isCrit && !isImmuneToCrits)
 				{
 					d *= 2;
 				}
 				//The player will be invincible for 750 ms after hit
 				invincibilityTicks = 15; 
 				//Determine the damage after armour, with a floor of 1 damage and then apply absorbs
 				double damageAfterArmor = MathHelper.floorOne(
 						(d * (1F - DEFENSE_REDUCTION_PERCENT * defense)) - (defense * DEFENSE_REDUCTION_FLAT)									
 						);
 				damageAfterArmor = dealDamageToAbsorbs(damageAfterArmor);
 				health -= damageAfterArmor; 
 				world.soundEngine.playSoundEffect(hitSound);
 				auraTracker.onDamageTaken(world, this);
 				//Show world text if applicable
 				if(showWorldText)
 				{
 					String message = (damageAfterArmor == 0) ? "Absorb" : ""+(int)damageAfterArmor;
					world.addTemporaryText(message, (int)x - 2, (int)y - 3, 20, (damageAfterArmor == 0) ? EnumColor.WHITE : EnumColor.CRITICAL); //add temperary text to be rendered, for the damage done
 				}
 			}	
 			
 			putInCombat();
 		}
 		
 		if(isDead()) //if the player has died
 		{
 			onDeath(world);
 			world.clearEntityList();
 		}		
 	}
 	
 	/**
 	 * Puts the player in combat.
 	 */
 	public void putInCombat()
 	{
 		isInCombat = true;
 		ticksOfHealthRegen = 0;
 	}
 	
 	/**
 	 * Heals the player, and displays green WorldText if applicable.
 	 * @param h the amount healed
 	 * @param rendersText whether or not to render WorldText on the screen
 	 */
 	public boolean healPlayer(World world, int h, boolean rendersText)
 	{
 		if(health < maxHealth)
 		{
 			health += h; 
 			if(rendersText)
 			{
 				world.addTemporaryText(""+(int)h, (int)x - 2, (int)y - 3, 20, EnumColor.GREEN); //add temperary text to be rendered, for the healing done
 			}
 			if(health > maxHealth) //if health exceeds the maximum, set it to the maximum
 			{
 				health = maxHealth;
 			}
 			auraTracker.onHeal(world, this);
 			return true;
 		}
 		return false;
 	}
 		
 	/**
 	 * Resets ticksSinceLastCast to 0, preventing mana regen for 6 seconds and restarting the regeneration cycle.
 	 */
 	public void resetCastTimer()
 	{
 		ticksSinceLastCast = 0;
 	}
 	
 	/**
 	 * Applies mana regen (currently a static amount)
 	 */
 	private void applyManaRegen()
 	{
 		if(ticksSinceLastCast > 120)
 		{
 			mana += ((ticksSinceLastCast - 120) / 1000 < 0.60f) ? ((ticksSinceLastCast - 120) / 1000) : 0.60f;
 		}
 		
 		mana += (maxMana * 0.0002f);
 		
 		if(mana > maxMana)
 		{
 			mana = maxMana;
 		}
 	}
 	
 	/**
 	 * Applies health regen that scales over time, until reaching the maximum of (11 HP/sec)
 	 */
 	private void applyHealthRegen(World world)
 	{
 		if(health < maxHealth)
 		{
 			auraTracker.onHeal(world, this);
 		}
 		if(!isInCombat)
 		{
 			double amountHealed = (ticksOfHealthRegen / 1800f < 0.55f) ? (ticksOfHealthRegen / 1800f) : 0.55f;
 			health += amountHealed;
 			ticksOfHealthRegen++;	
 		}
 	
 		health += (maxHealth * 0.0002f);
 		
 		if(health > maxHealth)
 		{
 			health = maxHealth;
 		}
 	}
 	
 	/**
 	 * Recalculates defense and set bonuses when armour changes
 	 */
 	public void onArmorChange()
 	{
 		armorChanged = true;
 	}
 	
 	/**
 	 * Overrides EntityLiving onDeath() to provide special things like hardcore (mode) and itemdrops
 	 */
 	public void onDeath(World world)
 	{	
 		auraTracker.onDeath(world, this);
 		
 		//If the player is still dead, then trigger the respawn code
 		if(health <= 0)
 		{
 			triggerDifficultyEffect(world);
 		}
 	}
 	
 	/**
 	 * Triggers an effect appropriate to the player difficulty.
 	 * @param world the world the player is in
 	 */
 	private void triggerDifficultyEffect(World world)
 	{
 		if(difficulty == EnumPlayerDifficulty.HARD)
 		{
 			dropAll(world);
 		}
 		else if(difficulty == EnumPlayerDifficulty.HARDCORE)
 		{
 			defeated = true;	
 			return;
 		}
 		health = maxHealth;
 		world.soundEngine.playSoundEffect(deathSound);
 		world.spawnPlayer(this);	
 	}
 	
 	/**
 	 * Drops all the player's inventory.
 	 * @param world the world the player is in
 	 */
 	private void dropAll(World world)
 	{
 		Random random = new Random();
 		for(int i = 0; i < inventory.getMainInventoryLength(); i++)
 		{
 			if(inventory.getMainInventoryStack(i) != null)
 			{
 				world.addItemStackToItemList((EntityItemStack)new EntityItemStack(x, 
 						y - 15, 
 						inventory.getMainInventoryStack(i)).setVelocity(new Vector2F((random.nextFloat() * 8) - 4, random.nextFloat() * 8)));
 				inventory.removeEntireStackFromInventory(world, this, i);
 			}
 		}
 		for(int i = 0; i < inventory.getArmorInventoryLength(); i++)
 		{
 			if(inventory.getArmorInventoryStack(i) != null)
 			{
 				world.addItemStackToItemList((EntityItemStack)new EntityItemStack(x, 
 						y - 15, 
 						inventory.getArmorInventoryStack(i)).setVelocity(new Vector2F((random.nextFloat() * 8) - 4, random.nextFloat() * 8)));
 				inventory.setArmorInventoryStack(this, null, inventory.getArmorInventoryStack(i), i);			
 			}
 		}
 		for(int i = 0; i < inventory.getQuiverLength(); i++)
 		{
 			if(inventory.getQuiverStack(i) != null)
 			{
 				world.addItemStackToItemList((EntityItemStack)new EntityItemStack(x, 
 						y - 15, 
 						inventory.getQuiverStack(i)).setVelocity(new Vector2F((random.nextFloat() * 8) - 4, random.nextFloat() * 8)));
 				inventory.setQuiverStack(null, i);			
 			}
 		}
 	}
 	
 	/**
 	 * Flag recipes for recalculation
 	 */
 	public void onInventoryChange()
 	{
 		inventoryChanged = true; 
 	}
 
 	/**
 	 * Recreates the allPossibleRecipes[] based on what the player is standing by.
 	 */
 	private void setAllRecipeArray()
 	{
 		//'int size' indicates how many recipes are able to be created, in total
 		//InventoryRecipes are always possible, add their total no matter what
 		int size = possibleRecipesByBlock.get(Block.none.getName()).length;
 		for(Block block : CraftingManager.getCraftingBlocks())
 		{
 			if(nearBlock.get(block.getName()))
 			{
 				size += possibleRecipesByBlock.get(block.getName()).length;
 			}
 		}
 		Recipe[] recipes = new Recipe[size]; //create a temperary Recipe[] to store stuff in.S
 		
 		int i = 0; //used for standard looping. declared here so its value can be known after the loop
 		int k = 0; //the index in recipes[] to begin saving recipes
 		
 		for(Block block : CraftingManager.getCraftingBlocks())
 		{
 			Recipe[] possibleRecipes = possibleRecipesByBlock.get(block.getName());
 			if(nearBlock.get(block.getName()))
 			{
 				for(i = 0; i < possibleRecipes.length; i++)
 				{
 					recipes[k + i] = possibleRecipes[i];
 				}
 				k += i;				
 			}
 		}
 		//Inventory Default recipes
 		Recipe[] possibleRecipes = possibleRecipesByBlock.get(Block.none.getName());
 		if(nearBlock.get(Block.none.getName()))
 		{
 			for(i = 0; i < possibleRecipes.length; i++)
 			{
 				recipes[k + i] = possibleRecipes[i];
 			}
 			k += i;				
 		}
 		
 		allPossibleRecipes = recipes; //set the possible recipes to the temperary Recipe[]
 		if(selectedRecipe >= allPossibleRecipes.length) //Fix the selectedRecipe Integer so that the crafting scroller doesnt go out of bounds
 		{
 			selectedRecipe = (allPossibleRecipes.length > 0) ? allPossibleRecipes.length - 1 : 0;
 		}
 	}
 	
 	/**
 	 * Serves for detecting what can be crafting, currently
 	 */
 	private void checkForNearbyBlocks(World world) 
 	{
 		Dictionary<String, Boolean> nearby = new Hashtable<String, Boolean>();
 		boolean recalculateRecipes = false;
 		final int detectionRadius = 2; //blocks away a player can detect a crafting_table/furnace/etc
 		
 		//Check if the blocks in CraftingManager.getCraftingBlocks() are nearby
 		for(Block block : CraftingManager.getCraftingBlocks())
 		{
 			nearby.put(block.getName(), 
 					   blockInBounds(world, -detectionRadius, detectionRadius, -detectionRadius, detectionRadius, block)); 
 			if(nearBlock.get(block.getName()) != nearby.get(block.getName())) //and they weren't just near one
 			{ 
 				recalculateRecipes = true; //recipes need recalculated
 			}
 		}
 		nearBlock.put(Block.none.getName(), true);
 			
 		//if the recipes need recalculated or the inventory has changed then recalculate recipes.
 		if(recalculateRecipes || inventoryChanged) 
 		{	
 			for(Block block : CraftingManager.getCraftingBlocks())
 			{
 				if(inventoryChanged || nearBlock.get(block.getName()) != nearby.get(block.getName()))
 				{
 					nearBlock.put(block.getName(), nearby.get(block.getName()));
 					updateRecipes(block);
 				}					
 			}
 		
 			updateRecipes(Block.none); //update inventory recipes
 			setAllRecipeArray(); //set the crafting recipe array to the new recipes
 			inventoryChanged = false;
 		}
 	}
 	
 	/**
 	 * Emergency or init function to brute force calculate all recipes
 	 */
 	public void updateAllPossibleRecipes()
 	{
 		updateAllRecipes();
 		setAllRecipeArray();
 	}
 	
 	/**
 	 * Updates the entire possibleRecipesByBlock to be accurate, after an inventoryChange generally
 	 */
 	private void updateAllRecipes()
 	{
 		for(Block block : CraftingManager.getCraftingBlocks())
 		{
 			possibleRecipesByBlock.put(block.getName(), craftingManager.getPossibleRecipesByBlock(inventory, block));		
 		}
 	}
 	
 	/**
 	 * Updates one crafting block in the possibleRecipesByBlock Dictionary
 	 * @param block the block in possibleRecipesByBlock to update
 	 */
 	private void updateRecipes(Block block)
 	{
 		possibleRecipesByBlock.put(block.getName(), craftingManager.getPossibleRecipesByBlock(inventory, block));		
 	}
 	
 	/**
 	 * Recalculates damage modifiers and health based on stats.
 	 */
 	private void recalculateStats()
 	{
 		rangeDamageModifier = 1.0f + 0.04f * dexterity;
 		meleeDamageModifier = 1.0f + 0.04f * strength;
 		magicDamageModifier = 1.0f + 0.04f * intellect;
 		maxHealth = baseMaxHealth + (stamina * HEALTH_FROM_STAMINA);
 		maxMana = baseMaxMana + (intellect * MANA_FROM_INTELLECT);		
 	}
 	
 	/**
 	 * Applies defense, stats, and SetBonuses from a single piece of armour that is being equipped. These 
 	 * should be constant for a given piece of armour to allow for easy removing.
 	 * @param armor the piece of armour being equipped
 	 */
 	public void applySingleArmorItem(ItemArmor armor, ItemStack stack, int index)
 	{
 		SetBonus[] bonuses = armor.getBonuses();		
 		for(SetBonus bonus : bonuses)
 		{
 			bonus.apply(this);
 		}		
 		Vector<Aura> auras = new Vector<Aura>();
 		for(GemSocket socket : stack.getGemSockets())
 		{
 			if(socket.getGem() != null)
 			{
 				for(SetBonus bonus : socket.getGem().getBonuses())
 				{
 					if(bonus != null)
 					{
 						bonus.apply(this);
 					}
 				}
 				for(Aura aura : socket.getGem().getAuras())
 				{
 					if(aura != null)
 					{
 						auras.add(aura);
 					}
 				}
 			}
 		}
 		for(SetBonus bonus : stack.getBonuses())
 		{
 			bonus.apply(this);
 		}
 		
 		for(Aura aura : armor.getAuras())
 		{
 			auras.add(aura);
 		}		
 		for(Aura aura : stack.getAuras())
 		{
 			auras.add(aura);
 		}
 		
 		auraTracker.setAurasByPiece(new AuraContainer(auras), index);			
 		
 		defense += armor.getDefense();
 		dexterity += armor.getDexterity();
 		intellect += armor.getIntellect();
 		strength += armor.getStrength();
 		stamina += armor.getStamina();
 		recalculateStats();
 	}
 	
 	/**
 	 * Removes defense, stats, and SetBonuses for a single piece of armour that is now being removed.
 	 * These should be the same as when it was equipped due to constant values in armour that do not
 	 * change.
 	 * @param armor the piece of armour being removed
 	 */
 	public void removeSingleArmorItem(ItemArmor armor, ItemStack stack, int index)
 	{
 		SetBonus[] bonuses = armor.getBonuses();		
 		for(SetBonus bonus : bonuses)
 		{
 			bonus.remove(this);
 		}		
 		for(GemSocket socket : stack.getGemSockets())
 		{
 			if(socket.getGem() != null)
 			{
 				for(SetBonus bonus : socket.getGem().getBonuses())
 				{
 					if(bonus != null)
 					{
 						bonus.remove(this);
 					}
 				}
 			}
 		}
 		for(SetBonus bonus : stack.getBonuses())
 		{
 			bonus.remove(this);
 		}
 		
 		auraTracker.setAurasByPiece(null, index);
 		
 		defense -= armor.getDefense();
 		dexterity -= armor.getDexterity();
 		intellect -= armor.getIntellect();
 		strength -= armor.getStrength();
 		stamina -= armor.getStamina();
 		recalculateStats();
 	}
 	
 	/**
 	 * Updates set bonus data. Unused bonuses are removed and new ones are applied. The new bonuses are 
 	 * stored in the player's instance to be removed later.
 	 */
 	private void refreshSetBonuses()
 	{
 		if(currentBonuses != null)
 		{
 			currentBonuses.removeAll(this);
 			currentBonuses = null;
 		}
 		currentBonuses = SetBonusFactory.getSetBonuses(inventory);
 		currentBonuses.applyAll(this);
 	}
 
 	/**
 	 * Function called when the player is mining a block
 	 * @param mx x position in worldmap array, of the block being mined
 	 * @param my y position in the worldmap array, of the block being mined
 	 * @param item the tool mining the block
 	 */
 	public void breakBlock (World world, int mx, int my, Item item)
 	{
 		if (world.getBlock(mx, my).getID() != Block.air.getID()){
 			if (((item instanceof ItemToolPickaxe && world.getBlock(mx, my).getBlockType() == 1) 
 			  || (item instanceof ItemToolAxe && world.getBlock(mx, my).getBlockType() == 2) 
 			  || (item instanceof ItemToolHammer && world.getBlock(mx, my).getBlockType() == 3) 
 		      || world.getBlock(mx, my).getBlockType() == 0) && Mouse.isButtonDown(0))
 			{ //If the left-mouse button is pressed && they have the correct tool to mine
 				EnumToolMaterial material;
 				if (item instanceof ItemTool)
 				{
 					material = item.getToolMaterial();
 				}
 				else
 				{
 					material = EnumToolMaterial.FIST;
 				}
 				double distance = MathHelper.distanceBetweenTwoPoints((MathHelper.getCorrectMouseXPosition() + Render.getCameraX()), (MathHelper.getCorrectMouseYPosition() + Render.getCameraY()), (this.x + ((isFacingRight) ? 9 : 3)), (this.y + 9));
 					      
 				if(distance <= material.getDistance()){
 					if (material.getToolTier() >= world.getBlock(mx, my).getBlockTier()) //If the block is within range
 						{ 	
 						if(ticksreq == 0 || world.getBlock(mx, my) != world.getBlock(sx, sy)) //the mouse has moved or they arent mining anything
 						{				
 							isMining = false;
 							sx = mx; //save the co-ords
 							sy = my;
 							ticksreq = (int) (world.getBlock(mx, my).getBlockHardness() / material.getStrength()) + 1; //Determine how long to break
 							
 						}	
 						else if(ticksreq == 1 && Mouse.isButtonDown(0) && world.getBlock(mx,  my).getIsMineable()) //If the block should break
 						{
 							isMining = false;
 							if(world.getBlock(mx, my) == Block.cactus) 
 							{
 								world.breakCactus(this, mx, my);
 							}
 							else if(world.getBlock(mx, my) == Block.tree)
 							{
 								world.breakTree(this, mx, my);					
 							}
 							world.breakBlock(this, mx, my);
 							
 							//Overwrite snow/flowers/etc...
 							if (world.getBlock(mx, my-1).getIsOveridable() == true && world.getBlock(mx, my-1) != Block.air)
 							{
 								world.breakBlock(this, mx, my-1);
 							}
 						}		
 						else if (Mouse.isButtonDown(0)) //mining is in progress, decrease remaining time.
 						{
 							isMining = true;
 							ticksreq--;			
 						}	
 					}
 				}
 			}
 		}
 	}	
 	
 	/**
 	 * Increases the maximum health of the player
 	 * @param increase the amount to increase maxHealth
 	 * @return success of the operation
 	 */
 	public boolean boostMaxHealth(int increase) 
 	{
 		if(baseMaxHealth + increase <= MAXIMUM_BASE_HEALTH)//if the player can have that much of a max health increase
 		{
 			maxHealth += increase; //increase max health
 			baseMaxHealth += increase;
 			return true;
 		}
 		return false;
 	}
 	
 	/**
 	 * Increases the maximum mana of the player
 	 * @param increase the amount to increase maxMana
 	 * @return success of the operation
 	 */
 	public boolean boostMaxMana(int increase) 
 	{
 		if(baseMaxMana + increase <= MAXIMUM_BASE_MANA) //if the player can have that much of a max mana increase
 		{
 			baseMaxMana += increase;
 			maxMana += increase; //increase max mana
 			return true;
 		}
 		return false;
 	}
 	
 	/**
 	 * Gets the player's difficulty setting. This field is final, and unchanging
 	 * @return the player's difficulty setting
 	 */
 	public final EnumPlayerDifficulty getDifficulty()
 	{
 		return difficulty;
 	}
 	
 	/**
 	 * Gets whether or not the player is currently mining a block
 	 * @return whether or not the player is currently mining
 	 */
 	public boolean getIsMining(){
 		return isMining;
 	}
 	
 	/**
 	 * Gets the maximum distance the player can place a block, measured in ortho units. this field is final
 	 * @return MAX_BLOCK_PLACE_DISTANCE field
 	 */
 	public final int getMaximumBlockPlaceDistance()
 	{
 		return MAX_BLOCK_PLACE_DISTANCE;
 	}
 	
 	/**
 	 * Sets the position of the viewed chest (from 'world map'), and changes isViewingChest to true.
 	 * @param x the x position of the chest, in the 'world map'
 	 * @param y the y position of the chest, in the 'world map'
 	 */
 	public void setViewedChest(int x, int y)
 	{
 		isViewingChest = true;
 		viewedChestX = x;
 		viewedChestY = y;
 	}
 	
 	/**
 	 * Clears the currently viewed chest position to 0,0, and sets isViewingChest to false
 	 */
 	public void clearViewedChest()
 	{
 		isViewingChest = false;
 		viewedChestX = -1;
 		viewedChestY = -1;
 	}
 	
 	/**
 	 * Resets all variables relating to mining, to a default value so the player can mine again
 	 */
 	public void resetMiningVariables()
 	{
 		ticksreq = 0;
 		sy = 0;
 		sx = 0;
 	}
 	
 	/**
 	 * Launch a player spell projectile
 	 * @param world = current world
 	 * @param mouseX = x position to create the projectile at
 	 * @param mouseY = y position to create the projectile at
 	 * @param item = Item to be used to launch projectile (what projectile is needed)
 	 */
 	public void launchProjectileMagic(World world, double mouseX, double mouseY, ItemMagic item){		
 		if (mana >= item.getManaReq()){
 			int angle = MathHelper.angleMousePlayer(mouseX, mouseY, x, y);
 			if (angle < 0){
 				angle += 360;
 			}
 			world.addEntityToProjectileList(new EntityProjectile(item.getProjectile()).setXLocAndYLoc(x, y)
 					.setDirection(angle).setDamage(item.getProjectile().getDamage()));
 			mana -= item.getManaReq();			
 		}
 		
 	}
 
 	/**
 	 * Launch a player weapon projectile
 	 * @param world = current world
 	 * @param mouseX = x position to create the projectile at
 	 * @param mouseY = y position to create the projectile at
 	 * @param item = Item to be used to launch projectile (to determine what projectile is needed)
 	 */
 	public void launchProjectileWeapon(World world, double mouseX, double mouseY, ItemRanged item){
 		if (ticksSinceLastCast > item.getCooldownTicks())
 		{
 			for (int i = 0; i < inventory.getQuiverLength(); i++)
 			{			
 				if(inventory.getQuiverStack(i) != null)
 				{	
 					ItemAmmo ammunition = (ItemAmmo) Item.itemsList[inventory.getQuiverStack(i).getItemID()];
 					
 					int angle = MathHelper.angleMousePlayer(mouseX, mouseY, x, y) - 90;
 					if (angle < 0)
 					{
 						angle += 360;
 					}
 					if (isFacingRight)
 					{
 						world.addEntityToProjectileList(new EntityProjectile(ammunition.getProjectile()).setDrop(new ItemStack(ammunition)).setXLocAndYLoc(x, y)
 								.setDirection(angle).setDamage((ammunition.getProjectile().getDamage() + item.getDamage())));
 					}
 					else{
 						world.addEntityToProjectileList(new EntityProjectile(ammunition.getProjectile()).setDrop(new ItemStack(ammunition)).setXLocAndYLoc(x, y)
 								.setDirection(angle).setDamage((ammunition.getProjectile().getDamage() + item.getDamage())));
 					}
 					inventory.removeItemsFromQuiverStack(1, i);
 					ticksSinceLastCast = 0;
 					break;
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Gets the Block the player is holding, if it's a Block and it's an instanceof BlockLight. Used for handheld lighting.
 	 * @return an instanceof BlockLight if the player is holding one, otherwise null
 	 */
 	public Block getHandheldLight()
 	{
 		ItemStack stack = inventory.getMainInventoryStack(selectedSlot);
 		if(stack != null)
 		{
 			if(stack.getItemID() < Item.itemIndex && (Block.blocksList[stack.getItemID()]) instanceof BlockLight)
 			{
 				return Block.blocksList[stack.getItemID()];
 			}
 		}
 		return null;
 	}
 	
 	/**
 	 * Decreases the ticksLeft on each cooldown by 1, and removes that cooldown if it's expired.
 	 */
 	private void updateCooldowns()
 	{
 		Enumeration<String> keys = cooldowns.keys();
 		while (keys.hasMoreElements()) 
 		{
 			String str = (String) keys.nextElement();
 			Cooldown cooldown = cooldowns.get(str);
 			cooldown.ticksLeft--;
 			if(cooldown.ticksLeft <= 0)
 			{
 				cooldowns.remove(str);
 			}
 		}
 	}
 	
 	/**
 	 * Puts an ID on cooldown, which generally relates to an item or something else. If that ID is already on cooldown and
 	 * with a longer time, this will fail and the current cooldown will continue, otherwise the new cooldown will be implemented.
 	 * @param id the id to put on cooldown
 	 * @param duration the duration of the cooldown in game ticks
 	 */
 	public void putOnCooldown(int id, int duration)
 	{
 		if(cooldowns.get("" + id) != null)
 		{
 			Cooldown current = cooldowns.get("" + id);
 			if(current.ticksLeft < duration)
 			{
 				cooldowns.put("" + id, new Cooldown(id, duration));
 			}		
 			return;	
 		}
 		cooldowns.put("" + id, new Cooldown(id, duration));
 	}
 	
 	/**
 	 * Gets whether or not an ID is on cooldown. This does not verify that ID actually exists.
 	 * @param id the id that should be checked for a cooldown
 	 * @return if the ID is on cooldown (true if it is, false otherwise)
 	 */
 	public boolean isOnCooldown(int id)
 	{
 		return cooldowns.get("" + id) != null;
 	}
 	
 	/**
 	 * Gets the time remaining on an ID's cooldown. This does not verify that ID actually exists.
 	 * @param id the id that should be queried for a cooldown remaining
 	 * @return the time left on the specified ID's cooldown, in ticks
 	 */
 	public int getTicksLeftOnCooldown(int id)
 	{
 		return cooldowns.get("" + id).ticksLeft;
 	}
 
 	/**
 	 * Regens a little bit of the special energy bar
 	 */
 	private void applySpecialRegen()
 	{
 		//25% per minute.
 		specialEnergy += 0.020833333F;
 		
 		if(specialEnergy > MAX_SPECIAL_ENERGY)
 		{
 			specialEnergy = MAX_SPECIAL_ENERGY;
 		}
 	}
 	
 	/**
 	 * Restores special energy, upto the cap
 	 * @param restore the amount restored
 	 * @return whether or not any special energy was restored
 	 */
 	public boolean restoreSpecialEnergy(int restore)
 	{
 		if(specialEnergy == MAX_SPECIAL_ENERGY)
 		{
 			return false;
 		}
 		
 		specialEnergy += restore;
 		if(specialEnergy > MAX_SPECIAL_ENERGY)
 		{
 			specialEnergy = MAX_SPECIAL_ENERGY;
 		}
 		return true;
 	}
 	
 	/**
 	 * Spends the given amount of special energy - even if the player doesnt have it.
 	 * @param amount the amount of energy to spend
 	 */
 	public void spendSpecialEnergy(int amount)
 	{
 		specialEnergy -= amount;
 	}
 	
 	public double getToolRotationAngle()
 	{
 		return rotateAngle;
 	}
 	
 	public boolean isSwingingTool()
 	{
 		return hasSwungTool;
 	}
 	
 	public boolean getIsSwingingRight()
 	{
 		return isSwingingRight;
 	}
 	
 	/**
 	 * Adds the given radian value to the tool rotation angle. If this is negative, it will
 	 * be subtracted.
 	 * @param addition the amount to add to the tool rotation angle, in radians
 	 */
 	public void updateRotationAngle(double addition)
 	{
 		rotateAngle += addition;
 	}
 	
 	/**
 	 * Stops the swinging of the player weapon
 	 */
 	public void clearSwing()
 	{
 		hasSwungTool = false;
 	}
 	
 	/**
 	 * Updates the tool swing. This is based on a weapon's swing speed - a faster speed swings faster.
 	 */
 	public void updateSwing()
 	{
 		double swingSpeed = (double)((ItemTool)(Item.itemsList[inventory.getMainInventoryStack(selectedSlot).getItemID()])).swingSpeed;
 		if(isSwingingRight)
 		{
             rotateAngle += MathHelper.degreeToRadian(10.0F * swingSpeed);	        
             if(rotateAngle <= leftSwingBound || rotateAngle >= rightSwingBound )
 	        {
 	        	clearSwing();
 	        }
 		}
 		else
 		{
 			rotateAngle -= MathHelper.degreeToRadian(10F * swingSpeed);
 		    if(rotateAngle <= leftSwingBound || rotateAngle >= rightSwingBound)
 	        {
 	        	clearSwing();
 	        }
 		}
 	}
 	
 	/**
 	 * Starts swinging the tool based on direction, resetting the angle to an appropriate starting location. 
 	 * @param swingRight whether the swing is right, or not (IE left)
 	 */
 	public void startSwingingTool(boolean swingRight)
 	{
 		isSwingingRight = swingRight;
 		hasSwungTool = true;
 		if(swingRight)
 		{
 			rotateAngle = leftSwingBound;			
 		}
 		else
 		{
 			rotateAngle = rightSwingBound;
 		}
 	}
 	
 	/**
 	 * If the player deals damage to a monster or something else this should be called. This will cause
 	 * auras to fire their onDamageDone() events.
 	 */
 	public void inflictedDamageToMonster(World world) 
 	{
 		auraTracker.onDamageDone(world, this);
 	}
 
 	/**
 	 * Gets the player's health percentage, from 0.0-1.0, where 0.0 indicates 0% and 1.0 indicates 100%
 	 * @return the player's health percent as a double between 0.0 and 1.0
 	 */
 	public double getHealthPercent() 
 	{
 		return health / maxHealth;
 	}
 }
