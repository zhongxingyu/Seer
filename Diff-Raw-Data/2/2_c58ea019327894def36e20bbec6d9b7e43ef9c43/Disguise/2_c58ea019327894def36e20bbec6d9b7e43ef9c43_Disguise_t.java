 package pgDev.bukkit.DisguiseCraft;
 
 import java.lang.reflect.Field;
 import java.util.LinkedList;
 
 import org.bukkit.Location;
 import org.bukkit.craftbukkit.entity.CraftPlayer;
 import org.bukkit.craftbukkit.inventory.CraftItemStack;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 
 import net.minecraft.server.DataWatcher;
 import net.minecraft.server.MathHelper;
 import net.minecraft.server.Packet201PlayerInfo;
 import net.minecraft.server.Packet20NamedEntitySpawn;
 import net.minecraft.server.Packet24MobSpawn;
 import net.minecraft.server.Packet29DestroyEntity;
 import net.minecraft.server.Packet32EntityLook;
 import net.minecraft.server.Packet33RelEntityMoveLook;
 import net.minecraft.server.Packet34EntityTeleport;
 import net.minecraft.server.Packet35EntityHeadRotation;
 import net.minecraft.server.Packet40EntityMetadata;
 import net.minecraft.server.Packet5EntityEquipment;
 
 public class Disguise {
 	// MobType Enum
 	public enum MobType {
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
 		Wolf(95),
 		Zombie(54);
 		
 		public final byte id;
 		MobType(int i) {
 			id = (byte) i;
 		}
 		
 		/**
 		 * Check if the mob type is a subclass of an Entity class from Bukkit
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
 		
 		public static String subTypes = "player, baby";
 	}
 	
 	// Individual disguise stuff
 	public int entityID;
 	public LinkedList<String> data; // $ means invisible player
 	public MobType mob; // null if player
 	DataWatcher metadata = new DataWatcher();
 	private double lastVectorX;
 	private double lastVectorY;
 	private double lastVectorZ;
 	
 	private double lastposX;
 	private double lastposY;
 	private double lastposZ;
 	
 	private int encposX;
 	private int encposY;
 	private int encposZ;
 	
 	private boolean firstpos = true;
 	
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
 	
 	public void initializeData() {
 		metadata.a(12, 0);
 	}
 	
 	public void handleData() {
 		if (mob != null) {
 			if (data != null) {
 				if (data.contains("baby")) {
 					metadata.watch(12, -23999);
 				} else {
 					metadata.watch(12, 0);
 				}
 			} else {
 				metadata.watch(12, 0);
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
 	 * @return
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
 	
 	// Packet creation methods
 	public Packet24MobSpawn getMobSpawnPacket(Location loc) {
 		if (mob != null) {
 			int x = MathHelper.floor(loc.getX() *32D);
 			int y = MathHelper.floor(loc.getY() *32D);
 			int z = MathHelper.floor(loc.getZ() *32D);
 			if(firstpos) {
 				encposX = x;
 				encposY = y;
 				encposZ = z;
 				firstpos = false;
 			}
 			Packet24MobSpawn packet = new Packet24MobSpawn();
 			packet.a = entityID;
 			packet.b = mob.id;
 			packet.c = (int) x;
 			packet.d = (int) y;
 			packet.e = (int) z;
 			packet.f = DisguiseCraft.degreeToByte(loc.getYaw());
 			packet.g = DisguiseCraft.degreeToByte(loc.getPitch());
 			packet.h = packet.f;
 			try {
 				Field metadataField = packet.getClass().getDeclaredField("i");
 				metadataField.setAccessible(true);
 				metadataField.set(packet, metadata);
 			} catch (Exception e) {
 				System.out.println("DisguiseCraft was unable to set the metadata for a " + mob.name() +  " disguise!");
 				e.printStackTrace();
 			}
 			
 			// Ender Dragon fix
 			if (mob == MobType.EnderDragon) {
 				packet.f = (byte) (packet.f - 128);
 			}
 			// Chicken fix
 			if (mob == MobType.Chicken) {
 				packet.g = (byte) (packet.g * -1);
 			}
 			return packet;
 		} else {
 			return null;
 		}
 	}
 	
 	public Packet20NamedEntitySpawn getPlayerSpawnPacket(Location loc, short item) {
 		if (mob == null && !data.equals("$")) {
 			Packet20NamedEntitySpawn packet = new Packet20NamedEntitySpawn();
 	        packet.a = entityID;
 	        packet.b = data.getFirst();
 	        int x = MathHelper.floor(loc.getX() *32D);
 			int y = MathHelper.floor(loc.getY() *32D);
 			int z = MathHelper.floor(loc.getZ() *32D);
 			if(firstpos) {
 				encposX = x;
 				encposY = y;
 				encposZ = z;
 				firstpos = false;
 			}
 	        packet.c = (int) x;
 	        packet.d = (int) y;
 	        packet.e = (int) z;
 	        packet.f = DisguiseCraft.degreeToByte(loc.getYaw());
 	        packet.g = DisguiseCraft.degreeToByte(loc.getPitch());
 	        packet.h = item;
 	        return packet;
 		} else {
 			return null;
 		}
 	}
 	
 	public Packet29DestroyEntity getEntityDestroyPacket() {
 		return new Packet29DestroyEntity(entityID);
 	}
 	
 	public Packet5EntityEquipment getEquipmentChangePacket(short slot, ItemStack item) {
 		if (mob == null && !data.equals("$")) {
 			return new Packet5EntityEquipment(entityID, slot, ((CraftItemStack) item).getHandle());
 		} else {
 			return null;
 		}
 	}
 	
 	public Packet32EntityLook getEntityLookPacket(Location loc) {
 		Packet32EntityLook packet = new Packet32EntityLook();
 		packet.a = entityID;
 		packet.b = 0;
 		packet.c = 0;
 		packet.d = 0;
 		packet.e = DisguiseCraft.degreeToByte(loc.getYaw());
 		packet.f = DisguiseCraft.degreeToByte(loc.getPitch());
 		
 		// EnderDragon specific
 		if (mob == MobType.EnderDragon) {
 			packet.e = (byte) (packet.e - 128);
 		}
 		// Chicken fix
 		if (mob == MobType.Chicken) {
 			packet.f = (byte) (packet.f * -1);
 		}
 		return packet;
 	}
 	
 	public Packet33RelEntityMoveLook getEntityMoveLookPacket(Location look) {
 		Packet33RelEntityMoveLook packet = new Packet33RelEntityMoveLook();
 		packet.a = entityID;
 		MovementValues movement = getMovement(look);
 		encposX += movement.x;
 		encposY += movement.y;
 		encposZ += movement.z;
 		packet.b = (byte) movement.x;
 		packet.c = (byte) movement.y;
 		packet.d = (byte) movement.z;
 		packet.e = DisguiseCraft.degreeToByte(look.getYaw());
 		packet.f = DisguiseCraft.degreeToByte(look.getPitch());
 		
 		// EnderDragon specific
 		if (mob == MobType.EnderDragon) {
 			packet.e = (byte) (packet.e - 128);
 		}
 		// Chicken fix
 		if (mob == MobType.Chicken) {
 			packet.f = (byte) (packet.f * -1);
 		}
 		return packet;
 	}
 	
 	public Packet34EntityTeleport getEntityTeleportPacket(Location loc) {
 		Packet34EntityTeleport packet = new Packet34EntityTeleport();
 		packet.a = entityID;
 		int x = (int) MathHelper.floor(32D * loc.getX());
 		int y = (int) MathHelper.floor(32D * loc.getY());
 		int z = (int) MathHelper.floor(32D * loc.getZ());
 		packet.b = x;
 		packet.c = y;
 		packet.d = z;
 		encposX = x;
 		encposY = y;
 		encposZ = z;
 		packet.e = DisguiseCraft.degreeToByte(loc.getYaw());
 		packet.f = DisguiseCraft.degreeToByte(loc.getPitch());
 		
 		// EnderDragon specific
 		if (mob == MobType.EnderDragon) {
 			packet.e = (byte) (packet.e - 128);
 		}
 		// Chicken fix
 		if (mob == MobType.Chicken) {
 			packet.f = (byte) (packet.f * -1);
 		}
 		return packet;
 	}
 	
 	public Packet40EntityMetadata getEntityMetadataPacket() {
 		Packet40EntityMetadata packet = new Packet40EntityMetadata();
 		packet.a = entityID;
 		try {
 			Field metadataField = packet.getClass().getDeclaredField("b");
 			metadataField.setAccessible(true);
 			metadataField.set(packet, metadata);
 		} catch (Exception e) {
 			System.out.println("DisguiseCraft was unable to set the metadata for a " + mob.name() +  " disguise!");
 			e.printStackTrace();
 		}
 		return packet;
 	}
 	
 	public Packet201PlayerInfo getPlayerInfoPacket(Player player, boolean show) {
 		Packet201PlayerInfo packet = null;
 		if (isPlayer()) {
 			int ping;
 			if (show) {
 				ping = ((CraftPlayer) player).getHandle().ping;
 			} else {
 				ping = 9999;
 			}
 			packet = new Packet201PlayerInfo(data.getFirst(), show, ping);
 		}
 		return packet;
 	}
 	
 	public MovementValues getMovement(Location to) {
 		int x = MathHelper.floor(to.getX() *32D);
 		int y = MathHelper.floor(to.getY() *32D);
 		int z = MathHelper.floor(to.getZ() *32D);
 		int diffx = x - encposX;
 		int diffy = y - encposY;
 		int diffz = z - encposZ;
 		return new MovementValues(diffx, diffy, diffz, DisguiseCraft.degreeToByte(to.getYaw()), DisguiseCraft.degreeToByte(to.getPitch()));
 	}
 	
 	public Packet35EntityHeadRotation getHeadRotatePacket(Location loc) {
 		return new Packet35EntityHeadRotation(entityID, DisguiseCraft.degreeToByte(loc.getYaw()));
 	}
 }
