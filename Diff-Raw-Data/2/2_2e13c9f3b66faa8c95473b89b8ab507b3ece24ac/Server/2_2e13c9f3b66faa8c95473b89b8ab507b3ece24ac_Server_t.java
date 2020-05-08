 /*
  * Created on Oct 1, 2003
  */
 package games.batoru.server;
 
 import java.awt.Point;
 import java.util.*;
 
 import javax.vecmath.*;
 
 import org.codejive.world3d.*;
 import org.codejive.world3d.net.MessagePacket;
 import org.codejive.world3d.net.MessagePort;
 import org.codejive.world3d.net.NetworkClassCache;
 
 import games.batoru.*;
 import games.batoru.client.PatchyLandscapeRenderer;
 import games.batoru.entities.*;
 import games.batoru.net.*;
 
 /**
  * @author Tako
  */
 public class Server {
 	private Thread m_broadcastThread, m_connectThread, m_universeThread;
 	private boolean m_bRunning;
 	
 	private MessagePort m_connectorPort;
 	private MessagePacket m_message;
 	private Map m_clients;
 	
 	private Universe m_universe;
 	
 	private static final int MAX_CLIENTS = 8;
 	
 	public Server() {
 		m_clients = new HashMap();
 		
 		NetworkClassCache cache = NetworkClassCache.getServerCache();
 		cache.registerClass("games.batoru.BatoruUniverse", "games.batoru.BatoruUniverse");
		cache.registerClass("games.batoru.PatchyLandscape", "games.batoru.PatchyLandscape");
 		cache.registerClass("games.batoru.entities.PlayerClass", "games.batoru.shapes.PlayerShape");
 		cache.registerClass("games.batoru.entities.BulletClass", "games.batoru.shapes.BulletShape");
 		cache.registerClass("games.batoru.entities.TreeClass", "games.batoru.shapes.TreeShape");
 		cache.registerClass("games.batoru.entities.TurretClass", "games.batoru.shapes.TurretShape");
 
 		m_universe = new BatoruUniverse();
 		
 		PatchyLandscape lmodel = new PatchyLandscape(101, 101, 5.0f, 5.0f);
 		m_universe.setLandscape(lmodel);
 		decorateLandscape(m_universe, lmodel);
 	}
 	
 	public Universe getUniverse() {
 		return m_universe;
 	}
 	
 	public void start() {
 		m_bRunning = true;
 
 		m_connectorPort = new MessagePort("ClientConnector");
 		m_connectorPort.start();
 		m_message = new MessagePacket();
 		
 		// Start thread that will handle the broadcast telling the world about this server
 		m_broadcastThread = new Thread(new Runnable() {
 			public void run() {
 				runBroadcaster();
 			}
 		});
 		m_broadcastThread.start();
 			
 		// Start the thread that will handle client connect requests
 		m_connectThread = new Thread(new Runnable() {
 			public void run() {
 				runConnector();
 			}
 		});
 		m_connectThread.start();
 
 		// Start the thread that will update the universe
 		m_universeThread = new Thread(new Runnable() {
 			public void run() {
 				runUniverse();
 			}
 		});
 		m_universeThread.start();
 	}
 	
 	public void stop() {
 		m_bRunning = false;
 		m_broadcastThread.interrupt();
 		m_connectThread.interrupt();
 		m_universeThread.interrupt();
 	}
 	
 	public void runBroadcaster() {
 		MessagePacket msg = new MessagePacket();
 		msg.setPort(22344);
 		while (m_bRunning && !m_connectorPort.isClosed()) {
 			ServerMessageHelper.sendIThink(msg, m_connectorPort);
 			try {
 				Thread.sleep(1000);
 			} catch (InterruptedException e) {
 				m_bRunning = false;
 			}
 		}
 	}
 		
 	public void runConnector() {
 		try {
 			while (m_bRunning && !m_connectorPort.isClosed()) {
 				MessagePacket packet = m_connectorPort.receivePacket();
 				byte packetType = packet.readByte();
 				if (packetType == ClientMessageHelper.MSG_CONNECT_REQUEST) {
 					Universe.log(this, "Client connect request received");
 					ClientHandler client = new ClientHandler(this, packet.getAddress(), packet.getPort());
 					if (addClient(client)) {
 						ServerMessageHelper.sendConnectAccept(packet, m_connectorPort, client);
 						client.doInitialization();
 					} else {
 						ServerMessageHelper.sendConnectDeny(packet, m_connectorPort, client, "Server full");
 					}
 				} else {
 					Universe.log(this, "unknown message type received from unconnected client");
 				}
 				m_connectorPort.releasePacket(packet);
 			}
 		} catch (InterruptedException e) { /* ignore */ }
 		
 		// Tell all connected clients to disconnect
 		Iterator i = m_clients.values().iterator();
 		while (i.hasNext()) {
 			ServerMessageHelper.sendDisconnect(m_message, (ClientHandler)i.next(), "Server shutdown");
 		}
 
 		m_connectorPort.stop();
 	}
 	
 	public boolean addClient(ClientHandler _client) {
 		boolean bResult = false;
 		synchronized(m_clients) {
 			if (m_clients.size() < MAX_CLIENTS) {
 				m_clients.put(new Integer(_client.getPort()), _client);
 				bResult = true;
 			}
 		}
 		return bResult;
 	} 
 	
 	public void removeClient(ClientHandler _client) {
 		synchronized(m_clients) {
 			m_clients.remove(new Integer(_client.getPort()));
 		}
 	} 
 	
 	public void runUniverse() {
 		int nFrameNr = 0;
 		long lLastSystemTime = 0;
 		long lCurrentSystemTime = m_universe.getAge();
 		
 		while (m_bRunning) {
 			if (lLastSystemTime > 0) {
 				float fElapsedTime = (float)(lCurrentSystemTime - lLastSystemTime) / 1000;
 				m_universe.handleFrame(fElapsedTime);
 				nFrameNr++;
 			}
 			lLastSystemTime = lCurrentSystemTime;
 			
 			try {
 				Thread.sleep(100);
 			} catch (InterruptedException e) {
 				m_bRunning = false;
 			}
 		}
 	}
 	
 	private void decorateLandscape(Universe _universe, PatchyLandscape _landscape) {
 		TreeClass treeClass = new TreeClass();
 		TurretClass turretClass = new TurretClass();
 			
 		float[][] vfHeights = _landscape.getHeights();
 		PatchyLandscape.LandscapePatch[][] vPatches = _landscape.getPatches();
 		int nObjectCount = (_landscape.getWidth() * _landscape.getHeight() / 100);
 		for (int i = 0; i < nObjectCount; i++) {
 			int x, y;
 			if (i == 0) {
 				// First object will be placed dead-center
 				x = _landscape.getWidth() / 2;
 				y = _landscape.getHeight() / 2;
 			} else {
 				// Pick a random patch
 				x = (int)(Math.random() * _landscape.getWidth());
 				y = (int)(Math.random() * _landscape.getHeight());
 			}
 			
 			// Make sure the patch is level
 			float h = vfHeights[x][y];
 			vfHeights[x + 1][y] = h;
 			vfHeights[x][y + 1] = h;
 			vfHeights[x + 1][y + 1] = h;
 
 			// Determine its center point in world coordinates
 			Point patchPos = new Point();
 			Point3f worldPos = new Point3f();
 			patchPos.move(x, y);
 			_landscape.getPatchPosition(patchPos, worldPos);
 
 			// Is it an empty patch?
 			if (vPatches[x][y].getObject() == null) {
 				// If so, put an object on it
 				Entity obj = null;
 				if (i == 0) {
 					obj = turretClass.createTurret(_universe, worldPos);
 //					obj = treeClass.createEntity(_universe, worldPos);
 				} else {
 					if ((i % 10) == 0) {
 						obj = turretClass.createTurret(_universe, worldPos);
 					} else {
 						obj = treeClass.createEntity(_universe, worldPos);
 					}
 				}
 				vPatches[x][y].setObject(obj);
 				vPatches[x][y].setType(_landscape.red);
 			}
 		}
 	}
 }
