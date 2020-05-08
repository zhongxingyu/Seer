 /** Copyright (c) 2002 Institute for Systems Biology and the Whitehead Institute
  **
  ** This library is free software; you can redistribute it and/or modify it
  ** under the terms of the GNU Lesser General Public License as published
  ** by the Free Software Foundation; either version 2.1 of the License, or
  ** any later version.
  **
  ** This library is distributed in the hope that it will be useful, but
  ** WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
  ** MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
  ** documentation provided hereunder is on an "as is" basis, and the
  ** Institute for Systems Biology and the Whitehead Institute
  ** have no obligations to provide maintenance, support,
  ** updates, enhancements or modifications.  In no event shall the
  ** Institute for Systems Biology and the Whitehead Institute
  ** be liable to any party for direct, indirect, special,
  ** incidental or consequential damages, including lost profits, arising
  ** out of the use of this software and its documentation, even if the
  ** Institute for Systems Biology and the Whitehead Institute
  ** have been advised of the possibility of such damage.  See
  ** the GNU Lesser General Public License for more details.
  **
  ** You should have received a copy of the GNU Lesser General Public License
  ** along with this library; if not, write to the Free Software Foundation,
  ** Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
  **/
 
 //------------------------------------------------------------------------------
 package cytoscape.view;
 
 //------------------------------------------------------------------------------
 
 import java.awt.event.*;
 import javax.swing.*;
 import javax.swing.event.MenuListener;
 import javax.swing.event.MenuEvent;
 import javax.help.HelpBroker;
 import javax.help.CSH.*;
 import javax.help.CSH; // Context Sensitive Help convenience object...
 import javax.swing.KeyStroke;
 
 import cytoscape.Cytoscape;
 import cytoscape.CyNetwork;
 import cytoscape.view.CyNetworkView;
 
 import cytoscape.plugin.AbstractPlugin;
 
 import cytoscape.actions.*;
 
 import cytoscape.data.annotation.AnnotationGui;
 
 import cytoscape.util.CytoscapeMenuBar;
 import cytoscape.util.CytoscapeToolBar;
 import cytoscape.util.CytoscapeAction;
 import cytoscape.util.CreditScreen;
 
 import cytoscape.visual.ui.VizMapUI;
 
 import giny.view.GraphViewChangeListener;
 import giny.view.GraphViewChangeEvent;
 
 import cytoscape.view.cytopanels.CytoPanel;
 
 //------------------------------------------------------------------------------
 /**
  * This class creates the menu and tool bars for a Cytoscape window object. It
  * also provides access to individual menus and items.
  */
 public class CyMenus implements GraphViewChangeListener {
 
 	boolean menusInitialized = false;
 	CytoscapeMenuBar menuBar;
 	JMenu fileMenu, loadSubMenu, saveSubMenu;
 	JMenu editMenu;
 	// JMenuItem undoMenuItem, redoMenuItem;
 	JMenuItem deleteSelectionMenuItem;
 	JMenu dataMenu;
 	JMenu selectMenu;
 	JMenu displayNWSubMenu;
 	JMenu layoutMenu;
 	JMenu vizMenu;
 	JMenu helpMenu;
 	JMenu cytoPanelMenu;
 
 	CytoscapeAction menuPrintAction, menuExportAction;
 	JMenuItem vizMenuItem, vizMapperItem;
 	JCheckBoxMenuItem cytoPanelWestItem, cytoPanelEastItem, cytoPanelSouthItem;
 	JMenuItem helpContentsMenuItem, helpContextSensitiveMenuItem,
 			helpAboutMenuItem;
 
 	JButton loadButton, saveButton, zoomInButton, zoomOutButton,
 			zoomSelectedButton, zoomDisplayAllButton, showAllButton,
 			hideSelectedButton, annotationButton, vizButton;
 	JMenu opsMenu;
 	CytoscapeToolBar toolBar;
 	boolean nodesRequiredItemsEnabled;
 
 	public CyMenus() {
 
 		// the following methods construct the basic bar objects, but
 		// don't fill them with menu items and associated action listeners
 		createMenuBar();
 		toolBar = new CytoscapeToolBar();
 	}
 
 	/**
 	 * Returns the main menu bar constructed by this object.
 	 */
 	public CytoscapeMenuBar getMenuBar() {
 		return menuBar;
 	}
 
 	/**
 	 * Returns the menu with items related to file operations.
 	 */
 	public JMenu getFileMenu() {
 		return fileMenu;
 	}
 
 	/**
 	 * Returns the submenu with items related to loading objects.
 	 */
 	public JMenu getLoadSubMenu() {
 		return loadSubMenu;
 	}
 
 	/**
 	 * Returns the submenu with items related to saving objects.
 	 */
 	public JMenu getSaveSubMenu() {
 		return saveSubMenu;
 	}
 
 	/**
 	 * returns the menu with items related to editing the graph.
 	 */
 	public JMenu getEditMenu() {
 		return editMenu;
 	}
 
 	/**
 	 * Returns the menu with items related to data operations.
 	 */
 	public JMenu getDataMenu() {
 		return dataMenu;
 	}
 
 	/**
 	 * Returns the menu with items related to selecting nodes and edges in the
 	 * graph.
 	 */
 	public JMenu getSelectMenu() {
 		return selectMenu;
 	}
 
 	/**
 	 * Returns the menu with items realted to layout actions.
 	 */
 	public JMenu getLayoutMenu() {
 		return layoutMenu;
 	}
 
 	/**
 	 * Returns the menu with items related to visualiation.
 	 */
 	public JMenu getVizMenu() {
 		return vizMenu;
 	}
 
 	/**
 	 * Returns the help menu.
 	 */
 	public JMenu getHelpMenu() {
 		return helpMenu;
 	}
 
 	/**
 	 * Returns the menu with items associated with plug-ins. Most plug-ins grab
 	 * this menu and add their menu option. The plugins should then call
 	 * refreshOperationsMenu to update the menu.
 	 */
 	public JMenu getOperationsMenu() {
 		return opsMenu;
 	}
 
 	/**
 	 * @deprecated This method is no longer needed now that we don't use the
 	 *             NO_OPERATIONS menu placeholder.
 	 * 
 	 * This method does nothing.
 	 */
 	public void refreshOperationsMenu() {
 	}
 
 	/**
 	 * Returns the toolbar object constructed by this class.
 	 */
 	public CytoscapeToolBar getToolBar() {
 		return toolBar;
 	}
 
 	public void addAction(CytoscapeAction action) {
 		addCytoscapeAction(action);
 	}
 
 	/**
 	 * Takes a CytoscapeAction and will add it to the MenuBar or the Toolbar as
 	 * is appropriate.
 	 */
 	public void addCytoscapeAction(CytoscapeAction action) {
 		if (action.isInMenuBar()) {
 			getMenuBar().addAction(action);
 		}
 		if (action.isInToolBar()) {
 			getToolBar().addAction(action);
 		}
 
 	}
 
 	/**
 	 * Called when the window switches to edit mode, enabling the menu option
 	 * for deleting selected objects.
 	 * 
 	 * Again, the keeper of the edit modes should probably get a reference to
 	 * the menu item and manage its state.
 	 */
 	public void enableDeleteSelectionMenuItem() {
 		if (deleteSelectionMenuItem != null) {
 			deleteSelectionMenuItem.setEnabled(true);
 		}
 	}
 
 	/**
 	 * Called when the window switches to read-only mode, disabling the menu
 	 * option for deleting selected objects.
 	 * 
 	 * Again, the keeper of the edit modes should probably get a reference to
 	 * the menu item and manage its state.
 	 */
 	public void disableDeleteSelectionMenuItem() {
 		if (deleteSelectionMenuItem != null) {
 			deleteSelectionMenuItem.setEnabled(false);
 		}
 	}
 
 	/**
 	 * Enables the menu items related to the visual mapper if the argument is
 	 * true, else disables them. This method should only be called from the
 	 * window that holds this menu.
 	 */
 	public void setVisualMapperItemsEnabled(boolean newState) {
 		vizMenuItem.setEnabled(newState);
 		vizButton.setEnabled(newState);
 		vizMapperItem.setText(newState ? "Disable Visual Mapper"
 				: "Enable Visual Mapper");
 	}
 
 	/**
 	 * Enables or disables save, print, and display nodes in new window GUI
 	 * functions, based on the number of nodes in this window's graph
 	 * perspective. This function should be called after every operation which
 	 * adds or removes nodes from the current window.
 	 */
 	public void setNodesRequiredItemsEnabled() {
 		boolean newState = Cytoscape.getCurrentNetwork().getNodeCount() > 0;
 		newState = true; // TODO: remove this once the
 							// GraphViewChangeListener system is working
 		if (newState == nodesRequiredItemsEnabled)
 			return;
 
 		saveButton.setEnabled(newState);
 		saveSubMenu.setEnabled(newState);
 		menuPrintAction.setEnabled(newState);
 		menuExportAction.setEnabled(newState);
 		displayNWSubMenu.setEnabled(newState);
 		nodesRequiredItemsEnabled = newState;
 	}
 
 	/**
 	 * Update the UI menus and buttons. When the graph view is changed, this
 	 * method is the listener which will update the UI items, enabling or
 	 * disabling items which are only available when the graph view is
 	 * non-empty.
 	 * 
 	 * @param e
 	 */
 	public void graphViewChanged(GraphViewChangeEvent e) {
 		// Do this in the GUI Event Dispatch thread...
 		SwingUtilities.invokeLater(new Runnable() {
 			public void run() {
 				setNodesRequiredItemsEnabled();
 			}
 		});
 	}
 
 	/**
 	 * Returns the cytopanels menu.
 	 */
 	public JMenu getCytoPanelMenu() {
 		return cytoPanelMenu;
 	}
 
 	/*
 	 * Sets up the CytoPanelMenu items. This is put into its own public method
 	 * so it can be called from CytoscapeInit.Init(), because we need the
 	 * CytoscapeDesktop to be instantiated first.
 	 */
 	public void initCytoPanelMenus() {
 		CytoPanel cytoPanel;
 
 		// setup cytopanel west (enabled/shown by default)
 		cytoPanel = Cytoscape.getDesktop().getCytoPanel(SwingConstants.WEST);
 		cytoPanelWestItem = new JCheckBoxMenuItem(cytoPanel.getTitle());
 		initCytoPanelMenuItem(cytoPanel, cytoPanelWestItem, true, true,
 				"cytoPanelWest", KeyEvent.VK_1);
 
 		// setup cytopanel east (disabled/hidden by default)
 		cytoPanel = Cytoscape.getDesktop().getCytoPanel(SwingConstants.EAST);
 		cytoPanelEastItem = new JCheckBoxMenuItem(cytoPanel.getTitle());
 		initCytoPanelMenuItem(cytoPanel, cytoPanelEastItem, false, false,
 				"cytoPanelEast", KeyEvent.VK_3);
 
 		// setup cytopanel south (disabled/hidden by default)
 		cytoPanel = Cytoscape.getDesktop().getCytoPanel(SwingConstants.SOUTH);
 		cytoPanelSouthItem = new JCheckBoxMenuItem(cytoPanel.getTitle());
 		initCytoPanelMenuItem(cytoPanel, cytoPanelSouthItem, false, false,
 				"cytoPanelSouth", KeyEvent.VK_2);
 
 		// add cytopanel menu items to CytoPanels Menu
 		menuBar.getMenu("CytoPanels").add(cytoPanelWestItem);
 		menuBar.getMenu("CytoPanels").add(cytoPanelSouthItem);
 		menuBar.getMenu("CytoPanels").add(cytoPanelEastItem);
 	}
 
 	private void initCytoPanelMenuItem(CytoPanel cytoPanel,
 			JCheckBoxMenuItem menuItem, boolean selected, boolean enabled,
 			String mapObject, int keyCode) {
 
 		// setup action
 		CytoPanelAction cytoPanelAction = new CytoPanelAction(menuItem,
 				cytoPanel);
 		menuItem.addActionListener(cytoPanelAction);
 		// enabled/disabled - shown/hidden
 		menuItem.setSelected(selected);
 		menuItem.setEnabled(enabled);
 		// setup menu item key accel
 		KeyStroke accel = KeyStroke.getKeyStroke(keyCode,
 				java.awt.event.InputEvent.CTRL_MASK);
 		menuItem.setAccelerator(accel);
 		// setup global key accel
 		menuItem.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
 				.put(accel, mapObject);
 		menuItem.getActionMap().put(mapObject, cytoPanelAction);
 	}
 
 	/**
 	 * Creates the menu bar and the various menus and submenus, but defers
 	 * filling those menus with items until later.
 	 */
 	private void createMenuBar() {
 		menuBar = new CytoscapeMenuBar();
 		fileMenu = menuBar.getMenu("File");
 		final JMenu f_fileMenu = fileMenu;
 		fileMenu.addMenuListener(new MenuListener() {
 			public void menuCanceled(MenuEvent e) {
 			}
 
 			public void menuDeselected(MenuEvent e) {
 			}
 
 			public void menuSelected(MenuEvent e) {
 				CyNetworkView graphView = Cytoscape.getCurrentNetworkView();
 				CyNetwork graph = Cytoscape.getCurrentNetwork();
 				boolean inactive = false;
 				if (graphView == null || graphView.nodeCount() == 0)
 					inactive = true;
 				boolean networkExists = (graph != null);
 				MenuElement[] popup = f_fileMenu.getSubElements();
 				if (popup[0] instanceof JPopupMenu) {
 					MenuElement[] submenus = ((JPopupMenu) popup[0])
 							.getSubElements();
 					for (int i = 0; i < submenus.length; i++) {
 						if (submenus[i] instanceof JMenuItem) {
 							JMenuItem item = (JMenuItem) submenus[i];
 							if (item.getText().equals(ExportAction.MENU_LABEL)
 									|| item.getText().equals(
 											PrintAction.MENU_LABEL)) {
 								item.setEnabled(!inactive);
 							} else if (item.getText().equals("Save")) {
 								item.setEnabled(networkExists);
 							}
 						}
 					}
 				}
 			}
 		});
 		loadSubMenu = menuBar.getMenu("File.Load");
 		saveSubMenu = menuBar.getMenu("File.Save");
 		editMenu = menuBar.getMenu("Edit");
 		final JMenu f_editMenu = editMenu;
 		editMenu.addMenuListener(new MenuListener() {
 			public void menuCanceled(MenuEvent e) {
 			}
 
 			public void menuDeselected(MenuEvent e) {
 			}
 
 			public void menuSelected(MenuEvent e) {
 				CyNetworkView graphView = Cytoscape.getCurrentNetworkView();
 				boolean inactive = false;
 				if (graphView == null || graphView.nodeCount() == 0)
 					inactive = true;
 				MenuElement[] popup = f_editMenu.getSubElements();
 				if (popup[0] instanceof JPopupMenu) {
 					MenuElement[] submenus = ((JPopupMenu) popup[0])
 							.getSubElements();
 					for (int i = 0; i < submenus.length; i++) {
 						if (submenus[i] instanceof JMenuItem) {
 							JMenuItem item = (JMenuItem) submenus[i];
							if (!item.getText().equals(
									PreferenceAction.MENU_LABEL)) {
								if (inactive)
									item.setEnabled(false);
								else
									item.setEnabled(true);
 							}
 						}
 					}
 				}
 			}
 		});
 		
 		// 
 		// Data menu.  disabled by default.
 		//
 		dataMenu = menuBar.getMenu("Data");
 		final JMenu f_dataMenu = dataMenu;
 		
 		dataMenu.setEnabled(false);
 		
 		
 		dataMenu.addMenuListener(new MenuListener() {
 			public void menuCanceled(MenuEvent e) {
 			}
 
 			public void menuDeselected(MenuEvent e) {
 			}
 
 			public void menuSelected(MenuEvent e) {
 				// CyNetworkView graphView = Cytoscape.getCurrentNetworkView();
 				// boolean inactive = false;
 				// if (graphView == null || graphView.nodeCount() == 0) inactive
 				// = true;
 //				CyNetwork graph = Cytoscape.getCurrentNetwork();
 //				boolean inactive = false;
 //				if (graph == null || graph.getNodeCount() == 0)
 //					inactive = true;
 //				MenuElement[] popup = f_dataMenu.getSubElements();
 //				if (popup[0] instanceof JPopupMenu) {
 //					MenuElement[] submenus = ((JPopupMenu) popup[0])
 //							.getSubElements();
 //					for (int i = 0; i < submenus.length; i++) {
 //						if (submenus[i] instanceof JMenuItem) {
 //							if (inactive)
 //								((JMenuItem) submenus[i]).setEnabled(false);
 //							else
 //								((JMenuItem) submenus[i]).setEnabled(true);
 //						}
 //					}
 //				}
 			}
 		});
 		selectMenu = menuBar.getMenu("Select");
 		final JMenu f_selectMenu = selectMenu;
 		selectMenu.addMenuListener(new MenuListener() {
 			public void menuCanceled(MenuEvent e) {
 			}
 
 			public void menuDeselected(MenuEvent e) {
 			}
 
 			public void menuSelected(MenuEvent e) {
 				CyNetwork graph = Cytoscape.getCurrentNetwork();
 				boolean inactive = false;
 				if (graph == null || graph.getNodeCount() == 0)
 					inactive = true;
 				MenuElement[] popup = f_selectMenu.getSubElements();
 				if (popup[0] instanceof JPopupMenu) {
 					MenuElement[] submenus = ((JPopupMenu) popup[0])
 							.getSubElements();
 					for (int i = 0; i < submenus.length; i++) {
 						if (submenus[i] instanceof JMenuItem) {
 							if (inactive)
 								((JMenuItem) submenus[i]).setEnabled(false);
 							else
 								((JMenuItem) submenus[i]).setEnabled(true);
 						}
 					}
 				}
 			}
 		});
 		layoutMenu = menuBar.getMenu("Layout");
 		final JMenu f_layoutMenu = layoutMenu;
 		layoutMenu.addMenuListener(new MenuListener() {
 			public void menuCanceled(MenuEvent e) {
 			}
 
 			public void menuDeselected(MenuEvent e) {
 			}
 
 			public void menuSelected(MenuEvent e) {
 				CyNetworkView graphView = Cytoscape.getCurrentNetworkView();
 				boolean inactive = false;
 				if (graphView == null || graphView.nodeCount() == 0)
 					inactive = true;
 				MenuElement[] popup = f_layoutMenu.getSubElements();
 				if (popup[0] instanceof JPopupMenu) {
 					MenuElement[] submenus = ((JPopupMenu) popup[0])
 							.getSubElements();
 					for (int i = 0; i < submenus.length; i++) {
 						if (submenus[i] instanceof JMenuItem) {
 							if (inactive)
 								((JMenuItem) submenus[i]).setEnabled(false);
 							else
 								((JMenuItem) submenus[i]).setEnabled(true);
 						}
 					}
 				}
 			}
 		});
 		vizMenu = menuBar.getMenu("Visualization");
 		opsMenu = menuBar.getMenu("Plugins");
 		cytoPanelMenu = menuBar.getMenu("CytoPanels");
 		helpMenu = menuBar.getMenu("Help");
 	}
 
 	/**
 	 * This method should be called by the creator of this object after the
 	 * constructor has finished. It fills the previously created menu and tool
 	 * bars with items and action listeners that respond when those items are
 	 * activated. This needs to come after the constructor is done, because some
 	 * of the listeners try to access this object in their constructors.
 	 * 
 	 * Any calls to this method after the first will do nothing.
 	 */
 	public void initializeMenus() {
 		if (!menusInitialized) {
 			menusInitialized = true;
 			fillMenuBar();
 			fillToolBar();
 			nodesRequiredItemsEnabled = false;
 			saveButton.setEnabled(false);
 			saveSubMenu.setEnabled(false);
 			menuPrintAction.setEnabled(false);
 			menuExportAction.setEnabled(false);
 			displayNWSubMenu.setEnabled(false);
 			setNodesRequiredItemsEnabled();
 
 		}
 	}
 
 	/**
 	 * Fills the previously created menu bar with a large number of items with
 	 * attached action listener objects.
 	 */
 	private void fillMenuBar() {
 
 		// fill the Load submenu
 		addAction(new LoadGraphFileAction(this));
 		addAction(new LoadNodeAttributesAction());
 		addAction(new LoadEdgeAttributesAction());
 		addAction(new LoadExpressionMatrixAction());
 		addAction(new LoadBioDataServerAction());
 
 		// fill the Save submenu
 		addAction(new SaveAsGMLAction());
 		addAction(new SaveAsInteractionsAction());
 		addAction(new SaveNodeAttributesAction());
 		addAction(new SaveEdgeAttributesAction());
 
 		// Print Actions
 
 		menuPrintAction = new PrintAction();
 		menuExportAction = new ExportAction();
 		addAction(menuPrintAction);
 		addAction(menuExportAction);
 
 		// Exit
 		addAction(new ExitAction());
 
 		// fill the Edit menu
 		// TODO: make the Squiggle Stuff be better
 		editMenu.add(new SquiggleAction());
 		addAction(new CreateNetworkViewAction());
 		addAction(new DestroyNetworkViewAction());
 		addAction(new DestroyNetworkAction());
 		addAction(new DestroySelectedAction());
 		// add Preferences...
 		editMenu.add(new JSeparator());
 		addAction(new PreferenceAction());
 
 		// fill the Data menu --> moved to the browser plugin.
 		//addAction(new DisplayBrowserAction());
 		
 		
 		
 		
 		// addAction( new GraphObjectSelectionAction() );
 
 		// fill the Select menu
 		selectMenu.add(new SelectionModeAction());
 
 		displayNWSubMenu = menuBar.getMenu("Select.To New Network");
 
 		addAction(new InvertSelectedNodesAction());
 		addAction(new HideSelectedNodesAction());
 		addAction(new UnHideSelectedNodesAction());
 
 		addAction(new SelectAllNodesAction());
 		addAction(new DeSelectAllNodesAction());
 		addAction(new SelectFirstNeighborsAction());
 		addAction(new AlphabeticalSelectionAction());
 		addAction(new ListFromFileSelectionAction());
 
 		addAction(new InvertSelectedEdgesAction());
 		addAction(new HideSelectedEdgesAction());
 		addAction(new UnHideSelectedEdgesAction());
 		addAction(new SelectAllEdgesAction());
 		addAction(new DeSelectAllEdgesAction());
 
 		addAction(new NewWindowSelectedNodesOnlyAction());
 		addAction(new NewWindowSelectedNodesEdgesAction());
 		addAction(new CloneGraphInNewWindowAction());
 		addAction(new SelectAllAction());
 		addAction(new DeselectAllAction());
 
 		layoutMenu.add(new SpringEmbeddedLayoutMenu());
 		addAction(new RotationScaleLayoutAction());
 		layoutMenu.addSeparator();
 
 		// fill the Visualization menu
 
 		// TODO: move to a plugin, and/or fix
 		addAction(new BirdsEyeViewAction());
 		addAction(new BackgroundColorAction());
 
 		vizMenuItem = new JMenuItem(new SetVisualPropertiesAction());
 		vizMapperItem = new JMenuItem(new ToggleVisualMapperAction());
 		menuBar.getMenu("Visualization").add(vizMenuItem);
 		menuBar.getMenu("Visualization").add(vizMapperItem);
 
 		// Help menu
 		// use the usual *Action class for menu entries which have static
 		// actions
 		helpAboutMenuItem = new JMenuItem(new HelpAboutAction());
 
 		// for Contents and Context Sensitive help, don't use *Action class
 		// since actions encapsulated by HelpBroker and need run-time data
 		helpContentsMenuItem = new JMenuItem("Contents...", KeyEvent.VK_C);
 		helpContentsMenuItem.setAccelerator(KeyStroke.getKeyStroke("F1"));
 		ImageIcon contextSensitiveHelpIcon = new ImageIcon(
 				"images/contextSensitiveHelp.gif");
 		helpContextSensitiveMenuItem = new JMenuItem("Context Sensitive...",
 				contextSensitiveHelpIcon);
 		helpContextSensitiveMenuItem.setAccelerator(KeyStroke
 				.getKeyStroke("shift F1"));
 		helpMenu.add(helpContentsMenuItem);
 		helpMenu.add(helpContextSensitiveMenuItem);
 		helpMenu.addSeparator();
 		helpMenu.add(helpAboutMenuItem);
 
 	}
 
 	/**
 	 * Fills the toolbar for easy access to commonly used actions.
 	 */
 	private void fillToolBar() {
 
 		loadButton = toolBar.add(new LoadGraphFileAction(this, false));
 		loadButton.setIcon(new ImageIcon(getClass().getResource(
 				"images/new/load36.gif")));
 		loadButton.setToolTipText("Load Network");
 		loadButton.setBorderPainted(false);
 		loadButton.setRolloverEnabled(true);
 
 		saveButton = toolBar.add(new SaveAsGMLAction(false));
 		saveButton.setIcon(new ImageIcon(getClass().getResource(
 				"images/new/save36.gif")));
 		saveButton.setToolTipText("Save Network as GML");
 		saveButton.setBorderPainted(false);
 		saveButton.setRolloverEnabled(true);
 		saveButton.setEnabled(false);
 
 		toolBar.addSeparator();
 
 		final ZoomAction zoom_in = new ZoomAction(1.1);
 		zoomInButton = new JButton();
 		zoomInButton.setIcon(new ImageIcon(getClass().getResource(
 				"images/new/zoom_in36.gif")));
 		zoomInButton.setToolTipText("Zoom In");
 		zoomInButton.setBorderPainted(false);
 		zoomInButton.setRolloverEnabled(true);
 		zoomInButton.addMouseListener(new MouseListener() {
 			public void mouseClicked(MouseEvent e) {
 				zoom_in.zoom();
 			}
 
 			public void mouseEntered(MouseEvent e) {
 			}
 
 			public void mouseExited(MouseEvent e) {
 			}
 
 			public void mousePressed(MouseEvent e) {
 				zoomInButton.setSelected(true);
 			}
 
 			public void mouseReleased(MouseEvent e) {
 				zoomInButton.setSelected(false);
 			}
 		});
 
 		final ZoomAction zoom_out = new ZoomAction(0.9);
 		zoomOutButton = new JButton();
 		zoomOutButton.setIcon(new ImageIcon(getClass().getResource(
 				"images/new/zoom_out36.gif")));
 		zoomOutButton.setToolTipText("Zoom Out");
 		zoomOutButton.setBorderPainted(false);
 		zoomOutButton.setRolloverEnabled(true);
 		zoomOutButton.addMouseListener(new MouseListener() {
 			public void mouseClicked(MouseEvent e) {
 				zoom_out.zoom();
 			}
 
 			public void mouseEntered(MouseEvent e) {
 			}
 
 			public void mouseExited(MouseEvent e) {
 			}
 
 			public void mousePressed(MouseEvent e) {
 				zoomOutButton.setSelected(true);
 			}
 
 			public void mouseReleased(MouseEvent e) {
 				zoomOutButton.setSelected(false);
 			}
 		});
 
 		zoomOutButton.addMouseWheelListener(new MouseWheelListener() {
 			public void mouseWheelMoved(MouseWheelEvent e) {
 				if (e.getWheelRotation() < 0) {
 					zoom_in.zoom();
 				} else {
 					zoom_out.zoom();
 				}
 			}
 		});
 		zoomInButton.addMouseWheelListener(new MouseWheelListener() {
 			public void mouseWheelMoved(MouseWheelEvent e) {
 				if (e.getWheelRotation() < 0) {
 					zoom_in.zoom();
 				} else {
 					zoom_out.zoom();
 				}
 			}
 		});
 
 		toolBar.add(zoomOutButton);
 		toolBar.add(zoomInButton);
 
 		zoomSelectedButton = toolBar.add(new ZoomSelectedAction());
 		zoomSelectedButton.setIcon(new ImageIcon(getClass().getResource(
 				"images/new/crop36.gif")));
 		zoomSelectedButton.setToolTipText("Zoom Selected Region");
 		zoomSelectedButton.setBorderPainted(false);
 
 		zoomDisplayAllButton = toolBar.add(new FitContentAction());
 		zoomDisplayAllButton.setIcon(new ImageIcon(getClass().getResource(
 				"images/new/fit36.gif")));
 		zoomDisplayAllButton
 				.setToolTipText("Zoom out to display all of current Network");
 		zoomDisplayAllButton.setBorderPainted(false);
 
 		// toolBar.addSeparator();
 
 		showAllButton = toolBar.add(new ShowAllAction());
 		showAllButton.setIcon(new ImageIcon(getClass().getResource(
 				"images/new/add36.gif")));
 		showAllButton
 				.setToolTipText("Show all Nodes and Edges (unhiding as necessary)");
 		showAllButton.setBorderPainted(false);
 
 		hideSelectedButton = toolBar.add(new HideSelectedAction(false));
 		hideSelectedButton.setIcon(new ImageIcon(getClass().getResource(
 				"images/new/delete36.gif")));
 		hideSelectedButton.setToolTipText("Hide Selected Region");
 		hideSelectedButton.setBorderPainted(false);
 
 		toolBar.addSeparator();
 
 		annotationButton = toolBar.add(new AnnotationGui());
 		annotationButton.setIcon(new ImageIcon(getClass().getResource(
 				"images/new/ontology36.gif")));
 		annotationButton.setToolTipText("Add Annotation Ontology to Nodes");
 		annotationButton.setBorderPainted(false);
 
 		toolBar.addSeparator();
 
 		vizButton = toolBar.add(new SetVisualPropertiesAction(false));
 		vizButton.setIcon(new ImageIcon(getClass().getResource(
 				"images/new/color_wheel36.gif")));
 		vizButton.setToolTipText("Set Visual Style");
 		vizButton.setBorderPainted(false);
 
 	}// createToolBar
 
 	/**
 	 * Register the help set and help broker with the various components
 	 */
 	void initializeHelp(HelpBroker hb) {
 		hb.enableHelp(helpContentsMenuItem, "intro", null);
 		helpContentsMenuItem
 				.addActionListener(new CSH.DisplayHelpFromSource(hb));
 		helpContextSensitiveMenuItem
 				.addActionListener(new CSH.DisplayHelpAfterTracking(hb));
 
 		// add Help support for toolbar
 		hb.enableHelp(toolBar, "toolbar", null);
 
 		// add Help support for toolbar buttons
 		hb.enableHelp(loadButton, "toolbar-load", null);
 		hb.enableHelp(saveButton, "toolbar-load", null);
 		hb.enableHelp(zoomInButton, "toolbar-zoom", null);
 		hb.enableHelp(zoomOutButton, "toolbar-zoom", null);
 		hb.enableHelp(zoomSelectedButton, "toolbar-zoom", null);
 		hb.enableHelp(zoomDisplayAllButton, "toolbar-zoom", null);
 		hb.enableHelp(showAllButton, "toolbar-hide", null);
 		hb.enableHelp(hideSelectedButton, "toolbar-hide", null);
 		hb.enableHelp(annotationButton, "toolbar-annotate", null);
 		hb.enableHelp(vizButton, "toolbar-setVisProps", null);
 
 		// add Help support for visual properties combo box created elsewhere
 		// but in this toolbar
 		/*
 		 * MDA - can't get this to work... can't get access to public method?
 		 * VizMapUI vizMapUI = Cytoscape.getDesktop().getVizMapUI();
 		 * hb.enableHelp(vizMapUI.getToolbarComboBox(),
 		 * "toolbar-setVisProps",null);
 		 */
 	}
 
 }
