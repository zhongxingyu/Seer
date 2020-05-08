 package net.buddat.mineserv.net;
 
 import java.util.ArrayList;
 
 import net.buddat.mineserv.Engine;
 import net.buddat.mineserv.MineServ;
 import net.buddat.mineserv.net.codec.MinecraftProtocolEncoder;
 import net.buddat.mineserv.net.packet.Packet;
 import net.buddat.mineserv.plr.Player;
 import net.buddat.mineserv.plr.PlayerManager;
 
 import org.apache.mina.core.service.IoHandler;
 import org.apache.mina.core.session.IdleStatus;
 import org.apache.mina.core.session.IoSession;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class ConnectionHandler implements IoHandler {
 	
	private final Logger logger = LoggerFactory.getLogger(ConnectionHandler.class);
 	
 	private Engine serverEngine;
 	private PlayerManager plrManager;
 	
 	private ArrayList<Packet> packetQueue;
 	
 	public ConnectionHandler() {
 		serverEngine = MineServ.getInstance().getEngine();
 		plrManager = serverEngine.getPlayerManager();
 		
 		packetQueue = serverEngine.getPacketQueue();
 	}
 
 	@Override
 	public void exceptionCaught(IoSession session, Throwable arg1)
 			throws Exception {
 		
 	}
 
 	@Override
 	public void messageReceived(IoSession session, Object msg) throws Exception {
 		Packet p = (Packet) msg;
 		
 		packetQueue.add(p);
 	}
 
 	@Override
 	public void messageSent(IoSession session, Object msg) throws Exception {
 		
 	}
 
 	@Override
 	public void sessionClosed(IoSession session) throws Exception {
 		Player p = plrManager.getPlayer(session);
 		
 		if (p != null) {
 			plrManager.removePlayer(p);
 		}
 	}
 
 	@Override
 	public void sessionCreated(IoSession session) throws Exception {		
 		Player newPlayer = new Player(session);
 		plrManager.addPlayer(newPlayer);
 		
 		session.setAttribute("PLAYER", newPlayer);
 	}
 
 	@Override
 	public void sessionIdle(IoSession session, IdleStatus arg1) throws Exception {
 		
 	}
 
 	@Override
 	public void sessionOpened(IoSession session) throws Exception {
 		
 	}
 
 }
