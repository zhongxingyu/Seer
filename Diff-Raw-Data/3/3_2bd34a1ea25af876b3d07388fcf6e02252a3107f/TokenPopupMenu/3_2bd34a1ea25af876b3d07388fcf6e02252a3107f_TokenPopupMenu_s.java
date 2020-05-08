 package net.rptools.maptool.client.ui;
 
 import java.awt.Color;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Set;
 
 import javax.swing.AbstractAction;
 import javax.swing.Action;
 import javax.swing.JCheckBoxMenuItem;
 import javax.swing.JComponent;
 import javax.swing.JMenu;
 import javax.swing.JMenuItem;
 import javax.swing.JSeparator;
 import javax.swing.JTextArea;
 import javax.swing.KeyStroke;
 
 import net.rptools.maptool.client.AppActions;
 import net.rptools.maptool.client.MapTool;
 import net.rptools.maptool.client.ui.token.LightDialog;
 import net.rptools.maptool.client.ui.token.TokenStates;
 import net.rptools.maptool.client.ui.zone.FogUtil;
 import net.rptools.maptool.client.ui.zone.ZoneRenderer;
 import net.rptools.maptool.language.I18N;
 import net.rptools.maptool.model.GUID;
 import net.rptools.maptool.model.Path;
 import net.rptools.maptool.model.Player;
 import net.rptools.maptool.model.Token;
 import net.rptools.maptool.model.Zone;
 import net.rptools.maptool.model.ZonePoint;
 
 public class TokenPopupMenu extends AbstractTokenPopupMenu {
 
 	
 	// TODO: This is temporary
 	private static final Object[][] HALO_COLORS = new Object[][] {
 		{"Black", Color.black},
 		{"Green", Color.green},
 		{"Yellow", Color.yellow},
 		{"Orange", new Color(255, 156, 0)}, // default orange is too light
 		{"Red", Color.red}
 	};
 	
 	public TokenPopupMenu(Set<GUID> selectedTokenSet, int x, int y,
 			ZoneRenderer renderer, Token tokenUnderMouse) {
 		super(selectedTokenSet, x, y, renderer, tokenUnderMouse);
 
 		add(new SetFacingAction());
 		add(new ClearFacingAction());
 		add(new StartMoveAction());
 		addOwnedItem(createMacroMenu());
 		addOwnedItem(createSpeechMenu());
 		addOwnedItem(createStateMenu());
 		addOwnedItem(createFlipMenu());
 		add(new JSeparator());
 
 		if (MapTool.getPlayer().isGM() || MapTool.getServerPolicy().getPlayersCanRevealVision()) {
 			
 			add(createExposeMenu());
 
 			if (MapTool.getPlayer().isGM()) {
 				addGMItem(createVisionMenu());
 			}
 			add(new JSeparator());
 		}
 
 		addToggledItem(new ShowPathsAction(), renderer.isPathShowing(tokenUnderMouse));
 		add(new RevertLastMoveAction());
 		addToggledGMItem(new VisibilityAction(), tokenUnderMouse.isVisible());
 		add(createHaloMenu());
 		add(new ChangeStateAction("light"));
 		addOwnedItem(createArrangeMenu());
 		
 		add(new JSeparator());
 		
 		add(AppActions.CUT_TOKENS);
 		add(AppActions.COPY_TOKENS);
 		
 		add(new JSeparator());
 
 		addOwnedItem(createSizeMenu(false));
 
 		add(new JSeparator());
 
 		add(new JMenuItem(new DeleteAction()));
 
 		add(new JSeparator());
 
 		addGMItem(createChangeToMenu(Zone.Layer.OBJECT, Zone.Layer.BACKGROUND));
 		add(new ShowPropertiesDialogAction());
 	}
 	
 	private JMenu createMacroMenu() {
 		
 		if (selectedTokenSet.size() != 1 || getTokenUnderMouse().getMacroNames().size() == 0) {
 			return null;
 		}
 		
 		JMenu menu = new JMenu("Macros");
 		List<String> keyList = new ArrayList<String>(getTokenUnderMouse().getMacroNames());
 		Collections.sort(keyList);
 		for (String key : keyList) {
 			menu.add(new RunMacroAction(key, getTokenUnderMouse().getMacro(key)));
 		}
 		
 		return menu;
 	}
 	
 	private JMenu createSpeechMenu() {
 		
 		if (selectedTokenSet.size() != 1 || getTokenUnderMouse().getSpeechNames().size() == 0) {
 			return null;
 		}
 		
 		JMenu menu = new JMenu("Speech");
 		List<String> keyList = new ArrayList<String>(getTokenUnderMouse().getSpeechNames());
 		Collections.sort(keyList);
 		for (String key : keyList) {
 			menu.add(new SayAction(key, getTokenUnderMouse().getSpeech(key)));
 		}
 		
 		return menu;
 	}
 	
 	private JMenu createExposeMenu() {
 		
 		JMenu menu = new JMenu("Expose");
 		menu.add(new ExposeVisibleAreaAction());
 		menu.add(new ExposeLastPathAction());
 		
 		if (getTokenUnderMouse().hasOwners() && MapTool.getPlayer().isGM()) {
 			menu.add(new ExposeVisibleAreaOnlyAction());
 		}
 		
 		menu.setEnabled(getTokenUnderMouse().hasVision());
 		
 		return menu;
 	}
 	
 	private class ExposeVisibleAreaAction extends AbstractAction {
 		public ExposeVisibleAreaAction() {
 			putValue(Action.NAME, "Visible area (Ctrl - I)");
 		}
 		
 		public void actionPerformed(ActionEvent e) {
 
 			FogUtil.exposeVisibleArea(getRenderer(), selectedTokenSet);
 			getRenderer().repaint();
 		}
 		
 	}
 
 	private class ExposeVisibleAreaOnlyAction extends AbstractAction {
 		public ExposeVisibleAreaOnlyAction() {
 			putValue(Action.NAME, "Player visible area only");
 		}
 		
 		public void actionPerformed(ActionEvent e) {
 
 			FogUtil.exposePCArea(getRenderer());
 			getRenderer().repaint();
 		}
 		
 	}
 
 	private class ExposeLastPathAction extends AbstractAction {
 		public ExposeLastPathAction() {
 			putValue(Action.NAME, "Last path (Ctrl - P)");
 			setEnabled(getTokenUnderMouse().getLastPath() != null);
 		}
 		
 		public void actionPerformed(ActionEvent e) {
 
 			FogUtil.exposeLastPath(getRenderer(), selectedTokenSet);
 			getRenderer().repaint();
 		}
 		
 	}
 
 	private JMenu createHaloMenu() {
 		JMenu haloMenu = new JMenu("Halo");
 
 		Color selectedColor = getTokenUnderMouse().getHaloColor();
 
 		JCheckBoxMenuItem noneMenu = new JCheckBoxMenuItem(new SetHaloAction(getRenderer(), selectedTokenSet, null, "None"));
 		if (selectedColor == null) {
 			noneMenu.setSelected(true);
 		}
 		haloMenu.add(noneMenu);
 		
 		haloMenu.add(new JSeparator());
 
 		for (Object[] row : HALO_COLORS) {
 			String name = (String)row[0];
 			Color color = (Color)row[1];
 			JCheckBoxMenuItem item = new JCheckBoxMenuItem(new SetHaloAction(getRenderer(), selectedTokenSet, color, name));
 			if (color.equals(selectedColor)) {
 				item.setSelected(true);
 			}
 			haloMenu.add(item);
 		}
 		
 		return haloMenu;
 	}
 	
 	private JMenu createStateMenu() {
 		JMenu stateMenu = I18N.createMenu("defaultTool.stateMenu");
 		stateMenu.add(new ChangeStateAction("clear"));
 		stateMenu.addSeparator();
 		for (String state : TokenStates.getStates()) {
 			createStateItem(state, stateMenu, getTokenUnderMouse());
 		}
 
 		return stateMenu;
 	}
 	
 
 	protected void addOwnedToggledItem(Action action, boolean checked) {
 		if (action == null) {
 			return;
 		}
 		
 		JCheckBoxMenuItem item = new JCheckBoxMenuItem(action);
 		item.setSelected(checked);
 		item.setEnabled(tokensAreOwned());
 		add(item);
 	}
 
 	public void showPopup(JComponent component) {
 		show(component, x, y);
 	}
 	
 	private static class PlayerOwnershipMenu extends JCheckBoxMenuItem
 			implements ActionListener {
 
 		private Set<GUID> tokenSet;
 
 		private Zone zone;
 
 		private boolean selected;
 
 		private String name;
 
 		public PlayerOwnershipMenu(String name, boolean selected,
 				Set<GUID> tokenSet, Zone zone) {
 			super(name, selected);
 			this.tokenSet = tokenSet;
 			this.zone = zone;
 			this.selected = selected;
 			this.name = name;
 
 			addActionListener(this);
 		}
 
 		public void actionPerformed(ActionEvent e) {
 			for (GUID guid : tokenSet) {
 				Token token = zone.getToken(guid);
 
 				if (selected) {
 					for (Player player : (Iterable<Player>) MapTool
 							.getPlayerList()) {
 						token.addOwner(player.getName());
 					}
 					token.removeOwner(name);
 				} else {
 					token.addOwner(name);
 				}
 
 				MapTool.serverCommand().putToken(zone.getId(), token);
 			}
 			MapTool.getFrame().updateTokenTree();
 		}
 	}
 	
 	/**
 	 * Create a radio button menu item for a particuar state
 	 * 
 	 * @param state
 	 *            Create the item for this state
 	 * @param menu
 	 *            The menu containing all items.
 	 * @return A menu item for the passed state.
 	 */
 	private JCheckBoxMenuItem createStateItem(String state, JMenu menu,
 			Token token) {
 		JCheckBoxMenuItem item = new JCheckBoxMenuItem(new ChangeStateAction(
 				state));
 		Object value = token.getState(state);
 		if (value != null && value instanceof Boolean
 				&& ((Boolean) value).booleanValue())
 			item.setSelected(true);
 		menu.add(item);
 		return item;
 	}
 
 	private class SetHaloAction extends AbstractAction {
 		
 		private Color color;
 		private Set<GUID> tokenSet;
 		private ZoneRenderer renderer;
 		
 		public SetHaloAction(ZoneRenderer renderer, Set<GUID> tokenSet, Color color, String name) {
 			this.color = color;
 			this.tokenSet = tokenSet;
 			this.renderer = renderer;
 			
 			putValue(Action.NAME, name);
 		}
 		
 		public void actionPerformed(ActionEvent e) {
 
 			Zone zone = renderer.getZone();
 			for (GUID guid : tokenSet) {
 				Token token = zone.getToken(guid);
 
 				token.setHaloColor(color);
 
 				MapTool.serverCommand().putToken(zone.getId(), token);
 			}
 			MapTool.getFrame().updateTokenTree();
 			renderer.repaint();
 		}
 	}
 
 	/**
 	 * Internal class used to handle token state changes.
 	 */
 	private class ChangeStateAction extends AbstractAction {
 
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
 				} // endif
 				MapTool.serverCommand().putToken(renderer.getZone().getId(),
 						token);
 			} // endfor
 			renderer.repaint();
 		}
 	}
 
 	private class AllOwnershipAction extends AbstractAction {
 
 		public void actionPerformed(ActionEvent e) {
 			Zone zone = getRenderer().getZone();
 
 			for (GUID tokenGUID : selectedTokenSet) {
 				Token token = zone.getToken(tokenGUID);
 				if (token != null) {
 					token.setAllOwners();
 					MapTool.serverCommand().putToken(zone.getId(), token);
 				}
 			}
 		}
 	}
 
 	private class RemoveAllOwnershipAction extends AbstractAction {
 
 		public void actionPerformed(ActionEvent e) {
 			Zone zone = getRenderer().getZone();
 
 			for (GUID tokenGUID : selectedTokenSet) {
 				Token token = zone.getToken(tokenGUID);
 				if (token != null) {
 					token.clearAllOwners();
 					MapTool.serverCommand().putToken(zone.getId(), token);
 				}
 			}
 		}
 	}
 
 	private class ShowPathsAction extends AbstractAction {
 		public ShowPathsAction() {
 			putValue(Action.NAME, "Show Path");
 		}
 		public void actionPerformed(ActionEvent e) {
 			
 			for (GUID tokenGUID : selectedTokenSet) {
 
 				Token token = getRenderer().getZone().getToken(tokenGUID);
 				if (token == null) {
 					continue;
 				}
 				
 				getRenderer().showPath(token, !getRenderer().isPathShowing(getTokenUnderMouse()));
 			}
 			getRenderer().repaint();
 		}
 	}
 	
 	private class RevertLastMoveAction extends AbstractAction {
 		public RevertLastMoveAction() {
 			putValue(Action.NAME, "Revert Last Move");
 			
 			// Only available if there is a last move
 			for (GUID tokenGUID : selectedTokenSet) {
 
 				Token token = getRenderer().getZone().getToken(tokenGUID);
 				if (token == null) {
 					continue;
 				}
 				
 				if (token.getLastPath() == null) {
 					setEnabled(false);
 					break;
 				}
 			}
 		}
 		public void actionPerformed(ActionEvent e) {
 			Zone zone = getRenderer().getZone();
 			
 			for (GUID tokenGUID : selectedTokenSet) {
 
 				Token token = zone.getToken(tokenGUID);
 				if (token == null) {
 					continue;
 				}
 				
 				Path path = token.getLastPath();
 				if (path == null) {
 					continue;
 				}
 				
 				// Get the start cell of the last move
 				ZonePoint zp = zone.getGrid().convert(path.getCellPath().get(0));
 				
 				// Relocate
 				token.setX(zp.x);
 				token.setY(zp.y);
 				
 				// Do it again to cancel out the last move position
 				token.setX(zp.x);
 				token.setY(zp.y);
 				
 				// No more last path
 				token.setLastPath(null);
 				
 				MapTool.serverCommand().putToken(zone.getId(), token);
 			}
 			getRenderer().repaint();
 		}
 	}
 	
 	public class RunMacroAction extends AbstractAction {
 
 		private String macro;
 		
 		public RunMacroAction(String key, String macro) {
 			putValue(Action.NAME, key);
 			this.macro = macro;
 		}
 		
 		public void actionPerformed(ActionEvent e) {
 			String identity = getTokenUnderMouse().getName();
 			String command = "/im " + identity + ":" + macro;
 			JTextArea commandArea = MapTool.getFrame().getCommandPanel().getCommandTextArea();
 			commandArea.setText(command);
 			MapTool.getFrame().getCommandPanel().commitCommand();
 		}
 	}
 
 	public class SayAction extends AbstractAction {
 
 		private String speech;
 		
 		public SayAction(String key, String speech) {
 			putValue(Action.NAME, key);
 			this.speech = speech;
 		}
 		
 		public void actionPerformed(ActionEvent e) {
 			String identity = getTokenUnderMouse().getName();
 			String command = "/im " + identity + ":" + speech;
 			JTextArea commandArea = MapTool.getFrame().getCommandPanel().getCommandTextArea();
 			commandArea.setText(command);
 			MapTool.getFrame().getCommandPanel().commitCommand();
 		}
 	}
 }
