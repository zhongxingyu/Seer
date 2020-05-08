 package net.buddat.mineserv.net.packet.handlers;
 
 import java.io.UnsupportedEncodingException;
 import java.nio.ByteBuffer;
 
 import org.apache.mina.core.session.IoSession;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import net.buddat.mineserv.MineServ;
 import net.buddat.mineserv.net.codec.MinecraftProtocolEncoder;
 import net.buddat.mineserv.net.packet.Packet;
 import net.buddat.mineserv.net.packet.PacketHandler;
 import net.buddat.mineserv.net.packet.PacketStore;
 import net.buddat.mineserv.plr.Player;
 import net.buddat.mineserv.plr.PlayerManager;
 
 public class LoginHandler implements PacketHandler {
 	
	private final Logger logger = LoggerFactory.getLogger(LoginHandler.class);
 
 	@Override
 	public void handlePacket(Packet p) {
 		IoSession session = p.getSession();
 		Player plr = (Player) session.getAttribute("PLAYER");
 		PlayerManager plrManager = MineServ.getInstance().getEngine().getPlayerManager();
 		
 		if (p.getPayload() != null) {
 			ByteBuffer buffer = ByteBuffer.wrap(p.getPayload());
 			
 			int protocolVersion = buffer.getInt();
 			
 			short stringLen = buffer.getShort();
 			byte[] username = new byte[stringLen * 2];
 			buffer.get(username);
 			
 			String playerName = "";
 			try {
 				playerName = new String(username, PacketStore.UTF16);
 			} catch (UnsupportedEncodingException e) {
 				e.printStackTrace();
 			}
 			
 			long mapSeed = buffer.getLong();
 			byte dimension = buffer.get();
 			
 			logger.info("Login: " + protocolVersion + "," + playerName + "," + mapSeed + "," + dimension);
 			
 			if (plrManager.isBanned(playerName) || plrManager.isIpBanned(plr.getSession().getRemoteAddress().toString()))
 				plrManager.disconnect(plr, "You are banned!");
 			
 			
 			
 		} else {
 			logger.error("Malformed login packet, disconnecting player: " + plr.getPlayerName());
 			plrManager.disconnect(plr);
 		}
 	}
 
 }
