 package assets.pchan3;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 
 import net.minecraft.entity.Entity;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.network.INetworkManager;
 import net.minecraft.network.packet.Packet;
 import net.minecraft.network.packet.Packet250CustomPayload;
 import assets.pchan3.steamship.EntityAirship;
 import cpw.mods.fml.common.network.IPacketHandler;
 import cpw.mods.fml.common.network.Player;
 
 /**
  *
  * @author pchan3
  */
 public class PacketHandler implements IPacketHandler
 {
 	public static String CHANNEL = "Steamship";
     @Override
     public void onPacketData(INetworkManager manager, Packet250CustomPayload payload, Player player)
     {
     	if (payload.channel.equals(CHANNEL)) {
             this.handle(payload,player);
     }
     }
 
 	private void handle(Packet250CustomPayload payload, Player player) {
 		DataInputStream inStream = new DataInputStream(new ByteArrayInputStream(payload.data));
 		short data;
 		try {
            data = inStream.readShort();
 		} catch (IOException e) {
             e.printStackTrace();
             return;
 		}
 		Entity ent = ((EntityPlayer)player).ridingEntity;
 		if (ent!=null && ent instanceof EntityAirship){	
 			switch(data){
			case 0: ((EntityPlayer)player).openGui(PChan3Mods.instance, PChan3Mods.instance.GUI_ID, ent.worldObj, (int)ent.posX, (int)ent.posY, (int)ent.posZ);break;
 			case 1: ((EntityAirship)ent).isGoingUp=true;break;
 			case 2: ((EntityAirship)ent).isGoingDown=true;break;			
 			case 3: ((EntityAirship)ent).isFiring=true;break;	
 			case 4: ((EntityAirship)ent).isGoingUp=false;break;
 			case 5: ((EntityAirship)ent).isGoingDown=false;break;
 			case 6: ((EntityAirship)ent).isFiring=false;break;
 			}		
 		}
 	}
 	
 	public static Packet getPacket(int i) {
 		ByteArrayOutputStream bos = new ByteArrayOutputStream(2);
 		DataOutputStream outputStream = new DataOutputStream(bos);
 		try {
 			outputStream.writeShort(i);
 		} catch (Exception ex) {
 			ex.printStackTrace();
 		}
 		Packet250CustomPayload packet = new Packet250CustomPayload();
 		packet.channel = CHANNEL;
 		packet.data = bos.toByteArray();
 		packet.length = bos.size();
 		return packet;
 	}
 }
