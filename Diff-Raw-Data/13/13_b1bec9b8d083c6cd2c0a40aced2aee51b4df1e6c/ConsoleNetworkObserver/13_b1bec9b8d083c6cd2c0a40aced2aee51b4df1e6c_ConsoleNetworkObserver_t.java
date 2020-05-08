 package client;
 
 import java.net.SocketAddress;
 import common.OffsetConstants;
 import common.ServerPacketType;
 import common.Util;
 import common.network.PacketObserver;
 
 public class ConsoleNetworkObserver implements PacketObserver {
 
 	public boolean packetReceived(byte[] data, SocketAddress saddr) {
 		final byte ptype = Util.packetType(data);
 		
 		if (ptype == ServerPacketType.INITIALIZER) {
 			final String name = new String(data, OffsetConstants.INITIALIZER_STRING_OFFSET, data.length-OffsetConstants.INITIALIZER_STRING_OFFSET);
 			final int id = Util.getInt(data, OffsetConstants.INITIALIZER_ID_OFFSET);
 			final long randSeed = Util.getLong(data, OffsetConstants.INITIALIZER_RANDOM_SEED_OFFSET);
 			System.out.println(	"Established contact with server. Got id: " + id + ". Got name: " + name + ". Got seed: " + randSeed);
 		} else if (ptype == ServerPacketType.PLAYER_LEFT) {
 			final int lid = Util.getInt(data, OffsetConstants.PLAYER_LEFT_ID_OFFSET);
 			System.out.println("Player Left. ID = " + lid);
 		} else if (ptype == ServerPacketType.PLAYER_JOINED) {
 			final int nid = Util.getInt(data, OffsetConstants.PLAYER_JOINED_ID_OFFSET);
 			final byte[] bt = new byte[data.length - OffsetConstants.PLAYER_JOINED_STRING_OFFSET];
 			System.arraycopy(data, OffsetConstants.PLAYER_JOINED_STRING_OFFSET, bt, 0, bt.length);
 			System.out.println("Player Joined. ID = " + nid + " Name = " + new String(bt));
 		} else if (ptype == ServerPacketType.PLAYERS_INFO) {
 			String s = "";
 			for (int i = 2; i < data.length;) {
				final byte namelen = data[i+8];
				final String name = new String(data, i+9, namelen);
				i += namelen + 9;
 				s += name + ", ";
 			}
 			System.out.println("Added players: " + s);
 		} else if (ptype == ServerPacketType.PLAYER_POSITIONS) {
 			for( int i = 10; i < data.length;){
 				//System.out.println(Util.getVector3f(data, i+4, new Vector3f()));
 				i += 44;
 			}
 		} else if (ptype == ServerPacketType.MESSAGE) {
 			System.out.println("Recieved msg: " + new String(data,OffsetConstants.MESSAGE_STRING_OFFSET, data.length-OffsetConstants.MESSAGE_STRING_OFFSET));
 		}
 		return false;
 	}
 
 }
