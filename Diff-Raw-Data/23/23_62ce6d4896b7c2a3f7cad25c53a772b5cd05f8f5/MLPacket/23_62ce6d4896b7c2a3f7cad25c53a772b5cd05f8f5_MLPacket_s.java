 package ml.core.network;
 
 import java.io.ByteArrayOutputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.lang.annotation.Retention;
 import java.lang.annotation.RetentionPolicy;
 import java.lang.reflect.Field;
 import java.util.ArrayList;
 import java.util.List;
 
 import ml.core.network.serializers.SForgeDirection;
 import ml.core.network.serializers.SItemsStack;
 import ml.core.network.serializers.SNBTTagCompound;
 import ml.core.network.serializers.SString;
 import net.minecraft.network.packet.Packet250CustomPayload;
 
 import com.google.common.io.ByteArrayDataInput;
 
 import cpw.mods.fml.common.network.Player;
 
 public abstract class MLPacket {
 	
 	public static final List<IDataSerializer> serializers = new ArrayList<IDataSerializer>();
 	static {
 		serializers.add(new SString());
 		serializers.add(new SNBTTagCompound());
 		serializers.add(new SItemsStack());
 		serializers.add(new SForgeDirection());
 	}
 	
 	@Retention(RetentionPolicy.RUNTIME)
 	public static @interface data {}
 	
 	protected Player player;
 	protected Integer packetID;
 	
 	// Incoming
 	
 	public MLPacket(Player pl, ByteArrayDataInput data) {
 		player = pl;
 		
 		loadData(data);
 	}
 	
 	public abstract void handleClientSide() throws IOException;
 	
 	public abstract void handleServerSide() throws IOException;
 	
 	// Outgoing
 	protected boolean chunkDataPacket = true;
 	protected String channel;
 	
 	public MLPacket(Player pl, String ch) {
 		player = pl;
 		
 		packetID = PacketHandler.findPacketId(this.getClass());
 		channel = ch;
 	}
 	
 	protected void loadData(ByteArrayDataInput dataIn) {
 		try {
 			for (Field fld : this.getClass().getFields()) {
 				data ann = fld.getAnnotation(data.class);
 				if (ann != null) {
 					Class cls = fld.getType();
 				
 					if (cls == int.class || cls == Integer.class)
						fld.setInt(this, dataIn.readInt());
 					else if (cls == boolean.class || cls == Boolean.class)
						fld.setBoolean(this, dataIn.readBoolean());
 					else if (cls == double.class || cls == Double.class)
						fld.setDouble(this, dataIn.readDouble());
 					else if (cls == byte.class || cls == Byte.class)
						fld.setByte(this, dataIn.readByte());
 					
 					else {
 						IDataSerializer slzr = null;
 						for (IDataSerializer IDS : serializers) {
 							if (IDS.handles(cls) && (slzr == null || slzr.getPriority()<IDS.getPriority()))
 									slzr = IDS;
 						}
 						if (slzr != null) fld.set(this, slzr.deserialize(dataIn));
 					}
 				}
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public Packet250CustomPayload convertToPkt250() {
 		ByteArrayOutputStream dataOutRaw = new ByteArrayOutputStream();
 		DataOutputStream dataOut = new DataOutputStream(dataOutRaw);
 		
 		try {
 			dataOut.writeInt(packetID);
 			
 			for (Field fld : this.getClass().getFields()) {
 				data ann = fld.getAnnotation(data.class);
 				if (ann != null) {
 					Class cls = fld.getType();
 				
 					if (cls == int.class || cls == Integer.class)
						dataOut.writeInt(fld.getInt(this));
 					else if (cls == boolean.class || cls == Boolean.class)
						dataOut.writeBoolean(fld.getBoolean(this));
 					else if (cls == double.class || cls == Double.class)
						dataOut.writeDouble(fld.getDouble(this));
 					else if (cls == byte.class || cls == Byte.class)
						dataOut.writeByte(fld.getByte(this));
 					
 					else {
 						IDataSerializer slzr = null;
 						for (IDataSerializer IDS : serializers) {
 							if (IDS.handles(cls) && (slzr == null || slzr.getPriority()<IDS.getPriority()))
 									slzr = IDS;
 						}
 						if (slzr != null) slzr.serialize(fld.get(this), dataOut);
 					}
 				}
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		
 		Packet250CustomPayload mcPkt = new Packet250CustomPayload(channel, dataOutRaw.toByteArray());
 		mcPkt.isChunkDataPacket = chunkDataPacket;
 		return mcPkt;
 	}
 	
 }
