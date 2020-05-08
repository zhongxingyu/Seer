 package com.lenis0012.bukkit.btm.api;
 
 import java.util.List;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 
 import com.lenis0012.bukkit.btm.nms.PacketGenerator;
 import com.lenis0012.bukkit.btm.nms.wrappers.DataWatcher;
 import com.lenis0012.bukkit.btm.util.MetaDataUtil;
 import com.lenis0012.bukkit.btm.util.NetworkUtil;
 
 /**
  * Data wrapper of a player disguise
  * 
  * @author lenis0012
  */
 public class Disguise {
 	private boolean isPlayer = false;
 	private Location loc;
 	private String name;
 	private int itemInHand;
 	private EntityType type;
 	private int EntityID;
 	private boolean spawned = false;
 	private Player player;
 	private List<String> extras;
 	private Movement movement;
 	private DataWatcher dw;
 	private IPacketGenerator gen;
 	
 	public Disguise(Player player, int EntityID, Location loc, String name, int itemInHand) {
 		this.isPlayer = true;
 		this.loc = loc;
 		this.type = EntityType.PLAYER;
 		this.EntityID = EntityID;
 		this.name = name;
 		this.itemInHand = itemInHand;
 		this.player = player;
 		this.movement = new Movement(loc);
 		this.gen = new PacketGenerator(this);
 	}
 	
 	public Disguise(Player player, int EntityID, Location loc, EntityType type, List<String> extras) {
 		this.EntityID = EntityID;
 		this.loc = loc;
 		this.type = type;
 		this.player = player;
 		this.extras = extras;
 		this.movement = new Movement(loc);
 		this.gen = new PacketGenerator(this);
 	}
 	
 	/**
 	 * Check if the type is a player
 	 * 
 	 * @return Tupe is a player
 	 */
 	public boolean isPlayer() {
 		return this.isPlayer;
 	}
 	
 	/**
 	 * Get the location of the disguise
 	 * 
 	 * @return Location
 	 */
 	public Location getLocation() {
 		return this.loc;
 	}
 	
 	/**
 	 * Get the disguised player
 	 * 
 	 * @return Player
 	 */
 	public Player getPlayer() {
 		return this.player;
 	}
 	
 	/**
 	 * @return The disguises custom name
 	 */
 	public String getCustomName(){
 		return name;
 	}
 	
 	/**
 	 * Get the custom entity id of the disguise
 	 * 
 	 * @return EntityId
 	 */
 	public int getEntityId() {
 		return this.EntityID;
 	}
 	
 	public DataWatcher getDataWatcher() {
 		return this.dw;
 	}
 	
 	/**
 	 * Set the location of the disguise
 	 * 
 	 * @param loc Location
 	 */
 	public void setLocation(Location loc) {
 		this.loc = loc;
 	}
 	
 	/**
 	 * Spawn the disguise for a player
 	 * 
 	 * @param player Player to recive spawn packet
 	 */
 	public void spawn(Player player) {
 		if(this.spawned) {
 			if(isPlayer) {
 				NetworkUtil.sendPacket(gen.getNamedEntitySpawnPacket(), player);
 			} else {
 				NetworkUtil.sendPacket(gen.getNamedEntitySpawnPacket(), player);
 			}
 		}
 	}
 	
 	/**
 	 * Spawn the disguise for a world
 	 * 
 	 * @param world World to recive packet
 	 */
 	public void spawn(World world) {
 		if(isPlayer) {
 			dw = new DataWatcher();
 			dw.set(0, Byte.valueOf((byte) 0));
 			dw.set(12, Integer.valueOf((int) 0));
 			NetworkUtil.sendGlobalPacket(gen.getNamedEntitySpawnPacket(), world, this.getPlayer());
 		} else {
 			dw = MetaDataUtil.getDataWatcher(type, extras);
 			NetworkUtil.sendGlobalPacket(gen.getMobSpawnPacket(), world, this.getPlayer());
 		}
 		
 		this.spawned = true;
 	}
 	
 	/**
 	 * Despawn the disguise
 	 */
 	public void despawn() {
 		World world = loc.getWorld();
 		NetworkUtil.sendGlobalPacket(gen.getDestroyEntityPacket(), world, this.player);
 		this.spawned = false;
 	}
 	
 	/**
 	 * Kill the entity with the normal fall over animation
 	 * (Warning kills and despawns disguise)
 	 */
 	public void kill() {
 		NetworkUtil.sendGlobalPacket(gen.getEntityStatusPacket((byte) 3), loc.getWorld(), this.getPlayer());
 	}
 	
 	/**
 	 * 'Ignites' the disguise
 	 */
 	public void ignite(){
 		dw.set(0, Byte.valueOf((byte) 1));
 		updateMetaData();
 	}
 	
 	/**
 	 * Stops fire on the entity
 	 */
 	public void extinguish(){
 		dw.set(0, Byte.valueOf((byte) 0));
 		updateMetaData();
 	}
 	
 	
 	/**
 	 * Despawn the disguise in a custom world
 	 * 
 	 * @param world	World to despawn disguise
 	 */
 	public void despawn(World world) {
 		NetworkUtil.sendGlobalPacket(gen.getDestroyEntityPacket(), world, this.getPlayer());
 		this.spawned = false;
 	}
 	
 	/**
 	 * Unload the disguise for a player
 	 * 
 	 * @param player Player to unload disguise for
 	 */
 	public void unLoadFor(Player player) {
 		NetworkUtil.sendPacket(gen.getDestroyEntityPacket(), player);
 	}
 	
 	/**
 	 * @param player Player to change disguises item in hand for
 	 * slot 0 is the item in hand
 	 */
 	public void changeItem(int new_slot) {
 		org.bukkit.inventory.ItemStack bukkitstack = null;
 		if(player.getInventory().getItem(new_slot) == null){
 			bukkitstack = new org.bukkit.inventory.ItemStack(Material.AIR, 1);
 		}else{
 			bukkitstack = player.getInventory().getItem(new_slot);
 		}
 		this.itemInHand = bukkitstack.getTypeId();
 		NetworkUtil.sendGlobalPacket(gen.getEntityEquipmentPacket(0, bukkitstack), loc.getWorld(), this.getPlayer());
 	}
 	
 	/**
 	 * @param player Player to set disguises armor
 	 * @param slot Which armour slot
 	 * 1 boots
 	 * 2 leggings
 	 * 3 chestplate
 	 * 4 helmet
 	 */
 	public void changeArmor(int slot) {
 		org.bukkit.inventory.ItemStack bukkitstack = null;
 		switch(slot){
 		case 1:
 			bukkitstack = player.getInventory().getBoots();
 			break;
 		case 2:
 			bukkitstack = player.getInventory().getLeggings();
 			break;
 		case 3:
 			bukkitstack = player.getInventory().getChestplate();
 			break;
 		case 4:
 			bukkitstack = player.getInventory().getHelmet();
 			break;
 		default:
 			return;
 		}
 		if(bukkitstack == null){
 			bukkitstack = new org.bukkit.inventory.ItemStack(Material.AIR);
 		}
 		NetworkUtil.sendGlobalPacket(gen.getEntityEquipmentPacket(slot, bukkitstack), loc.getWorld(), this.getPlayer());
 	}
 	
 	/**
 	 * Move the disguise to a new location
 	 * 
 	 * @param event Event to get moving from
 	 */
 	public void move(Location from, Location to, boolean moved) {
 		if(!spawned)
 			return;
 		
 		World world = to.getWorld();
 		this.loc = to;
 		
 		if(moved) {
 			movement.update(to);
 			NetworkUtil.sendGlobalPacket(gen.getEntityMoveLookPacket(movement), world, this.getPlayer());
 		} else
 			NetworkUtil.sendGlobalPacket(gen.getEntityLookPacket(), world, this.getPlayer());
 		NetworkUtil.sendGlobalPacket(gen.getEntityHeadRotatePacket(), world, this.getPlayer());
 	}
 	
 	/**
 	 * Teleport the disguise
 	 * 
 	 * @param loc Location to teleport to
 	 */
 	public void teleport(Location loc) {
 		this.loc = loc;
 		NetworkUtil.sendGlobalPacket(gen.getEntityTeleportPacket(), loc.getWorld(), this.getPlayer());
 		this.movement = new Movement(loc);
 	}
 	
 	/**
 	 * Swing the arm of the disguise
 	 */
 	public void swingArm() {
 		NetworkUtil.sendGlobalPacket(gen.getArmAnimationPacket(1), loc.getWorld(), this.getPlayer());
 	}
 	
 	/**
 	 * Let the disguised player damage a block
 	 * 
 	 * @param block	Block to damage
 	 */
 	public void damageBlock(Block block) {
 		NetworkUtil.sendGlobalPacket(gen.getBlockBreakAnimationPacket(block), loc.getWorld(), this.getPlayer());
 	}
 	
 	/**
 	 * Notify the disguised player got damaged
 	 */
 	public void damage() {
 		NetworkUtil.sendGlobalPacket(gen.getArmAnimationPacket(2), loc.getWorld(), this.getPlayer());
 	}
 	
 	/**
 	 * Make the entity crouch
 	 */
 	public void crouch() {
 		if(this.isPlayer) {
 			byte b = dw.getByte(0);
 			dw.watch(0, Byte.valueOf((byte)(b | 1 << 1)));
 			this.updateMetaData();
 		}
 	}
 	
 	/**
 	 * Make the entity uncrouch
 	 */
 	public void uncrouch() {
 		if(this.isPlayer) {
 			byte b = dw.getByte(0);
 			dw.watch(0, Byte.valueOf((byte)(b & (1 << 1 ^ 0xFFFFFFFF))));
 			this.updateMetaData();
 		}
 	}
 	
 	public void updateMetaData() {
 		NetworkUtil.sendGlobalPacket(gen.getEntityMetadataPacket(), loc.getWorld(), this.getPlayer());
 	}
 	
 	/**
 	 * Refresh the movement of the disguised player
 	 */
 	public void refreshMovement() {
 		this.movement = new Movement(loc);
 	}
 	
 	/**
 	 * Get the type of the disguise
 	 */
     public EntityType getDisguiseType() {
     	return type;
     }
     
     /**
      * Get the packet generator
      * 
      * @return Packet geneator
      */
     public IPacketGenerator getPacketGenerator() {
     	return this.gen;
     }
 
     /**
      * Get the item in the players hand
      * 
      * @return Item in hand
      */
 	public int getItemInHand() {
 		return itemInHand;
 	}
     
     public void playHurtSound() {
         if(BeTheMob.instance.getConfig().getBoolean("play_sound_on_hurt")) {
 			if(BeTheMob.instance.getConfig().getBoolean("can_damaged_hear_sound")) {
 				player.getWorld().playSound(getLocation(), getHurtSound(), 1, 1);
 			}else{
 				for(Entity e : player.getNearbyEntities(7, 7, 7)){
 					if(e.getType() == EntityType.PLAYER){
 						((Player)e).playSound(getLocation(), getHurtSound(), 1, 1);
 					}
 				}
 			}
         }
        				if(BeTheMob.instance.getConfig().getBoolean("play_sound_on_hurt")){
					if(BeTheMob.instance.getConfig().getBoolean("can_damaged_hear_sound")){
						player.getWorld().playSound(player.getLocation(), dis.getHurtSound(), 1, 1);
					}else{
						for(Entity e : player.getNearbyEntities(7, 7, 7)){
							if(e.getType() == EntityType.PLAYER){
								((Player)e).playSound(player.getLocation(), dis.getHurtSound(), 1, 1);
							}
						}
 					}
 				}
     
     }
     
     /**
      * Gets the correct damaged sound
      * If the disguise is a zombie it will be a zombie one
      * Etc.
      */
     public Sound getHurtSound() {
     	Random random = new Random();
     	if(type == EntityType.BAT){
     		return Sound.BAT_HURT;
     	} else if(type == EntityType.BLAZE){
     		return Sound.BLAZE_HIT;
     	} else if(type == EntityType.CAVE_SPIDER || type == EntityType.SPIDER){
     		return Sound.SPIDER_DEATH;// Spider death is hurt
     	}else if(type == EntityType.CHICKEN){
     		return Sound.CHICKEN_HURT;
     	}else if(type == EntityType.COW || type == EntityType.MUSHROOM_COW){
     		return Sound.COW_HURT;
     	}else if(type == EntityType.CREEPER){
     		return Sound.CREEPER_HISS;
     	}else if(type == EntityType.ENDER_DRAGON){
     		return Sound.ENDERDRAGON_HIT;
     	}else if(type == EntityType.ENDERMAN){
     		return Sound.ENDERMAN_HIT;
     	}else if(type == EntityType.GHAST){
     		return random.nextBoolean() ? Sound.GHAST_SCREAM : Sound.GHAST_SCREAM2;
     	}else if(type == EntityType.IRON_GOLEM){
     		return Sound.IRONGOLEM_HIT;
     	}else if(type == EntityType.MAGMA_CUBE || type == EntityType.SLIME){
     		return random.nextBoolean() ? Sound.SLIME_WALK : Sound.SLIME_WALK2;
     	}else if(type == EntityType.OCELOT){
     		return random.nextBoolean() ? Sound.CAT_HIT : Sound.CAT_HISS;
     	}else if(type == EntityType.PIG){
     		return Sound.PIG_DEATH;
     	} else if(type == EntityType.PIG_ZOMBIE){
     		return Sound.ZOMBIE_PIG_HURT;
     	}else if(isPlayer){
     		return Sound.HURT_FLESH;
     	}else if(type == EntityType.SHEEP){
     		return Sound.SHEEP_IDLE;
     	}else if(type == EntityType.SILVERFISH){
     		return Sound.SILVERFISH_HIT;
     	}else if(type == EntityType.SKELETON){
     		return Sound.SKELETON_HURT;
     	}else if(type == EntityType.WITHER){
     		return Sound.WITHER_HURT;
     	}else if(type == EntityType.ZOMBIE){
     		return Sound.ZOMBIE_HURT;
     	}
     	
     	return null;
     }
 
     
 }
