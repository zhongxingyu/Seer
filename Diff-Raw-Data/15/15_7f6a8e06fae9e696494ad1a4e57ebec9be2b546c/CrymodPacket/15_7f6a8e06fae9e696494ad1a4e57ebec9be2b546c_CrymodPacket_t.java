 package demonmodders.Crymod.Common.Network;
 
 import static demonmodders.Crymod.Common.Crymod.logger;
 
 import java.io.DataInput;
 import java.io.DataOutput;
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Map;
 
 import net.minecraft.src.EntityPlayer;
 import net.minecraft.src.Packet;
 import net.minecraft.src.Packet250CustomPayload;
 
 import com.google.common.io.ByteArrayDataInput;
 import com.google.common.io.ByteArrayDataOutput;
 import com.google.common.io.ByteStreams;
 
 import cpw.mods.fml.common.network.PacketDispatcher;
 import cpw.mods.fml.common.network.Player;
 import demonmodders.Crymod.Common.Crymod;
 
 public abstract class CrymodPacket {
 	
 	public static final String CHANNEL = "SummoningMod";
 	
 	private static Map<Integer, Class<? extends CrymodPacket>> idToClassMap = new HashMap<Integer, Class<? extends CrymodPacket>>();
 	private static Map<Class<? extends CrymodPacket>, Integer> classToIdMap = new HashMap<Class<? extends CrymodPacket>, Integer>();
 	
 	private static void addMapping(int packetId, Class<? extends CrymodPacket> clazz) {
 		idToClassMap.put(packetId, clazz);
 		classToIdMap.put(clazz, packetId);
 	}
 	
 	static {
 		addMapping(0, PacketPageChange.class);
 		addMapping(1, PacketPlayerKarma.class);
 	}
 	
 	public final Packet generatePacket() {
 		ByteArrayDataOutput output = ByteStreams.newDataOutput();
 		output.writeByte(classToIdMap.get(getClass()));
 		writeData(output);
 		byte[] bytes = output.toByteArray();
 		if (bytes.length <= Short.MAX_VALUE + 1) {
 			return PacketDispatcher.getTinyPacket(Crymod.instance, bytes[0], Arrays.copyOfRange(bytes, 1, bytes.length));
 		} else {
 			return new Packet250CustomPayload(CHANNEL, bytes);
 		}
 	}
 	
 	public final void sendToServer() {
 		PacketDispatcher.sendPacketToServer(generatePacket());
 	}
 	
 	public final void sendToPlayer(EntityPlayer player) {
 		PacketDispatcher.sendPacketToPlayer(generatePacket(), (Player)player);
 	}
 	
 	abstract void writeData(ByteArrayDataOutput out);
 	
 	abstract void readData(ByteArrayDataInput in);
 	
 	abstract void execute(EntityPlayer player);
 	
 	public static void execute(ByteArrayDataInput input, int packetId, EntityPlayer player) {
 		Class<? extends CrymodPacket> packetClass = idToClassMap.get(packetId);
 		if (packetClass == null) {
 			logger.warning("Recieved unknown Packet-Id " + packetId);
 		} else {
 			try {
 				CrymodPacket parsedPacket = packetClass.newInstance();
 				parsedPacket.readData(input);
 				parsedPacket.execute((EntityPlayer)player);
 			} catch (Exception e) {
 				logger.warning("Exception during packet handling: " + e.getClass().getSimpleName() + " (" + e.getMessage() + ")");
 			}
 		}
 	}
 }
