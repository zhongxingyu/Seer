 package net.mcforge.networking.packets.minecraft.extend;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.nio.ByteBuffer;
 
 import net.mcforge.API.ClassicExtension;
 import net.mcforge.iomodel.Player;
 import net.mcforge.networking.IOClient;
 import net.mcforge.networking.packets.PacketManager;
 import net.mcforge.networking.packets.PacketType;
 import net.mcforge.server.Server;
 
 @ClassicExtension(extName = "ExtInfo")
 public class ExtInfoPacket extends ExtendPacket {
 
 	private static final String NAME = "MCForge";
 	public ExtInfoPacket(String name, byte ID, PacketManager parent,
 			PacketType packetType) {
 		super(name, ID, parent, packetType);
 	}
 	
 	public ExtInfoPacket(PacketManager parent) {
 		this("ExtInfo", (byte)0x10, parent, PacketType.Client_to_Server);
 	}
 
 	@Override
 	public void Handle(byte[] message, Server server, IOClient player) {
 		Player p = (player instanceof Player ? (Player)player : null);
 		if (p == null)
 			return;
 		byte[] name = new byte[64];
 		for (int i = 1; i < 64; i++)
 			name[i] = message[i];
 		String clientname = "Minecraft";
 		try {
 			clientname = new String(name, "US-ASCII");
 		} catch (UnsupportedEncodingException e) {
 			e.printStackTrace();
 		}
 		p.setClientName(clientname);
 	}
 
 	@Override
 	public void WriteData(Player p, Server servers, Object... para) {
 		String data = NAME;
 		while (data.length() < 64)
 			data += " ";
		ByteBuffer bf = ByteBuffer.allocate(67);
 		bf.put(ID);
 		try {
 			bf.put(data.getBytes("US-ASCII"));
 		} catch (UnsupportedEncodingException e) {
 			e.printStackTrace();
 		}
 		bf.putShort((short)servers.getPluginHandler().getExtensions().size());
 		try {
 			p.WriteData(bf.array());
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 }
 
