 /*
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License. 
  */
 package net.rptools.maptool.client;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.EventQueue;
 import java.awt.Font;
 import java.awt.Graphics2D;
 import java.awt.Toolkit;
 import java.awt.Transparency;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.awt.image.BufferedImage;
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.IOException;
 import java.lang.reflect.InvocationTargetException;
 import java.net.URL;
 import java.net.UnknownHostException;
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Enumeration;
 import java.util.Locale;
 
 import javax.imageio.ImageIO;
 import javax.swing.BorderFactory;
 import javax.swing.ImageIcon;
 import javax.swing.JFrame;
 import javax.swing.JMenuBar;
 import javax.swing.JOptionPane;
 import javax.swing.SwingConstants;
 import javax.swing.ToolTipManager;
 import javax.swing.UIDefaults;
 import javax.swing.UIManager;
 import javax.swing.plaf.FontUIResource;
 
 import net.rptools.clientserver.hessian.client.ClientConnection;
 import net.rptools.lib.BackupManager;
 import net.rptools.lib.EventDispatcher;
 import net.rptools.lib.FileUtil;
 import net.rptools.lib.TaskBarFlasher;
 import net.rptools.lib.image.ThumbnailManager;
 import net.rptools.lib.net.RPTURLStreamHandlerFactory;
 import net.rptools.lib.sound.SoundManager;
 import net.rptools.lib.swing.SwingUtil;
 import net.rptools.maptool.client.functions.UserDefinedMacroFunctions;
 import net.rptools.maptool.client.swing.MapToolEventQueue;
 import net.rptools.maptool.client.swing.NoteFrame;
 import net.rptools.maptool.client.swing.SplashScreen;
 import net.rptools.maptool.client.ui.AppMenuBar;
 import net.rptools.maptool.client.ui.ConnectionStatusPanel;
 import net.rptools.maptool.client.ui.MapToolFrame;
 import net.rptools.maptool.client.ui.StartServerDialogPreferences;
 import net.rptools.maptool.client.ui.zone.PlayerView;
 import net.rptools.maptool.client.ui.zone.ZoneRenderer;
 import net.rptools.maptool.client.ui.zone.ZoneRendererFactory;
 import net.rptools.maptool.language.I18N;
 import net.rptools.maptool.model.AssetManager;
 import net.rptools.maptool.model.Campaign;
 import net.rptools.maptool.model.CampaignFactory;
 import net.rptools.maptool.model.GUID;
 import net.rptools.maptool.model.ObservableList;
 import net.rptools.maptool.model.Player;
 import net.rptools.maptool.model.TextMessage;
 import net.rptools.maptool.model.Zone;
 import net.rptools.maptool.model.ZoneFactory;
 import net.rptools.maptool.server.MapToolServer;
 import net.rptools.maptool.server.ServerCommand;
 import net.rptools.maptool.server.ServerConfig;
 import net.rptools.maptool.server.ServerPolicy;
 import net.rptools.maptool.transfer.AssetTransferManager;
 import net.rptools.maptool.util.UPnPUtil;
 import net.tsc.servicediscovery.ServiceAnnouncer;
 
 import org.apache.log4j.LogManager;
 import org.apache.log4j.Logger;
 import org.apache.log4j.xml.DOMConfigurator;
 
 import com.centerkey.utils.BareBonesBrowserLaunch;
 import com.jidesoft.plaf.LookAndFeelFactory;
 import com.jidesoft.plaf.UIDefaultsLookup;
 import com.jidesoft.plaf.basic.ThemePainter;
 
 import de.muntjak.tinylookandfeel.Theme;
 import de.muntjak.tinylookandfeel.controlpanel.ColorReference;
 
 /**
  */
 public class MapTool {
 
 	private static final Logger log = Logger.getLogger(MapTool.class);
 	
 	/**
 	 * URLs for the Help menu. Currently consists of: <b>helpURL</b>,
 	 * <b>tutorialsURL</b>, and <b>forumsURL</b>.
 	 */
 	private static final String CONFIGURATION_PROPERTIES = "net/rptools/maptool/client/configuration.properties";
 
 	/**
 	 * The splash image that comes up during application initialization.
 	 */
 	private static final String SPLASH_IMAGE = "net/rptools/maptool/client/image/maptool_splash.png";
 
 	/**
 	 * Contains just the version number of MapTool, such as <code>1.3.b49</code>
 	 * .
 	 */
 	private static final String VERSION_TXT = "net/rptools/maptool/client/version.txt";
 
 	/**
 	 * Specifies the properties file that holds sound information. Only two
 	 * sounds currently: <b>Dink</b> and <b>Clink</b>.
 	 */
 	private static final String SOUND_PROPERTIES = "net/rptools/maptool/client/sounds.properties";
 	
 	/**
 	 * Returns true if currently running on a Mac OS X based operating system.
 	 */
 	public static boolean MAC_OS_X = (System.getProperty("os.name").toLowerCase().startsWith("mac os x"));	
 
 	public static enum ZoneEvent {
 
 		Added, 
 		Removed, 
 		Activated, 
 		Deactivated
 
 	}
 
 	public static enum PreferencesEvent {
 		Changed
 	}
 
 	private static final Dimension THUMBNAIL_SIZE = new Dimension(100, 100);
 
 	private static ThumbnailManager thumbnailManager;
 
 	private static String version;
 
 	private static Campaign campaign;
 
 	private static ObservableList<Player> playerList;
 	private static ObservableList<TextMessage> messageList;
 
 	private static Player player;
 
 	private static ClientConnection conn;
 	private static ClientMethodHandler handler;
 	private static JMenuBar menuBar;
     private static MapToolFrame clientFrame;
     private static NoteFrame profilingNoteFrame;
     private static MapToolServer server;
     private static ServerCommand serverCommand;
     private static ServerPolicy serverPolicy;
     
     private static BackupManager backupManager;
     
     private static AssetTransferManager assetTransferManager;
 
 	private static ServiceAnnouncer announcer;
 
 	private static AutoSaveManager autoSaveManager;
 
 	private static SoundManager soundManager;
 	private static TaskBarFlasher taskbarFlasher;
 
 	private static EventDispatcher eventDispatcher;
 
 	private static AppConfiguration configuration;
 
 	private static MapToolLineParser parser = new MapToolLineParser();
 
 	private static String lastWhisperer;
 
 	/**
 	 * This method looks up the message key in the properties file and returns the resultant
 	 * text with the detail message from the <code>Throwable</code> appended to the end.
 	 * @param msgKey the string to use when calling {@link I18N#getText(String)}
 	 * @param t the exception currently being processed
 	 * @return the <code>String</code> result
 	 */
 	public static String generateMessage(String msgKey, Throwable t) {
 		String msg;
 		if (t == null) {
 			msg = I18N.getText(msgKey);
 		} else {
 			msg = I18N.getText(msgKey) + "<br>" + t.toString();
 		}
 		return msg;
 	}
 
 	/**
 	 * This method is the base method for putting a dialog box up on the screen that might
 	 * be an error, a warning, or just an information message.  Do not use this method if
 	 * the desired result is a simple confirmation box (use {@link #confirm(String)} instead).
 	 * @param msgKey the string to put in the body of the dialog
 	 * @param titleKey the string to use when retrieving the title of the dialog window
 	 * @param messageType JOptionPane.{ERROR|WARNING|INFORMATION}_MESSAGE
 	 */
 	public static void showMessage(String message, String titleKey, int messageType) {
 		String title = I18N.getText(titleKey);
 		JOptionPane.showMessageDialog(clientFrame, "<html>" + message, title, messageType);
 	}
 
 	public static void showError(String msgKey) {
 		showError(msgKey, null);
 	}
 	public static void showError(String msgKey, Throwable t) {
 		String msg = generateMessage(msgKey, t);
 		log.error(msg, t);
 		showMessage(msg, "msg.title.messageDialogError", JOptionPane.ERROR_MESSAGE);
 	}
 
 	public static void showWarning(String msgKey) {
 		showWarning(msgKey, null);
 	}
 	public static void showWarning(String msgKey, Throwable t) {
 		String msg = generateMessage(msgKey, t);
 		log.warn(msg, t);
 		showMessage(msg, "msg.title.messageDialogWarning", JOptionPane.WARNING_MESSAGE);
 	}
 
 	public static void showInformation(String msgKey) {
 		showInformation(msgKey, null);
 	}
 	public static void showInformation(String msgKey, Throwable t) {
 		String msg = generateMessage(msgKey, t);
 		log.info(msg, t);
 		showMessage(msg, "msg.title.messageDialogInfo", JOptionPane.INFORMATION_MESSAGE);
 	}
 
 	public static boolean confirm(String message) {
 		String msg = I18N.getText(message);
 		log.debug(msg);
 		String title = I18N.getText("msg.title.messageDialogConfirm");
 		return JOptionPane.showConfirmDialog(clientFrame, msg, title,
 				JOptionPane.OK_OPTION) == JOptionPane.OK_OPTION;
 	}
 
 	/**
 	 * This method is specific to deleting a token,  but it can be used as a basis
 	 * for any other method which wants to be turned off via a property
 	 * @return true if the token should be deleted.
 	 */
 	public static boolean confirmTokenDelete() {
 		if(!AppPreferences.getTokensWarnWhenDeleted()) {
 			return true;
 		}
 		
 		String msg = I18N.getText("msg.confirm.deleteToken");
 		log.debug(msg);
 		Object[] options = { I18N.getText("msg.title.messageDialog.yes"),
 				I18N.getText("msg.title.messageDialog.cancel"),
 				I18N.getText("msg.title.messageDialog.dontAskAgain") };
 		String title = I18N.getText("msg.title.messageDialogConfirm");
 		int val = JOptionPane.showOptionDialog(clientFrame, msg, title,
 	            JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
 	            null, options, options[0]);
 		
 		// Cancel Button 
 		if(val == 1) {
 			return false;
 		}
 		
 		// Don't show again Button
 		if(val == 2) {
 			showInformation("msg.confirm.deleteToken.removed");
 			AppPreferences.setTokensWarnWhenDeleted(false);
 		}
 		
 		// Assumed 'Yes' response
 		return true;
 	}
 	
 	private MapTool() {
 		// Not instantiatable
 	}
 
 	public static BackupManager getBackupManager() {
 		if (backupManager == null) {
 			try {
 				backupManager = new BackupManager(AppUtil.getAppHome("backup"));
 			} catch (IOException ioe) {
 				ioe.printStackTrace();
 			}
 		}
 		return backupManager;
 	}
 
 	public static void showDocument(String url) {
 		BareBonesBrowserLaunch.openURL(url);
 	}
 
 	public static SoundManager getSoundManager() {
 		return soundManager;
 	}
 
 	public static void playSound(String eventId) {
 		if (AppPreferences.getPlaySystemSounds()) {
 			if (AppPreferences.getPlaySystemSoundsOnlyWhenNotFocused() && isInFocus()) {
 				return;
 			}
 			soundManager.playSoundEvent(eventId);
 		}
 	}
 
 	public static void updateServerPolicy(ServerPolicy policy) {
 		setServerPolicy(policy);
 
 		// Give everyone the new policy
 		if (serverCommand != null) {
 			serverCommand.setServerPolicy(policy);
 		}
 	}
 
 	public static boolean isInFocus() {
 		// TODO: This should probably also check owned windows
 		return getFrame().isFocused();
 	}
 
 	public static BufferedImage takeMapScreenShot(final PlayerView view) {
 		final ZoneRenderer renderer = clientFrame.getCurrentZoneRenderer();
 		if (renderer == null) {
 			return null;
 		}
 
 		Dimension size = renderer.getSize();
 
 		BufferedImage image = new BufferedImage(size.width, size.height, Transparency.OPAQUE);
 		final Graphics2D g = image.createGraphics();
 		g.setClip(0, 0, size.width, size.height);
 
 		// Have to do this on the EDT so that there aren't any odd side effects
 		// of rendering
 		// using a renderer that's on screen
 		if (!EventQueue.isDispatchThread()) {
 			try {
 				EventQueue.invokeAndWait(new Runnable() {
 					public void run() {
 						renderer.renderZone(g, view);
 					}
 				});
 			} catch (InterruptedException ie) {
 				MapTool.showError("While creating snapshot", ie);
 			} catch (InvocationTargetException ite) {
 				MapTool.showError("While creating snapshot", ite);
 			}
 		} else {
 			renderer.renderZone(g, view);
 		}
 
 		g.dispose();
 
 		return image;
 	}
 
 	public static AutoSaveManager getAutoSaveManager() {
 		if (autoSaveManager == null) {
 			autoSaveManager = new AutoSaveManager();
 		}
 		return autoSaveManager;
 	}
 
 	public static EventDispatcher getEventDispatcher() {
 		return eventDispatcher;
 	}
 
 	private static void registerEvents() {
 		getEventDispatcher().registerEvents(ZoneEvent.values());
 		getEventDispatcher().registerEvents(PreferencesEvent.values());
 	}
 
 	public static AppConfiguration getConfiguration() {
 		return configuration;
 	}
 
 	private static void initialize() {
 
 		configuration = new AppConfiguration(CONFIGURATION_PROPERTIES);
 
 		// First timer
 		AppSetup.install();
 
 		// Clean up after ourselves
 		try {
 			FileUtil.delete(AppUtil.getAppHome("tmp"), 2);
 		} catch (IOException ioe) {
 			MapTool.showError("While initializing (cleaning tmpdir)", ioe);
 		}
 
 		// We'll manage our own images
 		ImageIO.setUseCache(false);
 
 		eventDispatcher = new EventDispatcher();
 		registerEvents();
 
 		soundManager = new SoundManager();
 		try {
 			soundManager.configure(SOUND_PROPERTIES);
 		} catch (IOException ioe) {
 			MapTool.showError("While initializing (configuring sound)", ioe);
 		}
 
 		assetTransferManager = new AssetTransferManager();
 		assetTransferManager.addConsumerListener(new AssetTransferHandler());
 		
         playerList = new ObservableList<Player>();
         messageList = new ObservableList<TextMessage>(Collections.synchronizedList(new ArrayList<TextMessage>()));
         
         handler = new ClientMethodHandler();
         
         clientFrame = new MapToolFrame(menuBar);
         
         serverCommand = new ServerCommandClientImpl();
         
         player = new Player("", Player.Role.GM, "");
         
         try {
         	startPersonalServer(CampaignFactory.createBasicCampaign());
         } catch (Exception e) {
 			MapTool.showError("While starting personal server", e);
         }
         AppActions.updateActions();
         
         ToolTipManager.sharedInstance().setInitialDelay(AppPreferences.getToolTipInitialDelay());
         ToolTipManager.sharedInstance().setDismissDelay(AppPreferences.getToolTipDismissDelay());
         
         // TODO: make this more formal when we switch to mina
         new ServerHeartBeatThread().start();
 	}
 
 	public static NoteFrame getProfilingNoteFrame() {
 		if (profilingNoteFrame == null) {
 	        profilingNoteFrame = new NoteFrame();
 	        profilingNoteFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
 	        profilingNoteFrame.addWindowListener(new WindowAdapter() {
 	        	public void windowClosing(WindowEvent e) {
 	        		AppState.setCollectProfilingData(false);
 	        		profilingNoteFrame.setVisible(false);
 	        	}
 	        });
 	        profilingNoteFrame.setSize(profilingNoteFrame.getPreferredSize());
 	        SwingUtil.centerOver(profilingNoteFrame, clientFrame);
 		}
 		return profilingNoteFrame;
 	}
 	
 	public static String getVersion() {
 		if (version == null) {
 			version = "DEVELOPMENT";
 			try {
 				if (MapTool.class.getClassLoader().getResource(VERSION_TXT) != null) {
 					version = new String(FileUtil.loadResource(VERSION_TXT));
 				}
 			} catch (IOException ioe) {
 				String msg = I18N.getText("msg.info.versionFile", VERSION_TXT);
 				version = msg;
 				MapTool.showError("msg.error.versionFileMissing");
 			}
 		}
 
 		return version;
 	}
 
 	public static boolean isDevelopment() {
 		return "DEVELOPMENT".equals(version);
 	}
 
 	public static ServerPolicy getServerPolicy() {
 		return serverPolicy;
 	}
 
 	public static ServerCommand serverCommand() {
 		return serverCommand;
 	}
 
 	public static MapToolServer getServer() {
 		return server;
 	}
 
 	public static void addPlayer(Player player) {
 		if (!playerList.contains(player)) {
 			playerList.add(player);
 
 			// LATER: Make this non-anonymous
 			playerList.sort(new Comparator<Player>() {
 				public int compare(Player arg0, Player arg1) {
 					return arg0.getName().compareToIgnoreCase(arg1.getName());
 				}
 			});
 
 			if (!player.equals(MapTool.getPlayer())) {
 				String msg = MessageFormat.format(I18N.getText("msg.info.playerConnected"), player.getName());
 				addLocalMessage("<span style='color:#0000ff'><i>" + msg + "</i></span>");
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
 			String msg = MessageFormat.format(I18N.getText("msg.info.playerDisconnected"), player.getName());
 			addLocalMessage("<span style='color:#0000ff'><i>" + msg + "</i></span>");
 		}
 	}
 
 	public static ObservableList<TextMessage> getMessageList() {
 		return messageList;
 	}
 
 	/**
 	 * These are the messages that originate from the server
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
 
 		// Flashing
 		if (!isInFocus()) {
 			taskbarFlasher.flash();
 		}
 		if (message.isWhisper()) {
 			setLastWhisperer(message.getSource());
 		}
 		messageList.add(message);
 	}
 
 	/**
 	 * These are the messages that are generated locally
 	 */
 	public static void addMessage(TextMessage message) {
 
 		// Filter stuff
 		addServerMessage(message);
 
 		if (!message.isMe()) {
 			serverCommand().message(message);
 		}
 	}
 
 	/**
 	 * Add a message only this client can see. This is a shortcut for
 	 * addMessage(ME, ...)
 	 * 
 	 * @param message
 	 */
 	public static void addLocalMessage(String message) {
 		addMessage(TextMessage.me(null, message));
 	}
 
 	public static Campaign getCampaign() {
 		if (campaign == null) {
 			campaign = new Campaign();
 		}
 		return campaign;
 	}
 
 	public static MapToolLineParser getParser() {
 		return parser;
 	}
 
 	public static void setCampaign(Campaign campaign) {
 		setCampaign(campaign, null);
 	}
 
 	public static void setCampaign(Campaign campaign, GUID defaultRendererId) {
 
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
 
 			if ((zone.getId().equals(defaultRendererId) || currRenderer == null)
 					&& (getPlayer().isGM() || zone.isVisible())) {
 				currRenderer = renderer;
 			}
 
 			eventDispatcher.fireEvent(ZoneEvent.Added, campaign, null, zone);
 		}
         clientFrame.getInitiativePanel().setOwnerPermissions(campaign.isInitiativeOwnerPermissions());
         clientFrame.getInitiativePanel().setMovementLock(campaign.isInitiativeMovementLock());
 		clientFrame.setCurrentZoneRenderer(currRenderer);
     	
     	AssetManager.updateRepositoryList();
     	MapTool.getFrame().getCampaignPanel().reset();
     	UserDefinedMacroFunctions.getInstance().loadCampaignLibFunctions();
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
 			showError("msg.error.alreadyRunningServer");
 			return;
 		}
 
 		assetTransferManager.flush();
 
 		// TODO: the client and server campaign MUST be different objects.
 		// Figure out a better init method
 		server = new MapToolServer(config, policy);
 		server.setCampaign(campaign);
 
 		serverPolicy = server.getPolicy();
 
 		if (announcer != null) {
 			announcer.stop();
 		}
 		// Don't announce personal servers
 		if (!config.isPersonalServer()) {
 			announcer = new ServiceAnnouncer(id, server.getConfig().getPort(),
 					AppConstants.SERVICE_GROUP);
 			announcer.start();
 		}
 
 		// Registered ?
 		if (config.isServerRegistered() && !config.isPersonalServer()) {
 			try {
 				int result = MapToolRegistry.registerInstance(config
 						.getServerName(), config.getPort());
 				if (result == 3) {
 					MapTool.showError("msg.error.alreadyRegistered");
 				}
 				// TODO: I don't like this
 			} catch (Exception e) {
 				MapTool.showError("msg.error.failedCannotRegisterServer", e);
 			}
 		}
 	}
 
 	public static ThumbnailManager getThumbnailManager() {
 		if (thumbnailManager == null) {
 			thumbnailManager = new ThumbnailManager(AppUtil.getAppHome("imageThumbs"), THUMBNAIL_SIZE);
 		}
 
 		return thumbnailManager;
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
 			if (ZoneFactory.DEFAULT_MAP_NAME.equals(singleZone.getName())&& singleZone.isEmpty()) {
 				removeZone(singleZone);
 			}
 		}
 
 		getCampaign().putZone(zone);
 
 		serverCommand().putZone(zone);
 
 		eventDispatcher.fireEvent(ZoneEvent.Added, getCampaign(), null, zone);
 
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
 		clientConn.addDisconnectHandler(new ServerDisconnectHandler());
 
 		clientConn.start();
 
 		// LATER: I really, really, really don't like this startup pattern
 		if (clientConn.isAlive()) {
 			conn = clientConn;
 		}
 
 		clientFrame.getLookupTablePanel().updateView();
 		clientFrame.getInitiativePanel().updateView();
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
 
 		// Close UPnP port mapping if used
 		StartServerDialogPreferences serverProps = new StartServerDialogPreferences();
 		if (serverProps.getUseUPnP()) {
 
 			int port = serverProps.getPort();
 			UPnPUtil.closePort(port);
 		}
 
 		boolean isPersonalServer = server != null && server.getConfig().isPersonalServer();
 
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
 				MapTool.showError("While unregistering server instance", t);
 			}
 		}
 
 		try {
 			conn.close();
 			conn = null;
 			playerList.clear();
 
 		} catch (IOException ioe) {
 			// This isn't critical, we're closing it anyway
 			log.debug("While closing connection", ioe);
 		}
 
 		MapTool.getFrame().getConnectionStatusPanel().setStatus(ConnectionStatusPanel.Status.disconnected);
 
 		if (!isPersonalServer) {
 			addLocalMessage("<span style='color:blue'><i>"
 					+ I18N.getText("msg.info.disconnected") + "</i></span>");
 		}
 	}
 
 	public static MapToolFrame getFrame() {
 		return clientFrame;
 	}
 
 	private static void configureLogging() {
 		
     	String logging = null;
     	try {
     		logging = new String(FileUtil.getBytes(MapTool.class.getClassLoader().getResourceAsStream("net/rptools/maptool/client/logging.xml")));
     	} catch (IOException e) {
     		System.err.println("Could not load logging file: " + e);
     		return;
     	}
 
         File localLoggingConfigFile = new File(AppUtil.getAppHome() + "/logging.xml");
         String localConfig = "";
         if (localLoggingConfigFile.exists()) {
         	try {
 				localConfig = new String(FileUtil.getBytes(localLoggingConfigFile));
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
         }
 
 		logging = logging.replace("INSERT_LOCAL_CONFIG_HERE", localConfig);
         logging = logging.replace("${appHome}", AppUtil.getAppHome().getAbsolutePath().replace('\\', '/'));
         
         // Configure
         new DOMConfigurator().doConfigure(new ByteArrayInputStream(logging.getBytes()), LogManager.getLoggerRepository());
 	}
 
 	private static final void configureJide() {
         LookAndFeelFactory.UIDefaultsCustomizer uiDefaultsCustomizer = new LookAndFeelFactory.UIDefaultsCustomizer() {
             public void customize(UIDefaults defaults) {
                 ThemePainter painter = (ThemePainter) UIDefaultsLookup.get("Theme.painter");
                 defaults.put("OptionPaneUI", "com.jidesoft.plaf.basic.BasicJideOptionPaneUI");
 
                 defaults.put("OptionPane.showBanner", Boolean.TRUE); // show banner or not. default is true
                 defaults.put("OptionPane.bannerIcon", new ImageIcon(MapTool.class.getClassLoader().getResource("net/rptools/maptool/client/image/maptool_icon.png")));
                 defaults.put("OptionPane.bannerFontSize", 13);
                 defaults.put("OptionPane.bannerFontStyle", Font.BOLD);
                 defaults.put("OptionPane.bannerMaxCharsPerLine", 60);
                 defaults.put("OptionPane.bannerForeground", painter != null ? painter.getOptionPaneBannerForeground() : null);  // you should adjust this if banner background is not the default gradient paint
                 defaults.put("OptionPane.bannerBorder", null); // use default border
 
                 // set both bannerBackgroundDk and // set both bannerBackgroundLt to null if you don't want gradient
                 defaults.put("OptionPane.bannerBackgroundDk", painter != null ? painter.getOptionPaneBannerDk() : null);
                 defaults.put("OptionPane.bannerBackgroundLt", painter != null ? painter.getOptionPaneBannerLt() : null);
                 defaults.put("OptionPane.bannerBackgroundDirection", Boolean.TRUE); // default is true
 
                 // optionally, you can set a Paint object for BannerPanel. If so, the three UIDefaults related to banner background above will be ignored.
                 defaults.put("OptionPane.bannerBackgroundPaint", null);
 
                 defaults.put("OptionPane.buttonAreaBorder", BorderFactory.createEmptyBorder(6, 6, 6, 6));
                 defaults.put("OptionPane.buttonOrientation", SwingConstants.RIGHT);
             }
         };
         uiDefaultsCustomizer.customize(UIManager.getDefaults());
 	}
 	
 	public static void main(String[] args) {
 
 		// Before anything else, create a place to store all the data
 		try {
 			AppUtil.getAppHome();
 		} catch (Throwable t) {
 			t.printStackTrace();
 
 			// Create an empty frame so there's something to click on if the dialog goes in the background
 			JFrame frame = new JFrame();
 			SwingUtil.centerOnScreen(frame);
 			frame.setVisible(true);
 			
 			JOptionPane.showMessageDialog(frame, t.getMessage(), "Error creating data dir", JOptionPane.ERROR_MESSAGE);
 			System.exit(1);
 		}
 		
 		configureLogging();
 		
 		// System properties
 		System.setProperty("swing.aatext", "true");
 		// System.setProperty("sun.java2d.opengl", "true");
 
 		final SplashScreen splash = new SplashScreen(SPLASH_IMAGE, getVersion());
 		splash.showSplashScreen();
 
 		// Protocol handlers
 		RPTURLStreamHandlerFactory factory = new RPTURLStreamHandlerFactory();
 		factory.registerProtocol("asset", new AssetURLStreamHandler());
 		URL.setURLStreamHandlerFactory(factory);
 
         Toolkit.getDefaultToolkit().getSystemEventQueue().push(new MapToolEventQueue());
 
         // LAF
         try {
         	
         	
         	// If we are running under Mac OS X then save native menu bar look & feel components
         	// Note the order of creation for the AppMenuBar, this specific chronology
         	// allows the system to set up system defaults before we go and modify things.
         	// That is, please don't move these lines around unless you test the result on windows and mac
         	if (MAC_OS_X) {
         		System.setProperty("apple.laf.useScreenMenuBar", "true");
         		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
         		menuBar = new AppMenuBar();
         		UIManager.setLookAndFeel("net.rptools.maptool.client.TinyLookAndFeelMac");
         	}
         	else {
         		UIManager.setLookAndFeel("de.muntjak.tinylookandfeel.TinyLookAndFeel");
        			menuBar = new AppMenuBar();
         	}
         	
     		com.jidesoft.utils.Lm.verifyLicense("Trevor Croft", "rptools", "5MfIVe:WXJBDrToeLWPhMv3kI2s3VFo");
     		LookAndFeelFactory.addUIDefaultsCustomizer(new LookAndFeelFactory.UIDefaultsCustomizer(){
     			public void customize(UIDefaults defaults) {
     				// Remove red border around menus
     				defaults.put("PopupMenu.foreground", Color.lightGray);
     			}
     		});
     		LookAndFeelFactory.installJideExtension(LookAndFeelFactory.XERTO_STYLE);
         	    		
         	// Make the toggle button pressed state look more distinct
         	Theme.buttonPressedColor[Theme.style] = new ColorReference(Color.gray);
         	
         	configureJide();
 		} catch (Exception e) {
 			MapTool.showError("msg.error.lafSetup", e);
 		}
 
 		// This is a tweak that makes the Chinese version work better
 		if (Locale.CHINA.equals(Locale.getDefault())) {
 			Font f = new Font("����宋体", Font.PLAIN, 12);
 			FontUIResource fontRes = new FontUIResource(f);
 			for (Enumeration keys = UIManager.getDefaults().keys(); keys.hasMoreElements();) {
 				Object key = keys.nextElement();
 				Object value = UIManager.get(key);
 				if (value instanceof FontUIResource)
 					UIManager.put(key, fontRes);
 			}
 		}
 
 		// Draw frame contents on resize
 		Toolkit.getDefaultToolkit().setDynamicLayout(true);
 
 		EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				initialize();
 
 				EventQueue.invokeLater(new Runnable() {
 					public void run() {
 						clientFrame.setVisible(true);
 
 						splash.hideSplashScreen();
 
 						EventQueue.invokeLater(new Runnable() {
 							public void run() {
 								postInitialize();
 							}
 						});
 					}
 				});
 
 			}
 		});
 
 		// new Thread(new HeapSpy()).start();
 	}
 
 	private static void postInitialize() {
 
 		// Check to see if there is an autosave file from mt crashing
 		getAutoSaveManager().check();
 		getAutoSaveManager().restart();
 
 		taskbarFlasher = new TaskBarFlasher(clientFrame);
 
 //		showWarning("WARNING!!! This is an experimental version. <p><p>It's possible that this version will hit a fringe case that will cause MT to lockup, without the ability to save.<p><p>Please test this build, but don't rely on it yet while we work out the kinks over the next build or two.<p><p><b>Please report any problems to the forums, or email to submit@rptools.net with the subject 'b57 Bug'</b>.<p><p>Happy Mapping :)<p><p>-Trev");
 	}
 
 	/**
 	 * Return whether the campaign file has changed
 	 */
 	public static boolean isCampaignDirty() {
 
 		// TODO: This is a very naive check, but it's better than nothing
 		if (getCampaign().getZones().size() == 1) {
 			Zone singleZone = MapTool.getCampaign().getZones().get(0);
 			if (ZoneFactory.DEFAULT_MAP_NAME.equals(singleZone.getName()) && singleZone.isEmpty()) {
 				return false;
 			}
 		} 
 		
 		return true;
 	}
 	
 	public static void setLastWhisperer(String lastWhisperer) {
 		if (lastWhisperer != null) {
 			MapTool.lastWhisperer = lastWhisperer;
 		}
 	}
 
 	public static String getLastWhisperer() {
 		return lastWhisperer;
 	}
 	
 	
 	public static boolean useToolTipsForUnformatedRolls() {
 		if (isPersonalServer()) {
 			return AppPreferences.getUseToolTipForInlineRoll();
 		} else {
 			return getServerPolicy().getUseToolTipsForDefaultRollFormat();
 		}
 	}
 
 	private static class ServerHeartBeatThread extends Thread {
 		@Override
 		public void run() {
 
 			// This should always run, so we should be able to safely
 			// loop forever
 			while (true) {
 				try {
 					Thread.sleep(20000);
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 
 				ServerCommand command = serverCommand;
 				if (command != null) {
 					command.heartbeat(getPlayer().getName());
 				}
 
 			}
 		}
 	}
 
 }
