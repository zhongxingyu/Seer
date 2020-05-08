 package pgDev.bukkit.DisguiseCraft.disguise;
 
 import java.util.LinkedList;
 import java.util.logging.Level;
 import java.util.Map;
 
 import org.bukkit.Bukkit;
 import org.bukkit.entity.Player;
 
 import pgDev.bukkit.DisguiseCraft.DisguiseCraft;
 import pgDev.bukkit.DisguiseCraft.packet.DCPacketGenerator;
 import pgDev.bukkit.DisguiseCraft.packet.PLPacketGenerator;
 
 import net.minecraft.server.DataWatcher;
 
 /**
  * This is the class for every disguise object. It contains
  * the functions for creating, editing, and sending disguises.
  * @author PG Dev Team (Devil Boy, Tux2)
  */
 public class Disguise {
 	
 	/**
 	 * The entity ID that this disguise uses in its packets
 	 */
 	public int entityID;
 	/**
 	 * The metadata contained in this disguise
 	 */
 	public LinkedList<String> data = new LinkedList<String>();
 	/**
 	 * The type of entity the disguise is
 	 */
 	public DisguiseType type;
 	public DataWatcher metadata;
 	
 	public DCPacketGenerator packetGenerator;
 	
 	/**
 	 * Constructs a new Disguise object
 	 * @param entityID The entity ID of the disguise
 	 * @param data The metadata of the disguise (if a player, the name goes here) (null if there is no special data)
 	 * @param type The type of entity the disguise is
 	 */
 	public Disguise(int entityID, LinkedList<String> data, DisguiseType type) {
 		this.entityID = entityID;
 		this.data = data;
 		this.type = type;
 		
 		sharedConstruct();
 	}
 	
 	/**
 	 * Constructs a new Disguise object with a single data value
 	 * @param entityID The entity ID of the disguise
 	 * @param data The metadata of the disguise (if a player, the name goes here) (null if there is no special data)
 	 * @param type The type of entity the disguise is
 	 */
 	public Disguise(int entityID, String data, DisguiseType type) {
 		this.entityID = entityID;
 		this.data.addFirst(data);
 		this.type = type;
 		
 		sharedConstruct();
 	}
 	
 	/**
 	 * Constructs a new Disguise object with null data
 	 * @param entityID The entity ID of the disguise
 	 * @param mob The type of mob the disguise is (null if player)
 	 */
 	public Disguise(int entityID, DisguiseType type) {
 		this.entityID = entityID;
 		this.type = type;
 		
 		sharedConstruct();
 	}
 	
 	protected void sharedConstruct() {
 		if (DisguiseCraft.protocolManager == null) {
 			packetGenerator = new DCPacketGenerator(this);
 		} else {
 			packetGenerator = new PLPacketGenerator(this);
 		}
 		
 		// Check for proper data
 		dataCheck();
 		
 		// Deal with data
 		if (!type.isObject()) {
 			initializeData();
 			handleData();
 		}
 	}
 	
 	protected void dataCheck() {
 		// Check for NoPickup default
 		if (DisguiseCraft.pluginSettings.nopickupDefault) {
 			data.add("nopickup");
 		}
 		
 		// Check for player name
 		if (type.isPlayer() && data.isEmpty()) {
 			data.add("Glaciem");
 		}
 		
 		// Check for block data
 		if (type == DisguiseType.FallingBlock) {
 			if (getBlockID() == null) {
 				data.add("blockID:19"); // In honor of my first plugin
 			}
 		}
 	}
 	
 	/**
 	 * Set the entity ID
 	 * @param entityID The ID to set
 	 * @return The Disguise object (for chaining)
 	 */
 	public Disguise setEntityID(int entityID) {
 		this.entityID = entityID;
 		return this;
 	}
 	
 	/**
 	 * Set the metadata
 	 * @param data The metadata to set
 	 * @return The Disguise object (for chaining)
 	 */
 	public Disguise setData(LinkedList<String> data) {
 		this.data = data;
 		if (!type.isObject()) {
 			initializeData();
 			handleData();
 		}
 		return this;
 	}
 	
 	/**
 	 * Sets the metadata to a single value (Likely a player name)
 	 * @param data The metadata to set
 	 * @return The Disguise object (for chaining)
 	 */
 	public Disguise setSingleData(String data) {
 		this.data.clear();
 		this.data.addFirst(data);
 		if (!type.isObject()) {
 			metadata = new DataWatcher();
 			initializeData();
 			handleData();
 		}
 		return this;
 	}
 	
 	/**
 	 * Adds a single metadata string
 	 * @param data The metadata to add
 	 * @return The Disguise object (for chaining)
 	 */
 	public Disguise addSingleData(String data) {
 		if (!this.data.contains(data)) {
 			this.data.add(data);
 		}
 		if (!type.isObject()) {
 			initializeData();
 			handleData();
 		}
 		return this;
 	}
 	
 	/**
 	 * Clears the metadata
 	 * @return The Disguise object (for chaining)
 	 */
 	public Disguise clearData() {
 		data.clear();
 		dataCheck();
 		return this;
 	}
 	
 	/**
 	 * Set the disguise type
 	 * @param mob
 	 * @return The new Disguise object (for chaining)
 	 */
 	public Disguise setType(DisguiseType type) {
 		this. type =  type;
 		initializeData();
 		return this;
 	}
 	
	public void initializeData() { // everything is casted to Object because of method signature
 		if (!type.isObject()) {
 			metadata = type.newMetadata();
			safeAddData(0, (Object) (byte) 0, false);
 			safeAddData(12, (Object) 0, false);
 		}
 	
 		/* Old Metadata System
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
 		}*/
 	}
 	
 	@SuppressWarnings("rawtypes")
 	public void safeAddData(int index, Object value, boolean forcemodify) {
 		try {
 			if (((Map) DisguiseType.mapField.get(metadata)).containsKey(index)) {
 				if (forcemodify) {
 					metadata.watch(index, value);
 				}
 			} else {
 				metadata.a(index, value);
 			}
 		} catch (IllegalArgumentException e) {
 			DisguiseCraft.logger.log(Level.SEVERE, "Something bad happened in a " + type.name() + " disguise!");
 		} catch (IllegalAccessException e) {
 			DisguiseCraft.logger.log(Level.SEVERE, "Could not access the metadata map for a " + type.name() + " disguise!");
 		}
 	}
 	
 	public void handleData() {
 		if (!data.isEmpty()) {
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
 				if (type == DisguiseType.Wolf) {
 					try {
 						metadata.a(16, (byte) 2);
 					} catch (IllegalArgumentException e) {
 						metadata.watch(16, (byte) 2);
 					}
 				} else if (type == DisguiseType.Ghast) {
 					metadata.watch(16, (byte) 1);
 				} else if (type == DisguiseType.Enderman) {
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
 			
 			Byte held = getBlockID();
 			if (held != null && type == DisguiseType.Enderman) {
 				metadata.watch(16, held.byteValue());
 				
 				Byte blockData = getBlockData();
 				if (blockData != null) {
 					safeAddData(17, blockData.byteValue(), true);
 				}
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
 	
 	/**
 	 * Clone the Disguise object
 	 * @return A clone of this Disguise object
 	 */
 	public Disguise clone() {
 		return new Disguise(entityID, data, type);
 	}
 	
 	/**
 	 * See if the disguises match
 	 * @param other The disguise to compare with
 	 * @return True if the disguises contain identical values
 	 */
 	public boolean equals(Disguise other) {
 		return (entityID == other.entityID && data.equals(other.data) && type == other.type);
 	}
 	
 	/**
 	 * Get the color of the disguise
 	 * @return The disguise color (null if no color)
 	 */
 	public String getColor() {
 		String[] colors = {"black", "blue", "brown", "cyan", "gray", "green",
 			"lightblue", "lime", "magenta", "orange", "pink", "purple", "red",
 			"silver", "white", "yellow", "sheared"};
 		if (!data.isEmpty()) {
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
 		if (!data.isEmpty()) {
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
 	 * Gets the block ID relevant to this disguise (stored within metadata)
 	 * @return The block ID (null if none found)
 	 */
 	public Byte getBlockID() {
 		if (!data.isEmpty()) {
 			for (String one : data) {
 				if (one.startsWith("blockID:")) {
 					String[] parts = one.split(":");
 					try {
 						return Byte.valueOf(parts[1]);
 					} catch (NumberFormatException e) {
 						DisguiseCraft.logger.log(Level.WARNING, "Could not parse the byte of a disguise's block!");
 					}
 				}
 			}
 		}
 		return null;
 	}
 	
 	/**
 	 * Gets any extra block data stored in the metadata list
 	 * @return The block data byte (null if there isn't any)
 	 */
 	public Byte getBlockData() {
 		if (!data.isEmpty()) {
 			for (String one : data) {
 				if (one.startsWith("blockData:")) {
 					String[] parts = one.split(":");
 					try {
 						return Byte.decode(parts[1]);
 					} catch (NumberFormatException e) {
 						DisguiseCraft.logger.log(Level.WARNING, "Could not decode the byte of a disguise's block data!");
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
 		if (data.contains("burning") && !plugin.hasPermissions(player, "disguisecraft.burning")) {
 			return false;
 		}
 		if (type.isPlayer()) { // Check Player
 			if (!plugin.hasPermissions(player, "disguisecraft.player." + data.getFirst())) {
 				return false;
 			}
 		} else if (type.isMob()) { // Check Mob
 			if (!plugin.hasPermissions(player, "disguisecraft.mob." + type.name().toLowerCase())) {
 				return false;
 			}
 			if (!data.isEmpty()) {
 				for (String dat : data) { // Check Subtypes
 					if (dat.equalsIgnoreCase("crouched") || dat.equalsIgnoreCase("riding") || dat.equalsIgnoreCase("sprinting") || dat.equalsIgnoreCase("nopickup")) { // Ignore some statuses
 						continue;
 					}
 					if (dat.startsWith("holding")) { // Check Holding Block
 						if (!plugin.hasPermissions(player, "disguisecraft.mob.enderman.hold")) {
 							return false;
 						}
 						continue;
 					}
 					if (getSize() != null && dat.equals(getSize())) { // Check Size
 						if (!plugin.hasPermissions(player, "disguisecraft.mob." + type.name().toLowerCase() + ".size." + dat)) {
 							return false;
 						}
 						continue;
 					}
 					if (getColor() != null && dat.equals(getColor())) { // Check Color
 						if (!plugin.hasPermissions(player, "disguisecraft.mob." + type.name().toLowerCase() + ".color." + dat)) {
 							return false;
 						}
 						continue;
 					}
 					if (dat.equalsIgnoreCase("tabby") || dat.equalsIgnoreCase("tuxedo") || dat.equalsIgnoreCase("siamese")) { // Check Cat
 						if (!plugin.hasPermissions(player, "disguisecraft.mob." + type.name().toLowerCase() + ".cat." + dat)) {
 							return false;
 						}
 						continue;
 					}
 					if (dat.equalsIgnoreCase("librarian") || dat.equalsIgnoreCase("priest") || dat.equalsIgnoreCase("blacksmith") || dat.equalsIgnoreCase("butcher")) { // Check Occupation
 						if (!plugin.hasPermissions(player, "disguisecraft.mob." + type.name().toLowerCase() + ".occupation." + dat)) {
 							return false;
 						}
 						continue;
 					}
 					if (!plugin.hasPermissions(player, "disguisecraft.mob." + type.name().toLowerCase() + "." + dat)) {
 						return false;
 					}
 				}
 			}
 		}
 		return true;
 	}
 }
