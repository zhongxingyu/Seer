 /* The MIT License
  * 
  * Copyright (c) 2005 David Rice, Trevor Croft
  * 
  * Permission is hereby granted, free of charge, to any person 
  * obtaining a copy of this software and associated documentation files 
  * (the "Software"), to deal in the Software without restriction, 
  * including without limitation the rights to use, copy, modify, merge, 
  * publish, distribute, sublicense, and/or sell copies of the Software, 
  * and to permit persons to whom the Software is furnished to do so, 
  * subject to the following conditions:
  * 
  * The above copyright notice and this permission notice shall be 
  * included in all copies or substantial portions of the Software.
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, 
  * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF 
  * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND 
  * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS 
  * BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN 
  * ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN 
  * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE 
  * SOFTWARE.
  */
 package net.rptools.maptool.client;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.EventQueue;
 import java.awt.Graphics2D;
 import java.awt.Toolkit;
 import java.awt.Transparency;
 import java.awt.image.BufferedImage;
 import java.io.IOException;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 
 import javax.imageio.ImageIO;
 import javax.swing.JOptionPane;
 import javax.swing.UIDefaults;
 import javax.swing.UIManager;
 
 import net.rptools.clientserver.ActivityListener;
 import net.rptools.clientserver.hessian.client.ClientConnection;
 import net.rptools.lib.FileUtil;
 import net.rptools.maptool.client.ui.ConnectionStatusPanel;
 import net.rptools.maptool.client.ui.MapToolFrame;
 import net.rptools.maptool.client.ui.zone.ZoneRenderer;
 import net.rptools.maptool.client.ui.zone.ZoneRendererFactory;
 import net.rptools.maptool.client.ui.zone.ZoneView;
 import net.rptools.maptool.language.I18N;
 import net.rptools.maptool.model.Campaign;
 import net.rptools.maptool.model.CampaignFactory;
 import net.rptools.maptool.model.ObservableList;
 import net.rptools.maptool.model.Player;
 import net.rptools.maptool.model.TextMessage;
 import net.rptools.maptool.model.Zone;
 import net.rptools.maptool.server.MapToolServer;
 import net.rptools.maptool.server.ServerCommand;
 import net.rptools.maptool.server.ServerConfig;
 import net.rptools.maptool.server.ServerPolicy;
 import net.rptools.maptool.transfer.AssetTransferManager;
 import net.tsc.servicediscovery.ServiceAnnouncer;
 
 import com.jidesoft.plaf.LookAndFeelFactory;
 
 import de.muntjak.tinylookandfeel.Theme;
 import de.muntjak.tinylookandfeel.controlpanel.ColorReference;
 
 /**
  */
 public class MapTool {
 	
 	private static MapToolFrame clientFrame;
     private static MapToolServer server;
     private static ServerCommand serverCommand;
     private static ServerPolicy serverPolicy;
     
     private static AssetTransferManager assetTransferManager;
 
     private static String version;
     
     private static Campaign campaign;
     
     private static ObservableList<Player> playerList;
     private static ObservableList<TextMessage> messageList;
     
     private static Player player;
     
     private static ClientConnection conn;
     private static ClientMethodHandler handler;
     
 	private static ServiceAnnouncer announcer;
 	
     private static AutoSaveManager autoSaveManager = new AutoSaveManager();
 	
 	public static void showError(String message) {
 		JOptionPane.showMessageDialog(clientFrame, I18N.getText(message), "Error", JOptionPane.ERROR_MESSAGE);
 	}
 	
 	public static void showInformation(String message) {
 		JOptionPane.showMessageDialog(clientFrame, I18N.getText(message), "Info", JOptionPane.INFORMATION_MESSAGE);
 	}
 	
 	public static boolean confirm(String message) {
 		return JOptionPane.showConfirmDialog(clientFrame, I18N.getText(message), "Confirm", JOptionPane.OK_OPTION) == JOptionPane.OK_OPTION;
 	}
 	
     private MapTool() {
         // Not instantiatable
     }
     
     public static void updateServerPolicy(ServerPolicy policy) {
     	setServerPolicy(policy);
     	
     	// Give everyone the new policy
     	if (serverCommand != null) {
     		serverCommand.setServerPolicy(policy);
     	}
     }
     
     public static BufferedImage takeMapScreenShot(ZoneView view) {
     	ZoneRenderer renderer = clientFrame.getCurrentZoneRenderer();
     	Dimension size = renderer.getSize();
     	
     	BufferedImage image = new BufferedImage(size.width, size.height, Transparency.OPAQUE);
     	Graphics2D g = image.createGraphics();
     	g.setClip(0, 0, size.width, size.height);
     	
     	renderer.renderZone(g, view);
     	
     	g.dispose();
     	
     	return image;
     }
     
 	public static AutoSaveManager getAutoSaveManager() {
 		return autoSaveManager;
 	}
 
 	private static void initialize() {
 		
         // First timer
 		AppSetup.install();
 		
 		// We'll manage our own images
 		ImageIO.setUseCache(false);
 		
 		assetTransferManager = new AssetTransferManager();
 		assetTransferManager.addConsumerListener(new AssetTransferHandler());
 		
         playerList = new ObservableList<Player>();
         messageList = new ObservableList<TextMessage>(Collections.synchronizedList(new ArrayList<TextMessage>()));
         
         handler = new ClientMethodHandler();
         
         clientFrame = new MapToolFrame();
         
         serverCommand = new ServerCommandClientImpl();
         
         player = new Player("", 0, "");
         
         try {
         	startPersonalServer(CampaignFactory.createBasicCampaign());
         } catch (Exception e) {
         	e.printStackTrace();
         }
         AppActions.updateActions();
         
 	}
 	
 	public static String getVersion() {
 		if (version == null) {
             version = "DEVELOPMENT";
             try {
 	            if (MapTool.class.getClassLoader().getResource("net/rptools/maptool/client/version.txt") != null) {
 	                version = new String(FileUtil.loadResource("net/rptools/maptool/client/version.txt"));
 	            }
             } catch (IOException ioe) {
             	version = "CAN'T FIND VERSION FILE";
             }
 		}
 		
 		return version;
 	}
 	
 	public static ServerPolicy getServerPolicy() {
 		return serverPolicy;
 	}
 	
     public static ServerCommand serverCommand() {
         return serverCommand;
     }
     
     public static void startIndeterminateAction() {
     	//clientFrame.startIndeterminateAction();
     }
     
     public static void endIndeterminateAction() {
     	//clientFrame.endIndeterminateAction();
     }
     
     public static void startDeterminateAction(int totalWork) {
     	//clientFrame.startDeterminateAction(totalWork);
     }
     
     public static void updateDeterminateActionProgress(int additionalWorkCompleted) {
     	//clientFrame.updateDeterminateActionProgress(additionalWorkCompleted);
     }
     
     public static void endDeterminateAction() {
     	//clientFrame.endDeterminateAction();
     }
 
     public static MapToolServer getServer() {
     	return server;
     }
     
     public static void addPlayer(Player player) {
         if (!playerList.contains(player)) {
             playerList.add(player);
             
             // LATER: Make this non-anonymous
             playerList.sort (new Comparator<Player>() {
                 public int compare(Player arg0, Player arg1) {
                     return arg0.getName().compareToIgnoreCase(arg1.getName());
                 }
             });
             
             if (!player.equals(MapTool.getPlayer())) {	
             	String announcement = player.getName() + " has connected.";
             	getFrame().getNotificationOverlay().addEvent(announcement);
     			addLocalMessage("<span style='color:#0000ff'><i>" + announcement + "</i></span>");
             }
         }
 	}
 	
     public Player getPlayer(String name) {
         
         for (int i = 0; i < playerList.size(); i++) {
             if (playerList.get(i).getName().equals(name)) {
                 return playerList.get(i);
             }
         }
         return null;
     }
 
     public static void removePlayer(Player player) {
     	
     	if (player == null) {
     		return;
     	}
     	
 		playerList.remove(player);
 
 		if (MapTool.getPlayer() != null && !player.equals(MapTool.getPlayer())) {
 			String announcement = player.getName() + " has disconnected.";
 			getFrame().getNotificationOverlay().addEvent(announcement);
 			addLocalMessage("<span style='color:#0000ff'><i>" + announcement + "</i></span>");
 		}
 	}
 	
 	
     public static ObservableList<TextMessage> getMessageList () {
         return messageList;
     }
     
     /**
      * These are the messages that originate from the server
      * @param channel
      * @param message
      */
     public static void addServerMessage(TextMessage message) {
 
         // Filter
         if (message.isGM() && !getPlayer().isGM()) {
             return;
         }
         if (message.isWhisper() && !getPlayer().getName().equalsIgnoreCase(message.getTarget())) {
             return;
         }
         
         if (!getFrame().isCommandPanelVisible()) {
         	getFrame().getChatActionLabel().setVisible(true);
         }
         messageList.add(message);
     }
 
     /**
      * These are the messages that are generated locally
      * @param channel
      * @param message
      */
     public static void addMessage(TextMessage message) {
         
         // Filter stuff
         addServerMessage(message);
         
         if (!message.isMe()) {
             serverCommand().message(message);
         }
     }
     
     /**
      * Add a message only this client can see.  This is a shortcut for addMessage(ME, ...)
      * @param message
      */
     public static void addLocalMessage(String message) {
         addMessage(TextMessage.me(message));
     }
     
 
     public static Campaign getCampaign() {
         if (campaign == null) {
             campaign = new Campaign();
         }
         return campaign;
     }
     
     public static void setCampaign(Campaign campaign) {
     	
     	// Load up the new
     	MapTool.campaign = campaign;
     	ZoneRenderer currRenderer = null;
 
         // Clean up
         clientFrame.setCurrentZoneRenderer(null);
         clientFrame.clearZoneRendererList();
         clientFrame.clearTokenTree();
         if (campaign == null) {
             return;
         }
 
         // Install new campaign
         for (Zone zone : campaign.getZones()) {
             
             ZoneRenderer renderer = ZoneRendererFactory.newRenderer(zone);
             clientFrame.addZoneRenderer(renderer);
             
             if (currRenderer == null && (getPlayer().isGM() || zone.isVisible())){
                 currRenderer = renderer;
             }
             
             AppListeners.fireZoneAdded(zone);
         }
 
     	clientFrame.setCurrentZoneRenderer(currRenderer);
     }
     
     public static void setServerPolicy(ServerPolicy policy) {
     	serverPolicy = policy;
     }
     
     public static AssetTransferManager getAssetTransferManager() {
     	return assetTransferManager;
     }
     
 	public static void startServer(String id, ServerConfig config, ServerPolicy policy, Campaign campaign) throws IOException {
 		
 		if (server != null) {
 			Thread.dumpStack();
 			showError("Server is already started");
 			return;
 		}
 
 		assetTransferManager.flush();
 		
 		// TODO: the client and server campaign MUST be different objects.  Figure out a better init method
 		server = new MapToolServer (config, policy);
 		server.setCampaign(campaign);
         
 		serverPolicy = server.getPolicy();
         setCampaign(null);
         
         if (announcer != null) {
         	announcer.stop();
         }
         // Don't announce personal servers
         if (!config.isPersonalServer()) {
 	        announcer = new ServiceAnnouncer(id, server.getConfig().getPort(), AppConstants.SERVICE_GROUP);
 	        announcer.start();
         }
         
         // Registered ?
         if (config.isServerRegistered() && !config.isPersonalServer()) {
         	try {
 	        	int result = MapToolRegistry.registerInstance(config.getServerName(), config.getPort(), config.getServerPassword());
 	        	if (result == 3) {
 	        		MapTool.showError("That ID is already in use, server not registered");
 	        	}
 	        	// TODO: I don't like this
         	} catch (Exception e) {
         		MapTool.showError("Unable to register your server: " + e);
         	}
         }
 	}
 	
 	public static void stopServer() {
 		if (server == null) {
 			return;
 		}
 		
 		disconnect();
 		server.stop();
 		server = null;
 	}
 
     public static ObservableList<Player> getPlayerList() {
         return playerList;
     }
     
     /**
      * Whether a specific player is connected to the game
      */
     public static boolean isPlayerConnected(String player) {
         
         for (int i = 0; i < playerList.size(); i++) {
             Player p = playerList.get(i);
             if (p.getName().equalsIgnoreCase(player)) {
                 return true;
             }
         }
         return false;
     }
     
     public static void removeZone(Zone zone) {
 		MapTool.serverCommand().removeZone(zone.getId());
 		MapTool.getFrame().removeZoneRenderer(MapTool.getFrame().getZoneRenderer(zone.getId()));
 		MapTool.getCampaign().removeZone(zone.getId());
     }
     
 	public static void addZone(Zone zone) {
 
 		if (getCampaign().getZones().size() == 1) {
 			// Remove the default map
 			Zone singleZone = getCampaign().getZones().get(0);
 			if (CampaignFactory.DEFAULT_MAP_NAME.equals(singleZone.getName()) && singleZone.isEmpty()) {
 				removeZone(singleZone);
 			}
 		}
 		
         getCampaign().putZone(zone);
 
         serverCommand().putZone(zone);
         
         AppListeners.fireZoneAdded(zone);
         
         // Show the new zone
         clientFrame.setCurrentZoneRenderer(ZoneRendererFactory.newRenderer(zone));
 	}
 	
 	public static Player getPlayer() {
 		return player;
 	}
     
 	public static void startPersonalServer(Campaign campaign) throws IOException {
 		
 		ServerConfig config = ServerConfig.createPersonalServerConfig();
 		MapTool.startServer(null, config, new ServerPolicy(), campaign);
 
 		String username = System.getProperty("user.name", "Player");
 		
 		// Connect to server
 		MapTool.createConnection("localhost", config.getPort(), new Player(username, Player.Role.GM, null));
 
 		// connecting
 		MapTool.getFrame().getConnectionStatusPanel().setStatus(ConnectionStatusPanel.Status.server);
 	}
 	
     public static void createConnection(String host, int port, Player player) throws UnknownHostException, IOException {
 
     	MapTool.player = player;
     	MapTool.getFrame().getCommandPanel().setIdentity(null);
     	
     	ClientConnection clientConn = new MapToolConnection(host, port, player);
     	
 	    	
     	clientConn.addMessageHandler(handler);
     	clientConn.addActivityListener(clientFrame.getActivityMonitor());
     	clientConn.addActivityListener(new ActivityProgressListener());
     	clientConn.addDisconnectHandler(new ServerDisconnectHandler());
         
     	clientConn.start();
 
     	// LATER: I really, really, really don't like this startup pattern
         if (clientConn.isAlive()) {
         	conn = clientConn;
     	}
     }
     
     public static void closeConnection() throws IOException {
         if (conn != null) {
             conn.close();
         }
     }
     
     public static ClientConnection getConnection() {
     	return conn;
     }
     
     public static boolean isPersonalServer() {
         return server != null && server.getConfig().isPersonalServer();
     }
 
     public static boolean isHostingServer() {
         return server != null && !server.getConfig().isPersonalServer();
     }
 
     public static void disconnect() {
 
    	boolean isPersonalServer = server.getConfig().isPersonalServer();
     	
     	if (announcer != null) {
     		announcer.stop();
     		announcer = null;
     	}
 
         if (conn == null || !conn.isAlive()) {
             return;
         }
         
     	// Unregister ourselves
     	if (server != null && server.getConfig().isServerRegistered() && !isPersonalServer) {
     		try {
     			MapToolRegistry.unregisterInstance(server.getConfig().getPort());
     		} catch (Throwable t) {
     			t.printStackTrace();
     		}
     	}
     	
         try {
             conn.close();
             conn = null;
             playerList.clear();
             
         } catch (IOException ioe) {
             // This isn't critical, we're closing it anyway
             ioe.printStackTrace();
         }
         
         MapTool.getFrame().getConnectionStatusPanel().setStatus(ConnectionStatusPanel.Status.disconnected);
         
         if (!isPersonalServer) {
         	addLocalMessage("<span style='color:blue'><i>You have disconnected.</i></span>");
         }
     }
     
 	public static MapToolFrame getFrame() {
 		return clientFrame;
 	}
 	
 	public static void main(String[] args) {
 
 		// System properties
 		System.setProperty("swing.aatext", "true");
 		//System.setProperty("sun.java2d.opengl", "true");
 		
 		// LAF
         try {
         	UIManager.setLookAndFeel("de.muntjak.tinylookandfeel.TinyLookAndFeel");
     		com.jidesoft.utils.Lm.verifyLicense("Trevor Croft", "rptools", "5MfIVe:WXJBDrToeLWPhMv3kI2s3VFo");
 //            UIManager.setLookAndFeel(LookAndFeelFactory.WINDOWS_LNF);
     		LookAndFeelFactory.addUIDefaultsCustomizer(new LookAndFeelFactory.UIDefaultsCustomizer(){
     			public void customize(UIDefaults defaults) {
     				// Remove red border around menus
     				defaults.put("PopupMenu.foreground", Color.lightGray);
     			}
     		});
     		LookAndFeelFactory.installJideExtension(LookAndFeelFactory.XERTO_STYLE);
         	
         	// Make the toggle button pressed state look more distinct
         	Theme.buttonPressedColor[Theme.style] = new ColorReference(Color.gray);
 
         } catch (Exception e) {
             System.err.println("Exception during look and feel setup: " + e);
         }
 		
         // Draw frame contents on resize
         Toolkit.getDefaultToolkit().setDynamicLayout(true);        
         
         EventQueue.invokeLater(new Runnable() {
         	public void run() {
                     initialize();
                 
                     clientFrame.setVisible(true);
                     
                     // Check to see if there is an autosave file from mt crashing
                     getAutoSaveManager().check();
         	}
         });
         
 //        new Thread(new HeapSpy()).start();
 	}
 	
 	private static class ActivityProgressListener implements ActivityListener {
 		/* (non-Javadoc)
 		 * @see net.rptools.clientserver.ActivityListener#notify(net.rptools.clientserver.ActivityListener.Direction, net.rptools.clientserver.ActivityListener.State, int, int)
 		 */
 		public void notify(Direction direction, State state, int total, int current) {
 
 			if (state == State.Start) {
 				MapTool.startIndeterminateAction();
 			} else if (state == State.Complete) {
                 MapTool.endIndeterminateAction();
 			}
 		}
 	}
 
 }
