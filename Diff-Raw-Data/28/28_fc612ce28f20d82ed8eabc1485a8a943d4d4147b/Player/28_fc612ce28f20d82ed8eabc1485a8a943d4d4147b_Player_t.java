 package com.podts.towns.server.entity.living;
 
 import java.util.concurrent.ConcurrentHashMap;
 
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
 	
 	private ConcurrentHashMap<String,Player> seeableplayers = new ConcurrentHashMap<String,Player>();
 	
 	public ConcurrentHashMap<String,Player> getViewablePlayers() {
 		return seeableplayers;
 	}
 	
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
 	
 	public void handleChunkHop() {
 		
 		for (Player p : seeableplayers.values()) {
 			
 			if (!canSee(p)) {
 				
 				// Remove players from their sights.
 				EntityUpdatePacket packet = new EntityUpdatePacket();
 				packet.setType(EntityUpdatePacket.REMOVEENTITY);
 				packet.setEntity(p);
 				
 				sendPacket(packet);
 				
				packet = new EntityUpdatePacket();
				packet.setType(EntityUpdatePacket.REMOVEENTITY);
 				packet.setEntity(this);
 				
 				p.sendPacket(packet);
 				
				p.seeableplayers.remove(getName());
				seeableplayers.remove(p.getName());
 				
 			}
 			
 		}
 		
 		for (Player p : World.getWorld().getPlayers().values()) {
 			
 			if (p.getID() == getID())
 				continue;
 			
 			if (canSee(p)) {
 				
 				if (!seeableplayers.contains(p)) {
 					
 					// Both players should see each other now.
 					EntityUpdatePacket packet = new EntityUpdatePacket();
 					packet.setType(EntityUpdatePacket.NEWENTITY);
 					packet.setEntity(p);
 					
 					sendPacket(packet);
 					
					packet = new EntityUpdatePacket();
					
					packet.setType(EntityUpdatePacket.NEWENTITY);
 					packet.setEntity(this);
 					
 					p.sendPacket(packet);
 					
 					p.seeableplayers.put(getName(), this);
 					seeableplayers.put(p.getName(), p);
 					
 				}
 				
 			}
 			
 		}
 		
 	}
 	
 	public Player(String name, Stream s, Location loc) {
 		
 		super(name, loc);
 		
 		stream = s;
 		
 		World.getWorld().getPlayers().put(getName(), this);
 		
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
 		
		handleChunkHop();
 		
 	}
 	
 }
