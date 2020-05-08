 package pgDev.bukkit.DisguiseCraft.packet;
 
 import java.util.LinkedList;
 
 import org.bukkit.Location;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 
 import pgDev.bukkit.DisguiseCraft.*;
 import pgDev.bukkit.DisguiseCraft.disguise.*;
 
 public class DCPacketGenerator {
 	final Disguise d;
 	
 	protected int encposX;
 	protected int encposY;
 	protected int encposZ;
 	protected boolean firstpos = true;
 	
 	public DCPacketGenerator(final Disguise disguise) {
 		d = disguise;
 	}
 	
 	// Vital packet methods
 	public Object getSpawnPacket(Player disguisee) {
 		if (d.type.isMob()) {
 			return getMobSpawnPacket(disguisee.getLocation());
 		} else if (d.type.isPlayer()) {
 			return getPlayerSpawnPacket(disguisee.getLocation(), (short) disguisee.getItemInHand().getTypeId());
 		} else {
 			return getObjectSpawnPacket(disguisee.getLocation());
 		}
 	}
 	
 	public LinkedList<Object> getArmorPackets(Player player) {
 		LinkedList<Object> packets = new LinkedList<Object>();
 		ItemStack[] armor = player.getInventory().getArmorContents();
 		for (byte i=0; i < armor.length; i++) {
 			packets.add(getEquipmentChangePacket((short) (i + 1), armor[i]));
 		}
 		return packets;
 	}
 	
 	// Individual packet generation methods
 	public int[] getLocationVariables(Location loc) {
 		int x = DynamicClassFunctions.mathHelperFloor(loc.getX() *32D);
 		int y = DynamicClassFunctions.mathHelperFloor(loc.getY() *32D);
 		int z = DynamicClassFunctions.mathHelperFloor(loc.getZ() *32D);
 		if(firstpos) {
 			encposX = x;
 			encposY = y;
 			encposZ = z;
 			firstpos = false;
 		}
 		return new int[] {x, y, z};
 	}
 	
 	public Object getMobSpawnPacket(Location loc) {
 		LinkedList<PacketField> values = new LinkedList<PacketField>();
 		int[] locVars = getLocationVariables(loc);
 		
 		byte yaw = DisguiseCraft.degreeToByte(loc.getYaw());
 		byte pitch = DisguiseCraft.degreeToByte(loc.getPitch());
 		if (d.type == DisguiseType.EnderDragon) { // Ender Dragon fix
 			yaw = (byte) (yaw - 128);
 		} else if (d.type == DisguiseType.Chicken) { // Chicken fix
 			pitch = (byte) (pitch * -1);
 		}
 		
 		values.add(new PacketField("a", d.entityID));
 		values.add(new PacketField("b", d.type.id));
 		values.add(new PacketField("c", locVars[0]));
 		values.add(new PacketField("d", locVars[1]));
 		values.add(new PacketField("e", locVars[2]));
 		values.add(new PacketField("i", yaw));
 		values.add(new PacketField("j", pitch));
 		values.add(new PacketField("k", yaw));
 		values.add(new PacketField("s", d.metadata, false));
 		
 		return DynamicClassFunctions.constructPacket("Packet24MobSpawn", values);
 	}
 	
 	public Object getPlayerSpawnPacket(Location loc, short item) {
 		LinkedList<PacketField> values = new LinkedList<PacketField>();
 		int[] locVars = getLocationVariables(loc);
 		
 		values.add(new PacketField("a", d.entityID));
 		values.add(new PacketField("b", d.data.getFirst()));
 		values.add(new PacketField("c", locVars[0]));
 		values.add(new PacketField("d", locVars[1]));
 		values.add(new PacketField("e", locVars[2]));
 		values.add(new PacketField("f", DisguiseCraft.degreeToByte(loc.getYaw())));
 		values.add(new PacketField("g", DisguiseCraft.degreeToByte(loc.getPitch())));
 		values.add(new PacketField("h", item));
 		values.add(new PacketField("i", d.metadata, false));
 		
         return DynamicClassFunctions.constructPacket("Packet20NamedEntitySpawn", values);
 	}
 	
 	public Object getObjectSpawnPacket(Location loc) {
 		LinkedList<PacketField> values = new LinkedList<PacketField>();
 		int[] locVars = getLocationVariables(loc);
 		
 		int data = 1;
 		
 		// Block specific
     	if (d.type.isBlock()) {
     		loc.setY(loc.getY() + 0.5);
     		
     		Byte blockID = d.getBlockID();
     		if (blockID != null) {
     			data = (int) blockID;
     			
     			Byte blockData = d.getBlockData();
     			if (blockData != null) {
     				data = data | (((int) blockData) << 0xC);
     			}
     		}
     	}
 		
 		values.add(new PacketField("a", d.entityID));
 		values.add(new PacketField("b", locVars[0]));
 		values.add(new PacketField("c", locVars[1]));
 		values.add(new PacketField("d", locVars[2]));
 		values.add(new PacketField("e", 0));
 		values.add(new PacketField("f", 0));
 		values.add(new PacketField("g", 0));
 		values.add(new PacketField("h", d.type.id));
 		values.add(new PacketField("i", data));
 		
 		return DynamicClassFunctions.constructPacket("Packet23VehicleSpawn", values);
 	}
 	
 	public Object getEntityDestroyPacket() {
 		LinkedList<PacketField> values = new LinkedList<PacketField>();
 		values.add(new PacketField("a", new int[] {d.entityID}));
 		return DynamicClassFunctions.constructPacket("Packet29DestroyEntity", values);
 	}
 	
 	public Object getEquipmentChangePacket(short slot, ItemStack item) {
 		return DynamicClassFunctions.constructEquipmentChangePacket(d.entityID, slot, item);
 	}
 	
 	public byte[] getYawPitch(Location loc) {
 		byte yaw = DisguiseCraft.degreeToByte(loc.getYaw());
 		byte pitch = DisguiseCraft.degreeToByte(loc.getPitch());
 		if (d.type == DisguiseType.EnderDragon) { // EnderDragon specific
 			yaw = (byte) (yaw - 128);
 		} else if (d.type == DisguiseType.Chicken) { // Chicken fix
 			pitch = (byte) (pitch * -1);
 		} else if (d.type.isVehicle()) { // Vehicle fix
 			yaw = (byte) (yaw - 64);
 		}
 		return new byte[] {yaw, pitch};
 	}
 	
 	public Object getEntityLookPacket(Location loc) {
 		LinkedList<PacketField> values = new LinkedList<PacketField>();
 		
 		byte[] yp = getYawPitch(loc);
 		
 		values.add(new PacketField("a", d.entityID).setSuper(1));
 		values.add(new PacketField("e", yp[0]).setSuper(1));
 		values.add(new PacketField("f", yp[1]).setSuper(1));
 		
 		return DynamicClassFunctions.constructPacket("Packet32EntityLook", values);
 	}
 	
 	public Object getEntityMoveLookPacket(Location loc) {
 		LinkedList<PacketField> values = new LinkedList<PacketField>();
 		
 		byte[] yp = getYawPitch(loc);
 		
 		MovementValues movement = getMovement(loc);
 		encposX += movement.x;
 		encposY += movement.y;
 		encposZ += movement.z;
 		
		values.add(new PacketField("a", d.entityID));
		values.add(new PacketField("b", (byte) movement.x));
		values.add(new PacketField("c", (byte) movement.y));
		values.add(new PacketField("d", (byte) movement.z));
		values.add(new PacketField("e", yp[0]));
		values.add(new PacketField("f", yp[1]));
 		
 		return DynamicClassFunctions.constructPacket("Packet33RelEntityMoveLook", values);
 	}
 	
 	public Object getEntityTeleportPacket(Location loc) {
 		LinkedList<PacketField> values = new LinkedList<PacketField>();
 		
 		byte[] yp = getYawPitch(loc);
 		
 		int x = (int) DynamicClassFunctions.mathHelperFloor(32D * loc.getX());
 		int y = (int) DynamicClassFunctions.mathHelperFloor(32D * loc.getY());
 		int z = (int) DynamicClassFunctions.mathHelperFloor(32D * loc.getZ());
 		
 		encposX = x;
 		encposY = y;
 		encposZ = z;
 		
 		values.add(new PacketField("a", d.entityID));
 		values.add(new PacketField("b", x));
 		values.add(new PacketField("c", y));
 		values.add(new PacketField("d", z));
 		values.add(new PacketField("e", yp[0]));
 		values.add(new PacketField("f", yp[1]));
 		
 		return DynamicClassFunctions.constructPacket("Packet34EntityTeleport", values);
 	}
 	
 	public Object getEntityMetadataPacket() {
 		return DynamicClassFunctions.constructMetadataPacket(d.entityID, d.metadata);
 	}
 	
 	public Object getPlayerInfoPacket(Player player, boolean show) {
 		if (d.type.isPlayer()) {
 			LinkedList<PacketField> values = new LinkedList<PacketField>();
 			
 			int ping;
 			if (show) {
 				ping = DynamicClassFunctions.getPlayerPing(player);
 			} else {
 				ping = 9999;
 			}
 			
 			values.add(new PacketField("a", d.data.getFirst()));
 			values.add(new PacketField("b", show));
 			values.add(new PacketField("c", ping));
 			
 			return DynamicClassFunctions.constructPacket("Packet201PlayerInfo", values);
 		} else {
 			return null;
 		}
 	}
 	
 	public MovementValues getMovement(Location to) {
 		int x = DynamicClassFunctions.mathHelperFloor(to.getX() *32D);
 		int y = DynamicClassFunctions.mathHelperFloor(to.getY() *32D);
 		int z = DynamicClassFunctions.mathHelperFloor(to.getZ() *32D);
 		int diffx = x - encposX;
 		int diffy = y - encposY;
 		int diffz = z - encposZ;
 		return new MovementValues(diffx, diffy, diffz, DisguiseCraft.degreeToByte(to.getYaw()), DisguiseCraft.degreeToByte(to.getPitch()));
 	}
 	
 	public Object getHeadRotatePacket(Location loc) {
 		LinkedList<PacketField> values = new LinkedList<PacketField>();
 		
 		values.add(new PacketField("a", d.entityID));
 		values.add(new PacketField("b", DisguiseCraft.degreeToByte(loc.getYaw())));
 		
 		return DynamicClassFunctions.constructPacket("Packet35EntityHeadRotation", values);
 	}
 	
 	public Object getAnimationPacket(int animation) {
 		LinkedList<PacketField> values = new LinkedList<PacketField>();
 		
 		values.add(new PacketField("a", d.entityID));
 		
 		// 1 - Swing arm
 		// 2 Damage animation
 		// 5 Eat food
 		values.add(new PacketField("b", (byte) animation));
 		
 		return DynamicClassFunctions.constructPacket("Packet18ArmAnimation", values);
 	}
 	
 	public Object getStatusPacket(int status) {
 		LinkedList<PacketField> values = new LinkedList<PacketField>();
 		
 		values.add(new PacketField("a", d.entityID));
 		
 		// 2 - entity hurt
 		// 3 - entity dead
 		// 6 - wolf taming
 		// 7 - wolf tamed
 		// 8 - wolf shaking water
 		// 10 - sheep eating grass
 		values.add(new PacketField("b", (byte) status));
 		
 		return DynamicClassFunctions.constructPacket("Packet38EntityStatus", values);
 	}
 
 	public Object getPickupPacket(int item) {
 		LinkedList<PacketField> values = new LinkedList<PacketField>();
 		
 		values.add(new PacketField("a", item));
 		values.add(new PacketField("b", d.entityID));
 		
 		return DynamicClassFunctions.constructPacket("Packet22Collect", values);
 	}
 	
 	public Object getVelocityPacket(int x, int y, int z) {
 		LinkedList<PacketField> values = new LinkedList<PacketField>();
 		
 		values.add(new PacketField("a", d.entityID));
 		values.add(new PacketField("b", x));
 		values.add(new PacketField("c", y));
 		values.add(new PacketField("d", z));
 		
 		return DynamicClassFunctions.constructPacket("Packet28EntityVelocity", values);
 	}
 }
