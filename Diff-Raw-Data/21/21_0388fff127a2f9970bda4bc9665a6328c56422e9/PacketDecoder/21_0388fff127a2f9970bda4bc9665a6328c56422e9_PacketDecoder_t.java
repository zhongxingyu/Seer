 package server;
 
 import java.net.SocketAddress;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import common.ClientPacketType;
 import common.Util;
 import common.network.AbstractPacketObserver;
 import common.network.PacketObserver;
 
 public class PacketDecoder extends AbstractPacketObserver {
 
 	private GameAdministration gadm;
 	private Game game;
 	
 	public PacketDecoder(GameAdministration gadm, Game game) {
 		this.gadm = gadm;
 		this.game = game;
 	}
 	
 	public boolean packetReceived(byte[] data, SocketAddress sender) {
 		byte ptype = packetType(data);
 		
 		switch (ptype) {
 			case ClientPacketType.LEAVING:
 				gadm.leaving(sender);
 				break;
 			case ClientPacketType.JOINING:
				String name = Util.getString(data, 2);
 				gadm.newConnection(name, sender);
 				break;
 			default:
 				return false;
 		}
 		return true;
 	}
 }
