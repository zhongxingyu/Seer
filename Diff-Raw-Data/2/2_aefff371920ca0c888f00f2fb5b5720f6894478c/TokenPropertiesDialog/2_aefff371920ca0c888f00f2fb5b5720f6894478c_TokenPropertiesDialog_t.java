 /* The MIT License
  * 
  * Copyright (c) 2005 David Rice, Trevor Croft, Jay Gorrell
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
 
 package net.rptools.maptool.client.ui.token;
 
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.EventQueue;
 import java.awt.Graphics2D;
 import java.awt.GridLayout;
 import java.awt.Transparency;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.image.BufferedImage;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.swing.AbstractListModel;
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.Icon;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JComboBox;
 import javax.swing.JComponent;
 import javax.swing.JLabel;
 import javax.swing.JMenuItem;
 import javax.swing.JPanel;
 import javax.swing.JPopupMenu;
 import javax.swing.JScrollPane;
 import javax.swing.JTabbedPane;
 import javax.swing.JTable;
 import javax.swing.JTextArea;
 import javax.swing.JTextPane;
 import javax.swing.SwingUtilities;
 import javax.swing.table.AbstractTableModel;
 
 import net.rptools.maptool.client.MapTool;
 import net.rptools.maptool.client.swing.AbeillePanel;
 import net.rptools.maptool.client.swing.GenericDialog;
 import net.rptools.maptool.model.Asset;
 import net.rptools.maptool.model.AssetManager;
 import net.rptools.maptool.model.Association;
 import net.rptools.maptool.model.ObservableList;
 import net.rptools.maptool.model.Player;
 import net.rptools.maptool.model.Token;
 import net.rptools.maptool.util.ImageManager;
 
 import com.jeta.forms.components.image.ImageComponent;
 import com.jidesoft.grid.AbstractPropertyTableModel;
 import com.jidesoft.grid.Property;
 import com.jidesoft.grid.PropertyPane;
 import com.jidesoft.grid.PropertyTable;
 import com.jidesoft.swing.CheckBoxListWithSelectable;
 import com.jidesoft.swing.DefaultSelectable;
 import com.jidesoft.swing.Selectable;
 
 /**
  * This dialog is used to display all of the token states and notes to the user.
  */
 public class TokenPropertiesDialog extends AbeillePanel {
 
 	private static final int INDX_NOTES = 0;
 	private static final int INDX_PROPERTIES = 1;
 	private static final int INDX_STATE = 2;
 	private static final int INDX_MACROS = 3;
 	private static final int INDX_SPEECH = 4;
 	private static final int INDX_OWNERSHIP = 5;
 	private static final int INDX_CONFIG = 6;
 
 	private Token token;
 	private boolean tokenSaved;
 	
 	private GenericDialog dialog;
 
 	/**
 	 * The size used to constrain the icon.
 	 */
 	public static final int SIZE = 64;
 
 	/**
 	 * Create a new token notes dialog.
 	 * 
 	 * @param token
 	 *            The token being displayed.
 	 */
 	public TokenPropertiesDialog() {
 		super("net/rptools/maptool/client/ui/forms/tokenPropertiesDialog.jfrm");
 
 		panelInit();
 	}
 	
 	public void showDialog(Token token) {
 		this.token = token;
 		
		dialog = new GenericDialog("Token Properties", MapTool.getFrame(), this) {
 			@Override
 			public void closeDialog() {
 				// TODO: I don't like this.  There should really be a AbeilleDialog class that does this
 				unbind();
 				super.closeDialog();
 			}
 		};
 		
 		bind(token);
 
 		getRootPane().setDefaultButton(getOKButton());
 		dialog.showDialog();
 	}
 
 	@Override
 	public void bind(Object model) {
 
 		// ICON
 		getTokenIconPanel().setIcon(getTokenIcon());
 
 		// PROPERTIES
 		EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				getPropertyTable().setModel(new TokenPropertyTableModel());
 				getPropertyTable().expandAll();
 			}
 		});
 		
 		// STATES
 		Component[] states = getStatesPanel().getComponents();
 		for (int i = 0; i < states.length; i++) {
 			JCheckBox state = (JCheckBox) states[i];
 			Boolean stateValue = (Boolean) token.getState(state.getText());
 			state.setSelected(stateValue == null ? false : stateValue.booleanValue());
 		}
 		
 		// OWNER LIST
 		EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				getOwnerList().setModel(new OwnerListModel());
 			}
 		});
 
 		// MACRO TABLE
 		EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				getMacroTable().setModel(new MacroTableModel(token));
 			}
 		});
 
 		// SPEECH TABLE
 		EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				getSpeechTable().setModel(new SpeechTableModel(token));
 			}
 		});
 
 //		Player player = MapTool.getPlayer();
 //				boolean editable = player.isGM()
 //						|| !MapTool.getServerPolicy().useStrictTokenManagement() || token.isOwner(player.getName());
 //		getAllPlayersCheckBox().setSelected(token.isOwnedByAll());
 
 		// OTHER
 		getShapeCombo().setSelectedItem(token.getShape());
 		if (token.isSnapToScale()) {
 			getSizeCombo().setSelectedItem(token.getFootprint(MapTool.getFrame().getCurrentZoneRenderer().getZone().getGrid()));
 		} else {
 			getSizeCombo().setSelectedIndex(0);
 		}
 		getPropertyTypeCombo().setSelectedItem(token.getPropertyType());
 		getCharSheetPanel().setImageId(token.getCharsheetImage());
 		getPortraitPanel().setImageId(token.getPortraitImage());
 		getTokenLayoutPanel().setToken(token);
 		
 		super.bind(model);
 	}
 	
 	public JTabbedPane getTabbedPane() {
 		return (JTabbedPane)getComponent("tabs");
 	}
 
 	public JTextArea getNotesTextArea() {
 		return (JTextArea) getComponent("@notes");
 	}
 	
 	public JTextArea getGMNotesTextArea() {
 		return (JTextArea) getComponent("@GMNotes");
 	}
 	
 //	private JLabel getGMNameLabel() {
 //		return (JLabel) getComponent("tokenGMNameLabel");
 //	}
 //	
 //	public JTextField getNameTextField() {
 //		return (JTextField) getComponent("tokenName");
 //	}
 //	
 //	public JTextField getGMNameTextField() {
 //		return (JTextField) getComponent("tokenGMName");
 //	}
 	
 	public void initTypeCombo() {
 		DefaultComboBoxModel model = new DefaultComboBoxModel();
 		model.addElement(Token.Type.NPC);
 		model.addElement(Token.Type.PC);
 //		getTypeCombo().setModel(model);
 	}
 	
 	public JComboBox getTypeCombo() {
 		return (JComboBox) getComponent("@type");
 	}
 	
 	public void initTokenIconPanel() {
 		getTokenIconPanel().setPreferredSize(new Dimension(100, 100));
 		getTokenIconPanel().setMinimumSize(new Dimension(100, 100));
 		
 	}
 	
 	public ImageComponent getTokenIconPanel() {
 		return (ImageComponent) getComponent("tokenIcon");
 	}
 	
 	public void initShapeCombo() {
 		getShapeCombo().setModel(new DefaultComboBoxModel(Token.TokenShape.values()));
 	}
 	
 	public JComboBox getShapeCombo() {
 		return (JComboBox) getComponent("shape");
 	}
 
 	public void initSizeCombo() {
 		DefaultComboBoxModel model = new DefaultComboBoxModel(MapTool.getFrame().getCurrentZoneRenderer().getZone().getGrid().getFootprints().toArray());
 		model.insertElementAt("Free Size", 0);
 		getSizeCombo().setModel(model);
 	}
 	
 	public JComboBox getSizeCombo() {
 		return (JComboBox) getComponent("size");
 	}
 
 	public JComboBox getPropertyTypeCombo() {
 		return (JComboBox) getComponent("propertyTypeCombo");
 	}
 
 	public void initOKButton() {
 		getOKButton().addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				if (commit()) {
 					unbind();
 					dialog.closeDialog();
 				}
 			}
 		});
 	}
 	
 	@Override
 	public boolean commit() {
 
 		// Commit any in-process edits
 		if (getMacroTable().isEditing()) {
 			getMacroTable().getCellEditor().stopCellEditing();
 		}
 		
 		if (getSpeechTable().isEditing()) {
 			getSpeechTable().getCellEditor().stopCellEditing();
 		}
 		
 		if (getPropertyTable().isEditing()) {
 			getPropertyTable().getCellEditor().stopCellEditing();
 		}
 
 		// Commit the changes to the token properties
 		if (!super.commit()) {
 			return false;
 		}
 		
 		// SIZE
 		if (getSizeCombo().getSelectedIndex() == 0) {
 			token.setSnapToScale(false);
 		} else {
 			token.setSnapToScale(true);
 		}
 
 		// Get the states
 		Component[] components = getStatesPanel().getComponents();
 		for (int i = 0; i < components.length; i++) {
 			JCheckBox cb = (JCheckBox) components[i];
 			String state = cb.getText();
 			token.setState(state, cb.isSelected() ? Boolean.TRUE
 					: Boolean.FALSE);
 		}
 
 		// Ownership
 		token.clearAllOwners();
 		
 		for (int i = 0; i < getOwnerList().getModel().getSize(); i++) {
 			DefaultSelectable selectable = (DefaultSelectable) getOwnerList().getModel().getElementAt(i);
 			if (selectable.isSelected()) {
 				token.addOwner((String) selectable.getObject());
 			}
 		}
 
 		// SHAPE
 		token.setShape((Token.TokenShape)getShapeCombo().getSelectedItem());
 		
 		// Macros
 		token.setMacroMap(((KeyValueTableModel)getMacroTable().getModel()).getMap());
 		token.setSpeechMap(((KeyValueTableModel)getSpeechTable().getModel()).getMap());
 		
 		// Properties
 		((TokenPropertyTableModel)getPropertyTable().getModel()).applyTo(token);
 	
 		// Charsheet
 		token.setCharsheetImage(getCharSheetPanel().getImageId());
 		if (token.getCharsheetImage() != null) {
 			// Make sure the server has the image
 			if (!MapTool.getCampaign().containsAsset(token.getCharsheetImage())) {
 				MapTool.serverCommand().putAsset(AssetManager.getAsset(token.getCharsheetImage()));
 			}
 		}
 		
 		// PORTRAIT
 		token.setPortraitImage(getPortraitPanel().getImageId());
 		if (token.getPortraitImage() != null) {
 			// Make sure the server has the image
 			if (!MapTool.getCampaign().containsAsset(token.getPortraitImage())) {
 				MapTool.serverCommand().putAsset(AssetManager.getAsset(token.getPortraitImage()));
 			}
 		}
 
 		// LAYOUT
 		token.setSizeScale(getTokenLayoutPanel().getSizeScale());
 		token.setAnchor(getTokenLayoutPanel().getAnchorX(), getTokenLayoutPanel().getAnchorY());
 
 		// OTHER
 		tokenSaved = true;
 		
 		// Update UI
 		MapTool.getFrame().updateTokenTree();
 		
 		return true;
 	}
 	
 	public JButton getOKButton() {
 		return (JButton) getComponent("okButton");
 	}
 
 	public void initCancelButton() {
 		getCancelButton().addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				unbind();
 				dialog.closeDialog();
 			}
 		});
 	}
 	
 	public JButton getCancelButton() {
 		return (JButton) getComponent("cancelButton");
 	}
 
 	public PropertyTable getPropertyTable() {
 		return (PropertyTable) getComponent("propertiesTable");
 	}
 
 	public void initStatesPanel() {
 		JPanel panel = getStatesPanel();
 		panel.removeAll();
 		Set<String> states = TokenStates.getStates();
 		panel.setLayout(new GridLayout(0, 4));
 		for (String state : states) {
 			panel.add(new JCheckBox(state));
 		}		
 	}
 	
 	public JPanel getStatesPanel() {
 		return (JPanel) getComponent("statesPanel");
 	}
 	
 	public JTable getMacroTable() {
 		return (JTable) getComponent("macroTable");
 	}
 	
 	public JTable getSpeechTable() {
 		return (JTable) getComponent("speechTable");
 	}
 
 	public JButton getSpeechClearAllButton() {
 		return (JButton) getComponent("speechClearAllButton");
 	}
 	
 	public JButton getMacroClearAllButton() {
 		return (JButton) getComponent("macroClearAllButton");
 	}
 	
 	private JLabel getVisibleLabel() {
 		return (JLabel) getComponent("visibleLabel");
 	}
 	
 	private JPanel getGMNotesPanel() {
 		return (JPanel) getComponent("gmNotesPanel");
 	}
 	
 	public CheckBoxListWithSelectable getOwnerList() {
 		return (CheckBoxListWithSelectable) getComponent("ownerList");
 	}
 	
 	public void initMacroPanel() {
 		
 		getMacroClearAllButton().addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				
 				if (!MapTool.confirm("Are you sure you want to clear all macros for this token?")) {
 					return;
 				}
 				
 				EventQueue.invokeLater(new Runnable() {
 					public void run() {
 						getMacroTable().setModel(new MacroTableModel());
 					}
 				});
 			}
 		});
 	}
 	
 	public void initSpeechPanel() {
 		
 		getSpeechClearAllButton().addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				
 				if (!MapTool.confirm("Are you sure you want to clear all speech for this token?")) {
 					return;
 				}
 				
 				EventQueue.invokeLater(new Runnable() {
 					public void run() {
 						getSpeechTable().setModel(new SpeechTableModel());
 					}
 				});
 			}
 		});
 	}
 	
 	public void initOwnershipPanel() {
 		
 		CheckBoxListWithSelectable list = new CheckBoxListWithSelectable();
 		list.setName("ownerList");
 		replaceComponent("ownershipPanel", "ownershipList", new JScrollPane(list, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));
 		
 	}
 
 //	public void initNotesPanel() {
 //		getNotesTextArea().addMouseListener(new MouseHandler(getNotesTextArea()));
 //		getGMNotesTextArea().addMouseListener(new MouseHandler(getGMNotesTextArea()));
 //	}
 	
 	public void initTokenDetails() {
 //		tokenGMNameLabel = panel.getLabel("tokenGMNameLabel");
 	}
 
 	public void initTokenLayoutPanel() {
 		TokenLayoutPanel layoutPanel = new TokenLayoutPanel();
 		layoutPanel.setPreferredSize(new Dimension(150, 125));
 		layoutPanel.setName("tokenLayout");
 		
 		replaceComponent("tokenLayoutPanel", "tokenLayout", layoutPanel);
 	}
 	
 	public void initCharsheetPanel() {
 		ImageAssetPanel panel = new ImageAssetPanel();
 		panel.setPreferredSize(new Dimension(150, 125));
 		panel.setName("charsheet");
 		panel.setLayout(new GridLayout());
 		
 		replaceComponent("charsheetPanel", "charsheet", panel);
 	}
 	
 	public void initPortraitPanel() {
 		ImageAssetPanel panel = new ImageAssetPanel();
 		panel.setPreferredSize(new Dimension(150, 125));
 		panel.setName("portrait");
 		panel.setLayout(new GridLayout());
 		
 		replaceComponent("portraitPanel", "portrait", panel);
 	}
 	
 	public ImageAssetPanel getPortraitPanel() {
 		return (ImageAssetPanel) getComponent("portrait");
 	}
 	
 	public ImageAssetPanel getCharSheetPanel() {
 		return (ImageAssetPanel) getComponent("charsheet");
 	}
 	
 	public TokenLayoutPanel getTokenLayoutPanel() {
 		return (TokenLayoutPanel) getComponent("tokenLayout");
 	}
 	
 	public void initPropertiesPanel() {
 
 		PropertyTable propertyTable = new PropertyTable();
 		propertyTable.setName("propertiesTable");
 		
 		PropertyPane pane = new PropertyPane(propertyTable);
 		pane.setPreferredSize(new Dimension(100, 300));
 		
 		replaceComponent("propertiesPanel", "propertiesTable", pane);
 	}
 
 
 //	/**
 //	 * Set the currently displayed token.
 //	 * 
 //	 * @param aToken
 //	 *            The token to be displayed
 //	 */
 //	public void setToken(Token aToken) {
 //
 //		if (aToken == token)
 //			return;
 //		if (token != null) {
 //			token.removeModelChangeListener(this);
 //		}
 //		
 //		token = aToken;
 //		
 //		if (token != null) {
 //			token.addModelChangeListener(this);
 //			
 //			List<String> typeList = new ArrayList<String>();
 //			typeList.addAll(MapTool.getCampaign().getTokenTypes());
 //			Collections.sort(typeList);
 //			getPropertyTypeCombo().setModel(new DefaultComboBoxModel(typeList.toArray()));
 //
 //			setFields();
 //			updateView();
 //		}
 //
 //		getTabbedPane().setSelectedIndex(0);
 //	}
 	
 //	private void updateView() {
 //		
 //		Player player = MapTool.getPlayer();
 //		
 //		boolean isEnabled = player.isGM() || token.isOwner(player.getName());
 //		
 //		getTabbedPane().setEnabledAt(INDX_PROPERTIES, isEnabled);
 //		getTabbedPane().setEnabledAt(INDX_STATE, isEnabled);
 //		getTabbedPane().setEnabledAt(INDX_MACROS, isEnabled);
 //		getTabbedPane().setEnabledAt(INDX_SPEECH, isEnabled);
 //		getTabbedPane().setEnabledAt(INDX_OWNERSHIP, isEnabled);
 //		getTabbedPane().setEnabledAt(INDX_CONFIG, isEnabled);
 //		
 //		// Set the editable & enabled state
 //		boolean editable = player.isGM() || !MapTool.getServerPolicy().useStrictTokenManagement() || token.isOwner(player.getName());
 //		getOKButton().setEnabled(editable);
 //		
 //		getNotesTextArea().setEditable(editable);
 //		getNameTextField().setEditable(editable);
 //		getShapeCombo().setEnabled(editable);
 //		getSizeCombo().setEnabled(editable);
 //		getSnapToGridCheckBox().setEnabled(editable);
 //		getVisibleCheckBox().setEnabled(editable);
 //		getTypeCombo().setSelectedItem(token.getType());
 //
 //		getGMNotesPanel().setVisible(player.isGM());
 //		getGMNameTextField().setVisible(player.isGM());
 //		getGMNameLabel().setVisible(player.isGM());
 //		getTypeCombo().setEnabled(player.isGM());
 //		getVisibleCheckBox().setVisible(player.isGM());
 //		getVisibleLabel().setVisible(player.isGM());
 //		
 //	}
 
 	/**
 	 * Get and icon from the asset manager and scale it properly.
 	 * 
 	 * @return An icon scaled to fit within a cell.
 	 */
 	private Icon getTokenIcon() {
 
 		// Get the base image && find the new size for the icon
 		BufferedImage assetImage = null;
 		Asset asset = AssetManager.getAsset(token.getImageAssetId());
 		if (asset == null) {
 			assetImage = ImageManager.UNKNOWN_IMAGE;
 		} else {
 			assetImage = ImageManager.getImage(asset, this);
 		}
 
 		// Need to resize?
 		if (assetImage.getWidth() > SIZE || assetImage.getHeight() > SIZE) {
 			Dimension dim = new Dimension(assetImage.getWidth(), assetImage
 					.getWidth());
 			if (dim.height < dim.width) {
 				dim.height = (int) ((dim.height / (double) dim.width) * SIZE);
 				dim.width = SIZE;
 			} else {
 				dim.width = (int) ((dim.width / (double) dim.height) * SIZE);
 				dim.height = SIZE;
 			}
 			BufferedImage image = new BufferedImage(dim.width, dim.height,
 					Transparency.BITMASK);
 			Graphics2D g = image.createGraphics();
 			g.drawImage(assetImage, 0, 0, dim.width, dim.height, null);
 			assetImage = image;
 		}
 		return new ImageIcon(assetImage);
 	}
 
 	/** @return Getter for tokenSaved */
 	public boolean isTokenSaved() {
 		return tokenSaved;
 	}
 	
 	////
 	// HANDLER
 	public class MouseHandler extends MouseAdapter {
 		
 		JTextArea source;
 		
 		public MouseHandler(JTextArea source) {
 			this.source = source;
 		}
 		
 		@Override
 		public void mouseClicked(MouseEvent e) {
 			if (SwingUtilities.isRightMouseButton(e)) {
 				
 				JPopupMenu menu = new JPopupMenu();
 				JMenuItem sendToChatItem = new JMenuItem("Send to Chat");
 				sendToChatItem.addActionListener(new ActionListener() {
 					public void actionPerformed(ActionEvent e) {
 
 						String selectedText = source.getSelectedText();
 						if (selectedText == null) {
 							selectedText = source.getText();
 						}
 
 						// TODO: COmbine this with the code int MacroButton
 						JTextPane commandArea = MapTool.getFrame().getCommandPanel().getCommandTextArea();
 
 						commandArea.setText(commandArea.getText() + selectedText);
 						commandArea.requestFocusInWindow();
 					}
 				});
 				
 				menu.add(sendToChatItem);
 				
 				JMenuItem sendAsEmoteItem = new JMenuItem("Send as Emit");
 				sendAsEmoteItem.addActionListener(new ActionListener() {
 					public void actionPerformed(ActionEvent e) {
 
 						String selectedText = source.getSelectedText();
 						if (selectedText == null) {
 							selectedText = source.getText();
 						}
 
 						// TODO: COmbine this with the code int MacroButton
 						JTextPane commandArea = MapTool.getFrame().getCommandPanel().getCommandTextArea();
 
 						commandArea.setText("/emit " + selectedText);
 						commandArea.requestFocusInWindow();
 						MapTool.getFrame().getCommandPanel().commitCommand();
 					}
 				});
 				
 				menu.add(sendAsEmoteItem);
 				
 				menu.show((JComponent)e.getSource(), e.getX(), e.getY());
 			}
 		}
 	}
 	
 	////
 	// MODELS
 	private class TokenPropertyTableModel extends AbstractPropertyTableModel {
 
 		private Map<String, String> propertyMap;
 		private List<net.rptools.maptool.model.TokenProperty> propertyList; 
 
 		private Map<String, String> getPropertyMap() {
 			if (propertyMap == null) {
 				propertyMap = new HashMap<String, String>();
 				
 				List<net.rptools.maptool.model.TokenProperty> propertyList = getPropertyList();
 				for (net.rptools.maptool.model.TokenProperty property : propertyList) {
 					propertyMap.put(property.getName(), (String) token.getProperty(property.getName()));
 				}
 			}
 			return propertyMap;
 		}
 		
 		private List<net.rptools.maptool.model.TokenProperty> getPropertyList() {
 			if (propertyList == null) {
 				propertyList = MapTool.getCampaign().getTokenPropertyList(token.getPropertyType());
 			}
 			return propertyList;
 		}
 		
 		public void applyTo(Token token) {
 			
 			for (net.rptools.maptool.model.TokenProperty property : getPropertyList()) {
 				token.setProperty(property.getName(), getPropertyMap().get(property.getName()));
 			}
 		}
 		
 		@Override
 		public Property getProperty(int index) {
 			
 			return new TokenProperty(getPropertyList().get(index).getName());
 		}
 
 		@Override
 		public int getPropertyCount() {
 			return getPropertyList() != null ? getPropertyList().size() : 0;
 		}
 		
 		private class TokenProperty extends Property {
 			private String key;
 			
 			public TokenProperty(String key) {
 				super(key, key, String.class, "Core");
 				this.key = key;
 			}
 			
 			@Override
 			public Object getValue() {
 				return getPropertyMap().get(key);
 			}
 
 			@Override
 			public void setValue(Object value) {
 				getPropertyMap().put(key, (String)value);
 			}
 
 			@Override
 			public boolean hasValue() {
 				return getPropertyMap().get(key) != null;
 			}
 		}
 	}
 	
 	private class OwnerListModel extends AbstractListModel {
 
 		List<Selectable> ownerList = new ArrayList<Selectable>();
 		
 		public OwnerListModel() {
 			List<String> list = new ArrayList<String>();
 			Set<String> ownerSet = token.getOwners();
 			list.addAll(ownerSet);
 			
 			ObservableList<Player> playerList = MapTool.getPlayerList(); 
 			for (Object item : playerList) {
 				Player player = (Player) item;
 				String playerId = player.getName();
 				if (!list.contains(playerId)) {
 					list.add(playerId);
 				}
 			}
 			
 			Collections.sort(list);
 			
 			for (String id : list) {
 				Selectable selectable = new DefaultSelectable(id);
 				selectable.setSelected(ownerSet.contains(id));
 				ownerList.add(selectable);
 			}
 		}
 		
 		public Object getElementAt(int index) {
 			return ownerList.get(index);
 		}
 		public int getSize() {
 			return ownerList.size();
 		}
 	}
 	
 	private static class MacroTableModel extends KeyValueTableModel {
 		
 		public MacroTableModel(Token token) {
 			List<Association<String, String>> rowList = new ArrayList<Association<String, String>>();
 			for (String macroName : token.getMacroNames()) {
 				rowList.add(new Association<String, String>(macroName, token.getMacro(macroName)));
 			}
 			
 			Collections.sort(rowList, new Comparator<Association<String, String>>() {
 				public int compare(Association<String, String> o1, Association<String, String> o2) {
 
 					return o1.getLeft().compareToIgnoreCase(o2.getLeft());
 				}
 			});
 			init(rowList);
 		}
 		public MacroTableModel() {
 			init(new ArrayList<Association<String, String>>());
 		}
 		
 		@Override
 		public String getColumnName(int column) {
 			switch (column) {
 			case 0: return "ID";
 			case 1: return "Action";
 			}
 			return "";
 		}
 	}
 
 	private static class SpeechTableModel extends KeyValueTableModel {
 		
 		public SpeechTableModel(Token token) {
 			List<Association<String, String>> rowList = new ArrayList<Association<String, String>>();
 			for (String speechName : token.getSpeechNames()) {
 				rowList.add(new Association<String, String>(speechName, token.getSpeech(speechName)));
 			}
 			
 			Collections.sort(rowList, new Comparator<Association<String, String>>() {
 				public int compare(Association<String, String> o1, Association<String, String> o2) {
 
 					return o1.getLeft().compareToIgnoreCase(o2.getLeft());
 				}
 			});
 			init(rowList);
 		}
 		public SpeechTableModel() {
 			init(new ArrayList<Association<String, String>>());
 		}
 		@Override
 		public String getColumnName(int column) {
 			switch (column) {
 			case 0: return "ID";
 			case 1: return "Speech Text";
 			}
 			return "";
 		}
 	}
 
 	private static class KeyValueTableModel extends AbstractTableModel {
 		
 		private Association<String, String> newRow = new Association<String, String>("", "");
 		private List<Association<String, String>> rowList;
 
 		protected void init(List<Association<String, String>> rowList) {
 			this.rowList = rowList;
 		}
 		
 		public int getColumnCount() {
 			return 2;
 		}
 		public int getRowCount() {
 			return rowList.size() + 1;
 		}
 		public Object getValueAt(int rowIndex, int columnIndex) {
 			if (rowIndex == getRowCount() - 1) {
 				switch(columnIndex) {
 				case 0: return newRow.getLeft();
 				case 1: return newRow.getRight();
 				}
 				return "";
 			}
 			
 			switch (columnIndex) {
 			case 0: return rowList.get(rowIndex).getLeft();
 			case 1: return rowList.get(rowIndex).getRight();
 			}
 			return "";
 		}
 		@Override
 		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
 			if (rowIndex == getRowCount() - 1) {
 				switch(columnIndex) {
 				case 0: newRow.setLeft((String)aValue); break;
 				case 1: newRow.setRight((String)aValue); break;
 				}
 				
 				rowList.add(newRow);
 				newRow = new Association<String, String>("", "");
 				return;
 			}
 			
 			switch(columnIndex) {
 			case 0: rowList.get(rowIndex).setLeft((String)aValue); break;
 			case 1: rowList.get(rowIndex).setRight((String)aValue); break;
 			}
 		}
 		@Override
 		public String getColumnName(int column) {
 			switch (column) {
 			case 0: return "Key";
 			case 1: return "Value";
 			}
 			return "";
 		}
 		@Override
 		public boolean isCellEditable(int rowIndex, int columnIndex) {
 			return true;
 		}
 		
 		public Map<String, String> getMap() {
 			Map<String, String> map = new HashMap<String, String>();
 			
 			for (Association<String, String> row : rowList) {
 				if (row.getLeft() == null || row.getLeft().trim().length() == 0) {
 					continue;
 				}
 				
 				map.put(row.getLeft(), row.getRight());
 			}
 			
 			return map;
 		}
 	}
 	
 }
