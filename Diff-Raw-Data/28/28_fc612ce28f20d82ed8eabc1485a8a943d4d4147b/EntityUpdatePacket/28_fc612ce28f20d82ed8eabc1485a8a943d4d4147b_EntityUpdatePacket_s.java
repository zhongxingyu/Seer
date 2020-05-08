 package com.podts.towns.server.networking.packets;
 
import com.podts.towns.server.Server;
 import com.podts.towns.server.entity.MapObject;
 import com.podts.towns.server.entity.living.Player;
 import com.podts.towns.server.networking.Packet;
 import com.podts.towns.server.networking.PacketType;
 import com.podts.towns.server.world.Chunk;
 import com.podts.towns.server.world.World;
 
 public class EntityUpdatePacket extends Packet {
 	
 	public static final byte NEWENTITY = 1, REMOVEENTITY = 2, NEWPOSITION = 3, NEWROTATION = 4;
 	
 	private MapObject entity;
 	private byte type;
 	
 	public MapObject getEntity() {
 		return entity;
 	}
 	
 	public void setEntity(MapObject entity) {
 		this.entity = entity;
 	}
 	
 	public byte getType() {
 		return type;
 	}
 	
 	public void setType(byte type) {
 		this.type = type;
 	}
 	
 	public EntityUpdatePacket() {
 		super(PacketType.ENTITY_UPDATE.getOpCode());
 	}
 	
 	@Override
 	public void handle() {
 		
 		// Player sent packet that server is interpreting.
 		
 		type = getPayLoad().readByte();
 		
 		MapObject entity = null;
 		Player player = null;
 		boolean chunkhop = false;
 		
 		if (type == NEWPOSITION) {
 			
 			entity = World.getWorld().getEntity(getPayLoad().readInt());
 			
 			if (entity instanceof Player) {
 				
 				player = (Player) entity;
 				double x = getPayLoad().readDouble();
 				double y = getPayLoad().readDouble();
 				int rot = getPayLoad().readInt();
 				Chunk oldchunk = entity.getChunk();
 				entity.getLocation().setX(x);
 				entity.getLocation().setY(y);
 				entity.getLocation().setRotation(rot);
 				
 				// Handle new chunk
 				Chunk newchunk = entity.getChunk();
 				
 				// Player is in a different chunk.
 				if (!newchunk.equals(oldchunk)) {
 					
 					chunkhop = true;
 					
 					int diffx = newchunk.getX() - oldchunk.getX();
 					int diffy = newchunk.getY() - oldchunk.getY();
 					
 					if (diffx != 0) {
 						
 						for (int oy=-1; oy<2; oy++) {
 							
 							ChunkPacket op = new ChunkPacket();
 							op.setType(ChunkPacket.REMOVECHUNK);
 							op.setChunk(World.getWorld().getChunk(oldchunk.getX()+diffx*-1, oldchunk.getY()+oy));
 							player.sendPacket(op);
 							
 							op = new ChunkPacket();
 							op.setType(ChunkPacket.NEWCHUNK);
 							op.setChunk(World.getWorld().getChunk(newchunk.getX()+diffx, newchunk.getY()+oy));
 							player.sendPacket(op);
 							
 						}
 						
 					}
 					if (diffy != 0) {
 						
 						for (int ox=-1; ox<2; ox++) {
 							
 							ChunkPacket op = new ChunkPacket();
 							op.setType(ChunkPacket.REMOVECHUNK);
 							op.setChunk(World.getWorld().getChunk(oldchunk.getX()+ox, oldchunk.getY()+diffy*-1));
 							player.sendPacket(op);
 							
 							op = new ChunkPacket();
 							op.setType(ChunkPacket.NEWCHUNK);
 							op.setChunk(World.getWorld().getChunk(newchunk.getX()+ox, newchunk.getY()+diffy));
 							player.sendPacket(op);
 							
 						}
 						
 					}
 					
 				}
 				
 			}
 			
 		}
 		else if (type == NEWROTATION) {
 			
 			entity = World.getWorld().getEntity(getPayLoad().readInt());
 			
 			if (entity instanceof Player) {
 				
 				entity.getLocation().setRotation(getPayLoad().readInt());
 				
 			}
 			
 		}
 		
 		if (player != null) {
 			
 			for (Player p : player.getViewablePlayers().values()) {
 				
 				if (p.getID() == entity.getID())
 					continue;
 				
 				EntityUpdatePacket packet = new EntityUpdatePacket();
 				packet.setType(type);
 				packet.setEntity(player);
 				
				Server.getLogger().info("sending player update packet");
				
 				p.sendPacket(packet);
 				
 			}
 			
 			if (chunkhop)
 				player.handleChunkHop();
 			
 		}
 		
 	}
 
 	@Override
 	public void generatepayload() {
 		
 		// Packet that is going to be sent to other players.
 		
 		getPayLoad().writeByte(type);
 		
 		if (type == NEWENTITY) {
 			
 			getPayLoad().writeInt(entity.getID());
 			getPayLoad().writeDouble(entity.getLocation().getX());
 			getPayLoad().writeDouble(entity.getLocation().getY());
 			getPayLoad().writeInt(entity.getLocation().getRotation());
 			
 		}
 		else if (type == NEWPOSITION) {
 			
 			getPayLoad().writeInt(entity.getID());
 			getPayLoad().writeDouble(entity.getLocation().getX());
 			getPayLoad().writeDouble(entity.getLocation().getY());
 			getPayLoad().writeInt(entity.getLocation().getRotation());
 			
 		}
 		else if (type == NEWROTATION) {
 			
 			getPayLoad().writeInt(entity.getID());
 			getPayLoad().writeInt(entity.getLocation().getRotation());
 			
 		}
 		else if (type == REMOVEENTITY) {
 			
 			getPayLoad().writeInt(entity.getID());
 			
 		}
 		
 	}
 
 }
