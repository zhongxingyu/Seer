 package net.mcforge.networking.packets.minecraft.extend;
 
 import java.io.IOException;
 
 import net.mcforge.API.ClassicExtension;
 import net.mcforge.iomodel.Player;
 import net.mcforge.networking.IOClient;
 import net.mcforge.networking.packets.PacketManager;
 import net.mcforge.networking.packets.PacketType;
 import net.mcforge.server.Server;
 
 @ClassicExtension(extName = "HoldThis")
 public class HoldThis extends ExtendPacket {
 
 	public HoldThis(String name, byte ID, PacketManager parent,
 			PacketType packetType) {
 		super(name, ID, parent, packetType);
 		parent.server.getPluginHandler().addExtension(this);
 	}
 	
 	public HoldThis(PacketManager parent) {
 		this("HoldThis", (byte)0x31, parent, PacketType.Client_to_Server);
 	}
 
 	@Override
 	public void WriteData(Player client, Server servers, Object... para) throws ExtensionNotSupportedException {
 		if (client.hasExtension(this)) {
 			byte[] array = new byte[3];
 			array[0] = ID;
 			array[1] = (Byte)para[0];
 			array[2] = (Byte)para[1];
 			try {
 				client.WriteData(array);
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		else
 			throw new ExtensionNotSupportedException(this);
 	}
 
 	@Override
	public void Handle(byte[] message, Server server, IOClient player) {
		
	}
 
 }
