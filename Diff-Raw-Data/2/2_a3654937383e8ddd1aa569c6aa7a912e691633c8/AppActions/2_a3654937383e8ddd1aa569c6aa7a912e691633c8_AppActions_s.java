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
 
 import java.awt.Dimension;
 import java.awt.EventQueue;
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.Transparency;
 import java.awt.event.ActionEvent;
 import java.awt.image.BufferedImage;
 import java.io.BufferedInputStream;
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.IOException;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import javax.imageio.ImageIO;
 import javax.swing.AbstractAction;
 import javax.swing.Action;
 import javax.swing.ImageIcon;
 import javax.swing.JComponent;
 import javax.swing.JFileChooser;
 import javax.swing.JOptionPane;
 import javax.swing.KeyStroke;
 
 import net.rptools.lib.FileUtil;
 import net.rptools.lib.MD5Key;
 import net.rptools.lib.image.ImageUtil;
 import net.rptools.maptool.client.tool.GridTool;
 import net.rptools.maptool.client.ui.AppMenuBar;
 import net.rptools.maptool.client.ui.CampaignPropertiesDialog;
 import net.rptools.maptool.client.ui.ClientConnectionPanel;
 import net.rptools.maptool.client.ui.ConnectToServerDialog;
 import net.rptools.maptool.client.ui.ConnectionStatusPanel;
 import net.rptools.maptool.client.ui.ExportDialog;
 import net.rptools.maptool.client.ui.MapPropertiesDialog;
 import net.rptools.maptool.client.ui.PreferencesDialog;
 import net.rptools.maptool.client.ui.PreviewPanelFileChooser;
 import net.rptools.maptool.client.ui.ServerInfoDialog;
 import net.rptools.maptool.client.ui.StartServerDialog;
 import net.rptools.maptool.client.ui.StaticMessageDialog;
 import net.rptools.maptool.client.ui.MapToolFrame.MTFrame;
 import net.rptools.maptool.client.ui.assetpanel.AssetPanel;
 import net.rptools.maptool.client.ui.assetpanel.Directory;
 import net.rptools.maptool.client.ui.zone.ZoneRenderer;
 import net.rptools.maptool.client.ui.zone.ZoneView;
 import net.rptools.maptool.language.I18N;
 import net.rptools.maptool.model.Asset;
 import net.rptools.maptool.model.AssetManager;
 import net.rptools.maptool.model.Campaign;
 import net.rptools.maptool.model.CampaignFactory;
 import net.rptools.maptool.model.CellPoint;
 import net.rptools.maptool.model.ExportInfo;
 import net.rptools.maptool.model.GUID;
 import net.rptools.maptool.model.Player;
 import net.rptools.maptool.model.Token;
 import net.rptools.maptool.model.Zone;
 import net.rptools.maptool.model.ZoneFactory;
 import net.rptools.maptool.model.ZonePoint;
 import net.rptools.maptool.model.drawing.DrawableTexturePaint;
 import net.rptools.maptool.server.ServerConfig;
 import net.rptools.maptool.server.ServerPolicy;
 import net.rptools.maptool.util.ImageManager;
 import net.rptools.maptool.util.PersistenceUtil;
 import net.rptools.maptool.util.PersistenceUtil.PersistedCampaign;
 
 import com.jidesoft.docking.DockableFrame;
 
 /**
  */
 public class AppActions {
 
 	private static Set<Token> tokenCopySet = null;
 	
 	
 	public static final Action MRU_LIST = new DefaultClientAction() {
 		{
 			init("menu.recent");
 		}
 		
 		@Override
 		public boolean isAvailable() {
 			return MapTool.isHostingServer() || MapTool.isPersonalServer();
 		}
 
 		public void execute(ActionEvent ae) {
 			// Do nothing
 		}
 	};
 
 	public static final Action EXPORT_SCREENSHOT = new DefaultClientAction() {
 		{
 			init("action.exportScreenShotAs");
 		}
 
 		public void execute(ActionEvent e) {
 
 			ExportInfo exportInfo = MapTool.getCampaign().getExportInfo();
 			ExportDialog dialog = new ExportDialog(exportInfo);
 
 			dialog.setVisible(true);
 
 			exportInfo = dialog.getExportInfo();
 
 			if (exportInfo == null) {
 				return;
 			}
 
 			MapTool.getCampaign().setExportInfo(exportInfo);
 
 			exportScreenCap(exportInfo);
 		}
 	};
 
 	public static final Action EXPORT_SCREENSHOT_LAST_LOCATION = new DefaultClientAction() {
 		{
 			init("action.exportScreenShot");
 		}
 
 		public void execute(ActionEvent e) {
 
 			ExportInfo exportInfo = MapTool.getCampaign().getExportInfo();
 			if (exportInfo == null) {
 				EXPORT_SCREENSHOT.actionPerformed(e);
 				return;
 			}
 
 			exportScreenCap(exportInfo);
 		}
 	};
 
 	private static void exportScreenCap(ExportInfo exportInfo) {
 
 		BufferedImage screenCap = null;
 		
 		int role = exportInfo.getView() == ExportInfo.View.GM ? Player.Role.GM : Player.Role.PLAYER;
 		
 		switch (exportInfo.getType()) {
 		case ExportInfo.Type.CURRENT_VIEW:
 			screenCap = MapTool.takeMapScreenShot(new ZoneView(role));
 			if (screenCap == null) {
 				MapTool.getFrame().setStatusMessage("Could not get screencap");
 				return;
 			}
 			break;
 		case ExportInfo.Type.FULL_MAP:
 			break;
 		}
 
 		MapTool.getFrame().setStatusMessage("Saving screenshot ...");
 
 		try {
 
 			ByteArrayOutputStream imageOut = new ByteArrayOutputStream();
 
 			ImageIO.write(screenCap, "png", imageOut);
 
 			exportInfo.getLocation().putContent(
 					new BufferedInputStream(new ByteArrayInputStream(imageOut
 							.toByteArray())));
 
 			MapTool.getFrame().setStatusMessage("Saved screenshot");
 
 		} catch (IOException ioe) {
 			MapTool.showError("Could not export image: " + ioe);
 			ioe.printStackTrace();
 		} catch (Exception e) {
 			MapTool.showError("Could not export image: " + e);
 			e.printStackTrace();
 		}
 	}
 
 	public static final Action ENFORCE_ZONE = new AdminClientAction() {
 
 		{
 			init("action.enforceZone");
 		}
 
 		@Override
 		public boolean isAvailable() {
 			return super.isAvailable()
 					&& (MapTool.isPersonalServer() || MapTool.isHostingServer());
 		}
 
 		public void execute(ActionEvent e) {
 			ZoneRenderer renderer = MapTool.getFrame().getCurrentZoneRenderer();
 			if (renderer == null) {
 				return;
 			}
 
 			MapTool.serverCommand().enforceZone(renderer.getZone().getId());
 		}
 	};
 
 	public static final Action RESTORE_DEFAULT_IMAGES = new DefaultClientAction() {
 
 		{
 			init("action.restoreDefaultImages");
 		}
 
 		public void execute(ActionEvent e) {
 			try {
 				AppSetup.installDefaultTokens();
 
 				// TODO: Remove this hardwiring
 				File unzipDir = new File(AppConstants.UNZIP_DIR
 						.getAbsolutePath()
 						+ File.separator + "Default");
 				MapTool.getFrame().addAssetRoot(unzipDir);
 				AssetManager.searchForImageReferences(unzipDir,
 						AppConstants.IMAGE_FILE_FILTER);
 
 			} catch (IOException ioe) {
 				MapTool.showError("Could not restore defaults: " + ioe);
 			}
 		}
 	};
 	
 	public static final Action RENAME_ZONE = new AdminClientAction() {
 		
 		{
 			init("action.renameMap");
 		}
 		
 		@Override
 		public void execute(ActionEvent e) {
 
 			Zone zone = MapTool.getFrame().getCurrentZoneRenderer().getZone();
 			String name = JOptionPane.showInputDialog(MapTool.getFrame(), "Rename " + (zone.getName() != null ? '"' + zone.getName() + "'" : ""), zone.getName() != null ? zone.getName(): "");
 			if (name != null) {
 				zone.setName(name);
 				MapTool.serverCommand().renameZone(zone.getId(), name);
 			}
 		}
 	};
 
 	public static final Action SHOW_FULLSCREEN = new DefaultClientAction() {
 
 		{
 			init("action.fullscreen");
 		}
 
 		public void execute(ActionEvent e) {
 
 			if (MapTool.getFrame().isFullScreen()) {
 				MapTool.getFrame().showWindowed();
 			} else {
 				MapTool.getFrame().showFullScreen();
 			}
 		}
 	};
 
 	public static final Action SHOW_SERVER_INFO = new DefaultClientAction() {
 		{
 			init("action.showServerInfo");
 		}
 
 		@Override
 		public boolean isAvailable() {
 			return super.isAvailable()
 					&& (MapTool.isPersonalServer() || MapTool.isHostingServer());
 		}
 
 		public void execute(ActionEvent e) {
 
 			if (MapTool.getServer() == null) {
 				return;
 			}
 
 			ServerInfoDialog dialog = new ServerInfoDialog(MapTool.getServer());
 			dialog.setVisible(true);
 		}
 	};
 
 	public static final Action SHOW_PREFERENCES = new DefaultClientAction() {
 		{
 			init("action.preferences");
 		}
 
 		public void execute(ActionEvent e) {
 
 			// Probably don't have to create a new one each time
 			PreferencesDialog dialog = new PreferencesDialog();
 			dialog.setVisible(true);
 		}
 	};
 
 	public static final Action SAVE_MESSAGE_HISTORY = new DefaultClientAction() {
 		{
 			init("action.saveMessageHistory");
 		}
 
 		public void execute(ActionEvent e) {
 			String messageHistory = MapTool.getFrame().getCommandPanel()
 					.getMessageHistory();
 
 			JFileChooser chooser = MapTool.getFrame().getSaveFileChooser();
 			chooser.setDialogTitle("Save Message History");
 			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
 
 			if (chooser.showSaveDialog(MapTool.getFrame()) != JFileChooser.APPROVE_OPTION) {
 				return;
 			}
 
 			File saveFile = chooser.getSelectedFile();
 			if (saveFile.getName().indexOf(".") < 0) {
 				saveFile = new File(saveFile.getAbsolutePath() + ".html");
 			}
 			if (saveFile.exists()
 					&& !MapTool.confirm("File exists, overwrite?")) {
 				return;
 			}
 
 			try {
 				FileUtil.writeBytes(saveFile, messageHistory.getBytes());
 			} catch (IOException ioe) {
 				MapTool.showError("Could not save: " + ioe);
 			}
 		}
 	};
 	
 	public static final Action CUT_TOKENS = new DefaultClientAction() {
 		{
 			init("action.cutTokens");
 		}
 		
 		@Override
 		public boolean isAvailable() {
 			return super.isAvailable()
 					&& MapTool.getFrame().getCurrentZoneRenderer() != null;
 		}
 		
 		public void execute(ActionEvent e) {
 			
 			ZoneRenderer renderer = MapTool.getFrame().getCurrentZoneRenderer();
 			Set<GUID> selectedSet = renderer.getSelectedTokenSet();
 
 			copyTokens(selectedSet);
 			
 			// delete tokens
 			Zone zone = renderer.getZone();
 			
 			for (GUID tokenGUID : selectedSet) {
 				
 				Token token = zone.getToken(tokenGUID);
 				
 				if (AppUtil.playerOwns(token)) {
                     renderer.getZone().removeToken(tokenGUID);
                     MapTool.serverCommand().removeToken(renderer.getZone().getId(), tokenGUID);
 				}
 			}
 		
 			renderer.clearSelectedTokens();
 		}
 	};
 
 	public static final Action COPY_TOKENS = new DefaultClientAction() {
 		{
 			init("action.copyTokens");
 		}
 
 		@Override
 		public boolean isAvailable() {
 			return super.isAvailable()
 					&& MapTool.getFrame().getCurrentZoneRenderer() != null;
 		}
 
 		public void execute(ActionEvent e) {
 			ZoneRenderer renderer = MapTool.getFrame().getCurrentZoneRenderer();
 			copyTokens(renderer.getSelectedTokenSet());
 		}
 
 	};
 	
 	public static final void copyTokens(Set<GUID> tokenSet) {
 
 		List<Token> tokenList = new ArrayList<Token>();
 		
 		ZoneRenderer renderer = MapTool.getFrame().getCurrentZoneRenderer();
 		Zone zone = renderer.getZone();
 
 		Integer top = null;
 		Integer left = null;
 		tokenCopySet = new HashSet<Token>();
 		for (GUID guid : tokenSet) {
 			Token token = zone.getToken(guid);
 			if (token != null) {
 				tokenList.add(token);
 			}
 		}
 		
 		copyTokens(tokenList);
 	}
 
 	public static final void copyTokens(List<Token> tokenList) {
 
 		ZoneRenderer renderer = MapTool.getFrame().getCurrentZoneRenderer();
 
 		Integer top = null;
 		Integer left = null;
 		tokenCopySet = new HashSet<Token>();
 		for (Token token : tokenList) {
 
 			if (top == null || token.getY() < top) {
 				top = token.getY();
 			}
 			if (left == null || token.getX() < left) {
 				left = token.getX();
 			}
 
 			tokenCopySet.add(new Token(token));
 		}
 
 		// Normalize
 		for (Token token : tokenCopySet) {
 			token.setX(token.getX() - left);
 			token.setY(token.getY() - top);
 		}
 	}
 
 	public static final Action PASTE_TOKENS = new DefaultClientAction() {
 		{
 			init("action.pasteTokens");
 		}
 
 		@Override
 		public boolean isAvailable() {
 			return super.isAvailable() && tokenCopySet != null;
 		}
 
 		public void execute(ActionEvent e) {
 
 			ZoneRenderer renderer = MapTool.getFrame().getCurrentZoneRenderer();
 			Zone zone = renderer.getZone();
 
 			ScreenPoint screenPoint = renderer.getPointUnderMouse();
 			if (screenPoint == null) {
 				return;
 			}
 
 			boolean snapToGrid = false;
 			for (Token origToken : tokenCopySet) {
 				if (origToken.isSnapToGrid()) {
 					snapToGrid = true;
 				}
 			}
 
 			ZonePoint zonePoint = screenPoint.convertToZone(renderer);
 			if (snapToGrid) {
 
 				CellPoint cellPoint = zone.getGrid().convert(zonePoint);
				zonePoint = zone.getGrid().convert(cellPoint);
 			}
 
 			for (Token origToken : tokenCopySet) {
 
 				Token token = new Token(origToken);
 
 				token.setX(token.getX() + zonePoint.x);
 				token.setY(token.getY() + zonePoint.y);
 				
 				// paste into correct layer
 				token.setLayer(renderer.getActiveLayer());	
 
 				// check the token's name, don't change PC token names ... ever
 				if (origToken.getType() != Token.Type.PC) {
 					token.setName(MapToolUtil.nextTokenId(zone, token));
 				}
 
 				zone.putToken(token);
 				MapTool.serverCommand().putToken(zone.getId(), token);
 			}
 
 			renderer.repaint();
 		}
 	};
 
 	public static final Action REMOVE_ASSET_ROOT = new DefaultClientAction() {
 		{
 			init("action.removeAssetRoot");
 		}
 
 		public void execute(ActionEvent e) {
 
 			AssetPanel assetPanel = MapTool.getFrame().getAssetPanel();
 			Directory dir = assetPanel.getSelectedAssetRoot();
 
 			if (dir == null) {
 				MapTool.showError("msg.error.mustSelectAssetGroupFirst");
 				return;
 			}
 
 			if (!assetPanel.isAssetRoot(dir)) {
 				MapTool.showError("msg.error.mustSelectRootGroup");
 				return;
 			}
 
 			AppPreferences.removeAssetRoot(dir.getPath());
 			assetPanel.removeAssetRoot(dir);
 		}
 
 	};
 	
 	public static final Action BOOT_CONNECTED_PLAYER = new DefaultClientAction() {
 		{
 			init("action.bootConnectedPlayer");
 		}
 		
 		@Override
 		public boolean isAvailable() {
 			return MapTool.isHostingServer() || MapTool.getPlayer().isGM();
 		}
 		
 		public void execute(ActionEvent e) {
 			ClientConnectionPanel panel = MapTool.getFrame().getConnectionPanel();
 			Player selectedPlayer = (Player)panel.getSelectedValue();
 			
 			if (selectedPlayer == null) {
 				MapTool.showError("msg.error.mustSelectPlayerFirst");
 				return;
 			}
 			
 			if(MapTool.getPlayer().equals(selectedPlayer)) {
 				MapTool.showError("msg.error.cantBootSelf");
 				return;
 			}
 			
 			if(MapTool.isPlayerConnected(selectedPlayer.getName()) &&
 					MapTool.confirm("Are you sure you want to boot " + 
 						selectedPlayer.getName() + "?")) {
 				MapTool.serverCommand().bootPlayer(selectedPlayer.getName());
 				MapTool.showInformation(selectedPlayer.getName() + " has been disconnected.");
 				return;
 			}
 			
 			MapTool.showError("msg.error.failedToBoot");
 
 		}
 	};
 
 	public static final Action TOGGLE_LINK_PLAYER_VIEW = new AdminClientAction() {
 		{
 			init("action.linkPlayerView");
 		}
 
 		@Override
 		public boolean isSelected() {
 			return AppState.isPlayerViewLinked();
 		}
 
 		public void execute(ActionEvent e) {
 
 			AppState.setPlayerViewLinked(!AppState.isPlayerViewLinked());
 			if (AppState.isPlayerViewLinked()) {
 				ZoneRenderer renderer = MapTool.getFrame()
 						.getCurrentZoneRenderer();
 				ZonePoint zp = new ScreenPoint(renderer.getWidth() / 2,
 						renderer.getHeight() / 2).convertToZone(renderer);
 				MapTool.serverCommand().enforceZoneView(
 						renderer.getZone().getId(), zp.x, zp.y,
 						renderer.getScaleIndex());
 			}
 		}
 
 	};
 
 	public static final Action TOGGLE_SHOW_PLAYER_VIEW = new AdminClientAction() {
 		{
 			init("action.showPlayerView");
 		}
 
 		@Override
 		public boolean isSelected() {
 			return AppState.isShowAsPlayer();
 		}
 
 		public void execute(ActionEvent e) {
 
 			AppState.setShowAsPlayer(!AppState.isShowAsPlayer());
 			MapTool.getFrame().refresh();
 		}
 
 	};
 
 	public static final Action TOGGLE_SHOW_MOVEMENT_MEASUREMENTS = new DefaultClientAction() {
 		{
 			init("action.showMovementMeasures");
 		}
 
 		@Override
 		public boolean isSelected() {
 			return AppState.getShowMovementMeasurements();
 		}
 
 		public void execute(ActionEvent e) {
 
 			AppState.setShowMovementMeasurements(!AppState
 					.getShowMovementMeasurements());
 			if (MapTool.getFrame().getCurrentZoneRenderer() != null) {
 				MapTool.getFrame().getCurrentZoneRenderer().repaint();
 			}
 		}
 
 	};
 
 	public static final Action TOGGLE_SHOW_LIGHT_RADIUS = new DefaultClientAction() {
 		{
 			init("action.showLightRadius");
 		}
 
 		@Override
 		public boolean isSelected() {
 			return AppState.isShowLightRadius();
 		}
 
 		public void execute(ActionEvent e) {
 
 			AppState.setShowLightRadius(!AppState.isShowLightRadius());
 			if (MapTool.getFrame().getCurrentZoneRenderer() != null) {
 				MapTool.getFrame().getCurrentZoneRenderer().repaint();
 			}
 		}
 
 	};
 	
 	public static final Action COPY_ZONE = new ZoneAdminClientAction() {
 		{
 			init("action.copyZone");
 		}
 
 		public void execute(ActionEvent e) {
 
 			Zone zone = MapTool.getFrame().getCurrentZoneRenderer().getZone();
 			String zoneName = JOptionPane.showInputDialog("New map name:", "Copy of " + zone.getName());
 			if (zoneName != null) {
 				Zone zoneCopy = new Zone(zone);
 				zoneCopy.setName(zoneName);
 				MapTool.addZone(zoneCopy);
 			}
 		}
 
 	};
 
 	public static final Action REMOVE_ZONE = new ZoneAdminClientAction() {
 		{
 			init("action.removeZone");
 		}
 
 		public void execute(ActionEvent e) {
 
 			if (!MapTool.confirm("msg.confirm.removeZone")) {
 				return;
 			}
 
 			ZoneRenderer renderer = MapTool.getFrame().getCurrentZoneRenderer();
 			MapTool.removeZone(renderer.getZone());
 		}
 
 	};
 
 	public static final Action SHOW_ABOUT = new DefaultClientAction() {
 		{
 			init("action.showAboutDialog");
 		}
 
 		public void execute(ActionEvent e) {
 
 			MapTool.getFrame().showAboutDialog();
 		}
 
 	};
 
 	public static final Action ENFORCE_ZONE_VIEW = new ZoneAdminClientAction() {
 		{
 			init("action.enforceView");
 		}
 
 		public void execute(ActionEvent e) {
 
 			ZoneRenderer renderer = MapTool.getFrame().getCurrentZoneRenderer();
 			if (renderer == null) {
 				return;
 			}
 
 			ZonePoint zp = new ScreenPoint(renderer.getWidth() / 2, renderer
 					.getHeight() / 2).convertToZone(renderer);
 			MapTool.serverCommand().enforceZoneView(renderer.getZone().getId(),
 					zp.x, zp.y, renderer.getScaleIndex());
 		}
 
 	};
 
 	/**
 	 * Start entering text into the chat field
 	 */
 	public static final String CHAT_COMMAND_ID = "action.sendChat";
 
 	public static final Action CHAT_COMMAND = new DefaultClientAction() {
 		{
 			init(CHAT_COMMAND_ID);
 		}
 
 		public void execute(ActionEvent e) {
 			if (!MapTool.getFrame().isCommandPanelVisible()) {
 				MapTool.getFrame().showCommandPanel();
 				MapTool.getFrame().getCommandPanel().startChat();
 			} else {
 				MapTool.getFrame().hideCommandPanel();
 			}
 		}
 	};
 
 	public static final String COMMAND_UP_ID = "action.commandUp";
 
 	public static final String COMMAND_DOWN_ID = "action.commandDown";
 
 	/**
 	 * Start entering text into the chat field
 	 */
 	public static final String ENTER_COMMAND_ID = "action.runMacro";
 
 	public static final Action ENTER_COMMAND = new DefaultClientAction() {
 		{
 			init(ENTER_COMMAND_ID);
 		}
 
 		public void execute(ActionEvent e) {
 			MapTool.getFrame().getCommandPanel().startMacro();
 		}
 	};
 
 	/**
 	 * Action tied to the chat field to commit the command.
 	 */
 	public static final String COMMIT_COMMAND_ID = "action.commitCommand";
 
 	public static final Action COMMIT_COMMAND = new DefaultClientAction() {
 		{
 			init(COMMIT_COMMAND_ID);
 		}
 
 		public void execute(ActionEvent e) {
 			MapTool.getFrame().getCommandPanel().commitCommand();
 		}
 	};
 
 	/**
 	 * Action tied to the chat field to commit the command.
 	 */
 	public static final String CANCEL_COMMAND_ID = "action.cancelCommand";
 
 	public static final Action CANCEL_COMMAND = new DefaultClientAction() {
 		{
 			init(CANCEL_COMMAND_ID);
 		}
 
 		public void execute(ActionEvent e) {
 			MapTool.getFrame().getCommandPanel().cancelCommand();
 		}
 	};
 
 	public static final Action RANDOMLY_ADD_LAST_ASSET = new DeveloperClientAction() {
 		{
 			init("action.debug.duplicateLastIcon");
 		}
 
 		public void execute(ActionEvent e) {
 
 			Asset asset = AssetManager.getLastRetrievedAsset();
 			for (int i = 0; i < 100; i++) {
 
 				Token token = new Token(asset.getId());
 				token.setX(MapToolUtil.getRandomNumber(100 * 5));
 				token.setY(MapToolUtil.getRandomNumber(100 * 5));
 				MapTool.getFrame().getCurrentZoneRenderer().getZone().putToken(
 						token);
 			}
 			MapTool.getFrame().getCurrentZoneRenderer().repaint();
 		}
 
 	};
 
 	public static final Action ADJUST_GRID = new ZoneAdminClientAction() {
 		{
 			init("action.adjustGrid");
 		}
 
 		public void execute(ActionEvent e) {
 
 			MapTool.getFrame().getToolbox().setSelectedTool(GridTool.class);
 		}
 
 	};
 
 	public static final Action TOGGLE_GRID = new DefaultClientAction() {
 		{
 			init("action.showGrid");
 			putValue(Action.SHORT_DESCRIPTION, getValue(Action.NAME));
 			try {
 				putValue(Action.SMALL_ICON, new ImageIcon(ImageUtil
 						.getImage("net/rptools/maptool/client/image/grid.gif")));
 			} catch (IOException ioe) {
 				ioe.printStackTrace();
 			}
 		}
 
 		public boolean isSelected() {
 			return AppState.isShowGrid();
 		}
 
 		public void execute(ActionEvent e) {
 
 			AppState.setShowGrid(!AppState.isShowGrid());
 
 			if (MapTool.getFrame().getCurrentZoneRenderer() != null) {
 				MapTool.getFrame().getCurrentZoneRenderer().repaint();
 			}
 		}
 	};
 
 	public static final Action TOGGLE_FOG = new ZoneAdminClientAction() {
 		{
 			init("action.enableFogOfWar");
 		}
 
 		@Override
 		public boolean isSelected() {
 			ZoneRenderer renderer = MapTool.getFrame().getCurrentZoneRenderer();
 			if (renderer == null) {
 				return false;
 			}
 
 			return renderer.getZone().hasFog();
 		}
 
 		public void execute(ActionEvent e) {
 
 			ZoneRenderer renderer = MapTool.getFrame().getCurrentZoneRenderer();
 			if (renderer == null) {
 				return;
 			}
 
 			Zone zone = renderer.getZone();
 			zone.setHasFog(!zone.hasFog());
 
 			MapTool.serverCommand().setZoneHasFoW(zone.getId(), zone.hasFog());
 
 			renderer.repaint();
 		}
 	};
 
 	public static final Action TOGGLE_SHOW_TOKEN_NAMES = new DefaultClientAction() {
 		{
 			init("action.showNames");
 			putValue(Action.SHORT_DESCRIPTION, getValue(Action.NAME));
 			try {
 				putValue(
 						Action.SMALL_ICON,
 						new ImageIcon(
 								ImageUtil
 										.getImage("net/rptools/maptool/client/image/names.png")));
 			} catch (IOException ioe) {
 				ioe.printStackTrace();
 			}
 		}
 
 		public void execute(ActionEvent e) {
 
 			AppState.setShowTokenNames(!AppState.isShowTokenNames());
 			if (MapTool.getFrame().getCurrentZoneRenderer() != null) {
 				MapTool.getFrame().getCurrentZoneRenderer().repaint();
 			}
 		}
 	};
 
 	public static final Action TOGGLE_CURRENT_ZONE_VISIBILITY = new ZoneAdminClientAction() {
 
 		{
 			init("action.hideMap");
 		}
 
 		@Override
 		public boolean isSelected() {
 			ZoneRenderer renderer = MapTool.getFrame().getCurrentZoneRenderer();
 			if (renderer == null) {
 				return false;
 			}
 			return !renderer.getZone().isVisible();
 		}
 
 		public void execute(ActionEvent e) {
 
 			ZoneRenderer renderer = MapTool.getFrame().getCurrentZoneRenderer();
 			if (renderer == null) {
 				return;
 			}
 
 			// TODO: consolidate this code with ZonePopupMenu
 			Zone zone = renderer.getZone();
 			zone.setVisible(!zone.isVisible());
 
 			MapTool.serverCommand().setZoneVisibility(zone.getId(),
 					zone.isVisible());
 			MapTool.getFrame().getZoneMiniMapPanel().flush();
 			MapTool.getFrame().repaint();
 		}
 	};
 
 	public static final Action NEW_CAMPAIGN = new AdminClientAction() {
 		{
 			init("action.newCampaign");
 		}
 
 		public void execute(ActionEvent e) {
 
 			if (!MapTool.confirm("msg.confirm.newCampaign")) {
 
 				return;
 			}
 
 			Campaign campaign = CampaignFactory.createBasicCampaign();
 			AppState.setCampaignFile(null);
 			MapTool.setCampaign(campaign);
 			MapTool.serverCommand().setCampaign(campaign);
 			
 			ImageManager.flush();
 			MapTool.getFrame().setCurrentZoneRenderer(MapTool.getFrame().getZoneRenderer(campaign.getZones().get(0)));
 		}
 	};
 
 	public static final Action ZOOM_IN = new DefaultClientAction() {
 		{
 			init("action.zoomIn");
 		}
 
 		public void execute(ActionEvent e) {
 			ZoneRenderer renderer = MapTool.getFrame().getCurrentZoneRenderer();
 			if (renderer != null) {
 				Dimension size = renderer.getSize();
 				renderer.zoomIn(size.width / 2, size.height / 2);
 
 				if (AppState.isPlayerViewLinked()) {
 					ZonePoint zp = new ScreenPoint(renderer.getWidth() / 2,
 							renderer.getHeight() / 2).convertToZone(renderer);
 					MapTool.serverCommand().enforceZoneView(
 							renderer.getZone().getId(), zp.x, zp.y,
 							renderer.getScaleIndex());
 				}
 			}
 		}
 	};
 
 	public static final Action ZOOM_OUT = new DefaultClientAction() {
 		{
 			init("action.zoomOut");
 		}
 
 		public void execute(ActionEvent e) {
 			ZoneRenderer renderer = MapTool.getFrame().getCurrentZoneRenderer();
 			if (renderer != null) {
 				Dimension size = renderer.getSize();
 				renderer.zoomOut(size.width / 2, size.height / 2);
 			}
 			if (AppState.isPlayerViewLinked()) {
 				ZonePoint zp = new ScreenPoint(renderer.getWidth() / 2,
 						renderer.getHeight() / 2).convertToZone(renderer);
 				MapTool.serverCommand().enforceZoneView(
 						renderer.getZone().getId(), zp.x, zp.y,
 						renderer.getScaleIndex());
 			}
 		}
 	};
 
 	public static final Action ZOOM_RESET = new DefaultClientAction() {
 		
 		private Integer lastZoom;
 		
 		{
 			init("action.zoom100");
 		}
 
 		public void execute(ActionEvent e) {
 			ZoneRenderer renderer = MapTool.getFrame().getCurrentZoneRenderer();
 			if (renderer != null) {
 
 				int scale = renderer.getScaleIndex();
 
 				// Revert to last zoom if we have one, but don't if the user has manually 
 				// changed the scale since the last reset zoom (one to one index)
 				if (lastZoom != null && renderer.getZoneScale().getOneToOneScaleIndex() == scale) {
 					// Go back to the previous zoom
 					renderer.setScaleIndex(lastZoom);
 					
 					// But make sure the next time we'll go back to 1:1
 					lastZoom = null;
 				} else {
 					lastZoom = renderer.getScaleIndex();
 					renderer.zoomReset();
 				}
 			}
 			if (AppState.isPlayerViewLinked()) {
 				ZonePoint zp = new ScreenPoint(renderer.getWidth() / 2,
 						renderer.getHeight() / 2).convertToZone(renderer);
 				MapTool.serverCommand().enforceZoneView(
 						renderer.getZone().getId(), zp.x, zp.y,
 						renderer.getScaleIndex());
 			}
 		}
 	};
 
 	public static final Action TOGGLE_ZONE_SELECTOR = new DefaultClientAction() {
 		{
 			init("action.showMapSelector");
 		}
 
 		@Override
 		public boolean isSelected() {
 			return MapTool.getFrame().getZoneMiniMapPanel().isVisible();
 		}
 
 		public void execute(ActionEvent e) {
 
 			JComponent panel = MapTool.getFrame().getZoneMiniMapPanel();
 
 			panel.setVisible(!panel.isVisible());
 		}
 	};
 
 	public static final Action TOGGLE_MOVEMENT_LOCK = new AdminClientAction() {
 		{
 			init("action.toggleMovementLock");
 		}
 
 		@Override
 		public boolean isSelected() {
 			return MapTool.getServerPolicy().isMovementLocked();
 		}
 
 		public void execute(ActionEvent e) {
 
 			ServerPolicy policy = MapTool.getServerPolicy();
 			policy.setIsMovementLocked(!policy.isMovementLocked());
 
 			MapTool.updateServerPolicy(policy);
 		}
 	};
 
 	public static final Action START_SERVER = new ClientAction() {
 		{
 			init("action.serverStart");
 		}
 
 		@Override
 		public boolean isAvailable() {
 			return MapTool.isPersonalServer();
 		}
 
 		public void execute(ActionEvent e) {
 
 			runBackground(new Runnable() {
 				public void run() {
 
 					if (!MapTool.isPersonalServer()) {
 						MapTool.showError("Already running a server.");
 						return;
 					}
 
 					// TODO: Need to shut down the existing server first;
 					StartServerDialog dialog = new StartServerDialog();
 
 					dialog.setVisible(true);
 
 					if (dialog.getOption() == StartServerDialog.OPTION_CANCEL) {
 						return;
 					}
 
 					ServerPolicy policy = new ServerPolicy();
 					policy.setUseStrictTokenManagement(dialog.getUseStrictOwnershipCheckbox().isSelected());
 					policy.setPlayersCanRevealVision(dialog.getPlayersCanRevealVisionCheckbox().isSelected());
 
 					ServerConfig config = new ServerConfig(dialog.getGMPasswordTextField().getText(), dialog.getPlayerPasswordTextField().getText(),
 							dialog.getPort(), dialog.getRPToolsNameTextField().getText(), dialog.getRPToolsPrivateCheckbox().isSelected());
 
 					// Use the existing campaign
 					Campaign campaign = MapTool.getCampaign();
 
 					boolean failed = false;
 					try {
 						ServerDisconnectHandler.disconnectExpected = true;
 						MapTool.stopServer();
 						MapTool.startServer(dialog.getUsernameTextField().getText(), config, policy, campaign);
 
 						// Connect to server
 						MapTool.createConnection("localhost", dialog.getPort(),
 								new Player(dialog.getUsernameTextField().getText(), dialog.getRole(), dialog.getGMPasswordTextField().getText()));
 
 						// connecting
 						MapTool.getFrame().getConnectionStatusPanel().setStatus(ConnectionStatusPanel.Status.server);
 					} catch (UnknownHostException uh) {
 						MapTool
 								.showError("Whoah, 'localhost' is not a valid address.  Weird.");
 						failed = true;
 					} catch (IOException ioe) {
 						MapTool
 								.showError("Could not connect to server: "
 										+ ioe);
 						failed = true;
 					}
 
 					if (failed) {
 						try {
 							MapTool.startPersonalServer(campaign);
 						} catch (IOException ioe) {
 							MapTool
 									.showError("Could not restart personal server");
 						}
 					}
 
 					MapTool.serverCommand().setCampaign(campaign);
 				}
 			});
 		}
 
 	};
 
 	public static final Action CONNECT_TO_SERVER = new ClientAction() {
 		{
 			init("action.clientConnect");
 		}
 
 		@Override
 		public boolean isAvailable() {
 			return MapTool.isPersonalServer();
 		}
 
 		public void execute(ActionEvent e) {
 
 			final ConnectToServerDialog dialog = new ConnectToServerDialog();
 
 			dialog.setVisible(true);
 
 			if (dialog.getOption() == ConnectToServerDialog.OPTION_CANCEL) {
 
 				return;
 			}
 
 			ServerDisconnectHandler.disconnectExpected = true;
 			MapTool.stopServer();
 			
 			// Install a temporary gimped campaign until we get the one from the server
 			final Campaign oldCampaign = MapTool.getCampaign();
 			MapTool.setCampaign(new Campaign());
 
 			// connecting
 			MapTool.getFrame().getConnectionStatusPanel().setStatus(
 					ConnectionStatusPanel.Status.connected);
 
 			// Show the user something interesting until we've got the campaign
 			// Look in ClientMethodHandler.setCampaign() for the corresponding hideGlassPane
 			StaticMessageDialog progressDialog = new StaticMessageDialog("Connecting");
 			MapTool.getFrame().showFilledGlassPane(progressDialog);
 
 			runBackground(new Runnable() {
 
 				public void run() {
 					boolean failed = false;
 					try {
 						MapTool.createConnection(dialog.getServer(), dialog
 								.getPort(), new Player(dialog.getUsername(),
 								dialog.getRole(), dialog.getPassword()));
 
 					} catch (UnknownHostException e1) {
 						MapTool.showError("Unknown host");
 						failed = true;
 					} catch (IOException e1) {
 						MapTool.showError("IO Error: " + e1);
 						failed = true;
 					}
 					
 					if (failed || MapTool.getConnection() == null) {
 						MapTool.getFrame().hideGlassPane();
 						try {
 							MapTool.startPersonalServer(oldCampaign);
 						} catch (IOException ioe) {
 							MapTool.showError("Could not restart personal server");
 						}
 					}
 				}
 			});
 
 		}
 
 	};
 
 	public static final Action DISCONNECT_FROM_SERVER = new ClientAction() {
 
 		{
 			init("action.clientDisconnect");
 		}
 
 		@Override
 		public boolean isAvailable() {
 			return !MapTool.isPersonalServer();
 		}
 
 		public void execute(ActionEvent e) {
 
 			if (MapTool.isHostingServer()
 					&& !MapTool.confirm("msg.confirm.hostingDisconnect")) {
 				return;
 			}
 
 			disconnectFromServer();
 		}
 
 	};
 	
 	public static void disconnectFromServer() {
 		Campaign campaign = MapTool.isHostingServer() ? MapTool.getCampaign() : CampaignFactory.createBasicCampaign();
 		ServerDisconnectHandler.disconnectExpected = true;
 		MapTool.stopServer();
 		MapTool.disconnect();
 
 		try {
 			MapTool.startPersonalServer(campaign);
 		} catch (IOException ioe) {
 			MapTool.showError("Could not restart personal server");
 		}
 	}
 
 	public static final Action LOAD_CAMPAIGN = new DefaultClientAction() {
 		{
 			init("action.loadCampaign");
 		}
 
 		@Override
 		public boolean isAvailable() {
 			return MapTool.isHostingServer() || MapTool.isPersonalServer();
 		}
 
 		public void execute(ActionEvent ae) {
 
 			JFileChooser chooser = new CampaignPreviewFileChooser();
 			chooser.setDialogTitle("Load Campaign");
 			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
 
 			if (chooser.showOpenDialog(MapTool.getFrame()) == JFileChooser.APPROVE_OPTION) {
 				File campaignFile = chooser.getSelectedFile();
 				loadCampaign(campaignFile);
 			}
 		}
 	};
 	
 	
 	private static class CampaignPreviewFileChooser extends PreviewPanelFileChooser {
 		
 		@Override
 		protected File getImageFileOfSelectedFile() {
 			if (getSelectedFile() == null) {
 				return null;
 			}
 			return PersistenceUtil.getCampaignThumbnailFile(getSelectedFile().getName());
 		}
 	}
 	
 	public static void loadCampaign(final File campaignFile) {
 		
 		new Thread() {
 			public void run() {
 
 				try {
 					StaticMessageDialog progressDialog = new StaticMessageDialog(
 							"Loading Campaign");
 
 					try {
 						// I'm going to get struck by lighting for
 						// writing code like this.
 						// CLEAN ME CLEAN ME CLEAN ME ! I NEED A
 						// SWINGWORKER !
 						MapTool.getFrame().showFilledGlassPane(progressDialog);
 
 						final PersistedCampaign campaign = PersistenceUtil.loadCampaign(campaignFile);
 						
 						if (campaign != null) {
 
 							AppState.setCampaignFile(campaignFile);
 							AppPreferences.setLoadDir(campaignFile.getParentFile());
 							
 							AppMenuBar.getMruManager().addMRUCampaign(campaignFile);
 
 							// Bypass the serialization when we are hosting the server
 							// TODO: This optimization doesn't work since the player name isn't the right thing to exclude this thread
 //							if (MapTool.isHostingServer() || MapTool.isPersonalServer()) {
 //								MapTool.getServer().getMethodHandler().handleMethod(MapTool.getPlayer().getName(), ServerCommand.COMMAND.setCampaign.name(), new Object[]{campaign.campaign});
 //							} else {
 								MapTool.serverCommand().setCampaign(campaign.campaign);
 //							}
 							MapTool.setCampaign(campaign.campaign);
 							
 							MapTool.getAutoSaveManager().restart();
 							MapTool.getAutoSaveManager().tidy();
 									
 							// Flush the images associated with the current campaign
 							// Do this juuuuuust before we get ready to show the new campaign, since we
 							// don't want the old campaign reloading images while we loaded the new campaign
 							ImageManager.flush();
 
 							if (campaign.currentZoneId != null) {
 								MapTool.getFrame().setCurrentZoneRenderer(MapTool.getFrame().getZoneRenderer(campaign.currentZoneId));
 
 								// TODO: This is wrong
 								if (campaign.currentView != null && MapTool.getFrame().getCurrentZoneRenderer() != null) {
 									MapTool.getFrame().getCurrentZoneRenderer().setZoneScale(campaign.currentView);
 								}
 							}			
 						}
 
 					} finally {
 						MapTool.getFrame().hideGlassPane();
 					}
 
 				} catch (IOException ioe) {
 					MapTool
 							.showError("Could not load campaign: "
 									+ ioe);
 				}
 			}
 		}.start();
 	}
 
 	public static final Action SAVE_CAMPAIGN = new DefaultClientAction() {
 		{
 			init("action.saveCampaign");
 		}
 
 		@Override
 		public boolean isAvailable() {
 			return (MapTool.isHostingServer() || MapTool.getPlayer().isGM());
 		}
 
 		public void execute(ActionEvent ae) {
 
 			if (AppState.getCampaignFile() == null) {
 				SAVE_CAMPAIGN_AS.actionPerformed(ae);
 				return;
 			}
 
 			// save to same place
 			Campaign campaign = MapTool.getCampaign();
 
 			try {
 				PersistenceUtil.saveCampaign(campaign, AppState
 						.getCampaignFile());
 				AppMenuBar.getMruManager().addMRUCampaign(AppState.getCampaignFile());
 				MapTool.showInformation("msg.info.campaignSaved");
 			} catch (IOException ioe) {
 				MapTool.showError("Could not save campaign: " + ioe);
 			}
 		}
 	};
 
 	public static final Action SAVE_CAMPAIGN_AS = new DefaultClientAction() {
 		{
 			init("action.saveCampaignAs");
 		}
 
 		@Override
 		public boolean isAvailable() {
 			return MapTool.isHostingServer() || MapTool.getPlayer().isGM();
 		}
 
 		public void execute(ActionEvent ae) {
 
 			Campaign campaign = MapTool.getCampaign();
 
 			JFileChooser chooser = MapTool.getFrame().getSaveFileChooser();
 			chooser.setDialogTitle("Save Campaign");
 
 			if (chooser.showSaveDialog(MapTool.getFrame()) == JFileChooser.APPROVE_OPTION) {
 
 				try {
 					File campaignFile = chooser.getSelectedFile();
 					if (campaignFile.getName().indexOf(".") < 0) {
 						campaignFile = new File(campaignFile.getAbsolutePath()
 								+ ".cmpgn");
 					}
 
 					PersistenceUtil.saveCampaign(campaign, campaignFile);
 
 					AppState.setCampaignFile(campaignFile);
 					AppPreferences.setSaveDir(campaignFile.getParentFile());
 					AppMenuBar.getMruManager().addMRUCampaign(AppState.getCampaignFile());
 					MapTool.showInformation("msg.info.campaignSaved");
 				} catch (IOException ioe) {
 					MapTool.showError("Could not save campaign: " + ioe);
 				}
 			}
 		}
 	};
 
 	public static final Action CAMPAIGN_PROPERTIES = new DefaultClientAction() {
 		{
 			init("action.campaignProperties");
 		}
 
 		@Override
 		public boolean isAvailable() {
 			return MapTool.getPlayer().isGM();
 		}
 
 		public void execute(ActionEvent ae) {
 
 			Campaign campaign = MapTool.getCampaign();
 
 			// TODO: There should probably be only one of these
 			CampaignPropertiesDialog dialog = new CampaignPropertiesDialog(MapTool.getFrame());
 			dialog.setCampaign(campaign);
 			
 			dialog.setVisible(true);
 			
 			if (dialog.getStatus() == CampaignPropertiesDialog.Status.CANCEL) {
 				return;
 			}
 
 			// TODO: Make this pass all properties, but we don't have that framework yet, so send what we 
 			// know the old fashioned way
 			MapTool.serverCommand().updateCampaign(Campaign.DEFAULT_TOKEN_PROPERTY_TYPE, campaign.getTokenPropertyList(Campaign.DEFAULT_TOKEN_PROPERTY_TYPE));
 		}
 	};
 
 	public static class GridSizeAction extends DefaultClientAction {
 
 		private int size;
 
 		public GridSizeAction(int size) {
 			putValue(Action.NAME, Integer.toString(size));
 			this.size = size;
 		}
 
 		@Override
 		public boolean isSelected() {
 			return AppState.getGridSize() == size;
 		}
 
 		@Override
 		public void execute(ActionEvent arg0) {
 			AppState.setGridSize(size);
 			MapTool.getFrame().refresh();
 		}
 	}
 
 	private static final int QUICK_MAP_ICON_SIZE = 25;
 
 	public static class QuickMapAction extends AdminClientAction {
 
 		private MD5Key assetId;
 
 		public QuickMapAction(String name, File imagePath) {
 
 			try {
 				Asset asset = new Asset(name, FileUtil.loadFile(imagePath));
 				assetId = asset.getId();
 
 				// Make smaller
 				BufferedImage iconImage = new BufferedImage(
 						QUICK_MAP_ICON_SIZE, QUICK_MAP_ICON_SIZE,
 						Transparency.OPAQUE);
 				Image image = MapTool.getThumbnailManager().getThumbnail(imagePath);
 
 				Graphics2D g = iconImage.createGraphics();
 				g.drawImage(image, 0, 0, QUICK_MAP_ICON_SIZE,
 						QUICK_MAP_ICON_SIZE, null);
 				g.dispose();
 
 				putValue(Action.SMALL_ICON, new ImageIcon(iconImage));
 				putValue(Action.NAME, name);
 
 				// Put it in the cache for easy access
 				AssetManager.putAsset(asset);
 
 				// But don't use up any extra memory
 				AssetManager.removeAsset(asset.getId());
 			} catch (IOException ioe) {
 				ioe.printStackTrace();
 			}
 			getActionList().add(this);
 		}
 
 		public void execute(java.awt.event.ActionEvent e) {
 
 			runBackground(new Runnable() {
 
 				public void run() {
 
 					Asset asset = AssetManager.getAsset(assetId);
 
 					Zone zone = ZoneFactory.createZone();
 					zone.setBackgroundPaint(new DrawableTexturePaint(asset.getId()));
 					zone.setName(asset.getName());
 
 					MapTool.addZone(zone);
 				}
 			});
 		}
 	};
 
 	public static final Action NEW_MAP = new AdminClientAction() {
 		{
 			init("action.newMap");
 		}
 
 		public void execute(java.awt.event.ActionEvent e) {
 
 			runBackground(new Runnable() {
 
 				public void run() {
 
 					Zone zone = ZoneFactory.createZone();
 					MapPropertiesDialog newMapDialog = new MapPropertiesDialog(MapTool.getFrame());
 					newMapDialog.setZone(zone);
 
 					newMapDialog.setVisible(true);
 					
 					if (newMapDialog.getStatus() == MapPropertiesDialog.Status.OK) {
 						MapTool.addZone(zone);
 					}
 				}
 			});
 		}
 	};
 
 	public static final Action ADD_ASSET_PANEL = new DefaultClientAction() {
 		{
 			init("action.addIconSelector");
 		}
 
 		public void execute(ActionEvent e) {
 
 			runBackground(new Runnable() {
 
 				public void run() {
 
 					JFileChooser chooser = MapTool.getFrame()
 							.getLoadFileChooser();
 					chooser.setDialogTitle("Load Asset Tree");
 					chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
 
 					if (chooser.showOpenDialog(MapTool.getFrame()) != JFileChooser.APPROVE_OPTION) {
 						return;
 					}
 
 					File root = chooser.getSelectedFile();
 					MapTool.getFrame().addAssetRoot(root);
 					AssetManager.searchForImageReferences(root,
 							AppConstants.IMAGE_FILE_FILTER);
 
 					AppPreferences.addAssetRoot(root);
 				}
 
 			});
 		}
 	};
 
 	public static final Action EXIT = new DefaultClientAction() {
 		{
 			init("action.exit");
 		}
 
 		public void execute(ActionEvent ae) {
 
 			if(!MapTool.getFrame().confirmClose()) {
 				return;
 			}
 			else {
 				MapTool.getFrame().closingMaintenance();
 				System.exit(0);
 			}
 		}
 	};
 
 	/**
 	 * Toggle the drawing of measurements.
 	 */
 	public static final Action TOGGLE_DRAW_MEASUREMENTS = new DefaultClientAction() {
 		{
 			init("action.toggleDrawMeasuements");
 		}
 
 		@Override
 		public boolean isSelected() {
 			return MapTool.getFrame().isPaintDrawingMeasurement();
 		}
 
 		public void execute(ActionEvent ae) {
 			MapTool.getFrame().setPaintDrawingMeasurement(
 					!MapTool.getFrame().isPaintDrawingMeasurement());
 		}
 	};
 
 	/**
 	 * Toggle drawing straight lines at double width on the line tool.
 	 */
 	public static final Action TOGGLE_DOUBLE_WIDE = new DefaultClientAction() {
 		{
 			init("action.toggleDoubleWide");
 		}
 
 		@Override
 		public boolean isSelected() {
 			return AppState.useDoubleWideLine();
 		}
 
 		public void execute(ActionEvent ae) {
 
 			AppState.setUseDoubleWideLine(!AppState.useDoubleWideLine());
 			if (MapTool.getFrame() != null
 					&& MapTool.getFrame().getCurrentZoneRenderer() != null)
 				MapTool.getFrame().getCurrentZoneRenderer().repaint();
 		}
 	};
 
 	public static class ToggleWindowAction extends ClientAction {
 
 		private MTFrame mtFrame;
 		
 		public ToggleWindowAction(MTFrame mtFrame) {
 			this.mtFrame = mtFrame;
 			init(mtFrame.toString());
 		}
 		
 		@Override
 		public boolean isSelected() {
 			return MapTool.getFrame().getFrame(mtFrame).isVisible();
 		}
 		
 		@Override
 		public boolean isAvailable() {
 			return true;
 		}
 		
 		@Override
 		public void execute(ActionEvent event) {
 			DockableFrame frame = MapTool.getFrame().getFrame(mtFrame);
 			if (frame.isVisible()) {
 				MapTool.getFrame().getDockingManager().hideFrame(mtFrame.name());
 			} else {
 				MapTool.getFrame().getDockingManager().showFrame(mtFrame.name());
 			}
 		}
 	}
 	
 	private static List<ClientAction> actionList;
 
 	private static List<ClientAction> getActionList() {
 		if (actionList == null) {
 			actionList = new ArrayList<ClientAction>();
 		}
 
 		return actionList;
 	}
 
 	public static void updateActions() {
 
 		for (ClientAction action : actionList) {
 			action.setEnabled(action.isAvailable());
 		}
 
 		MapTool.getFrame().getToolbox().updateTools();
 	}
 
 	public static abstract class ClientAction extends AbstractAction {
 
 		public void init(String key) {
 			String name = net.rptools.maptool.language.I18N.getText(key);
 			putValue(NAME, name);
 			int mnemonic = I18N.getMnemonic(key);
 			if (mnemonic != -1) {
 				putValue(MNEMONIC_KEY, mnemonic);
 			}
 
 			String accel = I18N.getAccelerator(key);
 			if (accel != null) {
 				putValue(ACCELERATOR_KEY, KeyStroke.getAWTKeyStroke(accel));
 			}
 			String description = I18N.getDescription(key);
 			if (description != null) {
 				putValue(SHORT_DESCRIPTION, description);
 			}
 
 			getActionList().add(this);
 		}
 
 		public abstract boolean isAvailable();
 
 		public boolean isSelected() {
 			return false;
 		}
 
 		public final void actionPerformed(ActionEvent e) {
 			execute(e);
 			// System.out.println(getValue(Action.NAME));
 			updateActions();
 		}
 
 		public abstract void execute(ActionEvent e);
 
 		public void runBackground(final Runnable r) {
 			new Thread() {
 				public void run() {
 					try {
 						MapTool.startIndeterminateAction();
 						r.run();
 					} finally {
 						MapTool.endIndeterminateAction();
 					}
 
 					updateActions();
 				}
 			}.start();
 		}
 	}
 
 	public static abstract class AdminClientAction extends ClientAction {
 
 		@Override
 		public boolean isAvailable() {
 			return MapTool.getPlayer().isGM();
 		}
 	}
 
 	public static abstract class ZoneAdminClientAction extends
 			AdminClientAction {
 
 		@Override
 		public boolean isAvailable() {
 			return super.isAvailable()
 					&& MapTool.getFrame().getCurrentZoneRenderer() != null;
 		}
 	}
 
 	public static abstract class DefaultClientAction extends ClientAction {
 
 		@Override
 		public boolean isAvailable() {
 			return true;
 		}
 	}
 
 	public static abstract class DeveloperClientAction extends ClientAction {
 
 		@Override
 		public boolean isAvailable() {
 			return System.getProperty("MAPTOOL_DEV") != null
 					&& "true".equals(System.getProperty("MAPTOOL_DEV"));
 		}
 	}
 	
 	public static class OpenMRUCampaign extends AbstractAction {
 		
 		private File campaignFile;
 
 		public OpenMRUCampaign(File file, int position) {
 			campaignFile = file;
 			String label = position + " " + campaignFile.getName();
 			putValue(Action.NAME, label);
 			
 			if (position <= 9) {
 				int keyCode = KeyStroke.getKeyStroke(Integer.toString(position)).getKeyCode();
 				putValue(Action.MNEMONIC_KEY, keyCode);
 			}
 			
 			// Use the saved campaign thumbnail as a tooltip
 			File thumbFile = PersistenceUtil.getCampaignThumbnailFile(campaignFile.getName());
 			String htmlTip;
 			
 			if (thumbFile.exists()){
 				htmlTip = "<html><img src=\"file:///" + thumbFile.getPath() + "\"></html>";
 			}
 			else {
 				htmlTip = "No preview available";
 			}
 			
 			/* There is some extra space appearing to the right of the images, which
 			* sounds similar to what was reported in this bug (bottom half):
 			* http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=5047379
 			* Removing the mnemonic will remove this extra space.
 			*/
 			putValue(Action.SHORT_DESCRIPTION, htmlTip);
 		}
 		
 		public void actionPerformed(ActionEvent ae) {
 			AppActions.loadCampaign(campaignFile);			
 		}
 	}
 
 }
