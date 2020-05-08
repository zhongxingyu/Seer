 package server;
 
 import java.util.Collection;
 
 import server.clientdb.Client;
 import server.clientdb.ClientDB;
 
 import common.OffsetConstants;
 import common.ServerPacketType;
 import common.Util;
 import common.world.Missile;
 import common.world.Ship;
 
 public class PacketDataFactory {
 
 	public static byte[] createMessagePacket(byte msgtype, String str) {
 
 		final byte[] sb = str.getBytes();
 
 		final byte[] b = new byte[OffsetConstants.MESSAGE_STRING_OFFSET	+ sb.length];
 
 		b[0] = ServerPacketType.MESSAGE;
 		b[1] = 0;
 		b[OffsetConstants.MESSAGE_MESSAGE_TYPE_OFFSET] = msgtype;
 		Util.put(sb, b, OffsetConstants.MESSAGE_STRING_OFFSET);
 
 		return b;
 	}
 
 	public static byte[] createInitializer(long worldseed, int id, int shipid, String name) {
 		final byte[] namebytes = name.getBytes();
 		final byte[] b = new byte[OffsetConstants.INITIALIZER_STRING_OFFSET	+ namebytes.length];
 
 		b[0] = ServerPacketType.INITIALIZER;
 		b[1] = 0;
 		Util.put(worldseed, b, OffsetConstants.INITIALIZER_RANDOM_SEED_OFFSET);
 		Util.put(id, b, OffsetConstants.INITIALIZER_ID_OFFSET);
 		Util.put(shipid, b, OffsetConstants.INITIALIZER_SHIPID_OFFSET);
 		Util.put(namebytes, b, OffsetConstants.INITIALIZER_STRING_OFFSET);
 
 		return b;
 	}
 
 	public static byte[] createPlayerJoined(int id, String name, int shipid) {
 		final byte[] sb = name.getBytes();
 		final byte[] b = new byte[OffsetConstants.PLAYER_JOINED_STRING_OFFSET
 				+ sb.length];
 
 		b[0] = ServerPacketType.PLAYER_JOINED;
 		b[1] = 0;
 		Util.put(id, b, OffsetConstants.PLAYER_JOINED_ID_OFFSET);
 		Util.put(shipid, b, OffsetConstants.PLAYER_JOINED_SHIPID_OFFSET);
 		Util.put(sb, b, OffsetConstants.PLAYER_JOINED_STRING_OFFSET);
 
 		return b;
 	}
 
 	public static byte[] createPlayerLeft(int id) {
 		final byte[] b = new byte[OffsetConstants.PLAYER_LEFT_SIZE];
 		b[0] = ServerPacketType.PLAYER_LEFT;
 		b[1] = 0;
 		Util.put(id, b, OffsetConstants.PLAYER_LEFT_ID_OFFSET);
 		return b;
 	}
 
 	public static byte[] createPosition(long time, Collection<Ship> ships) {
 		
 		//TODO: Check if the array will fit in a UDP Packet.
 		final byte[] b = new byte[ships.size() * 44  + 10];
 		
 		b[0] = ServerPacketType.PLAYER_POSITIONS;
 		b[1] = 0;
 		Util.put(time, b, OffsetConstants.PLAYER_POSITIONS_TICK_OFFSET);
 		
 		int offset = 10;
 		
 		for(final Ship s: ships) {
 			Util.put(s.getOwner().getID(), b, offset);
 			Util.put(s.getLocalTranslation(), b, offset+4);
 			Util.put(s.getLocalRotation(), b, offset+16);
 			Util.put(s.getMovement(), b, offset+32);
 			offset += 44;
 		}
 		
 		return b;
 	}
 
 	public static byte[] createPlayersInfo(ClientDB cdb, Client Player) {
 		int size = 2;
 		byte[][] byteNames;
 		int[] ids;
 		int[] shipids;
 		int i;
 
 		synchronized (cdb) {
 			Collection<Client> clients = cdb.getClients();
 			byteNames = new byte[clients.size()][];
 			ids = new int[clients.size()];
 			shipids = new int[clients.size()];
 			i = 0;
 			for (final Client c : clients) {
 				if (!c.equals(Player)) {
 					byteNames[i] = c.getName().getBytes();
 					ids[i] = c.getID();
 					shipids[i] = c.getShip().getID();
					size += 9 + byteNames[i].length;
 					i++;
 				}
 			}
 		}
 
 		final int numberOfClients = i;
 		final byte b[] = new byte[size];
 
 		b[0] = ServerPacketType.PLAYERS_INFO;
 		b[1] = 0;
 
 		int offset = 2;
 		for (int k = 0; k < numberOfClients; k++) {
 			Util.put(ids[k], b, offset);
 			Util.put(shipids[k], b, offset+4);
 			b[offset+8] = (byte) byteNames[k].length;
 			Util.put(byteNames[k], b, offset+9);
 			offset += 9+byteNames[k].length;
 		}
 
 		return b;
 	}
 	/**
 	 * Sent to all clients when a missile is created <br>
 	 * ServerPacketType - byte - 1 byte <br>
 	 * Sequence Number - byte - 1 byte <br>
 	 * Time of Creation - long - 8 bytes <br>
 	 * pos - Vector3f - 12 byte <br>
 	 * dir - Vector3f - 12 byte <br>
 	 */
 	public static byte[] createMissile(Missile m) {
 		
 		final byte[] b = new byte[OffsetConstants.MISSILE_SIZE];
 		
 		b[0] = ServerPacketType.MISSILE;
 		b[1] = 0;
 		Util.put(m.getLastUpdate(), b, OffsetConstants.MISSILE_TIME_OFFSET);
 		Util.put(m.getPosition(), b, OffsetConstants.MISSILE_POS_OFFSET);
 		Util.put(m.getMovement(), b, OffsetConstants.MISSILE_DIR_OFFSET);
 		Util.put(m.getID(), b, OffsetConstants.MISSILE_ID_OFFSET);
 		Util.put(m.getOwner().getID(), b, OffsetConstants.MISSILE_OWNER_OFFSET);
 					
 		return b;
 	}
 }
