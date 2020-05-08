 package com.podts.towns.server.entity.living;
 
 import com.podts.towns.server.Server;
 import com.podts.towns.server.console.ServerCommandSender;
 import com.podts.towns.server.entity.OfflinePlayer;
 import com.podts.towns.server.networking.Packet;
 import com.podts.towns.server.networking.Stream;
 import com.podts.towns.server.networking.packets.ChunkPacket;
 import com.podts.towns.server.networking.packets.EntityUpdatePacket;
 import com.podts.towns.server.networking.packets.LoginPacket;
import com.podts.towns.server.networking.packets.PlayNowPacket;
 import com.podts.towns.server.threading.impl.PacketSender;
 import com.podts.towns.server.world.Location;
 import com.podts.towns.server.world.World;
 
 public class Player extends Human implements ServerCommandSender {
 	
 	private OfflinePlayer offliname;
 	private Stream stream;
 	
 	public OfflinePlayer getOffline() {
 		return offliname;
 	}
 	
 	public Stream getStream() {
 		return stream;
 	}
 	
 	public void sendPacket(Packet packet) {
 		packet.setStream(stream);
 		PacketSender.addPacket(packet);
 	}
 	
 	@Override
 	public void sendMessage() {
 		
 	}
 	
 	public Player(String name, Stream s, Location loc) {
 		
 		super(name, loc);
 		
 		stream = s;
 		
 		Server.getLogger().info("new player with id" + getID());
 		
 		LoginPacket p = new LoginPacket();
 		
 		p.setId(getID());
 		p.setReply((byte) 1);
 		
 		sendPacket(p);
 		
 		EntityUpdatePacket locpacket = new EntityUpdatePacket();
 		
 		locpacket.setEntity(this);
 		locpacket.setType(EntityUpdatePacket.NEWENTITY);
 		
 		sendPacket(locpacket);
 		
 		
 		int cx = loc.getChunk().getX();
 		int cy = loc.getChunk().getY();
 		
 		for (int y=-1; y<2; y++) {
 			
 			for (int x=-1; x<2; x++) {
 				
 				
 				ChunkPacket packet = new ChunkPacket();
 				packet.setChunk(World.getWorld().getChunk(cx+x,cy+y));
 				packet.setType(ChunkPacket.NEWCHUNK);
 				
 				sendPacket(packet);
 				
 				
 			}
 			
 		}
 		
		PlayNowPacket playnow = new PlayNowPacket();
		
		sendPacket(playnow);
		
		
 	}
 	
 }
