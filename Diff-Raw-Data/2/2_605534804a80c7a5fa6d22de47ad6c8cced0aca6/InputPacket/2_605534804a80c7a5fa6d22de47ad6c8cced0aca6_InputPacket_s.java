 package tpw_rules.connectedmachines.network;
 
 
 import net.minecraft.tileentity.TileEntity;
 
 import java.io.ByteArrayInputStream;
 import java.io.DataInputStream;
 
 public class InputPacket extends Packet {
     public DataInputStream data;
 
     public PacketType type;
     public TileEntity tile;
 
     public InputPacket(byte[] data) {
         this.data = new DataInputStream(new ByteArrayInputStream(data));
        type =
     }
 }
