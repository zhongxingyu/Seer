 package pgDev.bukkit.DisguiseCraft;
 
 import java.util.LinkedList;
 import java.util.logging.Level;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 
 import pgDev.bukkit.DisguiseCraft.packet.DCPacketGenerator;
 import pgDev.bukkit.DisguiseCraft.packet.PLPacketGenerator;
 
 import net.minecraft.server.DataWatcher;
 import net.minecraft.server.Packet18ArmAnimation;
 import net.minecraft.server.Packet201PlayerInfo;
 import net.minecraft.server.Packet20NamedEntitySpawn;
 import net.minecraft.server.Packet24MobSpawn;
 import net.minecraft.server.Packet29DestroyEntity;
 import net.minecraft.server.Packet32EntityLook;
 import net.minecraft.server.Packet33RelEntityMoveLook;
 import net.minecraft.server.Packet34EntityTeleport;
 import net.minecraft.server.Packet35EntityHeadRotation;
 import net.minecraft.server.Packet38EntityStatus;
 import net.minecraft.server.Packet40EntityMetadata;
 import net.minecraft.server.Packet5EntityEquipment;
 
 /**
  * This is the class for every disguise object. It contains
  * the functions for creating, editing, and sending disguises.
  * @author PG Dev Team (Devil Boy, Tux2)
  */
 public class Disguise {
 	/**
 	 * This is the list of possible mob disguises listed by
 	 * their Bukkit class name.
 	 * @author PG Dev Team (Devil Boy)
 	 */
 	public enum MobType {
 		Bat(65),
 		Blaze(61),
 		CaveSpider(59),
 		Chicken(93),
 		Cow(92),
 		Creeper(50),
 		EnderDragon(63),
 		Enderman(58),
 		Ghast(56),
 		Giant(53),
 		IronGolem(99),
 		MagmaCube(62),
 		MushroomCow(96),
 		Ocelot(98),
 		Pig(90),
 		PigZombie(57),
 		Sheep(91),
 		Silverfish(60),
 		Skeleton(51),
 		Slime(55),
 		Snowman(97),
 		Spider(52),
 		Squid(94),
 		Villager(120),
 		Witch(66),
 		Wither(64),
 		Wolf(95),
 		Zombie(54);
 		
 		/**
 		 * The entity-type ID.
 		 */
 		public final byte id;
 		MobType(int i) {
 			id = (byte) i;
 		}
 		
 		/**
 		 * Check if the mob type is a subclass of an Entity class from Bukkit.
 		 * This is extremely useful to seeing if a mob can have a certain
 		 * subtype. For example: only members of the Animal class (and villagers)
 		 * can have a baby form.
 		 * @param cls The class to compare to
 		 * @return true if the mobtype is a subclass, false otherwise
 		 */
 		public boolean isSubclass(Class<?> cls) {
 			try {
 				return cls.isAssignableFrom(Class.forName("org.bukkit.entity." + name()));
 			} catch (ClassNotFoundException e) {
 				e.printStackTrace();
 			}
 			return false;
 		}
 		
 		/**
 		 * Get the MobType from its name
 		 * Works like valueOf, but not case sensitive
 		 * @param text The string to match with a MobType
 		 * @return The MobType with the given name (null if none are found)
 		 */
 		public static MobType fromString(String text) {
 			for (MobType m : MobType.values()) {
 				if (text.equalsIgnoreCase(m.name())) {
 					return m;
 				}
 			}
 			return null;
 		}
 		
 		/**
 		 * Just a string containing the possible subtypes. This is mainly
 		 * used for plugin help output.
 		 */
 		public static String subTypes = "player, baby, black, blue, brown, cyan, " +
 			"gray, green, lightblue, lime, magenta, orange, pink, purple, red, " +
 			"silver, white, yellow, sheared, charged, tiny, small, big, bigger, massive, godzilla, " +
 			"tamed, aggressive, tabby, tuxedo, siamese, burning, saddled, " +
 			"librarian, priest, blacksmith, butcher, nopickup";
 	}
 	
 	// Individual disguise stuff
 	/**
 	 * The entity ID that this disguise uses in its packets.
 	 */
 	public int entityID;
 	/**
 	 * The metadata contained in this disguise.
 	 */
 	public LinkedList<String> data; // $ means invisible player
 	/**
 	 * The type of mob this disguise is. (null if not a mob)
 	 */
 	public MobType mob; // null if player
 	public DataWatcher metadata;
 	
 	DCPacketGenerator packetGenerator;
 	
 	/**
 	 * Constructs a new Disguise object
 	 * @param entityID The entity ID of the disguise
 	 * @param data The metadata of the disguise (if a player, the name goes here) (null if there is no special data)
 	 * @param mob The type of mob the disguise is (null if player)
 	 */
 	public Disguise(int entityID, LinkedList<String> data, MobType mob) {
 		this.entityID = entityID;
 		this.data = data;
 		this.mob = mob;
 		
 		initializeData();
 		handleData();
 		
 		if (DisguiseCraft.protocolManager == null) {
 			packetGenerator = new DCPacketGenerator(this);
 		} else {
 			packetGenerator = new PLPacketGenerator(this);
 		}
 	}
 	
 	/**
 	 * Constructs a new Disguise object with a single data value
 	 * @param entityID The entity ID of the disguise
 	 * @param data The metadata of the disguise (if a player, the name goes here) (null if there is no special data)
 	 * @param mob The type of mob the disguise is (null if player)
 	 */
 	public Disguise(int entityID, String data, MobType mob) {
 		this.entityID = entityID;
 		LinkedList<String> dt = new LinkedList<String>();
 		dt.addFirst(data);
 		this.data = dt;
 		this.mob = mob;
 		
 		initializeData();
 		handleData();
 		
 		if (DisguiseCraft.protocolManager == null) {
 			packetGenerator = new DCPacketGenerator(this);
 		} else {
 			packetGenerator = new PLPacketGenerator(this);
 		}
 	}
 	
 	/**
 	 * Constructs a new Disguise object with null data
 	 * @param entityID The entity ID of the disguise
 	 * @param mob The type of mob the disguise is (null if player)
 	 */
 	public Disguise(int entityID, MobType mob) {
 		this.entityID = entityID;
 		this.data = null;
 		this.mob = mob;
 		
 		initializeData();
 		
 		if (DisguiseCraft.protocolManager == null) {
 			packetGenerator = new DCPacketGenerator(this);
 		} else {
 			packetGenerator = new PLPacketGenerator(this);
 		}
 	}
 	
 	/**
 	 * Set the entity ID
 	 * @param entityID The ID to set
 	 * @return The new Disguise object (for chaining)
 	 */
 	public Disguise setEntityID(int entityID) {
 		this.entityID = entityID;
 		return this;
 	}
 	
 	/**
 	 * Set the metadata
 	 * @param data The metadata to set
 	 * @return The new Disguise object (for chaining)
 	 */
 	public Disguise setData(LinkedList<String> data) {
 		this.data = data;
 		initializeData();
 		handleData();
 		return this;
 	}
 	
 	/**
 	 * Sets the metadata to a single value (Likely a player name)
 	 * @param data The metadata to set
 	 * @return The new Disguise object (for chaining)
 	 */
 	public Disguise setSingleData(String data) {
 		if (this.data == null) {
 			this.data = new LinkedList<String>();
 		}
 		this.data.clear();
 		this.data.addFirst(data);
 		metadata = new DataWatcher();
 		initializeData();
 		handleData();
 		return this;
 	}
 	
 	/**
 	 * Adds a single metadata string
 	 * @param data The metadata to add
 	 * @return The new Disguise object (for chaining)
 	 */
 	public Disguise addSingleData(String data) {
 		if (this.data == null) {
 			this.data = new LinkedList<String>();
 		}
 		if (!this.data.contains(data)) {
 			this.data.add(data);
 		}
 		initializeData();
 		handleData();
 		return this;
 	}
 	
 	/**
 	 * Set the mob type
 	 * @param mob
 	 * @return The new Disguise object (for chaining)
 	 */
 	public Disguise setMob(MobType mob) {
 		this.mob = mob;
 		return this;
 	}
 	
 	public void initializeData() { // everything is casted to Object because of method signature
 		metadata = new DataWatcher();
 		metadata.a(0, (Object) (byte) 0);
 		if (mob == MobType.Zombie || mob == MobType.PigZombie) {
 			metadata.a(12, (Object) (byte) 0);
 			metadata.a(13, (Object) (byte) 0);
 			metadata.a(14, (Object) (byte) 0);
 		} else {
 			metadata.a(12, (Object) 0);
 		}
 		if (mob == MobType.Sheep || mob == MobType.Pig || mob == MobType.Ghast || mob == MobType.Enderman || mob == MobType.Bat) {
 			metadata.a(16, (Object) (byte) 0);
 		} else if (mob == MobType.Slime || mob == MobType.MagmaCube) {
 			metadata.a(16, (Object) (byte) 3);
 		} else if (mob == MobType.Villager) {
 			metadata.a(16, (Object) 0);
 		} else if (mob == MobType.EnderDragon || mob == MobType.Wither) {
 			metadata.a(16, (Object) 100);
 		}
 		
 		if (mob == MobType.Creeper || mob == MobType.Enderman) {
 			metadata.a(17, (Object) (byte) 0);
 		}
 		if (mob == MobType.Ocelot) {
 			metadata.a(18, (Object) (byte) 0);
 		}
 		if (mob == MobType.Witch) {
 			metadata.a(21, (Object) (byte) 0);
 		}
 		if (mob == MobType.Wither) {
 			metadata.a(17, (Object) 0);
 			metadata.a(18, (Object) 0);
 			metadata.a(19, (Object) 0);
 			metadata.a(20, (Object) 0);
 		}
 	}
 	
 	public void handleData() {
 		if (data != null) {
 			// Index 0
 			byte firstIndex = 0;
 			if (data.contains("burning")) {
 				firstIndex = (byte) (firstIndex | 0x01);
 			}
 			if (data.contains("crouched")) {
 				firstIndex = (byte) (firstIndex | 0x02);
 			}
 			if (data.contains("riding")) {
 				firstIndex = (byte) (firstIndex | 0x04);
 			}
 			if (data.contains("sprinting")) {
 				firstIndex = (byte) (firstIndex | 0x08);
 			}
 			metadata.watch(0, firstIndex);
 			
 			// The other indexes
 			if (mob != null) {
 				if (data.contains("baby")) {
 					metadata.watch(12, -23999);
 				} else {
 					metadata.watch(12, 0);
 				}
 				
 				if (data.contains("black")) {
 					metadata.watch(16, (byte) 15);
 				} else if (data.contains("blue")) {
 					metadata.watch(16, (byte) 11);
 				} else if (data.contains("brown")) {
 					metadata.watch(16, (byte) 12);
 				} else if (data.contains("cyan")) {
 					metadata.watch(16, (byte) 9);
 				} else if (data.contains("gray")) {
 					metadata.watch(16, (byte) 7);
 				} else if (data.contains("green")) {
 					metadata.watch(16, (byte) 13);
 				} else if (data.contains("lightblue")) {
 					metadata.watch(16, (byte) 3);
 				} else if (data.contains("lime")) {
 					metadata.watch(16, (byte) 5);
 				} else if (data.contains("magenta")) {
 					metadata.watch(16, (byte) 2);
 				} else if (data.contains("orange")) {
 					metadata.watch(16, (byte) 1);
 				} else if (data.contains("pink")) {
 					metadata.watch(16, (byte) 6);
 				} else if (data.contains("purple")) {
 					metadata.watch(16, (byte) 10);
 				} else if (data.contains("red")) {
 					metadata.watch(16, (byte) 14);
 				} else if (data.contains("silver")) {
 					metadata.watch(16, (byte) 8);
 				} else if (data.contains("white")) {
 					metadata.watch(16, (byte) 0);
 				} else if (data.contains("yellow")) {
 					metadata.watch(16, (byte) 4);
 				} else if (data.contains("sheared")) {
 					metadata.watch(16, (byte) 16);
 				}
 				
 				if (data.contains("charged")) {
 					metadata.watch(17, (byte) 1);
 				}
 				
 				try {
 					if (data.contains("tiny")) {
 						metadata.watch(16, (byte) 1);
 					} else if (data.contains("small")) {
 						metadata.watch(16, (byte) 2);
 					} else if (data.contains("big")) {
 						metadata.watch(16, (byte) 4);
 					} else if (data.contains("bigger")) {
 						metadata.watch(16, (byte) DisguiseCraft.pluginSettings.biggerCube);
 					} else if (data.contains("massive")) {
 						metadata.watch(16, (byte) DisguiseCraft.pluginSettings.massiveCube);
 					} else if (data.contains("godzilla")) {
 						metadata.watch(16, (byte) DisguiseCraft.pluginSettings.godzillaCube);
 					}
 				} catch (Exception e) {
 					DisguiseCraft.logger.log(Level.WARNING, "Bad cube size values in configuration!", e);
 				}
 				
 				if (data.contains("sitting")) {
 					try {
 						metadata.a(16, (byte) 1);
 					} catch (IllegalArgumentException e) {
 						metadata.watch(16, (byte) 1);
 					}
 				} else if (data.contains("aggressive")) {
 					if (mob == MobType.Wolf) {
 						try {
 							metadata.a(16, (byte) 2);
 						} catch (IllegalArgumentException e) {
 							metadata.watch(16, (byte) 2);
 						}
 					} else if (mob == MobType.Ghast) {
 						metadata.watch(16, (byte) 1);
 					} else if (mob == MobType.Enderman) {
 						metadata.watch(17, (byte) 1);
 					}
 				} else if (data.contains("tamed")) {
 					try {
 						metadata.a(16, (byte) 4);
 					} catch (IllegalArgumentException e) {
 						metadata.watch(16, (byte) 4);
 					}
 				}
 				
 				if (data.contains("tabby")) {
 					metadata.watch(18, (byte) 2);
 				} else if (data.contains("tuxedo")) {
 					metadata.watch(18, (byte) 1);
 				} else if (data.contains("siamese")) {
 					metadata.watch(18, (byte) 3);
 				}
 				
 				if (data.contains("saddled")) {
 					metadata.watch(16, (byte) 1);
 				}
 				
 				Byte held = getHolding();
 				if (held != null) {
 					metadata.watch(16, held.byteValue());
 				}
 				
 				if (data.contains("farmer")) {
 					metadata.watch(16, 0);
 				} else if (data.contains("librarian")) {
 					metadata.watch(16, 1);
 				} else if (data.contains("priest")) {
 					metadata.watch(16, 2);
 				} else if (data.contains("blacksmith")) {
 					metadata.watch(16, 3);
 				} else if (data.contains("butcher")) {
 					metadata.watch(16, 4);
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Clone the Disguise object
 	 * @return A clone of this Disguise object
 	 */
 	public Disguise clone() {
 		return new Disguise(entityID, data, mob);
 	}
 	
 	/**
 	 * See if the disguises match
 	 * @param other The disguise to compare with
 	 * @return True if the disguises contain identical values
 	 */
 	public boolean equals(Disguise other) {
 		return (entityID == other.entityID && data.equals(other.data) && mob == other.mob);
 	}
 	
 	/**
 	 * Check if the disguise is of another player
 	 * @return true if it is a player disguise, false otherwise
 	 */
 	public boolean isPlayer() {
 		return (mob == null && !data.equals("$"));
 	}
 	
 	/**
 	 * Get the color of the disguise
 	 * @return The disguise color (null if no color)
 	 */
 	public String getColor() {
 		String[] colors = {"black", "blue", "brown", "cyan", "gray", "green",
 			"lightblue", "lime", "magenta", "orange", "pink", "purple", "red",
 			"silver", "white", "yellow", "sheared"};
 		if (data != null) {
 			for (String color : colors) {
 				if (data.contains(color)) {
 					return color;
 				}
 			}
 		}
 		return null;
 	}
 	
 	/**
 	 * Get the size of the disguise
 	 * @return The disguise size (null if no special size)
 	 */
 	public String getSize() {
 		String[] sizes = {"tiny", "small", "big"};
 		if (data != null) {
 			for (String size : sizes) {
 				if (data.contains(size)) {
 					return size;
 				}
 			}
 		}
 		return null;
 	}
 	
 	/**
 	 * Set whether or not the disguise is crouching
 	 * @param crouched True to make it crouch, False for standing
 	 */
 	public void setCrouch(boolean crouched) {
 		if (crouched) {
 			if (!data.contains("crouched")) {
 				data.add("crouched");
 			}
 		} else {
 			if (data.contains("crouched")) {
 				data.remove("crouched");
 			}
 		}
 		
 		// Index 0
 		byte firstIndex = 0;
 		if (data.contains("burning")) {
 			firstIndex = (byte) (firstIndex | 0x01);
 		}
 		if (data.contains("crouched")) {
 			firstIndex = (byte) (firstIndex | 0x02);
 		}
 		if (data.contains("riding")) {
 			firstIndex = (byte) (firstIndex | 0x04);
 		}
 		if (data.contains("sprinting")) {
 			firstIndex = (byte) (firstIndex | 0x08);
 		}
 		metadata.watch(0, firstIndex);
 	}
 	
 	/**
 	 * Gets the block ID this disguise is holding (according to the metadata)
 	 * @return The block ID of the held block (null if not holding anything)
 	 */
 	public Byte getHolding() {
 		if (data != null) {
 			for (String one : data) {
 				if (one.startsWith("holding")) {
 					String[] parts = one.split(":");
 					try {
 						return Byte.valueOf(parts[1]);
 					} catch (NumberFormatException e) {
 						DisguiseCraft.logger.log(Level.WARNING, "Could not parse the byte of an Enderman holding block!");
 					}
 				}
 			}
 		}
 		return null;
 	}
 	
 	/**
 	 * Checks if specified player has the permissions needed to wear this disguise
 	 * @param player The player to check the permissions of
 	 * @return Whether or not he has the permissions (true if yes)
 	 */
 	public boolean hasPermission(Player player) {
 		DisguiseCraft plugin = (DisguiseCraft) Bukkit.getServer().getPluginManager().getPlugin("DisguiseCraft");
 		if (data != null && data.contains("burning") && !plugin.hasPermissions(player, "disguisecraft.burning")) {
 			return false;
 		}
 		if (isPlayer()) { // Check Player
 			if (!plugin.hasPermissions(player, "disguisecraft.player." + data.getFirst())) {
 				return false;
 			}
 		} else { // Check Mob
 			if (!plugin.hasPermissions(player, "disguisecraft.mob." + mob.name().toLowerCase())) {
 				return false;
 			}
 			if (data != null) {
 				for (String dat : data) { // Check Subtypes
					if (dat.equalsIgnoreCase("crouched") || dat.equalsIgnoreCase("riding") || dat.equalsIgnoreCase("sprinting")) { // Ignore some statuses
 						continue;
 					}
 					if (dat.startsWith("holding")) { // Check Holding Block
 						if (!plugin.hasPermissions(player, "disguisecraft.mob.enderman.hold")) {
 							return false;
 						}
 						continue;
 					}
 					if (getSize() != null && dat.equals(getSize())) { // Check Size
 						if (!plugin.hasPermissions(player, "disguisecraft.mob." + mob.name().toLowerCase() + ".size." + dat)) {
 							return false;
 						}
 						continue;
 					}
 					if (getColor() != null && dat.equals(getColor())) { // Check Color
 						if (!plugin.hasPermissions(player, "disguisecraft.mob." + mob.name().toLowerCase() + ".color." + dat)) {
 							return false;
 						}
 						continue;
 					}
 					if (dat.equalsIgnoreCase("tabby") || dat.equalsIgnoreCase("tuxedo") || dat.equalsIgnoreCase("siamese")) { // Check Cat
 						if (!plugin.hasPermissions(player, "disguisecraft.mob." + mob.name().toLowerCase() + ".cat." + dat)) {
 							return false;
 						}
 						continue;
 					}
 					if (dat.equalsIgnoreCase("librarian") || dat.equalsIgnoreCase("priest") || dat.equalsIgnoreCase("blacksmith") || dat.equalsIgnoreCase("butcher")) { // Check Occupation
 						if (!plugin.hasPermissions(player, "disguisecraft.mob." + mob.name().toLowerCase() + ".occupation." + dat)) {
 							return false;
 						}
 						continue;
 					}
 					if (!plugin.hasPermissions(player, "disguisecraft.mob." + mob.name().toLowerCase() + "." + dat)) {
 						return false;
 					}
 				}
 			}
 		}
 		return true;
 	}
 	
 	// Packet creation methods
 	public Packet24MobSpawn getMobSpawnPacket(Location loc) {
 		return packetGenerator.getMobSpawnPacket(loc);
 	}
 	
 	public Packet20NamedEntitySpawn getPlayerSpawnPacket(Location loc, short item) {
 		return packetGenerator.getPlayerSpawnPacket(loc, item);
 	}
 	
 	public Packet29DestroyEntity getEntityDestroyPacket() {
 		return packetGenerator.getEntityDestroyPacket();
 	}
 	
 	public Packet5EntityEquipment getEquipmentChangePacket(short slot, ItemStack item) {
 		return packetGenerator.getEquipmentChangePacket(slot, item);
 	}
 	
 	public Packet32EntityLook getEntityLookPacket(Location loc) {
 		return packetGenerator.getEntityLookPacket(loc);
 	}
 	
 	public Packet33RelEntityMoveLook getEntityMoveLookPacket(Location look) {
 		return packetGenerator.getEntityMoveLookPacket(look);
 	}
 	
 	public Packet34EntityTeleport getEntityTeleportPacket(Location loc) {
 		return packetGenerator.getEntityTeleportPacket(loc);
 	}
 	
 	public Packet40EntityMetadata getEntityMetadataPacket() {
 		return packetGenerator.getEntityMetadataPacket();
 	}
 	
 	public Packet201PlayerInfo getPlayerInfoPacket(Player player, boolean show) {
 		return packetGenerator.getPlayerInfoPacket(player, show);
 	}
 	
 	public MovementValues getMovement(Location to) {
 		return packetGenerator.getMovement(to);
 	}
 	
 	public Packet35EntityHeadRotation getHeadRotatePacket(Location loc) {
 		return packetGenerator.getHeadRotatePacket(loc);
 	}
 	
 	public Packet18ArmAnimation getAnimationPacket(int animation) {
 		return packetGenerator.getAnimationPacket(animation);
 	}
 	
 	public Packet38EntityStatus getStatusPacket(int status) {
 		return packetGenerator.getStatusPacket(status);
 	}
 }
