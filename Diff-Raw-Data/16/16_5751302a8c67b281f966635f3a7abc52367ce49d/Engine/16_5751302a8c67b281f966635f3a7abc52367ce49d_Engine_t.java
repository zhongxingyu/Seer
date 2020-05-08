 package net.buddat.mineserv;
 
 import java.util.ArrayList;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import net.buddat.mineserv.net.codec.MinecraftProtocolEncoder;
 import net.buddat.mineserv.net.packet.Packet;
 import net.buddat.mineserv.net.packet.PacketHandler;
 import net.buddat.mineserv.net.packet.handlers.*;
 import net.buddat.mineserv.plr.PlayerManager;
 
 public class Engine {
 
	private final Logger logger = LoggerFactory.getLogger(Engine.class);
 	
 	private PlayerManager playerManager;
 	
 	private ArrayList<Packet> packetQueue = new ArrayList<Packet>();
 	private PacketHandler[] packetHandlers = new PacketHandler[256];
 	
 	private boolean running = true;
 	
 	public Engine() {
 		playerManager = new PlayerManager();
 	}
 	
 	public void start() {
 		init();
 		run();
 	}
 
 	private void init() {
 		bindPacketHandlers();
 		playerManager.init();
 	}
 
 	private void run() {
 		while(running) {
 			processIncomingPackets();
 			playerManager.tick();
 			
 			try {
 				Thread.sleep(50);
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	private void processIncomingPackets() {
 		for (Packet p : getPacketQueue()) {
 			PacketHandler ph = packetHandlers[p.getPacketId()];
 			if (ph != null) {
 				ph.handlePacket(p);
 			} else {
 				logger.error("Unhandled Packet: " + p.getPacketId());
 			}
 		}
 		
 		getPacketQueue().clear();
 	}
 
 	public boolean isRunning() {
 		return running;
 	}
 	
 	public void setRunning(boolean r) {
 		running = r;
 	}
 
 	public PlayerManager getPlayerManager() {
 		return playerManager;
 	}
 
 	public void setPlayerManager(PlayerManager playerManager) {
 		this.playerManager = playerManager;
 	}
 	
 	public ArrayList<Packet> getPacketQueue() {
 		return packetQueue;
 	}
 
 	public void setPacketQueue(ArrayList<Packet> packetQueue) {
 		this.packetQueue = packetQueue;
 	}
 
 	private void bindPacketHandlers() {
 		int[] packets = {
 				1,
 				2
 		};
 		
 		PacketHandler[] handlers = {
 				new LoginHandler(),
 				new HandshakeHandler()
 		};
 		
 		for (int i = 0; i < packets.length; i++) {
 			this.packetHandlers[packets[i]] = handlers[i];
 		}
 	}
 	
 }
