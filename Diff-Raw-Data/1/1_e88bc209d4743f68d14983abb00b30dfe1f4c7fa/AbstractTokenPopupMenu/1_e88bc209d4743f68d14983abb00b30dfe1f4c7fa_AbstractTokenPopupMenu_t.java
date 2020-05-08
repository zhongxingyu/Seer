 package net.rptools.maptool.client.ui;
 
 import java.awt.Image;
 import java.awt.event.ActionEvent;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import javax.swing.AbstractAction;
 import javax.swing.Action;
 import javax.swing.JCheckBoxMenuItem;
 import javax.swing.JComponent;
 import javax.swing.JFileChooser;
 import javax.swing.JMenu;
 import javax.swing.JMenuItem;
 import javax.swing.JPopupMenu;
 import javax.swing.JTextPane;
 import javax.swing.KeyStroke;
 
 import net.rptools.maptool.client.AppUtil;
 import net.rptools.maptool.client.MapTool;
 import net.rptools.maptool.client.tool.FacingTool;
 import net.rptools.maptool.client.tool.PointerTool;
 import net.rptools.maptool.client.tool.StampTool;
 import net.rptools.maptool.client.ui.token.LightDialog;
 import net.rptools.maptool.client.ui.token.EditTokenDialog;
 import net.rptools.maptool.client.ui.zone.ZoneRenderer;
 import net.rptools.maptool.language.I18N;
 import net.rptools.maptool.model.AssetManager;
 import net.rptools.maptool.model.Direction;
 import net.rptools.maptool.model.GUID;
 import net.rptools.maptool.model.Grid;
 import net.rptools.maptool.model.LightSource;
 import net.rptools.maptool.model.Token;
 import net.rptools.maptool.model.TokenFootprint;
 import net.rptools.maptool.model.Zone;
 import net.rptools.maptool.util.ImageManager;
 import net.rptools.maptool.util.PersistenceUtil;
 import net.rptools.maptool.util.TokenUtil;
 
 public abstract class AbstractTokenPopupMenu extends JPopupMenu {
 
 	private boolean areTokensOwned;
 
 	private ZoneRenderer renderer;
 
 	int x, y;
 
 	Set<GUID> selectedTokenSet;
 
 	private Token tokenUnderMouse;
 
 	public AbstractTokenPopupMenu(Set<GUID> selectedTokenSet, int x, int y,
 			ZoneRenderer renderer, Token tokenUnderMouse) {
 		this.renderer = renderer;
 		this.x = x;
 		this.y = y;
 		this.selectedTokenSet = selectedTokenSet;
 		this.tokenUnderMouse = tokenUnderMouse;
 
 		setOwnership();
 	}
 	
 	protected boolean tokensAreOwned() {
 		return areTokensOwned;
 	}
 	
 	private void setOwnership() {
 		
 		areTokensOwned = true;
 		if (!MapTool.getPlayer().isGM()
 				&& MapTool.getServerPolicy().useStrictTokenManagement()) {
 			for (GUID tokenGUID : selectedTokenSet) {
 				Token token = getRenderer().getZone().getToken(tokenGUID);
 
 				if (!token.isOwner(MapTool.getPlayer().getName())) {
 					areTokensOwned = false;
 					break;
 				}
 			}
 		}
 
 	}
 
 	protected class ShowHandoutAction extends AbstractAction {
 		public ShowHandoutAction() {
 			putValue(Action.NAME, "Show Handout");
 			setEnabled(getTokenUnderMouse().getCharsheetImage() != null);
 		}
 		
 		public void actionPerformed(ActionEvent e) {
 
 			AssetViewerDialog dialog = new AssetViewerDialog(getTokenUnderMouse().getName() + "'s Character Sheet", getTokenUnderMouse().getCharsheetImage());
 			dialog.pack();
 			dialog.setVisible(true);
 		}
 		
 	}
 
 	protected JMenu createLightSourceMenu() {
 		JMenu menu = new JMenu("Light Source");
 
 		for (Entry<String, Map<GUID, LightSource>> entry : MapTool.getCampaign().getLightSourcesMap().entrySet()) {
 			JMenu subMenu = new JMenu(entry.getKey());
 			
 			List<LightSource> lightSourceList = new ArrayList<LightSource>(entry.getValue().values());
 			for (LightSource lightSource : lightSourceList) {
 				
 				JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem(new ToggleLightSourceAction(lightSource));
 				menuItem.setSelected(tokenUnderMouse.hasLightSource(lightSource));
 
 				subMenu.add(menuItem);
 			}
 			
 			menu.add(subMenu);
 			
 		}
 		
 		return menu;
 	}
 	
 	protected Token getTokenUnderMouse() {
 		return tokenUnderMouse;
 	}
 	
 	protected JMenu createFlipMenu() {
 		
 		JMenu flipMenu = new JMenu("Flip");
 		
 		flipMenu.add(new AbstractAction() {
 			{
 				putValue(NAME, "Horizontal");
 			}
 			public void actionPerformed(ActionEvent e) {
 				for (GUID tokenGUID : selectedTokenSet) {
 					Token token = renderer.getZone().getToken(tokenGUID);
 					if (token == null) {
 						continue;
 					}
 					
 					token.setFlippedX(!token.isFlippedX());
 					
 					renderer.flush(token);
 					MapTool.serverCommand().putToken(renderer.getZone().getId(), token);
 				}
 				MapTool.getFrame().refresh();
 			}
 		});
 		
 		flipMenu.add(new AbstractAction() {
 			{
 				putValue(NAME, "Vertical");
 			}
 			public void actionPerformed(ActionEvent e) {
 				for (GUID tokenGUID : selectedTokenSet) {
 					Token token = renderer.getZone().getToken(tokenGUID);
 					if (token == null) {
 						continue;
 					}
 					
 					token.setFlippedY(!token.isFlippedY());
 					renderer.flush(token);
 					MapTool.serverCommand().putToken(renderer.getZone().getId(), token);
 				}
 				MapTool.getFrame().refresh();
 			}
 		});
 		
 		return flipMenu;
 	}
 	
 	protected JMenu createChangeToMenu(Zone.Layer... types) {
 		
 		JMenu changeTypeMenu = new JMenu("Change to");
 		for (Zone.Layer layer : types) {
 			changeTypeMenu.add(new JMenuItem(new ChangeTypeAction(layer)));
 		}
 		return changeTypeMenu;
 	}
 
 //	protected JMenu createVisionMenu() {
 //		JMenu visionMenu = I18N.createMenu("defaultTool.visionMenu");
 //		
 //		if (selectedTokenSet.size() != 1) {
 //			visionMenu.setEnabled(false);
 //		} else {
 //
 //			for (final Vision vision : getTokenUnderMouse().getVisionList()) {
 //				JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem(vision.getLabel()); 
 //				menuItem.setSelected(vision.isEnabled());
 //				menuItem.addMouseListener( new MouseAdapter() {
 //					@Override
 //					public void mousePressed(MouseEvent e) {
 //						EventQueue.invokeLater(new Runnable() {
 //							public void run() {
 //								new VisionDialog(getRenderer().getZone(), getTokenUnderMouse(), vision).setVisible(true);
 //
 //								getRenderer().flush(getTokenUnderMouse());
 //								MapTool.serverCommand().putToken(renderer.getZone().getId(), getTokenUnderMouse());
 //								getRenderer().repaint();
 //							}
 //						});
 //					}
 //				});
 //
 //				visionMenu.add(menuItem);
 //			}
 //			
 //			JMenuItem newVisionMenuItem = new JMenuItem("New Vision ...");
 //			newVisionMenuItem.addActionListener(new ActionListener() {
 //				public void actionPerformed(ActionEvent e) {
 //					new VisionDialog(getRenderer().getZone(), getTokenUnderMouse()).setVisible(true);
 //					getRenderer().flush(getTokenUnderMouse());
 //				}
 //			});
 //			visionMenu.add(newVisionMenuItem);
 //		}
 //		
 //		return visionMenu;
 //	}
 	
 	protected JMenu createArrangeMenu() {
 		JMenu arrangeMenu = new JMenu("Arrange");
 		JMenuItem bringToFrontMenuItem = new JMenuItem("Bring to Front");
 		bringToFrontMenuItem.addActionListener(new BringToFrontAction());
 
 		JMenuItem sendToBackMenuItem = new JMenuItem("Send to Back");
 		sendToBackMenuItem.addActionListener(new SendToBackAction());
 
 		arrangeMenu.add(bringToFrontMenuItem);
 		arrangeMenu.add(sendToBackMenuItem);
 
 		return arrangeMenu;
 	}
 	
 	protected JMenu createSizeMenu() {
 		
 		JMenu sizeMenu = new JMenu("Size");
 		
 		JCheckBoxMenuItem freeSize = new JCheckBoxMenuItem(new FreeSizeAction());
 		freeSize.setSelected(!tokenUnderMouse.isSnapToScale());
 
 		sizeMenu.add(freeSize);
 		sizeMenu.addSeparator();
 		
 		Grid grid = renderer.getZone().getGrid();
 		for (TokenFootprint footprint : grid.getFootprints()) {
 			JMenuItem menuItem = new JCheckBoxMenuItem(new ChangeSizeAction(footprint));
 			if (tokenUnderMouse.isSnapToScale() && tokenUnderMouse.getFootprint(grid) == footprint) {
 				menuItem.setSelected(true);
 			}
 
 			sizeMenu.add(menuItem);
 		}
 
 		return sizeMenu;
 	}
 	
 	protected void addGMItem(Action action) {
 		if (action == null) {
 			return;
 		}
 		
 		if (MapTool.getPlayer().isGM()) {
 			add(new JMenuItem(action));
 		}
 	}
 
 	protected void addGMItem(JMenu menu) {
 		if (menu == null) {
 			return;
 		}
 		
 		if (MapTool.getPlayer().isGM()) {
 			add(menu);
 		}
 	}
 
 	protected void addToggledGMItem(Action action, boolean checked) {
 		if (action == null) {
 			return;
 		}
 		
 		if (MapTool.getPlayer().isGM()) {
 			JCheckBoxMenuItem item = new JCheckBoxMenuItem(action);
 			item.setSelected(checked);
 			add(item);
 		}
 	}
 
 	protected void addToggledOwnedItem(Action action, boolean checked) {
 		if (action == null) {
 			return;
 		}
 		
 		JCheckBoxMenuItem item = new JCheckBoxMenuItem(action);
 		item.setSelected(checked);
 		item.setEnabled(areTokensOwned);
 		add(item);
 	}
 
 	protected void addToggledItem(Action action, boolean checked) {
 		if (action == null) {
 			return;
 		}
 		
 		JCheckBoxMenuItem item = new JCheckBoxMenuItem(action);
 		item.setSelected(checked);
 		add(item);
 	}
 
 	protected void addOwnedItem(Action action) {
 		if (action == null) {
 			return;
 		}
 		
 		JMenuItem item = new JMenuItem(action);
 		item.setEnabled(areTokensOwned);
 		add(new JMenuItem(action));
 	}
 
 	protected void addOwnedItem(JMenu menu) {
 		if (menu == null) {
 			return;
 		}
 		
 		menu.setEnabled(areTokensOwned);
 		add(menu);
 	}
 
 	protected ZoneRenderer getRenderer() {
 		return renderer;
 	}
 	
 	public void showPopup(JComponent component) {
 		show(component, x, y);
 	}
 	
 	public class ChangeTypeAction extends AbstractAction{
 		
 		private Zone.Layer layer;
 		
 		public ChangeTypeAction(Zone.Layer layer) {
 			putValue(Action.NAME, layer.toString());
 			this.layer = layer;
 		}
 		
 		public void actionPerformed(ActionEvent e) {
 			
 			for (GUID tokenGUID : selectedTokenSet) {
 				Token token = renderer.getZone().getToken(tokenGUID);
 				if (token == null) {
 					continue;
 				}
 
 				token.setLayer(layer);
 				switch (layer) {
 				case BACKGROUND:
 				case OBJECT:
 					token.setShape(Token.TokenShape.TOP_DOWN);
 					break;
 				case TOKEN:
 					Image image = ImageManager.getImage(AssetManager.getAsset(token.getImageAssetId()));
 					if (image == null || image == ImageManager.UNKNOWN_IMAGE) {
 						token.setShape(Token.TokenShape.TOP_DOWN);
 					} else {
 						token.setShape(TokenUtil.guessTokenType(image));
 					}
 					break;
 				}
 				
 				renderer.flush(token);
 				MapTool.serverCommand().putToken(renderer.getZone().getId(), token);
 			}
 			
 			renderer.repaint();
 			MapTool.getFrame().updateTokenTree();
 		}
 	}
 	
 	public class FreeSizeAction extends AbstractAction {
 		
 		public FreeSizeAction() {
 			putValue(Action.NAME, tokenUnderMouse.isStamp() ? "Free Size" : "Native Size");
 		}
 		
 		public void actionPerformed(ActionEvent e) {
 
 			for (GUID tokenGUID : selectedTokenSet) {
 				Token token = renderer.getZone().getToken(tokenGUID);
 				if (token == null) {
 					continue;
 				}
 				
 				token.setSnapToScale(false);
 				MapTool.serverCommand().putToken(renderer.getZone().getId(), token);
 			}
 			
 			renderer.repaint();
 		}
 	}
 	
 	public class ToggleLightSourceAction extends AbstractAction {
 		
 		private LightSource lightSource;
 		public ToggleLightSourceAction(LightSource lightSource) {
 			super(lightSource.getName());
 			this.lightSource = lightSource;
 		}
 		
 		public void actionPerformed(ActionEvent e) {
 
 			ZoneRenderer renderer = MapTool.getFrame().getCurrentZoneRenderer();
 			for (GUID tokenGUID : selectedTokenSet) {
 				Token token = renderer.getZone().getToken(tokenGUID);
 				if (token == null) {
 					continue;
 				}
 				
 				if (token.hasLightSource(lightSource)) {
 					token.removeLightSource(lightSource);
 				} else {
 					token.addLightSource(lightSource, Direction.CENTER);
 				}
 				
 				// Cache clearing
 				renderer.flush(token);
 
 				MapTool.serverCommand().putToken(renderer.getZone().getId(), token);
				renderer.getZone().putToken(token);
 
 				renderer.repaint();
 			}
 			
 		}
 		
 	}
 
 	public class SaveAction extends AbstractAction {
 		
 		public SaveAction() {
 			super("Save");
 			
 			if (selectedTokenSet.size() > 1) {
 				setEnabled(false);
 			}
 		}
 		
 		public void actionPerformed(ActionEvent e) {
 			
 			JFileChooser chooser = MapTool.getFrame().getSaveFileChooser(); 
 			if (chooser.showSaveDialog(MapTool.getFrame()) != JFileChooser.APPROVE_OPTION) {
 				return;
 			}
 			
 			File file = chooser.getSelectedFile();
 			
 			// Auto-extension
 			if (!file.getName().endsWith(Token.FILE_EXTENSION)) {
 				file = new File(file.getAbsolutePath() + "." + Token.FILE_EXTENSION);
 			}
 			
 			// Confirm
 			if (file.exists()) {
 				if (!MapTool.confirm("File exists, would you like to overwrite?")) {
 					return;
 				}
 			}
 			
 			try {
 				PersistenceUtil.saveToken(tokenUnderMouse, file);
 			} catch (IOException ioe) {
 				ioe.printStackTrace();
 				MapTool.showError("Could not save token: " + ioe);
 			}
 		}
 	}
 	
 	public class SetFacingAction extends AbstractAction {
 		
 		public SetFacingAction() {
 			super("Set Facing");
 		}
 		
 		public void actionPerformed(ActionEvent e) {
 			
 			Toolbox toolbox = MapTool.getFrame().getToolbox(); 
 			
 			FacingTool tool = (FacingTool) toolbox.getTool(FacingTool.class);
 			tool.init(tokenUnderMouse, selectedTokenSet);
 			
 			toolbox.setSelectedTool(FacingTool.class);
 		}
 	}
 	
 	public class ClearFacingAction extends AbstractAction {
 		
 		public ClearFacingAction() {
 			super("Clear Facing");
 		}
 		
 		public void actionPerformed(ActionEvent e) {
 			
 			ZoneRenderer renderer = MapTool.getFrame().getCurrentZoneRenderer();
 			for (GUID tokenGUID : selectedTokenSet) {
 
 				Token token = renderer.getZone().getToken(tokenGUID);
 				token.setFacing(null);
 				renderer.flush(token);
 				MapTool.serverCommand().putToken(renderer.getZone().getId(), token);
 			}
 
 			renderer.repaint();
 		}
 	}
 	
 	public class SnapToGridAction extends AbstractAction {
 
 		private boolean snapToGrid;
 
 		private ZoneRenderer renderer;
 
 		public SnapToGridAction(boolean snapToGrid, ZoneRenderer renderer) {
 			super("Snap to grid");
 			this.snapToGrid = snapToGrid;
 			this.renderer = renderer;
 		}
 
 		public void actionPerformed(ActionEvent e) {
 
 			for (GUID guid : selectedTokenSet) {
 
 				Token token = renderer.getZone().getToken(guid);
 				if (token == null) {
 					continue;
 				}
 
 				token.setSnapToGrid(!snapToGrid);
 				MapTool.serverCommand().putToken(renderer.getZone().getId(),
 						token);
 			}
 		}
 	}
 
 	/**
 	 * Internal class used to handle token state changes.
 	 */
 	public class ChangeStateAction extends AbstractAction {
 
 		/**
 		 * Initialize a state action for a given state.
 		 * 
 		 * @param state
 		 *            The name of the state set when this action is executed
 		 */
 		public ChangeStateAction(String state) {
 			putValue(ACTION_COMMAND_KEY, state); // Set the state command
 
 			// Load the name, mnemonic, accelerator, and description if
 			// available
 			String key = "defaultTool.stateAction." + state;
 			String name = net.rptools.maptool.language.I18N.getText(key);
 			if (!name.equals(key)) {
 				putValue(NAME, name);
 				int mnemonic = I18N.getMnemonic(key);
 				if (mnemonic != -1)
 					putValue(MNEMONIC_KEY, mnemonic);
 				String accel = I18N.getAccelerator(key);
 				if (accel != null)
 					putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(accel));
 				String description = I18N.getDescription(key);
 				if (description != null)
 					putValue(SHORT_DESCRIPTION, description);
 			} else {
 
 				// Default name if no I18N set
 				putValue(NAME, state);
 			} // endif
 		}
 
 		/**
 		 * Set the state for all of the selected tokens.
 		 * 
 		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
 		 */
 		public void actionPerformed(ActionEvent aE) {
 			ZoneRenderer renderer = MapTool.getFrame().getCurrentZoneRenderer();
 			for (GUID tokenGUID : selectedTokenSet) {
 
 				Token token = renderer.getZone().getToken(tokenGUID);
 				if (aE.getActionCommand().equals("clear")) {
 					// Wipe out the entire state HashMap, this is what the
 					// previous
 					// code attempted to do but was failing due to the Set
 					// returned
 					// by getStatePropertyNames being a non-static view into a
 					// set.
 					// Removing items from the map was messing up the iteration.
 					// Here, clear all states, unfortunately, including light.
 					token.getStatePropertyNames().clear();
 				} else if (aE.getActionCommand().equals("light")) {
 					LightDialog.show(token, "light");
 				} else {
 					token
 							.setState(aE.getActionCommand(),
 									((JCheckBoxMenuItem) aE.getSource())
 											.isSelected() ? Boolean.TRUE : null);
 				}
 				
 				renderer.flush(token);
 				MapTool.serverCommand().putToken(renderer.getZone().getId(), token);
 			} 
 			renderer.repaint();
 		}
 	}
 
 	public class ChangeSizeAction extends AbstractAction {
 
 		private TokenFootprint footprint;
 
 		public ChangeSizeAction(TokenFootprint footprint) {
 			super(footprint.getName());
 			this.footprint = footprint;
 		}
 
 		/*
 		 * (non-Javadoc)
 		 * 
 		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
 		 */
 		public void actionPerformed(ActionEvent e) {
 
 			ZoneRenderer renderer = MapTool.getFrame().getCurrentZoneRenderer();
 			for (GUID tokenGUID : selectedTokenSet) {
 
 				Token token = renderer.getZone().getToken(tokenGUID);
 				token.setFootprint(renderer.getZone().getGrid(), footprint);
 				token.setSnapToScale(true);
 				renderer.flush(token);
 				MapTool.serverCommand().putToken(renderer.getZone().getId(), token);
 			}
 
 			renderer.repaint();
 		}
 
 	}
 
 	public class VisibilityAction extends AbstractAction {
 
 		{
 			putValue(Action.NAME, "Visible to players");
 		}
 		
 		public void actionPerformed(ActionEvent e) {
 
 			for (GUID guid : selectedTokenSet) {
 
 				Token token = renderer.getZone().getToken(guid);
 				if (token == null) {
 					continue;
 				}
 
 				token.setVisible(((JCheckBoxMenuItem) e.getSource()).isSelected());
 
 				MapTool.getFrame().updateTokenTree();
 				
 				MapTool.serverCommand().putToken(renderer.getZone().getId(),
 						token);
 			}
 
 			renderer.repaint();
 		}
 	}
 
 	public class BringToFrontAction extends AbstractAction {
 
 		public void actionPerformed(ActionEvent e) {
 
 			MapTool.serverCommand().bringTokensToFront(
 					renderer.getZone().getId(), selectedTokenSet);
 
 			MapTool.getFrame().refresh();
 		}
 	}
 
 	public class SendToBackAction extends AbstractAction {
 
 		public void actionPerformed(ActionEvent e) {
 
 			MapTool.serverCommand().sendTokensToBack(
 					renderer.getZone().getId(), selectedTokenSet);
 
 			MapTool.getFrame().refresh();
 		}
 	}
 
 	public class ImpersonateAction extends AbstractAction {
 		
 		public ImpersonateAction() {
 			putValue(Action.NAME, "Impersonate");
 		}
 		public void actionPerformed(ActionEvent e) {
 			JTextPane commandArea = MapTool.getFrame().getCommandPanel().getCommandTextArea();
 
 			commandArea.setText("/im " + tokenUnderMouse.getId());
 			MapTool.getFrame().getCommandPanel().commitCommand();
 			commandArea.requestFocusInWindow();
 		}
 	}
 	
 	public class StartMoveAction extends AbstractAction {
 
 		public StartMoveAction() {
 			putValue(Action.NAME, "Move");
 		}
 
 		public void actionPerformed(ActionEvent e) {
 
 			Tool tool = MapTool.getFrame().getToolbox().getSelectedTool();
 			if (tool instanceof PointerTool) {
 				((PointerTool)tool).startTokenDrag(tokenUnderMouse);
 			} else if (tool instanceof StampTool) {
 				((StampTool)tool).startTokenDrag(tokenUnderMouse);
 			}
 		}
 	}
 
 	public class ShowPropertiesDialogAction extends AbstractAction {
 		
 		public ShowPropertiesDialogAction() {
 			putValue(Action.NAME, "Edit ...");
 		}
 		
 		public void actionPerformed(ActionEvent e) {
 
 	      EditTokenDialog dialog = MapTool.getFrame().getTokenPropertiesDialog();
 	      dialog.showDialog(tokenUnderMouse);
 	      if (dialog.isTokenSaved()) {
 	    	  getRenderer().repaint();
 	    	  MapTool.serverCommand().putToken(getRenderer().getZone().getId(), getTokenUnderMouse());
 	    	  getRenderer().getZone().putToken(getTokenUnderMouse());
 	      }
 		}
 	}
 	
 	public class DeleteAction extends AbstractAction {
 
 		public DeleteAction() {
 			putValue(Action.NAME, "Delete");
 		}
 
 		public void actionPerformed(ActionEvent e) {
 
 			if (!MapTool
 					.confirm("Are you sure you want to delete the selected tokens ?")) {
 				return;
 			}
 
 			for (GUID tokenGUID : selectedTokenSet) {
 
 				Token token = renderer.getZone().getToken(tokenGUID);
 
 				if (AppUtil.playerOwns(token)) {
 					renderer.getZone().removeToken(tokenGUID);
 					MapTool.serverCommand().removeToken(
 							renderer.getZone().getId(), tokenGUID);
 				}
 			}
 		}
 	}
 }
