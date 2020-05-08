 package chrisclark13.minecraft.artificing.network.packet;
 
 import java.io.IOException;
 import java.util.logging.Level;
 
 import com.google.common.io.ByteArrayDataInput;
 import com.google.common.io.ByteArrayDataOutput;
 import com.google.common.io.ByteStreams;
 
 import cpw.mods.fml.relauncher.Side;
 
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.network.INetworkManager;
 import net.minecraft.network.packet.Packet250CustomPayload;
 
 import chrisclark13.minecraft.artificing.core.helper.LogHelper;
 import chrisclark13.minecraft.artificing.lib.Reference;
 
 public abstract class PacketArtificing {
     public PacketType type;
     private boolean isChunkDataPacket;
     
     public PacketArtificing(PacketType type, boolean isChunkDataPacket) {
         this.type = type;
         this.isChunkDataPacket = isChunkDataPacket;
     }
     
     public final Packet250CustomPayload makePacket250() {
         ByteArrayDataOutput out = ByteStreams.newDataOutput();
         Packet250CustomPayload packet = new Packet250CustomPayload();
         
         try {
             out.writeByte(type.ordinal());
             this.writeData(out);
             
             packet.channel = Reference.CHANNEL;
             packet.data = out.toByteArray();
             packet.length = packet.data.length;
             packet.isChunkDataPacket = this.isChunkDataPacket;
         } catch (IOException exception) {
             LogHelper.log(Level.SEVERE, this.getClass().getSimpleName()
                     + " threw an IOException while trying to make a Packet250CustomPayload!");
             exception.printStackTrace(System.err);
         }
         
         return packet;
     }
     
     public static final PacketArtificing createPacket(byte[] data) {
         ByteArrayDataInput in = ByteStreams.newDataInput(data);
         PacketArtificing packet = null;
         
         try {
             packet = PacketType.values()[in.readUnsignedByte()].clazz.newInstance();
             packet.readData(in);
         } catch (Exception e) {
             LogHelper.log(Level.SEVERE, e.getClass().getSimpleName() + " occured while trying to read a packet!");
             e.printStackTrace(System.err);
         }
         
         
         return packet;
     }
     
     protected abstract void writeData(ByteArrayDataOutput out) throws IOException;
     
     protected abstract void readData(ByteArrayDataInput in) throws IOException;
     
     public abstract void execute(INetworkManager manager, EntityPlayer player, Side side);
 }
