 package uk.co.jacekk.bukkit.simpleirc;
 
 import java.net.InetSocketAddress;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.UUID;
 
 import org.bukkit.Achievement;
 import org.bukkit.Bukkit;
 import org.bukkit.Effect;
 import org.bukkit.EntityEffect;
 import org.bukkit.GameMode;
 import org.bukkit.Instrument;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.Note;
 import org.bukkit.Server;
 import org.bukkit.Sound;
 import org.bukkit.Statistic;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.conversations.Conversation;
 import org.bukkit.conversations.ConversationAbandonedEvent;
 import org.bukkit.entity.Arrow;
 import org.bukkit.entity.Egg;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Projectile;
 import org.bukkit.entity.Snowball;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.InventoryView;
 import org.bukkit.inventory.InventoryView.Property;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.PlayerInventory;
 import org.bukkit.map.MapView;
 import org.bukkit.metadata.MetadataValue;
 import org.bukkit.permissions.Permission;
 import org.bukkit.permissions.PermissionAttachment;
 import org.bukkit.permissions.PermissionAttachmentInfo;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.potion.PotionEffect;
 import org.bukkit.potion.PotionEffectType;
 import org.bukkit.util.Vector;
 
 public class SimpleIRCPlayer implements Player {
 	
 	private String name;
 	private Player player;
 	
 	private ArrayList<String> messages;
 	
 	public SimpleIRCPlayer(String name, Player player){
 		this.name = name;
 		this.player = player;
 	}
 	
 	@Override
 	public String getName(){
 		return this.name;
 	}
 	
 	@Override
 	public World getWorld(){
 		if (this.player != null){
 			return this.player.getWorld();
 		}
 		
 		return Bukkit.getWorlds().get(0);
 	}
 	
 	@Override
 	public void sendMessage(String message){
 		if (this.player != null){
 			this.player.sendMessage(message);
 		}
 		
 		this.messages.add(message);
 	}
 	
 	@Override
 	public void sendMessage(String[] messages){
 		for (String message : messages){
 			this.sendMessage(message);
 		}
 	}
 	
 	public ArrayList<String> getReceivedMessages(){
 		return this.messages;
 	}
 	
 	// Begin wrapper methods
 	
 	@Override
 	public void closeInventory(){
 		if (this.player != null){
 			this.player.closeInventory();
 		}
 	}
 	
 	@Override
 	public Inventory getEnderChest(){
 		if (this.player != null){
 			return this.player.getEnderChest();
 		}
 
 		return null;
 	}
 	
 	@Override
 	public int getExpToLevel(){
 		if (this.player != null){
 			return this.player.getExpToLevel();
 		}
 		
 		return 0;
 	}
 	
 	@Override
 	public GameMode getGameMode(){
 		if (this.player != null){
 			return this.player.getGameMode();
 		}
 		
 		return GameMode.SURVIVAL;
 	}
 	
 	@Override
 	public PlayerInventory getInventory(){
 		if (this.player != null){
 			return this.player.getInventory();
 		}
 		
 		return null;
 	}
 	
 	@Override
 	public ItemStack getItemInHand(){
 		if (this.player != null){
 			return this.player.getItemInHand();
 		}
 		
 		return new ItemStack(Material.AIR, 0);
 	}
 	
 	@Override
 	public ItemStack getItemOnCursor(){
 		if (this.player != null){
 			return this.player.getItemOnCursor();
 		}
 		
 		return new ItemStack(Material.AIR, 0);
 	}
 	
 	@Override
 	public InventoryView getOpenInventory(){
 		if (this.player != null){
 			return this.player.getOpenInventory();
 		}
 		
 		return null;
 	}
 	
 	@Override
 	public int getSleepTicks(){
 		if (this.player != null){
 			return this.player.getSleepTicks();
 		}
 		
 		return 0;
 	}
 	
 	@Override
 	public boolean isBlocking(){
 		if (this.player != null){
 			return this.player.isBlocking();
 		}
 		
 		return false;
 	}
 	
 	@Override
 	public boolean isSleeping(){
 		if (this.player != null){
 			return this.player.isSleeping();
 		}
 		
 		return false;
 	}
 	
 	@Override
 	public InventoryView openEnchanting(Location arg0, boolean arg1){
 		if (this.player != null){
 			return this.openEnchanting(arg0, arg1);
 		}
 		
 		return null;
 	}
 	
 	@Override
 	public InventoryView openInventory(Inventory arg0){
 		if (this.player != null){
 			return this.openInventory(arg0);
 		}
 		
 		return null;
 	}
 	
 	@Override
 	public void openInventory(InventoryView arg0){
 		if (this.player != null){
 			this.player.openInventory(arg0);
 		}
 	}
 	
 	@Override
 	public InventoryView openWorkbench(Location arg0, boolean arg1){
 		if (this.player != null){
 			this.player.openWorkbench(arg0, arg1);
 		}
 		
 		return null;
 	}
 	
 	@Override
 	public void setGameMode(GameMode arg0){
 		if (this.player != null){
 			this.player.setGameMode(arg0);
 		}
 	}
 	
 	@Override
 	public void setItemInHand(ItemStack arg0){
 		if (this.player != null){
 			this.player.setItemInHand(arg0);
 		}
 	}
 	
 	@Override
 	public void setItemOnCursor(ItemStack arg0){
 		if (this.player != null){
 			this.player.setItemOnCursor(arg0);
 		}
 	}
 	
 	@Override
 	public boolean setWindowProperty(Property arg0, int arg1){
 		if (this.player != null){
 			return this.player.setWindowProperty(arg0, arg1);
 		}
 		
 		return false;
 	}
 	
 	@Override
 	public boolean addPotionEffect(PotionEffect arg0){
 		if (this.player != null){
 			this.player.addPotionEffect(arg0);
 		}
 		
 		return false;
 	}
 	
 	@Override
 	public boolean addPotionEffect(PotionEffect arg0, boolean arg1){
 		if (this.player != null){
 			return this.player.addPotionEffect(arg0, arg1);
 		}
 		
 		return false;
 	}
 	
 	@Override
 	public boolean addPotionEffects(Collection<PotionEffect> arg0){
 		if (this.player != null){
 			return this.player.addPotionEffects(arg0);
 		}
 		
 		return false;
 	}
 	
 	@Override
 	public void damage(int arg0){
 		if (this.player != null){
 			this.player.damage(arg0);
 		}
 	}
 	
 	@Override
 	public void damage(int arg0, Entity arg1){
 		if (this.player != null){
 			this.player.damage(arg0, arg1);
 		}
 	}
 	
 	@Override
 	public Collection<PotionEffect> getActivePotionEffects(){
 		if (this.player != null){
 			return this.player.getActivePotionEffects();
 		}
 		
 		return Collections.emptyList();
 	}
 	
 	@Override
 	public double getEyeHeight(){
 		if (this.player != null){
 			return this.player.getEyeHeight();
 		}
 		
 		return 0;
 	}
 	
 	@Override
 	public double getEyeHeight(boolean arg0){
 		if (this.player != null){
 			return this.player.getEyeHeight(arg0);
 		}
 		
 		return 0;
 	}
 	
 	@Override
 	public Location getEyeLocation(){
 		if (this.player != null){
 			return this.player.getEyeLocation();
 		}
 		
 		return Bukkit.getWorlds().get(0).getSpawnLocation();
 	}
 	
 	@Override
 	public int getHealth(){
 		if (this.player != null){
 			return this.player.getHealth();
 		}
 		
 		return 0;
 	}
 	
 	@Override
 	public Player getKiller(){
 		if (this.player != null){
 			return this.getKiller();
 		}
 		
 		return null;
 	}
 	
 	@Override
 	public int getLastDamage(){
 		if (this.player != null){
 			return this.getLastDamage();
 		}
 		
 		return 0;
 	}
 	
 	@Override
 	public List<Block> getLastTwoTargetBlocks(HashSet<Byte> arg0, int arg1){
 		if (this.player != null){
 			return this.player.getLastTwoTargetBlocks(arg0, arg1);
 		}
 		
 		return Collections.emptyList();
 	}
 	
 	@Override
 	public List<Block> getLineOfSight(HashSet<Byte> arg0, int arg1){
 		if (this.player != null){
 			return this.player.getLineOfSight(arg0, arg1);
 		}
 		
 		return Collections.emptyList();
 	}
 	
 	@Override
 	public int getMaxHealth(){
 		if (this.player != null){
 			return this.player.getMaxHealth();
 		}
 		
 		return 0;
 	}
 	
 	@Override
 	public int getMaximumAir(){
 		if (this.player != null){
 			return this.player.getMaximumAir();
 		}
 		
 		return 0;
 	}
 	
 	@Override
 	public int getMaximumNoDamageTicks(){
 		if (this.player != null){
 			return this.player.getMaximumNoDamageTicks();
 		}
 		
 		return 0;
 	}
 	
 	@Override
 	public int getNoDamageTicks(){
 		if (this.player != null){
 			return this.player.getNoDamageTicks();
 		}
 		
 		return 0;
 	}
 	
 	@Override
 	public int getRemainingAir(){
 		if (this.player != null){
 			return this.player.getRemainingAir();
 		}
 		
 		return 0;
 	}
 	
 	@Override
 	public Block getTargetBlock(HashSet<Byte> arg0, int arg1){
 		if (this.player != null){
 			return this.player.getTargetBlock(arg0, arg1);
 		}
 		
 		return null;
 	}
 	
 	@Override
 	public boolean hasLineOfSight(Entity arg0){
 		if (this.player != null){
 			return this.player.hasLineOfSight(arg0);
 		}
 		
 		return false;
 	}
 	
 	@Override
 	public boolean hasPotionEffect(PotionEffectType arg0){
 		if (this.player != null){
 			return this.player.hasPotionEffect(arg0);
 		}
 		
 		return false;
 	}
 	
 	@Override
 	public <T extends Projectile> T launchProjectile(Class<? extends T> arg0){
 		if (this.player != null){
 			return this.launchProjectile(arg0);
 		}
 		
 		return null;
 	}
 	
 	@Override
 	public void removePotionEffect(PotionEffectType arg0){
 		if (this.player != null){
 			this.player.removePotionEffect(arg0);
 		}
 	}
 	
 	@Override
 	public void setHealth(int health){
 		if (this.player != null){
 			this.player.setHealth(health);
 		}
 	}
 	
 	@Override
 	public void setLastDamage(int damage){
 		if (this.player != null){
 			this.player.setLastDamage(damage);
 		}
 	}
 	
 	@Override
 	public void setMaximumAir(int ticks){
 		if (this.player != null){
 			this.player.setMaximumAir(ticks);
 		}
 	}
 	
 	@Override
 	public void setMaximumNoDamageTicks(int ticks){
 		if (this.player != null){
 			this.player.setMaximumNoDamageTicks(ticks);
 		}
 	}
 	
 	@Override
 	public void setNoDamageTicks(int ticks){
 		if (this.player != null){
 			this.player.setNoDamageTicks(ticks);
 		}
 	}
 	
 	@Override
 	public void setRemainingAir(int ticks){
 		if (this.player != null){
 			this.player.setRemainingAir(ticks);
 		}
 	}
 	
 	@Override
 	@Deprecated
 	public Arrow shootArrow(){
 		if (this.player != null){
 			return this.player.shootArrow();
 		}
 		
 		return null;
 	}
 	
 	@Override
 	@Deprecated
 	public Egg throwEgg(){
 		if (this.player != null){
 			this.player.throwEgg();
 		}
 		
 		return null;
 	}
 	
 	@Override
 	@Deprecated
 	public Snowball throwSnowball(){
 		if (this.player != null){
 			this.player.throwSnowball();
 		}
 		
 		return null;
 	}
 	
 	@Override
 	public boolean eject(){
 		if (this.player != null){
 			return this.player.eject();
 		}
 		
 		return false;
 	}
 	
 	@Override
 	public int getEntityId(){
 		if (this.player != null){
 			return this.player.getEntityId();
 		}
 		
 		return 0;
 	}
 	
 	@Override
 	public float getFallDistance(){
 		if (this.player != null){
 			return this.player.getFallDistance();
 		}
 		
 		return 0;
 	}
 	
 	@Override
 	public int getFireTicks(){
 		if (this.player != null){
 			return this.player.getFireTicks();
 		}
 		
 		return 0;
 	}
 	
 	@Override
 	public EntityDamageEvent getLastDamageCause(){
 		if (this.player != null){
 			return this.player.getLastDamageCause();
 		}
 		
 		return null;
 	}
 	
 	@Override
 	public Location getLocation(){
 		if (this.player != null){
 			return this.player.getLocation();
 		}
 		
 		return Bukkit.getWorlds().get(0).getSpawnLocation();
 	}
 	
 	@Override
 	public int getMaxFireTicks(){
 		if (this.player != null){
 			return this.player.getMaxFireTicks();
 		}
 		
 		return 0;
 	}
 	
 	@Override
 	public List<Entity> getNearbyEntities(double x, double y, double z){
 		if (this.player != null){
 			return this.player.getNearbyEntities(x, y, z);
 		}
 		
 		return Collections.emptyList();
 	}
 	
 	@Override
 	public Entity getPassenger(){
 		if (this.player != null){
 			return this.player.getPassenger();
 		}
 		
 		return null;
 	}
 	
 	@Override
 	public Server getServer(){
 		if (this.player != null){
 			return this.getServer();
 		}
 		
 		return Bukkit.getServer();
 	}
 	
 	@Override
 	public int getTicksLived(){
 		if (this.player != null){
 			return this.player.getTicksLived();
 		}
 		
 		return 0;
 	}
 	
 	@Override
 	public EntityType getType(){
 		if (this.player != null){
 			return this.player.getType();
 		}
 		
 		return EntityType.PLAYER;
 	}
 	
 	@Override
 	public UUID getUniqueId(){
 		if (this.player != null){
 			return this.player.getUniqueId();
 		}
 		
 		return null;
 	}
 	
 	@Override
 	public Entity getVehicle(){
 		if (this.player != null){
 			return this.player.getVehicle();
 		}
 		
 		return null;
 	}
 	
 	@Override
 	public Vector getVelocity(){
 		if (this.player != null){
 			return this.player.getVelocity();
 		}
 		
 		return new Vector(0, 0, 0);
 	}
 	
 	@Override
 	public boolean isDead(){
 		if (this.player != null){
 			return this.player.isDead();
 		}
 		
 		return false;
 	}
 	
 	@Override
 	public boolean isEmpty(){
 		if (this.player != null){
 			return this.player.isEmpty();
 		}
 		
 		return false;
 	}
 	
 	@Override
 	public boolean isInsideVehicle(){
 		if (this.player != null){
 			return this.player.isInsideVehicle();
 		}
 		
 		return false;
 	}
 	
 	@Override
 	public boolean isValid(){
 		if (this.player != null){
 			return this.player.isValid();
 		}
 		
 		return false;
 	}
 	
 	@Override
 	public boolean leaveVehicle(){
 		if (this.player != null){
 			return this.player.leaveVehicle();
 		}
 		
 		return false;
 	}
 	
 	@Override
 	public void playEffect(EntityEffect type){
 		if (this.player != null){
 			this.player.playEffect(type);
 		}
 	}
 	
 	@Override
 	public void remove(){
 		if (this.player != null){
 			this.player.remove();
 		}
 	}
 	
 	@Override
 	public void setFallDistance(float distance){
 		if (this.player != null){
 			this.player.setFallDistance(distance);
 		}
 	}
 	
 	@Override
 	public void setFireTicks(int ticks){
 		if (this.player != null){
 			this.player.setFireTicks(ticks);
 		}
 	}
 	
 	@Override
 	public void setLastDamageCause(EntityDamageEvent event){
 		if (this.player != null){
 			this.player.setLastDamageCause(event);
 		}
 	}
 	
 	@Override
 	public boolean setPassenger(Entity passenger){
 		if (this.player != null){
 			return this.setPassenger(passenger);
 		}
 		
 		return false;
 	}
 	
 	@Override
 	public void setTicksLived(int value){
 		if (this.player != null){
 			this.player.setTicksLived(value);
 		}
 	}
 	
 	@Override
 	public void setVelocity(Vector velocity){
 		if (this.player != null){
 			this.player.setVelocity(velocity);
 		}
 	}
 	
 	@Override
 	public boolean teleport(Location location){
 		if (this.player != null){
 			return this.player.teleport(location);
 		}
 		
 		return false;
 	}
 	
 	@Override
 	public boolean teleport(Entity destination){
 		if (this.player != null){
 			return this.player.teleport(destination);
 		}
 		
 		return false;
 	}
 	
 	@Override
 	public boolean teleport(Location location, TeleportCause cause){
 		if (this.player != null){
 			return this.player.teleport(location, cause);
 		}
 		
 		return false;
 	}
 	
 	@Override
 	public boolean teleport(Entity destination, TeleportCause cause){
 		if (this.player != null){
 			return this.teleport(destination, cause);
 		}
 		
 		return false;
 	}
 	
 	@Override
 	public List<MetadataValue> getMetadata(String metadataKey){
 		if (this.player != null){
 			return this.player.getMetadata(metadataKey);
 		}
 		
 		return Collections.emptyList();
 	}
 	
 	@Override
 	public boolean hasMetadata(String metadataKey){
 		if (this.player != null){
 			return this.player.hasMetadata(metadataKey);
 		}
 		
 		return false;
 	}
 	
 	@Override
 	public void removeMetadata(String metadataKey, Plugin owningPlugin){
 		if (this.player != null){
 			this.player.removeMetadata(metadataKey, owningPlugin);
 		}
 	}
 	
 	@Override
 	public void setMetadata(String metadataKey, MetadataValue newMetadataValue){
 		if (this.player != null){
 			this.player.setMetadata(metadataKey, newMetadataValue);
 		}
 	}
 	
 	@Override
 	public PermissionAttachment addAttachment(Plugin plugin){
 		if (this.player != null){
 			return this.player.addAttachment(plugin);
 		}
 		
 		return null;
 	}
 	
 	@Override
 	public PermissionAttachment addAttachment(Plugin plugin, int ticks){
 		if (this.player != null){
 			return this.player.addAttachment(plugin, ticks);
 		}
 		
 		return null;
 	}
 	
 	@Override
 	public PermissionAttachment addAttachment(Plugin plugin, String name, boolean value){
 		if (this.player != null){
 			return this.player.addAttachment(plugin, name, value);
 		}
 		
 		return null;
 	}
 	
 	@Override
 	public PermissionAttachment addAttachment(Plugin plugin, String name, boolean value, int ticks){
 		if (this.player != null){
 			return this.player.addAttachment(plugin, name, value, ticks);
 		}
 		
 		return null;
 	}
 	
 	@Override
 	public Set<PermissionAttachmentInfo> getEffectivePermissions(){
 		if (this.player != null){
 			return this.player.getEffectivePermissions();
 		}
 		
 		return Collections.emptySet();
 	}
 	
 	@Override
 	public boolean hasPermission(String name){
 		if (this.player != null){
 			return this.player.hasPermission(name);
 		}
 		
 		return false;
 	}
 	
 	@Override
 	public boolean hasPermission(Permission perm){
 		if (this.player != null){
 			return this.player.hasPermission(perm);
 		}
 		
 		return false;
 	}
 	
 	@Override
 	public boolean isPermissionSet(String name){
 		if (this.player != null){
 			return this.player.isPermissionSet(name);
 		}
 		
 		return false;
 	}
 	
 	@Override
 	public boolean isPermissionSet(Permission perm){
 		if (this.player != null){
 			return this.player.isPermissionSet(perm);
 		}
 		
 		return false;
 	}
 	
 	@Override
 	public void recalculatePermissions(){
 		if (this.player != null){
 			this.player.recalculatePermissions();
 		}
 	}
 	
 	@Override
 	public void removeAttachment(PermissionAttachment attachment){
 		if (this.player != null){
 			this.player.removeAttachment(attachment);
 		}
 	}
 	
 	@Override
 	public boolean isOp(){
 		if (this.player != null){
 			return this.player.isOp();
 		}
 		
 		return false;
 	}
 	
 	@Override
 	public void setOp(boolean value){
 		if (this.player != null){
 			this.player.setOp(value);
 		}
 	}
 	
 	@Override
 	public void abandonConversation(Conversation conversation){
 		if (this.player != null){
 			this.player.abandonConversation(conversation);
 		}
 	}
 	
 	@Override
 	public void abandonConversation(Conversation conversation, ConversationAbandonedEvent details){
 		if (this.player != null){
 			this.player.abandonConversation(conversation, details);
 		}
 	}
 	
 	@Override
 	public void acceptConversationInput(String input){
 		if (this.player != null){
 			this.player.acceptConversationInput(input);
 		}
 	}
 	
 	@Override
 	public boolean beginConversation(Conversation conversation){
 		if (this.player != null){
 			this.player.beginConversation(conversation);
 		}
 		
 		return false;
 	}
 	
 	@Override
 	public boolean isConversing(){
 		if (this.player != null){
 			return this.player.isConversing();
 		}
 		
 		return false;
 	}
 	
 	@Override
 	public long getFirstPlayed(){
 		if (this.player != null){
 			return this.player.getFirstPlayed();
 		}
 		
 		return 0;
 	}
 	
 	@Override
 	public long getLastPlayed(){
 		if (this.player != null){
 			return this.player.getLastPlayed();
 		}
 		
 		return 0;
 	}
 	
 	@Override
 	public Player getPlayer(){
 		if (this.player != null){
 			return this.player.getPlayer();
 		}
 		
 		return null;
 	}
 	
 	@Override
 	public boolean hasPlayedBefore(){
 		if (this.player != null){
 			return this.player.hasPlayedBefore();
 		}
 		
 		return false;
 	}
 	
 	@Override
 	public boolean isBanned(){
 		if (this.player != null){
 			return this.player.isBanned();
 		}
 		
 		return false;
 	}
 	
 	@Override
 	public boolean isOnline(){
 		if (this.player != null){
 			return this.player.isOnline();
 		}
 		
 		return false;
 	}
 	
 	@Override
 	public boolean isWhitelisted(){
 		if (this.player != null){
 			return this.player.isWhitelisted();
 		}
 		
 		return false;
 	}
 	
 	@Override
 	public void setBanned(boolean banned){
 		if (this.player != null){
 			this.player.setBanned(banned);
 		}
 	}
 	
 	@Override
 	public void setWhitelisted(boolean value){
 		if (this.player != null){
 			this.player.setWhitelisted(value);
 		}
 	}
 	
 	@Override
 	public Map<String, Object> serialize(){
 		if (this.player != null){
 			return this.player.serialize();
 		}
 		
 		return null;
 	}
 	
 	@Override
 	public Set<String> getListeningPluginChannels(){
 		if (this.player != null){
 			return this.player.getListeningPluginChannels();
 		}
 		
 		return Collections.emptySet();
 	}
 	
 	@Override
 	public void sendPluginMessage(Plugin source, String channel, byte[] message){
 		if (this.player != null){
 			this.sendPluginMessage(source, channel, message);
 		}
 	}
 	
 	@Override
 	public void awardAchievement(Achievement achievement){
 		if (this.player != null){
 			this.player.awardAchievement(achievement);
 		}
 	}
 	
 	@Override
 	public boolean canSee(Player player){
 		if (this.player != null){
 			return this.player.canSee(player);
 		}
 		
 		return false;
 	}
 	
 	@Override
 	public void chat(String msg){
 		if (this.player != null){
 			this.player.chat(msg);
 		}
 	}
 	
 	@Override
 	public InetSocketAddress getAddress(){
 		if (this.player != null){
 			return this.player.getAddress();
 		}
 		
 		return null;
 	}
 	
 	@Override
 	public boolean getAllowFlight(){
 		if (this.player != null){
 			return this.player.getAllowFlight();
 		}
 		
 		return false;
 	}
 	
 	@Override
 	public Location getBedSpawnLocation(){
 		if (this.player != null){
 			return this.player.getBedSpawnLocation();
 		}
 		
 		return Bukkit.getOfflinePlayer(this.name).getBedSpawnLocation();
 	}
 	
 	@Override
 	public Location getCompassTarget(){
 		if (this.player != null){
 			return this.player.getCompassTarget();
 		}
 		
 		return Bukkit.getWorlds().get(0).getSpawnLocation();
 	}
 	
 	@Override
 	public String getDisplayName(){
 		if (this.player != null){
 			return this.player.getDisplayName();
 		}
 		
 		return this.name;
 	}
 	
 	@Override
 	public float getExhaustion(){
 		if (this.player != null){
 			return this.player.getExhaustion();
 		}
 		
 		return 0;
 	}
 	
 	@Override
 	public float getExp(){
 		if (this.player != null){
 			return this.player.getExp();
 		}
 		
 		return 0;
 	}
 	
 	@Override
 	public float getFlySpeed(){
 		if (this.player != null){
 			return this.player.getFlySpeed();
 		}
 		
 		return 0;
 	}
 	
 	@Override
 	public int getFoodLevel(){
 		if (this.player != null){
 			return this.player.getFoodLevel();
 		}
 		
 		return 0;
 	}
 	
 	@Override
 	public int getLevel(){
 		if (this.player != null){
 			return this.player.getLevel();
 		}
 		
 		return 0;
 	}
 	
 	@Override
 	public String getPlayerListName(){
 		if (this.player != null){
 			return this.player.getPlayerListName();
 		}
 		
 		return this.name;
 	}
 	
 	@Override
 	public long getPlayerTime(){
 		if (this.player != null){
 			return this.player.getPlayerTime();
 		}
 		
 		return 0;
 	}
 	
 	@Override
 	public long getPlayerTimeOffset(){
 		if (this.player != null){
 			return this.player.getPlayerTimeOffset();
 		}
 		
 		return 0;
 	}
 	
 	@Override
 	public float getSaturation(){
 		if (this.player != null){
 			return this.player.getSaturation();
 		}
 		
 		return 0;
 	}
 	
 	@Override
 	public int getTotalExperience(){
 		if (this.player != null){
 			return this.player.getTotalExperience();
 		}
 		
 		return 0;
 	}
 	
 	@Override
 	public float getWalkSpeed(){
 		if (this.player != null){
 			return this.player.getWalkSpeed();
 		}
 		
 		return 0;
 	}
 	
 	@Override
 	public void giveExp(int amount){
 		if (this.player != null){
 			this.player.giveExp(amount);
 		}
 	}
 	
 	@Override
 	public void hidePlayer(Player player){
 		if (this.player != null){
 			this.player.hidePlayer(player);
 		}
 	}
 	
 	@Override
 	public void incrementStatistic(Statistic statistic){
 		if (this.player != null){
 			this.player.incrementStatistic(statistic);
 		}
 	}
 	
 	@Override
 	public void incrementStatistic(Statistic statistic, int amount){
 		if (this.player != null){
 			this.player.incrementStatistic(statistic, amount);
 		}
 	}
 	
 	@Override
 	public void incrementStatistic(Statistic statistic, Material material){
 		if (this.player != null){
 			this.player.incrementStatistic(statistic, material);
 		}
 	}
 	
 	@Override
 	public void incrementStatistic(Statistic statistic, Material material, int amount){
 		if (this.player != null){
 			this.player.incrementStatistic(statistic, material, amount);
 		}
 	}
 	
 	@Override
 	public boolean isFlying(){
 		if (this.player != null){
 			return this.player.isFlying();
 		}
 		
 		return false;
 	}
 	
 	@Override
 	public boolean isPlayerTimeRelative(){
 		if (this.player != null){
 			return this.player.isPlayerTimeRelative();
 		}
 		
 		return false;
 	}
 	
 	@Override
 	public boolean isSleepingIgnored(){
 		if (this.player != null){
 			return this.player.isSleepingIgnored();
 		}
 		
 		return false;
 	}
 	
 	@Override
 	public boolean isSneaking(){
 		if (this.player != null){
 			return this.player.isSneaking();
 		}
 		
 		return false;
 	}
 	
 	@Override
 	public boolean isSprinting(){
 		if (this.player != null){
 			return this.player.isSprinting();
 		}
 		
 		return false;
 	}
 	
 	@Override
 	public void kickPlayer(String message){
 		if (this.player != null){
 			this.player.kickPlayer(message);
 		}
 	}
 	
 	@Override
 	public void loadData(){
 		if (this.player != null){
 			this.player.loadData();
 		}
 	}
 	
 	@Override
 	public boolean performCommand(String command){
 		if (this.player != null){
 			return this.player.performCommand(command);
 		}
 		
 		return false;
 	}
 	
 	@Override
 	public void playEffect(Location loc, Effect effect, int data){
 		if (this.player != null){
 			this.player.playEffect(loc, effect, data);
 		}
 	}
 	
 	@Override
 	public <T> void playEffect(Location loc, Effect effect, T data){
 		if (this.player != null){
 			this.player.playEffect(loc, effect, data);
 		}
 	}
 	
 	@Override
 	public void playNote(Location loc, byte instrument, byte note){
 		if (this.player != null){
 			this.player.playNote(loc, instrument, note);
 		}
 	}
 	
 	@Override
 	public void playNote(Location loc, Instrument instrument, Note note){
 		if (this.player != null){
 			this.player.playNote(loc, instrument, note);
 		}
 	}
 	
 	@Override
 	public void playSound(Location arg0, Sound arg1, float arg2, float arg3){
 		if (this.player != null){
 			this.player.playSound(arg0, arg1, arg2, arg3);
 		}
 	}
 	
 	@Override
 	public void resetPlayerTime(){
 		if (this.player != null){
 			this.player.resetPlayerTime();
 		}
 	}
 	
 	@Override
 	public void saveData(){
 		if (this.player != null){
 			this.player.saveData();
 		}
 	}
 	
 	@Override
 	public void sendBlockChange(Location loc, Material material, byte data){
 		if (this.player != null){
 			this.player.sendBlockChange(loc, material, data);
 		}
 	}
 	
 	@Override
 	public void sendBlockChange(Location loc, int material, byte data){
 		if (this.player != null){
 			this.player.sendBlockChange(loc, material, data);
 		}
 	}
 	
 	@Override
 	public boolean sendChunkChange(Location loc, int sx, int sy, int sz, byte[] data){
 		if (this.player != null){
 			return this.player.sendChunkChange(loc, sx, sy, sz, data);
 		}
 		
 		return false;
 	}
 	
 	@Override
 	public void sendMap(MapView map){
 		if (this.player != null){
 			this.player.sendMap(map);
 		}
 	}
 	
 	@Override
 	public void sendRawMessage(String message){
 		if (this.player != null){
 			this.player.sendRawMessage(message);
 		}
 	}
 	
 	@Override
 	public void setAllowFlight(boolean flight){
 		if (this.player != null){
 			this.player.setAllowFlight(flight);
 		}
 	}
 	
 	@Override
 	public void setBedSpawnLocation(Location location){
 		if (this.player != null){
 			this.player.setBedSpawnLocation(location);
 		}
 	}
 	
 	@Override
 	public void setCompassTarget(Location loc){
 		if (this.player != null){
 			this.player.setCompassTarget(loc);
 		}
 	}
 	
 	@Override
 	public void setDisplayName(String name){
 		if (this.player != null){
 			this.player.setDisplayName(name);
 		}
 	}
 	
 	@Override
 	public void setExhaustion(float value){
 		if (this.player != null){
 			this.player.setExhaustion(value);
 		}
 	}
 	
 	@Override
 	public void setExp(float exp){
 		if (this.player != null){
 			this.player.setExp(exp);
 		}
 	}
 	
 	@Override
 	public void setFlySpeed(float arg0) throws IllegalArgumentException{
 		if (this.player != null){
 			this.player.setFlySpeed(arg0);
 		}
 	}
 	
 	@Override
 	public void setFlying(boolean value){
 		if (this.player != null){
 			this.player.setFlying(value);
 		}
 	}
 	
 	@Override
 	public void setFoodLevel(int value){
 		if (this.player != null){
 			this.player.setFoodLevel(value);
 		}
 	}
 	
 	@Override
 	public void setLevel(int level){
 		if (this.player != null){
 			this.player.setLevel(level);
 		}
 	}
 
 	@Override
 	public void setPlayerListName(String name){
 		if (this.player != null){
 			this.player.setPlayerListName(name);
 		}
 	}
 	
 	@Override
 	public void setPlayerTime(long time, boolean relative){
 		if (this.player != null){
 			this.player.setPlayerTime(time, relative);
 		}
 	}
 	
 	@Override
 	public void setSaturation(float value){
 		if (this.player != null){
 			this.player.setSaturation(value);
 		}
 	}
 	
 	@Override
 	public void setSleepingIgnored(boolean isSleeping){
 		if (this.player != null){
 			this.player.setSleepingIgnored(isSleeping);
 		}
 	}
 	
 	@Override
 	public void setSneaking(boolean sneak){
 		if (this.player != null){
 			this.player.setSneaking(sneak);
 		}
 	}
 	
 	@Override
 	public void setSprinting(boolean sprinting){
 		if (this.player != null){
 			this.player.setSprinting(sprinting);
 		}
 	}
 	
 	@Override
 	public void setTotalExperience(int exp){
 		if (this.player != null){
 			this.player.setTotalExperience(exp);
 		}
 	}
 	
 	@Override
 	public void setWalkSpeed(float arg0) throws IllegalArgumentException{
 		if (this.player != null){
 			this.player.setWalkSpeed(arg0);
 		}
 	}
 	
 	@Override
 	public void showPlayer(Player player){
 		if (this.player != null){
 			this.player.showPlayer(player);
 		}
 	}
 	
 	@Override
 	@Deprecated
 	public void updateInventory(){
 		if (this.player != null){
 			this.player.updateInventory();
 		}
 	}
 	
 }
