 package pgDev.bukkit.DisguiseCraft.packet;
 
 import java.util.logging.Level;
 
 import net.minecraft.server.DataWatcher;
 import net.minecraft.server.MathHelper;
 import net.minecraft.server.Packet20NamedEntitySpawn;
 import net.minecraft.server.Packet24MobSpawn;
 import net.minecraft.server.Packet29DestroyEntity;
 
 import org.bukkit.Location;
 
 import com.comphenix.protocol.ProtocolManager;
 import com.comphenix.protocol.events.PacketContainer;
 import com.comphenix.protocol.reflect.FieldAccessException;
import com.comphenix.protocol.reflect.StructureModifier;
 
 import pgDev.bukkit.DisguiseCraft.Disguise;
 import pgDev.bukkit.DisguiseCraft.DisguiseCraft;
 import pgDev.bukkit.DisguiseCraft.Disguise.MobType;
 
 public class PLPacketGenerator extends DCPacketGenerator {
 	ProtocolManager pM = DisguiseCraft.protocolManager;
 
 	public PLPacketGenerator(Disguise disguise) {
 		super(disguise);
 	}
 	
 	// Packet creation methods
 	@Override
 	public Packet24MobSpawn getMobSpawnPacket(Location loc) {
 		if (d.mob != null) {
 			// Make values
 			int x = MathHelper.floor(loc.getX() *32D);
 			int y = MathHelper.floor(loc.getY() *32D);
 			int z = MathHelper.floor(loc.getZ() *32D);
 			if(firstpos) {
 				encposX = x;
 				encposY = y;
 				encposZ = z;
 				firstpos = false;
 			}
 			int eID = d.entityID;
 			int mobID = d.mob.id;
 			int xPos = (int) x;
 			int yPos = (int) y;
 			int zPos = (int) z;
 			byte bodyYaw = DisguiseCraft.degreeToByte(loc.getYaw());
 			byte headPitch = DisguiseCraft.degreeToByte(loc.getPitch());
 			if (d.mob == MobType.EnderDragon) { // Ender Dragon fix
 				bodyYaw = (byte) (bodyYaw - 128);
 			}
 			if (d.mob == MobType.Chicken) { // Chicken fix
 				headPitch = (byte) (headPitch * -1);
 			}
 			byte headYaw = bodyYaw;
 			
 			// Make packet
 			PacketContainer pC = pM.createPacket(24);
 			try {
 				pC.getSpecificModifier(int.class).
 					write(0, eID).
 					write(1, mobID).
 					write(2, xPos).
 					write(3, yPos).
 					write(4, zPos);
 			} catch (FieldAccessException e) {
 				DisguiseCraft.logger.log(Level.SEVERE, "PL: Unable to modify the integers for a " + d.mob.name() +  " disguise!", e);
 			}
 			try {
 				pC.getSpecificModifier(byte.class).
 					write(0, bodyYaw).
 					write(1, headPitch).
 					write(2, headYaw);
 			} catch (FieldAccessException e) {
 				DisguiseCraft.logger.log(Level.SEVERE, "PL: Unable to modify the bytes for a " + d.mob.name() +  " disguise!", e);
 			}
 			try {
 				pC.getSpecificModifier(DataWatcher.class).
 					write(0, d.metadata);
 			} catch (FieldAccessException e) {
 				DisguiseCraft.logger.log(Level.SEVERE, "PL: Unable to modify the metadata for a " + d.mob.name() +  " disguise!", e);
 			}
 			return (Packet24MobSpawn) pC.getHandle();
 		} else {
 			return null;
 		}
 	}
 	
 	@Override
 	public Packet20NamedEntitySpawn getPlayerSpawnPacket(Location loc, short item) {
 		if (d.mob == null && d.data != null) {
 			// Make Values
 	        int x = MathHelper.floor(loc.getX() *32D);
 			int y = MathHelper.floor(loc.getY() *32D);
 			int z = MathHelper.floor(loc.getZ() *32D);
 			if(firstpos) {
 				encposX = x;
 				encposY = y;
 				encposZ = z;
 				firstpos = false;
 			}
 			int eID = d.entityID;
 	        String name = d.data.getFirst();
 	        int xPos = (int) x;
 	        int yPos = (int) y;
 	        int zPos = (int) z;
 	        byte yaw = DisguiseCraft.degreeToByte(loc.getYaw());
 	        byte pitch = DisguiseCraft.degreeToByte(loc.getPitch());
 	        
 	        // Make Packet
 	        PacketContainer pC = pM.createPacket(20);
 			try {
 				pC.getSpecificModifier(int.class).
 					write(0, eID).
 					write(1, xPos).
 					write(2, yPos).
 					write(3, zPos).
 					write(4, (int) item);
 			} catch (FieldAccessException e) {
 				DisguiseCraft.logger.log(Level.SEVERE, "PL: Unable to modify the integers for a player disguise!", e);
 			}
 			try {
 				pC.getSpecificModifier(String.class).
 					write(0, name);
 			} catch (FieldAccessException e) {
 				DisguiseCraft.logger.log(Level.SEVERE, "PL: Unable to modify the name for a player disguise!", e);
 			}
 			try {
 				pC.getSpecificModifier(byte.class).
 					write(0, yaw).
 					write(1, pitch);
 			} catch (FieldAccessException e) {
 				DisguiseCraft.logger.log(Level.SEVERE, "PL: Unable to modify the bytes for a player disguise!", e);
 			}
 			try {
 				pC.getSpecificModifier(DataWatcher.class).
 					write(0, d.metadata);
 			} catch (FieldAccessException e) {
 				DisguiseCraft.logger.log(Level.SEVERE, "PL: Unable to modify the metadata for a player disguise!", e);
 			}
 	        return (Packet20NamedEntitySpawn) pC.getHandle();
 		} else {
 			return null;
 		}
 	}
 	
 	@Override
 	public Packet29DestroyEntity getEntityDestroyPacket() {
 		PacketContainer pC = pM.createPacket(29);
		StructureModifier<int[]> intPos = pC.getSpecificModifier(int[].class);
		if (intPos.size() > 0) {
			try {
				int[] intArray = {d.entityID};
				intPos.write(0, intArray);
			} catch (FieldAccessException e) {
				DisguiseCraft.logger.log(Level.SEVERE, "PL: Unable to modify the integer array for a destroy packet!", e);
			}
		} else {
			try {
				pC.getSpecificModifier(int.class)
					.write(0, d.entityID);
			} catch (FieldAccessException e) {
				DisguiseCraft.logger.log(Level.SEVERE, "PL: Unable to modify the integer for a destroy packet!", e);
			}
 		}
 		return (Packet29DestroyEntity) pC.getHandle();
 	}
 }
