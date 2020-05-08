 package uk.thecodingbadgers.minekart.jockey;
 
 import java.util.Random;
 
 import net.citizensnpcs.api.CitizensAPI;
 import net.citizensnpcs.api.npc.NPC;
 import net.citizensnpcs.api.trait.trait.Owner;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Color;
 import org.bukkit.GameMode;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.enchantments.Enchantment;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.PlayerInventory;
 import org.bukkit.inventory.meta.ItemMeta;
 import org.bukkit.inventory.meta.LeatherArmorMeta;
 import org.bukkit.potion.PotionEffect;
 import org.bukkit.potion.PotionEffectType;
 import org.bukkit.util.Vector;
 
 import uk.thecodingbadgers.minekart.MineKart;
 import uk.thecodingbadgers.minekart.powerup.Powerup;
 import uk.thecodingbadgers.minekart.race.Race;
 
 /**
  * @author TheCodingBadgers
  *
  * A jockey consists of a player and his mount. A mount is any entity that can be controlled.
  * Jockeys also hold any powerups attained during a race. 
  *
  */
 @SuppressWarnings("deprecation")
 public class Jockey {
 	
 	/** The player which represents this jockey */
 	private Player player = null;
 	
 	/** The type of mount the jockey will use */
 	private EntityType mountType = EntityType.UNKNOWN;
 	
 	/** The color which represents this jockey */
 	private Color jockeyColor = Color.RED;
 	
 	/** The jockeys mount */
 	private NPC mount = null;
 	
 	/** The location where the jockey should be taken too on race exit */
 	private Location exitLocaiton = null;
 	
 	/** The race that this jockey is in */
 	private Race race = null;
 	
 	/** The time the jockey started the race */
 	private long startTime = 0L;
 
 	/** The player backup of this jockey, storing invent, gamemode ect... */
 	private PlayerBackup backup = null;
 	
 	/** The last known location */
 	private Vector cachedLocation = null;
 	
 	/** The last checkpoint a jockey went through, or their spawn point */
 	private Location respawnLocation = null;
 	
 	/** The jockeys powerup */
 	private Powerup powerup = null;
 	
 	/**
 	 * 
 	 * @param player
 	 * @param mountType
 	 * @param race 
 	 */
 	public Jockey(Player player, EntityType mountType, Race race) {
 		this.player = player;
 		this.mountType = mountType;
 		this.race = race;
 		this.exitLocaiton = player.getLocation();
 		this.jockeyColor = getRandomColor();
 		
 		this.backup = new PlayerBackup();
 		backupInventory(this.player);
 		
 		equipGear();
 	}
 
 	/**
 	 * Backup a players inventory and other information
 	 */
 	private void backupInventory(Player player) {
 		
 		// store data
 		this.backup.backup(player);
 		
 		// clear invent
 		player.setGameMode(GameMode.ADVENTURE);
 		player.setFlying(false);
 		clearInventory(player.getInventory());
 		player.updateInventory();
 		player.getActivePotionEffects().clear();
 		player.setExp(0.0f);
 		player.setLevel(0);
 	}
 	
 	/**
 	 * Restore a players inventory and other information
 	 */
 	private void restoreInventory(Player player) {
 
 		// clear invent
 		clearInventory(player.getInventory());
 		player.updateInventory();
 		player.getActivePotionEffects().clear();
 		player.setExp(0.0f);
 		player.setLevel(0);
 		
 		// restore data
 		this.backup.restore(player);
 
 	}
 	
 	/**
 	 * Clear a player inventory
 	 * @param invent The inventory to clear
 	 */
 	private void clearInventory(PlayerInventory invent) {
 		invent.clear();
 		invent.setHelmet(null);
 		invent.setChestplate(null);
 		invent.setLeggings(null);
 		invent.setBoots(null);
 	}
 	
 	/**
 	 * Equip the jockey armour gear
 	 */
 	public void equipGear() {
 		// Give the player a coloured jersey
 		ItemStack jersey = new ItemStack(Material.LEATHER_CHESTPLATE);
 		LeatherArmorMeta jerseyMeta = (LeatherArmorMeta) jersey.getItemMeta();
 		jerseyMeta.setColor(this.jockeyColor);
 		jersey.setItemMeta(jerseyMeta);
 		
 		// Give the player a coloured hat
 		ItemStack helmet = new ItemStack(Material.LEATHER_HELMET);
 		LeatherArmorMeta helmetMeta = (LeatherArmorMeta) helmet.getItemMeta();
 		helmetMeta.setColor(this.jockeyColor);
 		helmet.setItemMeta(helmetMeta);
 		
 		// Give the jockey white leggings
 		ItemStack leggings = new ItemStack(Material.LEATHER_LEGGINGS);
 		LeatherArmorMeta leggingsMeta = (LeatherArmorMeta) leggings.getItemMeta();
 		leggingsMeta.setColor(Color.WHITE);
 		leggings.setItemMeta(leggingsMeta);
 		
 		// Give the jockey black boots
 		ItemStack boots = new ItemStack(Material.LEATHER_BOOTS);
 		LeatherArmorMeta bootsMeta = (LeatherArmorMeta) boots.getItemMeta();
 		bootsMeta.setColor(Color.BLACK);
 		boots.setItemMeta(bootsMeta);
 		
 		player.getInventory().setHelmet(helmet);
 		player.getInventory().setChestplate(jersey);
 		player.getInventory().setLeggings(leggings);
 		player.getInventory().setBoots(boots);
 		
 		player.updateInventory();
 	}
 
 	/**
 	 * Get a random color
 	 * @return A color
 	 */
 	private Color getRandomColor() {
 		Random random = new Random();
 		return Color.fromRGB(random.nextInt(256), random.nextInt(256), random.nextInt(256));
 	}
 
 	/**
 	 * Get the player which represents this jockey
 	 * @return The player
 	 */
 	public Player getPlayer() {
 		return this.player;
 	}
 
 	/**
 	 * Teleport a jockey to a spawn and put them on their mount 
 	 * @param spawn The spawn location
 	 */
 	public void teleportToSpawn(Location spawn) {
 		
 		// Teleport the jockey to their mount
 		this.player.teleport(spawn);
 		this.respawnLocation = spawn;
 		
 		// Make their mounts
 		this.mount = CitizensAPI.getNPCRegistry().createNPC(this.mountType, getRadomMountName(this.player.getName()));
 		this.mount.setProtected(true);
 		this.mount.addTrait(new ControllableMount(true));
 		this.mount.spawn(spawn);
 		
 		// Set the owner of the mount to the jockey
 		Owner owner = this.mount.getTrait(Owner.class);
 		owner.setOwner(this.player.getName());
 		
 		// Make the NPC controllable and mount the player
 		ControllableMount trait = this.mount.getTrait(ControllableMount.class);
 		trait.mount(this.player);	
 		trait.setEnabled(false); // disable it until the race has started
 		
 		// Give the player a whip
 		ItemStack whip = new ItemStack(Material.STICK);
 		ItemMeta whipMeta = whip.getItemMeta();
 		whipMeta.setDisplayName("Whip");
 		whip.setItemMeta(whipMeta);
 		whip.setAmount(4);
 		whip.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 1);
 		player.getInventory().setItem(0, whip);
 		
 		// Give the player a respawn skull
 		ItemStack respawnSkull = new ItemStack(Material.SKULL);
 		ItemMeta skullMeta = respawnSkull.getItemMeta();
 		skullMeta.setDisplayName("Respawn");
 		respawnSkull.setItemMeta(skullMeta);
 		player.getInventory().setItem(8, respawnSkull);
 		
 		player.getInventory().setHeldItemSlot(0);
 	}
 
 	/**
 	 * Get a random name to be used by a mount
 	 * @return A string to be used as the mount name
 	 */
 	private String getRadomMountName(String jockeyName) {
 		
 		String[] allNames = {
 			"Mental Boy", "Nervous Sparxx", "OAP Money", "Clean Smoke",
 			"Gnashing Panic", "Near Pride", "Bringing Action", "Nefarious Dusty",
 			"Tornado Fall", "Jim's Depression", "Caramel Comedy", "Wally's Maiden",
 			"Dirty Underwear", "Romantic Apple", "Wisby's Revenge", "Rabid Ruler",
 			"Scared Sally", "Prancers Dream", "Tidy's Teen", "Losing Hope",
 			"Whisky Galore", "Who's Dr", "Nintendon't", "Glue Factory", "Hooves McCoy", 
 			"Red Lightning", "Lazy Susan", "Woolly Toque", "Granola Bar", "Bloody Harvest", 
 			"Wet Blanket", "Actually Fast", "Horse IV", "See Spot Fly", "Fox in Socks", 
 			"One Way", "Beans", "To The Moon", "Bitter Blue", "Black Cadillac", "Landing Gear", 
 			"Not American", "Ringo Star", "Mystery Man", "Spits-A-Lot", "Hungry Hippo", 
 			"Chapter 13", "Almost Pearls", "The Lady", "Graceling", 
 			"Lockpick", "Pants", "Gold Pilot", "Fires Star", "Simply Food", "Scrap Paper",
 			"Scrap Paper X", "Doomsday Kettle", "Vygotskys Plan", "German Tank", 
 			"Horse-Bear", "Steroids Galore", "Blindsight", "The Scientist", "Robo Horse", 
 			"Lightning Hoof", "Robo Horse II", "Robo Horse III", "Added Calcium", "Gnasty Gnorc", 
 			"Dream Weaver", "French Toast", "Sun Seeker", "El Horso", "My Little Pony",
 			"Guy in a Suit", "Almost Dead", "Big Mac", "Gravitys Foe", "George",
 			"Applesauce", "Iron Knight", "In the Morning", "Cleverfoot", "Peggy",
 			"TDC Pizza", "Princess Tilly", "Emmerica"
 		};
 		
 		Random random = new Random();
 		
 		if (jockeyName.equalsIgnoreCase("itstolate") && random.nextBoolean()) {
 			return "Canada Smells";
 		}
 		
 		if (jockeyName.equalsIgnoreCase("tilly_lala")) {
 			return random.nextBoolean() ? "George" : "Peggy";
 		}
 		
 		return allNames[random.nextInt(allNames.length)];
 	}
 
 	/**
 	 * Called when a race starts
 	 */
 	public void onRaceStart() {
 		
 		ControllableMount trait = this.mount.getTrait(ControllableMount.class);
 		trait.setEnabled(true);
 		this.startTime = System.currentTimeMillis();
 		
 	}
 
 	/**
 	 * Call when a race has ended
 	 */
 	public void onRaceEnd() {
 		
 		// Unmount and remove the mount
 		if (this.mount != null) {
 			this.mount.getBukkitEntity().eject();
 			this.mount.destroy();
 		}
 		
 		// Restore the jockeys items
 		restoreInventory(this.player);
 		
 		// Teleport the player to their exit location
 		this.player.teleport(this.exitLocaiton);
 	}
 
 	/**
 	 * Get the race this jockey is in
 	 * @return The race instance
 	 */
 	public Race getRace() {
 		return this.race;
 	}
 
 	/**
 	 * Increase the speed of the mount for a given amount of time
 	 * @param speed The new speed of the mount
 	 * @param length The amount of time the speed boost should be applied
 	 */
 	public void increaseSpeed(int speed, int length) {
 		
 		PotionEffect effect = new PotionEffect(PotionEffectType.SPEED, length * 20, speed, false);
 		this.player.addPotionEffect(effect, true);
 		this.mount.getBukkitEntity().addPotionEffect(effect, true);
 		
 	}
 
 	/**
 	 * Get the jockeys mount
 	 * @return The NPC mount
 	 */
 	public NPC getMount() {
 		return this.mount;
 	}
 
 	/**
 	 * Get the time the player has been in the race
 	 * @return The time in milliseconds
 	 */
 	public long getRaceTime() {
 		return System.currentTimeMillis() - this.startTime;
 	}
 
 	/**
 	 * Returns if a players has moved between blocks
 	 * @param location The location to test against
 	 * @return True if they have moved, false otherwise
 	 */
 	public boolean hasMoved(Location location) {
 		
 		if (cachedLocation == null) {
 			cachedLocation = location.toVector();
 			return true;
 		}
 		
 		if (cachedLocation.equals(location.toVector())) {
 			return false;
 		}
 		
 		cachedLocation = location.toVector();
 		return true;
 	}
 
 	/** 
 	 * Respawn a jockey to their last known respawn location.
 	 */
 	public void respawn() {
 				
 		final String mountName = this.mount.getName();
 		
 		ControllableMount trait = this.mount.getTrait(ControllableMount.class);
 		trait.mount(this.player);
 		this.mount.destroy();
 		
 		final Jockey jockey = this;
 		Bukkit.getScheduler().scheduleSyncDelayedTask(MineKart.getInstance(), new Runnable() {
 
 			@Override
 			public void run() {
 				
 				jockey.player.teleport(jockey.respawnLocation);
 				jockey.player.setHealth(jockey.player.getMaxHealth());
 				jockey.player.setFireTicks(0);
 				
 				// Make a new mount
 				jockey.mount = CitizensAPI.getNPCRegistry().createNPC(jockey.mountType, mountName);
 				jockey.mount.setProtected(true);
 				jockey.mount.addTrait(new ControllableMount(true));
 				jockey.mount.spawn(jockey.respawnLocation);
 				
 				// Set the owner of the mount to the jockey
 				Owner owner = jockey.mount.getTrait(Owner.class);
 				owner.setOwner(jockey.player.getName());
 				
 				// Make the NPC controllable and mount the player
 				ControllableMount trait = jockey.mount.getTrait(ControllableMount.class);
 				trait.mount(jockey.player);	
 				trait.setEnabled(true);
 				
 			}
 			
 		}, 2L);
 		
 	}
 
 	/**
 	 * Update a players respawn location to their current location
 	 */
 	public void updateRespawnLocation(Location location) {
 		this.respawnLocation = location;
 	}
 
 	/**
	 * 
	 * @param powerup
 	 */
 	public void setPowerup(Powerup powerup) {
 		this.powerup = powerup;
 	}
 	
 	/**
	 * 
	 * @return
 	 */
 	public Powerup getPowerup() {
 		return this.powerup;
 	}
 
 	/**
 	 * Mark this jockey as ready for the game to start
	 * 
      * @return true if successful, false otherwise (eg. already ready)
 	 */
     public boolean readyUp() {
         return this.race.readyUp(this);
     }
 	
 }
